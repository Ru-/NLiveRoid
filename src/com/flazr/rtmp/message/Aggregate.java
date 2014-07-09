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

import com.flazr.rtmp.RtmpHeader;
import org.jboss.netty.buffer.ChannelBuffer;

public class Aggregate extends DataMessage {

    public Aggregate(RtmpHeader header, ChannelBuffer in) {
        super(header, in);
    }

    public Aggregate(int time, ChannelBuffer in) {
        super();
        data = in;
        header.setTime(time);
        header.setSize(data.readableBytes());
    }

    @Override
	public MessageType getMessageType() {
        return MessageType.AGGREGATE;
    }


	@Override
	public RtmpHeader getHeader() {
		return super.header;
	}


}
