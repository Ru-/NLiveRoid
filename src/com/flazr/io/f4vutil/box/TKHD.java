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

package com.flazr.io.f4vutil.box;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.buffer.DynamicChannelBuffer;

import android.util.Log;

import com.flazr.io.f4vutil.Payload;
import com.flazr.util.Utils;

public class TKHD implements Payload {

    private byte version;
    private byte[] flags;
    private long creationTime;
    private long modificationTime;
    private int trackId;
    private int reserved1;
    private long duration;
    private int[] reserved2; // 2
    private short layer;
    private short alternateGroup;
    private short volume;
    private short reserved3;
    private int[] transformMatrix; // 9
    private int width;
    private int height;

    public TKHD(ChannelBuffer in) {
        read(in);
    }

    public int getTrackId() {
        return trackId;
    }

    @Override
    public void read(ChannelBuffer in) {
        version = in.readByte();
        Log.d("version: {}", ""+String.valueOf(Utils.toHexChars(version)));
        flags = new byte[3];
        in.readBytes(flags,0,3);
        if (version == 0x00) {
            creationTime = in.readInt();
            modificationTime = in.readInt();
        } else {
            creationTime = in.readLong();
            modificationTime = in.readLong();
        }
        trackId = in.readInt();
        reserved1 = in.readInt();
        if (version == 0x00) {
            duration = in.readInt();
        } else {
            duration = in.readLong();
        }
        reserved2 = new int[2];
        reserved2[0] = in.readInt();
        reserved2[1] = in.readInt();
        layer = in.readShort();
        alternateGroup = in.readShort();
        volume = in.readShort();
        reserved3 = in.readShort();
        Log.d("creationTime {} modificationTime {} trackId {} duration {} layer {} volume {}",
        		""+new Object[]{creationTime, modificationTime, trackId, duration, layer, volume});

        transformMatrix = new int[9];
        for (int i = 0; i < transformMatrix.length; i++) {
            transformMatrix[i] = in.readInt();
            Log.d("transform matrix[{}]: {}", ""+new Object[]{i, transformMatrix[i]});
        }
        width = in.readInt();
        height = in.readInt();
        Log.d("width {} height {}", ""+new Object[]{width, height});
    }

    @Override
    public ChannelBuffer write() {
        ChannelBuffer out = new DynamicChannelBuffer(ChannelBuffers.BIG_ENDIAN,256);
        out.writeByte(version);
        byte[] tmp = new byte[3];
        out.writeBytes(tmp,0,3); // flags
        if (version == 0x00) {
            out.writeInt((int) creationTime);
            out.writeInt((int) modificationTime);
        } else {
            out.writeLong(creationTime);
            out.writeLong(modificationTime);
        }
        out.writeInt(trackId);
        out.writeInt(reserved1);
        if (version == 0x00) {
            out.writeInt((int) duration);
        } else {
            out.writeLong(duration);
        }
        out.writeInt(reserved2[0]);
        out.writeInt(reserved2[1]);
        out.writeShort(layer);
        out.writeShort(alternateGroup);
        out.writeShort(volume);
        out.writeShort(reserved3);
        for (int i = 0; i < transformMatrix.length; i++) {
            out.writeInt(transformMatrix[i]);
        }
        out.writeInt(width);
        out.writeInt(height);
        return out;
    }

}
