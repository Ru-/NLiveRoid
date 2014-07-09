package nliveroid.nlr.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.IllegalFormatConversionException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nliveroid.nlr.main.parser.CategoryParser;
import nliveroid.nlr.main.parser.ChannelParser;
import nliveroid.nlr.main.parser.KeyWordParser;
import nliveroid.nlr.main.parser.NsenParser;
import nliveroid.nlr.main.parser.RankingClosedParser;
import nliveroid.nlr.main.parser.RankingParser;
import nliveroid.nlr.main.parser.RecentParser;
import nliveroid.nlr.main.parser.SearchTagParser;
import nliveroid.nlr.main.parser.TimeTableParser;

import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class SearchTab extends Activity implements Archiver,GatableTab {

	private static SearchTab ACT;
	private LayoutInflater inflater;
	private View parent;
	private SearchArrayAdapter adapter;
	private ListView listview;
	private View footer;

	private static SearchTask searchTask;
	private static RecentLiveTask recentTask;

	private ErrorCode error;

	private TextView kupaText;
	private static ViewGroup progressArea;
	private static ProgressBar progressBar;

	private EditText editTex;
	private Button categoryButton;
	private int categoryIndex;
	private Button sortButton;
	private int sortIndex;
	private Button modeButton;
	private int liveModeIndex;
	private Button generateButton;
	private int generateIndex;
	private Button switchButton;

	private String requestURL;//普通にリクエストにいく場合の基本URL
	private String tagURL;//抽出タグ用のURL
	private static int pageNum = -1;
	private String[] generateList;
	private String[] timetableHref;
	private String[] sortList;
	private Gate gate;

	private static boolean isFirstThread = true;
	private static boolean isListTaped = false;
	private boolean isPCSearch = false;

	//スタティックだとアプリ終了して起動すると、前のモードになってるから駄目
	//0カテゴリ、1カテゴリのニコ電又はiPhoneとキーワード・タグ  2はPC版
	private int searchMode = 0;

	private float linearPos;// 移動LinearLayoutのY位置
	private boolean isUped;
	private byte toptab_tcolor = 0;
	private BitmapDrawable back_t;
	private AlertDialog accountDialog;


	private ViewGroup layer2;
	private TableRow kupaLinearParent;
	private LinearLayout kupaLinear;
	private LinearLayout listParent;
	private LinearLayout progressareaParent;
	private String[] modeList;
	private String generateCloseValue;

	private ArrayList<String> filters = new ArrayList<String>();
	private AlertDialog filterAlert;
	private LinearLayout searchRootLinear;
	private FrameLayout rootView;
	private WebView sWv;

	private LinearLayout searchHisLL;

	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		ACT = this;
		requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		inflater = LayoutInflater.from(this);
		parent = inflater.inflate(R.layout.searchtab, null);
		final NLiveRoid app = (NLiveRoid) getApplicationContext();
		error = app.getError();
		final View browse_header = inflater.inflate(R.layout.browse_header, null);

		searchRootLinear = (LinearLayout)parent.findViewById(R.id.search_root_linear);
		layer2 = (FrameLayout)parent.findViewById(R.id.layer2);
		rootView = (FrameLayout)parent.findViewById(R.id.search_root);
		TextView headerTxt = (TextView) parent.findViewById(R.id.search_titletext);

		// 何故か定義しておいて1度消して後から検索に行く際にセットするときちんとした見た目になる
		progressArea = (ViewGroup) parent.findViewById(R.id.progress_area);
		progressBar = (ProgressBar) parent
				.findViewById(R.id.ProgressBarHorizontal);
		progressArea.removeView(progressBar);
		final LayoutInflater inflater = LayoutInflater.from(this);
		View pParent = inflater.inflate(R.layout.progressbar, null);
		progressBar = (ProgressBar) pParent
				.findViewById(R.id.ProgressBarHorizontal);
		progressBar.setMax(100); // プログレスバーの最大値を設定

		//このLinearLayout以下をアニメーションする
		//テキストのある場所の2つ上
		kupaLinear = (LinearLayout) parent.findViewById(R.id.header_parent0);
		kupaLinear.setOnTouchListener(new DrawerTouchListener());
		kupaLinearParent = (TableRow)kupaLinear.getParent();
		kupaText = (TextView) parent.findViewById(R.id.search_text_label);


		//PC検索用のWV初期化
		sWv = new WebView(this);
		sWv.setLayoutParams(new LinearLayout.LayoutParams(-1,-1));
		WebSettings settings = sWv.getSettings();
		try{
		settings.setJavaScriptEnabled(true);
		}catch(Exception e){
			Log.d("NLiveRoid","Search onCreate WV");
			e.printStackTrace();
			MyToast.customToastShow(ACT, "タブ画面を正しく初期化できませんでした\nJB以上専用アプリ等の影響が考えられます");
		}
		settings.setPluginsEnabled(true);
//		sWv .getSettings().setLoadWithOverviewMode(true);これができない為
		settings.setSupportZoom(true);
		settings.setDatabaseEnabled(false);//これしないと視聴へ飛んだ後、プレイヤーのWVでエラーする
		settings.setBuiltInZoomControls(true);
		sWv.setVerticalScrollbarOverlay(true);
		sWv.setWebViewClient(new PCSearchWVClient(this,sWv,(ProgressBar) browse_header.findViewById(R.id.browseheader_progress)));

		CookieSyncManager coo = null;
		try {
			coo = CookieSyncManager.getInstance();
			if (coo == null)
				CookieSyncManager.createInstance(ACT);
		} catch (IllegalStateException e) {// エラーレポートに何故かあった
			e.printStackTrace();
		}
		coo.startSync();
		CookieManager cooMan = CookieManager.getInstance();
		//ここでセッションを取得するから、アカウントの設定で、errorCodeをセットし直す必要がある
		cooMan.setCookie(
				"nicovideo.jp",
				Request.getSessionID(error)
						+ "; expires=Sun, 18-Mar-2015 14:25:29 GMT; path=/; domain=.nicovideo.jp");
		settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
		settings.setUserAgentString("Mozilla/5.0 (Windows NT 6.0; WOW64) AppleWebKit/534.24 (KHTML, like Gecko) Chrome/11.0.696.16 Safari/534.24");
		final LinearLayout header = (LinearLayout)parent.findViewById(R.id.search_titlebar);
		header.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				//レイアウトの変更 PC版を表示する
				rootView.removeAllViews();//ここでsearchRootLinearとlayer2と両方切り離さないとlayer2が挟まって固まる
				rootView.addView(sWv,new FrameLayout.LayoutParams(-1,-1));
				rootView.addView(browse_header);
				sWv.loadUrl(URLEnum.PC_SEARCH);
				isPCSearch = true;
			}
		});
		isPCSearch = getIntent().getBooleanExtra("sole", false);
		final Button browse_change = (Button)browse_header.findViewById(R.id.browseheader_return_bt);
		browse_change.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				//PC版から通常に戻す
				sWv.stopLoading();
				rootView.removeAllViews();
				System.gc();
						rootView.addView(searchRootLinear,new FrameLayout.LayoutParams(-1,-1));
						rootView.addView(layer2,new FrameLayout.LayoutParams(-1,-1));
						isPCSearch = false;
						if(getIntent().getBooleanExtra("sole", false)){
					    	//レイアウトをやり直す
							getIntent().putExtra("sole", false);
							NLiveRoid app = (NLiveRoid) getApplicationContext();
							int width = app.getViewWidth()/2;
							sortButton.setLayoutParams(new TableRow.LayoutParams(width,browse_change.getHeight()));
							generateButton.setLayoutParams(new TableRow.LayoutParams(width,browse_change.getHeight()));
							modeButton.setLayoutParams(new TableRow.LayoutParams(width,browse_change.getHeight()));
							editTex.setLayoutParams(new TableRow.LayoutParams(width,browse_change.getHeight()));
							onReload();
						}
			}
		});
		Button browse_goback = (Button)browse_header.findViewById(R.id.browseheader_goback_bt);
		browse_goback.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				sWv.goBack();
			}
		});
		Button pc_top = (Button)browse_header.findViewById(R.id.browseheader_pc_top);
		pc_top.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				if(sWv != null)sWv.loadUrl(URLEnum.PC_TOP);
			}
		});
//		pc_auth.setOnClickListener(new OnClickListener(){
//			@Override
//			public void onClick(View v) {
//				//PC版のセッションに変える キャンセルの類は、とりあえずいいか。。。
//				new AsyncTask<Void,Void,Void>(){
//					@Override
//					protected Void doInBackground(Void... params) {
//						try{
//						URL url = new URL(URLEnum.LOGINURL);
//						HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
//						con.setRequestMethod("POST");
//						con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.0; WOW64) AppleWebKit/534.24 (KHTML, like Gecko) Chrome/11.0.696.16 Safari/534.24");
//						con.setDoOutput(true);
//						con.setInstanceFollowRedirects(true);
//
//						final String cParam = String.format("mail=%s&password=%s",
//								app.getUserIDFromMap(), app.getPasswordFromMap());
//						// フォーム、コンテントレングス、ユーザーエージェントは無くても通常のセッションIDは取得できる
//
//						// ここが取れない場合、ここの前にgetInputStream()(getContentLength)とか
//						// この前にやってるとできない、もしくはUnknownHostとかはエミュの再起動で直る
//						PrintStream out = new PrintStream(con.getOutputStream());
//						out.print(cParam);
//						Map<String,List<String>> list = con.getHeaderFields();
//						List<String> cookie_list = list.get("set-cookie");
//						Pattern user_session = Pattern.compile("user_session=user_session_[0-9]+_([0-9]|[a-z])+");
//						String tmp = null;
//						for(int i = 0; i < cookie_list.size(); i++){
//							Log.d("NLiveRoid"," TEST " + cookie_list.get(i));
//							if(user_session.matcher(cookie_list.get(i)).find()){
//								tmp = cookie_list.get(i);
//								break;
//							}
//						}
//						InputStream is = con.getInputStream();
//						BufferedReader br = new BufferedReader(new InputStreamReader(is));
//						String temp = "";
//						String source = "";
//						for(;(temp = br.readLine()) != null;){
//							source += temp;
//						}
//						Log.d("NLiveRoid","SOURCE " + source);
//						if (tmp == null) {
//							error.setErrorCode(-1);
//							Log.d("NLiveRoid", "FAILED LOGIN");
//							return null;
//						}else{
//							app.setSessionid(tmp);
//							CookieManager.getInstance().setCookie("nicovideo.jp", tmp);
//						}
//						con.disconnect();
//					}catch (NullPointerException e){//OutputStreamを開けなくなる事がある(原因不明)
//						error.setErrorCode(-6);
//						e.printStackTrace();
//					} catch (UnknownHostException e) {
//						error.setErrorCode(-3);
//						e.printStackTrace();
//					} catch (MalformedURLException e) {
//						e.printStackTrace();
//					} catch (IOException e) {
//						error.setErrorCode(-6);
//						e.printStackTrace();
//					}
//						return null;
//					}
//					@Override
//					protected void onPostExecute(Void arg){
//						if(error.getErrorCode() == 0){
//							MyToast.customToastShow(ACT, "PC版で認証しました\nPCはログアウトされます");
//						}else{
//							error.showErrorToast();
//						}
//					}
//				}.execute();
//			}
//		});
		toptab_tcolor  = app.getDetailsMapValue("toptab_tcolor") == null? 0:Byte.parseByte(app.getDetailsMapValue("toptab_tcolor"));
		setTextColor(headerTxt,toptab_tcolor);
		try{
			FileInputStream back_t_file  = openFileInput("back_t");
			Bitmap back = BitmapFactory.decodeStream(back_t_file);
			back_t = new BitmapDrawable(back);
			parent.setBackgroundDrawable(back_t);
		} catch (FileNotFoundException e) {
			parent.findViewById(R.id.search_titlebar).setBackgroundDrawable(getResources().getDrawable(R.drawable.header));
//			e.printStackTrace();
		}catch(OutOfMemoryError e){
			e.printStackTrace();
			MyToast.customToastShow(this, "背景画像が大きすぎたため、適用に失敗しました");
		}catch(Exception e){
			e.printStackTrace();
			MyToast.customToastShow(this, "背景画像適用時エラー");
		}



		filters.add("official");
		filters.add("channel");
		filters.add("community");

		switchButton = (Button)parent.findViewById(R.id.search_switch_bt);
