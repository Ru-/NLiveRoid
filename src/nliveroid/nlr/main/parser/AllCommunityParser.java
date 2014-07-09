package nliveroid.nlr.main.parser;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nliveroid.nlr.main.FinishCallBacks;
import nliveroid.nlr.main.LiveInfo;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import android.util.Log;

public class AllCommunityParser implements ContentHandler {

	private String startTag;
	private Attributes nowAttr;
	private StringBuilder innerText = new StringBuilder(1024);
	private ArrayList<LiveInfo> liveInfos = new ArrayList<LiveInfo>();
	private LiveInfo tempInfo = new LiveInfo();
	private String infoMaster = "";
	private String communityAmount = "";//Interfaceの関係上String型、今表示しているX ～ XはPagerViewの今表示しているページと総数を使うので結局は総数のみでよい
	private boolean amountTarget = false;//
	private int strongTarget = 0;
	private FinishCallBacks task;
	private Pattern copt = Pattern.compile("co[0-9]+");
	private Pattern figurespt = Pattern.compile("[0-9]++");
	public AllCommunityParser(FinishCallBacks communityTask){
		this.task = communityTask;
	}


	private String getInnerText(char[] arg0,int arg2){
		innerText = innerText.delete(0,arg0.length);
		innerText.append(arg0, 0, arg2);
		return innerText.toString();
	}

	@Override
	public void characters(char[] arg0, int arg1, int arg2) throws SAXException {
//		Log.d("NLiveRoid","char " + getInnerText(arg0,arg2));
			if(startTag.equals("p")&&nowAttr != null &&nowAttr.getValue("class") != null ){
				if(nowAttr.getValue("class").equals("fs10")){
					strongTarget = 1;
				}else if(strongTarget == 4 && nowAttr.getValue("class").equals("date")){//最終更新日時
					infoMaster += getInnerText(arg0,arg2).replaceAll("\t|\n", "") + "<<SPLIT>>";
					strongTarget++;
					Log.d("NLiveRoid","STRONG " +strongTarget + "  " + infoMaster);
				}else if(strongTarget == 6 && nowAttr.getValue("class").equals("desc")){
					String coDesc = getInnerText(arg0,arg2);
					if(coDesc != null && !coDesc.equals("")){
						//ほとんどないけどもしコミュの詳細に<<SPLIT>>があったら、えげつないことになるので適当に変えておく
						if(coDesc.contains("<<SPLIT>>")){
							coDesc.replaceAll("<<SPLIT>>", "_split_");
						}
						infoMaster += coDesc.replaceAll("\t|\n", "") + "<<SPLIT>>";//コミュの説明
						strongTarget++;
//						Log.d("NLiveRoid","STRONG " +strongTarget + "  " + infoMaster);
					}
				}
			}else if(startTag.equals("strong")){
				if(amountTarget){
							Log.d("NLiveRoid","getInnerText " + getInnerText(arg0,arg2));
						Matcher nummc = figurespt.matcher(getInnerText(arg0,arg2).replaceAll("\t|\n", ""));
						if(nummc.find()){
						communityAmount = nummc.group();
//						Log.d("NLiveRoid","STRONG AMOUNT FINDED " +amountTarget + "  " + communityAmount);
						}
						amountTarget = false;
						Log.d("NLiveRoid","STRONG AMOUNT END" +amountTarget + "  " + communityAmount);
				}else if(strongTarget >= 1 && strongTarget <= 4){//レベル、メンバー、投稿動画の順で入る
					infoMaster += getInnerText(arg0,arg2).replaceAll("\t|\n", "") + "<<SPLIT>>";
					strongTarget++;
//					Log.d("NLiveRoid","STRONG " +strongTarget + "  " + infoMaster);
				}
			}else if(strongTarget == 5 && startTag.equals("a") && nowAttr != null && nowAttr.getValue("href") != null
					&& !nowAttr.getValue("href").equals("")){
				Matcher comc = copt.matcher(nowAttr.getValue("href"));
				if(comc.find()){
					tempInfo.setCommunityID(comc.group());//コミュニティID
				}
				String coName = getInnerText(arg0,arg2);
				if(coName != null && !coName.equals("")){
				tempInfo.setComunityName(coName.replaceAll("\t|\n", ""));//コミュニティ名
				}
				strongTarget++;
				Log.d("NLiveRoid","STRONG " +strongTarget + "  " + infoMaster);
			}else if(!amountTarget && startTag.equals("div") &&  nowAttr != null && nowAttr.getValue("class") != null
					&& nowAttr.getValue("class").equals("pagelink")){
//				Log.d("PAGERLINK ", " -------- " + amountTarget);
				amountTarget = true;
			}

	}

	@Override
	public void startElement(String arg0, String arg1, String arg2,
			Attributes arg3) throws SAXException {
		startTag=arg1;
		nowAttr = arg3;
		Log.d("NLiveRoid","startTag " + startTag);
		if(strongTarget == 7){
			if(startTag.equals("img") && nowAttr != null && nowAttr.getValue("src") != null){
//				Log.d("ATTR---- " , " " + nowAttr.getValue("src"));
				if(nowAttr.getValue("src").matches(".*icn_mstatLiveOn.*")){
					infoMaster += "1<<SPLIT>>";
				}else{
					infoMaster += "0<<SPLIT>>";
				}
				strongTarget++;
				Log.d("NLiveRoid","STRONG " +strongTarget + "  " + infoMaster);
			}
		}else if(strongTarget == 8){
			if(startTag.equals("img") && nowAttr != null && nowAttr.getValue("src") != null){
//				Log.d("ATTR---XXX- " , " " + nowAttr.getValue("src"));
				if(nowAttr.getValue("src").matches(".*icn_mstatBackstageON.*")){
					infoMaster += "1";//最後はSPLIT付けない
				}else{
					infoMaster += "0";
				}
				Log.d("NLiveRoid","ADDED tmpInfo " +strongTarget + "  " + infoMaster);
				tempInfo.setCommunity_info(infoMaster);
				infoMaster = "";
				strongTarget = 0;//1コミュできたら戻す
				liveInfos.add(tempInfo.clone());
				tempInfo = new LiveInfo();
			}
		}
	}

	@Override
	public void endDocument() throws SAXException {}
	@Override
	public void endElement(String arg0, String arg1, String arg2)throws SAXException {
		if(arg1.equals("body")){
			task.finishCallBack(liveInfos,communityAmount);
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
