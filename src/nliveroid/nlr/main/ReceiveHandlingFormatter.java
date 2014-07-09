package nliveroid.nlr.main;

import java.util.Map;

import nliveroid.nlr.main.CommentTable.CommentRecieveFormat;
import android.util.Log;

public class ReceiveHandlingFormatter  implements CommentRecieveFormat {

		private String[] rowStr = new String[7];
		public CommentRecieveFormat.UserTypeMap userTypeMap = new CommentRecieveFormat.UserTypeMap();

		// linuxエスケープシーケンスカラー
		private static final String COLOR_ESCAPE_BASE = "\u001b[%02d;%dm";

		/** 主コメの色。デフォルトは黄色 */
		private static final String START_COLOR_OWNER = String.format(
				COLOR_ESCAPE_BASE, 00, 33);

		/** エスケープシーケンスカラーの終わり */
		private static final String END_COLOR = "\u001b[0m";

		/** 投稿日時 */
//		private String postedtime = null;

		/** コメント番号 */
		private String number = null;

		/** ユーザー ID */
		private String userid = null;

		/** コメント主の種類 */
		private String comenterType = null;

		/** コメントのコマンド */
//		private String command = null;

		/** モバイルからのコメントの場合のマーカー */
		private String mobile = "";



		/**
		 * 属性の各フィールドをセット これは commentReceived() メソッドの前に呼ばれます。 このメソッドは CommentFormat
		 * クラスのメソッドをオーバーライドしています。
		 *
		 * @param attrMap
		 *            属性のマップ
		 */
		@Override
		public void commentAttrReceived(Map<String, String> attrMap) {
			// 送信者のtype
			rowStr[0] = attrMap
					.get(CommentRecieveFormat.USER_TYPE);

			// ユーザー ID
			rowStr[1] = attrMap.get(CommentRecieveFormat.USER_ID);
			// 公式運営コメントの名前
			String name = attrMap.get(CommentRecieveFormat.NAME);

			// コメントのコマンド
			rowStr[2] = attrMap.get(CommentRecieveFormat.COMMAND);

			// 投稿時間
			rowStr[3] = attrMap.get(CommentRecieveFormat.DATE);

			// スコア
			rowStr[4] = attrMap.get(CommentRecieveFormat.SCORE);

			// コメ番
			rowStr[5] = attrMap.get(CommentRecieveFormat.NUMBER);

			// 公式運営のIDは名前に
			if (name != null) {
				rowStr[1] = name;
			} else {
				if (rowStr[1] == null) {
					rowStr[1] = "Failed get id.";
				}
			}

			// 運営システムからのコメントはコマンドないらしい
			if (rowStr[2] == null) {
				rowStr[2] = "";
			}

			else {
				mobile = "";
			}
			rowStr[0] = userTypeMap.get(rowStr[0]);
//			rowStr[column_Sequence[0]] = rowStr[column_Sequence[0]]
//					.equals(CommentRecieveFormat.Member.NORMAL) ? "NOR"
//					: // 一般会員
//						rowStr[column_Sequence[0]]
//								.equals(CommentRecieveFormat.Member.PREMIUM) ? "PRE"
//										: // プレミアム会員
//											rowStr[column_Sequence[0]]
//													.equals(CommentRecieveFormat.Member.OWNER) ? "OWN"
//													: // 放送主
//														rowStr[column_Sequence[0]]
//														       .equals(CommentRecieveFormat.Member.SYSTEM) ? "SYSTEM"
//														    		   :rowStr[column_Sequence[0]];//それ以外はそのまま

	//
//					rowStr[column_Sequence[0]]
//							.equals(CommentRecieveFormat.Member.NORMAL_MALE) ? "N_MALE"
//							: // 男性一般会員
//							rowStr[column_Sequence[0]]
//									.equals(CommentRecieveFormat.Member.NORMAL_FEMALE) ? "N_FEMALE"
//									: // 女性一般会員
	//
	//
//											rowStr[column_Sequence[0]]
//													.equals(CommentRecieveFormat.Member.PREMIUM_MALE) ? "P_MALE"
//													: // 男性プレミアム会員
//													rowStr[column_Sequence[0]]
//															.equals(CommentRecieveFormat.Member.PREMIUM_FEMALE) ? "P_FEMALE"
//															: // 女性プレミアム会員
//															rowStr[column_Sequence[0]]
//																	.equals(CommentRecieveFormat.Member.SYSTEM) ? "SYSTEM"
//																	: // 運営システム
	//
//																			rowStr[column_Sequence[0]]
//																					.equals(CommentRecieveFormat.Member.OFFICIAL) ? "OFFICIAL"
//																					: // 公式運営
//																					rowStr[column_Sequence[0]]
//																							.equals(CommentRecieveFormat.Member.OFFICIAL2) ? "BSP"
//																							: // BSP
//																								rowStr[column_Sequence[0]]"; // その他(そのまま表示)
		}


		@Override
	    public String[] getReceivedComment(String comment){

			//TYPE
	    	if(!mobile.equals("")){
	    		rowStr[0] = rowStr[0] + "mob";		//携帯は単にmobを加える
	    	}
	    	if(rowStr[0] ==null){
	    		rowStr[0] = URLEnum.HYPHEN;
	    	}
	    	//ID
	    	if(rowStr[1] == null) {
	    		rowStr[1] = URLEnum.HYPHEN;
	    	}
	    	//CMD
	    	if(rowStr[2] == null) {
	    		rowStr[2] = URLEnum.HYPHEN;
	    	}
	    	//TIME
	    	if(rowStr[3] == null) {
	    		rowStr[3] = URLEnum.HYPHEN;
	    	}
	    	//SCORE
	    	if(rowStr[4]==null){
	    		rowStr[4] = "0";
	    	}
	    	//NUM
	    	if(rowStr[5]==null){
	    		rowStr[5] = URLEnum.HYPHEN;
	    	}
	    	//COMMENT
	    	rowStr[6] = comment;

//	    	Log.d("Log"," NEW RECORD " +rowStr[0]+" DATA "+rowStr[1]+" DATA "+rowStr[2]+" DATA "+rowStr[3]+" DATA " +rowStr[4]+" DATA " +rowStr[5]+" DATA "+rowStr[6]);
	    	return new String[]  {rowStr[0],rowStr[1],rowStr[2],rowStr[3],rowStr[4],rowStr[5],rowStr[6]};//とりあえずサムネなし

	      }


		public void clear() {
			rowStr = new String[7];
		}




	} // End of Receive_Formatter


