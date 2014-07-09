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

package com.flazr.rtmp.client;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

import android.util.Log;

import com.flazr.rtmp.RtmpHeader;
import com.flazr.rtmp.RtmpMessage;
import com.flazr.rtmp.message.MessageType;
import com.flazr.rtmp.reader.FileChannelReader;

public class FlvAtom implements RtmpMessage {

    private final RtmpHeader header;
    private ChannelBuffer data;


    /**
     * FlvWriterからのみ呼ばれる
     * @param in
     */
    public FlvAtom(final ChannelBuffer in) {
        header = readHeader(in);
        data = in.readBytes(header.getSize());
        in.skipBytes(4); // prev offset
    }

    /**
     * メインで呼ばれる
     * カメラ/音声は別なので、FlvReaderのnext,prevから呼ばれる
     * @param in
     */
    public FlvAtom(final FileChannelReader in) {
    	//タグタイプ1バイト、サイズ3バイト、タイムスタンプ3バイト、タイムスタンプ拡張1バイト、ストリームID3バイト
    	ChannelBuffer head = in.wrappedReadBytes(11);
    	header = readHeader(head);
    	byte[] xa = head.array();
    	String str = "";
    	for(int i = 0; i < xa.length; i++){
    		if(i > 0 && i % 20 == 0)str += "\n";
    		str += String.format("%02X ", xa[i]);
    	}
		Log.d("NLiveRoid" ,"HEAD::::::::::::::\n"+str);
//    	header = readHeader(in.wrappedReadBytes(11));
//    	byte[] h = head.array();
//		Log.d("FlvAtom","Header "+Utils.toHex(h,0,h.length,true));
        //ここでタグ内のデータがChannelBufferとして取得される→native化はここを代替してやればいい
        //ここでinはヘッダを読んだ後、4バイト(拡張タイムスタンプ+ストリームID)進んだ状態になって返ってくる
        //ここのreadでサイズ分new byte[]アロケートされている!!!!これ重いんでないか?
        data = in.wrappedReadBytes(header.getSize());//メタデータのデータを突っ込む
    	byte[] ba = data.toByteBuffer().array();
    	str = "";
    	for(int i = 0; i < 30; i++){
    		if(i % 30 == 0)str += "\n";
    		str += String.format("%02X ", ba[i]);
    	}
		Log.d("NLiveRoid" ,"DATA::::::::::::::\n"+str);
//    	byte[] ba = data.toByteBuffer().array();
//		Log.d("FlvAtom","data:"+Utils.toHex(ba,0,ba.length,true));
    	//4バイト(1つ前のタグのサイズ→必ず00 00 00 00)進める
        in.position(in.position() + 4);
        //StaticDebug.setTime(header.getTime());
    }

    /**
     * F4vReader
     * FlvWriter
     * から呼ばれる
     * @return
     */
    public FlvAtom(final MessageType messageType, final int time, final ChannelBuffer in) {
        header = new RtmpHeader(messageType, time, in.readableBytes());
        data = in;
//    	Log.d("new FlvAtom r"," ----"+data.toString());
    }


    /**
     * ここが呼ばれる前に普通にnewされているはずなので、headerとdataは既にある想定で
     * 1つのChannelBufferの参照にヘッダ、データを入れる
     * F4vReader#next
     * FlvReader#next
     * FlvWriter#write
     * から呼ばれる
     * @return
     */
    public ChannelBuffer write() {
//    	Log.d("FlvAtom","write HeaderSize" + header.getSize());
        final ChannelBuffer out = ChannelBuffers.buffer(ChannelBuffers.BIG_ENDIAN,15 +  header.getSize());
//        final ChannelBuffer out = ChannelBuffers.buffer(ChannelBuffers.BIG_ENDIAN,header.getSize()+4);
        //ヘッダーを書き込む
        out.writeByte((byte) header.getMessageType().intValue());
        out.writeMedium(header.getSize());
        out.writeMedium(header.getTime());
        out.writeInt(0); // 4 bytes of zeros (reserved)
        //データを書き込む
        out.writeBytes(data,data.readableBytes());
        out.writeInt(header.getSize() + 11); // previous tag size(Previousとかいいながら、このタグのサイズな件)
//    	byte[] ba = out.toByteBuffer().array();
//		Log.d("FlvAtom","write()data:"+Utils.toHex(ba,0,ba.length,true));
        return out;
    }

    //FileChannelがChannelBufferとして確保されたもの
    //ヘッダーとして読み出すべき11バイトが入ってくる
    public static RtmpHeader readHeader(final ChannelBuffer in) {
    	byte type= in.readByte();
//    	byte[] ba = in.copy().array();
//		Log.d("FlvAtom","readHeader"+Utils.toHex(ba,0,ba.length,true));
        final MessageType messageType = MessageType.valueToEnum(type);
//        Log.d("FlvAtom","TYPE:"+messageType.toString()+"  VAL:"+new String(Utils.toHexChars(type)));
        //次の3バイト
        final int size = in.readMedium();
        //次の3バイト
        final int timestamp = in.readMedium();
        in.skipBytes(4); // 拡張タイムスタンプと、ストリームIDは非対応を意味する
        return new RtmpHeader(messageType, timestamp, size);
    }

    @Override
    public RtmpHeader getHeader() {
        return header;
    }


    @Override
    public ChannelBuffer encode() {
        return data;
    }


    @Override
    public void decode(final ChannelBuffer in) {
        data = in;
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(header);
        sb.append(" data: ").append(data);
        return sb.toString();
    }

	@Override
	public MessageType getMessageType() {//AbstractMessage無くす為に新たに追加
		return MessageType.VIDEO;
	}

}
