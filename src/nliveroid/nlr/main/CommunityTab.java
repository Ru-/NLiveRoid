package nliveroid.nlr.main;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nliveroid.nlr.main.parser.AllCommunityParser;
import nliveroid.nlr.main.parser.MypageParser;
import nliveroid.nlr.main.parser.XMLparser;

import org.apache.http.ParseException;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParserException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * getView()が画面に表示されている要素数分だけ呼ばれる
 * @author Owner
 *
 */
public class CommunityTab extends FragmentActivity implements Archiver,GatableTab{
	private static MyArrayAdapter adapter;
	private LayoutInflater inflater;
	private static CommunityTab ACT;
	private View parent;
	private nliveroid.nlr.main.ErrorCode error;

	private ViewGroup progressArea;
	private ProgressBar progressBar;


	private EditText lvet;
	private static TopParseTask updateListTask;
	private static LvURLTask lvURLTask;


	private ListView listview;

	private static boolean schemeCalled = false;

	private Gate gate;

	private static boolean isListTaped = false;
	private static boolean lvURLcancelled;

	private byte toptab_tcolor = 0;

	private AlertDialog accountDialog;

	private LinearLayout tabHeader;
	private TextView headerText;
	private LinearLayout movePaneRoot;
	private LinearLayout liveCommunityPane;
	private ViewPager pagerView;
	private MyPagerAdapter pagerAdapter;
	private ArrayList<ProgressBar> fragmentProgress;
	private ArrayList<ListView> fragmentListViews;
	private ArrayList<AllCommunityListAdapter> fragmentListAdapters;
	private boolean isPagerViewing;
	private boolean isFristPager = true;
	private AllCommunityTask allcommunityTask;
	private int communityAmount;
	private int nowSelectPage;
	private boolean isContextOperation = false;
	private ArrayList<LinearLayout> allFragmentViews;

	private boolean isAlertMode = false;

