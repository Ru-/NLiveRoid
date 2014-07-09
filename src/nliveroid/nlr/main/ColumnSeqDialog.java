package nliveroid.nlr.main;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableRow;
import android.widget.TextView;

public class ColumnSeqDialog extends DialogPreference {

	private Context context;
	private FrameLayout orangeParent;
	private TextView orangeView;
	private LinearLayout[] lls;
	private Drawable left;
	private Drawable right;
	private int[] column_seq;
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
	public ColumnSeqDialog(Context context, AttributeSet attrs) {
	 super(context, attrs);
	 this.context = context;
	 //Attrの他の値を変える(追加)だけで、attrsの並びが変わる(不明)+nullのわけがないでOK?
	 }


	 @Override
	 protected View onCreateDialogView() {
		super.onCreateDialogView();
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
		if(Details.getPref() != null){//nullだったらこのクラス終わりだけど
			column_seq = new int[7];
		column_seq[0] = Integer.parseInt(Details.getPref().getDetailMapValue("type_seq"));
		column_seq[1] = Integer.parseInt(Details.getPref().getDetailMapValue("id_seq"));
		column_seq[2] = Integer.parseInt(Details.getPref().getDetailMapValue("cmd_seq"));
		column_seq[3] = Integer.parseInt(Details.getPref().getDetailMapValue("time_seq"));
		column_seq[4] = Integer.parseInt(Details.getPref().getDetailMapValue("score_seq"));
		column_seq[5] = Integer.parseInt(Details.getPref().getDetailMapValue("num_seq"));
		column_seq[6] = Integer.parseInt(Details.getPref().getDetailMapValue("comment_seq"));
		}

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

		//densityを取得
		density = ((NLiveRoid)context.getApplicationContext()).getMetrics();
		return parent;
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
									column_seq[toIndex-1] = fromTextID;
								}else{//左に移動
									for(int i = fromIndex; i > toIndex; i--){
											 column_seq[i] = column_seq[i-1];
									}
									//実際突っ込むもの以外の列順を合わせたら
									//fromIndexのcolumn_seqのテキストリソース番をtoIndexに持ってくる
									column_seq[toIndex] = fromTextID;
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
	 @Override
	 protected void onDialogClosed(boolean positiveResult) {
	 super.onDialogClosed(positiveResult);
	 isGetWidth = false;
		 if(positiveResult){
			 if(context != null){//nullだったらこのクラス終わりだけど
					NLiveRoid app = (NLiveRoid)context.getApplicationContext();
					Log.d("NLiveRoid","PASSED");
					app.setDetailsMapValue("type_seq",String.valueOf(column_seq[0]));
					app.setDetailsMapValue("id_seq",String.valueOf(column_seq[1]));
					app.setDetailsMapValue("cmd_seq",String.valueOf(column_seq[2]));
					app.setDetailsMapValue("time_seq",String.valueOf(column_seq[3]));
					app.setDetailsMapValue("score_seq",String.valueOf(column_seq[4]));
					app.setDetailsMapValue("num_seq",String.valueOf(column_seq[5]));
					app.setDetailsMapValue("comment_seq",String.valueOf(column_seq[6]));
				}
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
