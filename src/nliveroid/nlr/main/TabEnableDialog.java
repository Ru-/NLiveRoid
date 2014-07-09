package nliveroid.nlr.main;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class TabEnableDialog extends DialogPreference{

	private TextView tv;
	private Context context;
	private String key;
	private boolean value;
	private final String fileName = "temp";
	 public TabEnableDialog(Context context, AttributeSet attrs) {
	 super(context, attrs);
	 this.context = context;
	 this.key = attrs.getAttributeValue(null, "key");
	 Log.d("NLiveRoid"," HISKEY " + key);
	 }


	 @Override
	 protected View onCreateDialogView() {
		super.onCreateDialogView();
		ScrollView sv = new ScrollView(context);
		TableLayout tl = new TableLayout(context);
		TableRow tr0 = new TableRow(context);
		TextView tv = new TextView(context);
		tv.setGravity(Gravity.CENTER);
		tv.setTextSize(20F);
		NLiveRoid app = (NLiveRoid)context.getApplicationContext();
		try{
		if(key.equals("enable_his")){
			String str = app.getDetailsMapValue("enable_his");
			if(str == null)str = "false";
			value = Boolean.parseBoolean(str);
			if(value){
			 tv.setText("履歴タブを無効にしますか?\n(アプリ再起動後無効になります)");
			}else{
			 tv.setText("履歴タブを利用しますか?\n(アプリ再起動後有効になります)");
			}
		}else if(key.equals("enable_bc")){
			String value = app.getDetailsMapValue("enable_bc");
			if(value == null)value = "false";
			if(Boolean.parseBoolean(value)){
				 tv.setText("配信タブを無効にしますか?\n(アプリ再起動後無効になります)");
			}else{
				 tv.setText("配信タブを利用しますか?\n(アプリ再起動後有効になります)");
			}
		}
		}catch(Exception e){
			e.printStackTrace();
		}
			tr0.addView(tv);
			tl.addView(tr0);
			sv.addView(tl);
		 return sv;
	 }

	 @Override
	 protected void onDialogClosed(boolean positiveResult) {
	 super.onDialogClosed(positiveResult);
	 if(positiveResult){
			if(key.equals("enable_his")){
				if(PrimitiveSetting.getACT() == null){
					MyToast.customToastShow(context, "エラーが発生しました\n普通の画面遷移でやってみて下さい");
				}else{
					PrimitiveSetting.getACT().preferenceChangedExt(key, String.valueOf(!value));
				}
			}else{

			}
		 }
	 }

}