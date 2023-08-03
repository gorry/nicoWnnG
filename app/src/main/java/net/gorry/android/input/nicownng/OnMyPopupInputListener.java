/**
 *
 */

package net.gorry.android.input.nicownng;

/**
 *
 * ポップアップ入力リスナ
 *
 */
abstract public class OnMyPopupInputListener {
	/** キャンセルの選択値 */
	public static final int CANCEL = -1;
	/** アイテムクリックの選択値 */
	public static final int CLICKED = 10000;
	/** アイテムロングクリックの選択値 */
	public static final int LONGCLICKED = 20000;

	/**
	 * 入力リスナ
	 * @param id 入力値
	 */
	abstract public void onInput(final int id);
}
