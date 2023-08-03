/*
 */
package net.gorry.android.input.nicownng;

import java.util.StringTokenizer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Toast;
import android.os.Build;

public class NicoWnnGMain extends Activity {
	@Override public void onCreate(final Bundle saveInstanceState) {
		super.onCreate(saveInstanceState);

        // Log.e("NicoWnnG", "開始");
		ApplicationInfo appInfo = getApplicationInfo();
        int maskByDebuggable = appInfo.flags & ApplicationInfo.FLAG_DEBUGGABLE;
        boolean isDebug = maskByDebuggable == ApplicationInfo.FLAG_DEBUGGABLE;
        if (isDebug) {
            Log.e("NicoWnnG", "デバッグログテスト");
        }

		SymbolList.copyUserSymbolDicFileToExternalStorageDirectory(this, false);

		if (NicoWnnGEN.getInstance() == null) {
			NicoWnnGEN t = new NicoWnnGEN(this);
		}

		if (NicoWnnGJAJP.getInstance() == null) {
			NicoWnnGJAJP t = new NicoWnnGJAJP(this);
		}
		NicoWnnGJAJP.getInstance().initializeEasySetting();
		NicoWnnGJAJP.getInstance().convertOldPreferces();

		final WebView webView = new WebView(this);
		webView.getSettings().setJavaScriptEnabled(true);
		final Js2Java js2Java = new Js2Java(this);
		webView.addJavascriptInterface(js2Java, "android");
		webView.loadUrl("file:///android_asset/openwnn_main.html");
		setContentView(webView);

		final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		final SharedPreferences.Editor editor = pref.edit();
		editor.putBoolean("open_help_first", true);
		editor.commit();
	}

	
	public class Js2Java {
		private final Context me;

		public Js2Java(final Context context) {
			me = context;
		}

		@JavascriptInterface
		public String getMachineType() {
			int type = NicoWnnGJAJP.getInstance().getMachineType();
			switch (type) {
				case 1:
				case 2:
					return ("2");
			}
			return ("1");
		}

		private String getKeyboard(String mode) {
			final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(me);
			String key;
			key = "change_kana_12key";
			final boolean type = pref.getBoolean(key+mode, false);

			if (type) {
				key = "input_mode";
				final String type_12key = pref.getString(key+mode, NicoWnnG.INPUTMODE_NORMAL);
				if (type_12key.equalsIgnoreCase(NicoWnnG.INPUTMODE_2TOUCH)) {
					return ("1_3");
				}
				key = "nicoflick_mode";
				final int flick = Integer.valueOf(pref.getString(key+mode, "0"));
				if (flick != 0) {
					return ("1_2");
				}
				return ("1_1");
			}

			key = "qwerty_kana_mode3";
			final int type_qwerty = Integer.valueOf(pref.getString(key+mode, "0"));
			switch (type_qwerty) {
				case DefaultSoftKeyboard.KANAMODE_ROMAN2:
					return ("2_1");
				case DefaultSoftKeyboard.KANAMODE_ROMAN_COMPACT:
					return ("2_2");
				case DefaultSoftKeyboard.KANAMODE_ROMAN_MINI:
					return ("2_3");
				case DefaultSoftKeyboard.KANAMODE_ROMAN_MINI2:
					return ("2_4");
				case DefaultSoftKeyboard.KANAMODE_JIS2:
					return ("2_5");
				case DefaultSoftKeyboard.KANAMODE_50ON2:
					return ("2_6");
				case DefaultSoftKeyboard.KANAMODE_ROMAN:
					return ("2_7");
				case DefaultSoftKeyboard.KANAMODE_JIS:
					return ("2_8");
				case DefaultSoftKeyboard.KANAMODE_50ON:
					return ("2_9");
					
			}
			return ("2_1");
		}

		@JavascriptInterface
		public String getPortraitKeyboard() {
			return getKeyboard("_portrait");
		}

		@JavascriptInterface
		public String getLandscapeKeyboard() {
			return getKeyboard("_landscape");
		}

		@JavascriptInterface
		public String getKeyboardSize() {
			final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(me);
			String key;
			String mode = "_portrait";

			key = "mainview_height_mode2";
			int size = Integer.parseInt(pref.getString(key+mode, "0"))+1;
			if (size > 10) size = 10;
			if (size < 1) size = 1;

			return String.valueOf(size);
		}

		@JavascriptInterface
		public void callback(final String msg) {
			if (msg.equalsIgnoreCase("languageSetting")) {
				final Intent intent = new Intent(Intent.ACTION_MAIN);
				if (Build.VERSION.SDK_INT >= 14) {
					intent.setClassName("com.android.settings", "com.android.settings.Settings$InputMethodAndLanguageSettingsActivity");
				} else if (Build.VERSION.SDK_INT >= 11) {
					intent.setClassName("com.android.settings", "com.android.settings.Settings$InputMethodConfigActivity");
				} else {
					intent.setClassName("com.android.settings", "com.android.settings.LanguageSettings");
				}
				try {
					me.startActivity(intent);
				} catch (final Exception e) {
					Toast.makeText(
							getApplicationContext(),
							R.string.js2java_error_languagesetting,
							Toast.LENGTH_LONG
					).show();
				}
				return;
			}
			if (msg.equalsIgnoreCase("setting")) {
				final Intent intent = new Intent(
						me, NicoWnnGControlPanelJAJP.class
				);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				try {
					me.startActivity(intent);
				} catch (final Exception e) {
					Toast.makeText(
							getApplicationContext(),
							R.string.js2java_error_config,
							Toast.LENGTH_LONG
					).show();
				}
				return;
			}
			if (msg.substring(0, 5).equalsIgnoreCase("easy,")) {
				StringTokenizer st = new StringTokenizer(msg, ",");
				final int len = st.countTokens();
				if (len == 5) {
					String s;
					st.nextToken();
					s = st.nextToken();
					final int type = Integer.parseInt(s);
					final String portrait = st.nextToken();
					final String landscape = st.nextToken();
					s = st.nextToken();
					final int size = Integer.parseInt(s);
					if (NicoWnnGJAJP.getInstance() == null) {
						new NicoWnnGJAJP(me);
					}
					switch (type) {
						default:
						case 1:
							NicoWnnGJAJP.getInstance().setEasySetting_Phone(portrait, landscape, size);
							Toast.makeText(
									getApplicationContext(),
									R.string.js2java_accepted_easy_phone,
									Toast.LENGTH_LONG
									).show();
							break;
						case 2:
							NicoWnnGJAJP.getInstance().setEasySetting_Tablet(portrait, landscape, size);
							Toast.makeText(
									getApplicationContext(),
									R.string.js2java_accepted_easy_tablet,
									Toast.LENGTH_LONG
									).show();
							break;
					}
				}
				return;
			}
		}
	}

}