package nliveroid.nlr.main;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class QuickDialog extends AlertDialog {
	private byte[] settingVal = new byte[2];
	private AlertDialog me;
	protected QuickDialog(final CommentPostable postable,byte[] setting_byte,boolean isAutoUser) {
		super((Context) postable);
		View parent = LayoutInflater.from((Context)postable).inflate(R.layout.quick_dialog, null);
		setView(parent);
		me = this;
		settingVal[0] = setting_byte[40];
		settingVal[1] = setting_byte[41];
		RadioGroup radioGroup = (RadioGroup)parent.findViewById(R.id.quick_radiogroup);
		int checkedResourceID = 0;
		int menuVal = (setting_byte[40] & 0xF0) >> 4;
//		Log.d("NLiveRoid","QuickD " + Integer.toBinaryString(setting_byte[40]));
//		Log.d("NLiveRoid","QuickD  " + menuVal);
		if(menuVal > 2 || menuVal < 0){
			setting_byte[40] = (byte) (setting_byte[40] & 0x0F);//危険なので仕方なくここでチェックしとく
			menuVal = 0;
		}
		switch(menuVal){
		case 0:
			checkedResourceID = R.id.quick_radio_standard;
			break;
		case 1:
			checkedResourceID = R.id.quick_radio_this;
			break;
		case 2:
			checkedResourceID = R.id.quick_radio_post;
			break;
		}
		radioGroup.check(checkedResourceID);
		radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if(checkedId == R.id.quick_radio_standard){
					postable.quickAction(0);
				}else if(checkedId == R.id.quick_radio_this){
					postable.quickAction(1);
				}else if(checkedId == R.id.quick_radio_post){
					postable.quickAction(2);
				}
			}
		});
		//全画面
		Button fullscr = (Button)parent.findViewById(R.id.quick_fullscrn);
		if((settingVal[0] & 0x08) <= 0){
			fullscr.setVisibility(View.GONE);
		}else{
		fullscr.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				postable.quickAction(3);
				me.cancel();//閉じる
			}
		});
		}
		//表示設定
		Button layernum = (Button)parent.findViewById(R.id.quick_layernum);
		if((settingVal[0] & 0x04) <= 0){
			layernum.setVisibility(View.GONE);
		}else{
		layernum.setOnClickListener(new View.OnClickListener(){
				@Override
				public void onClick(View v) {
					me.cancel();
					new AlertDialog.Builder((Context)postable)
					.setItems(new String[]{"前面","背面","プレイヤーのみ"}, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {//コメント欄再起して頑張る
							postable.layerChange(which);
						}
					}).create().show();
			}
		});
		}
		//コメント欄の更新
		Button update = (Button)parent.findViewById(R.id.quick_update_comment);
		if((settingVal[0] & 0x02) <= 0){
			update.setVisibility(View.GONE);
		}else{
		update.setOnClickListener(new View.OnClickListener(){
				@Override
				public void onClick(View v) {
					me.cancel();//閉じる
					postable.updateCommentTable(true);//同期的だが大丈夫か?
				}
		});
		}
		//ログ取得
				Button log = (Button)parent.findViewById(R.id.quick_log);
				if((settingVal[0] & 0x01) <= 0){
					log.setVisibility(View.GONE);
				}else{
				log.setOnClickListener(new View.OnClickListener(){
						@Override
						public void onClick(View v) {
							postable.quickAction(4);
							me.cancel();//閉じる
						}
				});
				}
				//ユーザー名自動
				CheckBox username = (CheckBox)parent.findViewById(R.id.quick_username);
				if((settingVal[1] & 0x40) <= 0){
					username.setVisibility(View.GONE);
				}else{
					 if(isAutoUser)username.setChecked(true);
					 username.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
							@Override
							public void onCheckedChanged(CompoundButton buttonView,
									boolean isChecked) {
								((HandleNamable)postable).setAutoGetUserName(isChecked);
							}
						});
				}
				//184m,kijn
				CheckBox _184 = (CheckBox)parent.findViewById(R.id.quick_184);
				if((settingVal[1] & 0x20) <= 0){
					_184.setVisibility(View.GONE);
				}else{
					String anonym = postable.getCmd().getValue(CommandKey.CMD);
					if(anonym.equals("184")){
						_184.setChecked(true);
					}else{
						_184.setChecked(false);
					}
				_184.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						if(isChecked){
							postable.setCmd(CommandKey.CMD, "184");
						}else{
							postable.setCmd(CommandKey.CMD, "");
						}
					}
				});
				}

				//コマンド
				Button command = (Button)parent.findViewById(R.id.quick_command);
				if((settingVal[1] & 0x10) <= 0){
					command.setVisibility(View.GONE);
				}else{
				command.setOnClickListener(new View.OnClickListener(){
						@Override
						public void onClick(View v) {
							postable.showCommandDialog();
							me.cancel();//閉じる
						}
				});
				}
				//投稿
				Button post = (Button)parent.findViewById(R.id.quick_post);
				if((settingVal[1] & 0x08) <= 0){
					post.setVisibility(View.GONE);
				}else{
					post.setOnClickListener(new View.OnClickListener(){
						@Override
						public void onClick(View v) {
							me.cancel();//閉じる
							postable.showPostArea();
						}
				});
				}
				//Tweet
				Button tweet = (Button)parent.findViewById(R.id.quick_tweet);
				if((settingVal[1] & 0x04) <= 0){
					tweet.setVisibility(View.GONE);
				}else{
				tweet.setOnClickListener(new View.OnClickListener(){
						@Override
						public void onClick(View v) {
							postable.shoeTweetDialog();
							me.cancel();//閉じる
						}
				});
				}
				//設定
				Button setting = (Button)parent.findViewById(R.id.quick_setting);
				if((settingVal[1] & 0x02) <= 0){
					setting.setVisibility(View.GONE);
				}else{
					setting.setOnClickListener(new View.OnClickListener(){
						@Override
						public void onClick(View v) {
							postable.quickAction(5);
							me.cancel();//閉じる
						}
				});
				}
				//視聴画面終了
				Button finish = (Button)parent.findViewById(R.id.quick_finish);
				if((settingVal[1] & 0x01) <= 0){
					finish.setVisibility(View.GONE);
				}else{
					finish.setOnClickListener(new View.OnClickListener(){
						@Override
						public void onClick(View v) {
							postable.quickAction(6);
							me.cancel();//閉じる
						}
				});
				}
	}
	@Override
	public boolean dispatchKeyEvent(KeyEvent event){
//		Log.d("DISPATCH"," " + event.getKeyCode());
		if (event.getKeyCode() == KeyEvent.KEYCODE_MENU && event.getAction() == KeyEvent.ACTION_DOWN){
			me.cancel();
			return false;
		}else{
		return super.dispatchKeyEvent(event);
		}
	}

	public void showSelf(byte setting0, byte setting1){
		settingVal[0] = setting0;
		settingVal[1] = setting1;
		//ダイアログが閉じる時に一気に設定値を渡す?
		me.setOnCancelListener(new OnCancelListener(){
			@Override
			public void onCancel(DialogInterface dialog) {//設定値を保存する(渡す)
			}
		});
		this.show();
	}

}
