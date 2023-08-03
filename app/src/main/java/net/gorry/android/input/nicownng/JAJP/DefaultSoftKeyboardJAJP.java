/*
 * Copyright (C) 2008,2009  OMRON SOFTWARE Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.

 * test message
 */

package net.gorry.android.input.nicownng.JAJP;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import net.gorry.android.input.nicownng.CandidateFilter;
import net.gorry.android.input.nicownng.ComposingText;
import net.gorry.android.input.nicownng.DefaultSoftKeyboard;
import net.gorry.android.input.nicownng.MyHeightKeyboard;
import net.gorry.android.input.nicownng.NicoWnnG;
import net.gorry.android.input.nicownng.NicoWnnGEvent;
import net.gorry.android.input.nicownng.NicoWnnGJAJP;
import net.gorry.android.input.nicownng.R;
import android.content.SharedPreferences;
import android.inputmethodservice.Keyboard;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

/**
 * The default Software Keyboard class for Japanese IME.
 *
 * @author Copyright (C) 2009 OMRON SOFTWARE CO., LTD.  All Rights Reserved.
 */
public abstract class DefaultSoftKeyboardJAJP extends DefaultSoftKeyboard {

	/******************************************************************************************/
	/** Default constructor */
	public DefaultSoftKeyboardJAJP(final NicoWnnG parent) {
		super(parent);
		mCurrentLanguage     = LANG_JA;
		mShiftOn             = KEYBOARD_SHIFT_OFF;
		mCurrentKeyMode      = KEYMODE_JA_FULL_HIRAGANA;
		mCurrentSlide        = NICO_SLIDE_MODE_TOP;
	}

	/** @see net.gorry.android.input.nicownng.DefaultSoftKeyboard#createKeyboards */
	@Override protected void createKeyboards() {
		super.createKeyboards();
	}

	/**
	 *
	 */
	@Override public void checkHiddenKeyboard() {
		super.checkHiddenKeyboard();
	}

	/**
	 * Commit the pre-edit string for committing operation that is not explicit
	 * (ex. when a candidate is selected)
	 */
	protected void commitText() {
		if (!mNoInput) {
			mWnn.onEvent(mWnn.mEventCommitText);
		}
	}

	/** @see net.gorry.android.input.nicownng.DefaultSoftKeyboard#changeKeyboard */
	@Override protected boolean changeKeyboard(final MyHeightKeyboard keyboard) {
		resetKeyRepeat();
		if (mWnn.getAutoForwardToggle12key()) {
			// mWnn.resetAutoForwardToggle12key();
		}
		showPreview(NOT_A_KEY, -1);
		
		final InputConnection connection = mWnn.getCurrentInputConnection();
		if ((connection != null) && (mCurrentKeyboard != null)) {
			if (mCurrentKeyboard.getAltKeyIndicator()) {
				mCurrentKeyboard.setAltKeyIndicator(false);
				KeyEvent event;
				event = new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ALT_LEFT);
				connection.sendKeyEvent(event);
			}
			if (mCurrentKeyboard.getCtrlKeyIndicator()) {
				mCurrentKeyboard.setCtrlKeyIndicator(false);
				KeyEvent event;
				// event = new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_CTRL_LEFT);
				event = new KeyEvent(KeyEvent.ACTION_UP, 113);
				connection.sendKeyEvent(event);
			}
		}
		
		if (keyboard != null) {
			if (mIsInputTypeNull) {
				if (mChangeModeKey != null) {
					mChangeModeKey.popupResId = mPopupResId;
				}
			}

			final List<Keyboard.Key> keys = keyboard.getKeys();
			final int size = keys.size();
			mChangeModeKey = null;
			for (int i=0; i<size; i++) {
				Keyboard.Key key = keys.get(i);
				if ((key.codes[0] == KEYCODE_JP12_TOGGLE_MODE) || (key.codes[0] == KEYCODE_JP12_TOGGLE_MODE2)) {
					mChangeModeKey = key;
					break;
				}
				
			}

			if (mIsInputTypeNull) {
				if (mChangeModeKey != null) {
					mPopupResId = mChangeModeKey.popupResId;
					mChangeModeKey.popupResId = 0;
				}
			}
		}
		
		boolean ret = super.changeKeyboard(keyboard);

		if (Build.VERSION.SDK_INT < 11) {
			if (connection != null) {
				if (!mCurrentKeyboard.getShiftKeyIndicator()) {
					connection.clearMetaKeyStates(KeyEvent.META_SHIFT_ON|KeyEvent.META_SHIFT_LEFT_ON|KeyEvent.META_SHIFT_RIGHT_ON);
				}
				connection.clearMetaKeyStates(KeyEvent.META_ALT_ON|KeyEvent.META_ALT_LEFT_ON|KeyEvent.META_ALT_RIGHT_ON);
				// connection.clearMetaKeyStates(KeyEvent.META_CTRL_ON|KeyEvent.META_CTRL_LEFT_ON|KeyEvent.META_CTRL_RIGHT_ON);
				connection.clearMetaKeyStates(0x00001000|0x00002000|0x00004000);
			}
		}
		mCurrentKeyboard.setSelKeyIndicator(true);

		if (mWnn.getAutoForwardToggle12key()) {
			setReverseKey();
		} else {
			restoreReverseKey();
		}

