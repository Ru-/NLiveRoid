package nliveroid.nlr.main;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.AsyncTask;
import android.util.Log;
import aqkanji2koe.AqKanji2Koe;
import aquestalk2.AquesTalk2;

public class AquesSpeech implements Speechable{
		private Activity act;
		private static AudioTrack audioTrack;
		private static AqKanji2Koe kanji2koe;
		private static AquesTalk2 aquestalk2;
		private String dic_dir;
		private int[] phontArray;
		private int phontIndex;
		private String packageName;
		private String typeName;
		private int planeSpeed;
		private int speed = 100;

	    private boolean isInited;
		private boolean ENDFLAG = true;
	    private String skip_word;
		private int maxBufferSize;
		private SpeechLoop loopThread;
		private String tempBeforeStr = "";
	    private  ArrayList<String> readBuffer = new ArrayList<String>(10);
		private float vol = 0.5F;
		byte[] writeData = new byte[80000];
		private int trackBufSize;
		private int playCount;
		final private int wavHeadPadding = 44;
		private byte[] wav;
		private boolean wait;
	    private int oneReadLength;
		private byte[] phontDat;

		//TTSSpeechの影響でContextを別セット
		public AquesSpeech(String skipword,int maxBuffer,byte volp){
			this.skip_word = skipword;
			this.maxBufferSize = maxBuffer;
	        this.vol = (float)volp/10;
			kanji2koe = new AqKanji2Koe();
	        aquestalk2 = new AquesTalk2();
	        if(NLiveRoid.apiLevel >= 11){
			trackBufSize = AudioTrack.getMinBufferSize(8000, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT);
	        audioTrack = new AudioTrack(
	        		AudioManager.STREAM_MUSIC,//ストリームタイプ
	        		8000,//サンプルレート(kHz)
	        		AudioFormat.CHANNEL_CONFIGURATION_MONO,//オーディオチャネル
	        		AudioFormat.ENCODING_PCM_16BIT,//フォーマット
	        		trackBufSize,	// 本来バッファサイズ 1秒が(8000*2)だけど、できない!!!!システムに管理されていて、それ以上のバッファ(最大値不明)を入れるとonMarkerReachedが呼ばれない(2回目は何故か呼ばれる)
	        		AudioTrack.MODE_STREAM);//STREAMモードかSTATICモードかいずれか

    		audioTrack.setPlaybackPositionUpdateListener(
	        		new AudioTrack.OnPlaybackPositionUpdateListener() {
						public void onPeriodicNotification(AudioTrack track) {
	        		    }
	        		    // 再生完了時のコールバックで足りないバッファ分ループする
	        		    public void onMarkerReached(AudioTrack track) {
	        		    	if(!ENDFLAG)return;//destroyによってこのクラスが破棄されていたら終了
		    		    		playCount ++ ;
		    		    		Log.d("Log", "onMarkerReached ------- " + track.getPlaybackHeadPosition() + " " + trackBufSize*(playCount)  + " " + oneReadLength);
		    		    		// 指定の長さの再生が終わっても、statusがPLAYINGのままなので、指定位置の再生後にコールバックを行う
		    		    		if(trackBufSize*playCount > oneReadLength){
			    		    		Log.d("NLiveRoid", "STOP ONE READ ------- ");
		    		    			 if(audioTrack.getPlayState()==AudioTrack.PLAYSTATE_PLAYING){
		    		    		    		audioTrack.stop();// 発声中なら停止
		    		    		    		audioTrack.flush();
		    		    		     }
		        		    	wait = false;
		        		    	return;
		    		    		}
		    		    		audioTrack.write(wav, trackBufSize*playCount+wavHeadPadding, trackBufSize);
		    		        	audioTrack.setNotificationMarkerPosition(trackBufSize/2);// 16bitデータなので、サンプル数は１/２。
		    		        	audioTrack.play();
	        		    }
	        		}
	        	);
	        }else{
	        trackBufSize = 8000*2*10/2;//160000でもいけるかもしれんが、何故か連続してplayできない(だからgetMinBufferSizeだと足りない)+MODE_STATICじゃないとできない→(少なくともSC-02Bとx515mでは4.0以上と方式が違っている)
	        audioTrack = new AudioTrack(
	        		AudioManager.STREAM_MUSIC,//ストリームタイプ
	        		8000,//サンプルレート(kHz)
	        		AudioFormat.CHANNEL_CONFIGURATION_MONO,//オーディオチャネル
	        		AudioFormat.ENCODING_PCM_16BIT,//フォーマット
	        		trackBufSize,	// 本来バッファサイズ 1秒が(8000*2)だけど、できない!!!!システムに管理されていて、それ以上のバッファ(最大値不明)を入れるとonMarkerReachedが呼ばれない(2回目は何故か呼ばれる)
	        		AudioTrack.MODE_STATIC);//STREAMモードかSTATICモードかいずれか
    		audioTrack.setPlaybackPositionUpdateListener(
	        		new AudioTrack.OnPlaybackPositionUpdateListener() {
						public void onPeriodicNotification(AudioTrack track) {
	        		    }
	        		    // 再生完了時のコールバックで足りないバッファ分ループする
	        		    public void onMarkerReached(AudioTrack track) {
	        		    	if(!ENDFLAG)return;//destroyによってこのクラスが破棄されていたら終了
	        		    		if(track.getPlayState()==AudioTrack.PLAYSTATE_PLAYING){
		        		    		track.stop();// 発声中なら停止
		        		    		track.flush();
		        		    	}
		        		    	wait = false;
	        		    	}
	        			}
	        	);
	        }
		}

