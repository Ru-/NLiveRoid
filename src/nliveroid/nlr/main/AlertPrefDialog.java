package nliveroid.nlr.main;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import nliveroid.nlr.main.parser.AllCommunityParser;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
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

public class AlertPrefDialog extends DialogPreference{

	private NumberPicker np;
	private TextView tv;
	private int defaultNum;
	private String key;
	private Context context;
	 public AlertPrefDialog(Context context, AttributeSet attrs) {
	 super(context, attrs);
	 key = attrs.getAttributeValue(1);
	 this.context = context;
	 }

	 public AlertPrefDialog(Context context, AttributeSet attrs,
	 int defStyle) {
	 super(context, attrs, defStyle);
	 key = attrs.getAttributeValue(1);
	 this.context = context;
	 }

	 @Override
	 protected View onCreateDialogView() {
		super.onCreateDialogView();
		 ScrollView sv = new ScrollView(context);
		 TableLayout tl = new TableLayout(context);
		if(key == null)return sv;
		if(key.equals("alert_interval")){
		defaultNum = 5;
		try{
			//デフォルト値を始めに保存しているので
			//null起こらない事想定、ここが通らないとこのアプリは使い物にならない
		defaultNum = Integer.parseInt(PrimitiveSetting.getACT().getAlertParams("alert_interval"));
		}catch(Exception e){
			defaultNum = 5;
		}
		 np = new NumberPicker(context);
		 np.setRange(1, 120);
		 np.setCurrent(defaultNum);
		 np.setClickable(true);
		 np.setLongClickable(true);
		 tv = new TextView(context);
		 tv.setGravity(Gravity.CENTER);
		 tv.setText("アラートのアクセス頻度を入力(分)");
		 tl.setColumnStretchable(0, true);
		 TableRow tr0 = new TableRow(context);
		 TableRow tr1 = new TableRow(context);
		 tr0.addView(tv);
		 tr1.addView(np);
		 tl.addView(tr0,new LinearLayout.LayoutParams(-1,-2));
		 tl.addView(tr1,new LinearLayout.LayoutParams(-1,-2));
		}else{
			 tv = new TextView(context);
			 tv.setGravity(Gravity.CENTER);
			 tv.setText("OKをタップすると、\n参加中のコミュニティ全てを対象にします\n\n個別に設定する場合は、\n参加中コミュニティタブを一覧の表示にし、\nVOLUMEキー下押下後のチェックボックスで設定します");
			 TableRow tr0 = new TableRow(context);
			 tr0.addView(tv);
			 tl.addView(tr0,new LinearLayout.LayoutParams(-1,-1));
		}
		 sv.addView(tl,-1,-1);
		 return sv;
	 }

	 @Override
	 protected void onDialogClosed(boolean positiveResult) {
	 super.onDialogClosed(positiveResult);
		 if(positiveResult){
			 if(key == null)return;
			 if(key.equals("alert_interval")){
			 PrimitiveSetting.getACT().preferenceChangedExt("alert_interval", String.valueOf(np.getCurrent()));
			 }else if(key.equals("alert_community")){
				 new ReadAllCommunity().execute();
			 }
		 }
	 }

