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
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

/**
 *
 * 入力待ち型アラートダイアログ（リスト型）
 *
 * 返り値は以下のように拡張される。
 * CLICKED+n n番めの項目をクリック（一番上の項目がn=0）
 * LONGCLICKED+n n番めの項目をロングクリック（一番上の項目がn=0）
 * CANCEL 選択されなかった
 *
 * @author GORRY
 *
 */
public class AwaitAlertDialogIconList extends AwaitAlertDialogBase {
	private static final String TAG = "AwaitAlertDialogIconList";
	private static final boolean V = false;
	/** アイテムクリックの選択値 */
	public static final int CLICKED = 10000;
	/** アイテムロングクリックの選択値 */
	public static final int LONGCLICKED = 20000;

	@SuppressWarnings("unused")
	private static final int WC = ViewGroup.LayoutParams.WRAP_CONTENT;
	private static final int FP = ViewGroup.LayoutParams.FILL_PARENT;

	private final ArrayList<String> mItemList = new ArrayList<String>();
	private final ArrayList<Drawable> mIconList = new ArrayList<Drawable>();
	private final ArrayList<Integer> mItemShortcutLv = new ArrayList<Integer>();
	private final ArrayList<Integer> mItemShortcutLv1Id = new ArrayList<Integer>();
	private final ArrayList<Integer> mItemShortcutLv2Id = new ArrayList<Integer>();
	private int mLayout = R.layout.alertdialogiconlist;
	private int mClickResult = -1;
	private int mLongClickResult = -1;
	private boolean mEnableLongClick = true;
	private int mSelection = -1;
	private ListView mListView;

	/**
	 * コンストラクタ
	 * @param context コンテキスト
	 */
	AwaitAlertDialogIconList(final Context context) {
		super(context);
		if (V) Log.v(TAG, "AwaitAlertDialogIconList()");
	}

