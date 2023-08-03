/**
 *
 */
package net.gorry.android.input.nicownng;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Comparator;

import android.app.ListActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

/**
 * 
 * ファイルリストの取得
 * 
 * @author gorry
 *
 */
public class SelectTxtFileActivity extends ListActivity {
	private static final String TAG = "SelectTxtFileActivity";
	private static final boolean V = false;//true;

	private String mCurrentFolderName = "";
	private File mCurDir;
	private File[] mDirEntry;
	private SelectTxtFileAdapter mAdapter;
	private String mExtFilenameFilter;
	// private boolean mSelected;
	private String mCurrentFileName;
	private String mLastFolderName = "";

	/* アプリの一時退避
	 * @see android.app.Activity#onSaveInstanceState(android.os.Bundle)
	 */
	@Override
	protected void onSaveInstanceState(final Bundle outState) {
		if (V) Log.v(TAG, "onSaveInstanceState()");
		outState.putString("mCurrentFolderName", mCurrentFolderName);
		outState.putString("mCurrentFileName", mCurrentFileName);
		outState.putString("mLastFolderName", mLastFolderName);
	}

	/*
	 * アプリの一時退避復元
	 * @see android.app.Activity#onRestoreInstanceState(android.os.Bundle)
	 */
	@Override
	protected void onRestoreInstanceState(final Bundle savedInstanceState) {
		if (V) Log.v(TAG, "onRestoreInstanceState()");
		mCurrentFolderName = savedInstanceState.getString("mCurrentFolderName");
		mCurrentFileName = savedInstanceState.getString("mCurrentFileName");
		mLastFolderName = savedInstanceState.getString("mLastFolderName");
	}

	/**
	 * @param ext 拡張子
	 * @return 拡張子がマッチしたらtrue
	 */
	public FilenameFilter extNameFilter(final String ext) {
		mExtFilenameFilter = new String(ext.toLowerCase());
		return new FilenameFilter() {
			public boolean accept(final File dir, final String name) {
				final File file = new File(dir.getPath() + "/" + name);
				if (file.isDirectory()) {
					return false;
				}
				final String lname = name.toLowerCase();
				return lname.endsWith(mExtFilenameFilter);
			}
		};
	}

	/**
	 * @return エントリがdirならtrue
	 */
	public static FileFilter dirEntryFilter() {
		return new FileFilter() {
			public boolean accept(final File file) {
				return !(file.isFile());
			}
		};
	}

	class compareFileName implements Comparator<File> {
		public int compare(final File f1, final File f2) {
			return (f1.getName().compareToIgnoreCase(f2.getName()));
		}

		public boolean equals(final File f1, final File f2) {
			return (f1.getName().equalsIgnoreCase(f2.getName()));
		}
	}

	/**
	 * @param uri uri
	 */
	public void setFileList(final Uri uri) {
		// フォルダ一覧＋指定拡張子ファイル一覧
		final String path = uri.getPath();
		final int idx = path.lastIndexOf('/');
		final String folder;
		final String filename;
		if (idx >= 0) {
			folder = path.substring(0, idx+1);
			filename = path.substring(idx+1);
		} else {
			folder = "/";
			filename = "";
		}
		mCurrentFolderName = folder;
		mCurDir = new File(folder);
		mCurrentFileName = filename;
		final Comparator<File> c1 = new compareFileName();
		File[] dirs = mCurDir.listFiles(dirEntryFilter());
		if (dirs == null) {
			dirs = new File[0];
		}
		Arrays.sort(dirs, c1);
		File[] files = mCurDir.listFiles(extNameFilter(".txt"));
		if (files == null) {
			files = new File[0];
		}
		Arrays.sort(files, c1);
		final File upDir = new File(folder + "..");
		mDirEntry = new File[1 + dirs.length + files.length];
		mDirEntry[0] = upDir;
		System.arraycopy(dirs, 0, mDirEntry, 1, dirs.length);
		System.arraycopy(files, 0, mDirEntry, 1+dirs.length, files.length);

		int curpos = -1;
		if ((mLastFolderName != null) && (mLastFolderName.length() > 0+1)) {
			// ".."を選んだあと、今までいたフォルダを初期位置にする
			final String s1 = mLastFolderName.substring(0, mLastFolderName.length()-1);
			final int i1 = s1.lastIndexOf('/');
			if (i1 >= 0) {
				final String s2 = s1.substring(i1+1);
				if (s2.length() > 0) {
					for (int i=0; i<mDirEntry.length; i++) {
						if (mDirEntry[i].getName().equals(s2)) {
							curpos = i;
							break;
						}
					}
				}
			}
		}
		if (curpos < 0) {
			for (int i=0; i<mDirEntry.length; i++) {
				if (mDirEntry[i].getName().equals(mCurrentFileName)) {
					curpos = i;
					break;
				}
			}
		}
		mAdapter = new SelectTxtFileAdapter(this, mDirEntry, curpos);
		setListAdapter(mAdapter);

		if (curpos >= 0) {
			getListView().setSelection(curpos);
		}
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		if (V) Log.v(TAG, "onCreate()");
		super.onCreate(savedInstanceState);
	}

