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

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelDownstreamHandler;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.socket.nio.NioClientSocketPipelineSink;
import org.jboss.netty.channel.socket.nio.NioSocketChannel;

import android.util.Log;

import com.flazr.rtmp.message.ChunkSize;
import com.flazr.rtmp.message.Control;
import com.flazr.util.Utils;

/**
*RTMPのヘッダーやり取りをエンコードしてチャンネルに書き込む
*/
public class RtmpEncoder implements ChannelDownstreamHandler {

    private int chunkSize = 128;
    private RtmpHeader[] channelPrevHeaders = new RtmpHeader[RtmpHeader.MAX_CHANNEL_ID];
	private NioClientSocketPipelineSink sink;


    @Override
    public void handleDownstream(ChannelHandlerContext ctx, ChannelEvent e) {}


    		/**
			*前のヘッダーをクリアする
			*/
    private void clearPrevHeaders() {
        Log.d("clearing prev stream headers","");
        channelPrevHeaders = new RtmpHeader[RtmpHeader.MAX_CHANNEL_ID];
    }

			/**
			*rtmpのメッセージイベントを送信
			*/
    public void writeRequested(final NioSocketChannel ch, final MessageEvent e) throws Exception {
//    	if (e instanceof MessageEvent) {//encoder,decoder順番に来てる
        	Log.d("RtmpEncoder","0 handleDownStream MessageEvent ");
//        	try {
//    			sink.eventSunkMessageEvent(new DownstreamMessageEvent(ch, e.getFuture(), encode((RtmpMessage) e.getMessage()), null));
//    		} catch (Exception e1) {
//    			e1.printStackTrace();
//    		}
//        }
    }

    public ChannelBuffer encode(final RtmpMessage message) {
        final ChannelBuffer in = message.encode();
        //メッセージヘッダー
//        Log.d("RtmpEncoder","encode() "+in.capacity());
        final RtmpHeader header = message.getHeader();
        if(header.isChunkSize()) {
//            Log.d("RtmpEncoder","header.isChunkSize()");
            final ChunkSize csMessage = (ChunkSize) message;
//            Log.d("RtmpEncoder ", "message: "+csMessage);
            chunkSize = csMessage.getChunkSize();
        } else if(header.isControl()) {
//            Log.d("RtmpEncoder","header.isControl()");
            final Control control = (Control) message;
            if(control.getType() == Control.ControlType.STREAM_BEGIN) {
//                Log.d("RtmpEncoder","STREAM_BEGIN");
                clearPrevHeaders();
            }
        }
        final int channelId = header.getChannelId();
        header.setSize(in.readableBytes());
        final RtmpHeader prevHeader = channelPrevHeaders[channelId];
        if(prevHeader != null // first stream message is always large
                && header.getStreamId() > 0 // all control messages always large
                && header.getTime() > 0) { // if time is zero, always large
//            Log.d("RtmpEncoder","prevHeader != null");
            if(header.getSize() == prevHeader.getSize()) {
                Log.d("RtmpEncoder","SMALL");
                header.setHeaderType(RtmpHeader.HeaderType.SMALL);
            } else {
//                Log.d("RtmpEncoder","MEDIUM");
                header.setHeaderType(RtmpHeader.HeaderType.MEDIUM);
            }
            final int deltaTime = header.getTime() - prevHeader.getTime();
            if(deltaTime < 0) {
//                Log.d("RtmpEncoder","deltaTime < 0"+ header);
                header.setDeltaTime(0);
            } else {
//                Log.d("RtmpEncoder","deltaTime else");
                header.setDeltaTime(deltaTime);
            }
        } else {
//            Log.d("RtmpEncoder","LARGE");
			// otherwise force to LARGE
            header.setHeaderType(RtmpHeader.HeaderType.LARGE);
        }
        channelPrevHeaders[channelId] = header;

        final ChannelBuffer out = ChannelBuffers.buffer(ChannelBuffers.BIG_ENDIAN,
                RtmpHeader.MAX_ENCODED_SIZE + header.getSize() + header.getSize() / chunkSize);
        boolean first = true;
        while(in.readableBytes() > 0) {
//            Log.d("RtmpEncoder","readableBytes() > 0)");
            final int size = Math.min(chunkSize, in.readableBytes());
            if(first) {
//                Log.d("RtmpEncoder","first");
                header.encode(out);
                first = false;
            } else {
//                Log.d("RtmpEncoder","TINYHEADER");
            	byte[] tinyHeader = header.getTinyHeader();
                out.writeBytes(tinyHeader,0,tinyHeader.length);//追加?
            }
            in.readBytes(out, size);
        }
//        if(message.getHeader().isMedia()){
//        	Log.d("RtmpEncoder","isMedia");
//            byte[] ba = out.array();
//            Log.d("RtmpEncoder","Data:"+Utils.toHex(ba, 0, ba.length, true));
//        }
        return out;
    }

    /**
     * データ送信用(結局省略できたのは最初のChunkSizeの分岐だけ。。)
     * @param message
     * @return
     */
    public ChannelBuffer dataSendEncode(final RtmpMessage message) {
        final RtmpHeader header = message.getHeader();
        final int channelId = header.getChannelId();
        final RtmpHeader prevHeader = channelPrevHeaders[channelId];
        if(prevHeader != null
                && header.getStreamId() > 0
                && header.getTime() > 0) {
	    	 if(header.getSize() == prevHeader.getSize()) {
	             header.setHeaderType(RtmpHeader.HeaderType.SMALL);
	         } else {
	             header.setHeaderType(RtmpHeader.HeaderType.MEDIUM);
	         }
		         final int deltaTime = header.getTime() - prevHeader.getTime();
		         if(deltaTime < 0) {
		             header.setDeltaTime(0);
		         } else {
		             header.setDeltaTime(deltaTime);
		         }
        }else{
            header.setHeaderType(RtmpHeader.HeaderType.LARGE);
        }
         channelPrevHeaders[channelId] = header;

         final ChannelBuffer out = ChannelBuffers.buffer(ChannelBuffers.BIG_ENDIAN,
                 RtmpHeader.MAX_ENCODED_SIZE + header.getSize() + header.getSize() / chunkSize);
         boolean first = true;
         final ChannelBuffer in = message.encode();
         while(in.readableBytes() > 0) {
             final int size = Math.min(chunkSize, in.readableBytes());
             if(first) {
                 header.encode(out);
                 first = false;
             } else {
             	byte[] tinyHeader = header.getTinyHeader();
                 out.writeBytes(tinyHeader,0,tinyHeader.length);
             }
             in.readBytes(out, size);
         }
         return out;
    }


	public void setSink(NioClientSocketPipelineSink sink) {
		this.sink = sink;
	}

}
