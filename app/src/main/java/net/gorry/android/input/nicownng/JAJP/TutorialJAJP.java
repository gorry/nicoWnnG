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

/*
 * Copyright (C) 2008-2009 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package net.gorry.android.input.nicownng.JAJP;

import java.util.ArrayList;
import java.util.List;

import net.gorry.android.input.nicownng.NicoWnnGJAJP;
import net.gorry.android.input.nicownng.R;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.StaticLayout;
import android.text.style.DynamicDrawableSpan;
import android.text.style.ImageSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.PopupWindow;
import android.widget.TextView;

public class TutorialJAJP implements OnTouchListener {

	private final List<Bubble> mBubbles = new ArrayList<Bubble>();
	private static final int LONG_PRESS_INDEX = 8;
	private final View mInputView;
	private final NicoWnnGJAJP mIme;
	private final int[] mLocation = new int[2];
	private static final int MSG_SHOW_BUBBLE = 0;

	private int mBubbleIndex;
	private final DefaultSoftKeyboardJAJP mInputManager;
	private boolean mEnableKeyTouch = false;

	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(final Message msg) {
			switch (msg.what) {
				case MSG_SHOW_BUBBLE:
					final Bubble bubba = (Bubble) msg.obj;
					bubba.show(mLocation[0], mLocation[1]);
					break;
			}
		}
	};

	class Bubble {
		Drawable bubbleBackground;
		int x;
		int y;
		int width;
		int gravity;
		CharSequence text;
		boolean dismissOnTouch;
		boolean dismissOnClose;
		PopupWindow window;
		TextView textView;
		View inputView;

		Bubble(final Context context, final View inputView,
				final int backgroundResource, final int bx, final int by, final int description, final int guide) {

			final CharSequence text = context.getResources().getText(description);
			init(context, inputView, backgroundResource, bx, by, text, guide, false);
		}

		Bubble(final Context context, final View inputView, final int backgroundResource, final int bx, final int by,
				final CharSequence description, final int guide, final boolean leftAlign) {
			init(context, inputView, backgroundResource, bx, by, description, guide, leftAlign);
		}

		void init(final Context context, final View inputView, final int backgroundResource,
				final int bx, final int by, final CharSequence description, final int guide, final boolean leftAlign) {
			bubbleBackground = context.getResources().getDrawable(backgroundResource);
			x = bx;
			y = by;
			width = (int) (inputView.getWidth() * 0.9);
			gravity = Gravity.TOP | Gravity.LEFT;
			text = new SpannableStringBuilder()
			.append(description)
			.append("\n")
			.append(context.getResources().getText(guide));
			dismissOnTouch = true;
			dismissOnClose = false;
			this.inputView = inputView;
			window = new PopupWindow(context);
			window.setBackgroundDrawable(null);
			final LayoutInflater inflate =
				(LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			textView = (TextView) inflate.inflate(R.layout.bubble_text, null);
			textView.setBackgroundDrawable(bubbleBackground);
			textView.setText(text);
			if (leftAlign) {
				textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
			}
			window.setContentView(textView);
			window.setFocusable(false);
			window.setTouchable(true);
			window.setOutsideTouchable(false);
		}

		private int chooseSize(final PopupWindow pop, final View parentView, final CharSequence text, final TextView tv) {
			final int wid = tv.getPaddingLeft() + tv.getPaddingRight();
			final int ht = tv.getPaddingTop() + tv.getPaddingBottom();

			/*
			 * Figure out how big the text would be if we laid it out to the
			 * full width of this view minus the border.
			 */
			final int cap = width - wid;

			final Layout l = new StaticLayout(text, tv.getPaint(), cap,
					Layout.Alignment.ALIGN_NORMAL, 1, 0, true);
			float max = 0;
			for (int i = 0; i < l.getLineCount(); i++) {
				max = Math.max(max, l.getLineWidth(i));
			}

			/*
			 * Now set the popup size to be big enough for the text plus the border.
			 */
			pop.setWidth(width);
			pop.setHeight(ht + l.getHeight());
			return l.getHeight();
		}

		void show(int offx, int offy) {
			final int textHeight = chooseSize(window, inputView, text, textView);
			offy -= textView.getPaddingTop() + textHeight;
			if ((inputView.getVisibility() == View.VISIBLE)
					&& (inputView.getWindowVisibility() == View.VISIBLE)) {
				try {
					if ((gravity & Gravity.BOTTOM) == Gravity.BOTTOM) offy -= window.getHeight();
					if ((gravity & Gravity.RIGHT) == Gravity.RIGHT) offx -= window.getWidth();
					textView.setOnTouchListener(new View.OnTouchListener() {
						public boolean onTouch(final View view, final MotionEvent me) {

							final boolean ret = !mEnableKeyTouch;
							switch (me.getAction()) {
								case MotionEvent.ACTION_UP:
									if (mBubbleIndex >= mBubbles.size()) {
										mInputView.setOnTouchListener(null);
									} else {
										next();
									}
									break;
								default:
									break;
							}
							return ret;
						}
					});
					window.showAtLocation(inputView, Gravity.NO_GRAVITY, x + offx, y + offy);
				} catch (final Exception e) {
				}
			}
		}

		void hide() {
			if (window.isShowing()) {
				textView.setOnTouchListener(null);
				window.dismiss();
			}
		}

		boolean isShowing() {
			return window.isShowing();
		}
	}

	/** Constructor */
	public TutorialJAJP(final NicoWnnGJAJP ime, final View inputView, final DefaultSoftKeyboardJAJP inputManager) {
		mInputManager = inputManager;
		mInputView = inputView;
		mIme = ime;

		final Context context = inputView.getContext();
		final int inputWidth = inputView.getWidth();
		final Resources r = inputView.getContext().getResources();
		final int x = inputWidth / 20;
		r.getDimensionPixelOffset(R.dimen.bubble_pointer_offset);

		final SpannableStringBuilder spannable = new SpannableStringBuilder();
		Bubble button;

		spannable.clear();
		spannable.append(r.getText(R.string.tip_to_step1));

		setSpan(spannable, "\u25cb", R.drawable.tutorial_12key_key);
		button = new Bubble(context, inputView,
				R.drawable.dialog_bubble, x, 0,
				spannable, R.string.touch_to_continue, false);
		mBubbles.add(button);

		spannable.clear();
		spannable.append(r.getText(R.string.tip_to_step2_a));

		setSpan(spannable, "\u25cb", R.drawable.tutorial_12key_toggle);
		button = new Bubble(context, inputView,
				R.drawable.dialog_bubble, x, 0,
				spannable, R.string.touch_to_continue, true);
		mBubbles.add(button);

		spannable.append(r.getText(R.string.tip_to_step2_b));

		setSpan(spannable, "\u2192", R.drawable.tutorial_12key_right);

		button = new Bubble(context, inputView,
				R.drawable.dialog_bubble, x, 0,
				spannable, R.string.touch_to_continue, true);
		mBubbles.add(button);

		spannable.append(r.getText(R.string.tip_to_step2_c));

		setSpan(spannable, "\u25cb", R.drawable.tutorial_12key_toggle);

		button = new Bubble(context, inputView,
				R.drawable.dialog_bubble, x, 0,
				spannable, R.string.touch_to_continue, true);
		mBubbles.add(button);

		spannable.append(r.getText(R.string.tip_to_step2_d));

		setSpan(spannable, "\u25a0", R.drawable.tutorial_12key_space_jp);

		setSpan(spannable, "\u2193", R.drawable.tutorial_12key_enter);

		button = new Bubble(context, inputView,
				R.drawable.dialog_bubble, x, 0,
				spannable, R.string.touch_to_continue, true);
		mBubbles.add(button);

		spannable.clear();
		spannable.append(r.getText(R.string.tip_to_step3_a));

		setSpan(spannable, "\u25a0", R.drawable.tutorial_12key_mode);
		button = new Bubble(context, inputView,
				R.drawable.dialog_bubble_moji, x, 0,
				spannable, R.string.touch_to_continue, false);
		mBubbles.add(button);

		button = new Bubble(context, inputView,
				R.drawable.dialog_bubble_moji, x, 0,
				R.string.tip_to_step3_b, R.string.touch_to_continue);
		mBubbles.add(button);

		button = new Bubble(context, inputView,
				R.drawable.dialog_bubble_moji, x, 0,
				R.string.tip_to_step3_c, R.string.touch_to_continue);
		mBubbles.add(button);

		spannable.clear();
		spannable.append(r.getText(R.string.tip_to_step4));

		setSpan(spannable, "\u25a0", R.drawable.tutorial_12key_mode);
		button = new Bubble(context, inputView,
				R.drawable.dialog_bubble_moji, x, 0,
				spannable, R.string.touch_to_try, false);
		mBubbles.add(button);

		spannable.clear();
		spannable.append(r.getText(R.string.tip_to_step5));

		setSpan(spannable, "\u2190", R.drawable.tutorial_back);

		button = new Bubble(context, inputView,
				R.drawable.dialog_bubble, x, 0,
				spannable, R.string.touch_to_continue, false);
		mBubbles.add(button);

		button = new Bubble(context, inputView,
				R.drawable.dialog_bubble, x, 0,
				R.string.tip_to_step6, R.string.touch_to_finish);
		mBubbles.add(button);
	}

	private void setSpan(final SpannableStringBuilder spannable, final String marker, final int imageResourceId) {
		final String text = spannable.toString();
		int target = text.indexOf(marker);
		while (0 <= target) {
			final ImageSpan span = new ImageSpan(mIme, imageResourceId,
					DynamicDrawableSpan.ALIGN_BOTTOM);
			spannable.setSpan(span, target, target + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			target = text.indexOf(marker, target + 1);
		}
	}

	public void start() {
		mInputView.getLocationInWindow(mLocation);
		mBubbleIndex = -1;
		mInputView.setOnTouchListener(this);
		next();
	}

	boolean next() {
		if (mBubbleIndex >= 0) {
			if (!mBubbles.get(mBubbleIndex).isShowing()) {
				return true;
			}
			for (int i = 0; i <= mBubbleIndex; i++) {
				mBubbles.get(i).hide();
			}
		}
		mBubbleIndex++;
		if (mBubbleIndex >= mBubbles.size()) {
			mEnableKeyTouch = true;
			mIme.sendDownUpKeyEvents(-1);
			mIme.tutorialDone();
			return false;
		}

		if ((6 <= mBubbleIndex) && (mBubbleIndex <= 8)) {
			mInputManager.nextKeyMode();
		}

		if (mBubbleIndex == LONG_PRESS_INDEX) {
			mEnableKeyTouch = true;
		} else if (LONG_PRESS_INDEX < mBubbleIndex) {
			mEnableKeyTouch = false;
		}

		mHandler.sendMessageDelayed(
				mHandler.obtainMessage(MSG_SHOW_BUBBLE, mBubbles.get(mBubbleIndex)), 500);
		return true;
	}

	void hide() {
		for (int i = 0; i < mBubbles.size(); i++) {
			mBubbles.get(i).hide();
		}
		mInputView.setOnTouchListener(null);
	}

	public boolean close() {
		mHandler.removeMessages(MSG_SHOW_BUBBLE);
		hide();
		return true;
	}

	public boolean onTouch(final View v, final MotionEvent event) {
		final boolean ret = !mEnableKeyTouch;
		if (event.getAction() == MotionEvent.ACTION_UP) {
			if (mBubbleIndex >= mBubbles.size()) {
				mInputView.setOnTouchListener(null);
			} else {
				if (mBubbleIndex != LONG_PRESS_INDEX) {
					next();
				}
			}
		}
		return ret;
	}
}
