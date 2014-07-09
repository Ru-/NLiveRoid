package nliveroid.nlr.main.parser;

import nliveroid.nlr.main.CommuBrowser;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;


public class CommunityPageParser implements ContentHandler{

	private CommuBrowser task;
	private int index;
	private boolean target;
	private boolean commplate;
	private String result;

private static nliveroid.nlr.main.NLiveRoid.AppErrorCode error;

	public CommunityPageParser(int index, CommuBrowser commuBrowser) {
		this.index = index;
		this.task = commuBrowser;
	}


	@Override
	public void characters(char[] ac, int i, int j) throws SAXException {
		// TODO 自動生成されたメソッド・スタブ
	}

	@Override
	public void startElement(String s, String s1, String s2,
			Attributes attributes) throws SAXException {
		if(index == 0){
			if(s1.equals("div") && attributes != null && attributes.getValue("class") != null && attributes.getValue("class").contains("channel_blomaga_article_title")){
				target = true;
			}else if(target && s1.equals("a") && attributes.getValue("href") != null){
				target = false;
				result = attributes.getValue("href");
				task.onPageFinished(result);
				commplate = true;
			}
		}else{
			if(s1.equals("div") && attributes != null && attributes.getValue("id") != null && attributes.getValue("id").equals("community_prof_frm2")){
				target = true;
			}else if(target && s1.equals("a") && attributes.getValue("href") != null){
				target = false;
				result = attributes.getValue("href");
				task.onPageFinished(result);
				commplate = true;
			}
		}
	}

	@Override
	public void endElement(String s, String s1, String s2) throws SAXException {
		if(!commplate&&s1.equals("body")){
				task.onPageFinished("Nothing");
		}
	}

	@Override
	public void endDocument() throws SAXException {
		// TODO 自動生成されたメソッド・スタブ
	}

	@Override
	public void endPrefixMapping(String s) throws SAXException {
		// TODO 自動生成されたメソッド・スタブ
	}

	@Override
	public void ignorableWhitespace(char[] ac, int i, int j)throws SAXException {
		// TODO 自動生成されたメソッド・スタブ
	}

	@Override
	public void processingInstruction(String s, String s1) throws SAXException {
		// TODO 自動生成されたメソッド・スタブ
	}

	@Override
	public void setDocumentLocator(Locator locator) {
		// TODO 自動生成されたメソッド・スタブ
	}

	@Override
	public void skippedEntity(String s) throws SAXException {
		// TODO 自動生成されたメソッド・スタブ
	}

	@Override
	public void startDocument() throws SAXException {
		// TODO 自動生成されたメソッド・スタブ
	}

	@Override
	public void startPrefixMapping(String s, String s1) throws SAXException {
		// TODO 自動生成されたメソッド・スタブ

	}

}
