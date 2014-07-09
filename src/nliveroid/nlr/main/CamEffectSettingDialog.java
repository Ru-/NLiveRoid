package nliveroid.nlr.main;

import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;

import com.flazr.rtmp.client.CameraParams;

public class CamEffectSettingDialog extends AlertDialog.Builder{
	private AlertDialog me;

	public CamEffectSettingDialog(final BCPlayer player, int height,final CameraParams rCam) {
		super(player);
		setTitle("カメラ拡張設定");
		//シーンモード フラッシュ ホワイトバランス カラーエフェクト アンチバンディング
		//ズーム
		View parent = LayoutInflater.from(player).inflate(R.layout.camparams_dialog, null);
		Log.d("CamSetingD","VIEWH" + height);
		parent.setLayoutParams(new LinearLayout.LayoutParams(-1,height));
		setView(parent);
		me = this.create();
		Window w = me.getWindow();
		w.setFlags(0, WindowManager.LayoutParams.FLAG_DIM_BEHIND);
		WindowManager.LayoutParams wmlp= w.getAttributes();
		wmlp.gravity=Gravity.BOTTOM;
//	    wmlp.y=height;
		wmlp.height = height;
		w.setAttributes(wmlp);

		final Button scene = (Button)parent.findViewById(R.id.cam_params_scene_bt);
		scene.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				List<String> sceneList = rCam.getSceneList();
				if(sceneList == null||sceneList.size() < 1){
					MyToast.customToastShow(player, "サポートしていませんでした");
					return;
				}
				String[] scenes = new String[sceneList.size()];
				for(int i = 0; i < sceneList.size(); i++){
					scenes[i] = sceneList.get(i);
				}
				new AlertDialog.Builder(player)
				.setItems(scenes, new DialogInterface.OnClickListener() {
									public void onClick(
											DialogInterface dialog,
											int which) {
										rCam.changeScene(which);
										}
				}).create().show();
									}
		});
		final Button flash = (Button)parent.findViewById(R.id.cam_params_flash_bt);
		flash.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				List<String> flashList = rCam.getFlashModes();
				if(flashList == null||flashList.size() < 1){
					MyToast.customToastShow(player, "サポートしていませんでした");
					return;
				}
				String[] flashs = new String[flashList.size()];
				for(int i = 0; i < flashList.size(); i++){
					flashs[i] = flashList.get(i);
				}
				new AlertDialog.Builder(player)
				.setItems(flashs, new DialogInterface.OnClickListener() {
									public void onClick(
											DialogInterface dialog,
											int which) {
										rCam.changeFlash(which);
										}
				}).create().show();
									}
		});
		final Button whitebl = (Button)parent.findViewById(R.id.cam_params_whitebalance_bt);
		whitebl.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				List<String> whiteblList = rCam.getWhiteBlModes();
				if(whiteblList == null||whiteblList.size() < 1){
					MyToast.customToastShow(player, "サポートしていませんでした");
					return;
				}
				String[] whitebls = new String[whiteblList.size()];
				for(int i = 0; i < whiteblList.size(); i++){
					whitebls[i] = whiteblList.get(i);
				}
				new AlertDialog.Builder(player)
				.setItems(whitebls, new DialogInterface.OnClickListener() {
									public void onClick(
											DialogInterface dialog,
											int which) {
										rCam.changeWhitebl(which);
										}
				}).create().show();
									}
		});
		final Button coloreffect = (Button)parent.findViewById(R.id.cam_params_coloreffect_bt);
		coloreffect.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				List<String> coloreList = rCam.getColorEffects();
				if(coloreList == null||coloreList.size() < 1){
					MyToast.customToastShow(player, "サポートしていませんでした");
					return;
				}
				String[] colors = new String[coloreList.size()];
				for(int i = 0; i < coloreList.size(); i++){
					colors[i] = coloreList.get(i);
				}
				new AlertDialog.Builder(player)
				.setItems(colors, new DialogInterface.OnClickListener() {
									public void onClick(
											DialogInterface dialog,
											int which) {
										rCam.changeColorEffect(which);
										}
				}).create().show();
									}
		});
		final Button antibanding = (Button)parent.findViewById(R.id.cam_params_anti_bt);
		antibanding.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				List<String> antiList = rCam.getAntibList();
				if(antiList == null||antiList.size() < 1){
					MyToast.customToastShow(player, "サポートしていませんでした");
					return;
				}
				String[] antis = new String[antiList.size()];
				for(int i = 0; i < antiList.size(); i++){
					antis[i] = antiList.get(i);
				}
				new AlertDialog.Builder(player)
				.setItems(antis, new DialogInterface.OnClickListener() {
									public void onClick(
											DialogInterface dialog,
											int which) {
										rCam.changeAntiB(which);
										}
				}).create().show();
									}
		});
	}


	public void showSelf(){
		me.show();
	}

}
