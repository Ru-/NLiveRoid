package nliveroid.nlr.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import nliveroid.nlr.main.parser.XMLparser;

import org.apache.http.ParseException;
import org.xmlpull.v1.XmlPullParserException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.os.Process;
import android.speech.RecognizerIntent;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.ClipboardManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.BadTokenException;
import android.view.WindowManager.LayoutParams;
import android.webkit.ConsoleMessage;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TextView;

public class FlashPlayer extends Activity implements CommentPostable,
		 HandleNamable{
	private static FlashPlayer ACT;
	private static WebView wv;
	private static int webViewW = 0;// spplayerで必要
	private static int webViewH = 0;
	private static int pixcelViewW = 0;// spplayerで必要
	private static int pixcelViewH = 0;
	private float density = 0;
	private int cellHeight = 0;

	private static int resizeW;
	private static int resizeH;

	private LiveInfo liveInfo;
	private boolean[] setting_boolean;
	private byte[] setting_byte;

	private Intent overlay;
	private boolean keepActivity = true;

	private static CommentTable commentTable;
	private static CommandMapping cmd;

	private static ProgressDialog dialog;


	private BroadcastReceiver audioReceiver;

	private ErrorCode error;
	private View parent;

	private Intent getIntent;

	private boolean isOverlayStarted;

	// 同一レイヤーフィールド
	private LayoutInflater inflater;
	/** 描画開始座標：Y軸 **/
	private int firstcurrentY = 50;
	private int firstcurrentX = 0;
	/** タッチ座標：Y軸 **/
	private int firstoffsetY = 0;
	private int firstoffsetX = 0;
	// 2点目の座標変数
	/** 描画開始座標：Y軸 **/
	private int secondcurrentY = 50;
	/** タッチ座標：Y軸 **/
	private int secondoffsetY = 0;
	// xは変える必要がない

	private ExCommentListAdapter adapter;
	private ViewGroup rootFrame;
	private boolean isFirstMoving = false;
	private boolean isSecondMoving = false;
	/** テーブルのリスト類 */
	private ListView listview;
	private LinearLayout bufferMark;
	private LinearLayout headerview;
	private LinearLayout listBlank;
	private LinearLayout headerBlank;
	/** OverLayView **/
	private LinearLayout firstBlueHeader;
	private LinearLayout firstBlueBlank;
	private LinearLayout secondBlueHeader;
	private LinearLayout secondBlank;
	private int list_bottom;
	private int list_width;
	private static boolean isUplayout;
	private boolean isLayerChanged;
	// コテハンの配列
	private String handleFileName = "handlenames.xml";
	private Map<String, String> idToHandleName;
	private Map<String, Integer> idToBgColor;
	private Map<String, Integer> idToForeColor;

	private String tempID;
	// 設定フラグ
	private boolean enable_moveX;
	private boolean enable_moveY;

	private LinearLayout postArea;
	private static EditText postET;
	private Button postB;
	private CheckBox post_184;
	private Button post_command;
	private Button post_update;
	private Button post_desc;
	private CheckBox post_cdisp;
	private Button voiceInput;
	private Button post_menu;

	private int recognizeValue = 0;

	private AutoUpdateTask autoUpdateTask;
	private boolean AUTO_FLAG = true;

	private static boolean isScrollEnd = true;
	private static boolean tempIsScrollEnd = true;

	private static boolean isPortLayt;

	private boolean isJSLoaded = false;

	private Gate gate;
	private boolean isAt;
	private boolean isAtoverwrite;
	private int row_resource_id = R.layout.comment_row;
	private byte[] column_seq;
	private int[] column_ids;

	final private byte listSize = 7;
	private boolean isSetNameReady;//コテハン読み込み完了フラグ
	private float heightAdjust = 2;

	private AlertDialog listDialog;
	private QuickDialog quickDialog;
	private boolean isFullScreend;
	private boolean doDelay;

	private SurfaceView hlsSurface;

	private boolean noCommentServer;
	private MediaPlayer mMediaPlayer;
	private String[] hlsValues;
	private boolean hasActiveHolder;

	private AsyncTask<Void,Void,Void> hbLoopTask;
	private TweetDialog tweetDialog;

	private String debuglogStr;

	/**
	 * バックグラウンドでビジーなタスクが実行された時 onPauseもonDestroyも呼ばれずにプロセスがkillされる
	 * その時の復帰は新たにPIDを取得され、onCreateが呼ばれる
	 *
	 * 最初にOverLayを呼ぶときはこのクラスのonResumeは呼ばれない
	 *
	 * OverLayはバックグラウンドから復帰する際に まずonResumeが呼ばれ、上記のようにこのクラスのonCreateが呼ばれ
	 * 表示する設定によって
	 * OverLayのonPauseが呼ばれた後に、このクラスからOverLayのonCreate、OverLayのonResumeと呼ばれる
	 * (singleTopなので)
	 */

	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
		Window window = getWindow();
		window.setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		if (NLiveRoid.apiLevel >= 11) {
			window.setFlags(
					WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
					WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
		}

		ACT = this;
		LayoutInflater inflater = LayoutInflater.from(ACT);
		parent = inflater.inflate(R.layout.flashplayer, null);
		setContentView(parent);
		// 着信時に視聴画面を殺す(OverLayを呼ぶ(layer_numが0)でも、BACKキーでPlayerにした後着信すると入らなくて困るので、全表示モードでセット)
		TelephonyManager mTelephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
			@Override
			public void onCallStateChanged(int state, String number) {
				if (state == TelephonyManager.CALL_STATE_RINGING) {
					// 着信 又は通話中に視聴画面をを落とす
					Log.d("NLiveRoid",
							"Detect ringing  and call finish player activity");
					ACT.standardFinish();
				}
			}
		};
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		mTelephonyManager.listen(mPhoneStateListener,
				PhoneStateListener.LISTEN_CALL_STATE);
		getWindow().addFlags(
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		NLiveRoid app = (NLiveRoid) getApplicationContext();
//		if(app.apiLevel >= 11){//3.0以降未満でやると重すぎる場合がある
//			// プロセスの優先度を上げる
//			 android.os.Process.setThreadPriority(
//			 android.os.Process.myPid());
//		}
		app.initNoTagBitmap();
		app.setForeACT(ACT);
		error = app.getError();
		getIntent = getIntent();
		setting_boolean = getIntent.getBooleanArrayExtra("setting_boolean");// アラートからだとここでnull
		setting_byte = getIntent.getByteArrayExtra("setting_byte");// アラートからだとここでnull
		new AudioTask().execute();
		if(setting_boolean == null){
			errorFinish(CODE.RESULT_FLASH_ERROR, -43);
		}
			new InitFlashPlayer().execute();
	}

	@Override
	public void onWindowFocusChanged(boolean hasfocus) {
		getWindow().setSoftInputMode(
				LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		if(postET != null && postET.getWidth() > 0)postET.setWidth(postET.getWidth());//これしないと豪いことになる
		if(postB != null && postB.getWidth() > 0)postB.setWidth(postB.getWidth());//これしないと豪いことになる
		// ノティフィケーションの場合、別プロセスなので、
		// ここで決めないといけない
		// ノティフィの場合、Flashは別aplicationvで、ウィンドウサイズはTopTabがレイアウトされた時にとってるから
		// このプロセスはここでビューのサイズを求める

	}

	/**
	 * 初期化を別スレッドにするf
	 *
	 * @author Owner
	 *
	 */
	class InitFlashPlayer extends AsyncTask<Void, Void, Integer> {
		private BitmapDrawable drawable;

		@Override
		protected void onPreExecute() {
			if (dialog == null) {
				dialog = ProgressDialog.show(ACT, "",
						"Loading live information..", true, true);
				dialog.setContentView(R.layout.progress_dialog_flash);
				dialog.show();
			}
		}
		@Override
		public void onCancelled() {
			if (dialog != null && dialog.isShowing()) {
				dialog.dismiss();
				dialog = null;
			}
			super.onCancelled();
		}
		@Override
		protected Integer doInBackground(Void... arg) {
			if(NLiveRoid.isDebugMode){
				Log.d("NLiveRoid"," InitFlash");
			}
			NLiveRoid app = (NLiveRoid) getApplicationContext();// onCreateでnoTagBitmapで初期化している
			// 共通処理
			// 再開とかでnullにならないようにセットしておく
			overlay = new Intent(ACT, OverLay.class);
			overlay.putExtra("pid", Process.myPid());
			if (error.getErrorCode() != 0) {// 設定ファイルの読み込みに失敗
				return -1;
			}
			if(setting_byte[37] > 0){//オフタイマーセットされていたら
				long start_t = getIntent.getLongExtra("offtimer_start", 0);//履歴からの場合、offtimer_startが0になっている
				if(start_t == 0 || (System.currentTimeMillis() - start_t)/1000 > setting_byte[37]*60){//履歴からの起動ですでにオフタイマー時間を経過していた
					Intent topTab = new Intent(ACT,TopTabs.class);
					topTab.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					ACT.startActivity(topTab);
					if(NLiveRoid.isDebugMode)Log.d("NLiveRoid","F OFFTIMER ERROR");
					errorFinish(CODE.RESULT_FLASH_ERROR,-47);
					return -2;//何もしない
				}
			}
			// 背景画像があればインスタンス化する
			try {
				FileInputStream back_v = ACT.openFileInput("back_v");
				// Log.d("File IS ---- ",""+back_v);
				if (back_v != null) {
					Bitmap back = BitmapFactory.decodeStream(back_v);
					drawable = new BitmapDrawable(back);
				}
			} catch (FileNotFoundException e) {
				// e.printStackTrace();
				// 画像なし
			}
			//LiveInfoと画面サイズを取得
				liveInfo = (LiveInfo) getIntent
						.getSerializableExtra("LiveInfo");
				webViewW = getIntent.getIntExtra("viewW", getWindowManager()
						.getDefaultDisplay().getWidth());
				webViewH = getIntent.getIntExtra("viewH", getWindowManager()
						.getDefaultDisplay().getHeight());
				density = getIntent.getFloatExtra("density", 1.5F);
				app.setViewHeightDp(webViewH);// 別プロセスなので今の所詳細開いた時のみに必要0.8.62
				app.setViewWidthDp(webViewW);
				app.setMetrics(density);
				resizeW = getIntent
						.getIntExtra("resizeW",
								(int) (getWindowManager()
										.getDefaultDisplay()
										.getWidth() * 1.72D));
				resizeH = getIntent
						.getIntExtra("resizeH",
								(int) (getWindowManager()
										.getDefaultDisplay()
										.getHeight() * 1.8D));
			if (liveInfo == null || liveInfo.getLiveID() == null) {// 新着失敗から取得できなかった||ノティフィから取得失敗の場合に起こる
				Log.d("FlashPlayer", "get LiveInfo failed" + liveInfo);
				return -1;
			}
			// コマンドオブジェクトの初期化 ここでliveinfo.isOwner()があること
			cmd = (CommandMapping) getIntent.getSerializableExtra("cmd");
			if (cmd == null) {
				String[] cmdValue = new String[4];
				cmdValue[0] = app.getDetailsMapValue("cmd_cmd");
				cmdValue[1] = app.getDetailsMapValue("cmd_size");
				cmdValue[2] = app.getDetailsMapValue("cmd_color");
				cmdValue[3] = app.getDetailsMapValue("cmd_align");
				for (int i = 0; i < 4; i++) {
					if (cmdValue[i] != null) {
						if (i == 3) {
							cmd = new CommandMapping(cmdValue[0], cmdValue[1],
									cmdValue[2], cmdValue[3], false);
							break;
						}
					}
					if (i == 3) {// 1つでもnullがあったら普通の初期化
						cmd = new CommandMapping(false);
					}
				}
			}
			column_seq = getIntent.getByteArrayExtra("column_seq");

			String session = getIntent.getStringExtra("Cookie");
			final Matcher mc = Pattern.compile(
					"user_session=user_session_[0-9]+_([0-9]|[a-z])+").matcher(
					session);
			if (session == null || mc == null) {
				ACT.errorFinish(CODE.RESULT_FLASH_ERROR, -43);
				return 0;
			}
			byte[] liveStatus_for_hls = null;
			if (mc.find()) {// 通常のセッションOKなら、WVサイズとWVを初期化する
				//WebViewの初期化 webViewの横幅で使用するのでここでインテントから貰う
					// キャストで端が多少少なくなる?
					pixcelViewW = (int) (getIntent.getIntExtra("viewW", ACT
							.getWindowManager().getDefaultDisplay().getWidth()) * density);
					pixcelViewH = (int) (getIntent.getIntExtra("viewH", ACT
							.getWindowManager().getDefaultDisplay().getHeight()) * density);

				if(setting_byte[43] == 2){//HLSのURL取得と、メディアプレイヤーの初期化
					Log.d("NLiveRoid" , " HLS --- ");
					hlsValues = new String[5];//SP_SESSION_KEY , HeatBeatUrl , HeatBeatKey , PlayListURL , nico_hls_session
					if(getIntent.getStringExtra("sp_session") != null){
						hlsValues[0] = getIntent.getStringExtra("sp_session");
					}else{
						hlsValues[0] = Request.getSPSession(error);//無かったらここで取得
						if(error.getErrorCode() != 0){
							return -100;
						}
						getIntent.putExtra("sp_session", hlsValues[0]);
					}
					//HLSの場合は、ここで放送取得できるかどうかを判定する
					try {
						if(NLiveRoid.isDebugMode)Log.d("NLiveRoid","HLS FgetPlayer ");
						liveStatus_for_hls = Request.getPlayerStatusToByteArray(
								liveInfo.getLiveID(), error,
								session);
						Log.d("NLiveRoid","HLS FgetPlayer " + liveStatus_for_hls);
					} catch (Exception e) {
						e.printStackTrace();
						return -3;
					}
					String check = null;
					try {
						check = new String(liveStatus_for_hls, "UTF-8");
						Log.d("NLiveRoid","First HLS CHECK " + check);
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
					if (check == null){
						return -30;
					}else if(check.contains("status=\"fail\"")||check.contains("status=\"error\"")){
						if (check.contains("notlogin")) {// 他にもinvalid_lv(数値じゃない場合など)notfound(不明)
							return -4;
						} else if (check.contains("closed")) {
							return -5;
						} else if (check.contains("comingsoon")) {
							return -6;
						} else if (check.contains("require_community_member")) {
							return -7;
						} else if (check.contains("incorrect_account_data")) {// アカウント無しでも見れる放送でなる(コメサバへ繋げない)
							return -8;
						}  else if (check.contains("timeshift_ticket_exhaust")) {//TS視聴にチケットが必要なもの
							return -9;
						} else if (check.contains("usertimeshift")) {//コミュ限でTS
							return -7;
						} else if (check.contains("noauth")) {//チャンネルで終了していてTS提供されてない放送でなる
							return -5;
						} else if(check.contains("require_accept_print_timeshift_ticket")){

						} else if(check.contains("full")){///満席
							return -10;
						}
					}
					//取得できない放送の場合ここでエラー(コミュ限、R-18等)
					String[] hlsURLs = Request.getHLSURLs(liveInfo.getLiveID(),error);
					if(error.getErrorCode() != 0){
						return -100;
					}
					hlsValues[1] = hlsURLs[1];
					hlsValues[2] = hlsURLs[3];
					hlsValues[3] = hlsURLs[2];
					hlsValues[4] = Request.getHLSSession(hlsValues,error);
					if(error.getErrorCode() != 0){
						return -100;
					}
					hbLoopTask = new HBLoop();//HBをループしておく
					hbLoopTask.execute();
				}

				ACT.runOnUiThread(new Runnable(){
					@Override
					public void run(){
						Log.d("NLiveRoid" , " sessionFinded run");
						if (drawable != null)
							parent.findViewById(R.id.flash_root_linear).setBackgroundDrawable(
									drawable);//背景セット

						if (setting_boolean[21]) {
							getWindow().addFlags(
									WindowManager.LayoutParams.FLAG_FULLSCREEN);
						}
						// コメントのみでもVisibleをGoneにするからここでロードしておく
						wv = (WebView) parent.findViewById(R.id.player);
						// バッファアイコンは遅延実行かどうかに関わらずどちらでも使うので初期化layer_num0の時非表示にする為ここで初期化
						bufferMark = (LinearLayout) parent
								.findViewById(R.id.buffering_area);
						if(setting_byte[31] == 0){
						bufferMark.setVisibility(View.GONE);
						}
						// コメントだけ取得かどうか
						if (setting_byte[31] == 3 ) {// コメントのみ取得時
							wv.setVisibility(View.INVISIBLE);
							if (dialog != null)
								dialog.dismiss();
						} else if( setting_byte[43] == 2){//HLS
							hlsSurface = (SurfaceView)parent.findViewById(R.id.player_s);
							hlsSurface.setVisibility(View.VISIBLE);
							wv.setVisibility(View.GONE);
							layoutPlayer();
						}else {
							// keepscreenはフラッシュではWebViewがアクティブになると自動的にONになってしまう(.keepScreenOn(false)にしても)
							// ので、フラッシュが立ち上がる前にしかScreenOffできないので強制ONにしておく
							loadWV(mc.group());
						}

						// 画面固定の設定
						setOrientation(setting_byte[24]);
						Log.d("NLiveRoid" , " END OF run");
					}
				});

				try {// 念のため、セッションはここでもOverLayでもチェック
					if(setting_boolean[26]&&setting_byte[31] != 3&&setting_byte[43] != 2){//遅延実行ならonPageFinishedでここから先をやる
						doDelay  = true;
						return 0;
					}
						//コメント欄の初期化
						short init_comment_count = getIntent.getShortExtra(
								"init_comment_count", (short) 20);
						if(NLiveRoid.isDebugMode)Log.d("NLiveRoid","F initT " + setting_byte[31]);
						if (setting_byte[31] == 0) {//前面
							// オーバーレイを起動する
							try {
								liveInfo.serializeBitmap();
								if (overlay == null) {
									overlay = new Intent(ACT, OverLay.class);
								}
								if (cmd != null) {
									overlay.putExtra("cmd", cmd);
								}
								overlay.putExtra("pid", Process.myPid());
								overlay.putExtra("setting_boolean",
										setting_boolean);
								overlay.putExtra("setting_byte", setting_byte);
								overlay.putExtra("init_comment_count",
										init_comment_count);
								overlay.putExtra("speech_skip_word", getIntent
										.getStringExtra("speech_skip_word"));
								overlay.putExtra("isnsen", getIntent.getBooleanExtra("isnsen", false));
								overlay.putExtra("column_seq", column_seq);
								overlay.putExtra("twitterToken", getIntent.getStringExtra("twitterToken"));
								overlay.putExtra("Cookie", session);
								overlay.putExtra("sp_session", getIntent.getStringExtra("sp_session"));
								overlay.putExtra("viewW", getIntent
										.getIntExtra("viewW",
												getWindowManager()
														.getDefaultDisplay()
														.getWidth()));
								overlay.putExtra("viewH", getIntent
										.getIntExtra("viewH",
												getWindowManager()
														.getDefaultDisplay()
														.getHeight()));
								overlay.putExtra("density", getIntent
										.getFloatExtra("density", 1.5F));
								overlay.putExtra("notification", getIntent
										.getBooleanExtra("notification", false));
								overlay.putExtra("LiveInfo", liveInfo);
								overlay.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
								isOverlayStarted = true;
								startActivityForResult(overlay,
										CODE.REQUEST_OVERLAY);

							} catch (RuntimeException e) {
								Log.d("NLiveRoid", "FLASH ERROR RUNTIME ");
								e.printStackTrace();
							}

						} else if (setting_byte[31] == 2) {// Playerのみ
						} else if (setting_byte[31] == 1
								|| setting_byte[31] == 3) {// 背面,コメントのみ
							// gateを普通に表示する為
							app.createGateInstance();
							new SameLayerModeStart().execute(liveStatus_for_hls);
						}// End of setting_byte[31] == if


				} catch (PatternSyntaxException e) {
					Log.d("NLiveRoid",
							"INIT PLAYER FAILDED PATTERN MISSMATCH CODE : 0");
					MyToast.customToastShow(app,
							"プレイヤーの初期化に失敗しました\nパターンミスマッチ code:0");
					ACT.standardFinish();
				} catch (IllegalArgumentException e) {
					Log.d("NLiveRoid",
							"INIT PLAYER FAILDED PATTERN MISSMATCH CODE : 1");
					MyToast.customToastShow(app,
							"プレイヤーの初期化に失敗しました\nパターンミスマッチ code:1");
					ACT.standardFinish();
				} catch (IndexOutOfBoundsException e) {
					Log.d("NLiveRoid",
							"INIT PLAYER FAILDED MATCHER MISSMATCH CODE : 0");
					MyToast.customToastShow(app,
							"プレイヤーの初期化に失敗しました\nセッションミスマッチ code:0");
					ACT.standardFinish();
				}
		}else{//mc.find()
			ACT.errorFinish(CODE.RESULT_FLASH_ERROR, -4);
		}
			return 0;
	}// End of doInBack

		@Override
		protected void onPostExecute(Integer errorVal) {
			if (errorVal == -1) {
				if (dialog != null && dialog.isShowing()) {
					dialog.dismiss();
					dialog = null;
					}
				ACT.errorFinish(CODE.RESULT_FLASH_ERROR, -8);
			}else if(errorVal == -100){
				if (dialog != null && dialog.isShowing()) {
					dialog.dismiss();
					dialog = null;
					}
				if(error.getErrorCode() == -52){
					ACT.errorFinish(CODE.RESULT_FLASH_ERROR, -52);
				}else{
				error.showErrorToast();
				}
			}else if(setting_byte[43] == 2){
				if (dialog != null && dialog.isShowing()) {
					dialog.dismiss();
					dialog = null;
					}
				if (errorVal == -4) {
					ACT.errorFinish(CODE.RESULT_FLASH_ERROR, -17);
				} else if (errorVal == -5) {
					ACT.errorFinish(CODE.RESULT_FLASH_ERROR, -18);
				} else if (errorVal == -6) {
					ACT.errorFinish(CODE.RESULT_FLASH_ERROR, -42);
				} else if (errorVal == -7) {
					ACT.errorFinish(CODE.RESULT_FLASH_ERROR, -20);
				} else if (errorVal == -8) {
					ACT.errorFinish(CODE.RESULT_FLASH_ERROR, -17);
				} else if (errorVal == -9) {
					ACT.errorFinish(CODE.RESULT_FLASH_ERROR, -50);
				} else if (errorVal == -10) {
					ACT.errorFinish(CODE.RESULT_FLASH_ERROR, -54);
				}else{
				loadMP();
				}
			}
		}
	}

	class HBLoop extends AsyncTask<Void,Void,Void>{
		private boolean HBLOOP = true;
		@Override
		public void onCancelled(){
			super.onCancelled();
			HBLOOP = false;
		}
		@Override
		protected Void doInBackground(Void... params) {
			while(HBLOOP){
			try {
				HttpURLConnection con = (HttpURLConnection) new URL(hlsValues[1]).openConnection();
				con.setRequestProperty("User-Agent",Request.user_agent);
				con.setRequestProperty("Cookie", hlsValues[4] +"; "+hlsValues[0]);
//				con.setRequestProperty("X-Nicovideo-Connection-Type", "wifi\r\n");
				con.setRequestProperty("content-type", "application/x-www-form-urlencoded");
				con.setRequestMethod("GET");

				Log.d("NLiveRoid " , " hbLoopTask " + con.getResponseCode());
//				Map<String,List<String>> list = con.getHeaderFields();
//				Iterator<String> it = list.keySet().iterator();
//				for(int i = 0;  it.hasNext(); i++){
//					String key = it.next();
//					Log.d("NO HHHHH "," " + key);
//					Log.d("NO HHHHH  " , " " + list.get(key));
//				}
					InputStream is = con.getInputStream();
//					BufferedReader br = new BufferedReader(new InputStreamReader(is));
//					String temp = null;
//					String str = "";
//					while ((temp = br.readLine()) != null) {
//						str += temp;
//					}
//					Log.d(" " , "SOURCE --- " + str);
					is.close();
					con.disconnect();
					Thread.sleep(60000);
				} catch (MalformedURLException e) {
					// TODO 自動生成された catch ブロック
					e.printStackTrace();
				} catch (IOException e) {
					// TODO 自動生成された catch ブロック
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO 自動生成された catch ブロック
					e.printStackTrace();
				}
			}
			return null;
		}

	}
	private void loadMP(){
		        try {//test "http://49.212.39.17/mario/mario.m3u8"
		           if(mMediaPlayer == null)mMediaPlayer = new MediaPlayer();
					Map<String,String> map = new HashMap<String,String>();
					map.put("Cookie", hlsValues[4]);
//					map.put("X-Nicovideo-Connection-Type", "wifi\r\n");
					map.put("User-Agent",Request.user_agent);
					map.put("Request Method","GET");
		           if(NLiveRoid.apiLevel >= 14){
		        	   //APIレベル14
//		            mMediaPlayer.setDataSource(this.getApplicationContext(),Uri.parse(hlsValues[3]),map);
		           }else{
		        	   // Use java reflection call the hide API:
		        	   Method method = mMediaPlayer.getClass().getMethod("setDataSource", new Class[] { Context.class, Uri.class, Map.class });
		        	   method.invoke(mMediaPlayer, new Object[] {this.getApplicationContext(), Uri.parse(hlsValues[3]), map});
		           }
		    		SurfaceHolder holder = hlsSurface.getHolder();
		    	    holder.addCallback(new SurfaceHolder.Callback(){
						@Override
						public void surfaceChanged(SurfaceHolder holder,
								int format, int width, int height) {
							Log.d("NLiveRoid","surfaceChanged");
						}
						@Override
						public void surfaceCreated(SurfaceHolder holder) {
//							synchronized (this) {
//						        hasActiveHolder = true;
//						        this.notifyAll();
//						     }
							Log.d("NLiveRoid","surfaceCreated");
						}
						@Override
						public void surfaceDestroyed(SurfaceHolder holder) {
//							synchronized (this) {
//						        hasActiveHolder = false;
//						        synchronized(this)          {
//						              this.notifyAll();
//						        }
//						    }
							Log.d("NLiveRoid","surfaceDestroyed");
						}
		    	    });
		    	    synchronized (this) {
//		    	        while (!hasActiveHolder) {
//		    	              try {
//								this.wait();
//		    	              } catch (InterruptedException e) {
//		    	                //Print something
		    	              }
//		    	        }
			    	    holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
			            mMediaPlayer.setDisplay(holder);
			            mMediaPlayer.prepare();
//		    	    }
		            mMediaPlayer.setWakeMode(this, PowerManager.PARTIAL_WAKE_LOCK);
		            mMediaPlayer.setScreenOnWhilePlaying(true);
		            mMediaPlayer.setOnCompletionListener(new OnCompletionListener(){
						@Override
						public void onCompletion(MediaPlayer arg0) {
				            Log.e("NLiveRoid", "onCompletion:------- ");
				           new RetryMP().execute();
//							MyToast.customToastShow(ACT, "プレイヤーが切断したのでリトライします");
						}
		            });
		            mMediaPlayer.setOnErrorListener(new OnErrorListener(){
						@Override
						public boolean onError(MediaPlayer mp, int what,
								int extra) {
				            Log.e("NLiveRoid", "onError:------- ");
							MyToast.customToastShow(ACT, "再生でエラーが発生しました" + what + " " + extra);
					           new RetryMP().execute();
					           return false;
						}
		            });
		            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		        } catch (Exception e) {
		        	e.printStackTrace();
		            Log.e("NLiveRoid", "ExceptionMP ----- " + e.getMessage(), e);
					MyToast.customToastShow(ACT, "プレイヤーでエラーが発生\nAndroidのバージョンが古いかもしれません" +e.getMessage());
		        }
		        mMediaPlayer.start();
	}
	class RetryMP extends AsyncTask<Void,Void,Void>{

		@Override
		protected Void doInBackground(Void... params) {
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			try{
			loadMP();
			}catch(Exception e){
				e.printStackTrace();
			}
			return null;
		}

	}
	private void loadWV(String sessionid) {
		// WebViewの起動
		if (wv == null) {
			wv = (WebView) parent.findViewById(R.id.player);
		}
		if(NLiveRoid.isDebugMode){
			wv.setWebChromeClient(new WebChromeClient() {
		 public boolean onConsoleMessage(ConsoleMessage cm) {
		 Log.d("NLiveRoid", cm.message() + " -- From line "
		 + cm.lineNumber() + " of "
		 + cm.sourceId() );
		 return true;
		 }
		 });
		}

		WebSettings settings = wv.getSettings();
		settings.setJavaScriptEnabled(true);
		settings.setPluginsEnabled(true);
		settings.setDatabaseEnabled(false);
		wv.setWebViewClient(new FlashWebClient());
		wv.addJavascriptInterface(new WVJS(), "MyJS");
		wv.setVerticalScrollbarOverlay(false);
		wv.setVerticalScrollBarEnabled(false);
		wv.setHorizontalScrollbarOverlay(false);
		wv.setHorizontalScrollBarEnabled(false);
		settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
		settings.setSupportZoom(true);
		settings.setJavaScriptCanOpenWindowsAutomatically(true);
		if(NLiveRoid.apiLevel >=11){
		settings.setAllowContentAccess(true);
		}
		settings.setAllowFileAccess(true);
		settings.setSaveFormData(true);
		settings.setSavePassword(true);
		//画面ずれるの結局できなかった
		CookieSyncManager coo = null;
		try {
			coo = CookieSyncManager.getInstance();
			if (coo == null)
				CookieSyncManager.createInstance(ACT);
		} catch (IllegalStateException e) {// エラーレポートに何故かあった
			e.printStackTrace();
			CookieSyncManager.createInstance(ACT);
		}
		coo.startSync();
		CookieManager cooMan = CookieManager.getInstance();
		if(sessionid == null)sessionid = getIntent.getStringExtra("Cookie");
		cooMan.setCookie(
				"nicovideo.jp",
				sessionid
						+ "; expires=Sun, 18-Mar-2015 14:25:29 GMT; path=/; domain=.nicovideo.jp");

		if (setting_byte == null) {
			if (getIntent == null)
				getIntent = getIntent();
			setting_byte = getIntent.getByteArrayExtra("setting_byte");
		}
//		Log.d("NLiveRoid", "QUALITY "
//				+ (setting_byte[34] == 0 ? "low"
//						: setting_byte[34] == 1 ? "middle" : "high"));
		String url = "";
		if(getIntent.getBooleanExtra("isnsen", false)){
			url = URLEnum.NSENPLAYER.replace("%LIVEID%",
					liveInfo.getLiveID());
			url.replaceAll("%QUALITY%", setting_byte[34] == 0 ? "low"
					: setting_byte[34] == 1 ? "medium" : "high");
			wv.loadDataWithBaseURL(
					URLEnum.SP_WATCHBASEURL + liveInfo.getLiveID(), url,
					"text/html", "utf-8", null);
		}else{
			switch(setting_byte[43]){
			case 0:
				url = URLEnum.SPPLAYER.replace("%LIVEID%",
						liveInfo.getLiveID());
				url.replaceAll("%QUALITY%", setting_byte[34] == 0 ? "low"
						: setting_byte[34] == 1 ? "medium" : "high");
				wv.loadDataWithBaseURL(
						URLEnum.SP_WATCHBASEURL + liveInfo.getLiveID(), url,
						"text/html", "utf-8", null);
			break;
			case 1:
				url = URLEnum.PCPLAYER.replace("%LIVEID%",
						liveInfo.getLiveID());
				url.replaceAll("%QUALITY%", setting_byte[34] == 0 ? "low"
						: setting_byte[34] == 1 ? "medium" : "high");
				wv.loadDataWithBaseURL(
						URLEnum.PC_WATCHBASEURL + liveInfo.getLiveID(), url,
						"text/html", "utf-8", null);
			break;
			case 3://HLS

				break;
			}
		}
		Log.d("NLiveRoid","END OF loadWV ");
	}

	class SameLayerModeStart extends AsyncTask<byte[], Void, Integer> {

		@Override
		protected Integer doInBackground(byte[]... arg) {
			if (setting_boolean == null) {
				if (getIntent == null)
					getIntent = getIntent();
				setting_boolean = getIntent
						.getBooleanArrayExtra("setting_boolean");
			}
			if (setting_byte == null) {
				if (getIntent == null)
					getIntent = getIntent();
				setting_byte = getIntent.getByteArrayExtra("setting_byte");
			}
			idToHandleName = new ConcurrentHashMap<String, String>();
			idToBgColor = new ConcurrentHashMap<String, Integer>();
			idToForeColor = new ConcurrentHashMap<String, Integer>();
			// コメ欄の高さを調整する
			switch (setting_byte[35]) {
			case 1:
				heightAdjust = 3.5F;
				break;
			case 2:
				heightAdjust = 2.8F;
				break;
			case 3:
				heightAdjust = 2.4F;
				break;
			case 4:
				heightAdjust = 2;
				break;
			case 5:
				heightAdjust = 1.8F;
				break;
			}
			if (setting_byte[43] == 2) {//HLSならすでにgetPlayerに行っている
				Log.d("NLiveRoid","SameLayerHLS "  + arg[0]);
				if(arg[0] == null){
					return -11;
				}else{
					try {
						Log.d("NLiveRoid","F HLS CHECK");
						XMLparser.getLiveInfoFromAPIByteArray(arg[0],
								liveInfo);
						if(getIntent.getStringExtra("isPreLooked") != null && liveInfo != null && liveInfo.getIsPremium() != null && liveInfo.getIsPremium().equals("0")){//ブロキャス
							Intent isPre = new Intent();
							isPre.setAction("bindTop.NLR");
							isPre.putExtra("isPre", true);
							ACT.sendBroadcast(isPre);
						}
					} catch (NullPointerException e1) {
						e1.printStackTrace();
						error.setErrorCode(-37);
						return -100;//画面は落とさない
					} catch (ParseException e1) {
						e1.printStackTrace();
						error.setErrorCode(-37);
						return -100;//画面は落とさない
					} catch (XmlPullParserException e1) {
						e1.printStackTrace();
						String check = new String(arg[0]);
						Log.d("NLiveRoid F HLS FAILED?", " " + check);
						if(check.contains("<getplayerstatus")){//最初に余計な文章が入っている場合があるかも(よよ)
							Log.d("NLiveRoid"," HLS CH " + check.indexOf("<getplayerstatus"));
							check = check.substring(check.indexOf("<getplayerstatus"));
							try {
								XMLparser.getLiveInfoFromAPIString(check, liveInfo);
							} catch (Exception e) {
								e.printStackTrace();
								error.setErrorCode(-49);
								return -100;//画面は落とさない
							}
						}else{
							noCommentServer = true;
							error.setErrorCode(-30);//コメント取得できない公式・CH
						return -100;//画面は落とさない
						}
					} catch (IOException e1) {
						e1.printStackTrace();
						error.setErrorCode(-37);
						return -100;//画面は落とさない
					}
				}
				density = getIntent.getFloatExtra("density", 1.5f);
				if (setting_boolean[1]) {
					row_resource_id = R.layout.newline_row;
					column_ids = new int[] { R.id.nseq0, R.id.nseq1,
							R.id.nseq2, R.id.nseq3, R.id.nseq4,
							R.id.nseq5, R.id.nseq6 };
					// column_width = new byte[]{setting_byte[]};
				} else {
					column_ids = new int[] { R.id.seq0, R.id.seq1,
							R.id.seq2, R.id.seq3, R.id.seq4, R.id.seq5,
							R.id.seq6 };
					// column_width = new byte[]{setting_byte[]};
				}
				isAt = setting_boolean[6];
				isAtoverwrite = setting_boolean[7];
				initTable();
				return 0;
			} else {// 放送情報のポート番号がない場合、getPlayerに取りに行き、ステータス分岐

				byte[] liveStatus = null;
				try {
					String session = getIntent.getStringExtra("Cookie");

					liveStatus = Request.getPlayerStatusToByteArray(
							liveInfo.getLiveID(), error,
							session);
				} catch (Exception e) {
					e.printStackTrace();
					return -3;
				}
				if (liveStatus == null) {
					return -3;
				}
				String check = null;
				try {
					check = new String(liveStatus, "UTF-8");
					Log.d("NLiveRoid","CHECK " + check);
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				if (check == null){
					return -30;
				}else if(check.contains("status=\"fail\"")||check.contains("status=\"error\"")){
					if (check.contains("notlogin")) {// 他にもinvalid_lv(数値じゃない場合など)notfound(不明)
						return -4;
					} else if (check.contains("closed")) {
						return -5;
					} else if (check.contains("comingsoon")) {
						return -6;
					} else if (check.contains("require_community_member")) {
						return -7;
					} else if (check.contains("incorrect_account_data")) {// アカウント無しでも見れる放送でなる(コメサバへ繋げない)
						return -8;
					}  else if (check.contains("timeshift_ticket_exhaust")) {//TS視聴にチケットが必要なもの
						return -9;
					} else if (check.contains("usertimeshift")) {//コミュ限でTS
						return -7;
					} else if (check.contains("noauth")) {//チャンネルで終了していてTS提供されてない放送でなる
						return -5;
					}
				}else if (error != null && error.getErrorCode() == 0) {
					if (check.length() < 100) {
						Log.d("NLiveRoid", "failed_comment");// 100文字以下なら失敗の可能性が高い
							if (check.equals("SocketException")) {// ネットワークエラー
								return -10;
							}else{
								Log.d("NLiveRoid", "F failed_comment" + check);// 100文字以下なら失敗の可能性が高い
							}
					} else {
						try {
							Log.d("NLiveRoid","FCHECK");
							XMLparser.getLiveInfoFromAPIByteArray(liveStatus,
									liveInfo);
							if(getIntent.getStringExtra("isPreLooked") != null && liveInfo != null && liveInfo.getIsPremium() != null && liveInfo.getIsPremium().equals("0")){//ブロキャス
								Intent isPre = new Intent();
								isPre.setAction("bindTop.NLR");
								isPre.putExtra("isPre", true);
								ACT.sendBroadcast(isPre);
							}
						} catch (NullPointerException e1) {
							e1.printStackTrace();
							error.setErrorCode(-37);
							return -100;//画面は落とさない
						} catch (ParseException e1) {
							e1.printStackTrace();
							error.setErrorCode(-37);
							return -100;//画面は落とさない
						} catch (XmlPullParserException e1) {
							e1.printStackTrace();
							Log.d("NLiveRoid F", " " + check);
							if(check.contains("<getplayerstatus")){//最初に余計な文章が入っている場合があるかも(よよ)
								Log.d("NLiveRoid"," CCC " + check.indexOf("<getplayerstatus"));
								check = check.substring(check.indexOf("<getplayerstatus"));
								try {
									XMLparser.getLiveInfoFromAPIString(check, liveInfo);
								} catch (Exception e) {
									e.printStackTrace();
									error.setErrorCode(-49);
									return -100;//画面は落とさない
								}
							}else{
								noCommentServer = true;
								error.setErrorCode(-30);//コメント取得できない公式・CH
							return -100;//画面は落とさない
							}
						} catch (IOException e1) {
							e1.printStackTrace();
							error.setErrorCode(-37);
							return -100;//画面は落とさない
						}
						// 成功してたらコメ欄初期化----------------
						// ポストエリア等より先に判定が必要

						density = getIntent.getFloatExtra("density", 1.5f);
						if (setting_boolean[1]) {
							row_resource_id = R.layout.newline_row;
							column_ids = new int[] { R.id.nseq0, R.id.nseq1,
									R.id.nseq2, R.id.nseq3, R.id.nseq4,
									R.id.nseq5, R.id.nseq6 };
							// column_width = new byte[]{setting_byte[]};
						} else {
							column_ids = new int[] { R.id.seq0, R.id.seq1,
									R.id.seq2, R.id.seq3, R.id.seq4, R.id.seq5,
									R.id.seq6 };
							// column_width = new byte[]{setting_byte[]};
						}
						isAt = setting_boolean[6];
						isAtoverwrite = setting_boolean[7];
						initTable();
						return 0;
					}
				}
			}
			return 0;
		}

		private void initTable() {

			if (pixcelViewW > pixcelViewH) {
				int temp = pixcelViewW;
				pixcelViewW = pixcelViewH;
				pixcelViewH = temp;
			}

			// テーブル設定の読み込み 上表示、X移動可、Y移動可
			// ポストエリア等より先に判定が必要
			if (setting_boolean == null) {
				if (getIntent == null)
					getIntent = getIntent();
				setting_boolean = getIntent
						.getBooleanArrayExtra("setting_boolean");
			}
			if (setting_byte == null) {
				if (getIntent == null)
					getIntent = getIntent();
				setting_byte = getIntent.getByteArrayExtra("setting_byte");
				;
			}
			// テーブルの位置決定
			Configuration config = getResources().getConfiguration();
			if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
				isPortLayt = false;
				firstcurrentX = (int) ((double) pixcelViewH * (setting_byte[19] * 0.01D));
				firstcurrentY = (int) ((double) pixcelViewW * (setting_byte[20] * 0.01D));
				list_bottom = (int) ((double) pixcelViewW * (setting_byte[21] * 0.01D));
				list_width = (int) ((double) pixcelViewH * (setting_byte[39] * 0.01D));
				cellHeight = (int) (pixcelViewH * setting_byte[18] * 0.01D);
				enable_moveX = setting_boolean[10];
				enable_moveY = setting_boolean[11];
			} else {
				isPortLayt = true;
				firstcurrentX = (int) ((double) pixcelViewW * (setting_byte[8] * 0.01D));
				firstcurrentY = (int) ((double) pixcelViewH * (setting_byte[9] * 0.01D));
				list_bottom = (int) ((double) pixcelViewH * (setting_byte[10] * 0.01D));
				list_width = (int) ((double) pixcelViewW * (setting_byte[38] * 0.01D));
				cellHeight = (int) (pixcelViewH * setting_byte[7] * 0.01D);
				enable_moveX = setting_boolean[8];
				enable_moveY = setting_boolean[9];
			}
		}

		@Override
		protected void onPostExecute(Integer arg) {
			if (dialog != null && dialog.isShowing()) {
				dialog.dismiss();
				dialog = null;
				}
			if (arg == 0) {// 設定ファイル読み込み失敗する(不明)
				inflater = LayoutInflater.from(ACT);
				if(noCommentServer)return;
				new ReadHandleName().execute();// 失敗時トースト表示するのでここで読み込み
				// アダプタの初期化
				adapter = new ExCommentListAdapter(ACT);

				if (setting_byte == null) {
					if (getIntent == null)
						getIntent = getIntent();
					setting_byte = getIntent.getByteArrayExtra("setting_byte");
				}
				init(liveInfo);
				// アクティビティ終了しないでエラーだけを表示+必要ならここ独自に終了
				// finishするならここでトースト表示できない
			} else if (arg == -3) {
				ACT.errorFinish(CODE.RESULT_FLASH_ERROR, -8);
			} else if (arg == -4) {
				ACT.errorFinish(CODE.RESULT_FLASH_ERROR, -17);
			} else if (arg == -5) {
				ACT.errorFinish(CODE.RESULT_FLASH_ERROR, -18);
			} else if (arg == -6) {
				ACT.errorFinish(CODE.RESULT_FLASH_ERROR, -42);
			} else if (arg == -7) {
				ACT.errorFinish(CODE.RESULT_FLASH_ERROR, -20);
			} else if (arg == -8) {
				ACT.errorFinish(CODE.RESULT_FLASH_ERROR, -17);
			} else if (arg == -9) {
				ACT.errorFinish(CODE.RESULT_FLASH_ERROR, -50);
			}else if (arg == -10) {
				ACT.errorFinish(CODE.RESULT_FLASH_ERROR, -41);
			}else if(arg == -11){
				ACT.errorFinish(CODE.RESULT_FLASH_ERROR, -43);
			}else if(arg == -100){
				error.showErrorToast();
			}

		}
	}

	// UIスレッドばかりなのでバックグラウンドにできない
	private void init(LiveInfo liveinfo) {

		byte[] bitmaparray = liveinfo.getBitmapArray();
		if (bitmaparray != null) {
			Bitmap bitmap = BitmapFactory.decodeByteArray(bitmaparray, 0,
					bitmaparray.length);
			liveinfo.setThumbnail(bitmap);
		}

		String session = getIntent.getStringExtra("Cookie");
		if (liveinfo != null && session != null) {// コメント取得を開始する
			new StartCommentTable(liveinfo, session, true).execute();
		}
		// 重なりを考慮してポストエリアとバッファとリストのレイアウト
		if (setting_boolean[2]) {// フォーム上
			postArea = (LinearLayout) parent.findViewById(R.id.postArea_up);
			postET = (EditText) parent.findViewById(R.id.postarea_edit_up);
			postET.setFocusable(true);
			postET.clearFocus();
			postB = (Button) parent.findViewById(R.id.postarea_commit_up);
			postB.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					postComment();
				}
			});

			if(setting_boolean[3]){//イベントリスナでもここを聞いてるけどifの記述上しかたない
				post_184 = (CheckBox)parent.findViewById(R.id.postarea_184_up);
			post_command = (Button)parent.findViewById(R.id.postarea_command_up);
			post_update = (Button)parent.findViewById(R.id.postarea_update_up);
			post_menu = (Button)parent.findViewById(R.id.postarea_menukey_up);
			post_cdisp = (CheckBox)parent.findViewById(R.id.postarea_commentdisp_up);
			voiceInput = (Button) parent.findViewById(R.id.postarea_voiceinput_up);
			post_desc = (Button)parent.findViewById(R.id.postarea_desc_up);
			post_cdisp = (CheckBox)parent.findViewById(R.id.postarea_commentdisp_up);
			((ViewGroup)post_184.getParent()).setMinimumHeight((int) (30/((NLiveRoid)getApplicationContext()).getMetrics()));
			setFormListeners();
			}
		} else {
			postArea = (LinearLayout) parent.findViewById(R.id.postArea_buttom);
			postET = (EditText) parent.findViewById(R.id.postarea_edit_down);
			postET.setFocusable(true);
			postET.clearFocus();
			postB = (Button) parent.findViewById(R.id.postarea_commit_down);
			postB.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					// すぐに結果を取得
					Log.d("NLIveRoid","TESTNICOSPC");
					wv.loadUrl(""
							+ "javascript:"
							+ "MyJS.log("
							+ "NicoSPC.functions.getSetting('showComment').toString()+','+"
							+ "NicoSPC.functions.getSetting('ng184').toString()+','+"
							+ "NicoSPC.functions.getSetting('showBSPComment').toString()+','+"
							+ "NicoSPC.functions.getSetting('isMute').toString()+','+"
							+ "NicoSPC.functions.getSetting('loadSmile').toString()+','+"
							+ "NicoSPC.functions.getSetting('volumeSub').toString())");
					postComment();
				}
			});
			if(setting_boolean[3]){//イベントリスナでもここを聞いてるけどifの記述上しかたない
				post_184 = (CheckBox)parent.findViewById(R.id.postarea_184_down);
				post_command = (Button)parent.findViewById(R.id.postarea_command_down);
				post_update = (Button)parent.findViewById(R.id.postarea_update_down);
				post_cdisp = (CheckBox)parent.findViewById(R.id.postarea_commentdisp_down);
				post_desc = (Button)parent.findViewById(R.id.postarea_desc_down);
				voiceInput = (Button) parent
						.findViewById(R.id.postarea_voiceinput_down);
				post_menu = (Button)parent.findViewById(R.id.postarea_menukey_down);
				setFormListeners();
			}
		}

		postArea.setVisibility(View.GONE);//初期時は非表示
		((ViewGroup) parent).removeView(bufferMark);

		// リストの初期化
		rootFrame = (ViewGroup) parent.findViewById(R.id.list_parent_liner)
				.getParent();
		// list_bottomがマイナスだったらupLayoutでヘッダを下に配置
		// リストをブランクで上に調整
		// リスト左上を示す位置(firstcurrentY)は変えずにヘッダーを描画
		// アドする順も大事
		// リストのパッディングもisUpかどうかで変わる
		headerBlank = getHeaderBlank();
		headerview = getHeader();
		if(headerview == null){
			errorFinish(CODE.RESULT_FLASH_ERROR, -13);//コメサバに接続できてない(非対応CH・公式)時になる
			return;
		}
		firstBlueHeader = getBlueHeader();
		secondBlueHeader = getSimpleBlueHeader();
		listview = getList();
		// リストの最新のコメントが画面を超える場合、最新のコメントを画面範囲内にするのがどうしてもできなかった
		/*
		 * if(viewHeight <= firstcurrentY+list_bottom){//下がはみだす list_bottom =
		 * viewHeight-firstcurrentY-cellHeight; } list_bottom -=
		 * list_bottom%cellHeight; とかやってもフォントサイズが小さい時に何故かできない
		 */
		if (list_bottom < 0) {// 方向上
			isUplayout = true;
			listBlank = getListBlank_Up();
			rootFrame.addView(listBlank, new LinearLayout.LayoutParams(-1, -2));
			listBlank.addView(listview, new LinearLayout.LayoutParams(list_width,
					-list_bottom));
		} else {// 方向下
			isUplayout = false;
			listBlank = getListBlank_Down();
			listview.setPadding(0, cellHeight, 0, 0);
			rootFrame.addView(listBlank, new LinearLayout.LayoutParams(-1, -2));
			listBlank.addView(listview, new LinearLayout.LayoutParams(list_width,
					list_bottom));
		}
		rootFrame.addView(headerBlank, new LinearLayout.LayoutParams(-1, -2));
		headerBlank.addView(headerview, new LinearLayout.LayoutParams(list_width, -2));
		// バッファをリストの上にアド
		rootFrame.addView(bufferMark);

		// Foregroundに来るようにポスト一式を改めて普通のビューにアドし直す
		((ViewGroup) postArea.getParent()).removeView((postArea));
		((ViewGroup) parent).addView(postArea);
		postArea.clearFocus();// ソフトキー出なくなるのを回避

		if(NLiveRoid.isDebugMode){
			AudioManager audio = (AudioManager) getSystemService(ACT.AUDIO_SERVICE);
			Log.d("NLiveRoid"," Vol"+audio.getStreamVolume(AudioManager.STREAM_MUSIC));
		}
	}

	private void setFormListeners() {
		if(getCmd().getValue(CommandKey.CMD).equals("184")){
			post_184.setChecked(true);
		}else{
			post_184.setChecked(false);
		}
		post_184.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if(isChecked){
					setCmd(CommandKey.CMD, "184");
				}else{
					setCmd(CommandKey.CMD, "");
				}
			}
		});
		post_command.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View view) {
				showCommandDialog();
			}
		});
		post_update.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View view) {
				if (setting_byte[31] == 1) {
					new OperationDialog(ACT, setting_boolean[13], setting_byte[31])
							.showSelf();
				} else {// ここで呼ばれる場合は、プレイヤーのみ
					new OperationDialog(ACT, setting_boolean[13], (byte) 2).showSelf();
				}
			}
		});
		post_cdisp.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View view) {
				if(listview.getVisibility() != View.VISIBLE){
					listview.setVisibility(View.VISIBLE);
					if(headerview != null)headerview.setVisibility(View.VISIBLE);
				}else{
					listview.setVisibility(View.GONE);
					if(headerview != null)headerview.setVisibility(View.GONE);
				}
			}
		});
		post_desc.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View view) {
				if (liveInfo == null || liveInfo.getLiveID() == null) {
					MyToast.customToastShow(ACT, "読み込み中又は放送情報の取得に失敗している");
				}else{
				showLiveDescription();
				}
			}
		});
		voiceInput.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(isFullScreend){
					isFullScreend = false;
					setOrientation(setting_byte[24]);
					if(!setting_boolean[21]){//ステータスバーも戻しておく
						getWindow().clearFlags(
								WindowManager.LayoutParams.FLAG_FULLSCREEN);
					}
					layoutPlayer();
				}else{
				quickAction(3);
				}
			}

		});
		post_menu.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View view) {
				if(quickDialog != null && quickDialog.isShowing()){
					quickDialog.cancel();
				}else{
				quickDialog = new QuickDialog(ACT,setting_byte,setting_boolean[19]);
				quickDialog.showSelf(setting_byte[40],setting_byte[41]);
				}
			}
		});
	}

	class ExCommentListAdapter extends CommentListAdapter {
		public ExCommentListAdapter(Context context) {
			super(context);
		}

		@Override
		public View getView(int position, View paramView,
				ViewGroup paramViewGroup) {
			// nullの時だけ処理を行うと、更新してaddされた部分に情報が使いまわされてしまうので毎回ビューの情報を更新する
			String[] cellValue = getItem(position);

			ViewHolder holder;
			View view = paramView;
			if (view == null) {
				view = inflater.inflate(row_resource_id, null);
				holder = new ViewHolder();// リソースを順序どおりに割り当てる→リソースIDとCommentTableCellがマップしているので、以降はカスタム列順でholder#tvsが並んでいる形になる
				for (byte i = 0; i < listSize; i++) {
					holder.columnTvs[i] = (TextView) view
							.findViewById(column_ids[i]);
					if(column_seq[i] == 1)holder.id_index = i;
				}
				view.setTag(holder);
			} else {
				holder = (ViewHolder) view.getTag();
			}

			if (cellValue != null) {// 列入れ替えは、セット済みとする
				for (int i = 0; i < listSize && holder.columnTvs[i] != null; i++) {//テキストを列順に合わせてセット
					holder.columnTvs[i].setText(cellValue[column_seq[i]]);
				}
				if (idToHandleName.containsKey(cellValue[1])) {//まずコテハンだったら塗る
					holder.columnTvs[holder.id_index].setText(idToHandleName
							.get(cellValue[1]));
					for (int i = 0; i < listSize && holder.columnTvs[i] != null; i++) {
						holder.columnTvs[i].setBackgroundColor(idToBgColor
								.get(cellValue[1]));
						holder.columnTvs[i].setTextColor(idToForeColor
								.get(cellValue[1]));
					}
				} else if(setting_boolean[27]){//コテハン以外の背景とテキスト色を設定通りにする
					if (cellValue[0].equals("主")) {//主かどうかはテキスト色のみに反映
						for (int i = 0; i < listSize && holder.columnTvs[i] != null; i++) {
							holder.columnTvs[i].setBackgroundColor(Color.BLACK);
							holder.columnTvs[i].setTextColor(-1039790);
						}
					}else{
						for (int i = 0; i < listSize && holder.columnTvs[i] != null; i++) {
							holder.columnTvs[i].setBackgroundColor(Color.BLACK);
							holder.columnTvs[i].setTextColor(Color.WHITE);
						}
					}
				} else {
					if (cellValue[0].equals("主")) {//主かどうかはテキスト色のみに反映
						for (int i = 0; i < listSize && holder.columnTvs[i] != null; i++) {
							holder.columnTvs[i].setBackgroundColor(Color.WHITE);
							holder.columnTvs[i].setTextColor(-1039790);
						}
					}else{
						for (int i = 0; i < listSize && holder.columnTvs[i] != null; i++) {
							holder.columnTvs[i].setBackgroundColor(Color.WHITE);
							holder.columnTvs[i].setTextColor(Color.BLACK);
						}
					}
				}
			}
			return view;
		}

		@Override
		public void insert(String[] row, int index) {
			super.insert(row, index);
			if (isScrollEnd) {
				listview.setSelection(0);
			}
		}

		public void addRow(String[] str) {
			super.add(str);
			if (isScrollEnd) {
				listview.setSelection(listview.getCount());
			}
		}
	}// End of CommentListAdapter

	class StartCommentTable extends AsyncTask<Void, Void, Void> {
		private LiveInfo tempInfo;
		private ErrorCode threadError;
		private String session;

		StartCommentTable(LiveInfo lv, String session, boolean isVisibleTable) {
			this.tempInfo = lv;
			this.session = session;
			threadError = ((NLiveRoid) getApplicationContext()).getError();
		}

		@Override
		protected Void doInBackground(Void... arg0) {
			if (commentTable != null) {
				if (autoUpdateTask != null
						&& autoUpdateTask.getStatus() != AsyncTask.Status.FINISHED) {
					autoUpdateTask.cancel(true);
				}
				commentTable.closeMainConnection();
			}
			if (setting_boolean == null) {
				if (getIntent == null)
					getIntent = getIntent();
				setting_boolean = getIntent
						.getBooleanArrayExtra("setting_boolean");
			}
			if (setting_byte == null) {
				if (getIntent == null)
					getIntent = getIntent();
				setting_byte = getIntent.getByteArrayExtra("setting_byte");
			}
			if(tempInfo.getPort() == null){
				error.setErrorCode(-13);
				return null;
			}
			if (NLiveRoid.apiLevel >= 8) {
				commentTable = new CommentTable((byte) 0, tempInfo, ACT,
						adapter, column_seq, threadError, session,
						setting_byte[33], setting_boolean[12],
						setting_byte[33] == 3 ? setting_byte[28]
								: setting_byte[27], setting_byte[26],
						setting_byte[36],
						getIntent.getStringExtra("speech_skip_word"),
						setting_byte[29], getIntent.getShortExtra(
								"init_comment_count", (short) 20),
						setting_boolean[19]);
			} else {
				commentTable = new CommentTable((byte) 1, tempInfo, ACT,
						adapter, column_seq, threadError, session,
						setting_byte[33], setting_boolean[12],
						setting_byte[33] == 3 ? setting_byte[28]
								: setting_byte[27], setting_byte[26],
						setting_byte[36],
						getIntent.getStringExtra("speech_skip_word"),
						setting_byte[29], getIntent.getShortExtra(
								"init_comment_count", (short) 20),
						setting_boolean[19]);
			}
			if (setting_byte[32] > 0) {
				autoUpdateTask = new AutoUpdateTask();
				autoUpdateTask.execute();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void arg) {
			if (threadError != null) {// CommentTableでスレッドエラーの場合
				threadError.showErrorToast();
			}
		}
	}

	/**
	 * ユーザー発言リストに渡すインスタンス
	 */
	public CommentListAdapter createNewAdapter() {
		return new ExCommentListAdapter(this);
	}

	private static class ViewHolder {
		public byte id_index;//column_seqは順番に対してのマッピングなので、IDが何番目かは、覚えておく必要がある
		TextView[] columnTvs = new TextView[7];
	}

	class AudioTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... arg0) {
			if (audioReceiver != null) {
				return null;
			}
			try {
				AudioManager audio = (AudioManager) getSystemService(AUDIO_SERVICE);
				if (getIntent == null) {
					getIntent = getIntent();
				}
				int mode = audio.getRingerMode();// onCreate後の初期のモード
				if (setting_boolean == null) {
					setting_boolean = getIntent
							.getBooleanArrayExtra("setting_boolean");
				}
				if (setting_byte == null) {
					setting_byte = getIntent.getByteArrayExtra("setting_byte");
					if (setting_byte == null) {// クラッシュ後にこのクラスに戻る(ノティフィからとか)となる、どうしようもないか
						errorFinish(CODE.RESULT_FLASH_ERROR, -43);
						return null;
					}
				}
				// 音量固定設定ならその音量を設定
				if (setting_boolean[5] && setting_byte[25] != -1) {
					// マナーモードORサイレントなら音量0にする
					if (mode != AudioManager.RINGER_MODE_NORMAL) {
						audio.setStreamVolume(AudioManager.STREAM_MUSIC, 0,
								0);
					} else {
						audio.setStreamVolume(AudioManager.STREAM_MUSIC,
								(int) setting_byte[25],
								0);
					}
					// "audiovolume"のインテントに、byteとして格納して、AudioManagerのメソッドにセットする時にintにキャストする
					// 前回マナーで終わっていたら、元の音量も0になっているから設定値から保存
					// 戻す音量を保存
					getIntent.putExtra("audiovolume", (byte) setting_byte[25]);
				} else {
					// 固定値設定じゃなかったら、今の音量に今の音量を保存(要はaudiovolumeに、固定値設定に関わらず保存される+固定値設定している場合は、モード切替によって固定値に戻される)
					getIntent.putExtra("audiovolume", (byte) audio
							.getStreamVolume(AudioManager.STREAM_MUSIC));
				}

				// いつonCreateされるかわからないので、ここでサービスが実行されているか
				// 調べる必要がある

				// 音量監視開始
				if (audioReceiver == null) {
					startService(new Intent(ACT, RingReceiver.class));
					audioReceiver = new RingReceiver();

					IntentFilter filter = new IntentFilter();
					filter.addAction(AudioManager.RINGER_MODE_CHANGED_ACTION);
					filter.addAction("finish_player.NLR");
					filter.addAction("player_reload.NLR");
					filter.addAction("player_config.NLR");
					registerReceiver(audioReceiver, filter);
				}
			} catch (Exception e) {// 何故かaudio自体がnullになった
				e.printStackTrace();
			}
			return null;
		}
	}

	class RingReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent == null)
				return;
			// ※モード変更+音量変更でも呼ばれる
			if (intent.getAction().equals(
					AudioManager.RINGER_MODE_CHANGED_ACTION)) {
				if(!setting_boolean[22])return;//マナー中0設定じゃなければ終了
				int mode = intent.getIntExtra(AudioManager.EXTRA_RINGER_MODE,
						-1);
				if (mode == AudioManager.RINGER_MODE_VIBRATE
						|| mode == AudioManager.RINGER_MODE_SILENT) {
					// マナーモードになったら0にする
					AudioManager audio = (AudioManager) getSystemService(ACT.AUDIO_SERVICE);
					audio.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
				} else {
					// マナーモードではなくなったら音量を元に戻す+ボリュームを記憶
					AudioManager audio = (AudioManager) getSystemService(ACT.AUDIO_SERVICE);
					// 値が存在しない場合の値はintだから(int)getInten～"audio～",(int)0
					audio.setStreamVolume(
							AudioManager.STREAM_MUSIC,
							(int) getIntent().getByteExtra("audiovolume",
									(byte) 0), 0);
					getIntent()
							.putExtra(
									"audiovolume",
									(byte) audio
											.getStreamVolume(AudioManager.STREAM_MUSIC));
				}
			} else if (intent.getAction().equals("finish_player.NLR")) {
				if(NLiveRoid.isDebugMode)Log.d("NLiveRoid","S R");
				ACT.standardFinish();
			} else if (intent.getAction().equals("player_reload.NLR")) {
				if (wv != null) {
					loadWV(null);
				}
			} else if (intent.getAction().equals("player_config.NLR")) {
				if(intent.getByteExtra("temp_fullscrn", (byte)-2) != -2){//OverLay側から全画面を戻す場合はOverLay側で画面回転の設定を変えている可能背があるので
					byte orientation = intent.getByteExtra("temp_fullscrn",(byte)-2);
					if(orientation == -1){//一時的にフルスクリーン
					tempFullScrn();
					}else if(orientation == 11){//プレイヤーの位置を単に変更縦
						setting_byte[22] = intent.getByteExtra("value", (byte)0);
						layoutPlayer();
					}else if(orientation == 12){//プレイヤーの位置を単に変更横
						setting_byte[23] = intent.getByteExtra("value", (byte)0);
						layoutPlayer();
					}else if(orientation >= 0){//設定を一時的フルスクリーンから戻す
						setting_byte[24] = orientation;
						setOrientation(orientation);
						layoutPlayer();
					}
				}else if(intent.getStringExtra("offtimer_start") != null){//OverLayからオフタイマーの新規セット
					try{
					getIntent.putExtra("offtimer_start", Long.parseLong(intent.getStringExtra("offtimer_start")));
					setting_byte[37] = intent.getByteExtra("off_timer", (byte) -1);
					}catch(Exception e){
						e.printStackTrace();
						MyToast.customToastShow(ACT, "オフタイマーの処理に失敗しました");
					}
				}else{
				setSpPlayerOperation(
						intent.getByteExtra("operation", (byte) -1),
						intent.getByteExtra("value", (byte) -1));
				}
			}
		}

	}

	@Override
	public void onNewIntent(Intent intent) {
//		 Log.d("Log","NEW INTENT ");
		if (getIntent == null)
			getIntent = getIntent();
		if (intent != null) {
			if (intent.getBooleanExtra("restart", false)) {// 別の放送だったら今のgetIntent()がgetIntent
				intent.putExtra("restart", false);
				getIntent = intent;// restartじゃない時にこれするとsetting_booleanとかがnullになっちゃう
				if (setting_byte != null && setting_byte[31] != 2) {// プレイヤーのみの場合以外、一度クリアする、もっとちゃんと破棄できないのか。。
					// コメ欄クリア
					if (listview != null) {
						listview.setVisibility(View.GONE);
					}
					if (headerview != null) {
						headerview.setVisibility(View.GONE);
					}
				}
				new InitFlashPlayer().execute();
			}
		}
		super.onNewIntent(getIntent);

	}

	/**
	 * ACTを取得します。
	 *
	 * @return ACT
	 */
	public static FlashPlayer getACT() {
		return ACT;
	}

	public static void postComment() {
		if(NLiveRoid.isDebugMode){
			Log.d("NLiveRoid","postComment F");

		}

		final String comment = postET.getText().toString();
		if(comment == null || comment.equals(""))return;
		if (cmd != null) {
			commentTable.postComment(comment, cmd);
		} else {
			Log.d("NLiveRoid","Failed postComment");
			MyToast.customToastShow(ACT, "コメントの投稿に失敗しました");
		}
		postET.setText("");
		Log.d("NLiveRoid","postComment END");
	}

	public void setOrientation(int flug) {
		Log.d("NLiveRoid", "Orientation SetValue is " + flug);
		switch (flug) {
		case 0:
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);// 基本回転する
			if (setting_byte == null) {
				if (getIntent == null)
					getIntent = getIntent();
				setting_byte = getIntent.getByteArrayExtra("setting_byte");
				;
			}
			setting_byte[24] = 0;
			getIntent.putExtra("setting_byte", setting_byte);
			break;
		case 1:
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			if (setting_byte == null) {
				if (getIntent == null)
					getIntent = getIntent();
				setting_byte = getIntent.getByteArrayExtra("setting_byte");
				;
			}
			setting_byte[24] = 1;
			getIntent.putExtra("setting_byte", setting_byte);
			break;
		case 2:
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			if (setting_byte == null) {
				if (getIntent == null)
					getIntent = getIntent();
				setting_byte = getIntent.getByteArrayExtra("setting_byte");
				;
			}
			setting_byte[24] = 2;
			getIntent.putExtra("setting_byte", setting_byte);
			break;
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig); // Checks the orientation of
													// the screen
		Log.d("NLiveRoid","onConfigurationChanged F ");
		if(listDialog != null && listDialog.isShowing())listDialog.cancel();
		if (setting_boolean == null) {
			if (getIntent == null)
				getIntent = getIntent();
			setting_boolean = getIntent.getBooleanArrayExtra("setting_boolean");
			if (setting_boolean == null) {// クラッシュ確定
				errorFinish(CODE.RESULT_FLASH_ERROR, -43);
			}
		}
		if (setting_byte == null) {
			if (getIntent == null)
				getIntent = getIntent();
			setting_byte = getIntent.getByteArrayExtra("setting_byte");
			if (setting_byte == null) {// クラッシュ確定
				errorFinish(CODE.RESULT_FLASH_ERROR, -43);
			}
		}
		if (gate != null && gate.isOpened()) {
			gate.onConfigChanged(newConfig);
		}
		if(tweetDialog != null && tweetDialog.isShowing())tweetDialog.onConfigChanged(ACT);

		if (!isFullScreend) {
			layoutPlayer();
		}
		if(setting_byte[31] == 0 || setting_byte[31] == 2){// 以下の処理は前面またはプレイヤーのみ以外
			return;
		}
			// viewWidthは必ず短い方
			if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
				isPortLayt = false;
				firstcurrentX = (int) ((double) (pixcelViewH * setting_byte[19] * 0.01D));
				firstcurrentY = (int) ((double) (pixcelViewW * setting_byte[20] * 0.01D));
				list_bottom = (int) ((double) (pixcelViewW * setting_byte[21] * 0.01D));
				list_width = (int) ((double) (pixcelViewH * setting_byte[39] * 0.01D));
				cellHeight = (int) (pixcelViewH * (setting_byte[18] * 0.01D));
			} else {
				isPortLayt = true;
				firstcurrentX = (int) ((double) (pixcelViewW * setting_byte[8] * 0.01D));
				firstcurrentY = (int) ((double) (pixcelViewH * setting_byte[9] * 0.01D));
				list_bottom = (int) ((double) (pixcelViewH * setting_byte[10] * 0.01D));
				list_width = (int) ((double) (pixcelViewW * setting_byte[38] * 0.01D));
				cellHeight = (int) (pixcelViewH * (setting_byte[7] * 0.01D));
			}
			// 縦横で方向が変わる場合がある
			if (list_bottom < 0) {
				isUplayout = true;
			} else {
				isUplayout = false;
			}
