package nliveroid.nlr.main;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nliveroid.nlr.main.parser.GateParser;
import nliveroid.nlr.main.parser.XMLparser;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.ClipboardManager;
import android.text.Layout;
import android.text.Spannable;
import android.text.Spannable.Factory;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class Gate {
	private Gate Me;
	private LiveInfo liveinfo;
	private boolean isOpened;

	 private GestureDetector gestureDetector;
	 private int longPressVal;

	private Factory factory;
	// ドラッグ開始位置
	int mOffsetDragStart = -1;
	// ドラッグ終了位置g
	int mOffsetDragEnd;
	int mDragStartX;
	int mDragStartY;
	private boolean isFinished;
	private String clipbordText;
	private TextSelectArea title_text;
	private TextSelectArea desc_text;

	private boolean isSelectMode = false;// 長押しで選択モードにする
	private int areaTouch = 0;
	private TextSelectArea tag_text;
	private TextSelectArea commu_text;
	private Activity ACT;
	private GateView gateView;
	private ProgressBar p;
	private String[] tagList;
	private boolean ENDFLAG = true;

	private TagArrangeDialog tagDialog;
	private TweetDialog tweetDialog;

	private String tweet_token;
	protected Gate(final Activity act, final GateView gateView,
			final LiveInfo liveObj,final boolean isOverLay,final String sessionid,String tweet_token) {
		this(act,gateView,liveObj,isOverLay,sessionid);
		this.tweet_token = tweet_token;
	}
	protected Gate(final Activity act, final GateView gateView,
			final LiveInfo liveObj,final boolean isOveLay,final String sessionid) {
		if(NLiveRoid.isDebugMode)Log.d("NLiveRoid"," new Gate " + isOveLay);
		if(NLiveRoid.isDebugMode)Log.d("NLiveRoid"," new Gate " + act.getClass().getName());

		 Log.d("NLiveRoid","Gate startF " + liveObj.getLiveID() + " " + liveObj.getCommunity_info() + " " + liveObj.getCommunityName() + " " + liveObj.getCommunityID());
		 TopTabs.insertHis(1, liveObj.getLiveID(), liveObj.getCommunityID(), liveObj.getTitle(), liveObj.getCommunityName(), liveObj.getCommunity_info());
		this.liveinfo = liveObj;
		this.ACT = act;// error生成に使う
		if(liveObj == null)return;//配信時になる
		factory = Spannable.Factory.getInstance();
		Me = this;
		this.gateView = gateView;
		gateView.clearViewStatus();

		 gestureDetector = new GestureDetector(ACT,new
		 OnGestureListener(){
		 @Override
		 public boolean onDown(MotionEvent arg0) {
		 return false;
		 }
		 @Override
		 public boolean onFling(MotionEvent arg0, MotionEvent arg1,
		 float arg2, float arg3) {
		 return false;
		 }
		 @Override
		 public void onLongPress(MotionEvent arg0) {
		 if(isSelectMode){
		 isSelectMode = false;
		 //コンテキストメニューを表示
		 switch(areaTouch){
		 case 0:
		 gateView.getTagLabel().setText("タグ　選択");
		 break;
		 case 1:
		 gateView.getTitleLabel().setText("放送タイトル　選択");
		 break;
		 case 2:
		 gateView.getDescLabel().setText("詳細　選択");
		 break;
		 case 3:
		 gateView.getCommuLabel().setText("コミュニティ情報　選択");
		 break;
		 }
		 }else{
		 isSelectMode= true;
		 gateView.getTagLabel().setText("タグ");
		 gateView.getTitleLabel().setText("タイトル");
		 gateView.getDescLabel().setText("詳細");
		 gateView.getCommuLabel().setText("コミュニティ情報");
		 }
		 }
		 @Override
		 public boolean onScroll(MotionEvent arg0, MotionEvent arg1,
		 float arg2, float arg3) {
		 return false;
		 }
		 @Override
		 public void onShowPress(MotionEvent arg0) {
		 }
		 @Override
		 public boolean onSingleTapUp(MotionEvent arg0) {
		 return false;
		 }
		 });
		// サムネイル
		if (liveObj.getThumbnail() != null) {
			gateView.getCommuThumbView()
					.setImageBitmap(liveObj.getThumbnail());
		}else{
			//サムネがnullだったらもう一度取得に行く
			new ThumbNailTask().execute();
		}
		// タグ領域
		tag_text = new TextSelectArea(ACT, gateView.getMainTable());
		tag_text.setGravity(Gravity.LEFT);
		tag_text.setPadding(20, 0, 20, 0);
		tag_text.setWidth(gateView.getWidth() - 40);
		((ViewGroup) gateView.getTagP()).addView(tag_text, -1, -2);
		gateView.getTagP().setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				// if(gestureDetector.onTouchEvent(arg1))return true;
				if (!isSelectMode)
					return gateView.getTagP().onTouchEvent(arg1);
				int x = (int) (0.5 + arg1.getX());// 四捨五入
				int y = (int) (0.5 + arg1.getY());
				switch (arg1.getAction()) {
				case MotionEvent.ACTION_DOWN:
					isFinished = false;
					mOffsetDragStart = tag_text.getOffset(x, y);
					mDragStartX = x;
					mDragStartY = y;
					break;
				case MotionEvent.ACTION_MOVE:
					tag_text.updateDragSelection(x, y, false);
					break;
				case MotionEvent.ACTION_UP:
					tag_text.updateDragSelection(x, y, true);
				default: // キャンセル他
					mOffsetDragStart = -1;
				}
				return true;
			}
		});
		String tags = liveObj.getTags().replaceAll("\n", "");
		if (tags != null && !tags.equals("")) {
			NLiveRoid app = ((NLiveRoid) ACT.getApplicationContext());
			HashMap<String, String> map = app.getTagNameMap();
			String[] tagArray = tags.split("<<TAGXXX>>");
			tagList = new String[tagArray.length - 1];// splitで最初のタグは無効
			String result = "";
			String temp = "";
			if (map != null) {
				for (int i = 1; i < tagArray.length; i++) {
//					Log.d("log", "TAGARRAY I" + tagArray[i]);
					temp = map.get(tagArray[i]);
					if (temp != null) {
						tagList[i - 1] = temp;
						result += temp + " ";
					}
				}
				tag_text.setText(result);
			}
		} else {
			tag_text.setText(URLEnum.HYPHEN);
		}
		// タイトル領域
		title_text = new TextSelectArea(ACT, gateView.getMainTable());
		title_text.setGravity(Gravity.LEFT);
		title_text.setPadding(20, 0, 20, 0);
		title_text.setTextSize(20);
		title_text.setWidth(gateView.getWidth() - 40);
		((ViewGroup) gateView.getTitleP()).addView(title_text, -1, -2);
		gateView.getTitleP().setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
//				 if(gestureDetector.onTouchEvent(arg1))return true;
				if (!isSelectMode)
					return gateView.getTitleP().onTouchEvent(arg1);
				int x = (int) (0.5 + arg1.getX());// 四捨五入
				int y = (int) (0.5 + arg1.getY());
				switch (arg1.getAction()) {
				case MotionEvent.ACTION_DOWN:
					isFinished = false;
					mOffsetDragStart = title_text.getOffset(x, y);
					mDragStartX = x;
					mDragStartY = y;
					break;
				case MotionEvent.ACTION_MOVE:
					title_text.updateDragSelection(x, y, false);
					break;
				case MotionEvent.ACTION_UP:
					title_text.updateDragSelection(x, y, true);
				default: // キャンセル他
					mOffsetDragStart = -1;
				}
				return true;
			}
		});
		String title = liveObj.getTitle();
		if (title != null && !title.equals("")) {
			title_text.setText(title);
		} else {
			title_text.setText(URLEnum.HYPHEN);
		}
		// 詳細領域
		desc_text = new TextSelectArea(ACT, gateView.getMainTable());
		desc_text.setGravity(Gravity.LEFT);
		desc_text.setPadding(20, 0, 20, 0);
		desc_text.setWidth(gateView.getWidth() - 40);
		((ViewGroup) gateView.getDescP()).addView(desc_text, -1, -2);
		gateView.getDescP().setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				// if(gestureDetector.onTouchEvent(arg1)) return true;
				if (!isSelectMode)
					return gateView.getDescP().onTouchEvent(arg1);
				int x = (int) (0.5 + arg1.getX());// 四捨五入
				int y = (int) (0.5 + arg1.getY());
				switch (arg1.getAction()) {
				case MotionEvent.ACTION_DOWN:
					isFinished = false;
					mOffsetDragStart = desc_text.getOffset(x, y);
					mDragStartX = x;
					mDragStartY = y;
					break;
				case MotionEvent.ACTION_MOVE:
					desc_text.updateDragSelection(x, y, false);
					break;
				case MotionEvent.ACTION_UP:
					desc_text.updateDragSelection(x, y, true);
				default: // キャンセル他
					mOffsetDragStart = -1;
				}
				return true;
			}
		});
		String descript = liveObj.getDescription();
		if (descript != null && !descript.equals("")) {
			desc_text.setText(descript
					.replaceAll("<font.*?>|<u>|</u>|<b>|</b>|<i>|</i>|" +
							"<s>|</s>|<a.*?>|</a>|<br>", ""));//なるべく消しておく
		} else {
			desc_text.setText(URLEnum.HYPHEN);
		}
		// コミュ情報テキスト領域
		commu_text = new TextSelectArea(ACT, gateView.getMainTable());
		commu_text.setGravity(Gravity.LEFT);
		commu_text.setPadding(20, 0, 20, 0);
		commu_text.setWidth(gateView.getWidth() - 40);
		((ViewGroup) gateView.getCommuP()).addView(commu_text, -1, -2);
