package com.flazr.rtmp.reader;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import nliveroid.nlr.main.BCPlayer;
import nliveroid.nlr.main.LiveSettings;
import nliveroid.nlr.main.MyToast;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.util.internal.LinkedTransferQueue;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.flazr.rtmp.RtmpHeader;
import com.flazr.rtmp.RtmpMessage;
import com.flazr.rtmp.client.RealTimeMic;
import com.flazr.rtmp.message.MessageType;
import com.flazr.rtmp.message.MetadataAmf0;
import com.flazr.util.Utils;

public class PictureReader implements RtmpReader {

    private MetadataAmf0 metadata;
    private boolean isStoped = false;

	private EncodingTask encodeTask;

	private LinkedTransferQueue<RtmpMessage> globalQueue;

	private LiveSettings liveSetting;
	private RealTimeMic mic;

	private BCPlayer player;
	private PictureReader reader;
	private byte[] encodedData;

	private boolean pictureStarted = false;
	private int[] pixelsData;

    public PictureReader(BCPlayer player,LiveSettings liveSettings) {
    	this.player = player;
    	this.liveSetting = liveSettings;
    	globalQueue = new LinkedTransferQueue<RtmpMessage>();
    }

    private native int initBmpNative(int w,int h, int isUseMic);
    private native int test();
    private native int endBmp();
	private native int repeatBmp(byte[] encodedData, int i);

	private native int encodeBmp(int[] pixcels);

