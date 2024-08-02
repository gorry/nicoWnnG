/**
 * 
 */
package net.gorry.mydocument;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.UriPermission;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.util.Log;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * @author gorry
 *
 */
public class MyDocumentFileSelector {
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

	private ComponentActivity me;

	//String startDir = "DCIM/Camera";  // replace "/", "%2F"
	private String mInitialFile = "";
	private Uri mInitialUri = null;
	private BiConsumer<Integer, Uri> mSelected = null;
	private Consumer<Integer> mUnselected = null;
	private Boolean mWrite = false;
	private ActivityResultLauncher<Intent> mLauncher;

	public MyDocumentFileSelector(ComponentActivity a) {
		if (T) Log.v(TAG, M()+"@in: a="+a);

		me = a;

		mLauncher = me.registerForActivityResult(
			new ActivityResultContracts.StartActivityForResult(), result -> {
				final int resultCode = result.getResultCode();
				final Intent data = result.getData();

				if (T) Log.v(TAG, M()+"@in: resultCode="+resultCode+", data="+data);

				Bundle extras = null;
				int intentResult = 0;
				if (data != null) {
					extras = data.getExtras();
					if (extras != null) {
						intentResult = extras.getInt("result");
					}
				}
				if (resultCode != me.RESULT_OK) {
					if (T) Log.v(TAG, M()+"@out: cancelled");
					if (mUnselected != null) {
						mUnselected.accept(resultCode);
					}
					return;
				}
				if (data.getData() == null) {
					if (T) Log.v(TAG, M()+"@out: no data");
					if (mUnselected != null) {
						mUnselected.accept(resultCode);
					}
					return;
				}

				Uri uri = data.getData();
				ContentResolver cr = me.getContentResolver();
				cr.takePersistableUriPermission(
					uri,
					Intent.FLAG_GRANT_READ_URI_PERMISSION | 
					(mWrite ? Intent.FLAG_GRANT_WRITE_URI_PERMISSION : 0)
				);
				DocumentFile file = DocumentFile.fromSingleUri(me, uri);
				Uri fileUri = file.getUri();
				mInitialUri = fileUri;  // 次回の初期Uriとなる
				if (T) Log.v(TAG, M()+"fileUri="+fileUri);
				if (mSelected != null) {
					mSelected.accept(resultCode, fileUri);
				}
				if (T) Log.v(TAG, M()+"@out: selected");
				return;
			}
		);

		if (T) Log.v(TAG, M()+"@out");
	}

	public MyDocumentFileSelector setInitialFile(String file) {
		if (T) Log.v(TAG, M()+"@in: file="+file);

		mInitialFile = file;

		if (T) Log.v(TAG, M()+"@out");
		return this;
	}

	public MyDocumentFileSelector setInitialUri(Uri uri) {
		if (T) Log.v(TAG, M()+"@in: uri="+uri);

		mInitialUri = uri;

		if (T) Log.v(TAG, M()+"@out");
		return this;
	}

	public Uri getSelectedUri() {
		if (T) Log.v(TAG, M()+"@in");

		if (T) Log.v(TAG, M()+"@out: uri="+mInitialUri);
		return mInitialUri;
	}

	public MyDocumentFileSelector setSelected(BiConsumer<Integer, Uri> c) {
		if (T) Log.v(TAG, M()+"@in: c="+c);

		mSelected = c;

		if (T) Log.v(TAG, M()+"@out");
		return this;
	}

	public MyDocumentFileSelector setUnselected(Consumer<Integer> c) {
		if (T) Log.v(TAG, M()+"@in: c="+c);

		mUnselected = c;

		if (T) Log.v(TAG, M()+"@out");
		return this;
	}

	public MyDocumentFileSelector setWrite(Boolean b) {
		if (T) Log.v(TAG, M()+"@in: b="+b);

		mWrite = b;

		if (T) Log.v(TAG, M()+"@out");
		return this;
	}


	public void select() {
		if (T) Log.v(TAG, M()+"@in");

		Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		intent.setType("*/*");
		intent.putExtra("android.content.extra.SHOW_ADVANCED", true);
		if (mInitialFile != null) {
			String path = mInitialFile.replaceAll("/", "%2F");
			Uri uri = Uri.parse("content://com.android.externalstorage.documents/document/primary"+"%3A"+path);
			intent.putExtra("android.provider.extra.INITIAL_URI", uri);
		}
		if (mInitialUri != null) {
			intent.putExtra("android.provider.extra.INITIAL_URI", mInitialUri);
		}

		mLauncher.launch(intent);

		if (T) Log.v(TAG, M()+"@out");
	}

}
