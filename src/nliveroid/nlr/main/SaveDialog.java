package nliveroid.nlr.main;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Environment;
import android.text.SpannableStringBuilder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SaveDialog extends AlertDialog.Builder{

	private Context context;
	private LiveInfo liveinfo;
	private CommentListAdapter adapter;
	public static boolean isXML;
	private String fileName;
	private boolean isOverWrite = true;
	private AlertDialog me;
	public SaveDialog(Context arg0,final LiveInfo liveinfo,CommentListAdapter adapter) {
		super(arg0);
		this.context = arg0;
		this.adapter = adapter;
		this.liveinfo = liveinfo;
		this.setTitle("Save comments");
		LayoutInflater inflater = LayoutInflater.from(arg0);
		View parent = inflater.inflate(R.layout.savedialog, null);
		setView(parent);

		final EditText et = (EditText)parent.findViewById(R.id.save_edittex);
		et.setText(liveinfo.getLiveID()+".txt");
		//InputFilterは未確定の文字に対する処理なので普通にエスケープ
		//エスケープする文字 : [] / * ? " | \

		CheckBox date = (CheckBox)parent.findViewById(R.id.save_date);
		date.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				Date date = new Date();//端末の時間
				SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
				String dateStr = sdf.format(date) + "_";
				if(arg1){
					et.setText(dateStr + et.getText());
				}else{
					Matcher mc = Pattern.compile(dateStr).matcher(et.getText());
					if(mc.find()){
						et.setText(et.getText().toString().replaceAll(mc.group(), ""));
					}
				}
				fileName = et.getText().toString();
			}
		});
		CheckBox co = (CheckBox)parent.findViewById(R.id.save_communityid);
		co.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				String co = liveinfo.getCommunityID()==null? "official": liveinfo.getCommunityID()+"_";
				if(arg1){
					et.setText(co + et.getText());
				}else{
					Matcher mc = Pattern.compile(co).matcher(et.getText());
					if(mc.find()){
						et.setText(et.getText().toString().replaceAll(mc.group(), ""));
					}
				}
				fileName = et.getText().toString();
			}
		});
		CheckBox title = (CheckBox)parent.findViewById(R.id.save_title);
		title.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				String titleStr = liveinfo.getTitle() +"_";
				if(arg1){
					et.setText(titleStr + et.getText());
				}else{
					Matcher mc = Pattern.compile(titleStr).matcher(et.getText());
					if(mc.find()){
						et.setText(et.getText().toString().replaceAll(mc.group(), ""));
					}
				}
				fileName = et.getText().toString();
			}
		});
		CheckBox owner = (CheckBox)parent.findViewById(R.id.save_ownername);
		owner.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				String ownerStr = liveinfo.getOwnerName()==null? URLEnum.HYPHEN:liveinfo.getOwnerName() +"_";
				if(arg1){
					et.setText(ownerStr + et.getText());
				}else{
					Matcher mc = Pattern.compile(ownerStr).matcher(et.getText());
					if(mc.find()){
						et.setText(et.getText().toString().replaceAll(mc.group(), ""));
					}
				}
				fileName = et.getText().toString();
			}
		});

		Button okB = (Button) parent.findViewById(R.id.save_ok);
		okB.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View arg0) {
				String str = ((SpannableStringBuilder)et.getText()).toString();
				Matcher mc = Pattern.compile(":|/|\\[|\\]|\\?|\\*|\\\\|\\|").matcher(str);// |と\がエスケープされてない
				if(mc.find()){
					MyToast.customToastShow(context,"ファイル名に : | [] \\ \\? \\* / は含めません");
				}else if(str.equals("")||str.equals(".xml")||str.equals(".txt")){
					MyToast.customToastShow(context,"ファイル名を入力して下さい");
				}else{
					fileName = str;
				//.xmlか.txtじゃなかったら全て.txt
					Matcher mc1 = Pattern.compile(".xml$").matcher(str);
					Matcher mc2 = Pattern.compile(".txt$").matcher(str);
				if(mc1.find()){
					isXML = true;
				}else if(mc2.find()){

				}else{
					fileName = str+".txt";
				}

				if(isXML){
					writeToXML();
				}else{
					writeToTEXT();
				}
				me.cancel();
				}
			}
		});
		Button cancelB = (Button) parent.findViewById(R.id.save_cancel);
		cancelB.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View arg0) {
				me.cancel();
			}
		});
	}

	private void writeToTEXT(){
		String filepath = getStorageFilePath();
		if(filepath == null)return;

		Date date = new Date();//端末の時間
		SimpleDateFormat today = new SimpleDateFormat("yyyy/MM/dd");
		String dateStr = today.format(date);
		String source = "\n" + dateStr + "\nLv" + liveinfo.getLiveID()+"\n";
		//時間処理
		DateFormat start_open = new SimpleDateFormat("HH:mm",Locale.JAPAN);
		// APIでbase_timeがなくて、start_timeがある場合がある
		if (liveinfo.getBaseTime() != null){
			source += "OPEN TIME " + start_open.format(new Date(Long.parseLong(liveinfo.getBaseTime())*1000)) + "\n";
		}
		if (liveinfo.getStartTime() != null) {
			source += "START TIME " + start_open.format(new Date(Long.parseLong(liveinfo.getStartTime())*1000)) + "\n";
		}

			String roomLabel = liveinfo.getRoomlabel();
		if (roomLabel != null && roomLabel.contains("co")) {
			roomLabel = "アリーナ";
		} else if (roomLabel == null) {
			roomLabel = URLEnum.HYPHEN;
		}
		String seetLabel = liveinfo.getRoomno();
		if (seetLabel == null) {//座席が取れなかった
			seetLabel = URLEnum.HYPHEN;

		}
		source += "SEET INFO"+ roomLabel + " " + seetLabel +"\n";
		source += "COMMUNITY"+ liveinfo.getDefaultCommunity() +"\n";
		source += "OWNER NAME " +liveinfo.getOwnerName() +"\n\n";
		source += "TITLE "+liveinfo.getTitle() +"\n";


		String[] temp = new String[6];
		for(int i = 0; i < adapter.getCount(); i++) {
			temp = adapter.getItem(i);
			source += temp[0] + " " + temp[1] + " " + temp[2] + " " + temp[3] + " " + temp[4] + " " + temp[5] + "\n";
		}
		try {
			FileOutputStream fos = new FileOutputStream(filepath);
			fos.write(source.getBytes());
			fos.close();
		} catch (IOException e) {
			// TODO
			e.printStackTrace();
		}

	}


	private void writeToXML(){

		String filepath = getStorageFilePath();
		if(filepath == null)return;

		try{
		Date date = new Date();//端末の時間
		SimpleDateFormat today = new SimpleDateFormat("yyyyMMdd");
		String dateStr = today.format(date);
		long startTime = 0;//ループのとこで時間計算使う
		if(liveinfo.getStartTime() != null){
		startTime = Long.parseLong(liveinfo.getStartTime());
		}

			String roomLabel = liveinfo.getRoomlabel();
		if (roomLabel != null && roomLabel.contains("co")) {
			roomLabel = "アリーナ";
		} else if (roomLabel == null) {
			roomLabel = URLEnum.HYPHEN;
		}
		String seetLabel = liveinfo.getRoomno();
		if (seetLabel == null) {//座席が取れなかった
			seetLabel = URLEnum.HYPHEN;
		}
		String defaultCommunity = liveinfo.getDefaultCommunity()==null? "official":liveinfo.getDefaultCommunity();
		String ownerName = liveinfo.getOwnerName()==null? "official":liveinfo.getOwnerName();
		String source = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
		"<NLiveRoid_Log xmnls=\"http://nliveroid-tutorial.appspot.com/log/\">\n" +
		"<date>" + dateStr + "</date>\n"+
		"<lv>" + liveinfo.getLiveID()+"</lv>\n" +
				"<open_time>"+liveinfo.getBaseTime()+"</open_time>\n" +
						"<start_time>"+startTime+"</start_time>\n" +
								"<default_community>" +defaultCommunity+"</default_community>\n"+
						"<owner_name>" + ownerName +"</owner_name>\n" +
								"<title>"+liveinfo.getTitle()+"</title>\n" +
										"<description>"+liveinfo.getDescription()+"</description>\n" +
												"<seet>"+roomLabel + " " + seetLabel+"</seet>\n";

		//TYPE ID CMD TIME NUM COMMENT
		String[] temp = new String[6];
		String anonymity = "";
		long time = 0;
		HashMap<String,String> typeMap = new HashMap<String,String>();
			typeMap.put("P","1");
			typeMap.put("SYS","2");
			typeMap.put("主","3");
			typeMap.put("OFFICIAL1","6");
			typeMap.put("OFFICIAL2","7");
			typeMap.put("","");
			typeMap.put("NORMAL_MALE","8");
			typeMap.put("PREMIUM_MALE","9");
			typeMap.put("NORMAL_FEMALE","24");
			typeMap.put("PREMIUM_FEMALE","25");

		for(int i = 0; i < adapter.getCount(); i++){
			temp = adapter.getItem(i);
				anonymity = temp[2].contains("184")? "1":"0";
			time = startTime+(Integer.parseInt(temp[3].split(":")[0])*60)+Integer.parseInt(temp[3].split(":")[1]);
			source += "<chat anonymity=\"" + anonymity + "\" date=\"" + time + "\" mail=\"" + temp[2] + "\" no=\"" + temp[5] + "\" premium=\"" + typeMap.get(temp[0]) + "\" user_id=\"" +temp[1] + "\" > " + temp[6] + "</chat>\n";
		}
		source += "</NLiveRoid_Log>\n";

			FileOutputStream fos = new FileOutputStream(filepath);
			fos.write(source.getBytes());
			fos.close();
		}catch(NumberFormatException e){
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	/**
	 * ストレージのパスを取得します	 *
	 *
	 */

	private String getStorageFilePath(){
		boolean isStorageAvalable = false;
		boolean isStorageWriteable = false;
		String state = Environment.getExternalStorageState();
		if(state == null){
			MyToast.customToastShow(context, "SDカードが利用できませんでした\nログ保存は機能できません");
			return null;
		}else if (Environment.MEDIA_MOUNTED.equals(state)) {
		    //読み書きOK
		    isStorageAvalable = isStorageWriteable = true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
		    //読み込みだけOK
		    isStorageAvalable = true;
		    isStorageWriteable = false;
		} else {
			//ストレージが有効でない
		    isStorageAvalable = isStorageWriteable = false;
		}

		boolean notAvalable = !isStorageAvalable;
		boolean notWritable = !isStorageWriteable;
		if(notAvalable||notWritable){
			MyToast.customToastShow(context, "SDカードが利用できませんでした\nログ保存は機能できません");
			return null;
		}


		//sdcard直下に、パッケージ名のフォルダを作りファイルを生成
		String filePath = Environment.getExternalStorageDirectory().toString();
		if(filePath == null){
			MyToast.customToastShow(context, "SDカードが利用できませんでした\nログ保存は機能できません");
			return null;
		}
		filePath = filePath +  "/NLiveRoid/Log";

		File directory = new File(filePath);

		if(directory.mkdirs()){//ディレクトリを生成する すでにあった場合失敗する
			//mkdirsがやりたいので処理無し
		}


		File file = new File(filePath,fileName);
		if(!file.exists()){
			try {
				file.createNewFile();
			} catch (IOException e) {
				//errorはないのでしかと
			}
		}else if(isOverWrite){
			new OverWriteDialog().show();
			return null;
		}
		return file.getPath();
	}


	class OverWriteDialog extends Builder{
		private Activity parentACT;
		protected OverWriteDialog() {
			super(context);
			parentACT = (Activity) context;
			LinearLayout myDialogLayout = new LinearLayout(context);
				myDialogLayout.setBackgroundColor(Color.WHITE);
				TextView text = new TextView(context);
				text.setTextSize(25);
				text.setTextColor(Color.rgb(153,255,69));
				text.setText("上書きしますか?");
				text.setGravity(Gravity.CENTER);
				this.setView(myDialogLayout)
			       .setCancelable(true)
			       .setPositiveButton("OK", new YesListener())
			       .setNegativeButton("CANCEL", new NoListener())
			       .create();

			myDialogLayout.addView(text,new LinearLayout.LayoutParams(-1, -2));

		}

			class NoListener implements DialogInterface.OnClickListener{
				@Override
				public void onClick(DialogInterface dialog, int which) {
					  dialog.cancel();
		        	 	}
			}

			class YesListener implements DialogInterface.OnClickListener{

				@Override
				public void onClick(DialogInterface dialog, int which) {
					isOverWrite = false;
					if(isXML){
						writeToXML();
					}else{
						writeToTEXT();
					}
				dialog.cancel();
				}
			}
	}

	public void showSelf(){
		this.create();
		me = this.show();
	}


}
