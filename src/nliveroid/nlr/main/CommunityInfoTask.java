package nliveroid.nlr.main;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nliveroid.nlr.main.parser.CommunityInOutParser;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;
import android.view.WindowManager.BadTokenException;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

/**
 * コミュ参加/退会
 */
public class CommunityInfoTask extends AsyncTask<String,Void,Void>{
	private Context context;
	private CommunityInfoTask thisTask;
	private String commuID = "";
	private String sessionid = "";
	private boolean ENDFLAG = true;
	private boolean isError = false;
	private int width = 480;
	private boolean isJoined;
	private boolean isDemand;
	private ErrorCode error;


	public CommunityInfoTask(Context context,String comuID,String session,int width){
		this.thisTask = this;
		this.context = context;
		 this.commuID = comuID;
		 this.sessionid = session;
		this.width = width;
//		Log.d("CommunityInfoTask"," " + context + " " + commuID + " " + sessionid +" " + width);
	error = ((NLiveRoid) context.getApplicationContext())
			.getError();
	}

	@Override
	protected Void doInBackground(String... params) {

		 try {
		HttpURLConnection con = (HttpURLConnection)new URL(URLEnum.MOTION+commuID).openConnection();
		con.setRequestProperty("Cookie", sessionid);
		con.setRequestProperty("Referer", URLEnum.COMMUNITYURL+commuID);
//		Log.d("RESNLR","------------"+con.getResponseCode());
//		Log.d("CommuInfoTask"," ss " + (URLEnum.MOTION+commuID));
		InputStream is = con.getInputStream();
			  org.ccil.cowan.tagsoup.Parser parser = new org.ccil.cowan.tagsoup.Parser();
			  CommunityInOutParser handler = new CommunityInOutParser(thisTask,error);
		        parser.setContentHandler(handler);
		        parser.parse(new InputSource(is));
		  }catch(FileNotFoundException e){//motionにいけない事で参加してると判断(不安)
//			  Log.d("Log","ALREADY JOINED --- " +error);
			  this.finishCallBack(error, true, false);
//			  e.printStackTrace();
		  } catch (org.xml.sax.SAXNotRecognizedException e) {
		      // Should not happen.
			  e.printStackTrace();
		  } catch (org.xml.sax.SAXNotSupportedException e) {
		      // Should not happen.
			  e.printStackTrace();
		  } catch (IOException e) {
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
				break;
			}catch(IllegalArgumentException e){
				e.printStackTrace();
				ENDFLAG = false;
				break;
			}
			if((System.currentTimeMillis() - startT) > 30000){
				isError = true;
				error.setErrorCode(-10);
				return null;
			}
		}

