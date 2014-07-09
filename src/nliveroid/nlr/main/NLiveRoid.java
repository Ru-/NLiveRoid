package nliveroid.nlr.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

import nliveroid.nlr.main.parser.XMLparser;

import org.xmlpull.v1.XmlPullParserException;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build.VERSION;
import android.os.Environment;
import android.util.Log;

public class NLiveRoid extends Application {
	private Activity foreACT;
	private static HashMap<String,String> defaultMap;
	private static HashMap<String,String> detailsMap;
	private final String accountFileName = "defaultac";
	private final String detailsFileName = "detailsfile";
	private static AppErrorCode error;
	private Context mContext;

	private String sessionid ="";
	private String sp_session_key = null;

	private int viewWidth;
	private int viewHeight;
	private int resizeW;
	private int resizeH;
	private float scaleDensity;
	private static GateView gateView;

	public static final int apiLevel = Integer.parseInt(VERSION.SDK);

	private HashMap<String,Bitmap> tagBitMap;
	private HashMap<String,String> tagNameMap;
	private HashMap<String,String> colorStringMap;

	private Bitmap[] rankingBitMaps;

	public static boolean isPreLooked = false;
	public static boolean isNotPremium = false;

	final public static boolean isDebugMode = false;
	public static boolean log;
	public static FileChannel logChannel;
	public static ByteBuffer logBuffer;

	@Override
	public void onCreate() {
		// フラッシュから生成される時は何もする必要がないのでここで処理は書かない
	}

