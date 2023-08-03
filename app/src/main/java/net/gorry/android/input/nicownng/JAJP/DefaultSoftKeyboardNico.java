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
public class DefaultSoftKeyboardNico extends DefaultSoftKeyboardJAJP {

	private int mRecursiveOnKeyNico = 0;

	/******************************************************************************************/
	/** Default constructor */
	public DefaultSoftKeyboardNico(final NicoWnnG parent, final SetupKeyboard keyboard) {
		super(parent);
		mSetupKeyboard       = keyboard;
		mCycleTable          = keyboard.SetupCycleTable();
		mCycleTableColumns   = keyboard.GetCycleTableColumns();

		mCurrentKeyMode      = KEYMODE_JA_FULL_NICO;
		mNicoKeyMode         = NICO_MODE_FULL_HIRAGANA;
	}

	/** @see net.gorry.android.input.nicownng.DefaultSoftKeyboard#createKeyboards */
	@Override protected void createKeyboards() {
		super.createKeyboards();

		if (m12keyTable[mCurrentKeyMode] == KEYBOARD_12KEY) {
			mWnn.onEvent(mWnn.mEventChangeMode12Key);
			NicoWnnGJAJP.getInstance().setSpecialCandidateOnKana12KeyToggleMode((mFlickNicoInput > 0) ? 2 : 0);
		} else {
			mWnn.onEvent(mWnn.mEventChangeModeQwerty);
			NicoWnnGJAJP.getInstance().setSpecialCandidateOnKana12KeyToggleMode(0);
		}
	}

	/**
	 * Change input mode
	 * <br>
	 * @param keyMode   The type of input mode
	 */
	@Override
	public void changeKeyMode(final int keyMode) {
		final int targetMode = filterKeyMode(keyMode);
		if (targetMode == INVALID_KEYMODE) {
			return;
		}

		commitText();

		if (mCapsLock) {
			mWnn.onEvent(mWnn.mEventInputShiftLeft);
			mCapsLock = false;
		}
		mShiftOn = KEYBOARD_SHIFT_OFF;
		final MyHeightKeyboard kbd = getModeChangeKeyboard(targetMode);
		mCurrentKeyMode = targetMode;
		mPrevInputKeyCode = 0;
		mPrevInputKeyDir = 0;
		mCurrentSlide = 0;
		mNicoKeyMode = NICO_MODE_FULL_HIRAGANA;

		int mode = NicoWnnGEvent.Mode.DIRECT;

		switch (targetMode) {
			case KEYMODE_JA_HALF_ALPHABET:
				mInputType = INPUT_TYPE_TOGGLE;
				if (m12keyTable[targetMode] == KEYBOARD_12KEY) {
					mode = (mEnglishPredict12Key ? NicoWnnGEvent.Mode.NO_LV1_CONV : NicoWnnGEvent.Mode.DIRECT);
				} else {
					mode = (mEnglishPredictQwerty ? NicoWnnGEvent.Mode.NO_LV1_CONV : NicoWnnGEvent.Mode.DIRECT);
				}
				break;

			case KEYMODE_JA_FULL_NUMBER:
				mInputType = INPUT_TYPE_INSTANT;
				mode = NicoWnnGEvent.Mode.DIRECT;
				mCurrentInstantTable = INSTANT_CHAR_CODE_FULL_NUMBER;
				break;

			case KEYMODE_JA_HALF_NUMBER:
				mInputType = INPUT_TYPE_INSTANT;
				mode = NicoWnnGEvent.Mode.DIRECT;
				mCurrentInstantTable = INSTANT_CHAR_CODE_HALF_NUMBER;
				break;

			case KEYMODE_JA_FULL_ALPHABET:
				mInputType = INPUT_TYPE_TOGGLE;
				mode = NicoWnnGEvent.Mode.DIRECT;
				break;

			case KEYMODE_JA_FULL_NICO:
				mInputType = INPUT_TYPE_TOGGLE;
				mode = NicoWnnGEvent.Mode.DEFAULT;
				resetNicoKeyboard();
				// mNicoFirst = false;
				// mNicoFlick = false;
				break;

			case KEYMODE_JA_FULL_NICO_KATAKANA:
				mInputType = INPUT_TYPE_TOGGLE;
				mNicoKeyMode = NICO_MODE_FULL_KATAKANA;
				mode = NicoWnnGEvent.Mode.DEFAULT;
				resetNicoKeyboard();
				// mNicoFirst = false;
				// mNicoFlick = false;
				break;

			case KEYMODE_JA_HALF_NICO_KATAKANA:
				mInputType = INPUT_TYPE_TOGGLE;
				mNicoKeyMode = NICO_MODE_HALF_KATAKANA;
				mode = NicoWnnGEvent.Mode.DEFAULT;
				resetNicoKeyboard();
				// mNicoFirst = false;
				// mNicoFlick = false;
				break;

			case KEYMODE_JA_FULL_HIRAGANA:
				mInputType = INPUT_TYPE_TOGGLE;
				mode = NicoWnnGEvent.Mode.DEFAULT;
				break;

			case KEYMODE_JA_FULL_KATAKANA:
				mInputType = INPUT_TYPE_TOGGLE;
				mNicoKeyMode = NICO_MODE_FULL_KATAKANA;
				mode = NicoWnnGJAJP.ENGINE_MODE_FULL_KATAKANA;
				break;

			case KEYMODE_JA_HALF_KATAKANA:
				mInputType = INPUT_TYPE_TOGGLE;
				mNicoKeyMode = NICO_MODE_HALF_KATAKANA;
				mode = NicoWnnGJAJP.ENGINE_MODE_HALF_KATAKANA;
				break;

			default:
				break;
		}

		setStatusIcon();
		changeKeyboard(kbd);
		mWnn.onEvent(new NicoWnnGEvent(NicoWnnGEvent.CHANGE_MODE, mode));
		changeKeyboardType(m12keyTable[targetMode]);
	}

	/** @see net.gorry.android.input.nicownng.DefaultSoftKeyboard#initView */
	@Override public View initView(final NicoWnnG parent, final int width, final int height) {
		final View view = super.initView(parent, width, height);
		changeKeyboard(mKeyboard[mCurrentLanguage][mDisplayMode][m12keyTable[mCurrentKeyMode]][mShiftOn][mCurrentKeyMode][0]);
		return view;
	}

	/** @see net.gorry.android.input.nicownng.DefaultSoftKeyboard#setPreferences */
	@Override public void setPreferences(final SharedPreferences pref, final EditorInfo editor) {
		super.setPreferences(pref, editor);

		final int inputType = editor.inputType;
		switch (inputType & InputType.TYPE_MASK_CLASS) {

			case InputType.TYPE_CLASS_NUMBER:
			case InputType.TYPE_CLASS_DATETIME:
			case InputType.TYPE_CLASS_PHONE:
				if (mNoNumberMode) {
					mPreferenceKeyMode = KEYMODE_JA_HALF_ALPHABET;
				} else {
					mPreferenceKeyMode = KEYMODE_JA_HALF_NUMBER;
				}
				break;

			case InputType.TYPE_CLASS_TEXT:
				switch (inputType & InputType.TYPE_MASK_VARIATION) {

					case InputType.TYPE_TEXT_VARIATION_PASSWORD:
						break;

					case InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS:
						break;

					case InputType.TYPE_TEXT_VARIATION_URI:
						break;

					default:
						break;
				}
				break;

			default:
				break;
		}

		setPreferencesJAJP(pref, editor);
	}

	/**
	 * Change the keyboard to default
	 */
	@Override public void setDefaultKeyboard() {
		final Locale locale = Locale.getDefault();
		int keymode = JP_MODE_CYCLE_TABLE[mInputModeStart];

		if (mPreferenceKeyMode != INVALID_KEYMODE) {
			keymode = mPreferenceKeyMode;
		} else if (mLimitedKeyMode != null) {
			keymode = mLimitedKeyMode[0];
		} else {
			if (!locale.getLanguage().equals(Locale.JAPANESE.getLanguage())) {
				keymode = KEYMODE_JA_HALF_ALPHABET;
			}
		}

		changeKeyMode(keymode);
	}

	/** Input mode toggle cycle table */
	private static final int[] JP_MODE_CYCLE_TABLE = {
		KEYMODE_JA_FULL_NICO, KEYMODE_JA_HALF_ALPHABET, KEYMODE_JA_HALF_NUMBER,
	};

	protected int[] getModeCycleTable() {
		return JP_MODE_CYCLE_TABLE;
	}

	/**
	 * Create the keyboard for portrait mode
	 * <br>
	 * @param parent  The context
	 */
	@Override
	protected void createKeyboardsPortrait() {
		mNicoKeyboard = mSetupKeyboard.SetupSoftKeyboard(mWnn, PORTRAIT, mInputViewHeightIndex, mFlickNicoInput, mFlickGuide, mUse12KeySubTen || isMinimizeSoftKeyboardByHardKeyboard(), mSubTen12KeyMode);

		/*
		if (true == isHideSoftKeyboardByHardKeyboard()) {
			return;				// not create soft keyboard
		}
		*/

		int[] keyTable = getPortraitKeyTable();
		int keytype_qwerty = KEYTYPE_QWERTY;
		if (mUseQwertySubTen || isMinimizeSoftKeyboardByHardKeyboard()) {
			switch (mSubTenQwertyMode) {
				default:
				case 0:
					keyTable = selectSubTenQwertyLandKeyTable;
					break;
				case 1:
					keyTable = selectSubTenQwertyLandKeyTable2;
					break;
				case 2:
					keyTable = selectSubTenQwertyLandKeyTable3;
					break;
			}
			keytype_qwerty = KEYTYPE_SUBTEN_QWERTY;
		}

		int[] keyTable2 = selectOtherPortKeyTable;
		int keytype_12key = KEYTYPE_12KEY;
		if (mFlickNicoInput != NICOFLICK_NONE) {
			keyTable2 = selectFlickPortKeyTable;
		}
		if (mUse12KeySubTen || isMinimizeSoftKeyboardByHardKeyboard()) {
			switch (mSubTen12KeyMode) {
				default:
				case 0:
					keyTable2 = selectSubTenPortKeyTable;
					break;
				case 1:
					keyTable2 = selectSubTenPortKeyTable2;
					break;
				case 2:
					keyTable2 = selectSubTenPortKeyTable3;
					break;
			}
			keytype_12key = KEYTYPE_SUBTEN_12KEY;
		}

		MyHeightKeyboard[][] keyList;
		/* qwerty shift_off (portrait) */
		keyList = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_QWERTY][KEYBOARD_SHIFT_OFF];
		keyList[KEYMODE_JA_FULL_HIRAGANA][0] = new MyHeightKeyboard(mWnn, keyTable[0], mInputViewHeightIndex, keytype_qwerty, true);
		keyList[KEYMODE_JA_FULL_ALPHABET][0] = new MyHeightKeyboard(mWnn, keyTable[2], mInputViewHeightIndex, keytype_qwerty, true);
		keyList[KEYMODE_JA_FULL_NUMBER][0]   = new MyHeightKeyboard(mWnn, keyTable[4], mInputViewHeightIndex, keytype_qwerty, true);
		keyList[KEYMODE_JA_FULL_KATAKANA][0] = new MyHeightKeyboard(mWnn, keyTable[6], mInputViewHeightIndex, keytype_qwerty, true);
		keyList[KEYMODE_JA_HALF_ALPHABET][0] = new MyHeightKeyboard(mWnn, keyTable[8], mInputViewHeightIndex, keytype_qwerty, true);
		keyList[KEYMODE_JA_HALF_NUMBER][0]   = new MyHeightKeyboard(mWnn, keyTable[10], mInputViewHeightIndex, keytype_qwerty, true);
		keyList[KEYMODE_JA_HALF_KATAKANA][0] = new MyHeightKeyboard(mWnn, keyTable[12], mInputViewHeightIndex, keytype_qwerty, true);
		keyList[KEYMODE_JA_HALF_PHONE][0]    = new MyHeightKeyboard(mWnn, keyTable2[0], mInputViewHeightIndex, keytype_12key, true);
		keyList[KEYMODE_JA_FULL_NICO][0]     = keyList[KEYMODE_JA_FULL_HIRAGANA][0];
		keyList[KEYMODE_JA_FULL_NICO_KATAKANA][0]     = keyList[KEYMODE_JA_FULL_KATAKANA][0];
		keyList[KEYMODE_JA_HALF_NICO_KATAKANA][0]     = keyList[KEYMODE_JA_HALF_KATAKANA][0];

