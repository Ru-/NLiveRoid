package nliveroid.nlr.main;


public interface HandleNamable {
	void createCommentedList(String userid);
	void setHandleName(int bgColor,int foColor,String name);
	//ConfigDialogからゲットセットする

	boolean isAt();
	boolean isAtOverwrite();
	void setAtEnable(boolean isAt);
	void setAtOverwrite(boolean isAtoverwrite);
	void setAutoGetUserName(boolean isChecked);
	boolean isSetNameReady();
}
