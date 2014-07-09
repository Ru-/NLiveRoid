/*
 * Copyright 2009 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package org.jboss.netty.channel.socket.nio;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.NotYetConnectedException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import nliveroid.nlr.main.ClientHandler;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBufferFactory;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelException;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.DefaultExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.ReceiveBufferSizePredictor;
import org.jboss.netty.channel.SucceededChannelFuture;
import org.jboss.netty.channel.UpstreamMessageEvent;
import org.jboss.netty.channel.socket.nio.SocketSendBufferPool.SendBuffer;
import org.jboss.netty.util.ThreadRenamingRunnable;
import org.jboss.netty.util.internal.DeadLockProofWorker;
import org.jboss.netty.util.internal.LinkedTransferQueue;

import android.util.Log;

import com.flazr.rtmp.RtmpDecoder;
import com.flazr.rtmp.RtmpEncoder;
import com.flazr.rtmp.client.ClientHandshakeHandler;
import com.flazr.rtmp.client.RtmpPublisher;

/**
 *
 * @author <a href="http://www.jboss.org/netty/">The Netty Project</a>
 * @author <a href="http://gleamynode.net/">Trustin Lee</a>
 *
 * @version $Rev: 2376 $, $Date: 2010-10-25 03:24:20 +0900 (Mon, 25 Oct 2010) $
 *
 */
public class NioWorker implements Runnable {

    private static final int CONSTRAINT_LEVEL = NioProviderMetadata.CONSTRAINT_LEVEL;

    static final int CLEANUP_INTERVAL = 256; // XXX Hard-coded value, but won't need customization.

    private final int bossId;
    private final int id;
    private final Executor executor;
    private boolean started;
    private volatile Thread thread;
    volatile Selector selector;
    private final AtomicBoolean wakenUp = new AtomicBoolean();
    private final ReadWriteLock selectorGuard = new ReentrantReadWriteLock();
    private final Object startStopLock = new Object();
    private final LinkedTransferQueue<Runnable> registerTaskQueue = new LinkedTransferQueue<Runnable>();
    private final LinkedTransferQueue<Runnable> writeTaskQueue = new LinkedTransferQueue<Runnable>();
    private volatile int cancelledKeys; // should use AtomicInteger but we just need approximation

    private final SocketReceiveBufferPool recvBufferPool = new SocketReceiveBufferPool();
    private final SocketSendBufferPool sendBufferPool = new SocketSendBufferPool();

    private long failedCount = 0;

	private ClientHandshakeHandler handshaker;
	private RtmpDecoder decoder;
	private RtmpEncoder encoder;
	private ClientHandler handler;
	private RtmpPublisher publisher;

    NioWorker(int bossId, int id, Executor executor,ClientHandshakeHandler handshaker,RtmpDecoder decoder,RtmpEncoder encoder,ClientHandler handler,RtmpPublisher publisher) {
        this.bossId = bossId;
        this.id = id;
        this.executor = executor;
        this.handshaker = handshaker;
        this.decoder = decoder;
        this.encoder = encoder;
        this.handler = handler;
        this.publisher = publisher;
    }

    void register(NioSocketChannel channel, ChannelFuture future) {
        Runnable registerTask = new RegisterTask(channel, future);
        Selector selector;

        synchronized (startStopLock) {

            if (!started) {
                // Open a selector if this worker didn't start yet.
                try {
                    this.selector = selector = Selector.open();
                } catch (Throwable t) {
                    throw new ChannelException(
                            "Failed to create a selector.", t);
                }

                // Start the worker thread with the new Selector.
                String threadName = "New I/O client worker #" + bossId + '-' + id;

                boolean success = false;
                try {//引数のスレッドを名前で参照して、このクラスのRUNを、非同期で新たなスレッドとして走らせている
                    DeadLockProofWorker.start(
                            executor, new ThreadRenamingRunnable(this, threadName));
                    success = true;
                } finally {
                    if (!success) {//失敗したら片付ける
                    	Log.d("NioWorker","register failed and CLOSE");
                        // Release the Selector if the execution fails.
                        try {
                            selector.close();
                        } catch (Throwable t) {
                        	Log.d("NioWorker","Failed to close a selector.", t);
                        }
                        this.selector = selector = null;
                        // The method will return to the caller at this point.
                    }
                }
            } else {
                // Use the existing selector if this worker has been started.
                selector = this.selector;
            }

            assert selector != null && selector.isOpen();

            started = true;
            //実際に通信するタスクをキューイングする
            boolean offered = registerTaskQueue.offer(registerTask);
            assert offered;
        }

        if (wakenUp.compareAndSet(false, true)) {
            selector.wakeup();
        }
    }