		return ret;
	}

	/** @see net.gorry.android.input.nicownng.DefaultSoftKeyboard#changeKeyboardType */
	@Override public void changeKeyboardType(final int type) {
		commitText();

		fadePreview();
		resetKeyRepeat();

		m12keyTable[mCurrentKeyMode] = type;
		final MyHeightKeyboard kbd = getTypeChangeKeyboard(type);
		if (kbd != null) {
			/*
			mPrefEditor.putBoolean("opt_enable_qwerty", type == KEYBOARD_QWERTY);
			mPrefEditor.commit();
			 */
			changeKeyboard(kbd);
		}

		setStatusIcon();

		if (m12keyTable[mCurrentKeyMode] == KEYBOARD_12KEY) {
			mWnn.onEvent(mWnn.mEventChangeMode12Key);
		} else {
			mWnn.onEvent(mWnn.mEventChangeModeQwerty);
		}
	}

	@Override
	public void setPreferences(final SharedPreferences pref, final EditorInfo editor) {
		super.setPreferences(pref,  editor);

		mPreferenceKeyMode = INVALID_KEYMODE;
		mLimitedKeyMode = null;

		final int inputType = editor.inputType;
		int maskedInputType = (inputType & InputType.TYPE_MASK_VARIATION);
		if ((inputType & InputType.TYPE_MASK_CLASS) == InputType.TYPE_CLASS_TEXT) {
			switch (maskedInputType) {
				case InputType.TYPE_TEXT_VARIATION_PASSWORD:
				case InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS:
				case InputType.TYPE_TEXT_VARIATION_URI:
					if (mUseEmailKana) {
						maskedInputType = InputType.TYPE_TEXT_VARIATION_NORMAL;
					}
					break;
			}
		}
		switch (inputType & InputType.TYPE_MASK_CLASS) {
			case InputType.TYPE_CLASS_TEXT:
				switch (maskedInputType) {
					case InputType.TYPE_TEXT_VARIATION_PERSON_NAME:
						break;

					case InputType.TYPE_TEXT_VARIATION_PASSWORD:
						if (mNoAlphaMode) {
							mPreferenceKeyMode = KEYMODE_JA_HALF_NUMBER;
							mLimitedKeyMode = new int[] {KEYMODE_JA_HALF_NUMBER};
						} else if (mNoNumberMode) {
							mPreferenceKeyMode = KEYMODE_JA_HALF_ALPHABET;
							mLimitedKeyMode = new int[] {KEYMODE_JA_HALF_ALPHABET};
						} else {
							mPreferenceKeyMode = KEYMODE_JA_HALF_ALPHABET;
							mLimitedKeyMode = new int[] {KEYMODE_JA_HALF_ALPHABET, KEYMODE_JA_HALF_NUMBER};
						}
						break;

					case InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS:
						if (mNoAlphaMode) {
							mPreferenceKeyMode = KEYMODE_JA_HALF_NUMBER;
							mLimitedKeyMode = new int[] {KEYMODE_JA_HALF_NUMBER};
						} else if (mNoNumberMode) {
							mPreferenceKeyMode = KEYMODE_JA_HALF_ALPHABET;
							mLimitedKeyMode = new int[] {KEYMODE_JA_HALF_ALPHABET};
						} else {
							mPreferenceKeyMode = KEYMODE_JA_HALF_ALPHABET;
							mLimitedKeyMode = new int[] {KEYMODE_JA_HALF_ALPHABET, KEYMODE_JA_HALF_NUMBER};
						}
						break;

					case InputType.TYPE_TEXT_VARIATION_URI:
						if (mNoAlphaMode) {
							mPreferenceKeyMode = KEYMODE_JA_HALF_NUMBER;
						} else if (mNoNumberMode) {
							mPreferenceKeyMode = KEYMODE_JA_HALF_ALPHABET;
						} else {
							mPreferenceKeyMode = KEYMODE_JA_HALF_ALPHABET;
						}
						break;

					case InputType.TYPE_TEXT_VARIATION_POSTAL_ADDRESS:
						break;

					case InputType.TYPE_TEXT_VARIATION_PHONETIC:
						break;

					default:
						break;
				}
				break;

			default:
				break;
		}
	}
	
	protected void setPreferencesJAJP(final SharedPreferences pref, final EditorInfo editor) {
		final int inputType = editor.inputType;
		if (mHardKeyboardHidden) {
			/*
			if (inputType == InputType.TYPE_NULL) {
				if (!mIsInputTypeNull) {
					mIsInputTypeNull = true;
					if (mChangeModeKey != null) {
						mPopupResId = mChangeModeKey.popupResId;
						mChangeModeKey.popupResId = 0;
					}
				}
				return;
			}
			*/

			if (mIsInputTypeNull) {
				mIsInputTypeNull = false;
				if (mChangeModeKey != null) {
					mChangeModeKey.popupResId = mPopupResId;
				}
			}
		}

		mEnableAutoCaps = mWnn.getOrientPrefBoolean(pref, "auto_caps", false);
		mNoInput = true;
		// mDisableKeyInput = false;
		mCapsLock = false;

		boolean restartkey = false;
		final boolean oldKana12key = mKana12Key;
		final boolean oldAlpha12key = mAlpha12Key;
		final boolean oldNum12key = mNum12Key;

		loadOption(pref);

		if (true == mGetNoFlipScreen) {
			mNoFlipScreen = true;
		}
		else{
			mNoFlipScreen = false;
		}

		if ((oldKana12key != mKana12Key) || (oldAlpha12key != mAlpha12Key) || (oldNum12key != mNum12Key)) {
			restartkey = true;
			setM12KeyTable();
		}
		if (inputType != mLastInputType) {
			setDefaultKeyboard();
			mLastInputType = inputType;
		}
		else if (true == restartkey) {
			setDefaultKeyboard();
		}

		setStatusIcon();
		setShiftByEditorInfo();

	}
	

	/** @see net.gorry.android.input.nicownng.DefaultSoftKeyboard#onUpdateState */
	@Override public void onUpdateState(final NicoWnnG parent) {
		super.onUpdateState(parent);
		if (!mCapsLock) {
			setShiftByEditorInfo();
			if ((true == mNoInput) && (true == mNicoFirst)) {
				resetNicoKeyboard();
			}
		}
	}

	abstract int[] getModeCycleTable();
	
	/**
	 * Change to the next input mode
	 */
	@Override public void nextKeyMode() {
		nextKeyModeCore(1);
	}

	public void nextKeyModeBack() {
		nextKeyModeCore(-1);
	}

	public void nextKeyModeTop() {
		nextKeyModeCore(0);
	}

	private void nextKeyModeCore(int sw) {
		/* Search the current mode in the toggle table */
		int[] modeCycleTable = getModeCycleTable();
		boolean found = false;
		int index = 0;
		if (sw != 0) {
			if (sw < 0) sw = -1;
			if (sw > 0) sw = 1;
			if (mInputModeNext == 1) {
				sw = -sw;
			}
			for (index = 0; index < modeCycleTable.length; index++) {
				if (modeCycleTable[index] == mCurrentKeyMode) {
					found = true;
					break;
				}
			}
		}

		if (!found) {
			/* If the current mode not exists, set the default mode */
			setDefaultKeyboard();
			NicoWnnGJAJP.getInstance().setLastInputMode(0);
		} else {
			/* If the current mode exists, set the next input mode */
			final int size = modeCycleTable.length;
			int keyMode = INVALID_KEYMODE;
			for (int i = 0; i < size; i++) {
				index = (index + sw);
				if (index < 0) index = size-1;
				index %= size;
				keyMode = filterKeyMode(modeCycleTable[index]);
				if (keyMode == INVALID_KEYMODE) {
					continue;
				}
				if (m12keyTable[keyMode] == KEYBOARD_QWERTY) {
					if (mNoAlphaMode) {
						if (modeCycleTable[index] == KEYMODE_JA_HALF_ALPHABET) {
							continue;
						}
					}
					if (mNoNumberMode) {
						if (modeCycleTable[index] == KEYMODE_JA_HALF_NUMBER) {
							continue;
						}
					}
				}
				break;
			}

			if (keyMode != INVALID_KEYMODE) {
				changeKeyMode(keyMode);
				NicoWnnGJAJP.getInstance().setLastInputMode(index);
			}
		}
	}

	protected int[] getPortraitKeyTable() {
		final int[] keyTable;
		if (mQwertyMatrixMode) {
			switch (mQwertyKanaMode) {
				case KANAMODE_ROMAN:
				default:
					keyTable = selectQwertyPortKeyTable;
					break;
				case KANAMODE_JIS:
					keyTable = selectKanaJisPortKeyTable;
					break;
				case KANAMODE_50ON:
					keyTable = selectKana50onPortKeyTable;
					break;
				case KANAMODE_ROMAN_MINI:
					keyTable = selectMiniQwertyPortKeyTable;
					break;
				case KANAMODE_ROMAN2:
					keyTable = selectQwerty2PortKeyTable;
					break;
				case KANAMODE_JIS2:
					keyTable = selectKanaJis2PortKeyTable;
					break;
				case KANAMODE_50ON2:
					keyTable = selectKana50on2PortKeyTable;
					break;
				case KANAMODE_ROMAN_MINI2:
					keyTable = selectMiniQwerty2PortKeyTable;
					break;
				case KANAMODE_ROMAN_COMPACT:
					keyTable = selectCompactQwertyPortKeyTable;
					break;
			}
		} else {
			switch (mQwertyKanaMode) {
				case KANAMODE_ROMAN:
				default:
					keyTable = selectQwertyPortSlantKeyTable;
					break;
				case KANAMODE_JIS:
					keyTable = selectKanaJisPortSlantKeyTable;
					break;
				case KANAMODE_50ON:
					keyTable = selectKana50onPortSlantKeyTable;
					break;
				case KANAMODE_ROMAN_MINI:
					keyTable = selectMiniQwertyPortSlantKeyTable;
					break;
				case KANAMODE_ROMAN2:
					keyTable = selectQwerty2PortSlantKeyTable;
					break;
				case KANAMODE_JIS2:
					keyTable = selectKanaJis2PortSlantKeyTable;
					break;
				case KANAMODE_50ON2:
					keyTable = selectKana50on2PortSlantKeyTable;
					break;
				case KANAMODE_ROMAN_MINI2:
					keyTable = selectMiniQwerty2PortSlantKeyTable;
					break;
				case KANAMODE_ROMAN_COMPACT:
					keyTable = selectCompactQwertyPortSlantKeyTable;
					break;
			}
		}
		return keyTable;
	}

	protected int[] getLandscapeKeyTable() {
		final int[] keyTable;
		if (mQwertyMatrixMode) {
			switch (mQwertyKanaMode) {
				case KANAMODE_ROMAN:
				default:
					keyTable = selectQwertyLandKeyTable;
					break;
				case KANAMODE_JIS:
					keyTable = selectKanaJisLandKeyTable;
					break;
				case KANAMODE_50ON:
					keyTable = selectKana50onLandKeyTable;
					break;
				case KANAMODE_ROMAN_MINI:
					keyTable = selectMiniQwertyLandKeyTable;
					break;
				case KANAMODE_ROMAN2:
					keyTable = selectQwerty2LandKeyTable;
					break;
				case KANAMODE_JIS2:
					keyTable = selectKanaJis2LandKeyTable;
					break;
				case KANAMODE_50ON2:
					keyTable = selectKana50on2LandKeyTable;
					break;
				case KANAMODE_ROMAN_MINI2:
					keyTable = selectMiniQwerty2LandKeyTable;
					break;
				case KANAMODE_ROMAN_COMPACT:
					keyTable = selectCompactQwertyLandKeyTable;
					break;
			}
		} else {
			switch (mQwertyKanaMode) {
				case KANAMODE_ROMAN:
				default:
					keyTable = selectQwertyLandSlantKeyTable;
					break;
				case KANAMODE_JIS:
					keyTable = selectKanaJisLandSlantKeyTable;
					break;
				case KANAMODE_50ON:
					keyTable = selectKana50onLandSlantKeyTable;
					break;
				case KANAMODE_ROMAN_MINI:
					keyTable = selectMiniQwertyLandSlantKeyTable;
					break;
				case KANAMODE_ROMAN2:
					keyTable = selectQwerty2LandSlantKeyTable;
					break;
				case KANAMODE_JIS2:
					keyTable = selectKanaJis2LandSlantKeyTable;
					break;
				case KANAMODE_50ON2:
					keyTable = selectKana50on2LandSlantKeyTable;
					break;
				case KANAMODE_ROMAN_MINI2:
					keyTable = selectMiniQwerty2LandSlantKeyTable;
					break;
				case KANAMODE_ROMAN_COMPACT:
					keyTable = selectCompactQwertyLandSlantKeyTable;
					break;
			}
		}
		return keyTable;
	}
	
	/**
	 * Convert the key code to the index of table
	 * <br>
	 * @param index     The key code
	 * @return          The index of the toggle table for input
	 */
	@Override public int getTableIndex(final int keyCode) {
		final int index =
			(keyCode == KEYCODE_JP12_1)     ?  0 :
			(keyCode == KEYCODE_JP12_2)     ?  1 :
			(keyCode == KEYCODE_JP12_3)     ?  2 :
			(keyCode == KEYCODE_JP12_4)     ?  3 :
			(keyCode == KEYCODE_JP12_5)     ?  4 :
			(keyCode == KEYCODE_JP12_6)     ?  5 :
			(keyCode == KEYCODE_JP12_7)     ?  6 :
			(keyCode == KEYCODE_JP12_8)     ?  7 :
			(keyCode == KEYCODE_JP12_9)     ?  8 :
			(keyCode == KEYCODE_JP12_0)     ?  9 :
			(keyCode == KEYCODE_JP12_SHARP) ? 10 :
			(keyCode == KEYCODE_JP12_ASTER) ? 11 :
			(keyCode == KEYCODE_NEWKEY_0)   ? 10 :
			(keyCode == KEYCODE_NEWKEY_1)   ? 11 :
			(keyCode == KEYCODE_NEWKEY_2)   ? 12 :
			(keyCode == KEYCODE_NEWKEY_3)   ? 13 :
			(keyCode == KEYCODE_NEWKEY_4)   ? 14 :
			0;

		return index;
	}

	protected int checkTableIndex(final int keyCode) {
		final int index =
			(keyCode == KEYCODE_JP12_1)     ?  0 :
			(keyCode == KEYCODE_JP12_2)     ?  1 :
			(keyCode == KEYCODE_JP12_3)     ?  2 :
			(keyCode == KEYCODE_JP12_4)     ?  3 :
			(keyCode == KEYCODE_JP12_5)     ?  4 :
			(keyCode == KEYCODE_JP12_6)     ?  5 :
			(keyCode == KEYCODE_JP12_7)     ?  6 :
			(keyCode == KEYCODE_JP12_8)     ?  7 :
			(keyCode == KEYCODE_JP12_9)     ?  8 :
			(keyCode == KEYCODE_JP12_0)     ?  9 :
			-1;
		return index;
	}

	/**
	 * Get the toggle table for input that is appropriate in current mode.
	 *
	 * @return      The toggle table for input
	 */
	protected String[][] getCycleTable() {
		String[][] cycleTable = null;
		switch (mCurrentKeyMode) {
			case KEYMODE_JA_FULL_HIRAGANA:
				cycleTable = JP_FULL_HIRAGANA_CYCLE_TABLE;
				break;

			case KEYMODE_JA_FULL_KATAKANA:
				cycleTable = JP_FULL_KATAKANA_CYCLE_TABLE;
				break;

			case KEYMODE_JA_FULL_ALPHABET:
				cycleTable = JP_FULL_ALPHABET_CYCLE_TABLE;
				break;

			case KEYMODE_JA_FULL_NUMBER:
			case KEYMODE_JA_HALF_NUMBER:
				/* Because these modes belong to direct input group, No toggle table exists */
				break;

			case KEYMODE_JA_HALF_ALPHABET:
				cycleTable = JP_HALF_ALPHABET_CYCLE_TABLE;
				break;

			case KEYMODE_JA_HALF_KATAKANA:
				cycleTable = JP_HALF_KATAKANA_CYCLE_TABLE;
				break;

			default:
				break;
		}
		return cycleTable;
	}

	/**
	 * Get the replace table that is appropriate in current mode.
	 *
	 * @return      The replace table
	 */
	protected HashMap getReplaceTable(final boolean du) {
		HashMap hashTable = null;
		switch (mCurrentKeyMode) {
			case KEYMODE_JA_FULL_HIRAGANA:
				hashTable = (du ? JP_FULL_HIRAGANA_REPLACE_TABLE_2 : JP_FULL_HIRAGANA_REPLACE_TABLE_1);
				break;
			case KEYMODE_JA_FULL_KATAKANA:
				hashTable = (du ? JP_FULL_KATAKANA_REPLACE_TABLE_2 : JP_FULL_KATAKANA_REPLACE_TABLE_1);
				break;

			case KEYMODE_JA_FULL_ALPHABET:
				hashTable = JP_FULL_ALPHABET_REPLACE_TABLE;
				break;

			case KEYMODE_JA_FULL_NUMBER:
			case KEYMODE_JA_HALF_NUMBER:
				/* Because these modes belong to direct input group, No replacing table exists */
				break;

			case KEYMODE_JA_HALF_ALPHABET:
				hashTable = JP_HALF_ALPHABET_REPLACE_TABLE;
				break;

			case KEYMODE_JA_HALF_KATAKANA:
				hashTable = (du ? JP_HALF_KATAKANA_REPLACE_TABLE_2 : JP_HALF_KATAKANA_REPLACE_TABLE_1);
				break;

			default:
				break;
		}
		return hashTable;
	}

	/**
	 * Get the replace table that is appropriate in current mode.
	 *
	 * @return      The replace table
	 */
	protected HashMap getReplaceDakutenTable() {
		return JP_FULL_DAKUTEN_REPLACE_TABLE;
	}

	protected HashMap getReplaceHandakutenTable() {
		return JP_FULL_HANDAKUTEN_REPLACE_TABLE;
	}

	protected HashMap getReplaceKanaSmallTable() {
		return JP_KANASMALL_REPLACE_TABLE;
	}

	/** @see net.gorry.android.input.nicownng.DefaultSoftKeyboard#setHardKeyboardHidden */
	@Override public void setHardKeyboardHidden(final boolean hidden, final boolean hardkey) {
		if (mWnn != null) {
			if (!hidden) {
				mWnn.onEvent(mWnn.mEventChangeModeQwerty);
			}

			if (mHardKeyboardHidden != hidden) {
				if ((mLimitedKeyMode != null) || (
						(mCurrentKeyMode != KEYMODE_JA_FULL_NICO) &&
						(mCurrentKeyMode != KEYMODE_JA_FULL_NICO_KATAKANA) &&
						(mCurrentKeyMode != KEYMODE_JA_HALF_NICO_KATAKANA) &&
						(mCurrentKeyMode != KEYMODE_JA_FULL_HIRAGANA) &&
						(mCurrentKeyMode != KEYMODE_JA_HALF_ALPHABET)
				)) {
					mLastInputType = InputType.TYPE_NULL;
					if (mWnn.isInputViewShown()) {
						setDefaultKeyboard();
					}
				}
			}
		}
		super.setHardKeyboardHidden(hidden, hardkey);
	}

	/**
	 * Change the key-mode to the allowed one which is restricted
	 *  by the text input field or the type of the keyboard.
	 * @param keyMode The key-mode
	 * @return the key-mode allowed
	 */
	protected int filterKeyMode(final int keyMode) {
		int targetMode = keyMode;
		final int[] limits = mLimitedKeyMode;

		/* restrict by the type of the text field */
		if (limits != null) {
			boolean hasAccepted = false;
			boolean hasRequiredChange = true;
			final int size = limits.length;
			final int nowMode = mCurrentKeyMode;

			for (int i = 0; i < size; i++) {
				if (targetMode == limits[i]) {
					hasAccepted = true;
					break;
				}
				if (nowMode == limits[i]) {
					hasRequiredChange = false;
				}
			}

			if (!hasAccepted) {
				if (hasRequiredChange) {
					targetMode = mLimitedKeyMode[0];
				} else {
					targetMode = INVALID_KEYMODE;
				}
			}
		}

		return targetMode;
	}

	/*
	 * reset nicotouch keyboard
	 */
	@Override public void resetNicoKeyboard() {
		final MyHeightKeyboard newKeyboard = getShiftChangeKeyboard(KEYBOARD_SHIFT_OFF);
		if (newKeyboard != null) {
			mShiftOn = KEYBOARD_SHIFT_OFF;
			changeKeyboard(newKeyboard);
		}
		mNicoFirst = false;
		mNicoFlick = false;
		toggleShiftLock(0);
	}

	@Override public String[] convertFlickToKeyString(int flickdir) {
		final int col = getTableIndex(mPrevInputKeyCode);
		final int row = flickdir;
		String[][] cycleTable = null;
		if (mCurrentKeyMode == KEYMODE_JA_FULL_HIRAGANA) {
			cycleTable = JP_FLICK_FULL_HIRAGANA_CYCLE_TABLE[col];
		}
		else if (mCurrentKeyMode == KEYMODE_JA_FULL_KATAKANA) {
			cycleTable = JP_FLICK_FULL_KATAKANA_CYCLE_TABLE[col];
		}
		else if (mCurrentKeyMode == KEYMODE_JA_HALF_KATAKANA) {
			cycleTable = JP_FLICK_HALF_KATAKANA_CYCLE_TABLE[col];
		}
		else if (mCurrentKeyMode == KEYMODE_JA_HALF_ALPHABET) {
			cycleTable = JP_FLICK_HALF_ALPHABET_CYCLE_TABLE[col];
		}
		else if (mCurrentKeyMode == KEYMODE_JA_FULL_ALPHABET) {
			cycleTable = JP_FLICK_FULL_ALPHABET_CYCLE_TABLE[col];
		}
		if (null != cycleTable) {
			return cycleTable[row];
		}
		return null;
	}


	/**
	 */
	@Override public void onPress(final int primaryCode) {
		super.onPress(primaryCode);
	}
	/**
	 */
	@Override public void onRelease(final int primaryCode) {
		super.onRelease(primaryCode);
	} // onRelease

	public int convertCharacterToKeyCode(int charCode) {
		if ((charCode > 0) && (charCode < CHARACTER_TO_KEYCODE_TABLE.length)) {
			return CHARACTER_TO_KEYCODE_TABLE[charCode];
		}
		return 0;
	}

	private static final int[] CHARACTER_TO_KEYCODE_TABLE = {
		0, 0, 0, 0, 0, 0, 0, 0, 0, KeyEvent.KEYCODE_TAB, KeyEvent.KEYCODE_ENTER, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		/* " " 0x20 */ KeyEvent.KEYCODE_SPACE,
		/* "!" 0x21 */ 0,
		/* """ 0x22 */ 0,
		/* "#" 0x23 */ KeyEvent.KEYCODE_POUND,
		/* "$" 0x24 */ 0,
		/* "%" 0x25 */ 0,
		/* "&" 0x26 */ 0,
		/* "'" 0x27 */ KeyEvent.KEYCODE_APOSTROPHE,
		/* "(" 0x28 */ 0,
		/* ")" 0x29 */ 0,
		/* "*" 0x2a */ KeyEvent.KEYCODE_STAR,
		/* "+" 0x2b */ KeyEvent.KEYCODE_PLUS,
		/* "," 0x2c */ 0,
		/* "-" 0x2d */ KeyEvent.KEYCODE_MINUS,
		/* "." 0x2e */ 0,
		/* "/" 0x2f */ KeyEvent.KEYCODE_SLASH,
		/* "0" 0x30 */ KeyEvent.KEYCODE_0,
		/* "1" 0x31 */ KeyEvent.KEYCODE_1,
		/* "2" 0x32 */ KeyEvent.KEYCODE_2,
		/* "3" 0x33 */ KeyEvent.KEYCODE_3,
		/* "4" 0x34 */ KeyEvent.KEYCODE_4,
		/* "5" 0x35 */ KeyEvent.KEYCODE_5,
		/* "6" 0x36 */ KeyEvent.KEYCODE_6,
		/* "7" 0x37 */ KeyEvent.KEYCODE_7,
		/* "8" 0x38 */ KeyEvent.KEYCODE_8,
		/* "9" 0x39 */ KeyEvent.KEYCODE_9,
		/* ":" 0x3a */ 0,
		/* ";" 0x3b */ KeyEvent.KEYCODE_SEMICOLON,
		/* "<" 0x3c */ 0,
		/* "=" 0x3d */ KeyEvent.KEYCODE_EQUALS,
		/* ">" 0x3e */ 0,
		/* "?" 0x3f */ 0,
		/* "@" 0x40 */ KeyEvent.KEYCODE_AT,
		/* "A" 0x41 */ KeyEvent.KEYCODE_A,
		/* "B" 0x42 */ KeyEvent.KEYCODE_B,
		/* "C" 0x43 */ KeyEvent.KEYCODE_C,
		/* "D" 0x44 */ KeyEvent.KEYCODE_D,
		/* "E" 0x45 */ KeyEvent.KEYCODE_E,
		/* "F" 0x46 */ KeyEvent.KEYCODE_F,
		/* "G" 0x47 */ KeyEvent.KEYCODE_G,
		/* "H" 0x48 */ KeyEvent.KEYCODE_H,
		/* "I" 0x49 */ KeyEvent.KEYCODE_I,
		/* "J" 0x4a */ KeyEvent.KEYCODE_J,
		/* "K" 0x4b */ KeyEvent.KEYCODE_K,
		/* "L" 0x4c */ KeyEvent.KEYCODE_L,
		/* "M" 0x4d */ KeyEvent.KEYCODE_M,
		/* "N" 0x4e */ KeyEvent.KEYCODE_N,
		/* "O" 0x4f */ KeyEvent.KEYCODE_O,
		/* "P" 0x50 */ KeyEvent.KEYCODE_P,
		/* "Q" 0x51 */ KeyEvent.KEYCODE_Q,
		/* "R" 0x52 */ KeyEvent.KEYCODE_R,
		/* "S" 0x53 */ KeyEvent.KEYCODE_S,
		/* "T" 0x54 */ KeyEvent.KEYCODE_T,
		/* "U" 0x55 */ KeyEvent.KEYCODE_U,
		/* "V" 0x56 */ KeyEvent.KEYCODE_V,
		/* "W" 0x57 */ KeyEvent.KEYCODE_W,
		/* "X" 0x58 */ KeyEvent.KEYCODE_X,
		/* "Y" 0x59 */ KeyEvent.KEYCODE_Y,
		/* "Z" 0x5a */ KeyEvent.KEYCODE_Z,
		/* "[" 0x5b */ KeyEvent.KEYCODE_LEFT_BRACKET,
		/* "\" 0x5c */ KeyEvent.KEYCODE_BACKSLASH,
		/* "]" 0x5d */ KeyEvent.KEYCODE_RIGHT_BRACKET,
		/* "^" 0x5e */ 0,
		/* "_" 0x5f */ 0,
		/* "`" 0x60 */ KeyEvent.KEYCODE_GRAVE,
		/* "a" 0x61 */ KeyEvent.KEYCODE_A,
		/* "b" 0x62 */ KeyEvent.KEYCODE_B,
		/* "c" 0x63 */ KeyEvent.KEYCODE_C,
		/* "d" 0x64 */ KeyEvent.KEYCODE_D,
		/* "e" 0x65 */ KeyEvent.KEYCODE_E,
		/* "f" 0x66 */ KeyEvent.KEYCODE_F,
		/* "g" 0x67 */ KeyEvent.KEYCODE_G,
		/* "h" 0x68 */ KeyEvent.KEYCODE_H,
		/* "i" 0x69 */ KeyEvent.KEYCODE_I,
		/* "j" 0x6a */ KeyEvent.KEYCODE_J,
		/* "k" 0x6b */ KeyEvent.KEYCODE_K,
		/* "l" 0x6c */ KeyEvent.KEYCODE_L,
		/* "m" 0x6d */ KeyEvent.KEYCODE_M,
		/* "n" 0x6e */ KeyEvent.KEYCODE_N,
		/* "o" 0x6f */ KeyEvent.KEYCODE_O,
		/* "p" 0x70 */ KeyEvent.KEYCODE_P,
		/* "q" 0x71 */ KeyEvent.KEYCODE_Q,
		/* "r" 0x72 */ KeyEvent.KEYCODE_R,
		/* "s" 0x73 */ KeyEvent.KEYCODE_S,
		/* "t" 0x74 */ KeyEvent.KEYCODE_T,
		/* "u" 0x75 */ KeyEvent.KEYCODE_U,
		/* "v" 0x76 */ KeyEvent.KEYCODE_V,
		/* "w" 0x77 */ KeyEvent.KEYCODE_W,
		/* "x" 0x78 */ KeyEvent.KEYCODE_X,
		/* "y" 0x79 */ KeyEvent.KEYCODE_Y,
		/* "z" 0x7a */ KeyEvent.KEYCODE_Z,
		/* "{" 0x7b */ 0,
		/* "|" 0x7c */ 0,
		/* "}" 0x7d */ 0,
		/* "~" 0x7e */ 0,
		/* " " 0x7f */ 0,
	};

	/** Toggle cycle table for full-width HIRAGANA */
	private static final String[][] JP_FULL_HIRAGANA_CYCLE_TABLE = {
		{"\u3042", "\u3044", "\u3046", "\u3048", "\u304a", "\u3041", "\u3043", "\u3045", "\u3047", "\u3049"},
		{"\u304b", "\u304d", "\u304f", "\u3051", "\u3053"},
		{"\u3055", "\u3057", "\u3059", "\u305b", "\u305d"},
		{"\u305f", "\u3061", "\u3064", "\u3066", "\u3068", "\u3063"},
		{"\u306a", "\u306b", "\u306c", "\u306d", "\u306e"},
		{"\u306f", "\u3072", "\u3075", "\u3078", "\u307b"},
		{"\u307e", "\u307f", "\u3080", "\u3081", "\u3082"},
		{"\u3084", "\u3086", "\u3088", "\u3083", "\u3085", "\u3087"},
		{"\u3089", "\u308a", "\u308b", "\u308c", "\u308d"},
		{"\u308f", "\u3092", "\u3093", "\u308e", "\u30fc"},
		{"\u3001", "\u3002", "\uff1f", "\uff01", "\u30fb", "\u3000"},
	};

	/** Replace table for full-width HIRAGANA */
	private static final HashMap<String, String> JP_FULL_HIRAGANA_REPLACE_TABLE_1 = new HashMap<String, String>() {{
		put("\u3042", "\u3041"); // HIRAGANA A -> LA
		put("\u3044", "\u3043"); // HIRAGANA I -> LI
		put("\u3048", "\u3047"); // HIRAGANA E -> LE
		put("\u304a", "\u3049"); // HIRAGANA O -> LO

		put("\u3041", "\u3042"); // HIRAGANA LA -> A
		put("\u3043", "\u3044"); // HIRAGANA LI -> I
		put("\u3047", "\u3048"); // HIRAGANA LE -> E
		put("\u3049", "\u304a"); // HIRAGANA LO -> O

		put("\u304b", "\u304c"); // HIRAGANA KA -> GA
		put("\u304d", "\u304e"); // HIRAGANA KI -> GI
		put("\u304f", "\u3050"); // HIRAGANA KU -> GU
		put("\u3051", "\u3052"); // HIRAGANA KE -> GE
		put("\u3053", "\u3054"); // HIRAGANA KO -> GO
		put("\u3055", "\u3056"); // HIRAGANA SA -> ZA
		put("\u3057", "\u3058"); // HIRAGANA SI -> ZI
		put("\u3059", "\u305a"); // HIRAGANA SU -> ZU
		put("\u305b", "\u305c"); // HIRAGANA SE -> ZE
		put("\u305d", "\u305e"); // HIRAGANA SO -> ZO
		put("\u305f", "\u3060"); // HIRAGANA TA -> DA
		put("\u3061", "\u3062"); // HIRAGANA TI -> DI
		put("\u3066", "\u3067"); // HIRAGANA TE -> DE
		put("\u3068", "\u3069"); // HIRAGANA TO -> DO

		put("\u304c", "\u304b"); // HIRAGANA GA -> KA
		put("\u304e", "\u304d"); // HIRAGANA GI -> KI
		put("\u3050", "\u304f"); // HIRAGANA GU -> KU
		put("\u3052", "\u3051"); // HIRAGANA GE -> KE
		put("\u3054", "\u3053"); // HIRAGANA GO -> KO
		put("\u3056", "\u3055"); // HIRAGANA ZA -> SA
		put("\u3058", "\u3057"); // HIRAGANA ZI -> SI
		put("\u305a", "\u3059"); // HIRAGANA ZU -> SU
		put("\u305c", "\u305b"); // HIRAGANA ZE -> SE
		put("\u305e", "\u305d"); // HIRAGANA ZO -> SO
		put("\u3060", "\u305f"); // HIRAGANA DA -> TA
		put("\u3062", "\u3061"); // HIRAGANA DI -> TI
		put("\u3067", "\u3066"); // HIRAGANA DE -> TE
		put("\u3069", "\u3068"); // HIRAGANA DO -> TO

		put("\u306f", "\u3070"); // HIRAGANA HA -> BA
		put("\u3072", "\u3073"); // HIRAGANA HI -> BI
		put("\u3075", "\u3076"); // HIRAGANA HU -> BU
		put("\u3078", "\u3079"); // HIRAGANA HE -> BE
		put("\u307b", "\u307c"); // HIRAGANA HO -> BO

		put("\u3070", "\u3071"); // HIRAGANA BA -> PA
		put("\u3073", "\u3074"); // HIRAGANA BI -> PI
		put("\u3076", "\u3077"); // HIRAGANA BU -> PU
		put("\u3079", "\u307a"); // HIRAGANA BE -> PE
		put("\u307c", "\u307d"); // HIRAGANA BO -> PO

		put("\u30d1", "\u30cf"); // KATAKANA PA -> HA
		put("\u30d4", "\u30d2"); // KATAKANA PI -> HI
		put("\u30d7", "\u30d5"); // KATAKANA PU -> HU
		put("\u30da", "\u30d8"); // KATAKANA PE -> HE
		put("\u30dd", "\u30db"); // KATAKANA PO -> HO

		put("\u3084", "\u3083"); // HIRAGANA YA -> LYA
		put("\u3086", "\u3085"); // HIRAGANA YU -> LYU
		put("\u3088", "\u3087"); // HIRAGANA YO -> LYO
		put("\u3083", "\u3084"); // HIRAGANA LYA -> YA
		put("\u3085", "\u3086"); // HIRAGANA LYU -> YU
		put("\u3087", "\u3088"); // HIRAGANA LYO -> YO

		put("\u3046", "\u3045"); // HIRAGANA U -> LU
		put("\u3045", "\u3094"); // HIRAGANA LU -> VU
		put("\u3094", "\u3046"); // HIRAGANA VU -> U

		put("\u3064", "\u3063"); // HIRAGANA TU -> LTU
		put("\u3063", "\u3065"); // HIRAGANA LTU -> DU
		put("\u3065", "\u3064"); // HIRAGANA DU -> TU

		put("\u308f", "\u308e"); // HIRAGANA WA -> LWA
		put("\u308e", "\u308f"); // HIRAGANA LWA -> WA

		put("\u309b", "\u309c"); // DAKUTEN -> HANDAKUTEN
		put("\u309c", "\u309b"); // HANDAKUTEN -> DAKUTEN
	}};

	private static final HashMap<String, String> JP_FULL_HIRAGANA_REPLACE_TABLE_2 = new HashMap<String, String>() {{
		put("\u3042", "\u3041"); // HIRAGANA A -> LA
		put("\u3044", "\u3043"); // HIRAGANA I -> LI
		put("\u3048", "\u3047"); // HIRAGANA E -> LE
		put("\u304a", "\u3049"); // HIRAGANA O -> LO

		put("\u3041", "\u3042"); // HIRAGANA LA -> A
		put("\u3043", "\u3044"); // HIRAGANA LI -> I
		put("\u3047", "\u3048"); // HIRAGANA LE -> E
		put("\u3049", "\u304a"); // HIRAGANA LO -> O

		put("\u304b", "\u304c"); // HIRAGANA KA -> GA
		put("\u304d", "\u304e"); // HIRAGANA KI -> GI
		put("\u304f", "\u3050"); // HIRAGANA KU -> GU
		put("\u3051", "\u3052"); // HIRAGANA KE -> GE
		put("\u3053", "\u3054"); // HIRAGANA KO -> GO
		put("\u3055", "\u3056"); // HIRAGANA SA -> ZA
		put("\u3057", "\u3058"); // HIRAGANA SI -> ZI
		put("\u3059", "\u305a"); // HIRAGANA SU -> ZU
		put("\u305b", "\u305c"); // HIRAGANA SE -> ZE
		put("\u305d", "\u305e"); // HIRAGANA SO -> ZO
		put("\u305f", "\u3060"); // HIRAGANA TA -> DA
		put("\u3061", "\u3062"); // HIRAGANA TI -> DI
		put("\u3066", "\u3067"); // HIRAGANA TE -> DE
		put("\u3068", "\u3069"); // HIRAGANA TO -> DO

		put("\u304c", "\u304b"); // HIRAGANA GA -> KA
		put("\u304e", "\u304d"); // HIRAGANA GI -> KI
		put("\u3050", "\u304f"); // HIRAGANA GU -> KU
		put("\u3052", "\u3051"); // HIRAGANA GE -> KE
		put("\u3054", "\u3053"); // HIRAGANA GO -> KO
		put("\u3056", "\u3055"); // HIRAGANA ZA -> SA
		put("\u3058", "\u3057"); // HIRAGANA ZI -> SI
		put("\u305a", "\u3059"); // HIRAGANA ZU -> SU
		put("\u305c", "\u305b"); // HIRAGANA ZE -> SE
		put("\u305e", "\u305d"); // HIRAGANA ZO -> SO
		put("\u3060", "\u305f"); // HIRAGANA DA -> TA
		put("\u3062", "\u3061"); // HIRAGANA DI -> TI
		put("\u3067", "\u3066"); // HIRAGANA DE -> TE
		put("\u3069", "\u3068"); // HIRAGANA DO -> TO

		put("\u306f", "\u3070"); // HIRAGANA HA -> BA
		put("\u3072", "\u3073"); // HIRAGANA HI -> BI
		put("\u3075", "\u3076"); // HIRAGANA HU -> BU
		put("\u3078", "\u3079"); // HIRAGANA HE -> BE
		put("\u307b", "\u307c"); // HIRAGANA HO -> BO

		put("\u3070", "\u3071"); // HIRAGANA BA -> PA
		put("\u3073", "\u3074"); // HIRAGANA BI -> PI
		put("\u3076", "\u3077"); // HIRAGANA BU -> PU
		put("\u3079", "\u307a"); // HIRAGANA BE -> PE
		put("\u307c", "\u307d"); // HIRAGANA BO -> PO

		put("\u30d1", "\u30cf"); // KATAKANA PA -> HA
		put("\u30d4", "\u30d2"); // KATAKANA PI -> HI
		put("\u30d7", "\u30d5"); // KATAKANA PU -> HU
		put("\u30da", "\u30d8"); // KATAKANA PE -> HE
		put("\u30dd", "\u30db"); // KATAKANA PO -> HO

		put("\u3084", "\u3083"); // HIRAGANA YA -> LYA
		put("\u3086", "\u3085"); // HIRAGANA YU -> LYU
		put("\u3088", "\u3087"); // HIRAGANA YO -> LYO
		put("\u3083", "\u3084"); // HIRAGANA LYA -> YA
		put("\u3085", "\u3086"); // HIRAGANA LYU -> YU
		put("\u3087", "\u3088"); // HIRAGANA LYO -> YO

		put("\u3046", "\u3094"); // HIRAGANA U -> VU
		put("\u3094", "\u3045"); // HIRAGANA VU -> LU
		put("\u3045", "\u3046"); // HIRAGANA LU -> U

		put("\u3064", "\u3065"); // HIRAGANA TU -> DU
		put("\u3065", "\u3063"); // HIRAGANA DU -> LTU
		put("\u3063", "\u3064"); // HIRAGANA LTU -> TU

		put("\u308f", "\u308e"); // HIRAGANA WA -> LWA
		put("\u308e", "\u308f"); // HIRAGANA LWA -> WA

		put("\u309b", "\u309c"); // DAKUTEN -> HANDAKUTEN
		put("\u309c", "\u309b"); // HANDAKUTEN -> DAKUTEN
	}};

	/** Toggle cycle table for full-width KATAKANA */
	private static final String[][] JP_FULL_KATAKANA_CYCLE_TABLE = {
		{"\u30a2", "\u30a4", "\u30a6", "\u30a8", "\u30aa", "\u30a1", "\u30a3",
			"\u30a5", "\u30a7", "\u30a9"},
			{"\u30ab", "\u30ad", "\u30af", "\u30b1", "\u30b3"},
			{"\u30b5", "\u30b7", "\u30b9", "\u30bb", "\u30bd"},
			{"\u30bf", "\u30c1", "\u30c4", "\u30c6", "\u30c8", "\u30c3"},
			{"\u30ca", "\u30cb", "\u30cc", "\u30cd", "\u30ce"},
			{"\u30cf", "\u30d2", "\u30d5", "\u30d8", "\u30db"},
			{"\u30de", "\u30df", "\u30e0", "\u30e1", "\u30e2"},
			{"\u30e4", "\u30e6", "\u30e8", "\u30e3", "\u30e5", "\u30e7"},
			{"\u30e9", "\u30ea", "\u30eb", "\u30ec", "\u30ed"},
			{"\u30ef", "\u30f2", "\u30f3", "\u30ee", "\u30fc"},
			{"\u3001", "\u3002", "\uff1f", "\uff01", "\u30fb", "\u3000"}
	};

	/** Replace table for full-width KATAKANA */
	private static final HashMap<String,String> JP_FULL_KATAKANA_REPLACE_TABLE_1 = new HashMap<String,String>() {{
		put("\u30a2", "\u30a1"); // KATAKANA A -> LA
		put("\u30a4", "\u30a3"); // KATAKANA I -> LI
		put("\u30a8", "\u30a7"); // KATAKANA E -> LE
		put("\u30aa", "\u30a9"); // KATAKANA O -> LO

		put("\u30a1", "\u30a2"); // KATAKANA LA -> A
		put("\u30a3", "\u30a4"); // KATAKANA LI -> I
		put("\u30a7", "\u30a8"); // KATAKANA LE -> E
		put("\u30a9", "\u30aa"); // KATAKANA LO -> O

		put("\u30ab", "\u30ac"); // KATAKANA KA -> GA
		put("\u30ad", "\u30ae"); // KATAKANA KI -> GI
		put("\u30af", "\u30b0"); // KATAKANA KU -> GU
		put("\u30b1", "\u30b2"); // KATAKANA KE -> GE
		put("\u30b3", "\u30b4"); // KATAKANA KO -> GO
		put("\u30b5", "\u30b6"); // KATAKANA SA -> ZA
		put("\u30b7", "\u30b8"); // KATAKANA SI -> ZI
		put("\u30b9", "\u30ba"); // KATAKANA SU -> ZU
		put("\u30bb", "\u30bc"); // KATAKANA SE -> ZE
		put("\u30bd", "\u30be"); // KATAKANA SO -> ZO
		put("\u30bf", "\u30c0"); // KATAKANA TA -> DA
		put("\u30c1", "\u30c2"); // KATAKANA TI -> DI
		put("\u30c6", "\u30c7"); // KATAKANA TE -> DE
		put("\u30c8", "\u30c9"); // KATAKANA TO -> DO

		put("\u30ac", "\u30ab"); // KATAKANA GA -> KA
		put("\u30ae", "\u30ad"); // KATAKANA GI -> KI
		put("\u30b0", "\u30af"); // KATAKANA GU -> KU
		put("\u30b2", "\u30b1"); // KATAKANA GE -> KE
		put("\u30b4", "\u30b3"); // KATAKANA GO -> KO
		put("\u30b6", "\u30b5"); // KATAKANA ZA -> SA
		put("\u30b8", "\u30b7"); // KATAKANA ZI -> SI
		put("\u30ba", "\u30b9"); // KATAKANA ZU -> SU
		put("\u30bc", "\u30bb"); // KATAKANA ZE -> SE
		put("\u30be", "\u30bd"); // KATAKANA ZO -> SO
		put("\u30c0", "\u30bf"); // KATAKANA DA -> TA
		put("\u30c2", "\u30c1"); // KATAKANA DI -> TI
		put("\u30c5", "\u30c4"); // KATAKANA DU -> TU
		put("\u30c7", "\u30c6"); // KATAKANA DE -> TE
		put("\u30c9", "\u30c8"); // KATAKANA DO -> TO

		put("\u30cf", "\u30d0"); // KATAKANA HA -> BA
		put("\u30d2", "\u30d3"); // KATAKANA HI -> BI
		put("\u30d5", "\u30d6"); // KATAKANA HU -> BU
		put("\u30d8", "\u30d9"); // KATAKANA HE -> BE
		put("\u30db", "\u30dc"); // KATAKANA HO -> BO

		put("\u30d0", "\u30d1"); // KATAKANA BA -> PA
		put("\u30d3", "\u30d4"); // KATAKANA BI -> PI
		put("\u30d6", "\u30d7"); // KATAKANA BU -> PU
		put("\u30d9", "\u30da"); // KATAKANA BE -> PE
		put("\u30dc", "\u30dd"); // KATAKANA BO -> PO

		put("\u30d1", "\u30cf"); // KATAKANA PA -> HA
		put("\u30d4", "\u30d2"); // KATAKANA PI -> HI
		put("\u30d7", "\u30d5"); // KATAKANA PU -> HU
		put("\u30da", "\u30d8"); // KATAKANA PE -> HE
		put("\u30dd", "\u30db"); // KATAKANA PO -> HO

		put("\u30e4", "\u30e3"); // KATAKANA YA -> LYA
		put("\u30e6", "\u30e5"); // KATAKANA YU -> LYU
		put("\u30e8", "\u30e7"); // KATAKANA YO -> LYO
		put("\u30e3", "\u30e4"); // KATAKANA LYA -> YA
		put("\u30e5", "\u30e6"); // KATAKANA LYU -> YU
		put("\u30e7", "\u30e8"); // KATAKANA LYO -> YO

		put("\u30a6", "\u30a5"); // KATAKANA U -> LU
		put("\u30a5", "\u30f4"); // KATAKANA LU -> VU
		put("\u30f4", "\u30a6"); // KATAKANA VU -> U

		put("\u30c4", "\u30c3"); // KATAKANA TU -> LTU
		put("\u30c3", "\u30c5"); // KATAKANA LTU -> DU
		put("\u30c5", "\u30c4"); // KATAKANA DU -> TU

		put("\u30ef", "\u30ee"); // KATAKANA WA -> LWA
		put("\u30ee", "\u30ef"); // KATAKANA LWA -> WA

		put("\u309b", "\u309c"); // DAKUTEN -> HANDAKUTEN
		put("\u309c", "\u309b"); // HANDAKUTEN -> DAKUTEN
	}};

	private static final HashMap<String,String> JP_FULL_KATAKANA_REPLACE_TABLE_2 = new HashMap<String,String>() {{
		put("\u30a2", "\u30a1"); // KATAKANA A -> LA
		put("\u30a4", "\u30a3"); // KATAKANA I -> LI
		put("\u30a8", "\u30a7"); // KATAKANA E -> LE
		put("\u30aa", "\u30a9"); // KATAKANA O -> LO

		put("\u30a1", "\u30a2"); // KATAKANA LA -> A
		put("\u30a3", "\u30a4"); // KATAKANA LI -> I
		put("\u30a7", "\u30a8"); // KATAKANA LE -> E
		put("\u30a9", "\u30aa"); // KATAKANA LO -> O

		put("\u30ab", "\u30ac"); // KATAKANA KA -> GA
		put("\u30ad", "\u30ae"); // KATAKANA KI -> GI
		put("\u30af", "\u30b0"); // KATAKANA KU -> GU
		put("\u30b1", "\u30b2"); // KATAKANA KE -> GE
		put("\u30b3", "\u30b4"); // KATAKANA KO -> GO
		put("\u30b5", "\u30b6"); // KATAKANA SA -> ZA
		put("\u30b7", "\u30b8"); // KATAKANA SI -> ZI
		put("\u30b9", "\u30ba"); // KATAKANA SU -> ZU
		put("\u30bb", "\u30bc"); // KATAKANA SE -> ZE
		put("\u30bd", "\u30be"); // KATAKANA SO -> ZO
		put("\u30bf", "\u30c0"); // KATAKANA TA -> DA
		put("\u30c1", "\u30c2"); // KATAKANA TI -> DI
		put("\u30c6", "\u30c7"); // KATAKANA TE -> DE
		put("\u30c8", "\u30c9"); // KATAKANA TO -> DO

		put("\u30ac", "\u30ab"); // KATAKANA GA -> KA
		put("\u30ae", "\u30ad"); // KATAKANA GI -> KI
		put("\u30b0", "\u30af"); // KATAKANA GU -> KU
		put("\u30b2", "\u30b1"); // KATAKANA GE -> KE
		put("\u30b4", "\u30b3"); // KATAKANA GO -> KO
		put("\u30b6", "\u30b5"); // KATAKANA ZA -> SA
		put("\u30b8", "\u30b7"); // KATAKANA ZI -> SI
		put("\u30ba", "\u30b9"); // KATAKANA ZU -> SU
		put("\u30bc", "\u30bb"); // KATAKANA ZE -> SE
		put("\u30be", "\u30bd"); // KATAKANA ZO -> SO
		put("\u30c0", "\u30bf"); // KATAKANA DA -> TA
		put("\u30c2", "\u30c1"); // KATAKANA DI -> TI
		put("\u30c5", "\u30c4"); // KATAKANA DU -> TU
		put("\u30c7", "\u30c6"); // KATAKANA DE -> TE
		put("\u30c9", "\u30c8"); // KATAKANA DO -> TO

		put("\u30cf", "\u30d0"); // KATAKANA HA -> BA
		put("\u30d2", "\u30d3"); // KATAKANA HI -> BI
		put("\u30d5", "\u30d6"); // KATAKANA HU -> BU
		put("\u30d8", "\u30d9"); // KATAKANA HE -> BE
		put("\u30db", "\u30dc"); // KATAKANA HO -> BO

		put("\u30d0", "\u30d1"); // KATAKANA BA -> PA
		put("\u30d3", "\u30d4"); // KATAKANA BI -> PI
		put("\u30d6", "\u30d7"); // KATAKANA BU -> PU
		put("\u30d9", "\u30da"); // KATAKANA BE -> PE
		put("\u30dc", "\u30dd"); // KATAKANA BO -> PO

		put("\u30d1", "\u30cf"); // KATAKANA PA -> HA
		put("\u30d4", "\u30d2"); // KATAKANA PI -> HI
		put("\u30d7", "\u30d5"); // KATAKANA PU -> HU
		put("\u30da", "\u30d8"); // KATAKANA PE -> HE
		put("\u30dd", "\u30db"); // KATAKANA PO -> HO

		put("\u30e4", "\u30e3"); // KATAKANA YA -> LYA
		put("\u30e6", "\u30e5"); // KATAKANA YU -> LYU
		put("\u30e8", "\u30e7"); // KATAKANA YO -> LYO
		put("\u30e3", "\u30e4"); // KATAKANA LYA -> YA
		put("\u30e5", "\u30e6"); // KATAKANA LYU -> YU
		put("\u30e7", "\u30e8"); // KATAKANA LYO -> YO

		put("\u30a6", "\u30f4"); // KATAKANA U -> VU
		put("\u30f4", "\u30a5"); // KATAKANA VU -> LU
		put("\u30a5", "\u30a6"); // KATAKANA LU -> U

		put("\u30c4", "\u30c5"); // KATAKANA TU -> DU
		put("\u30c5", "\u30c3"); // KATAKANA DU -> LTU
		put("\u30c3", "\u30c4"); // KATAKANA LTU -> TU

		put("\u30ef", "\u30ee"); // KATAKANA WA -> LWA
		put("\u30ee", "\u30ef"); // KATAKANA LWA -> WA

		put("\u309b", "\u309c"); // DAKUTEN -> HANDAKUTEN
		put("\u309c", "\u309b"); // HANDAKUTEN -> DAKUTEN
	}};

	/** Toggle cycle table for half-width KATAKANA */
	private static final String[][] JP_HALF_KATAKANA_CYCLE_TABLE = {
		{"\uff71", "\uff72", "\uff73", "\uff74", "\uff75", "\uff67", "\uff68", "\uff69", "\uff6a", "\uff6b"},
		{"\uff76", "\uff77", "\uff78", "\uff79", "\uff7a"},
		{"\uff7b", "\uff7c", "\uff7d", "\uff7e", "\uff7f"},
		{"\uff80", "\uff81", "\uff82", "\uff83", "\uff84", "\uff6f"},
		{"\uff85", "\uff86", "\uff87", "\uff88", "\uff89"},
		{"\uff8a", "\uff8b", "\uff8c", "\uff8d", "\uff8e"},
		{"\uff8f", "\uff90", "\uff91", "\uff92", "\uff93"},
		{"\uff94", "\uff95", "\uff96", "\uff6c", "\uff6d", "\uff6e"},
		{"\uff97", "\uff98", "\uff99", "\uff9a", "\uff9b"},
		{"\uff9c", "\uff66", "\uff9d", "\uff70"},
		{"\uff64", "\uff61", "?", "!", "\uff65", " "},
	};

	/** Replace table for half-width KATAKANA */
	private static final HashMap<String,String> JP_HALF_KATAKANA_REPLACE_TABLE_1 = new HashMap<String,String>() {{
		put("\uff71", "\uff67"); // HALF KATAKANA A -> LA
		put("\uff72", "\uff68"); // HALF KATAKANA I -> LI
		put("\uff74", "\uff6a"); // HALF KATAKANA E -> LE
		put("\uff75", "\uff6b"); // HALF KATAKANA O -> LO

		put("\uff67", "\uff71"); // HALF KATAKANA LA -> A
		put("\uff68", "\uff72"); // HALF KATAKANA LI -> I
		put("\uff6a", "\uff74"); // HALF KATAKANA LE -> E
		put("\uff6b", "\uff75"); // HALF KATAKANA LO -> O

		put("\uff76", "\uff76\uff9e"); // HALF KATAKANA KA -> KA/DAKUTEN
		put("\uff77", "\uff77\uff9e"); // HALF KATAKANA KI -> KI/DAKUTEN
		put("\uff78", "\uff78\uff9e"); // HALF KATAKANA KU -> KU/DAKUTEN
		put("\uff79", "\uff79\uff9e"); // HALF KATAKANA KE -> KE/DAKUTEN
		put("\uff7a", "\uff7a\uff9e"); // HALF KATAKANA KO -> KO/DAKUTEN
		
		put("\uff76\uff9e", "\uff76"); // HALF KATAKANA KA/DAKUTEN -> KA
		put("\uff77\uff9e", "\uff77"); // HALF KATAKANA KI/DAKUTEN -> KI
		put("\uff78\uff9e", "\uff78"); // HALF KATAKANA KU/DAKUTEN -> KU
		put("\uff79\uff9e", "\uff79"); // HALF KATAKANA KE/DAKUTEN -> KE
		put("\uff7a\uff9e", "\uff7a"); // HALF KATAKANA KO/DAKUTEN -> KO

		put("\uff7b", "\uff7b\uff9e"); // HALF KATAKANA SA -> SA/DAKUTEN
		put("\uff7c", "\uff7c\uff9e"); // HALF KATAKANA SI -> SI/DAKUTEN
		put("\uff7d", "\uff7d\uff9e"); // HALF KATAKANA SU -> SU/DAKUTEN
		put("\uff7e", "\uff7e\uff9e"); // HALF KATAKANA SE -> SE/DAKUTEN
		put("\uff7f", "\uff7f\uff9e"); // HALF KATAKANA SO -> SO/DAKUTEN

		put("\uff7b\uff9e", "\uff7b"); // HALF KATAKANA SA/DAKUTEN -> SA
		put("\uff7c\uff9e", "\uff7c"); // HALF KATAKANA SI/DAKUTEN -> SI
		put("\uff7d\uff9e", "\uff7d"); // HALF KATAKANA SU/DAKUTEN -> SU
		put("\uff7e\uff9e", "\uff7e"); // HALF KATAKANA SE/DAKUTEN -> SE
		put("\uff7f\uff9e", "\uff7f"); // HALF KATAKANA SO/DAKUTEN -> SO

		put("\uff80", "\uff80\uff9e"); // HALF KATAKANA TA -> TA/DAKUTEN
		put("\uff81", "\uff81\uff9e"); // HALF KATAKANA TI -> TI/DAKUTEN
		put("\uff83", "\uff83\uff9e"); // HALF KATAKANA TE -> TE/DAKUTEN
		put("\uff84", "\uff84\uff9e"); // HALF KATAKANA TO -> TO/DAKUTEN

		put("\uff80\uff9e", "\uff80"); // HALF KATAKANA TA/DAKUTEN -> TA
		put("\uff81\uff9e", "\uff81"); // HALF KATAKANA TI/DAKUTEN -> TI
		put("\uff83\uff9e", "\uff83"); // HALF KATAKANA TE/DAKUTEN -> TE
		put("\uff84\uff9e", "\uff84"); // HALF KATAKANA TO/DAKUTEN -> TO

		put("\uff8a", "\uff8a\uff9e"); // HALF KATAKANA HA -> HA/DAKUTEN
		put("\uff8b", "\uff8b\uff9e"); // HALF KATAKANA HI -> HI/DAKUTEN
		put("\uff8c", "\uff8c\uff9e"); // HALF KATAKANA HU -> HU/DAKUTEN
		put("\uff8d", "\uff8d\uff9e"); // HALF KATAKANA HE -> HE/DAKUTEN
		put("\uff8e", "\uff8e\uff9e"); // HALF KATAKANA HO -> HO/DAKUTEN
		put("\uff8a\uff9e", "\uff8a\uff9f"); // HALF KATAKANA HA/DAKUTEN -> HA/HANDAKUTEN
		put("\uff8b\uff9e", "\uff8b\uff9f"); // HALF KATAKANA HI/DAKUTEN -> HI/HANDAKUTEN
		put("\uff8c\uff9e", "\uff8c\uff9f"); // HALF KATAKANA HU/DAKUTEN -> HU/HANDAKUTEN
		put("\uff8d\uff9e", "\uff8d\uff9f"); // HALF KATAKANA HE/DAKUTEN -> HE/HANDAKUTEN
		put("\uff8e\uff9e", "\uff8e\uff9f"); // HALF KATAKANA HO/DAKUTEN -> HO/HANDAKUTEN
		put("\uff8a\uff9f", "\uff8a"); // HALF KATAKANA HA/DAKUTEN -> HA
		put("\uff8b\uff9f", "\uff8b"); // HALF KATAKANA HA/DAKUTEN -> HA
		put("\uff8c\uff9f", "\uff8c"); // HALF KATAKANA HA/DAKUTEN -> HA
		put("\uff8d\uff9f", "\uff8d"); // HALF KATAKANA HA/DAKUTEN -> HA
		put("\uff8e\uff9f", "\uff8e"); // HALF KATAKANA HA/DAKUTEN -> HA
		put("\uff94", "\uff6c"); // HALF KATAKANA YA -> LYA
		put("\uff95", "\uff6d"); // HALF KATAKANA YU -> LYU
		put("\uff96", "\uff6e"); // HALF KATAKANA YO -> LYO
		put("\uff6c", "\uff94"); // HALF KATAKANA LYA -> YA
		put("\uff6d", "\uff95"); // HALF KATAKANA LYU -> YU
		put("\uff6e", "\uff96"); // HALF KATAKANA LYO -> YO

		put("\uff82", "\uff6f");       // HALF KATAKANA TU -> LTU
		put("\uff6f", "\uff82\uff9e"); // HALF KATAKANA LTU -> TU/DAKUTEN
		put("\uff82\uff9e", "\uff82"); // HALF KATAKANA TU/DAKUTEN -> TU

		put("\uff73", "\uff69"); // HALF KATAKANA U -> LU
		put("\uff69", "\uff73\uff9e"); // HALF KATAKANA LU -> U/DAKUTEN
		put("\uff73\uff9e", "\uff73"); // HALF KATAKANA VU -> U
	}};

	private static final HashMap<String,String> JP_HALF_KATAKANA_REPLACE_TABLE_2 = new HashMap<String,String>() {{
		put("\uff71", "\uff67"); // HALF KATAKANA A -> LA
		put("\uff72", "\uff68"); // HALF KATAKANA I -> LI
		put("\uff74", "\uff6a"); // HALF KATAKANA E -> LE
		put("\uff75", "\uff6b"); // HALF KATAKANA O -> LO

		put("\uff67", "\uff71"); // HALF KATAKANA LA -> A
		put("\uff68", "\uff72"); // HALF KATAKANA LI -> I
		put("\uff6a", "\uff74"); // HALF KATAKANA LE -> E
		put("\uff6b", "\uff75"); // HALF KATAKANA LO -> O

		put("\uff76", "\uff76\uff9e"); // HALF KATAKANA KA -> KA/DAKUTEN
		put("\uff77", "\uff77\uff9e"); // HALF KATAKANA KI -> KI/DAKUTEN
		put("\uff78", "\uff78\uff9e"); // HALF KATAKANA KU -> KU/DAKUTEN
		put("\uff79", "\uff79\uff9e"); // HALF KATAKANA KE -> KE/DAKUTEN
		put("\uff7a", "\uff7a\uff9e"); // HALF KATAKANA KO -> KO/DAKUTEN
		
		put("\uff76\uff9e", "\uff76"); // HALF KATAKANA KA/DAKUTEN -> KA
		put("\uff77\uff9e", "\uff77"); // HALF KATAKANA KI/DAKUTEN -> KI
		put("\uff78\uff9e", "\uff78"); // HALF KATAKANA KU/DAKUTEN -> KU
		put("\uff79\uff9e", "\uff79"); // HALF KATAKANA KE/DAKUTEN -> KE
		put("\uff7a\uff9e", "\uff7a"); // HALF KATAKANA KO/DAKUTEN -> KO

		put("\uff7b", "\uff7b\uff9e"); // HALF KATAKANA SA -> SA/DAKUTEN
		put("\uff7c", "\uff7c\uff9e"); // HALF KATAKANA SI -> SI/DAKUTEN
		put("\uff7d", "\uff7d\uff9e"); // HALF KATAKANA SU -> SU/DAKUTEN
		put("\uff7e", "\uff7e\uff9e"); // HALF KATAKANA SE -> SE/DAKUTEN
		put("\uff7f", "\uff7f\uff9e"); // HALF KATAKANA SO -> SO/DAKUTEN

		put("\uff7b\uff9e", "\uff7b"); // HALF KATAKANA SA/DAKUTEN -> SA
		put("\uff7c\uff9e", "\uff7c"); // HALF KATAKANA SI/DAKUTEN -> SI
		put("\uff7d\uff9e", "\uff7d"); // HALF KATAKANA SU/DAKUTEN -> SU
		put("\uff7e\uff9e", "\uff7e"); // HALF KATAKANA SE/DAKUTEN -> SE
		put("\uff7f\uff9e", "\uff7f"); // HALF KATAKANA SO/DAKUTEN -> SO

		put("\uff80", "\uff80\uff9e"); // HALF KATAKANA TA -> TA/DAKUTEN
		put("\uff81", "\uff81\uff9e"); // HALF KATAKANA TI -> TI/DAKUTEN
		put("\uff83", "\uff83\uff9e"); // HALF KATAKANA TE -> TE/DAKUTEN
		put("\uff84", "\uff84\uff9e"); // HALF KATAKANA TO -> TO/DAKUTEN

		put("\uff80\uff9e", "\uff80"); // HALF KATAKANA TA/DAKUTEN -> TA
		put("\uff81\uff9e", "\uff81"); // HALF KATAKANA TI/DAKUTEN -> TI
		put("\uff83\uff9e", "\uff83"); // HALF KATAKANA TE/DAKUTEN -> TE
		put("\uff84\uff9e", "\uff84"); // HALF KATAKANA TO/DAKUTEN -> TO

		put("\uff8a", "\uff8a\uff9e"); // HALF KATAKANA HA -> HA/DAKUTEN
		put("\uff8b", "\uff8b\uff9e"); // HALF KATAKANA HI -> HI/DAKUTEN
		put("\uff8c", "\uff8c\uff9e"); // HALF KATAKANA HU -> HU/DAKUTEN
		put("\uff8d", "\uff8d\uff9e"); // HALF KATAKANA HE -> HE/DAKUTEN
		put("\uff8e", "\uff8e\uff9e"); // HALF KATAKANA HO -> HO/DAKUTEN
		put("\uff8a\uff9e", "\uff8a\uff9f"); // HALF KATAKANA HA/DAKUTEN -> HA/HANDAKUTEN
		put("\uff8b\uff9e", "\uff8b\uff9f"); // HALF KATAKANA HI/DAKUTEN -> HI/HANDAKUTEN
		put("\uff8c\uff9e", "\uff8c\uff9f"); // HALF KATAKANA HU/DAKUTEN -> HU/HANDAKUTEN
		put("\uff8d\uff9e", "\uff8d\uff9f"); // HALF KATAKANA HE/DAKUTEN -> HE/HANDAKUTEN
		put("\uff8e\uff9e", "\uff8e\uff9f"); // HALF KATAKANA HO/DAKUTEN -> HO/HANDAKUTEN
		put("\uff8a\uff9f", "\uff8a"); // HALF KATAKANA HA/DAKUTEN -> HA
		put("\uff8b\uff9f", "\uff8b"); // HALF KATAKANA HA/DAKUTEN -> HA
		put("\uff8c\uff9f", "\uff8c"); // HALF KATAKANA HA/DAKUTEN -> HA
		put("\uff8d\uff9f", "\uff8d"); // HALF KATAKANA HA/DAKUTEN -> HA
		put("\uff8e\uff9f", "\uff8e"); // HALF KATAKANA HA/DAKUTEN -> HA
		put("\uff94", "\uff6c"); // HALF KATAKANA YA -> LYA
		put("\uff95", "\uff6d"); // HALF KATAKANA YU -> LYU
		put("\uff96", "\uff6e"); // HALF KATAKANA YO -> LYO
		put("\uff6c", "\uff94"); // HALF KATAKANA LYA -> YA
		put("\uff6d", "\uff95"); // HALF KATAKANA LYU -> YU
		put("\uff6e", "\uff96"); // HALF KATAKANA LYO -> YO

		put("\uff82", "\uff82\uff9e");       // HALF KATAKANA TU -> TU/DAKUTEN
		put("\uff82\uff9e", "\uff6f"); // HALF KATAKANA TU/DAKUTEN -> LTU
		put("\uff6f", "\uff82"); // HALF KATAKANA LTU -> TU

		put("\uff73", "\uff69"); // HALF KATAKANA U -> LU
		put("\uff69", "\uff73\uff9e"); // HALF KATAKANA LU -> U/DAKUTEN
		put("\uff73\uff9e", "\uff73"); // HALF KATAKANA VU -> U
	}};

	/** Toggle cycle table for full-width alphabet */
	private static final String[][] JP_FULL_ALPHABET_CYCLE_TABLE = {
		{"\uff0e", "\uff20", "\uff0d", "\uff3f", "\uff0f", "\uff1a", "\uff5e", "\uff11"},
		{"\uff41", "\uff42", "\uff43", "\uff21", "\uff22", "\uff23", "\uff12"},
		{"\uff44", "\uff45", "\uff46", "\uff24", "\uff25", "\uff26", "\uff13"},
		{"\uff47", "\uff48", "\uff49", "\uff27", "\uff28", "\uff29", "\uff14"},
		{"\uff4a", "\uff4b", "\uff4c", "\uff2a", "\uff2b", "\uff2c", "\uff15"},
		{"\uff4d", "\uff4e", "\uff4f", "\uff2d", "\uff2e", "\uff2f", "\uff16"},
		{"\uff50", "\uff51", "\uff52", "\uff53", "\uff30", "\uff31", "\uff32", "\uff33", "\uff17"},
		{"\uff54", "\uff55", "\uff56", "\uff34", "\uff35", "\uff36", "\uff18"},
		{"\uff57", "\uff58", "\uff59", "\uff5a", "\uff37", "\uff38", "\uff39", "\uff3a", "\uff19"},
		{"\uff0d", "\uff10"},
		{"\uff0c", "\uff0e", "\uff1f", "\uff01", "\u30fb", "\u3000"}
	};

	/** Replace table for full-width alphabet */
	private static final HashMap<String,String> JP_FULL_ALPHABET_REPLACE_TABLE = new HashMap<String,String>() {{
		put("\uff21", "\uff41"); put("\uff22", "\uff42"); put("\uff23", "\uff43"); put("\uff24", "\uff44"); put("\uff25", "\uff45");
		put("\uff41", "\uff21"); put("\uff42", "\uff22"); put("\uff43", "\uff23"); put("\uff44", "\uff24"); put("\uff45", "\uff25");
		put("\uff26", "\uff46"); put("\uff27", "\uff47"); put("\uff28", "\uff48"); put("\uff29", "\uff49"); put("\uff2a", "\uff4a");
		put("\uff46", "\uff26"); put("\uff47", "\uff27"); put("\uff48", "\uff28"); put("\uff49", "\uff29"); put("\uff4a", "\uff2a");
		put("\uff2b", "\uff4b"); put("\uff2c", "\uff4c"); put("\uff2d", "\uff4d"); put("\uff2e", "\uff4e"); put("\uff2f", "\uff4f");
		put("\uff4b", "\uff2b"); put("\uff4c", "\uff2c"); put("\uff4d", "\uff2d"); put("\uff4e", "\uff2e"); put("\uff4f", "\uff2f");
		put("\uff30", "\uff50"); put("\uff31", "\uff51"); put("\uff32", "\uff52"); put("\uff33", "\uff53"); put("\uff34", "\uff54");
		put("\uff50", "\uff30"); put("\uff51", "\uff31"); put("\uff52", "\uff32"); put("\uff53", "\uff33"); put("\uff54", "\uff34");
		put("\uff35", "\uff55"); put("\uff36", "\uff56"); put("\uff37", "\uff57"); put("\uff38", "\uff58"); put("\uff39", "\uff59");
		put("\uff55", "\uff35"); put("\uff56", "\uff36"); put("\uff57", "\uff37"); put("\uff58", "\uff38"); put("\uff59", "\uff39");
		put("\uff3a", "\uff5a");
		put("\uff5a", "\uff3a");
	}};

	/** Toggle cycle table for half-width alphabet */
	private static final String[][] JP_HALF_ALPHABET_CYCLE_TABLE = {
		{".", "@", "-", "_", "/", ":", "~", "1"},
		{"a", "b", "c", "A", "B", "C", "2"},
		{"d", "e", "f", "D", "E", "F", "3"},
		{"g", "h", "i", "G", "H", "I", "4"},
		{"j", "k", "l", "J", "K", "L", "5"},
		{"m", "n", "o", "M", "N", "O", "6"},
		{"p", "q", "r", "s", "P", "Q", "R", "S", "7"},
		{"t", "u", "v", "T", "U", "V", "8"},
		{"w", "x", "y", "z", "W", "X", "Y", "Z", "9"},
		{"-", "0"},
		{",", ".", "?", "!", ";", " "},
		{"*", "\"", " ", "'", " "},
	};

	private static final String[][][] JP_FLICK_FULL_HIRAGANA_CYCLE_TABLE = {
		{{"\u3042"}, {"\u3044"}, {"\u3046"}, {"\u3048"}, {"\u304a"}},
		{{"\u304b"}, {"\u304d"}, {"\u304f"}, {"\u3051"}, {"\u3053"}},
		{{"\u3055"}, {"\u3057"}, {"\u3059"}, {"\u305b"}, {"\u305d"}},
		{{"\u305f"}, {"\u3061"}, {"\u3064"}, {"\u3066"}, {"\u3068"}},
		{{"\u306a"}, {"\u306b"}, {"\u306c"}, {"\u306d"}, {"\u306e"}},
		{{"\u306f"}, {"\u3072"}, {"\u3075"}, {"\u3078"}, {"\u307b"}},
		{{"\u307e"}, {"\u307f"}, {"\u3080"}, {"\u3081"}, {"\u3082"}},
		{{"\u3084"}, {"\u3086"}, {"\u3088"}},
		{{"\u3089"}, {"\u308a"}, {"\u308b"}, {"\u308c"}, {"\u308d"}},
		{{"\u308f"}, {"\u3092"}, {"\u3093"}, {"\u30fc"}},
		{{"\u3001"}, {"\u3002"}, {"\uff1f"}, {"\uff01"}},
		{{"\uff0a"}, {"\u309b"}, {"\uff00"}, {"\u309c"}, {"\uff00"}},
	};
	private static final String[][][] JP_FLICK_FULL_KATAKANA_CYCLE_TABLE = {
		{{"\u30a2"}, {"\u30a4"}, {"\u30a6"}, {"\u30a8"}, {"\u30aa"}},
		{{"\u30ab"}, {"\u30ad"}, {"\u30af"}, {"\u30b1"}, {"\u30b3"}},
		{{"\u30b5"}, {"\u30b7"}, {"\u30b9"}, {"\u30bb"}, {"\u30bd"}},
		{{"\u30bf"}, {"\u30c1"}, {"\u30c4"}, {"\u30c6"}, {"\u30c8"}},
		{{"\u30ca"}, {"\u30cb"}, {"\u30cc"}, {"\u30cd"}, {"\u30ce"}},
		{{"\u30cf"}, {"\u30d2"}, {"\u30d5"}, {"\u30d8"}, {"\u30db"}},
		{{"\u30de"}, {"\u30df"}, {"\u30e0"}, {"\u30e1"}, {"\u30e2"}},
		{{"\u30e4"}, {"\u30e6"}, {"\u30e8"}},
		{{"\u30e9"}, {"\u30ea"}, {"\u30eb"}, {"\u30ec"}, {"\u30ed"}},
		{{"\u30ef"}, {"\u30f2"}, {"\u30f3"}, {"\u30fc"}},
		{{"\u3001"}, {"\u3002"}, {"\uff1f"}, {"\uff01"}},
		{{"\u302a"}, {"\u309b"}, {"\u3000"}, {"\u309c"}, {"\u3000"}},
	};
	private static final String[][][] JP_FLICK_HALF_KATAKANA_CYCLE_TABLE = {
		{{"\uff71"}, {"\uff72"}, {"\uff73"}, {"\uff74"}, {"\uff75"}},
		{{"\uff76"}, {"\uff77"}, {"\uff78"}, {"\uff79"}, {"\uff7a"}},
		{{"\uff7b"}, {"\uff7c"}, {"\uff7d"}, {"\uff7e"}, {"\uff7f"}},
		{{"\uff80"}, {"\uff81"}, {"\uff82"}, {"\uff83"}, {"\uff84"}},
		{{"\uff85"}, {"\uff86"}, {"\uff87"}, {"\uff88"}, {"\uff89"}},
		{{"\uff8a"}, {"\uff8b"}, {"\uff8c"}, {"\uff8d"}, {"\uff8e"}},
		{{"\uff8f"}, {"\uff90"}, {"\uff91"}, {"\uff92"}, {"\uff93"}},
		{{"\uff94"}, {"\uff95"}, {"\uff96"}},
		{{"\uff97"}, {"\uff98"}, {"\uff99"}, {"\uff9a"}, {"\uff9b"}},
		{{"\uff9c"}, {"\uff66"}, {"\uff9d"}, {"\uff70"}},
		{{"\uff64"}, {"\uff61"}, {"?"}, {"!"}},
		{{"*"}, {"\uff9e"}, {" "}, {"\uff9f"}, {" "}},
	};

	private static final String[][][] JP_FLICK_FULL_ALPHABET_CYCLE_TABLE = {
		{ {"\uff0e"}, {"\uff20"}, {"\uff0d"}, {"\uff3f"}, {"\uff0f"}, {"\uff1a"}, {"\uff5e"}, {"\uff11"}},
		{ {"\uff41"}, {"\uff42"}, {"\uff43"}, {"\uff21"}, {"\uff22"}, {"\uff23"}, {"\uff12"}, {"\uff02"}},
		{ {"\uff44"}, {"\uff45"}, {"\uff46"}, {"\uff24"}, {"\uff25"}, {"\uff26"}, {"\uff13"}, {"\uff07"}},
		{ {"\uff47"}, {"\uff48"}, {"\uff49"}, {"\uff27"}, {"\uff28"}, {"\uff29"}, {"\uff14"}, {"\uff1c"}},
		{ {"\uff4a"}, {"\uff4b"}, {"\uff4c"}, {"\uff2a"}, {"\uff2b"}, {"\uff2c"}, {"\uff15"}, {"\uff06"}},
		{ {"\uff4d"}, {"\uff4e"}, {"\uff4f"}, {"\uff2d"}, {"\uff2e"}, {"\uff2f"}, {"\uff16"}, {"\uff1e"}},
		{ {"\uff50"}, {"\uff51"}, {"\uff52"}, {"\uff53"}, {"\uff30"}, {"\uff31"}, {"\uff32"}, {"\uff33"}, {"\uff17"}},
		{ {"\uff54"}, {"\uff55"}, {"\uff56"}, {"\uff34"}, {"\uff35"}, {"\uff36"}, {"\uff18"}, {"\uff04"}},
		{ {"\uff57"}, {"\uff58"}, {"\uff59"}, {"\uff5a"}, {"\uff37"}, {"\uff38"}, {"\uff39"}, {"\uff3a"}, {"\uff19"}},
		{ {"\uff0d"}, {"\uff10"}, {"\uff08"}, {"\uff09"}},
		{ {"\uff0c"}, {"\uff0e"}, {"\uff1f"}, {"\uff01"}, {"\u30fb"}, {"\u3000"}},
		{ {"\uff0a"}, {"\u309b"}, {"\uff00"}, {"\u309c"}, {"\uff00"}},
	};

	private static final String[][][] JP_FLICK_HALF_ALPHABET_CYCLE_TABLE = {
		{ {"."}, {"@"}, {"-"}, {"_"}, {"/"}, {":"}, {"~"}, {"1"}},
		{ {"a"}, {"b"}, {"c"}, {"A"}, {"B"}, {"C"}, {"2"}, {"\""}},
		{ {"d"}, {"e"}, {"f"}, {"D"}, {"E"}, {"F"}, {"3"}, {"\'"}},
		{ {"g"}, {"h"}, {"i"}, {"G"}, {"H"}, {"I"}, {"4"}, {"<"}},
		{ {"j"}, {"k"}, {"l"}, {"J"}, {"K"}, {"L"}, {"5"}, {"&"}},
		{ {"m"}, {"n"}, {"o"}, {"M"}, {"N"}, {"O"}, {"6"}, {">"}},
		{ {"p"}, {"q"}, {"r"}, {"s"}, {"P"}, {"Q"}, {"R"}, {"S"}, {"7"}},
		{ {"t"}, {"u"}, {"v"}, {"T"}, {"U"}, {"V"}, {"8"}, {"$"}},
		{ {"w"}, {"x"}, {"y"}, {"z"}, {"W"}, {"X"}, {"Y"}, {"Z"}, {"9"}},
		{ {"-"}, {"0"}, {"("}, {")"}},
		{ {","}, {"."}, {"?"}, {"!"}, {";"}, {" "}},
		{ {"*"}, {"\uff9e"}, {" "}, {"\uff9f"}, {" "}},
	};

	/** Replace table for half-width alphabet */
	private static final HashMap<String,String> JP_HALF_ALPHABET_REPLACE_TABLE = new HashMap<String,String>() {{
		put("A", "a"); put("B", "b"); put("C", "c"); put("D", "d"); put("E", "e");
		put("a", "A"); put("b", "B"); put("c", "C"); put("d", "D"); put("e", "E");
		put("F", "f"); put("G", "g"); put("H", "h"); put("I", "i"); put("J", "j");
		put("f", "F"); put("g", "G"); put("h", "H"); put("i", "I"); put("j", "J");
		put("K", "k"); put("L", "l"); put("M", "m"); put("N", "n"); put("O", "o");
		put("k", "K"); put("l", "L"); put("m", "M"); put("n", "N"); put("o", "O");
		put("P", "p"); put("Q", "q"); put("R", "r"); put("S", "s"); put("T", "t");
		put("p", "P"); put("q", "Q"); put("r", "R"); put("s", "S"); put("t", "T");
		put("U", "u"); put("V", "v"); put("W", "w"); put("X", "x"); put("Y", "y");
		put("u", "U"); put("v", "V"); put("w", "W"); put("x", "X"); put("y", "Y");
		put("Z", "z");
		put("z", "Z");
	}};

	/** Replace table for full-width KANA DAKUTEN */
	private static final HashMap<String, String> JP_FULL_DAKUTEN_REPLACE_TABLE = new HashMap<String, String>() {{
		put("\u3046", "\u3094"); // HIRAGANA U -> VU
		put("\u304b", "\u304c"); // HIRAGANA KA -> GA
		put("\u304d", "\u304e"); // HIRAGANA KI -> GI
		put("\u304f", "\u3050"); // HIRAGANA KU -> GU
		put("\u3051", "\u3052"); // HIRAGANA KE -> GE
		put("\u3053", "\u3054"); // HIRAGANA KO -> GO
		put("\u3055", "\u3056"); // HIRAGANA SA -> ZA
		put("\u3057", "\u3058"); // HIRAGANA SI -> ZI
		put("\u3059", "\u305a"); // HIRAGANA SU -> ZU
		put("\u305b", "\u305c"); // HIRAGANA SE -> ZE
		put("\u305d", "\u305e"); // HIRAGANA SO -> ZO
		put("\u305f", "\u3060"); // HIRAGANA TA -> DA
		put("\u3061", "\u3062"); // HIRAGANA TI -> DI
		put("\u3064", "\u3065"); // HIRAGANA TU -> DU
		put("\u3066", "\u3067"); // HIRAGANA TE -> DE
		put("\u3068", "\u3069"); // HIRAGANA TO -> DO
		put("\u306f", "\u3070"); // HIRAGANA HA -> BA
		put("\u3072", "\u3073"); // HIRAGANA HI -> BI
		put("\u3075", "\u3076"); // HIRAGANA HU -> BU
		put("\u3078", "\u3079"); // HIRAGANA HE -> BE
		put("\u307b", "\u307c"); // HIRAGANA HO -> BO

		put("\u3094", "\u3046"); // HIRAGANA VU -> U
		put("\u304c", "\u304b"); // HIRAGANA GA -> KA
		put("\u304e", "\u304d"); // HIRAGANA GI -> KI
		put("\u3050", "\u304f"); // HIRAGANA GU -> KU
		put("\u3052", "\u3051"); // HIRAGANA GE -> KE
		put("\u3054", "\u3053"); // HIRAGANA GO -> KO
		put("\u3056", "\u3055"); // HIRAGANA ZA -> SA
		put("\u3058", "\u3057"); // HIRAGANA ZI -> SI
		put("\u305a", "\u3059"); // HIRAGANA ZU -> SU
		put("\u305c", "\u305b"); // HIRAGANA ZE -> SE
		put("\u305e", "\u305d"); // HIRAGANA ZO -> SO
		put("\u3060", "\u305f"); // HIRAGANA DA -> TA
		put("\u3062", "\u3061"); // HIRAGANA DI -> TI
		put("\u3065", "\u3064"); // HIRAGANA DU -> TU
		put("\u3067", "\u3066"); // HIRAGANA DE -> TE
		put("\u3069", "\u3068"); // HIRAGANA DO -> TO
		put("\u3070", "\u306f"); // HIRAGANA BA -> HA
		put("\u3073", "\u3072"); // HIRAGANA BI -> HI
		put("\u3076", "\u3075"); // HIRAGANA BU -> HU
		put("\u3079", "\u3078"); // HIRAGANA BE -> HE
		put("\u307c", "\u307b"); // HIRAGANA BO -> HO

		put("\u3071", "\u3070"); // HIRAGANA PA -> BA
		put("\u3074", "\u3073"); // HIRAGANA PI -> BI
		put("\u3077", "\u3076"); // HIRAGANA PU -> BU
		put("\u307a", "\u3079"); // HIRAGANA PE -> BE
		put("\u307d", "\u307c"); // HIRAGANA PO -> BO

		put("\u30a6", "\u30f4"); // KATAKANA U  -> VU
		put("\u30ab", "\u30ac"); // KATAKANA KA -> GA
		put("\u30ad", "\u30ae"); // KATAKANA KI -> GI
		put("\u30af", "\u30b0"); // KATAKANA KU -> GU
		put("\u30b1", "\u30b2"); // KATAKANA KE -> GE
		put("\u30b3", "\u30b4"); // KATAKANA KO -> GO
		put("\u30b5", "\u30b6"); // KATAKANA SA -> ZA
		put("\u30b7", "\u30b8"); // KATAKANA SI -> ZI
		put("\u30b9", "\u30ba"); // KATAKANA SU -> ZU
		put("\u30bb", "\u30bc"); // KATAKANA SE -> ZE
		put("\u30bd", "\u30be"); // KATAKANA SO -> ZO
		put("\u30bf", "\u30c0"); // KATAKANA TA -> DA
		put("\u30c1", "\u30c2"); // KATAKANA TI -> DI
		put("\u30c4", "\u30c5"); // KATAKANA TU -> DU
		put("\u30c6", "\u30c7"); // KATAKANA TE -> DE
		put("\u30c8", "\u30c9"); // KATAKANA TO -> DO
		put("\u30cf", "\u30d0"); // KATAKANA HA -> BA
		put("\u30d2", "\u30d3"); // KATAKANA HI -> BI
		put("\u30d5", "\u30d6"); // KATAKANA HU -> BU
		put("\u30d8", "\u30d9"); // KATAKANA HE -> BE
		put("\u30db", "\u30dc"); // KATAKANA HO -> BO

		put("\u30f4", "\u30a6"); // KATAKANA VU -> U
		put("\u30ac", "\u30ab"); // KATAKANA GA -> KA
		put("\u30ae", "\u30ad"); // KATAKANA GI -> KI
		put("\u30b0", "\u30af"); // KATAKANA GU -> KU
		put("\u30b2", "\u30b1"); // KATAKANA GE -> KE
		put("\u30b4", "\u30b3"); // KATAKANA GO -> KO
		put("\u30b6", "\u30b5"); // KATAKANA ZA -> SA
		put("\u30b8", "\u30b7"); // KATAKANA ZI -> SI
		put("\u30ba", "\u30b9"); // KATAKANA ZU -> SU
		put("\u30bc", "\u30bb"); // KATAKANA ZE -> SE
		put("\u30be", "\u30bd"); // KATAKANA ZO -> SO
		put("\u30c0", "\u30bf"); // KATAKANA DA -> TA
		put("\u30c2", "\u30c1"); // KATAKANA DI -> TI
		put("\u30c5", "\u30c4"); // KATAKANA DU -> TU
		put("\u30c7", "\u30c6"); // KATAKANA DE -> TE
		put("\u30c9", "\u30c8"); // KATAKANA DO -> TO
		put("\u30d0", "\u30cf"); // KATAKANA BA -> HA
		put("\u30d3", "\u30d2"); // KATAKANA BI -> HI
		put("\u30d6", "\u30d5"); // KATAKANA BU -> HU
		put("\u30d9", "\u30d8"); // KATAKANA BE -> HE
		put("\u30dc", "\u30db"); // KATAKANA BO -> HO

		put("\u30d1", "\u30d0"); // KATAKANA PA -> BA
		put("\u30d4", "\u30d3"); // KATAKANA PI -> BI
		put("\u30d7", "\u30d6"); // KATAKANA PU -> BU
		put("\u30da", "\u30d9"); // KATAKANA PE -> BE
		put("\u30dd", "\u30dc"); // KATAKANA PO -> BO
	}};

	/** Replace table for full-width KANA HANDAKUTEN */
	private static final HashMap<String,String> JP_FULL_HANDAKUTEN_REPLACE_TABLE = new HashMap<String,String>() {{
		put("\u306f", "\u3071"); // HIRAGANA HA -> PA
		put("\u3072", "\u3074"); // HIRAGANA HI -> PI
		put("\u3075", "\u3077"); // HIRAGANA HU -> PU
		put("\u3078", "\u307a"); // HIRAGANA HE -> PE
		put("\u307b", "\u307d"); // HIRAGANA HO -> PO
		put("\u3071", "\u306f"); // HIRAGANA PA -> HA
		put("\u3074", "\u3072"); // HIRAGANA PI -> HI
		put("\u3077", "\u3075"); // HIRAGANA PU -> HU
		put("\u307a", "\u3078"); // HIRAGANA PE -> HE
		put("\u307d", "\u307b"); // HIRAGANA PO -> HO
		put("\u3070", "\u3071"); // HIRAGANA BA -> PA
		put("\u3073", "\u3074"); // HIRAGANA BI -> PI
		put("\u3076", "\u3077"); // HIRAGANA BU -> PU
		put("\u3079", "\u307a"); // HIRAGANA BE -> PE
		put("\u307c", "\u307d"); // HIRAGANA BO -> PO

		put("\u30cf", "\u30d1"); // KATAKANA HA -> PA
		put("\u30d2", "\u30d4"); // KATAKANA HI -> PI
		put("\u30d5", "\u30d7"); // KATAKANA HU -> PU
		put("\u30d8", "\u30da"); // KATAKANA HE -> PE
		put("\u30db", "\u30dd"); // KATAKANA HO -> PO
		put("\u30d1", "\u30cf"); // KATAKANA PA -> HA
		put("\u30d4", "\u30d2"); // KATAKANA PI -> HI
		put("\u30d7", "\u30d5"); // KATAKANA PU -> HU
		put("\u30da", "\u30d8"); // KATAKANA PE -> HE
		put("\u30dd", "\u30db"); // KATAKANA PO -> HO
		put("\u30d0", "\u30d1"); // KATAKANA BA -> PA
		put("\u30d3", "\u30d4"); // KATAKANA BI -> PI
		put("\u30d6", "\u30d7"); // KATAKANA BU -> PU
		put("\u30d9", "\u30da"); // KATAKANA BE -> PE
		put("\u30dc", "\u30dd"); // KATAKANA BO -> PO
	}};

	/** Replace table for full-width KANA SMALL */
	private static final HashMap<String, String> JP_KANASMALL_REPLACE_TABLE = new HashMap<String, String>() {{
		put("\u3042", "\u3041"); // HIRAGANA A -> LA
		put("\u3044", "\u3043"); // HIRAGANA I -> LI
		put("\u3046", "\u3045"); // HIRAGANA U -> LU
		put("\u3048", "\u3047"); // HIRAGANA E -> LE
		put("\u304a", "\u3049"); // HIRAGANA O -> LO
		put("\u3041", "\u3042"); // HIRAGANA LA -> A
		put("\u3043", "\u3044"); // HIRAGANA LI -> I
		put("\u3045", "\u3096"); // HIRAGANA LU -> U
		put("\u3047", "\u3048"); // HIRAGANA LE -> E
		put("\u3049", "\u304a"); // HIRAGANA LO -> O

		put("\u3084", "\u3083"); // HIRAGANA YA -> LYA
		put("\u3086", "\u3085"); // HIRAGANA YU -> LYU
		put("\u3088", "\u3087"); // HIRAGANA YO -> LYO
		put("\u3083", "\u3084"); // HIRAGANA LYA -> YA
		put("\u3085", "\u3086"); // HIRAGANA LYU -> YU
		put("\u3087", "\u3088"); // HIRAGANA LYO -> YO

		put("\u3064", "\u3063"); // HIRAGANA TU -> LTU
		put("\u3063", "\u3064"); // HIRAGANA LTU -> TU

		put("\u308f", "\u308e"); // HIRAGANA WA -> LWA
		put("\u308e", "\u308f"); // HIRAGANA LWA -> WA

		put("\u30a2", "\u30a1"); // KATAKANA A -> LA
		put("\u30a4", "\u30a3"); // KATAKANA I -> LI
		put("\u30a6", "\u30a5"); // KATAKANA U -> LU
		put("\u30a8", "\u30a7"); // KATAKANA E -> LE
		put("\u30aa", "\u30a9"); // KATAKANA O -> LO
		put("\u30a1", "\u30a2"); // KATAKANA LA -> A
		put("\u30a3", "\u30a4"); // KATAKANA LI -> I
		put("\u30a5", "\u30f6"); // KATAKANA LU -> U
		put("\u30a7", "\u30a8"); // KATAKANA LE -> E
		put("\u30a9", "\u30aa"); // KATAKANA LO -> O

		put("\u30e4", "\u30e3"); // KATAKANA YA -> LYA
		put("\u30e6", "\u30e5"); // KATAKANA YU -> LYU
		put("\u30e8", "\u30e7"); // KATAKANA YO -> LYO
		put("\u30e3", "\u30e4"); // KATAKANA LYA -> YA
		put("\u30e5", "\u30e6"); // KATAKANA LYU -> YU
		put("\u30e7", "\u30e8"); // KATAKANA LYO -> YO

		put("\u30c4", "\u30c3"); // KATAKANA TU -> LTU
		put("\u30c3", "\u30c4"); // KATAKANA LTU -> TU

		put("\u30ef", "\u30ee"); // KATAKANA WA -> LWA
		put("\u30ee", "\u30ef"); // KATAKANA LWA -> WA

		put("\uff71", "\uff67"); // HALF KATAKANA A -> LA
		put("\uff72", "\uff68"); // HALF KATAKANA I -> LI
		put("\uff73", "\uff69"); // HALF KATAKANA U -> LU
		put("\uff74", "\uff6a"); // HALF KATAKANA E -> LE
		put("\uff75", "\uff6b"); // HALF KATAKANA O -> LO
		put("\uff67", "\uff71"); // HALF KATAKANA LA -> A
		put("\uff68", "\uff72"); // HALF KATAKANA LI -> I
		put("\uff69", "\uff73"); // HALF KATAKANA LU -> U
		put("\uff6a", "\uff74"); // HALF KATAKANA LE -> E
		put("\uff6b", "\uff75"); // HALF KATAKANA LO -> O

		put("\uff94", "\uff6c"); // HALF KATAKANA YA -> LYA
		put("\uff95", "\uff6d"); // HALF KATAKANA YU -> LYU
		put("\uff96", "\uff6e"); // HALF KATAKANA YO -> LYO
		put("\uff6c", "\uff94"); // HALF KATAKANA LYA -> YA
		put("\uff6d", "\uff95"); // HALF KATAKANA LYU -> YU
		put("\uff6e", "\uff96"); // HALF KATAKANA LYO -> YO

		put("\uff82", "\uff6f"); // HALF KATAKANA TU -> LTU
		put("\uff6f", "\uff82"); // HALF KATAKANA LTU -> TU

		// FULL-WIDTH Alphabet
		put("\uff21", "\uff41"); put("\uff22", "\uff42"); put("\uff23", "\uff43"); put("\uff24", "\uff44"); put("\uff25", "\uff45");
		put("\uff41", "\uff21"); put("\uff42", "\uff22"); put("\uff43", "\uff23"); put("\uff44", "\uff24"); put("\uff45", "\uff25");
		put("\uff26", "\uff46"); put("\uff27", "\uff47"); put("\uff28", "\uff48"); put("\uff29", "\uff49"); put("\uff2a", "\uff4a");
		put("\uff46", "\uff26"); put("\uff47", "\uff27"); put("\uff48", "\uff28"); put("\uff49", "\uff29"); put("\uff4a", "\uff2a");
		put("\uff2b", "\uff4b"); put("\uff2c", "\uff4c"); put("\uff2d", "\uff4d"); put("\uff2e", "\uff4e"); put("\uff2f", "\uff4f");
		put("\uff4b", "\uff2b"); put("\uff4c", "\uff2c"); put("\uff4d", "\uff2d"); put("\uff4e", "\uff2e"); put("\uff4f", "\uff2f");
		put("\uff30", "\uff50"); put("\uff31", "\uff51"); put("\uff32", "\uff52"); put("\uff33", "\uff53"); put("\uff34", "\uff54");
		put("\uff50", "\uff30"); put("\uff51", "\uff31"); put("\uff52", "\uff32"); put("\uff53", "\uff33"); put("\uff54", "\uff34");
		put("\uff35", "\uff55"); put("\uff36", "\uff56"); put("\uff37", "\uff57"); put("\uff38", "\uff58"); put("\uff39", "\uff59");
		put("\uff55", "\uff35"); put("\uff56", "\uff36"); put("\uff57", "\uff37"); put("\uff58", "\uff38"); put("\uff59", "\uff39");
		put("\uff3a", "\uff5a");
		put("\uff5a", "\uff3a");

		put("A", "a"); put("B", "b"); put("C", "c"); put("D", "d"); put("E", "e");
		put("a", "A"); put("b", "B"); put("c", "C"); put("d", "D"); put("e", "E");
		put("F", "f"); put("G", "g"); put("H", "h"); put("I", "i"); put("J", "j");
		put("f", "F"); put("g", "G"); put("h", "H"); put("i", "I"); put("j", "J");
		put("K", "k"); put("L", "l"); put("M", "m"); put("N", "n"); put("O", "o");
		put("k", "K"); put("l", "L"); put("m", "M"); put("n", "N"); put("o", "O");
		put("P", "p"); put("Q", "q"); put("R", "r"); put("S", "s"); put("T", "t");
		put("p", "P"); put("q", "Q"); put("r", "R"); put("s", "S"); put("t", "T");
		put("U", "u"); put("V", "v"); put("W", "w"); put("X", "x"); put("Y", "y");
		put("u", "U"); put("v", "V"); put("w", "W"); put("x", "X"); put("y", "Y");
		put("Z", "z");
		put("z", "Z");

	}};

	/**
	 * change map
	 */
	protected static final int flickAlphabetChangeMap[][] = {
		{  0,  7,  4,  1,  5 }, // .1/@:
		{  0,  6,  1,  2,  7 }, // A2BC"
		{  0,  6,  1,  2,  7 }, // D3EF'
		{  0,  6,  1,  2,  7 }, // G4HI<
		{  0,  6,  1,  2,  7 }, // J5KL&
		{  0,  6,  1,  2,  7 }, // M6NO>
		{  0,  8,  1,  2,  3 }, // P7QRS
		{  0,  6,  1,  2,  7 }, // T8UV$
		{  0,  8,  1,  2,  3 }, // W9XYZ
		{  0, -1,  2,  1,  3 }, // - (0)
		{  0, -1,  1,  2,  3 }, // , .?!
		{  0, -1,  1,  2,  3 }, // Aster
	};

	private static final int selectQwertyLandKeyTable[] = {
		R.xml.keyboard_qwerty_jp_0,
		R.xml.keyboard_qwerty_jp_shift_0,
		R.xml.keyboard_qwerty_jp_full_alphabet_0,
		R.xml.keyboard_qwerty_jp_full_alphabet_shift_0,
		R.xml.keyboard_qwerty_jp_full_symbols_0,
		R.xml.keyboard_qwerty_jp_full_symbols_shift_0,
		R.xml.keyboard_qwerty_jp_full_katakana_0,
		R.xml.keyboard_qwerty_jp_full_katakana_shift_0,
		R.xml.keyboard_qwerty_jp_half_alphabet_0,
		R.xml.keyboard_qwerty_jp_half_alphabet_shift_0,
		R.xml.keyboard_qwerty_jp_half_symbols_0,
		R.xml.keyboard_qwerty_jp_half_symbols_shift_0,
		R.xml.keyboard_qwerty_jp_half_katakana_0,
		R.xml.keyboard_qwerty_jp_half_katakana_shift_0,
	};

	private static final int selectQwertyPortKeyTable[] = {
		R.xml.keyboard_qwerty_jp_0,
		R.xml.keyboard_qwerty_jp_shift_0,
		R.xml.keyboard_qwerty_jp_full_alphabet_0,
		R.xml.keyboard_qwerty_jp_full_alphabet_shift_0,
		R.xml.keyboard_qwerty_jp_full_symbols_0,
		R.xml.keyboard_qwerty_jp_full_symbols_shift_0,
		R.xml.keyboard_qwerty_jp_full_katakana_0,
		R.xml.keyboard_qwerty_jp_full_katakana_shift_0,
		R.xml.keyboard_qwerty_jp_half_alphabet_0,
		R.xml.keyboard_qwerty_jp_half_alphabet_shift_0,
		R.xml.keyboard_qwerty_jp_half_symbols_0,
		R.xml.keyboard_qwerty_jp_half_symbols_shift_0,
		R.xml.keyboard_qwerty_jp_half_katakana_0,
		R.xml.keyboard_qwerty_jp_half_katakana_shift_0,
	};

	private static final int selectKanaJisLandKeyTable[] = {
		R.xml.keyboard_kanajis_jp_full_0,
		R.xml.keyboard_kanajis_jp_full_shift_0,
		R.xml.keyboard_qwerty_jp_full_alphabet_0,
		R.xml.keyboard_qwerty_jp_full_alphabet_shift_0,
		R.xml.keyboard_qwerty_jp_full_symbols_0,
		R.xml.keyboard_qwerty_jp_full_symbols_shift_0,
		R.xml.keyboard_katakanajis_jp_full_0,
		R.xml.keyboard_katakanajis_jp_full_shift_0,
		R.xml.keyboard_qwerty_jp_half_alphabet_0,
		R.xml.keyboard_qwerty_jp_half_alphabet_shift_0,
		R.xml.keyboard_qwerty_jp_half_symbols_0,
		R.xml.keyboard_qwerty_jp_half_symbols_shift_0,
		R.xml.keyboard_katakanajis_jp_half_0,
		R.xml.keyboard_katakanajis_jp_half_shift_0,
	};

	private static final int selectKanaJisPortKeyTable[] = {
		R.xml.keyboard_kanajis_jp_full_0,
		R.xml.keyboard_kanajis_jp_full_shift_0,
		R.xml.keyboard_qwerty_jp_full_alphabet_0,
		R.xml.keyboard_qwerty_jp_full_alphabet_shift_0,
		R.xml.keyboard_qwerty_jp_full_symbols_0,
		R.xml.keyboard_qwerty_jp_full_symbols_shift_0,
		R.xml.keyboard_katakanajis_jp_full_0,
		R.xml.keyboard_katakanajis_jp_full_shift_0,
		R.xml.keyboard_qwerty_jp_half_alphabet_0,
		R.xml.keyboard_qwerty_jp_half_alphabet_shift_0,
		R.xml.keyboard_qwerty_jp_half_symbols_0,
		R.xml.keyboard_qwerty_jp_half_symbols_shift_0,
		R.xml.keyboard_katakanajis_jp_half_0,
		R.xml.keyboard_katakanajis_jp_half_shift_0,
	};

	private static final int selectKana50onLandKeyTable[] = {
		R.xml.keyboard_kana50on_jp_full_0,
		R.xml.keyboard_kana50on_jp_full_shift_0,
		R.xml.keyboard_qwerty_jp_full_alphabet_0,
		R.xml.keyboard_qwerty_jp_full_alphabet_shift_0,
		R.xml.keyboard_qwerty_jp_full_symbols_0,
		R.xml.keyboard_qwerty_jp_full_symbols_shift_0,
		R.xml.keyboard_katakana50on_jp_full_0,
		R.xml.keyboard_katakana50on_jp_full_shift_0,
		R.xml.keyboard_qwerty_jp_half_alphabet_0,
		R.xml.keyboard_qwerty_jp_half_alphabet_shift_0,
		R.xml.keyboard_qwerty_jp_half_symbols_0,
		R.xml.keyboard_qwerty_jp_half_symbols_shift_0,
		R.xml.keyboard_katakana50on_jp_half_0,
		R.xml.keyboard_katakana50on_jp_half_shift_0,
	};

	private static final int selectKana50onPortKeyTable[] = {
		R.xml.keyboard_kana50on_jp_full_0,
		R.xml.keyboard_kana50on_jp_full_shift_0,
		R.xml.keyboard_qwerty_jp_full_alphabet_0,
		R.xml.keyboard_qwerty_jp_full_alphabet_shift_0,
		R.xml.keyboard_qwerty_jp_full_symbols_0,
		R.xml.keyboard_qwerty_jp_full_symbols_shift_0,
		R.xml.keyboard_katakana50on_jp_full_0,
		R.xml.keyboard_katakana50on_jp_full_shift_0,
		R.xml.keyboard_qwerty_jp_half_alphabet_0,
		R.xml.keyboard_qwerty_jp_half_alphabet_shift_0,
		R.xml.keyboard_qwerty_jp_half_symbols_0,
		R.xml.keyboard_qwerty_jp_half_symbols_shift_0,
		R.xml.keyboard_katakana50on_jp_half_0,
		R.xml.keyboard_katakana50on_jp_half_shift_0,
	};

	private static final int selectCompactQwertyLandKeyTable[] = {
		R.xml.keyboard_compact_qwerty_jp_l_0,
		R.xml.keyboard_compact_qwerty_jp_l_shift_0,
		R.xml.keyboard_compact_qwerty_jp_l_full_alphabet_0,
		R.xml.keyboard_compact_qwerty_jp_l_full_alphabet_shift_0,
		R.xml.keyboard_compact_qwerty_jp_l_full_symbols_0,
		R.xml.keyboard_compact_qwerty_jp_l_full_symbols_shift_0,
		R.xml.keyboard_compact_qwerty_jp_l_full_katakana_0,
		R.xml.keyboard_compact_qwerty_jp_l_full_katakana_shift_0,
		R.xml.keyboard_compact_qwerty_jp_l_half_alphabet_0,
		R.xml.keyboard_compact_qwerty_jp_l_half_alphabet_shift_0,
		R.xml.keyboard_compact_qwerty_jp_l_half_symbols_0,
		R.xml.keyboard_compact_qwerty_jp_l_half_symbols_shift_0,
		R.xml.keyboard_compact_qwerty_jp_l_half_katakana_0,
		R.xml.keyboard_compact_qwerty_jp_l_half_katakana_shift_0,
	};

	private static final int selectCompactQwertyPortKeyTable[] = {
		R.xml.keyboard_compact_qwerty_jp_p_0,
		R.xml.keyboard_compact_qwerty_jp_p_shift_0,
		R.xml.keyboard_compact_qwerty_jp_p_full_alphabet_0,
		R.xml.keyboard_compact_qwerty_jp_p_full_alphabet_shift_0,
		R.xml.keyboard_compact_qwerty_jp_p_full_symbols_0,
		R.xml.keyboard_compact_qwerty_jp_p_full_symbols_shift_0,
		R.xml.keyboard_compact_qwerty_jp_p_full_katakana_0,
		R.xml.keyboard_compact_qwerty_jp_p_full_katakana_shift_0,
		R.xml.keyboard_compact_qwerty_jp_p_half_alphabet_0,
		R.xml.keyboard_compact_qwerty_jp_p_half_alphabet_shift_0,
		R.xml.keyboard_compact_qwerty_jp_p_half_symbols_0,
		R.xml.keyboard_compact_qwerty_jp_p_half_symbols_shift_0,
		R.xml.keyboard_compact_qwerty_jp_p_half_katakana_0,
		R.xml.keyboard_compact_qwerty_jp_p_half_katakana_shift_0,
	};

	private static final int selectMiniQwertyLandKeyTable[] = {
		R.xml.keyboard_mini_qwerty_jp_l_0,
		R.xml.keyboard_mini_qwerty_jp_l_shift_0,
		R.xml.keyboard_mini_qwerty_jp_l_full_alphabet_0,
		R.xml.keyboard_mini_qwerty_jp_l_full_alphabet_shift_0,
		R.xml.keyboard_mini_qwerty_jp_l_full_symbols_0,
		R.xml.keyboard_mini_qwerty_jp_l_full_symbols_shift_0,
		R.xml.keyboard_mini_qwerty_jp_l_full_katakana_0,
		R.xml.keyboard_mini_qwerty_jp_l_full_katakana_shift_0,
		R.xml.keyboard_mini_qwerty_jp_l_half_alphabet_0,
		R.xml.keyboard_mini_qwerty_jp_l_half_alphabet_shift_0,
		R.xml.keyboard_mini_qwerty_jp_l_half_symbols_0,
		R.xml.keyboard_mini_qwerty_jp_l_half_symbols_shift_0,
		R.xml.keyboard_mini_qwerty_jp_l_half_katakana_0,
		R.xml.keyboard_mini_qwerty_jp_l_half_katakana_shift_0,
	};

	private static final int selectMiniQwertyPortKeyTable[] = {
		R.xml.keyboard_mini_qwerty_jp_p_0,
		R.xml.keyboard_mini_qwerty_jp_p_shift_0,
		R.xml.keyboard_mini_qwerty_jp_p_full_alphabet_0,
		R.xml.keyboard_mini_qwerty_jp_p_full_alphabet_shift_0,
		R.xml.keyboard_mini_qwerty_jp_p_full_symbols_0,
		R.xml.keyboard_mini_qwerty_jp_p_full_symbols_shift_0,
		R.xml.keyboard_mini_qwerty_jp_p_full_katakana_0,
		R.xml.keyboard_mini_qwerty_jp_p_full_katakana_shift_0,
		R.xml.keyboard_mini_qwerty_jp_p_half_alphabet_0,
		R.xml.keyboard_mini_qwerty_jp_p_half_alphabet_shift_0,
		R.xml.keyboard_mini_qwerty_jp_p_half_symbols_0,
		R.xml.keyboard_mini_qwerty_jp_p_half_symbols_shift_0,
		R.xml.keyboard_mini_qwerty_jp_p_half_katakana_0,
		R.xml.keyboard_mini_qwerty_jp_p_half_katakana_shift_0,
	};

	private static final int selectMiniQwerty2LandKeyTable[] = {
		R.xml.keyboard_mini_qwerty2_jp_l_0,
		R.xml.keyboard_mini_qwerty2_jp_l_shift_0,
		R.xml.keyboard_mini_qwerty2_jp_l_full_alphabet_0,
		R.xml.keyboard_mini_qwerty2_jp_l_full_alphabet_shift_0,
		R.xml.keyboard_mini_qwerty2_jp_l_full_symbols_0,
		R.xml.keyboard_mini_qwerty2_jp_l_full_symbols_shift_0,
		R.xml.keyboard_mini_qwerty2_jp_l_full_katakana_0,
		R.xml.keyboard_mini_qwerty2_jp_l_full_katakana_shift_0,
		R.xml.keyboard_mini_qwerty2_jp_l_half_alphabet_0,
		R.xml.keyboard_mini_qwerty2_jp_l_half_alphabet_shift_0,
		R.xml.keyboard_mini_qwerty2_jp_l_half_symbols_0,
		R.xml.keyboard_mini_qwerty2_jp_l_half_symbols_shift_0,
		R.xml.keyboard_mini_qwerty2_jp_l_half_katakana_0,
		R.xml.keyboard_mini_qwerty2_jp_l_half_katakana_shift_0,
	};

	private static final int selectMiniQwerty2PortKeyTable[] = {
		R.xml.keyboard_mini_qwerty2_jp_p_0,
		R.xml.keyboard_mini_qwerty2_jp_p_shift_0,
		R.xml.keyboard_mini_qwerty_jp_p_full_alphabet_0,
		R.xml.keyboard_mini_qwerty_jp_p_full_alphabet_shift_0,
		R.xml.keyboard_mini_qwerty_jp_p_full_symbols_0,
		R.xml.keyboard_mini_qwerty_jp_p_full_symbols_shift_0,
		R.xml.keyboard_mini_qwerty_jp_p_full_katakana_0,
		R.xml.keyboard_mini_qwerty_jp_p_full_katakana_shift_0,
		R.xml.keyboard_mini_qwerty_jp_p_half_alphabet_0,
		R.xml.keyboard_mini_qwerty_jp_p_half_alphabet_shift_0,
		R.xml.keyboard_mini_qwerty_jp_p_half_symbols_0,
		R.xml.keyboard_mini_qwerty_jp_p_half_symbols_shift_0,
		R.xml.keyboard_mini_qwerty_jp_p_half_katakana_0,
		R.xml.keyboard_mini_qwerty_jp_p_half_katakana_shift_0,
	};

	private static final int selectQwerty2LandKeyTable[] = {
		R.xml.keyboard_qwerty2_jp_0,
		R.xml.keyboard_qwerty2_jp_shift_0,
		R.xml.keyboard_qwerty2_jp_full_alphabet_0,
		R.xml.keyboard_qwerty2_jp_full_alphabet_shift_0,
		R.xml.keyboard_qwerty2_jp_full_symbols_0,
		R.xml.keyboard_qwerty2_jp_full_symbols_shift_0,
		R.xml.keyboard_qwerty2_jp_full_katakana_0,
		R.xml.keyboard_qwerty2_jp_full_katakana_shift_0,
		R.xml.keyboard_qwerty2_jp_half_alphabet_0,
		R.xml.keyboard_qwerty2_jp_half_alphabet_shift_0,
		R.xml.keyboard_qwerty2_jp_half_symbols_0,
		R.xml.keyboard_qwerty2_jp_half_symbols_shift_0,
		R.xml.keyboard_qwerty2_jp_half_katakana_0,
		R.xml.keyboard_qwerty2_jp_half_katakana_shift_0,
	};

	private static final int selectQwerty2PortKeyTable[] = {
		R.xml.keyboard_qwerty2_jp_0,
		R.xml.keyboard_qwerty2_jp_shift_0,
		R.xml.keyboard_qwerty2_jp_full_alphabet_0,
		R.xml.keyboard_qwerty2_jp_full_alphabet_shift_0,
		R.xml.keyboard_qwerty2_jp_full_symbols_0,
		R.xml.keyboard_qwerty2_jp_full_symbols_shift_0,
		R.xml.keyboard_qwerty2_jp_full_katakana_0,
		R.xml.keyboard_qwerty2_jp_full_katakana_shift_0,
		R.xml.keyboard_qwerty2_jp_half_alphabet_0,
		R.xml.keyboard_qwerty2_jp_half_alphabet_shift_0,
		R.xml.keyboard_qwerty2_jp_half_symbols_0,
		R.xml.keyboard_qwerty2_jp_half_symbols_shift_0,
		R.xml.keyboard_qwerty2_jp_half_katakana_0,
		R.xml.keyboard_qwerty2_jp_half_katakana_shift_0,
	};

	private static final int selectKanaJis2LandKeyTable[] = {
		R.xml.keyboard_kanajis2_jp_full_0,
		R.xml.keyboard_kanajis2_jp_full_shift_0,
		R.xml.keyboard_qwerty2_jp_full_alphabet_0,
		R.xml.keyboard_qwerty2_jp_full_alphabet_shift_0,
		R.xml.keyboard_qwerty2_jp_full_symbols_0,
		R.xml.keyboard_qwerty2_jp_full_symbols_shift_0,
		R.xml.keyboard_katakanajis2_jp_full_0,
		R.xml.keyboard_katakanajis2_jp_full_shift_0,
		R.xml.keyboard_qwerty2_jp_half_alphabet_0,
		R.xml.keyboard_qwerty2_jp_half_alphabet_shift_0,
		R.xml.keyboard_qwerty2_jp_half_symbols_0,
		R.xml.keyboard_qwerty2_jp_half_symbols_shift_0,
		R.xml.keyboard_katakanajis2_jp_half_0,
		R.xml.keyboard_katakanajis2_jp_half_shift_0,
	};

	private static final int selectKanaJis2PortKeyTable[] = {
		R.xml.keyboard_kanajis2_jp_full_0,
		R.xml.keyboard_kanajis2_jp_full_shift_0,
		R.xml.keyboard_qwerty2_jp_full_alphabet_0,
		R.xml.keyboard_qwerty2_jp_full_alphabet_shift_0,
		R.xml.keyboard_qwerty2_jp_full_symbols_0,
		R.xml.keyboard_qwerty2_jp_full_symbols_shift_0,
		R.xml.keyboard_katakanajis2_jp_full_0,
		R.xml.keyboard_katakanajis2_jp_full_shift_0,
		R.xml.keyboard_qwerty2_jp_half_alphabet_0,
		R.xml.keyboard_qwerty2_jp_half_alphabet_shift_0,
		R.xml.keyboard_qwerty2_jp_half_symbols_0,
		R.xml.keyboard_qwerty2_jp_half_symbols_shift_0,
		R.xml.keyboard_katakanajis2_jp_half_0,
		R.xml.keyboard_katakanajis2_jp_half_shift_0,
	};

	private static final int selectKana50on2LandKeyTable[] = {
		R.xml.keyboard_kana50on2_jp_full_0,
		R.xml.keyboard_kana50on2_jp_full_shift_0,
		R.xml.keyboard_qwerty2_jp_full_alphabet_0,
		R.xml.keyboard_qwerty2_jp_full_alphabet_shift_0,
		R.xml.keyboard_qwerty2_jp_full_symbols_0,
		R.xml.keyboard_qwerty2_jp_full_symbols_shift_0,
		R.xml.keyboard_katakana50on2_jp_full_0,
		R.xml.keyboard_katakana50on2_jp_full_shift_0,
		R.xml.keyboard_qwerty2_jp_half_alphabet_0,
		R.xml.keyboard_qwerty2_jp_half_alphabet_shift_0,
		R.xml.keyboard_qwerty2_jp_half_symbols_0,
		R.xml.keyboard_qwerty2_jp_half_symbols_shift_0,
		R.xml.keyboard_katakana50on2_jp_half_0,
		R.xml.keyboard_katakana50on2_jp_half_shift_0,
	};

	private static final int selectKana50on2PortKeyTable[] = {
		R.xml.keyboard_kana50on2_jp_full_0,
		R.xml.keyboard_kana50on2_jp_full_shift_0,
		R.xml.keyboard_qwerty2_jp_full_alphabet_0,
		R.xml.keyboard_qwerty2_jp_full_alphabet_shift_0,
		R.xml.keyboard_qwerty2_jp_full_symbols_0,
		R.xml.keyboard_qwerty2_jp_full_symbols_shift_0,
		R.xml.keyboard_katakana50on2_jp_full_0,
		R.xml.keyboard_katakana50on2_jp_full_shift_0,
		R.xml.keyboard_qwerty2_jp_half_alphabet_0,
		R.xml.keyboard_qwerty2_jp_half_alphabet_shift_0,
		R.xml.keyboard_qwerty2_jp_half_symbols_0,
		R.xml.keyboard_qwerty2_jp_half_symbols_shift_0,
		R.xml.keyboard_katakana50on2_jp_half_0,
		R.xml.keyboard_katakana50on2_jp_half_shift_0,
	};

	/** */

	private static final int selectQwertyLandSlantKeyTable[] = {
		R.xml.keyboard_qwerty_jp_s0,
		R.xml.keyboard_qwerty_jp_shift_s0,
		R.xml.keyboard_qwerty_jp_full_alphabet_s0,
		R.xml.keyboard_qwerty_jp_full_alphabet_shift_s0,
		R.xml.keyboard_qwerty_jp_full_symbols_s0,
		R.xml.keyboard_qwerty_jp_full_symbols_shift_s0,
		R.xml.keyboard_qwerty_jp_full_katakana_s0,
		R.xml.keyboard_qwerty_jp_full_katakana_shift_s0,
		R.xml.keyboard_qwerty_jp_half_alphabet_s0,
		R.xml.keyboard_qwerty_jp_half_alphabet_shift_s0,
		R.xml.keyboard_qwerty_jp_half_symbols_s0,
		R.xml.keyboard_qwerty_jp_half_symbols_shift_s0,
		R.xml.keyboard_qwerty_jp_half_katakana_s0,
		R.xml.keyboard_qwerty_jp_half_katakana_shift_s0,
	};

	private static final int selectQwertyPortSlantKeyTable[] = {
		R.xml.keyboard_qwerty_jp_s0,
		R.xml.keyboard_qwerty_jp_shift_s0,
		R.xml.keyboard_qwerty_jp_full_alphabet_s0,
		R.xml.keyboard_qwerty_jp_full_alphabet_shift_s0,
		R.xml.keyboard_qwerty_jp_full_symbols_s0,
		R.xml.keyboard_qwerty_jp_full_symbols_shift_s0,
		R.xml.keyboard_qwerty_jp_full_katakana_s0,
		R.xml.keyboard_qwerty_jp_full_katakana_shift_s0,
		R.xml.keyboard_qwerty_jp_half_alphabet_s0,
		R.xml.keyboard_qwerty_jp_half_alphabet_shift_s0,
		R.xml.keyboard_qwerty_jp_half_symbols_s0,
		R.xml.keyboard_qwerty_jp_half_symbols_shift_s0,
		R.xml.keyboard_qwerty_jp_half_katakana_s0,
		R.xml.keyboard_qwerty_jp_half_katakana_shift_s0,
	};

	private static final int selectKanaJisLandSlantKeyTable[] = {
		R.xml.keyboard_kanajis_jp_full_s0,
		R.xml.keyboard_kanajis_jp_full_shift_s0,
		R.xml.keyboard_qwerty_jp_full_alphabet_s0,
		R.xml.keyboard_qwerty_jp_full_alphabet_shift_s0,
		R.xml.keyboard_qwerty_jp_full_symbols_s0,
		R.xml.keyboard_qwerty_jp_full_symbols_shift_s0,
		R.xml.keyboard_katakanajis_jp_full_s0,
		R.xml.keyboard_katakanajis_jp_full_shift_s0,
		R.xml.keyboard_qwerty_jp_half_alphabet_s0,
		R.xml.keyboard_qwerty_jp_half_alphabet_shift_s0,
		R.xml.keyboard_qwerty_jp_half_symbols_s0,
		R.xml.keyboard_qwerty_jp_half_symbols_shift_s0,
		R.xml.keyboard_katakanajis_jp_half_s0,
		R.xml.keyboard_katakanajis_jp_half_shift_s0,
	};

	private static final int selectKanaJisPortSlantKeyTable[] = {
		R.xml.keyboard_kanajis_jp_full_s0,
		R.xml.keyboard_kanajis_jp_full_shift_s0,
		R.xml.keyboard_qwerty_jp_full_alphabet_s0,
		R.xml.keyboard_qwerty_jp_full_alphabet_shift_s0,
		R.xml.keyboard_qwerty_jp_full_symbols_s0,
		R.xml.keyboard_qwerty_jp_full_symbols_shift_s0,
		R.xml.keyboard_katakanajis_jp_full_s0,
		R.xml.keyboard_katakanajis_jp_full_shift_s0,
		R.xml.keyboard_qwerty_jp_half_alphabet_s0,
		R.xml.keyboard_qwerty_jp_half_alphabet_shift_s0,
		R.xml.keyboard_qwerty_jp_half_symbols_s0,
		R.xml.keyboard_qwerty_jp_half_symbols_shift_s0,
		R.xml.keyboard_katakanajis_jp_half_s0,
		R.xml.keyboard_katakanajis_jp_half_shift_s0,
	};

	private static final int selectKana50onLandSlantKeyTable[] = {
		R.xml.keyboard_kana50on_jp_full_s0,
		R.xml.keyboard_kana50on_jp_full_shift_s0,
		R.xml.keyboard_qwerty_jp_full_alphabet_s0,
		R.xml.keyboard_qwerty_jp_full_alphabet_shift_s0,
		R.xml.keyboard_qwerty_jp_full_symbols_s0,
		R.xml.keyboard_qwerty_jp_full_symbols_shift_s0,
		R.xml.keyboard_katakana50on_jp_full_s0,
		R.xml.keyboard_katakana50on_jp_full_shift_s0,
		R.xml.keyboard_qwerty_jp_half_alphabet_s0,
		R.xml.keyboard_qwerty_jp_half_alphabet_shift_s0,
		R.xml.keyboard_qwerty_jp_half_symbols_s0,
		R.xml.keyboard_qwerty_jp_half_symbols_shift_s0,
		R.xml.keyboard_katakana50on_jp_half_s0,
		R.xml.keyboard_katakana50on_jp_half_shift_s0,
	};

	private static final int selectKana50onPortSlantKeyTable[] = {
		R.xml.keyboard_kana50on_jp_full_s0,
		R.xml.keyboard_kana50on_jp_full_shift_s0,
		R.xml.keyboard_qwerty_jp_full_alphabet_s0,
		R.xml.keyboard_qwerty_jp_full_alphabet_shift_s0,
		R.xml.keyboard_qwerty_jp_full_symbols_s0,
		R.xml.keyboard_qwerty_jp_full_symbols_shift_s0,
		R.xml.keyboard_katakana50on_jp_full_s0,
		R.xml.keyboard_katakana50on_jp_full_shift_s0,
		R.xml.keyboard_qwerty_jp_half_alphabet_s0,
		R.xml.keyboard_qwerty_jp_half_alphabet_shift_s0,
		R.xml.keyboard_qwerty_jp_half_symbols_s0,
		R.xml.keyboard_qwerty_jp_half_symbols_shift_s0,
		R.xml.keyboard_katakana50on_jp_half_s0,
		R.xml.keyboard_katakana50on_jp_half_shift_s0,
	};

	private static final int selectCompactQwertyLandSlantKeyTable[] = {
		R.xml.keyboard_compact_qwerty_jp_l_s0,
		R.xml.keyboard_compact_qwerty_jp_l_shift_s0,
		R.xml.keyboard_compact_qwerty_jp_l_full_alphabet_s0,
		R.xml.keyboard_compact_qwerty_jp_l_full_alphabet_shift_s0,
		R.xml.keyboard_compact_qwerty_jp_l_full_symbols_s0,
		R.xml.keyboard_compact_qwerty_jp_l_full_symbols_shift_s0,
		R.xml.keyboard_compact_qwerty_jp_l_full_katakana_s0,
		R.xml.keyboard_compact_qwerty_jp_l_full_katakana_shift_s0,
		R.xml.keyboard_compact_qwerty_jp_l_half_alphabet_s0,
		R.xml.keyboard_compact_qwerty_jp_l_half_alphabet_shift_s0,
		R.xml.keyboard_compact_qwerty_jp_l_half_symbols_s0,
		R.xml.keyboard_compact_qwerty_jp_l_half_symbols_shift_s0,
		R.xml.keyboard_compact_qwerty_jp_l_half_katakana_s0,
		R.xml.keyboard_compact_qwerty_jp_l_half_katakana_shift_s0,
	};

	private static final int selectCompactQwertyPortSlantKeyTable[] = {
		R.xml.keyboard_compact_qwerty_jp_p_s0,
		R.xml.keyboard_compact_qwerty_jp_p_shift_s0,
		R.xml.keyboard_compact_qwerty_jp_p_full_alphabet_s0,
		R.xml.keyboard_compact_qwerty_jp_p_full_alphabet_shift_s0,
		R.xml.keyboard_compact_qwerty_jp_p_full_symbols_s0,
		R.xml.keyboard_compact_qwerty_jp_p_full_symbols_shift_s0,
		R.xml.keyboard_compact_qwerty_jp_p_full_katakana_s0,
		R.xml.keyboard_compact_qwerty_jp_p_full_katakana_shift_s0,
		R.xml.keyboard_compact_qwerty_jp_p_half_alphabet_s0,
		R.xml.keyboard_compact_qwerty_jp_p_half_alphabet_shift_s0,
		R.xml.keyboard_compact_qwerty_jp_p_half_symbols_s0,
		R.xml.keyboard_compact_qwerty_jp_p_half_symbols_shift_s0,
		R.xml.keyboard_compact_qwerty_jp_p_half_katakana_s0,
		R.xml.keyboard_compact_qwerty_jp_p_half_katakana_shift_s0,
	};

	private static final int selectMiniQwertyLandSlantKeyTable[] = {
		R.xml.keyboard_mini_qwerty_jp_l_s0,
		R.xml.keyboard_mini_qwerty_jp_l_shift_s0,
		R.xml.keyboard_mini_qwerty_jp_l_full_alphabet_s0,
		R.xml.keyboard_mini_qwerty_jp_l_full_alphabet_shift_s0,
		R.xml.keyboard_mini_qwerty_jp_l_full_symbols_s0,
		R.xml.keyboard_mini_qwerty_jp_l_full_symbols_shift_s0,
		R.xml.keyboard_mini_qwerty_jp_l_full_katakana_s0,
		R.xml.keyboard_mini_qwerty_jp_l_full_katakana_shift_s0,
		R.xml.keyboard_mini_qwerty_jp_l_half_alphabet_s0,
		R.xml.keyboard_mini_qwerty_jp_l_half_alphabet_shift_s0,
		R.xml.keyboard_mini_qwerty_jp_l_half_symbols_s0,
		R.xml.keyboard_mini_qwerty_jp_l_half_symbols_shift_s0,
		R.xml.keyboard_mini_qwerty_jp_l_half_katakana_s0,
		R.xml.keyboard_mini_qwerty_jp_l_half_katakana_shift_s0,
	};

	private static final int selectMiniQwertyPortSlantKeyTable[] = {
		R.xml.keyboard_mini_qwerty_jp_p_s0,
		R.xml.keyboard_mini_qwerty_jp_p_shift_s0,
		R.xml.keyboard_mini_qwerty_jp_p_full_alphabet_s0,
		R.xml.keyboard_mini_qwerty_jp_p_full_alphabet_shift_s0,
		R.xml.keyboard_mini_qwerty_jp_p_full_symbols_s0,
		R.xml.keyboard_mini_qwerty_jp_p_full_symbols_shift_s0,
		R.xml.keyboard_mini_qwerty_jp_p_full_katakana_s0,
		R.xml.keyboard_mini_qwerty_jp_p_full_katakana_shift_s0,
		R.xml.keyboard_mini_qwerty_jp_p_half_alphabet_s0,
		R.xml.keyboard_mini_qwerty_jp_p_half_alphabet_shift_s0,
		R.xml.keyboard_mini_qwerty_jp_p_half_symbols_s0,
		R.xml.keyboard_mini_qwerty_jp_p_half_symbols_shift_s0,
		R.xml.keyboard_mini_qwerty_jp_p_half_katakana_s0,
		R.xml.keyboard_mini_qwerty_jp_p_half_katakana_shift_s0,
	};

	private static final int selectMiniQwerty2LandSlantKeyTable[] = {
		R.xml.keyboard_mini_qwerty2_jp_l_s0,
		R.xml.keyboard_mini_qwerty2_jp_l_shift_s0,
		R.xml.keyboard_mini_qwerty2_jp_l_full_alphabet_s0,
		R.xml.keyboard_mini_qwerty2_jp_l_full_alphabet_shift_s0,
		R.xml.keyboard_mini_qwerty2_jp_l_full_symbols_s0,
		R.xml.keyboard_mini_qwerty2_jp_l_full_symbols_shift_s0,
		R.xml.keyboard_mini_qwerty2_jp_l_full_katakana_s0,
		R.xml.keyboard_mini_qwerty2_jp_l_full_katakana_shift_s0,
		R.xml.keyboard_mini_qwerty2_jp_l_half_alphabet_s0,
		R.xml.keyboard_mini_qwerty2_jp_l_half_alphabet_shift_s0,
		R.xml.keyboard_mini_qwerty2_jp_l_half_symbols_s0,
		R.xml.keyboard_mini_qwerty2_jp_l_half_symbols_shift_s0,
		R.xml.keyboard_mini_qwerty2_jp_l_half_katakana_s0,
		R.xml.keyboard_mini_qwerty2_jp_l_half_katakana_shift_s0,
	};

	private static final int selectMiniQwerty2PortSlantKeyTable[] = {
		R.xml.keyboard_mini_qwerty2_jp_p_s0,
		R.xml.keyboard_mini_qwerty2_jp_p_shift_s0,
		R.xml.keyboard_mini_qwerty2_jp_p_full_alphabet_s0,
		R.xml.keyboard_mini_qwerty2_jp_p_full_alphabet_shift_s0,
		R.xml.keyboard_mini_qwerty2_jp_p_full_symbols_s0,
		R.xml.keyboard_mini_qwerty2_jp_p_full_symbols_shift_s0,
		R.xml.keyboard_mini_qwerty2_jp_p_full_katakana_s0,
		R.xml.keyboard_mini_qwerty2_jp_p_full_katakana_shift_s0,
		R.xml.keyboard_mini_qwerty2_jp_p_half_alphabet_s0,
		R.xml.keyboard_mini_qwerty2_jp_p_half_alphabet_shift_s0,
		R.xml.keyboard_mini_qwerty2_jp_p_half_symbols_s0,
		R.xml.keyboard_mini_qwerty2_jp_p_half_symbols_shift_s0,
		R.xml.keyboard_mini_qwerty2_jp_p_half_katakana_s0,
		R.xml.keyboard_mini_qwerty2_jp_p_half_katakana_shift_s0,
	};

	private static final int selectQwerty2LandSlantKeyTable[] = {
		R.xml.keyboard_qwerty2_jp_s0,
		R.xml.keyboard_qwerty2_jp_shift_s0,
		R.xml.keyboard_qwerty2_jp_full_alphabet_s0,
		R.xml.keyboard_qwerty2_jp_full_alphabet_shift_s0,
		R.xml.keyboard_qwerty2_jp_full_symbols_s0,
		R.xml.keyboard_qwerty2_jp_full_symbols_shift_s0,
		R.xml.keyboard_qwerty2_jp_full_katakana_s0,
		R.xml.keyboard_qwerty2_jp_full_katakana_shift_s0,
		R.xml.keyboard_qwerty2_jp_half_alphabet_s0,
		R.xml.keyboard_qwerty2_jp_half_alphabet_shift_s0,
		R.xml.keyboard_qwerty2_jp_half_symbols_s0,
		R.xml.keyboard_qwerty2_jp_half_symbols_shift_s0,
		R.xml.keyboard_qwerty2_jp_half_katakana_s0,
		R.xml.keyboard_qwerty2_jp_half_katakana_shift_s0,
	};

	private static final int selectQwerty2PortSlantKeyTable[] = {
		R.xml.keyboard_qwerty2_jp_s0,
		R.xml.keyboard_qwerty2_jp_shift_s0,
		R.xml.keyboard_qwerty2_jp_full_alphabet_s0,
		R.xml.keyboard_qwerty2_jp_full_alphabet_shift_s0,
		R.xml.keyboard_qwerty2_jp_full_symbols_s0,
		R.xml.keyboard_qwerty2_jp_full_symbols_shift_s0,
		R.xml.keyboard_qwerty2_jp_full_katakana_s0,
		R.xml.keyboard_qwerty2_jp_full_katakana_shift_s0,
		R.xml.keyboard_qwerty2_jp_half_alphabet_s0,
		R.xml.keyboard_qwerty2_jp_half_alphabet_shift_s0,
		R.xml.keyboard_qwerty2_jp_half_symbols_s0,
		R.xml.keyboard_qwerty2_jp_half_symbols_shift_s0,
		R.xml.keyboard_qwerty2_jp_half_katakana_s0,
		R.xml.keyboard_qwerty2_jp_half_katakana_shift_s0,
	};

	private static final int selectKanaJis2LandSlantKeyTable[] = {
		R.xml.keyboard_kanajis2_jp_full_0,
		R.xml.keyboard_kanajis2_jp_full_shift_0,
		R.xml.keyboard_qwerty2_jp_full_alphabet_0,
		R.xml.keyboard_qwerty2_jp_full_alphabet_shift_0,
		R.xml.keyboard_qwerty2_jp_full_symbols_0,
		R.xml.keyboard_qwerty2_jp_full_symbols_shift_0,
		R.xml.keyboard_katakanajis2_jp_full_0,
		R.xml.keyboard_katakanajis2_jp_full_shift_0,
		R.xml.keyboard_qwerty2_jp_half_alphabet_0,
		R.xml.keyboard_qwerty2_jp_half_alphabet_shift_0,
		R.xml.keyboard_qwerty2_jp_half_symbols_0,
		R.xml.keyboard_qwerty2_jp_half_symbols_shift_0,
		R.xml.keyboard_katakanajis2_jp_half_0,
		R.xml.keyboard_katakanajis2_jp_half_shift_0,
	};

	private static final int selectKanaJis2PortSlantKeyTable[] = {
		R.xml.keyboard_kanajis2_jp_full_0,
		R.xml.keyboard_kanajis2_jp_full_shift_0,
		R.xml.keyboard_qwerty2_jp_full_alphabet_0,
		R.xml.keyboard_qwerty2_jp_full_alphabet_shift_0,
		R.xml.keyboard_qwerty2_jp_full_symbols_0,
		R.xml.keyboard_qwerty2_jp_full_symbols_shift_0,
		R.xml.keyboard_katakanajis2_jp_full_0,
		R.xml.keyboard_katakanajis2_jp_full_shift_0,
		R.xml.keyboard_qwerty2_jp_half_alphabet_0,
		R.xml.keyboard_qwerty2_jp_half_alphabet_shift_0,
		R.xml.keyboard_qwerty2_jp_half_symbols_0,
		R.xml.keyboard_qwerty2_jp_half_symbols_shift_0,
		R.xml.keyboard_katakanajis2_jp_half_0,
		R.xml.keyboard_katakanajis2_jp_half_shift_0,
	};

	private static final int selectKana50on2LandSlantKeyTable[] = {
		R.xml.keyboard_kana50on2_jp_full_0,
		R.xml.keyboard_kana50on2_jp_full_shift_0,
		R.xml.keyboard_qwerty2_jp_full_alphabet_0,
		R.xml.keyboard_qwerty2_jp_full_alphabet_shift_0,
		R.xml.keyboard_qwerty2_jp_full_symbols_0,
		R.xml.keyboard_qwerty2_jp_full_symbols_shift_0,
		R.xml.keyboard_katakana50on2_jp_full_0,
		R.xml.keyboard_katakana50on2_jp_full_shift_0,
		R.xml.keyboard_qwerty2_jp_half_alphabet_0,
		R.xml.keyboard_qwerty2_jp_half_alphabet_shift_0,
		R.xml.keyboard_qwerty2_jp_half_symbols_0,
		R.xml.keyboard_qwerty2_jp_half_symbols_shift_0,
		R.xml.keyboard_katakana50on2_jp_half_0,
		R.xml.keyboard_katakana50on2_jp_half_shift_0,
	};

	private static final int selectKana50on2PortSlantKeyTable[] = {
		R.xml.keyboard_kana50on2_jp_full_0,
		R.xml.keyboard_kana50on2_jp_full_shift_0,
		R.xml.keyboard_qwerty2_jp_full_alphabet_0,
		R.xml.keyboard_qwerty2_jp_full_alphabet_shift_0,
		R.xml.keyboard_qwerty2_jp_full_symbols_0,
		R.xml.keyboard_qwerty2_jp_full_symbols_shift_0,
		R.xml.keyboard_katakana50on2_jp_full_0,
		R.xml.keyboard_katakana50on2_jp_full_shift_0,
		R.xml.keyboard_qwerty2_jp_half_alphabet_0,
		R.xml.keyboard_qwerty2_jp_half_alphabet_shift_0,
		R.xml.keyboard_qwerty2_jp_half_symbols_0,
		R.xml.keyboard_qwerty2_jp_half_symbols_shift_0,
		R.xml.keyboard_katakana50on2_jp_half_0,
		R.xml.keyboard_katakana50on2_jp_half_shift_0,
	};

	/** */

	protected static int selectSubTenQwertyLandKeyTable[] = {
		R.xml.key_subten_qwerty_full_hiragana_0,
		R.xml.key_subten_qwerty_full_hiragana_0,
		R.xml.key_subten_qwerty_full_alphabet_0,
		R.xml.key_subten_qwerty_full_alphabet_0,
		R.xml.key_subten_qwerty_full_num_0,
		R.xml.key_subten_qwerty_full_num_0,
		R.xml.key_subten_qwerty_full_katakana_0,
		R.xml.key_subten_qwerty_full_katakana_0,
		R.xml.key_subten_qwerty_half_alphabet_0,
		R.xml.key_subten_qwerty_half_alphabet_0,
		R.xml.key_subten_qwerty_half_num_0,
		R.xml.key_subten_qwerty_half_num_0,
		R.xml.key_subten_qwerty_half_katakana_0,
		R.xml.key_subten_qwerty_half_katakana_0,
	};

	protected static int selectSubTenQwertyPortKeyTable[] = {
		R.xml.key_subten_qwerty_full_hiragana_0,
		R.xml.key_subten_qwerty_full_hiragana_0,
		R.xml.key_subten_qwerty_full_alphabet_0,
		R.xml.key_subten_qwerty_full_alphabet_0,
		R.xml.key_subten_qwerty_full_num_0,
		R.xml.key_subten_qwerty_full_num_0,
		R.xml.key_subten_qwerty_full_katakana_0,
		R.xml.key_subten_qwerty_full_katakana_0,
		R.xml.key_subten_qwerty_half_alphabet_0,
		R.xml.key_subten_qwerty_half_alphabet_0,
		R.xml.key_subten_qwerty_half_num_0,
		R.xml.key_subten_qwerty_half_num_0,
		R.xml.key_subten_qwerty_half_katakana_0,
		R.xml.key_subten_qwerty_half_katakana_0,
	};

	protected static int selectSubTenQwertyLandKeyTable2[] = {
		R.xml.key_subten_qwerty2_full_hiragana_0,
		R.xml.key_subten_qwerty2_full_hiragana_shift_0,
		R.xml.key_subten_qwerty2_full_alphabet_0,
		R.xml.key_subten_qwerty2_full_alphabet_shift_0,
		R.xml.key_subten_qwerty2_full_num_0,
		R.xml.key_subten_qwerty2_full_num_shift_0,
		R.xml.key_subten_qwerty2_full_katakana_0,
		R.xml.key_subten_qwerty2_full_katakana_shift_0,
		R.xml.key_subten_qwerty2_half_alphabet_0,
		R.xml.key_subten_qwerty2_half_alphabet_shift_0,
		R.xml.key_subten_qwerty2_half_num_0,
		R.xml.key_subten_qwerty2_half_num_shift_0,
		R.xml.key_subten_qwerty2_half_katakana_0,
		R.xml.key_subten_qwerty2_half_katakana_shift_0,
	};

	protected static int selectSubTenQwertyPortKeyTable2[] = {
		R.xml.key_subten_qwerty2_full_hiragana_0,
		R.xml.key_subten_qwerty2_full_hiragana_shift_0,
		R.xml.key_subten_qwerty2_full_alphabet_0,
		R.xml.key_subten_qwerty2_full_alphabet_shift_0,
		R.xml.key_subten_qwerty2_full_num_0,
		R.xml.key_subten_qwerty2_full_num_shift_0,
		R.xml.key_subten_qwerty2_full_katakana_0,
		R.xml.key_subten_qwerty2_full_katakana_shift_0,
		R.xml.key_subten_qwerty2_half_alphabet_0,
		R.xml.key_subten_qwerty2_half_alphabet_shift_0,
		R.xml.key_subten_qwerty2_half_num_0,
		R.xml.key_subten_qwerty2_half_num_shift_0,
		R.xml.key_subten_qwerty2_half_katakana_0,
		R.xml.key_subten_qwerty2_half_katakana_shift_0,
	};

	protected static int selectSubTenQwertyLandKeyTable3[] = {
		R.xml.key_subten_qwerty3_full_hiragana_0,
		R.xml.key_subten_qwerty3_full_hiragana_shift_0,
		R.xml.key_subten_qwerty3_full_alphabet_0,
		R.xml.key_subten_qwerty3_full_alphabet_shift_0,
		R.xml.key_subten_qwerty3_full_num_0,
		R.xml.key_subten_qwerty3_full_num_shift_0,
		R.xml.key_subten_qwerty3_full_katakana_0,
		R.xml.key_subten_qwerty3_full_katakana_shift_0,
		R.xml.key_subten_qwerty3_half_alphabet_0,
		R.xml.key_subten_qwerty3_half_alphabet_shift_0,
		R.xml.key_subten_qwerty3_half_num_0,
		R.xml.key_subten_qwerty3_half_num_shift_0,
		R.xml.key_subten_qwerty3_half_katakana_0,
		R.xml.key_subten_qwerty3_half_katakana_shift_0,
	};

	protected static int selectSubTenQwertyPortKeyTable3[] = {
		R.xml.key_subten_qwerty3_full_hiragana_0,
		R.xml.key_subten_qwerty3_full_hiragana_shift_0,
		R.xml.key_subten_qwerty3_full_alphabet_0,
		R.xml.key_subten_qwerty3_full_alphabet_shift_0,
		R.xml.key_subten_qwerty3_full_num_0,
		R.xml.key_subten_qwerty3_full_num_shift_0,
		R.xml.key_subten_qwerty3_full_katakana_0,
		R.xml.key_subten_qwerty3_full_katakana_shift_0,
		R.xml.key_subten_qwerty3_half_alphabet_0,
		R.xml.key_subten_qwerty3_half_alphabet_shift_0,
		R.xml.key_subten_qwerty3_half_num_0,
		R.xml.key_subten_qwerty3_half_num_shift_0,
		R.xml.key_subten_qwerty3_half_katakana_0,
		R.xml.key_subten_qwerty3_half_katakana_shift_0,
	};

}
