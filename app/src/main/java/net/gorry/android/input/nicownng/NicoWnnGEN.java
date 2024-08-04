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

import net.gorry.android.input.nicownng.EN.DefaultSoftKeyboardEN;
import net.gorry.android.input.nicownng.EN.NicoWnnGEngineEN;
import net.gorry.android.input.nicownng.EN.TutorialEN;
import net.gorry.android.input.nicownng.JAJP.NicoWnnGEngineJAJP;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Handler;
import android.os.Message;
import androidx.preference.PreferenceManager;
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

/**
 * The OpenWnn English IME class.
 *
 * @author Copyright (C) 2009 OMRON SOFTWARE CO., LTD.  All Rights Reserved.
 */
public class NicoWnnGEN extends NicoWnnG {
	/** A space character */
	private static final char[] SPACE = {' '};

	/** Character style of underline */
	private static final CharacterStyle SPAN_UNDERLINE   = new UnderlineSpan();
	/** Highlight color style for the selected string  */
	private static final CharacterStyle SPAN_EXACT_BGCOLOR_HL     = new BackgroundColorSpan(0xFF66CDAA);
	/** Highlight color style for the composing text */
	private static final CharacterStyle SPAN_REMAIN_BGCOLOR_HL    = new BackgroundColorSpan(0xFFF0FFFF);
	/** Highlight text color */
	private static final CharacterStyle SPAN_TEXTCOLOR  = new ForegroundColorSpan(0xFF000000);

	/** A private area code(ALT+SHIFT+X) to be ignore (G1 specific). */
	private static final int PRIVATE_AREA_CODE = 61184;
	/** Never move cursor in to the composing text (adapting to IMF's specification change) */
	private static final boolean FIX_CURSOR_TEXT_END = true;

	/** Spannable string for the composing text */
	protected SpannableStringBuilder mDisplayText;

	/** Characters treated as a separator */
	private String mWordSeparators;
	/** Previous event's code */
	private int mPreviousEventCode;

	/** Array of words from the user dictionary */
	private WnnWord[] mUserDictionaryWords = null;

	/** The converter for English prediction/spell correction */
	private NicoWnnGEngineEN mConverterEN;
	/** The symbol list generator */
	private SymbolList mSymbolList;
	/** Whether it is displaying symbol list */
	private boolean mSymbolMode;
	/** Whether prediction is enabled */
	private boolean mOptPrediction;
	/** Whether spell correction is enabled */
	private boolean mOptSpellCorrection;
	/** Whether learning is enabled */
	private boolean mOptLearning;

	/** SHIFT key state */
	private int mHardShift;
	/** SHIFT key state (pressing) */
	private boolean mShiftPressing;
	/** ALT key state */
	private int mHardAlt;
	/** ALT key state (pressing) */
	private boolean mAltPressing;

	/** Instance of this service */
	private static NicoWnnGEN mSelf = null;

	/** Shift lock toggle definition */
	private static final int[] mShiftKeyToggle = {0, MetaKeyKeyListener.META_SHIFT_ON, MetaKeyKeyListener.META_CAP_LOCKED};
	/** ALT lock toggle definition */
	private static final int[] mAltKeyToggle = {0, MetaKeyKeyListener.META_ALT_ON, MetaKeyKeyListener.META_ALT_LOCKED};
	/** Auto caps mode */
	private boolean mAutoCaps = false;

	/** Whether dismissing the keyboard when the enter key is pressed */
	private boolean mEnableAutoHideKeyboard = true;

	/** Tutorial */
	private TutorialEN mTutorial;

	/** Whether tutorial mode or not */
	private boolean mEnableTutorial;

	/** Message for {@code mHandler} (execute prediction) */
	private static final int MSG_PREDICTION = 0;

	/** Message for {@code mHandler} (execute tutorial) */
	private static final int MSG_START_TUTORIAL = 1;

	/** Message for {@code mHandler} (close) */
	private static final int MSG_CLOSE = 2;

	/** Delay time(msec.) to start prediction after key input when the candidates view is not shown. */
	private static final int PREDICTION_DELAY_MS_1ST = 200;

	/** Delay time(msec.) to start prediction after key input when the candidates view is shown. */
	private static final int PREDICTION_DELAY_MS_SHOWING_CANDIDATE = 200;