    //上のregisterのDeadLockProofWorkerで非同期でスタートされる
    public void run() {
//    	Log.d("NioWorker","Async Run ------- boss" + bossId + " worker" + id + " level"+CONSTRAINT_LEVEL);
        thread = Thread.currentThread();

        boolean shutdown = false;
        Selector selector = this.selector;
        for (;;) {
            wakenUp.set(false);

            if (CONSTRAINT_LEVEL != 0) {
                selectorGuard.writeLock().lock();
                    // This empty synchronization block prevents the selector
                    // from acquiring its lock.
                selectorGuard.writeLock().unlock();
            }

            try {
            	try {
                    selector.select(500);
                } catch (CancelledKeyException e) {
                    // Harmless exception - log anyway
                	Log.d("SelectorUtil",
                            CancelledKeyException.class.getSimpleName() +
                            " raised by a Selector - JDK bug?", e);
                }

                // 'wakenUp.compareAndSet(false, true)' is always evaluated
                // before calling 'selector.wakeup()' to reduce the wake-up
                // overhead. (Selector.wakeup() is an expensive operation.)
                //
                // However, there is a race condition in this approach.
                // The race condition is triggered when 'wakenUp' is set to
                // true too early.
                //
                // 'wakenUp' is set to true too early if:
                // 1) Selector is waken up between 'wakenUp.set(false)' and
                //    'selector.select(...)'. (BAD)
                // 2) Selector is waken up between 'selector.select(...)' and
                //    'if (wakenUp.get()) { ... }'. (OK)
                //
                // In the first case, 'wakenUp' is set to true and the
                // following 'selector.select(...)' will wake up immediately.
                // Until 'wakenUp' is set to false again in the next round,
                // 'wakenUp.compareAndSet(false, true)' will fail, and therefore
                // any attempt to wake up the Selector will fail, too, causing
                // the following 'selector.select(...)' call to block
                // unnecessarily.
                //
                // To fix this problem, we wake up the selector again if wakenUp
                // is true immediately after selector.select(...).
                // It is inefficient in that it wakes up the selector for both
                // the first case (BAD - wake-up required) and the second case
                // (OK - no wake-up required).

                if (wakenUp.get()) {
                    selector.wakeup();
                }

                cancelledKeys = 0;
                //registerTaskQueueと、WriteTaskQueueそれぞれ実行する
                runRegisterTaskQueue();
                runWriteTaskQueue();
                processSelectedKeys(selector.selectedKeys());

                // Exit the loop when there's nothing to handle.
                // The shutdown flag is used to delay the shutdown of this
                // loop to avoid excessive Selector creation when
                // connections are registered in a one-by-one manner instead of
                // concurrent manner.
                if (selector.keys().isEmpty()) {
                    if (shutdown ||
                        executor instanceof ExecutorService && ((ExecutorService) executor).isShutdown()) {
                    	//終了処置　通信タスクが空
                        synchronized (startStopLock) {
                            if (registerTaskQueue.isEmpty() && selector.keys().isEmpty()) {
                                started = false;
                                try {
                                	Log.d("NioWorker",
                                            "selector.close()");
                                	Channels.streamFase = 0;
                                    selector.close();
                                } catch (IOException e) {
                                	Log.d("NioWorker",
                                            "Failed to close a selector"+ e);
                                } finally {
                                    this.selector = null;
                                }
                                break;
                            } else {
                                shutdown = false;
                            }
                        }
                    } else {
                        // Give one more second.
                        shutdown = true;
                    }
                } else {
                    shutdown = false;
                }
            } catch (Throwable t) {
            	Log.d("NioWorker",
                        "Unexpected exception in the selector loop.", t);
                // Prevent possible consecutive immediate failures that lead to
                // excessive CPU consumption.
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // Ignore.
                }
            }
        }
    }

    private boolean cleanUpCancelledKeys() throws IOException {
        if (cancelledKeys >= CLEANUP_INTERVAL) {
            cancelledKeys = 0;
            selector.selectNow();
            return true;
        }
        return false;
    }

    private void runRegisterTaskQueue() throws IOException {
//    	Log.d("NioWorker","runRegisterTaskQueue");
        for (;;) {
            final Runnable task = registerTaskQueue.poll();
            if (task == null) {
                break;
            }
            task.run();
            cleanUpCancelledKeys();
        }
    }

    private void runWriteTaskQueue() throws IOException {
//    	Log.d("NioWorker","runWriteTaskQueue");
        for (;;) {
            final Runnable task = writeTaskQueue.poll();
            if (task == null) {
                break;
            }
            task.run();
            cleanUpCancelledKeys();
        }
    }

    private void processSelectedKeys(Set<SelectionKey> selectedKeys) throws IOException {
//    	Log.d("NioWorker","processSelectedKeys");
        for (Iterator<SelectionKey> i = selectedKeys.iterator(); i.hasNext();) {
            SelectionKey k = i.next();
            i.remove();
            try {
                int readyOps = k.readyOps();
                if ((readyOps & SelectionKey.OP_READ) != 0 || readyOps == 0) {
                    if (!read(k)) {
                        // Connection already closed - no need to handle write.
                        continue;
                    }
                }
                if ((readyOps & SelectionKey.OP_WRITE) != 0) {
                    writeFromSelectorLoop(k);
                }
            } catch (CancelledKeyException e) {
                close(k);
            }

            if (cleanUpCancelledKeys()) {
                break; // break the loop to avoid ConcurrentModificationException
            }
        }
    }

