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

package net.gorry.android.input.nicownng;

import java.util.List;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.Keyboard.Key;
import android.inputmethodservice.KeyboardView;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.util.FloatMath;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.PopupWindow;
import android.widget.TextView;

import net.gorry.android.input.nicownng.MyKeyboardView;


/**
 * The default software keyboard class.
 *
 * @author Copyright (C) 2009 OMRON SOFTWARE CO., LTD.  All Rights Reserved.
 */
public class DefaultSoftKeyboard implements InputViewManager, KeyboardView.OnKeyboardActionListener, KeyboardView.OnTouchListener, OnGestureListener {
	/*
	 *----------------------------------------------------------------------
	 * key codes for a software keyboard
	 *----------------------------------------------------------------------
	 */
	/** Change the keyboard language */
	public static final int KEYCODE_CHANGE_LANG = -500;

	/* for Japanese 12-key keyboard */
	/** Japanese 12-key keyboard [1] */
	public static final int KEYCODE_JP12_1 = -201;
	/** Japanese 12-key keyboard [2] */
	public static final int KEYCODE_JP12_2 = -202;
	/** Japanese 12-key keyboard [3] */
	public static final int KEYCODE_JP12_3 = -203;
	/** Japanese 12-key keyboard [4] */
	public static final int KEYCODE_JP12_4 = -204;
	/** Japanese 12-key keyboard [5] */
	public static final int KEYCODE_JP12_5 = -205;
	/** Japanese 12-key keyboard [6] */
	public static final int KEYCODE_JP12_6 = -206;
	/** Japanese 12-key keyboard [7] */
	public static final int KEYCODE_JP12_7 = -207;
	/** Japanese 12-key keyboard [8] */
	public static final int KEYCODE_JP12_8 = -208;
	/** Japanese 12-key keyboard [9] */
	public static final int KEYCODE_JP12_9 = -209;
	/** Japanese 12-key keyboard [0] */
	public static final int KEYCODE_JP12_0 = -210;
	/** Japanese 12-key keyboard [#] */
	public static final int KEYCODE_JP12_SHARP = -211;
	/** Japanese 12-key keyboard [*] */
	public static final int KEYCODE_JP12_ASTER = -213;
	/** Japanese 12-key keyboard [DEL] */
	public static final int KEYCODE_JP12_BACKSPACE = -214;
	/** Japanese 12-key keyboard [SPACE] */
	public static final int KEYCODE_JP12_SPACE = -215;
	/** Japanese 12-key keyboard [ENTER] */
	public static final int KEYCODE_JP12_ENTER = -216;
	/** Japanese 12-key keyboard [RIGHT ARROW] */
	public static final int KEYCODE_JP12_RIGHT = -217;
	/** Japanese 12-key keyboard [LEFT ARROW] */
	public static final int KEYCODE_JP12_LEFT = -218;
	/** Japanese 12-key keyboard [REVERSE TOGGLE] */
	public static final int KEYCODE_JP12_REVERSE = -219;
	/** Japanese 12-key keyboard [CLOSE] */
	public static final int KEYCODE_JP12_CLOSE   = -220;
	/** Japanese 12-key keyboard [KEYBOARD TYPE CHANGE] */
	public static final int KEYCODE_JP12_KBD   = -221;
	/** Japanese 12-key keyboard [EMOJI] */
	public static final int KEYCODE_JP12_EMOJI      = -222;
	/** Japanese 12-key keyboard [FULL-WIDTH HIRAGANA MODE] */
	public static final int KEYCODE_JP12_ZEN_HIRA   = -223;
	/** Japanese 12-key keyboard [FULL-WIDTH NUMBER MODE] */
	public static final int KEYCODE_JP12_ZEN_NUM    = -224;
	/** Japanese 12-key keyboard [FULL-WIDTH ALPHABET MODE] */
	public static final int KEYCODE_JP12_ZEN_ALPHA  = -225;
	/** Japanese 12-key keyboard [FULL-WIDTH KATAKANA MODE] */
	public static final int KEYCODE_JP12_ZEN_KATA   = -226;
	/** Japanese 12-key keyboard [HALF-WIDTH KATAKANA MODE] */
	public static final int KEYCODE_JP12_HAN_KATA   = -227;
	/** Japanese 12-key keyboard [HALF-WIDTH NUMBER MODE] */
	public static final int KEYCODE_JP12_HAN_NUM    = -228;
	/** Japanese 12-key keyboard [HALF-WIDTH ALPHABET MODE] */
	public static final int KEYCODE_JP12_HAN_ALPHA  = -229;
	/** Japanese 12-key keyboard [MODE TOOGLE CHANGE] */
	public static final int KEYCODE_JP12_TOGGLE_MODE = -230;
	/** Japanese qwerty-key keyboard [ZENKAKU SPACE] */
	public static final int KEYCODE_QWERTY_ZEN_SPACE = -231;
	/** Japanese 12-key keyboard [UP ARROW] */
	public static final int KEYCODE_JP12_UP = -232;
	/** Japanese 12-key keyboard [DOWN ARROW] */
	public static final int KEYCODE_JP12_DOWN = -233;
	/** Japanese 12-key keyboard [FULL-WIDTH SYMBOL MODE] */
	public static final int KEYCODE_JP12_ZEN_SYM     = -234;
	/** Japanese 12-key keyboard [CONVERT TO PREDICT WORDS] */
	public static final int KEYCODE_JP12_CONVPREDICT = -235;
	/** Japanese 12-key keyboard [CONVERT TO PREDICT WORDS] */
	public static final int KEYCODE_JP12_CONVPREDICT_BACKWARD = -236;
	/** Japanese 12-key keyboard [MODE TOOGLE CHANGE] without Open Popup */
	public static final int KEYCODE_JP12_TOGGLE_MODE2 = -237;
	/** Japanese 12-key keyboard [MODE TOOGLE CHANGE] back */
	public static final int KEYCODE_JP12_TOGGLE_MODE_BACK = -238;
	/** Japanese 12-key keyboard [MODE TOOGLE CHANGE] top */
	public static final int KEYCODE_JP12_TOGGLE_MODE_TOP = -239;
	/** Function [Select All] */
	public static final int KEYCODE_FUNCTION_SELECTALL = -240;
	/** Function [Cut] */
	public static final int KEYCODE_FUNCTION_CUT = -241;
	/** Function [Copy] */
	public static final int KEYCODE_FUNCTION_COPY = -242;
	/** Function [Paste] */
	public static final int KEYCODE_FUNCTION_PASTE = -243;
	/** Function [Select Toggle] */
	public static final int KEYCODE_FUNCTION_SELECT = -244;
	/** Japanese qwerty-key keyboard [ZENKAKU SPACE] Shift */
	public static final int KEYCODE_QWERTY_ZEN_SPACE_SHIFT = -245;
	/** Japanese qwerty-key keyboard [ZENKAKU SPACE] without convert */
	public static final int KEYCODE_QWERTY_ZEN_SPACE2 = -246;
	/** Japanese qwerty-key keyboard [ZENKAKU SPACE] Shift without convert */
	public static final int KEYCODE_QWERTY_ZEN_SPACE2_SHIFT = -247;
	/** Japanese 12-key keyboard [SPACE] Shift */
	public static final int KEYCODE_JP12_SPACE_SHIFT = -248;


	/** Key code for EISU-KANA conversion */
	public static final int KEYCODE_EISU_KANA = -305;

	/** Key code for NOP (no-operation) */
	public static final int KEYCODE_NOP = -310;

	/** Key code for ARROW STOP (no-operation) */
	public static final int KEYCODE_ARROW_STOP = -311;

	/** Key code for preference setting */
	public static final int KEYCODE_PREFERENCE_SETTING = -999;

	/** Key code for help */
	public static final int KEYCODE_SHOW_HELP = -998;

	/** Key code for Mushroom */
	public static final int KEYCODE_MUSHROOM = -997;

	/** Key code for switching to full-width HIRAGANA mode */
	public static final int KEYCODE_SWITCH_FULL_HIRAGANA = -301;

	/** Key code for switching to full-width KATAKANA mode */
	public static final int KEYCODE_SWITCH_FULL_KATAKANA = -302;

	/** Key code for switching to full-width alphabet mode */
	public static final int KEYCODE_SWITCH_FULL_ALPHABET = -303;

	/** Key code for switching to full-width number mode */
	public static final int KEYCODE_SWITCH_FULL_NUMBER = -304;

	/** Key code for switching to half-width KATAKANA mode */
	public static final int KEYCODE_SWITCH_HALF_KATAKANA = -306;

	/** Key code for switching to half-width alphabet mode */
	public static final int KEYCODE_SWITCH_HALF_ALPHABET = -307;

	/** Key code for switching to half-width number mode */
	public static final int KEYCODE_SWITCH_HALF_NUMBER = -308;

	/** Key code for case toggle key */
	public static final int KEYCODE_SELECT_CASE = -309;

	/** Key code for EISU-KANA conversion */
	// public static final int KEYCODE_EISU_KANA = -305;

	/** Key code for JIS "ろ" */
	public static final int KEYCODE_JIS_RO = -350;

	/** Key code for JIS "ー" */
	public static final int KEYCODE_JIS_CHOUON = -351;

	/** Key code for JIS "変換/前候補" */
	public static final int KEYCODE_JIS_PREV_CANDIDATE = -352;

	/** Key code for JIS "無変換" */
	public static final int KEYCODE_JIS_NO_CONVERT = -353;

	/** Key code for JIS "ひらがな/カタカナ" */
	public static final int KEYCODE_JIS_HIRA_KATA = -354;

	/** Key code for switching to full-width Nicotouch mode */
	public static final int KEYCODE_SWITCH_FULL_NICO = -400;

	/** Key code for switching to full-width Nicotouch katakana mode */
	public static final int KEYCODE_SWITCH_FULL_NICO_KATAKANA = -401;

	/** Key code for switching to half-width Nicotouch katakana mode */
	public static final int KEYCODE_SWITCH_HALF_NICO_KATAKANA = -402;

	/** Nico2 keys */
	public static final int KEYCODE_NEWKEY_0 = -410;
	public static final int KEYCODE_NEWKEY_1 = -411;
	public static final int KEYCODE_NEWKEY_2 = -412;
	public static final int KEYCODE_NEWKEY_3 = -413;
	public static final int KEYCODE_NEWKEY_4 = -414;

	/** Key code for DAKUTEN/HANDAKUTEN conversion */
	public static final int KEYCODE_DAKUTEN = -900;
	public static final int KEYCODE_HANDAKUTEN = -901;
	public static final int KEYCODE_KANASMALL = -902;