	/** {@code Handler} for drawing candidates/displaying tutorial */
	Handler mHandler = new Handler() {
		@Override public void handleMessage(final Message msg) {
			switch (msg.what) {
				case MSG_PREDICTION:
					updatePrediction();
					break;
				case MSG_START_TUTORIAL:
					if (mTutorial == null) {
						if (isInputViewShown()) {
							final DefaultSoftKeyboardEN inputManager = ((DefaultSoftKeyboardEN) mInputViewManager);
							final View v = inputManager.getKeyboardView();
							mTutorial = new TutorialEN(NicoWnnGEN.this, v, inputManager);

							mTutorial.start();
						} else {
							/* Try again soon if the view is not yet showing */
							sendMessageDelayed(obtainMessage(MSG_START_TUTORIAL), 100);
						}
					}
					break;
				case MSG_CLOSE:
					if (mConverterEN != null) mConverterEN.close();
					if (mSymbolList != null) mSymbolList.close();
					break;
			}
		}
	};

	/**
	 * Constructor
	 */
	public NicoWnnGEN() {
		super();
		// Log.w("NicoWnnG", "NicoWnnGEN()");

		// move to onCreate()
		/*
		mSelf = this;

		// used by OpenWnn
		mComposingText = new ComposingText();
		mCandidatesViewManager = new TextCandidatesViewManager(-1);
		mInputViewManager = new DefaultSoftKeyboardEN(this);
		mConverterEN = new NicoWnnGEngineEN(writableENDic);
		mConverter = mConverterEN;
		mSymbolList = null;

		// etc
		mDisplayText = new SpannableStringBuilder();
		mAutoHideMode = false;
		mSymbolMode = false;
		mOptPrediction = true;
		mOptSpellCorrection = true;
		mOptLearning = true;
		*/
	}

	/**
	 * Constructor
	 *
	 * @param context       The context
	 */
	public NicoWnnGEN(final Context context) {
		super(context);
		// Log.w("NicoWnnG", "NicoWnnGEN(final Context context)");
		myConstructor2(context);
	}
	
	public void myConstructor2(Context context) {
		mSelf = this;
		// Log.w("NicoWnnG", "NicoWnnGEN#myConstructor2(Context context)");
		// move from constructor
		// used by OpenWnn
		mComposingText = new ComposingText();
		mCandidatesViewManager = new TextCandidatesViewManager(-1, this);
		mInputViewManager = new DefaultSoftKeyboardEN(this);
		if (context != null) {
			super.setContext(context);
			initConverter();
		}
		mSymbolList = null;

		// etc
		mDisplayText = new SpannableStringBuilder();
		mAutoHideMode = false;
		mSymbolMode = false;
		mOptPrediction = true;
		mOptSpellCorrection = true;
		mOptLearning = true;
	}

	private void initConverter() {
		mConverterEN = new NicoWnnGEngineEN(writableDicENFileName);
		mConverter = mConverterEN;
		mWordSeparators = getResources().getString(R.string.en_word_separators);

		if (mSymbolList == null) {
			mSymbolList = new SymbolList(this, SymbolList.LANG_EN);
		}
		if (mSymbolList != null) {
			if (!mSymbolList.getUserSymbolChecked()) {
				mSymbolList.loadUserSymbolList();
			}
		}
	}
	/**
	 * Get the instance of this service.
	 * <br>
	 * Before using this method, the constructor of this service must be invoked.
	 *
	 * @return      The instance of this object
	 */
	public static NicoWnnGEN getInstance() {
		return mSelf;
	}

	/**
	 * Insert a character into the composing text.
	 *
	 * @param chars     A array of character
	 */
	private void insertCharToComposingText(final char[] chars) {
		final StrSegment seg = new StrSegment(chars);

		if ((chars[0] == SPACE[0]) || (chars[0] == '\u0009')) {
			/* if the character is a space, commit the composing text */
			commitText(1);
			commitText(seg.string);
			mComposingText.clear();
		} else if (mWordSeparators.contains(seg.string)) {
			/* if the character is a separator, remove an auto-inserted space and commit the composing text. */
			if (mPreviousEventCode == NicoWnnGEvent.SELECT_CANDIDATE) {
				mInputConnection.deleteSurroundingText(1, 0);
			}
			commitText(1);
			commitText(seg.string);
			mComposingText.clear();
		} else {
			mComposingText.insertStrSegment(0, 1, seg);
			updateComposingText(1);
		}
	}

