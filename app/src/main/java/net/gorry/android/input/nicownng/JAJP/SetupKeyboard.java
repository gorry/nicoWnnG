/*******************************************************************************************
 * setup softkeyboard (base)
 */

package net.gorry.android.input.nicownng.JAJP;

import java.util.HashMap;

import net.gorry.android.input.nicownng.DefaultSoftKeyboard;
import net.gorry.android.input.nicownng.MyHeightKeyboard;
import net.gorry.android.input.nicownng.NicoWnnG;
import net.gorry.android.input.nicownng.R;
import android.inputmethodservice.Keyboard;
import android.util.Log;

abstract class SetupKeyboard {
	protected static final int SELECT_SUBTEN_PORT_KEY_TABLE = 1;
	protected static final int SELECT_SUBTEN_LAND_KEY_TABLE = 2;
	protected static final int SELECT_SUBTEN_PORT_KEY_TABLE_2 = 3;
	protected static final int SELECT_SUBTEN_LAND_KEY_TABLE_2 = 4;
	protected static final int SELECT_FLICK_PORT_KEY_TABLE = 5;
	protected static final int SELECT_FLICK_LAND_KEY_TABLE = 6;
	protected static final int SELECT_PORT_KEY_TABLE = 7;
	protected static final int SELECT_LAND_KEY_TABLE = 8;
	protected static final int SELECT_SUBTEN_PORT_KEY_TABLE_3 = 9;
	protected static final int SELECT_SUBTEN_LAND_KEY_TABLE_3 = 10;
	