	/*
	 * ダイアログにビューを追加
	 * @see net.gorry.aicia.AwaitAlertDialogBase#addView(android.app.AlertDialog.Builder)
	 */
	@Override
	public void addView(final AlertDialog.Builder bldr) {
		if (V) Log.v(TAG, "addView()");

		int lv1Id = 0;
		int lv2Id = 0;
		for (int i=0; i<mItemShortcutLv.size(); i++) {
			switch (mItemShortcutLv.get(i)) {
				default:
				case 0:
					mItemShortcutLv1Id.add(0);
					mItemShortcutLv2Id.add(0);
					break;
				case 1:
					lv1Id++;
					lv2Id = 0;
					mItemShortcutLv1Id.add(lv1Id);
					mItemShortcutLv2Id.add(lv2Id);
					if (lv1Id <= 10) {
						final String ids = "1234567890";
						mItemList.set(i, ids.substring(lv1Id-1, lv1Id) + ": " + mItemList.get(i));
					}
					break;
				case 2:
					lv2Id++;
					mItemShortcutLv1Id.add(lv1Id);
					mItemShortcutLv2Id.add(lv2Id);
					if (lv2Id <= 26) {
						final String msg = mItemList.get(i);
						final String regex = "^([ ]*)(.*)";
						final Pattern p = Pattern.compile(regex);
						final Matcher m = p.matcher(msg);
						final String ids = "abcdefghijklmnopqrstuvwxyz";
						mItemList.set(i, m.replaceAll("$1" + ids.substring(lv2Id-1, lv2Id) + ": $2"));
					}
					break;
			}
		}

		mListView = new ListView(me);
		{
			mListView.setOnKeyListener(new OnKeyListener() {
				@Override
				public boolean onKey(final View v, final int keyCode, final KeyEvent event) {
					if (V) Log.v(TAG, "onCheckedChanged()");
					if (event.getAction() == KeyEvent.ACTION_DOWN) {
						switch (keyCode) {
							case KeyEvent.KEYCODE_1:
								doMoveSelectionLv1(1);
								break;
							case KeyEvent.KEYCODE_2:
								doMoveSelectionLv1(2);
								break;
							case KeyEvent.KEYCODE_3:
								doMoveSelectionLv1(3);
								break;
							case KeyEvent.KEYCODE_4:
								doMoveSelectionLv1(4);
								break;
							case KeyEvent.KEYCODE_5:
								doMoveSelectionLv1(5);
								break;
							case KeyEvent.KEYCODE_6:
								doMoveSelectionLv1(6);
								break;
							case KeyEvent.KEYCODE_7:
								doMoveSelectionLv1(7);
								break;
							case KeyEvent.KEYCODE_8:
								doMoveSelectionLv1(8);
								break;
							case KeyEvent.KEYCODE_9:
								doMoveSelectionLv1(9);
								break;
							case KeyEvent.KEYCODE_0:
								doMoveSelectionLv1(10);
								break;

							case KeyEvent.KEYCODE_A:
								doMoveSelectionLv2(1);
								break;
							case KeyEvent.KEYCODE_B:
								doMoveSelectionLv2(2);
								break;
							case KeyEvent.KEYCODE_C:
								doMoveSelectionLv2(3);
								break;
							case KeyEvent.KEYCODE_D:
								doMoveSelectionLv2(4);
								break;
							case KeyEvent.KEYCODE_E:
								doMoveSelectionLv2(5);
								break;
							case KeyEvent.KEYCODE_F:
								doMoveSelectionLv2(6);
								break;
							case KeyEvent.KEYCODE_G:
								doMoveSelectionLv2(7);
								break;
							case KeyEvent.KEYCODE_H:
								doMoveSelectionLv2(8);
								break;
							case KeyEvent.KEYCODE_I:
								doMoveSelectionLv2(9);
								break;
							case KeyEvent.KEYCODE_J:
								doMoveSelectionLv2(10);
								break;
							case KeyEvent.KEYCODE_K:
								doMoveSelectionLv2(11);
								break;
							case KeyEvent.KEYCODE_L:
								doMoveSelectionLv2(12);
								break;
							case KeyEvent.KEYCODE_M:
								doMoveSelectionLv2(13);
								break;
							case KeyEvent.KEYCODE_N:
								doMoveSelectionLv2(14);
								break;
							case KeyEvent.KEYCODE_O:
								doMoveSelectionLv2(15);
								break;
							case KeyEvent.KEYCODE_P:
								doMoveSelectionLv2(16);
								break;
							case KeyEvent.KEYCODE_Q:
								doMoveSelectionLv2(17);
								break;
							case KeyEvent.KEYCODE_R:
								doMoveSelectionLv2(18);
								break;
							case KeyEvent.KEYCODE_S:
								doMoveSelectionLv2(19);
								break;
							case KeyEvent.KEYCODE_T:
								doMoveSelectionLv2(20);
								break;
							case KeyEvent.KEYCODE_U:
								doMoveSelectionLv2(21);
								break;
							case KeyEvent.KEYCODE_V:
								doMoveSelectionLv2(22);
								break;
							case KeyEvent.KEYCODE_W:
								doMoveSelectionLv2(23);
								break;
							case KeyEvent.KEYCODE_X:
								doMoveSelectionLv2(24);
								break;
							case KeyEvent.KEYCODE_Y:
								doMoveSelectionLv2(25);
								break;
							case KeyEvent.KEYCODE_Z:
								doMoveSelectionLv2(26);
								break;
						}
					}
					return false;
				}
			});
			mListView.setLayoutParams(new LinearLayout.LayoutParams(WC, WC));
			mListView.setFocusable(true);
			mListView.setFocusableInTouchMode(true);

			final MyAdapter adapter = new MyAdapter(me, mLayout, mItemList);
			mListView.setAdapter(adapter);
			mListView.setOnItemClickListener(new MyListClickAdapter());
			if (mEnableLongClick) {
				mListView.setOnItemLongClickListener(new MyListLongClickAdapter());
			}
			mListView.requestFocus();
			mListView.setSelection(mSelection);
		}

		bldr.setView(mListView);
		bldr.setCancelable(true);
	}

	/**
	 * リストビュー各項目の表示用
	 */
	public class MyAdapter extends ArrayAdapter<String> {
		private final LayoutInflater inflater;

