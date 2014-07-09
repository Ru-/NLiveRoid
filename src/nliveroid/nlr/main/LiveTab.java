package nliveroid.nlr.main;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nliveroid.nlr.main.parser.FirstEditAccessParser;
import nliveroid.nlr.main.parser.FirstSendFormParser;
import nliveroid.nlr.main.parser.ReuseParser;
import nliveroid.nlr.main.parser.SecondEdit_GetLVParser;
import nliveroid.nlr.main.parser.XMLparser;

import org.apache.http.ParseException;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParserException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;


public class LiveTab extends Activity {
	private static LiveTab ACT;
	private View parent;
	private ErrorCode error;
	private ViewGroup progressArea;
	private ProgressBar progressBar;
	private LayoutInflater inflater;
	private String[] commu_list;
	private String[] commu_ids;
	private String[] reserveValues;
	private int commu_s_index;
	private int category_s_index;
	private LiveInfo liveInfo;
	private LiveSettings liveSetting;

	private static FirstEditAccess initalTask;
	private static FirstSendFormTask programTask1;
	private static SecondSendForm_GetLVTask programTask2;
	private PublishParse publishTask;

	private static FrameLayout dialogFrame;
	private boolean notpremium;
	private Button categoryselect;
	private Button community_select;
	private String[] category_list;
	private String ulck = "";
	private AlertDialog alertD;
	private WaitDialog waitDialog;
	private ScrollView mainScroll;

	private ArrayList<EditText> tagEts;
	private ArrayList<CheckBox> tagLocks;

	private String reuseid;
	private final String profileFile = "LiveProfile.xml";
	private EditText live_title;
	private EditText live_desc;
	private CheckBox onlycommu;
	private CheckBox ts;

	private int retryCount;

	private boolean isWaiting = false;
	private WakeLock lock;
	private byte toptab_tcolor;
	private AlertDialog accountDialog;

	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		ACT = this;
		requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
		inflater = LayoutInflater.from(this);
		parent = inflater.inflate(R.layout.livetab, null);
		NLiveRoid app = (NLiveRoid)getApplicationContext();
		app.setForeACT(ACT);
		error = app.getError();
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		//背景にヘッダーをセットするか判定する
				TextView header = (TextView) parent.findViewById(R.id.livetitletext);
				toptab_tcolor = app.getDetailsMapValue("toptab_tcolor") == null? 0:Byte.parseByte(app.getDetailsMapValue("toptab_tcolor"));
				TopTabs.setTextColor(header,toptab_tcolor);
		setContentView(parent);
		mainScroll = (ScrollView) parent.findViewById(R.id.mainscroll);
		progressArea = (ViewGroup) parent.findViewById(R.id.progresslinear);
		progressBar = (ProgressBar) parent
				.findViewById(R.id.ProgressBarHorizontal);
		progressArea.removeView(progressBar);
		View pParent = inflater.inflate(R.layout.progressbar, null);
		progressBar = (ProgressBar) pParent
				.findViewById(R.id.ProgressBarHorizontal);

		// 情報取得中のビュー
		dialogFrame = (FrameLayout) parent.findViewById(R.id.live_loadingframe);

