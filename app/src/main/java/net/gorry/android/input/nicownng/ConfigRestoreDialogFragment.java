/**
 * 
 */
package net.gorry.android.input.nicownng;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.Nullable;
import androidx.documentfile.provider.DocumentFile;
import androidx.preference.PreferenceDialogFragmentCompat;

import android.os.Bundle;
import android.widget.Toast;

import net.gorry.mydocument.MyDocumentFolderSelector;
import net.gorry.mydocument.MyDocumentTreeSelector;

/**
 * @author gorry
 *
 */

public class ConfigRestoreDialogFragment extends PreferenceDialogFragmentCompat {

	private static final String sDirName = "nicoWnnG";
	private static final String sFileName = "system.setting";
	private Context me;

	public static ConfigRestoreDialogFragment newInstance(String key) {

		ConfigRestoreDialogFragment dialog = new ConfigRestoreDialogFragment();
		Bundle b = new Bundle(1);
		b.putString(PreferenceDialogFragmentCompat.ARG_KEY, key);
		dialog.setArguments(b);
		return dialog;

	}

	public ConfigRestoreDialogFragment() {
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

			MyDocumentFolderSelector folder = new MyDocumentFolderSelector(me);
			MyDocumentTreeSelector doctree = ActivityNicoWnnGSetting.getInstance().getMyDocumentTreeSelector();
			folder.setDocumentTreeSelector(doctree)
				.setInitialDirectory(sDirName)
				.setSelected((resultCode, dir) -> {
					if (dir == null) {
						Toast.makeText(
								me.getApplicationContext(),
								R.string.toast_config_backup_failed,
								Toast.LENGTH_LONG
						).show();
						return;
					}

					// 指定したフォルダにファイルがあるかどうか調べる
					DocumentFile file = dir.findFile(sFileName);
					if (file == null) {
						// なければ失敗
						Toast.makeText(
								me.getApplicationContext(),
								R.string.toast_config_restore_failed,
								Toast.LENGTH_LONG
						).show();
						return;
					}
					Uri filename = file.getUri();

					try {
						ConfigBackup p = new ConfigBackup();
						p.Restore(me, filename);
						Toast.makeText(
								me.getApplicationContext(),
								R.string.toast_config_restore_success,
								Toast.LENGTH_LONG
						).show();
					} catch (final Exception e) {
						Toast.makeText(
								me.getApplicationContext(),
								R.string.toast_config_restore_failed,
								Toast.LENGTH_LONG
						).show();
					}
				})
				.setUnselected((resultCode) -> {
				})
				.setWrite(false)
				.setCreateFolder(true)
				.select();
		}

	}

}