	/******************************************************************************************/
	/*********************************
	 *
	 */
	public final MyHeightKeyboard[][][] SetupSoftKeyboard(final NicoWnnG parent, final int displaymode, final int keysize, final int flick, final boolean showFlick, final boolean subten, final int subten_mode) {
		final MyHeightKeyboard[][][] nicokeyboard = new MyHeightKeyboard[DefaultSoftKeyboard.NICO_MODE_MAX][DefaultSoftKeyboard.NICO_SLIDE_MODE_MAX][2];
		MyHeightKeyboard[][] k;
		int[] t;
		boolean isPortrait = (DefaultSoftKeyboard.PORTRAIT == displaymode);
		int keymode = DefaultSoftKeyboard.KEYTYPE_12KEY;

		final int modeKeyString = SetupModeKeyString();

		if (subten) {
			keymode = DefaultSoftKeyboard.KEYTYPE_SUBTEN_12KEY;
			if (isPortrait) {
				switch (subten_mode) {
					default:
					case 0:
						t = getSelectedKeyboard(SELECT_SUBTEN_PORT_KEY_TABLE);
						break;
					case 1:
						t = getSelectedKeyboard(SELECT_SUBTEN_PORT_KEY_TABLE_2);
						break;
					case 2:
						t = getSelectedKeyboard(SELECT_SUBTEN_PORT_KEY_TABLE_3);
						break;
				}
			} else {
				switch (subten_mode) {
					default:
					case 0:
						t = getSelectedKeyboard(SELECT_SUBTEN_LAND_KEY_TABLE);
						break;
					case 1:
						t = getSelectedKeyboard(SELECT_SUBTEN_LAND_KEY_TABLE_2);
						break;
					case 2:
						t = getSelectedKeyboard(SELECT_SUBTEN_LAND_KEY_TABLE_3);
						break;
				}
			}
		} else if ((flick != DefaultSoftKeyboard.NICOFLICK_NONE) && showFlick) {
			if (isPortrait) {
				t = getSelectedKeyboard(SELECT_FLICK_PORT_KEY_TABLE);
			} else {
				t = getSelectedKeyboard(SELECT_FLICK_LAND_KEY_TABLE);
			}
		} else {
			if (isPortrait) {
				t = getSelectedKeyboard(SELECT_PORT_KEY_TABLE);
			} else {
				t = getSelectedKeyboard(SELECT_LAND_KEY_TABLE);
			}
		}
		int c = 0;
		{
			k = nicokeyboard[DefaultSoftKeyboard.NICO_MODE_FULL_HIRAGANA];
			k[DefaultSoftKeyboard.NICO_SLIDE_MODE_TOP][0]   = new MyHeightKeyboard(parent, t[c++], keysize, keymode, isPortrait, modeKeyString);
			k[DefaultSoftKeyboard.NICO_SLIDE_MODE_SHIFT][0] = new MyHeightKeyboard(parent, t[c++], keysize, keymode, isPortrait, modeKeyString);
			k[DefaultSoftKeyboard.NICO_SLIDE_MODE_A][0]     = new MyHeightKeyboard(parent, t[c++], keysize, keymode, isPortrait, modeKeyString);
			k[DefaultSoftKeyboard.NICO_SLIDE_MODE_K][0]     = new MyHeightKeyboard(parent, t[c++], keysize, keymode, isPortrait, modeKeyString);
			k[DefaultSoftKeyboard.NICO_SLIDE_MODE_S][0]     = new MyHeightKeyboard(parent, t[c++], keysize, keymode, isPortrait, modeKeyString);
			k[DefaultSoftKeyboard.NICO_SLIDE_MODE_T][0]     = new MyHeightKeyboard(parent, t[c++], keysize, keymode, isPortrait, modeKeyString);
			k[DefaultSoftKeyboard.NICO_SLIDE_MODE_N][0]     = new MyHeightKeyboard(parent, t[c++], keysize, keymode, isPortrait, modeKeyString);
			k[DefaultSoftKeyboard.NICO_SLIDE_MODE_H][0]     = new MyHeightKeyboard(parent, t[c++], keysize, keymode, isPortrait, modeKeyString);
			k[DefaultSoftKeyboard.NICO_SLIDE_MODE_M][0]     = new MyHeightKeyboard(parent, t[c++], keysize, keymode, isPortrait, modeKeyString);
			k[DefaultSoftKeyboard.NICO_SLIDE_MODE_Y][0]     = new MyHeightKeyboard(parent, t[c++], keysize, keymode, isPortrait, modeKeyString);
			k[DefaultSoftKeyboard.NICO_SLIDE_MODE_R][0]     = new MyHeightKeyboard(parent, t[c++], keysize, keymode, isPortrait, modeKeyString);
			k[DefaultSoftKeyboard.NICO_SLIDE_MODE_W][0]     = new MyHeightKeyboard(parent, t[c++], keysize, keymode, isPortrait, modeKeyString);

			k[DefaultSoftKeyboard.NICO_SLIDE_MODE_TOP][1]   = new MyHeightKeyboard(parent, t[c++], keysize, keymode, isPortrait, modeKeyString);
			k[DefaultSoftKeyboard.NICO_SLIDE_MODE_SHIFT][1] = new MyHeightKeyboard(parent, t[c++], keysize, keymode, isPortrait, modeKeyString);
			k[DefaultSoftKeyboard.NICO_SLIDE_MODE_A][1]     = new MyHeightKeyboard(parent, t[c++], keysize, keymode, isPortrait, modeKeyString);
			k[DefaultSoftKeyboard.NICO_SLIDE_MODE_K][1]     = new MyHeightKeyboard(parent, t[c++], keysize, keymode, isPortrait, modeKeyString);
			k[DefaultSoftKeyboard.NICO_SLIDE_MODE_S][1]     = new MyHeightKeyboard(parent, t[c++], keysize, keymode, isPortrait, modeKeyString);
			k[DefaultSoftKeyboard.NICO_SLIDE_MODE_T][1]     = new MyHeightKeyboard(parent, t[c++], keysize, keymode, isPortrait, modeKeyString);
			k[DefaultSoftKeyboard.NICO_SLIDE_MODE_N][1]     = new MyHeightKeyboard(parent, t[c++], keysize, keymode, isPortrait, modeKeyString);
			k[DefaultSoftKeyboard.NICO_SLIDE_MODE_H][1]     = new MyHeightKeyboard(parent, t[c++], keysize, keymode, isPortrait, modeKeyString);
			k[DefaultSoftKeyboard.NICO_SLIDE_MODE_M][1]     = new MyHeightKeyboard(parent, t[c++], keysize, keymode, isPortrait, modeKeyString);
			k[DefaultSoftKeyboard.NICO_SLIDE_MODE_Y][1]     = new MyHeightKeyboard(parent, t[c++], keysize, keymode, isPortrait, modeKeyString);
			k[DefaultSoftKeyboard.NICO_SLIDE_MODE_R][1]     = new MyHeightKeyboard(parent, t[c++], keysize, keymode, isPortrait, modeKeyString);
			k[DefaultSoftKeyboard.NICO_SLIDE_MODE_W][1]     = new MyHeightKeyboard(parent, t[c++], keysize, keymode, isPortrait, modeKeyString);

			k = nicokeyboard[DefaultSoftKeyboard.NICO_MODE_FULL_KATAKANA];
			k[DefaultSoftKeyboard.NICO_SLIDE_MODE_TOP][0]   = new MyHeightKeyboard(parent, t[c++], keysize, keymode, isPortrait);
			k[DefaultSoftKeyboard.NICO_SLIDE_MODE_SHIFT][0] = new MyHeightKeyboard(parent, t[c++], keysize, keymode, isPortrait);
			k[DefaultSoftKeyboard.NICO_SLIDE_MODE_A][0]     = new MyHeightKeyboard(parent, t[c++], keysize, keymode, isPortrait);
			k[DefaultSoftKeyboard.NICO_SLIDE_MODE_K][0]     = new MyHeightKeyboard(parent, t[c++], keysize, keymode, isPortrait);
			k[DefaultSoftKeyboard.NICO_SLIDE_MODE_S][0]     = new MyHeightKeyboard(parent, t[c++], keysize, keymode, isPortrait);
			k[DefaultSoftKeyboard.NICO_SLIDE_MODE_T][0]     = new MyHeightKeyboard(parent, t[c++], keysize, keymode, isPortrait);
			k[DefaultSoftKeyboard.NICO_SLIDE_MODE_N][0]     = new MyHeightKeyboard(parent, t[c++], keysize, keymode, isPortrait);
			k[DefaultSoftKeyboard.NICO_SLIDE_MODE_H][0]     = new MyHeightKeyboard(parent, t[c++], keysize, keymode, isPortrait);
			k[DefaultSoftKeyboard.NICO_SLIDE_MODE_M][0]     = new MyHeightKeyboard(parent, t[c++], keysize, keymode, isPortrait);
			k[DefaultSoftKeyboard.NICO_SLIDE_MODE_Y][0]     = new MyHeightKeyboard(parent, t[c++], keysize, keymode, isPortrait);
			k[DefaultSoftKeyboard.NICO_SLIDE_MODE_R][0]     = new MyHeightKeyboard(parent, t[c++], keysize, keymode, isPortrait);
			k[DefaultSoftKeyboard.NICO_SLIDE_MODE_W][0]     = new MyHeightKeyboard(parent, t[c++], keysize, keymode, isPortrait);

			k[DefaultSoftKeyboard.NICO_SLIDE_MODE_TOP][1]   = new MyHeightKeyboard(parent, t[c++], keysize, keymode, isPortrait);
			k[DefaultSoftKeyboard.NICO_SLIDE_MODE_SHIFT][1] = new MyHeightKeyboard(parent, t[c++], keysize, keymode, isPortrait);
			k[DefaultSoftKeyboard.NICO_SLIDE_MODE_A][1]     = new MyHeightKeyboard(parent, t[c++], keysize, keymode, isPortrait);
			k[DefaultSoftKeyboard.NICO_SLIDE_MODE_K][1]     = new MyHeightKeyboard(parent, t[c++], keysize, keymode, isPortrait);
			k[DefaultSoftKeyboard.NICO_SLIDE_MODE_S][1]     = new MyHeightKeyboard(parent, t[c++], keysize, keymode, isPortrait);
			k[DefaultSoftKeyboard.NICO_SLIDE_MODE_T][1]     = new MyHeightKeyboard(parent, t[c++], keysize, keymode, isPortrait);
			k[DefaultSoftKeyboard.NICO_SLIDE_MODE_N][1]     = new MyHeightKeyboard(parent, t[c++], keysize, keymode, isPortrait);
			k[DefaultSoftKeyboard.NICO_SLIDE_MODE_H][1]     = new MyHeightKeyboard(parent, t[c++], keysize, keymode, isPortrait);
			k[DefaultSoftKeyboard.NICO_SLIDE_MODE_M][1]     = new MyHeightKeyboard(parent, t[c++], keysize, keymode, isPortrait);
			k[DefaultSoftKeyboard.NICO_SLIDE_MODE_Y][1]     = new MyHeightKeyboard(parent, t[c++], keysize, keymode, isPortrait);
			k[DefaultSoftKeyboard.NICO_SLIDE_MODE_R][1]     = new MyHeightKeyboard(parent, t[c++], keysize, keymode, isPortrait);
			k[DefaultSoftKeyboard.NICO_SLIDE_MODE_W][1]     = new MyHeightKeyboard(parent, t[c++], keysize, keymode, isPortrait);

			k = nicokeyboard[DefaultSoftKeyboard.NICO_MODE_HALF_KATAKANA];
			k[DefaultSoftKeyboard.NICO_SLIDE_MODE_TOP][0]   = new MyHeightKeyboard(parent, t[c++], keysize, keymode, isPortrait);
			k[DefaultSoftKeyboard.NICO_SLIDE_MODE_SHIFT][0] = new MyHeightKeyboard(parent, t[c++], keysize, keymode, isPortrait);
			k[DefaultSoftKeyboard.NICO_SLIDE_MODE_A][0]     = new MyHeightKeyboard(parent, t[c++], keysize, keymode, isPortrait);
			k[DefaultSoftKeyboard.NICO_SLIDE_MODE_K][0]     = new MyHeightKeyboard(parent, t[c++], keysize, keymode, isPortrait);
			k[DefaultSoftKeyboard.NICO_SLIDE_MODE_S][0]     = new MyHeightKeyboard(parent, t[c++], keysize, keymode, isPortrait);
			k[DefaultSoftKeyboard.NICO_SLIDE_MODE_T][0]     = new MyHeightKeyboard(parent, t[c++], keysize, keymode, isPortrait);
			k[DefaultSoftKeyboard.NICO_SLIDE_MODE_N][0]     = new MyHeightKeyboard(parent, t[c++], keysize, keymode, isPortrait);
			k[DefaultSoftKeyboard.NICO_SLIDE_MODE_H][0]     = new MyHeightKeyboard(parent, t[c++], keysize, keymode, isPortrait);
			k[DefaultSoftKeyboard.NICO_SLIDE_MODE_M][0]     = new MyHeightKeyboard(parent, t[c++], keysize, keymode, isPortrait);
			k[DefaultSoftKeyboard.NICO_SLIDE_MODE_Y][0]     = new MyHeightKeyboard(parent, t[c++], keysize, keymode, isPortrait);
			k[DefaultSoftKeyboard.NICO_SLIDE_MODE_R][0]     = new MyHeightKeyboard(parent, t[c++], keysize, keymode, isPortrait);
			k[DefaultSoftKeyboard.NICO_SLIDE_MODE_W][0]     = new MyHeightKeyboard(parent, t[c++], keysize, keymode, isPortrait);

			k[DefaultSoftKeyboard.NICO_SLIDE_MODE_TOP][1]   = new MyHeightKeyboard(parent, t[c++], keysize, keymode, isPortrait);
			k[DefaultSoftKeyboard.NICO_SLIDE_MODE_SHIFT][1] = new MyHeightKeyboard(parent, t[c++], keysize, keymode, isPortrait);
			k[DefaultSoftKeyboard.NICO_SLIDE_MODE_A][1]     = new MyHeightKeyboard(parent, t[c++], keysize, keymode, isPortrait);
			k[DefaultSoftKeyboard.NICO_SLIDE_MODE_K][1]     = new MyHeightKeyboard(parent, t[c++], keysize, keymode, isPortrait);
			k[DefaultSoftKeyboard.NICO_SLIDE_MODE_S][1]     = new MyHeightKeyboard(parent, t[c++], keysize, keymode, isPortrait);
			k[DefaultSoftKeyboard.NICO_SLIDE_MODE_T][1]     = new MyHeightKeyboard(parent, t[c++], keysize, keymode, isPortrait);
			k[DefaultSoftKeyboard.NICO_SLIDE_MODE_N][1]     = new MyHeightKeyboard(parent, t[c++], keysize, keymode, isPortrait);
			k[DefaultSoftKeyboard.NICO_SLIDE_MODE_H][1]     = new MyHeightKeyboard(parent, t[c++], keysize, keymode, isPortrait);
			k[DefaultSoftKeyboard.NICO_SLIDE_MODE_M][1]     = new MyHeightKeyboard(parent, t[c++], keysize, keymode, isPortrait);
			k[DefaultSoftKeyboard.NICO_SLIDE_MODE_Y][1]     = new MyHeightKeyboard(parent, t[c++], keysize, keymode, isPortrait);
			k[DefaultSoftKeyboard.NICO_SLIDE_MODE_R][1]     = new MyHeightKeyboard(parent, t[c++], keysize, keymode, isPortrait);
			k[DefaultSoftKeyboard.NICO_SLIDE_MODE_W][1]     = new MyHeightKeyboard(parent, t[c++], keysize, keymode, isPortrait);
			
			if (c != t.length) {
				Log.e("nicoWnnG", "SetupSoftKeyboard(): Table size error");
			}
		}
		return nicokeyboard;
	}

