package nliveroid.nlr.main;

import android.app.Activity;

public interface Speechable {
	void addSpeech(String str) throws InterruptedException;
	void setSpeed(int speed);
	void setPich(int pich);
	void destroy();
	boolean isInitalized();
	void setContext(Activity context, int speed, int phontIndex);
	Object[] getStatus();
}
