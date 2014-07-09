package nliveroid.nlr.main.parser;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nliveroid.nlr.main.ErrorCode;
import nliveroid.nlr.main.LiveInfo;
import nliveroid.nlr.main.SearchTab.RecentLiveTask;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import android.graphics.Bitmap;

public class RecentParser  implements ContentHandler {

	private String startTag;
	private Attributes nowAttr;
	private StringBuilder innerText = new StringBuilder(1024);
	private ArrayList<LiveInfo> liveInfos = new ArrayList<LiveInfo>();
	private ArrayList<Bitmap> thumbNails = new ArrayList<Bitmap>();
	private LiveInfo tempInfo;
	private boolean parseTarget = false;
	private boolean finished = false;
	private RecentLiveTask task;
	private ErrorCode error;//最初のパースとサムネイル取得に必要
	public RecentParser(RecentLiveTask task,ErrorCode error){
		this.task = task;
		this.error = error;
	}


	private String getInnerText(char[] arg0,int arg2){
		innerText = innerText.delete(0,innerText.length());
		innerText.append(arg0, 0, arg2);
		return innerText.toString();
	}

	@Override
	public void characters(char[] arg0, int arg1, int arg2) throws SAXException {
		if(parseTarget){
			 getInnerText(arg0, arg2);
		}

		if(startTag.equals("h1")){
			if(getInnerText(arg0, arg2).contains("放送中の注目番組")||getInnerText(arg0, arg2).contains("ニコ生クルーズ")){
			parseTarget = true;
			}else if(innerText.toString().contains("ユーザー番組")){
				finished = true;
//				Log.d("NLiveRoid","END---------");
			task.finishCallBack(liveInfos);
			parseTarget = false;
			}
		}else if(startTag.equals("h2") && nowAttr.getValue("class") != null&& nowAttr.getValue("class").equals("title")){//2回来るけどstartElementでは来ない
			String title = innerText.toString();
			title = title.replaceAll("\t| |　|	|\n", "");
		if(title.matches(".+")){//文字がない場合がある
		tempInfo.setTitle(title);
		}
//		Log.d("NLiveRoid","TTT---------" + tempInfo.getTitle());
		}

	}

	@Override
	public void startElement(String arg0, String arg1, String arg2,
			Attributes arg3) throws SAXException {
		startTag=arg1;
		nowAttr = arg3;
		if(parseTarget){
		if(startTag.equals("li")&&nowAttr != null && nowAttr.getValue("data-provider-type") != null){
		tempInfo = new LiveInfo();
				//officialかchannel以外何があるのかわからない
				String providerType = nowAttr.getValue("data-provider-type");
				if(providerType.equals("official")&&!tempInfo.getTags().contains("official")){
					tempInfo.setTags( tempInfo.getTags() + "<<TAGXXX>>official");
				}else if(providerType.equals("channel")&&!tempInfo.getTags().contains("channel")){
					tempInfo.setTags( tempInfo.getTags() + "<<TAGXXX>>channel");
				}
//				Log.d("NLiveRoid","NEW---------" + tempInfo.getTags());
		}else if(startTag.equals("a") &&nowAttr.getValue("class") != null&& nowAttr.getValue("class").equals("btn_inner")){
			String watchValue = nowAttr.getValue("href");
			Matcher lvmc = Pattern.compile("lv[0-9]+|ch[0-9]+").matcher(watchValue);
			if(lvmc.find()){
				tempInfo.setLiveID(lvmc.group());
			}
//			Log.d("NLiveRoid","LV---------" + tempInfo.getLiveID());
		}else if(startTag.equals("img")&&nowAttr.getValue("src") != null){
			String attrValue = nowAttr.getValue("src");
			tempInfo.setThumbnailURL(attrValue);
//			Log.d("NLiveRoid","SRC---------" + attrValue);
		}
		}

	}



	@Override
	public void endElement(String arg0, String arg1, String arg2)throws SAXException {
		if(parseTarget){
			if(arg1.equals("p") && nowAttr.getValue("class") != null){
				String inner = innerText.toString();
				inner = inner.replaceAll("\t| |　|\n", "");
				if(inner != null && inner.matches(".++")){//何回か呼ばれるので経過時間が入るときだけadd
					tempInfo.setStartTime(inner.replaceAll("\t| |　|\n", ""));
//					Log.d("NLiveRoid","STARTTIME Match---------" +inner + "  "+ inner.matches(".+") + "  " + inner.matches(".++"));
//					Log.d("NLiveRoid","PPP---------" + tempInfo.getStartTime());
					if(tempInfo != null && tempInfo.getLiveID() != null){//2放送目以降なら1放送分保存していく
					liveInfos.add(tempInfo.clone());
//					Log.d("NLiveRoid","ADD---------");
					}
				}
			}
		}//End Of parseTarget

	}

	@Override
	public void endDocument() throws SAXException {
		if(!finished){
			task.finishCallBack(null);
		}
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
