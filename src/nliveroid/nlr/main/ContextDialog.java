package nliveroid.nlr.main;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class ContextDialog extends AlertDialog.Builder{
	private AlertDialog dialog;

	protected ContextDialog(final Context context,final String[] row,final String nickname,int width, final int defaultBgColor, final int defaultFoColor){
		super(context);
		setCustomTitle(null);
		TableLayout parent = new TableLayout(context);
		parent.setStretchAllColumns(true);
		parent.setBackgroundColor(Color.WHITE);

		TableRow[] trs = new TableRow[7];
		TextView[] tvs = new TextView[7];
		String[] prefix = new String[]{"TYPE ","ID   ","CMD   ","TIME ","NG   ","NUM  ",""};
		for(int i = 0; i < 7 ; i++){
		if(row[i] != null){
			tvs[i] = new TextView(context);
			trs[i] = new TableRow(context);
			tvs[i].setText(prefix[i] + row[i]);
			tvs[i].setTextColor(Color.BLACK);
			trs[i].addView(tvs[i]);
			parent.addView(trs[i],new LinearLayout.LayoutParams(-1,-2));
		}
		}
		tvs[6].setWidth(width/3*2);
		parent.setLayoutParams(new TableLayout.LayoutParams(-1, -1));
		parent.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				dialog.cancel();
				new HandleNamePicker((Activity) context,
						new ColorPickerView.OnColorChangedListener() {
							@Override
							public void colorChanged(int color) {
								// 色が選択されるとcolorに値が入る OKボタンで確定するので未使用
								int R = Color.red(color);
								int G = Color.green(color);
								int B = Color.blue(color);
							}
						}, defaultBgColor, defaultFoColor, row[1],nickname,true)
						.show();
			}
		});
		this.setView(parent);
	}
	public void showSelf(){
		dialog = this.create();
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.getWindow().setLayout(-1,-1);
		dialog.show();
	}
}
