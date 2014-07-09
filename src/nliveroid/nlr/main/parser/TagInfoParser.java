package nliveroid.nlr.main.parser;

import java.util.HashMap;

import nliveroid.nlr.main.TagArrangeDialog.GetTagInfo;

import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import android.util.Log;

public class TagInfoParser implements ContentHandler {
	private StringBuilder innerText = new StringBuilder(1024);
	private boolean parseTarget = false;
	private HashMap<String,Boolean> map = new HashMap<String,Boolean>();
	private GetTagInfo task;
	private boolean finished;
	private int tableCount = 0;
	private boolean isLock;
	private boolean isTargetSpan;
	public TagInfoParser(GetTagInfo task){
		this.task = task;
	}


	private String getInnerText(char[] arg0,int arg2){
		innerText = innerText.delete(0,arg0.length);
		innerText.append(arg0, 0, arg2);
		return innerText.toString();
	}


	@Override
	public void startElement(String arg0, String arg1, String arg2,
			org.xml.sax.Attributes arg3) throws SAXException {
//		Log.d("log","S --- " + arg1);
		if(arg1 != null && arg1.equals("table")){
			tableCount++;
		}
		if(tableCount == 2){
			parseTarget= true;
		}
		if(parseTarget&&arg1.equals("span")){
			for(int i = 0; i < arg3.getLength(); i++){
				if(arg3.getValue(i).contains("locked_tags")||arg3.getValue(i).contains("free_tags")){
					Log.d("log","INNER --- " + innerText);
					isTargetSpan = true;
					if(arg3.getValue(i).contains("locked")){
						isLock=true;
					}else{
						isLock=false;
					}
				}
			}
		}
	}

	@Override
	public void characters(char[] arg0, int arg1, int arg2)
			throws SAXException {
		if(parseTarget){
			getInnerText(arg0,arg2);
		}
	}


	@Override
	public void endElement(String arg0, String arg1, String arg2)
			throws SAXException {
		if(arg1.equals("span")&&isTargetSpan){
			isTargetSpan = false;
			map.put(innerText.toString(), isLock);
		}
		if(arg1.equals("table")&&parseTarget&&!finished){//finishedないと2回きちゃう
			parseTarget = false;
			finished = true;
			task.finishCallBack(map);
		}
		if(arg1.equals("html")&&!finished){
			task.finishErrorCallBack();
		}
	}

	@Override
	public void endDocument() throws SAXException {
		// TODO

	}

	@Override
	public void endPrefixMapping(String arg0) throws SAXException {
		// TODO

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

	}


	@Override
	public void startPrefixMapping(String arg0, String arg1)
			throws SAXException {
		// TODO

	}





}
