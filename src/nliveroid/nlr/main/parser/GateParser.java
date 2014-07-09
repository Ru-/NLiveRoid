package nliveroid.nlr.main.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nliveroid.nlr.main.ErrorCode;
import nliveroid.nlr.main.Gate;
import nliveroid.nlr.main.Gate.BackGroundUpdate;
import nliveroid.nlr.main.LiveInfo;
import nliveroid.nlr.main.NLiveRoid;
import nliveroid.nlr.main.URLEnum;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import android.util.Log;

public class GateParser implements ContentHandler {

	private String startTag;
	private Attributes nowAttr;
	private StringBuilder innerText = new StringBuilder(1024);
	private final LiveInfo liveinfo;
	private boolean titleSet = false;
	private boolean ownerSet = false;
	private boolean cnameSet = false;
	private int section = 0;//<section>がでてくるのが、放送詳細、コミュ詳細、タグの順
	private boolean commuInfoSet = false;
	private boolean tagsection = false;
	private boolean isInfo = false;
	private boolean isChannel = false;
	private final BackGroundUpdate task;
	private String masterDescStr = "";
	private String masterTagsStr = "";
	private String masterComminfoStr = "";
	private final String sizeStr = "size";
	private final String colorStr = "color";
	private ErrorCode error;//最初のパースとサムネイル取得に必要
	private int isFirstArticle;
	private int linkIndex = 0;
	private final Pattern reservept = Pattern.compile("gate_timeshift_0_(community|channel|official)_lv[0-9]+_comingsoon");
	private boolean isFinished;
	public GateParser(Gate.BackGroundUpdate task,ErrorCode error,LiveInfo defaultLiveinfo){
		if(NLiveRoid.isDebugMode)Log.d("NLiveRoid"," new GateParser " + defaultLiveinfo.getLiveID());
		this.task = task;
		this.error = error;
		this.liveinfo = defaultLiveinfo;
	}

	private String getInnerText(char[] arg0,int arg2){
		innerText = innerText.delete(0,arg0.length);
		innerText.append(arg0, 0, arg2);
		return innerText.toString();
	}

