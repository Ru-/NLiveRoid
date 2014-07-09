package nliveroid.nlr.main.parser;

import java.util.ArrayList;

import nliveroid.nlr.main.ErrorCode;
import nliveroid.nlr.main.LiveTab.FirstEditAccess;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import android.util.Log;

public class FirstEditAccessParser implements ContentHandler {

	private Attributes nowAttr;
	private StringBuilder innerText = new StringBuilder(1024);
	private ArrayList<String> communitys = new ArrayList<String>();
	private ArrayList<String> commuids = new ArrayList<String>();
	private String[] reserveValues = new String[4];
	private int reserveValIndex = 0;
	private boolean communityTarget = false;
	private boolean reserveTarget = false;
	private boolean endParse = false;
	private boolean notPremium = false;
	private int commuCheckIndex = 0;
	private int nowCommuIndex = 0;
	private String commuName = "";
	private String reuseid = "";
	private FirstEditAccess task;
	private ErrorCode error;//最初のパースとサムネイル取得に必要
	private boolean reuseTarget;
	public FirstEditAccessParser(FirstEditAccess task,ErrorCode error){
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
		if(communityTarget){//複数行に渡ることがあるので、インデックスで確かめながらパース
			String name = getInnerText(arg0,arg2);
			if(commuCheckIndex == nowCommuIndex+1){
				commuName += name;
//				Log.d("Log","TEMP NAME " + name);
			}else if(commuCheckIndex == nowCommuIndex+2){
			communitys.add(new String(commuName));
//			Log.d("Log","NAME --- " + communitys.get(communitys.size()-1));
			commuName = name;//ここで+じゃなく=
			nowCommuIndex++;
			}
		}
	}

	@Override
	public void startElement(String arg0, String arg1, String arg2,
			Attributes arg3) throws SAXException {
//		Log.d("log"," "  + arg1);
		if(arg1.equals("div") && arg3 != null && arg3.getLength() ==1){
			if(arg3.getValue(0).equals("error_box_steps")){
				//プレアカじゃない可能性（プレアカ時、混雑?でもなった）
				notPremium = true;
				communitys.add("notpremium");
				task.finishCallBack(communitys,commuids,reserveValues,reuseid);
			}
		}
		if(arg1.equals("h2")&&arg3 != null){
			for(int i = 0; i < arg3.getLength(); i++){
				if(arg3.getValue(i).equals("middleh2 recycleh2")){
					reuseTarget = true;
					Log.d("FIREST -------- ", "reuseTarget = true -----------");
				}
			}
		}
		if(reuseTarget && arg1.equals("a")&& arg3 != null){
			for(int i = 0; i < arg3.getLength(); i++){
				if(arg3.getValue(i).contains("reuseid=")){
					reuseid = arg3.getValue(i).replaceAll("~[0-9]+", "");
					Log.d("FIREST -------- ", "REUSE ID " + reuseid);
					reuseTarget = false;
				}
			}
		}
		if(arg1.equals("select") && arg3 != null){
			for(int i = 0; i < arg3.getLength();i++){
				if(arg3.getValue(i).equals("default_community")){
					communityTarget = true;
					//idとnameで2回呼ばれるのを防ぐ
					break;
				}
				//endParseで2回finishCallBackが呼ばれるのを防ぐ
				if(!endParse&&arg3.getValue(i).equals("reserve_start_ymd")||arg3.getValue(i).equals("reserve_start_h")
						||arg3.getValue(i).equals("reserve_start_i")||arg3.getValue(i).equals("reserve_stream_time")){
					reserveTarget = true;
					//idとnameで2回呼ばれるのを防ぐ
					break;
				}
			}
		}
		if(communityTarget&& arg1.equals("option")){
			for(int i = 0; i < arg3.getLength(); i++){
				if(!arg3.getValue(i).replaceAll("\n|\t| |　","").equals("")&&arg3.getValue(i).matches("co[0-9]+")){
//					Log.d("og","COMMU " + arg3.getValue(i));
					commuCheckIndex++;
					commuids.add(arg3.getValue(i));
				}
			}
		}
		//valueとselectedの2つ属性を持っているものを抽出
		if(reserveTarget&& arg1.equals("option")&&arg3 != null && arg3.getLength() >= 2){
			reserveTarget = false;
//			Log.d("Log","RESERVE VALUE LENGTH --------- " + arg3.getLength());
			String temp = "";
			for(int i = 0; i < arg3.getLength(); i++){
				temp = arg3.getValue(i).replaceAll("\n|\t| |　","");
				if(!temp.equals("")&&temp.matches("^[0-9]+$")){
					reserveValues[reserveValIndex] = temp;
					reserveValIndex++;
					if(reserveValIndex >= 3){
						endParse = true;
						task.finishCallBack(communitys,commuids,reserveValues,reuseid);
					}
					break;
				}
			}
		}
	}

	@Override
	public void endElement(String arg0, String arg1, String arg2)throws SAXException {
		if(arg1.equals("td")&&communityTarget&&!notPremium){
			communityTarget = false;
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
