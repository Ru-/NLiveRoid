/*
 * Flazr <http://flazr.com> Copyright (C) 2009  Peter Thomas.
 *
 * This file is part of Flazr.
 *
 * Flazr is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Flazr is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Flazr.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.flazr.rtmp.client;

import java.net.SocketAddress;

import nliveroid.nlr.main.BCPlayer;
import nliveroid.nlr.main.ClientHandler;
import nliveroid.nlr.main.LiveSettings;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBufferFactory;
import org.jboss.netty.buffer.DynamicChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelDownstreamHandler;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelState;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ChannelUpstreamHandler;
import org.jboss.netty.channel.DownstreamMessageEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SucceededChannelFuture;
import org.jboss.netty.channel.UpstreamChannelStateEvent;
import org.jboss.netty.channel.socket.nio.NioClientSocketPipelineSink;
import org.jboss.netty.channel.socket.nio.NioSocketChannel;

import android.util.Log;

import com.flazr.rtmp.RtmpHandshake;

/**
 * 【重要クラス】
 * 最初のRTMPの接続と認証をマネージする、最初のUpstreamHandler
 * @author Owner
 *
 */
public class ClientHandshakeHandler implements ChannelDownstreamHandler,ChannelUpstreamHandler {

    private final RtmpHandshake handshake;
    private ClientHandler handler;
	private NioClientSocketPipelineSink sink;

    public ClientHandshakeHandler(LiveSettings options, ClientHandler aCT) {
    	this.handler = aCT;
    	//ハンドシェイクインスタンス生成
        this.handshake = new RtmpHandshake(options);
    }

    /**
     * コネクトされたら書き込む
     */
    public void channelConnected( ChannelStateEvent e) {
    	Log.d("ClientHandshakeHandler","channelConnected handshake");
        try {
			sink.eventSunkMessageEvent(new DownstreamMessageEvent(e.getChannel(), e.getFuture(), handshake.encodeClient0(), null));
			sink.eventSunkMessageEvent(new DownstreamMessageEvent(e.getChannel(), e.getFuture(), handshake.encodeClient1(), null));
			} catch (Exception e1) {
			e1.printStackTrace();
		}
    }

    protected Object decode( NioSocketChannel channel, ChannelBuffer in) {
//RtmpHandshake.HANDSHAKE_SIZEは1536
        if(in.readableBytes() < 1 + RtmpHandshake.HANDSHAKE_SIZE * 2) {
            return null;
        }
        handshake.decodeServerAll(in);
        try {
			sink.eventSunkMessageEvent(new DownstreamMessageEvent(channel,new SucceededChannelFuture(channel), handshake.encodeClient2(), null));
		} catch (Exception e) {
			e.printStackTrace();
		}
        if(handshake.getSwfvBytes() != null) {
            handler.setSwfvBytes(handshake.getSwfvBytes());
        }

        //【重要】ここでコンテキストから自分自身(ClientHandshakeHandler)をremoveしている→ここからデータのencode/decodeが始まる(channel.getPipeline().remove(this);又はchannel.getPipeline().removeFirst();)
//        handler.removeFirst();//少しでも呼び出し元で処理を進めたいのでhandlerで弄る
    	handler.channelConnected( new UpstreamChannelStateEvent(
    			channel, ChannelState.CONNECTED, channel.getRemoteAddress()));
        return in;
    }


    @Override
    public void handleDownstream(final ChannelHandlerContext ctx, final ChannelEvent ce) {
    	Log.d("ClientHandshakeHandler","handleDownstream");
    }

	/**
	 * handshakeを取得します。
	 * @return handshake
	 */
	public RtmpHandshake getHandshake() {
	    return handshake;
	}

	//FrameDecoderと統合

    private ChannelBuffer cumulation;

    @Override
    public void handleUpstream(
               ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
       	Log.d("FrameDecoder","handleUpstream");
//           if (e instanceof MessageEvent) {
//           	Log.d("FrameDecoder","0");
//               messageReceived(ctx, (MessageEvent) e);
//           } else if (e instanceof WriteCompletionEvent) {
//           	Log.d("FrameDecoder","1");
//               WriteCompletionEvent evt = (WriteCompletionEvent) e;
//               ctx.getPipeline().sendUpstream(ctx,evt);
//           } else if (e instanceof ChildChannelStateEvent) {
//           	Log.d("FrameDecoder","2");
//               ChildChannelStateEvent evt = (ChildChannelStateEvent) e;
//               ctx.getPipeline().sendUpstream(ctx,evt);
//           } else if (e instanceof ChannelStateEvent) {
//               ChannelStateEvent evt = (ChannelStateEvent) e;
//           	Log.d("FrameDecoder","3" + evt.getState());
//               switch (evt.getState()) {
//               case OPEN:
//                   if (Boolean.TRUE.equals(evt.getValue())) {
//                	   ctx.getPipeline().sendUpstream(ctx,e);
//                   } else {
//                       channelClosed(ctx, evt);
//                   }
//                   break;
//               case BOUND:
//            	   ctx.getPipeline().sendUpstream(ctx,e);
//                   break;
//               case CONNECTED:
//                   if (evt.getValue() != null) {
//                       channelConnected( evt);
//                   } else {
//                       channelDisconnected(ctx, evt);
//                   }
//                   break;
//               case INTEREST_OPS:
//            	   ctx.getPipeline().sendUpstream(ctx,e);
//                   break;
//               default:
//                   ctx.getPipeline().sendUpstream(ctx,e);
//               }
//           } else if (e instanceof ExceptionEvent) {
//               handler.exceptionCaught((ExceptionEvent) e);
//           } else {
//           	Log.d("FrameDecoder","4");
//               ctx.getPipeline().sendUpstream(ctx,e);
//           }
       }