	@Override
	public void onTerminate() {
		{
		Log.d("NLiveRoid", "Call terminate");
		}
	}
	//どのプロセスでもセッションは共通させなければアプリの中でログアウトされてしまうので
	//かならずセッションを引数に取る
	//メインアプリ
	public void initStandard() {
		try {
			mContext = createPackageContext("nliveroid.nlr.main", CONTEXT_RESTRICTED);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		error = new AppErrorCode();
		//マップを生成してアカウントを読み出す
		defaultMap = new HashMap<String,String>();
		readDefaultMapFromChipher();


		detailsMap = new HashMap<String,String>();
		ArrayList<String> arg  = importDetailsFile();
		if(arg == null){
//			MyToast.customToastShow(this, "設定値読み込み失敗\n原因不明");
			return;
		}else{
			if(arg.size() == 1&&arg.get(0).equals("wrong1")){
//			MyToast.customToastShow(this, "設定値IOに失敗しました\nストレージをお確かめ下さい");
			return;
			}else if(arg.size() == 1 &&arg.get(0).equals("wrong2")){
//			MyToast.customToastShow(this, "設定値ファイルXMLのパース失敗");
			return;
			}else if(arg.size() > 0){
			String missedStr = "";
			for(int i = 0; i < arg.size() ;i++){
				missedStr += arg.get(i) + " ";
			}
			MyToast.customToastShow(this, "次の値が読み取れませんでした\n"+missedStr);
			Log.d("NLiveRoid","Failed read SettingValue --- " + missedStr);
			}
		}
		settingFileCheck();

		Request.setApplication(this);
		//画像読み込み重いかもだからタスク
		//読み込み時にここが終わってるという保証をすべき
		new Thread(new Runnable(){
			@Override
			public void run() {
				readTagBitmaps();
			}
		}).start();
		error.showErrorToast();
		try{
			String ss = getDetailsMapValue("nlr_log");
			Log.d("NLiveRoid","NNN " + ss);
			log = Boolean.parseBoolean(ss);
		}catch(Exception e){
			e.printStackTrace();
		}
		Log.d("NLiveRoid","  apiLevel" + apiLevel + "  log" + log);
		if(NLiveRoid.isDebugMode && apiLevel >= 16||log){//ログをファイルに書き込む
			FileOutputStream fos;
			try {
				File dir = new File(Environment.getExternalStorageDirectory()+"/NLiveRoid");
				File file = new File(dir.getAbsoluteFile()+"/NLRLOG.txt");
				Log.d("NLiveRoid","DIR --- " + dir.getAbsolutePath());
				if(!dir.exists()){
					dir.mkdirs();
				}
				if(!file.exists()){
					Log.d("NLiveRoid","CreateNewLogFile --- ");
					file.createNewFile();
				}
				fos = new FileOutputStream(file);
				logChannel = fos.getChannel();
				logBuffer = ByteBuffer.allocateDirect(4096);
				SimpleDateFormat sdf = new SimpleDateFormat("MMddHHmmss");
				outLog("ログ取得開始 " + sdf.format(new Date()) +"\n");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}



	public static void outLog(String str){
//		str += "\n";
		Log.d("NLiveRoid","outLog" +  str);
		if(logChannel != null && logBuffer != null){
			try {
				byte[] bytes = str.getBytes();
				logBuffer.clear();
//				Log.d("NLiveRoid","outLog " + bytes.length);
//				Log.d("NLiveRoid","outLog " + logBuffer.position() + "  " + logBuffer.limit() +"  " + logBuffer.capacity());
				byte[] tmp = null;
				for(int i = 0; i < bytes.length/4096; i++){
				tmp = new byte[4096];
				System.arraycopy(bytes, (i-1)*4096, tmp, 0, 4096);
				Log.d("NLiveRoid","outLog for" + tmp.length);
					logBuffer.put(tmp);
					logBuffer.position(0);
					logBuffer.limit(tmp.length);
				logChannel.write(logBuffer);// position から limit の間の要素を書きこむ
				}
				tmp = new byte[bytes.length-4096<0? bytes.length:4096];
				System.arraycopy(bytes, bytes.length-4096<0? 0:bytes.length-4096, tmp, 0, bytes.length-4096<0? bytes.length:4096);
				logBuffer.clear();
				logBuffer.put(tmp);
				logBuffer.position(0);
				logBuffer.limit(tmp.length);
			logChannel.write(logBuffer);// position から limit の間の要素を書きこむ
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	//Gateのレイアウトの読み出し、widthが入ってきた後じゃないと呼べない
	public void createGateInstance(){
		gateView = new GateView(this);
	}

	/**
	 * 配信機能時の初期化
	 * タグのビットマップ初期化省略とgateViewの初期化を組み込んじゃう
	 * 以外通常の初期化と同じ
	 */
	public void initNoTagBitmap(){
		try {
			mContext = createPackageContext("nliveroid.nlr.main", CONTEXT_RESTRICTED);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		error = new AppErrorCode();
		defaultMap = new HashMap<String,String>();
		readDefaultMapFromChipher();

		detailsMap = new HashMap<String,String>();
		ArrayList<String> arg  = importDetailsFile();
		if(arg == null){
			MyToast.customToastShow(this, "設定値読み込み失敗\n原因不明");
			return;
		}else{
			if(arg.size() == 1&&arg.get(0).equals("wrong1")){
			MyToast.customToastShow(this, "設定値IOに失敗しました\nストレージをお確かめ下さい");
			return;
			}else if(arg.size() == 1 &&arg.get(0).equals("wrong2")){
			MyToast.customToastShow(this, "設定値ファイルXMLのパース失敗");
			return;
			}
//			else if(arg.size() == 1 &&arg.get(0).equals("0")){
//			MyToast.customToastShow(this, "内部設定値をインポートしました");
//			return;
//			}
//			String missedStr = "";
//			for(int i = 0; i < arg.size() ;i++){
//				missedStr += arg.get(i) + " ";
//			}
//			MyToast.customToastShow(this, "次の値が読み取れませんでした\n"+missedStr);
		}
		settingFileCheck();

		readTagsStringMap();
		Request.setApplication(this);
		try{
		error.showErrorToast();
		}catch(RuntimeException e){
			//doInBackgroundからここが呼ばれた場合、エラーする
			e.printStackTrace();
		}
	}


	private void readTagsStringMap(){
		tagNameMap = new HashMap<String,String>();
		 tagNameMap.put("channel","チャンネル");
		 tagNameMap.put("common", "一般");
		 tagNameMap.put("dance", "踊ってみた");
		 tagNameMap.put("draw", "描いてみた");
		 tagNameMap.put("face", "顔出し");
		 tagNameMap.put("iphone", "iphone");
		 tagNameMap.put("lecture", "講座");
		 tagNameMap.put("live", "ゲーム");
		 tagNameMap.put("official", "公式");
		 tagNameMap.put("request", "動画紹介");
		 tagNameMap.put("sing", "歌ってみた");
		 tagNameMap.put("totu", "凸待ち");
		 tagNameMap.put("politics", "政治");
		 tagNameMap.put("cooking", "料理");
		 tagNameMap.put("animal", "動物");

		 colorStringMap = new HashMap<String,String>();
		 colorStringMap.put("WHITE","#ffffff");
		 colorStringMap.put("white","#ffffff");
		 colorStringMap.put("White","#ffffff");
		 colorStringMap.put("RED","#ff0000");
		 colorStringMap.put("red","#ff0000");
		 colorStringMap.put("Red","#ff0000");
		 colorStringMap.put("BLUE","#0000ff");
		 colorStringMap.put("blue","#0000ff");
		 colorStringMap.put("Blue","#0000ff");
		 colorStringMap.put("CYAN","#00ffff");
		 colorStringMap.put("Cyan","#00ffff");
		 colorStringMap.put("cyan","#00ffff");
		 colorStringMap.put("YELLOW","#ffff00");
		 colorStringMap.put("yellow","#ffff00");
		 colorStringMap.put("Yellow","#ffff00");
		 colorStringMap.put("GREEN","#00ff00");
		 colorStringMap.put("green","#33ff00");
		 colorStringMap.put("Green","#33ff00");
		 colorStringMap.put("PINK","#ff8080");
		 colorStringMap.put("pink","#ff8080");
		 colorStringMap.put("Pink","#ff8080");
		 colorStringMap.put("ORANGE","#ffcc00");
		 colorStringMap.put("orange","#ffcc00");
		 colorStringMap.put("Orange","#ffcc00");
		 colorStringMap.put("PURPLE","#c000ff");
		 colorStringMap.put("purple","#c000ff");
		 colorStringMap.put("Purple","#c000ff");
		 colorStringMap.put("GRAY","#bdbdbd");
		 colorStringMap.put("Gray","#bdbdbd");
		 colorStringMap.put("gray","#bdbdbd");
	}

	private void readTagBitmaps(){
		tagBitMap = new HashMap<String,Bitmap>();
		InputStream[] tags = new InputStream[17];
		 tags[0] = getResources().openRawResource(R.drawable.tag_channel);
		 tags[1] = getResources().openRawResource(R.drawable.tag_common);
		 tags[2] = getResources().openRawResource(R.drawable.tag_dance);
		 tags[3] = getResources().openRawResource(R.drawable.tag_draw);
		 tags[4] = getResources().openRawResource(R.drawable.tag_face);
		 tags[5] = getResources().openRawResource(R.drawable.tag_iphone);
		 tags[6] = getResources().openRawResource(R.drawable.tag_lecture);
		 tags[7] = getResources().openRawResource(R.drawable.tag_live);
		 tags[8] = getResources().openRawResource(R.drawable.tag_official);
		 tags[9] = getResources().openRawResource(R.drawable.tag_only);
		 tags[10] = getResources().openRawResource(R.drawable.tag_request);
		 tags[11] = getResources().openRawResource(R.drawable.tag_sing);
		 tags[12] = getResources().openRawResource(R.drawable.tag_totu);
		 tags[13] = getResources().openRawResource(R.drawable.tag_politics);
		 tags[14] = getResources().openRawResource(R.drawable.tag_cooking);
		 tags[15] = getResources().openRawResource(R.drawable.tag_animal);
		 tags[16] = getResources().openRawResource(R.drawable.tag_user);

			 try {
				 tagBitMap.put("channel", BitmapFactory.decodeStream(tags[0]));
				 tagBitMap.put("common", BitmapFactory.decodeStream(tags[1]));
				 tagBitMap.put("dance", BitmapFactory.decodeStream(tags[2]));
				 tagBitMap.put("draw", BitmapFactory.decodeStream(tags[3]));
				 tagBitMap.put("face", BitmapFactory.decodeStream(tags[4]));
				 tagBitMap.put("iphone", BitmapFactory.decodeStream(tags[5]));
				 tagBitMap.put("lecture", BitmapFactory.decodeStream(tags[6]));
				 tagBitMap.put("live", BitmapFactory.decodeStream(tags[7]));
				 tagBitMap.put("official", BitmapFactory.decodeStream(tags[8]));
				 tagBitMap.put("only", BitmapFactory.decodeStream(tags[9]));
				 tagBitMap.put("request", BitmapFactory.decodeStream(tags[10]));
				 tagBitMap.put("sing", BitmapFactory.decodeStream(tags[11]));
				 tagBitMap.put("totu", BitmapFactory.decodeStream(tags[12]));
				 tagBitMap.put("politics", BitmapFactory.decodeStream(tags[13]));
				 tagBitMap.put("cooking", BitmapFactory.decodeStream(tags[14]));
				 tagBitMap.put("animal", BitmapFactory.decodeStream(tags[15]));
				 tagBitMap.put("user", BitmapFactory.decodeStream(tags[16]));
			 } finally {
			 try {
				 for(int i = 0; i < tags.length; i++){
			 tags[i].close();
				 }
			 } catch(IOException e) {
			 error.setErrorCode(-23);
			 }
		 }
			 readTagsStringMap();

	}


//アカは専用のメソッドから取得されるのでこのgetterは必要ない
//	public String getDefaultMapValue(String key) {
//		return defaultMap.get(key);
//	}


	public String getDetailsMapValue(String key) {
		return detailsMap.get(key);
	}

	public void setDetailsMapValue(String key, String value) {
		detailsMap.put(key, value);
	}

	/**
	 * sp_session_keyを取得します。
	 * @return sp_session_key
	 */
	public String getSp_session_key() {
	    return sp_session_key;
	}

	/**
	 * sp_session_keyを設定します。
	 * @param sp_session_key sp_session_key
	 */
	public void setSp_session_key(String sp_session_key) {
		Log.d("NLiveRoid","SP--- " + sp_session_key);
	    this.sp_session_key = sp_session_key;
	}

	/**
	 * スクリーン全体のビューの基準width,height
	 *
	 */
	public int getViewWidth() {
		return viewWidth;
	}

	public void setViewWidthDp(int width) {
		this.viewWidth = width;
	}

	public int getViewHeight() {
		return viewHeight;
	}

	public void setViewHeightDp(int height) {
		this.viewHeight = height;
	}


	/**
	 * sessionidを取得します。
	 * @return sessionid
	 */
	public String getSessionid() {
	    return sessionid;
	}

	/**
	 * sessionidを設定します。
	 * @param sessionid sessionid
	 */
	public void setSessionid(String sessionid) {
	    this.sessionid = sessionid;
	}

	public int getResizeW() {
		return resizeW;
	}

	public void setResizeW(int width) {
		this.resizeW = width;
	}

	public int getResizeH() {
		return resizeH;
	}

	public void setResizeH(int height) {
		this.resizeH = height;
	}

	/**
	 * ユーザID、パスワード利用するチェックに合わせて取得
	 *
	 * @return
	 */
	public String getUserIDFromMap() {
		String userid = "";
		if (defaultMap == null)
			return null;
		if (Boolean.parseBoolean(detailsMap.get("always_use1"))) {
			if(NLiveRoid.isDebugMode)Log.d("NLiveRoid","N always_use1");
			userid = defaultMap.get("user_id1");
		} else if (Boolean.parseBoolean(detailsMap.get("always_use2"))) {
			if(NLiveRoid.isDebugMode)Log.d("NLiveRoid","N always_use2");
			userid = defaultMap.get("user_id2");
		}
		return userid;
	}

	public String getPasswordFromMap() {
		String password = "";
		if (defaultMap == null)
			return null;
		if (Boolean.parseBoolean(detailsMap.get("always_use1"))) {
			password = defaultMap.get("password1");
		} else if (Boolean.parseBoolean(detailsMap.get("always_use2"))) {
			password = defaultMap.get("password2");
		}
		return password;
	}

	/**
	 * クッキーを消す必要がある場合に リストを空にする
	 */
	public void removeTopTabsAdapter() {
		if (SearchTab.getSearchTab() != null) {
			if (SearchTab.getSearchTab().getAdapter() != null) {
				SearchTab.getSearchTab().getAdapter().clear();
			}
		}
		if (CommunityTab.getCommunityTab() != null) {
			if (CommunityTab.getCommunityTab().getAdapter() != null) {
				CommunityTab.getCommunityTab().getAdapter().clear();
			}
		}
	}
	/**
	 * デフォルトプリファランスの終了時に設定マップの値全てをファイルに保存
	 */
	public void updateAccountFile() {
		writeAccountFile();
		if(error != null){
		error.showErrorToast();
		}
	}

	/**
	 * テーブルプリファランスの終了時に設定マップの値全てをファイルに保存
	 */
	public void updateDetailsFile() {
		writeDetailsFile();
		if(error != null){
		error.showErrorToast();
		}
	}

	public void deleteAllPreference() {
		deleteFile(accountFileName);
		deleteFile(detailsFileName);
		deleteFile("defaultpref");
		deleteFile("tabledisplay");
		if(defaultMap == null){
			defaultMap = new HashMap<String,String>();
		}
		defaultMap.clear();
		readDefaultMapFromChipher();
		if(detailsMap == null){
			detailsMap = new HashMap<String,String>();
		}
		detailsMap.clear();
		importDetailsFile();
		settingFileCheck();
	}
	public class AppErrorCode implements ErrorCode{
		private int errorCode = 0;

		public void showErrorToast() {
			int code = getErrorCode();
			Log.d("NLiveRoid","SHOW ERROR - " + code);
			switch (code) {
			case -1:
				MyToast.customToastShow(foreACT,
						"アカウント情報のIDまたはパスワードが空白です\n設定してください");
				break;
			case -2:
				MyToast.customToastShow(foreACT, "IDまたはパスワードが不正です");
				break;
			case -3:
				MyToast.customToastShow(foreACT,"ネットワークに接続できませんでした");
				break;
			case -4:
				// セッションを上書きして繋ぎ直す処理を書く
				MyToast.customToastShow(foreACT,
				"セッションが無効な為、消去しました\n接続は失敗しました");
				break;
			case -5:
				MyToast.customToastShow(foreACT, "メンテナンス中と思われます");
				break;
			case -6:
				MyToast.customToastShow(foreACT, "ネットワーク上でIO処理に失敗しました");
				break;
			case -7:
				MyToast.customToastShow(foreACT, "新着情報の取得に失敗しました");
				break;
			case -8:
				MyToast.customToastShow(foreACT, "放送情報の取得に失敗しました");
				break;
			case -9:
				MyToast.customToastShow(foreACT,
						"設定ファイルの読み込み失敗\n設定ファイルが削除されてしまった可能性があります");
				break;
			case -10:
				MyToast.customToastShow(foreACT, "リクエストタイムアウト");
				break;
			case -11:
				MyToast.customToastShow(foreACT, "サムネイル取得に失敗した(IOエラー)");
				break;
			case -12:
				MyToast.customToastShow(foreACT, "設定ファイルの書き込み失敗");
				break;
			case -13:
				MyToast.customToastShow(foreACT, "コメントサーバへの接続に失敗しました");
				break;
			case -14:
				MyToast.customToastShow(foreACT, "メンテナンス中又は、\nアカウントがロックされてしまった");
				break;
			case -15:
				MyToast.customToastShow(foreACT, "サーバ側の不明なエラー\ncode:unknown");
				break;
			case -16:
				MyToast.customToastShow(foreACT, "コメント情報の取得に失敗しました");
				break;
			case -17:
				MyToast.customToastShow(foreACT, "ログインに失敗ました。再試行又は二重ログインしていないかお確かめ下さい");//not_login
				break;
			case -18:
				MyToast.customToastShow(foreACT, "この放送は終了しています");
				break;
			case -19:
				MyToast.customToastShow(foreACT, "この放送は始まっていません");
				break;
			case -20:
				MyToast.customToastShow(foreACT, "コミュニティ限定放送です");
				break;
			case -21:
				MyToast.customToastShow(foreACT, "コメントログの取得に失敗しました");
				break;
			case -22:
				MyToast.customToastShow(foreACT, "OutOfMemorryError");
				break;
			case -23:
				MyToast.customToastShow(foreACT, "放送中の情報取得に失敗していた");
				break;
			case -24:
				MyToast.customToastShow(foreACT, "設定ファイルの読み込みに失敗しました\n設定ファイル(アカウント等)が消去されました\nお手数ですが設定をもう一度ご入力下さい)");
				break;
			case -25:
				MyToast.customToastShow(foreACT, "設定ファイルの読み込みに失敗しました\n設定ファイル(テーブル位置設定等)が消去されました\nお手数ですが設定をもう一度ご入力下さい)");
				break;
			case -26:
				MyToast.customToastShow(foreACT, "画像の読み込みに失敗");
				break;
			case -27:
				MyToast.customToastShow(foreACT, "検索に失敗しました");
				break;
			case -28:
				MyToast.customToastShow(foreACT, "放送主の情報取得に失敗しました");
				break;
			case -29:
				MyToast.customToastShow(foreACT, "放送枠の取得に失敗しました");
				break;
			case -30:
				MyToast.customToastShow(foreACT, "コメント取得できない公式・CH放送でした");
				break;
			case -31:
				MyToast.customToastShow(foreACT, "配信機能が有効になっていません。\n詳細設定から有効にしてください。");
				break;
			case -32:
				MyToast.customToastShow(foreACT, "配信機能を有効にするパスワードが間違っています");
				break;
			case -33:
				MyToast.customToastShow(foreACT, "コメント投稿に失敗しました\nチケット取得");
				break;
			case -34:
				MyToast.customToastShow(foreACT, "コメント投稿に失敗しました\n座席が無いかも");
				break;
			case -35:
				MyToast.customToastShow(foreACT, "コメント投稿に失敗しました\n通信系かも");
				break;
			case -36:
				MyToast.customToastShow(foreACT, "コメント投稿に失敗しました\nIOエラーかセッション不正");
				break;
			case -37:
				MyToast.customToastShow(foreACT, "期待値と異なるデータで\nエラーが発生しました");
				break;
			case -38:
				MyToast.customToastShow(foreACT, "エディットストリームのパースに失敗");
				break;
			case -39:
				MyToast.customToastShow(foreACT, "放送に必要な情報取得失敗");
				break;
			case -40:
				MyToast.customToastShow(foreACT, "参加状況の取得に失敗しました");
				break;
			case -41:
				MyToast.customToastShow(foreACT, "接続エラーしました");
				break;
			case -42:
				MyToast.customToastShow(foreACT, "予約枠です");
				break;
			case -43:
				MyToast.customToastShow(foreACT, "予期せぬエラー");
				break;
			case -44:
//				MyToast.customToastShow(foreACT, "コテハンの読み込みに失敗");//とりあえず出力してない
				break;
			case -45:
				MyToast.customToastShow(foreACT, "コミュニティ参加数が50を超えている為、\n参加できません");
				break;
			case -46:
				MyToast.customToastShow(foreACT, "配信設定の取得に失敗しました");
				break;
			case -47:
				MyToast.customToastShow(foreACT, "オフタイマー時間を経過している為、アイコンから起動し直しててください");
				break;
			case -48:
				MyToast.customToastShow(foreACT, "参加中一覧の取得に失敗しました\nしばらく経ってから試してみて下さい");
				break;
			case -49:
				MyToast.customToastShow(foreACT, "このトーストが出る場合、特定の公式・CHなので、できれば制作者に操作時の猫ログを送りつけて下さい");
				break;
			case -50:
				MyToast.customToastShow(foreACT, "TS視聴にチケットが必要な放送でした(NLR非対応となっています)");
				break;
			case -51:
				MyToast.customToastShow(foreACT, "HLS用のセッションが取得でませんでした");
				break;
			case -52:
				MyToast.customToastShow(foreACT, "生放送の再生に失敗しました");
				break;
			case -53:
				MyToast.customToastShow(foreACT, "パラメタ取得失敗\nHLS接続でのフォーマットが変更された可能性有り");
				break;
			case -54:
				MyToast.customToastShow(foreACT, "放送が満席でした");
				break;
			}
			// 0に戻しておく、無いと新たにセットできない仕様にしたため
			errorCode = 0;
		}

		/**
		 * errorCodeを取得します。
		 *
		 * @return errorCode
		 */
		public int getErrorCode() {
			return errorCode;
		}

		/**
		 * 0だった場合のみ errorCodeを設定します。
		 *
		 * @param errorCode
		 *            errorCode
		 */
		public void setErrorCode(int errorCode) {
			Log.d("NLiveRoid","SET ERROR CODE ------ " + errorCode);
				this.errorCode = errorCode;
		}


	}

	/**
	 * 引数に指定されたデータをプットして、アカウントファイルを保存 一番最初は無いのでreadの方を呼んでチェック
	 *
	 * @param key
	 * @param value
	 */
	protected void writeAccountFile(String key, String value) {
		Log.d("NLiveRoid","w AC - ");
		 try {
			 if(defaultMap == null||defaultMap.size() < 4){
				 defaultMap = new HashMap<String,String>();
				 readDefaultMapFromChipher();
			 }
			 defaultMap.put(key,value);
	            // 鍵
	            String kagi = "z_t_c00MA_IDE0x";
	            DESKeySpec desKey = new DESKeySpec(kagi.getBytes());
	            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
	            SecretKey keyGenerate = keyFactory.generateSecret(desKey);

	            // 暗号化
	            Cipher cipherInstance = Cipher.getInstance("DES");
	            cipherInstance.init(Cipher.ENCRYPT_MODE, keyGenerate);
	String chipStr = defaultMap.get("user_id1")+"<<SPLIT>>"+defaultMap.get("password1")+"<<SPLIT>>"+defaultMap.get("user_id2")+"<<SPLIT>>"+defaultMap.get("password2");

	            byte input[] = chipStr.getBytes();
	            byte encrypted[] = cipherInstance.doFinal(input);    // 暗号化データ
	            if(mContext == null){
					try {
						mContext = createPackageContext("nliveroid.nlr.main", CONTEXT_RESTRICTED);
					} catch (NameNotFoundException e1) {
						e1.printStackTrace();
					}
				}
	            FileOutputStream fos = mContext.openFileOutput(accountFileName,MODE_PRIVATE);
	            fos.write(encrypted);
		 } catch (Exception e) {
	            e.printStackTrace();
	            MyToast.customToastShow(this, "アカウント設定に失敗しました");
	        }
	}

	/**
	 * 今あるアカウントマップのデータをファイルに保存します
	 */
	protected void writeAccountFile() {
		try {
			 if(defaultMap == null){
				 defaultMap = new HashMap<String,String>();
			 }
	            // 鍵
	            String kagi = "z_t_c00MA_IDE0x";
	            DESKeySpec desKey = new DESKeySpec(kagi.getBytes());
	            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
	            SecretKey keyGenerate = keyFactory.generateSecret(desKey);

	            // 暗号化
	            Cipher cipherInstance = Cipher.getInstance("DES");
	            cipherInstance.init(Cipher.ENCRYPT_MODE, keyGenerate);
	            String chipStr =
	            (defaultMap.get("user_id1")==null? "":defaultMap.get("user_id1"))+"<<SPLIT>>"+
	            (defaultMap.get("password1")==null? "":defaultMap.get("password1"))+"<<SPLIT>>"+
	            (defaultMap.get("user_id2")==null? "":defaultMap.get("user_id2"))+"<<SPLIT>>"+
	            (defaultMap.get("password2")==null? "":defaultMap.get("password2"))+"<<SPLIT>>"+
	            (defaultMap.get("twitter_token")==null? "":defaultMap.get("twitter_token"))+"<<T_SPLIT>>"+
	            (defaultMap.get("twitter_secret")==null? "":defaultMap.get("twitter_secret"));
//	            Log.d("log","WRITE DATA  --- "  + chipStr);
	            byte input[] = chipStr.getBytes();
	            byte encrypted[] = cipherInstance.doFinal(input);    // 暗号化データ
	            if(mContext == null){
					try {
						mContext = createPackageContext("nliveroid.nlr.main", CONTEXT_RESTRICTED);
					} catch (NameNotFoundException e1) {
						e1.printStackTrace();
					}
				}
	            FileOutputStream fos = mContext.openFileOutput(accountFileName,MODE_PRIVATE);
	            fos.write(encrypted);
	            fos.close();
		 } catch (Exception e) {
	            e.printStackTrace();
	            this.deleteFile(accountFileName);
	            MyToast.customToastShow(this, "アカウント設定保存に失敗しました");
	        }
	}

	/**
	 * アカウントファイルの読み込み
	 */

	 private void readDefaultMapFromChipher(){
		 String[] str = null;
			try {
				if(mContext == null){
					try {
						mContext = createPackageContext("nliveroid.nlr.main", CONTEXT_RESTRICTED);
					} catch (NameNotFoundException e1) {
						e1.printStackTrace();
					}
				}
		 FileInputStream fis = mContext.openFileInput(accountFileName);

        // 復号処理
		 byte[] encrypted = new byte[fis.available()];
		 fis.read(encrypted);
          String kagi = "z_t_c00MA_IDE0x";
          DESKeySpec desKey = new DESKeySpec(kagi.getBytes());
          SecretKeyFactory keyFactory;
				keyFactory = SecretKeyFactory.getInstance("DES");

          SecretKey keyGenerate = keyFactory.generateSecret(desKey);
          Cipher cipherInstance = Cipher.getInstance("DES");
          cipherInstance.init(Cipher.DECRYPT_MODE, keyGenerate);
          byte input[] = cipherInstance.doFinal(encrypted);   // 復号したデータ
          //成功
          str =  new String(input,"UTF-8").split("<<SPLIT>>");
			}catch(FileNotFoundException e){
				//無ければ生成
				Log.d("NLiveRoid","AC NOT FOUND -");
					if(defaultMap == null){
						defaultMap = new HashMap<String,String>();//必要ないかもだけど
					}
					defaultMap.put("user_id1", "");
					defaultMap.put("password1", "");
					defaultMap.put("user_id2", "");
					defaultMap.put("password2", "");
					defaultMap.put("twitter_token", "");
					defaultMap.put("twitter_secret", "");
					writeAccountFile();
					readDefaultMapFromChipher();
					return;

			}catch(IOException e){
				e.printStackTrace();;
//				MyToast.customToastShow(this, "アカウント設定ファイルの読み込み失敗1");
				deleteFile(accountFileName);
				writeAccountFile();
				readDefaultMapFromChipher();
			}catch(IllegalBlockSizeException e){
				e.printStackTrace();
			}catch(Exception e){
				e.printStackTrace();
//				MyToast.customToastShow(this, "アカウント設定ファイルの読み込み失敗0");
				deleteFile(accountFileName);
				writeAccountFile();
				readDefaultMapFromChipher();
				return;
			}

			String[] account = new String[6];//lengthがそれ以上なかった場合、lengthが増えないのであらかじめ、要素を生成しておいて、それに入れる
			if(str == null)return;
				for(int i = 0; i < str.length; i++){
				account[i] = str[i];
				}

				if(str.length > 0 && str[str.length-1].split("<<T_SPLIT>>").length == 2){//Twitterアカウントがあったら
					account[4] = str[str.length-1].split("<<T_SPLIT>>")[0];
					account[5] = str[str.length-1].split("<<T_SPLIT>>")[1];
				}
			defaultMap.put("user_id1", account[0]==null||account[0].equals("null")? "":account[0]);
			defaultMap.put("password1", account[1]==null||account[1].equals("null")? "":account[1]);
			defaultMap.put("user_id2", account[2]==null||account[2].equals("null")? "":account[2]);
			defaultMap.put("password2", account[3]==null||account[3].equals("null")? "":account[3]);
			defaultMap.put("twitter_token", account[4]==null||account[4].equals("null")? "":account[4]);
			defaultMap.put("twitter_secret", account[5]==null||account[5].equals("null")? "":account[5]);

}


	/**
	 * 詳細の設定値を1つ更新し、ファイルに保存します 一番最初は無いのでreadの方を呼んでチェック
	 *
	 * @param key
	 * @param value
	 */
//	public void writeDetailsFile(String key, String value) {
//		Log.d("log","WRITE DETAILS  ------ ");
//		if(detailsMap == null){
//			detailsMap = new HashMap<String,String>();
//			importDetailsFile();
//		}
//		detailsMap.put(key, value);
//		int arg = exportDetailsFile();//書き込む
//		switch(arg){
////		case 0:
////			MyToast.customToastShow(this, "設定値を保存しました");
////			break;
//		case -1:
//			MyToast.customToastShow(this, "内部設定値の保存に失敗しました");
//			break;
//		case -2:
//			MyToast.customToastShow(this, "内部設定値ファイルへのアクセスに失敗しました");
//			break;
//		case -3:
//			MyToast.customToastShow(this, "内部設定値ファイルのIOに失敗しました");
//			break;
//		}
//	}

	/**
	 * 今ある詳細の設定値をファイルに保存します
	 *
	 * @param isPortLayt
	 * @param value
	 */
	public void writeDetailsFile() {
		Log.d("NLiveRoid","WRITE Details all");

			if(detailsMap == null){
				detailsMap = new HashMap<String,String>();
				importDetailsFile();
			}

			int arg = exportDetailsFile();//書き込む
			switch(arg){
//			case 0:
//				MyToast.customToastShow(this, "設定値を保存しました");
//				break;
			case -1:
				MyToast.customToastShow(this, "設定値の保存に失敗");
				break;
			case -2:
				MyToast.customToastShow(this, "内部設定値ファイルへのアクセスに失敗");
				break;
			case -3:
				MyToast.customToastShow(this, "内部設定値ファイルのIOに失敗");
				break;
			}
	}


	/*
	 * 設定値をチェックする
	 * このメソッドは、みんながアプデしたらいらなくなる
	 */
	private void settingFileCheck(){
		if(detailsMap.get("fix_screen") == null)detailsMap.put("fix_screen", "0");
		if(detailsMap.get("newline") == null)detailsMap.put("newline", "true");
		if(detailsMap.get("auto_username") == null)detailsMap.put("auto_username","false");
		if(detailsMap.get("form_up") == null)detailsMap.put("form_up", "false");
		if(detailsMap.get("form_backkey") == null)detailsMap.put("form_backkey", "true");

		if(detailsMap.get("player_select") == null)detailsMap.put("player_select", "0");
		if(detailsMap.get("sp_player") != null){//ここでHLS移行完了するはず(要チェック)
			if(detailsMap.get("sp_player").equals("true")){
			detailsMap.put("player_select", "0");
			}else if(detailsMap.get("sp_player").equals("false")){
			detailsMap.put("player_select", "1");
			}
		}
		if(detailsMap.get("fix_volenable") == null)detailsMap.put("fix_volenable","false");
		if(detailsMap.get("voice_input") == null)detailsMap.put("voice_input","false");
		if(detailsMap.get("layer_num") == null){
			if(detailsMap.get("comment_layer_num")!=null){
			detailsMap.put("layer_num",detailsMap.get("comment_layer_num"));
			}else{
			detailsMap.put("layer_num","0");
			}
		}
		if(detailsMap.get("player_quality") == null)detailsMap.put("player_quality", "0");
		if(detailsMap.get("init_comment_count") == null)detailsMap.put("init_comment_count","20");
		if(detailsMap.get("auto_comment_update") == null)detailsMap.put("auto_comment_update","-1");
		if(detailsMap.get("is_update_between") == null)detailsMap.put("is_update_between","true");
		if(detailsMap.get("off_timer") == null)detailsMap.put("off_timer","-1");

		if(detailsMap.get("alert_enable")==null)detailsMap.put("alert_enable","false");
		if(detailsMap.get("alert_sound_notif")==null)detailsMap.put("alert_sound_notif","false");
		if(detailsMap.get("alert_vibration_enable")==null)detailsMap.put("alert_vibration_enable","false");
		if(detailsMap.get("alert_led")==null)detailsMap.put("alert_led","true");
		if(detailsMap.get("alert_interval")==null)detailsMap.put("alert_interval","5");

		if(detailsMap.get("always_use1")==null)detailsMap.put("always_use1","false");
		if(detailsMap.get("always_use2")==null)detailsMap.put("always_use2","false");
		if(detailsMap.get("ac_confirm")==null)detailsMap.put("ac_confirm","false");
		if(detailsMap.get("fexit")==null)detailsMap.put("fexit","false");
		if(detailsMap.get("finish_back")==null)detailsMap.put("finish_back","true");
		if(detailsMap.get("discard_notification") == null)detailsMap.put("discard_notification","false");
		if(detailsMap.get("recent_ts") == null)detailsMap.put("recent_ts","true");
		if(detailsMap.get("delay_start") == null)detailsMap.put("delay_start","true");
		if(detailsMap.get("back_black") == null)detailsMap.put("back_black","false");

		if(detailsMap.get("speech_enable") == null){
			detailsMap.put("speech_enable", "0");//0がTTSのOFF、2がAQUESのOFF
		}else if(detailsMap.get("speech_enable") != null){
			if(detailsMap.get("speech_enable").equals("true")){
			detailsMap.put("speech_enable", "1");
			}else if(detailsMap.get("speech_enable").equals("false")){
				detailsMap.put("speech_enable", "0");
				}
		}
		if(detailsMap.get("speech_speed") == null)detailsMap.put("speech_speed", "5");
		if(detailsMap.get("speech_pich") == null)detailsMap.put("speech_pich", "5");
		if(detailsMap.get("speech_education_enable") == null)detailsMap.put("speech_education_enable", "true");
		if(detailsMap.get("speech_skip_count") == null)detailsMap.put("speech_skip_count", "5");
		if(detailsMap.get("speech_skip_word") == null)detailsMap.put("speech_skip_word", "いかりゃく");
		if(detailsMap.get("speech_aques_phont") == null)detailsMap.put("speech_aques_phont", "0");
		if(detailsMap.get("speech_aques_vol") == null)detailsMap.put("speech_aques_vol", "5");//マップには整数で詰める


		if(detailsMap.get("player_pos_p") == null)detailsMap.put("player_pos_p", "0");
		if(detailsMap.get("player_pos_l") == null)detailsMap.put("player_pos_l", "0");
		if(detailsMap.get("type_width_p") == null)detailsMap.put("type_width_p","0");
		if(detailsMap.get("type_width_l") == null)detailsMap.put("type_width_l","0");
		if(detailsMap.get("id_width_p") == null)detailsMap.put("id_width_p","15");
		if(detailsMap.get("id_width_l") == null)detailsMap.put("id_width_l","0");
		if(detailsMap.get("command_width_p") == null)detailsMap.put("command_width_p","0");
		if(detailsMap.get("command_width_l") == null)detailsMap.put("command_width_l", "0");
		if(detailsMap.get("time_width_p") == null)detailsMap.put("time_width_p","0");
		if(detailsMap.get("time_width_l") == null)detailsMap.put("time_width_l","0");
		if(detailsMap.get("score_width_p") == null)detailsMap.put("score_width_p","0");
		if(detailsMap.get("score_width_l") == null)detailsMap.put("score_width_l","0");
		if(detailsMap.get("num_width_p") == null)detailsMap.put("num_width_p","15");
		if(detailsMap.get("num_width_l") == null)detailsMap.put("num_width_l","15");
		if(detailsMap.get("comment_width_p") == null)detailsMap.put("comment_width_p", "70");
		if(detailsMap.get("comment_width_l") == null)detailsMap.put("comment_width_l","85");
		if(detailsMap.get("cellheight_p") == null)detailsMap.put("cellheight_p","3");
		if(detailsMap.get("cellheight_l") == null)detailsMap.put("cellheight_l","3");
		if(detailsMap.get("cellheight_test") == null)detailsMap.put("cellheight_test","3");
		if(detailsMap.get("x_pos_p") == null)detailsMap.put("x_pos_p","0");
		if(detailsMap.get("x_pos_l") == null)detailsMap.put("x_pos_l","60");
		if(detailsMap.get("y_pos_p") == null)detailsMap.put("y_pos_p","92");
		if(detailsMap.get("y_pos_l") == null)detailsMap.put("y_pos_l","88");
		if(detailsMap.get("bottom_pos_p") == null)detailsMap.put("bottom_pos_p","-43");
		if(detailsMap.get("bottom_pos_l") == null)detailsMap.put("bottom_pos_l","-86");
		if(detailsMap.get("width_p") == null)detailsMap.put("width_p","100");
		if(detailsMap.get("width_l") == null)detailsMap.put("width_l","40");


		if(detailsMap.get("his_value") == null)detailsMap.put("his_value","-1");

		if(detailsMap.get("cmd_cmd")==null)detailsMap.put("cmd_cmd","184");
		if(detailsMap.get("cmd_size")==null)detailsMap.put("cmd_size","");
		if(detailsMap.get("cmd_color")==null)detailsMap.put("cmd_color","");
		if(detailsMap.get("cmd_align")==null)detailsMap.put("cmd_align","");

		if(detailsMap.get("last_tab")==null)detailsMap.put("last_tab","1");

		if(detailsMap.get("sp_showcomment")==null)detailsMap.put("sp_showcomment","true");
		if(detailsMap.get("sp_ng184")==null)detailsMap.put("sp_ng184","false");
		if(detailsMap.get("sp_showbspcomment")==null)detailsMap.put("sp_showbspcomment","true");
		if(detailsMap.get("sp_ismute")==null)detailsMap.put("sp_ismute","false");
		if(detailsMap.get("sp_loadsmile")==null)detailsMap.put("sp_loadsmile","true");
		if(detailsMap.get("sp_volumesub")==null)detailsMap.put("sp_volumesub","50");

		if(detailsMap.get("type_seq")==null)detailsMap.put("type_seq","0");
		if(detailsMap.get("id_seq")==null)detailsMap.put("id_seq","1");
		if(detailsMap.get("cmd_seq")==null)detailsMap.put("cmd_seq","2");
		if(detailsMap.get("time_seq")==null)detailsMap.put("time_seq","3");
		if(detailsMap.get("score_seq")==null)detailsMap.put("score_seq","4");
		if(detailsMap.get("num_seq")==null)detailsMap.put("num_seq","5");
		if(detailsMap.get("comment_seq")==null)detailsMap.put("comment_seq","6");

		if(detailsMap.get("manner_0") == null)detailsMap.put("manner_0", "false");
		if(detailsMap.get("return_tab") == null)detailsMap.put("return_tab", "true");
		if(detailsMap.get("update_tab") == null)detailsMap.put("update_tab", "false");
		if(detailsMap.get("allco_operate") == null)detailsMap.put("allco_operate", "0");
		if(detailsMap.get("quick_0") == null)detailsMap.put("quick_0", "15");//初期値は前半全部ありで6個
		if(detailsMap.get("quick_1") == null)detailsMap.put("quick_1", "127");
		if(detailsMap.get("alpha") == null)detailsMap.put("alpha", "0");
		if(detailsMap.get("enable_his") == null)detailsMap.put("enable_his", "false");

		if(detailsMap.get("nlr_log") == null)detailsMap.put("nlr_log","false");
	}

	/**
	 * foreACTを設定します。
	 * @param foreACT foreACT
	 * @return
	 */
	public Activity getForeACT() {
	    return foreACT;
	}
	/**
	 * foreACTを設定します。
	 * @param foreACT foreACT
	 */
	public void setForeACT(Activity foreACT) {
	    this.foreACT = foreACT;
	}

	/**
	 * defaultMapを取得します。
	 *
	 * @return defaultMap
	 */
	public HashMap<String,String> getDefaultMap() {
		return defaultMap;
	}

	/**
	 * detailsMapを取得します。
	 *
	 * @return detailsMap
	 */
	public HashMap<String,String> getDetailsMap() {
		return detailsMap;
	}

	/**
	 * errorを取得します。
	 *
	 * @return error
	 */
	public ErrorCode getError() {
		return error;
	}

	/**
	 * tagBitMapを取得します。
	 * @return tagBitMap
	 */
	public Bitmap getTagBitMap(String key) {
		if(tagBitMap == null){
	return BitmapFactory.decodeStream(getResources().openRawResource(R.drawable.tag_noimage));
		}
	    return tagBitMap.get(key);
	}
	/**
	 * ランキングの矢印
	 */
	public Bitmap getRankingAloow(int index){
		if(rankingBitMaps == null){
			rankingBitMaps = new Bitmap[3];
			rankingBitMaps[0] = BitmapFactory.decodeStream(getResources().openRawResource(R.drawable.allow_up));
			rankingBitMaps[1] = BitmapFactory.decodeStream(getResources().openRawResource(R.drawable.allow_down));
			rankingBitMaps[2] = BitmapFactory.decodeStream(getResources().openRawResource(R.drawable.allow_even));
		}
		if(index >= rankingBitMaps.length || index < 0)return null;
		return rankingBitMaps[index];
	}
	/**
	 * tagNameMapを取得します。
	 * @return tagBitMap
	 */
	public HashMap<String, String> getTagNameMap() {
	    return tagNameMap;
	}
	/**
	 * colorStringMapを取得します。
	 * @return tagBitMap
	 */
	public HashMap<String, String> getColorMap() {
	    return colorStringMap;
	}
	/**
	 * metricsを取得します。
	 * @return metrics
	 */
	public float getMetrics() {
	    return scaleDensity;
	}

	/**
	 * metricsを設定します。
	 * @param scaleDensity2 metrics
	 */
	public void setMetrics(float scaleDensity2) {
	    this.scaleDensity = scaleDensity2;
	}

	/**
	 * gateViewを取得します。
	 * @return gateView
	 */
	public GateView getGateView() {
	    return gateView;
	}



	//インポートとエクスポート


	private int exportDetailsFile(){
		Log.d("Log","Save Details ---");

		try{
		if(detailsMap == null)return -1;
		String xml =
		"<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<Settings xmlns=\"http://nliveroid-tutorial.appspot.com/settings/\">\n"+
		"<common_settings>\n"+
		"<always_use1>"+(detailsMap.get("always_use1")==null? "false":detailsMap.get("always_use1"))+"</always_use1>\n"+
		"<always_use2>"+(detailsMap.get("always_use2")==null? "false":detailsMap.get("always_use2"))+"</always_use2>\n"+
		"<allco_operate>"+(detailsMap.get("allco_operate")==null? "0":detailsMap.get("allco_operate"))+"</allco_operate>\n"+
		"<quick_0>"+(detailsMap.get("quick_0")==null? "15":detailsMap.get("quick_0"))+"</quick_0>\n"+
		"<quick_1>"+(detailsMap.get("quick_1")==null? "127":detailsMap.get("quick_1"))+"</quick_1>\n"+
		"<account_confirm>"+(detailsMap.get("ac_confirm")==null? "false":detailsMap.get("ac_confirm"))+"</account_confirm>\n"+
		"<initial_tab>"+(detailsMap.get("last_tab")==null? "1":detailsMap.get("last_tab"))+"</initial_tab>\n"+
		"<history_tab>"+(detailsMap.get("enable_his")==null? "false":detailsMap.get("enable_his"))+"</history_tab>\n"+
		"<alpha>"+(detailsMap.get("alpha")==null? "0":detailsMap.get("alpha"))+"</alpha>\n"+
		"<initial_bc>"+(detailsMap.get("initial_bc")==null? "true":detailsMap.get("initial_bc"))+"</initial_bc>\n"+
		"<delay_start>"+(detailsMap.get("delay_start")==null? "true":detailsMap.get("delay_start"))+"</delay_start>\n"+
		"<back_black>"+(detailsMap.get("back_black")==null? "false":detailsMap.get("back_black"))+"</back_black>\n"+
		"<backkey_dialog>"+(detailsMap.get("fexit")==null? "false":detailsMap.get("fexit"))+"</backkey_dialog>\n"+
		"<backkey_append>"+(detailsMap.get("finish_back")==null? "":detailsMap.get("finish_back"))+"</backkey_append>\n"+
		"<only_comment>"+(detailsMap.get("only_comment")==null? "false":detailsMap.get("only_comment").equals("false")? "false":"true")+"</only_comment>\n"+
		"<upform>"+(detailsMap.get("form_up")==null? "false":detailsMap.get("form_up").equals("false")? "false":"true")+"</upform>\n"+
		"<form_backkey>"+(detailsMap.get("form_backkey")==null? "true":detailsMap.get("form_backkey").equals("false")? "false":"true")+"</form_backkey>\n"+
		"<newline>"+(detailsMap.get("newline")==null? "true":detailsMap.get("newline").equals("false")? "false":"true")+"</newline>\n"+
		"<auto_username>"+(detailsMap.get("auto_username")==null? "false":detailsMap.get("auto_username"))+"</auto_username>\n"+
		"<voice_input>"+(detailsMap.get("voice_input")==null? "false":detailsMap.get("voice_input"))+"</voice_input>\n"+
		"<layer_num>"+(detailsMap.get("layer_num")==null? "0":detailsMap.get("layer_num"))+"</layer_num>\n"+
		"<player_quality>"+(detailsMap.get("player_quality")==null? "0":detailsMap.get("player_quality"))+"</player_quality>\n"+
		"<his_value>"+(detailsMap.get("his_value")==null? "-1":detailsMap.get("his_value"))+"</his_value>\n"+
		"<is_update_between>"+(detailsMap.get("is_update_between")==null? "true":detailsMap.get("is_update_between"))+"</is_update_between>\n"+
		"<init_comment_count>"+(detailsMap.get("init_comment_count")==null? "20":detailsMap.get("init_comment_count"))+"</init_comment_count>\n"+
		"<auto_comment_update>"+(detailsMap.get("auto_comment_update")==null? "-1":detailsMap.get("auto_comment_update"))+"</auto_comment_update>\n"+
		"<off_timer>"+(detailsMap.get("off_timer")==null? "-1":detailsMap.get("off_timer"))+"</off_timer>\n"+
		"<player_select>"+(detailsMap.get("player_select")==null? "0":detailsMap.get("player_select"))+"</player_select>\n"+
		"<fix_volume>"+(detailsMap.get("fix_volenable")==null? "false":detailsMap.get("fix_volenable").equals("false")? "false":"true")+"</fix_volume>\n"+
		"<fix_volume_value>"+(detailsMap.get("fix_volvalue")==null? "0":detailsMap.get("fix_volvalue"))+"</fix_volume_value>\n"+
		"<manner_0>"+(detailsMap.get("manner_0")==null? "false":detailsMap.get("manner_0").equals("false")? "false":"true")+"</manner_0>\n"+
		"<return_tab>"+(detailsMap.get("return_tab")==null? "true":detailsMap.get("return_tab").equals("false")? "false":"true")+"</return_tab>\n"+
		"<update_tab>"+(detailsMap.get("update_tab")==null? "false":detailsMap.get("update_tab").equals("false")? "false":"true")+"</update_tab>\n"+
		"<orientation>"+(detailsMap.get("fix_screen")==null? "0":detailsMap.get("fix_screen"))+"</orientation>\n"+
		"<discard_notification>"+(detailsMap.get("discard_notification")==null? "false":detailsMap.get("discard_notification"))+"</discard_notification>\n"+
		"<recent_ts>"+(detailsMap.get("recent_ts")==null? "true":detailsMap.get("recent_ts"))+"</recent_ts>\n"+
		"<nlr_log>"+(detailsMap.get("nlr_log")==null? "false":detailsMap.get("nlr_log"))+"</nlr_log>\n"+
		"<handlename_at_enable>"+(detailsMap.get("at_enable")==null? "false":detailsMap.get("at_enable"))+"</handlename_at_enable>\n"+
		"<handlename_at_overwrite>"+(detailsMap.get("at_overwrite")==null? "false":detailsMap.get("at_overwrite"))+"</handlename_at_overwrite>\n"+
		"<alert_enable>"+(detailsMap.get("alert_enable")==null? "false":detailsMap.get("alert_enable").equals("false")? "false":"true")+"</alert_enable>\n"+
		"<alert_vibration_enable>"+(detailsMap.get("alert_vibration_enable")==null? "false":detailsMap.get("alert_vibration_enable").equals("false")? "false":"true")+"</alert_vibration_enable>\n"+
		"<alert_sound_notif>"+(detailsMap.get("alert_sound_notif")==null? "false":detailsMap.get("alert_sound_notif").equals("false")? "false":"true")+"</alert_sound_notif>\n"+
		"<alert_led>"+(detailsMap.get("alert_led")==null? "true":detailsMap.get("alert_led").equals("false")? "false":"true")+"</alert_led>\n"+
		"<alert_interval>"+(detailsMap.get("alert_interval")==null? "5":detailsMap.get("alert_interval"))+"</alert_interval>\n"+
		"<enable_bc>"+(detailsMap.get("enable_bc")==null? "false":detailsMap.get("enable_bc"))+"</enable_bc>\n"+
		"<command_settings>\n" +
		"<cmd_anonym>"+(detailsMap.get("cmd_cmd")==null? "":detailsMap.get("cmd_cmd"))+"</cmd_anonym>\n"+
		"<cmd_size>"+(detailsMap.get("cmd_size")==null? "":detailsMap.get("cmd_size"))+"</cmd_size>\n"+
		"<cmd_color>"+(detailsMap.get("cmd_color")==null? "":detailsMap.get("cmd_color"))+"</cmd_color>\n"+
		"<cmd_align>"+(detailsMap.get("cmd_align")==null? "":detailsMap.get("cmd_align"))+"</cmd_align>\n"+
		"</command_settings>\n"+
		"<speech_settings>\n" +
		"<speech_enable>"+(detailsMap.get("speech_enable")==null? "0":detailsMap.get("speech_enable"))+"</speech_enable>\n"+
		"<speech_speed>"+(detailsMap.get("speech_speed")==null? "50":detailsMap.get("speech_speed"))+"</speech_speed>\n"+
		"<speech_pich>"+(detailsMap.get("speech_pich")==null? "50":detailsMap.get("speech_pich"))+"</speech_pich>\n"+
		"<speech_education_enable>"+(detailsMap.get("speech_education_enable")==null? "true":detailsMap.get("speech_education_enable"))+"</speech_education_enable>\n"+
		"<speech_skip_word>"+(detailsMap.get("speech_skip_word")==null? "いかりゃく":detailsMap.get("speech_skip_word"))+"</speech_skip_word>\n"+
		"<speech_skip_count>"+(detailsMap.get("speech_skip_count")==null? "5":detailsMap.get("speech_skip_count"))+"</speech_skip_count>\n"+
		"<speech_aques_phont>"+(detailsMap.get("speech_aques_phont")==null? "0":detailsMap.get("speech_aques_phont"))+"</speech_aques_phont>\n"+
		"<speech_aques_vol>"+(detailsMap.get("speech_aques_vol")==null? "5":detailsMap.get("speech_aques_vol"))+"</speech_aques_vol>\n"+
		"</speech_settings>\n"+
		"<spplayer_settings>\n" +
		"<sp_showcomment>"+(detailsMap.get("sp_showcomment")==null? "true":detailsMap.get("sp_showcomment"))+"</sp_showcomment>\n"+
		"<sp_ng184>"+(detailsMap.get("sp_ng184")==null? "false":detailsMap.get("sp_ng184"))+"</sp_ng184>\n"+
		"<sp_showbspcomment>"+(detailsMap.get("sp_showbspcomment")==null? "true":detailsMap.get("sp_showbspcomment"))+"</sp_showbspcomment>\n"+
		"<sp_ismute>"+(detailsMap.get("sp_ismute")==null? "false":detailsMap.get("sp_ismute"))+"</sp_ismute>\n"+
		"<sp_loadsmile>"+(detailsMap.get("sp_loadsmile")==null? "false":detailsMap.get("sp_loadsmile"))+"</sp_loadsmile>\n"+
		"<sp_volumesub>"+(detailsMap.get("sp_volumesub")==null? "50":detailsMap.get("sp_volumesub"))+"</sp_volumesub>\n"+
		"</spplayer_settings>\n"+
		"<toptab_tcolor>"+(detailsMap.get("toptab_tcolor")==null? "0":detailsMap.get("toptab_tcolor"))+"</toptab_tcolor>\n"+
		"<column_sequence>\n"+
		"<type_seq>"+(detailsMap.get("type_seq")==null? "0":detailsMap.get("type_seq"))+"</type_seq>\n"+
		"<id_seq>"+(detailsMap.get("id_seq")==null? "1":detailsMap.get("id_seq"))+"</id_seq>\n"+
		"<cmd_seq>"+(detailsMap.get("cmd_seq")==null? "2":detailsMap.get("cmd_seq"))+"</cmd_seq>\n"+
		"<time_seq>"+(detailsMap.get("time_seq")==null? "3":detailsMap.get("time_seq"))+"</time_seq>\n"+
		"<score_seq>"+(detailsMap.get("score_seq")==null? "4":detailsMap.get("score_seq"))+"</score_seq>\n"+
		"<num_seq>"+(detailsMap.get("num_seq")==null? "5":detailsMap.get("num_seq"))+"</num_seq>\n"+
		"<comment_seq>"+(detailsMap.get("comment_seq")==null? "6":detailsMap.get("comment_seq"))+"</comment_seq>\n"+
		"</column_sequence>\n"+
		"</common_settings>\n"+
		"<portlayt_settings>\n"+
		"<player_position>"+(detailsMap.get("player_pos_p")==null? "0":detailsMap.get("player_pos_p"))+"</player_position>\n"+
		"<x_position>"+(detailsMap.get("x_pos_p")==null? "0":detailsMap.get("x_pos_p"))+"</x_position>\n"+
		"<x_dragging>"+(detailsMap.get("xd_enable_p")==null? "false":detailsMap.get("xd_enable_p"))+"</x_dragging>\n"+
		"<y_position>"+(detailsMap.get("y_pos_p")==null? "92":detailsMap.get("y_pos_p"))+"</y_position>\n"+
		"<y_dragging>"+(detailsMap.get("yd_enable_p")==null? "true":detailsMap.get("yd_enable_p"))+"</y_dragging>\n"+
		"<height>"+(detailsMap.get("bottom_pos_p")==null? "-43":detailsMap.get("bottom_pos_p"))+"</height>\n"+
		"<width>"+(detailsMap.get("width_p")==null? "100":detailsMap.get("width_p"))+"</width>\n"+
		"<cellheight_test>"+(detailsMap.get("cellheight_test")==null? "3":detailsMap.get("cellheight_test"))+"</cellheight_test>\n"+
		"<font_size>"+(detailsMap.get("cellheight_p")==null? "3":detailsMap.get("cellheight_p"))+"</font_size>\n"+
		"<column_settings>\n"+
		"<type_width>"+(detailsMap.get("type_width_p")==null? "0":detailsMap.get("type_width_p"))+"</type_width>\n"+
		"<id_width>"+(detailsMap.get("id_width_p")==null? "15":detailsMap.get("id_width_p"))+"</id_width>\n"+
		"<cmd_width>"+(detailsMap.get("command_width_p")==null? "0":detailsMap.get("command_width_p"))+"</cmd_width>\n"+
		"<time_width>"+(detailsMap.get("time_width_p")==null? "0":detailsMap.get("time_width_p"))+"</time_width>\n"+
		"<score_width>"+(detailsMap.get("score_width_p")==null? "0":detailsMap.get("score_width_p"))+"</score_width>\n"+
		"<num_width>"+(detailsMap.get("num_width_p")==null? "15":detailsMap.get("num_width_p"))+"</num_width>\n"+
		"<comment_width>"+(detailsMap.get("comment_width_p")==null? "70":detailsMap.get("comment_width_p"))+"</comment_width>\n"+
		"</column_settings>\n"+
		"</portlayt_settings>\n"+
		"<landscape_settings>\n"+
		"<player_position>"+(detailsMap.get("player_pos_l")==null? "0":detailsMap.get("player_pos_l"))+"</player_position>\n"+
		"<x_position>"+(detailsMap.get("x_pos_l")==null? "0":detailsMap.get("x_pos_l"))+"</x_position>\n"+
		"<x_dragging>"+(detailsMap.get("xd_enable_l")==null? "false":detailsMap.get("xd_enable_l"))+"</x_dragging>\n"+
		"<y_position>"+(detailsMap.get("y_pos_l")==null? "92":detailsMap.get("y_pos_l"))+"</y_position>\n"+
		"<y_dragging>"+(detailsMap.get("yd_enable_l")==null? "true":detailsMap.get("yd_enable_l"))+"</y_dragging>\n"+
		"<height>"+(detailsMap.get("bottom_pos_l")==null? "-43":detailsMap.get("bottom_pos_l"))+"</height>\n"+
		"<width>"+(detailsMap.get("width_l")==null? "40":detailsMap.get("width_l"))+"</width>\n"+
		"<font_size>"+(detailsMap.get("cellheight_l")==null? "3":detailsMap.get("cellheight_l"))+"</font_size>\n"+
		"<column_settings>\n"+
		"<type_width>"+(detailsMap.get("type_width_l")==null? "0":detailsMap.get("type_width_l"))+"</type_width>\n"+
		"<id_width>"+(detailsMap.get("id_width_l")==null? "0":detailsMap.get("id_width_l"))+"</id_width>\n"+
		"<cmd_width>"+(detailsMap.get("command_width_l")==null? "0":detailsMap.get("command_width_l"))+"</cmd_width>\n"+
		"<time_width>"+(detailsMap.get("time_width_l")==null? "0":detailsMap.get("time_width_l"))+"</time_width>\n"+
		"<score_width>"+(detailsMap.get("score_width_l")==null? "0":detailsMap.get("score_width_l"))+"</score_width>\n"+
		"<num_width>"+(detailsMap.get("num_width_l")==null? "15":detailsMap.get("num_width_l"))+"</num_width>\n"+
		"<comment_width>"+(detailsMap.get("comment_width_l")==null? "70":detailsMap.get("comment_width_l"))+"</comment_width>\n"+
		"</column_settings>\n"+
		"</landscape_settings>\n"+
		"</Settings>";
		if(mContext == null){
			try {
				mContext = createPackageContext("nliveroid.nlr.main", CONTEXT_RESTRICTED);
			} catch (NameNotFoundException e1) {
				e1.printStackTrace();
			}
		}
		FileOutputStream fos = mContext.openFileOutput(detailsFileName,this.MODE_PRIVATE);
		fos.write(xml.getBytes());
		fos.close();
	} catch (FileNotFoundException e) {
		e.printStackTrace();
		return -3;
	} catch (IOException e) {
		e.printStackTrace();
		return -3;
	}
		return 0;
	}



	private ArrayList<String> importDetailsFile(){
		Log.d("NLiveRoid","in Details -");
		//ファイルから設定値を読み込む
		//一時マップにXMLの全タグを読み込む
		HashMap<String,String> map = null;
		ArrayList<String> missedStrs = new ArrayList<String>();
		if(detailsMap == null){
			missedStrs.add("wrong1");
		}
			try {	if(mContext == null){
				try {
					mContext = createPackageContext("nliveroid.nlr.main", CONTEXT_RESTRICTED);
				} catch (NameNotFoundException e1) {
					e1.printStackTrace();
				}
			}
				FileInputStream fis = mContext.openFileInput(detailsFileName);
				byte[] readBytes = new byte[fis.available()];
				fis.read(readBytes);
				try {
					//設定値を読み込む
					map = XMLparser.setSettingValues(readBytes);
				}catch(XmlPullParserException e){
//					MyToast.customToastShow(this, "設定ファイルパース失敗");
					fis.close();
//					e.printStackTrace();
					Log.d("NLiveRoid","XmlPullParserException:0");
					throw new FileNotFoundException();
				}catch(IOException e){
//					MyToast.customToastShow(this, "設定値IOエラー");
					fis.close();
					e.printStackTrace();
					return importDetailsFile();
				} catch (Exception e) {
					//パーサでexceptionの場合
						//消して再度このメソッドをやり直す→FileNotFound→デフォ値で生成
						this.deleteFile(detailsFileName);
//						MyToast.customToastShow(this, "設定値読み込み失敗しました\n起動する為内部ファイルを削除しました");
						fis.close();
						e.printStackTrace();
						return importDetailsFile();
				}

				fis.close();

			} catch (FileNotFoundException e) {
				e.printStackTrace();
				detailsMap = new HashMap<String,String>();
				// 初期値を入れておく
				//デフォ値は結局XMLで読み出すので、わかりやすく"true"とか入れちゃう
				detailsMap.put("always_use1", "false");
				detailsMap.put("always_use2", "false");
				detailsMap.put("ac_confirm", "false");
				detailsMap.put("last_tab", "1");
				detailsMap.put("initial_bc", "true");
				detailsMap.put("finish_back", "true");
				detailsMap.put("fexit", "false");
				detailsMap.put("xd_enable_p","false");
				detailsMap.put("xd_enable_l","false");
				detailsMap.put("yd_enable_p","true");
				detailsMap.put("yd_enable_l","true");
				detailsMap.put("at_enable","false");
				detailsMap.put("at_overwrite","false");

				detailsMap.put("alert_enable", "false");
				detailsMap.put("alert_sound_notif", "false");
				detailsMap.put("alert_vibration_enable", "false");
				detailsMap.put("alert_led", "true");
				detailsMap.put("alert_interval", "5");

				detailsMap.put("enable_bc", "false");

				detailsMap.put("nlr_log", "false");

				detailsMap.put("allco_operate", "0");
				detailsMap.put("quick_0", "15");
				detailsMap.put("quick_1", "127");
				detailsMap.put("alpha", "0");
				detailsMap.put("fix_screen", "0");
				detailsMap.put("newline", "true");
				detailsMap.put("auto_username", "false");
				detailsMap.put("form_up", "false");
				detailsMap.put("form_backkey", "true");
				detailsMap.put("fix_volenable", "false");
				detailsMap.put("manner_0", "false");
				detailsMap.put("return_tab", "true");
				detailsMap.put("update_tab", "false");
				detailsMap.put("discard_notification", "false");
				detailsMap.put("recent_ts", "true");
				detailsMap.put("delay_start", "true");
				detailsMap.put("back_black", "false");
				detailsMap.put("fix_volvalue", "0");
				//only_commentはフォルスでインストールがなかったらDetailsでtrueにされるはず
				detailsMap.put("voice_input", "false");
				detailsMap.put("layer_num", "0");
				detailsMap.put("player_quality", "0");
				detailsMap.put("his_value", "-1");
				detailsMap.put("is_update_between", "true");
				detailsMap.put("init_comment_count", "20");
				detailsMap.put("auto_comment_update", "-1");
				detailsMap.put("off_timer", "-1");
				detailsMap.put("player_pos_p", "0");
				detailsMap.put("player_pos_l", "0");
				detailsMap.put("speech_enable", "0");
				detailsMap.put("speech_skip_word", "いかりゃく");
				detailsMap.put("speech_speed", "5");
				detailsMap.put("speech_pich", "5");
				detailsMap.put("speech_aques_phont", "0");
				detailsMap.put("speech_aques_vol", "5");
				detailsMap.put("speech_education_enable", "true");
				detailsMap.put("speech_skip_count", "5");

				detailsMap.put("toptab_tcolor", "0");
				detailsMap.put("type_width_p", "0");
				detailsMap.put("id_width_p", "15");
				detailsMap.put("command_width_p", "0");
				detailsMap.put("time_width_p", "0");
				detailsMap.put("score_width_p", "0");
				detailsMap.put("num_width_p", "15");
				detailsMap.put("comment_width_p", "70");
				detailsMap.put("cellheight_p", "3");
				detailsMap.put("x_pos_p", "0");
				detailsMap.put("y_pos_p", "92");
				detailsMap.put("bottom_pos_p", "-43");
				detailsMap.put("width_p", "100");

				detailsMap.put("type_width_l", "0");
				detailsMap.put("id_width_l", "0");
				detailsMap.put("command_width_l", "0");
				detailsMap.put("time_width_l", "0");
				detailsMap.put("score_width_l", "0");
				detailsMap.put("num_width_l", "15");
				detailsMap.put("comment_width_l", "70");
				detailsMap.put("cellheight_l", "3");
				detailsMap.put("x_pos_l", "60");
				detailsMap.put("y_pos_l", "88");
				detailsMap.put("bottom_pos_l", "-86");
				detailsMap.put("width_l", "40");

				detailsMap.put("cellheight_test", "3");

				detailsMap.put("type_seq","0");
				detailsMap.put("id_seq","1");
				detailsMap.put("cmd_seq","2");
				detailsMap.put("time_seq","3");
				detailsMap.put("score_seq","4");
				detailsMap.put("num_seq","5");
				detailsMap.put("comment_seq","6");

				int arg = exportDetailsFile();//初回起動の場合、書き込む
				switch(arg){
//					case 0:
//						MyToast.customToastShow(this, "設定値を保存しました");
//						break;
				case -1:
					MyToast.customToastShow(this, "内部設定値の保存に失敗しました");
					break;
				case -2:
					MyToast.customToastShow(this, "内部設定値ファイルへのアクセスに失敗しました");
					break;
				case -3:
					MyToast.customToastShow(this, "内部設定値ファイルのIOに失敗しました");
					break;
				}
				return importDetailsFile();// 再帰する、※無限注意
			} catch (IOException e) {
				e.printStackTrace();
				missedStrs.add("wrong2");
				return missedStrs;
			}
			return checkSettingValue(missedStrs,map);
	}

	/**
	 * 設定値のチェック
	 * SettingFileDialogからも参照される
	 * @param failedList
	 * @param map
	 * @return
	 */
	public ArrayList<String> checkSettingValue(ArrayList<String> failedList,HashMap<String,String> map){
			//値のチェック-----------------------------------
			//fexitとfinish_back以外はDetailsMap
			if(map.get("always_use1") != null){
				try{
					Boolean.parseBoolean(map.get("always_use1"));
					setDetailsMapValue("always_use1", map.get("always_use1"));
				}catch(Exception e){
					failedList.add("always_use1");
				}
			}if(map.get("always_use2") != null){
				try{
					if(getDetailsMapValue("always_use1").equals("true")){
						setDetailsMapValue("always_use2", "false");
					}else{
					Boolean.parseBoolean(map.get("always_use2"));
					setDetailsMapValue("always_use2", map.get("always_use2"));
					}
				}catch(Exception e){
					failedList.add("always_use2");
				}
			}if(map.get("allco_operate") != null){
				try{

					Integer.parseInt(map.get("allco_operate"));
					setDetailsMapValue("allco_operate", map.get("allco_operate"));
				}catch(Exception e){
					failedList.add("allco_operate");
				}
			}if(map.get("quick_0") != null){
				try{
					if(Integer.parseInt(map.get("quick_0"))<127)setDetailsMapValue("quick_0", map.get("quick_0"));
				}catch(Exception e){
					failedList.add("quick_0");
				}
			}if(map.get("quick_1") != null){
				try{
					if(Integer.parseInt(map.get("quick_1"))<127)setDetailsMapValue("quick_1", map.get("quick_1"));
				}catch(Exception e){
					failedList.add("quick_1");
				}
			}if(map.get("alpha") != null){
				try{
					if(Integer.parseInt(map.get("alpha"))<127)setDetailsMapValue("alpha", map.get("alpha"));
				}catch(Exception e){
					failedList.add("alpha");
				}
			}
			if(map.get("account_confirm") != null){
				try{
					Boolean.parseBoolean(map.get("account_confirm"));
					setDetailsMapValue("ac_confirm", map.get("account_confirm"));
				}catch(Exception e){
					failedList.add("account_confirm");
				}
			}
			if(map.get("history_tab") != null){
				try{
					Boolean.parseBoolean(map.get("history_tab"));
					setDetailsMapValue("enable_his", map.get("history_tab"));
				}catch(Exception e){
					failedList.add("history_tab");
				}
			}
			if(map.get("his_value") != null){
				try{
					Boolean.parseBoolean(map.get("his_value"));
					setDetailsMapValue("his_value", map.get("his_value"));
				}catch(Exception e){
					failedList.add("his_value");
				}
			}if(map.get("initial_tab") != null){
				try{
					Boolean.parseBoolean(map.get("initial_tab"));
					setDetailsMapValue("last_tab", map.get("initial_tab"));
				}catch(Exception e){
					failedList.add("initial_tab");
				}
			}if(map.get("initial_bc") != null){
				try{
					Boolean.parseBoolean(map.get("initial_bc"));
					setDetailsMapValue("initial_bc", map.get("initial_bc"));
				}catch(Exception e){
					failedList.add("initial_bc");
				}
			}
			if(map.get("backkey_dialog") != null){
				try{
					Boolean.parseBoolean(map.get("backkey_dialog"));
					setDetailsMapValue("fexit", map.get("backkey_dialog"));
				}catch(Exception e){
					failedList.add("backkey_dialog");
				}
			}if(map.get("backkey_append") != null){
				try{
					Boolean.parseBoolean(map.get("backkey_append"));
					setDetailsMapValue("finish_back", map.get("backkey_append"));
				}catch(Exception e){
					failedList.add("backkey_append");
				}
			}if(map.get("upform") != null){
				try{
					Boolean.parseBoolean(map.get("upform"));
					setDetailsMapValue("form_up", map.get("upform"));
				}catch(Exception e){
					failedList.add("upform");
				}
			}if(map.get("form_backkey") != null){
				try{
					Boolean.parseBoolean(map.get("form_backkey"));
					setDetailsMapValue("form_backkey", map.get("form_backkey"));
				}catch(Exception e){
					failedList.add("form_backkey");
				}
			}if(map.get("newline") != null){
				try{
					Boolean.parseBoolean(map.get("newline"));
					setDetailsMapValue("newline", map.get("newline"));
				}catch(Exception e){
					failedList.add("newline");
				}
			}if(map.get("auto_username") != null){
				try{
					Boolean.parseBoolean(map.get("auto_username"));
					setDetailsMapValue("auto_username", map.get("auto_username"));
				}catch(Exception e){
					failedList.add("auto_username");
				}
			}if(map.get("voice_input") != null){
				try{
					Boolean.parseBoolean(map.get("voice_input"));
					setDetailsMapValue("voice_input", map.get("voice_input"));
				}catch(Exception e){
					failedList.add("voice_input");
				}
			}if(map.get("is_update_between") != null){
				try{
					Boolean.parseBoolean(map.get("is_update_between"));
					setDetailsMapValue("is_update_between", map.get("is_update_between"));
				}catch(Exception e){
					failedList.add("is_update_between");
				}
			}if(map.get("delay_start") != null){
				try{
					Boolean.parseBoolean(map.get("delay_start"));
					setDetailsMapValue("delay_start", map.get("delay_start"));
				}catch(Exception e){
					failedList.add("delay_start");
				}
			}if(map.get("back_black") != null){
				try{
					Boolean.parseBoolean(map.get("back_black"));
					setDetailsMapValue("back_black", map.get("back_black"));
				}catch(Exception e){
					failedList.add("back_black");
				}
			}if(map.get("enable_bc") != null){
				try{
					Boolean.parseBoolean(map.get("enable_bc"));
					setDetailsMapValue("enable_bc", map.get("enable_bc"));
				}catch(Exception e){
					failedList.add("enable_bc");
				}
			}if(map.get("layer_num") != null){
				try{
					Integer.parseInt(map.get("layer_num"));
					setDetailsMapValue("layer_num", map.get("layer_num"));
				}catch(Exception e){
					failedList.add("layer_num");
				}
			}if(map.get("player_quality") != null){
				try{
					Integer.parseInt(map.get("player_quality"));
					setDetailsMapValue("player_quality", map.get("player_quality"));
				}catch(Exception e){
					failedList.add("player_quality");
				}
			}if(map.get("auto_comment_update") != null){
				try{
					Integer.parseInt(map.get("auto_comment_update"));
					setDetailsMapValue("auto_comment_update", map.get("auto_comment_update"));
				}catch(Exception e){
					failedList.add("auto_comment_update");
				}
			}if(map.get("off_timer") != null){
				try{
					Integer.parseInt(map.get("off_timer"));
					setDetailsMapValue("off_timer", map.get("off_timer"));
				}catch(Exception e){
					failedList.add("off_timer");
				}
			}if(map.get("init_comment_count") != null){
				try{

					Integer.parseInt(map.get("init_comment_count"));
					setDetailsMapValue("init_comment_count", map.get("init_comment_count"));
				}catch(Exception e){
					failedList.add("init_comment_count");
				}
			}
			if(map.get("player_select") != null){
				try{
					byte val = Byte.parseByte(map.get("player_select"));
					if(val > 2||val <0){
						failedList.add("player_select");
						map.put("player_select", "0");
					}
					setDetailsMapValue("player_select", map.get("player_select"));
				}catch(Exception e){
					failedList.add("player_select");
				}
			}
			if(map.get("handlename_at_enable") != null){
				try{
					Boolean.parseBoolean(map.get("handlename_at_enable"));
					setDetailsMapValue("at_enable", map.get("handlename_at_enable"));
				}catch(Exception e){
					failedList.add("handlename_at_enable");
				}
			}if(map.get("handlename_at_overwrite") != null){
				try{
					Boolean.parseBoolean(map.get("handlename_at_overwrite"));
					setDetailsMapValue("at_overwrite", map.get("handlename_at_overwrite"));
				}catch(Exception e){
					failedList.add("handlename_at_overwrite");
				}
			}if(map.get("manner_0") != null){
				try{
					Boolean.parseBoolean(map.get("manner_0"));
					setDetailsMapValue("manner_0", map.get("manner_0"));
				}catch(Exception e){
					failedList.add("manner_0");
				}
			}if(map.get("return_tab") != null){
				try{
					Boolean.parseBoolean(map.get("return_tab"));
					setDetailsMapValue("return_tab", map.get("return_tab"));
				}catch(Exception e){
					failedList.add("return_tab");
				}
			}if(map.get("update_tab") != null){
				try{
					Boolean.parseBoolean(map.get("update_tab"));
					setDetailsMapValue("update_tab", map.get("update_tab"));
				}catch(Exception e){
					failedList.add("update_tab");
				}
			}
			if(map.get("fix_volume") != null){
				try{
					Boolean.parseBoolean(map.get("fix_volume"));
					setDetailsMapValue("fix_volenable", map.get("fix_volume"));
				}catch(Exception e){
					failedList.add("fix_volume");
				}
			}if(map.get("fix_volume_value") != null){
				try{
					Integer.parseInt(map.get("fix_volume_value"));
					setDetailsMapValue("fix_volvalue", map.get("fix_volume_value"));
				}catch(Exception e){
					failedList.add("fix_volume_value");
				}
			}if(map.get("orientation") != null){
				try{
					int orient = Integer.parseInt(map.get("orientation"));
					if(orient > 2 || orient < 0){
						throw new Exception();
					}
					setDetailsMapValue("fix_screen", map.get("orientation"));
				}catch(Exception e){
					failedList.add("orientation");
				}
			}if(map.get("discard_notification") != null){
				try{
					Boolean.parseBoolean(map.get("discard_notification"));
					setDetailsMapValue("discard_notification", map.get("discard_notification"));
				}catch(Exception e){
					failedList.add("discard_notification");
				}
			}if(map.get("recent_ts") != null){
				try{
					Boolean.parseBoolean(map.get("recent_ts"));
					setDetailsMapValue("recent_ts", map.get("recent_ts"));
				}catch(Exception e){
					failedList.add("recent_ts");
				}
			}
			if(map.get("alert_enable") != null){
				try{
					Boolean.parseBoolean(map.get("alert_enable"));
					setDetailsMapValue("alert_enable", map.get("alert_enable"));
				}catch(Exception e){
					failedList.add("alert_enable");
				}
			}if(map.get("alert_sound_notif") != null){
				try{
					Boolean.parseBoolean(map.get("alert_sound_notif"));
					setDetailsMapValue("alert_sound_notif", map.get("alert_sound_notif"));
				}catch(Exception e){
					failedList.add("alert_sound_notif");
				}
			}if(map.get("alert_vibration_enable") != null){
				try{
					Boolean.parseBoolean(map.get("alert_vibration_enable"));
					setDetailsMapValue("alert_vibration_enable", map.get("alert_vibration_enable"));
				}catch(Exception e){
					failedList.add("alert_vibration_enable");
				}
			}if(map.get("alert_led") != null){
				try{
					Boolean.parseBoolean(map.get("alert_led"));
					setDetailsMapValue("alert_led", map.get("alert_led"));
				}catch(Exception e){
					failedList.add("alert_led");
				}
			}if(map.get("alert_interval") != null){
				try{
					Byte.parseByte(map.get("alert_interval"));
					setDetailsMapValue("alert_interval", map.get("alert_interval"));
				}catch(Exception e){
					failedList.add("alert_interval");
				}
			}if(map.get("nlr_log") != null){
				try{
					Boolean.parseBoolean(map.get("nlr_log"));
					setDetailsMapValue("nlr_log", map.get("nlr_log"));
				}catch(Exception e){
					failedList.add("nlr_log");
				}
			}
			if(map.get("cmd_anonym") != null){
				try{
					if(map.get("cmd_anonym").equals(CommandValue.ANONYM.toString())){
						setDetailsMapValue("cmd_cmd", CommandValue.ANONYM.toString());
					}

				}catch(Exception e){
					failedList.add("cmd_anonym");
				}
			}else{
					setDetailsMapValue("cmd_cmd", CommandValue.NOANON.toString());
			}
			if(map.get("cmd_size") != null){
				try{
					if(map.get("cmd_size").equals(CommandValue.BIG.toString())
						||map.get("cmd_size").equals(CommandValue.SMALL.toString())
						||map.get("cmd_size").equals(CommandValue.DEFAULT_SIZE.toString())){
						setDetailsMapValue("cmd_size", map.get("cmd_size"));
					}else{
						setDetailsMapValue("cmd_size", CommandValue.DEFAULT_SIZE.toString());
					}

				}catch(Exception e){
					failedList.add("cmd_size");
				}
			}if(map.get("cmd_color") != null){
				try{
					if(map.get("cmd_color").equals(CommandValue.BLACK.toString())
						||map.get("cmd_color").equals(CommandValue.BLUE.toString())
						||map.get("cmd_color").equals(CommandValue.BLUE2.toString())
						||map.get("cmd_color").equals(CommandValue.CYAN.toString())
						||map.get("cmd_color").equals(CommandValue.DEFAULT_COLOR.toString())
						||map.get("cmd_color").equals(CommandValue.ELEMENTALGREEN.toString())
						||map.get("cmd_color").equals(CommandValue.GREEN.toString())
						||map.get("cmd_color").equals(CommandValue.GREEN2.toString())
						||map.get("cmd_color").equals(CommandValue.MADYELLOW.toString())
						||map.get("cmd_color").equals(CommandValue.MARINEBLUE.toString())
						||map.get("cmd_color").equals(CommandValue.NICONICOWHITE.toString())
						||map.get("cmd_color").equals(CommandValue.NOBLEVIOLET.toString())
						||map.get("cmd_color").equals(CommandValue.ORANGE.toString())
						||map.get("cmd_color").equals(CommandValue.ORANGE2.toString())
						||map.get("cmd_color").equals(CommandValue.PASSIONORANGE.toString())
						||map.get("cmd_color").equals(CommandValue.PINK.toString())
						||map.get("cmd_color").equals(CommandValue.PURPLE.toString())
						||map.get("cmd_color").equals(CommandValue.PURPLE2.toString())
						||map.get("cmd_color").equals(CommandValue.RED.toString())
						||map.get("cmd_color").equals(CommandValue.RED2.toString())
						||map.get("cmd_color").equals(CommandValue.TRUERED.toString())
						||map.get("cmd_color").equals(CommandValue.WHITE2.toString())
						||map.get("cmd_color").equals(CommandValue.YELLOW.toString())
						||map.get("cmd_color").equals(CommandValue.YELLOW2.toString())){
						setDetailsMapValue("cmd_color", map.get("cmd_color"));
					}else{
						setDetailsMapValue("cmd_color", "");
					}

				}catch(Exception e){
					failedList.add("cmd_color");
				}
			}if(map.get("cmd_align") != null){
				try{
					if(map.get("cmd_align").equals(CommandValue.UE.toString())
						||map.get("cmd_align").equals(CommandValue.SHITA.toString())
						||map.get("cmd_align").equals(CommandValue.DEFAULT_POS.toString())
						||map.get("cmd_align").equals(CommandValue.MIGI.toString())
						||map.get("cmd_align").equals(CommandValue.HIDARI.toString())){
						setDetailsMapValue("cmd_align", map.get("cmd_align"));
					}else{
						setDetailsMapValue("cmd_align", CommandValue.DEFAULT_SIZE.toString());
					}

				}catch(Exception e){
					failedList.add("cmd_align");
				}
			}
			//SPプレイヤーの設定
			if(map.get("sp_showcomment") != null){
				try{
					boolean val = Boolean.parseBoolean(map.get("sp_showcomment"));
					setDetailsMapValue("sp_showcomment", map.get("sp_showcomment"));
				}catch(Exception e){
					failedList.add("sp_showcomment");
				}
			}
			if(map.get("sp_ng184") != null){
				try{
					boolean val = Boolean.parseBoolean(map.get("sp_ng184"));
					setDetailsMapValue("sp_ng184", map.get("sp_ng184"));
				}catch(Exception e){
					failedList.add("sp_ng184");
				}
			}
			if(map.get("sp_showbspcomment") != null){
				try{
					boolean val = Boolean.parseBoolean(map.get("sp_showbspcomment"));
					setDetailsMapValue("sp_showbspcomment", map.get("sp_showbspcomment"));
				}catch(Exception e){
					failedList.add("sp_showbspcomment");
				}
			}
			if(map.get("sp_ismute") != null){
				try{
					boolean val = Boolean.parseBoolean(map.get("sp_ismute"));
					setDetailsMapValue("sp_ismute", map.get("sp_ismute"));
				}catch(Exception e){
					failedList.add("sp_ismute");
				}
			}
			if(map.get("sp_loadsmile") != null){
				try{
					boolean val = Boolean.parseBoolean(map.get("sp_loadsmile"));
					setDetailsMapValue("sp_loadsmile", map.get("sp_loadsmile"));
				}catch(Exception e){
					failedList.add("sp_loadsmile");
				}
			}
			if(map.get("sp_volumesub") != null){
				try{
					int val = Integer.parseInt(map.get("sp_volumesub"));
					setDetailsMapValue("sp_volumesub", map.get("sp_volumesub"));
				}catch(Exception e){
					failedList.add("sp_volumesub");
				}
			}
			//読み上げ設定値
			if(map.get("speech_enable") != null){
				try{
					if(map.get("speech_enable").equals("true")){//0がTTSのOFF、2がAQUESのOFF
						setDetailsMapValue("speech_enable", "1");
					}else if(map.get("speech_enable").equals("false")){
						setDetailsMapValue("speech_enable", "0");
					}else{
						Byte.parseByte(map.get("speech_enable"));
						setDetailsMapValue("speech_enable", map.get("speech_enable"));
					}
				}catch(Exception e){
					failedList.add("speech_enable");
				}
			}if(map.get("speech_speed") != null){
				try{
					int val = Integer.parseInt(map.get("speech_speed"));
					setDetailsMapValue("speech_speed", map.get("speech_speed"));
				}catch(Exception e){
					failedList.add("speech_speed");
				}
			}if(map.get("speech_pich") != null){
				try{
					int val = Integer.parseInt(map.get("speech_pich"));
					setDetailsMapValue("speech_pich", map.get("speech_pich"));
				}catch(Exception e){
					failedList.add("speech_pich");
				}
			}if(map.get("speech_education_enable") != null){
				try{
					Boolean.parseBoolean(map.get("speech_education_enable"));
					setDetailsMapValue("speech_education_enable", map.get("speech_education_enable"));
				}catch(Exception e){
					failedList.add("speech_education_enable");
				}
			}if(map.get("speech_skip_word") != null){
				try{
					String.valueOf(map.get("speech_skip_word"));
					setDetailsMapValue("speech_skip_word", map.get("speech_skip_word"));
				}catch(Exception e){
					failedList.add("speech_skip_word");
				}
			}if(map.get("speech_skip_count") != null){
				try{
					int val = Integer.parseInt(map.get("speech_skip_count"));
					if(val > 10 || val < 0){
						throw new Exception();
					}
					setDetailsMapValue("speech_skip_count", map.get("speech_skip_count"));
				}catch(Exception e){
					failedList.add("speech_skip_count");
				}
			}if(map.get("speech_aques_phont") != null){
				try{
					int val = Integer.parseInt(map.get("speech_aques_phont"));
					if(val > 9 || val < 0){
						throw new Exception();
					}
					setDetailsMapValue("speech_aques_phont", map.get("speech_aques_phont"));
				}catch(Exception e){
					failedList.add("speech_aques_phont");
				}
			}if(map.get("speech_aques_vol") != null){
				try{
					int val = Integer.parseInt(map.get("speech_aques_vol"));
					if(val > 10 || val < 0){
						throw new Exception();
					}
					setDetailsMapValue("speech_aques_vol", map.get("speech_aques_vol"));
				}catch(Exception e){
					failedList.add("speech_aques_vol");
				}
			}
			if(map.get("player_pos_p") != null){
				try{
					int val = Integer.parseInt(map.get("player_pos_p"));
					if(val > 2 || val < 0){
						throw new Exception();
					}
					setDetailsMapValue("player_pos_p", map.get("player_pos_p"));
				}catch(Exception e){
					failedList.add("portlayt_settings:player_position");
				}
			}if(map.get("x_pos_p") != null){
				try{
					int val = Integer.parseInt(map.get("x_pos_p"));
					if(val > 100 || val < -100){
						throw new Exception();
					}
					setDetailsMapValue("x_pos_p", map.get("x_pos_p"));
				}catch(Exception e){
					failedList.add("portlayt_settings:x_position");
				}
			}if(map.get("xd_enable_p") != null){
				try{
					Boolean.parseBoolean(map.get("xd_enable_p"));
					setDetailsMapValue("xd_enable_p", map.get("xd_enable_p"));
				}catch(Exception e){
					failedList.add("portlayt_settings:x_dragging");
				}
			}if(map.get("y_pos_p") != null){
				try{
					int val = Integer.parseInt(map.get("y_pos_p"));
					if(val > 100 || val < 0){
						throw new Exception();
					}
					setDetailsMapValue("y_pos_p", map.get("y_pos_p"));
				}catch(Exception e){
					failedList.add("portlayt_settings:y_position");
				}
			}if(map.get("yd_enable_p") != null){
				try{
					Boolean.parseBoolean(map.get("yd_enable_p"));
					setDetailsMapValue("yd_enable_p", map.get("yd_enable_p"));
				}catch(Exception e){
					failedList.add("portlayt_settings:y_dragging");
				}
			}if(map.get("bottom_pos_p") != null){
				try{
					int val = Integer.parseInt(map.get("bottom_pos_p"));
					if(val > 100 || val < -100){
						throw new Exception();
					}
					setDetailsMapValue("bottom_pos_p", map.get("bottom_pos_p"));
				}catch(Exception e){
					failedList.add("portlayt_settings:height");
				}
			}if(map.get("width_p") != null){
				try{
					int val = Integer.parseInt(map.get("width_p"));
					if(val > 100 || val < 0){
						throw new Exception();
					}
					setDetailsMapValue("width_p", map.get("width_p"));
				}catch(Exception e){
					failedList.add("portlayt_settings:width");
				}
			}if(map.get("cellheight_p") != null){
				try{
					int val = Integer.parseInt(map.get("cellheight_p"));
					if(val > 10 || val < 0){
						throw new Exception();
					}
					setDetailsMapValue("cellheight_p", map.get("cellheight_p"));
				}catch(Exception e){
					failedList.add("portlayt_settings:font_size");
				}
			}if(map.get("type_width_p") != null){
				try{
					int val = Integer.parseInt(map.get("type_width_p"));
					if(val > 100 || val < 0){
						throw new Exception();
					}
					setDetailsMapValue("type_width_p", map.get("type_width_p"));
				}catch(Exception e){
					failedList.add("portlayt_settings:type_width");
				}
			}if(map.get("id_width_p") != null){
				try{
					int val = Integer.parseInt(map.get("id_width_p"));
					if(val > 100 || val < 0){
						throw new Exception();
					}
					setDetailsMapValue("id_width_p", map.get("id_width_p"));
				}catch(Exception e){
					failedList.add("portlayt_settings:id_width");
				}
			}if(map.get("command_width_p") != null){
				try{
					int val = Integer.parseInt(map.get("command_width_p"));
					if(val > 100 || val < 0){
						throw new Exception();
					}
					setDetailsMapValue("command_width_p", map.get("command_width_p"));
				}catch(Exception e){
					failedList.add("portlayt_settings:cmd_width");
				}
			}if(map.get("time_width_p") != null){
				try{
					int val = Integer.parseInt(map.get("time_width_p"));
					if(val > 100 || val < 0){
						throw new Exception();
					}
					setDetailsMapValue("time_width_p", map.get("time_width_p"));
				}catch(Exception e){
					failedList.add("portlayt_settings:time_width");
				}
			}if(map.get("score_width_p") != null){
				try{
					int val = Integer.parseInt(map.get("score_width_p"));
					if(val > 100 || val < 0){
						throw new Exception();
					}
					setDetailsMapValue("score_width_p", map.get("score_width_p"));
				}catch(Exception e){
					failedList.add("portlayt_settings:score_width");
				}
			}if(map.get("num_width_p") != null){
				try{
					int val = Integer.parseInt(map.get("num_width_p"));
					if(val > 100 || val < 0){
						throw new Exception();
					}
					setDetailsMapValue("num_width_p", map.get("num_width_p"));
				}catch(Exception e){
					failedList.add("portlayt_settings:num_width");
				}
			}if(map.get("comment_width_p") != null){
				try{
					int val = Integer.parseInt(map.get("comment_width_p"));
					if(val > 100 || val < 0){
						throw new Exception();
					}
					setDetailsMapValue("comment_width_p", map.get("comment_width_p"));
				}catch(Exception e){
					failedList.add("portlayt_settings:comment_width");
				}
			}if(map.get("player_pos_l") != null){
				try{
					int val = Integer.parseInt(map.get("player_pos_l"));
					if(val > 2 || val < 0){
						throw new Exception();
					}
					setDetailsMapValue("player_pos_l", map.get("player_pos_l"));
				}catch(Exception e){
					failedList.add("landscape_settings:player_position");
				}
			}if(map.get("x_pos_l") != null){
				try{
					int val = Integer.parseInt(map.get("x_pos_l"));
					if(val > 100 || val < -100){
						throw new Exception();
					}
					setDetailsMapValue("x_pos_l", map.get("x_pos_l"));
				}catch(Exception e){
					failedList.add("landscape_settings:x_position");
				}
			}if(map.get("xd_enable_l") != null){
				try{
					Boolean.parseBoolean(map.get("xd_enable_l"));
					setDetailsMapValue("xd_enable_l", map.get("xd_enable_l"));
				}catch(Exception e){
					failedList.add("landscape_settings:x_dragging");
				}
			}if(map.get("y_pos_l") != null){
				try{
					int val = Integer.parseInt(map.get("y_pos_l"));
					if(val > 100 || val < 0){
						throw new Exception();
					}
					setDetailsMapValue("y_pos_l", map.get("y_pos_l"));
				}catch(Exception e){
					failedList.add("landscape_settings:y_position");
				}
			}if(map.get("yd_enable_l") != null){
				try{
					Boolean.parseBoolean(map.get("yd_enable_l"));
					setDetailsMapValue("yd_enable_l", map.get("yd_enable_l"));
				}catch(Exception e){
					failedList.add("landscape_settings:y_dragging");
				}
			}if(map.get("bottom_pos_l") != null){
				try{
					int val = Integer.parseInt(map.get("bottom_pos_l"));
					if(val > 100 || val < -100){
						throw new Exception();
					}
					setDetailsMapValue("bottom_pos_l", map.get("bottom_pos_l"));
				}catch(Exception e){
					failedList.add("landscape_settings:height");
				}
			}if(map.get("width_l") != null){
				try{
					int val = Integer.parseInt(map.get("width_l"));
					if(val > 100 || val < 0){
						throw new Exception();
					}
					setDetailsMapValue("width_l", map.get("width_l"));
				}catch(Exception e){
					failedList.add("landscape_settings:width");
				}
			}if(map.get("cellheight_l") != null){
				try{
					int val = Integer.parseInt(map.get("cellheight_l"));
					if(val > 10 || val < 0){
						throw new Exception();
					}
					setDetailsMapValue("cellheight_l", map.get("cellheight_l"));
				}catch(Exception e){
					failedList.add("landscape_settings:font_size");
				}
			}if(map.get("type_width_l") != null){
				try{
					int val = Integer.parseInt(map.get("type_width_l"));
					if(val > 100 || val < 0){
						throw new Exception();
					}
					setDetailsMapValue("type_width_l", map.get("type_width_l"));
				}catch(Exception e){
					failedList.add("landscape_settings:type_width");
				}
			}if(map.get("id_width_l") != null){
				try{
					int val = Integer.parseInt(map.get("id_width_l"));
					if(val > 100 || val < 0){
						throw new Exception();
					}
					setDetailsMapValue("id_width_l", map.get("id_width_l"));
				}catch(Exception e){
					failedList.add("landscape_settings:id_width");
				}
			}if(map.get("command_width_l") != null){
				try{
					int val = Integer.parseInt(map.get("command_width_l"));
					if(val > 100 || val < 0){
						throw new Exception();
					}
					setDetailsMapValue("command_width_l", map.get("command_width_l"));
				}catch(Exception e){
					failedList.add("landscape_settings:cmd_width");
				}
			}if(map.get("time_width_l") != null){
				try{
					int val = Integer.parseInt(map.get("time_width_l"));
					if(val > 100 || val < 0){
						throw new Exception();
					}
					setDetailsMapValue("time_width_l", map.get("time_width_l"));
				}catch(Exception e){
					failedList.add("landscape_settings:time_width");
				}
			}if(map.get("score_width_l") != null){
				try{
					int val = Integer.parseInt(map.get("score_width_l"));
					if(val > 100 || val < 0){
						throw new Exception();
					}
					setDetailsMapValue("score_width_l", map.get("score_width_l"));
				}catch(Exception e){
					failedList.add("landscape_settings:score_width");
				}
			}if(map.get("num_width_l") != null){
				try{
					int val = Integer.parseInt(map.get("num_width_l"));
					if(val > 100 || val < 0){
						throw new Exception();
					}
					setDetailsMapValue("num_width_l", map.get("num_width_l"));
				}catch(Exception e){
					failedList.add("landscape_settings:num_width");
				}
			}if(map.get("comment_width_l") != null){
				try{
					int val = Integer.parseInt(map.get("comment_width_l"));
					if(val > 100 || val < 0){
						throw new Exception();
					}
					setDetailsMapValue("comment_width_l", map.get("comment_width_l"));
				}catch(Exception e){
					failedList.add("landscape_settings:comment_width");
				}
			}
			if(map.get("cellheight_test") != null){
				try{
					int val = Integer.parseInt(map.get("cellheight_test"));
					if(val > 5 || val < 1){
						throw new Exception();
					}
					setDetailsMapValue("cellheight_test", map.get("cellheight_test"));
				}catch(Exception e){
					failedList.add("landscape_settings:cellheight_test");
				}
			}
			//順序
			if(map.get("type_seq") != null){
				try{
					int[] vals = new int[7];
					vals[0] = Integer.parseInt(map.get("type_seq"));
					vals[1] = Integer.parseInt(map.get("id_seq"));
					vals[2] = Integer.parseInt(map.get("cmd_seq"));
					vals[3] = Integer.parseInt(map.get("time_seq"));
					vals[4] = Integer.parseInt(map.get("num_seq"));
					vals[5]= Integer.parseInt(map.get("score_seq"));
					vals[6] = Integer.parseInt(map.get("comment_seq"));
					for(int i = 0; i < 7; i++){
						for(int j = 0; j < 7; j++){
							if(vals[i] == vals[j]&&i!=j){
								throw new Exception();
							}
						}
					}
					setDetailsMapValue("type_seq", map.get("type_seq"));
					setDetailsMapValue("id_seq", map.get("id_seq"));
					setDetailsMapValue("cmd_seq", map.get("cmd_seq"));
					setDetailsMapValue("time_seq", map.get("time_seq"));
					setDetailsMapValue("score_seq", map.get("score_seq"));
					setDetailsMapValue("num_seq", map.get("num_seq"));
					setDetailsMapValue("comment_seq", map.get("comment_seq"));
				}catch(Exception e){
					e.printStackTrace();
					failedList.add("column_sequence");
				}
			}


			if(map.get("toptab_tcolor") != null){
				try{
					int val = Integer.parseInt(map.get("toptab_tcolor"));
					if(val > 5 || val < 0){
						throw new Exception();
					}
					setDetailsMapValue("toptab_tcolor", map.get("toptab_tcolor"));
				}catch(Exception e){
					failedList.add("landscape_settings:toptab_tcolor");
				}
			}

				return failedList;
	}












}
