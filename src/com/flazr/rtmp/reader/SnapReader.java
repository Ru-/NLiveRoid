package com.flazr.rtmp.reader;

import nliveroid.nlr.main.LiveSettings;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.util.internal.LinkedTransferQueue;

import android.os.AsyncTask;
import android.util.Log;

import com.flazr.rtmp.RtmpHeader;
import com.flazr.rtmp.RtmpMessage;
import com.flazr.rtmp.client.RealTimeMic;
import com.flazr.rtmp.message.MessageType;
import com.flazr.rtmp.message.MetadataAmf0;
import com.flazr.util.Utils;

public class SnapReader  implements RtmpReader {

    private MetadataAmf0 metadata;
    private boolean isStoped = false;
    private RealTimeMic mAudio;
    private long startTime;
	private QuePool qTask;
	private byte[] encodedPicture;

	private LinkedTransferQueue<RtmpMessage> globalQueue;

	private LiveSettings liveSetting;
	private RealTimeMic mic;
    //カメラプレビューとりあえず固定取得する
    public SnapReader(LiveSettings liveSettings) {
    	this.liveSetting = liveSettings;
    	globalQueue = new LinkedTransferQueue<RtmpMessage>();
    }

	@Override
	public int init(final String path) {
		//エラーチェック等
    	//FLVのプレフィックス(46 4C 56 01 05 00 00 00 09 00 00 00 00)
        //は必ず同じで読み取るより定数としておいてセットしたo方が速いので飛ばす
    	final RtmpMessage metadataAtom = new CamAtom();
        //メッセージタイプに渡されて、メタデータがデコードされる
        metadata = (MetadataAmf0) MessageType.decode(metadataAtom.getHeader(), metadataAtom.encode());

        if(liveSetting == null ||mic == null)return -1;
		return 0;
	}

