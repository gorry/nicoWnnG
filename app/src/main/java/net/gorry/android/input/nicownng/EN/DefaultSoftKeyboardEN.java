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
 */

package net.gorry.android.input.nicownng.EN;

import net.gorry.android.input.nicownng.DefaultSoftKeyboard;
import net.gorry.android.input.nicownng.MyHeightKeyboard;
import net.gorry.android.input.nicownng.NicoWnnG;
import net.gorry.android.input.nicownng.NicoWnnGEvent;
import net.gorry.android.input.nicownng.R;
import android.content.SharedPreferences;
import android.inputmethodservice.Keyboard;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

/**
 * The default Software Keyboard class for English IME.
 *
 * @author Copyright (C) 2009 OMRON SOFTWARE CO., LTD.  All Rights Reserved.
 */
public class DefaultSoftKeyboardEN extends DefaultSoftKeyboard {
	/** 12-key keyboard [PHONE MODE] */
	public static final int KEYCODE_PHONE  = -116;

	/**
	 * Keyboards toggled by ALT key.
	 * <br>
	 * The normal keyboard(KEYMODE_EN_ALPHABET) and the number/symbol
	 * keyboard(KEYMODE_EN_NUMBER) is active.  The phone number
	 * keyboard(KEYMODE_EN_PHONE) is disabled.
	 */
	private static final boolean[] TOGGLE_KEYBOARD = {true, true, false};

	/** Auto caps mode */
	private boolean mAutoCaps = false;

//	private NicoWnnG mNicoWnnG;

	/**
	 * Default constructor
	 */
	public DefaultSoftKeyboardEN(final NicoWnnG parent) {
		super(parent);
	}

	/**
	 * Dismiss the pop-up keyboard.
	 * <br>
	 * Nothing will be done if no pop-up keyboard is displaying.
	 */
	public void dismissPopupKeyboard() {
		try {
			if (mKeyboardView != null) {
				mKeyboardView.handleBack();
			}
		} catch (final Exception ex) {
			/* ignore */
		}
	}

