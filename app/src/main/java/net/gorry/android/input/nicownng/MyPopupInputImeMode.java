/**
 *
 * AiCiA - Android IRC Client
 *
 * Copyright (C)2010 GORRY.
 *
 */

package net.gorry.android.input.nicownng;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.provider.Settings;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.DynamicDrawableSpan;
import android.text.style.ImageSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

/**
 *
 * IMEモード選択ポップアップ
 *
 */
public class MyPopupInputImeMode {
	private static final String TAG = "PopupInputMode";
	private static final boolean V = false;
	@SuppressWarnings("unused")
	private static final boolean D = false;
	@SuppressWarnings("unused")
	private static final boolean I = false;

	private static final int WC = ViewGroup.LayoutParams.WRAP_CONTENT;
	@SuppressWarnings("unused")
	private static final int FP = ViewGroup.LayoutParams.FILL_PARENT;

	private Context me;

	private PopupWindow mPopupWindow;
	private LinearLayout mLayout;
	private LinearLayout mButtonLayout;
	private LinearLayout[] mButtonLayoutH;
	private int mButtonWidth;
	private int mButtonHeight;
	private int mButtonNumX;
	private int mButtonNumY;
	private Button[] mButton = null;
	private ImageButton mCloseButton = null;
	private int mClickResult = -1;
	private boolean mEnableLongClick = true;
	private boolean mOutsideTouchable = false;
	private OnMyPopupInputListener mOnMyPopupInputListener = null;

	/**
	 * コンストラクタ
	 * @param context コンテキスト
	 */
	MyPopupInputImeMode(final Context context, int w, int h, int btnw, int btnh) {
		if (V) Log.v(TAG, "PopupInputMode()");

		me = context;
		mButtonWidth = btnw;
		mButtonHeight = btnh;
		if (Build.VERSION.SDK_INT < 13) {
			if (mButtonWidth > 100) {
				// ボタン横幅が大きすぎるとレイアウトミスする環境があるので制限
				mButtonWidth = 100;
			}
		}
		mButtonNumX = w;
		mButtonNumY = h;
		mButton = new Button[w*h];
		mButtonLayoutH = new LinearLayout[h];

		mButtonLayout = new LinearLayout(me);
		mButtonLayout.setOrientation(LinearLayout.VERTICAL);
		mButtonLayout.setLayoutParams(new LinearLayout.LayoutParams(WC, WC));
		
		final int textsize = NicoWnnGJAJP.getInstance().candidatesViewTextSize();

		for (int y=0; y<h; y++) {
			mButtonLayoutH[y] = new LinearLayout(me);
			mButtonLayoutH[y].setOrientation(LinearLayout.HORIZONTAL);
			mButtonLayoutH[y].setLayoutParams(new LinearLayout.LayoutParams(WC, WC));
			mButtonLayoutH[y].setGravity(Gravity.LEFT|Gravity.CENTER_VERTICAL|Gravity.CLIP_VERTICAL);
			mButtonLayout.addView(mButtonLayoutH[y]);
			
			for (int x=0; x<w; x++) {
				int i = y*w+x;
				mButton[i] = new Button(me) {
					@Override
					protected void onSizeChanged(final int w, final int h, final int oldw, final int oldh) {
						super.onSizeChanged(w, h, oldw, oldh);
						
						// ボタンの縦横幅をすべて同じにする
						boolean changed = false;
						if (this == mButton[mButtonNumX*mButtonNumY-1]) {
							int maxw = mButton[0].getWidth();
							int maxh = mButton[0].getHeight();
							for (int i=1; i<mButtonNumY*mButtonNumX; i++) {
								int bw = mButton[i].getWidth();
								int bh = mButton[i].getHeight();
								if ((maxw < bw) || (maxh < bh)) {
									maxw = bw;
									maxh = bh;
									changed = true;
								}
							}
							if (changed) {
								for (int i=0; i<mButtonNumY*mButtonNumX; i++) {
									mButton[i].setWidth(maxw);
									mButton[i].setHeight(maxh);
								}
								mLayout.requestLayout();
							}
						}
					}
				};
				mButton[i].setLayoutParams(new LinearLayout.LayoutParams(WC, WC));
				mButton[i].setWidth(mButtonWidth);
				mButton[i].setHeight(mButtonHeight);
				mButton[i].setTextSize(textsize);
				mButton[i].setOnClickListener(mMyButtonClickListener);
				mButton[i].setOnLongClickListener(mMyButtonLongClickListener);
				mButton[i].setVisibility(View.GONE);
				mButton[i].setGravity(Gravity.CENTER|Gravity.CLIP_VERTICAL);
				mButton[i].setSingleLine();
				mButton[i].setEllipsize(null);
				mButton[i].setPadding(0,2,0,2);
				mButton[i].setFocusable(true);
				mButtonLayoutH[y].addView(mButton[i]);
			}
		}

		mLayout = new LinearLayout(me);
		mLayout.setOrientation(LinearLayout.HORIZONTAL);
		mLayout.setLayoutParams(new LinearLayout.LayoutParams(WC, WC));
		mLayout.setGravity(Gravity.LEFT|Gravity.CENTER_VERTICAL|Gravity.CLIP_VERTICAL);
		mLayout.addView(mButtonLayout);

		mCloseButton = new ImageButton(me);
		mCloseButton.setImageResource(R.drawable.btn_dialog_normal);
		mCloseButton.setBackgroundColor(0);
		mCloseButton.setOnClickListener(mCancelButtonClickListener);
		mCloseButton.setPadding(0,0,0,0);
		mCloseButton.setFocusable(true);
		mLayout.addView(mCloseButton);
		
		mPopupWindow = new PopupWindow(me);
		mPopupWindow.setWindowLayoutMode(WC, WC);
		mPopupWindow.setContentView(mLayout);
	}

