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

import android.util.Log;

import com.flazr.rtmp.message.MessageType;
import com.flazr.rtmp.message.TypeEnum;
import com.flazr.util.Utils;
import com.flazr.util.ValueToEnum;

public class RtmpHeader {

    public static enum HeaderType implements TypeEnum {

        LARGE(0), MEDIUM(1), SMALL(2), TINY(3);

        private final int value;

        private HeaderType(int value) {
            this.value = value;
        }

        @Override
        public int intValue() {
            return value;
        }

        private static final ValueToEnum<HeaderType> converter = new ValueToEnum<HeaderType>(HeaderType.values());

        public static HeaderType valueToEnum(final int value) {
            return converter.valueToEnum(value);
        }

    }

//    public static final int MAX_CHANNEL_ID = 65600;
    public static final int MAX_CHANNEL_ID = 1024;
    public static final int MAX_NORMAL_HEADER_TIME = 0xFFFFFF;
    public static final int MAX_ENCODED_SIZE = 18;

    private HeaderType headerType;
    private int channelId;
    private int deltaTime;
    private int time;
    private int size;
    private MessageType messageType;
    private int streamId;

    //[重要]
    public RtmpHeader(ChannelBuffer in, RtmpHeader[] incompleteHeaders) {
        //=================== TYPE AND CHANNEL (1 - 3 bytes) ===================
    	//Channelから最初のバイトデータを読み込む
        final int firstByteInt = in.readByte();
        final int typeAndChannel;
        final int headerTypeInt;
        //データタイプを判定する
        if ((firstByteInt & 0x3f) == 0) {
            typeAndChannel = (firstByteInt & 0xff) << 8 | (in.readByte() & 0xff);
            channelId = 64 + (typeAndChannel & 0xff);
            headerTypeInt = typeAndChannel >> 14;
        } else if ((firstByteInt & 0x3f) == 1) {
            typeAndChannel = (firstByteInt & 0xff) << 16 | (in.readByte() & 0xff) << 8 | (in.readByte() & 0xff);
            channelId = 64 + ((typeAndChannel >> 8) & 0xff) + ((typeAndChannel & 0xff) << 8);
            headerTypeInt = typeAndChannel >> 22;
        } else {
            typeAndChannel = firstByteInt & 0xff;
            channelId = (typeAndChannel & 0x3f);
            headerTypeInt = typeAndChannel >> 6;
        }
        headerType = HeaderType.valueToEnum(headerTypeInt);

        //タイプ以外の残りのヘッダー
        final RtmpHeader prevHeader = incompleteHeaders[channelId];
        Log.d("RtmpHeader","headerType "+ headerType);
        switch(headerType) {
            case LARGE:
                time = in.readMedium();
                size = in.readMedium();
                messageType = MessageType.valueToEnum(in.readByte());
                streamId = Utils.readInt32Reverse(in);
                if(time == MAX_NORMAL_HEADER_TIME) {
                    time = in.readInt();
                }
                break;
            case MEDIUM:
                deltaTime = in.readMedium();
                size = in.readMedium();
                messageType = MessageType.valueToEnum(in.readByte());
                streamId = prevHeader.streamId;
                if(deltaTime == MAX_NORMAL_HEADER_TIME) {
                    deltaTime = in.readInt();
                }
                break;
            case SMALL:
                deltaTime = in.readMedium();
                size = prevHeader.size;
                messageType = prevHeader.messageType;
                streamId = prevHeader.streamId;
                if(deltaTime == MAX_NORMAL_HEADER_TIME) {
                    deltaTime = in.readInt();
                }
                break;
            case TINY:
                headerType = prevHeader.headerType; // preserve original
                time = prevHeader.time;
                deltaTime = prevHeader.deltaTime;
                size = prevHeader.size;
                messageType = prevHeader.messageType;
                streamId = prevHeader.streamId;
                break;
        }
    }

    public RtmpHeader(MessageType messageType, int time, int size) {
        this(messageType);
        this.time = time;
        this.size = size;
    }

