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

import java.util.StringTokenizer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.inputmethodservice.InputMethodService;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

/**
 * The OpenWnn IME's base class.
 *
 * @author Copyright (C) 2009 OMRON SOFTWARE CO., LTD.  All Rights Reserved.
 */
public class NicoWnnG extends InputMethodService {
	//private static final String GETACTION_INTERCEPT = "net.gorry.android.input.nicownng.ACTION_INTERCEPT";
	//private static final String INPUTCONNECTION_KEY = "inputconnection_key";
	private static final String ACTION_INTERCEPT = "com.adamrocker.android.simeji.ACTION_INTERCEPT";
	private static final String CATEGORY_KEY = "com.adamrocker.android.simeji.REPLACE";
	private static final String REPLACE_KEY = "replace_key";
	private static final String INPUTCONNECTION_KEY = "inputconnection_key";


	// dic.setup
	public static final String writableDicJAJPBaseName = "writableJAJP.dic";
	public static final String writableDicENBaseName   = "writableEN.dic";
	public static final String outerUserDicENBaseName = "UserDicEN.xml";
	public static final String outerUserDicJAJPBaseName = "UserDicJAJP.xml";

//	public String writableJAJPDic = new String("/data/data/net.gorry.android.input.nicownng/databases/writableJAJP.dic");
//	public String writableENDic   = new String("/data/data/net.gorry.android.input.nicownng/databases/writableEN.dic");
	public String writableDicJAJPFileName = null;
	public String writableDicENFileName = null;

	public static final String INPUTMODE_NICO   = "input_nico";
	public static final String INPUTMODE_BELL   = "input_bell";
	public static final String INPUTMODE_NORMAL = "input_normal";
	public static final String INPUTMODE_NICO2  = "input_nico2";
	public static final String INPUTMODE_TEST   = "input_test";
	public static final String INPUTMODE_2TOUCH = "input_2touch";

	/** Candidate view */
//	protected CandidatesViewManager  mCandidatesViewManager = null;
	protected TextCandidatesViewManager  mCandidatesViewManager = null;
	/** Input view (software keyboard) */
	protected InputViewManager  mInputViewManager = null;
	protected String            mInputViewMode    = INPUTMODE_NORMAL;
	protected boolean           mInputViewFullScreenInLandscape = false;

	/** Conversion engine */
	protected WnnEngine  mConverter = null;
	/** Pre-converter (for Romaji-to-Kana input, Hangul input, etc.) */
	protected LetterConverter  mPreConverter = null;
	/** The inputing/editing string */
	protected ComposingText  mComposingText = null;
	/** The input connection */
	public InputConnection mInputConnection = null;
	/** Auto hide candidate view */
	protected boolean mAutoHideMode = true;
	/** Direct input mode */
	protected boolean mDirectInputMode = true;
	/** CandidatesView is Shown */
	protected boolean mCandidatesViewIsShown = false;
	/** Password input mode */
	protected boolean mPasswordInputMode = false;

	protected boolean mCleanAltPressed = true;

	/** Flag for checking if the previous down key event is consumed by OpenWnn  */
	private boolean mConsumeDownEvent;

	private static int mMachineType = 0;
	
	private static boolean mKeyCodeTest = false;

	private Context mContext = null;;
	/**
	 * Constructor
	 */
	public NicoWnnG() {
		super();
        // Log.w("NicoWnnG", "public NicoWnnG()");
	}

	public NicoWnnG(final Context context) {
		this();
		// Log.w("NicoWnnG", "NicoWnnG(final Context context)");
	}

	public void setContext(Context context) {
		// Log.w("NicoWnnG", "NicoWnnG#myConstructor(Context context)");
		try {
			if (mContext == null) {
				attachBaseContext(context);
				mContext = getBaseContext();
			}
		} catch (final Exception e) {
			//
		}
    	writableDicJAJPFileName = getDatabasePath(writableDicJAJPBaseName).getPath();
    	writableDicENFileName = getDatabasePath(writableDicENBaseName).getPath();
		// Log.w("NicoWnnG", "writableJAJPDic="+writableJAJPDic + ", writableENDic="+writableENDic);
		// Log.w("NicoWnnG", "getDir()="+getDir("hoge",Context.MODE_PRIVATE).getPath());
	}
	
	public Context getContext() {
		return mContext;
	}


	/***********************************************************************
	 * InputMethodService
	 **********************************************************************/
	/** @see android.inputmethodservice.InputMethodService#onCreate */
	@Override public void onCreate() {
		// Log.w("NicoWnnG", "NicoWnnG#onCreate()");
		super.onCreate();

		initializeEasySetting();
		convertOldPreferces();
		SymbolList.copyUserSymbolDicFileToExternalStorageDirectory(this, false);

		if (mConverter != null) { mConverter.init(); }
		if (mComposingText != null) { mComposingText.clear(); }

		registerMushroomReceiver();

		final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		final SharedPreferences.Editor editor = pref.edit();
		editor.putBoolean("keycode_test", false);
		editor.commit();

		boolean open_help_first = pref.getBoolean("open_help_first", false);
		if (!open_help_first) {
			openHelp();
		}
	}

	/** @see android.inputmethodservice.InputMethodService#onCreateCandidatesView */
	@Override public View onCreateCandidatesView() {
		Log.w("NicoWnnG", "NicoWnnG#onCreateCandidatesView()");

		if (mCandidatesViewManager != null) {
			final WindowManager wm = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
			final View view = mCandidatesViewManager.initView(this,
					wm.getDefaultDisplay().getWidth(),
					wm.getDefaultDisplay().getHeight());
			mCandidatesViewManager.setViewType(CandidatesViewManager.VIEW_TYPE_NORMAL);
			return view;
		} else {
			return super.onCreateCandidatesView();
		}
	}

	/** @see android.inputmethodservice.InputMethodService#onCreateInputView */
	@Override public View onCreateInputView() {
		Log.w("NicoWnnG", "NicoWnnG#onCreateInputView()");

		if (mInputViewManager != null) {
			final WindowManager wm = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
			return mInputViewManager.initView(this,
					wm.getDefaultDisplay().getWidth(),
					wm.getDefaultDisplay().getHeight());
		} else {
			return super.onCreateInputView();
		}
	}

	/** @see android.inputmethodservice.InputMethodService#onDestroy */
	@Override public void onDestroy() {
		super.onDestroy();

		unregisterMushroomReceiver();
		close();
	}

	private KeyEvent convKeyCode_JIS_on_US(KeyEvent event) {
		int scancode = event.getScanCode();
		if (mUseZenkakuKeyToMoji) {
			switch (scancode) {
				case 41:  // 全角キー
					return new KeyEvent(
						event.getDownTime(),
						event.getEventTime(),
						event.getAction(),
						DefaultSoftKeyboard.KEYCODE_IS01_MOJI, // event.getKeyCode(),
						event.getRepeatCount(),
						event.getMetaState(),
						event.getDeviceId(),
						event.getScanCode(),
						event.getFlags()
					);
			}
		}
		switch (scancode) {
		case 124:  // "￥ー"キーをUSモードで入力
			return new KeyEvent(
				event.getDownTime(),
				event.getEventTime(),
				event.getAction(),
				DefaultSoftKeyboard.KEYCODE_JIS_CHOUON, // event.getKeyCode(),
				event.getRepeatCount(),
				event.getMetaState(),
				event.getDeviceId(),
				event.getScanCode(),
				event.getFlags()
			);
		
		case 89:  // "ろ"キーをUSモードで入力
			return new KeyEvent(
				event.getDownTime(),
				event.getEventTime(),
				event.getAction(),
				DefaultSoftKeyboard.KEYCODE_JIS_RO, // event.getKeyCode(),
				event.getRepeatCount(),
				event.getMetaState(),
				event.getDeviceId(),
				event.getScanCode(),
				event.getFlags()
			);
			
		case 93:  // ひらがな/カタカナ/ローマ字キーをUSモードで入力
			return new KeyEvent(
				event.getDownTime(),
				event.getEventTime(),
				event.getAction(),
				DefaultSoftKeyboard.KEYCODE_JIS_HIRA_KATA, // event.getKeyCode(),
				event.getRepeatCount(),
				event.getMetaState(),
				event.getDeviceId(),
				event.getScanCode(),
				event.getFlags()
			);
			
		case 92:  // 変換/前候補キーをUSモードで入力
			return new KeyEvent(
				event.getDownTime(),
				event.getEventTime(),
				event.getAction(),
				DefaultSoftKeyboard.KEYCODE_JIS_PREV_CANDIDATE, // event.getKeyCode(),
				event.getRepeatCount(),
				event.getMetaState(),
				event.getDeviceId(),
				event.getScanCode(),
				event.getFlags()
			);
			
		case 94:  // 無変換キーをUSモードで入力
			return new KeyEvent(
				event.getDownTime(),
				event.getEventTime(),
				event.getAction(),
				DefaultSoftKeyboard.KEYCODE_JIS_NO_CONVERT, // event.getKeyCode(),
				event.getRepeatCount(),
				event.getMetaState(),
				event.getDeviceId(),
				event.getScanCode(),
				event.getFlags()
			);
		}
		return event;
	}

