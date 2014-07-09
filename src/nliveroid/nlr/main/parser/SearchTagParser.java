package nliveroid.nlr.main.parser;

import java.util.ArrayList;

import nliveroid.nlr.main.ErrorCode;
import nliveroid.nlr.main.NLiveRoid;
import nliveroid.nlr.main.SearchTab.TagParseTask;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import android.util.Log;

public class SearchTagParser implements ContentHandler {
		private ArrayList<String> result = new ArrayList<String>();
		private StringBuilder innerText = new StringBuilder(1024);
		private String startTag = "";//いれないと一番最初にNULLになる
		private boolean parseTarget = true;
		private boolean isFinished;
		private TagParseTask task;
		private ErrorCode error;//最初のパースとサムネイル取得に必要
		public SearchTagParser(TagParseTask task,ErrorCode error){
			this.task = task;
			this.error = error;
		}


		private String getInnerText(char[] arg0,int arg2){
			innerText = innerText.delete(0,arg0.length);
			innerText.append(arg0, 0, arg2);
			return innerText.toString();
		}


		@Override
		public void startElement(String arg0, String arg1, String arg2,
				Attributes attr) throws SAXException {
			startTag = arg1;
			if(arg1.equals("input") && attr != null && attr.getValue("class") != null && attr.getValue("class").equals("ac_close")){
				parseTarget = true;
			}else if(parseTarget && arg1.equals("form")){
				parseTarget = false;
				isFinished = true;
				task.finishCallBack(result);
			}
		}

		@Override
		public void characters(char[] arg0, int arg1, int arg2)
				throws SAXException {
			if(parseTarget){
				getInnerText(arg0,arg2);
			}
		}


		@Override
		public void endElement(String arg0, String arg1, String arg2)
				throws SAXException {
			 if(arg1.equals("label")){
//					Log.d("NLiveRoid"," INn" + innerText.toString());
					result.add(innerText.toString());
				}
		}

		@Override
		public void endDocument() throws SAXException {
			if(NLiveRoid.isDebugMode)Log.d("NLiveRoid","PEND Document" + startTag);
			if(!isFinished){
				error.setErrorCode(-27);//検索に失敗しました
				task.finishCallBack(null);
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
