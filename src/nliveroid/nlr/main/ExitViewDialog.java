package nliveroid.nlr.main;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ExitViewDialog extends Builder{
	private Activity calledACT;
	protected ExitViewDialog(final Context context) {
		super(context);
		calledACT = (Activity) context;
		LinearLayout myDialogLayout = new LinearLayout(context);
			myDialogLayout.setBackgroundColor(Color.WHITE);

			TextView text = new TextView(context);
			text.setTextSize(23);
			text.setTextColor(Color.rgb(153,255,69));
			text.setText("終了しますか?");
			text.setGravity(Gravity.CENTER);
			this.setView(myDialogLayout)
		       .setCancelable(true)
		       .setPositiveButton("Yes", new YesListener())
		       .setNegativeButton("No", new NoListener())
		       .create();



		myDialogLayout.addView(text,new LinearLayout.LayoutParams(-1, -2));

	}

		class NoListener implements DialogInterface.OnClickListener{
			@Override
			public void onClick(DialogInterface dialog, int which) {
				  dialog.cancel();
	        	 	}
		}

		class YesListener implements DialogInterface.OnClickListener{

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
				//苦肉の策なんかダサすぎ
				if(OverLay.getOvarLay() != null){
					((OverLay)calledACT).finish(CODE.RESULT_COOKIE);
				}else if(FlashPlayer.getACT() != null){
				((FlashPlayer)calledACT).standardFinish();
				}else if(BCPlayer.getBCACT() != null){
				((BCPlayer)calledACT).standardFinish();
				}
				}
			}
		}









