package nliveroid.nlr.main.parser;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nliveroid.nlr.main.BackGroundService;
import nliveroid.nlr.main.BackGroundService.AlertParseTask;
import nliveroid.nlr.main.ErrorCode;
import nliveroid.nlr.main.LiveInfo;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import android.graphics.Bitmap;
import android.os.AsyncTask;

public class AlertParser  implements ContentHandler {

	private String startTag;
	private Attributes nowAttr;
	private StringBuilder innerText = new StringBuilder(1024);
	private ArrayList<LiveInfo> liveInfos = new ArrayList<LiveInfo>();
	private ArrayList<Bitmap> thumbNails = new ArrayList<Bitmap>();
	private LiveInfo tempInfo;
	private boolean parseTarget = false;
	private BackGroundService.AlertParseTask task;
	private boolean isExsistCommunity = false;
	private ErrorCode error;//最初のパースとサムネイル取得に必要
	public AlertParser(AlertParseTask task,ErrorCode error){
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
		if(startTag.equals("script")){
			//単純にco　lvのマッチング
			//lvは途中スクリプトタグが間に入っちゃう
			String scriptInner = getInnerText(arg0, arg2);
			scriptInner = scriptInner.replaceAll("\n","");
			Matcher co = Pattern.compile("co[0-9]{1,12}|ch[0-9]{1,12}").matcher(scriptInner);
			Matcher lv = Pattern.compile("lv[0-9]{1,12}").matcher(scriptInner);
			Matcher vid = Pattern.compile("[0-9]+\",\"vid\"").matcher(scriptInner);
			if(co.find()){
				String communityID = co.group();
				tempInfo.setCommunityID(communityID);
			}
			if(lv.find()){
				tempInfo.setLiveID(lv.group());
			}else if(vid.find()){
				tempInfo.setLiveID(tempInfo.getLiveID()+vid.group().split("\"")[0]);
			}
			}else if(startTag.equals("div")&&nowAttr != null && nowAttr.getLength() > 0){
				//Attribute取得処理
				String attrValue = nowAttr.getValue(0);
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
					String startTime = getInnerText(arg0,arg2);
					if(startTime.matches(".+")){
					tempInfo.setStartTime(startTime);
					}
				}else if(attrValue.equals("title")){
					String title = getInnerText(arg0, arg2);
						title.replaceAll("\t| |　|\n", "");
					if(title.matches(".+")){//文字がない場合がある
					tempInfo.setTitle(title);
					}

				}

			}else if(startTag.equals("img")&&nowAttr != null && nowAttr.getLength() > 0){
				String attrValue = nowAttr.getValue(0);
				if(attrValue.equals("img/smartphone/view.png")){
					Matcher decimal = Pattern.compile("[0-9]{1,3},[0-9]{3},[0-9]{3}|[0-9]{1,3},[0-9]{3}|[0-9]{1,3}").matcher(getInnerText(arg0,arg2));
					if(decimal.find()){
						tempInfo.setViewCount(decimal.group());
					}
				}else if(attrValue.equals("img/smartphone/comment.png")){
					//カンマ入ってくる
					Matcher decimal = Pattern.compile("[0-9]{1,3},[0-9]{3},[0-9]{3}|[0-9]{1,3},[0-9]{3}|[0-9]{1,3}").matcher(getInnerText(arg0,arg2));
					if(decimal.find()){
						tempInfo.setResNumber(decimal.group());
					}
			}

			}
		}

		//メンテ中
		if(startTag.equals("h2")){
			for(int i= 0; i < nowAttr.getLength(); i++){
				if(nowAttr.getValue(i).contains("メンテナンス中")){
					task.finishCallBack(true);
				}
			}
		}

	}

	@Override
	public void startElement(String arg0, String arg1, String arg2,
			Attributes arg3) throws SAXException {
		startTag=arg1;
		nowAttr = arg3;
		if(startTag.equals("div")&&arg3 != null && arg3.getLength() > 0 ){//最初と最後チャンネル見えたら終わり
			String attrValue = arg3.getValue(0);
			if(attrValue.equals("result clearfix")){
				if(tempInfo != null){//2放送目以降なら1放送分保存していく
				liveInfos.add(tempInfo.clone());
				}
			parseTarget = true;
			isExsistCommunity = true;
			tempInfo = new LiveInfo();
		}
	}



	}

	@Override
	public void endDocument() throws SAXException {
		// TODO
	}

	@Override
	public void endElement(String arg0, String arg1, String arg2)
			throws SAXException {

		if(parseTarget&&arg1.equals("section")){//footerじゃ絶対駄目
			parseTarget = false;
			if(tempInfo != null && tempInfo.getLiveID() != null && tempInfo.getLiveID().matches("lv[0-9]+")){
			liveInfos.add(tempInfo);
			}
			task.finishCallBack(liveInfos);
		}else if(arg1.equals("footer")&&!isExsistCommunity){//h1&&parseTargetが5以上繰ることはないはず
			task.finishCallBack(liveInfos);//参加中無かった
		}
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