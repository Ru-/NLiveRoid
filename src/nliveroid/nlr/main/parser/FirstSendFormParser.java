package nliveroid.nlr.main.parser;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nliveroid.nlr.main.ErrorCode;
import nliveroid.nlr.main.LiveTab.FirstSendFormTask;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import android.util.Log;

public class FirstSendFormParser  implements ContentHandler {

	private StringBuilder innerText = new StringBuilder(1024);
	private ArrayList<String> messages = new ArrayList<String>();
	private String ulck = "";
	private String descri = "";
	private String[] response = new String[2];
	private boolean parseTarget = false;
	private FirstSendFormTask task;
	private boolean isStep2;
	private ErrorCode error;//最初のパースとサムネイル取得に必要
	public FirstSendFormParser(FirstSendFormTask task,ErrorCode error){
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
		if(parseTarget){
			String inner = getInnerText(arg0,arg2);
			if(inner.contains("既にこの時間に予約をしているか")){
				isStep2 = true;
			}else if(inner.contains("既に順番待ちに並んでいるか")){
				isStep2 = true;
			}else if(inner.contains("ページの有効期限が切れています")){
				messages.add("ページの有効期限が切れています");
			}else{
				messages.add(inner);
			}
			parseTarget = false;
		}
	}

	@Override
	public void startElement(String arg0, String arg1, String arg2,
			Attributes arg3) throws SAXException {
		if(arg1.equals("li") && arg3 != null && arg3.getLength() >=1){
			for(int i = 0; i < arg3.getLength(); i++){
				if(arg3.getValue(i).equals("error_message")){
					parseTarget = true;
				}
			}
		}
		if(arg1.equals("input")){
			for(int i = 0; i < arg3.getLength(); i++){
				if(arg3.getLocalName(i).equals("name")){
					if(arg3.getValue(i).equals("description")){
						for(int j = 0; j < arg3.getLength(); j++){
							if(arg3.getLocalName(j).equals("value")){
								descri = arg3.getValue(j);
							}
						}
					}else if(arg3.getValue(i).equals("usecoupon")||arg3.getValue(i).equals("confirm")){
					Pattern pt = Pattern.compile("ulck_[0-9]+");
					Matcher mc = null;
					for(int j = 0; j < arg3.getLength(); j++){
						mc = pt.matcher(arg3.getValue(j));
						if(mc.find()){
							ulck = mc.group();
					    	}
					   }
					}
				}
			}
		}

		if(arg1.equals("span")){
			for(int i = 0; i < arg3.getLength(); i++){
				if(arg3.getValue(i).equals("step02")){
					//放送できる
					isStep2 = true;
				}
			}
		}

	}

	@Override
	public void endElement(String arg0, String arg1, String arg2)throws SAXException {

		if(arg1.equals("body")){
			if(isStep2&&messages.size() == 0){
				response[0] = ulck;
				response[1] = descri;
				task.finishCallBack(response);
			}else{
			task.finishCallBack(messages,ulck);
			}
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
