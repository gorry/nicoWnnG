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

public class UserSymbolImportDialogFragment extends PreferenceDialogFragmentCompat {

	private static final String sDirName = "nicoWnnG";
	private static final String sFileName = "system.setting";
	private Context me;

	public static UserSymbolImportDialogFragment newInstance(String key) {

		UserSymbolImportDialogFragment dialog = new UserSymbolImportDialogFragment();
		Bundle b = new Bundle(1);
		b.putString(PreferenceDialogFragmentCompat.ARG_KEY, key);
		dialog.setArguments(b);
		return dialog;

	}

	public UserSymbolImportDialogFragment() {
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
			MyDocumentFolderSelector folder = new MyDocumentFolderSelector(me);
			MyDocumentTreeSelector doctree = a.getMyDocumentTreeSelector();
			folder.setDocumentTreeSelector(doctree)
				.setInitialDirectory(sDirName)
				.setSelected((resultCode, dir) -> {
					if (dir != null) {
						try {
							SymbolList.importUserSymbolDicFile(a, dir.getUri());
							SymbolList.copyUserSymbolDicResourceToExternalStorageDirectory(me, false);
							if (NicoWnnGJAJP.getInstance() == null) {
								new NicoWnnGJAJP(me);
							}
							boolean loaded = NicoWnnGJAJP.getInstance().reloadSymbol();
							if (loaded) {
								Toast.makeText(me, R.string.preference_nicownng_import_usersymbol_success, Toast.LENGTH_LONG).show();
								return;
							}
						} catch (final Exception e) {
							e.printStackTrace();
						}
					}
					Toast.makeText(me, R.string.preference_nicownng_import_usersymbol_failed, Toast.LENGTH_LONG).show();
				})
				.setUnselected((resultCode) -> {
				})
				.setWrite(false)
				.setCreateFolder(true)
				.select();
		}

	}

}
