package com.flazr.rtmp.reader;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import nliveroid.nlr.main.BCPlayer;
import nliveroid.nlr.main.LiveSettings;
import nliveroid.nlr.main.MyToast;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.util.internal.LinkedTransferQueue;

import android.graphics.Rect;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.util.Log;
import android.view.SurfaceHolder;

import com.flazr.rtmp.RtmpHeader;
import com.flazr.rtmp.RtmpMessage;
import com.flazr.rtmp.client.CameraParams;
import com.flazr.rtmp.client.RealTimeMic;
import com.flazr.rtmp.message.MessageType;
import com.flazr.rtmp.message.MetadataAmf0;
import com.flazr.util.Utils;

public class PreviewReader implements RtmpReader , Camera.PreviewCallback{

    private MetadataAmf0 metadata;

	private LinkedTransferQueue<RtmpMessage> globalQueue;

	private LiveSettings liveSetting;
	private RealTimeMic mic;
	private BCPlayer player;

	private LinkedTransferQueue<byte[]> rawQueue;
	private Camera mCam;
	private EncodingLoop encodeTask;
	private boolean ENDFLAG = true;
	private boolean startedPreview;
	private SurfaceHolder.Callback callBack;
	private SurfaceHolder mHolder;
	private boolean isInited = false;
	private int offerValue = 0;

	private CameraParams parameters;


	private native int initCamNative(int width,int height, int i, int j, int k, int l);
	private native int endCamNative();
	private native void setVideoTimUnit(int timeunit);
	private native int encodeYUVArray(byte[] cameraYUV420,boolean isportlayt);
	private native int fileTest();

    //カメラプレビューとりあえず固定取得する
    public PreviewReader(BCPlayer player_,LiveSettings liveSettings) {
    	this.player = player_;
    	this.liveSetting = liveSettings;
    	globalQueue = new LinkedTransferQueue<RtmpMessage>();
		rawQueue = new LinkedTransferQueue<byte[]>();
    }

	@Override
	public int init(final String path) {//引数nullでくる


		//カメラを起動する
        try{
        	Log.d("NLiveRoid","Cam_init");
        	ENDFLAG = false;//解像度設定変更などで呼ばれる場合、エンコーダを完全に終了するべき
        	long timeout = System.currentTimeMillis();
        	if(liveSetting.isEncodeStarted()){
        		player.stopStream();
        	}
        	while(liveSetting.isEncodeStarted()){
        		try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
					break;
				}
        		if(timeout - System.currentTimeMillis() > 60000){
        			return -10;
        		}
        	}
        	if(liveSetting.isUseCam()){
        		mCam = Camera.open();
       		 if(mCam == null)return -3;
 	        parameters = new CameraParams(player,liveSetting);
 	        parameters.init(mCam);
    	        //ビューの表示に必要なパラメタをセットする
        	}
    	        //解像度設定後でないとMetaDataにサイズが設定できない
    	        //FLVの最初はどっかでセットされている
    	    	final RtmpMessage metadataAtom = new PreviewMetaData();
    	        //メッセージタイプに渡されて、メタデータがデコードされる
    	        metadata = (MetadataAmf0) MessageType.decode(metadataAtom.getHeader(), metadataAtom.encode());
    	        if(liveSetting == null)return -1;

    	        isInited = true;
        }catch(RuntimeException e){
        	e.printStackTrace();
			if(startedPreview)mCam.stopPreview();
        	return -1;
        }