	protected int[] getSelectedKeyboard(final int n) {
		switch (n) {
			case SELECT_SUBTEN_PORT_KEY_TABLE:
				return selectSubTenPortKeyTable;
			case SELECT_SUBTEN_LAND_KEY_TABLE:
				return selectSubTenLandKeyTable;
		}
		return null;
	}

	
	abstract String[][][][] SetupCycleTable();
	abstract HashMap<String, String> SetupReplaceTable(boolean du);
	abstract int SetupIcon();
	abstract int SetupModeKeyString();
	abstract int GetFlickChangeMap(int keymode, int line, int row);
	abstract int GetCycleTableColumns();




	protected static final int selectSubTenLandKeyTable[] = {
		R.xml.key_subten_qwerty_full_hiragana_0,
		R.xml.key_subten_qwerty_full_hiragana_0,
		R.xml.key_subten_qwerty_full_hiragana_0,
		R.xml.key_subten_qwerty_full_hiragana_0,
		R.xml.key_subten_qwerty_full_hiragana_0,
		R.xml.key_subten_qwerty_full_hiragana_0,
		R.xml.key_subten_qwerty_full_hiragana_0,
		R.xml.key_subten_qwerty_full_hiragana_0,
		R.xml.key_subten_qwerty_full_hiragana_0,
		R.xml.key_subten_qwerty_full_hiragana_0,
		R.xml.key_subten_qwerty_full_hiragana_0,
		R.xml.key_subten_qwerty_full_hiragana_0,
		R.xml.key_subten_qwerty_full_hiragana_input_0,
		R.xml.key_subten_qwerty_full_hiragana_input_0,
		R.xml.key_subten_qwerty_full_hiragana_input_0,
		R.xml.key_subten_qwerty_full_hiragana_input_0,
		R.xml.key_subten_qwerty_full_hiragana_input_0,
		R.xml.key_subten_qwerty_full_hiragana_input_0,
		R.xml.key_subten_qwerty_full_hiragana_input_0,
		R.xml.key_subten_qwerty_full_hiragana_input_0,
		R.xml.key_subten_qwerty_full_hiragana_input_0,
		R.xml.key_subten_qwerty_full_hiragana_input_0,
		R.xml.key_subten_qwerty_full_hiragana_input_0,
		R.xml.key_subten_qwerty_full_hiragana_input_0,

		R.xml.key_subten_qwerty_full_katakana_0,
		R.xml.key_subten_qwerty_full_katakana_0,
		R.xml.key_subten_qwerty_full_katakana_0,
		R.xml.key_subten_qwerty_full_katakana_0,
		R.xml.key_subten_qwerty_full_katakana_0,
		R.xml.key_subten_qwerty_full_katakana_0,
		R.xml.key_subten_qwerty_full_katakana_0,
		R.xml.key_subten_qwerty_full_katakana_0,
		R.xml.key_subten_qwerty_full_katakana_0,
		R.xml.key_subten_qwerty_full_katakana_0,
		R.xml.key_subten_qwerty_full_katakana_0,
		R.xml.key_subten_qwerty_full_katakana_0,
		R.xml.key_subten_qwerty_full_katakana_input_0,
		R.xml.key_subten_qwerty_full_katakana_input_0,
		R.xml.key_subten_qwerty_full_katakana_input_0,
		R.xml.key_subten_qwerty_full_katakana_input_0,
		R.xml.key_subten_qwerty_full_katakana_input_0,
		R.xml.key_subten_qwerty_full_katakana_input_0,
		R.xml.key_subten_qwerty_full_katakana_input_0,
		R.xml.key_subten_qwerty_full_katakana_input_0,
		R.xml.key_subten_qwerty_full_katakana_input_0,
		R.xml.key_subten_qwerty_full_katakana_input_0,
		R.xml.key_subten_qwerty_full_katakana_input_0,
		R.xml.key_subten_qwerty_full_katakana_input_0,

		R.xml.key_subten_qwerty_half_katakana_0,
		R.xml.key_subten_qwerty_half_katakana_0,
		R.xml.key_subten_qwerty_half_katakana_0,
		R.xml.key_subten_qwerty_half_katakana_0,
		R.xml.key_subten_qwerty_half_katakana_0,
		R.xml.key_subten_qwerty_half_katakana_0,
		R.xml.key_subten_qwerty_half_katakana_0,
		R.xml.key_subten_qwerty_half_katakana_0,
		R.xml.key_subten_qwerty_half_katakana_0,
		R.xml.key_subten_qwerty_half_katakana_0,
		R.xml.key_subten_qwerty_half_katakana_0,
		R.xml.key_subten_qwerty_half_katakana_0,
		R.xml.key_subten_qwerty_half_katakana_input_0,
		R.xml.key_subten_qwerty_half_katakana_input_0,
		R.xml.key_subten_qwerty_half_katakana_input_0,
		R.xml.key_subten_qwerty_half_katakana_input_0,
		R.xml.key_subten_qwerty_half_katakana_input_0,
		R.xml.key_subten_qwerty_half_katakana_input_0,
		R.xml.key_subten_qwerty_half_katakana_input_0,
		R.xml.key_subten_qwerty_half_katakana_input_0,
		R.xml.key_subten_qwerty_half_katakana_input_0,
		R.xml.key_subten_qwerty_half_katakana_input_0,
		R.xml.key_subten_qwerty_half_katakana_input_0,
		R.xml.key_subten_qwerty_half_katakana_input_0,
	};