    public void messageReceived(
            ChannelHandlerContext ctx, MessageEvent e) throws Exception {
//        Object m = e.getMessage();
//    	Log.d("FrameDecoder","messageReceived "+(m instanceof ChannelBuffer));
//        if (!(m instanceof ChannelBuffer)) {
//            ctx.getPipeline().sendUpstream(ctx,e);
//            return;
//        }
//
//        ChannelBuffer input = (ChannelBuffer) m;
//        if (!(input.readableBytes() > 0)) {
//        	Log.d("FrameDecoder","!(input.readableBytes()>0)");
//            return;
//        }
//
//        ChannelBuffer cumulation = cumulation(ctx);
//        if (cumulation.readableBytes() > 0) {//2、3回目のreadでここ
//        	Log.d("FrameDecoder","(cumulation.readableBytes() > 0)");
//            cumulation.discardReadBytes();
//            cumulation.writeBytes(input,input.readableBytes());
//            callDecode(e.getChannel(), cumulation, e.getRemoteAddress());
//        } else {
//        	Log.d("FrameDecoder","(cumulation.readableBytes() > 0)ELLSE");
//            callDecode(e.getChannel(), input, e.getRemoteAddress());
//            if (input.readableBytes() > 0) {//初回のreadを受け取って呼ばれるのがここ
//            	Log.d("FrameDecoder","(cumulation.readableBytes() > 0)&&input.readableBytes() > 0");
//                cumulation.writeBytes(input,input.readableBytes());
//            }
//        }
    }

    public void handleReadCumulation(Channel channel,MessageEvent e) throws Exception {
    	ChannelBuffer cumulation = cumulation(channel);

        ChannelBuffer input = (ChannelBuffer) e.getMessage();
        if (cumulation.readableBytes() > 0) {//2、3回目のreadでここ
        	Log.d("FrameDecoder","(cumulation.readableBytes() > 0)");
            cumulation.discardReadBytes();
            cumulation.writeBytes(input,input.readableBytes());
            callDecode( e.getChannel(), cumulation, e.getRemoteAddress());
        } else {
        	Log.d("FrameDecoder","(cumulation.readableBytes() > 0)ELLSE");
            callDecode( e.getChannel(), input, e.getRemoteAddress());
            if (input.readableBytes() > 0) {//初回のreadを受け取って呼ばれるのがここ
            	Log.d("FrameDecoder","(cumulation.readableBytes() > 0)&&input.readableBytes() > 0");
                cumulation.writeBytes(input,input.readableBytes());
            }
        }
    }

    public void channelDisconnected(
            ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        cleanup( e);
    }

    public void channelClosed(
            ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        cleanup(e);
    }

    /**
     * Decodes the received data so far into a frame when the channel is
     * disconnected.
     *
     * @param ctx      the context of this handler
     * @param channel  the current channel
     * @param buffer   the cumulative buffer of received packets so far.
     *                 Note that the buffer might be empty, which means you
     *                 should not make an assumption that the buffer contains
     *                 at least one byte in your decoder implementation.
     *
     * @return the decoded frame if a full frame was received and decoded.
     *         {@code null} if there's not enough data in the buffer to decode a frame.
     */
    protected Object decodeLast(
            ChannelHandlerContext ctx, NioSocketChannel channel, ChannelBuffer buffer) throws Exception {
        return decode( channel, buffer);
    }

    private void callDecode(NioSocketChannel channel,
            ChannelBuffer cumulation, SocketAddress remoteAddress) throws Exception {
    	Log.d("FrameDecoder","callDecode");
        while (cumulation.readableBytes() > 0) {
            int oldReaderIndex = cumulation.readerIndex();
            Object frame = decode( channel, cumulation);//サーバ情報の整理が纏めて呼ばれる
            if (frame == null) {
                if (oldReaderIndex == cumulation.readerIndex()) {
                    // Seems like more data is required.
                    // Let us wait for the next notification.
                    break;
                } else {
                    // Previous data has been discarded.
                    // Probably it is reading on.
                    continue;
                }
            } else if (oldReaderIndex == cumulation.readerIndex()) {
                throw new IllegalStateException(
                        "decode() method must read at least one byte " +
                        "if it returned a frame (caused by: " + getClass() + ")");
            }
        }

        if (!(cumulation.readableBytes() > 0)) {
          this.cumulation = null;
        }
        Log.d("FrameDecoder","callDecode END ------------- ");
    }

    private void cleanup(ChannelStateEvent e)
            throws Exception {
    	Log.d("FrameDecoder","cleanup");
        try {
            ChannelBuffer cumulation = this.cumulation;
            if (cumulation == null) {
                return;
            } else {
                this.cumulation = null;
            }

            if (cumulation.readableBytes() > 0) {
                // Make sure all frames are read before notifying a closed channel.
                callDecode(e.getChannel(), cumulation, null);
            }

        } finally {
        	Log.d("FrameDecoder","finally");
//            ctx.getPipeline().sendUpstream(ctx,e);
        }
    }

    private ChannelBuffer cumulation(Channel ch) {
    	Log.d("ClientHandshakeHandler","Channelcumulation");
        ChannelBuffer c = cumulation;
        if (c == null) {
        	ChannelBufferFactory factory = ch.getConfig().getBufferFactory();
        	c = new DynamicChannelBuffer(factory.getDefaultOrder(), 256, factory);
            cumulation = c;
        }
        return c;
    }

	public void setSink(NioClientSocketPipelineSink sink) {
		this.sink = sink;
	}

}
