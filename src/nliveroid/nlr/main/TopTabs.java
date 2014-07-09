package nliveroid.nlr.main;

import nliveroid.nlr.main.BackGroundService.MyServiceBinder;
import android.app.Service;
import android.app.TabActivity;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieManager;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

public class TopTabs extends TabActivity
{
	private static TopTabs ACT;
	protected static boolean isFirstLogin = true;
	private Intent backGroundService;
	private BackGroundService bcService;
	private int finishDialogTcolor;
	public static HistoryDataBase his_db;
	public static byte his_value;
	static{
    	System.loadLibrary("enc");
    }
	public native int startTest();
	//別プロセスだとServiceConnection#onServiceConnectedで
	//渡されるIBinderオブジェクトからServiceの参照がとれないので
	//このオブジェクトをMessengerに食わせてMessengerインスタンスを生成
	//※ここが実行されるのはonCreateでコネクションが生成された後
	private ServiceConnection conn = new ServiceConnection() {
		public void onServiceConnected(ComponentName name, IBinder binder) {
			try{
			bcService = ((MyServiceBinder)binder).getService();
			Log.d("NLiveRoid","TopTabs onServiceConnected!! ----- " + bcService);
			}catch(Exception e){
				e.printStackTrace();
				ACT.finish(true);
			}
		}
		public void onServiceDisconnected(ComponentName name) {
			bcService = null;
		}
	};
	private TimeShiftDialog tsDialog;
	/**
	 * それぞれのクラスでタブを生成しビューにセットする
	 * @author Owner
	 *
	 */
	  @Override
	  public void onCreate(Bundle bundle){
		  super.onCreate(bundle);
	    NLiveRoid app = (NLiveRoid)getApplicationContext();
	    app.initStandard();
	    if(app.getError().getErrorCode() != 0){
	    	app.getError().showErrorToast();
	    }
	    try{
	    finishDialogTcolor = app.getDetailsMapValue("toptab_tcolor")==null? 0:Integer.parseInt(app.getDetailsMapValue("toptab_tcolor"));
	    }catch(Exception e){
	    	e.printStackTrace();
	    }
	    ACT = this;
	    requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
	    setVolumeControlStream(AudioManager.STREAM_MUSIC);
	    LayoutInflater factory = LayoutInflater.from(this);

	    View parent = factory.inflate(R.layout.common_tablayout , null);
	    setContentView(parent);

	    TabHost topTab = this.getTabHost();
	    Intent communityIntent = new Intent(this, CommunityTab.class);
	    Intent searchIntent = new Intent(this, SearchTab.class);
	  //URIで呼ばれた
		if(getIntent().getStringExtra("scheme")!= null){
			communityIntent.putExtra("scheme", getIntent().getStringExtra("scheme"));//連携起動された
		}
		//最初に開くタブの設定
		int lastTab = 0;
		try{
			lastTab = Integer.parseInt(app.getDetailsMapValue("last_tab"));
//    		Log.d("NLiveRoid","setTab---XX " + lastTab);
			if((lastTab & 0x10) > 0){//上の4ビットは0が裏じゃなくても0だから1から各タブ+1裏ってことにする
			//参加コミュ一の裏
				communityIntent.putExtra("sole",true );
			}
			if((lastTab & 0x20 ) > 0){//PC版検索
				searchIntent.putExtra("sole",true );
			}
		}catch(Exception e){
			e.printStackTrace();
			app.setDetailsMapValue("last_tab", "0");
			topTab.setCurrentTab(1);
		}

	    TabSpec tabSpec0 = topTab.newTabSpec("top_tab1").setContent(communityIntent).setIndicator("参加中コミュニティ");
	    topTab.addTab(tabSpec0);
	    TabSpec tabSpec1 = topTab.newTabSpec("top_tab2").setContent(searchIntent).setIndicator("検索");
	    topTab.addTab(tabSpec1);

	    try{//履歴タブ
	    if(Boolean.parseBoolean(app.getDetailsMapValue("enable_his"))){
	    	Intent  historyIntent = new Intent(this, HistoryTab.class);
		if((lastTab & 0x40) > 0){
			historyIntent.putExtra("sole",true );
		}
		TopTabs.his_value = Byte.parseByte(app.getDetailsMapValue("his_value"));
	    TabSpec tabSpec2 = topTab.newTabSpec("top_tab3").setContent(historyIntent).setIndicator("履歴");
	    topTab.addTab(tabSpec2);
	    }
	    }catch(Exception e){
	    	e.printStackTrace();
	    }
	    //配信タブ ここと設定のレイアウト部分とカメラのパーミッションをコメントアウト
	    ///*
	    Log.d("NLiveRoid","ENABLE_BC " + app.getDetailsMapValue("enable_bc"));
		 if(Boolean.parseBoolean(app.getDetailsMapValue("enable_bc"))){
				Intent liveIntent = new Intent(this, LiveTab.class);
			 TabSpec tabSpec3 = topTab.newTabSpec("top_tab4").setContent(liveIntent).setIndicator("配信");
		    topTab.addTab(tabSpec3);
		 }


	    if(getIntent().getStringExtra("scheme") != null){
	    	topTab.setCurrentTab(0);
	    }else{
	    	try{
				if(NLiveRoid.isDebugMode)Log.d("NLiveRoid","TOPlast_tab " + lastTab +  " "+Integer.toBinaryString(lastTab) + " HISVALUE "+ TopTabs.his_value + " " + Integer.toBinaryString(TopTabs.his_value));
	    	topTab.setCurrentTab(lastTab & 0x03);
	    	}catch(Exception e){
	    		e.printStackTrace();
	    		topTab.setCurrentTab(1);
	    	}
	    }
	    	new StartService().execute();//裏でやら無いと起動時に黒画面が長くなる?

//	    	LiveSettings ls = new LiveSettings(new String[]{
//           			"-version","00000000","-live","-host","nlpoca53.live.nicovideo.jp",	"-app",
//           			"publicorigin/121125_23_1"+"?"+
//   		"1267289:lv116734493:4:1353855299:0:1353855562:24b2aa668f6f0461",
//           			"lv116734493",
//           			"/sdcard/result.flv"
//           			});
//	    	MetadataAmf0 meta = MetadataAmf0.createMetaData(ls);
//	    	String meta_str = meta.toString();
//	    	Log.d("NLiveRoid","MetaStr:"+meta_str);
//	    	byte[] metadata = meta_str.getBytes();
//	    	Log.d("NLiveRoid","TEST_META"+Utils.toHex(metadata, 0, metadata.length, true));
//	    	String test_data = Utils.fromHexStringToString("02 00 0A 6F 6E 4D 65 74 61 44 61 74 61 08 00 00 00 0B 00 08 64 75 72 61 74 69 6F 6E 00 40 59 A6 97 8D 4F DF 3B 00 05 77 69 64 74 68 00 40 74 00 00 00");
//	    	Log.d("NLiveRoid","TOSTR:" + test_data);
	  }