		return 0;
	}


	public int startEncode(){
		 if(encodeTask != null && encodeTask.getStatus() == AsyncTask.Status.RUNNING){
	        	encodeTask.cancel(true);
	        }
	        ENDFLAG = true;
	        encodeTask = new EncodingLoop();
	        encodeTask.execute();
	        return 0;
	}

	@Override
	public void onPreviewFrame(byte[] arg, Camera arg1) {

		mCam.addCallbackBuffer(arg);//OOM対策
		if(!liveSetting.isStreamStarted())return;

		try{
		Log.d("RealTimeCam","RAWSIZE --- "  +rawQueue.size());
		if(offerValue > 0&&rawQueue.size() < 2){//エンコードが間に合わなかった時の為にここでrawQueueサイズ制限は必要
		if(arg != null)rawQueue.offer(arg);//カメラでフレームのアロケートに失敗する場合がある下ログ参照
			offerValue --;
		}
			}catch(Exception e){
			e.printStackTrace();
			MyToast.customToastShow(player, "MemoryOverflow:映像送信をストップしました");
			stopPreview();
		}
	}

	class EncodingLoop extends AsyncTask<Void,Void,Integer>{
		@Override
		protected Integer doInBackground(Void... params) {
			Log.d("RealTimeCam","StartEncodint loop");

	        liveSetting.setEncodeStarted(true);
			//エンコーダのinitとエンコードが同一のスレッド

			if(liveSetting.isUseMic()){
				if(mic == null){
					mic = new RealTimeMic(player);
					mic.setReader(PreviewReader.this);
				}
				if(!mic.isInited()&&mic.init(liveSetting)<0){
					ENDFLAG = false;
					return -6;
				}
				if(!mic.isRecording()){
					mic.startRecording();
				}
				//マイク側がスタートするまで待つ
				while(ENDFLAG && mic == null){
				}
				while(ENDFLAG && mic.getStartsync()){
				}
				Log.d("PictureReader","StartMicOnPreviewReader --------------------- ");
			}
			if(!liveSetting.isUseCam())return 0;//マイクのみだったら何もしない

			//getNowEncodeResolutionは縦の時に黒を入れることを考えたサイズ
//			final Rect nowSize = liveSetting.getNowEncodeResolution();
			final Rect nowSize = liveSetting.getNowActualResolution();
			if(nowSize == null){
				Log.d("RealTimeCam","Failed -11 loop");
				liveSetting.setUseCam(false);
				return -11;
			}
			if(initCamNative(nowSize.right,nowSize.bottom,liveSetting.getUser_fps(),liveSetting.getV_Bit_rate(),liveSetting.getKeyframe_interval(),liveSetting.isUseMic()? 1:0)<0){
				liveSetting.setUseCam(false);
				return -3;
			}
			System.gc();//ネイティブでちゃんと値がはいってない場合はGCすると落ちるのでわかる
			while(!startedPreview){
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					Log.d("RealTimeCam","Reader null Interrupt" + ENDFLAG );
					e.printStackTrace();
				}
			}
			while(ENDFLAG&&startedPreview){
				byte[] yuv = null;
				long mil = System.currentTimeMillis();
				long nano = System.nanoTime();
					try {
						yuv = rawQueue.take();//ここで突っ込まれたYUVを順に取り出してエンコーダに渡す
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				//ネイティブに渡してネイティブからsetGrobalQueueを呼んでRTMPへ
				if(encodeYUVArray(yuv,true)<0){
					endCamNative();
					return -1;
				}
				Log.d("NLiveRoid","ENCODE_MILL_TIME:" + (System.currentTimeMillis()-mil));
//				Log.d("NLiveRoid","ENCODE_NANO_TIME:" + (System.nanoTime()-nano));
			}

			endCamNative();
			Log.d("RealTimeCam","END Encoding loop");
			liveSetting.setEncodeStarted(false);
			return 0;
		}
		@Override
		protected void onPostExecute(Integer arg){
			if(arg == -1){
				MyToast.customToastShow(player, "カメラのエンコードに失敗したため停止しました");
			}else if(arg == -2){
				MyToast.customToastShow(player, "初期化に失敗していたためカメラを停止しました。端末の再起動で改善することがあります");
			}else if(arg == -3){
				MyToast.customToastShow(player, "解像度に非対応。処理を停止しました");
			}
		}

	}

	//ネイティブから呼ばれる
	public void setGrobalQueue(byte[] header_,byte[] data_,int size){
			Log.d("NLiveRoid","CAM -------- setGrobalQueue Called size:"+globalQueue.size());
			if(globalQueue.size() > 5){
				globalQueue.clear();
			}else{
				globalQueue.offer(new CamAtom(header_,data_,size));
			}
	}


	public int startPreview(){
		Log.d("NLiveRoid","RealTimeCam startPreview Called");
		if(mCam == null)return -1;
		if(startedPreview){
			mCam.stopPreview();
			startedPreview = false;
		}

		if(fpsTick == null){
        	fpsTick = new Timer();
        	fpsTick.schedule(new Tick(), 0,1000);
        }
		int value = resetPreviewDisplay();
			 if(value <0){
				 Log.d("NLiveRoid","Failed resetPreviewDisplay:"+value);
				 return -2;
			 }
			 try{
        mCam.startPreview();
			 }catch(Exception e){
				 e.printStackTrace();
				 return -3;
			 }
        startedPreview = true;
        Log.d("RealTimeCam","StartedPreviewrCam");

		return 0;
	}

	/**
	 * startedPreviewを取得します。
	 * @return startedPreview
	 */
	public boolean isStartedPreview() {
	    return startedPreview;
	}

	public CameraParams getParameters(){
		return parameters;
	}
	private Timer fpsTick;
	class Tick extends TimerTask{
		private int prevFrameCount;
		@Override
		public void run() {
			if(liveSetting.isStreamStarted()){
//					realFPS = previewCount - prevFrameCount;
//					//設定値でエンコードされるので、
//					//実際出ているFPSより設定値が高ければ同じフレームを追加、超えてる場合は減らす
//						dummyCount += (liveSetting.getUser_fps() - realFPS);
//					Log.d("ReadTimeCam","FPS " + realFPS  +"  " + dummyCount);
//				canOffer = liveSetting.getUser_fps();
				offerValue = 2;
				Log.d("ReadTimeCam","FPS canOffer  " + offerValue);
			}
		}

	}

	public boolean isInited(){
		return isInited;
	}
	//startPreviewは複数個所から連続されると非常に困るので、エンコードと分けたが、ストップは問題なかろう
	public int stopPreview() {
		try {
			ENDFLAG = false;
			if(encodeTask != null && (encodeTask.getStatus() == AsyncTask.Status.RUNNING||encodeTask.getStatus() == AsyncTask.Status.PENDING)){
				encodeTask.cancel(true);
			}
			if(fpsTick != null){
				fpsTick.cancel();
			}
			Log.d("RealTimeCam","stopPreview");
			if (this.mCam != null) {
				mCam.setPreviewDisplay(null);
				mCam.setPreviewCallback(null);
				mCam.stopPreview();
				mCam.release();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
		isInited = false;
		startedPreview = false;
		return 0;
	}

	public int resetPreviewDisplay(){
		try {
			mCam.setPreviewDisplay(null);
	        mCam.setPreviewCallback(null);
	        mCam.setPreviewCallbackWithBuffer(null);
		} catch (IOException e1) {
			e1.printStackTrace();
			if(startedPreview)mCam.stopPreview();
		return -3;
		}

		mHolder = player.getCamSurfaceHolder();
		Log.d("HOLDER"," " + mHolder);
      if(callBack == null){
        callBack = new SurfaceHolder.Callback() {
			@Override
			public void surfaceDestroyed(SurfaceHolder holder) {
			}
			@Override
			public void surfaceCreated(SurfaceHolder holder) {
			}
			@Override
			public void surfaceChanged(SurfaceHolder holder, int format, int width,
					int height) {
			}
        };
      }else{
    	  mHolder.removeCallback(callBack);
      }
        mHolder.addCallback(callBack);
			try {
		        mCam.setPreviewCallbackWithBuffer(this);
		        Rect rect = liveSetting.getNowActualResolution();
		        Log.d("NLiveRoid","ADD_BUFFER_CAM "+ rect.right + "  " + rect.bottom);
		        mCam.addCallbackBuffer(new byte[rect.right*rect.bottom*3/2]);
				mCam.setPreviewDisplay(mHolder);
			} catch (IOException e) {
				e.printStackTrace();
				if(startedPreview){
					mCam.stopPreview();
					startedPreview = false;
				}
				return -2;
			}
		return 0;
	}


	public void releaseCamera(){
		ENDFLAG = false;
		isInited = false;
		if(mCam != null)mCam.release();
		mCam = null;
		if(fpsTick != null)fpsTick.cancel();
	}

    class CamAtom implements RtmpMessage {

        private final RtmpHeader header;
        private ChannelBuffer data;

        //カメラプレビューのメインで呼ばれる
        public CamAtom(final byte[] av_frame) {
        	//とりあえずここでサイズに合わせてタグのヘッダーを生成しておく
        	//タグタイプ1バイト、サイズ3バイト、タイムスタンプ3バイト、タイムスタンプ拡張1バイト、ストリームID3バイト
        	int size = av_frame.length;
        	        	byte[] headerBytes = new byte[11];
        	        	System.arraycopy(av_frame, 0, headerBytes, 0, 11);
//        	Log.d("CamPreviewAtom","Header "+Utils.toHex(headerBytes, 0, headerBytes.length, true));
            header = readHeader(headerBytes);
            //ここでタグ内のデータがChannelBufferとして取得される→native化はここを代替してやればいい
            //ここでinはヘッダを読んだ後、4バイト(拡張タイムスタンプ+ストリームID)進んだ状態になって返ってくる
            //ここのreadでサイズ分new byte[]アロケートされている!!!!これ重いんでないか?でもしょうがないのか?
            byte[] dataAlloc = new byte[size-11];//ヘッダー+Previousを引いたデータのみのサイズ
            System.arraycopy(av_frame, 11, dataAlloc, 0, size-11);//av_frameを11からコピーする時、サイズ-11以降はav_frame側の要素がない
            data = ChannelBuffers.wrappedBuffer(ChannelBuffers.BIG_ENDIAN,dataAlloc);//タグのデータを突っ込む wrapped,copied,dynamicとあるが、wrappedでいいのかわからん
//        	byte[] ba = data.copy().toByteBuffer().array();
//    		Log.d("CamPreviewAtom","CamAtomDATA"+Utils.toHex(ba,0,ba.length,true));
        }
        public CamAtom(final byte[] header_,byte[] data_, int size) {

        	Log.d("CamPreviewAtom","CamAtom Called");
        	Log.d("CamPreviewAtom","CamAtom size " + size);
        	Log.d("CamPreviewAtom","CamAtom HEADER ");
//        	byte[] headerBytes = new byte[11];

//        	Log.d("CamPreviewAtom","Header "+Utils.toHex(headerBytes, 0, headerBytes.length, true));
            header = readHeader(header_);
        	String str = "";
//        	for(int i = 0; i < header_.length; i++){
//        		if(i > 0 && i % 20 == 0)str += "\n";
//        		str += String.format("%02X ", header_[i]);
//        	}
//    		Log.d("NLiveRoid" ,"HEAD::::::::::::::\n"+str);

            data = ChannelBuffers.wrappedBuffer(ChannelBuffers.BIG_ENDIAN,data_);//タグのデータを突っ込む wrapped,copied,dynamicとあるが、wrappedでいいのかわからん
//        	byte[] ba = data.toByteBuffer().array();
        	str = "";
//        	for(int i = 0; i < 10; i++){
//        		str += String.format("%02X ", data_[i]);
//        	}
//    		Log.d("NLiveRoid" ,"DATA::::::::::::::\n"+str);

//    		Log.d("CamPreviewAtom","CamAtomDATA"+Utils.toHex(ba,0,ba.length,true));
        }
		//カメラプレビューで呼ばれる
        public RtmpHeader readHeader(final byte[] in) {
//        	byte[] ba = in;
        	byte type= in[0];
//    		Log.d("CamPreviewAtom","readHeader"+Utils.toHex(ba,0,ba.length,true));
            final MessageType messageType = MessageType.VIDEO;
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
            final MessageType messageType = MessageType.VIDEO;
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

    class PreviewMetaData implements RtmpMessage {
        private final RtmpHeader header;
        private ChannelBuffer data;
        public PreviewMetaData() {
        	Log.d("NLiveRoid","MetaDataAtom From PreviewReader");
        	MetadataAmf0 meta = MetadataAmf0.createMetaData(liveSetting);
        	//タグタイプ1バイト、サイズ3バイト、タイムスタンプ3バイト、タイムスタンプ拡張1バイト、ストリームID3バイト
            header = meta.getHeader();//ここでヘッダーは正しく返るはず
            //ここでタグ内のデータがChannelBufferとして取得される→native化はここを代替してやればいい
            //ここでinはヘッダを読んだ後、4バイト(拡張タイムスタンプ+ストリームID)進んだ状態になって返ってくる
            //ここのreadでサイズ分new byte[]アロケートされている!!!!これ重いんでないか?
            data = meta.encode();//メタデータのデータを突っ込む
        	byte[] ba = data.copy().toByteBuffer().array();
    		Log.d("CamPreviewAtom METADATA)",""+Utils.toHex(ba,0,ba.length,true));
        	//ファイル呼んでないので、スキップとか無し
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


	public int startQueueTask(){
		Log.d("CamPreviewReader","startQueueTask");
//
//		if(liveSetting.isUseCam() && liveSetting.isUseMic()){
//				//同期をONにする
//				setSync(true);
//		}else{
//				setSync(false);
//		}
		Log.d("CamPreviewReader","startedQueueTask");
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



    @Override
    public boolean hasNext() {
    	//とりあえずtrueを返しておく
        return true;
    }


    protected boolean hasPrev() {
    	//とりあえずfalseを返しておく
        return false;
    }

    protected RtmpMessage prev() {//dataQueの1個前は取っておく?
        return null;
    }


    //QueueにあるデータがひたすらRTMPに送信される
    @Override
    public RtmpMessage next() {
    	Log.d("CamPreviewReader","next size: "+globalQueue.size());
        		RtmpMessage tmp = null;
        		long time = System.currentTimeMillis();
        while(true){
			try {
					tmp = globalQueue.take();
			    	Log.d("CamPreviewReader","next#take"+tmp);
        		if(tmp != null){
        			Log.d("PreviewReader","t" + (System.currentTimeMillis() -time));
        			return tmp;
        		}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
        }
    }

    @Override
    public void close() {
    	liveSetting.setStreamStarted(false);
    	Log.d("PreviewReader","CLOSE");
    	new AsyncTask<Void,Void,Void>(){
			@Override
			protected Void doInBackground(Void... params) {
		    	if(mic!=null&&mic.isRecording())mic.stopRecording(true);//マイクループを止める
		    	if(liveSetting.isUseMic()&&liveSetting.isUseCam()&&mCam != null && startedPreview)mCam.stopPreview();
				return null;
			}
    	}.execute();
    	if(mCam!=null&&startedPreview)mCam.stopPreview();
    }




	@Override
	public LinkedTransferQueue<RtmpMessage> getGrobalQueue() {
		return globalQueue;
	}
	public void stopMic() {
		if(mic != null)mic.stopRecording(true);
	}


}