//			 switch(Integer.parseInt(app.getDetailsMapValue("last_kind"))){}//0,1
//			 switchIndex = app.getDetailsMapValue("last_kind") == null? (byte)0:Byte.parseByte(app.getDetailsMapValue("last_kind"));
			 final String[] switchValues = new String[]{"トップ","ランキング","番組表","チャンネル","PC","Nsen"};//実況は結構めんどくさい
			 switchButton.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View arg0) {
					new AlertDialog.Builder(ACT)
					.setItems(switchValues, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
			                imm.hideSoftInputFromWindow(editTex.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
							switch(which){
							case 0://トップ
								sortIndex = 0;
								pageNum = 1;
								searchMode = 0;
								CommunityTab.cancelMovingTask();//一旦全てキャンセル
								LiveTab.cancelMovingTask();
								SearchTab.cancelMoveingTask();
								categoryIndex = 0;
								categoryButton.setText("カテゴリ検索▼");
								categoryIndex = 0;
								sortButton.setText("適合率の高い順");
								generateList = new String[0];
								generateIndex = 0;
								generateButton.setText("関連タグ");
								modeButton.setText("放送中");
								modeList[0] = "放送中";
								modeList[1] = "未来の放送";
								modeList[2] = "過去の放送";
								liveModeIndex = 0;
								switchButton.setText("SP");
								timetableHref = new String[0];//戻しておかないと番組表に戻ったときにgenerateが出ない
								addProgress();
								recentTask = new RecentLiveTask();//公式
								recentTask.execute();
								break;
							case 1:
								//ランキング検索する
								sortIndex = 0;
								editTex.setText("");
								adapter.clear();
								categoryIndex = 0;
								categoryButton.setText("カテゴリ検索▼");
								searchMode = 2;
								footer.setVisibility(View.GONE);
								CommunityTab.cancelMovingTask();//一旦全てキャンセル
								LiveTab.cancelMovingTask();
								SearchTab.cancelMoveingTask();
								generateList = new String[0];
								generateIndex = 0;
								generateButton.setText("");
								modeButton.setText("ユーザー生放送");
								modeList[0] = "ユーザー生放送";
								modeList[1] = "公式生放送";
								modeList[2] = "ch生放送";
								liveModeIndex = 0;
								timetableHref = new String[0];//戻しておかないと番組表に戻ったときにgenerateが出ない
								selectSortAdapter(3);
								sortButton.setText(sortList[0]);
								createRankingURL();
								try{
									searchTask = new SearchTask(requestURL.replaceAll("<<PAGEXXX>>", String.valueOf(pageNum)));
									searchTask.execute();
									switchButton.setText("ランキング");
									}catch(IllegalFormatConversionException e){
										MyToast.customToastShow(ACT, "検索できませんでした\nアプリ再起又は一定時間待ってからお試しください:code 0");
									}
								break;
							case 2://番組表検索する
								sortIndex = 0;
								adapter.clear();
								pageNum = 0;//0からじゃないと正しいクエリにならない
								categoryIndex = 0;
								categoryButton.setText("カテゴリ検索▼");
								searchMode = 3;
								footer.setVisibility(View.GONE);
								CommunityTab.cancelMovingTask();//一旦全てキャンセル
								LiveTab.cancelMovingTask();
								SearchTab.cancelMoveingTask();
								generateList = new String[0];
								generateIndex = -1;//最初に日付選んだ後と、日付入ってきてまだ選んでない状態とはスクロール時の次のページ動作に差異がある
								generateButton.setText("");
								modeButton.setText("");
								modeList[0] = "放送中";
								modeList[1] = "未来の放送";
								modeList[2] = "過去の放送";
								liveModeIndex = 0;
								timetableHref = new String[0];//戻しておかないと番組表に戻ったときにgenerateが出ない
								createTimeTableURL();
								sortButton.setText("");
								try{
									searchTask = new SearchTask(requestURL.replaceAll("<<PAGEXXX>>", String.valueOf(pageNum)));
									searchTask.execute();
									switchButton.setText("番組表");
									}catch(IllegalFormatConversionException e){
										MyToast.customToastShow(ACT, "検索できませんでした\nアプリ再起又は一定時間待ってからお試しください:code 0");
									}
								break;
							case 3://チャンネル
								pageNum = 0;
								sortIndex = 0;
								searchMode = 4;
								categoryIndex = 0;
								generateList = new String[0];
								generateIndex = 0;
								generateButton.setText("");
								sortButton.setText("");
								liveModeIndex = 0;
								switchButton.setText("チャンネル");
								timetableHref = new String[0];//戻しておかないと番組表に戻ったときにgenerateが出ない
								modeButton.setText("");
								categoryButton.setText("カテゴリ検索▼");
								footer.setVisibility(View.GONE);
								CommunityTab.cancelMovingTask();//一旦全てキャンセル
								LiveTab.cancelMovingTask();
								SearchTab.cancelMoveingTask();
								try{
									searchTask = new SearchTask(URLEnum.CHANNELURL);
									searchTask.execute();
									}catch(IllegalFormatConversionException e){
										MyToast.customToastShow(ACT, "エラーが発生しました\nアプリ再起又は一定時間待ってからお試しください:code nsen");
									}
								break;
							case 4://PC版に切替える
								//レイアウトの変更
								sortIndex = 0;
								rootView.removeAllViews();
								rootView.addView(sWv,new FrameLayout.LayoutParams(-1,-1));
								rootView.addView(browse_header);
								sWv.loadUrl(URLEnum.PC_SEARCH);
								isPCSearch = true;
								break;
							case 5://Nsen
								new AlertDialog.Builder(ACT)
								.setItems(new CharSequence[]{"VOCALOID","東方","ニコニコインディーズ","歌ってみた","演奏してみた","PV","蛍の光","オールジャンル"}, new DialogInterface.OnClickListener(){
									@Override
									public void onClick(DialogInterface arg0, int arg1) {
										String extension = "";
										switch(arg1){
										case 0:
											extension = "vocaloid";
											break;
										case 1:
											extension = "toho";
											break;
										case 2:
											extension = "nicoindies";
											break;
										case 3:
											extension = "sing";
											break;
										case 4:
											extension = "play";
											break;
										case 5:
											extension = "pv";
											break;
										case 6:
											extension = "hotaru";
											break;
										case 7:
											extension = "allgenre";
											break;
										}
										sortIndex = 0;
										searchMode = 5;
										footer.setVisibility(View.GONE);
										CommunityTab.cancelMovingTask();//一旦全てキャンセル
										LiveTab.cancelMovingTask();
										SearchTab.cancelMoveingTask();
										generateList = new String[0];
										try{
											searchTask = new SearchTask(URLEnum.NSENURL + extension);
											searchTask.execute();
											}catch(IllegalFormatConversionException e){
												MyToast.customToastShow(ACT, "エラーが発生しました\nアプリ再起又は一定時間待ってからお試しください:code nsen");
											}
									}
								}).create().show();
								break;
							case 6://実況
								new AlertDialog.Builder(ACT)
								.setItems(new CharSequence[]{"テレビ","ラジオ","BS","勢い","過去ログ(ブラウザ)"}, new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										String[] items = null;
										switch(which){
										case 0:
											new AlertDialog.Builder(ACT)
											.setItems(new CharSequence[]{"NHK総合","Eテレ","日本テレビ","テレビ朝日","TBSテレビ","テレビ東京","フジテレビ","TOKYO MX","テレ玉","tvk","チバテレビ"}, new DialogInterface.OnClickListener() {
												@Override
												public void onClick(DialogInterface dialog, int which) {
													String extension = "";
													if(which <= 1){
														extension = "jk"+(which+1);
													}else{
														extension = "jk"+(which+2);
													}
													LiveInfo info = new LiveInfo();
													info.setLiveID(URLEnum.JIKKYOU + extension);
													Log.d("NliveRoid","URL--- " + URLEnum.JIKKYOU + extension);
													startFlashPlayer(info);
												}
											})
											.create().show();
											break;
										case 1://ラジオ
											new AlertDialog.Builder(ACT)
											.setItems(new CharSequence[]{"NHKラジオ第1","NHKラジオ第2","NHK-FM","AIR-G'","","HBCラジオ","STVラジオ","ラジオNIKKEI第一放送","Inter FM","TOKYO FMk","J-WAVE","TBSラジオ","文化放送","茨城放送","ニッポン放送","","","","","","","","","","","","","","","","","","","","",""}, new DialogInterface.OnClickListener() {
												@Override
												public void onClick(DialogInterface dialog, int which) {
													String extension = "";
													if(which <= 1){
														extension = "jk"+(which+1);
													}else{
														extension = "jk"+(which+2);
													}
													LiveInfo info = new LiveInfo();
													info.setLiveID(URLEnum.JIKKYOU + extension);
													startFlashPlayer(info);
												}
											})
											.create().show();
											break;
										case 2:

											break;
										case 3://アクティブコメント表示
											new JIkkyouDialog(ACT,parent.getWidth(), error).showSelf();
											return;
										case 4://ブラウザで過去ログ
											Intent i = new Intent(Intent.ACTION_VIEW);
											i.addCategory(Intent.CATEGORY_BROWSABLE);
											i.setDataAndType(Uri.parse(URLEnum.JIKKYOU + "rankings"), "text/html");
											ACT.startActivityForResult(i,CODE.RESULT_REDIRECT);//リダイレクトコード
											return;
										}
									}
								})
								.create().show();
								break;

							}
						}
					}).create().show();
				}
			 });

		// Spinnerの設定
		 categoryButton = (Button) parent.findViewById(R.id.search_commontag_spinner);
		final String[] categoryList = new String[9];
		categoryList[0] = "トップ";//通常検索で戻した時に表示する
		categoryList[1] = "一般";
		categoryList[2] = "やってみた";
		categoryList[3] = "ゲーム";
		categoryList[4] = "動画紹介";
		categoryList[5] = "顔出し";
		categoryList[6] = "凸待ち";
		categoryList[7] = "ニコ電";
		categoryList[8] = "iPhone";
		categoryIndex = 0;//毎回0の「公式」に設定
		//タグスピナーのイベントの登録
		categoryButton.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View view) {
						editTex.clearFocus();
						new AlertDialog.Builder(ACT)
						.setTitle(categoryList[categoryIndex])
						.setItems(categoryList,
								new DialogInterface.OnClickListener() {
									public void onClick(
											DialogInterface dialog,
											int which) {
										if(editTex != null){
											 // ソフトキーボードを非表示にする
							                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
							                imm.hideSoftInputFromWindow(editTex.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
											}
										categoryIndex = which;
										categoryButton.setText(categoryList[categoryIndex]);
										searchMode = 0;
										generateButton.setText("関連タグ");
										generateList = new String[0];
										timetableHref = new String[0];//戻しておかないと番組表に戻ったときにgenerateが出ない
										//起動した時にここが呼ばれてしまうのでpageNumで一度検索したかどうかを聞いて戻したかどうかを判断
										if(pageNum > 0 && categoryIndex == 0 ){//recentの公式放送
														cancelMoveingTask();//一旦全てキャンセル
														LiveTab.cancelMovingTask();
														SearchTab.cancelMoveingTask();
														sortIndex = 0;
														selectSortAdapter(1);//ソートをキーワード用にする
														sortButton.setText(sortList[0]);
														addProgress();
														recentTask = new RecentLiveTask();//公式
														recentTask.execute();
													}else if(categoryIndex > 0){
														//カテゴリ検索する
														pageNum = 1;
														modeButton.setText("放送中");
														modeList[0] = "放送中";
														modeList[1] = "未来の放送";
														modeList[2] = "過去の放送";
														liveModeIndex = 0;
														sortIndex = 0;
														selectSortAdapter(0);//ソートをカテゴリ用に戻す
														sortButton.setText(sortList[0]);
														switchButton.setText("SP");
														//一旦他のタブの実行中タスクを全てキャンセル
														CommunityTab.cancelMovingTask();//一旦全てキャンセル
														LiveTab.cancelMovingTask();
														SearchTab.cancelMoveingTask();
													createCategoryURL();
													try{
													searchTask = new SearchTask(requestURL.replaceAll("<<PAGEXXX>>", String.valueOf(pageNum)));
													searchTask.execute();
													}catch(IllegalFormatConversionException e){
														MyToast.customToastShow(ACT, "検索できませんでした\nアプリ再起又は一定時間待ってからお試しください:code 0");
													}
													}
									}
								}).show();

					}
				});


		// sort_spinnerの設定
		sortButton = (Button) parent.findViewById(R.id.search_sort_spinner);
		selectSortAdapter(1);
			sortButton.setText("新しい番組順");
		// ソートスピナーのイベントの登録
		sortButton
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View view) {
						if(searchMode == 3 || searchMode == 4)return;
						new AlertDialog.Builder(ACT)
						.setTitle(sortList[sortIndex])
						.setItems(sortList,
								new DialogInterface.OnClickListener() {
									public void onClick(
											DialogInterface dialog,
											int which) {
										if(editTex != null){
											 // ソフトキーボードを非表示にする
							                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
							                imm.hideSoftInputFromWindow(editTex.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
											editTex.clearFocus();
											}
										//共通の処理
										sortIndex = which;
										sortButton.setText(sortList[sortIndex]);
						if(searchMode ==0){//カテゴリ検索
							CommunityTab.cancelMovingTask();//一旦全てキャンセル
							LiveTab.cancelMovingTask();
							SearchTab.cancelMoveingTask();
							editTex.setText("");
							addProgress();
							modeList[0] = "放送中";
							modeList[1] = "未来の放送";
							modeList[2] = "過去の放送";
								addProgress();
								pageNum = 1;
								createCategoryURL();
								try{
									if(categoryIndex == 0){
										sortIndex = 0;
										selectSortAdapter(1);//ソートをキーワード用にする
										sortButton.setText(sortList[0]);
										addProgress();
										recentTask = new RecentLiveTask();//公式
										recentTask.execute();
									}else{
								searchTask = new SearchTask(requestURL.replaceAll("<<PAGEXXX>>", String.valueOf(pageNum)));//pageNumは1にする
								searchTask.execute();
									}
								}catch(IllegalFormatConversionException e){
									MyToast.customToastShow(ACT, "検索できませんでした\nアプリ再起又は一定時間待ってからお試しください:code 2");
								}
						}else if(searchMode == 1){
								//キーワード検索する
							if(editTex.getText() == null || editTex.getText().equals("")){
								MyToast.customToastShow(ACT,"検索語を入力して下さい");
								return;
							}
								pageNum = 1;
								modeButton.setText("放送中");
								modeList[0] = "放送中";
								modeList[1] = "未来の放送";
								modeList[2] = "過去の放送";
								modeButton.setText(modeList[liveModeIndex]);
								CommunityTab.cancelMovingTask();//一旦全てキャンセル
								LiveTab.cancelMovingTask();
								SearchTab.cancelMoveingTask();
								createKeyWordURL();
								try{
								searchTask = new SearchTask(requestURL.replaceAll("<<PAGEXXX>>", String.valueOf(pageNum)));
								searchTask.execute();
								}catch(IllegalFormatConversionException e){
									MyToast.customToastShow(ACT, "検索できませんでした\nアプリ再起又は一定時間待ってからお試しください:code 1");
								}
						}else if(searchMode == 2){
							//ランキング検索する
							adapter.clear();
							categoryIndex = 0;
							CommunityTab.cancelMovingTask();//一旦全てキャンセル
							LiveTab.cancelMovingTask();
							SearchTab.cancelMoveingTask();
							generateList = new String[0];
							generateIndex = 0;
							generateButton.setText("");
							modeButton.setText("ユーザー生放送");
							modeList[0] = "ユーザー生放送";
							modeList[1] = "公式生放送";
							modeList[2] = "ch生放送";
							if(sortIndex == 3){//ルーキーはユーザー生放送しかありえない
								liveModeIndex = 0;
							}
							modeButton.setText(modeList[liveModeIndex]);
							createRankingURL();
							try{
								searchTask = new SearchTask(requestURL.replaceAll("<<PAGEXXX>>", String.valueOf(pageNum)));
								searchTask.execute();
								switchButton.setText("ランキング");
								}catch(IllegalFormatConversionException e){
									MyToast.customToastShow(ACT, "検索できませんでした\nアプリ再起又は一定時間待ってからお試しください:code 0");
								}
							}
						}
						}).show();
					}
				});


		// mode Spinnerの設定
		modeButton = (Button) parent.findViewById(R.id.search_mode_spinner);
		modeList = new String[3];
		modeList[0] = "放送中";
		modeList[1] = "未来の放送";
		modeList[2] = "過去の放送";
			liveModeIndex = 0;
			modeButton.setText("放送中");//初期値は毎回0
		modeButton
				.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View view) {
						if(categoryIndex != 0 || searchMode == 0 || searchMode == 3 || searchMode == 4){////ここで許されるのは、キーワード検索か、ランキング
							return;
						}
						new AlertDialog.Builder(ACT)
						.setTitle(modeList[liveModeIndex])
						.setItems(modeList,
								new DialogInterface.OnClickListener() {
									public void onClick(
											DialogInterface dialog,
											int which) {
							if(searchMode == 2){//ランキングだったら
								liveModeIndex = which;
								selectSortAdapter(3);
								sortButton.setText(sortList[0]);
								addProgress();
								pageNum = 0;
								footer.setVisibility(View.GONE);
								createRankingURL();
								try{
									modeButton.setText(modeList[liveModeIndex]);
									CommunityTab.cancelMovingTask();//一旦全てキャンセル
									LiveTab.cancelMovingTask();
									SearchTab.cancelMoveingTask();
									searchTask = new SearchTask(requestURL.replaceAll("<<PAGEXXX>>", String.valueOf(pageNum)));
								searchTask.execute();
								}catch(IllegalFormatConversionException e){
									MyToast.customToastShow(ACT, "検索できませんでした\nアプリ再起又は一定時間待ってからお試しください:code 2");
								}
								return;
							}else if(searchMode == 1){//キーワード検索
							if(editTex != null){
								 // ソフトキーボードを非表示にする
				                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				                imm.hideSoftInputFromWindow(editTex.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
								}
							liveModeIndex = which;
							selectSortAdapter(liveModeIndex==1? 2:1);
							sortButton.setText(sortList[0]);//適合率にしないと、未来の時にソート項目が減るのでOutOfBoundsしないように0にリセット
							modeButton.setText(modeList[liveModeIndex]);
							//過去未来を選ぶと、ソートの値が変わる
							//選べないので普通のタグ検索に行く
							categoryButton.setText("カテゴリ検索▼");
							String word = editTex.getText().toString();
							if(word.equals("")){
								MyToast.customToastShow(ACT, "検索ワードを入力してください");
								return;
							}
							switchButton.setText("SP");
							//新規キーワード
							kupaText.setText("検索中");
							addProgress();
							pageNum = 1;
							createKeyWordURL();
							try{
								CommunityTab.cancelMovingTask();//一旦全てキャンセル
								LiveTab.cancelMovingTask();
								SearchTab.cancelMoveingTask();
							searchTask = new SearchTask(requestURL.replaceAll("<<PAGEXXX>>", String.valueOf(pageNum)));//pageNumは1にする
							searchTask.execute();
							}catch(IllegalFormatConversionException e){
								MyToast.customToastShow(ACT, "検索できませんでした\nアプリ再起又は一定時間待ってからお試しください:code 2");
						}
					}
				}
			}).show();
		}
	});

		// generate Spinnerの設定
		generateButton = (Button) parent.findViewById(R.id.search_genarate_button);
		generateButton.setText("関連タグ");
		generateButton.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View arg0) {
						if(searchMode == 1){
							filterDialog();
							return;
						}
						if(generateList == null || generateList.length < 1){
							generateList = new String[0];
							generateIndex = 0;
							return;
						}
						if(generateList.length <= generateIndex){//まだ配列に値入ってない場合でも、クリックされる事がある
							generateIndex = generateList.length-1;
							if(generateIndex < 0){
								generateIndex = 0;
							}
						}
						new AlertDialog.Builder(ACT)
						.setTitle(generateList[generateIndex < 0? 0:generateIndex])
						.setItems(generateList,
								new DialogInterface.OnClickListener() {
									public void onClick(
											DialogInterface dialog,
											int which) {
										if(editTex != null){
											 // ソフトキーボードを非表示にする
							                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
							                imm.hideSoftInputFromWindow(editTex.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
											}
										generateIndex = which;
										generateButton.setText(generateList[generateIndex]);
								//抽出タグが決定するとここが自動的に呼ばれてしまうのでタスク非実行時のみにする
								//一度放送見に行くとsearchTaskはnullになってる
							if(searchTask == null || searchTask.getStatus() != AsyncTask.Status.RUNNING){
								if(searchMode == 0){//カテゴリ検索
											switchButton.setText("SP");
											String tag = "";
											if(which > 0){//タグ解除時は空文字
											tag = generateList[which];
											Matcher mc = Pattern.compile("\\([0-9]+\\)$").matcher(tag);
										if(mc.find()){
				//							Log.d("Log","GROUP ---- " + mc.group() + " LENGTH " + mc.group().length());
											tag = tag.substring(0, tag.length()-mc.group().length());
										}
											editTex.setText(tag);
											}
											kupaText.setText("検索中");
											CommunityTab.cancelMovingTask();//一旦全てキャンセル
											LiveTab.cancelMovingTask();
											SearchTab.cancelMoveingTask();
											addProgress();
											pageNum = 1;
										if(categoryIndex>0&&categoryIndex < 7){
											modeButton.setText("放送中");//放送中以外はありえない2013/05/24時
											tagURL = URLEnum.SMARTPLANE + "recent_onairstreams?tab="+getTagName()+ getCategorySortValue()+"&p=<<PAGEXXX>>&tags="+URLEncoder.encode(tag);
											requestURL = tagURL;
											searchTask = new SearchTask(requestURL);
											searchTask.execute();
											new TagParseTask().execute();//ここはcreateURL系を呼んでないから呼ばれない
										}
								}else if(searchMode == 2){//ランキングのclosed時
									generateCloseValue = "&select_date="+ generateList[generateIndex];
									generateButton.setText(generateList[generateIndex]);
									createRankingURL();
									try{
									searchTask = new SearchTask(requestURL.replaceAll("<<PAGEXXX>>", String.valueOf(pageNum)));//pageNumは1にする
									searchTask.execute();
									}catch(IllegalFormatConversionException e){
										MyToast.customToastShow(ACT, "検索できませんでした\nアプリ再起又は一定時間待ってからお試しください:code 3");
									}
								}else if(searchMode == 3){//番組表時
									Log.d("BUNGUMI ----", " " + searchMode);
									pageNum = 0;//ここで0でいいのかよくわからない
									adapter.clear();
									generateButton.setText(generateList[generateIndex]);
									createTimeTableURL();
									try{
									searchTask = new SearchTask(requestURL.replaceAll("<<PAGEXXX>>", String.valueOf(pageNum)));//pageNumは1にする
									searchTask.execute();
									}catch(IllegalFormatConversionException e){
										MyToast.customToastShow(ACT, "検索できませんでした\nアプリ再起又は一定時間待ってからお試しください:code 3");
									}
								}
						}//End of searchTask != RUNNING
					}
			}).show();
		}
	});
		//searchボタン(キーワードのみ)
				Button searchButton = (Button) parent.findViewById(R.id.searchbutton);
				searchButton.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View view) {
						keyWordSearch();
					}
				});
		editTex = (EditText) parent.findViewById(R.id.search_edittext);
		editTex.setOnFocusChangeListener(new View.OnFocusChangeListener() {
	        @Override
	        public void onFocusChange(View v, boolean hasFocus) {
	            // EditTextのフォーカスが外れた場合
	            if (hasFocus == false) {
	                // ソフトキーボードを非表示にする
	                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
	                imm.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
	                if(searchHisLL != null){
	                	ViewGroup vg = (ViewGroup)searchHisLL.getParent();
	                	if(vg != null)vg.removeView(searchHisLL);
	                }
	            }
	            //ソートとカテゴリ選択時に出てきてしまうので、フォーカスきっかけで表示するのを辞めた
//	            else if(editTex.getText() == null || editTex.getText().toString().equals("") && (TopTabs.his_value & 0x40) > 0 && TopTabs.his_db != null && TopTabs.his_db.getDB() != null){
//	            	displaySearchHis();
//	            }
	        }
	    });
		editTex.setFilters(new InputFilter[]{new InputFilter(){
			@Override
			public CharSequence filter(CharSequence source, int i, int j,
					Spanned spanned, int k, int l) {
				Log.d("NLiveRoid","SPANNED  " + i + " " + j + " " + k + " " + l);
				Log.d("NLiveRoid","SPNNNN" + TopTabs.his_db + " " + TopTabs.his_value + " " + (TopTabs.his_db == null? "NULL":TopTabs.his_db.getDB()) + " " + source + "  "+searchHisLL);
				if(spanned != null && spanned.length() > 0 ){
					if(searchHisLL != null){
	                	ViewGroup vg = (ViewGroup)searchHisLL.getParent();
	                	if(vg != null)vg.removeView(searchHisLL);
	                }
				}else if(i == 0 && (TopTabs.his_value & 0x40) > 0 && TopTabs.his_db != null && TopTabs.his_db.getDB() != null){
					if(source != null  && (source.toString().equals(" ")||source.toString().equals("　"))) displaySearchHis();
				}
				return source;
			}

		}});
		editTex.setOnEditorActionListener(new OnEditorActionListener(){
			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
//				Log.d("NLiveRoid","KEYCODE "  + KeyEvent.KEYCODE_ENTER  +" " + (event == null? "null":event.getAction() + " "+ event.getKeyCode()));
				if(event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN){//android:imeOptions="actionSearch"だとevent==nullとACTION_DOWNしか来ないみたい
				keyWordSearch();
				}
				return false;
			}
		});
		adapter = new SearchArrayAdapter(this);
		footer = (View)inflater.inflate(R.layout.search_footer, null);
		footer.setBackgroundColor(Color.parseColor("#00000000"));
		listview = new ListView(this);
		listview.addFooterView(footer);
		listview.setDividerHeight(1);
		listview.setAdapter(adapter);
		listview.setFastScrollEnabled(true);
		listview.setScrollingCacheEnabled(false);
		registerForContextMenu(listview);
		listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				 if(NLiveRoid.isDebugMode)Log.d("NLiveRoid","SonItemClick" + isListTaped);
				if(adapter.getCount() > position ){
				 if(gate != null){
					 if(!gate.isOpened() ){
						startFlashPlayer(adapter.getItem(position));
					}
				 }else{
						startFlashPlayer(adapter.getItem(position));
				 	}
				}
			}
		});
		listview.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScroll(AbsListView view, int firstVisible,
					int visibleItemCount, int totalItemCount) {
				if (totalItemCount == firstVisible + visibleItemCount) {// スクロール入ってた
											Log.d("NLiveRoid","PAGE ---- " + categoryIndex + "  "+ searchMode + "  "+ generateIndex + " " + liveModeIndex + "  " + footer.getVisibility());
					//次のページ処理
					if(searchMode == 2){
						footer.setVisibility(View.GONE);
						return;
					}else if(searchTask != null && searchTask.getStatus() == AsyncTask.Status.FINISHED && footer.getVisibility()== View.VISIBLE){
//						Log.d("log","PAGE ---- " + requestURL);
						if(searchMode == 3||(pageNum > 0 	&& requestURL != null&&requestURL.contains("<<PAGEXXX>>"))){//カテゴリから抽出タグに行く場合ページとかないので<<PAGEXXX>>>で判断
//							Log.d("log","C PAGE SEARCH----------- ");
						++pageNum;
						try{
						searchTask = new SearchTask(requestURL.replaceAll("<<PAGEXXX>>", String.valueOf(pageNum)));
						searchTask.execute();
						}catch(IllegalFormatConversionException e){
							MyToast.customToastShow(ACT, "検索できませんでした\nアプリ再起又は一定時間待ってからお試しください:code 4");
						}
//						Log.d("Log","NEW PAGE -----------------------------------");
						}else if(requestURL != null && editTex.getText().toString().matches(".+") && categoryIndex == 0){
							++pageNum;
							try{
							searchTask = new SearchTask(requestURL.replaceAll("<<PAGEXXX>>", String.valueOf(pageNum)));
							searchTask.execute();
							}catch(IllegalFormatConversionException e){
								MyToast.customToastShow(ACT, "検索できませんでした\nアプリ再起又は一定時間待ってからお試しください:code 5");
							}
						}else if(requestURL != null && !requestURL.matches("<<PAGEXXX>>")){
								//カテゴリの後の抽出タグサーチ
								footer.setVisibility(View.GONE);
							}
//						Log.d("Log","NEW AAAA -----------------------------------");
							//この辺りのrequestURLのnullチェックは無いと放置後落ちる
						}
				}
			}

			@Override
			public void onScrollStateChanged(AbsListView arg0, int arg1) {
			}
		});
		((LinearLayout) parent.findViewById(R.id.search_list_parent)).addView(
				listview, new LinearLayout.LayoutParams(-1, -2));

		progressareaParent = (LinearLayout) progressArea.getParent();
		listParent = (LinearLayout) listview.getParent();
		ImageButton voiceButton = (ImageButton) parent
				.findViewById(R.id.voice_search);
		voiceButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				// 音声検索処理
				//singleInstanceだと、returnが返ってこないので、新たにActivity作ってそっからブロキャスでセットして、検索に持っていく
				kupaText.setText("検索中");
				Intent recogDummy = new Intent(ACT,RecognizeDummy.class);
				ACT.startActivity(recogDummy);
			}
		});
		Button reloadBt = (Button)parent.findViewById(R.id.search_reload_bt);
		reloadBt.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				ACT.onReload();
			}
		});
		Button settingBt = (Button)parent.findViewById(R.id.search_setting_bt);
		settingBt.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Intent settingIntent = new Intent(ACT,SettingTabs.class);
	        	NLiveRoid app = (NLiveRoid)ACT.getApplicationContext();
	        	settingIntent.putExtra("session",app.getSessionid());
	        	startActivityForResult(settingIntent, CODE.REQUEST_SETTING_TAB);
			}
		});

		if(isPCSearch){
			//レイアウトの変更 PC版を表示する
			rootView.removeAllViews();//ここでsearchRootLinearとlayer2と両方切り離さないとlayer2が挟まって固まる
			rootView.addView(sWv,new FrameLayout.LayoutParams(-1,-1));
			rootView.addView(browse_header);
		}
		setContentView(parent);

	}//End of onCreate


	private void setTextColor(TextView tview, int tcolor) {
		switch(tcolor){
		case 0:
			tview.setTextColor(Color.GRAY);
			break;
		case 1:
			tview.setTextColor(Color.BLACK);
			break;
		case 2:
			tview.setTextColor(Color.WHITE);
			break;

		}
	}

	public static SearchTab getSearchTab() {
		return ACT;
	}

	@Override
	public void onResume() {
		Log.d("NLiveRoid","RESUME SSS " + NLiveRoid.isDebugMode + " " + NLiveRoid.apiLevel);
		isListTaped = false;//連続タップ防止、できてるか?

		final NLiveRoid app  = (NLiveRoid)getApplicationContext();
		app.setForeACT(this);

		try{
		if(TopTabs.isFirstLogin&&Boolean.parseBoolean(app.getDetailsMapValue("ac_confirm"))){
			//初回確認リスト表示
			if(accountDialog == null || !accountDialog.isShowing()){
				accountDialog =	new AlertDialog.Builder(this).setTitle("アカウントを選択").setItems(new CharSequence[]{app.getDefaultMap().get("user_id1"),app.getDefaultMap().get("user_id2")},
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							//アカウントを設定
							switch(which){
							case 0:
								app.setDetailsMapValue("always_use1", "true");
								app.setDetailsMapValue("always_use2", "false");
								app.setSessionid("");
								break;
							case 1:
								app.setDetailsMapValue("always_use1", "false");
								app.setDetailsMapValue("always_use2", "true");
								app.setSessionid("");
								break;
							}
							if(error.getErrorCode()<0)error.setErrorCode(0);//コミュニティタブでは発生しない
							//このタブでのonReload→RecentLiveTaskは、新たにアカウントを取りに行かないロジックがある
							if(PrimitiveSetting.getACT() != null)PrimitiveSetting.getACT().updateAlways();
							TopTabs.isFirstLogin = false;
						onReload();
						}
					}).setCancelable(false).create();
				accountDialog.show();
			}
		}else if (adapter.isEmpty()||Boolean.parseBoolean(app.getDetailsMapValue("update_tab"))) {
				this.onReload();
		}
	}catch(NullPointerException e){
		e.printStackTrace();
	}catch(Exception e){
		e.printStackTrace();
		MyToast.customToastShow(this, "コンテキストエラー");
	}
		super.onResume();
	}

	/**
	 * リロード又はタブを開いた時(onResume)にパース開始する
	 * カテゴリを見て判断
	 *
	 */
	public synchronized void onReload() {
		CommunityTab.cancelMovingTask();//一旦全てキャンセル
		LiveTab.cancelMovingTask();
		SearchTab.cancelMoveingTask();
		if(isPCSearch){//PC版は更新ボタンすら無いけど最初にPC版だったらここで表示開始
			if(sWv != null){
				if(sWv.getUrl() == null){
					sWv.loadUrl(URLEnum.PC_SEARCH);
				}else{
					sWv.reload();
				}
			}
			return;
		}
//		Log.d("ONRELOAD --- " ,"  " + searchMode);
		   InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
           imm.hideSoftInputFromWindow(editTex.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
		if((searchMode == 1 && (editTex == null || editTex.getText().toString().equals("")))
				||(searchMode == 0 && categoryIndex == 0)){//テキストが入ってればキーワード検索を優先する){
			recentTask = new RecentLiveTask();
			recentTask.execute();
			addProgress();
			return;
		}
			if(searchMode == 0){//カテゴリ検索
					editTex.setText("");
					pageNum = 1;
					modeButton.setText("放送中");
					modeList[0] = "放送中";
					modeList[1] = "未来の放送";
					modeList[2] = "過去の放送";
					liveModeIndex = 0;
	//				sortIndex = 0;
					selectSortAdapter(0);//ソートをカテゴリ用に戻す
					sortButton.setText(sortList[0]);
					switchButton.setText("SP");
					createCategoryURL();
			}else if(searchMode == 1){
				categoryIndex = 0;
				searchMode = 1;
				//ボタンのテキストも変更しておく
				categoryButton.setText("カテゴリ検索▼");
				kupaText.setText("検索中");
				generateList = new String[0];
				generateIndex = 0;
				generateButton.setText("+");
				switchButton.setText("SP");
				pageNum = 1;
				modeList[0] = "放送中";
				modeList[1] = "未来の放送";
				modeList[2] = "過去の放送";
//				liveModeIndex = 0;//他のモードだった場合か、検索語を変えた場合は放送中に戻す
				modeButton.setText(modeList[liveModeIndex]);
				timetableHref = new String[0];//戻しておかないと番組表に戻ったときにgenerateが出ない
				createKeyWordURL();
				selectSortAdapter(1);//適合率～にする
				if(sortIndex >= sortList.length)sortIndex = 0;
				sortButton.setText(sortList[sortIndex]);
				createKeyWordURL();
			}else if(searchMode == 2){//ランキング
				editTex.setText("");
				editTex.setText("");
				adapter.clear();
				categoryIndex = 0;
				categoryButton.setText("カテゴリ検索▼");
				searchMode = 2;
				footer.setVisibility(View.GONE);
				CommunityTab.cancelMovingTask();//一旦全てキャンセル
				LiveTab.cancelMovingTask();
				SearchTab.cancelMoveingTask();
				generateList = new String[0];
				generateIndex = 0;
				generateButton.setText("");
				modeButton.setText("ユーザー生放送");
				modeList[0] = "ユーザー生放送";
				modeList[1] = "公式生放送";
				modeList[2] = "ch生放送";
				liveModeIndex = 0;
				timetableHref = new String[0];//戻しておかないと番組表に戻ったときにgenerateが出ない
				selectSortAdapter(3);
				sortButton.setText(sortList[0]);
				createRankingURL();
			}else if(searchMode == 3){//番組表
					editTex.setText("");
					adapter.clear();
					pageNum = 0;//0からじゃないと正しいクエリにならない
					categoryIndex = 0;
					categoryButton.setText("カテゴリ検索▼");
					searchMode = 3;
					footer.setVisibility(View.GONE);
					CommunityTab.cancelMovingTask();//一旦全てキャンセル
					LiveTab.cancelMovingTask();
					SearchTab.cancelMoveingTask();
					generateList = new String[0];
					generateIndex = -1;//最初に日付選んだ後と、日付入ってきてまだ選んでない状態とはスクロール時の次のページ動作に差異がある
					generateButton.setText("");
					modeButton.setText("");
					modeList[0] = "放送中";
					modeList[1] = "未来の放送";
					modeList[2] = "過去の放送";
					liveModeIndex = 0;
					timetableHref = new String[0];//戻しておかないと番組表に戻ったときにgenerateが出ない
					sortButton.setText("");
					createTimeTableURL();
			}else if(searchMode == 4){//チャンネル
				sortIndex = 0;
				categoryIndex = 0;
				generateList = new String[0];
				generateIndex = 0;
				generateButton.setText("");
				sortButton.setText("");
				liveModeIndex = 0;
				modeButton.setText("");
				switchButton.setText("チャンネル");
				timetableHref = new String[0];//戻しておかないと番組表に戻ったときにgenerateが出ない
				categoryButton.setText("カテゴリ検索▼");
				footer.setVisibility(View.GONE);
				CommunityTab.cancelMovingTask();//一旦全てキャンセル
				LiveTab.cancelMovingTask();
				SearchTab.cancelMoveingTask();
			}
			addProgress();
			try{
			searchTask = new SearchTask(requestURL.replaceAll("<<PAGEXXX>>", String.valueOf(pageNum)));
			searchTask.execute();
			}catch(IllegalFormatConversionException e){
				MyToast.customToastShow(ACT, "検索できませんでした\nアプリ再起又は一定時間待ってからお試しください:code 7");
			}
	}

	@Override
	public void onPause() {
		isListTaped = false;

		//キーボードが出てきちゃうのでクリア
		// ボタンにフォーカスを移動させる
			if(parent != null){
				parent.setFocusable(true);
				parent.setFocusableInTouchMode(true);
				parent.requestFocus();
			}
			//Gateが開いてたら閉じる
			if(gate != null && gate.isOpened()){
				gate.close_noanimation();
			}
		// 前回の検索種類とソートを保存
//		NLiveRoid app = (NLiveRoid) getApplicationContext();
//		app.setDetailsMapValue("last_kind",
//				String.valueOf(seek.getProgress()));
//		app.setDetailsMapValue("last_sot_spinner",
//				String.valueOf(sortIndex));
//		app.setDetailsMapValue("last_seek_value",
//				String.valueOf(seek.getProgress()));
		super.onPause();
	}

	@Override
	public void onDestroy(){
		super.onDestroy();
		if(adapter != null)adapter.clear();
	}

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
    	super.onWindowFocusChanged(hasFocus);
    	if(searchMode ==2)return;
    	//レイアウトをやり直す
		NLiveRoid app = (NLiveRoid) getApplicationContext();
		int width = app.getViewWidth()/2;
		sortButton.setLayoutParams(new TableRow.LayoutParams(width,categoryButton.getHeight()));
		generateButton.setLayoutParams(new TableRow.LayoutParams(width,categoryButton.getHeight()));
		modeButton.setLayoutParams(new TableRow.LayoutParams(width,categoryButton.getHeight()));
		editTex.setLayoutParams(new TableRow.LayoutParams(width,categoryButton.getHeight()));
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig){
		super.onConfigurationChanged(newConfig);
    if(gate != null &&  gate.isOpened()){
		gate.onConfigChanged(newConfig);
	}
    }
	/**
	 * このクラスの実行中のタスクを全てキャンセルする
	 *
	 */
	public static void cancelMoveingTask() {
		if (isFirstThread)
			return;
		if (searchTask != null) {
			searchTask.cancel(true);
			searchTask = null;
		}
		if (recentTask != null) {
			recentTask.cancel(true);
			recentTask = null;
		}
	}


	public synchronized void startFlashPlayer(LiveInfo liveObj) {
		if(isListTaped){
			return;
		}
		isListTaped = true;

		if(liveObj.getLiveID() == null && liveObj.getCommunityID() != null && !liveObj.getCommunityID().equals(URLEnum.HYPHEN)){//ランキングでrookieで検索書けた場合LVはない
			Intent commuTab = new Intent(this,TopTabs.class);
			commuTab.putExtra("scheme", liveObj.getCommunityID());
			commuTab.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(commuTab);
			return;
		}
		NLiveRoid app = (NLiveRoid) getApplicationContext();
		// プレイヤー起動処理

		boolean[] setting_boolean = new boolean[28];
		try{
//			 Log.d("NLiveRoid","SearchTab startF " + liveObj.getLiveID() + " " + liveObj.getCommunity_info() + " " + liveObj.getCommunityName() + " " + liveObj.getCommunityID());
			 TopTabs.insertHis(0, liveObj.getLiveID(), liveObj.getCommunityID(), liveObj.getTitle(), liveObj.getCommunityName(), liveObj.getDescription());

			//fexit,(finish_back),at,at_overwriteはDefaultMapValue
		setting_boolean[0] = app.getDetailsMapValue("fexit")==null? true:Boolean.parseBoolean(app.getDetailsMapValue("fexit"));
		setting_boolean[1] = app.getDetailsMapValue("newline")== null? false:Boolean.parseBoolean(app.getDetailsMapValue("newline"));
		setting_boolean[2] = app.getDetailsMapValue("form_up") == null ? false: Boolean.parseBoolean(app.getDetailsMapValue("form_up"));
		setting_boolean[3] = app.getDetailsMapValue("voice_input") == null ? false:Boolean.parseBoolean(app.getDetailsMapValue("voice_input"));
//		setting_boolean[4] = app.getDetailsMapValue("sp_player") == null ? true:Boolean.parseBoolean(app.getDetailsMapValue("sp_player"));
		setting_boolean[5] = app.getDetailsMapValue("fix_volenable") == null ? false:Boolean.parseBoolean(app.getDetailsMapValue("fix_volenable"));
		setting_boolean[6] = app.getDetailsMapValue("at_enable")==null? false:Boolean.parseBoolean(app.getDetailsMapValue("at_enable"));
		setting_boolean[7] = app.getDetailsMapValue("at_overwrite")==null? false:Boolean.parseBoolean(app.getDetailsMapValue("at_overwrite"));

		setting_boolean[8] = app.getDetailsMapValue("xd_enable_p") == null ? false: Boolean.parseBoolean(app.getDetailsMapValue("xd_enable_p"));
		setting_boolean[9] = app.getDetailsMapValue("yd_enable_p") == null ? true: Boolean.parseBoolean(app.getDetailsMapValue("yd_enable_p"));
		setting_boolean[10] = app.getDetailsMapValue("xd_enable_l") == null ? false: Boolean.parseBoolean(app.getDetailsMapValue("xd_enable_l"));
		setting_boolean[11] = app.getDetailsMapValue("yd_enable_l") == null ? true: Boolean.parseBoolean(app.getDetailsMapValue("yd_enable_l"));
		setting_boolean[12] = app.getDetailsMapValue("speech_education_enable") == null ? true: Boolean.parseBoolean(app.getDetailsMapValue("speech_education_enable"));
		setting_boolean[13] = app.getDetailsMapValue("is_update_between") == null ? true: Boolean.parseBoolean(app.getDetailsMapValue("is_update_between"));

		setting_boolean[14] = app.getDetailsMapValue("sp_showcomment") == null ? true: Boolean.parseBoolean(app.getDetailsMapValue("sp_showcomment"));
		setting_boolean[15] = app.getDetailsMapValue("sp_ng184") == null ? true: Boolean.parseBoolean(app.getDetailsMapValue("sp_ng184"));
		setting_boolean[16] = app.getDetailsMapValue("sp_showbspcomment") == null ? true: Boolean.parseBoolean(app.getDetailsMapValue("sp_showbspcomment"));
		setting_boolean[17] = app.getDetailsMapValue("sp_ismute") == null ? true: Boolean.parseBoolean(app.getDetailsMapValue("sp_ismute"));
		setting_boolean[18] = app.getDetailsMapValue("sp_loadsmile") == null ? true: Boolean.parseBoolean(app.getDetailsMapValue("sp_loadsmile"));
		setting_boolean[19] = app.getDetailsMapValue("auto_username") == null ? false: Boolean.parseBoolean(app.getDetailsMapValue("auto_username"));
		setting_boolean[20] = app.getDetailsMapValue("form_backkey") == null ? true: Boolean.parseBoolean(app.getDetailsMapValue("form_backkey"));
		setting_boolean[21] = app.getDetailsMapValue("discard_notification") == null ? false: Boolean.parseBoolean(app.getDetailsMapValue("discard_notification"));
		setting_boolean[22] = app.getDetailsMapValue("manner_0") == null ? false: Boolean.parseBoolean(app.getDetailsMapValue("manner_0"));
		setting_boolean[23] = app.getDetailsMapValue("return_tab") == null ? false: Boolean.parseBoolean(app.getDetailsMapValue("return_tab"));
		setting_boolean[24] = app.getDetailsMapValue("update_tab") == null ? false: Boolean.parseBoolean(app.getDetailsMapValue("update_tab"));
		setting_boolean[25] = app.getDetailsMapValue("recent_ts") == null ? true: Boolean.parseBoolean(app.getDetailsMapValue("recent_ts"));
		setting_boolean[26] = app.getDetailsMapValue("delay_start") == null ? true: Boolean.parseBoolean(app.getDetailsMapValue("delay_start"));
		setting_boolean[27] = app.getDetailsMapValue("back_black") == null ? false: Boolean.parseBoolean(app.getDetailsMapValue("back_black"));
		}catch(Exception e){
			//失敗したらデフォ値
			setting_boolean = new boolean[]{true,false,false,false,true,false,false,false,false,true,false,true,false,true,true,true,true,true,true,false,true,false,false,false,false,true,true,false};
		}
		byte[] setting_byte = new byte[44];
		short init_comment_count = 20;
		String twitterToken = null;
		long offTimer = -1;
		try{
		twitterToken = app.getDefaultMap().get("twitter_token")==null? null:app.getDefaultMap().get("twitter_token") + " " + app.getDefaultMap().get("twitter_secret");
		init_comment_count = app.getDetailsMapValue("init_comment_count")==null? 20:Short.parseShort(app.getDetailsMapValue("init_comment_count"));
		offTimer = app.getDetailsMapValue("offtimer_start")==null? -1:Long.parseLong(app.getDetailsMapValue("offtimer_start"));
		setting_byte[0] = app.getDetailsMapValue("type_width_p")==null? 0:Byte.parseByte(app.getDetailsMapValue("type_width_p"));
		setting_byte[1] = app.getDetailsMapValue("id_width_p")==null? 15:Byte.parseByte(app.getDetailsMapValue("id_width_p"));
		setting_byte[2] = app.getDetailsMapValue("command_width_p")==null? 0:Byte.parseByte(app.getDetailsMapValue("command_width_p"));
		setting_byte[3] = app.getDetailsMapValue("time_width_p")==null? 0:Byte.parseByte(app.getDetailsMapValue("time_width_p"));
		setting_byte[4] = app.getDetailsMapValue("score_width_p")==null? 0:Byte.parseByte(app.getDetailsMapValue("score_width_p"));
		setting_byte[5] = app.getDetailsMapValue("num_width_p")==null? 15:Byte.parseByte(app.getDetailsMapValue("num_width_p"));
		setting_byte[6] = app.getDetailsMapValue("comment_width_p")==null? 70:Byte.parseByte(app.getDetailsMapValue("comment_width_p"));
		setting_byte[7] = app.getDetailsMapValue("cellheight_p")==null? 3:Byte.parseByte(app.getDetailsMapValue("cellheight_p"));
		setting_byte[8] = app.getDetailsMapValue("x_pos_p")==null? 0:Byte.parseByte(app.getDetailsMapValue("x_pos_p"));
		setting_byte[9] = app.getDetailsMapValue("y_pos_p")==null? 92:Byte.parseByte(app.getDetailsMapValue("y_pos_p"));
		setting_byte[10] = app.getDetailsMapValue("bottom_pos_p")==null? -43:Byte.parseByte(app.getDetailsMapValue("bottom_pos_p"));
		setting_byte[11] = app.getDetailsMapValue("type_width_l")==null? 0:Byte.parseByte(app.getDetailsMapValue("type_width_l"));
		setting_byte[12] = app.getDetailsMapValue("id_width_l")==null? 0:Byte.parseByte(app.getDetailsMapValue("id_width_l"));
		setting_byte[13] = app.getDetailsMapValue("command_width_l")==null? 0:Byte.parseByte(app.getDetailsMapValue("command_width_l"));
		setting_byte[14] = app.getDetailsMapValue("time_width_l")==null? 0:Byte.parseByte(app.getDetailsMapValue("time_width_l"));
		setting_byte[15] = app.getDetailsMapValue("score_width_l")==null? 0:Byte.parseByte(app.getDetailsMapValue("score_width_l"));
		setting_byte[16] = app.getDetailsMapValue("num_width_l")==null? 15:Byte.parseByte(app.getDetailsMapValue("num_width_l"));
		setting_byte[17] = app.getDetailsMapValue("comment_width_l")==null? 70:Byte.parseByte(app.getDetailsMapValue("comment_width_l"));
		setting_byte[18] = app.getDetailsMapValue("cellheight_l")==null? 3:Byte.parseByte(app.getDetailsMapValue("cellheight_l"));
		setting_byte[19] = app.getDetailsMapValue("x_pos_l")==null? 0:Byte.parseByte(app.getDetailsMapValue("x_pos_l"));
		setting_byte[20] = app.getDetailsMapValue("y_pos_l")==null? 92:Byte.parseByte(app.getDetailsMapValue("y_pos_l"));
		setting_byte[21] = app.getDetailsMapValue("bottom_pos_l")==null? -43:Byte.parseByte(app.getDetailsMapValue("bottom_pos_l"));
		setting_byte[22] = app.getDetailsMapValue("player_pos_p")==null ? 0:Byte.parseByte(app.getDetailsMapValue("player_pos_p"));
		setting_byte[23] = app.getDetailsMapValue("player_pos_l")==null ? 0:Byte.parseByte(app.getDetailsMapValue("player_pos_l"));
		setting_byte[24] = app.getDetailsMapValue("fix_screen")==null ? 0:Byte.parseByte(app.getDetailsMapValue("fix_screen"));
		if(setting_boolean[5]){//有効にしてなければ-1
			setting_byte[25] = app.getDetailsMapValue("fix_volvalue") == null ? -1:Byte.parseByte(app.getDetailsMapValue("fix_volvalue"));
		}else{
			setting_byte[25] = -1;
		}
		setting_byte[26] = app.getDetailsMapValue("speech_speed")==null ? 5:Byte.parseByte(app.getDetailsMapValue("speech_speed"));
		setting_byte[27] = app.getDetailsMapValue("speech_pich")==null ? 5:Byte.parseByte(app.getDetailsMapValue("speech_pich"));
		setting_byte[28] = app.getDetailsMapValue("speech_aques_phont")==null ? 0:Byte.parseByte(app.getDetailsMapValue("speech_aques_phont"));
		setting_byte[29] = app.getDetailsMapValue("speech_skip_count")==null ? 5:Byte.parseByte(app.getDetailsMapValue("speech_skip_count"));
		setting_byte[30] = app.getDetailsMapValue("sp_volumesub")==null ? 50:Byte.parseByte(app.getDetailsMapValue("sp_volumesub"));
		setting_byte[31] = app.getDetailsMapValue("layer_num")==null ? 0:Byte.parseByte(app.getDetailsMapValue("layer_num"));
		setting_byte[32] = app.getDetailsMapValue("auto_comment_update")==null ? -1:Byte.parseByte(app.getDetailsMapValue("auto_comment_update"));
		setting_byte[33] = app.getDetailsMapValue("speech_enable")==null ? 0:Byte.parseByte(app.getDetailsMapValue("speech_enable"));
		setting_byte[34] = app.getDetailsMapValue("player_quality")==null ? 0:Byte.parseByte(app.getDetailsMapValue("player_quality"));
		setting_byte[35] = app.getDetailsMapValue("cellheight_test")==null? 4:Byte.parseByte(app.getDetailsMapValue("cellheight_test"));
		setting_byte[36] = app.getDetailsMapValue("speech_aques_vol")==null? 5:Byte.parseByte(app.getDetailsMapValue("speech_aques_vol"));
		setting_byte[37] = app.getDetailsMapValue("off_timer")==null? -1:Byte.parseByte(app.getDetailsMapValue("off_timer"));
		setting_byte[38] = app.getDetailsMapValue("width_p")==null? 100:Byte.parseByte(app.getDetailsMapValue("width_p"));
		setting_byte[39] = app.getDetailsMapValue("width_l")==null? 40:Byte.parseByte(app.getDetailsMapValue("width_l"));
		setting_byte[40] = app.getDetailsMapValue("quick_0")==null? 15:Byte.parseByte(app.getDetailsMapValue("quick_0"));
		setting_byte[41] = app.getDetailsMapValue("quick_1")==null? 127:Byte.parseByte(app.getDetailsMapValue("quick_1"));
		setting_byte[42] = app.getDetailsMapValue("alpha")==null? 0:Byte.parseByte(app.getDetailsMapValue("alpha"));
		setting_byte[43] = app.getDetailsMapValue("player_select")==null? 0:Byte.parseByte(app.getDetailsMapValue("player_select"));
			}catch(Exception e){
				//失敗したらデフォ値
				setting_byte = new byte[]{0,15,0,0,0,15,70,3,0,92,-43,0,15,0,0,0,15,70,3,0,92,-43,0,0,0,-1,5,5,0,5,50,0,-1,0,0,4,5,-1,100,40,15,127,0,0};
			}
		byte[] seq = new byte[7];
		try{
			//列順
			seq[0] = app.getDetailsMapValue("type_seq")==null? 0:Byte.parseByte(app.getDetailsMapValue("type_seq"));
			seq[1] = app.getDetailsMapValue("id_seq")==null? 1:Byte.parseByte(app.getDetailsMapValue("id_seq"));
			seq[2] = app.getDetailsMapValue("cmd_seq")==null? 2:Byte.parseByte(app.getDetailsMapValue("cmd_seq"));
			seq[3] = app.getDetailsMapValue("time_seq")==null? 3:Byte.parseByte(app.getDetailsMapValue("time_seq"));
			seq[4] = app.getDetailsMapValue("score_seq")==null? 4:Byte.parseByte(app.getDetailsMapValue("score_seq"));
			seq[5] = app.getDetailsMapValue("num_seq")==null? 5:Byte.parseByte(app.getDetailsMapValue("num_seq"));
			seq[6] = app.getDetailsMapValue("comment_seq")==null? 6:Byte.parseByte(app.getDetailsMapValue("comment_seq"));

		}catch(Exception e){
			seq = new byte[]{0,1,2,3,4,5,6};
		}
		try {
			CommandMapping cmd = null;
			String[] cmdValue = new String[4];
			cmdValue[0] = app.getDetailsMapValue("cmd_cmd");
			cmdValue[1] = app.getDetailsMapValue("cmd_size");
			cmdValue[2] = app.getDetailsMapValue("cmd_color");
			cmdValue[3] = app.getDetailsMapValue("cmd_align");
					for(int i = 0 ; i < 4; i++){
						if(cmdValue[i] != null){
							if(i==3){
							cmd = new CommandMapping(cmdValue[0],cmdValue[1],cmdValue[2],cmdValue[3],false);
							break;
							}
						}
						if(i==3){//1つでもnullがあったら普通の初期化
							cmd = new CommandMapping(false);
						}
					}

			String skip_word = app.getDetailsMapValue("speech_skip_word")==null ? "いかりゃく":app.getDetailsMapValue("speech_skip_word");

			CookieSyncManager.getInstance().startSync();
			String cookie = CookieManager.getInstance().getCookie("nicovideo.jp");
			if (cookie == null) {
				cookie = Request.getSessionID(app.getError());
				if (cookie == null || cookie.equals("")
						|| cookie.equals("null")) {
					app.getError().showErrorToast();
					return;
				}
				CookieManager.getInstance()
						.setCookie("nicovideo.jp", cookie);
			}
			CookieSyncManager.getInstance().stopSync();
			CommunityTab.cancelMovingTask();//一旦全てキャンセル
			LiveTab.cancelMovingTask();
			cancelMoveingTask();
			Intent flash = new Intent(ACT, FlashPlayer.class);
			if(searchMode == 5)flash.putExtra("isnsen",true);//Nsenはここで加えておく
			flash.putExtra("setting_boolean", setting_boolean);
			flash.putExtra("setting_byte", setting_byte);
			flash.putExtra("init_comment_count", init_comment_count);
			if(offTimer > 0)flash.putExtra("offtimer_start", offTimer);
			flash.putExtra("column_seq", seq);
			flash.putExtra("cmd", cmd);
			flash.putExtra("speech_skip_word", skip_word);
			flash.putExtra("density", app.getMetrics());
			flash.putExtra("twitterToken", twitterToken);
		//コメのみならプレイヤーパラメタいらないけど。。とりあえず落としたくはないので
			flash.putExtra("resizeW", app.getResizeW());
			flash.putExtra("resizeH", app.getResizeH());
			flash.putExtra("viewW",app.getViewWidth());
			flash.putExtra("viewH",app.getViewHeight());
			flash.putExtra("LiveInfo", liveObj);
			flash.putExtra("Cookie",cookie);
			if(app.getDetailsMapValue("player_select") != null)flash.putExtra("sp_session",app.getSp_session_key());
			 flash.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			 if(!TopTabs.getACT().isMovingSameLV(liveObj.getLiveID())){
				//裏にいたら停止
				 flash.putExtra("restart", true);
			 }
		    	startActivity(flash);
		} catch (RuntimeException e) {
				Log.d("NLiveRoid", "RUNNTIME ERR SERCH TAB");
				e.printStackTrace();
		}
	}
	private void addProgress() {
		// プログレスバーのアニメーションを起動
		TranslateAnimation animation = new TranslateAnimation(
				Animation.RELATIVE_TO_PARENT, 0.0f,
				Animation.RELATIVE_TO_PARENT, 0.0f,
				Animation.RELATIVE_TO_PARENT, 1.0f,
				Animation.RELATIVE_TO_PARENT, 0.0f);
		animation.setDuration(400);
		animation.setFillAfter(true);
		View pParent = inflater.inflate(R.layout.progressbar, null);
		progressBar = (ProgressBar) pParent
				.findViewById(R.id.ProgressBarHorizontal);// 毎回生成しないとできない
		progressArea
				.addView(progressBar, new LinearLayout.LayoutParams(-1, -1));
		progressBar.startAnimation(animation);
		progressBar.setProgress(1);
	}

	private void removeProgress() {

		TranslateAnimation animation = new TranslateAnimation(
				Animation.RELATIVE_TO_PARENT, 0.0f,
				Animation.RELATIVE_TO_PARENT, 0.0f,
				Animation.RELATIVE_TO_PARENT, 0.0f,
				Animation.RELATIVE_TO_PARENT, 1.0f);
		animation.setDuration(400);
		animation.setFillAfter(true);
		try{
			//起動してすぐ終了とか、これが動いてるタイミングでアプリ終了するとilliegalArgumentExceptionになる事がある
		progressBar.startAnimation(animation);
		progressBar.setProgress(100);
		progressArea.removeAllViews();
		progressArea.setPadding(0, 0, 0,0);
		progressBar.setProgress(0);
		}catch(Exception e){
			e.printStackTrace();
		}
		if(error != null)error.setErrorCode(0);// エラーコードを戻しておく
	}

	private void displaySearchHis(){
		Log.d("NLiveRoid","displaySearchHis  ");
//		try{
        	if(searchHisLL == null){
        		searchHisLL = new LinearLayout(ACT);
        		searchHisLL.setOrientation(LinearLayout.VERTICAL);
        	}else{
        	searchHisLL.removeAllViews();
        	}
        	Cursor c = TopTabs.his_db.getDB().query("his", new String[] { "REMARK0" }, "KIND = 2", null, null , null, null);
        	boolean isEOF = c.moveToLast();
        	int size = c.getCount() > 5? 5:c.getCount();
    		Log.d("NLiveRoid","siz  " + size);
    		TextView[] searchTvs = new TextView[size];
    		ArrayList<String> list = new ArrayList<String>();
        	for(int i = 0; i < size && isEOF;i++){
            	Log.d("NLiveRoid","TEXT " + c.getString(0));
        		if(!list.contains(c.getString(0)))list.add(c.getString(0));//重複をなくす
        		isEOF = c.moveToPrevious();
        	}
        	c.close();
        	for(int i = 0; i < list.size(); i++){
    		final String tmp = list.get(i);
        	Log.d("NLiveRoid","LIST " + list.get(i));
        		searchTvs[i] = new TextView(ACT);
        		searchTvs[i].setBackgroundColor(Color.WHITE);
        		searchTvs[i].setTextColor(Color.BLACK);
        		searchTvs[i].setHeight(editTex.getHeight());
        		searchTvs[i].setText(list.get(i));
        		searchTvs[i].setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View view) {
						editTex.setText(tmp);
						ViewGroup vg = (ViewGroup)searchHisLL.getParent();
						if(vg != null)vg.removeView(searchHisLL);
					}
        		});
        		searchHisLL.addView(searchTvs[i]);
        	}
        	//UIを表示する位置を取得
        	Rect rect = new Rect();
        	editTex.getGlobalVisibleRect(rect);
        	Log.d("NLiveRoid","ET -- " + rect.left + "  " + rect.top + " " +editTex.getWidth() +" "+ editTex.getHeight() +" " + layer2);
        	if(layer2 != null){
        		layer2.removeAllViews();
        		layer2.addView(searchHisLL,new FrameLayout.LayoutParams(editTex.getWidth(), -1));
        		searchHisLL.setPadding(0, rect.top + editTex.getHeight()/2, 0, 0);//edtiTex.getHeight()/2の理由は特になし
        	}