	private KeyEvent convKeyCode_JIS_on_JIS(KeyEvent event) {
		int scancode = event.getScanCode();
		if (mUseZenkakuKeyToMoji) {
			switch (scancode) {
				case 41:  // 全角キー
					return new KeyEvent(
						event.getDownTime(),
						event.getEventTime(),
						event.getAction(),
						DefaultSoftKeyboard.KEYCODE_IS01_MOJI, // event.getKeyCode(),
						event.getRepeatCount(),
						event.getMetaState(),
						event.getDeviceId(),
						event.getScanCode(),
						event.getFlags()
					);
			}
		}
		switch (scancode) {
		// TF300T対策
		case 124:  // "￥ー"キーをUSモードで入力
			return new KeyEvent(
				event.getDownTime(),
				event.getEventTime(),
				event.getAction(),
				DefaultSoftKeyboard.KEYCODE_JIS_CHOUON, // event.getKeyCode(),
				event.getRepeatCount(),
				event.getMetaState(),
				event.getDeviceId(),
				event.getScanCode(),
				event.getFlags()
			);
		/*
		case 89:  // "ろ"キーをUSモードで入力
			return new KeyEvent(
				event.getDownTime(),
				event.getEventTime(),
				event.getAction(),
				DefaultSoftKeyboard.KEYCODE_JIS_RO, // event.getKeyCode(),
				event.getRepeatCount(),
				event.getMetaState(),
				event.getDeviceId(),
				event.getScanCode(),
				event.getFlags()
			);
		*/
			
		case 93:  // ひらがな/カタカナ/ローマ字キー
			return new KeyEvent(
				event.getDownTime(),
				event.getEventTime(),
				event.getAction(),
				DefaultSoftKeyboard.KEYCODE_JIS_HIRA_KATA, // event.getKeyCode(),
				event.getRepeatCount(),
				event.getMetaState(),
				event.getDeviceId(),
				event.getScanCode(),
				event.getFlags()
			);
			
		case 92:  // 変換/前候補キー
			return new KeyEvent(
				event.getDownTime(),
				event.getEventTime(),
				event.getAction(),
				DefaultSoftKeyboard.KEYCODE_JIS_PREV_CANDIDATE, // event.getKeyCode(),
				event.getRepeatCount(),
				event.getMetaState(),
				event.getDeviceId(),
				event.getScanCode(),
				event.getFlags()
			);
			
		case 94:  // 無変換キー
			return new KeyEvent(
				event.getDownTime(),
				event.getEventTime(),
				event.getAction(),
				DefaultSoftKeyboard.KEYCODE_JIS_NO_CONVERT, // event.getKeyCode(),
				event.getRepeatCount(),
				event.getMetaState(),
				event.getDeviceId(),
				event.getScanCode(),
				event.getFlags()
			);
		}
		return event;
	}


	private KeyEvent convKeyCode_US_on_US(KeyEvent event) {
		int scancode = event.getScanCode();
//		switch (scancode) {
//		}
		return event;
	}

	/** @see android.inputmethodservice.InputMethodService#onKeyDown */
	@Override public boolean onKeyDown(final int keyCode, KeyEvent event) {
		boolean ret;
		// Log.w("Input", "keyCode="+keyCode+", scanCode="+event.getScanCode());
		if (mKeyCodeTest) {
			String msg = String.format("KeyDown: keyCode=%02X, scanCode=%02X, metaState=%08X, unicodeChar=%08X", keyCode, event.getScanCode(), event.getMetaState(), event.getUnicodeChar());
            Intent intent = new Intent("NICOWNNG_KEYCODETEST_ACTION");
            intent.putExtra("message", msg);
            sendBroadcast(intent);
		}
		switch (mUseConvertKeyMap) {
			case CONVERT_KEYMAP_KB_JIS_OS_US:
				event = convKeyCode_JIS_on_US(event);
				break;
			case CONVERT_KEYMAP_KB_US_OS_US:
				event = convKeyCode_US_on_US(event);
				break;
			case CONVERT_KEYMAP_KB_JIS_OS_JIS:
				event = convKeyCode_JIS_on_JIS(event);
				break;
			default:
				break;
		}
		if (mUseConvertKeyMap != CONVERT_KEYMAP_NONE) {
			int scancode = event.getScanCode();
			switch (scancode) {
			case 172:  // LBR-BTK1 ESCキーの位置にあるHOMEキー
				event = new KeyEvent(
					event.getDownTime(),
					event.getEventTime(),
					event.getAction(),
					KeyEvent.KEYCODE_BACK, // event.getKeyCode(),
					event.getRepeatCount(),
					event.getMetaState(),
					event.getDeviceId(),
					event.getScanCode(),
					event.getFlags()
				);
				break;
			case 244:  // NEC TerrainのVoiceキー
				event = new KeyEvent(
					event.getDownTime(),
					event.getEventTime(),
					event.getAction(),
					DefaultSoftKeyboard.KEYCODE_IS01_MOJI, // event.getKeyCode(),
					event.getRepeatCount(),
					event.getMetaState(),
					event.getDeviceId(),
					event.getScanCode(),
					event.getFlags()
				);
				break;
			}
		}
		mConsumeDownEvent = onEvent(new NicoWnnGEvent(event));
		ret = mConsumeDownEvent;
		if (!ret) {
			ret = super.onKeyDown(keyCode, event);
		}
		return ret;
	}

	/** @see android.inputmethodservice.InputMethodService#onKeyUp */
	@Override public boolean onKeyUp(final int keyCode, final KeyEvent event) {
		if (mKeyCodeTest) {
			String msg = String.format("KeyUp: keyCode=%02X, scanCode=%02X", keyCode, event.getScanCode());
            Intent intent = new Intent("NICOWNNG_KEYCODETEST_ACTION");
            intent.putExtra("message", msg);
            sendBroadcast(intent);
		}
		boolean ret = mConsumeDownEvent;
		if (!ret) {
			ret = super.onKeyUp(keyCode, event);
		}else{
			onEvent(new NicoWnnGEvent(event));
		}
		return ret;
	}

	/** @see android.inputmethodservice.InputMethodService#onStartInput */
	@Override public void onStartInput(final EditorInfo attribute, final boolean restarting) {
		super.onStartInput(attribute, restarting);
		mInputConnection = getCurrentInputConnection();
		if (mComposingText != null) {
			mComposingText.clear();
		}
	}

	/** @see android.inputmethodservice.InputMethodService#onStartInputView */
	@Override public void onStartInputView(final EditorInfo attribute, final boolean restarting) {
		final boolean isPortrait = (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT);
		setOrientPrefKeyMode(isPortrait);

		super.onStartInputView(attribute, restarting);
		mInputConnection = getCurrentInputConnection();

		setCandidatesViewShown(false);
		if (mInputConnection != null) {
			mDirectInputMode = false;
			if (mConverter != null) { mConverter.init(); }
		} else {
			mDirectInputMode = true;
		}
		final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		if (mCandidatesViewManager != null) { mCandidatesViewManager.setPreferences(pref);  }
		if (mInputViewManager != null) { mInputViewManager.setPreferences(pref, attribute);  }
		if (mPreConverter != null) { mPreConverter.setPreferences(pref);  }
		if (mConverter != null) { mConverter.setPreferences(pref);  }
	}

	/** @see android.inputmethodservice.InputMethodService#requestHideSelf */
	@Override public void requestHideSelf(final int flag) {
		super.requestHideSelf(flag);
		if (mInputViewManager == null) {
			hideWindow();
		}
	}

	/** @see android.inputmethodservice.InputMethodService#setCandidatesViewShown */
	@Override public void setCandidatesViewShown(final boolean shown) {
		super.setCandidatesViewShown(shown);
		setExtractViewShown(shown);
		if (shown) {
			showWindow(true);
		} else {
			if (mAutoHideMode && (mInputViewManager == null)) {
				hideWindow();
			}
		}
		mCandidatesViewIsShown = shown;
	}

	/** @see android.inputmethodservice.InputMethodService#hideWindow */
	@Override public void hideWindow() {
		super.hideWindow();
		mDirectInputMode = true;
		hideStatusIcon();
	}
	/** @see android.inputmethodservice.InputMethodService#onComputeInsets */
	@Override public void onComputeInsets(final InputMethodService.Insets outInsets) {
		super.onComputeInsets(outInsets);
		outInsets.contentTopInsets = outInsets.visibleTopInsets;
	}


	/**********************************************************************
	 * OpenWnn
	 **********************************************************************/
	/**
	 * Process an event.
	 *
	 * @param  ev  An event
	 * @return  {@code true} if the event is processed in this method; {@code false} if not.
	 */
	public boolean onEvent(final NicoWnnGEvent ev) {
		return false;
	}

	/**
	 * Search a character for toggle input.
	 *
	 * @param prevChar     The character input previous
	 * @param toggleTable  Toggle table
	 * @param reverse      {@code false} if toggle direction is forward, {@code true} if toggle direction is backward
	 * @return          A character ({@code null} if no character is found)
	 */
	protected String searchToggleCharacter(final String prevChar, final String[] toggleTable, final boolean reverse) {
		if (toggleTable == null) {
			return null;
		}
		for (int i = 0; i < toggleTable.length; i++) {
			if (prevChar.equals(toggleTable[i])) {
				if (reverse) {
					i--;
					if (i < 0) {
						return toggleTable[toggleTable.length - 1];
					} else {
						return toggleTable[i];
					}
				} else {
					i++;
					if (i == toggleTable.length) {
						return toggleTable[0];
					} else {
						return toggleTable[i];
					}
				}
			}
		}
		return null;
	}

	protected void close() {
		if (mConverter != null) { mConverter.close(); }
	}

	/**
	 * @return mCandidatesViewIsShown
	 */
	public boolean isCandidatesViewShown() {
		return mCandidatesViewIsShown;
	}


	/**
	 *
	 */
	public void openPreferenceSetting() {
		final Intent intent = new Intent(
				NicoWnnG.this, ActivityNicoWnnGSetting.class
		);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
	}

	/**
	 *
	 */
	public void openHelp() {
		final Intent intent = new Intent(
				NicoWnnG.this, NicoWnnGMain.class
		);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
	}

