package nliveroid.nlr.main;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class ConfigCommentDialog  extends AlertDialog.Builder {
	private byte autoInterval = 3;
	private AlertDialog me;
	protected ConfigCommentDialog(final CommentPostable postable,final byte isCanLog,final byte[] setting_byte,final boolean[] setting_boolean,int init_mode) {
		super((Context)postable);
		View parent = LayoutInflater.from((Context)postable).inflate(R.layout.config_comment_dialog, null);
		setView(parent);
		autoInterval = setting_byte[32];
		final CheckBox _184 = (CheckBox)parent.findViewById(R.id.config_184);
		Button commandD = (Button) parent.findViewById(R.id.config_command);
		Button commentlog = (Button)parent.findViewById(R.id.config_commentlog);
		Button saveB = (Button)parent.findViewById(R.id.config_savecomment);
		Button seetD = (Button) parent.findViewById(R.id.config_seet);

		final CheckBox visible = (CheckBox)parent.findViewById(R.id.comment_visible);
		final Button position = (Button)parent.findViewById(R.id.comment_position);
//		final CheckBox trans = (CheckBox)parent.findViewById(R.id.comment_trans);

		final CheckBox auto_username = (CheckBox)parent.findViewById(R.id.config_auto_username_check);

		final CheckBox at = (CheckBox)parent.findViewById(R.id.config_at_enable);
		final CheckBox atOverwrite = (CheckBox)parent.findViewById(R.id.config_at_overwrite);
		final Button speechSetB = (Button)parent.findViewById(R.id.speech_setting);

		final Button auto_interval = (Button)parent.findViewById(R.id.auto_update_comment);
		final CheckBox auto_enable = (CheckBox)parent.findViewById(R.id.auto_enable);

		final Button position_xy = (Button)parent.findViewById(R.id.config_comment_position);
		final Button width_height = (Button)parent.findViewById(R.id.config_comment_widthheight);
		final Button dragg = (Button)parent.findViewById(R.id.config_comment_dragg);

		final Button column_seq = (Button)parent.findViewById(R.id.config_columnseq);
		final Button column_width = (Button)parent.findViewById(R.id.config_columnwidth);

		//項目の有効無効を設定する
		switch(init_mode){
		case 0://前面
			break;
		case 1://背面
			break;
		case 2://プレイヤーのみ
			commentlog.setEnabled(false);
			saveB.setEnabled(false);
			seetD.setEnabled(false);
			position_xy.setEnabled(false);
			column_seq.setEnabled(false);
			width_height.setEnabled(false);
			dragg.setEnabled(false);
			break;
		case 3:
			break;
		}

		if(setting_byte[32] < 0){
			auto_interval.setVisibility(View.INVISIBLE);
		}
			auto_username.setChecked(setting_boolean[19]);
			at.setChecked(((HandleNamable)postable).isAt());
			atOverwrite.setChecked(((HandleNamable)postable).isAtOverwrite());

			String anonym = postable.getCmd().getValue(CommandKey.CMD);
			if(anonym.equals("184")){
				_184.setChecked(true);
			}else{
				_184.setChecked(false);
			}
			_184.setOnCheckedChangeListener(new OnCheckedChangeListener(){
				@Override
				public void onCheckedChanged(CompoundButton compoundbutton,
						boolean flag) {//タップした後の値が返ってってくる
					if(flag){
						postable.setCmd(CommandKey.CMD, "184");
					}else{
					postable.setCmd(CommandKey.CMD, "");
					}
				}
			});
		commandD.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				me.cancel();
				postable.showCommandDialog();
			}
		});
		auto_username.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				((HandleNamable)postable).setAutoGetUserName(isChecked);
			}
		});
		visible.setChecked(postable.listVisibleGetSetter(false, false));
		visible.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				me.cancel();
				postable.listVisibleGetSetter(true,isChecked);
			}
		});
		position.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				me.cancel();
				postable.showPositionDialog();
			}
		});

		saveB.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				me.cancel();
			postable.saveComments();
			}
		});


		commentlog.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				me.cancel();
				if(isCanLog == 1){//一般は単にソートのみを提供
					postable.getCommentLog(false);
				}else if(isCanLog == 2){
					postable.getCommentLog(true);
				}
			}
		});

		saveB.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				me.cancel();
			postable.saveComments();
			}
		});



//		Button initial_comments = (Button)parent.findViewById(R.id.init_comment_count);
//		initial_comments.setOnClickListener(new View.OnClickListener(){
//			@Override
//			public void onClick(View v) {
//		me.cancel();
//				postable.openInitCommentPicker();
//			}
//		});

