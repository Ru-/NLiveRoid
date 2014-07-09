package nliveroid.nlr.main.parser;

import java.util.ArrayList;

import nliveroid.nlr.main.ErrorCode;
import nliveroid.nlr.main.SearchTab.RankingClosedTask;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import android.util.Log;

public class RankingClosedParser implements ContentHandler {

	private String startTag;
	private Attributes nowAttr;
	private StringBuilder innerText = new StringBuilder(1024);
	private ArrayList<String> result = new ArrayList<String>();
	private boolean parseTarget = false;
	private RankingClosedTask task;
	public RankingClosedParser(RankingClosedTask task,ErrorCode error){
		this.task = task;
	}


	private String getInnerText(char[] arg0,int arg2){
		innerText = innerText.delete(0,arg0.length);
		innerText.append(arg0, 0, arg2);
		return innerText.toString();
	}

	@Override
	public void characters(char[] arg0, int arg1, int arg2) throws SAXException {
	}

	@Override
	public void startElement(String arg0, String arg1, String arg2,
			Attributes arg3) throws SAXException {
		startTag=arg1;
		nowAttr = arg3;
		if(arg1.equals("div") && arg3 != null && arg3.getValue("class") != null){
		
		if( arg3.getValue("class").equals("search_option")){
			parseTarget = true;
		}
		}
		if(parseTarget && arg1.equals("option") && arg3 != null && arg3.getValue("value") != null){
			result.add(arg3.getValue("value"));
		}

	}

	@Override
	public void endDocument() throws SAXException {}
	@Override
	public void endElement(String arg0, String arg1, String arg2)throws SAXException {
		if(parseTarget && arg1.equals("select")){
			parseTarget = false;
			task.finishCallBack(result);
		}
		//footerも呼ばれないのでしかたなくbody
//		if(arg1.equals("body")){
//		task.finishCallBack(result);
//		}
	}
	@Override
	public void endPrefixMapping(String arg0) throws SAXException {}
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

	}


	@Override
	public void startPrefixMapping(String arg0, String arg1)
			throws SAXException {
		// TODO

	}




}
