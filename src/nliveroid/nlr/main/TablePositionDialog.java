package nliveroid.nlr.main;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class TablePositionDialog extends DialogPreference{
			private NumberPicker np;
			private TextView tv;
			private String key;
			private CheckBox cb;
			 public TablePositionDialog(Context context, AttributeSet attrs) {
			 super(context, attrs);
			 key = attrs.getAttributeValue(2);
			 }

			 public TablePositionDialog(Context context, AttributeSet attrs,
			 int defStyle) {
			 super(context, attrs, defStyle);
			 key = attrs.getAttributeValue(2);
			 }



			 @Override
			 protected View onCreateDialogView() {
				super.onCreateDialogView();
				int defaultNum = 0;
				Context context = getContext();
				 np = new NumberPicker(context);
				 tv = new TextView(context);
				 tv.setGravity(Gravity.CENTER);
				 TableLayout baseTableLayout = new TableLayout(context);
				 baseTableLayout.setColumnStretchable(0, true);
				 TableRow tr0 = new TableRow(context);
				 TableRow tr1 = new TableRow(context);
				 tr0.addView(tv);
				 tr1.addView(np);
				 baseTableLayout.addView(tr0,new LinearLayout.LayoutParams(-1,-2));

				 if(key.equals("x_pos_p")||key.equals("x_pos_l")){
				 tv.setText("ヘッダーの左上の位置を決定\n画面横幅全体が0～100として\n-99～+99で設定");
				 np.setRange(0, 99);
				 cb = new CheckBox(context);
				 cb.setText("マイナス");
				 TableRow trc = new TableRow(context);
				 trc.addView(cb);
				 baseTableLayout.addView(trc,new LinearLayout.LayoutParams(-1,-2));
				 try{
				 defaultNum = Integer.parseInt(Details.getPref().getDetailMapValue(key));
				 }catch(Exception e){
					 defaultNum = 0;
				 }

				 if(defaultNum<0){
					 defaultNum = -defaultNum;
					 cb.setChecked(true);
				 }
				 }else if(key.equals("y_pos_p")||key.equals("y_pos_l")){
					 tv.setText("上端0～下端100");
					 np.setRange(0, 100);
					 try{
					 defaultNum = Integer.parseInt(Details.getPref().getDetailMapValue(key));
					 if(defaultNum<0||defaultNum>100){
						 defaultNum = 50;
					 }
					 }catch(Exception e){
						 defaultNum = 50;
					 }
				 }else if(key.equals("bottom_pos_p")||key.equals("bottom_pos_l")){
					 tv.setText("画面全体の高さを100として\n0～100で設定");
					 np.setRange(0, 100);
					 cb = new CheckBox(context);
					 cb.setText("コメント追加の方向上");
					 TableRow trc = new TableRow(context);
					 trc.addView(cb);
					 baseTableLayout.addView(trc,new LinearLayout.LayoutParams(-1,-2));
					 try{
					 defaultNum = Integer.parseInt(Details.getPref().getDetailMapValue(key));
					 if(defaultNum<-100||defaultNum>100){
						 defaultNum = 50;
					 }
					 }catch(Exception e){
						 defaultNum = 50;
					 }
					 if(defaultNum<0){
						 defaultNum = -defaultNum;
						 cb.setChecked(true);
					 }
				 }else if(key.equals("width_p")||key.equals("width_l")){
					 tv.setText("画面全体の横幅を100として\n0～100で設定");
					 np.setRange(0, 100);
					 try{
					 defaultNum = Integer.parseInt(Details.getPref().getDetailMapValue(key));
					 if(defaultNum<0||defaultNum>100){
						 defaultNum = 100;
					 }
					 }catch(Exception e){
						 defaultNum = 100;
					 }
					 if(defaultNum<0){
						 defaultNum = -defaultNum;
					 }
				 }
				 np.setClickable(true);
				 np.setLongClickable(true);
				 np.setCurrent(defaultNum);

				 baseTableLayout.addView(tr1,new LinearLayout.LayoutParams(-1,-2));
				 ScrollView sv = new ScrollView(context);
				 sv.addView(baseTableLayout,-1,-1);
				 return sv;
			 }

			 @Override
			 protected void onDialogClosed(boolean positiveResult) {
			 super.onDialogClosed(positiveResult);
//			 Log.d("log"," CURRENT KEY " + key + " NUM " + np.getCurrent());
			 if(positiveResult){
				 if(cb != null&&(key.equals("bottom_pos_p")||key.equals("bottom_pos_l")||key.equals("x_pos_p")||key.equals("x_pos_l"))){
					 if(cb.isChecked()){
					 Details.getPref().setPreferenceKeyValue(key, -np.getCurrent());
					 }else{
						 Details.getPref().setPreferenceKeyValue(key, np.getCurrent());
					 }
				 }else{
				 Details.getPref().setPreferenceKeyValue(key, np.getCurrent());
				 }
			 }

			 }



	}