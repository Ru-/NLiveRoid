package nliveroid.nlr.main.parser;

import java.util.ArrayList;
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

public class KeyWordParser implements ContentHandler {

	private String startTag;
	private Attributes nowAttr;
	private StringBuilder innerText = new StringBuilder(1024);
	private ArrayList<LiveInfo> liveInfos = new ArrayList<LiveInfo>();
	private LiveInfo tempInfo;
	private int ulCount = 0;
	private int infoCount = 0;
	private boolean parseTarget = true;
	private boolean finished;
	private SearchTask task;
	private Pattern copt = Pattern.compile("co[0-9]+");
	private Pattern chpt = Pattern.compile("ch[0-9]+");
	public KeyWordParser(SearchTask task,ErrorCode error){
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
		if(nowAttr != null && nowAttr.getLength() > 0){
			if(startTag.equals("div")){
				String attrValue = nowAttr.getValue(0);
//				Log.d("Log","DIV " + attrValue);
				if(attrValue.equals("icon official")&&!tempInfo.getTags().contains("official")){
					tempInfo.setTags( tempInfo.getTags() + "<<TAGXXX>>official");
				}else if(attrValue.equals("icon common")&&!tempInfo.getTags().contains("common")){
					tempInfo.setTags( tempInfo.getTags() + "<<TAGXXX>>common");
				}else if(attrValue.equals("icon only")&&!tempInfo.getTags().contains("only")){
					tempInfo.setTags( tempInfo.getTags() + "<<TAGXXX>>only");
				}else if(attrValue.equals("icon face")&&!tempInfo.getTags().contains("face")){
					tempInfo.setTags( tempInfo.getTags() + "<<TAGXXX>>face");
				}else if(attrValue.equals("icon totu")&&!tempInfo.getTags().contains("totu")){
					tempInfo.setTags( tempInfo.getTags() + "<<TAGXXX>>totu");
				}else if(attrValue.equals("icon live")&&!tempInfo.getTags().contains("live")){
					tempInfo.setTags( tempInfo.getTags() + "<<TAGXXX>>live");
				}else if(attrValue.equals("icon play")&&!tempInfo.getTags().contains("play")){
					tempInfo.setTags( tempInfo.getTags() + "<<TAGXXX>>play");
				}else if(attrValue.equals("icon sing")&&!tempInfo.getTags().contains("sing")){
					tempInfo.setTags( tempInfo.getTags() + "<<TAGXXX>>sing");
				}else if(attrValue.equals("icon lecture")&&!tempInfo.getTags().contains("lecture")){
					tempInfo.setTags( tempInfo.getTags() + "<<TAGXXX>>lecture");
				}else if(attrValue.equals("icon request")&&!tempInfo.getTags().contains("request")){
					tempInfo.setTags( tempInfo.getTags() + "<<TAGXXX>>request");
				}else if(attrValue.equals("icon channel")&&!tempInfo.getTags().contains("channel")){
					tempInfo.setTags( tempInfo.getTags() + "<<TAGXXX>>channel");
				}else if(attrValue.equals("icon draw")&&!tempInfo.getTags().contains("draw")){
					tempInfo.setTags( tempInfo.getTags() + "<<TAGXXX>>draw");
				}else if(attrValue.equals("icon politics")&&!tempInfo.getTags().contains("politics")){
					tempInfo.setTags( tempInfo.getTags() + "<<TAGXXX>>politics");
				}else if(attrValue.equals("start")){//開始時間
					String startTime = getInnerText(arg0,arg2).replaceAll(" |\t|　|\n", "");
//					Log.d("STARTTIE"," ---- " + startTime);
					if(startTime.matches(".+")){
//						Log.d("MATCHSTART","   " + startTime);
					tempInfo.setStartTime(startTime);
					}
				}
					else if(tempInfo != null && attrValue.equals("title")){//<div class="title">種類</div>とかいうのがHTMLの上のほうにあるからtempInfo!=null ここはなんの時に呼ばれているのかよくわからん
					String title = getInnerText(arg0, arg2);
					if(NLiveRoid.isDebugMode)Log.d("TITILE===","TITI" + title);
						title.replaceAll("\t| |　|\n", "");
					if(title.matches(".+")){//文字がない場合がある
					tempInfo.setTitle(title);
					}
				}
			}else if(startTag.equals("h2")&&tempInfo != null  && tempInfo.getTitle().equals("") && nowAttr.getValue("class") != null && nowAttr.getValue("class").equals("title")){//キーワード・タグ側のタイトル
//					Log.d("H2","ATTR " + nowAttr.getValue(0));//charactersなので何回もtitle属性が来る
						//何故か何回も呼ばれるのでgetTitle().equals("")
						if(NLiveRoid.isDebugMode)Log.d("setTitle","  " + getInnerText(arg0,arg2));
						tempInfo.setTitle(getInnerText(arg0,arg2));
			}else if(startTag.equals("p")&&tempInfo != null && tempInfo.getStartTime().equals("-") && nowAttr.getValue("class") != null && nowAttr.getValue("class").equals("desc")){//キーワード・タグ側の開始時間
					if(nowAttr.getValue(0).equals("desc")){
						String inn = getInnerText(arg0,arg2);
						if(NLiveRoid.isDebugMode)Log.d("DESC","  " + inn);
						tempInfo.setStartTime(getInnerText(arg0,arg2));
					}
					infoCount = 1;//あとは来場コメ数予約数
			}else if(startTag.equals("img")){
				String attrValue = null;
				for(int i = 0; i < nowAttr.getLength(); i++){
				attrValue = nowAttr.getValue(i);
					if(attrValue.equals("img/smartphone/view.png")){
						//カンマ入ってくる
						Matcher decimal = Pattern.compile("[0-9]{1,3},[0-9]{3},[0-9]{3}|[0-9]{1,3},[0-9]{3}|[0-9]{1,3}").matcher(getInnerText(arg0,arg2));
						if(decimal.find()){
							if(NLiveRoid.isDebugMode)Log.d("NLiveRoid","setViewCount " + decimal.group());
							tempInfo.setViewCount(decimal.group());
						}
					}else if(attrValue.equals("img/smartphone/comment.png")){
						Matcher decimal = Pattern.compile("[0-9]{1,3},[0-9]{3},[0-9]{3}|[0-9]{1,3},[0-9]{3}|[0-9]{1,3}").matcher(getInnerText(arg0,arg2));
						if(decimal.find()){
							if(NLiveRoid.isDebugMode)Log.d("NLiveRoid","setResNumber " + decimal.group());
							tempInfo.setResNumber(decimal.group());
						}
					}else if(attrValue.contains("http://icon.nimg")){//ユーザーとチャンネル IDを元にサムネイル取得
						Matcher comc = copt.matcher(attrValue);
						if(comc.find()){
							//ここがchar...だから複数回呼ばれるので、co2回呼ばれるけどめんどうなので2回セットでいい
							if(NLiveRoid.isDebugMode)Log.d("NLiveRoid","setCommunityID" + comc.group());
							tempInfo.setCommunityID(comc.group());
						}else{//coじゃなければチャンネルとする
							Matcher chmc = chpt.matcher(attrValue);
							if(chmc.find()){
							//チャンネルはサムネURLでなくIDで統一
							tempInfo.setCommunityID(chmc.group());
							}
						}
					}else if(attrValue.contains("http://nl.simg.jp")){//これできない→キャッシュからの取得で、情報が繋がりようがない
						if(NLiveRoid.isDebugMode)Log.d("NLiveRoid NLMING"," " + attrValue);
							tempInfo.setThumbnailURL(attrValue);
					}else{
						Matcher thumb = Pattern.compile("thumb/[0-9]+\\..*\\.jpg|thumb/[0-9]+\\..*\\.png").matcher(attrValue);
						if(thumb.find()){
							//公式のサムネ番号はサムネURLに入れちゃう
						tempInfo.setThumbnailURL(thumb.group());
						}
					}
				}
			}else if(startTag.equals("a") && (tempInfo == null || tempInfo.getLiveID() == null)){
				for(int i = 0; i < nowAttr.getLength(); i++){
					Matcher lvmc = Pattern.compile("lv[0-9]+").matcher(nowAttr.getValue(i));
						if(lvmc.find()){//カテゴリ側のlv取得もここなので、lvがあればnewしない
							if(tempInfo == null || tempInfo.getLiveID() == null){
								if(NLiveRoid.isDebugMode)Log.d("NLiveRoid"," LV" + lvmc.group());
							tempInfo = new LiveInfo();//キーワード・タグの場合、ここでnew
							tempInfo.setLiveID(lvmc.group());
							}
						}
					}
			}else if(getInnerText(arg0,arg2).contains("('search_next').hide()")){
				finished = true;
				task.finishCallBack(liveInfos);
			}
		}
		if(infoCount >= 1)getInnerText(arg0,arg2);

	}

