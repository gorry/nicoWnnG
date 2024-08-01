/**
 * 
 */
package net.gorry.android.input.nicownng;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.util.Consumer;
import androidx.documentfile.provider.DocumentFile;
import androidx.preference.PreferenceDialogFragmentCompat;

import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import java.util.function.BiConsumer;

/**
 * @author gorry
 *
 */

public class MyDocumentFolderSelector {

	private String mInitialDirectory = null;
	private Context me;
	private boolean mWrite = false;
	private boolean mCreateFolder = false;
	private BiConsumer<Integer, DocumentFile> mSelected = null;
	private Consumer<Integer> mUnselected = null;
	private MyDocumentTreeSelector mDocumentTreeSelector;

	public MyDocumentFolderSelector(Context c) {
		me = c;
	}

	public MyDocumentFolderSelector setInitialDirectory(String s) {
		mInitialDirectory = s;
		return this;
	}

	public MyDocumentFolderSelector setDocumentTreeSelector(MyDocumentTreeSelector s) {
		mDocumentTreeSelector = s;
		return this;
	}

	public MyDocumentFolderSelector setWrite(boolean b) {
		mWrite = b;
		return this;
	}

	public MyDocumentFolderSelector setCreateFolder(boolean b) {
		mCreateFolder = b;
		return this;
	}

	public MyDocumentFolderSelector setSelected(BiConsumer<Integer, DocumentFile> c) {
		mSelected = c;
		return this;
	}

	public MyDocumentFolderSelector setUnselected(Consumer<Integer> c)  {
		mUnselected = c;
		return this;
	}

	public void select() {
		if (mDocumentTreeSelector == null) {
			return;
		}
		if (mInitialDirectory == null) {
			return;
		}
		mDocumentTreeSelector.setInitialDirectory(mInitialDirectory)
			.setSelected((resultCode, treeUri) -> {
				Uri path = treeUri;
				DocumentFile dir = DocumentFile.fromTreeUri(me, path);

				if (!path.toString().endsWith(mInitialDirectory)) {
					// 選択したフォルダが「mInitialDirectory」でないとき
					DocumentFile dir2 = dir.findFile(mInitialDirectory);
					if ((dir2 != null) && dir2.exists()) {
						// 選択したフォルダに「mInitialDirectory」があるとき
						if (!dir2.isDirectory()) {
							// 「mInitialDirectory」がフォルダでないとき
							dir2 = null;
						}
						// 選択したフォルダにある「mInitialDirectory」を新しい選択フォルダにする
						dir = dir2;
					} else {
						// 選択したフォルダに「mInitialDirectory」がないとき
						if (!mCreateFolder) {
							dir2 = null;
						} else {
							dir2 = dir.createDirectory(mInitialDirectory);
							if (dir2 == null) {
								// 選択したフォルダに「mInitialDirectory」が作れないとき
								dir2 = null;
							}
						}
						// 作成した「mInitialDirectory」フォルダを新しい選択フォルダにする
						dir = dir2;
					}
				}

				if (mSelected != null) {
					mSelected.accept(resultCode, dir);
				}
			})
			.setUnselected((resultCode) -> {
				if (mUnselected != null) {
					mUnselected.accept(resultCode);
				}
			})
			.setWrite(true)
			.select();
	}
}
