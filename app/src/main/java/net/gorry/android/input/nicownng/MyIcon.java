/**
 * 
 */
package net.gorry.android.input.nicownng;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.util.Log;

/**
 * 
 * アクティビティ情報の処理
 * 
 * @author GORRY
 *
 */
public class MyIcon {
	private static final String TAG = "MyIcon";
	private static final boolean V = false;
	private static Context me;

	/**
	 * コンテキストの設定
	 * @param context context
	 */
	public static void setContext(final Context context) {
		if (V) Log.v(TAG, "setContext()");
		me = context;
	}

	/**
	 * Drawable to Bitmap
	 * @param d Drawable
	 * @return Bitmap
	 */
	public static Bitmap getBitmapFromDrawable(final Drawable d) {
		final int w = d.getIntrinsicWidth();
		final int h = d.getIntrinsicHeight();
		final Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		final Canvas c = new Canvas(bmp);
		d.setBounds(0, 0, w, h);
		d.draw(c);
		return bmp;
	}

	/**
	 * アイコンのリサイズ
	 * @param icon アイコン
	 * @return リサイズ後アイコン
	 */
	public static Drawable resizeIcon(final Drawable icon) {
		if (V) Log.v(TAG, "resizeIcon()");
		if (icon != null) {
			final DisplayMetrics metrics = new DisplayMetrics();
			((Activity)me).getWindowManager().getDefaultDisplay().getMetrics(metrics);
			final float dw = 48*metrics.scaledDensity;
			final float dh = 48*metrics.scaledDensity;
			final Bitmap bmp = getBitmapFromDrawable(icon);
			if (bmp != null) {
				final int sw = bmp.getWidth();
				final int sh = bmp.getHeight();
				if (V) Log.v(TAG, "sw="+sw+" sh="+sh);
				if ((sw == (int)dw) && (sh == (int)dh)) {
					return icon;
				}
				final Matrix m = new Matrix();
				m.postScale(dw/sw, dh/sh);
				final Bitmap bmp2 = Bitmap.createBitmap(bmp, 0, 0, sw, sh, m, true);
				final Drawable icon2 = new BitmapDrawable(bmp2);
				return icon2;
			}
			return icon;
		}
		return null;
	}

}