	/**
	 * Insert a character into the composing text.
	 *
	 * @param charCode      A character code
	 * @return              {@code true} if success; {@code false} if an error occurs.
	 */
	private boolean insertCharToComposingText(final int charCode) {
		if (charCode == 0) {
			return false;
		}
		insertCharToComposingText(Character.toChars(charCode));
		return true;
	}

	/**
	 * Get the shift key state from the editor.
	 *
	 * @param editor    Editor
	 *
	 * @return          State ID of the shift key (0:off, 1:on)
	 */
	private int getShiftKeyState(final EditorInfo editor) {
		return (getCurrentInputConnection().getCursorCapsMode(editor.inputType) == 0) ? 0 : 1;
	}

	/**
	 * Set the mode of the symbol list.
	 *
	 * @param mode      {@code SymbolList.SYMBOL_ENGLISH} or {@code null}.
	 */
	private void setSymbolMode(final String mode) {
		if (mode != null) {
			mHandler.removeMessages(MSG_PREDICTION);
			mSymbolMode = true;
			mSymbolList.setSymbolDictionary(mode);
			mConverter = mSymbolList;
		} else {
			if (!mSymbolMode) {
				return;
			}
			mHandler.removeMessages(MSG_PREDICTION);
			mSymbolMode = false;
			mConverter = mConverterEN;
		}
	}

	/***********************************************************************
	 * InputMethodServer
	 ***********************************************************************/
	/** @see net.gorry.android.input.nicownng.NicoWnnG#onCreate */
	@Override public void onCreate() {
		// Log.w("NicoWnnG", "NicoWnnGEN#onCreate()");
		super.onCreate();
		
		if (mConverterEN == null) {
			super.setContext(this);
			initConverter();
		}
	}

	/** @see net.gorry.android.input.nicownng.NicoWnnG#onCreateInputView */
	@Override public View onCreateInputView() {
		final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		loadOption(pref);
		final int hiddenState = getResources().getConfiguration().hardKeyboardHidden;
		final boolean hidden = (hiddenState == Configuration.HARDKEYBOARDHIDDEN_YES);
		((DefaultSoftKeyboardEN) mInputViewManager).setHardKeyboardHidden(hidden, false);
		mEnableTutorial = hidden;

		return super.onCreateInputView();
	}

	/** @see net.gorry.android.input.nicownng.NicoWnnG#onStartInputView */
	@Override public void onStartInputView(final EditorInfo attribute, final boolean restarting) {
		super.onStartInputView(attribute, restarting);

		/* initialize views */
		mCandidatesViewManager.clearCandidates();
		mCandidatesViewManager.setViewType(CandidatesViewManager.VIEW_TYPE_CLOSE);

		mHardShift = 0;
		mHardAlt   = 0;
		updateMetaKeyStateDisplay();

		/* load preferences */
		final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);

		/* auto caps mode */
		mAutoCaps = getOrientPrefBoolean(pref, "auto_caps", false);

		/* set TextCandidatesViewManager's option */
		((TextCandidatesViewManager)mCandidatesViewManager).setAutoHide(true);

		/* display status icon */
		showStatusIcon(R.drawable.immodeic_half_alphabet);

		if (mComposingText != null) {
			mComposingText.clear();
		}
		/* initialize the engine's state */
		fitInputType(pref, attribute);

