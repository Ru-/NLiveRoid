package nliveroid.nlr.main;

import android.app.Activity;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class SoundSelectActivity extends Activity{

	private Activity act;
	private NumberPicker np;
	private TextView tv;
	private int defaultNum;
	private String key;

	@Override
	public void onCreate(Bundle bundle){
		super.onCreate(bundle);
		 Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
         startActivityForResult(intent, CODE.SOUND_SELECT);
	}


	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
	 if(requestCode == CODE.SOUND_SELECT &&data != null){
                Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                Ringtone tone = RingtoneManager.getRingtone(this, uri);
                Log.d("log","TONE  " + tone);
                if(tone != null && uri != null){
                RingtoneManager.getRingtone(act, uri);
                //サウンドのキーにURIを保存
                PrimitiveSetting.getACT().preferenceChangedExt("alert_sound_uri",uri.toString());
                }
        }
        this.finish();
    }




}
