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
package org.jboss.netty.channel;

import java.net.SocketAddress;

import org.jboss.netty.channel.socket.nio.NioClientSocketPipelineSink;
import org.jboss.netty.util.internal.ConversionUtil;

import android.util.Log;


public class Channels {


    public static int streamFase = 0;

    private static NioClientSocketPipelineSink sink;

    /**
     *
     * 単にnew DefaultChannelFuture(channel, cancellable)するだけ→消そう
     * Creates a new {@link ChannelFuture} for the specified {@link Channel}.
     *
     * @param cancellable {@code true} if and only if the returned future
     *                    can be canceled by {@link ChannelFuture#cancel()}
     */
    public static ChannelFuture future(Channel channel, boolean cancellable) {
        return new DefaultChannelFuture(channel, cancellable);
    }


    // event emission methods


    private static void validateInterestOps(int interestOps) {
        switch (interestOps) {
        case Channel.OP_NONE:
        case Channel.OP_READ:
        case Channel.OP_WRITE:
        case Channel.OP_READ_WRITE:
            break;
        default:
            throw new IllegalArgumentException(
                    "Invalid interestOps: " + interestOps);
        }
    }

	/**
	 * streamFaseを設定します。
	 * @param streamFase streamFase
	 */
	public static void setStreamPhase(int streamfase) {
	    streamFase = streamfase;
	}
}
