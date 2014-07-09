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
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;
import java.util.Queue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import nliveroid.nlr.main.ClientHandler;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelException;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelState;
import org.jboss.netty.channel.DefaultChannelFuture;
import org.jboss.netty.channel.DownstreamChannelStateEvent;
import org.jboss.netty.channel.FailedChannelFuture;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SucceededChannelFuture;
import org.jboss.netty.channel.socket.nio.SocketSendBufferPool.SendBuffer;
import org.jboss.netty.util.internal.ConcurrentHashMap;
import org.jboss.netty.util.internal.LinkedTransferQueue;
import org.jboss.netty.util.internal.ThreadLocalBoolean;

import android.util.Log;

import com.flazr.rtmp.client.RtmpPublisher;
/**
 * @author <a href="http://www.jboss.org/netty/">The Netty Project</a>
 * @author <a href="http://gleamynode.net/">Trustin Lee</a>
 *
 * @version $Rev: 2202 $, $Date: 2010-02-23 16:18:58 +0900 (Tue, 23 Feb 2010) $
 *
 */
public class NioSocketChannel implements Channel{

    private final ThreadLocalBoolean notifying = new ThreadLocalBoolean();

    private static final int ST_OPEN = 0;
    private static final int ST_BOUND = 1;
    private static final int ST_CONNECTED = 2;
    private static final int ST_CLOSED = -1;
    volatile int state = ST_OPEN;

    final SocketChannel socket;
    public final NioWorker worker;
    private final DefaultNioSocketChannelConfig config;
    private volatile InetSocketAddress localAddress;
    private volatile InetSocketAddress remoteAddress;

    final Object interestOpsLock = new Object();
    final Object writeLock = new Object();

    final Runnable writeTask = new WriteTask();
    final AtomicBoolean writeTaskInTaskQueue = new AtomicBoolean();

    public final Queue<MessageEvent> writeBuffer = new WriteRequestQueue();
    final AtomicInteger mWriteBufferSize = new AtomicInteger();
    final AtomicInteger highWaterMarkCounter = new AtomicInteger();
    boolean inWriteNowLoop;
    boolean writeSuspended;

    MessageEvent currentWriteEvent;
    SendBuffer currentWriteBuffer;
	private NioClientSocketPipelineSink sink;
	private RtmpPublisher publisher;
	private ClientHandler handler;

    public NioSocketChannel(ClientHandler handler,NioClientSocketPipelineSink sink, NioWorker worker) {
        this.socket = newSocket();
        this.worker = worker;
        config = new DefaultNioSocketChannelConfig(socket.socket());
        this.sink = sink;
        this.handler = handler;
        id = allocateId(this);
    }

    public DefaultNioSocketChannelConfig getConfig() {
        return config;
    }

    public InetSocketAddress getLocalAddress() {
        InetSocketAddress localAddress = this.localAddress;
        if (localAddress == null) {
            try {
                this.localAddress = localAddress =
                    (InetSocketAddress) socket.socket().getLocalSocketAddress();
            } catch (Throwable t) {
                // Sometimes fails on a closed socket in Windows.
                return null;
            }
        }
        return localAddress;
    }

    public InetSocketAddress getRemoteAddress() {
        InetSocketAddress remoteAddress = this.remoteAddress;
        if (remoteAddress == null) {
            try {
                this.remoteAddress = remoteAddress =
                    (InetSocketAddress) socket.socket().getRemoteSocketAddress();
            } catch (Throwable t) {
                // Sometimes fails on a closed socket in Windows.
                return null;
            }
        }
        return remoteAddress;
    }

    @Override
    public boolean isOpen() {
        return state >= ST_OPEN;
    }

    public boolean isBound() {
        return state >= ST_BOUND;
    }

    public boolean isConnected() {
        return state == ST_CONNECTED;
    }

    final void setBound() {
        assert state == ST_OPEN : "Invalid state: " + state;
        state = ST_BOUND;
    }

    final void setConnected() {
        if (state != ST_CLOSED) {
            state = ST_CONNECTED;
        }
    }

