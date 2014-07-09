package nliveroid.nlr.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import nliveroid.nlr.main.parser.XMLparser;

import org.xmlpull.v1.XmlPullParserException;

import android.app.AlertDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;

	public class SettingFileDialog extends DialogPreference{


	private String key;
	private Context context;
	private final String fileName = "Settings.xml";

	public SettingFileDialog(Context cont,AttributeSet attr,int i){
		super(cont,attr,i);
	}
	public SettingFileDialog(Context context, AttributeSet attr) {
		super(context, attr);
		key = attr.getAttributeValue(2);
		if(key.equals("clear_setting")){
		setDialogMessage("設定値を全てクリアします。\nよろしいですか?");
		}else if(key.equals("setting_file_import")){
			setDialogMessage("Settings.xmlから設定値をインポートしますか?");
		}else if(key.equals("setting_file_export")){
			setDialogMessage("現在の設定値をエクスポートしますか?");
		}else if(key.equals("setting_dic_delete")){
			setDialogMessage("辞書ファイルを削除しますか?");
		}
	}


	class CloseOnlyDialog extends AlertDialog.Builder{
		public CloseOnlyDialog(Context arg0,String message) {
			super(arg0);
			this.setMessage(message);
			this.setNegativeButton("CLOSE", null);
		}
	}
	 @Override
	 protected void onDialogClosed(boolean positiveResult) {
	 super.onDialogClosed(positiveResult);
	 if(positiveResult){
		 if(key == null){
			 new CloseOnlyDialog(this.getContext(),"不明なエラーが発生しました:CODE 001").show();
			 return;
		 }
		 if(key.equals("clear_setting")){
			 Context context = null;
			 if(PrimitiveSetting.getACT()!= null){
				 context = PrimitiveSetting.getACT();
			 }
			 if(context == null){
				 new CloseOnlyDialog(this.getContext(),"不明なエラーが発生しました:CODE 002").show();
			 }else{
				 ((PrimitiveSetting) context).deleteAllPreference();
				 new CloseOnlyDialog(this.getContext(),"設定値を初期化しました").show();
			 }
	 	}else if(key.equals("setting_file_import")){
	 		//設定ファイルをXMLからインポートする
	 		new SettingImport().execute();
	 	}else if(key.equals("setting_file_export")){
	 		//設定ファイルをXMLにエクスポートする
	 		new SettingExport().execute();
	 	}else if(key.equals("setting_dic_delete")){
	 		 Context context = null;
			 if(PrimitiveSetting.getACT()!= null){
				 context = Details.getPref();
			 }
	 		//辞書ファイルを消す
	 		if(context == null){
				 new CloseOnlyDialog(this.getContext(),"不明なエラーが発生しました:CODE 003").show();
	 		}else{
	 			//しるしを消す
    	    	String filepath = context.getFilesDir().getAbsolutePath() + "/" +  "copyed.dat";
    	    	new File(filepath).delete();
	 			File aqDir = new File(context.getFilesDir().toString() + "/aq_dic");
	 			if(aqDir != null){
	 			File[] files=aqDir.listFiles();
	 			if(files != null ){
	 			for(int i=0; i<files.length; i++){
	 				files[i].delete();//ディレクトリは無いはず
	 			}
	 			}
	 			aqDir.delete();
	 			}
	 			//読み上げの設定値をOFFにする
	 			((Details)context).setSpeechEnable_To_0();
	 		}
	 	}

	 }

	 }

	 class SettingExport extends AsyncTask<Void,Void,Integer>{
		@Override
		protected Integer doInBackground(Void... params) {
			try {
				 Context context = null;
				 if(PrimitiveSetting.getACT()!= null){
					 context = PrimitiveSetting.getACT();
				 }
				NLiveRoid app = (NLiveRoid)context.getApplicationContext();
				HashMap<String,String> detailsMap = app.getDetailsMap();
				if(detailsMap == null)return -1;
				String filepath = getStorageFilePath();
				if(filepath == null)return -2;
				String xml =
						"<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<Settings xmlns=\"http://nliveroid-tutorial.appspot.com/settings/\">\n"+
								"<common_settings>\n"+
								"<always_use1>"+(detailsMap.get("always_use1")==null? "false":detailsMap.get("always_use1"))+"</always_use1>\n"+
								"<always_use2>"+(detailsMap.get("always_use2")==null? "false":detailsMap.get("always_use2"))+"</always_use2>\n"+
								"<initial_tab>"+(detailsMap.get("last_tab")==null? "0":detailsMap.get("last_tab"))+"</initial_tab>\n"+
								"<initial_bc>"+(detailsMap.get("initial_bc")==null? "true":detailsMap.get("initial_bc"))+"</initial_bc>\n"+
								"<backkey_dialog>"+(detailsMap.get("fexit")==null? "false":detailsMap.get("fexit"))+"</backkey_dialog>\n"+
								"<backkey_append>"+(detailsMap.get("finish_back")==null? "":detailsMap.get("finish_back"))+"</backkey_append>\n"+
								"<only_comment>"+(detailsMap.get("only_comment")==null? "false":detailsMap.get("only_comment").equals("false")? "false":"true")+"</only_comment>\n"+
								"<upform>"+(detailsMap.get("form_up")==null? "false":detailsMap.get("form_up").equals("false")? "false":"true")+"</upform>\n"+
								"<form_backkey>"+(detailsMap.get("form_backkey")==null? "false":detailsMap.get("form_backkey").equals("false")? "false":"true")+"</form_backkey>\n"+
								"<newline>"+(detailsMap.get("newline")==null? "false":detailsMap.get("newline").equals("false")? "false":"true")+"</newline>\n"+
								"<auto_username>"+(detailsMap.get("auto_username")==null? "false":detailsMap.get("auto_username"))+"</auto_username>\n"+
								"<voice_input>"+(detailsMap.get("voice_input")==null? "false":detailsMap.get("voice_input"))+"</voice_input>\n"+
								"<layer_num>"+(detailsMap.get("layer_num")==null? "0":detailsMap.get("layer_num"))+"</layer_num>\n"+
								"<player_quality>"+(detailsMap.get("player_quality")==null? "0":detailsMap.get("player_quality"))+"</player_quality>\n"+
								"<his_value>"+(detailsMap.get("his_value")==null? "-1":detailsMap.get("his_value"))+"</his_value>\n"+
								"<is_update_between>"+(detailsMap.get("is_update_between")==null? "true":detailsMap.get("is_update_between"))+"</is_update_between>\n"+
								"<init_comment_count>"+(detailsMap.get("init_comment_count")==null? "20":detailsMap.get("init_comment_count"))+"</init_comment_count>\n"+
								"<auto_comment_update>"+(detailsMap.get("auto_comment_update")==null? "-1":detailsMap.get("auto_comment_update"))+"</auto_comment_update>\n"+
								"<off_timer>"+(detailsMap.get("off_timer")==null? "-1":detailsMap.get("off_timer"))+"</off_timer>\n"+
								"<player_select>"+(detailsMap.get("player_select")==null? "0":detailsMap.get("player_select"))+"</player_select>\n"+
								"<delay_start>"+(detailsMap.get("delay_start")==null? "true":detailsMap.get("delay_start").equals("false")? "false":"true")+"</delay_start>\n"+
								"<back_black>"+(detailsMap.get("back_black")==null? "false":detailsMap.get("back_black").equals("false")? "false":"true")+"</back_black>\n"+
								"<fix_volume>"+(detailsMap.get("fix_volenable")==null? "false":detailsMap.get("fix_volenable").equals("false")? "false":"true")+"</fix_volume>\n"+
								"<fix_volume_value>"+(detailsMap.get("fix_volvalue")==null? "0":detailsMap.get("fix_volvalue"))+"</fix_volume_value>\n"+
								"<manner_0>"+(detailsMap.get("manner_0")==null? "false":detailsMap.get("manner_0").equals("false")? "false":"true")+"</manner_0>\n"+
								"<return_tab>"+(detailsMap.get("return_tab")==null? "false":detailsMap.get("return_tab").equals("false")? "false":"true")+"</return_tab>\n"+
								"<update_tab>"+(detailsMap.get("update_tab")==null? "false":detailsMap.get("update_tab").equals("false")? "false":"true")+"</update_tab>\n"+
								"<orientation>"+(detailsMap.get("fix_screen")==null? "0":detailsMap.get("fix_screen"))+"</orientation>\n"+
								"<discard_notification>"+(detailsMap.get("discard_notification")==null? "false":detailsMap.get("discard_notification"))+"</discard_notification>\n"+
								"<handlename_at_enable>"+(detailsMap.get("at_enable")==null? "false":detailsMap.get("at_enable"))+"</handlename_at_enable>\n"+
								"<handlename_at_overwrite>"+(detailsMap.get("at_overwrite")==null? "false":detailsMap.get("at_overwrite"))+"</handlename_at_overwrite>\n"+
								"<quick_0>"+(detailsMap.get("quick_0")==null? "15":detailsMap.get("quick_0"))+"</quick_0>\n"+
								"<quick_1>"+(detailsMap.get("quick_1")==null? "127":detailsMap.get("quick_1"))+"</quick_1>\n"+
								"<alpha>"+(detailsMap.get("alpha")==null? "0":detailsMap.get("alpha"))+"</alpha>\n"+
								"<alert_enable>"+(detailsMap.get("alert_enable")==null? "false":detailsMap.get("alert_enable").equals("false")? "false":"true")+"</alert_enable>\n"+
								"<alert_vibration_enable>"+(detailsMap.get("alert_vibration_enable")==null? "false":detailsMap.get("alert_vibration_enable").equals("false")? "false":"true")+"</alert_vibration_enable>\n"+
								"<alert_sound_notif>"+(detailsMap.get("alert_sound_notif")==null? "false":detailsMap.get("alert_sound_notif").equals("false")? "false":"true")+"</alert_sound_notif>\n"+
								"<alert_led>"+(detailsMap.get("alert_led")==null? "false":detailsMap.get("alert_led").equals("false")? "false":"true")+"</alert_led>\n"+
								"<alert_interval>"+(detailsMap.get("alert_interval")==null? "5":detailsMap.get("alert_interval"))+"</alert_interval>\n"+
								"<command_settings>\n" +
								"<cmd_anonym>"+(detailsMap.get("cmd_cmd")==null? "":detailsMap.get("cmd_cmd"))+"</cmd_anonym>\n"+
								"<cmd_size>"+(detailsMap.get("cmd_size")==null? "":detailsMap.get("cmd_size"))+"</cmd_size>\n"+
								"<cmd_color>"+(detailsMap.get("cmd_color")==null? "":detailsMap.get("cmd_color"))+"</cmd_color>\n"+
								"<cmd_align>"+(detailsMap.get("cmd_align")==null? "":detailsMap.get("cmd_align"))+"</cmd_align>\n"+
								"</command_settings>\n"+
								"<speech_settings>\n" +
								"<speech_enable>"+(detailsMap.get("speech_enable")==null? "0":detailsMap.get("speech_enable"))+"</speech_enable>\n"+
								"<speech_speed>"+(detailsMap.get("speech_speed")==null? "50":detailsMap.get("speech_speed"))+"</speech_speed>\n"+
								"<speech_pich>"+(detailsMap.get("speech_pich")==null? "50":detailsMap.get("speech_pich"))+"</speech_pich>\n"+
								"<speech_education_enable>"+(detailsMap.get("speech_education_enable")==null? "true":detailsMap.get("speech_education_enable"))+"</speech_education_enable>\n"+
								"<speech_skip_word>"+(detailsMap.get("speech_skip_word")==null? "いかりゃく":detailsMap.get("speech_skip_word"))+"</speech_skip_word>\n"+
								"<speech_skip_count>"+(detailsMap.get("speech_skip_count")==null? "5":detailsMap.get("speech_skip_count"))+"</speech_skip_count>\n"+
								"<speech_aques_phont>"+(detailsMap.get("speech_aques_phont")==null? "0":detailsMap.get("speech_aques_phont"))+"</speech_aques_phont>\n"+
								"<speech_aques_vol>"+(detailsMap.get("speech_aques_vol")==null? "5":detailsMap.get("speech_aques_vol"))+"</speech_aques_vol>\n"+
								"</speech_settings>\n"+
								"<spplayer_settings>\n" +
								"<sp_showcomment>"+(detailsMap.get("sp_showcomment")==null? "true":detailsMap.get("sp_showcomment"))+"</sp_showcomment>\n"+
								"<sp_ng184>"+(detailsMap.get("sp_ng184")==null? "false":detailsMap.get("sp_ng184"))+"</sp_ng184>\n"+
								"<sp_showbspcomment>"+(detailsMap.get("sp_showbspcomment")==null? "true":detailsMap.get("sp_showbspcomment"))+"</sp_showbspcomment>\n"+
								"<sp_ismute>"+(detailsMap.get("sp_ismute")==null? "false":detailsMap.get("sp_ismute"))+"</sp_ismute>\n"+
								"<sp_loadsmile>"+(detailsMap.get("sp_loadsmile")==null? "false":detailsMap.get("sp_loadsmile"))+"</sp_loadsmile>\n"+
								"<sp_volumesub>"+(detailsMap.get("sp_volumesub")==null? "50":detailsMap.get("sp_volumesub"))+"</sp_volumesub>\n"+
								"</spplayer_settings>\n"+
								"<toptab_tcolor>"+(detailsMap.get("toptab_tcolor")==null? "0":detailsMap.get("toptab_tcolor"))+"</toptab_tcolor>\n"+
								"<column_sequence>\n"+
								"<type_seq>"+(detailsMap.get("type_seq")==null? "0":detailsMap.get("type_seq"))+"</type_seq>\n"+
								"<id_seq>"+(detailsMap.get("id_seq")==null? "1":detailsMap.get("id_seq"))+"</id_seq>\n"+
								"<cmd_seq>"+(detailsMap.get("cmd_seq")==null? "2":detailsMap.get("cmd_seq"))+"</cmd_seq>\n"+
								"<time_seq>"+(detailsMap.get("time_seq")==null? "3":detailsMap.get("time_seq"))+"</time_seq>\n"+
								"<score_seq>"+(detailsMap.get("score_seq")==null? "4":detailsMap.get("score_seq"))+"</score_seq>\n"+
								"<num_seq>"+(detailsMap.get("num_seq")==null? "5":detailsMap.get("num_seq"))+"</num_seq>\n"+
								"<comment_seq>"+(detailsMap.get("comment_seq")==null? "6":detailsMap.get("comment_seq"))+"</comment_seq>\n"+
								"<cellheight_test>"+(detailsMap.get("cellheight_test")==null? "3":detailsMap.get("cellheight_test"))+"</cellheight_test>\n"+
								"</column_sequence>\n"+
								"</common_settings>\n"+
								"<portlayt_settings>\n"+
								"<player_position>"+(detailsMap.get("player_pos_p")==null? "0":detailsMap.get("player_pos_p"))+"</player_position>\n"+
								"<x_position>"+(detailsMap.get("x_pos_p")==null? "0":detailsMap.get("x_pos_p"))+"</x_position>\n"+
								"<x_dragging>"+(detailsMap.get("xd_enable_p")==null? "false":detailsMap.get("xd_enable_p"))+"</x_dragging>\n"+
								"<y_position>"+(detailsMap.get("y_pos_p")==null? "92":detailsMap.get("y_pos_p"))+"</y_position>\n"+
								"<y_dragging>"+(detailsMap.get("yd_enable_p")==null? "true":detailsMap.get("yd_enable_p"))+"</y_dragging>\n"+
								"<height>"+(detailsMap.get("bottom_pos_p")==null? "-43":detailsMap.get("bottom_pos_p"))+"</height>\n"+
								"<width>"+(detailsMap.get("width_p")==null? "100":detailsMap.get("width_p"))+"</width>\n"+
								"<font_size>"+(detailsMap.get("cellheight_p")==null? "3":detailsMap.get("cellheight_p"))+"</font_size>\n"+
								"<column_settings>\n"+
								"<type_width>"+(detailsMap.get("type_width_p")==null? "0":detailsMap.get("type_width_p"))+"</type_width>\n"+
								"<id_width>"+(detailsMap.get("id_width_p")==null? "15":detailsMap.get("id_width_p"))+"</id_width>\n"+
								"<cmd_width>"+(detailsMap.get("command_width_p")==null? "0":detailsMap.get("command_width_p"))+"</cmd_width>\n"+
								"<time_width>"+(detailsMap.get("time_width_p")==null? "0":detailsMap.get("time_width_p"))+"</time_width>\n"+
								"<score_width>"+(detailsMap.get("score_width_p")==null? "0":detailsMap.get("score_width_p"))+"</score_width>\n"+
								"<num_width>"+(detailsMap.get("num_width_p")==null? "15":detailsMap.get("num_width_p"))+"</num_width>\n"+
								"<comment_width>"+(detailsMap.get("comment_width_p")==null? "70":detailsMap.get("comment_width_p"))+"</comment_width>\n"+
								"</column_settings>\n"+
								"</portlayt_settings>\n"+
								"<landscape_settings>\n"+
								"<player_position>"+(detailsMap.get("player_pos_l")==null? "0":detailsMap.get("player_pos_l"))+"</player_position>\n"+
								"<x_position>"+(detailsMap.get("x_pos_l")==null? "0":detailsMap.get("x_pos_l"))+"</x_position>\n"+
								"<x_dragging>"+(detailsMap.get("xd_enable_l")==null? "false":detailsMap.get("xd_enable_l"))+"</x_dragging>\n"+
								"<y_position>"+(detailsMap.get("y_pos_l")==null? "92":detailsMap.get("y_pos_l"))+"</y_position>\n"+
								"<y_dragging>"+(detailsMap.get("yd_enable_l")==null? "true":detailsMap.get("yd_enable_l"))+"</y_dragging>\n"+
								"<height>"+(detailsMap.get("bottom_pos_l")==null? "-43":detailsMap.get("bottom_pos_l"))+"</height>\n"+
								"<width>"+(detailsMap.get("width_l")==null? "40":detailsMap.get("width_l"))+"</width>\n"+
								"<font_size>"+(detailsMap.get("cellheight_l")==null? "3":detailsMap.get("cellheight_l"))+"</font_size>\n"+
								"<column_settings>\n"+
								"<type_width>"+(detailsMap.get("type_width_l")==null? "0":detailsMap.get("type_width_l"))+"</type_width>\n"+
								"<id_width>"+(detailsMap.get("id_width_l")==null? "0":detailsMap.get("id_width_l"))+"</id_width>\n"+
								"<cmd_width>"+(detailsMap.get("command_width_l")==null? "0":detailsMap.get("command_width_l"))+"</cmd_width>\n"+
								"<time_width>"+(detailsMap.get("time_width_l")==null? "0":detailsMap.get("time_width_l"))+"</time_width>\n"+
								"<score_width>"+(detailsMap.get("score_width_l")==null? "0":detailsMap.get("score_width_l"))+"</score_width>\n"+
								"<num_width>"+(detailsMap.get("num_width_l")==null? "15":detailsMap.get("num_width_l"))+"</num_width>\n"+
								"<comment_width>"+(detailsMap.get("comment_width_l")==null? "70":detailsMap.get("comment_width_l"))+"</comment_width>\n"+
								"</column_settings>\n"+
								"</landscape_settings>\n"+
								"</Settings>";

				FileOutputStream fos = new FileOutputStream(filepath);
				fos.write(xml.getBytes());
				fos.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				return -3;
			} catch (IOException e) {
				e.printStackTrace();
				return -3;
			}

			return 0;
		}

		@Override
		protected void onPostExecute(Integer arg){
			 if(PrimitiveSetting.getACT()!= null){
				 context = PrimitiveSetting.getACT();
			 }else{//UNKNOWNエラー
				 }
			switch(arg){
			case 0:
				 new CloseOnlyDialog(PrimitiveSetting.getACT(),"設定値をエクスポートしました").show();
				break;
			case -1:
				 new CloseOnlyDialog(PrimitiveSetting.getACT(),"現在の設定値の取得に失敗しました").show();
				break;
			case -2:
				 new CloseOnlyDialog(PrimitiveSetting.getACT(),"SDカード(ユーザメモリ)へのアクセスに失敗しました").show();
				break;
			case -3:
				 new CloseOnlyDialog(PrimitiveSetting.getACT(),"ファイルのIOに失敗しました").show();
				break;
			}
		}
	 }



	 class SettingImport extends AsyncTask<Void,Void,ArrayList<String>>{
		@Override
		protected ArrayList<String> doInBackground(Void... arg0) {
			//ファイルから設定値を読み込む
			//一時マップにXMLの全タグを読み込む
			HashMap<String,String> map = null;
			ArrayList<String> missedStrs = new ArrayList<String>();
				try {
					String filepath = getStorageFilePath();
					if(filepath == null){
						missedStrs.add("wrong1");
						return missedStrs;
					}
					FileInputStream fis = new FileInputStream(filepath);
					byte[] readBytes = new byte[fis.available()];
					fis.read(readBytes);
					try {
						map = XMLparser.setSettingValues(readBytes);//ここで値をmapにセット
					} catch (XmlPullParserException e) {
						e.printStackTrace();
						missedStrs.add("wrong2");
						fis.close();
						return missedStrs;
					}
					fis.close();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
					missedStrs.add("wrong2");
					return missedStrs;
				} catch (IOException e) {
					e.printStackTrace();
					missedStrs.add("wrong2");
					return missedStrs;
				}

				//値のチェック-----------------------------------
				//fexitとfinish_backとat_enableとat_overwrite以外はDetailsMap
				Context context = null;
				 if(PrimitiveSetting.getACT()!= null){
					 context = PrimitiveSetting.getACT();
				 }else{//UNKNOWNエラー
					 }
				NLiveRoid app = (NLiveRoid)context.getApplicationContext();
				missedStrs = app.checkSettingValue(missedStrs, map);//ここでDetailsMapにセットされる
				if(missedStrs.size() == 0)missedStrs.add("0");
					return missedStrs;
		}

	@Override
	protected void onPostExecute(ArrayList<String> arg){
		Context context = null;
		 if(PrimitiveSetting.getACT()!= null){
		if(arg == null){
			 new CloseOnlyDialog(PrimitiveSetting.getACT(),"設定ファイルのインポートに失敗しました:CODE 004").show();
			return;
		}else{
			if(arg.size() == 1&&arg.get(0).equals("wrong1")){
				 new CloseOnlyDialog(PrimitiveSetting.getACT(),"設定ファイルのIOに失敗しました\nSDカードをお確かめ下さい:CODE 005").show();
			return;
			}else if(arg.size() == 1 &&arg.get(0).equals("wrong2")){
				 new CloseOnlyDialog(PrimitiveSetting.getACT(),"設定ファイルXMLのパースに失敗しました:CODE 006").show();
			return;
			}else if(arg.size() == 1 &&arg.get(0).equals("0")){
				 new CloseOnlyDialog(PrimitiveSetting.getACT(),"設定値をインポートしました").show();
				 PrimitiveSetting.getACT().loadSettings();
			return;
			}

			String missedStr = "";
			for(int i = 0; i < arg.size() ;i++){
				missedStr += arg.get(i) + " ";
			}
			MyToast.customToastShow(context, "次の値が読み取れませんでした\n"+missedStr);
		}
		 }
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
//				MyToast.customToastShow(primitiveSetting, "SDカードが利用できませんでした\nコテハンは機能できません");
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
//				MyToast.customToastShow(primitiveSetting, "SDカードが利用できませんでした\nコテハンは機能できません");
				return null;
			}


			//sdcard直下に、パッケージ名のフォルダを作りファイルを生成
			String filePath = Environment.getExternalStorageDirectory().toString() + "/NLiveRoid";

			File directory = new File(filePath);
			if(directory.mkdirs()){//すでにあった場合も失敗する
				Log.d("NLiveRoid","mkdir");
			}
			File file = new File(filePath,fileName);
			if(!file.exists()){
				try {
					file.createNewFile();
			 		new SettingExport().execute();//次からの読み込みがエラーしないように今の値でのファイルを作っておく
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			return file.getPath();
		}
	}