	/* for Qwerty keyboard */
	/** Qwerty keyboard [DEL] */
	public static final int KEYCODE_QWERTY_BACKSPACE = -100;
	/** Qwerty keyboard [ENTER] */
	public static final int KEYCODE_QWERTY_ENTER = -101;
	/** Qwerty keyboard [SHIFT] */
	public static final int KEYCODE_QWERTY_SHIFT = Keyboard.KEYCODE_SHIFT;
	/** Qwerty keyboard [ALT] */
	public static final int KEYCODE_QWERTY_ALT   = -103;
	/** Qwerty keyboard [KEYBOARD TYPE CHANGE] */
	public static final int KEYCODE_QWERTY_KBD   = -104;
	/** Qwerty keyboard [CLOSE] */
	public static final int KEYCODE_QWERTY_CLOSE = -105;
	/** Japanese Qwerty keyboard [EMOJI] */
	public static final int KEYCODE_QWERTY_EMOJI = -106;
	/** Japanese Qwerty keyboard [FULL-WIDTH HIRAGANA MODE] */
	public static final int KEYCODE_QWERTY_ZEN_HIRA   = -107;
	/** Japanese Qwerty keyboard [FULL-WIDTH NUMBER MODE] */
	public static final int KEYCODE_QWERTY_ZEN_NUM    = -108;
	/** Japanese Qwerty keyboard [FULL-WIDTH ALPHABET MODE] */
	public static final int KEYCODE_QWERTY_ZEN_ALPHA  = -109;
	/** Japanese Qwerty keyboard [FULL-WIDTH KATAKANA MODE] */
	public static final int KEYCODE_QWERTY_ZEN_KATA   = -110;
	/** Japanese Qwerty keyboard [HALF-WIDTH KATAKANA MODE] */
	public static final int KEYCODE_QWERTY_HAN_KATA   = -111;
	/** Qwerty keyboard [NUMBER MODE] */
	public static final int KEYCODE_QWERTY_HAN_NUM    = -112;
	/** Qwerty keyboard [ALPHABET MODE] */
	public static final int KEYCODE_QWERTY_HAN_ALPHA  = -113;
	/** Qwerty keyboard [MODE TOOGLE CHANGE] */
	public static final int KEYCODE_QWERTY_TOGGLE_MODE = -114;
	/** Qwerty keyboard [PINYIN MODE] */
	public static final int KEYCODE_QWERTY_PINYIN  = -115;
	/** Qwerty keyboard [MODE TOOGLE CHANGE] without Open Popup */
	public static final int KEYCODE_QWERTY_TOGGLE_MODE2 = -117;
	/** Qwerty keyboard [CTRL] */
	public static final int KEYCODE_QWERTY_CTRL   = -118;
	/** Qwerty keyboard [MODE TOOGLE CHANGE] back */
	public static final int KEYCODE_QWERTY_TOGGLE_MODE_BACK = -119;
	/** Qwerty keyboard [MODE TOOGLE CHANGE] top */
	public static final int KEYCODE_QWERTY_TOGGLE_MODE_TOP = -120;
	/** Qwerty keyboard swappable SHIFT key */
	public static final int KEYCODE_QWERTY_MYSHIFT = -121;
	/** Qwerty keyboard swappable ENTER key */
	public static final int KEYCODE_QWERTY_MINIENTER = -122;
	/** Qwerty keyboard swappable ENTER key 2 */
	public static final int KEYCODE_QWERTY_MINIENTER2 = -123;

	public static final int KEYCODE_USERSYMBOL              = -1000;
	public static final int KEYCODE_USERSYMBOL_ZEN_HIRAGANA = -1001;
	public static final int KEYCODE_USERSYMBOL_ZEN_KATAKANA = -1002;
	public static final int KEYCODE_USERSYMBOL_ZEN_ALPHABET = -1003;
	public static final int KEYCODE_USERSYMBOL_ZEN_NUMBER   = -1004;
	public static final int KEYCODE_USERSYMBOL_HAN_KATAKANA = -1005;
	public static final int KEYCODE_USERSYMBOL_HAN_ALPHABET = -1006;
	public static final int KEYCODE_USERSYMBOL_HAN_NUMBER   = -1007;
	public static final int KEYCODE_USERSYMBOL_MAX          = -1099;

	/** IS01 key */
	public static final int KEYCODE_IS01_E_KAO_KI  = 92;
	public static final int KEYCODE_IS01_MOJI  = 93;

	/** IS11SH key */
	public static final int KEYCODE_IS11SH_E_KAO_KI  = 94;
	public static final int KEYCODE_IS11SH_MOJI  = 95;

	/** 007SH key */
	public static final int KEYCODE_007SH_E_KAO_KI  = 1;
	public static final int KEYCODE_007SH_MOJI  = 2;

	/** OpenWnn instance which hold this software keyboard*/
	protected NicoWnnG      mWnn;

	/** Current keyboard view */
	protected MyKeyboardView mKeyboardView;

	/** View objects (main side) */
	protected ViewGroup mMainView;
	/** View objects (sub side) */
	protected ViewGroup mSubView;

	/** Current keyboard definition */
	protected MyHeightKeyboard mCurrentKeyboard;

	/** Caps lock state */
	protected boolean mCapsLock;

	/** Input restraint */
	// protected boolean mDisableKeyInput = true;

	/**
	 * flick mode works
	 */
	protected boolean mNicoFirst = false;

	protected MyHeightKeyboard[][][] mNicoKeyboard;
	protected int mNicoKeyMode = 0;


	/** Previous input character code */
	protected int mPrevInputKeyCode = 0;
	protected int mPrevInputKeyDir = 0;

	private int mPressedKeyCode = 0;

	/** flick nicoinput **/
	public static final int NICOFLICK_NONE       = 0;
	public static final int NICOFLICK_1STROKE    = 1;
	public static final int NICOFLICK_NICOSTROKE = 2;

	public int mFlickNicoInput = 0;
	public boolean mNicoFlick = false;

	public boolean mUse12KeyShift = true;

	public boolean mUse12KeySubTen = false;
	public boolean mUseQwertySubTen = false;
	public int mSubTen12KeyMode = 0;
	public int mSubTenQwertyMode = 0;

	/** gecture  **/
	private static final DisplayMetrics mMetrics = new DisplayMetrics();
	private GestureDetector   mDetector;
	private float             mStartX, mStartY;
	private float             mGestureX, mGestureY;
	private boolean           mIsActiveLongPress;
	private boolean           mIsLongPress;
	//private static final int ya_flick[] = { 0, 2, -1, 1, -1 };
	//private static final int wa_flick[] = { 0, -1, 1, 2, 5 };
	/**
	 * flick sensivity
	 */
	public static final int[] flickSensitivityModeTable = {
		15, 20, 25, 30, 35, 40, 50, 60, 80, 100,
	};
	public int mFlickSensitivity = 0;
	public boolean mFlickGuide = true;
	/**
	 * input keyboard key height
	 */
	public int mInputViewHeightIndex = 0;
	public final static int mKeyboardPaddingTable[] = {
		13, 19, 25, 31, 37, 43, 49, 55, 67, 79,
	};

	/** Definition for {@code mInputType} (toggle) */
	public static final int INPUT_TYPE_TOGGLE = 1;

	/** Definition for {@code mInputType} (commit instantly) */
	public static final int INPUT_TYPE_INSTANT = 2;

	/** Max key number of the 12 key keyboard (depends on the definition of keyboards) */
	public static final int KEY_NUMBER_12KEY = 20;

	/** Kana Mode */
	public static final int KANAMODE_ROMAN = 0;
	public static final int KANAMODE_JIS = 1;
	public static final int KANAMODE_50ON = 2;
	public static final int KANAMODE_ROMAN_MINI = 3;
	public static final int KANAMODE_ROMAN2 = 4;
	public static final int KANAMODE_JIS2 = 5;
	public static final int KANAMODE_50ON2 = 6;
	public static final int KANAMODE_ROMAN_MINI2 = 7;
	public static final int KANAMODE_ROMAN_COMPACT = 8;

	/** Character table for full-width number */
	public static final char[] INSTANT_CHAR_CODE_FULL_NUMBER =
		"\uff11\uff12\uff13\uff14\uff15\uff16\uff17\uff18\uff19\uff10\uff03\uff0a".toCharArray();

	/** Character table for half-width number */
	public static final char[] INSTANT_CHAR_CODE_HALF_NUMBER =
		"1234567890#*".toCharArray();

	/**
	 * Character table to input when mInputType becomes INPUT_TYPE_INSTANT.
	 * (Either INSTANT_CHAR_CODE_FULL_NUMBER or INSTANT_CHAR_CODE_HALF_NUMBER)
	 */
	protected char[] mCurrentInstantTable = null;


	/** KeyIndex of "Moji" key on 12 keyboard (depends on the definition of keyboards) */
	public static final int KEY_INDEX_CHANGE_MODE_12KEY = 15;

	/** KeyIndex of "Moji" key on QWERTY keyboard (depends on the definition of keyboards) */
	public static final int KEY_INDEX_CHANGE_MODE_QWERTY = 29;

	/** Type of input mode */
	protected int mInputType = INPUT_TYPE_TOGGLE;


	/** Whether the InputType is null */
	protected boolean mIsInputTypeNull = false;

	/** "Moji" key (this is used for canceling long-press) */
	protected Keyboard.Key mChangeModeKey = null;

	/** PopupResId of "Moji" key (this is used for canceling long-press) */
	protected int mPopupResId = 0;

	/** Auto caps mode */
	protected boolean mEnableAutoCaps = true;

	/** Input mode that is not able to be changed. If ENABLE_CHANGE_KEYMODE is set, input mode can change. */
	protected int[] mLimitedKeyMode = null;

	/** Input mode that is given the first priority. If ENABLE_CHANGE_KEYMODE is set, input mode can change. */
	protected int mPreferenceKeyMode = INVALID_KEYMODE;

	/** The constant for mFixedKeyMode. It means that input mode is not fixed. */
	protected static final int INVALID_KEYMODE = -1;

	/** The last input type */
	protected int mLastInputType = 0;

	/** Flick Mode Key */
	protected boolean mCanFlickModeKey = true;
	
	/** Flick Arrow Key */
	protected boolean mCanFlickArrowKey = true;
	
	public static final int CUTPASTEACTIONBYIME_NONE = 0;
	public static final int CUTPASTEACTIONBYIME_ALT = 1;
	public static final int CUTPASTEACTIONBYIME_CTRL = 2;
	public static final int CUTPASTEACTIONBYIME_CTRL_ALT = 3;
	protected int mCutPasteActionByIme = CUTPASTEACTIONBYIME_CTRL_ALT;

	// KEY TYPE
	public static final int KEYTYPE_12KEY = 0;
	public static final int KEYTYPE_QWERTY = 1;
	public static final int KEYTYPE_NICO2 = 2;
	public static final int KEYTYPE_SUBTEN_QWERTY = 3;
	public static final int KEYTYPE_SUBTEN_12KEY = 4;

	protected int mQwertyKanaMode = 0;
	protected boolean mQwertyMatrixMode = false;
	protected boolean mQwertySwapShiftAlt = false;
	protected boolean mQwertySwapMiniEnter = false;

	private boolean mKeyRepeatReleased = false;

	protected MyPopupInputImeMode mMyPopupInputImeMode = null;
	protected int mKeyRepeatFirstTimeout = ViewConfiguration.getLongPressTimeout();
	protected int mKeyRepeatSecondTimeout = 100;
	protected int mKeyRepeatCount = 0;
	protected int mKeyRepeatCode = 0;
	protected final Handler mHandlerKeyRepeat = new Handler();
	protected final Runnable mActionKeyRepeat = new Runnable() {
		public void run() {
			synchronized (this) {
				mIsLongPress = true;
				{
					// キー入力取り消し
			  		final long now = SystemClock.uptimeMillis();
		            MotionEvent event = MotionEvent.obtain(
		              now, now,
		              MotionEvent.ACTION_CANCEL, 0, 0, 0
		            );
			  		mDetector.onTouchEvent(event);
			  		event.recycle();
			  	}
				switch (mKeyRepeatCode) {
					case KEYCODE_JP12_TOGGLE_MODE2:
					case KEYCODE_QWERTY_TOGGLE_MODE2:
						showPreview(NOT_A_KEY, -1);
						invokeMyPopupInputImeMode();
						break;
	

				  case KEYCODE_JP12_EMOJI:
				  case KEYCODE_QWERTY_EMOJI:
				  case KEYCODE_EISU_KANA:
					showPreview(NOT_A_KEY, -1);
					final String str = mWnn.getComposingText(ComposingText.LAYER2);
					mWnn.onEvent(mWnn.mEventInputBack);
					mWnn.invokeMushroom(str);
					break;

				  default:
					mKeyRepeatCount++;
					onKey(mKeyRepeatCode, null);
					if (mKeyRepeatCount > 1) {
						mHandlerKeyRepeat.postDelayed(mActionKeyRepeat, mKeyRepeatSecondTimeout);
					}
					break;
				}
			}
		}
	};

	public void setKeyRepeat(int code) {
		synchronized (this) {
			if (mKeyRepeatCount == 0) {
				mKeyRepeatCount = 1;
				mKeyRepeatCode = code;
				mHandlerKeyRepeat.postDelayed(mActionKeyRepeat, mKeyRepeatFirstTimeout);
			}
		}
	}