		return null;
	}

	public void finishCallBack(ErrorCode error,boolean isJoined,boolean isApply){
		this.isJoined = isJoined;
		this.isDemand = isApply;
		if(error != null && error.getErrorCode() == 0){//正常
		}else if(error.getErrorCode() != 0){
			isError = true;
		}
		ENDFLAG = false;
	}

	protected void onPostExecute(Void arg){
		if(isError){
			error.showErrorToast();
		}else if(isJoined){//参加してたら退会
			try{
			new AlertDialog.Builder(context)
			.setTitle("コミュニティ退会")
			.setMessage("退会しますか?")
			.setPositiveButton("YES", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					new ResignTask().execute();
				}
			})
			.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
				}
			}).create().show();
				}catch(BadTokenException e){
//					e.printStackTrace();
					Log.d("NLiveRoid","Returned Resign dialog after finished App");
				}catch(Exception e){
					e.printStackTrace();
				}
		}else if(!isJoined){
			final AlertDialog.Builder dialog =new AlertDialog.Builder(context);
			dialog
			.setTitle("コミュニティ参加")
			.setMessage("参加しますか?");
			if(isDemand){//参加申請あり
				ScrollView sv = new ScrollView(context);
				TableLayout tl = new TableLayout(context);
				tl.setStretchAllColumns(true);
				final CheckBox cb = new CheckBox(context);
				cb.setText("結果をメールで受け取る");
				TextView tv0 =new TextView(context);
				tv0.setText("参加申請タイトル");
				final EditText title = new EditText(context);
				title.setLines(1);
				title.setWidth(width/2);
				TextView tv1 =new TextView(context);
				tv1.setText("参加申請詳細");
				final EditText comment = new EditText(context);
				comment.setLines(3);
				comment.setWidth(width/2);
				TableRow tr = new TableRow(context);
				tr.addView(cb);
				TableRow tr0 = new TableRow(context);
				tr0.addView(tv0);
				TableRow tr1 = new TableRow(context);
				tr1.addView(title);
				TableRow tr2 = new TableRow(context);
				tr2.addView(tv1);
				TableRow tr3 = new TableRow(context);
				tr3.addView(comment);
				tl.addView(tr,-1,-2);
				tl.addView(tr0,-1,-2);
				tl.addView(tr1,-1,-2);
				tl.addView(tr2,-1,-2);
				tl.addView(tr3,-1,-2);
				sv.addView(tl,-1,-1);
			dialog
			.setView(sv)
			.setPositiveButton("YES", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					new SignTask().execute(String.valueOf(cb.isChecked()),title.getText().toString(),comment.getText().toString());
				}
			})
			.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
				}
			});
			}else{//参加申請なし
				dialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						new SignTask().execute(null,null);
					}
				}).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				});
			}
			try{
			dialog.create().show();
			}catch(BadTokenException e){
//				e.printStackTrace();
				Log.d("NLiveRoid","Returned Sign dialog after finished App");
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	class SignTask extends AsyncTask<String,Void,Boolean>{

		@Override
		protected Boolean doInBackground(String... params) {
//			Log.d("log","SIGN TASK ---- " + params[0] +"  " + params[1]  +" " + commuID  + "  APPLY " + isApply);
			try {
				HttpURLConnection con = (HttpURLConnection)new URL(URLEnum.MOTION+commuID).openConnection();
				con.setRequestProperty("Cookie", sessionid);
				con.setRequestMethod("POST");
				con.setRequestProperty("Referer", URLEnum.MOTION+commuID);
				con.setInstanceFollowRedirects(true);
				con.setDoOutput(true);
				con.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
				String param = "";
				if(isDemand){
					String isMail = params[0].equals("true")? "&notify=on":"";
					param = "mode=commit&title="+URLEncoder.encode(params[1],"UTF-8")+"&comment="+URLEncoder.encode(params[2],"UTF-8")+isMail;
				}else{
					param = "mode=commit&title="+URLEncoder.encode("参加申請","UTF-8")+"&comment=&notify=";
				}
				DataOutputStream out = new DataOutputStream(con.getOutputStream());
				out.write(param.getBytes(),0,param.getBytes().length);
				out.close();
				int responseCode = con.getResponseCode();
				con.disconnect();
				if(responseCode == 200||responseCode == 302){
					return true;
				}else{
					return false;
				}
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return false;
		}
		@Override
		protected void onPostExecute(Boolean arg){
			if(arg){
				if(isDemand){
				MyToast.customToastShow(context, "参加申請を送信しました");
				}else{
				MyToast.customToastShow(context, "コミュニティに参加しました");
				}
			}else{
				MyToast.customToastShow(context, "参加失敗しました");
			}
		}
	}
	class ResignTask extends AsyncTask<Void,Void,Void>{
		private String errorMessage = "";
		@Override
		protected Void doInBackground(Void... params) {
			try {
				HttpURLConnection con = (HttpURLConnection)new URL(URLEnum.LEAVE+commuID).openConnection();
				con.setRequestProperty("Cookie", sessionid);//sessionidはコミュタスクのスコープにした
				InputStream is = con.getInputStream();
				if(con.getResponseCode() == 406){
					errorMessage = "退会に失敗\n混み合っています";
					return null;
				}
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				int size = 0;
				byte[] tmp = new byte[1024];
				String source = "";
				while ((size = is.read(tmp)) != -1) {
					bos.write(tmp, 0, size);
				}
				source = new String(bos.toByteArray(),"UTF-8");;
				Log.d("CommInfoT","Source--" + source);
				String result = "";
				//絶対に改行があると仮定
				Matcher mc = Pattern .compile("<form.*action=\"/leave/(.*\n){0,10}.*</form>").matcher(source);
				if(mc.find()){
					result = mc.group().replaceAll("\n|\t", "");
//					System.out.println("RESUTL == " + result);
				}
				String[] values = new String[2];
				Matcher mc1 = Pattern .compile("time\".*value=\"[0-9]+\"").matcher(result);
				if(mc1.find()){
					values[0] = mc1.group().replaceAll("\"|time|value|=| |　|\n", "");
					System.out.println("TIME == " + values[0]);
				}
				Matcher mc2 = Pattern .compile("commit_key\".*value=\".+\"").matcher(source);
				if(mc2.find()){
					values[1] = mc2.group().replaceAll("\"|commit_key|value|=| |　|\n", "");
					System.out.println("COMMIT_KEY == " + values[1]);
				}
				if(values[0] == null||values[1] == null){
					errorMessage = "退会に失敗\n情報取得失敗";
					return null;
				}
				con.disconnect();
				HttpURLConnection con2 = (HttpURLConnection)new URL(URLEnum.LEAVE+commuID).openConnection();
				con2.setRequestProperty("Cookie", sessionid);//sessionidはコミュタスクのスコープにした
				con2.setRequestMethod("POST");
				con2.setRequestProperty("Referer", URLEnum.LEAVE+commuID);
				con2.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
				con2.setInstanceFollowRedirects(true);
				con2.setAllowUserInteraction(true);
				con2.setDoOutput(true);

				String param = "time="+values[0]+"&commit_key="+values[1]+"&commit="+URLEncoder.encode("はい、退会します","UTF-8");
				DataOutputStream out = new DataOutputStream(con2.getOutputStream());
				out.write(param.getBytes(),0,param.getBytes().length);
				out.close();

//				Map<String, List<String>> headers = con.getHeaderFields();
//				Iterator headerIt = headers.keySet().iterator();
//				while (headerIt.hasNext()) {
//					String headerKey = (String) headerIt.next();
//					 System.out.println("KEY ------ " + headerKey);
//					 System.out.println("VALUE------------"+headers.get(headerKey));
//				}

//				 System.out.println("RES ------ " + con2.getResponseCode());
				 int responseCode = con2.getResponseCode();
				 if(responseCode !=200 &&responseCode != 302){
						errorMessage = "退会に失敗しました\n接続エラー";
						con2.disconnect();
				 }else{
				con2.disconnect();
				HttpURLConnection con3 = (HttpURLConnection)new URL(URLEnum.LEAVEDONE).openConnection();
				con3.setRequestProperty("Cookie", sessionid);//sessionidはコミュタスクのスコープにした
				con3.setRequestMethod("POST");
				con3.setRequestProperty("Referer", URLEnum.LEAVE+commuID);
				con3.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
				con3.setInstanceFollowRedirects(true);
				if(con3.getResponseCode() == 200){
					con3.disconnect();
					return null;//成功
				}else{
					con3.disconnect();
					errorMessage = "退会に失敗した可能性がある\n接続エラー";
					return null;
				}

				 }
			} catch(FileNotFoundException e){//参加申請中の場合，最初のcon.getInputStream()でこのExceptionになる
				errorMessage = "退会に失敗しました\n参加中コミュが50を超えているか、コミュ参加申請中の為、ブラウザ等から処理を行ってください";
//				e.printStackTrace();
			}catch (MalformedURLException e) {
//				e.printStackTrace();
				errorMessage = "退会に失敗しました\n仕様がかわったかもしれません)";
			} catch (IOException e) {
//				e.printStackTrace();
				errorMessage = "退会に失敗しました\nIOエラー";
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void arg){
			if(errorMessage != null && !errorMessage.equals("")){
				MyToast.customToastShow(context, errorMessage);
			}else{
				MyToast.customToastShow(context, "退会しました");
			}
		}

	}

}
