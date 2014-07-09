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
import java.net.ConnectException;
import java.net.SocketAddress;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import nliveroid.nlr.main.ClientHandler;

import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelException;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelPipelineException;
import org.jboss.netty.channel.ChannelSink;
import org.jboss.netty.channel.ChannelState;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.DefaultExceptionEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SucceededChannelFuture;
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
 * @version $Rev: 2144 $, $Date: 2010-02-09 12:41:12 +0900 (Tue, 09 Feb 2010) $
 *
 */
public class NioClientSocketPipelineSink implements ChannelSink {

    private static final AtomicInteger nextId = new AtomicInteger();

    final int id = nextId.incrementAndGet();
    final Executor bossExecutor;

    private final Boss[] bosses;
    private final NioWorker[] workers;

    private final AtomicInteger bossIndex = new AtomicInteger();
    private final AtomicInteger workerIndex = new AtomicInteger();

    private ClientHandler handler;

    public NioClientSocketPipelineSink(
            Executor bossExecutor, Executor workerExecutor,
            int bossCount, int workerCount,ClientHandshakeHandler handshaker,RtmpDecoder decoder,RtmpEncoder encoder,ClientHandler handler,RtmpPublisher publisher) {
    	this.bossExecutor = bossExecutor;
        this.handler = handler;
        //ここでBossとWorkerがnewされる
        bosses = new Boss[bossCount];
        for (int i = 0; i < bosses.length; i ++) {
            bosses[i] = new Boss(i + 1);
        }
        workers = new NioWorker[workerCount];
        for (int i = 0; i < workers.length; i ++) {
            workers[i] = new NioWorker(id, i + 1, workerExecutor,handshaker,decoder,encoder,handler,publisher);
        }
    }

    public void eventSunkStateEvent(ChannelEvent e) throws Exception {
            ChannelStateEvent event = (ChannelStateEvent) e;
            NioSocketChannel channel =
                (NioSocketChannel) event.getChannel();
            ChannelFuture future = event.getFuture();
            ChannelState state = event.getState();
            Object value = event.getValue();
        	Log.d("NioClientSocketPipelineSink","ChannelStateEvent  " + state + "  " + value);
            switch (state) {
            case CONNECTED:
                if (value != null) {
                    connect(channel, future, (SocketAddress) value);
                } else {
                    channel.worker.close(channel, future);
                }
                break;
            case INTEREST_OPS:
                channel.worker.setInterestOps(channel, future, ((Integer) value).intValue());
                break;
            }
    }

    public void eventSunkMessageEvent(MessageEvent e) throws Exception {
    	Log.d("NioClientSocketPipelineSink","OFFER --- ");
        NioSocketChannel channel = (NioSocketChannel) e.getChannel();
        boolean offered = channel.writeBuffer.offer(e);//書き込み予約をセットする
        assert offered;
        channel.worker.writeFromUserCode(channel);//ここが実際にNioWorkerのwrite0からSendBufferPoolに渡されて送信される
    }

    public void offerWrite(ChannelEvent e){
//    	Log.d("NioClientSocketPipelineSink","offerWrite --- " +e.getChannel().toString() +"  " + e.toString() );
        MessageEvent event = (MessageEvent) e;
        NioSocketChannel channel = (NioSocketChannel) event.getChannel();
        boolean offered = channel.writeBuffer.offer(event);
        assert offered;
        channel.worker.writeFromUserCode(channel);//ここが実際にNioWorkerのwrite0からSendBufferPoolに渡されて送信される
    }

    public void connect(
            final NioSocketChannel channel, final ChannelFuture future,
            SocketAddress remoteAddress) {
        try {
            if (channel.socket.connect(remoteAddress)) {
            	Log.d("NioClientSocketPipelineSink","Sockeet connect success");
                channel.worker.register(channel, future);//スレッドをスタートする
            } else {
                channel.getCloseFuture().addListener(new ChannelFutureListener() {
                    public void operationComplete(ChannelFuture f)
                            throws Exception {//正常終了処置
                    	Log.d("NioClientSocketPipelineSink","Socket close operationComplete");
                        if (!future.isDone()) {
                            future.setFailure(new ClosedChannelException());
                        }
                    }
                });
                future.addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
                channel.connectFuture = future;
                nextBoss().register(channel);
            }

        } catch (Throwable t) {
            future.setFailure(t);
            handler.exceptionCaught(new DefaultExceptionEvent(channel, t));
            channel.worker.close(channel, new SucceededChannelFuture(channel));
        }
    }

