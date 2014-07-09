package aquestalk2;

public class AquesTalk2 {
	static {
		System.loadLibrary("AquesTalk2");
	}
	/**
	 * 音声記号列から音声データを生成します。
	 * <p>発話速度は通常の速度を100として、50 - 300 の間で指定します(単位は%)。</p>
	 * @param kanaText 音声記号列(UTF-8)
	 * @param speed 発話速度(%)
	 * @param phontDat Phontデータ   デフォルトのを用いるときはnullを指定
	 * @return wavフォーマットのデータ   エラー時には,長さ１で、先頭にエラーコードが返される
	 */
	public static byte[] synthe(String kanaText, int speed, byte[] phontDat)
	{
		return new AquesTalk2().syntheWav(kanaText, speed, phontDat);
	}
	/**
	 * 音声記号列から音声データを生成します。JNI実装(native修飾子)
	 * <p>発話速度は通常の速度を100として、50 - 300 の間で指定します(単位は%)。</p>
	 * @param kanaText 音声記号列(UTF-8)
	 * @param speed 発話速度(%)
	 * @param phontDat Phontデータ   デフォルトのを用いるときはnullを指定
	 * @return wavフォーマットのデータ  エラー時には,長さ１で、先頭にエラーコードが返される
	 */
	public synchronized native byte[] syntheWav(String kanaText, int speed, byte[] phontDat);
}
/*
エラーコード一覧
	100 その他のエラー
	101 メモリ不足
	102 音声記号列に未定義の読み記号が指定された
	103 韻律データの時間長がマイナスなっている
	104 内部エラー(未定義の区切りコード検出）
	105 音声記号列に未定義の読み記号が指定された
	106 音声記号列のタグの指定が正しくない
	107 タグの長さが制限を越えている（または[>]がみつからない）
	108 タグ内の値の指定が正しくない
	109 WAVE 再生ができない（サウンドドライバ関連の問題）
	110 WAVE 再生ができない（サウンドドライバ関連の問題 非同期再生）
	111 発声すべきデータがない
	-38 音声記号列が長すぎる
	-37 １つのフレーズ中の読み記号が多すぎる
	-36 音声記号列が長い（内部バッファオーバー1）
	-35 ヒープメモリ不足
	-34 音声記号列が長い（内部バッファオーバー1）
	-16~-24 Phont データが正しくない
*/