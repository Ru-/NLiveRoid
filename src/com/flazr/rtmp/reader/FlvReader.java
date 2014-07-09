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

import nliveroid.nlr.main.LiveSettings;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.buffer.DynamicChannelBuffer;
import org.jboss.netty.util.internal.LinkedTransferQueue;

import android.util.Log;

import com.flazr.rtmp.RtmpMessage;
import com.flazr.rtmp.client.FlvAtom;
import com.flazr.rtmp.message.Aggregate;
import com.flazr.rtmp.message.MessageType;
import com.flazr.rtmp.message.MetadataAmf0;

public class FlvReader implements RtmpReader {

    private FileChannelReader inputFileChannelReader;
    private long mediaStartPosition;
    private MetadataAmf0 metadata;
    private int aggregateDuration = 16384;
    private long fileLength;
    private LiveSettings liveSetting;

    public FlvReader(LiveSettings liveSetting){
    	this.liveSetting = liveSetting;
    }
    public int init(final String path)throws RuntimeException{
    	//ファイルの読み込み
    	inputFileChannelReader = new FileChannelReader();
    	int val = inputFileChannelReader.init(path);
       	if(val < 0){
	   		 return val;
	   	}
    	fileLength = inputFileChannelReader.size();
		Log.d("FlvReader","FILESIZE---" + fileLength);
        //FLVのプレフィックス(46 4C 56 01 05 00 00 00 09 00 00 00 00)
        //は必ず同じで読み取るより定数としておいてセットした方が速いので飛ばす
        inputFileChannelReader.position(13); // skip flv header
        RtmpMessage metadataAtom = null;
        try{
        	metadataAtom = next();
        }catch(Exception e){
        	e.printStackTrace();
        	return -10;//ファイルにタグが検出されない(メタデータのみとか、メタデータの時点で不正とか)
        }
        //メッセージタイプに渡されて、メタデータがデコードされる
        final RtmpMessage metadataTemp =
                MessageType.decode(metadataAtom.getHeader(), metadataAtom.encode());
        if(metadataTemp.getHeader().isMetadata()) {
            metadata = (MetadataAmf0) metadataTemp;
            mediaStartPosition = inputFileChannelReader.position();
        } else {
            Log.d("FlvReader","File doesn't start with 'onMetaData using empty one");
            metadata = new MetadataAmf0("onMetaData");
            inputFileChannelReader.position(13);
            mediaStartPosition = 13;
        }
        return 0;
    }

    @Override
    public MetadataAmf0 getMetadata() {
        return metadata;
    }

    @Override
    public RtmpMessage[] getStartMessages() {
        return new RtmpMessage[] { metadata };
    }

    public void setAggregateDuration(int targetDuration) {
        this.aggregateDuration = targetDuration;
    }

    public long getTimePosition() {
        final int time;
        if(hasNext()) {
            time = next().getHeader().getTime();
            prev();
        } else if(hasPrev()) {
            time = prev().getHeader().getTime();
            next();
        } else {
            throw new RuntimeException("not seekable");
        }
        return time;
    }
    //Seekされる時に呼ばれる
    private static boolean isSyncFrame(final RtmpMessage message) {
        final byte firstByte = message.encode().getByte(0);
        if((firstByte & 0xF0) == 0x10) {
            return true;
        }
        return false;
    }


    public long seek(final long time) {
        Log.d("trying to seek to:",""+ time);
        if(time == 0) { // special case
            try {
                inputFileChannelReader.position(mediaStartPosition);
                return 0;
            } catch(Exception e) {
                throw new RuntimeException(e);
            }
        }
        final long start = getTimePosition();
        if(time > start) {
            while(hasNext()) {
                final RtmpMessage cursor = next();
                if(cursor.getHeader().getTime() >= time) {
                    break;
                }
            }
        } else {
            while(hasPrev()) {
                final RtmpMessage cursor = prev();
                if(cursor.getHeader().getTime() <= time) {
                    next();
                    break;
                }
            }
        }
        // find the closest sync frame prior
        try {
            final long checkPoint = inputFileChannelReader.position();
            while(hasPrev()) {
                final RtmpMessage cursor = prev();
                if(cursor.getHeader().isVideo() && isSyncFrame(cursor)) {
                    Log.d("returned seek frame / position: {}", ""+cursor);
                    return cursor.getHeader().getTime();
                }
            }
            // could not find a sync frame !
            // TODO better handling, what if file is audio only
            inputFileChannelReader.position(checkPoint);
            return getTimePosition();
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean hasNext() {
    	Log.d("FlvReader","h " + inputFileChannelReader.position() + "   " + fileLength);
        return inputFileChannelReader.position() < fileLength;
    }


    protected boolean hasPrev() {
        return inputFileChannelReader.position() > mediaStartPosition;
    }

    protected RtmpMessage prev() {
        final long oldPos = inputFileChannelReader.position();
        inputFileChannelReader.position(oldPos - 4);
        final long newPos = oldPos - 4 - inputFileChannelReader.readInt();
        inputFileChannelReader.position(newPos);
        final FlvAtom flvAtom = new FlvAtom(inputFileChannelReader);
        inputFileChannelReader.position(newPos);
        return flvAtom;
    }

    private static final int AGGREGATE_SIZE_LIMIT = 65536;

    @Override
    public RtmpMessage next() {
		Log.d("FlvReader","POS " + inputFileChannelReader.position());
    	Log.d("FlvReader","next aggregateDuration:" + aggregateDuration);
        if(aggregateDuration <= 0) {
            return new FlvAtom(inputFileChannelReader);
        }
        final ChannelBuffer out = new DynamicChannelBuffer(ChannelBuffers.BIG_ENDIAN,256);
        int firstAtomTime = -1;
        while(hasNext()) {
            final FlvAtom flvAtom = new FlvAtom(inputFileChannelReader);
//            final int currentAtomTime = flvAtom.getHeader().getTime();
            final int currentAtomTime = flvAtom.getHeader().getSize();
            if(firstAtomTime == -1) {
                firstAtomTime = currentAtomTime;
            }
            final ChannelBuffer temp = flvAtom.write();
        	byte[] ba = temp.toByteBuffer().array();
//            Log.d("FlvReader","tempData:"+Utils.toHex(ba,0,ba.length+11,true));
            Log.d("FlvReader","DATABYTES o:"+out.readableBytes() + "  t:"+temp.readableBytes() + "  a:"+(out.readableBytes() + temp.readableBytes()));
            Log.d("FlvReader","BUFF"+currentAtomTime + "  f:"+firstAtomTime + " diff:"+(currentAtomTime-firstAtomTime)+"    LENGTH" + fileLength);

            if(out.readableBytes() + temp.readableBytes() > AGGREGATE_SIZE_LIMIT) {
                prev();
                break;
            }
           out.writeBytes(temp,temp.readableBytes());
            if(currentAtomTime - firstAtomTime > aggregateDuration) {
                break;
            }
        }
        Log.d("FlvReader","Return Aggregate ------------- ");
        return new Aggregate(firstAtomTime, out);
    }

    @Override
    public void close() {
    	liveSetting.setStreamStarted(false);
        inputFileChannelReader.close();
    }

	@Override
	public LinkedTransferQueue<RtmpMessage> getGrobalQueue() {
		// エンコとかしないので何もしない
		return null;
	}

	public long getDuration() {
		return inputFileChannelReader.position();
	}


}
