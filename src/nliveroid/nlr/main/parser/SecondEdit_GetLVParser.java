package nliveroid.nlr.main.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nliveroid.nlr.main.ErrorCode;
import nliveroid.nlr.main.LiveTab.SecondSendForm_GetLVTask;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import android.util.Log;

/**
 * 放送開始した際に
 * 放送情報を取得する
 * LiveTabのみで利用される
 * @author Owner
 *
 */
public class SecondEdit_GetLVParser  implements ContentHandler {

	private StringBuilder innerText = new StringBuilder(1024);
	private boolean parseTarget = false;
	private boolean ancherTarget = false;
	private boolean lvFinded = false;
	private String[] ulck_desc = new String[2];//[0]ulck,[1]description
	private SecondSendForm_GetLVTask task;
	public SecondEdit_GetLVParser(SecondSendForm_GetLVTask task,ErrorCode error){
		this.task = task;
	}

	private String getInnerText(char[] arg0,int arg2){
		innerText = innerText.delete(0,arg0.length);
		innerText.append(arg0, 0, arg2);
		return innerText.toString();
	}

	@Override
	public void characters(char[] arg0, int arg1, int arg2) throws SAXException {
//		Log.d("NLR","INNER ------- " + aa);
		if(parseTarget){
			String inner = getInnerText(arg0,arg2);
//			Log.d("log","TASK FINISH ------- " + inner);
			if(inner.contains("既にこの時間に予約をしている")||inner.contains("配信開始を押すまで、一覧には表示されません")){
				ancherTarget = true;
			}else if(inner.contains("既に順番待ちに並んでいるか")){
				ancherTarget = true;
			}else if(inner.contains("WEBページの有効期限が切れています")){
				lvFinded = true;//後のリトライをさせない為
				task.finishCallBack("RETRYW"+ulck_desc[0]);
			}else if(inner.contains("混み合っております")){
				lvFinded = true;//後のリトライをさせない為
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				task.finishCallBack("RETRYC");
			}else{
				lvFinded = true;//後のリトライをさせない為
				task.finishCallBack(inner);
			}
			parseTarget = false;
		}

		if(ancherTarget){
			getInnerText(arg0,arg2);//Getting innerText
		}
	}

	@Override
	public void startElement(String arg0, String arg1, String arg2,
			Attributes arg3) throws SAXException {
//		Log.d("NLR","1elem " + arg1);
//		Log.d("NLR","2elem " + arg2);
		if(arg1.equals("li") && arg3 != null && arg3.getLength()>0){
			for(int i = 0; i < arg3.getLength(); i++){
				if(arg3.getValue(i).equals("error_message")){
					Log.d("NLR","SECOND error_message --- ");
					parseTarget = true;
				}
			}
		}
		if(arg1.equals("input")){
			for(int i = 0; i < arg3.getLength(); i++){
				if(arg3.getLocalName(i).equals("name")){
					if(arg3.getValue(i).equals("description")){
						for(int j = 0; j < arg3.getLength(); j++){
							if(arg3.getLocalName(j).equals("value")){
								ulck_desc[1] = arg3.getValue(j);
							}
						}
					}else if(arg3.getValue(i).equals("usecoupon")||arg3.getValue(i).equals("confirm")){
					Pattern pt = Pattern.compile("ulck_[0-9]+");
					Matcher mc = null;
					for(int j = 0; j < arg3.getLength(); j++){
						mc = pt.matcher(arg3.getValue(j));
						if(mc.find()){
							ulck_desc[0] = mc.group();
					    	}
					   }
					}
				}
			}
		}
		if(arg1.equals("div")&&arg3!=null){
			for(int i = 0; i < arg3.getLength()&&!lvFinded; i++){
//				Log.d("NLR","DIV --- val  " + arg3.getValue(i));
				if(arg3.getValue(i).equals("page_footer")){
					task.finishCallBack("RETRY");//ulckなし+ページ終わり=枠取り失敗 これが不明
				}
			}
		}
	}

	@Override
	public void endElement(String arg0, String arg1, String arg2)throws SAXException {
		if(ancherTarget){
				Matcher glmc = Pattern.compile("lv[0-9]+").matcher(innerText);
				if(glmc.find()){
					Log.d("NLR","find" );
						String lv = glmc.group();
						lvFinded = true;
						parseTarget = false;
						ancherTarget = false;
						task.finishCallBack(lv);
					}
		}
		if(ulck_desc!=null&&ulck_desc[0] != null && !ulck_desc[0].equals("")&&ulck_desc[1]!=null&&!ulck_desc[1].equals(""))task.finishCallBack(ulck_desc);
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
