package nliveroid.nlr.main;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class EnableBCDialog extends DialogPreference{

	private TextView tv;
//	private EditText et;
	private Context context;
	private boolean isToEnable;
	private final String fileName = "temp";
	 public EnableBCDialog(Context context, AttributeSet attrs) {
	 super(context, attrs);
	 this.context = context;
	 }


	 @Override
	 protected View onCreateDialogView() {
		super.onCreateDialogView();
		View parent = LayoutInflater.from(context).inflate(R.layout.enablebc,null);
		try{
		 isToEnable = Boolean.parseBoolean(((NLiveRoid)context.getApplicationContext()).getDetailsMapValue("enable_bc"));
		}catch(Exception e){
			e.printStackTrace();
			 tv.setText("処理に失敗しました");
		}
		 tv = (TextView)parent.findViewById(R.id.enablebc_text);

		 if(!isToEnable){
			 tv.setText("配信タブを有効にする(再起後有効)");
		 }else{
			 tv.setText("配信タブを無くす(再起後有効)");
		 }
		 return parent;
	 }

	 @Override
	 protected void onDialogClosed(boolean positiveResult) {
	 super.onDialogClosed(positiveResult);
	 if(positiveResult){
		 	//認証部分
		 if(!isToEnable){
			 //規約ダイアログ
			 AlertDialog.Builder dialog = new AlertDialog.Builder(
						context);
				dialog
						.setMessage("※人柱専用です。\n" +
								"よろしいですか?");
//								+ "このアプリケーションの予期しないエラーで、放送が終了、又は意図しない動作を\n"
//								+ "する可能性があります。\n"
//								+ "製作者はその場合を含めた、このアプリケーションを利用したことによる、いかなる\n"
//								+ "損害の責任を負わない事、及びニコニコ生放送の配信の規約に同意したものと見なします\n"
//								+ "利用は全て自己責任であることに同意できる場合のみ、OKをタップしてください。」");

				dialog.setPositiveButton("OK",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								 try{
									 ((NLiveRoid)context.getApplicationContext()).setDetailsMapValue("enable_bc", "true");
									 }catch(Exception e){
										 e.printStackTrace();
										 MyToast.customToastShow(context, "処理に失敗しました");
									 }
							}
						});
				dialog.setNegativeButton("CANCEL",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
							}
						}).setCancelable(false).create().show();
		 }else{
				 try{
				 ((NLiveRoid)context.getApplicationContext()).setDetailsMapValue("enable_bc", "false");
				 }catch(Exception e){
					 e.printStackTrace();
					 MyToast.customToastShow(context, "処理に失敗しました");
				 }
			 }
		 }
	 }

	 /**
	  * パスワード認証する
	  */

//	 class AuthTask extends AsyncTask<Void,Void,Integer>{
//
//		@Override
//		protected Integer doInBackground(Void... params) {
//			Random rand = new Random();
//			String result = "";
//			URL url = null;
//			for(int i = 0; i < et.getText().toString().length(); i++){
//				result += et.getText().toString().codePointAt(i);
//			}
////			Log.d("Log","CODE POINT --- " + result);
//			try{
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
//			con.setRequestProperty("Cookie", "nlive.pass="+result);
//			String isOK = con.getHeaderField("Set-Cookie");
//			Log.d("log"," - " + isOK);
//			if(isOK.equals("nlive.pass=OK")){
//				//ファイルに保存しておく
//				try {
//					Context mContext = context.createPackageContext("nliveroid.nlr.main", context.CONTEXT_RESTRICTED);
//					FileOutputStream fos = mContext.openFileOutput(fileName, context.MODE_PRIVATE);
//					ObjectOutputStream oos = new ObjectOutputStream(fos);//ファイルができる
//				} catch (FileNotFoundException e1) {
//					e1.printStackTrace();
//				} catch (IOException e1) {
//					e1.printStackTrace();
//				} catch (NameNotFoundException e1) {
//					e1.printStackTrace();
//				}
//				SerializeMap map = new SerializeMap();
//				map.put("nlive.pass",result);
//				Context mContext = context.createPackageContext("nliveroid.nlr.main", context.CONTEXT_RESTRICTED);
//		ObjectOutputStream oos = new ObjectOutputStream(mContext.openFileOutput(fileName, context.MODE_PRIVATE));
//			oos.writeObject(map);
//			oos.close();
//				return 0;
//			}else{
//				return -2;
//			}
//			} catch (MalformedURLException e) {
//				e.printStackTrace();
//				return -1;
//			} catch (IOException e) {
//				e.printStackTrace();
//				return -1;
//			} catch (NameNotFoundException e) {
//				e.printStackTrace();
//				return -1;
//			}
//		}
//		@Override
//		protected void onPostExecute(Integer arg){
//			if(arg == -2){
//				MyToast.customToastShow(context, "パスワードが違います");
//			}else if(arg == 0){
//				MyToast.customToastShow(context, "配信タブ有効になりました。\n再起動してください");
//			}else if(arg == -1){
//				MyToast.customToastShow(context, "ファイル読み込み又は通信障害エラー");
//			}
//		}
//	 }

}