package nliveroid.nlr.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nliveroid.nlr.main.parser.XMLparser;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TableRow;
import android.widget.TextView;

public class HandleNameList extends Activity implements HandleNamable {
	private Activity ACT;
	private ListView listview;
	private ArrayAdapter<String[]> adapter;

	private LayoutInflater inflater;
	private ArrayList<String> ids;
	private ArrayList<String> names;
	private ArrayList<Integer> bgColors;
	private ArrayList<Integer> foColors;
	private HashMap<String,Boolean> cbCheck;
	private String handleNameFile = "handlenames.xml";

	private boolean isSelectMode;

	private int tempPosition;
	private boolean addFlug;//addする時のみキャンセル時処理を変える
	private CheckBox allSelectCb;
	private boolean isContextDisp;

	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		ACT = this;
		inflater = LayoutInflater.from(this);
		View parent = inflater.inflate(R.layout.handle_list, null);
		NLiveRoid app = (NLiveRoid)getApplicationContext();
		TextView head0 = (TextView) parent.findViewById(R.id.handle_head_name);
		head0.setWidth( app.getViewWidth() / 2);
		TextView head1 = (TextView) parent.findViewById(R.id.handle_head_id);
		head1.setWidth(app.getViewWidth() / 2);
		listview = (ListView) parent.findViewById(R.id.handle_list);
		adapter = new HandleListAdapter(this, R.layout.handle_list_row);
		listview.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				if(isContextDisp){
					isContextDisp = false;
	   					 return;
	   				 }
				tempPosition = position;
				new AlertDialog.Builder(ACT)
				.setItems(new String[]{"編集","削除"}, new OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch(which){
						case 0:
							int defaultBgColor = bgColors.get(tempPosition);
							int defaultFoColor = foColors.get(tempPosition);
							new HandleNamePicker(ACT,
					  				new ColorPickerView.OnColorChangedListener() {
										@Override
										public void colorChanged(int color) {
											// 色が選択されるとcolorに値が入る OKボタンで確定するので未使用
											int R = Color.red(color);
											int G = Color.green(color);
											int B = Color.blue(color);
										}
									}, defaultBgColor, defaultFoColor,ids.get(tempPosition),names.get(tempPosition),false).show();
							break;
						case 1:
							AlertDialog.Builder ad = new AlertDialog.Builder(ACT);
							ad.setTitle("ユーザ情報の削除");
							ad.setMessage("このコテハンを削除します\nよろしいですか?");
							ad.setPositiveButton("OK",
									new DialogInterface.OnClickListener() {
										public void onClick(DialogInterface dialog,
												int whichButton) {
											cbCheck.remove(ids.get(tempPosition));
											adapter.remove(adapter.getItem(tempPosition));
											ids.remove(tempPosition);
											names.remove(tempPosition);
											bgColors.remove(tempPosition);
											foColors.remove(tempPosition);
											new WriteHandleName().execute();
										}
									});
							ad.setNegativeButton("CANCEL",
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface dialog,
												int which) {
										}
									});
							ad.create();
							ad.show();
							break;
						}
					}
				}).create().show();
			}
		});
		listview.setFastScrollEnabled(true);
		listview.setAdapter(adapter);
		registerForContextMenu(listview);
		ids = new ArrayList<String>();
		names = new ArrayList<String>();
		bgColors = new ArrayList<Integer>();
		foColors = new ArrayList<Integer>();
		cbCheck = new HashMap<String,Boolean>();
		// ファイル読み込み
		ErrorCode error = ((NLiveRoid)getApplicationContext()).getError();
		readHandleNameData(error);
		if(error != null &&error.getErrorCode() != 0){
			error.showErrorToast();
			finish();
			return;
		}
		String id = "";
		try{
		for (int i = 0; i < ids.size(); i++) {
			id = ids.get(i).length() > 8 ? ids.get(i).substring(0, 6) + ".."
					: ids.get(i);
			adapter.add(new String[] { names.get(i), id });
		}
		}catch(NullPointerException e){
			MyToast.customToastShow(this.getApplicationContext(), "コテハンファイルの記述がおかしいです");
			finish();
		}

		Button addButton = (Button) parent.findViewById(R.id.addbutton);
		addButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				tempPosition = ids.size();
				addFlug = true;
				ids.add("");
				names.add("");
				bgColors.add(-1);
				foColors.add(0);
				cbCheck.put(ids.get(ids.size()-1),false);
				adapter.add(new String[] { "", "" });
				final EditText et = new EditText(ACT);
				et.setSingleLine(true);
				AlertDialog.Builder ad = new AlertDialog.Builder(ACT);
				ad.setTitle("ユーザ情報の追加");
				ad.setMessage("ユーザIDを入力して下さい");
				ad.setPositiveButton("OK",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								// 入力チェック
								String tex = et.getText().toString().trim();
								Matcher mc = Pattern.compile("[^0-9a-zA-Z]")
										.matcher(tex);
								if (!mc.find() && !tex.equals("")) {
									ids.set(tempPosition, tex);
									new HandleNamePicker(
											ACT,
											new ColorPickerView.OnColorChangedListener() {
												@Override
												public void colorChanged(
														int color) {
													// 色が選択されるとcolorに値が入る
													// OKボタンで確定するので未使用
													int R = Color.red(color);
													int G = Color.green(color);
													int B = Color.blue(color);
													android.util.Log
															.d("ColorPickerDialog",
																	"(R,G,B)=("
																			+ R
																			+ ","
																			+ G
																			+ ","
																			+ B
																			+ ")");
												}
											}, Color.WHITE,Color.BLACK, tex,tex,false).show();
								} else {
									Builder error = new AlertDialog.Builder(ACT);
									error.setMessage("ユーザIDが不正です");
									error.setPositiveButton("OK", null);
									error.show();
									cancelColorPicker();
								}
							}
						});
				ad.setView(et);
				ad.setNegativeButton("CANCEL",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								cancelColorPicker();
							}
						});
				ad.create();
				ad.show();

			}

		});
		final DialogInterface.OnClickListener preventVeryfiError1 = new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog,int whichButton) {
				// 削除処理
				Log.d("NLR","REMOVED" );
				for(int i = 0; i < ids.size(); i++){
						if(cbCheck.get(ids.get(i))){
//							Log.d("NLR","REMOVED" + adapter.getItem(i)[0]);
							adapter.remove(adapter.getItem(i));
							cbCheck.remove(ids.get(i));
							ids.remove(i);
							names.remove(i);
							bgColors.remove(i);
							foColors.remove(i);
							i--;
						}
				}
				new WriteHandleName().execute();
				//削除モードをキャンセルする
				clearDeleteMode();
			}// End of reallyOK click
		};

		final DialogInterface.OnClickListener preventVeryfiError2 = new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog,int whichButton) {}};
		Button deleteButton = (Button) parent
				.findViewById(R.id.deletebutton);
		deleteButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if(isSelectMode){//選択中だったら、チェックされた項目を削除するダイアログをだす
					//選択されたのが1つ以上なければ削除モードを解除
					if(!cbCheck.containsValue(true)){
						clearDeleteMode();
						return;
					}
					AlertDialog.Builder bachDelete = new AlertDialog.Builder(ACT);
					bachDelete.setMessage("選択したコテハンを削除します\nよろしいですか?");
									bachDelete.setPositiveButton("OK",preventVeryfiError1
											);
									bachDelete.setNegativeButton("CANCEL",preventVeryfiError2);
									bachDelete.create();
									bachDelete.show();
				}else{//チェックモードじゃなかった
				toDeleteMode();
				}
			}// End of DeleteButton Click
		});// End of DeleteButton listener

		//一括削除チェック
		allSelectCb = (CheckBox)parent.findViewById(R.id.handle_all_select);
		allSelectCb.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(CompoundButton compoundbutton,
					boolean flag) {
				if(flag){
					for(int i = 0; i < ids.size(); i++){
						cbCheck.put(ids.get(i),true);
					}
					//リストの見える部分のチェックを入れる
					for(int i = 0; i< listview.getCount(); i++){
						if(listview.getChildAt(i)==null)break;
						CheckBox cb = (CheckBox) listview.getChildAt(i).findViewById(R.id.handle_delete_cb);
						if(cb != null){
						cb.setChecked(true);
						}
					}
				}else{
					for(int i = 0; i < ids.size(); i++){
						cbCheck.put(ids.get(i),false);
					}
					//リストの見える部分のチェックをはずす
					for(int i = 0; i< listview.getCount(); i++){
						if(listview.getChildAt(i)==null)break;
						CheckBox cb = (CheckBox) listview.getChildAt(i).findViewById(R.id.handle_delete_cb);
						if(cb != null){
						cb.setChecked(false);
						}
					}
				}
			}
		});

		parent.setBackgroundColor(Color.WHITE);
		setContentView(parent);
	}

	private void toDeleteMode(){
		//画面に見えている選択削除のチェックボックスを表示する
		isSelectMode = true;
		for(int i = 0; i< listview.getCount(); i++){
			if(listview.getChildAt(i)==null)break;
			CheckBox cb = (CheckBox) listview.getChildAt(i).findViewById(R.id.handle_delete_cb);
			if(cb != null){
			cb.setVisibility(View.VISIBLE);
			cb.setChecked(false);
			}
		}
		for(int i = 0; i < ids.size(); i++){
			cbCheck.put(ids.get(i), false);
		}
		allSelectCb.setChecked(false);
		allSelectCb.setVisibility(View.VISIBLE);
	}
	private void clearDeleteMode(){
		Log.d("NLR","CLEAR DELETE MODE");
		isSelectMode = false;//画面上に見えているチェックボックスのチェックをはずす
		for(int i = 0 ; i < listview.getCount(); i++){
			if(listview.getChildAt(i)==null)break;
			CheckBox cb = (CheckBox)listview.getChildAt(i).findViewById(R.id.handle_delete_cb);
			if(cb != null){
				cb.setVisibility(View.GONE);
			}else{
				break;
			}
		}
		allSelectCb.setVisibility(View.GONE);
	}

	class HandleListAdapter extends ArrayAdapter<String[]> {

		NLiveRoid app = null;
		public HandleListAdapter(Context context, int textViewResourceId) {
			super(context, textViewResourceId);
			app = (NLiveRoid)getApplicationContext();
		}

		@Override
		public View getView(int position, View paramView,
				ViewGroup paramViewGroup) {
			final ViewHolder holder;
			View view = paramView;
			if (view == null) {
			view = inflater.inflate(R.layout.handle_list_row, null);
			holder = new ViewHolder();
			holder.row = (TableRow)view.findViewById(R.id.handle_row);
			holder.id = (TextView) view.findViewById(R.id.handle_id);
			holder.name = (TextView) view.findViewById(R.id.handle_name);
			holder.cb = (CheckBox)view.findViewById(R.id.handle_delete_cb);
			holder.cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
				@Override
				public void onCheckedChanged(CompoundButton compoundbutton,
						boolean flag) {
					cbCheck.put(holder.id.getText().toString(), flag);
					//全てがチェックされたら全選択を全解除に変える
					//リスナが自動的に呼ばれるのでcbCheckのデータはここでは変えない
					if(flag){//新たな値がtrueの時、全てがtrueになったら解除に変える
						if(!cbCheck.containsValue(false)){
							allSelectCb.setChecked(true);
							allSelectCb.setText("全解除");
						}
					}else{//新たな値がfalseの時、全てがfalseになったら選択に変える
						if(!cbCheck.containsValue(true)){
							allSelectCb.setChecked(false);
							allSelectCb.setText("全選択");
						}
					}
				}
			});
			view.setTag(holder);
			}else {
					holder = (ViewHolder) view.getTag();
				}

			holder.name.setWidth(app.getViewWidth() / 2);
			holder.name.setText(names.get(position));
			holder.id.setText(ids.get(position));
			holder.id.setWidth(app.getViewWidth() / 2);
			if (bgColors.size() > position) {//何故か色はこれでできる
				holder.row.setBackgroundColor(bgColors.get(position));
				holder.id.setBackgroundColor(bgColors.get(position));
				holder.name.setBackgroundColor(bgColors.get(position));
				holder.cb.setBackgroundColor(bgColors.get(position));
			}
			if (foColors.size() > position) {//何故か色はこれでできる
				holder.id.setTextColor(foColors.get(position));
				holder.name.setTextColor(foColors.get(position));
			}
			if(isSelectMode){
				holder.cb.setVisibility(View.VISIBLE);
			}else{
				holder.cb.setVisibility(View.GONE);
			}
			if(cbCheck.size() > position){
					holder.cb.setChecked(cbCheck.get(holder.id.getText()) ==null? true:cbCheck.get(holder.id.getText()));
			}
			return view;
		}

	}

		private static class ViewHolder {
			TableRow row;
			TextView id;
			TextView name;
			CheckBox cb;
		}
	@Override
	protected void onSaveInstanceState(Bundle outState) {
	    outState.putString("WORKAROUND_FOR_BUG_19917_KEY", "WORKAROUND_FOR_BUG_19917_VALUE");
//	    super.onSaveInstanceState(outState);
	}
	@Override
	public boolean dispatchKeyEvent(KeyEvent keyevent){
		if(keyevent.getKeyCode() == KeyEvent.KEYCODE_BACK&&isSelectMode){
			//選択モードだったらキャンセルしてチェックも消す
			clearDeleteMode();
			for(int i = 0;i < ids.size();i++){
				cbCheck.put(ids.get(i), false);
			}
				return false;
		}
		return super.dispatchKeyEvent(keyevent);
	}
	/**
	 * コンテキストメニュー生成時処理
	 */
	public void onCreateContextMenu(ContextMenu menu, View view,
			ContextMenuInfo info) {
		super.onCreateContextMenu(menu, view, info);
		isContextDisp = true;
		final AdapterContextMenuInfo adapterInfo = (AdapterContextMenuInfo) info;
		tempPosition = adapterInfo.position;
		addFlug = false;
					// 引数はメニューのテキスト
						int defaultBgColor = bgColors.get(tempPosition);
						int defaultFoColor = foColors.get(tempPosition);
						new HandleNamePicker(ACT,
				  				new ColorPickerView.OnColorChangedListener() {
									@Override
									public void colorChanged(int color) {
										// 色が選択されるとcolorに値が入る OKボタンで確定するので未使用
										int R = Color.red(color);
										int G = Color.green(color);
										int B = Color.blue(color);
									}
								}, defaultBgColor, defaultFoColor,ids.get(tempPosition),names.get(tempPosition),false).show();
	}

	/**
	 * コテハン設定ダイアログの反映
	 *
	 * @param color
	 * @param name
	 */
	@Override
	public void setHandleName(int bgColor,int foColor, String name) {
		// idはすでにaddされている
		bgColors.set(tempPosition, bgColor);
		foColors.set(tempPosition, foColor);
		names.set(tempPosition, name);
		new WriteHandleName().execute();
		listview.setAdapter(adapter);
		listview.setSelection(tempPosition);
	}

	public void cancelColorPicker() {
		if(addFlug){
			cbCheck.remove(cbCheck.get(ids.get(tempPosition)));
		ids.remove(tempPosition);
		names.remove(tempPosition);
		bgColors.remove(tempPosition);
		foColors.remove(tempPosition);
		adapter.remove(adapter.getItem(tempPosition));
		}
	}



	/**
	 * ストレージのパスを取得します	 *
	 *
	 */

	private String getStorageFilePath(){
		boolean isStorageAvalable = false;
		boolean isStorageWriteable = false;
		String state = Environment.getExternalStorageState();
		if(state == null){
			MyToast.customToastShow(this, "SDカードが利用できませんでした\nコテハンは機能できません");
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
			MyToast.customToastShow(this, "SDカードが利用できませんでした\nコテハンは機能できません");
			return null;
		}


		//sdcard直下に、パッケージ名のフォルダを作りファイルを生成
		String filePath = Environment.getExternalStorageDirectory().toString() + "/NLiveRoid";

		File directory = new File(filePath);
//		Log.d("log","filepath " + filePath + " \n isCANWRITE " + directory.canWrite());
		if(directory.mkdirs()){//すでにあった場合も失敗する
			Log.d("NLiveRoid","mkdir");
		}
		File file = new File(filePath,handleNameFile);
		if(!file.exists()){
			try {
				file.createNewFile();
				writeHandleName();//次からの読み込みがエラーしないように空のファイルを作っておく
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return file.getPath();
	}

	/**
	 * コテハンファイルの読み込み
	 *
	 * @author Owner
	 *
	 */

	private synchronized void readHandleNameData(ErrorCode error) {
		try {
			String filepath = getStorageFilePath();
			if(filepath == null)return;
			FileInputStream fis = new FileInputStream(filepath);
			byte[] readBytes = new byte[fis.available()];
			fis.read(readBytes);
			XMLparser.setHandleNameMaps(ids, names, bgColors,foColors, readBytes,error);
			//ここでチェックボックスのステータスもそのサイズで初期化
			for(int i= 0; i < ids.size(); i++){
				cbCheck.put(ids.get(i),false);
			}
			fis.close();
		} catch (FileNotFoundException e) {
			error.setErrorCode(-44);
			e.printStackTrace();
		} catch (IOException e) {
			error.setErrorCode(-44);
			e.printStackTrace();
		}
	}

	/**
	 * コテハンの書き込み
	 *
	 * @author Owner
	 *
	 */
	class WriteHandleName extends AsyncTask<Void,Void,Void>{

		@Override
		protected Void doInBackground(Void... arg0) {
			writeHandleName();
			return null;
		}

	}

	private synchronized void writeHandleName() {
		try {
			String filepath = getStorageFilePath();
			if(filepath == null)return;
			String xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<HandleNames xmlns=\"http://nliveroid-tutorial.appspot.com/handlenames/\">\n";
			for (int i = 0; i < ids.size(); i++) {
				xml += "<user bgcolor=\"" + bgColors.get(i) + "\" name=\""
						+ names.get(i) + "\" focolor=\""+foColors.get(i)+"\">" + ids.get(i) + "</user>\n";
			}
			xml += "</HandleNames>";

			FileOutputStream fos = new FileOutputStream(filepath);
			fos.write(xml.getBytes());
			fos.close();
		} catch (FileNotFoundException e) {
			ErrorCode error = ((NLiveRoid)getApplicationContext()).getError();
			e.printStackTrace();
		} catch (IOException e) {
			ErrorCode error = ((NLiveRoid)getApplicationContext()).getError();
			e.printStackTrace();
		}
	}

	@Override
	public void createCommentedList(String userid) {
		// TODO 自動生成されたメソッド・スタブ
	}
	@Override
	public boolean isAt() {
		// TODO 自動生成されたメソッド・スタブ
		return false;
	}
	@Override
	public boolean isAtOverwrite() {
		// TODO 自動生成されたメソッド・スタブ
		return false;
	}
	@Override
	public void setAtEnable(boolean isAt) {
		// TODO 自動生成されたメソッド・スタブ
	}
	@Override
	public void setAtOverwrite(boolean isAtoverwrite) {
		// TODO 自動生成されたメソッド・スタブ
	}
	@Override
	public void setAutoGetUserName(boolean isChecked) {
		// TODO 自動生成されたメソッド・スタブ
	}
	@Override
	public boolean isSetNameReady() {
		// TODO 自動生成されたメソッド・スタブ
		return false;
	}

}
