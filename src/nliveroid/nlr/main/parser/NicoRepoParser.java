package nliveroid.nlr.main.parser;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nliveroid.nlr.main.HistoryTab.NicoRepoTask;
import nliveroid.nlr.main.LiveInfo;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import android.util.Log;

public class NicoRepoParser  implements ContentHandler{

	private NicoRepoTask task;
	private ArrayList<LiveInfo> liveInfos = new ArrayList<LiveInfo>();
	private LiveInfo tempInfo = new LiveInfo();
	private boolean target;
	private byte pCount;
	private StringBuilder innerText = new StringBuilder(1024);
	private Pattern copt = Pattern.compile("co[0-9]+");
	private Pattern lvpt = Pattern.compile("lv[0-9]+");
	private Pattern userpt = Pattern.compile("user/[0-9]+");
	private String startTag;
	private byte filter;
	private Attributes befoerAttr;

	public NicoRepoParser(NicoRepoTask task,byte filter) {
		this.task = task;
		this.filter = filter;
//		Log.d("NLiveRoid"," REPOINDEX" + filter);
	}

	private String getInnerText(char[] arg0,int arg2){
		innerText = innerText.delete(0,arg0.length);
		innerText.append(arg0, 0, arg2);
		return innerText.toString();
	}

	@Override
	public void characters(char[] ac, int i, int j) throws SAXException {
//		if(pCount >= 1)Log.d("CCC"," " + startTag +" " + getInnerText(ac,j));
		if(target && startTag != null && pCount == 0 && startTag.equals("p")){
			pCount = 1;//ここでpCount足すけど、他でpタグだからって足すとstartTag基準にしてるから、endElementのあともcharactersが呼ばれるから難しくなる
//			Log.d("KKK", " " + getInnerText(ac,j));
			if(filter == 1){
				tempInfo.setOwnerName(getInnerText(ac,j).replaceAll("\t|\n", "") + tempInfo.getOwnerName());
			}else{
				tempInfo.setComunityName(getInnerText(ac,j).replaceAll("\t|\n", ""));
			}
		}else if(pCount == 2 && startTag != null && startTag.equals("p")){
			pCount = 3;
		}else if(pCount == 3 && startTag != null && startTag.equals("p")){
			tempInfo.setTitle(getInnerText(ac,j).replaceAll("\t|\n", ""));
			pCount = 4;
		}else if(pCount == 4 && startTag != null && startTag.equals("span")){
			tempInfo.setCommunity_info(getInnerText(ac,j).replaceAll("\t|\n", ""));
			pCount = 0;
		}
	}

	@Override
	public void startElement(String s, String s1, String s2,
			Attributes attributes) throws SAXException {

//		if(pCount > 0)Log.d("NNNN",pCount + " L " + s1);
//		if(s1.equals("a") && attributes.getValue("href")!= null)Log.d("NNN ", "AAAA " + attributes.getValue("href"));

		if(s1.equals("section") && attributes != null && attributes.getValue("class") != null && attributes.getValue("class").equals("nicorepoUser cf")){
				target = true;
//				Log.d("FFFF"," TARGET TRUE");
				if(befoerAttr != null && befoerAttr.getValue("href") != null){//sectionの一つ前にcoかuserがある→それ以外あるか不明
					String coUrl = befoerAttr.getValue("href");
					Matcher mc = copt.matcher(coUrl);
					if(mc.find()){
						tempInfo.setCommunityID(mc.group());
					}else{
						Matcher mcu = userpt.matcher(coUrl);
						if(mcu.find()){
							tempInfo.setOwnerName("<<USERID>>" + mcu.group());//ユーザーIDだったら
						}
					}
				}
			}else if(target && s1.equals("img") && attributes.getValue("src") != null){
				String thumbURL = attributes.getValue("src");
				tempInfo.setThumbnailURL(thumbURL);
				Matcher mc = copt.matcher(thumbURL);
				if(mc.find()){
					if(filter == 1){
						tempInfo.setOwnerName(mc.group());//自分フィルタの場合は、主名に入れておく
					}else{
						tempInfo.setCommunityID(mc.group());
					}
				}
			}else if(pCount == 1 && s1.equals("a")&& attributes.getValue("href") != null){
//				Log.d("FFFF"," LVGET -- " + attributes.getValue("href"));
				Matcher mc = lvpt.matcher(attributes.getValue("href"));
				if(mc.find()){
				tempInfo.setLiveID(mc.group());
				}
				pCount = 2;
			}else{
				startTag = s1;
			}
		befoerAttr = attributes;
	}

	@Override
	public void endElement(String s, String s1, String s2) throws SAXException {
		if(target && s1.equals("li")){
			target = false;
//			Log.d("FFFF"," TARGET FALSE");
			liveInfos.add(tempInfo.clone());
			tempInfo = new LiveInfo();
		}else if(s1.equals("body")){
				task.onPageFinished(liveInfos);
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
