package com.flazr.rtmp.client;

import nliveroid.nlr.main.BCPlayer;
import nliveroid.nlr.main.LiveSettings;
import nliveroid.nlr.main.MyToast;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.util.internal.LinkedTransferQueue;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.util.Log;

import com.flazr.rtmp.RtmpHeader;
import com.flazr.rtmp.RtmpMessage;
import com.flazr.rtmp.message.MessageType;
import com.flazr.rtmp.reader.PreviewReader;
import com.flazr.rtmp.reader.RtmpReader;

public class RealTimeMic {

	private LinkedTransferQueue<short[]> pcmQueue;
	private LinkedTransferQueue<MicAtom> buf_Q;
	private LiveSettings liveSetting;
	private AudioRecord audioRecord;
	private boolean isRecording;
	int SAMPLE_RATE = 44100;
	private int record_buf_size;
	private short[] buffer;
	private byte[] outPut;
	private BCPlayer player;
	private AudioTrack audioTrack;
	private boolean tick;
	private int audio_input_frame_size;

	private int dataAmount;
	private RtmpReader reader;
	private boolean ENDFLAG = true;

	private EncodingTask encodeTask;

	private boolean inited;
	private boolean isStopedRecordLoop = true;
	private boolean isStopedEncodeLoop = true;

	private boolean start_sync = true;//同期をとるためにエンコードで待たせる

	private native int initMicNative(int record_buf_size2, int i, boolean b);
	private native int endMicNative();
	private native int encodeAudioFrame(short[] buffer2);
	private native int setVolume(float vol);

	public RealTimeMic(BCPlayer player){
		this.player = player;
		pcmQueue = new LinkedTransferQueue<short[]>();
		buf_Q = new LinkedTransferQueue();
	}
	public int init(LiveSettings liveSetting){
		Log.d("RealTimeMic","INIT ------ ");
		this.liveSetting = liveSetting;
		// バッファサイズを求める サンプルレート44100Hz  モノラル 16ビット/サンプル
		record_buf_size = AudioRecord.getMinBufferSize(SAMPLE_RATE,
		                     AudioFormat.CHANNEL_CONFIGURATION_MONO,
		                     AudioFormat.ENCODING_PCM_16BIT)*2;//2バイトだから2倍になる?
		if(record_buf_size<0){
			//エミュレータでなる
			return -1;
		}
		// レコーダの取得  サンプルレート8kHz  モノラル 16ビット/サンプル
		audioRecord =  new AudioRecord(MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                AudioFormat.CHANNEL_CONFIGURATION_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                record_buf_size);		// バッファ
buffer = new short[record_buf_size];
		Log.d("RealTimeMic","INITTED ------ " + record_buf_size);
		inited = true;
		return 0;
	}

	public boolean getStartsync(){
		return start_sync;
	}

	class EncodingTask extends AsyncTask<Void,Void,Integer>{
		@Override
		public void onCancelled(){
			super.onCancelled();
			ENDFLAG = false;
			if(encodeTask != null && encodeTask.getStatus() != AsyncTask.Status.FINISHED){
				encodeTask.cancel(true);
			}
		}
				@Override
				protected Integer doInBackground(Void... params) {
					Log.d("NLiveRoid","startMicEncode");
					while(reader == null||!liveSetting.isStreamStarted()&&ENDFLAG){
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					 try {
						initMicNative(record_buf_size,liveSetting.getMode(),liveSetting.isUseCam());
	        			audioRecord.startRecording();
					 }catch(Exception e){//マイク取得に失敗→2回このメソッドを呼ぶと失敗する
						 e.printStackTrace();
						 player.runOnUiThread(new Runnable(){
							@Override
							public void run() {
								MyToast.customToastShow(player, "マイクの初期化に失敗しました");
							}
						 });
						 if(isRecording())stopRecording(true);
						 return -1;
					 }
					int readCount = 0;
					Log.d("NLiveRoid","BufSize:" + record_buf_size);
					start_sync = false;
					//カメラ側がスタートするまで待つ
					if(liveSetting.getMode() == 0 && liveSetting.isUseCam()){
						while(ENDFLAG && !((PreviewReader)reader).isStartedPreview()){
						}
					}
					// フラグが落ちるまでループ  例外処理略
					while(ENDFLAG && isRecording) {//1秒で5回、毎回7680バイト=1サンプル1バイトって事だろう

						long startT = System.nanoTime();
						long startM = System.currentTimeMillis();

						readCount = audioRecord.read(buffer, 0, record_buf_size);
//									pcmQueue.offer(buffer);//clone必要?

//									Log.d("RealTimeMic","RECORD_NANO_TIME: " + (System.nanoTime()-startT));
									Log.d("RealTimeMic","RECORD_MILL_TIME: " + (System.currentTimeMillis()-startM));
								Log.d("RealTimeMic","readCount----------- "+readCount  + "  pcmQueueSize " + pcmQueue.size());

								int length = encodeAudioFrame(buffer);

//								Log.d("RealTimeMic","ENCODE_NANO_TIME: " + (System.nanoTime()-startT));
								Log.d("RealTimeMic","ENCODE_MILL_TIME: " + (System.currentTimeMillis()-startM));
					}
					endMicNative();
					if(!liveSetting.isUseCam())liveSetting.setEncodeStarted(false);
						return null;
				}
	}

