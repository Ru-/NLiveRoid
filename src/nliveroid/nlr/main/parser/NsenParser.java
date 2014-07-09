package nliveroid.nlr.main.parser;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nliveroid.nlr.main.ErrorCode;
import nliveroid.nlr.main.LiveInfo;
import nliveroid.nlr.main.FinishCallBacks;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import android.util.Log;

public class NsenParser implements ContentHandler {
	private FinishCallBacks task;
	private ErrorCode error;
	private boolean isFinished;
	public NsenParser(FinishCallBacks task,ErrorCode error){
		this.task = task;
		this.error = error;
	}
	@Override
	public void characters(char[] arg0, int arg1, int arg2) throws SAXException {
		// TODO 自動生成されたメソッド・スタブ
	}
	@Override
	public void endDocument() throws SAXException {
		Log.d("NLiveROid"," endElementW" + isFinished);
		if(!isFinished){
			task.finishCallBack(null);
		}
	}
	@Override
	public void endElement(String arg0, String arg1, String arg2)
			throws SAXException {
	}
	@Override
	public void endPrefixMapping(String prefix) throws SAXException {
		// TODO 自動生成されたメソッド・スタブ
	}
	@Override
	public void ignorableWhitespace(char[] ch, int start, int length)
			throws SAXException {
		// TODO 自動生成されたメソッド・スタブ
	}
	@Override
	public void processingInstruction(String target, String data)
			throws SAXException {
		// TODO 自動生成されたメソッド・スタブ
	}
	@Override
	public void setDocumentLocator(Locator locator) {
		// TODO 自動生成されたメソッド・スタブ
	}
	@Override
	public void skippedEntity(String name) throws SAXException {
		// TODO 自動生成されたメソッド・スタブ
	}
	@Override
	public void startDocument() throws SAXException {
		// TODO 自動生成されたメソッド・スタブ
	}
	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes atts) throws SAXException {
		if(localName.equals("link") && atts != null && atts.getValue("href") != null && atts.getValue("href").contains("nicomoba.jp/live/watch/lv")){
			String url = atts.getValue("href");
			Matcher mc = Pattern.compile("lv[0-9]+").matcher(url);
			if(mc.find()){
			LiveInfo info = new LiveInfo();
			info.setLiveID(mc.group());
			ArrayList<LiveInfo> list = new ArrayList<LiveInfo>();
			list.add(info);
 			task.finishCallBack(list);
 			isFinished = true;
			}
		}
	}
	@Override
	public void startPrefixMapping(String prefix, String uri)
			throws SAXException {
		// TODO 自動生成されたメソッド・スタブ
	}

}