	public void resetKeyRepeat() {
		synchronized (this) {
			if (mKeyRepeatCount > 0) {
				mHandlerKeyRepeat.removeCallbacks(mActionKeyRepeat);
				mKeyRepeatCount = 0;
			}
			mIsLongPress = false;
		}
	}

	public boolean isEndOfArrowKeyRepeat() {
		boolean ret = (mKeyRepeatCode == KEYCODE_ARROW_STOP);
		return ret;
	}
	
	public boolean dismissMyPopupInputImeMode() {
		if (Build.VERSION.SDK_INT >= 11) {
			if (mMyPopupInputImeMode != null) {
				mMyPopupInputImeMode.cancel();
				mMyPopupInputImeMode = null;
				return true;
			}
		}
		return false;
	}

	public void invokeMyPopupInputImeMode() {
		if (Build.VERSION.SDK_INT >= 11) {
			final int w = 5;
			final int h = 2;
			final View v = mKeyboardView;
			final Context c = v.getContext();
			final int bw = (int)(v.getWidth()/6);
			final int bh = mCurrentKeyboard.getKeyHeight();
			mMyPopupInputImeMode = new MyPopupInputImeMode(c, w, h, bw, bh);
			final MyPopupInputImeMode dlg = mMyPopupInputImeMode;
			{
				dlg.setItemText(0, 0, mWnn.getString(R.string.key_12key_switch_full_hiragana));
				dlg.setItemText(1, 0, mWnn.getString(R.string.key_12key_switch_full_katakana));
				dlg.setItemText(2, 0, mWnn.getString(R.string.key_12key_switch_full_alphabet));
				dlg.setItemText(3, 0, mWnn.getString(R.string.key_12key_switch_full_number));
				dlg.setItemText(4, 0, mWnn.getString(R.string.preference_help));
				{
					int resid = R.drawable.key_mode_panel_kbd_12key_b;
					if (is12keyMode()) {
						resid = R.drawable.key_mode_panel_kbd_b;
					}
					dlg.setItemIcon(0, 1, BitmapFactory.decodeResource(mWnn.getResources(), resid));
				}
				dlg.setItemText(1, 1, mWnn.getString(R.string.key_12key_switch_half_katakana));
				dlg.setItemText(2, 1, mWnn.getString(R.string.key_12key_switch_half_alphabet));
				dlg.setItemText(3, 1, mWnn.getString(R.string.key_12key_switch_half_number));
				dlg.setItemText(4, 1, mWnn.getString(R.string.preference_ime_setting_app));

				dlg.setOnMyPopupInputListener(mMyPopupInputListener);
				dlg.setLongClickEnable(false);
				dlg.show(v);
				/*
			//new Runnable(){ public void run() {
				dlg.create();
				new Thread(new Runnable() { public void run() {
					final int result = dlg.show();
					if (result != AwaitAlertDialogBase.CANCEL) {
						final int id = result - AwaitAlertDialogIconButton.CLICKED;
						if ((0 <= id) && (id < w*h)) {
							Log.i("nicoWnnG", "AwaitAlertDialogIconButton="+id);
						}
					}
				}}).start();
			//}};
				 */
			}
		} else {
			mWnn.openPreferenceSetting();
		}
	}
	
	public boolean is12keyMode() {
		return(m12keyTable[mCurrentKeyMode] == KEYBOARD_12KEY);	
	}

	private final OnMyPopupInputListener mMyPopupInputListener = new OnMyPopupInputListener() { 
		public void onInput(final int id) {
			if (id == OnMyPopupInputListener.CANCEL) {
				mMyPopupInputImeMode = null;
				return;
			}
			switch (id-OnMyPopupInputListener.CLICKED) {
				case 0:  // 全角ひらがな
					if (
						mWnn.mInputViewMode.equals(mWnn.INPUTMODE_2TOUCH) ||
						mWnn.mInputViewMode.equals(mWnn.INPUTMODE_BELL) ||
						mWnn.mInputViewMode.equals(mWnn.INPUTMODE_NICO)
					) {
						changeKeyMode(KEYMODE_JA_FULL_NICO);
					} else {
						changeKeyMode(KEYMODE_JA_FULL_HIRAGANA);
					}
					break;

				case 1:  // 全角カタカナ
					changeKeyMode(KEYMODE_JA_FULL_KATAKANA);
					break;

				case 2:  // 全角アルファベット
					changeKeyMode(KEYMODE_JA_FULL_ALPHABET);
					break;

				case 3:  // 全角数字
					changeKeyMode(KEYMODE_JA_FULL_NUMBER);
					break;

				case 4:  // ヘルプ
					if (mWnn != null) {
						mWnn.openHelp();
					}
					break;

				case 5:  // キーボード変更
					if (m12keyTable[mCurrentKeyMode] == KEYBOARD_12KEY) {
						changeKeyboardType(KEYBOARD_QWERTY);
					} else {
						changeKeyboardType(KEYBOARD_12KEY);
					}
					break;

				case 6:  // 半角カタカナ
					changeKeyMode(KEYMODE_JA_HALF_KATAKANA);
					break;

				case 7:  // 半角アルファベット
					changeKeyMode(KEYMODE_JA_HALF_ALPHABET);
					break;

				case 8:  // 半角数字
					changeKeyMode(KEYMODE_JA_HALF_NUMBER);
					break;

				case 9:  // 設定
					if (mWnn != null) {
						mWnn.hideWindow();  // これがないと落ちる
						mWnn.openPreferenceSetting();
					}
					break;

			}
			mMyPopupInputImeMode = null;
			return;
		}
	};

	/**
	 * Keyboard surfaces
	 * <br>
	 * Keyboard[language][portrait/landscape][keyboard type][shift off/on][key-mode]
	 */
	public    MyHeightKeyboard[][][][][][] mKeyboard;
	protected boolean              mCreateKeyboard = false;
	protected boolean mNoAlphaMode = false;
	protected boolean mNoNumberMode = false;

	/* languages */
	/** Current language */
	protected int mCurrentLanguage;
	/** Language (English) */
	public static final int LANG_EN  = 0;
	/** Language (Japanese) */
	public static final int LANG_JA  = 1;
	/** Language (Chinese) */
	public static final int LANG_CN  = 2;
	/** */
	public static final int LANG_MAX  = 3;

	/* portrait/landscape */
	/** State of the display */
	protected int mDisplayMode = 0;
	/** Display mode (Portrait) */
	public static final int PORTRAIT  = 0;
	/** Display mode (Landscape) */
	public static final int LANDSCAPE = 1;
	/** */
	public static final int PORTRAIT_LANDSCAPE = 2;

	/* keyboard type */
	/** Current keyboard type */
	protected int mCurrentKeyboardType;
	/** Keyboard (QWERTY keyboard) */
	public static final int KEYBOARD_QWERTY  = 0;
	/** Keyboard (12-keys keyboard) */
	public static final int KEYBOARD_12KEY   = 1;
	/** */
	public static final int KEYBOARD_MAX   = 4;
	/** State of the shift key */
	protected int mShiftOn = KEYBOARD_SHIFT_OFF;
	/** Shift key off */
	public static final int KEYBOARD_SHIFT_OFF = 0;
	/** Shift key on */
	public static final int KEYBOARD_SHIFT_ON  = 1;
	/** Shift key 2nd */
	public static final int KEYBOARD_SHIFT_2ND  = 2;
	/** */
	public static final int KEYBOARD_SHIFT_MAX  = 3;

	/** State of the alt key */
	protected int mAltOn = 0;
	/** State of the ctrl key */
	protected int mCtrlOn = 0;


	/* key-modes */
	/** Current key-mode */
	protected int mCurrentKeyMode;

	/** change eisu change mode **/
	protected boolean mKana12Key = false;
	protected boolean mAlpha12Key = false;
	protected boolean mNum12Key = false;
	protected int[] m12keyTable = new int[KEYMODE_JA_MAX];

	protected boolean mGetNoFlipScreen = false;
	protected boolean mNoFlipScreen = false;
	protected boolean mUseEmailKana = false;
	protected int mInputModeStart = 0;
	protected int mInputModeNext = 0;

	protected boolean mChangeOnHardKey = false;

	/* Slide key(only Nicotouch */
	protected int  mCurrentSlide = 0;
	public static final int NICO_SLIDE_MODE_TOP   = 0;
	public static final int NICO_SLIDE_MODE_SHIFT = 1;
	public static final int NICO_SLIDE_MODE_A     = 2;
	public static final int NICO_SLIDE_MODE_K     = 3;
	public static final int NICO_SLIDE_MODE_S     = 4;
	public static final int NICO_SLIDE_MODE_T     = 5;
	public static final int NICO_SLIDE_MODE_N     = 6;
	public static final int NICO_SLIDE_MODE_H     = 7;
	public static final int NICO_SLIDE_MODE_M     = 8;
	public static final int NICO_SLIDE_MODE_Y     = 9;
	public static final int NICO_SLIDE_MODE_R     = 10;
	public static final int NICO_SLIDE_MODE_W     = 11;
	public static final int NICO_SLIDE_MODE_MAX   = 12;

	/* key-modes for Nicotouch */
	public static final int NICO_MODE_FULL_HIRAGANA = 0;
	public static final int NICO_MODE_FULL_KATAKANA = 1;
	public static final int NICO_MODE_HALF_KATAKANA = 2;
	public static final int NICO_MODE_MAX = 3;

	/* key-modes for English */
	/** English key-mode (alphabet) */
	public static final int KEYMODE_EN_ALPHABET = 0;
	/** English key-mode (number) */
	public static final int KEYMODE_EN_NUMBER   = 1;
	/** English key-mode (phone number) */
	public static final int KEYMODE_EN_PHONE    = 2;

	/* key-modes for Japanese */
	/** Japanese key-mode (Full-width Nicotouch) */
	public static final int KEYMODE_JA_FULL_NICO     = 0;
	/** Japanese key-mode (Full-width Hiragana) */
	public static final int KEYMODE_JA_FULL_HIRAGANA = 1;
	/** Japanese key-mode (Full-width alphabet) */
	public static final int KEYMODE_JA_FULL_ALPHABET = 2;
	/** Japanese key-mode (Full-width number) */
	public static final int KEYMODE_JA_FULL_NUMBER   = 3;
	/** Japanese key-mode (Full-width Katakana) */
	public static final int KEYMODE_JA_FULL_KATAKANA = 4;
	/** Japanese key-mode (Half-width alphabet) */
	public static final int KEYMODE_JA_HALF_ALPHABET = 5;
	/** Japanese key-mode (Half-width number) */
	public static final int KEYMODE_JA_HALF_NUMBER   = 6;
	/** Japanese key-mode (Half-width Katakana) */
	public static final int KEYMODE_JA_HALF_KATAKANA = 7;
	/** Japanese key-mode (Half-width phone number) */
	public static final int KEYMODE_JA_HALF_PHONE    = 8;
	/** Japanese key-mode (Full-width Nicotouch Katakana) */
	public static final int KEYMODE_JA_FULL_NICO_KATAKANA = 9;
	/** Japanese key-mode (Full-width Nicotouch Katakana) */
	public static final int KEYMODE_JA_HALF_NICO_KATAKANA = 10;
	/** */
	public static final int KEYMODE_JA_MAX    = 11;

	/* key-modes for Chinese */
	/** Chinese key-mode (pinyin) */
	public static final int KEYMODE_CN_PINYIN   = 0;
	/** Chinese key-mode (Full-width number) */
	public static final int KEYMODE_CN_FULL_NUMBER   = 1;
	/** Chinese key-mode (alphabet) */
	public static final int KEYMODE_CN_ALPHABET = 2;
	/** Chinese key-mode (phone) */
	public static final int KEYMODE_CN_PHONE    = 3;
	/** Chinese key-mode (Half-width number) */
	public static final int KEYMODE_CN_HALF_NUMBER   = 4;

