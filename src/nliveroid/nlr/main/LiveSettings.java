/*
 * Flazr <http://flazr.com> Copyright (C) 2009  Peter Thomas.
 *
 * This file is part of Flazr.
 *
 * Flazr is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Flazr is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Flazr.  If not, see <http://www.gnu.org/licenses/>.
 */

package nliveroid.nlr.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.CommandLine;
import org.apache.commons.CommandLineParser;
import org.apache.commons.GnuParser;
import org.apache.commons.HelpFormatter;
import org.apache.commons.Option;
import org.apache.commons.OptionBuilder;
import org.apache.commons.Options;
import org.apache.commons.Parser;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.hardware.Camera.Size;
import android.net.Uri;
import android.util.Log;

import com.flazr.rtmp.RtmpHandshake;
import com.flazr.rtmp.RtmpWriter;
import com.flazr.util.Utils;

public class LiveSettings implements Serializable{

	private static final long serialVersionUID =-7674852638292555620L;
    private String host = "localhost";
    private int port = 1935;
    private String appName = "vod";
    private String streamName;
    private String flvFilePath;
    private RtmpWriter writerToSave;
    private String chachePathForRecord;
    private Map<String, Object> params;
    private Object[] args;
    private byte[] clientVersion;
    //最初から再生
    private int start = -2;
    private int length = -1;
    private int buffer = 4096;
    private byte[] swfHash;
    private int swfSize;
    private int load = 1;
    private int loop = 1;
    private int threads = 10;
    private List<LiveSettings> clientOptionsList;
    //追加の設定値
    private int user_fps = 2;
    private boolean isUseMic;
    private boolean isUseCam;
    private int v_frame_rate = 10;
    private int v_bit_rate = 336000;
    private int keyframe_interval = 36;
	private int sample_rate = 44100;
	private boolean isPortLayt = false;
	private float ratio;
	private boolean zoomSupported;
	private boolean isSupportedSceneMode;
	private boolean isEncodeStarted;
	private boolean isStreamStarted;
	private int a_bit_rate = 64000;
    private int a_frame_rate = 10;
	private List<Size> resolutionList;
    private int resolutionIndex = 0;//必ず昇順に並べること
	private boolean isBackGroundCam;
	private boolean isBackGroundMic;
	private boolean isRingCamEnable;
	private boolean isRingMicEnable;
	private int MODE = 2;//デフォ値は画像配信
	private float viewAngleRatio = 0;
	private int sceneIndex;
	private boolean isSupportedFlashMode;
	private boolean isSupportedWhiteblMode;
	private boolean isSupportedColorEffects;
	private boolean isSupportedAntib;
	private Uri bmpUri;
	private Rect bmpRect;
	private Bitmap bmp;
//	private boolean isStereo;
	private float volume = 1;



    public LiveSettings(String[] args){
    	if(args  != null)this.parseCommand(args);
    }
	private static final Pattern URL_PATTERN = Pattern.compile(
          "(rtmp.?)://" // 1) protocol
        + "([^/:]+)(:[0-9]+)?/" // 2) host 3) port
        + "([^/]+)/" // 4) app
        + "(.*)" // 5) play
    );

    public void parseUrl(String url) {
        Matcher matcher = URL_PATTERN.matcher(url);
        host = matcher.group(2);
        String portString = matcher.group(3);
            portString = portString.substring(1); // skip the ':'
        port = portString == null ? 1935 : Integer.parseInt(portString);
        appName = matcher.group(4);
        streamName = matcher.group(5);
    }

