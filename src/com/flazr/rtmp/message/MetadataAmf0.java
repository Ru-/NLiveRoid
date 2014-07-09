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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import nliveroid.nlr.main.LiveSettings;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.buffer.DynamicChannelBuffer;

import android.graphics.Rect;
import android.util.Log;

import com.flazr.io.f4vutil.MovieInfo;
import com.flazr.io.f4vutil.TrackInfo;
import com.flazr.io.f4vutil.box.STSD.VideoSD;
import com.flazr.rtmp.RtmpHeader;
import com.flazr.rtmp.RtmpMessage;
import com.flazr.rtmp.client.Amf0Object;
import com.flazr.rtmp.client.Amf0Value;
import com.flazr.util.Utils;

public class MetadataAmf0 implements RtmpMessage {

    protected String name;//常にonMetaData
    protected Object[] mMetaData;//メタデータのKEY=VALUEが順に入る
    protected final RtmpHeader header;

    public MetadataAmf0(String name, Object... data) {//メタデータが無かった時に生成する時に呼ばれる
    	Log.d("MetadataAmf0","Not exists MetaData");
    	 header = new RtmpHeader(getMessageType());
         this.name = name;
         this.mMetaData = data;
         header.setSize(encode().readableBytes());
    }

    public MetadataAmf0(RtmpHeader header, ChannelBuffer in) {
    	this.header = header;
    	Log.d("MetadataAmf0","Exists MetaData"+in.capacity());
    	decode(in);
    }

    @Override
	public MessageType getMessageType() {
        return MessageType.METADATA_AMF0;
    }

    @Override
    public ChannelBuffer encode() {
        ChannelBuffer out = new DynamicChannelBuffer(ChannelBuffers.BIG_ENDIAN,256);
        Amf0Value.encode(out, name);
        Amf0Value.encode(out, mMetaData);
        return out;
    }

    /**
     * ここでメタデータの名前と値を読み込む
     * Amf0Valueから、TypeTypに渡されて、型が決まり、
     * valueToEnumで値が入る
     * BigEndianHeapChannelBufferのsuper→HeapChannelBufferのChannelBufferが呼ばれる(HeapChannelBufferのsuperはAbstract...)
     */
    @Override
    public void decode(ChannelBuffer in) {
        name = (String) Amf0Value.decode(in);
        Log.d("MetadataAmf0","name:"+name);//必ずonMetaDataが入る
        List<Object> list = new ArrayList<Object>();
        Log.d("MetadataAmf0","ChannelClass :"+in.getClass().getName());
        while(in.readableBytes() > 0) {//return writerIndex - readerIndex
            list.add(Amf0Value.decode(in));
        }
        mMetaData = list.toArray();
    }

	@Override
	public RtmpHeader getHeader() {
		return header;
	}


    public Object getData(int index) {
        if(mMetaData == null || mMetaData.length < index + 1) {
            return null;
        }
        return mMetaData[index];
    }

    private Object getValue(String key) {
        final Map<String, Object> map = getMap(0);
        if(map == null) {
            return null;
        }
        return map.get(key);
    }

    public void setValue(String key, Object value) {
        if(mMetaData == null || mMetaData.length == 0) {
            mMetaData = new Object[]{new LinkedHashMap<String, Object>()};
        }
        if(mMetaData[0] == null) {
            mMetaData[0] = new LinkedHashMap<String, Object>();
        }
        final Map<String, Object> map = (Map) mMetaData[0];
        map.put(key, value);
    }

    public Map<String, Object> getMap(int index) {
        return (Map<String, Object>) getData(index);
    }

    public String getString(String key) {
        return (String) getValue(key);
    }

    public Boolean getBoolean(String key) {
        return (Boolean) getValue(key);
    }

    public Double getDouble(String key) {
        return (Double) getValue(key);
    }

    public double getDuration() {
        if(mMetaData == null || mMetaData.length == 0) {
            return -1;
        }
        final Map<String, Object> map = getMap(0);
        if(map == null) {
            return -1;
        }
        final Object o = map.get("duration");
        if(o == null) {
            return -1;
        }
        return ((Double) o).longValue();
    }

