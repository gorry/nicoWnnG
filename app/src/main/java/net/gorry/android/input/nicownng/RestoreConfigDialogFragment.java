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

/**
 * @author gorry
 *
 */

public class RestoreConfigDialogFragment extends PreferenceDialogFragmentCompat {

	private static final String sDirName = "nicoWnnG";
	private static final String sFileName = "system.setting";
	private Context me;

	public static RestoreConfigDialogFragment newInstance(String key) {

		RestoreConfigDialogFragment dialog = new RestoreConfigDialogFragment();
		Bundle b = new Bundle(1);
		b.putString(PreferenceDialogFragmentCompat.ARG_KEY, key);
		dialog.setArguments(b);
		return dialog;

	}

	public RestoreConfigDialogFragment() {
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

			MyDocumentTreeSelector doctree = ActivityNicoWnnGSetting.getInstance().getMyDocumentTreeSelector();
			doctree.setDirectory(sDirName)
				.setSelected((resultCode, treeUri) -> {
					Uri path = treeUri;
					DocumentFile dir = DocumentFile.fromTreeUri(me, path);

					if (!path.toString().endsWith(sDirName)) {
						// 選択したフォルダが「nicoWnnG」でないとき
						DocumentFile dir2 = dir.findFile(sDirName);
						if ((dir2 != null) && dir2.exists()) {
							// 選択したフォルダに「nicoWnnG」があるとき
							if (!dir2.isDirectory()) {
								// 「nicoWnnG」がフォルダでないとき
								Toast.makeText(
										me.getApplicationContext(),
										R.string.toast_config_restore_failed,
										Toast.LENGTH_LONG
								).show();
								return;
							}
							// 選択したフォルダにある「nicoWnnG」を新しい選択フォルダにする
							dir = dir2;
						} else {
							// 選択したフォルダに「nicoWnnG」がないとき
							// 現在のフォルダをそのまま使う
						}
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
				.select();
		}

	}

}
