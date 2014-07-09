package nliveroid.nlr.main;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.flazr.rtmp.client.RealTimeMic;


public class LiveSettingDialog extends AlertDialog.Builder{
	private Dialog me;
	private LiveSettings liveSetting;
	public LiveSettingDialog(final BCPlayer player,final LiveSettings liveSetting) {
		super(player);
		View parent = LayoutInflater.from(player).inflate(R.layout.live_setting_dialog, null);
		this.setView(parent);
		this.liveSetting = liveSetting;
		final String[] modes = new String[]{"プレビューモード","静止画モード","FLV動画再生"};
		final TextView lv_text = (TextView)parent.findViewById(R.id.lv_text);
		lv_text.setText(liveSetting.getStreamName());
		final int mode = liveSetting.getMode();
		boolean isStreaming = liveSetting.isStreamStarted();

		final Button modeChange_bt = (Button)parent.findViewById(R.id.live_mode_bt);
		modeChange_bt.setText(modes[liveSetting.getMode()>=1? liveSetting.getMode()-1:liveSetting.getMode()]);
		if(isStreaming){
			modeChange_bt.setEnabled(false);
		}
		modeChange_bt.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg) {
				me.dismiss();
				new AlertDialog.Builder(player)
				.setTitle(modes[liveSetting.getMode()>=1? liveSetting.getMode()-1:liveSetting.getMode()])
				.setItems(modes,new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,int which) {
								modeChange_bt.setText(modes[which]);
								if(which >= 1)which+=1;//スナップを飛ばしておく
								if(liveSetting.getMode() != which&&liveSetting.isStreamStarted()){//変更の場合ストリームを停止する画面を出してストリームを止める

									new AlertDialog.Builder(player)
									.setMessage("ストリームが切断されますが、よろしいですか?")
									.setPositiveButton("OK", new DialogInterface.OnClickListener() {
										public void onClick(DialogInterface dialog,int which) {
											player.stopStream();
											player.changeMode(which);
										}
									})
									.setNegativeButton("CANCEL",  new DialogInterface.OnClickListener() {
										public void onClick(DialogInterface dialog,int which) {

										}
									}).create().show();
									}else{
								player.changeMode(which);//レイアウトをやり直してからネイティブをやり直す
							}
						}
				}).create().show();
			}
		});

		final CheckBox stream_start = (CheckBox)parent.findViewById(R.id.stream_start_cb);
		stream_start.setChecked(isStreaming);
		stream_start.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if(liveSetting.isStreamStarted()){//先に入れないとpublishでここから同期的にされて入れるタイミングがんない
					if(!isChecked){
						player.stopStream();
					}
				}else{
					if(isChecked){
						player.startStream();
					}
				}
			}
		});

		final Button live_setting_bt = (Button)parent.findViewById(R.id.live_info_setting);
		live_setting_bt.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg) {
				me.dismiss();
				new LiveSettingDialog_Sub(player).showSelf();
			}
		});

		//カメラ使用
		final CheckBox cam_cb = (CheckBox)parent.findViewById(R.id.cam_cb);
		if(mode >= 2){
			cam_cb.setChecked(false);
			cam_cb.setEnabled(false);
		}else{
		cam_cb.setChecked(liveSetting.isUseCam());
		}
//		if(isStreaming){//配信中に設定不可だけどとりあえず押せるように
//			cam_cb.setEnabled(false);
//		}
		cam_cb.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if(isChecked && !liveSetting.isUseCam() ){
					Log.d("NLiveRoid","startPreview_From_LiveSettingDialog");
						player.startPreView();
				}else{
					player.stopPreview();
				}
				liveSetting.setUseCam(isChecked);
			}
		});
		//マイク使用
		final CheckBox mic_cb = (CheckBox)parent.findViewById(R.id.mic_cb);
		if(mode == 3){
			mic_cb.setChecked(false);
			mic_cb.setEnabled(false);
		}else{
		mic_cb.setChecked(liveSetting.isUseMic());
		}
