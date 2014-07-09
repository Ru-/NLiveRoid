package nliveroid.nlr.main;

import java.util.regex.Pattern;

public class URLEnum {


	public static final String ALERTINFO;
	public static final String ALERTURL;
	public static final String ALLCOMMUNITY;
	public static final String BBS;
	public static final String BITMAPSCOMMUNITY;
	public static final String BITMAPCOMMUNITY;
	public static final String BITMAPCHANNEL;
	public static final String BITMAPSCHANNEL;
	public static final String BITMAPUSER;
	public static final String BSP_POST;
	public static final String CHANNELURL;
	public static final String CONFIGUREAPIVALUE1;
	public static final String CONFIGURE_EXCLUDEVALUE1;
	public static final String CONFIGUREAPIEXTEND;
	public static final String CONFIGURE_ENDLIVE;
	public static final String COMMUNITYURL;
	public static final String DAIHYAKKA;
	public static final String EDITSTREAM;
	public static final String FAVARITE_API;
	public static final String GATE;
	public static final String GETPLAYER;
	public static final String GETCOMMENTXML;
	public static final String GETWAYBACKKEY;
	public static final String GETCOMMENTLOGXML;
	public static final String GETPOSTKEYXML;
	public static final String HEATBEAT;
	public static final String HYPHEN;
	public static final String JIKKYOU ;
	public static final String LEAVE;
	public static final String LIVEARCHIVE;
	public static final String MYPAGE;
	public static final String LOGINURL;
	public static final String LOGIN_ANDROID;
	public static final String LEAVEDONE;
	public static final String PC_WATCHBASEURL;
	public static final String SP_WATCHBASEURL;
	public static final String MOTION;
	public static final String MYPAGECOMMUNITY;
	public static final String NICOREPO;
	public static final String NSENURL;
	public static final String NSENPLAYER;
	public static final String OFFICIALTHUMB;
	public static final String OWNPOSTURL;
	public static final String PCPLAYER;
	public static final String PUBLISHAPI;
	public static final String PC_SEARCH;
	public static final String POSTXML;
	public static final String PLAYLISTURL;
	public static final String PC_TOP;
	public static final String RECENTRSS;
	public static final String RESERVATION_FIRST;
	public static final String SMARTMY;
	public static final String SPPLAYER;
	public static final String SP_SEARCHQUESTION;
	public static final String SMARTPLANE;
	public static final String SMARTTOP;
	public static final String SP_SEARCHTHRASH;
	public static final String USERPAGE;
	public static final String WAITSTATUS;

	public static final Pattern urlpt = Pattern.compile("(http|https):([^\\x00-\\x20()\"<>\\x7F-\\xFF])*", Pattern.CASE_INSENSITIVE);



//	public static final String AUTH0;
//	public static final String AUTH1;
//	public static final String AUTH2;

	public static final String TAGEDIT;
	public final static String TAGCOMMIT;

	//コメ欄定数文字列
	public final static String TYPE_STR;
	public final static String ID_STR;
	public final static String CMD_STR;
	public final static String TIME_STR;
	public final static String NUM_STR;
	public final static String COMMENT_STR;


	public final static String NTYPE_STR;
	public final static String NID_STR;
	public final static String NCMD_STR;
	public final static String NTIME_STR;
	public final static String NNUM_STR;
	public final static String NCOMMENT_STR;
	public final static String SCORE_STR;
	public final static String NSCORE_STR;

	public final static String RANKING ;
	public final static String TIMETABLE;

	public final static String[] ColumnText = new String[]{"TYPE","ID","CMD","TIME","SCORE","NUM","COMMENT"};

