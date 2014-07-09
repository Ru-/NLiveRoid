package nliveroid.nlr.main.parser;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nliveroid.nlr.main.ErrorCode;
import nliveroid.nlr.main.LiveInfo;
import nliveroid.nlr.main.NLiveRoid;
import nliveroid.nlr.main.SearchTab.SearchTask;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import android.util.Log;

/**
 * カテゴリ||キーワード・タグ共通パーサ
 * カテゴリ側は、<img src="img/smartphone/status/onair.png?090813" alt="ONAIR">	"でnew
 * キーワード・タグ側は<a href="...lv[0-9]でnew
 * 来場、コメント、タグのアイコン、lv、開始時間、タイトル、サムネイル
 * ソートの方法が増えてる、予約数が新たに追加
 * @author Owner
 *
 */
public class CategoryParser implements ContentHandler {

	private String startTag;
	private Attributes nowAttr;
	private ArrayList<LiveInfo> liveInfos = new ArrayList<LiveInfo>();
	private StringBuilder innerText = new StringBuilder(1024);
	private LiveInfo tempInfo = new LiveInfo();
	private byte liCount = 0;
	private SearchTask task;
	private Pattern lvpt = Pattern.compile("lv[0-9]+");
	private Pattern copt = Pattern.compile("co[0-9]+");
	private Pattern chpt = Pattern.compile("ch[0-9]+");
	private ErrorCode error;//最初のパースとサムネイル取得に必要
	public CategoryParser(SearchTask task,ErrorCode error){
		this.task = task;
		this.error = error;
	}


	private String getInnerText(char[] arg0,int arg2){
		innerText = innerText.delete(0,arg0.length);
		innerText.append(arg0, 0, arg2);
		return innerText.toString();
	}

	@Override
	public void characters(char[] arg0, int arg1, int arg2) throws SAXException {
		if(liCount >= 5)getInnerText(arg0,arg2);//endElementのため
		if(liCount == 3 && startTag.equals("h2") && nowAttr != null && nowAttr.getValue("class") != null){//class="title"
			if(NLiveRoid.isDebugMode)Log.d("NLiveRoid","liCount "+liCount+" " + getInnerText(arg0,arg2));
			tempInfo.setTitle(getInnerText(arg0,arg2));
			liCount = 4;
		}else if(liCount == 4 && startTag.equals("span") && nowAttr != null && nowAttr.getValue("class") != null){//class="desc"
			try{
				if(NLiveRoid.isDebugMode)Log.d("NLiveRoid","setComunityName liCount "+liCount+" " + getInnerText(arg0,arg2));
				tempInfo.setComunityName(getInnerText(arg0,arg2).substring(4));
			}catch(Exception e){
				e.printStackTrace();
			}
			liCount = 5;
		}
	}

	@Override
	public void startElement(String arg0, String arg1, String arg2,
			Attributes attr) throws SAXException {
		Log.d("NLiveRoid","Category_s_tag " + arg1);
			if(liCount == 0 && arg1.equals("li") && attr != null && attr.getValue("class") != null && attr.getValue("class").contains("has_image")){
				liCount = 1;//has_imageで1つの放送の開始とみる
				Log.d("NLiveRoid","FIND_LI");
			}else if(liCount == 1 && arg1.equals("a") && attr != null && attr.getValue("href") != null){
				Matcher mc = lvpt.matcher(attr.getValue("href"));
				if(mc.find()){
//					if(NLiveRoid.isDebugMode)Log.d("NLiveRoid","liCount "+liCount+" setLiveID");
						tempInfo.setLiveID(mc.group());
				}else{
					error.setErrorCode(-27);
					Log.d("NLiveRoid","Failed CategoryParser :00");
					task.finishCallBack(null,new LinkedHashMap<String, String>());
				}
				liCount = 2;
			}else if(liCount == 2 && arg1.equals("img") && attr != null && attr.getValue("src") != null){
//				if(NLiveRoid.isDebugMode)Log.d("NLiveRoid"," IMG --- " + attr.getValue("src") +" ");
				Matcher comc = copt.matcher(attr.getValue("src"));
				if(comc.find()){
//					if(NLiveRoid.isDebugMode)Log.d("NLiveRoid","liCount  "+liCount+" setCommunityID");
						tempInfo.setCommunityID(comc.group());
				}else{
					Matcher chmc = chpt.matcher(attr.getValue("src"));
					if(chmc.find()){
						tempInfo.setCommunityID(chmc.group());
					}else{
					error.setErrorCode(-27);
					Log.d("NLiveRoid","Failed CategoryParser :01");
					task.finishCallBack(null,new LinkedHashMap<String, String>());
					}
				}
				liCount = 3;
			}
			if(liCount >=3){
				startTag=arg1;
				nowAttr = attr;
			}
	}

	@Override
	public void endDocument() throws SAXException {
//		if(NLiveRoid.isDebugMode)Log.d("NLiveRoid","CEND endDocument ---- " + startTag);
		task.finishCallBack(liveInfos);
		}
	@Override
	public void endElement(String arg0, String arg1, String arg2)throws SAXException {
		if(liCount == 5 && arg1.equals("li")){
			if(!innerText.toString().contains("：")){
//				if(NLiveRoid.isDebugMode)Log.d("NLiveRoid","liCount liCount == 5 ");
				tempInfo.setViewCount(innerText.toString());
			}else{
			tempInfo.setViewCount("0");
			}
			liCount = 6;
		}else if(liCount == 6&& arg1.equals("li")){
			if(!innerText.toString().contains("：")){
//				if(NLiveRoid.isDebugMode)Log.d("NLiveRoid","liCount "+liCount+" setResNumber");
				tempInfo.setResNumber(innerText.toString());
			}else{
			tempInfo.setResNumber("0");
			}
//			Log.d("NLiveRoid","ADD ---------- ");
			liveInfos.add(tempInfo.clone());
			tempInfo = new LiveInfo();
			liCount = 0;
		}
	}
	@Override
	public void endPrefixMapping(String arg0) throws SAXException {
		}
	@Override
	public void ignorableWhitespace(char[] arg0, int arg1, int arg2)
			throws SAXException {
		// TODO
	}

	@Override
	public void processingInstruction(String arg0, String arg1)
			throws SAXException {
		// TODO
	}
	@Override
	public void setDocumentLocator(Locator arg0) {
		// TODO

	}

	@Override
	public void skippedEntity(String arg0) throws SAXException {
		// TODO

	}

	@Override
	public void startDocument() throws SAXException {
		// TODO
		if(NLiveRoid.isDebugMode)Log.d("NLiveRoid","CEND startDocument -------------- " + startTag);

	}


	@Override
	public void startPrefixMapping(String arg0, String arg1)
			throws SAXException {
		// TODO

	}




}