	/**
	 *
	 */
	public void openKeycodeTest() {
		final Intent intent = new Intent(
				NicoWnnG.this, KeyCodeTest.class
		);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
	}

	/**
	 *
	 */
	public void reloadFlags() {
		if (!reloadFlagsSemaphore) {
			reloadFlagsSemaphore = true;
			final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
			loadOption(pref);
			if (mInputViewManager != null) {
				if (mInputViewManager.getCurrentView() != null) {
					final WindowManager wm = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
					mInputViewManager.initView(
							this,
							wm.getDefaultDisplay().getWidth(),
							wm.getDefaultDisplay().getHeight()
							);
					if (NicoWnnGJAJP.getInstance() != null) {
						NicoWnnGJAJP.getInstance().startSoftKeyboard();
					}
				}
			}
			reloadFlagsSemaphore = false;
		}
	}
	private boolean reloadFlagsSemaphore = false;

	/**
	 *
	 */
	public String getComposingText(final int layer) {
		return mComposingText.toString(layer);
	}

	public boolean isPasswordInputMode() {
		return mPasswordInputMode;
	}
	/**
	 *
	 */
	private boolean bPrefKeyIsPortrait = false;

	public void setOrientPrefKeyMode(final boolean isPortrait) {
		bPrefKeyIsPortrait = isPortrait;
	}

	public boolean getOrientPrefKeyMode() {
		return bPrefKeyIsPortrait;
	}

	public String getOrientPrefString(final SharedPreferences pref, final String key, final String defValue) {
		if (bPrefKeyIsPortrait) {
			return (pref.getString(key + "_portrait", defValue));
		}
		return (pref.getString(key + "_landscape", defValue));
	}

	public int getOrientPrefInteger(final SharedPreferences pref, final String key, final int defValue) {
		if (bPrefKeyIsPortrait) {
			return (pref.getInt(key + "_portrait", defValue));
		}
		return (pref.getInt(key + "_landscape", defValue));
	}

	public boolean getOrientPrefBoolean(final SharedPreferences pref, final String key, final Boolean defValue) {
		if (bPrefKeyIsPortrait) {
			return (pref.getBoolean(key + "_portrait", defValue));
		}
		return (pref.getBoolean(key + "_landscape", defValue));
	}

	public void convertOldPreferces() {
		final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		if ( !pref.getBoolean("new_preference_20110417a", false) ) {
			final SharedPreferences.Editor editor = pref.edit();

			// copyOldPreferenceString(pref, editor, "mainview_height_mode");
			// copyOldPreferenceString(pref, editor, "mainview_height_mode2");
			// copyOldPreferenceString(pref, editor, "qwerty_kana_mode");
			copyOldPreferenceBoolean(pref, editor, "qwerty_matrix_mode");
			// copyOldPreferenceString(pref, editor, "nicoflick_mode");
			// copyOldPreferenceString(pref, editor, "flick_sensitivity_mode");
			// copyOldPreferenceString(pref, editor, "input_mode");
			copyOldPreferenceBoolean(pref, editor, "auto_caps");
			copyOldPreferenceBoolean(pref, editor, "is_skip_space");
			copyOldPreferenceBoolean(pref, editor, "nospace_candidate2");
			// copyOldPreferenceString(pref, editor, "candidateview_height_mode");
			// copyOldPreferenceString(pref, editor, "candidateview_height_mode2");
			copyOldPreferenceBoolean(pref, editor, "change_noalpha_qwerty");
			copyOldPreferenceBoolean(pref, editor, "change_nonumber_qwerty");
			copyOldPreferenceBoolean(pref, editor, "no_flip_screen");

			{
				final String name = "input_mode";
				String n1 = NicoWnnG.INPUTMODE_NORMAL;
				String n2 = NicoWnnG.INPUTMODE_NORMAL;
				if (pref.contains(name)) {
					final String str = pref.getString(name, NicoWnnG.INPUTMODE_NORMAL);
					n1 = str;
					n2 = str;
					editor.remove(name);
					editor.putString(name + "_portrait", n1);
					editor.putString(name + "_landscape", n2);
				}
			}

			{
				final String name = "change_alphanum_12key";
				int n1 = 1;
				int n2 = 0;
				if (pref.contains(name)) {
					final Boolean b = pref.getBoolean(name, false);
					n1 = (b ? 1 : 0);
					n2 = (b ? 1 : 0);
					editor.remove(name);
					editor.putBoolean(name + "_portrait", (n1 != 0));
					editor.putBoolean(name + "_landscape", (n2 != 0));
					final String name2 = "change_num_12key";
					editor.putBoolean(name2 + "_portrait", (n1 != 0));
					editor.putBoolean(name2 + "_landscape", (n2 != 0));
				}
			}

			{
				final String name = "change_kana_12key";
				int n1 = 1;
				int n2 = 0;
				if (pref.contains(name)) {
					final Boolean b = pref.getBoolean(name, false);
					n1 = (b ? 1 : 0);
					n2 = (b ? 1 : 0);
					editor.remove(name);
					editor.putBoolean(name + "_portrait", (n1 != 0));
					editor.putBoolean(name + "_landscape", (n2 != 0));
				}
			}

			{
				final String name = "candidateview_height_mode2";
				int n = 2;
				if (pref.contains(name)) {
					final String str = pref.getString(name, "2");
					if (name.equals("candidateview_value_0")) {
						n = 0;
					} else if (name.equals("candidateview_value_1")) {
						n = 1;
					} else if (name.equals("candidateview_value_2")) {
						n = 2;
					} else if (name.equals("candidateview_value_3")) {
						n = 3;
					} else if (name.equals("candidateview_value_4")) {
						n = 4;
					}
					editor.remove(name);
					editor.putString(name + "_portrait", String.valueOf(n));
					editor.putString(name + "_landscape", String.valueOf(n));
				}
			}

			{
				final String name = "mainview_height_mode2";
				int n = 2;
				if (pref.contains(name)) {
					final String str = pref.getString(name, "2");
					if (name.equals("mainview_value_0")) {
						n = 0;
					} else if (name.equals("mainview_value_1")) {
						n = 1;
					} else if (name.equals("mainview_value_2")) {
						n = 2;
					} else if (name.equals("mainview_value_3")) {
						n = 3;
					} else if (name.equals("mainview_value_4")) {
						n = 4;
					}
					editor.remove(name);
					editor.putString(name + "_portrait", String.valueOf(n));
					editor.putString(name + "_landscape", String.valueOf(n));
				}
			}

			{
				final String name = "nicoflick_mode";
				int n = 0;
				if (pref.contains(name)) {
					final String str = pref.getString(name, "0");
					if (name.equals("normal_stroke")) {
						n = 1;
					} else if (name.equals("nico_stroke")) {
						n = 2;
					}
					editor.remove(name);
					editor.putString(name + "_portrait", String.valueOf(n));
					editor.putString(name + "_landscape", String.valueOf(n));
				}
			}

			{
				final String name = "flick_sensitivity_mode";
				int n = 0;
				if (pref.contains(name)) {
					final String str = pref.getString(name, "1");
					if (name.equals("sensitivity_value_1")) {
						n = 1;
					} else if (name.equals("sensitivity_value_2")) {
						n = 2;
					} else if (name.equals("sensitivity_value_3")) {
						n = 3;
					} else if (name.equals("sensitivity_value_4")) {
						n = 4;
					}
					editor.remove(name);
					editor.putString(name + "_portrait", String.valueOf(n));
					editor.putString(name + "_landscape", String.valueOf(n));
				}
			}

			{
				final String name = "nico_candidate_mode";
				int portraitLine = 3;
				int landscapeLine = 1;
				if (pref.contains(name)) {
					final String lineMode = pref.getString(name, "1_1");
					if (lineMode.equals("1_1")) {
						portraitLine = 1;
						landscapeLine = 1;
					} else if (lineMode.equals("2_1")) {
						portraitLine = 2;
						landscapeLine = 1;
					} else if (lineMode.equals("1_2")) {
						portraitLine = 1;
						landscapeLine = 2;
					} else if (lineMode.equals("2_2")) {
						portraitLine = 2;
						landscapeLine = 2;
					}
					editor.remove("nico_candidate_mode");
					editor.putString("nico_candidate_lines" + "_portrait", String.valueOf(portraitLine));
					editor.putString("nico_candidate_lines" + "_landscape", String.valueOf(landscapeLine));
				}
			}

			editor.putBoolean("new_preference_20110417a", true);
			editor.commit();
		}

		if ( !pref.getBoolean("new_preference_20120209a", false) ) {
			final SharedPreferences.Editor editor = pref.edit();

			copyPreferenceBoolean(pref, editor, "change_alphanum_12key", "change_num_12key");
			copyPreferenceString(pref, editor, "hidden_softkeyboard2", "hidden_softkeyboard4");
			copyPreferenceString(pref, editor, "qwerty_kana_mode", "qwerty_kana_mode3");

			editor.putBoolean("new_preference_20120209a", true);
			editor.commit();
		}

		if ( !pref.getBoolean("new_preference_20120813a", false) ) {
			final SharedPreferences.Editor editor = pref.edit();

			{
				final String name = "subten_12key_mode_portrait";
				if (pref.contains(name)) {
					final String str = pref.getString(name, "0");
					int n1 = Integer.parseInt(str);
					if (n1 == 1) n1 = 2;
					editor.remove(name);
					editor.putString("subten_12key_mode2_portrait", String.valueOf(n1));
				}
			}

			{
				final String name = "subten_12key_mode_landscape";
				if (pref.contains(name)) {
					final String str = pref.getString(name, "0");
					int n1 = Integer.parseInt(str);
					if (n1 == 1) n1 = 2;
					editor.remove(name);
					editor.putString("subten_12key_mode2_landscape", String.valueOf(n1));
				}
			}

			editor.putBoolean("new_preference_20120813a", true);
			editor.commit();
		}

		if ( !pref.getBoolean("new_preference_20121021a", false) ) {
			final SharedPreferences.Editor editor = pref.edit();

			copyPreferenceBoolean(pref, editor, "change_alphanum_12key", "change_alphanum_12key_onhardkey");
			copyPreferenceBoolean(pref, editor, "change_kana_12key", "change_kana_12key_onhardkey");
			copyPreferenceBoolean(pref, editor, "change_noalpha_qwerty", "change_noalpha_qwerty_onhardkey");
			copyPreferenceBoolean(pref, editor, "change_nonumber_qwerty", "change_nonumber_qwerty_onhardkey");
			copyPreferenceBoolean(pref, editor, "change_num_12key", "change_num_12key_onhardkey");

			editor.putBoolean("new_preference_20121021a", true);
			editor.commit();
		}

		if ( !pref.getBoolean("new_preference_20121107a", false) ) {
			final SharedPreferences.Editor editor = pref.edit();

			int mode = 3;
			if (Build.VERSION.SDK_INT >= 11) {
				mode = 1;
			}

			{
				final String name = "cutpasteaction_byime_landscape";
				if (!pref.contains(name)) {
					editor.putString("cutpasteaction_byime_landscape", String.valueOf(mode));
				}
			}
			{
				final String name = "cutpasteaction_byime_portrait";
				if (!pref.contains(name)) {
					editor.putString("cutpasteaction_byime_portrait", String.valueOf(mode));
				}
			}

			editor.putBoolean("new_preference_20121107a", true);
			editor.commit();
		}

	}

