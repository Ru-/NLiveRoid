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
import java.io.PrintStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import nliveroid.nlr.main.parser.XMLparser;

import org.apache.http.ParseException;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.util.Xml;
import android.view.View;

public class CommentTable{

	private CommentListAdapter adapter;
	private LiveInfo liveinfo;
	private Socket sock;
	private DataOutputStream msgOutStream;
	private Socket postSocket;
	private InputStream postIn;
	private DataOutputStream postOut;
	private String addr;
	private SAXParser mParser;
	private InputStream msgInStream;

	private ArrayList<String[]> buffer;
	private ArrayList<String[]> logList;
	private boolean isBuffering = true;
	private long lastGetTime = 0;

	private ErrorCode error;

	private String session;

	private GetUserNameManager autoUserNameTask;

	private AsyncTask<Integer,Void, Void> mainThread;//型が画一的になっちゃう
	private String[] mThread;
	private int[] mPort;
	private AsyncTask<Void, Void, Void> logTask;
	private static int nowSeetIndex;


	private boolean isEnd;
	private boolean isUpdating;
	private String temp_id;
	private String temp_comment;

	private boolean isAutoUser;


	private int busy =0;
	private CommentPostable postable;

	private Pattern atPt;

	private String matchTemp = "";

	private static Speechable mSpeech;
	//ダイアログから設定されるためにフィールド化
	private byte speech_KindValue;
	private boolean speech_education;
	private String spech_skip_word;
	private byte speech_max_buf;
	private LinkedHashMap<Pattern, String> educationMap;
	private ArrayBlockingQueue<String> readComments;
	final private String[] ngString = new String[]{"",URLEnum.HYPHEN,"","","","","NG"};
//	private AddSpeech addSpeechTask;//Aquesで非同期にならないから

	private int updateCount = 0;


	private int lastCommentCount = 0;//NG判定で利用
	private boolean isOfficial = false;
	private HashSet<String> escapeNG = new HashSet<String>();

	private SortTask sortTask;

	//各のコンストラクタ 起動元分岐 column_seq消してない
	public CommentTable(byte i,LiveInfo liveinfo,CommentPostable overlay,CommentListAdapter adapter,byte[] column_seq,ErrorCode threadError,String session,
			byte isSpeechEnable,boolean speech_education,byte speech_pich,byte speech_speed,byte speech_vol,String skip_word,byte max_speech_buf,int init_count,boolean isAutoUserName) {
		if(NLiveRoid.isDebugMode)Log.d("NLiveRoid","CommentTable Const ---");
		this.liveinfo = liveinfo;
		this.error = threadError;
		this.session = session;
		this.adapter = adapter;
		this.isAutoUser = isAutoUserName;
		this.speech_KindValue = isSpeechEnable;
		this.speech_education = speech_education;
		this.spech_skip_word = skip_word;
		this.speech_max_buf = max_speech_buf;
		this.adapter = adapter;
		this.postable = overlay;
		initSpeech( speech_KindValue,postable.getAPPContext(),speech_speed,speech_vol,speech_pich,skip_word,max_speech_buf);
		createNewCommentTable(i,init_count);
	}

	public void createNewCommentTable(int init_mode,int init_count){
		//ソケット処理が重いので、ソケット処理から別タスク
		switch(init_mode){
		case 0://OverLay,BCOverLa
			atPt = Pattern.compile("@|＠");//これが何故かコメ数が多い放送でnullになる場合がある
			buffer = new ArrayList<String[]>();
			// ここまでnow loadingにしておく
			mainThread = new GetCommentLoopTask();
			mainThread.execute(init_count);
			break;
		case 1://APIレベル8未満
			atPt = Pattern.compile("@|＠");
			buffer = new ArrayList<String[]>();
			mainThread = new lowLevelGetCommentRoopTask();
			mainThread.execute(init_count);
			break;
		}
		if(isAutoUser){
			autoUserNameTask = new GetUserNameManager();
			autoUserNameTask.execute();
		}
	}

	class GetCommentLoopTask extends AsyncTask<Integer,Void,Void>{

		@Override
		protected Void doInBackground(Integer... args) {
			try{
//				Log.d("NLiveCC"," loopTask");
		String s = "";
		if(args[0] == CODE.GET_BETWEEN_UPDATE){
			initialSocket(20);//間を取得する設定
//			Log.d("NLiveCC","DeleteBufferMark");
			new DeleteBufferMark().execute();
		}else if(args[0] == CODE.SIMPLE_UPDATE){
			initialSocket(-1);//シンプル更新
			new DeleteBufferMark().execute();
		}else{
			if(args[0] ==CODE.CHANGE_SEET_UPDATE){//CHANGE_SEET_UPDATEだったらソケットはすでに初期化されている(座席変更から)
				if(buffer!= null)buffer.clear();
			}else{
			initialSocket(args[0]);//初期化時
			new DeleteBufferMark().execute();
			}
			FirstParser firstParser = new FirstParser();
						//初回1件取得して公式か判定
					while (sock != null && !sock.isClosed()) {
							// コメント取得用インプットストリームをパースする
							s = getUTFString(msgInStream);
							//公式でもlast_resはある
							//一番最初はchatじゃない例 <thread resultcode="0" thread="1173394001" last_res="2539" ticket="0x29190000" revision="1" server_time="1335713513"/>
							if(s == null||!s.contains("chat")){
								continue;
							}
//							Log.d("NLiveCC","First " + s);
							InputSource source = new InputSource(new StringReader(s));
							mParser = SAXParserFactory.newInstance().newSAXParser();
							mParser.parse(source, firstParser);
							break;
					}
//					Log.d("NLiveCC","firstWhileEND");
			}
//		Log.d("NLiveCC","Mainloopstart");
		CommentParser parser = new CommentParser();
				//メインのループ
			while (sock != null && !sock.isClosed()) {
				if(isEnd){
//					Log.d("NLiveCC","isEnd true");
					closeMainConnection();
					break;
				}
				// コメント取得用インプットストリームをパースする
				s = getUTFString(msgInStream);
				InputSource source = new InputSource(new StringReader(s));
				mParser = SAXParserFactory.newInstance().newSAXParser();
//				Log.d("NLiveCC","Parse");
				mParser.parse(source, parser);
			}
//			Log.d("NLiveCC","Endmainloop--------------------------");

			}catch(OutOfMemoryError e){
				error.setErrorCode(-22);
				e.printStackTrace();
			} catch(SAXException e) {
				Log.d("CommentTable","ParseException at CommentParser()");
//				e.printStackTrace();
			} catch (ParserConfigurationException e) {
				error.setErrorCode(-13);
				e.printStackTrace();
			} catch (IOException e) {
				error.setErrorCode(-13);
				e.printStackTrace();
			}catch(Exception e){
				e.printStackTrace();
			}
			return null;
		}
		@Override
		protected void onPostExecute(Void arg){
			if(error != null && error.getErrorCode() != 0){
				error.showErrorToast();
			}
		}


	}

	class lowLevelGetCommentRoopTask extends AsyncTask<Integer,Void,Void>{

		@Override
		protected Void doInBackground(Integer... args) {
			try {
				String s = "";
			if(args[0] ==CODE.GET_BETWEEN_UPDATE){
			initialSocket(20);//座席変更の際は、そけっとはもうある
			//一度バッファマーク消す
			new DeleteBufferMark().execute();
			}else if(args[0] == CODE.SIMPLE_UPDATE){
				initialSocket(-1);//座席変更の際は、そけっとはもうある
				//一度バッファマーク消す
				new DeleteBufferMark().execute();
			}else{
					if(args[0] ==CODE.CHANGE_SEET_UPDATE){//CHANGE_SEET_UPDATEだったらソケットはすでに初期化されている(座席変更から)
						if(buffer!= null)buffer.clear();
					}else{
						initialSocket(args[0]);//初期化時
					new DeleteBufferMark().execute();
				}
			//初回1件取得して公式か判定

			while (sock != null && !sock.isClosed()) {
				// コメント取得用インプットストリームをパースする
				s = getUTFString(msgInStream);

				//公式でもlast_resはある
				//一番最初はchatじゃない
				if(s == null||!s.contains("chat")){
					continue;
				}
				s = getUTFString(msgInStream);
				LowLevelFirstParser(s);
				break;
			}
			}

			while (sock != null && !sock.isClosed()) {
				if(isEnd){
					closeMainConnection();
					break;
				}
				// コメント取得用インプットストリームをパースする

					s = getUTFString(msgInStream);
					lowLevelParser(s);
			}
			}catch(OutOfMemoryError e){
				error.setErrorCode(-22);
				e.printStackTrace();
			}catch (IOException e) {
				error.setErrorCode(-13);
				e.printStackTrace();
			}catch(Exception e){
				e.printStackTrace();
			}
			return null;
		}
		@Override
		protected void onPostExecute(Void arg){
			if(error != null && error.getErrorCode() != 0){
				error.showErrorToast();
			}
		}
	}

	//各OverLay,lowLevelAPI用ソケット生成
	private void initialSocket(int init_count){
		try {
			//座席配列の計算
			addr = liveinfo.getAddr();
			//アリーナのステータスを算出
			String roomLabel = liveinfo.getRoomlabel();
			String arinaThreadNum = "";
			int arinaPortNum = 0;
			if(roomLabel.contains("立ち見A")){
				nowSeetIndex = 1;
				arinaThreadNum=String.valueOf(Integer.parseInt(liveinfo.getThread())-1);
				int port = Integer.parseInt(liveinfo.getPort());
				if(port ==2805){
					arinaPortNum = 2814;
				}else{
					arinaPortNum = port-1;
				}
			}else if(roomLabel.contains("立ち見B")){//立見B
				arinaThreadNum=String.valueOf(Integer.parseInt(liveinfo.getThread())-2);
				int port = Integer.parseInt(liveinfo.getPort());
				nowSeetIndex = 2;
				if(port==2805){
					arinaPortNum = 2813;
				}else if(port==2806){
					arinaPortNum = 2814;
				}else{
					arinaPortNum = port-2;
				}
			}else if(roomLabel.contains("立ち見C")){//立見C
				nowSeetIndex = 3;
				arinaThreadNum=String.valueOf(Integer.parseInt(liveinfo.getThread())-3);
				int port = Integer.parseInt(liveinfo.getPort());
				if(port==2805){
					arinaPortNum = 2812;
				}else if(port==2806){
					arinaPortNum = 2813;
				}else if(port==2807){
					arinaPortNum = 2814;
				}else{
					arinaPortNum = port-3;
				}
			}else {//ユーザー生ならアリーナ(co) 公式なら(デッキ) 等
				nowSeetIndex = 0;
				arinaThreadNum = liveinfo.getThread();
				arinaPortNum= Integer.parseInt(liveinfo.getPort());
			}
			//アリーナからそれぞれの席のステータスに直す
			mThread = new String[]{arinaThreadNum,String.valueOf(Integer.parseInt(arinaThreadNum)+1),String.valueOf(Integer.parseInt(arinaThreadNum)+2),String.valueOf(Integer.parseInt(arinaThreadNum)+3)};

			if(arinaPortNum == 2812){
				mPort = new int[]{arinaPortNum,2813,2814,2805};
			}else if(arinaPortNum == 2813){
				mPort = new int[]{arinaPortNum,2814,2805,2806};
			}else if(arinaPortNum == 2814){
				mPort = new int[]{arinaPortNum,2805,2806,2807};
			}else{
				mPort = new int[]{arinaPortNum,arinaPortNum+1,arinaPortNum+2,arinaPortNum+3};
			}
			//デフォルトの席で初期化
			sock = new Socket(addr, mPort[nowSeetIndex]);

			// ソケットで空けた通信口を開いておく
			sock.setKeepAlive(true);

			// DataOutputStreamは移植性のある出力ストリーム
			msgOutStream = new DataOutputStream(sock.getOutputStream());
			// コメント取得用パケットを投げる
			msgOutStream.writeBytes(String.format(URLEnum.GETCOMMENTXML,
					mThread[nowSeetIndex], init_count+1));// コメントを取得する件数 初期化分+1

			msgInStream = sock.getInputStream();// このインプットストリームをパーサに渡す
		} catch (UnknownHostException e) {
			error.setErrorCode(-3);
			e.printStackTrace();
		} catch (IOException e) {
			error.setErrorCode(-13);
			e.printStackTrace();
		} catch (NumberFormatException e) {
			// ポート番号の変換で発生
			// 放送情報の取得に失敗
			error.setErrorCode(-13);
			e.printStackTrace();
		}
	}