    //==========================================================================
    /**
     * コマンドのパース
     * @return
     */
    protected static Options getCliOptions() {
        final Options options = new Options();
        options.addOption(new Option("help", "print this message"));
        options.addOption(OptionBuilder.withArgName("host").hasArg()
                .withDescription("host name").create("host"));
        options.addOption(OptionBuilder.withArgName("port").hasArg()
                .withDescription("port number").create("port"));
        options.addOption(OptionBuilder.withArgName("app").hasArg()
                .withDescription("app name").create("app"));
        options.addOption(OptionBuilder
                .withArgName("start").hasArg()
                .withDescription("start position (milliseconds)").create("start"));
        options.addOption(OptionBuilder.withArgName("length").hasArg()
                .withDescription("length (milliseconds)").create("length"));
        options.addOption(OptionBuilder.withArgName("buffer").hasArg()
                .withDescription("buffer duration (milliseconds)").create("buffer"));
        options.addOption(new Option("rtmpe", "use RTMPE (encryption)"));
        options.addOption(new Option("live", "publish local file to server in 'live' mode"));
        options.addOption(new Option("record", "publish local file to server in 'record' mode"));
        options.addOption(new Option("append", "publish local file to server in 'append' mode"));
        options.addOption(OptionBuilder.withArgName("property=value").hasArgs(2)
                .withValueSeparator().withDescription("add / override connection param").create("D"));
        options.addOption(OptionBuilder.withArgName("swf").hasArg()
                .withDescription("path to (decompressed) SWF for verification").create("swf"));
        options.addOption(OptionBuilder.withArgName("version").hasArg()
                .withDescription("client version to use in RTMP handshake (hex)").create("version"));
        options.addOption(OptionBuilder.withArgName("load").hasArg()
                .withDescription("no. of client connections (load testing)").create("load"));
        options.addOption(OptionBuilder.withArgName("loop").hasArg()
                .withDescription("for publish mode, loop count").create("loop"));
        options.addOption(OptionBuilder.withArgName("threads").hasArg()
                .withDescription("for load testing (load) mode, thread pool size").create("threads"));
        options.addOption(new Option("file", "spawn connections listed in file (load testing)"));
        return options;
    }

    public boolean parseCommand(final String[] args) {
        CommandLineParser parser = new GnuParser();
        CommandLine line = null;
        final Options options = getCliOptions();
        try {
            line = ((Parser) parser).parse(options, args,null,false);
            if(line.hasOption("help") || line.getArgs().length == 0) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("client [options] name [saveAs | fileToPublish]"
                        + "\n(name can be stream name, URL or load testing script file)", options);
                return false;
            }
            if(line.hasOption("host")) {
                host = line.getOptionValue("host");
            }
            if(line.hasOption("port")) {
                port = Integer.valueOf(line.getOptionValue("port"));
            }
            if(line.hasOption("app")) {
                appName = line.getOptionValue("app");
            }
            if(line.hasOption("start")) {
                start = Integer.valueOf(line.getOptionValue("start"));
            }
            if(line.hasOption("length")) {
                length = Integer.valueOf(line.getOptionValue("length"));
            }
            if(line.hasOption("buffer")) {
                buffer = Integer.valueOf(line.getOptionValue("buffer"));
            }
            if(line.hasOption("version")) {
                clientVersion = Utils.fromHex(line.getOptionValue("version"));
                if(clientVersion.length != 4) {
                    throw new RuntimeException("client version to use has to be 4 bytes long");
                }
            }
            if(line.hasOption("D")) { // TODO integers, TODO extra args for 'play' command
                params = new HashMap(line.getOptionProperties("D"));
            }
            if(line.hasOption("load")) {
                load = Integer.valueOf(line.getOptionValue("load"));
            }
            if(line.hasOption("threads")) {
                threads = Integer.valueOf(line.getOptionValue("threads"));
            }
            if(line.hasOption("loop")) {
                loop = Integer.valueOf(line.getOptionValue("loop"));
            }
        } catch(Exception e) {
            Log.d("ClientOptions","Parsing failed: " + e.getMessage());
            return false;
        }
        String[] actualArgs = line.getArgs();
        if(line.hasOption("file")) {
            String fileName = actualArgs[0];
            File file = new File(fileName);
            if(!file.exists()) {
                throw new RuntimeException("file does not exist: '" + fileName + "'");
            }
            Log.d("parsing file: {}", ""+file);
            try {
                FileInputStream fis = new FileInputStream(file);
                BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
                int i = 0;
                String s;
                clientOptionsList = new ArrayList<LiveSettings>();
                while ((s = reader.readLine()) != null) {
                    i++;
                    Log.d("parsing line", ""+i +": "+ s);
                    String[] tempArgs = s.split("\\s");
                    LiveSettings tempOptions = new LiveSettings(tempArgs);
                    clientOptionsList.add(tempOptions);
                }
                reader.close();
                fis.close();
            } catch(Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            Matcher matcher = URL_PATTERN.matcher(actualArgs[0]);
            if (matcher.matches()) {
                parseUrl(actualArgs[0]);
            } else {
                streamName = actualArgs[0];
            }
        }
            //ファイルパスをセット
        if(actualArgs.length > 1) {
            chachePathForRecord = actualArgs[1];
        }
        return true;
    }

