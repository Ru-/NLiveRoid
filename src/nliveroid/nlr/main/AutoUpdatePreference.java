package nliveroid.nlr.main;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.preference.Preference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ScrollView;

public class AutoUpdatePreference extends Preference {
    private Context context;
    private int defaultValue = -1;
	public AutoUpdatePreference(Context context,AttributeSet attrs) {
		this(context);
	}
	public AutoUpdatePreference(Context context) {
        super(context);
        setWidgetLayoutResource(R.layout.comment_update_pref);
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
		CheckBox check = (CheckBox)view.findViewById(R.id.auto_update_check);
        final Button intervalButton = (Button)view.findViewById(R.id.auto_update_button);
    	NLiveRoid app = null;
    	check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(CompoundButton compoundbutton,
					boolean flag) {
				intervalButton.setVisibility(View.VISIBLE);//何故か初回-1で分岐してもdisableにならない

		    		NLiveRoid app = (NLiveRoid)context.getApplicationContext();
			    		if(!flag){//自動更新無効
				    		app.setDetailsMapValue("auto_comment_update", "-1");
				    		intervalButton.setVisibility(View.INVISIBLE);
					}else{
						if(defaultValue < 0){
							defaultValue = 2;
						}
			    		app.setDetailsMapValue("auto_comment_update", String.valueOf(defaultValue));
			    		intervalButton.setVisibility(View.VISIBLE);
					}
			}
    	});
    	try{
    		app = (NLiveRoid)context.getApplicationContext();
    	defaultValue = Integer.parseInt(app.getDetailsMapValue("auto_comment_update"));
		setButtonEvent(intervalButton);
   	 //有効じゃなかったらボタンをdisableにするsetEnableだと初期時に何故かセットされてくれなかった
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

    private void setButtonEvent(Button bt){

        if (bt != null && Details.getPref() != null ) {
        	bt.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View view) {
					ScrollView sv = new ScrollView(context);
					final NumberPicker np = new NumberPicker(context);
					np.setRange(1, 10);
					if(defaultValue <= 0){
						np.setCurrent(2);
					}else{
					np.setCurrent(defaultValue);
					}
					sv.addView(np,-1,-1);
					new AlertDialog.Builder(context)
					.setTitle("更新間隔(分)")
					.setView(sv)
					.setPositiveButton("OK",new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							NLiveRoid app = (NLiveRoid)context.getApplicationContext();
							defaultValue = np.getCurrent();
							app.setDetailsMapValue("auto_comment_update", String.valueOf(np.getCurrent()));
						}
					})
					.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {

						}
					})
					.create().show();
				}

        	});
        }
    }



}
