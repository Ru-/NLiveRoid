package nliveroid.nlr.main.parser;

import nliveroid.nlr.main.TagArrangeDialog.GetTagInfo;

import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

public class TagTokenParser implements ContentHandler {
		private StringBuilder innerText = new StringBuilder(1024);
		private boolean parseTarget = true;
		private GetTagInfo task;
		public TagTokenParser(GetTagInfo task){
			this.task = task;
		}


		private String getInnerText(char[] arg0,int arg2){
			innerText = innerText.delete(0,arg0.length);
			innerText.append(arg0, 0, arg2);
			return innerText.toString();
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
					if(arg3.getValue(i).equals("livetag_token")){
						isToken = true;
					}
				}
				if(isToken){
					task.finishCallBack(value);
				}
				//<input id="livetag_token" type="hidden" value="18da4368d28f2fb3a926be37e968e18048d41faa">
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
			// TODO

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
