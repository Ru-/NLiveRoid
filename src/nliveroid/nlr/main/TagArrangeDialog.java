package nliveroid.nlr.main;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import nliveroid.nlr.main.parser.TagInfoParser;
import nliveroid.nlr.main.parser.TagTokenParser;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.os.AsyncTask;
import android.text.TextUtils.TruncateAt;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;


public class TagArrangeDialog extends Dialog implements OnDismissListener{

	private TableLayout tagDispArea;

	private GetTagInfo getTagInfoTask;

	private EditText addEdit;
	private Context context;
	private TagArrangeDialog me;
	private String token;

	private ArrayList<TableRow> rowList;
	private ArrayList<String> addTagList;
	private ArrayList<String> deleteTagList;

	private boolean isCanceled;
	private int width;

	public TagArrangeDialog(final Context context,final String sessionid,final String lv,final int width) {
		super(context);
		me = this;
		this.width = width;
		this.context = context;
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setOnDismissListener(this);
		View parent = LayoutInflater.from(context).inflate(R.layout.tagarrange_dialog, null);
		setContentView(parent);

		rowList = new ArrayList<TableRow>();
		addTagList = new ArrayList<String>();
		deleteTagList = new ArrayList<String>();

		//タグ情報を取得しに行く
		getTagInfoTask = new GetTagInfo();
		getTagInfoTask.execute(sessionid,lv);
		tagDispArea = (TableLayout)parent.findViewById(R.id.tag_arrange_parent);
		addEdit = (EditText)parent.findViewById(R.id.tag_add_edit);
		addEdit.setWidth(width/2);
		//登録
		Button bt = (Button)parent.findViewById(R.id.tag_add_button);
		bt.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				if(getTagInfoTask.getStatus() != AsyncTask.Status.FINISHED){
					return;
				}
				//全く同じのは、登録できない
				for(int i = 0; i < rowList.size(); i++){
					if(addEdit.getText().toString().equals(((TextView)rowList.get(i).getChildAt(0)).getText().toString())){
						addEdit.setText("");
						return;
					}
				}
				final String addValue = addEdit.getText().toString();
				addTagList.add(addValue);
				//配信時ロック付き追加はまだいいか
				//UI生成部分
				TableRow tr = new TableRow(context);
				TextView tagText = new TextView(context);
				tagText.setIncludeFontPadding(true);
				tagText.setText(addValue);
				Button delete = new Button(context);
				delete.setText("削除");
				delete.setTag(addValue);
				delete.setOnClickListener(new View.OnClickListener(){
					@Override
					public void onClick(View v) {
						//追加予定にあったやつなら消す
						if(addTagList.contains(v.getTag())){
							addTagList.remove(v.getTag());
						}else{//追加予定になかったやつなら消す命令をSENDする(消すコミットになる)
							deleteTagList.add(addValue);
						}
						//ビューから消す
						for(int i = 0; i < rowList.size(); i++){
							if(((TextView)rowList.get(i).getChildAt(0)).getText().toString().equals(v.getTag())){
								((TableLayout)rowList.get(i).getParent()).removeViewAt(i);
								rowList.remove(i);
							}
						}
					}
				});

				addEdit.setText("");
				rowList.add(tr);
				tr.addView(tagText,-1,-2);
				tr.addView(delete,-1,-2);
				tagDispArea.addView(tr,-1,-2);
				tagText.setWidth(width/2);//長い名前を入力した時伸びていかないように対策
			}
		});

		//完了
		Button send = (Button)parent.findViewById(R.id.tag_send_button);
		send.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(token == null||token.equals("")){
					MyToast.customToastShow(context, "情報取得中です");
					return;
				}
				if(addTagList != null||deleteTagList != null){
				new SendTagArrange().execute(sessionid,lv);
				}
				me.dismiss();
			}
		});

	}

	public class GetTagInfo extends AsyncTask<String,Void,Integer>{

		private boolean ENDFLAG;
		private HashMap<String,Boolean> map = null;
		@Override
		protected Integer doInBackground(String... params) {

			//タグトークンを取得する
				try {
					String sessionid = params[0];

					//PC版ページにアクセス
					HttpURLConnection con = (HttpURLConnection)new URL(URLEnum.PC_WATCHBASEURL+params[1]).openConnection();
					con.setRequestProperty("Cookie", sessionid);
					con.setRequestMethod("GET");
					con.setInstanceFollowRedirects(false);
					con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.0; WOW64) AppleWebKit/534.24 (KHTML, like Gecko) Chrome/11.0.696.16 Safari/534.24");
					con.setRequestProperty("Content-Type",
							"application/x-www-form-urlencoded");
					Log.d("log","RESPONSE " + con.getResponseCode());
					InputStream source = con.getInputStream();
					//保存
						 try {
						  TagTokenParser handler = new TagTokenParser(this);
						  org.ccil.cowan.tagsoup.Parser parser = new org.ccil.cowan.tagsoup.Parser();
					        parser.setContentHandler(handler);
					        parser.parse(new InputSource(source));
					  } catch (org.xml.sax.SAXNotRecognizedException e) {
					      // Should not happen.
						  e.printStackTrace();
					  } catch (org.xml.sax.SAXNotSupportedException e) {
					      // Should not happen.
						  e.printStackTrace();
					  } catch(UnknownHostException e){//接続悪い時になる
						  e.printStackTrace();
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
							ENDFLAG = false;
							e.printStackTrace();
							return 1;
						}catch(IllegalArgumentException e1){
							e1.printStackTrace();
							Log.d("NLiveRoid","IllegalArgumentException at GetTagInfo");
							ENDFLAG = false;
							return 1;
						}
						if(System.currentTimeMillis()-startT>60000){
							//タイムアウト
							ENDFLAG = false;
							return -2;
						}
					}
					source.close();
					con.disconnect();
					ENDFLAG = true;
					if(token == null)return -1;
					//タグ編集画面から、ロック情報を取得
					//PC版ページにアクセス
					HttpURLConnection con1 = (HttpURLConnection)new URL(URLEnum.TAGEDIT+params[1]+"?token="+token).openConnection();
					con1.setRequestProperty("Cookie", sessionid);
					con1.setRequestMethod("POST");
					con1.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.0; WOW64) AppleWebKit/534.24 (KHTML, like Gecko) Chrome/11.0.696.16 Safari/534.24");
					con1.setInstanceFollowRedirects(false);
					con1.setRequestProperty("Content-Type",
							"application/x-www-form-urlencoded");
					Log.d("log","RESPONSE " + con1.getResponseCode());
					InputStream source1 = con1.getInputStream();
						 try {
							 TagInfoParser handler = new TagInfoParser(this);
						  org.ccil.cowan.tagsoup.Parser parser = new org.ccil.cowan.tagsoup.Parser();
					        parser.setContentHandler(handler);
					        parser.parse(new InputSource(source1));
					  } catch (org.xml.sax.SAXNotRecognizedException e) {
					      // Should not happen.
						  e.printStackTrace();
					  } catch (org.xml.sax.SAXNotSupportedException e) {
					      // Should not happen.
						  e.printStackTrace();
					  } catch(UnknownHostException e){//接続悪い時になる
						  e.printStackTrace();
						  return -3;
						}catch (IOException e) {
						  e.printStackTrace();
						  return -4;
					} catch (SAXException e) {
						e.printStackTrace();
						  return -4;
					}
						 con1.disconnect();
					long startT1 = System.currentTimeMillis();
					while(ENDFLAG){
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							ENDFLAG = false;
							e.printStackTrace();
							return 1;//キャンセル
						}catch(IllegalArgumentException e1){
							e1.printStackTrace();
							Log.d("NLiveRoid","IllegalArgumentException at GetTagInfo");
							ENDFLAG = false;
							return 1;
						}
						if(System.currentTimeMillis()-startT1>60000){
							//タイムアウト
							ENDFLAG = false;
							return -2;
						}
					}
				} catch (MalformedURLException e) {
					e.printStackTrace();
					return -4;
				} catch (IOException e) {
					e.printStackTrace();
					return -4;
				}

				return 0;
		}
		public void finishCallBack(String value) {
			token = value;
			ENDFLAG = false;
		}
		public void finishCallBack(HashMap<String,Boolean> value) {
			map = value;
			ENDFLAG = false;
		}
		public void finishErrorCallBack(){
			map = null;
			ENDFLAG = false;
		}
		@Override
		protected void onPostExecute(Integer arg){
//			Log.d("Log","onPostExecute ----- " + arg);
			if(arg == 0&&map != null&&!isCanceled){
				//レイアウトを再構成する
				tagDispArea.removeAllViews();
				Iterator<String> it = map.keySet().iterator();
				tagDispArea.removeAllViews();
				//取得したタグをリストビュー表示してrowListに突っ込む
				while(it.hasNext()){
					final String key = it.next().replace("\n|\t","");
					//UI生成部分
					TableRow tr = new TableRow(context);
					TextView tagText = new TextView(context);
					tagText.setIncludeFontPadding(true);
					tagText.setText(key);
					Button delete = new Button(context);
					delete.setText("削除");
					delete.setTag(key);
					if(map.get(key)){
						delete.setEnabled(false);
					}
					delete.setOnClickListener(new View.OnClickListener(){
						@Override
						public void onClick(View v) {
							//これは他人が書き込んだタグ(追加予定にはないはず)
							deleteTagList.add(key);
							//ビューから消す
							for(int i = 0; i < rowList.size(); i++){
								if(((TextView)rowList.get(i).getChildAt(0)).getText().toString().equals(v.getTag())){
									((TableLayout)rowList.get(i).getParent()).removeViewAt(i);
									rowList.remove(i);
								}
							}
						}
					});
					rowList.add(tr);
					tr.addView(tagText,-1,-2);
					tr.addView(delete,-1,-2);
					tagDispArea.addView(tr,-1,-2);
					tagText.setWidth(width/2);//長い名前を入力した時伸びていかないように対策
				}
			}else{
				switch(arg){
				case 1:
					//キャンセル
					break;
				case -1:
					MyToast.customToastShow(context, "チャンネル等のタグ編集非対応コミュでした\n(Token failed)");
					break;
				case -2:
					MyToast.customToastShow(context, "エラー\n接続タイムアウト");
					break;
				case -3:
					MyToast.customToastShow(context, "接続エラー");
					break;
				case -4:
					MyToast.customToastShow(context, "タグ情報取得エラー");
					break;
					default:
						if(!isCanceled){
						MyToast.customToastShow(context, "設定により編集できない");
						}
						break;
				}
				me.dismiss();
			}
		}


	}

	/**
	 * 編集中に画面回転
	 * @param scale
	 */
	public void onConfigChanged(int scale) {
		width = scale;
		if(addEdit != null){
			addEdit.setWidth(width/2);
		}
		if(tagDispArea != null){
			tagDispArea.setLayoutParams(new TableRow.LayoutParams(-1,-2));
			tagDispArea.requestLayout();
		}
		//テーブルのテキスト部分をレイアウトし直す
		TableRow tempRow = null;
		for(int i = 0; i < tagDispArea.getChildCount(); i++){
			tempRow = (TableRow) tagDispArea.getChildAt(i);
			for(int j = 0; j < tempRow.getChildCount(); j++){
		if(tempRow.getChildAt(j).getClass().getName().equals(TextView.class.getName())){
			((TextView)tempRow.getChildAt(j)).setWidth(width/2);
		}
			}

		}
	}


	//タグ編集コミットタスク
	class SendTagArrange extends AsyncTask<String,Void,Integer>{
		@Override
		protected Integer doInBackground(String... params) {
			HttpURLConnection con = null;
			try {
			for(int i = 0; i < addTagList.size(); i++){
					con = (HttpURLConnection) new URL(URLEnum.TAGEDIT+params[1]+"?add="+URLEncoder.encode(addTagList.get(i),"UTF-8")+"&token="+token).openConnection();
//					Log.d("Log","ADD TAG ---- " + addTagList.get(i));
					con.setRequestProperty("Cookie", params[0]);
					con.setRequestMethod("POST");
					con.setRequestProperty("Content-Type",
							"application/x-www-form-urlencoded");
					if(con.getResponseCode() != 200){
						return -1;
					}
			}

			for(int i = 0; i < deleteTagList.size() ; i++){
				con = (HttpURLConnection) new URL(URLEnum.TAGEDIT+params[1]+"?del="+URLEncoder.encode(deleteTagList.get(i),"UTF-8")+"&token="+token).openConnection();
//				Log.d("Log","DELETE TAG ---- " + deleteTagList.get(i));
				con.setRequestProperty("Cookie", params[0]);
				con.setRequestMethod("POST");
				con.setRequestProperty("Content-Type",
						"application/x-www-form-urlencoded");
				if(con.getResponseCode() != 200){
					return -1;
				}
			}

					con = (HttpURLConnection) new URL(String.format(URLEnum.TAGCOMMIT,params[1])).openConnection();
					con.setRequestProperty("Cookie", params[0]);
					con.setRequestMethod("POST");
					con.setRequestProperty("Content-Type",
							"application/x-www-form-urlencoded");
					if(con.getResponseCode() != 200){
						return -2;
					}


			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return 0;
		}
		@Override
		protected void onPostExecute(Integer arg){
			if(arg < 0){
				MyToast.customToastShow(context, "タグ編集に失敗\nコネクションエラー");
			}
		}

	}

	@Override
	public void onDismiss(DialogInterface arg0) {
		isCanceled = true;
	}

}
