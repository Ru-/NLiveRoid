package nliveroid.nlr.main;

import android.content.Context;
import android.media.AudioManager;
import android.preference.Preference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class SpeechParamSeekBar  extends Preference implements OnSeekBarChangeListener {

	private static SeekBar seekbar;
    private Context context;
    private String key;
	private int width;
	private static SpeechParamSeekBar me_speed;
	private static SpeechParamSeekBar me_pich;
	private static SpeechParamSeekBar me_vol;

    public SpeechParamSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        setWidgetLayoutResource(R.layout.speech_param_seek);
			NLiveRoid app = (NLiveRoid)context.getApplicationContext();
			//nullはありえない+参照が同じなのでゲッターのある違うクラス(NLiveRoid)から取得してもOKと想定
			width = (int) (app.getViewWidth());
			//keyをセット
			key = attrs.getAttributeValue(1);

	}

    /*
     * Preference のために、ビューとデータをバインドする
     * レイアウトからカスタムビューを参照しプロパティを設定するのに適する
     * スーパークラスの実装の呼び出しを確実に行うこと
     */
    @Override
    protected void onBindView(View view){
    	seekbar = (SeekBar) view.findViewById(R.id.volume_seekbar);
        if (seekbar != null && Details.getPref() != null ) {
        	seekbar.setLayoutParams(new LinearLayout.LayoutParams((width),-2));
            seekbar.setMax(10);
            seekbar.setOnSeekBarChangeListener(this);
            seekbar.setPadding(0, 5, 30, 0);
//            seekbar.setOnTouchListener(new OnTouchListener() {
//				            public boolean onTouch(View v, MotionEvent event) {
//				                Boolean b;
//				                if ((int) ((event.getX() / seekbar.getWidth()) * 100) <= seekbar
//				                        .getProgress() + 12
//				                        && (int) ((event.getX() / seekbar.getWidth()) * 100) >= seekbar
//				                                .getProgress() - 12) {
//				                    b = false;
//				                } else {
//				                    b = true;
//				                }
//				                return b;
//				            }
//				        });
            //初期値を入れる
    		int defaultValue = 5;
        	NLiveRoid app = (NLiveRoid)context.getApplicationContext();
    		if(key != null&&!key.equals("")){
    			//テキスト
    		    TextView text = (TextView) view.findViewById(R.id.speech_param_label);
    		    if(key.equals("speech_aques_vol")){//Aques時のボリューム
        		    text.setText("ボリューム\n(AquesTalk)");
        			this.me_vol = this;
    		    }else if(key.equals("speech_speed")){
    		    text.setText("スピード");
    			this.me_speed = this;
    			}else{
        		    text.setText("ピッチ\n(標準エンジン)");
        			this.me_pich = this;
    			}
    			//最初にDetailsが表示される時、まだこのクラスはnullなので、Detailsから取得する
    			String speech_enable = Details.getPref().getDetailMapValue("speech_enable");
    			if(speech_enable != null){
    				if(speech_enable.equals("0")||speech_enable.equals("2")){
    					setEnabled(false);//全体の無効化
    				}else if(speech_enable.equals("1")){
    					setEnable_(true, false,true);
    				}else if(speech_enable.equals("3")){
    					setEnable_(true, true,false);
    				}
    			}
        	try{
        	defaultValue = Integer.parseInt(Details.getPref().getDetailMapValue(key));
        	//初期値あった場合
            seekbar.setProgress(defaultValue);
        	}catch(Exception e){
        		//初期値なかった場合5
        		seekbar.setProgress(5);
        	}//設定値がnullにならないように入れておく
        	app.setDetailsMapValue(key, String.valueOf(seekbar.getProgress()));
    		}
        }
        super.onBindView(view);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekbar) {}//onTouchactionDown見たいなもん
    @Override
    public void onProgressChanged(SeekBar seekbar, int progress, boolean fromUser) {
    	NLiveRoid app = (NLiveRoid)context.getApplicationContext();
    	Log.d("NLiveRoid","Set -------------- "+key+" " + progress);
    	app.setDetailsMapValue(key, String.valueOf(progress));
    }
    @Override
    public void onStopTrackingTouch(SeekBar seekbar) {}//onTouchActionUp見たいなもん

	 public static void setEnable_(boolean isspeed,boolean aqvolume,boolean ispich){
		 if(me_speed != null)me_speed.setEnabled(isspeed);
		 if(me_vol != null)me_vol.setEnabled(aqvolume);
		 if(me_pich != null)me_pich.setEnabled(ispich);
	 }
}