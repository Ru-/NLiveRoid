package nliveroid.nlr.main;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TableRow;
import android.widget.TextView;




/**
 * いろいろと実装許容
 * 文字数で140超えたら""を返してるけど、確定との関係でInputFilterで綺麗に文字数制限できない
 * しかもInputFilterを使うと、XMLでMaxLength指定が無意味化する+ソース側でやる術がみつからない
 * チェックはなるべくわかりやすい動作にしたが、不整合が生じる場合はある
 * レイアウトもクリアボタン文字サイズを10固定にしてる
 * @author Owner
 *
 */
public class TweetDialog extends AlertDialog{
	private AlertDialog me;
	private String tweet_token;
	private TextView charCount;
	private InputFilter[] filters;
	private EditText et;
	private Context context;
	public TweetDialog(final Context postable,final LiveInfo liveInfo, String token,boolean isDescription) {
		super(postable);
		this.tweet_token = token;
		me = this;
		context = postable;
		LayoutInflater inflater = LayoutInflater.from((Context)postable);
		View parent = inflater.inflate(R.layout.tweetdialog, null);
		setView(parent);
		charCount = (TextView)parent.findViewById(R.id.tweet_char_count);
		et = (EditText)parent.findViewById(R.id.tweet_et);
		et.setLayoutParams(new TableRow.LayoutParams((int) (postable.getResources().getDisplayMetrics().widthPixels * 0.7),-1));
		String defaultValue = "";
		if(isDescription){
			defaultValue = liveInfo.getTitle() + " "+ URLEnum.SP_WATCHBASEURL+liveInfo.getLiveID() +" #"+liveInfo.getLiveID()  +" #nicolive";
			et.setText(defaultValue);
		}else{
			defaultValue = "【ニコ生視聴中】" + liveInfo.getTitle() + " " + URLEnum.SP_WATCHBASEURL+liveInfo.getLiveID();
		et.setText(defaultValue);
		}
		filters =new InputFilter[] { new MyFilter() };
		et.setFilters(filters);
		charCount.setText(String.valueOf(defaultValue.length()));
		final CheckBox tweet_watching = (CheckBox)parent.findViewById(R.id.tweet_watching);
		tweet_watching.setChecked(true);
		tweet_watching.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				if(arg1){
					String tex = et.getText().toString();
					et.setText(tex + " " + "【ニコ生視聴中】");
				}else{
					String tex = et.getText().toString();
					tex = tex.replaceAll("【ニコ生視聴中】", "");
					et.setText(tex);
				}
				charCount.setText(String.valueOf(140-et.getText().length()));
			}
		});
		final CheckBox tweet_title = (CheckBox)parent.findViewById(R.id.tweet_title);
		tweet_title.setChecked(true);
		tweet_title.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				if(arg1){
					if(liveInfo == null)return;
					String tex = et.getText().toString();
					et.setText(tex + " " + liveInfo.getTitle());
				}else{
					String tex = et.getText().toString();
					tex = tex.replaceAll(liveInfo.getTitle(), "");
					et.setText(tex);
				}
				charCount.setText(String.valueOf(140-et.getText().length()));
			}
		});
		final CheckBox tweet_url = (CheckBox)parent.findViewById(R.id.tweet_url);
		tweet_url.setChecked(true);
		tweet_url.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				if(arg1){
					if(liveInfo == null)return;
					String tex = et.getText().toString();
					et.setText(tex + " " + URLEnum.SP_WATCHBASEURL + liveInfo.getLiveID());
				}else{
					String tex = et.getText().toString();
					tex = tex.replaceAll(URLEnum.SP_WATCHBASEURL + liveInfo.getLiveID(), "");
					et.setText(tex);
				}
				charCount.setText(String.valueOf(140-et.getText().length()));
			}
		});
		final CheckBox tweet_co = (CheckBox)parent.findViewById(R.id.tweet_co);
		tweet_co.setChecked(true);
		tweet_co.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				if(liveInfo == null)return;
				if(liveInfo.getCommunityID().contains(URLEnum.HYPHEN)){
					MyToast.customToastShow((Context)postable, "co取得失敗");
					return;
				}
				if(arg1){
				String tex = et.getText().toString();
				et.setText(tex + " #" + liveInfo.getCommunityID());
				}else{
					String tex = et.getText().toString();
					tex = tex.replaceAll("#" + liveInfo.getCommunityID(), "");
					et.setText(tex);
				}
				charCount.setText(String.valueOf(140-et.getText().length()));
			}
		});
		Button tweet_clear = (Button)parent.findViewById(R.id.tweet_clear);
		tweet_clear.setTextSize(10);
		tweet_clear.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				et.setText("");
				tweet_watching.setChecked(false);
				tweet_title.setChecked(false);
				tweet_url.setChecked(false);
				tweet_co.setChecked(false);
				charCount.setText(String.valueOf(140-et.getText().length()));
			}
		});
		Button tweet_ok = (Button)parent.findViewById(R.id.tweet_ok);
		tweet_ok.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if(et.getText().toString().length() > 140){
					MyToast.customToastShow(postable, "文字数オーバー");
					return;
				}
				me.cancel();
				new AsyncTask<Void,Void,Void>(){
					@Override
					protected Void doInBackground(Void... params) {
						postTweet(postable,et.getText().toString());
						return null;
					}
				}.execute();

			}
		});

		Button tweet_cancel = (Button)parent.findViewById(R.id.tweet_cancel);
		tweet_cancel.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if(me != null)me.cancel();
			}
		});
	}

	class MyFilter  implements InputFilter {
        public CharSequence filter(CharSequence source, int start, int end,
                Spanned dest, int dstart, int dend) {
        	charCount.setText(String.valueOf(140 - dest.length()));
        	if(dest.length() > 140)return "";
        	return source;
			}
		}
	public void showSelf(){
		this.show();
	}

	private void postTweet(final Context postable,String tweet){
		if(tweet_token == null){
			((Activity)postable).runOnUiThread(new Runnable(){
				@Override
				public void run() {
					MyToast.customToastShow((Context)postable, "Tweet失敗");
				}
			});
			return;
		}
		String[] tweetToken = tweet_token.split(" ");
		if(tweetToken.length < 2 ){
			((Activity)postable).runOnUiThread(new Runnable(){
				@Override
				public void run() {
					MyToast.customToastShow((Context)postable, "Twitter認証できていませんでした");
				}
			});
			return;
		}
		//twitterオブジェクトの作成
		Twitter tw = new TwitterFactory().getInstance();

		//AccessTokenオブジェクトの作成
		AccessToken at = new AccessToken(tweetToken[0], tweetToken[1]);
		//Consumer keyとConsumer key seacretの設定
		tw.setOAuthConsumer("pNxWQdK6hY3AUk9nbEMLQ", "FE4hdC1vW4PkDjJJdBl5bkxx0rm7CSGzeOkTk5rItk");
		//AccessTokenオブジェクトを設定
		tw.setOAuthAccessToken(at);
		try {
		    tw.updateStatus(tweet);
		} catch (TwitterException e) {
		    e.printStackTrace();
		    if(e.isCausedByNetworkIssue()){
				((Activity)postable).runOnUiThread(new Runnable(){
					@Override
					public void run() {
		         MyToast.customToastShow((Context)postable, "Twitter接続エラー");
					}
				});
		         return;
		    }
		}((Activity)postable).runOnUiThread(new Runnable(){
			@Override
			public void run() {
        MyToast.customToastShow((Context)postable, "Tweetしました");
			}
		});
	}


	public void onConfigChanged(Context context){
		this.context = context;
		if(et != null)et.setLayoutParams(new TableRow.LayoutParams((int) (context.getResources().getDisplayMetrics().widthPixels * 0.7),-1));
	}

}