    public ChannelFuture close(ChannelEvent e){
        ChannelStateEvent event = (ChannelStateEvent) e;
        Object value = event.getValue();
            NioSocketChannel channel =
                (NioSocketChannel) e.getChannel();
            ChannelFuture future = e.getFuture();
    	if (Boolean.FALSE.equals(value)) {//CLOSEが作られていなく、なぜかこの値のFALSEでCLOSEするみたい→このクラスのcloseに変更
            channel.worker.close(channel, future);
    	}
    	return future;
    }

    Boss nextBoss() {
        return bosses[Math.abs(
                bossIndex.getAndIncrement() % bosses.length)];
    }

    public NioWorker nextWorker() {
        return workers[Math.abs(
                workerIndex.getAndIncrement() % workers.length)];
    }

    private final class Boss implements Runnable {

        volatile Selector selector;
        private boolean started;
        private final int subId;
        private final AtomicBoolean wakenUp = new AtomicBoolean();
        private final Object startStopLock = new Object();
        private final Queue<Runnable> registerTaskQueue = new LinkedTransferQueue<Runnable>();

        Boss(int subId) {
            this.subId = subId;
        }

        void register(NioSocketChannel channel) {
            Runnable registerTask = new RegisterTask(this, channel);
            Selector selector;

            synchronized (startStopLock) {
                if (!started) {//ド頭のストリームと接続するロジック
                    // Open a selector if this worker didn't start yet.
                    try {
                    	Log.d("NioClientSocketPipelineSink","SelectorOpen");
                        this.selector = selector =  Selector.open();
                    } catch (Throwable t) {
                        throw new ChannelException(
                                "Failed to create a selector.", t);
                    }

                    // Start the worker thread with the new Selector.
                    boolean success = false;
                    try {
                        DeadLockProofWorker.start(
                                bossExecutor,
                                new ThreadRenamingRunnable(
                                        this, "New I/O client boss #" + id + '-' + subId));
                        success = true;
                    } finally {
                        if (!success) {
                            // Release the Selector if the execution fails.
                            try {
                                selector.close();
                            } catch (Throwable t) {
                                Log.d("","Failed to close a selector."+ t);
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
                boolean offered = registerTaskQueue.offer(registerTask);
                assert offered;
            }

            if (wakenUp.compareAndSet(false, true)) {
                selector.wakeup();
            }
        }

        public void run() {
            boolean shutdown = false;
            Selector selector = this.selector;
            long lastConnectTimeoutCheckTimeNanos = System.nanoTime();
            for (;;) {
                wakenUp.set(false);

                try {
                    int selectedKeyCount = selector.select(500);

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

                    processRegisterTaskQueue();

                    if (selectedKeyCount > 0) {
                        processSelectedKeys(selector.selectedKeys());
                    }

                    // Handle connection timeout every 0.5 seconds approximately.
                    long currentTimeNanos = System.nanoTime();
                    if (currentTimeNanos - lastConnectTimeoutCheckTimeNanos >= 500 * 1000000L) {
                        lastConnectTimeoutCheckTimeNanos = currentTimeNanos;
                        processConnectTimeout(selector.keys(), currentTimeNanos);
                    }

                    // Exit the loop when there's nothing to handle.
                    // The shutdown flag is used to delay the shutdown of this
                    // loop to avoid excessive Selector creation when
                    // connection attempts are made in a one-by-one manner
                    // instead of concurrent manner.
                    if (selector.keys().isEmpty()) {
                        if (shutdown ||
                            bossExecutor instanceof ExecutorService && ((ExecutorService) bossExecutor).isShutdown()) {

                            synchronized (startStopLock) {
                                if (registerTaskQueue.isEmpty() && selector.keys().isEmpty()) {
                                    started = false;
                                    try {
                                        selector.close();
                                    } catch (IOException e) {
                                        Log.d("IOException at Nio ClientSocketPipeline",
                                                "Failed to close a selector."+ e);
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
                	Log.d("ERROR",
                            "Unexpected exception in the selector loop."+t);

                    // Prevent possible consecutive immediate failures.
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        // Ignore.
                    }
                }
            }
        }

        private void processRegisterTaskQueue() {
            for (;;) {
                final Runnable task = registerTaskQueue.poll();
                if (task == null) {
                    break;
                }

                task.run();
            }
        }

        private void processSelectedKeys(Set<SelectionKey> selectedKeys) {
            for (Iterator<SelectionKey> i = selectedKeys.iterator(); i.hasNext();) {
                SelectionKey k = i.next();
                i.remove();

                if (!k.isValid()) {
                    close(k);
                    continue;
                }

                if (k.isConnectable()) {
                    connect(k);
                }
            }
        }

        private void processConnectTimeout(Set<SelectionKey> keys, long currentTimeNanos) {
            ConnectException cause = null;
            for (SelectionKey k: keys) {
                if (!k.isValid()) {
                    close(k);
                    continue;
                }

                NioSocketChannel ch = (NioSocketChannel) k.attachment();
                if (ch.connectDeadlineNanos > 0 &&
                        currentTimeNanos >= ch.connectDeadlineNanos) {

                    if (cause == null) {
                        cause = new ConnectException("connection timed out");
                    }

                    ch.connectFuture.setFailure(cause);
                    handler.exceptionCaught(new DefaultExceptionEvent(ch, cause));
                    ch.worker.close(ch, new SucceededChannelFuture(ch));
                }
            }
        }

        private void connect(SelectionKey k) {
        	NioSocketChannel ch = (NioSocketChannel) k.attachment();
            try {
                if (ch.socket.finishConnect()) {
                    k.cancel();
                    ch.worker.register(ch, ch.connectFuture);
                }
            } catch (Throwable t) {
                ch.connectFuture.setFailure(t);
                handler.exceptionCaught(new DefaultExceptionEvent(ch, t));
                k.cancel(); // Some JDK implementations run into an infinite loop without this.
                ch.worker.close(ch, new SucceededChannelFuture(ch));
            }
        }

        private void close(SelectionKey k) {
        	NioSocketChannel ch = (NioSocketChannel) k.attachment();
            ch.worker.close(ch, new SucceededChannelFuture(ch));
        }
    }

    private static final class RegisterTask implements Runnable {
        private final Boss boss;
        private final NioSocketChannel channel;

        RegisterTask(Boss boss, NioSocketChannel channel) {
            this.boss = boss;
            this.channel = channel;
        }

        public void run() {
            try {//実際の接続はここ
                channel.socket.register(
                        boss.selector, SelectionKey.OP_CONNECT, channel);
            } catch (ClosedChannelException e) {
                channel.worker.close(channel, new SucceededChannelFuture(channel));
            }

            int connectTimeout = channel.getConfig().getConnectTimeoutMillis();
            if (connectTimeout > 0) {
                channel.connectDeadlineNanos = System.nanoTime() + connectTimeout * 1000000L;
            }
        }
    }

    /**
     * Sends an {@link ExceptionEvent} upstream with the specified
     * {@code cause}.
     *
     * @param event the {@link ChannelEvent} which caused a
     *              {@link ChannelHandler} to raise an exception
     * @param cause the exception raised by a {@link ChannelHandler}
     */
    public void exceptionCaught(
            ChannelEvent event, ChannelPipelineException cause) throws Exception {
        Throwable actualCause = cause.getCause();
        if (actualCause == null) {
            actualCause = cause;
        }

        handler.exceptionCaught(new DefaultExceptionEvent(event.getChannel(), actualCause));
    }
}