		/* qwerty shift_on (portrait) */
		keyList = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_QWERTY][KEYBOARD_SHIFT_ON];
		keyList[KEYMODE_JA_FULL_HIRAGANA][0] = new MyHeightKeyboard(mWnn, keyTable[1], mInputViewHeightIndex, keytype_qwerty, true);
		keyList[KEYMODE_JA_FULL_ALPHABET][0] = new MyHeightKeyboard(mWnn, keyTable[3], mInputViewHeightIndex, keytype_qwerty, true);
		keyList[KEYMODE_JA_FULL_NUMBER][0]   = new MyHeightKeyboard(mWnn, keyTable[5], mInputViewHeightIndex, keytype_qwerty, true);
		keyList[KEYMODE_JA_FULL_KATAKANA][0] = new MyHeightKeyboard(mWnn, keyTable[7], mInputViewHeightIndex, keytype_qwerty, true);
		keyList[KEYMODE_JA_HALF_ALPHABET][0] = new MyHeightKeyboard(mWnn, keyTable[9], mInputViewHeightIndex, keytype_qwerty, true);
		keyList[KEYMODE_JA_HALF_NUMBER][0]   = new MyHeightKeyboard(mWnn, keyTable[11], mInputViewHeightIndex, keytype_qwerty, true);
		keyList[KEYMODE_JA_HALF_KATAKANA][0] = new MyHeightKeyboard(mWnn, keyTable[13], mInputViewHeightIndex, keytype_qwerty, true);
		keyList[KEYMODE_JA_HALF_PHONE][0]    = new MyHeightKeyboard(mWnn, keyTable2[0], mInputViewHeightIndex, keytype_12key, true);
		keyList[KEYMODE_JA_FULL_NICO][0]     = keyList[KEYMODE_JA_FULL_HIRAGANA][0];
		keyList[KEYMODE_JA_FULL_NICO_KATAKANA][0]     = keyList[KEYMODE_JA_FULL_KATAKANA][0];
		keyList[KEYMODE_JA_HALF_NICO_KATAKANA][0]     = keyList[KEYMODE_JA_HALF_KATAKANA][0];

		/* 12-keys shift_off (portrait) */
		keyList = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_12KEY][KEYBOARD_SHIFT_OFF];
		keyList[KEYMODE_JA_HALF_PHONE][0]    = new MyHeightKeyboard(mWnn, keyTable2[0], mInputViewHeightIndex, keytype_12key, true);
		keyList[KEYMODE_JA_FULL_ALPHABET][0] = new MyHeightKeyboard(mWnn, keyTable2[1], mInputViewHeightIndex, keytype_12key, true);
		keyList[KEYMODE_JA_FULL_ALPHABET][1] = new MyHeightKeyboard(mWnn, keyTable2[2], mInputViewHeightIndex, keytype_12key, true);
		keyList[KEYMODE_JA_FULL_NUMBER][0]   = new MyHeightKeyboard(mWnn, keyTable2[3], mInputViewHeightIndex, keytype_12key, true);
		keyList[KEYMODE_JA_HALF_ALPHABET][0] = new MyHeightKeyboard(mWnn, keyTable2[4], mInputViewHeightIndex, keytype_12key, true);
		keyList[KEYMODE_JA_HALF_ALPHABET][1] = new MyHeightKeyboard(mWnn, keyTable2[5], mInputViewHeightIndex, keytype_12key, true);
		keyList[KEYMODE_JA_HALF_NUMBER][0]   = new MyHeightKeyboard(mWnn, keyTable2[6], mInputViewHeightIndex, keytype_12key, true);

		/* 12-keys shift_on (portrait) */
		keyList = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_12KEY][KEYBOARD_SHIFT_ON];
		keyList[KEYMODE_JA_HALF_PHONE][0]    = new MyHeightKeyboard(mWnn, keyTable2[7], mInputViewHeightIndex, keytype_12key, true);
		keyList[KEYMODE_JA_FULL_ALPHABET][0] = new MyHeightKeyboard(mWnn, keyTable2[8], mInputViewHeightIndex, keytype_12key, true);
		keyList[KEYMODE_JA_FULL_ALPHABET][1] = new MyHeightKeyboard(mWnn, keyTable2[9], mInputViewHeightIndex, keytype_12key, true);
		keyList[KEYMODE_JA_FULL_NUMBER][0]   = new MyHeightKeyboard(mWnn, keyTable2[10], mInputViewHeightIndex, keytype_12key, true);
		keyList[KEYMODE_JA_HALF_ALPHABET][0] = new MyHeightKeyboard(mWnn, keyTable2[11], mInputViewHeightIndex, keytype_12key, true);
		keyList[KEYMODE_JA_HALF_ALPHABET][1] = new MyHeightKeyboard(mWnn, keyTable2[12], mInputViewHeightIndex, keytype_12key, true);
		keyList[KEYMODE_JA_HALF_NUMBER][0]   = new MyHeightKeyboard(mWnn, keyTable2[13], mInputViewHeightIndex, keytype_12key, true);

		MyHeightKeyboard[][] keyListOn;
		MyHeightKeyboard[][] keyList2nd;
		keyList   = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_12KEY][KEYBOARD_SHIFT_OFF];
		keyListOn = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_12KEY][KEYBOARD_SHIFT_ON];
		keyList2nd = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_12KEY][KEYBOARD_SHIFT_2ND];
		keyList[KEYMODE_JA_FULL_NICO]     = mNicoKeyboard[NICO_MODE_FULL_HIRAGANA][NICO_SLIDE_MODE_TOP];
		keyList[KEYMODE_JA_FULL_NICO_KATAKANA]     = mNicoKeyboard[NICO_MODE_FULL_KATAKANA][NICO_SLIDE_MODE_TOP];
		keyList[KEYMODE_JA_HALF_NICO_KATAKANA]     = mNicoKeyboard[NICO_MODE_HALF_KATAKANA][NICO_SLIDE_MODE_TOP];
		keyListOn[KEYMODE_JA_FULL_NICO]     = mNicoKeyboard[NICO_MODE_FULL_HIRAGANA][NICO_SLIDE_MODE_SHIFT];
		keyListOn[KEYMODE_JA_FULL_NICO_KATAKANA]     = mNicoKeyboard[NICO_MODE_FULL_KATAKANA][NICO_SLIDE_MODE_SHIFT];
		keyListOn[KEYMODE_JA_HALF_NICO_KATAKANA]     = mNicoKeyboard[NICO_MODE_HALF_KATAKANA][NICO_SLIDE_MODE_SHIFT];
		keyList2nd[KEYMODE_JA_FULL_NICO]   = mNicoKeyboard[NICO_MODE_FULL_HIRAGANA][NICO_SLIDE_MODE_A];
		keyList2nd[KEYMODE_JA_FULL_NICO_KATAKANA]   = mNicoKeyboard[NICO_MODE_FULL_KATAKANA][NICO_SLIDE_MODE_A];
		keyList2nd[KEYMODE_JA_HALF_NICO_KATAKANA]   = mNicoKeyboard[NICO_MODE_HALF_KATAKANA][NICO_SLIDE_MODE_A];
	}

	/**
	 * Create the keyboard for landscape mode
	 * <br>
	 * @param parent  The context
	 */
	@Override
	protected void createKeyboardsLandscape() {
		mNicoKeyboard = mSetupKeyboard.SetupSoftKeyboard(mWnn, LANDSCAPE, mInputViewHeightIndex, mFlickNicoInput, mFlickGuide, mUse12KeySubTen || isMinimizeSoftKeyboardByHardKeyboard(), mSubTen12KeyMode);

		/*
		if (true == isHideSoftKeyboardByHardKeyboard()) {
			return;				// not create soft keyboard
		}
		*/

		int[] keyTable = getLandscapeKeyTable();
		int keytype_qwerty = KEYTYPE_QWERTY;
		if (mUseQwertySubTen || isMinimizeSoftKeyboardByHardKeyboard()) {
			switch (mSubTenQwertyMode) {
				default:
				case 0:
					keyTable = selectSubTenQwertyLandKeyTable;
					break;
				case 1:
					keyTable = selectSubTenQwertyLandKeyTable2;
					break;
				case 2:
					keyTable = selectSubTenQwertyLandKeyTable3;
					break;
			}
			keytype_qwerty = KEYTYPE_SUBTEN_QWERTY;
		}

		int[] keyTable2 = selectOtherLandKeyTable;
		int keytype_12key = KEYTYPE_12KEY;
		if (mFlickNicoInput != NICOFLICK_NONE) {
			keyTable2 = selectFlickLandKeyTable;
		}
		if (mUse12KeySubTen || isMinimizeSoftKeyboardByHardKeyboard()) {
			switch (mSubTen12KeyMode) {
				default:
				case 0:
					keyTable2 = selectSubTenLandKeyTable;
					break;
				case 1:
					keyTable2 = selectSubTenLandKeyTable2;
					break;
				case 2:
					keyTable2 = selectSubTenLandKeyTable3;
					break;
			}
			keytype_12key = KEYTYPE_SUBTEN_12KEY;
		}

		MyHeightKeyboard[][] keyList;
		/* qwerty shift_off (landscape) */
		keyList = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_QWERTY][KEYBOARD_SHIFT_OFF];
		keyList[KEYMODE_JA_FULL_HIRAGANA][0] = new MyHeightKeyboard(mWnn, keyTable[0], mInputViewHeightIndex, keytype_qwerty, false);
		keyList[KEYMODE_JA_FULL_ALPHABET][0] = new MyHeightKeyboard(mWnn, keyTable[2], mInputViewHeightIndex, keytype_qwerty, false);
		keyList[KEYMODE_JA_FULL_NUMBER][0]   = new MyHeightKeyboard(mWnn, keyTable[4], mInputViewHeightIndex, keytype_qwerty, false);
		keyList[KEYMODE_JA_FULL_KATAKANA][0] = new MyHeightKeyboard(mWnn, keyTable[6], mInputViewHeightIndex, keytype_qwerty, false);
		keyList[KEYMODE_JA_HALF_ALPHABET][0] = new MyHeightKeyboard(mWnn, keyTable[8], mInputViewHeightIndex, keytype_qwerty, false);
		keyList[KEYMODE_JA_HALF_NUMBER][0]   = new MyHeightKeyboard(mWnn, keyTable[10], mInputViewHeightIndex, keytype_qwerty, false);
		keyList[KEYMODE_JA_HALF_KATAKANA][0] = new MyHeightKeyboard(mWnn, keyTable[12], mInputViewHeightIndex, keytype_qwerty, false);
		keyList[KEYMODE_JA_HALF_PHONE][0]    = new MyHeightKeyboard(mWnn, keyTable2[0], mInputViewHeightIndex, keytype_12key, false);
		keyList[KEYMODE_JA_FULL_NICO][0]     = keyList[KEYMODE_JA_FULL_HIRAGANA][0];
		keyList[KEYMODE_JA_FULL_NICO_KATAKANA][0]     = keyList[KEYMODE_JA_FULL_KATAKANA][0];
		keyList[KEYMODE_JA_HALF_NICO_KATAKANA][0]     = keyList[KEYMODE_JA_HALF_KATAKANA][0];

		/* qwerty shift_on (landscape) */
		keyList = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_QWERTY][KEYBOARD_SHIFT_ON];
		keyList[KEYMODE_JA_FULL_HIRAGANA][0] = new MyHeightKeyboard(mWnn, keyTable[1], mInputViewHeightIndex, keytype_qwerty, false);
		keyList[KEYMODE_JA_FULL_ALPHABET][0] = new MyHeightKeyboard(mWnn, keyTable[3], mInputViewHeightIndex, keytype_qwerty, false);
		keyList[KEYMODE_JA_FULL_NUMBER][0]   = new MyHeightKeyboard(mWnn, keyTable[5], mInputViewHeightIndex, keytype_qwerty, false);
		keyList[KEYMODE_JA_FULL_KATAKANA][0] = new MyHeightKeyboard(mWnn, keyTable[7], mInputViewHeightIndex, keytype_qwerty, false);
		keyList[KEYMODE_JA_HALF_ALPHABET][0] = new MyHeightKeyboard(mWnn, keyTable[9], mInputViewHeightIndex, keytype_qwerty, false);
		keyList[KEYMODE_JA_HALF_NUMBER][0]   = new MyHeightKeyboard(mWnn, keyTable[11], mInputViewHeightIndex, keytype_qwerty, false);
		keyList[KEYMODE_JA_HALF_KATAKANA][0] = new MyHeightKeyboard(mWnn, keyTable[13], mInputViewHeightIndex, keytype_qwerty, false);
		keyList[KEYMODE_JA_HALF_PHONE][0]    = new MyHeightKeyboard(mWnn, keyTable2[0], mInputViewHeightIndex, keytype_12key, false);
		keyList[KEYMODE_JA_FULL_NICO][0]     = keyList[KEYMODE_JA_FULL_HIRAGANA][0];
		keyList[KEYMODE_JA_FULL_NICO_KATAKANA][0]     = keyList[KEYMODE_JA_FULL_KATAKANA][0];
		keyList[KEYMODE_JA_HALF_NICO_KATAKANA][0]     = keyList[KEYMODE_JA_HALF_KATAKANA][0];

		/* 12-keys shift_off (landscape) */
		keyList = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_12KEY][KEYBOARD_SHIFT_OFF];
		keyList[KEYMODE_JA_HALF_PHONE][0]    = new MyHeightKeyboard(mWnn, keyTable2[0], mInputViewHeightIndex, keytype_12key, false);
		keyList[KEYMODE_JA_FULL_ALPHABET][0] = new MyHeightKeyboard(mWnn, keyTable2[1], mInputViewHeightIndex, keytype_12key, false);
		keyList[KEYMODE_JA_FULL_ALPHABET][1] = new MyHeightKeyboard(mWnn, keyTable2[2], mInputViewHeightIndex, keytype_12key, false);
		keyList[KEYMODE_JA_FULL_NUMBER][0]   = new MyHeightKeyboard(mWnn, keyTable2[3], mInputViewHeightIndex, keytype_12key, false);
		keyList[KEYMODE_JA_HALF_ALPHABET][0] = new MyHeightKeyboard(mWnn, keyTable2[4], mInputViewHeightIndex, keytype_12key, false);
		keyList[KEYMODE_JA_HALF_ALPHABET][1] = new MyHeightKeyboard(mWnn, keyTable2[5], mInputViewHeightIndex, keytype_12key, false);
		keyList[KEYMODE_JA_HALF_NUMBER][0]   = new MyHeightKeyboard(mWnn, keyTable2[6], mInputViewHeightIndex, keytype_12key, false);

		/* 12-keys shift_on (landscape) */
		keyList = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_12KEY][KEYBOARD_SHIFT_ON];
		keyList[KEYMODE_JA_HALF_PHONE][0]    = new MyHeightKeyboard(mWnn, keyTable2[7], mInputViewHeightIndex, keytype_12key, false);
		keyList[KEYMODE_JA_FULL_ALPHABET][0] = new MyHeightKeyboard(mWnn, keyTable2[8], mInputViewHeightIndex, keytype_12key, false);
		keyList[KEYMODE_JA_FULL_ALPHABET][1] = new MyHeightKeyboard(mWnn, keyTable2[9], mInputViewHeightIndex, keytype_12key, false);
		keyList[KEYMODE_JA_FULL_NUMBER][0]   = new MyHeightKeyboard(mWnn, keyTable2[10], mInputViewHeightIndex, keytype_12key, false);
		keyList[KEYMODE_JA_HALF_ALPHABET][0] = new MyHeightKeyboard(mWnn, keyTable2[11], mInputViewHeightIndex, keytype_12key, false);
		keyList[KEYMODE_JA_HALF_ALPHABET][1] = new MyHeightKeyboard(mWnn, keyTable2[12], mInputViewHeightIndex, keytype_12key, false);
		keyList[KEYMODE_JA_HALF_NUMBER][0]   = new MyHeightKeyboard(mWnn, keyTable2[13], mInputViewHeightIndex, keytype_12key, false);

		MyHeightKeyboard[][] keyListOn;
		MyHeightKeyboard[][] keyList2nd;
		keyList    = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_12KEY][KEYBOARD_SHIFT_OFF];
		keyListOn  = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_12KEY][KEYBOARD_SHIFT_ON];
		keyList2nd = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_12KEY][KEYBOARD_SHIFT_2ND];
		keyList[KEYMODE_JA_FULL_NICO]     = mNicoKeyboard[NICO_MODE_FULL_HIRAGANA][NICO_SLIDE_MODE_TOP];
		keyList[KEYMODE_JA_FULL_NICO_KATAKANA]     = mNicoKeyboard[NICO_MODE_FULL_KATAKANA][NICO_SLIDE_MODE_TOP];
		keyList[KEYMODE_JA_HALF_NICO_KATAKANA]     = mNicoKeyboard[NICO_MODE_HALF_KATAKANA][NICO_SLIDE_MODE_TOP];
		keyListOn[KEYMODE_JA_FULL_NICO]     = mNicoKeyboard[NICO_MODE_FULL_HIRAGANA][NICO_SLIDE_MODE_SHIFT];
		keyListOn[KEYMODE_JA_FULL_NICO_KATAKANA]     = mNicoKeyboard[NICO_MODE_FULL_KATAKANA][NICO_SLIDE_MODE_SHIFT];
		keyListOn[KEYMODE_JA_HALF_NICO_KATAKANA]     = mNicoKeyboard[NICO_MODE_HALF_KATAKANA][NICO_SLIDE_MODE_SHIFT];
		keyList2nd[KEYMODE_JA_FULL_NICO]   = mNicoKeyboard[NICO_MODE_FULL_HIRAGANA][NICO_SLIDE_MODE_A];
		keyList2nd[KEYMODE_JA_FULL_NICO_KATAKANA]   = mNicoKeyboard[NICO_MODE_FULL_KATAKANA][NICO_SLIDE_MODE_A];
		keyList2nd[KEYMODE_JA_HALF_NICO_KATAKANA]   = mNicoKeyboard[NICO_MODE_HALF_KATAKANA][NICO_SLIDE_MODE_A];
	}

	/**
	 * Set the status icon that is appropriate in current mode
	 */
	@Override
	public void setStatusIcon() {
		int icon = 0;

		switch (mCurrentKeyMode) {
			case KEYMODE_JA_FULL_HIRAGANA:
				icon = R.drawable.immodeic_hiragana;
				break;
			case KEYMODE_JA_FULL_KATAKANA:
			case KEYMODE_JA_FULL_NICO_KATAKANA:
				icon = R.drawable.immodeic_full_kana;
				break;
			case KEYMODE_JA_HALF_KATAKANA:
			case KEYMODE_JA_HALF_NICO_KATAKANA:
				icon = R.drawable.immodeic_half_kana;
				break;
			case KEYMODE_JA_FULL_ALPHABET:
				icon = R.drawable.immodeic_full_alphabet;
				break;
			case KEYMODE_JA_FULL_NUMBER:
				icon = R.drawable.immodeic_full_number;
				break;
			case KEYMODE_JA_HALF_ALPHABET:
				icon = R.drawable.immodeic_half_alphabet;
				break;
			case KEYMODE_JA_HALF_NUMBER:
			case KEYMODE_JA_HALF_PHONE:
				icon = R.drawable.immodeic_half_number;
				break;
			case KEYMODE_JA_FULL_NICO:
				icon = mSetupKeyboard.SetupIcon();
				if (m12keyTable[mCurrentKeyMode] == KEYBOARD_QWERTY) {
					icon = R.drawable.immodeic_hiragana;
				}
				break;

			default:
				break;
		}

		mWnn.showStatusIcon(icon);
	}

	/**
	 * Get the toggle table for input that is appropriate in current mode.
	 *
	 * @return      The toggle table for input
	 */
	protected String[][] getCycleTable() {
		switch (mCurrentKeyMode) {
			case KEYMODE_JA_FULL_NICO:
			case KEYMODE_JA_FULL_NICO_KATAKANA:
			case KEYMODE_JA_HALF_NICO_KATAKANA:
				return mCycleTable[mNicoKeyMode][mChangeAlphaBigMode];
		}
		return super.getCycleTable();
	}

	/**
	 * Get the replace table that is appropriate in current mode.
	 *
	 * @return      The replace table
	 */
	protected HashMap<String,String> getReplaceTable(final boolean du) {
		switch (mCurrentKeyMode) {
			case KEYMODE_JA_FULL_NICO:
			case KEYMODE_JA_FULL_NICO_KATAKANA:
			case KEYMODE_JA_HALF_NICO_KATAKANA:
				return mSetupKeyboard.SetupReplaceTable(mTsuMode);
		}
		return super.getReplaceTable(du);
	}

	@Override public String[] convertFlickToKeyString(int flickdir) {
		if (
				(mCurrentKeyMode == KEYMODE_JA_FULL_NICO) ||
				(mCurrentKeyMode == KEYMODE_JA_FULL_NICO_KATAKANA) ||
				(mCurrentKeyMode == KEYMODE_JA_HALF_NICO_KATAKANA)
		) {
			return convertFlickToKeyStringNico(flickdir);
		}

		return super.convertFlickToKeyString(flickdir);
	}

	/*****************************************
	 * onkey (normal)
	 */
	/** @see net.gorry.android.input.nicownng.DefaultSoftKeyboard#onKey */
	@Override public void onKey(final int primaryCode2, final int[] keyCodes) {
		int primaryCode = primaryCode2;
		boolean changeShiftLock = true;
		boolean changeAltLock = true;
		boolean changeCtrlLock = true;
		boolean metaAltStatus = mCurrentKeyboard.getAltKeyIndicator();
		boolean metaCtrlStatus = mCurrentKeyboard.getCtrlKeyIndicator();
		final InputConnection connection = mWnn.getCurrentInputConnection();
		KeyEvent keyEvent = null;

		// if (mDisableKeyInput) {
		// 	return;
		// }

		if (mAutoForwardToggle12key) {
			mWnn.resetAutoForwardToggle12key();
		}
		
		if (
				(mCurrentKeyMode == KEYMODE_JA_FULL_NICO) ||
				(mCurrentKeyMode == KEYMODE_JA_FULL_NICO_KATAKANA) ||
				(mCurrentKeyMode == KEYMODE_JA_HALF_NICO_KATAKANA)
		) {
			if (true == onKeyNico(primaryCode, keyCodes)) {
				/* update shift key's state */
				if (!mCapsLock && (primaryCode != DefaultSoftKeyboard.KEYCODE_QWERTY_SHIFT)) {
					setShiftByEditorInfo();
				}
				return;
			}
		}

		final int getFlickCode = checkFlickKeyCode(true, false);
		if (getFlickCode >= 0) {
			// get flick keydata
			final String[] keyString = convertFlickToKeyString(getFlickCode);
			if (null != keyString) {
				mWnn.onEvent(mWnn.mEventTouchOtherKey);
				mWnn.onEvent(new NicoWnnGEvent(NicoWnnGEvent.TOGGLE_CHAR, keyString));
				mNicoFirst = false;
				mNicoFlick = false;
				return;
			}
		} else if (getFlickCode < -1) {
			switch (primaryCode) {
			case KEYCODE_JP12_CONVPREDICT:
			case KEYCODE_JP12_CONVPREDICT_BACKWARD:
				break;
			default:
				primaryCode = getFlickCode;
			}
		}

		switch (primaryCode) {
			case KEYCODE_JP12_TOGGLE_MODE2:
			case KEYCODE_QWERTY_TOGGLE_MODE2:
			case KEYCODE_JP12_TOGGLE_MODE:
			case KEYCODE_QWERTY_TOGGLE_MODE:
				if (isLongPress()) {
					break;
				}
				if (!mWnn.isPasswordInputMode()) {
					if (!mNoInput) {
						final NicoWnnGJAJP wnn = NicoWnnGJAJP.getInstance();
						wnn.changeAlphaKanaDirectPhase();
						break;
					}
				}
				if (!mIsInputTypeNull) {
					nextKeyMode();
				}
				break;

			case KEYCODE_JP12_TOGGLE_MODE_TOP:
			case KEYCODE_QWERTY_TOGGLE_MODE_TOP:
				if (isLongPress()) {
					break;
				}
				if (!mWnn.isPasswordInputMode()) {
					if (!mNoInput) {
						final NicoWnnGJAJP wnn = NicoWnnGJAJP.getInstance();
						wnn.changeAlphaKanaDirectPhase();
						break;
					}
				}
				if (!mIsInputTypeNull) {
					nextKeyModeTop();
				}
				break;

			case KEYCODE_JP12_TOGGLE_MODE_BACK:
			case KEYCODE_QWERTY_TOGGLE_MODE_BACK:
				if (isLongPress()) {
					break;
				}
				if (!mWnn.isPasswordInputMode()) {
					if (!mNoInput) {
						final NicoWnnGJAJP wnn = NicoWnnGJAJP.getInstance();
						wnn.changeAlphaKanaDirectPhase();
						break;
					}
				}
				if (!mIsInputTypeNull) {
					nextKeyModeBack();
				}
				break;

			case DefaultSoftKeyboard.KEYCODE_QWERTY_BACKSPACE:
			case KEYCODE_JP12_BACKSPACE:
				mWnn.onEvent(mWnn.mEventInputKeyDel);
				changeShiftLock = false;
				break;

			case KEYCODE_QWERTY_SHIFT:
				if (mKeyRepeatCount == 2) {
					toggleShiftLock(3);
				} else if (mKeyRepeatCount == 0) {
					//
				} else {
					toggleShiftLock(1);
				}
				changeShiftLock = false;
				changeAltLock = false;
				changeCtrlLock = false;
				// if (mCurrentKeyboard.isShifted()) {
				if (mCurrentKeyboard.getShiftKeyIndicator()) {
					keyEvent = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SHIFT_LEFT);
				} else {
					keyEvent = new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_SHIFT_LEFT);
				}
				if (connection != null) {
					connection.sendKeyEvent(keyEvent);
				}
				break;

			case KEYCODE_QWERTY_ALT:
			case Keyboard.KEYCODE_ALT:
				// processAltKey();
				changeShiftLock = false;
				changeAltLock = false;
				changeCtrlLock = false;
				if (mCurrentKeyboard.getAltKeyIndicator()) {
					keyEvent = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ALT_LEFT);
				} else {
					if (Build.VERSION.SDK_INT < 11) {
						if (connection != null) {
							connection.clearMetaKeyStates(KeyEvent.META_ALT_ON|KeyEvent.META_ALT_LEFT_ON|KeyEvent.META_ALT_RIGHT_ON);
						}
					}
					keyEvent = new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ALT_LEFT);
				}
				if (connection != null) {
					connection.sendKeyEvent(keyEvent);
				}
				break;

			case KEYCODE_QWERTY_CTRL:
				// processCtrlKey();
				changeShiftLock = false;
				changeAltLock = false;
				changeCtrlLock = false;
				if (mCurrentKeyboard.getCtrlKeyIndicator()) {
					// keyEvent = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_CTRL_LEFT);
					// keyEvent = new KeyEvent(KeyEvent.ACTION_DOWN, 113);
					keyEvent = new KeyEvent(0, 0, KeyEvent.ACTION_DOWN, 113, 0, 0x3000);
				} else {
					if (Build.VERSION.SDK_INT < 11) {
						if (connection != null) {
							// connection.clearMetaKeyStates(KeyEvent.META_CTRL_ON|KeyEvent.META_CTRL_LEFT_ON|KeyEvent.META_CTRL_RIGHT_ON);
							connection.clearMetaKeyStates(0x00001000|0x00002000|0x00004000);
						}
					}
					// keyEvent = new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_CTRL_LEFT);
					keyEvent = new KeyEvent(KeyEvent.ACTION_UP, 113);
				}
				if (connection != null) {
					connection.sendKeyEvent(keyEvent);
				}
				break;

			case KEYCODE_QWERTY_ENTER:
			case KEYCODE_QWERTY_MINIENTER:
			case KEYCODE_QWERTY_MINIENTER2:
			case KEYCODE_JP12_ENTER:
				mWnn.onEvent(mWnn.mEventInputEnter);
				break;

			case KEYCODE_JP12_REVERSE:
				if (!mNoInput) {
					NicoWnnGJAJP t = NicoWnnGJAJP.getInstance();
					if (t.candidatesViewManagerIsShown() && t.candidatesViewManagerIsIndicated()) {
						mWnn.onEvent(new NicoWnnGEvent(NicoWnnGEvent.INPUT_SOFT_KEY, new KeyEvent(KeyEvent.ACTION_DOWN, KEYCODE_JP12_REVERSE)));
					} else if (t.isRenbun()||t.isPredict()) {
						mWnn.onEvent(new NicoWnnGEvent(NicoWnnGEvent.INPUT_KEY, new KeyEvent(KeyEvent.ACTION_DOWN, KEYCODE_JP12_REVERSE)));
					} else {
						mWnn.onEvent(new NicoWnnGEvent(NicoWnnGEvent.TOGGLE_REVERSE_CHAR, mCurrentCycleTable));
					}
					if (mAutoForwardToggle12key) {
						mWnn.resetAutoForwardToggle12key();
					}
				}
				break;

			case KEYCODE_QWERTY_KBD:
				changeKeyboardType(KEYBOARD_12KEY);
				break;

			case KEYCODE_JP12_KBD:
				changeKeyboardType(KEYBOARD_QWERTY);
				break;

			case KEYCODE_JP12_EMOJI:
			case KEYCODE_QWERTY_EMOJI:
				commitText();
				mWnn.onEvent(mWnn.mEventChangeModeSymbol);
				break;

			case KEYCODE_JP12_1:
			case KEYCODE_JP12_2:
			case KEYCODE_JP12_3:
			case KEYCODE_JP12_4:
			case KEYCODE_JP12_5:
			case KEYCODE_JP12_6:
			case KEYCODE_JP12_7:
			case KEYCODE_JP12_8:
			case KEYCODE_JP12_9:
			case KEYCODE_JP12_0:
			case KEYCODE_JP12_SHARP:
				/* Processing to input by ten key */
				if (mInputType == INPUT_TYPE_INSTANT) {
					/* Send a input character directly if instant input type is selected */
					commitText();
					mWnn.onEvent(new NicoWnnGEvent(NicoWnnGEvent.INPUT_CHAR, mCurrentInstantTable[getTableIndex(primaryCode)]));
				} else {
					if ((mPrevInputKeyCode != primaryCode)) {
						mWnn.onEvent(mWnn.mEventTouchOtherKey);
						if (mCurrentKeyMode == KEYMODE_JA_HALF_ALPHABET) {
							if (primaryCode == KEYCODE_JP12_SHARP) {
								/* Commit text by symbol character (',' '.') when alphabet input mode is selected */
								commitText();
							}
						}
					}

					if (mCurrentKeyMode == KEYMODE_JA_HALF_ALPHABET) {
						if (mAutoForwardToggle12key) {
							mWnn.setAutoForwardToggle12key();
						}
					}

					/* Convert the key code to the table index and send the toggle event with the table index */
					final String[][] cycleTable = getCycleTable();
					if (cycleTable == null) {
						Log.e("NicoWnnG", "not founds cycle table");
					} else {
						final int index = getTableIndex(primaryCode);
						mWnn.onEvent(new NicoWnnGEvent(NicoWnnGEvent.TOGGLE_CHAR, cycleTable[index]));
						mCurrentCycleTable = cycleTable[index];
					}
					mPrevInputKeyCode = primaryCode;
				}
				break;

			case KEYCODE_JP12_ASTER:
				if (mInputType == INPUT_TYPE_INSTANT) {
					commitText();
					mWnn.onEvent(new NicoWnnGEvent(NicoWnnGEvent.INPUT_CHAR, mCurrentInstantTable[getTableIndex(primaryCode)]));
				} else {
					if (!mNoInput || mWnn.isPasswordInputMode()) {
						/* Processing to toggle Dakuten, Handakuten, and capital */
						final HashMap<String,String> replaceTable = getReplaceTable(mTsuMode);
						if (replaceTable == null) {
							Log.e("NicoWnnG", "not founds replace table");
						} else {
							mWnn.onEvent(new NicoWnnGEvent(NicoWnnGEvent.REPLACE_CHAR, replaceTable));
							mPrevInputKeyCode = primaryCode;
						}
					} else {
						mWnn.onEvent(mWnn.mEventInputEnter);
					}
				}
				break;

			case KEYCODE_SWITCH_FULL_HIRAGANA:
				/* Change mode to Full width hiragana */
				changeKeyMode(KEYMODE_JA_FULL_NICO);
				break;

			case KEYCODE_SWITCH_FULL_KATAKANA:
				/* Change mode to Full width katakana */
				changeKeyMode(KEYMODE_JA_FULL_NICO_KATAKANA);
				break;

			case KEYCODE_SWITCH_FULL_ALPHABET:
				/* Change mode to Full width alphabet */
				changeKeyMode(KEYMODE_JA_FULL_ALPHABET);
				break;

			case KEYCODE_SWITCH_FULL_NUMBER:
				/* Change mode to Full width numeric */
				changeKeyMode(KEYMODE_JA_FULL_NUMBER);
				break;

			case KEYCODE_SWITCH_HALF_KATAKANA:
				/* Change mode to Half width katakana */
				changeKeyMode(KEYMODE_JA_HALF_NICO_KATAKANA);
				break;

			case KEYCODE_SWITCH_HALF_ALPHABET:
				/* Change mode to Half width alphabet */
				changeKeyMode(KEYMODE_JA_HALF_ALPHABET);
				break;

			case KEYCODE_SWITCH_HALF_NUMBER:
				/* Change mode to Half width numeric */
				changeKeyMode(KEYMODE_JA_HALF_NUMBER);
				break;

			case KEYCODE_SWITCH_FULL_NICO:
				/* Change mode to Full width nicotouch */
				changeKeyMode(KEYMODE_JA_FULL_NICO);
				break;

			case KEYCODE_SWITCH_FULL_NICO_KATAKANA:
				/* Change mode to Full width nicotouch katakana */
				changeKeyMode(KEYMODE_JA_FULL_NICO_KATAKANA);
				break;

			case KEYCODE_SWITCH_HALF_NICO_KATAKANA:
				/* Change mode to Half width nicotouch katakana */
				changeKeyMode(KEYMODE_JA_HALF_NICO_KATAKANA);
				break;

			case KEYCODE_SELECT_CASE:
				final int shifted = (mShiftOn == 0) ? 1 : 0;
				final MyHeightKeyboard newKeyboard = getShiftChangeKeyboard(shifted);
				if (newKeyboard != null) {
					mShiftOn = shifted;
					changeKeyboard(newKeyboard);
				}
				break;

			case KEYCODE_JP12_SPACE:
				{
					NicoWnnGJAJP t = NicoWnnGJAJP.getInstance();
					if (t.candidatesViewManagerIsShown() && t.candidatesViewManagerIsIndicated()) {
						mWnn.onEvent(new NicoWnnGEvent(NicoWnnGEvent.INPUT_SOFT_KEY, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SPACE)));
					} else if (t.isRenbun()||t.isPredict()) {
						mWnn.onEvent(new NicoWnnGEvent(NicoWnnGEvent.INPUT_KEY, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SPACE)));
						mWnn.onEvent(new NicoWnnGEvent(NicoWnnGEvent.INPUT_CHAR, ' '));
					} else if ((mCurrentKeyMode == KEYMODE_JA_FULL_HIRAGANA) && !mNoInput) {
						mWnn.onEvent(mWnn.mEventConvert);
					} else if ((mCurrentKeyMode == KEYMODE_JA_FULL_NICO) && !mNoInput) {
						mWnn.onEvent(mWnn.mEventConvert);
					} else if ((mCurrentKeyMode == KEYMODE_JA_FULL_NICO_KATAKANA) && !mNoInput) {
						mWnn.onEvent(mWnn.mEventConvert);
					} else if (t.candidatesViewManagerIsShown()) {
						mWnn.onEvent(new NicoWnnGEvent(NicoWnnGEvent.INPUT_SOFT_KEY, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SPACE)));
					} else if (mNoInput) {
						switch (mCurrentKeyMode) {
							case KEYMODE_JA_FULL_HIRAGANA:
							case KEYMODE_JA_FULL_KATAKANA:
							case KEYMODE_JA_FULL_ALPHABET:
							case KEYMODE_JA_FULL_NUMBER:
							case KEYMODE_JA_FULL_NICO:
							case KEYMODE_JA_FULL_NICO_KATAKANA:
								switch (primaryCode) {
								  case KEYCODE_JP12_SPACE:
								  	if (mKana12SpaceZen) {
										mWnn.onEvent(new NicoWnnGEvent(NicoWnnGEvent.INPUT_CHAR, '\u3000'));
									} else {
										mWnn.onEvent(new NicoWnnGEvent(NicoWnnGEvent.INPUT_CHAR, ' '));
									}
									break;
								  case KEYCODE_JP12_SPACE_SHIFT:
								  	if (mKana12SpaceZen) {
										mWnn.onEvent(new NicoWnnGEvent(NicoWnnGEvent.INPUT_CHAR, ' '));
									} else {
										mWnn.onEvent(new NicoWnnGEvent(NicoWnnGEvent.INPUT_CHAR, '\u3000'));
									}
									break;
								}
								break;
							default:
								mWnn.onEvent(new NicoWnnGEvent(NicoWnnGEvent.INPUT_CHAR, ' '));
								break;
						}
					} else {
						mWnn.onEvent(new NicoWnnGEvent(NicoWnnGEvent.INPUT_CHAR, ' '));
					}
				}
				break;

			case KEYCODE_QWERTY_ZEN_SPACE:
			case KEYCODE_QWERTY_ZEN_SPACE2:
			case KEYCODE_QWERTY_ZEN_SPACE_SHIFT:
			case KEYCODE_QWERTY_ZEN_SPACE2_SHIFT:
				{
					NicoWnnGJAJP t = NicoWnnGJAJP.getInstance();
					if (t.candidatesViewManagerIsShown() && t.candidatesViewManagerIsIndicated()) {
						mWnn.onEvent(new NicoWnnGEvent(NicoWnnGEvent.INPUT_SOFT_KEY, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SPACE)));
					} else if (t.isRenbun()||t.isPredict()) {
						mWnn.onEvent(new NicoWnnGEvent(NicoWnnGEvent.INPUT_KEY, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SPACE)));
					} else if ((mCurrentKeyMode == KEYMODE_JA_FULL_HIRAGANA) && !mNoInput) {
						mWnn.onEvent(mWnn.mEventConvert);
					} else if (t.candidatesViewManagerIsShown()) {
						mWnn.onEvent(new NicoWnnGEvent(NicoWnnGEvent.INPUT_SOFT_KEY, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SPACE)));
					} else {
						switch (primaryCode) {
						  case KEYCODE_QWERTY_ZEN_SPACE:
						  case KEYCODE_QWERTY_ZEN_SPACE2:
							if (mQwertySpaceZen) {
								mWnn.onEvent(new NicoWnnGEvent(NicoWnnGEvent.INPUT_CHAR, '\u3000'));
							} else {
								mWnn.onEvent(new NicoWnnGEvent(NicoWnnGEvent.INPUT_CHAR, ' '));
							}
							break;
						  case KEYCODE_QWERTY_ZEN_SPACE_SHIFT:
						  case KEYCODE_QWERTY_ZEN_SPACE2_SHIFT:
							if (mQwertySpaceZen) {
								mWnn.onEvent(new NicoWnnGEvent(NicoWnnGEvent.INPUT_CHAR, ' '));
							} else {
								mWnn.onEvent(new NicoWnnGEvent(NicoWnnGEvent.INPUT_CHAR, '\u3000'));
							}
							break;
						}
					}
				}
				break;

			case KEYCODE_EISU_KANA:
				mWnn.onEvent(mWnn.mEventChangeModeEisuKana);
				break;

			case KEYCODE_JP12_CLOSE:
				mWnn.onEvent(mWnn.mEventInputBack);
				break;

			case KEYCODE_JP12_LEFT:
				if (!isEndOfArrowKeyRepeat()) {
					mWnn.onEvent(mWnn.mEventInputDpadLeft);
				}
				changeShiftLock = false;
				break;

			case KEYCODE_JP12_RIGHT:
				if (!isEndOfArrowKeyRepeat()) {
					mWnn.onEvent(mWnn.mEventInputDpadRight);
				}
				changeShiftLock = false;
				break;

			case KEYCODE_JP12_UP:
				if (!isEndOfArrowKeyRepeat()) {
					mWnn.onEvent(mWnn.mEventInputDpadUp);
				}
				changeShiftLock = false;
				break;

			case KEYCODE_JP12_DOWN:
				if (!isEndOfArrowKeyRepeat()) {
					mWnn.onEvent(mWnn.mEventInputDpadDown);
				}
				changeShiftLock = false;
				break;

			case KEYCODE_ARROW_STOP:
				changeShiftLock = false;
				break;

			case KEYCODE_NOP:
				break;

			case KEYCODE_PREFERENCE_SETTING:
				if (mWnn != null) {
					try {
						mWnn.openPreferenceSetting();
					} catch (final Exception e) {
						//
					}
				}
				break;

			case KEYCODE_SHOW_HELP:
				if (mWnn != null) {
					//try {
					mWnn.openHelp();
					//} catch (final Exception e) {
					//
					//}
				}
				break;

			case KEYCODE_MUSHROOM:
				if (mWnn != null) {
					final String str = mWnn.getComposingText(ComposingText.LAYER2);
					mWnn.onEvent(mWnn.mEventInputBack);
					mWnn.invokeMushroom(str);
				}
				break;

			case KEYCODE_JP12_CONVPREDICT:
				mWnn.onEvent(mWnn.mEventConvertPredict);
				break;

			case KEYCODE_JP12_CONVPREDICT_BACKWARD:
				mWnn.onEvent(mWnn.mEventConvertPredictBackward);
				break;

			case KEYCODE_USERSYMBOL_ZEN_HIRAGANA:
			case KEYCODE_USERSYMBOL_ZEN_KATAKANA:
			case KEYCODE_USERSYMBOL_ZEN_ALPHABET:
			case KEYCODE_USERSYMBOL_ZEN_NUMBER:
			case KEYCODE_USERSYMBOL_HAN_KATAKANA:
			case KEYCODE_USERSYMBOL_HAN_ALPHABET:
			case KEYCODE_USERSYMBOL_HAN_NUMBER:
				changeShiftLock = false;
				changeAltLock = false;
				changeCtrlLock = false;
				commitText();
				mWnn.onEvent(new NicoWnnGEvent(NicoWnnGEvent.CHANGE_MODE, NicoWnnGJAJP.ENGINE_MODE_USERSYMBOL, -primaryCode));
				break;

			case KEYCODE_FUNCTION_SELECTALL:
				if (connection != null) {
					connection.performContextMenuAction(android.R.id.selectAll);
				}
				changeShiftLock = false;
				break;

			case KEYCODE_FUNCTION_CUT:
				if (connection != null) {
					connection.performContextMenuAction(android.R.id.cut);
					if (mCurrentKeyboard.getShiftKeyIndicator()) {
						keyEvent = new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_SHIFT_LEFT);
						connection.sendKeyEvent(keyEvent);
						if (mCurrentKeyboard.getSelKeyIndicator()) {
							mCurrentKeyboard.setSelKeyIndicator(false);
							mKeyboardView.invalidateKey(mCurrentKeyboard.setSelKeyIndex());
						} else {
							toggleShiftLock(2);
						}
					}
				}
				changeShiftLock = false;
				break;

			case KEYCODE_FUNCTION_COPY:
				if (connection != null) {
					connection.performContextMenuAction(android.R.id.copy);
					if (mCurrentKeyboard.getShiftKeyIndicator()) {
						keyEvent = new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_SHIFT_LEFT);
						connection.sendKeyEvent(keyEvent);
						if (mCurrentKeyboard.getSelKeyIndicator()) {
							mCurrentKeyboard.setSelKeyIndicator(false);
							mKeyboardView.invalidateKey(mCurrentKeyboard.setSelKeyIndex());
						} else {
							toggleShiftLock(2);
						}
					}
				}
				changeShiftLock = false;
				break;

			case KEYCODE_FUNCTION_PASTE:
				if (connection != null) {
					connection.performContextMenuAction(android.R.id.paste);
				}
				changeShiftLock = false;
				break;

			case KEYCODE_FUNCTION_SELECT:
				if (connection != null) {
					boolean sw = mCurrentKeyboard.getSelKeyIndicator();
					if (sw) {
						keyEvent = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SHIFT_LEFT);
					} else {
						keyEvent = new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_SHIFT_LEFT);
					}
					connection.sendKeyEvent(keyEvent);
				}
				changeShiftLock = false;
				break;

			default:
				if (mCurrentKeyboard.getAltKeyIndicator() || mCurrentKeyboard.getCtrlKeyIndicator()) {
					boolean send = false;
					if ((mCurrentKeyboard.getAltKeyIndicator() && ((mCutPasteActionByIme & CUTPASTEACTIONBYIME_ALT) != 0)) ||
							(mCurrentKeyboard.getCtrlKeyIndicator() && ((mCutPasteActionByIme & CUTPASTEACTIONBYIME_CTRL) != 0))) {
						if (connection != null) {
							switch (primaryCode) {
								case 'a':
								case 'A':
								case 12385:  // hiragana ti
								case 12481:  // katakana ti
								case 65409:  // katakana ti
									connection.performContextMenuAction(android.R.id.selectAll);
									send = true;
									break;
								case 'x':
								case 'X':
								case 12373:  // hiragana sa
								case 12469:  // katakana sa
								case 65403:  // katakana sa
									connection.performContextMenuAction(android.R.id.cut);
									send = true;
									break;
								case 'c':
								case 'C':
								case 12381:  // hiragana so
								case 12477:  // katakana so
								case 65407:  // katakana sa
									connection.performContextMenuAction(android.R.id.copy);
									send = true;
									break;
								case 'v':
								case 'V':
								case 12402:  // hiragana hi
								case 12498:  // katakana hi
								case 65419:  // katakana sa
									connection.performContextMenuAction(android.R.id.paste);
									send = true;
									break;
							}
						}
					}
					if (!send) {
						if (primaryCode >= 0) {
							int keyCode = convertCharacterToKeyCode(primaryCode);
							if (keyCode > 0) {
								int metaState = 0;
								if (mKeyboardView.isShifted()) {
									metaState |= KeyEvent.META_SHIFT_ON|KeyEvent.META_SHIFT_LEFT_ON;
									primaryCode = Character.toUpperCase(primaryCode);
								}
								if (mCurrentKeyboard.getAltKeyIndicator()) {
									metaState |= KeyEvent.META_ALT_ON|KeyEvent.META_ALT_LEFT_ON;
								}
								if (mCurrentKeyboard.getCtrlKeyIndicator()) {
									// metaState |= KeyEvent.META_CTRL_ON|KeyEvent.META_CTRL_LEFT_ON;
									metaState |= 0x00001000|0x00002000;
								}
								keyEvent = new KeyEvent(0, 0, KeyEvent.ACTION_DOWN, keyCode, 0, metaState);
								if (connection != null) {
									connection.sendKeyEvent(keyEvent);
								}
							}
						}
					}
				} else {
					if (primaryCode >= 0) {
						if (mKeyboardView.isShifted()) {
							primaryCode = Character.toUpperCase(primaryCode);
						}
						mWnn.onEvent(new NicoWnnGEvent(NicoWnnGEvent.INPUT_CHAR, (char)primaryCode));
					}
				}
				break;
		}

		if (changeShiftLock) {
			if (mShiftLockCount == 1) {
				if (mShiftOn == KEYBOARD_SHIFT_ON) {
					keyEvent = new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_SHIFT_LEFT);
					if (connection != null) {
						connection.sendKeyEvent(keyEvent);
					}
				}
				toggleShiftLock(2);
			}
		}

		boolean invalidate = false;
		if (changeAltLock) {
			if (metaAltStatus != mCurrentKeyboard.getAltKeyIndicator()) {
				invalidate = true;
			}
		}
		if (changeCtrlLock) {
			if (metaCtrlStatus != mCurrentKeyboard.getAltKeyIndicator()) {
				invalidate = true;
			}
		}
		if (invalidate) {
			mKeyboardView.invalidateAllKeys();
		}
		
		/* update shift key's state */
		if (!mCapsLock && (primaryCode != DefaultSoftKeyboard.KEYCODE_QWERTY_SHIFT)) {
			setShiftByEditorInfo();
		}
	}

	public String[] convertFlickToKeyStringNico(int flickdir) {
		final int col = getTableIndex(mPrevInputKeyCode);
		final int row = flickdir;
		final String[][] cycleTable = getCycleTable();

		if (null != cycleTable) {
			return cycleTable[col * mCycleTableColumns + row];
		}
		return null;
	}

	/*****************************************
	 * onkey nicotouch
	 */
	private boolean onKeyNico(final int primaryCode2, final int[] keyCodes) {
		MyHeightKeyboard newKeyboard = null;
		boolean retcode = false;
		int primaryCode = primaryCode2;

		// �������[�v�΍�
		if (mRecursiveOnKeyNico > 4) return true;
		mRecursiveOnKeyNico++;

		final int getFlickCode = checkFlickKeyCode(false, false);
		if (getFlickCode >= 0) {
			// get flick keydata
			final String keyString[] = convertFlickToKeyString(getFlickCode);
			if (null != keyString) {
				mWnn.onEvent(mWnn.mEventTouchOtherKey);
				if (keyString[0].equals("\u309b")) {
					retcode = onKeyNico(KEYCODE_DAKUTEN, keyCodes);
					mRecursiveOnKeyNico--;
					return retcode;
				}
				if (keyString[0].equals("\u309c")) {
					retcode = onKeyNico(KEYCODE_HANDAKUTEN, keyCodes);
					mRecursiveOnKeyNico--;
					return retcode;
				}
				mWnn.onEvent(new NicoWnnGEvent(NicoWnnGEvent.TOGGLE_CHAR, keyString));
			}
			mNicoFirst = false;
			mNicoFlick = false;
			mRecursiveOnKeyNico--;
			return true;
		} else if (getFlickCode < -1) {
			switch (primaryCode) {
			case KEYCODE_JP12_CONVPREDICT:
			case KEYCODE_JP12_CONVPREDICT_BACKWARD:
				break;
			default:
				primaryCode = getFlickCode;
			}
		}

		switch (primaryCode) {
			case DefaultSoftKeyboard.KEYCODE_QWERTY_BACKSPACE:
			case KEYCODE_JP12_BACKSPACE:
				if (mNicoFirst == true) {
					resetNicoKeyboard();
				}
				else {
					mWnn.onEvent(mWnn.mEventInputKeyDel);
				}
				retcode = true;
				break;
			case KEYCODE_QWERTY_ENTER:
			case KEYCODE_QWERTY_MINIENTER:
			case KEYCODE_QWERTY_MINIENTER2:
			case KEYCODE_JP12_ENTER:
				if (mNicoFirst == false) {
					mWnn.onEvent(mWnn.mEventInputEnter);
				}
				retcode = true;
				break;

			case KEYCODE_JP12_REVERSE:
				if (mNicoFirst == true) {
					resetNicoKeyboard();
				} else {
					NicoWnnGJAJP t = NicoWnnGJAJP.getInstance();
					if (t.candidatesViewManagerIsShown() && t.candidatesViewManagerIsIndicated()) {
						mWnn.onEvent(new NicoWnnGEvent(NicoWnnGEvent.INPUT_SOFT_KEY, new KeyEvent(KeyEvent.ACTION_DOWN, KEYCODE_JP12_REVERSE)));
					} else if (t.isRenbun()||t.isPredict()) {
						mWnn.onEvent(new NicoWnnGEvent(NicoWnnGEvent.INPUT_KEY, new KeyEvent(KeyEvent.ACTION_DOWN, KEYCODE_JP12_REVERSE)));
					}
				}
				retcode = true;
				break;

			case KEYCODE_QWERTY_KBD:
			case KEYCODE_JP12_KBD:
				resetNicoKeyboard();
				break;

			case KEYCODE_JP12_EMOJI:
			case KEYCODE_QWERTY_EMOJI:
				if (mNicoFirst == false) {
					commitText();
					mWnn.onEvent(mWnn.mEventChangeModeSymbol);
				}
				retcode = true;
				break;

			case KEYCODE_JP12_SHARP:
				if ((mNicoFirst == false) && mNoInput) {
					commitText();
					mWnn.onEvent(mWnn.mEventChangeModeDocomo);
				}
				retcode = true;
				break;

			case KEYCODE_JP12_1:
			case KEYCODE_JP12_2:
			case KEYCODE_JP12_3:
			case KEYCODE_JP12_4:
			case KEYCODE_JP12_5:
			case KEYCODE_JP12_6:
			case KEYCODE_JP12_7:
			case KEYCODE_JP12_8:
			case KEYCODE_JP12_9:
			case KEYCODE_JP12_0:
			case KEYCODE_NEWKEY_0:
			case KEYCODE_NEWKEY_1:
			case KEYCODE_NEWKEY_2:
			case KEYCODE_NEWKEY_3:
			case KEYCODE_NEWKEY_4:
				if (mNicoFirst == false) {
					mWnn.onEvent(mWnn.mEventTouchOtherKey);
					if (NICOFLICK_NONE == mFlickNicoInput) {
						mPrevInputKeyCode = primaryCode;
					}
					else{
						if (-1 == checkTableIndex(mPrevInputKeyCode)) {
							mPrevInputKeyCode = primaryCode;
						}
					}
					mNicoFirst = true;

					/* change keymap */
					final int index = getTableIndex(mPrevInputKeyCode);
					final MyHeightKeyboard[][] keyList = mKeyboard[LANG_JA][mDisplayMode][KEYBOARD_12KEY][KEYBOARD_SHIFT_2ND];

					// keyList[KEYMODE_JA_FULL_NICO] = mNicoKeyboard[mNicoKeyMode][index+1];
					switch (mCurrentKeyMode) {
						case KEYMODE_JA_FULL_NICO:
						case KEYMODE_JA_FULL_NICO_KATAKANA:
						case KEYMODE_JA_HALF_NICO_KATAKANA:
							keyList[mCurrentKeyMode] = mNicoKeyboard[mNicoKeyMode][index+NICO_SLIDE_MODE_A];
					}
					if (false == mNoFlipScreen) {
						newKeyboard = getShiftChangeKeyboard(KEYBOARD_SHIFT_2ND);
						mShiftOn = KEYBOARD_SHIFT_2ND;
						changeKeyboard(newKeyboard);
					}
				}
				else{
					if (false == mNoFlipScreen) {
						newKeyboard = getShiftChangeKeyboard(KEYBOARD_SHIFT_OFF);
						mShiftOn = KEYBOARD_SHIFT_OFF;
						changeKeyboard(newKeyboard);
					}
					final int col = getTableIndex(mPrevInputKeyCode);
					final int row = getTableIndex(primaryCode);
					final String[][] cycleTable = getCycleTable();
					if (cycleTable == null) {
						Log.e("NicoWnnG", "not founds cycle table");
					}
					else{
						if (cycleTable[col * mCycleTableColumns + row][0].equals("\u309b")) {
							retcode = onKeyNico(KEYCODE_DAKUTEN, keyCodes);
							mRecursiveOnKeyNico--;
							return retcode;
						}
						if (cycleTable[col * mCycleTableColumns + row][0].equals("\u309c")) {
							retcode = onKeyNico(KEYCODE_HANDAKUTEN, keyCodes);
							mRecursiveOnKeyNico--;
							return retcode;
						}
						mWnn.onEvent(new NicoWnnGEvent(NicoWnnGEvent.TOGGLE_CHAR, cycleTable[col * mCycleTableColumns + row]));
					}
					mNicoFirst = false;
					mNicoFlick = false;
				}
				if (mCurrentKeyMode == KEYMODE_JA_HALF_ALPHABET) {
					if (mAutoForwardToggle12key) {
						mWnn.setAutoForwardToggle12key();
					}
				}
				retcode = true;
				break;

			case KEYCODE_JP12_ASTER:
				if (mNicoFirst == true) {
					resetNicoKeyboard();
				} else{
					if (!mNoInput || mWnn.isPasswordInputMode()) {
						/* Processing to toggle Dakuten, Handakuten, and capital */
						final HashMap<String,String> replaceTable = getReplaceTable(mTsuMode);
						if (replaceTable == null) {
							Log.e("NicoWnnG", "not founds replace table");
						} else {
							mWnn.onEvent(new NicoWnnGEvent(NicoWnnGEvent.REPLACE_CHAR, replaceTable));
							mPrevInputKeyCode = primaryCode;
						}
					} else {
						mWnn.onEvent(mWnn.mEventInputEnter);
					}
				}
				retcode = true;
				break;

			case KEYCODE_DAKUTEN:
				if (mInputType == INPUT_TYPE_INSTANT) {
					commitText();
					mWnn.onEvent(new NicoWnnGEvent(NicoWnnGEvent.INPUT_CHAR, mCurrentInstantTable[getTableIndex(primaryCode)]));
				} else {
					char c = 0x309b;
					switch (mCurrentKeyMode) {
						case KEYMODE_JA_HALF_ALPHABET:
						case KEYMODE_JA_HALF_NUMBER:
						case KEYMODE_JA_HALF_PHONE:
						case KEYMODE_JA_HALF_KATAKANA:
						case KEYMODE_JA_HALF_NICO_KATAKANA:
							c = 0xff9e;
							break;
					}
					if (!mNoInput) {
						/* Processing to toggle Dakuten, Handakuten, and capital */
						final HashMap replaceTable = getReplaceDakutenTable();
						if (replaceTable == null) {
							Log.e("NicoWnnG", "not founds replace table");
						} else {
							final boolean f = mWnn.onEvent(new NicoWnnGEvent(NicoWnnGEvent.REPLACE_CHAR, replaceTable));
							if (!f) {
								mWnn.onEvent(new NicoWnnGEvent(NicoWnnGEvent.INPUT_CHAR, c));
							}
						}
					} else {
						mWnn.onEvent(new NicoWnnGEvent(NicoWnnGEvent.INPUT_CHAR, c));
					}
					mWnn.onEvent(mWnn.mEventResetStatus);
					if (false == mNoFlipScreen) {
						newKeyboard = getShiftChangeKeyboard(KEYBOARD_SHIFT_OFF);
						mShiftOn = KEYBOARD_SHIFT_OFF;
						changeKeyboard(newKeyboard);
					}
					mPrevInputKeyCode = 0;
					mNicoFirst = false;
					mNicoFlick = false;
				}
				retcode = true;
				break;

			case KEYCODE_HANDAKUTEN:
				if (mInputType == INPUT_TYPE_INSTANT) {
					commitText();
					mWnn.onEvent(new NicoWnnGEvent(NicoWnnGEvent.INPUT_CHAR, mCurrentInstantTable[getTableIndex(primaryCode)]));
				} else {
					char c = 0x309c;
					switch (mCurrentKeyMode) {
						case KEYMODE_JA_HALF_ALPHABET:
						case KEYMODE_JA_HALF_NUMBER:
						case KEYMODE_JA_HALF_PHONE:
						case KEYMODE_JA_HALF_KATAKANA:
						case KEYMODE_JA_HALF_NICO_KATAKANA:
							c = 0xff9f;
							break;
					}
					if (!mNoInput) {
						/* Processing to toggle Dakuten, Handakuten, and capital */
						final HashMap replaceTable = getReplaceHandakutenTable();
						if (replaceTable == null) {
							Log.e("NicoWnnG", "not founds replace table");
						} else {
							final boolean f = mWnn.onEvent(new NicoWnnGEvent(NicoWnnGEvent.REPLACE_CHAR, replaceTable));
							if (!f) {
								mWnn.onEvent(new NicoWnnGEvent(NicoWnnGEvent.INPUT_CHAR, c));
							}
						}
					} else {
						mWnn.onEvent(new NicoWnnGEvent(NicoWnnGEvent.INPUT_CHAR, c));
					}
					mWnn.onEvent(mWnn.mEventResetStatus);
					if (false == mNoFlipScreen) {
						newKeyboard = getShiftChangeKeyboard(KEYBOARD_SHIFT_OFF);
						mShiftOn = KEYBOARD_SHIFT_OFF;
						changeKeyboard(newKeyboard);
					}
					mPrevInputKeyCode = 0;
					mNicoFirst = false;
					mNicoFlick = false;
				}
				retcode = true;
				break;

			case KEYCODE_KANASMALL:
				if (mInputType == INPUT_TYPE_INSTANT) {
					//
				} else {
					if (!mNoInput) {
						/* Processing to toggle Dakuten, Handakuten, and capital */
						final HashMap replaceTable = getReplaceKanaSmallTable();
						if (replaceTable == null) {
							Log.e("NicoWnnG", "not founds replace table");
						} else {
							final boolean f = mWnn.onEvent(new NicoWnnGEvent(NicoWnnGEvent.REPLACE_CHAR, replaceTable));
							if (f) {
								mPrevInputKeyCode = primaryCode;
							}
						}
					}
				}
				break;

			case KEYCODE_JP12_SPACE:
				if (mNicoFirst == false) {
					NicoWnnGJAJP t = NicoWnnGJAJP.getInstance();
					if (t.isRenbun()||t.isPredict()) {
						mWnn.onEvent(new NicoWnnGEvent(NicoWnnGEvent.INPUT_KEY, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SPACE)));
						mWnn.onEvent(new NicoWnnGEvent(NicoWnnGEvent.INPUT_CHAR, ' '));
					} else if ((mCurrentKeyMode == KEYMODE_JA_FULL_HIRAGANA) && !mNoInput) {
						mWnn.onEvent(mWnn.mEventConvert);
					} else if ((mCurrentKeyMode == KEYMODE_JA_FULL_NICO) && !mNoInput) {
						mWnn.onEvent(mWnn.mEventConvert);
					} else if ((mCurrentKeyMode == KEYMODE_JA_FULL_NICO_KATAKANA) && !mNoInput) {
						mWnn.onEvent(mWnn.mEventConvert);
					} else if (t.candidatesViewManagerIsShown()) {
						mWnn.onEvent(new NicoWnnGEvent(NicoWnnGEvent.INPUT_SOFT_KEY, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SPACE)));
					} else if (mNoInput) {
						switch (mCurrentKeyMode) {
							case KEYMODE_JA_FULL_HIRAGANA:
							case KEYMODE_JA_FULL_KATAKANA:
							case KEYMODE_JA_FULL_ALPHABET:
							case KEYMODE_JA_FULL_NUMBER:
							case KEYMODE_JA_FULL_NICO:
							case KEYMODE_JA_FULL_NICO_KATAKANA:
								switch (primaryCode) {
								  case KEYCODE_JP12_SPACE:
								  	if (mKana12SpaceZen) {
										mWnn.onEvent(new NicoWnnGEvent(NicoWnnGEvent.INPUT_CHAR, '\u3000'));
									} else {
										mWnn.onEvent(new NicoWnnGEvent(NicoWnnGEvent.INPUT_CHAR, ' '));
									}
									break;
								  case KEYCODE_JP12_SPACE_SHIFT:
								  	if (mKana12SpaceZen) {
										mWnn.onEvent(new NicoWnnGEvent(NicoWnnGEvent.INPUT_CHAR, ' '));
									} else {
										mWnn.onEvent(new NicoWnnGEvent(NicoWnnGEvent.INPUT_CHAR, '\u3000'));
									}
									break;
								}
								break;
							default:
								mWnn.onEvent(new NicoWnnGEvent(NicoWnnGEvent.INPUT_CHAR, ' '));
								break;
						}
					} else {
						mWnn.onEvent(new NicoWnnGEvent(NicoWnnGEvent.INPUT_CHAR, ' '));
					}
				}
				retcode = true;
				break;

			case KEYCODE_QWERTY_ZEN_SPACE:
			case KEYCODE_QWERTY_ZEN_SPACE2:
			case KEYCODE_QWERTY_ZEN_SPACE_SHIFT:
			case KEYCODE_QWERTY_ZEN_SPACE2_SHIFT:
				if (mNicoFirst == false) {
					NicoWnnGJAJP t = NicoWnnGJAJP.getInstance();
					if (t.candidatesViewManagerIsShown() && t.candidatesViewManagerIsIndicated()) {
						mWnn.onEvent(new NicoWnnGEvent(NicoWnnGEvent.INPUT_SOFT_KEY, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SPACE)));
					} else if (t.isRenbun()||t.isPredict()) {
						mWnn.onEvent(new NicoWnnGEvent(NicoWnnGEvent.INPUT_KEY, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SPACE)));
					} else if ((mCurrentKeyMode == KEYMODE_JA_FULL_HIRAGANA) && !mNoInput) {
						mWnn.onEvent(mWnn.mEventConvert);
					} else if (t.candidatesViewManagerIsShown()) {
						mWnn.onEvent(new NicoWnnGEvent(NicoWnnGEvent.INPUT_SOFT_KEY, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SPACE)));
					} else {
						switch (primaryCode) {
						  case KEYCODE_QWERTY_ZEN_SPACE:
						  case KEYCODE_QWERTY_ZEN_SPACE2:
							if (mQwertySpaceZen) {
								mWnn.onEvent(new NicoWnnGEvent(NicoWnnGEvent.INPUT_CHAR, '\u3000'));
							} else {
								mWnn.onEvent(new NicoWnnGEvent(NicoWnnGEvent.INPUT_CHAR, ' '));
							}
							break;
						  case KEYCODE_QWERTY_ZEN_SPACE_SHIFT:
						  case KEYCODE_QWERTY_ZEN_SPACE2_SHIFT:
							if (mQwertySpaceZen) {
								mWnn.onEvent(new NicoWnnGEvent(NicoWnnGEvent.INPUT_CHAR, ' '));
							} else {
								mWnn.onEvent(new NicoWnnGEvent(NicoWnnGEvent.INPUT_CHAR, '\u3000'));
							}
							break;
						}
					}
				}
				retcode = true;
				break;

			case KEYCODE_EISU_KANA:
				if (mNicoFirst == false) {
					mWnn.onEvent(mWnn.mEventChangeModeEisuKana);
				}
				retcode = true;
				break;

			case KEYCODE_JP12_CLOSE:
				mWnn.onEvent(mWnn.mEventInputBack);
				retcode = true;
				break;

			case KEYCODE_JP12_LEFT:
				if (mNicoFirst == false) {
					if (!isEndOfArrowKeyRepeat()) {
						mWnn.onEvent(mWnn.mEventInputDpadLeft);
					}
				}
				retcode = true;
				break;
			case KEYCODE_JP12_RIGHT:
				if (mNicoFirst == false) {
					if (!isEndOfArrowKeyRepeat()) {
						mWnn.onEvent(mWnn.mEventInputDpadRight);
					}
				}
				retcode = true;
				break;

			case KEYCODE_JP12_UP:
				if (mNicoFirst == false) {
					if (!isEndOfArrowKeyRepeat()) {
						mWnn.onEvent(mWnn.mEventInputDpadUp);
					}
				}
				retcode = true;
				break;

			case KEYCODE_JP12_DOWN:
				if (mNicoFirst == false) {
					if (!isEndOfArrowKeyRepeat()) {
						mWnn.onEvent(mWnn.mEventInputDpadDown);
					}
				}
				retcode = true;
				break;

			case KEYCODE_ARROW_STOP:
				retcode = true;
				break;

		}
		mRecursiveOnKeyNico--;
		return retcode;
	} // onKeyNico

	/**
	 */
	@Override public int convertModeFlick(final int prev, final int key) {
		int code = -1;
		switch (mCurrentKeyMode) {
			case KEYMODE_JA_FULL_NICO:
			case KEYMODE_JA_FULL_NICO_KATAKANA:
			case KEYMODE_JA_HALF_NICO_KATAKANA:
			case KEYMODE_JA_HALF_ALPHABET:
			case KEYMODE_JA_FULL_ALPHABET:
				code = mSetupKeyboard.GetFlickChangeMap(mCurrentKeyMode, prev, key);
		}
		return code;
	}



	/**
	 * change map
	 */
	/* */
	SetupKeyboard              mSetupKeyboard;
	String[][][][]             mCycleTable;
	int                        mCycleTableColumns;
	// HashMap<String, String>    mReplaceTable;


	private static final int selectOtherLandKeyTable[] = {
		R.xml.keyboard_12key_phone_0,
		R.xml.keyboard_12key_full_alphabet_0,
		R.xml.keyboard_12key_full_alphabet_input_0,
		R.xml.keyboard_12key_full_num_0,
		R.xml.keyboard_12key_half_alphabet_0,
		R.xml.keyboard_12key_half_alphabet_input_0,
		R.xml.keyboard_12key_half_num_0,

		R.xml.keyboard_12key_phone_0,
		R.xml.keyboard_12key_full_alphabet_shift_0,
		R.xml.keyboard_12key_full_alphabet_input_shift_0,
		R.xml.keyboard_12key_full_num_shift_0,
		R.xml.keyboard_12key_half_alphabet_shift_0,
		R.xml.keyboard_12key_half_alphabet_input_shift_0,
		R.xml.keyboard_12key_half_num_shift_0,
	};
	private static final int selectOtherPortKeyTable[] = {
		R.xml.keyboard_12key_phone_0,
		R.xml.keyboard_12key_full_alphabet_0,
		R.xml.keyboard_12key_full_alphabet_input_0,
		R.xml.keyboard_12key_full_num_0,
		R.xml.keyboard_12key_half_alphabet_0,
		R.xml.keyboard_12key_half_alphabet_input_0,
		R.xml.keyboard_12key_half_num_0,

		R.xml.keyboard_12key_phone_0,
		R.xml.keyboard_12key_full_alphabet_shift_0,
		R.xml.keyboard_12key_full_alphabet_input_shift_0,
		R.xml.keyboard_12key_full_num_shift_0,
		R.xml.keyboard_12key_half_alphabet_shift_0,
		R.xml.keyboard_12key_half_alphabet_input_shift_0,
		R.xml.keyboard_12key_half_num_shift_0,
	};

	private static final int selectFlickLandKeyTable[] = {
		R.xml.keyboard_12key_phone_0,
		R.xml.keyboard_12key_flick_full_alphabet_0,
		R.xml.keyboard_12key_flick_full_alphabet_input_0,
		R.xml.keyboard_12key_full_num_0,
		R.xml.keyboard_12key_flick_half_alphabet_0,
		R.xml.keyboard_12key_flick_half_alphabet_input_0,
		R.xml.keyboard_12key_half_num_0,

		R.xml.keyboard_12key_phone_0,
		R.xml.keyboard_12key_full_alphabet_shift_0,
		R.xml.keyboard_12key_full_alphabet_input_shift_0,
		R.xml.keyboard_12key_full_num_shift_0,
		R.xml.keyboard_12key_half_alphabet_shift_0,
		R.xml.keyboard_12key_half_alphabet_input_shift_0,
		R.xml.keyboard_12key_half_num_shift_0,
	};
	private static final int selectFlickPortKeyTable[] = {
		R.xml.keyboard_12key_phone_0,
		R.xml.keyboard_12key_flick_full_alphabet_0,
		R.xml.keyboard_12key_flick_full_alphabet_input_0,
		R.xml.keyboard_12key_full_num_0,
		R.xml.keyboard_12key_flick_half_alphabet_0,
		R.xml.keyboard_12key_flick_half_alphabet_input_0,
		R.xml.keyboard_12key_half_num_0,

		R.xml.keyboard_12key_phone_0,
		R.xml.keyboard_12key_full_alphabet_shift_0,
		R.xml.keyboard_12key_full_alphabet_input_shift_0,
		R.xml.keyboard_12key_full_num_shift_0,
		R.xml.keyboard_12key_half_alphabet_shift_0,
		R.xml.keyboard_12key_half_alphabet_input_shift_0,
		R.xml.keyboard_12key_half_num_shift_0,
	};

	/** */

	private static final int selectSubTenLandKeyTable[] = {
		R.xml.key_subten_qwerty_phone_0,
		R.xml.key_subten_qwerty_full_alphabet_0,
		R.xml.key_subten_qwerty_full_alphabet_input_0,
		R.xml.key_subten_qwerty_full_num_0,
		R.xml.key_subten_qwerty_half_alphabet_0,
		R.xml.key_subten_qwerty_half_alphabet_input_0,
		R.xml.key_subten_qwerty_half_num_0,

		R.xml.key_subten_qwerty_phone_0,
		R.xml.key_subten_qwerty_full_alphabet_0,
		R.xml.key_subten_qwerty_full_alphabet_input_0,
		R.xml.key_subten_qwerty_full_num_0,
		R.xml.key_subten_qwerty_half_alphabet_0,
		R.xml.key_subten_qwerty_half_alphabet_input_0,
		R.xml.key_subten_qwerty_half_num_0,
	};
	private static final int selectSubTenPortKeyTable[] = {
		R.xml.key_subten_qwerty_phone_0,
		R.xml.key_subten_qwerty_full_alphabet_0,
		R.xml.key_subten_qwerty_full_alphabet_input_0,
		R.xml.key_subten_qwerty_full_num_0,
		R.xml.key_subten_qwerty_half_alphabet_0,
		R.xml.key_subten_qwerty_half_alphabet_input_0,
		R.xml.key_subten_qwerty_half_num_0,

		R.xml.key_subten_qwerty_phone_0,
		R.xml.key_subten_qwerty_full_alphabet_0,
		R.xml.key_subten_qwerty_full_alphabet_input_0,
		R.xml.key_subten_qwerty_full_num_0,
		R.xml.key_subten_qwerty_half_alphabet_0,
		R.xml.key_subten_qwerty_half_alphabet_input_0,
		R.xml.key_subten_qwerty_half_num_0,
	};

	private static final int selectSubTenLandKeyTable2[] = {
		R.xml.key_subten_12key2_phone_0,
		R.xml.key_subten_12key2_full_alphabet_0,
		R.xml.key_subten_12key2_full_alphabet_input_0,
		R.xml.key_subten_12key2_full_num_0,
		R.xml.key_subten_12key2_half_alphabet_0,
		R.xml.key_subten_12key2_half_alphabet_input_0,
		R.xml.key_subten_12key2_half_num_0,

		R.xml.key_subten_12key2_phone_0,
		R.xml.key_subten_12key2_full_alphabet_shift_0,
		R.xml.key_subten_12key2_full_alphabet_input_0,
		R.xml.key_subten_12key2_full_num_shift_0,
		R.xml.key_subten_12key2_half_alphabet_shift_0,
		R.xml.key_subten_12key2_half_alphabet_input_0,
		R.xml.key_subten_12key2_half_num_shift_0,
	};
	private static final int selectSubTenPortKeyTable2[] = {
		R.xml.key_subten_12key2_phone_0,
		R.xml.key_subten_12key2_full_alphabet_0,
		R.xml.key_subten_12key2_full_alphabet_input_0,
		R.xml.key_subten_12key2_full_num_0,
		R.xml.key_subten_12key2_half_alphabet_0,
		R.xml.key_subten_12key2_half_alphabet_input_0,
		R.xml.key_subten_12key2_half_num_0,

		R.xml.key_subten_12key2_phone_0,
		R.xml.key_subten_12key2_full_alphabet_shift_0,
		R.xml.key_subten_12key2_full_alphabet_input_0,
		R.xml.key_subten_12key2_full_num_shift_0,
		R.xml.key_subten_12key2_half_alphabet_shift_0,
		R.xml.key_subten_12key2_half_alphabet_input_0,
		R.xml.key_subten_12key2_half_num_shift_0,
	};

	private static final int selectSubTenLandKeyTable3[] = {
		R.xml.key_subten_12key3_phone_0,
		R.xml.key_subten_12key3_full_alphabet_0,
		R.xml.key_subten_12key3_full_alphabet_input_0,
		R.xml.key_subten_12key3_full_num_0,
		R.xml.key_subten_12key3_half_alphabet_0,
		R.xml.key_subten_12key3_half_alphabet_input_0,
		R.xml.key_subten_12key3_half_num_0,

		R.xml.key_subten_12key3_phone_0,
		R.xml.key_subten_12key3_full_alphabet_shift_0,
		R.xml.key_subten_12key3_full_alphabet_input_0,
		R.xml.key_subten_12key3_full_num_shift_0,
		R.xml.key_subten_12key3_half_alphabet_shift_0,
		R.xml.key_subten_12key3_half_alphabet_input_0,
		R.xml.key_subten_12key3_half_num_shift_0,
	};
	private static final int selectSubTenPortKeyTable3[] = {
		R.xml.key_subten_12key3_phone_0,
		R.xml.key_subten_12key3_full_alphabet_0,
		R.xml.key_subten_12key3_full_alphabet_input_0,
		R.xml.key_subten_12key3_full_num_0,
		R.xml.key_subten_12key3_half_alphabet_0,
		R.xml.key_subten_12key3_half_alphabet_input_0,
		R.xml.key_subten_12key3_half_num_0,

		R.xml.key_subten_12key3_phone_0,
		R.xml.key_subten_12key3_full_alphabet_shift_0,
		R.xml.key_subten_12key3_full_alphabet_input_0,
		R.xml.key_subten_12key3_full_num_shift_0,
		R.xml.key_subten_12key3_half_alphabet_shift_0,
		R.xml.key_subten_12key3_half_alphabet_input_0,
		R.xml.key_subten_12key3_half_num_shift_0,
	};

}
