package nliveroid.nlr.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.regex.Pattern;

import nliveroid.nlr.main.parser.XMLparser;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TextView;

public class EducationList extends Activity
	 implements OnClickListener {

		private EducationList ACT;
		private View parent;
		  private static final int SELECTED_BG_COLOR = Color.argb(128,255,255,255);
		  private static final int HOVER_BG_COLOR = Color.argb(128, 153,255,255);

		  private EducationArrayAdapter adapter = null;

		  private DragnDropListView listview;
		  private CheckBox chkSort;

		  @Override
		  public void onCreate(Bundle savedInstanceState) {
		    super.onCreate(savedInstanceState);
		    this.getWindow().requestFeature(Window.FEATURE_NO_TITLE);//requestFeature() must be called before adding content

		    parent = LayoutInflater.from(this).inflate(R.layout.education_list, null);
		    setContentView(parent);
		    ACT = this;


		    chkSort = (CheckBox) findViewById(R.id.chkSort);
		    chkSort.setOnClickListener(this);
		    //追加ボタン
		    Button addBt = (Button)findViewById(R.id.education_add);
		    addBt.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					//ダイアログ表示
					final Dialog addDialog = new Dialog(ACT);

					View dialogView = LayoutInflater.from(ACT).inflate(R.layout.education_add_dialog, null);
					//KEY
					final EditText keyEt = (EditText)dialogView.findViewById(R.id.education_addet_key);
					final EditText valueEt = (EditText)dialogView.findViewById(R.id.education_addet_value);
					//ＹＥＳ
					Button yes = (Button)dialogView.findViewById(R.id.education_add_yes);
					yes.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							if(keyEt.getText().toString().equals("")){
								MyToast.customToastShow(ACT, "対象文字列を入力して下さい");
								return;
							}else if(keyEt.getText().toString().equals(valueEt.getText().toString())){
								MyToast.customToastShow(ACT, "教育対象と違う文字列を入力して下さい");
								return;
							}
							if(adapter == null)return;
							//全く同じのをカット
							String key = keyEt.getText().toString();
							String value = valueEt.getText().toString();
							String[] temp = null;
							for(int i = 0; i < adapter.getCount(); i++){
								temp = adapter.getItem(i);
								if(temp[0].equals(key)){
									MyToast.customToastShow(ACT, "同じ教育が既にあります");
									return;
								}
							}
							//Patternとして登録できるかチェック
							try{
							Pattern.compile(key);
							}catch(Exception e){
								e.printStackTrace();
								MyToast.customToastShow(ACT, "正規表現のパターンとして登録できません");
								return;
							}
							//変換文字列をマップに格納
							//ファイルが無ければ生成
							String filePath = getStorageFilePath();
							if(filePath == null){
								MyToast.customToastShow(ACT, "ストレージが利用できませんでした");
								addDialog.dismiss();
								return;
							}
							File file = new File(filePath,"Education.xml");
							if(!file.exists()){
								try {
									file.createNewFile();
									String xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"+
											"<Education xmlns=\"http://nliveroid-tutorial.appspot.com/education/\">\n"+
											"<data>\n" +
											"<key>(http|https):([^\\x00-\\x20()\"&lt;&gt;\\x7F-\\xFF])*</key>\n" +
											"<value>URL省略</value>\n"+
											"</data>\n"+
										    "</Education>\n";
											FileOutputStream fos = new FileOutputStream(file.getPath());
											fos.write(xml.getBytes());
											fos.close();
											if(adapter != null){
												adapter.add(new String[]{"(http|https):([^\\x00-\\x20()\"&lt;&gt;\\x7F-\\xFF])*", "URL省略"});
											}

								} catch (IOException e) {
									e.printStackTrace();
									MyToast.customToastShow(ACT, "ファイル生成に失敗しました");
									addDialog.dismiss();
									return;
								}
							}
							//新たな入力を書き込む
							if(adapter != null){
								//表示する為のみなので<>エスケープしなくていい
								adapter.add(new String[]{key,value});
								//ファイルに保存する(エスケープしてる)
								int returnValue = WriteEducation();
								switch(returnValue){
								  case 0:
									  break;
								  case -1:
									  //画面回転でなっちゃうことがあるので無視
//									  MyToast.customToastShow(getApplication(), "保存する教育がありません");
									  break;
								  case -2:
									  MyToast.customToastShow(getApplication(), "保存に失敗\nストレージが利用できませんでした");
									  break;
								  case -3:
									  MyToast.customToastShow(getApplication(), "不明なエラーです");
									  break;
								  }
								addDialog.dismiss();
							}
						}
					});
					//ＣＡＮＣＥＬ
					Button cancel = (Button)dialogView.findViewById(R.id.education_add_cancel);
					cancel.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							addDialog.dismiss();
						}
					});
					//エディットテキストが伸びて行っちゃう対策
					keyEt.setWidth(0);
					valueEt.setWidth(0);
					addDialog.setContentView(dialogView,new TableLayout.LayoutParams(-1,-1));
					addDialog.setTitle("教育");
