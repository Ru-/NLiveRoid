package nliveroid.nlr.main;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.TextView;


public class CommentTableCell extends TextView{
	int cellWidth = 0;
	int cellHeight = 0;
	//実際のadjust値
	float heightAdjust =2 ;
	public CommentTableCell(Context context, AttributeSet attrs) {
		super(context, attrs);
		int widthValue = 0;
		CommentPostable postable = (CommentPostable)context;
		heightAdjust = postable.getHeightAdjust();
		//OverLayのインスタンスは必ずあるはずなのでそちらを利用する
		if(attrs.getAttributeValue(2).equals(URLEnum.TYPE_STR)){
		widthValue =  postable.getTableWidth(0);
		}else if(attrs.getAttributeValue(2).equals(URLEnum.ID_STR)){
			widthValue = postable.getTableWidth(1);
		}else if(attrs.getAttributeValue(2).equals(URLEnum.CMD_STR)){
			widthValue = postable.getTableWidth(2);
		}else if(attrs.getAttributeValue(2).equals(URLEnum.TIME_STR)){
			widthValue = postable.getTableWidth(3);
		}else if(attrs.getAttributeValue(2).equals(URLEnum.SCORE_STR)){
			widthValue = postable.getTableWidth(4);
		}else if(attrs.getAttributeValue(2).equals(URLEnum.NUM_STR)){
			widthValue = postable.getTableWidth(5);
		}else if(attrs.getAttributeValue(2).equals(URLEnum.COMMENT_STR)){
			widthValue = postable.getTableWidth(6);
		}
		if(postable.isPortLayt()){
		cellWidth = (int) (widthValue*0.01f* postable.getViewWidth());
		}else{
		cellWidth = (int) (widthValue*0.01f* postable.getViewHeight());
		}
		cellHeight = (int)postable.getCellHeight();
	}

	 @Override
	    protected void onMeasure(int widthMeasureSpec,int heightMeasureSpec){
	        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	        setMeasuredDimension(cellWidth,cellHeight);
	        this.setTextSize(TypedValue.COMPLEX_UNIT_DIP,cellHeight/heightAdjust);
	    }
}