		@Override
		public void setContext(Activity act, int paramSpeed, int phontIndex) {

		 	this.act = act;
		 	this.planeSpeed = paramSpeed;
		 	this.speed = (int) ((paramSpeed+50)+(paramSpeed*31));
		 	this.phontIndex = phontIndex;
		 	dic_dir = act.getFilesDir().toString() +"/aq_dic";// /data/data/<package name>/files
        	packageName = act.getResources().getResourcePackageName(R.raw.aq_rm);
        	typeName = act.getResources().getResourceTypeName(R.raw.aq_rm);

        	phontArray = new int[10];
	    	phontArray[0] = act.getResources().getIdentifier("aq_rm",typeName, packageName);
	    	phontArray[1] = act.getResources().getIdentifier("aq_f1c",typeName, packageName);
	    	phontArray[2] = act.getResources().getIdentifier("aq_f3a",typeName, packageName);
	    	phontArray[3] = act.getResources().getIdentifier("aq_rb2",typeName, packageName);
	    	phontArray[4] = act.getResources().getIdentifier("aq_rb3",typeName, packageName);
	    	phontArray[5] = act.getResources().getIdentifier("aq_robo",typeName, packageName);
	    	phontArray[6] = act.getResources().getIdentifier("aq_m4b",typeName, packageName);
	    	phontArray[7] = act.getResources().getIdentifier("aq_mf1",typeName, packageName);
	    	phontArray[8] = act.getResources().getIdentifier("aq_huskey",typeName, packageName);
	    	phontArray[9] = act.getResources().getIdentifier("aq_yukkuri",typeName, packageName);

   		    phontDat = loadPhont();
	        isInited = true;
	    	Log.d("NLiveRoid","AQUESTALK INIT ---------------- " );
	        //初期化前にアドされていたら読まれない
	        loopThread = new SpeechLoop();
	        loopThread.execute();
	 }

	 //native
	 private String convert(String kanji){
		if(act == null)return "";
 	    String strKoe = kanji2koe.Convert(dic_dir, kanji);
 	    return strKoe;
	 }

	 public void destroy() {
	    	Log.d("NLiveRoid","AQUESTALK DESTROY ---------------- ");
	    	if(readBuffer != null)readBuffer.clear();
	    	wait = false;
	    	ENDFLAG = false;
	    	if(loopThread != null&&loopThread.getStatus() != AsyncTask.Status.FINISHED)loopThread.cancel(true);
	    	loopThread = null;
	    	if(audioTrack != null)audioTrack.release();
	 }

