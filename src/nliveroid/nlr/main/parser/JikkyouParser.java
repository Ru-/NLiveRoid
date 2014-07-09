package nliveroid.nlr.main.parser;

import java.util.ArrayList;
import java.util.regex.Pattern;

import nliveroid.nlr.main.CommunityTab.AllCommunityTask;
import nliveroid.nlr.main.JIkkyouDialog.JikkyouTask;
import nliveroid.nlr.main.LiveInfo;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

public class JikkyouParser implements ContentHandler {

	private String startTag;
	private Attributes nowAttr;
	private StringBuilder innerText = new StringBuilder(1024);
	private ArrayList<LiveInfo> liveInfos = new ArrayList<LiveInfo>();
	private LiveInfo tempInfo = new LiveInfo();
	private String infoMaster = "";
	private String pager = "";
	private int pagerTarget = 0;
	private int strongTarget = 0;
	private JikkyouTask task;
	private Pattern copt = Pattern.compile("co[0-9]+");
	public JikkyouParser(JikkyouTask jikkyouTask){
		this.task = jikkyouTask;
	}
	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		// TODO 自動生成されたメソッド・スタブ

	}
	@Override
	public void endDocument() throws SAXException {
		// TODO 自動生成されたメソッド・スタブ

	}
	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		// TODO 自動生成されたメソッド・スタブ

	}
	@Override
	public void endPrefixMapping(String prefix) throws SAXException {
		// TODO 自動生成されたメソッド・スタブ

	}
	@Override
	public void ignorableWhitespace(char[] ch, int start, int length)
			throws SAXException {
		// TODO 自動生成されたメソッド・スタブ

	}
	@Override
	public void processingInstruction(String target, String data)
			throws SAXException {
		// TODO 自動生成されたメソッド・スタブ

	}
	@Override
	public void setDocumentLocator(Locator locator) {
		// TODO 自動生成されたメソッド・スタブ

	}
	@Override
	public void skippedEntity(String name) throws SAXException {
		// TODO 自動生成されたメソッド・スタブ

	}
	@Override
	public void startDocument() throws SAXException {
		// TODO 自動生成されたメソッド・スタブ

	}
	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes atts) throws SAXException {
		// TODO 自動生成されたメソッド・スタブ

	}
	@Override
	public void startPrefixMapping(String prefix, String uri)
			throws SAXException {
		// TODO 自動生成されたメソッド・スタブ

	}

}