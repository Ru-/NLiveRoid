package nliveroid.nlr.main;

public interface NumberPickable{

	void cancelIncrement();

	void cancelDecrement();

	void setRange(int i, int j);

	void setCurrent(int defaultNum);

	void setClickable(boolean b);

	void setLongClickable(boolean b);

	int getCurrent();

}
