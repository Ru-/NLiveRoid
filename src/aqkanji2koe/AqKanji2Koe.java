//////////////////////////////////////////////////////////////////////
/*!	@class	AqKanji2Koe

	@brief	AquesTalk(2)用言語処理部

  	漢字かな混じりテキスト->音声記号列

	@author	N.Yamazaki (Aquest)

	@date	2011/01/07	N.Yamazaki	Creation
*/
//  COPYRIGHT (C) 2011 AQUEST CORP.
//////////////////////////////////////////////////////////////////////

package aqkanji2koe;

public class AqKanji2Koe {
	static {
		System.loadLibrary("AqKanji2Koe");
	}
	/**
	 * 漢字かな交じりのテキストを音声記号列に変換します。
	 * @param dirDic	辞書のディレクトリを指定。通常、"<app dir>/aq_dic"
	 * @param kanjiText 入力漢字かな混じり文テキスト文字列（UTF-8)
	 * @return 音声記号列文字列（UTF-8)。最大文字数4096byte。
	 * 	処理エラーの時は"[ERR]<エラーメッセージ>" 文字列が返る。
	 */
	public static String conv(String dirDic, String kanjiText)
	{
		return new AqKanji2Koe().Convert(dirDic, kanjiText);
	}
	public synchronized native String Convert(String dirDic, String kanjiText);
}

