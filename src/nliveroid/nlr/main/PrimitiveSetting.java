package nliveroid.nlr.main;

import java.util.HashMap;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.webkit.CookieManager;

public class PrimitiveSetting extends PreferenceActivity implements
		Preference.OnPreferenceChangeListener {

	private static PrimitiveSetting ACT;
	private HashMap<String, String> defaultMap;
	private HashMap<String,String> detailsMap;
	private CheckBoxPreference always1;
	private CheckBoxPreference always2;
	private CheckBoxPreference ac_confirm;
	private EditTextPreference user1;
	private EditTextPreference pass1;
	private EditTextPreference user2;
	private EditTextPreference pass2;
	private CheckBoxPreference alert_enable;
	private CheckBoxPreference alert_sound_notif;
	private CheckBoxPreference alert_vibration_enable;

	private boolean isCookieChange;


	private boolean ListenerFlug = true;
	private NLiveRoid app;
	private ErrorCode error;
	private PreferenceScreen twitter_screen;
	private CheckBoxPreference alert_led;

	/**
	 * ファイル保存でテキストとBooleanがあるのでsettingMapはString統一
	 */

	@Override
	public void onCreate(Bundle paramBundle) {
		super.onCreate(paramBundle);
		setContentView(R.layout.config);
		ACT = this;
		addPreferencesFromResource(R.xml.preference);// preferenceをリソースから読み込む
		always1 = (CheckBoxPreference) findPreference("always_use1");
		always1.setOnPreferenceChangeListener(this);
		always2 = (CheckBoxPreference)findPreference("always_use2");
		always2.setOnPreferenceChangeListener(this);
		ac_confirm = (CheckBoxPreference)findPreference("ac_confirm");
		ac_confirm.setOnPreferenceChangeListener(this);
		user1 = (EditTextPreference)findPreference("user_id1");
		user1.setOnPreferenceChangeListener(this);
		pass1 = (EditTextPreference)findPreference("password1");
		pass1.setOnPreferenceChangeListener(this);
		user2 = (EditTextPreference)findPreference("user_id2");
		user2.setOnPreferenceChangeListener(this);
		pass2 = (EditTextPreference)findPreference("password2");
		pass2.setOnPreferenceChangeListener(this);

		alert_enable = (CheckBoxPreference)findPreference("alert_enable");
		alert_enable.setOnPreferenceChangeListener(this);
		alert_sound_notif = (CheckBoxPreference)findPreference("alert_sound_notif");
		alert_sound_notif.setOnPreferenceChangeListener(this);
		alert_vibration_enable = (CheckBoxPreference)findPreference("alert_vibration_enable");
		alert_vibration_enable.setOnPreferenceChangeListener(this);
		alert_led = (CheckBoxPreference)findPreference("alert_led");
		alert_led.setOnPreferenceChangeListener(this);
		//ここではsettingMapの値をデフォルト値に使い、SharedPreferenceは利用しない
		twitter_screen = (PreferenceScreen)findPreference("twitter_screen");

		CheckBoxPreference log = (CheckBoxPreference) findPreference("nlr_log");
		log.setOnPreferenceChangeListener(this);

		loadSettings();
	}

	public void loadSettings(){
		app = (NLiveRoid)getApplicationContext();
		if(app == null)return;
		defaultMap = app.getDefaultMap();
		if(defaultMap == null)return;
		detailsMap = app.getDetailsMap();
		if(detailsMap == null)return;
		error = app.getError();
		if (defaultMap != null) {
			if (defaultMap.get("user_id1") != null&&!defaultMap.get("user_id1").equals("null")) {
				user1.setText(defaultMap.get("user_id1"));
			}else{
				user1.setText("");
			}
			if (defaultMap.get("password1") != null&&!defaultMap.get("password1").equals("null")) {
				pass1.setText(defaultMap.get("password1"));
			}else{
				pass1.setText("");
			}
			if (defaultMap.get("user_id2") != null&&!defaultMap.get("user_id2").equals("null")) {
				user2.setText(defaultMap.get("user_id2"));
			}else{
				user2.setText("");
			}
			if (defaultMap.get("password2") != null&&!defaultMap.get("password2").equals("null")) {
				pass2.setText(defaultMap.get("password2"));
			}else{
				pass2.setText("");
			}
			if (defaultMap.get("twitter_token") != null&&!defaultMap.get("twitter_token").equals("null")
					&&defaultMap.get("twitter_token").replaceAll("<<T_SPLIT>>", "").length()>2) {
				twitter_screen.setSummary("設定済");
			}else{
				twitter_screen.setSummary("未設定");
			}
		}

		if(detailsMap != null){
			if (detailsMap.get("always_use1") != null) {
				boolean always_use1 = Boolean.parseBoolean(detailsMap.get("always_use1"));
				always1.setChecked(always_use1);
			}
			if (detailsMap.get("always_use2") != null) {
				boolean always_use2 = Boolean.parseBoolean(detailsMap.get("always_use2"));
				always2.setChecked(always_use2);
				}
			if (detailsMap.get("ac_confirm") != null) {
				boolean confirm = Boolean.parseBoolean(detailsMap.get("ac_confirm"));
				ac_confirm.setChecked(confirm);
				}


			if (detailsMap.get("alert_enable") != null) {
				boolean enable = Boolean.parseBoolean(detailsMap.get("alert_enable"));
				alert_enable.setChecked(enable);
			}
			if (detailsMap.get("alert_sound_enable") != null) {
				boolean notif = Boolean.parseBoolean(detailsMap.get("alert_sound_notif"));
				alert_sound_notif.setChecked(notif);
			}
			if (detailsMap.get("alert_vibration_enable") != null) {
				boolean vibration = Boolean.parseBoolean(detailsMap.get("alert_vibration_enable"));
				alert_vibration_enable.setChecked(vibration);
			}
			if (detailsMap.get("alert_led") != null) {
				boolean led = Boolean.parseBoolean(detailsMap.get("alert_led"));
				alert_led.setChecked(led);
			}

			if (detailsMap.get("nlr_log") != null) {
				CheckBoxPreference nlr_log = (CheckBoxPreference)findPreference("nlr_log");
				nlr_log.setChecked(Boolean.parseBoolean(detailsMap.get("nlr_log")));
			}
		}
		if(Details.getPref() != null){
			Details.getPref().loadSettings(detailsMap);//何故か参照をapplicationのフィールド経由で渡せない為、引数に取る
		}

	}
	@Override
	public void onResume(){
//		alertenable.setChecked(isBinded());
		super.onResume();
	}

	/*設定値保存と、アカウント変更時クッキーを消す
	 * @see android.app.Activity#onPause()
	 */

	@Override
	public void onPause(){
		ListenerFlug = false;
		//全ての設定値をファイルに保存する
		((NLiveRoid)getApplicationContext()).updateAccountFile();
		if(isCookieChange){
			CookieManager.getInstance().removeAllCookie();
			app.setSessionid("");
			((NLiveRoid)getApplicationContext()).removeTopTabsAdapter();
		}
		super.onPause();
	}

	public void deleteAllPreference(){
		((NLiveRoid)getApplicationContext()).deleteAllPreference();//設定ファイル全消し
		always1.setChecked(false);
		always2.setChecked(false);
		//フォームの値が何故か消されない
		EditTextPreference ep = (EditTextPreference)findPreference("user_id1");
		ep.setText("");
		ep = (EditTextPreference)findPreference("password1");
		ep.setText("");
		ep = (EditTextPreference)findPreference("user_id2");
		ep.setText("");
		ep = (EditTextPreference)findPreference("password2");
		ep.setText("");
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
	 *
	 * サウンド、インターバル用のコミットメソッドを定義
	 */
	public String getAlertParams(String key){
		return detailsMap.get(key);
	}
	public void preferenceChangedExt(String key,String uriStr){
//		Log.d("NLiveRoid","CHANGEP ----- " + key + " " + uriStr);
		onPreferenceChange(findPreference(key), uriStr);
	}


	/**
	 * プレファランスが変更された時に呼ばれる ここに設定値保存時の処理を書く
	 */
	@Override
	public boolean onPreferenceChange(Preference preferences, Object value) {// 変わってからの値が入ってくる ダイアログキャンセル時呼ばれない

		//なぜかレポートがでているので追加0.8.73
		if(defaultMap == null){
		app = (NLiveRoid)getApplicationContext();
		if(app == null)return false;
		defaultMap = app.getDefaultMap();
		if(defaultMap == null)return false;
		error = app.getError();
		}
		if(detailsMap == null){
			app = (NLiveRoid)getApplicationContext();
			if(app == null)return false;
		detailsMap = app.getDetailsMap();
		if(detailsMap == null)return false;
		error = app.getError();
		}

		if(value == null){
			value = false;
		}
		if(preferences.getKey().equals("alert_enable")){
			if((Boolean)value){
				//アラートスタートは、ここと起動時2箇所
				BackGroundService.prepareAlert();
				BackGroundService.registerNextAlert();
			}else{//ストップ
				BackGroundService.unRegisterAlert();
			}
			detailsMap.put(preferences.getKey(), String.valueOf(value));
		}else if(preferences.getKey().equals("fexit")){
			detailsMap.put(preferences.getKey(), String.valueOf(value));
		} else if (preferences.getKey().equals("always_use1")) {
			if(always2.isChecked()&&(Boolean)value){//2が有効だった場合、2をはずす
				detailsMap.put("always_use2", "false");
				always2.setChecked(false);
			}else if(!(Boolean)value&&!always2.isChecked()){//どちらもチェックされてなくなる場合、無効にする
				return false;
			}
			detailsMap.put("always_use1", String.valueOf(value));
			isCookieChange = true;
		}  else if (preferences.getKey().equals("always_use2")) {
			if(always1.isChecked()&&(Boolean)value){//1が有効だった場合、1をはずす
				detailsMap.put("always_use1", "false");
				always1.setChecked(false);
			}else if(!(Boolean)value&&!always1.isChecked()){//どちらもチェックされなくなる場合、無効にする
				return false;
			}
			detailsMap.put("always_use2", String.valueOf(value));
			isCookieChange = true;
		}  else if (preferences.getKey().equals("user_id1")) {
			defaultMap.put("user_id1", String.valueOf(value));
			//IDとパスが揃ったらそのアカウントを常に利用する
			String pass1 = defaultMap.get("password1");
			if(pass1!=null&&!pass1.equals("")){
				detailsMap.put("always_use1","true");
				detailsMap.put("always_use2","false");
				always1.setChecked(true);
				always2.setChecked(false);
			}
			isCookieChange = true;
		} else if (preferences.getKey().equals("password1")) {
			defaultMap.put("password1", String.valueOf(value));
			//IDとパスが揃ったらそのアカウントを常に利用する
			String userid1 = defaultMap.get("user_id1");
			if(userid1!=null&&!userid1.equals("")){
				detailsMap.put("always_use1", "true");
				detailsMap.put("always_use2", "false");
				always1.setChecked(true);
				always2.setChecked(false);
			}
			isCookieChange = true;
		}else if (preferences.getKey().equals("user_id2")) {
			defaultMap.put("user_id2", String.valueOf(value));

			//IDとパスが揃ったらそのアカウントを常に利用する
			String pass2 = defaultMap.get("password2");
			if(pass2!=null&&!pass2.equals("")){
				detailsMap.put("always_use2", "true");
				detailsMap.put("always_use1", "false");
				always2.setChecked(true);
				always1.setChecked(false);
			}
			isCookieChange = true;
		} else if (preferences.getKey().equals("password2")) {
			defaultMap.put("password2", String.valueOf(value));
			//IDとパスが揃ったらそのアカウントを常に利用する
			String userid2 = defaultMap.get("user_id2");
			if(userid2!=null&&!userid2.equals("")){
				detailsMap.put("always_use2", "true");
				detailsMap.put("always_use1", "false");
				always2.setChecked(true);
				always1.setChecked(false);
			}
			isCookieChange = true;
		}
		else{
			detailsMap.put(preferences.getKey(), String.valueOf(value));
		}

		return true;
	}


	/**
	 * ACTを取得します。
	 * @return ACT
	 */
	public static PrimitiveSetting getACT() {
	    return ACT;
	}

	public void updateAlways(){
		if(detailsMap != null){
			if (detailsMap.get("always_use1") != null) {
				boolean always_use1 = Boolean.parseBoolean(detailsMap.get("always_use1"));
				always1.setChecked(always_use1);
			}
			if (detailsMap.get("always_use2") != null) {
				boolean always_use2 = Boolean.parseBoolean(detailsMap.get("always_use2"));
				always2.setChecked(always_use2);
			}
		}
	}



	public String getSessionTutorial() {
		Log.d("Log","PRIM - " + getIntent().getStringExtra("session"));
		return this.getIntent().getStringExtra("session");
	}

	public void setTwitterSummary(boolean b) {
		if(twitter_screen != null){
			if(b){
				twitter_screen.setSummary("設定済");
			}else{
				twitter_screen.setSummary("未設定");
			}
		}
	}








}