    protected boolean setClosed() {
        state = ST_CLOSED;
        // Deallocate the current channel's ID from allChannels so that other
        // new channels can use it.
        allChannels.remove(id);//チャンネルから
        return closeFuture.setClosed();
    }

    @Override
    public int getInterestOps() {
        if (!isOpen()) {
            return Channel.OP_WRITE;
        }

        int interestOps = getRawInterestOps();//今のセレクターの状態を取得
        int writeBufferSize = mWriteBufferSize.get();
        if (writeBufferSize != 0) {
        	int hiWater = highWaterMarkCounter.get();
        	Log.d("NioSocketChannel","highWaterMarkCounter :" + hiWater);
            if (hiWater > 0) {//カウンタが正の時
                int lowWaterMark = getConfig().getWriteBufferLowWaterMark();
                Log.d("NioSocketChannel","writeBufferSize" + writeBufferSize);
                Log.d("NioSocketChannel","lowWaterMark" + lowWaterMark);
                if (writeBufferSize >= lowWaterMark) {
                    interestOps |= Channel.OP_WRITE;
                    Log.d("NioSocketChannel","WRITE OK L----" + lowWaterMark);
                } else {
                    interestOps &= ~Channel.OP_WRITE;//書き込みキーを取り消す
                    Log.d("NioSocketChannel","TOOLOWDATA? --- " + lowWaterMark);
//                    	try {
//							Thread.sleep(100);
//						} catch (InterruptedException e) {
//							e.printStackTrace();
//						}
//                    handler.exceptionCaught(new DefaultExceptionEvent(this, new Throwable("There are too few data.")));
                }
            } else {
                int highWaterMark = getConfig().getWriteBufferHighWaterMark();
                Log.d("NioSocketChannel","writeBufferSize" + writeBufferSize);
                Log.d("NioSocketChannel","highWaterMark" + highWaterMark);
                if (writeBufferSize >= highWaterMark) {
                    interestOps |= Channel.OP_WRITE;
//                    if(publisher.isWaitDataSend()){
                        Log.d("NioSocketChannel","WRITE OK H---- " + writeBufferSize);
//                    	publisher.restartDataSend(this);
//                    }
                } else {
                    Log.d("NioSocketChannel","TOOHIGHDATA? ---- " + writeBufferSize);
//                    if(!publisher.isWaitDataSend()){
//                        interestOps |= Channel.OP_WRITE;
//                    	publisher.waitDataSend();
//                    }else{
                    interestOps &= ~Channel.OP_WRITE;//書き込みキーを取り消す
//                    }
//                    handler.exceptionCaught(new DefaultExceptionEvent(this, new Throwable("There are too many data!!")));

                }
            }
        } else {
            interestOps &= ~Channel.OP_WRITE;//書き込みキーを取り消す
            Log.d("NioSocketChannel","elseinterestOps" + interestOps);
        }

        return interestOps;
    }

    int getRawInterestOps() {
        return interestOps;
    }

    public ChannelFuture write(Object message, SocketAddress remoteAddress) {
            return getUnsupportedOperationFuture();
    }

    private final class WriteRequestQueue extends LinkedTransferQueue<MessageEvent> {

//private static final long serialVersionUID = 1L;

        WriteRequestQueue() {
            super();
        }

        @Override
        public boolean offer(MessageEvent e) {
            boolean success = super.offer(e);
            assert success;

            int messageSize = getMessageSize(e);
            int newWriteBufferSize = mWriteBufferSize.addAndGet(messageSize);//サイズ分増やした値
            int highWaterMark = getConfig().getWriteBufferHighWaterMark();
            Log.d("NioSocketChannel","offer ---" +newWriteBufferSize );
            if (newWriteBufferSize >= highWaterMark && newWriteBufferSize - messageSize < highWaterMark) {
            	//今の(変えてる箇所はない)上限を、今のメッセージが超えたらカウンターをインクリメント
                highWaterMarkCounter.incrementAndGet();
                Log.d("NioSocketChannel","highWaterMarkCounter incrementAndGet");
                    if (!notifying.get()) {
                        Log.d("NioSocketChannel","offer notifying 0--------------" + notifying.get());
//                        notifying.set(Boolean.TRUE);
//                        notifying.set(Boolean.FALSE);
//                        Log.d("NioSocketChannel","offer notifying 1--------------" + notifying.get());
                    }
            }
            return true;
        }

