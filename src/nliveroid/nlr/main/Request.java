package nliveroid.nlr.main;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PushbackInputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

import org.xml.sax.InputSource;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class Request {
	private static NLiveRoid app;
	//Unicodeの符号化形式で符号化したテキストの識別のため先頭につけるバイトデータ
	private static byte[] BOM = { (byte) 0xef, (byte) 0xbb, (byte) 0xbf };
	public static void disPoseAPP(){
		if(NLiveRoid.isDebugMode)Log.d("NLiveRoid","disposeApp --- " + getApp());
		if(getApp() != null){
			setApp(null);
		}
		System.gc();
	}

	private final static Pattern user_session = Pattern.compile("user_session=user_session_[0-9]+_([0-9]|[a-z])+");
	public final static String user_agent = "Niconico/1.0 (Linux; U; Android 4.0.4; ja-jp; nicoandroid SDK) Version/1.06.4";
	public static String simpledoGET(String URL,ErrorCode code) {
		URL url;
		InputStream is = null;
		String source = null;
		try {
			url = new URL(URL);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");
			is = con.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			String temp = null;
			while ((temp = br.readLine()) != null) {
				source += temp;
			}
			con.disconnect();
		}catch(UnknownHostException e){
			code.setErrorCode(-3);
			return null;
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return source;
	}

	public static byte[] doGetToByteArray(String url,ErrorCode code) {
		byte[] ba = null;
		try {

			URL URL = new URL(url);
			HttpURLConnection con = (HttpURLConnection) URL.openConnection();
			con.setRequestMethod("GET");
			InputStream is = con.getInputStream();
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			int size = 0;
			byte[] byteArray = new byte[1024];
			while ((size = is.read(byteArray)) != -1) {
				bos.write(byteArray, 0, size);
			}
			ba = bos.toByteArray();
			con.disconnect();
		} catch (UnknownHostException e) {
			code.setErrorCode(-3);
			e.printStackTrace();
		} catch (MalformedURLException e) {
			if(url.contains("img/_"))code.setErrorCode(-11);
			e.printStackTrace();
		} catch (FileNotFoundException e){
			//メンテナンス時も検出
			code.setErrorCode(-7);
			e.printStackTrace();
			return null;
		}catch (IOException e) {
			e.printStackTrace();
		}
		return ba;
	}

	/**
	 * session固定+url+errorのbyte[]を返す
	 * publishStatusで利用
	 */

	public static byte[] doGetToByteArray(String url,String session,ErrorCode code) {
		byte[] ba = null;
		try {

			URL URL = new URL(url);
			HttpURLConnection con = (HttpURLConnection) URL.openConnection();
			con.setRequestMethod("GET");
			con.setRequestProperty("Cookie", session);
			InputStream is = con.getInputStream();
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			int size = 0;
			byte[] byteArray = new byte[1024];
			while ((size = is.read(byteArray)) != -1) {
				bos.write(byteArray, 0, size);
			}
			ba = bos.toByteArray();
			con.disconnect();
		} catch (UnknownHostException e) {
			code.setErrorCode(-3);
			e.printStackTrace();
		} catch (MalformedURLException e) {
			if(url.contains("img/_"))code.setErrorCode(-11);
			e.printStackTrace();
		} catch (FileNotFoundException e){
			//メンテナンス時も検出
			code.setErrorCode(-7);
			e.printStackTrace();
			return null;
		}catch (IOException e) {
			code.setErrorCode(-6);
			e.printStackTrace();
			return null;
		}
		return ba;
	}

	/**
	 * doGetのセッションをセット有りのInputStream返しバージョン
	 * @param url
	 * @return
	 */
	public static InputStream doGetToInputStream(String url,ErrorCode code) {
		InputStream is = null;
		try {
			URL URL = new URL(url);
			HttpURLConnection con = (HttpURLConnection) URL.openConnection();
			con.setRequestMethod("GET");
			con.setRequestProperty("Cookie", getSessionID(code));
			is = con.getInputStream();
		} catch (UnknownHostException e) {
			code.setErrorCode(-3);
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
			} catch (FileNotFoundException e){
			//サムネ取得時に検出、その後getImageでNULLでそのままでOK
				e.printStackTrace();
		}catch (IOException e) {
			e.printStackTrace();
		}
		return is;
	}
	/**
	 * doGetのセッションを無しのInputStream返しバージョン
	 * @param url
	 * @return
	 */
	public static InputStream doGetSmartTopToInputStream(ErrorCode code) {
		InputStream is = null;
		try {

			URL URL = new URL(URLEnum.SMARTTOP);
			HttpURLConnection con = (HttpURLConnection) URL.openConnection();
			con.setRequestMethod("GET");
			is = con.getInputStream();
		} catch (UnknownHostException e) {
			code.setErrorCode(-3);
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
			} catch (FileNotFoundException e){
			//サムネ取得時に検出、その後getImageでNULLでそのままでOK
				e.printStackTrace();
		}catch(SocketException e){
			code.setErrorCode(-6);//アプリ再起が必要??
			e.printStackTrace();
		}catch (IOException e) {
			code.setErrorCode(-6);
			e.printStackTrace();
		}
		return is;
	}
	/**
	 * トップページのソースをインプットストリームで取得します
	 *URL等のエスケープでパース出来ない
	 *未使用
	 */

	public static InputStream getTopPageToInputStream(nliveroid.nlr.main.NLiveRoid.AppErrorCode code){
		InputStream source = null;
		try {
			String sessionid = getSessionID(code);
			if(sessionid == null){
				code.setErrorCode(-5);
				return null;
			}
			URL URL = new URL(URLEnum.SMARTTOP);

			HttpURLConnection con = (HttpURLConnection) URL.openConnection();
			con.setRequestProperty("User-Agent", "iPhone");
			con.setRequestMethod("GET");
			con.setRequestProperty("Cookie", sessionid);
			source = con.getInputStream();
//			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			String temp = null;
//			while ((temp = br.readLine()) != null) {
//				source += temp;
//			}
//			con.disconnect();
//			Log.d("log","SLJFDSL" +source);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch(UnknownHostException e){
			code.setErrorCode(-3);
			e.printStackTrace();
		}catch (IOException e) {
			e.printStackTrace();
		}
		return source;
	}


	/**
	 * ログインします
	 * @param mailAddress
	 * @param password
	 * @return
	 */
	public static String nicoLogin(String mailAddress, String password,ErrorCode code) {
		if(NLiveRoid.isDebugMode)Log.d("NLiveRoid","N Lin N");
		String source = null;
		try {
			URL url = new URL(URLEnum.LOGINURL);
			HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
			con.setRequestMethod("POST");
			con.setDoOutput(true);
			con.setInstanceFollowRedirects(true);//redirectされてから行くようにスマホ版で変わった?

			mailAddress = URLEncoder.encode(mailAddress, "UTF-8");
			password = URLEncoder.encode(password, "UTF-8");
			final String cParam = String.format("mail=%s&password=%s",
					mailAddress, password);
			// フォーム、コンテントレングス、ユーザーエージェントは無くても通常のセッションIDは取得できる

			// ここが取れない場合、ここの前にgetInputStream()(getContentLength)とか
			// この前にやってるとできない、もしくはUnknownHostとかはエミュの再起動で直る
			PrintStream out = new PrintStream(con.getOutputStream());
			out.print(cParam);
			Map<String,List<String>> list = con.getHeaderFields();
			List<String> cookie_list = list.get("set-cookie");
//			Iterator<String> it = list.keySet().iterator();
//			while(it.hasNext()){
//				String key = it.next();
//				Log.d("NLiveRoid","KEY: " + key);
//				for(int i = 0; i < list.get(key).size(); i++){
//				Log.d("NLiveRoid","LIST: " + list.get(key).get(i));
//				}
//			}
			for(int i = 0; i < cookie_list.size(); i++){
//				Log.d("NLiveRoid","COOKIE--- : " + cookie_list.get(i));
				if(user_session.matcher(cookie_list.get(i)).find()){
					source = cookie_list.get(i);
					break;
				}
			}
			if (source == null) {
				code.setErrorCode(-2);// IDかパスが違っている
				Log.d("NLiveRoid", "FAILED LOGIN");
				return null;
			}
			con.disconnect();
		}catch (NullPointerException e){//OutputStreamを開けなくなる事がある(原因不明)
			code.setErrorCode(-6);
			e.printStackTrace();
		} catch (UnknownHostException e) {
			code.setErrorCode(-3);
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			code.setErrorCode(-6);
			e.printStackTrace();
		}
		if(NLiveRoid.isDebugMode)Log.d("NLiveRoid","N Lin END" + (source != null? "Success":"NULL"));
		return source;
	}

	/**
	 * セッションIDを取得します 最初に今のフィールドを見に行ってなければ 設定のメアド、パスからログインして取得
	 * 複数スレッドからアクセスされる為、synchronized必須
	 * @return
	 */
	protected synchronized static String getSessionID(ErrorCode error) {
		if(NLiveRoid.isDebugMode)Log.d("NLiveRoid","getSession "+error.getErrorCode());
		//設定値にセッションがあったらそれを使う
		String sessionid = "";
		if(getApp() == null)return null;//回線弱い時、synchronizedで固まって、nullになる

		sessionid = getApp().getSessionid();
			// 設定のマップにセッションがあった
			//マップが文字列返しなのでnullの時文字列として'null'になる場合がある
			if(sessionid != null &&!sessionid.equals("null")&&!sessionid.equals("")){
//				Log.d("log","FIND SESSION MAP");
			return sessionid;
			}
			//設定のマップにセッションが無かった
			String id =   getApp().getUserIDFromMap();
			String pass =  getApp().getPasswordFromMap();
			if (id == null || pass == null || id.equals("") || pass.equals("")) {
				// その他の設定値もしくはファイルが初期状態アカウント未入力
				error.setErrorCode(-1);
				return null;
			}
			sessionid = nicoLogin(id, pass,error);//普通のログインしに行く
			if(error.getErrorCode() != 0){//source == null UnknownHostException IOException
				return null;
			}
			if ((sessionid == null||sessionid.equals(""))){
				error.setErrorCode(-2);
			return null;// nullだったらそのまま返す(IDかパスがあったけど違った)
			}
			Matcher mc = null;
			try{
			mc = user_session.matcher(
					sessionid);
			if (mc.find()) {
				if(NLiveRoid.isDebugMode)Log.d("NLiveRoid","getNewSessionFind"+error.getErrorCode());
				//セッション取得成功
				sessionid = mc.group();
				//セッションをマップに書き込んでおく
				if(getApp() != null)getApp().setSessionid(sessionid);//アプリ終了のタイミングによってありえる
			} else {// セッションIDがあったけど駄目だった
				//セッションを消しておく
				if(getApp() != null)getApp().setSessionid("");//アプリ終了のタイミングによってありえる
				Log.d("NLiveRoid","session..ERROR -4 " + getApp());
				error.setErrorCode(-4);
				return null;
			}
			//サービス起動中にgetApp()がnullの場合もある
//			Log.d("NLiveRoid"," SESE " + getApp().getSp_session_key() +" " + getApp().getDetailsMapValue("player_select"));
//			if(getApp().getSp_session_key() == null && getApp().getDetailsMapValue("player_select") != null && getApp().getDetailsMapValue("player_select").equals("2")){
//				//HLSの設定だったら、セッションを取得しておく
//				String sp_sessionid = nicoLogin_sp(id, pass, error);
//				if(error.getErrorCode() != 0){//source == null UnknownHostException IOException
//					return null;
//				}
//				if ((sp_sessionid == null||sp_sessionid.equals(""))){
//					error.setErrorCode(-51);
//					return null;
//				}
//				getApp().setSp_session_key(sp_sessionid);
//			}


			}catch(NullPointerException e){
				//アカウントロックまたはメンテ中??
				error.setErrorCode(-14);
				e.printStackTrace();
			}
			if(NLiveRoid.isDebugMode)Log.d("NLiveRoid","getSession RESULT "+sessionid + " code:"+error.getErrorCode());
			return sessionid;
	}
	protected synchronized static String getSPSession(ErrorCode code) {
		if(NLiveRoid.isDebugMode)Log.d("NLiveRoid","getSPSession "+code.getErrorCode());
		//設定値にセッションがあったらそれを使う
		String sessionid = "";
		if(getApp() == null)return null;//回線弱い時、synchronizedで固まって、nullになる
		if(getApp().getSp_session_key() != null && !getApp().getSp_session_key().equals("")){
			return getApp().getSp_session_key();
		}
			//設定のマップは見に行かない
			String id =   getApp().getUserIDFromMap();
			String pass =  getApp().getPasswordFromMap();
			if (id == null || pass == null || id.equals("") || pass.equals("")) {
				// その他の設定値もしくはファイルが初期状態アカウント未入力
				code.setErrorCode(-1);
				return null;
			}
			sessionid = nicoLogin_sp(id, pass,code);//HLSログインしに行く
			if(code.getErrorCode() != 0){//source == null UnknownHostException IOException
				return null;
			}
			if ((sessionid == null||sessionid.equals(""))){
				code.setErrorCode(-2);
			return null;// nullだったらそのまま返す(IDかパスがあったけど違った)
			}
			Matcher mc = null;
			try{
			mc = Pattern.compile("SP_SESSION_KEY=user_session_[0-9]+_[0-9]+").matcher(
					sessionid);
			if (mc.find()) {
				if(NLiveRoid.isDebugMode)Log.d("NLiveRoid","SPgetNewSessionFind"+code.getErrorCode());
				//セッション取得成功
				sessionid = mc.group();
				//セッションをマップに書き込んでおく
				if(getApp() != null)getApp().setSp_session_key(sessionid);//SP_SESSSION_KEYでセッションを上書きする(SP_SESSION_KEYと普通のセッションを同時に使おうとすると後のセッションだけが優先される)
			} else {// Set-Cookieがあったけど駄目だった
				//セッションを消しておく
				if(getApp() != null)getApp().setSp_session_key("");//アプリ終了のタイミングによってありえる
				Log.d("NLiveRoid","SP ERROR -4");
				code.setErrorCode(-4);
				return null;
			}
			}catch(NullPointerException e){
				//アカウントロック時発生?
				code.setErrorCode(-14);
				e.printStackTrace();
			}
			return sessionid;
	}
	private static String nicoLogin_sp(String id, String pass, ErrorCode error) {
		Log.d("NLiveRoid ","nicoLogin_sp");
		String result = null;
		try {
			String[] cookieSource = new String[2];
			URL url = new URL("https://secure.nicovideo.jp/secure/login?site=nicoandroid");
			HttpsURLConnection con1 = (HttpsURLConnection) url.openConnection();
			con1.setRequestProperty("User-Agent", user_agent);
//			con1.setRequestProperty("X-Nicovideo-Connection-Type", "wifi\r\n");//ログイン時にこれを付けちゃうと2.2ではいけないことがある
			con1.setRequestMethod("POST");
			con1.setReadTimeout(20000);
			con1.setDoOutput(true);
			// ここが取れない場合、ここの前にgetInputStream()(getContentLength)とか
			// この前にやってるとできない、もしくはUnknownHostとかはエミュの再起動で直る
			final String cParam = String.format("mail=%s&password=%s&Login=true",
					URLEncoder.encode(id, "UTF-8"), URLEncoder.encode(pass, "UTF-8"));
			PrintStream out = new PrintStream(con1.getOutputStream());
			out.print(cParam);
			Map<String,List<String>> list = con1.getHeaderFields();

			if(list.get("set-cookie")!=null){
			cookieSource[0] = list.get("set-cookie").get(0);
			}else{
				error.setErrorCode(-51);
				return null;
			}
				InputStream is = con1.getInputStream();
				BufferedReader br = new BufferedReader(new InputStreamReader(is));
				String temp = "";
				String source = "";
				for(;(temp = br.readLine()) != null;){
					source += temp;
				}
//				Log.d("SSSSSSSS ","  " + source);
				Matcher mc = Pattern.compile("user_session_[0-9]+_[0-9]+").matcher(source);//普通の=で結ばれたものと違う(右側のみ)事に注意
				if(mc.find()){
					Log.d("NLiveRoid"," nicoFind");
					cookieSource[1] = mc.group();
				}else{//ログイン失敗
					Log.d("NLiveRoid"," nicoFailed");
					error.setErrorCode(-51);
				}
			br.close();
			is.close();
			con1.disconnect();
			url = new URL("http://api.gadget.nicovideo.jp/login?nicoSessionKey=" + cookieSource[1]);
			HttpURLConnection con2 = (HttpURLConnection) url.openConnection();
			con2.setRequestProperty("User-Agent", user_agent);
//			con2.setRequestProperty("X-Nicovideo-Connection-Type", "wifi\r\n");//ここでも2.2だとこれを付けると失敗する
			con2.setRequestProperty("Cookie", cookieSource[0]);
			con2.setRequestProperty("content-type", "application/x-www-form-urlencoded");//ここのRequestPropertyは2.2ではcontent-type,POSTが必須くさい
			con2.setRequestMethod("POST");
			con2.setReadTimeout(20000);
			con2.setDoOutput(true);
			is = con2.getInputStream();
			br = new BufferedReader(new InputStreamReader(is));
			temp = "";
			source = "";
			for(;(temp = br.readLine()) != null;){
				source += temp;
			}
//			Log.d("SSSSSSSS ","  " + source);
			list = con2.getHeaderFields();
			if(list.get("set-cookie") == null){
				error.setErrorCode(-51);
				return null;
			}else{
				result = list.get("set-cookie").get(0);
			}
//			Log.d(" XXXXXXXXXXXX " , " " + result);
			con2.disconnect();
		}catch (NullPointerException e){//OutputStreamを開けなくなる事がある(原因不明)
			e.printStackTrace();
			error.setErrorCode(-6);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			error.setErrorCode(-3);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			error.setErrorCode(-3);
		} catch (IOException e) {
			e.printStackTrace();
			error.setErrorCode(-6);
		}
		return result;
	}

	/**
	 * マイページをストリングで返す
	 * 未使用
	 *
	 * @param pageNum
	 * @return
	 */
	public static String getMyPageToString(int pageNum,ErrorCode code) {
		String source = null;
		try {
			String sessionid = getSessionID(code);

			URL URL = new URL(URLEnum.MYPAGECOMMUNITY + "?page=" + pageNum);

			HttpURLConnection con = (HttpURLConnection) URL.openConnection();
			con.setRequestMethod("GET");
			con.setRequestProperty("Cookie", sessionid);

			InputStream is = con.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			String temp = null;
			while ((temp = br.readLine()) != null) {
				source += temp;
			}
			con.disconnect();
		}catch (NullPointerException e){
			code.setErrorCode(-6);
			e.printStackTrace();
		} catch (UnknownHostException e) {
			code.setErrorCode(-3);
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return source;
	}

	/**
	 * PC版のマイページのソースをInputStreamで取得します
	 *
	 * @param pageNum
	 * @return
	 */
	public static InputStream getMyPageToInputStream(int pageNum,ErrorCode error) {
		try {
			String sessionid = getSessionID(error);
			if (sessionid == null)return null;//エラーはgetSessionID()でセットしているはずなのでOK


			URL URL = new URL(URLEnum.MYPAGECOMMUNITY + "?page=" + pageNum);
			HttpURLConnection con = (HttpURLConnection) URL.openConnection();
			con.setRequestMethod("GET");
			con.setRequestProperty("User-Agent", "1");
			con.setRequestProperty("Cookie", String.format(sessionid));

			return con.getInputStream();

		}catch (NullPointerException e){
			if(error != null){
			error.setErrorCode(-6);
			e.printStackTrace();
			}
		} catch (UnknownHostException e) {
			if(error != null){
				error.setErrorCode(-3);
				 e.printStackTrace();
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch(FileNotFoundException e){
			//混雑中の事があるのでもう一度行く
			Log.d("Log","CONZATU -");
			try {
				Thread.sleep(500);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}catch(IllegalArgumentException e1){
				e.printStackTrace();
				Log.d("NLiveRoid","IllegalArgumentException at Request getMyPageToInputStream");
			}
			return getMyPageToInputStream(pageNum,error);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * マイページの情報をバイト型配列で返します
	 * 未使用
	 *
	 * @return
	 */
	public static byte[] getMyPageToByteArray(int pageNum,ErrorCode code) {
		byte[] ba = null;
		try {
			String sessionid = getSessionID(code);

			URL URL = new URL(URLEnum.MYPAGECOMMUNITY + "?page=" + pageNum);
			HttpURLConnection con = (HttpURLConnection) URL.openConnection();
			con.setRequestMethod("GET");
			con.setRequestProperty("User-Agent", "1");
			con.setRequestProperty("Cookie", String.format(sessionid));

			InputStream is = con.getInputStream();
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			int size = 0;
			byte[] byteArray = new byte[1024];
			while ((size = is.read(byteArray)) != -1) {
				bos.write(byteArray, 0, size);
			}
			ba = bos.toByteArray();
			con.disconnect();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return ba;
	}

	/**
	 *
	 * 放送IDからgetPlayerStatusAPIへ接続し、レスポンスをバイト配列で返します
	 *
	 *
	 */

	public static byte[] getPlayerStatusToByteArray(String liveID,ErrorCode error) {
		byte[] ba = null;
		try {
			URL url = new URL(URLEnum.GETPLAYER + liveID);


			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			String sessionid = getSessionID(error);

			con.setRequestProperty("Cookie", sessionid);

			InputStream is = con.getInputStream();
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			int size = 0;
			byte[] byteArray = new byte[1024];
			while ((size = is.read(byteArray)) != -1) {
				bos.write(byteArray, 0, size);
			}
			ba = bos.toByteArray();
			con.disconnect();
		} catch(UnknownHostException e){
			if(error != null){
				error.setErrorCode(-3);
		}
		}catch (MalformedURLException e) {
			e.printStackTrace();
		} catch(FileNotFoundException e){
			if(error != null)error.setErrorCode(-1);
		}catch (IOException e) {
			e.printStackTrace();
		}
		return ba;
	}


	/**
	 *
	 * getPlayerのセッションを指定したバージョン
	 *
	 *
	 */

	public static byte[] getPlayerStatusToByteArray(String liveID,ErrorCode error,String sessionid) {
		byte[] ba = null;
		try {
			URL url = new URL(URLEnum.GETPLAYER + liveID);

			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setInstanceFollowRedirects(false);
			con.setReadTimeout(30000);
			con.setRequestProperty("Cookie", sessionid);

			InputStream is = con.getInputStream();
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			int size = 0;
			byte[] byteArray = new byte[1024];
			while ((size = is.read(byteArray)) != -1) {
				bos.write(byteArray, 0, size);
			}
			ba = bos.toByteArray();
			bos.close();
			con.disconnect();
		}catch(SocketException e){
			e.printStackTrace();
			ba = "SocketException".getBytes();
		}catch(UnknownHostException e){
			e.printStackTrace();
		}catch (MalformedURLException e) {
			e.printStackTrace();
		} catch(FileNotFoundException e){
			e.printStackTrace();
		}catch (IOException e) {
			e.printStackTrace();
		}
		return ba;
	}


	/**
	 * getPlayerのInputSream返し版
	 * パーサの入力がInputSourceの方がいい(byte[]だと何故か失敗する場合がある)ので
	 * もっと失敗するので未使用
	 */
	public static InputSource getPlayerStatusFromURL(String lv,ErrorCode error) {
		InputSource source = null;
		try {
			URL url = new URL(URLEnum.GETPLAYER +lv);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			String sessionid = getSessionID(error);
			con.setRequestProperty("Cookie", sessionid);
			PushbackInputStream in = new PushbackInputStream(
					con.getInputStream(), BOM.length);

			// UTF-8 の符号化で他のコードと識別のために付けるデータをスキップする
//			byte[] b = new byte[BOM.length];
//			in.read(b, 0, b.length);

//			if (!Arrays.equals(b, BOM)) {
//				in.unread(b);
//			}
			// XML入力ソース
			new InputSource(new InputStreamReader(in, "UTF-8"));


		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return source;
	}
	/**
	 * スレッド番、ホスト名、ポート番号から アラートサーバから新着情報を取得
	 *未使用
	 * @param thread
	 * @param host
	 * @param m_port
	 * @return
	 */
	public static String[] connectAlertServer(String thread, String host,
			String m_port, int getCount,ErrorCode error) {
		String[] source = new String[getCount];
		Socket sock = null;
		InetAddress address = null;

		try { // ホスト名からIPアドレスを取得
			address = InetAddress.getByName(host);

			// Socketの作成
			sock = new Socket(address, Integer.parseInt(m_port));
			sock.setKeepAlive(true);

			// リクエストメッセージを送信
			// 注)最後に'\0'を挿入しないとレスポンスは返ってこない
			DataOutputStream msgOutStream = new DataOutputStream(
					sock.getOutputStream());
			String param = String
					.format("<thread thread=\"%s\" version=\"20061206\" res_from=\"-1\"/>\0",
							thread);

			// コメント取得用パケットを投げる
			msgOutStream.writeBytes(param);// コメントを取得する件数

			InputStream msgInStream = null;
			for (int i = 0; i < getCount; i++) {
				// 受信する
				msgInStream = sock.getInputStream();
				source[i] = getSingleString(msgInStream).trim();

			}// End of for

		} catch (Exception e) {
			if(error != null){
			error.setErrorCode(-3);
			e.printStackTrace();
			}
		}
		return source;
	}

	/**
	 * APIアラートの新着取得ヘルパーメソッド
	 *
	 * @param in
	 * @return
	 * @throws IOException
	 */
	private static String getSingleString(InputStream in) throws IOException {

		ByteArrayOutputStream bo = new ByteArrayOutputStream(256);
		int c;

		try {
			while ((c = in.read()) != 0x00) {
				bo.write(c);
			}
		} catch (SocketException e) {
			//ソケット閉じてる
		} catch (OutOfMemoryError el) {
		}

		return bo.toString("UTF-8");
	}

	/**
	 * コメポスト時のポストキー取得のため
	 * アラートの際の基本リクエスト
	 * ソースを固定セッションでリクエストする
	 * @param url
	 * @return
	 */
	public static InputStream doGetToInputStreamFromFixedSession(String session,String url,ErrorCode error) {
		InputStream is = null;
		try {
			URL URL = new URL(url);
			HttpURLConnection con = (HttpURLConnection) URL.openConnection();
			con.setRequestMethod("GET");
			con.setRequestProperty("Cookie", session);
			is = con.getInputStream();
		} catch (UnknownHostException e) {
			if(error != null){
				error.setErrorCode(-3);
			e.printStackTrace();
		}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e){
			//検索で間違ったURLにアクセスすると検出
			//サムネ取得時に検出、その後getImageでNULLでそのままでOK
			e.printStackTrace();
		}catch(SocketException e){
			e.printStackTrace();
		}catch (IOException e) {
			e.printStackTrace();
		}
		return is;
	}
	/**
	 * 主コメポスト時のポストキー取得のためのソースを固定セッションでリクエストする
	 * @param url
	 * @return
	 */
	public static InputStream doPostToInputStreamFromFixedSession(String session,String url,ErrorCode error) {
		InputStream is = null;
		try {
			URL URL = new URL(url);
			HttpURLConnection con = (HttpURLConnection) URL.openConnection();
			con.setRequestMethod("POST");
			con.setRequestProperty("Cookie", session);
			is = con.getInputStream();
		} catch (UnknownHostException e) {
			if(error != null){
				error.setErrorCode(-3);
			e.printStackTrace();
		}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e){
			e.printStackTrace();
		}catch (IOException e) {
			e.printStackTrace();
		}
		return is;
	}

	/**
	 * 固定セッション+固定リファラーでリクエストする
	 * 未使用
	 * @param url
	 * @return
	 */
	public static InputStream doGetToInputStreamFromFixedSession(String session,String referer,String url,ErrorCode error) {
		InputStream is = null;
		try {
			URL URL = new URL(url);
			HttpURLConnection con = (HttpURLConnection) URL.openConnection();
			con.setRequestMethod("GET");
			con.setRequestProperty("Cookie", session);
			con.setRequestProperty("Referer", referer);
			is = con.getInputStream();
		} catch (UnknownHostException e) {
			if(error != null){
				error.setErrorCode(-3);
			e.printStackTrace();
		}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e){
			//サムネ取得時に検出、その後getImageでNULLでそのままでOK
			e.printStackTrace();
		}catch (IOException e) {
			e.printStackTrace();
		}
		return is;
	}



	/**
	 * URLのロケーションの画像を取得する
	 *取得できなかったらnoimangeを返す
	 * @param url
	 * @return
	 */
	public static Bitmap getImage(String url,ErrorCode error) {
		try{
		byte[] byteArray = Request.doGetToByteArrayForImg(url,error);
		if(byteArray!=null){//メンテ、サムネURLへのリクエスト失敗時null
			if(byteArray.length == 1){//要素が1つの場合、失敗ステータスで区別する
				if(byteArray[0] == 99){
					return getNoImage();
				}
			}else{
				return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
			}
		}else{//NoImage
		return getNoImage();
		}
		}catch(NullPointerException e){
			//イメージが無かった
		}
		return null;
	}

	/**
	 * 取得できなかったら取り直す版 チャンス5回
	 * @param url
	 * @param error
	 * @return
	 */
	public static Bitmap getImageForList(String url, ErrorCode error,int count){
		if(count > 4)return null;
		try{
			byte[] byteArray = Request.doGetToByteArrayForImg(url,error);
			if(byteArray!=null){//メンテ、サムネURLへのリクエスト失敗時null
			return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
			}
//			Log.d("log","NO IMAGE COUNT " + count);
			try {
				Thread.sleep(400);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}catch(IllegalArgumentException e1){
				e1.printStackTrace();
				Log.d("NLiveRoid","IllegalArgumentException at Request getImageForList");
			}
			return getImageForList(url,error,++count);//2回目をやっている
			}catch(NullPointerException e){
				//イメージが無かった
			}
			return null;
	}

	/**
	 * 画像用のGETリクエスト
	 */

	public static byte[] doGetToByteArrayForImg(String url,ErrorCode error) {
		byte[] ba = null;
		try {

			URL URL = new URL(url);
			HttpURLConnection con = (HttpURLConnection) URL.openConnection();
			con.setRequestMethod("GET");
			InputStream is = con.getInputStream();
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			int size = 0;
			byte[] byteArray = new byte[1024];
			while ((size = is.read(byteArray)) != -1) {
				bos.write(byteArray, 0, size);
			}
			ba = bos.toByteArray();
			con.disconnect();
		}catch(SocketException e){
			Log.d("NLiveRoid","SocketException at doGetToByteArrayForImg!!!!");
			e.printStackTrace();
			return new byte[]{99};
		} catch (UnknownHostException e) {
			if(error != null){
				error.setErrorCode(-3);
			e.printStackTrace();
			}
		} catch (MalformedURLException e) {
			if(url.contains("img/_")&&error != null){
			error.setErrorCode(-11);
			e.printStackTrace();
			}
			return null;
		} catch (FileNotFoundException e){
			//サムネ取得時にNoImageの場合検出
			//失敗フラグをセットしちゃうと、リストを更新しなくなってしまうので
			//フラグセットしない
//			if(error != null)error.setErrorCode(-11);
				Log.d("NLiveRoid","MISSED THUMBNAIL code 0");
//				e.printStackTrace();
			return new byte[]{99};
		}catch(EOFException e){//リトライする
//			e.printStackTrace();
			Log.d("NLiveRoid","MISSED THUMBNAIL code 1");
			return null;
		}catch (IOException e) {
			Log.d("NLiveRoid","MISSED THUMBNAIL code 2");
			e.printStackTrace();
			return new byte[]{99};
		}catch(Exception e){
			e.printStackTrace();
			Log.d("NLiveRoid","MISSED THUMBNAIL code 3");
			if(error != null)error.setErrorCode(-11);
			return new byte[]{99};
		}
		return ba;
	}

	private static Bitmap getNoImage(){
		if(getApp() == null){
			return null;
		}
		 InputStream is = getApp().getResources().openRawResource(R.drawable.noimage);
		 Bitmap bmp = null;
			 try {
				 bmp = BitmapFactory.decodeStream(is);
			 } finally {
			 try {
			 is.close();
			 } catch(IOException e) {
			 // Ignore.
			 }
			 }
			 return bmp;
	}
	/**
	 * parentACTを設定します。
	 * @param parentACT parentACT
	 */
	public static void setApplication(NLiveRoid parentACT) {
		setApp(parentACT);
	}


	/**
	 * エンコーダのための、簡易publishstatus取得メソッド
	 * @param url
	 * @param string
	 * @return
	 */
	public static byte[] doGetToByteArray(String url,String str) {
		byte[] ba = null;
		try {

			URL URL = new URL(url);
			HttpURLConnection con = (HttpURLConnection) URL.openConnection();
			con.setRequestMethod("GET");
			con.setRequestProperty("Cookie",str);
			InputStream is = con.getInputStream();
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			int size = 0;
			byte[] byteArray = new byte[1024];
			while ((size = is.read(byteArray)) != -1) {
				bos.write(byteArray, 0, size);
			}
			ba = bos.toByteArray();
			con.disconnect();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e){
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return ba;

	}

	/**
	 * HeatBeatURLにSP_SESSION_KEyでアクセス
	 * @param sp_session_key
	 * @param lv
	 * @param session
	 * @param error
	 * @return
	 */
	public static String[] getHLSURLs(String lv,ErrorCode error) {
		try {
			String session = getSPSession(error);
			if(session == null){
				error.setErrorCode(-51);
				return null;
			}
			URL url = new URL(String.format(URLEnum.PLAYLISTURL, lv));
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestProperty("User-Agent", user_agent);
//			con.setRequestProperty("X-Nicovideo-Connection-Type", "wifi\r\n");//ここでも2.2で駄目
			con.setRequestProperty("content-type", "application/x-www-form-urlencoded");
			con.setRequestProperty("Cookie", getSPSession(error));
			con.setRequestProperty("Cookie2", "$Version=1");
			con.setRequestMethod("GET");
				InputStream is = con.getInputStream();
				BufferedReader br = new BufferedReader(new InputStreamReader(is));
				String temp = "";
				String source = "";
				while ((temp = br.readLine()) != null) {
					source += temp;
				}
				if(NLiveRoid.isDebugMode)Log.d("NLiveRoid","getHLSURLs " + source);
				String[] params = source.split("heartBeatUrl\":\"|\",\"playListUrl\":\"|\",\"heartBeatKey\":\"|\",\"streamSyncUrl");
				con.disconnect();
				if(params.length == 5){
					return params;
				}else{
					Log.d("NLiveRoid","ARRAY WAS FAILED !!! ");//仕様変更された?
					error.setErrorCode(-53);
					return null;
				}
		}catch(FileNotFoundException e){
			e.printStackTrace();
			error.setErrorCode(-52);
			return null;
		}catch (NullPointerException e){//OutputStreamを開けなくなる事がある(原因不明)
			e.printStackTrace();
			error.setErrorCode(-6);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			error.setErrorCode(-3);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			error.setErrorCode(-3);
		} catch (IOException e) {
			e.printStackTrace();
			error.setErrorCode(-6);
		}
		return null;
	}

	public static String getHLSSession(String[] hlsValues, ErrorCode error) {
		String result = null;
		try {
		HttpURLConnection con = (HttpURLConnection) new URL(hlsValues[1] +"&key="+hlsValues[2]).openConnection();
		con.setRequestProperty("User-Agent",user_agent);
//		con.setRequestProperty("X-Nicovideo-Connection-Type", "wifi\r\n");//2.2以下で駄目
		String sp_session_key = getSessionID(error);
		if(error.getErrorCode() != 0||sp_session_key == null){
			return null;
		}
		con.setRequestProperty("Cookie", sp_session_key);
//		con.setRequestProperty("X-Nicovideo-Connection-Type", "wifi\r\n");

		if(NLiveRoid.isDebugMode)Log.d("NLiveRoid" , " getHLSSession " + con.getResponseCode());
		Map<String,List<String>> list = con.getHeaderFields();
		List<String> cookieValue = list.get("set-cookie");
		if(cookieValue == null){
			Log.d("NLiveRoid " , "Failed hls null");
			error.setErrorCode(-51);
			return null;
		}else{
			Log.d("NLiveRoid " , "Success hls");
			result = cookieValue.get(0);
		}
		con.disconnect();
	}catch (NullPointerException e){//OutputStreamを開けなくなる事がある(原因不明)
		e.printStackTrace();
		error.setErrorCode(-6);
	} catch (UnknownHostException e) {
		e.printStackTrace();
		error.setErrorCode(-3);
	} catch (MalformedURLException e) {
		e.printStackTrace();
		error.setErrorCode(-3);
	} catch (IOException e) {
		e.printStackTrace();
		error.setErrorCode(-6);
	}
		return result;
	}

	public static NLiveRoid getApp() {
		return app;
	}
	public static void setApp(NLiveRoid app) {
		Request.app = app;
	}


}