/**
 * ここに通信で受け取ったものが入ってくる
 * @param k
 * @return
 */
    private boolean read(SelectionKey k) {
    	Log.d("NioWorker","read P");
        final SocketChannel ch = (SocketChannel) k.channel();
        final NioSocketChannel channel = (NioSocketChannel) k.attachment();

        final ReceiveBufferSizePredictor predictor =
            channel.getConfig().getReceiveBufferSizePredictor();
        final int predictedRecvBufSize = predictor.nextReceiveBufferSize();

        int ret = 0;
        int readBytes = 0;
        boolean failure = true;

        ByteBuffer bb = recvBufferPool.acquire(predictedRecvBufSize);
        try {
            while ((ret = ch.read(bb)) > 0) {
                readBytes += ret;
                if (!bb.hasRemaining()) {
                    break;
                }
            }
            failure = false;
        } catch (ClosedChannelException e) {
            // Can happen, and does not need a user attention.
            handler.exceptionCaught(new DefaultExceptionEvent(channel, new Throwable("ClosedChannelException at NioWorker")));
        } catch (Throwable t) {
            handler.exceptionCaught(new DefaultExceptionEvent(channel, t));
        }

        if (readBytes > 0) {
            bb.flip();

            final ChannelBufferFactory bufferFactory =
                channel.getConfig().getBufferFactory();
            final ChannelBuffer buffer = bufferFactory.getBuffer(readBytes);
            buffer.setBytes(0, bb);
            buffer.writerIndex(readBytes);

            recvBufferPool.release(bb);

            // Update the predictor.
            predictor.previousReceiveBufferSize(readBytes);

            // Fire the event.
            if(Channels.streamFase == 0){//初回の接続後のサーバ情報のパケットを受け取る場合ここ
        		try {
    				handshaker.handleReadCumulation(channel, new UpstreamMessageEvent(channel, buffer, channel.getRemoteAddress()));
    			} catch (Exception e) {
    				e.printStackTrace();
    			}
            }else if(Channels.streamFase == 1){
            	try {
            		decoder.handleReadCumulation(channel, new UpstreamMessageEvent(channel, buffer, channel.getRemoteAddress()));
				} catch (Exception e) {
					e.printStackTrace();
				}
             }
        } else {
            recvBufferPool.release(bb);
        }

        if (ret < 0 || failure) {
            k.cancel(); // Some JDK implementations run into an infinite loop without this.
            close(channel, new SucceededChannelFuture(channel));
            return false;
        }

        return true;
    }

    private void close(SelectionKey k) {
        NioSocketChannel ch = (NioSocketChannel) k.attachment();
        close(ch, new SucceededChannelFuture(ch));
    }

    public void writeFromUserCode(final NioSocketChannel channel) {
        if (!channel.isConnected()) {
        	Log.d("NioWorker_","!isConnected");
            cleanUpWriteBuffer(channel);
            return;
        }

        if (scheduleWriteIfNecessary(channel)) {
//        	Log.d("NioWorker_","scheduleWriteIfNecessary");
            return;
        }

        // From here, we are sure Thread.currentThread() == workerThread.

        if (channel.writeSuspended) {
        	Log.d("NioWorker_","writeSuspended");
            return;
        }

        if (channel.inWriteNowLoop) {
        	Log.d("NioWorker_","inWriteNowLoop");
            return;
        }
        write0(channel);
    }

    void writeFromTaskLoop(final NioSocketChannel ch) {
        if (!ch.writeSuspended) {
            write0(ch);
        }
    }

    void writeFromSelectorLoop(final SelectionKey k) {
        NioSocketChannel ch = (NioSocketChannel) k.attachment();
        ch.writeSuspended = false;
        write0(ch);
    }

    private boolean scheduleWriteIfNecessary(final NioSocketChannel channel) {
        final Thread currentThread = Thread.currentThread();
        final Thread workerThread = thread;
        if (currentThread != workerThread) {
            if (channel.writeTaskInTaskQueue.compareAndSet(false, true)) {
                boolean offered = writeTaskQueue.offer(channel.writeTask);
                assert offered;
            }
                final Selector workerSelector = selector;
                if (workerSelector != null) {
                    if (wakenUp.compareAndSet(false, true)) {
                        workerSelector.wakeup();
                    }
                }
            return true;
        }

        return false;
    }

    private void write0(NioSocketChannel channel) {
        boolean open = true;
        boolean addOpWrite = false;
        boolean removeOpWrite = false;

        long writtenBytes = 0;

        final SocketSendBufferPool sendBufferPool = this.sendBufferPool;
        final SocketChannel ch = channel.socket;
        final Queue<MessageEvent> writeBuffer = channel.writeBuffer;
        final int writeSpinCount = channel.getConfig().getWriteSpinCount();
        synchronized (channel.writeLock) {
            channel.inWriteNowLoop = true;
            for (;;) {
                MessageEvent evt = channel.currentWriteEvent;
                SendBuffer buf;
                if (evt == null) {
                //ここでキューからタスクが取得されている
                    if ((channel.currentWriteEvent = evt = writeBuffer.poll()) == null) {
                        removeOpWrite = true;//nullならremoveフラグ有効にする
                        channel.writeSuspended = false;
                        break;
                    }

                    channel.currentWriteBuffer = buf = sendBufferPool.acquire(evt.getMessage());
                } else {
                    buf = channel.currentWriteBuffer;
                }

                ChannelFuture future = evt.getFuture();
                try {
                    long localWrittenBytes = 0;
                    for (int i = writeSpinCount; i > 0; i --) {
                        localWrittenBytes = buf.transferTo(ch);
                        if (localWrittenBytes != 0) {
                            writtenBytes += localWrittenBytes;
                            break;
                        }
                        if (buf.finished()) {
                            break;
                        }
                    }

                    if (buf.finished()) {
                        // Successful write - proceed to the next message.
                        buf.release();
                        channel.currentWriteEvent = null;
                        channel.currentWriteBuffer = null;
                        evt = null;
                        buf = null;
                        future.setSuccess();
                    } else {//kernel bufferが一杯らしいから書き込みできない
                        // Not written fully - perhaps the kernel buffer is full.
                        addOpWrite = true;
                        channel.writeSuspended = true;
                        this.failedCount += localWrittenBytes;
                        Log.d("NioWorker","failedAmount" + failedCount);
                        Log.d("NioWorker","localWrittenBytes " + localWrittenBytes + " writtenBytes "+buf.writtenBytes() +" totalBytes "+buf.totalBytes());
                        if (localWrittenBytes > 0) {
                            // Notify progress listeners if necessary.
                            future.setProgress(
                                    localWrittenBytes,
                                    buf.writtenBytes(), buf.totalBytes());
                        }
                        break;
                    }
                } catch (AsynchronousCloseException e) {
                    Log.d("NioWorker","AsynchronousCloseException");
                    // Doesn't need a user attention - ignore.
                } catch (Throwable t) {
                    Log.d("NioWorker","Throwable writtenBytes "+buf.writtenBytes() +" totalBytes "+buf.totalBytes());
                    buf.release();
                    channel.currentWriteEvent = null;
                    channel.currentWriteBuffer = null;
                    buf = null;
                    evt = null;
                    future.setFailure(t);
                    handler.exceptionCaught(new DefaultExceptionEvent(channel, t));
                    if (t instanceof IOException) {
                        open = false;
                        close(channel, new SucceededChannelFuture(channel));
                    }
                }
//            	Log.d("NioWorker","write0 " + writeBuffer.size());
            }
            channel.inWriteNowLoop = false;

            // Initially, the following block was executed after releasing
            // the writeLock, but there was a race condition, and it has to be
            // executed before releasing the writeLock:
            //
            //     https://issues.jboss.org/browse/NETTY-410
            //
            if (open) {
                if (addOpWrite) {
                    setOpWrite(channel);
                } else if (removeOpWrite) {
                    clearOpWrite(channel);
                }
            }
        }
        if (writtenBytes == 0) {
            return;
        }
//        Log.d("NioWorker","fireWriteComplete ----" + writtenBytes);
    }

    private void setOpWrite(NioSocketChannel channel) {
        Selector selector = this.selector;
        SelectionKey key = channel.socket.keyFor(selector);
        if (key == null) {
            return;
        }
        if (!key.isValid()) {
            close(key);
            return;
        }

        // interestOps can change at any time and at any thread.
        // Acquire a lock to avoid possible race condition.
        synchronized (channel.interestOpsLock) {
            int interestOps = channel.getRawInterestOps();
            if ((interestOps & SelectionKey.OP_WRITE) == 0) {
                interestOps |= SelectionKey.OP_WRITE;
                key.interestOps(interestOps);
                channel.setInterestOpsNow(interestOps);
            }
        }
    }

    private void clearOpWrite(NioSocketChannel channel) {
        Selector selector = this.selector;
        SelectionKey key = channel.socket.keyFor(selector);
        if (key == null) {
            return;
        }
        if (!key.isValid()) {
            close(key);
            return;
        }

        // interestOps can change at any time and at any thread.
        // Acquire a lock to avoid possible race condition.
        synchronized (channel.interestOpsLock) {
            int interestOps = channel.getRawInterestOps();
            if ((interestOps & SelectionKey.OP_WRITE) != 0) {
                interestOps &= ~SelectionKey.OP_WRITE;
                key.interestOps(interestOps);
                channel.setInterestOpsNow(interestOps);
            }
        }
    }

    void close(NioSocketChannel channel, ChannelFuture future) {
    	Log.d("NioWorker","Close ---");
    	Channels.streamFase = 0;
        boolean connected = channel.isConnected();
        boolean bound = channel.isBound();
        try {
            channel.socket.close();
            cancelledKeys ++;

            if (channel.setClosed()) {//ほとんど失敗しない
            	Log.d("NioWorker","fireChannelClosed");
                future.setSuccess();
                cleanUpWriteBuffer(channel);//結局は各RtmpReaderのcloseが呼ばれる
                handler.stopStream(channel);
            } else {
                future.setSuccess();
            }
        } catch (Throwable t) {
            future.setFailure(t);
            handler.exceptionCaught(new DefaultExceptionEvent(channel, t));
        }
    }

    private void cleanUpWriteBuffer(NioSocketChannel channel) {
    	Log.d("NioWorker","cleanUpWriteBuffer ");
        Exception cause = null;
        boolean fireExceptionCaught = false;

        // Clean up the stale messages in the write buffer.
        synchronized (channel.writeLock) {
            MessageEvent evt = channel.currentWriteEvent;
            if (evt != null) {
                // Create the exception only once to avoid the excessive overhead
                // caused by fillStackTrace.
                if (channel.isOpen()) {
                    cause = new NotYetConnectedException();
                } else {
                    cause = new ClosedChannelException();
                }

                ChannelFuture future = evt.getFuture();
                channel.currentWriteBuffer.release();
                channel.currentWriteBuffer = null;
                channel.currentWriteEvent = null;
                evt = null;
                future.setFailure(cause);
                fireExceptionCaught = true;
            }

            Queue<MessageEvent> writeBuffer = channel.writeBuffer;
            if (!writeBuffer.isEmpty()) {
                // Create the exception only once to avoid the excessive overhead
                // caused by fillStackTrace.
                if (cause == null) {
                    if (channel.isOpen()) {
                        cause = new NotYetConnectedException();
                    } else {
                        cause = new ClosedChannelException();
                    }
                }

                for (;;) {
                    evt = writeBuffer.poll();
                    if (evt == null) {
                        break;
                    }
                    evt.getFuture().setFailure(cause);
                    fireExceptionCaught = true;
                }
            }
        }

        if (fireExceptionCaught) {
        	Log.d("NioWorker","Exception ----- " + cause);
        	if(cause == null || cause.getMessage() == null || cause.getMessage().contains("ClosedChannelException")){//無限ループを防ぐ
        		Log.d("NioWorker","Cause contains ClosedChannelException!!");
        		return;
        	}
            handler.exceptionCaught(new DefaultExceptionEvent(channel, cause));
        }
    }

    void setInterestOps(
            NioSocketChannel channel, ChannelFuture future, int interestOps) {
        boolean changed = false;
        try {
            // interestOps can change at any time and at any thread.
            // Acquire a lock to avoid possible race condition.
            synchronized (channel.interestOpsLock) {
                Selector selector = this.selector;
                SelectionKey key = channel.socket.keyFor(selector);

                if (key == null || selector == null) {
                    // Not registered to the worker yet.
                    // Set the rawInterestOps immediately; RegisterTask will pick it up.
                    channel.setInterestOpsNow(interestOps);
                    return;
                }

                // Override OP_WRITE flag - a user cannot change this flag.
                interestOps &= ~Channel.OP_WRITE;
                interestOps |= channel.getRawInterestOps() & Channel.OP_WRITE;

                switch (CONSTRAINT_LEVEL) {
                case 0:
                    if (channel.getRawInterestOps() != interestOps) {
                        key.interestOps(interestOps);
                        if (Thread.currentThread() != thread &&
                            wakenUp.compareAndSet(false, true)) {
                            selector.wakeup();
                        }
                        changed = true;
                    }
                    break;
                case 1:
                case 2:
                    if (channel.getRawInterestOps() != interestOps) {
                        if (Thread.currentThread() == thread) {
                            key.interestOps(interestOps);
                            changed = true;
                        } else {
                            selectorGuard.readLock().lock();
                            try {
                                if (wakenUp.compareAndSet(false, true)) {
                                    selector.wakeup();
                                }
                                key.interestOps(interestOps);
                                changed = true;
                            } finally {
                                selectorGuard.readLock().unlock();
                            }
                        }
                    }
                    break;
                default:
                    throw new Error();
                }

                if (changed) {
                    channel.setInterestOpsNow(interestOps);
                }
            }

            future.setSuccess();
        } catch (CancelledKeyException e) {
            // setInterestOps() was called on a closed channel.
            ClosedChannelException cce = new ClosedChannelException();
            future.setFailure(cce);
            handler.exceptionCaught(new DefaultExceptionEvent(channel, cce));
        } catch (Throwable t) {
            future.setFailure(t);
            handler.exceptionCaught(new DefaultExceptionEvent(channel, t));
        }
    }

    /**[超重要]
     *
     *実際に通信するタスク
     *ド頭の接続のfutureはここで結果が入れられる
     *
     */
    private final class RegisterTask implements Runnable {
        private final NioSocketChannel channel;
        private final ChannelFuture future;

        RegisterTask(
                NioSocketChannel channel, ChannelFuture future) {
            this.channel = channel;
            this.future = future;
        }
        public void run() {
//        	Log.d("NioWorker","RegisterTask RUN --- ");
            SocketAddress remoteAddress = channel.getRemoteAddress();
            try {
                synchronized (channel.interestOpsLock) {
                    channel.socket.register(
                            selector, channel.getRawInterestOps(), channel);
                }
                if (future != null) {
                    channel.setConnected();
                    future.setSuccess();//nullじゃなければ成功としてるwww
                }
            } catch (IOException e) {
                if (future != null) {
                    future.setFailure(e);
                }
                close(channel, new SucceededChannelFuture(channel));
                if (!(e instanceof ClosedChannelException)) {
                    throw new ChannelException(
                            "Failed to register a socket to the selector.", e);
                }
            }
            //ロックがchannel.socket.register部分だけだからか?ここは呼び出しもとのfutureの結果が返るより後で呼ばれることがある
            if (!((NioSocketChannel) channel).boundManually) {
            	Log.d("NioWorker","RegisterTask not bound manually");
//                fireChannelBound(channel, localAddress);//何もやってないっぽい
            }
        	Log.d("NioWorker","RegisterTask done.");
//            fireChannelConnected(channel, remoteAddress);
        	//呼び出し元で、future.isSuccess()が確認できたらデータ送信ループ開始
        }
    }
}