	/** @see net.gorry.android.input.nicownng.DefaultSoftKeyboard#createKeyboards */
	@Override protected void createKeyboards() {
		mKeyboard = new MyHeightKeyboard[LANG_MAX][PORTRAIT_LANDSCAPE][KEYBOARD_MAX][KEYBOARD_SHIFT_MAX][KEYMODE_JA_MAX][2];

		MyHeightKeyboard[][] keyList;
		/***********************************************************************
		 * English
		 ***********************************************************************/
		if (mDisplayMode == DefaultSoftKeyboard.PORTRAIT) {
			int[] keyTable = selectOtherPortKeyTable;
			final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mWnn);
			mQwertyMatrixMode      = mWnn.getOrientPrefBoolean(pref, "qwerty_matrix_mode", false);
			if (mQwertyMatrixMode) {
				keyTable = selectOtherPortKeyTable;
			} else {
				keyTable = selectOtherSlantPortKeyTable;
			}

			/* qwerty shift_off */
			keyList = mKeyboard[LANG_EN][PORTRAIT][KEYBOARD_QWERTY][KEYBOARD_SHIFT_OFF];
			keyList[KEYMODE_EN_ALPHABET][0] = new MyHeightKeyboard(mWnn, keyTable[0], mInputViewHeightIndex, KEYTYPE_QWERTY, true);
			keyList[KEYMODE_EN_NUMBER][0]   = new MyHeightKeyboard(mWnn, keyTable[1], mInputViewHeightIndex, KEYTYPE_QWERTY, true);
			keyList[KEYMODE_EN_PHONE][0]    = new MyHeightKeyboard(mWnn, keyTable[2], mInputViewHeightIndex, KEYTYPE_12KEY, true);

			/* qwerty shift_on */
			keyList = mKeyboard[LANG_EN][PORTRAIT][KEYBOARD_QWERTY][KEYBOARD_SHIFT_ON];
			keyList[KEYMODE_EN_ALPHABET][0] = mKeyboard[LANG_EN][PORTRAIT][KEYBOARD_QWERTY][KEYBOARD_SHIFT_OFF][KEYMODE_EN_ALPHABET][0];
			keyList[KEYMODE_EN_NUMBER][0]   = new MyHeightKeyboard(mWnn, keyTable[3], mInputViewHeightIndex, KEYTYPE_QWERTY, true);
			keyList[KEYMODE_EN_PHONE][0]    = mKeyboard[LANG_EN][PORTRAIT][KEYBOARD_QWERTY][KEYBOARD_SHIFT_OFF][KEYMODE_EN_PHONE][0];
		} else {
			int[] keyTable = selectOtherLandKeyTable;
			if (!mQwertyMatrixMode) {
				keyTable = selectOtherLandKeyTable;
			} else {
				keyTable = selectOtherSlantLandKeyTable;
			}

			/* qwerty shift_off */
			keyList = mKeyboard[LANG_EN][LANDSCAPE][KEYBOARD_QWERTY][KEYBOARD_SHIFT_OFF];
			keyList[KEYMODE_EN_ALPHABET][0] = new MyHeightKeyboard(mWnn, keyTable[0], mInputViewHeightIndex, KEYTYPE_QWERTY, false);
			keyList[KEYMODE_EN_NUMBER][0]   = new MyHeightKeyboard(mWnn, keyTable[1], mInputViewHeightIndex, KEYTYPE_QWERTY, false);
			keyList[KEYMODE_EN_PHONE][0]    = new MyHeightKeyboard(mWnn, keyTable[2], mInputViewHeightIndex, KEYTYPE_12KEY, false);

			/* qwerty shift_on */
			keyList = mKeyboard[LANG_EN][LANDSCAPE][KEYBOARD_QWERTY][KEYBOARD_SHIFT_ON];
			keyList[KEYMODE_EN_ALPHABET][0] = mKeyboard[LANG_EN][LANDSCAPE][KEYBOARD_QWERTY][KEYBOARD_SHIFT_OFF][KEYMODE_EN_ALPHABET][0];
			keyList[KEYMODE_EN_NUMBER][0]   = new MyHeightKeyboard(mWnn, keyTable[3], mInputViewHeightIndex, KEYTYPE_QWERTY, false);
			keyList[KEYMODE_EN_PHONE][0]    = mKeyboard[LANG_EN][LANDSCAPE][KEYBOARD_QWERTY][KEYBOARD_SHIFT_OFF][KEYMODE_EN_PHONE][0];
		}
	}

	/**
	 * Switch the keymode
	 *
	 * @param keyMode		Keymode
	 */
	@Override
	public void changeKeyMode(final int keyMode) {
		final MyHeightKeyboard keyboard = super.getModeChangeKeyboard(keyMode);
		if (keyboard != null) {
			mCurrentKeyMode = keyMode;
			super.changeKeyboard(keyboard);
		}
	}

	/***********************************************************************
	 * from DefaultSoftKeyboard
	 ***********************************************************************/
	/** @see net.gorry.android.input.nicownng.DefaultSoftKeyboard#initView */
	@Override public View initView(final NicoWnnG parent, final int width, final int height) {
		final View view = super.initView(parent, width, height);

		/* default setting */
		mCurrentLanguage     = LANG_EN;
		mCurrentKeyboardType = KEYBOARD_QWERTY;
		mShiftOn             = KEYBOARD_SHIFT_OFF;
		mCurrentKeyMode      = KEYMODE_EN_ALPHABET;

		final MyHeightKeyboard kbd = mKeyboard[mCurrentLanguage][mDisplayMode][mCurrentKeyboardType][mShiftOn][mCurrentKeyMode][0];
		if (kbd == null) {
			if(mDisplayMode == LANDSCAPE){
				return view;
			}
			return null;
		}
		mCurrentKeyboard = null;
		changeKeyboard(kbd);
		return view;
	}

	/** @see net.gorry.android.input.nicownng.DefaultSoftKeyboard#setPreferences */
	@Override public void setPreferences(final SharedPreferences pref, final EditorInfo editor) {
		super.setPreferences(pref, editor);

		/* auto caps mode */
		mAutoCaps = mWnn.getOrientPrefBoolean(pref, "auto_caps", false);

		switch (editor.inputType & InputType.TYPE_MASK_CLASS) {
			case InputType.TYPE_CLASS_NUMBER:
			case InputType.TYPE_CLASS_DATETIME:
				mCurrentLanguage     = LANG_EN;
				mCurrentKeyboardType = KEYBOARD_QWERTY;
				mShiftOn             = KEYBOARD_SHIFT_OFF;
				mCurrentKeyMode      = KEYMODE_EN_NUMBER;

				final MyHeightKeyboard kbdn =
					mKeyboard[mCurrentLanguage][mDisplayMode][mCurrentKeyboardType][mShiftOn][mCurrentKeyMode][0];

				changeKeyboard(kbdn);
				break;

			case InputType.TYPE_CLASS_PHONE:
				mCurrentLanguage     = LANG_EN;
				mCurrentKeyboardType = KEYBOARD_QWERTY;
				mShiftOn             = KEYBOARD_SHIFT_OFF;
				mCurrentKeyMode      = KEYMODE_EN_PHONE;

				final MyHeightKeyboard kbdp =
					mKeyboard[mCurrentLanguage][mDisplayMode][mCurrentKeyboardType][mShiftOn][mCurrentKeyMode][0];

				changeKeyboard(kbdp);

				break;

			default:
				mCurrentLanguage     = LANG_EN;
				mCurrentKeyboardType = KEYBOARD_QWERTY;
				mShiftOn             = KEYBOARD_SHIFT_OFF;
				mCurrentKeyMode      = KEYMODE_EN_ALPHABET;

				final MyHeightKeyboard kbdq =
					mKeyboard[mCurrentLanguage][mDisplayMode][mCurrentKeyboardType][mShiftOn][mCurrentKeyMode][0];

				changeKeyboard(kbdq);
				break;
		}

		final int shift = (mAutoCaps)? getShiftKeyState(mWnn.getCurrentInputEditorInfo()) : 0;
		if (shift != mShiftOn) {
			final MyHeightKeyboard kbd = getShiftChangeKeyboard(shift);
			mShiftOn = shift;
			changeKeyboard(kbd);
		}
	}

	/** @see net.gorry.android.input.nicownng.DefaultSoftKeyboard#onKey */
	@Override public void onKey(int primaryCode, final int[] keyCodes) {
		boolean changeShiftLock = true;
		boolean changeAltLock = true;
		boolean changeCtrlLock = true;
		boolean metaAltStatus = mCurrentKeyboard.getAltKeyIndicator();
		boolean metaCtrlStatus = mCurrentKeyboard.getCtrlKeyIndicator();
		final InputConnection connection = mWnn.getCurrentInputConnection();
		KeyEvent keyEvent = null;

		switch (primaryCode) {
			case KEYCODE_QWERTY_HAN_ALPHA:
				changeKeyMode(KEYMODE_EN_ALPHABET);
				break;

			case KEYCODE_QWERTY_HAN_NUM:
				changeKeyMode(KEYMODE_EN_NUMBER);
				break;

			case KEYCODE_PHONE:
				changeKeyMode(KEYMODE_EN_PHONE);
				break;

			case KEYCODE_QWERTY_EMOJI:
				mWnn.onEvent(new NicoWnnGEvent(NicoWnnGEvent.LIST_SYMBOLS));
				break;

			case KEYCODE_QWERTY_TOGGLE_MODE:
			case KEYCODE_QWERTY_TOGGLE_MODE2:
				switch(mCurrentKeyMode){
					case KEYMODE_EN_ALPHABET:
						if (TOGGLE_KEYBOARD[KEYMODE_EN_NUMBER]){
							mCurrentKeyMode = KEYMODE_EN_NUMBER;
						} else if (TOGGLE_KEYBOARD[KEYMODE_EN_PHONE]) {
							mCurrentKeyMode = KEYMODE_EN_PHONE;
						}
						break;
					case KEYMODE_EN_NUMBER:
						if (TOGGLE_KEYBOARD[KEYMODE_EN_PHONE]) {
							mCurrentKeyMode = KEYMODE_EN_PHONE;
						} else if(TOGGLE_KEYBOARD[KEYMODE_EN_ALPHABET]) {
							mCurrentKeyMode = KEYMODE_EN_ALPHABET;
						}
						break;
					case KEYMODE_EN_PHONE:
						if (TOGGLE_KEYBOARD[KEYMODE_EN_ALPHABET]) {
							mCurrentKeyMode = KEYMODE_EN_ALPHABET;
						} else if (TOGGLE_KEYBOARD[KEYMODE_EN_NUMBER]) {
							mCurrentKeyMode = KEYMODE_EN_NUMBER;
						}
						break;
				}
				final MyHeightKeyboard kbdp =
					mKeyboard[mCurrentLanguage][mDisplayMode][mCurrentKeyboardType][mShiftOn][mCurrentKeyMode][0];
				super.changeKeyboard(kbdp);
				break;

			case DefaultSoftKeyboard.KEYCODE_QWERTY_BACKSPACE:
			case DefaultSoftKeyboard.KEYCODE_JP12_BACKSPACE:
				mWnn.onEvent(new NicoWnnGEvent(NicoWnnGEvent.INPUT_SOFT_KEY,
						new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL)));
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
					keyEvent = new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ALT_LEFT);
				}
				if (connection != null) {
					connection.sendKeyEvent(keyEvent);
				}
				break;

			case KEYCODE_QWERTY_ENTER:
			case KEYCODE_JP12_ENTER:
				mWnn.onEvent(new NicoWnnGEvent(NicoWnnGEvent.INPUT_SOFT_KEY,
						new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER)));
				break;

			case KEYCODE_JP12_LEFT:
				mWnn.onEvent(new NicoWnnGEvent(NicoWnnGEvent.INPUT_SOFT_KEY,
						new KeyEvent(KeyEvent.ACTION_DOWN,
								KeyEvent.KEYCODE_DPAD_LEFT)));
				break;

			case KEYCODE_JP12_RIGHT:
				mWnn.onEvent(new NicoWnnGEvent(NicoWnnGEvent.INPUT_SOFT_KEY,
						new KeyEvent(KeyEvent.ACTION_DOWN,
								KeyEvent.KEYCODE_DPAD_RIGHT)));
			default:
				if ((mCurrentKeyboard.getAltKeyIndicator() && ((mCutPasteActionByIme & CUTPASTEACTIONBYIME_ALT) != 0)) ||
							(mCurrentKeyboard.getCtrlKeyIndicator() && ((mCutPasteActionByIme & CUTPASTEACTIONBYIME_CTRL) != 0))) {
					if (connection != null) {
						switch (primaryCode) {
							case 'a':
							case 'A':
								connection.performContextMenuAction(android.R.id.selectAll);
								break;
							case 'x':
							case 'X':
								connection.performContextMenuAction(android.R.id.cut);
								break;
							case 'c':
							case 'C':
								connection.performContextMenuAction(android.R.id.copy);
								break;
							case 'v':
							case 'V':
								connection.performContextMenuAction(android.R.id.paste);
								break;
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
			if (mShiftOn == KEYBOARD_SHIFT_ON) {
				keyEvent = new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_SHIFT_LEFT);
				if (connection != null) {
					connection.sendKeyEvent(keyEvent);
				}
			}
			toggleShiftLock(2);
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
		if (!mCapsLock && (primaryCode != KEYCODE_QWERTY_SHIFT)) {
			if(mCurrentKeyMode != KEYMODE_EN_NUMBER){
				final int shift = (mAutoCaps)? getShiftKeyState(mWnn.getCurrentInputEditorInfo()) : 0;
				if (shift != mShiftOn) {
					final MyHeightKeyboard kbd = getShiftChangeKeyboard(shift);
					mShiftOn = shift;
					changeKeyboard(kbd);
				}
			}else{
				mShiftOn = KEYBOARD_SHIFT_OFF;
				final MyHeightKeyboard kbd = getShiftChangeKeyboard(mShiftOn);
				changeKeyboard(kbd);
			}
		}
	}



	private static int selectOtherLandKeyTable[] = {
		R.xml.keyboard_qwerty_jp_half_alphabet_0,
		R.xml.keyboard_qwerty_jp_half_symbols_0,
		R.xml.keyboard_12key_phone_0,
		R.xml.keyboard_qwerty_jp_half_symbols_shift_0,
	};

	private static int selectOtherPortKeyTable[] = {
		R.xml.keyboard_qwerty_jp_half_alphabet_0,
		R.xml.keyboard_qwerty_jp_half_symbols_0,
		R.xml.keyboard_12key_phone_0,
		R.xml.keyboard_qwerty_jp_half_symbols_shift_0,
	};

	/** */

	private static int selectOtherSlantLandKeyTable[] = {
		R.xml.keyboard_qwerty_jp_half_alphabet_s0,
		R.xml.keyboard_qwerty_jp_half_symbols_s0,
		R.xml.keyboard_12key_phone_0,
		R.xml.keyboard_qwerty_jp_half_symbols_shift_s0,
	};

	private static int selectOtherSlantPortKeyTable[] = {
		R.xml.keyboard_qwerty_jp_half_alphabet_s0,
		R.xml.keyboard_qwerty_jp_half_symbols_s0,
		R.xml.keyboard_12key_phone_0,
		R.xml.keyboard_qwerty_jp_half_symbols_shift_s0,
	};

}



