package nliveroid.nlr.main;

import android.content.Context;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;


public class NewLineTableCell extends TextView{
	int cellWidth = 0;
	int cellHeight = 0;
	//実際のadjust値
	float heightAdjust = 2;
	public NewLineTableCell(Context context, AttributeSet attrs) {
		super(context, attrs);
		int index = 3;
		int widthValue = 0;
		CommentPostable postable = (CommentPostable)context;
		heightAdjust = postable.getHeightAdjust();
		//OverLayのインスタンスは必ずあるはずなのでそちらを利用する
		if(attrs.getAttributeValue(index).equals(URLEnum.NTYPE_STR)){
		widthValue =  postable.getTableWidth(0);
		}else if(attrs.getAttributeValue(index).equals(URLEnum.NID_STR)){
			widthValue = postable.getTableWidth(1);
		}else if(attrs.getAttributeValue(index).equals(URLEnum.NCMD_STR)){
			widthValue = postable.getTableWidth(2);
		}else if(attrs.getAttributeValue(index).equals(URLEnum.NTIME_STR)){
			widthValue = postable.getTableWidth(3);
		}else if(attrs.getAttributeValue(index).equals(URLEnum.NNUM_STR)){
			widthValue = postable.getTableWidth(4);
		}else if(attrs.getAttributeValue(index).equals(URLEnum.NSCORE_STR)){
			widthValue = postable.getTableWidth(5);
		}else if(attrs.getAttributeValue(index).equals(URLEnum.NCOMMENT_STR)){
			this.setEllipsize(null);
			widthValue = postable.getTableWidth(6);//改行表示はここが変わってくる
//			widthValue = 100;
		}
		if(postable.isPortLayt()){
		cellWidth = (int) (widthValue*0.01f* postable.getViewWidth());
		}else{
		cellWidth = (int) (widthValue*0.01f* postable.getViewHeight());
		}
		cellHeight = (int)postable.getCellHeight();
//		Log.d("Log","Cpos " + pos);
//		Log.d("Log","isP " + postable.isPortLayt());
//		Log.d("Log","wv " + widthValue);
//		Log.d("Log","WH VAL " + cellHeight+ "  "+ cellWidth);
	}

	 @Override
	    protected void onMeasure(int widthMeasureSpec,int heightMeasureSpec){
	        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	        this.setTextSize(cellHeight/heightAdjust);
	        setWidth(cellWidth);
	    }
}
