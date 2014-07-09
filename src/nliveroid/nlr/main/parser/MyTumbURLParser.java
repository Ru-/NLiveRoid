package nliveroid.nlr.main.parser;

import nliveroid.nlr.main.HistoryTab.GetMyThumb;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import android.util.Log;

public class MyTumbURLParser  implements ContentHandler{

	private GetMyThumb task;
	private boolean target;
	private boolean finished;
	private StringBuilder innerText = new StringBuilder(1024);

	public MyTumbURLParser(GetMyThumb getMyThumb) {
		this.task = getMyThumb;
	}

	private String getInnerText(char[] arg0,int arg2){
		innerText = innerText.delete(0,arg0.length);
		innerText.append(arg0, 0, arg2);
		return innerText.toString();
	}

	@Override
	public void characters(char[] ac, int i, int j) throws SAXException {
		if(target)Log.d("NLiveRoid", " ttttt" + getInnerText(ac,j));
	}

	@Override
	public void startElement(String s, String s1, String s2,
			Attributes attributes) throws SAXException {
		if(target)Log.d("NLiveRoid","  TTT  " + target + "  " + s1);
		if(target && attributes != null){
			for(int i = 0; i < attributes.getLength(); i ++){
				Log.d("NLiveRoid"," " + attributes.getLocalName(i) + " " + attributes.getValue(i));
			}
		}
		if(s1.equals("div")&&attributes != null && attributes.getValue("class") != null && attributes.getValue("class").equals("avatar")){
			target = true;
			Log.d("NLiveRoid"," TRUE"  );
		}else if(target && s1.equals("img")&& attributes != null && attributes.getValue("src") != null){
			task.finishCallBack(attributes.getValue("src"));
			finished = true;
			Log.d("NLiveRoid"," TRUE2"  );
		}else if(target && s1.equals("div")){
			target = false;
			Log.d("NLiveRoid"," 3ZZZZ3"  );
			finished = true;
		}
	}

	@Override
	public void endElement(String s, String s1, String s2) throws SAXException {
		if(!finished && s1.equals("body")){
			Log.d("NLiveRoid"," xBODY"  );
				task.finishCallBack(null);
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