	//ここはendTagの時にも呼ばれる気がするのでstartElementでのinnerTextがこっちと共通してるとは限らない
	@Override
	public void characters(char[] arg0, int arg1, int arg2) throws SAXException {
		if(startTag.equals("h2")&&!titleSet&&nowAttr != null && nowAttr.getLength() > 0){//放送タイトル
				for(int i = 0; i < nowAttr.getLength();i++){
					if(nowAttr.getLocalName(i).equals("class")&&nowAttr.getValue(i).equals("title")){
					getInnerText(arg0, arg2);
					String title = innerText.toString().replaceAll("\t|\n", "");
						if(liveinfo != null){
							if(NLiveRoid.isDebugMode)Log.d("NLiveRoid"," GateParse title" + title);
							liveinfo.setTitle(title);
							titleSet = true;
						}
					}
				}
		}else if(startTag.equals("p")&&!ownerSet &&nowAttr != null && nowAttr.getLength() > 0
				&&nowAttr.getValue("class")!=null&&nowAttr.getValue("class").equals("desc")){//放送者名
				String ownname = getInnerText(arg0, arg2).replaceAll("\t|\n|放送者：", "");
				if(ownname != null && ownname.equals("放送チャンネル：")){//チャンネルは、放送者名が「放送チャンネル：」となっていて、そのなかに<a>でURLと放送チャンネル者名がある
					ownerSet = true;
					isChannel = true;
					commuInfoSet = true;
					return;
				}
					if(liveinfo != null){
						liveinfo.setOwnerName(ownname);
						if(NLiveRoid.isDebugMode)Log.d("NLiveRoid"," GateParse ownname" + ownname);
						ownerSet = true;
					}
		}else if(startTag.equals("a")&&!cnameSet&&ownerSet){//コミュニティ名
			String inner =getInnerText(arg0,arg2).replaceAll("\t|\n", "");
			if(liveinfo != null){
				if(commuInfoSet){//すでにインフォセットフラグがあるのはチャンネルとする
				liveinfo.setOwnerName(inner);
					if(nowAttr != null && nowAttr.getValue("onclick")!= null){
						if(NLiveRoid.isDebugMode)Log.d("NLiveRoid"," GateParse setComunityName");
						liveinfo.setComunityName(nowAttr.getValue("onclick").split("/")[4].split("'")[0]);
					}
				}else{
					if(NLiveRoid.isDebugMode)Log.d("NLiveRoid"," GateParse setComunityName innerX " + inner);
				liveinfo.setComunityName(inner);
				//coが無かったらここでセットする(chはGateからのコミュニティ系機能に非対応なので無視)
					if(liveinfo.getCommunityID().equals(URLEnum.HYPHEN) && nowAttr != null && nowAttr.getValue("onclick") != null){
						Matcher copt = Pattern.compile("co[0-9]+").matcher(nowAttr.getValue("onclick"));
						if(copt.find()){
							liveinfo.setCommunityID(copt.group());
						}
					}
				}
				cnameSet = true;
			}
		}else if(isInfo){
			//ここで<section><h2>お知らせ</h2>XXXX/XX/XXに終了||開始しますの、テキスト</section>
			//となっているのでstartElement、endElementではテキストが取れない
			String inner = getInnerText(arg0,arg2).replaceAll("\t|\n| |　", "");
			Matcher mc = Pattern.compile("[0-9]{4}/[0-9]{2}/[0-9]{2}.*").matcher(inner);
				if(mc.find()){
					if(NLiveRoid.isDebugMode)Log.d("NLiveRoid"," GateParse <section><h2>OSHIRASE ");
					masterDescStr += mc.group();
				}
		}else if(section == 1){//放送詳細の文字列
			String inner = getInnerText(arg0,arg2).replaceAll("\n","");
			if(startTag.equals("h2")&&innerText.toString().contains("お知らせ")){
				isInfo = true;//お知らせセクションがコミュ詳細の上に来る→終了していた||まだ始まっていない
				section--;
				return;
			}
			if(inner.length() < 1)return;
			//詳細の文字列前にタブ文字などがあり、綺麗に表示できない
			//なぜか"^ |^　|^\t"にマッチしないのに最初にスペースが入ってくる
			//何行番目で記事がはじまるのか統一されていない
			if(isFirstArticle <= 5){//最初の5文字までにタブ文字等があると消える
				for(int i = 0; i < inner.length(); i++){
				if(inner.substring(0,1).matches("\n| |\t|　")){
					inner = inner.substring(1);
					i--;
				}else{
					break;
				}
				}
				if(inner.contains("ID"))inner = inner.replaceAll("番組紹介文|番組|\\(|\\)|ID|:","")+"<br><br>";
			}
			if(inner.matches(".++")){
				if(NLiveRoid.isDebugMode)Log.d("NLiveRoid"," GateParse Desc " + inner);
			masterDescStr += inner;
			}
		}else if(section == 2){//コミュニティ情報の文字列
				if(startTag.equals("div")&&!commuInfoSet){
					if(nowAttr != null){
						if(nowAttr.getValue("id") != null && nowAttr.getValue("id").equals("levelsection")){//レベル
						masterComminfoStr +=  getInnerText(arg0,arg2).replaceAll("\t| |　|\n", "");
						}else if(nowAttr.getValue("id") != null && nowAttr.getValue("id").equals("countsection")){//メンバー
							masterComminfoStr +=  "<br>" + getInnerText(arg0,arg2).replaceAll("\t| |　|\n", "") ;
						}else if(nowAttr.getValue("id") != null && nowAttr.getValue("id").equals("descriptionsection")){
							commuInfoSet = true;//コミュニティプロフィールっていう文字列も、いらないので飛ばす
						}
					}

					if(NLiveRoid.isDebugMode)Log.d("NLiveRoid"," GateParse CommuInfo ");
				}else if(commuInfoSet||isChannel){//チャンネルの場合、レベルとメンバーは来ない
					String inner = getInnerText(arg0,arg2).replaceAll("\n","");
					if(inner.length() < 1)return;
					//何行番目で記事がはじまるのか統一されていない
					if(isFirstArticle <= 10){//最初の10文字までにタブ文字等があると消える
						for(int i = 0; i < inner.length(); i++){
						if(inner.substring(0,1).matches(" |\t|　")){
							inner = inner.substring(1);
							i--;
						}else{
							break;
						}
						}
					}
					if(isChannel){
						if(NLiveRoid.isDebugMode)Log.d("NLiveRoid"," GateParse  ");
						if(startTag.equals("h2") && nowAttr != null && nowAttr.getValue("class") != null
								&&nowAttr.getValue("class").equals("hd")){//<h2 class="hd">チャンネル情報</h2>は消す
							inner = inner.replaceAll("チャンネル情報|\n| |\t|　", "");//chXXX改行とするなら、この次のcharacters時にくる時に改行入れないといけない
						}
						//<span class="btn_text">で終了
						if(startTag.equals("span") && nowAttr != null && nowAttr.getValue("class")!=null
								&&nowAttr.getValue("class").equals("btn_text")){
							return;
						}
					}
					if(inner.matches(".+")){
						if(NLiveRoid.isDebugMode)Log.d("NLiveRoid"," GateParse inner.matches(\".+\")" + masterComminfoStr.length());
					masterComminfoStr +=inner;
					}
				}
		}else if(tagsection&&startTag.equals("option")){
			if(NLiveRoid.isDebugMode)Log.d("NLiveRoid"," GateParse Live Tag set");
			masterTagsStr += "<<TAGXXX>>"+getInnerText(arg0,arg2);
		}
//		Log.d("GateParser","char " + getInnerText(arg0,arg2).replace("\n",""));
	}

