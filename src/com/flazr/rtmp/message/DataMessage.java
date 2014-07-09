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

package com.flazr.rtmp.message;

import java.util.LinkedHashMap;
import java.util.Map;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

import com.flazr.rtmp.RtmpHeader;
import com.flazr.rtmp.RtmpMessage;
import com.flazr.rtmp.client.Amf0Object;

public abstract class DataMessage implements RtmpMessage {

    private boolean encoded;
    protected ChannelBuffer data;

    public DataMessage() {
      header = new RtmpHeader(getMessageType());
    }
    public DataMessage(final byte[] ... bytes) {
        data = ChannelBuffers.wrappedBuffer(ChannelBuffers.BIG_ENDIAN,bytes);
        header = new RtmpHeader(getMessageType());
        header.setSize(data.readableBytes());
    }

    public DataMessage(final RtmpHeader header, final ChannelBuffer in) {
      this.header = header;
      decode(in);
    }

    public DataMessage(final int time, final ChannelBuffer in) {
        header = new RtmpHeader(getMessageType());
        header.setTime(time);
        header.setSize(in.readableBytes());
        data = in;
    }

    @Override
    public ChannelBuffer encode() {
        if(encoded) {
            // in case used multiple times e.g. broadcast
            data.resetReaderIndex();
        } else {
            encoded = true;
        }
        return data;
    }

    @Override
    public void decode(ChannelBuffer in) {
        data = in;
    }



    //AbstractMessageと統合

    protected final RtmpHeader header;

//    public AbstractMessage() {
//        header = new RtmpHeader(getMessageType());
//    }

//    public AbstractMessage(RtmpHeader header, ChannelBuffer in) {
//        this.header = header;
//        decode(in);
//    }

    @Override
    public RtmpHeader getHeader() {
        return header;
    }

    public abstract MessageType getMessageType();

    @Override
    public String toString() {
        return header.toString() + ' ';
    }


    public static Amf0Object object(Amf0Object object, Pair ... pairs) {
        if(pairs != null) {
            for(Pair pair : pairs) {
                object.put(pair.name, pair.value);
            }
        }
        return object;
    }

    public static Amf0Object object(Pair ... pairs) {
        return object(new Amf0Object(), pairs);
    }

    public static Map<String, Object> map(Map<String, Object> map, Pair ... pairs) {
        if(pairs != null) {
            for(Pair pair : pairs) {
                map.put(pair.name, pair.value);
            }
        }
        return map;
    }

    public static Map<String, Object> map(Pair ... pairs) {
        return map(new LinkedHashMap<String, Object>(), pairs);
    }

    public static class Pair {
        String name;
        Object value;
    }

    public static Pair pair(String name, Object value) {
        Pair pair = new Pair();
        pair.name = name;
        pair.value = value;
        return pair;
    }

}