    public RtmpHeader(MessageType messageType) {
        this.messageType = messageType;
        headerType = HeaderType.LARGE;
        channelId = messageType.getDefaultChannelId();
    }

    public boolean isMedia() {
        switch(messageType) {
            case AUDIO:
            case VIDEO:
            case AGGREGATE:
                return true;
            default:
                return false;
        }
    }

    public boolean isMetadata() {
        return messageType == MessageType.METADATA_AMF0
                || messageType == MessageType.METADATA_AMF3;
    }

    public boolean isAggregate() {
        return messageType == MessageType.AGGREGATE;
    }

    public boolean isAudio() {
        return messageType == MessageType.AUDIO;
    }

    public boolean isVideo() {
        return messageType == MessageType.VIDEO;
    }

    public boolean isLarge() {
        return headerType == HeaderType.LARGE;
    }

    public boolean isControl() {
        return messageType == MessageType.CONTROL;
    }

    public boolean isChunkSize() {
        return messageType == MessageType.CHUNK_SIZE;
    }

    public HeaderType getHeaderType() {
        return headerType;
    }

    public void setHeaderType(HeaderType headerType) {
        this.headerType = headerType;
    }

    public int getChannelId() {
        return channelId;
    }

    public void setChannelId(int channelId) {
        this.channelId = channelId;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public int getDeltaTime() {
        return deltaTime;
    }

    public void setDeltaTime(int deltaTime) {
        this.deltaTime = deltaTime;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getSize() {
        return size;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    public int getStreamId() {
        return streamId;
    }

    public void setStreamId(int streamId) {
        this.streamId = streamId;
    }

    public void encode(ChannelBuffer out) {
    	byte[] encHTAC = encodeHeaderTypeAndChannel(headerType.value, channelId);
        out.writeBytes(encHTAC,0,encHTAC.length);
        if(headerType == HeaderType.TINY) {
            return;
        }
        final boolean extendedTime;
        if(headerType == HeaderType.LARGE) {
            extendedTime = time >= MAX_NORMAL_HEADER_TIME;
        } else {
            extendedTime = deltaTime >= MAX_NORMAL_HEADER_TIME;
        }
        if(extendedTime) {
            out.writeMedium(MAX_NORMAL_HEADER_TIME);
        } else {                                        // LARGE / MEDIUM / SMALL
            out.writeMedium(headerType == HeaderType.LARGE ? time : deltaTime);
        }
        if(headerType != HeaderType.SMALL) {
            out.writeMedium(size);                      // LARGE / MEDIUM ここにタグタイプの次のサイズ3バイトが入る
            out.writeByte((byte) messageType.intValue());     // LARGE / MEDIUM
            if(headerType == HeaderType.LARGE) {
                Utils.writeInt32Reverse(out, streamId); // LARGE
            }
        }
        if(extendedTime) {
            out.writeInt(headerType == HeaderType.LARGE ? time : deltaTime);
        }
    }

    public byte[] getTinyHeader() {
        return encodeHeaderTypeAndChannel(HeaderType.TINY.intValue(), channelId);
    }

    private byte[] encodeHeaderTypeAndChannel(final int headerType, final int channelId) {
        if (channelId <= 63) {
            return new byte[] {(byte) ((headerType << 6) + channelId)};
        } else if (channelId <= 320) {
            return new byte[] {(byte) (headerType << 6), (byte) (channelId - 64)};
        } else {
            return new byte[] {(byte) ((headerType << 6) | 1),
                (byte) ((channelId - 64) & 0xff), (byte) ((channelId - 64) >> 8)};
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('[').append(headerType.ordinal());
        sb.append(' ').append(messageType);
        sb.append(" c").append(channelId);
        sb.append(" #").append(streamId);
        sb.append(" t").append(time);
        sb.append(" (").append(deltaTime);
        sb.append(") s").append(size);
        sb.append(']');
        return sb.toString();
    }


}
