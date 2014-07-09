package nliveroid.nlr.main;

import java.io.Serializable;
import java.util.EnumMap;
import java.util.Iterator;

class CommandMapping implements Serializable{
	private static final long serialVersionUID =-7674852638292555620L;
	private EnumMap<CommandKey, String> map;
	private boolean isOwner;
	private boolean isBSPEnable;
	private String bspToken = "";
	private String bspName = "";
	private String bspColor = "white";

	/**
	 * デフォルト初期化
	 * 184初期化される
	 */
	public CommandMapping() {//プレーン初期化なし
		map = new EnumMap<CommandKey, String>(CommandKey.class);
		//CMD は要するに万能に使う参照 CMD, Size, Align, Color, Mobile, Se
		map.put(CommandKey.CMD, CommandValue.ANONYM.toString());
		map.put(CommandKey.Align, "");
		map.put(CommandKey.Size, "");
		map.put(CommandKey.Color, "");
	}

	public String getBSPName() {
		return bspName;
	}
	public void setBSPName(String name) {
		this.bspName = name;
	}

	public String getBSPColor() {
		return this.bspColor;
	}
	public void setBSPColor(String color) {
		this.bspColor = color;
	}

	public String getBSPToken() {
		return bspToken;
	}

	public void setBSPToken(String token) {
		this.bspToken = token;
	}
	/**
	 * 保存していた値で初期化
	 */
	public CommandMapping(String cmd,String size,String color,String align,boolean isOwner) {
		this.isOwner = isOwner;

		map = new EnumMap<CommandKey, String>(CommandKey.class);
		//CMD は要するに万能に使う参照 CMD, Size, Align, Color, Mobile, Se
		map.put(CommandKey.CMD, cmd);
		map.put(CommandKey.Align, align);
		map.put(CommandKey.Size, size);
		map.put(CommandKey.Color, color);
	}

	public CommandMapping(boolean isOwner) {
		this();
		this.isOwner = isOwner;
	}

	public void set(CommandValue cmd) {
		map.put(CommandKey.CMD, cmd.toString());
	}

	public void set(CommandKey key, CommandValue cmd) {
		map.put(key, cmd.toString());
	}

	public void set(String cmd) {
		map.put(CommandKey.CMD, cmd);
	}

	/**
	 * 結局の所普通のStringを入れている
	 * @param key
	 * @param cmd
	 */
	public void set(CommandKey key, String cmd) {
		map.put(key, cmd);
	}

	/**
	 * コマンドの削除
	 */
	public void remove(CommandValue cmd) {
		map.remove(CommandKey.CMD);
	}

	public String getValue(CommandKey key) {
		return map.get(key);
	}

	public String toString() {
		StringBuilder s = new StringBuilder("");
		Iterator<CommandKey> it = map.keySet().iterator();
		while (it.hasNext()) {
			s.append(" ");
			s.append(map.get(it.next()));
		}
		return s.toString();
	}

	public boolean isOwner() {
		return isOwner;
	}
	public void setOwner(boolean isOwner){
		this.isOwner = isOwner;
	}

	public boolean isBSPEnable() {
		return isBSPEnable;
	}
	public void setBSPEnable(boolean isBSPEnable){
		this.isBSPEnable = isBSPEnable;
	}




}// End of CommandMapping

