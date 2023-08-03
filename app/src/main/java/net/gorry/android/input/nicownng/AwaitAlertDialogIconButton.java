/**
 *
 * AiCiA - Android IRC Client
 *
 * Copyright (C)2010 GORRY.
 *
 */

package net.gorry.android.input.nicownng;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

/**
 *
 * 入力待ち型アラートダイアログ（ボタン型）
 *
 * 返り値は以下のように拡張される。
 * CLICKED+n n番めの項目をクリック（一番上の項目がn=0）
 * LONGCLICKED+n n番めの項目をロングクリック（一番上の項目がn=0）
 * CANCEL 選択されなかった
 *
 * @author GORRY
 *
 */
public class AwaitAlertDialogIconButton extends AwaitAlertDialogBase {
	private static final String TAG = "AwaitAlertDialogIconButton";
	private static final boolean V = false;
	private static final boolean D = false;
	private static final boolean I = false;
	/** アイテムクリックの選択値 */
	public static final int CLICKED = 10000;
	/** アイテムロングクリックの選択値 */
	public static final int LONGCLICKED = 20000;

	@SuppressWarnings("unused")
	private static final int WC = ViewGroup.LayoutParams.WRAP_CONTENT;
	private static final int FP = ViewGroup.LayoutParams.FILL_PARENT;

	private LinearLayout mLayout;
	private LinearLayout[] mLayoutH;
	private int mButtonNumX;
	private int mButtonNumY;
	private Button[] mButton = null;
	private int mClickResult = -1;
	private int mLongClickResult = -1;
	private boolean mEnableLongClick = true;
	private int mSelection = -1;
	private ListView mListView;

	
	/**
	 * コンストラクタ
	 * @param context コンテキスト
	 */
	AwaitAlertDialogIconButton(final Context context, int w, int h) {
		super(context);
		if (V) Log.v(TAG, "AwaitAlertDialogIconButton()");

		mButtonNumX = w;
		mButtonNumY = h;
		mButton = new Button[w*h];
		mLayoutH = new LinearLayout[h];
		for (int y=0; y<h; y++) {
			mLayoutH[y] = new LinearLayout(me);
			mLayoutH[y].setOrientation(LinearLayout.HORIZONTAL);
			mLayoutH[y].setLayoutParams(new LinearLayout.LayoutParams(WC, WC));
			for (int x=0; x<w; x++) {
				int i = y*w+x;
				mButton[i] = new Button(me);
				mButton[i].setOnClickListener(mMyButtonClickListener);
				mButton[i].setOnLongClickListener(mMyButtonLongClickListener);
				mButton[i].setVisibility(View.GONE);
				mLayoutH[y].addView(mButton[i]);
			}
		}
		
	}

	/*
	 * ダイアログにビューを追加
	 * @see net.gorry.aicia.AwaitAlertDialogBase#addView(android.app.AlertDialog.Builder)
	 */
	@Override
	public void addView(final AlertDialog.Builder bldr) {
		if (V) Log.v(TAG, "addView()");

		mLayout = new LinearLayout(me);
		mLayout.setOrientation(LinearLayout.VERTICAL);
		mLayout.setLayoutParams(new LinearLayout.LayoutParams(WC, WC));
		for (int y=0; y<mButtonNumY; y++) {
			mLayout.addView(mLayoutH[y]);
		}

		bldr.setView(mLayout);
		bldr.setNegativeButton("Cancel", MyClickListener);
		bldr.setCancelable(true);
	}


	/**
	 * ダイアログ表示時に呼び出される
	 */
	@Override
	public void onShowMyDialog() {
		if (V) Log.v(TAG, "onShowMyDialog()");
	}

	/**
	 * ダイアログの返り値を作成
	 * @return show()が返す値
	 */
	@Override
	public int getResult() {
		if (V) Log.v(TAG, "getResult()");
		if (mClickResult >= 0) return (CLICKED + mClickResult);
		if (mLongClickResult >= 0) return (LONGCLICKED + mLongClickResult);
		return CANCEL;
	}

	/**
	 * Cancelクリック時の応答
	 */
	DialogInterface.OnClickListener MyClickListener = new DialogInterface.OnClickListener(){
		@Override
		public void onClick(final DialogInterface dialog, final int which) {
			if (I) Log.i(TAG, "onClick()");
			switch (which) {
				case DialogInterface.BUTTON2:
					mClickResult = -1;
					mLongClickResult = -1;
					break;
			}
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
					mClickResult = i+CLICKED;
					mDialog.dismiss();
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
					mClickResult = i+LONGCLICKED;
					mDialog.dismiss();
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
		ImageSpan span = new ImageSpan(bmp);
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
