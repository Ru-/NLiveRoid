package nliveroid.nlr.main;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ProgramInfoDialog extends Builder{
	private Activity parentACT;
	protected ProgramInfoDialog(final Context context,String message) {
		super(context);
		parentACT = (Activity) context;
		LinearLayout myDialogLayout = new LinearLayout(context);
			myDialogLayout.setBackgroundColor(Color.WHITE);

			TextView text = new TextView(context);
			text.setTextSize(20);
			text.setTextColor(Color.rgb(153,255,69));
			text.setText(message);
			text.setGravity(Gravity.CENTER);
			this.setView(myDialogLayout)
		       .setCancelable(true)
		       .setPositiveButton("OK", new YesListener())
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
				}
			}
		}









