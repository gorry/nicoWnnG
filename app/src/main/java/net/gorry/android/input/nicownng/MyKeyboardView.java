/**
 * 
 */
package net.gorry.android.input.nicownng;

import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.Keyboard.Key;
import android.inputmethodservice.KeyboardView;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.EditorInfo;
import android.widget.PopupWindow;
import android.widget.TextView;

/**
 * 
 * キーボードビュー
 * 
 * @author GORRY
 *
 */
public class MyKeyboardView extends KeyboardView {
	private static final String TAG = "MyKeyboardView";
	private static final boolean V = false;

	private DefaultSoftKeyboard mDefaultSoftKeyboard;
	private int mVerticalCorrection;

	/**
	 * @param context context
	 * @param attrs attrs
	 */
	public MyKeyboardView(Context context, AttributeSet attrs) {
		super(context, attrs);
		getAttrs(context, attrs, 0/*com.android.internal.R.attr.keyboardViewStyle*/);
	}

	/**
	 * @param context context 
	 * @param attrs attrs
	 * @param defStyle defStyle
	 */
	public MyKeyboardView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		getAttrs(context, attrs, defStyle);
	}

	private void getAttrs(Context context, AttributeSet attrs, int defStyle) {
		Resources res = getResources();
		mVerticalCorrection = res.getDimensionPixelSize(R.dimen.vertical_correction);

		/*
		TypedArray a =
            context.obtainStyledAttributes(
                attrs, android.R.styleable.KeyboardView, defStyle, 0);

        int n = a.getIndexCount();
        
        for (int i = 0; i < n; i++) {
            int attr = a.getIndex(i);

            switch (attr) {
            case com.android.internal.R.styleable.KeyboardView_verticalCorrection:
                mVerticalCorrection = a.getDimensionPixelOffset(attr, 0);
                break;
        }
        */
	}

	@Override
	protected boolean onLongPress(Key popupKey) {
		int popupKeyboardId = popupKey.popupResId;
		if (popupKeyboardId != 0) {
			fadePreview();
		}
		return super.onLongPress(popupKey);
	}

	@Override
	public void closing() {
		super.closing();
	}

	
	
	
	//

	public static final int NOT_A_KEY = -1;

	private TextView mPreviewText;
	private PopupWindow mPreviewPopup;
	private int mPreviewTextSizeLarge;
	private int mPreviewOffset;
	private int mPreviewHeight;
	private int mCurrentKeyIndex = NOT_A_KEY;
	private Key[] mKeys;
	private boolean mShowPreview = true;
	private boolean mPreviewCentered = false;
	private int mKeyTextSize;
	private int mPopupPreviewX;
	private int mPopupPreviewY;
	private int mMiniKeyboardOffsetX;
	private int mMiniKeyboardOffsetY;
	private int mWindowY;
	private int mProximityThreshold;
	private int mLastFlingDir;
	private Key mFirstKey;

	private static final int[] mPreviewBackground = {
		R.drawable.keyboard_key_feedback_background,
		R.drawable.keyboard_key_feedback_d_background,
		R.drawable.keyboard_key_feedback_l_background,
		R.drawable.keyboard_key_feedback_u_background,
		R.drawable.keyboard_key_feedback_r_background,
	};
	private static final Drawable[] mCursorIcon = new Drawable[5];

	private static final int[] mCursorIconResId = {
		0,
		R.drawable.key_qwerty_down_b,
		R.drawable.key_qwerty_left_b,
		R.drawable.key_qwerty_up_b,
		R.drawable.key_qwerty_right_b,
	};

	private int[] mOffsetInWindow = new int[2];
	private int[] mWindowLocation = new int[2];

	private static int MAX_NEARBY_KEYS = 12;
	private int[] mDistances = new int[MAX_NEARBY_KEYS];

	private static final int MSG_SHOW_PREVIEW = 1;
	private static final int MSG_REMOVE_PREVIEW = 2;

	private static final int DELAY_BEFORE_PREVIEW = 0;
	private static final int DELAY_AFTER_PREVIEW = 200;
	private static final int DELAY_AFTER_PREVIEW_FLICK = 250;
	private static final int DEBOUNCE_TIME = 70;

	Handler mShowPreviewHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case MSG_SHOW_PREVIEW:
					showKey(msg.arg1, msg.arg2);
					break;
				case MSG_REMOVE_PREVIEW:
					mPreviewText.setVisibility(View.INVISIBLE);
					break;
			}
		}
	};

	private CharSequence adjustCase(CharSequence label) {
		if (getKeyboard().isShifted() && (label != null) && (label.length() < 3)
				&& Character.isLowerCase(label.charAt(0))) {
			label = label.toString().toUpperCase();
		}
		return label;
	}

	private CharSequence getPreviewText(Key key) {
		return adjustCase(key.label);
	}

	private void computeProximityThreshold(Keyboard keyboard) {
		if (keyboard == null) return;
		final Key[] keys = mKeys;
		if (keys == null) return;
		int length = keys.length;
		int dimensionSum = 0;
		for (int i = 0; i < length; i++) {
			Key key = keys[i];
			dimensionSum += Math.min(key.width, key.height) + key.gap;
		}
		if (dimensionSum < 0 || length == 0) return;
		mProximityThreshold = (int) (dimensionSum * 1.4f / length);
		mProximityThreshold *= mProximityThreshold; // Square it
	}

	/**
	 * init show preview
	 */
	public void initShowPreview() {
		Context context = getContext();
		Keyboard keyboard = getKeyboard();
		List<Key> keys = keyboard.getKeys();
		mKeys = keys.toArray(new Key[keys.size()]);

		computeProximityThreshold(keyboard);
		
		if (mPreviewPopup == null) {
			mPreviewPopup = new PopupWindow(context);
			LayoutInflater inflate = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			mPreviewText = (TextView) inflate.inflate(R.layout.keyboard_key_preview, null);
		}
		mPreviewTextSizeLarge = (int)mPreviewText.getTextSize();
		mPreviewPopup.setContentView(mPreviewText);
		mPreviewPopup.setBackgroundDrawable(null);

		Resources res = getResources();
		mKeyTextSize = res.getDimensionPixelSize(R.dimen.key_text_size);
		mPreviewHeight = res.getDimensionPixelSize(R.dimen.key_preview_height);

		mPreviewPopup.setTouchable(false);

		for (int i=0; i<mCursorIcon.length; i++) {
			int id = mCursorIconResId[i];
			if (id > 0) {
				Drawable icon = res.getDrawable(id);
				icon.setBounds(0, 0, icon.getIntrinsicWidth(), icon.getIntrinsicHeight());
				mCursorIcon[i] = icon;
			}
		}

		mLastFlingDir = -1;
	}

	/**
	 * fade preview
	 */
	public void fadePreview() {
		showPreview(NOT_A_KEY, -1);
	}

	/**
	 * show preview
	 * @param keyIndex index
	 * @param flingDir dir
	 */
	public void showPreview(int keyIndex, int flingDir) {
		int oldFlingDir = mLastFlingDir;
		int oldKeyIndex = mCurrentKeyIndex;
		final PopupWindow previewPopup = mPreviewPopup;
		
		mLastFlingDir = flingDir;
		mCurrentKeyIndex = keyIndex;

		// Release the old key and press the new key
		/*
		final Key[] keys = mKeys;
		if (oldKeyIndex != mCurrentKeyIndex) {
			if ((oldKeyIndex != NOT_A_KEY) && (keys.length > oldKeyIndex)) {
				keys[oldKeyIndex].onReleased((mCurrentKeyIndex == NOT_A_KEY));
				// invalidateKey(oldKeyIndex);
			}
			if ((mCurrentKeyIndex != NOT_A_KEY) && (keys.length > mCurrentKeyIndex)) {
				keys[mCurrentKeyIndex].onPressed();
				// invalidateKey(mCurrentKeyIndex);
			}
		}
		*/
		// If key changed and preview is on ...
		if (((oldKeyIndex != mCurrentKeyIndex) || (oldFlingDir != mLastFlingDir)) && (null != previewPopup) && mShowPreview) {
			mShowPreviewHandler.removeMessages(MSG_SHOW_PREVIEW);
			if (previewPopup.isShowing()) {
				if (keyIndex == NOT_A_KEY) {
					mShowPreviewHandler.sendMessageDelayed(
							mShowPreviewHandler.obtainMessage(MSG_REMOVE_PREVIEW), 
							((oldFlingDir>0) ? DELAY_AFTER_PREVIEW_FLICK : DELAY_AFTER_PREVIEW)
					);
				}
			}
			if (keyIndex != NOT_A_KEY) {
				if (previewPopup.isShowing() && mPreviewText.getVisibility() == View.VISIBLE) {
					// Show right away, if it's already visible and finger is moving around
					showKey(keyIndex, flingDir);
				} else {
					mShowPreviewHandler.sendMessageDelayed(
							mShowPreviewHandler.obtainMessage(MSG_SHOW_PREVIEW, keyIndex, flingDir), 
							DELAY_BEFORE_PREVIEW
					);
				}
			}
		}
	}
	
	private static final String[] mCursorString = {"CURSOR"};
	private void showKey(final int keyIndex, int flingDir) {
		final PopupWindow previewPopup = mPreviewPopup;
		final Key[] keys = mKeys;
		if (keyIndex < 0 || keyIndex >= mKeys.length) return;
		Key key = keys[keyIndex];
		if (mFirstKey == null) {
			mFirstKey = key;
			// Log.d("NicoWnnG", "mFirstKey="+mFirstKey.codes[0]);
		}
		String[] keyString = null;
		if (flingDir >= 0) {
			// Log.d("NicoWnnG", "flingDir="+flingDir);
			int dir2 = -1;
			switch (mDefaultSoftKeyboard.getPrevInputKeyCode()) {
			case DefaultSoftKeyboard.KEYCODE_JP12_RIGHT:
			case DefaultSoftKeyboard.KEYCODE_JP12_LEFT:
			case DefaultSoftKeyboard.KEYCODE_JP12_UP:
			case DefaultSoftKeyboard.KEYCODE_JP12_DOWN:
			case DefaultSoftKeyboard.KEYCODE_ARROW_STOP:
				if (flingDir > 0) {
					keyString = mCursorString;
				}
				break;
			case DefaultSoftKeyboard.KEYCODE_JP12_TOGGLE_MODE:
			case DefaultSoftKeyboard.KEYCODE_JP12_TOGGLE_MODE2:
			case DefaultSoftKeyboard.KEYCODE_QWERTY_TOGGLE_MODE:
			case DefaultSoftKeyboard.KEYCODE_QWERTY_TOGGLE_MODE2:
			case DefaultSoftKeyboard.KEYCODE_JP12_TOGGLE_MODE_TOP:
			case DefaultSoftKeyboard.KEYCODE_QWERTY_TOGGLE_MODE_TOP:
			case DefaultSoftKeyboard.KEYCODE_JP12_TOGGLE_MODE_BACK:
			case DefaultSoftKeyboard.KEYCODE_QWERTY_TOGGLE_MODE_BACK:
				break;
			default:
				dir2 = mDefaultSoftKeyboard.convertModeFlick(mDefaultSoftKeyboard.getTableIndex(mDefaultSoftKeyboard.getPrevInputKeyCode()), flingDir);
				if (dir2 >= 0) {
					keyString = mDefaultSoftKeyboard.convertFlickToKeyString(dir2);
				} else {
					flingDir = -1;
				}
				break;
			}
			if (flingDir >= 0) {
				mPreviewText.setBackgroundResource(mPreviewBackground[flingDir]);
			} else {
				mPreviewText.setBackgroundResource(R.drawable.keyboard_key_feedback_background);
			}
		} else {
			mPreviewText.setBackgroundResource(R.drawable.keyboard_key_feedback_background);
		}
		if ((null == keyString) && (key.icon != null)) {
			mPreviewText.setCompoundDrawables(
			  null, null, null, 
			  ((key.iconPreview) != null ? key.iconPreview : key.icon)
			);
			mPreviewText.setText(null);
		} else {
			if (keyString == mCursorString) {
				mPreviewText.setCompoundDrawables(
				  null, null, null, 
				  mCursorIcon[flingDir]
				);
				mPreviewText.setText(null);
			} else {
				mPreviewText.setCompoundDrawables(null, null, null, null);
				if (null != keyString) {
					mPreviewText.setText(keyString[0]);
					mPreviewText.setTextSize(TypedValue.COMPLEX_UNIT_PX, mPreviewTextSizeLarge);
					mPreviewText.setTypeface(Typeface.DEFAULT);
				} else {
					mPreviewText.setText(getPreviewText(key));
					if (key.label.length() > 1 && key.codes.length < 2) {
						mPreviewText.setTextSize(TypedValue.COMPLEX_UNIT_PX, mKeyTextSize);
						mPreviewText.setTypeface(Typeface.DEFAULT_BOLD);
					} else {
						mPreviewText.setTextSize(TypedValue.COMPLEX_UNIT_PX, mPreviewTextSizeLarge);
						mPreviewText.setTypeface(Typeface.DEFAULT);
					}
				}
			}
		}
		mPreviewText.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), 
				MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
		int popupWidth = Math.max(mPreviewText.getMeasuredWidth(), mFirstKey.width 
				+ mPreviewText.getPaddingLeft() + mPreviewText.getPaddingRight());
		// final int popupHeight = Math.max(mPreviewHeight, key.height);
		final float scale = getContext().getResources().getDisplayMetrics().density;
		final int popupHeight = (int)(/*mKeyTextSize * scale*/ mPreviewTextSizeLarge * 1.25)
				+ (mPreviewText.getPaddingTop() + mPreviewText.getPaddingBottom());
		LayoutParams lp = mPreviewText.getLayoutParams();
		if (lp != null) {
			lp.width = popupWidth;
			lp.height = popupHeight;
		}
		if (!mPreviewCentered) {
			mPopupPreviewX = mFirstKey.x - mPreviewText.getPaddingLeft() + getPaddingLeft();
			mPopupPreviewY = mFirstKey.y - (keys[0].height/2) - popupHeight + mPreviewOffset;
		} else {
			// TODO: Fix this if centering is brought back
			mPopupPreviewX = 160 - mPreviewText.getMeasuredWidth() / 2;
			mPopupPreviewY = - mPreviewText.getMeasuredHeight();
		}
		mShowPreviewHandler.removeMessages(MSG_REMOVE_PREVIEW);
		getLocationInWindow(mOffsetInWindow);
		mOffsetInWindow[0] += mMiniKeyboardOffsetX; // Offset may be zero
		mOffsetInWindow[1] += mMiniKeyboardOffsetY; // Offset may be zero
		getLocationOnScreen(mWindowLocation);
		mWindowY = mWindowLocation[1];
		// Set the preview background state
		/*
		mPreviewText.getBackground().setState(
				key.popupResId != 0 ? LONG_PRESSABLE_STATE_SET : EMPTY_STATE_SET);
		*/
		mPopupPreviewX += mOffsetInWindow[0];
		mPopupPreviewY += mOffsetInWindow[1];

		// If the popup cannot be shown above the key, put it on the side
		if (mPopupPreviewY + mWindowY < 0) {
			// If the key you're pressing is on the left side of the keyboard, show the popup on
			// the right, offset by enough to see at least one key to the left/right.
			if (mFirstKey.x + key.width <= getWidth() / 2) {
				mPopupPreviewX += (int) (key.width * 2.5);
			} else {
				mPopupPreviewX -= (int) (key.width * 2.5);
			}
			mPopupPreviewY += popupHeight;
		}

		if (previewPopup.isShowing()) {
			previewPopup.update(mPopupPreviewX, mPopupPreviewY, popupWidth, popupHeight);
		} else {
			previewPopup.setWidth(popupWidth);
			previewPopup.setHeight(popupHeight);
			try {
				previewPopup.showAtLocation(
						this, Gravity.NO_GRAVITY, 
						mPopupPreviewX, mPopupPreviewY
						);
			} catch (final Exception e) {
				Log.i("NicoWnnG", "showAtLocation() failed");
			}
		}
		mPreviewText.setVisibility(View.VISIBLE);
	}

	public int getKeyIndices(int x, int y, int[] allKeys) {
		final Key[] keys = mKeys;
		int primaryIndex = NOT_A_KEY;
		int closestKey = NOT_A_KEY;
		int closestKeyDist = mProximityThreshold + 1;
		java.util.Arrays.fill(mDistances, Integer.MAX_VALUE);
		int [] nearestKeyIndices = getKeyboard().getNearestKeys(x, y);
		final int keyCount = nearestKeyIndices.length;
		for (int i = 0; i < keyCount; i++) {
			final int index = nearestKeyIndices[i];
			if (index >= keys.length) continue;
			final Key key = keys[index];
			int dist = 0;
			boolean isInside = key.isInside(x,y);
			if (isInside) {
				primaryIndex = nearestKeyIndices[i];
			}

			if (((isProximityCorrectionEnabled() 
					&& (dist = key.squaredDistanceFrom(x, y)) < mProximityThreshold) 
					|| isInside)
					&& key.codes[0] > 32) {
				// Find insertion point
				final int nCodes = key.codes.length;
				if (dist < closestKeyDist) {
					closestKeyDist = dist;
					closestKey = nearestKeyIndices[i];
				}
				
				if (allKeys == null) continue;
				
				for (int j = 0; j < mDistances.length; j++) {
					if (mDistances[j] > dist) {
						// Make space for nCodes codes
						System.arraycopy(mDistances, j, mDistances, j + nCodes,
								mDistances.length - j - nCodes);
						System.arraycopy(allKeys, j, allKeys, j + nCodes,
								allKeys.length - j - nCodes);
						for (int c = 0; c < nCodes; c++) {
							allKeys[j + c] = key.codes[c];
							mDistances[j + c] = dist;
						}
						break;
					}
				}
			}
		}
		if (primaryIndex == NOT_A_KEY) {
			primaryIndex = closestKey;
		}
		return primaryIndex;
	}

	public int getLastFlingDir() {
		return mLastFlingDir;
	}
	
	public void resetKeyboardFlingDir() {
		mLastFlingDir = -1;
		mFirstKey = null;
	}

	public boolean isPreviewPopupShown() {
		return (mPreviewPopup.isShowing() && mPreviewText.getVisibility() == View.VISIBLE);
	}
	
	
	
	/**
	 * @param kbd keyboard
	 */
	public void setDefaultKeyboardView(DefaultSoftKeyboard kbd) {
		mDefaultSoftKeyboard = kbd;
	}

	
	
	/**
	 * @return VerticalCorrection
	 */
	public int getVerticalCorrection() {
		return mVerticalCorrection;
	}

	/**
	 * @param pref pref
	 * @param editor editor
	 */
	public void setPreferences(final SharedPreferences pref, final EditorInfo editor) {
		mShowPreview = pref.getBoolean("popup_preview", true);
	}

	@Override
	public void invalidateKey(int keyIndex) {
		if (Build.VERSION.SDK_INT >= 8) {
			super.invalidateKey(keyIndex);
		} else {
			// OS2.1バグ回避
			super.invalidateAllKeys();
		}
	}

}
