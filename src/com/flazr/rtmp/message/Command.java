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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import nliveroid.nlr.main.LiveSettings;

import org.jboss.netty.buffer.ChannelBuffer;

import com.flazr.rtmp.RtmpHeader;
import com.flazr.rtmp.RtmpMessage;
import com.flazr.rtmp.client.Amf0Object;
import com.flazr.rtmp.client.PublishTypeSubscriber.PublishType;
import com.flazr.util.Utils;
import com.flazr.util.Utils.Pair;

public abstract class Command implements RtmpMessage {

    protected String name;
    protected int transactionId;
    protected Amf0Object object;
    protected Object[] args;
    protected final RtmpHeader header;

    public Command(RtmpHeader header, ChannelBuffer in) {
        this.header = header;
        decode(in);
    }

    public Command(int transactionId, String name, Amf0Object object, Object ... args) {
        header = new RtmpHeader(getMessageType());
        this.transactionId = transactionId;
        this.name = name;
        this.object = object;
        this.args = args;
    }

    public Command(String name, Amf0Object object, Object ... args) {
        this(0, name, object, args);
    }

    public Amf0Object getObject() {
        return object;
    }

    public Object getArg(int index) {
        return args[index];
    }

    public int getArgCount() {
        if(args == null) {
            return 0;
        }
        return args.length;
    }

    //==========================================================================

    public static enum OnStatus {

        ERROR, STATUS, WARNING;

        public static OnStatus parse(final String raw) {
            return OnStatus.valueOf(raw.substring(1).toUpperCase());
        }

        public String asString() {
            return "_" + this.name().toLowerCase();
        }

    }

    private static Amf0Object onStatus(final OnStatus level, final String code,
            final String description, final String details, final Pair ... pairs) {
        final Amf0Object object = Utils.createAmfObject(new Amf0Object(),
            Utils.createPair("level", level.asString()),
            Utils.createPair("code", code));
        if(description != null) {
            object.put("description", description);
        }
        if(details != null) {
            object.put("details", details);
        }
        return Utils.createAmfObject(object, pairs);
    }

    private static Amf0Object onStatus(final OnStatus level, final String code,
            final String description, final Pair ... pairs) {
        return onStatus(level, code, description, null, pairs);
    }

    public static Amf0Object onStatus(final OnStatus level, final String code, final Pair ... pairs) {
        return onStatus(level, code, null, null, pairs);
    }

    //==========================================================================

    public static Command connect(LiveSettings options) {
        Amf0Object object = Utils.createAmfObject(new Amf0Object(),
        		Utils.createPair("app", options.getAppName()),
        		Utils.createPair("flashVer", "WIN 9,0,124,2"),
        		Utils.createPair("tcUrl", options.getTcUrl()),
        		Utils.createPair("fpad", false),
        		Utils.createPair("audioCodecs", 1639.0),
        		Utils.createPair("videoCodecs", 252.0),
        		Utils.createPair("objectEncoding", 0.0),
        		Utils.createPair("capabilities", 15.0),
        		Utils.createPair("videoFunction", 1.0));
        if(options.getParams() != null) {
            object.putAll(options.getParams());
        }
        return new CommandAmf0("connect", object, options.getArgs());
    }

    public static Command connectSuccess(int transactionId) {
        Map<String, Object> object = onStatus(OnStatus.STATUS,
            "NetConnection.Connect.Success", "Connection succeeded.",
            Utils.createPair("fmsVer", "FMS/3,5,1,516"),
            Utils.createPair("capabilities", 31.0),
            Utils.createPair("mode", 1.0),
            Utils.createPair("objectEncoding", 0.0));
        return new CommandAmf0(transactionId, "_result", null, object);
    }

    public static Command createStream() {
        return new CommandAmf0("createStream", null);
    }

    public static Command onBWDone() {
        return new CommandAmf0("onBWDone", null);
    }

    public static Command createStreamSuccess(int transactionId, int streamId) {
        return new CommandAmf0(transactionId, "_result", null, streamId);
    }

    public static Command play(int streamId, LiveSettings options) {
        final List playArgs = new ArrayList();
        playArgs.add(options.getStreamName());
        if(options.getStart() != -2 || options.getArgs() != null) {
            playArgs.add(options.getStart());
        }
        if(options.getLength() != -1 || options.getArgs() != null) {
            playArgs.add(options.getLength());
        }
        if(options.getArgs() != null) {
            playArgs.addAll(Arrays.asList(options.getArgs()));
        }
        Command command = new CommandAmf0("play", null, playArgs.toArray());
        command.header.setChannelId(8);
        command.header.setStreamId(streamId);
        return command;
    }

    private static Command playStatus(String code, String description, String playName, String clientId, Pair ... pairs) {
        Amf0Object status = onStatus(OnStatus.STATUS,
                "NetStream.Play." + code, description + " " + playName + ".",
                Utils.createPair("details", playName),
                Utils.createPair("clientid", clientId));
        Utils.createAmfObject(status, pairs);
        Command command = new CommandAmf0("onStatus", null, status);
        command.header.setChannelId(5);
        return command;
    }

