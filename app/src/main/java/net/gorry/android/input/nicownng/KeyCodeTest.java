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
public class KeyCodeTest extends Activity implements View.OnClickListener {

	private static final String TAG = "KeyCodeTest";
	private static final boolean V = false;
	private static final boolean D = false;
	private static final boolean I = true;
	//	private static final int WC = ViewGroup.LayoutParams.WRAP_CONTENT;
	private static final int FP = ViewGroup.LayoutParams.FILL_PARENT;
	private Activity me;

	private Button mClearLogButton;
	private Button mCopyLogButton;
	private Button mCloseButton;
	private EditText mKeycodeText;
	private TextView mKeycodeLog;
	private ScrollView mKeycodeScrollView;

	IntentFilter mIntentFilter;
	MyBroadcastReceiver mReceiver = null;

	@Override
	public void onCreate(final Bundle icicle) {
		if (I) Log.i(TAG, "onCreate()");
		super.onCreate(icicle);
		me = this;

		setTitle(R.string.dialog_keycode_test_title);
		setContentView(R.layout.keycode_test);

		// キーコードログスクロールビュー
		mKeycodeScrollView = (ScrollView)findViewById(R.id.keycode_scrollview);

		// キーコードログビュー
		mKeycodeLog = (TextView)findViewById(R.id.keycode_log);

		// キーコード入力用テキスト
		mKeycodeText = (EditText)findViewById(R.id.keycode_text);

		// ログの消去
		mClearLogButton = (Button)findViewById(R.id.dialog_button_clearlog);
		mClearLogButton.setOnClickListener(this);

		// クローズボタン
		mCopyLogButton = (Button)findViewById(R.id.dialog_button_copylog);
		mCopyLogButton.setOnClickListener(this);

		// クローズボタン
		mCloseButton = (Button)findViewById(R.id.dialog_button_close);
		mCloseButton.setOnClickListener(this);

	}

	@Override
	protected void onResume() {
		if (I) Log.i(TAG, "onResume()");
		super.onResume();

		myRegisterReceiver();

		Handler handler = new Handler();
		Message m = Message.obtain(handler,
		    new Runnable() {
		        public void run() {
		            InputMethodManager manager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		            manager.showSoftInput(mKeycodeText, InputMethodManager.SHOW_FORCED);
		        }
		    }
		);
		handler.sendMessageDelayed(m, 200);
	}

	@Override
	protected void onPause() {
		if (I) Log.i(TAG, "onPause()");

		EditText et = (EditText)findViewById(R.id.keycode_text);
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE); 
		imm.hideSoftInputFromWindow(et.getWindowToken(),0);  
		
		myUnregisterReceiver();

		super.onPause();
	}

	@Override
	public void onDestroy() {
		if (I) Log.i(TAG, "onDestroy()");

		myUnregisterReceiver();

		super.onDestroy();
	}

	/** @see android.view.View.OnClickListener */
	public void onClick(final View v) {

		switch (v.getId()) {
			case R.id.dialog_button_close:
				finish();
				break;
			case R.id.dialog_button_clearlog:
				mKeycodeText.setText("");
				mKeycodeLog.setText("");
				mKeycodeScrollView.scrollTo(0, 0);
				break;
			case R.id.dialog_button_copylog:
			{
				 ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
				 cm.setText(mKeycodeLog.getText());
				 break;
			}
		}
	}

	/*
	 * ブロードキャストレシーバー
	 */
	private class MyBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle bundle = intent.getExtras();
			String message = bundle.getString("message");

			if (mKeycodeLog.length() > 0) {
				mKeycodeLog.append("\n");
			}
			mKeycodeLog.append(message);
			mKeycodeScrollView.smoothScrollTo(0, mKeycodeLog.getHeight()-mKeycodeScrollView.getHeight());
		}
	}

	private void myRegisterReceiver() { 
		if (mReceiver == null) {
			mReceiver = new MyBroadcastReceiver();
			mIntentFilter = new IntentFilter();
			mIntentFilter.addAction("NICOWNNG_KEYCODETEST_ACTION");
			registerReceiver(mReceiver, mIntentFilter);        
		}
		final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		final SharedPreferences.Editor editor = pref.edit();
		editor.putBoolean("keycode_test", true);
		editor.commit();
	}

	private void myUnregisterReceiver() { 
		final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		final SharedPreferences.Editor editor = pref.edit();
		editor.putBoolean("keycode_test", false);
		editor.commit();

		if (mReceiver != null) {
			unregisterReceiver(mReceiver);
			mReceiver = null;
		}
	}

}