//		Button layer_change = (Button)parent.findViewById(R.id.layer_num);
//		layer_change.setOnClickListener(new View.OnClickListener(){
//			@Override
//			public void onClick(View v) {
//		me.cancel();//コメントのみの場合、これが駄目
//				new AlertDialog.Builder((Context)postable)
//				.setItems(new String[]{"全面","背面","なし"},
//						new DialogInterface.OnClickListener() {
//							public void onClick(
//									DialogInterface dialog,
//									int which) {
//		me.cancel();
//								postable.layerChange(which);
//							}
//				})
//				.create().show();
//			}
//		});

		at.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
					if(!isChecked && atOverwrite != null && atOverwrite.isChecked()){
						atOverwrite.setChecked(false);
					}
				((HandleNamable)postable).setAtEnable(isChecked);
			}
		});
		atOverwrite.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
					if(isChecked && at != null && !at.isChecked()){
						atOverwrite.setChecked(false);
						return;
					}
				((HandleNamable)postable).setAtOverwrite(isChecked);
			}
		});


		auto_interval.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				me.cancel();
				ScrollView sv = new ScrollView((Context)postable);
				final NumberPicker np = new NumberPicker((Context)postable);
				Log.d("NLR",""+autoInterval);
				np.setRange(1, 10);
				if(setting_byte[32] < 0){
					np.setCurrent(2);
				}else{
					if(autoInterval < 1)autoInterval = 3;
				np.setCurrent(autoInterval);
				}
				sv.addView(np,-1,-1);
				new AlertDialog.Builder((Context)postable)
				.setTitle("更新間隔(分)")
				.setView(sv)
				.setPositiveButton("OK",new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						autoInterval = (byte)np.getCurrent();
						postable.setUpdateInterval(autoInterval);
					}
				})
				.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {

					}
				})
				.create().show();
			}

		});

		//チェックは排他でNumberPickerの方を参照するので後に書く
		if(setting_byte[32] > 0){
			auto_enable.setChecked(true);
		}else{
			auto_enable.setChecked(false);
		}
		auto_enable.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if(isChecked){
					auto_interval.setVisibility(View.VISIBLE);
					postable.setUpdateInterval(autoInterval);
				}else{
					auto_interval.setVisibility(View.INVISIBLE);
					postable.setUpdateInterval((byte)-1);
				}
			}
		});

		speechSetB.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				me.cancel();
				if(postable.getCommentTable() != null){
				new SpeechSettingDialog(postable, postable.getCommentTable()).showSelf();
				}else{
					MyToast.customToastShow((Context)postable, "コメントサーバとの接続が確認できませんでした");
				}
			}
		});

		seetD.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				me.cancel();
				postable.showSeetDialog();
			}
		});


		position_xy.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				me.cancel();
				new AlertDialog.Builder((Context)postable)
				.setItems(new CharSequence[]{"X","Y"}, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
							if(which == 0){//X
								if(me.getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
									new PositionXY(postable,true,true, setting_byte[8]).showSelf();
								}else{
									new PositionXY(postable,false,true, setting_byte[19]).showSelf();
								}
							}else{//Y
								if(me.getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
									new PositionXY(postable,true,false,setting_byte[9]).showSelf();
								}else{
									new PositionXY(postable,false,false,setting_byte[20]).showSelf();
								}
							}
						}
					})
				.create().show();
			}
		});
		width_height.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				me.cancel();
				new AlertDialog.Builder((Context)postable)
				.setItems(new CharSequence[]{"横幅","縦幅"}, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
							if(which == 0){//横幅
								if(me.getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
									new WidthHeight(postable,true,true, setting_byte[38]).showSelf();
								}else{
									new WidthHeight(postable,false,true, setting_byte[39]).showSelf();
								}
							}else{//縦幅
								if(me.getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
									new WidthHeight(postable,true,false, setting_byte[10]).showSelf();
								}else{
									new WidthHeight(postable,false,false, setting_byte[21]).showSelf();
								}
							}
						}
					}).create().show();
				}
		});
		dragg.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				if(me.getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
					new DraggDialog(postable,true,setting_boolean[8],setting_boolean[9]).showSelf();
				}else{
					new DraggDialog(postable,false,setting_boolean[10],setting_boolean[11]).showSelf();
				}
			}
		});
		column_seq.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				me.cancel();
				new ColumnSeqDialog(postable).showSelf();
			}
		});
		column_width.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				new AlertDialog.Builder((Context)postable)
				.setItems(new CharSequence[]{"TYPE","ID","CMD","TIME","SCORE","NUM","COMMENT"}, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						me.cancel();
								if(me.getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
									new ColumnWidthDialog(postable,true,which, setting_byte).showSelf();
								}else{
									new ColumnWidthDialog(postable,false,which+11, setting_byte).showSelf();
								}
						}
					}).create().show();
			}
		});
	}

	public void showSelf(){
		this.create();
		me = this.show();
	}

	public boolean isShowing() {
		return me == null? false:me.isShowing();
	}

	public void cancel_() {
		me.cancel();
	}

	class PositionXY extends AlertDialog.Builder{
		private AlertDialog innerMe;
		private boolean isPortLayt;
		private CheckBox cb = null;
		public PositionXY(final CommentPostable postable,boolean isportLayt,final boolean isX,int param) {
			super((Context)postable);
			this.isPortLayt = isportLayt;
			 ScrollView sv = new ScrollView((Context)postable);
			 TableLayout baseTableLayout = new TableLayout((Context)postable);
			 baseTableLayout.setColumnStretchable(0, true);
			 TableRow tr0 = new TableRow((Context)postable);
			 TableRow tr1 = new TableRow((Context)postable);
			 TableRow tr2 = new TableRow((Context)postable);
			 final NumberPicker np = new NumberPicker((Context)postable);
			 np.setClickable(true);
			 np.setLongClickable(true);
			 TextView tv = new TextView((Context)postable);
			 tv.setGravity(Gravity.CENTER);
			 int defaultNum = param;
			 if(isX){
			 np.setRange(0, 99);
			 tv.setText("ヘッダーの左上の位置を決定\n画面横幅全体が0～100として\n-99～+99で設定");
			 cb = new CheckBox((Context)postable);
			 cb.setText("マイナス");
			 if(defaultNum<0){
				 defaultNum = -defaultNum;
				 cb.setChecked(true);
			 }
			 tr0.addView(tv);
			 tr1.addView(cb);
			 tr2.addView(np);
			 baseTableLayout.addView(tr0,new LinearLayout.LayoutParams(-1,-2));
			 baseTableLayout.addView(tr1,new LinearLayout.LayoutParams(-1,-2));
			 baseTableLayout.addView(tr2,new LinearLayout.LayoutParams(-1,-2));
			 }else{
				 np.setRange(0, 100);
				 tv.setText("上端0～下端100");
				 if(defaultNum<0||defaultNum>100){
					 defaultNum = 50;
				 }
				 tr0.addView(tv);
				 tr1.addView(np);
				 baseTableLayout.addView(tr0,new LinearLayout.LayoutParams(-1,-2));
				 baseTableLayout.addView(tr1,new LinearLayout.LayoutParams(-1,-2));
			 }
			 np.setCurrent(defaultNum);
			 sv.addView(baseTableLayout,-1,-1);
				this.setTitle(isPortLayt? "縦時":"横時");
			 setPositiveButton("OK", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if(isX){
						if(cb == null)MyToast.customToastShow(getContext(), "設定に失敗しました:cb null");
						if(isPortLayt){//X縦
							postable.settingChange(0, cb.isChecked()? (byte)-np.getCurrent():(byte)np.getCurrent(),null);
						}else{//X横
							postable.settingChange(1, cb.isChecked()? (byte)-np.getCurrent():(byte)np.getCurrent(),null);
						}
					}else{
						if(isPortLayt){//Y縦
							postable.settingChange(2, (byte)np.getCurrent(),null);
						}else{//Y横
							postable.settingChange(3, (byte)np.getCurrent(),null);
						}
					}
				}
			});
			 setView(sv);
		}
		public void showSelf(){
			innerMe = this.create();
			innerMe.show();
		}
	}

	class WidthHeight extends AlertDialog.Builder{
		private AlertDialog innerMe;
		private boolean isPortLayt;
		private CheckBox cb = null;
		public WidthHeight(final CommentPostable postable,boolean isportLayt,final boolean isWidth,int param) {
			super((Context)postable);
			this.isPortLayt = isportLayt;
			 ScrollView sv = new ScrollView((Context)postable);
			 TableLayout baseTableLayout = new TableLayout((Context)postable);
			 baseTableLayout.setColumnStretchable(0, true);
			 TableRow tr0 = new TableRow((Context)postable);
			 TableRow tr1 = new TableRow((Context)postable);
			 TableRow tr2 = new TableRow((Context)postable);
			 final NumberPicker np = new NumberPicker((Context)postable);
			 np.setClickable(true);
			 np.setLongClickable(true);
			 TextView tv = new TextView((Context)postable);
			 tv.setGravity(Gravity.CENTER);
			 int defaultNum = param;
			 if(isWidth){
				 np.setRange(0, 100);
				 tv.setText("画面全体の横幅を100として\n0～100で設定");
				 if(defaultNum<0||defaultNum>100){
					 defaultNum = 100;
				 }
				 tr0.addView(tv);
				 tr1.addView(np);
				 baseTableLayout.addView(tr0,new LinearLayout.LayoutParams(-1,-2));
				 baseTableLayout.addView(tr1,new LinearLayout.LayoutParams(-1,-2));
			 }else{
				 np.setRange(0, 100);
				 tv.setText("画面全体の高さを100として\n0～100で設定");
				 cb = new CheckBox((Context)postable);
				 cb.setText("コメント追加の方向上");
				 if(defaultNum<0){
					 defaultNum = -defaultNum;
					 cb.setChecked(true);
				 }
				 tr0.addView(tv);
				 tr1.addView(cb);
				 tr2.addView(np);
				 baseTableLayout.addView(tr0,new LinearLayout.LayoutParams(-1,-2));
				 baseTableLayout.addView(tr1,new LinearLayout.LayoutParams(-1,-2));
				 baseTableLayout.addView(tr2,new LinearLayout.LayoutParams(-1,-2));
			 }
			 np.setCurrent(defaultNum);
			 sv.addView(baseTableLayout,-1,-1);
				this.setTitle(isPortLayt? "縦時":"横時");
			 setPositiveButton("OK", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if(isWidth){
						if(isPortLayt){//X縦
							postable.settingChange(4, (byte)np.getCurrent(),null);
						}else{//X横
							postable.settingChange(5, (byte)np.getCurrent(),null);
						}
					}else{
						if(cb == null)MyToast.customToastShow(getContext(), "設定に失敗しました:cb null2");
						if(isPortLayt){//Y縦
							postable.settingChange(6, cb.isChecked()? (byte)-np.getCurrent():(byte)np.getCurrent(),null);
						}else{//Y横
							postable.settingChange(7, cb.isChecked()? (byte)-np.getCurrent():(byte)np.getCurrent(),null);
						}
					}
				}
			});
			 setView(sv);
		}
		public void showSelf(){
			innerMe = this.create();
			innerMe.show();
		}
	}
	class DraggDialog extends AlertDialog.Builder{
		private AlertDialog innerMe;
		private boolean isPortLayt;
		private CheckBox cb0 = null;
		private CheckBox cb1 = null;
		public DraggDialog(final CommentPostable postable,boolean isportLayt,final boolean xDragg,boolean yDragg) {
			super((Context)postable);
			this.isPortLayt = isportLayt;
			 ScrollView sv = new ScrollView((Context)postable);
			 TableLayout baseTableLayout = new TableLayout((Context)postable);
			 baseTableLayout.setColumnStretchable(0, true);
			 TableRow tr0 = new TableRow((Context)postable);
			 TableRow tr1 = new TableRow((Context)postable);
			 cb0 = new CheckBox((Context)postable);
			 cb0.setText("Xドラッグ");
			 cb0.setChecked(xDragg);
			 cb1 = new CheckBox((Context)postable);
			 cb1.setText("Yドラッグ");
			 cb1.setChecked(yDragg);
			 tr0.addView(cb0);
			 tr1.addView(cb1);
			 baseTableLayout.addView(tr0,new LinearLayout.LayoutParams(-1,-2));
			 baseTableLayout.addView(tr1,new LinearLayout.LayoutParams(-1,-2));
			 sv.addView(baseTableLayout,-1,-1);
			 setTitle(isPortLayt? "縦時":"横時");
			 setPositiveButton("OK", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					//XXXX この4ビット目が0=portlayt 1=landscape 下位2ビットで xy
					if(cb0 == null || cb1 == null)MyToast.customToastShow(getContext(), "設定に失敗しました:cb null dragg");
						int result =  (isPortLayt? 0x00:0x04);
						Log.d("RESULT ---- ", " " + result);
						result = cb0.isChecked()? result | 0x02:result | 0x00;
						Log.d("RESULT ---- ", " " + result);
						result = cb1.isChecked()? result | 0x01:result | 0x00;
						Log.d("RESULT ---- ", " " + result);
							postable.settingChange(8, (byte)result,null);

				}
			});
			 setView(sv);
		}
		public void showSelf(){
			innerMe = this.create();
			innerMe.show();
		}
	}
	class ColumnSeqDialog extends AlertDialog.Builder{
		private AlertDialog innerMe;
		private Context context;
		private FrameLayout orangeParent;
		private TextView orangeView;
		private LinearLayout[] lls;
		private Drawable left;
		private Drawable right;
		private byte[] column_seq;
		final private String[] seq_str = new String[]{"TYPE","ID","CMD","TIME","SCORE","NUM","COMMENT"};
		private int[] halfArray = new int[14];
		private boolean isGetWidth = false;

		private int toHalfIndex;
		private int viewW;
		private View parent;
		private TextView dummy;
		private TextView[] tvs;
		private float density = 1.5F;
		private int halfWidth = 0;
		public ColumnSeqDialog(final CommentPostable postable) {
			super((Context)postable);
			context = (Context)postable;
			parent = LayoutInflater.from(context).inflate(R.layout.column_seq, null);
			dummy = new DummyView(context);
			((TableRow)parent.findViewById(R.id.dummy_root)).addView(dummy,new TableRow.LayoutParams(-1,-2));

			orangeParent = (FrameLayout)parent.findViewById(R.id.seq_layer2);
			orangeView = new TextView(context);
			orangeView.setVisibility(View.INVISIBLE);
			orangeView.setBackgroundColor(Color.parseColor("#9966ffff"));
			orangeView.setOnTouchListener(new OnTouchListener(){
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					if(orangeParent != null){
						orangeParent.removeAllViews();
					}
					return false;
				}
			});
			lls = new LinearLayout[7];
			lls[0]  = (LinearLayout)parent.findViewById(R.id.seq0);
			lls[1]  = (LinearLayout)parent.findViewById(R.id.seq1);
			lls[2]  = (LinearLayout)parent.findViewById(R.id.seq2);
			lls[3]  = (LinearLayout)parent.findViewById(R.id.seq3);
			lls[4]  = (LinearLayout)parent.findViewById(R.id.seq4);
			lls[5]  = (LinearLayout)parent.findViewById(R.id.seq5);
			lls[6]  = (LinearLayout)parent.findViewById(R.id.seq6);
			TableRow tr = ((TableRow)parent.findViewById(R.id.seq_tablerow));
			tr.setFocusable(true);
			tr.setOnTouchListener(new SimpleTouchListener());

			left = context.getResources().getDrawable(R.drawable.leftshape);
			right = context.getResources().getDrawable(R.drawable.rightshape);

			//設定値を読み込む
				column_seq = postable.getColumnSeq();
				if(column_seq == null)MyToast.customToastShow((Context)postable,"列順の設定値の取得に失敗");

			tvs = new TextView[7];
			tvs[0]  = (TextView)parent.findViewById(R.id.seq_tv0);
			tvs[1]  = (TextView)parent.findViewById(R.id.seq_tv1);
			tvs[2]  = (TextView)parent.findViewById(R.id.seq_tv2);
			tvs[3]  = (TextView)parent.findViewById(R.id.seq_tv3);
			tvs[4]  = (TextView)parent.findViewById(R.id.seq_tv4);
			tvs[5]  = (TextView)parent.findViewById(R.id.seq_tv5);
			tvs[6]  = (TextView)parent.findViewById(R.id.seq_tv6);
			for(int i = 0; i < column_seq.length; i++){
			tvs[i].setText(seq_str[column_seq[i]]);
			}

			ScrollView sv = (ScrollView)parent.findViewById(R.id.seq_scrollview);
			sv.setFocusable(true);
			sv.setOnTouchListener(new ClearListener());

			 setView(sv);
			//densityを取得
			density = ((NLiveRoid)context.getApplicationContext()).getMetrics();
			 setPositiveButton("OK", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					 isGetWidth = false;
					 postable.settingChange(9, (byte)0, column_seq);
				}
			});
			 this.setOnCancelListener(new OnCancelListener(){
				@Override
				public void onCancel(DialogInterface dialog) {
					 isGetWidth = false;
				}
			 });
		}
		public void showSelf(){
			innerMe = this.create();
			innerMe.show();
		}

		class SimpleTouchListener implements OnTouchListener {
			private int offsetX;
			private int currentX;
			private int fromIndex;
			private int toIndex;

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(isGetWidth){
				// タッチリスナは普通のヘッダと最初の青ヘッダのみ
				// getX()とかgetY()とかはリスナにセットされたviewに対しての座標なので相対的に使う
				if(!isGetWidth){
					Log.d("NLiveRoid","DID'T GET WIDTH");
					dummy.invalidate();
					return false;
				}
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					fromIndex = 0;
					toIndex = -1;
					int halfIndex = -1;

						int x1 = (int) (event.getX()/density);
						offsetX = x1;
						Rect rect1 = new Rect();
						v.getGlobalVisibleRect(rect1);
							currentX =  rect1.left == 0 ?  -(viewW - rect1.right)
									: rect1.left;

					//どのカラムを動かすのかを判定
					for(int i= 0; i < halfArray.length; i++){//dividePointsだから7がありえる
						if(halfArray[i] < x1){
							halfIndex = i;
						}else{//インデックス確定
//							Log.d("HALF",""+halfIndex);
							fromIndex = halfIndex/2+halfIndex%2;
							break;
						}
					}
					if(fromIndex >= 0){

						//レングスを割り出してパディングを決める
						((ViewGroup) orangeParent).removeAllViews();
						((ViewGroup) orangeParent).addView(orangeView,
								new LinearLayout.LayoutParams(lls[0].getWidth(), lls[0].getHeight()));
						orangeParent.setPadding( lls[fromIndex].getLeft(), 4, 0, 0);
					orangeView.setText(seq_str[column_seq[fromIndex]]);
						// 上に乗せたViewを見えるようにする
						orangeView.setVisibility(View.VISIBLE);
					}
					break;
				case MotionEvent.ACTION_MOVE:
					if(fromIndex >=0){
//						Log.d("FROM ",""+fromIndex + "   ");
						toIndex = 0;
					for(LinearLayout ll:lls){//色をクリア
						ll.setBackgroundDrawable(null);
						ll.setBackgroundColor(Color.WHITE);
					}
						int x = (int) (event.getX());
						Rect rect11 = new Rect();
						v.getGlobalVisibleRect(rect11);
							int diffX = offsetX - x;
							offsetX = x;
							// 現在座標
							currentX -= diffX;
							// タッチ座標情報を更新する
						// 上のレイヤーに乗せたViewの描画内容を更新する
							orangeParent.setPadding(lls[fromIndex].getLeft()+currentX-halfArray[fromIndex]*2, 4, 0, 0);

							for(int i= 0; i < halfArray.length; i++){//dividePointsだから7がありえる

								if(halfArray[i] < lls[fromIndex].getLeft()+((int)currentX/density)-(halfArray[fromIndex]*2)){
									toHalfIndex = i;
								}else{//インデックス確定
//									Log.d("HALF",""+toHalfIndex);
									toIndex = toHalfIndex/2+toHalfIndex%2;
									break;
								}
							}

							if(toIndex < 0||toHalfIndex < 0)return false;
							if(toHalfIndex == 13){//一番右の時(toIndexとしては0になる)
								lls[lls.length-1].setBackgroundDrawable(right);
							}else if(toHalfIndex == 0){//一番左の時
								lls[0].setBackgroundDrawable(left);
							}else{//見た目を優先してインデックスがうまくいかなくなったのでこの時11か12で一番右とする
								lls[toIndex-1].setBackgroundDrawable(right);
								lls[toIndex].setBackgroundDrawable(left);
							}
					}
						break;
				case MotionEvent.ACTION_UP:
					for(LinearLayout ll:lls){//色をクリア
						ll.setBackgroundDrawable(null);
						ll.setBackgroundColor(Color.WHITE);
					}
						// 上のレイヤーから乗せたViewを除去する
					if(orangeParent != null){
						orangeParent.removeAllViews();
					}
					if(fromIndex >= 0 && toIndex >= 0&&toHalfIndex >= 0){//UPが有効なら
						if((fromIndex == toIndex)&&toHalfIndex !=13//同一で一番左から一番右ではない
								||fromIndex+1==toIndex//その要素の右に移動しようとした
								||((fromIndex == 6&&toIndex == 0&&toHalfIndex != 13//一番右から一番左以外でtoが0
								&&(fromIndex == 6&&toIndex == 0&&toHalfIndex != 0)))//一番右から一番左以外でtoが0
								)return false;//移動無効
								if(toIndex == 0 && toHalfIndex == 13){//一番右に移動した
									toIndex = 7;
								}
								Log.d("NLiveRoid","Column sequence " + fromIndex + "  " + toIndex +"  " + toHalfIndex);
						//ここまでのifで下準備おｋ
								//移動する元を取っておく
								int fromTextID = column_seq[fromIndex];
								if(fromIndex < toIndex){//右に移動
									for(int i = fromIndex; i < toIndex-1; i++){
											 column_seq[i] = column_seq[i+1];
									}
									//実際突っ込むもの以外の列順を合わせたら
									//fromIndexのcolumn_seqのテキストリソース番をtoIndex-1に持ってくる
									column_seq[toIndex-1] = (byte) fromTextID;
								}else{//左に移動
									for(int i = fromIndex; i > toIndex; i--){
											 column_seq[i] = column_seq[i-1];
									}
									//実際突っ込むもの以外の列順を合わせたら
									//fromIndexのcolumn_seqのテキストリソース番をtoIndexに持ってくる
									column_seq[toIndex] = (byte) fromTextID;
								}

						//表示しなおす
						for(int i = 0; i < column_seq.length; i++){
							tvs[i].setText(seq_str[column_seq[i]]);
						}
					}
					fromIndex = -1;
					toIndex = -1;
					toHalfIndex = -1;
					break;
					}
				}
				return true;
			}
		}

		class ClearListener implements OnTouchListener{

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				for(LinearLayout ll:lls){//色をクリア
					if(ll != null){
					ll.setBackgroundDrawable(null);
					ll.setBackgroundColor(Color.WHITE);
					}
				}

					// 上のレイヤーから乗せたViewを除去する
				if(orangeParent != null){
					orangeParent.removeAllViews();
				}
				return true;
			}

		}

	 //可視領域を取得する為だけのビュー
	 class DummyView extends TextView{
		public DummyView(Context context) {
			super(context);
		}
		 @Override
		 public void onDraw(Canvas canvas){
			 super.onDraw(canvas);
					 if(!isGetWidth){
						 viewW = (int) (((TableRow)parent.findViewById(R.id.dummy_root)).getWidth()/density);
						 if(viewW == 0)return;//まだレイアウトされていなければ抜ける
							//横幅を均等にする
							int width = viewW/column_seq.length;
							android.widget.TableRow.LayoutParams ll = new TableRow.LayoutParams(width,-1);
							for(int i = 0; i < column_seq.length;i++){
								lls[i].setLayoutParams(ll);
								ll.setMargins(2, 0, 2, 0);//マージンは内側の話なので加味しなくて良い
										tvs[column_seq[i]].setWidth(width);
							}
							halfWidth = width/2;
							halfArray[0] = halfWidth;
							for(int i = 1; i < column_seq.length*2; i++){
								halfArray[i] = halfArray[i-1]+halfWidth;
								Log.d("W",""+halfArray[i-1]);
							}

						 isGetWidth = true;
					 }
		 	}
	 	}
	}

	class ColumnWidthDialog extends AlertDialog.Builder{
		private AlertDialog innerMe;
		private LinkedHashMap<String,Byte> wlist;
		private String key;
		public ColumnWidthDialog(final CommentPostable postable, final boolean isPortLayt,
				final int whichIndex, final byte[] setting_byte) {
			super((Context)postable);
			 ScrollView sv = new ScrollView((Context)postable);
			 TableLayout baseTableLayout = new TableLayout((Context)postable);
			 baseTableLayout.setColumnStretchable(0, true);
			 TableRow tr0 = new TableRow((Context)postable);
			 TableRow tr1 = new TableRow((Context)postable);
			 final NumberPicker np = new NumberPicker((Context)postable);
			 np.setClickable(true);
			 np.setLongClickable(true);
			 TextView tv = new TextView((Context)postable);
			 tv.setGravity(Gravity.CENTER);
			 np.setRange(0, 100);
			 tv.setText("横幅全体が0～100\n設定した列以外自動調整");
			 tr0.addView(tv);
			 tr1.addView(np);
			 baseTableLayout.addView(tr0,new LinearLayout.LayoutParams(-1,-2));
			 baseTableLayout.addView(tr1,new LinearLayout.LayoutParams(-1,-2));
			 sv.addView(baseTableLayout,-1,-1);
				this.setTitle(isPortLayt? "縦時":"横時");

				wlist = new LinkedHashMap<String,Byte>();

				 byte defaultNum = 0;
				 if(isPortLayt){
					 	wlist.put("type_width_p",setting_byte[0]);
						wlist.put("id_width_p",setting_byte[1]);
						wlist.put("command_width_p",setting_byte[2]);
						wlist.put("time_width_p",setting_byte[3]);
						wlist.put("score_width_p",setting_byte[4]);
						wlist.put("num_width_p",setting_byte[5]);
						wlist.put("comment_width_p",setting_byte[6]);
					 defaultNum = setting_byte[whichIndex];
					 Iterator<String> it = wlist.keySet().iterator();
					 String temp = "";
					 for(int i = 0; i < 7; i++){
						 temp = it.next();
						 if(whichIndex == i){
							 key = temp;
						 }
					 }
				 }else{
					 wlist.put("type_width_l",setting_byte[11]);
						wlist.put("id_width_l",setting_byte[12]);
						wlist.put("command_width_l",setting_byte[13]);
						wlist.put("time_width_l",setting_byte[14]);
						wlist.put("score_width_l",setting_byte[15]);
						wlist.put("num_width_l",setting_byte[16]);
						wlist.put("comment_width_l",setting_byte[17]);
					 defaultNum = setting_byte[whichIndex];//コンストラクタに受け取る時に+11してるからOK
					 Iterator<String> it = wlist.keySet().iterator();
					 String temp = "";
					 for(int i = 11; i < 18; i++){
						 temp = it.next();
						 if(whichIndex == i){
							 key = temp;
						 }
					 }
				 }
				 np.setCurrent(defaultNum);
			 setPositiveButton("OK", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					 int limitCount = 10;
					 //値が増えたらその他の列に均等に分散する
						 //自分自身の値を更新
						 wlist.put(key, (byte) np.getCurrent());
						 double diff = (100-np.getCurrent());
//						 Log.d("log","DIFF A " + diff);


						 int loop = 0;
						 int ammount = 0;
						 while(loop < limitCount){
						 wlist = calculateWidth(wlist,diff);
						 //足して100前後10になるまで繰り返す
						 Iterator<String> it = wlist.keySet().iterator();
						 while(it.hasNext()){
							 ammount += wlist.get(it.next());
						 }
						 if(ammount>=95&&ammount<=105){
							 break;
						 }
						 loop ++;
						 }
						 //100に満たなかったらあまりを全てcommentに( 色を変えたときに右が白くなるのを防ぐ)
						 if(ammount<100){
							 int reast = 100-ammount;
							 if(isPortLayt){
							 wlist.put("comment_width_p",(byte) (wlist.get("comment_width_p")+reast));
							 }else{
								 wlist.put("comment_width_l",(byte) (wlist.get("comment_width_l")+reast));
							 }
						 }
						 //設定値を保存
						 Iterator<String> it = wlist.keySet().iterator();
						 String listkey = "";

						 if(isPortLayt){
							 for(int i = 0; i < 7; i++){
								 listkey = it.next();
								 setting_byte[i] = wlist.get(listkey);
							 }
						 }else{
							 for(int i = 11; i < 18; i++){
								 listkey = it.next();
								 setting_byte[i] = wlist.get(listkey);
							 }
						 }
							postable.settingChange(10, (byte)-1,setting_byte);
					}
			});
			 setView(sv);
		}
		public void showSelf(){
			innerMe = this.create();
			innerMe.show();
		}

		private LinkedHashMap<String, Byte> calculateWidth(LinkedHashMap<String, Byte> wl,double diff){
			 Log.d("NLiveRoid","Befor " + diff);

			 String nextKey = "";
			 Iterator<String> it  = wl.keySet().iterator();
			 while(it.hasNext()){
				 nextKey = it.next();
				 if(!nextKey.equals(key)){//このキー以外の要素に設定値と100との差から引いたあまりを求める
					  diff -= wl.get(nextKey);
				 }
			 }
			 diff /= 5;//あまりとして足す値を平均化
//			 Log.d("log","DIFF " + diff);

			 it  = wl.keySet().iterator();
			 while(it.hasNext()){
				 nextKey = it.next();
				 if(!nextKey.equals(key)){//このキー以外の要素に均等に足していく
					 wl.put(nextKey, (byte) (wl.get(nextKey)+diff));
					 if(wl.get(nextKey)<0){
//						 Log.d("log","NEGATIVE " + nextKey + "  val " +wlist.get(nextKey));
						 //マイナスになる時は0にしてマイナス値を平均化して足していって繰り返す
						wl =  negativeRecursive(wl,wl.keySet().iterator(),nextKey,wl.get(nextKey)/5);
					 }
				 }
//				 Log.d("log","KEY " + nextKey + "  value "  + wlist.get(nextKey));
			 }
			return wl;

		 }
		 private LinkedHashMap<String, Byte> negativeRecursive(LinkedHashMap<String, Byte> wlist, Iterator<String> it,String myKey,double negative){
			 String nextKey = "";
				wlist.put(myKey,(byte) 0);
			 			while(it.hasNext()){
			 				nextKey = it.next();
//			 				Log.d("log"," NEG " + nextKey + " val " + wlist.get(nextKey) + " negative " + negative);
			 				if(!nextKey.equals(myKey)&&!nextKey.equals(key)){
			 					wlist.put(nextKey,(byte) (wlist.get(nextKey)+negative));

			 				if(wlist.get(nextKey)<0){
//			 					Log.d("log"," TIVE " + nextKey + " val " + wlist.get(nextKey) + " negative " + negative);

			 				wlist = negativeRecursive(wlist,wlist.keySet().iterator(),nextKey,wlist.get(nextKey)/5);

			 				}
			 				}
			 			}
			 return wlist;
		 }

	}
}
