package nliveroid.nlr.main;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class ColumnWidthACT extends Activity{
	private LayoutInflater inflater;
	private boolean isPortLayt = true;
	private int[] column_width = new int[7];
	private byte[] column_seq = new byte[7];
	private int viewW;
	private int viewH;
	private View parent;
	private int[] dividePoints = new int[6];
	private LinearLayout[] lls;
	private TextView[] tvs;
	private int rowWidth;
	private float density;
	private float widthUnit;
	private CheckBox[] cbs;
	private TextView[] summarys;

	@Override
	public void onCreate(Bundle bundle){
		super.onCreate(bundle);
		getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		inflater = LayoutInflater.from(this);
		parent = inflater.inflate(R.layout.column_width, null);
		try{
			isPortLayt = this.getIntent().getStringExtra("key").equals("p");
			}catch(NullPointerException e){
				isPortLayt = false;
			}
		final TableRow tr = (TableRow)parent.findViewById(R.id.column_width_row);
		lls = new LinearLayout[7];
		lls[0]  = (LinearLayout)parent.findViewById(R.id.seq0);
		lls[1]  = (LinearLayout)parent.findViewById(R.id.seq1);
		lls[2]  = (LinearLayout)parent.findViewById(R.id.seq2);
		lls[3]  = (LinearLayout)parent.findViewById(R.id.seq3);
		lls[4]  = (LinearLayout)parent.findViewById(R.id.seq4);
		lls[5]  = (LinearLayout)parent.findViewById(R.id.seq5);
		lls[6]  = (LinearLayout)parent.findViewById(R.id.seq6);
		tvs = new TextView[7];
		tvs[0]  = (TextView)parent.findViewById(R.id.seq_tv0);
		tvs[1]  = (TextView)parent.findViewById(R.id.seq_tv1);
		tvs[2]  = (TextView)parent.findViewById(R.id.seq_tv2);
		tvs[3]  = (TextView)parent.findViewById(R.id.seq_tv3);
		tvs[4]  = (TextView)parent.findViewById(R.id.seq_tv4);
		tvs[5]  = (TextView)parent.findViewById(R.id.seq_tv5);
		tvs[6]  = (TextView)parent.findViewById(R.id.seq_tv6);
		cbs = new CheckBox[7];
		cbs[0] = (CheckBox)parent.findViewById(R.id.column_width_cb0);
		cbs[1] = (CheckBox)parent.findViewById(R.id.column_width_cb1);
		cbs[2] = (CheckBox)parent.findViewById(R.id.column_width_cb2);
		cbs[3] = (CheckBox)parent.findViewById(R.id.column_width_cb3);
		cbs[4] = (CheckBox)parent.findViewById(R.id.column_width_cb4);
		cbs[5] = (CheckBox)parent.findViewById(R.id.column_width_cb5);
		cbs[6] = (CheckBox)parent.findViewById(R.id.column_width_cb6);
		summarys = new TextView[7];
		summarys[0] = (TextView)parent.findViewById(R.id.column_width_bt0);
		summarys[1] = (TextView)parent.findViewById(R.id.column_width_bt1);
		summarys[2] = (TextView)parent.findViewById(R.id.column_width_bt2);
		summarys[3] = (TextView)parent.findViewById(R.id.column_width_bt3);
		summarys[4] = (TextView)parent.findViewById(R.id.column_width_bt4);
		summarys[5] = (TextView)parent.findViewById(R.id.column_width_bt5);
		summarys[6] = (TextView)parent.findViewById(R.id.column_width_bt6);

		//設定値を読み込む
		if(isPortLayt){
		column_width[0] = Integer.parseInt(Details.getPref().getDetailMapValue("type_width_p"));
		column_width[1] = Integer.parseInt(Details.getPref().getDetailMapValue("id_width_p"));
		column_width[2] = Integer.parseInt(Details.getPref().getDetailMapValue("command_width_p"));
		column_width[3] = Integer.parseInt(Details.getPref().getDetailMapValue("time_width_p"));
		column_width[4] = Integer.parseInt(Details.getPref().getDetailMapValue("score_width_p"));
		column_width[5] = Integer.parseInt(Details.getPref().getDetailMapValue("num_width_p"));
		column_width[6] = Integer.parseInt(Details.getPref().getDetailMapValue("comment_width_p"));
		}else{
		column_width[0] = Integer.parseInt(Details.getPref().getDetailMapValue("type_width_l"));
		column_width[1] = Integer.parseInt(Details.getPref().getDetailMapValue("id_width_l"));
		column_width[2] = Integer.parseInt(Details.getPref().getDetailMapValue("command_width_l"));
		column_width[3] = Integer.parseInt(Details.getPref().getDetailMapValue("time_width_l"));
		column_width[4] = Integer.parseInt(Details.getPref().getDetailMapValue("score_width_l"));
		column_width[5] = Integer.parseInt(Details.getPref().getDetailMapValue("num_width_l"));
		column_width[6] = Integer.parseInt(Details.getPref().getDetailMapValue("comment_width_l"));
		}

		NLiveRoid app = (NLiveRoid) this.getApplicationContext();
			density = app.getMetrics();
			viewW = app.getViewWidth();
			widthUnit = (float) (viewW/density*0.01);
			viewH = app.getViewHeight();
		//テキストをセットする
		//列順
		column_seq[0] = app.getDetailsMapValue("type_seq")==null? 0:Byte.parseByte(app.getDetailsMapValue("type_seq"));
		column_seq[1] = app.getDetailsMapValue("id_seq")==null? 1:Byte.parseByte(app.getDetailsMapValue("id_seq"));
		column_seq[2] = app.getDetailsMapValue("cmd_seq")==null? 2:Byte.parseByte(app.getDetailsMapValue("cmd_seq"));
		column_seq[3] = app.getDetailsMapValue("time_seq")==null? 3:Byte.parseByte(app.getDetailsMapValue("time_seq"));
		column_seq[4] = app.getDetailsMapValue("score_seq")==null? 4:Byte.parseByte(app.getDetailsMapValue("score_seq"));
		column_seq[5] = app.getDetailsMapValue("num_seq")==null? 5:Byte.parseByte(app.getDetailsMapValue("num_seq"));
		column_seq[6] = app.getDetailsMapValue("comment_seq")==null? 6:Byte.parseByte(app.getDetailsMapValue("comment_seq"));
		for(int i = 0;  i< column_seq.length ;i++){
			tvs[i].setText(URLEnum.ColumnText[column_seq[i]]);
			summarys[i].setText(String.valueOf((int) (column_width[column_seq[i]]/density)));
			final int ii = i;
			cbs[i].setOnCheckedChangeListener(new OnCheckedChangeListener(){
				@Override
				public void onCheckedChanged(CompoundButton buttonView,
						boolean isChecked) {
					if(isChecked){
						lls[ii].setLayoutParams(new TableRow.LayoutParams(30,-1));
						tvs[ii].setWidth(30);
						tr.invalidate();
					}else{
						lls[ii].setLayoutParams(new TableRow.LayoutParams(0,-1));
						tvs[ii].setWidth(0);
						tr.invalidate();
					}
				}
			});
			if(column_width[column_seq[i]]==0){
				cbs[column_seq[i]].setChecked(false);
			}
		}


		setContentView(parent);

	}

	@Override
    public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
			for(int i = 1; i < lls.length; i++){
				//間はcolumnのlength-2しかないので、添え字0と添え字最後の値は入らなくていい
				dividePoints[i-1] = lls[i].getLeft();
			}
			LinearLayout overlayLinear =  (LinearLayout)parent.findViewById(R.id.seq_linear);
			overlayLinear.setOnTouchListener(new SimpleTouchListener());
			//タッチ判定のビューを置く
			FrameLayout frameParent = (FrameLayout)parent.findViewById(R.id.seq_f_layer2);
			frameParent.removeView(overlayLinear);
			//何故か下のほう余る
			frameParent.addView(overlayLinear,new FrameLayout.LayoutParams(-1,tvs[0].getLayoutParams().height));
			//横幅を保存
			rowWidth = overlayLinear.getWidth();
			for(int i = 0; i < column_width.length; i++){
				tvs[i].setWidth((int) (column_width[i]*widthUnit*density));
				cbs[i].setWidth((int) (column_width[i]*widthUnit*density));
				if(!cbs[i].isChecked()){
					lls[i].setLayoutParams(new TableRow.LayoutParams(0,200));
					tvs[i].setLayoutParams(new LinearLayout.LayoutParams(0,200));
					summarys[i].setWidth(30);
				}
			}
	}


	class SimpleTouchListener implements OnTouchListener {
		private int offsetX;
		private int currentRight;
		private int nextLeft;
		private int fromIndex;

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			// タッチリスナは普通のヘッダと最初の青ヘッダのみ
			// getX()とかgetY()とかはリスナにセットされたviewに対しての座標なので相対的に使う
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				int x1 = (int) event.getX();
				//どの境界線を動かすのかを判定
				fromIndex = -1;
				label:
				for(int i= 0; i < dividePoints.length; i++){
					if(dividePoints[i]-30 < x1&&dividePoints[i]+30 >x1){
						for(int j = i; j < cbs.length; j++){//初期化のj=iは右のカラムの線を優先するということ
							if(cbs[j].isChecked()){
						offsetX = x1;
						fromIndex = i;
						currentRight = lls[fromIndex].getRight();
						nextLeft = lls[fromIndex+1].getLeft();
						break label;
							}
						}
					}
				}
				break;
			case MotionEvent.ACTION_MOVE:// 1点目2点目共通
				if(fromIndex > -1){//移動が有効だったら
					int x = (int) event.getX();
						int diffX = offsetX - x;
						// タッチ座標情報を更新する
					int right = currentRight-diffX < 0? 0:currentRight-diffX;
					int left = nextLeft-diffX;
					if(fromIndex == 0){
						if(right <= 0)break;
					}else{
					if(right <= dividePoints[fromIndex-1])break;
					}
					if(fromIndex == dividePoints.length-1){
						if(right >= lls[lls.length-1].getRight())break;
					}else{
					if(right >= dividePoints[fromIndex+1])break;
					}
					lls[fromIndex].layout(lls[fromIndex].getLeft(), lls[fromIndex].getTop(), right, lls[fromIndex].getBottom());
					tvs[fromIndex].layout(0, tvs[fromIndex].getTop(), right, tvs[fromIndex].getBottom());
					lls[fromIndex+1].layout(left, lls[fromIndex+1].getTop(), lls[fromIndex+1].getRight(), lls[fromIndex+1].getBottom());
					tvs[fromIndex+1].layout(0, tvs[fromIndex+1].getTop(), lls[fromIndex+1].getRight(), tvs[fromIndex+1].getBottom());
					}
					break;
			case MotionEvent.ACTION_UP:
					//per100に変換して設定値を更新
				if(fromIndex >= 0){
					for(int i = 0; i < column_width.length; i++){
						column_width[i] = (int) (lls[i].getWidth()/density);
					}
					for(int i = 1; i< lls.length; i++){
						dividePoints[i-1] = lls[i].getLeft();
					}
					for(int i = 0; i < summarys.length; i++){
					summarys[i].setText(String.valueOf((int) (column_width[column_seq[i]])));
					}
				}


				break;
			}

			return true;
		}
	}
}
