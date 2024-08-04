/*
 * Copyright (C) 2008,2009  OMRON SOFTWARE Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.gorry.android.input.nicownng;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.XmlResourceParser;
import android.net.Uri;
import androidx.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.ComponentActivity;
import androidx.documentfile.provider.DocumentFile;

import net.gorry.mydocument.MyDocumentFile;

/**
 * The generator class of symbol list.
 * <br>
 * This class is used for generating lists of symbols.
 *
 * @author Copyright (C) 2009 OMRON SOFTWARE CO., LTD.  All Rights Reserved.
 */
public class KeySound {
	/*
	 *
	 */
	static public boolean importUserKeySoundFile(ComponentActivity a, Uri inFile) {
		ContentResolver cr = a.getContentResolver();

		try {
			final File dir = a.getExternalFilesDir("nicoWnnG");
			if (null == dir) {
				return false;
			}

			final String soundFile="type.ogg";
			DocumentFile d = DocumentFile.fromSingleUri(a, inFile);
			if (d.exists()) {
				final File file = new File(dir, soundFile);
				final InputStream istream = cr.openInputStream(inFile);
				final FileOutputStream ostream = new FileOutputStream(file);
				byte[] buf = new byte[1024 * 16];
				while (true) {
					int size = istream.read(buf);
					if (size < 0) break;
					ostream.write(buf, 0, size);
				}
				istream.close();
				ostream.close();
			}

			return true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}
}
