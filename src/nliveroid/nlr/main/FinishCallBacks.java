package nliveroid.nlr.main;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public interface FinishCallBacks {
	public void finishCallBack(ArrayList<LiveInfo> info);
	public void finishCallBack(ArrayList<LiveInfo> info,
			LinkedHashMap<String, String> generate);
	public void finishCallBack(ArrayList<LiveInfo> liveInfos, String str);
}
