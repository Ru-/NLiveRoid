package nliveroid.nlr.main;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Shader.TileMode;
import android.graphics.SweepGradient;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class ColorPickerView extends View {
	 public interface OnColorChangedListener {
	        void colorChanged(int color);
	    }
    private Paint mPaint;
	private Paint svPaint;
	private Paint vvPaint;
    private final int[] mCercleColors;
    
    private ColorPickable parentDialog;

    private OnColorChangedListener mListener;

    private boolean mTrackingCenter;
    private boolean mHighlightCenter;
	private int normalColor;
	private Paint mCenterPaint;
	private boolean isBg;


    private static final int CENTER_X = 100;
    private static final int CENTER_Y = 100;
    private static final int CENTER_RADIUS = 32;

    ColorPickerView(Activity c,ColorPickable pickable, OnColorChangedListener l, int color,boolean isBg) {
        super(c);
        mListener = l;
        this.parentDialog = pickable;
        this.isBg = isBg;
      //円形のピッカー部分
        mCercleColors = new int[] {
            0xFFFF0000, 0xFFFF00FF, 0xFF0000FF, 0xFF00FFFF, 0xFF00FF00,
            0xFFFFFF00, 0xFFFF0000
        };
        Shader cercleShader = new SweepGradient(0, 0, mCercleColors, null);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setShader(cercleShader);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(32);

        //彩度部分
        float[] shsv = new float[3];
        Color.RGBToHSV(Color.red(color), Color.green(color), Color.blue(color), shsv);
        shsv[1] = 1;
        shsv[2] = 1;
        int[] sBarColor = new int[] {
                0xFFFFFFFF,Color.HSVToColor(shsv)
            };
        Shader sbShader = new LinearGradient(-CENTER_X, 30, CENTER_X,30,sBarColor,null,TileMode.CLAMP);
        svPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        svPaint.setShader(sbShader);
        svPaint.setStyle(Paint.Style.STROKE);
        svPaint.setStrokeWidth(30);

        //明度部分
        float[] vhsv = new float[3];
        Color.RGBToHSV(Color.red(color), Color.green(color),Color.blue(color), vhsv);
        vhsv[1] = 0;
        vhsv[2] = 0;
        int[] vBarColor = new int[]{
        	color,Color.HSVToColor(vhsv)
        };
        Shader vbShader = new LinearGradient(-CENTER_X, 85, CENTER_X,85,vBarColor,null,TileMode.CLAMP);
        vvPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        vvPaint.setShader(vbShader);
        vvPaint.setStyle(Paint.Style.STROKE);
        vvPaint.setStrokeWidth(30);

        //真ん中の色部分
        //色が決定する所で、モノクロにしない値を保存しておく
		normalColor = color;
        mCenterPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCenterPaint.setColor(color);
        mCenterPaint.setStrokeWidth(5);

    }


    @Override
    protected void onDraw(Canvas canvas) {
    	//キャンバス自体の真ん中を合わせる
        float r = CENTER_X - mPaint.getStrokeWidth()*0.5f;
        canvas.translate(CENTER_X, CENTER_X);
        //drawOvalで楕円を描画
        canvas.drawOval(new RectF(-r, -r, r, r), mPaint);


        //真ん中の描画
        canvas.drawCircle(0, 0, CENTER_RADIUS, mCenterPaint);
        //彩度明度の描画
        canvas.drawLine(-CENTER_X, CENTER_Y+30 ,CENTER_X , CENTER_Y+30 , svPaint);
        //明度の描画
        canvas.drawLine(-CENTER_X, CENTER_Y+85 ,CENTER_X , CENTER_Y+85 , vvPaint);
        if (mTrackingCenter) {
            int c = mCenterPaint.getColor();
            mCenterPaint.setStyle(Paint.Style.STROKE);

            if (mHighlightCenter) {
            	mCenterPaint.setAlpha(0xFF);
            } else {
            	mCenterPaint.setAlpha(0x80);
            }
            canvas.drawCircle(0, 0,
                              CENTER_RADIUS + mCenterPaint.getStrokeWidth(),
                              mCenterPaint);

            mCenterPaint.setStyle(Paint.Style.FILL);
            mCenterPaint.setColor(c);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(CENTER_X*2, CENTER_Y*2+100);
    }


    private int floatToByte(float x) {
        int n = java.lang.Math.round(x);
        return n;
    }
    private int pinToByte(int n) {
        if (n < 0) {
            n = 0;
        } else if (n > 255) {
            n = 255;
        }
        return n;
    }

    private int ave(int s, int d, float p) {
        return s + java.lang.Math.round(p * (d - s));
    }

  //真ん中の色を取得する
    /**
     * サークル
     * カラー配列とアングルを引数に、真ん中の色を取得
     * @param colors
     * @param unit
     * @return
     */
    private int getCenterFromCercle(int colors[], float unit) {
        if (unit <= 0) {
            return colors[0];
        }
        if (unit >= 1) {
            return colors[colors.length - 1];
        }

        float p = unit * (colors.length - 1);
        int i = (int)p;
        p -= i;

        // now p is just the fractional part [0...1) and i is the index
        int a = ave(Color.alpha(colors[i]), Color.alpha(colors[i+1]), p);
        int r = ave(Color.red(colors[i]), Color.red(colors[i+1]), p);
        int g = ave(Color.green(colors[i]), Color.green(colors[i+1]), p);
        int b = ave(Color.blue(colors[i]), Color.blue(colors[i+1]), p);

        return Color.argb(a, r, g, b);
    }
    /**
     * 彩度バー
     * カラー配列とX座標を引数に、真ん中の色を取得
     * @param bgColors
     * @param unit
     * @return
     */
    private int getCenterFromS(float x) {
        int rgb = normalColor;
        int r = Color.red(rgb);
        int g = Color.green(rgb);
        int b = Color.blue(rgb);
        float[] hsv = new float[3];
        Color.RGBToHSV(r, g, b, hsv);
        //右半分が明度で、左半分が彩度
        hsv[1] = (x+CENTER_X)/(CENTER_X*2);
        return Color.HSVToColor(hsv);
    }


    /**
     * 明度バー
     * カラー配列とX座標を引数に、真ん中の色を取得
     * @param bgColors
     * @param unit
     * @return
     */
    private int getCenterFromV(float x) {
        int rgb = normalColor;
        int r = Color.red(rgb);
        int g = Color.green(rgb);
        int b = Color.blue(rgb);
        float[] hsv = new float[3];
        Color.RGBToHSV(r, g, b, hsv);
        //右半分が明度で、左半分が彩度
        hsv[2] = 1-(x+CENTER_X)/(CENTER_X*2);
        return Color.HSVToColor(hsv);
    }

    private static final float PI = 3.1415926f;

    @Override
    public boolean onTouchEvent(MotionEvent event) {

    	//座標を戻す
        float x = event.getX() - CENTER_X;
        float y = event.getY() - CENTER_Y;
        if(y > CENTER_Y+15){//タッチが、下の2本に適応する場合
        	//端にも円の色が適応されるので左右は幅利かせておかないといけない
        	Log.d("log","Y  " +  y + " " +(CENTER_Y+85));
        	if(y<CENTER_Y+85){//上だった
        		if( x <= -CENTER_X){
           		 mCenterPaint.setColor(0xFFFFFFFF);
                    invalidate();
           		return true;
        		}else if(x >= CENTER_X){//彩度MAXカラー
        			 float[] shsv = new float[3];
        	            Color.RGBToHSV(Color.red(normalColor), Color.green(normalColor), Color.blue(normalColor), shsv);
        	            shsv[1] = 1;
        	     mCenterPaint.setColor(Color.HSVToColor(shsv));
                 invalidate();
        		return true;
        		}
        		//端以外の彩度
            	switch(event.getAction()){
            	case MotionEvent.ACTION_DOWN:
                	int returnColor0 = getCenterFromS(x);
                    mCenterPaint.setColor(returnColor0);
                    invalidate();
            		break;
            	case MotionEvent.ACTION_MOVE:
                	int returnColor1 = getCenterFromS(x);
                    mCenterPaint.setColor(returnColor1);
                    invalidate();
            		break;
            	case MotionEvent.ACTION_UP:
                	int returnColor2 = getCenterFromS(x);
                    mCenterPaint.setColor(returnColor2);
                    parentDialog.setETColor(returnColor2, isBg);
                    invalidate();
            		break;
            	}
        	}else if(y >= CENTER_Y+70){//下だった
        		if(x >= CENTER_X){
        			mCenterPaint.setColor(0xFF000000);
                    invalidate();
        		return true;
        		}else if( x <= -CENTER_X){//明度MAXカラー
        			float[] vhsv = new float[3];
                    Color.RGBToHSV(Color.red(normalColor), Color.green(normalColor),Color.blue(normalColor), vhsv);
                     vhsv[2] = 1;
        		 mCenterPaint.setColor(Color.HSVToColor(vhsv));
                 invalidate();
           		return true;
        		}
        		//端以外の明度
            	switch(event.getAction()){
            	case MotionEvent.ACTION_DOWN:
                	int returnColor0 = getCenterFromV(x);
                    mCenterPaint.setColor(returnColor0);
                    invalidate();
            		break;
            	case MotionEvent.ACTION_MOVE:
                	int returnColor1 = getCenterFromV(x);
                    mCenterPaint.setColor(returnColor1);
                    invalidate();
            		break;
            	case MotionEvent.ACTION_UP:
                	int returnColor2 = getCenterFromV(x);
                    mCenterPaint.setColor(returnColor2);
                    parentDialog.setETColor(returnColor2, isBg);
                    invalidate();
            		break;
            	}
        	}
        	//明度を適応する
        	return true;
        }
        //センターを押したかどうか
        boolean isCenterTouch = java.lang.Math.sqrt(x*x + y*y) <= CENTER_RADIUS;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mTrackingCenter = isCenterTouch;
                if (isCenterTouch) {
                    mHighlightCenter = true;
                    invalidate();
                    break;
                }
            case MotionEvent.ACTION_MOVE:
                if (mTrackingCenter) {
                    if (mHighlightCenter != isCenterTouch) {
                        mHighlightCenter = isCenterTouch;
                        invalidate();
                    }
                } else {
                    float angle = (float)java.lang.Math.atan2(y, x);
                    // need to turn angle [-PI ... PI] into unit [0....1]
                    float unit = angle/(2*PI);
                    if (unit < 0) {
                        unit += 1;
                    }
                    int selectedColor = getCenterFromCercle(mCercleColors, unit);
                    //色が決定する所で、モノクロにしない値を保存しておく
                    normalColor = selectedColor;
                    mCenterPaint.setColor(selectedColor);

                    //SVの値を決める
                    float[] shsv = new float[3];
                    Color.RGBToHSV(Color.red(selectedColor), Color.green(selectedColor), Color.blue(selectedColor), shsv);
                    shsv[1] = 1;
                    int[] sBarColor = new int[] {
                            0xFFFFFFFF, Color.HSVToColor(shsv)
                        };
                    float[] vhsv = new float[3];
                    Color.RGBToHSV(Color.red(selectedColor), Color.green(selectedColor),Color.blue(selectedColor), vhsv);
                    vhsv[2] = 1;
                    int[] vBarColor = new int[]{
                    	Color.HSVToColor(vhsv),0xFF000000
                    };
                    Shader sbShader = new LinearGradient(-CENTER_X, 30, CENTER_X,30,sBarColor,null,TileMode.CLAMP);
                    Shader vbShader = new LinearGradient(-CENTER_X, 85, CENTER_X,85,vBarColor,null,TileMode.CLAMP);
                    svPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                    svPaint.setShader(sbShader);
                    svPaint.setStyle(Paint.Style.STROKE);
                    svPaint.setStrokeWidth(30);
                    vvPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                    vvPaint.setShader(vbShader);
                    vvPaint.setStyle(Paint.Style.STROKE);
                    vvPaint.setStrokeWidth(30);
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mTrackingCenter) {
                    if (isCenterTouch) {
//                        mListener.colorChanged(mCenterPaint.getColor());//リスナを呼ぶ
                    }
                    parentDialog.setETColor(mCenterPaint.getColor(), isBg);
                    mTrackingCenter = false;    // so we draw w/o halo
                    invalidate();
                }
                break;
        }
        return true;
    }


	public int getCenterColor() {
		return mCenterPaint.getColor();
	}
}//End of ColorPickerView