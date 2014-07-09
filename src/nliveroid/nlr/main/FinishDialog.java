package nliveroid.nlr.main;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnKeyListener;
import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.widget.LinearLayout;
import android.widget.TextView;

public class FinishDialog extends Builder{
	private Activity parentACT;
	protected FinishDialog(final Context context, final int dialogType,int textColor) {
		super(context);
		parentACT = (Activity) context;
		LinearLayout mainLinear = new LinearLayout(context);
			mainLinear.setBackgroundColor(Color.WHITE);

			TextView text = new TextView(context);
			text.setTextSize(20);
			switch(textColor){
			case 0:
				textColor = Color.GRAY;
				break;
			case 1:
				textColor = Color.BLACK;
				break;
			case 2:
				textColor = Color.rgb(153,255,69);
				break;
			}
			text.setText("終了しますか?");
			text.setTextColor(textColor);
			text.setGravity(Gravity.CENTER);
		switch(dialogType){
		case 0://YES NO のタイプ
			this.setView(mainLinear)
		       .setCancelable(true)
		       .setPositiveButton("Yes", new YesListener())
		       .setNegativeButton("No", new NoListener())
		       .setOnKeyListener(new BackkeyListener())
		       .create();
			break;
		}

		mainLinear.addView(text,new LinearLayout.LayoutParams(-1, -1));

	}

		class NoListener implements OnClickListener{
			@Override
			public void onClick(DialogInterface dialog, int which) {
				  dialog.cancel();
	        	 	}
		}

		class YesListener implements OnClickListener{

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
				((TopTabs)parentACT).finish(true);
				}
			}


		class BackkeyListener implements OnKeyListener{

			private boolean isFirstTime = false;

			@Override
			public boolean onKey(DialogInterface dialog, int keyCode,
					KeyEvent event) {//バックキーをハンドルしてこのダイアログがcreateされた時、1回目をcreate時に検出する
				if(keyCode == KeyEvent.KEYCODE_BACK){
					if(isFirstTime){
		        	  dialog.cancel();
		        	  NLiveRoid app = (NLiveRoid)parentACT.getApplicationContext();
		        	  byte lastTab = (byte) ((TopTabs)parentACT).getTabHost().getCurrentTab();
		        		  if(CommunityTab.getCommunityTab() != null && CommunityTab.getCommunityTab().isPagerViewing())lastTab |= 0x10;
		        		  if(SearchTab.getSearchTab() != null && SearchTab.getSearchTab().isPCSearch())lastTab |= 0x20;
		        		  if(HistoryTab.getHistoryTab() != null && HistoryTab.getHistoryTab().isDBView())lastTab |= 0x40;
//		        		  Log.d("NLiveRoid","SAVETAB " + lastTab);
		        	  	app.setDetailsMapValue("last_tab", String.valueOf(lastTab));
		        	  	String isBackFinish = app.getDetailsMapValue("finish_back");
						if(isBackFinish != null&&Boolean.parseBoolean(isBackFinish)){
						  ((TopTabs)parentACT).finish(true);
						}
					}else{
						isFirstTime = true;
					}
				}
		        	  return false;
			}

		}



		}






