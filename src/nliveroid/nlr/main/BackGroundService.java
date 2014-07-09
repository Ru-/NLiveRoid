package nliveroid.nlr.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import nliveroid.nlr.main.parser.AlertParser;
import nliveroid.nlr.main.parser.XMLparser;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.os.RemoteException;
import android.util.Log;

public class BackGroundService extends Service {

//	final Messenger mMessenger = new Messenger(new MyServiceHandler());//コネクトしてメッセージやり取りするためのハンドラー
	private String tempLv = "";
	private BackGroundReceiver receiver;
	private boolean isReplaced;
	private static BackGroundService bcService;
	private byte retryCount = 0;
	private Timer offTimer;

	private static ArrayList<String> alertList;
	private static PendingIntent pendingIntent;
	private static HashMap<String, Long> alertedList;
	private static Messenger aHelper;
	public static boolean isFinish;
	private static byte alert_interval = 5;
	private static PowerManager.WakeLock wl;
    public static void releaseWakeLock() {
        if (wl != null) {
            wl.release();
        }
    }
    private static ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
        	aHelper = new Messenger(service);
        }
        public void onServiceDisconnected(ComponentName className) {
        	aHelper = null;
        }
    };
	@Override
	public void onCreate() {
		super.onCreate();
		Log.d("NLiveRoid","BCService create");
		bcService = this;
    	//バックグラウンドのサービスのリスナ
    	IntentFilter back = new IntentFilter();
    	back.addAction("return_f.NLR");
    	back.addAction("bindTop.NLR");
    	receiver = new BackGroundReceiver();
    	registerReceiver(receiver, back);
    	new AsyncTask<Void,Void,Integer>(){//ブロキャスから来た時のオフタイマーセットとは競合することがない想定は大丈夫か?
			@Override
			protected Integer doInBackground(Void... params) {
				NLiveRoid app = (NLiveRoid)getApplicationContext();
				if(app == null)return -3;//アプリ起動できない
	    		try{
		    		if(app.getDetailsMapValue("off_timer") == null)return -1;//未設定(エラーではない))
					if(app.getDetailsMapValue("alert_enable") == null)return -2;//未設定(エラーではない)
	    		}catch(NullPointerException e){//しばらくすると、マップ自体がnullになってる
	    			e.printStackTrace();
	    			Log.d("NLiveRoid","BackGround failed InitSetting value");//オフタイマーは、そもそも起動してなきゃ問題は無いが
	    			app.initNoTagBitmap();//アラート継続の為、読み込みし直す
	    			try{
	    			if(app.getDetailsMapValue("off_timer") == null)return -4;
					if(app.getDetailsMapValue("alert_enable") == null)return -5;
	    			}catch(Exception e1){
	    				Log.d("NLiveRoid","BCService oncreate FailedFailed");
	    				e1.printStackTrace();
	    				return -6;
	    			}
	    		}
				int returnval =  setOfftimer();
				try{
					if(NLiveRoid.isDebugMode)Log.d("NLiveRoid","alert_enable --- " + app.getDetailsMapValue("alert_enable"));
					if(app.getDetailsMapValue("alert_enable").equals("true")){
						if(NLiveRoid.isDebugMode)Log.d("NLiveRoid","BCService AlertEnable true--- ");
						prepareAlert();
						BackGroundService.registerNextAlert();
					}
					}catch(Exception e){
						e.printStackTrace();
						return returnval == -1?	-3:-2;
					}
				return returnval;
			}
			@Override
			protected void onPostExecute(Integer arg){
				Log.d("NLiveRoid","BCService init Async onPost --- error:" + arg);
				if(arg == -1 || arg == -4){
					MyToast.customToastShow(bcService, "オフタイマーの起動に失敗しました");
				}else if(arg == -2||arg == -5){
					MyToast.customToastShow(bcService, "アラートの起動に失敗しました");
				}else if(arg == -6){
					MyToast.customToastShow(bcService, "アラートとオフタイマーの起動に失敗しました");
				}
			}
    	}.execute();
	}
	@Override
	public void onDestroy(){
		Log.d("NLiveRoid","BCService onDestroy ----- ");
		if(isFinish){
			try{
			unregisterReceiver(receiver);
			}catch(Exception e){
				e.printStackTrace();
			}
		}else{
			bindService(new Intent("nliveroid.nlr.main.AlertHelper"),
		            mConnection, Context.BIND_AUTO_CREATE);
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		Log.d("NLiveRoid","BCService onBind ---- " + intent );
//		return mMessenger.getBinder();
		Notification notif = new Notification();
		// 見えないアイコンをセット
		notif.icon = R.drawable.alert_notificon;
		// アイコンを右に寄せる
		if (NLiveRoid.apiLevel < 9){
			notif.when = Long.MAX_VALUE; // v2.3 未満
		}else{
			notif.when = Long.MIN_VALUE; // v2.3 以上
		}
		startForeground(0, notif);
		return new MyServiceBinder();
	}
	@Override
	public void onRebind(Intent intent){

		Notification notif = new Notification();
		// 見えないアイコンをセット
		notif.icon = R.drawable.alert_notificon;
		// アイコンを右に寄せる
		if (NLiveRoid.apiLevel < 9){
			notif.when = Long.MAX_VALUE; // v2.3 未満
		}else{
			notif.when = Long.MIN_VALUE; // v2.3 以上
		}
		startForeground(0, notif);
		super.onRebind(intent);
	}


	@Override
	public boolean onUnbind(Intent intent) {
		Log.d("NLiveRoid","BCService Unbind --- " + receiver + " " + intent);
		NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		notificationManager.cancelAll();
		//インテントブロードキャスト
        Intent bcIntent  = new Intent();
        bcIntent.setAction("configration.NLR");
        bcIntent.putExtra("val", "finish");
        this.getBaseContext().sendBroadcast(bcIntent);
        Intent bcp = new Intent();
        bcp.setAction("finish_bcplayer.NLR");
        this.getBaseContext().sendBroadcast(bcp);
        Intent splayer = new Intent();
        splayer.setAction("finish_player.NLR");
        this.getBaseContext().sendBroadcast(splayer);
        if(offTimer != null){
    		offTimer.cancel();
    		offTimer.purge();
    	}
        if(aHelper != null){
        	System.gc();
        Message msg = new Message();
		msg.what = 3;
		try {
			aHelper.send(msg);
		} catch (RemoteException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
        }
		super.onUnbind(intent);
		return true;
	}
	@Override
	public boolean stopService(Intent sIntent){
		//TopTabsからstopServiceしても何故かここが呼ばれない
		return super.stopService(sIntent);
	}

	private int setOfftimer(){
		try{
			NLiveRoid app = (NLiveRoid)getApplicationContext();
    		long offtimer_starttime = -1;
    		byte timerVal = Byte.parseByte(app.getDetailsMapValue("off_timer"));
    		Log.d("NLiveRoid","timerVal  " + timerVal);
    		if(timerVal > 0){
    	    	if(offTimer != null){
    	    		offTimer.cancel();
    	    		offTimer.purge();
    	    	}
    	    	offTimer = new Timer();
    	    	offTimer.schedule(new TimerTask(){
					@Override
					public void run() {
						Log.d("BackGround----"," Varusu" );
						//全て終了する
						if(Details.getPref() != null)Details.getPref().finish();
						if(PrimitiveSetting.getACT() != null)PrimitiveSetting.getACT().finish();
						Intent finishFlashPlayer = new Intent();
						finishFlashPlayer.setAction("finish_player.NLR");
						sendBroadcast(finishFlashPlayer);
						Intent finishBCPlayer = new Intent();
						finishBCPlayer.setAction("finish_bcplayer.NLR");
						sendBroadcast(finishBCPlayer);
						if(TopTabs.getACT()!=null){
							TopTabs.getACT().runOnUiThread(new Runnable(){
								@Override
								public void run() {
									MyToast.customToastShow(bcService, "オフタイマーにより終了しました");
									TopTabs.getACT().finish(false);
								}
							});
						}
					}
    	    	},timerVal*60000);
    	    	offtimer_starttime = System.currentTimeMillis();
    		}else{//OFFタイマーがOFFに設定された
    			if(offTimer != null){
    	    		offTimer.cancel();
    	    		offTimer.purge();
    	    	}
    		}
    		//開始時間をセットしてブロキャス
    		app.setDetailsMapValue("offtimer_start", String.valueOf(offtimer_starttime));
    		app.setDetailsMapValue("off_timer", String.valueOf(timerVal));
    		Intent intent = new Intent();
    		intent.setAction("player_config.NLR");
    		intent.putExtra("offtimer_start", String.valueOf(offtimer_starttime));
    		intent.putExtra("off_timer", timerVal);
    		bcService.sendBroadcast(intent);
    	}catch(Exception e){
    		e.printStackTrace();
    		return -1;
    	}
		return 0;
	}
	//※同一プロセスのみでこのサービス自体を返せる
	public class MyServiceBinder extends Binder {
		BackGroundService getService() {
			return BackGroundService.this;
		}
	}

	//サービスとコネクトしてメッセージをやり取りするためのHandler
	class MyServiceHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
        	Log.d("NLiveRoid","BCService handleMessage" + msg.what);
            switch (msg.what) {
                case 1:
                	Intent ahelper = new Intent("nliveroid.nlr.main.AlertHelper");
    				//AlertHelperのonDestroyが呼ばれたらバインドし直す
    				bcService.bindService(ahelper,
    	                mConnection, Context.BIND_AUTO_CREATE);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }
	public void setBackGroundLive(int pid,String lv,String title,int whichPlayer){
		//通知領域


				Notification notification = new Notification();
				notification.icon = R.drawable.icon_notif;
				notification.flags = Notification.FLAG_NO_CLEAR;
				Intent player = null;
				PendingIntent pending = null;
				switch(whichPlayer ){
				case -1:
					return;
				case 0:
					player = new Intent(getApplicationContext(), FlashPlayer.class);
					pending = PendingIntent.getActivity(this, 0, player, 0);
					player.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//普通にTopTabsより上にいる時に正常に呼ばれるため?どうせ上にいるのであま意味ない?
					notification.setLatestEventInfo(getApplicationContext(),lv,title,pending);
					break;
				case 1:
				player = new Intent(getApplicationContext(), BCPlayer.class);
				pending = PendingIntent.getActivity(this, 0, player, 0);
				player.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				notification.setLatestEventInfo(getApplicationContext(),"配信中 "+lv,title,pending);
				break;
				case 2:
					player = new Intent(getApplicationContext(), BCPlayer.class);
					pending = PendingIntent.getActivity(this, 0, player, 0);
					player.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
					notification.setLatestEventInfo(getApplicationContext(),"配信中 "+lv,title,pending);
					break;
				}
				this.tempLv = lv;
				NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
				notificationManager.notify(R.string.app_name, notification);
	}

	class BackGroundReceiver extends BroadcastReceiver{
		@Override
		public void onReceive(Context context, Intent data) {
			Log.d("BackGroundService","RECEIVE_BC - ");
			if(data.getAction().equals("return_f.NLR")){//設定値の返却
					// フラッシュから返ってきた
//				Log.d("BackGroundService","return_f at RECEIVER - ");
				int resultCode = data.getIntExtra("r_code", 5);//5==RESULT_COOKIE 基本的にRESULT_COOKIE
					if (resultCode == CODE.RESULT_REDIRECT) {// 詳細のリンクタップでフラッシュ終了からリダイレクトでブラウザ起動
						String url = data.getStringExtra("redirectlink");
							if(url == null || url.equals(""))return;
							Uri uri = Uri.parse(url);
							Intent i = new Intent(Intent.ACTION_VIEW);
							i.addCategory(Intent.CATEGORY_BROWSABLE);
							i.setDataAndType(uri, "text/html");
							bcService.startActivity(i);
					} else if (resultCode == CODE.RESULT_COOKIE) {
							// フラッシュから正常終了又はコメントのみ
							 storeReturnData(data);
					}else if(resultCode == CODE.RESULT_BROADCAST){//配信の場合はliveSettingを書き込まないといけないので視聴と分けた
							 storeReturnData(data);
							int arg = WriteProfile(data);
							NLiveRoid app = (NLiveRoid)bcService.getApplicationContext();
							if(arg == -1){
								MyToast.customToastShow(app.getForeACT(), "SDカードが利用できませんでした");
							}else if(arg == -2){
								MyToast.customToastShow(app.getForeACT(), "設定値書き込みでIOエラーが発生しました");
							}else if(arg == -3){
								MyToast.customToastShow(app.getForeACT(), "配信設定の書き込みに失敗しました");
							}else if(arg == -4){
								MyToast.customToastShow(app.getForeACT(), "配信設定の書き込みでエラーが発生しました");
							}
					}else if (resultCode == CODE.RESULT_NOLOGIN) {// ログインしておらず、セッションを消す必要がある
//							CookieSyncManager.getInstance().startSync();
//							CookieManager.getInstance().setCookie("nicovideo.jp",
//									"");//コメントのみの時にCookieを消す
//							CookieSyncManager.getInstance().stopSync();
							int errorCode = data.getIntExtra("flash_error", 0);
							NLiveRoid app = (NLiveRoid)bcService.getApplicationContext();
							app.setForeACT(TopTabs.getACT());
							ErrorCode error = app.getError();
							if (error != null) {
								error.setErrorCode(errorCode);
								error.showErrorToast();
							}
					}else if(resultCode == CODE.RESULT_ALL_UPDATE){
							//セッションを更新しておく
							NLiveRoid app = (NLiveRoid)bcService.getApplicationContext();
							app.setSessionid(data.getStringExtra("new_session"));
					}else if(resultCode == CODE.RESULT_FROM_GATE_FINISH){//フラッシュ(別プロセスからタグ検索の依頼)
							if (SearchTab.getSearchTab() == null ) {//検索タブを起動から開いたことない場合開く
								TopTabs.getACT().getTabHost().setCurrentTab(1);
							}
							if(data.getStringExtra("tagword") != null){
							TopTabs.getACT().changeTag(1);
							SearchTab.getSearchTab()
									.keyWordSearch_FromGate(data.getStringExtra("tagword"));
							}else if(data.getStringExtra("archive") != null){//Flashから開いた詳細(Trans)からのみ呼ばれる(OverLayは同一プロセスなので直接呼んじゃう)
								Intent commuTab = new Intent(bcService,TopTabs.class);
								commuTab.putExtra("scheme", data.getStringExtra("archive"));
								commuTab.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
								startActivity(commuTab);
							}
					} else if (resultCode == CODE.RESULT_FLASH_ERROR) {//その他エラー全般
							int errorCode = data.getIntExtra("flash_error", 0);
							if(retryCount <=0&&errorCode == -17){//セッションだったら1度だけリトライする
								LiveInfo li = (LiveInfo) data.getSerializableExtra("LiveInfo");
								if(li != null)CommunityTab.getCommunityTab().startFlashPlayer(li);
								retryCount++;
								Log.d("NLiveRoid","RETRY CONNECTION LI " + li);
								return;
							}
							//TS時のlvURL(lv又はURL)の処理
							NLiveRoid app = (NLiveRoid)bcService.getApplicationContext();
							if(errorCode == -18&&app.getDetailsMapValue("recent_ts") != null&& Boolean.parseBoolean(app.getDetailsMapValue("recent_ts"))){//放送が終了していて、直近TS設定ONだったら見に行く(履歴からだと設定値がnullになる)
								LiveInfo li = (LiveInfo) data.getSerializableExtra("LiveInfo");
								if(li != null){
								String liveidResult = li.getCommunityID() == null||li.getCommunityID().equals(URLEnum.HYPHEN)? li.getLiveID():li.getCommunityID();
								Intent commuTab = new Intent(bcService,TopTabs.class);
								commuTab.putExtra("scheme", "ts"+liveidResult);
								commuTab.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
								startActivity(commuTab);
								return;
								}
							}
							retryCount = 0;
							Log.d("NLiveRoid","FLASH ERROR " + errorCode);
							ErrorCode error = app.getError();
							if (error != null) {
								error.setErrorCode(errorCode);
								error.showErrorToast();
							}
					}else if(resultCode == CODE.RESULT_ENDPLAYER_LV_URL){//一応データ保存しておく
							storeReturnData(data);
							String lv_url = data.getStringExtra("scheme");
							if(lv_url != null ){
								Intent topTab = new Intent(bcService.getApplicationContext(),TopTabs.class);
								topTab.putExtra("scheme", lv_url);
								topTab.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
								bcService.getApplicationContext().startActivity(topTab);
							}
					}else if(resultCode == CODE.RESULT_LOG){
							Log.d("NLiveRoid","RECEIVE LOG ----------- " + data.getStringExtra("log"));
							if(data.getStringExtra("log") != null){
							NLiveRoid.outLog(data.getStringExtra("log") + "\n");
							}
					}else if(resultCode == CODE.ALERT){
						Log.d("NLiveRoid","AlertReceived --------------- " + context);
				        if(data.getIntExtra("alert_h", -1) > 0){
//				        	Process.sendSignal(data.getIntExtra("alert_h", -1), Process.SIGNAL_KILL);
//
//				        	Intent ahelper = new Intent("nliveroid.nlr.main.AlertHelper");
//							//別プロセスだとstartServiceするとバインドできない
//							bcService.bindService(ahelper,
//				                mConnection, Context.BIND_AUTO_CREATE);
				        }else{
						doAlert(context);
				        }
					}

						// フラッシュのところでマナーにされていたりしたら音量をもどす
						byte returnVol = data.getByteExtra("audiovolume",(byte)-1);
						if (returnVol != -1) {
							AudioManager audio = (AudioManager) getSystemService(bcService.AUDIO_SERVICE);
							int mode = audio.getRingerMode();
							if (mode == AudioManager.RINGER_MODE_VIBRATE
									|| mode == AudioManager.RINGER_MODE_SILENT) {
								// 音量を戻しておく(モードは変えなくてもセットされてくれるみたい)
								audio.setStreamVolume(AudioManager.STREAM_MUSIC,
										(int)returnVol, 0);
							}
						}

			}else if(data.getAction().equals("bindTop.NLR")){
			int pid = data.getIntExtra("pid", -1);
			if(data.getStringExtra("off_timer")!=null){//OFFタイマーの変更
				NLiveRoid app = (NLiveRoid)bcService.getApplicationContext();
				Log.d("RECEIV " , "  " + data.getStringExtra("off_timer"));
				app.setDetailsMapValue("off_timer", String.valueOf(data.getStringExtra("off_timer")));
				new AsyncTask<Void,Void,Integer>(){
					@Override
					protected Integer doInBackground(Void... params) {
						return setOfftimer();
					}
					@Override
					protected void onPostExecute(Integer arg){
						if(arg == -1)MyToast.customToastShow(bcService, "オフタイマーの起動に失敗しました");
					}
		    	}.execute();
			}else if(data.getBooleanExtra("isPre", false)){//プレアカじゃない
				NLiveRoid.isNotPremium = true;
			}else if(pid != -1){
				setBackGroundLive(pid,data.getStringExtra("lv"),data.getStringExtra("title"),data.getIntExtra("playerNumber", -1));
			}else if(data.getIntExtra("clear", -1) != -1){
				Log.d("NLiveRoid","CLEAR --- " + data.getIntExtra("clear", -1));
				if(data.getIntExtra("clear", -1) == 0){//2窓を非対応にする
//					Log.d("log","NLR =------ CLEAR 0");
					//インテントブロードキャスト
			        Intent backIntent  = new Intent();
			        backIntent.setAction("finish_bcplayer.NLR");//BCをfinishする
			        getBaseContext().sendBroadcast(backIntent);
			        Intent killbcIntent  = new Intent();
			        killbcIntent.setAction("configration.NLR");//BCをfinishする
			        killbcIntent.putExtra("val", "finish");
			        getBaseContext().sendBroadcast(killbcIntent);
				}else if(data.getIntExtra("clear", -1) == 1){
//					Log.d("log","NLR =------ CLEAR 1");
					//インテントブロードキャスト
			        Intent backIntent  = new Intent();
			        backIntent.setAction("finish_player.NLR");
			        getBaseContext().sendBroadcast(backIntent);
				}else if(data.getIntExtra("clear", -1) == 2){//BCは、BCPlayerからじゃないと遷移できないのでいずれにせよ、今はリスナープレイヤーを終了すればいい
//					Log.d("log","NLR =------ CLEAR 2");
					//インテントブロードキャスト
			        Intent backIntent  = new Intent();
			        backIntent.setAction("finish_player.NLR");
			        getBaseContext().sendBroadcast(backIntent);
				}else if(data.getBooleanExtra("restart_bc",true)){
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}catch(IllegalArgumentException e){
						e.printStackTrace();
					}
					bcService.startActivity(data);
				}
			}
			}

		}

	}
	public boolean getReplaced(){
		return isReplaced;
	}

	public void setReplacedFlase(){
		isReplaced = false;
	}

	public String getLiveID(){
		return tempLv;
	}



	/**
	 * 帰ってきた値を設定マップに保存する
	 */
	private void storeReturnData(Intent data){
		System.gc();//ここでGC入れないとどんどんRAMが圧迫されていく
		byte[] setting_byte = data.getByteArrayExtra("setting_byte");
		boolean[] setting_boolean = data.getBooleanArrayExtra("setting_boolean");
		int init_comment_count = data.getShortExtra("init_comment_count", (short)20);
		long offTimer = data.getLongExtra("offtimer_start", -1);
		CommandMapping cmd = (CommandMapping) data.getSerializableExtra("cmd");
		byte[] column_seq = data.getByteArrayExtra("column_seq");
		NLiveRoid app = (NLiveRoid)getApplicationContext();

		//nullチェック
		if(setting_boolean == null){
		setting_boolean = new boolean[28];
		try{
		setting_boolean[0] = app.getDetailsMapValue("fexit")==null? true:Boolean.parseBoolean(app.getDetailsMapValue("fexit"));
		setting_boolean[1] = app.getDetailsMapValue("newline")== null? true:Boolean.parseBoolean(app.getDetailsMapValue("newline"));
		setting_boolean[2] = app.getDetailsMapValue("form_up") == null ? false: Boolean.parseBoolean(app.getDetailsMapValue("form_up"));
		setting_boolean[3] = app.getDetailsMapValue("voice_input") == null ? false:Boolean.parseBoolean(app.getDetailsMapValue("voice_input"));
//		setting_boolean[4] = app.getDetailsMapValue("sp_player") == null ? true:Boolean.parseBoolean(app.getDetailsMapValue("sp_player"));
		setting_boolean[5] = app.getDetailsMapValue("fix_volenable") == null ? false:Boolean.parseBoolean(app.getDetailsMapValue("fix_volenable"));
		setting_boolean[6] = app.getDetailsMapValue("at_enable")==null? false:Boolean.parseBoolean(app.getDetailsMapValue("at_enable"));
		setting_boolean[7] = app.getDetailsMapValue("at_overwrite")==null? false:Boolean.parseBoolean(app.getDetailsMapValue("at_overwrite"));

		setting_boolean[8] = app.getDetailsMapValue("xd_enable_p") == null ? false: Boolean.parseBoolean(app.getDetailsMapValue("xd_enable_p"));
		setting_boolean[9] = app.getDetailsMapValue("yd_enable_p") == null ? true: Boolean.parseBoolean(app.getDetailsMapValue("yd_enable_p"));
		setting_boolean[10] = app.getDetailsMapValue("xd_enable_l") == null ? false: Boolean.parseBoolean(app.getDetailsMapValue("xd_enable_l"));
		setting_boolean[11] = app.getDetailsMapValue("yd_enable_l") == null ? true: Boolean.parseBoolean(app.getDetailsMapValue("yd_enable_l"));
		setting_boolean[12] = app.getDetailsMapValue("speech_education_enable") == null ? true: Boolean.parseBoolean(app.getDetailsMapValue("speech_education_enable"));
		setting_boolean[13] = app.getDetailsMapValue("is_update_between") == null ? true:Boolean.parseBoolean(app.getDetailsMapValue("is_update_between"));

		setting_boolean[14] = app.getDetailsMapValue("sp_showcomment") == null ? true: Boolean.parseBoolean(app.getDetailsMapValue("sp_showcomment"));
		setting_boolean[15] = app.getDetailsMapValue("sp_ng184") == null ? true: Boolean.parseBoolean(app.getDetailsMapValue("sp_ng184"));
		setting_boolean[16] = app.getDetailsMapValue("sp_showbspcomment") == null ? true: Boolean.parseBoolean(app.getDetailsMapValue("sp_showbspcomment"));
		setting_boolean[17] = app.getDetailsMapValue("sp_ismute") == null ? true: Boolean.parseBoolean(app.getDetailsMapValue("sp_ismute"));
		setting_boolean[18] = app.getDetailsMapValue("sp_loadsmile") == null ? true: Boolean.parseBoolean(app.getDetailsMapValue("sp_loadsmile"));
		setting_boolean[19] = app.getDetailsMapValue("auto_username") == null ? false: Boolean.parseBoolean(app.getDetailsMapValue("auto_username"));
		setting_boolean[20] = app.getDetailsMapValue("form_backkey") == null ? true: Boolean.parseBoolean(app.getDetailsMapValue("form_backkey"));
		setting_boolean[21] = app.getDetailsMapValue("discard_notification") == null ? false: Boolean.parseBoolean(app.getDetailsMapValue("discard_notification"));
		setting_boolean[22] = app.getDetailsMapValue("manner_0") == null ? false: Boolean.parseBoolean(app.getDetailsMapValue("manner_0"));
		setting_boolean[23] = app.getDetailsMapValue("return_tab") == null ? false: Boolean.parseBoolean(app.getDetailsMapValue("return_tab"));
		setting_boolean[24] = app.getDetailsMapValue("update_tab") == null ? false: Boolean.parseBoolean(app.getDetailsMapValue("update_tab"));
		setting_boolean[25] = app.getDetailsMapValue("recent_ts") == null ? true: Boolean.parseBoolean(app.getDetailsMapValue("recent_ts"));
		setting_boolean[26] = app.getDetailsMapValue("delay_start") == null ? true: Boolean.parseBoolean(app.getDetailsMapValue("delay_start"));
		setting_boolean[27] = app.getDetailsMapValue("back_black") == null ? false: Boolean.parseBoolean(app.getDetailsMapValue("back_black"));
		}catch(Exception e){
			//失敗したらデフォ値
			setting_boolean = new boolean[]{true,false,false,false,true,false,false,false,false,true,false,true,false,true,true,true,true,true,true,false,true,false,false,false,false,true,true,false};
		}
		}
		if(init_comment_count == 0){
			init_comment_count = app.getDetailsMapValue("init_comment_count") == null? 20:Short.parseShort(app.getDetailsMapValue("init_comment_count"));
		}
		//nullチェック
		if(setting_byte == null){
		setting_byte = new byte[44];
		try{
		setting_byte[0] = app.getDetailsMapValue("type_width_p")==null? 0:Byte.parseByte(app.getDetailsMapValue("type_width_p"));
		setting_byte[1] = app.getDetailsMapValue("id_width_p")==null? 15:Byte.parseByte(app.getDetailsMapValue("id_width_p"));
		setting_byte[2] = app.getDetailsMapValue("command_width_p")==null? 0:Byte.parseByte(app.getDetailsMapValue("command_width_p"));
		setting_byte[3] = app.getDetailsMapValue("time_width_p")==null? 0:Byte.parseByte(app.getDetailsMapValue("time_width_p"));
		//挿入
		setting_byte[4] = app.getDetailsMapValue("score_width_p")==null? 0:Byte.parseByte(app.getDetailsMapValue("score_width_p"));
		setting_byte[5] = app.getDetailsMapValue("num_width_p")==null? 15:Byte.parseByte(app.getDetailsMapValue("num_width_p"));
		setting_byte[6] = app.getDetailsMapValue("comment_width_p")==null? 70:Byte.parseByte(app.getDetailsMapValue("comment_width_p"));
		setting_byte[7] = app.getDetailsMapValue("cellheight_p")==null? 3:Byte.parseByte(app.getDetailsMapValue("cellheight_p"));
		setting_byte[8] = app.getDetailsMapValue("x_pos_p")==null? 0:Byte.parseByte(app.getDetailsMapValue("x_pos_p"));
		setting_byte[9] = app.getDetailsMapValue("y_pos_p")==null? 92:Byte.parseByte(app.getDetailsMapValue("y_pos_p"));
		setting_byte[10] = app.getDetailsMapValue("bottom_pos_p")==null? -43:Byte.parseByte(app.getDetailsMapValue("bottom_pos_p"));
		setting_byte[11] = app.getDetailsMapValue("type_width_l")==null? 0:Byte.parseByte(app.getDetailsMapValue("type_width_l"));
		setting_byte[12] = app.getDetailsMapValue("id_width_l")==null? 0:Byte.parseByte(app.getDetailsMapValue("id_width_l"));
		setting_byte[13] = app.getDetailsMapValue("command_width_l")==null? 0:Byte.parseByte(app.getDetailsMapValue("command_width_l"));
		setting_byte[14] = app.getDetailsMapValue("time_width_l")==null? 0:Byte.parseByte(app.getDetailsMapValue("time_width_l"));
		//挿入
		setting_byte[15] = app.getDetailsMapValue("score_width_l")==null? 0:Byte.parseByte(app.getDetailsMapValue("score_width_l"));
		setting_byte[16] = app.getDetailsMapValue("num_width_l")==null? 15:Byte.parseByte(app.getDetailsMapValue("num_width_l"));
		setting_byte[17] = app.getDetailsMapValue("comment_width_l")==null? 70:Byte.parseByte(app.getDetailsMapValue("comment_width_l"));
		setting_byte[18] = app.getDetailsMapValue("cellheight_l")==null? 3:Byte.parseByte(app.getDetailsMapValue("cellheight_l"));
		setting_byte[19] = app.getDetailsMapValue("x_pos_l")==null? 0:Byte.parseByte(app.getDetailsMapValue("x_pos_l"));
		setting_byte[20] = app.getDetailsMapValue("y_pos_l")==null? 92:Byte.parseByte(app.getDetailsMapValue("y_pos_l"));
		setting_byte[21] = app.getDetailsMapValue("bottom_pos_l")==null? -43:Byte.parseByte(app.getDetailsMapValue("bottom_pos_l"));
		setting_byte[22] = app.getDetailsMapValue("player_pos_p")==null ? 0:Byte.parseByte(app.getDetailsMapValue("player_pos_p"));
		setting_byte[23] = app.getDetailsMapValue("player_pos_l")==null ? 0:Byte.parseByte(app.getDetailsMapValue("player_pos_l"));
		setting_byte[24] = app.getDetailsMapValue("fix_screen")==null ? 0:Byte.parseByte(app.getDetailsMapValue("fix_screen"));
		if(setting_boolean[5]){//有効にしてなければ-1
			setting_byte[25] = app.getDetailsMapValue("fix_volvalue") == null ? -1:Byte.parseByte(app.getDetailsMapValue("fix_volvalue"));
		}else{
			setting_byte[25] = -1;
		}
		setting_byte[26] = app.getDetailsMapValue("speech_speed")==null ? 5:Byte.parseByte(app.getDetailsMapValue("speech_speed"));
		setting_byte[27] = app.getDetailsMapValue("speech_pich")==null ? 5:Byte.parseByte(app.getDetailsMapValue("speech_pich"));
		setting_byte[28] = app.getDetailsMapValue("speech_aques_phont")==null ? 0:Byte.parseByte(app.getDetailsMapValue("speech_aques_phont"));
		setting_byte[29] = app.getDetailsMapValue("speech_skip_count")==null ? 5:Byte.parseByte(app.getDetailsMapValue("speech_skip_count"));
		setting_byte[30] = app.getDetailsMapValue("sp_volumesub")==null ? 50:Byte.parseByte(app.getDetailsMapValue("sp_volumesub"));
		setting_byte[31] = app.getDetailsMapValue("layer_num")==null ? 0:Byte.parseByte(app.getDetailsMapValue("layer_num"));
		setting_byte[32] = app.getDetailsMapValue("auto_comment_update")==null ? -1:Byte.parseByte(app.getDetailsMapValue("auto_comment_update"));
		setting_byte[33] = app.getDetailsMapValue("speech_enable")==null ? 0:Byte.parseByte(app.getDetailsMapValue("speech_enable"));
		setting_byte[34] = app.getDetailsMapValue("player_quality")==null ? 0:Byte.parseByte(app.getDetailsMapValue("player_quality"));
		setting_byte[35] = app.getDetailsMapValue("cellheight_test")==null? 3:Byte.parseByte(app.getDetailsMapValue("cellheight_test"));
		setting_byte[36] = app.getDetailsMapValue("speech_aques_vol")==null? 5:Byte.parseByte(app.getDetailsMapValue("speech_aques_vol"));
		setting_byte[37] = app.getDetailsMapValue("off_timer")==null? -1:Byte.parseByte(app.getDetailsMapValue("off_timer"));
		setting_byte[38] = app.getDetailsMapValue("width_p")==null? 100:Byte.parseByte(app.getDetailsMapValue("width_p"));
		setting_byte[39] = app.getDetailsMapValue("width_l")==null? 40:Byte.parseByte(app.getDetailsMapValue("width_l"));
		setting_byte[40] = app.getDetailsMapValue("quick_0")==null? 15:Byte.parseByte(app.getDetailsMapValue("quick_0"));
		setting_byte[41] = app.getDetailsMapValue("quick_1")==null? 127:Byte.parseByte(app.getDetailsMapValue("quick_1"));
		setting_byte[42] = app.getDetailsMapValue("alpha")==null? 0:Byte.parseByte(app.getDetailsMapValue("alpha"));
		setting_byte[43] = app.getDetailsMapValue("player_select")==null? 0:Byte.parseByte(app.getDetailsMapValue("player_select"));

			}catch(Exception e){
				//失敗したらデフォ値
				setting_byte = new byte[]{0,15,0,0,0,15,70,3,0,92,-43,0,15,0,0,0,15,70,3,0,92,-43,0,0,0,-1,5,5,0,5,50,0,-1,0,0,3,5,-1,100,40,15,127,0,0};
			}
		}

		//保存するべきものだけ、設定値を全て保存する
		//ここで返ってきた値がnullじゃない
		if(cmd == null){
		cmd = new CommandMapping(false);//isOwnerは毎回のタブでisOwner以外を引き継いで決めているのでここではどちらでもおｋ
		}
		//プレイヤーのみで視聴しててしばらくたって、戻ると、ここがnullの場合(連携起動?)がある
		if(app == null||app.getDetailsMap() == null){
			return;
		}
		Log.d("NLiveRoid","Return BService --- ");
		if(setting_boolean[23]&&setting_byte[37]<0){//タブに戻る設定+オフタイマーによる全て終了じゃないだったら、ここでトップを起動する
			Intent topTab = new Intent(bcService.getApplicationContext(),TopTabs.class);
			topTab.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			bcService.getApplicationContext().startActivity(topTab);
		}
		app.setDetailsMapValue("cmd_cmd", cmd.getValue(CommandKey.CMD));
		app.setDetailsMapValue("cmd_size", cmd.getValue(CommandKey.Size));
		app.setDetailsMapValue("cmd_color", cmd.getValue(CommandKey.Color));
		app.setDetailsMapValue("cmd_align", cmd.getValue(CommandKey.Align));

		if(column_seq != null){
		app.setDetailsMapValue("type_seq",String.valueOf(column_seq[0]));
		app.setDetailsMapValue("id_seq",String.valueOf(column_seq[1]));
		app.setDetailsMapValue("cmd_seq",String.valueOf(column_seq[2]));
		app.setDetailsMapValue("time_seq",String.valueOf(column_seq[3]));
		app.setDetailsMapValue("score_seq",String.valueOf(column_seq[4]));
		app.setDetailsMapValue("num_seq",String.valueOf(column_seq[5]));
		app.setDetailsMapValue("comment_seq",String.valueOf(column_seq[6]));
		}

		//boolean
		app.setDetailsMapValue("fexit",String.valueOf(setting_boolean[0]));

		app.setDetailsMapValue("newline",String.valueOf(setting_boolean[1]));
		app.setDetailsMapValue("form_up",String.valueOf(setting_boolean[2]));
		app.setDetailsMapValue("voice_input",String.valueOf(setting_boolean[3]));
		app.setDetailsMapValue("sp_player",String.valueOf(setting_boolean[4]));
		app.setDetailsMapValue("fix_volenable",String.valueOf(setting_boolean[5]));
		app.setDetailsMapValue("at_enable",String.valueOf(setting_boolean[6]));
		app.setDetailsMapValue("at_overwrite",String.valueOf(setting_boolean[7]));

		app.setDetailsMapValue("xd_enable_p",String.valueOf(setting_boolean[8]));
		app.setDetailsMapValue("yd_enable_p",String.valueOf(setting_boolean[9]));
		app.setDetailsMapValue("xd_enable_l",String.valueOf(setting_boolean[10]));
		app.setDetailsMapValue("yd_enable_l",String.valueOf(setting_boolean[11]));
		app.setDetailsMapValue("speech_education_enable",String.valueOf(setting_boolean[12]));
		app.setDetailsMapValue("is_update_between",String.valueOf(setting_boolean[13]));

		app.setDetailsMapValue("sp_showcomment",String.valueOf(setting_boolean[14]));
		app.setDetailsMapValue("sp_ng184",String.valueOf(setting_boolean[15]));
		app.setDetailsMapValue("sp_showbspcomment",String.valueOf(setting_boolean[16]));
		app.setDetailsMapValue("sp_ismute",String.valueOf(setting_boolean[17]));
		app.setDetailsMapValue("sp_loadsmile",String.valueOf(setting_boolean[18]));
		app.setDetailsMapValue("auto_username",String.valueOf(setting_boolean[19]));
		app.setDetailsMapValue("form_backkey",String.valueOf(setting_boolean[20]));
		app.setDetailsMapValue("discard_notification",String.valueOf(setting_boolean[21]));
		app.setDetailsMapValue("manner_0",String.valueOf(setting_boolean[22]));
		app.setDetailsMapValue("return_tab",String.valueOf(setting_boolean[23]));
		app.setDetailsMapValue("update_tab",String.valueOf(setting_boolean[24]));
		app.setDetailsMapValue("recent_ts",String.valueOf(setting_boolean[25]));
		app.setDetailsMapValue("delay_start",String.valueOf(setting_boolean[26]));
		app.setDetailsMapValue("back_black",String.valueOf(setting_boolean[27]));

		//int
		app.setDetailsMapValue("init_comment_count",String.valueOf(init_comment_count));
		//long
		app.setDetailsMapValue("offtimer_start",String.valueOf(offTimer));

		//byte
		app.setDetailsMapValue("type_width_p",String.valueOf(setting_byte[0]));
		app.setDetailsMapValue("id_width_p",String.valueOf(setting_byte[1]));
		app.setDetailsMapValue("command_width_p",String.valueOf(setting_byte[2]));
		app.setDetailsMapValue("time_width_p",String.valueOf(setting_byte[3]));
		app.setDetailsMapValue("score_width_p",String.valueOf(setting_byte[4]));
		app.setDetailsMapValue("num_width_p",String.valueOf(setting_byte[5]));
		app.setDetailsMapValue("comment_width_p",String.valueOf(setting_byte[6]));
		app.setDetailsMapValue("cellheight_p",String.valueOf(setting_byte[7]));
		app.setDetailsMapValue("x_pos_p",String.valueOf(setting_byte[8]));
		app.setDetailsMapValue("y_pos_p",String.valueOf(setting_byte[9]));
		app.setDetailsMapValue("bottom_pos_p",String.valueOf(setting_byte[10]));
		app.setDetailsMapValue("type_width_l",String.valueOf(setting_byte[11]));
		app.setDetailsMapValue("id_width_l",String.valueOf(setting_byte[12]));
		app.setDetailsMapValue("command_width_l",String.valueOf(setting_byte[13]));
		app.setDetailsMapValue("time_width_l",String.valueOf(setting_byte[14]));
		app.setDetailsMapValue("score_width_l",String.valueOf(setting_byte[15]));
		app.setDetailsMapValue("num_width_l",String.valueOf(setting_byte[16]));
		app.setDetailsMapValue("comment_width_l",String.valueOf(setting_byte[17]));
		app.setDetailsMapValue("cellheight_l",String.valueOf(setting_byte[18]));
		app.setDetailsMapValue("x_pos_l",String.valueOf(setting_byte[19]));
		app.setDetailsMapValue("y_pos_l",String.valueOf(setting_byte[20]));
		app.setDetailsMapValue("bottom_pos_l",String.valueOf(setting_byte[21]));
		app.setDetailsMapValue("player_pos_p",String.valueOf(setting_byte[22]));
		app.setDetailsMapValue("player_pos_l",String.valueOf(setting_byte[23]));
		app.setDetailsMapValue("fix_screen",String.valueOf(setting_byte[24]));
		app.setDetailsMapValue("fix_volvalue",String.valueOf(setting_byte[25]));
		app.setDetailsMapValue("speech_speed",String.valueOf(setting_byte[26]));
		app.setDetailsMapValue("speech_pich",String.valueOf(setting_byte[27]));
		app.setDetailsMapValue("speech_aques_phont",String.valueOf(setting_byte[28]));
		app.setDetailsMapValue("speech_skip_count",String.valueOf(setting_byte[29]));
		app.setDetailsMapValue("sp_volumesub",String.valueOf(setting_byte[30]));
		app.setDetailsMapValue("layer_num",String.valueOf(setting_byte[31]));
		app.setDetailsMapValue("auto_comment_update",String.valueOf(setting_byte[32]));
		app.setDetailsMapValue("speech_enable",String.valueOf(setting_byte[33]));
		app.setDetailsMapValue("player_quality",String.valueOf(setting_byte[34]));
		app.setDetailsMapValue("cellheight_test",String.valueOf(setting_byte[35]));
		app.setDetailsMapValue("speech_aques_vol",String.valueOf(setting_byte[36]));
		app.setDetailsMapValue("off_timer",String.valueOf(setting_byte[37]));
		app.setDetailsMapValue("width_p",String.valueOf(setting_byte[38]));
		app.setDetailsMapValue("width_l",String.valueOf(setting_byte[39]));
		app.setDetailsMapValue("quick_0",String.valueOf(setting_byte[40]));
		app.setDetailsMapValue("quick_1",String.valueOf(setting_byte[41]));
		app.setDetailsMapValue("alpha",String.valueOf(setting_byte[42]));
		app.setDetailsMapValue("player_select",String.valueOf(setting_byte[43]));
		Log.d("NLiveRoid","setBackGround");
	}

	  private int WriteProfile(Intent data){
		  LiveInfo liveInfo = (LiveInfo)data.getSerializableExtra("LiveInfo");
		  int[] params = data.getIntArrayExtra("LiveSettings");

		  Log.d("BackGroundService","WriteProfile");
		  if(liveInfo == null || params == null)return -3;
			//ファイルを読み込む
			boolean isStorageAvalable = false;
			boolean isStorageWriteable = false;
			String state = Environment.getExternalStorageState();
			if(state == null){
				return -1;
			}else if (Environment.MEDIA_MOUNTED.equals(state)) {
			    //読み書きOK
			    isStorageAvalable = isStorageWriteable = true;
			} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			    //読み込みだけOK
			    isStorageAvalable = true;
			    isStorageWriteable = false;
			} else {
				//ストレージが有効でない
			    isStorageAvalable = isStorageWriteable = false;
			    return -1;
			}
			boolean notAvalable = !isStorageAvalable;
			boolean notWritable = !isStorageWriteable;
			if(notAvalable||notWritable){
				return -1;
			}


			//sdcard直下に、パッケージ名のフォルダを作りファイルを生成
			String filePath = Environment.getExternalStorageDirectory().toString() + "/NLiveRoid";
			File directory = new File(filePath);
			ErrorCode error = ((NLiveRoid)getApplicationContext()).getError();
//			Log.d("log","filepath " + filePath + " \n isCANWRITE " + directory.canWrite());
			if(directory.mkdirs()){//すでにあった場合も失敗する
				Log.d("NLiveRoid","mkdir");
			}
			File file = new File(filePath,"LiveProfile.xml");
				try {
					file.createNewFile();//次からの読み込みがエラーしないように空のファイル生成
					//コミュニティ名(getCommunityName())が何故か時間がたつと初期化され?"-"になって次から読み込まれなくなるので応急処置
					Log.d("BCPlayer","commu info" + liveInfo.getDefaultCommunity() + " " + liveInfo.getCommunityName());
					if(liveInfo.getCommunityName() == null){//正しく返っていなかったら
						liveInfo.setComunityName("0");//とりあえずインデックスを入れておく
					}else if(liveInfo.getCommunityName().contains("Lv")){
						//OK
					}else if(liveInfo.getDefaultCommunity()!=null&&liveInfo.getDefaultCommunity().matches("co[0-9]+")){
						Log.d("BackGroundService","DefaultCommu");
						liveInfo.setComunityName(liveInfo.getDefaultCommunity());
					}else if(liveInfo.getCommunityID()!=null&&liveInfo.getCommunityID().matches("co[0-9]+")){
						Log.d("BackGroundService","getCommunityID");
						liveInfo.setComunityName(liveInfo.getCommunityID());
					}else if(!liveInfo.getCommunityName().contains("Lv")){
						liveInfo.setComunityName("0");//とりあえずインデックスを入れておく
					}else{// 不明
						liveInfo.setComunityName("0");
					}
				String xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"+
					"<LiveProfile xmlns=\"http://nliveroid-tutorial.appspot.com/liveprofile/\">\n"+
					"<title>"+liveInfo.getTitle()+"</title>\n"+
					"<description>"+liveInfo.getDescription()+"</description>\n" +
					"<community_name>"+liveInfo.getCommunityName()+"</community_name>\n" +
					"<category>"+liveInfo.getCategoryName()+"</category>\n" +
					"<tag></tag>\n" +//おかしくなるのでとりあえず無し
					"<lock></lock>\n" +
					"<public_status>"+liveInfo.isMemberOnly()+"</public_status>\n" +
					"<timeshift_enable>"+liveInfo.isTimeShiftEnable()+"</timeshift_enable>\n" +
					"<live_mode>"+((params[0] & 0x00F00000) != 0)+"</live_mode>\n" +
					"<use_camera>"+((params[0] & 0x80000000) != 0)+"</use_camera>\n" +//符号ありとして考えられてしまうので>0は!=0
					"<use_mic>"+((params[0] & 0x40000000) != 0)+"</use_mic>\n" +
					"<back_camera>"+((params[0] & 0x20000000) != 0)+"</back_camera>\n" +
					"<back_mic>"+((params[0] & 0x10000000) != 0)+"</back_mic>\n" +
					"<ring_camera>"+((params[0] & 0x08000000) != 0)+"</ring_camera>\n" +
					"<ring_mic>"+((params[0] & 0x04000000) != 0)+"</ring_mic>\n" +
					"<live_mode>"+((params[0] >>20 ) & 0x0000000F)+"</live_mode>\n" +
					"<resolution_index>"+((params[0] >>16 ) & 0x0000000F)+"</resolution_index>\n" +
					"<scene>"+((params[0] >>12 ) & 0x0000000F )+"</scene>\n" +
					"<is_stereo>"+((params[0] & 0x00000400) != 0 )+"</is_stereo>\n" +
					"<fps>"+((params[1] >>24 ) & 0x000000FF )+"</fps>\n" +
					"<keyframe_interval>"+((params[1] >>16) & 0x000000FF )+"</keyframe_interval>\n" +
					"<volume>"+((params[1]) & 0x0000FFFF )+"</volume>\n" +
					"<movie_path>"+data.getStringExtra("movie_path")+"</movie_path>\n" +
				    "</LiveProfile>\n";
				Log.d("BackGroundService","XML " + xml);
					FileOutputStream fos = new FileOutputStream(file.getPath());
					fos.write(xml.getBytes());
					fos.close();
					return 0;
				} catch (FileNotFoundException e) {
					e.printStackTrace();
					return -1;
				} catch (IOException e) {
					e.printStackTrace();
					return -3;
				}
		}

		/**
		 *
		 * 以下アラートに必要な処理
		 *
		 * メッセージ送信は一番最初以外、mServiceMsgrがnullになって送れなかった
		 */


		public static void prepareAlert() {
			Log.d("NLiveRoid","prepareAlert --- ");
			if(NLiveRoid.log)NLiveRoid.outLog("アラート準備開始---\n");
			//アラートがある場合は別プロセスをバインドしてバインドし合う
			if(bcService == null){
				Log.d("NLiveRoid","Failed prepareAlert");
				if(NLiveRoid.log)NLiveRoid.outLog("アラート準備失敗---\n");
				return;
			}else{
				Log.d("NLiveRoid","BCService prepareAlert Bind AlertHelper");
				if(aHelper != null){
					Message msg = new Message();
					msg.what = 2;
					try {
						aHelper.send(msg);
					} catch (RemoteException e) {
						// TODO 自動生成された catch ブロック
						e.printStackTrace();
					}
				}else{
					Intent ahelper = new Intent("nliveroid.nlr.main.AlertHelper");
					//別プロセスだとstartServiceするとバインドできない
					bcService.bindService(ahelper,
		                mConnection, Context.BIND_AUTO_CREATE);
				}
			}
			NLiveRoid app = (NLiveRoid)bcService.getApplicationContext();
			try{
				alert_interval = Byte.parseByte(app.getDetailsMapValue("alert_interval"));
			}catch(Exception e){
				e.printStackTrace();
				return;
			}
			if(alertList == null)alertList = new ArrayList<String>();
			if(alertList.size() > 0)return;//既に読み込まれていれば終了する
			new AsyncTask<Void,Void,Integer>(){
				@Override
				protected Integer doInBackground(Void... params) {
			try {
				FileInputStream fis = bcService.openFileInput("alertL");
				byte[] readBytes = new byte[fis.available()];
				fis.read(readBytes);
				int result = XMLparser.getAlertList(alertList,readBytes);
				if(NLiveRoid.log){
					String str = "";
					for(String i:alertList){
						str += i +"\n";
					}
					NLiveRoid.outLog("ターゲットリスト\n" + str);
				}
				fis.close();
				return result;
			} catch (FileNotFoundException e) {
				e.printStackTrace();
						try {
							bcService.openFileOutput("alertL", bcService.MODE_WORLD_READABLE).close();//ファイルを生成しておく
							return 1;
						} catch (FileNotFoundException e1) {
							e1.printStackTrace();
							return 1;
						} catch (IOException e1) {
							e1.printStackTrace();
							return -1;
						}
			} catch (IOException e) {
				e.printStackTrace();
				return -1;
			}
				}
				@Override
				protected void onPostExecute(Integer arg){
					if(arg < 0){
						MyToast.customToastShow(bcService, "アラート情報の取得失敗");
					}
				}
			}.execute();
//			Log.d("NLiveRoid","prepareAlert END--- ");
		}


	  	//まず定期処理をRegisterする
		public static int registerNextAlert() {
			Log.d("NLiveRoid","RegisterAlert --- " + bcService);
//			Log.d("NLiveRoid","AlertReceived --------------- " + context);
			if(bcService == null)return -1;
			if(NLiveRoid.log){
				SimpleDateFormat sdf = new SimpleDateFormat("MMdd HH:mm ss");
				NLiveRoid.outLog("アラートセット---" + sdf.format(new Date())+"\n");
			}
			 Intent intent = new Intent();
			 intent.setAction("return_f.NLR");//使いまわす
			 intent.putExtra("r_code",CODE.ALERT);
		        pendingIntent = PendingIntent.getBroadcast(bcService, 0, intent, 0);
		        AlarmManager am = (AlarmManager) bcService.getSystemService(ALARM_SERVICE);
		        am.set(AlarmManager.RTC_WAKEUP,
		        		System.currentTimeMillis() + alert_interval*60*1000, pendingIntent);//5分
				Log.d("NLiveRoid","RegisterAlert END ---- " + pendingIntent);
		        return 0;
		}


		public static int unRegisterAlert() {
		Log.d("NLiveRoid","unRegisterAlert -------- " + bcService);
		isFinish = true;
		if(NLiveRoid.log)NLiveRoid.outLog("アラート正常停止---\n");
        Message msg = new Message();
        msg.what = 1;
        try {
			aHelper.send(msg);//バインドし合っているサービスを終了する
		} catch (RemoteException e) {
			e.printStackTrace();
		}
			if(pendingIntent == null||bcService == null)return -1;
	        AlarmManager am = (AlarmManager) bcService.getSystemService(ALARM_SERVICE);
	        am.cancel(pendingIntent);
	        if(alertedList != null)alertedList.clear();
			return 0;
		}

		private void doAlert(Context context) {
			PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
	        wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "BackGroundReceiver");
	        wl.acquire();
            // ここでバックグラウンドでの処理を実行
	        new AlertParseTask().execute();
		}
		public class AlertParseTask extends AsyncTask<Void,Void,Integer>{
			private ErrorCode error;
			private boolean ENDFLAG = true;
			private ArrayList<LiveInfo> list;
			@Override
			protected Integer doInBackground(Void... params) {
//				Log.d("NLiveRoid","RECEIVE_ALERTREEEEEEE");
				//参加中コミュを取得
				NLiveRoid app = (NLiveRoid)getApplicationContext();
				//セッション取得
				error = app.getError();
				if(error == null)return -1;
				if(Request.getApp() == null)Request.setApp(app);
				String sessionid = Request.getSessionID(error);
				if(error.getErrorCode() != 0){
					return 0;
				}
				if(sessionid == null)return -2;
				InputStream source = Request.doGetToInputStreamFromFixedSession(sessionid, URLEnum.SMARTMY, error);
				if(error.getErrorCode() != 0){
					return -3;
				}
				if(source == null){
					error.setErrorCode(-8);
					return -4;
				}
				 try {
					  AlertParser handler = new AlertParser(this,error);
					  org.ccil.cowan.tagsoup.Parser parser = new org.ccil.cowan.tagsoup.Parser();
				        parser.setContentHandler(handler);
				        parser.parse(new InputSource(source));
				  } catch (org.xml.sax.SAXNotRecognizedException e) {
				      // Should not happen.
					  e.printStackTrace();
				      throw new RuntimeException(e);
				  } catch (org.xml.sax.SAXNotSupportedException e) {
				      // Should not happen.
					  e.printStackTrace();
				  } catch(UnknownHostException e){//接続悪い時になる
					 return -5;
					}catch (IOException e) {
					  e.printStackTrace();
				} catch (SAXException e) {
					e.printStackTrace();
					return -6;
				}
				long startT = System.currentTimeMillis();
				while(ENDFLAG){
					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
						ENDFLAG = false;
						e.printStackTrace();
						Log.d("NLiveRoid","AlertTask InterruptedException -------- " +e.getClass().getName() + " "+e.getMessage() +" " + e.getCause());
						return -7;
					}catch(IllegalArgumentException e){
						Log.d("NLiveRoid","IllegalArgumentException at AlertTask");
						e.printStackTrace();
						ENDFLAG = false;
						return -8;
					}
					if(System.currentTimeMillis()-startT>30000){
						//タイムアウト
						ENDFLAG = false;
						error.setErrorCode(-10);
						return -9;
					}
				}
				return 0;
			}
			public void finishCallBack(ArrayList<LiveInfo> list){
				if(NLiveRoid.isDebugMode)Log.d("NLiveRoid","AlertTask finishCallBack0");
				this.list = list;
				ENDFLAG = false;
			}
			public void finishCallBack(boolean isMaintenance){
				if(NLiveRoid.isDebugMode)Log.d("NLiveRoid","AlertTask finishCallBack1");
				error.setErrorCode(-5);
				ENDFLAG = false;
			}
			@Override
			protected void onPostExecute(Integer arg){
				Log.d("NLiveRoid","BCService alertedonPost --- arg:" + arg +" ErrorCode:" + (error == null? "Error was null":error.getErrorCode()));
				if(error == null){
					Notification notif = new Notification();
					notif.icon = R.drawable.alert_notificon;
					notif.icon = R.drawable.icon_nlr;
					notif.setLatestEventInfo(bcService, "アラートでエラーが発生しました", "内部エラー", null);
					NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
					nm.notify(0, notif);
				}else if(arg < 0 ||error.getErrorCode() != 0){
					if(arg == -3||arg == -9||error.getErrorCode() == -6){//UnknownHostか、IOエラー、タイムアウトの場合は継続することにする
						if(NLiveRoid.log)NLiveRoid.outLog("アラート通信失敗");
					}else{
						Log.d("NLiveRoid","BCService AlertERROR --- ");
						SimpleDateFormat sdf = new SimpleDateFormat("MMddHHmmss");
						if(NLiveRoid.log)NLiveRoid.outLog("アラートエラー:" + sdf.format(new Date()) + " code:" + arg +"\n");
						isFinish = true;
						Notification notif = new Notification();
						notif.icon = R.drawable.alert_notificon;
						notif.icon = R.drawable.icon_nlr;
						notif.setLatestEventInfo(bcService, "アラートでエラーが発生しました", "code:" + arg + " ecode:" + (error == null? "NULL":error.getErrorCode()), null);
						NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
						nm.notify(0, notif);
								//bcServiceをAlertDialogのコンテキストにとると、BadTokenException
//					new AlertDialog.Builder(bcService)
//					.setMessage("アラート用の通信に失敗しました\ncode1:"+arg+"\n code2:"+(error == null? "NULL":error.getErrorCode()))
//					.create().show();
					}
					//エラーを戻しておく
					if(error.getErrorCode() != 0)error.setErrorCode(0);
				}else if(list != null){
					Log.d("NLiveRoid","SuccessAlert --- " + list.size());
					if(alertedList == null)alertedList = new HashMap<String,Long>();
					//6時間以上経過しているものを消す
					Iterator<String> it = alertedList.keySet().iterator();
					String next = "";
					while(it.hasNext()){
						next = it.next();
						if(System.currentTimeMillis() - alertedList.get(next) > 7200000){
							Log.d("NLiveRoid","Remove alertedlist " + next +" " + System.currentTimeMillis() + " " + alertedList.get(next));
							alertedList.remove(next);
						}
					}
//					for(int d = 0; d < alertList.size(); d++){
//						Log.d("NLiveRoid","alertTarget:" + alertList.get(d));
//					}
					if(NLiveRoid.log){
						String str = "通知済み放送ID:件数"+alertedList.size() +"\n";
						for(int d = 0; d < alertedList.size(); d++){
							str += alertedList.get(d)+"\n";
						}
						NLiveRoid.outLog(str);
					}
					ArrayList<LiveInfo> alert = new ArrayList<LiveInfo>();
					for(int i = 0; i < list.size(); i++){
						Log.d("NLiveRoid","getLiveData LV " + list.get(i).getLiveID() + " co" +  list.get(i).getCommunityID());
						if(alertList.contains(list.get(i).getCommunityID()) && !alertedList.containsKey(list.get(i).getLiveID())){
							Log.d("NLiveRoid","Notify____" + list.get(i).getCommunityID() + "  " + list.get(i).getLiveID());
							alertedList.put(list.get(i).getLiveID(), System.currentTimeMillis());
							alert.add(list.get(i));
						}
					}
					if(alert.size() > 0){
						if(NLiveRoid.log)NLiveRoid.outLog("アラート通知 件数:"+alert.size()+"\n");
						try{
						NLiveRoid app = (NLiveRoid)getApplicationContext();
						NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
						boolean sound_notif = app.getDetailsMapValue("alert_sound_notif").equals("true");
						boolean vibration = app.getDetailsMapValue("alert_vibration_enable").equals("true");
						boolean led = app.getDetailsMapValue("alert_led").equals("true");
							for(int i = 0; i < alert.size();i++){
							Notification notify = new Notification();
							notify.icon = R.drawable.icon_nlr;
							notify.flags = Notification.FLAG_AUTO_CANCEL;
							if(vibration)notify.vibrate = new long[]{0,1000,1000,1000};
							if(led){
								notify.ledARGB = 0xff00ff00;//緑
								notify.ledOnMS = 1000;
								notify.ledOffMS = 1000;
								notify.flags |= Notification.FLAG_SHOW_LIGHTS; // LED点灯のフラグを追加する
							}
							if(sound_notif)notify.defaults = notify.defaults | Notification.DEFAULT_SOUND;
							Intent toptab = new Intent(getApplicationContext(), TopTabs.class);
							toptab.putExtra("scheme", alert.get(i).getLiveID());
							toptab.addFlags(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY);
							toptab.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
							PendingIntent pending = PendingIntent.getActivity(getApplicationContext(), 0, toptab, 0);
							notify.setLatestEventInfo(getApplicationContext(),alert.get(i).getTitle(),alert.get(i).getCommunityID()+"\n来場:"+ alert.get(i).getViewCount() + " コメ数:"+alert.get(i).getResNumber(),pending);
							nm.notify(i, notify);
							}
							alert_interval = Byte.parseByte(app.getDetailsMapValue("alert_interval"));
						}catch(Exception e){
							e.printStackTrace();
						}
					}
				}
				//通知がなくても次のアラートをセット
				registerNextAlert();
	        	releaseWakeLock();  // 最後に端末がスリープ状態に戻れるようにする。
			}
		}

		/**
		 * alertListを取得します。
		 * @return alertList
		 */
		public static ArrayList<String> getAlertList() {
		    return alertList;
		}
		public static void setAlertList(ArrayList<String> coList) {
			alertList = coList;
		}

