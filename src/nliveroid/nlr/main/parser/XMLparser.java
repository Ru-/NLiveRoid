package nliveroid.nlr.main.parser;

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nliveroid.nlr.main.ErrorCode;
import nliveroid.nlr.main.LiveInfo;
import nliveroid.nlr.main.LiveSettings;

import org.apache.http.ParseException;
import org.xml.sax.InputSource;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Log;
import android.util.Xml;


public class XMLparser {

	private final static Pattern passedPt = Pattern.compile("[0-9][0-9]:[0-9][0-9]:[0-9][0-9]");
	/**
	 * タグの間の文字列を取得
	 *
	 * @param xml
	 * @param tagname
	 * @return
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	private static String getItemString(XmlPullParser xml, String tagname)
			throws XmlPullParserException, IOException {
		String result = null;
		int eventType = xml.next();
		while (eventType != xml.END_DOCUMENT) {
			// itemのエンドタグを見つけたらループ終了
			if (eventType == XmlPullParser.END_TAG
					&& tagname.equals(xml.getName())) {
				break;
			}
			if (eventType == XmlPullParser.TEXT) {
				result = xml.getText();
				return result;
			}
			eventType = xml.next();
		}
		return result;

	}

	/**
	 *
	 * STARTDOCUMENT=0 ENDDOCUMENT=1 STARTTAG=2 ENDTAG=3 TEXT=4
	 *
	 * // title; ID; // discription; // resNum; // viewCounter; //
	 * defaultCommunity; // startTime;
	 */

