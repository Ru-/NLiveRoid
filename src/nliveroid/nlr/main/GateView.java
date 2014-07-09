package nliveroid.nlr.main;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

/**
 * Gateのインフレートクラス
 *
 * @author Owner
 *
 */
public class GateView {
	private View view;
	private View progressParent;
	private ViewGroup progressArea;
	private ImageView commuThumbNail;
	private TextView start;
//	private TextView open;
	private TextView seet_num;
	private TextView commuName;
	private LinearLayout tagP;
	private LinearLayout titleP;
	private TextView titleLabel;
	private TextView descLabel;
	private TextView tagLabel;
	private TextView commuLabel;
	private TableLayout maintable;
	private LinearLayout descP;
	private LinearLayout commuP;
	private TextView viewCount;
	private TextView resNum;
	private int width;
	private int height;
	private FrameLayout close;
	private Button reserveBt;
	private Button tagLinkBt;
	private Button communityBt;
	private Button copyBt;
	private Button browserBt;
	private Button snsBt;
	private Button goPlayerB;
	private TextView ownerName;
	private TextView passedtime;
	private TableRow lParent;

	public GateView(Context context) {

		LayoutInflater inflater = LayoutInflater.from(context);
		view = inflater.inflate(R.layout.gatelayout, null);
		view.setVisibility(View.INVISIBLE);
		// メインのスクロールビュー
		maintable = (TableLayout) view
				.findViewById(R.id.gate_maintable);
		tagLabel = (TextView) view.findViewById(R.id.gate_taglabel);
		titleLabel = (TextView) view.findViewById(R.id.gate_titlelabel);
		commuLabel = (TextView) view.findViewById(R.id.gate_commulabel);
		descLabel = (TextView) view.findViewById(R.id.gate_desclabel);
		close = (FrameLayout) view.findViewById(R.id.gate_close_frame);

		progressParent = inflater.inflate(R.layout.progressbar, null);
		progressArea = (ViewGroup) view.findViewById(R.id.progresslinear);
		ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.ProgressBarHorizontal);
		progressArea.removeView(progressBar);

		commuThumbNail = (ImageView) view.findViewById(R.id.community_thumbnail);
		commuThumbNail.setScaleType(ImageView.ScaleType.CENTER_CROP);
		start = (TextView) view.findViewById(R.id.open_livetime);
		seet_num = (TextView) view.findViewById(R.id.seet_num);
//		open = (TextView) view.findViewById(R.id.open_roomtime);
		passedtime = (TextView)view.findViewById(R.id.passed_time);
		commuName = (TextView) view.findViewById(R.id.gate_community_name);
		ownerName = (TextView)view.findViewById(R.id.gate_owner_name);
		// タグテキストの親
		tagP = (LinearLayout) view.findViewById(R.id.gate_tags_parent);
		// タイトルテキストの親
		titleP = (LinearLayout) view.findViewById(R.id.gate_title_parent);
		// 詳細の親
		descP = (LinearLayout) view.findViewById(R.id.gate_desc_parent);
		// コミュニティインフォの親
		commuP = (LinearLayout) view.findViewById(R.id.gate_commuinfo_parent);
		//来場、コメ数
		viewCount = (TextView) view.findViewById(R.id.gate_viewcount);
		resNum = (TextView) view.findViewById(R.id.gate_resnum);

		reserveBt = (Button)view.findViewById(R.id.gate_reserve_bt);

