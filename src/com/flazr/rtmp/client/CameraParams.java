package com.flazr.rtmp.client;

import java.util.Collections;
import java.util.List;

import nliveroid.nlr.main.BCPlayer;
import nliveroid.nlr.main.LiveSettings;
import nliveroid.nlr.main.MyToast;
import nliveroid.nlr.main.NLiveRoid;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.util.Log;

public class CameraParams{

	private BCPlayer player;

	private LiveSettings liveSetting;

	private int zoomValue;
	private Parameters parameters;
	private int sceneIndex;

	private List<String> sceneModes;
	private List<String> flashModes;
	private int flashIndex;
	private List<String> whiteblModes;
	private int whiteblIndex;
	private List<String> colorEffects;
	private int coloreIndex;
	private List<String> antiList;
	private int antiIndex;

	private boolean orientation90;

	private Camera mCam;

	public CameraParams(BCPlayer context,LiveSettings liveSetting){
		this.player = context;
		this.liveSetting = liveSetting;
	}

	public int init(Camera cam) {
		this.mCam = cam;
        updateResolutionParams(mCam);//解像度を取得する

        //FPSの初期化(計算に使うだけでセットはできない)
        //どっちか設定しただけで落ちる
        //parameters.setPreviewFpsRange(minRange[0], minRange[1]);
        //parameters.setPreviewFrameRate(minRange[1]);
//        Log.d("NLR","CAM framerate------:"+mCam.getParameters().getPreviewFrameRate());


        //各カメラ機能の初期化
        //ズーム、、シーンモード
        liveSetting.setZoomSupported(parameters.isZoomSupported());
        sceneModes = parameters.getSupportedSceneModes();//サポートしてなきゃnull
        flashModes = parameters.getSupportedFlashModes();
        whiteblModes = parameters.getSupportedWhiteBalance();
        colorEffects = parameters.getSupportedColorEffects();
        antiList = parameters.getSupportedAntibanding();
        if(sceneModes !=null)liveSetting.setSupportedSceneMode(true);
        if(flashModes !=null)liveSetting.setSupportedFlashMode(true);
        if(whiteblModes !=null)liveSetting.setSupportedWhiteblMode(true);
        if(colorEffects !=null)liveSetting.setSupoprtedColorEeffects(true);
        if(antiList !=null)liveSetting.setSupportedAntiB(true);

        //設定値をセットする
        try{
        	mCam.setParameters(parameters);
        }catch(Exception e){
        	e.printStackTrace();
        	return -4;
        }
		return 0;
	}
	/**
	 * 解像度変更処理
	 * @return
	 */
	/**
	 * @return
	 */
	public int updateResolutionParams(Camera mCam) {
		try{
			 //解像度の設定
			if(mCam == null)return -1;
			if(parameters == null)parameters = mCam.getParameters();
	        if(liveSetting.getResolutionList() == null)initResolutionList();//リスト昇順で取得
	        //現在の解像度決定
			final Rect nowSize = liveSetting.getNowActualResolution();

			Log.d("RealTimeCam","updateRes ---------------------- ");
			Log.d("RealTimeCam","updateRes width "+nowSize.right + " bo " + nowSize.bottom + " left " + nowSize.left);

			//プレビューが歪まないアス比を計算する(縦時は縦長になる)
//	        Log.d("RealTimeCam","Angle " + parameters.getVerticalViewAngle() +"   " + parameters.getHorizontalViewAngle());

			float ratio = parameters.getVerticalViewAngle()/parameters.getHorizontalViewAngle();
	        liveSetting.setViewAngleRatio(ratio);
		        //【縦が長いアス比はカメラに設定できないことに注意】
	        	if(liveSetting.isPortLayt()){
	        	ratio = (float)	nowSize.bottom/(float)nowSize.right;
	        	parameters.setPreviewSize(nowSize.bottom, nowSize.right);
	        	}else{
	        	ratio = (float)nowSize.right/(float)nowSize.bottom;
	        	parameters.setPreviewSize(nowSize.right, nowSize.bottom);
	        	Log.d("NLiveRoid","getPreviewSize " + parameters.getPreviewSize().width + " " + parameters.getPreviewSize().height);
	        	}
//	        Log.d("RealTimeCam","RatioVal " + parameters.getVerticalViewAngle()/parameters.getHorizontalViewAngle() +"  " + ratio);
	        liveSetting.setRatio(ratio);
	        liveSetting.culclateRatio();
	        if(liveSetting.isPortLayt()){
	        	if(NLiveRoid.apiLevel >=8){
	        		mCam.setDisplayOrientation(90);//これがAPILevel8以降!!!!
	        	}else{
	        		liveSetting.setPortLayt(false);
	        	}
	        	orientation90 = true;
	        }
		}catch(Exception e){
			e.printStackTrace();
			return -1;
		}
		return 0;
	}


	private void initResolutionList(){
        List<Size> sizes = parameters.getSupportedPreviewSizes();//これがAPILevel8以降!!!!
        Collections.sort(sizes, new Ascending());//昇順に並べ替える
        liveSetting.setResolutionList(sizes);
        for(Size i:sizes){
        	Log.d("CameraParams", "initResolutionList Size :" + i.width + "  " + i.height);
        }
	}
	class Ascending implements java.util.Comparator {
		public int compare(Object s, Object t) {
//			Log.d("RealTimeCam","Comparator:" + ((Size)s).width + "  " + ((Size)t).width);
			return ((Size)s).width < ((Size)t).width? -1:0;
		}
	}