	/**
	 * RSSの新着情報のXMLをパース
	 *
	 * @param param
	 * @return
	 */
	public static ArrayList<LiveInfo> parseRSSFromByteArrat(byte[] param) {// イリーガルとパーサの例外をどうするか
//	 Log.d("log","--------" +param );
		ArrayList<LiveInfo> infoObject = new ArrayList<LiveInfo>();
		final XmlPullParser xml = Xml.newPullParser();
		LiveInfo liveinfo = new LiveInfo();
		try {
			// paramがNULLならトーストネットワークが調子悪いパターン
			xml.setInput(new StringReader(new String(param, "UTF-8")));
			int eventType = 0;

			while ((eventType = xml.next()) != XmlPullParser.END_DOCUMENT) {
//				 Log.d("log", "EVENT " + xml.getName());
				// itemタグを見つけたらセット
				if (eventType == XmlPullParser.START_TAG
						&& "title".equals(xml.getName())) {
					liveinfo.setTitle(getItemString(xml, "title"));
				} else if (eventType == XmlPullParser.START_TAG
						&& "guid".equals(xml.getName())) {
					liveinfo.setLiveID(getItemString(xml, "guid"));
				} else if (eventType == XmlPullParser.START_TAG
						&& "description".equals(xml.getName())) {
					liveinfo.setDescription(getItemString(xml, "description"));
				} else if (eventType == XmlPullParser.START_TAG
						&& "num_res".equals(xml.getName())) {
					liveinfo.setResNumber(getItemString(xml, "num_res"));
				} else if (eventType == XmlPullParser.START_TAG
						&& "view".equals(xml.getName())) {
					liveinfo.setViewCount(getItemString(xml, "view"));
				} else if (eventType == XmlPullParser.START_TAG
						&& "community_id".equals(xml.getName())) {//コミュで取得するのはIDのみ
					liveinfo.setCommunityID(getItemString(xml, "community_id"));
				} else if (eventType == XmlPullParser.START_TAG
						&& "owner_name".equals(xml.getName())) {
					liveinfo.setOwnerName(getItemString(xml, "owner_name"));
				} else if (eventType == XmlPullParser.START_TAG
						&& "pubDate".equals(xml.getName())) {
					String pubDate = getItemString(xml, "pubDate");
					liveinfo.setStartTime(pubDate);
					Matcher mc = passedPt.matcher(pubDate);
					if(mc.find()){
						Date date = new Date();//端末の時間を合わせていないとできない
						SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
						int n = getDateAmmount(sdf.format(date).split(":"));
						int s = getDateAmmount(mc.group().split(":"));
						String result = n-s<0? "0":(n-s)/60>60? String.valueOf((n-s)/3600)+"時間"+String.valueOf(((n-s)/60)%60):String.valueOf((n-s)/60);
					liveinfo.setPassedTime(result);
					}else{
						liveinfo.setPassedTime("-");
					}
				} else if (eventType == XmlPullParser.START_TAG
						&& "thumbnail".equals(xml.getName())) {
					// サムネイルはURLが属性値にある
					liveinfo.setThumbnailURL(xml.getAttributeValue(0));
				}  else if (eventType == XmlPullParser.END_TAG
						&& "item".equals(xml.getName())) {
					infoObject.add(liveinfo.clone());
					liveinfo = new LiveInfo();
				}
			}

		} catch(NullPointerException e){
			//ネットワーク障害
			return null;
		}catch (XmlPullParserException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		return infoObject;

	}
		/**
		 * RSS経過時間取得ヘルパメソッド
		 * @param data
		 * @return
		 */
		private static int getDateAmmount(String[] data){
			int dateformat = Integer.parseInt(data[0])*3600;
			dateformat += (Integer.parseInt(data[1])*60);
			dateformat += Integer.parseInt(data[2]);
			return dateformat;

		}
	/**
	 * マイページのHTMLをXMLに解釈してパース 未使用　たぶんできない
	 *
	 * @param param
	 * @return
	 */
	public static ArrayList<LiveInfo> myPageParseFromByteArray(byte[] param) {
		ArrayList<LiveInfo> infoObject = new ArrayList<LiveInfo>();
		final XmlPullParser xml = Xml.newPullParser();
		LiveInfo liveinfo = new LiveInfo();
		try {
			// paramがNULLならトーストネットワークが調子悪いパターン
			xml.setInput(new StringReader(new String(param, "UTF-8")));
			int eventType = 0;

			while ((eventType = xml.next()) != XmlPullParser.END_DOCUMENT) {
				// Log.d("log", "EVENT " + xml.getName());
				// divタグを見つけたらセット
				if (eventType == XmlPullParser.START_TAG
						&& "div".equals(xml.getName())) {
					if (xml.getAttributeValue(0).equals("liveItem")) {
						eventType = xml.next();
						while (eventType != XmlPullParser.END_DOCUMENT) {// このループで詳細(description)以外の情報を取得、次の詳細のブロックまで行かずに抜ける→必要になったらAPI
							if (eventType == XmlPullParser.START_TAG
									&& "noProgram".equals(xml.getName())) {
								break;// noProgramはない時
							} else if (eventType == XmlPullParser.START_TAG
									&& "a".equals(xml.getName())) {
								liveinfo.setTitle(xml.getAttributeValue(0));
								// URL前半部が固定の物と仮定しているので注意が必要かも
								liveinfo.setLiveID(xml.getAttributeValue(1)
										.substring(34));
							} else if (eventType == XmlPullParser.START_TAG
									&& "img".equals(xml.getName())) {
								// 属性値の2番目にサムネURLがあると仮定しているので注意が必要かも
								liveinfo.setThumbnailURL(xml
										.getAttributeValue(1));// コミュニティのサムネいる
							} else if (eventType == XmlPullParser.START_TAG
									&& "strong".equals(xml.getName())) {
								liveinfo.setStartTime(getItemString(xml,
										"strong"));
							} else if (eventType == XmlPullParser.END_TAG
									&& "strong".equals(xml.getName())) {
								infoObject.add(liveinfo);
								break;
							}
							eventType = xml.next();
						}

					}
				}
			}

		} catch (XmlPullParserException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}

		return infoObject;
	}

	/**
	 * マイページのXML解釈のパースの入力String版
	 *未使用
	 * @param param
	 * @return
	 */
	public static ArrayList<LiveInfo> myPageParseFromString(String param, LiveInfo liveinfo) {
		ArrayList<LiveInfo> infoObject = new ArrayList<LiveInfo>();
		final XmlPullParser xml = Xml.newPullParser();
		try {
			xml.setInput(new StringReader(param));
			int eventType = 0;

			while ((eventType = xml.next()) != XmlPullParser.END_DOCUMENT) {
				// divタグを見つけたらセット
				if (eventType == XmlPullParser.START_TAG
						&& "div".equals(xml.getName())) {
					if (xml.getAttributeValue(0).equals("liveItem")) {
						eventType = xml.next();
						while (eventType != XmlPullParser.END_DOCUMENT) {// このループで詳細(description)以外の情報を取得、次の詳細のブロックまで行かずに抜ける→必要になったらAPI
//							 Log.d("log", "EVENT " + xml.getName());
							if (eventType == XmlPullParser.START_TAG
									&& "a".equals(xml.getName())) {
								liveinfo.setTitle(xml.getAttributeValue(0));
								// URL前半部が固定の物と仮定しているので注意が必要かも
								liveinfo.setLiveID(xml.getAttributeValue(1)
										.substring(34));
							} else if (eventType == XmlPullParser.START_TAG
									&& "img".equals(xml.getName())) {
								// 属性値の2番目にサムネURLがあると仮定しているので注意が必要かも
								liveinfo.setThumbnailURL(xml
										.getAttributeValue(1));// コミュニティのサムネいる
							} else if (eventType == XmlPullParser.START_TAG
									&& "strong".equals(xml.getName())) {
								liveinfo.setStartTime(getItemString(xml,
										"strong"));
							} else if (eventType == XmlPullParser.END_TAG
									&& "strong".equals(xml.getName())) {
								infoObject.add(liveinfo);
								break;
							}
							eventType = xml.next();
						}

					}
				}
			}

		} catch (XmlPullParserException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}

		return infoObject;
	}

	/**
	 * getpublishstatusAPIの入力byte[]XMLからrtmp+token+open_timeを取得(パース)する
	 *
	 * @param source
	 * @return
	 */
	public static LiveInfo getTokenInfoFromAPIByteArray(byte[] source,
			LiveInfo liveinfo) {
		final XmlPullParser xml = Xml.newPullParser();
		String rtmp = "";
		try {
			xml.setInput(new StringReader(new String(source,"UTF-8")));
			int eventType = 0;

			while ((eventType = xml.next()) != XmlPullParser.END_DOCUMENT) {
				if (eventType == XmlPullParser.START_TAG
						&& "token".equals(xml.getName())) {
					liveinfo.setToken(getItemString(xml, "token"));
				}else if(eventType == XmlPullParser.START_TAG
						&& "url".equals(xml.getName())) {//何故か呼ばれない?
				}else if (eventType == XmlPullParser.START_TAG
						&& "ticket".equals(xml.getName())) {
					liveinfo.setRtmpurl(rtmp + "?"+ getItemString(xml, "ticket"));
				}else if (eventType == XmlPullParser.START_TAG
						&& "rtmp".equals(xml.getName())) {
					rtmp = getItemString(xml,"rtmp");
				}else if (eventType == XmlPullParser.START_TAG
						&& "end_time".equals(xml.getName())) {
					liveinfo.setEndTime(getItemString(xml,"end_time"));
				}
			}
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			return null;
		} catch (ParseException e){
			e.printStackTrace();
		}
		return liveinfo;
	}


	/**
	 * getPlayersAPIの入力byte[]XMLから放送情報オブジェクトにパースする
	 *
	 * @param source
	 * @return
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	public static String getLiveInfoFromAPIByteArray(byte[] source,
			LiveInfo liveinfo) throws XmlPullParserException, NullPointerException,ParseException,IOException {
		final XmlPullParser xml = Xml.newPullParser();
		String code = "";
//		String input = new String(source,"UTF-8");
//		input = input.replaceAll("&", "&amp");
//		input = input.replaceAll("&", "&amp");
			xml.setInput(new StringReader(new String(source,"UTF-8")));
			int eventType = 0;

			while ((eventType = xml.next()) != XmlPullParser.END_DOCUMENT) {
				if (eventType == XmlPullParser.START_TAG
						&& "id".equals(xml.getName())) {
					liveinfo.setLiveID(getItemString(xml, "id"));
				} else if(eventType == XmlPullParser.START_TAG
						&& "code".equals(xml.getName())) {
					code = getItemString(xml, "code");//エラーチェック
				}else if (eventType == XmlPullParser.START_TAG
						&& "title".equals(xml.getName())) {
					liveinfo.setTitle(getItemString(xml, "title"));
				} else if (eventType == XmlPullParser.START_TAG
						&& "comment_count".equals(xml.getName())) {
					liveinfo.setResNumber(getItemString(xml, "comment_count"));
				} else if (eventType == XmlPullParser.START_TAG
						&& "description".equals(xml.getName())) {
					liveinfo.setDescription(getItemString(xml, "description"));
				} else if (eventType == XmlPullParser.START_TAG
						&& "watch_count".equals(xml.getName())) {
					liveinfo.setViewCount(getItemString(xml, "watch_count"));
				} else if (eventType == XmlPullParser.START_TAG
						&& "owner_name".equals(xml.getName())) {
					liveinfo.setOwnerName(getItemString(xml, "owner_name"));
				} else if (eventType == XmlPullParser.START_TAG
						&& "default_community".equals(xml.getName())) {
					liveinfo.setDefaultCommunity(getItemString(xml,
							"default_community"));
				} else if (eventType == XmlPullParser.START_TAG
						&& "start_time".equals(xml.getName())) {
					liveinfo.setStartTime(getItemString(xml, "start_time"));
				} else if (eventType == XmlPullParser.START_TAG
						&& "base_time".equals(xml.getName())) {
					liveinfo.setBaseTime(getItemString(xml, "base_time"));
				}else if (eventType == XmlPullParser.START_TAG
						&& "profile_image_url".equals(xml.getName())) {
					liveinfo.setThumbnailURL(getItemString(xml,
							"profile_image_url"));
				}else if (eventType == XmlPullParser.START_TAG
						&& "room_label".equals(xml.getName())) {
					liveinfo.setRoomlabel(getItemString(xml,
							"room_label"));
				}else if (eventType == XmlPullParser.START_TAG
						&& "room_seetno".equals(xml.getName())) {
					liveinfo.setRoomno(getItemString(xml,
							"room_seetno"));
				}else if (eventType == XmlPullParser.START_TAG
						&& "addr".equals(xml.getName())) {
					liveinfo.setAddr(getItemString(xml, "addr"));
				} else if (eventType == XmlPullParser.START_TAG
						&& "port".equals(xml.getName())) {
					liveinfo.setPort(getItemString(xml, "port"));
				} else if (eventType == XmlPullParser.START_TAG
						&& "thread".equals(xml.getName())) {
					liveinfo.setThread(getItemString(xml, "thread"));
				}else if (eventType == XmlPullParser.START_TAG
						&& "is_premium".equals(xml.getName())) {
					liveinfo.setIsPremium(getItemString(xml, "is_premium"));
				}
			}

		return code;
	}

	/**
	 * getPlayerAPIのコネクトのインプットソースからLiveInfoオブジェクトを返します
	 *
	 *リクエスト側ができないかも
	 * @param param
	 */
	public static LiveInfo getLiveInfoFromAPIInputStream(InputSource param,
			LiveInfo liveinfo) {
		final XmlPullParser xml = Xml.newPullParser();
//		Log.d("log", "APIparser from IS" + param);

		try {
			xml.setInput(param.getCharacterStream());
			int eventType = 0;
			while ((eventType = xml.next()) != XmlPullParser.END_DOCUMENT) {
				if (eventType == XmlPullParser.START_TAG
						&& "id".equals(xml.getName())) {
					liveinfo.setLiveID(getItemString(xml, "id"));
				} else if (eventType == XmlPullParser.START_TAG
						&& "title".equals(xml.getName())) {
					liveinfo.setTitle(getItemString(xml, "title"));
				} else if (eventType == XmlPullParser.START_TAG
						&& "comment_count".equals(xml.getName())) {
					liveinfo.setResNumber(getItemString(xml, "comment_count"));
				} else if (eventType == XmlPullParser.START_TAG
						&& "description".equals(xml.getName())) {
					liveinfo.setDescription(getItemString(xml, "description"));
				} else if (eventType == XmlPullParser.START_TAG
						&& "watch_count".equals(xml.getName())) {
					liveinfo.setViewCount(getItemString(xml, "watch_count"));
				} else if (eventType == XmlPullParser.START_TAG
						&& "owner_name".equals(xml.getName())) {
					liveinfo.setOwnerName(getItemString(xml, "owner_name"));
				} else if (eventType == XmlPullParser.START_TAG
						&& "default_community".equals(xml.getName())) {
					liveinfo.setDefaultCommunity(getItemString(xml,
							"default_community"));
				} else if (eventType == XmlPullParser.START_TAG
						&& "start_time".equals(xml.getName())) {
					liveinfo.setStartTime(getItemString(xml, "start_time"));
				} else if (eventType == XmlPullParser.START_TAG
						&& "base_time".equals(xml.getName())) {
					liveinfo.setBaseTime(getItemString(xml, "base_time"));
				}else if (eventType == XmlPullParser.START_TAG
						&& "profile_image_url".equals(xml.getName())) {
					liveinfo.setThumbnailURL(getItemString(xml,
							"profile_image_url"));// サムネを取るにはチケットをやり取りしなきゃいけない
				}else if (eventType == XmlPullParser.START_TAG
						&& "room_label".equals(xml.getName())) {
					liveinfo.setRoomlabel(getItemString(xml,
							"room_label"));
				}else if (eventType == XmlPullParser.START_TAG
						&& "room_seetno".equals(xml.getName())) {
					liveinfo.setRoomno(getItemString(xml,
							"room_seetno"));
				}else if (eventType == XmlPullParser.START_TAG
						&& "addr".equals(xml.getName())) {
					liveinfo.setAddr(getItemString(xml, "addr"));
				} else if (eventType == XmlPullParser.START_TAG
						&& "port".equals(xml.getName())) {
					liveinfo.setPort(getItemString(xml, "port"));
				} else if (eventType == XmlPullParser.START_TAG
						&& "thread".equals(xml.getName())) {
					liveinfo.setThread(getItemString(xml, "thread"));
				}else if (eventType == XmlPullParser.START_TAG
						&& "is_premium".equals(xml.getName())) {
					liveinfo.setIsPremium(getItemString(xml, "is_premium"));
				}
			}

		} catch (XmlPullParserException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		return liveinfo;

	}

	/**
	 * getPlayerAPIのコネクトのString版
	 *
	 * @param param
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	public static String getLiveInfoFromAPIString(String param,
			LiveInfo liveinfo) throws XmlPullParserException, IOException {
		final XmlPullParser xml = Xml.newPullParser();
		String code = "";
			xml.setInput(new StringReader(param));
			int eventType = 0;

			while ((eventType = xml.next()) != XmlPullParser.END_DOCUMENT) {
				if (eventType == XmlPullParser.START_TAG
						&& "id".equals(xml.getName())) {
					liveinfo.setLiveID(getItemString(xml, "id"));
				} else if(eventType == XmlPullParser.START_TAG
						&& "code".equals(xml.getName())) {
					code = getItemString(xml, "code");//エラーチェック
				}else if (eventType == XmlPullParser.START_TAG
						&& "title".equals(xml.getName())) {
					liveinfo.setTitle(getItemString(xml, "title"));
				} else if (eventType == XmlPullParser.START_TAG
						&& "comment_count".equals(xml.getName())) {
					liveinfo.setResNumber(getItemString(xml, "comment_count"));
				} else if (eventType == XmlPullParser.START_TAG
						&& "description".equals(xml.getName())) {
					liveinfo.setDescription(getItemString(xml, "description"));
				} else if (eventType == XmlPullParser.START_TAG
						&& "watch_count".equals(xml.getName())) {
					liveinfo.setViewCount(getItemString(xml, "watch_count"));
				} else if (eventType == XmlPullParser.START_TAG
						&& "owner_name".equals(xml.getName())) {
					liveinfo.setOwnerName(getItemString(xml, "owner_name"));
				} else if (eventType == XmlPullParser.START_TAG
						&& "default_community".equals(xml.getName())) {
					liveinfo.setDefaultCommunity(getItemString(xml,
							"default_community"));
				} else if (eventType == XmlPullParser.START_TAG
						&& "start_time".equals(xml.getName())) {
					liveinfo.setStartTime(getItemString(xml, "start_time"));
				} else if (eventType == XmlPullParser.START_TAG
						&& "base_time".equals(xml.getName())) {
					liveinfo.setBaseTime(getItemString(xml, "base_time"));
				}else if (eventType == XmlPullParser.START_TAG
						&& "profile_image_url".equals(xml.getName())) {
					liveinfo.setThumbnailURL(getItemString(xml,
							"profile_image_url"));
				}else if (eventType == XmlPullParser.START_TAG
						&& "room_label".equals(xml.getName())) {
					liveinfo.setRoomlabel(getItemString(xml,
							"room_label"));
				}else if (eventType == XmlPullParser.START_TAG
						&& "room_seetno".equals(xml.getName())) {
					liveinfo.setRoomno(getItemString(xml,
							"room_seetno"));
				}else if (eventType == XmlPullParser.START_TAG
						&& "addr".equals(xml.getName())) {
					liveinfo.setAddr(getItemString(xml, "addr"));
				} else if (eventType == XmlPullParser.START_TAG
						&& "port".equals(xml.getName())) {
					liveinfo.setPort(getItemString(xml, "port"));
				} else if (eventType == XmlPullParser.START_TAG
						&& "thread".equals(xml.getName())) {
					liveinfo.setThread(getItemString(xml, "thread"));
				}else if (eventType == XmlPullParser.START_TAG
						&& "is_premium".equals(xml.getName())) {
					liveinfo.setIsPremium(getItemString(xml, "is_premium"));
				}
			}

		return code;

	}
	/**
	 * getPlayerAPIのbyte[]からオーナー情報のみを取得
	 *
	 * @param param
	 */
	public static String getLiveInfoInputStreamOnlyOwner(byte[] param) {
		final XmlPullParser xml = Xml.newPullParser();

		try {
			xml.setInput(new StringReader(new String(param,"UTF-8")));
			int eventType = 0;
			while ((eventType = xml.next()) != XmlPullParser.END_DOCUMENT) {
			if (eventType == XmlPullParser.START_TAG
						&& "owner_id".equals(xml.getName())) {
					return getItemString(xml, "owner_id");
				}
			}

		} catch (XmlPullParserException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		return null;

	}

	/**
	 * Alert用　getPlayerAPIのbyte[]からオーナー名のみを取得
	 * getPlayerの最低限Notification用
	 */
	public static String getLiveInfoFromAPIbytes(byte[] param) {
		final XmlPullParser xml = Xml.newPullParser();

		try {
			xml.setInput(new StringReader(new String(param,"UTF-8")));
			int eventType = 0;
			while ((eventType = xml.next()) != XmlPullParser.END_DOCUMENT) {
			if (eventType == XmlPullParser.START_TAG
						&& "owner_name".equals(xml.getName())) {
					return getItemString(xml, "owner_name");
				}
			}
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;


	}


	/**
	 * アラートのインフォ情報からスレッドID、ホスト名、ポート番号を取り出す
	 * 未使用
	 */
	public static String[] getParamsFromAlertInfo(byte[] str) {
		final XmlPullParser xml = Xml.newPullParser();
		String[] array = new String[3];
		int eventType = 0;
		try {
			xml.setInput(new StringReader(new String(str, "UTF-8")));

			while ((eventType = xml.next()) != XmlPullParser.END_DOCUMENT) {
				if (eventType == XmlPullParser.START_TAG
						&& "addr".equals(xml.getName())) {
					array[1] = getItemString(xml, "addr");
				} else if (eventType == XmlPullParser.START_TAG
						&& "port".equals(xml.getName())) {
					array[2] = getItemString(xml, "port");
				} else if (eventType == XmlPullParser.START_TAG
						&& "thread".equals(xml.getName())) {
					array[0] = getItemString(xml, "thread");
				}
			}
		} catch (XmlPullParserException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return array;
	}

	/**
	 * アラート鯖から放送新着放送LV CO OWNERIDを取得
	 * リスト参照渡し
	 * 未使用
	 * @param threadPacket
	 * @return
	 */
	public static ArrayList<String> getLVFromAlertRes(String[] threadPacket) {
		final XmlPullParser xml = Xml.newPullParser();
		ArrayList<String> list = new ArrayList<String>();
		for (int i = 0; i < threadPacket.length; i++) {
			int eventType = 0;
			try {
				xml.setInput(new StringReader(threadPacket[i]));

				while ((eventType = xml.next()) != XmlPullParser.END_DOCUMENT) {
					if (eventType == XmlPullParser.START_TAG
							&& "chat".equals(xml.getName())) {
						String tmp = getItemString(xml, "chat");
						list.add(tmp);
					}
				}
			} catch (XmlPullParserException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			} catch (IOException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			}
		}
		return list;
	}





	/**
	 * オーバーレイのマップに
	 * コテハンファイルをパース
	 * (bgcolor name focolor)
	 * @return
	 *
	 *
	 */

	public static int setHandleNameMaps(Map<String,Integer> bgColorMap,Map<String,Integer> foColorMap,Map<String,String> nameMap,byte[] input){

		final XmlPullParser xml = Xml.newPullParser();
		int eventType = 0;
		String bgColor = "-1";
		String foColor = "-16777216";
		String name = "";
		String id = "";
		try {
			xml.setInput(new StringReader(new String(input,"UTF-8")));

			while ((eventType = xml.next()) != XmlPullParser.END_DOCUMENT) {
				if (eventType == XmlPullParser.START_TAG
						&& "user".equals(xml.getName())) {//名前は2番目、背景色は1番目、テキスト色は3番目の属性とする
					bgColor = "-1";
					foColor = "-16777216";
					name = "";
					id = "";
					for(int i= 0; i< xml.getAttributeCount(); i++){
						if(xml.getAttributeName(i).equals("name")){
							name = xml.getAttributeValue(i);
						}else if(xml.getAttributeName(i).equals("bgcolor")){
							bgColor = xml.getAttributeValue(i);
						}else if(xml.getAttributeName(i).equals("focolor")){
							foColor = xml.getAttributeValue(i);
						}
					}
					id = getItemString(xml,"user");
					nameMap.put(id,name);
					bgColorMap.put(id,Integer.parseInt(bgColor));
					foColorMap.put(id, Integer.parseInt(foColor));
				}
			}
		} catch (XmlPullParserException e1) {
//			e1.printStackTrace();
			Log.d("NLiveRoid","XmlPullParserException at setHandleNameMaps");
			return -1;//ファイル不正(0はファイル読み込めない(パスがnull)で使用)
		}catch(NumberFormatException e){
			//色情報に間違った文字があった
//			e.printStackTrace();
			Log.d("NLiveRoid","XmlPullParserException at NumberFormatException");
			return -3;
		} catch(NullPointerException e){
			Log.d("NLiveRoid","HandleNameMap NullPO");
			e.printStackTrace();
			return -2;
		}catch (IOException e) {
			e.printStackTrace();
		}
		return 0;
	}

	/**
	 * 設定画面用コテハンファイルのパース
	 *
	 * @param ids
	 * @param nameList
	 * @param input
	 */
	public static void setHandleNameMaps(
			ArrayList<String> ids,ArrayList<String> nameList,
			ArrayList<Integer> bgColorList,ArrayList<Integer> foColorList,byte[] input,ErrorCode error){

		final XmlPullParser xml = Xml.newPullParser();
		int eventType = 0;
		String bgColor = "-1";
		String foColor = "-16777216";
		String name = "";
		String id = "";
		try {
			xml.setInput(new StringReader(new String(input,"UTF-8")));

			while ((eventType = xml.next()) != XmlPullParser.END_DOCUMENT) {
				if (eventType == XmlPullParser.START_TAG
						&& "user".equals(xml.getName())) {//名前は2番目、背景色は1番目、テキスト色は3番目の属性とする

					//保存してからでないとgetItemStringで進んでしまう
					bgColor = "-1";
					foColor = "-16777216";
					name = "";
					id = "";
					for(int i= 0; i< xml.getAttributeCount(); i++){
						if(xml.getAttributeName(i).equals("name")){
							name = xml.getAttributeValue(i);
						}else if(xml.getAttributeName(i).equals("bgcolor")){
							bgColor = xml.getAttributeValue(i);
						}else if(xml.getAttributeName(i).equals("focolor")){
							foColor = xml.getAttributeValue(i);
						}
					}
					nameList.add((name));
					bgColorList.add(Integer.parseInt(bgColor));
					foColorList.add(Integer.parseInt(foColor));
					id = getItemString(xml,"user");
					ids.add(id);
				}
			}
		} catch (XmlPullParserException e1) {
			// TODO 自動生成された catch ブロック
			e1.printStackTrace();
		}catch(NumberFormatException e){
			//
			e.printStackTrace();
			error.setErrorCode(-44);
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
			error.setErrorCode(-44);
		}

	}

	/**
	 * 設定値をXMLからSettingObjectにセットする
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	public static HashMap<String,String> setSettingValues(byte[] input) throws XmlPullParserException, IOException{
		final XmlPullParser xml = Xml.newPullParser();
		int eventType = 0;
		boolean isPortLayt = true;
		HashMap<String,String> map = new HashMap<String,String>();
		xml.setInput(new StringReader(new String(input,"UTF-8")));

			while ((eventType = xml.next()) != XmlPullParser.END_DOCUMENT) {
				if (eventType == XmlPullParser.START_TAG) {
					//XMLで縦横をportlayt_settingsと、landscape_settingsに分けて、
					//その子要素は、同じ名前なので、ここでフラグで縦横分ける
					if("portlayt_settings".equals(xml.getName())){
						isPortLayt = true;
					}else if("landscape_settings".equals(xml.getName())){
						isPortLayt = false;
					}else if("player_position".equals(xml.getName())){
						if(isPortLayt){
							map.put("player_pos_p", getItemString(xml,xml.getName()));
						}else{
							map.put("player_pos_l", getItemString(xml,xml.getName()));
						}
					}else if("x_position".equals(xml.getName())){
						if(isPortLayt){
							map.put("x_pos_p", getItemString(xml,xml.getName()));
						}else{
							map.put("x_pos_l", getItemString(xml,xml.getName()));
						}
					}else if("x_dragging".equals(xml.getName())){
						if(isPortLayt){
							map.put("xd_enable_p", getItemString(xml,xml.getName()));
						}else{
							map.put("xd_enable_l", getItemString(xml,xml.getName()));
						}
					}else if("y_position".equals(xml.getName())){
						if(isPortLayt){
							map.put("y_pos_p", getItemString(xml,xml.getName()));
						}else{
							map.put("y_pos_l", getItemString(xml,xml.getName()));
						}
					}else if("y_dragging".equals(xml.getName())){
						if(isPortLayt){
							map.put("yd_enable_p", getItemString(xml,xml.getName()));
						}else{
							map.put("yd_enable_l", getItemString(xml,xml.getName()));
						}
					}else if("height".equals(xml.getName())){
						if(isPortLayt){
							map.put("bottom_pos_p", getItemString(xml,xml.getName()));
						}else{
							map.put("bottom_pos_l", getItemString(xml,xml.getName()));
						}
					}else if("font_size".equals(xml.getName())){
						if(isPortLayt){
							map.put("cellheight_p", getItemString(xml,xml.getName()));
						}else{
							map.put("cellheight_l", getItemString(xml,xml.getName()));
						}
					}else if("type_width".equals(xml.getName())){
						if(isPortLayt){
							map.put("type_width_p", getItemString(xml,xml.getName()));
						}else{
							map.put("type_width_l", getItemString(xml,xml.getName()));
						}
					}else if("id_width".equals(xml.getName())){
						if(isPortLayt){
							map.put("id_width_p", getItemString(xml,xml.getName()));
						}else{
							map.put("id_width_l", getItemString(xml,xml.getName()));
						}
					}else if("cmd_width".equals(xml.getName())){
						if(isPortLayt){
							map.put("command_width_p", getItemString(xml,xml.getName()));
						}else{
							map.put("command_width_l", getItemString(xml,xml.getName()));
						}
					}else if("time_width".equals(xml.getName())){
						if(isPortLayt){
							map.put("time_width_p", getItemString(xml,xml.getName()));
						}else{
							map.put("time_width_l", getItemString(xml,xml.getName()));
						}
					}else if("score_width".equals(xml.getName())){
						if(isPortLayt){
							map.put("score_width_p", getItemString(xml,xml.getName()));
						}else{
							map.put("score_width_l", getItemString(xml,xml.getName()));
						}
					}else if("num_width".equals(xml.getName())){
						if(isPortLayt){
							map.put("num_width_p", getItemString(xml,xml.getName()));
						}else{
							map.put("num_width_l", getItemString(xml,xml.getName()));
						}
					}else if("comment_width".equals(xml.getName())){
						if(isPortLayt){
							map.put("comment_width_p", getItemString(xml,xml.getName()));
						}else{
							map.put("comment_width_l", getItemString(xml,xml.getName()));
						}
					}else if("width".equals(xml.getName())){
						if(isPortLayt){
							map.put("width_p", getItemString(xml,xml.getName()));
						}else{
							map.put("width_l", getItemString(xml,xml.getName()));
						}
					}else{
						//設定値が入っていないタグは飛ばす
						if(!(xml.getName().equals("common_settings")||xml.getName().equals("column_sequence")
								||xml.getName().equals("column_settings")||xml.getName().equals("command_settings")
								||xml.getName().equals("spplayer_settings")||xml.getName().equals("Settings")
								||xml.getName().equals("speech_settings"))){
					map.put(xml.getName(), getItemString(xml,xml.getName()));
						}
					}
				}
			}
//			Log.d("XMLparser","XML " + map);
		return map;
	}

	/**
	 * コミュニティ画面用ファイルのパース
	 *
	 * @param ids
	 * @param names
	 * @param input
	 */
	public static void setCommunityMaps(ArrayList<String> ids,ArrayList<String> ownernames,ArrayList<String> titles,HashMap<String,String> isAlert,byte[] input){

		final XmlPullParser xml = Xml.newPullParser();
		int eventType = 0;
		try {
			xml.setInput(new StringReader(new String(input,"UTF-8")));

			while ((eventType = xml.next()) != XmlPullParser.END_DOCUMENT) {
				if (eventType == XmlPullParser.START_TAG
						&& "community".equals(xml.getName())) {//名前は2番目、背景色は1番目の属性とする
					String id = xml.getAttributeValue(0);//保存してからでないとgetItemStringで進んでしまう
					String ownname = xml.getAttributeValue(1);
					String title = xml.getAttributeValue(2);
					String value = getItemString(xml,"community");
					ids.add(id);
					ownernames.add(ownname);
					titles.add(title);
					isAlert.put(id,value);
				}
			}
		} catch (XmlPullParserException e1) {
			// TODO 自動生成された catch ブロック
			e1.printStackTrace();
		}catch(NumberFormatException e){
			//
			e.printStackTrace();
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}

	}

	/**
	 * 放送情報プロファイルをパース
	 */
	public static void parseLiveProfile(String source,HashMap<String,String> settings){

		final XmlPullParser xml = Xml.newPullParser();
		int eventType = 0;
		int tagIndex = 0;
		int lockIndex = 0;
		try {
			xml.setInput(new StringReader(source));

			while ((eventType = xml.next()) != XmlPullParser.END_DOCUMENT) {
				if (eventType == XmlPullParser.START_TAG
						&& "title".equals(xml.getName())) {
					settings.put("title",getItemString(xml,"title"));
				}else if (eventType == XmlPullParser.START_TAG
						&& "description".equals(xml.getName())) {
					settings.put("description",getItemString(xml,"description"));
				}else if (eventType == XmlPullParser.START_TAG
						&& "community_name".equals(xml.getName())) {
					settings.put("community_name",getItemString(xml,"community_name"));
				}else if (eventType == XmlPullParser.START_TAG
						&& "category".equals(xml.getName())) {
					settings.put("category",getItemString(xml,"category"));
				}else if (eventType == XmlPullParser.START_TAG
						&& "tag".equals(xml.getName())) {
					settings.put("tag" + tagIndex,getItemString(xml,"tag"));
					tagIndex++;
				}else if (eventType == XmlPullParser.START_TAG
						&& "public_status".equals(xml.getName())) {
					settings.put("public_status",getItemString(xml,"public_status"));
				}else if (eventType == XmlPullParser.START_TAG
						&& "timeshift_enable".equals(xml.getName())) {
					settings.put("timeshift_enable",getItemString(xml,"timeshift_enable"));
				}else if (eventType == XmlPullParser.START_TAG
						&& "lock".equals(xml.getName())) {
					settings.put("lock"+lockIndex,getItemString(xml,"lock"));
					lockIndex++;
				}else if (eventType == XmlPullParser.START_TAG
						&& "use_camera".equals(xml.getName())) {
					settings.put("use_camera",getItemString(xml,"use_camera"));
				}else if (eventType == XmlPullParser.START_TAG
						&& "use_mic".equals(xml.getName())) {
					settings.put("use_mic",getItemString(xml,"use_mic"));
				}else if (eventType == XmlPullParser.START_TAG
						&& "back_camera".equals(xml.getName())) {
					settings.put("back_camera",getItemString(xml,"back_camera"));
				}else if (eventType == XmlPullParser.START_TAG
						&& "back_mic".equals(xml.getName())) {
					settings.put("back_mic",getItemString(xml,"back_mic"));
				}else if (eventType == XmlPullParser.START_TAG
						&& "ring_camera".equals(xml.getName())) {
					settings.put("ring_camera",getItemString(xml,"ring_camera"));
				}else if (eventType == XmlPullParser.START_TAG
						&& "ring_mic".equals(xml.getName())) {
					settings.put("ring_mic",getItemString(xml,"ring_mic"));
				}else if (eventType == XmlPullParser.START_TAG
						&& "live_mode".equals(xml.getName())) {
					settings.put("live_mode",getItemString(xml,"live_mode"));
				}else if (eventType == XmlPullParser.START_TAG
						&& "resolution_index".equals(xml.getName())) {
					settings.put("resolution_index",getItemString(xml,"resolution_index"));
				}else if (eventType == XmlPullParser.START_TAG
						&& "fps".equals(xml.getName())) {
					settings.put("fps",getItemString(xml,"fps"));
				}else if (eventType == XmlPullParser.START_TAG
						&& "keyframe_interval".equals(xml.getName())) {
					settings.put("keyframe_interval",getItemString(xml,"keyframe_interval"));
				}else if (eventType == XmlPullParser.START_TAG
						&& "scene".equals(xml.getName())) {
					settings.put("scene",getItemString(xml,"scene"));
				}else if (eventType == XmlPullParser.START_TAG
						&& "is_stereo".equals(xml.getName())) {
					settings.put("is_stereo",getItemString(xml,"is_stereo"));
				}else if (eventType == XmlPullParser.START_TAG
						&& "volume".equals(xml.getName())) {
					settings.put("volume",getItemString(xml,"volume"));
				}else if (eventType == XmlPullParser.START_TAG
						&& "movie_path".equals(xml.getName())) {
					settings.put("movie_path",getItemString(xml,"movie_path"));
				}
			}
		} catch (XmlPullParserException e1) {
			e1.printStackTrace();
		}catch(NumberFormatException e){
			//
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/**
	 * 放送情報プロファイルから、Flashの情報のみを取得する
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	public static String[] parseLiveProfile(String source,String[] result) throws XmlPullParserException, IOException{

		String[] use_miccamera = new String[2];
		String resolution = "160,120";
		final XmlPullParser xml = Xml.newPullParser();
		int eventType = 0;
			xml.setInput(new StringReader(source));

			while ((eventType = xml.next()) != XmlPullParser.END_DOCUMENT) {
				//<br>タグで失敗する

				if (eventType == XmlPullParser.START_TAG
						&& "use_camera".equals(xml.getName())) {
					use_miccamera[0] = getItemString(xml,"use_camera");
				}else if (eventType == XmlPullParser.START_TAG
						&& "camera_device_index".equals(xml.getName())) {
					result[4] = getItemString(xml,"camera_device_index");
				}else if (eventType == XmlPullParser.START_TAG
						&& "encode".equals(xml.getName())) {
					result[5] = getItemString(xml,"encode");
				}else if (eventType == XmlPullParser.START_TAG
						&& "quality".equals(xml.getName())) {
					result[6] = getItemString(xml,"quality");
				}else if (eventType == XmlPullParser.START_TAG
						&& "resolution".equals(xml.getName())) {
					resolution = getItemString(xml,"resolution");
				}else if (eventType == XmlPullParser.START_TAG
						&& "fps".equals(xml.getName())) {
					result[9] = getItemString(xml,"fps");
				}else if (eventType == XmlPullParser.START_TAG
						&& "keyframe_interval".equals(xml.getName())) {
					result[10] = getItemString(xml,"keyframe_interval");
				}else if (eventType == XmlPullParser.START_TAG
						&& "motion_detect".equals(xml.getName())) {
					result[11] = getItemString(xml,"motion_detect");
				}else if (eventType == XmlPullParser.START_TAG
						&& "motion_timeout".equals(xml.getName())) {
					result[12] = getItemString(xml,"motion_timeout");
				}else if (eventType == XmlPullParser.START_TAG
						&& "band_width".equals(xml.getName())) {
					result[13] = getItemString(xml,"band_width");
				}else if (eventType == XmlPullParser.START_TAG
						&& "use_mic".equals(xml.getName())) {
					use_miccamera[1] = getItemString(xml,"use_mic");
				}else if (eventType == XmlPullParser.START_TAG
						&& "mic_device_index".equals(xml.getName())) {
					result[14] = getItemString(xml,"mic_device_index");
				}else if (eventType == XmlPullParser.START_TAG
						&& "codec".equals(xml.getName())) {
					result[15] = getItemString(xml,"codec");
				}else if (eventType == XmlPullParser.START_TAG
						&& "volume".equals(xml.getName())) {
					result[16] = getItemString(xml,"volume");
				}else if (eventType == XmlPullParser.START_TAG
						&& "gain".equals(xml.getName())) {
					result[17] = getItemString(xml,"gain");
				}else if (eventType == XmlPullParser.START_TAG
						&& "echo_suppression".equals(xml.getName())) {
					result[18] = getItemString(xml,"echo_suppression");
				}else if (eventType == XmlPullParser.START_TAG
						&& "chapture_rate".equals(xml.getName())) {
					result[19] = getItemString(xml,"chapture_rate");
				}else if (eventType == XmlPullParser.START_TAG
						&& "silent_detect".equals(xml.getName())) {
					result[21] = getItemString(xml,"silent_detect");
				}else if (eventType == XmlPullParser.START_TAG
						&& "silent_timeout".equals(xml.getName())) {
					result[22] = getItemString(xml,"silent_timeout");
				}else if (eventType == XmlPullParser.START_TAG
						&& "fix_landscape".equals(xml.getName())) {
					result[23] = getItemString(xml,"fix_landscape");
				}else if (eventType == XmlPullParser.START_TAG
						&& "preview".equals(xml.getName())) {
					result[24] = getItemString(xml,"preview");
				}
			}

			try{
				String[] temp = resolution.split(",");
				result[7] = temp[0];
				result[8] = temp[1];
			}catch(Exception e){//失敗したらデフォ値
				e.printStackTrace();
			}
			result[1] = use_miccamera[0]+use_miccamera[1];

		return result;
	}


	/**
	 * 順番待ちの人数をwaitinfo/countから取得
	 */

	public static String getWaitingCount(byte[] param){
		final XmlPullParser xml = Xml.newPullParser();

		try {
			xml.setInput(new StringReader(new String(param,"UTF-8")));
			int eventType = 0;
			String value = "";
			while ((eventType = xml.next()) != XmlPullParser.END_DOCUMENT) {
				if (eventType == XmlPullParser.START_TAG
						&& "count".equals(xml.getName())) {
					return getItemString(xml,"count");
				}
			}
		} catch (XmlPullParserException e) {
			e.printStackTrace();
			return "";
		} catch (IOException e) {
			e.printStackTrace();
			return "";
		}catch(Exception e){
			return "";
		}
		return "";
	}


	/**
	 * Heatbeatから来場コメ数取得
	 */

	public static String[] getHeatBeat(byte[] param){
		final XmlPullParser xml = Xml.newPullParser();
		String watch = "";
		String comments = "";
		try {
			xml.setInput(new StringReader(new String(param,"UTF-8")));
			int eventType = 0;
			while ((eventType = xml.next()) != XmlPullParser.END_DOCUMENT) {
				if (eventType == XmlPullParser.START_TAG
						&& "watchCount".equals(xml.getName())) {
					watch =  getItemString(xml,"watchCount");
				}else if (eventType == XmlPullParser.START_TAG
						&& "commentCount".equals(xml.getName())) {
					comments =  getItemString(xml,"commentCount");
				}
			}
			return new String[]{comments,watch};
		} catch (XmlPullParserException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}catch(Exception e){
			return null;
		}
	}

	/**
	 * end_timeを取得
	 * @param source
	 * @return
	 */
	public static String getTokenInfoFromAPIByteArray(byte[] source) {
			final XmlPullParser xml = Xml.newPullParser();
			try {
				xml.setInput(new StringReader(new String(source,"UTF-8")));
				int eventType = 0;

				while ((eventType = xml.next()) != XmlPullParser.END_DOCUMENT) {
					if (eventType == XmlPullParser.START_TAG
							&& "end_time".equals(xml.getName())) {
						return getItemString(xml,"end_time");
					}
				}
			} catch (XmlPullParserException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (NullPointerException e) {
				return null;
			} catch (ParseException e){
				e.printStackTrace();
			}
			return null;
	}

	/**
	 * 教育リストを読み込む
	 * @param source
	 * @param map
	 */
	public static int parseLiveEducation(byte[] source,
			LinkedHashMap<String,String> educationList) {
		final XmlPullParser xml = Xml.newPullParser();
		String tempKey = "";
		try {
			xml.setInput(new StringReader(new String(source,"UTF-8")));
			int eventType = 0;

			while ((eventType = xml.next()) != XmlPullParser.END_DOCUMENT) {
				if (eventType == XmlPullParser.START_TAG
						&& "data".equals(xml.getName())) {
				}else if(eventType == XmlPullParser.START_TAG
						&& "key".equals(xml.getName())){
					tempKey = getItemString(xml,"key");
						educationList.put(tempKey.replaceAll("&lt;","<"),"");
				}else if(eventType == XmlPullParser.START_TAG
						&& "value".equals(xml.getName())){//順番に読んでいく(順番が間違ったりしていたらできない!?)
					String itemStr = getItemString(xml,"value");
					if(itemStr == null){
						itemStr="";
					}else{
						itemStr.replaceAll("&gt", ">");
					}
					educationList.put(tempKey,itemStr);
				}
			}
		} catch (XmlPullParserException e) {
			e.printStackTrace();
			return -1;
		} catch (IOException e) {
			e.printStackTrace();
			return -1;
		} catch (NullPointerException e) {
			e.printStackTrace();
			return -2;
		} catch (ParseException e){
			e.printStackTrace();
			return -1;
		}
		return 0;

	}

	public static int getAlertList(ArrayList<String> alertList,
			byte[] readBytes) {
		Log.d("NLiveRoid","GetAlertList");
		final XmlPullParser xml = Xml.newPullParser();
		String str = "";
		try {
			xml.setInput(new StringReader(new String(readBytes,"UTF-8")));
			int eventType = 0;
			while ((eventType = xml.next()) != XmlPullParser.END_DOCUMENT) {
				if(eventType == XmlPullParser.START_TAG
						&& "id".equals(xml.getName())){
					str = getItemString(xml,"id");
					Log.d("NLiveRoid","GetAlertList add " + str);
					alertList.add(str);
				}
			}
		} catch (XmlPullParserException e) {
			e.printStackTrace();
			return -2;
		} catch (IOException e) {
			e.printStackTrace();
			return -2;
		} catch (NullPointerException e) {
			e.printStackTrace();
			return -2;
		} catch (ParseException e){
			e.printStackTrace();
			return -1;
		}
		return 0;
	}

}
