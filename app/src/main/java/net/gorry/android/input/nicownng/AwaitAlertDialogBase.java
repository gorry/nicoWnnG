/**
 *
 * AiCiA - Android IRC Client
 *
 * Copyright (C)2010 GORRY.
 *
 */

package net.gorry.android.input.nicownng;

import java.util.concurrent.CountDownLatch;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.os.Handler;
import android.util.Log;
import android.view.ViewGroup;

/**
 *
 * 入力待ち型アラートダイアログ
 *
 *
 */
/**
 * 以下のように使う。
 *
 *  // 完全にダイアログ入力待ちを待ちたいときは、ロックを使う。
 *  Object o = new Object();

 *	// 以下はUIスレッドで実行しなければならない。
 *  // （すでにUIスレッドであれば切り替える必要はない）
 *	((Activity)context).runOnUiThread(new Runnable(){ public void run() {
 *		// ダイアログインスタンスを作成する。
 *		// このとき非UIスレッドで実行すると、IllegalThreadStateException例外が
 *		// 発生する。
 *		final AwaitAlertDialogXXX dlg = new AwaitAlertDialogXXX(context);
 *		dlg.create();
 *
 *  	// ここで非UIスレッドに切り替える。
 *		new Thread(new Runnable() {	public void run() {
 *			// ダイアログを表示して、入力を行う。
 *			// このときUIスレッドで実行すると、IllegalThreadStateException例外が
 *			// 発生する。
 *			final int result = dlg.show();
 *
 *			// 入力された値がログに表示される。
 *			Log.d("", "result=" + result);
 *
 *			// ロックを解除する。
 *			synchronized(o) {
 *				Log.d("", "Unlock thread");
 *				o.notifyAll();
 *			}
 *		} }).start();
 *	} });
 *
 *	// ダイアログ入力が終了するまでロックする。
 *	synchronized (o) {
 *		try {
 *			Log.d("", "Lock thread");
 *			o.wait();
 *		} catch (final InterruptedException e) {
 *			// TODO Auto-generated catch block
 *		}
 *	}
 *
 *	// ロック終了
 *	Log.d("", "Unlocked thread");
 *
 * 返り値は「YES/NO/OK/CANCEL」のいずれかが基本となる。
 * （AwaitAlertDialogListのように、返り値が増えるものもある）
 */
public abstract class AwaitAlertDialogBase {
	private static final String TAG = "AwaitAlertDialogBase";
	private static final boolean V = false;
	private static final int WC = ViewGroup.LayoutParams.WRAP_CONTENT;
	private static final int FP = ViewGroup.LayoutParams.FILL_PARENT;
	
	/** キャンセル選択値 */
	public static final int CANCEL = -1;
	/** YES選択値 */
	public static final int YES = 1;
	/** NO選択値 */
	public static final int NO = 0;
	/** OK選択値 */
	public static final int OK = 1;

	protected Context me;
	protected Handler mHandler;

	protected String mDialogTitle;
	protected AlertDialog mDialog;

	/**
	 * コンストラクタ
	 * @param context コンテキスト
	 */
	AwaitAlertDialogBase(final Context context) {
		if (V) Log.v(TAG, "AlertListDialog()");
		me = context;
	}

	/**
	 * 同期待ち
	 * @param handler 同期用ハンドラ
	 * @param dialog ダイアログクラス
	 * @param dismissListener ダイアログ消去リスナ
	 */
	private final void showDialogWaitDismiss(final Handler handler, final Dialog dialog, final OnDismissListener dismissListener ) {
		if (V) Log.v(TAG, "showDialogWaitDismiss()");
		final CountDownLatch signal = new CountDownLatch(1);
		dialog.setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss(final DialogInterface dlgIf) {
				try {
					if (dismissListener != null) {
						dismissListener.onDismiss(dlgIf);
					}
				}
				finally {
					signal.countDown();
				}
			}
		});
		if (Thread.currentThread() != handler.getLooper().getThread()) {
			handler.post(new Runnable() {
				@Override
				public void run() {
					dialog.show();
				}
			});
			try {
				signal.await();
			}
			catch (final InterruptedException e) {
				//
			}
		} else {
			dialog.show();
			throw new IllegalThreadStateException("use from non-UI thread.");
		}

	}

	/**
	 * ダイアログを作成
	 */
	public void create() {
		if (V) Log.v(TAG, "create()");
		mHandler = new Handler();
		if (Thread.currentThread() != mHandler.getLooper().getThread()) {
			throw new IllegalThreadStateException("use from UI thread.");
		}
		final AlertDialog.Builder bldr = new AlertDialog.Builder(me);
		bldr.setTitle(mDialogTitle);
		addView(bldr);
		mDialog = bldr.create();
	}

	/**
	 * ダイアログを開く
	 * @return ダイアログ入力結果。「YES/NO/OK/CANCEL」のいずれか
	 */
	public int show() {
		if (V) Log.v(TAG, "show()");
		showDialogWaitDismiss(mHandler, mDialog, new OnDismissListener() {
			@Override
			public void onDismiss(final DialogInterface dialog) {
				if (V) Log.v(TAG, "open() dismiss");
			}
		});
		if (V) Log.v(TAG, "show() end");

		return getResult();
	}

	/**
	 * ダイアログのタイトルを設定
	 * @param title ダイアログタイトル
	 */
	public void setTitle(final String title) {
		if (V) Log.v(TAG, "setTitle()");
		mDialogTitle = title;
	}

	/**
	 * ダイアログにビューを追加
	 * @param bldr ダイアログビルダクラス
	 */
	abstract void addView(final AlertDialog.Builder bldr);

	/**
	 * ダイアログにビューを追加
	 * @param bldr ダイアログビルダクラス
	 */
	abstract void onShowMyDialog();

	/**
	 * ダイアログの返り値を作成
	 * @return show()が返す値
	 */
	abstract int getResult();

}
