package nliveroid.nlr.main;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;

import nliveroid.nlr.main.parser.TimeShiftParser;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TableRow;
import android.widget.TextView;

public class TimeShiftDialog extends AlertDialog.Builder{
	private AlertDialog me;
	private ErrorCode error;
	private ProgressBar progress;
	private ListView listview;
	private TSAdapter adapter;
	private LayoutInflater inflater;
	private TopTabs context;
	private int cbMinWidth = 50;
	private TextView tv;
//	private Button bt;
	private ArrayList<String[]> infos;
	private  HashMap<Integer, Boolean>  cbCheck;
	private boolean isDeleteMode;
	private View parent;
	public TimeShiftDialog(final TopTabs topTabs,final ErrorCode error) {
		super((Context)topTabs);
		inflater = LayoutInflater.from((Context)topTabs);
		parent = LayoutInflater.from((Context)topTabs).inflate(R.layout.timeshift_dialog, null);
		tv = (TextView)parent.findViewById(R.id.ts_tv);
//		bt = (Button)parent.findViewById(R.id.ts_delete);
//		bt.setOnClickListener(new OnClickListener(){
//			@Override
//			public void onClick(View v) {
//				if(isDeleteMode){
//					if(cbCheck.containsValue(true)){
//							new AlertDialog.Builder(topTabs)
//							.setMessage("チェックした放送を削除しますか?\n(元に戻せません)※まだ未実装です!!")
//							.setPositiveButton("YES", new DialogInterface.OnClickListener(){
//								@Override
//								public void onClick(DialogInterface arg0, int arg1) {
//									for(int i = 0; i < cbCheck.size(); i++){
//									if(cbCheck.get(i)){
//										Log.d("NLiveRoid","REMOVED" + adapter.getItem(i)[0]);
//										adapter.remove(adapter.getItem(i));
//										cbCheck.remove(i);
//										i--;
//									}
//									}
//									//一括削除コマンドを送信する
//									isDeleteMode = false;
//									bt.setText("削除");
//								}
//							})
//							.setNegativeButton("CANCEL",new DialogInterface.OnClickListener(){
//								@Override
//								public void onClick(DialogInterface arg0, int arg1) {}
//							}).create().show();
//					}else{//1つも選択していない場合は、削除モードを戻す
//							for(int i = 0; i< listview.getCount(); i++){
//								Log.d("NLiveRoid"," - " + listview.getChildAt(i));
//								if(listview.getChildAt(i)==null)break;
//								CheckBox cb = (CheckBox) listview.getChildAt(i).findViewById(R.id.list_ts_check);
//								if(cb != null){
//								cb.setVisibility(View.GONE);
//								cb.setChecked(false);
//								cbCheck.put(i, false);//チェックしている行番号を格納しておく
//								}
//							}
//							adapter.notifyDataSetChanged();
//							isDeleteMode = false;
//						bt.setText("削除");
//					}
//				}else{//削除モードじゃなかった場合は削除モードにする
//					if(cbCheck == null)cbCheck = new HashMap<Integer,Boolean>();
//					Log.d("NLiveRoid","REMOVEDX" + listview.getCount());
//						for(int i = 0; i< listview.getCount(); i++){
//							Log.d("NLiveRoid"," - " + listview.getChildAt(i));
//							if(listview.getChildAt(i)==null)break;
//							CheckBox cb = (CheckBox) listview.getChildAt(i).findViewById(R.id.list_ts_check);
//							if(cb != null){
//							cb.setVisibility(View.VISIBLE);
//							cb.setChecked(false);
//							cbCheck.put(i, false);//チェックしている行番号を格納しておく
//							}
//						}
//					bt.setText("削除する");
//					isDeleteMode = true;
//				}
//			}
//		});
		this.setCustomTitle(null);
		this.context = topTabs;
		this.error = error;
		int width = (int) (topTabs.getResources().getDisplayMetrics().widthPixels);
		cbMinWidth = width/8;
//		Log.d("NLiveRoid","MInWIDTH TS " + cbMinWidth);
		progress = (ProgressBar)parent.findViewById(R.id.ts_progress);
		listview = (ListView)parent.findViewById(android.R.id.list);
		listview.setLayoutParams(new TableRow.LayoutParams(width,-1));
		adapter = new TSAdapter((Context)topTabs);
		listview.setAdapter(adapter);
		listview.setFocusable(true);
		listview.setHorizontalScrollBarEnabled(true);
		listview.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				final String[] info = adapter.getItem(arg2);
						new AlertDialog.Builder((Context)context)
						.setItems(new CharSequence[]{"視聴","詳細","削除"}, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								switch(which){
								case 0:
									me.cancel();
									LiveInfo liveinfo = new LiveInfo();
									liveinfo.setLiveID(info[0]);
									((GatableTab)((NLiveRoid)topTabs.getApplicationContext()).getForeACT()).startFlashPlayer(liveinfo);
									break;
								case 1:
									me.cancel();//背面にきちゃうからしょうがない
									//ここのセッションは、一番元のログインの物であるはずなので、そのままRequest.getSessionIDでおｋなはず
									liveinfo = new LiveInfo();
									liveinfo.setLiveID(info[0]);
									liveinfo.setTitle(info[1]);
									((GatableTab)((NLiveRoid)topTabs.getApplicationContext()).getForeACT()).showGate(liveinfo);
									break;
								case 2:
									//1件削除コマンドを送信
									new AsyncTask<Void,Void,Integer>(){
										@Override
										protected Integer doInBackground(
												Void... params) {
											try {
												if(infos == null || infos.get(0)[2] == null)return -1;
												String url = URLEnum.MYPAGE + String.format("?delete=timeshift&vid=%s&confirm=%s",info[0].substring(2),infos.get(0)[2]);
												if(NLiveRoid.isDebugMode)Log.d("NLiveRoid","RESCODEX " + url);
												HttpURLConnection con = (HttpURLConnection)new URL(url).openConnection();
												con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.0; WOW64) AppleWebKit/534.24 (KHTML, like Gecko) Chrome/11.0.696.16 Safari/534.24");
												con.setRequestProperty("Cookie", Request.getSessionID(error));
												if(con.getResponseCode() == 302 || con.getResponseCode() == 200){
													Log.d("NLiveRoid","RESCODE" + con.getResponseCode());
													con.disconnect();
													return 0;
												}else{
													con.disconnect();
													return -2;
												}
											} catch (MalformedURLException e) {
												e.printStackTrace();
											} catch (IOException e) {
												e.printStackTrace();
											}catch(Exception e){
												e.printStackTrace();
											}
											return -3;
										}
										@Override
										protected void onPostExecute(Integer arg){
											if(arg == 0){
												MyToast.customToastShow(topTabs, "削除しました");
												adapter.clear();
												progress.setVisibility(View.VISIBLE);
												listview.setVisibility(View.GONE);
												tv.setVisibility(View.GONE);
												new TimeShiftTask().execute();
											}else{
												MyToast.customToastShow(topTabs, "削除でエラーが発生しました :" + arg);
											}
										}
									}.execute();
									break;
								}
							}
						})
						.setTitle(info[1]).create().show();
			}
		});
		setView(parent);
		new TimeShiftTask().execute();
	}
	public void showSelf(){
		me = this.create();
		me.show();
	}
