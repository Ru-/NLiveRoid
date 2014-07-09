package nliveroid.nlr.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;

import nliveroid.nlr.main.parser.XMLparser;

import org.apache.http.ParseException;
import org.jboss.netty.channel.DefaultExceptionEvent;
import org.xmlpull.v1.XmlPullParserException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
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
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Environment;
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
import android.webkit.WebView;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.flazr.rtmp.reader.PictureReader;
import com.flazr.rtmp.reader.PreviewReader;

public class BCPlayer extends Activity implements IOwnerContext,CommentPostable,HandleNamable{
	private static BCPlayer ACT;
	private static int webViewW;//spplayerで必要
	private static int webViewH;
	private static int listViewW = 0;//spplayerで必要
	private static int listViewH = 0;
	private float density = 0;
	private int cellHeight = 0;
	private static int resizeW;
	private static int resizeH;


	private LiveInfo liveInfo;
    private boolean[] setting_boolean;
	private byte[] setting_byte;

	private boolean keepActivity = true;

	private static CommentTable commentTable;
	private CommandMapping cmd;

	private static ProgressDialog dialog;

	private BroadcastReceiver audioReceiver;

	private ErrorCode error;
	private static View parent;
	private Intent getIntent;


	//同一レイヤー用フィールド
		private LayoutInflater inflater;
		/** 描画開始座標：Y軸 **/
		private int firstcurrentY = 50;
		private int firstcurrentX = 0;
		/** タッチ座標：Y軸 **/
		private int firstoffsetY = 0;
		private int firstoffsetX = 0;
		//2点目の座標変数
		/** 描画開始座標：Y軸 **/
		private int secondcurrentY = 50;
		/** タッチ座標：Y軸 **/
		private int secondoffsetY = 0;
		//xは変える必要がない

		private ExCommentListAdapter adapter;
		private ViewGroup rootFrame;
		private boolean isFirstMoving = false;
		private boolean isSecondMoving = false;
		/** テーブルのリスト類 */
		private ListView listview;
		private static LinearLayout bufferMark;
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
		private EditText postET;
		private Button postB;
		private CheckBox post_184;
		private Button post_command;
		private Button post_update;
		private Button post_cdisp;
		private Button post_desc;
		private ImageButton voiceInput;
		private Button post_menu;

		private int recognizeValue = 0;


		private AutoUpdateTask autoUpdateTask;
		private boolean AUTO_FLAG = true;

		private static boolean isScrollEnd = true;
		private static boolean tempIsScrollEnd = true;

		private static boolean isPortLayt;

		private Gate gate;
		private boolean isAt;
		private boolean isAtoverwrite;
		private int row_resource_id = R.layout.comment_row;
		private byte[] column_seq;
		private int[] column_ids;

		private boolean isSetNameReady;//コテハン読み込み完了フラグ
		private float heightAdjust=2;

		private SurfaceView camSurface;


		public static byte[] dummyVFrame;

		public static SocketAddress REMOTE_ADDR;
		private LiveSettings liveSetting;

		private boolean setCamLayout;


		private Timer remainTimer;
		private RemainTimeUpdate remainTask;
		private HeatBeatLoop heatbeat;
		private TextView comment_count;
		private TextView view_count;
		private TextView remain_time;

		private AlertDialog listDialog;
		private QuickDialog quickDialog;

		private ClientHandler handler;
		private Button func0;
		private Button func1;
		private Button restart_bt;

		private int restartingStream;

		private ImageView mImageView;
		private int previewWidth;
		private int previewHeight;
		final private boolean red5 = true;
		final private String red5StreamID = "stream1393152526036";
		final private String red5IP = "192.168.1.5";
		private TweetDialog tweetDialog;
		static{
	    	System.loadLibrary("enc");
	    }

		private native int inittest(int video_flag,int audio_flag);
	/**
	 * バックグラウンドでビジーなタスクが実行された時
	 * onPauseもonDestroyも呼ばれずにプロセスがkillされる
	 * その時の復帰は新たにPIDを取得され、onCreateが呼ばれる
	 *
	 * 最初にOverLayを呼ぶときはこのクラスのonResumeは呼ばれない
	 *f
	 * OverLayはバックグラウンドから復帰する際に
	 * まずonResumeが呼ばれ、上記のようにこのクラスのonCreateが呼ばれ
	 * 表示する設定によって
	 * OverLayのonPauseが呼ばれた後に、このクラスからOverLayのonCreate、OverLayのonResumeと呼ばれる(singleTopなので)
	 */
	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
		Window window = getWindow();
		window.setSoftInputMode(
				LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        getWindow().addFlags(
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		if(Integer.parseInt(VERSION.SDK)>=11){
			window.setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED, WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
			}

		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//とりあえず縦固定
		// プロセスの優先度を上げる
		android.os.Process.setThreadPriority(
		          android.os.Process.myPid());
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		ACT = this;
		inflater = LayoutInflater.from(ACT);
		parent = inflater.inflate(R.layout.live, null);
        camSurface = (SurfaceView) parent.findViewById(R.id.camframe);
		setContentView(parent);
		//着信時に視聴画面を殺す(OverLayを呼ぶ(layer_numが0)でも、BACKキーでPlayerにした後着信すると入らなくて困るので、全表示モードでセット)
				TelephonyManager mTelephonyManager
		        = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
				PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
				    @Override
				    public void onCallStateChanged(int state, String number) {
				    	if(state == TelephonyManager.CALL_STATE_RINGING){
				            // 着信 又は通話中に視聴画面をを落とす
				             Log.d("NLiveRoid","Detect ringing  and call finish player activity");
				    		ACT.standardFinish();
				    	}
				       }
				   };
				   mTelephonyManager.listen
			        (mPhoneStateListener,PhoneStateListener.LISTEN_CALL_STATE);
		getIntent = getIntent();


        func0  = (Button) parent.findViewById(R.id.function_bt0);

        func1  = (Button) parent.findViewById(R.id.function_bt1);
        restart_bt  = (Button) parent.findViewById(R.id.restart_bt);

	    new AudioTask().execute();
		new InitFlashPlayer().execute();
	}

	/**
	 * 初期化を別タスクにする
	 * @author Owner
	 *
	 */
	class InitFlashPlayer extends AsyncTask<Void,Void,Integer>{

		private BitmapDrawable drawable;


