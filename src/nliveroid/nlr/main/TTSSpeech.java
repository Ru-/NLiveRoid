package nliveroid.nlr.main;

import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.ArrayBlockingQueue;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener;
import android.util.Log;

public class TTSSpeech implements TextToSpeech.OnInitListener,Speechable{
    private TextToSpeech tts;//これはnot static
    private Activity act;
    private boolean isInited;
    private int speed;
    private int pich;
    private boolean wait = false;
	private boolean ENDFLAG = true;
	private boolean isClearing = false;
    private String skip_word;
	private int maxBufferSize;
	private SpeechLoop loopThread;
    private  ArrayBlockingQueue<String> readBuffer = new ArrayBlockingQueue<String>(10);
    private HashMap<String,String> noMean = new HashMap<String,String>();
    private String nomeanStr = "0";
    public TTSSpeech(String skip_word,int maxBufferSize){
        // TextToSpeechオブジェクトの生成
        this.skip_word = skip_word;
        this.maxBufferSize = maxBufferSize;
    }

    //Contextは何故かrebindみたくなってるっぽいのですぐnullになったから別ロジック
    public void setContext(Activity act,int speed,int pich){
    	this.act = act;
        tts = new TextToSpeech(((NLiveRoid)act.getApplicationContext()).getBaseContext(), this);
        // 再生速度の設定
        this.speed = speed;
        // 再生ピッチの設定 initしてないと、設定されないのでinit後に設定
        this.pich = pich;
    }

    public void destroy() {
    	Log.d("NLiveRoid","TTS DESTROY ---------------- ");
    	tts.stop();
    	readBuffer.clear();
    	if(loopThread != null&&loopThread.getStatus() != AsyncTask.Status.FINISHED)loopThread.cancel(true);
    	loopThread = null;
        // TextToSpeechのリソースを解放する
        tts.shutdown();
    }

    @Override
    public void onInit(final int status) {
    	Log.d("NLiveRoid","TTS INIT ---------------- " + isInited + "  status:"+status);
    	if(isInited)return;
    	if(loopThread != null&&loopThread.getStatus() != AsyncTask.Status.FINISHED)loopThread.cancel(true);
    	loopThread = null;
        if (TextToSpeech.SUCCESS == status) {
            Locale locale1 = Locale.JAPANESE;
            Locale locale2 = Locale.JAPAN;
            if(act == null){
            	return;
            }
            if(tts == null){
                tts = new TextToSpeech(act.getBaseContext(), this);
            }
            if (tts.isLanguageAvailable(locale1) >= TextToSpeech.LANG_AVAILABLE){
            	tts.setLanguage(locale1);
            }else if(tts.isLanguageAvailable(locale2) >= TextToSpeech.LANG_AVAILABLE) {
                tts.setLanguage(locale2);
            }else {
                act.runOnUiThread(new Runnable(){
					@Override
					public void run() {
		                MyToast.customToastShow(act, "読み上げの初期化に失敗しました\n日本語に非対応:000");
					}
                });
                return;
            }
        } else {
        	act.runOnUiThread(new Runnable(){
				@Override
				public void run() {
		        	MyToast.customToastShow(act, "読み上げの初期化に失敗しました:111 \nSTATUS:" + status);
				}
            });
        	return;
        }
        // 発話終了のListner
        tts.setOnUtteranceCompletedListener(new OnUtteranceCompletedListener() {
                public void onUtteranceCompleted(String utteranceId) {
                    wait = false;
                    if(isClearing){
                    	isClearing = false;
                    }
                }
            });

        isInited = true;
        setSpeed(speed);
        setPich(pich);
        //初期化前にアドされていたら読まれない
        loopThread = new SpeechLoop();
        loopThread.execute();
    }

    @Override
    public void addSpeech(String sentence) throws InterruptedException {
    	if(isInited&&0 < sentence.length()&&!isClearing) {
			//isClearingクリア中にアドされちゃうと、省略を何回も読んじゃうのでisClearingにして、それを読んだらfalse
    	readBuffer.put(sentence);
		}
    }



    class SpeechLoop extends AsyncTask<Void,Void,Void>{
    	@Override
    	public void onCancelled(){
    		super.onCancelled();
    		wait = false;
    		ENDFLAG = false;
    	}
		@Override
		protected Void doInBackground(Void... params) {
			try {
			noMean.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID ,nomeanStr);
			while(ENDFLAG){
//				Log.d("Log","WHILE");
            //常にADDされ続ける
			if(readBuffer.size() > 0){//無理だった場合、今あるのをクリアして省略を読む
				if(readBuffer.size() > maxBufferSize){
					readBuffer.clear();
		            wait =true;
					if(skip_word == null||skip_word.equals("")){//ワードなしなら読み上げずにisClearingも変えない
					}else{
		            isClearing = true;
					tts.speak(skip_word, TextToSpeech.QUEUE_ADD, noMean);//クリアする場合、QUEUE_FLUSH
					}
				}else{
		            isClearing = false;
		            wait = true;
		            String read = readBuffer.poll();//clearのタイミングとかぶるとおかしくなるので一旦コピー
		            if(read != null && !read.equals(""))tts.speak(read, TextToSpeech.QUEUE_ADD, noMean);
				}

	            while(wait){
	            	try{
	            	Thread.sleep(100);
	            	}catch(Exception e){
	            		Log.d("NLiveRoid","SPEACH SLEEP EXCEPTION");
	            	}
	            	//読み上げ完了するまで待つ
	            }

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
	public void setSpeed(int speed) {
		if(tts != null){
    		if(speed == 0){
    			tts.setSpeechRate((float)0.1);
    		}else{
    			tts.setSpeechRate((float) (speed*0.3));
    		}
    	}
	}
	@Override
    public void setPich(int val){
    	if(tts != null){
    		if(val == 0){
    			tts.setPitch((float)0.1);
    		}else{
    			tts.setPitch((float) (val*0.2));
    		}
    	}
    }

	@Override
	public boolean isInitalized() {//Testのみで参照(0.8.80)
		return isInited;
	}

	@Override
	public Object[] getStatus() {
		return new Object[]{this.speed,this.pich,false,5,0};
	}
}