        @Override
        public MessageEvent poll() {
            MessageEvent e = super.poll();
            if (e != null) {
                int messageSize = getMessageSize(e);
                int newWriteBufferSize = mWriteBufferSize.addAndGet(-messageSize);
                int lowWaterMark = getConfig().getWriteBufferLowWaterMark();

                if ((newWriteBufferSize == 0 || newWriteBufferSize < lowWaterMark) && newWriteBufferSize + messageSize >= lowWaterMark) {
                    	//メッセージサイズが0か、今のメッセージが最初に下限を下回ったらカウンターをデクリメント
                        highWaterMarkCounter.decrementAndGet();
                        Log.d("NioSocketChannel","highWaterMarkCounter decrementAndGet messageSize " + messageSize + " lowWaterMark " + lowWaterMark +" newWriteBufferSize " +newWriteBufferSize);
                        if (isConnected() && notifying.get()) {
                            Log.d("NioSocketChannel","poll notifying --------------" + notifying.get());
//                            notifying.set(Boolean.FALSE);
//                            Log.d("NioSocketChannel","poll notifying 1--------------" + notifying.get());
                        }
                 }
            }
            return e;
        }

        /**
         * ChannelBufferだったら、readableBytes()を返す
         * それ以外は0を返す
         * @param e
         * @return
         */
        private int getMessageSize(MessageEvent e) {
            Object m = e.getMessage();
            if (m instanceof ChannelBuffer) {
                return ((ChannelBuffer) m).readableBytes();
            }
            return 0;
        }
    }

    private final class WriteTask implements Runnable {

        WriteTask() {
            super();
        }

        public void run() {
            writeTaskInTaskQueue.set(false);
            worker.writeFromTaskLoop(NioSocketChannel.this);
        }
    }

//NioClientSocketChannelと上位のこのクラスを統合
    private static SocketChannel newSocket() {
        SocketChannel socket;
        try {
            socket = SocketChannel.open();
        } catch (IOException e) {
            throw new ChannelException("Failed to open a socket.", e);
        }

        boolean success = false;
        try {
            socket.configureBlocking(false);
            success = true;
        } catch (IOException e) {
            throw new ChannelException("Failed to enter non-blocking mode.", e);
        } finally {
            if (!success) {
                try {
                    socket.close();
                } catch (IOException e) {
                    Log.d("ERROR",
                            "Failed to close a partially initialized socket."
                            +e);
                }
            }
        }

        return socket;
    }

    volatile ChannelFuture connectFuture;
    volatile boolean boundManually;

    // Does not need to be volatile as it's accessed by only one thread.
    long connectDeadlineNanos;


    //AbstractChannelと統合

    static final ConcurrentMap<Integer, Channel> allChannels = new ConcurrentHashMap<Integer, Channel>();

    private static Integer allocateId(Channel channel) {
        Integer id = Integer.valueOf(System.identityHashCode(channel));
        for (;;) {
            // Loop until a unique ID is acquired.
            // It should be found in one loop practically.
            if (allChannels.putIfAbsent(id, channel) == null) {
                // Successfully acquired.
                return id;
            } else {
                // Taken by other channel at almost the same moment.
                id = Integer.valueOf(id.intValue() + 1);
            }
        }
    }

    private final Integer id;
    private final ChannelFuture succeededFuture = new SucceededChannelFuture(this);
    private final ChannelCloseFuture closeFuture = new ChannelCloseFuture();
    private volatile int interestOps = OP_READ;

    /** Cache for the string representation of this channel */
    private boolean strValConnected;
    private String strVal;



    public final Integer getId() {
        return id;
    }

    /**
     * Returns the cached {@link SucceededChannelFuture} instance.
     */
    protected ChannelFuture getSucceededFuture() {
        return succeededFuture;
    }

    /**
     * Returns the {@link FailedChannelFuture} whose cause is an
     * {@link UnsupportedOperationException}.
     */
    protected ChannelFuture getUnsupportedOperationFuture() {
        return new FailedChannelFuture(this, new UnsupportedOperationException());
    }