	@Override
	public void onCreate(Bundle bundle){
		super.onCreate(bundle);
		ACT = this;
		requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		inflater = LayoutInflater.from(this);
		parent = inflater.inflate(R.layout.communitytab, null);
		NLiveRoid app = (NLiveRoid) getApplicationContext();
		error = app.getError();
		//背景にヘッダーをセットするか判定する
		headerText = (TextView) parent.findViewById(R.id.community_titletext);
		toptab_tcolor = app.getDetailsMapValue("toptab_tcolor") == null? 0:Byte.parseByte(app.getDetailsMapValue("toptab_tcolor"));
		TopTabs.setTextColor(headerText,toptab_tcolor);
		//全てのコミュニティの一覧初期化
		movePaneRoot = (LinearLayout)parent.findViewById(R.id.comm_movepane_root);
		liveCommunityPane = (LinearLayout)parent.findViewById(R.id.comm_live_pane);
		pagerView = new ViewPager(this);
		pagerView.setPadding(0, 0, 0, 100);//100でいいのか?丁度いいけども
		pagerView.setId(100);
		pagerView.setOnPageChangeListener(new OnPageChangeListener(){
			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}
			@Override
			public void onPageSelected(int arg0) {
//				Log.d("SELECTED ", " " + arg0 + " " + fragmentProgress.get(arg0).getVisibility() +" " + View.INVISIBLE + " " + View.VISIBLE +" " + View.GONE);
				nowSelectPage = arg0;
				if(fragmentListViews.get(arg0) == null || fragmentProgress.get(arg0).getVisibility() == View.VISIBLE){
				 if(allcommunityTask != null && allcommunityTask.getStatus() != AsyncTask.Status.FINISHED){
			        	allcommunityTask.cancel(true);
			        }
			        allcommunityTask = new AllCommunityTask(arg0+1);
			        allcommunityTask.execute();
				}
				if(headerText != null){
			headerText.setText("参加中一覧 "+ (arg0*30)+URLEnum.HYPHEN+ (arg0*30+30 > communityAmount? communityAmount:arg0*30+30) + "/" + communityAmount);
				}
			}
		});
		fragmentProgress = new ArrayList<ProgressBar>();
		fragmentListViews = new ArrayList<ListView>();
		allFragmentViews = new ArrayList<LinearLayout>();
		fragmentListAdapters = new ArrayList<AllCommunityListAdapter>();
        pagerAdapter = new MyPagerAdapter(getSupportFragmentManager());
		tabHeader = (LinearLayout)parent.findViewById(R.id.commu_titlebar);
		tabHeader.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				if(isPagerViewing){
					movePaneRoot.removeAllViews();
					movePaneRoot.addView(liveCommunityPane);
					headerText.setText("参加中コミュニティ");
					isPagerViewing = false;
					if(adapter != null && adapter.isEmpty()){
						onReload();
					}
				}else{
					BackGroundService.prepareAlert();
					//参加中一覧のレイアウトを変更
					movePaneRoot.removeAllViews();
					movePaneRoot.addView(pagerView,new LinearLayout.LayoutParams(-1,-1));
					if(isFristPager){//初回だったらまず1ページ目をセット
					PageItem popular = new PageItem();
			        pagerAdapter.addItem(popular);
			        pagerView.setAdapter(pagerAdapter);
			        if(allcommunityTask != null && allcommunityTask.getStatus() != AsyncTask.Status.FINISHED){
			        	allcommunityTask.cancel(true);
			        }
			        allcommunityTask = new AllCommunityTask(1);
			        allcommunityTask.execute();
					}
//					if(headerText != null){//今選択されているページが取得できないのでこれできない
//						headerText.setText("MyCommunity 0-"+ (allFragments.size() >= 2? 30:communityAmount));
//							}
					isPagerViewing = true;
				}
			}

		});
		try{//背景画像をセットする
			FileInputStream back_t_file  = openFileInput("back_t");
			Bitmap back = BitmapFactory.decodeStream(back_t_file);
			parent.setBackgroundDrawable(new BitmapDrawable(back));
		} catch (FileNotFoundException e) {
			tabHeader.setBackgroundDrawable(getResources().getDrawable(R.drawable.header));
//			e.printStackTrace();
		}catch(OutOfMemoryError e){
			e.printStackTrace();
			MyToast.customToastShow(this, "背景画像が大きすぎたため、適用に失敗しました");
		}catch(Exception e){
			e.printStackTrace();
			MyToast.customToastShow(this, "背景画像適用時エラー");
		}

	    progressArea = (ViewGroup)parent.findViewById(R.id.progresslinear);
	    progressBar = (ProgressBar)parent.findViewById(R.id.ProgressBarHorizontal);
	    progressArea.removeView(progressBar);
		View pParent = inflater.inflate(R.layout.progressbar, null);
		progressBar = (ProgressBar) pParent
		.findViewById(R.id.ProgressBarHorizontal);

	    adapter = new MyArrayAdapter(parent.getContext());
	    listview = (ListView)parent.findViewById(android.R.id.list);
		listview.setAdapter(adapter);
		listview.setDrawingCacheEnabled(false);
		registerForContextMenu(listview);
		listview.setOnItemClickListener(new OnItemClickListener() {
			 @Override
			 public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				 if(NLiveRoid.isDebugMode)Log.d("NLiveRoid","ConItemClick" + isListTaped);
				 if(gate != null){
					 if(!gate.isOpened()){
						 startFlashPlayer(adapter.getItem(position));
					 	}
				 }else{
				 	startFlashPlayer(adapter.getItem(position));
				 }
			 }
		});



		Button lvbt = (Button)parent.findViewById(R.id.button_lvurl);
		lvbt.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {

				if(gate == null || !gate.isOpened()){
					SearchTab.cancelMoveingTask();
					LiveTab.cancelMovingTask();
					cancelMovingTask();
					addProgress();
					progressBar.setProgress(20);
					lvURLTask = new LvURLTask();
					lvURLTask.execute();
				}
			}
		});

		Button reloadBt = (Button)parent.findViewById(R.id.commu_reload_bt);
		reloadBt.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				ACT.onReload();
			}
		});
		final Button settingBt = (Button)parent.findViewById(R.id.right_setting_bt);
		settingBt.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Intent settingIntent = new Intent(ACT,SettingTabs.class);
	        	NLiveRoid app = (NLiveRoid)ACT.getApplicationContext();
	        	settingIntent.putExtra("session",app.getSessionid());
	        	startActivityForResult(settingIntent, CODE.REQUEST_SETTING_TAB);
			}
		});
		//lv又はURLの設定
				lvet = (EditText)parent.findViewById(R.id.edit_lvurl);
				lvet.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			        @Override
			        public void onFocusChange(View v, boolean hasFocus) {
			            // EditTextのフォーカスが外れた場合
			            if (hasFocus == false) {
			                // ソフトキーボードを非表示にする
			                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
			                imm.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
			            }
			        }
			    });
		FrameLayout fl = (FrameLayout)parent.findViewById(R.id.frame_lvurl);
		fl.setPadding(0, 150, 0,0);


		//裏タブだったら参加中一覧をレイアウトする
		Intent getIntent = getIntent();
	    //URIから呼ばれたか
		schemeCalled = getIntent.getStringExtra("scheme")==null? false:true;
		if(!schemeCalled && getIntent.getBooleanExtra("sole", false)){
			BackGroundService.prepareAlert();
			movePaneRoot.removeAllViews();
			movePaneRoot.addView(pagerView,new LinearLayout.LayoutParams(-1,-1));
			PageItem popular = new PageItem();
	        pagerAdapter.addItem(popular);
	        pagerView.setAdapter(pagerAdapter);
	        isPagerViewing = true;
		}
	    setContentView(parent);

	}


	@Override
	public void onWindowFocusChanged(boolean hasFocus){
		super.onWindowFocusChanged(hasFocus);
		//設定しないと長い文字列を入力するとボタンが見えなくなっちゃう
		lvet.setWidth(((NLiveRoid)getApplicationContext()).getViewWidth()/3);
		if(schemeCalled){
			SearchTab.cancelMoveingTask();
			LiveTab.cancelMovingTask();
			cancelMovingTask();
			addProgress();
			progressBar.setProgress(20);
			lvet.setText(getIntent().getStringExtra("scheme"));
			//このタイミングで行かないと、いろいろおかしくなる
			schemeCalled = false;
			lvURLTask = new LvURLTask();
			lvURLTask.execute();
		}
	}

	class MyInputFilter implements InputFilter{
		private int nowLength;
		@Override
		public CharSequence filter(CharSequence source, int start, int end,
				Spanned dest, int dstart, int dend) {
//			Log.d("PPPPP","filter " + nowLength + " " + dest.length());
			if(nowLength > dest.length()&&dest.length() - nowLength > 3){//3文字以上
				lvet.setWidth(((NLiveRoid)getApplicationContext()).getViewWidth()/3);
			}
			return null;
		}
	}

	class LvURLTask extends AsyncTask<Void,Void,String>{
		private LiveInfo liveinfo;
		private boolean isTs;
		@Override
		public void onCancelled(){
			removeProgress();
			super.onCancelled();
		}
		@Override
		protected String doInBackground(Void... arg0) {
			//エスケ
			final String inputStr = lvet.getText().toString();
//			lvet.setFilters(new InputFilter[]{new MyInputFilter()});
			progressBar.setProgress(30);
			if(inputStr == null || inputStr.equals("")){
				return "inputError";
			}else{
				if(inputStr.startsWith("ts")){
					isTs = true;
					ACT.runOnUiThread(new Runnable(){
						@Override
						public void run() {
							lvet.setText(inputStr.replace("ts", ""));
						}
					});
				}
				Matcher lvmc = Pattern.compile("lv[0-9]{1,16}").matcher(inputStr);//find()は2回目だとfalseだけどgroup()は何回でも返ってくる
				Matcher cochmc = Pattern.compile("co[0-9]{1,16}|ch[0-9]{1,16}").matcher(inputStr);
				try {
				byte[] source = null;
					liveinfo = new LiveInfo();
					if(error != null && error.getErrorCode() != 0){
						return null;
					}else{
						//失敗後も詳細表示する為にここでlvをセットしちゃう→gateはcoでもchでも行けることが判明(getplayerだとclosedの場合も行けるが、何も情報が得られない)
						if(lvmc.find()){
						liveinfo.setLiveID(lvmc.group());
								if(cochmc.find()){//lvとcoを同時に指定するような変なURL
									liveinfo.setCommunityID(cochmc.group());
								}
						}else if(cochmc.find()){
							liveinfo.setLiveID(cochmc.group());
							liveinfo.setCommunityID(cochmc.group());
						}else{
							return "inputError";
						}
						source = Request.getPlayerStatusToByteArray(liveinfo.getLiveID(),  error);
						if(error != null && error.getErrorCode() != 0){
							return null;
						}
						progressBar.setProgress(70);
							String code = XMLparser.getLiveInfoFromAPIByteArray(source,liveinfo);//ここで<id>があればsetLiveIDされる
							if(code != null && !code.equals("")){
								if(code.trim() == null){
									error.setErrorCode(-37);//期待値と異なるデータで\nエラーが発生しました
									return null;
								}
									return code.trim();
							}else if(liveinfo != null&&liveinfo.getLiveID() != null){
										return liveinfo.getLiveID();
							}else{//予期せぬエラー
								error.setErrorCode(-43);
								return null;
							}
					}
				} catch (NullPointerException e1) {
					e1.printStackTrace();
					error.setErrorCode(-37);
					return null;//画面は落とさない
				} catch (ParseException e1) {
					e1.printStackTrace();
				} catch (XmlPullParserException e1) {
					e1.printStackTrace();
					error.setErrorCode(-30);
					return null;//画面は落とさない
				} catch (IOException e1) {
					e1.printStackTrace();
					error.setErrorCode(-37);
					return null;//画面は落とさない
				}
			}
			return null;

		}
		@Override
		protected void onPostExecute(String returnStr){
			progressBar.setProgress(100);
			removeProgress();
			//returnStrがnullの場合は入力がないとする
			if(error != null && error.getErrorCode() != 0){
				error.showErrorToast();
			}else if(returnStr.equals("inputError")){
				MyToast.customToastShow(ACT, "入力に誤りがあります");
			}else{
				// ソフトキーボードを非表示にする
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(parent.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
				if(returnStr.equals("maybe_delete")){
				MyToast.customToastShow(ACT, "情報取得に失敗(放送終了か削除されている)");
				}else{
					Log.d("NLiveRoid","TS failed " + returnStr);
						String reason = "不明";
							if(returnStr.equals("notfound")){
								MyToast.customToastShow(ACT, "放送がみつかりませんでした");
							}else if(returnStr.equals("notlogin")){
								MyToast.customToastShow(ACT, "ログイン失敗");
							}else if(returnStr.equals("closed")){
								if(isTs){
								reason = "放送終了後、TSを検出できませんでした\n終了直後か一般会員又はTS非対応放送";
								}else{
								reason = "放送が終了していました";
								}
								showGateOrCoDialog(liveinfo,reason);
							}else if(returnStr.equals("comingsoon")){
								reason = "予約枠でした";
								showGateOrCoDialog(liveinfo,reason);
							}else if(returnStr.equals("require_community_member")){
								reason = "コミュニティ限定放送";
								showGateOrCoDialog(liveinfo,reason);
							}else if(returnStr.equals("incorrect_account_data")){
								reason = "アカウント情報に誤りがあるか、解析中案件なので非対応です";
								showGateOrCoDialog(liveinfo,reason);
							}else if(returnStr.equals("timeshift_ticket_exhaust")){
								reason = "チケットが必要な有料放送等でした(NLR非対応)";
								showGateOrCoDialog(liveinfo,reason);
							}else if(returnStr.equals("usertimeshift")){
								reason = "コミュニティ限定放送";
								showGateOrCoDialog(liveinfo,reason);
							}else if(returnStr.equals("noauth")){
								reason = "放送が終了していました";
								showGateOrCoDialog(liveinfo,reason);
							}else{
								startFlashPlayer(liveinfo);
								return;
							}
				}
			}
			schemeCalled = false;
		}
		private void showGateOrCoDialog(final LiveInfo info,String message){
			Log.d("NLiveRoid"," " + info.getLiveID() + " " + info.getCommunityID() + " " + info.getCommunityName());
			new AlertDialog.Builder(ACT)
			.setMessage(message + "\n放送詳細を表示しますか?")
			.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {}
			})
			.setPositiveButton("YES", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					showGate(info);
				}
			}).create().show();
		}
	}


	@Override
	public void onConfigurationChanged(Configuration newConfig){
		super.onConfigurationChanged(newConfig);
		if(gate != null &&  gate.isOpened()){
			gate.onConfigChanged(newConfig);
		}
	}


	public static void cancelMovingTask(){
//		if(isFirstThread)return;
		if(updateListTask != null&&updateListTask.getStatus()!=AsyncTask.Status.FINISHED){
			updateListTask.cancel(true);
			updateListTask = null;
		}
		if(lvURLTask != null && lvURLTask.getStatus() != AsyncTask.Status.FINISHED){
			lvURLTask.cancel(true);
			lvURLcancelled = true;
		}
	}




    class AllCommunityListAdapter extends ArrayAdapter<LiveInfo> {
    	private final String level = "レベル ";
    	private final String member = "メンバー ";
    	private final String movie = "投稿動画 ";
    	private final String brocas = "放送権限 ";
    	private final String bsp = "BSP権限 ";
    	private final String ari = "有り ";
    	private final String nasi = "なし";
		public ArrayList<String> alertList = BackGroundService.getAlertList();
        public AllCommunityListAdapter(Context context, ArrayList<LiveInfo> list) {
            super(context,R.layout.list_row_allcommunity);
        }
        @Override
        public View getView(int position, View view, ViewGroup parent) {
        	LiveInfo item = getItem(position);
            final ViewHolder holder;
            if (view == null) {
                view = inflater.inflate(R.layout.list_row_allcommunity, null);
                holder = new ViewHolder();
                holder.thumbnail = (ImageView) view.findViewById(R.id.all_community_thumbnail);
                holder.thumbnail.setTag(position);
				holder.thumb_progress = (FrameLayout)view.findViewById(R.id.progress_frame);
				holder.cb = (CheckBox)view.findViewById(R.id.all_commu_alert);
                holder.co = (TextView) view.findViewById(R.id.all_communityid);
                holder.cotitle = (TextView) view.findViewById(R.id.all_community_title);
                holder.desc = (TextView) view.findViewById(R.id.all_community_desc);
                holder.update = (TextView) view.findViewById(R.id.all_community_update);
                holder.level = (TextView) view.findViewById(R.id.allco_level);
                holder.member = (TextView) view.findViewById(R.id.allco_member);
                holder.movie = (TextView) view.findViewById(R.id.allco_movie);
                holder.broadcast = (TextView) view.findViewById(R.id.allco_broadcast);
                holder.bsp = (TextView) view.findViewById(R.id.allco_bsp);
                view.setTag(holder);
                holder.cb.setOnCheckedChangeListener(new OnCheckedChangeListener(){
					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						Log.d("NLiveRoid","Checked:::: " + holder.co.getText());
						if(alertList != null){
							if(isChecked){
								if(!alertList.contains( holder.co.getText().toString()))alertList.add(holder.co.getText().toString());
							}else{
								while(true){
									if(alertList.contains(holder.co.getText().toString())){
										alertList.remove( holder.co.getText().toString());
									}else{
										break;
									}
								}
							}
						}
					}
                });
            } else {
                holder = (ViewHolder) view.getTag();
            }
            String[] coInfo = item.getCommunity_info().split("<<SPLIT>>");
            holder.co.setText(item.getCommunityID());
            holder.cotitle.setText(item.getCommunityName());
            holder.desc.setText(item.getCommunityName());
            holder.cotitle.setText(item.getCommunityName());
            holder.level.setText(level + coInfo[0] + " ");
            holder.member.setText(member + coInfo[1] + " ");
            holder.movie.setText(movie + coInfo[2] + " ");
            holder.update.setText(coInfo[3]);
            holder.desc.setText(coInfo[4]);
            holder.broadcast.setText(brocas + (coInfo[5].equals("1")? ari:nasi));
            holder.bsp.setText(bsp + (coInfo[6].equals("1")? ari:nasi));
            	holder.cb.setChecked(alertList != null && alertList.contains(item.getCommunityID()));//アラートをチェック
            Bitmap t = item.getThumbnail();
			ViewGroup vg = (ViewGroup)view;
			if(t != null){
				vg.removeView(holder.thumb_progress);
				holder.thumbnail.setImageBitmap(t);
			}
			if(isAlertMode){
				holder.cb.setVisibility(View.VISIBLE);
			}else{
				holder.cb.setVisibility(View.GONE);
			}
            return view;
        }
        private class ViewHolder {
			public ImageView thumbnail;
            public FrameLayout thumb_progress;
            public CheckBox cb;
            public TextView co;
            public TextView cotitle;
            public TextView desc;
            public TextView update;
            public TextView level;
            public TextView member;
            public TextView movie;
            public TextView broadcast;
            public TextView bsp;
        }
    }

    //AllCommuのとこはここで表示されるビューを生成して返す
    @SuppressLint("ValidFragment")
	class MyFragment extends android.support.v4.app.Fragment {
    	private int position;
    	protected MyFragment(int position){
    		this.position = position;
    	}
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        	Log.d("MyFragment ", " onCreateView ------ ");
        	if(allFragmentViews.size()<= position || allFragmentViews.get(position) == null){
        	final Activity act = getActivity();
        	LinearLayout ll = new LinearLayout(act);
        	ProgressBar pb = new ProgressBar(act);
        	ll.setGravity(Gravity.CENTER);
        	ll.addView(pb,new LinearLayout.LayoutParams(100,100));
        	ListView localList = new ListView(act);
        	localList.setDrawingCacheEnabled(false);
        	fragmentProgress.add(pb);
        	localList.setVisibility(View.GONE);
        	fragmentListViews.add(localList);
            @SuppressWarnings("unchecked")
            ArrayList<LiveInfo> list = (ArrayList<LiveInfo>) getArguments().get("list");
            final AllCommunityListAdapter fAdapter = new AllCommunityListAdapter(act,list);
            fragmentListAdapters.add(fAdapter);
            localList.setAdapter(fragmentListAdapters.get(fragmentListAdapters.size()-1));
        	registerForContextMenu(localList);
        	localList.setOnItemClickListener(new OnItemClickListener() {
					 private AlertDialog dialogCreate = null;
   			 @Override
   			 public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
   				 if(isContextOperation){
   					 isContextOperation = false;
   					 return;
   				 }
   						 fAdapter.getItem(position);
   						 AlertDialog.Builder alertD = new AlertDialog.Builder(act);
   						 alertD.setItems(new String[]{"直近の放送","最近の放送履歴","ブラウザ","退会"}, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								if(dialogCreate != null)dialogCreate.cancel();
								allCommFunction(which,fAdapter.getItem(position));
							}
						});
   						dialogCreate = alertD.create();
   						 alertD.show();
   			 }
        	});
            ll.addView(localList);
            allFragmentViews.add(ll);
        	}else{
        		((ViewGroup)allFragmentViews.get(position).getParent()).removeView(allFragmentViews.get(position));
        	}
        		return allFragmentViews.get(position);
        }
    }

        class PageItem {
            public ArrayList<LiveInfo> communityList;
            private PageItem(){
            	communityList = new ArrayList<LiveInfo>();
            }
        }
        //ページ全体のアダプター
    class MyPagerAdapter extends FragmentPagerAdapter {
        private ArrayList<PageItem> pageItemList;//ここに1ページの要素が入る
        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
            pageItemList = new ArrayList<PageItem>();
        }
        @Override
        public Fragment getItem(int position) {
            MyFragment myfragment = new MyFragment(position);
            Bundle bundle = new Bundle();//これがよくわからない
            bundle.putSerializable("list", pageItemList.get(position).communityList);
            myfragment.setArguments(bundle);
        	return myfragment;
        }
        public PageItem getPageItem(int position){
        	return pageItemList.get(position);

        }
        @Override
        public int getCount() {//ページ数を返す
            return pageItemList.size();
        }
        public void addItem(PageItem item) {//ページを追加する
            pageItemList.add(item);
        }
		public void setAlertList(ArrayList<String> alertList) {
			for(AllCommunityListAdapter i:fragmentListAdapters){
				i.alertList = alertList;
			}
		}
    }

	public void schemeCalled(String uri){
		//サーチしてたらやめる
    	SearchTab.cancelMoveingTask();
    	LiveTab.cancelMovingTask();
		cancelMovingTask();
		schemeCalled = true;
		getIntent().putExtra("scheme", uri);
		onWindowFocusChanged(false);
	}
	@Override
	public void onResume(){
		final NLiveRoid app = (NLiveRoid)getApplicationContext();
			app.setForeACT(this);
	    	//サーチしてたらやめる
	    	SearchTab.cancelMoveingTask();
	    	LiveTab.cancelMovingTask();

			try{
	    	if(TopTabs.isFirstLogin&&Boolean.parseBoolean(app.getDetailsMapValue("ac_confirm"))){
			//初回確認リスト表示

				if(accountDialog == null || !accountDialog.isShowing()){
			accountDialog = new AlertDialog.Builder(this).setTitle("アカウントを選択").setItems(new CharSequence[]{app.getDefaultMap().get("user_id1"),app.getDefaultMap().get("user_id2")},
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							//アカウントを設定
							switch(which){
							case 0:
								app.setDetailsMapValue("always_use1", "true");
								app.setDetailsMapValue("always_use2", "false");
								app.setSessionid("");
								break;
							case 1:
								app.setDetailsMapValue("always_use1", "false");
								app.setDetailsMapValue("always_use2", "true");
								app.setSessionid("");
								break;
							}
							if(PrimitiveSetting.getACT() != null)PrimitiveSetting.getACT().updateAlways();
							TopTabs.isFirstLogin = false;
							isFristPager = true;
						onReload();
						}
					}).setCancelable(false).create();
			accountDialog.show();
				}
		}else{
		if(adapter.isEmpty()||Boolean.parseBoolean(app.getDetailsMapValue("update_tab"))){
			//  schemeの場合lvURLを呼ぶ
					if(!schemeCalled){
						onReload();
					}
		}
		}
	}catch(NullPointerException e){
		e.printStackTrace();
	}catch(Exception e){
		e.printStackTrace();
	}
		super.onResume();
	}

	public void setAlertList(ArrayList<String> alist){
		if(isPagerViewing&&pagerAdapter != null)pagerAdapter.setAlertList(alist);
	}


	public class AllCommunityTask extends AsyncTask<Void,Void,Void> implements FinishCallBacks{
		private boolean ENDFLAG = true;
		private ArrayList<LiveInfo> coLiveList;
		private ArrayList<GETThumb> thumbnailTask;
		private String amount;
		private int pageNum;

		public AllCommunityTask(int pageNum){
			this.pageNum = pageNum;
		}
		@Override
		public void onCancelled(){
			super.onCancelled();
			if(thumbnailTask != null){
				for(int i  = 0 ; i < thumbnailTask.size();i++){
					thumbnailTask.get(i).cancel(true);
				}
			}
		}
		@Override
		protected Void doInBackground(Void... params) {
			NLiveRoid app = (NLiveRoid)getApplicationContext();
			if(error == null){
				app.initStandard();
				error = app.getError();
			}
			//セッション取得
			String sessionid = Request.getSessionID(error);
			if(error.getErrorCode() != 0){
				return null;
			}
			//トップのソース取得後パース
			InputStream source = Request.doGetToInputStreamFromFixedSession(sessionid, String.format(URLEnum.ALLCOMMUNITY,pageNum), error);
			if(source == null){
				error.setErrorCode(-48);
				return null;
			}
			 try {
				 AllCommunityParser handler = new AllCommunityParser(this);
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
			      throw new RuntimeException(e);
			  } catch(UnknownHostException e){//接続悪い時になる
				  if(error != null ){
					  error.setErrorCode(-6);
					  e.printStackTrace();
				  }
				  return null;
				}catch (IOException e) {
				  e.printStackTrace();
			} catch (SAXException e) {
				e.printStackTrace();
			}
			long startT = System.currentTimeMillis();
			while(ENDFLAG){
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					ENDFLAG = false;
					e.printStackTrace();
					return null;
				}catch(IllegalArgumentException e){
					Log.d("NLiveRoid","IllegalArgumentException at CommunityTab TopParseTask");
					e.printStackTrace();
					ENDFLAG = false;
					return null;
				}
				if(System.currentTimeMillis()-startT>120000){
					//タイムアウト
					ENDFLAG = false;
					error.setErrorCode(-10);
					return null;
				}
			}
			return null;
		}
		@Override
		protected void onPostExecute(Void arg){
			if(error != null){
				if( error.getErrorCode() != 0){
					error.showErrorToast();
				}else if(coLiveList != null){
					thumbnailTask = new ArrayList<GETThumb>();
					fragmentListAdapters.get(pageNum-1).clear();
					for(int i = 0; i < coLiveList.size() ; i++){
//						Log.d("-------------------------------------"," " + coLiveList.size() + " " + coLiveList.get(i).getCommunity_info().split("<<SPLIT>>").length);
//						Log.d("INFO ---  ", " " + coLiveList.get(i).getCommunityID());
//						Log.d("INFO --- ", " " + coLiveList.get(i).getCommunityName());
//							String[] infos = coLiveList.get(i).getCommunity_info().split("<<SPLIT>>");
//						for(int j = 0; j < infos.length; j++){
//								Log.d("COMMUNITY --- ", " " + infos[j]);
//						}
						fragmentListAdapters.get(pageNum-1).add(coLiveList.get(i));
						thumbnailTask.add(new GETThumb(fragmentListViews.get(pageNum-1),i));
						thumbnailTask.get(i).execute(coLiveList.get(i));
					}
					thumbnailTask.clear();
					fragmentProgress.get(pageNum-1).setVisibility(View.GONE);
					fragmentListViews.get(pageNum-1).setVisibility(View.VISIBLE);
					if(isFristPager && amount != null){//初回だったら
						//総コミュニティ分のページを追加する
						//X ～ Xは今表示しているPagerView的なページ番号と総数を使うので総数以外は必要ない
						try{
							communityAmount = 0;
							communityAmount = Integer.parseInt(amount);
						}catch(Exception e){
							e.printStackTrace();
							MyToast.customToastShow(ACT, "参加中のコミュニティの総数の取得に失敗しました");
						}
						for(int i = 0; i < communityAmount/30; i++){//i = 0からなら/30でOK
							PageItem popular = new PageItem();
				        pagerAdapter.addItem(popular);
						}
						if(headerText != null)headerText.setText("参加中一覧 0-30/" + communityAmount);
						isFristPager = false;
					}
				}
			}
		}

		public void finishCallBack(ArrayList<LiveInfo> list,String amount){
				this.coLiveList = list;
				this.amount = amount;
				ENDFLAG = false;
		}
		@Override
		public void finishCallBack(ArrayList<LiveInfo> info) {}
		@Override
		public void finishCallBack(ArrayList<LiveInfo> info,
				LinkedHashMap<String, String> generate) {}
	}
	@Override
	public void onPause(){
		isListTaped = false;
		//キーボードが出てきちゃうのでクリア
				// ボタンにフォーカスを移動させる
				if(parent != null){
					parent.setFocusable(true);
					parent.setFocusableInTouchMode(true);
					parent.requestFocus();
				}
				//Gateが開いてたら閉じる
				if(gate != null && gate.isOpened()){
					gate.close_noanimation();
				}
				writeAlertFile();
		super.onPause();
	}
	@Override
	protected void onDestroy() {
	CookieSyncManager.getInstance().stopSync();
	super.onDestroy();
	}


	//フラッシュをアプリケーションの設定値で起動する
	public void startFlashPlayer(LiveInfo liveObj){
		if(isListTaped){
			return;
		}
			isListTaped = true;
		 if(liveObj.getPassedTime().equals("予約枠")){
			 MyToast.customToastShow(ACT, "予約枠です");
		 }else{
//			 Log.d("NLiveRoid","CommunityTab startF " + liveObj.getLiveID() + " " + liveObj.getCommunity_info() + " " + liveObj.getCommunityName() + " " + liveObj.getCommunityID());
			 TopTabs.insertHis(0, liveObj.getLiveID(), liveObj.getCommunityID(), liveObj.getTitle(), liveObj.getCommunityName(), liveObj.getDescription());
				NLiveRoid app = (NLiveRoid)getApplicationContext();
				boolean[] setting_boolean = new boolean[28];
				try{//fexit,(finish_back),at,at_overwriteはDefaultMapValue
				setting_boolean[0] = app.getDetailsMapValue("fexit")==null? true:Boolean.parseBoolean(app.getDetailsMapValue("fexit"));
				setting_boolean[1] = app.getDetailsMapValue("newline")== null? false:Boolean.parseBoolean(app.getDetailsMapValue("newline"));
				setting_boolean[2] = app.getDetailsMapValue("form_up") == null ? false: Boolean.parseBoolean(app.getDetailsMapValue("form_up"));
				setting_boolean[3] = app.getDetailsMapValue("voice_input") == null ? false:Boolean.parseBoolean(app.getDetailsMapValue("voice_input"));
				setting_boolean[4] = app.getDetailsMapValue("sp_player") == null ? true:Boolean.parseBoolean(app.getDetailsMapValue("sp_player"));
				setting_boolean[5] = app.getDetailsMapValue("fix_volenable") == null ? false:Boolean.parseBoolean(app.getDetailsMapValue("fix_volenable"));
				setting_boolean[6] = app.getDetailsMapValue("at_enable")==null? false:Boolean.parseBoolean(app.getDetailsMapValue("at_enable"));
				setting_boolean[7] = app.getDetailsMapValue("at_overwrite")==null? false:Boolean.parseBoolean(app.getDetailsMapValue("at_overwrite"));
				setting_boolean[8] = app.getDetailsMapValue("xd_enable_p") == null ? false: Boolean.parseBoolean(app.getDetailsMapValue("xd_enable_p"));
				setting_boolean[9] = app.getDetailsMapValue("yd_enable_p") == null ? true: Boolean.parseBoolean(app.getDetailsMapValue("yd_enable_p"));
				setting_boolean[10] = app.getDetailsMapValue("xd_enable_l") == null ? false: Boolean.parseBoolean(app.getDetailsMapValue("xd_enable_l"));
				setting_boolean[11] = app.getDetailsMapValue("yd_enable_l") == null ? true: Boolean.parseBoolean(app.getDetailsMapValue("yd_enable_l"));
				setting_boolean[12] = app.getDetailsMapValue("speech_education_enable") == null ? true: Boolean.parseBoolean(app.getDetailsMapValue("speech_education_enable"));
				setting_boolean[13] = app.getDetailsMapValue("is_update_between") == null ? true: Boolean.parseBoolean(app.getDetailsMapValue("is_update_between"));

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
				//update_tabは渡す必要ない→欠番だけど、渡した上でBackGroundServiceで処理しているのでそこも変えなきゃいけない
				setting_boolean[24] = app.getDetailsMapValue("update_tab") == null ? false: Boolean.parseBoolean(app.getDetailsMapValue("update_tab"));
				setting_boolean[25] = app.getDetailsMapValue("recent_ts") == null ? true: Boolean.parseBoolean(app.getDetailsMapValue("recent_ts"));
				setting_boolean[26] = app.getDetailsMapValue("delay_start") == null ? true: Boolean.parseBoolean(app.getDetailsMapValue("delay_start"));
				setting_boolean[27] = app.getDetailsMapValue("back_black") == null ? false: Boolean.parseBoolean(app.getDetailsMapValue("back_black"));
				}catch(Exception e){
					//失敗したらデフォ値
					setting_boolean = new boolean[]{true,false,false,false,true,false,false,false,false,true,false,true,false,true,true,true,true,true,true,false,true,false,false,false,false,true,true,false};
				}
				byte[] setting_byte = new byte[44];
				short init_comment_count = 20;//所期コメ件数
				String twitterToken = null;
				long offTimer = -1;
				try{
				twitterToken = app.getDefaultMap().get("twitter_token") == null? null:app.getDefaultMap().get("twitter_token") + " " + app.getDefaultMap().get("twitter_secret");
				init_comment_count = app.getDetailsMapValue("init_comment_count") == null? 20:Short.parseShort(app.getDetailsMapValue("init_comment_count"));
				offTimer = app.getDetailsMapValue("offtimer_start") == null? -1:Long.parseLong(app.getDetailsMapValue("offtimer_start"));
				setting_byte[0] = app.getDetailsMapValue("type_width_p")==null? 0:Byte.parseByte(app.getDetailsMapValue("type_width_p"));
				setting_byte[1] = app.getDetailsMapValue("id_width_p")==null? 15:Byte.parseByte(app.getDetailsMapValue("id_width_p"));
				setting_byte[2] = app.getDetailsMapValue("command_width_p")==null? 0:Byte.parseByte(app.getDetailsMapValue("command_width_p"));
				setting_byte[3] = app.getDetailsMapValue("time_width_p")==null? 0:Byte.parseByte(app.getDetailsMapValue("time_width_p"));
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
						e.printStackTrace();
						//失敗したらデフォ値
						setting_byte = new byte[]{0,15,0,0,0,15,70,3,0,92,-43,0,15,0,0,0,15,70,3,0,92,-43,0,0,0,-1,5,5,0,5,50,0,-1,0,0,3,5,-1,100,40,15,127,0,0};
					}
				byte[] seq = new byte[7];
				try{
					//列順
					seq[0] = app.getDetailsMapValue("type_seq")==null? 0:Byte.parseByte(app.getDetailsMapValue("type_seq"));
					seq[1] = app.getDetailsMapValue("id_seq")==null? 1:Byte.parseByte(app.getDetailsMapValue("id_seq"));
					seq[2] = app.getDetailsMapValue("cmd_seq")==null? 2:Byte.parseByte(app.getDetailsMapValue("cmd_seq"));
					seq[3] = app.getDetailsMapValue("time_seq")==null? 3:Byte.parseByte(app.getDetailsMapValue("time_seq"));
					seq[4] = app.getDetailsMapValue("score_seq")==null? 4:Byte.parseByte(app.getDetailsMapValue("score_seq"));
					seq[5] = app.getDetailsMapValue("num_seq")==null? 5:Byte.parseByte(app.getDetailsMapValue("num_seq"));
					seq[6] = app.getDetailsMapValue("comment_seq")==null? 6:Byte.parseByte(app.getDetailsMapValue("comment_seq"));

				}catch(Exception e){
					seq = new byte[]{0,1,2,3,4,5,6};
				}

				try{
				String skip_word = app.getDetailsMapValue("speech_skip_word")==null ? "いかりゃく":app.getDetailsMapValue("speech_skip_word");

				CommandMapping cmd = null;
				String[] cmdValue = new String[4];
				cmdValue[0] = app.getDetailsMapValue("cmd_cmd");
				cmdValue[1] = app.getDetailsMapValue("cmd_size");
				cmdValue[2] = app.getDetailsMapValue("cmd_color");
				cmdValue[3] = app.getDetailsMapValue("cmd_align");
				for(int i = 0 ; i < 4; i++){
					if(cmdValue[i] != null){
						if(i==3){
						cmd = new CommandMapping(cmdValue[0],cmdValue[1],cmdValue[2],cmdValue[3],false);
						break;
						}
					}
					if(i==3){//1つでもnullがあったら普通の初期化
						cmd = new CommandMapping(false);
					}
				}

				CookieSyncManager.getInstance().startSync();
				 String cookie = CookieManager.getInstance().getCookie("nicovideo.jp");
				 if(cookie == null){
					 cookie = Request.getSessionID(error);
					 if (cookie == null || cookie.equals("")
								|| cookie.equals("null")) {
							app.getError().showErrorToast();
							return;
						}
				 }
				 CookieManager.getInstance().setCookie("nicovideo.jp", cookie);
				 CookieSyncManager.getInstance().stopSync();
				  liveObj.serializeBitmap();
		 //Flashプレイヤーの起動
					LiveTab.cancelMovingTask();
					SearchTab.cancelMoveingTask();
		 final Intent flash = new Intent(ACT,FlashPlayer.class);
		 flash.putExtra("cmd", cmd);
		 flash.putExtra("setting_boolean", setting_boolean);
		 flash.putExtra("setting_byte", setting_byte);
		 flash.putExtra("init_comment_count", init_comment_count);
		 if(offTimer>0)flash.putExtra("offtimer_start", offTimer);//起動していればセット→無ければnull→渡す必要は無い
		 flash.putExtra("column_seq", seq);
		 if(!NLiveRoid.isPreLooked){
			 flash.putExtra("isPreLooked", (NLiveRoid.isPreLooked? null:"A"));
			 NLiveRoid.isPreLooked = true;
		 }
		 flash.putExtra("speech_skip_word", skip_word);
		 flash.putExtra("viewW",app.getViewWidth());
		 flash.putExtra("viewH",app.getViewHeight());
		 flash.putExtra("density", app.getMetrics());
		 flash.putExtra("twitterToken", twitterToken);
		 //コメのみならプレイヤーパラメタいらないけど。。とりあえず落としたくはないので
		 flash.putExtra("resizeW",app.getResizeW());
		 flash.putExtra("resizeH",app.getResizeH());
		 flash.putExtra("Cookie", cookie);
		 if(app.getDetailsMapValue("player_select") != null)flash.putExtra("sp_session",app.getSp_session_key());
		 flash.putExtra("LiveInfo", liveObj);
		 flash.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		 if(!TopTabs.getACT().isMovingSameLV(liveObj.getLiveID())){//バックグラウンドで同一のプレイヤーが起動しているか判定して起動
		//裏にいたら停止
		 flash.putExtra("restart", true);
	 }
	    	startActivity(flash);

				}catch (RuntimeException e) {
						Log.d("NLiveRoid", "RUNNTIME ERR COMMUNITY TAB");
						e.printStackTrace();
				}
		 }
	}


	public synchronized void onReload(){
		if(isPagerViewing){
			if(allcommunityTask != null && allcommunityTask.getStatus() != AsyncTask.Status.FINISHED){
	        	allcommunityTask.cancel(true);
	        }
			try{
				if(fragmentProgress.size() > 0){
			fragmentProgress.get(nowSelectPage).setVisibility(View.VISIBLE);
			fragmentListViews.get(nowSelectPage).setVisibility(View.GONE);
				}
	        allcommunityTask = new AllCommunityTask(nowSelectPage+1);//ページ番号だから+1
	        allcommunityTask.execute();
			}catch(Exception e){
				e.printStackTrace();
				MyToast.customToastShow(ACT, "参加中コミュの処理に失敗しました");
			}
		}else{
		//Topのパース Thread.sleep(0)で未確認System.errのためsynchronized
		if(updateListTask != null&&updateListTask.getStatus() != AsyncTask.Status.FINISHED){
		updateListTask.cancel(true);
		}
		if(lvURLTask != null && lvURLTask.getStatus() != AsyncTask.Status.FINISHED){
			lvURLTask.cancel(true);
			lvURLcancelled = true;
		}
		updateListTask = new TopParseTask();
		updateListTask.execute();
		addProgress();
		}
	}

	private void addProgress(){
		removeProgress();

		TranslateAnimation animation = new TranslateAnimation(
				Animation.RELATIVE_TO_PARENT, 0.0f,
				Animation.RELATIVE_TO_PARENT, 0.0f,
				Animation.RELATIVE_TO_PARENT, 1.0f,
				Animation.RELATIVE_TO_PARENT, 0.0f);
		animation.setDuration(400);
		animation.setFillAfter(true);

		View pParent = inflater.inflate(R.layout.progressbar, null);
		progressBar = (ProgressBar) pParent
		.findViewById(R.id.ProgressBarHorizontal);//毎回生成しないとできない
		progressArea.addView(progressBar,new LinearLayout.LayoutParams(-1,-1));
		progressBar.startAnimation(animation);
		progressBar.setProgress(1);
	}
	private void removeProgress(){
		TranslateAnimation animation = new TranslateAnimation(
				Animation.RELATIVE_TO_PARENT, 0.0f,
				Animation.RELATIVE_TO_PARENT, 0.0f,
				Animation.RELATIVE_TO_PARENT, 0.0f,
				Animation.RELATIVE_TO_PARENT, 1.0f);
		animation.setDuration(400);
		animation.setFillAfter(true);
		try{
		progressBar.startAnimation(animation);
		progressBar.setProgress(100);
		progressArea.removeAllViews();
		progressBar.setProgress(0);
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	/**
	 * SPマイページパース
	 * @author Owner
	 *
	 */
	public class TopParseTask extends AsyncTask<Void,Void,Void>{
		private boolean ENDFLAG = true;
		private ArrayList<LiveInfo> list;
		private boolean isMaintenance;
		private ArrayList<GETThumb> thumbnailTask;
		private boolean sleeped;
		@Override
		public void onCancelled(){
			super.onCancelled();
			if(thumbnailTask != null){
				for(int i = 0 ; i < thumbnailTask.size() ;i++){
					if(thumbnailTask.get(i).getStatus() != AsyncTask.Status.FINISHED){
						thumbnailTask.get(i).cancel(true);
					}
				}
			}
		}
		@Override
		protected Void doInBackground(Void... arg0) {
			Log.d("NLiveRoid","TopParseTask START ---- " );
			progressBar.setProgress(8);
			NLiveRoid app = (NLiveRoid)getApplicationContext();
			if(error == null){
				app.initStandard();
				error = app.getError();
			}
			progressBar.setProgress(10);
			//セッション取得
			String sessionid = Request.getSessionID(error);
			if(error.getErrorCode() != 0){
				return null;
			}
			if(NLiveRoid.isDebugMode)Log.d("NLiveRoid","TopParseTask" + (sessionid == null? "SESSION NULL":"SESS"));
			progressBar.setProgress(15);
			//トップのソース取得後パース
			InputStream source = Request.doGetToInputStreamFromFixedSession(sessionid, URLEnum.SMARTMY, error);
			if(error.getErrorCode() != 0){
				return null;
			}
			if(source == null){
				error.setErrorCode(-8);
				return null;
			}
			 try {
				  MypageParser handler = new MypageParser(this,error);
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
			      throw new RuntimeException(e);
			  } catch(UnknownHostException e){//接続悪い時になる
				  if(error != null ){
					  error.setErrorCode(-6);
					  e.printStackTrace();
				  }
				  return null;
				}catch (IOException e) {
				  e.printStackTrace();
			} catch (SAXException e) {
				e.printStackTrace();
			}
			long startT = System.currentTimeMillis();
			while(ENDFLAG){
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					ENDFLAG = false;
					e.printStackTrace();
					Log.d("NLiveRoid","TopParseTask InterruptedException -------- " +e.getClass().getName() + " "+e.getMessage() +" " + e.getCause());
					for(int i = 0; i< e.getStackTrace().length; i++){
						Log.d("NLiveRoid"," Interrupted " + e.getStackTrace()[i].getClassName() );
					}
					if(e.getMessage().contains("Thread.sleep")){
						Log.d("NLiveRoid","Threeped true ----- " );
						sleeped = true;
					}
					return null;
				}catch(IllegalArgumentException e){
					Log.d("NLiveRoid","IllegalArgumentException at CommunityTab TopParseTask");
					e.printStackTrace();
					ENDFLAG = false;
					return null;
				}
				if(System.currentTimeMillis()-startT>60000){
					//タイムアウト
					ENDFLAG = false;
					error.setErrorCode(-10);
					return null;
				}
			}
			progressBar.setProgress(80);
			return null;
		}

		public void finishCallBack(ArrayList<LiveInfo> list){
			if(NLiveRoid.isDebugMode)Log.d("NLiveRoid","ENDfinishCallBackTopParseTask");
			progressBar.setProgress(75);
			this.list = list;
			ENDFLAG = false;
		}
		public void finishCallBack(boolean isMaintenance){
			if(NLiveRoid.isDebugMode)Log.d("NLiveRoid","ENDfinishCallBackTopParseTask");
			this.isMaintenance = true;
			ENDFLAG = false;
		}
		@Override
		protected void onPostExecute(Void arg){
			if(isMaintenance){
				MyToast.customToastShow(ACT, "メンテナンス中と思われます");
				removeProgress();
			}else if(list != null&& error.getErrorCode() == 0){
				if(list.isEmpty()){
					MyToast.customToastShow(ACT, "参加中のコミュニティの放送がありませんでした");
					removeProgress();
					}else{
				adapter.clear();
				for(int i = 0; i < adapter.getCount(); i++){//getViewでnull判定できるように入れておく
					adapter.getItem(i).setThumbnail(null);
				}
			for(LiveInfo i:list){
				adapter.add(i);
			}
			long oneRowCount = 20/list.size();
			thumbnailTask = new ArrayList<GETThumb>();
			for(int i = 0; i < list.size(); i++){//adapter.getItem(i)とかの方がいい気もするけど
				progressBar.setProgress(80+i*(int)oneRowCount);
				thumbnailTask.add(new GETThumb(listview,i));
				thumbnailTask.get(i).execute(list.get(i));
				}
			removeProgress();
					}
			}else{
				removeProgress();
				error.showErrorToast();
				if(sleeped){
					MyToast.customToastShow(ACT, "端末のスリープによって処理がキャンセルされました");
				}
			}
			updateListTask = null;
			if(lvURLcancelled){
				//画面表示時に、onWindowFocusChangedとonResume→onReloadがめっちゃ競合するので
				//updateListTaskを優先して、lvURLTaskがキャンセルされていた場合のみlvURLTaskを後から走らせる
				lvURLcancelled = false;
				lvURLTask = new LvURLTask();
				lvURLTask.execute();
			}
//			isFirstThread = false;
		}
	}
		/**
		 * サムネイル取得
		 * @author Owner
		 *
		 */
		class GETThumb extends AsyncTask<LiveInfo,Void,Integer>{
			private ListView listV;
			private int thumbTagID;
			private GETThumb(ListView listview,int id){
				this.listV = listview;
				this.thumbTagID = id;
			}
			@Override
			protected Integer doInBackground(LiveInfo... arg0) {
				Bitmap bm = null;
				if(arg0[0] == null||arg0[0].getCommunityID() == null)return -1;
				if(arg0[0].getCommunityID().contains("ch")){
					bm = Request.getImageForList(String.format(URLEnum.BITMAPSCHANNEL,
							arg0[0].getCommunityID()),error,0);
				}else if(arg0[0].getThumbnailURL()!= null && !arg0[0].getThumbnailURL().equals("")){
					bm = Request.getImageForList(arg0[0].getThumbnailURL(),error,0);
				}else{
					bm = Request.getImageForList(String.format(URLEnum.BITMAPSCOMMUNITY,
							arg0[0].getCommunityID()),error,0);
				}
				if(bm != null){
					arg0[0].setThumbnail(bm);
				}else{
					arg0[0].setThumbnail(BitmapFactory.decodeResource(getResources(), R.drawable.noimage));
					Log.d("NLiveRoid","C thumb no image" + arg0[0]);
				}
				return thumbTagID;
			}
			@Override
			protected void onPostExecute(Integer arg){
				if(arg < 0)return;
				ImageView iv = (ImageView)listV.findViewWithTag(thumbTagID);
				if(iv != null&&listV != null){
				listV.invalidateViews();
				}
			}
		@Override
		public void onCancelled(){//onPostExecuteに行かずにキャンセルされた場合があるので必要
			removeProgress();
			super.onCancelled();
		}
	}

	public static CommunityTab getCommunityTab(){
		return ACT;
	}

	final class MyArrayAdapter extends ArrayAdapter<LiveInfo>{


		public MyArrayAdapter(Context context) {
			super(context, R.layout.list_row_community);
		}

		@Override
		public View getView(int position, View paramView, ViewGroup paramViewGroup){
			ViewHolder holder;
			View view = paramView;
			//nullの時だけ処理を行うと、更新してaddされた部分に情報が使いまわされてしまうので毎回ビューの情報を更新する
			if(view == null){
				view = inflater.inflate(R.layout.list_row_community, null);
				TextView livetitle = (TextView)view.findViewById(R.id.livetitle);
				TextView commutitle = (TextView)view.findViewById(R.id.communityid);
				TextView passedtime = (TextView)view.findViewById(R.id.starttime);
				ImageView thumbnail = (ImageView)view.findViewById(R.id.community_thumbnail);
				thumbnail.setTag(position);
				LinearLayout lbm = (LinearLayout)view.findViewById(R.id.tag_linear);
				FrameLayout fl = (FrameLayout)view.findViewById(R.id.progress_frame);
				TextView viewcount = (TextView)view.findViewById(R.id.viewcount);
				TextView rescount = (TextView)view.findViewById(R.id.rescount);
				TopTabs.setTextColor(livetitle,toptab_tcolor);
				TopTabs.setTextColor(commutitle,toptab_tcolor);
				TopTabs.setTextColor(passedtime,toptab_tcolor);
				TopTabs.setTextColor(viewcount,toptab_tcolor);
				TopTabs.setTextColor(rescount,toptab_tcolor);
				holder = new ViewHolder();
				holder.livetitle = livetitle;
				holder.commutitle = commutitle;
				holder.passedtime = passedtime;
				holder.thumbnail = thumbnail;
				holder.lbm = lbm;
				holder.fl = fl;
				holder.viewcount = viewcount;
				holder.rescount = rescount;
				holder.noimage = ACT.getResources().getDrawable(
						R.drawable.noimage);
				view.setTag(holder);
			}else{
				holder = (ViewHolder)view.getTag();
			}


			LiveInfo info = getItem(position);
			if(info != null){
				holder.livetitle.setText(info.getTitle());
				holder.commutitle.setText(info.getCommunityID());
				holder.passedtime.setText(info.getStartTime());
				holder.viewcount.setText("来場者数 " + info.getViewCount()+" ");
				holder.rescount.setText("コメント数 " + info.getResNumber());
				String[] tags = info.getTags().split("<<TAGXXX>>");
				NLiveRoid app = ((NLiveRoid)ACT.getApplicationContext());
				Bitmap tagsBm = null;
				holder.lbm.removeAllViews();
				for(int i = 0; i < tags.length; i++){
					if(tags[i].equals(" "))continue;
					tagsBm = app.getTagBitMap(tags[i]);
					if(tagsBm != null){
						ImageView iv = new ImageView(ACT);
						iv.setImageBitmap(tagsBm);
						iv.setScaleType(ImageView.ScaleType.FIT_START);
						holder.lbm.addView(iv);
					}
				}
				Bitmap t = info.getThumbnail();
				ViewGroup vg = (ViewGroup)view;
				if(t != null){
					vg.removeView(holder.fl);
					holder.thumbnail.setImageBitmap(t);
				}else if(vg.getChildCount() == 1){//サムネ自体はremoveしてないので1
					holder.thumbnail.setImageDrawable(holder.noimage);
					vg.addView(holder.fl);
				}
			}
			return view;
		}

	}
	private static class ViewHolder {
	    TextView livetitle;
	    TextView commutitle;
	    TextView passedtime;
	    Drawable noimage;
	    ImageView thumbnail;
	    LinearLayout lbm;
	    FrameLayout fl;
	    TextView viewcount;
	    TextView rescount;
	}

	//LiveArchiveDialogから呼ばれるのでpublicにした
	public void allCommFunction(int which,final LiveInfo li){
		switch(which){
		case 0://直近の放送
		case 10:
			String text = "";
			if(li.getCommunityID() == null || li.getCommunityID().equals("") || li.getCommunityID().equals(URLEnum.HYPHEN)){
				if(li.getLiveID() != null && !li.getLiveID().equals("") && !li.getLiveID().equals(URLEnum.HYPHEN))text = li.getLiveID();
			}else{
				text = li.getCommunityID();
			}
			if(text != null && !text.equals("")){
				lvet.setText(text);
				lvURLTask = new LvURLTask();
				lvURLTask.execute();
			}
			break;
		case 1://放送履歴
		case 11:
			NLiveRoid app = (NLiveRoid)getApplicationContext();
			new LiveArchivesDialog(this,li.getCommunityID(),app.getViewWidth(),error).showSelf();
			break;
		case 2://ブラウザ
			new AlertDialog.Builder(this)
			.setItems(new String[]{"コミュニティページ","BBS","ブロマガ","オーナーページ"}, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Uri uri = null;
					switch(which){
					case 0:
						uri = Uri.parse(URLEnum.COMMUNITYURL + li.getCommunityID());
						break;
					case 1:
						uri = Uri.parse(URLEnum.BBS + li.getCommunityID());
						break;
					case 2:
						new CommuBrowser(ACT, error, progressBar, 0,li.getCommunityID()).execute();
						return;
					case 3:
						new CommuBrowser(ACT, error, progressBar, 1,li.getCommunityID()).execute();
						return;
					}
					Intent i = new Intent(Intent.ACTION_VIEW);
					i.addCategory(Intent.CATEGORY_BROWSABLE);
					i.setDataAndType(uri, "text/html");
					ACT.startActivity(i);
				}
			})
			.create().show();
			break;
			//長タップからのブラウザ12～14
		case 12:
			Uri uri0 = Uri.parse(URLEnum.COMMUNITYURL + li.getCommunityID());
			Intent i0 = new Intent(Intent.ACTION_VIEW);
			i0.addCategory(Intent.CATEGORY_BROWSABLE);
			i0.setDataAndType(uri0, "text/html");
			ACT.startActivity(i0);
			break;
		case 13:
			Uri uri1 = Uri.parse(URLEnum.BBS + li.getCommunityID());
			Intent i1 = new Intent(Intent.ACTION_VIEW);
			i1.addCategory(Intent.CATEGORY_BROWSABLE);
			i1.setDataAndType(uri1, "text/html");
			ACT.startActivity(i1);
			break;
		case 14:
			new CommuBrowser(this, error, progressBar, 0,li.getCommunityID()).execute();
			break;
		case 15:
			new CommuBrowser(this, error, progressBar, 1,li.getCommunityID()).execute();
			break;
		case 3://コミュ参加/退会
		case 16:
			new CommunityInfoTask(this,li.getCommunityID()
					,Request.getSessionID(error),((NLiveRoid)ACT.getApplicationContext()).getViewWidth()).execute();
			break;
		}
	}

	/**
	 * コンテキストメニュー生成時処理
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View view,
			ContextMenuInfo info) {
		super.onCreateContextMenu(menu, view, info);
		NLiveRoid app = (NLiveRoid)getApplicationContext();//シンプルじゃない、嫌い
		final AdapterContextMenuInfo adapterInfo = (AdapterContextMenuInfo) info;
		if(isPagerViewing){
			isContextOperation = true;
			try{//こっからallCommFunctionへいく場合は設定値でやるので10足して区別する
			allCommFunction(Integer.parseInt(app.getDetailsMapValue("allco_operate"))+10,fragmentListAdapters.get(nowSelectPage).getItem(adapterInfo.position));
			}catch(Exception e){
				e.printStackTrace();
			}
		}else{
			if(adapter.getCount() > adapterInfo.position ){
			//ここのセッションは、一番元のログインの物であるはずなので、そのままRequest.getSessionIDでおｋなはず
			GateView gView = app.getGateView();
			if(gView == null)return;
			gate = new Gate(this,gView,adapter.getItem(adapterInfo.position),false,Request.getSessionID(error));
			ViewGroup gateParent = (ViewGroup) app.getGateView().getView().getParent();
			if(gateParent != null){
				gateParent.removeView(app.getGateView().getView());
			}
			((ViewGroup)parent.getParent()).addView(app.getGateView().getView());
			gate.show(this.getResources().getConfiguration());
			}
		}


	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
	    outState.putString("WORKAROUND_FOR_BUG_19917_KEY", "WORKAROUND_FOR_BUG_19917_VALUE");
//	    super.onSaveInstanceState(outState);
	}




	@Override
	public boolean dispatchKeyEvent(KeyEvent keyevent){
		if(keyevent.getAction() == KeyEvent.ACTION_DOWN){
			if(keyevent.getKeyCode() == KeyEvent.KEYCODE_BACK && gate != null && gate.isOpened()){
				gate.close_noanimation();//外側からアニメーション起動するとなぜか重い
				return true;
			}else if(keyevent.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN && isPagerViewing){
				isAlertMode = !isAlertMode;
				fragmentListAdapters.get(nowSelectPage).notifyDataSetChanged();
				return true;
			}
		}
			return super.dispatchKeyEvent(keyevent);
	}



	private void writeAlertFile() {
		ArrayList<String> alertList = BackGroundService.getAlertList();
		Log.d("NLiveRoid","writeAlert" + (alertList == null? "List was NULL":alertList.size()));
		if(alertList == null || alertList.size() <= 0)return;
		try {
			FileOutputStream fos = ACT.openFileOutput("alertL", ACT.MODE_WORLD_READABLE);
			String xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"+
				"<Alert xmlns=\"http://nliveroid-tutorial.appspot.com/education/\">\n";
			for(String i:alertList){
				xml += "<id>"+i+"</id>\n";
//				Log.d("NLiveRoid","ALERT:" + i);
			}
		    xml += "</Alert>\n";
		    fos.write(xml.getBytes());
		    fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode,Intent data){
		isListTaped = false;
		//クルーズの時のフラッシュ等からブラウザに行って帰ってきた時はerrorもnull
//		if(error == null||resultCode == CODE.RESULT_ALLFINISH){
//			return;
//		}
		if(lvet != null){
			 // ソフトキーボードを非表示にする
          InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
          imm.hideSoftInputFromWindow(lvet.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
		}
		//flashのインテントに対して、何故か起動後のonPauseあたりでここが呼ばれちゃう
//		if(requestCode==CODE.FLASH&&data != null){//フラッシュから返ってきた
//
//			if(resultCode == CODE.RESULT_COOKIE||resultCode == CODE.RESULT_ONLYCOMMENT){
//				CookieSyncManager.getInstance().startSync();
//				CookieManager.getInstance().setCookie("nicovideo.jp",
//						data.getStringExtra("cookie"));//コメントのみの時にCookieを消す
//				CookieSyncManager.getInstance().stopSync();
//				NLiveRoid app = (NLiveRoid) getApplicationContext();
//				 int[] nullCheckI = data.getIntArrayExtra("setting_byte");
//				 boolean[] nullCeckB = data.getBooleanArrayExtra("setting_boolean");
//				 storeReturnData(nullCheckI,nullCeckB);
//		}else if(resultCode == CODE.RESULT_REDIRECT){//詳細からブラウザ
//				String url = data.getStringExtra("redirectlink");
//				if(url == null || url.equals(""))return;
//				Uri uri = Uri.parse(url);
//				Intent i = new Intent(Intent.ACTION_VIEW);
//				i.addCategory(Intent.CATEGORY_BROWSABLE);
//				i.setDataAndType(uri, "text/html");
//				startActivity(i);//とりあえず何も返却値とらない
//				}else if(resultCode == CODE.RESULT_NOLOGIN){//ログインしておらず、セッションを消す必要がある
//					NLiveRoid app = (NLiveRoid)getApplicationContext();
//					app.setDefaultMap("sessionid", "");
//				}else if(resultCode == CODE.RESULT_FLASH_ERROR){
//					int errorCode = data.getIntExtra("flash_error", 0);
//					if(error!= null){
//						Log.d("log","ERROR FLASH --- " + errorCode);
//						NLiveRoid app = (NLiveRoid)getApplicationContext();
//						app.setForeACT(ACT);
//					error.setErrorCode(errorCode);
//					error.showErrorToast();
//					}
//				}
			//フラッシュのところでマナーにされていら音量をもどす
//			String returnVol = data.getStringExtra("audiovolume");
//			if(returnVol !=null&&!returnVol.equals("")){
//				 AudioManager audio = (AudioManager) getSystemService(this.AUDIO_SERVICE);
//				  int mode = audio.getRingerMode();
//					Log.d("log","VOL RESULT --- " + returnVol);
//				  if(mode == AudioManager.RINGER_MODE_VIBRATE || mode == AudioManager.RINGER_MODE_SILENT){
//					  //音量を戻しておく(モードは変えなくてもセットされてくれるみたい)
//						audio.setStreamVolume(AudioManager.STREAM_MUSIC,Integer.parseInt(returnVol) , 0);
//			  }
//			}
//			}else{
//	            //フラッシュからでなく、ブラウザから戻ってきたか、その他
////	    		onReload();
//	    }

	}


	/**
	 * adapterを取得します。
	 * @return adapter
	 */
	public ArrayAdapter<LiveInfo> getAdapter() {
	    return adapter;
	}


	public boolean isPagerViewing() {
		return isPagerViewing;
	}


	@Override
	public void showGate(LiveInfo liveObj) {
		NLiveRoid app = (NLiveRoid)ACT.getApplicationContext();
		GateView gView = app.getGateView();
			if(gView == null)return;
		gate = new Gate(ACT,gView,liveObj,false,Request.getSessionID(error));
		ViewGroup gateParent = (ViewGroup) app.getGateView().getView().getParent();
		if(gateParent != null){
			gateParent.removeView(app.getGateView().getView());
		}
		((ViewGroup)parent.getParent()).addView(app.getGateView().getView());
		gate.show(ACT.getResources().getConfiguration());
	}




}