	/* key-modes for HARD */
	/** HARD key-mode (SHIFT_OFF_ALT_OFF) */
	public static final int HARD_KEYMODE_SHIFT_OFF_ALT_OFF     = 2;
	/** HARD key-mode (SHIFT_ON_ALT_OFF) */
	public static final int HARD_KEYMODE_SHIFT_ON_ALT_OFF      = 3;
	/** HARD key-mode (SHIFT_OFF_ALT_ON) */
	public static final int HARD_KEYMODE_SHIFT_OFF_ALT_ON      = 4;
	/** HARD key-mode (SHIFT_ON_ALT_ON) */
	public static final int HARD_KEYMODE_SHIFT_ON_ALT_ON       = 5;
	/** HARD key-mode (SHIFT_LOCK_ALT_OFF) */
	public static final int HARD_KEYMODE_SHIFT_LOCK_ALT_OFF    = 6;
	/** HARD key-mode (SHIFT_LOCK_ALT_ON) */
	public static final int HARD_KEYMODE_SHIFT_LOCK_ALT_ON     = 7;
	/** HARD key-mode (SHIFT_LOCK_ALT_LOCK) */
	public static final int HARD_KEYMODE_SHIFT_LOCK_ALT_LOCK   = 8;
	/** HARD key-mode (SHIFT_OFF_ALT_LOCK) */
	public static final int HARD_KEYMODE_SHIFT_OFF_ALT_LOCK    = 9;
	/** HARD key-mode (SHIFT_ON_ALT_LOCK) */
	public static final int HARD_KEYMODE_SHIFT_ON_ALT_LOCK     = 10;

	/** Whether the H/W keyboard is hidden. */
	protected boolean mIsHardKeyboard   = false;
	protected boolean mHardKeyboardHidden = true;
	/** option softkeyboard on/off **/
	protected int mHiddenSoftKeyboard = 0;

	/**
	 * Status of the composing text
	 * <br>
	 * {@code true} if there is no composing text.
	 */
	protected boolean mNoInput = true;

	/** Vibratior for key click vibration */
	static Vibrator mVibrator = null;

	/** key click sound */
	static SoundPool mSoundPool = null;
	static int mSound = 0;
	static float mSoundVolume = 0.0F;
	static int mSoundStreamId = 0;

	/** Key toggle cycle table currently using */
	protected String[] mCurrentCycleTable;

	/** */
	protected boolean mTsuMode = false;

	/** */
	protected boolean mKana12SpaceZen = false;
	protected boolean mQwertySpaceZen = false;

	protected boolean mSpaceBelowKeyboard = false;

	protected boolean mEnglishPredict12Key = true;
	protected boolean mEnglishPredictQwerty = false;

	protected boolean mUseOnetimeShift = true;
	protected boolean mUseFixedShift = true;
	public int mShiftKeyStyle = 0;
	protected int mShiftLockCount = 0;

	protected boolean mAutoForwardToggle12key = true;

	/** option bell type **/
	protected final int ALPHAMODE_SMALL = 0;
	protected final int ALPHAMODE_BIG   = 1;
	protected int mChangeAlphaBigMode = ALPHAMODE_SMALL;

	int mKeyboardViewLayoutId;

	/**
	 * Constructor
	 */
	public DefaultSoftKeyboard(final NicoWnnG parent) {
		mWnn = parent;
	}

	public boolean checkOption(final SharedPreferences pref) {
		boolean b;
		int i;
		
			i = Integer.valueOf(mWnn.getOrientPrefString(pref, "hidden_softkeyboard4", "3"));
			if (mHiddenSoftKeyboard != i) {
				return true;
			}
			i = Integer.valueOf(mWnn.getOrientPrefString(pref, "mainview_height_mode2", "0"));
			if (mInputViewHeightIndex != i) {
				return true;
			}
			i = Integer.valueOf(mWnn.getOrientPrefString(pref, "qwerty_kana_mode3", "0"));
			if (mQwertyKanaMode != i) {
				return true;
			}
			b = mWnn.getOrientPrefBoolean(pref, "qwerty_matrix_mode", false);
			if (mQwertySwapShiftAlt != b) {
				return true;
			}
			b = mWnn.getOrientPrefBoolean(pref, "qwerty_swap_minienter", false);
			if (mQwertySwapMiniEnter != b) {
				return true;
			}
			b = mWnn.getOrientPrefBoolean(pref, "qwerty_swap_shift_alt", true);
			if (mQwertyMatrixMode != b) {
				return true;
			}
			b = pref.getBoolean("tsu_du_ltu", false);
			if (mTsuMode != b) {
				return true;
			}
			b = mWnn.getOrientPrefBoolean(pref, "kana12_space_zen", false);
			if (mKana12SpaceZen != b) {
				return true;
			}
			b = mWnn.getOrientPrefBoolean(pref, "qwerty_space_zen", true);
			if (mQwertySpaceZen != b) {
				return true;
			}
			b = mWnn.getOrientPrefBoolean(pref, "english_predict_qwerty", false);
			if (mEnglishPredictQwerty != b) {
				return true;
			}
			b = mWnn.getOrientPrefBoolean(pref, "english_predict_12key", true);
			if (mEnglishPredict12Key != b) {
				return true;
			}
			b = mWnn.getOrientPrefBoolean(pref, "space_below_keyboard", false);
			if (mSpaceBelowKeyboard != b) {
				return true;
			}
			i = Integer.valueOf(mWnn.getOrientPrefString(pref, "shiftkey_style", "0"));
			if (mShiftKeyStyle != i) {
				return true;
			}
			i = Integer.valueOf(mWnn.getOrientPrefString(pref, "nicoflick_mode", "0"));
			if (mFlickNicoInput != i) {
				return true;
			}
			b = mWnn.getOrientPrefBoolean(pref, "flick_guide", true);
			if (mFlickGuide != b) {
				return true;
			}
			boolean h = pref.getBoolean("change_onhardkey", false);
			if (mChangeOnHardKey != h) {
				return true;
			}
			String onhardkey = ((h&&isEnabledHardKeyboard()) ? "_onhardkey" : "");
			b = mWnn.getOrientPrefBoolean(pref, "change_alphanum_12key"+onhardkey, false);
			if (mAlpha12Key != b) {
				return true;
			}
			b = mWnn.getOrientPrefBoolean(pref, "change_kana_12key"+onhardkey, false);
			if (mKana12Key != b) {
				return true;
			}
			b = mWnn.getOrientPrefBoolean(pref, "change_num_12key"+onhardkey, false);
			if (mNum12Key != b) {
				return true;
			}
			b = mWnn.getOrientPrefBoolean(pref, "change_noalpha_qwerty"+onhardkey, false);
			if (mNoAlphaMode != b) {
				return true;
			}
			b = mWnn.getOrientPrefBoolean(pref, "change_nonumber_qwerty"+onhardkey, false);
			if (mNoNumberMode != b) {
				return true;
			}
			b = mWnn.getOrientPrefBoolean(pref, "use_email_kana", false);
			if (mUseEmailKana != b) {
				return true;
			}
			i = Integer.valueOf(mWnn.getOrientPrefString(pref, "input_mode_start"+onhardkey, "0"));
			if (mInputModeStart != i) {
				return true;
			}
			i = Integer.valueOf(mWnn.getOrientPrefString(pref, "input_mode_next"+onhardkey, "0"));
			if (mInputModeNext != i) {
				return true;
			}

		return false;
	}

	public void loadOption(final SharedPreferences pref) {
		// 先行で取得
		mHiddenSoftKeyboard     = Integer.valueOf(mWnn.getOrientPrefString(pref, "hidden_softkeyboard4", "3"));

		boolean mChangeOnHardKey = pref.getBoolean("change_onhardkey", false);
		String onhardkey = ((mChangeOnHardKey && isEnabledHardKeyboard()) ? "_onhardkey" : "");

		mInputViewHeightIndex   = Integer.valueOf(mWnn.getOrientPrefString(pref, "mainview_height_mode2", "0"));
		mQwertyKanaMode         = Integer.valueOf(mWnn.getOrientPrefString(pref, "qwerty_kana_mode3", "0"));
		mQwertyMatrixMode       = mWnn.getOrientPrefBoolean(pref, "qwerty_matrix_mode", false);
		mQwertySwapShiftAlt     = mWnn.getOrientPrefBoolean(pref, "qwerty_swap_shift_alt", true);
		mQwertySwapMiniEnter    = mWnn.getOrientPrefBoolean(pref, "qwerty_swap_minienter", false);
		mTsuMode                = mWnn.getOrientPrefBoolean(pref, "tsu_du_ltu", false);
		mKana12SpaceZen         = mWnn.getOrientPrefBoolean(pref, "kana12_space_zen", false);
		mQwertySpaceZen         = mWnn.getOrientPrefBoolean(pref, "qwerty_space_zen", true);
		mEnglishPredict12Key    = mWnn.getOrientPrefBoolean(pref, "english_predict_12key", true);
		mEnglishPredictQwerty   = mWnn.getOrientPrefBoolean(pref, "english_predict_qwerty", false);
		mNoAlphaMode            = mWnn.getOrientPrefBoolean(pref, "change_noalpha_qwerty"+onhardkey, false);
		mNoNumberMode           = mWnn.getOrientPrefBoolean(pref, "change_nonumber_qwerty"+onhardkey, false);
		mGetNoFlipScreen        = mWnn.getOrientPrefBoolean(pref, "no_flip_screen", false);
		mUse12KeySubTen         = mWnn.getOrientPrefBoolean(pref, "use_12key_subten", false);
		mUse12KeyShift          = mWnn.getOrientPrefBoolean(pref, "use_12key_shift", true);
		mUseQwertySubTen        = mWnn.getOrientPrefBoolean(pref, "use_qwerty_subten", false);
		mSubTen12KeyMode        = Integer.valueOf(mWnn.getOrientPrefString(pref, "subten_12key_mode2", "1"));
		mSubTenQwertyMode       = Integer.valueOf(mWnn.getOrientPrefString(pref, "subten_qwerty_mode", "0"));
		mFlickNicoInput         = Integer.valueOf(mWnn.getOrientPrefString(pref, "nicoflick_mode", "0"));
		mFlickSensitivity       = Integer.valueOf(mWnn.getOrientPrefString(pref, "flick_sensitivity_mode", "2"));
		mFlickGuide             = mWnn.getOrientPrefBoolean(pref, "flick_guide", true);
		mSpaceBelowKeyboard     = mWnn.getOrientPrefBoolean(pref, "space_below_keyboard", false);
		mAutoForwardToggle12key = mWnn.getOrientPrefBoolean(pref, "autoforward_toggle_12key", true);
		mCutPasteActionByIme    = Integer.valueOf(mWnn.getOrientPrefString(pref, "cutpasteaction_byime", "3"));
		mCanFlickModeKey        = mWnn.getOrientPrefBoolean(pref, "can_flick_mode_key", true);
		mCanFlickArrowKey        = mWnn.getOrientPrefBoolean(pref, "can_flick_arrow_key", true);
		{
			int f = Integer.valueOf(mWnn.getOrientPrefString(pref, "shiftkey_style", "0"));
			mShiftKeyStyle = f;
			mUseOnetimeShift = false;
			mUseFixedShift = true;
			if (f == 1) {
				mUseOnetimeShift = true;
				mUseFixedShift = false;
			} else if (f == 2) {
				mUseOnetimeShift = true;
				mUseFixedShift = true;
			}
		}

		final boolean getalphamode  = mWnn.getOrientPrefBoolean(pref, "change_alphamode", false);
		if (false == getalphamode) {
			mChangeAlphaBigMode = ALPHAMODE_SMALL;
		}else{
			mChangeAlphaBigMode = ALPHAMODE_BIG;
		}

		mNoFlipScreen = mGetNoFlipScreen;

		mUseEmailKana = mWnn.getOrientPrefBoolean(pref, "use_email_kana", false);
		mInputModeStart = Integer.valueOf(mWnn.getOrientPrefString(pref, "input_mode_start"+onhardkey, "0"));
		mInputModeNext = Integer.valueOf(mWnn.getOrientPrefString(pref, "input_mode_next"+onhardkey, "0"));
		if (mInputModeStart == 3) {
			mInputModeStart = NicoWnnGJAJP.getInstance().getLastInputMode();
		}

		mKana12Key = mWnn.getOrientPrefBoolean(pref, "change_kana_12key"+onhardkey, false);
		mAlpha12Key = mWnn.getOrientPrefBoolean(pref, "change_alphanum_12key"+onhardkey, false);
		mNum12Key = mWnn.getOrientPrefBoolean(pref, "change_num_12key"+onhardkey, false);

		MyHeightKeyboard.setSwapShiftAlt(mQwertySwapShiftAlt);
		MyHeightKeyboard.setSwapMiniEnter(mQwertySwapMiniEnter);
		MyHeightKeyboard.setAssign12KeyShift(mUse12KeyShift);
	}
	
