package nliveroid.nlr.main;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;


//Manifestでプロセスがflash_proccessになっているのが気になるが。。
public class TransDiscr extends Activity implements Archiver{
	private Gate gate;
	private static TransDiscr ACT;
	@Override
	public void onCreate(Bundle bundle){
		super.onCreate(bundle);
		requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
		ACT = this;
		Intent getIntent = getIntent();
		byte mode = getIntent.getByteExtra("init_mode", (byte)-1);
		if(NLiveRoid.isDebugMode)Log.d("NLiveRoid"," Trans onCreate" + mode);
		if(mode == -1){
			this.finish();
		}else{
		if(mode == 0){//通常のFlashPlayerから呼ばれた
			int orientation = getIntent.getIntExtra("orientation", ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
			setRequestedOrientation(orientation);
			NLiveRoid app = (NLiveRoid)getApplicationContext();//シンプルじゃない、嫌い
			if(app == null||app.getGateView() == null||(FlashPlayer.getACT() == null && BCPlayer.getBCACT() == null)){
				if(NLiveRoid.isDebugMode)Log.d("NLiveRoid"," Trans Failed" + app +" " + FlashPlayer.getACT() + " " + BCPlayer.getBCACT());
				Intent data = new Intent();
				data.putExtra("error", true);
				data.putExtra("error_message", "読み込み中です");
				this.setResult(CODE.RESULT_TRANS_LAYER,data);
				this.finish();
				return;
			}else{
			if(FlashPlayer.getACT() != null){
				gate = new Gate(this,app.getGateView(),FlashPlayer.getACT().getLiveInfo(),true,getIntent().getStringExtra("Cookie"),getIntent().getStringExtra("twitterToken"));
			}else if(BCPlayer.getBCACT() != null){
				gate = new Gate(this,app.getGateView(),BCPlayer.getBCACT().getLiveInfo(),true,getIntent().getStringExtra("Cookie"),getIntent().getStringExtra("twitterToken"));
			}
			if(gate == null){
				Intent data = new Intent();
				data.putExtra("error", true);
				data.putExtra("error_message", "読み込み中です");
				this.setResult(CODE.RESULT_TRANS_LAYER,data);
				this.finish();
				return;
			}
			gate.show(this.getResources().getConfiguration());
			this.setContentView(app.getGateView().getView());
			Log.d("NLiveRoid"," TransDescr END onCreate");
			}
		}
		}

	}

	public static TransDiscr getACT(){
		return ACT;
	}

	@Override
	public void finish(){
		if(gate != null){
			gate.close_noanimation();
		}
		super.finish();
	}
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
	    super.onConfigurationChanged(newConfig);
	    if(gate != null &&  gate.isOpened()){
			gate.onConfigChanged(newConfig);
		}
	}

	/**
	 * アプリケーション選択で終了するように
	 */
	@Override
	public void onUserLeaveHint(){
		super.onUserLeaveHint();
		finish();
	}

	public void forTagSearch(String string) {
			Intent data = new Intent();
			data.putExtra("init_mode", (byte)1);
			data.putExtra("tagword", string);
			this.setResult(CODE.RESULT_TRANS_LAYER,data);
			finish();
	}

	@Override
	public void allCommFunction(int index, LiveInfo info) {
		if(info.getLiveID() == null ){
			MyToast.customToastShow(ACT, "放送IDがnullでした(不明のエラー)");
			return;
		}
		Intent data = new Intent();
		data.putExtra("init_mode", (byte)1);
		data.putExtra("archive", info.getLiveID());
		this.setResult(CODE.RESULT_TRANS_LAYER,data);
		finish();
	}
}