	protected static final int selectSubTenPortKeyTable[] = {
		R.xml.key_subten_qwerty_full_hiragana_0,
		R.xml.key_subten_qwerty_full_hiragana_0,
		R.xml.key_subten_qwerty_full_hiragana_0,
		R.xml.key_subten_qwerty_full_hiragana_0,
		R.xml.key_subten_qwerty_full_hiragana_0,
		R.xml.key_subten_qwerty_full_hiragana_0,
		R.xml.key_subten_qwerty_full_hiragana_0,
		R.xml.key_subten_qwerty_full_hiragana_0,
		R.xml.key_subten_qwerty_full_hiragana_0,
		R.xml.key_subten_qwerty_full_hiragana_0,
		R.xml.key_subten_qwerty_full_hiragana_0,
		R.xml.key_subten_qwerty_full_hiragana_0,
		R.xml.key_subten_qwerty_full_hiragana_input_0,
		R.xml.key_subten_qwerty_full_hiragana_input_0,
		R.xml.key_subten_qwerty_full_hiragana_input_0,
		R.xml.key_subten_qwerty_full_hiragana_input_0,
		R.xml.key_subten_qwerty_full_hiragana_input_0,
		R.xml.key_subten_qwerty_full_hiragana_input_0,
		R.xml.key_subten_qwerty_full_hiragana_input_0,
		R.xml.key_subten_qwerty_full_hiragana_input_0,
		R.xml.key_subten_qwerty_full_hiragana_input_0,
		R.xml.key_subten_qwerty_full_hiragana_input_0,
		R.xml.key_subten_qwerty_full_hiragana_input_0,
		R.xml.key_subten_qwerty_full_hiragana_input_0,

		R.xml.key_subten_qwerty_full_katakana_0,
		R.xml.key_subten_qwerty_full_katakana_0,
		R.xml.key_subten_qwerty_full_katakana_0,
		R.xml.key_subten_qwerty_full_katakana_0,
		R.xml.key_subten_qwerty_full_katakana_0,
		R.xml.key_subten_qwerty_full_katakana_0,
		R.xml.key_subten_qwerty_full_katakana_0,
		R.xml.key_subten_qwerty_full_katakana_0,
		R.xml.key_subten_qwerty_full_katakana_0,
		R.xml.key_subten_qwerty_full_katakana_0,
		R.xml.key_subten_qwerty_full_katakana_0,
		R.xml.key_subten_qwerty_full_katakana_0,
		R.xml.key_subten_qwerty_full_katakana_input_0,
		R.xml.key_subten_qwerty_full_katakana_input_0,
		R.xml.key_subten_qwerty_full_katakana_input_0,
		R.xml.key_subten_qwerty_full_katakana_input_0,
		R.xml.key_subten_qwerty_full_katakana_input_0,
		R.xml.key_subten_qwerty_full_katakana_input_0,
		R.xml.key_subten_qwerty_full_katakana_input_0,
		R.xml.key_subten_qwerty_full_katakana_input_0,
		R.xml.key_subten_qwerty_full_katakana_input_0,
		R.xml.key_subten_qwerty_full_katakana_input_0,
		R.xml.key_subten_qwerty_full_katakana_input_0,
		R.xml.key_subten_qwerty_full_katakana_input_0,

		R.xml.key_subten_qwerty_half_katakana_0,
		R.xml.key_subten_qwerty_half_katakana_0,
		R.xml.key_subten_qwerty_half_katakana_0,
		R.xml.key_subten_qwerty_half_katakana_0,
		R.xml.key_subten_qwerty_half_katakana_0,
		R.xml.key_subten_qwerty_half_katakana_0,
		R.xml.key_subten_qwerty_half_katakana_0,
		R.xml.key_subten_qwerty_half_katakana_0,
		R.xml.key_subten_qwerty_half_katakana_0,
		R.xml.key_subten_qwerty_half_katakana_0,
		R.xml.key_subten_qwerty_half_katakana_0,
		R.xml.key_subten_qwerty_half_katakana_0,
		R.xml.key_subten_qwerty_half_katakana_input_0,
		R.xml.key_subten_qwerty_half_katakana_input_0,
		R.xml.key_subten_qwerty_half_katakana_input_0,
		R.xml.key_subten_qwerty_half_katakana_input_0,
		R.xml.key_subten_qwerty_half_katakana_input_0,
		R.xml.key_subten_qwerty_half_katakana_input_0,
		R.xml.key_subten_qwerty_half_katakana_input_0,
		R.xml.key_subten_qwerty_half_katakana_input_0,
		R.xml.key_subten_qwerty_half_katakana_input_0,
		R.xml.key_subten_qwerty_half_katakana_input_0,
		R.xml.key_subten_qwerty_half_katakana_input_0,
		R.xml.key_subten_qwerty_half_katakana_input_0,
	};



}
/******************** end of file ********************/