    public void setDuration(final double duration) {
        if(mMetaData == null || mMetaData.length == 0) {
            mMetaData = new Object[] {Utils.createMap(new LinkedHashMap<String, Object>(),Utils.createPair("duration", duration))};
        }
        final Object meta = mMetaData[0];
        final Map<String, Object> map = (Map) meta;
        if(map == null) {
            mMetaData[0] = Utils.createMap(new LinkedHashMap<String, Object>(),Utils.createPair("duration", duration));
            return;
        }
        map.put("duration", duration);
    }

    //==========================================================================

    public static MetadataAmf0 onPlayStatus(double duration, double bytes) {
        Map<String, Object> map = Command.onStatus(Command.OnStatus.STATUS,
                "NetStream.Play.Complete",
                Utils.createPair("duration", duration),
                Utils.createPair("bytes", bytes));
        return new MetadataAmf0("onPlayStatus", map);
    }

    public static MetadataAmf0 rtmpSampleAccess() {
        return new MetadataAmf0("|RtmpSampleAccess", false, false);
    }

    public static MetadataAmf0 dataStart() {
        return new MetadataAmf0("onStatus", Utils.createAmfObject(new Amf0Object(),Utils.createPair("code", "NetStream.Data.Start")));
    }

    //==========================================================================

    /**
    [ (map){
        duration=112.384, moovPosition=28.0, width=640.0, height=352.0, videocodecid=avc1,
        audiocodecid=mp4a, avcprofile=100.0, avclevel=30.0, aacaot=2.0, videoframerate=29.97002997002997,
        audiosamplerate=24000.0, audiochannels=2.0, trackinfo= [
            (object){length=3369366.0, timescale=30000.0, language=eng, sampledescription=[(object){sampletype=avc1}]},
            (object){length=2697216.0, timescale=24000.0, language=eng, sampledescription=[(object){sampletype=mp4a}]}
        ]}]
    */
    //ここに何か追加するとプレビューモード時のメタデータに入る
    public static MetadataAmf0 createMetaData(LiveSettings liveSetting){
    	Log.d("NLiveRoid","createMetaData Called");
    	int width = 0,height = 0;
    	if(liveSetting.getMode() == 2){
    		Rect nowSize = liveSetting.getBmpRect() == null? liveSetting.getNowActualResolution():liveSetting.getBmpRect();
    		width = nowSize.right;
    		height = nowSize.bottom;
    	}else if(liveSetting.getMode() == 0 || liveSetting.getMode() == 1){//プレビューかスナップ
    		if(liveSetting.isPortLayt()){
    		Rect nowSize = liveSetting.getNowPortlaytResolution();
    		width = nowSize.right;
    		height = nowSize.bottom;
    		}else{
    		Rect nowSize = liveSetting.getNowActualResolution();
    		width = nowSize.right;
    		height = nowSize.bottom;
    		}
    	}
    	//Metadataが欲しい時用(一時的にnullを避ける)
    	Map<String, Object> map = Utils.createMap(new LinkedHashMap<String, Object>(),
         		Utils.createPair("duration", 3600),
         		Utils.createPair("width", width),
         		Utils.createPair("height",height),
         		Utils.createPair("videocodecid", 2.0),
         		Utils.createPair("audiocodecid", 2.0),
 	            Utils.createPair("audiosamplerate", 44100.0),
 	            Utils.createPair("framerate", liveSetting.getUser_fps()),
 	            Utils.createPair("encoder","NLR")
         );
         return new MetadataAmf0("onMetaData", map);
    }
    /*
     * 		1	JPEG (currently unused)	－
			2	Sorenson H.263				FLV1
			3	Screen video				FLV3
			4	On2 VP6						FLV4
			5	On2 VP6 with alpha channel 	FLV5
			6	Screen video version 2	未対応
			7	AVC	未対応
			0 	uncompressed
			1 	ADPCM
			2 	MP3
			4 	Nellymoser @ 16 kHz モノラル
			5 	Nellymoser、8kHz モノラル
			6 	Nellymoser
			10 	AAC
			11 	Speex
     */
    public static MetadataAmf0 onMetaDataTest(MovieInfo movie) {
        Amf0Object track1 = Utils.createAmfObject(new Amf0Object(),
        		Utils.createPair("length", 3369366.0),
        		Utils.createPair("timescale", 30000.0),
        		Utils.createPair("language", "eng"),
        		Utils.createPair("sampledescription", new Amf0Object[]{Utils.createAmfObject(new Amf0Object(),Utils.createPair("sampletype", "avc1"))})
        );
        Amf0Object track2 = Utils.createAmfObject(new Amf0Object(),
        		Utils.createPair("length", 2697216.0),
        		Utils.createPair("timescale", 24000.0),
        		Utils.createPair("language", "eng"),
        		Utils.createPair("sampledescription", new Amf0Object[]{Utils.createAmfObject(new Amf0Object(),Utils.createPair("sampletype", "mp4a"))})
        );
        Map<String, Object> map = Utils.createMap(new LinkedHashMap<String, Object>(),
        		Utils.createPair("duration", movie.getDuration()),
        		Utils. createPair("moovPosition", movie.getMoovPosition()),
        		Utils.createPair("width", 640.0),
        		Utils.createPair("height", 352.0),
        		Utils.createPair("videocodecid", "avc1"),
        		Utils.createPair("audiocodecid", "mp4a"),
	            Utils.createPair("avcprofile", 100.0),
	            Utils.createPair("avclevel", 30.0),
	            Utils.createPair("aacaot", 2.0),
	            Utils.createPair("videoframerate", 29.97002997002997),
	            Utils.createPair("audiosamplerate", 24000.0),
	            Utils.createPair("audiochannels", 2.0),
	            Utils.createPair("trackinfo", new Amf0Object[]{track1, track2})
        );
        return new MetadataAmf0("onMetaData", map);
    }