	@Override
	public void startElement(String arg0, String arg1, String arg2,
			Attributes arg3) throws SAXException {
		startTag=arg1;
		nowAttr = arg3;
		if(isFinished)return;
//		if(startTag.equals("footer")){//footerタグが無くなっていた	ので</body>で終了判定にした 2014 1/27
//			if(NLiveRoid.isDebugMode)Log.d("NLiveRoid"," GateParse footer finishCallBack --- ");
//			task.finishCallBack(liveinfo);
//			isFinished = true;
//			return;
//		}else
			if(startTag.equals("select") && arg3 != null && arg3.getLength() > 0 && arg3.getValue(0).equals("selecttag")){
				tagsection = true;//ここで登録タグのパースが開始される この後optionタグはないので、このフラグを終了フラグに使う→ここのoptionタグがあったらできなくなっちゃう事に注意
				if(NLiveRoid.isDebugMode)Log.d("NLR"," GateParse tagsection");
		}else if(section == 0 && startTag.equals("a") && nowAttr != null && nowAttr.getValue("onclick") != null){
			if(NLiveRoid.isDebugMode)Log.d("NLiveRoid","FINDATTR --- " + nowAttr.getValue("onclick"));
			Matcher mc = reservept.matcher(nowAttr.getValue("onclick"));
			if(mc.find()){
				if(NLiveRoid.isDebugMode)Log.d("NLiveRoid","FINDMATCHER  --- ");
				liveinfo.setTsReserveToken(mc.group());
			}
		}else if(startTag.equals("section")){//<section>がでてくるのが、放送詳細、コミュ詳細、タグの順
			section++;
			isFirstArticle = 0;
		}
		if(section == 1){
				if(isFirstArticle < 10){//放送詳細、コミュインフォは2行目?から
			isFirstArticle ++;
				}
			masterDescStr += isTag_Start(arg1,arg3);
		}else if(section == 2){
			if(isFirstArticle < 10){
			//詳細、コミュインフォは2行目?から
			isFirstArticle ++;
			}
			if(nowAttr != null && nowAttr.getLength() > 0
					&&nowAttr.getValue("id")!=null&&nowAttr.getValue("id").equals("alertsection")){
				if(NLiveRoid.isDebugMode)Log.d("NLiveRoid"," END Community description");
				section = 3;//アラート登録とかの前に次のセクションに行ってコミュ詳細文面のパースを終了する
				return;
			}
			masterComminfoStr += isTag_Start(arg1,arg3);
		}

	}

	@Override
	public void endDocument() throws SAXException {
	}