//					addDialog.setOnShowListener(new OnShowListener(){
//
//						@Override
//						public void onShow(DialogInterface dialog) {
//							Log.d("Log","DIALOG SHOW ------ " + addDialog.getWindow().getDecorView().getWidth());
//						}
//
//					});



					addDialog.show();
				}
		    });

		    //全て削除ボタン
		    Button allDelete = (Button)findViewById(R.id.education_all_delete);
		    allDelete.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					//アラート(本当に)
					new AlertDialog.Builder(ACT)
					.setMessage("教育ファイルを削除します\nよろしいですか?")
					.setPositiveButton("YES", new DialogInterface.OnClickListener(){
						@Override
						public void onClick(DialogInterface dialog, int which) {
							if(adapter != null && adapter.getCount() > 0){
								adapter.clear();
							}
							String filePath = getStorageFilePath();
							if(filePath== null){
								MyToast.customToastShow(ACT, "ストレージが利用できませんでした");
								return;
							}
							File file = new File(filePath,"Education.xml");
							file.delete();
						}
					})
					.setNegativeButton("CANCEL",new DialogInterface.OnClickListener(){
						@Override
						public void onClick(DialogInterface dialog, int which) {
						}
					})
					.create().show();
				}
			});

		    //ファイルから教育レコード読み込み
		    new EducationRead().execute();
		  }//End of onCreate


		  @Override
		  public void onClick(View v) {
		    boolean sortable = chkSort.isChecked();
		    listview.setSortMode(sortable);
		  }

		  class EducationArrayAdapter extends ArrayAdapter<String[]> implements SortableAdapter {

		    private LayoutInflater inflater;
		    private int selectedPosition = -1;
		    private int hoverPosition = -1;

		    public EducationArrayAdapter(Context context) {
		      super(context, R.layout.education_list_row);
		      inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		    }

		    public View getView(int position, View convertView, ViewGroup parent) {
		      View view = null;
		      ViewHolder holder = null;
		      if (convertView == null) {
		        view = inflater.inflate(R.layout.education_list_row, null);
		        holder = new ViewHolder();
		        holder.txtKey = (TextView) view.findViewById(R.id.txtString_key);
		        holder.txtValue = (TextView) view.findViewById(R.id.txtString_value);
		        view.setTag(holder);
		      } else {
		        view = convertView;
		        holder = (ViewHolder) view.getTag();
		      }

		      if (selectedPosition == hoverPosition) {
		        if (position == selectedPosition) {
		          view.setBackgroundColor(HOVER_BG_COLOR);
		        } else {
		          view.setBackgroundResource(android.R.drawable.list_selector_background);
		        }
		      } else {
		        if (position == selectedPosition) {
		          view.setBackgroundColor(SELECTED_BG_COLOR);
		        } else if (position == hoverPosition) {
		          view.setBackgroundColor(HOVER_BG_COLOR);
		        } else {
		          view.setBackgroundResource(android.R.drawable.list_selector_background);
		        }
		      }
		      String[] cellValue = getItem(position);

		      holder.txtKey.setText(cellValue[0]);
		      holder.txtValue.setText(cellValue[1]);

		      return view;
		    }

		    @Override
		    public void setSelectedPosition(int position) {
		      if (selectedPosition != position) {
		        selectedPosition = position;
		        notifyDataSetChanged();
		      }
		    }

		    @Override
		    public void setHoverPosition(int position) {
		      if (hoverPosition != position) {
		        hoverPosition = position;
		        notifyDataSetChanged();
		      }
		    }

		  }//End of EducationArrayAdapter

		  class ViewHolder {
			TextView txtKey;
		    TextView txtValue;
		  }





		  /**
			 * コンテキストメニュー生成時処理
			 */
			@Override
			public void onCreateContextMenu(ContextMenu menu, View view,
					ContextMenuInfo info) {
				super.onCreateContextMenu(menu, view, info);
				final AdapterContextMenuInfo adapterInfo = (AdapterContextMenuInfo) info;
				if(adapter == null || adapter.getCount() < adapterInfo.position){//立見などをタップした時におかしくなるArrayList.throwIndexOutOfBoundsException
					return;
				}
				final String[] row = adapter.getItem(adapterInfo.position);

				menu.add("編集");
				menu.getItem(0).setOnMenuItemClickListener(
						new OnMenuItemClickListener() {
							@Override
							// 引数はメニューのテキスト
							public boolean onMenuItemClick(MenuItem arg0) {
								//ダイアログ表示
								final Dialog addDialog = new Dialog(ACT);
								View dialogView = LayoutInflater.from(ACT).inflate(R.layout.education_add_dialog, null);
								//KEY
								final EditText keyEt = (EditText)dialogView.findViewById(R.id.education_addet_key);
								final EditText valueEt = (EditText)dialogView.findViewById(R.id.education_addet_value);
								keyEt.setText(row[0] == null? "":row[0]);
								valueEt.setText(row[1]==null? "":row[1]);
								//ＹＥＳ
								Button yes = (Button)dialogView.findViewById(R.id.education_add_yes);
								yes.setOnClickListener(new View.OnClickListener() {
									@Override
									public void onClick(View v) {
										if(keyEt.getText().toString().equals("")){
											MyToast.customToastShow(ACT, "対象文字列を入力して下さい");
											return;
										}else if(keyEt.getText().toString().equals(valueEt.getText().toString())){
											MyToast.customToastShow(ACT, "教育対象と違う文字列を入力して下さい");
											return;
										}
										if(adapter == null)return;
										//Patternとして登録できるかチェック
										try{
										Pattern.compile(keyEt.getText().toString());
										}catch(Exception e){
											e.printStackTrace();
											MyToast.customToastShow(ACT, "正規表現のパターンとして登録できません");
											return;
										}
										//変換文字列をマップに格納
										//ファイルが無ければ生成
										String filePath = getStorageFilePath();
										if(filePath == null){
											MyToast.customToastShow(ACT, "ストレージが利用できませんでした");
											addDialog.dismiss();
											return;
										}
										File file = new File(filePath,"Education.xml");
										if(!file.exists()){
											try {
												file.createNewFile();
												String xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"+
														"<Education xmlns=\"http://nliveroid-tutorial.appspot.com/education/\">\n"+
														"<data>\n" +
														"<key>(http|https):([^\\x00-\\x20()\"&lt;&gt;\\x7F-\\xFF])*</key>\n" +
														"<value>URL省略</value>\n"+
														"</data>\n"+
													    "</Education>\n";
														FileOutputStream fos = new FileOutputStream(file.getPath());
														fos.write(xml.getBytes());
														fos.close();
														if(adapter != null){
															adapter.add(new String[]{"(http|https):([^\\x00-\\x20()\"&lt;&gt;\\x7F-\\xFF])*", "URL省略"});
														}

											} catch (IOException e) {
												e.printStackTrace();
												MyToast.customToastShow(ACT, "ファイル生成に失敗しました");
												addDialog.dismiss();
												return;
											}
										}
										//編集を対象に保存
										if(adapter != null){
											//表示する為のみなのでエスケープしなくていい
											adapter.remove(row);
											adapter.insert(new String[]{keyEt.getText().toString(),valueEt.getText().toString()},adapterInfo.position);
											//ファイルに書き込む(エスケープしてる)
											int returnValue = WriteEducation();
											switch(returnValue){
											  case 0:
												  break;
											  case -1:
												  //画面回転でなっちゃうことがあるので無視
//												  MyToast.customToastShow(getApplication(), "保存する教育がありません");
												  break;
											  case -2:
												  MyToast.customToastShow(getApplication(), "保存に失敗\nストレージが利用できませんでした");
												  break;
											  case -3:
												  MyToast.customToastShow(getApplication(), "不明なエラーです");
												  break;
											  }
											addDialog.dismiss();
										}
									}
								});
								//ＣＡＮＣＥＬ
								Button cancel = (Button)dialogView.findViewById(R.id.education_add_cancel);
								cancel.setOnClickListener(new View.OnClickListener() {
									@Override
									public void onClick(View v) {
										addDialog.dismiss();
									}
								});
								addDialog.setContentView(dialogView,new TableLayout.LayoutParams(-1,-1));
								addDialog.setTitle("教育");
								addDialog.show();
								return false;
							}
							});
				menu.add("忘却");
				menu.getItem(1).setOnMenuItemClickListener(
						new OnMenuItemClickListener() {
							@Override
							public boolean onMenuItemClick(MenuItem menuitem) {
								//onStopで書き込むのでファイルからまだ消さなくていい
								new AlertDialog.Builder(ACT)
								.setMessage("忘却しますか?")
								.setPositiveButton("YES", new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										adapter.remove(row);
								        //ファイルに書き込んじゃう
								        WriteEducation();
									}
								})
								.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
									}
								})
								.create().show();

								return false;
							}
						});

			}//End of onCreateContextMenu


			  class EducationRead extends AsyncTask<Void,Void,ArrayList<String>>{

				private LinkedHashMap<String,String> educationList;
				@Override
				protected ArrayList<String> doInBackground(Void... arg0) {
					educationList = new LinkedHashMap<String, String>();
					//ファイルを読み込む
					ArrayList<String> result = new ArrayList<String>();
					String filePath = getStorageFilePath();
					if(filePath == null){
						result.add("-1");
						return result;
					}
					File file = new File(filePath,"Education.xml");
					if(!file.exists()){
						try {
							file.createNewFile();//ファイル無ければテンプレ作成
							Log.d("Log","EDUCATION  CREATGED ---- ");
							String xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"+
							"<Education xmlns=\"http://nliveroid-tutorial.appspot.com/education/\">\n"+
							"<data>\n" +
							"<key>(http|https):([^\\x00-\\x20()\"&lt;&gt;\\x7F-\\xFF])*</key>\n" +
							"<value>URL省略</value>\n"+
							"</data>\n"+
							"<data>\n" +
							"<key>^/.+</key>\n" +
							"<value></value>\n"+
							"</data>\n"+
							"</Education>\n";
							FileOutputStream fos = new FileOutputStream(file.getPath());
							fos.write(xml.getBytes());
							fos.close();
							educationList.put("(http|https):([^\\x00-\\x20()\"&lt;&gt;\\x7F-\\xFF])*", "URL省略");
							result.add("1");
							return result;
							//リストを生成(if文を抜ける)
						} catch (FileNotFoundException e) {
							e.printStackTrace();
							result.add("-2");
							return result;
						} catch (IOException e) {
							e.printStackTrace();
							result.add("-3");
							return result;
						}
					}else{//ファイルあった

						ArrayList<String> resultList = new ArrayList<String>();
						byte[] data = new byte[(int)((file).length())];
						FileInputStream fis = null;
						try {
						fis = new FileInputStream(file);
						fis.read(data);
						fis.close();
					int parseError = XMLparser.parseLiveEducation(data,educationList);

					if(parseError == -1){
						resultList.add("-4");
						return resultList;
					}else if(parseError == -2){
						resultList.add("-5");
						return resultList;
					}
						} catch (FileNotFoundException e) {
							e.printStackTrace();
							resultList.add("-2");
							return resultList;
						} catch (IOException e) {
							resultList.add("-3");
							return resultList;
						}
						//教育ファイル読み込み成功
						//リストを生成(if文を抜ける)
					}
					//リスト生成

					return null;
				}
				@Override
				protected void onPostExecute(ArrayList<String> arg){
					if(parent != null){
					ProgressBar p = (ProgressBar)parent.findViewById(R.id.education_init_progressbar);
					p.setVisibility(View.GONE);
					}
					if(arg == null){
						//読み込み成功
					    adapter = new EducationArrayAdapter(ACT);
					    Iterator<String> it = educationList.keySet().iterator();
					    String tempKey = "";

					    while(it.hasNext()){
					    	tempKey = it.next();
					    	adapter.add(new String[]{tempKey,educationList.get(tempKey)});
					    }

					    listview = (DragnDropListView) findViewById(R.id.list);
					    //XMLじゃできなかった
					    listview.setFastScrollEnabled(true);
					    listview.setAdapter(adapter);
					    ACT.registerForContextMenu(listview);
					}else if(arg.size() > 0){
						if(arg.get(0).equals("1")){
							//一番初期に、生成してアダプタ追加
							MyToast.customToastShow(ACT, "教育ファイル生成しました");
						    adapter = new EducationArrayAdapter(ACT);
						    Iterator<String> it = educationList.keySet().iterator();
						    String tempKey = "";
						    while(it.hasNext()){
						    	tempKey = it.next();
						    	adapter.add(new String[]{tempKey,educationList.get(tempKey)});
						    }

						    listview = (DragnDropListView) findViewById(R.id.list);
						    listview.setAdapter(adapter);
						    ACT.registerForContextMenu(listview);
						}else if(arg.get(0).equals("-1")){
							MyToast.customToastShow(ACT, "ストレージが利用できませんでした");
						}else if(arg.get(0).equals("-2")||arg.get(0).equals("-3")){
							MyToast.customToastShow(ACT, "ファイルIOに失敗しました");
						}else if(arg.get(0).equals("-4")||arg.get(0).equals("-5")){
							MyToast.customToastShow(ACT, "教育ファイルの記述がおかしい");
						}
					}
				}
			  }

			  private String getStorageFilePath(){
				  boolean isStorageAvalable = false;
					boolean isStorageWriteable = false;
					String state = Environment.getExternalStorageState();
					if(state == null){
						return null;
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
					}

					boolean notAvalable = !isStorageAvalable;
					boolean notWritable = !isStorageWriteable;
					if(notAvalable||notWritable){
						return null;
					}


					//sdcard直下に、パッケージ名のフォルダを作りファイルを生成
					String filePath = Environment.getExternalStorageDirectory().toString() + "/NLiveRoid";

					File directory = new File(filePath);
					if(directory.mkdirs()){//すでにあった場合も失敗する
						Log.d("log","mkdir");
					}

					return filePath;
			  }


			  /**
			   * 今ある教育を保存する!!
			   *
			   */

			 public int WriteEducation(){
					  if(adapter == null)return -1;
					  String filePath = getStorageFilePath();
						if(filePath == null){
							return -2;
						}
						File file = new File(filePath,"Education.xml");
						if(!file.exists()){
							//ファイル無ければなにかがおかしいので終了
							return -3;
						}

								String xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"+
								"<Education xmlns=\"http://nliveroid-tutorial.appspot.com/education/\">\n";
								String[] tempStr = null;
								for(int i = 0; i< adapter.getCount(); i++){
									tempStr = adapter.getItem(i);
									Log.d("LIST -- " ,  " " +tempStr[0]);
									xml += "<data>\n" +
											"<key>"+tempStr[0].replaceAll("<", "&lt;")+"</key>\n"+
											"<value>"+tempStr[1].replaceAll(">", "&gt;")+"</value>\n" +
											"</data>";
								}
								xml += "</Education>\n";

								FileOutputStream fos = null;
								try {
									fos = new FileOutputStream(file.getPath());
									fos.write(xml.getBytes());
									fos.close();
								} catch (FileNotFoundException e) {
									e.printStackTrace();
								} catch (IOException e) {
									e.printStackTrace();
								}

					return 0;
				  }


}//End of EducationList Activity




	class DragnDropListView extends ListView {
		  private static final boolean DEBUG = false;

		  private static final int SCROLL_SPEED_FAST = 25;
		  private static final int SCROLL_SPEED_SLOW = 8;

		  private static final int MOVING_ITEM_BG_COLOR = Color.argb(128, 0, 0, 0);
		  private static final int HOVER_REMOVE_ITEM_BG_COLOR = Color.argb(200, 255, 0, 0);

		  private EducationList calledClass;
		  private static final String TAG = DragnDropListView.class.getSimpleName();

		  private boolean sortMode = false;
		  private DragListener mDrag = new DragListenerImpl();
		  private DropListener mDrop = new DropListenerImpl();
		  private RemoveListener mRemove = new RemoveListenerImpl();

		  public DragnDropListView(Context context, AttributeSet attrs) {
		    this(context, attrs, 0);
		  }

		  public DragnDropListView(Context context, AttributeSet attrs, int defStyle) {
		    super(context, attrs, defStyle);
		    calledClass = (EducationList)context;
		  }

		  private SortableAdapter adapter;
		  private Bitmap mDragBitmap = null;
		  private ImageView mDragView = null;
		  private WindowManager.LayoutParams mWindowParams = null;
		  private int mFrom = -1;

		  private View mRemoveTile = null;
		  private Rect mRemoveHit = null;

		  @Override
		  public boolean onTouchEvent(MotionEvent event) {
		    if (!sortMode) {
		      return super.onTouchEvent(event);
		    }

		    int index = -1;
		    final int x = (int) event.getX();
		    final int y = (int) event.getY();

		    int action = event.getAction();

		    if (action == MotionEvent.ACTION_DOWN) {
		      index = pointToIndex(event);

		      if (index < 0) {
		        return false;
		      }

		      mFrom = index;
		      startDrag();

		      adapter.setSelectedPosition(index);

		      return true;
		    } else if (action == MotionEvent.ACTION_MOVE) {
		      final int height = getHeight();
		      final int fastBound = height / 9;
		      final int slowBound = height / 4;
		      final int center = height / 2;

		      int speed = 0;
		      if (event.getEventTime() - event.getDownTime() < 500) {
		        // 500ミリ秒間はスクロールなし
		      } else if (y < slowBound) {
		        speed = y < fastBound ? -SCROLL_SPEED_FAST : -SCROLL_SPEED_SLOW;
		      } else if (y > height - slowBound) {
		        speed = y > height - fastBound ? SCROLL_SPEED_FAST : SCROLL_SPEED_SLOW;
		      }

		      if (DEBUG) {
		        Log.d(TAG, "ACTION_MOVE y=" + y + ", height=" + height + ", fastBound=" + fastBound + ", slowBound=" + slowBound + ", center=" + center + ", speed=" + speed);
		      }

		      View v = null;
		      if (speed != 0) {
		        // 横方向はとりあえず考えない
		        int centerPosition = pointToPosition(0, center);
		        if (centerPosition == AdapterView.INVALID_POSITION) {
		          centerPosition = pointToPosition(0, center + getDividerHeight() + 64);
		        }
		        v = getChildByIndex(centerPosition);
		        if (v != null) {
		          int pos = v.getTop();
		          setSelectionFromTop(centerPosition, pos - speed);
		        }
		      }

		      if (mDragView != null) {
		        if (mDragView.getHeight() < 0) {
		          mDragView.setVisibility(View.INVISIBLE);
		        } else {
		          mDragView.setVisibility(View.VISIBLE);
		          if (this.isHitRemoveTile(x, y)) {
		            mDragView.setBackgroundColor(HOVER_REMOVE_ITEM_BG_COLOR);
		          } else {
		            mDragView.setBackgroundColor(MOVING_ITEM_BG_COLOR);
		          }
		        }

		        mWindowParams.x = getLeft();
		        mWindowParams.y = getTop() + y;

		        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
		        wm.updateViewLayout(mDragView, mWindowParams);

		        if (mDrag != null) {
		          index = pointToIndex(event);
		          mDrag.drag(mFrom, index);
		        }

		        adapter.setHoverPosition(index);

		        return true;
		      }

		    } else if (action == MotionEvent.ACTION_UP) {
		      if (isHitRemoveTile(x, y)) {
		        // 削除イメージにヒットしていた場合、削除する
		        if (mRemove != null)
		          mRemove.remove(mFrom);
		      } else {
		        // 削除イメージにヒットしていなければ、要素の入れ替えをする
		        if (mDrop != null) {
		          index = pointToIndex(event);
		          mDrop.drop(mFrom, index);
		        }
		      }

		      // ドラッグ中の項目の表示を削除する
		      endDrag();

		      adapter.setHoverPosition(-1);
		      adapter.setSelectedPosition(-1);

		      return true;
		    } else if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_OUTSIDE) {
		      // ドラッグ中の項目の表示を削除する
		      endDrag();
		      adapter.setHoverPosition(-1);
		      adapter.setSelectedPosition(-1);
		      return true;

		    } else {
		      Log.d(TAG, "Unknown event action=" + action);
		    }

		    return super.onTouchEvent(event);
		  }

		  /**
		   * ドラッグを開始したときに呼び出される<br />
		   * ドラッグ中の項目を表示する
		   */
		  private void startDrag() {
		    WindowManager wm;
		    View view = getChildByIndex(mFrom);

		    final Bitmap.Config c = Bitmap.Config.ARGB_8888;
		    mDragBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), c);
		    Canvas canvas = new Canvas();
		    canvas.setBitmap(mDragBitmap);
		    view.draw(canvas);

		    if (mWindowParams == null) {
		      mWindowParams = new WindowManager.LayoutParams();
		      mWindowParams.gravity = Gravity.TOP | Gravity.LEFT;

		      mWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
		      mWindowParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
		      mWindowParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
		      mWindowParams.format = PixelFormat.TRANSLUCENT;
		      mWindowParams.windowAnimations = 0;
		      mWindowParams.x = 0;
		      mWindowParams.y = 0;
		    }

		    ImageView v = new ImageView(getContext());
		    v.setBackgroundColor(MOVING_ITEM_BG_COLOR);
		    v.setImageBitmap(mDragBitmap);

		    wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
		    if (mDragView != null) {
		      wm.removeView(mDragView);
		    }
		    wm.addView(v, mWindowParams);
		    mDragView = v;
		  }

		  /**
		   * ドラッグアンドドロップが終了したときに呼び出される<br />
		   * ドラッグ中の項目の表示を削除する
		   */
		  private void endDrag() {
		    if (mDragView == null) {
		      return;
		    }
		    WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
		    wm.removeView(mDragView);
		    mDragView = null;
		    // リサイクルするとたまに死ぬけどタイミング分からない
		    // Desireでおきる。ht-03aだと発生しない
		    // mDragBitmap.recycle();
		    mDragBitmap = null;
		  }

		  /**
		   * 指定された座標が削除画像に被っているか返す
		   *
		   * @param x
		   * @param y
		   * @return
		   */
		  private boolean isHitRemoveTile(int x, int y) {
		    if (mRemoveTile != null && mRemoveTile.getVisibility() == View.VISIBLE) {
		      if (mRemoveHit == null) {
		        mRemoveHit = new Rect();
		      }
		      mRemoveTile.getHitRect(mRemoveHit);
		      if (mRemoveHit.contains(x + getLeft(), y + getTop())) {
		        return true;
		      } else {
		        return false;
		      }
		    } else {
		      return false;
		    }
		  }

		  /**
		   * 指定された要素番号のViewを返す<br />
		   * 要素番号はソースとなるListの要素番号を指定する
		   *
		   * @param index
		   * @return
		   */
		  private View getChildByIndex(int index) {
		    return getChildAt(index - getFirstVisiblePosition());
		  }

		  /**
		   * MotionEventから要素番号に変換する
		   *
		   * @param ev
		   * @return
		   */
		  private int pointToIndex(MotionEvent event) {
		    return pointToIndex((int) event.getX(), (int) event.getY());
		  }

		  /**
		   * 座標から要素番号に変換する
		   *
		   * @param x
		   * @param y
		   * @return
		   */
		  private int pointToIndex(int x, int y) {
		    return (int) pointToPosition(x, y);
		  }

		  /**
		   * 削除画像の設定
		   *
		   * @param v
		   */
		  public void setRemoveTile(View v) {
		    mRemoveTile = v;
		  }

		  public void setOnDragListener(DragListener listener) {
		    mDrag = listener;
		  }

		  public void setOnDropListener(DragListener listener) {
		    mDrag = listener;
		  }

		  public void setOnRemoveListener(RemoveListener listener) {
		    mRemove = listener;
		  }


		  class DragListenerImpl implements DragListener {
		    public void drag(int from, int to) {
		      if (DEBUG) {
		        Log.d(TAG, "DragListenerImpl drag event. from=" + from + ", to=" + to);
		      }
		    }
		  }

		  public class DropListenerImpl implements DropListener {

			@SuppressWarnings("unchecked")
		    public void drop(int from, int to) {
		      if (DEBUG) {
		        Log.d(TAG, "DropListenerImpl drop event. from=" + from + ", to=" + to);
		      }

		      if (from == to || from < 0 || to < 0) {
		        return;
		      }

		      Adapter adapter = getAdapter();
		      if (adapter != null && adapter instanceof ArrayAdapter) {
		        ArrayAdapter arrayAdapter = (ArrayAdapter) adapter;
		        Object item = adapter.getItem(from);

		        arrayAdapter.remove(item);
		        arrayAdapter.insert(item, to);
		        //ファイルに書き込んじゃう
		        calledClass.WriteEducation();
		      }
		    }
		  }

		  public class RemoveListenerImpl implements RemoveListener {
		    @SuppressWarnings("unchecked")
		    public void remove(int which) {
		      if (DEBUG) {
		        Log.d(TAG, "RemoveListenerImpl remove event. which=" + which);
		      }

		      if (which < 0) {
		        return;
		      }

		      Adapter adapter = getAdapter();
		      if (adapter != null && adapter instanceof ArrayAdapter) {
		        ArrayAdapter arrayAdapter = (ArrayAdapter) adapter;
		        Object item = adapter.getItem(which);

		        arrayAdapter.remove(item);
		      }
		    }
		  }

		  /**
		   * 並び替えをするかを設定する
		   *
		   * @param sortMode
		   */
		  public void setSortMode(boolean sortMode) {
		    this.sortMode = sortMode;
		  }

		  /**
		   * 並び替えをするかを返す
		   *
		   * @return
		   */
		  public boolean isSortMode() {
		    return sortMode;
		  }

		  /**
		   * ListViewにAdapterを設定する<br />
		   * Sortableを実装したAdapter以外を受け付けません
		   *
		   * @param adapter
		   */
		  @Override
		  public void setAdapter(ListAdapter adapter) {
		    if (adapter instanceof SortableAdapter) {
		      this.adapter = (SortableAdapter) adapter;
		      super.setAdapter(adapter);
		    } else {
		      throw new RuntimeException("Sortable未実装");
		    }
		  }



		}//End of DragnDropListView

	   interface DragListener {
	    public void drag(int from, int to);
	  }

	   interface DropListener {
	    public void drop(int from, int to);
	  }

	   interface RemoveListener {
	    public void remove(int which);
	  }

	  interface SortableAdapter {
	    void setSelectedPosition(int position);
	    String[] getItem(int i);
		int getCount();
		void setHoverPosition(int position);
	  }


