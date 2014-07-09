package nliveroid.nlr.main.parser;

import java.util.HashMap;

import nliveroid.nlr.main.LiveTab.SetBeforeServerProfile;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import android.util.Log;

public class ReuseParser  implements ContentHandler {

	private StringBuilder innerText = new StringBuilder(1024);
	private HashMap<String,String> formValues = new HashMap<String,String>();
	private SetBeforeServerProfile task;
	private boolean isDescription;
	private boolean communityTarget;
	private boolean defaultCommunity;
	private boolean isCommuSelector;
	private boolean isCategorySelector;
	private boolean isCategory;
	private boolean isTagArea;
	private int tagCount = 0;
	public ReuseParser(SetBeforeServerProfile task){
		this.task = task;
	}

	private String getInnerText(char[] arg0,int arg2){
		innerText = innerText.delete(0,arg0.length);
		innerText.append(arg0, 0, arg2);
		return innerText.toString();
	}

	@Override
	public void characters(char[] arg0, int arg1, int arg2) throws SAXException {
		if(isDescription){
			String desc = getInnerText(arg0,arg2);
			if(desc != null)formValues.put("description", desc.trim());
			isDescription = false;
		}else if(defaultCommunity){
//			Log.d("defaultCommunity "," " + defaultCommunity);
			formValues.put("community_name", getInnerText(arg0,arg2).trim().replace("\n|\t", ""));
			defaultCommunity = false;
		}else if(isCategory){
			formValues.put("category", getInnerText(arg0,arg2).trim().replace("\n|\t", ""));
			isCategory = false;
		}
	}

	@Override
	public void startElement(String arg0, String arg1, String arg2,
			Attributes arg3) throws SAXException {

		if(arg1.equals("input") && arg3 != null){
//			for(int i = 0; i < arg3.getLength(); i++){
//			Log.d("input ","  " + arg3.getLocalName(i) + "  " + arg3.getValue(i) );
//			}
			if(isTagArea){//タグの辺りもinputタグなので先に書く
				boolean isLiveTags = false;
				String value = "";
				boolean isTagLock = false;
				boolean isChecked = false;
				for(int i = 0; i < arg3.getLength(); i++){
					if(arg3.getValue(i).contains("livetags")){
						isLiveTags = true;
					}else if(arg3.getValue(i).contains("taglock")){
						isTagLock = true;
					}
					if(arg3.getLocalName(i).equals("value")){
						value = arg3.getValue(i);
					}else if(arg3.getLocalName(i).equals("checked")){
						isChecked = true;
					}
				}
				if(isLiveTags){
					if(value == null || value.equals(""))return;//valueがないlabel等が
					formValues.put("tag"+tagCount, value);
				}else if(isTagLock){
					formValues.put("lock"+tagCount, String.valueOf(isChecked));
					tagCount++;
				}
			}else{
				boolean isTitle = false;
				String value = null;
				boolean isPublicStatus = false;
				boolean isTimeShift = false;
				boolean isTwitter = false;
				boolean isChecked = false;
				for(int i = 0; i < arg3.getLength(); i++){
					if(arg3.getLocalName(i).equals("name") && arg3.getValue(i).equals("title")){
						isTitle = true;
					}else if(arg3.getLocalName(i).equals("id")){
						if(arg3.getValue(i).equals("community_only")){
							isPublicStatus = true;
						}else if( arg3.getValue(i).equals("timeshift_enabled")){
							isTimeShift = true;
						}else if(arg3.getValue(i).equals("id_twitter_enabled")){
							isTwitter = true;
						}
					}else if(arg3.getLocalName(i).equals("checked")){
						isChecked = true;
					}else if(arg3.getLocalName(i).equals("value")){
						value = arg3.getValue(i);
					}
				}
//				Log.d("FORM "," value" + value +  "   "+isChecked);
				if(isTitle && value != null)formValues.put("title",value);//タイトル取得
				if(isPublicStatus)formValues.put("public_status", String.valueOf(isChecked));//コミュ限か
				if(isTimeShift)formValues.put("timeshift_enable", String.valueOf(isChecked));//TSか
				if(isTwitter)formValues.put("twitter_enable", String.valueOf(isChecked));//Twitter。。とりあえず作ってないけど
			}
		}else if(arg1.equals("textarea") && arg3 != null){
			for(int i = 0; i < arg3.getLength(); i++){
				if(arg3.getLocalName(i).equals("name") && arg3.getValue(i).equals("description")){
					isDescription = true;
					break;
				}
			}
		}else if(arg1.equals("select") && arg3 != null){
			for(int i = 0; i < arg3.getLength(); i++){
				if(arg3.getLocalName(i).equals("id")&&arg3.getValue(i).equals("default_community")){
					isCommuSelector = true;
				}
			}
		}else if(isCommuSelector && arg1.equals("option") && arg3 != null){
			//オプションはタグの中に値無しのgetLocalNameでとれる selected >みたいになってる
			for(int i = 0; i < arg3.getLength(); i++){
				if(arg3.getLocalName(i).equals("selected")){
					defaultCommunity = true;
					isCommuSelector = false;
					break;
				}
			}
		}else if(isCategorySelector && arg1.equals("option") && arg3 != null){
			//選択したカテゴリ
			for(int i = 0; i < arg3.getLength(); i++){
				if(arg3.getLocalName(i).equals("selected")){
					isCategory = true;
					isCategorySelector = false;
					break;
				}
			}
		}else if(arg1.equals("div") && arg3 != null){//div判定がinputを内包しているとinputが判定できない
			for(int i = 0; i < arg3.getLength(); i++){
				if(arg3.getLocalName(i).equals("id")){
					if(arg3.getValue(i).equals("live_tag_main")){
//						Log.d("TAGAREA" ,"TRUE ----------");
						isTagArea = true;
					}else if(arg3.getValue(i).equals("page_footer")){
						task.finishCallBack(formValues);
					}
				}
			}
		}


	}

	@Override
	public void endElement(String arg0, String arg1, String arg2)throws SAXException {

		if(arg1.equals("select")){
			isCommuSelector = false;
			isCategorySelector = false;
		}else if(isTagArea && arg1.equals("td")){
//			Log.d("TAGAREA" ,"FALSE ----------");
			isTagArea = false;
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
