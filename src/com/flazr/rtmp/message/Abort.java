package com.flazr.rtmp.message;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

import com.flazr.rtmp.RtmpHeader;
import com.flazr.rtmp.RtmpMessage;

public class Abort implements RtmpMessage {

    private int streamId;
    protected final RtmpHeader header;


    public Abort(final RtmpHeader header, final ChannelBuffer in) {
            this.header = header;
            decode(in);
    }

    public int getStreamId() {
        return streamId;
    }

    @Override
	public MessageType getMessageType() {
        return MessageType.ABORT;
    }

    @Override
    public ChannelBuffer encode() {
        final ChannelBuffer out = ChannelBuffers.buffer(ChannelBuffers.BIG_ENDIAN,4);
        out.writeInt(streamId);
        return out;
    }

    @Override
    public void decode(ChannelBuffer in) {
        streamId = in.readInt();
    }

    @Override
    public String toString() {
        return super.toString() + "streamId: " + streamId;
    }

	@Override
	public RtmpHeader getHeader() {
		return header;
	}

}
