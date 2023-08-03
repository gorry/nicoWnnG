/**
 * 
 */
package net.gorry.android.input.nicownng;

import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.inputmethodservice.Keyboard;
import android.util.Log;

/**
 * 
 * 高さを変更できるキーボード
 * 
 * @author GORRY
 *
 */
public class MyHeightKeyboard extends Keyboard {
	private static final String TAG = "MyHeightKeyboard";
	private static final boolean V = false;

	private static final int keyHeightTablePortrait[] = {
		36, 48, 60, 72, 84, 96, 108, 120, 144, 168
	};
	private static final int keyHeightTableLandscape[] = {
		24, 36, 48, 60, 72, 84, 96, 108, 132, 156
	};

	private Context mContext;
	private int mKeyHeight;
	private int mTotalHeight;
	private int mIndexShiftKey[] = { -1, -1 };
	private int mNumIndexShiftKey = 0;
	private int mIndexAltKey[] = { -1, -1 };
	private int mNumIndexAltKey = 0;
	private int mIndexCtrlKey[] = { -1, -1 };
	private int mNumIndexCtrlKey = 0;
	private int mIndexSelKey = -1;
	private List<Key> mKeys;
	private static boolean mSwapShiftAlt = false;
	private static boolean mSwapMiniEnter = false;
	private static boolean mAssign12KeyShift = false;
	
	private static final int popupKeyboardTable_12key[] = {
		R.xml.keyboard_popup_12key_jp_0,
		R.xml.keyboard_popup_12key_jp_1,
		R.xml.keyboard_popup_12key_jp_2,
		R.xml.keyboard_popup_12key_jp_3,
		R.xml.keyboard_popup_12key_jp_4,
		R.xml.keyboard_popup_12key_jp_5,
		R.xml.keyboard_popup_12key_jp_6,
		R.xml.keyboard_popup_12key_jp_7,
		R.xml.keyboard_popup_12key_jp_8,
		R.xml.keyboard_popup_12key_jp_9,
	};
	
	private static final int popupKeyboardTable_qwerty[] = {
		R.xml.keyboard_popup_qwerty_jp_0,
		R.xml.keyboard_popup_qwerty_jp_1,
		R.xml.keyboard_popup_qwerty_jp_2,
		R.xml.keyboard_popup_qwerty_jp_3,
		R.xml.keyboard_popup_qwerty_jp_4,
		R.xml.keyboard_popup_qwerty_jp_5,
		R.xml.keyboard_popup_qwerty_jp_6,
		R.xml.keyboard_popup_qwerty_jp_7,
		R.xml.keyboard_popup_qwerty_jp_8,
		R.xml.keyboard_popup_qwerty_jp_9,
	};
	
	private static final int popupKeyboardTable_subten_qwerty[] = {
		R.xml.keyboard_popup_subten_qwerty_jp_0,
		R.xml.keyboard_popup_subten_qwerty_jp_1,
		R.xml.keyboard_popup_subten_qwerty_jp_2,
		R.xml.keyboard_popup_subten_qwerty_jp_3,
		R.xml.keyboard_popup_subten_qwerty_jp_4,
		R.xml.keyboard_popup_subten_qwerty_jp_5,
		R.xml.keyboard_popup_subten_qwerty_jp_6,
		R.xml.keyboard_popup_subten_qwerty_jp_7,
		R.xml.keyboard_popup_subten_qwerty_jp_8,
		R.xml.keyboard_popup_subten_qwerty_jp_9,
	};
	
	private static final int popupKeyboardTable_subten_12key[] = {
		R.xml.keyboard_popup_subten_12key_jp_0,
		R.xml.keyboard_popup_subten_12key_jp_1,
		R.xml.keyboard_popup_subten_12key_jp_2,
		R.xml.keyboard_popup_subten_12key_jp_3,
		R.xml.keyboard_popup_subten_12key_jp_4,
		R.xml.keyboard_popup_subten_12key_jp_5,
		R.xml.keyboard_popup_subten_12key_jp_6,
		R.xml.keyboard_popup_subten_12key_jp_7,
		R.xml.keyboard_popup_subten_12key_jp_8,
		R.xml.keyboard_popup_subten_12key_jp_9,
	};
	
