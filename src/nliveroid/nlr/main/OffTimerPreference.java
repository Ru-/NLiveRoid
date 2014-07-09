package nliveroid.nlr.main;

import nliveroid.nlr.main.Details.OffTimerNp;
import android.content.Context;
import android.content.Intent;
import android.preference.Preference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;

public class OffTimerPreference extends Preference {
	    private Context context;

		public OffTimerPreference(Context context,AttributeSet attrs) {//使わないけど無くすと起動時かここ表示時にエラーする
			this(context);
		}
		public OffTimerPreference(Context context) {
	        super(context);
	        setWidgetLayoutResource(R.layout.off_timer_pref);
	        this.context = context;
				}
	    /*
	     * Preference のために、ビューとデータをバインドする
	     * レイアウトからカスタムビューを参照しプロパティを設定するのに適する
	     * スーパークラスの実装の呼び出しを確実に行うこと
	     */
	    @Override
	    protected void onBindView(View view){
	    	//チェックが有効かどうか
	    	//nullはありえない+参照が同じなのでゲッターのある違うクラス(NLiveRoid)から取得してもOKと想定
			CheckBox check = (CheckBox)view.findViewById(R.id.off_timer_check);
	        final Button intervalButton = (Button)view.findViewById(R.id.off_timer_button);
	    	check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
				@Override
				public void onCheckedChanged(CompoundButton compoundbutton,
						boolean flag) {
					intervalButton.setVisibility(View.VISIBLE);//何故か初回-1で分岐してもdisableにならない
				    	if(!flag){//無効
				    		Intent intent = new Intent();
				    		intent.setAction("bindTop.NLR");
				    		intent.putExtra("off_timer", "-1");
				    		context.sendBroadcast(intent);
					    		intervalButton.setVisibility(View.INVISIBLE);
						}else{
							int defaultValue = -1;
							NLiveRoid app = (NLiveRoid)context.getApplicationContext();
							try{
							defaultValue = Integer.parseInt(app.getDetailsMapValue("off_timer"));
							}catch(Exception e){
								e.printStackTrace();
								defaultValue = -1;
							}
							if(defaultValue < 1){
								defaultValue = 30;
							}
//							Log.d("DEFAULT","OFFTIERAAA " + defaultValue);
							Intent intent = new Intent();
				    		intent.setAction("bindTop.NLR");
				    		intent.putExtra("off_timer", String.valueOf(defaultValue));
				    		context.sendBroadcast(intent);
				    		intervalButton.setVisibility(View.VISIBLE);
						}
				}
	    	});
	    	try{
	    	if (intervalButton != null && Details.getPref() != null ) {
	    		intervalButton.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View view) {
						int defaultValue = -1;
						NLiveRoid app = (NLiveRoid)context.getApplicationContext();
						try{
						defaultValue = Integer.parseInt(app.getDetailsMapValue("off_timer"));
						}catch(Exception e){
							e.printStackTrace();
							defaultValue = -1;
						}
						long offer_start = -1;
						try{
						if(app.getDetailsMapValue("offtimer_start")!=null){
							offer_start = Long.parseLong(app.getDetailsMapValue("offtimer_start"));
						}
						}catch(Exception e){
							e.printStackTrace();
							offer_start = -1L;
						}
//						Log.d("DEFAULT","OFFTIERDDD " + defaultValue);
						new OffTimerNp(context,offer_start,defaultValue).create().show();
					}
	        	});
	        }
	   	 //有効じゃなかったらボタンをdisableにするsetEnableだと初期時に何故かセットされてくれなかった
	    	int defaultValue = -1;
			NLiveRoid app = (NLiveRoid)context.getApplicationContext();
			try{
			defaultValue = Integer.parseInt(app.getDetailsMapValue("off_timer"));
			}catch(Exception e){
				e.printStackTrace();
				defaultValue = -1;
			}
//			Log.d("DEFAULT","OFFTIERXXX " + defaultValue);
	    	if(defaultValue > 0){
	    		check.setChecked(true);
	    		intervalButton.setVisibility(View.VISIBLE);
	    	}else{
	    		check.setChecked(false);
	    		intervalButton.setVisibility(View.INVISIBLE);
	    	}
	    	}catch(NumberFormatException e){
	    		e.printStackTrace();
	    		return;
	    	}catch(Exception e){
	    		e.printStackTrace();
	    		return;
	    	}
	        super.onBindView(view);
	    }

	}
