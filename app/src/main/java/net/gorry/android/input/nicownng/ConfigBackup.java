/**
 * 
 */
package net.gorry.android.input.nicownng;

import android.content.Context;

/**
 * @author gorry
 *
 */
public class ConfigBackup {
	private static final String TAG = "MySharedPreferences";
	private static final boolean V = false;
	private static final boolean D = false;
	private static final boolean I = false;

	private MySharedPreferences mPrefFrom;
	private MySharedPreferences mPrefTo;
	private MySharedPreferences.Editor mEditor;

	private boolean copyElementBoolean(final String name, final boolean defparam, final boolean different_pl) {
		if (different_pl) {
			String name2;
			name2 = name + "_landscape";
			copyElementBoolean(name2, defparam, false);
			name2 = name + "_portrait";
			copyElementBoolean(name2, defparam, false);
		}

		Boolean tmp;
		tmp = mPrefFrom.getBoolean(name, defparam);
		mEditor.putBoolean(name, tmp);

		return true;
	}

	private boolean copyElementString(final String name, final String defparam, final boolean different_pl) {
		if (different_pl) {
			String name2;
			name2 = name + "_landscape";
			copyElementString(name2, defparam, false);
			name2 = name + "_portrait";
			copyElementString(name2, defparam, false);
		}

		String tmp;
		tmp = mPrefFrom.getString(name, defparam);
		mEditor.putString(name, tmp);

		return true;
	}

	private boolean copyCore(Context context) {
		copyElementBoolean("auto_caps", false, true);
		copyElementBoolean("autoforward_toggle_12key", true, true);
		copyElementBoolean("can_flick_arrow_key", true, true);
		copyElementBoolean("can_flick_mode_key", true, true);
		copyElementString ("candidate_font_size", "2", true);
		copyElementBoolean("candidate_leftrightkey", true, true);
		copyElementString ("candidateview_height_mode2", "2", true);
		copyElementBoolean("change_alphamode", false, true);
		copyElementBoolean("change_alphanum_12key", false, true);
		copyElementBoolean("change_alphanum_12key_onhardkey", false, true);
		copyElementBoolean("change_kana_12key", false, true);
		copyElementBoolean("change_kana_12key_onhardkey", false, true);
		copyElementBoolean("change_noalpha_qwerty", false, true);
		copyElementBoolean("change_noalpha_qwerty_onhardkey", false, true);
		copyElementBoolean("change_nonumber_qwerty", false, true);
		copyElementBoolean("change_nonumber_qwerty_onhardkey", false, true);
		copyElementBoolean("change_num_12key", false, true);
		copyElementBoolean("change_num_12key_onhardkey", false, true);
		copyElementBoolean("change_onhardkey", false, false);
		copyElementString ("convert_keymap_type", "0", true);
		copyElementString ("cutpasteaction_byime", "3", true);
		copyElementBoolean("different_pl", true, false);
		copyElementString ("emoji_type", "1", true);
		copyElementBoolean("english_predict_12key", true, true);
		copyElementBoolean("english_predict_qwerty", false, true);
		copyElementBoolean("flick_guide", true, true);
		copyElementString ("flick_sensitivity_mode", "2", true);
		copyElementBoolean("fullscreen", false, false);
		copyElementString ("hidden_softkeyboard4", "3", true);
		copyElementString ("input_mode", "input_normal", true);
		copyElementString ("input_mode_next", "0", true);
		copyElementString ("input_mode_next_onhardkey", "0", true);
		copyElementString ("input_mode_start", "0", true);
		copyElementString ("input_mode_start_onhardkey", "0", true);
		copyElementBoolean("is_skip_space", true, true);
		copyElementBoolean("kana12_space_zen", false, true);
		copyElementBoolean("key_sound", false, false);
		copyElementString ("key_sound_vol", "0", false);
		copyElementBoolean("key_vibration", false, false);
		copyElementString ("keyboard_skin", context.getResources().getString(R.string.keyboard_skin_id_default), false);
		copyElementString ("mainview_height_mode2", "0", true);
		copyElementBoolean("new_preference_20110417a", true, false);
		copyElementBoolean("new_preference_20120209a", true, false);
		copyElementBoolean("new_preference_20120813a", true, false);
		copyElementBoolean("new_preference_20121021a", true, false);
		copyElementBoolean("new_preference_20121107a", true, false);
		copyElementString ("nico_candidate_lines", "1", false);
		copyElementString ("nico_candidate_lines_landscape", "1", false);
		copyElementString ("nico_candidate_lines_portrait", "3", false);
		copyElementBoolean("nico_candidate_vertical", true, true);
		copyElementString ("nicoflick_mode", "0", true);
		copyElementBoolean("nospace_candidate2", false, true);
		copyElementBoolean("no_flip_screen", false, true);
		copyElementBoolean("opt_enable_learning", true, false);
		copyElementBoolean("opt_prediction_afterenter", true, false);
		copyElementString ("opt_prediction_mode2", "0", false);
		copyElementBoolean("opt_spell_correction", true, false);
		copyElementBoolean("popup_preview", true, false);
		copyElementString ("qwerty_kana_mode3", "0", true);
		copyElementBoolean("qwerty_matrix_mode", false, true);
		copyElementBoolean("qwerty_space_zen", true, true);
		copyElementBoolean("qwerty_swap_minienter", false, true);
		copyElementBoolean("qwerty_swap_shift_alt", true, true);
		copyElementString ("shiftkey_style", "0", true);
		copyElementBoolean("show_candidate_fulllist_button", true, true);
		copyElementBoolean("shrink_candidate_lines", false, true);
		copyElementBoolean("space_below_keyboard", false, true);
		copyElementString ("subten_12key_mode2", "1", true);
		copyElementString ("subten_qwerty_mode", "0", true);
		copyElementBoolean("symbol_addsymbolemoji_12key", true, true);
		copyElementBoolean("symbol_addsymbolemoji_qwerty", true, true);
		copyElementBoolean("tsu_du_ltu", false, true);
		copyElementBoolean("use_12key_shift", true, true);
		copyElementBoolean("use_12key_subten", false, true);
		copyElementBoolean("use_email_kana", false, true);
		copyElementBoolean("use_qwerty_subten", false, true);
		copyElementBoolean("use_zenkaku_to_moji", false, true);

		return (true);
	}
	
	/**
	 * 設定の書き出し
	 * @param context Context
	 * @param filename 書き出し先
	 * @return 成功ならtrue
	 */
	public boolean Backup(Context context, String filename) {
		mPrefFrom = new MySharedPreferences(context, null);
		mPrefTo = new MySharedPreferences(context, filename);
		mEditor = mPrefTo.edit();
		copyCore(context);
		mEditor.commit();

		return true;
	}

	/**
	 * 設定の読み込み
	 * @param context Context
	 * @param filename 読み込み先
	 * @return 成功ならtrue
	 */
	public boolean Restore(Context context, String filename) {
		mPrefFrom = new MySharedPreferences(context, filename);
		mPrefTo = new MySharedPreferences(context, null);
		mEditor = mPrefTo.edit();
		copyCore(context);
		mEditor.commit();

		return true;
	}

}


// [EOF]