	 private void play(String koe, int speed) {
		 if(audioTrack.getPlayState()==AudioTrack.PLAYSTATE_PLAYING){
	    		audioTrack.stop();// 発声中なら停止
	    		audioTrack.flush();
	     }
	    		// 音声合成
		    	wav = aquestalk2.syntheWav(koe, speed, phontDat);//スピードを設定してwavが返ってくる
//	    		 Log.d("SD","SYNCP"+readBuffer.size());
		    	if(wav.length==1){//生成エラー時には,長さ１で、先頭にエラーコードが返される
		        	Log.d("NLiveRoid","AquesTalk2SyntheERROR:"+wav[0]);
		        	Log.d("NLiveRoid","koe: "+koe);
		        	/*エラーコード一覧
		        		100 その他のエラー
		        		101 メモリ不足
		        		102 音声記号列に未定義の読み記号が指定された
		        		103 韻律データの時間長がマイナスなっている
		        		104 内部エラー(未定義の区切りコード検出）
		        		105 音声記号列に未定義の読み記号が指定された
		        		106 音声記号列のタグの指定が正しくない
		        		107 タグの長さが制限を越えている（または[>]がみつからない）
		        		108 タグ内の値の指定が正しくない
		        		109 WAVE 再生ができない（サウンドドライバ関連の問題）
		        		110 WAVE 再生ができない（サウンドドライバ関連の問題 非同期再生）
		        		111 発声すべきデータがない
		        		-38 音声記号列が長すぎる
		        		-37 １つのフレーズ中の読み記号が多すぎる
		        		-36 音声記号列が長い（内部バッファオーバー1）
		        		-35 ヒープメモリ不足
		        		-34 音声記号列が長い（内部バッファオーバー1）
		        		-16~-24 Phont データが正しくない
		        	*/
		        	try{
		        	audioTrack.stop();
		        	audioTrack.flush();
		        	readBuffer.clear();
		        	wait = false;
		        	act.runOnUiThread(new Runnable(){
						@Override
						public void run() {
							MyToast.customToastShow(act, "読み上げのコンバートでこけました\nコメント欄を保存して制作に送信下さい");
						}
		        	});
		        	}catch(Exception e){
		        		e.printStackTrace();
		        	}
		        	return;
		    	}else {	//コンバートエラーでないので再生する
		    		try{
		    		if(NLiveRoid.apiLevel >= 11){
				    		oneReadLength = wav.length-wavHeadPadding;
					    	Log.d("NLiveRoid","oneReadLength---------------- " + oneReadLength);
				    			playCount = 0;
				        	audioTrack.write(wav, wavHeadPadding, trackBufSize);//ここは無音区間を含めてAudioTrackのバッファを上書き
				        		// 指定の長さの再生が終わっても、statusがPLAYINGのままなので、指定位置の再生後にコールバックを行う
				        	audioTrack.setNotificationMarkerPosition(trackBufSize/2);// 16bitデータなので、サンプル数は１/２。
				        	audioTrack.setStereoVolume(0, vol);//モノラルだから意味ない
				        	audioTrack.play();
		    		}else{//APKLevel < 11
			    		// データサイズ[byte] 先頭の４４バイトはWAVヘッダでわからなくなったので除かない
			    		int len=wav.length-44;
			    		Log.d("NLiveRoid","AQ LOW LENGTH WHY--- " + len);
			    		//AudioTrackに指定したバッファサイズ（最大１０秒）を超えるときは切り詰める
			    		if(len>trackBufSize){
			    			len = trackBufSize;
			    		}
			    		// Tricky! audioTrackのバッファに残っているデータが最後に再生されるのを防ぐ
			    		// 波形データの後ろに無音区間を追加して、それを含めた長さをwrite()。
			    		// setNotificationMarkerPosition()では、無音区間を含まない長さを指定。
			    		// これで、setNotificationMarkerPosition()のコールバックでstop()が遅延しても、ゴミが出力されない。
	//		    		byte[] b = new byte[8000*2*10];
	//		    		Arrays.fill(writeData,0,80000,(byte)0);//データをクリアする
	//		        	//先頭の４４バイトはWAVヘッダなので除く
	//		    		System.arraycopy(wav,44,writeData,0, len);//配列にwavデータを入れる

			        	audioTrack.reloadStaticData();
			        	audioTrack.write(wav,44, len);//ここは無音区間を含めてAudioTrackのバッファを上書き
			        		// 指定の長さの再生が終わっても、statusがPLAYINGのままなので、指定位置の再生後にコールバックを行う
			        	audioTrack.setNotificationMarkerPosition(len/2);// 16bitデータなので、サンプル数は１/２。
			        	audioTrack.setStereoVolume(0, vol);//モノラルだから意味ない
			        	audioTrack.play();
		    		}
		    	}catch(Exception e){
	    			Log.d("NLiveRoid","Failed Aques ");
	    			e.printStackTrace();
	    			act.runOnUiThread(new Runnable(){
						@Override
						public void run() {
							if(ENDFLAG){
							MyToast.customToastShow(act, "読み上げがコケました:0001");
							}else{
								MyToast.customToastShow(act, "読み上げがキャンセルされました");
							}
						}
	    			});
	    		}
		    }
	 }
	// 指定のPhont名に等しいPhontデータをリソースからLoad
	    private byte[] loadPhont() {
	    	try {
		    	InputStream	in = act.getResources().openRawResource(phontArray[phontIndex]);
		    	int size = in.available();	// リソースのデータサイズ
		    	byte[] phontDat = new byte[size];
		    	in.read(phontDat);
		    	return phontDat;
	    	}catch (IOException e) {
	    		return null;
			}
	    }
	@Override
	public void addSpeech(final String str) throws InterruptedException {//これが非同期にならないみたいなのでここで非同期にする
		new AsyncTask<Void,Void,Void>(){
			@Override
			protected Void doInBackground(Void... params) {
				if(isInited&& str.length() > 0) {
					if(NLiveRoid.isDebugMode)Log.d("NLiveRoid","AQ ADD " + str);
					readBuffer.add(str.replaceAll("・|<|>", ""));
				}
				return null;
			}
		}.execute();
	}