	private static final int popupKeyboardTable_nico2[] = {
		R.xml.keyboard_popup_nico2_jp_0,
		R.xml.keyboard_popup_nico2_jp_1,
		R.xml.keyboard_popup_nico2_jp_2,
		R.xml.keyboard_popup_nico2_jp_3,
		R.xml.keyboard_popup_nico2_jp_4,
		R.xml.keyboard_popup_nico2_jp_5,
		R.xml.keyboard_popup_nico2_jp_6,
		R.xml.keyboard_popup_nico2_jp_7,
		R.xml.keyboard_popup_nico2_jp_8,
		R.xml.keyboard_popup_nico2_jp_9,
	};

	private static final int popupKeyboardTable[][] = {
		popupKeyboardTable_12key,  // KEYTYPE_12KEY
		popupKeyboardTable_qwerty, // KEYTYPE_QWERTY
		popupKeyboardTable_nico2,  // KEYTYPE_NICO2
		popupKeyboardTable_subten_qwerty, // KEYTYPE_QWERTY(SUBTEN)
		popupKeyboardTable_subten_12key, // KEYTYPE_12KEY(SUBTEN)
	};

	/**
	 * @param context context
	 * @param xmlLayoutResId resid
	 * @param heightIndex height
	 * @param isPortrait portrait
	 */
	public MyHeightKeyboard(Context context, int xmlLayoutResId, int heightIndex, int keyType, boolean isPortrait) {
		super(context, xmlLayoutResId);
		MyHeightKeyboardCore(context, xmlLayoutResId, heightIndex, keyType, isPortrait, 0);
	}

	/**
	 * @param context context
	 * @param xmlLayoutResId resid
	 * @param heightIndex height
	 * @param keyType keytype
	 * @param isPortrait portrait
	 * @param modeKeyString modekeystring
	 */
	public MyHeightKeyboard(Context context, int xmlLayoutResId, int heightIndex, int keyType, boolean isPortrait, int modeKeyString) {
		super(context, xmlLayoutResId);
		MyHeightKeyboardCore(context, xmlLayoutResId, heightIndex, keyType, isPortrait, modeKeyString);
	}