	/**
	 * 入力リスナの設定
	 * @param onMyPopupInputListener 入力リスナ
	 */
	public void setOnMyPopupInputListener(OnMyPopupInputListener onMyPopupInputListener) {
		mOnMyPopupInputListener = onMyPopupInputListener;
	}
	
	/**
	 * ポップアップの外側にタッチメッセージを出すかどうかを設定
	 * @param sw スイッチ
	 */
	public void setOutsideTouchable(boolean sw) {
		mOutsideTouchable = sw;
	}
	
	/**
	 * ポップアップ表示
	 * @param v 親ビュー
	 */
	public void show(View v) {
		// NicoWnnGJAJP.getInstance().setNoStartInputView(true);
		NicoWnnGJAJP.getInstance().setNoStartInputView2(true);
		mPopupWindow.setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_UNCHANGED);
		mPopupWindow.setFocusable(true);
		mPopupWindow.setTouchable(true);
		mPopupWindow.setOutsideTouchable(true/*mOutsideTouchable*/);
		mPopupWindow.setTouchInterceptor(mOnTouchListener);
		// mPopupWindow.setBackgroundDrawable(me.getResources().getDrawable(R.drawable.popup_background));
		mPopupWindow.showAtLocation(v, Gravity.CENTER, 0, 0);
	}
	
	private View.OnTouchListener mOnTouchListener = new View.OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			int w = v.getWidth();
			int h = v.getHeight();
			int action = event.getAction();
			int x = (int)event.getX();
			int y = (int)event.getY();
			if ((x >= 0) && (x < w) && (y >= 0) && (y < h)) {
				return false;
			}
			return true;
		}
	};

	/**
	 * キャンセル
	 */
	public void cancel() {
		mClickResult = OnMyPopupInputListener.CANCEL;
		if (mOnMyPopupInputListener != null) {
			mOnMyPopupInputListener.onInput(mClickResult);
		}
		NicoWnnGJAJP.getInstance().setNoStartInputView(true);
		NicoWnnGJAJP.getInstance().setNoStartInputView2(false);
		mPopupWindow.dismiss();
	}
	
	/**
	 * Cancelボタンクリック時の応答
	 */
	private final OnClickListener mCancelButtonClickListener = new OnClickListener() {
		@Override
		public void onClick(final View v) {
			cancel();
		}
	};

	/**
	 * ボタンクリック時の応答
	 */
	private final OnClickListener mMyButtonClickListener = new OnClickListener() {
		@Override
		public void onClick(final View v) {
			if (!v.isShown()) {
				return;
			}
			for (int i=0; i<mButton.length; i++) {
				if (v == mButton[i]) {
					mClickResult = i+OnMyPopupInputListener.CLICKED;
					if (mOnMyPopupInputListener != null) {
						mOnMyPopupInputListener.onInput(mClickResult);
					}
					NicoWnnGJAJP.getInstance().setNoStartInputView(true);
					NicoWnnGJAJP.getInstance().setNoStartInputView2(false);
					mPopupWindow.dismiss();
					return;
				}
			}
		}
	};

	/**
	 * ボタンロングクリック時の応答
	 */
	private final OnLongClickListener mMyButtonLongClickListener = new OnLongClickListener() {
		@Override
		public boolean onLongClick(final View v) {
			if (!mEnableLongClick) {
				return false;
			}
			if (!v.isShown()) {
				return false;
			}
			for (int i=0; i<mButton.length; i++) {
				if (v == mButton[i]) {
					mClickResult = i+OnMyPopupInputListener.LONGCLICKED;
					if (mOnMyPopupInputListener != null) {
						mOnMyPopupInputListener.onInput(mClickResult);
					}
					NicoWnnGJAJP.getInstance().setNoStartInputView(true);
					NicoWnnGJAJP.getInstance().setNoStartInputView2(false);
					mPopupWindow.dismiss();
					return true;
				}
			}
			return false;
		}
	};

	/**
	 * ボタンにテキストを登録
	 * @param x 登録位置
	 * @param y 登録位置
	 * @param text テキスト
	 */
	public void setItemText(final int x, final int y, final String text) {
		if (V) Log.v(TAG, "setItemText()");
		int pos = y*mButtonNumX + x;
		mButton[pos].setText(text);
		mButton[pos].setVisibility(View.VISIBLE);
	}

	/**
	 * ボタンにアイコンを登録
	 * @param x 登録位置
	 * @param y 登録位置
	 * @param bmp アイコン
	 */
	public void setItemIcon(final int x, int y, final Bitmap bmp) {
		if (V) Log.v(TAG, "setItemText()");
		int pos = y*mButtonNumX + x;
		ImageSpan span = new ImageSpan(bmp, DynamicDrawableSpan.ALIGN_BASELINE);
		final SpannableString spannable = new SpannableString("   ");
		spannable.setSpan(span, 1, 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		mButton[pos].setText(spannable);
		mButton[pos].setVisibility(View.VISIBLE);

	}

	/**
	 * ロングクリックの有効/無効を設定
	 * @param enable 有効にするならtrue
	 */
	public void setLongClickEnable(final boolean enable) {
		if (V) Log.v(TAG, "setLongClickEnable()");
		mEnableLongClick = enable;
	}
}
