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

public class ChannelParser implements ContentHandler {

	private String startTag;
	private ArrayList<LiveInfo> liveInfos = new ArrayList<LiveInfo>();
	private StringBuilder innerText = new StringBuilder(1024);
	private LiveInfo tempInfo = new LiveInfo();
	private byte divCount = 0;
	private byte h3Count = 0;
	private SearchTask task;
	private Pattern lvpt = Pattern.compile("lv[0-9]+");
	private ErrorCode error;//最初のパースとサムネイル取得に必要
	public ChannelParser(SearchTask task,ErrorCode error){
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

		if(divCount == 5 && startTag.equals("div")){
			tempInfo.setTitle(getInnerText(arg0,arg2));
//			Log.d("NLiveRoid"," ChannelParser " + divCount + " " + tempInfo.getTitle());
			divCount = 6;
		}else if(divCount == 7 && startTag.equals("div")){
			tempInfo.setDescription(getInnerText(arg0,arg2));
//			Log.d("NLiveRoid"," ChannelParser " + divCount + " " + tempInfo.getDescription());
			divCount = 8;
		}else if(divCount == 9 && startTag.equals("dd")){
			tempInfo.setStartTime(getInnerText(arg0,arg2));
			liveInfos.add(tempInfo.clone());
			tempInfo = new LiveInfo();
//			Log.d("NLiveRoid"," ChannelParser " + divCount +" " + tempInfo.getStartTime());
			divCount = 0;
		}
	}

	@Override
	public void startElement(String arg0, String arg1, String arg2,
			Attributes attr) throws SAXException {
		if(arg1.equals("h3") && attr != null && attr.getValue("class") != null && attr.getValue("class").equals("secttl")){//過去に切り替わった
			Log.d("NLiveRoid"," ChannelParser CCCC h3");
			h3Count++;
		}
		if(divCount == 0 && arg1.equals("li") && attr != null && attr.getValue("class") != null && attr.getValue("class").equals("live")){
			if(h3Count == 1)tempInfo.setLiveStarted(true);//現在放送中ならtrueにしておいてSearchTabで区別する
			divCount = 1;
		}else if(divCount == 1 && arg1.equals("a") && attr != null && attr.getValue("href") != null){
			Matcher mc = lvpt.matcher(attr.getValue("href"));
			if(mc.find()){
				tempInfo.setLiveID(mc.group());
			}else{
				error.setErrorCode(-27);
				Log.d("NLiveRoid","Failed ChannelParser :00");
				task.finishCallBack(null,new LinkedHashMap<String, String>());
			}
//			Log.d("NLiveRoid"," ChannelParser " + divCount);
			divCount = 2;
		}else if(divCount == 2 && arg1.equals("img") && attr != null && attr.getValue("data-src") != null){
			tempInfo.setThumbnailURL(attr.getValue("data-src"));//サムネイルは全然chXXXが無いのでURLを取得
//			Log.d("NLiveRoid"," ChannelParser " + divCount + " " + tempInfo.getThumbnailURL());
			divCount = 3;
		}else if(divCount == 3 && arg1.equals("div")){//charcterはエンドタグの間でも呼ばれるのでこうする
			divCount = 4;
		}else if(divCount == 4 && arg1.equals("div")){
			divCount = 5;
		}else if(divCount == 6 && arg1.equals("div")){
			divCount = 7;
		}else if(divCount == 8 && arg1.equals("div")){
			divCount = 9;
		}
		if(divCount >= 5)startTag = arg1;
	}

	@Override
	public void endDocument() throws SAXException {
		if(NLiveRoid.isDebugMode)Log.d("NLiveRoid","CEND endDocument ---- " + startTag);
		task.finishCallBack(liveInfos);
	}
	@Override
	public void endElement(String arg0, String arg1, String arg2)throws SAXException {
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