//		gateView.getCommuP().setOnTouchListener(new OnTouchListener() {
//			@Override
//			public boolean onTouch(View arg0, MotionEvent arg1) {
//				// if(gestureDetector.onTouchEvent(arg1)) return true;
//				if (!isSelectMode)
//					return gateView.getCommuP().onTouchEvent(arg1);
//				int x = (int) (0.5 + arg1.getX());// 四捨五入
//				int y = (int) (0.5 + arg1.getY());
//				switch (arg1.getAction()) {
//				case MotionEvent.ACTION_DOWN:
//					isFinished = false;
//					mOffsetDragStart = commu_text.getOffset(x, y);
//					mDragStartX = x;
//					mDragStartY = y;
//					break;
//				case MotionEvent.ACTION_MOVE:
//					commu_text.updateDragSelection(x, y, false);
//					break;
//				case MotionEvent.ACTION_UP:
//					commu_text.updateDragSelection(x, y, true);
//				default: // キャンセル他
//					mOffsetDragStart = -1;
//				}
//				return true;
//			}
//		});
		String commu = liveObj.getCommunity_info();
		if (commu != null && !commu.equals("")) {
			commu_text.setText(commu.replaceAll("<font.*?>|<u>|</u>|<b>|</b>|<i>|</i>|" +
					"<s>|</s>|<a.*?>|</a>|<br>", ""));//なるべく消しておく
		} else {
			commu_text.setText(URLEnum.HYPHEN);
		}

		// 来場者とコメント数
		gateView.getViewCountView().setText(liveObj.getViewCount());
		gateView.getResNumView().setText(liveObj.getResNumber());
		gateView.getCloseView().setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				if (Me != null) {
					Me.close();
				}
				return false;
			}
		});
		// 開場、開演テキスト getplayerでUTCで返ってくる時と、GateのHH:mmの両方入ってくる
		if(!(liveObj.isOwner()&&!liveObj.isLiveStarted())){//配信テスト中は時間がマイナスで計算しずら過ぎる

		String starttime = liveObj.getStartTime();

		//02/05(日)00:03開始 1328367797
		// 経過時間
//		Log.d("log","GATE Start Time --------------------- " + starttime);
		try{
//		long nowMills = System.currentTimeMillis();
//		if(nowMills/1000 < Long.parseLong(starttime)){//配信開始前は未来の時間になってる
//				gateView.getPassedTime().setText(URLEnum.HYPHEN);

		Matcher smc = Pattern.compile("[0-9][0-9]:[0-9][0-9]").matcher(
				starttime);
		if(liveObj.getPassedTime().contains("<<SPLIT>>")){//ランキングだった(getRankingValueとかは何故か使いまわされるから使えない)
			gateView.getPassedTime().setText(liveObj.getPassedTime(false).split("<<SPLIT>>")[1]+"分");
		}else if (smc.find()) {//Gateだった
			starttime = smc.group();
			gateView.getStart().setText(starttime);
		}else{
			Matcher utc = Pattern.compile("[0-9]{10,15}").matcher(starttime);
			if(utc.find()){//getPlayerだった
				Date d = new Date(Long.parseLong(utc.group())*1000);
				SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
				starttime = sdf.format(d);
				gateView.getStart().setText(starttime);
			}
		}

				 Date date = new Date();
				 SimpleDateFormat sdf1 = new SimpleDateFormat("HH:mm");
				 String[] startTime = starttime.split(":");
		 		 String[] nowTime = sdf1.format(date).split(":");
				 //1日以上続いている放送には対応しない
				 int start_hour = Integer.parseInt(startTime[0]);
				 int start_minutes = Integer.parseInt(startTime[1]);
				 int now_hour = Integer.parseInt(nowTime[0]);
				 int now_minutes = Integer.parseInt(nowTime[1]);

				 if(now_hour-start_hour < 0){//日付跨いでいた
					 now_hour+=24;
				 }
				 if(now_minutes-start_minutes < 0){
					 now_minutes+=60;
				 }
				 if(now_hour-start_hour > 0){
				 	gateView.getPassedTime().setText(String.format("%d時間%d分", (now_hour-start_hour),(now_minutes-start_minutes)%60));
				 }else{
					 gateView.getPassedTime().setText(String.format("%d分", (now_minutes-start_minutes)%60));
				 }

		 }catch(Exception e){
 			 //シカト
 			 Log.d("NLiveRoid","Gate Time Failed");
 		 }
		}
 		 //座席
 		 String seetStr = liveObj.getRoomlabel() + "/" + liveObj.getRoomno() + "番";
 		 gateView.getSeetText().setText(seetStr);


		// 各ボタン--------------------------------------------------------------

 		gateView.getCopyB().setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if(NLiveRoid.isDebugMode)Log.d("NLiveRoid"," getCopyB");
				if(ENDFLAG){
					MyToast.customToastShow(ACT, "放送情報取得が完了していません");
					return;
				}
				new AlertDialog.Builder(ACT)
				.setTitle("クリップボードにコピー")
				.setItems(new String[]{"放送URL","コミュニティURL","タイトル","詳細","コミュ詳細"},
						new DialogInterface.OnClickListener() {
							public void onClick(
									DialogInterface dialog,
									int which) {
								ClipboardManager cm = (ClipboardManager) ACT.getSystemService(ACT.CLIPBOARD_SERVICE);
								// クリップボードへ値をコピー。
								switch(which){
								case 0:
									cm.setText(URLEnum.PC_WATCHBASEURL + liveinfo.getLiveID());
								break;
								case 1:
									if(liveinfo.getCommunityID().equals(URLEnum.HYPHEN)){
										MyToast.customToastShow(ACT, "コミュニティタイトル取得できません");
										return;
									}
									cm.setText(URLEnum.COMMUNITYURL + liveinfo.getCommunityID());
								break;
								case 2:
									cm.setText(liveinfo.getTitle().replaceAll("<<LINK1>>" +
											"|<font.*?>" +
											"|</font>" +
											"|<s/*?>" +
											"|</s>" +
											"|<i.*?>" +
											"|</i>" +
											"|<br>", ""));
								break;
								case 3:
									cm.setText(liveinfo.getDescription().replaceAll("<<LINK1>>" +
											"|<font.*?>" +
											"|</font>" +
											"|<s/*?>" +
											"|</s>" +
											"|<i.*?>" +
											"|</i>" +
											"|<br>", ""));
								break;
								case 4:
									cm.setText(liveinfo.getCommunity_info().replaceAll("<<LINK1>>" +
											"|<font.*?>" +
											"|</font>" +
											"|<s/*?>" +
											"|</s>" +
											"|<i.*?>" +
											"|</i>" +
											"|<br>", ""));
								break;
								}
							}
				}).create().show();
			}
		});

		gateView.getCommunityLinkB().setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if(NLiveRoid.isDebugMode){
					Log.d("NLiveRoid"," getCommuLinkB" + liveinfo);
					if(liveinfo != null){
						if(liveinfo != null)Log.d("NLiveRoid"," " + liveinfo.getLiveID());
						if(liveinfo != null)Log.d("NLiveRoid"," " + liveinfo.getCommunityID());
						if(liveinfo != null)Log.d("NLiveRoid"," " + liveinfo.getDefaultCommunity());//初期化後に-からnullにセットされる事がある
					}
				}
				if(liveinfo == null || liveinfo.getLiveID() == null ||  (liveinfo.getCommunityID().equals(URLEnum.HYPHEN)&&liveinfo.getDefaultCommunity() == null)||(liveinfo.getCommunityID().equals(URLEnum.HYPHEN ) &&liveinfo.getDefaultCommunity().equals(URLEnum.HYPHEN))){
					MyToast.customToastShow(ACT, "コミュニティIDが取得できませんでした");
					Log.d("NLiveRoid"," " + liveinfo);
					if(liveinfo != null){
						if(liveinfo != null)Log.d("NLiveRoid"," " + liveinfo.getLiveID());
						if(liveinfo != null)Log.d("NLiveRoid"," " + liveinfo.getCommunityID());
						if(liveinfo != null)Log.d("NLiveRoid"," " + liveinfo.getDefaultCommunity());
					}
					return;
				}else if(liveinfo.getCommunityID().equals(URLEnum.HYPHEN) && liveinfo.getDefaultCommunity().matches("co[0-9]+")){
					liveinfo.setCommunityID(liveinfo.getDefaultCommunity());//理由は忘れたが、getplayerでのdefaultCommunityのみでコミュIDを取ってきてる場合がある
				}
				if(!liveinfo.getCommunityID().matches("co[0-9]+")){
					MyToast.customToastShow(ACT, "ユーザー放送が検出されませんでした");
					return;
				}
				final AlertDialog.Builder dialog = new AlertDialog.Builder(ACT);
				dialog
				.setItems(new CharSequence[]{"参加/退会","最近の放送履歴"}, new DialogInterface.OnClickListener() {
					public void onClick(
							DialogInterface dialog,
							int which) {
						switch(which){
						case 0:
							dialog.dismiss();
							//コミュ参加/退会
							new CommunityInfoTask(ACT,liveinfo.getCommunityID(),sessionid,gateView.getWidth()).execute();
							break;
						case 1://最近の放送履歴
							NLiveRoid app = (NLiveRoid)ACT.getApplicationContext();
							new LiveArchivesDialog((Archiver)ACT,liveinfo.getCommunityID(),app.getViewWidth(),app.getError()).showSelf();
							break;
						}
					}
				})
				.create().show();
			}

		});

		gateView.getBrowserLinkB().setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if(NLiveRoid.isDebugMode)Log.d("NLiveRoid"," getSentenceLinkB");
				if(liveinfo == null || liveinfo.getDescription() == null || liveinfo.getCommunity_info() == null){
					return;
				}
				CharSequence[] items = null;
				if(liveinfo.getCommunityID().matches("co[0-9]+")){//文中リンク、配信者ページは確実にできるので最初にしておく
					items = new CharSequence[]{"詳細文中のリンク","配信者ページ","コミュニティページ","BBS","ブロマガ","オーナーページ"};
				}else{
					items = new CharSequence[]{"詳細文中のリンク"};
				}
				new AlertDialog.Builder(ACT)
				.setItems(items, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Uri uri = null;//whichが2以上の時のみ
							switch(which){
							case 0://文中リンク
								final String[] links;
								String description = liveinfo.getDescription();
								String commuinfo = liveinfo.getCommunity_info();
								String allStr = description + commuinfo;
								Pattern urlPt = Pattern.compile("(http|https):([^\\x00-\\x20()\"<>\\x7F-\\xFF])*", Pattern.CASE_INSENSITIVE);
								Matcher mc0 = urlPt.matcher(allStr);
								//co user mylist は普通にブラウザ連携としておく
								Matcher mc1 = Pattern.compile("co[0-9]+").matcher(allStr);
								Matcher mc2 = Pattern.compile("user/[0-9]+").matcher(allStr);
								Matcher mc3 = Pattern.compile("mylist/[0-9]+").matcher(allStr);
								//リンクを文字列的に取得しておく
								ArrayList<String> linkList = new ArrayList<String>();
								for(int i = 0; i < 30; i++){//アンカーとしてのリンクは30個まで
								Matcher mcL = Pattern.compile("<<LINK"+i+">>.+<<LINK"+i+">>").matcher(allStr);
									if(mcL.find()){
										linkList.add(mcL.group().replaceAll("<<LINK[0-9]+?>>", ""));
									}
								}
								if(mc0.find()){
									do{
										linkList.add(mc0.group());
									}while(mc0.find());
								}
								if(mc1.find()){
									do{
										linkList.add(URLEnum.COMMUNITYURL+mc1.group());
									}while(mc1.find());
								}
								if(mc2.find()){
									do{
										linkList.add("http://www.nicovideo.jp/"+mc2.group());
									}while(mc2.find());
								}
								if(mc3.find()){
									do{
										linkList.add("http://www.nicovideo.jp/"+mc3.group());
									}while(mc3.find());
								}
									links = new String[linkList.size()];
									for(int i = 0; i <linkList.size() ;i++){
										links[i] = linkList.get(i);
									}
									if(links.length ==0){
										MyToast.customToastShow(ACT, "リンクが検出できませんでした");
										return;
									}
									new AlertDialog.Builder(ACT)
									.setTitle("リンク先へ")
									.setItems(links,
											new DialogInterface.OnClickListener() {
												public void onClick(
														DialogInterface dialog,
														int which) {
													if(NLiveRoid.isDebugMode)Log.d("log", "links "
															+  links[which]);
																		Uri uri = null;
																			Random rand = new Random();
//																			//セッション編集
//																			String[] split_ = sessionid.split("_");
//																			for(String i:split_){
//																				Log.d("log", "session: "+i);
//																			}
																		uri = Uri.parse(links[which]);
																		if(NLiveRoid.isDebugMode)Log.d("Log","URI - " + uri.getPath());
																		Intent i = new Intent(Intent.ACTION_VIEW);
																		i.addCategory(Intent.CATEGORY_BROWSABLE);
																		i.setDataAndType(uri, "text/html");
																		ACT.startActivityForResult(i,CODE.RESULT_REDIRECT);//リダイレクトコード
												}
											}).show();
									return;
							case 1:
								if(liveinfo.getOwnerID() == null){
									new OwnerParseTask().execute(sessionid);
									}else{
										uri = Uri.parse(URLEnum.USERPAGE + liveinfo.getOwnerID());
									Intent i = new Intent(Intent.ACTION_VIEW);
									i.addCategory(Intent.CATEGORY_BROWSABLE);
									i.setDataAndType(uri, "text/html");
									ACT.startActivityForResult(i,CODE.RESULT_REDIRECT);//リダイレクトコード
									}
								return;
							case 2:
								uri = Uri.parse(URLEnum.COMMUNITYURL +liveinfo.getCommunityID());
								break;
							case 3:
								uri = Uri.parse(URLEnum.BBS + liveinfo.getCommunityID());
								break;
							case 4:
								new CommuBrowser(ACT, ((NLiveRoid)ACT.getApplicationContext()).getError(), new ProgressBar(ACT), 0,liveinfo.getCommunityID()).execute();
								return;
							case 5:
								new CommuBrowser(ACT, ((NLiveRoid)ACT.getApplicationContext()).getError(), new ProgressBar(ACT), 1,liveinfo.getCommunityID()).execute();
								return;
							}
							Intent i = new Intent(Intent.ACTION_VIEW);
								i.addCategory(Intent.CATEGORY_BROWSABLE);
								i.setDataAndType(uri, "text/html");
								ACT.startActivityForResult(i,CODE.RESULT_REDIRECT);//リダイレクトコード

					}
				}).create().show();

				}

		});
		//タグ検索/編集
				gateView.getTagLnkB().setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						if(NLiveRoid.isDebugMode)Log.d("NLiveRoid"," getTagLnkB");

						final AlertDialog.Builder dialog = new AlertDialog.Builder(ACT);
						dialog.setItems(new String[]{"キーワード検索","編集"}, new DialogInterface.OnClickListener() {
							public void onClick(
									DialogInterface dialog,
									int which) {
								switch(which){
								case 0:
									//検索
									dialog.dismiss();
									//検索ロジック
									try{
									if (tagList != null && tagList.length > 0) {
										new AlertDialog.Builder(ACT)
												.setTitle("キーワード検索")
												.setItems(tagList,
														new DialogInterface.OnClickListener() {
															public void onClick(
																	DialogInterface dialog,
																	int which) {
																Log.d("log", "TAG LSIT "
																		+ tagList[which]);
																close_noanimation();
																if(isOveLay){
																	if(ACT != null){
																		//視聴画面を終わる
																		if(OverLay.getOvarLay() != null){
																			OverLay.getOvarLay().finish(CODE.RESULT_FROM_GATE_FINISH);
																		}
																	}
																}
																//onActivityResultを直接呼ぶ
															if (SearchTab.getSearchTab() == null ) {//別プロセスか、まだサーチタブを開いていない
																	if(TopTabs.getACT() == null){//TopTabsが別プロセス=Flash||BC
//																		Log.d("Gate","ANOTHER PROCESS -----");
																		//FlashPlayer||BCPlayerをonActtivityResultから終了→BackGroundServiceからサーチタブへ
																		if(TransDiscr.getACT() != null){
																			TransDiscr.getACT().forTagSearch(tagList[which]);
																		}else if(FlashPlayer.getACT() != null){//コメントのみ
																			Intent data = new Intent();
																			data.putExtra("init_mode", (byte)1);
																			data.putExtra("tagword", tagList[which]);
																			FlashPlayer.getACT().onActivityResult(CODE.RESULT_TRANS_LAYER,CODE.RESULT_TRANS_LAYER,data);
																		}else if(BCPlayer.getBCACT() != null){//コメントのみで配信?なし?
																			Intent data = new Intent();
																			data.putExtra("init_mode", (byte)1);
																			data.putExtra("tagword", tagList[which]);
																			BCPlayer.getBCACT().onActivityResult(CODE.RESULT_TRANS_LAYER,CODE.RESULT_TRANS_LAYER,data);
																		}

																	}else{//同一プロセス=OverLay
																	TopTabs.getACT().changeTag(1);
																	SearchTab.getSearchTab()
																			.keyWordSearch_FromGate(
																					tagList[which]);
																	}
																}else{//SearchTabを開いたことがある
																	TopTabs.getACT().changeTag(1);
																	SearchTab.getSearchTab()
																			.keyWordSearch_FromGate(
																					tagList[which]);
																}
															}
														}).show();

											}
									}catch(Exception e){
										e.printStackTrace();
									}

									break;
								case 1:
									//編集ロジック
									dialog.dismiss();
									tagDialog = new TagArrangeDialog(ACT,sessionid,liveinfo.getLiveID(),gateView.getWidth());
									tagDialog.show();
									break;
								}
							}
						})
						.create().show();


					}
				});
		gateView.snsLnkB().setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if(NLiveRoid.isDebugMode)Log.d("NLiveRoid"," getSNSLnkB");

				new AlertDialog.Builder(ACT)
				.setItems(new CharSequence[]{"Tweet(NLR)","Tweet","mixiチェック","LINEで送る"}, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Uri uri = null;
						switch(which){
						case 0:
							if(NLiveRoid.isDebugMode)Log.d("NLiveRoid","Tweet at gate app" );
							if(tweet_token == null){
							NLiveRoid app = (NLiveRoid)ACT.getApplicationContext();
							tweet_token = app.getDefaultMap().get("twitter_token") + " " + app.getDefaultMap().get("twitter_secret");
							}
							if(tweet_token == null || tweet_token.split(" ").length < 2){
								MyToast.customToastShow(ACT, "Twitter認証されていませんでした");
								return;
							}else{
								tweetDialog = new TweetDialog(ACT, liveObj, tweet_token,false);
								tweetDialog.showSelf();
							}
							return;
						case 1:
							uri = Uri.parse("http://mobile.twitter.com/?status="+liveinfo.getTitle() +" http://nico.ms/"+liveinfo.getLiveID() + " #"+liveinfo.getLiveID() + " #nicolive" );
							break;
						case 2:
							uri = Uri.parse("http://mixi.jp/share.pl?u=http://live.nicovideo.jp/watch/"+liveinfo.getLiveID() +"&k=5319e6394c09374bb633820cb07add6a335076b2");
							break;
						case 3:
							uri = Uri.parse("line://msg/text/?" + liveinfo.getTitle() + URLEnum.SP_WATCHBASEURL+liveinfo.getLiveID()+"?cp_webto=share");
							break;
						}
						try{
						Intent intent = new Intent(Intent.ACTION_VIEW,uri);
						ACT.startActivity(intent);
						}catch(ActivityNotFoundException e){
							e.printStackTrace();
							MyToast.customToastShow(ACT, "起動できるアプリケーションが見つかりませんでした");
						}catch(Exception e){
							e.printStackTrace();
							MyToast.customToastShow(ACT, "アプリケーション起動時に予期せぬエラーが発生しました");
						}
					}
				}).create().show();
			}
		});

		if(isOveLay){
			gateView.getGoPlayerB().setVisibility(View.GONE);//別プロセスにならないように
		}else{
		gateView.getGoPlayerB().setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				if(NLiveRoid.isDebugMode)Log.d("NLiveRoid"," getGoPlayerB");
				close_noanimation();
				if(CommunityTab.getCommunityTab() != null && liveinfo != null){
				CommunityTab.getCommunityTab().startFlashPlayer(liveinfo);
				}
			}

		});
		}
		update(sessionid);
	}


	public void onConfigChanged(Configuration newConfig){
		if(NLiveRoid.isDebugMode)Log.d("NLiveRoid"," Gate onConfigChanged");
		if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			if(tagDialog != null)tagDialog.onConfigChanged(gateView.getHeight());
			commu_text.setWidth(gateView.getHeight() - 40);
			tag_text.setWidth(gateView.getHeight() - 40);
			desc_text.setWidth(gateView.getHeight() - 40);
			title_text.setWidth(gateView.getHeight() - 40);
			gateView.getTagLnkB().setLayoutParams(new TableRow.LayoutParams(gateView.getHeight()/7,80));
			gateView.getCommunityLinkB().setLayoutParams(new TableRow.LayoutParams(gateView.getHeight()/7,80));
			gateView.getBrowserLinkB().setLayoutParams(new TableRow.LayoutParams(gateView.getHeight()/7,80));
			gateView.snsLnkB().setLayoutParams(new TableRow.LayoutParams(gateView.getHeight()/7,80));
			gateView.getCopyB().setLayoutParams(new TableRow.LayoutParams(gateView.getHeight()/7,80));
			gateView.getGoPlayerB().setLayoutParams(new TableRow.LayoutParams(gateView.getHeight()/4,80));
			//goPlayerは自動的になるので大丈夫
		} else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
			if(tagDialog != null)tagDialog.onConfigChanged(gateView.getWidth());
			commu_text.setWidth(gateView.getWidth() - 40);
			tag_text.setWidth(gateView.getWidth() - 40);
			desc_text.setWidth(gateView.getWidth() - 40);
			title_text.setWidth(gateView.getWidth() - 40);
			gateView.getCommuName().setWidth(gateView.getWidth()-40);
			gateView.getTagLnkB().setLayoutParams(new TableRow.LayoutParams(gateView.getWidth()/7,80));
			gateView.getCommunityLinkB().setLayoutParams(new TableRow.LayoutParams(gateView.getWidth()/7,80));
			gateView.getBrowserLinkB().setLayoutParams(new TableRow.LayoutParams(gateView.getWidth()/7,80));
			gateView.snsLnkB().setLayoutParams(new TableRow.LayoutParams(gateView.getWidth()/7,80));
			gateView.getCopyB().setLayoutParams(new TableRow.LayoutParams(gateView.getWidth()/7,80));
			gateView.getGoPlayerB().setLayoutParams(new TableRow.LayoutParams(gateView.getWidth()/4,80));
		}
		if(tweetDialog != null && tweetDialog.isShowing())tweetDialog.onConfigChanged(ACT);
	}

	/**
	 * 透明ACTからプログレスバーを触ると何故かここだけ
	 * android.view.ViewRoot$CalledFromWrongThreadException
	 * 言われるので分けた
	 * いままでと何が違うのだろう?
	 * @author Owner
	 *
	 */
	class SetProgress extends AsyncTask<Integer,Void,Integer>{
		@Override
		protected Integer doInBackground(Integer... params) {
			return params[0];
		}
		@Override
		protected void onPostExecute(Integer arg){
			if(p != null){
				try{
				p.setProgress(arg);
				}catch(IllegalArgumentException e){
					e.printStackTrace();
				}
			}
		}
	}

	public class BackGroundUpdate extends AsyncTask<String, Void, ErrorCode> {

		@Override
		protected ErrorCode doInBackground(String... arg0) {
			if(NLiveRoid.isDebugMode)Log.d("NLiveRoid"," Call Gate BackGroundUpdate");
			ErrorCode error = ((NLiveRoid) ACT.getApplicationContext())
					.getError();
			if (error == null) {
				return null;
			} else if (error.getErrorCode() != 0) {
				return error;
			}else if(arg0[0] == null){
				return null;
			}// セッション取得
			if (error.getErrorCode() != 0) {
				return null;
			}
			if (p != null) {
				new SetProgress().execute(10);
			}
			InputStream source = Request.doGetToInputStreamFromFixedSession(
					arg0[0],
					String.format(URLEnum.GATE, liveinfo.getLiveID()), error);

			if(NLiveRoid.isDebugMode)Log.d("NLiveRoid"," source " + source);
			if (source == null) {
				return null;
			}
			if (p != null) {
				new SetProgress().execute(15);
			}
			try {
				if(NLiveRoid.isDebugMode)Log.d("NLiveRoid"," GateUpdate startGateParser");
				GateParser handler = new GateParser(this, error,liveinfo);
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
			} catch (IOException e) {
				e.printStackTrace();
			} catch (SAXException e) {
				e.printStackTrace();
			}
			long startT = System.currentTimeMillis();
			while (ENDFLAG) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
					ENDFLAG = false;
					break;
				}catch(IllegalArgumentException e){
					e.printStackTrace();
					ENDFLAG = false;
					break;
				}
				if(System.currentTimeMillis()-startT>30000){
					error.setErrorCode(-10);
					ENDFLAG = false;
					return error;
				}
			}
			return error;
		}

		protected void onPostExecute(ErrorCode arg) {
			if(NLiveRoid.isDebugMode)Log.d("NLiveRoid"," GateUpdate onPostExecute --- ");
			if (p != null) {
				p.setProgress(95);
			}
			if (arg == null) {
				MyToast.customToastShow(ACT, "放送詳細の取得に失敗しました");
			} else if (arg.getErrorCode() != 0) {
				arg.showErrorToast();
			} else {
				// 取得成功
				if(liveinfo == null){
					MyToast.customToastShow(ACT, "放送詳細の取得に失敗しました");
					return ;
				}
				String[] tagArray = liveinfo.getTags().split("<<TAGXXX>>");
				tagList = new String[tagArray.length - 1];// splitで最初のタグは無効な文字列が入っている
				String result = "";
				for (int i = 1; i < tagArray.length; i++) {
					tagList[i - 1] = tagArray[i];
					result += tagArray[i] + " ";
				}
				if(liveinfo.getTsReserveToken() != null){
					gateView.getReserveBt().setVisibility(View.VISIBLE);
					gateView.getReserveBt().setOnClickListener(new OnClickListener(){

						@Override
						public void onClick(View v) {
							new ReserveFirstTask().execute();
						}

					});
				}
				tag_text.setText(result);
				title_text.setText(liveinfo.getTitle()==null? URLEnum.HYPHEN:liveinfo.getTitle());
				desc_text.setText(getSpannable(liveinfo.getDescription()==null? URLEnum.HYPHEN:liveinfo.getDescription()));
				commu_text.setText(getSpannable(liveinfo.getCommunity_info()==null? URLEnum.HYPHEN:liveinfo.getCommunity_info()));
				gateView.getCommuName().setText(liveinfo.getCommunityName()==null? URLEnum.HYPHEN:liveinfo.getCommunityName());
				gateView.getOwnerName().setText(liveinfo.getOwnerName()==null? URLEnum.HYPHEN:liveinfo.getOwnerName());
			}
			removeProgress();
		}

		public void finishCallBack(LiveInfo linfo) {
			if (p != null) {
				new SetProgress().execute(80);
			}
			ENDFLAG = false;
			liveinfo = linfo;
		}
	}

	public void update(String sessionid) {
		if(NLiveRoid.isDebugMode)Log.d("NLiveRoid"," Call update" + (sessionid == null? "sNULL":"sOK"));
		addProgress();
		new BackGroundUpdate().execute(sessionid);
	}


	private void addProgress() {
		// プログレスバー処理
		removeProgress();
		TranslateAnimation pAnimation = new TranslateAnimation(
				Animation.RELATIVE_TO_PARENT, 0.0f,
				Animation.RELATIVE_TO_PARENT, 0.0f,
				Animation.RELATIVE_TO_PARENT, 1.0f,
				Animation.RELATIVE_TO_PARENT, 0.0f);
		pAnimation.setDuration(400);
		pAnimation.setFillAfter(true);
		try{
		// ここでfindViewByIdせざるおえないのかな～
		p = (ProgressBar) gateView.getPParent().findViewById(
				R.id.ProgressBarHorizontal);// 毎回生成しないとできない
		p.setMax(100);
		p.setProgress(1);
		}catch(IllegalArgumentException e){//背面の時にGateでActivity初期化と画面回転との関係でなる
			e.printStackTrace();
			return;
		}
		if(gateView.getPArea().getChildCount() > 0){
			gateView.getPArea().removeView(p);
		}
		gateView.getPArea().addView(p, new LinearLayout.LayoutParams(-1, -1));
		p.startAnimation(pAnimation);
		gateView.getMainTable().setPadding(0, 35,0,0);
	}

	private void removeProgress() {
		if (p != null) {
			TranslateAnimation animation = new TranslateAnimation(
					Animation.RELATIVE_TO_PARENT, 0.0f,
					Animation.RELATIVE_TO_PARENT, 0.0f,
					Animation.RELATIVE_TO_PARENT, 0.0f,
					Animation.RELATIVE_TO_PARENT, 1.0f);
			animation.setDuration(400);
			animation.setFillAfter(true);
			try{
			p.startAnimation(animation);
			p.setProgress(100);
			gateView.getPArea().removeAllViews();
			p.setProgress(0);
			}catch(IllegalArgumentException e){//背面の時にGateでActivity初期化と画面回転との関係でなる
				e.printStackTrace();
				return;
			}
			gateView.getMainTable().setPadding(0,30,0,0);
		}
	}

	public void show(Configuration newConfig) {
		isOpened = true;
		if(gateView == null){//通信不通でなる
			return;
		}
		gateView.getView().setVisibility(View.VISIBLE);
		TranslateAnimation animation = new TranslateAnimation(
				Animation.RELATIVE_TO_PARENT, 0.0f,
				Animation.RELATIVE_TO_PARENT, 0.0f,
				Animation.RELATIVE_TO_PARENT, 1.0f,
				Animation.RELATIVE_TO_PARENT, 0.0f);
		animation.setDuration(300);
		animation.setFillAfter(true);
		gateView.getView().setAnimation(animation);
		onConfigChanged(newConfig);//ここでボタンがアドされる
		animation.start();
	}

	public void close() {
		if(NLiveRoid.isDebugMode)Log.d("NLiveRoid"," Gate close ---- ");
		TranslateAnimation animation = new TranslateAnimation(
				Animation.RELATIVE_TO_PARENT, 0.0f,
				Animation.RELATIVE_TO_PARENT, 0.0f,
				Animation.RELATIVE_TO_PARENT, 0.0f,
				Animation.RELATIVE_TO_PARENT, 1.0f);
		animation.setDuration(200);
		animation.setFillAfter(true);
		if(TransDiscr.getACT() != null){
			TransDiscr.getACT().finish();
		}
		if(gateView == null){
			return;
		}
		gateView.getView().setAnimation(animation);
		animation.start();
		animation.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationEnd(Animation arg0) {
				close_noanimation();
			}

			@Override
			public void onAnimationRepeat(Animation arg0) {
			}

			@Override
			public void onAnimationStart(Animation arg0) {
			}
		});
	}

	public void close_noanimation() {
		if(NLiveRoid.isDebugMode)Log.d("NLiveRoid"," Gate close_noanimation ---- ");
		if(tweetDialog != null && tweetDialog.isShowing())return;//TweetDialog表示中は画面回転でレイアウトがおかしくなるので閉じない
		isOpened = false;
		if(gateView == null){
			return;
		}
		View v = gateView.getView();
		if(v == null){
			return;
		}
		ViewGroup vg = (ViewGroup) gateView.getView().getParent();
		if (vg != null) {
			vg.removeView(gateView.getView());
		}
	}



	/**
	 * getPlayerにオーナーIDを取得に行くだけ
	 *
	 */
	class OwnerParseTask extends AsyncTask<String,Void,ErrorCode>{
		@Override
		protected ErrorCode doInBackground(String... arg0) {

			ErrorCode error = ((NLiveRoid) ACT.getApplicationContext())
					.getError();
			if (error == null) {
			return null;
			} else if (error.getErrorCode() != 0) {
				error.setErrorCode(-28);
				return error;
			}else if(arg0[0] == null){
				return null;
			}
// セッション取得
			if (error.getErrorCode() != 0) {
				error.setErrorCode(-28);
				return error;
			}
			if (p != null) {
				p.setProgress(10);
			}
			if(liveinfo == null || liveinfo.getLiveID() == null){
				error.setErrorCode(-28);
				return error;
			}
			InputStream source = Request.doGetToInputStreamFromFixedSession(
					arg0[0],URLEnum.GETPLAYER + liveinfo.getLiveID(), error);
			if (source == null) {
				error.setErrorCode(-28);
				return error;
			}
			if (p != null) {
				p.setProgress(15);
			}
			byte[] b = null;
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			int size = 0;
			byte[] byteArray = new byte[1024];
			try {
				while ((size = source.read(byteArray)) != -1) {
					bos.write(byteArray, 0, size);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			b = bos.toByteArray();
			if(b == null ){
				error.setErrorCode(-28);
				return error;
			}
			String result = XMLparser.getLiveInfoInputStreamOnlyOwner(b);
			if(result == null){
				error.setErrorCode(-28);
				return error;
			}else{
				liveinfo.setOwnerID(result);
			}
			Uri uri = Uri.parse(URLEnum.USERPAGE + result);
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.addCategory(Intent.CATEGORY_BROWSABLE);
		i.setDataAndType(uri, "text/html");
		ACT.startActivityForResult(i,CODE.RESULT_REDIRECT);//リダイレクトコード
//			Log.d("log","RESULT  " + result);
			return error;
		}

		@Override
		protected void onPostExecute(ErrorCode error){
			if(error == null){
				MyToast.customToastShow(ACT, "放送主の情報取得に失敗しました");//エラーがNULLだと取得失敗よりおかしいけど。。
			}else if(error.getErrorCode() != 0){
				error.showErrorToast();
			}
		}

	}

	//TSの予約ボタンタップ時の動作
	class ReserveFirstTask extends AsyncTask<Void,Void,Integer>{
		private ProgressDialog pd;
		private ErrorCode error;
		protected ReserveFirstTask(){
			pd = new ProgressDialog(ACT);
			pd.setMessage("情報取得中");
			pd.show();
			error = ((NLiveRoid) ACT.getApplicationContext())
					.getError();
		}
		@Override
		public void onCancelled(){
			super.onCancelled();
			if(pd != null && pd.isShowing())pd.cancel();
		}
		@Override
		protected Integer doInBackground(Void... arg0) {
			if (error == null) {
			return -1;
			} else if (error.getErrorCode() != 0) {
				return 0;
			}
// セッション取得
			String session = Request.getSessionID(error);
			if (error.getErrorCode() != 0) {
				return 0;
			}
			if(liveinfo == null || liveinfo.getLiveID() == null){
				error.setErrorCode(-28);
				return 0;
			}
			String url = String.format(URLEnum.RESERVATION_FIRST ,liveinfo.getLiveID().replace("lv", ""),liveinfo.getLiveID(),liveinfo.getLiveID());
			if(NLiveRoid.isDebugMode)Log.d("NLIveRoid","ReserveCommand0 " + url );
			InputStream is = Request.doGetToInputStreamFromFixedSession(session,
					url, error);
			if(is == null){
				return -2;
			}else if (error.getErrorCode() != 0) {
				error.setErrorCode(-28);
				return 0;
			}
			byte[] b = null;
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			int size = 0;
			byte[] byteArray = new byte[1024];
			try {
				while ((size = is.read(byteArray)) != -1) {
					bos.write(byteArray, 0, size);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			b = bos.toByteArray();
			String source;
			try {
				source = new String(b,"UTF-8");
				if(NLiveRoid.isDebugMode)Log.d("NLiveRoid","RESERVE0 response " + source);
				Matcher ulck = Pattern.compile("ulck_[0-9]+").matcher(source);
				if(ulck.find()){
					liveinfo.setTsReserveToken(ulck.group());
				}else{
					return -3;
				}
			} catch (UnsupportedEncodingException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			}
			return 0;
		}
		@Override
		protected void onPostExecute(Integer arg){
			if(pd != null && pd.isShowing())pd.cancel();
			if(arg == 0 && error != null && error.getErrorCode() != 0){
				error.showErrorToast();
			}else if(arg == -1){
				MyToast.customToastShow(ACT, "不明のエラーでした");
			}else if(arg == -2){
				MyToast.customToastShow(ACT, "予約に必要な情報取得に失敗しました");
			}else if(arg == -3){
				new AlertDialog.Builder(ACT)
				.setMessage("(コミュニティ限定、予約済み、又は有料CHや公式等でした)\n生放送マイページをブラウザで開きますか?")
				.setPositiveButton("YES", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent i = new Intent(Intent.ACTION_VIEW);
										i.addCategory(Intent.CATEGORY_BROWSABLE);
										i.setDataAndType(Uri.parse(URLEnum.MYPAGE), "text/html");
										ACT.startActivity(i);
					}
				}).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				}).create().show();
			}else{
				new AlertDialog.Builder(ACT)
				.setMessage("この放送を予約しますか?")
				.setPositiveButton("予約する", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						new ReserveSecondTask().execute();
					}
				})
				.setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {}
				}).create().show();
			}
		}
		class ReserveSecondTask extends AsyncTask<Void,Void,Integer>{
			@Override
			protected Integer doInBackground(Void... params) {
				String session = Request.getSessionID(error);
				if (error.getErrorCode() != 0) {
					return 0;
				}
				if(liveinfo == null || liveinfo.getLiveID() == null){
					error.setErrorCode(-28);
					return 0;
				}
				try {
				HttpURLConnection con = (HttpURLConnection)new URL(URLEnum.RESERVATION_FIRST.split("\\?")[0]).openConnection();
				con.setRequestMethod("POST");
				con.setRequestProperty("Cookie", session);
				con.setDoOutput(true);
				PrintStream out = new PrintStream(con.getOutputStream());
				out.print("mode=regist&vid="+liveinfo.getLiveID().replace("lv", "")+"&token="+liveinfo.getTsReserveToken());
				InputStream is = con.getInputStream();
				if(is == null){
					return -2;
				}
				byte[] b = null;
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				int size = 0;
				byte[] byteArray = new byte[1024];
					while ((size = is.read(byteArray)) != -1) {
						bos.write(byteArray, 0, size);
					}
				b = bos.toByteArray();
				String source = new String(b,"UTF-8");
					if(NLiveRoid.isDebugMode)Log.d("NLiveRoid","RESERVE2 response " + source);
					Matcher regist_finish = Pattern.compile("id=\"regist_finished").matcher(source);
					if(regist_finish.find()){
						return 1;
					}else{
						return -3;
					}
				} catch (UnsupportedEncodingException e) {
					// TODO 自動生成された catch ブロック
					e.printStackTrace();
				} catch (MalformedURLException e1) {
					// TODO 自動生成された catch ブロック
					e1.printStackTrace();
				} catch (IOException e1) {
					// TODO 自動生成された catch ブロック
					e1.printStackTrace();
				}
				return 0;
			}
			@Override
			protected void onPostExecute(Integer arg){
				if(pd != null && pd.isShowing())pd.cancel();
				if(arg == 0 && error != null && error.getErrorCode() != 0){
					error.showErrorToast();
				}else if(arg == -1){
					MyToast.customToastShow(ACT, "不明のエラー2でした");
				}else if(arg == -2){
					MyToast.customToastShow(ACT, "予約2に必要な情報取得に失敗しました");
				}else if(arg == -3){
					MyToast.customToastShow(ACT, "予約2に必要なTokenの取得に失敗しました");
				}else if(arg == 1){
					MyToast.customToastShow(ACT, "予約しました");
				}
			}
		}

	}

	/**
	 * isOpenedを取得します。
	 *
	 * @return isOpened
	 */
	public boolean isOpened() {
		return isOpened;
	}

	/**
	 * isOpenedを設定します。
	 *
	 * @return isOpened
	 */
	public void setOpened(boolean isopen) {
		isOpened = isopen;
	}

	class TextSelectArea extends TextView {
		private TableLayout pView;
		private Rect rect;
		// Spannable インターフェイスは SPAN を組み込む機能
		private int scrollVert;
		private int scrollHor;

		public TextSelectArea(Context context, TableLayout pView) {
			super(context);
			setTextColor(Color.BLACK);
			rect = new Rect();
			this.pView = pView;
		}

		// Viewの座標系から文字オフセットに変換
		public int getOffset(int x, int y) {
			x += pView.getScrollX();
			y += pView.getScrollY();
			// getLineTop(line)でこのViewのその行のy座標
			// getOffsetForHorizontal(line,X座標)でこのViewのその行のそのX座標だと全体の何文字目か
			Layout l = getLayout();
			int line = l.getLineForVertical(y);
			if (line == 0 && y < l.getLineTop(line))
				return 0;
			if (line >= l.getLineCount() - 1 && y >= l.getLineTop(line + 1))
				return l.getText().length();
			int offset = l.getOffsetForHorizontal(line, x);
			return offset;
		}

		// ドラッグ終了位置を更新
		private void updateDragSelection(int x, int y, boolean finish) {
			// if(isSelectMode){
			// pView.requestDisallowInterceptTouchEvent(true);//選択モードじゃなければスクロールする
			// }else{
			// pView.requestDisallowInterceptTouchEvent(false);
			// }
			if (mOffsetDragStart != -1) {
				// ScrollViewとの親和性のため、移動中の誤差は無視する
				if (!finish) {
					int dx = x - mDragStartX;
					if (dx < 0)
						dx = -dx;
					int dy = y - mDragStartY;
					if (dy < 0)
						dy = -dy;
					int lh = getLineHeight();
					// 1行の高さに対して横方向3割、縦方向7割くらい
					if (dx * 10 < lh * 3 && dy * 10 < lh * 7)
						return;
				} else if (isFinished) {
					title_text.clearSelect();
					desc_text.clearSelect();
				}
				mOffsetDragEnd = getOffset(x, y);
				getGlobalVisibleRect(rect);

				// Log.d("log","START " + mOffsetDragStart +" " + mOffsetDragEnd
				// + " " + x + " " + y + " rect " + rect.left + " " +
				// rect.right);
				if (mOffsetDragStart < mOffsetDragEnd) {
					clipbordText = getText().toString();
					Spannable sn = factory.newSpannable(clipbordText);
					sn.setSpan(new BackgroundColorSpan(Color.BLACK),
							mOffsetDragStart, mOffsetDragEnd,
							Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					setText(sn);
				} else if (mOffsetDragEnd < mOffsetDragStart) {
					clipbordText = getText().toString();
					Spannable sn = factory.newSpannable(clipbordText);
					sn.setSpan(new BackgroundColorSpan(Color.BLACK),
							mOffsetDragEnd, mOffsetDragStart,
							Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					setText(sn);
				} else if (mOffsetDragStart == mOffsetDragEnd) {
					title_text.clearSelect();
					desc_text.clearSelect();
				}
				if (finish) {
					isFinished = true;
				}
			}

		}

		private void waitMoment() {
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}catch(IllegalArgumentException e){
				e.printStackTrace();
			}
		}

		public void clearSelect() {
			String nowText = getText().toString();
			clipbordText = "";// 空文字
			Spannable sn = factory.newSpannable(nowText);
			sn.setSpan(new BackgroundColorSpan(Color.WHITE), 0,
					nowText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			setText(sn);
		}

	}

	/**
	 * 文字列からセットできるSpannableに変換
	 *
	 * @param str
	 * @return
	 */
	private Spannable getSpannable(String str) {

		// それぞれのタグを処理する
		ArrayList<String> fIndex = new ArrayList<String>();
		ArrayList<String> uIndex = new ArrayList<String>();
		ArrayList<String> sIndex = new ArrayList<String>();
		ArrayList<String> iIndex = new ArrayList<String>();
		ArrayList<String> bIndex = new ArrayList<String>();
		int i = 0;
		int j = 0;
		int k = 0;
		String[] temp = null;
		String size = "1";
		String color = "#000000";
		Pattern sizept = Pattern.compile("size");
		Pattern colorpt = Pattern.compile("color");
		Matcher mc = null;
		HashMap<String, String> colorMap = ((NLiveRoid) ACT
				.getApplicationContext()).getColorMap();
		//改行なくす
		str = str.replaceAll("\n", "");
		str = str.replaceAll("<br>", "\n");
		//とりあえずu i s a なし
		str = str.replaceAll("<u>|</u>|<i>|</i>|<s>|</s>|<b>|</b>","");
			for(int links = 0; links < 100; links++){
				str = str.replaceAll("<<LINK"+links+">>.*<<LINK"+links+">>","");
			}
		int isNest = 0;
		ArrayList<Integer> nest = new ArrayList<Integer>();
		ALLCANCEL://チャンネルとかで、fontタグを無視してテキスト読み込み
		// fontタグ
		while (true) {
			i = str.indexOf("<font",0);//一番上の階層のフォントタグ
			if (i == -1) {
				break;
			}
			// 終了タグがあるかを調べる パース自体が失敗してたらここまでが全て適応される
			k = str.indexOf("</font>", i);
			if (k == -1) {
				break;// 無ければ適応しない
			}
			nest.add(i);//フォントタグ発見

							while(true){//入れ子のスタート位置を全て入れておく
							isNest = str.indexOf("<font",nest.get(nest.size()-1)+1);//
							if(isNest == -1){
								break;
							}
							if(isNest < k){//入れ子だった
								int K = str.indexOf("</font>", isNest);
							if (K == -1) {
								break;// 無ければ適応しない
							}
				//			Log.d("log","isNext " + isNest + " K " + k);
				//			Log.d("log","STR =-- " + str.substring(isNest, k));
							k = K;
							nest.add(isNest);
				//			Log.d("log","NEST SIZE " + nest.size());
							}else{//入れ子じゃなければ終了
							break;
							}
							}
			while(true){

				try{//公式や、チャンネルで、コミュニティが普通のサイトに繋がっている場合がある
			// タグ終わりの>の位置を取得
			j = str.indexOf(">", nest.get(nest.size()-1));//一番深い入れ子のfontタグ終わり
			k = str.indexOf("</font>", nest.get(nest.size()-1));
			//サイズと色の処理
			temp = str.substring(nest.get(nest.size()-1), j).split("=| ");// 例 <font size=3でfont,size,3　color=#ff0000> でcolor,#ff0000
				// サイズと色を配列にCSVで詰める
				for (int x = 0; x < temp.length; x++) {
					mc = sizept.matcher(temp[x]);
					if (mc.find()) {
						try {
							size = temp[x + 1].substring(0, 1);// サイズは1桁という事を想定
						} catch (NumberFormatException e) {
							break;
						}
						size = temp[x + 1];
					}
					mc = null;
					mc = colorpt.matcher(temp[x]);
					if (mc.find()) {
						// Log.d("log","F ---- " + temp[x+1]);
						try {
							if (colorMap.get(temp[x + 1]) != null) {//REDとかの時
								color = colorMap.get(temp[x + 1]);
							} else {
								color = temp[x + 1].substring(0, 7);// #XXXXXXとかの時
							}
						} catch (StringIndexOutOfBoundsException e) {
							break;
						}
					}
				}
				}catch(ArrayIndexOutOfBoundsException e){
					Log.d("NLiveRoid","Gate Spannable error --- 0 - 0");
					break ALLCANCEL;
				}catch(StringIndexOutOfBoundsException e){
					Log.d("NLiveRoid","Gate Spannable error --- 1 - 0");
					break;
				}
				//ここでnest.get(nest.size()-1)～jまでがタグ、kからk+7までが終了タグ
				//なのでそれらを計算して消す
				//0からタグ前まで、インナーテキスト、タグ終わり後から最後まで
				try{
//					if(str.substring(0,nest.get(nest.size()-1)).length() > 10){
//						Log.d("Log","BEFORE---- "+str.substring(0,nest.get(nest.size()-1)).substring(nest.get(nest.size()-1)-5, nest.get(nest.size()-1)));
//					}
//					Log.d("Log","STR " + str.substring(j+1, k));
//					if(str.substring(k+7).length() > 10){
//						Log.d("Log","K+7 " + str.substring(k+7).substring(0,8));
//					}
				str = str.substring(0,nest.get(nest.size()-1))+str.substring(j+1,k)+str.substring(k+7);
				int tagLength = j-nest.get(nest.size()-1);//前に換算する長さ

				//2回目以降のaddで、今のnestより後の位置ならその分前に持っていかなきゃいけない
				for(int index = 0; index < fIndex.size(); index++){
					if(Integer.parseInt(fIndex.get(index).split(",")[0]) > nest.get(nest.size()-1)){
						fIndex.set(index, (Integer.parseInt(fIndex.get(index).split(",")[0])-tagLength-1)+","+
								(Integer.parseInt(fIndex.get(index).split(",")[1])-tagLength-1)+","+
								fIndex.get(index).split(",")[2]+","+
								fIndex.get(index).split(",")[3]);
					}
					//後の位置に来るタグが入れ子になっていない場合終わりタグ(</font>)分も引かなければいけない
//					if(Integer.parseInt(fIndex.get(index).split(",")[0]) > k-tagLength){
//						fIndex.set(index, (Integer.parseInt(fIndex.get(index).split(",")[0])-7)+","+
//								(Integer.parseInt(fIndex.get(index).split(",")[1])-7)+","+
//								fIndex.get(index).split(",")[2]+","+
//								fIndex.get(index).split(",")[3]);
//					}
				}
				//上のstr = str.substring処理で、インナーが、str.substring(j+1,k)だから
				fIndex.add((nest.get(nest.size()-1)) + "," + (k-tagLength-1) + "," + size + "," + color);//スタート、終わり、サイズ、位置
				size = "1";
				color = "#000000";
				nest.clear();
				break;
		}catch(StringIndexOutOfBoundsException e){
					Log.d("NLiveRoid","Gate Spannable ERROR --- 2 - 0");
					break;
				}
	}

		}
		//この時点で最後に</font>とかあるのは不正なので消す
		str = str.replaceAll("</font>", "");
		//ここから先文字を消すと行数が合わなくなる
		//とりあえず入れ子なし
		// uタグ
//		i = 0;
//		k = 0;
//		while (true) {
//			i = str.indexOf("<u>", i);
//			if (i != -1) {
//				k = str.indexOf("</u>", i+1);
//				if (k == -1) {
//					Log.d("log","UTAG NO END ");
//					break;
//				}
//				try{
//					str = str.substring(0,i-1) + str.substring(i+3, k) + str.substring(k+4);
//
//					Log.d("LOG","DEBUG 0--- " + i + " " + k + " " + str.substring(k-3));
//				int diff = 3;//タグの長さ<u>
//				for(int index = 0; index < fIndex.size(); index++){
//					if(Integer.parseInt(fIndex.get(index).split(",")[0]) > i){
//						fIndex.set(index, (Integer.parseInt(fIndex.get(index).split(",")[0])-diff)+","+
//								(Integer.parseInt(fIndex.get(index).split(",")[1])-diff)+","+
//								fIndex.get(index).split(",")[2]+","+
//								fIndex.get(index).split(",")[3]);
//					}
//				}
//
//				Log.d("Log","U " + i  + " " + (k-diff));
//				uIndex.add((i) + "," + (i+k-diff));
//				i++;
//				}catch(StringIndexOutOfBoundsException e){
//					Log.d("log","ERROR BREAK UTAG---");
//					break;
//				}
//			}else {
//				break;
//			}
//		}
//		// iタグ
//		i = 0;
//		k = 0;
//		while (true) {
//			i = str.indexOf("<i>", i);
//			// Log.d("Log","I -- " + i);
//			if (i != -1) {
//				k = str.indexOf("</i>", i);
//				if (k == -1) {
//					break;
//				}
//				iIndex.add((i + 3) + "," + (k));
//			} else {
//				break;
//			}
//			i++;
//		}
//		// sタグ
//		i = 0;
//		k = 0;
//		while (true) {
//			i = str.indexOf("<s>", i);
//			// Log.d("Log","S -- " + i);
//			if (i != -1) {
//				k = str.indexOf("</s>", i);
//				if (k == -1) {
//					break;
//				}
//				sIndex.add((i + 3) + "," + (k));
//			} else {
//				break;
//			}
//			i++;
//		}
//		// bタグ
//		i = 0;
//		k = 0;
//		while (true) {
//			i = str.indexOf("<b>", i);
//			if (i != -1) {
//				k = str.indexOf("</b>", i);
//				if (k == -1) {
//					break;
//				}
//				// Log.d("Log","B -- " + i + " " + k);
//				bIndex.add((i + 3) + "," + (k));
//			} else {
//				break;
//			}
//			i++;
//		}
		temp = null;
		Spannable sn = factory.newSpannable(str);
		try {
			// fサイズ
			//スタートが早い方が優先されて適応してしまうのでソート
			Collections.sort(fIndex,new fIndexComparator());
			for (int a = 0; a < fIndex.size(); a++) {
				temp = fIndex.get(a).split(",");
//				Log.d("log","INDEX ---------- " + temp[0] + " " + temp[1] + " " + temp[3]);
//				Log.d("log"," ----------" + str.substring(Integer.parseInt(temp[0]),Integer.parseInt(temp[1])));
				if(Integer.parseInt(temp[0]) > str.length()){
					break;
				}else if(Integer.parseInt(temp[1]) > str.length()){
					temp[1] = String.valueOf(str.length()-1);
				}
				sn.setSpan(
						new AbsoluteSizeSpan(Integer.parseInt(temp[2])*2+20),
						Integer.parseInt(temp[0]), Integer.parseInt(temp[1]),
						Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				int tempCol = -1;
				try{
				tempCol = Color.parseColor(temp[3]);
				}catch(IllegalArgumentException e){
					continue;
				}
				sn.setSpan(new ForegroundColorSpan(tempCol),
						Integer.parseInt(temp[0]), Integer.parseInt(temp[1]),
						Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
			temp = null;
//			// f色
//			for (int a = 0; a < fIndex.size(); a++) {
//				temp = fIndex.get(a).split(",");
//				Log.d("log","INDEX ---------- " + temp[0] + " " + temp[1]);
//				int tempCol = 0;
//				try {
//					tempCol = Color.parseColor(temp[3]);
//				} catch (IllegalArgumentException e) {
//					Log.d("log", "COLOR ERROR --- " + temp[3]);
//					break;
//				}
//				if(Integer.parseInt(temp[0]) > str.length()){
//					break;
//				}else if(Integer.parseInt(temp[1]) > str.length()){
//					temp[1] = String.valueOf(str.length()-1);
//				}
//				sn.setSpan(new ForegroundColorSpan(tempCol),
//						Integer.parseInt(temp[0]), Integer.parseInt(temp[1]),
//						Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//			}

//			temp = null;
			// u
//			for (int a = 0; a < uIndex.size(); a++) {
//				temp = uIndex.get(a).split(",");
//				sn.setSpan(new UnderlineSpan(), Integer.parseInt(temp[0]),
//						Integer.parseInt(temp[1]),
//						Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//			}

//			temp = null;
//			// i
//			for (int a = 0; a < iIndex.size(); a++) {
//				temp = iIndex.get(a).split(",");
//				sn.setSpan(new StyleSpan(android.graphics.Typeface.ITALIC),
//						Integer.parseInt(temp[0]), Integer.parseInt(temp[1]),
//						Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//			}
//			temp = null;
//			// s
//			for (int a = 0; a < sIndex.size(); a++) {
//				temp = sIndex.get(a).split(",");
//				sn.setSpan(new StrikethroughSpan(), Integer.parseInt(temp[0]),
//						Integer.parseInt(temp[1]),
//						Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//			}
//			temp = null;
//			// b
//			for (int a = 0; a < bIndex.size(); a++) {
//				temp = bIndex.get(a).split(",");
//				Log.d("log", "B " + temp[0] + " " + temp[1]);
//				sn.setSpan(new StyleSpan(android.graphics.Typeface.BOLD),
//						Integer.parseInt(temp[0]), Integer.parseInt(temp[1]),
//						Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//			}
		} catch (NumberFormatException e) {
			Log.d("NLiveRoid","Gate error 3-0 " );
			 e.printStackTrace();
		} catch (IndexOutOfBoundsException e) {
			Log.d("NLiveRoid","Gate error 3-1");
			 e.printStackTrace();
		}
		return sn;
	}

	private void fileWrite(String str){
		try {
			Context mContext = ACT.createPackageContext("nliveroid.nlr.main", ACT.CONTEXT_RESTRICTED);
		FileOutputStream fos = mContext.openFileOutput("TEST.txt", ACT.MODE_WORLD_WRITEABLE);
		fos.write(str.getBytes());
		fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}

	}
	class fIndexComparator implements java.util.Comparator {
		public int compare(Object s, Object t) {
			return Integer.parseInt(((String)s).split(",")[0]) - Integer.parseInt(((String)t).split(",")[0]);
		}
	}

	class ThumbNailTask extends AsyncTask<Void,Void,Integer>{

		@Override
		protected Integer doInBackground(Void... arg0) {
			// サムネイルを取得
			Bitmap bm = null;
			if(liveinfo == null || liveinfo.getCommunityID() == null){
				return -1;
			}
			ErrorCode error = ((NLiveRoid)ACT.getApplicationContext()).getError();
			if(error == null){
				return -2;
			}
			if(liveinfo.getCommunityID().contains(URLEnum.HYPHEN)){//公式
				bm = Request.getImageForList(String.format(URLEnum.OFFICIALTHUMB,
						liveinfo.getThumbnailURL()),error,0);
			}else if(liveinfo.getCommunityID().contains("ch")){//チャンネル
				bm = Request.getImageForList(String.format(URLEnum.BITMAPSCHANNEL,
						liveinfo.getCommunityID()),error,0);
			}else if(liveinfo.getCommunityID().contains("co")){//ユーザー
				bm = Request.getImageForList(String.format(URLEnum.BITMAPSCOMMUNITY,
						liveinfo.getCommunityID()),error,0);
			}
			if(bm != null){
				liveinfo.setThumbnail(bm);
			}else{
				Log.d("Log","THUMBNAIL ERROR ");
			}
			return 0;
		}

		protected void onPostExecute(Integer arg){
			if(arg == 0){
				gateView.getCommuThumbView()
				.setImageBitmap(liveinfo.getThumbnail());
				gateView.getCommuThumbView().invalidate();
			}
		}

	}

	/**
	 * gateViewを取得します。
	 * @return gateView
	 */
	public GateView getGateView() {
	    return gateView;
	}

}