	  /*
	   * onMetaData:[{duration=3600, width=480, height=320, videocodecid=2.0, audiocodecid=2.0, framerate=10, audiosamplerate=44100.0, encoder=NLR}]

D/NLiveRoid( 1218): TEST_META6F 6E 4D 65 74 61 44 61 74 61 3A 5B 7B 64 75 72 61 74 69 6F 6E 3D 33 36 30 30 2C 20 77 69 64 74 68 3D 34 38 30 2C 20 68 65 69 67 68 74 3D 33 32 30 2C 20 76 69 64 65 6F 63 6F 64 65 63 69 64 3D 32 2E 30 2C 20 61 75 64 69 6F 63 6F 64 65 63 69 64 3D 32 2E 30 2C 20 66 72 61 6D 65 72 61 74 65 3D 31 30 2C 20 61 75 64 69 6F 73 61 6D 70 6C 65 72 61 74 65 3D 34 34 31 30 30 2E 30 2C 20 65 6E 63 6F 64 65 72 3D 4E 4C 52 7D 5D
	   */
	  class StartService extends AsyncTask<Void,Void,Void>{
		@Override
		protected Void doInBackground(Void... params) {
			//裏サービスの起動
	    	backGroundService = new Intent(ACT,BackGroundService.class);
	    	startService(backGroundService);
			bindService(backGroundService, conn , Service.BIND_AUTO_CREATE);
			return null;
		}
	  }

