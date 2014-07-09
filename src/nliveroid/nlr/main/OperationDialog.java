package nliveroid.nlr.main;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class OperationDialog extends AlertDialog.Builder{
	private AlertDialog me;
	public OperationDialog(final CommentPostable postable,boolean getBetween,byte init_mode) {
		super((Context)postable);
		if(NLiveRoid.isDebugMode)Log.d("NLiveRoid","OperationDialog ");
		View parent = (View)(LayoutInflater.from((Context)postable).inflate(R.layout.operation_dialog,null));
		setView(parent);

		final CheckBox between = (CheckBox)parent.findViewById(R.id.is_get_between);
		Button updateComment = (Button)parent.findViewById(R.id.update_comment);
		Button disconnect = (Button)parent.findViewById(R.id.disconnect_comment);
		Button update_player = (Button)parent.findViewById(R.id.oeration_reload_bt);
		Button all_update = (Button)parent.findViewById(R.id.all_update);


		switch(init_mode){
		case 2://Playerのみ
			updateComment.setEnabled(false);
			between.setEnabled(false);
			disconnect.setEnabled(false);
			break;
		case 3://コメントのみ
			update_player.setEnabled(false);
			break;
		case 4://配信
			update_player.setVisibility(View.GONE);
			break;
		}

		between.setChecked(getBetween);
		updateComment.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				me.cancel();
				postable.updateCommentTable(between.isChecked());
			}
		});
		between.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(CompoundButton compoundbutton,
					boolean flag) {
				postable.setUpdateBetween(flag);
			}

		});

		disconnect.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View arg0) {
				me.cancel();
				postable.disConnectComment();
			}
		});
		update_player.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				me.cancel();
				postable.reloadPlayer();
			}
		});

		all_update.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				me.cancel();
				postable.allUpdate();
			}
		});



	}

	public void showSelf(){
		this.create();
		me = this.show();

	}

}
