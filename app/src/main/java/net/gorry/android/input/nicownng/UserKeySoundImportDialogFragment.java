/**
 * 
 */
package net.gorry.android.input.nicownng;

import android.content.Context;

import androidx.activity.ComponentActivity;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceDialogFragmentCompat;

import android.os.Bundle;
import android.widget.Toast;

import net.gorry.mydocument.MyDocumentFileSelector;

/**
 * @author gorry
 *
 */

public class UserKeySoundImportDialogFragment extends PreferenceDialogFragmentCompat {

	private static final String sDirName = "nicoWnnG";
	private static final String sFileName = "type.ogg";
	private Context me;

	public static UserKeySoundImportDialogFragment newInstance(String key) {

		UserKeySoundImportDialogFragment dialog = new UserKeySoundImportDialogFragment();
		Bundle b = new Bundle(1);
		b.putString(PreferenceDialogFragmentCompat.ARG_KEY, key);
		dialog.setArguments(b);
		return dialog;

	}

	public UserKeySoundImportDialogFragment() {
	}

	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		me = this.getContext();
	}

	// @Override
	public void onDialogClosed(final boolean positiveResult) {

		if (positiveResult) {
			if (NicoWnnGJAJP.getInstance() == null) {
				new NicoWnnGJAJP(me);
			}

			ActivityNicoWnnGSetting a = ActivityNicoWnnGSetting.getInstance();
			MyDocumentFileSelector selector = a.getMyDocumentFileSelector();
			selector
				.setInitialFile("nicoWnnG/type.ogg")
				.setSelected((resultCode, file) -> {
					if (file != null) {
						try {
							if (NicoWnnGJAJP.getInstance() == null) {
								new NicoWnnGJAJP(me);
							}
							boolean loaded = KeySound.importUserKeySoundFile(a, file);
							if (loaded) {
								Toast.makeText(me, R.string.preference_nicownng_import_userkeysound_success, Toast.LENGTH_LONG).show();
								return;
							}
						} catch (final Exception e) {
							e.printStackTrace();
						}
					}
					Toast.makeText(me, R.string.preference_nicownng_import_userkeysound_failed, Toast.LENGTH_LONG).show();
				})
				.setUnselected((resultCode) -> {
				})
				.setWrite(false)
				.select();
		}

	}

}
