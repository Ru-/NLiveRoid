package nliveroid.nlr.main;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.concurrent.RejectedExecutionException;

import nliveroid.nlr.main.parser.MyTumbURLParser;
import nliveroid.nlr.main.parser.NicoRepoParser;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.ClipboardManager;
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
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class HistoryTab extends Activity implements Archiver,GatableTab{
		private MyArrayAdapter nicorepoAdapter;
		private DBAdapter dbAdapter;
		private LayoutInflater inflater;
		private static HistoryTab ACT;
		private View parent;
		private ErrorCode error;

		private ViewGroup progressArea;
		private ProgressBar progressBar;

		private NicoRepoTask nicorepoTask;
		private ListView listview;
		private ListView dblistview;


		private Gate gate;

		private static boolean isListTaped = false;

		private byte toptab_tcolor = 0;

		private AlertDialog accountDialog;

		private Bitmap myThumb;
		private LinearLayout tabHeader;
		private TextView headerText;

		private boolean isDBView;
		private LinearLayout movePaneRoot;
		private LinearLayout nicorepoPane;
		private TableRow nicorepoRow;
		private LinearLayout dbPane;
		private TableRow dbRow;
		private ArrayList<DBBean> dbBeans;
		private boolean[] filters;


		private byte repoIndex;
		private TableLayout rowTableParent;
		private CharSequence[] repo_list;
		 private boolean isContextOperation;

		@Override
		public void onCreate(Bundle bundle){
			super.onCreate(bundle);
			ACT = this;
			requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
			setVolumeControlStream(AudioManager.STREAM_MUSIC);
			inflater = LayoutInflater.from(this);
			parent = inflater.inflate(R.layout.historytab, null);
			final NLiveRoid app = (NLiveRoid) getApplicationContext();
			error = app.getError();
			//背景にヘッダーをセットするか判定する
			headerText = (TextView) parent.findViewById(R.id.history_titletext);
			toptab_tcolor = app.getDetailsMapValue("toptab_tcolor") == null? 0:Byte.parseByte(app.getDetailsMapValue("toptab_tcolor"));
			TopTabs.setTextColor(headerText,toptab_tcolor);
			//全てのコミュニティの一覧初期化
			movePaneRoot = (LinearLayout)parent.findViewById(R.id.his_movepane_root);
			nicorepoPane = (LinearLayout)parent.findViewById(R.id.his_live_pane);
			tabHeader = (LinearLayout)parent.findViewById(R.id.his_titlebar);

			tabHeader.setOnClickListener(new View.OnClickListener(){
				@Override
				public void onClick(View v) {
					Log.d("NLiveRoid","HISVALUE " + TopTabs.his_value + " " + Integer.toBinaryString(TopTabs.his_value));
					newDBPane();
					//ヘッダータップしたらレイアウト変更
					rowTableParent.removeAllViews();
					movePaneRoot.removeAllViews();
					if(isDBView){
						rowTableParent.addView(nicorepoRow);
						if(dbAdapter != null)dbAdapter.clear();
						movePaneRoot.addView(nicorepoPane);
						isDBView = false;
					}else{
						rowTableParent.addView(dbRow);
						if(nicorepoAdapter != null)nicorepoAdapter.clear();
						if(dbPane == null){
							dbPane = new LinearLayout(ACT);
							dbPane.addView(dblistview,new LinearLayout.LayoutParams(-1,-1));
						}
						movePaneRoot.addView(dbPane,new LinearLayout.LayoutParams(-1,-1));
						isDBView = true;
					}
					onReload();
				}
			});
			try{//背景画像をセットする
				FileInputStream back_t_file  = openFileInput("back_t");
				Bitmap back = BitmapFactory.decodeStream(back_t_file);
				parent.setBackgroundDrawable(new BitmapDrawable(back));
			} catch (FileNotFoundException e) {
				tabHeader.setBackgroundDrawable(getResources().getDrawable(R.drawable.header));
//				e.printStackTrace();
			}catch(OutOfMemoryError e){
				e.printStackTrace();
				MyToast.customToastShow(this, "背景画像が大きすぎたため、適用に失敗しました");
			}catch(Exception e){
				e.printStackTrace();
				MyToast.customToastShow(this, "背景画像適用時エラー");
			}
		    setContentView(parent);

		    progressArea = (ViewGroup)parent.findViewById(R.id.progresslinear);
		    progressBar = (ProgressBar)parent.findViewById(R.id.ProgressBarHorizontal);
		    progressArea.removeView(progressBar);
			View pParent = inflater.inflate(R.layout.progressbar, null);
			progressBar = (ProgressBar) pParent
			.findViewById(R.id.ProgressBarHorizontal);

		    nicorepoAdapter = new MyArrayAdapter(this);
		    listview = (ListView)parent.findViewById(android.R.id.list);
			listview.setAdapter(nicorepoAdapter);
			listview.setFastScrollEnabled(true);
			listview.setDrawingCacheEnabled(false);
			registerForContextMenu(listview);
//			if(repoIndex == 1){//コミュIDが入ってくるのは「自分」以外のフィルタ
//            	holder.co_or_owner.setText(item.getOwnerName());
//            	holder.commu_name.setText(URLEnum.HYPHEN);
//            }else{
//            	holder.co_or_owner.setText(item.getCommunityID());
//            	holder.commu_name.setText(item.getCommunityName());
//            }
//            holder.title.setText(item.getTitle());
//            holder.time.setText(item.getCommunity_info());
			listview.setOnItemClickListener(new OnItemClickListener() {
				@Override
				 public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					 if(isContextOperation){
	   					 isContextOperation = false;
	   					 return;
	   				 }
					 final int pos = position;
					 //ニコる、コメント はPC版のみ
					 final LiveInfo l = nicorepoAdapter.getItem(position);
//					 Log.d("NLiveRoid"," ----------------- " + l.getCommunityID());
					 if(l.getLiveID() != null && ! l.getCommunityID().equals(URLEnum.HYPHEN)){//getOwnerNameにcoを入れている場合があるが、とりあえず考慮してない
						 new AlertDialog.Builder(ACT)
						 .setItems(new CharSequence[]{"視聴","詳細","最近の放送履歴","ブラウザ"},new DialogInterface.OnClickListener(){
							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								switch(arg1){
								case 0:
									startFlash(pos);
									break;
								case 1:
									 showGate(nicorepoAdapter.getItem(pos));
									break;
								case 2:
									NLiveRoid app = (NLiveRoid)getApplicationContext();//新たに取らないとWidthがセットされて無い気がする
									new LiveArchivesDialog(ACT,l.getCommunityID(),app.getViewWidth(),error).showSelf();
									break;
								case 3:
									browser(l);
									break;
								}
							}

						 }).create().show();
					 }else if(l.getLiveID() != null){
						 new AlertDialog.Builder(ACT)
						 .setItems(new CharSequence[]{"視聴","詳細"},new DialogInterface.OnClickListener(){
							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								switch(arg1){
								case 0:
									startFlash(pos);
									break;
								case 1:
									 showGate(nicorepoAdapter.getItem(pos));
									break;
								}
							}
						 }).create().show();
					 }else if(!l.getCommunityID().equals(URLEnum.HYPHEN)){
						 new AlertDialog.Builder(ACT)
						 .setItems(new CharSequence[]{"最近の放送履歴","ブラウザ"},new DialogInterface.OnClickListener(){
							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								switch(arg1){
								case 0:
									NLiveRoid app = (NLiveRoid)getApplicationContext();//新たに取らないとWidthがセットされて無い気がする
								new LiveArchivesDialog(ACT,l.getCommunityID(),app.getViewWidth(),error).showSelf();
									break;
								case 1:
									browser(l);
									break;
								}
							}
						 }).create().show();
					 }
				 }

				 private void startFlash(int position){
					 if(gate != null){
						 if(!gate.isOpened()){
							 startFlashPlayer(nicorepoAdapter.getItem(position));
						 	}
					 }else{
					 		startFlashPlayer(nicorepoAdapter.getItem(position));
					 }
				 }
    			 private void browser(final LiveInfo li) {
    				 new AlertDialog.Builder(ACT)
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
				 }

			});


			final Button reloadBt = (Button)parent.findViewById(R.id.commu_reload_bt);
			reloadBt.setOnClickListener(new View.OnClickListener(){
				@Override
				public void onClick(View v) {
					ACT.onReload();
				}
			});
			final Button settingBt = (Button)parent.findViewById(R.id.right_setting_bt);
			settingBt.setOnClickListener(new View.OnClickListener(){
				@Override
				public void onClick(View v) {
					Intent settingIntent = new Intent(ACT,SettingTabs.class);
		        	NLiveRoid app = (NLiveRoid)ACT.getApplicationContext();
		        	settingIntent.putExtra("session",app.getSessionid());
		        	startActivityForResult(settingIntent, CODE.REQUEST_SETTING_TAB);
				}
			});

			rowTableParent = (TableLayout)parent.findViewById(R.id.switch_linear);
			rowTableParent.setColumnStretchable(0, true);
			rowTableParent.setColumnStretchable(1, true);
			rowTableParent.setColumnStretchable(2, true);
			final Button filter_bt = new Button(this);
			filter_bt.setGravity(Gravity.CENTER);
			repo_list = new CharSequence[]{"すべて","自分","お気に入りユーザ","チャンネル&コミュニティ"};
			filter_bt.setText(repo_list[repoIndex]);
			filter_bt.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					new AlertDialog.Builder(ACT)
					.setItems(repo_list, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							repoIndex = (byte) which;
							filter_bt.setText(repo_list[repoIndex]);
							if(nicorepoAdapter != null)nicorepoAdapter.clear();
							addProgress();
							if(nicorepoTask != null && nicorepoTask.getStatus() != AsyncTask.Status.FINISHED){
								nicorepoTask.cancel(true);
							}
							nicorepoTask = new NicoRepoTask();
							nicorepoTask.execute();
						}
					})
					.create().show();

				}
			});
			nicorepoRow = new TableRow(this);
			nicorepoRow.addView(new LinearLayout(this),new TableRow.LayoutParams(-2,-1));//ダミーを入れる
			nicorepoRow.addView(new LinearLayout(this),new TableRow.LayoutParams(-2,-1));//ダミーを入れる
			nicorepoRow.addView(filter_bt,new TableRow.LayoutParams(-1,-1));
			rowTableParent.addView(nicorepoRow,new LinearLayout.LayoutParams(-1,-1));
			if(getIntent().getBooleanExtra("sole", false)){
				Log.d("NLiveRoid","S T");
				newDBPane();
				rowTableParent.removeAllViews();
				movePaneRoot.removeAllViews();
					rowTableParent.addView(dbRow);
					if(nicorepoAdapter != null)nicorepoAdapter.clear();
					if(dbPane == null){
						dbPane = new LinearLayout(ACT);
						dbPane.addView(dblistview,new LinearLayout.LayoutParams(-1,-1));
					}
					movePaneRoot.addView(dbPane,new LinearLayout.LayoutParams(-1,-1));
					isDBView = true;
					onReload();
			}
		}


		@Override
		public void onWindowFocusChanged(boolean hasFocus){
			super.onWindowFocusChanged(hasFocus);
		}

		@Override
		public void onConfigurationChanged(Configuration newConfig){
			super.onConfigurationChanged(newConfig);
			if(gate != null &&  gate.isOpened()){
				gate.onConfigChanged(newConfig);
			}
		}

		public static void cancelMovingTask(){
//			if(isFirstThread)return;
		}


		private void newDBPane(){
			Log.d("NLiveRoid" , "HISVALUE his_value" + TopTabs.his_value +" " +Integer.toBinaryString(TopTabs.his_value));
			final NLiveRoid app = (NLiveRoid) getApplicationContext();
			if(dbBeans == null)dbBeans = new ArrayList<DBBean>();
			if(dbRow == null){
				dbRow = new TableRow(ACT);
				if(filters == null)filters = new boolean[3];
				final CheckBox search = new CheckBox(ACT);
				search.setText("検索");
				if((TopTabs.his_value & 0x20) > 0 ){
					search.setChecked(true);
					filters[2] = true;
				}
				search.setOnCheckedChangeListener(new OnCheckedChangeListener(){
					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						filters[2] = isChecked;
						if(isChecked){
							TopTabs.his_value |= 0x20;
						}else{
							TopTabs.his_value &= 0x5F;
						}
						app.setDetailsMapValue("his_value",String.valueOf(TopTabs.his_value));
						applyFilter();
					}
				});
				final CheckBox desc = new CheckBox(ACT);
				desc.setText("詳細");
				if((TopTabs.his_value & 0x10) > 0 ){
					desc.setChecked(true);
					filters[1] = true;
				}
				desc.setOnCheckedChangeListener(new OnCheckedChangeListener(){
					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						filters[1] = isChecked;
						if(isChecked){
							TopTabs.his_value |= 0x10;
						}else{
							TopTabs.his_value &= 0x6F;
						}
						app.setDetailsMapValue("his_value",String.valueOf(TopTabs.his_value));
						applyFilter();
					}
				});
				final CheckBox view = new CheckBox(ACT);
				view.setText("視聴");
				if((TopTabs.his_value & 0x08) > 0 ){
					view.setChecked(true);
					filters[0] = true;
				}
				view.setOnCheckedChangeListener(new OnCheckedChangeListener(){
					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						filters[0] = isChecked;
						if(isChecked){
							TopTabs.his_value |= 0x08;
						}else{
							TopTabs.his_value &= 0x77;
						}
						app.setDetailsMapValue("his_value",String.valueOf(TopTabs.his_value));
						applyFilter();
					}
				});
				final CheckBox enable = new CheckBox(ACT);
				enable.setText("有効");
				if((TopTabs.his_value  & 0x40) > 0)enable.setChecked(true);
				enable.setOnCheckedChangeListener(new OnCheckedChangeListener(){
					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						if(isChecked){
							TopTabs.his_value = -1;//一括で有効
						    if(TopTabs.his_db == null){
						    	TopTabs.his_db = new HistoryDataBase(ACT);
						    TopTabs.his_db.getWritableDatabase();
						    }
							search.setChecked(true);
							desc.setChecked(true);
							view.setChecked(true);
						}else{
							TopTabs.his_value = 0;//一括で0にしちゃう
							search.setChecked(false);
							desc.setChecked(false);
							view.setChecked(false);
						}
						app.setDetailsMapValue("his_value",String.valueOf(TopTabs.his_value));
					}
				});
				Button remove = new Button(ACT);
				remove.setText("履歴削除");
				remove.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View v) {//DBから全ての履歴を削除して更新
							new AlertDialog.Builder(ACT)
							.setMessage("履歴を全て削除しますか?")
							.setPositiveButton("削除",new DialogInterface.OnClickListener(){
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									if(!enable.isChecked()){
										MyToast.customToastShow(ACT, "機能が有効ではありませんでした");
										return;
									}
									if(TopTabs.his_db == null){
										TopTabs.his_db = new HistoryDataBase(ACT);
										TopTabs.his_db.getWritableDatabase();
									}
										TopTabs.removeDBAll();
										onReload();
								}
							})
							.setNegativeButton("CANCEL",new DialogInterface.OnClickListener(){
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// TODO 自動生成されたメソッド・スタブ
								}
							})
							.create().show();