	/**
	 * 読み上げインスタンスの初期化
	 */
	public void initSpeech(byte kind,Context context,byte speed,byte vol,byte pich,String skip_word,byte maxBuf){
		if(mSpeech != null)mSpeech.destroy();
		switch(kind){
		case 1:
			initEducation();
			mSpeech = new TTSSpeech(skip_word,maxBuf);
			mSpeech.setContext((Activity)postable,speed,pich);//ここはAquesと違って、appのbaseContextじゃないと初期化されない!!!!
			break;
		case 3:
			initEducation();
			mSpeech = new AquesSpeech(skip_word,maxBuf,vol);
			mSpeech.setContext((Activity)postable,speed,pich);//pichはphont
			break;
		}
	}


	/**
	 * 教育データ初期化
	 */
	private void initEducation(){
		//ファイルから教育データを読み込む
		LinkedHashMap<String,String> tempMap = new LinkedHashMap<String,String>();
		//ファイルを読み込む
		String filePath = getStorageFilePath();
		if(filePath == null){
			new ErrorOutput().execute(-1);
			return;
		}
		File file = new File(filePath,"Education.xml");
		if(!file.exists()){
			try {
				file.createNewFile();//ファイル無ければテンプレ作成
				String xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"+
				"<Education xmlns=\"http://nliveroid-tutorial.appspot.com/education/\">\n"+
				"<data>\n" +
				"<key>(http|https):([^\\x00-\\x20()\"&lt;&gt;\\x7F-\\xFF])*</key>\n" +
				"<value>URL省略</value>\n"+
				"</data>\n"+
			    "</Education>\n";
				FileOutputStream fos = new FileOutputStream(file.getPath());
				fos.write(xml.getBytes());
				fos.close();
				tempMap.put("(http|https):([^\\x00-\\x20()\"&lt;&gt;\\x7F-\\xFF])*", "URL省略");

				new ErrorOutput().execute(1);
				//リストを生成(if文を抜ける)
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				new ErrorOutput().execute(-2);
				return;
			} catch (IOException e) {
				e.printStackTrace();
				new ErrorOutput().execute(-3);
				return;
			}
		}else{//ファイルあった

			byte[] data = new byte[(int)((file).length())];
			FileInputStream fis = null;
			try {
			fis = new FileInputStream(file);
			fis.read(data);
			fis.close();
		int parseError = XMLparser.parseLiveEducation(data,tempMap);

		if(parseError == -1){
			new ErrorOutput().execute(-4);
			return;
		}else if(parseError == -2){
			new ErrorOutput().execute(-5);
			return;
		}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				new ErrorOutput().execute(-2);
				return;
			} catch (IOException e) {
				new ErrorOutput().execute(-3);
				return;
			}
			//教育ファイル読み込み成功
			educationMap = new LinkedHashMap<Pattern,String>();
			Iterator<String> it = tempMap.keySet().iterator();
			String temp = "";
			while(it.hasNext()){
				temp = it.next();
				Pattern pt = Pattern.compile(temp);
				educationMap.put(pt,tempMap.get(temp));
			}

		}
	}

	  private String getStorageFilePath(){
		  boolean isStorageAvalable = false;
			boolean isStorageWriteable = false;
			String state = Environment.getExternalStorageState();
			if(state == null){
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
				return null;
			}


			//sdcard直下に、パッケージ名のフォルダを作りファイルを生成
			String filePath = Environment.getExternalStorageDirectory().toString() + "/NLiveRoid";

			File directory = new File(filePath);
			if(directory.mkdirs()){//すでにあった場合も失敗する
				Log.d("log","mkdir");
			}

			return filePath;
	  }
	//読み上げ読み込みエラー出力のためだけ
	class ErrorOutput extends AsyncTask<Integer,Void ,Integer>{
		@Override
		protected Integer doInBackground(Integer... params) {
			return params[0];
		}
		@Override
		protected void onPostExecute(Integer arg){
			switch(arg){
			case 1:
				//一番初期に、生成してアダプタ追加
				MyToast.customToastShow((Context) postable, "教育ファイル生成しました");
				break;
			case -1:
				MyToast.customToastShow((Context) postable, "ストレージが利用できませんでした");
				break;
			case -2:
				MyToast.customToastShow((Context) postable, "ファイルIOに失敗しました");
				break;
			case -3:
				MyToast.customToastShow((Context) postable, "ファイルIOに失敗しました");
				break;
			case -4:
				MyToast.customToastShow((Context) postable, "教育ファイルの記述がおかしい");
				break;
			case -5:
				MyToast.customToastShow((Context) postable, "教育ファイルの記述がおかしい");
				break;
			}
		}
	}
	/**
	 * 座席変更で新たなテーブルに残さないためのバッファクリア
	 */
	public void newSeetRady(){
	}

	/**
	 * 座席変更
	 * @throws IOException
	 * @throws UnknownHostException
	 *
	 */
	public byte selectSeet(int seet,short initCommentCount,int isLowAPI) throws UnknownHostException, IOException{
//		Log.d("Log","NewSeet Thread "+ mThread[seet] + " Port " + mPort);
		//ソケットに全く繋げなかったらportがnull?
		if(liveinfo == null )return -1;
		if(mPort == null)return -2;
		//NG間違いを避ける為の配列を消す
		if(escapeNG != null)escapeNG.clear();
		closeMainConnection();
		if(adapter != null)adapter.clear();
		if(buffer!=null)buffer.clear();

			sock = new Socket(liveinfo.getAddr(), mPort[seet]);
			// ソケットで空けた通信口を開いておく
			sock.setKeepAlive(true);
			// アウトプットを確立
			msgOutStream = new DataOutputStream(sock.getOutputStream());
			isEnd = false;//ぎりぎりで再開
			// コメント取得用パケットを投げる
			msgOutStream.writeBytes(String.format(URLEnum.GETCOMMENTXML,
					mThread[seet], initCommentCount));// コメントを取得する件数 座席変更は固定で20

		msgInStream = sock.getInputStream();// このインプットストリームをパーサに渡す
		// ここまでnow loadingにしておく
		if(isLowAPI == 1){
			mainThread = new lowLevelGetCommentRoopTask();
			mainThread.execute(CODE.CHANGE_SEET_UPDATE);
		}else{
		mainThread = new GetCommentLoopTask();
		mainThread.execute(CODE.CHANGE_SEET_UPDATE);
		}
		return 0;
	}


