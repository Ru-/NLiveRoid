package nliveroid.nlr.main;

import android.content.Intent;

public interface IOwnerContext {
	void startLive();
	void endLive();
	void extendTestLive();
	void switchPlayer(boolean b);//BCOverLayの為にどちらかフラグ必要
}
