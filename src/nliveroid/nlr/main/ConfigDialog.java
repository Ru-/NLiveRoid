package nliveroid.nlr.main;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import nliveroid.nlr.main.parser.NsenParser;

import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

/**
 * 新たにダイアログをshowしようとすると、
 * 低レベルでOutOfResourcesException
 * が吐かれてしまうのでとりあえず全て表示前にdissmissしちゃう
 * @author Owner
 *
 */


public class ConfigDialog extends AlertDialog.Builder{
	private CommentPostable postable;
	private AlertDialog me;
	public ConfigDialog(final CommentPostable postable,final LiveInfo liveinfo,final byte[] setting_byte,final boolean[] setting_boolean,final byte init_mode) {
		super((Context)postable);
		if(NLiveRoid.isDebugMode)Log.d("NLiveRoid","CONFIG --- " +  postable +"  " + liveinfo  +"  " + init_mode);

		this.postable = postable;
		View parent = LayoutInflater.from((Context)postable).inflate(R.layout.config_dialog, null);
		setView(parent);

		final Button player_setting = (Button)parent.findViewById(R.id.config_player_setting);
		Button comment_setting = (Button) parent.findViewById(R.id.config_comment_setting);
		if(init_mode == 2){
			comment_setting.setEnabled(false);
		}else if(init_mode == 3){
			player_setting.setEnabled(false);
		}
		Button other = (Button)parent.findViewById(R.id.config_other);
		if(init_mode == 4){//配信の時はPlayer更新非表示
			player_setting.setText("配信設定");
			player_setting.setOnClickListener(new View.OnClickListener(){
				@Override
				public void onClick(View arg0) {
					me.cancel();
					new LiveSettingDialog((BCPlayer)postable,((BCPlayer)postable).getLiveSettings()).showSelf();
				}
			});
		}else{
		//Player設定
		player_setting.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View arg0) {
				me.cancel();
				postable.setSpPlayerOperation((byte)-1,(byte)-1);
			}
		});
		}


		//コメ欄の設定
		comment_setting.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View arg0) {
				me.cancel();
				byte isCanLog = 0;//0できない、1一般、2プレミアム
				//ログ取得はコミュが対応していて、プレミアムじゃないと駄目
				if(liveinfo.getDefaultCommunity()==null||liveinfo.getDefaultCommunity().equals("")
						||liveinfo.getDefaultCommunity().contains("ch")){
				}else{
					if(liveinfo.getIsPremium().equals("0")){
					isCanLog = 1;
					}else{
					isCanLog = 2;
					}
				}
				new ConfigCommentDialog(postable,isCanLog,setting_byte,setting_boolean,init_mode).showSelf();
			}
		});

		other.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View arg0) {
				me.cancel();
				new ConfigOtherDialog(postable,setting_byte[37],init_mode).showSelf();
			}//End of onTouch
		});

		this.setOnCancelListener(new OnCancelListener(){
			@Override
			public void onCancel(DialogInterface arg0) {
				if(postable == null)return;
				//OverLayの時はfinish時ブロキャスしないのでマップにt直接セットする
				if(init_mode == 0){
				NLiveRoid app = (NLiveRoid)((Context)postable).getApplicationContext();
				//コマンドをファイルに保存する
				//Flashから呼ばれた場合、マップだけでは保存できない?
				CommandMapping cm = postable.getCmd();
				app.setDetailsMapValue("cmd_cmd", cm.getValue(CommandKey.CMD));
				app.setDetailsMapValue("cmd_size",cm.getValue(CommandKey.Size));
				app.setDetailsMapValue("cmd_color",cm.getValue(CommandKey.Color));
				app.setDetailsMapValue("cmd_align",cm.getValue(CommandKey.Align));
				}
			}
		});

	}//End of constractor

	public void showSelf(){
		this.create();
		me = this.show();
	}

}
