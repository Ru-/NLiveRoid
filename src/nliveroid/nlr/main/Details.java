package nliveroid.nlr.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class Details extends PreferenceActivity implements
Preference.OnPreferenceChangeListener {

	private static Details details;
	protected static final String tabledispFileName = "tabledisplay";
	private HashMap<String,String> detailsMap;

	//フィールドにするかの可否は、サマリー更新とonlyCommentReflectで使用するかとしておく
	private CheckBoxPreference fixvolenable;
	private TableWidthDialog type_p;
	private TableWidthDialog id_p;
	private TableWidthDialog command_p;
	private TableWidthDialog time_p;
	private TableWidthDialog score_p;
	private TableWidthDialog num_p;
	private TableWidthDialog comment_p;
	private ListPreference player_pos_p;

	private TableWidthDialog type_l;
	private TableWidthDialog id_l;
	private TableWidthDialog command_l;
	private TableWidthDialog time_l;
	private TableWidthDialog score_l;
	private TableWidthDialog num_l;
	private TableWidthDialog comment_l;
	private ListPreference player_pos_l;
	private ListPreference player_select;
	private CheckBoxPreference manner_0;

	private CheckBoxPreference speech_enable;
	private ListPreference speech_engine;
//	private CheckBoxPreference speech_education_enable;
	private EditTextPreference speech_skip_word;
	private PreferenceScreen education_screen;
	private ListPreference layer_num;
	private CheckBoxPreference fexit;
	private CheckBoxPreference finishback;


	private ProgressDialog dialog;
	private SettingFileDialog delete_dic;
	private ListPreference player_quality;

	private static ViewGroup parent;


	@Override
	public void onCreate(Bundle paramBundle) {
		super.onCreate(paramBundle);
		details = this;
		 LayoutInflater inflater = LayoutInflater.from(this);
		 parent = (ViewGroup) inflater.inflate(R.layout.config, null);
			setContentView(parent);
		addPreferencesFromResource(R.xml.details);

		//縦横共通
		player_select = (ListPreference)findPreference("player_select");
		player_select.setOnPreferenceChangeListener(this);
		manner_0 = (CheckBoxPreference)findPreference("manner_0");
		manner_0.setOnPreferenceChangeListener(this);

		layer_num = (ListPreference)findPreference("layer_num");
		layer_num.setOnPreferenceChangeListener(this);
		player_quality = (ListPreference)findPreference("player_quality");
		player_quality.setOnPreferenceChangeListener(this);

		CheckBoxPreference voice_input = (CheckBoxPreference)findPreference("voice_input");
		voice_input.setOnPreferenceChangeListener(this);

		fixvolenable = (CheckBoxPreference) findPreference("fix_volenable");
		fixvolenable.setOnPreferenceChangeListener(this);

		ListPreference allco_operate  = (ListPreference)findPreference("allco_operate");
		allco_operate.setOnPreferenceChangeListener(this);
		ListPreference fixscreen = (ListPreference)findPreference("fix_screen");
		fixscreen.setOnPreferenceChangeListener(this);
//		ListPreference alpha = (ListPreference)findPreference("alpha");
//		alpha.setOnPreferenceChangeListener(this);
		ListPreference back_t = (ListPreference)findPreference("select_back_img_t");
		back_t.setOnPreferenceChangeListener(this);
		ListPreference tcolor = (ListPreference)findPreference("toptab_tcolor");
		tcolor.setOnPreferenceChangeListener(this);
		ListPreference back_v = (ListPreference)findPreference("select_back_img_v");
		back_v.setOnPreferenceChangeListener(this);
		CheckBoxPreference newline = (CheckBoxPreference)findPreference("newline");
		newline.setOnPreferenceChangeListener(this);
		CheckBoxPreference auto_username = (CheckBoxPreference)findPreference("auto_username");
		auto_username.setOnPreferenceChangeListener(this);
		CheckBoxPreference form_up = (CheckBoxPreference)findPreference("form_up");
		form_up.setOnPreferenceChangeListener(this);
		CheckBoxPreference form_backkey = (CheckBoxPreference)findPreference("form_backkey");
		form_backkey.setOnPreferenceChangeListener(this);
		CheckBoxPreference is_fullscreen = (CheckBoxPreference)findPreference("discard_notification");
		is_fullscreen.setOnPreferenceChangeListener(this);
		CheckBoxPreference return_tab = (CheckBoxPreference)findPreference("return_tab");
		return_tab.setOnPreferenceChangeListener(this);
		CheckBoxPreference update_tab = (CheckBoxPreference)findPreference("update_tab");
		update_tab.setOnPreferenceChangeListener(this);
		CheckBoxPreference recent_ts = (CheckBoxPreference)findPreference("recent_ts");
		recent_ts.setOnPreferenceChangeListener(this);
		CheckBoxPreference delay_start = (CheckBoxPreference)findPreference("delay_start");
		delay_start.setOnPreferenceChangeListener(this);
		CheckBoxPreference back_black = (CheckBoxPreference)findPreference("back_black");
		back_black.setOnPreferenceChangeListener(this);
		speech_enable = (CheckBoxPreference)findPreference("speech_enable");
		speech_enable.setOnPreferenceChangeListener(this);
		speech_engine = (ListPreference)findPreference("speech_engine");
		speech_engine.setOnPreferenceChangeListener(this);
		speech_engine.setEntries(R.array.speech_engine);
		speech_engine.setEntryValues(R.array.two_values);
//		speech_education_enable = (CheckBoxPreference)findPreference("speech_education_enable");
//		speech_education_enable.setOnPreferenceChangeListener(this);
		//スピード、ピッチ、教育はカスタムプリフ
		speech_skip_word = (EditTextPreference)findPreference("speech_skip_word");
		speech_skip_word.setOnPreferenceChangeListener(this);
		education_screen = (PreferenceScreen)findPreference("education_screen");
		//辞書ファイル削除
		delete_dic = (SettingFileDialog)findPreference("setting_dic_delete");
		//すでに展開済み？
		String filepath = getFilesDir().getAbsolutePath() + "/" +  "copyed.dat";
		File file = new File(filepath);
		boolean isExists = file.exists();
		if(isExists){
			delete_dic.setEnabled(true);
		}else{
			delete_dic.setEnabled(false);
		}

		fexit = (CheckBoxPreference)findPreference("fexit");
		fexit.setOnPreferenceChangeListener(this);
		finishback = (CheckBoxPreference)findPreference("finish_back");
		finishback.setOnPreferenceChangeListener(this);

		//テーブルの幅 int配列で表示列が変わってもそのまま適応
		type_p = (TableWidthDialog) findPreference("type_width_p");
		type_p.setOnPreferenceChangeListener(this);
		id_p = (TableWidthDialog) findPreference("id_width_p");
		id_p.setOnPreferenceChangeListener(this);
		command_p = (TableWidthDialog) findPreference("command_width_p");
		command_p.setOnPreferenceChangeListener(this);
		time_p = (TableWidthDialog) findPreference("time_width_p");
		time_p.setOnPreferenceChangeListener(this);
		score_p = (TableWidthDialog) findPreference("score_width_p");
		score_p.setOnPreferenceChangeListener(this);
		num_p = (TableWidthDialog) findPreference("num_width_p");
		num_p.setOnPreferenceChangeListener(this);
		comment_p = (TableWidthDialog) findPreference("comment_width_p");
		comment_p.setOnPreferenceChangeListener(this);

		CheckBoxPreference xd_enable_p = (CheckBoxPreference)findPreference("xd_enable_p");
		xd_enable_p.setOnPreferenceChangeListener(this);
		CheckBoxPreference yd_enable_p = (CheckBoxPreference)findPreference("yd_enable_p");
		yd_enable_p.setOnPreferenceChangeListener(this);
		TableHeightDialog cellheight_p = (TableHeightDialog)findPreference("cellheight_p");
		cellheight_p.setOnPreferenceChangeListener(this);
		player_pos_p = (ListPreference)findPreference("player_pos_p");
		player_pos_p.setOnPreferenceChangeListener(this);




		type_l = (TableWidthDialog) findPreference("type_width_l");
		type_l.setOnPreferenceChangeListener(this);
		id_l = (TableWidthDialog) findPreference("id_width_l");
		id_l.setOnPreferenceChangeListener(this);
		command_l = (TableWidthDialog) findPreference("command_width_l");
		command_l.setOnPreferenceChangeListener(this);
		time_l = (TableWidthDialog) findPreference("time_width_l");
		time_l.setOnPreferenceChangeListener(this);
		score_l = (TableWidthDialog) findPreference("score_width_l");
		score_l.setOnPreferenceChangeListener(this);
		num_l = (TableWidthDialog) findPreference("num_width_l");
		num_l.setOnPreferenceChangeListener(this);
		comment_l = (TableWidthDialog) findPreference("comment_width_l");
		comment_l.setOnPreferenceChangeListener(this);
		CheckBoxPreference xd_enable_l = (CheckBoxPreference)findPreference("xd_enable_l");
		xd_enable_l.setOnPreferenceChangeListener(this);
		CheckBoxPreference yd_enable_l = (CheckBoxPreference)findPreference("yd_enable_l");
		yd_enable_l.setOnPreferenceChangeListener(this);
		TableHeightDialog cellheight_l = (TableHeightDialog)findPreference("cellheight_l");
		cellheight_l.setOnPreferenceChangeListener(this);
		player_pos_l = (ListPreference)findPreference("player_pos_l");
		player_pos_l.setOnPreferenceChangeListener(this);

		loadSettings(((NLiveRoid)getApplicationContext()).getDetailsMap());
	}

	/**
	 * デフォルト値と有効無効を設定する
	 * @param map
	 */
	public void loadSettings(HashMap<String,String> map){
		detailsMap = map;
		if (detailsMap != null) {
			//縦横共通
			ListPreference fixscreen = (ListPreference)findPreference("fix_screen");
			fixscreen.setEntries(R.array.fix_screen_entrys);
			fixscreen.setEntryValues(R.array.three_values);
			int defaultValue = 0;
			if (detailsMap.get("fix_screen") != null) {
				try{
					defaultValue = Integer.parseInt(detailsMap.get("fix_screen"));
				}catch(NumberFormatException e){
					e.printStackTrace();
				}
			}
			fixscreen.setValueIndex(defaultValue);
//			ListPreference alpha = (ListPreference)findPreference("alpha");
//			alpha.setEntries(R.array.alpha_entrys);
//			alpha.setEntryValues(R.array.for_values);
//			defaultValue = 0;
//			if (detailsMap.get("alpha") != null) {
//				try{
//					defaultValue = Integer.parseInt(detailsMap.get("alpha"));
//				}catch(NumberFormatException e){
//					e.printStackTrace();
//				}
//			}
//			alpha.setValueIndex(defaultValue);
			ListPreference allco_operate = (ListPreference)findPreference("allco_operate");
			allco_operate.setEntries(R.array.allco_operate_entrys);
			allco_operate.setEntryValues(R.array.seven_values);
			defaultValue = 0;
			if (detailsMap.get("allco_operate") != null) {
				try{
					defaultValue = Integer.parseInt(detailsMap.get("allco_operate"));
				}catch(NumberFormatException e){
					e.printStackTrace();
				}
			}
			allco_operate.setValueIndex(defaultValue);
			//背景画像
			ListPreference back_t = (ListPreference)findPreference("select_back_img_t");
			back_t.setEntries(R.array.select_back_img);
			back_t.setEntryValues(R.array.three_values);
				try{
					FileInputStream back_t_file  = details.openFileInput("back_t");
				if(back_t_file == null)throw new FileNotFoundException();
				back_t.setValueIndex(1);
				} catch (FileNotFoundException e) {
					back_t.setValueIndex(0);
					e.printStackTrace();
				}catch(Exception e){
					e.printStackTrace();
				}
				ListPreference tcolor = (ListPreference)findPreference("toptab_tcolor");
				tcolor.setEntries(R.array.select_tcolor);
				tcolor.setEntryValues(R.array.three_values);
				if (detailsMap.get("toptab_tcolor") != null) {
					try{
						defaultValue = Integer.parseInt(detailsMap.get("toptab_tcolor"));
					}catch(NumberFormatException e){
						defaultValue = 0;
						e.printStackTrace();
					}
					tcolor.setValueIndex(defaultValue);
				}
			ListPreference back_v = (ListPreference)findPreference("select_back_img_v");
			back_v.setEntries(R.array.select_back_img);
			back_v.setEntryValues(R.array.two_values);
				try{
					FileInputStream back_v_file  = details.openFileInput("back_t");
				if(back_v_file == null)throw new FileNotFoundException();
				back_v.setValueIndex(1);
				} catch (FileNotFoundException e) {
					back_t.setValueIndex(0);
					e.printStackTrace();
				}catch(Exception e){
					e.printStackTrace();
				}
				//フルスクリーン
				if(detailsMap.get("discard_notification")!= null){
				CheckBoxPreference is_fullscreen = (CheckBoxPreference)findPreference("discard_notification");
				boolean val = false;
				try{
					val = Boolean.parseBoolean(detailsMap.get("discard_notification"));
				}catch(Exception e){
					e.printStackTrace();
				}
				is_fullscreen.setChecked(val);
				}

			if (detailsMap.get("layer_num") != null&&layer_num != null) {
				layer_num.setEntries(R.array.layer_num);
				layer_num.setEntryValues(R.array.for_values);
				byte defaultvalue = 0;
				try{
					defaultvalue = Byte.parseByte(detailsMap.get("layer_num"));
					layer_num.setValueIndex(defaultvalue);
				}catch(NumberFormatException e){
					e.printStackTrace();
					Log.d("NLiveRoid","Error read Pref layer_num" + defaultvalue);
					layer_num.setValueIndex(0);
				}catch(IndexOutOfBoundsException e){
					e.printStackTrace();
					Log.d("NLiveRoid","Error read Pref layer_num" + defaultvalue);
					layer_num.setValueIndex(0);
				}
			}
			//誤操作防止ダイアログはコメントのみかに依存しない
			if(detailsMap.get("fexit") != null){
				boolean isFexit = Boolean.parseBoolean(detailsMap.get("fexit"));
				fexit.setChecked(isFexit);
			}
			if (detailsMap.get("finish_back") != null) {
				boolean finish_back = Boolean.parseBoolean(detailsMap.get("finish_back"));
				finishback.setChecked(finish_back);
			}
			try {
				getPackageManager().getApplicationInfo("com.adobe.flashplayer",
						0);
			} catch (PackageManager.NameNotFoundException localNameNotFoundException) {
				// フラッシュがインストールされてない
				layer_num.setValueIndex(3);
				if(detailsMap.get("only_comment")!=null){//nullではないはずではある
					detailsMap.put("layer_num","3");
				}
			}
			if (detailsMap.get("player_quality") != null) {
					player_quality.setEntries(R.array.player_quality_entrys);
					player_quality.setEntryValues(R.array.three_values);
					int defaultFix = 0;
					try{
						defaultFix = Integer.parseInt(detailsMap.get("player_quality"));
					}catch(NumberFormatException e){
						e.printStackTrace();
					}
					player_quality.setValueIndex(defaultFix);
			}
			if (detailsMap.get("voice_input") != null) {
				CheckBoxPreference voice_input = (CheckBoxPreference)findPreference("voice_input");
				voice_input.setChecked(Boolean.parseBoolean(detailsMap.get("voice_input")));
			}
			if (detailsMap.get("form_up") != null) {
				CheckBoxPreference form_up = (CheckBoxPreference)findPreference("form_up");
				form_up.setChecked(Boolean.parseBoolean(detailsMap.get("form_up")));
			}
			if (detailsMap.get("form_backkey") != null) {
				CheckBoxPreference form_backkey = (CheckBoxPreference)findPreference("form_backkey");
				form_backkey.setChecked(Boolean.parseBoolean(detailsMap.get("form_backkey")));
			}
			if (detailsMap.get("newline") != null) {
				CheckBoxPreference newline = (CheckBoxPreference)findPreference("newline");
				newline.setChecked(Boolean.parseBoolean(detailsMap.get("newline")));
			}
			if (detailsMap.get("auto_username") != null) {
				CheckBoxPreference auto_username = (CheckBoxPreference)findPreference("auto_username");
				auto_username.setChecked(Boolean.parseBoolean(detailsMap.get("auto_username")));
			}
			if (detailsMap.get("player_select") != null) {
				player_select.setEntries(R.array.player_select);//HLSの場合、ここの先のarrayを変える
				player_select.setEntryValues(R.array.two_values);//HLSの場合、ここを変える
				byte defaultvalue = 0;
				try{
					defaultvalue = Byte.parseByte(detailsMap.get("player_select"));
					player_select.setValueIndex(defaultvalue);
				}catch(NumberFormatException e){
					e.printStackTrace();
					Log.d("NLiveRoid","Error read Pref player_select" + defaultvalue);
					player_select.setValueIndex(0);
				}catch(IndexOutOfBoundsException e){
					e.printStackTrace();
					Log.d("NLiveRoid","Error read Pref player_select" + defaultvalue);
					player_select.setValueIndex(0);
				}
			}
			if (detailsMap.get("manner_0") != null) {
				manner_0.setChecked(Boolean.parseBoolean(detailsMap.get("manner_0")));
			}
			if (detailsMap.get("fix_volenable") != null) {
				boolean isFixVol = Boolean.parseBoolean(detailsMap.get("fix_volenable"));
				fixvolenable.setChecked(isFixVol);
				SeekBarPreference.setSeekEnable(isFixVol);
			}
			if (detailsMap.get("return_tab") != null) {
				CheckBoxPreference return_tab = (CheckBoxPreference)findPreference("return_tab");
				return_tab.setChecked(Boolean.parseBoolean(detailsMap.get("return_tab")));
			}
			if (detailsMap.get("update_tab") != null) {
				CheckBoxPreference update_tab = (CheckBoxPreference)findPreference("update_tab");
				update_tab.setChecked(Boolean.parseBoolean(detailsMap.get("update_tab")));
			}
			if (detailsMap.get("recent_ts") != null) {
				CheckBoxPreference recent_ts = (CheckBoxPreference)findPreference("recent_ts");
				recent_ts.setChecked(Boolean.parseBoolean(detailsMap.get("recent_ts")));
			}
			if (detailsMap.get("delay_start") != null) {
				CheckBoxPreference delay_start = (CheckBoxPreference)findPreference("delay_start");
				delay_start.setChecked(Boolean.parseBoolean(detailsMap.get("delay_start")));
			}
			if (detailsMap.get("back_black") != null) {
				CheckBoxPreference back_black = (CheckBoxPreference)findPreference("back_black");
				back_black.setChecked(Boolean.parseBoolean(detailsMap.get("back_black")));
			}
			if (detailsMap.get("speech_enable") != null) {
				int speecheable = Integer.parseInt(detailsMap.get("speech_enable"));
				if(speecheable == 1){
					onSpeechReflect(true);
					speech_engine.setValueIndex(0);
					speech_engine.setSummary("標準エンジン");
					speech_enable.setChecked(true);
					SpeechParamSeekBar.setEnable_(true,false,true);
					SpeechTestPreference.setEnable_(true,false);
				}else if(speecheable == 3){
					onSpeechReflect(true);
				speech_engine.setValueIndex(1);
				speech_engine.setSummary("AquesTalk");
				speech_enable.setChecked(true);
				SpeechParamSeekBar.setEnable_(true,true,false);
				SpeechTestPreference.setEnable_(true,true);
				}else{
					speech_enable.setChecked(false);
					SpeechParamSeekBar.setEnable_(false,false,false);
					SpeechTestPreference.setEnable_(false,false);
					onSpeechReflect(false);
				}
			}else{
				onSpeechReflect(false);
			}
			if(detailsMap.get("speech_skip_word") != null){
				speech_skip_word.setText(detailsMap.get("speech_skip_word"));
			}

			//縦----------------------------------
			if (detailsMap.get("type_width_p") != null) {
				type_p.setSummary(detailsMap.get("type_width_p"));
			}
			if(detailsMap.get("id_width_p") != null){
				id_p.setSummary(detailsMap.get("id_width_p"));
			}
			if(detailsMap.get("command_width_p") != null){
				command_p.setSummary(detailsMap.get("command_width_p"));
			}
			if(detailsMap.get("time_width_p") != null){
				time_p.setSummary(detailsMap.get("time_width_p"));
			}
			if(detailsMap.get("score_width_p") != null){
				score_p.setSummary(detailsMap.get("score_width_p"));
			}
			if(detailsMap.get("num_width_p") != null){
				num_p.setSummary(detailsMap.get("num_width_p"));
			}
			if(detailsMap.get("comment_width_p") != null){
				comment_p.setSummary(detailsMap.get("comment_width_p"));
			}
			if (detailsMap.get("xd_enable_p") != null) {
				CheckBoxPreference xd_enable_p = (CheckBoxPreference)findPreference("xd_enable_p");
				xd_enable_p.setChecked(Boolean.parseBoolean(detailsMap.get("xd_enable_p")));
			}
			if (detailsMap.get("yd_enable_p") != null) {
				CheckBoxPreference yd_enable_p = (CheckBoxPreference)findPreference("yd_enable_p");
				yd_enable_p.setChecked(Boolean.parseBoolean(detailsMap.get("yd_enable_p")));
			}
			if (detailsMap.get("player_pos_p") != null) {
				player_pos_p.setEntries(R.array.player_pos_updown);
				player_pos_p.setEntryValues(R.array.player_posval_rightleftupdown);
				int defaultPos = 0;
				try{
					defaultPos = Integer.parseInt(detailsMap.get("player_pos_p"));
				}catch(NumberFormatException e){
					e.printStackTrace();
				}
				try{
				player_pos_p.setValueIndex(defaultPos);
				}catch(ArrayIndexOutOfBoundsException e){
				player_pos_p.setValueIndex(0);
				}
			}
			if (detailsMap.get("player_pos_l") != null) {
				if(player_select.getValue() != null && !player_select.getValue().equals("1")){//原宿以外全画面有り
					player_pos_l.setEntries(R.array.player_pos_rightleftall);
					player_pos_l.setEntryValues(R.array.player_posval_rightleftall);
				}else{
				player_pos_l.setEntries(R.array.player_pos_rightleft);
				player_pos_l.setEntryValues(R.array.player_posval_rightleftupdown);
				}
				int defaultPos = 0;
				try{
					defaultPos = Integer.parseInt(detailsMap.get("player_pos_l"));
				}catch(NumberFormatException e){
					e.printStackTrace();
				}
				try{
				player_pos_l.setValueIndex(defaultPos);
				}catch(ArrayIndexOutOfBoundsException e){
					player_pos_l.setValueIndex(0);
				}
			}
			//横---------------------------------
			if (detailsMap.get("type_width_l") != null) {
				type_l.setSummary(detailsMap.get("type_width_l"));
			}
			if(detailsMap.get("id_width_l") != null){
				id_l.setSummary(detailsMap.get("id_width_l"));
			}
			if(detailsMap.get("command_width_l") != null){
				command_l.setSummary(detailsMap.get("command_width_l"));
			}
			if(detailsMap.get("time_width_l") != null){
				time_l.setSummary(detailsMap.get("time_width_l"));
			}
			if(detailsMap.get("score_width_l") != null){
				score_l.setSummary(detailsMap.get("score_width_l"));
			}
			if(detailsMap.get("num_width_l") != null){
				num_l.setSummary(detailsMap.get("num_width_l"));
			}
			if(detailsMap.get("comment_width_l") != null){
				comment_l.setSummary(detailsMap.get("comment_width_l"));
			}
			if (detailsMap.get("xd_enable_l") != null) {
				CheckBoxPreference xd_enable_l = (CheckBoxPreference)findPreference("xd_enable_l");
				xd_enable_l.setChecked(Boolean.parseBoolean(detailsMap.get("xd_enable_l")));
			}
			if (detailsMap.get("yd_enable_l") != null) {
				CheckBoxPreference yd_enable_l = (CheckBoxPreference)findPreference("yd_enable_l");
				yd_enable_l.setChecked(Boolean.parseBoolean(detailsMap.get("yd_enable_l")));
			}

			onlyCommentReflect();
		}
	}


	@Override
	public void onPause(){
		//全ての設定値をファイルに保存する
		((NLiveRoid)getApplicationContext()).updateDetailsFile();
		super.onPause();
	}
	public static Details getPref(){
		return details ;
	}

	public String getDetailMapValue(String key){
		return detailsMap.get(key);
	}

	//列幅の値を型変換後リストで取得
	public Map<String,Integer> getPWidthList(){
		Map<String,Integer> wlist = new HashMap<String,Integer>();
		wlist.put("type_width_p",Integer.parseInt(detailsMap.get("type_width_p")));
		wlist.put("id_width_p",Integer.parseInt( detailsMap.get("id_width_p")));
		wlist.put("command_width_p",Integer.parseInt(detailsMap.get("command_width_p")));
		wlist.put("time_width_p",Integer.parseInt(detailsMap.get("time_width_p")));
		wlist.put("score_width_p",Integer.parseInt(detailsMap.get("score_width_p")));
		wlist.put("num_width_p",Integer.parseInt( detailsMap.get("num_width_p")));
		wlist.put("comment_width_p",Integer.parseInt( detailsMap.get("comment_width_p")));
		return wlist;
	}
	public Map<String,Integer> getLWidthList(){
		Map<String,Integer> wlist = new HashMap<String,Integer>();
		wlist.put("type_width_l",Integer.parseInt(detailsMap.get("type_width_l")));
		wlist.put("id_width_l",Integer.parseInt( detailsMap.get("id_width_l")));
		wlist.put("command_width_l",Integer.parseInt(detailsMap.get("command_width_l")));
		wlist.put("time_width_l",Integer.parseInt(detailsMap.get("time_width_l")));
		wlist.put("score_width_l",Integer.parseInt(detailsMap.get("score_width_l")));
		wlist.put("num_width_l",Integer.parseInt( detailsMap.get("num_width_l")));
		wlist.put("comment_width_l",Integer.parseInt( detailsMap.get("comment_width_l")));
		return wlist;
	}
	public void setPreferenceKeyValue(String key, int value){
		onPreferenceChange(details.findPreference(key),value);
	}
	public void setDetailsMap(String key,int value){//QuickSettingDialogのみで使用
	detailsMap.put(key, String.valueOf(value));
	}
	/**
	 * 裏再生中はTopTabsがSingleInstanceな為、裏再生中のACTに戻っちゃうので
	 * それを防ぐ
	 */
	@Override
	public void finish()
	{
		Intent topTab = new Intent(this,TopTabs.class);
		startActivity(topTab);
		super.finish();
	}

	/**
	 * 設定値変更でマップを更新する
	 *
	 */
	@Override
	public boolean onPreferenceChange(Preference preferences, Object paramObj) {
		if (preferences.getKey().equals("layer_num")) {
			boolean isNoflash = false;
			try {
				getPackageManager().getApplicationInfo("com.adobe.flashplayer",
						0);
			} catch (PackageManager.NameNotFoundException localNameNotFoundException) {
				// フラッシュがインストールされてない
				isNoflash = true;
				if(!String.valueOf(paramObj).equals("3")){
					MyToast.customToastShow(this, "フラッシュプレイヤーがインストールされていません");
				}
				layer_num.setValueIndex(3);
			}
			if(!isNoflash){
			layer_num.setValueIndex(Integer.parseInt(String.valueOf(paramObj)));
			}
			onlyCommentReflect();
		}else if(preferences.getKey().equals("fix_volenable")){
			//コメントオンリーなら音量は設定できないonResumeで無効なはず
			if(layer_num.getValue().equals("3")){
			return false;
			}
			if((Boolean)paramObj){
				SeekBarPreference.setSeekEnable(true);
			}else{
				SeekBarPreference.setSeekEnable(false);
				}
		}else if(preferences.getKey().equals("sp_player")){
			//横画面に全面の選択肢を作る
				if((Boolean)paramObj){
					player_pos_l.setEntries(R.array.player_pos_rightleftall);
					player_pos_l.setEntryValues(R.array.player_posval_rightleftall);
				}else{
				player_pos_l.setEntries(R.array.player_pos_rightleft);
				player_pos_l.setEntryValues(R.array.player_posval_rightleftupdown);
				}
				int defaultPos = 0;
				try{
					defaultPos = Integer.parseInt(detailsMap.get("player_pos_l"));
				}catch(NumberFormatException e){
					e.printStackTrace();
				}
				try{
				player_pos_l.setValueIndex(defaultPos);
				}catch(ArrayIndexOutOfBoundsException e){
					player_pos_l.setValueIndex(0);
				}
		}else if(preferences.getKey().equals("speech_enable")){
			String enable_before = detailsMap.get("speech_enable");
			if((Boolean)paramObj){//有効にした
				//今設定されている読み上げで設定する
				//0 TTSのOFF
				//1 TTSのON
				//2 AquesTalkのOFF
				//3 AquesTalkのON
				if(enable_before.equals("0")){//標準エンジンにした
					detailsMap.put("speech_enable", "1");
					speech_engine.setValueIndex(0);
					speech_engine.setSummary("標準エンジン");
					//日本語TTSがインストールされているか確かめる
//					Intent checkIntent = new Intent();
//					checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
//					startActivityForResult(checkIntent, CODE.IS_INSTALL_TTS);
					SpeechTestPreference.setEnable_(true,false);
					SpeechParamSeekBar.setEnable_(true,false,true);
				}else if(enable_before.equals("2")){//Aquesにした
					detailsMap.put("speech_enable", "3");
					speech_engine.setValueIndex(1);
					speech_engine.setSummary("AquesTalk");
					SpeechTestPreference.setEnable_(true, true);
					SpeechParamSeekBar.setEnable_(true,true,false);
				}
				onSpeechReflect(true);
			}else{
				if(enable_before.equals("1")){
					detailsMap.put("speech_enable", "0");
					SpeechTestPreference.setEnable_( false,false);
					SpeechParamSeekBar.setEnable_(true,false,true);
				}else if(enable_before.equals("3")){
					detailsMap.put("speech_enable", "2");
					SpeechTestPreference.setEnable_( false,true);
					SpeechParamSeekBar.setEnable_(true,true,false);
				}
				onSpeechReflect(false);
			}
			return true;//speechの設定は最後そのままセットはしない
		}else if(preferences.getKey().equals("speech_engine")){
			String param = String.valueOf(paramObj);
			if(param.equals("0")){//TTSにした
				detailsMap.put("speech_enable", "1");
				speech_engine.setSummary("標準エンジン");
				//日本語TTSがインストールされているか確かめる
//				Intent checkIntent = new Intent();
//				checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
//				startActivityForResult(checkIntent, CODE.IS_INSTALL_TTS);
				}else if(param.equals("1")){//AQUESTALKにした
					//辞書ファイルがあるか確かめる
					// すでに展開済み？
		    		String filepath = getFilesDir().getAbsolutePath() + "/" +  "copyed.dat";
		    		File file = new File(filepath);
		    		boolean isExists = file.exists();
		    		if(isExists){//辞書ファイルがあればAques有効にする
				detailsMap.put("speech_enable","3");
				speech_engine.setSummary("AquesTalk");
				SpeechTestPreference.setEnable_(true,true);
				SpeechParamSeekBar.setEnable_(true,true,false);
		    		}else{
		    			new AlertDialog.Builder(this)
		    			.setTitle("辞書ファイルダウンロード")
		    			.setMessage("辞書ファイルがインストールされていません\n約27MBになりますが、\nダウンロードしますか?")
		    			.setPositiveButton("YES", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
				    			copyDic();
							}
						})
						.setNegativeButton("NO", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
							}
						}).create().show();
		    			return false;//キャンセルしておいて、展開タスク終了後、スピナーUIに値をセット
		    		}
				}
			return true;
		}else if(preferences.getKey().equals("select_back_img_t")){
				if(String.valueOf(paramObj).equals("1")){
					//ギャラリーを開く
					Intent intent = new Intent();
					intent.setType("image/*");
					intent.setAction(Intent.ACTION_GET_CONTENT);
					startActivityForResult(intent, CODE.REQUEST_GALALY_TAB);
				}else{
					//ファイルを消す
					details.deleteFile("back_t");
				}
				return true;
		}else if(preferences.getKey().equals("select_back_img_v")){
				if(String.valueOf(paramObj).equals("1")){
			//ギャラリーを開く
			Intent intent = new Intent();
			intent.setType("image/*");
			intent.setAction(Intent.ACTION_GET_CONTENT);
			startActivityForResult(intent, CODE.REQUEST_GALALY_PLAYERVIEW);
				}else{
					//ファイルを消す
					details.deleteFile("back_v");
				}
				return true;
		}
		Log.d("Details","onPreferenceChange " + preferences.getKey() + "  " + String.valueOf(paramObj));
		detailsMap.put(preferences.getKey(), String.valueOf(paramObj));
	return true	;
	}

	//サマリーを変えた値で更新する
	public void updatePSummary(){
		type_p.setSummary(detailsMap.get("type_width_p"));
		id_p.setSummary(detailsMap.get("id_width_p"));
		command_p.setSummary(detailsMap.get("command_width_p"));
		time_p.setSummary(detailsMap.get("time_width_p"));
		score_p.setSummary(detailsMap.get("score_width_p"));
		num_p.setSummary(detailsMap.get("num_width_p"));
		comment_p.setSummary(detailsMap.get("comment_width_p"));
	}
	public void updateLSummary(){
		type_l.setSummary(detailsMap.get("type_width_l"));
		id_l.setSummary(detailsMap.get("id_width_l"));
		command_l.setSummary(detailsMap.get("command_width_l"));
		time_l.setSummary(detailsMap.get("time_width_l"));
		score_l.setSummary(detailsMap.get("score_width_l"));
		num_l.setSummary(detailsMap.get("num_width_l"));
		comment_l.setSummary(detailsMap.get("comment_width_l"));
	}


	/**
	 * コメントのみに設定した場合のその他の設定値UI+設定値マップの更新
	 * onCreateとonPreferenceChangedから呼ばれる
	 * @param isOnlyComment
	 */
	private void onlyCommentReflect(){

		if(layer_num.getValue().equals("3")){//コメントのみ時はプレイヤー+音設定を無効にする
			player_select.setEnabled(false);
			fixvolenable.setEnabled(false);
			player_pos_p.setEnabled(false);
			player_pos_l.setEnabled(false);
			manner_0.setEnabled(false);
		detailsMap.put("manner_0","false");
		detailsMap.put("fix_volenable","false");
		fixvolenable.setEnabled(false);
		SeekBarPreference.setSeekEnable(false);
		player_quality.setEnabled(false);
		}else{
			player_select.setEnabled(true);
			manner_0.setEnabled(true);
			fixvolenable.setEnabled(true);
			player_pos_p.setEnabled(true);
			player_pos_l.setEnabled(true);
			player_quality.setEnabled(true);
		}
	}

	/**
	 * 読み上げON時に他を有効にする
	 */
	private void onSpeechReflect(boolean isEnable){
		if(isEnable){
			speech_engine.setEnabled(true);
			speech_skip_word.setEnabled(true);
			education_screen.setEnabled(true);
//			speech_education_enable.setEnabled(true);
			SpeechSkipCountPicker.setEnable_(true);
		}else{
			speech_engine.setEnabled(false);
			speech_skip_word.setEnabled(false);
			education_screen.setEnabled(false);
//			speech_education_enable.setEnabled(false);
			SpeechSkipCountPicker.setEnable_(false);
		}
	}

	//Aquesの時に辞書ファイル削除したら設定値をOFF(0)にする
	public void setSpeechEnable_To_0(){
		if(detailsMap != null){
			detailsMap.put("speech_enable", "0");
		}
		if(speech_engine != null){
			speech_engine.setValueIndex(0);
			speech_engine.setSummary("標準エンジン");
		}
		if(delete_dic != null)delete_dic.setEnabled(false);
		SpeechParamSeekBar.setEnable_(true,false, true);
	}

	/**
	 * TTSエンジンがインストールされているかの返り値を取得
	 */

	protected void onActivityResult(
	        int requestCode, int resultCode, Intent data) {
		if (requestCode == CODE.REQUEST_GALALY_TAB&&data != null){
			Bitmap bmp = createBackBMP(data.getData());
			if(bmp != null){
				bmpFileOut("back_t",bmp);
			}
		}else if(requestCode == CODE.REQUEST_GALALY_PLAYERVIEW&&data != null) {
			Bitmap bmp = createBackBMP(data.getData());
			if(bmp != null){
				bmpFileOut("back_v",bmp);
			}
		}else if (requestCode == CODE.IS_INSTALL_TTS) {
	        if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
	            //何かしらのTTSがインストールされている
	        	//教育ファイルがなければ生成しておく
//	        	checkEducationFile();
	        } else {
	            //インストールされていない
	            Intent installIntent = new Intent();
	            installIntent.setAction(
	                TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
	            startActivity(installIntent);
	        }
	    }
	}


	private Bitmap createBackBMP(Uri uri){
		Bitmap bitmap = null;
		try {
			InputStream is = getContentResolver().openInputStream(uri);
			bitmap = BitmapFactory.decodeStream(is);
			is.close();
			return bitmap;
		} catch (FileNotFoundException e) {
			MyToast.customToastShow(details, "画像ファイルがみつかりませんでした");
			e.printStackTrace();
		} catch (IOException e) {
			MyToast.customToastShow(details, "画像ファイル読み込みでエラーしました");
			e.printStackTrace();
		}
		return null;
	}

	private void bmpFileOut(String filename,Bitmap bmp){
		try {
			FileOutputStream fos = details.openFileOutput(filename, details.MODE_PRIVATE);
			bmp.compress(CompressFormat.JPEG, 100, fos);
		} catch (FileNotFoundException e) {
			MyToast.customToastShow(details, "画像ファイルがみつかりませんでした");
			e.printStackTrace();
		}

	}


	//初回起動時に辞書データを展開する
    // 別スレッドで処理
    // /data/data/<app>/files/copyed.datが存在しなかったら
    // /assets/aq_dic.zip を /data/data/<app>/files/に ファイルを展開
    private void copyDic(){
    	try {

    			initDialog();

    	} catch (Exception e) {
			e.printStackTrace();
    	}
    			new AsyncTask<Void,Void,Integer>(){
    				@Override
    				public void onPreExecute(){
    			    	dialog = new ProgressDialog(details);	// 起動時に一度しか呼ばれないので
    			    	dialog.setTitle("ダウンロード中");
    			    	dialog.setMessage("数分かかることがあります");
    			    	dialog.setIndeterminate(false);
    			    	dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
    			    	dialog.setMax(100);
    			    	dialog.setCancelable(false);
    			    	dialog.show();
    				}
    				@Override
    				public void onCancelled(){
    					super.onCancelled();
    					if(dialog != null && dialog.isShowing())dialog.cancel();
    				}
					@Override
					protected Integer doInBackground(Void... params) {
						try{
//    			    	    AssetManager    am  = getResources().getAssets();
//    			    	    InputStream is  = am.open("aq_dic.zip", AssetManager.ACCESS_STREAMING);
    						//isに辞書ファイル
    						InputStream is = null;
    						HttpURLConnection con = null;
    						URL url = null;
    						try {
    							switch(new Random().nextInt(3)){
    							case 0:
    								url = new URL("http://app-gb-gb.appspot.com");
    								break;
    							case 1:
    								url = new URL("http://app-gc.appspot.com");
    								break;
    							case 2:
    								url = new URL("http://app-gd.appspot.com/");
    								break;
    							}
        						con = (HttpURLConnection)url.openConnection();
    							con.setRequestProperty("Cookie", "nlive.pass=x&");
    							String cookie = con.getHeaderField("Set-Cookie");
    							Log.d("NLiveRoid","For Aques responseCode --- " + con.getResponseCode());
    							Log.d("NLiveRoid","content "+con.getContentLength());
    							Log.d("NLiveRoid","randValue " + cookie);
    							if(con.getResponseCode() != 200 ||cookie == null){//失敗
    								return -1;
    							}
    							Matcher mc = Pattern.compile("[0-9]++").matcher(cookie);
    							String val = "";

    							if(!mc.find(25)){
    								return -2;
    							}else{
    								Log.d("NLiveRoid","Aquest RANDVALUE "+mc.group());
    								val = mc.group();
    							int rand_ = Integer.parseInt(val);
    							int one=rand_-(rand_/3);
    					            one=one+(one%111);
    					        int two=one-(one/12);
    					            two =two+(two%999);
    					            String result = String.valueOf(two)+".0";
    							Log.d("NLiveRoid","Aquest result"+result);

    							con = (HttpURLConnection)url.openConnection();//再接続する
    							con.setRequestProperty("Cookie", "nlive.pass=t&nlive.test="+result);
    							String response = con.getHeaderField("Set-Cookie");
    							String contentLength = String.valueOf(con.getContentLength())+"\n";
    							String res =String.valueOf(result)+"\n";
    							Log.d("NLiveRoid","Aquest con2 content "+contentLength);
    							Log.d("NLiveRoid","Aquest con2 res --- "+response);
    							if(response != null && response.equals("OK")){
    								is = con.getInputStream();
    							}else{
    								return -1;
    							}

    							}
    						} catch (MalformedURLException e) {
    							e.printStackTrace();
    						} catch (IOException e) {
    							e.printStackTrace();
    						}
    			    	    ZipInputStream  zis = new ZipInputStream(is);
    			    	    ZipEntry        ze  = zis.getNextEntry();

			    		    int totalSize=0;
			    		    File dic_dir = new File(getFilesDir().toString() + "/aq_dic");
			    		    if(!dic_dir.exists()){
			    		    	dic_dir.mkdir();
			    		    }
    			    	    for(;ze != null;) {
    			    	        String path = getFilesDir().toString() + "/" + ze.getName();
    			    	        FileOutputStream fos = null;
    			    	        try{
    			    	        fos = new FileOutputStream(path, false);
    			    	        }catch(FileNotFoundException e){
    			    	        	//初期時無かったら作る
    			    	        	new File(path).createNewFile();
        			    	        fos = new FileOutputStream(path, false);
    			    	        }
    			    		    byte[] buf = new byte[8192];
    			    		    int size = 0;
    			    		    int posLast=0;
    			    		    while ((size = zis.read(buf, 0, buf.length)) > -1) {
    			    		        fos.write(buf, 0, size);
    			    		        totalSize += size;
        			    		    int pos = totalSize*100/27220452+1;
        			    		    if(posLast!=pos){
        			    		    	dialog.setProgress(pos);
        			    		    	posLast=pos;
        			    		    }
    			    		    }

    			    		    fos.close();
    			    		    zis.closeEntry();
    			    	    	ze  = zis.getNextEntry();
    			    	    }
			    		    is.close();
    			    	    zis.close();
    			    	    con.disconnect();
    			    	    // コピー完了のマークとして、copyed.datを作成
    			    	    	String filepath = getFilesDir().getAbsolutePath() + "/" +  "copyed.dat";
    			    	    	FileOutputStream fos = new FileOutputStream(filepath, false);
    			    		    byte[] buf = new byte[1];
    			    		    buf[0]='*';
			    		        fos.write(buf, 0, 1);
			    		        fos.close();
    					} catch(Exception e){
    						e.printStackTrace();

    					}
						return 0;
					}
					@Override
					protected void onPostExecute(Integer arg){
						if(dialog != null && dialog.isShowing())dialog.cancel();
							switch(arg){
							case 0:
								//成功
								if(speech_engine != null){
									speech_engine.setValueIndex(1);
								}
								if(detailsMap!= null)detailsMap.put("speech_enable","3");
								if(speech_engine != null)speech_engine.setSummary("AquesTalk");
				    			if(delete_dic != null)delete_dic.setEnabled(true);
								SpeechTestPreference.setEnable_(true,true);
								SpeechParamSeekBar.setEnable_(true,true, false);
								return;
							case -1:
								MyToast.customToastShow(details, "辞書ファイルのダウンロード失敗\nしばらくしてからもう一度お試しください code:0000");
								break;
							case -2:
								MyToast.customToastShow(details, "辞書ファイルのダウンロードに失敗\nしばらくしてからもう一度お試しください code:1111");
								break;
							}
							//失敗
							if(speech_engine != null){
								speech_engine.setValueIndex(0);
								speech_engine.setSummary("標準エンジン");
								SpeechTestPreference.setEnable_(false,false);
								SpeechParamSeekBar.setEnable_(true,false,true);
							}
							if(detailsMap!= null)detailsMap.put("speech_enable","0");
					}
    			}.execute();

    }

    /**
     * オフタイマーのアラートダイアログ
     * 視聴側と同じの2つ書くのだるいから設置
     */
    static class OffTimerNp extends AlertDialog.Builder{
    	private Timer timer;
    	private AsyncTask<Void,Void,Void> manyTimerTask;
		public OffTimerNp(final Context context,final long offtimer_start,final int defaultValue) {
			super( context);

			final TextView tv = new TextView( context);
			tv.setGravity(Gravity.CENTER);

			if(offtimer_start > 0){//オフタイマー起動中が決定
				if(timer != null){
					timer.cancel();
					timer.purge();
				}
				timer = new Timer();
				timer.schedule(new TimerTask(){//残り時間計算
					private long remainTime = 0;
					private int minutes = 0;
					private int seconds = 0;
					@Override
					public void run() {
						if(remainTime == 0){
							remainTime = (defaultValue*60)-(System.currentTimeMillis()-offtimer_start)/1000;
							minutes = (int) (remainTime/60);
							seconds = (int) (remainTime%60);
						}
						seconds--;
						if(seconds<0){
							seconds = 59;
							minutes--;
						}
						if(manyTimerTask != null&&manyTimerTask.getStatus() != AsyncTask.Status.FINISHED){
							manyTimerTask.cancel(true);
						}
						manyTimerTask = new AsyncTask<Void,Void,Void>(){//contextがカスタムプリファランスからだとActivityにできないので毎回AsyncTask生成しちゃう
							@Override
							protected Void doInBackground(Void... params) {
								return null;
							}
							@Override
							protected void onPostExecute(Void arg){
								tv.setText(String.format("残り%d:%02d",minutes,seconds));
							}
						}.execute();
					}
				},0,1000);
			}
			final NumberPicker np = new NumberPicker( context);
			np.setRange(1, 120);
			if(defaultValue < 1){
				np.setCurrent(30);
			}else{
			np.setCurrent(defaultValue);
			}
			setTitle("アプリ起動後からの分を設定します");
			setPositiveButton("OK",new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Intent intent = new Intent();//新規オフタイマーをセットする
					intent.setAction("bindTop.NLR");
					intent.putExtra("off_timer", String.valueOf(np.getCurrent()));
					((Context)context).sendBroadcast(intent);
				}
			});
			setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {

				}
			});
			setOnCancelListener(new OnCancelListener(){
				@Override
				public void onCancel(DialogInterface dialog) {
					if(timer != null){
						timer.cancel();
						timer.purge();
					}
				}
			});
			ScrollView sv = new ScrollView( context);
			TableLayout tl = new TableLayout( context);
			tl.setStretchAllColumns(true);
			TableRow tr0 = new TableRow( context);
			tr0.addView(tv,-2,-1);
			TableRow tr1 = new TableRow( context);
			tr1.addView(np);
			tl.addView(tr0,-2,-1);
			tl.addView(tr1,-2,-1);
			sv.addView(tl,-1,-1);
			setView(sv);
		}

    }


    // 初回起動時のプログレスダイアログの初期化
    protected void initDialog()
    {
    }



}
