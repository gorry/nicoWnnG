/*
 * ◇
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


import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.gorry.android.input.nicownng.EN.NicoWnnGEngineEN;
import net.gorry.android.input.nicownng.JAJP.DefaultSoftKeyboardJAJP;
import net.gorry.android.input.nicownng.JAJP.DefaultSoftKeyboardToggle;
import net.gorry.android.input.nicownng.JAJP.DefaultSoftKeyboardNico;
import net.gorry.android.input.nicownng.JAJP.KanaConverter;
import net.gorry.android.input.nicownng.JAJP.NicoWnnGEngineJAJP;
import net.gorry.android.input.nicownng.JAJP.Romkan;
import net.gorry.android.input.nicownng.JAJP.RomkanFullKatakana;
import net.gorry.android.input.nicownng.JAJP.RomkanHalfKatakana;
import net.gorry.android.input.nicownng.JAJP.SetupKeyboard2Touch;
import net.gorry.android.input.nicownng.JAJP.SetupKeyboardBell;
import net.gorry.android.input.nicownng.JAJP.SetupKeyboardNico;
import net.gorry.android.input.nicownng.JAJP.SetupKeyboardNico2;
import net.gorry.android.input.nicownng.JAJP.TutorialJAJP;
import net.gorry.android.input.nicownng.MyKeyboardView;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.MetaKeyKeyListener;
import android.text.style.BackgroundColorSpan;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

/**
 * The OpenWnn Japanese IME class
 *
 * @author Copyright (C) 2009 OMRON SOFTWARE CO., LTD.  All Rights Reserved.
 */
public class NicoWnnGJAJP extends NicoWnnG {
	/**
	 * Mode of the convert engine (Full-width KATAKANA).
	 * Use with {@code OpenWnn.CHANGE_MODE} event.
	 */
	public static final int ENGINE_MODE_FULL_KATAKANA = 101;

	/**
	 * Mode of the convert engine (Half-width KATAKANA).
	 * Use with {@code OpenWnn.CHANGE_MODE} event.
	 */
	public static final int ENGINE_MODE_HALF_KATAKANA = 102;

	/**
	 * Mode of the convert engine (EISU-KANA conversion).
	 * Use with {@code OpenWnn.CHANGE_MODE} event.
	 */
	public static final int ENGINE_MODE_EISU_KANA = 103;

	/**
	 * Mode of the convert engine (Symbol list).
	 * Use with {@code OpenWnn.CHANGE_MODE} event.
	 */
	public static final int ENGINE_MODE_SYMBOL = 104;
	public static final int ENGINE_MODE_DOCOMOSYMBOL = 107;

	public static final int ENGINE_MODE_USERSYMBOL = 1000;

	/**
	 * Mode of the convert engine (Keyboard type is QWERTY).
	 * Use with {@code OpenWnn.CHANGE_MODE} event to change ambiguous searching pattern.
	 */
	public static final int ENGINE_MODE_OPT_TYPE_QWERTY = 105;

	/**
	 * Mode of the convert engine (Keyboard type is 12-keys).
	 * Use with {@code OpenWnn.CHANGE_MODE} event to change ambiguous searching pattern.
	 */
	public static final int ENGINE_MODE_OPT_TYPE_12KEY = 106;

	/**
	 * Mode of the convert engine (Full-width KATAKANA).
	 * Use with {@code OpenWnn.CHANGE_MODE} event.
	 */
	public static final int ENGINE_MODE_FULL_KATAKANA_CONV = 108;

	/** Never move cursor in to the composing text (adapting to IMF's specification change) */
	private static final boolean FIX_CURSOR_TEXT_END = true;

	/** Highlight color style for the converted clause */
	private static final CharacterStyle SPAN_CONVERT_BGCOLOR_HL   = new BackgroundColorSpan(0xFF8888FF);
	/** Highlight color style for the selected string  */
	private static final CharacterStyle SPAN_EXACT_BGCOLOR_HL     = new BackgroundColorSpan(0xFF66CDAA);
	/** Highlight color style for EISU-KANA conversion */
	private static final CharacterStyle SPAN_EISUKANA_BGCOLOR_HL  = new BackgroundColorSpan(0xFF9FB6CD);
	/** Highlight color style for the composing text */
	private static final CharacterStyle SPAN_REMAIN_BGCOLOR_HL    = new BackgroundColorSpan(0xFFF0FFFF);
	/** Highlight color style for the composing text */
	private static final CharacterStyle SPAN_TOGGLING_HL    = new BackgroundColorSpan(0xFFC0FFC0);
	/** Highlight text color */
	private static final CharacterStyle SPAN_TEXTCOLOR  = new ForegroundColorSpan(0xFF000000);
	/** Underline style for the composing text */
	private static final CharacterStyle SPAN_UNDERLINE            = new UnderlineSpan();

	/** IME's status for {@code mStatus} input/no candidates). */
	private static final int STATUS_INIT            = 0x0000;
	/** IME's status for {@code mStatus}(input characters). */
	private static final int STATUS_INPUT           = 0x0001;
	/** IME's status for {@code mStatus}(input functional keys). */
	private static final int STATUS_INPUT_EDIT      = 0x0003;
	/** IME's status for {@code mStatus}(all candidates are displayed). */
	private static final int STATUS_CANDIDATE_FULL  = 0x0010;

	/** Alphabet-last pattern */
	private static final Pattern ENGLISH_CHARACTER_LAST = Pattern.compile(".*[a-zA-Z]$");

	/**
	 *  Private area character code got by {@link KeyEvent#getUnicodeChar()}.
	 *   (SHIFT+ALT+X G1 specific)
	 */
	private static final int PRIVATE_AREA_CODE = 61184;

	/** Maximum length of input string */
	private static final int LIMIT_INPUT_NUMBER = 30;

	/** Bit flag for English auto commit mode (ON) */
	private static final int AUTO_COMMIT_ENGLISH_ON      = 0x0000;
	/** Bit flag for English auto commit mode (OFF) */
	private static final int AUTO_COMMIT_ENGLISH_OFF     = 0x0001;
	/** Bit flag for English auto commit mode (symbol list) */
	private static final int AUTO_COMMIT_ENGLISH_SYMBOL  = 0x0010;

	/** Message for {@code mHandler} (execute prediction) */
	private static final int MSG_PREDICTION = 0;

	/** Message for {@code mHandler} (execute tutorial) */
	private static final int MSG_START_TUTORIAL = 1;

	/** Message for {@code mHandler} (close) */
	private static final int MSG_CLOSE = 2;

	/** Delay time(msec.) to start prediction after key input when the candidates view is not shown. */
	//private static final int PREDICTION_DELAY_MS_1ST = 400;
	private static final int PREDICTION_DELAY_MS_1ST_00 = 250;
	private static final int PREDICTION_DELAY_MS_1ST_01 = 150;

	/** Delay time(msec.) to start prediction after key input when the candidates view is shown. */
	//private static final int PREDICTION_DELAY_MS_SHOWING_CANDIDATE = 200;
	private static final int PREDICTION_DELAY_MS_SHOWING_CANDIDATE_00 = 150;
	private static final int PREDICTION_DELAY_MS_SHOWING_CANDIDATE_01 = 150;


	private int mPredictionDelayMS1st = PREDICTION_DELAY_MS_1ST_00;
	private int mPredictionDelayMSShowingCandidate = PREDICTION_DELAY_MS_1ST_01;

	private boolean mIndicateAfterCommit = false;

	/** Convert engine's state */
	private class EngineState {
		/** Definition for {@code EngineState.*} (invalid) */
		public static final int INVALID = -1;

		/** Definition for {@code EngineState.dictionarySet} (Japanese) */
		public static final int DICTIONARYSET_JP = 0;

		/** Definition for {@code EngineState.dictionarySet} (English) */
		public static final int DICTIONARYSET_EN = 1;

		/** Definition for {@code EngineState.convertType} (prediction/no conversion) */
		public static final int CONVERT_TYPE_NONE = 0;

		/** Definition for {@code EngineState.convertType} (consecutive clause conversion) */
		public static final int CONVERT_TYPE_RENBUN = 1;

		/** Definition for {@code EngineState.convertType} (EISU-KANA conversion) */
		public static final int CONVERT_TYPE_EISU_KANA = 2;

		/** Definition for {@code EngineState.convertType} (ALPHABET/KANA Direct conversion) */
		public static final int CONVERT_TYPE_KANA_DIRECT = 3;

		/** Definition for {@code EngineState.convertType} (consecutive clause predict conversion) */
		public static final int CONVERT_TYPE_PREDICT = 4;

		/** Definition for {@code EngineState.temporaryMode} (change back to the normal dictionary) */
		public static final int TEMPORARY_DICTIONARY_MODE_NONE = 0;

		/** Definition for {@code EngineState.temporaryMode} (change to the symbol dictionary) */
		public static final int TEMPORARY_DICTIONARY_MODE_SYMBOL = 1;
		public static final int TEMPORARY_DICTIONARY_MODE_DOCOMOSYMBOL00 = 3;
		public static final int TEMPORARY_DICTIONARY_MODE_DOCOMOSYMBOL01 = 4;

		// user symbol state
		public static final int TEMPORARY_DICTIONARY_MODE_USERSYMBOL = 1000;
		public static final int TEMPORARY_DICTIONARY_MODE_USERSYMBOL_ZEN_HIRAGANA = 1001;
		public static final int TEMPORARY_DICTIONARY_MODE_USERSYMBOL_ZEN_KATAKANA = 1002;
		public static final int TEMPORARY_DICTIONARY_MODE_USERSYMBOL_ZEN_ALPHABET = 1003;
		public static final int TEMPORARY_DICTIONARY_MODE_USERSYMBOL_ZEN_NUMBER = 1004;
		public static final int TEMPORARY_DICTIONARY_MODE_USERSYMBOL_HAN_KATAKANA = 1005;
		public static final int TEMPORARY_DICTIONARY_MODE_USERSYMBOL_HAN_ALPHABET = 1006;
		public static final int TEMPORARY_DICTIONARY_MODE_USERSYMBOL_HAN_SYMBOL = 1007;
		public static final int TEMPORARY_DICTIONARY_MODE_USERSYMBOL_END = 1008;
		public static final int TEMPORARY_DICTIONARY_MODE_USERSYMBOL_TYPES = 7; //TEMPORARY_DICTIONARY_MODE_USERSYMBOL_END - TEMPORARY_DICTIONARY_MODE_USERSYMBOL - 1;

		/** Definition for {@code EngineState.temporaryMode} (change to the user dictionary) */
		public static final int TEMPORARY_DICTIONARY_MODE_USER = 2;

		/** Definition for {@code EngineState.preferenceDictionary} (no preference dictionary) */
		public static final int PREFERENCE_DICTIONARY_NONE = 0;

		/** Definition for {@code EngineState.preferenceDictionary} (person's name) */
		public static final int PREFERENCE_DICTIONARY_PERSON_NAME = 1;

		/** Definition for {@code EngineState.preferenceDictionary} (place name) */
		public static final int PREFERENCE_DICTIONARY_POSTAL_ADDRESS = 2;

		/** Definition for {@code EngineState.preferenceDictionary} (email/URI) */
		public static final int PREFERENCE_DICTIONARY_EMAIL_ADDRESS_URI = 3;

		/** Definition for {@code EngineState.keyboard} (undefined) */
		public static final int KEYBOARD_UNDEF = 0;

		/** Definition for {@code EngineState.keyboard} (QWERTY) */
		public static final int KEYBOARD_QWERTY = 1;

		/** Definition for {@code EngineState.keyboard} (12-keys) */
		public static final int KEYBOARD_12KEY  = 2;

		/** Set of dictionaries */
		public int dictionarySet = INVALID;

		/** Type of conversion */
		public int convertType = INVALID;

		/** Temporary mode */
		public int temporaryMode = INVALID;

		/** Preference dictionary setting */
		public int preferenceDictionary = INVALID;

		/** keyboard */
		public int keyboard = INVALID;

		/**
		 * Returns whether current type of conversion is consecutive clause(RENBUNSETSU) conversion.
		 *
		 * @return {@code true} if current type of conversion is consecutive clause conversion.
		 */
		public boolean isRenbun() {
			return convertType == CONVERT_TYPE_RENBUN;
		}

		/**
		 * Returns whether current type of conversion is consecutive clause(PREDICT) conversion.
		 *
		 * @return {@code true} if current type of conversion is consecutive clause conversion.
		 */
		public boolean isPredict() {
			return convertType == CONVERT_TYPE_PREDICT;
		}

		/**
		 * Returns whether current type of conversion is EISU-KANA conversion.
		 *
		 * @return {@code true} if current type of conversion is EISU-KANA conversion.
		 */
		public boolean isEisuKana() {
			return convertType == CONVERT_TYPE_EISU_KANA;
		}

		/**
		 * Returns whether current type of conversion is KANA Direct conversion.
		 *
		 * @return {@code true} if current type of conversion is KANA Direct conversion.
		 */
		public boolean isKanaDirect() {
			return convertType == CONVERT_TYPE_KANA_DIRECT;
		}

		/**
		 * Returns whether current type of conversion is no conversion.
		 *
		 * @return {@code true} if no conversion is executed currently.
		 */
		public boolean isConvertState() {
			return convertType != CONVERT_TYPE_NONE;
		}

		/**
		 * Check whether or not the mode is "symbol list".
		 *
		 * @return {@code true} if the mode is "symbol list".
		 */
		public boolean isSymbolList() {
			return (temporaryMode == TEMPORARY_DICTIONARY_MODE_SYMBOL);
			//return ((temporaryMode == TEMPORARY_DICTIONARY_MODE_SYMBOL) || (temporaryMode == TEMPORARY_DICTIONARY_MODE_DOCOMOSYMBOL));
		}
		public boolean isDocomoSymbolList() {
			return ((temporaryMode == TEMPORARY_DICTIONARY_MODE_DOCOMOSYMBOL00) || (temporaryMode == TEMPORARY_DICTIONARY_MODE_DOCOMOSYMBOL01));
		}

		/**
		 * Check whether or not the mode is "user symbol".
		 *
		 * @return {@code true} if the mode is "user symbol".
		 */
		public boolean isUserSymbol() {
			if ((temporaryMode > TEMPORARY_DICTIONARY_MODE_USERSYMBOL) && (temporaryMode < TEMPORARY_DICTIONARY_MODE_USERSYMBOL_END)) {
				return true;
			}
			return false;
		}

		/**
		 * Check whether or not the current language is English.
		 *
		 * @return {@code true} if the current language is English.
		 */
		public boolean isEnglish() {
			return dictionarySet == DICTIONARYSET_EN;
		}
	}

	/** IME's status */
	protected int mStatus = STATUS_INIT;

	/** Whether exact match searching or not */
	protected boolean mExactMatchMode = false;

	/** Spannable string builder for displaying the composing text */
	protected SpannableStringBuilder mDisplayText;

	/** Instance of this service */
	private static NicoWnnGJAJP mSelf = null;

	/** Backup for switching the converter */
	private WnnEngine mConverterBack;

	/** Backup for switching the pre-converter */
	private LetterConverter mPreConverterBack;

	/** OpenWnn conversion engine for Japanese */
	private NicoWnnGEngineJAJP mConverterJAJP;

	/** OpenWnn conversion engine for English */
	private NicoWnnGEngineEN mConverterEN;

	/** Conversion engine for listing symbols */
	private SymbolList mConverterSymbolEngineBack;

	/** Symbol lists to display when the symbol key is pressed */
	/*
	private static final String[] SYMBOL_LISTS = {
		SymbolList.SYMBOL_JAPANESE, SymbolList.SYMBOL_ENGLISH, SymbolList.SYMBOL_JAPANESE_FACE,  SymbolList.SYMBOL_JAPANESE_USER_FACE
	};
	 */

	/** Current symbol list */
	private int mCurrentSymbol = -1;

	private int[] mCurrentUserSymbol = new int[EngineState.TEMPORARY_DICTIONARY_MODE_USERSYMBOL_TYPES];
	
	/** is change normal/full */
	private int mDocomoEmojiCount = 0;

	/** prediction count **/
	private int mPredictionCount = 0;
	private int mPredictionMode  = 0;
	private boolean mEnablePredictionAfterEnter;


	private static final int kPREDICTION_ON     = 0;
	private static final int kPREDICTION_OFF    = 1;
	private static final int kPREDICTION_HENKAN = 2;
	private static final HashMap<String, Integer> predictionModeTable = new HashMap<String, Integer>() {
		private static final long serialVersionUID = 1L;
		{
			put("on_prediction", 0);
			put("off_prediction", 1);
			put("henkan_prediction", 2);
		}
	};


	/** Romaji-to-Kana converter (HIRAGANA) */
	private Romkan mPreConverterHiragana;

	/** Romaji-to-Kana converter (full-width KATAKANA) */
	private RomkanFullKatakana mPreConverterFullKatakana;

	/** Romaji-to-Kana converter (half-width KATAKANA) */
	private RomkanHalfKatakana mPreConverterHalfKatakana;

	/** Conversion Engine's state */
	private final EngineState mEngineState = new EngineState();

	/** Whether learning function is active of not. */
	private boolean mEnableLearning = true;

	/** Whether prediction is active or not. */
	//private boolean mEnablePrediction = true;
	private boolean mActionPrediction = true;

	/** Whether using the converter */
	private boolean mEnableConverter = true;

	/** Whether displaying the symbol list */
	private boolean mEnableSymbolList = true;

	/** Whether non ASCII code is enabled */
	private boolean mEnableSymbolListNonHalf = true;

	/** Enable mistyping spell correction or not */
	private boolean mEnableSpellCorrection = true;

	/** Auto commit state (in English mode) */
	private int mDisableAutoCommitEnglishMask = AUTO_COMMIT_ENGLISH_ON;

	/** Whether removing a space before a separator or not. (in English mode) */
	private boolean mEnableAutoDeleteSpace = false;

	/** Whether dismissing the keyboard when the enter key is pressed */
	private boolean mEnableAutoHideKeyboard = true;

	/** Number of committed clauses on consecutive clause conversion */
	private int mCommitCount = 0;

	/** Target layer of the {@link ComposingText} */
	private int mTargetLayer = 1;

	/** Current orientation of the display */
	private int mOrientation = Configuration.ORIENTATION_UNDEFINED;

	/** Current normal dictionary set */
	private int mPrevDictionarySet = NicoWnnGEngineJAJP.DIC_LANG_INIT;

	/** Regular expression pattern for English separators */
	private  Pattern mEnglishAutoCommitDelimiter = null;

	/** Cursor position in the composing text */
	private int mComposingStartCursor = 0;

	/** Cursor position before committing text */
	private int mCommitStartCursor = 0;

	/** Previous committed text */
	private StringBuffer mPrevCommitText = null;

	/** Call count of {@code commitText} */
	private int mPrevCommitCount = 0;

	/** Shift lock status of the Hardware keyboard */
	private int mHardShift;

	/** SHIFT key state (pressing) */
	private boolean mShiftPressing;

	/** ALT lock status of the Hardware keyboard */
	private int mHardAlt;

	/** ALT key state (pressing) */
	private boolean mAltPressing;

	/** CTRL lock status of the Hardware keyboard */
	private int mHardCtrl;

	/** CTRL key state (pressing) */
	private boolean mCtrlPressing;

	/** Shift lock toggle definition */
	private static final int[] mShiftKeyToggle = {0, MetaKeyKeyListener.META_SHIFT_ON, MetaKeyKeyListener.META_CAP_LOCKED};

	/** ALT lock toggle definition */
	private static final int[] mAltKeyToggle = {0, MetaKeyKeyListener.META_ALT_ON, MetaKeyKeyListener.META_ALT_LOCKED};

	/** Auto caps mode */
	private boolean mAutoCaps = false;

	/** List of words in the user dictionary */
	private WnnWord[] mUserDictionaryWords = null;

	/** Tutorial */
	private TutorialJAJP mTutorial;

	/** Whether tutorial mode or not */
	private boolean mEnableTutorial;

	/** Whether there is a continued predicted candidate */
	private boolean mHasContinuedPrediction = false;

	/* recreate inputview mode */
	private String mNewInputViewMode;

	/* IS01 Hard key enabler */
	private final boolean mIs01Enable = true;
	private final boolean mIs12keyEnable = true;
	private NicoWnnGEvent evHardwareKeyboardIS01MojiKey;
	private final int mTimeOutLongPressHardwareKeyboardIS01MojiKey = 500;
	private boolean mRegisterLongPressHardwareKeyboardIS01MojiKey = false;
	private final Handler mHandlerLongPressHardwareKeyboardIS01MojiKey = new Handler();
	private final Runnable mActionLongPressHardwareKeyboardIS01MojiKey = new Runnable() {
		public void run() {
			synchronized (this) {
				if (mRegisterLongPressHardwareKeyboardIS01MojiKey) {
					mHandlerLongPressHardwareKeyboardIS01MojiKey.removeCallbacks(mActionLongPressHardwareKeyboardIS01MojiKey);
					mRegisterLongPressHardwareKeyboardIS01MojiKey = false;
					
					final DefaultSoftKeyboard inputManager = ((DefaultSoftKeyboard) mInputViewManager);
					inputManager.invokeMyPopupInputImeMode();
					
					// openPreferenceSetting();
				}
			}
		}
	};

	private boolean mUseHardAltShift = true;
	