		/**
		 * @param context context
		 * @param textViewResourceId ID
		 * @param items items
		 */
		public MyAdapter(final Context context, final int textViewResourceId, final ArrayList<String> items) {
			super(context, textViewResourceId, items);
			inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public View getView(final int position, final View convertView, final ViewGroup parent) {
			View view = convertView;
			if (view == null) {
				view = inflater.inflate(R.layout.alertdialogiconlist, null);
			}

			final TextView text = (TextView)view.findViewById(R.id.alertdialogiconlist_text);
			text.setText(mItemList.get(position));
			final ImageView icon = (ImageView)view.findViewById(R.id.alertdialogiconlist_icon);
			icon.setImageDrawable(mIconList.get(position));
			return view;
		}
	}

	/**
	 * 指定Lv1ショートカットへ移動
	 * @param lv1Id Lv1Id
	 */
	private void doMoveSelectionLv1(final int lv1Id)
	{
		for (int i=0; i<mItemShortcutLv1Id.size(); i++) {
			if (mItemShortcutLv1Id.get(i) == lv1Id) {
				mListView.setSelection(i);
				mSelection = i;
				return;
			}
		}
	}

	/**
	 * 指定Lv2ショートカットへ移動
	 * @param lv2Id Lv2Id
	 */
	private void doMoveSelectionLv2(final int lv2Id)
	{
		int pos = mListView.getSelectedItemPosition();
		if (pos < 0) {
			pos = mSelection;
			if (pos < 0) pos = 0;
		}
		if (pos >= mItemShortcutLv1Id.size()) return;
		final int lv1Id = mItemShortcutLv1Id.get(pos);
		for (int i=0; i<mItemShortcutLv1Id.size(); i++) {
			if (mItemShortcutLv1Id.get(i) == lv1Id) {
				for (int j=i; j<mItemShortcutLv2Id.size(); j++) {
					if (mItemShortcutLv1Id.get(j) != lv1Id) {
						return;
					}
					if (mItemShortcutLv2Id.get(j) == lv2Id) {
						mListView.setSelection(j);
						mSelection = j;
						return;
					}
				}
			}
		}
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
	 * クリック時の応答
	 */
	class MyListClickAdapter implements OnItemClickListener {
		@Override
		public void onItemClick(final AdapterView<?> adapter, final View view, final int position, final long id) {
			if (V) Log.v(TAG, "onItemClick()");
			mClickResult = position;
			mDialog.dismiss();
		}
	}

	/**
	 * ロングクリック時の応答
	 */
	class MyListLongClickAdapter implements OnItemLongClickListener {
		@Override
		public boolean onItemLongClick(final AdapterView<?> adapter, final View view, final int position, final long id) {
			if (V) Log.v(TAG, "onItemLongClick()");
			mLongClickResult = position;
			mDialog.dismiss();
			return true;
		}
	}

	/**
	 * リストアイテムを登録
	 * @param pos 登録位置
	 * @param item 登録アイテム
	 * @param icon アイコン
	 * @param lv ショートカットレベル
	 */
	public void setItem(final int pos, final String item, final Drawable icon, final int lv) {
		if (V) Log.v(TAG, "setListItem()");
		mItemList.set(pos, item);
		if (icon != null) {
			mIconList.set(pos, MyIcon.resizeIcon(icon));
		} else {
			mIconList.set(pos, null);
		}
		mItemShortcutLv.set(pos, lv);
	}

	/**
	 * リストアイテムを末尾に登録
	 * @param item 登録アイテム
	 * @param icon アイコン
	 * @param lv ショートカットレベル
	 */
	public void addItem(final String item, final Drawable icon, final int lv) {
		if (V) Log.v(TAG, "addListItem()");
		if (V) Log.v(TAG, "item=["+item+"]");
		mItemList.add(item);
		if (icon != null) {
			mIconList.add(MyIcon.resizeIcon(icon));
		} else {
			mIconList.add(null);
		}
		mItemShortcutLv.add(lv);
	}

	/**
	 * リストの初期選択位置を設定
	 * @param pos 初期選択位置
	 */
	public void setSelection(final int pos) {
		if (V) Log.v(TAG, "setSelection()");
		mSelection = pos;
	}

	/**
	 * リストのレイアウトを設定
	 * @param resourceId リソースID
	 */
	public void setListResource(final int resourceId) {
		if (V) Log.v(TAG, "setListResource()");
		mLayout = resourceId;
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