//    	}catch(Exception e){
//    		e.printStackTrace();
//    	}
	}
	private void keyWordSearch(){//UIタスクである必要がある
		// 検索処理
		if(editTex.getText().toString().matches("^ |^　|^\t|^\n")){
			MyToast.customToastShow(ACT, "検索ワードを入力して下さい");
		}else{//キーワード検索する
			// ソフトキーボードを非表示にする
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(editTex.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
			categoryIndex = 0;
			searchMode = 1;
			//ボタンのテキストも変更しておく
			categoryButton.setText("カテゴリ検索▼");
			kupaText.setText("検索中");
			cancelMoveingTask();//一旦全てキャンセル
			LiveTab.cancelMovingTask();
			SearchTab.cancelMoveingTask();
			generateList = new String[0];
			generateIndex = 0;
			generateButton.setText("+");
			switchButton.setText("SP");
			pageNum = 1;
			modeList[0] = "放送中";
			modeList[1] = "未来の放送";
			modeList[2] = "過去の放送";
			liveModeIndex = 0;//他のモードだった場合か、検索語を変えた場合は放送中に戻す
			modeButton.setText(modeList[liveModeIndex]);
			timetableHref = new String[0];//戻しておかないと番組表に戻ったときにgenerateが出ない
			createKeyWordURL();
			selectSortAdapter(1);//適合率～にする
			sortButton.setText(sortList[0]);
			try{
			searchTask = new SearchTask(requestURL.replaceAll("<<PAGEXXX>>", String.valueOf(pageNum)));//pageNumは0にする
			searchTask.execute();
			}catch(IllegalFormatConversionException e){
				MyToast.customToastShow(ACT, "検索できませんでした\nアプリ再起又は一定時間待ってからお試しください:code 6");
			}
			TopTabs.insertHis(2,"","",editTex.getText().toString(),"","");
		}
	}
	public void keyWordSearch_FromGate(String word){
			//新規キーワード
		categoryIndex = 0;
		categoryButton.setText("カテゴリ検索▼");
			switchButton.setText("SP");
			//タグの件数表示が最後にくっついているので削除
			String tag = word;
			Matcher mc = Pattern.compile("\\([0-9|,]+\\)$").matcher(tag);
			if(mc.find()){
//				Log.d("Log","GROUP ---- " + mc.group() + " LENGTH " + mc.group().length());
				tag = tag.substring(0, tag.length()-mc.group().length());
			}
			editTex.setText(tag);
			editTex.clearFocus();
			kupaText.setText("検索中");
			CommunityTab.cancelMovingTask();//一旦全てキャンセル
			LiveTab.cancelMovingTask();
			cancelMoveingTask();
			pageNum = 1;
			searchMode = 1;
			createKeyWordURL();
			try{
			searchTask = new SearchTask(requestURL.replaceAll("<<PAGEXXX>>", String.valueOf(pageNum)));//pageNumは0にする
			searchTask.execute();
			}catch(IllegalFormatConversionException e){
				MyToast.customToastShow(ACT, "検索できませんでした\nアプリ再起又は一定時間待ってからお試しください:code 9");
			}

	}

	private void selectSortAdapter(int index){
		switch(index){
		case 0://カテゴリ検索の場合これになる
			sortList = new String[10];
			sortList[0] = "放送日時が近い順";
			sortList[1] = "放送日時が遠い順";
			sortList[2] = "来場者数が多い順";
			sortList[3] = "来場者数が少ない順";
			sortList[4] = "コメント数が多い順";
			sortList[5] = "コメント数が少ない順";
			sortList[6] = "コミュニティレベルが高い順";
			sortList[7] = "コミュニティレベルが低い順";
			sortList[8] = "コミュニティが新しい順";
			sortList[9] = "コミュニティが古い順";
			break;
		case 1://キーワードで未来じゃない場合これになる
			sortList = new String[13];
			sortList[0] = "適合率の高い順";
			sortList[1] = "放送日時が近い順";
			sortList[2] = "放送日時が遠い順";
			sortList[3] = "タイムシフト予約が多い順";
			sortList[4] = "タイムシフト予約が少ない順";
			sortList[5] = "来場者数が多い順";
			sortList[6] = "来場者数が少ない順";
			sortList[7] = "コメント数が多い順";
			sortList[8] = "コメント数が少ない順";
			sortList[9] = "コミュニティレベルが高い順";
			sortList[10] = "コミュニティレベルが低い順";
			sortList[11] = "コミュニティが新しい順";
			sortList[12] = "コミュニティが古い順";
			break;
		case 2://キーワードの未来の放送の時だけこれになる
			sortList = new String[9];
			sortList[0] = "適合率の高い順";
			sortList[1] = "放送日時が近い順";
			sortList[2] = "放送日時が遠い順";
			sortList[3] = "タイムシフト予約が多い順";
			sortList[4] = "タイムシフト予約が少ない順";
			sortList[5] = "コミュニティレベルが高い順";
			sortList[6] = "コミュニティレベルが低い順";
			sortList[7] = "コミュニティが新しい順";
			sortList[8] = "コミュニティが古い順";
			break;
		case 3://ランキング時
			sortList = new String[3];
			sortList[0] = "生放送中ランキング";
			sortList[1] = "番組予約数ランキング";
			sortList[2] = "過去の放送ランキング";
			break;
		}
	}

	/**
	 * セッションなし、トップの公式の部分のパース
	 * @author Owner
	 *
	 */
	public class RecentLiveTask extends AsyncTask<Void, Void, Void> {
		private boolean ENDFLAG = true;
		private ArrayList<LiveInfo> list;
		private boolean isMaintenance;
		@Override
		protected Void doInBackground(Void... arg0) {
			if(NLiveRoid.isDebugMode)Log.d("NLiveRoid","RecentParser");
			pageNum = 0;
			progressBar.setProgress(5);
			//とりあえずセッションなしのトップページを見に行く
			InputStream response = Request
					.doGetSmartTopToInputStream(error);
			if (response == null||error.getErrorCode() == -7) {
				// ここで-7でメンテか画像失敗かわからない
					return null;
			}
			progressBar.setProgress(10);
			  try {
				  org.ccil.cowan.tagsoup.Parser parser = new org.ccil.cowan.tagsoup.Parser();
				  RecentParser handler = new RecentParser(this,error);
			        parser.setContentHandler(handler);
			        parser.parse(new InputSource(response));
			        progressBar.setProgress(35);
			  } catch (org.xml.sax.SAXNotRecognizedException e) {
			      // Should not happen.
				  e.printStackTrace();
			      throw new RuntimeException(e);
			  } catch (org.xml.sax.SAXNotSupportedException e) {
			      // Should not happen.
				  e.printStackTrace();
			      throw new RuntimeException(e);
			  } catch (IOException e) {
				  e.printStackTrace();
			} catch (SAXException e) {
				e.printStackTrace();
			}
			long startT = System.currentTimeMillis();
			while(ENDFLAG){
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					e.printStackTrace();
					ENDFLAG = false;
					break;
				}catch(IllegalArgumentException e1){
					e1.printStackTrace();
					Log.d("NLiveRoid","IllegalArgumentException at Request getImageForList");
					ENDFLAG = false;
					break;
				}
				if((System.currentTimeMillis() - startT) > 30000){
					error.setErrorCode(-10);
					ENDFLAG = false;
					return null;
				}
			}
			progressBar.setProgress(75);

			return null;
		}//End of doInBack


		public void finishCallBack(ArrayList<LiveInfo> list){
			this.list = list;
			ENDFLAG = false;
		}

		public void finishCallBack(boolean ismente){
			this.isMaintenance = true;
			ENDFLAG = false;
		}
		@Override
		protected void onCancelled() {
			ENDFLAG = false;
			removeProgress();
			super.onCancelled();
		}

		@Override
		protected void onPostExecute(Void arg) {
			progressBar.setProgress(80);
			footer.setVisibility(View.GONE);
			if(isMaintenance){
				MyToast.customToastShow(ACT, "メンテナンス中と思われます");
				removeProgress();
			}else if (list != null && error.getErrorCode() == 0) {
				// 認証がなければどの道getPlayerで取得できないので
				// RSSのリターンがないだけの場合以外のエラーは全てここで不可
				if (list.isEmpty()) {
					MyToast.customToastShow(ACT, "公式、取得できませんでした。");
				} else {
					adapter.clear();//ページとかは無いのでアダプタをクリア
					for(int i = 0; i < adapter.getCount(); i++){//getViewでnull判定できるように入れておく
						adapter.getItem(i).setThumbnail(null);
					}
					for (LiveInfo i : list) {
						adapter.add(i);
					}

					long oneRowCount= 20/list.size();
					for(int i = 0; i < list.size(); i++){//adapter.getItem(i)とかの方がいい気もするけど
						progressBar.setProgress(80+i*(int)oneRowCount);
						new GETThumb(list).execute(i);
						}
					progressBar.setProgress(100);
					isFirstThread = false;
					removeProgress();
				}
			} else {
				error.showErrorToast();
				removeProgress();
			}
		}


	}


	/**
	 * カテゴリのあとの抽出タグでの検索のため、
	 * 単にタグ名を取得するメソッド
	 */
	private String getTagName(){
		switch(categoryIndex){
		case 1:
			return "common";
		case 2:
			return "try";
		case 3:
			return "live";
		case 4:
			return "request";
		case 5:
			return "face";
		case 6:
			return "totu";
		}
		return "";
	}

	/**
	 * 検索タスク
	 * カテゴリ検索のロジックとタグ・キーワード検索のロジックは分かれている
	 *
	 * カテゴリ検索で未来・過去の放送は取得できない
	 *
	 * @author Owner
	 *
	 */

	private String getCategorySortValue(){
		String sort = "";
		switch(sortIndex){
		case 0:
			sort = "&sort=start_time&order=desc";
			break;
		case 1:
			sort = "&sort=start_time&order=asc";
			break;
		case 2:
			sort = "&sort=view_counter&order=desc";
			break;
		case 3:
			sort = "&sort=view_counter&order=asc";
			break;
		case 4:
			sort = "&sort=comment_num&order=desc";
			break;
		case 5:
			sort = "&sort=comment_num&order=asc";
			break;
		case 6:
			sort = "&sort=community_level&order=desc";
			break;
		case 7:
			sort = "&sort=community_level&order=asc";
			break;
		case 8:
			sort = "&sort=community_create&order=desc";
			break;
		case 9:
			sort = "&sort=community_create&order=asc";
			break;
		}
		return sort;
	}

	private String getKeyWordSortValue_0(){
		String sort = "";
		switch(sortIndex){
		case 0:
			sort = "&sort=point";
			break;
		case 1:
			sort = "&sort=recent";
			break;
		case 2:
			sort = "&sort=recent_r";
			break;
		case 3:
			sort = "&sort=ts";
			break;
		case 4:
			sort = "&sort=ts_r";
			break;
		case 5:
			sort = "&sort=user";
			break;
		case 6:
			sort = "&sort=user_r";
			break;
		case 7:
			sort = "&sort=comment";
			break;
		case 8:
			sort = "&sort=comment_r";
			break;
		case 9:
			sort = "&sort=comlevel";
			break;
		case 10:
			sort = "&sort=comlevel_r";
			break;
		case 11:
			sort = "&sort=comcreated";
			break;
		case 12:
			sort = "&sort=comcreated_r";
			break;
		}
		return sort;
	}

	private String getKeyWordSortValue_1(){
		String sort = "";
		switch(sortIndex){
		case 0:
			sort = "&sort=point";
			break;
		case 1:
			sort = "&sort=recent";
			break;
		case 2:
			sort = "&sort=recent_r";
			break;
		case 3:
			sort = "&sort=ts";
			break;
		case 4:
			sort = "&sort=ts_r";
			break;
		case 9:
			sort = "&sort=comlevel";
			break;
		case 10:
			sort = "&sort=comlevel_r";
			break;
		case 11:
			sort = "&sort=comcreated";
			break;
		case 12:
			sort = "&sort=comcreated_r";
			break;
		}
		return sort;
	}

	private String getRankingSortValue(){
		//ランキングでは過去の放送closedを選んだ時だけ日付のフィルタがでてくる
		String sort = "";
		switch(sortIndex){
		case 0:
			sort = "&type=onair";
			break;
		case 1:
			sort = "&type=commingsoon";
			break;
		case 2:
			sort = "&type=closed";
			break;
		}
		return sort;
	}



	public void createCategoryURL(){
		//未来・過去はできないみたい
		//抽出タブは別取得
		//普通のカテゴリでrecent_tags、ニコ電･iPhoneで/search/iPhone?kind=tagsとかあるけど、
		//recent_onairstreamsと普通のタグ検索で全て統一しちゃう(違っちゃう可能瀬はある)
		switch(categoryIndex){
		case 1:
				searchMode = 0;
				editTex.setText("");
				liveModeIndex = 0;
				modeButton.setText("放送中");
			requestURL = URLEnum.SMARTPLANE + "recent_onairstreams?tab=common"+ getCategorySortValue() + "&p=<<PAGEXXX>>";
			tagURL = URLEnum.SMARTPLANE + "recent?tab=common";
			new TagParseTask().execute();
			break;
		case 2:
			searchMode = 0;
			editTex.setText("");
			liveModeIndex = 0;
			modeButton.setText("放送中");
			requestURL = URLEnum.SMARTPLANE + "recent_onairstreams?tab=try"+ getCategorySortValue() + "&p=<<PAGEXXX>>";
			tagURL = URLEnum.SMARTPLANE + "recent?tab=try";
			new TagParseTask().execute();
			break;
		case 3:
			searchMode = 0;
			editTex.setText("");
			liveModeIndex = 0;
			modeButton.setText("放送中");
			requestURL = URLEnum.SMARTPLANE + "recent_onairstreams?tab=live"+ getCategorySortValue() + "&p=<<PAGEXXX>>";
			tagURL = URLEnum.SMARTPLANE + "recent?tab=live";
			new TagParseTask().execute();
			break;
		case 4:
			searchMode = 0;
			editTex.setText("");
			liveModeIndex = 0;
			modeButton.setText("放送中");
			requestURL = URLEnum.SMARTPLANE + "recent_onairstreams?tab=req"+ getCategorySortValue() + "&p=<<PAGEXXX>>";
			tagURL = URLEnum.SMARTPLANE + "recent?tab=req";
			new TagParseTask().execute();
			break;
		case 5:
			searchMode = 0;
			editTex.setText("");
			liveModeIndex = 0;
			modeButton.setText("放送中");
			requestURL = URLEnum.SMARTPLANE + "recent_onairstreams?tab=face"+ getCategorySortValue() + "&p=<<PAGEXXX>>";
			tagURL = URLEnum.SMARTPLANE + "recent?tab=face";
			new TagParseTask().execute();
			break;
		case 6:
			searchMode = 0;
			editTex.setText("");
			liveModeIndex = 0;
			modeButton.setText("放送中");
			requestURL = URLEnum.SMARTPLANE + "recent_onairstreams?tab=totu"+ getCategorySortValue() + "&p=<<PAGEXXX>>";
			tagURL = URLEnum.SMARTPLANE + "recent?tab=totu";//ページ番を付ける
			new TagParseTask().execute();
			break;
		case 7://IPHONEニコ電の時はキーワード・タグに似てるけど初回は検索するURLが違う→統一
			searchMode = 1;
			categoryIndex = 0;
			liveModeIndex = 0;
			editTex.setText("ニコニコ電話");
			selectSortAdapter(liveModeIndex==1? 2:1);
			sortButton.setText(sortList[0]);//適合率にしないと、未来の時にソート項目が減るのでOutOfBoundsしないように0にリセット
			sortButton.setText("適合率の高い順");
			createKeyWordURL();
			return;
		case 8:
			searchMode = 1;
			categoryIndex = 0;
			liveModeIndex = 0;
			editTex.setText("iPhone");
			selectSortAdapter(liveModeIndex==1? 2:1);
			sortButton.setText(sortList[0]);
			sortButton.setText("適合率の高い順");
			createKeyWordURL();
			return;
		}

	}

	private void filterDialog(){
		if(filterAlert != null && filterAlert.isShowing()){
			filterAlert.cancel();
		}else if(filterAlert == null){
			View alertView = inflater.inflate(R.layout.search_plus, null);
			CheckBox official = (CheckBox)alertView.findViewById(R.id.search_fil_official);
			official.setChecked(filters.contains("official"));
			official.setOnCheckedChangeListener(new OnCheckedChangeListener(){
				@Override
				public void onCheckedChanged(CompoundButton buttonView,
						boolean isChecked) {
					if(isChecked){
						filters.add("official");
					}else{
						filters.remove("official");
					}
				}
			});
			CheckBox channel = (CheckBox)alertView.findViewById(R.id.search_fil_channel);
			channel.setChecked(filters.contains("channel"));
			channel.setOnCheckedChangeListener(new OnCheckedChangeListener(){
				@Override
				public void onCheckedChanged(CompoundButton buttonView,
						boolean isChecked) {
					if(isChecked){
						filters.add("channel");
					}else{
						filters.remove("channel");
					}
				}
			});
			CheckBox user = (CheckBox)alertView.findViewById(R.id.search_fil_community);
			user.setChecked(filters.contains("community"));
			user.setOnCheckedChangeListener(new OnCheckedChangeListener(){
				@Override
				public void onCheckedChanged(CompoundButton buttonView,
						boolean isChecked) {
					if(isChecked){
						filters.add("community");
					}else{
						filters.remove("community");
					}
				}
			});
			CheckBox nosame = (CheckBox)alertView.findViewById(R.id.search_fil_nocommunitygroup);
			nosame.setChecked(filters.contains("nocommunitygroup"));
			nosame.setOnCheckedChangeListener(new OnCheckedChangeListener(){
				@Override
				public void onCheckedChanged(CompoundButton buttonView,
						boolean isChecked) {
					if(isChecked){
						filters.add("nocommunitygroup");
					}else{
						filters.remove("nocommunitygroup");
					}
				}
			});
			CheckBox nomember = (CheckBox)alertView.findViewById(R.id.search_fil_hidecomonly);
			nomember.setChecked(filters.contains("hidecomonly"));
			nomember.setOnCheckedChangeListener(new OnCheckedChangeListener(){
				@Override
				public void onCheckedChanged(CompoundButton buttonView,
						boolean isChecked) {
					if(isChecked){
						filters.add("hidecomonly");
					}else{
						filters.remove("hidecomonly");
					}
				}
			});
			filterAlert = new AlertDialog.Builder(ACT)
			.setView(alertView)
			.create();
		}
		filterAlert.setOnCancelListener(new OnCancelListener(){
			@Override
			public void onCancel(DialogInterface dialog) {
				onReload();
			}
		});
		filterAlert.show();
	}

	public void createKeyWordURL(){
//		Log.d("LOG AAAAA "," " + sortIndex);
		//検索種類の決定
		String word = "";
		try {
			word = URLEncoder.encode(editTex.getText().toString(),"UTF-8");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
			return;
		}
		//モードの決定
		//カテゴリ検索では選べない
		String filter = "";
		String sort = "";//ソートの値はモードによって違う
		switch (liveModeIndex) {
		case 0:
			filter = "&filter=+:onair:";
			sort = getKeyWordSortValue_0();
			break;
		case 1:
			filter =  "&filter=+:reserved:";
			sort = getKeyWordSortValue_1();//未来はソートで使えるものが違ってくる
			break;
		case 2:
			filter =  "&filter=+:closed:";
			sort = getKeyWordSortValue_0();
			break;
		}
		for(int i = 0 ; i < filters.size(); i++){
			filter += "+:" + filters.get(i) + ":";
		}
		//&で挟む順番が関係あるので注意
		requestURL = URLEnum.SP_SEARCHQUESTION+"keyword=" + word  + sort + "&page=<<PAGEXXX>>"  + filter;
		Log.d("SearchTab","CREATE " + requestURL );
//		Log.d("SearchTab","CREATE " + tagURL );
	}

	private void createRankingURL(){
		//ランキングはキーワード検索と違ってどのモードでもソートは同じ
		//→というかモードとしているのは、ユーザー生||公式||チャンネル
		//過去
		String sort = getRankingSortValue();
		String mode = "";
		switch(liveModeIndex){
		case 0:
			mode = "?provider_type=community";
			break;
		case 1:
			mode = "?provider_type=official";
			break;
		case 2:
			mode = "?provider_type=channel";
			break;
		}
		if(sortIndex == 3){//rookieの場合ほぼ別のパーサみたいなもんになって、しかもprvider_typeつけると入ってこないのでめんどいのでなし(にこにこ広告も)
//		requestURL = URLEnum.RANKING + sort;
		}else{
			if(sortIndex == 2){
				tagURL = URLEnum.RANKING +"?type=closed";//過去の場合普通にprovider_type付けちゃうとHTMLのヘッダーとかは入ってこない
				new RankingClosedTask().execute();
				requestURL = URLEnum.RANKING + mode + sort + generateCloseValue;
			}else{
				generateList = new String[0];
				generateIndex = 0;
				generateButton.setText("");
			requestURL = URLEnum.RANKING + mode + sort;
			}
		}
	}

	private void createTimeTableURL(){
		if(timetableHref != null && timetableHref.length > 0){
			String[] d_o =  timetableHref[generateIndex < 0? 0:generateIndex].split("\\?");
			if(d_o.length >= 1){
			requestURL = URLEnum.TIMETABLE + "?p=<<PAGEXXX>>&" + d_o[1];
			}
		}else{
		requestURL = URLEnum.TIMETABLE;
		}
	}
	/**http://sp.live.nicovideo.jp/api/getTimetable?date=2014-4-16
	 * 基本検索タスク
	 * @author Owner
	 *
	 */
	public class SearchTask extends AsyncTask<Void, Void, Void> implements FinishCallBacks{
		private String URL;
		private boolean ENDFLAG = true;
		private ArrayList<LiveInfo> list;
		public SearchTask(String url) {
			addProgress();
			if(searchMode != 4)footer.setVisibility(View.VISIBLE);
			URL = url;
		}

		@Override
		protected Void doInBackground(Void... arg0) {
			//検索ページのパース
			if(NLiveRoid.isDebugMode)Log.d("NLiveRoid","SearchMode -------  " + searchMode  +" " + sortIndex + " " + URL);
			progressBar.setProgress(5);
//			String pcbann_template = "&track=nicolive_onair_keyword&search_mode=onair&target=onair&";

			try{
				if(URL == null){
					return null;
				}
				URL url = new URL(URL);
				HttpURLConnection con = (HttpURLConnection) url.openConnection();
				con.setRequestProperty("Cookie", Request.getSessionID(error));

				if(searchMode == 2){
					con.setRequestMethod("POST");
				}else{
					con.setRequestMethod("GET");
				}
				InputStream response = con.getInputStream();
				progressBar.setProgress(15);
				if(response == null){
					error.setErrorCode(-27);
					return null;
				}else if(error.getErrorCode() == -7) {
					// ここで-7でメンテか別の失敗かわからない
					return null;
				}
				if(NLiveRoid.isDebugMode)Log.d("NLiveRoid","CL " + con.getResponseCode() + " " + con.getContentLength() );
			progressBar.setProgress(20);
//			ACT.writeFile(response);
				  org.ccil.cowan.tagsoup.Parser parser = new org.ccil.cowan.tagsoup.Parser();
				  ContentHandler sHandler = null;
				if(searchMode == 0){
				   sHandler  = new CategoryParser(this,error);
				  }else if(searchMode == 1){//キーワード||ニコニコ電話||iPhone
				   sHandler = new KeyWordParser(this,error);
				  }else if(searchMode == 2){//ランキング
					 sHandler = new RankingParser(this,error);
				  }else if(searchMode == 3){//番組表
					  sHandler = new TimeTableParser(this,error);
				  }else if(searchMode == 4){//Channel
					  sHandler = new ChannelParser(this,error);
				  }else if(searchMode == 5){//Nsen
					  sHandler = new NsenParser(this,error);
				  }
			        parser.setContentHandler(sHandler);
			        parser.parse(new InputSource(response));
			  } catch (org.xml.sax.SAXNotRecognizedException e) {
			      // Should not happen.
				  e.printStackTrace();
			      throw new RuntimeException(e);
			  } catch (org.xml.sax.SAXNotSupportedException e) {
			      // Should not happen.
				  e.printStackTrace();
			      throw new RuntimeException(e);
			  } catch (UnknownHostException e) {
					if(error != null){
						error.setErrorCode(-3);
					e.printStackTrace();
				}
				}catch (IOException e) {
					error.setErrorCode(-27);
				  e.printStackTrace();
			} catch (SAXException e) {
				e.printStackTrace();
			}
			progressBar.setProgress(30);

			long startT = System.currentTimeMillis();
			while(ENDFLAG ){
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
					ENDFLAG = false;
					break;
				}catch(IllegalArgumentException e1){
					e1.printStackTrace();
					Log.d("NLiveRoid","IllegalArgumentException at SearchingTask");
					ENDFLAG = false;
					break;
				}
				if(System.currentTimeMillis() - startT > 40000){
					error.setErrorCode(-10);
					ENDFLAG = false;
					return null;
				}
			}
			Log.d("Log","SearchEND -------  " + searchMode );
			return null;
		}

		@Override
		protected void onPostExecute(Void arg) {
			progressBar.setProgress(80);
			Log.d("Log","SearchEND ------onPostExecute  " + searchMode + " "  + pageNum + " " +error.getErrorCode());
			if (list == null || error.getErrorCode() != 0){
				if(error.getErrorCode() == 10&&footer != null)footer.setVisibility(View.GONE);
				error.showErrorToast();
			}else if(list != null){
				 if(list.isEmpty()){
						 if(searchMode == 3){
							 Log.d("NLiveRoid","BANNGUMI " + pageNum);
							 if(pageNum == 0)adapter.clear();
								footer.setVisibility(View.GONE);
						 }else if(pageNum == 1){//番組表の時はページは0から始まっていて、データがある時は果てしなくある気がするのでFooterは別に出ちゃってもいいとしてここでは抜かす
							adapter.clear();
						 }else{//ここはスクロール終了と普通の検索0件の場合があるのでメッセージが微妙。
							footer.setVisibility(View.GONE);
							if(kupaText != null){
							kupaText.setText("検索結果");
							}
						 }
				}else{//リストが空じゃない
					if(searchMode != 3 && (pageNum <= 1||(tagURL != null && tagURL.matches("recent_tags?tab=")))){//カテゴリからタグ検索の場合、ページが無いみたい
						adapter.clear();
					}
					if(searchMode == 5){//Nsenは検索とか特に弄らない
						removeProgress();
						new AsyncTask<Void,Void,Void>(){
							@Override
							protected Void doInBackground(Void... params) {
								startFlashPlayer(list.get(0));
								return null;
							}
						}.execute();//この後onResumeでプログレスバーが表示されてちょいおかしくなる
						return;
					}
					//結局最後かどうか調べる為のHTMLの返却に法則が無い([<p class="error_message">表示できる番組はございません</p>]かページ数多くしても、最後の1件が常に返ってくる場合がある)ため
					//アダプター内で取ってきたリストのLVが全てある場合は、終了とする→元から2つ出てくるものもあるようなので、これがおかしくなるばあいもある
					ArrayList<String> lvs = new ArrayList<String>();
					for(int ind = 0; ind < list.size(); ind++){
						lvs.add( list.get(ind).getLiveID());
					}
						int j  = adapter.getCount()-list.size();
						try{
						if(lvs.size() > 0 && j > 0&&adapter.getCount() > j){
						if(NLiveRoid.isDebugMode)Log.d("NLiveRoid"," " + adapter.getCount() + " " + lvs.size() +" " +j);
							for(int ind = adapter.getCount()-1;j < ind  ; ind--){
								if(lvs.contains(adapter.getItem(ind).getLiveID())){
									if( j == ind){
										footer.setVisibility(View.GONE);
										removeProgress();
										kupaText.setText("検索結果");
										isFirstThread = false;
										return;
									}
								}else{
									break;
								}
							}
						}
						}catch(Exception e){
							e.printStackTrace();
						}
					for (LiveInfo i : list) {
						adapter.add(i);
					}
						//サムネイル取得
					long oneRowCount = 20/list.size();
					for(int i = 0; i < list.size(); i++){//adapter.getItem(i)とかの方がいい気もするけど
						progressBar.setProgress(80+i*(int)oneRowCount);
						new GETThumb(list).execute(i);
						}
					progressBar.setProgress(100);
					kupaText.setText("検索結果");
					isFirstThread = false;
					if(searchMode == 3){//番組表の場合、関連のとこに日付をセット
						if(generateList.length <= 1){
							generateButton.setText("");
						}else{
							if(generateIndex < 0){
								footer.setVisibility(View.GONE);
								generateButton.setText(generateList[0]);//スクロール時の動作の為、番組表初回はインデックスを-1にしたので
							}else{
						generateButton.setText(generateList[generateIndex]);
						footer.setVisibility(View.VISIBLE);
							}
						}
					}
				}
			}else{
				MyToast.customToastShow(ACT, "カテゴリ検索に失敗しました\n(仕様変更された又はネットワークエラー)");
			}
			removeProgress();
		}

		@Override
		public void onCancelled() {
			ENDFLAG = false;
			removeProgress();
			super.onCancelled();
		}
		@Override
		public void finishCallBack(ArrayList<LiveInfo> list) {
			if(NLiveRoid.isDebugMode)Log.d("NLiveRoid","ST finishCallBack" + (list == null? "list was NULL!!" : list.size()));
			ENDFLAG = false;
			this.list = list;
		}
		@Override
		public void finishCallBack(ArrayList<LiveInfo> liveInfos,
				LinkedHashMap<String, String> generate) {
			if(NLiveRoid.isDebugMode)Log.d("NLiveRoid","Normal finishCallBack" + (list == null? "list was NULL!!" : list.size()));

			ENDFLAG = false;
			this.list = liveInfos;
//			Log.d("GENERATE -- "," " + generate.size());
			if(generate == null || generate.size() <=0)return;
			Iterator<String> it = generate.keySet().iterator();
			generateList = new String[generate.size()];
			timetableHref = new String[generate.size()];
			for(int i = 0; i < generate.size(); i++){
			generateList[i] = it.next();
			timetableHref[i] =  generate.get(generateList[i]);
			}
		}

		@Override
		public void finishCallBack(ArrayList<LiveInfo> liveInfos, String pager) {
			// TODO 自動生成されたメソッド・スタブ

		}

	}// End of SearchTask


	public void writeFile(InputStream is) {
		try {
			Log.d("AAAAAAAAAAAAAAA","WRITE FILE " + is.available());
		} catch (IOException e1) {
			// TODO 自動生成された catch ブロック
			e1.printStackTrace();
		}
		FileOutputStream fos;
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			String temp = "";
			String source = "";
			int i = 0;
			while ((temp = br.readLine()) != null) {
//				i++;
				source += temp;
			}
			fos = new FileOutputStream(new File(getWiteHTMLFileStoragePath()));
			fos.write(source.getBytes());
			br.close();
			fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private String getWiteHTMLFileStoragePath(){
		boolean isStorageAvalable = false;
		boolean isStorageWriteable = false;
		String state = Environment.getExternalStorageState();
		if(state == null){
			MyToast.customToastShow(this, "SDカードが利用できない");
			return null;
		}else if (Environment.MEDIA_MOUNTED.equals(state)) {
		    //読み書きOK
		    isStorageAvalable = isStorageWriteable = true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
		    //読み込みだけOK
		    isStorageAvalable = true;
		    isStorageWriteable = false;
		} else {
			//ストレージが有効でない
		    isStorageAvalable = isStorageWriteable = false;
		}

		boolean notAvalable = !isStorageAvalable;
		boolean notWritable = !isStorageWriteable;
		if(notAvalable||notWritable){
			MyToast.customToastShow(this, "SDカードが利用できない");
			return null;
		}

		//sdcard直下に、パッケージ名のフォルダを作りファイルを生成
		String filePath = Environment.getExternalStorageDirectory().toString() + "/NLiveRoid";

		File directory = new File(filePath);
//		Log.d("log","filepath " + filePath + " \n isCANWRITE " + directory.canWrite());
		if(directory.mkdirs()){//すでにあった場合も失敗する
			Log.d("log","mkdir");
		}
		File file = new File(filePath,"test.html");
		if(!file.exists()){
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return file.getPath();
	}
	//サムネイル取得クラス
	class GETThumb extends AsyncTask<Integer,Void,Integer>{
		private ArrayList<LiveInfo> list;
		public GETThumb(ArrayList<LiveInfo> livelist){
			this.list = livelist;
		}
		@Override
		protected Integer doInBackground(Integer... arg0) {
			// サムネイルを取得
			Bitmap bm = null;
			if(list == null || list.get(arg0[0]) == null || list.get(arg0[0]).getCommunityID() == null){
				return -1;
			}
//			Log.d("NLiveRoid","GETTHUMBURL" + list.get(arg0[0]).getThumbnailURL());
			if(list.get(arg0[0]).getThumbnailURL().startsWith("http://")){//その他でサムネイルURL(初期値は"")があればそれで行く
				bm = Request.getImageForList(list.get(arg0[0]).getThumbnailURL(), error, 0);
			}else if(list.get(arg0[0]).getCommunityID().contains(URLEnum.HYPHEN)){//公式
				bm = Request.getImageForList(String.format(URLEnum.OFFICIALTHUMB,
						list.get(arg0[0]).getThumbnailURL()),error,0);
			}else if(list.get(arg0[0]).getCommunityID().contains("ch")){//チャンネル
				bm = Request.getImageForList(String.format(URLEnum.BITMAPSCHANNEL,
						list.get(arg0[0]).getCommunityID()),error,0);
			}else if(list.get(arg0[0]).getCommunityID().contains("co")){//ユーザー
				bm = Request.getImageForList(String.format(URLEnum.BITMAPSCOMMUNITY,
						list.get(arg0[0]).getCommunityID()),error,0);
			}else if(list.get(arg0[0]).getTitle().contains("ニコ生クルーズ")){
				bm = getLocalBitmap(R.drawable.cruise);//クルーズと世界の新着はローカル
			}else if(list.get(arg0[0]).getTitle().contains("世界の新着")){
				bm = getLocalBitmap(R.drawable.world_newmoview);
			}
			if(bm != null){
				list.get(arg0[0]).setThumbnail(bm);
			}else{
				list.get(arg0[0]).setThumbnail(BitmapFactory.decodeResource(getResources(), R.drawable.noimage));
				Log.d("NLiveRoid","S thumb no image" + arg0[0]);
			}
			return arg0[0];
		}

		protected void onPostExecute(Integer arg){
			if(arg == -1){
				MyToast.customToastShow(ACT, "サムネイル取得に失敗しました");
			}else{
					listview.invalidateViews();
		        if(arg == list.size()-1){
					removeProgress();
		        }
			}
		}
		/**
		 * トップパース時の公式のBitmapのローカル取得
		 *
		 * @param id
		 * @return
		 */
		private Bitmap getLocalBitmap(int id) {
			InputStream is = ACT.getResources().openRawResource(id);
			Bitmap bmp = null;
			try {
				bmp = BitmapFactory.decodeStream(is);
			} finally {
				try {
					is.close();
				} catch (IOException e) {
					// Ignore.
				}
			}
			return bmp;
		}
	}



	/*
	 * カテゴリ検索用パースタスク
	 *
	 */
	public class TagParseTask extends AsyncTask<Void,Void,Void>{
		private boolean ENDFLAG = true;
		private ArrayList<String> tags;
		@Override
		protected Void doInBackground(Void... arg0) {
			String sessionid = Request.getSessionID(error);

//			Log.d("Log","TAGURL -------  " + tagURL );

			InputStream response = Request
					.doGetToInputStreamFromFixedSession(sessionid,tagURL, error);
			progressBar.setProgress(15);
			if(response == null){
				error.setErrorCode(-12);
				return null;
			}
			if (response == null||error.getErrorCode() == -7) {
				// ここで-7でメンテか別の失敗かわからない
					return null;
			}
			progressBar.setProgress(20);
			  try {
				  org.ccil.cowan.tagsoup.Parser parser = new org.ccil.cowan.tagsoup.Parser();
				  SearchTagParser sHandler = new SearchTagParser(this,error);
			        parser.setContentHandler(sHandler);
			        parser.parse(new InputSource(response));
			  } catch (org.xml.sax.SAXNotRecognizedException e) {
			      // Should not happen.
				  e.printStackTrace();
			      throw new RuntimeException(e);
			  } catch (org.xml.sax.SAXNotSupportedException e) {
			      // Should not happen.
				  e.printStackTrace();
			      throw new RuntimeException(e);
			  } catch (IOException e) {
				  e.printStackTrace();
			} catch (SAXException e) {
				e.printStackTrace();
			}
			progressBar.setProgress(30);

			long timeOut = System.currentTimeMillis();
			while(ENDFLAG){
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					e.printStackTrace();
					ENDFLAG = false;
					break;
				}catch(IllegalArgumentException e1){
					e1.printStackTrace();
					Log.d("NLiveRoid","IllegalArgumentException at TagParseTask");
					ENDFLAG = false;
					break;
				}
				if((System.currentTimeMillis()-timeOut) > 40000){
					error.setErrorCode(-10);
					ENDFLAG = false;
					return null;
				}
			}
			return null;
		}

		public void finishCallBack(ArrayList<String> tags){
			ENDFLAG = false;
			this.tags = tags;
		}
		@Override
		protected void onPostExecute(Void arg){

			try{//ここの瞬間に配列の長さが増えることがあるのか?
			if(tags != null && !tags.isEmpty()){
//				if(tags.size() == 0){//ありえないかも抱けど
//					generatedList = new String[1];
//					generatedList[0] = "なし";
//					generateIndex = 0;
//				}
				generateList = new String[tags.size()];
			for(int i = 0; i < tags.size() ; i++){
				generateList[i] = tags.get(i);
				}
			generateButton.setText(generateList[0]);
			}
			}catch(ArrayIndexOutOfBoundsException e){
				Log.d("NLiveRoid","GENTAG OUT OF BOUNDS");
			}
			removeProgress();
		}
	}

	/*
	 * ランキング検索の過去の時に日付情報を取得する
	 *
	 */
	public class RankingClosedTask extends AsyncTask<Void,Void,Void>{
		private boolean ENDFLAG = true;
		private ArrayList<String> closedList;
		@Override
		protected Void doInBackground(Void... arg0) {
			String sessionid = Request.getSessionID(error);
			Log.d("Log","TAGURL -------  " + tagURL );

			InputStream response = Request
					.doGetToInputStreamFromFixedSession(sessionid,tagURL, error);
			progressBar.setProgress(15);
			if(response == null){
				error.setErrorCode(-12);
				return null;
			}
			if (response == null||error.getErrorCode() == -7) {
				// ここで-7でメンテか別の失敗かわからない
					return null;
			}
			progressBar.setProgress(20);
			  try {
				  org.ccil.cowan.tagsoup.Parser parser = new org.ccil.cowan.tagsoup.Parser();
				  RankingClosedParser sHandler = new RankingClosedParser(this, error);
			        parser.setContentHandler(sHandler);
			        parser.parse(new InputSource(response));
			  } catch (org.xml.sax.SAXNotRecognizedException e) {
			      // Should not happen.
				  e.printStackTrace();
			      throw new RuntimeException(e);
			  } catch (org.xml.sax.SAXNotSupportedException e) {
			      // Should not happen.
				  e.printStackTrace();
			      throw new RuntimeException(e);
			  } catch (IOException e) {
				  e.printStackTrace();
			} catch (SAXException e) {
				e.printStackTrace();
			}
			progressBar.setProgress(30);

			long timeOut = System.currentTimeMillis();
			while(ENDFLAG){
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					e.printStackTrace();
					ENDFLAG = false;
					break;
				}catch(IllegalArgumentException e1){
					e1.printStackTrace();
					Log.d("NLiveRoid","IllegalArgumentException at TagParseTask");
					ENDFLAG = false;
					break;
				}
				if((System.currentTimeMillis()-timeOut) > 40000){
					error.setErrorCode(-10);
					ENDFLAG = false;
					return null;
				}
			}
			return null;
		}

		public void finishCallBack(ArrayList<String> tags){
			ENDFLAG = false;
			this.closedList = tags;
		}
		@Override
		protected void onPostExecute(Void arg){
//			Log.d("TAGURL " ," ONPOST");
			try{//ここの瞬間に配列の長さが増えることがあるのか?
			if(closedList != null && !closedList.isEmpty()){
				generateList = new String[closedList.size()];
			for(int i = 0; i < closedList.size() ; i++){
				generateList[i] = closedList.get(i);
				}
			if(generateIndex >= generateList.length)generateIndex = 0;
			generateButton.setText(generateList[generateIndex]);
			}
			}catch(ArrayIndexOutOfBoundsException e){
				Log.d("NLiveRoid","GENTAG OUT OF BOUNDS");
			}
			removeProgress();
		}
	}


	class DrawerTouchListener implements OnTouchListener {
		private LinearLayout ll0;
		private LinearLayout ll1;
		private LinearLayout ll2;

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				if (isUped) {
					// 上がったリストを戻す
					reBackMoveList();
				} else {//検索結果欄の移動アニメーション
					// 移動基準の位置の算出
					Rect rect = new Rect();
					((View) kupaLinear)
							.getGlobalVisibleRect(rect);
					//ヘッダー部分の元の位置を記憶しておく
					linearPos = (float) (rect.top)
							/ ((NLiveRoid) getApplicationContext())
									.getViewHeight();
					if(back_t == null){
						listview.setBackgroundColor(Color.BLACK);
					}else{
						listview.setBackgroundDrawable(back_t);
					}

		// 検索ヘッダーとリストを親から切りはなして上のレイヤーに乗せる
		//くぱのタップする部分の親、プログレスバーの親、リストの親それぞれの親のLinearLayoutから切り離す
					((ViewGroup) listParent).removeView(listview);
					((ViewGroup) progressareaParent).removeView(progressArea);
					((ViewGroup) kupaLinearParent).removeView(kupaLinear);
		//新たなLinearLayoutに、それぞれ子を乗せる
					if(ll0 == null)ll0 = new LinearLayout(ACT);
					ll0.addView(kupaLinear);
					if(ll1 == null)ll1 = new LinearLayout(ACT);
					ll1.addView(progressArea);
					ll1.setPadding(0, kupaLinear.getHeight(), 0, 0);
					if(ll2 == null)ll2 = new LinearLayout(ACT);
					ll2.addView(listview);
					ll2.setPadding(0, kupaLinear.getHeight(), 0, 0);
					//それらを上のレイヤーに乗せる
					layer2.removeAllViews();
					layer2.addView(ll0);// ※後からaddされた方が上に来る
					layer2.addView(ll1);
					layer2.addView(ll2);
					//移動アニメーションで移動する
					TranslateAnimation animation = new TranslateAnimation(
							Animation.RELATIVE_TO_PARENT, 0.0f,
							Animation.RELATIVE_TO_PARENT, 0.0f,
							Animation.RELATIVE_TO_PARENT, linearPos,
							Animation.RELATIVE_TO_PARENT, 0.0f);
					animation.setDuration(500);
					animation.setFillAfter(true);
					layer2.startAnimation(animation);
					isUped = true;
				}
				return true;

			case MotionEvent.ACTION_MOVE:
				break;
			case MotionEvent.ACTION_UP:

				break;
			}

			return false;
		}
	};