	private void MyHeightKeyboardCore(Context context, int xmlLayoutResId, int heightIndex, int keyType, boolean isPortrait, int modeKeyString) {
		mContext = context;
		final int[] htable = (isPortrait ? keyHeightTablePortrait : keyHeightTableLandscape);
		final int height = htable[heightIndex];
		mKeys = this.getKeys();
		// Log.d("nicoWnnG", "keys.size="+keys.size());
		int maxy = 0;
		int maxheight = 0;

		mNumIndexShiftKey = 0;
		for (int i=0; i<mIndexShiftKey.length; i++ ) {
			mIndexShiftKey[i] = -1;
		}
		mNumIndexCtrlKey = 0;
		for (int i=0; i<mIndexCtrlKey.length; i++ ) {
			mIndexCtrlKey[i] = -1;
		}
		mNumIndexAltKey = 0;
		for (int i=0; i<mIndexAltKey.length; i++ ) {
			mIndexAltKey[i] = -1;
		}

		mIndexSelKey = -1;

		if (mAssign12KeyShift) {
			assign12KeyShift();
		}

		for (int i=0; i<mKeys.size(); i++) {
			Key key = mKeys.get(i);
			// Log.d("nicoWnnG", i+": w="+key.width+",h="+key.height+",label="+key.label+",text="+key.text);

			// ポップアップキーボードのサイズ変更
			if (key.codes.length > 0) {
				switch (key.codes[0]) {
					case DefaultSoftKeyboard.KEYCODE_JP12_TOGGLE_MODE2:
					case DefaultSoftKeyboard.KEYCODE_QWERTY_TOGGLE_MODE2:
					{
						switch (key.popupResId) {
							case R.xml.keyboard_popup_subten_qwerty_jp_0: 
							case R.xml.keyboard_popup_subten_qwerty_jp_1: 
							case R.xml.keyboard_popup_subten_qwerty_jp_2: 
							case R.xml.keyboard_popup_subten_qwerty_jp_3: 
							case R.xml.keyboard_popup_subten_qwerty_jp_4: 
							case R.xml.keyboard_popup_subten_qwerty_jp_5: 
							case R.xml.keyboard_popup_subten_qwerty_jp_6: 
							case R.xml.keyboard_popup_subten_qwerty_jp_7: 
							case R.xml.keyboard_popup_subten_qwerty_jp_8: 
							case R.xml.keyboard_popup_subten_qwerty_jp_9: 
								if (isPortrait) {
									key.popupResId = 0;									
								} else {
									if (key.codes[0] == DefaultSoftKeyboard.KEYCODE_JP12_TOGGLE_MODE2) {
										key.codes[0] = DefaultSoftKeyboard.KEYCODE_JP12_TOGGLE_MODE;
									} else {
										key.codes[0] = DefaultSoftKeyboard.KEYCODE_QWERTY_TOGGLE_MODE;
									}
									
								}
						}						
					}
					break;
				}
				switch (key.codes[0]) {
					case DefaultSoftKeyboard.KEYCODE_JP12_TOGGLE_MODE:
					case DefaultSoftKeyboard.KEYCODE_QWERTY_TOGGLE_MODE:
					{
						int n = 0;
						for (int j=0; j<htable.length; j++) {
							if (height >= htable[j]) {
								n = j;
							}
						}
						switch (key.popupResId) {
							case R.xml.keyboard_popup_12key_jp_0:
							case R.xml.keyboard_popup_12key_jp_1:
							case R.xml.keyboard_popup_12key_jp_2:
							case R.xml.keyboard_popup_12key_jp_3:
							case R.xml.keyboard_popup_12key_jp_4:
							case R.xml.keyboard_popup_12key_jp_5:
							case R.xml.keyboard_popup_12key_jp_6:
							case R.xml.keyboard_popup_12key_jp_7:
							case R.xml.keyboard_popup_12key_jp_8:
							case R.xml.keyboard_popup_12key_jp_9:
								keyType = 0;
								break;
							case R.xml.keyboard_popup_qwerty_jp_0:
							case R.xml.keyboard_popup_qwerty_jp_1:
							case R.xml.keyboard_popup_qwerty_jp_2:
							case R.xml.keyboard_popup_qwerty_jp_3:
							case R.xml.keyboard_popup_qwerty_jp_4:
							case R.xml.keyboard_popup_qwerty_jp_5:
							case R.xml.keyboard_popup_qwerty_jp_6:
							case R.xml.keyboard_popup_qwerty_jp_7:
							case R.xml.keyboard_popup_qwerty_jp_8:
							case R.xml.keyboard_popup_qwerty_jp_9:
								keyType = 1;
								break;
							case R.xml.keyboard_popup_nico2_jp_0: 
							case R.xml.keyboard_popup_nico2_jp_1: 
							case R.xml.keyboard_popup_nico2_jp_2: 
							case R.xml.keyboard_popup_nico2_jp_3: 
							case R.xml.keyboard_popup_nico2_jp_4: 
							case R.xml.keyboard_popup_nico2_jp_5: 
							case R.xml.keyboard_popup_nico2_jp_6: 
							case R.xml.keyboard_popup_nico2_jp_7:
							case R.xml.keyboard_popup_nico2_jp_8:
							case R.xml.keyboard_popup_nico2_jp_9:
								keyType = 2;
								break;
							case R.xml.keyboard_popup_subten_qwerty_jp_0: 
							case R.xml.keyboard_popup_subten_qwerty_jp_1: 
							case R.xml.keyboard_popup_subten_qwerty_jp_2: 
							case R.xml.keyboard_popup_subten_qwerty_jp_3: 
							case R.xml.keyboard_popup_subten_qwerty_jp_4: 
							case R.xml.keyboard_popup_subten_qwerty_jp_5: 
							case R.xml.keyboard_popup_subten_qwerty_jp_6: 
							case R.xml.keyboard_popup_subten_qwerty_jp_7: 
							case R.xml.keyboard_popup_subten_qwerty_jp_8: 
							case R.xml.keyboard_popup_subten_qwerty_jp_9: 
								keyType = 3;
								break;
							case R.xml.keyboard_popup_subten_12key_jp_0: 
							case R.xml.keyboard_popup_subten_12key_jp_1: 
							case R.xml.keyboard_popup_subten_12key_jp_2: 
							case R.xml.keyboard_popup_subten_12key_jp_3: 
							case R.xml.keyboard_popup_subten_12key_jp_4: 
							case R.xml.keyboard_popup_subten_12key_jp_5: 
							case R.xml.keyboard_popup_subten_12key_jp_6: 
							case R.xml.keyboard_popup_subten_12key_jp_7: 
							case R.xml.keyboard_popup_subten_12key_jp_8: 
							case R.xml.keyboard_popup_subten_12key_jp_9: 
								keyType = 4;
								break;
						}
						key.popupResId = popupKeyboardTable[keyType][n];
						if (key.label != null) {
							if (key.label.toString().equals("[mode]")) {
								if (modeKeyString != 0) {
									key.label = context.getString(modeKeyString);
								} else {
									key.label = null;
									key.icon = context.getResources().getDrawable(R.drawable.key_12key_mode_hira);
								}
							}
						}
						// Log.d("nicoWnnG", "resourceName="+context.getResources().getResourceName(key.popupResId));
					}
					break;
				}
			}

			// キーサイズ変更
			key.y *= height;
			key.height *= height;
			if (maxy < key.y) {
				maxy = key.y;
				maxheight = 0;
			}
			if (maxheight < key.height) {
				maxheight = key.height;
			}

			// 入れ替え可能シフトキーを置換
			if (key.codes[0] == DefaultSoftKeyboard.KEYCODE_QWERTY_MYSHIFT) {
				key.codes[0] = DefaultSoftKeyboard.KEYCODE_QWERTY_SHIFT;
			}

			// メタキーを覚えておく
			if (key.codes[0] == DefaultSoftKeyboard.KEYCODE_QWERTY_SHIFT) {
				mIndexShiftKey[mNumIndexShiftKey++] = i;
			}
			if (key.codes[0] == DefaultSoftKeyboard.KEYCODE_QWERTY_CTRL) {
				mIndexCtrlKey[mNumIndexCtrlKey++] = i;
			}
			if ((key.codes[0] == DefaultSoftKeyboard.KEYCODE_QWERTY_ALT) || (key.codes[0] == Keyboard.KEYCODE_ALT)) {
				mIndexAltKey[mNumIndexAltKey++] = i;
			}
			if (key.codes[0] == DefaultSoftKeyboard.KEYCODE_FUNCTION_SELECT) {
				mIndexSelKey = i;
			}

		}
		mTotalHeight = maxy + maxheight;
		mKeyHeight = maxheight;
		if (mSwapShiftAlt) {
			swapShiftAltKey();
		}
		if (mSwapMiniEnter) {
			swapMiniEnter(context);
		}
		if (mAssign12KeyShift) {
			setShiftKeyIconLock(false);
		}

		// assign12KeyReverse(false);
	}

