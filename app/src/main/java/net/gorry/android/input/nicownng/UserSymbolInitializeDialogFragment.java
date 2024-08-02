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

import net.gorry.mydocument.MyDocumentFolderSelector;
import net.gorry.mydocument.MyDocumentTreeSelector;

/**
 * @author gorry
 *
 */

public class UserSymbolInitializeDialogFragment extends PreferenceDialogFragmentCompat {

	private static final String sDirName = "nicoWnnG";
	private static final String sFileName = "system.setting";
	private Context me;

	public static UserSymbolInitializeDialogFragment newInstance(String key) {

		UserSymbolInitializeDialogFragment dialog = new UserSymbolInitializeDialogFragment();
		Bundle b = new Bundle(1);
		b.putString(PreferenceDialogFragmentCompat.ARG_KEY, key);
		dialog.setArguments(b);
		return dialog;

	}

	public UserSymbolInitializeDialogFragment() {
	}

	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		me = this.getContext();
	}

	// @Override
	public void onDialogClosed(final boolean positiveResult) {

		if (positiveResult) {
			SymbolList.copyUserSymbolDicResourceToExternalStorageDirectory(me, true);
			if (NicoWnnGJAJP.getInstance() == null) {
				new NicoWnnGJAJP(me);
			}
			boolean loaded = NicoWnnGJAJP.getInstance().reloadSymbol();
			if (loaded) {
				Toast.makeText(me, R.string.preference_nicownng_initialize_usersymbol_success, Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(me, R.string.preference_nicownng_initialize_usersymbol_failed, Toast.LENGTH_LONG).show();
			}
		}
	}
}