	public int startQueueTask(){
		Log.d("PictureReader","startQueueTask");
		if(liveSetting.isUseMic()&&mic != null&&!mic.isRecording()){
			mic.startRecording();//エラーはRealTimeMicで一応している
		}else{
			mic.setReader(SnapReader.this);
		}
		qTask = new QuePool();
		qTask.execute();
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
    public void setPicture(byte[] encodedPicture){
    	this.encodedPicture = encodedPicture;
    }
    @Override
    public boolean hasNext() {
    	//とりあえずtrueを返しておく
        return !isStoped;
    }

    protected boolean hasPrev() {
    	//とりあえずfalseを返しておく
        return false;
    }

    protected RtmpMessage prev() {//dataQueの1個前は取っておく?
        return null;
    }

    class QuePool extends AsyncTask<Void,Void,Void>{
    	private boolean ENDFLAG = true;
    	@Override
    	public void onCancelled(){
    		super.onCancelled();
    		ENDFLAG = false;
    	}
		@Override
		protected Void doInBackground(Void... args){
			byte[] aData = null;
			return null;
		}
    }

    @Override
    public RtmpMessage next() {
    	Log.d("CamPreviewReader","next"+liveSetting.isStreamStarted());
        		RtmpMessage tmp;
        while(true){
			try {
					tmp = globalQueue.take();
        		if(tmp != null){
        			return tmp;
        		}else{
						Thread.sleep(2);
        		}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
        }
    }

    @Override
    public void close() {
    	if(qTask != null && qTask.getStatus() != AsyncTask.Status.FINISHED){
    		qTask.cancel(true);
    	}
    }


    class CamAtom implements RtmpMessage {

        private final RtmpHeader header;
        private ChannelBuffer data;


      //メタデータを作成するときに呼ぶ
        public CamAtom() {
        	MetadataAmf0 meta = MetadataAmf0.createMetaData(liveSetting);
        	//タグタイプ1バイト、サイズ3バイト、タイムスタンプ3バイト、タイムスタンプ拡張1バイト、ストリームID3バイト
            header = meta.getHeader();//ここでヘッダーは正しく返るはず
            //ここでタグ内のデータがChannelBufferとして取得される→native化はここを代替してやればいい
            //ここでinはヘッダを読んだ後、4バイト(拡張タイムスタンプ+ストリームID)進んだ状態になって返ってくる
            //ここのreadでサイズ分new byte[]アロケートされている!!!!これ重いんでないか?
            data = meta.encode();//メタデータのデータを突っ込む
        	byte[] ba = data.copy().toByteBuffer().array();
    		Log.d("PictureReader METADATA)",""+Utils.toHex(ba,0,ba.length,true));
        	//ファイル呼んでないので、スキップとか無し
        }

        //カメラプレビューのメインで呼ばれる
        public CamAtom(final byte[] av_frame) {
        	//とりあえずここでサイズに合わせてタグのヘッダーを生成しておく
        	//タグタイプ1バイト、サイズ3バイト、タイムスタンプ3バイト、タイムスタンプ拡張1バイト、ストリームID3バイト
        	int size = av_frame.length;
        	        	byte[] headerBytes = new byte[11];
        	        	System.arraycopy(av_frame, 0, headerBytes, 0, 11);
        	Log.d("PictureReader","Header "+Utils.toHex(headerBytes, 0, headerBytes.length, true));
            header = readHeader(headerBytes);
            //ここでタグ内のデータがChannelBufferとして取得される→native化はここを代替してやればいい
            //ここでinはヘッダを読んだ後、4バイト(拡張タイムスタンプ+ストリームID)進んだ状態になって返ってくる
            //ここのreadでサイズ分new byte[]アロケートされている!!!!これ重いんでないか?でもしょうがないのか?
            byte[] dataAlloc = new byte[size-11];//ヘッダー+Previousを引いたデータのみのサイズ
            System.arraycopy(av_frame, 11, dataAlloc, 0, size-11);//av_frameを11からコピーする時、サイズ-11以降はav_frame側の要素がない
            data = ChannelBuffers.wrappedBuffer(ChannelBuffers.BIG_ENDIAN,dataAlloc);//タグのデータを突っ込む wrapped,copied,dynamicとあるが、wrappedでいいのかわからん
        	byte[] ba = data.copy().toByteBuffer().array();
    		Log.d("PictureReader","CamAtomDATA"+Utils.toHex(ba,0,ba.length,true));
        }

		//カメラプレビューで呼ばれる
        public RtmpHeader readHeader(final byte[] in) {
//        	byte[] ba = in;
        	byte type= in[0];
//    		Log.d("CamPreviewAtom","readHeader"+Utils.toHex(ba,0,ba.length,true));
            final MessageType messageType = MessageType.valueToEnum(type);
//            Log.d("CamPreviewAtom","TYPE:"+messageType.toString()+"  VAL:"+new String(Utils.toHexChars(type)));
            //次の3バイト
            final int size = (in[1]   & 0xff) << 16 |
                    (in[2] & 0xff) <<  8 |
                    (in[3] & 0xff) <<  0;
            //次の3バイト
            final int timestamp =  (in[4]   & 0xff) << 16 |
                    (in[5] & 0xff) <<  8 |
                    (in[6] & 0xff) <<  0;
             // 次の拡張タイムスタンプと、ストリームIDは非対応を意味する
            return new RtmpHeader(messageType, timestamp, size);
        }
        /**
         * F4vReader#next
         * FlvReader#next
         * FlvWriter#write
         * から呼ばれる
         * @return
         */
        public ChannelBuffer write() {
            final ChannelBuffer out = ChannelBuffers.buffer(ChannelBuffers.BIG_ENDIAN,15 + header.getSize());
            out.writeByte((byte) header.getMessageType().intValue());
            out.writeMedium(header.getSize());
            out.writeMedium(header.getTime());
            out.writeInt(0); // 4 bytes of zeros (reserved)#next
            out.writeBytes(data,data.readableBytes());
            out.writeInt(header.getSize() + 11); // previous tag size
            return out;
        }

        //FileChannelがChannelBufferとして確保されたもの
        //ヘッダーとして読み出すべき11バイトが入ってくる
        public RtmpHeader readHeader(final ChannelBuffer in) {
        	byte[] ba = in.copy().array();
        	byte type= in.readByte();
            final MessageType messageType = MessageType.valueToEnum(type);
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
    		return null;
    	}

    }

    class MicAtom implements RtmpMessage {

        private final RtmpHeader header;
        private ChannelBuffer data;


      //メタデータを作成するときに呼ぶ
        public MicAtom() {
        	MetadataAmf0 meta = MetadataAmf0.createMetaData(liveSetting);
        	//タグタイプ1バイト、サイズ3バイト、タイムスタンプ3バイト、タイムスタンプ拡張1バイト、ストリームID3バイト
            header = meta.getHeader();//ここでヘッダーは正しく返るはず
            //ここでタグ内のデータがChannelBufferとして取得される→native化はここを代替してやればいい
            //ここでinはヘッダを読んだ後、4バイト(拡張タイムスタンプ+ストリームID)進んだ状態になって返ってくる
            //ここのreadでサイズ分new byte[]アロケートされている!!!!これ重いんでないか?
            data = meta.encode();//メタデータのデータを突っ込む
        	byte[] ba = data.copy().toByteBuffer().array();
    		Log.d("PictureReaderMicAtom","METADATA:"+Utils.toHex(ba,0,ba.length,true));
        	//ファイル呼んでないので、スキップとか無し
        }
        //
        public MicAtom(final byte[] av_frame) {
        	//タグタイプ1バイト、サイズ3バイト、タイムスタンプ3バイト、タイムスタンプ拡張1バイト、ストリームID3バイト
        	int size = av_frame.length;
        	Log.d("PictureReaderAudioAtom","SIZE ------------ " + size);
        	byte[] headerBytes = new byte[11];
        	System.arraycopy(av_frame, 0, headerBytes, 0, 11);
        	header = readHeader(headerBytes);
        	Log.d("PictureReaderMicAtom","Header "+Utils.toHex(headerBytes, 0, headerBytes.length, true));
            //ここでinはヘッダを読んだ後、4バイト(拡張タイムスタンプ+ストリームID)進んだ状態になって返ってくる
            //ここのreadでサイズ分new byte[]アロケートされている!!!!これ重いんでないか?でもしょうがないのか?
            byte[] dataAlloc = new byte[size-11];
            System.arraycopy(av_frame, 11, dataAlloc, 0,size-11);

            data = ChannelBuffers.wrappedBuffer(ChannelBuffers.BIG_ENDIAN,dataAlloc);//タグのデータを突っ込む wrapped,copied,dynamicとあるが、wrappedでいいのかわからん
        	byte[] ba = data.copy().toByteBuffer().array();
    		Log.d("PictureReaderMicAtom","DATA:"+Utils.toHex(ba,0,ba.length,true));
        	//0: Uncompressed, 1: ADPCM, 2: MP3, 5: Nellymoser 8kHz mono, 6: Nellymoser,10:AAC, 11: Speex
        	//0: 5.5 kHz (or speex 16kHz), 1: 11 kHz, 2: 22 kHz, 3: 44 kHz
        	//0: 8-bit, 1: 16-bit
			//0: mono, 1: stereo
        	 //StaticDebug.setTime(header.getTime());
        }
        //単にヘッダをビーン化する→将来的にはいらない
        public RtmpHeader readHeader(final byte[] in) {
        	byte type= in[0];
            final MessageType messageType = MessageType.valueToEnum(type);
            //次の3バイト
            final int size = (in[1]   & 0xff) << 16 |
                    (in[2] & 0xff) <<  8 |
                    (in[3] & 0xff) <<  0;
            //次の3バイト
            final int timestamp =  (in[4]   & 0xff) << 16 |
                    (in[5] & 0xff) <<  8 |
                    (in[6] & 0xff) <<  0;
             // 次の拡張タイムスタンプと、ストリームIDは非対応を意味する
            return new RtmpHeader(messageType, timestamp, size);
        }
        /**
         * F4vReader#next
         * FlvReader#next
         * FlvWriter#write
         * から呼ばれる
         * @return
         */
        public ChannelBuffer write() {
            final ChannelBuffer out = ChannelBuffers.buffer(ChannelBuffers.BIG_ENDIAN,15 + header.getSize());
            out.writeByte((byte) header.getMessageType().intValue());
            out.writeMedium(header.getSize());
            out.writeMedium(header.getTime());
            out.writeInt(0); // 4 bytes of zeros (reserved)#next
            out.writeBytes(data,data.readableBytes());
            out.writeInt(header.getSize() + 11); // previous tag size
            return out;
        }

        //FileChannelがChannelBufferとして確保されたもの
        //ヘッダーとして読み出すべき11バイトが入ってくる
//        public RtmpHeader readHeader(final ChannelBuffer in) {
//        	byte[] ba = in.copy().array();
//        	byte type= in.readByte();
//    		Log.d("AudioAtom","readHeader"+Utils.toHex(ba,0,ba.length,true));
//            final MessageType messageType = MessageType.valueToEnum(type);
//            Log.d("AudioAtom","TYPE:"+messageType.toString()+"  VAL:"+new String(Utils.toHexChars(type)));
//            //次の3バイト
//            final int size = in.readMedium();
//            //次の3バイト
//            final int timestamp = in.readMedium();
//            in.skipBytes(4); // 拡張タイムスタンプと、ストリームIDは非対応を意味する
//            return new RtmpHeader(messageType, timestamp, size);
//        }

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
    		return null;
    	}

    }

	@Override
	public LinkedTransferQueue<RtmpMessage> getGrobalQueue() {
		return globalQueue;
	}

	public boolean isStartedPreview() {
		// TODO 自動生成されたメソッド・スタブ
		return false;
	}

	public void stopPreview() {
		// TODO 自動生成されたメソッド・スタブ

	}


}