    /**
     * Returns the {@linkplain System#identityHashCode(Object) identity hash code}
     * of this channel.
     */
    @Override
    public final int hashCode() {
        return System.identityHashCode(this);
    }

    /**
     * Returns {@code true} if and only if the specified object is identical
     * with this channel (i.e: {@code this == o}).
     */
    @Override
    public final boolean equals(Object o) {
        return this == o;
    }

    /**
     * Compares the {@linkplain #getId() ID} of the two channels.
     */
    public final int compareTo(Channel o) {
        return getId().compareTo(o.getId());
    }



    public ChannelFuture close() {
    	try {
    		Log.d("NLiveRoid","NioSocketChannel CLOSE");
			sink.close(
			new DownstreamChannelStateEvent(
			        this, this.getCloseFuture(), ChannelState.OPEN, Boolean.FALSE));
	        assert closeFuture == this.getCloseFuture();
		} catch (Exception e) {
			e.printStackTrace();
			this.closeFuture.setFailure(new Throwable());
		}
        return closeFuture;
    }

    public ChannelFuture getCloseFuture() {
        return closeFuture;
    }

    /**
     * Sets the {@link #getInterestOps() interestOps} property of this channel
     * immediately.  This method is intended to be called by an internal
     * component - please do not call it unless you know what you are doing.
     */
    protected void setInterestOpsNow(int interestOps) {
        this.interestOps = interestOps;
    }

      //単に呼ばれてない
//    public boolean isReadable() {
//        return (getInterestOps() & OP_READ) != 0;
//    }

    public boolean isWritable() {
    	int iOps = getInterestOps();
//    	Log.d("NioSocketChannel","SIZE " +writeBuffer.size());
//    	Log.d("NioSocketChannel","iOps " + Integer.toBinaryString(iOps) + " " + Integer.toBinaryString(OP_WRITE));
        return (iOps & OP_WRITE) == 0;
    }


    /**
     * Returns the {@link String} representation of this channel.  The returned
     * string contains the {@linkplain #getId() ID}, {@linkplain #getLocalAddress() local address},
     * and {@linkplain #getRemoteAddress() remote address} of this channel for
     * easier identification.
     */
    @Override
    public String toString() {
        boolean connected = isConnected();
        if (strValConnected == connected && strVal != null) {
            return strVal;
        }

        StringBuilder buf = new StringBuilder(128);
        buf.append("[id: 0x");
        buf.append(getIdString());

        SocketAddress localAddress = getLocalAddress();
        SocketAddress remoteAddress = getRemoteAddress();
        if (remoteAddress != null) {
            buf.append(", ");
                buf.append(remoteAddress);
                buf.append(connected? " => " : " :> ");
                buf.append(localAddress);
        } else if (localAddress != null) {
            buf.append(", ");
            buf.append(localAddress);
        }

        buf.append(']');

        String strVal = buf.toString();
        this.strVal = strVal;
        strValConnected = connected;
        return strVal;
    }

    private String getIdString() {
        String answer = Integer.toHexString(id.intValue());
        switch (answer.length()) {
        case 0:
            answer = "00000000";
            break;
        case 1:
            answer = "0000000" + answer;
            break;
        case 2:
            answer = "000000" + answer;
            break;
        case 3:
            answer = "00000" + answer;
            break;
        case 4:
            answer = "0000" + answer;
            break;
        case 5:
            answer = "000" + answer;
            break;
        case 6:
            answer = "00" + answer;
            break;
        case 7:
            answer = "0" + answer;
            break;
        }
        return answer;
    }

    private final class ChannelCloseFuture extends DefaultChannelFuture {

        public ChannelCloseFuture() {
            super(NioSocketChannel.this, false);
        }

        @Override
        public boolean setSuccess() {
            // User is not supposed to call this method - ignore silently.
            return false;
        }

        @Override
        public boolean setFailure(Throwable cause) {
            // User is not supposed to call this method - ignore silently.
            return false;
        }

        boolean setClosed() {
            return setSuccess();
        }
    }

	public void setPublisher(RtmpPublisher publisher) {
		this.publisher = publisher;
	}


}
