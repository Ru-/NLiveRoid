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

package com.flazr.rtmp.reader;

import java.util.List;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.buffer.DynamicChannelBuffer;
import org.jboss.netty.util.internal.LinkedTransferQueue;

import android.util.Log;

import com.flazr.io.f4vutil.MovieInfo;
import com.flazr.io.f4vutil.Sample;
import com.flazr.rtmp.RtmpHeader;
import com.flazr.rtmp.RtmpMessage;
import com.flazr.rtmp.client.FlvAtom;
import com.flazr.rtmp.message.Aggregate;
import com.flazr.rtmp.message.Audio;
import com.flazr.rtmp.message.MetadataAmf0;
import com.flazr.rtmp.message.Video;
import com.flazr.util.Utils;

public class F4vReader implements RtmpReader {

    private static final byte[] MP4A_BEGIN_PREFIX = Utils.fromHex("af00");
    private static final byte[] MP4A_PREFIX = Utils.fromHex("af01");
    private static final byte[] AVC1_BEGIN_PREFIX = Utils.fromHex("1700000000");
    private static final byte[] AVC1_PREFIX_KEYFRAME = Utils.fromHex("1701");
    private static final byte[] AVC1_PREFIX = Utils.fromHex("2701");

    private byte[] AVC1_BEGIN;
    private byte[] MP4A_BEGIN;

    private FileChannelReader in;
    private List<Sample> samples;
    private MetadataAmf0 metadata;

    private int cursor;
    private int aggregateDuration;

    @Override
    public int init(final String path) throws RuntimeException{
    	 in = new FileChannelReader();
    	 int val = in.init(path);
    	 if(val < 0){
    		 return val;
    	 }
         final MovieInfo movie = new MovieInfo(in);
         in.position(0);
         AVC1_BEGIN = movie.getVideoDecoderConfig();
         MP4A_BEGIN = movie.getAudioDecoderConfig();
         Log.d("F4vReader",""+ Utils.toHex(AVC1_BEGIN,0,AVC1_BEGIN.length,false));
         metadata = MetadataAmf0.onMetaData(movie);
         samples = movie.getSamples();
         cursor = 0;
         return 0;
    }
    @Override
    public MetadataAmf0 getMetadata() {
        return metadata;
    }

    @Override
    public RtmpMessage[] getStartMessages() {
        return new RtmpMessage[] {
            getMetadata(),
            new Video(AVC1_BEGIN_PREFIX, AVC1_BEGIN),
            new Audio(MP4A_BEGIN_PREFIX, MP4A_BEGIN)
        };
    }

    public void setAggregateDuration(int targetDuration) {
        this.aggregateDuration = targetDuration;
    }

    public long getTimePosition() {
        final int index;
        if(cursor == samples.size()) {
            index = cursor - 1;
        } else {
            index = cursor;
        }
        return samples.get(index).getTime();
    }


    public long seek(long timePosition) {
        cursor = 0;
        while(cursor < samples.size()) {
            final Sample sample = samples.get(cursor);
            if(sample.getTime() >= timePosition) {
                break;
            }
            cursor++;
        }
        while(!samples.get(cursor).isSyncSample() && cursor > 0) {
            cursor--;
        }
        return samples.get(cursor).getTime();
    }

    @Override
    public boolean hasNext() {
        return cursor < samples.size();
    }

    private static final int AGGREGATE_SIZE_LIMIT = 65536;

    @Override
    public RtmpMessage next() {
        if(aggregateDuration <= 0) {
            return getMessage(samples.get(cursor++));
        }
        final ChannelBuffer out = new DynamicChannelBuffer(ChannelBuffers.BIG_ENDIAN,256);
        int startSampleTime = -1;
        while(cursor < samples.size()) {
            final Sample sample = samples.get(cursor++);
            if(startSampleTime == -1) {
                startSampleTime = sample.getTime();
            }
            final RtmpMessage message = getMessage(sample);
            final RtmpHeader header = message.getHeader();
            final FlvAtom flvAtom = new FlvAtom(header.getMessageType(), header.getTime(), message.encode());
            final ChannelBuffer temp = flvAtom.write();
            if(out.readableBytes() + temp.readableBytes() > AGGREGATE_SIZE_LIMIT) {
                cursor--;
                break;
            }
            out.writeBytes(temp,temp.readableBytes());
            if(sample.getTime() - startSampleTime > aggregateDuration) {
                break;
            }
        }
        return new Aggregate(startSampleTime, out);
    }

    private RtmpMessage getMessage(final Sample sample) {
        in.position(sample.getFileOffset());
        final byte[] sampleBytes = in.readBytes(sample.getSize());
        final byte[] prefix;
        if(sample.isVideo()) {
            if(sample.isSyncSample()) {
                prefix = AVC1_PREFIX_KEYFRAME;
            } else {
                prefix = AVC1_PREFIX;
            }
            // TODO move prefix logic to Audio / Video
            return new Video(sample.getTime(), prefix, sample.getCompositionTimeOffset(), sampleBytes);
        } else {
            prefix = MP4A_PREFIX;
            return new Audio(sample.getTime(), prefix, sampleBytes);
        }
    }

    @Override
    public void close() {
        in.close();
    }

    public static void main(String[] args) {
        F4vReader reader = new F4vReader();
        reader.init("test2.5.mp4");
        while(reader.hasNext()) {
            Log.d("read: {}",""+ reader.next());
        }
    }
	@Override
	public LinkedTransferQueue<RtmpMessage> getGrobalQueue() {
		// エンコとかしないので何もしない
		return null;
	}

}