		@Override
		protected void onPreExecute(){
			if(dialog == null){//2重対策
			dialog = ProgressDialog.show(ACT, " ",
		               "Loading live information..", true,true);
			 dialog.setContentView(R.layout.progress_dialog_flash);
			dialog.show();
			}
		}
		@Override
		public void onCancelled(){
			if(dialog != null && dialog.isShowing()) {
                dialog.dismiss();
                dialog = null;
            }
            super.onCancelled();
		}
		@Override
	protected Integer doInBackground(Void...arg){
		//テーブル表示非表示共通
		setting_boolean = getIntent.getBooleanArrayExtra("setting_boolean");
		setting_byte = getIntent.getByteArrayExtra("setting_byte");

		NLiveRoid app = (NLiveRoid)getApplicationContext();
		app.initNoTagBitmap();
		app.createGateInstance();//TopTabの時点で取得されたもの(ウィンドウwidth等)がインテントに引き継がれてきているのでここでgateViewすれば大丈夫
		app.setForeACT(ACT);
		error = app.getError();

        DefaultExceptionEvent.setUIContext(ACT.getBaseContext());
		if(error.getErrorCode() != 0){//設定ファイルの読み込みに失敗
			return -1;
		}
		if(setting_byte[37] > 0){//オフタイマーセットされていたら
			long start_t = getIntent.getLongExtra("offtimer_start", 0);//履歴からの場合、offtimer_startが0になっている
			if(start_t == 0 || (System.currentTimeMillis() - start_t)/1000 > setting_byte[37]*60){//履歴からの起動ですでにオフタイマー時間を経過していた
				Intent topTab = new Intent(ACT,TopTabs.class);
				topTab.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				ACT.startActivity(topTab);
				errorFinish(CODE.RESULT_FLASH_ERROR,-47);
				return -100;//何もしない
			}
		}
		//背景画像があればセットする
				try{
				FileInputStream back_v = ACT.openFileInput("back_v");
//				Log.d("File IS ---- ",""+back_v);
						if(back_v != null){
							Bitmap back = BitmapFactory.decodeStream(back_v);
							drawable = new BitmapDrawable(back);
						}
			    	} catch (FileNotFoundException e) {
//						e.printStackTrace();
			    		Log.d("NLR","Fnot Exception");
					}catch(OutOfMemoryError e){
						e.printStackTrace();
						ACT.runOnUiThread(new Runnable(){//エラー使ってないみたいだからrunOnにしちゃった
							@Override
							public void run(){
								MyToast.customToastShow(ACT, "背景画像が大きすぎたため、適用に失敗しました");
							}
						});
					}catch(Exception e){
						e.printStackTrace();ACT.runOnUiThread(new Runnable(){
							@Override
							public void run(){
								MyToast.customToastShow(ACT, "背景画像適用時エラー");
							}
						});
					}
		//バックグランドでBCが動いているか
			//ノティフィケーションから来ることはない
			liveInfo = (LiveInfo)getIntent.getSerializableExtra("LiveInfo");
			if(NLiveRoid.isDebugMode)Log.d("NLiveRoid","VVVVVVVV " + liveInfo.getLiveID());
		if(liveInfo == null||liveInfo.getLiveID() == null){//新着失敗から取得できなかった||ノティフィから取得失敗の場合に起こる
				if(liveInfo != null)Log.d("NLiveRoid","INFOLV " + liveInfo.getLiveID());
			return -1;
		}
		//コマンドオブジェクトの初期化 ここでliveinfo.isOwner()があること
		cmd = (CommandMapping) getIntent.getSerializableExtra("cmd");
		if(cmd == null){
		String[] cmdValue = new String[4];
		cmdValue[0] = app.getDetailsMapValue("cmd_cmd");
		cmdValue[1] = app.getDetailsMapValue("cmd_size");
		cmdValue[2] = app.getDetailsMapValue("cmd_color");
		cmdValue[3] = app.getDetailsMapValue("cmd_align");
		for(int i = 0 ; i < 4; i++){
			if(cmdValue[i] != null){
				if(i==3){
				cmd = new CommandMapping(cmdValue[0],cmdValue[1],cmdValue[2],cmdValue[3],true);
				break;
				}
			}
			if(i==3){//1つでもnullがあったら普通の初期化
				cmd = new CommandMapping(true);
			}
		}
		}

		density = getIntent.getFloatExtra("density", 1.5F);
		webViewW = getIntent.getIntExtra("viewW",getWindowManager().getDefaultDisplay().getWidth());
		webViewH = getIntent.getIntExtra("viewH",getWindowManager().getDefaultDisplay().getHeight());
		resizeW = getIntent.getIntExtra("resizeW", (int)(getWindowManager().getDefaultDisplay().getWidth()*1.72D));
		resizeH = getIntent.getIntExtra("resizeH", (int)(getWindowManager().getDefaultDisplay().getHeight()*1.8D));
		app.setViewHeightDp(webViewH);//別プロセスなので今の所詳細開いた時のみに必要0.8.62
		column_seq = getIntent.getByteArrayExtra("column_seq");
		app.setViewWidthDp(webViewW);
		app.setMetrics(density);
		liveSetting = (LiveSettings) getIntent.getSerializableExtra("init");
		if(liveSetting == null){
			return -11;
		}else if(NLiveRoid.isDebugMode){
			Log.d("NLiveRoid","BCPlayer LMODE " + liveSetting.getMode());
		}


		//setting_byte[31]に関係なく全て背面(このレイヤー)で扱う
		idToHandleName = new ConcurrentHashMap<String, String>();
		idToBgColor = new ConcurrentHashMap<String, Integer>();
		idToForeColor = new ConcurrentHashMap<String, Integer>();
		app.createGateInstance();
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

		heatbeat = new HeatBeatLoop();
		heatbeat.execute();
		if (liveInfo.getPort() != null) {// アラート新着からの場合すでにgetPlayerの情報がある

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
			density = getIntent.getFloatExtra("density", 1.5f);
			initTable();
		} else {// 放送情報のポート番号がない場合、getPlayerに取りに行き、ステータス分岐

			byte[] liveStatus = null;
			try{
			liveStatus = Request.getPlayerStatusToByteArray(
					liveInfo.getLiveID(), error,getIntent.getStringExtra("Cookie"));
			}catch(Exception e){
				e.printStackTrace();
				return -3;
			}
			if (liveStatus == null) {
				Log.d("BCPlayer","LiveStatus was null");
				return -3;
			}
			String check = null;
			try {
				check = new String(liveStatus,"UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}

			if(check == null)return -30;

			if (check.contains("notlogin")) {// 他にもinvalid_lv(数値じゃない場合など)notfound(不明)
				return -4;
			} else if (check.contains("closed")) {
				return -5;
			} else if (check.contains("comingsoon")) {
				return -6;
			} else if (check.contains("require_community_member")) {
				return -7;
			} else if(check.contains("incorrect_account_data")){//アカウント無しでも見れる放送(コメサバへ繋げない)
				return -8;
			}else if (check.contains("timeshift_ticket_exhaust")) {//TS視聴にチケットが必要なもの
				return -9;
			}  else if (check.contains("usertimeshift")) {//コミュ限でTS
				return -7;
			} else if (check.contains("noauth")) {//チャンネルで終了していてTS提供されてない放送でなる
				return -5;
			}else if (error != null && error.getErrorCode() == 0) {
				if(check.length()<100){
					Log.d("NLiveRoid", "failed_comment");//100文字以下なら失敗の可能性が高い
				}else{
					try{
					XMLparser.getLiveInfoFromAPIByteArray(liveStatus, liveInfo);
					} catch (NullPointerException e1) {//頻繁に起きるが。。
						e1.printStackTrace();
						error.setErrorCode(-37);
						return -100;//画面は落とさない
					} catch (ParseException e1) {
						e1.printStackTrace();
						error.setErrorCode(-37);
						return -100;//画面は落とさない
					} catch (XmlPullParserException e1) {
						e1.printStackTrace();
						error.setErrorCode(-30);
						return -100;//画面は落とさない
					} catch (IOException e1) {
						e1.printStackTrace();
						error.setErrorCode(-37);
						return -100;//画面は落とさない
					}
					//成功してたらコメ欄初期化----------------
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
					initTable();
				}
			}
		}

		//残り時間の計算
		remainTimer = new Timer();
		remainTask = new RemainTimeUpdate();
		remainTask.culcTime(liveInfo.getEndTime());
		remainTimer.schedule(remainTask, 0, 1000);



				//ClientHandlertを初期化する→カメラのパラメタが必要+startPreviewの前じゃないと、モード判定で動画のときにプレビューきなくなる
				handler = new ClientHandler(ACT,liveSetting);

		return 0;
		}//End of doInBack

		private void initTable(){

			listViewW = (int) (getIntent.getIntExtra("viewW", ACT.getWindowManager()
					.getDefaultDisplay().getWidth())*density);//キャストで端が多少少なくなる?
			listViewH = (int) (getIntent.getIntExtra("viewH", ACT.getWindowManager()
					.getDefaultDisplay().getHeight())*density);
			if(listViewW > listViewH){
				int temp = listViewW;
				listViewW = listViewH;
				listViewH = temp;
			}

			// テーブル設定の読み込み 上表示、X移動可、Y移動可
			// ポストエリア等より先に判定が必要
			//テーブルの位置決定
			Configuration config = getResources().getConfiguration();
			if(config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
				isPortLayt = false;
				firstcurrentX = (int) ((double)listViewH*(setting_byte[19]*0.01D));
				firstcurrentY = (int) ((double)listViewW*(setting_byte[20]*0.01D));
				list_bottom = (int)((double)listViewW*(setting_byte[21]*0.01D));
				list_width = (int) ((double) listViewH * (setting_byte[39] * 0.01D));
				cellHeight = (int) (listViewH*setting_byte[18]*0.01D);
				enable_moveX = setting_boolean[10];
				enable_moveY = setting_boolean[11];
			}else{
				isPortLayt = true;
				firstcurrentX =  (int) ((double)listViewW*(setting_byte[8]*0.01D));
				firstcurrentY = (int) ((double)listViewH*(setting_byte[9]*0.01D));
				list_bottom = (int)((double)listViewH*(setting_byte[10]*0.01D));
				list_width = (int) ((double) listViewW * (setting_byte[38] * 0.01D));
				cellHeight = (int) (listViewH*setting_byte[7]*0.01D);
				enable_moveX = setting_boolean[8];
				enable_moveY = setting_boolean[9];
			}
		}
		@Override
		protected void onPostExecute(Integer arg){
			if(dialog != null && dialog.isShowing()) {
                dialog.dismiss();
                dialog = null;
            }
			Log.d("NLR","BCPLAYER ARG :" + arg);
			if(arg == 0||arg == -12){

				//LiveSettingsが読み込まれていてどのモードも押して大丈夫じゃないといけないのでここでリスナセット
				Button setting_bt = (Button)parent.findViewById(R.id.live_setting_bt);
				setting_bt.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
							new LiveSettingDialog(ACT,liveSetting).showSelf();
					}
				});
				func0.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View v) {
						switch(liveSetting.getMode()){
						case 0:
							PreviewReader reader = (PreviewReader)handler.getReader();
							if(reader.getParameters() != null)reader.getParameters().focus();
						case 1:
							break;
						case 2://静止画モード
							break;
						case 3:
							break;
						}
					}
		        });
		        func1.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View v) {
						switch(liveSetting.getMode()){
						case 0:
							PreviewReader reader = (PreviewReader)handler.getReader();
							new CamEffectSettingDialog(ACT,(int) (webViewH/density/2),reader.getParameters()).showSelf();
							break;
						case 1:
							break;
						case 2://静止画モード 画像選択させる→ギャラリーを開く
							Intent intent = new Intent();
							intent.setType("image/*");
							intent.setAction(Intent.ACTION_GET_CONTENT);
							startActivityForResult(intent, CODE.REQUEST_GALALY_PLAYERVIEW);
							break;
						case 3://パスを入力させる
							final EditText editView = new EditText(ACT);
							if(liveSetting.getFilePath() != null)editView.setText(liveSetting.getFilePath());
						    new AlertDialog.Builder(ACT)
						        .setIcon(android.R.drawable.ic_dialog_info)
						        .setTitle("動画ファイルのパス\n")
						        .setView(editView)
						        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
						            public void onClick(DialogInterface dialog, int whichButton) {
						                        liveSetting.setFilePath(editView.getText().toString());
						            }
						        })
						        .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
						            public void onClick(DialogInterface dialog, int whichButton) {
						            }
						        })
						        .show();
							break;
						}
					}
		        });
		        restart_bt.setOnClickListener(new OnClickListener(){

					@Override
					public void onClick(View v) {
						if(restartingStream == 1){
							MyToast.customToastShow(ACT, "処理中です");
							return;
						}
						restartStream();
					}

		        });
				if(drawable != null)parent.findViewById(R.id.layer3).setBackgroundDrawable(drawable);
				comment_count = (TextView)parent.findViewById(R.id.comment_count);
				view_count = (TextView)parent.findViewById(R.id.view_count);
				remain_time = (TextView)parent.findViewById(R.id.remain_time);


				new ReadHandleName().execute();//失敗時トースト表示するのでここで読み込み
				// アダプタの初期化
				adapter = new ExCommentListAdapter(ACT);

				if(setting_byte == null){
					if(getIntent == null)getIntent = getIntent();
					setting_byte = getIntent.getByteArrayExtra("setting_byte");;
				}
				setOrientation(setting_byte[24]);
				init();
				changeMode(liveSetting.getMode());//UIスレッドで行う必要がある
				//アクティビティ終了しないでエラーだけを表示+必要ならここ独自に終了

				//設定値インスタンス化したらchangeModeでカメラか静止画のReaderを初期化する
				//マイクはそれぞれのReaderで初期化している
			}else if(arg == -1){
				ACT.errorFinish(CODE.RESULT_FLASH_ERROR, -8);
			}else if(arg == -3){
				ACT.errorFinish(CODE.RESULT_FLASH_ERROR, -8);
			}else if(arg == -4){
				ACT.errorFinish(CODE.RESULT_FLASH_ERROR, -17);//自分の放送でセッション不正!?
			}else if(arg == -5){
				ACT.errorFinish(CODE.RESULT_FLASH_ERROR, -18);//終了してたら終わる!?
				ACT.finish();
			}else if(arg == -6){
				ACT.errorFinish(CODE.RESULT_FLASH_ERROR, -42);//自分の放送で予約枠って事はないが一応
			}else if(arg == -7){
				ACT.errorFinish(CODE.RESULT_FLASH_ERROR, -20);//自分の放送でコミュ限ってことはないが一応
			}else if(arg == -8){
				ACT.errorFinish(CODE.RESULT_FLASH_ERROR, -17);
			}else if(arg == -9){
				ACT.errorFinish(CODE.RESULT_FLASH_ERROR, -41);
			}else if(arg == -11){
				ACT.errorFinish(CODE.RESULT_FLASH_ERROR, -46);
			}else if(arg == -100){
				error.showErrorToast();
			}
		}//End of onPost
	}//End of InitFlayPlayer



	//UIスレッドばかりなのでバックグラウンドにできない
			private void init() {
								byte[] bitmaparray = liveInfo.getBitmapArray();
				if (bitmaparray != null) {
					Bitmap bitmap = BitmapFactory.decodeByteArray(bitmaparray, 0,
							bitmaparray.length);
					liveInfo.setThumbnail(bitmap);
				}

				String session = getIntent.getStringExtra("Cookie");
				if(session != null){//コメント取得を開始する
				new StartCommentTable(liveInfo,session ,true).execute();
				}
				// 重なりを考慮してポストエリアとバッファとリストのレイアウト
				if (setting_boolean[2]) {//フォーム上
					postArea = (LinearLayout) parent.findViewById(R.id.postArea_up);
					postET = (EditText) parent
							.findViewById(R.id.postarea_edit_up);
					postET.setFocusable(true);
					postET.clearFocus();
					if(setting_boolean[3]){//イベントリスナでもここを聞いてるけどifの記述上しかたない
						post_184 = (CheckBox)parent.findViewById(R.id.postarea_184_up);
					post_command = (Button)parent.findViewById(R.id.postarea_command_up);
					post_update = (Button)parent.findViewById(R.id.postarea_update_up);
					post_menu = (Button)parent.findViewById(R.id.postarea_menukey_up);
					voiceInput = (ImageButton) parent.findViewById(R.id.postarea_voiceinput_up);
					post_desc = (Button)parent.findViewById(R.id.postarea_desc_up);
					post_cdisp = (Button)parent.findViewById(R.id.postarea_command_up);
					}
					postB = (Button) parent.findViewById(R.id.postarea_commit_up);
					postB.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View view) {
							if(cmd != null){
								final String comment = postET.getText().toString();
								if(comment == null || comment.equals(""))return;
							commentTable.postComment(postET.getText().toString(), cmd);
							}else{
								MyToast.customToastShow(ACT, "コメントの投稿に失敗しました");
							}
							postET.setText("");

						}
					});
				} else {
					postArea = (LinearLayout) parent.findViewById(R.id.postArea_buttom);
					postET = (EditText) parent
							.findViewById(R.id.postarea_edit_down);
					postET.setFocusable(true);
					postET.clearFocus();
					if(setting_boolean[3]){//イベントリスナでもここを聞いてるけどifの記述上しかたない
						post_184 = (CheckBox)parent.findViewById(R.id.postarea_184_down);
						post_command = (Button)parent.findViewById(R.id.postarea_command_down);
						post_update = (Button)parent.findViewById(R.id.postarea_update_down);
						post_desc = (Button)parent.findViewById(R.id.postarea_desc_down);
						post_cdisp = (Button)parent.findViewById(R.id.postarea_command_down);
						voiceInput = (ImageButton) parent
								.findViewById(R.id.postarea_voiceinput_down);
						post_menu = (Button)parent.findViewById(R.id.postarea_menukey_down);
					}
					postB = (Button) parent.findViewById(R.id.postarea_commit_down);
					postB.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View view) {
							if(cmd != null){
								final String comment = postET.getText().toString();
								if(comment == null || comment.equals(""))return;
								commentTable.postComment(postET.getText().toString(), cmd);
							}else{
								MyToast.customToastShow(ACT, "コメントの投稿に失敗しました");
							}
							postET.setText("");
						}
					});
				}
				if (setting_boolean[3]) {
					setFormListeners();
				}
				postArea.setVisibility(View.GONE);//デフォルト非表示
				//バッファアイコン初期化
				bufferMark = (LinearLayout) parent.findViewById(R.id.buffering_area);
				((ViewGroup) parent).removeView(bufferMark);

				//リストの初期化
				rootFrame = (ViewGroup) parent.findViewById(R.id.list_parent_liner)
						.getParent();
				//list_bottomがマイナスだったらupLayoutでヘッダを下に配置
				//リストをブランクで上に調整
				//リスト左上を示す位置(firstcurrentY)は変えずにヘッダーを描画
				//アドする順も大事
				//リストのパッディングもisUpかどうかで変わる
				headerBlank = getHeaderBlank();
				headerview = getHeader();
				firstBlueHeader = getBlueHeader();
				secondBlueHeader = getSimpleBlueHeader();
				listview = getList();
				//リストの最新のコメントが画面を超える場合、最新のコメントを画面範囲内にするのがどうしてもできなかった
				/*
				 * if(viewHeight <= firstcurrentY+list_bottom){//下がはみだす
					list_bottom = viewHeight-firstcurrentY-cellHeight;
				}
				list_bottom -= list_bottom%cellHeight;
				とかやってもフォントサイズが小さい時に何故かできない
				 */
				if(list_bottom<0){//方向上
					isUplayout = true;
					listBlank = getListBlank_Up();
					rootFrame.addView(listBlank, new LinearLayout.LayoutParams(-1, -2));
					listBlank.addView(listview, new LinearLayout.LayoutParams(list_width, -list_bottom));
				}else{//方向下
					isUplayout = false;
				listBlank = getListBlank_Down();
				listview.setPadding(0, cellHeight, 0,0);
				rootFrame.addView(listBlank, new LinearLayout.LayoutParams(-1, -2));
				listBlank.addView(listview, new LinearLayout.LayoutParams(list_width, list_bottom));
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
						if (setting_byte[31] == 1) {
							new OperationDialog(ACT, setting_boolean[13], setting_byte[31])
									.showSelf();
						} else {// ここで呼ばれる場合は、プレイヤーのみ
							new OperationDialog(ACT, setting_boolean[13], (byte) 2).showSelf();
						}
					}
				});
				post_desc.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View view) {
						showLiveDescription();
					}
				});
				post_cdisp.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View view) {
						if(listview.getVisibility() != View.VISIBLE){
							listview.setVisibility(View.VISIBLE);
						}else{
							listview.setVisibility(View.GONE);
						}
					}
				});
				voiceInput.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						//プレイヤーとか無いのでフルスクリーンとかない
						AudioManager audio = (AudioManager) getSystemService(AUDIO_SERVICE);
						recognizeValue = audio
								.getStreamVolume(AudioManager.STREAM_MUSIC);
						audio.setStreamVolume(AudioManager.STREAM_MUSIC, 0,
								0);
						try {
							// 音声をテキストにしてeditTexにセットする
							Intent intent = new Intent(
									RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
							intent.putExtra(
									RecognizerIntent.EXTRA_LANGUAGE_MODEL,
									RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
							intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
									"音声入力");
							intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
							// インテント発行
							startActivityForResult(intent,
									CODE.RESULT_RECOGNIZE_SPEECH);
						} catch (ActivityNotFoundException e) {
							// このインテントに応答できるアクティビティがインストールされていない場合
							MyToast.customToastShow(ACT, "音声認識に対応していないようです");
						} catch (Exception e) {
							e.printStackTrace();
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
			class ExCommentListAdapter extends CommentListAdapter{
				final private byte listSize = 7;
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
				public void insert(String[] row,int index){
					super.insert(row, index);
					if(isScrollEnd){
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

			private static class ViewHolder {
				public byte id_index;
				TextView[] columnTvs = new TextView[7];
			}

			class StartCommentTable extends AsyncTask<Void, Void, Void> {
				private LiveInfo tempInfo;
				private ErrorCode threadError;
				private String session;

				StartCommentTable(LiveInfo lv, String session,boolean isVisibleTable) {
					this.tempInfo = lv;
					this.session = session;
					threadError = ((NLiveRoid)getApplicationContext()).getError();
				}

				@Override
				protected Void doInBackground(Void... arg0) {
					if(commentTable !=null){
						if(autoUpdateTask != null && autoUpdateTask.getStatus() != AsyncTask.Status.FINISHED){
							autoUpdateTask.cancel(true);
						}
						commentTable.closeMainConnection();
					}
					if(setting_boolean == null){
						if(getIntent == null)getIntent = getIntent();
						setting_boolean = getIntent.getBooleanArrayExtra("setting_boolean");
					}
					if(setting_byte == null){
						if(getIntent == null)getIntent = getIntent();
						setting_byte = getIntent.getByteArrayExtra("setting_byte");;
					}
					if(tempInfo.getPort() == null){
						error.setErrorCode(-13);
						return null;
					}
					if(NLiveRoid.apiLevel >= 8){
					commentTable = new CommentTable((byte)0,tempInfo ,ACT, adapter,column_seq, threadError,session,
							setting_byte[33],
							setting_boolean[12],
							setting_byte[33] == 3? setting_byte[28]:setting_byte[27],
									setting_byte[26],setting_byte[36],
									getIntent.getStringExtra("speech_skip_word"),
									setting_byte[29],getIntent.getShortExtra("init_comment_count", (short)20),setting_boolean[19]);
					}else{
					commentTable = new CommentTable((byte)1,tempInfo ,ACT, adapter, column_seq,threadError,session,
							setting_byte[33],setting_boolean[12],
							setting_byte[33] == 3? setting_byte[28]:setting_byte[27],setting_byte[36],
									setting_byte[26],getIntent.getStringExtra("speech_skip_word"),
									setting_byte[29],getIntent.getShortExtra("init_comment_count", (short)20),setting_boolean[19]);
					}
					if(setting_byte[32] > 0){
						autoUpdateTask = new AutoUpdateTask();
						autoUpdateTask.execute();
					}
					return null;
				}

				@Override
				protected void onPostExecute(Void arg) {
					if(threadError != null){//CommentTableでスレッドエラーの場合
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


	class AudioTask extends AsyncTask<Void,Void,Void>{
		@Override
		protected Void doInBackground(Void... arg0) {
		if(audioReceiver != null){
			return null;
		}

	AudioManager audio = (AudioManager)getSystemService(AUDIO_SERVICE);
		if(getIntent == null){
			getIntent = getIntent();
		}
			int mode = audio.getRingerMode();//onCreate後の初期のモード
						if(setting_boolean == null){
						if(getIntent == null)getIntent = getIntent();
						setting_boolean = getIntent.getBooleanArrayExtra("setting_boolean");
						}
						if(setting_byte == null){
							if(getIntent == null)getIntent = getIntent();
							setting_byte = getIntent.getByteArrayExtra("setting_byte");
							if(setting_byte == null){//クラッシュ後にこのクラスに戻る(ノティフィからとか)となる、どうしようもないか
								errorFinish(CODE.RESULT_FLASH_ERROR,-43);
								return null;
							}
						}
			//音量固定設定ならその音量を設定
			if(setting_boolean[5] && setting_byte[25] != -1){
				//マナーモードORサイレントなら音量0にする
				if(mode != AudioManager.RINGER_MODE_NORMAL){
					audio.setStreamVolume(AudioManager.STREAM_MUSIC, 0,AudioManager.FLAG_ALLOW_RINGER_MODES);
					}else{
					audio.setStreamVolume(AudioManager.STREAM_MUSIC,
							(int)setting_byte[25],AudioManager.FLAG_ALLOW_RINGER_MODES);
					}
				//前回マナーで終わっていたら、元の音量も0になっているから設定値から保存
				//戻す音量を保存
				getIntent.putExtra("audiovolume",(byte)setting_byte[25]);
				}else{
				//固定値設定じゃなかったら、今の音量に今の音量を保存(要はaudiovolumeに、固定値設定に関わらず保存される+固定値設定している場合は、モード切替によって固定値に戻される)
				getIntent.putExtra("audiovolume",(byte)audio.getStreamVolume(AudioManager.STREAM_MUSIC));
				}

				//いつonCreateされるかわからないので、ここでサービスが実行されているか
				//調べる必要がある

		 // 音量監視開始
			    if(audioReceiver == null){
		    startService(new Intent(ACT, RingReceiver.class));
			 IntentFilter filter = new IntentFilter();
		        filter.addAction(AudioManager.RINGER_MODE_CHANGED_ACTION);
		        filter.addAction("finish_bcplayer.NLR");
		        filter.addAction("player_reload.NLR");
		        filter.addAction("change_player.NLR");
		        audioReceiver = new RingReceiver();
		        try{
		    registerReceiver(audioReceiver, filter);
		        }catch(Exception e){
		        	e.printStackTrace();
		        }
			    }
			return null;
		}
	}
	 class RingReceiver extends BroadcastReceiver {
		    @Override
		    public void onReceive(Context context, Intent intent) {
		    	if(liveSetting != null){//バックグラウンド設定通りに動作させる
		    		if(!liveSetting.isRingMicEnable()){
		    			stopMic();
		    		}
		    		if(!liveSetting.isRingCamEnable()){
		    			if(handler != null){//起動して直ぐはhandlerもnull
		    				PreviewReader reader = (PreviewReader)handler.getReader();
			    			if(reader != null&&reader.isStartedPreview())reader.stopPreview();
		    			}
		    		}
		    	}

		    	if(intent == null)return;
		    	//※モード変更+音量変更でも呼ばれる
		        if (intent.getAction().equals(AudioManager.RINGER_MODE_CHANGED_ACTION)) {
					if(!setting_boolean[22])return;//マナー中0設定じゃなければ終了
		        	int mode = intent.getIntExtra(AudioManager.EXTRA_RINGER_MODE, -1);
		            if (mode == AudioManager.RINGER_MODE_VIBRATE||mode == AudioManager.RINGER_MODE_SILENT) {
		                // マナーモードになったら0にする
		            	AudioManager audio = (AudioManager) getSystemService(ACT.AUDIO_SERVICE);
						audio.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
		            } else {
		                // マナーモードではなくなったら音量を元に戻す+ボリュームを記憶
		            	AudioManager audio = (AudioManager) getSystemService(ACT.AUDIO_SERVICE);
						audio.setStreamVolume(AudioManager.STREAM_MUSIC, (int)getIntent().getByteExtra("audiovolume",(byte) 0),0);
						getIntent().putExtra("audiovolume",(byte)audio.getStreamVolume(AudioManager.STREAM_MUSIC));
		            }
		        }else if(intent.getAction().equals("finish_bcplayer.NLR")){
		        	ACT.standardFinish();
		        }else if(intent.getAction().equals("player_reload.NLR")){
		        	//ここでプレビューを再起するのか??
		        }else if(intent.getAction().equals("player_config.NLR")){
		        	if(intent.getStringExtra("offtimer_start") != null){
						try{
						getIntent.putExtra("offtimer_start", Long.parseLong(intent.getStringExtra("offtimer_start")));
						setting_byte[37] = intent.getByteExtra("off_timer", (byte) -1);
						}catch(Exception e){
							e.printStackTrace();
							MyToast.customToastShow(ACT, "オフタイマーの処理に失敗しました");
						}
					}else{
        			setSpPlayerOperation(intent.getByteExtra("operation", (byte)-1),intent.getByteExtra("value", (byte)-1));
					}
				}
		    }
	 }
		public SurfaceHolder getCamSurfaceHolder(){
			return camSurface.getHolder();
		}

	 @Override
	 public void onNewIntent(Intent intent){
			if(getIntent == null)getIntent = getIntent();
	 	if(intent != null){
     	if(intent.getBooleanExtra("restart", false)){
     		intent.putExtra("restart", false);
     		 getIntent = intent;//restartじゃない時にこれするとsetting_booleanとかがnullになっちゃう
     		if(setting_byte != null &&setting_byte[31]== 1){
 				//コメ欄クリア
 				if(listview != null){
 				listview.setVisibility(View.GONE);
 				}
 				if(headerview != null){
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
	 * @return ACT
	 */
	public static BCPlayer getBCACT() {
	    return ACT;
	}


	/**
	 * ビューのコンポーネント周り
	 */

	class MultiTouchListener implements OnTouchListener{
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			isScrollEnd = false;//何かしらのタップでfalseにする
			//タッチリスナは普通のヘッダと最初の青ヘッダのみ
			//getX()とかgetY()とかはリスナにセットされたviewに対しての座標なので相対的に使う
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN://1点目 ID 0

				if(enable_moveX || enable_moveY){// ドラッグどちらかが有効な場合、青ヘッダーを上のレイヤーに乗せる

					isFirstMoving = true;
				int y1 = (int) event.getY(0);
				int x1 = (int) event.getX(0);

				Rect rect1 = new Rect();
				headerview.getGlobalVisibleRect(rect1);
				//リスト下限を最後の値で初期化しておく
				//最初にタップして、すぐに指を放したらリストのheightが消えるのを防ぐ
				int top = rect1.top;
				secondcurrentY = top;

				if (enable_moveY) {// uplayotと共通
					firstoffsetY = y1;
					firstcurrentY = rect1.top;
				}
				if (enable_moveX) {
					firstoffsetX = x1;
					firstcurrentX = rect1.left==0? isPortLayt? -(listViewW-rect1.right):-(listViewH-rect1.right):rect1.left;
				}


				firstBlueBlank = getHeaderBlank();

				firstBlueBlank.removeAllViews();
				firstBlueBlank.addView(firstBlueHeader,
						new LinearLayout.LayoutParams(list_width, cellHeight));
				//上のレイヤに加える
				FrameLayout fl1 = ((FrameLayout) parent.findViewById(R.id.layer2));
				fl1.removeAllViews();
				fl1.addView(
						firstBlueBlank, new FrameLayout.LayoutParams(-1, -1));

				listBlank.setVisibility(View.INVISIBLE);
				headerBlank.setVisibility(View.INVISIBLE);
				listview.setVisibility(View.INVISIBLE);
				headerview.setVisibility(View.INVISIBLE);

				// 上に乗せたViewを見えるようにする
				firstBlueHeader.setVisibility(View.VISIBLE);
				firstBlueBlank.setVisibility(View.VISIBLE);
				}
				break;
			case MotionEvent.ACTION_POINTER_2_DOWN://2点目のタップ
				if(isFirstMoving){//2点目 isFirstMovingはtrueであろうが確実性のため
					int y2 = (int) event.getY(1);
					isSecondMoving = true;
					headerview.requestDisallowInterceptTouchEvent(false);
					// 移動対象Viewの現在座標を取得する  yは今の位置、リストのタッチイベントで呼ばれてくる
					Rect rect2 = new Rect();
					listview.getGlobalVisibleRect(rect2);
					if(isUplayout()){
							secondoffsetY = y2;
							secondcurrentY = rect2.top+y2+(-list_bottom);//マイナスなはずなので足す
					}else{
						secondoffsetY = y2;
						secondcurrentY = rect2.top+y2;
					}
					secondBlank = getSimpleBlueHeaderBlank();


					// 2点目の青ヘッダーを上のレイヤーに乗せる
					secondBlank.removeAllViews();
					secondBlank.addView(secondBlueHeader,
							new LinearLayout.LayoutParams(list_width, cellHeight));

					//2点目用の上のレイヤに加える
					FrameLayout fl2 =((FrameLayout) parent.findViewById(R.id.layer3));
					fl2.removeAllViews();
					fl2.addView(
							secondBlank, new FrameLayout.LayoutParams(-1, -1));
				}
				secondBlank.setVisibility(View.VISIBLE);
				secondBlueHeader.setVisibility(View.VISIBLE);
				break;
			case MotionEvent.ACTION_MOVE://1点目2点目共通
				if(isSecondMoving && secondBlueHeader != null){
					int y2 = (int) event.getY(1);
					//xは変える必要なし

					if (enable_moveY) {
						int diffY = secondoffsetY - y2;
						secondcurrentY -= diffY;
						secondoffsetY = y2;
					}

//					preparePositionValue2();//すると下限いっぱいまで広げられなくなっちゃう
					// 上のレイヤーに乗せたViewの描画内容を更新する
					secondBlank.setPadding(firstcurrentX, secondcurrentY, -firstcurrentX, 0);
					secondBlueHeader.layout(firstcurrentX, secondcurrentY, listViewW,
							cellHeight);
					break;
				}else if (isFirstMoving && firstBlueHeader != null) {
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
					firstBlueBlank.setPadding(firstcurrentX, firstcurrentY, -firstcurrentX, 0);
					firstBlueHeader.layout(firstcurrentX, firstcurrentY, listViewW,
							cellHeight);

				}
				break;
			case MotionEvent.ACTION_POINTER_2_UP://2点目のアップ
//				preparePositionValue();
				headerview.requestDisallowInterceptTouchEvent(true);
				if(isSecondMoving){
					// 上のレイヤーから乗せたViewを除去する
					((FrameLayout) parent.findViewById(R.id.layer3))
							.removeView(secondBlank);
					secondBlank.removeView(secondBlueHeader);
					//ボトム位置の確定
					Rect rect2 = new Rect();
					firstBlueHeader.getGlobalVisibleRect(rect2);
					list_bottom = secondcurrentY-rect2.top;

					//組み合わせ的にはdown→down down→up up→up up→down
					if(isUplayout()){
					if(list_bottom < 0){/*何もしない*/}
					else{//レイアウト変更した
							isUplayout = false;
							if(commentTable != null){
								commentTable.manualSort();
							}
					}
					}else{
						if(list_bottom < 0){//レイアウト変更した
							isUplayout = true;
							if(commentTable != null){
								commentTable.manualSort();
							}
						}else{/*何もしない*/}
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
					if(rootFrame == null){//ほとんどならない
						rootFrame = (ViewGroup) parent.findViewById(R.id.list_parent_liner)
						.getParent();
					}
					listview = getList();

					// 元のレイヤーを作り直してadd(アドの順番でorderが決まる)
					if(isUplayout()){
						listBlank = getListBlank_Up();
						listBlank.addView(listview, new FrameLayout.LayoutParams(list_width,
								(-list_bottom)));
						listview.setPadding(0, 0, 0, 0);
						listview.setSelection(0);//強制スクロール末尾

					}else{
					listBlank = getListBlank_Down();
					listBlank.addView(listview, new FrameLayout.LayoutParams(list_width,
							list_bottom+cellHeight));
					listview.setPadding(0, cellHeight, 0, 0);
					listview.setSelection(listview.getCount());//強制スクロール末尾


					}

					headerBlank = getHeaderBlank();
					headerview = getHeader();

					rootFrame.addView(listBlank, new FrameLayout.LayoutParams(-1,
							-2));
					rootFrame.addView(headerBlank, new FrameLayout.LayoutParams(-1,
							-2));
					headerBlank.addView(headerview, new FrameLayout.LayoutParams(list_width,
							-2));
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
					//引き継ぐ値を代入
					if(setting_byte == null){

							if(setting_byte == null){
									if(getIntent == null)getIntent = getIntent();
									setting_byte = getIntent.getByteArrayExtra("setting_byte");
								}
						}
					//この値を保存するので若干保存する値がずれる、
					//タッチイベント起こさなければ、table_intがそのまま保存されるのでずれないのでOKとした
					if(isPortLayt){
						setting_byte[8] = (byte) (firstcurrentX*100/listViewW);
						setting_byte[9] = (byte) (firstcurrentY*100/listViewH);
						setting_byte[10] = (byte) (list_bottom*100/listViewH);
					}else{
							setting_byte[19] = (byte) (firstcurrentX*100/listViewH);
							setting_byte[20] = (byte) (firstcurrentY*100/listViewW);
							setting_byte[21] = (byte) (list_bottom*100/listViewW);
					}

					isFirstMoving = false;
					isSecondMoving = false;//何故かfalseにならない事がある
					if(secondBlank != null){
					if(secondBlueHeader != null){
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


	class SimpleTouchListener implements OnTouchListener{
		@Override
		public boolean onTouch(View v, MotionEvent event) {

			//タッチリスナは普通のヘッダと最初の青ヘッダのみ
			//getX()とかgetY()とかはリスナにセットされたviewに対しての座標なので相対的に使う
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN://1点目 ID 0

				if(enable_moveX || enable_moveY){// ドラッグどちらかが有効な場合、青ヘッダーを上のレイヤーに乗せる

				int y1 = (int) event.getY();
				int x1 = (int) event.getX();
				isFirstMoving = true;
				Rect rect1 = new Rect();
				headerview.getGlobalVisibleRect(rect1);
				//リスト下限を最後の値で初期化しておく
				//最初にタップして、すぐに指を放したらリストのheightが消えるのを防ぐ
				int top = rect1.top;

				if (enable_moveY) {// uplayotと共通
					firstoffsetY = y1;
					firstcurrentY = rect1.top;
				}
				if (enable_moveX) {
					firstoffsetX = x1;
					firstcurrentX = rect1.left==0? isPortLayt? -(listViewW-rect1.right):-(listViewH-rect1.right):rect1.left;
				}


				firstBlueBlank = getHeaderBlank();

				firstBlueBlank.removeAllViews();
				firstBlueBlank.addView(firstBlueHeader,
						new LinearLayout.LayoutParams(list_width, cellHeight));
				//上のレイヤに加える
				FrameLayout fl1 = ((FrameLayout) parent.findViewById(R.id.layer2));
				fl1.removeAllViews();
				fl1.addView(
						firstBlueBlank, new FrameLayout.LayoutParams(-1, -1));

				listBlank.setVisibility(View.INVISIBLE);
				headerBlank.setVisibility(View.INVISIBLE);
				listview.setVisibility(View.INVISIBLE);
				headerview.setVisibility(View.INVISIBLE);

				// 上に乗せたViewを見えるようにする
				firstBlueHeader.setVisibility(View.VISIBLE);
				firstBlueBlank.setVisibility(View.VISIBLE);
				}
				break;
			case MotionEvent.ACTION_MOVE://1点目2点目共通
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
					firstBlueBlank.setPadding(firstcurrentX, firstcurrentY, -firstcurrentX, 0);
					firstBlueHeader.layout(firstcurrentX, firstcurrentY, listViewW,
							cellHeight);

				}
				break;
			case MotionEvent.ACTION_UP:
//				preparePositionValue();
				if (isFirstMoving) {
					// 上のレイヤーから乗せたViewを除去する
					((FrameLayout) parent.findViewById(R.id.layer2))
							.removeView(firstBlueBlank);
					firstBlueBlank.removeView(firstBlueHeader);
					if(rootFrame == null){//ほとんどならない
						rootFrame = (ViewGroup) parent.findViewById(R.id.list_parent_liner)
						.getParent();
					}
					listview = getList();

					// 元のレイヤーを作り直してadd(アドの順番でorderが決まる)
					if(isUplayout()){
						listBlank = getListBlank_Up();
						listBlank.addView(listview, new FrameLayout.LayoutParams(list_width,
								(-list_bottom)));
						listview.setPadding(0, 0, 0, 0);
						listview.setSelection(0);//強制スクロール末尾

					}else{
					listBlank = getListBlank_Down();
					listBlank.addView(listview, new FrameLayout.LayoutParams(-1,
							list_bottom+cellHeight));
					listview.setPadding(0, cellHeight, 0, 0);
					listview.setSelection(listview.getCount());//強制スクロール末尾


					}

					headerBlank = getHeaderBlank();
					headerview = getHeader();

					rootFrame.addView(listBlank, new FrameLayout.LayoutParams(-1,
							-2));
					rootFrame.addView(headerBlank, new FrameLayout.LayoutParams(-1,
							-2));
					headerBlank.addView(headerview, new FrameLayout.LayoutParams(list_width,
							-2));
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
					//引き継ぐ値を代入
					if(setting_byte == null){
							//テーブルに必要な値が全て消えていた場合 念には念

							if(setting_byte == null){
									if(getIntent == null)getIntent = getIntent();
									setting_byte = getIntent.getByteArrayExtra("setting_byte");;
								}

						}
					//この値を保存するので若干保存する値がずれる、
					//タッチイベント起こさなければ、table_intarrayがそのまま保存されるのでずれないのでOKとした
							if(isPortLayt){
								setting_byte[8] = (byte) (firstcurrentX*100/listViewW);
								setting_byte[9] = (byte) (firstcurrentY*100/listViewH);
								setting_byte[10] = (byte) (list_bottom*100/listViewH);
							}else{
									setting_byte[19] = (byte) (firstcurrentX*100/listViewH);
									setting_byte[20] = (byte) (firstcurrentY*100/listViewW);
									setting_byte[21] = (byte) (list_bottom*100/listViewW);
							}

					isFirstMoving = false;
					isSecondMoving = false;//何故かfalseにならない事がある
					if(secondBlank != null){
					if(secondBlueHeader != null){
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
	}//End of SimpleTouchListener

	/**
	 * isScrollEndを取得する
	 * CommentTableからのみ呼び出される
	 * @return
	 */
	@Override
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
		if(isUplayout()){
			if(firstVisible == 0){
				isScrollEnd = true;
			}else{
				isScrollEnd = false;
			}
		}else{
			if (totalItemCount == firstVisible + visibleItemCount) {// スクロール入ってた
//				Log.d("log", "ADITIONAL TRUE");
				isScrollEnd = true;
				if(commentTable != null){
					commentTable.scrollEnded();
				}
			} else if (listview.getCount() > 20) {// スクロールはずれた
													// 初回バッファでスクロールできなくなるので応急修正
//				Log.d("log", "ADITIONAL FALSE");
				isScrollEnd = false;
			}
		}
		if(bufferMark == null){
			bufferMark = (LinearLayout) parent.findViewById(R.id.buffering_area);
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
 * @return
 */
private LinearLayout getHeader() {
	if(setting_boolean == null){
		if(getIntent == null)getIntent = getIntent();
		setting_boolean = getIntent.getBooleanArrayExtra("setting_boolean");
	}
	if(setting_boolean[1]){
		TableLayout row = (TableLayout) inflater.inflate(R.layout.newline_header,
				null);
		NewLineHeader num = (NewLineHeader)row.findViewById(R.id.nseq5);
		num.setGravity(Gravity.LEFT);



		if(NLiveRoid.apiLevel >= 8){
			row.setOnTouchListener(new MultiTouchListener());
		}else{
			row.setOnTouchListener(new SimpleTouchListener());
		}
			row.setBackgroundColor(Color.parseColor("#F2F2F2"));
			return row;
	}else{
		LinearLayout row = (LinearLayout) inflater.inflate(row_resource_id,
				null);
	CommentTableCell num = (CommentTableCell)row.findViewById(R.id.seq5);
	num.setGravity(Gravity.LEFT);
	if(NLiveRoid.apiLevel >= 8){
		row.setOnTouchListener(new MultiTouchListener());
	}else{
		row.setOnTouchListener(new SimpleTouchListener());
	}
		row.setBackgroundColor(Color.parseColor("#F2F2F2"));
		return row;
	}
}
/**
 * DownのリストBlank
 * @return
 */
private LinearLayout getListBlank_Down() {
	LinearLayout blank = new LinearLayout(this);
	blank.setPadding(firstcurrentX, firstcurrentY, -firstcurrentX, 0);
	return blank;
}
/**
 * UpのリストBlank
 * @return
 */
private LinearLayout getListBlank_Up() {
	LinearLayout blank = new LinearLayout(this);
	//ヘッダー位置より上にレイアウト=topのパッディングを減らす=マイナスなはずだから足す
	blank.setPadding(firstcurrentX, firstcurrentY+list_bottom, -firstcurrentX, 0);
	return blank;
}
/**
 * DownのヘッダーBlank
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

	LinearLayout row = (LinearLayout) inflater.inflate(R.layout.colorheader,
			null);
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
private void preparePositionValue() {//ドラッグするリミットを設ける　何故かビッタリ合わない
	if(isPortLayt){
		//縦画面時X軸に対するリミットf
		if (firstcurrentX < -(listViewW - 10)) {
			firstcurrentX = -(listViewW - 10);
		} else if (firstcurrentX > listViewW - 10) {
			firstcurrentX = listViewW - 10;
		}
		//縦画面時Y軸に対するリミット
		if (firstcurrentY > listViewH - cellHeight/2*5) {
			firstcurrentY = listViewH - cellHeight/2*5;
		}

	}else{
		//横画面X軸に対するリミット
		if (firstcurrentX < -(listViewH - 10)) {
			firstcurrentX = -(listViewH - 10);
		} else if (firstcurrentX > listViewH - 10) {
			firstcurrentX = listViewH - 10;
		}
		//横画面Y軸に対するリミット
		if (firstcurrentY > listViewW - cellHeight/2*5) {
			firstcurrentY = listViewW - cellHeight/2*5;
		}
	}
}





/**
 * RTMPアドレス，ticket , token ,end_time取得 liveinfoを更新
 * @author Owner
 *
 */
class PublishParse extends AsyncTask<Void, Void, Void> {
	@Override
	protected Void doInBackground(Void... started) {
		return publishParse();
	}
	protected void onPostExecute(Void arg) {
			if (error != null&&error.getErrorCode() == 0) {
				//表示を更新する
				Log.d("NLR","PublishParsed ---- "+ liveInfo.getEndTime());
				if(remainTask != null)remainTask.culcTime(liveInfo.getEndTime());
			}
	}
}
/*
 * ExtendLiveTaskでも同期的に使うのでメソッド化
 */
private Void publishParse(){
	int tryCount = 0;
	String beforeEndTime = liveInfo.getEndTime();
	while(true){
	try{
		Log.d("NLR","LL END TIME -----" + liveInfo.getEndTime());
		Thread.sleep(10000);//end_timeが更新されるまで10秒待つ
	}catch(InterruptedException e){
		e.printStackTrace();
		break;
	}
	if (liveInfo == null || liveInfo.getLiveID() == null
			|| liveInfo.getLiveID().equals("")) {
		error.setErrorCode(-8);
		return null;
	}
	String sessionid = getIntent.getStringExtra("Cookie");
	if (sessionid == null || sessionid.equals("")) {
		error.setErrorCode(-39);
		return null;
	}
	byte[] publishSource = Request.doGetToByteArray(URLEnum.PUBLISHAPI
			+ liveInfo.getLiveID(), sessionid, error);

	if (publishSource == null) {
		error.setErrorCode(-39);
		return null;
	}
	XMLparser.getTokenInfoFromAPIByteArray(publishSource, liveInfo);
	if (liveInfo.getToken() == null) {
		error.setErrorCode(-39);
		return null;
		} else if(beforeEndTime == null||beforeEndTime.equals(liveInfo.getEndTime())){//更新されていなければ、もう一度だけやり直す
			if(tryCount > 0){
				error.setErrorCode(-39);
				return null;
			}
			tryCount++;
			continue;
		}else{
		return null;
		}
	}
	return null;
}
/**
 * 残り時間を更新する
 *
 */

class RemainTimeUpdate extends TimerTask{
	private long remainTime;
	public void culcTime(String end_time){
		Log.d("NLR","END time " + end_time + " Current " + System.currentTimeMillis()/1000);
		if(end_time == null)return;
		long end = Long.parseLong(end_time);
		remainTime = end - System.currentTimeMillis()/1000;
	}
	@Override
	public void run() {
		new AsyncTask<Void,Void,Void>(){
			@Override
			protected Void doInBackground(Void... params) {
				return null;
			}
			@Override
			protected void onPostExecute(Void arg){
				long remain = --remainTime>0? remainTime:0;
				if(remain_time != null)remain_time.setText(String.format("残り時間 %02d:%02d", remain/60,remain%60));
			}
		}.execute();
	}
}
/**
 * 来場コメ数を更新する(放送開始から)
 */
class HeatBeatLoop extends AsyncTask<Void,Void,Void>{
	String[] val;
	@Override
	protected Void doInBackground(Void... params) {
		while(true){
		String sessionid = ACT.getIntent().getStringExtra("Cookie");
		if(sessionid == null||sessionid.equals("")){
			error.setErrorCode(-8);
			return null;
		}
		byte[] source = Request.doGetToByteArray(String.format(URLEnum.HEATBEAT,liveInfo.getLiveID()), sessionid, error);
		if(source != null){
		val = XMLparser.getHeatBeat(source);
		}
		if(val != null)new UpdateCounts().execute();
		Log.d("BCPlayer","HeatBeat");
			try {
				Thread.sleep(60000);
			} catch (InterruptedException e) {
				e.printStackTrace();
				break;
			}//失敗に関わらず1分待つ
		}
		return null;
	}
	class UpdateCounts extends AsyncTask<Void,Void,Void>{
		@Override
		protected Void doInBackground(Void... params) {
			return null;
		}
		@Override
		protected void onPostExecute(Void arg){
			if(val == null||val.length<2)return;
			if(comment_count != null)comment_count.setText("コメント "+val[0]);
			if(view_count != null)view_count.setText("来場 "+val[1]);
		}

	}

}
	/**
	 * 配信を開始する
	 */
	@Override
	public void startLive(){
			new StartLiveTask().execute();
	}
	public class StartLiveTask extends AsyncTask<Void,Void,Void>{
		@Override
		protected Void doInBackground(Void... arg0) {
			try {
				String sessionid = ACT.getIntent().getStringExtra("Cookie");
				if(sessionid == null||sessionid.equals("")){
					error.setErrorCode(-8);
					return null;
				}
	URL url = new URL(String.format(URLEnum.CONFIGUREAPIVALUE1,liveInfo.getLiveID(),liveInfo.getToken()));
	HttpURLConnection con = (HttpURLConnection)url.openConnection();
	con.setRequestProperty("Cookie", sessionid);
	if(con.getResponseCode() != 200){
					error.setErrorCode(-8);
		return null;
	}

	URL url2 = new URL(String.format(URLEnum.CONFIGURE_EXCLUDEVALUE1,liveInfo.getLiveID(),liveInfo.getToken()));
	HttpURLConnection con2 = (HttpURLConnection)url2.openConnection();
	con2.setRequestProperty("Cookie", sessionid);
	if(con2.getResponseCode() != 200){
					error.setErrorCode(-8);
		return null;
	}
	liveInfo.setLiveStarted(true);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}
		@Override
		protected void onPostExecute(Void arg){
			if(error != null && error.getErrorCode() != 0){
				error.showErrorToast();
			}else if(error == null){
				MyToast.customToastShow(ACT, "放送開始の通信に失敗しました");
			}else{
				new PublishParse().execute();
			}
		}
	}

	/**
	 * テスト配信開始+終了
	 *
	 */
	@Override
	public void extendTestLive(){
		new ExtendLiveTask().execute();
	}
	class ExtendLiveTask extends AsyncTask<Void,Void,Void>{

		@Override
		protected Void doInBackground(Void... params) {
			try {
				String sessionid = ACT.getIntent().getStringExtra("Cookie");
				if(sessionid == null||sessionid.equals("")){
					error.setErrorCode(-8);
					return null;
				}
				if(liveInfo.getToken() == null){
					publishParse();
					if(error.getErrorCode() == 0){
						return null;
					}
				}

				URL url = new URL(String.format(URLEnum.CONFIGUREAPIEXTEND,liveInfo.getLiveID(),liveInfo.getToken()));
	HttpURLConnection con = (HttpURLConnection)url.openConnection();
	con.setRequestProperty("Cookie", sessionid);
	if(con.getResponseCode() != 200){//isがnullな件
		error.setErrorCode(-8);
		return null;
	}

			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void arg){
			if(error != null && error.getErrorCode() != 0){
				error.showErrorToast();
			}else if(error == null){
				MyToast.customToastShow(ACT, "テスト延長時の通信に失敗しました");
			}else{
				new PublishParse().execute();
			}
		}

	}
	/**
	 * 配信を終了する
	 *
	 */
	@Override
	public void endLive(){
		if(commentTable == null){
			MyToast.customToastShow(this, "放送終了に失敗した可能性があります\n(/disconnectコマンドが投稿できませんでした)");
		}else{
			if(cmd == null){
				cmd = new CommandMapping(true);
			}
			commentTable.postOwnerComment("/disconnect",cmd);
		}
			new EndLiveTask().execute();
	}

	public class EndLiveTask extends AsyncTask<Void,Void,Void>{

		@Override
		protected Void doInBackground(Void... params) {
			try {
				String sessionid = ACT.getIntent().getStringExtra("Cookie");
				if(sessionid == null||sessionid.equals("")){
					error.setErrorCode(-8);
					return null;
				}
				URL url = new URL(URLEnum.CONFIGURE_ENDLIVE+liveInfo.getLiveID()+"?token="+liveInfo.getToken()+"&key=end%5Fnow&version=2");
	HttpURLConnection con = (HttpURLConnection)url.openConnection();
	con.setRequestProperty("Cookie", sessionid);
//	Log.d("log","URL ------- " + url.toString());
	if(con.getResponseCode() != 200){//isがnullな件
		error.setErrorCode(-8);
		return null;
	}

			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}
		@Override
		protected void onPostExecute(Void arg){
			if(error != null && error.getErrorCode() != 0){
				error.showErrorToast();
			}else if(error == null){
				MyToast.customToastShow(ACT, "放送終了時の通信に失敗しました");
			}
		}

	}


	public void setOrientation(int flug){
		Log.d("NLiveRoid","set orientation flashplayer " + flug);
		switch(flug){
		case 0:
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			if(setting_byte == null){
					if(getIntent == null)getIntent = getIntent();
					setting_byte = getIntent.getByteArrayExtra("setting_byte");
				}
			setting_byte[24] = 0;
			getIntent.putExtra("setting_byte", setting_byte);
//			liveSetting.setPortLayt(true);
//			if(rCam != null)rCam.orientation(true);
		break;
		case 1:
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			if(setting_byte == null){
					if(getIntent == null)getIntent = getIntent();
					setting_byte = getIntent.getByteArrayExtra("setting_byte");
				}
			setting_byte[24] = 1;
			getIntent.putExtra("setting_byte", setting_byte);
			if(liveSetting.isPortLayt()){
			liveSetting.setPortLayt(false);
			PreviewReader reader = (PreviewReader)handler.getReader();
			if(reader != null)reader.getParameters().orientation(false);
			}
			break;
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
	    super.onConfigurationChanged(newConfig);
		if(listDialog != null && listDialog.isShowing())listDialog.cancel();
	    if(setting_boolean == null){
			if(getIntent == null)getIntent = getIntent();
			setting_boolean = getIntent.getBooleanArrayExtra("setting_boolean");
			if(setting_boolean == null){//クラッシュ確定
				errorFinish(CODE.RESULT_FLASH_ERROR,-43);
			}
		}
	    if(setting_byte == null){
			if(getIntent == null)getIntent = getIntent();
			setting_byte = getIntent.getByteArrayExtra("setting_byte");
			if(setting_byte == null){//クラッシュ確定
				errorFinish(CODE.RESULT_FLASH_ERROR,-43);
			}
		}
	    //投稿フォームをレイアウトし直すじゃないとはみ出す
	    if(postET != null && postB != null){
	    	if(voiceInput != null){
	    	voiceInput.setLayoutParams(new TableRow.LayoutParams(-2, -2));
	    	}
	    	postET.setLayoutParams(new TableRow.LayoutParams(-1, -2));
	    	postB.setLayoutParams(new TableRow.LayoutParams(-2, -2));
	    }
	    if(tweetDialog != null && tweetDialog.isShowing())tweetDialog.onContentChanged();


	    if(setting_byte[31] == 1){
	  //viewWidthは必ず短い方
	    if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			isPortLayt = false;
			firstcurrentX = (int) ((double)(listViewH*setting_byte[19]*0.01D));
			firstcurrentY = (int) ((double)(listViewW*setting_byte[20]*0.01D));
			list_bottom = (int) ((double)(listViewW*setting_byte[21]*0.01D));
			list_width = (int) ((double) (listViewH * setting_byte[39] * 0.01D));
			cellHeight = (int) (listViewH*(setting_byte[18]*0.01D));
	    }else{
			isPortLayt = true;
			firstcurrentX = (int) ((double)(listViewW*setting_byte[8]*0.01D));
			firstcurrentY = (int) ((double)(listViewH*setting_byte[9]*0.01D));
			list_bottom = (int) ((double)(listViewH*setting_byte[10]*0.01D));
			list_width = (int) ((double) (listViewW * setting_byte[38] * 0.01D));
			cellHeight = (int) (listViewH*(setting_byte[7]*0.01D));
	    }
	    //縦横で方向が変わる場合がある
	    if(list_bottom < 0){
	    	isUplayout = true;
	    }else{
	    	isUplayout = false;
	    }
//	    preparePositionValue();
	    if(listview == null){
	    	listview = getList();
	    }
			listview.setAdapter(adapter);

	    //親(Blank系)のパディングをやり直してアドし直す
    	if(isUplayout()){
    		if(listBlank == null){
    			listBlank = getListBlank_Up();
    		}
			listBlank.setPadding(firstcurrentX, firstcurrentY+list_bottom, -firstcurrentX, 0);
			listBlank.removeView(listview);
			listview.setPadding(0, 0, 0,0);
			listBlank.addView(listview, new LinearLayout.LayoutParams(list_width, -list_bottom));
				listview.setSelection(0);
    	}else{
    		if(listBlank == null){
    			listBlank = getListBlank_Down();
    		}
			listBlank.setPadding(firstcurrentX, firstcurrentY, -firstcurrentX, 0);
			listview.setPadding(0, cellHeight, 0,0);
			listBlank.removeView(listview);
			listBlank.addView(listview, new LinearLayout.LayoutParams(list_width, list_bottom));
			listview.setSelection(listview.getCount());
    	}
    	if(headerBlank != null){
    	headerBlank.setPadding(firstcurrentX, firstcurrentY, -firstcurrentX, 0);
		headerBlank.removeView(headerview);
		headerview =getHeader();
		headerBlank.addView(headerview,new LinearLayout.LayoutParams(list_width,cellHeight));
		firstBlueHeader = getBlueHeader();//テキストサイズをやり直した物を作っておく
    	}
	    }
    	if(commentTable != null){
    		commentTable.manualSort();
    	}
	}

	@Override
	public void onResume(){
		//いかがなものかと思うけど、インテントブロードキャスト
		 //インテントブロードキャスト
		NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		notificationManager.cancelAll();
		super.onResume();
	}

	@Override
	public void onPause(){

//		if (commentTable != null) {
//			if(autoUpdateTask != null && autoUpdateTask.getStatus() != AsyncTask.Status.FINISHED){
//				autoUpdateTask.cancel(true);
//			}
//			commentTable.killSpeech();
//			commentTable.closeMainConnection();
//			commentTable.closeLogConnection();
//		}
			if(keepActivity){
				onStart();
			}
		super.onPause();
	}

	@Override
	public void onStop(){
		if(keepActivity){
			onStart();
		}
		super.onStop();
	}

	@Override
	public void onDestroy(){

		if (commentTable != null) {
			if(autoUpdateTask != null && autoUpdateTask.getStatus() != AsyncTask.Status.FINISHED){
				autoUpdateTask.cancel(true);
			}
			commentTable.killSpeech();
			commentTable.closeMainConnection();
			commentTable.closeLogConnection();
		}
		PreviewReader reader = (PreviewReader)handler.getReader();
		if(reader != null )reader.stopPreview();
		if(reader != null )reader.releaseCamera();
		if(reader != null )reader.stopMic();
		if(heatbeat != null && heatbeat.getStatus() != AsyncTask.Status.FINISHED)heatbeat.cancel(true);
		if(remainTimer != null)remainTimer.cancel();

	  if(audioReceiver != null){
		  try{
		  unregisterReceiver(audioReceiver); // 音量監視登録解除
		  }catch(IllegalArgumentException e){
			  //早い操作でレジスタできてなかった場合に起こる
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
			Process.killProcess(Process.myPid());
			return;
		}
	super.onDestroy();
	}

	public void standardFinish(){
		if (commentTable != null) {
			if(autoUpdateTask != null && autoUpdateTask.getStatus() != AsyncTask.Status.FINISHED){
				autoUpdateTask.cancel(true);
			}
			commentTable.killSpeech();
			commentTable.closeMainConnection();
			commentTable.closeLogConnection();
		}
		//配信設定の保存
		Intent data = new Intent();
		data.setAction("return_f.NLR");
		try{
			if(setting_boolean == null){
				if(getIntent == null)getIntent = getIntent();
				setting_boolean = getIntent.getBooleanArrayExtra("setting_boolean");
			}
			if(setting_byte == null){
					if(getIntent == null)getIntent = getIntent();
					setting_byte = getIntent.getByteArrayExtra("setting_byte");
				}
			data.putExtra("setting_byte",setting_byte);
			data.putExtra("setting_boolean", setting_boolean);
			data.putExtra("init_comment_count", getIntent.getShortExtra("init_comment_count", (short)20));
			data.putExtra("cmd", cmd);
			data.putExtra("column_seq", column_seq);
			//コンテキストが既に無い事を考え、LiveInfoを渡す
			data.putExtra("LiveInfo", liveInfo);
			//android.hardware.Camera.Sizeがシリアライズしてブロキャスできないのでパラメタ化する
			int[] params = new int[2];
			//isUsecam 10000000 00000000 00000000 00000000
			//isUseMic 01000000 00000000 00000000 00000000
			//backCam  00100000 00000000 00000000 00000000
			//baccMic  00010000 00000000 00000000 00000000
			//ringCam  00001000 00000000 00000000 00000000
			//ringMic  00000100 00000000 00000000 00000000
			//mode     00000000 XXXX0000 00000000 00000000
			//resind   00000000 0000XXXX 00000000 00000000
			//scene    00000000 00000000 XXXX0000 00000000
			//stereo   00000000 00000000 0000X000 00000000 //残り1+7ビット
			int param0 = 0;
			if(liveSetting.isUseCam())param0 = 0x80000000;
			if(liveSetting.isUseMic())param0 = param0 | 0x40000000;
			if(liveSetting.isBackGroundCam())param0 = param0 | 0x20000000;
			if(liveSetting.isBackGroundMic())param0 = param0 | 0x10000000;
			if(liveSetting.isRingCamEnable())param0 = param0 | 0x08000000;
			if(liveSetting.isRingMicEnable())param0 = param0 | 0x04000000;
			param0 = param0 | (liveSetting.getMode()<<20);
			param0 = param0 | (liveSetting.getResolutionIndex()<<16);
			param0 = param0 | (liveSetting.getSceneModeIndex()<<12);
//			if(liveSetting.isStereo())param0 = param0 | 0x00000400;
			params[0] = param0;
			//fps      			XXXXXXXX 00000000 00000000 00000000
			//keyframe_interval 00000000 XXXXXXXX 00000000 00000000
			//volume			00000000 00000000 XXXXXXXX XXXXXXXX
			int param1 = 0;
			param1 = param1 | (liveSetting.getUser_fps()<<24);
			param1 = param1 | (liveSetting.getKeyframe_interval()<<16);
			param1 = param1 | (int)(liveSetting.getVolume()*10);
			params[1] = param1;
			data.putExtra("LiveSettings", params);
			Log.d("BCPlayer","MOVIE FILE PATH" + liveSetting.getFilePath());
			data.putExtra("movie_path", liveSetting.getFilePath());
//			Log.d("BCPlayer","PARAMS " + Integer.toBinaryString(param0));
				data.putExtra("cookie",getIntent.getStringExtra("Cookie"));
						if(setting_byte[31] != 3 &&!setting_boolean[5]&&setting_boolean[22]){//コメントのみでなく、音量固定ではなく、マナー0設定している場合のみ
					 //マナーかサイレントだと、音量を消しているので元に戻す
							  AudioManager audio = (AudioManager) getSystemService(this.AUDIO_SERVICE);
							  int mode = audio.getRingerMode();
							  if(mode == AudioManager.RINGER_MODE_VIBRATE || mode == AudioManager.RINGER_MODE_SILENT){
										data.putExtra("audiovolume", (byte)getIntent.getByteExtra("audiovolume",(byte)-1));
							  }
						}
		//Broadcastする
		data.putExtra("r_code", CODE.RESULT_BROADCAST);
		sendBroadcast(data);
		//super.dispatchKeyEventでDestroyがすぐに呼ばれないことがあるので
		finish();

		}catch(IllegalStateException e){
			e.printStackTrace();
			MyToast.customToastShow(ACT, "設定の保存に失敗しました。");
			finish();
		}catch(NullPointerException e){
			MyToast.customToastShow(ACT, "設定の保存に失敗しました。");
			finish();
		}
	}
	public void errorFinish(int resultCode,int errorCode){
		if (commentTable != null) {
			if(autoUpdateTask != null && autoUpdateTask.getStatus() != AsyncTask.Status.FINISHED){
				autoUpdateTask.cancel(true);
			}
			commentTable.killSpeech();
			commentTable.closeMainConnection();
			commentTable.closeLogConnection();
		}
		Intent data = new Intent();
		data.setAction("return_f.NLR");
		try{
			if(setting_boolean == null){
				if(getIntent == null)getIntent = getIntent();
				setting_boolean = getIntent.getBooleanArrayExtra("setting_boolean");
			}
			if(setting_byte == null){
					if(getIntent == null)getIntent = getIntent();
					setting_byte = getIntent.getByteArrayExtra("setting_byte");
				}
			data.putExtra("setting_byte",setting_byte);
			data.putExtra("setting_boolean", setting_boolean);
			data.putExtra("cookie","");
			if(setting_byte[31] != 3 &&!setting_boolean[5] &&setting_boolean[22]){
		 //マナーかサイレントだと、音量を消しているので元に戻す
		  AudioManager audio = (AudioManager) getSystemService(this.AUDIO_SERVICE);
			  int mode = audio.getRingerMode();
			  if(mode == AudioManager.RINGER_MODE_VIBRATE || mode == AudioManager.RINGER_MODE_SILENT){
						data.putExtra("audiovolume", (byte)getIntent.getByteExtra("audiovolume",(byte)-1));
			  }
			}
		//Broadcastする
			data.putExtra("flash_error", errorCode);
		data.putExtra("r_code", resultCode);
		sendBroadcast(data);
		//super.dispatchKeyEventでDestroyがすぐに呼ばれないことがあるので
		finish();

		}catch(IllegalStateException e){
			e.printStackTrace();
			if(ACT != null)MyToast.customToastShow(ACT, "プレイヤーエラーしました CODE:0");
			finish();//errorFinishは無限ループしちゃう
		}catch(NullPointerException e){
			if(ACT != null)MyToast.customToastShow(ACT, "プレイヤーエラーしました CODE:1");
			finish();
		}
	}
	  @Override
	  public void finish(){
		  keepActivity = false;
		  super.finish();
		  System.gc();
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
			if(adapter.getCount() < adapterInfo.position){//立見などをタップした時におかしくなるArrayList.throwIndexOutOfBoundsException
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
									}, defaultBgColor,defaultFoColor, tempID,idToHandleName.get(tempID) == null? tempID:idToHandleName.get(tempID),true).show();
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
							new ContextDialog(ACT, row,idToHandleName.get(row[1])== null? row[1]:idToHandleName.get(row[1]),(int) (previewWidth),defaultBgColor,defaultFoColor).showSelf();
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
		  MenuInflater inflater = getMenuInflater();
		  inflater.inflate(R.menu.menu_disp_0_1_3,menu);
	        return super.onPrepareOptionsMenu(menu);
	  }



		@Override
		public boolean onOptionsItemSelected(MenuItem item) {
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
					new OperationDialog(this, setting_boolean[13], (byte) 4)
							.showSelf();

				break;
			case R.id.setting:
				if (cmd == null) {
					if (getIntent == null)
						return false;
					cmd = (CommandMapping) getIntent.getSerializableExtra("cmd");
					if (cmd == null)
						return false;
				}
				if (liveInfo != null && liveInfo.getLiveID() != null) {// 何故かありえる
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
					if ((setting_byte[31] == 1 || setting_byte[31] == 3)
							&& commentTable == null)
						return false;
					// 前面、プレイヤーのみの場合、ここではコメサバ接続を持たないのでinit_modeが必ず2
					if (setting_byte[31] == 0 || setting_byte[31] == 2) {
						new ConfigDialog(this, liveInfo, setting_byte,
								setting_boolean, (byte) 4).showSelf();
					} else {
						new ConfigDialog(this, liveInfo, setting_byte,
								setting_boolean,setting_byte[31]).showSelf();
					}
				} else {
					MyToast.customToastShow(this, "読み込み中です");
				}
				break;
			case R.id.live_descOpen:// ここは、背面か、コメントのみの時しか呼ばれない
				showLiveDescription();
				break;
			case R.id.commentArea_change:// ここは、背面か、コメントのみの時しか呼ばれない
				showPostArea();
				break;
			case R.id.quick:// ここは、背面か、コメントのみの時しか呼ばれない
				if(quickDialog != null && quickDialog.isShowing()){
					quickDialog.cancel();
				}else{
					quickDialog = new QuickDialog(this,setting_byte,setting_boolean[19]);
					quickDialog.showSelf(setting_byte[40],setting_byte[41]);
				}
				break;

			}
			return true;
		}

		@Override
		protected void onSaveInstanceState(Bundle outState) {
		    outState.putString("WORKAROUND_FOR_BUG_19917_KEY", "WORKAROUND_FOR_BUG_19917_VALUE");
//		    super.onSaveInstanceState(outState);
		}
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_DOWN&&event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			if (event.getAction() == KeyEvent.ACTION_UP)return false;
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
							//フォーカスをクリア
							if(postET != null && postET.isFocused()){
								postET.clearFocus();
								return false;
							}
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
									post_desc.setVisibility(View.GONE);
									post_cdisp.setVisibility(View.GONE);
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
						}else{


					//誤操作防止設定なら1度ダイアログ表示
					if(setting_boolean[0]){
						new ExitViewDialog(this).show();
						return true;
					}else{
						standardFinish();
					}
				}
				return true;
			}else if(event.getKeyCode() == KeyEvent.KEYCODE_MENU ){
				int menuAction = (setting_byte[40] & 0xF0) >> 4;
				if(setting_byte[31] != 1)return super.dispatchKeyEvent(event);
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
			}else if(event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP){//カメラズーム
				PreviewReader reader = (PreviewReader)handler.getReader();
				if(event.getAction() == KeyEvent.ACTION_DOWN&&reader != null)reader.getParameters().zoomUp();
				return true;
			}else if(event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN){
				PreviewReader reader = (PreviewReader)handler.getReader();
				if(event.getAction() == KeyEvent.ACTION_DOWN&&reader != null)reader.getParameters().zoomDown();
				return true;
			}else{
			return super.dispatchKeyEvent(event);
			}
	}

	@Override
	public void onUserLeaveHint(){
		if(liveSetting != null){
			if(!liveSetting.isBackGroundMic()){
			stopMic();
			}

			if(!liveSetting.isBackGroundCam()){
				PreviewReader reader = (PreviewReader)handler.getReader();
				if(reader != null&&reader.isStartedPreview())reader.stopPreview();
			}

		}
		if(gate != null && gate.isOpened()){
			gate.close_noanimation();
		}
		 //インテントブロードキャスト
		if(liveInfo != null){
        Intent backIntent  = new Intent();
        backIntent.setAction("bindTop.NLR");
        backIntent.putExtra("playerNumber", 1);
        backIntent.putExtra("pid", Process.myPid());
        backIntent.putExtra("lv", liveInfo.getLiveID());
        backIntent.putExtra("title", liveInfo.getTitle());
        this.getBaseContext().sendBroadcast(backIntent);
		}
	}





	@Override
	public void setFullScreen(boolean isChecked) {
		setSpPlayerOperation((byte)100,isChecked? (byte)1:(byte)0);
	}


	@Override
	public void setSpPlayerOperation(byte operation,byte value) {
		if(setting_boolean == null){
			if(getIntent == null)getIntent = getIntent();
			setting_boolean = getIntent.getBooleanArrayExtra("setting_boolean");
		}
		if(setting_byte == null){
			if(getIntent == null)getIntent = getIntent();
			setting_byte = getIntent.getByteArrayExtra("setting_byte");
		}
		if(operation == -1 && value == -1){
			new ConfigPlayerDialog(this,setting_byte,setting_boolean).showSelf();
			return;
		}


	}


	class setControllPos {
		public void layout(WebView view, String url) {
			Configuration config = getResources().getConfiguration();
				if(setting_boolean == null){
					if(getIntent == null)getIntent = getIntent();
					setting_boolean = getIntent.getBooleanArrayExtra("setting_boolean");
				}
				if(setting_byte == null){
					if(getIntent == null)getIntent = getIntent();
					setting_byte = getIntent.getByteArrayExtra("setting_byte");
				}

				if (config.orientation == Configuration.ORIENTATION_PORTRAIT) {
					if(setting_boolean[4]){
						switch(setting_byte[22]){//スマホ版 縦
						case 0://上
							break;
						case 1://下
							break;
						}
					}else{//PC版 縦
						switch(setting_byte[22]){
						case 0://上
							break;
						case 1://下
							break;
						}
					}

				}else{
					if(setting_boolean[4]){//スマホ版 横

						switch(setting_byte[23]){//1:1にする
						case 0://左
							break;
						case 1://右
							break;
						case 2://全面
							break;
						}
						//縦幅は共通

					}else{//PC版 横
						switch(setting_byte[23]){
						case 0://左
							break;
						case 1://右
							break;
						case 2://切り替えでありえる
							break;
						}
						//縦幅は共通
					}
				}
			// Square(正方形) エミュレータではこの値は返って来ない。
//			if (config.orientation == Configuration.ORIENTATION_SQUARE)

			if(dialog!=null){
				dialog.dismiss();
			}
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode,Intent data){
		//クルーズの時のフラッシュ等からブラウザに行って帰ってきた時はerrorもnull?
//		Log.d("Log","BC REUTURN");
				if(error == null){
					finish();
				}
				//Overから返ってきたら、キーイベントを呼ばない
				if(requestCode == CODE.RESULT_TRANS_LAYER){
					if(data != null){
						if(data.getByteExtra("init_mode", (byte)-1)==1){//透明ACTの放送詳細から

					if( data.getBooleanExtra("error", false)){
						MyToast.customToastShow(this, data.getStringExtra("error_message"));
						return;
					}
					Intent bcService = new Intent();
					bcService.setAction("return_f.NLR");
					bcService.putExtra("r_code",CODE.RESULT_FROM_GATE_FINISH);
					bcService.putExtra("tagword", data.getStringExtra("tagword"));
					this.sendBroadcast(bcService);
					NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
					notificationManager.cancelAll();
					standardFinish();
						}
					}
				}else if(requestCode == CODE.REQUEST_GALALY_PLAYERVIEW&&data != null) {//視聴画面の背景CODEを使いまわす
					Log.d("NLiveRoid","GET_BMP_DATA " + data);
					if(data.getData() != null){
						//エンコードの為のセットをする
						liveSetting.setBmpPath(data.getData());
					}
				}else if (requestCode == CODE.RESULT_RECOGNIZE_SPEECH) {
					// 音声認識から
					// 結果文字列リスト
					AudioManager audio = (AudioManager)getSystemService(AUDIO_SERVICE);
					audio.setStreamVolume(AudioManager.STREAM_MUSIC, recognizeValue,AudioManager.FLAG_ALLOW_RINGER_MODES);
					recognizeValue = 0;

					if (data != null) {
						ArrayList<String> results = data
								.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
						if(results == null || results.size() == 0)this.finish();
						 final String[] candidate = new String[results.size()];
						 for (int i = 0; i< results.size(); i++) {
						 // ここでは、候補がいくつか格納されてくるので結合しています
						 candidate[i] = results.get(i);
						 }
						 //候補をアラート表示
						 new AlertDialog.Builder(this)
							.setItems(candidate,
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog,
												int which) {
											//音声入力
											if(postET != null){
												postET.setText(candidate[which]);
											}
										}
							})
							.create().show();
					}
				}



				if(resultCode == CODE.RESULT_COOKIE){
					cmd = (CommandMapping) data.getSerializableExtra("cmd");
					if(cmd == null){
						//コマンドオブジェクトの初期化
						NLiveRoid app = (NLiveRoid) ACT.getApplicationContext();
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
							if(i==3){
								cmd = new CommandMapping(false);
							}
						}
					}
					if(setting_boolean == null){
						if(getIntent == null)getIntent = getIntent();
						setting_boolean = getIntent.getBooleanArrayExtra("setting_boolean");
					}
					if(setting_byte == null){
								if(getIntent == null)getIntent = getIntent();
								setting_byte = getIntent.getByteArrayExtra("setting_byte");
					}
					if(setting_byte[31]==0&&data != null){//前面時は設定を保存
						boolean[] boolArray = data.getBooleanArrayExtra("setting_boolean");
						byte[] byteArray = data.getByteArrayExtra("setting_byte");
						if(boolArray != null){
							this.setting_boolean = boolArray;
						}
						if(byteArray != null){
							this.setting_byte = byteArray;
						}
					}
				 }else if(resultCode == CODE.RESULT_FROM_GATE_FINISH){
					 //タグ検索する為に終了する
//						 onUserLeaveHint();
//						 Intent topTab = new Intent(this, TopTabs.class);
//							startActivity(topTab);
						 if(data !=null){
							 standardFinish();//普通に終了する
						 }else{
							 errorFinish(CODE.RESULT_FLASH_ERROR,-12);//設定ファイルの読み込み失敗としておく
						 }
				 }else if(resultCode == CODE.RESULT_OVERLAY_ERROR){//OverLayで何かしらの放送取得エラー*このアクティビティは終了しないが、トーストのためにflash_errorとして扱ってもらう
					 if(data == null||error == null){
						 MyToast.customToastShow(ACT, "コメ欄エラー");
						 return;
					 }
					 int errorCode = data.getIntExtra("overlay_error", 0);
					 errorFinish(CODE.RESULT_FLASH_ERROR, errorCode);//FLASHのエラーとして変換する
				 }else if(resultCode == CODE.RESULT_CLOSED){//OverLayで放送終了していた*トーストのためにflash_errorとして扱ってもらう
					 errorFinish(CODE.RESULT_FLASH_ERROR, -18);
				 }else if(resultCode == CODE.RESULT_NOLOGIN){//ログイン失敗でセッションを消す必要がある場合
					 errorFinish(CODE.RESULT_NOLOGIN, -17);
				 }else if (resultCode == CODE.RESULT_REDIRECT) {//リダイレクトから返ってきた
					 Log.d("NLiveRoid","RETURNED REDIRECT");
				 }else if(resultCode == CODE.RESULT_ALL_UPDATE){
						//すべて更新
					 if(data !=null){
				        	allUpdate();
					 }
				 }

	}



	private void storeData(Intent data){
		 if(data !=null){
			 //OverLayが普通の終了だったら前回の値を保存
			 //このクラスはコメ欄を表示しないからそのままこのフィールドに保存するEX同一レイヤにしたら要変更

				 byte[] nullCheckI = data.getByteArrayExtra("setting_byte");
				 if(nullCheckI != null)setting_byte = nullCheckI;
				 boolean[] nullCeckB = data.getBooleanArrayExtra("setting_boolean");
				 if(nullCeckB != null)setting_boolean = nullCeckB;
					getIntent.putExtra("init_comment_count", data.getShortExtra("init_comment_count", (short)20));
			cmd = (CommandMapping) data.getSerializableExtra("cmd");
			if(cmd == null){
				//コマンドオブジェクトの初期化
				NLiveRoid app = (NLiveRoid) this.getApplicationContext();
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
					if(i==3){
						cmd = new CommandMapping(false);
					}
				}
			}
			 }
	}



	@Override
	public void clearAdapter() {
	}

	public void changePlayer(){
			if(setting_boolean == null){
				if(getIntent == null)getIntent = getIntent();
				setting_boolean = getIntent.getBooleanArrayExtra("setting_boolean");
			}
			setting_boolean[4] = !setting_boolean[4];
			getIntent.putExtra("setting_boolean",setting_boolean);

			if(setting_boolean[4]){
			}else{
			}
	}

	@Override
	public void showCommandDialog() {
		if(getIntent == null)getIntent = getIntent();
		new CommandDialog(this, true,getIntent.getStringExtra("Cookie"),liveInfo.getLiveID()).show();
	}

	@Override
	public void showOrientationAlertBuilder() {
		final String[] spinnerAdapter = new String[2];
		spinnerAdapter[0] = "縦固定";
		spinnerAdapter[1] = "横固定";
		if(setting_byte == null){
			if(getIntent == null)getIntent = getIntent();
			setting_byte = getIntent.getByteArrayExtra("setting_byte");
		}//設定値として0,1,2とあるので2だとOutOfBounds
		if(setting_byte[24] >= 1){
			setting_byte[24] = 0;//なのでここで横固定にしても、やり直すと縦固定で始まっちゃうがとりあえず
		}
		new AlertDialog.Builder(this)
		.setTitle(spinnerAdapter[setting_byte[24]])
		.setItems(spinnerAdapter,
				new DialogInterface.OnClickListener() {
					public void onClick(
							DialogInterface dialog,
							int which) {
						//ダイアログ表示時にデフォルト値を入れるsetSelctionで1度呼ばれる
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
	}

	@Override
	public void toScrollEnd(){
	if(listview == null)return;
	isScrollEnd = true;
	if(isUplayout()){
	listview.setSelection(0);
	}else{
	listview.setSelection(listview.getCount());
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
			if(NLiveRoid.isDebugMode)Log.d("NLiveRoid","B readHandleNameData ");
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

			if(NLiveRoid.isDebugMode)Log.d("NLiveRoid","B END writeHandleName ");
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

			if(NLiveRoid.isDebugMode)Log.d("NLiveRoid","B END writeHandleName " + (xml != null? xml.length():"XML NULL"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * ストレージのパスを取得します	 *
	 *
	 */

	private String getStorageFilePath(){
		boolean isStorageAvalable = false;
		boolean isStorageWriteable = false;
		String state = Environment.getExternalStorageState();
		if(state == null){
//			MyToast.customToastShow(this, "SDカードが利用できませんでした\nコテハンは機能できません");
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
//			MyToast.customToastShow(this, "SDカードが利用できませんでした\nコテハンは機能できません");
			return null;
		}


		//sdcard直下に、パッケージ名のフォルダを作りファイルを生成
		String filePath = Environment.getExternalStorageDirectory().toString();

		if(filePath == null){
//			MyToast.customToastShow(this, "SDカードが利用できませんでした\nコテハンは機能できません");
			return null;
		}
		filePath = filePath +  "/NLiveRoid";

		File directory = new File(filePath);//初回起動時で、ディレクトリ自体が無い時はnullじゃなく、書き込み権限も無い状態なので、mkdirする前で、そこをフックしてはいけない
		if(directory.mkdirs()){//すでにあった場合も失敗する
			Log.d("log","SUCCESS MKDIRS ");
		}
		File file = new File(filePath,handleFileName);
		if(!file.exists()){
			try {
				file.createNewFile();
				writeHandleName();//次からの読み込みがエラーしないように空のファイルを作っておく
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return file.getPath();
	}




	@Override
	public void switchPlayer(boolean b) {//常にtrueで呼ばれる
	}
	@Override
	public void openInitCommentPicker() {
		if(setting_byte == null){
			if(setting_byte == null)getIntent = getIntent();
			setting_byte = getIntent.getByteArrayExtra("setting_byte");
		}

		ScrollView sv = new ScrollView(this);
		final NumberPicker np = new NumberPicker(this);
		np.setRange(0, 100);
		np.setCurrent(getIntent.getShortExtra("init_comment_count", (short)20));
		sv.addView(np,-1,-1);
		AlertDialog.Builder npDialog = new AlertDialog.Builder(this);
		npDialog.setView(sv);
		npDialog.setTitle("初期コメ取得件数");
		npDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				getIntent.putExtra("init_comment_count", (short)np.getCurrent());
			}
		});
		npDialog.setNeutralButton("CANCEL", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {

			}
		});
		npDialog.create().show();
	}


	class AutoUpdateTask extends AsyncTask<Void,Void,Void>{
		@Override
		public void onCancelled(){
			super.onCancelled();
			AUTO_FLAG = false;
		}
		@Override
		protected Void doInBackground(Void... params) {
			if(setting_byte == null){
				if(getIntent == null)getIntent = getIntent();
				setting_byte = getIntent.getByteArrayExtra("setting_byte");
			}
			if(setting_boolean == null){
				if(getIntent == null)getIntent = getIntent();
				setting_boolean = getIntent.getBooleanArrayExtra("setting_boolean");
			}

			while(AUTO_FLAG){
				try {
					Thread.sleep(setting_byte[32]*60000);
				} catch (InterruptedException e) {
					e.printStackTrace();
					AUTO_FLAG = false;
					break;
				}catch(IllegalArgumentException e){
					Log.d("NLiveRoid","IllegalArgumentException at BCPlayer AutoUpdateTask");
//					e.printStackTrace();
					AUTO_FLAG = false;
					break;
				}
				updateCommentTable(true);//リスト更新するのでUIスレッドにないと駄目

			}
			return null;
		}
	}
	@Override
	public void updateCommentTable(boolean is_get_between) {
		if(setting_boolean == null){
			if(getIntent == null)getIntent = getIntent();
			setting_boolean = getIntent.getBooleanArrayExtra("setting_boolean");
		}
		setting_boolean[13] = is_get_between;

		if(commentTable != null){
		if (commentTable.isClosed()) {// すでにキャンセルされていたらnewする
			commentTable.createNewCommentTable(0, 0);
		}
			if(adapter != null){
				if(adapter.getCount() > 0){
					String[] last_row = null;
					if(isUplayout()){
					last_row = adapter.getItem(0);
					}else{
					last_row = adapter.getItem(adapter.getCount()-1);
					}

					if(is_get_between){
					commentTable.updateCommentTable(last_row[1], last_row[6], last_row[5],NLiveRoid.apiLevel>=8? 0:1);
					}else{
						commentTable.updateCommentTable(null, null, last_row[5],NLiveRoid.apiLevel>=8? 0:1);
					}
				}else{
					commentTable.updateCommentTable(null, null,null, NLiveRoid.apiLevel>=8? 0:1);
				}
				if (isUplayout()) {//UIスレッドでやんなきゃ落ちる
					if (listview != null)new SetSelection().execute(true);
				} else {
					if (listview != null)new SetSelection().execute(false);
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
			if(liveInfo.getPort() == null){
				error.setErrorCode(-13);
				return;
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
	//UIスレッドでやりたいだけ
	class SetSelection extends AsyncTask<Boolean,Void,Boolean>{
		@Override
		protected Boolean doInBackground(Boolean... arg0) {
			return arg0[0];
		}
		@Override
		protected void onPostExecute(Boolean arg){
			if(arg){
			listview.setSelection(0);
			}else{
			listview.setSelection(listview.getCount());
			}
		}
	}

		@Override
		public boolean isUplayout(){
			return isUplayout;
		}
	@Override
	public void layerChange(int which) {

	}
	@Override
	public void setUpdateInterval(byte interval) {
		if(setting_byte == null){
			if(getIntent == null)getIntent = getIntent();
			setting_byte = getIntent.getByteArrayExtra("setting_byte");
		}
		setting_byte[32] = interval;
		//更新間隔変更処理
	}

	@Override
	public void allUpdate() {
		System.gc();

			if(error == null||liveInfo == null){
				MyToast.customToastShow(ACT, "更新失敗しました\n");
				return;
			}

	       if(listview != null){
				listview.setVisibility(View.GONE);
				}
				if(headerview != null){
				headerview.setVisibility(View.GONE);
				}
			new AllUpdateTask().execute();
	}

	class AllUpdateTask extends AsyncTask<Void,Void,Void>{

		@Override
		protected Void doInBackground(Void... params) {
			if(getIntent == null)getIntent = getIntent();
			//セッションを更新する
			String new_session = Request.getSessionID(error);
			getIntent.putExtra("Cookie", new_session);
			//タブ画面のセッションを更新
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
		if(setting_boolean == null){
			if(getIntent == null)getIntent = getIntent();
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
		if (!isPortLayt)columnNum += 11;// 0からpが並んでいて、横画面なら10上
		return setting_byte[columnNum];
	}

	@Override
	public boolean isPortLayt() {
		return isPortLayt;
	}

	@Override
	public int getViewWidth() {
		return listViewW;
	}

	public int getViewHeight() {
		return listViewH;
	}

	@Override
	public int getCellHeight() {
		return cellHeight;
	}

	@Override
	public void setAtHandleName(String id, String nicName) {

		if(commentTable != null &&!isAtoverwrite&&idToHandleName.containsKey(id)){
			return;
		}
		if(nicName.equals("")){
			return;
		}else{
		nicName = nicName.replace("<|>|/|\"","");
		}
		idToHandleName.put(id, nicName);
		idToBgColor.put(id, -1);
		idToForeColor.put(id, -16777216);
		new WriteHandleName().execute();
		// セットアダプタを呼ぶと、自動的に最初の行まで戻されてしまう
		listview.setAdapter(adapter);
		if(isUplayout()){
			int tempLastRow = 0;
			tempLastRow = listview.getFirstVisiblePosition();
			if (isScrollEnd) {
				listview.setSelection(0);
			} else {
				listview.setSelection(tempLastRow);
			}
		}else{
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
		if(getIntent == null)getIntent = getIntent();
		short initCount = getIntent.getShortExtra("init_comment_count", (short)20);
		if(commentTable != null)new SeetDialog(this,commentTable,adapter,initCount,NLiveRoid.apiLevel >= 8? 0:1).showSelf();
	}

	@Override
	public void getCommentLog(boolean isPremium) {
		//取得量はプラスで渡して判定に使う
				if(adapter != null && adapter.getCount() > 0){
				if(isPremium){
					if(isUplayout()){
						int adapterCount = adapter.getCount()-1;
					commentTable.getCommentLog(200,adapter.getItem(adapterCount)[3],adapter.getItem(adapterCount)[5]);
					}else{
						commentTable.getCommentLog(200,adapter.getItem(0)[3],adapter.getItem(0)[5]);
					}
				}else{
					MyToast.customToastShow(this, "プレアカじゃないのでコメ欄のソートのみを行います");
					commentTable.manualSort();
				}
				}else{
					MyToast.customToastShow(this, "コメントがありません");
				}
	}

	@Override
	public void saveComments() {
		new SaveDialog(this,liveInfo,adapter).showSelf();
	}

	@Override
	public View getBufferMark() {
		return bufferMark;

	}

	@Override
	public void setSpeachSettingValue(byte isEnable, byte speed,byte vol, byte pich) {
		if(setting_boolean == null){
			if(getIntent == null)getIntent = getIntent();
			setting_boolean = getIntent.getBooleanArrayExtra("setting_boolean");
		}
		if(setting_byte == null){
			if(getIntent == null)getIntent = getIntent();
			setting_byte = getIntent.getByteArrayExtra("setting_byte");
		}
		setting_byte[33] = isEnable;
		setting_byte[26] = speed;
		setting_byte[36] = vol;
		if(isEnable == 2||isEnable == 3){
			setting_byte[28] = pich;
		}else{
		setting_byte[27] = pich;
		}
	}


	@Override
	public CommentTable getCommentTable() {
		return commentTable;
	}

	@Override
	public LiveInfo getLiveInfo() {
		  return liveInfo;
	}
	private void showLiveDescription() {
		// コメントのみだったら、そのまま詳細表示
		if (gate != null && gate.isOpened()) {// 開いてたら戻す
			Log.d("Log", "CLOSE GATE IN ITEMSELECT");
			gate.close_noanimation();
			if (listview == null) {// あまり無いと思うが。
				Log.d("NLiveRoid", "O LIST ERROR");
				MyToast.customToastShow(this, "レイアウトをやり直せませんでした\nコ読み込み中です");
				return;
			}
			if (listview != null) {
				listview.setVisibility(View.VISIBLE);
			}
			if (headerview != null) {
				headerview.setVisibility(View.VISIBLE);
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
			if (setting_byte[31] == 3) {// コメントのみ
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
			} else {// Playerのみ,前面時はこのメニューでないので背面時の場合
					// LiveInfoはシリアライズめんどいかもしれないのでstatic参照
				Intent intent = new Intent(this, TransDiscr.class);
				intent.putExtra("init_mode", (byte) 1);
				intent.putExtra("orientation", getRequestedOrientation());
				intent.putExtra("Cookie",
						getIntent.getStringExtra("Cookie"));
				intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				startActivityForResult(intent, CODE.RESULT_TRANS_LAYER);
			}
		}
	}

	@Override
	public void disConnectComment() {
		if (commentTable != null) {
			if(autoUpdateTask != null && autoUpdateTask.getStatus() != AsyncTask.Status.FINISHED){
				autoUpdateTask.cancel(true);
			}
			//closeMainConnectionは他でもいろいろ呼ばれるので分けておく
			commentTable.killSpeech();
			commentTable.closeLogConnection();
			commentTable.closeMainConnection();
		}
	}
	/**
	 * 発言リストを出力
	 * 下方向レイアウトのみ
	 *
	 */
	@Override
	public void createCommentedList(String userid) {
		new UserCommentedTask(userid,new ArrayList<String[]>()).execute();
	}
	class UserCommentedTask extends AsyncTask<Void,Void,Void>{
		private String userid;
		private ArrayList<String[]> commentedRows;
		UserCommentedTask(String userid,ArrayList<String[]> row){
			this.userid = userid;
			this.commentedRows = row;
		}
		 @Override
	        protected void onPreExecute() {
	            super.onPreExecute();
	            dialog = ProgressDialog.show(ACT, "",
	 	               "Loading user commented..", true,true);
	 		 dialog.setContentView(R.layout.progress_dialog_user);
	 		dialog.show();
	        }
		@Override
		protected Void doInBackground(Void... arg0) {

			tempIsScrollEnd = isScrollEnd;
			isScrollEnd = false;
			for(int i = 0; i < adapter.getCount(); i++){
				if(adapter.getItem(i)[1].equals(userid)){
					commentedRows.add(adapter.getItem(i));
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void arg){
			try{//すでにActivityが終了してたらエラーする
				int defaultBgColor = idToBgColor.get(userid) == null ? Color.WHITE
						: idToBgColor.get(userid);
				int defaultFoColor = idToForeColor.get(userid) == null ? Color.BLACK
						: idToForeColor.get(userid);
				new UserCommentedDialog(ACT, userid,idToHandleName.get(userid) == null? userid:idToHandleName.get(userid) ,commentedRows, dialog,defaultBgColor,defaultFoColor)
						.showSelf();
			}catch(BadTokenException e){
				e.printStackTrace();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		 @Override
	        protected void onCancelled() {//画面回転対策
	            if(dialog != null && dialog.isShowing()) {
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
		if(setting_boolean == null){
			if(getIntent == null)getIntent = getIntent();
			setting_boolean = getIntent.getBooleanArrayExtra("setting_boolean");
		}
		setting_boolean[6] = isAt;
	}

	@Override
	public void setAtOverwrite(boolean isAtoverwrite) {
		this.isAtoverwrite = isAtoverwrite;
		if(setting_boolean == null){
			if(getIntent == null)getIntent = getIntent();
			setting_boolean = getIntent.getBooleanArrayExtra("setting_boolean");
		}
		setting_boolean[7] = isAtoverwrite;
	}

	@Override
	public void setHandleName(int bgColor,int foColor, String name) {
		idToHandleName.put(tempID, name);
		idToBgColor.put(tempID, bgColor);
		idToForeColor.put(tempID, foColor);
		new WriteHandleName().execute();
		// セットアダプタを呼ぶと、自動的に最初の行まで戻されてしまう
		listview.setAdapter(adapter);
		if(isUplayout()){
			int tempLastRow = 0;
			tempLastRow = listview.getFirstVisiblePosition();
			if (isScrollEnd) {
				listview.setSelection(0);
			} else {
				listview.setSelection(tempLastRow);
			}
		}else{
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
		if(commentTable != null)commentTable.setAutoUser(isChecked);
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
	public boolean isContainsUserID(String string) {
		return idToHandleName.containsKey(tempID);
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

	public void stopStream(){
    	liveSetting.setStreamStarted(false);
		Log.d("NLR","SETSTREAMSTARTED ---- FALSE");
    	if(handler != null){
    		handler.stopStream();
    	}
    }
	public void startStream() {
		Log.d("BCPlayer","startStream MODE:" + liveSetting.getMode());
		 //設定値を読み込む//flv_on2vp6vp62_mp3.flv   flv_h263_mp3.flv
		switch(liveSetting.getMode()){
   	case 0://カメラマイクモード
   	case 1://スナップはパブリッシュのモードで、RtmpReaderが変わること意外同じ
   		if(red5){
		liveSetting.setHost(red5IP );
		liveSetting.setStreamName(red5StreamID);
		liveSetting.setAppName("oflaDemo");
   		}

			Log.d("NLiveRoid","startPreview_From_startStream");

//   		if(liveSetting.isUseCam()){
//   			int camValue = 0;
//   			if(rCam == null){
//   				rCam = new CameraParams(ACT, liveSetting);
//   			}
//   			if(!rCam.isInited()){
//   				camValue = rCam.init();
//   			}
//   			if(!rCam.isStartedPreview()){
//   				camValue = rCam.startPreview();
//   			}
//   			camValue = rCam.startEncode();
//
//				Log.d("NLiveRoid","camValue:"+camValue);
//   			if(camValue < 0){
//	   			this.runOnUiThread(new Runnable(){
//					@Override
//					public void run() {
//						MyToast.customToastShow(ACT, "カメラの起動に失敗");
//					}
//	   			});
//   			}
//   		}
   		break;
   	case 2://静止画モード
   		if(red5){
		liveSetting.setHost(red5IP );
		liveSetting.setStreamName(red5StreamID);
		liveSetting.setAppName("oflaDemo");
   		}
   		//エンコードがまだな場合があるので、ここではマイクをスタートしない
   		//画像を読み込む
   		if(liveSetting.getBmpPath() == null){
   			Intent intent = new Intent();
			intent.setType("image/*");
			intent.setAction(Intent.ACTION_GET_CONTENT);
			startActivityForResult(intent, CODE.REQUEST_GALALY_PLAYERVIEW);
   		}
   	break;
   	case 3://FLV再生
   		if(red5){
		liveSetting.setHost(red5IP);
		liveSetting.setStreamName(red5StreamID);
		liveSetting.setAppName("oflaDemo");
   		}
	break;
   	}
		Log.d("BC--","PP " + liveSetting.getHost());
		Log.d("BC--","PP " + liveSetting.getStreamName());
		Log.d("BC--","PP " + liveSetting.getAppName());
		Log.d("NLR","SETSTREAMSTARTED ---- TRUE" + handler);
    	if(handler != null){
    		liveSetting.setStreamStarted(true);
    		handler.startPublish();
    	}
	}
	//非同期な事に注意!!
	public void restartStream(){
		new AsyncTask<Void,Void,Integer>(){
			@Override
			protected Integer doInBackground(Void... params) {
				Log.d("BCPlayer","restartStream --- ");

				if(heatbeat != null && heatbeat.getStatus() != AsyncTask.Status.FINISHED){
					heatbeat.cancel(true);
				}
				if(handler != null&&liveSetting.isStreamStarted())handler.stopStream();//ここでストリームをストップしてからNetStream.Unpublish.Successを待つ

					//ネイティブが終わる前にマイクとカメラを完全に止めておく必要がある
				switch(liveSetting.getMode() ){
				case 0:
				{
					PreviewReader reader = (PreviewReader)handler.getReader();
					if(reader.isStartedPreview())reader.stopPreview();
					while(reader.isStartedPreview()){
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							e.printStackTrace();
							break;
						}
					}
					reader.stopMic();
				}
				break;
				case 1:
					break;
				case 2:
				{
					PictureReader reader = (PictureReader)handler.getReader();
					reader.stopMic();
				}
					break;
				case 3:
					break;
				}
				//ストリームを閉じる   NetStream.Unpublish.Successが来る(=ReaderがCLOSEされて処理が終わる)まで待機
				while(liveSetting.isStreamStarted()&&!handler.getUnpublish()){
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
						break;
					}
				}
				if(heatbeat != null && heatbeat.getStatus() == AsyncTask.Status.RUNNING){
					heatbeat.cancel(true);
				}
				heatbeat = new HeatBeatLoop();
				heatbeat.execute();
				startStream();
				return 0;
			}
			@Override
			protected void onPostExecute(Integer arg){
				if(arg == -1){
					MyToast.customToastShow(ACT, "接続がタイムアウトしました");
				}
			}
		}.execute();

	}
	public LiveSettings getLiveSettings() {
		return liveSetting;
	}

	public void changeMode(int which) {//変更後のモードを取って、変える前のモードで場合分け
		Log.d("BCPlayer","changeMode_Called"+liveSetting.getMode() +"   "+which );

		switch(liveSetting.getMode()){
		case 0://元のモードに対して必要な処理をかく
			break;
		case 1:
			break;
		case 2:
			break;
		case 3:
			break;
		}

		liveSetting.setMode(which);
		//プレビューをレイアウトする
				View preview = null;
				switch(which){
				case 0:
				case 1:
					preview = camSurface;
					break;
				case 2:
				case 3:
					if(mImageView == null){
						mImageView = new ImageView(ACT);
						mImageView.setScaleType(ScaleType.FIT_XY);
						mImageView.setBackgroundColor(Color.BLACK);
					}
					preview = mImageView;
					break;
				}
				previewWidth = (int) (webViewW*density/2);//横幅の最大値を決める
				previewHeight = (int) (webViewH*density/8*3);//縦幅の最大値を決める

				switch(which){
				case 0:
				{
					func0.setVisibility(View.VISIBLE);
					func0.setText("フォーカス");
					func1.setVisibility(View.VISIBLE);
					func1.setText("カメラの機能");
					//culcratioをやるためにratioがレイアウト前に計算されている必要がある
					PreviewReader reader;
					if(handler.getReader() == null){
						reader = new PreviewReader(ACT,liveSetting);
						handler.setReader(reader);
					}else{
						reader = (PreviewReader)handler.getReader();
					}
					if(!liveSetting.isUseCam() ){
						//ここでカメラが起動されていない場合はratioは存在しないのでビューに何も表示されなくなるのでデフォルトとしてビューの最大にしておく
						float angleRatio = (float)previewWidth/(float)previewHeight;//デフォルトのビュー
						liveSetting.setViewAngleRatio(angleRatio);
					}else if(!reader.isInited()&&reader.init(null)<0){
								MyToast.customToastShow(ACT, "カメラプレビュー表示に失敗");
								//ここでカメラが起動されていない場合はratioは存在しないのでビューに何も表示されなくなるのでデフォルトとしてビューの最大にしておく
								float angleRatio = (float)previewWidth/(float)previewHeight;//デフォルトのビュー
								liveSetting.setViewAngleRatio(angleRatio);
					}
				}
					break;
				case 1:
					func0.setVisibility(View.VISIBLE);
					func0.setText("送信中");
					func0.setVisibility(View.VISIBLE);
					func1.setText("撮影");
					break;
				case 2://静止画
				case 3://動画
					PreviewReader reader = (PreviewReader)handler.getReader();
					if(reader!= null&&reader.isStartedPreview())reader.stopPreview();
					func0.setVisibility(View.GONE);
					func1.setVisibility(View.VISIBLE);
					func1.setText("ファイル選択");
					break;
				}

				liveSetting.culclateRatio();

				if(liveSetting.getNowActualResolution() != null)Log.d("BCPlayer","XXX : " +liveSetting.getNowActualResolution().right + " " + liveSetting.getNowActualResolution().bottom );
				Log.d("BCPlayer","RATIO "+density);
				Log.d("BCPlayer","RATIO "+webViewW);
				Log.d("BCPlayer","RATIO "+webViewH);
				Log.d("BCPlayer","RATIO "+previewWidth);
				Log.d("BCPlayer","RATIO "+previewHeight);
				Log.d("BCPlayer","RATIO "+liveSetting.getRatio());
				int setWidth = previewWidth;
				int setHeight = (int) (previewWidth*liveSetting.getRatio());
				if(liveSetting.isPortLayt()){
					//Heightが超えるようなら、比率を計算し直す→最大縦幅変えずに、Widthが小さくなる
					if(previewHeight < setHeight){
						setWidth = (int)((float)previewHeight/liveSetting.getRatio());
						setHeight = previewHeight;
					}
					((TableLayout)parent.findViewById(R.id.parent_table)).setLayoutParams(new FrameLayout.LayoutParams(-1,-1));
				}else{//横レイアウト
					//Widthが超えるようなら、比率を計算し直す→最大横幅変えずに、Heightが小さくなる
					if(previewWidth < setWidth){
						setWidth = previewWidth;
						setHeight = (int)((float)previewHeight/liveSetting.getRatio());
					}
					Log.d("NLR","LANDSCAPE_LAYOUT "+liveSetting.getRatio() + " W:"+setWidth + " H:"+setHeight);
					((TableLayout)parent.findViewById(R.id.parent_table)).setLayoutParams(new FrameLayout.LayoutParams((int) (webViewH*density/2)+previewWidth/2,-1));
				}
				Log.d("BCPlayer","layoutPreviewPane : " +setWidth + " " + setHeight +"  "+ previewWidth + " " + previewHeight);

				LinearLayout previewParent = (LinearLayout)parent.findViewById(R.id.surface_parent);
				previewParent.removeAllViews();
				previewParent.addView(preview);
				preview.setLayoutParams(new LinearLayout.LayoutParams(setWidth,setHeight));
				//右のボタン3つ
				LinearLayout functionParent = (LinearLayout)parent.findViewById(R.id.function_parent);
				int funcHeight = previewHeight/4;
				func0.setLayoutParams(new LinearLayout.LayoutParams(-1,funcHeight));
				func1.setLayoutParams(new LinearLayout.LayoutParams(-1,funcHeight));
				((Button)parent.findViewById(R.id.restart_bt)).setLayoutParams(new LinearLayout.LayoutParams(-1,funcHeight));
				functionParent.setLayoutParams(new TableRow.LayoutParams((int) (previewWidth-webViewW*density/3),previewHeight));//これやる前にボタンの高さを固定する


				if(which == 0 ){
					//レイアウトしてからじゃないとstartPreviewしてもタイミング的にSurfaceViewにプレビューが表示されない
					startPreView();
				}
	}

	/**
	 *
   		liveSetting = new LiveSettings(new String[]{
           			"-version","00000000","-live","-host","nlpoca53.live.nicovideo.jp",	"-app",
           			"publicorigin/121125_23_1"+"?"+
   		"1267289:lv116734493:4:1353855299:0:1353855562:24b2aa668f6f0461",
           			"lv116734493",
           			"/sdcard/result.flv"
           			});
	 */

	public void drawCamSurface(final Bitmap bmp,int w ,int h){
		Log.d("BCPlayer","drawSurface ---" + w + " " + h);
		if(w == 0)w = previewWidth;
		if(h == 0)h = previewHeight;
		this.runOnUiThread(new Runnable(){
			@Override
			public void run(){
				if(mImageView == null){
					mImageView = new ImageView(ACT);
					mImageView.setScaleType(ScaleType.FIT_XY);
					mImageView.setBackgroundColor(Color.BLACK);
				}
				if(mImageView.getParent() == null){
					ViewGroup previewParent = (ViewGroup)parent.findViewById(R.id.surface_parent);
					previewParent.removeAllViews();
					previewParent.addView(mImageView);
				}
				//4:3の固定にする
				ViewGroup functionParent = (ViewGroup)parent.findViewById(R.id.function_parent);
					mImageView.setLayoutParams(new TableRow.LayoutParams(previewWidth,previewWidth*3/4));
					functionParent.setLayoutParams(new TableRow.LayoutParams((int) (previewWidth-webViewW*density/3),previewWidth*3/4));

				mImageView.setImageBitmap(bmp);
				mImageView.invalidate();
			}
		});
	}
	//
	public int getTargetWidth() {
		return previewWidth;
	}


	@Override
	public long getOffTimerStart() {
		long val = getIntent.getLongExtra("offtimer_start", -1);
				if(val < 0)val = 30;
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
	public void showPositionDialog() {
		if(listDialog != null && listDialog.isShowing())listDialog.cancel();
		Builder d = new AlertDialog.Builder(this).setItems(this.listPositionGetSetter(false,false)? new CharSequence[]{"上","下"}:new CharSequence[]{"前面","右"},
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
		case 3://全画面は配信では非対応
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
			}else if (setting_byte[31] == 0 || setting_byte[31] == 2) {// 前面、プレイヤーのみの場合、ここではコメサバ接続を持たないのでinit_modeが必ず2
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
	@Override
	public boolean showPostArea() {
		if (postArea == null) {
			return false;
		}
		if (postArea == null || postET == null || postB == null
				|| voiceInput == null) {
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
		postET.setLayoutParams(new TableRow.LayoutParams(-1, -2));
		postB.setLayoutParams(new TableRow.LayoutParams(-2, -2));
		postET.setWidth(0);

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
					voiceInput = (ImageButton) parent.findViewById(R.id.postarea_voiceinput_up);
					post_cdisp = (CheckBox)parent.findViewById(R.id.postarea_commentdisp_up);
					post_desc = (Button)parent.findViewById(R.id.postarea_desc_up);
				}else{
					post_184 = (CheckBox)parent.findViewById(R.id.postarea_184_down);
					post_command = (Button)parent.findViewById(R.id.postarea_command_down);
					post_update = (Button)parent.findViewById(R.id.postarea_update_down);
					post_desc = (Button)parent.findViewById(R.id.postarea_desc_down);
					voiceInput = (ImageButton) parent.findViewById(R.id.postarea_voiceinput_down);
					post_cdisp = (CheckBox)parent.findViewById(R.id.postarea_commentdisp_down);
					post_menu = (Button)parent.findViewById(R.id.postarea_menukey_down);
				}
				setFormListeners();
			}
			if (setting_boolean[3]) {
				post_184.setVisibility(View.VISIBLE);
				post_command.setVisibility(View.VISIBLE);
				post_update.setVisibility(View.VISIBLE);
				post_desc.setVisibility(View.VISIBLE);
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
			Log.d("NLiveRoid",  "B changeColumnSeq");
			column_seq = seq;
			if(listview != null && adapter != null){
				listview.setVisibility(View.INVISIBLE);
				byte idIndex = 0;
				for(byte i = 0; i < 7; i++){
					if(column_seq[i] == 1)idIndex = i;
				}
				for(int i = 0; i < listview.getCount();i++){
					if(listview.getChildAt(i) != null){
						((TableLayout)listview.getChildAt(i)).removeAllViews();
					for (int j = 0; j < 7; j++) {
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
	public void startPreView() {
		PreviewReader reader;
		if(handler.getReader() == null){
					reader = new PreviewReader(ACT,liveSetting);
					handler.setReader(reader);
		}else{
			reader = (PreviewReader)handler.getReader();
		}
		if(!liveSetting.isUseCam()){
			float angleRatio = (float)previewWidth/(float)previewHeight;//デフォルトのビュー
			liveSetting.setViewAngleRatio(angleRatio);
			return;
		}else if(!reader.isInited()&&reader.init(null)<0){
					MyToast.customToastShow(ACT, "カメラプレビュー表示に失敗");
					//ここでカメラが起動されていない場合はratioは存在しないのでビューに何も表示されなくなるのでデフォルトとしてビューの最大にしておく
					float angleRatio = (float)previewWidth/(float)previewHeight;//デフォルトのビュー
					liveSetting.setViewAngleRatio(angleRatio);
					return;
		}
				Log.d("NLiveRoid","startPreview_From_BCstartPreView2");
				reader.startPreview();
		}
	public void stopPreview() {
		if(handler.getReader() != null){
				Log.d("NLiveRoid","startPreview_From_BCstartPreView2");
				((PreviewReader)handler.getReader()).stopPreview();
		}
	}
	public void stopMic() {
		switch(liveSetting.getMode()){
		case 0:
		{
			PreviewReader reader = (PreviewReader)handler.getReader();
			if(reader != null)reader.stopMic();
		}
			break;
		case 1:
			break;
		case 2:
		{
		PictureReader reader = (PictureReader)handler.getReader();
		reader.stopMic();
		}
			break;
		}
	}
}