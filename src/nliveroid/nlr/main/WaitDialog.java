package nliveroid.nlr.main;

import nliveroid.nlr.main.LiveTab.SecondSendForm_GetLVTask;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager.BadTokenException;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

//できれば裏に回った場合に消したいがそれは未実装

public class WaitDialog extends Dialog{
	private boolean isCanceled;
	private TextView tex;
	public WaitDialog(final Context context,final SecondSendForm_GetLVTask task) {
		super(context);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		final LinearLayout myDialogLayout = new LinearLayout(context);
		myDialogLayout.setBackgroundColor(Color.WHITE);
		ProgressBar kurukuru = new ProgressBar(context,null,android.R.attr.progressBarStyle);

		tex = new TextView(context);
		tex.setTextSize(20);
		tex.setTextColor(Color.rgb(153,255,69));
		tex.setText("順番待ち中です...\n");
		tex.setGravity(Gravity.CENTER);

		myDialogLayout.addView(kurukuru,new LinearLayout.LayoutParams(-2,-2));
		myDialogLayout.addView(tex,new LinearLayout.LayoutParams(-1, -2));
		setContentView(myDialogLayout);
	       setCancelable(true);
	       this.setOnDismissListener(new OnDismissListener(){

				@Override
				public void onDismiss(DialogInterface arg0) {
					isCanceled = true;
				}

		       	});
	       this.setOnCancelListener(new DialogInterface.OnCancelListener(){
			@Override
			public void onCancel(DialogInterface dialog) {
				isCanceled = true;
				//キャンセルをするか
				new AlertDialog.Builder(context)
				.setTitle("順番待ち")
				.setMessage("キャンセルしますか?")
				.setPositiveButton("YES",new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which) {
						task.setWaitCancel();
					}
					
				})
				.setNegativeButton("NO", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						//順番待ちを継続する	
						task.continueWait();
					}
				}).create().show();
						
			}

	       		});
	}

	public void updateCount(String str){
		tex.setText("順番待ち中です...\n待ち人数:"+str+"人");
	}

	public boolean isCanceled(){
		return isCanceled;
	}

	@Override
	public void show(){
		try{
			isCanceled = false;
			super.show();
		}catch(BadTokenException e){
			e.printStackTrace();
			return;
		}
	}

}