    //==========================================================================

    public String getTcUrl() {
        return ("rtmp://") + host + ":" + port + "/" + appName;
    }
    public int getLoad() {
        return load;
    }
    public void setLoad(int load) {
        this.load = load;
    }
    public int getLoopCount() {
        return loop;
    }
    public void setLoop(int loop) {
        this.loop = loop;
    }
    public String getFilePath() {
        return flvFilePath;
    }
    public void setFilePath(String path) {
        flvFilePath = path;
    }
    public String getAppName() {
        return appName;
    }
    public void setAppName(String appName) {
        this.appName = appName;
    }
    public void setArgs(Object ... args) {
        this.args = args;
    }
    public Object[] getArgs() {
        return args;
    }

    public void setClientVersionToUse(byte[] clientVersionToUse) {
        this.clientVersion = clientVersionToUse;
    }

    public byte[] getClientVersionToUse() {
        return clientVersion;
    }

    public void initSwfVerification(String pathToLocalSwfFile) {
        initSwfVerification(new File(pathToLocalSwfFile));
    }

    public void initSwfVerification(File localSwfFile) {
        Log.d("initializing swf verification data for: " ,""+ localSwfFile.getAbsolutePath());
        byte[] bytes = Utils.readAsByteArray(localSwfFile,localSwfFile.length());
        byte[] hash = Utils.sha256(bytes, RtmpHandshake.CLIENT_CONST);
        swfSize = bytes.length;
        swfHash = hash;
        Log.d("swf verification initialized - size: {}, hash: {}",""+ swfSize  + " "+ Utils.toHex(swfHash,0,swfHash.length,false));
    }