	@Override
	public int getHeight() {
		return mTotalHeight;
	}
	
	public int getKeyHeight() {
		return mKeyHeight;
	}
	
	private class swapKeyInfo {
		int[] codes;
		Drawable icon;
		Drawable iconPreview;
		CharSequence label;
		CharSequence popupCharacters;
		int popupResId;
		CharSequence text;
		
		swapKeyInfo(Key key) {
			codes = key.codes;
			icon = key.icon;
			iconPreview = key.iconPreview;
			label = key.label;
			popupCharacters = key.popupCharacters;
			popupResId = key.popupResId;
			text = key.text;
		}
		
		public void assign(Key key) {
			key.codes = codes;
			key.icon = icon;
			key.iconPreview = iconPreview;
			key.label = label;
			key.popupCharacters = popupCharacters;
			key.popupResId = popupResId;
			key.text = text;
		}
	};
	
	private void swapShiftAltKey() {
		// Shift/Altともに１つのときだけ変更可能
		if ((mNumIndexShiftKey != 1) || (mNumIndexAltKey != 1)) {
			return;
		}
		if (mIndexShiftKey[0] < 0) {
			return;
		}
		if (mIndexAltKey[0] < 0) {
			return;
		}

		// キー定義内容のswap
		{
			Key key;

			key = mKeys.get(mIndexShiftKey[0]);
			swapKeyInfo swapShiftKey = new swapKeyInfo(key);
			key = mKeys.get(mIndexAltKey[0]);
			swapKeyInfo swapAltKey = new swapKeyInfo(key);

			for (int i=0; i<mNumIndexShiftKey; i++) {
				if (mIndexShiftKey[i] >= mKeys.size()) {
					continue;
				}
				key = mKeys.get(mIndexShiftKey[i]);
				swapAltKey.assign(key);
			}
			for (int i=0; i<mNumIndexAltKey; i++) {
				if (mIndexAltKey[i] >= mKeys.size()) {
					continue;
				}
				key = mKeys.get(mIndexAltKey[i]);
				swapShiftKey.assign(key);
			}
		}

		// キー番号のswap
		int numIndexShiftKey = mNumIndexShiftKey;
		int[] indexShiftKey = mIndexShiftKey;
		mNumIndexShiftKey = mNumIndexAltKey;
		mIndexShiftKey = mIndexAltKey;
		mNumIndexAltKey = numIndexShiftKey;
		mIndexAltKey = indexShiftKey;
	}
	
