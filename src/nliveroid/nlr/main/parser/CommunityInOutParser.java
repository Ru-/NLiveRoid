package nliveroid.nlr.main.parser;

import nliveroid.nlr.main.CommunityInfoTask;
import nliveroid.nlr.main.ErrorCode;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import android.util.Log;

public class CommunityInOutParser implements ContentHandler {
		private boolean joinForm = false;
		private boolean parseFinished = false;
		private CommunityInfoTask task;
		private StringBuilder innerText = new StringBuilder(1024);
		private boolean alreadyJoinOrOver50=false;
		private ErrorCode error;//最初のパースとサムネイル取得に必要
		public CommunityInOutParser(CommunityInfoTask communityInfoTask,ErrorCode error){
			this.task = communityInfoTask;
			this.error = error;
		}

		private String getInnerText(char[] arg0,int arg2){
			innerText = innerText.delete(0,arg0.length);
			innerText.append(arg0, 0, arg2);
			return innerText.toString();
		}
		@Override
		public void characters(char[] arg0, int arg1, int arg2) throws SAXException {
			if(!parseFinished){
			if(joinForm){
				String inner = getInnerText(arg0,arg2);
				Log.d("#Log","PARSER ------  XXX " +inner);
				if(inner.contains("コミュニティに参加申請を送る")){
					task.finishCallBack(error, false,true);
					parseFinished = true;
				}else if(inner.contains("コミュニティに登録申請を送る")){
					task.finishCallBack(error, false,true);
					parseFinished = true;
				}else if(inner.contains("ティを登録する")){
					task.finishCallBack(error, false,false);
					parseFinished = true;
				}
			}else if(alreadyJoinOrOver50){
				String inner = getInnerText(arg0,arg2);
				Log.d("logttt",""+inner);
				if(inner.contains("このコミュニティには")){//すでに参加している
					task.finishCallBack(error, false,false);//この時isApplyは意味ない
					parseFinished = true;
				}
			}
			}
		}

		@Override
		public void startElement(String arg0, String arg1, String arg2,
				Attributes arg3) throws SAXException {

			if(arg1.equals("div") && arg3 != null && arg3.getLength() >=1){
				for(int i = 0; i < arg3.getLength(); i++){
					if(arg3.getValue(i).equals("main0727")){//参加申請またはまだ参加してないことが決定
//						Log.d("#Log","PARSER ------  form + isJoined==false" );
						joinForm = true;
					}
					if(!parseFinished&&arg3.getValue(i).equals("mb16p4")){//参加してるか一般50を超えている
						if(!joinForm){
//							Log.d("#Log","PARSER ------  joined "   );
							alreadyJoinOrOver50=true;
						}
					}
				}
			}


		}

		@Override
		public void endElement(String arg0, String arg1, String arg2)throws SAXException {
			if(arg1.equals("p")&&alreadyJoinOrOver50&&!parseFinished){//50を超えている→機能してない
				error.setErrorCode(-45);
				task.finishCallBack(error,false,false);
				joinForm = false;
				parseFinished = true;
			}
			if(arg1.equals("body")&&!parseFinished){//不明のエラー
				error.setErrorCode(-40);
				task.finishCallBack(error,false,false);
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
