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
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class ConfigPlayerDialog extends AlertDialog.Builder{
	private AlertDialog me;
	protected ConfigPlayerDialog(final CommentPostable postable,final byte[] setting_byte,final boolean[] setting_boolean){
		super((Context)postable);
		View parent = LayoutInflater.from((Context)postable).inflate(R.layout.config_player_dialog, null);
		setView(parent);

		final Button player_change = (Button)parent.findViewById(R.id.config_playerchange);
		final CheckBox player_comment_disable = (CheckBox)parent.findViewById(R.id.config_comment_disable);
		final CheckBox player_184_disable = (CheckBox)parent.findViewById(R.id.config_184_disable);
		final CheckBox player_184_disable_bsp = (CheckBox)parent.findViewById(R.id.config_184_disable_bsp_enable);
		final CheckBox player_mute = (CheckBox)parent.findViewById(R.id.config_mute);
		final CheckBox player_command_movie = (CheckBox)parent.findViewById(R.id.config_playmovie);
		final SeekBar subvolume = (SeekBar)parent.findViewById(R.id.config_subvolume);
		final CheckBox is_fullscreen = (CheckBox)parent.findViewById(R.id.config_fullscreen);

		final Button player_position = (Button)parent.findViewById(R.id.config_playerposition);


		//項目の有効無効を設定する
		switch(setting_byte[31]){
		case 3://コメントのみ
			player_change.setEnabled(false);
			break;
		}

		if(setting_byte[43] == 0){
			player_comment_disable.setChecked(!setting_boolean[14]);//[14]はshowcommentだからtrueなら非表示はfalse
			player_184_disable.setChecked(setting_boolean[15]);
			player_184_disable_bsp.setChecked(setting_boolean[16]);
			player_mute.setChecked(setting_boolean[17]);
			player_command_movie.setChecked(setting_boolean[18]);
			subvolume.setProgress(setting_byte[30]);
			is_fullscreen.setChecked(setting_boolean[21]);
		}else{//PC版
			if(setting_byte[43]==2)player_change.setVisibility(View.GONE);
			player_comment_disable.setVisibility(View.GONE);
			player_184_disable.setVisibility(View.GONE);
			player_184_disable_bsp.setVisibility(View.GONE);
			player_mute.setVisibility(View.GONE);
			player_command_movie.setVisibility(View.GONE);
			subvolume.setVisibility(View.GONE);
			TextView subvol_text = (TextView)parent.findViewById(R.id.config_player_subtext);
			subvol_text.setVisibility(View.GONE);
		}
		player_change.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				me.cancel();
						if(setting_byte[43] == 2){
							MyToast.customToastShow((Context)postable, "HLSは切り替えに対応させていません");
							return;
						}
						postable.setSpPlayerOperation((byte)10,(byte)-1);
			}
		});

		if(postable.isNsen()){
			Button nsen = (Button)parent.findViewById(R.id.config_nsenchange);
			nsen.setVisibility(View.VISIBLE);
			nsen.setOnClickListener(new View.OnClickListener(){
					@Override
					public void onClick(View arg0) {
						me.cancel();
						new AlertDialog.Builder((Context)postable)
						.setItems(new CharSequence[]{"VOCALOID","東方","ニコニコインディーズ","歌ってみた","演奏してみた","PV","蛍の光","オールジャンル"}, new DialogInterface.OnClickListener(){
							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								String extension = "";
								switch(arg1){
								case 0:
									extension = "vocaloid";
									break;
								case 1:
									extension = "toho";
									break;
								case 2:
									extension = "nicoindies";
									break;
								case 3:
									extension = "sing";
									break;
								case 4:
									extension = "play";
									break;
								case 5:
									extension = "pv";
									break;
								case 6:
									extension = "hotaru";
									break;
								case 7:
									extension = "allgenre";
									break;
								}
								new GetNsenLVTask(postable).execute(URLEnum.NSENURL + extension);
							}
						}).create().show();
					}
			});
		}
		player_position.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				final boolean isPortLayt = me.getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
				CharSequence[] items;
				if(isPortLayt){
					items = new CharSequence[]{"上","下"};
				}else{
					if(setting_boolean[4]){
						items = new CharSequence[]{"左","右","全面"};
					}else{
						items = new CharSequence[]{"左","右"};
					}
				}
				new AlertDialog.Builder((Context)postable)
				.setItems(items, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						me.cancel();
						if(isPortLayt){
							setting_byte[22] = (byte)which;
						}else{
							setting_byte[23] = (byte)which;
						}
						postable.settingChange(11, (byte) -1 , setting_byte);
						}
					}).create().show();
			}
		});
		is_fullscreen.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				postable.setFullScreen(isChecked);
			}
		});

		player_comment_disable.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				postable.setSpPlayerOperation((byte)0,isChecked? (byte)1:(byte)0);
			}
		});
		player_184_disable.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				postable.setSpPlayerOperation((byte)1,isChecked? (byte)1:(byte)0);
			}
		});
		player_184_disable_bsp.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				postable.setSpPlayerOperation((byte)2,isChecked? (byte)1:(byte)0);
			}
		});
		player_mute.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				postable.setSpPlayerOperation((byte)3,isChecked? (byte)1:(byte)0);
			}
		});
		player_command_movie.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				postable.setSpPlayerOperation((byte)4,isChecked? (byte)1:(byte)0);
			}
		});

		subvolume.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){

			@Override
			public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
				postable.setSpPlayerOperation((byte)5, (byte)arg1);
			}
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO 自動生成されたメソッド・スタブ
			}
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO 自動生成されたメソッド・スタブ
			}
		});
	}

	public void showSelf(){
		this.create();
		me = this.show();
	}


	class GetNsenLVTask extends AsyncTask<String,Void,Void> implements FinishCallBacks{
		ErrorCode error;
		String lv;
		private boolean ENDFLAG = true;
		private CommentPostable postable;
		protected GetNsenLVTask(CommentPostable postable){
			this.postable = postable;
		}
		@Override
		protected Void doInBackground(String... arg) {
			error = ((NLiveRoid)((Context)postable).getApplicationContext()).getError();
			try {
				URL url = new URL(arg[0]);
				HttpURLConnection con = (HttpURLConnection) url.openConnection();
				InputStream response = con.getInputStream();
				if(response == null){
					error.setErrorCode(-27);
					return null;
				}else if(error.getErrorCode() == -7) {
					// ここで-7でメンテか別の失敗かわからない
					return null;
				}
				org.ccil.cowan.tagsoup.Parser parser = new org.ccil.cowan.tagsoup.Parser();
				  ContentHandler sHandler = new NsenParser(this,error);
		        parser.setContentHandler(sHandler);
		        parser.parse(new InputSource(response));
		        long startT = System.currentTimeMillis();
				while(ENDFLAG  ){
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
						ENDFLAG = false;
						break;
					}catch(IllegalArgumentException e1){
						e1.printStackTrace();
						Log.d("NLiveRoid","IllegalArgumentException at SearchingTask");
						ENDFLAG = false;
						break;
					}
					if(System.currentTimeMillis() - startT > 30000){
						error.setErrorCode(-10);
						ENDFLAG = false;
						return null;
					}
				}
			} catch (IOException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			} catch (SAXException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			}
			return null;
		}
		@Override
		protected void onPostExecute(Void arg){
			if(error.getErrorCode() != 0){
				error.showErrorToast();
			}else{
				if(lv == null){
					MyToast.customToastShow((Context)postable, "Nsenの放送IDの取得に失敗しました");
				}else{
				postable.getLiveInfo().setLiveID(lv);
				postable.allUpdate();
				}
			}
		}

		@Override
		public void finishCallBack(ArrayList<LiveInfo> infos) {
			ENDFLAG = false;
			lv = infos.get(0).getLiveID();
		}
		@Override
		public void finishCallBack(ArrayList<LiveInfo> info,
				LinkedHashMap<String, String> generate) {//呼ばれることは無い
		}
		@Override
		public void finishCallBack(ArrayList<LiveInfo> liveInfos, String pager) {
			// TODO 自動生成されたメソッド・スタブ

		}

	}
}
