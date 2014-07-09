package nliveroid.nlr.main;

import android.content.Context;
import android.os.AsyncTask;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Spinner;

public class SpeechTestPreference  extends DialogPreference{

    private Context context;
    private static SpeechTestPreference me;
	private int width;
	private static Spinner phontSpinner;
	private FrameLayout progressPane;
	private Speechable mSpeech;
	private boolean ENDFLAG = true;

    public SpeechTestPreference(final Context context,AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        this.me = this;
    }

    @Override
    protected View onCreateDialogView() {
//Aquesの時の声色
			View view = (View)LayoutInflater.from(context).inflate(R.layout.speech_test, null);
    			ArrayAdapter adapter = ArrayAdapter.createFromResource(context, R.array.speech_phont,
    				android.R.layout.simple_spinner_item);
    		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

    		phontSpinner = (Spinner)view.findViewById(R.id.speech_test_spinner);
    					phontSpinner.setAdapter(adapter);

    					int defaultValue = ((Details)context).getDetailMapValue("speech_aques_phont") == null?  0:Integer.parseInt(((Details)context).getDetailMapValue("speech_aques_phont"));
    					phontSpinner.setSelection(defaultValue);
    					String speech_enable = Details.getPref().getDetailMapValue("speech_enable");
    					if(speech_enable == null||(speech_enable != null && speech_enable.equals(""))){
    						//何故かnullなら一応有効にしておく
    					}else {
    						if(speech_enable.equals("0")||speech_enable.equals("1")){//TTSの時
        						phontSpinner.setEnabled(false);
    						}
    					}
    		final EditText et = (EditText)view.findViewById(R.id.speech_test_et);

    		//playボタン
    			Button play = (Button)view.findViewById(R.id.speech_test_bt);
    			play.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View v) {
						String text = et.getText().toString();
						if(text != null && !text.equals("")){
							if(progressPane != null)progressPane.setVisibility(View.VISIBLE);
							new TestSpeech().execute(text);
						}
					}
    			});

    	progressPane = (FrameLayout)view.findViewById(R.id.speech_test_progress);
    			return view;
    }




	public static void setEnable_(boolean isEnable ,boolean isPhontEnable){
		if(me!=null)me.setEnabled(isEnable);
		 if(phontSpinner != null)phontSpinner.setEnabled(isPhontEnable);
	 }


	 @Override
	 protected void onDialogClosed(boolean positiveResult) {
	 super.onDialogClosed(positiveResult);
		 if(mSpeech != null){
			 mSpeech.destroy();
			 mSpeech = null;
		 }
		 if(positiveResult){
				if(context == null)return;
				NLiveRoid app = (NLiveRoid)context.getApplicationContext();
				if(app != null)app.setDetailsMapValue("speech_aques_phont", String.valueOf(phontSpinner.getSelectedItemPosition()));
		 }
	 }

	    class TestSpeech extends AsyncTask<String,Void,Void>{
			@Override
			protected Void doInBackground(String... params) {
				if(context == null)return null;//ここの場合、トーストもできないっぽい気がするので何もしない

				NLiveRoid app = (NLiveRoid) context.getApplicationContext();
				if(app == null)return null;

				String enable = app.getDetailsMapValue("speech_enable");
				if(enable.equals("1")){
					int speed = Integer.parseInt(app.getDetailsMapValue("speech_speed"));
					int pich = Integer.parseInt(app.getDetailsMapValue("speech_pich"));
					if(mSpeech == null){
						mSpeech = new TTSSpeech("", 1);
					mSpeech.setContext(((NLiveRoid)context.getApplicationContext()).getForeACT(), speed , pich );//ここはAquesと違って、appのbaseContextじゃないと初期化されない!!!!
					}
					waitInitilize();
					if(mSpeech == null)return null;
					mSpeech.setSpeed(speed);
					mSpeech.setPich(pich);
				}else if(enable.equals("3")){
					int speed = Integer.parseInt(app.getDetailsMapValue("speech_speed"));
					int phont = phontSpinner.getSelectedItemPosition();
					 byte vol = Byte.parseByte(app.getDetailsMapValue("speech_aques_vol"));
					if(mSpeech == null){
						mSpeech = new AquesSpeech("", 1,vol);
					mSpeech.setContext(((NLiveRoid)context.getApplicationContext()).getForeACT(),speed , phont);
					}
					waitInitilize();
					mSpeech.setSpeed(speed);
					mSpeech.setPich(phont);
					((AquesSpeech)mSpeech).setVolume(vol);
				}
//				Log.d("Log","DARUI ---- " + params[0]);

				try {
					mSpeech.addSpeech(params[0]);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}catch(NullPointerException e){
//					e.printStackTrace();//onDialogClosedでnullにしたタイミングがある
				}
				return null;
			}
			@Override
			protected void onPostExecute(Void arg){
				if(progressPane != null){
					progressPane.setVisibility(View.GONE);
				}
			}

			private void waitInitilize(){
				//初期化されるまで待つ
				while(ENDFLAG){
					try {
						Thread.sleep(1000);
						Log.d("Log","IS" + mSpeech.isInitalized());
						if(mSpeech.isInitalized()){
							break;
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
						ENDFLAG = false;
						break;
					}catch(NullPointerException e){
//						e.printStackTrace();//onDialogClosedでnullにしたタイミングがある
						ENDFLAG = false;
						break;
					}catch(Exception e){
						e.printStackTrace();
						ENDFLAG = false;
						break;
					}
				}
			}
	    }



}