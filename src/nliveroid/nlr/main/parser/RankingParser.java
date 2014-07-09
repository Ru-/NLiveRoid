package nliveroid.nlr.main.parser;

import java.util.ArrayList;
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

public class RankingParser implements ContentHandler {

	private String startTag;
	private String endTag;
	private boolean finished = false;
	private int view_or_comment = 0;
	private Attributes nowAttr;
	private StringBuilder innerText = new StringBuilder(1024);
	private ArrayList<LiveInfo> liveInfos ;
	private LiveInfo tempInfo = new LiveInfo();//毎回nullチェックしなくていいように最初の1つを入れておく
	private SearchTask task;
	private final Pattern lvpt = Pattern.compile("lv[0-9]+");
	private final Pattern numpt = Pattern.compile("[0-9]+");
	private final Pattern copt = Pattern.compile("co[0-9]{5,10}");
	private final Pattern chpt = Pattern.compile("ch[0-9]{2,10}");
	private final Pattern timept = Pattern.compile("[0-9]{0,2}時{0,1}間{0,1}[0-9]{1,2}");
	public RankingParser(SearchTask task,ErrorCode error){
		this.task = task;
		this.liveInfos = new ArrayList<LiveInfo>();
	}


	private String getInnerText(char[] arg0,int arg2){
		innerText = innerText.delete(0,arg0.length);
		innerText.append(arg0, 0, arg2);
		return innerText.toString();
	}

	@Override
	public void characters(char[] arg0, int arg1, int arg2) throws SAXException {
		Log.d("NLiveRoid","characters ST" + startTag);


		//rankingValueのshortで上位1バイトは0=UP,1=DOWN,2=EVENになってる
		if(startTag.equals("strong")||startTag.equals("span")||startTag.equals("p")||startTag.equals("h2")||startTag.equals("li")){//endElementでの処理の為
			getInnerText(arg0,arg2);
		}

	}

	@Override
	public void startElement(String arg0, String arg1, String arg2,
			Attributes arg3) throws SAXException {
		startTag=arg1;
		nowAttr = arg3;
		Log.d("NLiveRoid","startElement ST" + startTag);
		if(arg1.equals("a")&& arg3.getValue("class") != null && arg3.getValue("class").contains("btn_inner")){
			String lv = arg3.getValue("href").replace("watch/", "");
			tempInfo = new LiveInfo();
			tempInfo.setLiveID(lv);
			Log.d("NLiveRoid","startElement LV NEW --------" +tempInfo.getLiveID());
		}else if(startTag.equals("img") && tempInfo.getLiveID() != null && !tempInfo.getThumbnailURL().equals("")){
			if(NLiveRoid.isDebugMode)Log.d("NLiveRoid","src--------- "+nowAttr.getValue("src"));
			tempInfo.setThumbnailURL(nowAttr.getValue("src"));
		}else if(startTag.equals("img") && nowAttr.getValue("src") != null){
			String url = nowAttr.getValue("src").replaceAll("http.+/|.jpg\\?[0-9]+", "");
			if(NLiveRoid.isDebugMode)Log.d("NLiveRoid","url:" + url);
			if(url.matches("co[0-9]+")){
				tempInfo.setCommunityID(url);
			}
			if(NLiveRoid.isDebugMode)Log.d("NLiveRoid","co:" + tempInfo.getCommunityID());
			tempInfo.setThumbnailURL(nowAttr.getValue("src"));
		}
	}

	@Override
	public void endDocument() throws SAXException {
			task.finishCallBack(liveInfos);
	}
	@Override
	public void endElement(String arg0, String arg1, String arg2)throws SAXException {
		Log.d("NLiveRoid","endElement " + arg1);
		Log.d("NLiveRoid","endElement ST" + startTag);
		endTag = arg1;


		if(arg1.equals("strong") && nowAttr.getValue("class") != null && nowAttr.getValue("class").equals("number")){
			String rankValue = innerText.toString().replaceAll("\t|\n| |　", "");
			if(rankValue.matches("[0-9]{1,2}")){
				if(NLiveRoid.isDebugMode)Log.d("NLiveRoid","rankValue--------- "+rankValue);
			tempInfo.setRankingValue(Short.parseShort(rankValue));
			}
		}else if(arg1.equals("span") && nowAttr.getValue("class") != null){
			if(nowAttr.getValue("class").contains("updown")){
				//やじるしの処理(ランクの値(1〜50はcharactorで処理))、rankingValueのshortで上位1バイトは0=UP,1=DOWN,2=EVENになってる
				String updown = innerText.toString().replaceAll("\t|\n| |　", "");
				if(updown.matches("→|↑|↓")){
					if(NLiveRoid.isDebugMode)Log.d("NLiveRoid","updown--------- "+updown);
					if(updown.equals("↓")){
					tempInfo.setRankingValue((short) (tempInfo.getRankingValue() | 0x0100));
					}else if(updown.equals("→")){
						tempInfo.setRankingValue((short) (tempInfo.getRankingValue() | 0x0200));
					}
				}
			}else if(view_or_comment == 0 && nowAttr.getValue("class").contains("label")){
				view_or_comment = 1;
			}
		}else if(arg1.equals("h2") && nowAttr.getValue("class") != null && nowAttr.getValue("class").equals("title")){
			String title = innerText.toString().replaceAll("\t|\n| |　", "");
			if(title.matches(".+")){
				if(NLiveRoid.isDebugMode)Log.d("NLiveRoid","title--------- "+title);
			tempInfo.setTitle(title);
			}
		}else if(arg1.equals("p") && nowAttr.getValue("class") != null && nowAttr.getValue("class").equals("desc")){
			String passedTime = innerText.toString().replaceAll("\t|\n| |　", "");
			if(NLiveRoid.isDebugMode)Log.d("NLiveRoid","passedTime--------- "+passedTime);
			if(passedTime.matches(".+")){
				if(NLiveRoid.isDebugMode)Log.d("NLiveRoid","passedTimeOK--------- "+passedTime);
			tempInfo.setPassedTime(passedTime);
			}
		}else if(arg1.equals("li") && nowAttr.getValue("class") != null && nowAttr.getValue("class").equals("desc")){
			String passedTime = innerText.toString().replaceAll("\t|\n| |　", "");
			if(passedTime.matches("分")){
				if(NLiveRoid.isDebugMode)Log.d("NLiveRoid","passedTime--------- "+passedTime);
				//経過時間は表示するところでactiveと経過時間に分けている
			tempInfo.setPassedTime("<<SPLIT>>" +passedTime);
			}
		}else if(view_or_comment == 1&& arg1.equals("li") ){
			String views = innerText.toString();
			if(NLiveRoid.isDebugMode)Log.d("NLiveRoid","views--------- "+views);
			tempInfo.setViewCount(views);
			view_or_comment = 2;
		}else if(view_or_comment == 2 && arg1.equals("li")){
			String comment = innerText.toString();
			if(NLiveRoid.isDebugMode)Log.d("NLiveRoid","comment ADD--------- "+comment);
			tempInfo.setViewCount(comment);
			view_or_comment = 0;
			liveInfos.add(tempInfo.clone());
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
	}


	@Override
	public void startPrefixMapping(String arg0, String arg1)
			throws SAXException {
		// TODO

	}




}
