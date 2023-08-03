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

package net.gorry.android.input.nicownng;

import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.Toast;

/**
 * The control panel preference class for Japanese IME.
 *
 * @author Copyright (C) 2009 OMRON SOFTWARE CO., LTD.  All Rights Reserved.
 */
public class NicoWnnGControlPanelJAJP extends PreferenceActivity {
	private final int MENU_RESET_SYMBOL = 0;

	SharedPreferences mPref;
	PreferenceScreen mpsNicoWnnGMenu;
	CheckBoxPreference mcpDifferentPl;
	CheckBoxPreference mcpChangeHardkey;
	ListPreference mlNicoFlickMode;
	ListPreference mlInputMode12key;

	private boolean mDifferent_pl = false;

	private Preference mPreferenceHelp = null;
	private Preference mKeycodeTest = null;
	
	private Menu      mMenu;
	private boolean mIsLandscape;

	private static NicoWnnGControlPanelJAJP mSelf;
	
	public NicoWnnGControlPanelJAJP() {
		mSelf = this;
	}
	
	public static NicoWnnGControlPanelJAJP getInstance() {
		return mSelf;
	}
	
	/** @see android.preference.PreferenceActivity#onCreate */
	@Override public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (NicoWnnGEN.getInstance() == null) {
			new NicoWnnGEN(this);
		}

		if (NicoWnnGJAJP.getInstance() == null) {
			new NicoWnnGJAJP(this);
		}
		NicoWnnGJAJP.getInstance().initializeEasySetting();
		NicoWnnGJAJP.getInstance().convertOldPreferces();

		mPref = PreferenceManager.getDefaultSharedPreferences(this);
		mDifferent_pl = mPref.getBoolean("different_pl", true);

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
		copyInPreferences(mIsLandscape, mPref);

		if (NicoWnnGJAJP.getInstance() == null) {
			new NicoWnnGJAJP(this);
		}

		// 縦横メニューの設定
		addPreferencesFromResource(R.xml.nicownng_pref_ja);
		mpsNicoWnnGMenu = (PreferenceScreen)findPreference("nicownng_menu");
		if (mDifferent_pl) {
			mpsNicoWnnGMenu.setSummary(getString(mIsLandscape ? R.string.preference_nicownng_menu_summary_ja_landscape : R.string.preference_nicownng_menu_summary_ja_portrait));
		} else {
			mpsNicoWnnGMenu.setSummary("");
		}
		mcpDifferentPl = (CheckBoxPreference)findPreference("different_pl");
		mcpDifferentPl.setOnPreferenceChangeListener(onPreferenceChangeListener);
		
		// ヘルプボタン
		mPreferenceHelp = findPreference("nicownng_help");
		mPreferenceHelp.setEnabled(true);
		mPreferenceHelp.setOnPreferenceClickListener(onPreferenceClickListener);

		// キーコードテスト
		mKeycodeTest = findPreference("keycode_test");
		mKeycodeTest.setEnabled(true);
		mKeycodeTest.setOnPreferenceClickListener(onPreferenceClickListener);

		// onhardkey
		mcpChangeHardkey =(CheckBoxPreference)findPreference("change_onhardkey"); 
		setPreferenceEnabled_onhardkey();
		mcpChangeHardkey.setOnPreferenceChangeListener(onPreferenceChangeListener);