	  public void changeTag(int param){
		  this.getTabHost().setCurrentTab(param);
	  }

	  @Override
	  public void onResume(){
		  super.onResume();
		  if(NLiveRoid.isDebugMode)Log.d("NLiveRoid","onResume TOP ----- ");
			  try{
		    byte his_value = Byte.parseByte(((NLiveRoid)getApplicationContext()).getDetailsMapValue("his_value"));

			    if((his_value & 0x40) > 0 && (his_db == null || !his_db.getDB().isOpen())){
			    his_db = new HistoryDataBase(this);
			    his_db.getWritableDatabase();
			    }
			  }catch(Exception e){
				  e.printStackTrace();
				  MyToast.customToastShow(this, "履歴の取得に失敗しました");
			  }
	  }

	  @Override
	  public void onStart(){
		  super.onStart();
		  Log.d("NLiveRoid","TOP onSTART ---- ");
	  }

	  @Override
	  public void onStop(){
		//セッションIDを削除
		  NLiveRoid app = (NLiveRoid)getApplicationContext();
		  CookieManager.getInstance().removeAllCookie();
		  app.setSessionid("");
		  app.setDetailsMapValue("sp_session", null);
		  app.updateAccountFile();
		  app.updateDetailsFile();//前回の値などを保存
			CommunityTab.cancelMovingTask();//一旦全てキャンセル
			SearchTab.cancelMoveingTask();
			LiveTab.cancelMovingTask();
			try{
		  super.onStop();
			}catch(NullPointerException e){
				e.printStackTrace();
				MyToast.customToastShow(ACT, "タブ画面を正しく初期化できませんでした\nJB以上専用アプリ等の影響が考えられます");
			}
	  }

	  @Override
	  public void onDestroy(){
		  //TopTabsは自然に終わるので、ここで呼ぶ
		  if(NLiveRoid.isDebugMode)Log.d("NLiveRoid","TopTab DDD ---");
		  if(his_db != null && his_db.getDB().isOpen())his_db.close();
		  try{
			  //アラートに関係なくサービス案バインド
			  unbindService(conn);//unbindして参照が1つも無くなるとService#onDestroy()は呼ばれちゃう
			  if(((NLiveRoid)getApplicationContext()).getDetailsMapValue("alert_enable").equals("false")){
				  BackGroundService.isFinish = true;
				  stopService(backGroundService);
				  }
				if(NLiveRoid.log){
					NLiveRoid.outLog("NLiveRoid ログ取得終了\n");
					NLiveRoid.logChannel.close();
				}
		  }catch(Exception e){
			  e.printStackTrace();
		  }
		  Request.disPoseAPP();
		  super.onDestroy();
	  }


	  public void finish(boolean isDialog){
		 if(NLiveRoid.isDebugMode)Log.d("NLiveRoid","Topfinish " + isDialog);
			CommunityTab.cancelMovingTask();//一旦全てキャンセル
			SearchTab.cancelMoveingTask();
			LiveTab.cancelMovingTask();
			((NLiveRoid)getApplicationContext()).setSessionid("");//必須
			isFirstLogin = true;
			super.finish();
		}

		@Override
		public void finish(){
			new FinishDialog(this,0,finishDialogTcolor).show();
		}