	 class ReadAllCommunity extends AsyncTask<Void,Void,Integer> implements FinishCallBacks{
		private int pageCount = 10;
		private boolean ENDFLAG = true;
		private String pageStr;
		private ErrorCode error;
		private ProgressDialog dialog;
		private ArrayList<String> coList = new ArrayList<String>();
		@Override
		public void onPreExecute(){
			dialog = new ProgressDialog(context);
			dialog.setMessage("処理中");
			dialog.show();
		}
		@Override
		public void onCancelled(){
			if(dialog != null && dialog.isShowing())dialog.cancel();
			super.onCancelled();
		}
		 @Override
		protected Integer doInBackground(Void... params) {
			NLiveRoid app = (NLiveRoid)context.getApplicationContext();
				error = app.getError();
				if(error == null){
					app.initStandard();
					error = app.getError();
				}
				//セッション取得
				String sessionid = Request.getSessionID(error);
				if(error.getErrorCode() != 0){
					return 0;
				}
			for(int pageNum = 1; pageNum <= pageCount;pageNum++){
				Log.d("NLiveRoid","ALERT ALL READ " + pageNum + " " + pageCount);
					//トップのソース取得後パース
					InputStream source = Request.doGetToInputStreamFromFixedSession(sessionid, String.format(URLEnum.ALLCOMMUNITY,pageNum), error);
					if(source == null){
						error.setErrorCode(-48);
						return 0;
					}
					 try {
						 AllCommunityParser handler = new AllCommunityParser(this);
						  org.ccil.cowan.tagsoup.Parser parser = new org.ccil.cowan.tagsoup.Parser();
					        parser.setContentHandler(handler);
					        parser.parse(new InputSource(source));
					  } catch (org.xml.sax.SAXNotRecognizedException e) {
					      // Should not happen.
						  e.printStackTrace();
					      throw new RuntimeException(e);
					  } catch (org.xml.sax.SAXNotSupportedException e) {
					      // Should not happen.
						  e.printStackTrace();
					      throw new RuntimeException(e);
					  } catch(UnknownHostException e){//接続悪い時になる
						  if(error != null ){
							  error.setErrorCode(-6);
							  e.printStackTrace();
						  }
						  return 0;
						}catch (IOException e) {
						  e.printStackTrace();
					} catch (SAXException e) {
						e.printStackTrace();
					}
					long startT = System.currentTimeMillis();
					while(ENDFLAG){
						try {
							Thread.sleep(200);
						} catch (InterruptedException e) {
							ENDFLAG = false;
							e.printStackTrace();
							return 0;
						}catch(IllegalArgumentException e){
							Log.d("NLiveRoid","IllegalArgumentException at CommunityTab TopParseTask");
							e.printStackTrace();
							ENDFLAG = false;
							return 0;
						}
						if(System.currentTimeMillis()-startT>120000){
							//タイムアウト
							ENDFLAG = false;
							error.setErrorCode(-10);
							return 0;
						}
					}
					String[] str = pageStr.split("<<SPLIT>>");
					Log.d("NLiveRoid","SPLIT " + str[0]);
					try{
						pageCount = (Integer.parseInt(str[0])/30)+1;
					}catch(Exception e){
						e.printStackTrace();
						return -1;
					}
					ENDFLAG = true;
			}
			//ファイルに登録しておく
			try {
				Log.d("NLiveRoid","COLIST SIZE :" + coList.size());
				FileOutputStream fos = context.openFileOutput("alertL", context.MODE_WORLD_READABLE);
				String xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"+
					"<Alert xmlns=\"http://nliveroid-tutorial.appspot.com/education/\">\n";
				for(String i:coList){
					xml += "<id>"+i+"</id>\n";
					Log.d("NLiveRoid","ALERT:" + i);
				}
			    xml += "</Alert>\n";
			    fos.write(xml.getBytes());
			    fos.close();
			    BackGroundService.setAlertList(coList);
			    if(CommunityTab.getCommunityTab() != null){
			    	CommunityTab.getCommunityTab().setAlertList(coList);
			    }
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return 0;
		}
		@Override
		public void finishCallBack(ArrayList<LiveInfo> info) {}
		@Override
		public void finishCallBack(ArrayList<LiveInfo> info,
				LinkedHashMap<String, String> generate) {}
		@Override
		public void finishCallBack(ArrayList<LiveInfo> liveInfos, String pager) {
			//実際返ってくるのはここだけ
//			Log.d("NLvieRoid","FINISHCALL ALL");
			this.pageStr = pager;
			if(liveInfos != null){
				for(LiveInfo i:liveInfos){
//					Log.d("NLiveRoid","COS " + i.getCommunityID());
					coList.add(i.getCommunityID());
				}
			}
			ENDFLAG = false;
		}
		@Override
		protected void onPostExecute(Integer arg){
			if(dialog != null && dialog.isShowing())dialog.cancel();
			if(arg == -1){
				MyToast.customToastShow(context, "処理に失敗しました");
			}else if(error.getErrorCode() != 0){
				error.showErrorToast();
			}else if(arg == 0){
				MyToast.customToastShow(context, "読み込みが完了しました");
			}
		}
	 }

}
