package nliveroid.nlr.main;

import nliveroid.nlr.main.Details.OffTimerNp;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ScrollView;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class ConfigOtherDialog  extends AlertDialog.Builder {
	private AlertDialog me;
	protected ConfigOtherDialog(final CommentPostable postable,final byte offtimer,int init_mode) {
		super((Context)postable);
		View parent = LayoutInflater.from((Context)postable).inflate(R.layout.config_other_dialog, null);
		setView(parent);
		Button fix_screen = (Button) parent.findViewById(R.id.config_fixscreen);
		final CheckBox offtimer_enable = (CheckBox)parent.findViewById(R.id.offtimer_enable);
		final Button timer_interval = (Button)parent.findViewById(R.id.offtimer_interval);

		//項目の有効無効を設定する
		switch(init_mode){
		case 0://前面
			break;
		case 1://背面
			break;
		case 2://プレイヤーのみ
			break;
		case 3:
			break;
		}


		fix_screen.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View arg0) {
				me.cancel();
				postable.showOrientationAlertBuilder();
			}
		});
		if(offtimer<0){
			timer_interval.setVisibility(View.INVISIBLE);
		}
		timer_interval.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				me.cancel();
				new OffTimerNp((Context) postable,postable.getOffTimerStart(),offtimer).create().show();
			}
		});
		if(offtimer>0){
			offtimer_enable.setChecked(true);
		}else{
			offtimer_enable.setChecked(false);
		}
		offtimer_enable.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if(!isChecked){//設定をOFFにした
		    		timer_interval.setVisibility(View.INVISIBLE);
					Intent intent = new Intent();
		    		intent.setAction("bindTop.NLR");
		    		intent.putExtra("off_timer", "-1");
		    		((Context)postable).sendBroadcast(intent);
				}else if(isChecked){//新たにスタート
		    		timer_interval.setVisibility(View.VISIBLE);
					Intent intent = new Intent();
		    		intent.setAction("bindTop.NLR");
		    		intent.putExtra("off_timer", String.valueOf(postable.getOffTimerValue()));
		    		((Context)postable).sendBroadcast(intent);
				}
			}
		});
	}


	public void showSelf(){
		this.create();
		me = this.show();
	}

}
