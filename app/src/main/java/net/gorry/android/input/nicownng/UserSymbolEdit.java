/**
 * ◇
 */
package net.gorry.android.input.nicownng;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.text.ClipboardManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author gorry
 *
 */
public class UserSymbolEdit extends Activity implements View.OnClickListener {

	private static final String TAG = "UserSymbolEdit";
	private static final boolean V = false;
	private static final boolean D = false;
	private static final boolean I = true;
	//	private static final int WC = ViewGroup.LayoutParams.WRAP_CONTENT;
	private static final int FP = ViewGroup.LayoutParams.FILL_PARENT;
	private Activity me;
	private boolean mIsLandscape;

	@Override
	public void onCreate(final Bundle icicle) {
		if (I) Log.i(TAG, "onCreate()");
		super.onCreate(icicle);
		me = this;

		mIsLandscape = (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE);
		if (mIsLandscape) {
			if (Build.VERSION.SDK_INT >= 9) {
				// setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
				setRequestedOrientation(6);
			} else {
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			}
		} else {
			if (Build.VERSION.SDK_INT >= 9) {
				// setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
				setRequestedOrientation(7);
			} else {
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			}
		}

		setTitle(R.string.dialog_usersymbol_edit_title);
		setContentView(R.layout.usersymbol_edit);

	}

	@Override
	protected void onResume() {
		if (I) Log.i(TAG, "onResume()");
		super.onResume();
	}

	@Override
	protected void onPause() {
		if (I) Log.i(TAG, "onPause()");
		super.onPause();
	}

	@Override
	public void onDestroy() {
		if (I) Log.i(TAG, "onDestroy()");

		super.onDestroy();
	}

	/** @see android.view.View.OnClickListener */
	public void onClick(final View v) {

		switch (v.getId()) {
			case R.id.dialog_button_close:
				finish();
				break;
		}
	}
}