	private NicoWnnGEvent evHardwareKeyboardIS01EKaoKiKey;
	private boolean mRegisterLongPressHardwareKeyboardIS01EKaoKiKey = false;
	private final int mTimeOutLongPressHardwareKeyboardIS01EKaoKiKey = 500;
	private final Handler mHandlerLongPressHardwareKeyboardIS01EKaoKiKey = new Handler();
	private final Runnable mActionLongPressHardwareKeyboardIS01EKaoKiKey = new Runnable() {
		public void run() {
			synchronized (this) {
				if (mRegisterLongPressHardwareKeyboardIS01EKaoKiKey) {
					mHandlerLongPressHardwareKeyboardIS01EKaoKiKey.removeCallbacks(mActionLongPressHardwareKeyboardIS01EKaoKiKey);
					mRegisterLongPressHardwareKeyboardIS01EKaoKiKey = false;
					if (mSelf != null) {
						final NicoWnnGEvent mEventInputBack        = new NicoWnnGEvent(NicoWnnGEvent.INPUT_KEY, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK));
						final String str = getComposingText(ComposingText.LAYER2);
						onEvent(mEventInputBack);
						invokeMushroom(str);
					}
				}
			}
		}
	};

	private boolean mInputOrientation = false;

	/** {@code Handler} for drawing candidates/displaying tutorial */
	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(final Message msg) {
			switch (msg.what) {
				case MSG_PREDICTION:
					updatePrediction();
					break;
				case MSG_START_TUTORIAL:
					if (mTutorial == null) {
						if (isInputViewShown()) {
							final DefaultSoftKeyboardJAJP inputManager = ((DefaultSoftKeyboardJAJP) mInputViewManager);
							final View v = inputManager.getKeyboardView();
							mTutorial = new TutorialJAJP(NicoWnnGJAJP.this, v, inputManager);

							mTutorial.start();
						} else {
							/* Try again soon if the view is not yet showing */
							sendMessageDelayed(obtainMessage(MSG_START_TUTORIAL), 100);
						}
					}
					break;
				case MSG_CLOSE:
					if (mConverterJAJP != null) mConverterJAJP.close();
					if (mConverterEN != null) mConverterEN.close();
					if (mConverterSymbolEngineBack != null) mConverterSymbolEngineBack.close();
					break;
			}
		}
	};

	/** The candidate filter */
	private CandidateFilter mFilter;

	/**
	 * Constructor
	 */
	public NicoWnnGJAJP() {
		super();
		// Log.w("NicoWnnG", "NicoWnnGJAJP()");
		myConstructor2(null);
		// ここに置くとcontextが確保できていないので廃止。onCreateへ移動
		/*
		mSelf = this;

		mComposingText = new ComposingText();
		mCandidatesViewManager = new TextCandidatesViewManager(-1);
		startSoftKeyboard();
		mConverter = mConverterJAJP = new NicoWnnGEngineJAJP(NicoWnnG.writableJAJPDic);
		mConverterEN = new NicoWnnGEngineEN(NicoWnnG.writableENDic);
		mPreConverter = mPreConverterHiragana = new Romkan();
		mPreConverterFullKatakana = new RomkanFullKatakana();
		mPreConverterHalfKatakana = new RomkanHalfKatakana();
		mFilter = new CandidateFilter();

		mDisplayText = new SpannableStringBuilder();
		mAutoHideMode = false;

		mPrevCommitText = new StringBuffer();

		for (int i=0; i<mCurrentUserSymbol.length; i++) {
			mCurrentUserSymbol[i] = -1;
		}

		 */
	}

	/**
	 * Constructor
	 *
	 * @param context       The context
	 */
	public NicoWnnGJAJP(final Context context) {
		super(context);
		// Log.w("NicoWnnG", "NicoWnnGJAJP(final Context context)");
		myConstructor2(context);
	}
	
	public void myConstructor2(Context context) {
		// Log.w("NicoWnnG", "NicoWnnGJAJP#myConstructor2(Context context)");
		mSelf = this;
		// NicoWnnGJAJP()にあったのを移動
		mComposingText = new ComposingText();
		mCandidatesViewManager = new TextCandidatesViewManager(-1, this);
		startSoftKeyboard();
		if (context != null) {
			super.setContext(context);
			initConverter();
		}
		mPreConverter = mPreConverterHiragana = new Romkan();
		mPreConverterFullKatakana = new RomkanFullKatakana();
		mPreConverterHalfKatakana = new RomkanHalfKatakana();
		mFilter = new CandidateFilter();

		mDisplayText = new SpannableStringBuilder();
		mAutoHideMode = false;

		mPrevCommitText = new StringBuffer();

		for (int i=0; i<mCurrentUserSymbol.length; i++) {
			mCurrentUserSymbol[i] = -1;
		}

		
		/*
		// EN側も初期化しておく
		NicoWnnGEN.getInstance().myConstructor();
		*/
	}

	private void initConverter() {
		mConverter = mConverterJAJP = new NicoWnnGEngineJAJP(writableJAJPDic);
		mConverter.init();
		mConverterEN = new NicoWnnGEngineEN(writableENDic);
		final String delimiter = Pattern.quote(getResources().getString(R.string.en_word_separators));
		mEnglishAutoCommitDelimiter = Pattern.compile(".*[" + delimiter + "]$");
		if (mConverterSymbolEngineBack == null) {
			mConverterSymbolEngineBack = new SymbolList(this, SymbolList.LANG_JA);
		}
		if (mConverterSymbolEngineBack != null) {
			if (!mConverterSymbolEngineBack.getUserSymbolChecked()) {
				mConverterSymbolEngineBack.loadUserSymbolList();
			}
		}
	}

	/** @see net.gorry.android.input.nicownng.NicoWnnG#onCreate */
	@Override public void onCreate() {
		// Log.w("NicoWnnG", "NicoWnnGJAJP#onCreate()");
		super.onCreate();

		if (mConverterJAJP == null) {
			super.setContext(this);
			initConverter();
		}
	}

	/** @see net.gorry.android.input.nicownng.NicoWnnG#onCreateInputView */
	@Override public View onCreateInputView() {
		final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		loadOption(pref);
		final String newinputmode = getOrientPrefString(pref, "input_mode", INPUTMODE_NORMAL);
		boolean start = false;
		if (false == mInputViewMode.equals(newinputmode)) {
			mInputViewMode = newinputmode;
			start = true;
		}
		if (mInputOrientation != super.getOrientPrefKeyMode()) {
			mInputOrientation = super.getOrientPrefKeyMode();
			start = true;
		}
		if (false == mInputViewMode.equals(newinputmode)) {
			mInputViewMode = newinputmode;
			start = true;
		}
		if (start) {
			startSoftKeyboard();
		}
		final int hiddenState = getResources().getConfiguration().hardKeyboardHidden;
		final boolean hidden  = (hiddenState == Configuration.HARDKEYBOARDHIDDEN_YES);
		mEnableTutorial = hidden;
		return super.onCreateInputView();
	}

	private boolean mNoStartInputView = false;
	public void setNoStartInputView(boolean sw) {
		mNoStartInputView = sw;
	}

	private boolean mNoStartInputView2 = false;
	public void setNoStartInputView2(boolean sw) {
		mNoStartInputView2 = sw;
	}

	/** @see net.gorry.android.input.nicownng.NicoWnnG#onStartInputView */
	@Override public void onStartInputView(final EditorInfo attribute, final boolean restarting) {
		if (mNoStartInputView) {
			setNoStartInputView(false);
			return;
		}
		if (mNoStartInputView2) {
			return;
		}
		if (mInputViewManager != null) {
			final DefaultSoftKeyboard inputManager = ((DefaultSoftKeyboard) mInputViewManager);
			inputManager.dismissMyPopupInputImeMode();
		}
		
		final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);

		boolean isrestart = false;
		final String newinputmode = getOrientPrefString(pref, "input_mode", INPUTMODE_NORMAL);
		if (false == mInputViewMode.equals(newinputmode)) {
			isrestart = true;
			mInputViewMode = newinputmode;
		}
		/* get old state */
		final boolean oldstate = ((DefaultSoftKeyboard)mInputViewManager).isHideSoftKeyboardByHardKeyboard();
		/* setup new hardkeyboard state */
		final int hardkeyState = getResources().getConfiguration().keyboard;
		final boolean hardkey  = (hardkeyState >= Configuration.KEYBOARD_QWERTY);
		final int hiddenState = getResources().getConfiguration().hardKeyboardHidden;
		final boolean hidden  = (hiddenState == Configuration.HARDKEYBOARDHIDDEN_YES);
		if (null != mInputViewManager) {
			((DefaultSoftKeyboard)mInputViewManager).setHardKeyboardHidden(hidden, hardkey);
			final boolean newstate = ((DefaultSoftKeyboard)mInputViewManager).isHideSoftKeyboardByHardKeyboard();
			if (oldstate != newstate) {
				isrestart = true;
			}
			if (!isrestart) {
				isrestart = ((DefaultSoftKeyboard)mInputViewManager).checkOption(pref);
			}
		}
		/**
		 * check restart
		 */
		if (true == isrestart) {
			startSoftKeyboard();
			setInputView(super.onCreateInputView());
		}

		final EngineState state = new EngineState();
		state.temporaryMode = EngineState.TEMPORARY_DICTIONARY_MODE_NONE;
		updateEngineState(state);

		mPrevCommitCount = 0;
		clearCommitInfo();

		/* load preferences */
		/*
		if (true == pref.getBoolean("reset_symbol", false)) {
			if (mConverterSymbolEngineBack != null) {
				mConverterSymbolEngineBack.close();
				mConverterSymbolEngineBack = null;
			}
			pref.edit().putBoolean("reset_symbol", false);
			pref.edit().commit();
		}
		 */
		if (mConverterSymbolEngineBack == null) {
			mConverterSymbolEngineBack = new SymbolList(this, SymbolList.LANG_JA);
		}
		if (mConverterSymbolEngineBack != null) {
			if (!mConverterSymbolEngineBack.getUserSymbolChecked()) {
				mConverterSymbolEngineBack.loadUserSymbolList();
			}
		}

		/* start inputview */
		((DefaultSoftKeyboard) mInputViewManager).resetCurrentKeyboard();

		super.onStartInputView(attribute, restarting);

		/* initialize views */
		mCandidatesViewManager.clearCandidates();
		mDocomoEmojiCount = 0;
		resetPrediction();

		/* initialize status */
		mStatus = STATUS_INIT;
		mExactMatchMode = false;

		/* hardware keyboard support */
		mHardShift = 0;
		mHardAlt   = 0;
		mHardCtrl  = 0;
		updateMetaKeyStateDisplay();

		/* initialize the engine's state */
		fitInputType(pref, attribute);

		mCandidatesViewManager.setAutoHide(true);

		if (isEnableL2Converter()) {
			breakSequence();
		}
	}

	/** @see net.gorry.android.input.nicownng.NicoWnnG#hideWindow */
	@Override public void hideWindow() {
		mCandidatesViewManager.hideCandidateTask();	// close AsyncTask

		final DefaultSoftKeyboard inputManager = ((DefaultSoftKeyboard) mInputViewManager);
		inputManager.dismissMyPopupInputImeMode();

		mComposingText.clear();
		mInputViewManager.onUpdateState(this);
		clearCommitInfo();
		mHandler.removeMessages(MSG_START_TUTORIAL);
		mInputViewManager.closing();
		if (mTutorial != null) {
			mTutorial.close();
			mTutorial = null;
		}
		super.hideWindow();
	}
	/*
	 *
	 */
	public boolean reloadSymbol() {
		if (mConverterSymbolEngineBack != null) {
			mConverterSymbolEngineBack.close();
			mConverterSymbolEngineBack = null;
		}
		if (mConverterSymbolEngineBack == null) {
			mConverterSymbolEngineBack = new SymbolList(this, SymbolList.LANG_JA);
		}
		return mConverterSymbolEngineBack.getUserSymbolChecked();
	}
	/*
	 *
	 */
	private void resetPrediction() {
		mPredictionCount = 0;
		if (kPREDICTION_ON == mPredictionMode) {
			mActionPrediction = true;
			mPredictionDelayMS1st = PREDICTION_DELAY_MS_1ST_00;
			mPredictionDelayMSShowingCandidate = PREDICTION_DELAY_MS_SHOWING_CANDIDATE_00;
		}
		else{
			mActionPrediction = false;
			mPredictionDelayMS1st = PREDICTION_DELAY_MS_1ST_01;
			mPredictionDelayMSShowingCandidate = PREDICTION_DELAY_MS_SHOWING_CANDIDATE_01;
		}
	}
	/*
	 *
	 */
	public void startSoftKeyboard() {
		// Log.w("NicoWnnG", "NicoWnnGJAJP#startSoftKeyboard()");
		if (null != mInputViewManager) {
			mInputViewManager.closing();
		}
		if (mInputViewMode.equals(INPUTMODE_NICO)) {
			// Log.w("NicoWnnG", "NicoWnnGJAJP#startSoftKeyboard(): INPUTMODE_NICO");
			final SetupKeyboardNico keyboardnico = new SetupKeyboardNico();
			mInputViewManager = new DefaultSoftKeyboardNico(this, keyboardnico);
		}
		else if (mInputViewMode.equals(INPUTMODE_BELL)) {
			// Log.w("NicoWnnG", "NicoWnnGJAJP#startSoftKeyboard(): INPUTMODE_BELL");
			final SetupKeyboardBell keyboardbell = new SetupKeyboardBell();
			mInputViewManager = new DefaultSoftKeyboardNico(this, keyboardbell);
		}
		else if (mInputViewMode.equals(INPUTMODE_NORMAL)) {
			// Log.w("NicoWnnG", "NicoWnnGJAJP#startSoftKeyboard(): INPUTMODE_NORMAL");
			mInputViewManager = new DefaultSoftKeyboardToggle(this);
		}
		else if (mInputViewMode.equals(INPUTMODE_NICO2)) {
			// Log.w("NicoWnnG", "NicoWnnGJAJP#startSoftKeyboard(): INPUTMODE_NICO2");
			final SetupKeyboardNico2 keyboardnico2 = new SetupKeyboardNico2();
			mInputViewManager = new DefaultSoftKeyboardNico(this, keyboardnico2);
		}
		else if (mInputViewMode.equals(INPUTMODE_2TOUCH)) {
			// Log.w("NicoWnnG", "NicoWnnGJAJP#startSoftKeyboard(): INPUTMODE_2TOUCH");
			final SetupKeyboard2Touch keyboard2touch = new SetupKeyboard2Touch();
			mInputViewManager = new DefaultSoftKeyboardNico(this, keyboard2touch);
		}
		/*
			else if (mInputViewMode.equals(INPUTMODE_TEST)) {
				final SetupKeyboardNico22 keyboardnico22 = new SetupKeyboardNico22();
				mInputViewManager  = new DefaultSoftKeyboardTest(keyboardnico22);
			}
		 */
		else{
			// Log.w("NicoWnnG", "NicoWnnGJAJP#startSoftKeyboard(): INPUTMODE_TOGGLE");
			mInputViewManager = new DefaultSoftKeyboardToggle(this);
		}
		// Log.w("NicoWnnG", "NicoWnnGJAJP#startSoftKeyboard(): finish");
	}
	
	private boolean mSemaphore_onUpdateSelection = false;
	/** @see net.gorry.android.input.nicownng.NicoWnnG#onUpdateSelection */
	@Override public void onUpdateSelection(final int oldSelStart, final int oldSelEnd, final int newSelStart, final int newSelEnd, final int candidatesStart, final int candidatesEnd) {

		if (mSemaphore_onUpdateSelection) {
			return;
		}
		mSemaphore_onUpdateSelection = true;
		mComposingStartCursor = (candidatesStart < 0) ? newSelEnd : candidatesStart;

		if (newSelStart != newSelEnd) {
			clearCommitInfo();
		}

		if (mHasContinuedPrediction) {
			mHasContinuedPrediction = false;
			if (0 < mPrevCommitCount) {
				mPrevCommitCount--;
			}
			mSemaphore_onUpdateSelection = false;
			return;
		}

		final boolean isNotComposing = ((candidatesStart < 0) && (candidatesEnd < 0));
		if ((mComposingText.size(ComposingText.LAYER1) != 0)
				&& !isNotComposing) {
			updateViewStatus(mTargetLayer, false, true, mIndicateAfterCommit);  // ２文節変換の２文節めを選択ON
			mIndicateAfterCommit = false;
		} else {
			if (0 < mPrevCommitCount) {
				mPrevCommitCount--;
			} else {
				final int commitEnd = mCommitStartCursor + mPrevCommitText.length();
				if ((((newSelEnd < oldSelEnd) || (commitEnd < newSelEnd)) && clearCommitInfo())
						|| isNotComposing) {
					if (isEnableL2Converter()) {
						breakSequence();
					}

					if (mInputConnection != null) {
						if (isNotComposing && (mComposingText.size(ComposingText.LAYER1) != 0)) {
							mInputConnection.finishComposingText();
						}
					}
					initializeScreen();
				}
			}
		}
		mSemaphore_onUpdateSelection = false;
	}

	/** @see net.gorry.android.input.nicownng.NicoWnnG#onConfigurationChanged */
	@Override public void onConfigurationChanged(final Configuration newConfig) {
		try {
			super.onConfigurationChanged(newConfig);

			if (mInputConnection != null) {
				if (super.isInputViewShown()) {
					updateViewStatus(mTargetLayer, true, true, false);
				}

				/* display orientation */
				if (mOrientation != newConfig.orientation) {
					mOrientation = newConfig.orientation;
					commitConvertingText();
					initializeScreen();
				}
				/* Hardware keyboard */
				final int hardkeyState = newConfig.keyboard;
				final boolean hardkey  = (hardkeyState >= Configuration.KEYBOARD_QWERTY);
				final int hiddenState = newConfig.hardKeyboardHidden;
				final boolean hidden  = (hiddenState == Configuration.HARDKEYBOARDHIDDEN_YES);
				//((DefaultSoftKeyboard) mInputViewManager).setHardKeyboardHidden(hidden, hardkey);
				mEnableTutorial = hidden;
			}
			hideWindow();
		} catch (final Exception ex) {
			/* do nothing if an error occurs. */
		}
	}

	/** @see net.gorry.android.input.nicownng.NicoWnnG#onEvent */
	@Override synchronized public boolean onEvent(final NicoWnnGEvent ev) {

		EngineState state;

		/* handling events which are valid when InputConnection is not active. */
		switch (ev.code) {

			case NicoWnnGEvent.KEYUP:
				onKeyUpEvent(ev.keyEvent);
				return true;

			case NicoWnnGEvent.INITIALIZE_LEARNING_DICTIONARY:
				mConverterEN.initializeDictionary(WnnEngine.DICTIONARY_TYPE_LEARN);
				mConverterJAJP.initializeDictionary(WnnEngine.DICTIONARY_TYPE_LEARN);
				return true;

			case NicoWnnGEvent.INITIALIZE_USER_DICTIONARY:
				return mConverterJAJP.initializeDictionary( WnnEngine.DICTIONARY_TYPE_USER );

			case NicoWnnGEvent.LIST_WORDS_IN_USER_DICTIONARY:
				mUserDictionaryWords = mConverterJAJP.getUserDictionaryWords( );
				return true;

			case NicoWnnGEvent.GET_WORD:
				if (mUserDictionaryWords != null) {
					ev.word = mUserDictionaryWords[0];
					for (int i = 0 ; i < mUserDictionaryWords.length - 1 ; i++) {
						mUserDictionaryWords[i] = mUserDictionaryWords[i + 1];
					}
					mUserDictionaryWords[mUserDictionaryWords.length - 1] = null;
					if (mUserDictionaryWords[0] == null) {
						mUserDictionaryWords = null;
					}
					return true;
				}
				break;

			case NicoWnnGEvent.ADD_WORD:
				mConverterJAJP.addWord(ev.word);
				return true;

			case NicoWnnGEvent.ADD_WORDS:
				mConverterJAJP.addWords(ev.words, ev.progress);
				return true;

			case NicoWnnGEvent.DELETE_WORD:
				mConverterJAJP.deleteWord(ev.word);
				return true;

			case NicoWnnGEvent.CHANGE_MODE:
				changeEngineMode(ev.mode, ev.mode2);
				if (!((ev.mode == ENGINE_MODE_SYMBOL) || (ev.mode == ENGINE_MODE_USERSYMBOL) || (ev.mode == ENGINE_MODE_DOCOMOSYMBOL) || (ev.mode == ENGINE_MODE_EISU_KANA))) {
					initializeScreen();
				}
				if ((ev.mode == ENGINE_MODE_OPT_TYPE_QWERTY) || (ev.mode == ENGINE_MODE_OPT_TYPE_12KEY)) {
					reloadSymbol();
				}
				return true;

			case NicoWnnGEvent.UPDATE_CANDIDATE:
				if (mEngineState.isRenbun()||mEngineState.isPredict()) {
					mComposingText.setCursor(ComposingText.LAYER1,
							mComposingText.toString(ComposingText.LAYER1).length());
					mExactMatchMode = false;
					updateViewStatusForPrediction(true, true, true);
				} else {
					updateViewStatus(mTargetLayer, true, true, true);
				}
				return true;

			case NicoWnnGEvent.CHANGE_INPUT_VIEW:
				setInputView(onCreateInputView());
				return true;

			case NicoWnnGEvent.CANDIDATE_VIEW_TOUCH:
				boolean ret;
				ret = ((TextCandidatesViewManager)mCandidatesViewManager).onTouchSync();
				return ret;

			case NicoWnnGEvent.TOUCH_OTHER_KEY:
				mStatus |= STATUS_INPUT_EDIT;
				return true;

			case NicoWnnGEvent.RESET_STATUS:
				mStatus = STATUS_INIT;
				return true;

			default:
				break;
		}

		final KeyEvent keyEvent = ev.keyEvent;
		int keyCode = 0;
		if (keyEvent != null) {
			keyCode = keyEvent.getKeyCode();
			// Log.d("NicoWnnG", "keyCode="+keyCode);
		}

		if (mDirectInputMode) {
			if (mInputConnection != null) {
				switch (ev.code) {
					case NicoWnnGEvent.INPUT_SOFT_KEY:
						if (keyCode == KeyEvent.KEYCODE_ENTER) {
							sendKeyChar('\n');
						} else {
							mInputConnection.sendKeyEvent(keyEvent);
							mInputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP,
									keyEvent.getKeyCode()));
						}
						break;

					case NicoWnnGEvent.INPUT_CHAR:
						for (int i=0; i<ev.chars.length; i++) {
							char c = ev.chars[i];
							if ((c >= 0x20) && (c <= 0x7e)) {
								// ASCII
								sendKeyChar(c);
							} else if (c >= 0x80) {
								// over ASCII
								sendKeyChar(c);
							}
						}
						break;

					default:
						break;
				}
			}

			/* return if InputConnection is not active */
			return false;
		}

		{
			boolean cci = true;
			switch (ev.code) {
			case NicoWnnGEvent.COMMIT_COMPOSING_TEXT:
			case NicoWnnGEvent.FORWARD_TOGGLE:
				cci = false;
				break;
			}
			switch (keyCode) {
			case KeyEvent.KEYCODE_SHIFT_LEFT:
			case KeyEvent.KEYCODE_SHIFT_RIGHT:
			case KeyEvent.KEYCODE_ALT_LEFT:
			case KeyEvent.KEYCODE_ALT_RIGHT:
				cci = false;
				break;
			case KeyEvent.KEYCODE_DPAD_UP:
			case KeyEvent.KEYCODE_DPAD_DOWN:
				if (isCandidatesViewShown()) {
					cci = false;
				}
				break;
			case KeyEvent.KEYCODE_DPAD_LEFT:
			case KeyEvent.KEYCODE_DPAD_RIGHT:
				if (isCandidatesViewShown()) {
					if (mCandidatesViewManager.getIndicateCandidateView() > 0) {
						if (mUseLeftRightKeyCandidateSelection) {
							cci = false;
						}
					}
				}
				break;
			case KeyEvent.KEYCODE_DPAD_CENTER:
			case KeyEvent.KEYCODE_ENTER:
				if (isCandidatesViewShown()) {
					if (mEngineState.isSymbolList() || mEngineState.isDocomoSymbolList()) {
						cci = false;
					}
				}
				break;
			case KeyEvent.KEYCODE_SPACE:
				if (mCandidatesViewManager.getIndicateCandidateView() > 0) {
					cci = false;
				}
				if ((keyEvent != null) && (keyEvent.isAltPressed())) {
					cci = false;
				}
				break;
			case DefaultSoftKeyboard.KEYCODE_JP12_REVERSE:
				if (mCandidatesViewManager.getIndicateCandidateView() > 0) {
					cci = false;
				}
			}
			if (cci) {
				clearCommitInfo();
			}
		}

		/* change back the dictionary if necessary */
		{
			boolean ues = true;
			switch (ev.code) {
			case NicoWnnGEvent.SELECT_CANDIDATE:
			case NicoWnnGEvent.LIST_CANDIDATES_NORMAL:
			case NicoWnnGEvent.LIST_CANDIDATES_FULL:
			case NicoWnnGEvent.FORWARD_TOGGLE:
				ues = false;
			}
			switch (keyCode) {
			case KeyEvent.KEYCODE_SHIFT_LEFT:
			case KeyEvent.KEYCODE_SHIFT_RIGHT:
			case KeyEvent.KEYCODE_ALT_LEFT:
			case KeyEvent.KEYCODE_ALT_RIGHT:
				ues = false;
				break;
			case KeyEvent.KEYCODE_DPAD_UP:
			case KeyEvent.KEYCODE_DPAD_DOWN:
				if (isCandidatesViewShown()) {
					ues = false;
				}
				break;
			case KeyEvent.KEYCODE_DPAD_LEFT:
			case KeyEvent.KEYCODE_DPAD_RIGHT:
				if (isCandidatesViewShown()) {
					if (mCandidatesViewManager.getIndicateCandidateView() > 0) {
						if (mUseLeftRightKeyCandidateSelection) {
							ues = false;
						}
					}
				}
				break;
			case KeyEvent.KEYCODE_DPAD_CENTER:
			case KeyEvent.KEYCODE_ENTER:
				if (isCandidatesViewShown()) {
					if (mEngineState.isSymbolList() || mEngineState.isDocomoSymbolList() || mEngineState.isUserSymbol()) {
						ues = false;
					}
				}
				break;
			case KeyEvent.KEYCODE_BACK:
				if (mCandidatesViewManager.getViewType() == CandidatesViewManager.VIEW_TYPE_FULL) {
					ues = false;
				}
				break;
			case KeyEvent.KEYCODE_SPACE:
				if (mCandidatesViewManager.getIndicateCandidateView() > 0) {
					ues = false;
				}
				if ((keyEvent != null) && (keyEvent.isAltPressed())) {
					ues = false;
				}
				break;
			case DefaultSoftKeyboard.KEYCODE_JP12_REVERSE:
				if (mCandidatesViewManager.getIndicateCandidateView() > 0) {
					ues = false;
				}
				break;
			case DefaultSoftKeyboard.KEYCODE_IS01_MOJI:
			case DefaultSoftKeyboard.KEYCODE_IS01_E_KAO_KI:
			case DefaultSoftKeyboard.KEYCODE_IS11SH_MOJI:
			case DefaultSoftKeyboard.KEYCODE_IS11SH_E_KAO_KI:
			case DefaultSoftKeyboard.KEYCODE_007SH_MOJI:
			case DefaultSoftKeyboard.KEYCODE_007SH_E_KAO_KI:
				if (mIs01Enable) {
					ues = false;
				}
				break;
			}
			if (ues) {
				state = new EngineState();
				state.temporaryMode = EngineState.TEMPORARY_DICTIONARY_MODE_NONE;
				updateEngineState(state);
			}
		}

		if (ev.code == NicoWnnGEvent.LIST_CANDIDATES_FULL) {
			mStatus |= STATUS_CANDIDATE_FULL;
			mCandidatesViewManager.setViewType(CandidatesViewManager.VIEW_TYPE_FULL);
			return true;
		}
		if (ev.code == NicoWnnGEvent.LIST_CANDIDATES_NORMAL) {
			mStatus &= ~STATUS_CANDIDATE_FULL;
			mCandidatesViewManager.setViewType(CandidatesViewManager.VIEW_TYPE_NORMAL);
			return true;
		}

		boolean ret = false;
		switch (ev.code) {
			case NicoWnnGEvent.INPUT_CHAR:
				if ((mPreConverter == null) && !isEnableL2Converter()) {
					/* direct input (= full-width alphabet/number input) */
					commitText(false);
					commitText(new String(ev.chars));
					mCandidatesViewManager.clearCandidates();
					mDocomoEmojiCount = 0;
					resetPrediction();
				} else if (!isEnableL2Converter()) {
					processSoftKeyboardCodeWithoutConversion(ev.chars);
				} else {
					processSoftKeyboardCode(ev.chars);
				}
				ret = true;
				break;

			case NicoWnnGEvent.TOGGLE_CHAR:
				processSoftKeyboardToggleChar(ev.toggleTable);
				ret = true;
				break;

			case NicoWnnGEvent.TOGGLE_REVERSE_CHAR:
				if (((mStatus & ~STATUS_CANDIDATE_FULL) == STATUS_INPUT)
						&& !(mEngineState.isConvertState())) {

					final int cursor = mComposingText.getCursor(ComposingText.LAYER1);
					if (cursor > 0) {
						final String prevChar = mComposingText.getStrSegment(ComposingText.LAYER1, cursor - 1).string;
						final String c = searchToggleCharacter(prevChar, ev.toggleTable, true);
						if (c != null) {
							mComposingText.delete(ComposingText.LAYER1, false);
							appendStrSegment(new StrSegment(c));
							updateViewStatusForPrediction(true, true, false);
							ret = true;
							break;
						}
					}
				}
				break;

			case NicoWnnGEvent.REPLACE_CHAR:
				final int cursor = mComposingText.getCursor(ComposingText.LAYER1);
				if ((cursor > 0)
						&& !(mEngineState.isConvertState())) {

					final String search = mComposingText.getStrSegment(ComposingText.LAYER1, cursor - 1).string;
					final String c = (String)ev.replaceTable.get(search);
					if (c != null) {
						mComposingText.delete(1, false);
						appendStrSegment(new StrSegment(c));
						updateViewStatusForPrediction(true, true, false);
						ret = true;
						mStatus = STATUS_INPUT_EDIT;
						break;
					}
				}
				break;

			case NicoWnnGEvent.INPUT_KEY:
				final DefaultSoftKeyboard inputManager = ((DefaultSoftKeyboard) mInputViewManager);
				int cutPasteActionByIme = inputManager.getCutPasteActionByIme();
				/* update shift/alt state */
				switch (keyCode) {
					case KeyEvent.KEYCODE_0:
						if (mIs12keyEnable) {
							if (inputManager != null) {
								if (inputManager.getKeyboardType() == DefaultSoftKeyboard.KEYBOARD_12KEY) {
									inputManager.onKey(
											DefaultSoftKeyboard.KEYCODE_JP12_0,
											null
									);
									return true;
								}
							}
						}
						break;

					case KeyEvent.KEYCODE_1:
					case KeyEvent.KEYCODE_2:
					case KeyEvent.KEYCODE_3:
					case KeyEvent.KEYCODE_4:
					case KeyEvent.KEYCODE_5:
					case KeyEvent.KEYCODE_6:
					case KeyEvent.KEYCODE_7:
					case KeyEvent.KEYCODE_8:
					case KeyEvent.KEYCODE_9:
						if (mIs12keyEnable) {
							if (inputManager != null) {
								if (inputManager.getKeyboardType() == DefaultSoftKeyboard.KEYBOARD_12KEY) {
									inputManager.onKey(
											DefaultSoftKeyboard.KEYCODE_JP12_1 - (keyCode-KeyEvent.KEYCODE_1),
											null
									);
									return true;
								}
							}
						}
						break;

					case KeyEvent.KEYCODE_STAR:
						if (mIs12keyEnable) {
							if (inputManager != null) {
								if (inputManager.getKeyboardType() == DefaultSoftKeyboard.KEYBOARD_12KEY) {
									inputManager.onKey(
											DefaultSoftKeyboard.KEYCODE_JP12_ASTER,
											null
									);
									return true;
								}
							}
						}
						break;

					case KeyEvent.KEYCODE_POUND:
						if (mIs12keyEnable) {
							if (inputManager != null) {
								if (inputManager.getKeyboardType() == DefaultSoftKeyboard.KEYBOARD_12KEY) {
									inputManager.onKey(
											DefaultSoftKeyboard.KEYCODE_JP12_SHARP,
											null
									);
									return true;
								}
							}
						}
						break;
				
					case KeyEvent.KEYCODE_DEL:
						if (mIs12keyEnable) {
							if (inputManager != null) {
								if (inputManager.getKeyboardType() == DefaultSoftKeyboard.KEYBOARD_12KEY) {
									inputManager.onKey(
											DefaultSoftKeyboard.KEYCODE_JP12_BACKSPACE,
											null
									);
									return true;
								}
							}
						}
						break;
				
					case KeyEvent.KEYCODE_ALT_LEFT:
					case KeyEvent.KEYCODE_ALT_RIGHT:
						if (keyEvent.getRepeatCount() == 0) {
							if (++mHardAlt > 2) { mHardAlt = 0; }
							mAltPressing   = true;
							updateMetaKeyStateDisplay();
							if (mInputConnection != null) {
								mInputConnection.sendKeyEvent(keyEvent);
							}
						}
						return true;

					case 113: // KeyEvent.KEYCODE_CTRL_LEFT:
					case 114: // KeyEvent.KEYCODE_CTRL_RIGHT:
						if (keyEvent.getRepeatCount() == 0) {
							if (++mHardCtrl > 2) { mHardCtrl = 0; }
							mCtrlPressing   = true;
							updateMetaKeyStateDisplay();
							if (mInputConnection != null) {
								mInputConnection.sendKeyEvent(keyEvent);
							}
						}
						return true;

					case KeyEvent.KEYCODE_SHIFT_LEFT:
					case KeyEvent.KEYCODE_SHIFT_RIGHT:
						if (keyEvent.getRepeatCount() == 0) {
							if (++mHardShift > 2) { mHardShift = 0; }
							mShiftPressing = true;
							updateMetaKeyStateDisplay();
							if (mInputConnection != null) {
								mInputConnection.sendKeyEvent(keyEvent);
							}
						}
						return true;

						/*
					case KeyEvent.KEYCODE_A:
					case KeyEvent.KEYCODE_X:
					case KeyEvent.KEYCODE_C:
					case KeyEvent.KEYCODE_V:
						if (mInputConnection != null) {
							if (keyEvent.isAltPressed() || isCtrlPressed(keyEvent)) {
								mHardAlt = 0;
								mHardShift = 0;
								mHardCtrl = 0;
								updateMetaKeyStateDisplay();
								mInputConnection.sendKeyEvent(keyEvent);
								return true;
							}
						}
						break;
						*/

					case KeyEvent.KEYCODE_A:
						if (mInputConnection != null) {
							if ((keyEvent.isAltPressed() && ((cutPasteActionByIme & DefaultSoftKeyboard.CUTPASTEACTIONBYIME_ALT) != 0)) || (isCtrlPressed(keyEvent) && ((cutPasteActionByIme & DefaultSoftKeyboard.CUTPASTEACTIONBYIME_CTRL) != 0))) {
								mHardAlt = 0;
								mHardShift = 0;
								mHardCtrl = 0;
								updateMetaKeyStateDisplay();
								mInputConnection.performContextMenuAction(android.R.id.selectAll);
								return true;
							}
						}
						break;

					case KeyEvent.KEYCODE_X:
						if (mInputConnection != null) {
							if ((keyEvent.isAltPressed() && ((cutPasteActionByIme & DefaultSoftKeyboard.CUTPASTEACTIONBYIME_ALT) != 0)) || (isCtrlPressed(keyEvent) && ((cutPasteActionByIme & DefaultSoftKeyboard.CUTPASTEACTIONBYIME_CTRL) != 0))) {
								mHardAlt = 0;
								mHardShift = 0;
								mHardCtrl = 0;
								updateMetaKeyStateDisplay();
								mInputConnection.performContextMenuAction(android.R.id.cut);
								return true;
							}
						}
						break;

					case KeyEvent.KEYCODE_C:
						if (mInputConnection != null) {
							if ((keyEvent.isAltPressed() && ((cutPasteActionByIme & DefaultSoftKeyboard.CUTPASTEACTIONBYIME_ALT) != 0)) || (isCtrlPressed(keyEvent) && ((cutPasteActionByIme & DefaultSoftKeyboard.CUTPASTEACTIONBYIME_CTRL) != 0))) {
								mHardAlt = 0;
								mHardShift = 0;
								mHardCtrl = 0;
								updateMetaKeyStateDisplay();
								mInputConnection.performContextMenuAction(android.R.id.copy);
								return true;
							}
						}
						break;

					case KeyEvent.KEYCODE_V:
						if (mInputConnection != null) {
							if ((keyEvent.isAltPressed() && ((cutPasteActionByIme & DefaultSoftKeyboard.CUTPASTEACTIONBYIME_ALT) != 0)) || (isCtrlPressed(keyEvent) && ((cutPasteActionByIme & DefaultSoftKeyboard.CUTPASTEACTIONBYIME_CTRL) != 0))) {
								mHardAlt = 0;
								mHardShift = 0;
								mHardCtrl = 0;
								updateMetaKeyStateDisplay();
								mInputConnection.performContextMenuAction(android.R.id.paste);
								return true;
							}
						}
						break;

					case KeyEvent.KEYCODE_DPAD_UP:
					case KeyEvent.KEYCODE_DPAD_DOWN:
					case KeyEvent.KEYCODE_DPAD_LEFT:
					case KeyEvent.KEYCODE_DPAD_RIGHT:
						if (mInputConnection != null) {
							if (!mEngineState.isConvertState() && keyEvent.isShiftPressed()) {
								mHardAlt = 0;
								mHardShift = 0;
								mHardCtrl = 0;
								updateMetaKeyStateDisplay();
								mInputConnection.sendKeyEvent(keyEvent);
								return true;
							}
						}
						break;
						
					case DefaultSoftKeyboard.KEYCODE_IS01_MOJI:
					case DefaultSoftKeyboard.KEYCODE_007SH_MOJI:
					case DefaultSoftKeyboard.KEYCODE_IS11SH_MOJI:
						if (keyEvent.getRepeatCount() == 0) {
							evHardwareKeyboardIS01MojiKey = ev;
							synchronized (this) {
								if (!mRegisterLongPressHardwareKeyboardIS01MojiKey) {
									mRegisterLongPressHardwareKeyboardIS01MojiKey = true;
									mHandlerLongPressHardwareKeyboardIS01MojiKey.postDelayed(mActionLongPressHardwareKeyboardIS01MojiKey, mTimeOutLongPressHardwareKeyboardIS01MojiKey);
								}
							}
						}
						return true;

					case DefaultSoftKeyboard.KEYCODE_IS01_E_KAO_KI:
					case DefaultSoftKeyboard.KEYCODE_007SH_E_KAO_KI:
					case DefaultSoftKeyboard.KEYCODE_IS11SH_E_KAO_KI:
						if (keyEvent.getRepeatCount() == 0) {
							evHardwareKeyboardIS01EKaoKiKey = ev;
							synchronized (this) {
								if (!mRegisterLongPressHardwareKeyboardIS01EKaoKiKey) {
									mRegisterLongPressHardwareKeyboardIS01EKaoKiKey = true;
									mHandlerLongPressHardwareKeyboardIS01EKaoKiKey.postDelayed(mActionLongPressHardwareKeyboardIS01EKaoKiKey, mTimeOutLongPressHardwareKeyboardIS01EKaoKiKey);
								}
							}
						}
						return true;
				}

				/* handle other key event */
				ret = processKeyEvent(keyEvent);
				break;

			case NicoWnnGEvent.INPUT_SOFT_KEY:
				ret = processKeyEvent(keyEvent);
				if (!ret) {
					mInputConnection.sendKeyEvent(keyEvent);
					mInputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, keyEvent.getKeyCode()));
					ret = true;
				}
				break;

			case NicoWnnGEvent.SELECT_CANDIDATE:
				initCommitInfoForWatchCursor();
				if (isEnglishPrediction()) {
					mComposingText.clear();
				}
				mStatus = commitText(ev.word);
				if (isEnglishPrediction() && !mEngineState.isSymbolList() && !mEngineState.isDocomoSymbolList() && !mEngineState.isUserSymbol() && mEnableAutoInsertSpace) {
					commitSpaceJustOne();
				}
				checkCommitInfo();

				if (mEngineState.isSymbolList() || mEngineState.isDocomoSymbolList() || mEngineState.isUserSymbol()) {
					mEnableAutoDeleteSpace = false;
					mCandidatesViewManager.setIndicateCandidateView();
				} else {
					mCandidatesViewManager.clearIndicateCandidateView();
					if (!mEnablePredictionAfterEnter) {
						if (mComposingText.size(ComposingText.LAYER1) == 0) {
							initializeScreen();
						}
					}
					if (mComposingText.size(ComposingText.LAYER0) != 0) {
						mIndicateAfterCommit = true;
					}
				}
				if (!mEngineState.isDocomoSymbolList()) {
					mDocomoEmojiCount = 0;
				}
				break;

			case NicoWnnGEvent.CONVERT:
				if (mPreConverter != null) {
					mPreConverter.convert2(mComposingText);
				}
				if ((kPREDICTION_ON == mPredictionMode) || (kPREDICTION_OFF == mPredictionMode)) {
					startConvert(EngineState.CONVERT_TYPE_RENBUN);
				}
				else{
					//Log.d("NicoWnnG", "convert!!! [" + String.valueOf(mPredictionCount) + "]\n");
					if (0 == mPredictionCount) {
						mActionPrediction = true;
						updateViewStatusForPrediction(true, true, true);
					}
					else{
						mActionPrediction = false;
						startConvert(EngineState.CONVERT_TYPE_RENBUN);
						mActionPrediction = true; // enable prediction
					}
					mPredictionCount = (mPredictionCount+1)&1;
				}
				break;

			case NicoWnnGEvent.CONVERT_PREDICT:
				if ((kPREDICTION_ON == mPredictionMode) || (kPREDICTION_OFF == mPredictionMode)) {
					startConvert(EngineState.CONVERT_TYPE_PREDICT);
				}
				else{
					//Log.d("NicoWnnG", "convert!!! [" + String.valueOf(mPredictionCount) + "]\n");
					if (0 == mPredictionCount) {
						mActionPrediction = true;
						updateViewStatusForPrediction(true, true, true);
					}
					else{
						mActionPrediction = false;
						startConvert(EngineState.CONVERT_TYPE_RENBUN);
						mActionPrediction = true; // enable prediction
					}
					mPredictionCount = (mPredictionCount+1)&1;
				}
				break;

			case NicoWnnGEvent.CONVERT_PREDICT_BACKWARD:
				if ((kPREDICTION_ON == mPredictionMode) || (kPREDICTION_OFF == mPredictionMode)) {
					startConvert(EngineState.CONVERT_TYPE_PREDICT, true);
				}
				else{
					//Log.d("NicoWnnG", "convert!!! [" + String.valueOf(mPredictionCount) + "]\n");
					if (0 == mPredictionCount) {
						mActionPrediction = true;
						updateViewStatusForPrediction(true, true, true);
					}
					else{
						mActionPrediction = false;
						startConvert(EngineState.CONVERT_TYPE_RENBUN, true);
						mActionPrediction = true; // enable prediction
					}
					mPredictionCount = (mPredictionCount+1)&1;
				}
				break;

			case NicoWnnGEvent.COMMIT_COMPOSING_TEXT:
				commitAllText();
				break;

			case NicoWnnGEvent.FORWARD_TOGGLE:
				if (mPasswordInputMode) {
					commitText(false);
					// mInputConnection.
				} else {
					processToggleForwardEvent();
				}
				return true;
				
			case NicoWnnGEvent.FORGET_CANDIDATE:
				forgetWord(ev.word);
				return true;

		}

		return ret;
	}

	/** @see net.gorry.android.input.nicownng.NicoWnnG#onEvaluateFullscreenMode */
	@Override public boolean onEvaluateFullscreenMode() {
		final int hiddenState = getResources().getConfiguration().hardKeyboardHidden;
		final boolean hidden  = (hiddenState == Configuration.HARDKEYBOARDHIDDEN_YES);
		if (!hidden) {
			return false;
		}
		if (mInputViewFullScreenInLandscape) {
			final Configuration config = getResources().getConfiguration();
			return (config.orientation == Configuration.ORIENTATION_LANDSCAPE);
		}
		/* never use full-screen mode */
		return false;
	}

	/** @see net.gorry.android.input.nicownng.NicoWnnG#onEvaluateInputViewShown */
	@Override public boolean onEvaluateInputViewShown() {
		return true;
	}

	/**
	 * Get the instance of this service.
	 * <br>
	 * Before using this method, the constructor of this service must be invoked.
	 *
	 * @return      The instance of this service
	 */
	public static NicoWnnGJAJP getInstance() {
		return mSelf;
	}

	/**
	 * Create a {@link StrSegment} from a character code.
	 * <br>
	 * @param charCode           A character code
	 * @return                  {@link StrSegment} created; {@code null} if an error occurs.
	 */
	private StrSegment createStrSegment(final int charCode) {
		if (charCode <= 0) {
			return null;
		}
		return new StrSegment(Character.toChars(charCode));
	}

	private boolean isCtrlPressed(KeyEvent ev) {
		// return (ev.getMetaState() & META_CTRL_ON) != 0;
		return (ev.getMetaState() & 0x00001000) != 0;
	}

	private static final int keyCodeUs2JisFullHiraganaCharTable[] = {
		0x0000, // 00 : UNKNOWN
		0x0000, // 01 : SOFT_LEFT
		0x0000, // 02 : SOFT_RIGHT
		0x0000, // 03 : HOME
		0x0000, // 04 : BACK
		0x0000, // 05 : CALL
		0x0000, // 06 : ENDCALL
		0x308f, // 07 : 0
		0x306c, // 08 : 1
		0x3075, // 09 : 2
		0x3042, // 0A : 3
		0x3046, // 0B : 4
		0x3048, // 0C : 5
		0x304a, // 0D : 6
		0x3084, // 0E : 7
		0x3086, // 0F : 8
		0x3088, // 10 : 9
		0x0000, // 11 : STAR
		0x0000, // 12 : POUND
		0x0000, // 13 : DPAD_UP
		0x0000, // 14 : DPAD_DOWN
		0x0000, // 15 : DPAD_LEFT
		0x0000, // 16 : DPAD_RIGHT
		0x0000, // 17 : DPAD_CENTER
		0x0000, // 18 : VOLUME_UP
		0x0000, // 19 : VOLUME_DOWN
		0x0000, // 1A : POWER
		0x0000, // 1B : CAMERA
		0x0000, // 1C : CLEAR
		0x3061, // 1D : A
		0x3053, // 1E : B
		0x305d, // 1F : C
		0x3057, // 20 : D
		0x3044, // 21 : E
		0x306f, // 22 : F
		0x304d, // 23 : G
		0x304f, // 24 : H
		0x306b, // 25 : I
		0x307e, // 26 : J
		0x306e, // 27 : K
		0x308a, // 28 : L
		0x3082, // 29 : M
		0x307f, // 2A : N
		0x3089, // 2B : O
		0x305b, // 2C : P
		0x305f, // 2D : Q
		0x3059, // 2E : R
		0x3068, // 2F : S
		0x304b, // 30 : T
		0x306a, // 31 : U
		0x3072, // 32 : V
		0x3066, // 33 : W
		0x3055, // 34 : X
		0x3093, // 35 : Y
		0x3064, // 36 : Z
		0x306d, // 37 : COMMA
		0x308b, // 38 : PERIOD
		0x0000, // 39 : ALT_LEFT
		0x0000, // 3A : ALT_RIGHT
		0x0000, // 3B : SHIFT_LEFT
		0x0000, // 3C : SHIFT_RIGHT
		0x0000, // 3D : TAB
		0x0000, // 3E : SPACE
		0x0000, // 3F : SYM
		0x0000, // 40 : EXPLORER
		0x0000, // 41 : ENVELOPE
		0x0000, // 42 : ENTER
		0x0000, // 43 : DEL
		0x308d, // 44 : GRAVE
		0x307b, // 45 : MINUS
		0x3078, // 46 : EQUALS
		0x309b, // 47 : LEFT_BRACKET
		0x309c, // 48 : RIGHT_BRACKET
		0x3080, // 49 : BACKSLASH
		0x308c, // 4A : SEMICOLON
		0x3051, // 4B : APOSTROPHE
		0x3081, // 4C : SLASH
		0x0000, // 4D : AT
		0x0000, // 4E : NUM
		0x0000, // 4F : HEADSETHOOK
		0x0000, // 50 : FOCUS
		0x0000, // 51 : PLUS
		0x0000, // 52 : MENU
		0x0000, // 53 : NOTIFICATION
		0x0000, // 54 : SEARCH
		0x0000, // 55 : MEDIA_PLAY_PAUSE
		0x0000, // 56 : MEDIA_STOP
		0x0000, // 57 : MEDIA_NEXT
		0x0000, // 58 : MEDIA_PREVIOUS
		0x0000, // 59 : MEDIA_REWIND
		0x0000, // 5A : MEDIA_FAST_FORWARD
		0x0000, // 5B : MUTE
		0x0000, // 5C : PAGE_UP
		0x0000, // 5D : PAGE_DOWN
		0x0000, // 5E : PICTSYMBOLS
		0x0000, // 5F : SWITCH_CHARSET
		0x0000, // 60 : BUTTON_A
		0x0000, // 61 : BUTTON_B
		0x0000, // 62 : BUTTON_C
		0x0000, // 63 : BUTTON_X
		0x0000, // 64 : BUTTON_Y
		0x0000, // 65 : BUTTON_Z
		0x0000, // 66 : BUTTON_L1
		0x0000, // 67 : BUTTON_R1
		0x0000, // 68 : BUTTON_L2
		0x0000, // 69 : BUTTON_R2
		0x0000, // 6A : BUTTON_THUMBL
		0x0000, // 6B : BUTTON_THUMBR
		0x0000, // 6C : BUTTON_START
		0x0000, // 6D : BUTTON_SELECT
		0x0000, // 6E : BUTTON_MODE
	};
	
	private static final int keyCodeUs2JisFullHiraganaShiftCharTable[] = {
		0x0000, // 00 : UNKNOWN
		0x0000, // 01 : SOFT_LEFT
		0x0000, // 02 : SOFT_RIGHT
		0x0000, // 03 : HOME
		0x0000, // 04 : BACK
		0x0000, // 05 : CALL
		0x0000, // 06 : ENDCALL
		0x3092, // 07 : 0
		0x306c, // 08 : 1
		0x3075, // 09 : 2
		0x3041, // 0A : 3
		0x3045, // 0B : 4
		0x3047, // 0C : 5
		0x3049, // 0D : 6
		0x3083, // 0E : 7
		0x3085, // 0F : 8
		0x3087, // 10 : 9
		0x0000, // 11 : STAR
		0x0000, // 12 : POUND
		0x0000, // 13 : DPAD_UP
		0x0000, // 14 : DPAD_DOWN
		0x0000, // 15 : DPAD_LEFT
		0x0000, // 16 : DPAD_RIGHT
		0x0000, // 17 : DPAD_CENTER
		0x0000, // 18 : VOLUME_UP
		0x0000, // 19 : VOLUME_DOWN
		0x0000, // 1A : POWER
		0x0000, // 1B : CAMERA
		0x0000, // 1C : CLEAR
		0x3061, // 1D : A
		0x3053, // 1E : B
		0x305d, // 1F : C
		0x3057, // 20 : D
		0x3043, // 21 : E
		0x306f, // 22 : F
		0x304d, // 23 : G
		0x304f, // 24 : H
		0x306b, // 25 : I
		0x307e, // 26 : J
		0x306e, // 27 : K
		0x308a, // 28 : L
		0x3082, // 29 : M
		0x307f, // 2A : N
		0x3089, // 2B : O
		0x305b, // 2C : P
		0x305f, // 2D : Q
		0x3059, // 2E : R
		0x3068, // 2F : S
		0x30f5, // 30 : T
		0x306a, // 31 : U
		0x3072, // 32 : V
		0x3066, // 33 : W
		0x3055, // 34 : X
		0x3093, // 35 : Y
		0x3063, // 36 : Z
		0x3001, // 37 : COMMA
		0x3002, // 38 : PERIOD
		0x0000, // 39 : ALT_LEFT
		0x0000, // 3A : ALT_RIGHT
		0x0000, // 3B : SHIFT_LEFT
		0x0000, // 3C : SHIFT_RIGHT
		0x0000, // 3D : TAB
		0x0000, // 3E : SPACE
		0x0000, // 3F : SYM
		0x0000, // 40 : EXPLORER
		0x0000, // 41 : ENVELOPE
		0x0000, // 42 : ENTER
		0x0000, // 43 : DEL
		0x0000, // 44 : GRAVE
		0x30fc, // 45 : MINUS
		0x3078, // 46 : EQUALS
		0x300c, // 47 : LEFT_BRACKET
		0x300d, // 48 : RIGHT_BRACKET
		0x3080, // 49 : BACKSLASH
		0x308c, // 4A : SEMICOLON
		0x30f6, // 4B : APOSTROPHE
		0x30fb, // 4C : SLASH
		0x0000, // 4D : AT
		0x0000, // 4E : NUM
		0x0000, // 4F : HEADSETHOOK
		0x0000, // 50 : FOCUS
		0x0000, // 51 : PLUS
		0x0000, // 52 : MENU
		0x0000, // 53 : NOTIFICATION
		0x0000, // 54 : SEARCH
		0x0000, // 55 : MEDIA_PLAY_PAUSE
		0x0000, // 56 : MEDIA_STOP
		0x0000, // 57 : MEDIA_NEXT
		0x0000, // 58 : MEDIA_PREVIOUS
		0x0000, // 59 : MEDIA_REWIND
		0x0000, // 5A : MEDIA_FAST_FORWARD
		0x0000, // 5B : MUTE
		0x0000, // 5C : PAGE_UP
		0x0000, // 5D : PAGE_DOWN
		0x0000, // 5E : PICTSYMBOLS
		0x0000, // 5F : SWITCH_CHARSET
		0x0000, // 60 : BUTTON_A
		0x0000, // 61 : BUTTON_B
		0x0000, // 62 : BUTTON_C
		0x0000, // 63 : BUTTON_X
		0x0000, // 64 : BUTTON_Y
		0x0000, // 65 : BUTTON_Z
		0x0000, // 66 : BUTTON_L1
		0x0000, // 67 : BUTTON_R1
		0x0000, // 68 : BUTTON_L2
		0x0000, // 69 : BUTTON_R2
		0x0000, // 6A : BUTTON_THUMBL
		0x0000, // 6B : BUTTON_THUMBR
		0x0000, // 6C : BUTTON_START
		0x0000, // 6D : BUTTON_SELECT
		0x0000, // 6E : BUTTON_MODE
	};
	

	private static final int keyCodeJis2JisFullHiraganaCharTable[] = {
		0x0000, // 00 : UNKNOWN
		0x0000, // 01 : SOFT_LEFT
		0x0000, // 02 : SOFT_RIGHT
		0x0000, // 03 : HOME
		0x0000, // 04 : BACK
		0x0000, // 05 : CALL
		0x0000, // 06 : ENDCALL
		0x308f, // 07 : 0
		0x306c, // 08 : 1
		0x3075, // 09 : 2
		0x3042, // 0A : 3
		0x3046, // 0B : 4
		0x3048, // 0C : 5
		0x304a, // 0D : 6
		0x3084, // 0E : 7
		0x3086, // 0F : 8
		0x3088, // 10 : 9
		0x0000, // 11 : STAR
		0x0000, // 12 : POUND
		0x0000, // 13 : DPAD_UP
		0x0000, // 14 : DPAD_DOWN
		0x0000, // 15 : DPAD_LEFT
		0x0000, // 16 : DPAD_RIGHT
		0x0000, // 17 : DPAD_CENTER
		0x0000, // 18 : VOLUME_UP
		0x0000, // 19 : VOLUME_DOWN
		0x0000, // 1A : POWER
		0x0000, // 1B : CAMERA
		0x0000, // 1C : CLEAR
		0x3061, // 1D : A
		0x3053, // 1E : B
		0x305d, // 1F : C
		0x3057, // 20 : D
		0x3044, // 21 : E
		0x306f, // 22 : F
		0x304d, // 23 : G
		0x304f, // 24 : H
		0x306b, // 25 : I
		0x307e, // 26 : J
		0x306e, // 27 : K
		0x308a, // 28 : L
		0x3082, // 29 : M
		0x307f, // 2A : N
		0x3089, // 2B : O
		0x305b, // 2C : P
		0x305f, // 2D : Q
		0x3059, // 2E : R
		0x3068, // 2F : S
		0x304b, // 30 : T
		0x306a, // 31 : U
		0x3072, // 32 : V
		0x3066, // 33 : W
		0x3055, // 34 : X
		0x3093, // 35 : Y
		0x3064, // 36 : Z
		0x306d, // 37 : COMMA
		0x308b, // 38 : PERIOD
		0x0000, // 39 : ALT_LEFT
		0x0000, // 3A : ALT_RIGHT
		0x0000, // 3B : SHIFT_LEFT
		0x0000, // 3C : SHIFT_RIGHT
		0x0000, // 3D : TAB
		0x0000, // 3E : SPACE
		0x0000, // 3F : SYM
		0x0000, // 40 : EXPLORER
		0x0000, // 41 : ENVELOPE
		0x0000, // 42 : ENTER
		0x0000, // 43 : DEL
		0x30fc, /* 0x0000, */ // 44 : GRAVE
		0x307b, // 45 : MINUS
		0x3078, // 46 : EQUALS
		0x309c, /* 0x309b, */ // 47 : LEFT_BRACKET
		0x3080, /* 0x309c, */ // 48 : RIGHT_BRACKET
		0x308d, /* 0x3080, */ // 49 : BACKSLASH
		0x308c, // 4A : SEMICOLON
		0x3051, // 4B : APOSTROPHE
		0x3081, // 4C : SLASH
		0x309b, /* 0x0000, */ // 4D : AT
		0x0000, // 4E : NUM
		0x0000, // 4F : HEADSETHOOK
		0x0000, // 50 : FOCUS
		0x0000, // 51 : PLUS
		0x0000, // 52 : MENU
		0x0000, // 53 : NOTIFICATION
		0x0000, // 54 : SEARCH
		0x0000, // 55 : MEDIA_PLAY_PAUSE
		0x0000, // 56 : MEDIA_STOP
		0x0000, // 57 : MEDIA_NEXT
		0x0000, // 58 : MEDIA_PREVIOUS
		0x0000, // 59 : MEDIA_REWIND
		0x0000, // 5A : MEDIA_FAST_FORWARD
		0x0000, // 5B : MUTE
		0x0000, // 5C : PAGE_UP
		0x0000, // 5D : PAGE_DOWN
		0x0000, // 5E : PICTSYMBOLS
		0x0000, // 5F : SWITCH_CHARSET
		0x0000, // 60 : BUTTON_A
		0x0000, // 61 : BUTTON_B
		0x0000, // 62 : BUTTON_C
		0x0000, // 63 : BUTTON_X
		0x0000, // 64 : BUTTON_Y
		0x0000, // 65 : BUTTON_Z
		0x0000, // 66 : BUTTON_L1
		0x0000, // 67 : BUTTON_R1
		0x0000, // 68 : BUTTON_L2
		0x0000, // 69 : BUTTON_R2
		0x0000, // 6A : BUTTON_THUMBL
		0x0000, // 6B : BUTTON_THUMBR
		0x0000, // 6C : BUTTON_START
		0x0000, // 6D : BUTTON_SELECT
		0x0000, // 6E : BUTTON_MODE
	};
	
	private static final int keyCodeJis2JisFullHiraganaShiftCharTable[] = {
		0x0000, // 00 : UNKNOWN
		0x0000, // 01 : SOFT_LEFT
		0x0000, // 02 : SOFT_RIGHT
		0x0000, // 03 : HOME
		0x0000, // 04 : BACK
		0x0000, // 05 : CALL
		0x0000, // 06 : ENDCALL
		0x3092, // 07 : 0
		0x306c, // 08 : 1
		0x3075, // 09 : 2
		0x3041, // 0A : 3
		0x3045, // 0B : 4
		0x3047, // 0C : 5
		0x3049, // 0D : 6
		0x3083, // 0E : 7
		0x3085, // 0F : 8
		0x3087, // 10 : 9
		0x0000, // 11 : STAR
		0x0000, // 12 : POUND
		0x0000, // 13 : DPAD_UP
		0x0000, // 14 : DPAD_DOWN
		0x0000, // 15 : DPAD_LEFT
		0x0000, // 16 : DPAD_RIGHT
		0x0000, // 17 : DPAD_CENTER
		0x0000, // 18 : VOLUME_UP
		0x0000, // 19 : VOLUME_DOWN
		0x0000, // 1A : POWER
		0x0000, // 1B : CAMERA
		0x0000, // 1C : CLEAR
		0x3061, // 1D : A
		0x3053, // 1E : B
		0x305d, // 1F : C
		0x3057, // 20 : D
		0x3043, // 21 : E
		0x306f, // 22 : F
		0x304d, // 23 : G
		0x304f, // 24 : H
		0x306b, // 25 : I
		0x307e, // 26 : J
		0x306e, // 27 : K
		0x308a, // 28 : L
		0x3082, // 29 : M
		0x307f, // 2A : N
		0x3089, // 2B : O
		0x305b, // 2C : P
		0x305f, // 2D : Q
		0x3059, // 2E : R
		0x3068, // 2F : S
		0x30f5, // 30 : T
		0x306a, // 31 : U
		0x3072, // 32 : V
		0x3066, // 33 : W
		0x3055, // 34 : X
		0x3093, // 35 : Y
		0x3063, // 36 : Z
		0x3001, // 37 : COMMA
		0x3002, // 38 : PERIOD
		0x0000, // 39 : ALT_LEFT
		0x0000, // 3A : ALT_RIGHT
		0x0000, // 3B : SHIFT_LEFT
		0x0000, // 3C : SHIFT_RIGHT
		0x0000, // 3D : TAB
		0x0000, // 3E : SPACE
		0x0000, // 3F : SYM
		0x0000, // 40 : EXPLORER
		0x0000, // 41 : ENVELOPE
		0x0000, // 42 : ENTER
		0x0000, // 43 : DEL
		0xff5c, /* 0x0000, */ // 44 : GRAVE
		0x307b, // 45 : MINUS
		0x3078, // 46 : EQUALS
		0x300c, /* 0x309b, */ // 47 : LEFT_BRACKET
		0x300d, /* 0x300c, */ // 48 : RIGHT_BRACKET
		0x308d, /* 0x300d, */ // 49 : BACKSLASH
		0x308c, // 4A : SEMICOLON
		0x30f6, // 4B : APOSTROPHE
		0x30fb, // 4C : SLASH
		0x309b, /* 0x3000, */ // 4D : AT
		0x0000, // 4E : NUM
		0x0000, // 4F : HEADSETHOOK
		0x0000, // 50 : FOCUS
		0x0000, // 51 : PLUS
		0x0000, // 52 : MENU
		0x0000, // 53 : NOTIFICATION
		0x0000, // 54 : SEARCH
		0x0000, // 55 : MEDIA_PLAY_PAUSE
		0x0000, // 56 : MEDIA_STOP
		0x0000, // 57 : MEDIA_NEXT
		0x0000, // 58 : MEDIA_PREVIOUS
		0x0000, // 59 : MEDIA_REWIND
		0x0000, // 5A : MEDIA_FAST_FORWARD
		0x0000, // 5B : MUTE
		0x0000, // 5C : PAGE_UP
		0x0000, // 5D : PAGE_DOWN
		0x0000, // 5E : PICTSYMBOLS
		0x0000, // 5F : SWITCH_CHARSET
		0x0000, // 60 : BUTTON_A
		0x0000, // 61 : BUTTON_B
		0x0000, // 62 : BUTTON_C
		0x0000, // 63 : BUTTON_X
		0x0000, // 64 : BUTTON_Y
		0x0000, // 65 : BUTTON_Z
		0x0000, // 66 : BUTTON_L1
		0x0000, // 67 : BUTTON_R1
		0x0000, // 68 : BUTTON_L2
		0x0000, // 69 : BUTTON_R2
		0x0000, // 6A : BUTTON_THUMBL
		0x0000, // 6B : BUTTON_THUMBR
		0x0000, // 6C : BUTTON_START
		0x0000, // 6D : BUTTON_SELECT
		0x0000, // 6E : BUTTON_MODE
	};
	
	private int getJisFullHiraganaCharByEvent(final KeyEvent ev, final boolean shift) {
		int keyCode = ev.getKeyCode();
		if (keyCode < 0) {
			switch (keyCode) {
				case DefaultSoftKeyboard.KEYCODE_JIS_RO:
					return 0x308d;
				case DefaultSoftKeyboard.KEYCODE_JIS_CHOUON:
					return 0x30fc;
			}
		} else if (keyCode < keyCodeUs2JisFullHiraganaCharTable.length) {
			if (shift || ev.isShiftPressed()) {
				switch (mUseConvertKeyMap) {
					case CONVERT_KEYMAP_KB_US_OS_US:
						return keyCodeUs2JisFullHiraganaShiftCharTable[keyCode];
					case CONVERT_KEYMAP_KB_JIS_OS_US:
						return keyCodeUs2JisFullHiraganaShiftCharTable[keyCode];
					default:
						return keyCodeJis2JisFullHiraganaShiftCharTable[keyCode];
				}
			}
			switch (mUseConvertKeyMap) {
				case CONVERT_KEYMAP_KB_US_OS_US:
					return keyCodeUs2JisFullHiraganaCharTable[keyCode];
				case CONVERT_KEYMAP_KB_JIS_OS_US:
					return keyCodeUs2JisFullHiraganaCharTable[keyCode];
				default:
					return keyCodeJis2JisFullHiraganaCharTable[keyCode];
			}
		}
		return 0;
	}
	
	private static final int keyCodeUs2JisFullKatakanaCharTable[] = {
		0x0000, // 00 : UNKNOWN
		0x0000, // 01 : SOFT_LEFT
		0x0000, // 02 : SOFT_RIGHT
		0x0000, // 03 : HOME
		0x0000, // 04 : BACK
		0x0000, // 05 : CALL
		0x0000, // 06 : ENDCALL
		0x30ef, // 07 : 0
		0x30cc, // 08 : 1
		0x30d5, // 09 : 2
		0x30a2, // 0A : 3
		0x30a6, // 0B : 4
		0x30a8, // 0C : 5
		0x30aa, // 0D : 6
		0x30e4, // 0E : 7
		0x30e6, // 0F : 8
		0x30e8, // 10 : 9
		0x0000, // 11 : STAR
		0x0000, // 12 : POUND
		0x0000, // 13 : DPAD_UP
		0x0000, // 14 : DPAD_DOWN
		0x0000, // 15 : DPAD_LEFT
		0x0000, // 16 : DPAD_RIGHT
		0x0000, // 17 : DPAD_CENTER
		0x0000, // 18 : VOLUME_UP
		0x0000, // 19 : VOLUME_DOWN
		0x0000, // 1A : POWER
		0x0000, // 1B : CAMERA
		0x0000, // 1C : CLEAR
		0x30c1, // 1D : A
		0x30b3, // 1E : B
		0x30bd, // 1F : C
		0x30b7, // 20 : D
		0x30a4, // 21 : E
		0x30cf, // 22 : F
		0x30ad, // 23 : G
		0x30af, // 24 : H
		0x30cb, // 25 : I
		0x30de, // 26 : J
		0x30ce, // 27 : K
		0x30ea, // 28 : L
		0x30e2, // 29 : M
		0x30df, // 2A : N
		0x30e9, // 2B : O
		0x30bb, // 2C : P
		0x30bf, // 2D : Q
		0x30b9, // 2E : R
		0x30c8, // 2F : S
		0x30ab, // 30 : T
		0x30ca, // 31 : U
		0x30d2, // 32 : V
		0x30c6, // 33 : W
		0x30b5, // 34 : X
		0x30f3, // 35 : Y
		0x30c4, // 36 : Z
		0x30cd, // 37 : COMMA
		0x30eb, // 38 : PERIOD
		0x0000, // 39 : ALT_LEFT
		0x0000, // 3A : ALT_RIGHT
		0x0000, // 3B : SHIFT_LEFT
		0x0000, // 3C : SHIFT_RIGHT
		0x0000, // 3D : TAB
		0x0000, // 3E : SPACE
		0x0000, // 3F : SYM
		0x0000, // 40 : EXPLORER
		0x0000, // 41 : ENVELOPE
		0x0000, // 42 : ENTER
		0x0000, // 43 : DEL
		0x30ed, // 44 : GRAVE
		0x30db, // 45 : MINUS
		0x30d8, // 46 : EQUALS
		0x309b, // 47 : LEFT_BRACKET
		0x309c, // 48 : RIGHT_BRACKET
		0x30e0, // 49 : BACKSLASH
		0x30ec, // 4A : SEMICOLON
		0x30b1, // 4B : APOSTROPHE
		0x30e1, // 4C : SLASH
		0x0000, // 4D : AT
		0x0000, // 4E : NUM
		0x0000, // 4F : HEADSETHOOK
		0x0000, // 50 : FOCUS
		0x0000, // 51 : PLUS
		0x0000, // 52 : MENU
		0x0000, // 53 : NOTIFICATION
		0x0000, // 54 : SEARCH
		0x0000, // 55 : MEDIA_PLAY_PAUSE
		0x0000, // 56 : MEDIA_STOP
		0x0000, // 57 : MEDIA_NEXT
		0x0000, // 58 : MEDIA_PREVIOUS
		0x0000, // 59 : MEDIA_REWIND
		0x0000, // 5A : MEDIA_FAST_FORWARD
		0x0000, // 5B : MUTE
		0x0000, // 5C : PAGE_UP
		0x0000, // 5D : PAGE_DOWN
		0x0000, // 5E : PICTSYMBOLS
		0x0000, // 5F : SWITCH_CHARSET
		0x0000, // 60 : BUTTON_A
		0x0000, // 61 : BUTTON_B
		0x0000, // 62 : BUTTON_C
		0x0000, // 63 : BUTTON_X
		0x0000, // 64 : BUTTON_Y
		0x0000, // 65 : BUTTON_Z
		0x0000, // 66 : BUTTON_L1
		0x0000, // 67 : BUTTON_R1
		0x0000, // 68 : BUTTON_L2
		0x0000, // 69 : BUTTON_R2
		0x0000, // 6A : BUTTON_THUMBL
		0x0000, // 6B : BUTTON_THUMBR
		0x0000, // 6C : BUTTON_START
		0x0000, // 6D : BUTTON_SELECT
		0x0000, // 6E : BUTTON_MODE
	};
	
	private static final int keyCodeUs2JisFullKatakanaShiftCharTable[] = {
		0x0000, // 00 : UNKNOWN
		0x0000, // 01 : SOFT_LEFT
		0x0000, // 02 : SOFT_RIGHT
		0x0000, // 03 : HOME
		0x0000, // 04 : BACK
		0x0000, // 05 : CALL
		0x0000, // 06 : ENDCALL
		0x30f2, // 07 : 0
		0x30cc, // 08 : 1
		0x30d5, // 09 : 2
		0x30a1, // 0A : 3
		0x30a5, // 0B : 4
		0x30a7, // 0C : 5
		0x30a9, // 0D : 6
		0x30e3, // 0E : 7
		0x30e5, // 0F : 8
		0x30e7, // 10 : 9
		0x0000, // 11 : STAR
		0x0000, // 12 : POUND
		0x0000, // 13 : DPAD_UP
		0x0000, // 14 : DPAD_DOWN
		0x0000, // 15 : DPAD_LEFT
		0x0000, // 16 : DPAD_RIGHT
		0x0000, // 17 : DPAD_CENTER
		0x0000, // 18 : VOLUME_UP
		0x0000, // 19 : VOLUME_DOWN
		0x0000, // 1A : POWER
		0x0000, // 1B : CAMERA
		0x0000, // 1C : CLEAR
		0x30c1, // 1D : A
		0x30b3, // 1E : B
		0x30bd, // 1F : C
		0x30b7, // 20 : D
		0x30a3, // 21 : E
		0x30cf, // 22 : F
		0x30ad, // 23 : G
		0x30af, // 24 : H
		0x30cb, // 25 : I
		0x30de, // 26 : J
		0x30ce, // 27 : K
		0x30ea, // 28 : L
		0x30e2, // 29 : M
		0x30df, // 2A : N
		0x30e9, // 2B : O
		0x30bb, // 2C : P
		0x30bf, // 2D : Q
		0x30b9, // 2E : R
		0x30c8, // 2F : S
		0x30f5, // 30 : T
		0x30ca, // 31 : U
		0x30d2, // 32 : V
		0x30c6, // 33 : W
		0x30b5, // 34 : X
		0x30f3, // 35 : Y
		0x30c3, // 36 : Z
		0x3001, // 37 : COMMA
		0x3002, // 38 : PERIOD
		0x0000, // 39 : ALT_LEFT
		0x0000, // 3A : ALT_RIGHT
		0x0000, // 3B : SHIFT_LEFT
		0x0000, // 3C : SHIFT_RIGHT
		0x0000, // 3D : TAB
		0x0000, // 3E : SPACE
		0x0000, // 3F : SYM
		0x0000, // 40 : EXPLORER
		0x0000, // 41 : ENVELOPE
		0x0000, // 42 : ENTER
		0x0000, // 43 : DEL
		0x0000, // 44 : GRAVE
		0x30fc, // 45 : MINUS
		0x30d8, // 46 : EQUALS
		0x300c, // 47 : LEFT_BRACKET
		0x300d, // 48 : RIGHT_BRACKET
		0x30e0, // 49 : BACKSLASH
		0x30ec, // 4A : SEMICOLON
		0x30f6, // 4B : APOSTROPHE
		0x30fb, // 4C : SLASH
		0x0000, // 4D : AT
		0x0000, // 4E : NUM
		0x0000, // 4F : HEADSETHOOK
		0x0000, // 50 : FOCUS
		0x0000, // 51 : PLUS
		0x0000, // 52 : MENU
		0x0000, // 53 : NOTIFICATION
		0x0000, // 54 : SEARCH
		0x0000, // 55 : MEDIA_PLAY_PAUSE
		0x0000, // 56 : MEDIA_STOP
		0x0000, // 57 : MEDIA_NEXT
		0x0000, // 58 : MEDIA_PREVIOUS
		0x0000, // 59 : MEDIA_REWIND
		0x0000, // 5A : MEDIA_FAST_FORWARD
		0x0000, // 5B : MUTE
		0x0000, // 5C : PAGE_UP
		0x0000, // 5D : PAGE_DOWN
		0x0000, // 5E : PICTSYMBOLS
		0x0000, // 5F : SWITCH_CHARSET
		0x0000, // 60 : BUTTON_A
		0x0000, // 61 : BUTTON_B
		0x0000, // 62 : BUTTON_C
		0x0000, // 63 : BUTTON_X
		0x0000, // 64 : BUTTON_Y
		0x0000, // 65 : BUTTON_Z
		0x0000, // 66 : BUTTON_L1
		0x0000, // 67 : BUTTON_R1
		0x0000, // 68 : BUTTON_L2
		0x0000, // 69 : BUTTON_R2
		0x0000, // 6A : BUTTON_THUMBL
		0x0000, // 6B : BUTTON_THUMBR
		0x0000, // 6C : BUTTON_START
		0x0000, // 6D : BUTTON_SELECT
		0x0000, // 6E : BUTTON_MODE
	};
	
	private static final int keyCodeJis2JisFullKatakanaCharTable[] = {
		0x0000, // 00 : UNKNOWN
		0x0000, // 01 : SOFT_LEFT
		0x0000, // 02 : SOFT_RIGHT
		0x0000, // 03 : HOME
		0x0000, // 04 : BACK
		0x0000, // 05 : CALL
		0x0000, // 06 : ENDCALL
		0x30ef, // 07 : 0
		0x30cc, // 08 : 1
		0x30d5, // 09 : 2
		0x30a2, // 0A : 3
		0x30a6, // 0B : 4
		0x30a8, // 0C : 5
		0x30aa, // 0D : 6
		0x30e4, // 0E : 7
		0x30e6, // 0F : 8
		0x30e8, // 10 : 9
		0x0000, // 11 : STAR
		0x0000, // 12 : POUND
		0x0000, // 13 : DPAD_UP
		0x0000, // 14 : DPAD_DOWN
		0x0000, // 15 : DPAD_LEFT
		0x0000, // 16 : DPAD_RIGHT
		0x0000, // 17 : DPAD_CENTER
		0x0000, // 18 : VOLUME_UP
		0x0000, // 19 : VOLUME_DOWN
		0x0000, // 1A : POWER
		0x0000, // 1B : CAMERA
		0x0000, // 1C : CLEAR
		0x30c1, // 1D : A
		0x30b3, // 1E : B
		0x30bd, // 1F : C
		0x30b7, // 20 : D
		0x30a4, // 21 : E
		0x30cf, // 22 : F
		0x30ad, // 23 : G
		0x30af, // 24 : H
		0x30cb, // 25 : I
		0x30de, // 26 : J
		0x30ce, // 27 : K
		0x30ea, // 28 : L
		0x30e2, // 29 : M
		0x30df, // 2A : N
		0x30e9, // 2B : O
		0x30bb, // 2C : P
		0x30bf, // 2D : Q
		0x30b9, // 2E : R
		0x30c8, // 2F : S
		0x30ab, // 30 : T
		0x30ca, // 31 : U
		0x30d2, // 32 : V
		0x30c6, // 33 : W
		0x30b5, // 34 : X
		0x30f3, // 35 : Y
		0x30c4, // 36 : Z
		0x30cd, // 37 : COMMA
		0x30eb, // 38 : PERIOD
		0x0000, // 39 : ALT_LEFT
		0x0000, // 3A : ALT_RIGHT
		0x0000, // 3B : SHIFT_LEFT
		0x0000, // 3C : SHIFT_RIGHT
		0x0000, // 3D : TAB
		0x0000, // 3E : SPACE
		0x0000, // 3F : SYM
		0x0000, // 40 : EXPLORER
		0x0000, // 41 : ENVELOPE
		0x0000, // 42 : ENTER
		0x0000, // 43 : DEL
		0x30fc, /* 0x0000, */ // 44 : GRAVE
		0x30db, // 45 : MINUS
		0x30d8, // 46 : EQUALS
		0x309c, /* 0x309b, */ // 47 : LEFT_BRACKET
		0x30e0, /* 0x309c, */ // 48 : RIGHT_BRACKET
		0x30ed, /* 0x30e0, */ // 49 : BACKSLASH
		0x30ec, // 4A : SEMICOLON
		0x30b1, // 4B : APOSTROPHE
		0x30e1, // 4C : SLASH
		0x309b, /* 0x0000, */ // 4D : AT
		0x0000, // 4E : NUM
		0x0000, // 4F : HEADSETHOOK
		0x0000, // 50 : FOCUS
		0x0000, // 51 : PLUS
		0x0000, // 52 : MENU
		0x0000, // 53 : NOTIFICATION
		0x0000, // 54 : SEARCH
		0x0000, // 55 : MEDIA_PLAY_PAUSE
		0x0000, // 56 : MEDIA_STOP
		0x0000, // 57 : MEDIA_NEXT
		0x0000, // 58 : MEDIA_PREVIOUS
		0x0000, // 59 : MEDIA_REWIND
		0x0000, // 5A : MEDIA_FAST_FORWARD
		0x0000, // 5B : MUTE
		0x0000, // 5C : PAGE_UP
		0x0000, // 5D : PAGE_DOWN
		0x0000, // 5E : PICTSYMBOLS
		0x0000, // 5F : SWITCH_CHARSET
		0x0000, // 60 : BUTTON_A
		0x0000, // 61 : BUTTON_B
		0x0000, // 62 : BUTTON_C
		0x0000, // 63 : BUTTON_X
		0x0000, // 64 : BUTTON_Y
		0x0000, // 65 : BUTTON_Z
		0x0000, // 66 : BUTTON_L1
		0x0000, // 67 : BUTTON_R1
		0x0000, // 68 : BUTTON_L2
		0x0000, // 69 : BUTTON_R2
		0x0000, // 6A : BUTTON_THUMBL
		0x0000, // 6B : BUTTON_THUMBR
		0x0000, // 6C : BUTTON_START
		0x0000, // 6D : BUTTON_SELECT
		0x0000, // 6E : BUTTON_MODE
	};
	
	private static final int keyCodeJis2JisFullKatakanaShiftCharTable[] = {
		0x0000, // 00 : UNKNOWN
		0x0000, // 01 : SOFT_LEFT
		0x0000, // 02 : SOFT_RIGHT
		0x0000, // 03 : HOME
		0x0000, // 04 : BACK
		0x0000, // 05 : CALL
		0x0000, // 06 : ENDCALL
		0x30f2, // 07 : 0
		0x30cc, // 08 : 1
		0x30d5, // 09 : 2
		0x30a1, // 0A : 3
		0x30a5, // 0B : 4
		0x30a7, // 0C : 5
		0x30a9, // 0D : 6
		0x30e3, // 0E : 7
		0x30e5, // 0F : 8
		0x30e7, // 10 : 9
		0x0000, // 11 : STAR
		0x0000, // 12 : POUND
		0x0000, // 13 : DPAD_UP
		0x0000, // 14 : DPAD_DOWN
		0x0000, // 15 : DPAD_LEFT
		0x0000, // 16 : DPAD_RIGHT
		0x0000, // 17 : DPAD_CENTER
		0x0000, // 18 : VOLUME_UP
		0x0000, // 19 : VOLUME_DOWN
		0x0000, // 1A : POWER
		0x0000, // 1B : CAMERA
		0x0000, // 1C : CLEAR
		0x30c1, // 1D : A
		0x30b3, // 1E : B
		0x30bd, // 1F : C
		0x30b7, // 20 : D
		0x30a3, // 21 : E
		0x30cf, // 22 : F
		0x30ad, // 23 : G
		0x30af, // 24 : H
		0x30cb, // 25 : I
		0x30de, // 26 : J
		0x30ce, // 27 : K
		0x30ea, // 28 : L
		0x30e2, // 29 : M
		0x30df, // 2A : N
		0x30e9, // 2B : O
		0x30bb, // 2C : P
		0x30bf, // 2D : Q
		0x30b9, // 2E : R
		0x30c8, // 2F : S
		0x30f5, // 30 : T
		0x30ca, // 31 : U
		0x30d2, // 32 : V
		0x30c6, // 33 : W
		0x30b5, // 34 : X
		0x30f3, // 35 : Y
		0x30c3, // 36 : Z
		0x3001, // 37 : COMMA
		0x3002, // 38 : PERIOD
		0x0000, // 39 : ALT_LEFT
		0x0000, // 3A : ALT_RIGHT
		0x0000, // 3B : SHIFT_LEFT
		0x0000, // 3C : SHIFT_RIGHT
		0x0000, // 3D : TAB
		0x0000, // 3E : SPACE
		0x0000, // 3F : SYM
		0x0000, // 40 : EXPLORER
		0x0000, // 41 : ENVELOPE
		0x0000, // 42 : ENTER
		0x0000, // 43 : DEL
		0xff5c, /* 0x0000, */ // 44 : GRAVE
		0x30db, // 45 : MINUS
		0x30d8, // 46 : EQUALS
		0x300c, /* 0x30fb, */ // 47 : LEFT_BRACKET
		0x300d, /* 0x300c, */ // 48 : RIGHT_BRACKET
		0x30ed, /* 0x300d, */ // 49 : BACKSLASH
		0x30ec, // 4A : SEMICOLON
		0x30f6, // 4B : APOSTROPHE
		0x30fb, // 4C : SLASH
		0x309b, /* 0x0000, */ // 4D : AT
		0x0000, // 4E : NUM
		0x0000, // 4F : HEADSETHOOK
		0x0000, // 50 : FOCUS
		0x0000, // 51 : PLUS
		0x0000, // 52 : MENU
		0x0000, // 53 : NOTIFICATION
		0x0000, // 54 : SEARCH
		0x0000, // 55 : MEDIA_PLAY_PAUSE
		0x0000, // 56 : MEDIA_STOP
		0x0000, // 57 : MEDIA_NEXT
		0x0000, // 58 : MEDIA_PREVIOUS
		0x0000, // 59 : MEDIA_REWIND
		0x0000, // 5A : MEDIA_FAST_FORWARD
		0x0000, // 5B : MUTE
		0x0000, // 5C : PAGE_UP
		0x0000, // 5D : PAGE_DOWN
		0x0000, // 5E : PICTSYMBOLS
		0x0000, // 5F : SWITCH_CHARSET
		0x0000, // 60 : BUTTON_A
		0x0000, // 61 : BUTTON_B
		0x0000, // 62 : BUTTON_C
		0x0000, // 63 : BUTTON_X
		0x0000, // 64 : BUTTON_Y
		0x0000, // 65 : BUTTON_Z
		0x0000, // 66 : BUTTON_L1
		0x0000, // 67 : BUTTON_R1
		0x0000, // 68 : BUTTON_L2
		0x0000, // 69 : BUTTON_R2
		0x0000, // 6A : BUTTON_THUMBL
		0x0000, // 6B : BUTTON_THUMBR
		0x0000, // 6C : BUTTON_START
		0x0000, // 6D : BUTTON_SELECT
		0x0000, // 6E : BUTTON_MODE
	};
	
	private int getJisFullKatakanaCharByEvent(final KeyEvent ev, final boolean shift) {
		int keyCode = ev.getKeyCode();
		if (keyCode < 0) {
			switch (keyCode) {
				case DefaultSoftKeyboard.KEYCODE_JIS_RO:
					return 0x30ed;
				case DefaultSoftKeyboard.KEYCODE_JIS_CHOUON:
					return 0x30fc;
			}
		} else if (keyCode < keyCodeUs2JisFullKatakanaCharTable.length) {
			if (shift || ev.isShiftPressed()) {
				switch (mUseConvertKeyMap) {
					case CONVERT_KEYMAP_KB_US_OS_US:
						return keyCodeUs2JisFullKatakanaShiftCharTable[keyCode];
					case CONVERT_KEYMAP_KB_JIS_OS_US:
						return keyCodeUs2JisFullKatakanaShiftCharTable[keyCode];
					default:
						return keyCodeJis2JisFullKatakanaShiftCharTable[keyCode];
				}
			}
			switch (mUseConvertKeyMap) {
				case CONVERT_KEYMAP_KB_US_OS_US:
					return keyCodeUs2JisFullKatakanaCharTable[keyCode];
				case CONVERT_KEYMAP_KB_JIS_OS_US:
					return keyCodeUs2JisFullKatakanaCharTable[keyCode];
				default:
					return keyCodeJis2JisFullKatakanaCharTable[keyCode];
			}
		}
		return 0;
	}
	
	private static final int keyCodeUs2JisHalfKatakanaCharTable[] = {
		0x0000, // 00 : UNKNOWN
		0x0000, // 01 : SOFT_LEFT
		0x0000, // 02 : SOFT_RIGHT
		0x0000, // 03 : HOME
		0x0000, // 04 : BACK
		0x0000, // 05 : CALL
		0x0000, // 06 : ENDCALL
		0xff9c, // 07 : 0
		0xff87, // 08 : 1
		0xff8c, // 09 : 2
		0xff71, // 0A : 3
		0xff73, // 0B : 4
		0xff74, // 0C : 5
		0xff75, // 0D : 6
		0xff94, // 0E : 7
		0xff95, // 0F : 8
		0xff96, // 10 : 9
		0x0000, // 11 : STAR
		0x0000, // 12 : POUND
		0x0000, // 13 : DPAD_UP
		0x0000, // 14 : DPAD_DOWN
		0x0000, // 15 : DPAD_LEFT
		0x0000, // 16 : DPAD_RIGHT
		0x0000, // 17 : DPAD_CENTER
		0x0000, // 18 : VOLUME_UP
		0x0000, // 19 : VOLUME_DOWN
		0x0000, // 1A : POWER
		0x0000, // 1B : CAMERA
		0x0000, // 1C : CLEAR
		0xff81, // 1D : A
		0xff7a, // 1E : B
		0xff7f, // 1F : C
		0xff7c, // 20 : D
		0xff72, // 21 : E
		0xff8a, // 22 : F
		0xff77, // 23 : G
		0xff78, // 24 : H
		0xff86, // 25 : I
		0xff8f, // 26 : J
		0xff89, // 27 : K
		0xff98, // 28 : L
		0xff93, // 29 : M
		0xff90, // 2A : N
		0xff97, // 2B : O
		0xff7e, // 2C : P
		0xff80, // 2D : Q
		0xff7d, // 2E : R
		0xff84, // 2F : S
		0xff76, // 30 : T
		0xff85, // 31 : U
		0xff8b, // 32 : V
		0xff83, // 33 : W
		0xff7b, // 34 : X
		0xff9d, // 35 : Y
		0xff82, // 36 : Z
		0xff88, // 37 : COMMA
		0xff99, // 38 : PERIOD
		0x0000, // 39 : ALT_LEFT
		0x0000, // 3A : ALT_RIGHT
		0x0000, // 3B : SHIFT_LEFT
		0x0000, // 3C : SHIFT_RIGHT
		0x0000, // 3D : TAB
		0x0000, // 3E : SPACE
		0x0000, // 3F : SYM
		0x0000, // 40 : EXPLORER
		0x0000, // 41 : ENVELOPE
		0x0000, // 42 : ENTER
		0x0000, // 43 : DEL
		0xff9b, // 44 : GRAVE
		0xff8e, // 45 : MINUS
		0xff8d, // 46 : EQUALS
		0xff9e, // 47 : LEFT_BRACKET
		0xff9f, // 48 : RIGHT_BRACKET
		0xff91, // 49 : BACKSLASH
		0xff9a, // 4A : SEMICOLON
		0xff79, // 4B : APOSTROPHE
		0xff92, // 4C : SLASH
		0x0000, // 4D : AT
		0x0000, // 4E : NUM
		0x0000, // 4F : HEADSETHOOK
		0x0000, // 50 : FOCUS
		0x0000, // 51 : PLUS
		0x0000, // 52 : MENU
		0x0000, // 53 : NOTIFICATION
		0x0000, // 54 : SEARCH
		0x0000, // 55 : MEDIA_PLAY_PAUSE
		0x0000, // 56 : MEDIA_STOP
		0x0000, // 57 : MEDIA_NEXT
		0x0000, // 58 : MEDIA_PREVIOUS
		0x0000, // 59 : MEDIA_REWIND
		0x0000, // 5A : MEDIA_FAST_FORWARD
		0x0000, // 5B : MUTE
		0x0000, // 5C : PAGE_UP
		0x0000, // 5D : PAGE_DOWN
		0x0000, // 5E : PICTSYMBOLS
		0x0000, // 5F : SWITCH_CHARSET
		0x0000, // 60 : BUTTON_A
		0x0000, // 61 : BUTTON_B
		0x0000, // 62 : BUTTON_C
		0x0000, // 63 : BUTTON_X
		0x0000, // 64 : BUTTON_Y
		0x0000, // 65 : BUTTON_Z
		0x0000, // 66 : BUTTON_L1
		0x0000, // 67 : BUTTON_R1
		0x0000, // 68 : BUTTON_L2
		0x0000, // 69 : BUTTON_R2
		0x0000, // 6A : BUTTON_THUMBL
		0x0000, // 6B : BUTTON_THUMBR
		0x0000, // 6C : BUTTON_START
		0x0000, // 6D : BUTTON_SELECT
		0x0000, // 6E : BUTTON_MODE
	};
	
	private static final int keyCodeUs2JisHalfKatakanaShiftCharTable[] = {
		0x0000, // 00 : UNKNOWN
		0x0000, // 01 : SOFT_LEFT
		0x0000, // 02 : SOFT_RIGHT
		0x0000, // 03 : HOME
		0x0000, // 04 : BACK
		0x0000, // 05 : CALL
		0x0000, // 06 : ENDCALL
		0xff66, // 07 : 0
		0xff87, // 08 : 1
		0xff8c, // 09 : 2
		0xff67, // 0A : 3
		0xff69, // 0B : 4
		0xff6a, // 0C : 5
		0xff6b, // 0D : 6
		0xff6c, // 0E : 7
		0xff6d, // 0F : 8
		0xff6e, // 10 : 9
		0x0000, // 11 : STAR
		0x0000, // 12 : POUND
		0x0000, // 13 : DPAD_UP
		0x0000, // 14 : DPAD_DOWN
		0x0000, // 15 : DPAD_LEFT
		0x0000, // 16 : DPAD_RIGHT
		0x0000, // 17 : DPAD_CENTER
		0x0000, // 18 : VOLUME_UP
		0x0000, // 19 : VOLUME_DOWN
		0x0000, // 1A : POWER
		0x0000, // 1B : CAMERA
		0x0000, // 1C : CLEAR
		0xff81, // 1D : A
		0xff7a, // 1E : B
		0xff7f, // 1F : C
		0xff7c, // 20 : D
		0xff68, // 21 : E
		0xff8a, // 22 : F
		0xff77, // 23 : G
		0xff78, // 24 : H
		0xff86, // 25 : I
		0xff8f, // 26 : J
		0xff89, // 27 : K
		0xff98, // 28 : L
		0xff93, // 29 : M
		0xff90, // 2A : N
		0xff97, // 2B : O
		0xff7e, // 2C : P
		0xff80, // 2D : Q
		0xff7d, // 2E : R
		0xff84, // 2F : S
		0xff76, // 30 : T
		0xff85, // 31 : U
		0xff8b, // 32 : V
		0xff83, // 33 : W
		0xff7b, // 34 : X
		0xff9d, // 35 : Y
		0xff6f, // 36 : Z
		0xff64, // 37 : COMMA
		0xff61, // 38 : PERIOD
		0x0000, // 39 : ALT_LEFT
		0x0000, // 3A : ALT_RIGHT
		0x0000, // 3B : SHIFT_LEFT
		0x0000, // 3C : SHIFT_RIGHT
		0x0000, // 3D : TAB
		0x0000, // 3E : SPACE
		0x0000, // 3F : SYM
		0x0000, // 40 : EXPLORER
		0x0000, // 41 : ENVELOPE
		0x0000, // 42 : ENTER
		0x0000, // 43 : DEL
		0x0000, // 44 : GRAVE
		0xff70, // 45 : MINUS
		0xff8d, // 46 : EQUALS
		0xff62, // 47 : LEFT_BRACKET
		0xff63, // 48 : RIGHT_BRACKET
		0xff91, // 49 : BACKSLASH
		0xff9a, // 4A : SEMICOLON
		0xff79, // 4B : APOSTROPHE
		0xff65, // 4C : SLASH
		0x0000, // 4D : AT
		0x0000, // 4E : NUM
		0x0000, // 4F : HEADSETHOOK
		0x0000, // 50 : FOCUS
		0x0000, // 51 : PLUS
		0x0000, // 52 : MENU
		0x0000, // 53 : NOTIFICATION
		0x0000, // 54 : SEARCH
		0x0000, // 55 : MEDIA_PLAY_PAUSE
		0x0000, // 56 : MEDIA_STOP
		0x0000, // 57 : MEDIA_NEXT
		0x0000, // 58 : MEDIA_PREVIOUS
		0x0000, // 59 : MEDIA_REWIND
		0x0000, // 5A : MEDIA_FAST_FORWARD
		0x0000, // 5B : MUTE
		0x0000, // 5C : PAGE_UP
		0x0000, // 5D : PAGE_DOWN
		0x0000, // 5E : PICTSYMBOLS
		0x0000, // 5F : SWITCH_CHARSET
		0x0000, // 60 : BUTTON_A
		0x0000, // 61 : BUTTON_B
		0x0000, // 62 : BUTTON_C
		0x0000, // 63 : BUTTON_X
		0x0000, // 64 : BUTTON_Y
		0x0000, // 65 : BUTTON_Z
		0x0000, // 66 : BUTTON_L1
		0x0000, // 67 : BUTTON_R1
		0x0000, // 68 : BUTTON_L2
		0x0000, // 69 : BUTTON_R2
		0x0000, // 6A : BUTTON_THUMBL
		0x0000, // 6B : BUTTON_THUMBR
		0x0000, // 6C : BUTTON_START
		0x0000, // 6D : BUTTON_SELECT
		0x0000, // 6E : BUTTON_MODE
	};
	
	private static final int keyCodeJis2JisHalfKatakanaCharTable[] = {
		0x0000, // 00 : UNKNOWN
		0x0000, // 01 : SOFT_LEFT
		0x0000, // 02 : SOFT_RIGHT
		0x0000, // 03 : HOME
		0x0000, // 04 : BACK
		0x0000, // 05 : CALL
		0x0000, // 06 : ENDCALL
		0xff9c, // 07 : 0
		0xff87, // 08 : 1
		0xff8c, // 09 : 2
		0xff71, // 0A : 3
		0xff73, // 0B : 4
		0xff74, // 0C : 5
		0xff75, // 0D : 6
		0xff94, // 0E : 7
		0xff95, // 0F : 8
		0xff96, // 10 : 9
		0x0000, // 11 : STAR
		0x0000, // 12 : POUND
		0x0000, // 13 : DPAD_UP
		0x0000, // 14 : DPAD_DOWN
		0x0000, // 15 : DPAD_LEFT
		0x0000, // 16 : DPAD_RIGHT
		0x0000, // 17 : DPAD_CENTER
		0x0000, // 18 : VOLUME_UP
		0x0000, // 19 : VOLUME_DOWN
		0x0000, // 1A : POWER
		0x0000, // 1B : CAMERA
		0x0000, // 1C : CLEAR
		0xff81, // 1D : A
		0xff7a, // 1E : B
		0xff7f, // 1F : C
		0xff7c, // 20 : D
		0xff72, // 21 : E
		0xff8a, // 22 : F
		0xff77, // 23 : G
		0xff78, // 24 : H
		0xff86, // 25 : I
		0xff8f, // 26 : J
		0xff89, // 27 : K
		0xff98, // 28 : L
		0xff93, // 29 : M
		0xff90, // 2A : N
		0xff97, // 2B : O
		0xff7e, // 2C : P
		0xff80, // 2D : Q
		0xff7d, // 2E : R
		0xff84, // 2F : S
		0xff76, // 30 : T
		0xff85, // 31 : U
		0xff8b, // 32 : V
		0xff83, // 33 : W
		0xff7b, // 34 : X
		0xff9d, // 35 : Y
		0xff82, // 36 : Z
		0xff88, // 37 : COMMA
		0xff99, // 38 : PERIOD
		0x0000, // 39 : ALT_LEFT
		0x0000, // 3A : ALT_RIGHT
		0x0000, // 3B : SHIFT_LEFT
		0x0000, // 3C : SHIFT_RIGHT
		0x0000, // 3D : TAB
		0x0000, // 3E : SPACE
		0x0000, // 3F : SYM
		0x0000, // 40 : EXPLORER
		0x0000, // 41 : ENVELOPE
		0x0000, // 42 : ENTER
		0x0000, // 43 : DEL
		0xff70, /* 0x0000, */ // 44 : GRAVE
		0xff8e, // 45 : MINUS
		0xff8d, // 46 : EQUALS
		0xff9f, /* 0xff9e, */ // 47 : LEFT_BRACKET
		0xff91, /* 0xff9f, */ // 48 : RIGHT_BRACKET
		0xff9b, /* 0xff91, */ // 49 : BACKSLASH
		0xff9a, // 4A : SEMICOLON
		0xff79, // 4B : APOSTROPHE
		0xff92, // 4C : SLASH
		0xff9e, /* 0x0000, */ // 4D : AT
		0x0000, // 4E : NUM
		0x0000, // 4F : HEADSETHOOK
		0x0000, // 50 : FOCUS
		0x0000, // 51 : PLUS
		0x0000, // 52 : MENU
		0x0000, // 53 : NOTIFICATION
		0x0000, // 54 : SEARCH
		0x0000, // 55 : MEDIA_PLAY_PAUSE
		0x0000, // 56 : MEDIA_STOP
		0x0000, // 57 : MEDIA_NEXT
		0x0000, // 58 : MEDIA_PREVIOUS
		0x0000, // 59 : MEDIA_REWIND
		0x0000, // 5A : MEDIA_FAST_FORWARD
		0x0000, // 5B : MUTE
		0x0000, // 5C : PAGE_UP
		0x0000, // 5D : PAGE_DOWN
		0x0000, // 5E : PICTSYMBOLS
		0x0000, // 5F : SWITCH_CHARSET
		0x0000, // 60 : BUTTON_A
		0x0000, // 61 : BUTTON_B
		0x0000, // 62 : BUTTON_C
		0x0000, // 63 : BUTTON_X
		0x0000, // 64 : BUTTON_Y
		0x0000, // 65 : BUTTON_Z
		0x0000, // 66 : BUTTON_L1
		0x0000, // 67 : BUTTON_R1
		0x0000, // 68 : BUTTON_L2
		0x0000, // 69 : BUTTON_R2
		0x0000, // 6A : BUTTON_THUMBL
		0x0000, // 6B : BUTTON_THUMBR
		0x0000, // 6C : BUTTON_START
		0x0000, // 6D : BUTTON_SELECT
		0x0000, // 6E : BUTTON_MODE
	};
	
	private static final int keyCodeJis2JisHalfKatakanaShiftCharTable[] = {
		0x0000, // 00 : UNKNOWN
		0x0000, // 01 : SOFT_LEFT
		0x0000, // 02 : SOFT_RIGHT
		0x0000, // 03 : HOME
		0x0000, // 04 : BACK
		0x0000, // 05 : CALL
		0x0000, // 06 : ENDCALL
		0xff66, // 07 : 0
		0xff87, // 08 : 1
		0xff8c, // 09 : 2
		0xff67, // 0A : 3
		0xff69, // 0B : 4
		0xff6a, // 0C : 5
		0xff6b, // 0D : 6
		0xff6c, // 0E : 7
		0xff6d, // 0F : 8
		0xff6e, // 10 : 9
		0x0000, // 11 : STAR
		0x0000, // 12 : POUND
		0x0000, // 13 : DPAD_UP
		0x0000, // 14 : DPAD_DOWN
		0x0000, // 15 : DPAD_LEFT
		0x0000, // 16 : DPAD_RIGHT
		0x0000, // 17 : DPAD_CENTER
		0x0000, // 18 : VOLUME_UP
		0x0000, // 19 : VOLUME_DOWN
		0x0000, // 1A : POWER
		0x0000, // 1B : CAMERA
		0x0000, // 1C : CLEAR
		0xff81, // 1D : A
		0xff7a, // 1E : B
		0xff7f, // 1F : C
		0xff7c, // 20 : D
		0xff68, // 21 : E
		0xff8a, // 22 : F
		0xff77, // 23 : G
		0xff78, // 24 : H
		0xff86, // 25 : I
		0xff8f, // 26 : J
		0xff89, // 27 : K
		0xff98, // 28 : L
		0xff93, // 29 : M
		0xff90, // 2A : N
		0xff97, // 2B : O
		0xff7e, // 2C : P
		0xff80, // 2D : Q
		0xff7d, // 2E : R
		0xff84, // 2F : S
		0xff76, // 30 : T
		0xff85, // 31 : U
		0xff8b, // 32 : V
		0xff83, // 33 : W
		0xff7b, // 34 : X
		0xff9d, // 35 : Y
		0xff6f, // 36 : Z
		0xff64, // 37 : COMMA
		0xff61, // 38 : PERIOD
		0x0000, // 39 : ALT_LEFT
		0x0000, // 3A : ALT_RIGHT
		0x0000, // 3B : SHIFT_LEFT
		0x0000, // 3C : SHIFT_RIGHT
		0x0000, // 3D : TAB
		0x0000, // 3E : SPACE
		0x0000, // 3F : SYM
		0x0000, // 40 : EXPLORER
		0x0000, // 41 : ENVELOPE
		0x0000, // 42 : ENTER
		0x0000, // 43 : DEL
		0xff5c, /* 0x0000, */ // 44 : GRAVE
		0xff8e, // 45 : MINUS
		0xff8d, // 46 : EQUALS
		0xff62, /* 0xff9e, */ // 47 : LEFT_BRACKET
		0xff63, /* 0xff62, */ // 48 : RIGHT_BRACKET
		0xff9b, /* 0xff63, */ // 49 : BACKSLASH
		0xff9a, // 4A : SEMICOLON
		0xff79, // 4B : APOSTROPHE
		0xff65, // 4C : SLASH
		0xff9e, /* 0x0000, */ // 4D : AT
		0x0000, // 4E : NUM
		0x0000, // 4F : HEADSETHOOK
		0x0000, // 50 : FOCUS
		0x0000, // 51 : PLUS
		0x0000, // 52 : MENU
		0x0000, // 53 : NOTIFICATION
		0x0000, // 54 : SEARCH
		0x0000, // 55 : MEDIA_PLAY_PAUSE
		0x0000, // 56 : MEDIA_STOP
		0x0000, // 57 : MEDIA_NEXT
		0x0000, // 58 : MEDIA_PREVIOUS
		0x0000, // 59 : MEDIA_REWIND
		0x0000, // 5A : MEDIA_FAST_FORWARD
		0x0000, // 5B : MUTE
		0x0000, // 5C : PAGE_UP
		0x0000, // 5D : PAGE_DOWN
		0x0000, // 5E : PICTSYMBOLS
		0x0000, // 5F : SWITCH_CHARSET
		0x0000, // 60 : BUTTON_A
		0x0000, // 61 : BUTTON_B
		0x0000, // 62 : BUTTON_C
		0x0000, // 63 : BUTTON_X
		0x0000, // 64 : BUTTON_Y
		0x0000, // 65 : BUTTON_Z
		0x0000, // 66 : BUTTON_L1
		0x0000, // 67 : BUTTON_R1
		0x0000, // 68 : BUTTON_L2
		0x0000, // 69 : BUTTON_R2
		0x0000, // 6A : BUTTON_THUMBL
		0x0000, // 6B : BUTTON_THUMBR
		0x0000, // 6C : BUTTON_START
		0x0000, // 6D : BUTTON_SELECT
		0x0000, // 6E : BUTTON_MODE
	};
	
	private int getJisHalfKatakanaCharByEvent(final KeyEvent ev, final boolean shift) {
		int keyCode = ev.getKeyCode();
		if (keyCode < 0) {
			switch (keyCode) {
				case DefaultSoftKeyboard.KEYCODE_JIS_RO:
					return 0xff9b;
				case DefaultSoftKeyboard.KEYCODE_JIS_CHOUON:
					return 0xff70;
			}
		} else if (keyCode < keyCodeUs2JisHalfKatakanaCharTable.length) {
			if (shift || ev.isShiftPressed()) {
				switch (mUseConvertKeyMap) {
					case CONVERT_KEYMAP_KB_US_OS_US:
						return keyCodeUs2JisHalfKatakanaShiftCharTable[keyCode];
					case CONVERT_KEYMAP_KB_JIS_OS_US:
						return keyCodeUs2JisHalfKatakanaShiftCharTable[keyCode];
					default:
						return keyCodeJis2JisHalfKatakanaShiftCharTable[keyCode];
				}
			}
			switch (mUseConvertKeyMap) {
				case CONVERT_KEYMAP_KB_US_OS_US:
					return keyCodeUs2JisHalfKatakanaCharTable[keyCode];
				case CONVERT_KEYMAP_KB_JIS_OS_US:
					return keyCodeUs2JisHalfKatakanaCharTable[keyCode];
				default:
					return keyCodeJis2JisHalfKatakanaCharTable[keyCode];
			}
		}
		return 0;
	}
	
	private int getJisFullAlphabetCharByEvent(final KeyEvent ev, final boolean shift) {
		int c = getUnicodeChar_with_ConvertKeyMap(ev, (shift || ev.isShiftPressed()) ? MetaKeyKeyListener.META_SHIFT_ON : 0);
		if (c == 0x5c) {
			c = 0xffe5;  // "￥"
		} else if ((0x20 <= c) && (c <= 0x7e)) {
			c += 0xfee0;
		} else {
			c = 0;
		}
		return c;
	}
	
	private static final int charCode_JIS_on_US_Table[] = {
		0x20, 0x21, 0x2a, 0x23,  0x24, 0x25, 0x27, 0x3a,  0x29, 0x00, 0x28, 0x7e,  0x2c, 0x2d, 0x2e, 0x2f, 
		0x30, 0x31, 0x32, 0x33,  0x34, 0x35, 0x36, 0x37,  0x38, 0x39, 0x2b, 0x3b,  0x3c, 0x5e, 0x3e, 0x3f, 
		0x22, 0x41, 0x42, 0x43,  0x44, 0x45, 0x46, 0x47,  0x48, 0x49, 0x4a, 0x4b,  0x4c, 0x4d, 0x4e, 0x4f, 
		0x50, 0x51, 0x52, 0x53,  0x54, 0x55, 0x56, 0x57,  0x58, 0x59, 0x5a, 0x40,  0x5d, 0x5b, 0x26, 0x3d, 
		0x00, 0x61, 0x62, 0x63,  0x64, 0x65, 0x66, 0x67,  0x68, 0x69, 0x6a, 0x6b,  0x6c, 0x6d, 0x6e, 0x6f, 
		0x70, 0x71, 0x72, 0x73,  0x74, 0x75, 0x76, 0x77,  0x78, 0x79, 0x7a, 0x60,  0x7d, 0x7b, 0x3d, 0x00, 
	};
	private int getUnicodeChar_with_ConvertKeyMap(final KeyEvent ev, int meta) {
		int keycode = ev.getKeyCode();
		int c;
		switch (keycode) {
			case DefaultSoftKeyboard.KEYCODE_JIS_RO:
				if ((meta & KeyEvent.META_SHIFT_ON) != 0) {
					return 0x5f;  // "_"
				}
				return 0x5c;  // "\"

			case DefaultSoftKeyboard.KEYCODE_JIS_CHOUON:
				if ((meta & KeyEvent.META_SHIFT_ON) != 0) {
					return 0x7c;  // "|"
				}
				return 0x5c;  // "\"

			default:
				c = ev.getUnicodeChar(meta);
				if ((0x20 <= c) && (c <= 0x7e)) {
					switch (mUseConvertKeyMap) {
						case CONVERT_KEYMAP_KB_JIS_OS_US:
							return charCode_JIS_on_US_Table[c-0x20];
					}
				}
				return c;
		}
	}
	
	/**
	 * Key event handler.
	 *
	 * @param ev        A key event
	 * @return  {@code true} if the event is handled in this method.
	 */
	private boolean processKeyEvent(final KeyEvent ev) {
		final int key = ev.getKeyCode();
		final DefaultSoftKeyboard inputManager = ((DefaultSoftKeyboard) mInputViewManager);

		/* keys which produce a glyph */
		if (ev.isPrintingKey() || (key == DefaultSoftKeyboard.KEYCODE_JIS_RO) || (key == DefaultSoftKeyboard.KEYCODE_JIS_CHOUON)) {
			/* do nothing if the character is not able to display or the character is dead key */
			if (((mHardShift > 0) && (mHardAlt > 0) && (mHardCtrl > 0)) || (ev.isAltPressed() && ev.isShiftPressed() && isCtrlPressed(ev))) {
				// final int charCode = ev.getUnicodeChar(MetaKeyKeyListener.META_SHIFT_ON | MetaKeyKeyListener.META_ALT_ON);
				final int charCode = getUnicodeChar_with_ConvertKeyMap(ev, MetaKeyKeyListener.META_SHIFT_ON | MetaKeyKeyListener.META_ALT_ON);
				if ((charCode == 0) || ((charCode & KeyCharacterMap.COMBINING_ACCENT) != 0) || (charCode == PRIVATE_AREA_CODE)) {
					if(mHardAlt == 1){
						mAltPressing   = false;
					}
					if(mHardShift == 1){
						mShiftPressing = false;
					}
					if(mHardCtrl == 1){
						mCtrlPressing = false;
					}
					if(!ev.isAltPressed()){
						if (mHardAlt == 1) {
							mHardAlt = 0;
							mInputConnection.clearMetaKeyStates(KeyEvent.META_ALT_ON|KeyEvent.META_ALT_LEFT_ON|KeyEvent.META_ALT_RIGHT_ON);
						}
					}
					if(!ev.isShiftPressed()){
						if (mHardShift == 1) {
							mHardShift = 0;
							mInputConnection.clearMetaKeyStates(KeyEvent.META_SHIFT_ON|KeyEvent.META_SHIFT_LEFT_ON|KeyEvent.META_SHIFT_RIGHT_ON);
						}
					}
					if(!isCtrlPressed(ev)){
						if (mHardCtrl == 1) {
							mHardCtrl = 0;
							// mInputConnection.clearMetaKeyStates(KeyEvent.META_CTRL_ON|KeyEvent.META_CTRL_LEFT_ON|KeyEvent.META_CTRL_RIGHT_ON);
							mInputConnection.clearMetaKeyStates(0x00001000|0x00002000|0x00004000);
						}
					}
					if(!ev.isShiftPressed() && !ev.isAltPressed() && !isCtrlPressed(ev)){
						updateMetaKeyStateDisplay();
					}
					return true;
				}
			}

			commitConvertingText();

			final EditorInfo edit = getCurrentInputEditorInfo();
			StrSegment str;

			/* get the key character */
			// Log.d("nicoWnnG", "processKeyEvent(): "+String.format("keyCode=%d", ev.getKeyCode()));
			if ((mHardShift== 0) && (mHardAlt == 0)) {
				/* no meta key is locked */
				final int shift = (mAutoCaps)? getShiftKeyState(edit) : 0;
				if ((shift != mHardShift) && ((key >= KeyEvent.KEYCODE_A) && (key <= KeyEvent.KEYCODE_Z))) {
					/* handling auto caps for a alphabet character */
					if (inputManager.isJisFullHiraganaMode()) {
						str = createStrSegment(getJisFullHiraganaCharByEvent(ev, true));
					} else if (inputManager.isJisFullKatakanaMode()) {
						str = createStrSegment(getJisFullKatakanaCharByEvent(ev, true));
					} else if (inputManager.isJisHalfKatakanaMode()) {
						str = createStrSegment(getJisHalfKatakanaCharByEvent(ev, true));
					} else if (inputManager.isJisFullAlphabetMode()) {
						str = createStrSegment(getJisFullAlphabetCharByEvent(ev, true));
					} else {
						// str = createStrSegment(ev.getUnicodeChar(MetaKeyKeyListener.META_SHIFT_ON));
						str = createStrSegment(getUnicodeChar_with_ConvertKeyMap(ev, MetaKeyKeyListener.META_SHIFT_ON));
					}
				} else {
					if (inputManager.isJisFullHiraganaMode()) {
						str = createStrSegment(getJisFullHiraganaCharByEvent(ev, false));
					} else if (inputManager.isJisFullKatakanaMode()) {
						str = createStrSegment(getJisFullKatakanaCharByEvent(ev, false));
					} else if (inputManager.isJisHalfKatakanaMode()) {
						str = createStrSegment(getJisHalfKatakanaCharByEvent(ev, false));
					} else if (inputManager.isJisFullAlphabetMode()) {
						str = createStrSegment(getJisFullAlphabetCharByEvent(ev, false));
					} else {
						// str = createStrSegment(ev.getUnicodeChar));
						str = createStrSegment(getUnicodeChar_with_ConvertKeyMap(ev, ev.getMetaState()));
					}
				}
			} else {
				if (inputManager.isJisFullHiraganaMode()) {
					str = createStrSegment(getJisFullHiraganaCharByEvent(ev, (mShiftKeyToggle[mHardShift] != 0)));
				} else if (inputManager.isJisFullKatakanaMode()) {
					str = createStrSegment(getJisFullKatakanaCharByEvent(ev, (mShiftKeyToggle[mHardShift] != 0)));
				} else if (inputManager.isJisHalfKatakanaMode()) {
					str = createStrSegment(getJisHalfKatakanaCharByEvent(ev, (mShiftKeyToggle[mHardShift] != 0)));
				} else if (inputManager.isJisFullAlphabetMode()) {
					str = createStrSegment(getJisFullAlphabetCharByEvent(ev, (mShiftKeyToggle[mHardShift] != 0)));
				} else {
					int meta = mShiftKeyToggle[mHardShift] | (mUseHardAltShift ? mAltKeyToggle[mHardAlt] : 0);
					// str = createStrSegment(ev.getUnicodeChar(meta));
					str = createStrSegment(getUnicodeChar_with_ConvertKeyMap(ev, meta));
				}
				if(mHardShift == 1){
					mShiftPressing = false;
				}
				if(mHardAlt == 1){
					mAltPressing   = false;
				}
				if(mHardCtrl == 1){
					mCtrlPressing   = false;
				}
				/* back to 0 (off) if 1 (on/not locked) */
				if (!ev.isAltPressed()) {
					if (mHardAlt == 1) {
						mHardAlt = 0;
						mInputConnection.clearMetaKeyStates(KeyEvent.META_ALT_ON|KeyEvent.META_ALT_LEFT_ON|KeyEvent.META_ALT_RIGHT_ON);
					}
				}
				if (!ev.isShiftPressed()) {
					if (mHardShift == 1) {
						mHardShift = 0;
						mInputConnection.clearMetaKeyStates(KeyEvent.META_SHIFT_ON|KeyEvent.META_SHIFT_LEFT_ON|KeyEvent.META_SHIFT_RIGHT_ON);
					}
				}
				if (!isCtrlPressed(ev)) {
					if (mHardCtrl == 1) {
						mHardCtrl = 0;
						// mInputConnection.clearMetaKeyStates(KeyEvent.META_SHIFT_ON|KeyEvent.META_SHIFT_LEFT_ON|KeyEvent.META_SHIFT_RIGHT_ON);
						mInputConnection.clearMetaKeyStates(0x00001000|0x00002000|0x00004000);
					}
				}
				if (!ev.isShiftPressed() && !ev.isShiftPressed() && !isCtrlPressed(ev)) {
					updateMetaKeyStateDisplay();
				}
			}

			if (str == null) {
				return true;
			}

			/* append the character to the composing text if the character is not TAB */
			if (str.string.charAt(0) == '\u0009') {
				commitText(true);
				commitText(str.string);
				initializeScreen();
				return true;
			} else if (str.string.charAt(0) == '\u309b') {
				// DAKUTEN
				if (inputManager != null) {
					inputManager.onKey(DefaultSoftKeyboard.KEYCODE_DAKUTEN, null);
				}
				return true;
			} else if (str.string.charAt(0) == '\u309c') {
				// HANDAKUTEN
				if (inputManager != null) {
					inputManager.onKey(DefaultSoftKeyboard.KEYCODE_HANDAKUTEN, null);
				}
				return true;
			}
			processHardwareKeyboardInputChar(str);
			return true;

		} else if (key == KeyEvent.KEYCODE_SPACE) {
			/* H/W space key */
			if (!isCandidatesViewShown() || !candidatesViewManagerIsIndicated()) {
				processHardwareKeyboardSpaceKey(ev);
				return true;
			}

		} else if (key == KeyEvent.KEYCODE_SYM) {
			/* display the symbol list */
			if (mPreConverter != null) {
				mPreConverter.convert2(mComposingText);
			}
			initCommitInfoForWatchCursor();
			mStatus = commitText(true);
			checkCommitInfo();
			changeEngineMode(ENGINE_MODE_SYMBOL, 0);
			mHardAlt = 0;
			mHardCtrl = 0;
			updateMetaKeyStateDisplay();
			return true;
		} else if ((mIs01Enable) && (key == DefaultSoftKeyboard.KEYCODE_IS01_MOJI)) {  // KeyEvent.KEYCODE_IS01_MOJI
			/* IS01 [moji] key */
			processHardwareKeyboardIS01MojiKey(ev);
			return true;
		} else if ((mIs01Enable) && (key == DefaultSoftKeyboard.KEYCODE_IS01_E_KAO_KI)) {  // KeyEvent.KEYCODE_IS01_E_KAO_KI
			/* IS01 [e/kao/ki] key */
			processHardwareKeyboardIS01EKaoKiKey(ev);
			return true;
		} else if ((mIs01Enable) && (key == DefaultSoftKeyboard.KEYCODE_IS11SH_MOJI)) {  // KeyEvent.KEYCODE_IS11SH_MOJI
			/* IS11SH [moji] key */
			processHardwareKeyboardIS01MojiKey(ev);
			return true;
		} else if ((mIs01Enable) && (key == DefaultSoftKeyboard.KEYCODE_IS11SH_E_KAO_KI)) {  // KeyEvent.KEYCODE_IS11SH_E_KAO_KI
			/* IS11SH [e/kao/ki] key */
			processHardwareKeyboardIS01EKaoKiKey(ev);
			return true;
		} else if ((mIs01Enable) && (key == DefaultSoftKeyboard.KEYCODE_007SH_MOJI)) {  // KeyEvent.KEYCODE_007SH_MOJI
			/* 007SH [moji] key */
			processHardwareKeyboardIS01MojiKey(ev);
			return true;
		} else if ((mIs01Enable) && (key == DefaultSoftKeyboard.KEYCODE_007SH_E_KAO_KI)) {  // KeyEvent.KEYCODE_007SH_E_KAO_KI
			/* 007SH [e/kao/ki] key */
			processHardwareKeyboardIS01EKaoKiKey(ev);
			return true;
		} else if (key == DefaultSoftKeyboard.KEYCODE_JIS_HIRA_KATA) {
			if (mPreConverter != null) {
				mPreConverter.convert2(mComposingText);
			}
			if (inputManager != null) {
				if (inputManager.mCurrentKeyMode == DefaultSoftKeyboard.KEYMODE_JA_FULL_KATAKANA) {
					inputManager.changeKeyMode(DefaultSoftKeyboard.KEYMODE_JA_FULL_HIRAGANA);
				} else {
					inputManager.changeKeyMode(DefaultSoftKeyboard.KEYMODE_JA_FULL_KATAKANA);
				}
			}
			return true;
		} else if (key == DefaultSoftKeyboard.KEYCODE_JIS_NO_CONVERT) {
			if (mPreConverter != null) {
				mPreConverter.convert2(mComposingText);
			}
			if (mEngineState.isEisuKana()) {
				onEvent(new NicoWnnGEvent(NicoWnnGEvent.INPUT_KEY, new KeyEvent(ev.getDownTime(), ev.getEventTime(), KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SPACE, ev.getRepeatCount(), ev.getMetaState())));
			} else {
				onEvent(mEventChangeModeEisuKana);
			}
			return true;
		} else if (key == DefaultSoftKeyboard.KEYCODE_JIS_PREV_CANDIDATE) {
			if (mPreConverter != null) {
				mPreConverter.convert2(mComposingText);
			}
			if (candidatesViewManagerIsShown() && candidatesViewManagerIsIndicated()) {
				onEvent(new NicoWnnGEvent(NicoWnnGEvent.INPUT_SOFT_KEY, new KeyEvent(ev.getDownTime(), ev.getEventTime(), KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SPACE, ev.getRepeatCount(), ev.getMetaState())));
			} else if (isRenbun()||isPredict()) {
				onEvent(new NicoWnnGEvent(NicoWnnGEvent.INPUT_KEY, new KeyEvent(ev.getDownTime(), ev.getEventTime(), KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SPACE, ev.getRepeatCount(), ev.getMetaState())));
			} else {
				onEvent(mEventConvert);
			}
			return true;
		}

		/* Functional key */
		if (mComposingText.size(ComposingText.LAYER1) > 0) {
			switch (key) {
				case KeyEvent.KEYCODE_DEL:
					mStatus = STATUS_INPUT_EDIT;
					if (mEngineState.isConvertState()) {
						mComposingText.setCursor(ComposingText.LAYER1,
								mComposingText.toString(ComposingText.LAYER1).length());
						mExactMatchMode = false;
					} else {
						if (mComposingText.size(ComposingText.LAYER1) == 1) {
							initializeScreen();
							return true;
						} else {
							mComposingText.delete(ComposingText.LAYER1, false);
						}
					}
					// updateViewStatusForPrediction(true, true, true);
					updateViewStatusForPrediction(true, true, false);
					return true;

				case KeyEvent.KEYCODE_BACK:
				case 111: // KeyEvent.KEYCODE_ESCAPE:
					if (mCandidatesViewManager.getViewType() == CandidatesViewManager.VIEW_TYPE_FULL) {
						mStatus &= ~STATUS_CANDIDATE_FULL;
						mCandidatesViewManager.setViewType(CandidatesViewManager.VIEW_TYPE_NORMAL);
					} else {
						if (!mEngineState.isConvertState()) {
							initializeScreen();
							if (mConverter != null) {
								mConverter.init();
							}
						} else {
							mCandidatesViewManager.clearCandidates();
							mDocomoEmojiCount = 0;
							resetPrediction();
							mStatus = STATUS_INPUT_EDIT;
							mExactMatchMode = false;
							mComposingText.setCursor(ComposingText.LAYER1,
									mComposingText.toString(ComposingText.LAYER1).length());
							updateViewStatusForPrediction(true, true, true);
						}
					}
					return true;

				case KeyEvent.KEYCODE_DPAD_UP:
					if (!isEnableL2Converter()) {
						commitText(false);
						return false;
					}
					if ((mPreConverter != null) && !(mEngineState.isRenbun()||mEngineState.isPredict())) {
						if ((mComposingText.size(0) != 0)) {
							if (inputManager != null) {
								int keycode = DefaultSoftKeyboard.KEYCODE_JP12_CONVPREDICT_BACKWARD;
								switch (mPredictionMode) {
									case kPREDICTION_OFF:
										keycode = DefaultSoftKeyboard.KEYCODE_JP12_SPACE;
										break;
									case kPREDICTION_HENKAN:
										if (isCandidatesViewShown() && candidatesViewManagerIsIndicated()) {
											WnnWord s = mCandidatesViewManager.getUpWnnWordCandidate();
											processChangeWnnWordCandidate(s);
											return true;
										}
										if (!mActionPrediction) {
											keycode = DefaultSoftKeyboard.KEYCODE_JP12_SPACE;
										}
										break;
								}
								inputManager.onKey(
									keycode,
									null
								);
								if (keycode == DefaultSoftKeyboard.KEYCODE_JP12_SPACE) {
									WnnWord s;
									s = mCandidatesViewManager.getUpWnnWordCandidate();
									processChangeWnnWordCandidate(s);
								}
								return true;
							}
							// break;
						}
						return true;
					}
					{
						WnnWord s;
						s = mCandidatesViewManager.getUpWnnWordCandidate();
						processChangeWnnWordCandidate(s);
					}
					return true;

				case KeyEvent.KEYCODE_DPAD_DOWN:
					if (!isEnableL2Converter()) {
						commitText(false);
						return false;
					}
					if ((mPreConverter != null) && !(mEngineState.isRenbun()||mEngineState.isPredict())) {
						if ((mComposingText.size(0) != 0)) {
							if (inputManager != null) {
								int keycode = DefaultSoftKeyboard.KEYCODE_JP12_CONVPREDICT;
								switch (mPredictionMode) {
									case kPREDICTION_OFF:
										keycode = DefaultSoftKeyboard.KEYCODE_JP12_SPACE;
										break;
									case kPREDICTION_HENKAN:
										if (isCandidatesViewShown() && candidatesViewManagerIsIndicated()) {
											WnnWord s = mCandidatesViewManager.getDownWnnWordCandidate();
											processChangeWnnWordCandidate(s);
											return true;
										}
										if (!mActionPrediction) {
											keycode = DefaultSoftKeyboard.KEYCODE_JP12_SPACE;
										}
										break;
								}
								inputManager.onKey(
									keycode,
									null
								);
								return true;
							}
							// break;
						}
						return true;
					}
					{
						WnnWord s;
						s = mCandidatesViewManager.getDownWnnWordCandidate();
						processChangeWnnWordCandidate(s);
					}
					return true;

				case KeyEvent.KEYCODE_SPACE:
					if (!isEnableL2Converter()) {
						commitText(false);
						return false;
					}
					{
						WnnWord s;
						if (inputWithShift(ev)) {
							s = mCandidatesViewManager.getPrevWnnWordCandidate();
						} else {
							s = mCandidatesViewManager.getNextWnnWordCandidate();
						}
						processChangeWnnWordCandidate(s);
					}
					return true;

/*
				case KeyEvent.KEYCODE_DPAD_LEFT:
					if (!isEnableL2Converter()) {
						if (mEngineState.keyboard == EngineState.KEYBOARD_12KEY) {
							commitText(false);
						}
					} else {
						processLeftKeyEvent();
					}
					return true;
				case KeyEvent.KEYCODE_DPAD_RIGHT:
					if (!isEnableL2Converter()) {
						if (mEngineState.keyboard == EngineState.KEYBOARD_12KEY) {
							commitText(false);
						}
					} else {
						processRightKeyEvent();
					}
					return true;
*/
				case KeyEvent.KEYCODE_DPAD_LEFT:
					if (!isEnableL2Converter()) {
						commitText(false);
						return false;
					}
					if (mUseLeftRightKeyCandidateSelection && isCandidatesViewShown() && (mCandidatesViewManager.getIndicateCandidateView() > 0)) {
						if (!inputWithShift(ev)) {
							WnnWord s;
							s = mCandidatesViewManager.getLeftWnnWordCandidate();
							processChangeWnnWordCandidate(s);
							return true;
						}
					}
					processLeftKeyEvent();
					return true;
				case KeyEvent.KEYCODE_DPAD_RIGHT:
					if (!isEnableL2Converter()) {
						commitText(false);
						return false;
					}
					if (mUseLeftRightKeyCandidateSelection && isCandidatesViewShown() && (mCandidatesViewManager.getIndicateCandidateView() > 0)) {
						if (!inputWithShift(ev)) {
							WnnWord s;
							s = mCandidatesViewManager.getRightWnnWordCandidate();
							processChangeWnnWordCandidate(s);
							return true;
						}
					}
					processRightKeyEvent();
					return true;

				case DefaultSoftKeyboard.KEYCODE_JP12_REVERSE:
					if (!isEnableL2Converter()) {
						commitText(false);
						return false;
					}
					if (isCandidatesViewShown() && (mCandidatesViewManager.getIndicateCandidateView() > 0)) {
						WnnWord s;
						s = mCandidatesViewManager.getPrevWnnWordCandidate();
						processChangeWnnWordCandidate(s);
						return true;
					}
					break;


				case KeyEvent.KEYCODE_DPAD_CENTER:
				case KeyEvent.KEYCODE_ENTER:
					if (!isEnglishPrediction()) {
						final int cursor = mComposingText.getCursor(ComposingText.LAYER1);
						if (cursor < 1) {
							return true;
						}
					}
					initCommitInfoForWatchCursor();
//					mStatus = commitText(mEngineState.convertType != EngineState.CONVERT_TYPE_PREDICT);
					mStatus = commitText(true);
					checkCommitInfo();

					if (isEnglishPrediction() || !mEnablePredictionAfterEnter) {
						if (mComposingText.size(ComposingText.LAYER0) == 0) {
							initializeScreen();
						}
					}
					if (mComposingText.size(ComposingText.LAYER0) != 0) {
						mIndicateAfterCommit = true;
					}

					if (mEnableAutoHideKeyboard) {
						mInputViewManager.closing();
						requestHideSelf(0);
					}
					return true;

				case KeyEvent.KEYCODE_CALL:
					return false;

				default:
					return true;
			}
		} else {
			/* if there is no composing string. */
			if (mCandidatesViewManager.getCurrentView().isShown()) {
				/* displaying relational prediction candidates */
				switch (key) {
					case KeyEvent.KEYCODE_DPAD_LEFT:
						if (mUseLeftRightKeyCandidateSelection && isCandidatesViewShown() && (mCandidatesViewManager.getIndicateCandidateView() > 0)) {
							final WnnWord s = mCandidatesViewManager.getLeftWnnWordCandidate();
							if (s != null) {
								updateViewStatusForPrediction(false, true, true);
								return true;
							}
						}
						if (isEnableL2Converter()) {
							/* initialize the converter */
							mConverter.init();
						}
						mStatus = STATUS_INPUT_EDIT;
						updateViewStatusForPrediction(true, true, true);
						return false;

					case KeyEvent.KEYCODE_DPAD_RIGHT:
						if (mUseLeftRightKeyCandidateSelection && isCandidatesViewShown() && (mCandidatesViewManager.getIndicateCandidateView() > 0)) {
							final WnnWord s = mCandidatesViewManager.getRightWnnWordCandidate();
							if (s != null) {
								updateViewStatusForPrediction(false, true, true);
								return true;
							}
						}
						if (isEnableL2Converter()) {
							/* initialize the converter */
							mConverter.init();
						}
						mStatus = STATUS_INPUT_EDIT;
						updateViewStatusForPrediction(true, true, true);
						return false;

					case KeyEvent.KEYCODE_DPAD_UP:
					{
						final WnnWord s = mCandidatesViewManager.getUpWnnWordCandidate();
						if (s != null) {
							updateViewStatusForPrediction(false, true, true);
							return true;
						}
					}
					break;

					case KeyEvent.KEYCODE_DPAD_DOWN:
					{
						final WnnWord s = mCandidatesViewManager.getDownWnnWordCandidate();
						if (s != null) {
							updateViewStatusForPrediction(false, true, true);
							return true;
						}
					}
					break;

					case DefaultSoftKeyboard.KEYCODE_JP12_REVERSE:
					{
						final WnnWord s = mCandidatesViewManager.getPrevWnnWordCandidate();
						if (s != null) {
							updateViewStatusForPrediction(false, true, true);
							return true;
						}
					}
					break;

					case KeyEvent.KEYCODE_SPACE:
					{
						final WnnWord s;
						if (!inputWithShift(ev)) {
							s = mCandidatesViewManager.getNextWnnWordCandidate();
						} else {
							s = mCandidatesViewManager.getPrevWnnWordCandidate();
						}
						if (s != null) {
							updateViewStatusForPrediction(false, true, true);
							return true;
						}
					}
					break;

					case KeyEvent.KEYCODE_DPAD_CENTER:
					case KeyEvent.KEYCODE_ENTER:
						if (candidatesViewManagerIsIndicated()) {
							final WnnWord s = mCandidatesViewManager.getNowWnnWordCandidate();
							onEvent(new NicoWnnGEvent(NicoWnnGEvent.SELECT_CANDIDATE, s));
							return true;
						}
						break;

					default:
						return processKeyEventNoInputCandidateShown(ev);
				}
			} else {
				switch (key) {
					/*
					case KeyEvent.KEYCODE_DPAD_CENTER:
					case KeyEvent.KEYCODE_ENTER:
						if (mEnableAutoHideKeyboard) {
							mInputViewManager.closing();
							requestHideSelf(0);
							return true;
						}
						break;
					*/
					case KeyEvent.KEYCODE_BACK:
						/*
						 * If 'BACK' key is pressed when the SW-keyboard is shown
						 * and the candidates view is not shown, dismiss the SW-keyboard.
						 */
					{
						if (inputManager.dismissMyPopupInputImeMode()) {
							return true;
						}
					}
						if (isInputViewShown()) {
							mInputViewManager.closing();
							requestHideSelf(0);
							return true;
						}
						break;
					default:
						break;
				}
			}
		}

		return false;
	}

	private boolean inputWithShift(final KeyEvent ev) {
		final DefaultSoftKeyboard inputManager = ((DefaultSoftKeyboard) mInputViewManager);
		final MyKeyboardView v = (MyKeyboardView)(inputManager.getKeyboardView());
		final boolean softShift = v.isShifted();
		return (ev.isShiftPressed() || softShift);
	}

	private void processChangeWnnWordCandidate(final WnnWord s) {
		if (s != null) {
			final int l0cursor = mComposingText.getCursor(ComposingText.LAYER0);
			final int l1cursor = mComposingText.getCursor(ComposingText.LAYER1);
			mComposingText.setCursor(ComposingText.LAYER2, 0);
			final StrSegment[] ss = new StrSegment[1];
			StrSegment old = mComposingText.getStrSegment(ComposingText.LAYER2, 0);
			ss[0] = new StrSegment(s.candidate, old.from, old.to);
			mComposingText.replaceStrSegment(ComposingText.LAYER2, ss);
			updateViewStatus(ComposingText.LAYER2, false, true, true);
			mComposingText.setCursorDirect(ComposingText.LAYER0, l0cursor);
			mComposingText.setCursorDirect(ComposingText.LAYER1, l1cursor);
			mComposingText.setCursorDirect(ComposingText.LAYER2, 1);
		}
	}

	/**
	 * Handle the space key event from the Hardware keyboard.
	 *
	 * @param ev  The space key event
	 */
	private void processHardwareKeyboardIS01MojiKey(final KeyEvent ev) {
		/* IS01 [moji] key */
		if (mComposingText.size(0) == 0) {
			/* change Japanese <-> English mode */
			mHardAlt = 0;
			mHardShift = 0;
			mHardCtrl = 0;
			updateMetaKeyStateDisplay();
			/*
				if (mEngineState.isEnglish()) {
			    	// English mode to Japanese mode
					//((DefaultSoftKeyboard) mInputViewManager).changeKeyMode(DefaultSoftKeyboard.KEYMODE_JA_FULL_HIRAGANA);
					((DefaultSoftKeyboard) mInputViewManager).setDefaultKeyboard();
					mConverter = mConverterJAJP;
				} else {
					// Japanese mode to English mode
					((DefaultSoftKeyboard) mInputViewManager).changeKeyMode(DefaultSoftKeyboard.KEYMODE_JA_HALF_ALPHABET);
					mConverter = mConverterEN;
				}
			 */
			// change next mode
			((DefaultSoftKeyboard) mInputViewManager).nextKeyMode();
			// reset candidate & symbol position
			mCandidatesViewManager.clearCandidates();
			mDocomoEmojiCount = 0;
			resetPrediction();
		} else {
			changeAlphaKanaDirectPhase();
		}
	}

	private void processHardwareKeyboardIS01EKaoKiKey(final KeyEvent ev) {
		/* IS01 [E/Kao/Ki] key */
		if (mPreConverter != null) {
			mPreConverter.convert2(mComposingText);
		}
		if (!mEngineState.isSymbolList() && !mEngineState.isDocomoSymbolList() && !mEngineState.isUserSymbol()) {
			commitAllText();
		}
		if (inputWithShift(ev)) {
			int mode = 0;
			switch (((DefaultSoftKeyboard)mInputViewManager).mCurrentKeyMode) {
			  case DefaultSoftKeyboard.KEYMODE_JA_FULL_NICO:
			  case DefaultSoftKeyboard.KEYMODE_JA_FULL_HIRAGANA:
				mode = DefaultSoftKeyboard.KEYCODE_USERSYMBOL_ZEN_HIRAGANA;
				break;
			  case DefaultSoftKeyboard.KEYMODE_JA_FULL_ALPHABET:
				mode = DefaultSoftKeyboard.KEYCODE_USERSYMBOL_ZEN_ALPHABET;
				break;
			  case DefaultSoftKeyboard.KEYMODE_JA_FULL_NUMBER:
				mode = DefaultSoftKeyboard.KEYCODE_USERSYMBOL_ZEN_NUMBER;
				break;
			  case DefaultSoftKeyboard.KEYMODE_JA_FULL_KATAKANA:
			  case DefaultSoftKeyboard.KEYMODE_JA_FULL_NICO_KATAKANA:
				mode = DefaultSoftKeyboard.KEYCODE_USERSYMBOL_ZEN_KATAKANA;
				break;
			  case DefaultSoftKeyboard.KEYMODE_JA_HALF_ALPHABET:
				mode = DefaultSoftKeyboard.KEYCODE_USERSYMBOL_HAN_ALPHABET;
				break;
			  case DefaultSoftKeyboard.KEYMODE_JA_HALF_NUMBER:
				mode = DefaultSoftKeyboard.KEYCODE_USERSYMBOL_HAN_NUMBER;
				break;
			  case DefaultSoftKeyboard.KEYMODE_JA_HALF_KATAKANA:
			  case DefaultSoftKeyboard.KEYMODE_JA_HALF_NICO_KATAKANA:
				mode = DefaultSoftKeyboard.KEYCODE_USERSYMBOL_HAN_KATAKANA;
				break;
			}
			if (mode != 0) {
				changeEngineMode(ENGINE_MODE_USERSYMBOL, -mode);
			}
		} else {
			changeEngineMode(ENGINE_MODE_SYMBOL, 0);
		}
	}

	/**
	 * Handle the space key event from the Hardware keyboard.
	 *
	 * @param ev  The space key event
	 */
	private void processHardwareKeyboardSpaceKey(final KeyEvent ev) {
		/* H/W space key */
		if (mPreConverter != null) {
			mPreConverter.convert2(mComposingText);
		}
		if (ev.isShiftPressed()) {
			/* change Japanese <-> English mode */
			mHardAlt = 0;
			mHardShift = 0;
			mHardCtrl = 0;
			updateMetaKeyStateDisplay();
			/*
			if (mEngineState.isEnglish()) {
			    // English mode to Japanese mode
				//((DefaultSoftKeyboard) mInputViewManager).changeKeyMode(DefaultSoftKeyboard.KEYMODE_JA_FULL_HIRAGANA);
				((DefaultSoftKeyboard) mInputViewManager).setDefaultKeyboard();
				mConverter = mConverterJAJP;
			} else {
				// Japanese mode to English mode
				((DefaultSoftKeyboard) mInputViewManager).changeKeyMode(DefaultSoftKeyboard.KEYMODE_JA_HALF_ALPHABET);
				mConverter = mConverterEN;
			}
			 */
			// change next mode
			((DefaultSoftKeyboard) mInputViewManager).nextKeyMode();
			// reset candidate & symbol position
			mCandidatesViewManager.clearCandidates();
			mDocomoEmojiCount = 0;
			resetPrediction();
		} else if(ev.isAltPressed() || isCtrlPressed(ev)){
			/* display the symbol list (G1 specific. same as KEYCODE_SYM) */
			if (!mEngineState.isSymbolList() && !mEngineState.isDocomoSymbolList() && !mEngineState.isUserSymbol()) {
				commitAllText();
			}
			changeEngineMode(ENGINE_MODE_SYMBOL, 0);
			mHardAlt = 0;
			mHardCtrl = 0;
			updateMetaKeyStateDisplay();

		} else if (isEnglishPrediction()) {
			/* Auto commit if English mode */
			if (mComposingText.size(0) == 0) {
				commitText(" ");
				mCandidatesViewManager.clearCandidates();
				mDocomoEmojiCount = 0;
				resetPrediction();
				breakSequence();
			} else {
				initCommitInfoForWatchCursor();
				commitText(true);
				commitSpaceJustOne();
				checkCommitInfo();
			}
			mEnableAutoDeleteSpace = false;

		} else {
			/* start consecutive clause conversion if Japanese mode */
			if (mComposingText.size(0) == 0) {
				boolean isZen = false;
				final DefaultSoftKeyboard inputManager = ((DefaultSoftKeyboard) mInputViewManager);
				if (inputManager != null) {
					switch (inputManager.mCurrentKeyMode) {
						case DefaultSoftKeyboard.KEYMODE_JA_FULL_NICO:
						case DefaultSoftKeyboard.KEYMODE_JA_FULL_HIRAGANA:
						case DefaultSoftKeyboard.KEYMODE_JA_FULL_ALPHABET:
						case DefaultSoftKeyboard.KEYMODE_JA_FULL_NUMBER:
						case DefaultSoftKeyboard.KEYMODE_JA_FULL_KATAKANA:
						case DefaultSoftKeyboard.KEYMODE_JA_FULL_NICO_KATAKANA:
							if (inputManager.getKeyboardType() == DefaultSoftKeyboard.KEYBOARD_12KEY) {
								isZen = inputManager.mKana12SpaceZen;
							} else {
								isZen = inputManager.mQwertySpaceZen;
							}
							break;
					}
				}
				commitText(isZen ? "\u3000" : " ");
				mCandidatesViewManager.clearCandidates();
				mDocomoEmojiCount = 0;
				resetPrediction();
				breakSequence();
			} else {
				startConvert(EngineState.CONVERT_TYPE_RENBUN);
			}
		}
	}

	/**
	 * Handle the character code from the hardware keyboard except the space key.
	 *
	 * @param str  The input character
	 */
	private void processHardwareKeyboardInputChar(final StrSegment str) {
		if (isEnableL2Converter()) {
			boolean commit = false;
			if (mPreConverter == null) {
				final Matcher m = mEnglishAutoCommitDelimiter.matcher(str.string);
				if (m.matches()) {
					commitText(true);

					commit = true;
				}
				appendStrSegment(str);
			} else {
				appendStrSegment(str);
				mPreConverter.convert(mComposingText);
			}

			if (commit) {
				commitText(true);
			} else {
				mStatus = STATUS_INPUT;
				//updateViewStatusForPrediction(true, true, true);
				updateViewStatusForPrediction(true, true, false);
			}
		} else {
			appendStrSegment(str);
			boolean completed = true;
			if (mPreConverter != null) {
				completed = mPreConverter.convert(mComposingText);
			}

			if (completed) {
				commitText(false);
			} else {
				updateViewStatus(ComposingText.LAYER1, false, true, false);
			}
		}
	}

	/** Thread for updating the candidates view */
	private void updatePrediction() {
		int candidates = 0;
		final int cursor = mComposingText.getCursor(ComposingText.LAYER1);
		if (isEnableL2Converter() || mEngineState.isSymbolList() || mEngineState.isDocomoSymbolList() || mEngineState.isUserSymbol()) {
			if (mExactMatchMode) {
				/* exact matching */
				candidates = mConverter.predict(mComposingText, 0, cursor);
			} else {
				/* normal prediction */
				candidates = mConverter.predict(mComposingText, 0, -1);
			}
		}

		/* update the candidates view */
		if (candidates > 0) {
			mHasContinuedPrediction = ((mComposingText.size(ComposingText.LAYER1) == 0) && !(mEngineState.isSymbolList() || mEngineState.isDocomoSymbolList() || mEngineState.isUserSymbol()));
			mCandidatesViewManager.displayCandidates(mConverter);
		} else {
			mCandidatesViewManager.clearCandidates();
			mDocomoEmojiCount = 0;
			resetPrediction();
		}
	}

	/**
	 * Handle a left key event.
	 */
	private void processLeftKeyEvent() {
		if (mEngineState.isConvertState()) {
			if (mEngineState.isEisuKana()) {
				mExactMatchMode = true;
			}

			if (1 < mComposingText.getCursor(ComposingText.LAYER1)) {
				mComposingText.moveCursor(ComposingText.LAYER1, -1);
			}
		} else if (mExactMatchMode) {
			mComposingText.moveCursor(ComposingText.LAYER1, -1);
		} else {
			if (isEnglishPrediction()) {
				mComposingText.moveCursor(ComposingText.LAYER1, -1);
			} else {
				mExactMatchMode = true;
			}
		}

		mCommitCount = 0; /* retry consecutive clause conversion if necessary. */
		mStatus = STATUS_INPUT_EDIT;

		if (mEngineState.isRenbun()) {
			updateViewStatus(mTargetLayer, true, true, true);
		} else {
			updateViewStatusForPrediction(true, true, false);
		}
	}

	/**
	 * Handle a right key event.
	 */
	private void processRightKeyEvent() {
		int layer = mTargetLayer;
		final ComposingText composingText = mComposingText;
		if (mExactMatchMode || (mEngineState.isConvertState())) {
			final int textSize = composingText.size(ComposingText.LAYER1);
			if (composingText.getCursor(ComposingText.LAYER1) == textSize) {
				mExactMatchMode = false;
				layer = ComposingText.LAYER1; /* convert -> prediction */
				final EngineState state = new EngineState();
				state.convertType = EngineState.CONVERT_TYPE_NONE;
				updateEngineState(state);
			} else {
				if (mEngineState.isEisuKana()) {
					mExactMatchMode = true;
				}
				composingText.moveCursor(ComposingText.LAYER1, 1);
			}
		} else {
			if (composingText.getCursor(ComposingText.LAYER1)
					< composingText.size(ComposingText.LAYER1)) {
				composingText.moveCursor(ComposingText.LAYER1, 1);
			}
		}

		mCommitCount = 0; /* retry consecutive clause conversion if necessary. */
		mStatus = STATUS_INPUT_EDIT;

		if (mEngineState.isRenbun()) {
			updateViewStatus(mTargetLayer, true, true, true);
		} else {
			updateViewStatusForPrediction(true, true, false);
		}
	}

	/**
	 * Handle a process toggle forward event.
	 */
	private void processToggleForwardEvent() {
		mCommitCount = 0; /* retry consecutive clause conversion if necessary. */
		mStatus = STATUS_INPUT_EDIT;
		updateViewStatusForPrediction(false, true, false);
	}

	/**
	 * Handle a key event which is not right or left key when the
	 * composing text is empty and some candidates are shown.
	 *
	 * @param ev        A key event
	 * @return          {@code true} if this consumes the event; {@code false} if not.
	 */
	boolean processKeyEventNoInputCandidateShown(final KeyEvent ev) {
		boolean ret = true;

		switch (ev.getKeyCode()) {
			case KeyEvent.KEYCODE_DEL:
				ret = true;
				break;
			case KeyEvent.KEYCODE_ENTER:
			case KeyEvent.KEYCODE_DPAD_UP:
			case KeyEvent.KEYCODE_DPAD_DOWN:
			case KeyEvent.KEYCODE_MENU:
				ret = false;
				break;

			case KeyEvent.KEYCODE_CALL:
				return false;

			case KeyEvent.KEYCODE_DPAD_CENTER:
				ret = true;
				break;

			case KeyEvent.KEYCODE_BACK:
			case 111: // KeyEvent.KEYCODE_ESCAPE:
				if (mCandidatesViewManager.getViewType() == CandidatesViewManager.VIEW_TYPE_FULL) {
					mStatus &= ~STATUS_CANDIDATE_FULL;
					mCandidatesViewManager.setViewType(CandidatesViewManager.VIEW_TYPE_NORMAL);
					return true;
				} else {
					ret = true;
				}
				break;

			default:
				return true;
		}

		mCandidatesViewManager.checkCandidateTask();
		if (mConverter != null) {
			/* initialize the converter */
			mConverter.init();
		}
		updateViewStatusForPrediction(true, true, true);
		return ret;
	}

	/**
	 * Update views and the display of the composing text for predict mode.
	 *
	 * @param updateCandidates  {@code true} to update the candidates view
	 * @param updateEmptyText   {@code false} to update the composing text if it is not empty; {@code true} to update always.
	 */
	private void updateViewStatusForPrediction(final boolean updateCandidates, final boolean updateEmptyText, final boolean indicateCandidates) {
		final EngineState state = new EngineState();
		state.convertType = EngineState.CONVERT_TYPE_NONE;
		updateEngineState(state);

		updateViewStatus(ComposingText.LAYER1, updateCandidates, updateEmptyText, indicateCandidates);
	}

	/**
	 * Update views and the display of the composing text.
	 *
	 * @param layer                      Display layer of the composing text
	 * @param updateCandidates  {@code true} to update the candidates view
	 * @param updateEmptyText   {@code false} to update the composing text if it is not empty; {@code true} to update always.
	 */
	private void updateViewStatus(final int layer, final boolean updateCandidates, final boolean updateEmptyText, final boolean indicateCandidates) {
		mTargetLayer = layer;

		if (updateCandidates) {
			updateCandidateView();
		}
		/* notice to the input view */
		mInputViewManager.onUpdateState(this);

		/* set the text for displaying as the composing text */
		mDisplayText.clear();
		mDisplayText.insert(0, mComposingText.toString(layer));

		/* add decoration to the text */
		final int cursor = mComposingText.getCursor(layer);
		if ((mInputConnection != null) && ((mDisplayText.length() != 0) || updateEmptyText)) {
			if (cursor != 0) {
				int highlightEnd = 0;

				if ((mExactMatchMode && (!mEngineState.isEisuKana()))
						|| (FIX_CURSOR_TEXT_END && isEnglishPrediction()
								&& (cursor < mComposingText.size(ComposingText.LAYER1)))){

					mDisplayText.setSpan(SPAN_EXACT_BGCOLOR_HL, 0, cursor,
							Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
					highlightEnd = cursor;

				} else if (FIX_CURSOR_TEXT_END && mEngineState.isEisuKana()) {
					mDisplayText.setSpan(SPAN_EISUKANA_BGCOLOR_HL, 0, cursor,
							Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
					highlightEnd = cursor;

				} else if (layer == ComposingText.LAYER2) {
					highlightEnd = mComposingText.toString(layer, 0, 0).length();

					/* highlights the first segment */
					mDisplayText.setSpan(SPAN_CONVERT_BGCOLOR_HL, 0,
							highlightEnd,
							Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				}

				if (FIX_CURSOR_TEXT_END && (highlightEnd != 0)) {
					/* highlights remaining text */
					mDisplayText.setSpan(SPAN_REMAIN_BGCOLOR_HL, highlightEnd,
							mComposingText.toString(layer).length(),
							Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

					/* text color in the highlight */
					mDisplayText.setSpan(SPAN_TEXTCOLOR, 0,
							mComposingText.toString(layer).length(),
							Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				}
			}

			if (getAutoForwardToggle12key()) {
				mDisplayText.setSpan(SPAN_TOGGLING_HL, mDisplayText.length()-1, mDisplayText.length(),
						Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
			
			mDisplayText.setSpan(SPAN_UNDERLINE, 0, mDisplayText.length(),
					Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

			int displayCursor = mComposingText.toString(layer, 0, cursor - 1).length();
			if (FIX_CURSOR_TEXT_END) {
				displayCursor = (cursor == 0) ?  0 : 1;
			}
			/* update the composing text on the EditView */
			mInputConnection.setComposingText(mDisplayText, displayCursor);

		}
		if (indicateCandidates) {
			mCandidatesViewManager.setIndicateCandidateView();
		}
	}

	/**
	 * Update the candidates view.
	 */
	private void updateCandidateView() {
		switch (mTargetLayer) {
			case ComposingText.LAYER0:
			case ComposingText.LAYER1: /* prediction */
				if (mActionPrediction || mEngineState.isSymbolList() || mEngineState.isDocomoSymbolList() || mEngineState.isUserSymbol() || mEngineState.isEisuKana()) {
					/* update the candidates view */
					if ((mComposingText.size(ComposingText.LAYER1) != 0)
							&& !mEngineState.isConvertState()) {

						mHandler.removeMessages(MSG_PREDICTION);
						if (mCandidatesViewManager.getCurrentView().isShown()) {
							mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_PREDICTION),
									mPredictionDelayMSShowingCandidate);
						} else {
							mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_PREDICTION),
									mPredictionDelayMS1st);
						}
					} else {
						mHandler.removeMessages(MSG_PREDICTION);
						updatePrediction();
					}
				} else {
					mHandler.removeMessages(MSG_PREDICTION);
					mCandidatesViewManager.clearCandidates();
					mDocomoEmojiCount = 0;
					resetPrediction();
				}
				break;
			case ComposingText.LAYER2: /* convert */
				if (mEngineState.isKanaDirect()) {
					mHandler.removeMessages(MSG_PREDICTION);
					mCandidatesViewManager.clearCandidates();
					mDocomoEmojiCount = 0;
					resetPrediction();
					mComposingText.setCursor(ComposingText.LAYER2, 1);
					mCandidatesViewManager.displayCandidates(mConverter);
					break;
				}
				if (mCommitCount == 0) {
					mHandler.removeMessages(MSG_PREDICTION);
					mConverter.convert(mComposingText);
				}

				final int candidates = mConverter.makeCandidateListOf(mCommitCount);

				if (candidates != 0) {
					mComposingText.setCursor(ComposingText.LAYER2, 1);
					mCandidatesViewManager.displayCandidates(mConverter);
				} else {
					mComposingText.setCursor(ComposingText.LAYER1, mComposingText.toString(ComposingText.LAYER1).length());
					mCandidatesViewManager.clearCandidates();
					mDocomoEmojiCount = 0;
					resetPrediction();
				}
				break;
			default:
				break;
		}
	}

	/**
	 * Commit the displaying composing text.
	 *
	 * @param learn  {@code true} to register the committed string to the learning dictionary.
	 * @return          IME's status after commit
	 */
	private int commitText(final boolean learn) {
		if (isEnglishPrediction()) {
			mComposingText.setCursor(ComposingText.LAYER1,
					mComposingText.size(ComposingText.LAYER1));
		}

		final int layer = mTargetLayer;
		final int cursor = mComposingText.getCursor(layer);
		if (cursor == 0) {
			return mStatus;
		}
		final String tmp = mComposingText.toString(layer, 0, cursor - 1);

		if (mConverter != null) {
			if (learn) {
				if (mEngineState.isRenbun()) {
					learnWord(0); /* select the top of the clauses */
				} else {
					if (mComposingText.size(ComposingText.LAYER1) != 0) {
						final String stroke = mComposingText.toString(ComposingText.LAYER1, 0, mComposingText.getCursor(layer) - 1);
						final WnnWord word = new WnnWord(tmp, stroke);

						learnWord(word);
					}
				}
			} else {
				breakSequence();
			}
		}
		return commitTextThroughInputConnection(tmp);
	}

	/**
	 * Commit all uncommitted words.
	 */
	private void commitAllText() {
		initCommitInfoForWatchCursor();
		if (mEngineState.isConvertState()) {
			commitConvertingText();
		} else {
			mComposingText.setCursor(ComposingText.LAYER1,
					mComposingText.size(ComposingText.LAYER1));
			mStatus = commitText(true);
		}
		checkCommitInfo();
	}

	/**
	 * Commit a word.
	 *
	 * @param word              A word to commit
	 * @return                  IME's status after commit
	 */
	private int commitText(final WnnWord word) {
		if (mConverter != null) {
			learnWord(word);
		}
		return commitTextThroughInputConnection(word.candidate);
	}

	/**
	 * Commit a string.
	 *
	 * @param str  A string to commit
	 */
	private void commitText(final String str) {
		mInputConnection.commitText(str, (FIX_CURSOR_TEXT_END ? 1 : str.length()));
		mPrevCommitText.append(str);
		mPrevCommitCount++;
		mEnableAutoDeleteSpace = true;
		updateViewStatusForPrediction(false, false, false);
	}

	/**
	 * Commit a string through {@link InputConnection}.
	 *
	 * @param string  A string to commit
	 * @return                  IME's status after commit
	 */
	private int commitTextThroughInputConnection(final String string) {
		int layer = mTargetLayer;

		mInputConnection.commitText(string, (FIX_CURSOR_TEXT_END ? 1 : string.length()));
		mPrevCommitText.append(string);
		mPrevCommitCount++;

		final int cursor = mComposingText.getCursor(layer);
		if (cursor > 0) {
			mComposingText.deleteStrSegment(layer, 0, mComposingText.getCursor(layer) - 1);
			mComposingText.setCursor(layer, mComposingText.size(layer));
		}
		mExactMatchMode = false;
		mCommitCount++;

		if ((layer == ComposingText.LAYER2) && (mComposingText.size(layer) == 0)) {
			layer = 1; /* for connected prediction */
		}

		final boolean commited = autoCommitEnglish();
		mEnableAutoDeleteSpace = true;

		if (layer == ComposingText.LAYER2) {
			final EngineState state = new EngineState();
			state.convertType = mEngineState.convertType;  // EngineState.CONVERT_TYPE_RENBUN;
			updateEngineState(state);
			if (isPredict()) {
				updateViewStatusForPrediction(!commited, false, false);
			} else {
				updateViewStatus(layer, !commited, false, false);
			}
		} else {
			if (mEngineState.isSymbolList() || mEngineState.isDocomoSymbolList() || mEngineState.isUserSymbol()) {
				//
			} else {
				updateViewStatusForPrediction(!commited, false, false);
			}
		}

		if (mComposingText.size(ComposingText.LAYER0) == 0) {
			return STATUS_INIT;
		} else {
			return STATUS_INPUT_EDIT;
		}
	}

	/**
	 * Returns whether it is English prediction mode or not.
	 *
	 * @return  {@code true} if it is English prediction mode; otherwise, {@code false}.
	 */
	private boolean isEnglishPrediction() {
		return (mEngineState.isEnglish() && isEnableL2Converter());
	}

	/**
	 * Change the conversion engine and the letter converter(Romaji-to-Kana converter).
	 *
	 * @param mode  Engine's mode to be changed
	 * @see net.gorry.android.input.nicownng.NicoWnnGEvent.Mode
	 * @see net.gorry.android.input.nicownng.JAJP.DefaultSoftKeyboardJAJP
	 */
	private void changeEngineMode(final int mode, final int mode2) {
		EngineState state = new EngineState();
		switch (mode) {
			case ENGINE_MODE_OPT_TYPE_QWERTY:
				state.keyboard = EngineState.KEYBOARD_QWERTY;
				updateEngineState(state);
				clearCommitInfo();
				mDocomoEmojiCount = 0;
				resetPrediction();
				return;

			case ENGINE_MODE_OPT_TYPE_12KEY:
				state.keyboard = EngineState.KEYBOARD_12KEY;
				updateEngineState(state);
				clearCommitInfo();
				mDocomoEmojiCount = 0;
				resetPrediction();
				return;

			case ENGINE_MODE_EISU_KANA:
				if (mEngineState.isEisuKana()) {
					state.temporaryMode = EngineState.TEMPORARY_DICTIONARY_MODE_NONE;
					updateEngineState(state);
					updateViewStatusForPrediction(true, true, true); /* prediction only */
				} else {
					startConvert(EngineState.CONVERT_TYPE_EISU_KANA);
				}
				mDocomoEmojiCount = 0;
				resetPrediction();
				return;

			case ENGINE_MODE_SYMBOL:
				if (mEnableSymbolList && !mDirectInputMode) {
					state.temporaryMode = EngineState.TEMPORARY_DICTIONARY_MODE_SYMBOL;
					updateEngineState(state);
					updateViewStatusForPrediction(true, true, true);
				}
				mDocomoEmojiCount = 0;
				resetPrediction();
				return;

			case ENGINE_MODE_DOCOMOSYMBOL:
				if (mEnableSymbolList && !mDirectInputMode) {
					state.temporaryMode = EngineState.TEMPORARY_DICTIONARY_MODE_DOCOMOSYMBOL00 + mDocomoEmojiCount;
					mDocomoEmojiCount = (mDocomoEmojiCount+1)&1;
					updateEngineState(state);
					updateViewStatusForPrediction(true, true, true);
				}
				return;

			case ENGINE_MODE_USERSYMBOL:
				if (mEnableSymbolList && !mDirectInputMode) {
					state.temporaryMode = mode2;
					updateEngineState(state);
					updateViewStatusForPrediction(true, true, true);
				}
				mDocomoEmojiCount = 0;
				resetPrediction();
				return;

			default:
				break;
		}

		state = new EngineState();
		state.temporaryMode = EngineState.TEMPORARY_DICTIONARY_MODE_NONE;
		updateEngineState(state);

		state = new EngineState();
		switch (mode) {
			case NicoWnnGEvent.Mode.DIRECT:
				/* Full/Half-width number or Full-width alphabet */
				mConverter = null;
				mPreConverter = null;
				break;

			case NicoWnnGEvent.Mode.NO_LV1_CONV:
				/* no Romaji-to-Kana conversion (=English prediction mode) */
				state.dictionarySet = EngineState.DICTIONARYSET_EN;
				updateEngineState(state);
				mConverter = mConverterEN;
				mPreConverter = null;
				break;

			case NicoWnnGEvent.Mode.NO_LV2_CONV:
				mConverter = null;
				mPreConverter = mPreConverterHiragana;
				break;

			case ENGINE_MODE_FULL_KATAKANA:
				mConverter = null;
				mPreConverter = mPreConverterFullKatakana;
				break;

			case ENGINE_MODE_HALF_KATAKANA:
				mConverter = null;
				mPreConverter = mPreConverterHalfKatakana;
				break;

			case ENGINE_MODE_FULL_KATAKANA_CONV:
				state.dictionarySet = EngineState.DICTIONARYSET_JP;
				updateEngineState(state);
				mConverter = mConverterJAJP;
				mPreConverter = mPreConverterFullKatakana;
				break;

			default:
				/* HIRAGANA input mode */
				state.dictionarySet = EngineState.DICTIONARYSET_JP;
				updateEngineState(state);
				mConverter = mConverterJAJP;
				mPreConverter = mPreConverterHiragana;
				break;
		}

		mPreConverterBack = mPreConverter;
		mConverterBack = mConverter;
	}

	/**
	 * Update the conversion engine's state.
	 *
	 * @param state  Engine's state to be updated
	 */
	private void updateEngineState(final EngineState state) {
		final EngineState myState = mEngineState;

		/* language */
		if ((state.dictionarySet != EngineState.INVALID)
				&& (myState.dictionarySet != state.dictionarySet)) {

			switch (state.dictionarySet) {
				case EngineState.DICTIONARYSET_EN:
					setDictionary(NicoWnnGEngineJAJP.DIC_LANG_EN);
					break;

				case EngineState.DICTIONARYSET_JP:
				default:
					setDictionary(NicoWnnGEngineJAJP.DIC_LANG_JP);
					break;
			}
			myState.dictionarySet = state.dictionarySet;
			breakSequence();

			/* update keyboard setting */
			if (state.keyboard == EngineState.INVALID) {
				state.keyboard = myState.keyboard;
			}
		}

		/* type of conversion */
		if ((state.convertType != EngineState.INVALID)
				&& (myState.convertType != state.convertType)) {

			switch (state.convertType) {
				case EngineState.CONVERT_TYPE_NONE:
				case EngineState.CONVERT_TYPE_PREDICT:
					setDictionary(mPrevDictionarySet);
					break;

				case EngineState.CONVERT_TYPE_EISU_KANA:
				case EngineState.CONVERT_TYPE_KANA_DIRECT:
					setDictionary(NicoWnnGEngineJAJP.DIC_LANG_JP_EISUKANA);
					break;

				case EngineState.CONVERT_TYPE_RENBUN:
				default:
					setDictionary(NicoWnnGEngineJAJP.DIC_LANG_JP);
					break;
			}
			myState.convertType = state.convertType;
		}

		/* temporary dictionary */
		if (state.temporaryMode != EngineState.INVALID) {

			switch (state.temporaryMode) {
				case EngineState.TEMPORARY_DICTIONARY_MODE_NONE:
					if (myState.temporaryMode != EngineState.TEMPORARY_DICTIONARY_MODE_NONE) {
						setDictionary(mPrevDictionarySet);
						mCurrentSymbol = -1;
						mPreConverter = mPreConverterBack;
						mConverter = mConverterBack;
						mDisableAutoCommitEnglishMask &= ~AUTO_COMMIT_ENGLISH_SYMBOL;
						for (int i=0; i<mCurrentUserSymbol.length; i++) {
							mCurrentUserSymbol[i] = -1;
						}
					}
					break;

				case EngineState.TEMPORARY_DICTIONARY_MODE_SYMBOL:
					if (mEnableSymbolListNonHalf) {
						switch (((DefaultSoftKeyboard)mInputViewManager).mCurrentKeyMode) {
						case DefaultSoftKeyboard.KEYMODE_JA_HALF_ALPHABET:
						case DefaultSoftKeyboard.KEYMODE_JA_HALF_NUMBER:
							mConverterSymbolEngineBack.setSymbolDictionary(SymbolList.SYMBOL_ENGLISH);
							break;
						default:
							if (++mCurrentSymbol >= mConverterSymbolEngineBack.getJAJPSymbolNum()) {
								mCurrentSymbol = 0;
							}
							mConverterSymbolEngineBack.setSymbolDictionary(mCurrentSymbol);
							break;
						}
					} else {
						mConverterSymbolEngineBack.setSymbolDictionary(SymbolList.SYMBOL_ENGLISH);
					}
					mConverter = mConverterSymbolEngineBack;
					mDisableAutoCommitEnglishMask |= AUTO_COMMIT_ENGLISH_SYMBOL;
					breakSequence();
					break;

				case EngineState.TEMPORARY_DICTIONARY_MODE_DOCOMOSYMBOL00:
					mConverterSymbolEngineBack.setSymbolDictionary(SymbolList.SYMBOL_DOCOMO_EMOJI00);
					mConverter = mConverterSymbolEngineBack;
					mDisableAutoCommitEnglishMask |= AUTO_COMMIT_ENGLISH_SYMBOL;
					breakSequence();
					break;

				case EngineState.TEMPORARY_DICTIONARY_MODE_DOCOMOSYMBOL01:
					mConverterSymbolEngineBack.setSymbolDictionary(SymbolList.SYMBOL_DOCOMO_EMOJI01);
					mConverter = mConverterSymbolEngineBack;
					mDisableAutoCommitEnglishMask |= AUTO_COMMIT_ENGLISH_SYMBOL;
					breakSequence();
					break;

				default:
					if ((state.temporaryMode > EngineState.TEMPORARY_DICTIONARY_MODE_USERSYMBOL) &&
						(state.temporaryMode < EngineState.TEMPORARY_DICTIONARY_MODE_USERSYMBOL_END)) {
						final int num = state.temporaryMode - EngineState.TEMPORARY_DICTIONARY_MODE_USERSYMBOL - 1;
						if (++mCurrentUserSymbol[num] >= mConverterSymbolEngineBack.getJAJPUserSymbolNum(num)) {
							mCurrentUserSymbol[num] = 0;
						}
						mConverterSymbolEngineBack.setUserSymbolDictionary(num, mCurrentUserSymbol[num]);
						mConverter = mConverterSymbolEngineBack;
						mDisableAutoCommitEnglishMask |= AUTO_COMMIT_ENGLISH_SYMBOL;
						breakSequence();
					}
					break;
			}
			myState.temporaryMode = state.temporaryMode;
		}

		/* preference dictionary */
		if ((state.preferenceDictionary != EngineState.INVALID)
				&& (myState.preferenceDictionary != state.preferenceDictionary)) {

			myState.preferenceDictionary = state.preferenceDictionary;
			setDictionary(mPrevDictionarySet);
		}

		/* keyboard type */
		if (state.keyboard != EngineState.INVALID) {
			switch (state.keyboard) {
				case EngineState.KEYBOARD_12KEY:
					mConverterJAJP.setKeyboardType(NicoWnnGEngineJAJP.KEYBOARD_KEYPAD12);
					mConverterEN.setDictionary(NicoWnnGEngineEN.DICT_DEFAULT);
					break;

				case EngineState.KEYBOARD_QWERTY:
				default:
					mConverterJAJP.setKeyboardType(NicoWnnGEngineJAJP.KEYBOARD_QWERTY);
					if (mEnableSpellCorrection) {
						mConverterEN.setDictionary(NicoWnnGEngineEN.DICT_FOR_CORRECT_MISTYPE);
					} else {
						mConverterEN.setDictionary(NicoWnnGEngineEN.DICT_DEFAULT);
					}
					break;
			}
			myState.keyboard = state.keyboard;
		}
	}

	/**
	 * Set dictionaries to be used.
	 *
	 * @param mode  Definition of dictionaries
	 */
	private void setDictionary(final int mode) {
		int target = mode;
		switch (target) {

			case NicoWnnGEngineJAJP.DIC_LANG_JP:

				switch (mEngineState.preferenceDictionary) {
					case EngineState.PREFERENCE_DICTIONARY_PERSON_NAME:
						target = NicoWnnGEngineJAJP.DIC_LANG_JP_PERSON_NAME;
						break;
					case EngineState.PREFERENCE_DICTIONARY_POSTAL_ADDRESS:
						target = NicoWnnGEngineJAJP.DIC_LANG_JP_POSTAL_ADDRESS;
						break;
					default:
						break;
				}

				break;

			case NicoWnnGEngineJAJP.DIC_LANG_EN:

				switch (mEngineState.preferenceDictionary) {
					case EngineState.PREFERENCE_DICTIONARY_EMAIL_ADDRESS_URI:
						target = NicoWnnGEngineJAJP.DIC_LANG_EN_EMAIL_ADDRESS;
						break;
					default:
						break;
				}

				break;

			default:
				break;
		}

		switch (mode) {
			case NicoWnnGEngineJAJP.DIC_LANG_JP:
			case NicoWnnGEngineJAJP.DIC_LANG_EN:
				mPrevDictionarySet = mode;
				break;
			default:
				break;
		}

		mConverterJAJP.setDictionary(target);
	}

	/**
	 * Handle a toggle key input event.
	 *
	 * @param table  Table of toggle characters
	 */
	private void processSoftKeyboardToggleChar(final String[] table) {
		if (table == null) {
			return;
		}

		commitConvertingText();

		boolean toggled = false;
		if ((mStatus & ~STATUS_CANDIDATE_FULL) == STATUS_INPUT) {
			final int cursor = mComposingText.getCursor(ComposingText.LAYER1);
			if (cursor > 0) {
				final String prevChar = mComposingText.getStrSegment(ComposingText.LAYER1,
						cursor - 1).string;
				final String c = searchToggleCharacter(prevChar, table, false);
				if (c != null) {
					mComposingText.delete(ComposingText.LAYER1, false);
					appendStrSegment(new StrSegment(c));
					toggled = true;
				}
			}
		}

		if (!toggled) {
			if (!isEnableL2Converter()) {
				commitText(false);
			}

			String str = table[0];
			/* shift on */
			if (mAutoCaps
					&& isEnglishPrediction()
					&& (getShiftKeyState(getCurrentInputEditorInfo()) == 1)) {

				final char top = table[0].charAt(0);
				if (Character.isLowerCase(top)) {
					str = Character.toString(Character.toUpperCase(top));
				}
			}
			appendStrSegment(new StrSegment(str));
		}

		mStatus = STATUS_INPUT;

		// updateViewStatusForPrediction(true, true, true);
		updateViewStatusForPrediction(true, true, false);
	}

	/**
	 * Handle character input from the software keyboard without listing candidates.
	 *
	 * @param chars  The input character(s)
	 */
	private void processSoftKeyboardCodeWithoutConversion(final char[] chars) {
		if (chars == null) {
			return;
		}

		final ComposingText text = mComposingText;
		appendStrSegment(new StrSegment(chars));

		if (!isAlphabetLast(text.toString(ComposingText.LAYER1))) {
			/* commit if the input character is not alphabet */
			commitText(false);
		} else {
			final boolean completed = mPreConverter.convert(text);
			if (completed) {
				commitText(false);
			} else {
				mStatus = STATUS_INPUT;
				updateViewStatusForPrediction(true, true, true);
			}
		}
	}

	/**
	 * Handle character input from the software keyboard.
	 *
	 * @param chars   The input character(s)
	 */
	private void processSoftKeyboardCode(final char[] chars) {
		if (chars == null) {
			return;
		}

		if ((chars[0] == ' ') || (chars[0] == '\u3000' /* Full-width space */)) {
			if (mComposingText.size(0) == 0) {
				mCandidatesViewManager.clearCandidates();
				mDocomoEmojiCount = 0;
				resetPrediction();
				commitText(new String(chars));
				breakSequence();
			} else {
				if (isEnglishPrediction()) {
					initCommitInfoForWatchCursor();
					commitText(true);
					commitSpaceJustOne();
					checkCommitInfo();
				} else {
					if (mPreConverter != null) {
						mPreConverter.convert2(mComposingText);
					}
					startConvert(EngineState.CONVERT_TYPE_RENBUN);
				}
			}
			mEnableAutoDeleteSpace = false;
		} else {
			commitConvertingText();

			/* Auto-commit a word if it is English and Qwerty mode */
			boolean commit = false;
			if (isEnglishPrediction()
					&& (mEngineState.keyboard == EngineState.KEYBOARD_QWERTY)) {

				final Matcher m = mEnglishAutoCommitDelimiter.matcher(new String(chars));
				if (m.matches()) {
					commit = true;
				}
			}

			if (commit) {
				commitText(true);

				appendStrSegment(new StrSegment(chars));
				commitText(true);
			} else {
				appendStrSegment(new StrSegment(chars));
				if (mPreConverter != null) {
					mPreConverter.convert(mComposingText);
					mStatus = STATUS_INPUT;
				}
				// updateViewStatusForPrediction(true, true, true);
				updateViewStatusForPrediction(true, true, false);
			}
		}
	}

	/**
	 * Start consecutive clause conversion or EISU-KANA conversion mode.
	 *
	 * @param convertType               The conversion type({@code EngineState.CONVERT_TYPE_*})
	 */
	private void startConvert(final int convertType) {
		startConvert(convertType, false);
	}
	private void startConvert(final int convertType, boolean backward) {
		if (!isEnableL2Converter()) {
			return;
		}

		final int l0cursor = mComposingText.getCursor(ComposingText.LAYER0);
		final int l1cursor = mComposingText.getCursor(ComposingText.LAYER1);
		if (mEngineState.convertType != convertType) {
			/* adjust the cursor position */
			if (!mExactMatchMode) {
				if ((convertType == EngineState.CONVERT_TYPE_RENBUN) || (convertType == EngineState.CONVERT_TYPE_PREDICT)) {
					/* not specify */
					mComposingText.setCursor(ComposingText.LAYER1, 0);
				} else {
					if (mEngineState.isRenbun()||mEngineState.isPredict()) {
						/* EISU-KANA conversion specifying the position of the segment if previous mode is conversion mode */
						mExactMatchMode = true;
					} else {
						/* specify all range */
						mComposingText.setCursor(ComposingText.LAYER1,
								mComposingText.size(ComposingText.LAYER1));
					}
				}
			}

			if (convertType == EngineState.CONVERT_TYPE_RENBUN) {
				/* clears variables for the prediction */
				mExactMatchMode = false;
			}
			/* clears variables for the convert */
			mCommitCount = 0;

			int layer = ComposingText.LAYER2;
			if (convertType == EngineState.CONVERT_TYPE_EISU_KANA) {
				layer = ComposingText.LAYER1;
			}

			final EngineState state = new EngineState();
			state.convertType = convertType;
			updateEngineState(state);

			if ((convertType == EngineState.CONVERT_TYPE_PREDICT)) {
				WnnWord s = mCandidatesViewManager.getFirstWnnWordCandidate();
				if (backward) {
					s = mCandidatesViewManager.getPrevWnnWordCandidate();
				}
				if (s != null) {
					final int l1size = mComposingText.getStringLayer(ComposingText.LAYER1).size();
					final StrSegment[] ss = new StrSegment[(l1cursor == l1size) ? 1 : 2];
					ss[0] = new StrSegment(s.candidate, 0, (l1cursor>0 ? l1cursor-1 : 0));
					if (l1cursor != l1size) {
						String right = mComposingText.toString(ComposingText.LAYER1, l1cursor, l1size-1);
						ss[1] = new StrSegment(right, l1cursor, l1size-1);
					}
					final int l2size = mComposingText.getStringLayer(ComposingText.LAYER2).size();
					mComposingText.setCursor(ComposingText.LAYER2, l2size);
					final int l2cursor = mComposingText.getCursor(ComposingText.LAYER2);
					mComposingText.replaceStrSegment(ComposingText.LAYER2, ss, l2cursor);
					updateViewStatus(ComposingText.LAYER2, false, true, true);
					mComposingText.setCursorDirect(ComposingText.LAYER0, l0cursor);
					mComposingText.setCursorDirect(ComposingText.LAYER1, l1cursor);
					mComposingText.setCursorDirect(ComposingText.LAYER2, 1);
					mPrevCommitCount++;  // これがないと２文節以上の変換で中断されることがある
					return;
				}
				mPrevCommitCount++;  // これがないと変換中止したあとの変換再開で中断されることがある
			}
			updateViewStatus(layer, true, true, true);
		}
	}

	/**
	 * Auto commit a word in English (on half-width alphabet mode).
	 *
	 * @return  {@code true} if auto-committed; otherwise, {@code false}.
	 */
	private boolean autoCommitEnglish() {
		if (isEnglishPrediction() && (mDisableAutoCommitEnglishMask == AUTO_COMMIT_ENGLISH_ON)) {
			final CharSequence seq = mInputConnection.getTextBeforeCursor(2, 0);
			if (seq != null) {
				final Matcher m = mEnglishAutoCommitDelimiter.matcher(seq);
				if (m.matches()) {
					if ((seq.charAt(0) == ' ') && mEnableAutoDeleteSpace) {
						mInputConnection.deleteSurroundingText(2, 0);
						final CharSequence str = seq.subSequence(1, 2);
						mInputConnection.commitText(str, 1);
						mPrevCommitText.append(str);
						mPrevCommitCount++;
					}

					mHandler.removeMessages(MSG_PREDICTION);
					mCandidatesViewManager.clearCandidates();
					mDocomoEmojiCount = 0;
					resetPrediction();
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Insert a white space if the previous character is not a white space.
	 */
	private void commitSpaceJustOne() {
		final CharSequence seq = mInputConnection.getTextBeforeCursor(1, 0);
		if (seq != null) {
			if (seq.charAt(0) != ' ') {
				commitText(" ");
			}
		}
	}

	/**
	 * Get the shift key state from the editor.
	 *
	 * @param editor    The editor
	 * @return          State ID of the shift key (0:off, 1:on)
	 */
	private int getShiftKeyState(final EditorInfo editor) {
		return (getCurrentInputConnection().getCursorCapsMode(editor.inputType) == 0) ? 0 : 1;
	}

	/**
	 * Display current meta-key state.
	 */
	private void updateMetaKeyStateDisplay() {
		int mode = 0;
		if((mHardShift == 0) && (mHardAlt == 0)){
			mode = DefaultSoftKeyboard.HARD_KEYMODE_SHIFT_OFF_ALT_OFF;
		}else if((mHardShift == 1) && (mHardAlt == 0)){
			mode = DefaultSoftKeyboard.HARD_KEYMODE_SHIFT_ON_ALT_OFF;
		}else if((mHardShift == 2)  && (mHardAlt == 0)){
			mode = DefaultSoftKeyboard.HARD_KEYMODE_SHIFT_LOCK_ALT_OFF;
		}else if((mHardShift == 0) && (mHardAlt == 1)){
			mode = DefaultSoftKeyboard.HARD_KEYMODE_SHIFT_OFF_ALT_ON;
		}else if((mHardShift == 0) && (mHardAlt == 2)){
			mode = DefaultSoftKeyboard.HARD_KEYMODE_SHIFT_OFF_ALT_LOCK;
		}else if((mHardShift == 1) && (mHardAlt == 1)){
			mode = DefaultSoftKeyboard.HARD_KEYMODE_SHIFT_ON_ALT_ON;
		}else if((mHardShift == 1) && (mHardAlt == 2)){
			mode = DefaultSoftKeyboard.HARD_KEYMODE_SHIFT_ON_ALT_LOCK;
		}else if((mHardShift == 2) && (mHardAlt == 1)){
			mode = DefaultSoftKeyboard.HARD_KEYMODE_SHIFT_LOCK_ALT_ON;
		}else if((mHardShift == 2) && (mHardAlt == 2)){
			mode = DefaultSoftKeyboard.HARD_KEYMODE_SHIFT_LOCK_ALT_LOCK;
		}else{
			mode = DefaultSoftKeyboard.HARD_KEYMODE_SHIFT_OFF_ALT_OFF;
		}
		((DefaultSoftKeyboard) mInputViewManager).updateIndicator(mode);
	}

	/**
	 * Memory a selected word.
	 *
	 * @param word  A selected word
	 */
	private void learnWord(final WnnWord word) {
		if (mEnableLearning && (word != null)) {
			mConverter.learn(word);
		}
	}

	/**
	 * Memory a clause which is generated by consecutive clause conversion.
	 *
	 * @param index  Index of a clause
	 */
	private void learnWord(final int index) {
		final ComposingText composingText = mComposingText;

		if (mConverter == null) return;
		if (mEnableLearning && (composingText.size(ComposingText.LAYER2) > index)) {
			final StrSegment seg = composingText.getStrSegment(ComposingText.LAYER2, index);
			if (seg instanceof StrSegmentClause) {
				mConverter.learn(((StrSegmentClause)seg).clause);
			} else {
				final String stroke = composingText.toString(ComposingText.LAYER1, seg.from, seg.to);
				mConverter.learn(new WnnWord(seg.string, stroke));
			}
		}
	}

	/**
	 * Forget a selected word.
	 *
	 * @param word  A selected word
	 */
	private void forgetWord(final WnnWord word) {
		if (word != null) {
			mConverter.forget(word);
		}
	}

	/**
	 * Fits an editor info.
	 *
	 * @param preferences  The preference data.
	 * @param info              The editor info.
	 */
	protected void fitInputType(final SharedPreferences pref, final EditorInfo info) {
		/*
		if (info.inputType == InputType.TYPE_NULL) {
			mDirectInputMode = true;
			return;
		}
		*/

		resetPrediction();

		mDisableAutoCommitEnglishMask &= ~AUTO_COMMIT_ENGLISH_OFF;
		int preferenceDictionary = EngineState.PREFERENCE_DICTIONARY_NONE;
		mEnableConverter = true;
		mEnableSymbolList = true;
		mEnableSymbolListNonHalf = true;
		mAutoCaps = getOrientPrefBoolean(pref, "auto_caps", false);
		mFilter.filter = 0;
		//mEnableAutoInsertSpace = true;
		mEnableAutoHideKeyboard = false;
		mPasswordInputMode = false;

		loadOption(pref);
		
		switch (info.inputType & InputType.TYPE_MASK_CLASS) {
			case InputType.TYPE_CLASS_NUMBER:
			case InputType.TYPE_CLASS_DATETIME:
				mEnableConverter = false;
				break;

			case InputType.TYPE_CLASS_PHONE:
				mEnableSymbolList = false;
				mEnableConverter = false;
				break;

			case InputType.TYPE_CLASS_TEXT:

				switch (info.inputType & InputType.TYPE_MASK_VARIATION) {
					case InputType.TYPE_TEXT_VARIATION_PERSON_NAME:
						preferenceDictionary = EngineState.PREFERENCE_DICTIONARY_PERSON_NAME;
						break;

					case InputType.TYPE_TEXT_VARIATION_PASSWORD:
						mEnableLearning = false;
						mEnableConverter = false;
						mEnableSymbolListNonHalf = false;
						mFilter.filter = CandidateFilter.FILTER_NON_ASCII;
						mDisableAutoCommitEnglishMask |= AUTO_COMMIT_ENGLISH_OFF;
						mEnableAutoHideKeyboard = true;
						mPasswordInputMode = true;
						break;

					case InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS:
						//mFilter.filter = CandidateFilter.FILTER_NON_ASCII;
						//mEnableSymbolListNonHalf = false;
						//mEnableAutoInsertSpace = false;
						//mDisableAutoCommitEnglishMask |= AUTO_COMMIT_ENGLISH_OFF;
						//preferenceDictionary = EngineState.PREFERENCE_DICTIONARY_EMAIL_ADDRESS_URI;
						break;

					case InputType.TYPE_TEXT_VARIATION_URI:
						mEnableAutoInsertSpace = false;
						//mDisableAutoCommitEnglishMask |= AUTO_COMMIT_ENGLISH_OFF;
						//preferenceDictionary = EngineState.PREFERENCE_DICTIONARY_EMAIL_ADDRESS_URI;
						break;

					case InputType.TYPE_TEXT_VARIATION_POSTAL_ADDRESS:
						preferenceDictionary = EngineState.PREFERENCE_DICTIONARY_POSTAL_ADDRESS;
						break;

					case InputType.TYPE_TEXT_VARIATION_PHONETIC:
						mEnableLearning = false;
						mEnableConverter = false;
						mEnableSymbolList = false;
						break;

					default:
						break;
				}
				break;

			default:
				break;
		}

		if (mFilter.filter == 0) {
			mConverterEN.setFilter(null);
			mConverterJAJP.setFilter(null);
		} else {
			mConverterEN.setFilter(mFilter);
			mConverterJAJP.setFilter(mFilter);
		}

		final EngineState state = new EngineState();
		state.preferenceDictionary = preferenceDictionary;
		state.convertType = EngineState.CONVERT_TYPE_NONE;
		state.keyboard = mEngineState.keyboard;
		updateEngineState(state);
		updateMetaKeyStateDisplay();

		checkTutorial(info.privateImeOptions);
	}

	/**
	 * Append a {@link StrSegment} to the composing text
	 * <br>
	 * If the length of the composing text exceeds
	 * {@code LIMIT_INPUT_NUMBER}, the appending operation is ignored.
	 *
	 * @param  str  Input segment
	 */
	private void appendStrSegment(final StrSegment str) {
		final ComposingText composingText = mComposingText;

		if (composingText.size(ComposingText.LAYER1) >= LIMIT_INPUT_NUMBER) {
			return; /* do nothing */
		}
		composingText.insertStrSegment(ComposingText.LAYER0, ComposingText.LAYER1, str);
		return;
	}

	/**
	 * Commit the consecutive clause conversion.
	 */
	private void commitConvertingText() {
		if (mEngineState.isConvertState()) {
			final int size = mComposingText.size(ComposingText.LAYER2);
			for (int i = 0; i < size; i++) {
				learnWord(i);
			}

			final String text = mComposingText.toString(ComposingText.LAYER2);
			mInputConnection.commitText(text, (FIX_CURSOR_TEXT_END ? 1 : text.length()));
			mPrevCommitText.append(text);
			mPrevCommitCount++;
			initializeScreen();
		}
	}

	/**
	 * Initialize the screen displayed by IME
	 */
	private void initializeScreen() {
		if (mComposingText.size(ComposingText.LAYER0) != 0) {
			mInputConnection.setComposingText("", 0);
		}
		mComposingText.clear();
		mExactMatchMode = false;
		mStatus = STATUS_INIT;
		mHandler.removeMessages(MSG_PREDICTION);
		final View candidateView = mCandidatesViewManager.getCurrentView();
		if ((candidateView != null) && candidateView.isShown()) {
			mCandidatesViewManager.clearCandidates();
			mDocomoEmojiCount = 0;
			resetPrediction();
		}
		mInputViewManager.onUpdateState(this);

		final EngineState state = new EngineState();
		state.temporaryMode = EngineState.TEMPORARY_DICTIONARY_MODE_NONE;
		updateEngineState(state);
	}

	/**
	 * Whether the tail of the string is alphabet or not.
	 *
	 * @param  str      The string
	 * @return          {@code true} if the tail is alphabet; {@code false} if otherwise.
	 */
	private boolean isAlphabetLast(final String str) {
		final Matcher m = ENGLISH_CHARACTER_LAST.matcher(str);
		return m.matches();
	}

	/** @see net.gorry.android.input.nicownng.NicoWnnG#onFinishInput */
	@Override public void onFinishInput() {
		if (mInputConnection != null) {
			initializeScreen();
		}
		super.onFinishInput();
	}

	/**
	 * Check whether or not the converter is active.
	 *
	 * @return {@code true} if the converter is active.
	 */
	private boolean isEnableL2Converter() {
		if ((mConverter == null) || !mEnableConverter) {
			return false;
		}

		if (mEngineState.isEnglish() && !mActionPrediction) {
			return false;
		}

		return true;
	}

	/**
	 * Handling KeyEvent(KEYUP)
	 * <br>
	 * This method is called from {@link #onEvent()}.
	 *
	 * @param ev   An up key event
	 */
	private void onKeyUpEvent(final KeyEvent ev) {
		final int key = ev.getKeyCode();
		boolean send = false;

		if (mRegisterLongPressHardwareKeyboardIS01MojiKey) {
			synchronized (this) {
				mHandlerLongPressHardwareKeyboardIS01MojiKey.removeCallbacks(mActionLongPressHardwareKeyboardIS01MojiKey);
				mRegisterLongPressHardwareKeyboardIS01MojiKey = false;
			}
			processKeyEvent(evHardwareKeyboardIS01MojiKey.keyEvent);
			return;
		}

		if (mRegisterLongPressHardwareKeyboardIS01EKaoKiKey) {
			synchronized (this) {
				mHandlerLongPressHardwareKeyboardIS01EKaoKiKey.removeCallbacks(mActionLongPressHardwareKeyboardIS01EKaoKiKey);
				mRegisterLongPressHardwareKeyboardIS01EKaoKiKey = false;
			}
			processKeyEvent(evHardwareKeyboardIS01EKaoKiKey.keyEvent);
			return;
		}

		if((key == KeyEvent.KEYCODE_SHIFT_LEFT) || (key == KeyEvent.KEYCODE_SHIFT_RIGHT)){
			send = true;
			if(!mShiftPressing){
				mHardShift = 0;
				mShiftPressing = true;
			}
			updateMetaKeyStateDisplay();
			if (mHardShift == 0) {
				mInputConnection.clearMetaKeyStates(KeyEvent.META_SHIFT_ON|KeyEvent.META_SHIFT_LEFT_ON|KeyEvent.META_SHIFT_RIGHT_ON);
			}
		} else {
			if (ev.isShiftPressed()) {
				mHardShift = 0;
				updateMetaKeyStateDisplay();
			}
		}
		
		if((key == KeyEvent.KEYCODE_ALT_LEFT) || (key == KeyEvent.KEYCODE_ALT_RIGHT)){
			send = true;
			if(!mAltPressing ){
				mHardAlt = 0;
				mAltPressing   = true;
			}
			updateMetaKeyStateDisplay();
			if (mHardAlt == 0) {
				mInputConnection.clearMetaKeyStates(KeyEvent.META_ALT_ON|KeyEvent.META_ALT_LEFT_ON|KeyEvent.META_ALT_RIGHT_ON);
			}
		} else {
			if (ev.isAltPressed()) {
				mHardAlt = 0;
				updateMetaKeyStateDisplay();
			}
		}
		
		// if((key == KeyEvent.KEYCODE_CTRL_LEFT) || (key == KeyEvent.KEYCODE_CTRL_RIGHT)){
		if((key == 113) || (key == 114)){
			send = true;
			if(!mCtrlPressing ){
				mHardCtrl = 0;
				mCtrlPressing   = true;
			}
			updateMetaKeyStateDisplay();
			if (mHardCtrl == 0) {
				// mInputConnection.clearMetaKeyStates(KeyEvent.META_CTRL_ON|KeyEvent.META_CTRL_LEFT_ON|KeyEvent.META_CTRL_RIGHT_ON);
				mInputConnection.clearMetaKeyStates(0x00001000|0x00002000|0x00004000);
			}
		} else {
			if (isCtrlPressed(ev)) {
				mHardCtrl = 0;
				updateMetaKeyStateDisplay();
			}
		}

		if (send) {
			mInputConnection.sendKeyEvent(ev);
		}
	}

	/**
	 * Initialize the committed text's information.
	 */
	private void initCommitInfoForWatchCursor() {
		if (!isEnableL2Converter()) {
			return;
		}

		mCommitStartCursor = mComposingStartCursor;
		mPrevCommitText.delete(0, mPrevCommitText.length());
	}

	/**
	 * Clear the commit text's info.
	 * @return {@code true}:cleared, {@code false}:has already cleared.
	 */
	private boolean clearCommitInfo() {
		if (mCommitStartCursor < 0) {
			return false;
		}

		mCommitStartCursor = -1;
		return true;
	}

	/**
	 * Verify the commit text.
	 */
	private void checkCommitInfo() {
		if (mCommitStartCursor < 0) {
			return;
		}

		final int composingLength = mComposingText.toString(mTargetLayer).length();
		CharSequence seq = mInputConnection.getTextBeforeCursor(mPrevCommitText.length() + composingLength, 0);
		if (seq != null) {
			int e = seq.length() - composingLength;
			if (e >= 0) {
				seq = seq.subSequence(0, seq.length() - composingLength);
				if (!seq.equals(mPrevCommitText.toString())) {
					mPrevCommitCount = 0;
					clearCommitInfo();
				}
			} else {
				Log.d("NicoWnnG", "checkCommitInfo(): e<0");
			}
		}
	}

	/**
	 * Check and start the tutorial if it is the tutorial mode.
	 *
	 * @param privateImeOptions IME's options
	 */
	private void checkTutorial(final String privateImeOptions) {
		if (privateImeOptions == null) return;
		if (privateImeOptions.equals("com.android.setupwizard:ShowTutorial")) {
			if ((mTutorial == null) && mEnableTutorial) startTutorial();
		} else if (privateImeOptions.equals("com.android.setupwizard:HideTutorial")) {
			if (mTutorial != null) {
				if (mTutorial.close()) {
					mTutorial = null;
				}
			}
		}
	}

	/**
	 * Start the tutorial
	 */
	private void startTutorial() {
		final DefaultSoftKeyboardJAJP manager = (DefaultSoftKeyboardJAJP) mInputViewManager;
		manager.setDefaultKeyboard();
		if (mEngineState.keyboard == EngineState.KEYBOARD_QWERTY) {
			manager.changeKeyboardType(DefaultSoftKeyboard.KEYBOARD_12KEY);
		}

		final DefaultSoftKeyboardJAJP inputManager = ((DefaultSoftKeyboardJAJP) mInputViewManager);
		final View v = inputManager.getKeyboardView();
		v.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(final View v, final MotionEvent event) {
				return true;
			}});
		mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_START_TUTORIAL), 500);
	}

	/**
	 * Close the tutorial
	 */
	public void tutorialDone() {
		mTutorial = null;
	}

	/** @see NicoWnnG#close */
	@Override protected void close() {
		mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_CLOSE), 0);
	}

	/**
	 * Break the sequence of words.
	 */
	private void breakSequence() {
		mEnableAutoDeleteSpace = false;
		mConverterJAJP.breakSequence();
		mConverterEN.breakSequence();
	}

	public void changeAlphaKanaDirectPhase() {
		if (mPreConverter != null) {
			mPreConverter.convert2(mComposingText);
		}
		final KanaConverter conv = new KanaConverter();
		final ArrayList<StrSegment> ssIn = mComposingText.getStringLayer(ComposingText.LAYER1);
		final StringBuilder sb = new StringBuilder();
		for (int i=0; i<ssIn.size(); i++) {
			final String sIn = ssIn.get(i).string;
			final String sOut = conv.Hiragana2FullKatakanaAndCapitalizeAlphabet(sIn);
			sb.append(sOut);
		}
		mComposingText.setCursor(ComposingText.LAYER2, 0); // mComposingText.size(ComposingText.LAYER2));
		final StrSegment[] ss = new StrSegment[1];
		ss[0] = new StrSegment(sb.toString(), 0, sb.length()-1);
		mComposingText.setStrSegment(ComposingText.LAYER2, ss);
		startConvert(EngineState.CONVERT_TYPE_KANA_DIRECT);
	}

	public boolean isRenbun() {
		return mEngineState.isRenbun();
	}

	public boolean isPredict() {
		return mEngineState.isPredict();
	}

	public boolean candidatesViewManagerIsShown() {
		return mCandidatesViewManager.getCurrentView().isShown();
	}
	
	public boolean candidatesViewManagerIsIndicated() {
		return (mCandidatesViewManager.getIndicateCandidateView() > 0);
	}
	
	public int candidatesViewTextSize() {
		return mCandidatesViewManager.getCandidateTextSize();
	}
	
    public void setSpecialCandidateOnKana12KeyToggleMode(int sw) {
        mConverterJAJP.setSpecialCandidateOnKana12KeyToggleMode(sw);
    }
    
    public int getSpecialCandidateOnKana12KeyToggleMode() {
        return mConverterJAJP.getSpecialCandidateOnKana12KeyToggleMode();
    }

    @Override
	public void setAutoForwardToggle12key() {
		final DefaultSoftKeyboard inputManager = ((DefaultSoftKeyboard) mInputViewManager);
		inputManager.setReverseKey();
    	super.setAutoForwardToggle12key();
	}

    @Override
	public void resetAutoForwardToggle12key() {
		final DefaultSoftKeyboard inputManager = ((DefaultSoftKeyboard) mInputViewManager);
		inputManager.restoreReverseKey();
    	super.resetAutoForwardToggle12key();
	}
    
    @Override
    public void loadOption(final SharedPreferences pref) {
    	super.loadOption(pref);

    	mEnableLearning   = pref.getBoolean("opt_enable_learning", true);
		//mEnablePrediction = pref.getBoolean("opt_prediction", true);
		mEnableSpellCorrection = pref.getBoolean("opt_spell_correction", true);
		mPredictionMode = Integer.valueOf(pref.getString("opt_prediction_mode2", "0"));
		mEnablePredictionAfterEnter = pref.getBoolean("opt_prediction_afterenter", true);
    }
    
	private int mLastInputMode = 0;
	public int getLastInputMode() {
		return mLastInputMode;
	}
	public void setLastInputMode(int mode) {
		mLastInputMode = mode;
	}
}