	private void copyOldPreferenceString(final SharedPreferences pref, final SharedPreferences.Editor editor, final String key ) {
		if (pref.contains(key)) {
			final String value = pref.getString(key, "");
			editor.putString(key + "_portrait", value);
			editor.putString(key + "_landscape", value);
			editor.remove(key);
		}
	}

	private void copyOldPreferenceInteger(final SharedPreferences pref, final SharedPreferences.Editor editor, final String key ) {
		if (pref.contains(key)) {
			final int value = pref.getInt(key, 0);
			editor.putInt(key + "_portrait", value);
			editor.putInt(key + "_landscape", value);
			editor.remove(key);
		}
	}

	private void copyOldPreferenceBoolean(final SharedPreferences pref, final SharedPreferences.Editor editor, final String key ) {
		if (pref.contains(key)) {
			final boolean value = pref.getBoolean(key, false);
			editor.putBoolean(key + "_portrait", value);
			editor.putBoolean(key + "_landscape", value);
			editor.remove(key);
		}
	}

	private void copyPreferenceBoolean(final SharedPreferences pref, final SharedPreferences.Editor editor, final String key, final String key2 ) {
		String pl;
		pl = "_portrait";
		if (pref.contains(key + pl)) {
			if (!pref.contains(key2 + pl)) {
				final boolean value = pref.getBoolean(key + pl, false);
				editor.putBoolean(key2 + pl, value);
			}
		}
		pl = "_landscape";
		if (pref.contains(key + pl)) {
			if (!pref.contains(key2 + pl)) {
				final boolean value = pref.getBoolean(key + pl, false);
				editor.putBoolean(key2 + pl, value);
			}
		}
	}

	private void copyPreferenceString(final SharedPreferences pref, final SharedPreferences.Editor editor, final String key, final String key2 ) {
		String pl;
		pl = "_portrait";
		if (pref.contains(key + pl)) {
			if (!pref.contains(key2 + pl)) {
				final String value = pref.getString(key + pl, "");
				editor.putString(key2 + pl, value);
			}
		}
		pl = "_landscape";
		if (pref.contains(key + pl)) {
			if (!pref.contains(key2 + pl)) {
				final String value = pref.getString(key + pl, "");
				editor.putString(key2 + pl, value);
			}
		}
	}

	/**
	 * mushroom
	 */

