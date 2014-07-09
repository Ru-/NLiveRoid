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

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

import android.util.Log;

import com.flazr.rtmp.RtmpHeader;
import com.flazr.rtmp.RtmpMessage;
import com.flazr.util.Utils;
import com.flazr.util.ValueToEnum;

public class Control implements RtmpMessage {

    public static enum ControlType implements TypeEnum {

        STREAM_BEGIN(0),
        STREAM_EOF(1),
        STREAM_DRY(2),
        SET_BUFFER(3),
        STREAM_IS_RECORDED(4),
        PING_REQUEST(6),
        PING_RESPONSE(7),
        SWFV_REQUEST(26),
        SWFV_RESPONSE(27),
        BUFFER_EMPTY(31),
        BUFFER_FULL(32);

        private final int value;

        private ControlType(int value) {
            this.value = value;
        }

        @Override
        public int intValue() {
            return value;
        }

        private static final ValueToEnum<ControlType> converter = new ValueToEnum<ControlType>(ControlType.values());

        public static ControlType valueToEnum(final int value) {
            return converter.valueToEnum(value);
        }

    }

    private ControlType type;
    private int streamId;
    private int bufferLength;
    private int time;
    private byte[] bytes;
    protected final RtmpHeader header;

    public Control(RtmpHeader header, ChannelBuffer in) {
        this.header = header;
        decode(in);
    }

    private Control(ControlType type, int time) {
        header = new RtmpHeader(getMessageType());
        this.type = type;
        this.time = time;
    }

    private Control(int streamId, ControlType type) {
        header = new RtmpHeader(getMessageType());
        this.streamId = streamId;
        this.type = type;
    }

    @Override
	public MessageType getMessageType() {
        return MessageType.CONTROL;
    }

    public static Control setBuffer(int streamId, int bufferLength) {
        Control control = new Control(ControlType.SET_BUFFER, 0);
        Log.d("Control","BUFFER SIZE at Control " + bufferLength);
        control.bufferLength = bufferLength;
        control.streamId = streamId;
        return control;
    }

    public static Control pingRequest(int time) {
        return new Control(ControlType.PING_REQUEST, time);
    }

    public static Control pingResponse(int time) {
        return new Control(ControlType.PING_RESPONSE, time);
    }

    public static Control swfvResponse(byte[] bytes) {
        Control control = new Control(ControlType.SWFV_RESPONSE, 0);
        control.bytes = bytes;
        return control;
    }

    public static Control streamBegin(int streamId) {
        Control control = new Control(ControlType.STREAM_BEGIN, 0);
        control.streamId = streamId;
        return control;
    }

    public static Control streamIsRecorded(int streamId) {
        return new Control(streamId, ControlType.STREAM_IS_RECORDED);
    }

    public static Control streamEof(int streamId) {
        return new Control(streamId, ControlType.STREAM_EOF);
    }

    public static Control bufferEmpty(int streamId) {
        return new Control(streamId, ControlType.BUFFER_EMPTY);
    }

    public static Control bufferFull(int streamId) {
        return new Control(streamId, ControlType.BUFFER_FULL);
    }

    public ControlType getType() {
        return type;
    }

    public int getTime() {
        return time;
    }

    public int getBufferLength() {
        return bufferLength;
    }

    @Override
    public ChannelBuffer encode() {
        final int size;
        switch(type) {
            case SWFV_RESPONSE:
            	Log.d("Control encode","SWFV_RESPONSE");
            	size = 44;
            break;
            case SET_BUFFER:
            	Log.d("Control encode","SET_BUFFER 10");
            	size = 10; break;
            default: size = 6;
        }
        ChannelBuffer out = ChannelBuffers.buffer(ChannelBuffers.BIG_ENDIAN,size);
        out.writeShort((short) type.value);
        switch(type) {
            case STREAM_BEGIN:
            case STREAM_EOF:
            case STREAM_DRY:
            case STREAM_IS_RECORDED:
                out.writeInt(streamId);
                break;
            case SET_BUFFER:
            	Log.d("Control encode","SET_BUFFER " +bufferLength);
                out.writeInt(streamId);
                out.writeInt(bufferLength);
                break;
            case PING_REQUEST:
            case PING_RESPONSE:
            	Log.d("Control encode","PING_RESPONSE");
                out.writeInt(time);
                break;
            case SWFV_REQUEST:
                break;
            case SWFV_RESPONSE:
            	Log.d("Control encode","SWFV_RESPONSE");
                out.writeBytes(bytes,0,bytes.length);
                break;
            case BUFFER_EMPTY:
            	Log.d("Control encode","BUFFER_EMPTY");
            case BUFFER_FULL:
            	Log.d("Control encode","BUFFER_FULL");
                out.writeInt(streamId);
                break;
        }
        return out;
    }

    @Override
    public void decode(ChannelBuffer in) {
        type = ControlType.valueToEnum(in.readShort());
        switch(type) {
            case STREAM_BEGIN:
            case STREAM_EOF:
            case STREAM_DRY:
            case STREAM_IS_RECORDED:
                streamId = in.readInt();
                break;
            case SET_BUFFER:
                streamId = in.readInt();
                bufferLength = in.readInt();
                break;
            case PING_REQUEST:
            case PING_RESPONSE:
                time = in.readInt();
                break;
            case SWFV_REQUEST:
                // only type (2 bytes)
                break;
            case SWFV_RESPONSE:
                bytes = new byte[42];
                in.readBytes(bytes,0,42);
                break;
            case BUFFER_EMPTY:
            case BUFFER_FULL:
                streamId = in.readInt();
                break;
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString());
        sb.append(type);
        sb.append(" streamId: ").append(streamId);
        switch(type) {
            case SET_BUFFER:
                sb.append(" bufferLength: ").append(bufferLength);
                break;
            case PING_REQUEST:
            case PING_RESPONSE:
                sb.append(" time: ").append(time);
                break;
        }
        if(bytes != null) {
            sb.append(" bytes: " + Utils.toHex(bytes,0,bytes.length,false));
        }
        return sb.toString();
    }

	@Override
	public RtmpHeader getHeader() {
		return header;
	}

}