	/**
	 * Create keyboard views
	 *
	 */
	protected void createKeyboards() {
		/*
		 *  Keyboard[# of Languages][portrait/landscape][# of keyboard type]
		 *          [shift off/on][max # of key-modes][non-input/input]
		 */
		mKeyboard = new MyHeightKeyboard[LANG_MAX][PORTRAIT_LANDSCAPE][KEYBOARD_MAX][KEYBOARD_SHIFT_MAX][KEYMODE_JA_MAX][2];

		final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mWnn);
		loadOption(pref);
		setM12KeyTable();

		// Create the suitable keyboard object
		if (mDisplayMode == DefaultSoftKeyboard.PORTRAIT) {
			createKeyboardsPortrait();
		} else {
			createKeyboardsLandscape();
		}
	}

	protected void setM12KeyTable() {
		m12keyTable[KEYMODE_JA_FULL_NICO]          = mKana12Key ? KEYBOARD_12KEY : KEYBOARD_QWERTY;
		m12keyTable[KEYMODE_JA_FULL_HIRAGANA]      = mKana12Key ? KEYBOARD_12KEY : KEYBOARD_QWERTY;
		m12keyTable[KEYMODE_JA_FULL_ALPHABET]      = mAlpha12Key ? KEYBOARD_12KEY : KEYBOARD_QWERTY;
		m12keyTable[KEYMODE_JA_FULL_NUMBER]        = mNum12Key ? KEYBOARD_12KEY : KEYBOARD_QWERTY;
		m12keyTable[KEYMODE_JA_FULL_KATAKANA]      = mKana12Key ? KEYBOARD_12KEY : KEYBOARD_QWERTY;
		m12keyTable[KEYMODE_JA_HALF_ALPHABET]      = mAlpha12Key ? KEYBOARD_12KEY : KEYBOARD_QWERTY;
		m12keyTable[KEYMODE_JA_HALF_NUMBER]        = mNum12Key ? KEYBOARD_12KEY : KEYBOARD_QWERTY;
		m12keyTable[KEYMODE_JA_HALF_KATAKANA]      = mKana12Key ? KEYBOARD_12KEY : KEYBOARD_QWERTY;
		m12keyTable[KEYMODE_JA_HALF_PHONE]         = mNum12Key ? KEYBOARD_12KEY : KEYBOARD_QWERTY;
		m12keyTable[KEYMODE_JA_FULL_NICO_KATAKANA] = mKana12Key ? KEYBOARD_12KEY : KEYBOARD_QWERTY;
		m12keyTable[KEYMODE_JA_HALF_NICO_KATAKANA] = mKana12Key ? KEYBOARD_12KEY : KEYBOARD_QWERTY;
	}

	protected void createKeyboardsPortrait() {
		//
	}

	protected void createKeyboardsLandscape() {
		//
	}

	/**
	 *
	 */
	public void checkHiddenKeyboard() {
	}

	/**
	 * Get the keyboard changed the specified shift state.
	 *
	 * @param shift     Shift state
	 * @return          Keyboard view
	 */
	protected MyHeightKeyboard getShiftChangeKeyboard(final int shift) {
		try {
			final MyHeightKeyboard[] kbd = mKeyboard[mCurrentLanguage][mDisplayMode][m12keyTable[mCurrentKeyMode]][shift][mCurrentKeyMode];

			if (!mNoInput && (kbd[1] != null)) {
				return kbd[1];
			}
			return kbd[0];
		} catch (final Exception ex) {
			return null;
		}
	}
	/**
	 * Get the keyboard changed the specified shift state.
	 *
	 */
	protected MyHeightKeyboard getSlideChangeKeyboard(final int slide) {
		try {
			mCurrentSlide = slide;
			final MyHeightKeyboard[] kbd = mKeyboard[mCurrentLanguage][mDisplayMode][m12keyTable[mCurrentKeyMode]][mCurrentSlide][mCurrentKeyMode];

			if (!mNoInput && (kbd[1] != null)) {
				return kbd[1];
			}
			return kbd[0];
		} catch (final Exception ex) {
			return null;
		}
	}

	/**
	 * Get the keyboard changed the specified input mode.
	 *
	 * @param mode      Input mode
	 * @return          Keyboard view
	 */
	protected MyHeightKeyboard getModeChangeKeyboard(final int mode) {
		try {
			final MyHeightKeyboard[] kbd = mKeyboard[mCurrentLanguage][mDisplayMode][m12keyTable[mCurrentKeyMode]][mShiftOn+mCurrentSlide][mode];

			if (!mNoInput && (kbd[1] != null)) {
				return kbd[1];
			}
			return kbd[0];
		} catch (final Exception ex) {
			return null;
		}
	}

	/**
	 * Get the keyboard changed the specified keyboard type
	 *
	 * @param type      Keyboard type
	 * @return          Keyboard view
	 */
	protected MyHeightKeyboard getTypeChangeKeyboard(final int type) {
		try {
			final MyHeightKeyboard[] kbd = mKeyboard[mCurrentLanguage][mDisplayMode][type][mShiftOn+mCurrentSlide][mCurrentKeyMode];

			if (!mNoInput && (kbd[1] != null)) {
				return kbd[1];
			}
			return kbd[0];
		} catch (final Exception ex) {
			return null;
		}
	}

	/**
	 * Get the keyboard when some characters are input or no character is input.
	 *
	 * @param inputed   {@code true} if some characters are inputed; {@code false} if no character is inputed.
	 * @return          Keyboard view
	 */
	protected MyHeightKeyboard getKeyboardInputed(final boolean inputed) {
		try {
			final MyHeightKeyboard[] kbd = mKeyboard[mCurrentLanguage][mDisplayMode][m12keyTable[mCurrentKeyMode]][mShiftOn+mCurrentSlide][mCurrentKeyMode];

			if (inputed && (kbd[1] != null)) {
				return kbd[1];
			}
			return kbd[0];
		} catch (final Exception ex) {
			return null;
		}
	}

	/**
	 * Change the circulative key-mode.
	 */
	protected void toggleKeyMode() {
		/* unlock shift */
		mShiftOn = KEYBOARD_SHIFT_OFF;
		mAltOn = 0;
		mCtrlOn = 0;
		mShiftLockCount = 0;
		mCurrentSlide = 0;

		/* search next defined key-mode */
		final MyHeightKeyboard[][] keyboardList = mKeyboard[mCurrentLanguage][mDisplayMode][m12keyTable[mCurrentKeyMode]][mShiftOn+mCurrentSlide];
		do {
			if (++mCurrentKeyMode >= keyboardList.length) {
				mCurrentKeyMode = 0;
			}
		} while (keyboardList[mCurrentKeyMode][0] == null);

		MyHeightKeyboard kbd;
		if (!mNoInput && (keyboardList[mCurrentKeyMode][1] != null)) {
			kbd = keyboardList[mCurrentKeyMode][1];
		} else {
			kbd = keyboardList[mCurrentKeyMode][0];
		}
		changeKeyboard(kbd);

		mWnn.onEvent(new NicoWnnGEvent(NicoWnnGEvent.CHANGE_MODE,
				NicoWnnGEvent.Mode.DEFAULT));
	}

	/**
	 * Toggle change the shift lock state.
	 */
	protected void toggleShiftLock(int sw) {
		switch (sw) {
		default:
			mShiftLockCount = 0;
			break;
		case 1: // Shift Next
			mShiftLockCount++;
			if (!mUseOnetimeShift) {
				if (mShiftLockCount == 1) {
					mShiftLockCount = 2;
				}
			}
			if (!mUseFixedShift) {
				if (mShiftLockCount == 2) {
					mShiftLockCount = 0;
				}
			}
			if (mShiftLockCount > 2) {
				mShiftLockCount = 0;
			}
			break;
		case 2: // Shift Reset
			if (mShiftLockCount == 1) {
				mShiftLockCount = 0;
			}
			break;
		case 3: // Shift Alternate
			if (mUseOnetimeShift && mUseFixedShift) {
				if (mShiftLockCount == 2) {
					mShiftLockCount = 0;
				} else {
					toggleShiftLock(0);
					mShiftLockCount = 2;
				}
			} else if (mUseOnetimeShift) {
				if (mShiftLockCount != 2) {
					toggleShiftLock(0);
					mShiftLockCount = 2;
				} else {
					mShiftLockCount = 0;
				}
			} else if (mUseFixedShift) {
				if (mShiftLockCount != 1) {
					toggleShiftLock(0);
					mShiftLockCount = 1;
				} else {
					mShiftLockCount = 0;
				}
			}
			break;
		}
		if (mShiftLockCount > 0) {
			/* turn shift on */
			final MyHeightKeyboard newKeyboard = getShiftChangeKeyboard(KEYBOARD_SHIFT_ON);
			if (newKeyboard != null) {
				mShiftOn = KEYBOARD_SHIFT_ON;
				changeKeyboard(newKeyboard);
			}
			mCapsLock = true;
		} else {
			/* turn shift off */
			final MyHeightKeyboard newKeyboard = getShiftChangeKeyboard(KEYBOARD_SHIFT_OFF);
			if (newKeyboard != null) {
				mShiftOn = KEYBOARD_SHIFT_OFF;
				changeKeyboard(newKeyboard);
			}
			mCapsLock = false;
		}

		int mode = 0;
		switch (mShiftLockCount) {
		case 0:
			mode = HARD_KEYMODE_SHIFT_OFF_ALT_OFF;
			break;
		case 1:
			mode = HARD_KEYMODE_SHIFT_ON_ALT_OFF;
			break;
		case 2:
			mode = HARD_KEYMODE_SHIFT_LOCK_ALT_OFF;
			break;
		}
		updateIndicator(mode);
	}

	/**
	 * Handling Alt key event.
	 */
	protected void processAltKey() {
		/* invalid if it is not qwerty mode */
		if (m12keyTable[mCurrentKeyMode] != KEYBOARD_QWERTY) {
			return;
		}

		int mode = -1;
		final int keymode = mCurrentKeyMode;
		switch (mCurrentLanguage) {
			case LANG_EN:
				if (keymode == KEYMODE_EN_ALPHABET) {
					mode = KEYMODE_EN_NUMBER;
				} else if (keymode == KEYMODE_EN_NUMBER) {
					mode = KEYMODE_EN_ALPHABET;
				}
				break;

			case LANG_JA:
				if (keymode == KEYMODE_JA_HALF_ALPHABET) {
					mode = KEYMODE_JA_HALF_NUMBER;
				} else if (keymode == KEYMODE_JA_HALF_NUMBER) {
					mode = KEYMODE_JA_HALF_ALPHABET;
				} else if (keymode == KEYMODE_JA_FULL_ALPHABET) {
					mode = KEYMODE_JA_FULL_NUMBER;
				} else if (keymode == KEYMODE_JA_FULL_NUMBER) {
					mode = KEYMODE_JA_FULL_ALPHABET;
				}
				break;

			default:
				/* invalid */
		}

		if (mode >= 0) {
			final MyHeightKeyboard kbd = getModeChangeKeyboard(mode);
			if (kbd != null) {
				mCurrentKeyMode = mode;
				changeKeyboard(kbd);
			}
		}
	}

	/**
	 * Change the keyboard type.
	 *
	 * @param type  Type of the keyboard
	 * @see net.gorry.android.input.nicownng.DefaultSoftKeyboard#KEYBOARD_QWERTY
	 * @see net.gorry.android.input.nicownng.DefaultSoftKeyboard#KEYBOARD_12KEY
	 */
	public void changeKeyboardType(final int type) {
		/* ignore invalid parameter */
		if ((type != KEYBOARD_QWERTY) && (type != KEYBOARD_12KEY)) {
			return;
		}

		showPreview(NOT_A_KEY, -1);
		resetKeyRepeat();
		
		/* change keyboard view */
		final MyHeightKeyboard kbd = getTypeChangeKeyboard(type);
		if (kbd != null) {
			m12keyTable[mCurrentKeyMode] = type;
			changeKeyboard(kbd);
		}

		setStatusIcon();

		/* notice that the keyboard is changed */
		mWnn.onEvent(new NicoWnnGEvent(NicoWnnGEvent.CHANGE_MODE,
				NicoWnnGEvent.Mode.DEFAULT));
	}

	/**
	 * Change the keyboard.
	 *
	 * @param keyboard  The new keyboard
	 * @return          {@code true} if the keyboard is changed; {@code false} if not changed.
	 */
	protected boolean changeKeyboard(final MyHeightKeyboard keyboard) {
		resetKeyRepeat();

		if (true == isHideSoftKeyboardByHardKeyboard()) {
			mCurrentKeyboard = keyboard;
			return false;	// not change soft keyboard
		}

		if (keyboard == null) {
			return false;
		}

		showPreview(NOT_A_KEY, -1);

		if (mCurrentKeyboard != keyboard) {
			keyboard.setShiftKeyIndicator((mShiftOn == 0) ? false : true);
			mKeyboardView.setKeyboard(keyboard);
			mKeyboardView.setShifted((mShiftOn == 0) ? false : true);
			mCurrentKeyboard = keyboard;
			mKeyboardView.setPadding(0, 0, 0, (mSpaceBelowKeyboard ? mKeyboardPaddingTable[mInputViewHeightIndex] : 0));
			initShowPreview();
			return true;
		} else {
			mKeyboardView.setShifted((mShiftOn == 0) ? false : true);
			return false;
		}
	}

	/*
	 *
	 */
	public void changeKeyMode(final int keyMode) {
	}

	/** @see net.gorry.android.input.nicownng.InputViewManager#initView */
	@Override
	public View initView(final NicoWnnG parent, final int width, final int height) {
		if (mWnn != parent) {
			Log.w("NicoWnnG", "initView(): mWnn != parent");
			mWnn = parent;
		}
		mDisplayMode =
			(mWnn.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
			? LANDSCAPE : PORTRAIT;
		mWnn.setOrientPrefKeyMode(mDisplayMode == PORTRAIT);
		// get hardkeyboard status
		final int hardkeyState = mWnn.getResources().getConfiguration().keyboard;
		final boolean hardkey  = (hardkeyState >= Configuration.KEYBOARD_QWERTY);
		final int hiddenState = mWnn.getResources().getConfiguration().hardKeyboardHidden;
		final boolean hidden  = (hiddenState == Configuration.HARDKEYBOARDHIDDEN_YES);
		setHardKeyboardHidden(hidden, hardkey);

		final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mWnn);
		loadOption(pref);

		/*
		 * create keyboards & the view.
		 * To re-display the input view when the display mode is changed portrait <-> landscape,
		 * create keyboards every time.
		 */
		createKeyboards();

		final String skin = pref.getString("keyboard_skin", mWnn.getResources().getString(R.string.keyboard_skin_id_default));
		final int id = mWnn.getResources().getIdentifier(skin, "layout", NicoWnnG.PACKAGE_NAME);

		mKeyboardViewLayoutId = id;
		mKeyboardView = (MyKeyboardView) mWnn.getLayoutInflater().inflate(id, null);
		mKeyboardView.setOnKeyboardActionListener(this);
		mKeyboardView.setOnTouchListener(this);
		mKeyboardView.setDefaultKeyboardView(this);

		mMainView = (ViewGroup) mWnn.getLayoutInflater().inflate(R.layout.keyboard_default_main, null);
		mSubView = (ViewGroup) mWnn.getLayoutInflater().inflate(R.layout.keyboard_default_sub, null);

		if (PORTRAIT == mDisplayMode) {
			mKeyboardView.setPadding(0, 0, 0, 12);
		}
		else{
			mKeyboardView.setPadding(0, 0, 0, 8);
		}
		// if (mDisplayMode == LANDSCAPE && true == mHardKeyboardHidden) {
		//     mMainView.addView(mSubView);
		// }
		if (mKeyboardView != null) {
			mMainView.addView(mKeyboardView);
		}
		// entry gesture detector
		mDetector = new GestureDetector(this);
		mDetector.setIsLongpressEnabled(false);
		if (NICOFLICK_1STROKE == mFlickNicoInput) {
			/// mDetector.setIsLongpressEnabled(true);
			mIsActiveLongPress = true;
		}
		else{
			// mDetector.setIsLongpressEnabled(false);
			/// mDetector.setIsLongpressEnabled(true);
			mIsActiveLongPress = false;
		}
		mMetrics.setToDefaults();
		//changeKeyboardKeyHeight();
		return mMainView;
	}

	/**
	 * Update the SHIFT/ALT keys indicator.
	 *
	 * @param mode  The state of SHIFT/ALT keys.
	 */
	public void updateIndicator(final int mode) {
		/*
		final Resources res = mWnn.getResources();
		final TextView text1 = (TextView)mSubView.findViewById(R.id.shift);
		final TextView text2 = (TextView)mSubView.findViewById(R.id.alt);

		switch (mode) {
			case HARD_KEYMODE_SHIFT_OFF_ALT_OFF:
				text1.setTextColor(res.getColor(R.color.indicator_textcolor_caps_off));
				text2.setTextColor(res.getColor(R.color.indicator_textcolor_alt_off));
				text1.setBackgroundColor(res.getColor(R.color.indicator_textbackground_default));
				text2.setBackgroundColor(res.getColor(R.color.indicator_textbackground_default));
				break;
			case HARD_KEYMODE_SHIFT_ON_ALT_OFF:
				text1.setTextColor(res.getColor(R.color.indicator_textcolor_caps_on));
				text2.setTextColor(res.getColor(R.color.indicator_textcolor_alt_off));
				text1.setBackgroundColor(res.getColor(R.color.indicator_textbackground_default));
				text2.setBackgroundColor(res.getColor(R.color.indicator_textbackground_default));
				break;
			case HARD_KEYMODE_SHIFT_LOCK_ALT_OFF:
				text1.setTextColor(res.getColor(R.color.indicator_textcolor_caps_lock));
				text2.setTextColor(res.getColor(R.color.indicator_textcolor_alt_off));
				text1.setBackgroundColor(res.getColor(R.color.indicator_background_lock_caps));
				text2.setBackgroundColor(res.getColor(R.color.indicator_textbackground_default));
				break;
			case HARD_KEYMODE_SHIFT_OFF_ALT_ON:
				text1.setTextColor(res.getColor(R.color.indicator_textcolor_caps_off));
				text2.setTextColor(res.getColor(R.color.indicator_textcolor_alt_on));
				text1.setBackgroundColor(res.getColor(R.color.indicator_textbackground_default));
				text2.setBackgroundColor(res.getColor(R.color.indicator_textbackground_default));
				break;
			case HARD_KEYMODE_SHIFT_OFF_ALT_LOCK:
				text1.setTextColor(res.getColor(R.color.indicator_textcolor_caps_off));
				text2.setTextColor(res.getColor(R.color.indicator_textcolor_alt_lock));
				text1.setBackgroundColor(res.getColor(R.color.indicator_textbackground_default));
				text2.setBackgroundColor(res.getColor(R.color.indicator_background_lock_alt));
				break;
			case HARD_KEYMODE_SHIFT_ON_ALT_ON:
				text1.setTextColor(res.getColor(R.color.indicator_textcolor_caps_on));
				text2.setTextColor(res.getColor(R.color.indicator_textcolor_alt_on));
				text1.setBackgroundColor(res.getColor(R.color.indicator_textbackground_default));
				text2.setBackgroundColor(res.getColor(R.color.indicator_textbackground_default));
				break;
			case HARD_KEYMODE_SHIFT_ON_ALT_LOCK:
				text1.setTextColor(res.getColor(R.color.indicator_textcolor_caps_on));
				text2.setTextColor(res.getColor(R.color.indicator_textcolor_alt_lock));
				text1.setBackgroundColor(res.getColor(R.color.indicator_textbackground_default));
				text2.setBackgroundColor(res.getColor(R.color.indicator_background_lock_alt));
				break;
			case HARD_KEYMODE_SHIFT_LOCK_ALT_ON:
				text1.setTextColor(res.getColor(R.color.indicator_textcolor_caps_lock));
				text2.setTextColor(res.getColor(R.color.indicator_textcolor_alt_on));
				text1.setBackgroundColor(res.getColor(R.color.indicator_background_lock_caps));
				text2.setBackgroundColor(res.getColor(R.color.indicator_textbackground_default));
				break;
			case HARD_KEYMODE_SHIFT_LOCK_ALT_LOCK:
				text1.setTextColor(res.getColor(R.color.indicator_textcolor_caps_lock));
				text2.setTextColor(res.getColor(R.color.indicator_textcolor_alt_lock));
				text1.setBackgroundColor(res.getColor(R.color.indicator_background_lock_caps));
				text2.setBackgroundColor(res.getColor(R.color.indicator_background_lock_alt));
				break;
			default:
				text1.setTextColor(res.getColor(R.color.indicator_textcolor_caps_off));
				text2.setTextColor(res.getColor(R.color.indicator_textcolor_alt_off));
				text1.setBackgroundColor(res.getColor(R.color.indicator_textbackground_default));
				text2.setBackgroundColor(res.getColor(R.color.indicator_textbackground_default));
				break;
		}
		*/
		if (mCurrentKeyboard == null) {
			return;
		}
		switch (mode) {
		case HARD_KEYMODE_SHIFT_OFF_ALT_OFF:
		case HARD_KEYMODE_SHIFT_OFF_ALT_ON:
		case HARD_KEYMODE_SHIFT_OFF_ALT_LOCK:
			mCurrentKeyboard.setShiftKeyIconLock(false);
			break;
		case HARD_KEYMODE_SHIFT_ON_ALT_OFF:
		case HARD_KEYMODE_SHIFT_ON_ALT_ON:
		case HARD_KEYMODE_SHIFT_ON_ALT_LOCK:
			mCurrentKeyboard.setShiftKeyIconLock(false);
			break;
		case HARD_KEYMODE_SHIFT_LOCK_ALT_OFF:
		case HARD_KEYMODE_SHIFT_LOCK_ALT_ON:
		case HARD_KEYMODE_SHIFT_LOCK_ALT_LOCK:
			mCurrentKeyboard.setShiftKeyIconLock(true);
			break;
		default:
			mCurrentKeyboard.setShiftKeyIconLock(false);
			break;
		}
		return;
	}

	/** @see net.gorry.android.input.nicownng.InputViewManager#getCurrentView */
	@Override
	public View getCurrentView() {
		if (mCurrentKeyboard == null) {
			return null;
		}
		return mMainView;
	}

	/** @see net.gorry.android.input.nicownng.InputViewManager#onUpdateState */
	@Override
	public void onUpdateState(final NicoWnnG parent) {
		try {
			if (parent.mComposingText.size(1) == 0) {
				if (!mNoInput) {
					/* when the mode changed to "no input" */
					mNoInput = true;
					final MyHeightKeyboard newKeyboard = getKeyboardInputed(false);
					if (mCurrentKeyboard != newKeyboard) {
						changeKeyboard(newKeyboard);
					}
				}
			} else {
				if (mNoInput) {
					/* when the mode changed to "input some characters" */
					mNoInput = false;
					final MyHeightKeyboard newKeyboard = getKeyboardInputed(true);
					if (mCurrentKeyboard != newKeyboard) {
						changeKeyboard(newKeyboard);
					}
				}
			}
		} catch (final Exception ex) {
		}
	}

	/** @see net.gorry.android.input.nicownng.InputViewManager#setPreferences */
	@Override
	public void setPreferences(final SharedPreferences pref, final EditorInfo editor) {

		/* vibrator */
		try {
			if (pref.getBoolean("key_vibration", false)) {
				mVibrator = (Vibrator)mWnn.getSystemService(Context.VIBRATOR_SERVICE);
			} else {
				mVibrator = null;
			}
		} catch (final Exception ex) {
			Log.d("NicoWnnG", "NO VIBRATOR");
		}

		/* sound */
		try {
			if (mSoundPool != null) {
				mSoundPool.release();
				mSoundPool = null;
				mSound = 0;
			}
			if (pref.getBoolean("key_sound", false)) {
				mSoundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
				try {
					String path = mWnn.getExternalFilesDir(null).toString();
					path += "/nicoWnnG/type.wav";
					mSound = mSoundPool.load(path, 1);
				} catch (final Exception e) {
					//
				}
				if (mSound == 0) {
					try {
						String path = mWnn.getExternalFilesDir(null).toString();
						path += "/nicoWnnG/type.ogg";
						mSound = mSoundPool.load(path, 1);
					} catch (final Exception e) {
						//
					}
				}
				if (mSound == 0) {
					mSound = mSoundPool.load(mWnn, R.raw.type, 1);
				}
				final int vol = Integer.valueOf(pref.getString("key_sound_vol", "0"));
				switch (vol) {
					default:
					case 0:
						mSoundVolume = 1.0F;
						break;
					case 1:
						mSoundVolume = 0.5F;
						break;
					case 2:
						mSoundVolume = 0.25F;
						break;
				}
			}
		} catch (final Exception ex) {
			Log.d("NicoWnnG", "NO SOUND");
		}
		mFlickNicoInput = Integer.valueOf(mWnn.getOrientPrefString(pref, "nicoflick_mode", "0"));
		mFlickSensitivity = Integer.valueOf(mWnn.getOrientPrefString(pref, "flick_sensitivity_mode", "2"));
		mFlickGuide = mWnn.getOrientPrefBoolean(pref, "flick_guide", true);
		if (null != mDetector) {
			if (NICOFLICK_1STROKE == mFlickNicoInput) {
				/// mDetector.setIsLongpressEnabled(true);
				mIsActiveLongPress = true;
			}
			else{
				/// mDetector.setIsLongpressEnabled(true);
				// mDetector.setIsLongpressEnabled(false);
				mIsActiveLongPress = false;
			}
		}
		/* pop-up preview */
		mKeyboardView.setPreviewEnabled(false);
		mKeyboardView.setPreferences(pref, editor);

	}

	
	/**
	 * Get the shift key state from the editor.
	 * <br>
	 * @param editor    The editor information
	 * @return          The state id of the shift key (0:off, 1:on)
	 */
	protected int getShiftKeyState(final EditorInfo editor) {
		final InputConnection connection = mWnn.getCurrentInputConnection();
		if (connection != null) {
			final int caps = connection.getCursorCapsMode(editor.inputType);
			return (caps == 0) ? 0 : 1;
		} else {
			return 0;
		}
	}

	/**
	 * Set the shift key state from {@link EditorInfo}.
	 */
	protected void setShiftByEditorInfo() {
		if (mEnableAutoCaps && (mCurrentKeyMode == KEYMODE_JA_HALF_ALPHABET)) {
			final int shift = getShiftKeyState(mWnn.getCurrentInputEditorInfo());

			mShiftOn = shift;
			changeKeyboard(getShiftChangeKeyboard(shift));
		}
	}

	/** @see net.gorry.android.input.nicownng.InputViewManager#closing */
	@Override
	public void closing() {
		if (mKeyboardView != null) {
			dismissMyPopupInputImeMode();
			mKeyboardView.closing();
		}
		// mDisableKeyInput = true;
		resetKeyRepeat();
		showPreview(NOT_A_KEY, -1);
	}

	public String[] convertFlickToKeyString(int flickdir) {
		return null;
	}

	/***********************************************************************
	 * onKeyboardActionListener
	 ***********************************************************************/
	
	private int mKeyIndex = NOT_A_KEY;

	/** @see android.inputmethodservice.KeyboardView.OnKeyboardActionListener#onKey */
	@Override
	public void onKey(final int primaryCode, final int[] keyCodes) {
	}

	/** @see android.inputmethodservice.KeyboardView.OnKeyboardActionListener#swipeRight */
	@Override
	public void swipeRight() {
	}
	/** @see android.inputmethodservice.KeyboardView.OnKeyboardActionListener#swipeLeft */
	@Override
	public void swipeLeft() {
	}
	/** @see android.inputmethodservice.KeyboardView.OnKeyboardActionListener#swipeDown */
	@Override
	public void swipeDown() {
	}
	/** @see android.inputmethodservice.KeyboardView.OnKeyboardActionListener#swipeUp */
	@Override
	public void swipeUp() {
	}
	/** @see android.inputmethodservice.KeyboardView.OnKeyboardActionListener#onRelease */
	@Override
	public void onRelease(final int primaryCode) {
		mKeyRepeatReleased = true;
		mPrevInputKeyDir = 0;
		switch (mPrevInputKeyCode) {
			case KEYCODE_JP12_RIGHT:
			case KEYCODE_JP12_LEFT:
			case KEYCODE_JP12_UP:
			case KEYCODE_JP12_DOWN:
			case KEYCODE_ARROW_STOP:
			case KEYCODE_JP12_TOGGLE_MODE:
			case KEYCODE_QWERTY_TOGGLE_MODE:
			case KEYCODE_JP12_TOGGLE_MODE2:
			case KEYCODE_QWERTY_TOGGLE_MODE2:
			mPrevInputKeyCode = 0;
			break;
		}
		resetKeyRepeat();
		showPreview(NOT_A_KEY, -1);
	}

	/** @see android.inputmethodservice.KeyboardView.OnKeyboardActionListener#onPress */
	@Override
	public void onPress(final int primaryCode) {
		mKeyRepeatReleased = false;
		mPressedKeyCode = primaryCode;
		doClickFeedback(0);
		// check flick
		if (false == mNicoFirst) {
			mPrevInputKeyCode = primaryCode;
		}
		switch (primaryCode) {
		case KEYCODE_JP12_UP:
		case KEYCODE_JP12_DOWN:
		case KEYCODE_JP12_LEFT:
		case KEYCODE_JP12_RIGHT:
		case KEYCODE_ARROW_STOP:
		case KEYCODE_JP12_BACKSPACE:
		case KEYCODE_QWERTY_BACKSPACE:
		case KEYCODE_JP12_EMOJI:
		case KEYCODE_QWERTY_EMOJI:
		case KEYCODE_EISU_KANA:
		case KEYCODE_QWERTY_SHIFT:
		case KEYCODE_JP12_TOGGLE_MODE2:
		case KEYCODE_QWERTY_TOGGLE_MODE2:
			resetKeyRepeat();
			setKeyRepeat(primaryCode);
			break;
		}
		showPreview(mKeyIndex, -1);
	}

	static public void doClickFeedback(int type) {
		/* key click sound & vibration */
		if (mSoundPool != null) {
			try {
				if (mSoundStreamId != 0) {
					mSoundPool.stop(mSoundStreamId);
					mSoundStreamId = 0;
				}
				mSoundStreamId = mSoundPool.play(mSound, mSoundVolume, mSoundVolume, 0, 0, 1);
			}
			catch (final Exception ex) {
				//
			}
		}
		if (mVibrator != null) {
			try { mVibrator.vibrate(30); } catch (final Exception ex) { }
		}
	}
	
	/** @see android.inputmethodservice.KeyboardView.OnKeyboardActionListener#onText */
	@Override
	public void onText(final CharSequence text) {}

	/**
	 * Get current key mode.
	 *
	 * @return Current key mode
	 */
	public int getKeyMode() {
		return mCurrentKeyMode;
	}

	/**
	 * Get current keyboard type.
	 *
	 * @return Current keyboard type
	 */
	public int getKeyboardType() {
		return m12keyTable[mCurrentKeyMode];
	}

	/**
	 * Set the H/W keyboard's state.
	 *
	 * @param hidden {@code true} if hidden.
	 */
	public void setHardKeyboardHidden(final boolean hidden, final boolean hardkey) {
		mHardKeyboardHidden = hidden;
		mIsHardKeyboard   = hardkey;
	}
	/**
	 *
	 */
	public boolean isHideSoftKeyboardByHardKeyboard() {
/*
		if (mDisplayMode == DefaultSoftKeyboard.PORTRAIT) {
			return false;
		}
*/
		if (1 == mHiddenSoftKeyboard) {
			return true;
		}
		if (2 == mHiddenSoftKeyboard) {
			return false;
		}
		if (3 == mHiddenSoftKeyboard) {
			return false;
		}

		if (false == mIsHardKeyboard) {
			return false;
		}
		if (true == mHardKeyboardHidden) {
			return false;
		}
		return true;
	}

	/**
	 *
	 */
	public boolean isMinimizeSoftKeyboardByHardKeyboard() {
/*
		if (mDisplayMode == DefaultSoftKeyboard.PORTRAIT) {
			return false;
		}
*/
		if (3 != mHiddenSoftKeyboard) {
			return false;
		}

		if (false == mIsHardKeyboard) {
			return false;
		}
		if (true == mHardKeyboardHidden) {
			return false;
		}
		return true;
	}

	/**
	 *
	 */
	public boolean isEnabledHardKeyboard() {
		if (false == mIsHardKeyboard) {
			return false;
		}
		if (true == mHardKeyboardHidden) {
			return false;
		}
		return true;
	}

	/**
	 * Get current keyboard view.
	 */
	public View getKeyboardView() {
		return mKeyboardView;
	}

	/**
	 * reset kicotouch keyboard
	 */
	public void resetNicoKeyboard() {
		// no operation
	}

	/**
	 * get mHiddenSoftKeyboard
	 */
	public int getHiddenSoftKeyboard() {
		return mHiddenSoftKeyboard;
	}

	/**
	 * Reset the current keyboard
	 */
	public void resetCurrentKeyboard() {
		closing();
		final MyHeightKeyboard keyboard = mCurrentKeyboard;
		mCurrentKeyboard = null;
		changeKeyboard(keyboard);
	}
	/**
	 * Change to the next input mode
	 */
	public void nextKeyMode() {
	}
	/**
	 * set default Keyboard
	 */
	public void setDefaultKeyboard() {
		changeKeyMode(KEYMODE_JA_FULL_HIRAGANA);
	}
	/**
	 * get table index
	 */
	public int getTableIndex(final int keyCode) {
		return keyCode;
	}
	
	public void fadePreview() {
		if (mKeyboardView != null) {
			mKeyboardView.fadePreview();
		}
	}

	public static final int NOT_A_KEY = MyKeyboardView.NOT_A_KEY;
	public void showPreview(int keyIndex, int flingDir) {
		if (mKeyboardView != null) {
			mKeyboardView.showPreview(keyIndex, flingDir);
		}
	}
	
	public void initShowPreview() {
		if (mKeyboardView != null) {
			mKeyboardView.initShowPreview();
		}
	}
	
	/******************************************************************************************/
	/******************************************************************************************/
	/***
	 * gesture control
	 */
	@Override
	public boolean onTouch(final View v, final MotionEvent event) {
		int touchX = (int) event.getX() - mKeyboardView.getPaddingLeft();
		int touchY = (int) event.getY() - mKeyboardView.getPaddingTop();
		final int offset = mKeyboardView.getVerticalCorrection();
		touchY += offset;
		mKeyIndex = mKeyboardView.getKeyIndices(touchX, touchY, null);
		mDetector.onTouchEvent(event);
		return false;
	}
	/**
	 * OnGestureListener functions
	 */
	@Override
	public boolean onDown(final MotionEvent ev){
		//Log.d("NicoWnnG", "onDown");
		mStartX = ev.getRawX();
		mStartY = ev.getRawY();
		mGestureX = mStartX;
		mGestureY = mStartY;
		mIsLongPress = false;
		mKeyboardView.resetKeyboardFlingDir();
		//Log.d("NicoWnnG", "mFirstKey clear");
		return true;
	}
	@Override
	public boolean onFling(final MotionEvent ev1, final MotionEvent ev2, final float getX, final float getY){
		//Log.d("NicoWnnG", "onFling: x="+getX+", y="+getY);
/*
		final int flingdir = checkFlickKeyCode(true, true);
		if (mLastFlingDir != flingdir) {
			final int flingkeycode = checkFlickKeyCode(true, false);
			showPreview(mKeyIndex, flingdir);
		}
*/
		return true;
	}
	
	@Override
	public void onLongPress(final MotionEvent ev){
		/*
		if (mFirstKey != null) {
			switch (mFirstKey.codes[0]) {
			case KEYCODE_JP12_RIGHT:
			case KEYCODE_JP12_LEFT:
			case KEYCODE_JP12_UP:
			case KEYCODE_JP12_DOWN:
			case KEYCODE_ARROW_STOP:
				return;
			}
		}
		// showPreview(NOT_A_KEY, -1);
		mIsLongPress = true;
		*/
		/*
		switch (mPressedKeyCode) {
			case KEYCODE_JP12_EMOJI:
			case KEYCODE_QWERTY_EMOJI:
			case KEYCODE_EISU_KANA:
				final String str = mWnn.getComposingText(ComposingText.LAYER2);
				mWnn.onEvent(new NicoWnnGEvent(NicoWnnGEvent.INPUT_KEY, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK)));
				mWnn.invokeMushroom(str);
				break;
		}
		*/
	}
	@Override
	public boolean onScroll(final MotionEvent ev1, final MotionEvent ev2, final float distX, final float distY){
		mGestureX = ev2.getRawX();
		mGestureY = ev2.getRawY();
		// Log.d("NicoWnnG", "onScroll: x="+mGestureX+", y="+mGestureY);
		final int flingdir = checkFlickKeyCode(true, true);
		if (mKeyboardView.getLastFlingDir() != flingdir) {
			showPreview(mKeyIndex, flingdir);
		} else if (flingdir == -1) {
			if (mKeyboardView.isPreviewPopupShown()) {
				showPreview(mKeyIndex, flingdir);
			}
		}

		return true;
	}
	@Override
	public void    onShowPress(final MotionEvent ev){
		return;
	}
	@Override
	public boolean onSingleTapUp(final MotionEvent ev){
		showPreview(NOT_A_KEY, -1);
		return true;
	}
	/**
	 * calc. flick keycode
	 */
	public int checkFlickKeyCode(final boolean enablesharp, final boolean test) {
		boolean isArrowKey = false;
		boolean isModeKey = false;

		switch (mPrevInputKeyCode) {
			case KEYCODE_JP12_RIGHT:
			case KEYCODE_JP12_LEFT:
			case KEYCODE_JP12_UP:
			case KEYCODE_JP12_DOWN:
			case KEYCODE_ARROW_STOP:
			case KEYCODE_JP12_TOGGLE_MODE:
			case KEYCODE_QWERTY_TOGGLE_MODE:
			case KEYCODE_JP12_TOGGLE_MODE2:
			case KEYCODE_QWERTY_TOGGLE_MODE2:
				break;

			default:
				if (NICOFLICK_NONE == mFlickNicoInput) {
					return -1;
				}
				if (!(
						(mCurrentKeyMode == KEYMODE_JA_FULL_NICO) ||
						(mCurrentKeyMode == KEYMODE_JA_FULL_NICO_KATAKANA) ||
						(mCurrentKeyMode == KEYMODE_JA_HALF_NICO_KATAKANA) ||
						(mCurrentKeyMode == KEYMODE_JA_HALF_ALPHABET) ||
						(mCurrentKeyMode == KEYMODE_JA_FULL_ALPHABET) ||
						(mCurrentKeyMode == KEYMODE_JA_HALF_KATAKANA) ||
						(mCurrentKeyMode == KEYMODE_JA_FULL_HIRAGANA) ||
						(mCurrentKeyMode == KEYMODE_JA_FULL_KATAKANA)
						)) {
					return -1;
				}
		}
		if (true == mNicoFirst) {
			return -1;
		}
		// check 12key
		switch (mPrevInputKeyCode) {
			case KEYCODE_JP12_TOGGLE_MODE:
			case KEYCODE_JP12_TOGGLE_MODE2:
			case KEYCODE_QWERTY_TOGGLE_MODE:
			case KEYCODE_QWERTY_TOGGLE_MODE2:
				isModeKey = true;
				break;
			case KEYCODE_JP12_RIGHT:
			case KEYCODE_JP12_LEFT:
			case KEYCODE_JP12_UP:
			case KEYCODE_JP12_DOWN:
			case KEYCODE_ARROW_STOP:
				isArrowKey = true;
				break;
			case KEYCODE_JP12_1:	// A
			case KEYCODE_JP12_2:	// Ka
			case KEYCODE_JP12_3:	// Sa
			case KEYCODE_JP12_4:	// Ta
			case KEYCODE_JP12_5:	// Na
			case KEYCODE_JP12_6:	// Ha
			case KEYCODE_JP12_7:	// Ma
			case KEYCODE_JP12_8:	// Ya
			case KEYCODE_JP12_9:	// Ra
			case KEYCODE_JP12_0:	// Wa
			case KEYCODE_JP12_ASTER:	// ASTER
				break;
			case KEYCODE_JP12_SHARP:
				if (false == enablesharp) {
					return -1;
				}
				break;
			default:
				return -1;
		}
		/**
		 * creation flick keys
		 */
		float calcx, calcy;
		//calcx = (mGestureX - mStartX) * mMetrics.scaledDensity;
		//calcy = (mGestureY - mStartY) * mMetrics.scaledDensity;
		calcx = (mGestureX - mStartX);
		calcy = (mGestureY - mStartY);
		//Log.d("NicoWnnG", "checkFlick " + calcx + "/" + calcy);

		final float length = (float)Math.sqrt((calcx * calcx) + (calcy * calcy));
		double rotation;
		rotation = Math.atan2(calcy, calcx);
		// change radian -> degree
		float getrot = (float)(rotation / Math.PI * 180.0);
		if (getrot < 0.0f) {
			getrot = 360.0f + getrot;
		}
		// change rotate -> keydir
		int keydir = -1;
		int keycode = -1;
		if (length < flickSensitivityModeTable[mFlickSensitivity]) {
			if (mIsActiveLongPress) {
				// if (!mIsLongPress) {
					keydir = 0;
				// }
			}
		} else {
			if ((getrot >= 45.0f) && (getrot < 135.0f)) {
				keydir = 1;
			}
			else if ((getrot >= 135.0f) && (getrot < 225.0f)) {
				keydir = 2;
			}
			else if ((getrot >= 225.0f) && (getrot < 315.0f)) {
				keydir = 3;
			}
			else {
				keydir = 4;
			}
			if (!mCanFlickModeKey && isModeKey) {
				keydir = -1;
			}
			if (!mCanFlickArrowKey && isArrowKey) {
				keydir = -1;
			}
		}
		if (test) {
			return keydir;
		}
		if (-1 != keydir) {
			if (isModeKey) {
				switch (keydir) {
					default:
					case 0:
					case 4:  // right
						keycode = mPrevInputKeyCode;
						break;
					case 1:  // down
					case 2:  // left
					case 3:  // up
						switch (mPrevInputKeyCode) {
							default:
							case KEYCODE_JP12_TOGGLE_MODE2:
								keycode = KEYCODE_JP12_TOGGLE_MODE_BACK;
								break;
							case KEYCODE_QWERTY_TOGGLE_MODE2:
								keycode = KEYCODE_QWERTY_TOGGLE_MODE_BACK;
								break;
						}
						break;
				}
			} else if (isArrowKey) {
				switch (keydir) {
					default:
					case 0:
						keycode = mPrevInputKeyCode;
						break;
					case 1:
						keycode = KEYCODE_JP12_DOWN;
						break;
					case 2:
						keycode = KEYCODE_JP12_LEFT;
						break;
					case 3:
						keycode = KEYCODE_JP12_UP;
						break;
					case 4:
						keycode = KEYCODE_JP12_RIGHT;
						break;
				}
				if (mPrevInputKeyCode != keycode) {
					resetKeyRepeat();
					setKeyRepeat(keycode);
					mPrevInputKeyCode = keycode;
				}
			} else if (mPrevInputKeyCode == KEYCODE_JP12_ASTER) {
				switch (keydir) {
					default:
					case 0:
						keycode = mPrevInputKeyCode;
						break;
					case 1:
						keycode = mPrevInputKeyCode;
						break;
					case 2:
						keycode = KEYCODE_DAKUTEN;
						break;
					case 3:
						keycode = KEYCODE_KANASMALL;
						break;
					case 4:
						keycode = KEYCODE_HANDAKUTEN;
						break;
				}
			} else {
				keycode = convertModeFlick(getTableIndex(mPrevInputKeyCode), keydir);
			}
		} else {
			keycode = mPrevInputKeyCode;
			if (isArrowKey) {
				if (mPrevInputKeyDir > 0) {
					keycode = KEYCODE_ARROW_STOP;
					resetKeyRepeat();
					setKeyRepeat(keycode);
					mPrevInputKeyCode = keycode;
				}
			}
		}
		mPrevInputKeyDir = keydir;
		return keycode;
	}
	/**
	 *
	 */
	public int convertModeFlick(final int prev, final int key) {
		return key;
	}
	/**
	 *
	 */
	public boolean isLongPress() {
		return mIsLongPress;
	}

	public void setStatusIcon() {
		//
	}
	
	public boolean isJisFullHiraganaMode() {
		switch (mQwertyKanaMode) {
			case KANAMODE_JIS:
			case KANAMODE_JIS2:
				if (mCurrentKeyMode == KEYMODE_JA_FULL_HIRAGANA) {
					return true;
				}
				if (mCurrentKeyMode == KEYMODE_JA_FULL_NICO) {
					return true;
				}
				break;
		}
		return false;
	}
	
	public boolean isJisFullKatakanaMode() {
		switch (mQwertyKanaMode) {
			case KANAMODE_JIS:
			case KANAMODE_JIS2:
				if (mCurrentKeyMode == KEYMODE_JA_FULL_KATAKANA) {
					return true;
				}
				if (mCurrentKeyMode == KEYMODE_JA_FULL_KATAKANA) {
					return true;
				}
				break;
		}
		return false;
	}
	
	public boolean isJisHalfKatakanaMode() {
		switch (mQwertyKanaMode) {
			case KANAMODE_JIS:
			case KANAMODE_JIS2:
				if (mCurrentKeyMode == KEYMODE_JA_HALF_KATAKANA) {
					return true;
				}
				break;
		}
		return false;
	}
	
	public boolean isJisFullAlphabetMode() {
		return (mCurrentKeyMode == KEYMODE_JA_FULL_ALPHABET);
	}
	
	public void setReverseKey() {
		//
	}
	
	public void restoreReverseKey() {
		//
	}
	
	public int getPrevInputKeyCode() {
		return mPrevInputKeyCode;
	}
	
	public int getCutPasteActionByIme() {
		return mCutPasteActionByIme;
	}
}
/**************** end of file ****************/
