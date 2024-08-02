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
public class MyDocumentTreeSelector {
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

	private static final int REQUEST_READ_DOCUMENT_TREE = 100;
	private static final int REQUEST_WRITE_DOCUMENT_TREE = 101;

	private ComponentActivity me;

	//String startDir = "DCIM/Camera";  // replace "/", "%2F"
	private String mInitialDirectory = "";
	private Uri mInitialUri = null;
	private BiConsumer<Integer, Uri> mSelected = null;
	private Consumer<Integer> mUnselected = null;
	private Boolean mWrite = false;
	private ActivityResultLauncher<Intent> mLauncher;

	public MyDocumentTreeSelector(ComponentActivity a) {
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
				DocumentFile tree = DocumentFile.fromTreeUri(me, uri);
				Uri treeUri = tree.getUri();
				mInitialUri = treeUri;  // 次回の初期Uriとなる
				if (T) Log.v(TAG, M()+"treeUri="+treeUri);
				if (mSelected != null) {
					mSelected.accept(resultCode, treeUri);
				}
				if (T) Log.v(TAG, M()+"@out: selected");
				return;
			}
		);

		if (T) Log.v(TAG, M()+"@out");
	}

	public MyDocumentTreeSelector setInitialDirectory(String dir) {
		if (T) Log.v(TAG, M()+"@in: dir="+dir);

		mInitialDirectory = dir;

		if (T) Log.v(TAG, M()+"@out");
		return this;
	}

	public MyDocumentTreeSelector setInitialUri(Uri uri) {
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

	public MyDocumentTreeSelector setSelected(BiConsumer<Integer, Uri> c) {
		if (T) Log.v(TAG, M()+"@in: c="+c);

		mSelected = c;

		if (T) Log.v(TAG, M()+"@out");
		return this;
	}

	public MyDocumentTreeSelector setUnselected(Consumer<Integer> c) {
		if (T) Log.v(TAG, M()+"@in: c="+c);

		mUnselected = c;

		if (T) Log.v(TAG, M()+"@out");
		return this;
	}

	public MyDocumentTreeSelector setWrite(Boolean b) {
		if (T) Log.v(TAG, M()+"@in: b="+b);

		mWrite = b;

		if (T) Log.v(TAG, M()+"@out");
		return this;
	}


	public void select() {
		if (T) Log.v(TAG, M()+"@in");

		Intent intent;
		StorageManager sm = (StorageManager)me.getSystemService(Context.STORAGE_SERVICE);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
			intent = sm.getPrimaryStorageVolume().createOpenDocumentTreeIntent();
			if (mInitialUri != null) {
				intent.putExtra("android.provider.extra.INITIAL_URI", mInitialUri);
				Log.d(TAG, "uri: " + mInitialUri.toString());
			} else {
				Uri uri = intent.getParcelableExtra("android.provider.extra.INITIAL_URI");
				String scheme = uri.toString();
				Log.d(TAG, "INITIAL_URI scheme: " + scheme);
				scheme = scheme.replace("/root/", "/document/");
				scheme += "%3A" + mInitialDirectory;
				uri = Uri.parse(scheme);
				intent.putExtra("android.provider.extra.INITIAL_URI", uri);
				Log.d(TAG, "uri: " + uri.toString());
			}
		} else {
			intent = sm.getPrimaryStorageVolume().createAccessIntent(Environment.DIRECTORY_DOCUMENTS);
		}

		mLauncher.launch(intent);

		if (T) Log.v(TAG, M()+"@out");
	}

}
