package nliveroid.nlr.main.parser;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nliveroid.nlr.main.LiveArchivesDialog.LiveArchiveTask;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import android.util.Log;

public class LiveArchiveParser implements ContentHandler {
	private LiveArchiveTask task;
	private StringBuilder innerText = new StringBuilder(1024);
	private ArrayList<String[]> result = new ArrayList<String[]>();
	private String[] element = new String[6];//日付、放送者名、LV、タイトル、TS有無、詳細
	private String startTag;
	private Attributes nowAttr;
	private int tdCount;
	private final Pattern lvpt = Pattern.compile("lv[0-9]+");
	private boolean parseTarget;
	public LiveArchiveParser(LiveArchiveTask task){
		this.task = task;
	}
	private String getInnerText(char[] arg0,int arg2){
		innerText = innerText.delete(0,arg0.length);
		innerText.append(arg0, 0, arg2);
		return innerText.toString();
	}
	@Override
	public void characters(char[] arg0, int arg1, int arg2) throws SAXException {
		if(!parseTarget)return;
		if(startTag.equals("td") && nowAttr != null && nowAttr.getValue("class") != null && nowAttr.getValue("class").equals("date")){
			tdCount = 1;
			String inner = getInnerText(arg0,arg2);//ここで2013/03/03<br>開演：08:43<br>とかなってる
//			Log.d("DATE INNER --- " , " " + inner);
			if(inner == null)return;
			element[0] = inner.replaceAll("\t|\n", "") + "\n";
		}else if(tdCount == 1&&startTag.equals("br")){
			tdCount++;
//			Log.d("DATE INNER --- " , " １　" + startTag);
				String inner = getInnerText(arg0,arg2);
				if(inner == null)return;
				element[0] += inner.replaceAll("\t|\n", "");
		}else if(tdCount == 2 && startTag.equals("div")){
//			Log.d("DATE INNER --- " , " 2　" + startTag);
				tdCount++;
				String inner = getInnerText(arg0,arg2);
				if(inner == null)return;
				element[1] = inner.replaceAll("\t|\n", "");
		}else if(tdCount == 3 && nowAttr != null && nowAttr.getValue("href") != null){
//			Log.d("DATE INNER --- " , " 3　" + startTag);
			tdCount ++;
			Matcher mc = lvpt.matcher(nowAttr.getValue("href"));
			if(mc.find()){
				element[2] = mc.group();
			}
			String inner = getInnerText(arg0,arg2);
			if(inner == null)return;
			element[3] = inner.replaceAll("\t|\n", "");
		}else if(tdCount == 4 ){
//			Log.d("DATE INNER --- " , " 4" + startTag);
			if(startTag.equals("img")){//imgタグがあった時点でTS視聴可能と判断する
			element[4] = "1";
			}else if(startTag.equals("div") && nowAttr != null && nowAttr.getValue("class") != null
					&&nowAttr.getValue("class").equals("txt")){
				tdCount = 0;
				String inner = getInnerText(arg0,arg2);
					if(inner == null)return;
					element[5] = inner.replaceAll("\t", "");
					result.add(element.clone());
					element = new String[6];
			}
		}
	}
	@Override
	public void startElement(String arg0, String arg1, String arg2,
			Attributes arg3) throws SAXException {
		startTag = arg1;
		nowAttr = arg3;
		if(arg1.equals("table") && nowAttr.getValue("class") != null && nowAttr.getValue("class").equals("live_history")){
			parseTarget = true;//念のためメインのテーブルのみパース
		}
	}

	@Override
	public void endElement(String arg0, String arg1, String arg2)throws SAXException {
//		Log.d("ARCHIVE", " " + arg0 + "  " +arg1);
		if(arg1.equals("table")||arg1.equals("body")){
			parseTarget = false;
			task.finishCallBack(result);
		}


	}

	@Override
	public void endDocument() throws SAXException {}
	@Override
	public void endPrefixMapping(String arg0) throws SAXException {}
	@Override
	public void ignorableWhitespace(char[] arg0, int arg1, int arg2)throws SAXException {}
	@Override
	public void processingInstruction(String arg0, String arg1)throws SAXException {}
	@Override
	public void setDocumentLocator(Locator arg0) {}
	@Override
	public void skippedEntity(String arg0) throws SAXException {}
	@Override
	public void startDocument() throws SAXException {}
	@Override
	public void startPrefixMapping(String arg0, String arg1)throws SAXException {}


}