	static {

//		AUTH0 = "http://app-ga.appspot.com/";
//		AUTH1 = "http://app-gc.appspot.com/";
//		AUTH2 = "http://app-gd.appspot.com/";

		ALERTINFO = "http://live.nicovideo.jp/api/getalertinfo";

		ALERTURL = "http://live.nicovideo.jp/api/getalertstatus";
		ALLCOMMUNITY = "http://com.nicovideo.jp/community?page=%s";
		BBS = "http://com.nicovideo.jp/bbs/";
		BITMAPCOMMUNITY = "http://icon.nimg.jp/community/%s.jpg?";
		BITMAPSCOMMUNITY = "http://icon.nimg.jp/community/s/%s.jpg?";
		BITMAPCHANNEL = "http://icon.nimg.jp/channel/%s.jpg?";
		BITMAPSCHANNEL = "http://icon.nimg.jp/channel/s/%s.jpg?";
		BITMAPUSER = "http://usericon.nimg.jp/usericon/%s.jpg?";
		BSP_POST = "http://live.nicovideo.jp/api/presscast/";

		CONFIGUREAPIVALUE1 = "http://watch.live.nicovideo.jp/api/configurestream/%s?token=%s&key=hq&value=1&version=2";
		CONFIGURE_EXCLUDEVALUE1 = "http://watch.live.nicovideo.jp/api/configurestream/%s?token=%s&key=exclude&value=0&version=2";
		CONFIGUREAPIEXTEND = "http://watch.live.nicovideo.jp/api/configurestream/%s?key=extend%%5Ftest&version=2&token=%s";//%を2つ続けるのが普通の文字としての%になる
		CONFIGURE_ENDLIVE = "http://watch.live.nicovideo.jp/api/configurestream/";
		COMMUNITYURL = "http://com.nicovideo.jp/community/";
		CHANNELURL ="http://sp.ch.nicovideo.jp/?cp_in=header";
//		CHANNELURL ="http://sp.ch.nicovideo.jp/live_top";//旧URL
		DAIHYAKKA = "http://dic.nicovideo.jp/s/al/t/%s/rev_created/desc/1-";
		EDITSTREAM = "http://live.nicovideo.jp/editstream/";
		FAVARITE_API = "http://www.nicovideo.jp/api/watchitem/";
		GETPLAYER = "http://live.nicovideo.jp/api/getplayerstatus?v=";
		GETWAYBACKKEY = "http://flapi.nicovideo.jp/api/getwaybackkey?thread=";

		GETCOMMENTXML = "<thread thread=\"%s\" res_from=\"-%s\" version=\"20061206\" />\0";

		GETCOMMENTLOGXML = "<thread thread=\"%s\" res_from=\"%s\" version=\"20061206\" when=\"%s\" waybackkey=\"%s\" user_id=\"%s\" />\0";

		GATE = "http://sp.live.nicovideo.jp/gate/%s";
		GETPOSTKEYXML = "http://live.nicovideo.jp/api/getpostkey?thread=%s&block_no=%d";

		HEATBEAT = "http://live.nicovideo.jp/api/heartbeat?v=%s";
		HYPHEN = "-";

		JIKKYOU = "http://jk.nicovideo.jp/";
		LOGINURL = "https://secure.nicovideo.jp/secure/login?site=spniconico";
		LOGIN_ANDROID = "https://secure.nicovideo.jp/secure/login?site=nicoandroid";
		LEAVEDONE = "http://com.nicovideo.jp/leave/done";
		LEAVE="http://com.nicovideo.jp/leave/";
		LIVEARCHIVE = "http://com.nicovideo.jp/live_archives/";
		MOTION = "http://com.nicovideo.jp/motion/";
		MYPAGE = "http://live.nicovideo.jp/my";
		MYPAGECOMMUNITY = "http://www.nicovideo.jp/my/community/";
		NICOREPO = "http://sp.nicovideo.jp/my/nicorepo/";
		NSENURL = "http://live.nicovideo.jp/watch/nsen/";
		OFFICIALTHUMB = "http://live.nicovideo.jp/%s";

		OWNPOSTURL = "http://watch.live.nicovideo.jp/api/broadcast/%s";
		PC_WATCHBASEURL = "http://live.nicovideo.jp/watch/";

		POSTXML = "<chat thread=\"%s\" ticket=\"%s\" vpos=\"%d\" postkey=\"%s\" mail=\"%s\" user_id=\"%s\" premium=\"%s\">%s</chat>\0";
		PC_SEARCH = "http://live.nicovideo.jp/search";
		PC_TOP = "http://live.nicovideo.jp";
		PLAYLISTURL = "http://api.gadget.nicovideo.jp/live/lives/%s/play";
		PUBLISHAPI = "http://live.nicovideo.jp/api/getpublishstatus?v=";

		RESERVATION_FIRST = "http://sp.live.nicovideo.jp/api/watchingreservation?mode=watch_num&vid=%s&next_url=watch/%s&analytic=watch_timeshift_0_community_%s_comingsoon";

		SP_SEARCHQUESTION = "http://sp.live.nicovideo.jp/search?";
		SP_SEARCHTHRASH = "http://sp.live.nicovideo.jp/search/";

		SP_WATCHBASEURL = "http://sp.live.nicovideo.jp/watch/";




		PCPLAYER = "<html><head><base href=\"http://live.nicovideo.jp/\">" +
	 	"</head><body bgcolor=\"#000000\" style=\"margin:0px; padding:0px; overflow:hidden;\" >" +
	 	"<embed id=\"flvplayer\" width=\"100%\" height=\"100%\"" +
	 	" flashvars=\"v=%LIVEID%&&watchVideoID=%LIVEID%&bgColor=#000000\" quality=\"%QUALITY%\" " +
	 	"name=\"flvplayer\" style=\"\" src=\"liveplayer.swf\" " +
	 	"type=\"application/x-shockwave-flash\"></body></html>";

		RANKING = "http://sp.live.nicovideo.jp/ranking";
		SMARTPLANE = "http://sp.live.nicovideo.jp/";

		SMARTTOP = "http://sp.live.nicovideo.jp/?frompc";

		SMARTMY = "http://sp.live.nicovideo.jp/my?frompc";
//		SPPLAYER = "<html>" +
//			 	"<body  style=\"margin: 0px;\">" +
//			 	"<link rel=\"stylesheet\" type=\"text/css\" charset=\"utf-8\" href=\"http://sp.live.nicovideo.jp/inc/gate.css?11110807\">"+
//			"<script type=\"text/javascript\" src=\"http://sp.live.nicovideo.jp/inc/nicolive.js?11032410\"></script>"+
//			"<script type=\"text/javascript\" src=\"http://sp.live.nicovideo.jp/inc/watch.js?11120211\"></script>"+
//			"<link rel=\"stylesheet\" type=\"text/css\" charset=\"utf-8\" href=\"http://sp.live.nicovideo.jp/inc/error.css?11053105\">"+
//			"<script type=\"text/javascript\" src=\"http://sp.live.nicovideo.jp/inc/javascript-xpath-latest.js?11071804\"></script>"+
//			"<script type=\"text/javascript\" src=\"http://sp.live.nicovideo.jp/inc/nicoui.js?11102711\"></script>"+
//			"<link rel=\"stylesheet\" type=\"text/css\" charset=\"utf-8\" href=\"http://sp.live.nicovideo.jp/inc/nicoui.css?11102411\">"+
//			"<script type=\"text/javascript\" src=\"http://sp.live.nicovideo.jp/inc/nicosp.js?11102411\"></script>"+
//			"<script type=\"text/javascript\" src=\"http://sp.live.nicovideo.jp/inc/spTouch.js?11102411\"></script>"+
//			"<script type=\"text/javascript\" src=\"http://sp.live.nicovideo.jp/inc/watch_sp.js?11102711\"></script>"+
//			"<link rel=\"stylesheet\" type=\"text/css\" charset=\"utf-8\" href=\"http://sp.live.nicovideo.jp/inc/watch_sp.css?11110709\">" +
//			" <div id=\"player\">" +
//			 	"<script type=\"text/javascript\" src=\"http://sp.live.nicovideo.jp/inc/swfobject.js?11071804\">" +
//			 	"</script>" +
//			 	"<a class=\"p_anchor\" name=\"player\"></a> " +
//			 	"<div id=\"flvplayer_container\" style=\"\">" +
//			 	"<embed type=\"application/x-shockwave-flash\" src=\"http://nl.nimg.jp/sp/swf/spplayer.swf?120216082332\" style=\"\" id=\"flvplayer\" name=\"flvplayer\" bgcolor=\"#FFFFFF\" quality=\"low\" allowscriptaccess=\"always\" flashvars=\"playerRev=120216082332_0&amp;playerTplRev=110721071458&amp;playerType=sp&amp;v=%LIVEID%&amp;lcname=&amp;pt=official&amp;category=&amp;watchVideoID=&amp;videoTitle=&amp;gameKey=&amp;gameTime=&amp;isChannel=&amp;ver=2.5&amp;userOwner=false&amp;us=0\" height=\"100%\" width=\"100%\">" +
//			 	"</div></div></body></html>";
		//1221に読めなくなった
//		SPPLAYER = "<html><head>" +
//				"<script type=\"text/javascript\" src=\"http://sp.live.nicovideo.jp/inc/lib/prototype-1.6.0.3.js\"></script>"+
//				"<script type=\"text/javascript\" src=\"http://sp.live.nicovideo.jp/inc/javascript-xpath-latest.js\"></script>"+
//				"</head>" +
//			 	"<body style=\"margin: 0px;\">" +
//				"<script type=\"text/javascript\" src=\"http://sp.live.nicovideo.jp/inc/nicolive.js\"></script>"+
//				"<script type=\"text/javascript\" src=\"http://sp.live.nicovideo.jp/inc/watch.js\"></script>"+
//				"<script type=\"text/javascript\" src=\"http://sp.live.nicovideo.jp/inc/nicoui.js\"></script>"+
//				"<script type=\"text/javascript\" src=\"http://sp.live.nicovideo.jp/inc/nicosp.js\"></script>"+
//			"<script type=\"text/javascript\" src=\"http://sp.live.nicovideo.jp/inc/spTouch.js\"></script>"+
//			"<script type=\"text/javascript\" src=\"http://sp.live.nicovideo.jp/inc/watch_sp.js\"></script>"+
//			" <div id=\"player\">" +
//			"<script type=\"text/javascript\" src=\"http://sp.live.nicovideo.jp/inc/swfobject.js\"></script>" +
//			 	"<a class=\"p_anchor\" name=\"player\"></a> " +
//			 	"<div id=\"flvplayer_container\">" +
//			 	"<embed type=\"application/x-shockwave-flash\" src=\"http://nl.nimg.jp/sp/swf/spplayer.swf?120501105350\" " +
//			 	"style=\"\" id=\"flvplayer\" name=\"flvplayer\" bgcolor=\"#FFFFFF\" quality=\"%QUALITY%\" " +
//			 	"allowscriptaccess=\"always\" flashvars=\"playerRev=120216082332_0&" +
//			 	"playerTplRev=110721071458&playerType=sp&v=%LIVEID%&lcname=&" +
//			 	"category=&watchVideoID=&videoTitle=&gameKey=&gameTime=&isChannel=&ver=2.5&userOwner=false&us=0\"" +
//			 	"height=\"100%\" width=\"100%\">" +
//			 	"</div></div>" +
//			 	"</body></html>";

		SPPLAYER = "<html style=\"overflow:hidden;\"><head>" +
				"<script type=\"text/javascript\" src=\"http://sp.live.nicovideo.jp/inc/lib/prototype-1.6.0.3.js?1103241\"></script>"+
				"<script type=\"text/javascript\" src=\"http://sp.live.nicovideo.jp/inc/javascript-xpath-latest.js\"></script>"+
				"<script type=\"text/javascript\" src=\"inc/common.js?12091001\"></script>"+
				"<script type=\"text/javascript\" src=\"http://sp.live.nicovideo.jp/inc/__utm.js?12112008\"></script>"+
				 "<script type=\"text/javascript\" src=\"http://sp.live.nicovideo.jp/inc/lib/jquery-1.8.2.min.js?12092503\"></script>"+
				"<script>var jQueries = jQueries ? jQueries :{}; jQueries[\"1.8.2\"] = jQuery.noConflict(true);</script>"+
				 "</head>" +
			 	"<body bgcolor=\"#000000\" style=\"margin:0px; padding:0px; overflow:hidden; >" +// height:100%;\"とすると縦スクロールも無効にできるが調整で下の来場とかが見えなくなるのと、%指定だと縦横切り替えで因果関係が発生する
				"<script type=\"text/javascript\" src=\"http://sp.live.nicovideo.jp/inc/nicolive.js\"></script>"+
				"<script type=\"text/javascript\" src=\"http://sp.live.nicovideo.jp/inc/watch.js\"></script>"+
				"<script type=\"text/javascript\" src=\"http://sp.live.nicovideo.jp/inc/nicoui.js\"></script>"+
				"<script type=\"text/javascript\" src=\"http://sp.live.nicovideo.jp/inc/nicosp.js\"></script>"+
			"<script type=\"text/javascript\" src=\"http://sp.live.nicovideo.jp/inc/spTouch.js\"></script>"+
			"<script type=\"text/javascript\" src=\"http://sp.live.nicovideo.jp/inc/watch_sp.js\"></script>"+
			" <div id=\"player\" style=\"margin: 0px;\">" +
			"<script type=\"text/javascript\" src=\"http://sp.live.nicovideo.jp/inc/swfobject.js\"></script>" +
			 	"<a class=\"p_anchor\" name=\"player\"></a> " +
			 	"<div id=\"flvplayer_container\">" +
			 	"<embed type=\"application/x-shockwave-flash\" src=\"http://nl.nimg.jp/sp/swf/spplayer.swf?121214180842\" " +
			 	"id=\"flvplayer\" name=\"flvplayer\" bgcolor=\"#FFFFFF\" quality=\"%QUALITY%\" " +
			 	"allowscriptaccess=\"always\" flashvars=\"playerRev=121214180842_0&" +
			 	"playerTplRev=110721071458&playerType=sp&v=%LIVEID%&lcname=&" +
			 	"category=&watchVideoID=&videoTitle=&gameKey=&gameTime=&isChannel=&ver=2.5&userOwner=false&us=0\"" +
			 	"height=\"0\" width=\"0\">" +

			 	"</div></div>" +
			 	"</body></html>";
		NSENPLAYER =
//				"<html style=\"overflow:hidden;\">" +
				"<html>"+
						"<head>" +
						"<script type=\"text/javascript\" src=\"http://live.nicovideo.jp/inc/lib/prototype-1.6.0.3.js?1103241\"></script>"+
						"<script type=\"text/javascript\" src=\"http://live.nicovideo.jp/inc/javascript-xpath-latest.js\"></script>"+
						"<script type=\"text/javascript\" src=\"http://live.nicovideo.jp/inc/inc/common.js?12091001\"></script>"+
						"<script type=\"text/javascript\" src=\"http://live.nicovideo.jp/inc/__utm.js?12112008\"></script>"+
						 "<script type=\"text/javascript\" src=\"http://live.nicovideo.jp/inc/lib/jquery-1.8.2.min.js?12092503\"></script>"+
						"<script>var jQueries = jQueries ? jQueries :{}; jQueries[\"1.8.2\"] = jQuery.noConflict(true);</script>"+
						 "</head>" +
//						 "<body bgcolor=\"#000000\" style=\"margin:0px; padding:0px; overflow:hidden; >" +
						 "<body bgcolor=\"#000000\" style=\"margin:0px; padding:0px; >" +
						"<script type=\"text/javascript\" src=\"http://live.nicovideo.jp/inc/nicolive.js\"></script>"+
						"<script type=\"text/javascript\" src=\"http://nl.simg.jp/public/inc/watch_nsen_cb.js\"></script>"+
						"<script type=\"text/javascript\" src=\"http://nl.simg.jp/public/inc/watch.js\"></script>"+
						"<script type=\"text/javascript\" src=\"http://nl.simg.jp/public/inc/notifybox_preload.js\"></script>"+
						"<script type=\"text/javascript\" src=\"http://nl.simg.jp/public/inc/watch_zero_cb.js\"></script>"+
						"<script type=\"text/javascript\" src=\"http://nl.simg.jp/public/inc/nicolive.placeholder.js\"></script>"+
						"<script type=\"text/javascript\" src=\"http://nl.simg.jp/public/inc/swfobject.js\"></script>"+
						"<script type=\"text/javascript\" src=\"http://nl.nimg.jp/public/swf/plugintap.swf\"></script>"+
						"<script type=\"text/javascript\" src=\"http://live.nicovideo.jp/inc/share_button_tool.js\"></script>"+
						"<script type=\"text/javascript\" src=\"http://live.nicovideo.jp/inc/nicoui.js\"></script>"+
						"<script type=\"text/javascript\" src=\"http://live.nicovideo.jp/inc/nicosp.js\"></script>"+
						"<script type=\"text/javascript\" src=\"http://live.nicovideo.jp/inc/jsconnector.swf\"></script>"+
					" <div id=\"player\" style=\"margin: \"0px;\">" +
					"<script type=\"text/javascript\" src=\"http://nl.simg.jp/public/inc/swfobject.js\"></script>" +
					"<script type=\"text/javascript\" src=\"http://nl.nimg.jp/public/swf/plugintap.swf\"></script>"+	"<a class=\"p_anchor\" name=\"player\"></a> " +
					 	"<div id=\"flvplayer_container\">" +
					 	"<embed id=\"flvplayer\" " +
					 	"flashvars=\"logicRev=130516131439&playerTplRev=130227131205&crRev=120308094520&djRev=111209162555&dpRev=120206163333&nsRev=130130141528&v=%LIVEID%&pt=official&siteDomain=jp&languagecode=ja-jp&localecode=JP&localeForSeat=JP&applyLangFilter=0&playerDicRev=130404135413\" " +
					 	"allowscriptaccess=\"always\"" +
					 	"allowfullscreen=\"true\" " +
					 	"quality=\"low\" " +
					 	"name=\"flvplayer\" " +
					 	"src=\"http://live.nicovideo.jp/nicoliveplayer.swf?130516205924\" " +
					 	"type=\"application/x-shockwave-flash\">"+
					 	"</div></div>" +
					 	"</body></html>";

		RECENTRSS = "http://live.nicovideo.jp/recent/rss?p=0";
		TAGEDIT = "http://live.nicovideo.jp/editlivetags/";
		TAGCOMMIT = "http://live.nicovideo.jp/livetags.php?v=%s&version=zero";

		TIMETABLE = "http://sp.live.nicovideo.jp/timetable";

		USERPAGE = "http://www.nicovideo.jp/user/";

		WAITSTATUS = "http://live.nicovideo.jp/api/waitinfo/";


		TYPE_STR = "@"+R.id.seq0;
		ID_STR = "@"+R.id.seq1;
		CMD_STR = "@"+R.id.seq2;
		TIME_STR = "@"+R.id.seq3;
		SCORE_STR = "@"+R.id.seq4;
		NUM_STR = "@"+R.id.seq5;
		COMMENT_STR = "@"+R.id.seq6;

		NTYPE_STR = "@"+R.id.nseq0;
		NID_STR = "@"+R.id.nseq1;
		NCMD_STR = "@"+R.id.nseq2;
		NTIME_STR = "@"+R.id.nseq3;
		NSCORE_STR = "@"+R.id.nseq4;
		NNUM_STR = "@"+R.id.nseq5;
		NCOMMENT_STR = "@"+R.id.nseq6;

	}

}