	@Override
	public void setSpeed(int paramSpeed) {
	 	this.planeSpeed = paramSpeed;
		this.speed = (int) ((paramSpeed+50)+(paramSpeed*31));//50-300
	}


	@Override
	public void setPich(int pich) {//Pichは調整できないので代わりにphontを設定する
    	this.phontIndex = pich;
    	phontDat = loadPhont();
	}

	public void setVolume(byte param) {//Aquesのみ
    	this.vol = (float)param/10;
//    	if(audioTrack != null)audioTrack.setStereoVolume(0, vol);
	}

	@Override
	public Object[] getStatus() {
//		Log.d("NLiveRoid","AQ STATUS ---- " +planeSpeed+"   "+phontIndex);
		return new Object[]{this.planeSpeed,5,false,(int)(this.vol*10),phontIndex};
	}

    class SpeechLoop extends AsyncTask<Void,Void,Void>{
    	String tempStr = "";
    	@Override
    	public void onCancelled(){
    		super.onCancelled();
    		Log.d("NLiveRoid","SpeechLoop canceled");
    		destroy();
    	}
		@Override
		protected Void doInBackground(Void... params) {
			try {
		while(ENDFLAG){
            //常にADDされ続ける
			if(readBuffer.size() > 0){
				wait = true;
				Log.d("SpeechLoop","AQreadBuffer "+readBuffer.size());
				if(readBuffer.size() > maxBufferSize){
					//無理だった場合クリアしてスキップワードを読み上げさせる
					readBuffer.clear();
						 if(skip_word != null&&!skip_word.equals("")){//ワードなしならクリアはしたのでそのまま継続
							play(convert(skip_word),speed);
							}
				}else{//スキップする必要が無かったら読み上げる
		            tempStr = readBuffer.get(0);//destroyでのclearのタイミングとかぶるとおかしくなるので一旦コピー
		    		readBuffer.remove(0);
//		            Log.d("NLiveRoid","POLL---" + tempStr);
		            if(tempStr != null && !tempStr.equals("")){
								play(convert(tempStr),speed);
		            }
				}
            	//読み上げ完了するまで待つ
	            while(wait){
	            	try{
	            	Thread.sleep(100);
	            	}catch(InterruptedException e){
	            		Log.d("NLiveRoid","Speech Interrupted.");
	            		wait = false;
	            		break;
	            	}
	            }
	            Log.d("NLiveRoid","AQUES WAIT END");
			}
		}
	} catch (IndexOutOfBoundsException e) {//途中キャンセル
		e.printStackTrace();
	}catch(IllegalArgumentException e1){
		e1.printStackTrace();
		Log.d("NLiveRoid","IllegalArgumentException at SpeechLoop");
	}catch (RuntimeException e) {//その他
			e.printStackTrace();
	}
			return null;
		}

    }


	@Override
	public boolean isInitalized() {//Testのみで参照(0.8.80)
		return isInited;
	}


	}