	public void focus() {
		try{
		if(mCam != null)mCam.autoFocus(null);
		}catch(RuntimeException e){
			e.printStackTrace();
		}
	}
	public void zoomUp() {
//		Log.d("NLR","ZOOM"+parameters.getZoom());
		if(liveSetting != null && liveSetting.isZoomSupported()&&zoomValue < parameters.getMaxZoom()){
			parameters.setZoom(++zoomValue);
			try{
				if(mCam != null)mCam.setParameters(parameters);
				}catch(RuntimeException e){
					e.printStackTrace();
					player.runOnUiThread(new Runnable(){
						@Override
						public void run() {
							MyToast.customToastShow(player, "ズームがサポートされていませんでした");
						}
					});
				}
		}
	}
	public void zoomDown() {
//		Log.d("NLR","ZOOMD"+parameters.getZoom());
		if(liveSetting != null && liveSetting.isZoomSupported()&&zoomValue>=0){
			parameters.setZoom(--zoomValue);
			try{
			if(mCam != null)mCam.setParameters(parameters);
			}catch(RuntimeException e){
				e.printStackTrace();
				player.runOnUiThread(new Runnable(){
					@Override
					public void run() {
						MyToast.customToastShow(player, "ズームがサポートされていませんでした");
					}
				});
			}
		}
	}
	public void changeScene(int index) {
		if(liveSetting != null && liveSetting.isSupoprtedSceneMode()){
			sceneIndex = index;
			parameters.setSceneMode(sceneModes.get(sceneIndex));
			if(mCam != null)mCam.setParameters(parameters);
			liveSetting.setSceneModeIndex(sceneIndex);
		}
	}
	public List<String> getSceneList(){
		return sceneModes;
	}
	public void changeFlash(int index) {
		if(liveSetting != null && liveSetting.isSupoprtedFlashMode()){
			flashIndex = index;
			parameters.setFlashMode(flashModes.get(flashIndex));
			if(mCam != null)mCam.setParameters(parameters);//LiveSettingsに保存はしない
		}
	}

	public List<String> getFlashModes() {
	    return flashModes;
	}

	public void changeWhitebl(int index) {
		if(liveSetting != null && liveSetting.isSupoprtedWhiteblMode()){
			whiteblIndex = index;
			parameters.setWhiteBalance(whiteblModes.get(whiteblIndex));
			if(mCam != null)mCam.setParameters(parameters);//LiveSettingsに保存はしない
		}
	}
	public List<String> getWhiteBlModes() {
		return whiteblModes;
	}

	public void changeColorEffect(int index) {
		if(liveSetting != null && liveSetting.isSupoprtedColorEeffects()){
			coloreIndex = index;
			parameters.setColorEffect(colorEffects.get(coloreIndex));
			if(mCam != null)mCam.setParameters(parameters);//LiveSettingsに保存はしない
		}
	}
	public List<String> getColorEffects() {
		return colorEffects;
	}
	public void changeAntiB(int index) {
		if(liveSetting != null && liveSetting.isSupoprtedAntiB()){
			antiIndex = index;
			parameters.setAntibanding(antiList.get(antiIndex));
			if(mCam != null)mCam.setParameters(parameters);//LiveSettingsに保存はしない
		}
	}
	public List<String> getAntibList() {
		return antiList;
	}



	/**
	 * orientation90を取得します。
	 * @return orientation90
	 */
	public boolean isOrientation90() {
	    return orientation90;
	}

	public void orientation(boolean portlayt) {
		if(portlayt&&!orientation90){
			mCam.setDisplayOrientation(90);
			orientation90 = true;
		}else if(!portlayt&&orientation90){
			mCam.setDisplayOrientation(0);
			orientation90 = false;
		}
	}


}

/*
 * V/QCameraHWI(14214):  newFrame =0x41750b1c, frm_type = 1

V/QCameraHWI(14214):  newFrame =0x41750d78, frm_type = 1

D/dalvikvm(24219): GC_BEFORE_OOM freed 0K, 3% free 64184K/65543K, paused 60ms

E/dalvikvm-heap(24219): Out of memory on a 259216-byte allocation.

I/dalvikvm(24219): "Binder Thread #1" prio=5 tid=9 RUNNABLE

I/dalvikvm(24219):   | group="main" sCount=0 dsCount=0 obj=0x417d4fb0 self=0x17c73a8

I/dalvikvm(24219):   | sysTid=24230 nice=0 sched=0/0 cgrp=default handle=25233584

I/dalvikvm(24219):   | schedstat=( 0 0 0 ) utm=49 stm=88 core=0

I/dalvikvm(24219):   at dalvik.system.NativeStart.run(Native Method)

I/dalvikvm(24219):

E/Camera-JNI(24219): Couldn't allocate byte array for JPEG data

V/CameraService(14214): enableMsgType(0)

I/QCameraHWI(14214): enableMsgType: E, msgType =0x10

I/QCameraHWI(14214): enableMsgType: X, msgType =0x10, mMsgEnabled=0x1d
*/