	@Override
	public void startElement(String arg0, String arg1, String arg2,
			Attributes arg3) throws SAXException {
		if(NLiveRoid.isDebugMode)Log.d("NLiveRoid","startTag " + arg1);
		if(arg1.equals("header") && arg3 != null && arg3.getLength() > 0){
			if(arg3.getValue(0).equals("hdg2")){//放送予定のところ
				parseTarget = false;
				finished = true;
				task.finishCallBack(liveInfos);
			}
		}else{
		startTag=arg1;
		nowAttr = arg3;
		}
	}

	@Override
	public void endDocument() throws SAXException {
		if(NLiveRoid.isDebugMode)Log.d("NLiveRoid","CEND endDocument -------------- " + startTag);
		if(!finished){
			task.finishCallBack(liveInfos);
		}
		}
	@Override
	public void endElement(String arg0, String arg1, String arg2)throws SAXException {
//		Log.d("Log","END ELEMENT " + arg1);

	if(!parseTarget)return;

	if(infoCount >= 1 && arg1.equals("li")&&tempInfo != null && tempInfo.getLiveID() != null){//キーワード・タグ側の来場者
		infoCount++;
		if(NLiveRoid.isDebugMode)Log.d("NLiveRoid","spancount " + infoCount);
		switch(infoCount){
		case 2:
			tempInfo.setViewCount(innerText.toString());
			break;
		case 3:
			tempInfo.setResNumber(innerText.toString());
			break;
		case 4:
			tempInfo.setReservedcount(innerText.toString());
			break;
		}
	}else if(infoCount >= 4 && arg1.equals("ul")&&tempInfo != null && tempInfo.getLiveID() != null){//LVを持っていて、アンカーの終わりなら1放送終了
		if(NLiveRoid.isDebugMode)Log.d("NLiveRoid","ADD KEYWORD INFO");
		liveInfos.add(tempInfo.clone());
		tempInfo.setLiveID(null);
		infoCount = 0;
		}
	}
	@Override
	public void endPrefixMapping(String arg0) throws SAXException {
		if(NLiveRoid.isDebugMode)Log.d("NLiveRoid","CEND endPrefixMapping -------------- " + startTag);
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
		if(NLiveRoid.isDebugMode)Log.d("NLiveRoid","CEND startDocument -------------- " + startTag);

	}


	@Override
	public void startPrefixMapping(String arg0, String arg1)
			throws SAXException {
		// TODO

	}




}