	/*
	 * 再起動
	 * @see android.app.Activity#onRestart()
	 */
	@Override
	public void onRestart() {
		if (V) Log.v(TAG, "onRestart()");
		super.onRestart();

	}

	/*
	 * 開始
	 * @see android.app.Activity#onStart()
	 */
	@Override
	public void onStart() {
		if (V) Log.v(TAG, "onStart()");
		super.onStart();
	}

	/*
	 * 再開
	 * @see android.app.Activity#onResume()
	 */
	@Override
	public void onResume() {
		if (V) Log.v(TAG, "onResume()");
		super.onResume();

		// mSelected = false;
		Uri uri;
		final Intent intent = getIntent();
		uri = intent.getData();
		if ((mCurrentFolderName != null) && (mCurrentFolderName.length() > 0)) {
			if ((mCurrentFileName != null) && (mCurrentFileName.length() > 0)) {
				uri = Uri.parse("file://" + mCurrentFolderName + mCurrentFileName);
			} else {
				uri = Uri.parse("file://" + mCurrentFolderName);
			}
		}
		if (uri == null) {
			uri = Uri.parse("file:///");
		}
		setFileList(uri);
		mySetTitle(uri);
	}

	/*
	 * 停止
	 * @see android.app.Activity#onPause()
	 */
	@Override
	public synchronized void onPause() {
		if (V) Log.v(TAG, "onPause()");
		super.onPause();
	}

	/*
	 * 中止
	 * @see android.app.Activity#onStop()
	 */
	@Override
	public void onStop() {
		if (V) Log.v(TAG, "onStop()");
		super.onStop();
	}

	@Override
	public void onDestroy() {
		if (V) Log.v(TAG, "onDestroy()");
		super.onDestroy();
	}

	@Override
	public void onContentChanged() {
		if (V) Log.v(TAG, "onContentChanged");
		super.onContentChanged();
	}

	@Override
	public void onListItemClick(final ListView l, final View v, final int position, final long id) {
		if (V) Log.v(TAG, "onListItemClick");
		final File file = mDirEntry[position];
		if (V) Log.v(TAG, "Selected [" + file.getPath() + "]");

		if (file.isDirectory() || (file.getName().equals(".."))) {
			Uri uri = Uri.parse("file://" + file.getPath() + "/");
			final String name = file.getName();
			mLastFolderName = "";
			if (name.equals("..")) {
				uri = Uri.parse("file://" + file.getParentFile().getParent() + "/");
				mLastFolderName = mCurrentFolderName;
			}
			setFileList(uri);
			mySetTitle(uri);
		} else {
			mLastFolderName = "";
			// mSelected = true;
			final Intent intent = new Intent();
			intent.putExtra("path", file.getPath());
			setResult(RESULT_OK, intent);
			finish();
		}
	}

	/**
	 * Urlをタイトルに設定
	 * @param uri Uri
	 */
	public void mySetTitle(final Uri uri) {
		final String path = uri.getPath();
		final int idx = path.lastIndexOf('/');
		final String folder;
		if (idx >= 0) {
			folder = path.substring(0, idx+1);
		} else {
			folder = "/";
		}
		setTitle(folder + " " + getString(R.string.selecttxtfileactivity_java_title));
	}
}