		NLiveRoid app = (NLiveRoid)context.getApplicationContext();
		this.width = (int) (app.getViewWidth()*app.getMetrics());
		this.height = (int) (app.getViewHeight()*app.getMetrics());
		//何故メトリックスをかけなきゃいけないのかがわからない
		int bWidth = (int) (width/6);
		// 下のボタン
		Log.d("NLiveRoid","GateBt " + width);
		copyBt = new Button(context);
		copyBt.setText("コピー");
		copyBt.setTextSize(width/65);
		communityBt = new Button(context);
		communityBt.setText("コミュ機能");
		communityBt.setTextSize(width/80);
		browserBt = new Button(context);
		browserBt.setText("ブラウザ");
		browserBt.setTextSize(width/65);
		tagLinkBt = new Button(context);
		tagLinkBt.setText("タグ");
		tagLinkBt.setTextSize(width/60);
		snsBt =  new Button(context);
		snsBt.setText("SNS");
		snsBt.setTextSize(width/60);
		goPlayerB =  new Button(context);
		goPlayerB.setText("視聴する");
		goPlayerB.setGravity(Gravity.TOP);
		lParent = (TableRow)view.findViewById(R.id.gate_linkbutton_parent);
		lParent.addView(copyBt,bWidth,80);
		lParent.addView(communityBt,bWidth,80);
		lParent.addView(browserBt,bWidth,80);
		lParent.addView(tagLinkBt,bWidth,80);
		lParent.addView(snsBt,bWidth,80);
		lParent.addView(goPlayerB,bWidth*2,80);
	}

	public void clearViewStatus(){
		commuThumbNail.setImageBitmap(null);
		start.setText("--:--");
		commuName.setText(URLEnum.HYPHEN);
		titleLabel.setText("タイトル");
		descLabel.setText("放送詳細");
		tagLabel.setText("タグ");
		commuLabel.setText("コミュニティ情報");
		viewCount.setText(URLEnum.HYPHEN);
		resNum.setText(URLEnum.HYPHEN);
//		open.setText("--:--");
		ownerName.setText(URLEnum.HYPHEN);
		tagP.removeAllViews();
		titleP.removeAllViews();
		descP.removeAllViews();
		commuP.removeAllViews();
	}

	public void udate(LiveInfo liveinfo) {

		commuThumbNail.setImageBitmap(liveinfo.getThumbnail());
		String c = liveinfo.getCommunityName();
		if (c == null || c.equals(URLEnum.HYPHEN)) {
			commuName.setText(liveinfo.getCommunityID());
		} else {
			commuName.setText(c);
		}

		Matcher smc = Pattern.compile("[0-9]+:[0-9]+").matcher(
				liveinfo.getStartTime());
		if (smc.find()) {
			start.setText(smc.group());
		}
	}

	/**
	 * viewを取得します。
	 *
	 * @return view
	 */
	public View getView() {
		return view;
	}
	/**
	 * parentScrollViewを取得します。
	 *
	 * @return parentScrollView
	 */
	public TableLayout getMainTable() {
		return maintable;
	}
	/**
	 * commuThumbNailを取得します
	 */
	public ImageView getCommuThumbView(){
		return commuThumbNail;
	}
	//テキスト区画の親取得-----------------
	/**
	 * tagpを取得します。
	 *
	 * @return tagp
	 */
	public LinearLayout getTagP() {
		return tagP;
	}
	/**
	 * titを取得します。
	 *
	 * @return tit
	 */
	public LinearLayout getTitleP() {
		return titleP;
	}
	/**
	 * descPを取得します。
	 *
	 * @return tagLabel
	 */
	public LinearLayout getDescP() {
		return descP;
	}
	/**
	 * commuPを取得します。
	 *
	 * @return tagLabel
	 */
	public LinearLayout getCommuP() {
		return commuP;
	}
	//ラベル取得----------------------
	/**
	 * openを取得します
	 * @return open
	 */
//	public TextView getOpen(){
//		return open;
//	}
	/**
	 * startを取得します
	 * @return start
	 */
	public TextView getStart(){
		return start;
	}
	/**
	 * passedtimeを取得します
	 * @return passedtime
	 */
	public TextView getPassedTime(){
		return passedtime;
	}
	/**
	 * seet_numを取得します
	 * @return seet_num
	 */
	public TextView getSeetText(){
		return seet_num;
	}
	/**
	 * commuNameを取得します
	 * @return commuName
	 */
	public TextView getCommuName(){
		return commuName;
	}
	/**
	 * ownerNameを取得します
	 * @return commuName
	 */
	public TextView getOwnerName(){
		return ownerName;
	}
	/**
	 * tagLabelを取得します。
	 *
	 * @return tagLabel
	 */
	public TextView getTagLabel() {
		return tagLabel;
	}
	/**
	 * titleLabelを取得します。
	 *
	 * @return titleLabel
	 */
	public TextView getTitleLabel() {
		return titleLabel;
	}
	/**
	 * descLabelを取得します。
	 *
	 * @return tagLabel
	 */
	public TextView getDescLabel() {
		return descLabel;
	}
	/**
	 * commuLabelを取得します。
	 *
	 * @return commuLabel
	 */
	public TextView getCommuLabel() {
		return commuLabel;
	}
	public TextView getViewCountView() {
		return viewCount;
	}
	public TextView getResNumView() {
		return resNum;
	}
	//リンクボタンのゲッター
	public Button getTagLnkB(){
		return tagLinkBt;
	}
	public Button getCommunityLinkB(){
		return communityBt;
	}
	public Button getBrowserLinkB(){
		return browserBt;
	}
	public Button snsLnkB(){
		return snsBt;
	}
	public Button getCopyB(){
		return copyBt;
	}
	/**
	 * reserveBtを取得します。
	 * @return reserveBt
	 */
	public Button getReserveBt() {
	    return reserveBt;
	}

	public Button getGoPlayerB(){
		return goPlayerB;
	}
	/**
	 * widthを取得します。
	 *
	 * @return width
	 */
	public int getWidth() {
		return width;
	}
	/**
	 * heightを取得します。
	 *
	 * @return width
	 */
	public int getHeight() {
		return height;
	}
	/**
	 * closeを取得します。
	 *
	 * @return close
	 */
	public FrameLayout getCloseView() {
		return close;
	}
	/**
	 * progressParentを取得します
	 * @return
	 * @return progressParent
	 */
	public View getPParent(){
		return progressParent;
	}
	/**
	 * progressAreaを取得します
	 * @return
	 * @return progressArea
	 */
	public ViewGroup getPArea(){
		return progressArea;
	}

	/**
	 * lParentを取得します。
	 * @return lParent
	 */
	public LinearLayout getlParent() {
	    return lParent;
	}

}
