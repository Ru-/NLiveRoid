package nliveroid.nlr.main;

import java.util.Iterator;
import java.util.Map;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

public class TableWidthDialog extends DialogPreference {

	private NumberPicker np;
	private int beforNum;
	private boolean isPortLaytSetting;
	private String key;
	private Context context;
	 public TableWidthDialog(Context context, AttributeSet attrs) {
	 super(context, attrs);
	 this.context = context;
	 //Attrの他の値を変える(追加)だけで、attrsの並びが変わる(不明)+nullのわけがないでOK?
	 key = attrs.getAttributeValue(3);
	 isPortLaytSetting = attrs.getAttributeValue(0).equals("0")? true:false;

	 }

	 public TableWidthDialog(Context context, AttributeSet attrs,
	 int defStyle) {
	 super(context, attrs, defStyle);
	 this.context = context;
	 key = attrs.getAttributeValue(3);
	 isPortLaytSetting = attrs.getAttributeValue(0).equals("0")? true:false;
	 }

	 @Override
	 protected View onCreateDialogView() {
		super.onCreateDialogView();
		ScrollView sv = new ScrollView(context);
		try{
		 beforNum =  Integer.parseInt(Details.getPref().getDetailMapValue(key));
		}catch(NumberFormatException e){
			e.printStackTrace();
			TextView tv = new TextView(context);
			tv.setText("設定値の初期化に失敗");
			sv.addView(tv);
			return sv;
		}

		 np = new NumberPicker(this.getContext());
		 np.setRange(0, 100);
		 np.setCurrent(beforNum);
		 np.setClickable(true);
		 np.setLongClickable(true);
		 sv.addView(np);


		return sv;
	 }

	 @Override
	 protected void onDialogClosed(boolean positiveResult) {
	 super.onDialogClosed(positiveResult);
	 Log.d("NLiveRoid"," CURRENT KEY " + key + " NUM " + np.getCurrent());

	 if(positiveResult){
		 int limitCount = 10;
	 //値が増えたらその他の列に均等に分散する
		 Map<String,Integer> wlist = null;
		 if(isPortLaytSetting){
		 wlist = Details.getPref().getPWidthList();
		 }else{
			 wlist = Details.getPref().getLWidthList();
		 }
		 //自分自身の値を更新
		 wlist.put(key, np.getCurrent());
		 double diff = (100-np.getCurrent());
//		 Log.d("log","DIFF A " + diff);


		 int loop = 0;
		 int ammount = 0;
		 while(loop < limitCount){
		 wlist = calculateWidth(wlist,diff);
		 //足して100前後10になるまで繰り返す
		 Iterator<String> repeatKey = wlist.keySet().iterator();
		 while(repeatKey.hasNext()){
			 ammount += wlist.get(repeatKey.next());
		 }
		 if(ammount>=95&&ammount<=105){
			 break;
		 }
		 loop ++;
		 }
		 //100に満たなかったらあまりを全てcommentに( 色を変えたときに右が白くなるのを防ぐ)
		 if(ammount<100){
			 int reast = 100-ammount;
			 if(isPortLaytSetting){
			 wlist.put("comment_width_p",wlist.get("comment_width_p")+reast);
			 }else{
				 wlist.put("comment_width_l",wlist.get("comment_width_l")+reast);
			 }
		 }
		 //設定値を保存
		 Iterator<String> it = wlist.keySet().iterator();
		 String listkey = "";
		 while(it.hasNext()){
			 listkey = it.next();
//			 Log.d("log","RESULT " + listkey + "  val " + wlist.get(listkey));
			Details.getPref().setPreferenceKeyValue(listkey, wlist.get(listkey));
			if(isPortLaytSetting){
			Details.getPref().updatePSummary();
			}else{
				Details.getPref().updateLSummary();
			}
		 }
	 }

	 }

	 private Map<String,Integer> calculateWidth(Map<String,Integer> wlist,double diff){
//		 Log.d("log","Befor " + beforNum + "  np " + np.getCurrent());

		 String nextKey = "";
		 Iterator<String> it  = wlist.keySet().iterator();
		 while(it.hasNext()){
			 nextKey = it.next();
			 if(!nextKey.equals(key)){//このキー以外の要素に設定値と100との差から引いたあまりを求める
				  diff -= wlist.get(nextKey);
			 }
		 }
		 diff /= 5;//あまりとして足す値を平均化
//		 Log.d("log","DIFF " + diff);

		 it  = wlist.keySet().iterator();
		 while(it.hasNext()){
			 nextKey = it.next();
			 if(!nextKey.equals(key)){//このキー以外の要素に均等に足していく
				 wlist.put(nextKey, (int) (wlist.get(nextKey)+diff));
				 if(wlist.get(nextKey)<0){
//					 Log.d("log","NEGATIVE " + nextKey + "  val " +wlist.get(nextKey));
					 //マイナスになる時は0にしてマイナス値を平均化して足していって繰り返す
					wlist =  negativeRecursive(wlist,wlist.keySet().iterator(),nextKey,wlist.get(nextKey)/5);
				 }
			 }
//			 Log.d("log","KEY " + nextKey + "  value "  + wlist.get(nextKey));
		 }
		return wlist;

	 }

	 private Map<String,Integer> negativeRecursive(Map<String , Integer> wlist, Iterator<String> it,String myKey,double negative){
		 String nextKey = "";
			wlist.put(myKey,0);
		 			while(it.hasNext()){
		 				nextKey = it.next();
//		 				Log.d("log"," NEG " + nextKey + " val " + wlist.get(nextKey) + " negative " + negative);
		 				if(!nextKey.equals(myKey)&&!nextKey.equals(key)){
		 					wlist.put(nextKey,(int) (wlist.get(nextKey)+negative));

		 				if(wlist.get(nextKey)<0){
//		 					Log.d("log"," TIVE " + nextKey + " val " + wlist.get(nextKey) + " negative " + negative);

		 				wlist = negativeRecursive(wlist,wlist.keySet().iterator(),nextKey,wlist.get(nextKey)/5);

		 				}
		 				}
		 			}
		 return wlist;
	 }

}