	  @Override
		public boolean onCreateOptionsMenu(Menu menu) {
		  MenuInflater mInflater = getMenuInflater();
		  mInflater.inflate(R.menu.menu_top,menu);
		  Log.d("NLiveRoid","ON MENU --- ");
			long start = System.currentTimeMillis();
			startTest();
			Log.d("NLiveRoid","TIME ------------ "+(System.currentTimeMillis() -start));

	        return super.onCreateOptionsMenu(menu);
	  }
	  @Override
	    public boolean onOptionsItemSelected(MenuItem item) {
	        boolean ret = true;
	        switch (item.getItemId()) {
	        default:
	            ret = super.onOptionsItemSelected(item);
	            break;
	        case R.id.top_menu_item0:
	        	int nowTab = getTabHost().getCurrentTab();
	        	if(nowTab==0){
	        		if(CommunityTab.getCommunityTab() != null)CommunityTab.getCommunityTab().onReload();
	        	}else if(nowTab == 1){
	        		if(SearchTab.getSearchTab() != null)SearchTab.getSearchTab().onReload();
	        	}else if(nowTab == 2){
		        	if(HistoryTab.getHistoryTab() != null){
		        		HistoryTab.getHistoryTab().onReload();
		        	}else if(LiveTab.getLiveTab() != null){
		        		LiveTab.getLiveTab().onReload();
		        	}
	        	}else if(nowTab == 3){
	        		if(LiveTab.getLiveTab() != null)LiveTab.getLiveTab().onReload();
	        	}
	            break;
	        case R.id.top_menu_item2:
	        	NLiveRoid app1 = (NLiveRoid)this.getApplicationContext();
	        	new TimeShiftDialog(this,app1.getError()).showSelf();
	        	break;
	        case R.id.top_menu_item1:
	        	Intent settingIntent = new Intent(this.getApplicationContext(),SettingTabs.class);
	        	NLiveRoid app = (NLiveRoid)this.getApplicationContext();
	        	settingIntent.putExtra("session",app.getSessionid());
	        	startActivityForResult(settingIntent, CODE.REQUEST_SETTING_TAB);
	            break;
	        }
	        return ret;
	    }


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		//全てのウィンドウとビューのサイズ(基準)はここで決まる
		NLiveRoid app = (NLiveRoid)getApplicationContext();
	      DisplayMetrics metrics = new DisplayMetrics();
			Display disp = getWindowManager().getDefaultDisplay();
			disp.getMetrics(metrics);
			float scaleDensity = metrics.scaledDensity;
			int widthPx = getWindow().getDecorView().getWidth();
			int heightPx = getWindow().getDecorView().getHeight();
			if(widthPx > heightPx){
				int temp = widthPx;
				widthPx = heightPx;
				heightPx = temp;
			}
			app.setMetrics(scaleDensity);
			int widthDp = (int) (widthPx/scaleDensity);
			int heightDp = (int) (heightPx/scaleDensity);
			app.setResizeW((int) (widthDp*1.72D));
			app.setResizeH((int) (heightDp*1.8D));
			app.setViewWidthDp((int) widthDp);
			app.setViewHeightDp((int) heightDp);
			app.createGateInstance();
	}

    public BackGroundService getBackGroundService(){
    	return bcService;
    }

    //裏に同一の放送がプレイされているかどうかを返す
    public boolean isMovingSameLV(String lv){
    	if(bcService == null)return false;
    	String Lv = bcService.getLiveID();
    	if(Lv == null || Lv.equals(""))return false;
    	return Lv.equals(lv);
    }

    @Override
    public void onNewIntent(Intent intent){
    	super.onNewIntent(intent);
    	if(intent.getStringExtra("scheme")!= null){
			//セッションIDを削除
			  NLiveRoid app = (NLiveRoid)getApplicationContext();
			  CookieManager.getInstance().removeAllCookie();
			  app.setSessionid( "");
			CommunityTab.getCommunityTab().schemeCalled(intent.getStringExtra("scheme"));
			getTabHost().setCurrentTab(0);
		}
    }


    @Override
	public void onConfigurationChanged(Configuration newConfig){
    	super.onConfigurationChanged(newConfig);
    	if(tsDialog != null && tsDialog.isShowing()){
    		tsDialog.onConfigChanged(this);
    	}
    }
	/**
	 * ACTを取得します。
	 * @return ACT
	 */
	public static TopTabs getACT() {
	    return ACT;
	}

	public static void setTextColor(TextView tview, int tcolor) {
		switch(tcolor){
		case 0:
			tview.setTextColor(Color.GRAY);
			break;
		case 1:
			tview.setTextColor(Color.BLACK);
			break;
		case 2:
			tview.setTextColor(Color.WHITE);
			break;

		}
	}
	public static void setIsFirstLogin(boolean b) {
		isFirstLogin  = b;
	}

	public static void insertHis(int kind, String lv, String coch, String remark0,String remark1,String remark2) {
		Log.d("NLiveRoid","Called insertHis :" + his_db +"  "  + his_value);
		if(ACT == null)return;//視聴画面から
	    byte his_value = Byte.parseByte(((NLiveRoid)ACT.getApplicationContext()).getDetailsMapValue("his_value"));//毎回これこうなっちゃうのか。。
		if(his_db != null && his_db.getDB() != null &&  (his_value & 0x40) > 0){//ID KIND TIME LV CO CO_TITLE LV_TITLE CO_DESC OTHER
			Log.d("NLiveRoid","INSERTHIS " + remark2);
			remark2 = remark2.replaceAll("<<LINK|LINK>>|<b>|<i>|<s>|<u>|<a>|/>|<br>|<font.+?>|</font>", "");//タグは小文字しかないはず?BRはあるかも?
				ContentValues val = new ContentValues();
				val.put("DATE",System.currentTimeMillis());
				val.put("KIND", kind);
				val.put("LV", lv);
				val.put("COCH", coch);
				val.put("REMARK0", remark0);
				val.put("REMARK1", remark1);
				val.put("REMARK2", remark2);

				long returnVal = his_db.getDB().insert("his", null, val);
				Log.d("NLiveRoid","HIS DB INSERT RETURN :" + returnVal);
				if(returnVal > 0){
				//超えてる分を消す処理
				Cursor c = his_db.getDB().query("his", new String[] { "ID" }, "KIND = "+kind, null, null, null, "ID ASC");
				Log.d("NLiveRoid", "KIND: " + kind + " RowCount " + c.getCount());
				//そのKINDが20件を超えていたら消す
				if(c.getCount() > 20){
					//IDは別のKINDのIDが間にあるので、飛んでいる可能性がある
					int deleteCount = c.getCount() - 20;//消す件数を決定する
					 boolean isEOF = c.moveToFirst();
					 Log.d("NLiveRoid"," Update isEOF " + isEOF);
						 long id = -1;
						 for(int i = 0; i < deleteCount  && isEOF; i++){
								id = c.getLong(0);
								Log.d("NLiveRoid","DELETE ID: " + id);
								returnVal = his_db.getDB().delete("his", "ID = ?" , new String[]{String.valueOf(id)});//そのIDのデータを削除
								Log.d("NLiveRoid","HIS DB DELETE RETURN :" + returnVal);
								if(returnVal < 0)break;
								isEOF = c.moveToNext();
						 }
				}
				c.close();
				}
				if(returnVal < 0){
					ACT.runOnUiThread(new Runnable(){
						@Override
						public void run(){
							MyToast.customToastShow(ACT, "履歴の保存に失敗");
						}
					});
				}
		}
	}

	public static void removeDBAll() {
		if(his_db == null){
			 his_db = new HistoryDataBase(ACT);
			 his_db.getWritableDatabase();
		}
			his_db.deleteTable();
		    his_db = new HistoryDataBase(ACT);
		    his_db.getWritableDatabase();

	}





	}