//		public boolean isBinded(){
//			try {
////				Log.d("log","PRIM PID  --- " + android.os.Process.myPid());
//				if(app.getAidl() != null){
//				return app.getAidl().isBinded();
//				}
//			} catch (RemoteException e) {
//				e.printStackTrace();
//			}
//			return false;
//		}
	//
//		public void startAlertService(String sessionid) {
	//
//			//セッション取得しておく
//			if(sessionid == null){
//				error.setErrorCode(-17);
//				error.showErrorToast();
//				return;
//			}
//			//セッションOKなら
//			//サービスを起動する
//			//ServiceのonCreateでgetIntent()できないのでここで初期値を渡してバインド
//			String[] notifvalues = new String[5];
//			notifvalues[0] = sessionid;
//			notifvalues[1] = detailsMap.get("alert_interval");
//			notifvalues[2] = detailsMap.get("alert_sound_enable");
//			notifvalues[3] = detailsMap.get("alert_sound_uri");
//			notifvalues[4] = detailsMap.get("alert_vibration_enable");
//			//何故かAPPからしかBindできない
//			app.startAlertService(notifvalues);
//			new AlertStopListener().execute();
//		}
	//
	//
//		class AlertSession extends AsyncTask<Void,Void,String>{
//			@Override
//			protected String doInBackground(Void... arg0) {
//				error = app.getError();
//				String str = Request.getSessionID(error);
//				return str;
//			}
//			@Override
//			protected void onPostExecute(String arg){
//				if(arg != null){
//				startAlertService(arg);
//				}else{
////					alertenable.setChecked(false);
//					app.getError().showErrorToast();
//				}
//			}
	//
//		}

		/**
		 * アラート終了自前リスナ(1秒毎)
		 */
//		class AlertStopListener extends AsyncTask<Void,Void,Boolean>{
	//
//			@Override
//			protected Boolean doInBackground(Void... arg0) {
//				while(ListenerFlug){//ここでリッスンしてチェックはずすかPrimitiveSettingのオンポーズで終了する
//					try {
//						Thread.sleep(1000);
//						Log.d("Log","LISTEN---");
	//
//						if(!isBinded()){
//							return false;
//						}
//					} catch (InterruptedException e) {
//						e.printStackTrace();
//						ListenerFlug = false;
//					}catch(IllegalArgumentException e){
//						e.printStackTrace();
//						Log.d("NLiveRoid","IllegalArgumentException at Primitive AlertStopListener");
//						ListenerFlug = false;
//						break;
//					}
//				}
//				return false;
//			}
//			@Override
//			protected void onPostExecute(Boolean arg){
////				if(arg&&alertenable != null){
////				alertenable.setChecked(false);
////				}
//			}
//		}




}