		// 12キーのフリックモードの制限
		mlNicoFlickMode = (ListPreference)findPreference("nicoflick_mode");
		mlNicoFlickMode.setOnPreferenceChangeListener(onPreferenceChangeListener);
		mlInputMode12key = (ListPreference)findPreference("input_mode");
		mlInputMode12key.setOnPreferenceChangeListener(onPreferenceChangeListener);
		
	}

	private void setPreferenceEnabled_onhardkey() {
		Preference p;
		boolean sw;

		sw = mcpChangeHardkey.isChecked();
		
		p = findPreference("change_kana_12key_onhardkey");
		p.setEnabled(sw);
		p = findPreference("change_noalpha_qwerty_onhardkey");
		p.setEnabled(sw);
		p = findPreference("change_alphanum_12key_onhardkey");
		p.setEnabled(sw);
		p = findPreference("change_nonumber_qwerty_onhardkey");
		p.setEnabled(sw);
		p = findPreference("change_num_12key_onhardkey");
		p.setEnabled(sw);
		p = findPreference("input_mode_start_onhardkey");
		p.setEnabled(sw);
		p = findPreference("input_mode_next_onhardkey");
		p.setEnabled(sw);
	}

	public Preference.OnPreferenceChangeListener onPreferenceChangeListener = new Preference.OnPreferenceChangeListener() {
		@Override
		public boolean onPreferenceChange(final Preference p, final Object value) {
			if (p == mcpDifferentPl) {
				mDifferent_pl = (Boolean)value;
				if (mDifferent_pl) {
					mpsNicoWnnGMenu.setSummary(getString(mIsLandscape ? R.string.preference_nicownng_menu_summary_ja_landscape : R.string.preference_nicownng_menu_summary_ja_portrait));
				} else {
					mpsNicoWnnGMenu.setSummary("");
				}
			}
			else if (p == mcpChangeHardkey) {
				mcpChangeHardkey.setChecked((Boolean)value);
				setPreferenceEnabled_onhardkey();
			}
			else if (p == mlNicoFlickMode) {
				ListPreference lp = mlInputMode12key;
				String v = lp.getValue();
				if (NicoWnnG.INPUTMODE_2TOUCH.equals(v)) {
					ListPreference lp2 = mlNicoFlickMode; 
					String v2 = (String)value;
					if (!("0".equals(v2))) {
						SharedPreferences.Editor editor = mPref.edit();
						editor.putString("nicoflick_mode", "0");
						editor.commit();
						Toast.makeText(mSelf, mSelf.getString(R.string.toast_2touch_is_not_flickable), Toast.LENGTH_SHORT).show();
						return false;
					}
				}
			}
			else if (p == mlInputMode12key) {
				ListPreference lp = mlInputMode12key;
				String v = (String)value;
				if (NicoWnnG.INPUTMODE_2TOUCH.equals(v)) {
					ListPreference lp2 = mlNicoFlickMode; 
					String v2 = lp2.getValue();
					if (!("0".equals(v2))) {
						SharedPreferences.Editor editor = mPref.edit();
						editor.putString("nicoflick_mode", "0");
						editor.commit();
						lp2.setValueIndex(lp2.findIndexOfValue("0"));
						Toast.makeText(mSelf, mSelf.getString(R.string.toast_2touch_is_not_flickable), Toast.LENGTH_SHORT).show();
					}
				}
			}
			return true;
		}
	};

	public Preference.OnPreferenceClickListener onPreferenceClickListener = new Preference.OnPreferenceClickListener() {
		@Override
		public boolean onPreferenceClick(final Preference p) {
			if (p == mPreferenceHelp) {
				NicoWnnGJAJP.getInstance().openHelp();
				finish();
			}
			else if (p == mKeycodeTest) {
				NicoWnnGJAJP.getInstance().openKeycodeTest();
			}
			return true;
		}
	};

	/*
	 *
	 */
	@Override public void onPause() {
		super.onStop();

		mDifferent_pl = mPref.getBoolean("different_pl", true);
		if (mDifferent_pl) {
			copyOutPreferences(mIsLandscape, mPref);
		} else {
			copyOutPreferences(true, mPref);
			copyOutPreferences(false, mPref);
		}

		final SharedPreferences.Editor editor = mPref.edit();
		editor.putBoolean("request_reboot", false);
		editor.commit();

		if (NicoWnnGJAJP.getInstance() == null) {
			new NicoWnnGJAJP(this);
		}
		// 一度もキーボードを表示させずに以下を呼ぶとエラーになる
		// NicoWnnGJAJP.getInstance().mInputViewManager.closing();
		// NicoWnnGJAJP.getInstance().requestHideSelf(0);

		NicoWnnGJAJP.getInstance().reloadFlags();
	}

	/*
	 *
	 */
	/*
	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		// initialize the menu
		menu.clear();
		// [menu] add a word
		menu.add(0, MENU_RESET_SYMBOL, 0, R.string.preference_nicownng_menu_reset_symbol);
		mMenu = menu;
		return super.onCreateOptionsMenu(menu);
	}
	*/
	/*
	 *
	 */
	/*
	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		boolean ret;
		switch (item.getItemId()) {
			case MENU_RESET_SYMBOL:
				if (NicoWnnGJAJP.getInstance() == null) {
					new NicoWnnGJAJP(this);
				}
				NicoWnnGJAJP.getInstance().resetSymbol();
				Toast.makeText(getApplicationContext(), R.string.preference_nicownng_menu_reset_symbol_success, Toast.LENGTH_LONG).show();

				ret = true;
				break;
			default:
				ret = false;
		}
		return ret;
	}
	*/

	public void copyInPreferences(final boolean sw, final SharedPreferences pref) {
		final SharedPreferences.Editor editor = pref.edit();

		copyInPreferenceBoolean(sw, pref, editor, "auto_caps");
		copyInPreferenceBoolean(sw, pref, editor, "autoforward_toggle_12key");
		copyInPreferenceBoolean(sw, pref, editor, "can_flick_arrow_key");
		copyInPreferenceBoolean(sw, pref, editor, "can_flick_mode_key");
		copyInPreferenceString(sw, pref, editor, "candidate_font_size");
		copyInPreferenceBoolean(sw, pref, editor, "candidate_leftrightkey");
		copyInPreferenceString(sw, pref, editor, "candidateview_height_mode2");
		copyInPreferenceBoolean(sw, pref, editor, "change_alphamode");
		copyInPreferenceBoolean(sw, pref, editor, "change_alphanum_12key");
		copyInPreferenceBoolean(sw, pref, editor, "change_alphanum_12key_onhardkey");
		copyInPreferenceBoolean(sw, pref, editor, "change_kana_12key");
		copyInPreferenceBoolean(sw, pref, editor, "change_kana_12key_onhardkey");
		copyInPreferenceBoolean(sw, pref, editor, "change_noalpha_qwerty");
		copyInPreferenceBoolean(sw, pref, editor, "change_noalpha_qwerty_onhardkey");
		copyInPreferenceBoolean(sw, pref, editor, "change_nonumber_qwerty");
		copyInPreferenceBoolean(sw, pref, editor, "change_nonumber_qwerty_onhardkey");
		copyInPreferenceBoolean(sw, pref, editor, "change_num_12key");
		copyInPreferenceBoolean(sw, pref, editor, "change_num_12key_onhardkey");
		copyInPreferenceString(sw, pref, editor, "convert_keymap_type");
		copyInPreferenceString(sw, pref, editor, "cutpasteaction_byime");
		copyInPreferenceBoolean(sw, pref, editor, "english_predict_12key");
		copyInPreferenceBoolean(sw, pref, editor, "english_predict_qwerty");
		copyInPreferenceBoolean(sw, pref, editor, "flick_guide");
		copyInPreferenceString(sw, pref, editor, "flick_sensitivity_mode");
		copyInPreferenceString(sw, pref, editor, "hidden_softkeyboard4");
		copyInPreferenceString(sw, pref, editor, "input_mode");
		copyInPreferenceString(sw, pref, editor, "input_mode_next");
		copyInPreferenceString(sw, pref, editor, "input_mode_next_onhardkey");
		copyInPreferenceString(sw, pref, editor, "input_mode_start");
		copyInPreferenceString(sw, pref, editor, "input_mode_start_onhardkey");
		copyInPreferenceBoolean(sw, pref, editor, "is_skip_space");
		copyInPreferenceBoolean(sw, pref, editor, "kana12_space_zen");
		copyInPreferenceString(sw, pref, editor, "mainview_height_mode2");
		copyInPreferenceString(sw, pref, editor, "nico_candidate_lines");
		copyInPreferenceBoolean(sw, pref, editor, "nico_candidate_vertical");
		copyInPreferenceString(sw, pref, editor, "nicoflick_mode");
		copyInPreferenceBoolean(sw, pref, editor, "nospace_candidate2");
		copyInPreferenceBoolean(sw, pref, editor, "no_flip_screen");
		copyInPreferenceString(sw, pref, editor, "qwerty_kana_mode3");
		copyInPreferenceBoolean(sw, pref, editor, "qwerty_matrix_mode");
		copyInPreferenceBoolean(sw, pref, editor, "qwerty_space_zen");
		copyInPreferenceBoolean(sw, pref, editor, "qwerty_swap_minienter");
		copyInPreferenceBoolean(sw, pref, editor, "qwerty_swap_shift_alt");
		copyInPreferenceString(sw, pref, editor, "shiftkey_style");
		copyInPreferenceBoolean(sw, pref, editor, "show_candidate_fulllist_button");
		copyInPreferenceBoolean(sw, pref, editor, "shrink_candidate_lines");
		copyInPreferenceBoolean(sw, pref, editor, "space_below_keyboard");
		copyInPreferenceString(sw, pref, editor, "subten_12key_mode2");
		copyInPreferenceString(sw, pref, editor, "subten_qwerty_mode");
		copyInPreferenceBoolean(sw, pref, editor, "symbol_addsymbolemoji_12key");
		copyInPreferenceBoolean(sw, pref, editor, "symbol_addsymbolemoji_qwerty");
		copyInPreferenceBoolean(sw, pref, editor, "tsu_du_ltu");
		copyInPreferenceBoolean(sw, pref, editor, "use_12key_shift");
		copyInPreferenceBoolean(sw, pref, editor, "use_12key_subten");
		copyInPreferenceBoolean(sw, pref, editor, "use_email_kana");
		copyInPreferenceBoolean(sw, pref, editor, "use_qwerty_subten");
		copyInPreferenceBoolean(sw, pref, editor, "use_zenkaku_to_moji");

		editor.commit();
	}

	public void copyOutPreferences(final boolean sw, final SharedPreferences pref) {
		final SharedPreferences.Editor editor = pref.edit();

		copyOutPreferenceBoolean(sw, pref, editor, "auto_caps");
		copyOutPreferenceBoolean(sw, pref, editor, "autoforward_toggle_12key");
		copyOutPreferenceBoolean(sw, pref, editor, "can_flick_arrow_key");
		copyOutPreferenceBoolean(sw, pref, editor, "can_flick_mode_key");
		copyOutPreferenceString(sw, pref, editor, "candidate_font_size");
		copyOutPreferenceBoolean(sw, pref, editor, "candidate_leftrightkey");
		copyOutPreferenceString(sw, pref, editor, "candidateview_height_mode2");
		copyOutPreferenceBoolean(sw, pref, editor, "change_alphamode");
		copyOutPreferenceBoolean(sw, pref, editor, "change_alphanum_12key");
		copyOutPreferenceBoolean(sw, pref, editor, "change_alphanum_12key_onhardkey");
		copyOutPreferenceBoolean(sw, pref, editor, "change_kana_12key");
		copyOutPreferenceBoolean(sw, pref, editor, "change_kana_12key_onhardkey");
		copyOutPreferenceBoolean(sw, pref, editor, "change_noalpha_qwerty");
		copyOutPreferenceBoolean(sw, pref, editor, "change_noalpha_qwerty_onhardkey");
		copyOutPreferenceBoolean(sw, pref, editor, "change_nonumber_qwerty");
		copyOutPreferenceBoolean(sw, pref, editor, "change_nonumber_qwerty_onhardkey");
		copyOutPreferenceBoolean(sw, pref, editor, "change_num_12key");
		copyOutPreferenceBoolean(sw, pref, editor, "change_num_12key_onhardkey");
		copyOutPreferenceString(sw, pref, editor, "convert_keymap_type");
		copyOutPreferenceString(sw, pref, editor, "cutpasteaction_byime");
		copyOutPreferenceBoolean(sw, pref, editor, "english_predict_12key");
		copyOutPreferenceBoolean(sw, pref, editor, "english_predict_qwerty");
		copyOutPreferenceBoolean(sw, pref, editor, "flick_guide");
		copyOutPreferenceString(sw, pref, editor, "flick_sensitivity_mode");
		copyOutPreferenceString(sw, pref, editor, "hidden_softkeyboard4");
		copyOutPreferenceString(sw, pref, editor, "input_mode");
		copyOutPreferenceString(sw, pref, editor, "input_mode_next");
		copyOutPreferenceString(sw, pref, editor, "input_mode_next_onhardkey");
		copyOutPreferenceString(sw, pref, editor, "input_mode_start");
		copyOutPreferenceString(sw, pref, editor, "input_mode_start_onhardkey");
		copyOutPreferenceBoolean(sw, pref, editor, "is_skip_space");
		copyOutPreferenceBoolean(sw, pref, editor, "kana12_space_zen");
		copyOutPreferenceString(sw, pref, editor, "mainview_height_mode2");
		copyOutPreferenceString(sw, pref, editor, "nico_candidate_lines");
		copyOutPreferenceBoolean(sw, pref, editor, "nico_candidate_vertical");
		copyOutPreferenceString(sw, pref, editor, "nicoflick_mode");
		copyOutPreferenceBoolean(sw, pref, editor, "nospace_candidate2");
		copyOutPreferenceBoolean(sw, pref, editor, "no_flip_screen");
		copyOutPreferenceString(sw, pref, editor, "qwerty_kana_mode3");
		copyOutPreferenceBoolean(sw, pref, editor, "qwerty_matrix_mode");
		copyOutPreferenceBoolean(sw, pref, editor, "qwerty_space_zen");
		copyOutPreferenceBoolean(sw, pref, editor, "qwerty_swap_minienter");
		copyOutPreferenceBoolean(sw, pref, editor, "qwerty_swap_shift_alt");
		copyOutPreferenceString(sw, pref, editor, "shiftkey_style");
		copyOutPreferenceBoolean(sw, pref, editor, "show_candidate_fulllist_button");
		copyOutPreferenceBoolean(sw, pref, editor, "shrink_candidate_lines");
		copyOutPreferenceBoolean(sw, pref, editor, "space_below_keyboard");
		copyOutPreferenceString(sw, pref, editor, "subten_12key_mode2");
		copyOutPreferenceString(sw, pref, editor, "subten_qwerty_mode");
		copyOutPreferenceBoolean(sw, pref, editor, "symbol_addsymbolemoji_12key");
		copyOutPreferenceBoolean(sw, pref, editor, "symbol_addsymbolemoji_qwerty");
		copyOutPreferenceBoolean(sw, pref, editor, "tsu_du_ltu");
		copyOutPreferenceBoolean(sw, pref, editor, "use_12key_shift");
		copyOutPreferenceBoolean(sw, pref, editor, "use_12key_subten");
		copyOutPreferenceBoolean(sw, pref, editor, "use_email_kana");
		copyOutPreferenceBoolean(sw, pref, editor, "use_qwerty_subten");
		copyOutPreferenceBoolean(sw, pref, editor, "use_zenkaku_to_moji");

		editor.commit();
	}

	private void copyInPreferenceString(final boolean sw, final SharedPreferences pref, final SharedPreferences.Editor editor, final String key ) {
		final String inKey = key + (sw ? "_landscape" : "_portrait");
		if (pref.contains(inKey)) {
			final String value = pref.getString(inKey, "");
			editor.putString(key, value);
		} else {
			editor.remove(key);
		}
	}

	private void copyInPreferenceInteger(final boolean sw, final SharedPreferences pref, final SharedPreferences.Editor editor, final String key ) {
		final String inKey = key + (sw ? "_landscape" : "_portrait");
		if (pref.contains(inKey)) {
			final int value = pref.getInt(inKey, 0);
			editor.putInt(key, value);
		} else {
			editor.remove(key);
		}
	}

	private void copyInPreferenceBoolean(final boolean sw, final SharedPreferences pref, final SharedPreferences.Editor editor, final String key ) {
		final String inKey = key + (sw ? "_landscape" : "_portrait");
		if (pref.contains(inKey)) {
			final Boolean value = pref.getBoolean(inKey, false);
			editor.putBoolean(key, value);
		} else {
			editor.remove(key);
		}
	}


	private void copyOutPreferenceString(final boolean sw, final SharedPreferences pref, final SharedPreferences.Editor editor, final String key ) {
		final String outKey = key + (sw ? "_landscape" : "_portrait");
		if (pref.contains(key)) {
			final String value = pref.getString(key, "");
			editor.putString(outKey, value);
		}
	}

	private void copyOutPreferenceInteger(final boolean sw, final SharedPreferences pref, final SharedPreferences.Editor editor, final String key ) {
		final String outKey = key + (sw ? "_landscape" : "_portrait");
		if (pref.contains(key)) {
			final int value = pref.getInt(key, 0);
			editor.putInt(outKey, value);
		}
	}

	private void copyOutPreferenceBoolean(final boolean sw, final SharedPreferences pref, final SharedPreferences.Editor editor, final String key ) {
		final String outKey = key + (sw ? "_landscape" : "_portrait");
		if (pref.contains(key)) {
			final boolean value = pref.getBoolean(key, false);
			editor.putBoolean(outKey, value);
		}
	}
	
	public boolean isLandscape() {
		return mIsLandscape;
	}
}
