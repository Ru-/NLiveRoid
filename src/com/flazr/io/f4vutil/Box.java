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

package com.flazr.io.f4vutil;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.util.Log;

import com.flazr.rtmp.reader.FileChannelReader;

public class Box {

    private final BoxType type;
    private final long fileOffset;
    private List<Box> children;
    private Payload payload;

    public Box(final FileChannelReader in, final long endPos) {
        final long boxSize = (in.readInt()& 0xFFFFFFFFL);
        final byte[] typeBytes = in.readBytes(4);
        type = BoxType.parse(new String(typeBytes));
        final long payloadSize;
        if (boxSize == 1) { // extended
            final byte[] extBytes = in.readBytes(8);
            final BigInteger bigLen = new BigInteger(1, extBytes);
            payloadSize = bigLen.longValue() - 16;
        } else if (boxSize == 0) { // decided by parent bound
            payloadSize = endPos - in.position();
        } else {
            payloadSize = boxSize - 8;
        }
        fileOffset = in.position();
        final long childEndPos = fileOffset + payloadSize;
        Log.d(">> type: {}, payloadSize: {}",""+ type +" "+ payloadSize);
        final BoxType[] childBoxes = type.getChildren();
        if(childBoxes == null) {
            if(type == BoxType.MDAT) {
                Log.d("skipping MDAT","");
                in.position(childEndPos);
                return;
            }
            payload = type.read(in.wrappedReadBytes((int) payloadSize));
            Log.d("<< {} payload: {}",""+ type+" "+ payload);
            return;
        }
        while(in.position() < childEndPos) {
            if(children == null) {
                children = new ArrayList<Box>();
            }
            children.add(new Box(in, childEndPos));
        }
        Log.d("<< {} children: {}", ""+type+" "+ children);
    }

    public BoxType getType() {
        return type;
    }

    public long getFileOffset() {
        return fileOffset;
    }

    public List<Box> getChildren() {
        return children;
    }

    public Payload getPayload() {
        return payload;
    }

    public static void recurse(final Box box, final List<Box> collect, final int level) {
          final char[] chars = new char[level * 2];
            Arrays.fill(chars, ' ');
            Log.d("{} recursing {}, payload: {}",
                    ""+new Object[]{String.valueOf(chars), box.type, box.payload});

        if(collect != null && box.getPayload() != null) {
            collect.add(box);
        }
        if(box.getChildren() != null) {
            for(final Box child : box.getChildren()) {
                recurse(child, collect, level + 1);
            }
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append('[').append(type);
        sb.append(" fileOffset: ").append(fileOffset);
        if(children != null) {
            sb.append(" children: [");
            for(Box box : children) {
                sb.append(box.getType()).append(' ');
            }
            sb.append(']');
        }
        sb.append(" payload: ").append(payload);
        sb.append(']');
        return sb.toString();
    }

}
