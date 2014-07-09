package nliveroid.nlr.main;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;


public class SpeechSkipCountPicker extends DialogPreference {

	private String key;
	private int beforNum = 10;
	private Context context;
	private NumberPicker np;
	private static SpeechSkipCountPicker me;

	public SpeechSkipCountPicker(Context context, AttributeSet attrs) {
		super(context, attrs);
		 key = attrs.getAttributeValue(2);//これがどうゆう規則でこのインデックスになるのかわからない!!
		 this.context = context;
		 this.me = this;
	}

	 @Override
	 protected View onCreateDialogView() {
		super.onCreateDialogView();
		ScrollView sv = new ScrollView(context);
		try{
		 beforNum  =  Integer.parseInt(Details.getPref().getDetailMapValue(key));
		}catch(NumberFormatException e){
			TextView tv = new TextView(context);
			tv.setText("設定値の初期化に失敗");
			sv.addView(tv,-1,-1);
			return sv;
		}

		 np = new NumberPicker(this.getContext());
		 np.setRange(1, 10);
		 np.setCurrent(beforNum);
		 np.setClickable(true);
		 np.setLongClickable(true);
		 sv.addView(np);

		return sv;
	 }

	 @Override
	 protected void onDialogClosed(boolean positiveResult) {
	 super.onDialogClosed(positiveResult);
	 Log.d("log"," CURRENT KEY " + key + " NUM " + np.getCurrent());

	 if(positiveResult){
		 Details.getPref().setPreferenceKeyValue(key, np.getCurrent());
	 }
	 }


	 public static void setEnable_(boolean isEnable){
		 me.setEnabled(isEnable);
	 }

}