/**
 * 上がったリストを元に戻す
 * @author Owner
 *
 *
 */

	private void reBackMoveList(){
		// 上がったリストを戻す
		//アニメーションの初期化
		TranslateAnimation animation = new TranslateAnimation(
				Animation.RELATIVE_TO_PARENT, 0.0f,
				Animation.RELATIVE_TO_PARENT, 0.0f,
				Animation.RELATIVE_TO_PARENT, 0.0f,
				Animation.RELATIVE_TO_PARENT, linearPos);
		animation.setDuration(500);
		animation.setFillAfter(true);
		animation.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationEnd(Animation arg0) {
				// レイヤ2から元のビューに戻す(しないと次からOnTouchイベントが起こらなくなる)
				((ViewGroup) listview.getParent()).removeView(listview);
				((ViewGroup) progressArea.getParent()).removeView(progressArea);
				((ViewGroup)kupaLinear.getParent()).removeView(kupaLinear);
				layer2.removeAllViews();
				kupaLinearParent
						.addView(kupaLinear);
				listParent
						.addView(listview);
				progressareaParent
						.addView(progressArea);
//				ViewGroup vg = (ViewGroup)layer2.getParent();
//				vg.removeView(layer2);
//				vg.addView(layer2);
				isUped = false;
			}

			@Override
			public void onAnimationRepeat(Animation arg0) {
			}

			@Override
			public void onAnimationStart(Animation arg0) {
			}
		});
			layer2.startAnimation(animation);
	}

	/**
	 * ランキングライナー サムネイル
	 * 放送ID
	 * コミュID
	 * タグライナー
	 * タイトル
	 * 開始時間
	 * @author Owner
	 *
	 */
	class SearchArrayAdapter extends ArrayAdapter<LiveInfo> {
		private NLiveRoid app;
		public SearchArrayAdapter(Context context) {
			super(context, R.layout.list_row_search);
		}
		@Override
		public View getView(int position, View paramView,
				ViewGroup paramViewGroup) {
			ViewHolder holder;
			View view = paramView;
			if(view == null){
				view = inflater.inflate(R.layout.list_row_search, null);
				TextView lv = (TextView)view.findViewById(R.id.search_list_liveid);
				TextView co = (TextView)view.findViewById(R.id.search_list_communityid);
				TextView livetitle = (TextView)view.findViewById(R.id.search_list_livetitle);
				TextView passedtime = (TextView)view.findViewById(R.id.search_list_starttime);
				ImageView thumbnail = (ImageView)view.findViewById(R.id.community_thumbnail);
				LinearLayout lbm = (LinearLayout)view.findViewById(R.id.search_list_tag_linear);
				FrameLayout fl = (FrameLayout)view.findViewById(R.id.search_list_progress_frame);
				TextView viewcount = (TextView)view.findViewById(R.id.search_list_viewcount);
				TextView rescount = (TextView)view.findViewById(R.id.search_list_rescount);

				TextView ranking_num = (TextView)view.findViewById(R.id.ranking_num);
				ImageView ranking_allow = (ImageView)view.findViewById(R.id.ranking_allow);
				TextView ranking_active = (TextView)view.findViewById(R.id.ranking_active);
				LinearLayout ranking_linear = (LinearLayout)view.findViewById(R.id.ranking_linear);
				setTextColor(lv,toptab_tcolor);
				setTextColor(livetitle,toptab_tcolor);
				setTextColor(co,toptab_tcolor);
				setTextColor(passedtime,toptab_tcolor);
				setTextColor(viewcount,toptab_tcolor);
				setTextColor(rescount,toptab_tcolor);
				setTextColor(ranking_num,toptab_tcolor);
				setTextColor(ranking_active,toptab_tcolor);
				holder = new ViewHolder();
				holder.lv = lv;
				holder.co = co;
				holder.title = livetitle;
				holder.commname_or_passedtime = passedtime;
				holder.thumbnail = thumbnail;
				holder.lbm = lbm;
				holder.fl = fl;
				holder.viewcount = viewcount;
				holder.rescount = rescount;
				holder.ranking_num = ranking_num;
				holder.ranking_allow = ranking_allow;
				holder.ranking_active = ranking_active;
				holder.ranking_linear = ranking_linear;
				holder.ranking_linear.setLayoutParams(new LinearLayout.LayoutParams(100,-1));
				app = ((NLiveRoid)ACT.getApplicationContext());

				holder.noimage = ACT.getResources().getDrawable(
						R.drawable.noimage);
				view.setTag(holder);
			}else{
				holder = (ViewHolder)view.getTag();
			}


			LiveInfo info = getItem(position);
			if(info != null){

				Bitmap t = info.getThumbnail();
				ViewGroup vg = (ViewGroup)view;
				if(t != null){
					vg.removeView(holder.fl);//フレームレイアウトのクルクルを消す
					holder.thumbnail.setImageBitmap(t);
				}else if(vg.getChildCount() == 1){//サムネ自体はremoveしてないので1
					holder.thumbnail.setImageDrawable(holder.noimage);
					vg.addView(holder.fl);
				}

				holder.lv.setText(info.getLiveID());
				holder.co.setText(info.getCommunityID());
				if(searchMode == 0 && categoryIndex > 0){//カテゴリ(トップ以外)の時はコミュニティ名を入れる
					holder.commname_or_passedtime.setText(info.getCommunityName());
					holder.title.setText(info.getTitle());
				}else if(searchMode == 4){//カテゴリ以外は開始時間
					holder.co.setText(info.getTitle());//チャンネル時はに説明を入れる
					holder.commname_or_passedtime.setText(info.getStartTime());
					holder.title.setText(info.getDescription());
				}else{
					holder.commname_or_passedtime.setText(info.getStartTime());
					holder.title.setText(info.getTitle());
				}
				String[] tags = info.getTags().split("<<TAGXXX>>");
				Bitmap tagsBm = null;
				holder.lbm.removeAllViews();
				for(int i = 0; i < tags.length; i++){//タグの画像有の場合
					if(tags[i].equals(" "))continue;
					tagsBm = app.getTagBitMap(tags[i]);//マップされているタグの画像を返す
					if(tagsBm != null){
						ImageView iv = new ImageView(ACT);//新たなivを生成してadd→setTag/getTagにいつかする
						iv.setImageBitmap(tagsBm);
						iv.setScaleType(ImageView.ScaleType.FIT_START);
						holder.lbm.addView(iv);
					}
				}

				if(searchMode == 2){//ランキング
					if(holder.ranking_linear.getVisibility() == View.GONE){
						holder.ranking_linear.setVisibility(View.VISIBLE);
						holder.fl.setPadding(100, 0, 0, 0);
					}
					holder.ranking_num.setText(String.valueOf(info.getRankingValue() & 0x00FF));
	//				Log.d("NLR -- "," " + info.getRankingValue());
					holder.ranking_allow.setImageBitmap(app.getRankingAloow(info.getRankingValue() >> 8));
					holder.commname_or_passedtime.setText(info.getPassedTime(false));
				}else{
					if(searchMode == 4){//チャンネルは放送中と未来のを区別する
						TextView tv = new TextView(ACT);//新たなivを生成してadd→setTag/getTagにいつかする
						setTextColor(tv,toptab_tcolor);
						tv.setText(info.isLiveStarted()? "放送中":"未来の放送");
						holder.lbm.addView(tv);
					}else if(searchMode == 0 ||searchMode == 1){//カテゴリ検索とキーワード検索は公式、チャンネル、ユーザーが違うアイコンだが一緒にしちゃう
					if(tagsBm == null){//ユーザーを追加
						if(info.getCommunityID().startsWith("co")){
							tagsBm = app.getTagBitMap("user");
							ImageView iv = new ImageView(ACT);
							iv.setImageBitmap(tagsBm);
							iv.setScaleType(ImageView.ScaleType.FIT_START);
							holder.lbm.addView(iv);
						}else if(info.getCommunityID().startsWith("ch")){
							tagsBm = app.getTagBitMap("channel");
							ImageView iv = new ImageView(ACT);
							iv.setImageBitmap(tagsBm);
							iv.setScaleType(ImageView.ScaleType.FIT_START);
							holder.lbm.addView(iv);
						}
					}
					holder.ranking_linear.setVisibility(View.GONE);
					holder.fl.setPadding(0, 0, 0, 0);
					}
				}
				if(liveModeIndex == 1){
				holder.viewcount.setText("予約数 " + info.getViewCount());
				holder.rescount.setText("コメ数 " + info.getResNumber());
				}else if(liveModeIndex == 0){
				holder.viewcount.setText("");
				holder.rescount.setText("");
				}else{
				holder.viewcount.setText("来場 " + info.getViewCount());
				holder.rescount.setText("コメ数 " + info.getResNumber());
				}
			}
			return view;
		}
	}

	private static class ViewHolder {
	    Drawable noimage;
	    ImageView thumbnail;
	    FrameLayout fl;

		TextView lv;
	    TextView co;
	    LinearLayout lbm;
		TextView title;
	    TextView commname_or_passedtime;
	    TextView viewcount;
	    TextView rescount;

	    LinearLayout ranking_linear;
	    TextView ranking_num;
	    ImageView ranking_allow;
	    TextView ranking_active;
	}

	/**
	 * キーイベント
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
	    outState.putString("WORKAROUND_FOR_BUG_19917_KEY", "WORKAROUND_FOR_BUG_19917_VALUE");
//	    super.onSaveInstanceState(outState);
	}
	@Override
	public boolean dispatchKeyEvent(KeyEvent keyevent){
		if(keyevent.getKeyCode() == KeyEvent.KEYCODE_BACK && keyevent.getAction() == KeyEvent.ACTION_DOWN)
		if(gate != null && gate.isOpened()){
			gate.close_noanimation();//外側からアニメーション起動するとなぜか重い
			return true;
		}else if(isUped){
			reBackMoveList();// 上がったリストを元に戻す
		return true;
		}
		return super.dispatchKeyEvent(keyevent);


	}
	/**
	 * コンテキストメニュー生成時処理
	 */
	public void onCreateContextMenu(ContextMenu menu, View view,
			ContextMenuInfo info) {
		super.onCreateContextMenu(menu, view, info);
		final AdapterContextMenuInfo adapterInfo = (AdapterContextMenuInfo) info;
		if(adapter.getCount() > adapterInfo.position ){
			if(searchMode == 2 && sortIndex == 3){//ランキングのルーキーはコミュIDしかない
				if(adapter.getItem(adapterInfo.position).getLiveID() == null && adapter.getItem(adapterInfo.position).getCommunityID() != null && !adapter.getItem(adapterInfo.position).getCommunityID().equals(URLEnum.HYPHEN)){//ランキングでrookieで検索書けた場合LVはない
					Intent commuTab = new Intent(this,TopTabs.class);
					commuTab.putExtra("scheme", adapter.getItem(adapterInfo.position).getCommunityID());
					commuTab.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(commuTab);
					return;
				}
			}
			showGate(adapter.getItem(adapterInfo.position));
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//		Log.d("log","RESULT SEARCH ------------ " + requestCode + " " + resultCode +" " + data);
		isListTaped = false;
		//クルーズの時のフラッシュ等からブラウザに行って帰ってきた時はerrorもnull この時は値も保存されない
		if (error == null || resultCode == CODE.RESULT_ALLFINISH) {
			return;
		}


	}

	/**
	 * adapterを取得します。
	 *
	 * @return adapter
	 */
	public ArrayAdapter<LiveInfo> getAdapter() {
		return adapter;
	}

	public void startRecognizeSearch(String text) {
		if(editTex != null){
			editTex.setText(text);
			searchMode = 1;
			categoryIndex = 0;
			categoryButton.setText("カテゴリ検索▼");
			kupaText.setText("検索中");
			CommunityTab.cancelMovingTask();//一旦全てキャンセル
			LiveTab.cancelMovingTask();
			cancelMoveingTask();//一旦全てキャンセル
			createKeyWordURL();
			try{
			searchTask = new SearchTask(requestURL.replaceAll("<<PAGEXXX>>", String.valueOf(1)));
			searchTask.execute();
			}catch(IllegalFormatConversionException e){
				MyToast.customToastShow(ACT, "検索できませんでした\nアプリ再起又は一定時間待ってからお試しください:code 9");
			}
		}
	}

	/**
	 * PC版のWebViewClient
	 */

  class PCSearchWVClient extends WebViewClient{
	private SearchTab ACT;
	private WebView wv;
	private Pattern lvpt = Pattern.compile("lv[0-9]+");
	private ProgressBar progress;
	public PCSearchWVClient(SearchTab ACT,WebView wv,ProgressBar progress){
		this.ACT = ACT;
		this.wv = wv;
		this.progress = progress;
	}
	@Override
	public void onPageStarted(WebView view, String url, Bitmap favicon) {
		Log.d("NLiveRoid","onPageStart"  + url);
		super.onPageStarted(view, url, favicon);
		progress.setVisibility(View.VISIBLE);
	}

	@Override
	public void onPageFinished(WebView view, String url) {
		Log.d("NLiveRoid"," PCWV onPageFinished" + url);
		progress.setVisibility(View.INVISIBLE);
		wv.requestFocus(View.FOCUS_DOWN);
	}
	@Override
	public boolean shouldOverrideUrlLoading(WebView view, String url) {
		Log.d("NLiveRoid"," shouldOverrideUrl" + url);
		Matcher mc = lvpt.matcher(url);
		if(mc.find()){
			wv.stopLoading();
			String lv = mc.group();
			if(NLiveRoid.isDebugMode)Log.d("NLiveRoid","PC WV  " + lv);
			LiveInfo liveI = new LiveInfo();
			liveI.setLiveID(lv);
			Intent commuTab = new Intent(ACT,TopTabs.class);
			commuTab.putExtra("scheme", lv);
			startActivity(commuTab);
		}else{
			wv.loadUrl(url);
		}
		return true;
	}

}


	@Override
	public void allCommFunction(int index, LiveInfo info) {
		if(NLiveRoid.isDebugMode)Log.d("NLiveRoid","S allCommFunction ---" + info.getLiveID());
		switch(index){
	case 0://Gate→放送履歴→TSを視聴から呼ばれる
	case 10:
		if(gate != null && gate.isOpened())gate.close_noanimation();
		if(info.getLiveID() != null){
			Intent commuTab = new Intent(this,TopTabs.class);
			commuTab.putExtra("scheme", info.getLiveID());
			commuTab.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(commuTab);
			return;
		}
		break;
		}
	}


	/**
	 * isPCSearchを取得します。
	 * @return isPCSearch
	 */
	public boolean isPCSearch() {
	    return isPCSearch;
	}


	@Override
	public void showGate(LiveInfo liveObj) {
		NLiveRoid app = (NLiveRoid)getApplicationContext();//シンプルじゃない、嫌い
		//ここのセッションは、一番元のログインの物であるはずなので、そのままRequest.getSessionIDでおｋなはず
		GateView gView = app.getGateView();
		if(gView == null)return;
		gate = new Gate(this,gView,liveObj,false,Request.getSessionID(error));
		ViewGroup gateParent = (ViewGroup) app.getGateView().getView().getParent();
		if(gateParent != null){
			gateParent.removeView(app.getGateView().getView());
		}
		((ViewGroup)parent.getParent()).addView(app.getGateView().getView());
		gate.show(this.getResources().getConfiguration());
	}



}

