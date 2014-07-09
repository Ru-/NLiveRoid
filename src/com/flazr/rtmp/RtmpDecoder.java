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

package com.flazr.rtmp;

import java.net.SocketAddress;
import java.util.concurrent.atomic.AtomicReference;

import nliveroid.nlr.main.BCPlayer;
import nliveroid.nlr.main.ClientHandler;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBufferFactory;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.buffer.DynamicChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ChannelUpstreamHandler;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.UpstreamMessageEvent;
import org.jboss.netty.channel.socket.nio.NioSocketChannel;
import org.jboss.netty.handler.codec.replay.ReplayingDecoderBuffer;

import android.util.Log;

import com.flazr.rtmp.RtmpDecoder.DecoderState;
import com.flazr.rtmp.message.ChunkSize;
import com.flazr.rtmp.message.MessageType;



		public class RtmpDecoder<DecoderState> implements ChannelUpstreamHandler {
			private ClientHandler handler;

		enum DecoderState {
		    GET_HEADER,
		    GET_PAYLOAD
		}


		private final AtomicReference<ChannelBuffer> cumulation =
		new AtomicReference<ChannelBuffer>();
		private final boolean unfold = false;
		private ReplayingDecoderBuffer replayable;
		private DecoderState state = DecoderState.GET_HEADER;
		private int checkpoint;


		public RtmpDecoder(ClientHandler aCT) {
			this.handler = aCT;
		}



		@Override
		public void handleUpstream(
		       ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
		   	Log.d("RtmpDecoder","handleUpstream");
//		   if (e instanceof MessageEvent) {
//		   	Log.d("RtmpDecoder","0");
//		       messageReceived(ctx, (MessageEvent) e);
//		   } else if (e instanceof WriteCompletionEvent) {
//		   	Log.d("RtmpDecoder","1");
//		       WriteCompletionEvent evt = (WriteCompletionEvent) e;
//		       ctx.getPipeline().sendUpstream(ctx,evt);
//		   } else if (e instanceof ChildChannelStateEvent) {
//		   	Log.d("RtmpDecoder","2");
//		       ChildChannelStateEvent evt = (ChildChannelStateEvent) e;
//		       ctx.getPipeline().sendUpstream(ctx,evt);
//		   } else if (e instanceof ChannelStateEvent) {
//		       ChannelStateEvent evt = (ChannelStateEvent) e;
//		   	Log.d("RtmpDecoder","3" + evt.getState());
//		       switch (evt.getState()) {
//		       case OPEN:
//		           if (Boolean.TRUE.equals(evt.getValue())) {
//		        	   ctx.getPipeline().sendUpstream(ctx,evt);
//		           } else {
//		               channelClosed(ctx, evt);
//		           }
//		           break;
//		       case BOUND:
//		    	   ctx.getPipeline().sendUpstream(ctx,evt);
//		           break;
//		       case CONNECTED:
//		           if (evt.getValue() != null) {
//		        	   ctx.getPipeline().sendUpstream(ctx,evt);
//		           } else {
//		               channelDisconnected(ctx, evt);
//		           }
//		           break;
//		       case INTEREST_OPS://何もやってねー
////		    	   ctx.getPipeline().sendUpstream(ctx,evt);
//		           break;
//		       default:
//		    	   ctx.getPipeline().sendUpstream(ctx,evt);
//		       }
//		   } else if (e instanceof ExceptionEvent) {
//				Log.d("RtmpDecoder ","exceptionCaught");
//		       exceptionCaught(ctx, (ExceptionEvent) e);
//		   } else {
//		   	Log.d("RtmpDecoder","4");
//		       ctx.getPipeline().sendUpstream(ctx,e);
//		   }
		}



		/**
		* Stores the internal cumulative buffer's reader position.
		*/
		protected void checkpoint() {
		ChannelBuffer cumulation = this.cumulation.get();
		if (cumulation != null) {
		    checkpoint = cumulation.readerIndex();
		} else {
		    checkpoint = -1; // buffer not available (already cleaned up)
		}
		}

		/**
		* Stores the internal cumulative buffer's reader position and updates
		* the current decoder state.
		*/
		protected void checkpoint(DecoderState state) {
		checkpoint();
		setState(state);
		}

		/**
		* Returns the current state of this decoder.
		* @return the current state of this decoder
		*/
		protected DecoderState getState() {
		return state;
		}

		/**
		* Sets the current state of this decoder.
		* @return the old state of this decoder
		*/
		protected DecoderState setState(DecoderState newState) {
			DecoderState oldState = state;
		state = newState;
		return oldState;
		}

		/**
		* Returns the actual number of readable bytes in the internal cumulative
		* buffer of this decoder.  You usually do not need to rely on this value
		* to write a decoder.  Use it only when you muse use it at your own risk.
		* This method is a shortcut to {@link #internalBuffer() internalBuffer().readableBytes()}.
		*/
		protected int actualReadableBytes() {
		return internalBuffer().readableBytes();
		}

		/**
		* Returns the internal cumulative buffer of this decoder.  You usually
		* do not need to access the internal buffer directly to write a decoder.
		* Use it only when you must use it at your own risk.
		*/
		protected ChannelBuffer internalBuffer() {
		ChannelBuffer buf = cumulation.get();
		if (buf == null) {
		    return ChannelBuffers.EMPTY_BUFFER;
		}
		return buf;
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
		* @param state    the current decoder state ({@code null} if unused)
		*
		* @return the decoded frame
		*/
		protected Object decodeLast( Channel channel, ChannelBuffer buffer, DecoderState state) throws Exception {
		return decode( channel, buffer, state);
		}

		/**
		* メインのヘッダ、ペイロードの送信要求が入ってくる
		* @param ctx
		* @param e
		* @throws Exception
		*/
		public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
		    throws Exception {
       	 Log.d("RtmpDecoder","messageReceived");
//		Object m = e.getMessage();
//		if (!(m instanceof ChannelBuffer)) {
//			/*
//			 * ここに来る時は、mのクラス名はcom.flazr.rtmp.RtmpPublisher$Conversation
//			 * ctx.getName()はdecoder
//			 */
//
//			try {//直接呼んでしまう
//		    	 DefaultChannelHandlerContext next = ((DefaultChannelPipeline) ctx.getPipeline()).getActualUpstreamContext(((DefaultChannelHandlerContext)ctx).next);
//		         if (next != null) {
//		        	 Log.d("RtmpDecoder","sendUp" +next.getName());
//		             ((ChannelUpstreamHandler) next.getHandler()).handleUpstream(next, e);
//		         }else{
//		        	 Log.d("END sendUp:: ","");
//		         }
//		    } catch (Throwable t) {
//		    	((DefaultChannelPipeline) ctx.getPipeline()).notifyHandlerException(e, t);
//		    }
//		    return;
//		}
//
//		ChannelBuffer input = (ChannelBuffer) m;
//		if (!(input.readableBytes() > 0)) {
//	      	 Log.d("RtmpDecoder","!(input.readableBytes() > 0)");
//		    return;
//		}
//
//     	 Log.d("RtmpDecoder","cumulation");
//		ChannelBuffer cumulation = cumulation(ctx);
//		cumulation.discardReadBytes();
//		cumulation.writeBytes(input,input.readableBytes());
//		callDecode(e.getChannel(), cumulation, e.getRemoteAddress());
		}

		public void handleReadCumulation(Channel channel,MessageEvent e)throws Exception {
			ChannelBuffer input = (ChannelBuffer) e.getMessage();
			ChannelBuffer cumulation = cumulation(channel);
			cumulation.discardReadBytes();
			cumulation.writeBytes(input,input.readableBytes());
			
			callDecode( e.getChannel(), cumulation, e.getRemoteAddress());
		}


		public void channelDisconnected(
		    ChannelStateEvent e) throws Exception {
		cleanup(e);
		}

		public void channelClosed(ChannelStateEvent e) throws Exception {
		cleanup(e);
		}

		public void exceptionCaught(ExceptionEvent e)
		    throws Exception {
		handler.exceptionCaught(e);
		}

		public void callDecode(NioSocketChannel channel, ChannelBuffer cumulation, SocketAddress remoteAddress) throws Exception {
		while (cumulation.readableBytes() > 0) {
			Log.d("RtmpDecoder","callDecode");
		    int oldReaderIndex = checkpoint = cumulation.readerIndex();
		    Object result = null;
		    DecoderState oldState = state;
		        result = decode(channel, replayable, state);//ここで貰ったRTMPのメッセージをClientHandlerに流す
		        if (result == null) {
		            if (oldReaderIndex == cumulation.readerIndex() && oldState == state) {
		                throw new IllegalStateException(
		                        "null cannot be returned if no data is consumed and state didn't change.");
		            } else {
		                // Previous data has been discarded or caused state transition.
		                // Probably it is reading on.
		                continue;
		            }
		        }


		    if (oldReaderIndex == cumulation.readerIndex() && oldState == state) {
		        throw new IllegalStateException(
		                "decode() method must consume at least one byte " +
		                "if it returned a decoded message (caused by: " +
		                getClass() + ")");
		    }

		    // A successful decode
		    handler.messageReceived(new UpstreamMessageEvent(
					channel, result, remoteAddress));

		    if (!(cumulation.readableBytes() > 0)) {
		        this.cumulation.set(null);
		        replayable = ReplayingDecoderBuffer.EMPTY_BUFFER;
		    }
			Log.d("RtmpDecoder","callDecode ------------- END");
		}
		}

		private void unfoldAndFireMessageReceived(NioSocketChannel channel, Object result, SocketAddress remoteAddress) {
		if (unfold) {
		    if (result instanceof Object[]) {
		    	Log.d("RtmpDecoder","unfoldAndFireMessageReceived 0");
		        for (Object r: (Object[]) result) {
		        	handler.messageReceived(new UpstreamMessageEvent(
		        			channel, r, remoteAddress));
		        }
		    } else if (result instanceof Iterable<?>) {
		    	Log.d("RtmpDecoder","unfoldAndFireMessageReceived 1");
		        for (Object r: (Iterable<?>) result) {
		        	handler.messageReceived(new UpstreamMessageEvent(
		        			channel, r, remoteAddress));
		        }
		    } else {
		    	Log.d("RtmpDecoder","unfoldAndFireMessageReceived 2");
	        	handler.messageReceived(new UpstreamMessageEvent(
	        			channel, result, remoteAddress));
		    }
		} else {
	    	Log.d("RtmpDecoder","unfoldAndFireMessageReceived 3");
        	handler.messageReceived(new UpstreamMessageEvent(
        			channel, result, remoteAddress));
		}
		}

		private void cleanup(ChannelStateEvent e)
		    throws Exception {
			Log.d("RtmpDecoder","cleanup");
		try {
		    ChannelBuffer cumulation = this.cumulation.getAndSet(null);
		    if (cumulation == null) {
		        return;
		    }

		    replayable.terminate();

		    if (cumulation.readableBytes() > 0) {
		        // Make sure all data was read before notifying a closed channel.
		        callDecode(e.getChannel(), cumulation, null);
		    }

		    // Call decodeLast() finally.  Please note that decodeLast() is
		    // called even if there's nothing more to read from the buffer to
		    // notify a user that the connection was closed explicitly.
		    Object partiallyDecoded = decode( e.getChannel(), replayable, state);
		    if (partiallyDecoded != null) {
		        unfoldAndFireMessageReceived(e.getChannel(), partiallyDecoded, null);
		    }
		}finally {
        	Log.d("RtmpDecoder","finally");
		    replayable = ReplayingDecoderBuffer.EMPTY_BUFFER;
//		    ctx.getPipeline().sendUpstream(ctx,e);
		}
		}

		private ChannelBuffer cumulation(Channel ch) {
        	Log.d("RtmpDecoder","cumulation");
			ChannelBuffer buf = cumulation.get();
			if (buf == null) {
			    ChannelBufferFactory factory = ch.getConfig().getBufferFactory();
			    buf = new DynamicChannelBuffer(factory.getDefaultOrder(), 256,factory);//256メモリバッファ
			    if (cumulation.compareAndSet(null, buf)) {
			        replayable = new ReplayingDecoderBuffer(buf);
			    } else {
			        buf = cumulation.get();
			    }
			}
			return buf;
			}



		    private RtmpHeader header;
		    private int channelId;
		    private ChannelBuffer payload;
		    private int chunkSize = 128;

		    private final RtmpHeader[] incompleteHeaders = new RtmpHeader[RtmpHeader.MAX_CHANNEL_ID];
		    private final ChannelBuffer[] incompletePayloads = new ChannelBuffer[RtmpHeader.MAX_CHANNEL_ID];
		    private final RtmpHeader[] completedHeaders = new RtmpHeader[RtmpHeader.MAX_CHANNEL_ID];

		    protected Object decode(final Channel channel, final ChannelBuffer in, final DecoderState state) {
		    	Log.d("RtmpDecoder","GET PACKET --- state: " + state);
		        switch(state) {
		            case GET_HEADER://ヘッダを生成→そのままPAYLOADの処理
		                header = new RtmpHeader(in, incompleteHeaders);
		                channelId = header.getChannelId();
		                if(incompletePayloads[channelId] == null) { // new chunk stream
		                    incompleteHeaders[channelId] = header;
		                    incompletePayloads[channelId] = ChannelBuffers.buffer(ChannelBuffers.BIG_ENDIAN, header.getSize());
		                }
		                payload = incompletePayloads[channelId];
		                checkpoint(DecoderState.GET_PAYLOAD);
		            case GET_PAYLOAD://動画音声の送出
		                final byte[] bytes = new byte[Math.min(payload.writableBytes(), chunkSize)];
		                in.readBytes(bytes,0,bytes.length);
		                payload.writeBytes(bytes,0,bytes.length);
		                checkpoint(DecoderState.GET_HEADER);
		                if(payload.writableBytes() > 0) { // more chunks remain
		                	Log.d("RtmpDecoder","payload.writableBytes > 0");
		                    return null;
		                }
		                incompletePayloads[channelId] = null;
		                final RtmpHeader prevHeader = completedHeaders[channelId];
		                if (!header.isLarge()) {
		                    header.setTime(prevHeader.getTime() + header.getDeltaTime());
		                }
		                final RtmpMessage message = MessageType.decode(header, payload);
		                payload = null;
		                if(header.isChunkSize()) {
		                    final ChunkSize csMessage = (ChunkSize) message;
		                    Log.d("decoder new chunk size at RtmpDecoder: ",""+ csMessage);
		                    chunkSize = csMessage.getChunkSize();
		                }
		                completedHeaders[channelId] = header;
		                return message;
		            default:
		                throw new RuntimeException("unexpected decoder state: " + state);
		        }

		    }



		}