    public static MetadataAmf0 onMetaData(MovieInfo movie) {
        Map<String, Object> map =Utils.createMap(new LinkedHashMap<String, Object>(),
        		Utils.createPair("duration", movie.getDuration()),
        		Utils.createPair("moovPosition", movie.getMoovPosition())
        );
        TrackInfo track1 = movie.getVideoTrack();
        Amf0Object t1 = null;
        if(track1 != null) {
            String sampleType = track1.getStsd().getSampleTypeString(1);
            t1 = Utils.createAmfObject(new Amf0Object(),
            		Utils.createPair("length", track1.getMdhd().getDuration()),
            		Utils.createPair("timescale", track1.getMdhd().getTimeScale()),
            		Utils.createPair("sampledescription", new Amf0Object[]{Utils.createAmfObject(new Amf0Object(),Utils.createPair("sampletype", sampleType))})
            );
            VideoSD video = movie.getVideoSampleDescription();
            Utils.createMap(map,
            		Utils.createPair("width", (double) video.getWidth()),
            		Utils.createPair("height", (double) video.getHeight()),
            		Utils.createPair("videocodecid", sampleType)
            );
        }
        TrackInfo track2 = movie.getAudioTrack();
        Amf0Object t2 = null;
        if(track2 != null) {
            String sampleType = track2.getStsd().getSampleTypeString(1);
            t2 = Utils.createAmfObject(new Amf0Object(),
            		Utils.createPair("length", track2.getMdhd().getDuration()),
            		Utils.createPair("timescale", track2.getMdhd().getTimeScale()),
            		Utils.createPair("sampledescription", new Amf0Object[]{Utils.createAmfObject(new Amf0Object(),Utils.createPair("sampletype", sampleType))})
            );
            Utils.createMap(map,
            		Utils.createPair("audiocodecid", sampleType)
            );
        }
        List<Amf0Object> trackList = new ArrayList<Amf0Object>();
        if(t1 != null) {
            trackList.add(t1);
        }
        if(t2 != null) {
            trackList.add(t2);
        }
        Utils.createMap(map, Utils.createPair("trackinfo", trackList.toArray()));
        return new MetadataAmf0("onMetaData", map);
    }

    //==========================================================================

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
//        sb.append(super.toString());//コメントにしたので問題がある場合がある
//        sb.append("name: ")//コメントにしたので問題がある場合がある
        sb.append(name)//常にonMetaData
        .append(":")
        .append(Arrays.toString(mMetaData));//メタデータのKEY=VALUEの全容
        return sb.toString();
    }
}
