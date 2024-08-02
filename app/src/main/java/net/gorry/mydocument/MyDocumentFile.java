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
import android.widget.Toast;

import java.util.function.BiConsumer;

/**
 * @author gorry
 *
 */

public class MyDocumentFile {

	public static DocumentFile createDirectoryIfNotExist(Context c, Uri uri) {
		int idx = uri.toString().lastIndexOf("%2F");
		if (idx < 0) {
			return null;
		}
		Uri parent = Uri.parse(uri.toString().substring(0, idx));
		String displayName = uri.toString().substring(idx+3);
		return createDirectoryIfNotExist(c, parent, displayName)
				;
	}

	public static DocumentFile createDirectoryIfNotExist(Context c, Uri parent, String displayName) {
		DocumentFile d = DocumentFile.fromTreeUri(c, parent);
		if (d == null) {
			return null;
		}

		DocumentFile ret = d.createDirectory(displayName);
		if (ret != null) {
			return ret;
		}

		ret = d.findFile(displayName);
		if (ret == null) {
			return null;
		}
		if (ret.isDirectory()) {
			return ret;
		}

		return null;
	}

	public static DocumentFile createFileIfNotExist(Context c, Uri uri, String mimeType) {
		int idx = uri.toString().lastIndexOf("%2F");
		if (idx < 0) {
			return null;
		}
		Uri parent = Uri.parse(uri.toString().substring(0, idx));
		String displayName = uri.toString().substring(idx+3);
		return createFileIfNotExist(c, parent, displayName, mimeType);
	}

	public static DocumentFile createFileIfNotExist(Context c, Uri parent, String displayName, String mimeType) {
		DocumentFile d = DocumentFile.fromTreeUri(c, parent);
		if (d == null) {
			return null;
		}

		DocumentFile ret = d.createFile(mimeType, displayName);
		if (ret != null) {
			return ret;
		}

		ret = d.findFile(displayName);
		if (ret == null) {
			return null;
		}
		if (ret.isDirectory()) {
			return null;
		}

		return ret;
	}
}