	@Override
	public void endElement(String arg0, String arg1, String arg2)
			throws SAXException {
			if(tagsection&&arg1.equals("div")){
				Log.d("NLiveRoid"," GateParse tagsection && div finishCallBack");
			task.finishCallBack(liveinfo);
			isFinished = true;
			return;
			}
		if(arg1.equals("select")){
//			tagsection = false;//ここで登録タグのパースが終了する この後
			if(liveinfo != null){
				if(NLiveRoid.isDebugMode)Log.d("NLiveRoid"," GateParse tagsection = false");
				liveinfo.setTags(masterTagsStr);
				liveinfo.setDescription(masterDescStr);
				liveinfo.setCommunity_info(masterComminfoStr);
			}
		}

		//放送詳細か、コミュ詳細で、今ついになる予定のタグがきて、且つ
		//charactersが呼ばれたので空タグじゃない(<i></i>みたいのじゃない)
		if(section == 1 ){//放送詳細
					masterDescStr += isTag_End(arg1);
		}else if(section == 2 && commuInfoSet){
			//コミュプロフィール
					masterComminfoStr += isTag_End(arg1);
		}
		if(isInfo&&arg1.equals("section")){//終了またはまだ開始されてない
		isInfo = false;
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

	/**
	 * 詳細、コミュニティインフォタグを返す　スタートタグ
	 * @param String startTag
	 * @param String endTag
	 * @return result
	 */
	private String isTag_Start(String tag,Attributes attr){
		String result = "";
		//startTagは次のタグを見に行っていて、endTagより先に呼ばれている
		//常にstartTag,endTagはあるので改行のみならスキップ
		//innerの終わり判定がうまくいかずその時のstartTagが付いてしまうので
		//実際に色などつける時(Spannable)にタグの終わりがなければシカト処理が必要
		//brはソースに含まれている\nが入ってるので無視できる
		if(!tag.replaceAll("\n","").matches(".++")){
			return "";
		}
		if(tag.equals("font")){//大文字でもこれでちゃんと入ってくる、Valueが空文字の場合があるので注意
			if(attr != null && attr.getLength() > 0){
				String size = attr.getValue(sizeStr) != null? attr.getValue(sizeStr).equals("")? " size=1":" size="+attr.getValue(sizeStr):"";
				String color = attr.getValue(colorStr) != null? attr.getValue(colorStr).equals("")? " color=#000000":" color="+attr.getValue(colorStr):"";
				result += "<font" +size + color+">";
			}
		}else if(tag.equals("b")){
				result += "<b>";
		}else if(tag.equals("i")){
				result += "<i>";
		}else if(tag.equals("s")){
				result += "<s>";
		}else if(tag.equals("u")){//uはaに取って代わられるようなので付けない
			result += "<u>";
		}else if(tag.equals("a")){
			String url = "";
			for(int i = 0; i < attr.getLength() ; i++){
				if(attr.getLocalName(i).equals("href")&&!attr.getValue(i).contains("javascript:void(0)")){
					url = attr.getValue(i);
				}
			}
			result += "<<LINK" + linkIndex+">>" + url+"<<LINK"+linkIndex+">>";
			linkIndex++;
		}else if(tag.equals("br")){//改行はスタートだけでとる
			result += "<br>";
		}
		if(NLiveRoid.isDebugMode)Log.d("NLiveRoid","Tag_Start" + result);
		return result;
	}

	/**
	 * 詳細、コミュニティインフォタグを返す　エンドタグ
	 * @param String startTag
	 * @param String endTag
	 * @return result
	 */
	private String isTag_End(String tag){
		String result = "";
		//startTagは次のタグを見に行っていて、endTagより先に呼ばれている
		//常にstartTag,endTagはあるので改行のみならスキップ
		//innerの終わり判定がうまくいかずその時のstartTagが付いてしまうので
		//実際に色などつける時(Spannable)にタグの終わりがなければシカト処理が必要
		//brはソースに含まれている\nが入ってるので無視できる
		if(!tag.replaceAll("\n","").matches(".++")){
			return "";
		}
		if(tag.equals("font")){
				result += "</font>";
		}else if(tag.equals("b")){
				result += "</b>";
		}else if(tag.equals("i")){
				result += "</i>";
		}else if(tag.equals("s")){
				result += "</s>";
		}else if(tag.equals("u")){
			result += "</u>";
		}
		if(NLiveRoid.isDebugMode)Log.d("NLiveRoid","Tag_End " + result);
		return result;
	}

}
