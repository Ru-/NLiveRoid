package nliveroid.nlr.main;

import java.util.HashMap;

import android.content.Context;
import android.media.AudioManager;
import android.preference.Preference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class SeekBarPreference extends Preference implements OnSeekBarChangeListener {

	private static SeekBar seekbar;
	private static SeekBarPreference thisView;
    private static int MAX_PROGRESS;
    private Context context;
    private boolean isEnable = true;
	private int width;
	private static AudioManager audio;

    public SeekBarPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        try{
        setWidgetLayoutResource(R.layout.volume_seek);
         //音量を記憶
        thisView = this;
			audio = (AudioManager)context.getSystemService(context.AUDIO_SERVICE);
			MAX_PROGRESS = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
			NLiveRoid app = (NLiveRoid)context.getApplicationContext();
			//nullはありえない+参照が同じなのでゲッターのある違うクラス(NLiveRoid)から取得してもOKと想定
			width = (int) (app.getViewWidth());
			//しばらくするとappのdetailsMapがnullになるのでこのヌルポ
			if(Details.getPref() != null&& app != null && app.getDetailsMapValue("fix_volenable") != null &&Boolean.parseBoolean(app.getDetailsMapValue("fix_volenable"))){
				//有効
				thisView.setEnabled(true);
				isEnable = true;
			}else{
				//コメントオンリー(固定値設定無効ならシークももちろん無効)
				thisView.setEnabled(false);
				isEnable = false;
			}

        }catch(Exception e){
        	e.printStackTrace();
        	if(Details.getPref() != null)Details.getPref().finish();
        }
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
        	seekbar.setLayoutParams(new LinearLayout.LayoutParams(width,-2));
            seekbar.setMax(MAX_PROGRESS);
            seekbar.setPadding(10, 30, 50,0);
            seekbar.setOnSeekBarChangeListener(this);
            seekbar.setEnabled(isEnable);
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
        	try{
        	int defaultValue = Integer.parseInt(Details.getPref().getDetailMapValue("fix_volvalue"));
        	//初期値あった場合
            seekbar.setProgress(defaultValue);
        	}catch(Exception e){
        		//初期値なかった場合 今の音量
        		seekbar.setProgress(audio.getStreamVolume(AudioManager.STREAM_MUSIC));
        	}//設定値がnullにならないように入れておく
        	NLiveRoid app = (NLiveRoid)context.getApplicationContext();
        	app.setDetailsMapValue("fix_volvalue", String.valueOf(seekbar.getProgress()));
        }
        super.onBindView(view);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekbar) {}//onTouchactionDown見たいなもん
    @Override
    public void onProgressChanged(SeekBar seekbar, int progress, boolean fromUser) {
    	NLiveRoid app = (NLiveRoid)context.getApplicationContext();
    	app.setDetailsMapValue("fix_volvalue", String.valueOf(progress));
    }
    @Override
    public void onStopTrackingTouch(SeekBar seekbar) {}//onTouchActionUp見たいなもん

    public static void setSeekEnable(boolean isEnable){
    	if(seekbar == null)return;
        thisView.setEnabled(isEnable);
        if(isEnable){
        //初期値を入れる
    	try{
    	int defaultValue = Integer.parseInt(Details.getPref().getDetailMapValue("fix_volvalue"));
    	//初期値あった場合
        seekbar.setProgress(defaultValue);
    	}catch(Exception e){
    		//初期値なかった場合 今の音量
    		seekbar.setProgress(audio.getStreamVolume(AudioManager.STREAM_MUSIC));
    		}
    	}
    }
}