	public void setGrobalQueue(byte[] header,byte[] data){
		Log.d("NLiveRoid","MIC -------- setGrobalQueue Called size:"+reader.getGrobalQueue().size());
		if(reader.getGrobalQueue().size() > 5){
			reader.getGrobalQueue().clear();
		}else{
			reader.getGrobalQueue().offer(new MicAtom(header,data));
		}
	}
	public void startRecording(){
			Log.d("RealTimeMic","Start_Mic_Record");
			isStopedRecordLoop = false;
			isStopedEncodeLoop = false;
			ENDFLAG = true;
			if(!inited){
				int val = init(this.liveSetting);
				if(val < 0){
					player.runOnUiThread(new Runnable(){
						@Override
						public void run() {
							MyToast.customToastShow(player, "マイクの初期化に失敗しました");
						}
					 });
					return;
				}
			}
			if(encodeTask != null && encodeTask.getStatus() != AsyncTask.Status.FINISHED){
				encodeTask.cancel(true);
			}
			// 録音開始
			encodeTask = new EncodingTask();
			encodeTask.execute();

			isRecording = true;
			isStopedEncodeLoop = true;
	}
	public void stopRecording(boolean isWait){
			Log.d("RealTimeMic","Stop Mic Record");
			// 終了処理 例外処理略
					try{
						if(encodeTask != null && encodeTask.getStatus() != AsyncTask.Status.FINISHED){
							encodeTask.cancel(true);
						}
						ENDFLAG = false;
						while(isWait && isStopedEncodeLoop&&isStopedRecordLoop){//待ちタスクを突っ込んじゃう
							Thread.sleep(1000);
						}
						pcmQueue.clear();
					if(audioRecord != null&&isRecording){
//						Log.d("RealTimeMic","stopRecording");
						audioRecord.stop();
					}
					isRecording = false;
					}catch(Exception e){
						e.printStackTrace();
						player.runOnUiThread(new Runnable(){
							@Override
							public void run(){
								 MyToast.customToastShow(player, "マイクの終了に失敗しました");
							}
						});
					}
		}
	public void releaseAudioRecord(){
		if(audioRecord != null)audioRecord.release();
	}

	public void playShortPCM(short[] audioData){
						// 出力
						// サンプルレート 8kHz
						// オーディオトラック取得
		if(audioTrack == null){
					audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
						        SAMPLE_RATE,
						        AudioFormat.CHANNEL_CONFIGURATION_MONO,
						        AudioFormat.ENCODING_PCM_16BIT,
						        AudioTrack.getMinBufferSize(SAMPLE_RATE,
						                                    AudioFormat.CHANNEL_CONFIGURATION_STEREO,
						                                    AudioFormat.ENCODING_PCM_16BIT) ,
						        AudioTrack.MODE_STREAM);
		}
						audioTrack.play();
						audioTrack.write(audioData, 0, audioData.length);
		}

	public void releaseTrack(){
		try{
		if(audioTrack != null)audioTrack.release();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	public boolean isRecording() {
		return isRecording;
	}
	public void setVolume_(float vol){
		setVolume(vol);
	}
    class MicAtom implements RtmpMessage {

        private final RtmpHeader header;
        private ChannelBuffer data;


        public MicAtom(byte[] header_, byte[] data_) {
        	header = readHeader(header_);
        	data = ChannelBuffers.wrappedBuffer(ChannelBuffers.BIG_ENDIAN,data_);
        }


        //
        public MicAtom(final byte[] av_frame) {
        	//タグタイプ1バイト、サイズ3バイト、タイムスタンプ3バイト、タイムスタンプ拡張1バイト、ストリームID3バイト
        	int size = av_frame.length;
//        	Log.d("AudioAtom","SIZE ------------ " + size);
        	byte[] headerBytes = new byte[11];
        	System.arraycopy(av_frame, 0, headerBytes, 0, 11);
        	header = readHeader(headerBytes);
//        	Log.d("MicAtom","Header "+Utils.toHex(headerBytes, 0, headerBytes.length, true));
            //ここでinはヘッダを読んだ後、4バイト(拡張タイムスタンプ+ストリームID)進んだ状態になって返ってくる
            //ここのreadでサイズ分new byte[]アロケートされている!!!!これ重いんでないか?でもしょうがないのか?
            byte[] dataAlloc = new byte[size-11];
            System.arraycopy(av_frame, 11, dataAlloc, 0,size-11);

            data = ChannelBuffers.wrappedBuffer(ChannelBuffers.BIG_ENDIAN,dataAlloc);//タグのデータを突っ込む wrapped,copied,dynamicとあるが、wrappedでいいのかわからん
//        	byte[] ba = data.copy().toByteBuffer().array();
//    		Log.d("MicAtom","DATA:"+Utils.toHex(ba,0,ba.length,true));
        	//XXXX Uncompressed, 1: ADPCM, 2: MP3, 5: Nellymoser 8kHz mono, 6: Nellymoser,10:AAC, 11: Speex
        	//XX 5.5 kHz (or speex 16kHz), 1: 11 kHz, 2: 22 kHz, 3: 44 kHz
        	//X 8-bit, 1: 16-bit
			//X mono, 1: stereo
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


	public boolean isInited() {
		return inited;
	}
	public boolean isStoped() {
		return isStopedEncodeLoop&&isStopedRecordLoop;
	}
	public void setReader(RtmpReader reader_) {
		this.reader = reader_;
	}

}
