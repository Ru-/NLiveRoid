package nliveroid.nlr.main.parser;

import java.util.ArrayList;
import java.util.regex.Pattern;

import nliveroid.nlr.main.NLiveRoid;
import nliveroid.nlr.main.TimeShiftDialog.TimeShiftTask;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import android.util.Log;

public class TimeShiftParser implements ContentHandler {

	private String startTag;
	private Attributes nowAttr;
	private StringBuilder innerText = new StringBuilder(1024);
	private ArrayList<String[]> infos = new ArrayList<String[]>();
	private String[] info = new String[3];
	private byte propertyCount = 0;
	private byte parseCount = 0;
	private TimeShiftTask task;
	private boolean parseEnd = false;
	public TimeShiftParser(TimeShiftTask timeShiftTask){
		this.task = timeShiftTask;
	}


	private String getInnerText(char[] arg0,int arg2){
		innerText = innerText.delete(0,arg0.length);
		innerText.append(arg0, 0, arg2);
		return innerText.toString();
	}

	@Override
	public void characters(char[] arg0, int arg1, int arg2) throws SAXException {
		if(propertyCount == 1 &&startTag.equals("b")){//一般の時はここが来ないので、h3ttlでひっかける
				info[1] = getInnerText(arg0,arg2).replaceAll("&nbsp;|件", "");//最初の1つは api-key,あと何件ご利用に,ulckがこの順番でくる
				if(NLiveRoid.isDebugMode)Log.d("NLiveRoid","Count 1: " + info[1] );
				propertyCount = 2;
		}else if(parseCount == 2){
			getInnerText(arg0,arg2);
		}
	}

	@Override
	public void startElement(String arg0, String arg1, String arg2,
			Attributes attr) throws SAXException {
//		Log.d("NLiveRoid","sTag " + arg1);
//		for(int i = 0; attr != null && i < attr.getLength(); i++){
//			Log.d("NLiveRoid", "att " + attr.getLocalName(i) + " " + attr.getValue(i));
//		}
		if(parseCount == 2||propertyCount == 1){
			startTag=arg1;
			nowAttr = attr;
		}else if(propertyCount == 0 && arg1.equals("span") && attr != null && attr.getValue("api-key") != null){
			if(NLiveRoid.isDebugMode)Log.d("NLiveRoid"," Count 0(api):" + attr.getValue("api-key"));
			propertyCount = 1;
			startTag = "";
			info[0] = attr.getValue("api-key");
			info[1] = "?";//一般はここが入ってこないので入れておく
		}else if(propertyCount == 2 && arg1.equals("input") && attr != null && attr.getValue("name") != null && attr.getValue("name").equals("confirm")){
			info[2] = attr.getValue("value");//ulck
			if(NLiveRoid.isDebugMode)Log.d("NLiveRoid"," Count 2:" + info[2]);
			infos.add(info.clone());
			info = new String[3];
			propertyCount = -1;
		}else if(parseCount == 0 && arg1.equals("div") && attr != null && attr.getValue("class") != null ){
			if(attr.getValue("class").equals("column")){
				if(NLiveRoid.isDebugMode)Log.d("NLiveRoid"," Count 0:");//新たな放送開始
				parseCount = 1;//ここで2にして↓のifでLVとタイトルが入ってるはず
			}else if(!parseEnd && attr.getValue("id") != null && attr.getValue("id").equals("Favorite_list")){//パース終わり
		 		parseEnd = true;
				Log.d("NLiveRoid"," Favorite_list:");
		 		task.finishCallBack(infos);
			}
		}else if(parseCount == 1 && arg1.equals("a")){
			if(NLiveRoid.isDebugMode)Log.d("NLiveRoid"," Count 1:");
			info[0] = attr.getValue("href").replaceAll("http://live.nicovideo.jp/gate/", "");
			info[1] = attr.getValue("title").replaceAll("\n|\t", "");
			startTag = "";
			parseCount = 2;//ここでchar取得するモードにして次のspanの終わりタグの時にステータスが入る
		}
	}
	@Override
	public void endElement(String arg0, String arg1, String arg2)throws SAXException {
//		Log.d("NLiveRoid","eTag" + arg2);
		if(propertyCount == 1 && arg1.equals("h3"))propertyCount = 2;//一般は件数が表示されない
		if(parseCount == 2 && arg1.equals("span")){//終わりタグがspanなら中のテキストでステータスを全て判断
			info[2] = innerText.toString().replaceAll("\n|\t| |　", "");;
			if(NLiveRoid.isDebugMode)Log.d("NLiveRoid"," Count 2:" + info[2]);
			infos.add(info.clone());
			info = new String[3];
			parseCount = 0;
		}
	}

	@Override
	public void endDocument() throws SAXException {
			if(!parseEnd){
				if(NLiveRoid.isDebugMode)Log.d("NLiveRoid","ParseFailed TS..");
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
