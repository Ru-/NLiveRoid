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
import com.flazr.rtmp.client.Amf0Object;
import com.flazr.rtmp.client.Amf0Value;

import java.util.ArrayList;
import java.util.List;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.buffer.DynamicChannelBuffer;

public class CommandAmf0 extends Command {

    public CommandAmf0(RtmpHeader header, ChannelBuffer in) {
        super(header, in);
    }

    public CommandAmf0(int transactionId, String name, Amf0Object object, Object ... args) {
        super(transactionId, name, object, args);
    }

    public CommandAmf0(String name, Amf0Object object, Object ... args) {
        super(name, object, args);
    }

    @Override
	public MessageType getMessageType() {
        return MessageType.COMMAND_AMF0;
    }

    @Override
    public ChannelBuffer encode() {
        ChannelBuffer out = new DynamicChannelBuffer(ChannelBuffers.BIG_ENDIAN,256);
        Amf0Value.encode(out, name, transactionId, object);
        if(args != null) {
            for(Object o : args) {
                Amf0Value.encode(out, o);
            }
        }
        return out;
    }

    @Override
    public void decode(ChannelBuffer in) {
        name = (String) Amf0Value.decode(in);
        transactionId = ((Double) Amf0Value.decode(in)).intValue();
        object = (Amf0Object) Amf0Value.decode(in);
        List<Object> list = new ArrayList<Object>();
        while(in.readableBytes() > 0) {
            list.add(Amf0Value.decode(in));
        }
        args = list.toArray();
    }

	@Override
	public RtmpHeader getHeader() {
		return super.header;
	}

}