    public static Command playReset(String playName, String clientId) {
        Command command = playStatus("Reset", "Playing and resetting", playName, clientId);
        command.header.setChannelId(4); // ?
        return command;
    }

    public static Command playStart(String playName, String clientId) {
        Command play = playStatus("Start", "Started playing", playName, clientId);
        return play;
    }

    public static Command playStop(String playName, String clientId) {
        return playStatus("Stop", "Stopped playing", playName, clientId);
    }

    public static Command playFailed(String playName, String clientId) {
        Amf0Object status = onStatus(OnStatus.ERROR,
                "NetStream.Play.Failed", "Stream not found");
        Command command = new CommandAmf0("onStatus", null, status);
        command.header.setChannelId(8);
        return command;
    }

    public static Command seekNotify(int streamId, int seekTime, String playName, String clientId) {
        Amf0Object status = onStatus(OnStatus.STATUS,
                "NetStream.Seek.Notify", "Seeking " + seekTime + " (stream ID: " + streamId + ").",
                Utils.createPair("details", playName),
                Utils.createPair("clientid", clientId));
        Command command = new CommandAmf0("onStatus", null, status);
        command.header.setChannelId(5);
        command.header.setStreamId(streamId);
        command.header.setTime(seekTime);
        return command;
    }

    public static Command pauseNotify(String playName, String clientId) {
        Amf0Object status = onStatus(OnStatus.STATUS,
                "NetStream.Pause.Notify", "Pausing " + playName,
                Utils.createPair("details", playName),
                Utils.createPair("clientid", clientId));
        Command command = new CommandAmf0("onStatus", null, status);
        command.header.setChannelId(5);
        return command;
    }

    public static Command unpauseNotify(String playName, String clientId) {
        Amf0Object status = onStatus(OnStatus.STATUS,
                "NetStream.Unpause.Notify", "Unpausing " + playName,
                Utils.createPair("details", playName),
                Utils.createPair("clientid", clientId));
        Command command = new CommandAmf0("onStatus", null, status);
        command.header.setChannelId(5);
        return command;
    }

    public static Command publish(int streamId, LiveSettings options) { // TODO
        Command command = new CommandAmf0("publish", null, options.getStreamName(),
        		PublishType.LIVE.asString());
        command.header.setChannelId(8);
        command.header.setStreamId(streamId);
        return command;
    }

    private static Command publishStatus(String code, String streamName, String clientId, Pair ... pairs) {
        Amf0Object status = onStatus(OnStatus.STATUS,
                code, null, streamName,
                Utils.createPair("details", streamName),
                Utils.createPair("clientid", clientId));
        Utils.createAmfObject(status, pairs);
        Command command = new CommandAmf0("onStatus", null, status);
        command.header.setChannelId(8);
        return command;
    }

    public static Command publishStart(String streamName, String clientId, int streamId) {
        return publishStatus("NetStream.Publish.Start", streamName, clientId);
    }

    public static Command unpublishSuccess(String streamName, String clientId, int streamId) {
        return publishStatus("NetStream.Unpublish.Success", streamName, clientId);
    }

    public static Command unpublish(int streamId) {
        Command command = new CommandAmf0("publish", null, false);
        command.header.setChannelId(8);
        command.header.setStreamId(streamId);
        return command;
    }

    public static Command publishBadName(int streamId) {
        Command command = new CommandAmf0("onStatus", null,
                onStatus(OnStatus.ERROR, "NetStream.Publish.BadName", "Stream already exists."));
        command.header.setChannelId(8);
        command.header.setStreamId(streamId);
        return command;
    }

    public static Command publishNotify(int streamId) {
        Command command = new CommandAmf0("onStatus", null,
                onStatus(OnStatus.STATUS, "NetStream.Play.PublishNotify"));
        command.header.setChannelId(8);
        command.header.setStreamId(streamId);
        return command;
    }

    public static Command unpublishNotify(int streamId) {
        Command command = new CommandAmf0("onStatus", null,
                onStatus(OnStatus.STATUS, "NetStream.Play.UnpublishNotify"));
        command.header.setChannelId(8);
        command.header.setStreamId(streamId);
        return command;
    }

    public static Command closeStream(int streamId) {
        Command command = new CommandAmf0("closeStream", null);
        command.header.setChannelId(8);
        command.header.setStreamId(streamId);
        return command;
    }



    //==========================================================================

    public String getName() {
        return name;
    }

    public int getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(super.toString());
        sb.append("name: ").append(name);
        sb.append(", transactionId: ").append(transactionId);
        sb.append(", object: ").append(object);
        sb.append(", args: ").append(Arrays.toString(args));
        return sb.toString();
    }

}
