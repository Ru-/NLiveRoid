package nliveroid.nlr.main;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.util.Log;

public class DBBean {
	private String id;//(int)ArrayAdapterで使いまわす為に全てStringで定義
	private String date;//(long)
	private String kind;//(int)
	private String lv;
	private String coch;
	private String remark0;
	private String remark1;
	private String remark2;
	public DBBean(String id,long date ,int kind,String lv,String coch,String remark0,String remark1,String remark2){
		this.id = id;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd/ HH:mm");
		this.date = sdf.format(new Date(date));
		switch(kind){
		case 0:
			this.kind = "視聴";
			break;
		case 1:
			this.kind = "詳細";
			break;
		case 2:
			this.kind = "検索";
			break;
		}
		this.lv = lv;
		if(coch != null && coch.equals("-")){
		this.coch = "";
		}else{
		this.coch = coch;
		}
		if(remark0 != null && remark0.equals("-")){
			this.remark0 = "";
		}else{
		this.remark0 = remark0;
		}
		if(remark1 != null && remark1.equals("-")){
			this.remark1 = "";
		}else{
		this.remark1 = remark1;
		}
		if(remark2 != null && remark2.equals("-")){
			this.remark2 = "";
		}else{
		this.remark2 = remark2;
		}
		Log.d("NLiveRoid","DBBean " + lv + " " + coch + "  "+ remark0 + " " + remark1 + " " + remark2);
	}
	/**
	 * idを取得します。
	 * @return id
	 */
	public String getId() {
	    return id;
	}
	/**
	 * dateを取得します。
	 * @return date
	 */
	public String getDate() {
	    return date;
	}
	/**
	 * kindを取得します。
	 * @return kind
	 */
	public String getKind() {
	    return kind;
	}
	/**
	 * lvを取得します。
	 * @return lv
	 */
	public String getLv() {
	    return lv;
	}
	/**
	 * cochを取得します。
	 * @return coch
	 */
	public String getCoch() {
	    return coch;
	}
	/**
	 * remark0を取得します。
	 * @return remark0
	 */
	public String getRemark0() {
	    return remark0;
	}
	/**
	 * remark1を取得します。
	 * @return remark1
	 */
	public String getRemark1() {
	    return remark1;
	}
	/**
	 * remark2を取得します。
	 * @return remark2
	 */
	public String getRemark2() {
	    return remark2;
	}
}
