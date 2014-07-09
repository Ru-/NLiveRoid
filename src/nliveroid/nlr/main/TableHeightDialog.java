package nliveroid.nlr.main;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
//TableHeightというかRowHeight
public class TableHeightDialog extends DialogPreference{

		private NumberPickable np;
		private TextView tv;
		private int defaultNum;
		private String key;
		private Context context;
		//こっちが呼ばれている
		 public TableHeightDialog(Context context, AttributeSet attrs) {
		 super(context, attrs);
		 this.context = context;
		 //属性は最初を1と考えてタグに記載した順序
		 //getAttributeValue(null,"key")としても何故か返ってこない
		 key = attrs.getAttributeValue(2);
		 }

		 //こっちは呼ばれてない
		 public TableHeightDialog(Context context, AttributeSet attrs,
		 int defStyle) {
		 super(context, attrs, defStyle);
		 this.context = context;
		 key = attrs.getAttributeName(2);


		 }



		 @Override
		 protected View onCreateDialogView() {
			super.onCreateDialogView();
//			Log.d("Log"," ON CREATE DIALOG VIEW -----------" + key);
			defaultNum = 0;
			 ScrollView sv = new ScrollView(context);
			if(key.equals("init_comment_count")){//初期コメ取得件数

				try{
				defaultNum = Integer.parseInt(Details.getPref().getDetailMapValue(key));
				}catch(NumberFormatException e){
					defaultNum = 20;
					e.printStackTrace();
					TextView tv = new TextView(context);
					tv.setText("設定値の初期化に失敗");
					sv.addView(tv,-1,-1);
					return sv;
				}
				 TableLayout tl = new TableLayout(context);
				 tl.setColumnStretchable(0, true);
				 TableRow tr0 = new TableRow(context);
				np = new NumberPicker_dev10(context);
				np.setRange(0, 1000);
				np.setCurrent(defaultNum);
				 tr0.addView((View) np);
				 tl.addView(tr0,new LinearLayout.LayoutParams(-1,-2));
				 sv.addView(tl,-1,-1);
			}else if(key.equals("cellheight_test")){//高さ調整
				try{
				defaultNum = Integer.parseInt(Details.getPref().getDetailMapValue(key));
				}catch(Exception e){
					defaultNum = 3;
				}
				 np = new NumberPicker(context);
				 np.setRange(1, 5);
				 np.setCurrent(defaultNum);
				 np.setClickable(true);
				 np.setLongClickable(true);
				 tv = new TextView(context);
				 tv.setGravity(Gravity.CENTER);
				 tv.setText("文字切れする場合に、高さの比率を調整します");
				 TableLayout tl = new TableLayout(context);
				 tl.setColumnStretchable(0, true);
				 TableRow tr0 = new TableRow(context);
				 TableRow tr1 = new TableRow(context);
				 tr0.addView(tv);
				 tr1.addView((View) np);
				 tl.addView(tr0,new LinearLayout.LayoutParams(-1,-2));
				 tl.addView(tr1,new LinearLayout.LayoutParams(-1,-2));
				 sv.addView(tl,-1,-1);

			}else{//テーブル高さ設定
			try{
				//起こらない事想定、ここが通らないとこのアプリは使い物にならない
			defaultNum = Integer.parseInt(Details.getPref().getDetailMapValue(key));
			}catch(Exception e){
				defaultNum = 6;
			}
			 np = new NumberPicker(context);
			 np.setRange(0, 10);
			 np.setCurrent(defaultNum);
			 np.setClickable(true);
			 np.setLongClickable(true);
			 tv = new TextView(context);
			 tv.setGravity(Gravity.CENTER);
			 tv.setText("行の高さを0～10の範囲で入力");
			 TableLayout tl = new TableLayout(context);
			 tl.setColumnStretchable(0, true);
			 TableRow tr0 = new TableRow(context);
			 TableRow tr1 = new TableRow(context);
			 tr0.addView(tv);
			 tr1.addView((View) np);
			 tl.addView(tr0,new LinearLayout.LayoutParams(-1,-2));
			 tl.addView(tr1,new LinearLayout.LayoutParams(-1,-2));
			 sv.addView(tl,-1,-1);
			}
			 return sv;
		 }

		 @Override
		 protected void onDialogClosed(boolean positiveResult) {
		 super.onDialogClosed(positiveResult);

		 if(positiveResult){
			 Details.getPref().setPreferenceKeyValue(key, np.getCurrent());
		 }

		 }



}
