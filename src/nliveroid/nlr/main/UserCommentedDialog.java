package nliveroid.nlr.main;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.text.ClipboardManager;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class UserCommentedDialog extends AlertDialog.Builder{

	private CommentListAdapter adapter;
	private Activity ACT;
	private ProgressDialog progressD;
	private AlertDialog me;

	/**
	 * ユーザーの発言リスト
	 * @param postable
	 * @param userid
	 * @param commentedRows
	 * @param dialog
	 */
	public UserCommentedDialog(final CommentPostable postable,String userid,final String nickname,ArrayList<String[]> commentedRows,ProgressDialog dialog,final int bgColor,final int foreColor) {
		super((Activity)postable);
		ACT = (Activity) postable;
		progressD = dialog;
		LayoutInflater inflater = LayoutInflater.from(ACT);
		View parent = inflater.inflate(R.layout.usercomment, null);
		setView(parent);
		setCustomTitle(null);
		TextView tv = (TextView)parent.findViewById(R.id.userid);
		tv.setText("ユーザID　"+userid);
		final ListView list = new ListView(ACT);
		list.setOnCreateContextMenuListener(new OnCreateContextMenuListener(){

			private String tempID;

			@Override
			public void onCreateContextMenu(ContextMenu menu, View v,
					ContextMenuInfo menuInfo) {
				AdapterContextMenuInfo adapterInfo = (AdapterContextMenuInfo) menuInfo;
				if (adapter.getCount() < adapterInfo.position) {// 立見などをタップした時におかしくなるArrayList.throwIndexOutOfBoundsException
					return;
				}
				final String[] row = adapter.getItem(adapterInfo.position);
				tempID = adapter.getItem(adapterInfo.position)[1];

				menu.add("コテハンを編集");
				menu.getItem(0).setOnMenuItemClickListener(
						new OnMenuItemClickListener() {
							@Override
							// 引数はメニューのテキスト
							public boolean onMenuItemClick(MenuItem arg0) {
								me.cancel();
								new HandleNamePicker(ACT,
										new ColorPickerView.OnColorChangedListener() {
											@Override
											public void colorChanged(int color) {
												// 色が選択されるとcolorに値が入る OKボタンで確定するので未使用
												int R = Color.red(color);
												int G = Color.green(color);
												int B = Color.blue(color);
											}
										}, bgColor, foreColor , tempID,nickname,true)
										.show();
								return false;
							}
						});

				menu.add("列のコピー");
				menu.getItem(1).setOnMenuItemClickListener(
						new OnMenuItemClickListener() {
							@Override
							public boolean onMenuItemClick(MenuItem menuitem) {
								new AlertDialog.Builder(ACT)
								.setItems(new CharSequence[]{"ユーザタイプ","ID","コマンド","時間","NGスコア","コメ番"},new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										ClipboardManager cm = (ClipboardManager) ((Activity)postable).getSystemService(((Activity)postable).CLIPBOARD_SERVICE);
										String text = "";
										switch(which){
										case 0:
											text = row[0];
											break;
										case 1:
											text = row[1];
											break;
										case 2:
											text = row[2];
											break;
										case 3:
											text = row[3];
											break;
										case 4:
											text = row[4];
											break;
										case 5:
											text = row[5];
											break;
										}
										cm.setText(text);
									}
								}).create().show();
								return false;
							}
						});
				menu.add("コメントを表示");
				menu.getItem(2).setOnMenuItemClickListener(
						new OnMenuItemClickListener() {
							@Override
							public boolean onMenuItemClick(MenuItem menuitem) {
								me.cancel();
								new ContextDialog(ACT, row,nickname,postable.getViewWidth(), bgColor, foreColor ).showSelf();
								return false;
							}
						});
				menu.add("コメントをコピー");
				menu.getItem(3).setOnMenuItemClickListener(
						new OnMenuItemClickListener() {
							@Override
							public boolean onMenuItemClick(MenuItem menuitem) {

								ClipboardManager cm = (ClipboardManager) ((Activity)postable).getSystemService(((Activity)postable).CLIPBOARD_SERVICE);
								// クリップボードへ値をコピー。
								cm.setText(row[6]);
								return false;
							}
						});
			}

		});
		list.setFastScrollEnabled(true);
		list.setBackgroundColor(-1);
		adapter = postable.createNewAdapter();
		list.setAdapter(adapter);
		for(String[] i:commentedRows){
			adapter.add(i);
		}
		LinearLayout ll = (LinearLayout)parent.findViewById(R.id.list_parent_liner);
		ll.addView(list,new LinearLayout.LayoutParams(-1,-1));
	}

	public void showSelf(){
		me = this.create();
		if(progressD != null)progressD.cancel();
		me.show();
	}





}