	@Override
	public int init(final String path) {

		//画像が読み込まれていなければ、画像をセットする
        Log.d("PictureReader","initPictureReader " + liveSetting.getMode() +"  " + liveSetting.getBmpPath() + " " + liveSetting.getBmp());
        if(liveSetting == null)return -1;
        if(path != null){
        	Uri file_path = Uri.parse(path);
        	liveSetting.setBmpPath(file_path);
        }
        liveSetting.setUser_fps(2);
        while(liveSetting.getBmpPath() == null){//パスが設定されていなければ設定されるまで待つ
        	long startT = System.currentTimeMillis();
			Log.d("PictureReader","setSize of BMP");
			try {
				Thread.sleep(1000);
				if(System.currentTimeMillis() - startT > 60000){
					return -1;
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		//画像のサイズを取得する
		pixelsData = initBmp();
		if(liveSetting.getBmpRect() == null){
			return -1;
		}

		Rect size = liveSetting.getBmpRect();
		 Log.d("PictureReader","BMP_SIZE "+size.right + "  "+size.bottom+"  encodeData"+encodedData);

        	//MetaDataをスタートする	メタデータの解像度を合わせるためにこの時点でサイズがないといけない
        	final RtmpMessage metadataAtom = new MetaDataAtom();
            //メッセージタイプに渡されて、メタデータがデコードされる
            metadata = (MetadataAmf0) MessageType.decode(metadataAtom.getHeader(), metadataAtom.encode());


		return 0;
	}



    class MetaDataAtom implements RtmpMessage {
        private final RtmpHeader header;
        private ChannelBuffer data;
        public MetaDataAtom() {
        	MetadataAmf0 meta = MetadataAmf0.createMetaData(liveSetting);
        	//タグタイプ1バイト、サイズ3バイト、タイムスタンプ3バイト、タイムスタンプ拡張1バイト、ストリームID3バイト
            header = meta.getHeader();//ここでヘッダーは正しく返るはず
            //ここでタグ内のデータがChannelBufferとして取得される→native化はここを代替してやればいい
            //ここでinはヘッダを読んだ後、4バイト(拡張タイムスタンプ+ストリームID)進んだ状態になって返ってくる
            //ここのreadでサイズ分new byte[]アロケートされている!!!!これ重いんでないか?
            data = meta.encode();//メタデータのデータを突っ込む
        	byte[] ba = data.copy().toByteBuffer().array();
    		Log.d("PictureReader METADATA)"," "+Utils.toHex(ba,0,ba.length,true));
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
	//ここはモード判定してないけど大丈夫そう
	public int startEncode(){
		Log.d("PictureReader","startEncode");
		while(!liveSetting.isStreamStarted()){
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		if(encodeTask != null && encodeTask.getStatus() != AsyncTask.Status.FINISHED){
			encodeTask.cancel(true);
		}
		pictureStarted = true;
		encodeTask = new EncodingTask();
		encodeTask.execute();
		Log.d("PictureReader","started QueueTask");
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
        return false;
    }

    protected RtmpMessage prev() {//dataQueの1個前は取っておく?
        return null;
    }

    class EncodingTask extends AsyncTask<Void,Void,Integer>{
    	private boolean ENDFLAG = true;
		@Override
    	public void onCancelled(){
    		super.onCancelled();
    		ENDFLAG = false;
    	}
		@Override
		protected Integer doInBackground(Void... args){
			Log.d("PictureReader","QTASK " + ENDFLAG + "  " +  liveSetting.isStreamStarted());
			int returnValue = 0;
			//BmpのPathが最初から設定されているか、ギャラリーだったら設定されるのを待つ
			while(liveSetting.getBmp() == null || liveSetting.getBmpRect().width() <= 0){
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
				Rect size = liveSetting.getBmpRect();
				 Log.d("PictureReader","BMP_SIZE "+size.right + "  "+size.bottom+"  "+encodedData);


					//マイクを使用する場合同一スレッドにする為?？ここでスタートしている
					//カメラでなく静止画と同期するフラグをマイクのエンコーダにセットするにので、先にinitBmpNativeを呼ぶべき
			   		if(liveSetting.isUseMic()){
						if(mic == null){
							mic = new RealTimeMic(player);
							mic.setReader(PictureReader.this);
						}
						if(!mic.isInited()&&mic.init(liveSetting)<0){
							ENDFLAG = false;
							returnValue = -6;
						}else if(!mic.isRecording()){
							mic.startRecording();
						}
						Log.d("PictureReader","StartMicOnPictureReader --------------------- ");
					}

			   		returnValue = initBmpNative(size.right,size.bottom,liveSetting.isUseMic()? 1:0);
			   		if(returnValue<0){
			   			//-3だったらget_linesizeでこけてる
			   			ENDFLAG = false;
					 }
				   		System.gc();
						Log.d("PictureReader","encodeBmp Before ");

			   		if(encodeBmp(pixelsData) < 0){
						returnValue = -4;//普通のエンコード失敗
						}
			   		System.gc();
			   		//成功したら描画
			   		Rect rect = liveSetting.getBmpRect();
					Bitmap bmp = liveSetting.getBmp();
					Log.d("PictureReader","drawSurface " + rect.right + "   " + rect.bottom);
					player.drawCamSurface(bmp,rect.right,rect.bottom);
					Log.d("PictureReader","ENDFLAG liveSetting.isStreamStarted " + ENDFLAG + "  " + liveSetting.isStreamStarted() + " " + encodedData);
				while(ENDFLAG&&liveSetting.isStreamStarted()){
					Log.d("PictureReader","repeatBmp_loop ");

					if(encodedData != null && repeatBmp(encodedData,encodedData.length-1) < 0){
						player.drawCamSurface(null,rect.right,rect.bottom);
						returnValue = -5;//エンコード失敗(サイズがでかすぎる場合もある?)
					}
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				player.drawCamSurface(null,rect.right,rect.bottom);
				endBmp();
			pictureStarted = false;
			Log.d("PictureReader","EncodeTask");
			return returnValue;
		}
		@Override
		protected void onPostExecute(Integer arg){
			if(arg == -1){
				MyToast.customToastShow(player, "画像のサイズ取得に失敗しました");
			}else if(arg == -2){
				MyToast.customToastShow(player, "画像エンコーダの初期化に失敗しました");
			}else if(arg == -3){
				MyToast.customToastShow(player, "画像のサイズに非対応であったため、停止しましt");
			}else if(arg == -4){
				MyToast.customToastShow(player, "画像のエンコード処理に失敗しました");
			}else if(arg == -5){
				MyToast.customToastShow(player, "画像の送信に失敗しました");
			}else if(arg == -6){
				MyToast.customToastShow(player, "マイクの処理に失敗しました");
			}
		}
    }


	public void setEncodedBitmap(byte[] header_,byte[] data_){
		Log.d("NLiveRoid","setEncodedBmp ------------------ ");
		Log.d("NLiveRoid","setEncoded " +header_.length + "  " + data_.length);
		for(int i = 0; i < data_.length && i < 10; i++){
			Log.d("NLvieRoid",String.format("%02X", data_[i]));
		}
		encodedData = data_;
		globalQueue.offer(new BmpAtom(header_,data_));

	}

    @Override
    public RtmpMessage next() {
//    	Log.d("PictureReader","next"+globalQueue.size());
        		RtmpMessage tmp;
        while(true){
			try {
					tmp = globalQueue.take();
			    	Log.d("NLiveRoid","Picture next#take "+tmp);
        		if(tmp != null){
        			return tmp;
        		}
        		Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
        }
    }

    @Override
    public void close() {
    	Log.d("PictureReader","CLOSE");
    	new AsyncTask<Void,Void,Void>(){
			@Override
			protected Void doInBackground(Void... params) {
		    	if(mic!=null&&mic.isRecording())mic.stopRecording(true);//マイクループを止める
		    	if(encodeTask != null && encodeTask.getStatus() != AsyncTask.Status.FINISHED){
		    		encodeTask.cancel(true);
		    		liveSetting.setBmp(null);
		    		liveSetting.setBmpRect(null);
		    		while(pictureStarted){
		    			try {
							Thread.sleep(10);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
		    		}
		    		Log.d("PictureReader","Closed");
		    	}
				return null;
			}
    	}.execute();
    }

    class BmpAtom implements RtmpMessage {

        private final RtmpHeader header;
        private ChannelBuffer data;


        //カメラプレビューのメインで呼ばれる
        public BmpAtom(final byte[] header_,final byte[] data_) {
        	//とりあえずここでサイズに合わせてタグのヘッダーを生成しておく
        	//タグタイプ1バイト、サイズ3バイト、タイムスタンプ3バイト、タイムスタンプ拡張1バイト、ストリームID3バイト

        	Log.d("PingAtom","BmpAtom Header "+Utils.toHex(header_, 0, header_.length, true));
            header = readHeader(header_);
            //ここでタグ内のデータがChannelBufferとして取得される→native化はここを代替してやればいい
            //ここでinはヘッダを読んだ後、4バイト(拡張タイムスタンプ+ストリームID)進んだ状態になって返ってくる
            //ここのreadでサイズ分new byte[]アロケートされている!!!!これ重いんでないか?でもしょうがないのか?
            data = ChannelBuffers.wrappedBuffer(ChannelBuffers.BIG_ENDIAN,data_);//タグのデータを突っ込む wrapped,copied,dynamicとあるが、wrappedでいいのかわからん

//        	byte[] ba = data.copy().toByteBuffer().array();
//    		Log.d("PingAtom","PingAtomDATA"+Utils.toHex(ba,0,10,true));//これ表示できる!!
        }

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

	@Override
	public LinkedTransferQueue<RtmpMessage> getGrobalQueue() {
		return globalQueue;
	}


	public int[] initBmp(){
		Log.d("PictureReader","preencode");
		//此処が呼ばれる前のreader instanceof PictureReaderでreturn -1なので-2 パスが無ければギャラリーから取得

	Bitmap bitmap = null;
	int[] pixels = null;

	try {
		InputStream is = player.getContentResolver().openInputStream(liveSetting.getBmpPath());
		bitmap = BitmapFactory.decodeStream(is);
		is.close();
	} catch (FileNotFoundException e) {
		showNoFileNotif();
		e.printStackTrace();
	} catch (IOException e) {
		showNoFileNotif();
		e.printStackTrace();
	}
	Bitmap copy = null;
	try{
		copy = bitmap.copy(Bitmap.Config.ARGB_8888, false);//フォーマットを統一する
    }catch(IllegalStateException e){
    	e.printStackTrace();
    	return null;
    }catch(NullPointerException e){
    	e.printStackTrace();//ファイルが認識できませんでした
    	return null;
    }
    int w = copy.getWidth();
    int h = copy.getHeight();
    Log.d("NLiveRoid","PIXEL_SIZE w:"+w + " h:" + h);
    //YUV変換すると、色の表現が乏しくなるので、width100に縮小する
//    100:x = width:height
//    100height = x*width
//    x=100height/width
// 	 拡大比率
    Matrix matrix = new Matrix();
    float resizeWidth = w;
    float resizeHeight = ((float)(resizeWidth*h))/w;
    // 元画像
    // リサイズ画像
    Log.d("NLiveRoid","RE_SIZE H:"+resizeHeight);


	float resizeScaleWidth = (float)resizeWidth / w;
	float resizeScaleHeight = (float)resizeHeight / h;
    Log.d("NLiveRoid","PIXEL_RESIZE w:"+resizeScaleWidth + " h:" + resizeScaleHeight);
	matrix.postScale(resizeScaleWidth, resizeScaleHeight);
	Bitmap resizeBitmap = Bitmap.createBitmap(copy, 0, 0, w, h, matrix, true);
    try{
    	pixels = new int[w*h];
    }catch(OutOfMemoryError e){//画像がでかすぎる場合、ここの要素数確保でエラーする
    	e.printStackTrace();
    	return null;
    }

//    copy.getPixels(pixels, 0, w, 0, 0, w, h);
    resizeBitmap.getPixels(pixels, 0, (int)resizeWidth, 0, 0, (int)resizeWidth, (int)resizeHeight);

	Rect rect = new Rect();
//	rect.right = w;
//	rect.bottom = h;
	rect.right = (int) Math.ceil(resizeWidth);
	rect.bottom = (int) Math.ceil(resizeHeight);
	liveSetting.setBmpRect(rect);
	liveSetting.setBmp(resizeBitmap);
	Log.d("PictureReader","PreEncoded");
	return pixels;
	}


	private void showNoFileNotif(){
		player.runOnUiThread(new Runnable(){
			@Override
			public void run(){
				MyToast.customToastShow(player, "画像ファイル読み込みでエラーしました");
			}
		});
	}

	public void stopMic() {
		if(mic != null)mic.stopRecording(true);
	}

}