		((DefaultSoftKeyboard) mInputViewManager).resetCurrentKeyboard();
	}

	/** @see net.gorry.android.input.nicownng.NicoWnnG#hideWindow */
	@Override public void hideWindow() {
		mComposingText.clear();
		mInputViewManager.onUpdateState(this);
		mHandler.removeMessages(MSG_START_TUTORIAL);
		mInputViewManager.closing();
		if (mTutorial != null) {
			mTutorial.close();
			mTutorial = null;
		}

		super.hideWindow();
	}

	/** @see net.gorry.android.input.nicownng.NicoWnnG#onUpdateSelection */
	@Override public void onUpdateSelection(final int oldSelStart, final int oldSelEnd,
			final int newSelStart, final int newSelEnd, final int candidatesStart,
			final int candidatesEnd) {

		final boolean isNotComposing = ((candidatesStart < 0) && (candidatesEnd < 0));
		if (isNotComposing) {
			mComposingText.clear();
			updateComposingText(1);
		} else {
			if (mComposingText.size(1) != 0) {
				updateComposingText(1);
			}
		}
	}

	/** @see net.gorry.android.input.nicownng.NicoWnnG#onConfigurationChanged */
	@Override public void onConfigurationChanged(final Configuration newConfig) {
		try {
			super.onConfigurationChanged(newConfig);
			if (mInputConnection != null) {
				updateComposingText(1);
			}
			/* Hardware keyboard */
			final int hiddenState = newConfig.hardKeyboardHidden;
			final boolean hidden = (hiddenState == Configuration.HARDKEYBOARDHIDDEN_YES);
			mEnableTutorial = hidden;
		} catch (final Exception ex) {
		}
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
		super.onEvaluateInputViewShown();

		return true;
	}

	/***********************************************************************
	 * OpenWnn
	 ***********************************************************************/
	/** @see net.gorry.android.input.nicownng.NicoWnnG#onEvent */
	@Override synchronized public boolean onEvent(final NicoWnnGEvent ev) {
		/* handling events which are valid when InputConnection is not active. */
		switch (ev.code) {

			case NicoWnnGEvent.KEYUP:
				onKeyUpEvent(ev.keyEvent);
				return true;

			case NicoWnnGEvent.INITIALIZE_LEARNING_DICTIONARY:
				return mConverterEN.initializeDictionary( WnnEngine.DICTIONARY_TYPE_LEARN );

			case NicoWnnGEvent.INITIALIZE_USER_DICTIONARY:
				return mConverterEN.initializeDictionary( WnnEngine.DICTIONARY_TYPE_USER );

			case NicoWnnGEvent.LIST_WORDS_IN_USER_DICTIONARY:
				mUserDictionaryWords = mConverterEN.getUserDictionaryWords( );
				return true;

			case NicoWnnGEvent.GET_WORD:
				if( mUserDictionaryWords != null ) {
					ev.word = mUserDictionaryWords[ 0 ];
					for( int i = 0 ; i < mUserDictionaryWords.length-1 ; i++ ) {
						mUserDictionaryWords[ i ] = mUserDictionaryWords[ i + 1 ];
					}
					mUserDictionaryWords[ mUserDictionaryWords.length-1 ] = null;
					if( mUserDictionaryWords[ 0 ] == null ) {
						mUserDictionaryWords = null;
					}
					return true;
				}
				break;

			case NicoWnnGEvent.ADD_WORD:
				mConverterEN.addWord(ev.word);
				return true;

			case NicoWnnGEvent.ADD_WORDS:
				mConverterEN.addWords(ev.words, ev.progress);
				return true;

			case NicoWnnGEvent.DELETE_WORD:
				mConverterEN.deleteWord(ev.word);
				return true;

			case NicoWnnGEvent.CHANGE_MODE:
				return false;

			case NicoWnnGEvent.UPDATE_CANDIDATE:
				updateComposingText(ComposingText.LAYER1);
				return true;

			case NicoWnnGEvent.CHANGE_INPUT_VIEW:
				setInputView(onCreateInputView());
				return true;

			case NicoWnnGEvent.CANDIDATE_VIEW_TOUCH:
				boolean ret;
				ret = ((TextCandidatesViewManager)mCandidatesViewManager).onTouchSync();
				return ret;

			default:
				break;
		}

		dismissPopupKeyboard();
		final KeyEvent keyEvent = ev.keyEvent;
		int keyCode = 0;
		if (keyEvent != null) {
			keyCode = keyEvent.getKeyCode();
		}
		if (mDirectInputMode) {
			if ((ev.code == NicoWnnGEvent.INPUT_SOFT_KEY) && (mInputConnection != null)) {
				mInputConnection.sendKeyEvent(keyEvent);
				mInputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP,
						keyEvent.getKeyCode()));
			}
			return false;
		}

		if (ev.code == NicoWnnGEvent.LIST_CANDIDATES_FULL) {
			mCandidatesViewManager.setViewType(CandidatesViewManager.VIEW_TYPE_FULL);
			return true;
		} else if (ev.code == NicoWnnGEvent.LIST_CANDIDATES_NORMAL) {
			mCandidatesViewManager.setViewType(CandidatesViewManager.VIEW_TYPE_NORMAL);
			return true;
		}

		boolean ret = false;
		switch (ev.code) {
			case NicoWnnGEvent.INPUT_CHAR:
				((TextCandidatesViewManager)mCandidatesViewManager).setAutoHide(false);
				final EditorInfo edit = getCurrentInputEditorInfo();
				if( edit.inputType == InputType.TYPE_CLASS_PHONE){
					commitText(new String(ev.chars));
				}else{
					setSymbolMode(null);
					insertCharToComposingText(ev.chars);
					ret = true;
					mPreviousEventCode = ev.code;
				}
				break;

			case NicoWnnGEvent.INPUT_KEY:
				keyCode = ev.keyEvent.getKeyCode();
				/* update shift/alt state */
				switch (keyCode) {
					case KeyEvent.KEYCODE_ALT_LEFT:
					case KeyEvent.KEYCODE_ALT_RIGHT:
						if (ev.keyEvent.getRepeatCount() == 0) {
							if (++mHardAlt > 2) { mHardAlt = 0; }
						}
						mAltPressing   = true;
						updateMetaKeyStateDisplay();
						return true;

					case KeyEvent.KEYCODE_SHIFT_LEFT:
					case KeyEvent.KEYCODE_SHIFT_RIGHT:
						if (ev.keyEvent.getRepeatCount() == 0) {
							if (++mHardShift > 2) { mHardShift = 0; }
						}
						mShiftPressing = true;
						updateMetaKeyStateDisplay();
						return true;
				}
				setSymbolMode(null);
				updateComposingText(1);
				/* handle other key event */
				ret = processKeyEvent(ev.keyEvent);
				mPreviousEventCode = ev.code;
				break;

			case NicoWnnGEvent.INPUT_SOFT_KEY:
				setSymbolMode(null);
				updateComposingText(1);
				ret = processKeyEvent(ev.keyEvent);
				if (!ret) {
					mInputConnection.sendKeyEvent(ev.keyEvent);
					mInputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, ev.keyEvent.getKeyCode()));
					ret = true;
				}
				mPreviousEventCode = ev.code;
				break;

			case NicoWnnGEvent.SELECT_CANDIDATE:
				if (mSymbolMode) {
					commitText(ev.word, false);
				} else {
					if (mWordSeparators.contains(ev.word.candidate) &&
							(mPreviousEventCode == NicoWnnGEvent.SELECT_CANDIDATE)) {
						mInputConnection.deleteSurroundingText(1, 0);
					}
					commitText(ev.word, true);
				}
				mComposingText.clear();
				mPreviousEventCode = ev.code;
				updateComposingText(1);
				break;

			case NicoWnnGEvent.LIST_SYMBOLS:
				commitText(1);
				mComposingText.clear();
				setSymbolMode(SymbolList.SYMBOL_ENGLISH);
				updateComposingText(1);
				break;

			default:
				break;
		}

		if (mCandidatesViewManager.getViewType() == CandidatesViewManager.VIEW_TYPE_FULL) {
			mCandidatesViewManager.setViewType(CandidatesViewManager.VIEW_TYPE_NORMAL);
		}

		return ret;
	}

	/***********************************************************************
	 * OpenWnnEN
	 ***********************************************************************/
	/**
	 * Handling KeyEvent
	 * <br>
	 * This method is called from {@link #onEvent()}.
	 *
	 * @param ev   A key event
	 * @return      {@code true} if the event is processed in this method; {@code false} if the event is not processed in this method
	 */
	private boolean processKeyEvent(final KeyEvent ev) {

		final int key = ev.getKeyCode();
		final EditorInfo edit = getCurrentInputEditorInfo();
		/* keys which produce a glyph */
		if (ev.isPrintingKey()) {
			/* do nothing if the character is not able to display or the character is dead key */
			if (((mHardShift > 0) && (mHardAlt > 0)) || (ev.isAltPressed() && ev.isShiftPressed())) {
				final int charCode = ev.getUnicodeChar(MetaKeyKeyListener.META_SHIFT_ON | MetaKeyKeyListener.META_ALT_ON);
				if ((charCode == 0) || ((charCode & KeyCharacterMap.COMBINING_ACCENT) != 0) || (charCode == PRIVATE_AREA_CODE)) {
					if(mHardShift > 0){
						mShiftPressing = false;
					}
					if(mHardAlt > 0){
						mAltPressing   = false;
					}
					if(!ev.isAltPressed()){
						if (mHardAlt > 0) {
							mHardAlt = 0;
						}
					}
					if(!ev.isShiftPressed()){
						if (mHardShift > 0) {
							mHardShift = 0;
						}
					}
					if(!ev.isShiftPressed() && !ev.isAltPressed()){
						updateMetaKeyStateDisplay();
					}
					return true;
				}
			}

			((TextCandidatesViewManager)mCandidatesViewManager).setAutoHide(false);

			/* get the key character */
			if ((mHardShift== 0)  && (mHardAlt == 0)) {
				/* no meta key is locked */
				final int shift = (mAutoCaps) ? getShiftKeyState(edit) : 0;
				if ((shift != mHardShift) && ((key >= KeyEvent.KEYCODE_A) && (key <= KeyEvent.KEYCODE_Z))) {
					/* handling auto caps for a alphabet character */
					insertCharToComposingText(ev.getUnicodeChar(MetaKeyKeyListener.META_SHIFT_ON));
				} else {
					insertCharToComposingText(ev.getUnicodeChar());
				}
			} else {
				insertCharToComposingText(ev.getUnicodeChar(mShiftKeyToggle[mHardShift]
				                                                            | mAltKeyToggle[mHardAlt]));
				if(mHardShift == 1){
					mShiftPressing = false;
				}
				if(mHardAlt == 1){
					mAltPressing   = false;
				}
				/* back to 0 (off) if 1 (on/not locked) */
				if(!ev.isAltPressed()){
					if (mHardAlt == 1) {
						mHardAlt = 0;
					}
				}
				if(!ev.isShiftPressed()){
					if (mHardShift == 1) {
						mHardShift = 0;
					}
				}
				if(!ev.isShiftPressed() && !ev.isAltPressed()){
					updateMetaKeyStateDisplay();
				}
			}

			if (edit.inputType == InputType.TYPE_CLASS_PHONE) {
				commitText(1);
				mComposingText.clear();
				return true;
			}
			return true;

		} else if (key == KeyEvent.KEYCODE_SPACE) {
			if (ev.isAltPressed()) {
				/* display the symbol list (G1 specific. same as KEYCODE_SYM) */
				commitText(1);
				mComposingText.clear();
				setSymbolMode(SymbolList.SYMBOL_ENGLISH);
				updateComposingText(1);
				mHardAlt = 0;
				updateMetaKeyStateDisplay();
			} else {
				insertCharToComposingText(SPACE);
			}
			return true;
		} else if (key == KeyEvent.KEYCODE_SYM) {
			/* display the symbol list */
			commitText(1);
			mComposingText.clear();
			setSymbolMode(SymbolList.SYMBOL_ENGLISH);
			updateComposingText(1);
			mHardAlt = 0;
			updateMetaKeyStateDisplay();
		}


		/* Functional key */
		if (mComposingText.size(1) > 0) {
			switch (key) {
				case KeyEvent.KEYCODE_DEL:
					mComposingText.delete(1, false);
					updateComposingText(1);
					return true;

				case KeyEvent.KEYCODE_BACK:
					if (mCandidatesViewManager.getViewType() == CandidatesViewManager.VIEW_TYPE_FULL) {
						mCandidatesViewManager.setViewType(CandidatesViewManager.VIEW_TYPE_NORMAL);
					} else {
						mComposingText.clear();
						updateComposingText(1);
					}
					return true;

				case KeyEvent.KEYCODE_DPAD_LEFT:
					mComposingText.moveCursor(1, -1);
					updateComposingText(1);
					return true;

				case KeyEvent.KEYCODE_DPAD_RIGHT:
					mComposingText.moveCursor(1, 1);
					updateComposingText(1);
					return true;

				case KeyEvent.KEYCODE_ENTER:
				case KeyEvent.KEYCODE_DPAD_CENTER:
					commitText(1);
					mComposingText.clear();
					if (mEnableAutoHideKeyboard) {
						mInputViewManager.closing();
						requestHideSelf(0);
					}
					return true;

				default:
					break;
			}
		} else {
			/* if there is no composing string. */
			if (mCandidatesViewManager.getCurrentView().isShown()) {
				if (key == KeyEvent.KEYCODE_BACK) {
					if (mCandidatesViewManager.getViewType() == CandidatesViewManager.VIEW_TYPE_FULL) {
						mCandidatesViewManager.setViewType(CandidatesViewManager.VIEW_TYPE_NORMAL);
					} else {
						mCandidatesViewManager.setViewType(CandidatesViewManager.VIEW_TYPE_CLOSE);
					}
					return true;
				}
			} else {
				switch (key) {
					case KeyEvent.KEYCODE_DPAD_CENTER:
					case KeyEvent.KEYCODE_ENTER:
						if (mEnableAutoHideKeyboard) {
							mInputViewManager.closing();
							requestHideSelf(0);
							return true;
						}
						break;
					case KeyEvent.KEYCODE_BACK:
						/*
						 * If 'BACK' key is pressed when the SW-keyboard is shown
						 * and the candidates view is not shown, dismiss the SW-keyboard.
						 */
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

	/**
	 * Thread for updating the candidates view
	 */
	private void updatePrediction() {
		int candidates = 0;
		if (mConverter != null) {
			/* normal prediction */
			candidates = mConverter.predict(mComposingText, 0, -1);
		}
		/* update the candidates view */
		if (candidates > 0) {
			mCandidatesViewManager.displayCandidates(mConverter);
		} else {
			mCandidatesViewManager.clearCandidates();
		}
	}

	/**
	 * Update the composing text.
	 *
	 * @param layer  {@link mComposingText}'s layer to display
	 */
	private void updateComposingText(final int layer) {
		/* update the candidates view */
		if (!mOptPrediction) {
			commitText(1);
			mComposingText.clear();
			if (mSymbolMode) {
				mHandler.removeMessages(MSG_PREDICTION);
				mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_PREDICTION), 0);
			}
		} else {
			if (mComposingText.size(1) != 0) {
				mHandler.removeMessages(MSG_PREDICTION);
				if (mCandidatesViewManager.getCurrentView().isShown()) {
					mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_PREDICTION),
							PREDICTION_DELAY_MS_SHOWING_CANDIDATE);
				} else {
					mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_PREDICTION),
							PREDICTION_DELAY_MS_1ST);
				}
			} else {
				mHandler.removeMessages(MSG_PREDICTION);
				mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_PREDICTION), 0);
			}

			/* notice to the input view */
			mInputViewManager.onUpdateState(this);

			/* set the text for displaying as the composing text */
			final SpannableStringBuilder disp = mDisplayText;
			disp.clear();
			disp.insert(0, mComposingText.toString(layer));

			/* add decoration to the text */
			final int cursor = mComposingText.getCursor(layer);
			if (disp.length() != 0) {
				if ((cursor > 0) && (cursor < disp.length())) {
					disp.setSpan(SPAN_EXACT_BGCOLOR_HL, 0, cursor,
							Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				}
				if (cursor < disp.length()) {
					mDisplayText.setSpan(SPAN_REMAIN_BGCOLOR_HL, cursor, disp.length(),
							Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
					mDisplayText.setSpan(SPAN_TEXTCOLOR, 0, disp.length(),
							Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				}

				disp.setSpan(SPAN_UNDERLINE, 0, disp.length(),
						Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			}

			int displayCursor = cursor;
			if (FIX_CURSOR_TEXT_END) {
				displayCursor = (cursor == 0) ?  0 : 1;
			}
			/* update the composing text on the EditView */
			mInputConnection.setComposingText(disp, displayCursor);
		}
	}

	/**
	 * Commit the composing text.
	 *
	 * @param layer  {@link mComposingText}'s layer to commit.
	 */
	private void commitText(final int layer) {
		final String tmp = mComposingText.toString(layer);

		if (mOptLearning && (mConverter != null) && (tmp.length() > 0)) {
			final WnnWord word = new WnnWord(tmp, tmp);
			mConverter.learn(word);
		}

		mInputConnection.commitText(tmp, (FIX_CURSOR_TEXT_END ? 1 : tmp.length()));
		mCandidatesViewManager.clearCandidates();
	}

	/**
	 * Commit a word
	 *
	 * @param word          A word to commit
	 * @param withSpace     Append a space after the word if {@code true}.
	 */
	private void commitText(final WnnWord word, final boolean withSpace) {

		if (mOptLearning && (mConverter != null)) {
			mConverter.learn(word);
		}

		mInputConnection.commitText(word.candidate, (FIX_CURSOR_TEXT_END ? 1 : word.candidate.length()));

		if (withSpace) {
			commitText(" ");
		}
	}

	/**
	 * Commit a string
	 * <br>
	 * The string is not registered into the learning dictionary.
	 *
	 * @param str  A string to commit
	 */
	private void commitText(final String str) {
		mInputConnection.commitText(str, (FIX_CURSOR_TEXT_END ? 1 : str.length()));
		mCandidatesViewManager.clearCandidates();
	}

	/**
	 * Dismiss the pop-up keyboard
	 */
	protected void dismissPopupKeyboard() {
		final DefaultSoftKeyboardEN kbd = (DefaultSoftKeyboardEN)mInputViewManager;
		if (kbd != null) {
			kbd.dismissPopupKeyboard();
		}
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
	 * Handling KeyEvent(KEYUP)
	 * <br>
	 * This method is called from {@link #onEvent()}.
	 *
	 * @param ev   An up key event
	 */
	private void onKeyUpEvent(final KeyEvent ev) {
		final int key = ev.getKeyCode();
		if(!mShiftPressing){
			if((key == KeyEvent.KEYCODE_SHIFT_LEFT) || (key == KeyEvent.KEYCODE_SHIFT_RIGHT)){
				mHardShift = 0;
				mShiftPressing = true;
				updateMetaKeyStateDisplay();
			}
		}
		if(!mAltPressing ){
			if((key == KeyEvent.KEYCODE_ALT_LEFT) || (key == KeyEvent.KEYCODE_ALT_RIGHT)){
				mHardAlt = 0;
				mAltPressing   = true;
				updateMetaKeyStateDisplay();
			}
		}
	}
	/**
	 * Fits an editor info.
	 *
	 * @param preferences  The preference data.
	 * @param info          The editor info.
	 */
	private void fitInputType(final SharedPreferences pref, final EditorInfo info) {
		if (info.inputType == InputType.TYPE_NULL) {
			mDirectInputMode = true;
			return;
		}

		mEnableAutoHideKeyboard = false;

		/* set prediction & spell correction mode */
		mOptPrediction      = pref.getBoolean("opt_en_prediction", true);
		mOptSpellCorrection = pref.getBoolean("opt_en_spell_correction", true);
		mOptLearning        = pref.getBoolean("opt_en_enable_learning", true);

		loadOption(pref);

		/* prediction on/off */
		switch (info.inputType & InputType.TYPE_MASK_CLASS) {
			case InputType.TYPE_CLASS_NUMBER:
			case InputType.TYPE_CLASS_DATETIME:
			case InputType.TYPE_CLASS_PHONE:
				mOptPrediction = false;
				mOptLearning = false;
				break;

			case InputType.TYPE_CLASS_TEXT:
				switch (info.inputType & InputType.TYPE_MASK_VARIATION) {
					case InputType.TYPE_TEXT_VARIATION_PASSWORD:
						mEnableAutoHideKeyboard = true;
						mOptLearning = false;
						mOptPrediction = false;
						break;

					case InputType.TYPE_TEXT_VARIATION_PHONETIC:
						mOptLearning = false;
						mOptPrediction = false;
						break;
					default:
						break;
				}
		}

		/* doesn't learn any word if it is not prediction mode */
		if (!mOptPrediction) {
			mOptLearning = false;
		}

		/* set engine's mode */
		if (mOptSpellCorrection) {
			mConverterEN.setDictionary(NicoWnnGEngineEN.DICT_FOR_CORRECT_MISTYPE);
		} else {
			mConverterEN.setDictionary(NicoWnnGEngineEN.DICT_DEFAULT);
		}
		checkTutorial(info.privateImeOptions);
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
		final DefaultSoftKeyboardEN inputManager = ((DefaultSoftKeyboardEN) mInputViewManager);
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
}