	public static void setSwapShiftAlt(boolean sw) {
		mSwapShiftAlt = sw;
	}

	private void swapMiniEnter(Context context) {
		int leftArrowIndex = -1;
		int rightArrowIndex = -1;
		int enterIndex = -1;
		int enter2Index = -1;
		int emojiIndex = -1;
		int delIndex = -1;
		int userSymbolIndex = -1;

		for (int i=0; i<mKeys.size(); i++) {
			Key key = mKeys.get(i);
			// Log.d("nicoWnnG", i+": w="+key.width+",h="+key.height+",label="+key.label+",text="+key.text);
			switch (key.codes[0]) {
				case DefaultSoftKeyboard.KEYCODE_JP12_LEFT:
					leftArrowIndex = i;
					break;
				case DefaultSoftKeyboard.KEYCODE_JP12_RIGHT:
					rightArrowIndex = i;
					break;
				case DefaultSoftKeyboard.KEYCODE_QWERTY_MINIENTER:
					enterIndex = i;
					break;
				case DefaultSoftKeyboard.KEYCODE_QWERTY_MINIENTER2:
					enter2Index = i;
					break;
				case DefaultSoftKeyboard.KEYCODE_QWERTY_EMOJI:
					emojiIndex = i;
					break;
				case DefaultSoftKeyboard.KEYCODE_QWERTY_BACKSPACE:
					delIndex = i;
					break;
				case DefaultSoftKeyboard.KEYCODE_USERSYMBOL:
				case DefaultSoftKeyboard.KEYCODE_USERSYMBOL_ZEN_HIRAGANA:
				case DefaultSoftKeyboard.KEYCODE_USERSYMBOL_ZEN_KATAKANA:
				case DefaultSoftKeyboard.KEYCODE_USERSYMBOL_ZEN_ALPHABET:
				case DefaultSoftKeyboard.KEYCODE_USERSYMBOL_ZEN_NUMBER:
				case DefaultSoftKeyboard.KEYCODE_USERSYMBOL_HAN_KATAKANA:
				case DefaultSoftKeyboard.KEYCODE_USERSYMBOL_HAN_ALPHABET:
				case DefaultSoftKeyboard.KEYCODE_USERSYMBOL_HAN_NUMBER:
					userSymbolIndex = i;
					break;
			}
		}

		// 全てのキーがあるときだけ変更可能
		if (
				(leftArrowIndex != -1) &&
				(rightArrowIndex != -1) &&
				(enterIndex != -1) &&
				(emojiIndex != -1) &&
				(delIndex != -1)
				) {
			Key key;

			key = mKeys.get(leftArrowIndex);
			swapKeyInfo leftArrowKey = new swapKeyInfo(key);
			key = mKeys.get(rightArrowIndex);
			swapKeyInfo rightArrowKey = new swapKeyInfo(key);
			key = mKeys.get(enterIndex);
			swapKeyInfo enterKey = new swapKeyInfo(key);
			key = mKeys.get(emojiIndex);
			swapKeyInfo emojiKey = new swapKeyInfo(key);
			key = mKeys.get(delIndex);
			swapKeyInfo delKey = new swapKeyInfo(key);

			delKey.icon = context.getResources().getDrawable(R.drawable.key_12key_del);
			delKey.iconPreview = context.getResources().getDrawable(R.drawable.key_12key_del_b);
			emojiKey.icon = context.getResources().getDrawable(R.drawable.key_12key_sym_mushroom);
			emojiKey.iconPreview = context.getResources().getDrawable(R.drawable.key_12key_sym_mushroom_b);

			key = mKeys.get(leftArrowIndex);
			delKey.assign(key);
			key = mKeys.get(rightArrowIndex);
			emojiKey.assign(key);
			key = mKeys.get(enterIndex);
			rightArrowKey.assign(key);
			key = mKeys.get(delIndex);
			leftArrowKey.assign(key);
			key = mKeys.get(emojiIndex);
			enterKey.assign(key);
			return;
		}

		// 全てのキーがあるときだけ変更可能
		if (
				(leftArrowIndex != -1) &&
				(rightArrowIndex != -1) &&
				(enter2Index != -1) &&
				(emojiIndex != -1) &&
				(userSymbolIndex != -1)
				) {
			Key key;

			key = mKeys.get(leftArrowIndex);
			swapKeyInfo leftArrowKey = new swapKeyInfo(key);
			key = mKeys.get(rightArrowIndex);
			swapKeyInfo rightArrowKey = new swapKeyInfo(key);
			key = mKeys.get(emojiIndex);
			swapKeyInfo emojiKey = new swapKeyInfo(key);
			key = mKeys.get(userSymbolIndex);
			swapKeyInfo userSymbolKey = new swapKeyInfo(key);

			key = mKeys.get(leftArrowIndex);
			emojiKey.assign(key);
			key = mKeys.get(rightArrowIndex);
			userSymbolKey.assign(key);
			key = mKeys.get(emojiIndex);
			leftArrowKey.assign(key);
			key = mKeys.get(userSymbolIndex);
			rightArrowKey.assign(key);
			return;
		}
	}
	
