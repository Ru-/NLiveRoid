package nliveroid.nlr.main.parser;

import nliveroid.nlr.main.CommandDialog.GetBSPToken;

import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import android.util.Log;

public class BSPTokenParser implements ContentHandler {
		private GetBSPToken task;
		private boolean isSuccess = false;

		public BSPTokenParser(GetBSPToken task){
			this.task = task;
		}

		@Override
		public void startElement(String arg0, String arg1, String arg2,
				org.xml.sax.Attributes arg3) throws SAXException {

			if(arg1 != null && arg1.equals("input")&& arg3 != null && arg3.getLength() > 0){
				String value = "";
				boolean isToken = false;
				for(int i = 0; i < arg3.getLength(); i++){
					if(arg3.getLocalName(i).equals("value")){
						value = arg3.getValue(i);
					}
					if(arg3.getValue(i).equals("presscast_token")){
						isToken = true;
						isSuccess = true;
					}
				}
				if(isToken){//ここは3回位来る
					isToken = false;
					task.finishCallBack(value);
				}
				//<input id="presscast_token" type="hidden" value="9aa9680aa92fc775c5e1294d8fd3c0678e7454f4">
			}

		}

		@Override
		public void characters(char[] arg0, int arg1, int arg2)
				throws SAXException {
		}


		@Override
		public void endElement(String arg0, String arg1, String arg2)
				throws SAXException {
		}

		@Override
		public void endDocument() throws SAXException {
				if(!isSuccess){
					Log.d("NLiveRoid","BSP F-- ");
					task.finishCallBack();
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
