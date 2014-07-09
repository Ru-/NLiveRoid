package nliveroid.nlr.main;

import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.util.ArrayList;

import nliveroid.nlr.main.parser.JikkyouParser;
import nliveroid.nlr.main.parser.LiveArchiveParser;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

public class JIkkyouDialog extends AlertDialog.Builder{
	private AlertDialog me;
	private ErrorCode error;
	private ProgressBar progress;
	private ListView listview;
	private JikkyouAdapter adapter;
	private LayoutInflater inflater;
	private SearchTab context;
	private int width;
	public JIkkyouDialog(SearchTab act,final int width,ErrorCode error) {
		super((Context)act);
		Log.d("NLiveRoid","Jikkyou -----"+ width);
		inflater = LayoutInflater.from((Context)act);
		View parent = LayoutInflater.from((Context)act).inflate(R.layout.jikkyou_dialog, null);
		setCustomTitle(null);
		this.context = act;
		this.error = error;
		this.width = width;
		progress = (ProgressBar)parent.findViewById(R.id.archives_progress);
		listview = (ListView)parent.findViewById(android.R.id.list);
		adapter = new JikkyouAdapter((Context)act);
		listview.setAdapter(adapter);
		listview.setFocusable(true);
		listview.setHorizontalScrollBarEnabled(true);
		listview.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				final String[] info = adapter.getItem(arg2);
//				この時タイトルがinfo[3]で詳細がinfo[5]だけどタイトルは使ってない
					if(info[4] != null && info[4].equals("1") && info[2] != null){
						final AlertDialog.Builder tsSimple = new AlertDialog.Builder((Context)context);
						ScrollView sv = new ScrollView((Context)context);
						tsSimple.setMessage("視聴しますか?");
						tsSimple.setPositiveButton("視聴",new DialogInterface.OnClickListener(){
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								me.cancel();
								LiveInfo li = new LiveInfo();
								li.setLiveID(info[2]);
								context.startFlashPlayer(li);
							}
						});
						tsSimple.setNegativeButton("キャンセル", new DialogInterface.OnClickListener(){
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.cancel();
							}
						});
						tsSimple.setView(sv).create().show();
					}
			}
		});
		setView(parent);
		new JikkyouTask().execute();
	}
	public void showSelf(){
		me = this.create();
		me.show();
	}
//	@Override
//	public void onWindowFocusChanged(boolean hasFocus){//このメソッド呼びたいからBuilderじゃないAlertDialog継承にした
//		this.getWindow().getDecorView().setLayoutParams(new WindowManager.LayoutParams(getWindow().getDecorView().getWidth(),-1));
//	}

	public class JikkyouTask extends AsyncTask<Void,Void,Void>{
		private boolean ENDFLAG = true;
		private ArrayList<String[]> infos;
		@Override
		protected Void doInBackground(Void... params) {
			//セッション取得
			String sessionid = Request.getSessionID(error);
			if(error.getErrorCode() != 0){
				return null;
			}
			//トップのソース取得後パース
			InputStream source = Request.doGetToInputStreamFromFixedSession(sessionid, String.format(URLEnum.JIKKYOU), error);
			if(source == null){
				Log.d("NLiveRoid"," Source was NULL ----- ");
				error.setErrorCode(-8);
				return null;
			}
			 try {
				 JikkyouParser handler = new JikkyouParser(this);
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
					Log.d("NLiveRoid","IllegalArgumentException at CommunityTab TopParseTask");
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

		public void finishCallBack(ArrayList<String[]> archives) {
			ENDFLAG = false;
			infos = archives;
//			for(int i = 0; i < archives.size(); i++){
//				for(int j = 0 ; j < archives.get(i).length; j++){
//				Log.d(" R " + i, " " + archives.get(i)[j]);
//				}
//			}
		}
		@Override
		protected void onPostExecute(Void arg){
			if(error != null){
				if(error.getErrorCode() != 0){
					if(error.getErrorCode() == -8){
						if(me != null)me.cancel();
						MyToast.customToastShow((Context)context, "接続エラー\n又は放送履歴が公開されていない");
						error.setErrorCode(0);
					}else{
					error.showErrorToast();
					}
				}else{
					for(int i = 0; i < infos.size(); i++){
					adapter.add(infos.get(i));
					}
				}
			}else{
				MyToast.customToastShow((Context)context, "放送履歴の取得に失敗 : " + error.getErrorCode());
			}
			progress.setVisibility(View.GONE);
			listview.setVisibility(View.VISIBLE);
		}

	}
	final class JikkyouAdapter extends ArrayAdapter<String[]>{
		public JikkyouAdapter(Context context) {
			super(context, R.layout.list_jikkyou);
		}
		@Override
		public View getView(int position, View paramView, ViewGroup paramViewGroup){
			ViewHolder holder;
			View view = paramView;
			//nullの時だけ処理を行うと、更新してaddされた部分に情報が使いまわされてしまうので毎回ビューの情報を更新する
			if(view == null){
				view = inflater.inflate(R.layout.list_livearchive, null);
				TextView date = (TextView)view.findViewById(R.id.archive_date);
				TextView owner = (TextView)view.findViewById(R.id.archive_owner);
				TextView title = (TextView)view.findViewById(R.id.archive_title);
				title.setWidth(width);
				TextView desc = (TextView)view.findViewById(R.id.archive_desc);
				holder = new ViewHolder();
				holder.date = date;
				holder.owner = owner;
				holder.title = title;
				holder.desc = desc;
				view.setTag(holder);
			}else{
				holder = (ViewHolder)view.getTag();
			}

			String[] info = getItem(position);
				holder.date.setText(info[0]);
				holder.owner.setText(info[1]);
				holder.title.setText(info[3]);
				holder.desc.setText(info[5]);
				if(info[4] != null && info[4].equals("1")){
					view.setBackgroundColor(Color.parseColor("#ffee00"));
				}else{
					view.setBackgroundColor(Color.parseColor("#b9b7b9"));
				}
			return view;
		}
		private class ViewHolder {
		    TextView date;
		    TextView owner;
		    TextView title;
		    TextView desc;
		    Button bt;
		}
	}


}