	private View vMushroomFocus;
	private String mMushroomWord = "";
	final private Handler hMushroom = new Handler();
	final private Runnable rMushroom = new Runnable() {
		public void run() {
			final InputConnection ic = getCurrentInputConnection();
			if ((mMushroomWord != null) && (mMushroomWord.length() > 0)) {
				ic.commitText(mMushroomWord, 1);
				if ((mMushroomSrc != null) && (mMushroomSrc.length() > 0)) {
					if (mConverter != null) {
						WnnWord word = new WnnWord(mMushroomWord, mMushroomSrc);
						mConverter.learn(word);
					}
				}
			}

			sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_CENTER);
		}
	};

	private String mMushroomSrc = "";
	public void invokeMushroom(final String src) {
		mMushroomSrc = src;
		// hideWindow();
		final Intent intent = new Intent(
				this, Mushroom.class
		);
		intent.setAction("net.gorry.android.input.nicownng.ACTION_INTERCEPT");
		intent.putExtra("replace_key", src);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
	}

	private final MushroomReceiver mReceiver = new MushroomReceiver();

	public void registerMushroomReceiver() {
		final IntentFilter filter = new IntentFilter(Mushroom.ACTION);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			registerReceiver(mReceiver, filter, Context.RECEIVER_EXPORTED);
		} else {
			registerReceiver(mReceiver, filter);
		}
	}

	public void unregisterMushroomReceiver() {
		unregisterReceiver(mReceiver);
	}

	public void onMushroom(final Context context, final Intent intent) {
		final Bundle extras = intent.getExtras();
		mMushroomWord = "";
		if (extras != null) {
			mMushroomWord = extras.getString("replace_key");
		}
		final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		int wait = Integer.valueOf(pref.getString("wait_mushroom", "3"));
		hMushroom.postDelayed(rMushroom, wait*200);
	}

	public class MushroomReceiver extends BroadcastReceiver {
		@Override
		public synchronized void onReceive(final Context context, final Intent intent) {
			onMushroom(context, intent);
		}

	}

	public int getMachineType() {
		return mMachineType;
	}

	public void initializeEasySetting() {
		// 画面サイズに合わせて初期キーボードサイズを設定
		WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		DisplayMetrics metrics = new DisplayMetrics();
		display.getMetrics(metrics);
		float dpi = metrics.ydpi;
		int shorter = display.getHeight();
		if (shorter > display.getWidth()) {
			shorter = display.getWidth();
			dpi = metrics.xdpi;
		}
		float shorterinch = shorter / dpi;
		if (shorterinch > 4.5) {
			mMachineType = 2;
		} else if (shorterinch > 3.0) {
			mMachineType = 1;
		} else {
			mMachineType = 0;
		}

		final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		if (pref.getString("mainview_height_mode2" + "_portrait", "").length() == 0) {
			int size;
			switch (mMachineType) {
				default:
				case 0:
					size = 3;
					if (shorter >= 240) size = 4;
					if (shorter > 320) size = 5;
					if (shorter > 480) size = 6;
					if (shorter > 600) size = 7;
					if (shorter > 720) size = 8;
					setEasySetting_Phone("1_1", "2_3", size);
					break;
				case 1:
					size = 1;
					if (shorter >= 320) size = 2;
					if (shorter > 480) size = 3;
					if (shorter > 600) size = 4;
					if (shorter > 720) size = 5;
					if (shorter > 800) size = 6;
					setEasySetting_Tablet("2_3", "2_3", size);
					break;
				case 2:
					size = 0;
					if (shorter >= 320) size = 1;
					if (shorter > 480) size = 2;
					if (shorter > 600) size = 3;
					if (shorter > 720) size = 4;
					if (shorter > 800) size = 5;
					setEasySetting_Tablet("2_1", "2_1", size);
					break;
			}
		}
	}

	public void setEasySetting_Tablet(final String portrait, final String landscape, final int size) {
		int different_pl = 1;
		int p_nicoflick_mode = -1;
		int p_flick_sensitivity_mode = -1;
		String p_input_mode = null;
		int p_change_kana_12key = -1;
		int p_change_alphanum_12key = -1;
		int p_change_num_12key = -1;
		int p_size = -1;
		int p_maxline = -1;
		int p_candidateheight = -1;
		int p_textsize = -1;
		int p_kanamode = -1;
		int l_nicoflick_mode = -1;
		int l_flick_sensitivity_mode = -1;
		String l_input_mode = null;
		int l_change_kana_12key = -1;
		int l_change_alphanum_12key = -1;
		int l_change_num_12key = -1;
		int l_size = -1;
		int l_maxline = -1;
		int l_candidateheight = -1;
		int l_textsize = -1;
		int l_kanamode = -1;

		int candidateheight = size;
		if (candidateheight > 4) {
			candidateheight = 4;
		}

		final int p_keytype1, p_keytype2;
		{
			StringTokenizer st = new StringTokenizer(portrait, "_");
			p_keytype1 = Integer.parseInt(st.nextToken());
			p_keytype2 = Integer.parseInt(st.nextToken());
		}
		switch (p_keytype1) {
			default:
			case 1:  // 12key
				switch (p_keytype2) {
					default:
					case 1:  // トグル
						p_nicoflick_mode = 0;
						p_flick_sensitivity_mode = size;
						p_input_mode = NicoWnnG.INPUTMODE_NORMAL;
						p_change_kana_12key = 1;
						p_change_alphanum_12key = 1;
						p_change_num_12key = 1;
						p_size = size;
						p_maxline = 3;
						p_candidateheight = candidateheight;
						p_textsize = candidateheight;
						p_kanamode = 8;
						break;
					case 2:  // フリック
						p_nicoflick_mode = 1;
						p_flick_sensitivity_mode = size;
						p_input_mode = NicoWnnG.INPUTMODE_NORMAL;
						p_change_kana_12key = 1;
						p_change_alphanum_12key = 1;
						p_change_num_12key = 1;
						p_size = size;
						p_maxline = 3;
						p_candidateheight = candidateheight;
						p_textsize = candidateheight;
						p_kanamode = 8;
						break;
					case 3:  // 2タッチ
						p_nicoflick_mode = 0;
						p_flick_sensitivity_mode = size;
						p_input_mode = NicoWnnG.INPUTMODE_2TOUCH;
						p_change_kana_12key = 1;
						p_change_alphanum_12key = 1;
						p_change_num_12key = 1;
						p_size = size;
						p_maxline = 3;
						p_candidateheight = candidateheight;
						p_textsize = candidateheight;
						p_kanamode = 8;
						break;
				}
				break;
			case 2:  // qwerty
				switch (p_keytype2) {
					default:
					case 1:  // ローマ字(default)
						p_nicoflick_mode = 0;
						p_flick_sensitivity_mode = size;
						p_input_mode = NicoWnnG.INPUTMODE_NORMAL;
						p_change_kana_12key = 0;
						p_change_alphanum_12key = 0;
						p_change_num_12key = 0;
						p_size = size;
						p_maxline = 3;
						p_candidateheight = candidateheight;
						p_textsize = candidateheight;
						p_kanamode = 4;
						break;
					case 2:  // ローマ字(コンパクト)
						p_nicoflick_mode = 0;
						p_flick_sensitivity_mode = size;
						p_input_mode = NicoWnnG.INPUTMODE_NORMAL;
						p_change_kana_12key = 0;
						p_change_alphanum_12key = 0;
						p_change_num_12key = 0;
						p_size = size;
						p_maxline = 3;
						p_candidateheight = candidateheight;
						p_textsize = candidateheight;
						p_kanamode = 8;
						break;
					case 3:  // ローマ字(ミニ)
						p_nicoflick_mode = 0;
						p_flick_sensitivity_mode = size;
						p_input_mode = NicoWnnG.INPUTMODE_NORMAL;
						p_change_kana_12key = 0;
						p_change_alphanum_12key = 0;
						p_change_num_12key = 0;
						p_size = size;
						p_maxline = 3;
						p_candidateheight = candidateheight;
						p_textsize = candidateheight;
						p_kanamode = 3;
						break;
					case 4:  // ローマ字(ミニ横)
						p_nicoflick_mode = 0;
						p_flick_sensitivity_mode = size;
						p_input_mode = NicoWnnG.INPUTMODE_NORMAL;
						p_change_kana_12key = 0;
						p_change_alphanum_12key = 0;
						p_change_num_12key = 0;
						p_size = size;
						p_maxline = 3;
						p_candidateheight = candidateheight;
						p_textsize = candidateheight;
						p_kanamode = 7;
						break;
					case 5:  // JISかな
						p_nicoflick_mode = 0;
						p_flick_sensitivity_mode = size;
						p_input_mode = NicoWnnG.INPUTMODE_NORMAL;
						p_change_kana_12key = 0;
						p_change_alphanum_12key = 0;
						p_change_num_12key = 0;
						p_size = size;
						p_maxline = 3;
						p_candidateheight = candidateheight;
						p_textsize = candidateheight;
						p_kanamode = 5;
						break;
					case 6:  // 五十音かな
						p_nicoflick_mode = 0;
						p_flick_sensitivity_mode = size;
						p_input_mode = NicoWnnG.INPUTMODE_NORMAL;
						p_change_kana_12key = 0;
						p_change_alphanum_12key = 0;
						p_change_num_12key = 0;
						p_size = size;
						p_maxline = 3;
						p_candidateheight = candidateheight;
						p_textsize = candidateheight;
						p_kanamode = 6;
						break;
					case 7:  // ローマ字(旧)
						p_nicoflick_mode = 0;
						p_flick_sensitivity_mode = size;
						p_input_mode = NicoWnnG.INPUTMODE_NORMAL;
						p_change_kana_12key = 0;
						p_change_alphanum_12key = 0;
						p_change_num_12key = 0;
						p_size = size;
						p_maxline = 3;
						p_candidateheight = candidateheight;
						p_textsize = candidateheight;
						p_kanamode = 0;
						break;
					case 8:  // JISかな(旧)
						p_nicoflick_mode = 0;
						p_flick_sensitivity_mode = size;
						p_input_mode = NicoWnnG.INPUTMODE_NORMAL;
						p_change_kana_12key = 0;
						p_change_alphanum_12key = 0;
						p_change_num_12key = 0;
						p_size = size;
						p_maxline = 3;
						p_candidateheight = candidateheight;
						p_textsize = candidateheight;
						p_kanamode = 1;
						break;
					case 9:  // 五十音かな(旧)
						p_nicoflick_mode = 0;
						p_flick_sensitivity_mode = size;
						p_input_mode = NicoWnnG.INPUTMODE_NORMAL;
						p_change_kana_12key = 0;
						p_change_alphanum_12key = 0;
						p_change_num_12key = 0;
						p_size = size;
						p_maxline = 3;
						p_candidateheight = candidateheight;
						p_textsize = candidateheight;
						p_kanamode = 2;
						break;
				}
				break;
		}
		
		final int l_keytype1, l_keytype2;
		{
			StringTokenizer st = new StringTokenizer(landscape, "_");
			l_keytype1 = Integer.parseInt(st.nextToken());
			l_keytype2 = Integer.parseInt(st.nextToken());
		}
		switch (l_keytype1) {
			default:
			case 1:  // 12key
				switch (l_keytype2) {
					default:
					case 1:  // トグル
						l_nicoflick_mode = 0;
						l_flick_sensitivity_mode = size;
						l_input_mode = NicoWnnG.INPUTMODE_NORMAL;
						l_change_kana_12key = 1;
						l_change_alphanum_12key = 1;
						l_change_num_12key = 1;
						l_size = size;
						l_maxline = 1;
						l_candidateheight = candidateheight;
						l_textsize = candidateheight;
						l_kanamode = 0;
						break;
					case 2:  // フリック
						l_nicoflick_mode = 1;
						l_flick_sensitivity_mode = size;
						l_input_mode = NicoWnnG.INPUTMODE_NORMAL;
						l_change_kana_12key = 1;
						l_change_alphanum_12key = 1;
						l_change_num_12key = 1;
						l_size = size;
						l_maxline = 1;
						l_candidateheight = candidateheight;
						l_textsize = candidateheight;
						l_kanamode = 0;
						break;
					case 3:  // 2タッチ
						l_nicoflick_mode = 0;
						l_flick_sensitivity_mode = size;
						l_input_mode = NicoWnnG.INPUTMODE_2TOUCH;
						l_change_kana_12key = 1;
						l_change_alphanum_12key = 1;
						l_change_num_12key = 1;
						l_size = size;
						l_maxline = 1;
						l_candidateheight = candidateheight;
						l_textsize = candidateheight;
						l_kanamode = 0;
						break;
				}
				break;
			case 2:  // qwerty
				switch (l_keytype2) {
					default:
					case 1:  // ローマ字(default)
						l_nicoflick_mode = 0;
						l_flick_sensitivity_mode = size;
						l_input_mode = NicoWnnG.INPUTMODE_NORMAL;
						l_change_kana_12key = 0;
						l_change_alphanum_12key = 0;
						l_change_num_12key = 0;
						l_size = size;
						l_maxline = 1;
						l_candidateheight = candidateheight;
						l_textsize = candidateheight;
						l_kanamode = 4;
						break;
					case 2:  // ローマ字(コンパクト)
						l_nicoflick_mode = 0;
						l_flick_sensitivity_mode = size;
						l_input_mode = NicoWnnG.INPUTMODE_NORMAL;
						l_change_kana_12key = 0;
						l_change_alphanum_12key = 0;
						l_change_num_12key = 0;
						l_size = size;
						l_maxline = 1;
						l_candidateheight = candidateheight;
						l_textsize = candidateheight;
						l_kanamode = 8;
						break;
					case 3:  // ローマ字(ミニ)
						l_nicoflick_mode = 0;
						l_flick_sensitivity_mode = size;
						l_input_mode = NicoWnnG.INPUTMODE_NORMAL;
						l_change_kana_12key = 0;
						l_change_alphanum_12key = 0;
						l_change_num_12key = 0;
						l_size = size;
						l_maxline = 1;
						l_candidateheight = candidateheight;
						l_textsize = candidateheight;
						l_kanamode = 3;
						break;
					case 4:  // ローマ字(ミニ横)
						l_nicoflick_mode = 0;
						l_flick_sensitivity_mode = size;
						l_input_mode = NicoWnnG.INPUTMODE_NORMAL;
						l_change_kana_12key = 0;
						l_change_alphanum_12key = 0;
						l_change_num_12key = 0;
						l_size = size;
						l_maxline = 1;
						l_candidateheight = candidateheight;
						l_textsize = candidateheight;
						l_kanamode = 7;
						break;
					case 5:  // JISかな
						l_nicoflick_mode = 0;
						l_flick_sensitivity_mode = size;
						l_input_mode = NicoWnnG.INPUTMODE_NORMAL;
						l_change_kana_12key = 0;
						l_change_alphanum_12key = 0;
						l_change_num_12key = 0;
						l_size = size;
						l_maxline = 1;
						l_candidateheight = candidateheight;
						l_textsize = candidateheight;
						l_kanamode = 5;
						break;
					case 6:  // 五十音かな
						l_nicoflick_mode = 0;
						l_flick_sensitivity_mode = size;
						l_input_mode = NicoWnnG.INPUTMODE_NORMAL;
						l_change_kana_12key = 0;
						l_change_alphanum_12key = 0;
						l_change_num_12key = 0;
						l_size = size;
						l_maxline = 1;
						l_candidateheight = candidateheight;
						l_textsize = candidateheight;
						l_kanamode = 6;
						break;
					case 7:  // ローマ字(旧)
						l_nicoflick_mode = 0;
						l_flick_sensitivity_mode = size;
						l_input_mode = NicoWnnG.INPUTMODE_NORMAL;
						l_change_kana_12key = 0;
						l_change_alphanum_12key = 0;
						l_change_num_12key = 0;
						l_size = size;
						l_maxline = 1;
						l_candidateheight = candidateheight;
						l_textsize = candidateheight;
						l_kanamode = 0;
						break;
					case 8:  // JISかな(旧)
						l_nicoflick_mode = 0;
						l_flick_sensitivity_mode = size;
						l_input_mode = NicoWnnG.INPUTMODE_NORMAL;
						l_change_kana_12key = 0;
						l_change_alphanum_12key = 0;
						l_change_num_12key = 0;
						l_size = size;
						l_maxline = 1;
						l_candidateheight = candidateheight;
						l_textsize = candidateheight;
						l_kanamode = 1;
						break;
					case 9:  // 五十音かな(旧)
						l_nicoflick_mode = 0;
						l_flick_sensitivity_mode = size;
						l_input_mode = NicoWnnG.INPUTMODE_NORMAL;
						l_change_kana_12key = 0;
						l_change_alphanum_12key = 0;
						l_change_num_12key = 0;
						l_size = size;
						l_maxline = 1;
						l_candidateheight = candidateheight;
						l_textsize = candidateheight;
						l_kanamode = 2;
						break;
				}
				break;
		}

		final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		final SharedPreferences.Editor editor = pref.edit();
		String pl;

		pl = "_portrait";
		if (different_pl >= 0) {
			editor.putBoolean("different_pl", (different_pl > 0) );
		}
		if (p_nicoflick_mode >= 0) {
			editor.putString("nicoflick_mode"+pl, String.valueOf(p_nicoflick_mode));
		}
		if (p_flick_sensitivity_mode >= 1) {
			editor.putString("flick_sensitivity_mode"+pl, String.valueOf(p_flick_sensitivity_mode-1));
		}
		if (p_input_mode != null) {
			editor.putString("input_mode"+pl, p_input_mode);
		}
		if (p_change_kana_12key >= 0) {
			editor.putBoolean("change_kana_12key"+pl, (p_change_kana_12key > 0) );
		}
		if (p_change_alphanum_12key >= 0) {
			editor.putBoolean("change_alphanum_12key"+pl, (p_change_alphanum_12key > 0) );
		}
		if (p_change_num_12key >= 0) {
			editor.putBoolean("change_num_12key"+pl, (p_change_num_12key > 0) );
		}
		if (p_size >= 1) {
			editor.putString("mainview_height_mode2"+pl, String.valueOf(p_size-1));
		}
		if (p_maxline >= 1) {
			editor.putString("nico_candidate_lines"+pl, String.valueOf(p_maxline));
		}
		if (p_candidateheight >= 1) {
			editor.putString("candidateview_height_mode2"+pl, String.valueOf(p_candidateheight-1));
		}
		if (p_textsize >= 1) {
			editor.putString("candidate_font_size"+pl, String.valueOf(p_textsize-1));
		}
		if (p_kanamode >= 0) {
			editor.putString("qwerty_kana_mode3"+pl, String.valueOf(p_kanamode));
		}

		pl = "_landscape";
		if (l_nicoflick_mode >= 0) {
			editor.putString("nicoflick_mode"+pl, String.valueOf(l_nicoflick_mode));
		}
		if (l_flick_sensitivity_mode >= 1) {
			editor.putString("flick_sensitivity_mode"+pl, String.valueOf(l_flick_sensitivity_mode-1));
		}
		if (l_input_mode != null) {
			editor.putString("input_mode"+pl, l_input_mode);
		}
		if (l_change_kana_12key >= 0) {
			editor.putBoolean("change_kana_12key"+pl, (l_change_kana_12key > 0) );
		}
		if (l_change_alphanum_12key >= 0) {
			editor.putBoolean("change_alphanum_12key"+pl, (l_change_alphanum_12key > 0) );
		}
		if (l_change_num_12key >= 0) {
			editor.putBoolean("change_num_12key"+pl, (l_change_num_12key > 0) );
		}
		if (l_size >= 1) {
			editor.putString("mainview_height_mode2"+pl, String.valueOf(l_size-1));
		}
		if (l_maxline >= 1) {
			editor.putString("nico_candidate_lines"+pl, String.valueOf(l_maxline));
		}
		if (l_candidateheight >= 1) {
			editor.putString("candidateview_height_mode2"+pl, String.valueOf(l_candidateheight-1));
		}
		if (l_textsize >= 1) {
			editor.putString("candidate_font_size"+pl, String.valueOf(l_textsize-1));
		}
		if (l_kanamode >= 0) {
			editor.putString("qwerty_kana_mode3"+pl, String.valueOf(l_kanamode));
		}

		editor.commit();

		reloadFlags();
	}

	public void setEasySetting_Phone(final String portrait, final String landscape, final int size) {
		final int size2 = size;
		int different_pl = 1;
		int p_nicoflick_mode = -1;
		int p_flick_sensitivity_mode = -1;
		String p_input_mode = null;
		int p_change_kana_12key = -1;
		int p_change_alphanum_12key = -1;
		int p_change_num_12key = -1;
		int p_size = -1;
		int p_maxline = -1;
		int p_candidateheight = -1;
		int p_textsize = -1;
		int p_kanamode = -1;
		int l_nicoflick_mode = -1;
		int l_flick_sensitivity_mode = -1;
		String l_input_mode = null;
		int l_change_kana_12key = -1;
		int l_change_alphanum_12key = -1;
		int l_change_num_12key = -1;
		int l_size = -1;
		int l_maxline = -1;
		int l_candidateheight = -1;
		int l_textsize = -1;
		int l_kanamode = -1;

		int candidateheight = size;
		if (candidateheight > 4) {
			candidateheight = 4;
		}

		int candidateheight2 = size;
		if (candidateheight2 > 4) {
			candidateheight2 = 4;
		}

		final int p_keytype1, p_keytype2;
		{
			StringTokenizer st = new StringTokenizer(portrait, "_");
			p_keytype1 = Integer.parseInt(st.nextToken());
			p_keytype2 = Integer.parseInt(st.nextToken());
		}
		switch (p_keytype1) {
			default:
			case 1:  // 12key
				switch (p_keytype2) {
					default:
					case 1:  // トグル
						p_nicoflick_mode = 0;
						p_flick_sensitivity_mode = size;
						p_input_mode = NicoWnnG.INPUTMODE_NORMAL;
						p_change_kana_12key = 1;
						p_change_alphanum_12key = 1;
						p_change_num_12key = 1;
						p_size = size;
						p_maxline = 3;
						p_candidateheight = candidateheight;
						p_textsize = candidateheight;
						p_kanamode = 0;
						break;
					case 2:  // フリック
						p_nicoflick_mode = 1;
						p_flick_sensitivity_mode = size;
						p_input_mode = NicoWnnG.INPUTMODE_NORMAL;
						p_change_kana_12key = 1;
						p_change_alphanum_12key = 1;
						p_change_num_12key = 1;
						p_size = size;
						p_maxline = 3;
						p_candidateheight = candidateheight;
						p_textsize = candidateheight;
						p_kanamode = 0;
						break;
					case 3:  // 2タッチ
						p_nicoflick_mode = 0;
						p_flick_sensitivity_mode = size;
						p_input_mode = NicoWnnG.INPUTMODE_2TOUCH;
						p_change_kana_12key = 1;
						p_change_alphanum_12key = 1;
						p_change_num_12key = 1;
						p_size = size;
						p_maxline = 3;
						p_candidateheight = candidateheight;
						p_textsize = candidateheight;
						p_kanamode = 0;
						break;
				}
				break;
			case 2:  // qwerty
				switch (p_keytype2) {
					default:
					case 1:  // ローマ字(default)
						p_nicoflick_mode = 0;
						p_flick_sensitivity_mode = size;
						p_input_mode = NicoWnnG.INPUTMODE_NORMAL;
						p_change_kana_12key = 0;
						p_change_alphanum_12key = 0;
						p_change_num_12key = 0;
						p_size = size;
						p_maxline = 3;
						p_candidateheight = candidateheight;
						p_textsize = candidateheight;
						p_kanamode = 4;
						break;
					case 2:  // ローマ字(コンパクト)
						p_nicoflick_mode = 0;
						p_flick_sensitivity_mode = size;
						p_input_mode = NicoWnnG.INPUTMODE_NORMAL;
						p_change_kana_12key = 0;
						p_change_alphanum_12key = 0;
						p_change_num_12key = 0;
						p_size = size;
						p_maxline = 3;
						p_candidateheight = candidateheight;
						p_textsize = candidateheight;
						p_kanamode = 8;
						break;
					case 3:  // ローマ字(ミニ)
						p_nicoflick_mode = 0;
						p_flick_sensitivity_mode = size;
						p_input_mode = NicoWnnG.INPUTMODE_NORMAL;
						p_change_kana_12key = 0;
						p_change_alphanum_12key = 0;
						p_change_num_12key = 0;
						p_size = size;
						p_maxline = 3;
						p_candidateheight = candidateheight;
						p_textsize = candidateheight;
						p_kanamode = 3;
						break;
					case 4:  // ローマ字(ミニ横)
						p_nicoflick_mode = 0;
						p_flick_sensitivity_mode = size;
						p_input_mode = NicoWnnG.INPUTMODE_NORMAL;
						p_change_kana_12key = 0;
						p_change_alphanum_12key = 0;
						p_change_num_12key = 0;
						p_size = size;
						p_maxline = 3;
						p_candidateheight = candidateheight;
						p_textsize = candidateheight;
						p_kanamode = 7;
						break;
					case 5:  // JISかな
						p_nicoflick_mode = 0;
						p_flick_sensitivity_mode = size;
						p_input_mode = NicoWnnG.INPUTMODE_NORMAL;
						p_change_kana_12key = 0;
						p_change_alphanum_12key = 0;
						p_change_num_12key = 0;
						p_size = size;
						p_maxline = 3;
						p_candidateheight = candidateheight;
						p_textsize = candidateheight;
						p_kanamode = 5;
						break;
					case 6:  // 五十音かな
						p_nicoflick_mode = 0;
						p_flick_sensitivity_mode = size;
						p_input_mode = NicoWnnG.INPUTMODE_NORMAL;
						p_change_kana_12key = 0;
						p_change_alphanum_12key = 0;
						p_change_num_12key = 0;
						p_size = size;
						p_maxline = 3;
						p_candidateheight = candidateheight;
						p_textsize = candidateheight;
						p_kanamode = 6;
						break;
					case 7:  // ローマ字(旧)
						p_nicoflick_mode = 0;
						p_flick_sensitivity_mode = size;
						p_input_mode = NicoWnnG.INPUTMODE_NORMAL;
						p_change_kana_12key = 0;
						p_change_alphanum_12key = 0;
						p_change_num_12key = 0;
						p_size = size;
						p_maxline = 3;
						p_candidateheight = candidateheight;
						p_textsize = candidateheight;
						p_kanamode = 0;
						break;
					case 8:  // JISかな(旧)
						p_nicoflick_mode = 0;
						p_flick_sensitivity_mode = size;
						p_input_mode = NicoWnnG.INPUTMODE_NORMAL;
						p_change_kana_12key = 0;
						p_change_alphanum_12key = 0;
						p_change_num_12key = 0;
						p_size = size;
						p_maxline = 3;
						p_candidateheight = candidateheight;
						p_textsize = candidateheight;
						p_kanamode = 1;
						break;
					case 9:  // 五十音かな(旧)
						p_nicoflick_mode = 0;
						p_flick_sensitivity_mode = size;
						p_input_mode = NicoWnnG.INPUTMODE_NORMAL;
						p_change_kana_12key = 0;
						p_change_alphanum_12key = 0;
						p_change_num_12key = 0;
						p_size = size;
						p_maxline = 3;
						p_candidateheight = candidateheight;
						p_textsize = candidateheight;
						p_kanamode = 2;
						break;
				}
				break;
		}
		
		final int l_keytype1, l_keytype2;
		{
			StringTokenizer st = new StringTokenizer(landscape, "_");
			l_keytype1 = Integer.parseInt(st.nextToken());
			l_keytype2 = Integer.parseInt(st.nextToken());
		}
		switch (l_keytype1) {
			default:
			case 1:  // 12key
				switch (l_keytype2) {
					default:
					case 1:  // トグル
						l_nicoflick_mode = 0;
						l_flick_sensitivity_mode = size2;
						l_input_mode = NicoWnnG.INPUTMODE_NORMAL;
						l_change_kana_12key = 1;
						l_change_alphanum_12key = 1;
						l_change_num_12key = 1;
						l_size = size2;
						l_maxline = 1;
						l_candidateheight = candidateheight2;
						l_textsize = candidateheight2;
						l_kanamode = 0;
						break;
					case 2:  // フリック
						l_nicoflick_mode = 1;
						l_flick_sensitivity_mode = size2;
						l_input_mode = NicoWnnG.INPUTMODE_NORMAL;
						l_change_kana_12key = 1;
						l_change_alphanum_12key = 1;
						l_change_num_12key = 1;
						l_size = size2;
						l_maxline = 1;
						l_candidateheight = candidateheight2;
						l_textsize = candidateheight2;
						l_kanamode = 0;
						break;
					case 3:  // 2タッチ
						l_nicoflick_mode = 0;
						l_flick_sensitivity_mode = size2;
						l_input_mode = NicoWnnG.INPUTMODE_2TOUCH;
						l_change_kana_12key = 1;
						l_change_alphanum_12key = 1;
						l_change_num_12key = 1;
						l_size = size2;
						l_maxline = 1;
						l_candidateheight = candidateheight2;
						l_textsize = candidateheight2;
						l_kanamode = 0;
						break;
				}
				break;
			case 2:  // qwerty
				switch (l_keytype2) {
					default:
					case 1:  // ローマ字(default)
						l_nicoflick_mode = 0;
						l_flick_sensitivity_mode = size2;
						l_input_mode = NicoWnnG.INPUTMODE_NORMAL;
						l_change_kana_12key = 0;
						l_change_alphanum_12key = 0;
						l_change_num_12key = 0;
						l_size = size2;
						l_maxline = 1;
						l_candidateheight = candidateheight2;
						l_textsize = candidateheight2;
						l_kanamode = 4;
						break;
					case 2:  // ローマ字(コンパクト)
						l_nicoflick_mode = 0;
						l_flick_sensitivity_mode = size2;
						l_input_mode = NicoWnnG.INPUTMODE_NORMAL;
						l_change_kana_12key = 0;
						l_change_alphanum_12key = 0;
						l_change_num_12key = 0;
						l_size = size2;
						l_maxline = 1;
						l_candidateheight = candidateheight2;
						l_textsize = candidateheight2;
						l_kanamode = 8;
						break;
					case 3:  // ローマ字(ミニ)
						l_nicoflick_mode = 0;
						l_flick_sensitivity_mode = size2;
						l_input_mode = NicoWnnG.INPUTMODE_NORMAL;
						l_change_kana_12key = 0;
						l_change_alphanum_12key = 0;
						l_change_num_12key = 0;
						l_size = size2;
						l_maxline = 1;
						l_candidateheight = candidateheight2;
						l_textsize = candidateheight2;
						l_kanamode = 3;
						break;
					case 4:  // ローマ字(ミニ横)
						l_nicoflick_mode = 0;
						l_flick_sensitivity_mode = size2;
						l_input_mode = NicoWnnG.INPUTMODE_NORMAL;
						l_change_kana_12key = 0;
						l_change_alphanum_12key = 0;
						l_change_num_12key = 0;
						l_size = size2;
						l_maxline = 1;
						l_candidateheight = candidateheight2;
						l_textsize = candidateheight2;
						l_kanamode = 7;
						break;
					case 5:  // JISかな
						l_nicoflick_mode = 0;
						l_flick_sensitivity_mode = size2;
						l_input_mode = NicoWnnG.INPUTMODE_NORMAL;
						l_change_kana_12key = 0;
						l_change_alphanum_12key = 0;
						l_change_num_12key = 0;
						l_size = size2;
						l_maxline = 1;
						l_candidateheight = candidateheight2;
						l_textsize = candidateheight2;
						l_kanamode = 5;
						break;
					case 6:  // 五十音かな
						l_nicoflick_mode = 0;
						l_flick_sensitivity_mode = size2;
						l_input_mode = NicoWnnG.INPUTMODE_NORMAL;
						l_change_kana_12key = 0;
						l_change_alphanum_12key = 0;
						l_change_num_12key = 0;
						l_size = size2;
						l_maxline = 1;
						l_candidateheight = candidateheight2;
						l_textsize = candidateheight2;
						l_kanamode = 6;
						break;
					case 7:  // ローマ字(旧)
						l_nicoflick_mode = 0;
						l_flick_sensitivity_mode = size2;
						l_input_mode = NicoWnnG.INPUTMODE_NORMAL;
						l_change_kana_12key = 0;
						l_change_alphanum_12key = 0;
						l_change_num_12key = 0;
						l_size = size2;
						l_maxline = 1;
						l_candidateheight = candidateheight2;
						l_textsize = candidateheight2;
						l_kanamode = 0;
						break;
					case 8:  // JISかな(旧)
						l_nicoflick_mode = 0;
						l_flick_sensitivity_mode = size2;
						l_input_mode = NicoWnnG.INPUTMODE_NORMAL;
						l_change_kana_12key = 0;
						l_change_alphanum_12key = 0;
						l_change_num_12key = 0;
						l_size = size2;
						l_maxline = 1;
						l_candidateheight = candidateheight2;
						l_textsize = candidateheight2;
						l_kanamode = 1;
						break;
					case 9:  // 五十音かな(旧)
						l_nicoflick_mode = 0;
						l_flick_sensitivity_mode = size2;
						l_input_mode = NicoWnnG.INPUTMODE_NORMAL;
						l_change_kana_12key = 0;
						l_change_alphanum_12key = 0;
						l_change_num_12key = 0;
						l_size = size2;
						l_maxline = 1;
						l_candidateheight = candidateheight2;
						l_textsize = candidateheight2;
						l_kanamode = 2;
						break;
				}
				break;
		}

		final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		final SharedPreferences.Editor editor = pref.edit();
		String pl;

		pl = "_portrait";
		if (different_pl >= 0) {
			editor.putBoolean("different_pl", (different_pl > 0) );
		}
		if (p_nicoflick_mode >= 0) {
			editor.putString("nicoflick_mode"+pl, String.valueOf(p_nicoflick_mode));
		}
		if (p_flick_sensitivity_mode >= 1) {
			editor.putString("flick_sensitivity_mode"+pl, String.valueOf(p_flick_sensitivity_mode-1));
		}
		if (p_input_mode != null) {
			editor.putString("input_mode"+pl, p_input_mode);
		}
		if (p_change_kana_12key >= 0) {
			editor.putBoolean("change_kana_12key"+pl, (p_change_kana_12key > 0) );
		}
		if (p_change_alphanum_12key >= 0) {
			editor.putBoolean("change_alphanum_12key"+pl, (p_change_alphanum_12key > 0) );
		}
		if (p_change_num_12key >= 0) {
			editor.putBoolean("change_num_12key"+pl, (p_change_num_12key > 0) );
		}
		if (p_size >= 1) {
			editor.putString("mainview_height_mode2"+pl, String.valueOf(p_size-1));
		}
		if (p_maxline >= 1) {
			editor.putString("nico_candidate_lines"+pl, String.valueOf(p_maxline));
		}
		if (p_candidateheight >= 1) {
			editor.putString("candidateview_height_mode2"+pl, String.valueOf(p_candidateheight-1));
		}
		if (p_textsize >= 1) {
			editor.putString("candidate_font_size"+pl, String.valueOf(p_textsize-1));
		}
		if (p_kanamode >= 0) {
			editor.putString("qwerty_kana_mode3"+pl, String.valueOf(p_kanamode));
		}

		pl = "_landscape";
		if (l_nicoflick_mode >= 0) {
			editor.putString("nicoflick_mode"+pl, String.valueOf(l_nicoflick_mode));
		}
		if (l_flick_sensitivity_mode >= 1) {
			editor.putString("flick_sensitivity_mode"+pl, String.valueOf(l_flick_sensitivity_mode-1));
		}
		if (l_input_mode != null) {
			editor.putString("input_mode"+pl, l_input_mode);
		}
		if (l_change_kana_12key >= 0) {
			editor.putBoolean("change_kana_12key"+pl, (l_change_kana_12key > 0) );
		}
		if (l_change_alphanum_12key >= 0) {
			editor.putBoolean("change_alphanum_12key"+pl, (l_change_alphanum_12key > 0) );
		}
		if (l_change_num_12key >= 0) {
			editor.putBoolean("change_num_12key"+pl, (l_change_num_12key > 0) );
		}
		if (l_size >= 1) {
			editor.putString("mainview_height_mode2"+pl, String.valueOf(l_size-1));
		}
		if (l_maxline >= 1) {
			editor.putString("nico_candidate_lines"+pl, String.valueOf(l_maxline));
		}
		if (l_candidateheight >= 1) {
			editor.putString("candidateview_height_mode2"+pl, String.valueOf(l_candidateheight-1));
		}
		if (l_textsize >= 1) {
			editor.putString("candidate_font_size"+pl, String.valueOf(l_textsize-1));
		}
		if (l_kanamode >= 0) {
			editor.putString("qwerty_kana_mode3"+pl, String.valueOf(l_kanamode));
		}

		editor.commit();

		reloadFlags();
	}


	private boolean mRegisterAutoForwardToggle12key = false;
	private final int mTimeOutAutoForwardToggle12key = 700;
	private final Handler mHandlerAutoForwardToggle12key = new Handler();
	private final Runnable mActionAutoForwardToggle12key = new Runnable() {
		public void run() {
			synchronized (this) {
				if (mRegisterAutoForwardToggle12key) {
					resetAutoForwardToggle12key();
					// onEvent(mEventInputDpadRight);
					onEvent(mEventForwardToggle);
				}
			}
		}
	};

	public boolean getAutoForwardToggle12key() {
		return mRegisterAutoForwardToggle12key;
	}

	public void setAutoForwardToggle12key() {
		mRegisterAutoForwardToggle12key = true;
		mHandlerAutoForwardToggle12key.postDelayed(mActionAutoForwardToggle12key, mTimeOutAutoForwardToggle12key);
	}
	
	public void resetAutoForwardToggle12key() {
		mRegisterAutoForwardToggle12key = false;
		mHandlerAutoForwardToggle12key.removeCallbacks(mActionAutoForwardToggle12key);
		
	}
	
	
	public static final NicoWnnGEvent mEventResetStatus      = new NicoWnnGEvent(NicoWnnGEvent.RESET_STATUS);

	public static final NicoWnnGEvent mEventTouchOtherKey    = new NicoWnnGEvent(NicoWnnGEvent.TOUCH_OTHER_KEY);
	public static final NicoWnnGEvent mEventCommitText       = new NicoWnnGEvent(NicoWnnGEvent.COMMIT_COMPOSING_TEXT);
	public static final NicoWnnGEvent mEventConvert          = new NicoWnnGEvent(NicoWnnGEvent.CONVERT);
	public static final NicoWnnGEvent mEventConvertPredict   = new NicoWnnGEvent(NicoWnnGEvent.CONVERT_PREDICT);
	public static final NicoWnnGEvent mEventConvertPredictBackward   = new NicoWnnGEvent(NicoWnnGEvent.CONVERT_PREDICT_BACKWARD);

	public static final NicoWnnGEvent mEventChangeMode12Key    = new NicoWnnGEvent(NicoWnnGEvent.CHANGE_MODE, NicoWnnGJAJP.ENGINE_MODE_OPT_TYPE_12KEY);
	public static final NicoWnnGEvent mEventChangeModeQwerty   = new NicoWnnGEvent(NicoWnnGEvent.CHANGE_MODE, NicoWnnGJAJP.ENGINE_MODE_OPT_TYPE_QWERTY);
	public static final NicoWnnGEvent mEventChangeModeSymbol   = new NicoWnnGEvent(NicoWnnGEvent.CHANGE_MODE, NicoWnnGJAJP.ENGINE_MODE_SYMBOL);
	public static final NicoWnnGEvent mEventChangeModeDocomo   = new NicoWnnGEvent(NicoWnnGEvent.CHANGE_MODE, NicoWnnGJAJP.ENGINE_MODE_DOCOMOSYMBOL);
	public static final NicoWnnGEvent mEventChangeModeEisuKana = new NicoWnnGEvent(NicoWnnGEvent.CHANGE_MODE, NicoWnnGJAJP.ENGINE_MODE_EISU_KANA);

	public static final NicoWnnGEvent mEventInputShiftLeft   = new NicoWnnGEvent(NicoWnnGEvent.INPUT_SOFT_KEY, new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_SHIFT_LEFT));

	public static final NicoWnnGEvent mEventInputKeyDel      = new NicoWnnGEvent(NicoWnnGEvent.INPUT_SOFT_KEY, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
	public static final NicoWnnGEvent mEventInputEnter       = new NicoWnnGEvent(NicoWnnGEvent.INPUT_SOFT_KEY, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
	public static final NicoWnnGEvent mEventInputBack        = new NicoWnnGEvent(NicoWnnGEvent.INPUT_KEY, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK));
	public static final NicoWnnGEvent mEventInputDpadLeft    = new NicoWnnGEvent(NicoWnnGEvent.INPUT_SOFT_KEY, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_LEFT));
	public static final NicoWnnGEvent mEventInputDpadRight   = new NicoWnnGEvent(NicoWnnGEvent.INPUT_SOFT_KEY, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_RIGHT));
	public static final NicoWnnGEvent mEventInputDpadUp      = new NicoWnnGEvent(NicoWnnGEvent.INPUT_SOFT_KEY, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_UP));
	public static final NicoWnnGEvent mEventInputDpadDown    = new NicoWnnGEvent(NicoWnnGEvent.INPUT_SOFT_KEY, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_DOWN));

	public static final NicoWnnGEvent mEventForwardToggle    = new NicoWnnGEvent(NicoWnnGEvent.FORWARD_TOGGLE);

	/** 候補選択に左右矢印キーを使う */
	protected boolean mUseLeftRightKeyCandidateSelection = true;

	/** キーボード配列の変換 */
	public static final int CONVERT_KEYMAP_KB_JIS_OS_JIS = 0;
	public static final int CONVERT_KEYMAP_KB_JIS_OS_US = 1;
	public static final int CONVERT_KEYMAP_KB_US_OS_US = 2;
	public static final int CONVERT_KEYMAP_NONE = 3;
	protected int mUseConvertKeyMap = CONVERT_KEYMAP_KB_JIS_OS_US;
	protected boolean mUseZenkakuKeyToMoji = true;

	/** Whether auto-spacing is enabled or not. */
	protected boolean mEnableAutoInsertSpace = true;


    public boolean checkOption(final SharedPreferences pref) {
    	boolean b;

    	// mEnableAutoInsertSpaceはチェックしない
    	// mUseConvertKeyMapはチェックしない
    	// mUseZenkakuKeyToMojiはチェックしない

    	b = pref.getBoolean("fullscreen", false);
		if (mInputViewFullScreenInLandscape != b) {
			return true;
		}
		b = getOrientPrefBoolean(pref, "candidate_leftrightkey", true);
		if (mUseLeftRightKeyCandidateSelection != b) {
			return true;
		}
		b = pref.getBoolean("keycode_test", false);
		if (mKeyCodeTest != b) {
			return true;
		}

		return false;
    }

    public void loadOption(final SharedPreferences pref) {
		mUseZenkakuKeyToMoji = Boolean.valueOf(getOrientPrefBoolean(pref, "use_zenkaku_to_moji", true));
		mUseConvertKeyMap = Integer.valueOf(getOrientPrefString(pref, "convert_keymap_type", "0"));
		mEnableAutoInsertSpace = getOrientPrefBoolean(pref, "is_skip_space", true);
		mUseLeftRightKeyCandidateSelection = getOrientPrefBoolean(pref, "candidate_leftrightkey", true);
		mInputViewFullScreenInLandscape = pref.getBoolean("fullscreen", false);
		mKeyCodeTest = pref.getBoolean("keycode_test", false);
    }

    /*
    public boolean getUseLeftRightKeyCandidateSelection() {
    	return mUseLeftRightKeyCandidateSelection;
    }
    */

}
