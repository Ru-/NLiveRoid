package nliveroid.nlr.main;

import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;
import twitter4j.auth.OAuthAuthorization;
import twitter4j.auth.RequestToken;
import twitter4j.conf.ConfigurationContext;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.FrameLayout;

public class TwitterAccount extends Activity {
    public static RequestToken _req = null;
    public static OAuthAuthorization _oauth = null;
    private boolean finishOK;
    private ProgressDialog p;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		FrameLayout parent = new FrameLayout(this);
		parent.setBackgroundColor(Color.parseColor("#00000000"));
        setContentView(parent);
        final Activity me = this;
        //Twitterの認証画面から発行されるIntentからUriを取得
        Uri uri = getIntent().getData();//初回は必ずnull
    	AccessToken token = null;
    	if(NLiveRoid.isDebugMode)Log.d("URL ", " URLAAAAAAAAAAAA " + uri);
    	//戻ってきた時の処理
	    if(uri != null && (uri.toString().startsWith("http://nliveroid-tutorial.appspot.com/")
	    		||uri.toString().startsWith("https://nliveroid-tutorial.appspot.com/")
	    		||uri.toString().startsWith("callback://nliveroid-tutorial.appspot.com")
	    		||uri.toString().startsWith("Callback://nliveroid-tutorial.appspot.com"))){
	            //oauth_verifierを取得する
	            String verifier = uri.getQueryParameter("oauth_verifier");
	            try {
	                //AccessTokenオブジェクトを取得
	            	if(verifier == null){
	             		MyToast.customToastShow(this, "Twitter認証に失敗しました:05");
	             		Intent topTab = new Intent(this,TopTabs.class);
			    		startActivity(topTab);
	             		finish(true);
	             		return;
	            	}
	                token = _oauth.getOAuthAccessToken(_req, verifier);
	            } catch (TwitterException e) {
	                e.printStackTrace();
            		MyToast.customToastShow(this, "HOMEキー又は起動履歴からNLiveRoidへ御戻り下さい");
            		finish();
            		return;
	            }
		        if(token != null){
		        NLiveRoid app = (NLiveRoid)getApplicationContext();
		        if(app == null||app.getDefaultMap() == null ){
		        	MyToast.customToastShow(this,"設定の保存に失敗");
		        	if(PrimitiveSetting.getACT()!= null)PrimitiveSetting.getACT().setTwitterSummary(false);
		        }else {//暗号化して保存
		        	app.getDefaultMap().put("twitter_token", token.getToken());
		        	app.getDefaultMap().put("twitter_secret", token.getTokenSecret());
		        	app.updateAccountFile();
		        	if(PrimitiveSetting.getACT()!= null)PrimitiveSetting.getACT().setTwitterSummary(true);
		        	MyToast.customToastShow(this, "Twitter認証成功\nHOMEキー又は起動履歴からNLiveRoidに御戻り下さい");
		        	finish(true);
		        }
		        }else{
		        	MyToast.customToastShow(this, "Twitter認証に失敗しました:01");
		        	if(PrimitiveSetting.getACT()!= null)PrimitiveSetting.getACT().setTwitterSummary(false);
		        	finish();
		        }
        }else{//既に設定されている||初回の処理
        	final NLiveRoid app = (NLiveRoid)getApplicationContext();
        	if (app.getDefaultMap() != null && app.getDefaultMap().get("twitter_token") != null
        			&&!app.getDefaultMap().get("twitter_token").equals("null")
					&&app.getDefaultMap().get("twitter_token").replaceAll("<<T_SPLIT>>", "").length()>2) {//既に設定されている
        		//既に設定されている場合削除するか?
        		new AlertDialog.Builder(this)
        		.setMessage("このアプリに保存されたTwitter認証情報を削除しますか?\n完全に連携を解除するにはここで情報削除後、\nブラウザ等でTwitter側のNLiveRoidの連携を解除して下さい")
        		.setPositiveButton("YES", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						app.getDefaultMap().put("twitter_token", null);
						app.getDefaultMap().put("twitter_secret", null);
						finish(true);
					}
				}).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						finish();
					}
				}).setOnCancelListener(new DialogInterface.OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {
						finish();
					}
				}).create().show();
        		return;
        	}
    		p = new ProgressDialog(this);
    		p.setMessage("ブラウザを起動します");
    		p.show();
        	final Activity ACT = this;
        	new AsyncTask<Void,Void,Integer>(){
				@Override
				protected Integer doInBackground(Void... params) {
					//Twitetr4Jの設定を読み込む
		            twitter4j.conf.Configuration conf = ConfigurationContext.getInstance();
		            //Oauth認証オブジェクト作成
		            _oauth = new OAuthAuthorization(conf);
		            //Oauth認証オブジェクトにconsumerKeyとconsumerSecretを設定
		            _oauth.setOAuthConsumer("pNxWQdK6hY3AUk9nbEMLQ", "FE4hdC1vW4PkDjJJdBl5bkxx0rm7CSGzeOkTk5rItk");
		            _oauth.setOAuthAccessToken(null); // これをやらないと下記getOAuthRequestToken()で例外が発生する
		            //アプリの認証オブジェクト作成
		            try {
		                _req = _oauth.getOAuthRequestToken("Callback://nliveroid-tutorial.appspot.com/");
		            } catch (TwitterException e) {
		                e.printStackTrace();
		                return -1;
		            }
		            if(_req != null){
		            String _uri;
		            _uri = _req.getAuthorizationURL();
		            if(NLiveRoid.isDebugMode)Log.d("NLiveRoid","_req URL" + _uri);
		            startActivityForResult(new Intent(Intent.ACTION_VIEW , Uri.parse(_uri)), CODE.REQUEST_TWITTERBROWSER);
		            }else{
			        	return -2;
		            }

					return 0;
				}
				@Override
				protected void onPostExecute(Integer arg){
					if(arg == -1){
		        	MyToast.customToastShow(ACT, "Twitter認証に失敗しました:02");
		        	if(PrimitiveSetting.getACT()!= null)PrimitiveSetting.getACT().setTwitterSummary(false);
		        	finish();
					}else if(arg == -2){
					MyToast.customToastShow(ACT, "Twitter認証に失敗しました:03");
		        	if(PrimitiveSetting.getACT()!= null)PrimitiveSetting.getACT().setTwitterSummary(false);
		        	finish();
					}else if(arg == 0){
					finishOK = true;
					finish();
					}
				}
        	}.execute();
        	}
    }
	@Override
	public void onResume(){
		super.onResume();
		if(finishOK)finish();
	}
	public void finish(boolean isPrimitiveFinish){
		if(isPrimitiveFinish&&PrimitiveSetting.getACT() != null)PrimitiveSetting.getACT().finish();//TopTabsが蹴られる
		super.finish();
	}
	@Override
	public void onWindowFocusChanged(boolean isFocus){
		if(p != null && p.isShowing()){
			p.cancel();
		}
	}
}