//							TopTabs.his_db
					}
				});
				dbRow.addView(enable,new TableRow.LayoutParams(-2,-1));
				dbRow.addView(search,new TableRow.LayoutParams(-2,-1));
				dbRow.addView(desc,new TableRow.LayoutParams(-2,-1));
				dbRow.addView(view,new TableRow.LayoutParams(-2,-1));
				dbRow.addView(remove,new TableRow.LayoutParams(-2,-1));
				}
				if(dblistview == null){
				    dbAdapter = new DBAdapter(ACT);

					dblistview = new ListView(ACT);
					dblistview.setDivider(new ColorDrawable(Color.WHITE));
					dblistview.setDividerHeight(1);//後からHeightを入れないといけないみたい
					dblistview.setAdapter(dbAdapter);
					dblistview.setFastScrollEnabled(true);
					dblistview.setDrawingCacheEnabled(false);
					dblistview.setOnItemClickListener(new OnItemClickListener() {
						@Override
						 public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
							final DBBean bean = dbAdapter.getItem(position);
							CharSequence[] items = null;
							boolean haslv = false;
							if(bean.getLv() != null && !bean.getLv().equals("")){
								haslv = true;
								items = new CharSequence[]{"視聴","詳細"};
							}else if(bean.getRemark0() != null && !bean.getRemark0().equals("")){
								items = new CharSequence[]{"検索","ブラウザ(大百科)","ブラウザ(ggr)","コピー"};
							}
							final boolean hasLV = haslv;
							new AlertDialog.Builder(ACT)
							.setItems(items,new DialogInterface.OnClickListener(){
								@Override
								public void onClick(DialogInterface arg0,
										int which) {
									if(hasLV){
										LiveInfo info = new LiveInfo();
										info.setLiveID(bean.getLv());
										if(bean.getCoch() != null && !bean.getCoch().equals("") && !bean.getCoch().equals("-")){
											info.setCommunityID(bean.getCoch());
										}
										if(bean.getRemark0() != null && !bean.getRemark0().equals("")){
											info.setTitle(bean.getRemark0());
										}
										if(bean.getRemark1() != null && !bean.getRemark1().equals(URLEnum.HYPHEN)){
											info.setComunityName(bean.getRemark1());
										}
										if(bean.getRemark2() != null && !bean.getRemark2().equals("")){
											info.setDescription(bean.getRemark2());
										}
										switch(which){
										case 0://視聴
											startFlashPlayer(info);
											break;
										case 1://詳細
											GateView gView = app.getGateView();
											if(gView == null)return;
											gate = new Gate(ACT,gView,info,false,Request.getSessionID(error));
											ViewGroup gateParent = (ViewGroup) app.getGateView().getView().getParent();
											if(gateParent != null){
												gateParent.removeView(app.getGateView().getView());
											}
											((ViewGroup)parent.getParent()).addView(app.getGateView().getView());
											gate.show(ACT.getResources().getConfiguration());
											break;
										}
									}else{
										switch(which){
										case 0://検索
											TopTabs.getACT().changeTag(1);
											SearchTab.getSearchTab()
											.keyWordSearch_FromGate(
													bean.getRemark0());
											break;
										case 1://大百科で検索 URLEncodeしなくていいみたい
											Uri uri = Uri.parse(String.format(URLEnum.DAIHYAKKA,bean.getRemark0()));
											Intent i = new Intent(Intent.ACTION_VIEW);
											i.addCategory(Intent.CATEGORY_BROWSABLE);
											i.setDataAndType(uri, "text/html");
											startActivityForResult(i,CODE.RESULT_REDIRECT);
											break;
										case 2://ブラウザで検索
											Intent search = new Intent(Intent.ACTION_WEB_SEARCH);
											search.putExtra(SearchManager.QUERY, bean.getRemark0());
											startActivity(search);
											break;
										case 3://コピー
											ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
											cm.setText(bean.getRemark0());
											break;
										}
									}
								}
							})
							.create().show();
						}
					});
				}
		}

	    class MyArrayAdapter extends ArrayAdapter<LiveInfo> {
	        public MyArrayAdapter(Context context) {
	            super(context,R.layout.list_row_history);
	        }
	        @Override
	        public View getView(int position, View view, ViewGroup parent) {
	        	LiveInfo item = getItem(position);
	            ViewHolder holder;
	            if (view == null) {
	                view = inflater.inflate(R.layout.list_row_history, null);
	                holder = new ViewHolder();
	                holder.thumbnail = (ImageView) view.findViewById(R.id.history_thumbnail);
	                holder.thumbnail.setTag(position);
					holder.thumb_progress = (FrameLayout)view.findViewById(R.id.progress_frame);
	                holder.lv = (TextView) view.findViewById(R.id.history_lv);
	                holder.co_or_owner = (TextView) view.findViewById(R.id.history_co_or_owner);
	                holder.commu_name = (TextView) view.findViewById(R.id.history_coname);
	                holder.title = (TextView) view.findViewById(R.id.history_title);
	                holder.time = (TextView) view.findViewById(R.id.history_time);
	                view.setTag(holder);
	            } else {
	                holder = (ViewHolder) view.getTag();
	            }
	            holder.lv.setText(item.getLiveID());
	            if(repoIndex == 1){//コミュIDが入ってくるのは「自分」以外のフィルタ
	            	holder.co_or_owner.setText(item.getOwnerName());
	            	holder.commu_name.setText(URLEnum.HYPHEN);
	            }else{
	            	holder.co_or_owner.setText(item.getCommunityID());
	            	holder.commu_name.setText(item.getCommunityName());
	            }
	            holder.title.setText(item.getTitle());
	            holder.time.setText(item.getCommunity_info());
	            Bitmap t = item.getThumbnail();
				ViewGroup vg = (ViewGroup)view;
				if(t != null){
					vg.removeView(holder.thumb_progress);
					holder.thumbnail.setImageBitmap(t);
				}
	            return view;
	        }
	        private class ViewHolder {
				public ImageView thumbnail;
	            public FrameLayout thumb_progress;
	            public TextView lv;
	            public TextView co_or_owner;
	            public TextView commu_name;
	            public TextView title;
	            public TextView time;
	        }
	    }

	    class DBAdapter extends ArrayAdapter<DBBean> {
	        public DBAdapter(Context context) {
	            super(context,R.layout.list_row_history_db);
	        }
	        @Override
	        public View getView(int position, View view, ViewGroup parent) {
	        	DBBean item = getItem(position);
	            ViewHolder holder;
	            if (view == null) {
	                view = inflater.inflate(R.layout.list_row_history_db, null);
	                holder = new ViewHolder();
	                holder.date = (TextView) view.findViewById(R.id.history_date);
	                holder.kind = (TextView) view.findViewById(R.id.history_kind);
	                holder.lv = (TextView) view.findViewById(R.id.history_lv);
	                holder.coch = (TextView) view.findViewById(R.id.history_coch);
	                holder.remark0 = (TextView) view.findViewById(R.id.history_remark0);
	                holder.remark1 = (TextView) view.findViewById(R.id.history_remark1);
	                holder.remark2 = (TextView) view.findViewById(R.id.history_remark2);
	                view.setTag(holder);
	            } else {
	                holder = (ViewHolder) view.getTag();
	            }
	            Log.d("NLiveRoid"," " + item.getDate());
	            Log.d("NLiveRoid"," " + item.getKind());
	            Log.d("NLiveRoid"," " + item.getLv());
	            Log.d("NLiveRoid"," " + item.getCoch());
	            Log.d("NLiveRoid"," " + item.getRemark0());
	            Log.d("NLiveRoid"," " + item.getRemark1());
	            Log.d("NLiveRoid"," " + item.getRemark2());
	            holder.date.setText(item.getDate());
	            holder.kind.setText(item.getKind());

	            if(item.getLv() != null && !item.getLv().equals("")){
	            	holder.lv.setText(item.getLv());
	            	holder.lv.setVisibility(View.VISIBLE);
	            }else{
	            	holder.lv.setVisibility(View.GONE);
	            }
	            if(item.getCoch() != null && !item.getCoch().equals("") ){
	            	holder.coch.setText(item.getCoch());
	            	holder.coch.setVisibility(View.VISIBLE);
	            }else{
	            	holder.coch.setVisibility(View.GONE);
	            }
	            if(item.getRemark0() != null && !item.getRemark0().equals("")){
	            	holder.remark0.setText(item.getRemark0());
	            	holder.remark0.setVisibility(View.VISIBLE);
	            }else{
	            	holder.remark0.setVisibility(View.GONE);
	            }
	            if(item.getRemark1() != null && !item.getRemark1().equals("")){
	            	holder.remark1.setText(item.getRemark1());
	            	holder.remark1.setVisibility(View.VISIBLE);
	            }else{
	            	holder.remark1.setVisibility(View.GONE);
	            }
	            if(item.getRemark2() != null && !item.getRemark2().equals("") ){
	            	holder.remark2.setText(item.getRemark2());
	            	holder.remark2.setVisibility(View.VISIBLE);
	            }else{
	            	holder.remark2.setVisibility(View.GONE);
	            }
	            return view;
	        }
	        private class ViewHolder {
;	            public TextView date;
	            public TextView kind;
	            public TextView lv;
	            public TextView coch;
	            public TextView remark0;
	            public TextView remark1;
	            public TextView remark2;
	        }
	    }

	    public class NicoRepoTask extends AsyncTask<Void,Void,Void>{
	    			private boolean ENDFLAG = true;
	    			private ArrayList<LiveInfo> list;
	    			private ArrayList<GETThumb> thumbnailTasks = new ArrayList<GETThumb>();
					private int index = 0;
					private AsyncTask<Void,Void,Void> thumbTaskBase;
	    			@Override
	    			public void onCancelled(){
	    				super.onCancelled();
	    				if(thumbnailTasks != null){
	    					for(int i = 0; i < thumbnailTasks.size(); i++){
    							if(thumbnailTasks.get(i).getStatus() == AsyncTask.Status.FINISHED){
    								thumbnailTasks.get(i).cancel(true);
    							}
    						}
    						thumbnailTasks.clear();
	    				}
	    				if(thumbTaskBase != null && thumbTaskBase.getStatus() != AsyncTask.Status.FINISHED){
	    					thumbTaskBase.cancel(true);
	    				}
	    			}
	    			@Override
	    			protected Void doInBackground(Void... params) {
	    				if(NLiveRoid.isDebugMode)Log.d("NLiveRoid","NicoRepoTask");
	    				 try {
	    					 if(error == null || error.getErrorCode() != 0){
	    							return null;
	    						}
	    					 String filter = "";
	    					 switch(repoIndex){
	    					 case 0:
	    						 filter = "?segment=all";
	    						 break;
	    					 case 1:
	    						 filter = "?segment=myself";
	    						 break;
	    					 case 2:
	    						 filter = "?segment=user";
	    						 break;
	    					 case 3:
	    						 filter = "?segment=chcom";
	    						 break;
	    					 }
	    					 progressBar.setProgress(30);
	    					InputStream is = Request.doGetToInputStreamFromFixedSession(Request.getSessionID(error), URLEnum.NICOREPO + filter , error);
	    					if(error == null || error.getErrorCode() != 0){
	    						return null;
	    					}
	    					 progressBar.setProgress(70);
	    					org.ccil.cowan.tagsoup.Parser parser = new org.ccil.cowan.tagsoup.Parser();
	    					NicoRepoParser handler = new NicoRepoParser(this,repoIndex);
	    				        parser.setContentHandler(handler);
	    				        parser.parse(new InputSource(is));
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
	   					 progressBar.setProgress(85);
	    				long startT = System.currentTimeMillis();
	    				while(ENDFLAG){
	    					try {
	    						Thread.sleep(100);
	    					} catch (InterruptedException e) {
	    						e.printStackTrace();
	    						ENDFLAG = false;
	    						break;
	    					}catch(IllegalArgumentException e1){
	    						e1.printStackTrace();
	    						Log.d("NLiveRoid","IllegalArgumentException at History whileNicoRepo");
	    						ENDFLAG = false;
	    						break;
	    					}
	    					if((System.currentTimeMillis() - startT) > 30000){
	    						error.setErrorCode(-10);
	    						return null;
	    					}
	    				}
	    				return null;
	    			}
	    			public void onPageFinished(ArrayList<LiveInfo> result){
	    				ENDFLAG = false;
	    				this.list = result;
	    			}
	    		@Override
	    		protected void onPostExecute(Void arg){
	    			if(error != null){
	    				if(error.getErrorCode() != 0){
	    					error.showErrorToast();
	    				}else if(list != null){
    				        progressBar.setProgress(90);
	    					if(list.size() == 0){
	    						MyToast.customToastShow(ACT, "ニコレポがありませんでした");
	    					}else{
    							for(int i = 0; i < list.size(); i++){
    								nicorepoAdapter.add(list.get(i));
    							}
	    						if(repoIndex == 1){
	    							if( myThumb == null){
		    							 new GetMyThumb().execute();
	    							}else{
	    								for(int i = 0; i < nicorepoAdapter.getCount(); i++){
	    									nicorepoAdapter.getItem(i).setThumbnail(myThumb);
	    								}
	    							}
	    						}else{
	    							for(int i = 0; i < list.size(); i++){
			    						nicorepoAdapter.add(list.get(i));
	    							}
	    							if(thumbnailTasks != null){
	    	    						for(int i = 0; i < thumbnailTasks.size(); i++){
	    	    							if(thumbnailTasks.get(i).getStatus() != AsyncTask.Status.FINISHED){
	    	    								thumbnailTasks.get(i).cancel(true);
	    	    							}
	    	    						}
	    	    						thumbnailTasks.clear();
	    	    					}
	    		    				if(thumbTaskBase != null && thumbTaskBase.getStatus() != AsyncTask.Status.FINISHED){
	    		    					thumbTaskBase.cancel(true);
	    		    				}
	    							thumbTaskBase = new AsyncTask<Void,Void,Void>(){
	    				    			@Override
	    				    			protected Void doInBackground(Void... params) {
	    				    				boolean flag = false;
	    				    				while(!flag){
	    				    				try{
				    							for(; index < list.size(); index++){
					    							thumbnailTasks.add(new GETThumb(index));
						    						thumbnailTasks.get(index).execute(list.get(index));
					    							}
			    				    				 flag = true;
				    							}catch(RejectedExecutionException e){
				    								e.printStackTrace();
				    								try {
														Thread.sleep(5000);
														Log.d("NLiveRoid","GETThumb RejectedExecutionException");
														break;
													} catch (InterruptedException e1) {
														e1.printStackTrace();
														flag = true;
														break;
													}catch(Exception e1){
														e1.printStackTrace();
														flag = true;
														break;
													}
				    							}
	    				    				}
										return null;
	    				    			}
	    							};
	    		    				try{
	    							thumbTaskBase.execute();
	    		    				}catch(RejectedExecutionException e){
	    		    					Log.d("NLiveRoid","NicoRepoRejectedExecutionException thumbTaskBase");
	    								e.printStackTrace();
	    		    				}catch(Exception e){
	    		    					Log.d("NLiveRoid","NicoRepoException thumbTaskBase");
	    		    					e.printStackTrace();
	    		    				}
	    						}
	    					}
	    				}
	    			}
	    			removeProgress();
	    		}
	    	}

	    public class GetMyThumb extends AsyncTask<Void,Void,Integer>{
	    	private boolean ENDFLAG = true;
			private String result;
			@Override
			protected Integer doInBackground(Void... params) {
				//ユーザーサムネURLの法則がわからないので、一度ユーザーページに行ってURL取ってからサムネを取る
				try{
					if(error == null || error.getErrorCode() != 0){
						return -1;
					}
					String myid = Request.getSessionID(error).split("_")[3];
					String url = URLEnum.USERPAGE + myid;
				InputStream is = Request.doGetToInputStreamFromFixedSession(Request.getSessionID(error), url , error);
				if(error == null || error.getErrorCode() != 0){
					return -1;
				}
				org.ccil.cowan.tagsoup.Parser parser = new org.ccil.cowan.tagsoup.Parser();
				MyTumbURLParser handler = new MyTumbURLParser(this);
			        parser.setContentHandler(handler);
			        parser.parse(new InputSource(is));
			  } catch (org.xml.sax.SAXNotRecognizedException e) {
			      // Should not happen.
				  e.printStackTrace();
					return -2;
			  } catch (org.xml.sax.SAXNotSupportedException e) {
			      // Should not happen.
				  e.printStackTrace();
					return -3;
			  } catch (IOException e) {
				  e.printStackTrace();
				  return -5;
			} catch (SAXException e) {
				e.printStackTrace();
				return -4;
			}
			long startT = System.currentTimeMillis();
			while(ENDFLAG){
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
					ENDFLAG = false;
					break;
				}catch(IllegalArgumentException e1){
					e1.printStackTrace();
					Log.d("NLiveRoid","IllegalArgumentException at Request getImageForList");
					ENDFLAG = false;
					break;
				}
				if((System.currentTimeMillis() - startT) > 30000){
					error.setErrorCode(-10);
					return -6;
				}
			}
			//サムネを取得
			if(NLiveRoid.isDebugMode)Log.d("NLiveRoid", "  MY THUMB - " + result);
			if(result != null){
				myThumb = Request.getImage(result, error);
			}else{
				myThumb = BitmapFactory.decodeResource(getResources(), R.drawable.use_noimage);
			}
			for(int i = 0; i < nicorepoAdapter.getCount(); i++){
				nicorepoAdapter.getItem(i).setThumbnail(myThumb);
			}
			return 0;
		}
		public void finishCallBack(String value) {
			ENDFLAG = false;
			result = value;
		}
		public void onPageFinished(String url){
				result = url;
				ENDFLAG = false;
			}
	    	@Override
	    	protected void onPostExecute(Integer arg){
	    		if(error != null){
	    			if(error.getErrorCode() != 0){
	    				error.showErrorToast();
	    			}else if(arg < 0 ) {
	    				Log.d("NLiveRoid","FAILED GetMyThumb" + arg);
	    				MyToast.customToastShow(ACT, "自分のサムネイル取得に失敗しました");
	    			}else{
	    				if(nicorepoAdapter != null)nicorepoAdapter.notifyDataSetChanged();
	    			}
	    		}
	    	}
	    }

		@Override
		public void onResume(){
			final NLiveRoid app = (NLiveRoid)getApplicationContext();
				app.setForeACT(this);
		    	//一旦全て終了
		    	SearchTab.cancelMoveingTask();
		    	LiveTab.cancelMovingTask();
		    	CommunityTab.cancelMovingTask();
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
							onReload();
							}
						}).setCancelable(false).create();
				accountDialog.show();
					}
			}else{
			if(nicorepoAdapter.isEmpty()){
				if(nicorepoTask==null||nicorepoTask.getStatus() == AsyncTask.Status.FINISHED){//自分自身が実行されていない
							onReload();
				}
			}
			if(Boolean.parseBoolean(app.getDetailsMapValue("update_tab"))){
				onReload();
			}
			}
		}catch(NullPointerException e){
			e.printStackTrace();
		}catch(Exception e){
			e.printStackTrace();
		}
				super.onResume();
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
					NLiveRoid app = (NLiveRoid)getApplicationContext();
					boolean[] setting_boolean = new boolean[28];
					try{
						 Log.d("NLiveRoid","HistoryTab startF " + liveObj.getLiveID() + " " + liveObj.getCommunity_info() + " " + liveObj.getCommunityName() + " " + liveObj.getCommunityID());
						 TopTabs.insertHis(0, liveObj.getLiveID(), liveObj.getCommunityID(), liveObj.getTitle(), liveObj.getCommunityName(), liveObj.getDescription());

						//fexit,(finish_back),at,at_overwriteはDefaultMapValue
					setting_boolean[0] = app.getDetailsMapValue("fexit")==null? true:Boolean.parseBoolean(app.getDetailsMapValue("fexit"));
					setting_boolean[1] = app.getDetailsMapValue("newline")== null? false:Boolean.parseBoolean(app.getDetailsMapValue("newline"));
					setting_boolean[2] = app.getDetailsMapValue("form_up") == null ? false: Boolean.parseBoolean(app.getDetailsMapValue("form_up"));
					setting_boolean[3] = app.getDetailsMapValue("voice_input") == null ? false:Boolean.parseBoolean(app.getDetailsMapValue("voice_input"));
//					setting_boolean[4] = app.getDetailsMapValue("sp_player") == null ? true:Boolean.parseBoolean(app.getDetailsMapValue("sp_player"));
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

		private void applyFilter(){
			dbAdapter.clear();
			for(int i = 0; i < dbBeans.size(); i++){
				if(dbBeans.get(i).getKind().equals("視聴")&&filters[0]){
						dbAdapter.insert(dbBeans.get(i),0);
				}
				if(dbBeans.get(i).getKind().equals("詳細")&&filters[1]){
						dbAdapter.insert(dbBeans.get(i),0);
				}
				if(dbBeans.get(i).getKind().equals("検索")&&filters[2]){
						dbAdapter.insert(dbBeans.get(i),0);
				}
			}
			dbAdapter.notifyDataSetInvalidated();
		}
		public void onReload(){
			if(isDBView && (TopTabs.his_value & 0x40) > 0){//データベースを現在のhis_valueで更新して表示する
		        Log.d("NLiveRoid","HIS onReload " + TopTabs.his_db);
				if(TopTabs.his_db != null && TopTabs.his_db.getDB() != null){
					if(!TopTabs.his_db.getDB().isOpen()){//onDestroyとの間でアプリを起動/終了すると，既にDBが閉じていて例外を吐いてsetCurrentTabが失敗することがある
						TopTabs.his_db.getWritableDatabase();
					}
					if(dbAdapter != null)dbAdapter.clear();
					try{//削除後ここが呼ばれるとエラーする
				Cursor c = TopTabs.his_db.getDB().query("his", new String[] { "ID", "DATE", "KIND","LV","COCH","REMARK0","REMARK1","REMARK2" },
							                null, null, null, null, null);
					        boolean isEof = c.moveToFirst();
					        dbBeans.clear();
					        Log.d("NLiveRoid","HIS ISEOF " + isEof);
					        while (isEof) {
					        	DBBean bean = new DBBean(c.getString(0),c.getLong(1),c.getInt(2),c.getString(3),c.getString(4),c.getString(5),c.getString(6),c.getString(7));
					            isEof = c.moveToNext();
					            Log.d("NLiveRoid","DBBean " +bean.getId() + " "+ bean.getDate() +" " + bean.getKind() + " " + bean.getLv() +" " + bean.getCoch() + " " + bean.getRemark0() + " " + bean.getRemark1() + " " + bean.getRemark2());
					            dbBeans.add(bean);
						        Log.d("NLiveRoid","HIS ISEOF " + isEof);
					        }
					        c.close();
					}catch(SQLiteException e){
						e.printStackTrace();
					}
				}
				applyFilter();
			}else{
			addProgress();
			if(nicorepoTask != null && nicorepoTask.getStatus() != AsyncTask.Status.FINISHED){
				nicorepoTask.cancel(true);
			}
			nicorepoAdapter.clear();
			nicorepoTask = new NicoRepoTask();
			nicorepoTask.execute();
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
			 * サムネイル取得
			 * @author Owner
			 *
			 */
			class GETThumb extends AsyncTask<LiveInfo,Void,Integer>{
				private int thumbTagID;
				private GETThumb(int id){
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
					}else if(arg0[0].getCommunityID().contains("co")){
						bm = Request.getImageForList(String.format(URLEnum.BITMAPSCOMMUNITY,
								arg0[0].getCommunityID()),error,0);
					}else{
						arg0[0].setThumbnail(BitmapFactory.decodeResource(getResources(), R.drawable.noimage));
						Log.d("NLiveRoid","H set noimage" + arg0[0]);
					}
					if(bm != null){
						arg0[0].setThumbnail(bm);
					}
					return thumbTagID;
				}
				@Override
				protected void onPostExecute(Integer arg){
					if(arg < 0)return;
					ImageView iv = (ImageView)listview.findViewWithTag(thumbTagID);
					if(iv != null){
					listview.invalidateViews();
					}
				}
			@Override
			public void onCancelled(){//onPostExecuteに行かずにキャンセルされた場合があるので必要
				removeProgress();
				super.onCancelled();
			}
		}

		public static HistoryTab getHistoryTab(){
			return ACT;
		}


		/**
		 * コンテキストメニュー生成時処理
		 */
		@Override
		public void onCreateContextMenu(ContextMenu menu, View view,
				ContextMenuInfo info) {
			super.onCreateContextMenu(menu, view, info);
			isContextOperation = true;
			NLiveRoid app = (NLiveRoid)getApplicationContext();//シンプルじゃない、嫌い
			final AdapterContextMenuInfo adapterInfo = (AdapterContextMenuInfo) info;

				if(nicorepoAdapter.getCount() > adapterInfo.position ){
					LiveInfo li = nicorepoAdapter.getItem(adapterInfo.position);
					if(li.getLiveID()== null)return;
				//ここのセッションは、一番元のログインの物であるはずなので、そのままRequest.getSessionIDでおｋなはず
				GateView gView = app.getGateView();
				if(gView == null)return;
				gate = new Gate(this,gView,li,false,Request.getSessionID(error));
				ViewGroup gateParent = (ViewGroup) app.getGateView().getView().getParent();
				if(gateParent != null){
					gateParent.removeView(app.getGateView().getView());
				}
				((ViewGroup)parent.getParent()).addView(app.getGateView().getView());
				gate.show(this.getResources().getConfiguration());
				}


		}

		@Override
		protected void onSaveInstanceState(Bundle outState) {
		    outState.putString("WORKAROUND_FOR_BUG_19917_KEY", "WORKAROUND_FOR_BUG_19917_VALUE");
//		    super.onSaveInstanceState(outState);
		}




		@Override
		public boolean dispatchKeyEvent(KeyEvent keyevent){
			if(keyevent.getKeyCode() == KeyEvent.KEYCODE_BACK && keyevent.getAction() == KeyEvent.ACTION_DOWN)
			if(gate != null && gate.isOpened()){
				gate.close_noanimation();//外側からアニメーション起動するとなぜか重い
				return true;
			}
				return super.dispatchKeyEvent(keyevent);
		}


		@Override
		protected void onActivityResult(int requestCode, int resultCode,Intent data){
			isListTaped = false;
			//クルーズの時のフラッシュ等からブラウザに行って帰ってきた時はerrorもnull
//			if(error == null||resultCode == CODE.RESULT_ALLFINISH){
//				return;
//			}
			if(isDBView){
			onReload();
			}

		}


		/**
		 * adapterを取得します。
		 * @return adapter
		 */
		public ArrayAdapter<LiveInfo> getAdapter() {
		    return nicorepoAdapter;
		}


		@Override
		public void allCommFunction(int index, LiveInfo info) {
			switch(index){
				case 0:
				String liveidResult = info.getCommunityID() == null||info.getCommunityID().equals(URLEnum.HYPHEN)? info.getLiveID():info.getCommunityID();
				Intent commuTab = new Intent(this,TopTabs.class);
				commuTab.putExtra("scheme", "ts"+liveidResult);
				commuTab.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(commuTab);
				break;
			}
		}


		public boolean isDBView() {
			return isDBView;
		}


		/**
		 * リストのクリック+TSのダイアログから参照
		 */
		@Override
		public void showGate(LiveInfo liveObj) {
			//ここのセッションは、一番元のログインの物であるはずなので、そのままRequest.getSessionIDでおｋなはず
			NLiveRoid app = (NLiveRoid) getApplicationContext();
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

