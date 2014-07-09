package nliveroid.nlr.main;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.Spinner;

public class SpeechSettingDialog extends AlertDialog.Builder{
	private AlertDialog me;
	public SpeechSettingDialog(final CommentPostable postable,final CommentTable comTable) {
		super((Context)postable);
		Object[] speechStatus = comTable.getSpeechStatus();
		//OKで値をセットするので動的に変える必要はない
		View parent = LayoutInflater.from((Context)postable).inflate(R.layout.speech_setting_dialog, null);
		setView(parent);
		final CheckBox enable = (CheckBox)parent.findViewById(R.id.speech_set_on);
		final SeekBar speed = (SeekBar)parent.findViewById(R.id.speech_dialog_speed_seek);
		speed.setProgress(Integer.parseInt(String.valueOf(speechStatus[0])));
		final SeekBar pich = (SeekBar)parent.findViewById(R.id.speech_dialog_pich_seek);
		pich.setProgress(Integer.parseInt(String.valueOf(speechStatus[1])));
		final SeekBar vol = (SeekBar)parent.findViewById(R.id.speech_dialog_vol_seek);
		vol.setProgress(Integer.parseInt(String.valueOf(speechStatus[3])));

//		CheckBox education_enable = (CheckBox)findViewById(R.id.speech_set_education);
//		education_enable.setChecked(comTable.getSpeechEducation());

		//Aquesの時の声色
		ArrayAdapter adapter = ArrayAdapter.createFromResource((Context)postable, R.array.speech_phont,
			android.R.layout.simple_spinner_item);
	adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

	final Spinner phontSpinner = (Spinner)parent.findViewById(R.id.speech_dialog_phont_spinner);
				phontSpinner.setAdapter(adapter);
				final byte enableValue = comTable.isSpeechEnable();
				if(enableValue == 1){
					enable.setChecked(true);
					phontSpinner.setEnabled(false);
					vol.setEnabled(false);
				}else if(enableValue==3){
					enable.setChecked(true);
					pich.setEnabled(false);
					//speed,volはデフォルトがtrue
				}else{
					enable.setChecked(false);
					phontSpinner.setEnabled(false);
					vol.setEnabled(false);
				}
				phontSpinner.setSelection(Integer.parseInt(String.valueOf(speechStatus[4])));

		Button ok = (Button)parent.findViewById(R.id.speech_ok_button);
		Log.d("Log","VAL - " + Integer.parseInt(String.valueOf(speechStatus[0])));
		ok.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				me.cancel();
				if(enableValue == 0||enableValue == 1){//TTSでの話しだったら
					if(enable.isChecked()){
					comTable.updateSpeechSetting(postable.getAPPContext(),(byte)1,(byte)speed.getProgress() ,(byte)vol.getProgress(),(byte)pich.getProgress());
					postable.setSpeachSettingValue((byte)1,(byte)speed.getProgress() ,(byte)vol.getProgress(),(byte)pich.getProgress() );
					}else{
						comTable.updateSpeechSetting(postable.getAPPContext(),(byte)0,(byte)speed.getProgress(),(byte)vol.getProgress() ,(byte)pich.getProgress());
						postable.setSpeachSettingValue((byte)0,(byte)speed.getProgress() ,(byte)vol.getProgress(),(byte)pich.getProgress() );
					}
				}else if(enableValue == 2||enableValue==3){
					if(enable.isChecked()){
						//pichはなくphont
				comTable.updateSpeechSetting(postable.getAPPContext(),(byte)3,(byte)speed.getProgress(),(byte)vol.getProgress() ,(byte)phontSpinner.getSelectedItemPosition());
				postable.setSpeachSettingValue((byte)3,(byte)speed.getProgress(),(byte)vol.getProgress() ,(byte)phontSpinner.getSelectedItemPosition() );
					}else{
						comTable.updateSpeechSetting(postable.getAPPContext(),(byte)2,(byte)speed.getProgress(),(byte)vol.getProgress() ,(byte)phontSpinner.getSelectedItemPosition());
						postable.setSpeachSettingValue((byte)2,(byte)speed.getProgress() ,(byte)vol.getProgress(),(byte)phontSpinner.getSelectedItemPosition() );
					}
				}
			}
		});
		Button cancel = (Button)parent.findViewById(R.id.speech_cancel_button);
		cancel.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				me.cancel();
			}
		});
	}
	
	public void showSelf(){
		this.create();
		me = this.show();
	}

}

