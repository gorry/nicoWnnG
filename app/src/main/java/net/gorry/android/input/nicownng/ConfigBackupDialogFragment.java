/**
 * 
 */
package net.gorry.android.input.nicownng;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.documentfile.provider.DocumentFile;
import androidx.preference.PreferenceDialogFragmentCompat;

import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import net.gorry.mydocument.MyDocumentFolderSelector;
import net.gorry.mydocument.MyDocumentTreeSelector;

/**
 * @author gorry
 *
 */

public class ConfigBackupDialogFragment extends PreferenceDialogFragmentCompat {

	private static final String sDirName = "nicoWnnG";
	private static final String sFileName = "system.setting";
	private Context me;

	public static ConfigBackupDialogFragment newInstance(String key) {

		ConfigBackupDialogFragment dialog = new ConfigBackupDialogFragment();
		Bundle b = new Bundle(1);
		b.putString(PreferenceDialogFragmentCompat.ARG_KEY, key);
		dialog.setArguments(b);
		return dialog;

	}

	public ConfigBackupDialogFragment() {
	}

	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		me = this.getContext();
	}

	public void onPrepareDialogBuilder(@NonNull AlertDialog.Builder builder) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
			builder.setMessage(R.string.dialog_config_backup_message_api28);
		}
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
						// なければ新規作成する
						file = dir.createFile("application/octet-stream", sFileName);
						if (file == null) {
							// 作れなければ失敗
							Toast.makeText(
									me.getApplicationContext(),
									R.string.toast_config_backup_failed,
									Toast.LENGTH_LONG
							).show();
							return;
						}
					}
					Uri filename = file.getUri();

					try {
						ConfigBackup p = new ConfigBackup();
						p.Backup(me, filename);
						Toast.makeText(
								me.getApplicationContext(),
								R.string.toast_config_backup_success,
								Toast.LENGTH_LONG
						).show();
					} catch (final Exception e) {
						Toast.makeText(
								me.getApplicationContext(),
								R.string.toast_config_backup_failed,
								Toast.LENGTH_LONG
						).show();
					}
				})
				.setUnselected((resultCode) -> {
				})
				.setWrite(true)
				.setCreateFolder(true)
				.select();
		}

	}

}