	public static void setSwapMiniEnter(boolean sw) {
		mSwapMiniEnter = sw;
	}
	
	public void assign12KeyShift() {
		// 最初のキーがusersymbolならSHIFTキーに変更
		Key key = mKeys.get(0);
		if ((DefaultSoftKeyboard.KEYCODE_USERSYMBOL >= key.codes[0]) && (key.codes[0] >= DefaultSoftKeyboard.KEYCODE_USERSYMBOL_MAX)) {
			// あとでMYSHIFT→SHIFTに置換される
			key.codes[0] = DefaultSoftKeyboard.KEYCODE_QWERTY_MYSHIFT;
			key.modifier = true;
			key.sticky = true;
		}
	}

	public static void setAssign12KeyShift(boolean sw) {
		mAssign12KeyShift = sw;
	}

	public void assign12KeyReverse(boolean sw) {
		int id1 = 0, id2 = 0;
		int keycode = 0;
		Key key = mKeys.get(0);
		if (sw) {
			// 最初のキーがSHIFTキーならReverseキーに変更
			if (key.codes[0] == DefaultSoftKeyboard.KEYCODE_QWERTY_SHIFT) {
				keycode = DefaultSoftKeyboard.KEYCODE_JP12_REVERSE;
				id1 = R.drawable.key_12key_reverse;
				id2 = R.drawable.key_12key_reverse_b;
				key.modifier = false;
				key.sticky = false;
				key.on = false;
			}
		} else {
			// 最初のキーがReverseキーならSHIFTキーに変更
			if (key.codes[0] == DefaultSoftKeyboard.KEYCODE_JP12_REVERSE) {
				keycode = DefaultSoftKeyboard.KEYCODE_QWERTY_SHIFT;
				id1 = R.drawable.key_qwerty_shift;
				id2 = R.drawable.key_qwerty_shift_b;
				key.modifier = true;
				key.sticky = true;
				key.on = false;
			}
		}
		if (keycode != 0) {
			Drawable d;
			key.codes[0] = keycode;
			d = mContext.getResources().getDrawable(id1);
			if (d != null) d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
			key.icon = d;
			d = mContext.getResources().getDrawable(id2);
			if (d != null) d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
			key.iconPreview = d;
		}
	}

