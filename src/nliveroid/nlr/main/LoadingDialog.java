package nliveroid.nlr.main;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class LoadingDialog extends Builder{
	private Activity parentACT;
	private  boolean isFirstTimeFlug = false;
	private int flug;
	private ErrorCode error;
	protected LoadingDialog(final Context context) {
		super(context);
		error = ((NLiveRoid)context.getApplicationContext()).getError();
		parentACT = (Activity) context;
		TableLayout myDialogLayout = new TableLayout(context);
			myDialogLayout.setBackgroundColor(Color.parseColor("#4abae8"));
			myDialogLayout.setColumnStretchable(0, true);

			TextView text = new TextView(context);
			text.setTextSize(25);
			text.setTextColor(Color.BLACK);
			 text.setPadding(10, 50, 10, 50);
			 text.setText("Loading info..");
			 ProgressBar pb = new ProgressBar(context);

			setView(myDialogLayout)
			       .setCancelable(true)
			       .create();
			TableRow tr0 = new TableRow(context);
			tr0.addView(text);
			TableRow tr1 = new TableRow(context);
			LinearLayout ll = new LinearLayout(context);
			ll.setGravity(Gravity.CENTER);
			ll.addView(pb,new LinearLayout.LayoutParams(-1,-1));
			tr1.addView(ll);
		myDialogLayout.addView(tr0,new TableLayout.LayoutParams(-1, -1));
		myDialogLayout.addView(tr1,new TableLayout.LayoutParams(-1, -1));
		new CancelClass();
	}

	class CancelClass implements DialogInterface{

		@Override
		public void cancel() {
			// TODO 自動生成されたメソッド・スタブ

		}

		@Override
		public void dismiss() {
			// TODO 自動生成されたメソッド・スタブ

		}

	}


		}