		// 放送コンポーネント初期設定
		//前回の情報ボタン
		Button profileB = (Button)parent.findViewById(R.id.live_profile_app);
		profileB.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				if(commu_ids == null ||commu_ids.length <= commu_s_index){
					MyToast.customToastShow(ACT, "放送可能コミュニティが読み込まれていません");
				}else{
				new SetBeforeAppProfile().execute();
				}
			}
		});
		//サーバ側履歴
		Button serverhistory = (Button)parent.findViewById(R.id.live_profile_server);
		serverhistory.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				if(commu_ids == null ||commu_ids.length <= commu_s_index){
					MyToast.customToastShow(ACT, "放送可能コミュニティが読み込まれていません");
				}else{
					addProgress();
				new SetBeforeServerProfile().execute();
				}
			}
		});
		live_title = (EditText) parent
				.findViewById(R.id.live_title_edit);
		live_desc = (EditText) parent
				.findViewById(R.id.live_description_edit);
		community_select = (Button) parent
				.findViewById(R.id.live_communityselefct_spinner);
		commu_list = new String[1];
		commu_list[0] = URLEnum.HYPHEN;
		community_select.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// 何かしらのタスクが走ってれば終了
				if (initalTask != null
						&& initalTask.getStatus() != AsyncTask.Status.FINISHED){
					initalTask.cancel(true);
					return;
				}
				if (programTask1 != null&& programTask1.getStatus() != AsyncTask.Status.FINISHED){
					programTask1.cancel(true);
					return;
				}
				if (programTask2 != null
						&& programTask2.getStatus() != AsyncTask.Status.FINISHED){
					programTask2.cancel(true);
					return;
				}
				// 一般の場合コミュ1つも来ない
				if (commu_list.length <= commu_s_index)
					return;
				new AlertDialog.Builder(ACT)
						.setTitle(commu_list[commu_s_index])
						.setItems(commu_list,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {
										commu_s_index = which;
										community_select.setText(commu_list[commu_s_index]);
									}
								}).show();
			}
		});
		categoryselect = (Button) parent
				.findViewById(R.id.live_categoryselect_spinner);
		category_list = new String[12];
		category_list[0] = "一般(その他)";
		category_list[1] = "政治";
		category_list[2] = "動物";
		category_list[3] = "料理";
		category_list[4] = "演奏してみた";
		category_list[5] = "歌ってみた";
		category_list[6] = "踊ってみた";
		category_list[7] = "描いてみた";
		category_list[8] = "講座";
		category_list[9] = "ゲーム";
		category_list[10] = "動画紹介";
		category_list[11] = "R18";
		categoryselect.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// 何かしらのタスクが走ってれば終了
				if (initalTask != null
						&& initalTask.getStatus() != AsyncTask.Status.FINISHED){
					initalTask.cancel(true);
					return;
				}
				if (programTask1 != null&& programTask1.getStatus() != AsyncTask.Status.FINISHED){
					programTask1.cancel(true);
					return;
				}
				if (programTask2 != null
						&& programTask2.getStatus() != AsyncTask.Status.FINISHED){
					programTask2.cancel(true);
					return;
				}
				new AlertDialog.Builder(ACT)
						.setTitle(category_list[category_s_index])
						.setItems(category_list,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {
										category_s_index = which;
										categoryselect
												.setText(category_list[category_s_index]);
									}
								}).show();
			}
		});
		final EditText tag1 = (EditText) parent
				.findViewById(R.id.live_tag1_edit);
		final EditText tag2 = (EditText) parent
				.findViewById(R.id.live_tag2_edit);
		final EditText tag3 = (EditText) parent
				.findViewById(R.id.live_tag3_edit);
		tagEts = new ArrayList<EditText>();
		tagEts.add(tag1);
		tagEts.add(tag2);
		tagEts.add(tag3);
		final CheckBox taglock1 = (CheckBox) parent
				.findViewById(R.id.taglock_1);
		final CheckBox taglock2 = (CheckBox) parent
				.findViewById(R.id.taglock_2);
		final CheckBox taglock3 = (CheckBox) parent
				.findViewById(R.id.taglock_3);
		tagLocks = new ArrayList<CheckBox>();
		tagLocks.add(taglock1);
		tagLocks.add(taglock2);
		tagLocks.add(taglock3);
		// タグの追加ボタンの処理
		final Button addBt = (Button) parent.findViewById(R.id.add_tag_button);
		addBt.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				addTagComponent();

			}
		});
		onlycommu = (CheckBox) parent
				.findViewById(R.id.live_public_check);
		ts = (CheckBox) parent
				.findViewById(R.id.live_timeshift_check);

		//起動選択は削除

		// 枠取りボタン
		Button bt = (Button) parent.findViewById(R.id.live_getprogram_button);
		bt.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if(waitDialog != null && waitDialog.isCanceled()){
					//順番待ち中だったらダイアログを表示するだけ(キャンセル||枠取得した場合は必ずnullをいれている)
					waitDialog.show();
				}else if (notpremium) {
					alertD = new ProgramInfoDialog(ACT, "プレミアム会員ではない可能性があります。")
							.show();
				} else {
					// 何かしらのタスクが走っていても動作させないと順番待ちとかのタスクがキャンセルされずに消えることがある
					if (initalTask != null
							&& initalTask.getStatus() != AsyncTask.Status.FINISHED){
						MyToast.customToastShow(ACT, "処理中です_");
						return;
					}
					if (programTask1 != null
							&& programTask1.getStatus() != AsyncTask.Status.FINISHED){
						MyToast.customToastShow(ACT, "処理中です.");
						return;
					}
					if (programTask2 != null
							&& programTask2.getStatus() != AsyncTask.Status.FINISHED){
						MyToast.customToastShow(ACT, "処理中です。");
						return;
					}
					liveInfo = new LiveInfo();
					try {
						liveInfo.setTitle(live_title.getText().toString().replaceAll("\n|\t| |　", ""));
						liveInfo.setDescription(live_desc.getText().toString());
						if (commu_ids == null
								|| commu_ids.length <= commu_s_index) {
							MyToast.customToastShow(ACT,
									"放送可能コミュニティの取得に失敗しています");
							return;
						}
						liveInfo.setCommunityID(URLEncoder.encode(
								commu_ids[commu_s_index], "utf-8"));
						liveInfo.setCategoryName(category_list[category_s_index]);
						String tagStr = "";
						String boundary = "BOUNDARY";
						for (int i = 0; i < tagEts.size(); i++) {
							String sufix = "Content-Disposition: form-data; name=\"livetags"
									+ (i + 1)
									+ "\"\r\n\r\n"
									+ tagEts.get(i).getText().toString()
									+ "\r\n"
									+ "--" + boundary + "\r\n";
							tagStr += sufix;
							if (tagLocks.get(i).isChecked()) {
								tagStr += "Content-Disposition: form-data; name=\"taglock"
										+ (i + 1)
										+ "\"\r\n\r\n"
										+ "true\r\n"
										+ "--" + boundary + "\r\n";
							}
						}
						liveInfo.setTags(tagStr);
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
					liveInfo.setMemberOnly(onlycommu.isChecked()); //コミュ限=2 非コミュ限=1
					liveInfo.setTimeShiftEnable(ts.isChecked()); //TS有効=1 TS無効=0
					if (programTask1 == null
							|| programTask1.getStatus() == AsyncTask.Status.FINISHED) {
						addProgress();
						// 最初の放送取得タスク開始
						programTask1 = new FirstSendFormTask();
						programTask1.execute();
					}

				}
			}
		});
		Button reloadBt = (Button)parent.findViewById(R.id.livetab_reload_bt);
		reloadBt.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				ACT.onReload();
			}
		});
		Button settingBt = (Button)parent.findViewById(R.id.right_setting_bt);
		settingBt.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Intent settingIntent = new Intent(ACT,SettingTabs.class);
	        	NLiveRoid app = (NLiveRoid)ACT.getApplicationContext();
	        	settingIntent.putExtra("session",app.getSessionid());
	        	startActivityForResult(settingIntent, CODE.REQUEST_SETTING_TAB);
			}
		});

		addProgress();
		initalTask = new FirstEditAccess();
		initalTask.execute();

	}//End of onCreate

	@Override
	public void onPause(){
		super.onPause();

		if(isWaiting){
			NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			notificationManager.cancelAll();
			Notification notif = new Notification();
			notif.icon = R.drawable.icon_nlr;
			// ペンディングできるIntentの入れ物
				        PendingIntent intent = PendingIntent.getActivity(
				                ACT,
				                CODE.REQUEST_NO_MEAN_NOTIFICATION , new Intent(ACT, TopTabs.class),
				                Intent.FLAG_ACTIVITY_NEW_TASK
				        );//なぜか2.2だとintentのnullが許されないみたいなのでTopTabsをいれとく
			notif.setLatestEventInfo(ACT,"順番待ち中","人数取得中",intent);
			notificationManager.notify(R.string.app_name, notif);
			}
//		Log.d("LiveTab","Is waiting? ---" + isWaiting);
	}

	@Override
	public void onDestroy(){
		super.onDestroy();
		//開放しないとタスクが詰った時に、画面が無反応になる
		initalTask = null;
		programTask1 = null;
		programTask2 = null;
	}

	public static LiveTab getLiveTab() {
		return ACT;
	}

	@Override
    public void onWindowFocusChanged(boolean hasFocus) {
		//テキストエリアの横幅を固定する
		NLiveRoid app = (NLiveRoid)getApplicationContext();
		int tagWidth = -1;
		if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
			tagWidth = (int) ((app.getViewWidth()*app.getMetrics()/3)*2);
			if(live_title != null){
				live_title.setWidth((app.getViewWidth()/4)*3);
			}
			if(live_desc != null){
				live_desc.setWidth((app.getViewWidth()/4)*3);
			}
			if(community_select != null){
				community_select.setWidth((app.getViewWidth()/4)*3);
			}
		}else{
			tagWidth = (int) ((app.getViewHeight()*app.getMetrics()/3)*2);
			if(live_title != null){
				live_title.setWidth((app.getViewHeight()/4)*3);
			}
			if(live_desc != null){
				live_desc.setWidth((app.getViewHeight()/4)*3);
			}
			if(community_select != null){
				community_select.setWidth((app.getViewHeight()/4)*3);
			}
		}
		for(int i = 0; i < tagEts.size(); i++){
			if(tagEts.get(i) != null){
		tagEts.get(i).setLayoutParams(new TableRow.LayoutParams((tagWidth),-2));
			}else{
				break;
			}
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {

		NLiveRoid app = (NLiveRoid)getApplicationContext();

			int tagWidth = 0;
			int w = 0;
			if(newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
				tagWidth = (int) ((app.getViewWidth()*app.getMetrics()/3)*2);
				if(live_title != null){
					live_title.setWidth((app.getViewWidth()/4)*3);
				}
				if(live_desc != null){
					live_desc.setWidth((app.getViewWidth()/4)*3);
				}
				if(community_select != null){
					community_select.setWidth((app.getViewWidth()/4)*3);
				}
				w = (int) (app.getViewWidth()*app.getMetrics());
			}else{
				tagWidth = (app.getViewHeight()/3)*2;
				if(live_title != null){
					live_title.setWidth((app.getViewHeight()/4)*3);
				}
				if(live_desc != null){
					live_desc.setWidth((app.getViewHeight()/4)*3);
				}
				if(community_select != null){
					community_select.setWidth((app.getViewHeight()/4)*3);
				}
				w = (int) (app.getViewHeight()*app.getMetrics());
			}

			for(int i = 0; i < tagEts.size(); i++){
				if(tagEts.get(i) != null){
					tagEts.get(i).setLayoutParams(new TableRow.LayoutParams(tagWidth,-2));
				}
			}

		super.onConfigurationChanged(newConfig);
	}

	public static void cancelMovingTask(){
		if (initalTask != null
				&& initalTask.getStatus() != AsyncTask.Status.FINISHED)
			initalTask.cancel(true);
		if (programTask1 != null
				&& programTask1.getStatus() != AsyncTask.Status.FINISHED)
			programTask1.cancel(true);
		if (programTask2 != null
				&& programTask2.getStatus() != AsyncTask.Status.FINISHED)
			programTask2.cancel(true);
	}
	public void onReload() {
		//フォームクリア
		if(live_title != null)live_title.setText("");
		if(live_desc != null)live_desc.setText("");
		for(int i = 0; i < tagEts.size(); i++){
			tagEts.get(i).setText("");
			tagLocks.get(i).setChecked(false);
		}
		if(onlycommu != null)onlycommu.setChecked(false);
		if(ts!= null)ts.setChecked(false);
		// 何かしらのタスクが走ってれば終了
		notpremium = false;
		if (initalTask != null
				&& initalTask.getStatus() != AsyncTask.Status.FINISHED){
			initalTask.cancel(true);
			return;
		}
		if (programTask1 != null&& programTask1.getStatus() != AsyncTask.Status.FINISHED){
			programTask1.cancel(true);
			return;
		}
		if (programTask2 != null
				&& programTask2.getStatus() != AsyncTask.Status.FINISHED){
			programTask2.cancel(true);
			return;
		}
		addProgress();
		initalTask = new FirstEditAccess();
		initalTask.execute();

	}

	@Override
	public void onResume() {
		super.onResume();
		CommunityTab.cancelMovingTask();//一旦全てキャンセル
		SearchTab.cancelMoveingTask();
		//schemeより後

		final NLiveRoid app = (NLiveRoid)getApplicationContext();

		try{
		if(TopTabs.isFirstLogin&&Boolean.parseBoolean(app.getDetailsMapValue("ac_confirm"))){
		//初回確認リスト表示
			if(accountDialog == null || !accountDialog.isShowing()){
		accountDialog = new AlertDialog.Builder(this).setTitle("アカウントを選択").setItems(new CharSequence[]{app.getDefaultMap().get("user_id1"),app.getDefaultMap().get("user_id2")},
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
						if(PrimitiveSetting.getACT() != null)PrimitiveSetting.getACT().updateAlways();
						TopTabs.isFirstLogin = false;
					onReload();
					}
				}).setCancelable(false).create();
		accountDialog.show();
			}
	}else{
			if(Boolean.parseBoolean(app.getDetailsMapValue("update_tab"))){
				this.onReload();
			}

	}
		}catch(NullPointerException e){
			e.printStackTrace();
		}catch(Exception e){
		e.printStackTrace();
		MyToast.customToastShow(this, "アプリケーションコンテキストエラー");
	}
	}


	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putString("WORKAROUND_FOR_BUG_19917_KEY",
				"WORKAROUND_FOR_BUG_19917_VALUE");
		// super.onSaveInstanceState(outState);
	}
	@Override
	public void onUserLeaveHint(){
		super.onUserLeaveHint();//順番待ちのダイアログ表示時呼ばれない
//		Log.d("LiveTab","OnUser ---" + isWaiting);
	}
	/*
	 * タグのエリアを増やす処理
	 */
	private void addTagComponent(){
	if (tagEts.size() + 1 > 10) {
		return;
	}
	int tagWidth = 0;
	NLiveRoid app = (NLiveRoid)getApplicationContext();
	if(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
	tagWidth = (app.getViewWidth()/3)*2;
	}else{
		tagWidth = (app.getViewHeight()/3)*2;
	}
	EditText et = new EditText(ACT);
	et.setLayoutParams(new TableRow.LayoutParams(tagWidth, -2));
	et.setHint("タグ" + (tagEts.size() + 1));
	et.setLines(1);
	CheckBox cb = new CheckBox(ACT);
	cb.setLayoutParams(new TableRow.LayoutParams(-2, -2));
	cb.setText("ロック");
	tagEts.add(et);
	tagLocks.add(cb);
	TableLayout tagsTable = (TableLayout) parent
			.findViewById(R.id.tags_table);
	TableRow tr = new TableRow(ACT);
	tr.addView(et);
	tr.addView(cb);
	tagsTable.addView(tr, new TableLayout.LayoutParams(-1, -2));
	}

	private ArrayList<String> readSetting(HashMap<String, String> map,boolean isApplyInfo,boolean isApplyCamMic){
		ArrayList<String> resultList = new ArrayList<String>();
		//ファイルを読み込む
		boolean isStorageAvalable = false;
		boolean isStorageWriteable = false;
		String state = Environment.getExternalStorageState();
		if(state == null){
			resultList.add("-1");
			return resultList;
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
			resultList.add("-1");
			return resultList;
		}


		//sdcard直下に、パッケージ名のフォルダを作りファイルを生成
		String filePath = Environment.getExternalStorageDirectory().toString() + "/NLiveRoid";

		File directory = new File(filePath);
		ErrorCode error = ((NLiveRoid)getApplicationContext()).getError();
//		Log.d("log","filepath " + filePath + " \n isCANWRITE " + directory.canWrite());
		if(directory.mkdirs()){//すでにあった場合も失敗する
			Log.d("log","mkdir");
		}
		File file = new File(filePath,profileFile);
		if(!file.exists()){//ファイル無かった
			try {
				file.createNewFile();//次からの読み込みがエラーしないように空のファイル(テンプレ)を作っておく
			String xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"+
				"<LiveProfile xmlns=\"http://nliveroid-tutorial.appspot.com/liveprofile/\">\n"+
				"<live_mode>2</live_mode>\n" +
				"<title></title>\n"+
				"<description></description>\n" +
				"<community_name>"+commu_list[commu_s_index]+"</community_name>\n" +
				"<category>"+category_list[category_s_index]+"</category>\n" +
				"<tag></tag>\n" +
				"<lock></lock>\n" +
				"<public_status>false</public_status>\n" +
				"<timeshift_enable>false</timeshift_enable>\n" +
				"<resolution_index>0</resolution_index>\n" +
				"<use_camera>true</use_camera>\n" +
				"<use_mic>true</use_mic>\n" +
				"<back_camera>false</back_camera>\n" +
				"<back_mic>false</back_mic>\n" +
				"<ring_camera>false</ring_camera>\n" +
				"<ring_mic>false</ring_mic>\n" +
				"<fps>10</fps>\n" +
				"<keyframe_interval>40</keyframe_interval>\n" +
				"<scene>0</scene>\n" +
				"<is_stereo>false</is_stereo>\n" +
				"<volume>100</volume>\n" +
				"<movie_path>/sdcard/test.flv</movie_path>\n" +
			    "</LiveProfile>\n";
				FileOutputStream fos = new FileOutputStream(file.getPath());
				fos.write(xml.getBytes());
				fos.close();
				resultList.add("1");
				liveInfo = new LiveInfo();
				liveSetting = new LiveSettings(null);
				return resultList;
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				resultList.add("-2");
				return resultList;
			} catch (IOException e) {
				e.printStackTrace();
				resultList.add("-3");
				return resultList;
			}
		}else{//ファイルがあった
			byte[] tempby = new byte[(int)((file).length())];
			FileInputStream fis = null;
			try {
			fis = new FileInputStream(file);
			fis.read(tempby);
			fis.close();
			String replaceDescStr = new String(tempby,"UTF-8");
			//詳細の部分のタグ文字を避けておく
			String parseStr = "";
			parseStr = replaceDescStr.replaceAll("<description>.*</description>", "");
			Matcher mc = Pattern.compile("<description>.*</description>").matcher(replaceDescStr);
			if(mc.find()){
				replaceDescStr = mc.group();
			}else{
				resultList.add("-10");
				return resultList;
			}
			replaceDescStr = replaceDescStr.substring(13,replaceDescStr.length()-14);
			map.put("description", replaceDescStr);
			XMLparser.parseLiveProfile(parseStr,map);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				resultList.add("-2");
				return resultList;
			} catch (IOException e) {
				e.printStackTrace();
				resultList.add("-3");
				return resultList;
			}

			//LiveProfileのエラーチェック
			if(map.get("title") == null){
				resultList.add("-9");
			}else{
				map.put("title", map.get("title").replaceAll("\n|\t| |　", ""));
			}
			//詳細
			if(map.get("description")==null){
				resultList.add("-10");
			}
			//コミュニティ
			try{
				boolean failed = false;
				if(map.get("community_name") != null){
					if(map.get("community_name").matches("co[0-9]+")){
						for(int i = 0; i < commu_ids.length; i++){
//							Log.d("LiveTab","CCCC" + commu_ids[i] + " " + map.get("community_name"));
							if(commu_ids[i].equals(map.get("community_name"))){
								map.put("community_name", String.valueOf(i));
								break;
							}else if(i == commu_ids.length-1){
								failed = true;
							}
						}
					}else if(map.get("community_name").contains("Lv")){
						for(int i = 0; i < commu_list.length; i++){
//							Log.d("LiveTab","CCCC" + commu_list[i] + " " + map.get("community_name"));
							if(commu_list[i].equals(map.get("community_name"))){
								map.put("community_name", String.valueOf(i));
								break;
							}else if(i == commu_ids.length-1){
								failed = true;
							}
						}
					}
					if(failed){
//						Log.d("LiveTab","failed " + failed);
						resultList.add("-8");
						liveInfo.setComunityName("0");
					}
					Integer.parseInt(map.get("community_name"));//エラーする場合ここでExceptionして大丈夫になる
				}else{
					resultList.add("-8");
					map.put("community_name", "0");
				}
			}catch(Exception e){
				e.printStackTrace();
				resultList.add("-8");
				map.put("community_name", "0");
			}
			//カテゴリ
			if(map.get("category")==null){
				resultList.add("-7");
			}else{
				for(int i = 0; i < category_list.length; i++){
					if(map.get("category").contains(category_list[i])){
			//ここでcategory_s_indexに値を入れてしまうと、エラーした時にテキストは変わらないのに内部で値が違っちゃうことになるので
			//tempに数値を入れておく
						map.put("category",String.valueOf(i));
						break;
					}else if(i == category_list.length-1){
						resultList.add("-7");
						break;
					}
				}
			}
			try{
				Boolean.parseBoolean(map.get("public_status"));
			}catch(Exception e){
				e.printStackTrace();
				resultList.add("-4");
			}
			try{
				Boolean.parseBoolean(map.get("timeshift_enable"));
			}catch(Exception e){
				e.printStackTrace();
				resultList.add("-5");
			}
			try{
				Boolean.parseBoolean(map.get("use_camera"));
				Boolean.parseBoolean(map.get("use_mic"));
				Boolean.parseBoolean(map.get("back_camera"));
				Boolean.parseBoolean(map.get("back_mic"));
				Boolean.parseBoolean(map.get("ring_camera"));
				Boolean.parseBoolean(map.get("ring_mic"));
			}catch(Exception e){
				e.printStackTrace();
				resultList.add("-11");
				map.put("use_camera", "true");
				map.put("use_mic","true");
				map.put("back_camera", "false");
				map.put("back_mic", "false");
				map.put("ring_camera", "false");
				map.put("ring_mic", "false");
			}
			try{
				Integer.parseInt(map.get("live_mode"));
			}catch(Exception e){
				e.printStackTrace();
				map.put("live_mode", "0");
				resultList.add("live_mode");
			}
			try{
				Integer.parseInt(map.get("fps"));
				Integer.parseInt(map.get("keyframe_interval"));
				int vol = Integer.parseInt(map.get("volume"));
				if(vol < 0 || vol > 200)throw new Exception("volume GA OKASIIYO-NN");
				Boolean.parseBoolean(map.get("is_stereo"));
			}catch(Exception e){
				e.printStackTrace();
				resultList.add("-11");
				map.put("fps", "10");
				map.put("keyframe_interval","40");
				map.put("volume", "100");
				map.put("is_stereo", "false");
			}
			//うまくいっていたら、オブジェクトにセットする
			if(liveInfo == null)liveInfo = new LiveInfo();
			if(liveSetting == null)liveSetting = new LiveSettings(null);
				if(isApplyInfo){
			liveInfo.setTitle(map.get("title"));
			liveInfo.setDescription(map.get("description"));
			try{
			liveInfo.setCommunityID((map.get("community_name") == null? "":commu_ids[Integer.parseInt(map.get("community_name"))]));//ここまでで"0"にしてあるはず
			}catch(Exception e){
				e.printStackTrace();
				liveInfo.setCommunityID("");
			}
			liveInfo.setMemberOnly(map.get("public_status").equals("2"));
			liveInfo.setTimeShiftEnable(map.get("timeshift_enable").equals("true"));
			try{
			liveInfo.setCategoryName((map.get("category") == null? "":category_list[Integer.parseInt(map.get("category"))]));
			}catch(Exception e){
				e.printStackTrace();
				liveInfo.setCategoryName("");
			}
			Log.d("readSetting","INFO --- " + liveInfo.getTitle());
			Log.d("readSetting","INFO --- " + liveInfo.getDescription());
			Log.d("readSetting","INFO --- " + liveInfo.getCommunityName());
			Log.d("readSetting","INFO --- " + liveInfo.getCategoryName());
			Log.d("readSetting","INFO --- " + liveInfo.getCommunityID());
			Log.d("readSetting","INFO --- " + liveInfo.isMemberOnly());
			Log.d("readSetting","INFO --- " + liveInfo.isTimeShiftEnable());
				}//End of isApplyInfo
				if(isApplyCamMic){
			liveSetting.setUseCam(Boolean.parseBoolean(map.get("use_camera")));
			liveSetting.setUseMic(Boolean.parseBoolean(map.get("use_mic")));
			liveSetting.setMode(Integer.parseInt(map.get("live_mode")));
			liveSetting.setBackGroundCam(Boolean.parseBoolean(map.get("back_camera")));
			liveSetting.setBackGroundMic(Boolean.parseBoolean(map.get("back_mic")));
			liveSetting.setRingCamEnable(Boolean.parseBoolean(map.get("ring_camera")));
			liveSetting.setRingMicEnable(Boolean.parseBoolean(map.get("ring_mic")));
			liveSetting.setUser_fps(Integer.parseInt(map.get("fps")));
			liveSetting.setKeyframe_interval(Integer.parseInt(map.get("keyframe_interval")));
			liveSetting.setVolume((float)Integer.parseInt(map.get("volume"))/(float)10.0);
//			liveSetting.setIsStereo(Boolean.parseBoolean(map.get("is_stereao")));
			Log.d("readSetting","INFO --- " + liveSetting.getMode());
			Log.d("readSetting","INFO --- " + liveSetting.isUseCam());
			Log.d("readSetting","INFO --- " + liveSetting.isUseMic());
			Log.d("readSetting","INFO --- " + liveSetting.isBackGroundCam());
			Log.d("readSetting","INFO --- " + liveSetting.isBackGroundMic());
			Log.d("readSetting","INFO --- " + liveSetting.isRingCamEnable());
			Log.d("readSetting","INFO --- " + liveSetting.isRingMicEnable());
			Log.d("readSetting","INFO --- " + liveSetting.getFilePath());
				}
		}
			return resultList;
		}
	/**
	 * ※前回ボタンで読み込んで、枠取得で書き込む
	 * 読み込めなかったら原因はここにありそう
	 * @author Owner	 *
	 *
	 */
	class SetBeforeAppProfile extends AsyncTask<Void,Void,ArrayList<String>>{
		private HashMap<String,String> temp = null;
		@Override
		protected ArrayList<String> doInBackground(Void... arg0) {
			Log.d("LiveTab","SetBefore");
			temp = new HashMap<String,String>();
			return readSetting(temp,true,true);
		}
		@Override
		protected void onPostExecute(ArrayList<String> arg){
			//メンドイからどの道値入れちゃってみる
			if(arg != null&& temp != null){
				try{
				live_title.setText(temp.get("title"));
				live_desc.setText(temp.get("description"));
				commu_s_index = Integer.parseInt(temp.get("community_name"));//インデックス番が入っているはず
				community_select.setText(commu_list[commu_s_index]);
				category_s_index = Integer.parseInt(temp.get("category"));
				categoryselect.setText(category_list[category_s_index]);
				onlycommu.setChecked(Boolean.parseBoolean(temp.get("public_status")));
				ts.setChecked(Boolean.parseBoolean(temp.get("timeshift_enable")));

				try{
				//タグ
				for(int i = 0;i <= 10;i++){
					if(temp.get("tag" + i) == null){
						break;
					}else{
						if(tagEts.size()-1<i){//3つ目以上になったら増やす
							addTagComponent();
						}
						tagEts.get(i).setText(temp.get("tag"+i));
						if(temp.get("lock"+i) != null&&temp.get("lock"+i).equals("true")){
							tagLocks.get(i).setChecked(true);
						}
					}
				}
				}catch(Exception e){
					e.printStackTrace();
					MyToast.customToastShow(ACT, "放送プロファイルの読み込み失敗\nタグの値");
				}
				}catch(Exception e){
					e.printStackTrace();
					MyToast.customToastShow(ACT, "放送プロファイルの読み込み失敗");
				}
				if(arg.contains("1")){
					MyToast.customToastShow(ACT, "放送情報ファイルがありませんでした。生成しました。");
				}if(arg.contains("-1")){
					MyToast.customToastShow(ACT, "SDカードが利用できませんでした\nプロファイルは機能できません");
				}if(arg.contains("-2")){
					MyToast.customToastShow(ACT, "放送情報ファイルがありませんでした。読み込み失敗しました");
				}if(arg.contains("-11")){
					MyToast.customToastShow(ACT, "放送情報ファイルの読み込み失敗\nカメラ/マイクの設定値");
				}if(arg.contains("-3")){
					MyToast.customToastShow(ACT, "放送情報ファイルの読み込みでIOエラー");
				}if(arg.contains("-9")){
					MyToast.customToastShow(ACT, "放送情報ファイルの読み込み失敗\nタイトルの値");
				}if(arg.contains("-4")){
					MyToast.customToastShow(ACT, "放送情報ファイルの読み込み失敗\nコミュニティ限定の値");
				}if(arg.contains("-5")){
					MyToast.customToastShow(ACT, "放送情報ファイルの読み込み失敗\nタイムシフト利用の値");
				}if(arg.contains("-6")){
					MyToast.customToastShow(ACT, "放送情報ファイルの読み込み失敗\nタグの値");
				}if(arg.contains("-7")){
					MyToast.customToastShow(ACT, "放送情報ファイルの読み込み失敗\nカテゴリの値");
				}if(arg.contains("-8")){
					MyToast.customToastShow(ACT, "放送情報ファイルの読み込み失敗\nコミュニティの値");
				}if(arg.contains("-10")){
					MyToast.customToastShow(ACT, "放送情報ファイルの読み込み失敗\n詳細の値");
				}if(arg.contains("-11")){
					MyToast.customToastShow(ACT, "放送情報ファイルの読み込み失敗\nカメラ/マイクの設定値");
				}if(arg.contains("live_mode")){
					MyToast.customToastShow(ACT, "放送情報ファイルの読み込み失敗\n放送モード");
				}
			}
		}
	}

	/**
		サーバ側
	 * @author Owner	 *
	 *
	 */
	public class SetBeforeServerProfile extends AsyncTask<Void,Void,ArrayList<String>>{
		private HashMap<String,String> formValues = null;
		private boolean ENDFLAG = true;
		@Override
		protected ArrayList<String> doInBackground(Void... arg0) {
			if(reuseid == null)return null;
			formValues = new HashMap<String,String>();
			progressBar.setProgress(10);
			String sessionid = Request.getSessionID(error);
			if (sessionid == null || sessionid.equals("")) {
				error.setErrorCode(-4);
				return null;
			}
			//エティットストリームを見に行く
			InputStream reuse = Request
					.doGetToInputStreamFromFixedSession(sessionid,
							URLEnum.EDITSTREAM + reuseid, error);
			progressBar.setProgress(40);
			try {
				org.ccil.cowan.tagsoup.Parser parser = new org.ccil.cowan.tagsoup.Parser();
				ReuseParser handler = new ReuseParser(this);
				parser.setContentHandler(handler);
				parser.parse(new InputSource(reuse));
				progressBar.setProgress(75);
			} catch (org.xml.sax.SAXNotRecognizedException e) {
				// Should not happen.
				e.printStackTrace();
				throw new RuntimeException(e);
			} catch (org.xml.sax.SAXNotSupportedException e) {
				// Should not happen.
				e.printStackTrace();
				throw new RuntimeException(e);
			} catch (IOException e) {
				error.setErrorCode(-39);
				e.printStackTrace();
			} catch (SAXException e) {
				error.setErrorCode(-39);
				e.printStackTrace();
			}
			long startT = System.currentTimeMillis();
			while (ENDFLAG ) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
					ENDFLAG = false;
					return null;
				}catch(IllegalArgumentException e){
					e.printStackTrace();
					ENDFLAG = false;
					return null;
				}
				if ((System.currentTimeMillis() - startT) > 30000) {
					error.setErrorCode(-10);
					ENDFLAG = false;
					return null;
				}
			}

			return null;
		}
		public void finishCallBack(HashMap<String, String> formValues){
			this.formValues = formValues;
			if(formValues == null){
				error.setErrorCode(-46);
				ENDFLAG = false;
				return;
			}
			for(Iterator<String> it = formValues.keySet().iterator(); it.hasNext();){
				String next = it.next();
			}
			ENDFLAG = false;
		}

		@Override
		protected void onPostExecute(ArrayList<String> arg){
			if(error != null && error.getErrorCode() != 0){
				error.showErrorToast();
				removeProgress();
				return;
			}
			progressBar.setProgress(90);
			if(formValues != null){
				if(formValues.get("title") != null)live_title.setText(formValues.get("title"));
				if(formValues.get("description") != null)live_desc.setText(formValues.get("description"));
				if(formValues.get("community_name") != null){//コミュ名がそのまま入ってくるので編集
					for(int i = 0; i < commu_list.length; i++){
						if(commu_list[i].equals(formValues.get("community_name"))){
							commu_s_index = i;
						}
					}
				}
				community_select.setText(commu_list[commu_s_index]);
				if(formValues.get("category") != null){//カテゴリ名がそのまま入ってくるので編集
					for(int i = 0; i < category_list.length;i++){
						if(category_list[i].equals(formValues.get("category"))){
							category_s_index = i;
						}
					}
				}
				categoryselect.setText(category_list[category_s_index]);
				if(formValues.get("public_status") != null)onlycommu.setChecked(Boolean.parseBoolean(formValues.get("public_status")));
				if(formValues.get("timeshift_enable") != null)ts.setChecked(Boolean.parseBoolean(formValues.get("timeshift_enable")));

				try{
				//タグ
					for(int i = 0;i <= 10;i++){
						if(formValues.get("tag" + i) == null){
							break;
						}else{
							if(tagEts.size()-1<i){//3つ目以上になったら増やす
								addTagComponent();
							}
							tagEts.get(i).setText(formValues.get("tag"+i));
							if(formValues.get("lock"+i) != null&&formValues.get("lock"+i).equals("true")){
								tagLocks.get(i).setChecked(true);
							}
						}
					}
				}catch(Exception e){
					e.printStackTrace();
					MyToast.customToastShow(ACT, "放送プロファイルの読み込み失敗\nタグの値");
				}
				//ビローン対策、フォーカスを着けはずしする
				TopTabs.getACT().onWindowFocusChanged(false);
			}else{
				MyToast.customToastShow(ACT, "前回の情報の取得に失敗しました");
			}
			removeProgress();
		}
	}
	/**
	 * アプリ用の放送履歴プロファイルの書き込み
	 *
	 * @author Owner
	 *
	 */
	class WriteProFile extends AsyncTask<Void,Void,ArrayList<String>>{
		@Override
		protected ArrayList<String> doInBackground(Void... params) {
			Log.d("WriteProfile","doIn");
			if(liveSetting != null && NLiveRoid.isDebugMode)Log.d("NLiveRoid"," MODE " + liveSetting.getMode());
			ArrayList<String> result = new ArrayList<String>();
//			この値にエンコーダの設定値が入る
			Matcher hostmc = Pattern.compile("nlpoca[0-9]+\\.live\\.nicovideo\\.jp").matcher(liveInfo.getRtmpurl());
			if(hostmc.find()){
				liveSetting.setHost(hostmc.group());
			}else{
				result.add("-9");
				return result;
			}
			Matcher appnamemc = Pattern.compile("publicorigin.+").matcher(liveInfo.getRtmpurl());
			if(appnamemc.find()){
				liveSetting.setAppName(appnamemc.group());
			}else{
				result.add("-9");
				return result;
			}
			liveSetting.setStreamName(liveInfo.getLiveID());

			//ファイルを読み込む
			boolean isStorageAvalable = false;
			boolean isStorageWriteable = false;
			String state = Environment.getExternalStorageState();
			if(state == null){
				result.add("-1");
				return result;
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
				result.add("-1");
				return result;
			}


			//sdcard直下に、パッケージ名のフォルダを作りファイルを生成
			String filePath = Environment.getExternalStorageDirectory().toString() + "/NLiveRoid";

			File directory = new File(filePath);
			if(directory.mkdirs()){//すでにあった場合も失敗する
				Log.d("log","mkdir");
			}
			File file = new File(filePath,profileFile);
			if(!file.exists()){//ファイル無かった
				try {
					file.createNewFile();//ファイル無ければテンプレ作成
					//タグの文字をあらかじめ編集
					String tagStr = "";
					for(int i = 0; i < tagEts.size(); i++){
						tagStr += "<tag>"+tagEts.get(i).getText().toString()+"</tag>\n" +
								"<lock>"+tagLocks.get(i).isChecked() +"</lock>\n";
					}
				String xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"+
					"<LiveProfile xmlns=\"http://nliveroid-tutorial.appspot.com/liveprofile/\">\n"+
					"<title>"+live_title.getText().toString()+"</title>\n"+
					"<description>"+live_desc.getText().toString()+"</description>\n" +
					"<community_name>"+commu_list[commu_s_index]+"</community_name>\n" +
					"<category>"+category_list[category_s_index]+"</category>\n" +
					tagStr+
					"<public_status>"+onlycommu.isChecked()+"</public_status>\n" +
					"<timeshift_enable>"+ts.isChecked()+"</timeshift_enable>\n" +
					"<use_camera>true</use_camera>\n" +
					"<use_mic>true</use_mic>\n" +
					"<resolution_index>0</resolution_index>\n" +
					"<back_camera>false</back_camera>\n" +
					"<back_mic>false</back_mic>\n" +
					"<ring_camera>false</ring_camera>\n" +
					"<ring_mic>false</ring_mic>\n" +
					"<live_mode>2</live_mode>\n" +
					"<fps>10</fps>\n" +
					"<keyframe_interval>40</keyframe_interval>\n" +
					"<scene>0</scene>\n" +
					"<is_stereo>false</is_stereo>\n" +
					"<volume>100</volume>\n" +
					"<movie_path>/sdcard/test.flv</movie_path>\n" +
				    "</LiveProfile>\n";
					FileOutputStream fos = new FileOutputStream(file.getPath());
					fos.write(xml.getBytes());
					fos.close();
					result.add("1");
					return result;
				} catch (FileNotFoundException e) {
					e.printStackTrace();
					result.add("-2");
					return result;
				} catch (IOException e) {
					e.printStackTrace();
					result.add("-3");
					return result;
				}
			}else{//ファイルあった Flash側の設定をチェック
				byte[] tempby = new byte[(int)((file).length())];
				FileInputStream fis = null;
				HashMap<String,String> map = new HashMap<String,String>();
				try {
					fis = new FileInputStream(file);
					fis.read(tempby);
					fis.close();
					String source = new String(tempby,"UTF-8");
					source = source.replaceAll("<description>.*</description>", "");
					XMLparser.parseLiveProfile(source,map);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
					result.add("-2");
					return result;
				} catch (IOException e) {
					e.printStackTrace();
					result.add("-3");
					return result;
				}
				//-----------------------読み込んだ値のチェック
				try{
					if(map.get("use_camera")!= null){
						liveSetting.setUseCam(Boolean.parseBoolean(map.get("use_camera")));
					}else{
						liveSetting.setUseCam(true);
					}
				}catch(Exception e){
					e.printStackTrace();
					result.add("use_camera");
					return result;
				}
				try{
					if(map.get("use_mic")!= null){
						liveSetting.setUseMic(Boolean.parseBoolean(map.get("use_mic")));
					}else{
						liveSetting.setUseMic(true);
					}
				}catch(Exception e){
					e.printStackTrace();
					result.add("use_mic");
					return result;
				}
				try{
					if(map.get("back_camera")!= null){
						liveSetting.setBackGroundCam(Boolean.parseBoolean(map.get("back_camera")));
					}else{
						liveSetting.setBackGroundCam(true);
					}
				}catch(Exception e){
					e.printStackTrace();
					result.add("back_camera");
					return result;
				}
				try{
					if(map.get("back_mic")!= null){
						liveSetting.setBackGroundMic(Boolean.parseBoolean(map.get("back_mic")));
					}else{
						liveSetting.setBackGroundMic(true);
					}
				}catch(Exception e){
					e.printStackTrace();
					result.add("back_mic");
					return result;
				}
				try{
					if(map.get("ring_camera")!= null){
						liveSetting.setRingCamEnable(Boolean.parseBoolean(map.get("ring_camera")));
					}else{
						liveSetting.setRingCamEnable(true);
					}
				}catch(Exception e){
					e.printStackTrace();
					result.add("ring_camera");
					return result;
				}
				try{
					if(map.get("ring_mic")!= null){
						liveSetting.setRingMicEnable(Boolean.parseBoolean(map.get("ring_mic")));
					}else{
						liveSetting.setRingMicEnable(true);
					}
				}catch(Exception e){
					e.printStackTrace();
					result.add("ring_mic");
					return result;
				}
				try{
					if(map.get("live_mode")!= null){
						liveSetting.setMode(Integer.parseInt(map.get("live_mode")));
					}else{
						liveSetting.setMode(2);
					}
				}catch(Exception e){
					e.printStackTrace();
					result.add("live_mode");
					return result;
				}
				//カメラ系パラメタは一纏め
				try{
					if(map.get("resolution_index") != null)liveSetting.setNowResolution(Integer.parseInt(map.get("resolution_index")));
					if(map.get("fps")!= null)liveSetting.setUser_fps(Integer.parseInt(map.get("fps")));//ない場合デフォ値
					if(map.get("keyframe_interval") != null)liveSetting.setKeyframe_interval(Integer.parseInt(map.get("keyframe_interval")));
					if(map.get("scene") != null)liveSetting.setSceneModeIndex(Integer.parseInt(map.get("scene")));
				}catch(Exception e){
					e.printStackTrace();
					result.add("camera_params");
					return result;
				}
				//マイク系パラメタチェック
				try{
//					if(map.get("is_stereo") != null){
//						liveSetting.setIsStereo(Boolean.parseBoolean(map.get("is_stereo")));
//					}
					if(map.get("volume")!= null){
						int vol = Integer.parseInt(map.get("volume"));
						if(vol < 0 || vol > 200)throw new Exception("Volume GA OKASIIYO-NN");
						liveSetting.setVolume((float)vol/(float)10.0);//ない場合デフォ値
					}
				}catch(Exception e){
					e.printStackTrace();
					result.add("mic_params");
					return result;
				}

//				Log.d("LiveTab","Write ---  PATH" + liveSetting.getFilePath());
				try{
					if(map.get("movie_path")!= null&&!map.get("movie_path").equals("null")){
						liveSetting.setFilePath(map.get("movie_path"));
					}else{
						map.put("movie_path", "/sdacard/test.flv");
					}
				}catch(Exception e){
					e.printStackTrace();
					result.add("movie_path");
					return result;
				}
				//ファイル書き込み
				//タグの文字をあらかじめ編集
				String tagStr = "";
				for(int i = 0; i < tagEts.size(); i++){
					tagStr += "<tag>"+tagEts.get(i).getText().toString()+"</tag>\n" +
							"<lock>"+tagLocks.get(i).isChecked() +"</lock>\n";
				}
//				Log.d("LiveTab","SaveData ---- " + live_title.getText() + "  " + commu_list[commu_s_index]);
			String xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"+
				"<LiveProfile xmlns=\"http://nliveroid-tutorial.appspot.com/liveprofile/\">\n"+
				"<live_mode>"+liveSetting.getMode()+"</live_mode>\n" +
				"<title>"+live_title.getText().toString()+"</title>\n"+
				"<description>"+live_desc.getText().toString()+"</description>\n" +
				"<community_name>"+commu_list[commu_s_index]+"</community_name>\n" +
				"<category>"+category_list[category_s_index]+"</category>\n" +
				tagStr+
				"<public_status>"+onlycommu.isChecked()+"</public_status>\n" +
				"<timeshift_enable>"+ts.isChecked()+"</timeshift_enable>\n" +
				"<use_camera>"+liveSetting.isUseCam()+"</use_camera>\n" +
				"<use_mic>"+liveSetting.isUseMic()+"</use_mic>\n" +
				"<resolution_index>"+liveSetting.getResolutionIndex()+"</resolution_index>\n" +
				"<back_camera>"+liveSetting.isBackGroundCam()+"</back_camera>\n" +
				"<back_mic>"+liveSetting.isBackGroundMic()+"</back_mic>\n" +
				"<ring_camera>"+liveSetting.isRingCamEnable()+"</ring_camera>\n" +
				"<ring_mic>"+liveSetting.isRingMicEnable()+"</ring_mic>\n" +
				"<fps>"+liveSetting.getUser_fps()+"</fps>\n" +
				"<keyframe_interval>"+liveSetting.getKeyframe_interval()+"</keyframe_interval>\n" +
				"<scene>"+liveSetting.getSceneModeIndex()+"</scene>\n" +
//				"<is_stereo>"+liveSetting.isStereo() +"</is_stereo>\n" +
				"<volume>"+((int)(liveSetting.getVolume()*10))+"</volume>\n" +
				"<movie_path>"+liveSetting.getFilePath()+"</movie_path>\n" +
				"</LiveProfile>\n";
				FileOutputStream fos = null;
				try {
					fos = new FileOutputStream(file.getPath());
					fos.write(xml.getBytes());
					fos.close();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
					result.add("-2");
					return result;
				} catch (IOException e) {
					e.printStackTrace();
					result.add("-3");
					return result;
				}
				return result;
			}
		}//END OF deInBackground
		@Override
		protected void onPostExecute(ArrayList<String> arg){
			if(arg != null){
			 if(arg != null && arg.size() ==  1&&arg.get(0).equals("1")){
				//最初に遷移時に普通に生成した場合、これが出るとエ?ってなるから出さない
//				MyToast.customToastShow(ACT, "放送情報ファイルがありませんでした。生成しました。");
			}else if(arg.contains("-1")){
				MyToast.customToastShow(ACT, "SDカードが利用できませんでした\n");
			}else if(arg.contains("-2")){
				MyToast.customToastShow(ACT, "放送情報ファイルの保存に失敗しました");
			}else if(arg.contains("-3")){
				MyToast.customToastShow(ACT, "放送情報ファイルの保存で入出力エラーが発生しました。");
			}else if(arg.contains("-8")){
				MyToast.customToastShow(ACT, "放送情報ファイルの保存に失敗\nXMLが不正");
			}else if(arg.contains("-9")){
				MyToast.customToastShow(ACT, "RTMPの情報取得に失敗");
			}else if(arg.size() > 0){
				String missed = "";
				for(int i = 0; i < arg.size(); i++){
					missed += arg.get(i);
				}
				MyToast.customToastShow(ACT, "放送情報ファイルの次の値は失敗しました\n" + missed);
			}
			}
			//失敗しようがどの道放送画面へ遷移する
			initialLive();
		}

	}
	/**
	 * エディットストリーム
	 * ここではulckは得れないのと、順番待ちのフォームを送れない
	 * 最初のタスク
	 */
	public class FirstEditAccess extends AsyncTask<Void, Void, Void> {
		private boolean ENDFLAG = true;
		private ArrayList<String> communitys;
		private ArrayList<String> commuids;

		@Override
		protected void onPreExecute() {
			if (dialogFrame != null
					&& (dialogFrame.getVisibility() == View.INVISIBLE || dialogFrame
							.getVisibility() == View.GONE)) {
				dialogFrame.setVisibility(View.VISIBLE);
			}
		}

		@Override
		public void onCancelled() {
			ENDFLAG = false;
			if (dialogFrame != null
					&& dialogFrame.getVisibility() == View.VISIBLE) {
				dialogFrame.setVisibility(View.GONE);

			}
			if (alertD != null && alertD.isShowing()) {// 表示中に終了か回転で落ちるかもしれない
				alertD.dismiss();
				alertD = null;
			}
			removeProgress();
			super.onCancelled();
		}

		@Override
		protected Void doInBackground(Void... arg0) {
			if (error == null || error.getErrorCode() != 0){
				return null;
			}
			//認証 マップに保存より読み込むほうが楽か?
//			try {
//				Context mContext = createPackageContext("nliveroid.nlr.main", CONTEXT_RESTRICTED);
//				FileInputStream fis = mContext.openFileInput("temp");
//			SerializeMap serMap = (SerializeMap)new ObjectInputStream(fis).readObject();
//			fis.close();
//			if(serMap == null || serMap.get("nlive.pass") == null || serMap.get("nlive.pass").equals("")){
//				error.setErrorCode(-31);
//				return null;
//			}
//			URL url = null;
//			Random rand = new Random();
//			switch(rand.nextInt(3)){
//			case 0:
//				url = new URL(URLEnum.AUTH0);
//				break;
//			case 1:
//				url = new URL(URLEnum.AUTH1);
//				break;
//			case 2:
//				url = new URL(URLEnum.AUTH2);
//				break;
//			default:
//				url = new URL(URLEnum.AUTH0);
//				break;
//			}
//			HttpURLConnection con = (HttpURLConnection)url.openConnection();
//			con.setRequestProperty("Cookie", "nlive.pass=" + serMap.get("nlive.pass"));
//			String result = con.getHeaderField("Set-Cookie");
//			if(result == null){
//				error.setErrorCode(-31);
//				return null;
//			}
//					if(!result.equals("nlive.pass=OK")){
//						error.setErrorCode(-32);
//						return null;
//					}
//			} catch(FileNotFoundException e){
//				//例外の場合未認証にする事を忘れない
//				e.printStackTrace();
//				error.setErrorCode(-31);
//				return null;
//			}catch (MalformedURLException e) {
//				e.printStackTrace();
//				error.setErrorCode(-31);
//				return null;
//			} catch (IOException e) {
//				e.printStackTrace();
//				error.setErrorCode(-31);
//				return null;
//			} catch (ClassNotFoundException e) {
//				e.printStackTrace();
//				error.setErrorCode(-31);
//				return null;
//			} catch (NameNotFoundException e) {
//				e.printStackTrace();
//				error.setErrorCode(-31);
//				return null;
//			}
			String sessionid = Request.getSessionID(error);
			if (sessionid == null || sessionid.equals("")) {
				error.setErrorCode(-4);
				return null;
			}
			progressBar.setProgress(15);
			//エティットストリームを見に行く
			InputStream firstEditstream = Request
					.doGetToInputStreamFromFixedSession(sessionid,
							URLEnum.EDITSTREAM, error);

			try {

				org.ccil.cowan.tagsoup.Parser parser = new org.ccil.cowan.tagsoup.Parser();
				FirstEditAccessParser handler = new FirstEditAccessParser(this, error);
				parser.setContentHandler(handler);
				parser.parse(new InputSource(firstEditstream));
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
				error.setErrorCode(-39);
				e.printStackTrace();
			} catch (SAXException e) {
				error.setErrorCode(-39);
				e.printStackTrace();
			}

			progressBar.setProgress(40);
			long startT = System.currentTimeMillis();
			while (ENDFLAG) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
					ENDFLAG = false;
					return null;
				}catch(IllegalArgumentException e){
					e.printStackTrace();
					ENDFLAG = false;
					return null;
				}
				if ((System.currentTimeMillis() - startT) > 30000) {
					error.setErrorCode(-10);
					ENDFLAG = false;
					return null;
				}
			}
			progressBar.setProgress(75);
			return null;
		}

		@Override
		protected void onPostExecute(Void arg) {
			if (dialogFrame != null
					&& dialogFrame.getVisibility() == View.VISIBLE) {
				dialogFrame.setVisibility(View.GONE);
			}
			if (communitys != null && communitys.size() == 1
					&& communitys.get(0).equals("notpremium")) {
				notpremium = true;
				alertD = new ProgramInfoDialog(ACT, "プレミアム会員ではない可能性があります。").show();
			} else if (error != null) {
				if (error.getErrorCode() == 0&&communitys != null&&commuids != null) {
					commu_list = new String[communitys.size()];
					for (int i = 0; i < communitys.size(); i++) {
						commu_list[i] = communitys.get(i);
					}
					commu_ids = new String[commuids.size()];
					for (int i = 0; i < commuids.size(); i++) {
						commu_ids[i] = commuids.get(i);
					}
					try {
						community_select.setText(commu_list[commu_s_index]);
						categoryselect.setText(category_list[category_s_index]);
					} catch (ArrayIndexOutOfBoundsException e) {
						Log.d("NLiveRoid","CATEGORY out of bounds");
						error.setErrorCode(-29);
						error.showErrorToast();
					}
				} else {
					error.showErrorToast();
				}

			}
			progressBar.setProgress(90);
			removeProgress();
		}

		public void finishCallBack(ArrayList<String> communitys,
				ArrayList<String> commuids,String[] resValues, String reuse) {
			ENDFLAG = false;
			this.communitys = communitys;
			this.commuids = commuids;
			reuseid = reuse;
			reserveValues = resValues;
			if (communitys != null) {
				if (communitys.size() == 1
						&& communitys.get(0).equals("notpremium")) {
					return;
				}
				for (int i = 0; i < communitys.size(); i++) {
					if (communitys.get(i).replaceAll("\n|\t| |　", "")
							.equals("")) {
						communitys.remove(i);
					}
				}
			}
		}
	}//End of FirstEditAccess

	/**
	 * 枠取得ボタンをクリックして、最初の放送情報取得タスク フォームの値を送りつける
	 *
	 * @author Owner
	 *
	 */
	public class FirstSendFormTask extends AsyncTask<Void, Void, Void> {
		private boolean ENDFLAG = true;
		private String warning = "";
		@Override
		protected void onPreExecute() {
			if (dialogFrame != null
					&& (dialogFrame.getVisibility() == View.INVISIBLE || dialogFrame
							.getVisibility() == View.GONE)) {
				try{
				dialogFrame.setVisibility(View.VISIBLE);
				}catch(Exception e){
					Log.d("NLiveRoid","Failed dialogFrame VISIBLE");
					e.printStackTrace();
				}
			}
		}

		@Override
		public void onCancelled() {
			ENDFLAG = false;
			if (dialogFrame != null
					&& dialogFrame.getVisibility() == View.VISIBLE) {
				dialogFrame.setVisibility(View.GONE);
			}
			if (alertD != null && alertD.isShowing()) {// 表示中に終了か回転で落ちるかもしれない
				alertD.dismiss();
				alertD = null;
			}
			removeProgress();
			super.onCancelled();
		}

		@Override
		protected Void doInBackground(Void... arg0) {
			retryCount = 0;
			tryFirst();
			if (error != null) {
				if (error.getErrorCode() == 0) {
					if (!warning.equals("")) {//単純に1回目でページの有効期限がが出た状態
						if(warning.contains("ページの有効期限が")&&retryCount < 2){
							retryCount++;
							tryFirst();
							return null;
						}
					}
				}
			}
			return null;
		}

		private Void tryFirst(){
			if (error == null || error.getErrorCode() != 0)
				return null;
			String sessionid = Request.getSessionID(error);
			if (sessionid == null || sessionid.equals("")) {
				error.setErrorCode(-4);
				return null;
			}
			// フォームを送りつける
			InputStream source = sendFirstForm(sessionid);
//			 writeFile(source);
			try {
				org.ccil.cowan.tagsoup.Parser parser = new org.ccil.cowan.tagsoup.Parser();
				FirstSendFormParser handler = new FirstSendFormParser(this, error);
				parser.setContentHandler(handler);
				parser.parse(new InputSource(source));
				progressBar.setProgress(35);
			} catch (org.xml.sax.SAXNotRecognizedException e) {
				// Should not happen.
				e.printStackTrace();
			} catch (org.xml.sax.SAXNotSupportedException e) {
				// Should not happen.
				e.printStackTrace();
				throw new RuntimeException(e);
			} catch (IOException e) {
				e.printStackTrace();
				error.setErrorCode(-38);
			} catch (SAXException e) {
				e.printStackTrace();
				error.setErrorCode(-38);
			}

			long startT = System.currentTimeMillis();
			while (ENDFLAG) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
//					e.printStackTrace();
					Log.d("NLiveRoid","InterruptedException at FirstSendFormTask loop");
					ENDFLAG = false;
					return null;
				}catch(IllegalArgumentException e){
					e.printStackTrace();
					Log.d("NLiveRoid","IllegalArgumentException at FirstSendFormTask loop");
					ENDFLAG = false;
					return null;
				}
				if ((System.currentTimeMillis() - startT) > 30000) {
					error.setErrorCode(-10);
					ENDFLAG = false;
					return null;
				}
			}
			return null;
		}
		@Override
		protected void onPostExecute(Void arg) {
			if (error != null) {
				if (error.getErrorCode() == 0) {
					if (programTask2 != null &&
							programTask2.getStatus() != AsyncTask.Status.FINISHED) {
								programTask2.cancel(true);
							}
						addProgress();
						// 成功したらLVを取得する
						programTask2 = new SecondSendForm_GetLVTask();
						programTask2.execute(false);
				}else{
					error.showErrorToast();
					removeProgress();
				}
			}
		}

		/**
		 * 最初のeditstreamで失敗
		 * @param messages
		 * @param ul
		 */

		public void finishCallBack(ArrayList<String> messages, String ul) {
			ENDFLAG = false;
			ulck = ul;
//			Log.d("NLR","First Failed" + ul);
			for (int i = 0; i < messages.size(); i++) {
//				Log.d("NLR","messages.get("+i+")"+messages.get(i));
				warning += messages.get(i).replaceAll("\n|\t| |　", "") + "\n";
			}
			if(messages.size() <= 0){
				error.setErrorCode(-29);
			}
		}

		/**
		 * 最初のステップ成功
		 * @param response
		 */
		public void finishCallBack(String[] response) {
			ENDFLAG = false;
			ulck = response[0];
//			Log.d("NLR","First SuccessULCK");
			if(liveInfo!=null)liveInfo.setDescription(response[1]);//詳細は独自に変換されている
		}

	}//End of FirstSendFormTask


	private void reConnectFirst(){
//		Log.d("Log","RECONNECT FIRST ---- ");
		if(programTask1 != null && programTask1.getStatus() != AsyncTask.Status.FINISHED){
			programTask1.cancel(true);
		}
		programTask1 = new FirstSendFormTask();
		programTask1.execute();
	}
	/**
	 * lvを取得できる前の最低限の値で フォームを送りつける
	 *
	 * @param session
	 * @return
	 */
	public InputStream sendFirstForm(String session) {
		try {
			HttpURLConnection con = (HttpURLConnection) new URL(
					URLEnum.EDITSTREAM).openConnection();
			con.setRequestProperty("Cookie", session);
			con.setRequestMethod("POST");
			con.setInstanceFollowRedirects(true);
			con.setAllowUserInteraction(true);
			con.setDoOutput(true);
			con.setRequestProperty("Content-Type",
					"multipart/form-data; boundary=BOUNDARY");
			String boundary = "BOUNDARY";

			final String cParam = "--"
					+ boundary
					+ "\r\n"
					+"Content-Disposition: form-data; name=\"is_wait\"\r\n\r\n"
					+ "\r\n" + "--"
					+ boundary
					+ "\r\n"
					+ "Content-Disposition: form-data; name=\"confirm\"\r\n\r\n"
					+ ulck
					+ "\r\n"
					+ "--"
					+ boundary
					+ "\r\n"
					+ "Content-Disposition: form-data; name=\"back\"\r\n\r\n"
					+ "false\r\n"
					+ "--"
					+ boundary
					+ "\r\n"
					+ "Content-Disposition: form-data; name=\"usecoupon\"\r\n\r\n"
					+ "\r\n"
					+ "--"
					+ boundary
					+ "\r\n"
					+ "Content-Disposition: form-data; name=\"title\"\r\n\r\n"
					+ liveInfo.getTitle()
					+ "\r\n"
					+ "--"
					+ boundary
					+ "\r\n"
					+ "Content-Disposition: form-data; name=\"description\"\r\n\r\n"
					+ liveInfo.getDescription()
					+ "\r\n"
					+ "--"
					+ boundary
					+ "\r\n"
					+ "Content-Disposition: form-data; name=\"default_community\"\r\n\r\n"
					+ liveInfo.getCommunityID()
					+ "\r\n"
					+ "--"
					+ boundary
					+ "\r\n"
					+ "Content-Disposition: form-data; name=\"tags[]\"\r\n\r\n"
					// カテゴリなのに入力項目名はtags[]
					+ liveInfo.getCategoryName()
					+ "\r\n"
					+ "--"
					+ boundary
					+ "\r\n"
					//タグはContent-Dispositionを含めたlivetagsXの様な文字列として編集済み
					+liveInfo.getTags()
					+ "Content-Disposition: form-data; name=\"is_charge\"\r\n\r\n"
					+ "false"
					+ "\r\n"
					+ "--"
					+ boundary
					+ "\r\n"
					+ "Content-Disposition: form-data; name=\"reserve_start_ymd\"\r\n\r\n"
					+ reserveValues[0]
					+ "\r\n"
					+ "--"
					+ boundary
					+ "\r\n"
					+ "Content-Disposition: form-data; name=\"reserve_start_h\"\r\n\r\n"
					+ reserveValues[1]
					+ "\r\n"
					+ "--"
					+ boundary
					+ "\r\n"
					+ "Content-Disposition: form-data; name=\"reserve_start_i\"\r\n\r\n"
					+ reserveValues[2]
					+ "\r\n"
					+ "--"
					+ boundary
					+ "\r\n"
					+ "Content-Disposition: form-data; name=\"reserve_stream_time\"\r\n\r\n"
					+ reserveValues[3]
					+ "\r\n"
					+ "--"
					+ boundary
					+ "\r\n"
					+"Content-Disposition: form-data; name=\"public_status\"\r\n\r\n"
					+ String.valueOf((liveInfo.isMemberOnly()? "2":"1"))// 2コミュニティ限定
					+ "\r\n"
					+ "--"
					+ boundary
					+ "\r\n"
					+ "Content-Disposition: form-data; name=\"timeshift_enabled\"\r\n\r\n"
					+ String.valueOf((liveInfo.isTimeShiftEnable()? "1":"0"))
					+ "\r\n"
					+"--"
					+ boundary
					+ "\r\n"
					+"Content-Disposition: form-data; name=\"twitter_disabled\"\r\n\r\n"
					+ "\r\n"
					+ "--"
					+ boundary
					+ "\r\n"
					+ "Content-Disposition: form-data; name=\"input_twitter_tag\"\r\n\r\n"
					+ "\r\n"
					+ "--"
					+ boundary
					+ "\r\n"
					+ "Content-Disposition: form-data; name=\"twitter_tag\"\r\n\r\n"
					+ "\r\n"
					+ "--"
					+ boundary
					+ "\r\n"
					+ "Content-Disposition: form-data; name=\"ad_enable\"\r\n\r\n"
					+ "1\r\n"
					+ "--"
					+ boundary
					+ "\r\n"
					+ "Content-Disposition: form-data; name=\"auto_charge_confirmed\"\r\n\r\n"
					+ "0\r\n" + "--" + boundary + "\r\n"
					+"\r\n--" + boundary + "--\r\n";
			DataOutputStream out = new DataOutputStream(con.getOutputStream());
			out.write(cParam.getBytes(),0,cParam.getBytes().length);
//			Log.d("log", "CPARAMX " + cParam);


			InputStream is = con.getInputStream();

//			Log.d("NLR","FIRST RESPONSE --- " + con.getResponseMessage() +"  " + con.getResponseCode());
			String redirect = con.getURL().toString();
//			Log.d("NLR","FIRST URL --- " + redirect);
			if(redirect != null){
			Matcher mc = Pattern.compile("lv[0-9]+").matcher(redirect);
			if(mc.find()){
//				Log.d("FIRST " , "FIRST URL"+mc.group());
			}
			}
			Map<String, List<String>> headers = con.getHeaderFields();
			Iterator<String> headerIt = headers.keySet().iterator();
			while (headerIt.hasNext()) {
				String headerKey = (String) headerIt.next();
//				 Log.d("NLR","First KEY ------ " + headerKey);
//				 Log.d("NLR","First VALUE------------"+headers.get(headerKey));
				if (headerKey != null && headerKey.matches("location")) {//初回でLVがあった(たぶんこれはないけど。。)
					Matcher mc = Pattern.compile("lv[0-9]+").matcher(headers.get(headerKey).get(0));
					if (mc.find()) {
//						Log.d("Log","FIRST HEADER ----" + mc.group());
						if(liveInfo != null)liveInfo.setLiveID(mc.group());
					}
				}
			}
			return is;
		} catch (MalformedURLException e) {
			if (error != null) {
				error.setErrorCode(-8);
			}
			e.printStackTrace();
		} catch (IOException e) {
			if (error != null) {
				error.setErrorCode(-8);
			}
			e.printStackTrace();
		}
		return null;
	}

	/**
	 *
	 * これがなんか知んないけど初回は「WEBページの有効期限が過ぎています」になっちゃう
	 * なのでFirstに投げる
	 * フォームの入力に間違いが無ければ、
	 * lvを取得する→すでに枠が取られている場合(htmlにlv)とまだな場合(redirectheaderにlv)
	 *
	 * @author Owner
	 *
	 */
	public class SecondSendForm_GetLVTask extends AsyncTask<Boolean, Void, Void> {
		private boolean ENDFLAG = true;
		private String lv = "";
		private String warning = null;
		private boolean isWaitCancel = false;
		private boolean retry;
		public void setWaitCancel(){
			this.isWaitCancel = true;
			removeProgress();
		}
		public void continueWait(){
			waitDialog.show();
		}
		@Override
		protected void onPreExecute() {
			if (dialogFrame != null
					&& (dialogFrame.getVisibility() == View.INVISIBLE || dialogFrame
							.getVisibility() == View.GONE)) {
				dialogFrame.setVisibility(View.VISIBLE);
			}
		}
		@Override
		public void onCancelled() {
			ENDFLAG = false;
			if (alertD != null && alertD.isShowing()) {// 表示中に終了か回転で落ちるかもしれない
				alertD.dismiss();
				alertD = null;
			}
			if(waitDialog != null && waitDialog.isShowing()){
				waitDialog.dismiss();
				waitDialog = null;
			}
			removeProgress();
			super.onCancelled();
		}

		@Override
		protected Void doInBackground(Boolean... arg0) {
//			Log.d("log","SECOND FORM ------ " + ulck +"  " + liveInfo.getDescription());
//			Log.d("Log"," URLCK " + ulck);
			return trySecond(arg0[0]);
		}

		private Void trySecond(boolean isWait_){
			if (error == null)
				return null;
			progressBar.setProgress(40);
			String sessionid = Request.getSessionID(error);
			if (sessionid == null || sessionid.equals("")) {
				error.setErrorCode(-28);
				return null;
			}
			try {
				HttpURLConnection con = (HttpURLConnection) new URL(
						URLEnum.EDITSTREAM).openConnection();
				con.setRequestProperty("Cookie", sessionid);
				con.setRequestMethod("POST");
				con.setInstanceFollowRedirects(true);
				con.setAllowUserInteraction(true);
				con.setDoOutput(true);
				con.setRequestProperty("Content-Type",
						"multipart/form-data; boundary=BOUNDARY");
				String boundary = "BOUNDARY";
				// all_remain_point視聴者にタグ編集をさせない?
				// reserve_start_ymd予約機能を利用する?
				String waitStr = "";
				if(isWait_){
					waitStr = "--"
							+ boundary
							+ "\r\n"
							+"Content-Disposition: form-data; name=\"is_wait\"\r\n\r\n"
							+ "wait\r\n" + "--"
							+ boundary
							+ "\r\n";
				}else{
					waitStr = "--"
							+ boundary
							+ "\r\n"
							+"Content-Disposition: form-data; name=\"is_wait\"\r\n\r\n"
							+ "\r\n" + "--"
							+ boundary
							+ "\r\n";
				}
				final String cParam =
						 waitStr
						+ "Content-Disposition: form-data; name=\"confirm\"\r\n\r\n"
						+ ulck
						+ "\r\n"
						+ "--"
						+ boundary
						+ "\r\n"
						+ "Content-Disposition: form-data; name=\"back\"\r\n\r\n"
						+ "false\r\n"
						+ "--"
						+ boundary
						+ "\r\n"
						+ "Content-Disposition: form-data; name=\"usecoupon\"\r\n\r\n"
						+ "\r\n"
						+ "--"
						+ boundary
						+ "\r\n"
						+ "Content-Disposition: form-data; name=\"title\"\r\n\r\n"
						+ liveInfo.getTitle()
						+ "\r\n"
						+ "--"
						+ boundary
						+ "\r\n"
						+ "Content-Disposition: form-data; name=\"description\"\r\n\r\n"
						+ liveInfo.getDescription()
						+ "\r\n"
						+ "--"
						+ boundary
						+ "\r\n"
						+ "Content-Disposition: form-data; name=\"default_community\"\r\n\r\n"
						+ liveInfo.getCommunityID()
						+ "\r\n"
						+ "--"
						+ boundary
						+ "\r\n"
						+ "Content-Disposition: form-data; name=\"tags[]\"\r\n\r\n"
						// カテゴリなのに入力項目名はtags[]
						+ liveInfo.getCategoryName()
						+ "\r\n"
						+ "--"
						+ boundary
						+ "\r\n"
						//タグはContent-Dispositionを含めたlivetagsXの様な文字列として編集済み
						+liveInfo.getTags()
						+"Content-Disposition: form-data; name=\"public_status\"\r\n\r\n"
						+ String.valueOf((liveInfo.isMemberOnly()? "2":"1"))// 2コミュニティ限定
						+ "\r\n"
						+ "--"
						+ boundary
						+ "\r\n"
						+ "Content-Disposition: form-data; name=\"timeshift_enabled\"\r\n\r\n"
						+ String.valueOf((liveInfo.isTimeShiftEnable()? "1":"0"))
						+ "\r\n"
						+"--"
						+ boundary
						+ "\r\n"
						+"Content-Disposition: form-data; name=\"twitter_disabled\"\r\n\r\n"
						+ "\r\n"
						+ "--"
						+ boundary
						+ "\r\n"
						+ "Content-Disposition: form-data; name=\"input_twitter_tag\"\r\n\r\n"
						+ "\r\n"
						+ "--"
						+ boundary
						+ "\r\n"
						+ "Content-Disposition: form-data; name=\"twitter_tag\"\r\n\r\n"
						+ "\r\n"
						+ "--"
						+ boundary
						+ "\r\n"
						+ "Content-Disposition: form-data; name=\"ad_enable\"\r\n\r\n"
						+ "1\r\n"
						+ "--"
						+ boundary
						+ "\r\n"
						+ "Content-Disposition: form-data; name=\"kiyaku\"\r\n\r\n"
						+ "true\r\n"
						+ "--"
						+ boundary
						+ "\r\n"
						+ "Content-Disposition: form-data; name=\"auto_charge_confirmed\"\r\n\r\n"
						+ "0\r\n" + "--" + boundary + "\r\n"
						+"\r\n--" + boundary + "--\r\n";
//				 Log.d("log","CPARAM " + cParam);
				try{//連続押しでなる
				DataOutputStream out = new DataOutputStream(con.getOutputStream());
				out.write(cParam.getBytes(),0,cParam.getBytes().length);
				}catch(StackOverflowError e){
					e.printStackTrace();
					error.setErrorCode(-6);
					return null;
				}
				String location = null;
				//リダイレクト先を見る
//				Log.d("NLR","SECONDMEAAGAGE"+con.getResponseMessage() + " code:"+con.getResponseCode());
				String redirect = con.getURL().toString();
//				Log.d("NLR","SECOND URL --- " + redirect);
				if(redirect != null&&redirect.matches(".*lv[0-9]+.*")){//redirect先は1度取り逃がすともうこのURLとして入ってこない
					location = redirect;
				}
				Map<String, List<String>> headers = con.getHeaderFields();
				if(headers == null || headers.keySet() == null){
					Log.d("LiveTab","E -32");
					if(error != null)error.setErrorCode(-39);
					return null;
				}
				Iterator<String> headerIt = headers.keySet().iterator();
				while (headerIt.hasNext()) {
					String headerKey = (String) headerIt.next();
//					 Log.d("NLR","Second KEY ------ " + headerKey);
//					 Log.d("NLR","Second VALUE------------"+headers.get(headerKey));
					if (location == null && headerKey != null && headerKey.matches("location")&&headers.get(headerKey).get(0)!=null) {
						location = headers.get(headerKey).get(0);
//						 Log.d("NLR","SECOND HEADER------------"+location);
					}
				}
				InputStream is = con.getInputStream();
//				writeFile(is);
				if (location == null) {// ヘッダー・リダイレクト先に無かったら、パースして返す

					try {
//						Log.d("Log","SECOND No location ----");
						org.ccil.cowan.tagsoup.Parser parser = new org.ccil.cowan.tagsoup.Parser();
						SecondEdit_GetLVParser handler = new SecondEdit_GetLVParser(this, error);
						parser.setContentHandler(handler);
						parser.parse(new InputSource(is));
						progressBar.setProgress(35);
					} catch (org.xml.sax.SAXNotRecognizedException e) {
						// Should not happen.
//						e.printStackTrace();
						Log.d("NLiveRoid","SAXNotRecognizedException at SecondSendForm_GetLVTask");
						throw new RuntimeException(e);
					} catch (org.xml.sax.SAXNotSupportedException e) {
						// Should not happen.
//						e.printStackTrace();
						Log.d("NLiveRoid","SAXNotSupportedException at SecondSendForm_GetLVTask");
						throw new RuntimeException(e);
					} catch (IOException e) {
						e.printStackTrace();
					} catch (SAXException e) {
						e.printStackTrace();
					}

					long startT = System.currentTimeMillis();
					while (ENDFLAG) {
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
//							e.printStackTrace();
							Log.d("NLiveRoid","Interrupted SecondSendForm_GetLVTask");
							ENDFLAG = false;
							return null;
						}catch(IllegalArgumentException e){
							e.printStackTrace();
							Log.d("NLiveRoid","IllegalArgumentException at SecondSendForm_GetLVTask");
							ENDFLAG = false;
							return null;
						}
						if ((System.currentTimeMillis() - startT) > 30000) {
							error.setErrorCode(-10);
							ENDFLAG = false;
							return null;
						}
					}
					Log.d("NLR","End of Second whileLoop");
					if(retry){
						retry = false;
						if(retryCount > 2){
//							Log.d("NLR","RETRY ERROR --- ");
							error.setErrorCode(-29);
							return null;
						}
						if(lv.contains("RETRYW")){
							//リダイレクト先を見る
							String redirect1 = con.getURL().toString();
//							Log.d("NLR","SECOND URL --- " + redirect1);
							Matcher mc1 = Pattern.compile("lv[0-9]+").matcher(redirect1);//ここはこないかもだけど完全にlv[0-9]じゃないと駄目
							if(mc1.find()){
								lv = mc1.group();
							}
							//ulckがSecondで取得できていたら更新しておく
							mc1 = Pattern.compile("ulck_[0-9]+").matcher(lv);
							if(mc1.find()){
							ulck = mc1.group();
//							Log.d("NLR","ULCKMATCH---" + ulck);
							}
						trySecond(false);
						}else if(lv.contains("RETRYC")){//込み合っていた
							Thread.sleep(3000);
							trySecond(false);
						}else{
							Thread.sleep(1000);
							reConnectFirst();
						}
						return null;//リトライの場合この後何もしない
					}
				} else {//ヘッダーかリダイレクト先にLVがあった
//					Log.d("NLR","SECOND FIND LV ---");
					Matcher mc1 = Pattern.compile("lv[0-9]+").matcher(location);
					if (mc1.find()) {
						lv = mc1.group();
					}
				}
//				Log.d("NLR","LV " + lv);
			if (lv == null) {
					Log.d("Log","-29 1 ---------------------- ");
					error.setErrorCode(-29);
					return null;
				}
			} catch (MalformedURLException e) {
				e.printStackTrace();
				Log.d("Log","-29 2 ---------------------- ");
				error.setErrorCode(-29);
				return null;
			} catch (IOException e) {
				e.printStackTrace();
				Log.d("Log","-29 3 ---------------------- ");
				error.setErrorCode(-29);
				return null;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			progressBar.setProgress(75);

			if(isWait_){//順番待ちの場合、15秒毎にwaitinfo/lv[0-9]のソースをパース lvは順番待ちのフォームを送信した時点で、リダイレクトに返却されている
				isWaiting = true;
				if(error != null){
					error.setErrorCode(0);//キャンセルとのタイミングが計れないので、この時点でエラーは無いとする
				}
				if(liveInfo == null){
					liveInfo = new LiveInfo();
				}
				liveInfo.setLiveID(lv);
				PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
				 lock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My tag");
					//KEEPSCREEN にする
					lock.acquire();
			try {
								while(true){
									if(isWaitCancel){
										isWaiting = false;
										lock.release();
										if(waitDialog != null){
											waitDialog.dismiss();
										waitDialog = null;
										}
										break;
									}
									HttpURLConnection xmlcon = (HttpURLConnection)new URL(URLEnum.WAITSTATUS+lv).openConnection();
									xmlcon.setRequestProperty("Cookie", sessionid);
									xmlcon.setRequestMethod("POST");
									InputStream xml = xmlcon.getInputStream();
									byte[] source = null;
									ByteArrayOutputStream bos = new ByteArrayOutputStream();
									int size = 0;
									byte[] byteArray = new byte[1024];
									try {
										while ((size = xml.read(byteArray)) != -1) {
											bos.write(byteArray, 0, size);
										}
									} catch (IOException e) {
										e.printStackTrace();
									}
									source = bos.toByteArray();
									if(source == null ){
										Log.d("Log","-29 5 ---------------------- ");
										error.setErrorCode(-29);
											//ここの間、画面をONのままにできる
											lock.release();
										return null;
									}
									String count = XMLparser.getWaitingCount(source);
//									Log.d("NLiveRoid","WAITCOUNT --- " + count + " LV " + lv+ " error code:" + error.getErrorCode()  + " isNotif" + isWaiting);
											if(isWaiting){
												new NotifShow().execute(count);
											}

									if(count == null){//null且つerorrがない場合も順番キタ
										//ここの間、画面をONのままにできる
										lock.release();
										if(waitDialog != null){
											waitDialog.dismiss();
										waitDialog = null;
										}
										if(error.getErrorCode() == 0){
											//順番来た

										}
										break;
									}else if(count.equals("0")){
										//ここの間、画面をONのままにできる
										lock.release();
										if(waitDialog != null){
											waitDialog.dismiss();
										waitDialog = null;
										}
										break;
									}else{
										new DialogUpdate().execute(count);
										Thread.sleep(15000);
									}
								}//End of While

			// 順番待ち終わったタイミングで放送情報生成
					if(!isWaitCancel){
						isWaiting = false;
						byte[] playerState = Request.getPlayerStatusToByteArray(lv, error,
								sessionid);
						liveInfo = new LiveInfo();
						String code = "";
						try{
						code = XMLparser.getLiveInfoFromAPIByteArray(playerState, liveInfo);
					} catch (NullPointerException e1) {//頻繁に起きるが。。
						e1.printStackTrace();
					} catch (ParseException e1) {
						e1.printStackTrace();
					} catch (XmlPullParserException e1) {
						e1.printStackTrace();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
						if(code != null && code.equals("unknown")){

						}
					}
			} catch (InterruptedException e) {
				//ここの間、画面をONのままにできる
				if(lock != null){
				lock.release();
				}
				e.printStackTrace();
			}catch(IllegalArgumentException e){
				e.printStackTrace();
				if(lock != null){
					lock.release();
					}
			} catch (MalformedURLException e) {
				if(lock != null){
					lock.release();
					}
				e.printStackTrace();
			} catch (IOException e) {
				if(lock != null){
					lock.release();
					}
				e.printStackTrace();
			}
		}else if(error != null && error.getErrorCode() == 0){//順番待ちじゃない場合
//			Log.d("NLR","SecondNotWait --- " + lv);
			if (!lv.matches("^.*lv[0-9]+")) {
				Log.d("Log","-29 4 ---------------------- ");
				//順番待ちだった場合、ここでリターンされるけどエラーではない
				error.setErrorCode(-29);
				return null;
			}
			// タスクのキャンセルのタイミングによってはここが実行されて予約になっちゃう?
			//lv取得できて、順番待ちでなければ放送情報を取得に行く
			byte[] playerState = Request.getPlayerStatusToByteArray(lv, error,
					sessionid);
			if(liveInfo == null)liveInfo = new LiveInfo();
			try{
			XMLparser.getLiveInfoFromAPIByteArray(playerState, liveInfo);
		} catch (NullPointerException e1) {//シカトしちゃってる
			e1.printStackTrace();
		} catch (ParseException e1) {
			e1.printStackTrace();
		} catch (XmlPullParserException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		}
			return null;
		}
		@Override
		protected void onPostExecute(Void arg) {
//			Log.d("Second onPost","Second onLV " + (liveInfo!=null? "LV="+liveInfo.getLiveID():"liveInfo == null"));
			if(warning != null){
				alertD = new ProgramInfoDialog(ACT, warning)
				.show();
				removeProgress();
			}else if (error != null) {
				if (error.getErrorCode() == 0 ){
					if( liveInfo != null&& liveInfo.getLiveID()!= null && liveInfo.getLiveID().matches("lv[0-9]+")) {
						//成功していたら、必要なRTMPADDR等を取得に行く
						publishTask = new PublishParse();
						publishTask.execute();
					}else if(isWaitCancel){//順番待ちキャンセル
						MyToast.customToastShow(ACT, "順番待ちをキャンセルしました");
						removeProgress();
						return;
					}else{
						retryCount++;
						MyToast.customToastShow(ACT, "再接続します");
					}
					removeProgress();
				} else if(error.getErrorCode()<0){
					error.showErrorToast();
					removeProgress();
				}else if(!isWaiting&&(liveInfo != null && liveInfo.getLiveID() == null)){//速い操作でありえる
					Log.d("Log","-29 6 ---------------------- ");
					error.setErrorCode(-29);
					error.showErrorToast();
					removeProgress();
				}else if(error.getErrorCode() != 0){
					error.showErrorToast();
					removeProgress();
				}
			}
		}
		//ulckと暗号化された詳細を取得
		public void finishCallBack(String[] ulck_desc) {
//			Log.d("NLR","SecondULCK_DESC --- "+ulck);
			retryCount++;
				trySecond(false);
		}
		public void finishCallBack(String lv) {
//			Log.d("log","SECOND FINISH LV:" + lv);
			if(lv != null && lv.matches("lv[0-9]+$")){
				//ここで既にこの時間に予約をしているか、放送中の番組があります。(該当の番組はだったら
				//IDが自分かどうかを確かめる必要がある
				this.lv = lv;
			}else if(lv != null && (lv.equals("RETRY")||lv.equals("RETRYW"))||lv.equals("RETRYC")){//LV取得できずにpage_footer時
				//RETRYになるのが不明 ulckなし? WEBページの有効期限が切れていますだったらFirstからやりなおさないといけないっぽい(ENDFLAGのループでそれぞれ分岐)
//				Log.d("NLR","RetrySecond::" + lv);
					retryCount++;
					retry = true;
					ENDFLAG = false;//必要!!
			}else{//失敗してたらその旨をダイアログ表示
				ENDFLAG = false;
				if(lv != null && lv.contains("順番待ちのユーザーがいるため")){
//					Log.d("NLR","SecondStartWait" +lv);
					tryWaitTask();
				}else{//順番待ち以外は全てエラー
					this.warning = lv;
				}
			}
			ENDFLAG = false;
		}



		class NotifShow extends AsyncTask<String,Void,Void>{

			@Override
			protected Void doInBackground(String... params) {
				NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
				notificationManager.cancelAll();
				Notification notif = new Notification();
				notif.icon = R.drawable.icon_nlr;
				// ペンディングできるIntentの入れ物
					        PendingIntent intent = PendingIntent.getActivity(
					                ACT,
					                CODE.REQUEST_NO_MEAN_NOTIFICATION , new Intent(ACT, TopTabs.class),
					                Intent.FLAG_ACTIVITY_NEW_TASK
					        );//なぜか2.2だとintentのnullが許されないみたいなのでTopTabsをいれとく
				notif.setLatestEventInfo(ACT,"順番待ちあと",params[0]==null? "0":params[0]+"人",intent);
				notificationManager.notify(R.string.app_name, notif);
				return null;
			}

		}




	}//End of SecondSendForm_GetLVTask

	/**
	 * 順番待ちのスレッドを新たにis_wait=trueで生成→実行
	 * 自分自身がキャンセルされる懸念があるので
	 * スレッドの外に実装
	 * @author Owner
	 *
	 */

	private void tryWaitTask(){
//	Log.d("NLR","TryWaitTask");
		isWaiting = true;
		if(programTask2 != null){
			if(programTask2.getStatus() != AsyncTask.Status.FINISHED){
					programTask2.cancel(true);
			programTask2 = null;
			}
		//この段階で、順番待ちの～が返っていて、どこかしらでキャンセルか、
			//return nullされているので、エラーではないが、どうしてもキャンセルとの
			//タイミングが計れないので、onCancelledでisWaitingの場合、errorコードを0にする

			programTask2 = new SecondSendForm_GetLVTask();
			programTask2.execute(true);
		}
	}

	/**
	 * 順番待ちのダイアログを更新する
	 * @param is
	 */

	class DialogUpdate extends AsyncTask<String,Void,String>{
		@Override
		protected String doInBackground(String... params) {
			return params[0];
		}
		@Override
		protected void onPostExecute(String arg){
			if (dialogFrame != null
					&& dialogFrame.getVisibility() == View.VISIBLE) {
				dialogFrame.setVisibility(View.GONE);
			}
			if(waitDialog == null){
			waitDialog = new WaitDialog(ACT,programTask2);
			waitDialog.show();
			}
			waitDialog.updateCount(arg);
		}
	}

	public void writeFile(InputStream is) {
		FileOutputStream fos;
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			String temp = "";
			String source = "";
//			int i = 0;
			while ((temp = br.readLine()) != null) {
//				i++;
//				if(i > 250&&i<500){
				source += temp;
//				}
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
		ErrorCode error = ((NLiveRoid)getApplicationContext()).getError();
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

	private void addProgress() {
		removeProgress();
		if (mainScroll == null) {
			mainScroll = (ScrollView) parent.findViewById(R.id.mainscroll);
		}
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
		if (dialogFrame != null
				&& dialogFrame.getVisibility() == View.VISIBLE) {
			dialogFrame.setVisibility(View.GONE);
		}
		if (mainScroll == null) {
			mainScroll = (ScrollView) parent.findViewById(R.id.mainscroll);
		}
		TranslateAnimation animation = new TranslateAnimation(
				Animation.RELATIVE_TO_PARENT, 0.0f,
				Animation.RELATIVE_TO_PARENT, 0.0f,
				Animation.RELATIVE_TO_PARENT, 0.0f,
				Animation.RELATIVE_TO_PARENT, 1.0f);
		animation.setDuration(400);
		animation.setFillAfter(true);
		try{
		progressBar.startAnimation(animation);
		progressBar.setProgress(100);
		progressArea.removeAllViews();
		progressBar.setProgress(0);
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	/**
	 * RTMPアドレス，ticket , token ,end_time取得
	 * @author Owner
	 *
	 */
	class PublishParse extends AsyncTask<Void, Void, Void> {

		@Override
		public void onCancelled(){
			removeProgress();
			super.onCancelled();
		}

		@Override
		protected Void doInBackground(Void... arg0) {
//			Log.d("PublishParse","doIn");
			if (error == null)return null;
			if (liveInfo == null || liveInfo.getLiveID() == null
					|| liveInfo.getLiveID().equals("")) {
				error.setErrorCode(-8);
				return null;
			}
			if(liveSetting == null)liveSetting = new LiveSettings(null);//SetBeforeApp押さずにSetBeforeServer押したかどちらも呼ばない場合、まだnull
			progressBar.setProgress(40);
			String sessionid = Request.getSessionID(error);
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
			Log.d("NLR","GETTOKENINFO --- " + liveInfo.getRtmpurl());
			progressBar.setProgress(55);
			if (liveInfo.getToken() == null) {
				error.setErrorCode(-39);
				return null;
			} else {
				return null;
			}
		}

		protected void onPostExecute(Void arg) {
			progressBar.setProgress(75);
			Log.d("PublishParse","onPost");
			if (error != null) {
				if (error.getErrorCode() == 0) {
					//プロファイルを保存する
					new WriteProFile().execute();
				}else{
					error.showErrorToast();
				}
			}
			removeProgress();
		}
	}

	// フラッシュをアプリケーションの設定値で起動する
	public void initialLive() {

		NLiveRoid app = (NLiveRoid) getApplicationContext();
		boolean[] setting_boolean = new boolean[28];
		try{
			//fexit,(finish_back),at,at_overwriteはDefaultMapValue
		setting_boolean[0] = app.getDetailsMapValue("fexit")==null? true:Boolean.parseBoolean(app.getDetailsMapValue("fexit"));
		setting_boolean[1] = app.getDetailsMapValue("newline")== null? true:Boolean.parseBoolean(app.getDetailsMapValue("newline"));
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
			setting_boolean = new boolean[]{true,true,false,false,true,true,false,false,false,false,true,false,false,true,true,true,true,true,true,false,true,false,false,false,false,true,true,false};
		}

		byte[] setting_byte = new byte[44];
		String twitterToken = null;
		short init_comment_count = 20;
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
		setting_byte[35] = app.getDetailsMapValue("cellheight_test")==null? 3:Byte.parseByte(app.getDetailsMapValue("cellheight_test"));
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
				setting_byte = new byte[]{0,15,0,0,0,15,70,3,0,92,-43,0,15,0,0,0,15,70,3,0,92,-43,0,0,0,-1,5,5,0,5,50,0,-1,0,0,3,5,-1,100,40,15,127,0,0};
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
							cmd = new CommandMapping(cmdValue[0],cmdValue[1],cmdValue[2],cmdValue[3],true);
							break;
							}
						}
						if(i==3){//1つでもnullがあったら普通の初期化
							cmd = new CommandMapping(true);
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

			liveInfo.serializeBitmap();
			Intent initialLiveIntent = new Intent(this,BCPlayer.class);
			liveInfo.setOwner(true);
			initialLiveIntent.putExtra("init", liveSetting);
			initialLiveIntent.putExtra("setting_boolean", setting_boolean);
			initialLiveIntent.putExtra("setting_byte", setting_byte);
			initialLiveIntent.putExtra("init_comment_count", init_comment_count);
			if(offTimer > 0)initialLiveIntent.putExtra("offtimer_start", offTimer);
			initialLiveIntent.putExtra("column_seq", seq);
			initialLiveIntent.putExtra("cmd", cmd);
			initialLiveIntent.putExtra("speech_skip_word", skip_word);
			initialLiveIntent.putExtra("density", app.getMetrics());
			initialLiveIntent.putExtra("twitterToken", twitterToken);
		//コメのみならプレイヤーパラメタいらないけど。。とりあえず落としたくはないので
			initialLiveIntent.putExtra("resizeW", app.getResizeW());
			initialLiveIntent.putExtra("resizeH", app.getResizeH());
			initialLiveIntent.putExtra("viewW",app.getViewWidth());
			initialLiveIntent.putExtra("viewH",app.getViewHeight());
			initialLiveIntent.putExtra("LiveInfo",liveInfo );
			initialLiveIntent.putExtra("Cookie",cookie);
			 initialLiveIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			 if(!TopTabs.getACT().isMovingSameLV(liveInfo.getLiveID())){//違うLVだったら
				//裏にいたら停止
				 initialLiveIntent.putExtra("restart", true);
			 }
			 //順番待ちでHOMEキー押していたらノティフィ
			 if(isWaiting){
				 Notification notif = new Notification();
						PendingIntent pendingIntent =
						    PendingIntent.getActivity(this,0,initialLiveIntent,0);
						notif.setLatestEventInfo(app, "順番待ち終了", liveInfo.getLiveID(), pendingIntent);

				NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
				notificationManager.cancelAll();
				notif.icon = R.drawable.icon_nlr;
				notif.sound = Settings.System.DEFAULT_NOTIFICATION_URI;
				notificationManager.notify(R.string.app_name, notif);
			 }else{
		    	startActivity(initialLiveIntent);
			 }

		} catch (RuntimeException e) {
				Log.d("NLiveRoid", "RUNNTIME ERR LIVE TAB");
				e.printStackTrace();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// ブラウザに行って帰ってきた時はerrorもnull
		if (error == null || resultCode == CODE.RESULT_ALLFINISH) {
			return;
		}
		// Log.d("log","OWN RETURN " + resultCode+ " req " + requestCode + " " +
		// data);
	}







}