	public synchronized void closeMainConnection() {
		isEnd = true;
		if(NLiveRoid.isDebugMode)Log.d("NLiveRoid","closeMainConnection -- ");
		try {
			cancellAutoUserName();
			cancellReadTask();
			if(mainThread != null){
				mainThread.cancel(true);
			}
			if (msgInStream != null) {
				msgInStream.close();
			}
			if(msgOutStream != null){
				msgOutStream.close();
			}
			if(sock!=null){
				sock.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public synchronized void closeLogConnection() {
		try {
			if(logTask != null){
				logTask.cancel(true);
			}
			if (logInStream != null) {
				logInStream.close();
			}
			if(logOutStream != null){
				logOutStream.close();
			}
			if(logSock!=null){
				logSock.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean isClosed(){
		return sock==null? true:sock.isClosed();
	}


	/**
	 * 初期化パース
	 * @param handler
	 *            使用する CommentHandler_Format
	 */
	// コメント取得用パーサメイン初期化
	class FirstParser extends MyDefaultHandler {
		private ReceiveHandlingFormatter formatter = new ReceiveHandlingFormatter();
		private Map<String, String> attributesMap = new HashMap<String, String>();
		private String[] newRecord;
		@Override
		public void start(String uri, String localName, String qName,
				Attributes attributes) {
//			Log.d("NLiveCC","firstParser start" +qName);
			if (qName.equals("chat")) {
				int len = attributes.getLength();
				for (int i = 0; i < len; i++) {// パケットの属性値をキーに全て突っ込む
					attributesMap.put(attributes.getQName(i),
							attributes.getValue(i));
				}

				// 会員タイプなしで一般
				if (!attributesMap.containsKey(CommentRecieveFormat.USER_TYPE)) {
					attributesMap.put(CommentRecieveFormat.USER_TYPE, "");
				}
			}
		}

		@Override
		public void end(String uri, String localName, String qName) {
//			Log.d("NLiveCC","firstParser end" + qName);
			if (qName.equals("chat")) {
				String type = attributesMap.get(CommentRecieveFormat.USER_TYPE);
				// システムが/disconnectを投げたら終了 "2"SYSTEM "3"OWNER
				//disconnectで終了すると、復活した時にそれ以上コメントが読み込まれない
				if (type != null && (type.equals("3") || type.equals("2"))) {
					if (getInnerText().equals("/disconnect")) { // disconnectが見えたらソケットを閉じる
//						isEnd = true;
					}
				}

				// コメントのプロパティをRecievクラスのフィールドにセット
				formatter.commentAttrReceived(attributesMap);
				newRecord = formatter
						.getReceivedComment(getInnerText());//コメントだけはここで突っ込む
				formatter.clear();

				if (newRecord == null)
					return;

				//投稿時間計算
				String hour = "";
				int startTime = Integer.parseInt(liveinfo.getStartTime());
				long passedminute = (Long.parseLong(newRecord[3]) - startTime) / 60;
				String minute = String.format("%02d", passedminute);
				String second = String.format("%02d",
						(Long.parseLong(newRecord[3]) - startTime) % 60);
				if (passedminute > 59) {
					hour = String.format("%d:",
							(Long.parseLong(newRecord[3]) - startTime) / 3600);
					minute = String
							.format("%02d",
									((Long.parseLong(newRecord[3]) - startTime) % 3600) / 60);
				}
				newRecord[3] = hour + minute + ":" + second;
				try {
					if (newRecord[5].equals(URLEnum.HYPHEN)||newRecord[5].equals("")) {// 公式はコメ番がない
						isOfficial = true;
					}else{
						lastCommentCount = Integer.parseInt(newRecord[5]);
					}
				} catch (NumberFormatException e) {
					e.printStackTrace();
//					Log.d("NLiveCC","Failed Integer.parseInt");
					isEnd = true;
					closeMainConnection();
					closeLogConnection();
				}

							// テーブルに行追加
						lastGetTime = System.currentTimeMillis();
						//AddRowTaskはここだけにしないと複雑になりすぎる
						//バッファと普通のが不整合する可能性があるので気をつける
						buffer.add(newRecord);

						//ここでflushBufferTaskはnullでexecuteするんだけど、
						//何故かdoInBackGroundが呼ばれずコメ欄がホワイトアウトするのでメソッド化
						firstFlushBuffer();

			}
			attributesMap.clear();
		}// End of end
	}// End of commentChapture


	/**
	 * 指定された CommentHandler_Format を使用してコメントをパース
	 *
	 * @param handler
	 *            使用する CommentHandler_Format
	 */
	// コメント取得用パーサメイン
	class CommentParser extends MyDefaultHandler {
		private ReceiveHandlingFormatter formatter = new ReceiveHandlingFormatter();
		private Map<String, String> attributesMap = new HashMap<String, String>();
		private String[] newRecord;

		@Override
		public void start(String uri, String localName, String qName,
				Attributes attributes) {
			if (qName.equals("chat")) {
//				Log.d("NLiveCC","start chat");
				int len = attributes.getLength();
				for (int i = 0; i < len; i++) {// パケットの属性値をキーに全て突っ込む
					attributesMap.put(attributes.getQName(i),
							attributes.getValue(i));
				}

				// 会員タイプなしで一般
				if (!attributesMap.containsKey(CommentRecieveFormat.USER_TYPE)) {
					attributesMap.put(CommentRecieveFormat.USER_TYPE, "");
				}
			}
		}

		@Override
		public void end(String uri, String localName, String qName) {
			if (qName.equals("chat")) {
//				Log.d("NLiveCC","end chat isUpdating" + isUpdating);
				String type = attributesMap.get(CommentRecieveFormat.USER_TYPE);
				// システムが/disconnectを投げたら終了 "2"SYSTEM "3"OWNER
				//disconnectで終了すると、復活した時にそれ以上コメントが読み込まれない
				if (type != null && (type.equals("3") || type.equals("2"))) {
					if (getInnerText().equals("/disconnect")) { // disconnectが見えたらソケットを閉じる
//						isEnd = true;
					}
				}

				// コメントのプロパティをRecievクラスのフィールドにセット
				formatter.commentAttrReceived(attributesMap);
				newRecord = formatter
						.getReceivedComment(getInnerText());//コメントだけはここで突っ込む
				formatter.clear();
				if (newRecord == null)
					return;

				//投稿時間計算
				String hour = "";
				int startTime = Integer.parseInt(liveinfo.getStartTime());
				long passedminute = (Long.parseLong(newRecord[3]) - startTime) / 60;
				String minute = String.format("%02d", passedminute);
				String second = String.format("%02d",
						(Long.parseLong(newRecord[3]) - startTime) % 60);
				if (passedminute > 59) {
					hour = String.format("%d:",
							(Long.parseLong(newRecord[3]) - startTime) / 3600);
					minute = String
							.format("%02d",
									((Long.parseLong(newRecord[3]) - startTime) % 3600) / 60);
				}
				newRecord[3] = hour + minute + ":" + second;
//				Log.d("NEWRECORD","buf size" + buffer.size() + " " + postable.isScrollEnd() + "  updating"+isUpdating + "  last_time"+lastGetTime);
				// テーブルに行追加
				//時間を比べる
				if(isUpdating){
					//最後の行が、見つかった所で通常に戻す
					updateCount++;
					if(updateCount>20){//遡って該当コメ無かったら最新のコメから
//						Log.d("Log","NOT FIND ----" + newRecord[5] + " " + newRecord[6]);
						buffer.clear();//初期ループで溜まっているかもしれない(一応)
						updateCount = 0;
						if(!isOfficial)lastCommentCount = Integer.parseInt(newRecord[5])+1;
						isUpdating = false;
						isBuffering = false;
						updateCommentTable(null,null,null,0);//NG間違い防止番号は最初に設定してるはず
					}else if(newRecord[1].equals(temp_id)&&newRecord[6].equals(temp_comment)){
//						Log.d("Log","FIND COMMENT ---- " +newRecord[1] + " "+ newRecord[6] + " ");
						updateCount = 0;
						isUpdating = false;
						isBuffering = false;
						buffer.clear();//初期ループのレコードか溜まってしまっていたのがあるかもしれない(一応)
					}
				}else{
				long nowTime = System.currentTimeMillis();
				if(nowTime-lastGetTime<1000){//13回以上連続で感覚1秒未満なら1度バッファモードにする
					lastGetTime = nowTime;
					busy++;
					if(busy > 13){
						isBuffering = true;
//						Log.d("NLiveCC",">13 DisPlayBufferMark");
						new DisplayBufferMark().execute();
						buffer.add(newRecord);
						busy = 0;
						attributesMap.clear();
						return;
					}
				}
				//時間13回未満でスクロールが末尾の場合全て処理
//				Log.d("NLiveCC","isScrollEnd " + postable.isScrollEnd());
					if (postable.isScrollEnd()) {
						//AddRowTaskはこことスクロールを末尾に戻した時だけにしないと複雑になりすぎる
						//バッファと普通のが不整合する可能性があるので気をつける
						lastGetTime = nowTime;
						isBuffering = true;
						buffer.add(newRecord);
						flushBuffer();
					}else{
						isBuffering = true;
						buffer.add(newRecord);//スクロール末尾じゃない
					}
				}
			}

			attributesMap.clear();

		}// End of end

	}// End of commentParser



	/**
	 * API Level 8未満の端末用のファーストパーサ
	 * コメントをStringの配列にする
	 *
	 * @param source
	 * @return
	 */
	public void LowLevelFirstParser(String source) {
		final XmlPullParser xml = Xml.newPullParser();
		Map<String, String> attributesMap = new HashMap<String, String>();//実際入ってくる属性値をマップした値に変換するワーク変数
		ReceiveHandlingFormatter formatter = new ReceiveHandlingFormatter();//マップ的割り当て
		try {
			xml.setInput(new StringReader(source));
			int eventType = 0;
			while ((eventType = xml.next()) != XmlPullParser.END_DOCUMENT) {
				if (eventType == XmlPullParser.START_TAG
						&& "chat".equals(xml.getName())) {

					int len = xml.getAttributeCount();
					for (int i = 0; i < len; i++) {// パケットの属性値をキーに全て突っ込む
						attributesMap.put(xml.getAttributeName(i),
								xml.getAttributeValue(i));
					}

					String comment = getItemString(xml, "chat");
					String type = attributesMap.get(CommentRecieveFormat.USER_TYPE);
					if (type != null && (type.equals("3") || type.equals("2"))) {
						if (comment.equals("/disconnect")) { // disconnectが見えたら終了
//							isEnd = true;
						}
					}

					formatter.commentAttrReceived(attributesMap);
					String[] newRecord =  formatter.getReceivedComment(comment);//コメントだけは別でこの時に取得
					//投稿時間計算
						String hour = "";
						int startTime = Integer.parseInt(liveinfo.getStartTime());
						long passedminute = (Long.parseLong(newRecord[3]) - startTime) / 60;
						String minute = String.format("%02d", passedminute);
						String second = String.format("%02d",
								(Long.parseLong(newRecord[3]) - startTime) % 60);
						if (passedminute > 59) {
							hour = String.format("%d:",
									(Long.parseLong(newRecord[3]) - startTime) / 3600);
							minute = String
									.format("%02d",
											((Long.parseLong(newRecord[3]) - startTime) % 3600) / 60);
						}
						newRecord[3] = hour + minute + ":" + second;
						try {
							if (newRecord[5].equals(URLEnum.HYPHEN)||newRecord[5].equals("")) {// 公式はコメ番がない
								isOfficial = true;
							}else{
								lastCommentCount = Integer.parseInt(newRecord[5]);
							}
						} catch (NumberFormatException e) {
								Log.d("NLiveRoid", "Failed Integer.parseInt　" + newRecord[5]);
							e.printStackTrace();
							isEnd = true;
							closeMainConnection();
							closeLogConnection();
						}
						// テーブルに行追加
					lastGetTime = System.currentTimeMillis();
					//AddRowTaskはここだけにしないと複雑になりすぎる
					//バッファと普通のが不整合する可能性があるので気をつける
					buffer.add(newRecord);
					flushBuffer();//2コメ目以降の方が早くなる可能性はあるけど、とにかく追加
				}
			}
		} catch (XmlPullParserException e) {
			Log.d("NLiveRoid","ParseException at LowLevelFirstParser");
//			e.printStackTrace();
		}catch (IOException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		} catch (ParseException e){
			e.printStackTrace();
		}
	}


	/**
	 * API Level 8未満の端末用のパーサ
	 * コメントをStringの配列にする
	 *
	 * @param source
	 * @return
	 */
	public void lowLevelParser(String source) {
		final XmlPullParser xml = Xml.newPullParser();
		Map<String, String> attributesMap = new HashMap<String, String>();//実際入ってくる属性値をマップした値に変換するワーク変数
		ReceiveHandlingFormatter formatter = new ReceiveHandlingFormatter();//マップ的割り当て
		try {
			xml.setInput(new StringReader(source));
			int eventType = 0;
			while ((eventType = xml.next()) != XmlPullParser.END_DOCUMENT) {
				if (eventType == XmlPullParser.START_TAG
						&& "chat".equals(xml.getName())) {

					int len = xml.getAttributeCount();
					for (int i = 0; i < len; i++) {// パケットの属性値をキーに全て突っ込む
						attributesMap.put(xml.getAttributeName(i),
								xml.getAttributeValue(i));
					}

					String comment = getItemString(xml, "chat");
					String type = attributesMap.get(CommentRecieveFormat.USER_TYPE);
					if (type != null && (type.equals("3") || type.equals("2"))) {
						if (comment.equals("/disconnect")) { // disconnectが見えたら終了
//							isEnd = true;
						}
					}

					formatter.commentAttrReceived(attributesMap);
					String[] newRecord =  formatter.getReceivedComment(comment);//コメントだけは別でこの時に取得
					if(newRecord[0] == null)newRecord[0] = "";
					//投稿時間計算
						String hour = "";
						int startTime = Integer.parseInt(liveinfo.getStartTime());
						long passedminute = (Long.parseLong(newRecord[3]) - startTime) / 60;
						String minute = String.format("%02d", passedminute);
						String second = String.format("%02d",
								(Long.parseLong(newRecord[3]) - startTime) % 60);
						if (passedminute > 59) {
							hour = String.format("%d:",
									(Long.parseLong(newRecord[3]) - startTime) / 3600);
							minute = String
									.format("%02d",
											((Long.parseLong(newRecord[3]) - startTime) % 3600) / 60);
						}
						newRecord[3] = hour + minute + ":" + second;

						// テーブルに行追加

//						Log.d("Log","NEW RECORD " + newRecord[0] + " " + newRecord[1] + " " + newRecord[2] + " " + newRecord[3] + " " + newRecord[4]  +" " + newRecord[5]);
						//時間を比べる
						if(isUpdating){
							//最後の行が、見つかった所で通常に戻す
							updateCount++;
							if(updateCount>20){//遡って該当コメ無かったら最新のコメから
//								Log.d("Log","NOT FIND ----" + newRecord[5] + " " + newRecord[6]);
								buffer.clear();//初期ループで溜まっているかもしれない(一応)
								updateCount = 0;
								if(!isOfficial)lastCommentCount = Integer.parseInt(newRecord[5])+1;
								isUpdating = false;
								isBuffering = false;
								updateCommentTable(null,null,null,1);//NG間違い防止番号は最初に設定してるはず
							}else if(newRecord[1].equals(temp_id)&&newRecord[6].equals(temp_comment)){
//								Log.d("Log","FIND COMMENT ---- " +newRecord[1] + " "+ newRecord[6] + " ");
								updateCount = 0;
								isUpdating = false;
								isBuffering = false;
								buffer.clear();//初期ループのレコードか溜まってしまっていたのがあるかもしれない(一応)
							}
						}else{
						long nowTime = System.currentTimeMillis();
						if(nowTime-lastGetTime<1000){//13回以上連続で感覚1秒未満なら1度バッファモードにする
							lastGetTime = nowTime;
							busy++;
							if(busy > 13){
								isBuffering = true;
								new DisplayBufferMark().execute();
								buffer.add(newRecord);
								busy = 0;
								attributesMap.clear();
								return;
							}
						}
						//時間13回未満でスクロールが末尾の場合全て処理
							if (postable.isScrollEnd()) {
								//AddRowTaskはここだけにしないと複雑になりすぎる
								//バッファと普通のが不整合する可能性があるので気をつける
								lastGetTime = nowTime;
								isBuffering = true;
								buffer.add(newRecord);
								flushBuffer();
							}else{
								isBuffering = true;
								buffer.add(newRecord);//スクロール末尾じゃない
							}

						}
				}
			}
		} catch (XmlPullParserException e) {
			Log.d("NLiveRoid","ParseException at lowLevelParser");
//			e.printStackTrace();
		}catch (IOException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		} catch (ParseException e){
			e.printStackTrace();
		}
	}

	private String getItemString(XmlPullParser xml, String tagname)
				throws XmlPullParserException, IOException {
			String[] result = new String[]{"","","","","",""};//とりあえず順序どおりに入れていく(列の入れ替え機能想定なし)
			int eventType = xml.next();
			while (eventType != xml.END_DOCUMENT) {
				// itemのエンドタグを見つけたらループ終了
				if (eventType == XmlPullParser.END_TAG
						&& tagname.equals(xml.getName())) {
					if(result[0] == null){//premium属性が無かったら一般

					}
					break;//returnしているので実行されない <?xmlとか無いので、念のため
				}
				if (eventType == XmlPullParser.TEXT) {
					return xml.getText();
				}
				eventType = xml.next();
			}
			return "";
		}


	/**
	 * 更新処理
	 */

	public void updateCommentTable(String last_ID,String last_Comment,String num,int isLowLevelAPI){

		Log.d("NLiveRoid","updateCT " + last_Comment);
		System.gc();//長時間観てどんどんメモリを圧迫しないように
		manualSort();
		if(num != null && !num.equals("")){
		escapeNG.add(num);
		Log.d("NLiveRoid","ADD NUM " + num);
		}
		if(buffer.size() > 0){//バッファがあれば紛らわしいので消しちゃう
			buffer.clear();
		}
		updateCount = 0;
		int UPDATE_VALUE = CODE.SIMPLE_UPDATE;
		isBuffering = true;
		if(last_ID != null && last_Comment != null){//目指すコメントがあれば
			isUpdating = true;
			UPDATE_VALUE = CODE.GET_BETWEEN_UPDATE;
		}
//		Log.d("NLiveCC", "updateCommentTable");
		this.closeMainConnection();
		this.closeLogConnection();
		isEnd =false;//必ず
		temp_id = last_ID;
		temp_comment = last_Comment;

		switch(isLowLevelAPI){
		case 0:
			mainThread = new GetCommentLoopTask();
			mainThread.execute(UPDATE_VALUE);
			break;
		case 1:
			mainThread = new lowLevelGetCommentRoopTask();
			mainThread.execute(UPDATE_VALUE);
			break;
		}


	}

	/**
	 * ユーザー名自動取得
	 */
	public void setAutoUser(boolean isAutoUser){
		this.isAutoUser = isAutoUser;
	}

	public void cancellAutoUserName(){
		if(autoUserNameTask != null && autoUserNameTask.getStatus() != AsyncTask.Status.FINISHED){
			autoUserNameTask.cancel(false);
		}
	}
	public void cancellReadTask(){
//		if(addSpeechTask != null && addSpeechTask.getStatus() != AsyncTask.Status.FINISHED){
//			addSpeechTask.cancel(true);
//		}
	}

	class GetUserNameManager extends AsyncTask<Void, Void, Void>{
		private HashSet<String> taskQ = new HashSet<String>();
		private HashMap<String,Integer> failed = new HashMap<String,Integer>();
		private boolean ENDFLAG = true;
		private   GetUserName childTask;
		private   StoreHandleName storeTask;
		private boolean isFinishGet;//常に0ならかまさないようにフラグ2つ
		private boolean isAdded;
		@Override
		public void onCancelled() {
			if(childTask != null)childTask.cancel(false);
			ENDFLAG = false;
			super.onCancelled();
		}
		public void putQ(String ID){
			if(taskQ.size() > 30){
				taskQ.clear();
				return;
			}
			if(ID.equals("900000000")||(failed.get(ID)!=null&&failed.get(ID)>3)){
				return;
			}
			isAdded = true;
				taskQ.add(ID);
		}
		@Override
		protected Void doInBackground(Void... params) {
			while (ENDFLAG) {
				if(taskQ.size()>0&&((childTask != null && childTask.getStatus() ==AsyncTask.Status.FINISHED)||childTask == null)){
					isFinishGet = false;
					childTask = new GetUserName();
					childTask.execute();
				}
				if(taskQ.size() == 0 &&isFinishGet){
					if(storeTask==null||(storeTask != null&&storeTask.getStatus() == AsyncTask.Status.FINISHED)){
						isFinishGet = false;
					storeTask = new StoreHandleName();
					storeTask.execute();
					}
				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
//					e.printStackTrace();
					Log.d("NLiveRoid","InterruptedException at GetUserNameManager");
					ENDFLAG = false;
					break;
				}catch(IllegalStateException e){
					Log.d("NLiveRoid","IliegalState at GetUserNameManager");
					ENDFLAG = false;
					break;
				}catch(Exception e){
					e.printStackTrace();
					ENDFLAG = false;
					break;
				}
			}
			return null;
		}



		class GetUserName extends AsyncTask<Void,Void,Void>{

			@Override
			protected Void doInBackground(Void... params) {
				String id = taskQ.iterator().next();
				if(id == null){
					return null;
				}
				try{

				HttpURLConnection con = (HttpURLConnection) new URL(
						URLEnum.USERPAGE + id).openConnection();
				con.setRequestProperty("Cookie",
						session);

				InputStream is = con.getInputStream();
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				int size = 0;
				byte[] byteArray = new byte[1024];
				while ((size = is.read(byteArray)) != -1) {
					bos.write(byteArray, 0, size);
				}
				byteArray = bos.toByteArray();
				String result = "";
				Matcher mc = Pattern.compile("<h2><strong>.+</strong>さん</h2>")
						.matcher(new String(byteArray, "UTF-8"));
				if (mc.find()) {
					result = mc.group();
					result = result.substring(12, result.length() - 16);
				}else{
					Matcher mc1 = Pattern.compile("<h2>.+<small>さん</small></h2>")
							.matcher(new String(byteArray, "UTF-8"));
					if (mc1.find()) {
						result = mc1.group();
						result = result.substring(4, result.length() - 22);
					}
				}
				if(result == null|| result.equals("")){
					int failedCount = failed.get(id)==null? 0:failed.get(id);
					failed.put(id, ++failedCount);
				}
				if(result!=null&&!result.equals("")){
				postable.setAutoHandleName(id,result);
				}else{
					int failedCount = failed.get(id)==null? 0:failed.get(id);
					failed.put(id, ++failedCount);
				}
				taskQ.remove(id);
				if(taskQ.size() == 0&&isAdded){
					isFinishGet = true;
					isAdded = false;
				}
			}catch(FileNotFoundException e){
//				e.printStackTrace();
				//900000000とかのIDが運営等の場合
				int failedCount = failed.get(id)==null? 0:failed.get(id);
				failed.put(id, ++failedCount);
			} catch (Exception e) {
				e.printStackTrace();
				int failedCount = failed.get(id)==null? 0:failed.get(id);
				failed.put(id, ++failedCount);
			}
				return null;
			}

		}
		class StoreHandleName extends AsyncTask<Void,Void,Void>{
			@Override
			protected Void doInBackground(Void... params) {
//				Log.d("Log","STORE DATA --------- ");
				postable.writeHandleName();
				return null;
			}
		}
	}



	/**
	 * 投稿処理
	 *
	 * リスナーコメントをポスト
	 * @param comment
	 *            コメント
	 * @param cmd
	 *            コマンドセット
	 * @see CommandMapping.viewer.CommandSet
	 */
	public void postComment(String comment, CommandMapping cmd) {
		if(NLiveRoid.isDebugMode)Log.d("NLiveRoid","postComment C" + session);
		if(cmd.isOwner()){
			postOwnerComment(comment, cmd);
		}else if(cmd.isBSPEnable()){
//			Log.d("Log","BSP POST TASK ---- "+cmd.getBSPName() + " " + cmd.getBSPColor() + " " + cmd.getBSPToken());
			new PostBSPCommentTask(comment,cmd).execute();
		}else{
			new PostCommentTask(comment,cmd).execute();
		}
	}


	class PostCommentTask extends AsyncTask <String,Void,Integer>{
		private String comment;
		private CommandMapping cmd;
		protected PostCommentTask(String comment,CommandMapping cmd){
			this.comment = comment;
			this.cmd = cmd;
		}
		@Override
		protected Integer doInBackground(String... arg0) {

		String res = getAttrValue("last_res");
		int blockNo = 0;
		if(res != null){//0コメなのでnullだからといってリターンとかしちゃ駄目(0コメの場合、blockNo=0)
		blockNo = Integer.parseInt(res) / 100;
		}
		String ticket = getAttrValue("ticket");

		if ( ticket == null){
			return -33;
		}

		Matcher mc = Pattern.compile("_[0-9]+_").matcher(
				session);
		String userid = "";
		if (mc.find()) {
			userid = mc.group().substring(1, mc.group().length() - 1);
		}
		InputStream key_ = null;
		try{
		key_ = Request
				.doGetToInputStreamFromFixedSession(session,
						String.format(URLEnum.GETPOSTKEYXML
								,
								mThread[nowSeetIndex], blockNo), error);
		}catch(ArrayIndexOutOfBoundsException e){
			return -34;
		}
		String param = "";
		String postkey = "";

		if (key_ == null) {
			return -35;
		}

		try{
			param = (new BufferedReader(new InputStreamReader(key_,
					"UTF-8"))).readLine();
			postkey = param.substring(param.indexOf('=') + 1);

			// コメントのフィルタリング
			// SecMSec format. (e.g., 11122)
			int vpos = (int) (System.currentTimeMillis() / 1000 - Long
					.parseLong(liveinfo.getBaseTime()))
					* 100
					+ Calendar.getInstance().get(Calendar.MILLISECOND) / 10;
			comment = comment.replace("&", "&amp;").replace("<", "&lt;")
					.replace(">", "&gt;");


			final String postXML = String.format(URLEnum.POSTXML, mThread[nowSeetIndex], ticket,
					vpos, postkey, cmd.toString(), userid,
					liveinfo.getIsPremium(), comment);

			postOut.write(postXML.getBytes("UTF-8"));


			return 0;
		} catch (IOException e) {
			return -36;
		} catch (Exception e) {
			// 抹茶ーとか
			e.printStackTrace();
			return -36;
		}finally{
			try {
				if(postSocket!= null){
					postSocket.close();
					postSocket = null;
				}
				if(postOut != null){
					postOut.close();
					postOut = null;
				}
				if(postIn != null){
					postIn.close();
					postIn = null;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	} // End of postComment() function.
		protected void onPostExecute(Integer arg){
			if(arg != 0 && error != null){
				error.setErrorCode(arg);
				error.showErrorToast();
			}
		}
	}

	/**
	 * BSPコメントをポスト
	 *
	 */


	class PostBSPCommentTask extends AsyncTask <String,Void,Integer>{
		private String comment;
		private CommandMapping cmd;
		protected PostBSPCommentTask(String comment,CommandMapping cmd){
			this.comment = comment;
			this.cmd = cmd;
		}
		@Override
		protected Integer doInBackground(String... arg0) {
			//publishStatusで得たtokenが必要
			String token = cmd.getBSPToken();
//			Log.d("Log","POST TOKEN " + token);
		if ( token == null||token.equals("")){
			return -1;
		}
//		String body = "";
//		if(comment != null){
//			String[] commandStr = comment.split(" ");
//			if(commandStr[0].matches("^/")){
//				body = commandStr[0];
//			}
//		}
		//BSPコメポストコネクション
		try {
			//?v=%s&body=%s&color=%s&name=%s&mode=\"json\"&token=%s";
			String url = String.format(URLEnum.BSP_POST +liveinfo.getLiveID());
			HttpURLConnection con = (HttpURLConnection)new URL(url).openConnection();
			con.setRequestProperty("Content-Type",
					"application/x-www-form-urlencoded");
			con.setRequestMethod("POST");
			con.setDoOutput(true);
			con.setRequestProperty("Cookie", session);
			String postData = String.format(
					"body=%s&color=%s&name=%s&mode=\"json\"&token=%s",
					URLEncoder.encode(comment, "UTF-8"),cmd.getBSPColor(),URLEncoder.encode(cmd.getBSPName(),"UTF-8"),cmd.getBSPToken());
			con.setRequestProperty("Content-Length",
					Integer.toString(postData.length()));
			PrintStream out = new PrintStream(con.getOutputStream());
			out.print(postData);

			BufferedReader in = new BufferedReader(new InputStreamReader(
					con.getInputStream(), "UTF-8"));
			String temp = "";
			String returnValue = "";
			while((temp = in.readLine()) != null){
				returnValue += temp;
			}
//			Log.d("Log","RETURN VALUE --------- " + returnValue);
			if (returnValue.contains("error")) {
				return -2;
			} else {
				return 0;
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return -3;
		} catch (IOException e) {
			e.printStackTrace();
			return -4;
		}

	} // End of postComment() function.
		protected void onPostExecute(Integer arg){
			switch(arg){
			case -1:
				MyToast.customToastShow((Context)postable, "トークン取得に失敗しています");
				break;
			case -2:
				MyToast.customToastShow((Context)postable, "サーバ側でエラーしました");
				break;
			case -3:
				MyToast.customToastShow((Context)postable, "URLエラー\n仕様変更された可能性があります");
				break;
			case -4:
				MyToast.customToastShow((Context)postable, "IOエラーが発生しました");
				break;
			}
		}

	}

	/**
	 * 投稿処理
	 *
	 * 放送主コメントをポスト
	 * @param comment
	 *            コメント
	 * @param cmd
	 *            コマンドセット	 *
	 * @see CommandMapping.viewer.CommandSet
	 * /api/broadcast/%s?is184=%s&mail=%s&body=%s&token=%s"
	 */
	public void postOwnerComment(String comment, CommandMapping cmd) {
		new PostOwnerCommentTask(comment,cmd).execute();
	}

	class PostOwnerCommentTask extends AsyncTask <String,Void,Boolean>{
		private String comment;
		private CommandMapping cmd;
		protected PostOwnerCommentTask(String comment,CommandMapping cmd){
			this.comment = comment;
			this.cmd = cmd;
		}
		@Override
		protected Boolean doInBackground(String... arg0) {
			//publishStatusで得たtokenが必要
			String token = liveinfo.getToken();
		if ( token == null){
			error.setErrorCode(-15);
			return false;
		}
//		String body = "";
//		if(comment != null){
//			String[] commandStr = comment.split(" ");
//			if(commandStr[0].matches("^/")){
//				body = commandStr[0];
//			}
//		}
		Matcher mc = Pattern.compile("_[0-9]+_").matcher(
				session);
		String userid = "";
		if (mc.find()) {
			userid = mc.group().substring(1, mc.group().length() - 1);
		}
		//主コメポストコネクション
		try {
			HttpURLConnection con = (HttpURLConnection)new URL(String.format(URLEnum.OWNPOSTURL ,liveinfo.getLiveID())).openConnection();
			con.setRequestProperty("Content-Type",
					"application/x-www-form-urlencoded");
			con.setRequestMethod("POST");
			con.setDoOutput(true);
			con.setRequestProperty("Cookie", session);
			String postData = String.format(
					"is184=%s&mail=%s&body=%s&token=%s", cmd.getValue(CommandKey.CMD),cmd.toString(),
					URLEncoder.encode(comment, "UTF-8"), token);

			con.setRequestProperty("Content-Length",
					Integer.toString(postData.length()));
			PrintStream out = new PrintStream(con.getOutputStream());
			out.print(postData);

			BufferedReader in = new BufferedReader(new InputStreamReader(
					con.getInputStream(), "UTF-8"));
			String temp = "";
			String returnValue = "";
			while((temp = in.readLine()) != null){
				returnValue += temp;
			}
			if (returnValue.contains("error")) {
				return false;
			} else {
				return true;
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

				return true;
	} // End of postComment() function.
		protected void onPostExecute(Boolean arg){
			if(error != null && error.getErrorCode() != 0){
				error.showErrorToast();
			}
		}
	}

	/**
	 * Socketから得るデータを文字列に変換
	 * @param in
	 * @return
	 * @throws IOException
	 * @throws OutOfMemoryError
	 */
	private String getUTFString(InputStream in) throws IOException,OutOfMemoryError{
		ByteArrayOutputStream bo = new ByteArrayOutputStream(256);
		int c;
	try{
			while ((c = in.read()) != 0) {
				bo.write(c);
			}
	} catch (SocketException e) {
			Log.d("NLiveRoid","getUTFString Socket was closed");
//		e.printStackTrace();
	} catch (OutOfMemoryError el) {
		// Out of Memmory?
		Log.d("NLiveRoid","getUTFString Memory over flow");
		el.printStackTrace();
		closeMainConnection();
		closeLogConnection();
	}catch(NullPointerException e){
		Log.d("NLiveRoid","getUTFString Socket was Null");
		e.printStackTrace();
		closeLogConnection();
	}
		return bo.toString("UTF-8");
	}

	// 新たに接続を持って最後のコメ番から属性を取得
	private String getAttrValue(final String name) {

		class SAXHandler extends MyDefaultHandler {
			private String value;

			public String getValue() {
				return value;
			}

			@Override
			public void start(String uri, String localName, String qName,
					Attributes attributes) {
				value = attributes.getValue(name);
			}
		}

		SAXHandler saxHandler = new SAXHandler();

		try {
			postSocket = new Socket(addr, mPort[nowSeetIndex]);

			postOut = new DataOutputStream(postSocket.getOutputStream());
			postOut.writeBytes(String.format(URLEnum.GETCOMMENTXML, mThread[nowSeetIndex],
					0));

			postIn = postSocket.getInputStream();

			SAXParser sax = SAXParserFactory.newInstance().newSAXParser();
			String s = getSingleString(postIn);
			InputSource source = new InputSource(new StringReader(s));

			sax.parse(source, saxHandler);
		} catch (UnknownHostException e) {
		} catch (ParserConfigurationException e) {
		} catch (SAXException e) {
		} catch (IOException e) {
		}


		return saxHandler.getValue();
	} // End of getAttrValue() function.

	/**
	 * ヘルパーメソッド
	 *
	 * @param in
	 * @return
	 * @throws IOException
	 */
	private String getSingleString(InputStream in) throws IOException {

		ByteArrayOutputStream bo = new ByteArrayOutputStream(256);
		int c;

		try {
			while ((c = in.read()) != 0x00) {
				bo.write(c);
			}
		} catch (SocketException e) {
				Log.d("NLiveRoid","getSingleString Socket was closed");
//				e.printStackTrace();
		} catch (OutOfMemoryError el) {
			Log.d("NLiveRoid","getSingleString Memory over flow");
//				el.printStackTrace();
			closeMainConnection();
			closeLogConnection();
		}

		return bo.toString("UTF-8");
	}


	class DisplayBufferMark extends AsyncTask<Void,Void,Void>{
		@Override
		protected Void doInBackground(Void... arg0) {
			return null;
		}
		@Override
		protected void onPostExecute(Void arg){
			if(postable != null){
			postable.getBufferMark().setVisibility(View.VISIBLE);
			}
		}
	}
	class DeleteBufferMark extends AsyncTask<Void,Void,Void>{
		@Override
		protected Void doInBackground(Void... arg0) {
			return null;
		}
		@Override
		protected void onPostExecute(Void arg){
			if(postable != null){
			postable.getBufferMark().setVisibility(View.INVISIBLE);
			}
		}
	}
	interface CommentRecieveFormat {

		// public interface Member {// 会員か運営情報
		//
		// /** プミアム */
		// public static final String PREMIUM = "1";
		//
		// /** 運営コマンド */
		// public static final String SYSTEM = "2";
		//
		// /** 放送主、または運営 */
		// public static final String OWNER = "3";
		//
		// /** 公式の運営 */
		// public static final String OFFICIAL = "6";
		//
		// /** BSP */
		// public static final String BSP = "7";
		//
		// /** 一般会員 */
		// public static final String NORMAL = "";
		//
		// /** 男性の一般 */
		// public static final String NORMAL_MALE = "8";
		//
		// /** 男性のプレミアム */
		// public static final String PREMIUM_MALE = "9";
		//
		// /** 女性の一般 */
		// public static final String NORMAL_FEMALE = "24";
		//
		// /** 女性のプレミアム */
		// public static final String PREMIUM_FEMALE = "25";
		// }

		class UserTypeMap extends HashMap<String, String> {
			UserTypeMap() {
				put("1", "P");
				put("2", "SYSTEM");
				put("3", "主");
				put("6", "OFFICIAL1");
				put("7", "BSP");
				put("", "");
				put("8", "iPhone/iPod");
				put("9", "携帯");
				put("24", "");
				put("25", "");
			}
		}

		// マップのキー値の仕様
		/** 184 */
		public static final String ANONYMITY = "anonymity";

		/** 日付 */
		public static final String DATE = "date";

		/** コメントコマンド */
		public static final String COMMAND = "mail";

		/** コメント番号 */
		public static final String NUMBER = "no";

		/**
		 * 会員タイプの仕様
		 *
		 * @see CommentRecieveFormat.Member
		 */
		public static final String USER_TYPE = "premium";

		/** ユーザーID */
		public static final String USER_ID = "user_id";

		/** ビデオ位置 */
		public static final String VPOS = "vpos";

		/** セルフコメント */
		public static final String YOURPOST = "yourpost";

		/**
		 * 公式の運営コメントのみ
		 */
		public static final String NAME = "name";

		public static final String SCORE = "score";

		/**
		 * コメントの属性を受信したときにハンドルされる このメソッドはコメントの属性を受信する度に呼ばれます。
		 * 属性がない場合もある。これは会員タイプやユーザー ID、コメントコマンドも例外でない(要nullチェック)
		 *
		 * @param map
		 *            属性の名前をキーにしたマップ
		 * @see java.util.Map
		 */
		void commentAttrReceived(Map<String, String> map);

		// void commentOutConsole(String comment);
		String[] getReceivedComment(String comment);
		// Map<String,String> getReceivedComment(Map<String,String> comment);

	}// End of CommentRecieveFormat

	class MyDefaultHandler extends DefaultHandler {
		private static final int BUFSIZE = 256;

		private StringBuilder sbuf = new StringBuilder(BUFSIZE);

		public String getInnerText() {
			return sbuf.toString();
		}

		public void start(String uri, String localName, String qName,
				Attributes attributes) throws SAXException {
			// please override.
		}

		@Override
		public final void startElement(String uri, String localName,
				String qName, Attributes attributes) throws SAXException {
			start(uri, localName, qName, attributes);
		}

		public void end(String uri, String localName, String qName)
				throws SAXException {
			// please override.
		}

		@Override
		public final void endElement(String uri, String localName, String qName)
				throws SAXException {
			end(uri, localName, qName);
			sbuf = sbuf.delete(0, sbuf.length());
		}

		@Override
		public final void characters(char[] ch, int start, int length) {
			sbuf.append(ch, start, length);
		}

		public void clear(){
			sbuf.delete(0, sbuf.length());
		}

	} // End of NicoLiveSAXHandler class.




	public boolean getIsBuffering() {
		return isBuffering;
	}


			/**
			 * バッファーにあるデータを全てリストに追加する
			 * @author Owner
			 *
			 */

	private boolean bufferFilterComment(int index){
//		Log.d("ComTable","filter");
		boolean result = false;
		//コメントのフィルタを行う
		if(((HandleNamable)postable).isAt()){//＠でコテハン付け
			Matcher mc = atPt.matcher(buffer.get(index)[6]);
			if(mc.find()){//コメントに@がある
				String[] split = buffer.get(index)[6].split("@|＠");//コメントを＠でスプリットした右側が有効
				if(split != null && split.length >= 1){
				result = true;
				}
			}
		}
		//読み上げ有効な場合は読み上げる
		if(speech_KindValue == 1||speech_KindValue == 3){
			String comment = buffer.get(index)[6];
			try{
				if(speech_education){
					if(comment.matches("教育(.+=.*)")){

					}
					if(comment.matches("忘却(.+)")){

					}
				}
				Iterator<Pattern> it = educationMap.keySet().iterator();
				Matcher mc = null;
				String mcStr = "";
				Pattern key = null;
				String value = "";
				while(it.hasNext()){
					key = it.next();//パターンを１つ取得
					mc = key.matcher(comment);
					while(mc.find()){
						mcStr = mc.group();
						value = educationMap.get(key);//変換する語句を取得
						comment = comment.replaceAll(mcStr,value);
						mc = key.matcher(comment);
						}
					}
			if(mSpeech != null){//Aquesの場合、非同期にならない
//				if(addSpeechTask == null||(addSpeechTask != null && addSpeechTask.getStatus() != AsyncTask.Status.RUNNING)){
//					addSpeechTask = new AddSpeech();
//				addSpeechTask.execute();
//				}
//				addSpeechTask.push(comment);
				try {
//					Log.d("NLiveRoid","ADD  at CommentTable");
					mSpeech.addSpeech(comment);
				} catch (InterruptedException e) {
					// TODO 自動生成された catch ブロック
					e.printStackTrace();
				}
			}
			}catch(IndexOutOfBoundsException e){
				Log.d("NLiveRoid","IndexOutOfBoundsException at bufferFilterComment index:"+index+" bufsize:"+buffer.size());
			}
		}
		return result;
	}

		//バッファをフィルタを通した後、全て追加する 同じコメントが何度も入ってくる可能性がある!!!!
		//非同期の意味あんまりないけど、別タスク ソートが入るのでそこで妥協
		private void firstFlushBuffer(){
			final HashMap<String,String> handlnameFilter = new HashMap<String,String>();
//			Log.d("NLiveCC","FirstFlushBufferCONST" + buffer.size());
				try{
			String[] temp = null;
					if(bufferFilterComment(0)){//読み上げ+コテハンかどうか
						temp = buffer.get(0);
						handlnameFilter.put(temp[1],temp[6].split("@|＠")[1]);//IDと、名前
						}
					if(isAutoUser){//ユーザー名自動
						temp = buffer.get(0);
						if(temp != null){
						Matcher mc = Pattern.compile("[^0-9]").matcher(temp[1]);//生ID
						if(!mc.find()){
						if(autoUserNameTask == null||(autoUserNameTask != null&&autoUserNameTask.getStatus() != AsyncTask.Status.RUNNING)){
							//スレッド走ってなきゃスタートする
							if(((HandleNamable)postable).isSetNameReady()){
							autoUserNameTask = new GetUserNameManager();
							autoUserNameTask.execute();
							}
						}
						if(!postable.isContainsUserID(temp[1])){
						autoUserNameTask.putQ(temp[1]);
						}
						}
					}
				}


			}catch(Exception e){
				e.printStackTrace();//だいたいリストにアドされない行が何故か出て、IndexOutofBounds おそらく連続でここが呼ばれてクリア後直ぐにたまったりしてなる
//				Log.d("NLiveCC","LostFlushBuffer");
			}
				((Activity)postable).runOnUiThread(new Runnable(){
					@Override
					public void run() {
						try{
							if(postable.isUplayout()){//途中で変わるかもしれないけど難しい
									lastCommentCount++;//一番下に書くとbreakする可能性がある
									adapter.insert(buffer.get(0),0);
									if(handlnameFilter != null && handlnameFilter.containsKey(buffer.get(0)[1])){
										postable.setAtHandleName(buffer.get(0)[1],handlnameFilter.get(buffer.get(0)[1]));
									}
							}else{
//									Log.d("Log","lastCommentCount " + lastCommentCount + " buffer " + buffer.get(i)[5]);
									lastCommentCount++;//一番下に書くとbreakする可能性がある
									adapter.addRow(buffer.get(0));
									if(handlnameFilter != null && handlnameFilter.containsKey(buffer.get(0)[1])){
										postable.setAtHandleName(buffer.get(0)[1],handlnameFilter.get(buffer.get(0)[1]));
									}
							}
							buffer.remove(0);
						}catch(Exception e){
							Log.d("NLiveRoid","Failed NG Judge");
							e.printStackTrace();
						}
//							Log.d("NLiveCC","endFirstAdd");
					}
				});
		}



		//同時進行するので、IndexOutOfBoundsはかなり発生するが、それでソケット閉じちゃうと、
		//観れたもんじゃなくなっちゃうのでそこでロジック終わるようにしておく
		private void flushBuffer(){
			final HashMap<String,String> handlnameFilter = new HashMap<String,String>();
// 			Log.d("NLiveCC","FlushBufferCalled" + buffer.size());
// 			ArrayList<String[]> copyList = new ArrayList<String[]>();
// 			for(int i= 0; i < buffer.size();i++){
// 				copyList.add(buffer.get(i));
// 			}
// 			int size = copyList.size();
// 			Log.d("NLiveCC","SIZE " + size);
 				try{
 					String[] temp = null;
				for(int i = 0; i < buffer.size(); i++){
					if(bufferFilterComment(i)){//読み上げ+コテハンかどうか
						temp = buffer.get(i);
						handlnameFilter.put(temp[1],temp[6].split("@|＠")[1]);//IDと、名前
						}
					if(isAutoUser){//ユーザー名自動
						temp = buffer.get(i);
						if(temp != null){
						Matcher mc = Pattern.compile("[^0-9]").matcher(temp[1]);//生ID
						if(!mc.find()){
						if(autoUserNameTask == null||(autoUserNameTask != null&&autoUserNameTask.getStatus() != AsyncTask.Status.RUNNING)){
							//スレッド走ってなきゃスタートする
							if(((HandleNamable)postable).isSetNameReady()){
							autoUserNameTask = new GetUserNameManager();
							autoUserNameTask.execute();
							}
						}
						if(!postable.isContainsUserID(temp[1])){
						autoUserNameTask.putQ(temp[1]);
						}
						}
						}
					}
				}


			}catch(IndexOutOfBoundsException e){
				e.printStackTrace();
				System.gc();
			}catch(Exception e){
				e.printStackTrace();//だいたいリストにアドされない行が何故か出て、IndexOutofBounds おそらく連続でここが呼ばれてクリア後直ぐにたまったりしてなる
//				Log.d("NLiveCC","LostFlushBuffer");
			}

			((Activity)postable).runOnUiThread(new Runnable(){
					@Override
					public void run() {
						if(buffer.isEmpty())return;
						try{
						if(postable.isUplayout()){//途中で変わるかもしれないけど難しい
							for(int i = 0; i < buffer.size();i++){
								if(!isOfficial&&lastCommentCount < Integer.parseInt(buffer.get(i)[5])){//NG
//									Log.d("NG---------",""+lastCommentCount);
									ngString[5] = String.valueOf(lastCommentCount);
									adapter.insert(ngString.clone(),0);
									lastCommentCount++;
									i--;//bufferのインデックスだからNGだったらiは変わらずにする
									continue;
								}
								lastCommentCount++;//一番下に書くとbreakする可能性がある
								adapter.insert(buffer.get(i),0);
								if(handlnameFilter != null && handlnameFilter.containsKey(buffer.get(i)[1])){
									postable.setAtHandleName(buffer.get(i)[1],handlnameFilter.get(buffer.get(i)[1]));
								}
								if(i == buffer.size()-1){
									buffer.clear();
									break;
								}
						}
						}else{
							for(int i = 0; i < buffer.size();i++){
//								Log.d("Log","lastCommentCount " + lastCommentCount + " buffer " + buffer.get(i)[5]);
								if(!isOfficial&&lastCommentCount < Integer.parseInt(buffer.get(i)[5])){//NG
									ngString[5] = String.valueOf(lastCommentCount);
									adapter.addRow(ngString.clone());
									lastCommentCount++;
									i--;//bufferのインデックスだからNGだったらiは変わらずにする
									continue;
								}
								lastCommentCount++;//一番下に書くとbreakする可能性がある
								adapter.addRow(buffer.get(i));
								if(handlnameFilter != null && handlnameFilter.containsKey(buffer.get(i)[1])){
									postable.setAtHandleName(buffer.get(i)[1],handlnameFilter.get(buffer.get(i)[1]));
								}
								if(i == buffer.size()-1){
									buffer.clear();
									break;
									}
								}
						}
						if(buffer.size() == 0){
							isBuffering = false;
							new DeleteBufferMark().execute();
						}
					}catch(Exception e){
						Log.d("NLiveRoid","Failed NG Judge");
						e.printStackTrace();
					}

					}

				});

//			Log.d("CommentTable","endFlushBuffer");
		}//End of FlushBuffer



//
//		class AddSpeech extends AsyncTask<Void,Void,Void>{
//			private boolean ENDFLAG = true;
//			@Override
//			public void onCancelled(){
//				ENDFLAG = false;
//				super.onCancelled();
//			}
//			protected AddSpeech(){
//				 readComments = new ArrayBlockingQueue<String>(30,true);
//			}
//			@Override
//			protected Void doInBackground(Void... params) {
//				while(ENDFLAG){
//					if(readComments.size() > 0){
//						try {
//							if(mSpeech != null)mSpeech.addSpeech(readComments.poll());
//						} catch (InterruptedException e) {
//							e.printStackTrace();
//							ENDFLAG = false;
//							break;
//						}catch(Exception e){
//							e.printStackTrace();
//							ENDFLAG = false;
//							break;
//						}
//					}
//				}
//				return null;
//			}
//			public void push(String comment){
//				readComments.add(comment);
//			}
//		}



		//手動ソート
			public void manualSort(){
			if(isOfficial){
//				new OfficialSort().execute();//逆になった時だけ
			}else{
				if(sortTask == null||sortTask.getStatus() == AsyncTask.Status.FINISHED){
				new SortTask().execute();
				}
			}
			}

			class OfficialSort extends AsyncTask<Void,Void,Void>{
				String[][] temp;
				@Override
				protected Void doInBackground(Void... params) {
					//単純に逆にする
					temp = new String[adapter.getCount()][];
					for(int i = 0; i < adapter.getCount(); i++){
					 temp[i] = adapter.getItem(i);
					}
					return null;
				}
				@Override
				protected void onPostExecute(Void arg){
					adapter.clear();
					for(int i = temp.length-1; i >= 0 ; i--){
					adapter.addRow(temp[i]);
					}
				}

			}
			class SortTask extends AsyncTask<Void,Void,Void>{
				@Override
				protected Void doInBackground(Void... arg0) {
//					for(int i = 0; i < adapter.getCount(); i++){
//						Log.d("NLR",adapter.getItem(i)[0] +" " + adapter.getItem(i)[1] + " " + adapter.getItem(i)[2] + " " + adapter.getItem(i)[3] + " " + adapter.getItem(i)[4] + " " + adapter.getItem(i)[5] );
//					}
//					Log.d("NLR","SortTask");
					return null;
				}
				//アダプターのソート
				@Override
				protected void onPostExecute(Void arg){
					try{
					new DisplayBufferMark().execute();
					if(adapter.getCount()>0){
						if(!isOfficial){//コメ番のない放送(公式等)は除外
							//間違ってNGしてたら削除
							for(int i = 0; i < adapter.getCount(); i++){
								ngString[5] = String.valueOf(i);
									adapter.remove(ngString.clone());//cloneじゃないと絶対駄目、番号が最新のNGコメントになっちゃって複数あると、そこからソートをミスる
							}
							//ソート
							if(postable.isUplayout()){//上方向レイアウト
								adapter.sort(new CommentComparator_UpLayout());//コメ番でソート
								//ソートされたので行がダブったりしていたら連続しているはずなので削除(まずダブるのが駄目だが)
								//なお2つ以上あっても繰り返すから全部消える
								String firstRow =  adapter.getItem(adapter.getCount()-1)[5];
								for(int i = adapter.getCount()-2 ; i >= 0;i--){//2つ続いてたら消す
									if(firstRow.equals(adapter.getItem(i)[5])){
										adapter.remove(adapter.getItem(i));//インデックスの若い方1件が消される様
										i++;
									}else{
										firstRow = adapter.getItem(i)[5];
									}
								}
//								Log.d("SORTTASK ", "firstRowResult:" + firstRow);
								//NG付加
//								Object[] eNG = escapeNG.toArray();
//								for(int i = 0; i < eNG.length;i++){
//								Log.d("escapeNG0 ", " " + eNG[i].toString());
//								}
								int commentNum = Integer.parseInt(adapter.getItem(adapter.getCount()-1)[5]);//最初のコメ番
								for(int i = adapter.getCount()-2; i >= 0; i--){
									if(escapeNG.contains(adapter.getItem(i)[5])){//更新してNGが関係なくなった箇所だったら、次の箇所まで進める
//										Log.d("NLR","DIACOVER UP  ESCAPE ----- " + adapter.getItem(i)[5]);
										if(i-1<=0){
											break;//終わりなら抜ける
										}
										commentNum = Integer.parseInt(adapter.getItem(i-1)[5]);//次の行のコメ番
											continue;
											}else{
												commentNum++;
											}
//									Log.d("HIKAKU ", "  " + commentNum + " " + adapter.getItem(i)[5]);
									if(commentNum <  Integer.parseInt(adapter.getItem(i)[5])){//今のコメ番より次のコメ番が大きかったらコメ番が飛んでいる
										ngString[5] = String.valueOf(commentNum);
										adapter.insert(ngString.clone(),i);
										commentNum++;//次に進める(これが無いと次から全てNGに判定される)
									}
								}
							}else{
							adapter.sort(new CommentComparator_DownLayout());//コメ番でソート
							//ソートされたので行がダブったりしていたら連続しているはずなので削除(まずダブるのが駄目だが)
							//なお2つ以上あっても繰り返すから全部消える
							String rowNum =  adapter.getItem(0)[5];
							for(int i = 1 ; i < adapter.getCount();i++){//1行しかなければ実行されないから大丈夫
								if(rowNum.equals(adapter.getItem(i)[5])){
									adapter.remove(adapter.getItem(i));//インデックスの若い方1件が消される様
									i--;
								}else{
									rowNum = adapter.getItem(i)[5];
								}
							}
							//NG付加
							int commentNum = Integer.parseInt(adapter.getItem(0)[5]);//最初のコメ番
							for(int i = 1; i < adapter.getCount(); i++){
											if(escapeNG.contains(adapter.getItem(i)[5])){//更新してNGが関係なくなった箇所だったら、次の箇所まで進める
//												Log.d("Log","DIACOVER ED  ESCAPE ----- " + adapter.getItem(i)[5]);
													if(i+1>=adapter.getCount()){
														break;//終わりなら抜ける
													}
													commentNum = Integer.parseInt(adapter.getItem(i+1)[5]);//次の行のコメ番
													continue;
											}else{
												commentNum++;
											}
								if(commentNum <  Integer.parseInt(adapter.getItem(i)[5])){//コメ番より次のコメ番が大きかったらコメ番が飛んでいる
									ngString[5] = String.valueOf(commentNum);
									adapter.insert(ngString.clone(),i);
									commentNum++;//次に進める(これが無いと次から全てNGに判定される)
									}
								}
							}
						}
						postable.toScrollEnd();
					}
						new DeleteBufferMark().execute();

					}catch(Exception e){
						e.printStackTrace();
					}
				}
		}


		/**
		 * これは外側のCommentTableのスコープ
		 *
		 */


			private Socket logSock;
			private InputStream logInStream;
			private DataOutputStream logOutStream;

			protected void getCommentLog(int amount,String firstTime,String firstNum){
				isBuffering = true;
				logList = new ArrayList<String[]>();
				logTask = new GetCommentLogTask(amount,firstTime,firstNum);
				logTask.execute();
			}


			/**
			 * コメントのログ追加クラス
			 * @author Owner
			 *
			 */
			class GetCommentLogTask extends AsyncTask<Void,Void,Void>{

				private int amount;
				private String firstTime;
				private String firstNum;
				GetCommentLogTask(int amount,String firstTime,String firstNum){
					this.amount = amount;
					this.firstTime = firstTime;
					this.firstNum = firstNum;
				}
				@Override
				protected Void doInBackground(Void... arg0) {

					if(session == null||session.equals("")||session.split("_").length < 4){
						if(error != null)error.setErrorCode(-21);
						return null;
					}
					try {
					URL url = new URL(URLEnum.GETWAYBACKKEY + mThread[nowSeetIndex]);
					HttpURLConnection con = (HttpURLConnection)url.openConnection();
					con.setDoOutput(true);
//					Log.d("Log","LOG---- " + mThread[nowSeetIndex] + " " + session);

					con.setRequestProperty("Cookie", session.trim());
					InputStream is = con.getInputStream();
					BufferedReader bf = new BufferedReader(new InputStreamReader(is));
					String temp = "";
					String waybackkey = "";
					while((temp = bf.readLine()) != null){
						if(temp.equals("")||temp.equals("waybackkey=")){
							error.setErrorCode(-21);
							return null;
						}
						waybackkey = temp.split("=")[1];
					}
					//whenの時間の計算
					String[] splited = firstTime.split(":");
					long firstCommentTime = 0;
					if(liveinfo.getBaseTime() == null || liveinfo.getBaseTime().equals("")){
						error.setErrorCode(-21);
						return null;
					}
					long baseTime = Long.parseLong(liveinfo.getBaseTime());

					if(splited.length==3){
						firstCommentTime = baseTime + Long.parseLong(liveinfo.getBaseTime()) + Long.parseLong(splited[0])*3600 + Integer.parseInt(splited[1])*60 + Integer.parseInt(splited[2]);
					}else if(splited.length == 2){
					firstCommentTime =  baseTime + Long.parseLong(splited[0])*60 + Long.parseLong(splited[1]);
					}
					//取得したもとコメ欄の最初のコメとの間にNGがあるかもしれないから余裕を持たせる
					firstCommentTime += 1000;
					//デフォルトの席で初期化
					logSock = new Socket(addr, mPort[nowSeetIndex]);

					// ソケットで空けた通信口を開いておく
					logSock.setKeepAlive(true);

					// DataOutputStreamは移植性のある出力ストリーム
					logOutStream = new DataOutputStream(logSock.getOutputStream());

					// コメント取得用パケットを投げる
					String GET_LOG_XML = String.format(URLEnum.GETCOMMENTLOGXML
							,mThread[nowSeetIndex],-amount,firstCommentTime,waybackkey,session.split("_")[3]);
					logOutStream.writeBytes(GET_LOG_XML);

					logInStream = logSock.getInputStream();// このインプットストリームをパーサに渡す
					} catch (UnknownHostException e1) {
						e1.printStackTrace();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					try{

						String s = getUTFString(logInStream);
						Pattern threadPt = Pattern.compile("last_res=");
						//1回目<thread のパケットで対応してるか判断
						Matcher mc = threadPt.matcher(s);
						if(!mc.find()){
							new NotSupportedCommentLog().execute();
						}
					while (logSock != null && !logSock.isClosed()) {
						// コメント取得用インプットストリームをパースする(最初にgetUTFStringした物を無視して最初からgetUTFStringとして取得される様)
						s = getUTFString(logInStream);
						InputSource source = new InputSource(new StringReader(s));
						SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
						parser.parse(source, new LogParser());
					}

					}catch(OutOfMemoryError e){
						error.setErrorCode(-22);
					}catch(SAXException e) {
					} catch (ParserConfigurationException e) {
						error.setErrorCode(-13);
						e.printStackTrace();
					} catch (IOException e) {
						error.setErrorCode(-13);
						e.printStackTrace();
					}
					return null;
				}
				@Override
				protected void onPostExecute(Void arg){
					if(error != null && error.getErrorCode() != 0){
						error.showErrorToast();
					}
				}

				/**
				 * ログのパース
				 * 指定された CommentHandler_Format を使用してコメントをパース
				 *
				 * @param handler
				 *            使用する CommentHandler_Format
				 */
				class LogParser extends MyDefaultHandler {
					private ReceiveHandlingFormatter formatter = new ReceiveHandlingFormatter();


					private Map<String, String> attributesMap = new HashMap<String, String>();
					private String[] newRecord;

					@Override
					public void start(String uri, String localName, String qName,
							Attributes attributes) {
						if (qName.equals("chat")) {
							int len = attributes.getLength();

							for (int i = 0; i < len; i++) {// パケットの属性値をキーに値をマップする
								attributesMap.put(attributes.getQName(i),
										attributes.getValue(i));
							}

							// 会員タイプなしで一般
							if (!attributesMap.containsKey(CommentRecieveFormat.USER_TYPE)) {
								attributesMap.put(CommentRecieveFormat.USER_TYPE, "");
							}
						}
					}

					@Override
					public void end(String uri, String localName, String qName) {
						if (qName.equals("chat")) {
							String type = attributesMap.get(CommentRecieveFormat.USER_TYPE);
							// システムが/disconnectを投げたら終了 "2"SYSTEM "3"OWNER
							if (type != null && (type.equals("3") || type.equals("2"))) {
								if (getInnerText().equals("/disconnect")) { // disconnectが見えたら終了
									closeMainConnection();
								}
							}

							// コメントのプロパティをRecieveクラスのフィールドにセット
							formatter.commentAttrReceived(attributesMap);
							newRecord = formatter
									.getReceivedComment(getInnerText());

							if (newRecord == null)
								return;

							//投稿時間計算
							String hour = "";
							int startTime = Integer.parseInt(liveinfo.getStartTime());
							long passedminute = (Long.parseLong(newRecord[3]) - startTime) / 60;
							String minute = String.format("%02d", passedminute);
							String second = String.format("%02d",
									(Long.parseLong(newRecord[3]) - startTime) % 60);
							if (passedminute > 59) {
								hour = String.format("%d:",
										(Long.parseLong(newRecord[3]) - startTime) / 3600);
								minute = String
										.format("%02d",
												((Long.parseLong(newRecord[3]) - startTime) % 3600) / 60);
							}
							newRecord[3] = hour + minute + ":" + second;
							try {
								if (!newRecord[5].equals(URLEnum.HYPHEN)||newRecord[5].equals("")) {// 公式はコメ番がない
//									commentCount = Integer.parseInt(newRecord[5]);
								}
							} catch (NumberFormatException e) {
									Log.d("log", "MISSED COMMENT" + newRecord[5]);
								e.printStackTrace();
							}

							if(logList.size() >= amount||newRecord[5].equals(firstNum)){//firstNum周辺はNGで来ない可能性がある
								if(logList.size()<1){
									closeLogConnection();
									if(sortTask == null||sortTask.getStatus()== AsyncTask.Status.FINISHED){
									sortTask = new SortTask();
									sortTask.execute();
									}
									return;
								}
								if(!logList.get(0)[5].equals(""))Collections.sort(logList,new CommentComparator_DownLayout());
								//NGコメントを処理
								int diffNum = Integer.parseInt(logList.get(0)[5]);//最初のコメ番と要素数との差を記憶しておく
								for(int i = 1; i < logList.size(); i++){
									if((i+diffNum) <  Integer.parseInt(logList.get(i)[5])){//iと差を足して要素の値にならなかったらコメ番が飛んでいる
										ngString[5] = String.valueOf(i+diffNum);
										logList.add(i,ngString.clone());
										i--;
									}
								}

								closeLogConnection();
								new AddLogTask().execute();
							}else{
								// 行を配列に保存
								logList.add(newRecord);
							}
						}
						attributesMap.clear();
					}// End of end
				}//End of LogParser

				/*
				 * 取得したコメントログ追加分をリストにインサートする
				 */
				class AddLogTask extends AsyncTask<Void,Void,Void>{

					@Override
					protected Void doInBackground(Void... arg0) {
						return null;
					}
					@Override
					protected void onPostExecute(Void arg){
						if(postable.isUplayout()){
							for(int i = logList.size()-1; i >= 0 ; i--){
								adapter.add(logList.get(i));
								}
						}else{
						for(int i = 0; i < logList.size() ; i++){
						adapter.insert(logList.get(i), i);
						}
						}
						isBuffering = false;
						closeLogConnection();
					}
				}

				/**
				 * コメントログに対応していない場合のエラートースト
				 * @author Owner
				 *
				 */
				class NotSupportedCommentLog extends AsyncTask<Void,Void,Void>{
					@Override
					protected Void doInBackground(Void... arg0) {
						return null;
					}
					@Override
					protected void onPostExecute(Void arg){
						MyToast.customToastShow((Context)postable, "ログ取得に対応していない放送です");
					}
				}


			}//End of GetCommentLogTask

			/**
			 * スクロールを末尾じゃなくした後、バッファはいている途中でなければ
			 * 末尾に戻した際に、バッファを追加する
			 */
			public void scrollEnded(){
//				Log.d("NLiveCC","ScrollEnd --" );
				if(buffer.size() > 0){
					flushBuffer();
				}
			}

			class CommentComparator_DownLayout implements java.util.Comparator {
				public int compare(Object s, Object t) {
					return Integer.parseInt(((String[])s)[5]) - Integer.parseInt(((String[])t)[5]);
				}
			}

			class CommentComparator_UpLayout implements java.util.Comparator {
				public int compare(Object s, Object t) {
					return -(Integer.parseInt(((String[])s)[5]) - Integer.parseInt(((String[])t)[5]));
				}
			}



			//読み上げダイアログからのセット
			public byte isSpeechEnable(){
				return speech_KindValue;
			}
			public void updateSpeechSetting(Context context,byte isEnable,byte speed,byte vol,byte pich){
				this.speech_KindValue = isEnable;
				if(postable != null){
					postable.setSpeachSettingValue(isEnable,speed,vol,pich);
				}
//				Log.d("----","SPEED ----------------------- " + speed);
					//読み上げを有効にする
						initSpeech(isEnable,context, speed,vol,pich, spech_skip_word,speech_max_buf);

			}
			public void killSpeech(){
				if(mSpeech != null){
				mSpeech.destroy();
				}
				mSpeech = null;
			}

			public Object[] getSpeechStatus(){
				Object[] status = new Object[]{5,5,speech_education,5,0};
				if(mSpeech != null)status = mSpeech.getStatus();
				return status;
			}

			public boolean getSpeechEducation() {
				return speech_education;
			}




	}//End of CommentTable


/**
 * コマンド
 *
 */
enum CommandKey {
	CMD, Size, Align, Color, Mobile, Se
};

enum CommandValue {

	ANONYM("184"), NOANON(""),
	// フォントサイズ変更
	DEFAULT_SIZE(""), BIG("big"), SMALL("small"),
	// 表示位置
	DEFAULT_POS(""), HIDARI("hidari"), MIGI("migi"), UE("ue"), SHITA("shita"),
	// 一般会員も利用できるコマンド
	DEFAULT_COLOR(""), RED("red"), BLUE("blue"), GREEN("green"), PINK("pink"), CYAN(
			"cyan"), ORANGE("orange"), YELLOW("yellow"), PURPLE("purple"),
	// プレミアムのみ利用できるコマンド
	NICONICOWHITE("niconicowhite"), WHITE2("white2"), TRUERED("truered"), RED2(
			"red2"), PASSIONORANGE("passionorange"), ORANGE2("orange2"), MADYELLOW(
			"madyellow"), YELLOW2("yellow2"), ELEMENTALGREEN("elementalgreen"), GREEN2(
			"green2"), MARINEBLUE("marineblue"), BLUE2("blue2"), NOBLEVIOLET(
			"nobleviolet"), PURPLE2("purple2"), BLACK("black"),
	// モバイル
	DEFAULT_MOBILE(""), DOCOMO("docomo"),
	// SE
	DEFAULT_SE(""), SE1("se1"), SE2("se2");

	// 列挙子よりも下に書かないと通らない
	private String name;

	private CommandValue(String name) {
		this.name = name;
	}

	public String toString() {
		return name;
	}
}// End of CommandValue