//	@Override
//	public void onWindowFocusChanged(boolean hasFocus){//このメソッド呼びたいからBuilderじゃないAlertDialog継承にした
//		this.getWindow().getDecorView().setLayoutParams(new WindowManager.LayoutParams(getWindow().getDecorView().getWidth(),-1));
//	}

	public class TimeShiftTask extends AsyncTask<String,Void,Void>{
		private boolean ENDFLAG = true;
		@Override
		protected Void doInBackground(String... params) {
			//セッション取得
			String sessionid = Request.getSessionID(error);
			if(error.getErrorCode() != 0){
				return null;
			}
			 try {
					Log.d("NLiveRoid"," TSDSTART ----- ");
			HttpURLConnection con = (HttpURLConnection) new URL(URLEnum.MYPAGE).openConnection();
			con.setRequestProperty("Cookie", sessionid);
			con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.0; WOW64) AppleWebKit/534.24 (KHTML, like Gecko) Chrome/11.0.696.16 Safari/534.24");
			InputStream source = con.getInputStream();
			if(source == null){
				Log.d("NLiveRoid"," TSD Source was NULL ----- ");
				error.setErrorCode(-8);
				return null;
			}
				 TimeShiftParser handler = new TimeShiftParser(this);
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
			  } catch(UnknownHostException e){//接続悪い時になる
				  if(error != null ){
					  error.setErrorCode(-6);
					  e.printStackTrace();
				  }
				  return null;
				}catch (IOException e) {
				  e.printStackTrace();
			} catch (SAXException e) {
				e.printStackTrace();
			}
			long startT = System.currentTimeMillis();
			while(ENDFLAG){
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
					ENDFLAG = false;
					return null;
				}catch(IllegalArgumentException e){
					Log.d("NLiveRoid","IllegalArgumentException at TimeShiftDialog Tsk");
					e.printStackTrace();
					ENDFLAG = false;
					return null;
				}
				if(System.currentTimeMillis()-startT>30000){
					//タイムアウト
					ENDFLAG = false;
					error.setErrorCode(-10);
					return null;
				}
			}
			return null;
		}

		public void finishCallBack(ArrayList<String[]> infoz) {
			ENDFLAG = false;
			infos = infoz;
			Log.d("NLiveRoid","FC --- CALLED ");
//			for(int i = 0; infos != null && i <  infos.size(); i++){
//				for(int j = 0 ; j  < 3; j++)Log.d("NLiveRoid","FL --- " + infos.get(i)[j]);
//			}
		}
		@Override
		protected void onPostExecute(Void arg){
			if(error != null){
				if(error.getErrorCode() != 0){
					if(error.getErrorCode() == -8){
						if(me != null)me.cancel();
						MyToast.customToastShow((Context)context, "接続エラー");
						error.setErrorCode(0);
					}else{
					error.showErrorToast();
					}
				}else if(infos != null){
					if(tv != null){
						if(infos.get(0)[1] != null){
							String count = infos.get(0)[1];
							if(infos.get(0)[1].equals("?")){
								count = String.valueOf(10-(infos.size()-1));
							}
						tv.setText("あと"+count+"件ご利用になれます");
						tv.setVisibility(View.VISIBLE);
						}
					}
//					if(bt != null)bt.setVisibility(View.VISIBLE);
					for(int i = 1; i < infos.size(); i++){
					adapter.add(infos.get(i));
					}
					adapter.notifyDataSetInvalidated();
				}else{
					MyToast.customToastShow((Context)context, "接続エラー:" + error.getErrorCode());
					me.cancel();
				}
			}
			progress.setVisibility(View.GONE);
			listview.setVisibility(View.VISIBLE);
		}
	}

	final class TSAdapter extends ArrayAdapter<String[]>{
		public TSAdapter(Context context) {
			super(context, R.layout.list_ts);
		}
		@Override
		public View getView(int position, View paramView, ViewGroup paramViewGroup){
			ViewHolder holder;
			View view = paramView;
			//nullの時だけ処理を行うと、更新してaddされた部分に情報が使いまわされてしまうので毎回ビューの情報を更新する
			if(view == null){
				view = inflater.inflate(R.layout.list_ts, null);
				CheckBox cb = (CheckBox)view.findViewById(R.id.list_ts_check);
				cb.setMinWidth(cbMinWidth);
				final int pos = position;
				cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
					@Override
					public void onCheckedChanged(CompoundButton compoundbutton,
							boolean flag) {
						if(cbCheck == null)cbCheck = new HashMap<Integer,Boolean>();
						cbCheck.put(pos, flag);
						//リスナが自動的に呼ばれるのでcbCheckのデータはここでは変えない
					}
				});
				TextView title = (TextView)view.findViewById(R.id.list_ts_title);
				TextView status = (TextView)view.findViewById(R.id.list_ts_status);
				holder = new ViewHolder();
				holder.cb = cb;
				holder.title = title;
				holder.status = status;
				view.setTag(holder);
			}else{
				holder = (ViewHolder)view.getTag();
			}

			String[] info = getItem(position);
				holder.title.setText(info[1]);
				holder.status.setText(info[2]);
				if(info[2].matches(".*予約中.*")){
					view.setBackgroundColor(Color.parseColor("#66e1e1"));//青
				}else if(info[2].matches(".*利用期間は終了しました.*|.*期限が切れています.*|.*中止されました.*")){
					view.setBackgroundColor(Color.parseColor("#9c9c9c"));//グレー
				}else if(info[2].matches(".*何度でも.*|.*視聴期限未定.*|.*まで].*")){
					view.setBackgroundColor(Color.parseColor("#ff0000"));//赤
				}
			return view;
		}
		private class ViewHolder {
		    CheckBox cb;
		    TextView title;
		    TextView status;
		}
	}
	public void onConfigChanged(Context topTabs) {
		int width = (int) (topTabs.getResources().getDisplayMetrics().widthPixels);
		cbMinWidth = width/8;
//		Log.d("NLiveRoid","CONFIGAAMInWIDTH TS " + cbMinWidth);
		listview.setLayoutParams(new TableRow.LayoutParams(width,-1));
	}

	public boolean isShowing() {
		return me.isShowing();
	}


}
