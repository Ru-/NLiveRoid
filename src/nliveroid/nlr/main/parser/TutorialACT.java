package nliveroid.nlr.main.parser;

import nliveroid.nlr.main.CommunityInfoTask;
import nliveroid.nlr.main.NLiveRoid;
import nliveroid.nlr.main.PrimitiveSetting;
import nliveroid.nlr.main.R;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class TutorialACT extends Activity{
	private WebView wv;
	private TutorialACT ACT;
	@Override
	public void onCreate(Bundle bundle){
		super.onCreate(bundle);
		ACT = this;
		requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
		LayoutInflater inflater = LayoutInflater.from(this);
		View parent = inflater.inflate(R.layout.tutorial, null);
		wv = (WebView) parent.findViewById(R.id.tutorial_wv);
		setContentView(parent);
		WebSettings settings = wv.getSettings();
		settings.setJavaScriptEnabled(true);
		settings.setPluginsEnabled(true);
		settings.setUserAgent(1);
		wv.setWebViewClient(new TutorialClient());
		wv.loadUrl("http://nliveroid-tutorial.appspot.com/");
	}


	class TutorialClient extends WebViewClient{
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			if(url == null || url.equals(""))return false;
			if(url.contains("/community/co")){
				NLiveRoid app = (NLiveRoid)ACT.getApplicationContext();
						if(app != null&&app.getGateView()!=null){
							String session = null;
							if(PrimitiveSetting.getACT()!=null){
								session = PrimitiveSetting.getACT().getSessionTutorial();
							}
							if(session != null){
				new CommunityInfoTask(ACT,"co395273",session,app.getGateView().getWidth()).execute();
				return true;
							}
						}
						//コミュ参加駄目ならブラウザ
			Uri uri = Uri.parse(url);
			Intent i = new Intent(Intent.ACTION_VIEW);
			i.addCategory(Intent.CATEGORY_BROWSABLE);
			i.setDataAndType(uri, "text/html");
			startActivity(i);//とりあえず何も返却値とらない
			}else if(url.contains("hotmail.co.jp")){//メール
				try{
				Intent it = new Intent();
				it.setAction(Intent.ACTION_SENDTO);
				it.setData(Uri.parse("mailto:" + "ru-apps@hotmail.co.jp"));
//				it.putExtra(Intent.EXTRA_SUBJECT, );
//				it.putExtra(Intent.EXTRA_TEXT, );
				startActivity(it);
				}catch(ActivityNotFoundException e){
					e.printStackTrace();
					callBrowser(url);
				}
			}else if(url.contains("twitter.com")){//Twitter
				try {
	                // メーラーやtwitterクライアントなどを呼び出す
	                Intent intent = new Intent();
	                intent.setAction(Intent.ACTION_SEND);
	                intent.setType("text/plain");
	                intent.putExtra(Intent.EXTRA_TEXT, "@ru_apps ");
	                startActivity(intent);
	            } catch (ActivityNotFoundException e) {
	                e.printStackTrace();
	                // 呼び出せるActivityが存在しない
	                callBrowser(url);
	             }

			}else{
				callBrowser(url);
			}
            return true ;
     }

		private void callBrowser(String url){
			Uri uri = Uri.parse(url);
			Intent i = new Intent(Intent.ACTION_VIEW);
			i.addCategory(Intent.CATEGORY_BROWSABLE);
			i.setDataAndType(uri, "text/html");
			startActivity(i);
		}
	}
}
