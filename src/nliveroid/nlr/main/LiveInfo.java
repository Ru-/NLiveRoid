package nliveroid.nlr.main;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;

import android.graphics.Bitmap;
import android.util.Log;

public class LiveInfo implements Cloneable ,Serializable{
	/**
	 *
	 */
	private static final long serialVersionUID =-7674852638292555620L;
	private String liveID;//エラーなどはこのフィールドで判別する -1ユーザ情報がない -2ネットワークに接続できない
	private String title = "";//null文字が入らないように
	private String ownerName = URLEnum.HYPHEN;
	private String ownerID = null;
	private String communityID = URLEnum.HYPHEN;
	private String coomunityName = URLEnum.HYPHEN;
	private String community_name = URLEnum.HYPHEN;
	private String community_info = URLEnum.HYPHEN;
	private String startTime = URLEnum.HYPHEN;
	private String passedTime ="Lost status..";
	private String description = "";
	private String watchcount = URLEnum.HYPHEN;
	private String resnum = URLEnum.HYPHEN;
	private String thumbnailURL = "";
	transient private Bitmap thumbnail = null;
	private byte[] thumbNailTempBitmap;
	transient private Bitmap[] tagsBitmap;
	private short rankingValue;//(000000XX 00000000)2バイト目がup0down1even10で残りの1ビットがランク1～50←結局50まで使うから、1バイトにするの無理
	private String addr;
	private String port;
	private String thread;
	private String isPremium = "1";
	private String baseTime = URLEnum.HYPHEN;
	private String roomlabel = URLEnum.HYPHEN;
	private String roomno = URLEnum.HYPHEN;
	private String reservedcount = URLEnum.HYPHEN;
	private String tags = "";//null文字が入らないように
	private boolean isMemberOnly;
	private boolean isTimeShiftEnable;
	private String token;
	private String tsReserveToken;
	private String rtmpurl;
	private String endtime;
	private boolean isLiveStarted;
	private boolean isOwner;
	private String categoryname;
	/**
	 * passedTimeを取得します。
	 * @return passedTime
	 */
	public String getPassedTime() {
		if(passedTime.equals("Lost status..")){
			return passedTime;
		}else if(passedTime.equals("RESERVED")){
			return "予約枠";
		}else{
//			this.passedTime = String.valueOf((int)(System.currentTimeMillis() / 1000 - Integer.parseInt(getStartTime()))/60);
	    return passedTime + "分経過";
		}
	}

	public String getPassedTime(boolean b) {
		return passedTime;
	}
	/**
	 * ownerNameを取得します。
	 * @return ownerName
	 */
	public String getOwnerName() {
	    return ownerName;
	}

	/**
	 * ownerNameを設定します。
	 * @param ownerName ownerName
	 */
	public void setOwnerName(String ownerName) {
	    this.ownerName = ownerName;
	}

	/**
	 * titleを取得します。
	 * @return title
	 */
	public String getTitle() {
	    return title;
	}

	/**
	 * titleを設定します。
	 * @param title title
	 */
	public void setTitle(String title) {
	    this.title = title;
	}
	public String getLiveID(){
		return liveID;
	}
	public void setLiveID(String itemString) {
		this.liveID = itemString;
	}
	/**
	 * ランキングをパース下時の経過時間をセット
	 * 未使用のRSSでも使用
	 * @param passedTime passedTime
	 */
	public void setPassedTime(String passedTime) {
	    this.passedTime = passedTime;
	}

	/**
	 * resnumを取得します。
	 * @return resnum
	 */
	public String getResNumber() {
	    return resnum;
	}
	public void setResNumber(String itemString) {
		this.resnum = itemString;
	}
	public void setViewCount(String itemString) {
		this.watchcount = itemString;
	}

	public void setStartTime(String attributeValue) {
		this.startTime = attributeValue;
	}

	/**
	 * ownerIDを取得します。
	 * @return ownerID
	 */
	public String getOwnerID() {
	    return ownerID;
	}

	/**
	 * ownerIDを設定します。
	 * @param ownerID ownerID
	 */
	public void setOwnerID(String ownerID) {
	    this.ownerID = ownerID;
	}