    public void putParam(String key, Object value) {
        if(params == null) {
            params = new LinkedHashMap<String, Object>();
        }
        params.put(key, value);
    }
    public void setParams(Map<String, Object> params) {
        this.params = params;
    }
    public Map<String, Object> getParams() {
        return params;
    }
    public String getStreamName() {
        return streamName;
    }
    public void setStreamName(String streamName) {
        this.streamName = streamName;
    }
    public int getStart() {
        return start;
    }
    public void setStart(int start) {
        this.start = start;
    }
    public int getLength() {
        return length;
    }
    public void setLength(int length) {
        this.length = length;
    }
    public int getBuffer() {
        return buffer;
    }
    public void setBuffer(int buffer) {
        this.buffer = buffer;
    }
    public String getHost() {
        return host;
    }
    public void setHost(String host) {
    	Log.d("NLiveRoid","SETHOST   " + host);
        this.host = host;
    }
    public int getPort() {
        return port;
    }
    public void setPort(int port) {
        this.port = port;
    }
    public String getSaveAs() {
        return chachePathForRecord;
    }
    public void setSaveAs(String saveAs) {
        this.chachePathForRecord = saveAs;
    }
    public byte[] getSwfHash() {
        return swfHash;
    }
    public void setSwfHash(byte[] swfHash) {
        this.swfHash = swfHash;
    }
    public int getSwfSize() {
        return swfSize;
    }
    public void setSwfSize(int swfSize) {
        this.swfSize = swfSize;
    }
    public int getThreads() {
        return threads;
    }
    public void setThreads(int threads) {
        this.threads = threads;
    }
    public RtmpWriter getWriterToSave() {
        return writerToSave;
    }
    public void setWriterToSave(RtmpWriter writerToSave) {
        this.writerToSave = writerToSave;
    }
    public List<LiveSettings> getClientOptionsList() {
        return clientOptionsList;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[host: '").append(host);
        sb.append("' port: ").append(port);
        sb.append(" appName: '").append(appName);
        sb.append("' streamName: '").append(streamName);
        sb.append("' saveAs: '").append(chachePathForRecord);
        if(clientVersion != null) {
            sb.append(" clientVersionToUse: '").append(Utils.toHex(clientVersion,0,clientVersion.length,false)).append('\'');
        }
        sb.append(" start: ").append(start);
        sb.append(" length: ").append(length);
        sb.append(" buffer: ").append(buffer);
        sb.append(" params: ").append(params);
        sb.append(" args: ").append(Arrays.toString(args));
        if(swfHash != null) {
            sb.append(" swfHash: '").append(Utils.toHex(swfHash,0,swfHash.length,false));
            sb.append("' swfSize: ").append(swfSize).append('\'');
        }
        sb.append(" load: ").append(load);
        sb.append(" loop: ").append(loop);
        sb.append(" threads: ").append(threads);
        if(getNowPortlaytResolution() != null){
        sb.append(" resolution w: ").append(getNowPortlaytResolution().right);
        sb.append(" resolution h: ").append(getNowPortlaytResolution().bottom);
        }
        sb.append(" framerate: ").append(v_frame_rate);
        sb.append(" keyframe: ").append(keyframe_interval);
        sb.append(']');
        return sb.toString();
    }




	/**
	 * isUseMicを取得します。
	 * @return isUseMic
	 */
	public boolean isUseMic() {
	    return isUseMic;
	}
	/**
	 * isUseMicを設定します。
	 * @param isUseMic isUseMic
	 */
	public void setUseMic(boolean isUseMic) {
	    this.isUseMic = isUseMic;
	}
	/**
	 * isUseCamを取得します。
	 * @return isUseCam
	 */
	public boolean isUseCam() {
	    return isUseCam;
	}
	/**
	 * isUseCamを設定します。
	 * @param isUseCam isUseCam
	 */
	public void setUseCam(boolean isUseCam) {
	    this.isUseCam = isUseCam;
	}

	/**
	 * user_fpsを取得します。
	 * @return user_fps
	 */
	public int getUser_fps() {
	    return user_fps;
	}
	/**
	 * user_fpsを設定します。
	 * @param user_fps user_fps
	 */
	public void setUser_fps(int user_fps) {
	    this.user_fps = user_fps;
	}
	/**
	 * bit_rateを取得します。
	 * @return bit_rate
	 */
	public int getV_Bit_rate() {
	    return v_bit_rate;
	}

	/**
	 * bit_rateを設定します。
	 * @param bit_rate bit_rate
	 */
	public void setBit_rate(int bit_rate) {
	    this.v_bit_rate = bit_rate;
	}

	/**
	 * keyframe_intervalを取得します。
	 * @return keyframe_interval
	 */
	public int getKeyframe_interval() {
	    return keyframe_interval;
	}

	/**
	 * keyframe_intervalを設定します。
	 * @param keyframe_interval keyframe_interval
	 */
	public void setKeyframe_interval(int keyframe_interval) {
	    this.keyframe_interval = keyframe_interval;
	}

	public int getSampleRate() {
		return this.sample_rate;
	}

	public void setSampleRate(int sample_rate) {
		this.sample_rate = sample_rate;
	}

	/**
	 * isPortLaytを取得します。
	 * @return isPortLayt
	 */
	public boolean isPortLayt() {
	    return isPortLayt;
	}

	/**
	 * isPortLaytを設定します。
	 * @param isPortLayt isPortLayt
	 */
	public void setPortLayt(boolean isPortLayt) {
		Log.d("NLiveRoid","setPortLayt " + isPortLayt);
	    this.isPortLayt = isPortLayt;
	}

	public void setRatio(float ratio) {
		this.ratio = ratio;
	}

	public float getRatio() {
		return ratio;
	}


	/**
	 * zoomSupportedを取得します。
	 * @return zoomSupported
	 */
	public boolean isZoomSupported() {
	    return zoomSupported;
	}

	/**
	 * zoomSupportedを設定します。
	 * @param zoomSupported zoomSupported
	 */
	public void setZoomSupported(boolean zoomSupported) {
	    this.zoomSupported = zoomSupported;
	}

	/**
	 * シーンモードのみ保存する
	 * @return
	 */
	public int getSceneModeIndex() {
		return sceneIndex;
	}
	public void setSceneModeIndex(int parseInt) {
		this.sceneIndex = parseInt;
	}
	public void setSupportedSceneMode(boolean isSupportedSceneMode) {
		this.isSupportedSceneMode = isSupportedSceneMode;
	}
	public boolean isSupoprtedSceneMode() {
		return isSupportedSceneMode;
	}
	public void setSupportedFlashMode(boolean isSupportedFlashMode) {
		this.isSupportedFlashMode = isSupportedFlashMode;
	}
	public boolean isSupoprtedFlashMode() {
		return isSupportedFlashMode;
	}
	public void setSupportedWhiteblMode(boolean isSupportedWhiteblMode) {
		this.isSupportedWhiteblMode = isSupportedWhiteblMode;
	}
	public boolean isSupoprtedWhiteblMode() {
		return isSupportedWhiteblMode;
	}
	public void setSupoprtedColorEeffects(boolean isSupportedColorEffects) {
		this.isSupportedColorEffects = isSupportedColorEffects;;
	}
	public boolean isSupoprtedColorEeffects() {
		return isSupportedColorEffects;
	}
	public void setSupportedAntiB(boolean isSupportedAntib) {
		this.isSupportedAntib = isSupportedAntib;
	}
	public boolean isSupoprtedAntiB() {
		return isSupportedAntib;
	}



	public boolean isEncodeStarted() {
		return isEncodeStarted;
	}

	public void setEncodeStarted(boolean isencodeStarted) {
	    this.isEncodeStarted = isencodeStarted;
	}


	public boolean isStreamStarted() {
		return isStreamStarted;
	}

	/**
	 * isStreamStartedを設定します。
	 * @param isStreamStarted isStreamStarted
	 */
	public void setStreamStarted(boolean isStreamStarted) {
	    this.isStreamStarted = isStreamStarted;
	}

	public int getA_Bitrate() {
		return this.a_bit_rate;
	}

	/**
	 * a_bit_rateを設定します。
	 * @param a_bit_rate a_bit_rate
	 */
	public void setA_Bit_rate(int a_bit_rate) {
	    this.a_bit_rate = a_bit_rate;
	}

	/**
	 * a_frame_rateを取得します。
	 * @return a_frame_rate
	 */
	public int getA_Frame_rate() {
	    return a_frame_rate;
	}

	/**
	 * a_frame_rateを設定します。
	 * @param a_frame_rate a_frame_rate
	 */
	public void setA_Frame_rate(int a_frame_rate) {
	    this.a_frame_rate = a_frame_rate;
	}

	public List<Size> getResolutionList() {
		return resolutionList;
	}

	public void setResolutionList(List<Size> sizes) {
		this.resolutionList = sizes;
	}

	/**
	 * resolutionIndexを取得します。
	 * @return resolutionIndex
	 */
	public int getResolutionIndex() {
	    return resolutionIndex;
	}

	/**
	 * resolutionIndexを設定します。
	 * @param resolutionIndex resolutionIndex
	 */
	public void setNowResolution(int resolutionIndex) {
		Log.d("NLiveRoid","setNowResolution resolutionIndex" + resolutionIndex);
	    this.resolutionIndex = resolutionIndex;
	}


	/**
	 *
	 * レイアウト用のカメラ解像度取得します。
	 * Camera.Sizeがインスタンス化できないのでRectで代用
	 * @return resolutionIndex
	 */
	public Rect getNowActualResolution() {
		Log.d("NLiveRoid","isPortLayt " + isPortLayt);
		if(resolutionList == null)return null;
//		if(isPortLayt){
//		return new Rect(0,0,resolutionList.get(resolutionIndex).height,resolutionList.get(resolutionIndex).width);
//		}else{
		return new Rect(0,0,resolutionList.get(resolutionIndex).width,resolutionList.get(resolutionIndex).height);
//		}
	}
	/**
	 *エンコード時の解像度、実際のエンコード時はYUVのHeightも必要な為、getNowActualResolutionを使用する
	 *なのでこのメソッドはメタデータのみで使用
	 * @return resolutionIndex{}
	 */
	public Rect getNowPortlaytResolution() {//左右に黒を入れることを考慮した解像度を返す
		if(resolutionList == null)return null;
			int height = resolutionList.get(resolutionIndex).width;
			float tmp_ratio = ((float)height)/((float)resolutionList.get(resolutionIndex).height);
			int width = (int)(height*tmp_ratio);
			Log.d("LiveSetting","getNowResolution " + width + " " + height);
		return new Rect(0,0,(int)(height*tmp_ratio),height);//横/縦の比率を縦に書けた値が横幅になる
	}

	/**
	 * isBackGroundCamを取得します。
	 * @return isBackGroundCam
	 */
	public boolean isBackGroundCam() {
	    return isBackGroundCam;
	}

	/**
	 * isBackGroundCamを設定します。
	 * @param isBackGroundCam isBackGroundCam
	 */
	public void setBackGroundCam(boolean isBackGroundCam) {
	    this.isBackGroundCam = isBackGroundCam;
	}

	/**
	 * isBackGroundMicを取得します。
	 * @return isBackGroundMic
	 */
	public boolean isBackGroundMic() {
	    return isBackGroundMic;
	}

	/**
	 * isBackGroundMicを設定します。
	 * @param isBackGroundMic isBackGroundMic
	 */
	public void setBackGroundMic(boolean isBackGroundMic) {
	    this.isBackGroundMic = isBackGroundMic;
	}

	/**
	 * isRingCamEnableを取得します。
	 * @return isRingCamEnable
	 */
	public boolean isRingCamEnable() {
	    return isRingCamEnable;
	}

	/**
	 * isRingCamEnableを設定します。
	 * @param isRingCamEnable isRingCamEnable
	 */
	public void setRingCamEnable(boolean isRingCamEnable) {
	    this.isRingCamEnable = isRingCamEnable;
	}
	/**
	 * isRingMicEnableを取得します。
	 * @return isRingMicEnable
	 */
	public boolean isRingMicEnable() {
	    return isRingMicEnable;
	}
	/**
	 * isRingMicEnableを設定します。
	 * @param isRingMicEnable isRingMicEnable
	 */
	public void setRingMicEnable(boolean isRingMicEnable) {
	    this.isRingMicEnable = isRingMicEnable;
	}
	//プレビュー0、スナップ1、静止画2、動画3
	public int getMode() {
		return MODE;
	}
	/**
	 * MODEを設定します。
	 * @param MODE MODE
	 */
	public void setMode(int MODE) {
		Log.d("LiveSetting","SETMODE --"+MODE);
	    this.MODE = MODE;
	}
	/**
	 * viewAngleRatioを取得します。
	 * @return viewAngleRatio
	 */
	public float getViewAngleRatio() {
	    return viewAngleRatio;
	}
	/**
	 * viewAngleRatioを設定します。
	 * @param viewAngleRatio viewAngleRatio
	 */
	public void setViewAngleRatio(float viewAngleRatio) {
		Log.d("LiveSettings","ViewAngleRatio" + viewAngleRatio);
	    this.viewAngleRatio = viewAngleRatio;
	}
	public void culclateRatio() {
		Rect rect = getNowActualResolution();
		 if(viewAngleRatio == 1){
	        	ratio = (float)	rect.bottom/(float)rect.right;
	        }else{
	        	ratio = viewAngleRatio;
	        }
		 Log.d("LS","CULC RATIO ---- " + ratio);
	}

	/**
	 * bmpを取得します。
	 * @return bmp
	 */
	public Uri getBmpPath() {
	    return bmpUri;
	}


	public void setBmpPath(Uri path) {//ネイティブ初期化済み必須
		this.bmpUri = path;

	}
	public Rect getBmpRect() {
		return bmpRect;
	}
	public void setBmpRect(Rect rect){
		this.bmpRect = rect;
	}
	public void setBmp(Bitmap bmp){
		this.bmp = bmp;
	}
	public Bitmap getBmp() {
		return bmp;
	}

//	public boolean isStereo() {
//		return isStereo;
//	}
//	public void setIsStereo(boolean isstereo) {
//		this.isStereo = isstereo;
//	}

	public float getVolume() {
		return volume;
	}
	public void setVolume(float volume) {
		this.volume = volume;
	}
}
