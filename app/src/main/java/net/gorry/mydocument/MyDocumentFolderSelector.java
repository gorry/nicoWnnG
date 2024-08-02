/**
 * 
 */
package net.gorry.mydocument;

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
import android.util.Log;
import android.widget.Toast;

import java.util.function.BiConsumer;

/**
 * @author gorry
 *
 */

public class MyDocumentFolderSelector {
	private static final boolean RELEASE = true;//!BuildConfig.DEBUG;
	private static final String TAG = "ActivityMain";
	private static final boolean T = !RELEASE;
	private static final boolean V = !RELEASE;
	private static final boolean D = !RELEASE;
	private static final boolean I = true;

	private static String M() {
		StackTraceElement[] es = new Exception().getStackTrace();
		int count = 1; while (es[count].getMethodName().contains("$")) count++;
		return es[count].getFileName()+"("+es[count].getLineNumber()+"): "+es[count].getMethodName()+"(): ";
	}


	private String mInitialDirectory = null;
	private Context me;
	private boolean mWrite = false;
	private boolean mCreateFolder = false;
	private BiConsumer<Integer, DocumentFile> mSelected = null;
	private Consumer<Integer> mUnselected = null;
	private MyDocumentTreeSelector mDocumentTreeSelector;

	public MyDocumentFolderSelector(Context c) {
		if (T) Log.v(TAG, M()+"@in: c="+c);

		me = c;

		if (T) Log.v(TAG, M()+"@out");
	}

	public MyDocumentFolderSelector setInitialDirectory(String s) {
		if (T) Log.v(TAG, M()+"@in: s="+s);

		mInitialDirectory = s;

		if (T) Log.v(TAG, M()+"@out");
		return this;
	}

	public MyDocumentFolderSelector setDocumentTreeSelector(MyDocumentTreeSelector s) {
		if (T) Log.v(TAG, M()+"@in: s="+s);

		mDocumentTreeSelector = s;

		if (T) Log.v(TAG, M()+"@out");
		return this;
	}

	public MyDocumentFolderSelector setWrite(boolean b) {
		if (T) Log.v(TAG, M()+"@in: b="+b);

		mWrite = b;

		if (T) Log.v(TAG, M()+"@out");
		return this;
	}

	public MyDocumentFolderSelector setCreateFolder(boolean b) {
		if (T) Log.v(TAG, M()+"@in: b="+b);

		mCreateFolder = b;

		if (T) Log.v(TAG, M()+"@out: ret="+this);
		return this;
	}

	public MyDocumentFolderSelector setSelected(BiConsumer<Integer, DocumentFile> c) {
		if (T) Log.v(TAG, M()+"@in: c="+c);

		mSelected = c;

		if (T) Log.v(TAG, M()+"@out: ret="+this);
		return this;
	}

	public MyDocumentFolderSelector setUnselected(Consumer<Integer> c)  {
		if (T) Log.v(TAG, M()+"@in: c="+c);

		mUnselected = c;

		if (T) Log.v(TAG, M()+"@out: ret="+this);
		return this;
	}

	public void select() {
		if (T) Log.v(TAG, M()+"@in");

		if (mDocumentTreeSelector == null) {
			return;
		}
		if (mInitialDirectory == null) {
			return;
		}
		mDocumentTreeSelector.setInitialDirectory(mInitialDirectory)
			.setSelected((resultCode, treeUri) -> {
				if (T) Log.v(TAG, M()+"@in: resultCode="+resultCode+", treeUri="+treeUri);

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

				if (T) Log.v(TAG, M()+"@out");
			})
			.setUnselected((resultCode) -> {
				if (T) Log.v(TAG, M()+"@in: resultCode="+resultCode);

				if (mUnselected != null) {
					mUnselected.accept(resultCode);
				}

				if (T) Log.v(TAG, M()+"@out");
			})
			.setWrite(true)
			.select();
		if (T) Log.v(TAG, M()+"@out");
	}
}