	/**
	 * communityIDを取得します。
	 * @return communityID
	 */
	public String getCommunityID() {
	    return communityID;
	}
	/**
	 * communityIDを設定します。
	 * @param communityID communityID
	 */
	public void setCommunityID(String communityID) {
	    this.communityID = communityID;
	}
	/**
	 * coomunityNameを取得します。
	 * @return coomunityName
	 */
	public String getCommunityName() {
	    return coomunityName;
	}
	/**
	 * coomunityNameを設定します。
	 * @param coomunityName coomunityName
	 */
	public void setComunityName(String coomunityName) {
	    this.coomunityName = coomunityName;
	}
	/**
	 * community_infoを取得します。
	 * @return community_info
	 */
	public String getCommunity_info() {
	    return community_info;
	}
	/**
	 * community_infoを設定します。
	 * @param community_info community_info
	 */
	public void setCommunity_info(String community_info) {
	    this.community_info = community_info;
	}
	public String getStartTime(){
//		 SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
//       String date = sdf.format(new Date(Long.parseLong(attributeValue)));
//		 startTime = String.valueOf(Integer.parseInt(startTime)/60);
		return startTime;
	}

	/**
	 * デフォルトのコミュニティIDを取得します
	 *
	 */

	public String getDefaultCommunity(){
		return community_name;
	}
	public void setDefaultCommunity(String commu){
		this.community_name = commu;
	}
	/**
	 * descriptionを取得します。
	 * @return description
	 */
	public String getDescription() {
	    return description;
	}
	/**
	 * itemStringを設定します。
	 * @param itemString itemString
	 */
	public void setDescription(String itemString) {
		this.description = itemString;
	}
	/**
	 * watchcountを取得します。
	 * @return watchcount
	 */
	public String getViewCount() {
	    return watchcount;
	}

	/**
	 * thumbnailを設定します。
	 * @param thumbnail thumbnail
	 */
	public void setThumbnailURL(String thumbnail) {
	    this.thumbnailURL = thumbnail;
	}

	/**
	 * thumbnailURLを取得します。
	 * @return thumbnailURL
	 */
	public String getThumbnailURL() {
	    return thumbnailURL;
	}
	/**
	 * thumbnaiを設定します。
	 * @param thumbnai thumbnai
	 */
	public void setThumbnail(Bitmap thumbnai) {
	    this.thumbnail = thumbnai;
	}

	/**
	 * thumbnailを取得します。
	 * @return thumbnail
	 */
	public Bitmap getThumbnail() {
	    return thumbnail;
	}

	/**
	 * サムネイルをバイト配列にする
	 */
	public final void serializeBitmap() {
		if(thumbnail != null){
	     ByteArrayOutputStream bout = new ByteArrayOutputStream();
	     thumbnail.compress(Bitmap.CompressFormat.PNG ,100, bout);
	     thumbNailTempBitmap = bout.toByteArray();
		}
	 }

	public byte[] getBitmapArray(){
		return thumbNailTempBitmap;
	}

