package nliveroid.nlr.main;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;

public class RecognizeDummy extends Activity{
	private static RecognizeDummy ACT;
	@Override
	public void onCreate(Bundle bundle){
		super.onCreate(bundle);
		ACT = this;
		//透明にする
		try {
			// 音声をテキストにしてeditTexにセットする
			Intent intent = new Intent(
					RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
			intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
					RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
			intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "検索");
			intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
			// インテント発行
			startActivityForResult(intent, CODE.RESULT_RECOGNIZE_SPEECH);
		} catch (ActivityNotFoundException e) {
			// このインテントに応答できるアクティビティがインストールされていない場合
			MyToast.customToastShow(this, "音声認識に対応していないようです");
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	@Override
	public void onNewIntent(Intent intent){
		super.onNewIntent(intent);
		ACT = this;
		//透明にする
		try {
			// 音声をテキストにしてeditTexにセットする
			Intent intent1 = new Intent(
					RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
			intent1.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
					RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
			intent1.putExtra(RecognizerIntent.EXTRA_PROMPT, "検索");
			intent1.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
			// インテント発行
			startActivityForResult(intent1, CODE.RESULT_RECOGNIZE_SPEECH);
		} catch (ActivityNotFoundException e) {
			// このインテントに応答できるアクティビティがインストールされていない場合
			MyToast.customToastShow(this, "音声認識に対応していないようです");
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//		Log.d("log","RESULT RECOG SEARCH ------------ " + requestCode + " " + resultCode +" " + data);
	if (requestCode == CODE.RESULT_RECOGNIZE_SPEECH) {
		// 音声認識から
		// 結果文字列リスト
		if (data != null) {
			ArrayList<String> results = data
					.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
			if(results == null || results.size() == 0)this.finish();
			 final String[] candidate = new String[results.size()];
			 for (int i = 0; i< results.size(); i++) {
			 // ここでは、候補がいくつか格納されてくるので結合しています
			 candidate[i] = results.get(i);
			 }
			 //候補をアラート表示
			 new AlertDialog.Builder(this)
				.setItems(candidate,
						new DialogInterface.OnClickListener() {
							public void onClick(
									DialogInterface dialog,
									int which) {
								//音声検索
								if(SearchTab.getSearchTab() != null){
								SearchTab.getSearchTab().startRecognizeSearch(candidate[which]);
								}
								ACT.finish();
							}
				})
				.setOnCancelListener(new DialogInterface.OnCancelListener() {

					@Override
					public void onCancel(DialogInterface dialog) {
						ACT.finish();
					}
				})
				.create().show();
		}else{//data == null
			this.finish();
		}
	}
	}

	public static RecognizeDummy getACT() {
		return ACT;
	}
}