//			preparePositionValue();
			if (listview == null) {
				listview = getList();
			}
			listview.setAdapter(adapter);

			// 親(Blank系)のパディングをやり直してアドし直す
			if (isUplayout()) {
				if (listBlank == null) {
					listBlank = getListBlank_Up();
				}
				listBlank.setPadding(firstcurrentX,
						firstcurrentY + list_bottom, -firstcurrentX, 0);
				listBlank.removeView(listview);
				listview.setPadding(0, 0, 0, 0);
				listBlank.addView(listview, new LinearLayout.LayoutParams(list_width,
						-list_bottom));
				listview.setSelection(0);
			} else {
				if (listBlank == null) {
					listBlank = getListBlank_Down();
				}
				listBlank.setPadding(firstcurrentX, firstcurrentY,
						-firstcurrentX, 0);
				listview.setPadding(0, cellHeight, 0, 0);
				listBlank.removeView(listview);
				listBlank.addView(listview, new LinearLayout.LayoutParams(list_width,
						list_bottom));
				listview.setSelection(listview.getCount());
			}
			if (headerBlank != null) {
				headerBlank.setPadding(firstcurrentX, firstcurrentY,
						-firstcurrentX, 0);
				headerBlank.removeView(headerview);
				headerview = getHeader();
				if(headerview == null){
					errorFinish(CODE.RESULT_FLASH_ERROR, -13);//コメサバに接続できてない(非対応CH・公式)時になる
					return;
				}
				headerBlank.addView(headerview, new LinearLayout.LayoutParams(
						list_width, cellHeight));
				firstBlueHeader = getBlueHeader();// テキストサイズをやり直した物を作っておく
			}

		if (commentTable != null) {
			commentTable.manualSort();
		}
	}

	@Override
	public void onResume() {
		NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		notificationManager.cancelAll();
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
		if (keepActivity) {
			onStart();
		}
		if(NLiveRoid.isDebugMode){
			Log.d("NLiveRoid"," pause F");
		}
	}

	@Override
	public void onDestroy() {
		if(NLiveRoid.isDebugMode)Log.d("NLiveRoid"," dest F");
		if (commentTable != null) {
			if (autoUpdateTask != null
					&& autoUpdateTask.getStatus() != AsyncTask.Status.FINISHED) {
				autoUpdateTask.cancel(true);
			}
			commentTable.killSpeech();
			commentTable.closeMainConnection();
			commentTable.closeLogConnection();
		}

			if (audioReceiver != null) {
				try {
					unregisterReceiver(audioReceiver); // 音量監視登録解除
				} catch (IllegalArgumentException e) {
					// 早い操作でレジスタできてなかった場合に起こる
					e.printStackTrace();
				}
			}

			if (keepActivity) {
				//履歴から終了された場合、このままアプリ自体のパッケージのPIDがKILLされるので、
				//このアプリのPIDだけが生きてしまい、音が出続けてしまう→設定値をブロキャスしても、受け取るサービスも死んでる
				//なのでここでノティフィ消してプロセスをKILLして終了
				NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
				notificationManager.cancelAll();
				super.onDestroy();
				if(hbLoopTask != null && hbLoopTask.getStatus() != AsyncTask.Status.FINISHED)hbLoopTask.cancel(true);
				Process.killProcess(Process.myPid());
				return;
			}
		super.onDestroy();
	}

	@Override
	public void onStop() {
		if (keepActivity) {
			onStart();
		}
		if(NLiveRoid.isDebugMode)Log.d("NLiveRoid"," stop F");
		super.onStop();
	}

	@Override
	public boolean isUplayout() {
		return isUplayout;
	}

	@Override
	public void onStart() {
		super.onStart();
		if(NLiveRoid.isDebugMode)Log.d("NLiveRoid"," start F");
	}

	public void standardFinish() {//keepActivity=falseはfinish()で呼んでいる
		if(NLiveRoid.isDebugMode){
			Log.d("NLiveRoid","standardF");
			if(NLiveRoid.apiLevel >= 16){
				Intent log = new Intent();
			log.setAction("return_f.NLR");
			log.putExtra("r_code", CODE.RESULT_LOG);
			log.putExtra("pid", Process.myPid());
//			log.putExtra("log", debuglogStr);
			this.getBaseContext().sendBroadcast(log);
			}
		}
		if(mMediaPlayer != null ){
			if( mMediaPlayer.isPlaying())mMediaPlayer.stop();
			mMediaPlayer.release();
		}
		if (commentTable != null) {
			if (autoUpdateTask != null
					&& autoUpdateTask.getStatus() != AsyncTask.Status.FINISHED) {
				autoUpdateTask.cancel(true);
			}
			commentTable.killSpeech();
			commentTable.closeMainConnection();
			commentTable.closeLogConnection();
		}
		Intent data = new Intent();
		data.setAction("return_f.NLR");
		try {
			if (setting_boolean == null) {
				if (getIntent == null)
					getIntent = getIntent();
				setting_boolean = getIntent
						.getBooleanArrayExtra("setting_boolean");
			}
			if (setting_byte == null) {
				if (getIntent == null)
					getIntent = getIntent();
				setting_byte = getIntent.getByteArrayExtra("setting_byte");
			}
			data.putExtra("setting_byte", setting_byte);
			data.putExtra("setting_boolean", setting_boolean);
			data.putExtra("column_seq", column_seq);
			data.putExtra("init_comment_count",
					getIntent.getShortExtra("init_comment_count", (short) 20));
			data.putExtra("cmd", cmd);
			data.putExtra("cookie",
					CookieManager.getInstance().getCookie("nicovideo.jp"));
			if (!setting_boolean[5]&&setting_boolean[22]) {
				// マナーかサイレントだと、音量を消しているので元に戻す
				AudioManager audio = (AudioManager) getSystemService(this.AUDIO_SERVICE);
				int mode = audio.getRingerMode();
				if (mode == AudioManager.RINGER_MODE_VIBRATE
						|| mode == AudioManager.RINGER_MODE_SILENT) {
					data.putExtra("audiovolume",
							getIntent.getByteExtra("audiovolume", (byte) -1));
				}
			}
			// Broadcastする
			data.putExtra("r_code", CODE.RESULT_COOKIE);
			sendBroadcast(data);
			finish();

		} catch (IllegalStateException e) {
			e.printStackTrace();
			MyToast.customToastShow(ACT, "設定値の引き継ぎに失敗しました。");
			finish();
		} catch (NullPointerException e) {
			MyToast.customToastShow(ACT, "設定値の引き継ぎに失敗しました。");
			finish();
		}
	}

	public void errorFinish(int resultCode, int errorCode) {
		 Log.d("NLiveRoid","F ERROR FINISH ---" + resultCode +" " +errorCode);
		if (commentTable != null) {
			if (autoUpdateTask != null
					&& autoUpdateTask.getStatus() != AsyncTask.Status.FINISHED) {
				autoUpdateTask.cancel(true);
			}
			commentTable.killSpeech();
			commentTable.closeMainConnection();
			commentTable.closeLogConnection();
		}
		Intent data = new Intent();
		data.setAction("return_f.NLR");
		try {
			if (setting_boolean == null) {
				if (getIntent == null)
					getIntent = getIntent();
				setting_boolean = getIntent
						.getBooleanArrayExtra("setting_boolean");
			}
			if (setting_byte == null) {
				if (getIntent == null)
					getIntent = getIntent();
				setting_byte = getIntent.getByteArrayExtra("setting_byte");
			}
			data.putExtra("setting_byte", setting_byte);
			data.putExtra("setting_boolean", setting_boolean);
			data.putExtra("cmd", cmd);
			if (!setting_boolean[5]&&setting_boolean[22]) {
				// マナーかサイレントだと、音量を消しているので元に戻す
				AudioManager audio = (AudioManager) getSystemService(this.AUDIO_SERVICE);
				int mode = audio.getRingerMode();
				if (mode == AudioManager.RINGER_MODE_VIBRATE
						|| mode == AudioManager.RINGER_MODE_SILENT) {
					data.putExtra("audiovolume",
							getIntent.getByteExtra("audiovolume", (byte) -1));
				}
			}
			// Broadcastする
			data.putExtra("flash_error", errorCode);
			if (errorCode == -17||(setting_boolean[25]&&errorCode == -18)){
				Log.d("NLiveRoid","F session Failed -----");
				data.putExtra("LiveInfo", liveInfo);// セッションの失敗||TSだったらやり直す
			}
			data.putExtra("r_code", resultCode);
			sendBroadcast(data);
			finish();
		} catch (IllegalStateException e) {// Serviceがregistされてないとかの関係と思われる
			e.printStackTrace();//時間経過によりACTがnullになる事がある
			if(ACT != null)MyToast.customToastShow(ACT, "プレイヤーエラーしました CODE:0");
			finish();// errorFinishは無限ループしちゃう
		} catch (NullPointerException e) {// Intent受け渡し失敗と思われる
			if(ACT != null)MyToast.customToastShow(ACT, "プレイヤーエラーしました CODE:1");
			finish();
		}
	}

	/**
	 * プレイヤーからの遷移で終了
	 */
	public void lv_url_Finish(String scheme) {
		if(NLiveRoid.isDebugMode)Log.d("NLiveRoid","lv_url_Finish" + scheme);
		if (commentTable != null) {
			if (autoUpdateTask != null
					&& autoUpdateTask.getStatus() != AsyncTask.Status.FINISHED) {
				autoUpdateTask.cancel(true);
			}
			commentTable.killSpeech();
			commentTable.closeMainConnection();
			commentTable.closeLogConnection();
		}
		Intent data = new Intent();
		data.setAction("return_f.NLR");
		data.putExtra("scheme", scheme);
		try {
			if (setting_boolean == null) {
				if (getIntent == null)
					getIntent = getIntent();
				setting_boolean = getIntent
						.getBooleanArrayExtra("setting_boolean");
			}
			if (setting_byte == null) {
				if (getIntent == null)
					getIntent = getIntent();
				setting_byte = getIntent.getByteArrayExtra("setting_byte");
				;
			}
			data.putExtra("setting_byte", setting_byte);
			data.putExtra("setting_boolean", setting_boolean);
			data.putExtra("init_comment_count",
					getIntent.getShortExtra("init_comment_count", (short) 20));
			data.putExtra("cmd", cmd);
			if (!setting_boolean[1]) {
				data.putExtra("cookie",
						CookieManager.getInstance().getCookie("nicovideo.jp"));
				// マナーかサイレントだと、音量を消しているので元に戻す
				AudioManager audio = (AudioManager) getSystemService(this.AUDIO_SERVICE);
				int mode = audio.getRingerMode();
				if (mode == AudioManager.RINGER_MODE_VIBRATE
						|| mode == AudioManager.RINGER_MODE_SILENT) {
					data.putExtra("audiovolume",
							getIntent.getByteExtra("audiovolume", (byte) -1));
				}

			}
			// Broadcastする
			data.putExtra("r_code", CODE.RESULT_ENDPLAYER_LV_URL);
			sendBroadcast(data);
			finish();

		} catch (IllegalStateException e) {
			e.printStackTrace();
			MyToast.customToastShow(ACT, "設定値の引き継ぎに失敗しました。");
			finish();
		} catch (NullPointerException e) {
			MyToast.customToastShow(ACT, "設定値の引き継ぎに失敗しました。");
			finish();
		}
	}

	@Override
	public void finish() {
		keepActivity = false;
		stopWebView();
		CookieSyncManager.getInstance().stopSync();
		System.gc();
		super.finish();
		Process.sendSignal(Process.myPid(), Process.SIGNAL_KILL);

	}

	/**
	 * cmdを取得します。
	 *
	 * @return cmd
	 */
	public CommandMapping getCmd() {
		return cmd;
	}

	/**
	 * cmdを設定します。
	 *
	 * @param cmd
	 *            cmd
	 */
	public void setCmd(CommandKey key, String cmd) {
		if(key.equals(CommandKey.CMD) && setting_boolean[3] && post_184 != null){//投稿フォームに184とかあったら、ダイアログの見た目が整合性取れなくなるので
			if(cmd.equals("184")){
				post_184.setChecked(true);
			}else{
				post_184.setChecked(false);
			}
		}
		this.cmd.set(key, cmd);
	}

	/**
	 * コンテキストメニュー生成時処理
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View view,
			ContextMenuInfo info) {
		super.onCreateContextMenu(menu, view, info);
		AdapterContextMenuInfo adapterInfo = (AdapterContextMenuInfo) info;
		if (adapter.getCount() < adapterInfo.position) {// 立見などをタップした時におかしくなるArrayList.throwIndexOutOfBoundsException
			return;
		}
		final String[] row = adapter.getItem(adapterInfo.position);
		tempID = adapter.getItem(adapterInfo.position)[1];

		menu.add("コテハンを編集");
		menu.getItem(0).setOnMenuItemClickListener(
				new OnMenuItemClickListener() {
					@Override
					// 引数はメニューのテキスト
					public boolean onMenuItemClick(MenuItem arg0) {
						int defaultBgColor = idToBgColor.get(tempID) == null ? Color.WHITE
								: idToBgColor.get(tempID);
						int defaultFoColor = idToForeColor.get(tempID) == null ? Color.BLACK
								: idToForeColor.get(tempID);

						new HandleNamePicker(ACT,
								new ColorPickerView.OnColorChangedListener() {
									@Override
									public void colorChanged(int color) {
										// 色が選択されるとcolorに値が入る OKボタンで確定するので未使用
										int R = Color.red(color);
										int G = Color.green(color);
										int B = Color.blue(color);
									}
								}, defaultBgColor, defaultFoColor, tempID,idToHandleName.get(tempID)==null? tempID:idToHandleName.get(tempID),true)
								.show();
						return false;
					}
				});

		menu.add("列のコピー");
		menu.getItem(1).setOnMenuItemClickListener(
				new OnMenuItemClickListener() {
					@Override
					public boolean onMenuItemClick(MenuItem menuitem) {
						new AlertDialog.Builder(ACT)
						.setItems(new CharSequence[]{"ユーザタイプ","ID","コマンド","時間","NGスコア","コメ番"},new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
								String text = "";
								switch(which){
								case 0:
									text = row[0];
									break;
								case 1:
									text = row[1];
									break;
								case 2:
									text = row[2];
									break;
								case 3:
									text = row[3];
									break;
								case 4:
									text = row[4];
									break;
								case 5:
									text = row[5];
									break;
								}
								cm.setText(text);
							}
						}).create().show();
						return false;
					}
				});
		menu.add("コメントを表示");
		menu.getItem(2).setOnMenuItemClickListener(
				new OnMenuItemClickListener() {
					@Override
					public boolean onMenuItemClick(MenuItem menuitem) {
						int defaultBgColor = idToBgColor.get(tempID) == null ? Color.WHITE
								: idToBgColor.get(tempID);
						int defaultFoColor = idToForeColor.get(tempID) == null ? Color.BLACK
								: idToForeColor.get(tempID);
						new ContextDialog(ACT, row,idToHandleName.get(row[1])== null? row[1]:idToHandleName.get(row[1]),(int) (pixcelViewW),defaultBgColor,defaultFoColor).showSelf();
						return false;
					}
				});
		menu.add("コメントをコピー");
		menu.getItem(3).setOnMenuItemClickListener(
				new OnMenuItemClickListener() {
					@Override
					public boolean onMenuItemClick(MenuItem menuitem) {
						ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
						// クリップボードへ値をコピー。
						cm.setText(row[6]);
						return false;
					}
				});
		final Matcher mc  = URLEnum.urlpt.matcher(row[6]);
		if(row[6] != null&&mc.find()){
			menu.add("URLをブラウザで開く");
			menu.getItem(4).setOnMenuItemClickListener(
					new OnMenuItemClickListener() {
						@Override
						public boolean onMenuItemClick(MenuItem menuitem) {
							try{
							Intent i = new Intent(Intent.ACTION_VIEW);
							i.addCategory(Intent.CATEGORY_BROWSABLE);
							i.setDataAndType(Uri.parse(mc.group()), "text/html");
							ACT.startActivityForResult(i,CODE.RESULT_REDIRECT);//リダイレクトコード
							}catch(ActivityNotFoundException e){
								e.printStackTrace();
								MyToast.customToastShow(ACT, "起動できるアプリが見つかりませんでした");
							}catch(Exception e){
								e.printStackTrace();
							}
							return false;
						}
					});
		}

	}

	/**
	 * Menuキー絡み処理
	 *
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// このクラスでMENUを開く時は、前面でBACKキー押した場合があるので、全ての表示設定がありえる
		MenuInflater inflater = getMenuInflater();
		if (setting_byte == null) {
			if (getIntent == null)
				getIntent = getIntent();
			setting_byte = getIntent.getByteArrayExtra("setting_byte");
		}
		if (setting_byte[31] == 0 || setting_byte[31] == 2) {
			inflater.inflate(R.menu.menu_disp_2, menu);// 前面からBACKキーか、プレイヤーのみ
		} else {// 背面か、コメントのみ
			inflater.inflate(R.menu.menu_disp_0_1_3, menu);
		}
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		if(isLayerChanged){
			isLayerChanged  = false;
			menu.clear();
		if (setting_byte[31] == 0 || setting_byte[31] == 2) {
			inflater.inflate(R.menu.menu_disp_2, menu);// 前面からBACKキーか、プレイヤーのみ
		} else {// 背面か、コメントのみ
			inflater.inflate(R.menu.menu_disp_0_1_3, menu);
		}
		}
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(NLiveRoid.isDebugMode)Log.d("NLiveRoid","F OptionMenu id:" + item.getItemId() +" update:" + R.id.update + " setting:" + R.id.setting +" desc:"+R.id.live_descOpen +" commentArea_change:" + R.id.commentArea_change + " quick:" + R.id.quick +" layer_change:" + R.id.layer_change);
		switch (item.getItemId()) {
		case R.id.update:
			if (setting_boolean == null) {
				if (getIntent == null)
					getIntent = getIntent();
				setting_boolean = getIntent
						.getBooleanArrayExtra("setting_boolean");
			}
			if (setting_byte == null) {
				if (getIntent == null)
					getIntent = getIntent();
				setting_byte = getIntent.getByteArrayExtra("setting_byte");
			}
			if (setting_byte[31] != 1||noCommentServer) {// ここで呼ばれる場合は、プレイヤーのみか背面でBACキー
				new OperationDialog(this, setting_boolean[13], (byte) 2).showSelf();
			} else {
				new OperationDialog(this, setting_boolean[13], setting_byte[31])
				.showSelf();
			}
			break;
		case R.id.setting:
			if (cmd == null) {
				if (getIntent == null)
					return false;
				cmd = (CommandMapping) getIntent.getSerializableExtra("cmd");
				if (cmd == null)
					return false;
			}
				if (setting_boolean == null) {
					if (getIntent == null)
						getIntent = getIntent();
					setting_boolean = getIntent
							.getBooleanArrayExtra("setting_boolean");
				}
				if (setting_byte == null) {
					if (getIntent == null)
						getIntent = getIntent();
					setting_byte = getIntent.getByteArrayExtra("setting_byte");
				}
				// 前面、プレイヤーのみの場合、ここではコメサバ接続を持たないのでinit_modeが必ず2
				if (setting_byte[31] == 0 || setting_byte[31] == 2||noCommentServer) {
					new ConfigDialog(this, liveInfo, setting_byte,
							setting_boolean, (byte) 2).showSelf();
				}else if (liveInfo == null || liveInfo.getLiveID() == null) {
					MyToast.customToastShow(this, "読み込み中です");
				}else {
					new ConfigDialog(this, liveInfo, setting_byte,
							setting_boolean, setting_byte[31]).showSelf();
				}
			break;
		case R.id.live_descOpen:
			if (liveInfo == null || liveInfo.getLiveID() == null) {
				MyToast.customToastShow(this, "読み込み中又は放送情報の取得に失敗している");
			}else{
				showLiveDescription();
			}
			break;
		case R.id.commentArea_change:// ここは、背面か、コメントのみの時しか呼ばれない
			showPostArea();
			break;
		case R.id.quick:// ここは、背面か、コメントのみの時しか呼ばれない
			if(NLiveRoid.isDebugMode)Log.d("NLiveRoid","F quick");
			if(quickDialog != null && quickDialog.isShowing()){
				quickDialog.cancel();
			}else{
			quickDialog = new QuickDialog(this,setting_byte,setting_boolean[19]);
			quickDialog.showSelf(setting_byte[40],setting_byte[41]);
			}
			break;
		case R.id.layer_change:// プレイヤーのみか前面から戻っている状態か
			if (commentTable != null) {
				commentTable.closeMainConnection();
			}
			if(noCommentServer){
				MyToast.customToastShow(ACT, "コメント取得できない公式・CHです");
			}else{
			startOverLay();
			}
			break;
		}
		return true;
	}
	private void startOverLay() {
		if (overlay == null) {
			overlay = new Intent(this, OverLay.class);
		}
		if (cmd != null) {
			overlay.putExtra("cmd", cmd);
		}
		if (setting_boolean == null) {
			if (getIntent == null)
				getIntent = getIntent();
			setting_boolean = getIntent
					.getBooleanArrayExtra("setting_boolean");
		}
		if (setting_byte == null) {
			if (getIntent == null)
				getIntent = getIntent();
			setting_byte = getIntent.getByteArrayExtra("setting_byte");
			;
		}
		overlay.putExtra("pid", Process.myPid());
		overlay.putExtra("Cookie", getIntent.getStringExtra("Cookie"));
		overlay.putExtra("sp_session", getIntent.getStringExtra("sp_session"));
		overlay.putExtra("setting_boolean", setting_boolean);
		overlay.putExtra("setting_byte", setting_byte);
		overlay.putExtra("column_seq", column_seq);
		overlay.putExtra("isnsen", getIntent.getBooleanExtra("isnsen", false));
		overlay.putExtra("init_comment_count",
				getIntent.getShortExtra("init_comment_count", (short) 20));
		overlay.putExtra("speech_skip_word",
				getIntent.getStringExtra("speech_skip_word"));
		overlay.putExtra("viewW", getIntent.getIntExtra("viewW",
				getWindowManager().getDefaultDisplay().getWidth()));
		overlay.putExtra("viewH", getIntent.getIntExtra("viewH",
				getWindowManager().getDefaultDisplay().getHeight()));
		overlay.putExtra("density",
				getIntent.getFloatExtra("density", 1.5F));
		overlay.putExtra("twitterToken", getIntent.getStringExtra("twitterToken"));
		overlay.putExtra("LiveInfo", liveInfo);
		overlay.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		isOverlayStarted = true;
		startActivityForResult(overlay, CODE.REQUEST_OVERLAY);
	}

	private void showLiveDescription() {
		if (gate != null && gate.isOpened()) {// 開いてたら戻す
			Log.d("NLiveRoid", "CLOSE GATE IN ITEMSELECT");
			gate.close_noanimation();
			if (setting_byte[31] != 2 && !noCommentServer && listview == null) {// あまり無いと思うが。
				Log.d("NLiveRoid", "F LIST ERROR");
				MyToast.customToastShow(this, "レイアウトをやり直せませんでした\nコ読み込み中と思われます");
				return;
			}
			// 開く前にisScrollEndだったらスクロール末尾に設定
			if (isUplayout()) {
				if (tempIsScrollEnd) {
					listview.setSelection(0);
				}
			} else {
				if (tempIsScrollEnd) {
					listview.setSelection(listview.getCount());
				}
			}
		} else {
			// 詳細を取得する
			tempIsScrollEnd = isScrollEnd;
			isScrollEnd = false;
			if (setting_byte == null) {
				if (getIntent == null)
					getIntent = getIntent();
				setting_byte = getIntent.getByteArrayExtra("setting_byte");
			}
			if (setting_byte[31] == 3) {// コメントのみだったら特殊で、そのまま詳細表示
				NLiveRoid app = (NLiveRoid) getApplicationContext();// シンプルじゃない、嫌い
				if (app == null) {
					MyToast.customToastShow(this, "読み込み中です");
					return;
				} else if (app.getGateView() == null) {
					MyToast.customToastShow(this, "読み込み中です");
					return;
				}
				if (getIntent == null)
					getIntent = getIntent();
				GateView gateView = (GateView) app.getGateView();
				ViewGroup gateParent = (ViewGroup) gateView.getView()
						.getParent();
				gate = new Gate(this, gateView, liveInfo, true,
						getIntent.getStringExtra("Cookie"),getIntent.getStringExtra("twitterToken"));
				if (gateParent != null) {
					gateParent.removeView(gateView.getView());
				}
				((ViewGroup) parent.getParent())
						.addView(gateView.getView());
				gate.show(this.getResources().getConfiguration());
			} else {// Playerのみか背面時の通常の場合
					// LiveInfoはシリアライズめんどいかもしれないのでstatic参照
				if(gate == null || gate.getGateView() == null){
					((NLiveRoid)getApplicationContext()).createGateInstance();
				}
				Intent intent = new Intent(this, TransDiscr.class);
				intent.putExtra("init_mode", (byte) 0);
				intent.putExtra("orientation", getRequestedOrientation());
				intent.putExtra("Cookie",
						getIntent.getStringExtra("Cookie"));
				intent.putExtra("twitterToken", getIntent.getStringExtra("twitterToken"));
				intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				startActivityForResult(intent, CODE.RESULT_TRANS_LAYER);
			}
		}
	}

	@Override
	public LiveInfo getLiveInfo() {
		return liveInfo;
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putString("WORKAROUND_FOR_BUG_19917_KEY",
				"WORKAROUND_FOR_BUG_19917_VALUE");
		// super.onSaveInstanceState(outState);
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {

		if(NLiveRoid.isDebugMode)Log.d("NLiveRoid","FLASH KEY EVENT --- " + event.getKeyCode());

		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			if (event.getAction() == KeyEvent.ACTION_UP)
				return false;

			if (gate != null && gate.isOpened()) {// 詳細表示中なら閉じる
				gate.close();
				// 開く前にisScrollEndだったらスクロール末尾に設定
//				if (isUplayout()) {
//					if (tempIsScrollEnd && listview != null) {
//						listview.setSelection(0);
//						listview.setVisibility(View.VISIBLE);
//					}
//				} else {
//					if (tempIsScrollEnd && listview != null) {
//						listview.setSelection(listview.getCount());
//						listview.setVisibility(View.VISIBLE);
//					}
//				}
				return true;
			}
			if (setting_boolean == null) {
				if (getIntent == null)
					getIntent = getIntent();
				setting_boolean = getIntent
						.getBooleanArrayExtra("setting_boolean");
			}
			// コメントフォーム表示中なら
			if (postArea != null && postArea.getVisibility() == View.VISIBLE) {
				// フォーカスをクリア
				if (postET != null && postET.isFocused()) {
					postET.clearFocus();
					return false;
				}

				if (setting_boolean[20]) {
					// 投稿フォームを閉じる
					postArea.setVisibility(View.GONE);
					postET.setVisibility(View.GONE);
					postB.setVisibility(View.GONE);
					if (setting_boolean[3]) {
						post_184.setVisibility(View.GONE);
						post_command.setVisibility(View.GONE);
						post_update.setVisibility(View.GONE);
						post_desc.setVisibility(View.GONE);
						post_cdisp.setVisibility(View.GONE);
						voiceInput.setVisibility(View.GONE);
						post_menu.setVisibility(View.GONE);
					}
					// タブレットでキーボードが引っ込まないのを防ぐ　何故かダイアログを出すと消えてくれる。超ーーー意味わかんない
					// Dialog dummy = new Dialog(ACT);
					// dummy.show();
					// dummy.dismiss();
				}//
				return false;
			}else if(isFullScreend){//Quickから全画面にしたなら
				isFullScreend = false;
				setOrientation(setting_byte[24]);
				if(!setting_boolean[21]){//ステータスバーも戻しておく
					getWindow().clearFlags(
							WindowManager.LayoutParams.FLAG_FULLSCREEN);
				}
				layoutPlayer();
				return false;
			}else {
				// 誤操作防止設定なら1度ダイアログ表示
				if (setting_boolean[0]) {
					new ExitViewDialog(this).show();
					return true;
				} else {
					standardFinish();
				}
			}
			return true;
		}else if(event.getKeyCode() == KeyEvent.KEYCODE_MENU ){//ACTION_UPを弾く様なコードを書くと、2.2とかでMENUが出なくなるので駄目
			int menuAction = (setting_byte[40] & 0xF0) >> 4;
		Log.d("NLiveRoid" , "F MENUACTION " + menuAction);
			if(setting_byte[31] != 1 || setting_byte[31] == 0)return super.dispatchKeyEvent(event);
			switch(menuAction){
			case 0:
				return super.dispatchKeyEvent(event);
			case 1:
				if(event.getAction() == KeyEvent.ACTION_UP)return false;//普通のオプションメニュー出す時にこれ書いちゃ絶対駄目(出なくなる)
			if(quickDialog != null && quickDialog.isShowing()){
				quickDialog.cancel();
			}else{
				quickDialog = new QuickDialog(this,setting_byte,setting_boolean[19]);
				quickDialog.showSelf(setting_byte[40],setting_byte[41]);
			}
				break;
			case 2://投稿
				if(event.getAction() == KeyEvent.ACTION_UP)return false;//普通のオプションメニュー出す時にこれ書いちゃ絶対駄目(出なくなる)
				if(setting_byte[31] != 2)showPostArea();//プレイヤーのみ時は出さない
				break;
			default :
				return super.dispatchKeyEvent(event);
			}
			return true;
		}else{
			return super.dispatchKeyEvent(event);
		}
	}

	@Override
	public void onUserLeaveHint() {
		// インテントブロードキャスト
		if (gate != null && gate.isOpened()) {
			gate.close_noanimation();
		}
		if (!isOverlayStarted && liveInfo != null) {
			Intent backIntent = new Intent();
			backIntent.setAction("bindTop.NLR");
			backIntent.putExtra("playerNumber", 0);
			backIntent.putExtra("pid", Process.myPid());
			backIntent.putExtra("lv", liveInfo.getLiveID());
			backIntent.putExtra("title", liveInfo.getTitle());
			this.getBaseContext().sendBroadcast(backIntent);
		}
		System.gc();
	}

	class SPWVConfigLoop extends AsyncTask<Void, Void, Void> {
		private int tryCount = 0;

		@Override
		protected Void doInBackground(Void... params) {
			if(NLiveRoid.isDebugMode)Log.d("NLiveRoid"," SPSetting");
			while (true) {
				// 最初はたぶん読み込まれてないから10秒待つ
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
					break;
				} catch (IllegalArgumentException e1) {
					Log.d("NLiveRoid",
							"IllegalArgumentException at Player SPWVConfigLoop 0");
					e1.printStackTrace();
					break;
				}
				if (tryCount < 10 && !isJSLoaded) {
					try {
						tryCount++;
						new DoSetSPWVParam().execute();
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						e.printStackTrace();
						break;
					} catch (IllegalArgumentException e1) {
						Log.d("NLiveRoid",
								"IllegalArgumentException at Player SPWVConfigLoop 1");
						e1.printStackTrace();
						break;
					}
				} else {
					break;
				}
			}
			if(NLiveRoid.isDebugMode)Log.d("NLiveRoid"," END SPSetting");
			return null;
		}

		class DoSetSPWVParam extends AsyncTask<Void, Void, Void> {
			@Override
			protected Void doInBackground(Void... params) {

				return null;
			}

			@Override
			protected void onPostExecute(Void arg) {
				if (wv != null) {
					if (setting_boolean == null) {
						if (getIntent == null)
							getIntent = getIntent();
						setting_boolean = getIntent
								.getBooleanArrayExtra("setting_boolean");
					}
					if (setting_byte == null) {
						if (getIntent == null)
							getIntent = getIntent();
						setting_byte = getIntent
								.getByteArrayExtra("setting_byte");
					}
					if(NLiveRoid.isDebugMode){
						Log.d("NLiveRoid","TRY js --- " +tryCount);
						Log.d("NLiveRoid","SP_VALUE " + setting_boolean[14] + "  " + setting_boolean[15] + " " + setting_boolean[16] + " "+ setting_boolean[17] + " " + setting_boolean[18] + " " + setting_byte[30]);
					}


					// showCommentといいながら、trueだとコメントが非表示になる
					wv.loadUrl("javascript:NicoSPC.functions.setSetting('showBSPComment',"
							+ String.valueOf(setting_boolean[16]) + ");");
					wv.loadUrl("javascript:NicoSPC.functions.setSetting('showComment'"
							+ (!setting_boolean[14] ? ",'true'" : "") + ");");
					wv.loadUrl("javascript:NicoSPC.functions.setSetting('isMute',"
							+ String.valueOf(setting_boolean[17]) + ");");
					wv.loadUrl("javascript:NicoSPC.functions.setSetting('loadSmile',"
							+ String.valueOf(setting_boolean[18]) + ");");
					wv.loadUrl("javascript:NicoSPC.functions.setSetting('volumeSub',"
							+ String.valueOf(setting_byte[30]) + ");");
					// これが超トリッキー、他より後に書く
					wv.loadUrl("javascript:NicoSPC.functions.setSetting('ng184'"
							+ (setting_boolean[15] ? ",'true'" : "") + ");");

					// すぐに結果を取得
					wv.loadUrl(""
							+ "javascript:"
							+ "MyJS.log("
							+ "NicoSPC.functions.getSetting('showComment').toString()+','+"
							+ "NicoSPC.functions.getSetting('ng184').toString()+','+"
							+ "NicoSPC.functions.getSetting('showBSPComment').toString()+','+"
							+ "NicoSPC.functions.getSetting('isMute').toString()+','+"
							+ "NicoSPC.functions.getSetting('loadSmile').toString()+','+"
							+ "NicoSPC.functions.getSetting('volumeSub').toString())");
				}
			}
		}
	}

	/**
	 * javascriptインターフェース SP時の設定値が入ったか判定する
	 */
	class WVJS {
		public void log(String value) {
			if(NLiveRoid.isDebugMode)Log.d("NLiveRoid"," SP SCRIPT" + value);
			String[] split = value.split(",");
			if (split.length == 6) {
				if (split[0].equals(String.valueOf(!setting_boolean[14]))
						&& split[1].equals(String.valueOf(setting_boolean[15]))
						&& split[2].equals(String.valueOf(setting_boolean[16]))
						&& split[3].equals(String.valueOf(setting_boolean[17]))
						&& split[4].equals(String.valueOf(setting_boolean[18]))) {// SubVolumeは無視でいいや
					if(NLiveRoid.isDebugMode)Log.d("NLiveRoid"," SCRIPT_RESULT OK");
					isJSLoaded = true;
				}
			}else{
				Log.d("NLiveRoid"," SPSCRIPTFAILED");
			}
		}
	}

	@Override
	public void setFullScreen(boolean isChecked) {
		setSpPlayerOperation((byte) 100, isChecked ? (byte) 1 : (byte) 0);
	}

	@Override
	public void setSpPlayerOperation(byte operation, byte value) {
		if (setting_boolean == null) {
			if (getIntent == null)
				getIntent = getIntent();
			setting_boolean = getIntent.getBooleanArrayExtra("setting_boolean");
		}
		if (setting_byte == null) {
			if (getIntent == null)
				getIntent = getIntent();
			setting_byte = getIntent.getByteArrayExtra("setting_byte");
		}
		if (operation == -1 && value == -1) {
			new ConfigPlayerDialog(this, setting_byte, setting_boolean).showSelf();
			return;
		}

		switch (operation) {
		case 0:
			setting_boolean[14] = value > 0 ? false : true;// layer_numが0の時はfinish(CODE==RESULT_COOKIE)で上書きされるからいいのか!?
			if (wv != null) {
				wv.loadUrl("javascript:NicoSPC.functions.setSetting('showComment'"
						+ (!setting_boolean[14] ? ",'true'" : "") + ");");
				wv.loadUrl("javascript:MyJS.log(NicoSPC.functions.getSetting('showComment').toString());");
			}
			break;
		case 1:
			setting_boolean[15] = value > 0 ? true : false;
			if (wv != null) {
				wv.loadUrl("javascript:NicoSPC.functions.setSetting('ng184'"
						+ (setting_boolean[15] ? ",'true'" : "") + ");");
				wv.loadUrl("javascript:MyJS.log(NicoSPC.functions.getSetting('ng184').toString());");
			}
			break;
		case 2:
			setting_boolean[16] = value > 0 ? true : false;
			if (wv != null) {
				wv.loadUrl("javascript:NicoSPC.functions.setSetting('showBSPComment',"
						+ String.valueOf(setting_boolean[16]) + ");");
				wv.loadUrl("javascript:MyJS.log(NicoSPC.functions.getSetting('showBSPComment').toString());");
			}
			break;
		case 3:
			setting_boolean[17] = value > 0 ? true : false;
			if (wv != null) {
				wv.loadUrl("javascript:NicoSPC.functions.setSetting('isMute',"
						+ String.valueOf(setting_boolean[17]) + ");");
				wv.loadUrl("javascript:MyJS.log(NicoSPC.functions.getSetting('isMute').toString());");
			}
			break;
		case 4:
			setting_boolean[18] = value > 0 ? true : false;
			if (wv != null) {
				wv.loadUrl("javascript:NicoSPC.functions.setSetting('loadSmile',"
						+ String.valueOf(setting_boolean[18]) + ");");
				wv.loadUrl("javascript:MyJS.log(NicoSPC.functions.getSetting('loadSmile').toString());");
			}
			break;
		case 5:
			setting_byte[30] = (byte) ((byte) value * 10);
			if (wv != null) {
				wv.loadUrl("javascript:NicoSPC.functions.setSetting('volumeSub',"
						+ String.valueOf(setting_byte[30]) + ");");
				wv.loadUrl("javascript:MyJS.log(NicoSPC.functions.getSetting('volumeSub').toString());");
			}
			break;
		case 10:
			changePlayer(value);
			break;
		case 100:
			if (value == 1) {
				setting_boolean[21] = true;
				getWindow()
						.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			} else {
				setting_boolean[21] = false;
				getWindow().clearFlags(
						WindowManager.LayoutParams.FLAG_FULLSCREEN);
			}
			break;
		}
	}

	/**
	 * 要するにHTMLはdensityを加味しない viewのパディングの時はdensityを加味する
	 *
	 * @author Owner
	 *
	 */
	class FlashWebClient extends WebViewClient {
		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			view.setVisibility(View.GONE);
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			// ログインしてくださいのタップでURLが入ってこないので、
			// タイトルがnullじゃなかったらログインタップしたをみなす
			if (view.getTitle() != null) {
				view.loadData("<html></html>", "text/html", "utf-8");// タイトルを消す
				allUpdate();
				return;
			} else {
				wv.setVisibility(View.VISIBLE);
			}

			LinearLayout ll = (LinearLayout) wv.getParent();
			ll.removeView(wv);
			ll.addView(wv, -1, -1);
			if(NLiveRoid.isDebugMode)Log.d("NLiveRoid", "onPageFinished ---" + url);
			if (setting_boolean == null) {
				if (getIntent == null)
					getIntent = getIntent();
				setting_boolean = getIntent
						.getBooleanArrayExtra("setting_boolean");
			}
			if (setting_byte == null) {
				if (getIntent == null)
					getIntent = getIntent();
				setting_byte = getIntent.getByteArrayExtra("setting_byte");
			}
			// スマホ版だったら、設定を反映
			if (setting_byte[43] == 0) {
				new SPWVConfigLoop().execute();
			}

			layoutPlayer();
			// Square(正方形) エミュレータではこの値は返って来ない。
			// if (config.orientation == Configuration.ORIENTATION_SQUARE)
			if (dialog != null) {
				dialog.dismiss();
			}
			if(doDelay){
				Log.d("NLiveRoid","DO DELAY" + setting_byte[31]);
				doDelay = false;
						NLiveRoid app = (NLiveRoid) getApplicationContext();
						try{
							//コメント欄の初期化
							short init_comment_count = getIntent.getShortExtra(
									"init_comment_count", (short) 20);
							if (setting_byte[31] == 0) {//前面
								// オーバーレイを起動する
								try {
									liveInfo.serializeBitmap();
									if (overlay == null) {
										overlay = new Intent(ACT, OverLay.class);
									}
									if (cmd != null) {
										overlay.putExtra("cmd", cmd);
									}
									overlay.putExtra("pid", Process.myPid());
									overlay.putExtra("setting_boolean",
											setting_boolean);
									overlay.putExtra("setting_byte", setting_byte);
									overlay.putExtra("init_comment_count",
											init_comment_count);
									overlay.putExtra("isnsen", getIntent.getBooleanExtra("isnsen", false));
									overlay.putExtra("speech_skip_word", getIntent
											.getStringExtra("speech_skip_word"));
									overlay.putExtra("column_seq", column_seq);
									overlay.putExtra("twitterToken", getIntent.getStringExtra("twitterToken"));
									overlay.putExtra("Cookie", getIntent.getStringExtra("Cookie"));
									overlay.putExtra("sp_session", getIntent.getStringExtra("sp_session"));
									overlay.putExtra("viewW", getIntent
											.getIntExtra("viewW",
													getWindowManager()
															.getDefaultDisplay()
															.getWidth()));
									overlay.putExtra("viewH", getIntent
											.getIntExtra("viewH",
													getWindowManager()
															.getDefaultDisplay()
															.getHeight()));
									overlay.putExtra("density", getIntent
											.getFloatExtra("density", 1.5F));
									overlay.putExtra("notification", getIntent
											.getBooleanExtra("notification", false));
									overlay.putExtra("LiveInfo", liveInfo);
									overlay.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
									isOverlayStarted = true;
									startActivityForResult(overlay,
											CODE.REQUEST_OVERLAY);

								} catch (RuntimeException e) {
									Log.d("NLiveRoid", "FLASH ERROR RUNTIME ");
									e.printStackTrace();
								}

							} else if (setting_byte[31] == 2) {// Playerのみ
								app.createGateInstance();
							} else if (setting_byte[31] == 1
									|| setting_byte[31] == 3) {// 背面,コメントのみ
								// gateを普通に表示する為
								app.createGateInstance();
								new SameLayerModeStart().execute();
							}// End of setting_byte[31] == if


					} catch (PatternSyntaxException e) {
						Log.d("NLiveRoid",
								"INIT PLAYER FAILDED PATTERN MISSMATCH CODE : 0");
						MyToast.customToastShow(app,
								"プレイヤーの初期化に失敗しました\nパターンミスマッチ code:0");
						ACT.standardFinish();
					} catch (IllegalArgumentException e) {
						Log.d("NLiveRoid",
								"INIT PLAYER FAILDED PATTERN MISSMATCH CODE : 1");
						MyToast.customToastShow(app,
								"プレイヤーの初期化に失敗しました\nパターンミスマッチ code:1");
						ACT.standardFinish();
					} catch (IndexOutOfBoundsException e) {
						Log.d("NLiveRoid",
								"INIT PLAYER FAILDED MATCHER MISSMATCH CODE : 0");
						MyToast.customToastShow(app,
								"プレイヤーの初期化に失敗しました\nセッションミスマッチ code:0");
						ACT.standardFinish();
					}


			}
		}

		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			try {
				// 前面設定の時プレミアムに割り込まれてもインテント投げない
				/*
				 * 例)
				 * http://www.nicovideo.jp/premiumentry?sec=nicolive_oidashi&sub
				 * =watchplayer_oidashialert_0_official_lv89829663_onair
				 *
				 * 参加 http://ch.nicovideo.jp/community/co1536961 放送を見に行く
				 * http://sp.live.nicovideo.jp/watch/lv94873972?cr=1
				 */
				if(NLiveRoid.isDebugMode)Log.d("NLiveRoid", "should --- " + url);

				if (isOverlayStarted) {// (前面時onUserLeaveHintが呼ばれないようにする)

				} else {
					final Matcher lv = Pattern.compile("lv[0-9]+").matcher(url);
					if (lv.find()) {
						// 放送を見に行くかダイアログ
						new AlertDialog.Builder(ACT)
								.setTitle("移動")
								.setMessage("放送を見に行きますか?(視聴終了します)")
								.setPositiveButton("YES",
										new DialogInterface.OnClickListener() {
											@Override
											public void onClick(
													DialogInterface dialog,
													int which) {
												// 終了してURLのページをLV又はURLで開く
												ACT.lv_url_Finish(lv.group());
											}
										})
								.setNegativeButton("CANCEL",
										new DialogInterface.OnClickListener() {
											@Override
											public void onClick(
													DialogInterface dialog,
													int which) {
											}
										}).create().show();
					} else {
						final Matcher co = Pattern.compile("co[0-9]+").matcher(
								url);
						if (co.find()) {
							// コミュ参加/退会
							if (getIntent == null)
								getIntent = ACT.getIntent();
							new CommunityInfoTask(ACT, co.group(),
									getIntent.getStringExtra("Cookie"), webViewW)
									.execute();
						} else {
							Uri uri = Uri.parse(url);
							Intent browserIntent = new Intent(
									Intent.ACTION_VIEW);
							browserIntent
									.addCategory(Intent.CATEGORY_BROWSABLE);
							browserIntent.setDataAndType(uri, "text/html");
							startActivityForResult(browserIntent,
									CODE.RESULT_REDIRECT);// リダイレクトコードを持たせる
						}
					}
				}
			} catch (ActivityNotFoundException e) {
				// 更新して開いたSWFで起動できるアプリを呼ぼうとしてしまう?
			}
			return (true);
		}
	}

	//プレイヤーだけを今の設定値どおりに配置するメソッド
	private void layoutPlayer(){
		if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
			if (setting_byte[43] == 0) {
				switch (setting_byte[22]) {// スマホ版 縦
				case 0:// 上
					// wvにパディング設定すると、flashが動かせなくなる
					((LinearLayout) wv.getParent()).setPadding(0, 0, 0, 0);
					wv.loadUrl("javascript:document.getElementById('flvplayer').width="
							+ webViewW);
					break;
				case 1:// 下
					((LinearLayout) wv.getParent()).setPadding(0,
							(int) (pixcelViewH / 2 ), 0, 0);
					wv.loadUrl("javascript:document.getElementById('flvplayer').width="
							+ webViewW);
					break;
				}
				// 縦幅は共通
				wv.loadUrl("javascript:document.getElementById('flvplayer').height="
						+ ((webViewH / 2)));
				wv.setLayoutParams(new LinearLayout.LayoutParams(pixcelViewW,pixcelViewH/2));
			} else if(setting_byte[43] == 1){// PC版 縦
				switch (setting_byte[22]) {
				case 0:// 上
					((LinearLayout) wv.getParent()).setPadding(0, 0, 0, 0);
					wv.loadUrl("javascript:document.getElementById('flvplayer').width="
							+ resizeW);
					break;
				case 1:// 下
					((LinearLayout) wv.getParent()).setPadding(0,
							(int) (pixcelViewH / 2 ), 0, 0);
					wv.loadUrl("javascript:document.getElementById('flvplayer').width="
							+ resizeW);
					break;
				}
				// 縦幅は共通
				wv.loadUrl("javascript:document.getElementById('flvplayer').height="
						+ resizeH);
				wv.setLayoutParams(new LinearLayout.LayoutParams(pixcelViewW,(int) (pixcelViewH*5/8)));//resizeHの中でどこまでがプレイヤーなのかわからないので致し方ない
			}else if(setting_byte[43] == 2){
				if(hlsSurface != null){
					hlsSurface.setLayoutParams(new LinearLayout.LayoutParams(pixcelViewW,pixcelViewH/2));
				}
			}
		} else {
			if (setting_byte[43] == 0) {// スマホ版 横
				switch (setting_byte[23]) {// 1:1にする
				case 0:// 左
					((LinearLayout) wv.getParent()).setPadding(0, 0, 0, 0);
					wv.loadUrl("javascript:document.getElementById('flvplayer').width="
							+ webViewW);
					wv.setLayoutParams(new LinearLayout.LayoutParams(pixcelViewW,pixcelViewW));
					break;
				case 1:// 右
					((LinearLayout) wv.getParent()).setPadding(
							(int) ((webViewH - webViewW) * density), 0, 0, 0);
					wv.loadUrl("javascript:document.getElementById('flvplayer').width="
							+ webViewW);
					wv.setLayoutParams(new LinearLayout.LayoutParams(pixcelViewH,pixcelViewW));//何故か横幅がpixcelViewH無いと見えない
					break;
				case 2:// 全面
					((LinearLayout) wv.getParent()).setPadding(0, 0, 0, 0);
					wv.loadUrl("javascript:document.getElementById('flvplayer').width="
							+ webViewH);
					wv.setLayoutParams(new LinearLayout.LayoutParams(pixcelViewH,pixcelViewW));
					break;
				}
				// 縦幅は共通
				wv.loadUrl("javascript:document.getElementById('flvplayer').height="
						+ webViewW);

			} else if(setting_byte[43] == 1){// PC版 横
				switch (setting_byte[23]) {
				case 0:// 左
					((LinearLayout) wv.getParent()).setPadding(0, 0, 0, 0);
					wv.loadUrl("javascript:document.getElementById('flvplayer').width=100%");
					break;
				case 1:// 右
					((LinearLayout) wv.getParent()).setPadding(
							(int) ((int) (webViewH - webViewW + 10) * density),
							0, 0, 0);
					wv.loadUrl("javascript:document.getElementById('flvplayer').width="
							+ resizeH * 0.57);
					break;
				case 2:// 切り替えでありえる
					((LinearLayout) wv.getParent()).setPadding(0, 0, 0, 0);
					wv.loadUrl("javascript:document.getElementById('flvplayer').width=100%");
					break;
				}
				// 縦幅は共通
				wv.loadUrl("javascript:document.getElementById('flvplayer').height="
						+ resizeW * 0.53);
				wv.setLayoutParams(new LinearLayout.LayoutParams(pixcelViewH,pixcelViewW));//右の時も別に右側に原宿のコメント欄表示していいからここは共通
			}else if(setting_byte[43] == 2){
				if(hlsSurface != null){
					hlsSurface.setLayoutParams(new LinearLayout.LayoutParams(pixcelViewH,pixcelViewW));
				}
			}
			if(NLiveRoid.isDebugMode)Log.d("NLiveRoid","layoutWV " + pixcelViewW + " " + pixcelViewH +" "+ webViewW + " " + webViewH + " " + resizeW + " " + resizeH +" " + density );
		}
	}

	/**
	 * ビューのコンポーネント周り
	 */

	class MultiTouchListener implements OnTouchListener {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			// タッチリスナは普通のヘッダと最初の青ヘッダのみ
			// getX()とかgetY()とかはリスナにセットされたviewに対しての座標なので相対的に使う
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:// 1点目 ID 0
				if(NLiveRoid.isDebugMode)Log.d("NLiveRoid","FonTouchM D "+ event +" "+ parent);
				if (enable_moveX || enable_moveY) {// ドラッグどちらかが有効な場合、青ヘッダーを上のレイヤーに乗せる

					isFirstMoving = true;
					int y1 = (int) event.getY(0);
					int x1 = (int) event.getX(0);

					Rect rect1 = new Rect();
					headerview.getGlobalVisibleRect(rect1);
					// リスト下限を最後の値で初期化しておく
					// 最初にタップして、すぐに指を放したらリストのheightが消えるのを防ぐ
					int top = rect1.top;
					secondcurrentY = top;

					if (enable_moveY) {// uplayotと共通
						firstoffsetY = y1;
						firstcurrentY = rect1.top;
					}
					if (enable_moveX) {
						firstoffsetX = x1;
						firstcurrentX = rect1.left == 0 ? isPortLayt ? -(pixcelViewW - rect1.right)
								: -(pixcelViewH - rect1.right)
								: rect1.left;
					}

					firstBlueBlank = getHeaderBlank();

					firstBlueBlank.removeAllViews();
					firstBlueBlank.addView(firstBlueHeader,
							new LinearLayout.LayoutParams(list_width, cellHeight));
					// 上のレイヤに加える
					FrameLayout fl1 = ((FrameLayout) parent
							.findViewById(R.id.layer2));
					fl1.removeAllViews();
					fl1.addView(firstBlueBlank, new FrameLayout.LayoutParams(
							-1, -1));

					listBlank.setVisibility(View.INVISIBLE);
					headerBlank.setVisibility(View.INVISIBLE);
					listview.setVisibility(View.INVISIBLE);
					headerview.setVisibility(View.INVISIBLE);

					// 上に乗せたViewを見えるようにする
					firstBlueHeader.setVisibility(View.VISIBLE);
					firstBlueBlank.setVisibility(View.VISIBLE);
				}
				break;
			case MotionEvent.ACTION_POINTER_2_DOWN:// 2点目のタップ
				if (isFirstMoving) {// 2点目 isFirstMovingはtrueであろうが確実性のため
					int y2 = (int) event.getY(1);
					isSecondMoving = true;
					headerview.requestDisallowInterceptTouchEvent(false);
					// 移動対象Viewの現在座標を取得する yは今の位置、リストのタッチイベントで呼ばれてくる
					Rect rect2 = new Rect();
					listview.getGlobalVisibleRect(rect2);
					if (isUplayout()) {
						secondoffsetY = y2;
						secondcurrentY = rect2.top + y2 + (-list_bottom);// マイナスなはずなので足す
					} else {
						secondoffsetY = y2;
						secondcurrentY = rect2.top + y2;
					}
					secondBlank = getSimpleBlueHeaderBlank();

					// 2点目の青ヘッダーを上のレイヤーに乗せる
					secondBlank.removeAllViews();
					secondBlank.addView(secondBlueHeader,
							new LinearLayout.LayoutParams(list_width, cellHeight));

					// 2点目用の上のレイヤに加える
					FrameLayout fl2 = ((FrameLayout) parent
							.findViewById(R.id.layer3));
					fl2.removeAllViews();
					fl2.addView(secondBlank, new FrameLayout.LayoutParams(-1,
							-1));
				secondBlank.setVisibility(View.VISIBLE);
				secondBlueHeader.setVisibility(View.VISIBLE);
				}
				break;
			case MotionEvent.ACTION_MOVE:// 1点目2点目共通
				if (isSecondMoving && secondBlueHeader != null) {

					int y2 = 0;
					try {// ここでエラーすることがある(2点目が無い場合がある IllegalArgumentException)
						y2 = (int) event.getY(1);
					} catch (IllegalArgumentException e) {
						Log.d("NLiveRoid", "IllegalArgumentException in Touch");
						return true;
					}
					// xは変える必要なし

					if (enable_moveY) {
						int diffY = secondoffsetY - y2;
						secondcurrentY -= diffY;
						secondoffsetY = y2;
					}

					// preparePositionValue2();//すると下限いっぱいまで広げられなくなっちゃう
					// 上のレイヤーに乗せたViewの描画内容を更新する
					secondBlank.setPadding(firstcurrentX, secondcurrentY,
							-firstcurrentX, 0);
					secondBlueHeader.layout(firstcurrentX, secondcurrentY,
							pixcelViewW, cellHeight);
					break;
				} else if (isFirstMoving && firstBlueHeader != null) {
					int y = (int) event.getY(0);
					int x = (int) event.getX(0);
					if (enable_moveX) {
						int diffX = firstoffsetX - x;
						// 現在座標
						firstcurrentX -= diffX;
						// タッチ座標情報を更新する
						firstoffsetX = x;
					}
					if (enable_moveY) {
						int diffY = firstoffsetY - y;
						firstcurrentY -= diffY;
						firstoffsetY = y;
					}

//					preparePositionValue();
					// 上のレイヤーに乗せたViewの描画内容を更新する
					firstBlueBlank.setPadding(firstcurrentX, firstcurrentY,
							-firstcurrentX, 0);
					firstBlueHeader.layout(firstcurrentX, firstcurrentY,
							pixcelViewW, cellHeight);

				}
				break;
			case MotionEvent.ACTION_POINTER_2_UP:// 2点目のアップ
//				preparePositionValue();
				headerview.requestDisallowInterceptTouchEvent(true);
				if (isSecondMoving) {
					// 上のレイヤーから乗せたViewを除去する
					((FrameLayout) parent.findViewById(R.id.layer3))
							.removeView(secondBlank);
					secondBlank.removeView(secondBlueHeader);
					// ボトム位置の確定
					Rect rect2 = new Rect();
					firstBlueHeader.getGlobalVisibleRect(rect2);
					list_bottom = secondcurrentY - rect2.top;

					// 組み合わせ的にはdown→down down→up up→up up→down
					if (isUplayout()) {
						if (list_bottom < 0) {/* 何もしない */
						} else {// レイアウト変更した
							isUplayout = false;
							if (commentTable != null) {
								commentTable.manualSort();
							}
						}
					} else {
						if (list_bottom < 0) {// レイアウト変更した
							isUplayout = true;
							if (commentTable != null) {
								commentTable.manualSort();
							}
						} else {/* 何もしない */
						}
					}
					isSecondMoving = false;
				}
				break;
			case MotionEvent.ACTION_UP:
//				preparePositionValue();
				if (isFirstMoving) {
					// 上のレイヤーから乗せたViewを除去する
					((FrameLayout) parent.findViewById(R.id.layer2))
							.removeView(firstBlueBlank);
					if(firstBlueBlank != null)firstBlueBlank.removeView(firstBlueHeader);
					if (rootFrame == null) {// レイヤーチェンジとか入れたのでなる
						if(setting_byte[31] == 0 || setting_byte[31] == 2)return false;
						rootFrame = (ViewGroup) parent.findViewById(
								R.id.list_parent_liner).getParent();
					}
					listview = getList();

					// 元のレイヤーを作り直してadd(アドの順番でorderが決まる)
					if (isUplayout()) {
						listBlank = getListBlank_Up();
						listBlank
								.addView(listview,
										new FrameLayout.LayoutParams(list_width,
												(-list_bottom)));
						listview.setPadding(0, 0, 0, 0);
						listview.setSelection(0);// 強制スクロール末尾

					} else {
						listBlank = getListBlank_Down();
						listBlank.addView(listview,
								new FrameLayout.LayoutParams(list_width, list_bottom
										+ cellHeight));
						listview.setPadding(0, cellHeight, 0, 0);
						listview.setSelection(listview.getCount());// 強制スクロール末尾

					}

					headerBlank = getHeaderBlank();
					headerview = getHeader();
					if(headerview == null){
						errorFinish(CODE.RESULT_FLASH_ERROR, -13);//コメサバに接続できてない(非対応CH・公式)時になる
						return false;
					}

					rootFrame.addView(listBlank, new FrameLayout.LayoutParams(
							-1, -2));
					rootFrame.addView(headerBlank,
							new FrameLayout.LayoutParams(-1, -2));
					headerBlank.addView(headerview,
							new FrameLayout.LayoutParams(list_width, -2));
					// バッファ中をアドしなおす
					rootFrame.removeView(bufferMark);
					rootFrame.addView(bufferMark);
					// Foregroundに来るようにポスト一式もアドしておく
					((ViewGroup) parent).removeView((postArea));
					((ViewGroup) parent).addView(postArea);
					postArea.clearFocus();// ソフトキー出なくなるのを回避
					listview.setVisibility(View.VISIBLE);
					headerview.setVisibility(View.VISIBLE);
					listBlank.setVisibility(View.VISIBLE);
					headerBlank.setVisibility(View.VISIBLE);
					// 引き継ぐ値を代入
					if (setting_byte == null) {
						// テーブルに必要な値が全て消えていた場合 念には念
						if (setting_byte == null) {
							if (getIntent == null)
								getIntent = getIntent();
							setting_byte = getIntent
									.getByteArrayExtra("setting_byte");
							// クラッシュ確定
							if (setting_byte == null) {
								// errorFinish()
							}
						}
					}
					// この値を保存するので若干保存する値がずれる、
					// タッチイベント起こさなければ、table_intがそのまま保存されるのでずれないのでOKとした
					if (isPortLayt) {
						setting_byte[8] = (byte) (firstcurrentX * 100 / pixcelViewW);
						setting_byte[9] = (byte) (firstcurrentY * 100 / pixcelViewH);
						setting_byte[10] = (byte) (list_bottom * 100 / pixcelViewH);
					} else {
						setting_byte[19] = (byte) (firstcurrentX * 100 / pixcelViewH);
						setting_byte[20] = (byte) (firstcurrentY * 100 / pixcelViewW);
						setting_byte[21] = (byte) (list_bottom * 100 / pixcelViewW);
					}

					isFirstMoving = false;
					isSecondMoving = false;// 何故かfalseにならない事がある
					if (secondBlank != null) {
						if (secondBlueHeader != null) {
							secondBlank.removeView(secondBlueHeader);
						}
						((FrameLayout) parent.findViewById(R.id.layer3))
								.removeView(secondBlank);
					}

				}
				break;
			}
			return true;
		}
	}

	class SimpleTouchListener implements OnTouchListener {
		@Override
		public boolean onTouch(View v, MotionEvent event) {

			// タッチリスナは普通のヘッダと最初の青ヘッダのみ
			// getX()とかgetY()とかはリスナにセットされたviewに対しての座標なので相対的に使う
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:// 1点目 ID 0

				if (enable_moveX || enable_moveY) {// ドラッグどちらかが有効な場合、青ヘッダーを上のレイヤーに乗せる

					int y1 = (int) event.getY();
					int x1 = (int) event.getX();
					isFirstMoving = true;
					Rect rect1 = new Rect();
					headerview.getGlobalVisibleRect(rect1);
					// リスト下限を最後の値で初期化しておく
					// 最初にタップして、すぐに指を放したらリストのheightが消えるのを防ぐ
					int top = rect1.top;

					if (enable_moveY) {// uplayotと共通
						firstoffsetY = y1;
						firstcurrentY = rect1.top;
					}
					if (enable_moveX) {
						firstoffsetX = x1;
						firstcurrentX = rect1.left == 0 ? isPortLayt ? -(pixcelViewW - rect1.right)
								: -(pixcelViewH - rect1.right)
								: rect1.left;
					}

					firstBlueBlank = getHeaderBlank();

					firstBlueBlank.removeAllViews();
					firstBlueBlank.addView(firstBlueHeader,
							new LinearLayout.LayoutParams(list_width, cellHeight));
					// 上のレイヤに加える
					FrameLayout fl1 = ((FrameLayout) parent
							.findViewById(R.id.layer2));
					fl1.removeAllViews();
					fl1.addView(firstBlueBlank, new FrameLayout.LayoutParams(
							-1, -1));

					listBlank.setVisibility(View.INVISIBLE);
					headerBlank.setVisibility(View.INVISIBLE);
					listview.setVisibility(View.INVISIBLE);
					headerview.setVisibility(View.INVISIBLE);

					// 上に乗せたViewを見えるようにする
					firstBlueHeader.setVisibility(View.VISIBLE);
					firstBlueBlank.setVisibility(View.VISIBLE);
				}
				break;
			case MotionEvent.ACTION_MOVE:// 1点目2点目共通
				if (isFirstMoving && firstBlueHeader != null) {
					int y = (int) event.getY();
					int x = (int) event.getX();
					if (enable_moveX) {
						int diffX = firstoffsetX - x;
						// 現在座標
						firstcurrentX -= diffX;
						// タッチ座標情報を更新する
						firstoffsetX = x;
					}
					if (enable_moveY) {
						int diffY = firstoffsetY - y;
						firstcurrentY -= diffY;
						firstoffsetY = y;
					}

//					preparePositionValue();
					// 上のレイヤーに乗せたViewの描画内容を更新する
					firstBlueBlank.setPadding(firstcurrentX, firstcurrentY,
							-firstcurrentX, 0);
					firstBlueHeader.layout(firstcurrentX, firstcurrentY,
							pixcelViewW, cellHeight);

				}
				break;
			case MotionEvent.ACTION_UP:
//				preparePositionValue();
				if (isFirstMoving) {
					// 上のレイヤーから乗せたViewを除去する
					((FrameLayout) parent.findViewById(R.id.layer2))
							.removeView(firstBlueBlank);
					firstBlueBlank.removeView(firstBlueHeader);
					if (rootFrame == null) {// レイヤーチェンジとか入れたのでなる
						if(setting_byte[31] == 0 || setting_byte[31] == 2)return false;
						rootFrame = (ViewGroup) parent.findViewById(
								R.id.list_parent_liner).getParent();
					}
					listview = getList();

					// 元のレイヤーを作り直してadd(アドの順番でorderが決まる)
					if (isUplayout()) {
						listBlank = getListBlank_Up();
						listBlank
								.addView(listview,
										new FrameLayout.LayoutParams(list_width,
												(-list_bottom)));
						listview.setPadding(0, 0, 0, 0);
						listview.setSelection(0);// 強制スクロール末尾

					} else {
						listBlank = getListBlank_Down();
						listBlank.addView(listview,
								new FrameLayout.LayoutParams(list_width, list_bottom
										+ cellHeight));
						listview.setPadding(0, cellHeight, 0, 0);
						listview.setSelection(listview.getCount());// 強制スクロール末尾

					}

					headerBlank = getHeaderBlank();
					headerview = getHeader();
					if(headerview == null){
						errorFinish(CODE.RESULT_FLASH_ERROR, -13);//コメサバに接続できてない(非対応CH・公式)時になる
						return false;
					}

					rootFrame.addView(listBlank, new FrameLayout.LayoutParams(
							-1, -2));
					rootFrame.addView(headerBlank,
							new FrameLayout.LayoutParams(-1, -2));
					headerBlank.addView(headerview,
							new FrameLayout.LayoutParams(list_width, -2));
					// バッファ中をアドしなおす
					rootFrame.removeView(bufferMark);
					rootFrame.addView(bufferMark);
					// Foregroundに来るようにポスト一式もアドしておく
					((ViewGroup) parent).removeView((postArea));
					((ViewGroup) parent).addView(postArea);
					postArea.clearFocus();// ソフトキー出なくなるのを回避
					listview.setVisibility(View.VISIBLE);
					headerview.setVisibility(View.VISIBLE);
					listBlank.setVisibility(View.VISIBLE);
					headerBlank.setVisibility(View.VISIBLE);
					// 引き継ぐ値を代入
					if (setting_byte == null) {
						// テーブルに必要な値が全て消えていた場合 念には念

						if (setting_byte == null) {
							if (getIntent == null)
								getIntent = getIntent();
							setting_byte = getIntent
									.getByteArrayExtra("setting_byte");
							;
						}

					}
					// この値を保存するので若干保存する値がずれる、
					// タッチイベント起こさなければ、table_intarrayがそのまま保存されるのでずれないのでOKとした
					if (isPortLayt) {
						setting_byte[8] = (byte) (firstcurrentX * 100 / pixcelViewW);
						setting_byte[9] = (byte) (firstcurrentY * 100 / pixcelViewH);
						setting_byte[10] = (byte) (list_bottom * 100 / pixcelViewH);
					} else {
						setting_byte[19] = (byte) (firstcurrentX * 100 / pixcelViewH);
						setting_byte[20] = (byte) (firstcurrentY * 100 / pixcelViewW);
						setting_byte[21] = (byte) (list_bottom * 100 / pixcelViewW);
					}

					isFirstMoving = false;
					isSecondMoving = false;// 何故かfalseにならない事がある
					if (secondBlank != null) {
						if (secondBlueHeader != null) {
							secondBlank.removeView(secondBlueHeader);
						}
						((FrameLayout) parent.findViewById(R.id.layer3))
								.removeView(secondBlank);
					}

				}
				break;
			}

			return true;
		}
	}// End of SimpleTouchListener

	/**
	 * isScrollEndを取得する CommentTableからのみ呼び出される
	 *
	 * @return
	 */
	public boolean isScrollEnd() {
		return isScrollEnd;
	}

	private ListView getList() {
		final ListView list = new ListView(this);
		registerForContextMenu(list);
		list.setFastScrollEnabled(true);
		list.setAdapter(adapter);
		if(setting_boolean[27]){
		list.setBackgroundColor(Color.BLACK);
		}else{
		list.setBackgroundColor(-1);
		}
		list.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScroll(AbsListView view, int firstVisible,
					int visibleItemCount, int totalItemCount) {
				if (isUplayout()) {
					if (firstVisible == 0) {
						if (!isScrollEnd) {
							isScrollEnd = true;
							if (commentTable != null) {
								commentTable.scrollEnded();
							}
						} else {
							isScrollEnd = true;
						}
					} else {
						isScrollEnd = false;
					}
				} else {
					if (totalItemCount == firstVisible + visibleItemCount) {// スクロール入ってた
						if (!isScrollEnd) {
							isScrollEnd = true;
							if (commentTable != null) {
								commentTable.scrollEnded();
							}
						} else {// ただスクロールエンド入れる
							isScrollEnd = true;
						}
					} else {// スクロールはずれた
							// 初回バッファでスクロールできなくなるので応急修正
						isScrollEnd = false;
					}
				}
				if (bufferMark == null) {
					bufferMark = (LinearLayout) parent
							.findViewById(R.id.buffering_area);
					bufferMark.setVisibility(View.GONE);
				}
				if (commentTable != null && !commentTable.getIsBuffering()) {// バッファ中
					bufferMark.setVisibility(View.INVISIBLE);
				}
			}

			@Override
			public void onScrollStateChanged(AbsListView arg0, int arg1) {
			}
		});
		return list;
	}

	/**
	 * ヘッダーは常にy_posの位置
	 *
	 * @return
	 */
	private LinearLayout getHeader() {
		if (setting_boolean == null) {
			if (getIntent == null)
				getIntent = getIntent();
			setting_boolean = getIntent.getBooleanArrayExtra("setting_boolean");
		}
		if (setting_boolean[1]) {
			TableLayout row = (TableLayout) inflater.inflate(
					R.layout.newline_header, null);
			if(row == null || column_seq == null || column_ids == null){//対応してない放送又は想定してない放送の場合ここがtrue→このメソッドはgetplayerの後に呼ばれるので、この時点で接続に失敗している
				return null;
			}
			TextView tv = null;
			for (int i = 0; i < column_seq.length; i++) {
				tv = (TextView) row.findViewById(column_ids[i]);
				if (tv == null) {
					continue;
				} else {
					tv.setText(URLEnum.ColumnText[column_seq[i]]);
				}
				if (i == 5) {
					NewLineHeader num = (NewLineHeader) row
							.findViewById(column_ids[column_seq[i]]);// 左寄せはseq5で共通にしておく
					num.setGravity(Gravity.LEFT);
				}
			}

			if (NLiveRoid.apiLevel >= 8) {
				row.setOnTouchListener(new MultiTouchListener());
			} else {
				row.setOnTouchListener(new SimpleTouchListener());
			}
			row.setBackgroundColor(Color.parseColor("#b9b9b9"));
			return row;
		} else {
			LinearLayout row = (LinearLayout) inflater.inflate(row_resource_id,
					null);
			if(row == null || column_seq == null || column_ids == null){//対応してない放送又は想定してない放送の場合ここがtrue→このメソッドはgetplayerの後に呼ばれるので、この時点で接続に失敗している
				return null;
			}
			TextView tv = null;
			for (int i = 0; i < column_seq.length; i++) {
				tv = (TextView) row.findViewById(column_ids[i]);

				if (tv == null) {
					continue;
				} else {
					tv.setText(URLEnum.ColumnText[column_seq[i]]);
				}
				if (i == 5) {
					CommentTableCell num = (CommentTableCell) row
							.findViewById(column_ids[column_seq[i]]);// 左寄せはseq5で共通にしておく
					num.setGravity(Gravity.LEFT);
				}
			}
			if (NLiveRoid.apiLevel >= 8) {
				row.setOnTouchListener(new MultiTouchListener());
			} else {
				row.setOnTouchListener(new SimpleTouchListener());
			}
			row.setBackgroundColor(Color.parseColor("#b9b9b9"));
			return row;
		}
	}

	/**
	 * DownのリストBlank
	 *
	 * @return
	 */
	private LinearLayout getListBlank_Down() {
		LinearLayout blank = new LinearLayout(this);
		blank.setPadding(firstcurrentX, firstcurrentY, -firstcurrentX, 0);
		return blank;
	}

	/**
	 * UpのリストBlank
	 *
	 * @return
	 */
	private LinearLayout getListBlank_Up() {
		LinearLayout blank = new LinearLayout(this);
		// ヘッダー位置より上にレイアウト=topのパッディングを減らす=マイナスなはずだから足す
		blank.setPadding(firstcurrentX, firstcurrentY + list_bottom,
				-firstcurrentX, 0);
		return blank;
	}

	/**
	 * DownのヘッダーBlank
	 *
	 * @return
	 */
	private LinearLayout getHeaderBlank() {
		LinearLayout blank = new LinearLayout(this);
		blank.setPadding(firstcurrentX, firstcurrentY, -firstcurrentX, 0);
		return blank;
	}

	private LinearLayout getSimpleBlueHeaderBlank() {
		LinearLayout blank = new LinearLayout(this);
		blank.setPadding(firstcurrentX, secondcurrentY, -firstcurrentX, 0);
		return blank;
	}

	private LinearLayout getBlueHeader() {

		LinearLayout row = (LinearLayout) inflater.inflate(
				R.layout.colorheader, null);
		if (NLiveRoid.apiLevel >= 8) {
			row.setOnTouchListener(new MultiTouchListener());
		} else {
			row.setOnTouchListener(new SimpleTouchListener());
		}
		row.setBackgroundColor(Color.parseColor("#ccffff"));
		return row;
	}

	private LinearLayout getSimpleBlueHeader() {
		LinearLayout row = (LinearLayout) inflater.inflate(
				R.layout.colorheader, null);
		row.setBackgroundColor(Color.parseColor("#c0ffa0"));
		// タッチリスナーセットするとviewの位置なのか画面上の位置なのかわりずらくなる
		return row;
	}

	private void preparePositionValue() {// ドラッグするリミットを設ける　何故かビッタリ合わない
		if (isPortLayt) {
			// 縦画面時X軸に対するリミット
			if (firstcurrentX < -(pixcelViewW - 10)) {
				firstcurrentX = -(pixcelViewW - 10);
			} else if (firstcurrentX > pixcelViewW - 10) {
				firstcurrentX = pixcelViewW - 10;
			}
			// 縦画面時Y軸に対するリミット
			if (firstcurrentY > pixcelViewH - cellHeight / 2 * 5) {
				firstcurrentY = pixcelViewH - cellHeight / 2 * 5;
			}

		} else {
			// 横画面X軸に対するリミット
			if (firstcurrentX < -(pixcelViewH - 10)) {
				firstcurrentX = -(pixcelViewH - 10);
			} else if (firstcurrentX > pixcelViewH - 10) {
				firstcurrentX = pixcelViewH - 10;
			}
			// 横画面Y軸に対するリミット
			if (firstcurrentY > pixcelViewW - cellHeight / 2 * 5) {
				firstcurrentY = pixcelViewW - cellHeight / 2 * 5;
			}
		}
	}

	/**
	 * コテハンファイルの読み込み
	 *
	 * @author Owner
	 *
	 */
	class ReadHandleName extends AsyncTask<Void,Void,Integer>{
		@Override
		protected Integer doInBackground(Void... params) {
			return readHandleNameData();
		}
		@Override
		protected void onPostExecute(Integer arg){
			if(arg == -1){
				MyToast.customToastShow(ACT, "コテハンファイル読み込みに失敗しました\n再起動等で回復又はデータが多すぎる可能背があります");
			}else if(arg == -2){
				MyToast.customToastShow(ACT, "コテハンファイルが不正\n又は読み取り失敗");
			}else if(arg == -3){
				MyToast.customToastShow(ACT, "コテハンファイルの色設定で不正な値");
			}else if(arg == -4){
				MyToast.customToastShow(ACT, "/sdcard/NLiveRoidフォルダがありませんでした");
			}else if(arg == -5){
				MyToast.customToastShow(ACT, "コテハンファイルが存在しない");
			}else if(arg == -6){
				MyToast.customToastShow(ACT, "コテハン読み込み時IOエラー");
			}
		}
	}
	private synchronized int readHandleNameData() {
		int returnVal = 0;
		try {
			if(NLiveRoid.isDebugMode)Log.d("NLiveRoid","F readHandleNameData ");
			String filepath = getStorageFilePath();
			if (filepath == null)
				return -4;
			FileInputStream fis = new FileInputStream(filepath);
			byte[] readBytes = new byte[fis.available()];
			fis.read(readBytes);
			returnVal = XMLparser.setHandleNameMaps(idToBgColor,idToForeColor, idToHandleName, readBytes);
			fis.close();
//			if(returnVal < 0){//消すとコテハン全部消えることになる
//				idToBgColor.clear();
//				idToForeColor.clear();
//				idToHandleName.clear();
//			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			returnVal = -5;
		} catch (IOException e) {
			e.printStackTrace();
			returnVal = -6;
		}finally{
		isSetNameReady = true;
		}
		if(NLiveRoid.isDebugMode)Log.d("NLiveRoid","F END readHandleNameData " + returnVal);
		return returnVal;
	}

	/**
	 * コテハンの書き込み
	 *
	 * @author Owner
	 *
	 */
	class WriteHandleName extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... params) {
			writeHandleName();
			return null;
		}
	}

	@Override
	public synchronized void writeHandleName() {
		try {

			if(NLiveRoid.isDebugMode)Log.d("NLiveRoid","F END writeHandleName ");
			String filepath = getStorageFilePath();
			if (filepath == null)
				return;
			FileOutputStream fos = new FileOutputStream(filepath);
			Iterator<String> itid = idToHandleName.keySet().iterator();
			Iterator<String> itBco = idToBgColor.keySet().iterator();
			Iterator<String> itFco = idToForeColor.keySet().iterator();
			String xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<HandleNames xmlns=\"http://nliveroid-tutorial.appspot.com/handlenames/\">\n";// nliveroid.co.cc/handlenames

			String id = "";
			for (;itid.hasNext();) {
				id = itid.next();
				xml += "<user bgcolor=\"" + idToBgColor.get(itBco.next()) + "\" name=\"" + idToHandleName.get(id)
						+ "\" focolor=\""+idToForeColor.get(itFco.next())+"\">" + id + "</user>\n";
			}
			xml += "</HandleNames>";
			fos.write(xml.getBytes());
			fos.close();

			if(NLiveRoid.isDebugMode)Log.d("NLiveRoid","F END writeHandleName " + (xml != null? xml.length():"XML NULL"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * ストレージのパスを取得します *
	 *
	 */

	private String getStorageFilePath() {
		boolean isStorageAvalable = false;
		boolean isStorageWriteable = false;
		String state = Environment.getExternalStorageState();
		if (state == null) {
			// MyToast.customToastShow(this, "SDカードが利用できませんでした\nコテハンは機能できません");
			return null;
		} else if (Environment.MEDIA_MOUNTED.equals(state)) {
			// 読み書きOK
			isStorageAvalable = isStorageWriteable = true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			// 読み込みだけOK
			isStorageAvalable = true;
			isStorageWriteable = false;
		} else {
			// ストレージが有効でない
			isStorageAvalable = isStorageWriteable = false;
		}

		boolean notAvalable = !isStorageAvalable;
		boolean notWritable = !isStorageWriteable;
		if (notAvalable || notWritable) {
			// MyToast.customToastShow(this, "SDカードが利用できませんでした\nコテハンは機能できません");
			return null;
		}

		// sdcard直下に、パッケージ名のフォルダを作りファイルを生成
		String filePath = Environment.getExternalStorageDirectory().toString();

		if (filePath == null) {
			// MyToast.customToastShow(this, "SDカードが利用できませんでした\nコテハンは機能できません");
			return null;
		}
		filePath = filePath + "/NLiveRoid";

		File directory = new File(filePath);//初回起動時で、ディレクトリ自体が無い時はnullじゃなく、書き込み権限も無い状態なので、mkdirする前で、そこをフックしてはいけない
		if (directory.mkdirs()) {// すでにあった場合も失敗する
			Log.d("log", "SUCCESS MKDIRS ");
		}
		File file = new File(filePath, handleFileName);
		if (!file.exists()) {
			try {
				file.createNewFile();
				writeHandleName();// 次からの読み込みがエラーしないように空のファイルを作っておく
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return file.getPath();
	}

	/**
	 * OverLayとこのクラスの間はResultがありにできる!
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// クルーズの時のフラッシュ等からブラウザに行って帰ってきた時はerrorもnull?
		 Log.d("NLiveRoid","onAct F ---- " + requestCode +"  " + resultCode);
		if (error == null) {// ALLFINISH
			finish();
		}
		if (requestCode == CODE.REQUEST_OVERLAY) {
			isOverlayStarted = false;// onUserLeaveで利用
		} else if (requestCode == CODE.RESULT_TRANS_LAYER) {//Gateから検索
			isOverlayStarted = false;// onUserLeaveで利用
			if (data != null) {
				if (data.getByteExtra("init_mode", (byte) -1) == 1) {// 透明ACTの放送詳細からタグ検索又は放送履歴からのTS視聴

					if (data.getBooleanExtra("error", false)) {
						MyToast.customToastShow(this,
								data.getStringExtra("error_message"));
						return;
					}//詳細からの戻りでタグ検索か、放送履歴からのTS視聴か
					Intent bcService = new Intent();
					bcService.setAction("return_f.NLR");
					bcService.putExtra("r_code", CODE.RESULT_FROM_GATE_FINISH);
					if(data.getStringExtra("tagword") != null){
						bcService.putExtra("tagword",
								data.getStringExtra("tagword"));
					}else if(data.getStringExtra("archive") != null){
						bcService.putExtra("archive",
								data.getStringExtra("archive"));
					}
					this.sendBroadcast(bcService);
					NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
					notificationManager.cancelAll();
					standardFinish();
				}
			}
		} else if (requestCode == CODE.RESULT_RECOGNIZE_SPEECH) {
			// 音声認識から
			// 結果文字列リスト
			AudioManager audio = (AudioManager) getSystemService(AUDIO_SERVICE);
			audio.setStreamVolume(AudioManager.STREAM_MUSIC, recognizeValue,
					0);
			recognizeValue = 0;

			if (data != null) {
				ArrayList<String> results = data
						.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
				if (results == null || results.size() == 0)
					this.finish();
				final String[] candidate = new String[results.size()];
				for (int i = 0; i < results.size(); i++) {
					// ここでは、候補がいくつか格納されてくるので結合しています
					candidate[i] = results.get(i);
				}
				// 候補をアラート表示
				new AlertDialog.Builder(this)
						.setItems(candidate,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {
										// 音声入力
										if (postET != null) {
											postET.setText(candidate[which]);
										}
									}
								}).create().show();
			}
		}

		if (resultCode == CODE.RESULT_COOKIE) {// OverLay正常終了
			storeOverLayData(data);
		} else if (resultCode == CODE.RESULT_FROM_GATE_FINISH) {//OverLayでのGateからタグ検索又は放送履歴からの終了は、単にOverLayを終了してタグ検索の場合は、Gateから直接SearchTabをキック、放送履歴からTSの場合はOvgerLayからBCSerciveにブロキャスするのでここでは何もしない
			if (data != null) {
				if(NLiveRoid.isDebugMode)Log.d("NLiveRoid","S onAct ");
				standardFinish();// 普通に終了する
			} else {
				errorFinish(CODE.RESULT_FLASH_ERROR, -12);// 設定ファイルの読み込み失敗としておく
			}
		} else if (resultCode == CODE.RESULT_OVERLAY_ERROR) {
			// OverLayで何かしらの放送取得エラー*このアクティビティは終了しないが、トーストのためにflash_errorとして扱ってもらう
			if (data == null || error == null) {
				MyToast.customToastShow(ACT, "予期せぬエラー\nこの放送の条件を報告願う!");
				return;
			}
			int errorCode = data.getIntExtra("overlay_error", 0);
			if(errorCode == -30 || errorCode == -49){
				noCommentServer = true;
			}else{
			this.errorFinish(CODE.RESULT_FLASH_ERROR, errorCode);// FLASHのエラーとして変換する
			}
		} else if (resultCode == CODE.RESULT_CLOSED) {// OverLayで放送終了していた*トーストのためにflash_errorとして扱ってもらう
			errorFinish(CODE.RESULT_FLASH_ERROR, -18);
		} else if (resultCode == CODE.RESULT_NOLOGIN) {// ログイン失敗でセッションを消す必要がある場合
			errorFinish(CODE.RESULT_NOLOGIN, -17);
		} else if (resultCode == CODE.RESULT_REDIRECT) {// リダイレクトから返ってきた
			Log.d("NLiveRoid", "RETURNED REDIRECT");
		} else if (resultCode == CODE.RESULT_ALL_UPDATE) {
			// すべて更新
			if (data != null) {
				if(data.getStringExtra("nsen") != null){
					liveInfo.setLiveID(data.getStringExtra("nsen"));
				}
				allUpdate();
			}
		}else if(resultCode == CODE.RESULT_LAYERCHANGE_TO_BACKFLASH){
			storeOverLayData(data);
			layerChange(1);
		}else if(resultCode == CODE.RESULT_LAYERCHANGE_TO_PLAYERONLY){
			storeOverLayData(data);
			layerChange(3);
		}else if (resultCode == CODE.RESULT_QUICKFINISH_OR_RINGING) {// 着信またはQuickMneuによる終了
			if(NLiveRoid.isDebugMode)Log.d("NLiveRoid","S RESULT_QUICKFINISH_OR_RINGING ");
			standardFinish();
		}

	}

	private void storeOverLayData(Intent data){
		cmd = (CommandMapping) data.getSerializableExtra("cmd");
		if (cmd == null) {
			// コマンドオブジェクトの初期化
			NLiveRoid app = (NLiveRoid) ACT.getApplicationContext();
			String[] cmdValue = new String[4];
			cmdValue[0] = app.getDetailsMapValue("cmd_cmd");
			cmdValue[1] = app.getDetailsMapValue("cmd_size");
			cmdValue[2] = app.getDetailsMapValue("cmd_color");
			cmdValue[3] = app.getDetailsMapValue("cmd_align");
			for (int i = 0; i < 4; i++) {
				if (cmdValue[i] != null) {
					if (i == 3) {
						cmd = new CommandMapping(cmdValue[0], cmdValue[1],
								cmdValue[2], cmdValue[3], false);
						break;
					}
				}
				if (i == 3) {
					cmd = new CommandMapping(false);
				}
			}
		}
		byte[] columnseq = data.getByteArrayExtra("column_seq");
		if(columnseq != null){
			column_seq = columnseq;
		}
		if (setting_boolean == null) {
			if (getIntent == null)
				getIntent = getIntent();
			setting_boolean = getIntent
					.getBooleanArrayExtra("setting_boolean");
		}
		if (setting_byte == null) {
			if (getIntent == null)
				getIntent = getIntent();
			setting_byte = getIntent.getByteArrayExtra("setting_byte");
		}
		if (setting_byte[31] == 0 && data != null) {// 前面時は設定を保存
			boolean[] boolArray = data
					.getBooleanArrayExtra("setting_boolean");
			byte[] byteArray = data.getByteArrayExtra("setting_byte");
			if (boolArray != null) {
				this.setting_boolean = boolArray;
			}
			if (byteArray != null) {
				this.setting_byte = byteArray;
			}
		}
	}

	private void stopWebView() {
		if(NLiveRoid.isDebugMode)Log.d("NLiveRoid"," stopWV");
		if (wv != null) {
			wv.stopLoading();
			wv.clearCache(true);
			wv.setWebChromeClient(null);
			wv.setWebViewClient(null);
			wv.destroyDrawingCache();
			wv = null;
		}
	}

	@Override
	public boolean showPostArea() {
		if(NLiveRoid.isDebugMode)Log.d("NLiveRoid"," showPostArea");

		if(noCommentServer){
			MyToast.customToastShow(ACT, "コメント取得できない公式・CHと判定されている為、投稿もできません");
			return false;
		}
		if (postArea == null) {
			return false;
		}
		if (postArea == null || postET == null || postB == null) {
			MyToast.customToastShow(ACT, "コメント取得に失敗している又はメモリが不足しています");
			return false;
		}

		if (setting_boolean == null) {
			if (getIntent == null)
				getIntent = getIntent();
			setting_boolean = getIntent
					.getBooleanArrayExtra("setting_boolean");
		}

		if (postArea.getVisibility() == View.GONE) {
			postArea.setVisibility(View.VISIBLE);
			postET.setVisibility(View.VISIBLE);
			postB.setVisibility(View.VISIBLE);
			//MENUキーの割り当てが投稿フォームなのにボタンとかがONじゃなかったら戻りようがなくなるので
			if((setting_byte[40] & 0xF0) >> 4 == 2&&(post_184 == null || post_command == null || post_desc == null || voiceInput == null || post_menu == null)){
				setting_boolean[3] = true;
				if(setting_boolean[2]){//イベントリスナでもここを聞いてるけどifの記述上しかたない
					post_184 = (CheckBox)parent.findViewById(R.id.postarea_184_up);
					post_command = (Button)parent.findViewById(R.id.postarea_command_up);
					post_update = (Button)parent.findViewById(R.id.postarea_update_up);
					post_menu = (Button)parent.findViewById(R.id.postarea_menukey_up);
					post_cdisp = (CheckBox)parent.findViewById(R.id.postarea_commentdisp_up);
					voiceInput = (Button) parent.findViewById(R.id.postarea_voiceinput_up);
					post_desc = (Button)parent.findViewById(R.id.postarea_desc_up);
					((ViewGroup)post_184.getParent()).setMinimumHeight((int) (30/((NLiveRoid)getApplicationContext()).getMetrics()));//操作ボタンを表示がOFFの場合、パディング入れないためにGONEにしてるので
				}else{
					post_184 = (CheckBox)parent.findViewById(R.id.postarea_184_down);
					post_command = (Button)parent.findViewById(R.id.postarea_command_down);
					post_update = (Button)parent.findViewById(R.id.postarea_update_down);
					post_cdisp = (CheckBox)parent.findViewById(R.id.postarea_commentdisp_down);
					post_desc = (Button)parent.findViewById(R.id.postarea_desc_down);
					voiceInput = (Button) parent.findViewById(R.id.postarea_voiceinput_down);
					post_menu = (Button)parent.findViewById(R.id.postarea_menukey_down);
				}
				setFormListeners();
			}
			if (setting_boolean[3]) {//ここは元からの操作ボタンを表示の設定ON時なのでMINHEIGHTのパディング処理は入れなくていいと思われる
				post_184.setVisibility(View.VISIBLE);
				post_command.setVisibility(View.VISIBLE);
				post_update.setVisibility(View.VISIBLE);
				post_desc.setVisibility(View.VISIBLE);
				post_cdisp.setVisibility(View.VISIBLE);
				voiceInput.setVisibility(View.VISIBLE);
				post_menu.setVisibility(View.VISIBLE);
			}
		} else {
			postArea.setVisibility(View.GONE);
			postET.setVisibility(View.GONE);
			postB.setVisibility(View.GONE);
			if (setting_boolean[3]) {
				post_184.setVisibility(View.GONE);
				post_command.setVisibility(View.GONE);
				post_update.setVisibility(View.GONE);
				post_desc.setVisibility(View.GONE);
				post_cdisp.setVisibility(View.GONE);
				voiceInput.setVisibility(View.GONE);
				post_menu.setVisibility(View.GONE);
			}
			// タブレットでキーボードが引っ込まないのを防ぐ　何故かダイアログを出すと消えてくれる。超ーーー意味わかんない
			Dialog dummy = new Dialog(ACT);
			dummy.show();
			dummy.dismiss();
		}
		return false;
	}
	@Override
	public void clearAdapter() {
	}

	public void changePlayer(byte index) {
			if (wv != null) {
				if (setting_byte == null) {
					if (getIntent == null)
						getIntent = getIntent();
					setting_byte = getIntent
							.getByteArrayExtra("setting_byte");
				}

				if (setting_byte[43] == 1) {
					wv.loadDataWithBaseURL(
							URLEnum.SP_WATCHBASEURL + liveInfo.getLiveID(),
							URLEnum.SPPLAYER.replace("%LIVEID%",
									liveInfo.getLiveID()), "text/html", "utf-8",
							null);
					setting_byte[43] = 0;
					getIntent.putExtra("setting_byte", setting_byte);
				} else if(setting_byte[43] == 0) {
					wv.loadDataWithBaseURL(
							URLEnum.PC_WATCHBASEURL + liveInfo.getLiveID(),
							URLEnum.PCPLAYER.replace("%LIVEID%",
									liveInfo.getLiveID()), "text/html", "utf-8",
							null);
					setting_byte[43] = 1;
					getIntent.putExtra("setting_byte", setting_byte);
				}
			} else if(setting_byte[43] != 2){
				MyToast.customToastShow(ACT, "プレイヤーが読み込まれていません");
			}
	}

	@Override
	public void showCommandDialog() {
		if (getIntent == null)
			getIntent = getIntent();
		new CommandDialog(this, false, getIntent.getStringExtra("Cookie"),
				liveInfo.getLiveID()).show();
	}

	@Override
	public void showOrientationAlertBuilder() {
		final String[] spinnerAdapter = new String[3];
		spinnerAdapter[0] = "回転する";
		spinnerAdapter[1] = "縦固定";
		spinnerAdapter[2] = "横固定";
		if (setting_byte == null) {
			if (getIntent == null)
				getIntent = getIntent();
			setting_byte = getIntent.getByteArrayExtra("setting_byte");
			;
		}
		new AlertDialog.Builder(this)
				.setTitle(spinnerAdapter[setting_byte[24]])
				.setItems(spinnerAdapter,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								setOrientation(which);
							}
						}).show();
	}

	@Override
	public Context getAPPContext() {
		return this.getBaseContext();
	}

	@Override
	public void reloadPlayer() {
		if (wv != null) {
			loadWV(null);
		}
	}

	@Override
	public void openInitCommentPicker() {
		if (setting_byte == null) {
			if (setting_byte == null)
				getIntent = getIntent();
			setting_byte = getIntent.getByteArrayExtra("setting_byte");
		}

		ScrollView sv = new ScrollView(this);
		final NumberPicker np = new NumberPicker(this);
		np.setRange(0, 100);
		np.setCurrent(getIntent.getShortExtra("init_comment_count", (short) 20));
		sv.addView(np, -1, -1);
		AlertDialog.Builder npDialog = new AlertDialog.Builder(this);
		npDialog.setView(sv);
		npDialog.setTitle("初期コメ取得件数");
		npDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				getIntent.putExtra("init_comment_count",
						(short) np.getCurrent());
			}
		});
		npDialog.setNeutralButton("CANCEL",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {

					}
				});
		npDialog.create().show();
	}

	class AutoUpdateTask extends AsyncTask<Void, Void, Void> {
		@Override
		public void onCancelled() {
			super.onCancelled();
			AUTO_FLAG = false;
		}

		@Override
		protected Void doInBackground(Void... params) {
			if (setting_byte == null) {
				if (getIntent == null)
					getIntent = getIntent();
				setting_byte = getIntent.getByteArrayExtra("setting_byte");
			}
			if (setting_boolean == null) {
				if (getIntent == null)
					getIntent = getIntent();
				setting_boolean = getIntent
						.getBooleanArrayExtra("setting_boolean");
			}
			while (AUTO_FLAG) {
				try {
					Thread.sleep(setting_byte[32] * 60000);
				} catch (InterruptedException e) {
					e.printStackTrace();
					AUTO_FLAG = false;
					break;
				} catch (IllegalArgumentException e) {
					// e.printStackTrace();
					AUTO_FLAG = false;
					break;
				}
				updateCommentTable(true);// リスト更新するのでUIスレッドにないと駄目
			}
			return null;
		}

	}

	@Override
	public void updateCommentTable(boolean is_get_between) {
		if (setting_boolean == null) {
			if (getIntent == null)
				getIntent = getIntent();
			setting_boolean = getIntent.getBooleanArrayExtra("setting_boolean");
		}
		setting_boolean[13] = is_get_between;

		if (commentTable != null) {
			if (commentTable.isClosed()) {// すでにキャンセルされていたらnewする
				commentTable.createNewCommentTable(0, 0);
			}
			if (adapter != null) {
				if (adapter.getCount() > 0) {
					String[] last_row = null;
					if (isUplayout()) {
						last_row = adapter.getItem(0);
					} else {
						last_row = adapter.getItem(adapter.getCount() - 1);
					}
					if(last_row == null){//公式で何故かなった
						this.runOnUiThread(new Runnable(){
							@Override
							public void run() {
								MyToast.customToastShow(ACT, "コメントの取得に失敗しました");
							}
						});
						return;
					}
					if (is_get_between) {
						commentTable
								.updateCommentTable(last_row[1], last_row[6],
										last_row[5], NLiveRoid.apiLevel >= 8 ? 0 : 1);
					} else {
						commentTable.updateCommentTable(null, null,
								last_row[5], NLiveRoid.apiLevel >= 8 ? 0 : 1);
					}
				} else {
					commentTable.updateCommentTable(null, null, null,
							NLiveRoid.apiLevel >= 8 ? 0 : 1);
				}
			}
			// Log.d("Log","IS UPSLAYOUT ---- "+listview.getCount());
			if (isUplayout()) {// UIスレッドでやんなきゃ落ちる
				if (listview != null)
					new SetSelection().execute(true);
			} else {
				if (listview != null)
					new SetSelection().execute(false);
			}
		} else {// commentTableがnull(なんでなるのかわからない)
			Log.d("NLiveRoid", "CommentSock was null " + liveInfo);
			if (setting_boolean == null) {
				if (getIntent == null)
					getIntent = getIntent();
				setting_boolean = getIntent
						.getBooleanArrayExtra("setting_boolean");
			}
			if (setting_byte == null) {
				if (getIntent == null)
					getIntent = getIntent();
				setting_byte = getIntent.getByteArrayExtra("setting_byte");
				;
			}
			if(liveInfo.getPort() == null){
				error.setErrorCode(-13);
				return;
			}
			if (NLiveRoid.apiLevel >= 8) {
				commentTable = new CommentTable((byte) 0, this.liveInfo, ACT,
						adapter, column_seq, this.error,
						getIntent.getStringExtra("Cookie"), setting_byte[33],
						setting_boolean[12],
						setting_byte[33] == 3 ? setting_byte[28]
								: setting_byte[27], setting_byte[26],
						setting_byte[36],
						getIntent.getStringExtra("speech_skip_word"),
						setting_byte[29], getIntent.getShortExtra(
								"init_comment_count", (short) 20),
						setting_boolean[19]);
			} else {
				commentTable = new CommentTable((byte) 1, this.liveInfo, ACT,
						adapter, column_seq, this.error,
						getIntent.getStringExtra("Cookie"), setting_byte[33],
						setting_boolean[12],
						setting_byte[33] == 3 ? setting_byte[28]
								: setting_byte[27], setting_byte[26],
						setting_byte[36],
						getIntent.getStringExtra("speech_skip_word"),
						setting_byte[29], getIntent.getShortExtra(
								"init_comment_count", (short) 20),
						setting_boolean[19]);
			}
		}
	}

	// UIスレッドでやりたいだけ
	class SetSelection extends AsyncTask<Boolean, Void, Boolean> {
		@Override
		protected Boolean doInBackground(Boolean... arg0) {
			return arg0[0];
		}

		@Override
		protected void onPostExecute(Boolean arg) {
			// Log.d("log","UPDATED v!!!! --------------- ");
			if (arg) {
				listview.setSelection(0);
			} else {
				listview.setSelection(listview.getCount());
			}
		}
	}

	@Override
	public void layerChange(int which) {
		if(NLiveRoid.isDebugMode)Log.d("NLiveRoid"," layerChange" + which);
		if(commentTable != null && !commentTable.isClosed()){
			commentTable.closeMainConnection();
		}
		isLayerChanged = true;
		switch(which){
		case 0://前面へ
			setting_byte[31] = 0;
			if(listview != null){
				listview.setAdapter(null);
				listview.setVisibility(View.GONE);
			}
			if(headerview != null){
				headerview.setVisibility(View.GONE);
			}
			if(postArea != null)postArea.setVisibility(View.GONE);
			startOverLay();
			break;
		case 1://OverLayから背面時
			setting_byte[31] = 1;
			new SameLayerModeStart().execute();
			break;
		case 2://プレイヤーのみへ
			isLayerChanged = true;
			setting_byte[31] = 2;
			if(listview != null){
				listview.setVisibility(View.GONE);
			}if(headerview != null){
				headerview.setVisibility(View.GONE);
			}
			break;
		//コメントのみは削除
		}
		if(adapter != null)adapter.clear();//ここでデータが多いとANRになる危険有りなのでできれば余裕のある時に
	}

	@Override
	public void setUpdateInterval(byte interval) {
		if (setting_byte == null) {
			if (getIntent == null)
				getIntent = getIntent();
			setting_byte = getIntent.getByteArrayExtra("setting_byte");
		}
		setting_byte[32] = interval;
		// 更新間隔変更処理
	}

	@Override
	public void allUpdate() {
		System.gc();
		if(NLiveRoid.isDebugMode)Log.d("NLiveRoid","F alUpdate");
		if (error == null || liveInfo == null) {
			MyToast.customToastShow(ACT, "更新失敗しました\n");
			return;
		}
		// セッションを更新する
		NLiveRoid app = (NLiveRoid) getApplicationContext();
		app.setSessionid("");
		getIntent.putExtra("sp_session", "");
		if (listview != null) {
			listview.setVisibility(View.GONE);
		}
		if (headerview != null) {
			headerview.setVisibility(View.GONE);
		}
		new AllUpdateTask().execute();

	}

	class AllUpdateTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			if (getIntent == null)
				getIntent = getIntent();
			String new_session = Request.getSessionID(error);
			getIntent.putExtra("Cookie", new_session);
			if(setting_byte[43] == 2){
			getIntent.putExtra("sp_session", ((NLiveRoid)ACT.getApplicationContext()).getSp_session_key());
			}
			// タブ画面のセッションを更新
			Intent bcIntent = new Intent();
			bcIntent.setAction("return_f.NLR");
			bcIntent.putExtra("new_session", new_session);
			bcIntent.putExtra("r_code", CODE.RESULT_ALL_UPDATE);
			ACT.getBaseContext().sendBroadcast(bcIntent);
			new InitFlashPlayer().execute();
			return null;
		}

	}

	@Override
	public void setUpdateBetween(boolean flag) {
		if (setting_boolean == null) {
			if (getIntent == null)
				getIntent = getIntent();
			setting_boolean = getIntent.getBooleanArrayExtra("setting_boolean");
		}
		setting_boolean[13] = flag;
	}

	@Override
	public int getTableWidth(int columnNum) {
		if (setting_byte == null) {// 画面回転時nullになる場合がある
			if (getIntent == null)
				getIntent = getIntent();
			setting_byte = getIntent.getByteArrayExtra("setting_byte");
		}
		columnNum = column_seq[columnNum];
		if (!isPortLayt)
			columnNum += 11;// 0からpが並んでいて、横画面なら10上
		return setting_byte[columnNum];
	}

	@Override
	public boolean isPortLayt() {
		return isPortLayt;
	}

	@Override
	public int getViewWidth() {
		return pixcelViewW;
	}

	public int getViewHeight() {
		return pixcelViewH;
	}

	@Override
	public int getCellHeight() {
		return cellHeight;
	}

	@Override
	public void toScrollEnd() {
		if (listview == null)
			return;
		isScrollEnd = true;
		if (isUplayout()) {
			listview.setSelection(0);
		} else {
			listview.setSelection(listview.getCount());
		}
	}

	@Override
	public void setAtHandleName(String id, String nicName) {

		if (commentTable != null && !isAtoverwrite
				&& idToHandleName.containsKey(id)) {
			return;
		}
		if (nicName.equals("")) {
			return;
		} else {
			nicName = nicName.replace("<|>|/|\"", "");
		}
		idToHandleName.put(id, nicName);
		idToBgColor.put(id, -1);
		idToForeColor.put(id, -16777216);
		new WriteHandleName().execute();
		// セットアダプタを呼ぶと、自動的に最初の行まで戻されてしまう
		listview.setAdapter(adapter);
		if (isUplayout()) {
			int tempLastRow = 0;
			tempLastRow = listview.getFirstVisiblePosition();
			if (isScrollEnd) {
				listview.setSelection(0);
			} else {
				listview.setSelection(tempLastRow);
			}
		} else {
			int tempLastRow = 0;
			tempLastRow = listview.getLastVisiblePosition();
			if (isScrollEnd) {
				listview.setSelection(adapter.getCount());
			} else {
				listview.setSelection(tempLastRow);
			}
		}
	}

	@Override
	public void showSeetDialog() {
		if (getIntent == null)
			getIntent = getIntent();
		short initCount = getIntent.getShortExtra("init_comment_count",
				(short) 20);
		if (commentTable != null)
			new SeetDialog(this, commentTable, adapter, initCount,
					NLiveRoid.apiLevel >= 8 ? 0 : 1).showSelf();
	}

	@Override
	public void getCommentLog(boolean isPremium) {
		// 取得量はプラスで渡して判定に使う
		if (adapter != null && adapter.getCount() > 0) {
			if (isPremium) {
				if (isUplayout()) {
					int adapterCount = adapter.getCount() - 1;
					commentTable.getCommentLog(200,
							adapter.getItem(adapterCount)[3],
							adapter.getItem(adapterCount)[5]);
				} else {
					commentTable.getCommentLog(200, adapter.getItem(0)[3],
							adapter.getItem(0)[5]);
				}
			} else {
				MyToast.customToastShow(this, "プレアカじゃないのでソートのみを行います");
				commentTable.manualSort();
			}
		} else {
			MyToast.customToastShow(this, "コメントがありません");
		}
	}

	@Override
	public void saveComments() {
		new SaveDialog(this, liveInfo, adapter).showSelf();
	}

	@Override
	public View getBufferMark() {
		return bufferMark;

	}

	/**
	 * 発言リストを出力 下方向レイアウトのみ
	 *
	 */
	@Override
	public void createCommentedList(String userid) {
		new UserCommentedTask(userid, new ArrayList<String[]>()).execute();

	}

	class UserCommentedTask extends AsyncTask<Void, Void, Void> {
		private String userid;
		private ArrayList<String[]> commentedRows;

		UserCommentedTask(String userid, ArrayList<String[]> row) {
			this.userid = userid;
			this.commentedRows = row;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			dialog = ProgressDialog.show(ACT, "", "Loading user commented..",
					true, true);
			dialog.setContentView(R.layout.progress_dialog_user);
			dialog.show();
		}

		@Override
		protected Void doInBackground(Void... arg0) {

			tempIsScrollEnd = isScrollEnd;
			isScrollEnd = false;
			for (int i = 0; i < adapter.getCount(); i++) {
				if (adapter.getItem(i)[1].equals(userid)) {
					commentedRows.add(adapter.getItem(i));
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void arg) {
			try {// すでにActivityが終了してたらエラーする
				int defaultBgColor = idToBgColor.get(userid) == null ? Color.WHITE
						: idToBgColor.get(userid);
				int defaultFoColor = idToForeColor.get(userid) == null ? Color.BLACK
						: idToForeColor.get(userid);
				new UserCommentedDialog(ACT, userid, idToHandleName.get(userid) == null? userid:idToHandleName.get(userid),commentedRows, dialog,defaultBgColor,defaultFoColor)
						.showSelf();
			} catch (BadTokenException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		protected void onCancelled() {// 画面回転対策
			if (dialog != null && dialog.isShowing()) {
				dialog.dismiss();
				dialog = null;
			}
			super.onCancelled();
		}
	}


	@Override
	public boolean isAt() {
		return isAt;
	}

	@Override
	public boolean isAtOverwrite() {
		return isAtoverwrite;
	}

	@Override
	public void setAtEnable(boolean isAt) {
		this.isAt = isAt;
		if (setting_boolean == null) {
			if (getIntent == null)
				getIntent = getIntent();
			setting_boolean = getIntent.getBooleanArrayExtra("setting_boolean");
		}
		setting_boolean[6] = isAt;
	}

	@Override
	public void setAtOverwrite(boolean isAtoverwrite) {
		this.isAtoverwrite = isAtoverwrite;
		if (setting_boolean == null) {
			if (getIntent == null)
				getIntent = getIntent();
			setting_boolean = getIntent.getBooleanArrayExtra("setting_boolean");
		}
		setting_boolean[7] = isAtoverwrite;
	}

	@Override
	public void setHandleName(int bgColor, int foColor, String name) {
		idToHandleName.put(tempID, name);
		idToBgColor.put(tempID, bgColor);
		idToForeColor.put(tempID, foColor);

//		Log.d("SET HANDLE", "" + tempID + "    " + name + " " + bgColor + "  "
//				+ foColor);
		new WriteHandleName().execute();
		// セットアダプタを呼ぶと、自動的に最初の行まで戻されてしまう

		listview.setAdapter(adapter);
		if (isUplayout()) {
			int tempLastRow = 0;
			tempLastRow = listview.getFirstVisiblePosition();
			if (isScrollEnd) {
				listview.setSelection(0);
			} else {
				listview.setSelection(tempLastRow);
			}
		} else {
			int tempLastRow = 0;
			tempLastRow = listview.getLastVisiblePosition();
			if (isScrollEnd) {
				listview.setSelection(adapter.getCount());
			} else {
				listview.setSelection(tempLastRow);
			}
		}
	}

	@Override
	public void setAutoGetUserName(boolean isChecked) {
		if (setting_boolean == null) {// 画面回転時nullになる場合がある
			if (getIntent == null)
				getIntent = getIntent();
			setting_boolean = getIntent.getBooleanArrayExtra("setting_boolean");
		}
		setting_boolean[19] = isChecked;
		if (commentTable != null)
			commentTable.setAutoUser(isChecked);
	}

	/**
	 * ユーザー名自動からハンドルネーム設定
	 */
	@Override
	public void setAutoHandleName(String id, String result) {
		idToHandleName.put(id, result);
		idToBgColor.put(id, -1);
		idToForeColor.put(id, -16777216);
	}

	@Override
	public void setSpeachSettingValue(byte isEnable, byte speed, byte vol,
			byte pich) {
		if (setting_boolean == null) {
			if (getIntent == null)
				getIntent = getIntent();
			setting_boolean = getIntent.getBooleanArrayExtra("setting_boolean");
		}
		if (setting_byte == null) {
			if (getIntent == null)
				getIntent = getIntent();
			setting_byte = getIntent.getByteArrayExtra("setting_byte");
		}
		setting_byte[33] = isEnable;
		setting_byte[26] = speed;
		setting_byte[36] = vol;
		if (isEnable == 2 || isEnable == 3) {
			setting_byte[28] = pich;
		} else {
			setting_byte[27] = pich;
		}
	}

	@Override
	public CommentTable getCommentTable() {
		return commentTable;
	}

	@Override
	public void disConnectComment() {
		if (commentTable != null) {
			if (autoUpdateTask != null
					&& autoUpdateTask.getStatus() != AsyncTask.Status.FINISHED) {
				autoUpdateTask.cancel(true);
			}
			// closeMainConnectionは他でもいろいろ呼ばれるので分けておく
			commentTable.killSpeech();
			commentTable.closeLogConnection();
			commentTable.closeMainConnection();
		}
	}

	@Override
	public boolean isContainsUserID(String string) {
		return idToHandleName.containsKey(string);
	}

	/**
	 * isSetNameReadyを取得します。
	 *
	 * @return isSetNameReady
	 */
	@Override
	public boolean isSetNameReady() {
		return isSetNameReady;
	}

	@Override
	public float getHeightAdjust() {
		return heightAdjust;
	}

	@Override
	public long getOffTimerStart() {
		long val = getIntent.getLongExtra("offtimer_start", -1);
		Log.d("FlashPlayer","OFFTIMER---" + val);
		return val;
	}

	@Override
	public byte getOffTimerValue() {
		return setting_byte[37];
	}

	@Override
	public void shoeTweetDialog() {
		tweetDialog = new TweetDialog(this,liveInfo,getIntent.getStringExtra("twitterToken"),false);
		tweetDialog.showSelf();
	}

	@Override
	public boolean listVisibleGetSetter(boolean isSet,boolean isChecked) {
		if(!isSet)return listview==null? false:listview.getVisibility() == View.VISIBLE;
		if(setting_byte[31] == 0 || setting_byte[31] == 2)return false;//前面、プレイヤーのみ時は関係ない
		if(listview != null&&headerview != null){
		if(isChecked){
			listview.setVisibility(View.VISIBLE);
			headerview.setVisibility(View.VISIBLE);
		}else{
			listview.setVisibility(View.INVISIBLE);
			headerview.setVisibility(View.INVISIBLE);
		}
		}
		return false;
	}


	@Override
	public boolean listPositionGetSetter(boolean isSet, boolean position) {
		if(!isSet){
			return isPortLayt;
		}
		if(isPortLayt){
			if(isUplayout()){//方向上
				if(position){//上
					setting_byte[8] = 0;//X
					setting_byte[9] = 43;//Y
					setting_byte[10] = -43;//Height
				}else{//下
					setting_byte[8] = 0;//X
					setting_byte[9] = 94;//Y
					setting_byte[10] = -45;//Height
				}
			}else{//方向下
				if(position){//上
					setting_byte[8] = 0;//X
					setting_byte[9] = 0;//Y
					setting_byte[10] = 43;//Height
				}else{//下
					setting_byte[8] = 0;//X
					setting_byte[9] = 50;//Y
					setting_byte[10] = 45;//Height
				}
			}
		}else{
			if(isUplayout()){//方向上
				if(position){//左
					setting_byte[19] = 0;//X
					setting_byte[20] = 88;//Y
					setting_byte[21] = -90;//Height
				}else{//右
					setting_byte[19] = 60;//X
					setting_byte[20] = 88;//Y
					setting_byte[21] = -90;//Height
				}
			}else{//方向下
				if(position){//左
					setting_byte[19] = 0;//X
					setting_byte[20] = 0;//Y
					setting_byte[21] = 92;//Height
				}else{//右
					setting_byte[19] = 60;//X
					setting_byte[20] = 0;//Y
					setting_byte[21] = 92;//Height
				}
			}
		}
		onConfigurationChanged(this.getResources().getConfiguration());
		return false;
	}

	@Override
	public void showPositionDialog() {//コメント欄の位置設定
		if(NLiveRoid.isDebugMode)Log.d("NLiveRoid","showPosition ");
		if(listDialog != null && listDialog.isShowing())listDialog.cancel();
		AlertDialog.Builder d = new AlertDialog.Builder(this).setItems(this.listPositionGetSetter(false,false)? new CharSequence[]{"上","下"}:new CharSequence[]{"左","右"},
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if(NLiveRoid.isDebugMode)Log.d("NLiveRoid","listPositionGetSetter " + which);
						listPositionGetSetter(true, which==0? true:false);
					}
				});
		d.create();
		listDialog = d.show();
	}

	@Override
	public void quickAction(int action) {
		switch(action){
		case 0:
			setting_byte[40] = (byte)(setting_byte[40] & 0x0F);
			break;
		case 1:
			setting_byte[40] = (byte) ((setting_byte[40] & 0x0F) | 0x10); //01XX XXXX
			break;
		case 2:
			setting_byte[40] = (byte) ((setting_byte[40] & 0x0F) | 0x20); //10XX XXXX
			break;
		case 3://全画面
			isFullScreend = true;//バックキーの為フラグをONにしておく
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			getWindow().addFlags(
					WindowManager.LayoutParams.FLAG_FULLSCREEN);
			tempFullScrn();
			break;
		case 4://ログ取得
			if(liveInfo.getIsPremium().equals("0")){
				getCommentLog(false);
				}else{
				getCommentLog(true);
				}
			break;
		case 5://設定
			if (liveInfo == null || liveInfo.getLiveID() == null) {
				MyToast.customToastShow(this, "エラーが発生しています");
			}else if (setting_byte[31] == 0 || setting_byte[31] == 2||noCommentServer) {// 前面、プレイヤーのみの場合、ここではコメサバ接続を持たないのでinit_modeが必ず2
				new ConfigDialog(this, liveInfo, setting_byte,
						setting_boolean, (byte) 2).showSelf();
			}else {
				new ConfigDialog(this, liveInfo, setting_byte,
						setting_boolean, setting_byte[31]).showSelf();
			}
				break;
		case 6://視聴画面終了
			standardFinish();
			break;
		}
	}
	private void tempFullScrn() {
		if(setting_byte[43] == 0){//spplayer
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			if(wv != null){
				((LinearLayout) wv.getParent()).setPadding(0, 0, 0, 0);
			wv.loadUrl("javascript:document.getElementById('flvplayer').width="
					+ webViewH);
			wv.loadUrl("javascript:document.getElementById('flvplayer').height="
					+ webViewW);
			wv.setLayoutParams(new LinearLayout.LayoutParams(pixcelViewH,pixcelViewW));
			}
		}else{
			MyToast.customToastShow(this, "原宿版ではプレイヤータップ操作のみ利用できます");
		}
	}

	@Override
	public void settingChange(int casevalue, byte npValue,byte[] seq) {
		Log.d("NLiveRoid","settingChange ---- " + casevalue);
		switch(casevalue){
		case 0://縦X位置
			setting_byte[8] = npValue;
			break;
		case 1://横X位置
			setting_byte[19] = npValue;
			break;
		case 2://縦Y位置
			setting_byte[9] = npValue;
			break;
		case 3://横Y位置
			setting_byte[20] = npValue;
			break;
		case 4://縦X幅
			setting_byte[38] = npValue;
			break;
		case 5://横X幅
			setting_byte[39] = npValue;
			break;
		case 6://縦Y幅
			setting_byte[10] = npValue;
			break;
		case 7://横Y幅
			setting_byte[21] = npValue;
			break;
		case 8://Xdragg Ydragg
			//XXXX この4ビット目が0=portlayt 1=landscape 下位2ビットで xy
			if((npValue & 0x04) == 0){//縦
				if((npValue & 0x02) > 0){//XDragg有効
					setting_boolean[8] = true;
				}else{
					setting_boolean[8] = false;
				}
				if((npValue & 0x01) > 0){//YDragg有効
					setting_boolean[9] = true;
				}else{
					setting_boolean[9] = false;
				}
				enable_moveX = setting_boolean[8];
				enable_moveY = setting_boolean[9];
			}else if((npValue & 0x04) > 0){//横
				if((npValue & 0x02) > 0){//XDragg有効
					setting_boolean[10] = true;
				}else{
					setting_boolean[10] = false;
				}
				if((npValue & 0x01) > 0){//YDragg有効
					setting_boolean[11] = true;
				}else{
					setting_boolean[11] = false;
				}
				enable_moveX = setting_boolean[10];
				enable_moveY = setting_boolean[11];
			}
			break;
		case 9:
			Log.d("NLiveRoid",  "changeColumnSeq");
			column_seq = seq;
			if(listview != null && adapter != null){
				listview.setVisibility(View.INVISIBLE);
				byte idIndex = 0;
				for(byte i = 0; i < listSize; i++){
					if(column_seq[i] == 1)idIndex = i;
				}
				for(int i = 0; i < listview.getCount();i++){
					if(listview.getChildAt(i) != null){
						((TableLayout)listview.getChildAt(i)).removeAllViews();
					for (int j = 0; j < listSize; j++) {
						//列のビューの初期化をやり直す
						((ViewHolder)listview.getChildAt(i).getTag()).id_index = idIndex;
						}
					}
				}
				listview.setVisibility(View.VISIBLE);
			}
			break;
		case 10:
			break;
		case 11://プレイヤーの位置変更
			break;
		}
		//コメント欄をやり直す
		onConfigurationChanged(this.getResources().getConfiguration());
	}

	@Override
	public byte[] getColumnSeq() {
		return column_seq;
	}

	@Override
	public boolean isNsen() {
		return getIntent.getBooleanExtra("isnsen", false);
	}


}
