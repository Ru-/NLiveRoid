package nliveroid.nlr.main;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;

public class QuickSettingDialog  extends DialogPreference{
	private Context context;
	private String key;
	private CheckBox[] cbs;
	private RadioGroup rg;
	public QuickSettingDialog(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		key = attrs.getAttributeValue(2);
		Log.d("NLiveRoid"," key"+key);
	}

	 @Override
	 protected View onCreateDialogView() {
		 super.onCreateDialogView();
			ScrollView sv = new ScrollView(context);
			int defaultVal0 = 0;
			int defaultVal1 = 0;
			try{
				 defaultVal0 =  Integer.parseInt(Details.getPref().getDetailMapValue("quick_0"));
				 defaultVal1 =  Integer.parseInt(Details.getPref().getDetailMapValue("quick_1"));
				 Log.d(" ", " QQQQ" + defaultVal0);
				 Log.d(" ", " QQQQ" + defaultVal1);
				}catch(NumberFormatException e){
					e.printStackTrace();
					MyToast.customToastShow(context,"カスタムMENUの設定値の読み取りに失敗\nXMLが不正");
					return sv;
				}
			TableLayout tl = new TableLayout(context);
			TableRow[] trs = new TableRow[12];//ラジオボタン分多くする
			cbs = new CheckBox[11];
			for(int i = 0; i < trs.length; i++){
			trs[i] = new TableRow(context);
			if(i < cbs.length)cbs[i] = new CheckBox(context);//ラジオボタンの行分1つ少ない
			}
			rg = new RadioGroup(context);
			rg.setOrientation(RadioGroup.HORIZONTAL);
			RadioButton standard_rb = new RadioButton(context);
			standard_rb.setId(0);
			standard_rb.setText("標準");
			RadioButton quick_rb = new RadioButton(context);
			quick_rb.setId(1);
			quick_rb.setText("カスタムMENU");
			RadioButton comment_form_rb = new RadioButton(context);
			comment_form_rb.setId(2);
			comment_form_rb.setText("投稿");
			rg.addView(standard_rb);
			rg.addView(quick_rb);
			rg.addView(comment_form_rb);
			rg.check((defaultVal0 & 0xF0)>>4 <=2? (defaultVal0 & 0xF0)>>4:0);
			trs[0].addView(rg,new TableRow.LayoutParams(-1,-2));

			cbs[0].setText("全画面(スマホ版プレイヤーのみ");
			cbs[0].setChecked((defaultVal0 & 0x08) > 0? true:false);
			cbs[1].setText("表示設定");
			cbs[1].setChecked((defaultVal0 & 0x04) > 0? true:false);
			cbs[2].setText("コメント欄更新");
			cbs[2].setChecked((defaultVal0 & 0x02) > 0? true:false);
			cbs[3].setText("ログ取得(プレアカのみ動作)");
			cbs[3].setChecked((defaultVal0 & 0x01) > 0? true:false);
			cbs[4].setText("ユーザー名自動");
			cbs[4].setChecked((defaultVal1 & 0x40) > 0? true:false);
			cbs[5].setText("184");
			cbs[5].setChecked((defaultVal1 & 0x20) > 0? true:false);
			cbs[6].setText("コマンド");
			cbs[6].setChecked((defaultVal1 & 0x10) > 0? true:false);
			cbs[7].setText("投稿");
			cbs[7].setChecked((defaultVal1 & 0x08) > 0? true:false);
			cbs[8].setText("Tweet");
			cbs[8].setChecked((defaultVal1 & 0x04) > 0? true:false);
			cbs[9].setText("設定");
			cbs[9].setChecked((defaultVal1 & 0x02) > 0? true:false);
			cbs[10].setText("視聴終了");//各設定にするにはビットが足りないので仕方ない
			cbs[10].setChecked((defaultVal1 & 0x01) > 0? true:false);
			for(int i = 0; i < trs.length; i++){
				if(i < cbs.length)trs[i+1].addView(cbs[i],new TableRow.LayoutParams(-1,-2));
				trs[i].setPadding(15, 25, 15, 0);//パディング共通
				tl.addView(trs[i],new TableLayout.LayoutParams(-1,-2));
			}
			sv.addView(tl,-1,-1);
				return sv;
	 }
	 @Override
	 protected void onDialogClosed(boolean positiveResult) {
	 super.onDialogClosed(positiveResult);
		 if(positiveResult){//設定値をセットする
			 byte quick_0 = 0;
			 byte quick_1 = 0;
			 switch(rg.getCheckedRadioButtonId()){
			 case 0://上位ビット4つは0のまま
				 break;
			 case 1:
				 quick_0 = 0x10;
				 break;
			 case 2:
				 quick_0 = 0x20;
				 break;
			 }
			 if(cbs[0].isChecked()){
				 quick_0 = (byte) (quick_0 | 0x08);
			 }if(cbs[1].isChecked()){
				 quick_0 = (byte) (quick_0 | 0x04);
			 }if(cbs[2].isChecked()){
				 quick_0 = (byte) (quick_0 | 0x02);
			 }if(cbs[3].isChecked()){
				 quick_0 = (byte) (quick_0 | 0x01);
			 }if(cbs[4].isChecked()){
				 quick_1 = (byte) (quick_1 | 0x40);
			 }if(cbs[5].isChecked()){
				 quick_1 = (byte) (quick_1 | 0x20);
			 }if(cbs[6].isChecked()){
				 quick_1 = (byte) (quick_1 | 0x10);
			 }if(cbs[7].isChecked()){
				 quick_1 = (byte) (quick_1 | 0x08);
			 }if(cbs[8].isChecked()){
				 quick_1 = (byte) (quick_1 | 0x04);
			 }if(cbs[9].isChecked()){
				 quick_1 = (byte) (quick_1 | 0x02);
			 }if(cbs[10].isChecked()){
				 quick_1 = (byte) (quick_1 | 0x01);
			 }
			 if(Details.getPref() == null){
				 MyToast.customToastShow(context, "設定値の保存に失敗しました");
			 }else{
//				 Log.d("QQQQQQXXXX"," " + quick_0);
//				 Log.d("QQQQQQXXXX"," " + quick_1);
				 Details.getPref().setDetailsMap("quick_0", quick_0);
				 Details.getPref().setDetailsMap("quick_1", quick_1);
			 }
		}
	 }

}
