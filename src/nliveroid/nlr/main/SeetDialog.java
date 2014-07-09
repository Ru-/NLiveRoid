package nliveroid.nlr.main;

import java.io.IOException;
import java.net.UnknownHostException;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;

public class SeetDialog extends AlertDialog.Builder{
	private AlertDialog me;
	public SeetDialog(final CommentPostable commentPostable ,final CommentTable commentTable,final CommentListAdapter adapter,final short initCommentCount,final int isLowAPI) {
		super((Context)commentPostable);
		LayoutInflater inflater = LayoutInflater.from((Context)commentPostable);
		View parent = inflater.inflate(R.layout.seetselect, null);
		setView(parent);
		Button arina_b = (Button) parent.findViewById(R.id.seet_arina);
		arina_b.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View arg0) {
				try {
					byte failed = commentTable.selectSeet(0,initCommentCount,isLowAPI);
					switch(failed){
					case -1:
						MyToast.customToastShow((Context)commentPostable, "座席の変更に失敗しました\n放送情報取得に失敗している");
						break;
					case -2:
						MyToast.customToastShow((Context)commentPostable, "座席の変更に失敗しました\nポートの情報がなかった");
						break;
					}
				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				me.cancel();
			}
		});
		Button stand_a = (Button) parent.findViewById(R.id.seet_a);
		stand_a.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View arg0) {
				try {
					byte failed = commentTable.selectSeet(1,initCommentCount,isLowAPI);
					switch(failed){
					case -1:
						MyToast.customToastShow((Context)commentPostable, "座席の変更に失敗しました\n放送情報取得に失敗している");
						break;
					case -2:
						MyToast.customToastShow((Context)commentPostable, "座席の変更に失敗しました\nポートの情報がなかった");
						break;
					}
				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				me.cancel();
			}
		});
		Button stand_b = (Button) parent.findViewById(R.id.seet_b);
		stand_b.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View arg0) {
				try {
					byte failed = commentTable.selectSeet(2,initCommentCount,isLowAPI);
					switch(failed){
					case -1:
						MyToast.customToastShow((Context)commentPostable, "座席の変更に失敗しました\n放送情報取得に失敗している");
						break;
					case -2:
						MyToast.customToastShow((Context)commentPostable, "座席の変更に失敗しました\nポートの情報がなかった");
						break;
					}
				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				me.cancel();
			}
		});
		Button stand_c = (Button) parent.findViewById(R.id.seet_c);
		stand_c.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View arg0) {
				try {
					byte failed = commentTable.selectSeet(3,initCommentCount,isLowAPI);
					switch(failed){
					case -1:
						MyToast.customToastShow((Context)commentPostable, "座席の変更に失敗しました\n放送情報取得に失敗している");
						break;
					case -2:
						MyToast.customToastShow((Context)commentPostable, "座席の変更に失敗しました\nポートの情報がなかった");
						break;
					}
				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				me.cancel();
			}
		});
	}

	public void showSelf(){
		this.create();
		me = this.show();
	}


}
