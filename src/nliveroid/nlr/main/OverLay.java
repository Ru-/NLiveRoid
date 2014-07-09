package nliveroid.nlr.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;

import nliveroid.nlr.main.parser.XMLparser;

import org.apache.http.ParseException;
import org.xmlpull.v1.XmlPullParserException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.speech.RecognizerIntent;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.ClipboardManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.BadTokenException;
import android.view.WindowManager.LayoutParams;
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
import android.widget.TableRow;
import android.widget.TextView;

public class OverLay extends Activity implements CommentPostable,
		 HandleNamable,Archiver {
	private LayoutInflater inflater;
	private View parent;
	private Intent getIntent;
	private static OverLay ACT;
	static CommentTable commentTable;
	private LiveInfo liveInfo;
	private byte[] setting_byte;
	private boolean[] setting_boolean;

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
	private boolean isUplayout;

	private int viewH = 0;
	private int viewW = 0;
	private float density = 0;
	private int cellHeight = 0;

	// コテハンの配列
	private String handleFileName = "handlenames.xml";
	private Map<String, String> idToHandleName;
	private Map<String, Integer> idToBgColor;
	private Map<String, Integer> idToForeColor;

	private CommandMapping cmd;

	private String tempID;
	// 設定フラグ
	private boolean enable_moveX;
	private boolean enable_moveY;

	private LinearLayout postArea;
	private EditText postET;
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

	private boolean isScrollEnd = true;
	private boolean tempIsScrollEnd = true;

	private boolean drawerKey;// 何故かKEYEVENTが2回呼ばれる

	private boolean isPortLayt;

	private boolean isNotification;

	private byte listSize = 7;
	private ErrorCode error;
	private Gate gate;
	private ProgressDialog dialog;

	private boolean isAt;
	private boolean isAtoverwrite;
	private int row_resource_id = R.layout.comment_row;
	private byte[] column_seq;
	private int[] column_ids;

	private AlertDialog listDialog;
	private QuickDialog quickDialog;
	private boolean isFullScreend;
	private boolean isSetNameReady;//コテハン読み込み完了フラグ
	private float heightAdjust=2;
	private TweetDialog tweetDialog;
	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		ACT = this;
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		inflater = LayoutInflater.from(this);
		parent = inflater.inflate(R.layout.overlay, null);
		setContentView(parent);
		getIntent = getIntent();
		isNotification = getIntent.getBooleanExtra("notification", false);
		new InitalyzeOverLay().execute();
	}

	@Override
	public void onWindowFocusChanged(boolean hasfocus) {
		getWindow().setSoftInputMode(
				LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		if(postET != null && postET.getWidth() > 0)postET.setWidth(postET.getWidth());//これしないと豪いことになる
		if(postB != null && postB.getWidth() > 0)postB.setWidth(postB.getWidth());//これしないと豪いことになる
		// ノティフィケーションの場合、フラッシュとは別プロセスなので、
		// ここで決めないといけない
		if (isNotification) {
			NLiveRoid app = (NLiveRoid) getApplicationContext();
			app.setViewWidthDp(getWindow().getDecorView().getWidth());
			app.setViewHeightDp(getWindow().getDecorView().getHeight());
			DisplayMetrics metrics = new DisplayMetrics();
			Display disp = getWindowManager().getDefaultDisplay();
			disp.getMetrics(metrics);
			float scaleDensity = metrics.scaledDensity;
			int width = getWindow().getDecorView().getWidth();
			int height = getWindow().getDecorView().getHeight();
			if (width > height) {
				int temp = width;
				width = height;
				height = temp;
			}
			app.setMetrics(scaleDensity);
			width = (int) (width / scaleDensity);
			height = (int) (height / scaleDensity);
			app.setResizeW((int) (width * 1.72D));
			app.setResizeH((int) (height * 1.8D));
			app.setViewWidthDp((int) width);
			app.setViewHeightDp((int) height);
			app.createGateInstance();
			// メニューキー等押下時に毎回呼ばれてしまうのでfalseにしておく
			isNotification = false;
		}
	}

	class InitalyzeOverLay extends AsyncTask<Void, Void, Integer> {
		@Override
		protected Integer doInBackground(Void... arg0) {
			if(NLiveRoid.isDebugMode)Log.d("NLiveRoid"," InitOver");
			NLiveRoid app = (NLiveRoid) getApplicationContext();
			error = app.getError();
			if (error == null) {// Notificationから来るとnull
				((NLiveRoid) getApplicationContext()).initStandard();
				if (error == null || error.getErrorCode() != 0) {// 設定ファイル読み込み失敗する(不明)初期化処理はしないとその後大変なので無視
					Log.d("NLiveRoid", "FAILED READ SETTING FILE IN OVERLAY ");
				}
			}
			idToHandleName = new ConcurrentHashMap<String, String>();
			idToBgColor = new ConcurrentHashMap<String, Integer>();
			idToForeColor = new ConcurrentHashMap<String,Integer>();
			// stillSpeechStartedを裏でやりたい+一応読み出しておく
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
			//コメ欄の高さを調整する
			switch(setting_byte[35]){
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
			// セッションを合わせる
			String sessionid = getIntent.getStringExtra("Cookie");
			if(getIntent.getStringExtra("sp_sesison") != null){
				sessionid = getIntent.getStringExtra("sp_session");
			}
			if (sessionid == null || sessionid.equals("") ) {
				return -1;
			}
			liveInfo = (LiveInfo) getIntent.getSerializableExtra("LiveInfo");
			if (liveInfo == null) {
				return -2;
			}
			cmd = (CommandMapping) getIntent.getSerializableExtra("cmd");
			if (cmd == null) {
				cmd = new CommandMapping(false);// ここに書けばliveinfoはnullじゃない+既にポートを取得していてもOK
			}
			column_seq = getIntent.getByteArrayExtra("column_seq");


			if (liveInfo.getPort() != null) {// アラート新着からの場合すでにgetPlayerの情報がある

				density = getIntent.getFloatExtra("density", 1.5f);
				// 改行で再計算するならここ
				if (setting_boolean[1]) {
					row_resource_id = R.layout.newline_row;
					column_ids = new int[] {R.id.nseq0, R.id.nseq1, R.id.nseq2,
							R.id.nseq3, R.id.nseq4, R.id.nseq5,R.id.nseq6 };
					// column_width = new byte[]{setting_byte[]};
				} else {
					column_ids = new int[] { R.id.seq0, R.id.seq1, R.id.seq2,
							R.id.seq3, R.id.seq4, R.id.seq5, R.id.seq6 };
					// column_width = new byte[]{setting_byte[]};
				}
				isAt = setting_boolean[6];
				isAtoverwrite = setting_boolean[7];
				density = getIntent.getFloatExtra("density", 1.5f);
				initTable();
				return 0;
			} else {// 放送情報のポート番号がない場合(ほとんど毎回)、getPlayerに取りに行き、ステータス分岐

				byte[] liveStatus = null;
				try {
					if(NLiveRoid.isDebugMode)Log.d("NLiveRoid","OgetPlayer ");
					liveStatus = Request.getPlayerStatusToByteArray(
							liveInfo.getLiveID(), error, sessionid);
				} catch (Exception e) {
					return -3;
				}
				if (liveStatus == null) {
					return -3;
				}
				String check = null;
				try {
					check = new String(liveStatus, "UTF-8");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}

				if (check == null)
					return -30;
				if(check.contains("status=\"fail\"")||check.contains("status=\"error\"")){
				if (check.contains("notlogin")) {// 他にもinvalid_lv(数値じゃない場合など)notfound(不明)
					return -4;
				} else if (check.contains("closed")) {
					return -5;
				} else if (check.contains("comingsoon")) {
					return -6;
				} else if (check.contains("require_community_member")) {
					return -7;
				} else if (check.contains("incorrect_account_data")) {// アカウント無しでも見れる放送(コメサバへ繋げない)
					return -8;
				}  else if (check.contains("timeshift_ticket_exhaust")) {//TS視聴にチケットが必要なもの
					return -9;
				}  else if (check.contains("usertimeshift")) {//コミュ限でTS
					return -7;
				} else if (check.contains("noauth")) {//チャンネルで終了していてTS提供されてない放送でなる
					return -5;
				}else if(check.contains("require_accept_print_timeshift_ticket")){

				}else if(check.contains("full")){///満席
					if(NLiveRoid.isDebugMode)Log.d("NLiveRoid","Over full " + check);
					return -11;//-10ほかで使ってる
				}
				}else if (error != null && error.getErrorCode() == 0) {
					if (check.length() < 100) {
						if (check.equals("SocketException")) {// ネットワークエラー
							return -10;
						}else{
							Log.d("NLiveRoid", "O failed_comment" + check);// 100文字以下なら失敗の可能性が高い
						}
					} else {
						try {
							Log.d("NLiveRoid","OCHECK");
							XMLparser.getLiveInfoFromAPIByteArray(liveStatus,
									liveInfo);

							if(getIntent.getStringExtra("isPreLooked") != null && liveInfo != null && liveInfo.getIsPremium() != null && liveInfo.getIsPremium().equals("0")){//ブロキャス
								Intent isPre = new Intent();
								isPre.setAction("bindTop.NLR");
								isPre.putExtra("isPre", true);
								ACT.sendBroadcast(isPre);
							}
						} catch (NullPointerException e1) {// 頻繁に起きるが。。
							e1.printStackTrace();
							error.setErrorCode(-37);
							return -100;//画面は落とさない
						} catch (ParseException e1) {
							e1.printStackTrace();
							error.setErrorCode(-37);
							return -100;//画面は落とさない
						}  catch (XmlPullParserException e1) {
							e1.printStackTrace();
							Log.d("NLiveRoid O", " " + check);
							if(check.contains("<getplayerstatus")){//最初に余計な文章が入っている場合があるかも(よよ)
									check = check.substring(check.indexOf("<getplayerstatus"));
								try {
									XMLparser.getLiveInfoFromAPIByteArray(liveStatus,
											liveInfo);
								} catch (Exception e) {
									e.printStackTrace();
									Intent eIntent = new Intent();
									eIntent.putExtra("overlay_error", -49);
									setResult(CODE.RESULT_OVERLAY_ERROR, eIntent);
									ACT.finish();
									return -100;
								}
							}else{
								Intent eIntent = new Intent();
								eIntent.putExtra("overlay_error", -30);
								setResult(CODE.RESULT_OVERLAY_ERROR, eIntent);
								ACT.finish();
								return -100;
							}
						} catch (IOException e1) {
							e1.printStackTrace();
							error.setErrorCode(-37);
							return -100;
						}
						// 成功してたらコメ欄初期化----------------
						// ポストエリア等より先に判定が必要

						density = getIntent.getFloatExtra("density", 1.5f);
						// 改行で再計算するならここ
						if (setting_boolean[1]) {
							row_resource_id = R.layout.newline_row;
							column_ids = new int[] { R.id.nseq0, R.id.nseq1, R.id.nseq2,
									R.id.nseq3, R.id.nseq4, R.id.nseq5,R.id.nseq6 };
							// column_width = new byte[]{setting_byte[]};
						} else {
							column_ids = new int[] { R.id.seq0, R.id.seq1, R.id.seq2,
									R.id.seq3, R.id.seq4, R.id.seq5,R.id.seq6 };
							// column_width = new byte[]{setting_byte[]};
						}
						isAt = setting_boolean[6];
						isAtoverwrite = setting_boolean[7];
						Log.d("NLiveRoid","BEFORE INIT --------- ");
						initTable();
						return 0;
					}
				}
			}
			if(NLiveRoid.isDebugMode)Log.d("NLiveRoid","O END InitOver ");
			return -1;
		}

		private void initTable() {//doInBackGroundでリストの設定値をセットするinit()よりも先に呼ばれる

			viewW = (int) (getIntent.getIntExtra("viewW", ACT
					.getWindowManager().getDefaultDisplay().getWidth()) * density);// キャストで端が多少少なくなる?
			viewH = (int) (getIntent.getIntExtra("viewH", ACT
					.getWindowManager().getDefaultDisplay().getHeight()) * density);
			if (viewW > viewH) {
				int temp = viewW;
				viewW = viewH;
				viewH = temp;
			}

			// テーブル設定の読み込み 上表示、X移動可、Y移動可

			// テーブルの位置決定
			Configuration config = getResources().getConfiguration();
			if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
				isPortLayt = false;
				firstcurrentX = (int) ((double) viewH * (setting_byte[19] * 0.01D));
				firstcurrentY = (int) ((double) viewW * (setting_byte[20] * 0.01D));
				list_bottom = (int) ((double) viewW * (setting_byte[21] * 0.01D));
				list_width = (int) ((double) viewH * (setting_byte[39] * 0.01D));
				cellHeight = (int) (viewH * setting_byte[18] * 0.01D);
				enable_moveX = setting_boolean[10];
				enable_moveY = setting_boolean[11];
			} else {
				isPortLayt = true;
				firstcurrentX = (int) ((double) viewW * (setting_byte[8] * 0.01D));
				firstcurrentY = (int) ((double) viewH * (setting_byte[9] * 0.01D));
				list_bottom = (int) ((double) viewH * (setting_byte[10] * 0.01D));
				list_width = (int) ((double) viewW * (setting_byte[38] * 0.01D));
				cellHeight = (int) (viewH * setting_byte[7] * 0.01D);
				enable_moveX = setting_boolean[8];
				enable_moveY = setting_boolean[9];
			}
		}

		@Override
		protected void onPostExecute(Integer arg) {
			setRingingFinish();
			if (arg == 0) {// 設定ファイル読み込み失敗する(不明)
				new ReadHandleName().execute();// 失敗時トースト表示するのでここで読み込み
				// アダプタの初期化
				adapter = new ExCommentListAdapter(ACT);

				Window window = getWindow();
				window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);// 強制KEEP_SCREEN_ON
				window.setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
				if (setting_boolean[21]) {
					window.addFlags(
							WindowManager.LayoutParams.FLAG_FULLSCREEN);
				}
				setOrientation(setting_byte[24]);
				init(liveInfo, getIntent);
			} else if (arg == -1) {
				MyToast.customToastShow(ACT, "Error:コメント情報の取得に失敗しました");
				ACT.finish(CODE.RESULT_OVERLAY_ERROR);
			} else if (arg == -2) {
				Intent eIntent = new Intent();
				eIntent.putExtra("overlay_error", -8);
				setResult(CODE.RESULT_OVERLAY_ERROR, eIntent);
				ACT.finish();
			} else if (arg == -3) {
				Intent eIntent = new Intent();
				eIntent.putExtra("overlay_error", -8);
				setResult(CODE.RESULT_OVERLAY_ERROR, eIntent);
				ACT.finish();
			} else if (arg == -4) {
				Intent eIntent = new Intent();
				eIntent.putExtra("overlay_error", -17);
				setResult(CODE.RESULT_OVERLAY_ERROR, eIntent);
				ACT.finish();
			} else if (arg == -5) {
				Intent eIntent = new Intent();
				eIntent.putExtra("overlay_error", -18);
				setResult(CODE.RESULT_CLOSED, eIntent);// この場合、エラーというか終わってるので終了する
				ACT.finish();
			} else if (arg == -6) {
				Intent eIntent = new Intent();
				eIntent.putExtra("overlay_error", -19);
				setResult(CODE.RESULT_OVERLAY_ERROR, eIntent);
				ACT.finish();
			} else if (arg == -7) {
				Intent eIntent = new Intent();
				eIntent.putExtra("overlay_error", -20);
				setResult(CODE.RESULT_OVERLAY_ERROR, eIntent);
				ACT.finish();
			} else if (arg == -8) {
				Intent eIntent = new Intent();
				eIntent.putExtra("overlay_error", -30);
				setResult(CODE.RESULT_OVERLAY_ERROR, eIntent);
				ACT.finish();
			} else if (arg == -9) {
				Intent eIntent = new Intent();
				eIntent.putExtra("overlay_error", -50);
				setResult(CODE.RESULT_OVERLAY_ERROR, eIntent);
				ACT.finish();
			} else if (arg == -10) {
				Intent eIntent = new Intent();
				eIntent.putExtra("overlay_error", -41);
				setResult(CODE.RESULT_OVERLAY_ERROR, eIntent);
				ACT.finish();
			} else if (arg == -11) {
				Intent eIntent = new Intent();
				eIntent.putExtra("overlay_error", -54);
				setResult(CODE.RESULT_OVERLAY_ERROR, eIntent);
				ACT.finish();
			}else if(arg == -100){
				error.showErrorToast();
			}
		}
		private void setRingingFinish(){
			//着信時に視聴画面を殺す
			TelephonyManager mTelephonyManager
	        = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
			PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
			    @Override
			    public void onCallStateChanged(int state, String number) {
			    	if(state == TelephonyManager.CALL_STATE_RINGING){
			            // 着信 又は通話中に視聴画面をを落とす
			             Log.d("NLiveRoid","Detect ringing  and call finish player activity");
			    		ACT.finish(CODE.RESULT_QUICKFINISH_OR_RINGING);
			    	}
			       }
			   };
			   mTelephonyManager.listen
		        (mPhoneStateListener,PhoneStateListener.LISTEN_CALL_STATE);
		}
	}

	public static OverLay getOvarLay() {
		return ACT;
	}

	//onPostExecuteから呼ばれる UIスレッドばかりなのでバックグラウンドにできない
	private void init(LiveInfo liveinfo, Intent receiveIntent) {

		byte[] bitmaparray = liveinfo.getBitmapArray();
		if (bitmaparray != null) {
			Bitmap bitmap = BitmapFactory.decodeByteArray(bitmaparray, 0,
					bitmaparray.length);
			liveinfo.setThumbnail(bitmap);
		}

		String session = getIntent().getStringExtra("Cookie");
		if (liveinfo != null && session != null) {// コメント取得を開始する
			new StartCommentTable(liveinfo, session, true).execute();
		}
		// 重なりを考慮してポストエリアとバッファとリストのレイアウト
		if (setting_boolean == null) {
			if (getIntent == null)
				getIntent = getIntent();
			setting_boolean = getIntent.getBooleanArrayExtra("setting_boolean");
		}
		if (setting_boolean[2]) {
			postArea = (LinearLayout) parent.findViewById(R.id.postArea_up);
			postET = (EditText) parent.findViewById(R.id.postarea_edit_up);
			postET.setFocusable(true);
			postET.clearFocus();
			// 音声入力、利用しない設定でもインフレートしないと、入りの縦横と画面回転のところで、投稿フォームがおかしくなる恐れ
			if(setting_boolean[3]){//イベントリスナでもここを聞いてるけどifの記述上しかたない
				post_184 = (CheckBox)parent.findViewById(R.id.postarea_184_up);
			post_command = (Button)parent.findViewById(R.id.postarea_command_up);
			post_update = (Button)parent.findViewById(R.id.postarea_update_up);
			post_menu = (Button)parent.findViewById(R.id.postarea_menukey_up);
			voiceInput = (Button) parent.findViewById(R.id.postarea_voiceinput_up);
			post_cdisp = (CheckBox) parent.findViewById(R.id.postarea_commentdisp_up);
			post_desc = (Button)parent.findViewById(R.id.postarea_desc_up);
			((ViewGroup)post_184.getParent()).setMinimumHeight((int)(30/((NLiveRoid)getApplicationContext()).getMetrics()));
			setFormListeners();
			}
			postB = (Button) parent.findViewById(R.id.postarea_commit_up);
			postB.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					Log.d("NLiveRoid","postComment O");
					final String comment = postET.getText().toString();
					if(comment == null || comment.equals(""))return;
					if (cmd != null) {
						commentTable.postComment(comment,
								cmd);
					} else {
						MyToast.customToastShow(ACT, "コメントの投稿に失敗しました");
					}
					postET.setText("");
					Log.d("NLiveRoid","OpostComment END ");

				}
			});
		} else {
			postArea = (LinearLayout) parent.findViewById(R.id.postArea_buttom);
			postET = (EditText) parent.findViewById(R.id.postarea_edit_down);
			postET.setFocusable(true);
			postET.clearFocus();
			if (setting_boolean == null) {
				if (getIntent == null)
					getIntent = getIntent();
				setting_boolean = getIntent
						.getBooleanArrayExtra("setting_boolean");
			}
			if(setting_boolean[3]){//イベントリスナでもここを聞いてるけどifの記述上しかたない
				post_184 = (CheckBox)parent.findViewById(R.id.postarea_184_down);
				post_command = (Button)parent.findViewById(R.id.postarea_command_down);
				post_update = (Button)parent.findViewById(R.id.postarea_update_down);
				post_desc = (Button)parent.findViewById(R.id.postarea_desc_down);
				post_cdisp = (CheckBox) parent.findViewById(R.id.postarea_commentdisp_down);
				voiceInput = (Button) parent
						.findViewById(R.id.postarea_voiceinput_down);
				post_menu = (Button)parent.findViewById(R.id.postarea_menukey_down);
				setFormListeners();
			}
			postB = (Button) parent.findViewById(R.id.postarea_commit_down);
			postB.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					if (cmd != null) {
						final String comment = postET.getText().toString();
						if(comment == null || comment.equals(""))return;
						commentTable.postComment(postET.getText().toString(),
								cmd);
					} else {
						MyToast.customToastShow(ACT, "コメントの投稿に失敗しました");
					}
					postET.setText("");
				}
			});
		}
		postArea.setVisibility(View.GONE);// デフォルト非表示
		// バッファアイコン初期化
		bufferMark = (LinearLayout) parent.findViewById(R.id.buffering_area);
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
			Intent eIntent = new Intent();
			eIntent.putExtra("overlay_error", -13);//コメサバに接続できてない(非対応CH・公式)時になる
			setResult(CODE.RESULT_OVERLAY_ERROR, eIntent);
			ACT.finish();
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
					new OperationDialog(ACT, setting_boolean[13], setting_byte[31])
							.showSelf();
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
					Intent intent = new Intent();
					intent.setAction("player_config.NLR");
					intent.putExtra("temp_fullscrn", setting_byte[24]);
					sendBroadcast(intent);//戻すインテントを投げる
					if(!setting_boolean[21]){//ステータスバーも戻しておく
						getWindow().clearFlags(
								WindowManager.LayoutParams.FLAG_FULLSCREEN);
					}
					setOrientation(setting_byte[24]);//設定に戻す
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

	@Override
	public void onResume() {
		// if(adapter != null){
		// adapter.clear();
		// String session = getIntent().getStringExtra("Cookie");
		// if(liveinfo != null && session != null){
		// new CommentThread(liveinfo, session ,true).execute();
		// }
		// }
		super.onResume();
	}

	/**
	 * 引数のコードをそのままResultCodeにして終了する
	 */
	public void finish(int code) {
		if (commentTable != null) {
			// closeMainConnectionは他でもいろいろ呼ばれるので分けておく
			if (autoUpdateTask != null
					&& autoUpdateTask.getStatus() != AsyncTask.Status.FINISHED) {
				autoUpdateTask.cancel(true);
			}
			commentTable.killSpeech();
			commentTable.closeLogConnection();
			commentTable.closeMainConnection();
		}
		// テーブルの値を保存
		Intent data = new Intent();
		if (setting_byte == null) {
			if (getIntent == null)
				getIntent = getIntent();
			setting_byte = getIntent.getByteArrayExtra("setting_byte");
		}
		if (setting_boolean == null) {
			if (getIntent == null)
				getIntent = getIntent();
			setting_boolean = getIntent.getBooleanArrayExtra("setting_boolean");
		}
		data.putExtra("nsen", liveInfo.getLiveID());
		data.putExtra("setting_byte", setting_byte);
		data.putExtra("setting_boolean", setting_boolean);
		data.putExtra("cmd", cmd);
		data.putExtra("column_seq", column_seq);
		this.setResult(code, data);
		super.finish();
	}

	@Override
	public void onPause() {
		super.onPause();
		if (recognizeValue > 0) {
			onStart();
		} else if (commentTable != null) {
			if (autoUpdateTask != null
					&& autoUpdateTask.getStatus() != AsyncTask.Status.FINISHED) {
				autoUpdateTask.cancel(true);
			}
			commentTable.killSpeech();
			commentTable.closeLogConnection();
			commentTable.closeMainConnection();
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (commentTable != null) {
			if (autoUpdateTask != null
					&& autoUpdateTask.getStatus() != AsyncTask.Status.FINISHED) {
				autoUpdateTask.cancel(true);
			}
			commentTable.killSpeech();
			commentTable.closeLogConnection();
			commentTable.closeMainConnection();
		}
	}

	public void setOrientation(int flug) {
		switch (flug) {
		case 0:
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);// 基本回転する
			// setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);//端末の設定
			if (setting_byte == null) {
				if (getIntent == null)
					getIntent = getIntent();
				setting_byte = getIntent.getByteArrayExtra("setting_byte");
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
			}
			setting_byte[24] = 2;
			getIntent.putExtra("setting_byte", setting_byte);
			break;
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		if(listDialog != null && listDialog.isShowing())listDialog.cancel();
		// プレビュー中は画面回転を無視する
		// 画面回転時nullになる場合がある
		if (setting_byte == null) {
			if (getIntent == null)
				getIntent = getIntent();
			setting_byte = getIntent.getByteArrayExtra("setting_byte");
			if (setting_byte == null) {// クラッシュ確定
				finish(CODE.RESULT_OVERLAY_ERROR);
			}
		}
		if (gate != null && gate.isOpened()) {
			gate.onConfigChanged(newConfig);
		}
		if(tweetDialog != null && tweetDialog.isShowing())tweetDialog.onConfigChanged(ACT);

		if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			isPortLayt = false;
			firstcurrentX = (int) ((double) (viewH * setting_byte[19] * 0.01D));
			firstcurrentY = (int) ((double) (viewW * setting_byte[20] * 0.01D));
			list_bottom = (int) ((double) (viewW * setting_byte[21] * 0.01D));
			list_width = (int) ((double) (viewH * setting_byte[39] * 0.01D));
			cellHeight = (int) (viewH * (setting_byte[18] * 0.01D));
		} else {
			isPortLayt = true;
			firstcurrentX = (int) ((double) (viewW * setting_byte[8] * 0.01D));
			firstcurrentY = (int) ((double) (viewH * setting_byte[9] * 0.01D));
			list_bottom = (int) ((double) (viewH * setting_byte[10] * 0.01D));
			list_width = (int) ((double) (viewW * setting_byte[38] * 0.01D));
			cellHeight = (int) (viewH * (setting_byte[7] * 0.01D));
		}
		// 縦横で方向が変わる場合がある
		if (list_bottom < 0) {
			isUplayout = true;
		} else {
			isUplayout = false;
		}
//		preparePositionValue();
		if (listview == null) {
			listview = getList();
		}
		listview.setAdapter(adapter);

		// 親(Blank系)のパディングをやり直してアドし直す
		if (isUplayout()) {
			if (listBlank == null) {
				listBlank = getListBlank_Up();
			}
			listBlank.setPadding(firstcurrentX, firstcurrentY + list_bottom,
					-firstcurrentX, 0);
			listBlank.removeView(listview);
			listview.setPadding(0, 0, 0, 0);
			listBlank.addView(listview, new LinearLayout.LayoutParams(list_width,
					-list_bottom));
			listview.setSelection(0);
		} else {
			if (listBlank == null) {
				listBlank = getListBlank_Down();
			}
			listBlank.setPadding(firstcurrentX, firstcurrentY, -firstcurrentX,
					0);
			listview.setPadding(0, cellHeight, 0, 0);
			listBlank.removeView(listview);
			listBlank.addView(listview, new LinearLayout.LayoutParams(list_width,
					list_bottom));
			listview.setSelection(listview.getCount());
		}
		if (headerBlank != null && headerview != null) {
			headerBlank.setPadding(firstcurrentX, firstcurrentY,
					-firstcurrentX, 0);
			headerBlank.removeView(headerview);
			headerview = getHeader();
			if(headerview == null){
				Intent eIntent = new Intent();
				eIntent.putExtra("overlay_error", -13);//コメサバに接続できてない(非対応CH・公式)時になる
				setResult(CODE.RESULT_OVERLAY_ERROR, eIntent);
				ACT.finish();
				return;
			}
			headerBlank.addView(headerview, new LinearLayout.LayoutParams(list_width,
					cellHeight));
			firstBlueHeader = getBlueHeader();// テキストサイズをやり直した物を作っておく
		}
		if (commentTable != null) {
			commentTable.manualSort();
		}
	}

	/**
	 * Menuキー絡み処理
	 *
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		// プレイヤーのみはあり得ない
		inflater.inflate(R.menu.menu_disp_0_1_3, menu);
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(NLiveRoid.isDebugMode)Log.d("NLiveRoid","O OptionMenu " + item.getItemId());
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
			new OperationDialog(this, setting_boolean[13], (byte) 0).showSelf();// プレイヤーのみから来ても、コメ欄更新をdisableにしないようにinit_modeは0
			break;
		case R.id.setting:
			if (cmd == null) {
				if (getIntent == null)
					return false;
				cmd = (CommandMapping) getIntent.getSerializableExtra("cmd");
				if (cmd == null)
					return false;
			}
			if (liveInfo != null && liveInfo.getLiveID() != null
					&& commentTable != null) {//一応
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
				new ConfigDialog(this, liveInfo, setting_byte,
						setting_boolean, (byte) 0).showSelf();
			} else {
				MyToast.customToastShow(this, "読み込み中です");
			}
			break;
		case R.id.live_descOpen:
			showLiveDescription();
			break;
		case R.id.commentArea_change:
				showPostArea();
			break;
		case R.id.quick:
			if(NLiveRoid.isDebugMode)Log.d("NLiveRoid","O quick");
			if(quickDialog != null && quickDialog.isShowing()){
				quickDialog.cancel();
			}else{
			quickDialog = new QuickDialog(this,setting_byte,setting_boolean[19]);
			quickDialog.showSelf(setting_byte[40],setting_byte[41]);
			}
			break;
		}
		return false;
	}

	/**
	 * コンテキストメニュー生成時処理
	 */
	public void onCreateContextMenu(ContextMenu menu, View view,
			ContextMenuInfo info) {
		super.onCreateContextMenu(menu, view, info);
		AdapterContextMenuInfo adapterInfo = (AdapterContextMenuInfo) info;
		if (adapter.getCount() < adapterInfo.position) {// 立見などをタップした時におかしくなるArrayList.throwIndexOutOfBoundsException
			return;
		}
		final String[] row = adapter.getItem(adapterInfo.position);
		tempID = adapter.getItem(adapterInfo.position)[1];

		menu.add("このIDのコテハンを編集");
		menu.getItem(0).setOnMenuItemClickListener(
				new OnMenuItemClickListener() {
					@Override
					// 引数はメニューのテキスト
					public boolean onMenuItemClick(MenuItem arg0) {
						int defaultBgColor = idToBgColor.get(tempID) == null ? Color.WHITE
								: idToBgColor.get(tempID);
						int defaultForeColor = idToForeColor.get(tempID) == null ? Color.BLACK
								: idToForeColor.get(tempID);
						new HandleNamePicker(
								ACT,
								new ColorPickerView.OnColorChangedListener() {
									@Override
									public void colorChanged(int color) {
										// 色が選択されるとcolorに値が入る OKボタンで確定するので未使用
										int R = Color.red(color);
										int G = Color.green(color);
										int B = Color.blue(color);

									}
								}, defaultBgColor, defaultForeColor,tempID,idToHandleName.get(tempID)== null? tempID:idToHandleName.get(tempID),true).show();
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
						new ContextDialog(ACT, row,idToHandleName.get(row[1])== null? row[1]:idToHandleName.get(row[1]),(int) (viewW),defaultBgColor,defaultFoColor).showSelf();
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

	@Override
	protected void onSaveInstanceState(Bundle outState) {
	    outState.putString("WORKAROUND_FOR_BUG_19917_KEY", "WORKAROUND_FOR_BUG_19917_VALUE");
//	    super.onSaveInstanceState(outState);
	}
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if(NLiveRoid.isDebugMode)Log.d("NLiveRoid"," Over ----" + event.getKeyCode());
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			if (event.getAction() == KeyEvent.ACTION_UP)return false;
			if (gate != null && gate.isOpened() && drawerKey) {
				drawerKey = false;
				return true;
			}
			if (gate != null && gate.isOpened()) {// 詳細表示中なら閉じる
				gate.close();

				if (headerview != null)
					headerview.setVisibility(View.VISIBLE);
				// 開く前にisScrollEndだったらスクロール末尾に設定
				if (isUplayout()) {
					if (tempIsScrollEnd && listview != null) {
						listview.setSelection(0);
						listview.setVisibility(View.VISIBLE);
					}
				} else {
					if (tempIsScrollEnd && listview != null) {
						listview.setSelection(listview.getCount());
						listview.setVisibility(View.VISIBLE);
					}
				}
				return true;
			}
			// コメントフォーム表示中なら
			if (postArea != null && postArea.getVisibility() == View.VISIBLE) {
				if (setting_boolean == null) {
					if (getIntent == null)
						getIntent = getIntent();
					setting_boolean = getIntent.getBooleanArrayExtra("setting_boolean");
				}
				if(setting_boolean[20]&&event.getAction() == KeyEvent.ACTION_DOWN){
						//投稿フォームを閉じる
					postArea.setVisibility(View.GONE);
					postET.setVisibility(View.GONE);
					postB.setVisibility(View.GONE);
					if (setting_boolean[3]) {
						post_184.setVisibility(View.GONE);
						post_command.setVisibility(View.GONE);
						post_update.setVisibility(View.GONE);
						post_cdisp.setVisibility(View.GONE);
						post_desc.setVisibility(View.GONE);
						voiceInput.setVisibility(View.GONE);
						post_menu.setVisibility(View.GONE);
					}
					// タブレットでキーボードが引っ込まないのを防ぐ　何故かダイアログを出すと消えてくれる。超ーーー意味わかんない
					Dialog dummy = new Dialog(ACT);
					dummy.show();
					dummy.dismiss();
				return true;
				}else{
					//閉じない
				return false;
				}
			}else if(isFullScreend){//全画面にしたなら戻す
				isFullScreend = false;
				Intent intent = new Intent();
				intent.setAction("player_config.NLR");
				intent.putExtra("temp_fullscrn", setting_byte[24]);
				sendBroadcast(intent);//戻すインテントを投げる
				if(!setting_boolean[21]){//ステータスバーも戻しておく
					getWindow().clearFlags(
							WindowManager.LayoutParams.FLAG_FULLSCREEN);
				}
				setOrientation(setting_byte[24]);//設定に戻す
				return false;
			}

			// テーブルの値を保存
			if (setting_byte == null) {
				if (getIntent == null)
					getIntent = getIntent();
				setting_byte = getIntent.getByteArrayExtra("setting_byte");
			}

			Intent data = new Intent();
			data.putExtra("setting_byte", setting_byte);
			data.putExtra("setting_boolean", setting_boolean);
			data.putExtra("init_comment_count",
					getIntent.getShortExtra("init_comment_count", (short) 20));
			data.putExtra("cmd", cmd);
			data.putExtra("column_seq", column_seq);
			this.setResult(CODE.RESULT_COOKIE, data);
			return super.dispatchKeyEvent(event);
		}else if(event.getKeyCode() == KeyEvent.KEYCODE_MENU ){
			int menuAction = (setting_byte[40] & 0xF0) >> 4;
			Log.d("NLiveRoid","O MENUACTION " + menuAction);
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
				showPostArea();
				break;
			default :
				return super.dispatchKeyEvent(event);
			}
			return true;
		} else {
			return super.dispatchKeyEvent(event);// 無いとオプションメニューが出ない
		}
	}

	@Override
	public void onUserLeaveHint() {
		// BackGround
		// インテントブロードキャスト
		if (liveInfo == null)
			return;
		// if(gate != null && gate.isOpened())return;
		if (recognizeValue > 0)
			return;
		Intent backIntent = new Intent();
		backIntent.setAction("bindTop.NLR");
		backIntent.putExtra("playerNumber", 0);
		backIntent.putExtra("pid", getIntent.getIntExtra("pid", -1));
		backIntent.putExtra("lv", liveInfo.getLiveID());
		backIntent.putExtra("title", liveInfo.getTitle());
		this.getBaseContext().sendBroadcast(backIntent);
		if (commentTable != null) {
			// closeMainConnectionは他でもいろいろ呼ばれるので分けておく
			if (autoUpdateTask != null
					&& autoUpdateTask.getStatus() != AsyncTask.Status.FINISHED) {
				autoUpdateTask.cancel(true);
			}
			commentTable.killSpeech();
			commentTable.closeLogConnection();
			commentTable.closeMainConnection();
		}
		finish(CODE.RESULT_TO_BACKGROUND);

	}

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
				;
			}
			if(tempInfo.getPort() == null){
				error.setErrorCode(-13);
				return null;
			}
			if (NLiveRoid.apiLevel >= 8) {
				commentTable = new CommentTable((byte)0, tempInfo, ACT, adapter,column_seq,
						threadError, session, setting_byte[33],
						setting_boolean[12],
						setting_byte[33] == 3 ? setting_byte[28]
								: setting_byte[27], setting_byte[26],setting_byte[36],
						getIntent.getStringExtra("speech_skip_word"),
						setting_byte[29], getIntent.getShortExtra(
								"init_comment_count", (short) 20),
						setting_boolean[19]);
			} else {
				commentTable = new CommentTable((byte)1, tempInfo, ACT, adapter,column_seq,
						threadError, session, setting_byte[33],
						setting_boolean[12],
						setting_byte[33] == 3 ? setting_byte[28]
								: setting_byte[27], setting_byte[26],setting_byte[36],
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
				holder = new ViewHolder();//リソースを順序どおりに割り当てる→リソースIDとCommentTableCellがマップしているので、移行はカスタム列順でholder#tvsが並んでいる形になる
				for(byte i = 0; i < listSize; i++){
				holder.columnTvs[i] = (TextView) view.findViewById(column_ids[i]);
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

	/**
	 * ユーザー発言リストに渡すインスタンス
	 */
	@Override
	public CommentListAdapter createNewAdapter() {
		return new ExCommentListAdapter(this);
	}

	private static class ViewHolder {
		TextView[] columnTvs = new TextView[7];
		public byte id_index;
	}

	/**
	 * ビューサイズのゲッタ
	 */

	public int getViewWidth() {
		return viewW;
	}

	public int getViewHeight() {
		return viewH;
	}

	public int getCellHeight() {
		return cellHeight;
	}

	/**
	 * テーブルのWidthのゲッター
	 *
	 */

	public int getTableWidth(int columnNum) {
		if (setting_byte == null) {// 画面回転時nullになる場合がある
			if (getIntent == null)
				getIntent = getIntent();
			setting_byte = getIntent.getByteArrayExtra("setting_byte");
		}
		columnNum = column_seq[columnNum];
		if (!isPortLayt)columnNum += 11;// 0からpが並んでいて、横画面なら10上
		return setting_byte[columnNum];
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
	 * コテハン設定ダイアログの反映
	 *
	 * @param color
	 * @param name
	 */
	@Override
	public void setHandleName(int bgColor,int foreColor, String name) {
		Log.d("TEMP ","setHandleName" + tempID +"  name"+name +" bg"+bgColor +" fo"+foreColor);
		idToHandleName.put(tempID, name);
		idToBgColor.put(tempID, bgColor);
		idToForeColor.put(tempID, foreColor);
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
		listview.invalidateViews();
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
			if(NLiveRoid.isDebugMode)Log.d("NLiveRoid","O readHandleNameData ");
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
		if(NLiveRoid.isDebugMode)Log.d("NLiveRoid","O END readHandleNameData " + returnVal);
		return returnVal;
	}

	/**
	 * コテハンの書き込み
	 *
	 * @author Owner
	 *
	 */
	class WriteHandleName extends AsyncTask<Void,Void,Void>{

		@Override
		protected Void doInBackground(Void... params) {
			writeHandleName();
			return null;
		}

	}
	@Override
	public synchronized void writeHandleName() {
		try {

			if(NLiveRoid.isDebugMode)Log.d("NLiveRoid","O END writeHandleName ");
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
			if(NLiveRoid.isDebugMode)Log.d("NLiveRoid","O END writeHandleName " + (xml != null? xml.length():"XML NULL"));

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	class MultiTouchListener implements OnTouchListener {
		@Override
		public boolean onTouch(View v, MotionEvent event) {

			// タッチリスナは普通のヘッダと最初の青ヘッダのみ
			// getX()とかgetY()とかはリスナにセットされたviewに対しての座標なので相対的に使う
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:// 1点目 ID 0

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
						firstcurrentX = rect1.left == 0 ? isPortLayt ? -(viewW - rect1.right)
								: -(viewH - rect1.right)
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
					int y2 = (int) event.getY(1);
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
							viewW, cellHeight);
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
							viewW, cellHeight);
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
					firstBlueBlank.removeView(firstBlueHeader);
					if (rootFrame == null) {// ほとんどならない
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
						Intent eIntent = new Intent();
						eIntent.putExtra("overlay_error", -13);//コメサバに接続できてない(非対応CH・公式)時になる
						setResult(CODE.RESULT_OVERLAY_ERROR, eIntent);
						ACT.finish();
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
						}
					}
					// この値を保存するので若干保存する値がずれる、
					// タッチイベント起こさなければ、table_intがそのまま保存されるのでずれないのでOKとした
					if (isPortLayt) {
						setting_byte[8] = (byte) (firstcurrentX * 100 / viewW);
						setting_byte[9] = (byte) (firstcurrentY * 100 / viewH);
						setting_byte[10] = (byte) (list_bottom * 100 / viewH);
					} else {
						setting_byte[19] = (byte) (firstcurrentX * 100 / viewH);
						setting_byte[20] = (byte) (firstcurrentY * 100 / viewW);
						setting_byte[21] = (byte) (list_bottom * 100 / viewW);
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
						firstcurrentX = rect1.left == 0 ? isPortLayt ? -(viewW - rect1.right)
								: -(viewH - rect1.right)
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
							viewW, cellHeight);

				}
				break;
			case MotionEvent.ACTION_UP:
//				preparePositionValue();
				if (isFirstMoving) {
					// 上のレイヤーから乗せたViewを除去する
					((FrameLayout) parent.findViewById(R.id.layer2))
							.removeView(firstBlueBlank);
					firstBlueBlank.removeView(firstBlueHeader);
					if (rootFrame == null) {// ほとんどならない
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
						Intent eIntent = new Intent();
						eIntent.putExtra("overlay_error", -13);//コメサバに接続できてない(非対応CH・公式)時になる
						setResult(CODE.RESULT_OVERLAY_ERROR, eIntent);
						ACT.finish();
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
						}

						// setting_byte = new byte[10];
						// NLiveRoid app = (NLiveRoid)getApplicationContext();
						// setting_byte[0] =
						// app.getDetailsMapValue("type_width")==null?
						// 0:Byte.parseByte(app.getDetailsMapValue("type_width"));
						// setting_byte[1] =
						// app.getDetailsMapValue("id_width")==null?
						// 15:Byte.parseByte(app.getDetailsMapValue("id_width"));
						// setting_byte[2] =
						// app.getDetailsMapValue("command_width")==null?
						// 0:Byte.parseByte(app.getDetailsMapValue("command_width"));
						// setting_byte[3] =
						// app.getDetailsMapValue("time_width")==null?
						// 0:Byte.parseByte(app.getDetailsMapValue("time_width"));
						// setting_byte[4] =
						// app.getDetailsMapValue("num_width")==null?
						// 15:Byte.parseByte(app.getDetailsMapValue("num_width"));
						// setting_byte[5] =
						// app.getDetailsMapValue("comment_width")==null?
						// 70:Byte.parseByte(app.getDetailsMapValue("comment_width"));
						// setting_byte[6] =
						// app.getDetailsMapValue("table_cellheight")==null?
						// 3:Byte.parseByte(app.getDetailsMapValue("table_cellheight"));
						// setting_byte[7] =
						// app.getDetailsMapValue("x_pos")==null?
						// 0:Byte.parseByte(app.getDetailsMapValue("x_pos"));
						// setting_byte[8] =
						// app.getDetailsMapValue("y_pos")==null?
						// 50:Byte.parseByte(app.getDetailsMapValue("y_pos"));
						// setting_byte[9] =
						// app.getDetailsMapValue("bottom_pos")==null?
						// 50:Byte.parseByte(app.getDetailsMapValue("bottom_pos"));

					}
					// この値を保存するので若干保存する値がずれる、
					// タッチイベント起こさなければ、table_intarrayがそのまま保存されるのでずれないのでOKとした
					if (isPortLayt) {
						setting_byte[8] = (byte) (firstcurrentX * 100 / viewW);
						setting_byte[9] = (byte) (firstcurrentY * 100 / viewH);
						setting_byte[10] = (byte) (list_bottom * 100 / viewH);
					} else {
						setting_byte[19] = (byte) (firstcurrentX * 100 / viewH);
						setting_byte[20] = (byte) (firstcurrentY * 100 / viewW);
						setting_byte[21] = (byte) (list_bottom * 100 / viewW);
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
						isScrollEnd = true;
					} else {
						isScrollEnd = false;
					}
				} else {
					if (totalItemCount == firstVisible + visibleItemCount) {// スクロール入ってた
						// Log.d("log", "ADITIONAL TRUE");
						isScrollEnd = true;
						if (commentTable != null) {
							commentTable.scrollEnded();
						}
					} else if (listview.getCount() > 20) {// スクロールはずれた
															// 初回バッファでスクロールできなくなるので応急修正
															// Log.d("log",
															// "ADITIONAL FALSE");
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
			if(row == null || column_seq == null || column_ids == null){
				return null;
			}
			TextView tv = null;
			for(int i= 0 ; i < column_seq.length; i++){
				tv = (TextView)row.findViewById(column_ids[i]);
				if(tv == null){
					continue;
				}else{
					tv.setText(URLEnum.ColumnText[column_seq[i]]);
				}
				if(i == 5){
					NewLineHeader num = (NewLineHeader) row.findViewById(column_ids[column_seq[i]]);//左寄せはseq5で共通にしておく
					num.setGravity(Gravity.LEFT);
				}
			}

			if (NLiveRoid.apiLevel >= 8) {
				row.setOnTouchListener(new MultiTouchListener());
			} else {
				row.setOnTouchListener(new SimpleTouchListener());
			}
			row.setBackgroundColor(Color.parseColor("#F2F2F2"));
			return row;
		} else {
			LinearLayout row = (LinearLayout) inflater.inflate(row_resource_id,
					null);
			if(row == null || column_seq == null || column_ids == null){
				return null;
			}
			TextView tv = null;
			for(int i= 0 ; i < column_seq.length; i++){
				tv = (TextView)row.findViewById(column_ids[i]);

				if(tv == null){
					continue;
				}else{
					tv.setText(URLEnum.ColumnText[column_seq[i]]);
				}
				if(i == 5){
					CommentTableCell num = (CommentTableCell) row.findViewById(column_ids[column_seq[i]]);//左寄せはseq5で共通にしておく
					num.setGravity(Gravity.LEFT);
				}
			}
			if (NLiveRoid.apiLevel >= 8) {
				row.setOnTouchListener(new MultiTouchListener());
			} else {
				row.setOnTouchListener(new SimpleTouchListener());
			}
			row.setBackgroundColor(Color.parseColor("#F2F2F2"));
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
			if (firstcurrentX < -(viewW - 10)) {
				firstcurrentX = -(viewW - 10);
			} else if (firstcurrentX > viewW - 10) {
				firstcurrentX = viewW - 10;
			}
			// 縦画面時Y軸に対するリミット
			if (firstcurrentY > viewH - cellHeight / 2 * 5) {
				firstcurrentY = viewH - cellHeight / 2 * 5;
			}

		} else {
			// 横画面X軸に対するリミット
			if (firstcurrentX < -(viewH - 10)) {
				firstcurrentX = -(viewH - 10);
			} else if (firstcurrentX > viewH - 10) {
				firstcurrentX = viewH - 10;
			}
			// 横画面Y軸に対するリミット
			if (firstcurrentY > viewW - cellHeight / 2 * 5) {
				firstcurrentY = viewW - cellHeight / 2 * 5;
			}
		}
	}

	/**
	 * cmdを取得します。
	 *
	 * @return cmd
	 */
	@Override
	public CommandMapping getCmd() {
		return cmd;
	}

	/**
	 * cmdを設定します。
	 *
	 * @param cmd
	 *            cmd
	 */
	@Override
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
	 * バッファマークのゲッタ
	 *
	 * @return
	 */
	public LinearLayout getBufferMark() {
		if (bufferMark == null) {
			this.finish(CODE.RESULT_OVERLAY_ERROR);// 何故か検索後しばらく視聴後別の放送でなった
		}
		return bufferMark;
	}

	/**
	 * isPortLaytを取得します。
	 *
	 * @return isPortLayt
	 */
	public boolean isPortLayt() {
		return isPortLayt;
	}

	@Override
	public void clearAdapter() {
		if (commentTable != null) {
			// クリアだけだとバッファに残っている場合、描画してしまう
			commentTable.newSeetRady();
		}
		adapter.clear();
		adapter = new ExCommentListAdapter(this);

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
				//リスト生成
				int defaultBgColor = idToBgColor.get(userid) == null ? Color.WHITE
						: idToBgColor.get(userid);
				int defaultFoColor = idToForeColor.get(userid) == null ? Color.BLACK
						: idToForeColor.get(userid);
				new UserCommentedDialog(ACT, userid,idToHandleName.get(userid)== null? userid:idToHandleName.get(userid),commentedRows, dialog,defaultBgColor,defaultFoColor)
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
	public void getCommentLog(boolean isPremium) {// 引数は設定できるように予備的
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

	public boolean isUplayout() {
		return isUplayout;
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
	public void saveComments() {
		new SaveDialog(this, liveInfo, adapter).showSelf();
	}

	@Override
	public void showCommandDialog() {
		if (getIntent == null)
			getIntent = getIntent();
		new CommandDialog(this, false, getIntent.getStringExtra("Cookie"),
				liveInfo.getLiveID()).show();
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
	public void showOrientationAlertBuilder() {
		final String[] spinnerAdapter = new String[3];
		spinnerAdapter[0] = "回転する";
		spinnerAdapter[1] = "縦固定";
		spinnerAdapter[2] = "横固定";
		if (setting_byte == null) {
			if (getIntent == null)
				getIntent = getIntent();
			setting_byte = getIntent.getByteArrayExtra("setting_byte");
		}

		new AlertDialog.Builder(this)
				.setTitle(spinnerAdapter[setting_byte[24]])
				.setItems(spinnerAdapter,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								// ダイアログ表示時にデフォルト値を入れるsetSelctionで1度呼ばれる
								if (which != setting_byte[24]) {
									setOrientation(which);
								}
							}
						}).show();
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

	/**
	 * ユーザー名自動からハンドルネーム設定
	 */
	@Override
	public void setAutoHandleName(String id, String result) {
		idToHandleName.put(id, result);
		idToBgColor.put(id, -1);
		idToForeColor.put(id, -16777216);
	}

	/**
	 * ダイアログで＠コテ付けON/OFFした時に呼ばれる 設定変数を更新
	 *
	 * @param isAt
	 */
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

	/**
	 * isAtを取得します。
	 *
	 * @return isAt
	 */
	@Override
	public boolean isAt() {
		return isAt;
	}

	@Override
	public boolean isAtOverwrite() {
		return isAtoverwrite;
	}

	@Override
	public Context getAPPContext() {
		return this.getBaseContext();
	}

	@Override
	public void reloadPlayer() {
		Intent intent = new Intent();
		intent.setAction("player_reload.NLR");
		this.getBaseContext().sendBroadcast(intent);
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
			AUTO_FLAG = false;
			super.onCancelled();
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
					// e.printStackTrace();
					Log.d("NLiveRoid",
							"InterruptedException at OverLay AutoUpdateTask");
					AUTO_FLAG = false;
					break;
				} catch (IllegalArgumentException e) {
					// e.printStackTrace();
					Log.d("NLiveRoid",
							"IllegalArgumentException at OverLay AutoUpdateTask");
					AUTO_FLAG = false;
					break;
				} catch (Exception e) {
					e.printStackTrace();
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
				if (isUplayout()) {// UIスレッドでやんなきゃ落ちる
					if (listview != null)
						new SetSelection().execute(true);
				} else {
					if (listview != null)
						new SetSelection().execute(false);
				}
			}
		}else{//commentTableがnull(なんでなるのかわからない)
			Log.d("NLiveRoid","CommentSock was null " + liveInfo);
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
			if (NLiveRoid.apiLevel >= 8) {
				commentTable = new CommentTable((byte)0, this.liveInfo, ACT, adapter,column_seq,
						this.error, getIntent.getStringExtra("Cookie"), setting_byte[33],
						setting_boolean[12],
						setting_byte[33] == 3 ? setting_byte[28]
								: setting_byte[27], setting_byte[26],setting_byte[36],
						getIntent.getStringExtra("speech_skip_word"),
						setting_byte[29], getIntent.getShortExtra(
								"init_comment_count", (short) 20),
						setting_boolean[19]);
			} else {
				commentTable = new CommentTable((byte)1, this.liveInfo, ACT, adapter,column_seq,
						this.error, getIntent.getStringExtra("Cookie"), setting_byte[33],
						setting_boolean[12],
						setting_byte[33] == 3 ? setting_byte[28]
								: setting_byte[27], setting_byte[26],setting_byte[36],
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
			if (arg) {
				listview.setSelection(0);
			} else {
				listview.setSelection(listview.getCount());
			}
			if(listview != null)listview.invalidate();
		}
	}

	@Override
	public void layerChange(int which) {
		switch(which){
		case 0://前面
			break;
		case 1://背面へ
			this.finish(CODE.RESULT_LAYERCHANGE_TO_BACKFLASH);
			break;
		case 2://プレイヤーのみ
			this.finish(CODE.RESULT_LAYERCHANGE_TO_PLAYERONLY);
			break;
			//コメントのみは削除
		}
	}

	// 自動更新の更新
	@Override
	public void setUpdateInterval(byte interval) {
		if (setting_byte == null) {
			if (getIntent == null)
				getIntent = getIntent();
			setting_byte = getIntent.getByteArrayExtra("setting_byte");
		}
		setting_byte[32] = interval;
		// 更新間隔変更処理
		if (autoUpdateTask != null) {
			if (autoUpdateTask.getStatus() != AsyncTask.Status.FINISHED) {
				autoUpdateTask.cancel(true);
			}
			if (interval < 0) {
				return;
			}
		}
		autoUpdateTask = new AutoUpdateTask();
		autoUpdateTask.execute();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == CODE.RESULT_RECOGNIZE_SPEECH) {
			// 音声認識から
			// 結果文字列リスト
			AudioManager audio = (AudioManager) getSystemService(AUDIO_SERVICE);
			audio.setStreamVolume(AudioManager.STREAM_MUSIC, recognizeValue,
					AudioManager.FLAG_ALLOW_RINGER_MODES);
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
	}

	@Override
	public void allUpdate() {
		if(NLiveRoid.isDebugMode)Log.d("NLiveRoid","O all UPDATE ");
		this.finish(CODE.RESULT_ALL_UPDATE);
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
	public void setSpeachSettingValue(byte isEnable, byte speed,byte vol, byte pich) {
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
			setting_boolean[14] = value > 0 ? false : true;//[14]はshowcommentだからtrueなら非表示はfalse
			break;
		case 1:
			setting_boolean[15] = value > 0 ? true : false;
			break;
		case 2:
			setting_boolean[16] = value > 0 ? true : false;
			break;
		case 3:
			setting_boolean[17] = value > 0 ? true : false;
			break;
		case 4:
			setting_boolean[18] = value > 0 ? true : false;
			break;
		case 5:
			setting_byte[30] = (byte) value;
			break;
		case 10:
			Intent intent = new Intent();
			intent.setAction("player_config.NLR");
			intent.putExtra("operation", (byte) 10);
			intent.putExtra("value", (byte) value);
			sendBroadcast(intent);
			setting_byte[43] = value;
			return;
		}

		// ブロキャス
		Intent playerConf = new Intent();
		playerConf.setAction("player_config.NLR");
		playerConf.putExtra("operation", operation);
		playerConf.putExtra("value", value);
		sendBroadcast(playerConf);
	}

	@Override
	public void setFullScreen(boolean isChecked) {
		if (setting_boolean == null) {
			if (getIntent == null)
				getIntent = getIntent();
			setting_boolean = getIntent.getBooleanArrayExtra("setting_boolean");
		}
		setting_boolean[21] = isChecked;
				Intent playerConf = new Intent();
				playerConf.setAction("player_config.NLR");
				playerConf.putExtra("operation", (byte)100);
				playerConf.putExtra("value", isChecked? (byte)1:(byte)0);
				sendBroadcast(playerConf);
		if(isChecked){
			getWindow().addFlags(
					WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}else{
			getWindow().clearFlags(
					WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}
	}

	@Override
	public boolean isContainsUserID(String string) {
		return idToHandleName.containsKey(string);
	}


	/**
	 * isSetNameReadyを取得します。
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
		long val = -1;
		NLiveRoid app = (NLiveRoid)getApplicationContext();
		if(app.getDetailsMapValue("offtimer_start") != null){
			val = Long.parseLong(app.getDetailsMapValue("offtimer_start"));
		}
		return val;
	}

	@Override
	public byte getOffTimerValue() {
		NLiveRoid app = (NLiveRoid)getApplicationContext();
		byte val = -1;
		if(app.getDetailsMapValue("off_timer") != null){
			 val = Byte.parseByte(app.getDetailsMapValue("off_timer"));
		}
		setting_byte[37] = val;
		return val;
	}
	private void showLiveDescription() {
		if(NLiveRoid.isDebugMode)Log.d("NLiveRoid", "O showLiveDescription");
		if (gate != null && gate.isOpened()) {// 開いてたら戻す
			gate.close_noanimation();
			if (listview == null) {// あまり無いと思うが。
				Log.d("NLiveRoid", "O LIST ERROR");
				MyToast.customToastShow(this, "レイアウトをやり直せませんでした\nコ読み込み中と思われます");
				return ;
			}
			if (listview != null) {
				listview.setVisibility(View.VISIBLE);
			}
			if (headerview != null) {
				headerview.setVisibility(View.VISIBLE);
			}
			drawerKey = false;
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
			NLiveRoid app = (NLiveRoid) getApplicationContext();// シンプルじゃない、嫌い
			if (app == null || app.getGateView() == null) {
				MyToast.customToastShow(this, "読み込み中です");
				return ;
			}
			if (getIntent == null)
				getIntent = getIntent();
			gate = new Gate(this, app.getGateView(), liveInfo, true,
					getIntent.getStringExtra("Cookie"));
			GateView gateView = (GateView) app.getGateView();
			if (gateView == null) {
				MyToast.customToastShow(this, "読み込み中です");
				return;
			}
			ViewGroup gateParent = (ViewGroup) gateView.getView()
					.getParent();
			if (gateParent != null) {
				gateParent.removeView(app.getGateView().getView());
			}

			((ViewGroup) parent.getParent()).addView(app.getGateView()
					.getView());
			gate.show(this.getResources().getConfiguration());
		}
	}
	@Override
	public LiveInfo getLiveInfo() {
		return liveInfo;
	}
	@Override
	public void shoeTweetDialog() {
		tweetDialog = new TweetDialog(this,liveInfo, getIntent.getStringExtra("twitterToken"),false);
		tweetDialog.showSelf();
	}
	@Override
	public boolean listVisibleGetSetter(boolean isSet,boolean isChecked) {
		if(!isSet)return listview==null? false:listview.getVisibility() == View.VISIBLE;
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
					setting_byte[10] = -43;//Height
				}
			}else{//方向下
				if(position){//上
					setting_byte[8] = 0;//X
					setting_byte[9] = 0;//Y
					setting_byte[10] = 43;//Height
				}else{//下
					setting_byte[8] = 0;//X
					setting_byte[9] = 50;//Y
					setting_byte[10] = 43;//Height
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
	public void showPositionDialog() {
		if(listDialog != null && listDialog.isShowing())listDialog.cancel();
		Builder d = new AlertDialog.Builder(this).setItems(this.listPositionGetSetter(false,false)? new CharSequence[]{"上","下"}:new CharSequence[]{"左","右"},
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
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
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			isFullScreend = true;//バックキーの為フラグをONにしておく
			Intent intent = new Intent();
			intent.setAction("player_config.NLR");
			intent.putExtra("temp_fullscrn", (byte)-1);
				getWindow().addFlags(
						WindowManager.LayoutParams.FLAG_FULLSCREEN);
			sendBroadcast(intent);
			break;
		case 4://ログ取得
			if(liveInfo.getIsPremium().equals("0")){
				getCommentLog(false);
				}else{
				getCommentLog(true);
				}
			break;
		case 5:
			if (liveInfo != null && liveInfo.getLiveID() != null
					&& commentTable != null) {//一応
				new ConfigDialog(this, liveInfo, setting_byte,
						setting_boolean, (byte) 0).showSelf();
			}
			break;
		case 6://視聴画面終了 ここで何かエラーしてるのに終了して設定が保存されておかしくなるのが怖いが。。
			Intent data = new Intent();
			data.putExtra("setting_byte", setting_byte);
			data.putExtra("setting_boolean", setting_boolean);
			data.putExtra("init_comment_count",
					getIntent.getShortExtra("init_comment_count", (short) 20));
			data.putExtra("cmd", cmd);
			this.setResult(CODE.RESULT_QUICKFINISH_OR_RINGING, data);
			finish();
			break;
		}
	}

	@Override
	public boolean showPostArea() {
		if(NLiveRoid.isDebugMode)Log.d("NLiveRoid", "showPostArea");
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
		// 投稿フォームをレイアウトし直すじゃないとはみ出す
//		postET.setLayoutParams(new TableRow.LayoutParams(-1, -2));
//		postB.setLayoutParams(new TableRow.LayoutParams(-2, -2));

		if (postArea.getVisibility() == View.GONE) {
			postArea.setVisibility(View.VISIBLE);
			postET.setVisibility(View.VISIBLE);
			postB.setVisibility(View.VISIBLE);
			if((setting_byte[40] & 0xF0) >> 4 == 2&&(post_184 == null || post_command == null || post_desc == null || voiceInput == null || post_menu == null)){
				setting_boolean[3] = true;
				if(setting_boolean[2]){//イベントリスナとinitでもここを聞いてるけどifの記述上しかたない
					post_184 = (CheckBox)parent.findViewById(R.id.postarea_184_up);
					post_command = (Button)parent.findViewById(R.id.postarea_command_up);
					post_update = (Button)parent.findViewById(R.id.postarea_update_up);
					post_menu = (Button)parent.findViewById(R.id.postarea_menukey_up);
					post_cdisp = (CheckBox)parent.findViewById(R.id.postarea_commentdisp_up);;
					voiceInput = (Button) parent.findViewById(R.id.postarea_voiceinput_up);
					post_desc = (Button)parent.findViewById(R.id.postarea_desc_up);
					((ViewGroup)post_184.getParent()).setMinimumHeight((int)(30/((NLiveRoid)getApplicationContext()).getMetrics()));
				}else{
					post_184 = (CheckBox)parent.findViewById(R.id.postarea_184_down);
					post_command = (Button)parent.findViewById(R.id.postarea_command_down);
					post_update = (Button)parent.findViewById(R.id.postarea_update_down);
					post_desc = (Button)parent.findViewById(R.id.postarea_desc_down);
					post_cdisp = (CheckBox)parent.findViewById(R.id.postarea_commentdisp_down);
					voiceInput = (Button) parent.findViewById(R.id.postarea_voiceinput_down);
					post_menu = (Button)parent.findViewById(R.id.postarea_menukey_down);
				}
				setFormListeners();
			}
			if (setting_boolean[3]) {
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
	public void settingChange(int casevalue, byte npValue,byte[] seq) {
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
			Log.d("RESULT ---- ", " " + npValue);
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
			Log.d("NLiveRoid",  "O changeColumnSeq");
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
		case 11:
			Intent intent = new Intent();
			intent.setAction("player_config.NLR");
			if(isPortLayt){
			intent.putExtra("temp_fullscrn", (byte)11);
			intent.putExtra("value", setting_byte[22]);
			}else{
				intent.putExtra("temp_fullscrn", (byte)12);//しかたなく12としておく
				intent.putExtra("value", setting_byte[23]);
			}
			sendBroadcast(intent);//戻すインテントを投げる
			return;
		}
		//コメント欄をやり直す
		onConfigurationChanged(this.getResources().getConfiguration());
	}

	@Override
	public byte[] getColumnSeq() {
		return column_seq;
	}

	@Override
	public void allCommFunction(int index, LiveInfo info) {
		switch(index){
		case 0:
		case 10:
			this.finish(CODE.RESULT_FROM_GATE_FINISH);//直接CommunityTabも呼べるけど、onWindowFocusChangedが変になるので背面時のTransから行くのと同じにした
			Intent bcService = new Intent();
			bcService.setAction("return_f.NLR");
			bcService.putExtra("r_code", CODE.RESULT_FROM_GATE_FINISH);
			bcService.putExtra("archive",info.getLiveID());
			this.sendBroadcast(bcService);
			break;
		}
	}

	@Override
	public boolean isNsen() {
		return getIntent.getBooleanExtra("isnsen", false);
	}
}