//		if(isStreaming){//配信中に設定不可だけどとりあえず押せるように
//			mic_cb.setEnabled(false);
//		}
		mic_cb.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if(liveSetting.getMode() == 3){
					mic_cb.setChecked(false);
					return;
				}
				if(isChecked && !liveSetting.isUseMic() ){
					RealTimeMic mic = new RealTimeMic(player);
					int value = mic.init(liveSetting);
					if(value < 0){
						MyToast.customToastShow(player, "マイクの初期化に失敗");
					}
				}
				if(liveSetting.isUseMic()){
						if(!isChecked){
							player.stopMic();
						}
					}
					liveSetting.setUseMic(isChecked);
			}
		});
		final Button cam_setting = (Button)parent.findViewById(R.id.cam_setting);
		cam_setting.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg) {
				me.dismiss();
				new LiveSettingDialog_Cam(player).showSelf();
			}
		});
		final Button mic_setting = (Button)parent.findViewById(R.id.mic_setting);
		mic_setting.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg) {
				me.dismiss();
				new LiveSettingDialog_Mic(player).showSelf();
			}
		});
		final CheckBox back_mic_cb = (CheckBox)parent.findViewById(R.id.back_mic_cb);
		back_mic_cb.setChecked(liveSetting.isBackGroundMic());
		back_mic_cb.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				liveSetting.setBackGroundMic(isChecked);
			}
		});
		final CheckBox back_cam_cb = (CheckBox)parent.findViewById(R.id.back_cam_cb);
		back_cam_cb.setChecked(liveSetting.isBackGroundCam());
		back_cam_cb.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				liveSetting.setBackGroundCam(isChecked);
			}
		});
		CheckBox ring_mic_cb = (CheckBox)parent.findViewById(R.id.ring_mic_cb);
		ring_mic_cb.setChecked(liveSetting.isRingMicEnable());
		ring_mic_cb.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				liveSetting.setRingMicEnable(isChecked);
			}
		});
		final CheckBox ring_cam_cb = (CheckBox)parent.findViewById(R.id.ring_cam_cb);
		ring_cam_cb.setChecked(liveSetting.isRingCamEnable());
		ring_cam_cb.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				liveSetting.setRingCamEnable(isChecked);
			}
		});
	}

	public void showSelf() {
		me = this.create();
		try{
		me.show();
		}catch(Exception e){//メモリが溢れているとなる
			e.printStackTrace();
			BCPlayer.getBCACT().stopStream();
		}
	}

	class LiveSettingDialog_Sub extends AlertDialog.Builder{

		private Dialog sub;

		public LiveSettingDialog_Sub(final BCPlayer playerACT) {
			super(playerACT);
			View parent = LayoutInflater.from(playerACT).inflate(R.layout.live_setting_sub_dialog, null);
			this.setView(parent);


			Button start_bt = (Button)parent.findViewById(R.id.start_bt);
			start_bt.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View arg) {
					sub.dismiss();
					((IOwnerContext) playerACT).startLive();
				}
			});

			Button extend_bt = (Button)parent.findViewById(R.id.extend_bt);
			extend_bt.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View arg) {
					sub.dismiss();
					((IOwnerContext) playerACT).extendTestLive();
				}
			});

			Button end_bt = (Button)parent.findViewById(R.id.end_bt);
			end_bt.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View arg) {
					sub.dismiss();
					((IOwnerContext) playerACT).endLive();
				}
			});

		}

		public void showSelf() {
			sub = this.create();
			sub.show();
		}

	}

	class LiveSettingDialog_Mic extends AlertDialog.Builder{

		private Dialog mic;

		public LiveSettingDialog_Mic(final BCPlayer player) {
			super(player);
			View parent = LayoutInflater.from(player).inflate(R.layout.live_setting_mic_dialog, null);
			this.setView(parent);
//			final CheckBox stereo_cb = (CheckBox)parent.findViewById(R.id.stereo_bt);
//			final boolean defaultIsStereo = liveSetting.isStereo();
//			stereo_cb.setChecked(defaultIsStereo);
//			stereo_cb.setOnCheckedChangeListener(new OnCheckedChangeListener(){
//				@Override
//				public void onCheckedChanged(CompoundButton buttonView,
//						boolean isChecked) {
//					liveSetting.setIsStereo(isChecked);
//				}
//			});
//			final TextView volume_tx = (TextView)parent.findViewById(R.id.volume_tx);
//			final SeekBar volume_seek = (SeekBar)parent.findViewById(R.id.volume_seek);
//			final int defaultVolume = (int) (liveSetting.getVolume()*10);
//			volume_tx.setText("音量 :"+((float)defaultVolume/(float)10));
//			volume_seek.setProgress(defaultVolume);
//			volume_seek.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
//				@Override
//				public void onProgressChanged(SeekBar seekbar, int i,
//						boolean flag) {
//					liveSetting.setVolume((float)i/(float)10);
//					volume_tx.setText("音量 :"+(float)i/(float)10);
//				}
//				@Override
//				public void onStartTrackingTouch(SeekBar seekbar) {
//				}
//				@Override
//				public void onStopTrackingTouch(SeekBar seekbar) {
//				}
//			});
//			this.setOnCancelListener(new OnCancelListener(){
//				@Override
//				public void onCancel(DialogInterface arg0) {
//					//値を変更した場合は、ダイアログ終了時に設定値を確定する
//					Log.d("LiveSettingDialog_Mic","Stereo " + stereo_cb.isChecked() +" " + volume_seek.getProgress() );
//					if(defaultVolume != volume_seek.getProgress()){
//						player.getrMic().setVolume_((float)volume_seek.getProgress()/(float)10);
//					}
//					if(defaultIsStereo != liveSetting.isStereo()){
//						player.restartStrream();
//					}
//				}
//			});
		}

		public void showSelf() {
			mic = this.create();
			mic.show();
		}

	}

	class LiveSettingDialog_Cam extends AlertDialog.Builder{

		private Dialog cam;

		public LiveSettingDialog_Cam(final BCPlayer player) {
			super(player);
			View parent = LayoutInflater.from(player).inflate(R.layout.live_setting_cam_dialog, null);
			this.setView(parent);
			final Button resolution_bt = (Button)parent.findViewById(R.id.resolution_bt);
			final Rect defaultSize = liveSetting.getNowActualResolution();
			resolution_bt.setText("解像度: " + defaultSize.right+" × " + defaultSize.bottom);
			resolution_bt.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View view) {
					if(liveSetting.getResolutionList() == null){
						Toast.makeText(player, "カメラが起動されていません", Toast.LENGTH_LONG).show();
						return;
					}
					String[] sizeStrArray = new String[liveSetting.getResolutionList().size()];
					for(int i = 0; i < liveSetting.getResolutionList().size(); i++){
						sizeStrArray[i] = liveSetting.getResolutionList().get(i).width + "×" + liveSetting.getResolutionList().get(i).height;
					}
					Rect nowSize;
					if(liveSetting.isPortLayt()){
						nowSize = liveSetting.getNowPortlaytResolution();
					}else{
						nowSize = liveSetting.getNowActualResolution();
					}
					new AlertDialog.Builder(player)
					.setTitle(nowSize.right + "×"+nowSize.bottom)
					.setItems(sizeStrArray,
							new DialogInterface.OnClickListener() {
								public void onClick(
										DialogInterface dialog,
										final int which) {
									//解像度は、一旦カメラを再起(Camera.open())しないと反映してくれない
									//カメラがネイティブ初期化より後になったら、不正なYUVが入っていく可能性が出てくる=Fatal
									//だから完全にカメラを止めて、解像度をセット→open()を呼ばないといけない→その後Encoderを初期化(ここでもう一回カメラ終了してるか判定されちゃうけどとりあえずしょうがないか)
								new AsyncTask<Void,Void,Integer>(){
										@Override
									protected Integer doInBackground(Void... params) {
//											if(player.getrCam() != null && player.getrCam().isStartedPreview())player.getrCam().stopPreview();
//
//											Log.d("NLiveRoid","rCam Call stop----------- ");
//											while(player.getrCam().isStartedPreview()){
//												try {
//													Thread.sleep(100);
//												} catch (InterruptedException e) {
//													e.printStackTrace();
//													break;
//												}
//												Log.d("NLiveRoid","rCam isStop? " + player.getrCam().isStartedPreview());
//											}
											Log.d("NLiveRoid","rCam was change ------------ ");
											liveSetting.setNowResolution(which);
											liveSetting.culclateRatio();
												return 0;
									}
									@Override
									protected void onPostExecute(Integer arg){
										if(arg == -10){
											MyToast.customToastShow(player, "処理中でした");
											return;
										}else if(arg == -1){
											MyToast.customToastShow(player, "解像度変更に失敗しました");
										}else if(arg == -2){
											MyToast.customToastShow(player, "カメラの再起動に失敗しました");
										}else if(arg < 0){
											MyToast.customToastShow(player, "なんか失敗しました");
										}else{
										final Rect nowSize = liveSetting.getNowActualResolution();
										resolution_bt.setText("解像度: " + nowSize.right+" × " + nowSize.bottom);
										player.changeMode(0);
										}
									}
								}.execute();

								}
					}).create().show();
				}
			});
			final TextView fps_tx = (TextView)parent.findViewById(R.id.fps_tx);
			final SeekBar fps_seek = (SeekBar)parent.findViewById(R.id.fps_seek);
			final int defaultFPS = liveSetting.getUser_fps();
			fps_tx.setText("FPS :"+defaultFPS);
			fps_seek.setProgress(defaultFPS);
			fps_seek.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
				@Override
				public void onProgressChanged(SeekBar seekBar, int progress,
						boolean fromUser) {
					fps_tx.setText("FPS :"+progress);
					liveSetting.setUser_fps(progress);
				}
				@Override
				public void onStartTrackingTouch(SeekBar seekBar) {
				}
				@Override
				public void onStopTrackingTouch(SeekBar seekBar) {
				}
			});
			final TextView keyframe_tx = (TextView)parent.findViewById(R.id.keyframe_tx);
			final SeekBar keyframe_seek = (SeekBar)parent.findViewById(R.id.keyframe_seek);
			final int defaultKeyframe = liveSetting.getKeyframe_interval();
			keyframe_tx.setText("KeyframeInterval :"+defaultKeyframe);
			keyframe_seek.setProgress(defaultKeyframe);
			keyframe_seek.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
				@Override
				public void onProgressChanged(SeekBar seekBar, int progress,
						boolean fromUser) {
					keyframe_tx.setText("KeyframeInterval :"+progress);
					liveSetting.setKeyframe_interval(progress);
				}
				@Override
				public void onStartTrackingTouch(SeekBar seekBar) {
				}
				@Override
				public void onStopTrackingTouch(SeekBar seekBar) {
				}
			});
			this.setOnCancelListener(new OnCancelListener(){
				@Override
				public void onCancel(DialogInterface arg0) {
					//設定値を確定する 解像度はOnClickListenerでやっているので大丈夫
					Log.d("LiveSettingDialog_Cam","FPS " + fps_seek.getProgress() +" " + keyframe_seek.getProgress() );
					if(defaultFPS != fps_seek.getProgress() || defaultKeyframe != keyframe_seek.getProgress()){
						//設定変更→完全にカメラをストップ→ネイティブやり直し
						//mabutaをやり直す
						player.restartStream();
					}
				}
			});
		}

		public void showSelf() {
			cam = this.create();
			cam.show();
		}

	}
}