	@Override
	public LiveInfo clone(){
		try {
			return (LiveInfo)super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * rankingValueを取得します。
	 * @return rankingValue
	 */
	public short getRankingValue() {
	    return rankingValue;
	}
	/**
	 * rankingValueを設定します。
	 * @param rankingValue rankingValue
	 */
	public void setRankingValue(short rankingValue) {
	    this.rankingValue = rankingValue;
	}
	/**
	 * addrを取得します。
	 * @return addr
	 */
	public String getAddr() {
	    return addr;
	}
	/**
	 * addrを設定します。
	 * @param addr addr
	 */
	public void setAddr(String addr) {
	    this.addr = addr;
	}
	/**
	 * portを取得します。
	 * @return port
	 */
	public String getPort() {
	    return port;
	}
	/**
	 * portを設定します。
	 * @param port port
	 */
	public void setPort(String port) {
	    this.port = port;
	}
	/**
	 * threadを取得します。
	 * @return thread
	 */
	public String getThread() {
	    return thread;
	}
	/**
	 * threadを設定します。
	 * @param thread thread
	 */
	public void setThread(String thread) {
	    this.thread = thread;
	}
	/**
	 * bitMapArrayを取得します。
	 * @return bitMapArray
	 */
	public byte[] getBitMapArray() {
	    return thumbNailTempBitmap;
	}
	/**
	 * bitMapArrayを設定します。
	 * @param bitMapArray bitMapArray
	 */
	public void setBitMapArray(byte[] bitMapArray) {
	    this.thumbNailTempBitmap = bitMapArray;
	}
	/**
	 * isPremiumを取得します。
	 * @return isPremium
	 */
	public String getIsPremium() {
	    return isPremium;
	}
	/**
	 * isPremiumを設定します。
	 * @param isPremium isPremium
	 */
	public void setIsPremium(String isPremium) {
		Log.d("ISPREMIUM ---  SET ", " " + isPremium);
	    this.isPremium = isPremium.replaceAll("\t|\n| |　", "");
	}
	/**
	 * baseTimeを取得します。
	 * @return baseTime
	 */
	public String getBaseTime() {
	    return baseTime;
	}
	/**
	 * baseTimeを設定します。
	 * @param baseTime baseTime
	 */
	public void setBaseTime(String baseTime) {
	    this.baseTime = baseTime;
	}
	/**
	 * roomlabelを取得します。
	 * @return roomlabel
	 */
	public String getRoomlabel() {
	    return roomlabel;
	}
	/**
	 * roomlabelを設定します。
	 * @param roomlabel roomlabel
	 */
	public void setRoomlabel(String roomlabel) {
	    this.roomlabel = roomlabel;
	}
	/**
	 * roomnoを取得します。
	 * @return roomno
	 */
	public String getRoomno() {
	    return roomno;
	}
	/**
	 * roomnoを設定します。
	 * @param roomno roomno
	 */
	public void setRoomno(String roomno) {
	    this.roomno = roomno;
	}
	/**
	 * reservedcountを取得します。
	 * @return reservedcount
	 */
	public String getReservedcount() {
	    return reservedcount;
	}
	/**
	 * reservedcountを設定します。
	 * @param reservedcount reservedcount
	 */
	public void setReservedcount(String reservedcount) {
	    this.reservedcount = reservedcount;
	}
	/**
	 * tagsを取得します。
	 * @return tags
	 */
	public String getTags() {
	    return tags;
	}
	/**
	 * tagsを設定します。
	 * @param tags tags
	 */
	public void setTags(String tags) {
	    this.tags = tags;
	}
	/**
	 * isMemberOnlyを取得します。
	 * @return isMemberOnly
	 */
	public boolean isMemberOnly() {
	    return isMemberOnly;
	}
	/**
	 * isMemberOnlyを設定します。
	 * @param isMemberOnly isMemberOnly
	 */
	public void setMemberOnly(boolean isMemberOnly) {
	    this.isMemberOnly = isMemberOnly;
	}

	/**
	 * isTimeShiftEnableを取得します。
	 * @return isTimeShiftEnable
	 */
	public boolean isTimeShiftEnable() {
	    return isTimeShiftEnable;
	}
	/**
	 * isTimeShiftEnableを設定します。
	 * @param isTimeShiftEnable isTimeShiftEnable
	 */
	public void setTimeShiftEnable(boolean isTimeShiftEnable) {
	    this.isTimeShiftEnable = isTimeShiftEnable;
	}
	public String getToken() {
		return token;
	}
	public void setToken(String itemString) {
		this.token = itemString;
	}
	/**
	 * tsReserveTokenを取得します。
	 * @return tsReserveToken
	 */
	public String getTsReserveToken() {
	    return tsReserveToken;
	}

	/**
	 * tsReserveTokenを設定します。
	 * @param tsReserveToken tsReserveToken
	 */
	public void setTsReserveToken(String tsReserveToken) {
	    this.tsReserveToken = tsReserveToken;
	}

	/**
	 * rtmpurlを取得します。
	 * @return rtmpurl
	 */
	public String getRtmpurl() {
	    return rtmpurl;
	}
	/**
	 * rtmpurlを設定します。
	 * @param rtmpurl rtmpurl
	 */
	public void setRtmpurl(String rtmpurl) {
	    this.rtmpurl = rtmpurl;
	}

	public String getEndTime() {
		return endtime;
	}

	public void setEndTime(String itemString) {
		this.endtime = itemString;
	}
	/**
	 * isLiveTestを取得します。
	 * @return isLiveTest
	 */
	public boolean isLiveStarted() {
	    return isLiveStarted;
	}
	/**
	 * isLiveTestを設定します。
	 * @param isLiveTest isLiveTest
	 */
	public void setLiveStarted(boolean isLiveStarted) {
	    this.isLiveStarted = isLiveStarted;
	}
	/**
	 * isOwnerを取得します。
	 * @return isOwner
	 */
	public boolean isOwner() {
	    return isOwner;
	}
	/**
	 * isOwnerを設定します。
	 * @param isOwner isOwner
	 */
	public void setOwner(boolean isOwner) {
	    this.isOwner = isOwner;
	}
	public String getCategoryName() {
		return categoryname;
	}
	public void setCategoryName(String string) {
		this.categoryname = string;
	}



}

