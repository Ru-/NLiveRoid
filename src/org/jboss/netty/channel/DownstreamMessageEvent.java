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

import nliveroid.nlr.main.BCPlayer;

import org.jboss.netty.channel.socket.nio.NioSocketChannel;
import org.jboss.netty.util.internal.StringUtil;

/**
 *
 * 実際に送信するデータのビーン
 * The default downstream {@link MessageEvent} implementation.
 *
 * @author <a href="http://www.jboss.org/netty/">The Netty Project</a>
 * @author <a href="http://gleamynode.net/">Trustin Lee</a>
 *
 * @version $Rev: 2080 $, $Date: 2010-01-26 18:04:19 +0900 (Tue, 26 Jan 2010) $
 *
 */
public class DownstreamMessageEvent implements MessageEvent {

    private final NioSocketChannel channel;
    private final ChannelFuture future;
    private final Object message;
    private SocketAddress remoteAddress = BCPlayer.REMOTE_ADDR;

    /**
     * Creates a new instance.
     */
    public DownstreamMessageEvent(
            NioSocketChannel channel, ChannelFuture future,
            Object message, SocketAddress remoteAddress) {
        this.channel = channel;
        this.future = future;
        this.message = message;
        if (remoteAddress != null) {
            this.remoteAddress = remoteAddress;
        } else {
            this.remoteAddress = channel.getRemoteAddress();
        }
    }
    /*
     * RtmpPublisher用
     */
    public DownstreamMessageEvent(
            NioSocketChannel channel, ChannelFuture future,
            Object message) {
        this.channel = channel;
        this.future = future;
        this.message = message;
    }

    public NioSocketChannel getChannel() {
        return channel;
    }

    public ChannelFuture getFuture() {
        return future;
    }

    public Object getMessage() {
        return message;
    }

    public SocketAddress getRemoteAddress() {
        return remoteAddress;
    }

    @Override
    public String toString() {
        if (getRemoteAddress() == getChannel().getRemoteAddress()) {
            return getChannel().toString() + " DownstreamMessageEvent: " +
                   StringUtil.stripControlCharacters(getMessage());
        } else {
            return getChannel().toString() + " DownstreamMessageEvent: " +
                   StringUtil.stripControlCharacters(getMessage()) + " to " +
                   getRemoteAddress();
        }
    }
}