	public void setShiftKeyIconLock(boolean sw) {
		Key key;
		int id1, id2;
		Drawable d;

		for (int i=0; i<mNumIndexShiftKey; i++) {
			if (mIndexShiftKey[i] >= mKeys.size()) {
				continue;
			}
			id1 = R.drawable.key_qwerty_shift;
			id2 = R.drawable.key_qwerty_shift_b;
			if (sw) {
				id1 = R.drawable.key_qwerty_shiftlock;
				id2 = R.drawable.key_qwerty_shiftlock_b;
			}
			key = mKeys.get(mIndexShiftKey[i]);
			d = mContext.getResources().getDrawable(id1);
			if (d != null) d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
			key.icon = d;
			d = mContext.getResources().getDrawable(id2);
			if (d != null) d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
			key.iconPreview = d;
		}
	}
	
	public boolean getShiftKeyIndicator() {
		if (mNumIndexShiftKey > 0) {
			if (mIndexShiftKey[0] >= 0) {
				final Key key = mKeys.get(mIndexShiftKey[0]);
				return key.on;
			}
		}
		return false;
	}
	
	public void setShiftKeyIndicator(boolean sw) {
		for (int i=0; i<mNumIndexShiftKey; i++) {
			if (mIndexShiftKey[i] >= mKeys.size()) {
				continue;
			}
			final Key key = mKeys.get(mIndexShiftKey[i]);
			key.on = sw;
		}
	}

	public boolean getAltKeyIndicator() {
		if (mNumIndexAltKey > 0) {
			if (mIndexAltKey[0] >= 0) {
				final Key key = mKeys.get(mIndexAltKey[0]);
				return key.on;
			}
		}
		return false;
	}

	public void setAltKeyIndicator(boolean sw) {
		for (int i=0; i<mNumIndexAltKey; i++) {
			if (mIndexAltKey[i] >= mKeys.size()) {
				continue;
			}
			final Key key = mKeys.get(mIndexAltKey[i]);
			key.on = sw;
		}
	}

	public boolean getCtrlKeyIndicator() {
		if (mNumIndexCtrlKey > 0) {
			if (mIndexCtrlKey[0] >= 0) {
				final Key key = mKeys.get(mIndexCtrlKey[0]);
				return key.on;
			}
		}
		return false;
	}

	public void setCtrlKeyIndicator(boolean sw) {
		for (int i=0; i<mNumIndexCtrlKey; i++) {
			if (mIndexCtrlKey[i] >= mKeys.size()) {
				continue;
			}
			final Key key = mKeys.get(mIndexCtrlKey[i]);
			key.on = sw;
		}
	}

	public boolean getSelKeyIndicator() {
		if (mIndexSelKey >= 0) {
			final Key key = mKeys.get(mIndexSelKey);
			return key.on;
		}
		return false;
	}

	public void setSelKeyIndicator(boolean sw) {
		if (mIndexSelKey >= 0) {
			final Key key = mKeys.get(mIndexSelKey);
			key.on = sw;
		}
	}

	public int setSelKeyIndex() {
		return mIndexSelKey;
	}

	public int setReverseKeyIndex() {
		return 0;
	}
}
