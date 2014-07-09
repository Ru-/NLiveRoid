/*
 * Copyright 2009 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package org.jboss.netty.buffer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * A {@link ChannelBufferFactory} which merely allocates a heap buffer with
 * the specified capacity.  {@link HeapChannelBufferFactory} should perform
 * very well in most situations because it relies on the JVM garbage collector,
 * which is highly optimized for heap allocation.
 *
 */
public class HeapChannelBufferFactory implements ChannelBufferFactory {

    private static final HeapChannelBufferFactory INSTANCE_BE =
        new HeapChannelBufferFactory(ByteOrder.BIG_ENDIAN);

    private static final HeapChannelBufferFactory INSTANCE_LE =
        new HeapChannelBufferFactory(ByteOrder.LITTLE_ENDIAN);


    private static ByteOrder defaultOrder;


    public static ChannelBufferFactory getInstance(ByteOrder endianness) {
    	defaultOrder = endianness;
        if (endianness == ByteOrder.BIG_ENDIAN) {
            return INSTANCE_BE;
        } else if (endianness == ByteOrder.LITTLE_ENDIAN) {
            return INSTANCE_LE;
        } else {
            throw new IllegalStateException("Should not reach here");
        }
    }

    /**
     * Creates a new factory with the specified default {@link ByteOrder}.
     *
     * @param defaultOrder the default {@link ByteOrder} of this factory
     */
    public HeapChannelBufferFactory(ByteOrder order) {
        defaultOrder = order;
    }


    public ChannelBuffer getBuffer(ByteBuffer nioBuffer) {
        if (nioBuffer.hasArray()) {
            return ChannelBuffers.wrappedBuffer(nioBuffer);
        }

        ChannelBuffer buf = ChannelBuffers.buffer(nioBuffer.order(), nioBuffer.remaining());
        int pos = nioBuffer.position();
        buf.writeBytes(nioBuffer);
        nioBuffer.position(pos);
        return buf;
    }

	@Override
	public ChannelBuffer getBuffer(int capacity) {
		return ChannelBuffers.buffer(defaultOrder, capacity);
	}

	@Override
	public ByteOrder getDefaultOrder() {
		return defaultOrder;
	}
}
