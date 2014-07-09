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

import com.flazr.rtmp.RtmpHeader;
import com.flazr.rtmp.RtmpMessage;

public class SetPeerBw implements RtmpMessage {

    public static enum LimitType {
        HARD, // 0
        SOFT, // 1
        DYNAMIC // 2
    }

    private int value;
    private LimitType limitType;
    protected final RtmpHeader header;

    public SetPeerBw(RtmpHeader header, ChannelBuffer in) {
        this.header = header;
        decode(in);
    }

    public SetPeerBw(int value, LimitType limitType) {
        header = new RtmpHeader(getMessageType());
        this.value = value;
        this.limitType = limitType;
    }

    public static SetPeerBw dynamic(int value) {
        return new SetPeerBw(value, LimitType.DYNAMIC);
    }

    public static SetPeerBw hard(int value) {
        return new SetPeerBw(value, LimitType.HARD);
    }

    public int getValue() {
        return value;
    }

    @Override
	public MessageType getMessageType() {
        return MessageType.SET_PEER_BW;
    }

    @Override
    public ChannelBuffer encode() {
        ChannelBuffer out = ChannelBuffers.buffer(ChannelBuffers.BIG_ENDIAN,5);
        out.writeInt(value);
        out.writeByte((byte) limitType.ordinal());
        return out;
    }

    @Override
    public void decode(ChannelBuffer in) {
        value = in.readInt();
        limitType = LimitType.values()[in.readByte()];
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString());
        sb.append("windowSize: ").append(value);
        sb.append(" limitType: ").append(limitType);
        return sb.toString();
    }

	@Override
	public RtmpHeader getHeader() {
		return header;
	}

}
