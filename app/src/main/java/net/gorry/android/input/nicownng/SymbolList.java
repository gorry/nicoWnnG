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
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.content.res.XmlResourceParser;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

/**
 * The generator class of symbol list.
 * <br>
 * This class is used for generating lists of symbols.
 *
 * @author Copyright (C) 2009 OMRON SOFTWARE CO., LTD.  All Rights Reserved.
 */
public class SymbolList implements WnnEngine {
	/*
	 * DEFINITION OF CONSTANTS
	 */
	/** Language definition (English) */
	public static final int LANG_EN = 0;

	/** Language definition (Japanese) */
	public static final int LANG_JA = 1;

	/** Language definition (Chinese) */
	public static final int LANG_ZHCN = 2;


	/** Key string to get normal symbol list for Japanese */
	public static final String SYMBOL_JAPANESE = "j";

	/** Key string to get normal symbol list for English */
	public static final String SYMBOL_ENGLISH = "e";

	/** Key string to get normal symbol list for Chinese */
	public static final String SYMBOL_CHINESE = "c1";

	/** Key string to get face mark list for Japanese */
	public static final String SYMBOL_JAPANESE_FACE  = "j_face";
	/** Key string to get face mark list for Japanese */
	public static final String SYMBOL_USER  = "usersymbol_";

	/** Key string to get face mark list for Japanese */
	public static final String SYMBOL_DOCOMO_EMOJI00  = "d_emoji00";
	/** Key string to get face mark list for Japanese */
	public static final String SYMBOL_DOCOMO_EMOJI01  = "d_emoji01";

	/** The name of XML tag key */
	private static final String XMLTAG_KEY = "string";

	/*
	 * DEFINITION OF VARIABLES
	 */
	/** Symbols data */
	protected HashMap<String,ArrayList<String>> mSymbols;

	/** OpenWnn which has this instance */
	private final NicoWnnG mWnn;

	/** current list of symbols */
	private ArrayList<String> mCurrentList;

	/** Iterator for getting symbols from the list */
	private Iterator<String> mCurrentListIterator;

	private final ArrayList<String> mJAJPSymbols;
	private final ArrayList<String> mJAJPNormalSymbols;
	private final ArrayList[] mJAJPUserSymbolArray = new ArrayList[USERSYMBOL_TYPES];

	public static final String[] USERSYMBOL_DIC_NAME = {
		"usersymbol_zen_hiragana",
		"usersymbol_zen_katakana",
		"usersymbol_zen_alphabet",
		"usersymbol_zen_number",
		"usersymbol_han_katakana",
		"usersymbol_han_alphabet",
		"usersymbol_han_number",
	};
	public static final int USERSYMBOL_TYPES = USERSYMBOL_DIC_NAME.length;

	private boolean mUserSymbolLoaded = false;
	private boolean mUserSymbolDeviceReady = false;
	private int mUserSymbolWaitLoadCount = 0;
	
	/*
	 * DEFINITION OF METHODS
	 */
	/**
	 * Constructor
	 *
	 * @param parent  OpenWnn instance which uses this.
	 * @param lang    Language ({@code LANG_EN}, {@code LANG_JA} or {@code LANG_ZHCN})
	 */
	public SymbolList(final NicoWnnG parent, final int lang) {
		mWnn = parent;
		mSymbols = new HashMap<String, ArrayList<String>>();

		int jajpsymbolcount = 0;
		int jajpnormalsymbolcount = 0;
		mJAJPSymbols = new ArrayList<String>();
		mJAJPNormalSymbols = new ArrayList<String>();

		switch (lang) {
			case LANG_EN:
				/* symbols for English IME */
				mSymbols.put(SYMBOL_ENGLISH, getXmlfile(R.xml.symbols_latin12_list));
				mCurrentList = mSymbols.get(SYMBOL_ENGLISH);
				break;

			case LANG_JA:
				/* symbols for Japanese IME */
				mSymbols.put(SYMBOL_JAPANESE, getXmlfile(R.xml.symbols_japan_list));
				mJAJPSymbols.add(jajpsymbolcount++, SYMBOL_JAPANESE);
				mJAJPNormalSymbols.add(jajpnormalsymbolcount++, SYMBOL_JAPANESE);
				mSymbols.put(SYMBOL_ENGLISH, getXmlfile(R.xml.symbols_latin1_list));
				mJAJPSymbols.add(jajpsymbolcount++, SYMBOL_ENGLISH);
				mJAJPNormalSymbols.add(jajpnormalsymbolcount++, SYMBOL_ENGLISH);
				mSymbols.put(SYMBOL_JAPANESE_FACE, getXmlfile(R.xml.symbols_japan_face_list));
				mJAJPSymbols.add(jajpsymbolcount++, SYMBOL_JAPANESE_FACE);
				mJAJPNormalSymbols.add(jajpnormalsymbolcount++, SYMBOL_JAPANESE_FACE);
				/* entry user symbols */
				boolean userentry = true;
				Integer userentrynum = 0;
				do {
					final String symbolname = SYMBOL_USER + userentrynum.toString();
					final String symbolfile = SYMBOL_USER + userentrynum.toString() + ".xml";
					if (true == checkUserXmlfile(symbolfile)) {
						mSymbols.put(symbolname, getXmlfile(symbolfile));
						mJAJPSymbols.add(jajpsymbolcount++, symbolname);
						mJAJPNormalSymbols.add(jajpnormalsymbolcount++, symbolname);
						userentrynum++;
					}
					else{
						userentry = false;
					}
				} while (userentry);

				final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mWnn);
				int emoji_type = Integer.valueOf(pref.getString("emoji_type", "1"));
				switch (emoji_type) {
				  default:
				  case 0:
					mSymbols.put(SYMBOL_DOCOMO_EMOJI00, getXmlfile(R.xml.symbols_docomo_emoji00_list));
					mSymbols.put(SYMBOL_DOCOMO_EMOJI01, getXmlfile(R.xml.symbols_docomo_emoji01_list));
					break;
				  case 1:
					mSymbols.put(SYMBOL_DOCOMO_EMOJI00, getXmlfile(R.xml.symbols_google_docomo_emoji00_list));
					mSymbols.put(SYMBOL_DOCOMO_EMOJI01, getXmlfile(R.xml.symbols_google_docomo_emoji01_list));
					break;
				  case 2:
					mSymbols.put(SYMBOL_DOCOMO_EMOJI00, getXmlfile(R.xml.symbols_google_softbank_emoji00_list));
					mSymbols.put(SYMBOL_DOCOMO_EMOJI01, getXmlfile(R.xml.symbols_google_softbank_emoji01_list));
					break;
				  case 3:
					mSymbols.put(SYMBOL_DOCOMO_EMOJI00, getXmlfile(R.xml.symbols_google_kddi_emoji00_list));
					mSymbols.put(SYMBOL_DOCOMO_EMOJI01, getXmlfile(R.xml.symbols_google_kddi_emoji01_list));
					break;
				  case 4:
					mSymbols.put(SYMBOL_DOCOMO_EMOJI00, getXmlfile(R.xml.symbols_google_unicode_emoji00_list));
					mSymbols.put(SYMBOL_DOCOMO_EMOJI01, getXmlfile(R.xml.symbols_google_unicode_emoji01_list));
					break;
				}

				final NicoWnnGJAJP t = NicoWnnGJAJP.getInstance();
				final DefaultSoftKeyboard inputManager = ((DefaultSoftKeyboard)(t.mInputViewManager));
				boolean addsymbolsemoji;
				if (inputManager.getKeyboardType() == DefaultSoftKeyboard.KEYBOARD_12KEY) {
					addsymbolsemoji = mWnn.getOrientPrefBoolean(pref, "symbol_addsymbolemoji_12key", true);
				} else {
					addsymbolsemoji = mWnn.getOrientPrefBoolean(pref, "symbol_addsymbolemoji_qwerty", true);
				}
				if (addsymbolsemoji) {
					mJAJPSymbols.add(jajpsymbolcount++, SYMBOL_DOCOMO_EMOJI00);
					mJAJPSymbols.add(jajpsymbolcount++, SYMBOL_DOCOMO_EMOJI01);
				}

				mCurrentList = mSymbols.get(SYMBOL_ENGLISH);
				break;

			case LANG_ZHCN:
				/* symbols for Chinese IME */
				mSymbols.put(SYMBOL_CHINESE, getXmlfile(R.xml.symbols_china_list));
				mSymbols.put(SYMBOL_ENGLISH, getXmlfile(R.xml.symbols_latin1_list));
				mCurrentList = mSymbols.get(SYMBOL_CHINESE);
				break;
		}

		loadUserSymbolList();

		mCurrentList = null;
	}

	/*
	 * ユーザーシンボルリストの読み込み
	 */
	public boolean loadUserSymbolList() {
		mUserSymbolDeviceReady = false;
		mUserSymbolLoaded = false;
		int[] jajpusersymbolarray = new int[USERSYMBOL_TYPES];
		for (int i=0; i<USERSYMBOL_TYPES; i++) {
			mJAJPUserSymbolArray[i] = new ArrayList<String>();
			jajpusersymbolarray[i] = 0;
		}
		
		for (int i=0; i<USERSYMBOL_DIC_NAME.length; i++) {
			for (int j=1; j<10; j++) {
				final String dicName = String.format("%s_%d", USERSYMBOL_DIC_NAME[i], j);
				final String dicFile = dicName + ".xml";
				if (!checkUserXmlfile(dicFile)) break;
				mUserSymbolDeviceReady = true;
				ArrayList<String> xml = getXmlfile(dicFile);
				if (xml == null) break;
				mSymbols.put(dicName, xml);
				mJAJPUserSymbolArray[i].add(jajpusersymbolarray[i]++, dicName);
				mUserSymbolLoaded = true;
			}
		}

		return mUserSymbolLoaded;
	}

	/*
	 * ユーザーシンボルリストのチェックに成功しているかどうか
	 */
	public boolean getUserSymbolChecked() {
		return mUserSymbolDeviceReady;
	}

	/*
	 *
	 */
	public int getJAJPSymbolNum() {
		return mJAJPSymbols.size();
	}

	/*
	 *
	 */
	public int getJAJPNormalSymbolNum() {
		return mJAJPNormalSymbols.size();
	}

	/*
	 *
	 */
	public int getJAJPUserSymbolNum(int type) {
		return mJAJPUserSymbolArray[type].size();
	}

	/**
	 * Get a attribute value from a XML resource.
	 *
	 * @param xrp   XML resource
	 * @param name  The attribute name
	 *
	 * @return  The value of the attribute
	 */
	private String getXmlAttribute(final XmlResourceParser xrp, final String name) {
		final int resId = xrp.getAttributeResourceValue(null, name, 0);
		if (resId == 0) {
			return xrp.getAttributeValue(null, name);
		} else {
			return mWnn.getString(resId);
		}
	}

	/**
	 * Load a symbols list from XML resource.
	 *
	 * @param id    XML resource ID
	 * @return      The symbols list
	 */
	private ArrayList<String> getXmlfile(final int id) {
		final ArrayList<String> list = new ArrayList<String>();

		final XmlResourceParser xrp = mWnn.getResources().getXml(id);
		try {
			int xmlEventType;
			while ((xmlEventType = xrp.next()) != XmlPullParser.END_DOCUMENT) {
				if (xmlEventType == XmlPullParser.START_TAG) {
					final String attribute = xrp.getName();
					if (XMLTAG_KEY.equals(attribute)) {
						final String value = xrp.getAttributeValue(null, "value");
						if (value != null) {
							list.add(value);
						}
					}
				}
			}
			xrp.close();
		} catch (final XmlPullParserException e) {
			Log.e("NicoWnnG", "Ill-formatted keybaord resource file");
			return null;
		} catch (final IOException e) {
			Log.e("NicoWnnG", "Unable to read keyboard resource file");
			return null;
		}

		return list;
	}
	/*******************************************************************************************
	 * external file access
	 *******************************************************************************************/
	/*
	 *
	 */
	private ArrayList<String> getXmlfile(final String filename) {
		final ArrayList<String> list = new ArrayList<String>();

		final File dir = getExternalStorageDirectory();

		try {
			final File fileLoad = new File(dir, filename);
			// Log.d("load", "create fileload\n");
			final FileInputStream fin = new FileInputStream(fileLoad);
			// Log.d("load", "create inputstream\n");
			final XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			final XmlPullParser xrp = factory.newPullParser();
			xrp.setInput(fin, "UTF8");
			// Log.d("load", "create XML parser\n");
			int xmlEventType;
			while ((xmlEventType = xrp.next()) != XmlPullParser.END_DOCUMENT) {
				if (xmlEventType == XmlPullParser.START_TAG) {
					final String attribute = xrp.getName();
					if (XMLTAG_KEY.equals(attribute)) {
						final String value = xrp.getAttributeValue(null, "value");
						if (value != null) {
							list.add(value);
						}
					}
				}
			}
			fin.close();
		} catch (final XmlPullParserException e) {
			Log.e("NicoWnnG", "Ill-formatted keybaord resource file");
		} catch (final IOException e) {
			Log.e("NicoWnnG", "Unable to read keyboard resource file");
		} catch (final Exception e) {
			Log.e("NicoWnnG", "Unknown error file");
		}
		return list;
	}

	/*
	 *
	 */
	private boolean checkUserXmlfile(final String checkname) {
		final File dir = getExternalStorageDirectory();
		if (null == dir) {
			return false;
		}
		final File checkf = new File(dir, checkname);
		if (!checkf.exists()) {
			return false;
		}
		return true;
	}
	/*
	 *
	 */
	static private File getExternalStorageDirectory() {
		try {
			final boolean state = Environment.getExternalStorageState().contains(Environment.MEDIA_MOUNTED);
			if (false == state) {
				Log.w("sdcard", "not mount sdcard!!\n");
				return null;
			}
			// Log.d("sdcard", "mount sdcard!!\n");
			Environment.getExternalStorageDirectory();
			File dir = null;
			dir = new File(Environment.getExternalStorageDirectory(), "nicoWnnG");
			if (!dir.exists()) {
				dir.mkdirs();
				if (!dir.isDirectory()) {
					return null;
				}
			}
			return dir;
        } catch (final Exception e) {
        	Log.e("NicoWnnG", "Unknown error file");
        }
        return null;
	}
	/*
	 *
	 */
	static public boolean copyUserSymbolDicFileToExternalStorageDirectory(Context context, boolean force) {
		InputStream fin = null;
		FileOutputStream fout = null;
		try {
			final File dir = getExternalStorageDirectory();
			if (null == dir) {
				return false;
			}

			for (int i=0; i<USERSYMBOL_DIC_NAME.length; i++) {
				for (int j=1; j<10; j++) {
					final String dicName = String.format("%s_%d", USERSYMBOL_DIC_NAME[i], j);
					final String dicFile = dicName + ".xml";
					final File file = new File(dir, dicFile);
					if (!file.exists() || force) {
						int resid = context.getResources().getIdentifier(dicName, "raw", "net.gorry.android.input.nicownng");
						if (resid != 0) {
							fin = context.getResources().openRawResource(resid);
							int size = fin.available();
							fout = new FileOutputStream(file);
							byte[] buf = new byte[size];
							fin.read(buf, 0, size);
							fout.write(buf, 0, size);
							fout.close();
							fin.close();
						}
					}
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		fout = null;
		fin = null;
		return true;
	}

	/**
	 * Set the dictionary
	 *
	 * @param listType  The list of symbol
	 * @return          {@code true} if valid type is specified; {@code false} if not;
	 */
	public boolean setSymbolDictionary(final String listType) {
		mCurrentList = mSymbols.get(listType);
		return (mCurrentList != null);
	}
	public boolean checkSymbolDictionary(final String listType) {
		if (null == mCurrentList) {
			return false;
		}
		if (mCurrentList != mSymbols.get(listType)) {
			return false;
		}
		return true;
	}
	/*
	 *
	 */
	public boolean setSymbolDictionary(final int jajpid) {
		mCurrentList = mSymbols.get(mJAJPSymbols.get(jajpid));
		return (mCurrentList != null);
	}

	/*
	 *
	 */
	public boolean setUserSymbolDictionary(final int type, final int id) {

		// SDカード検査中などで読めないときは待つよう指示
		if (!mUserSymbolDeviceReady) {
			loadUserSymbolList();
			String s = mWnn.getApplicationContext().getString(R.string.toast_setusersymboldictionary_waitload);
			Toast.makeText(mWnn.getApplicationContext(), s, Toast.LENGTH_SHORT).show();
			return false;
		}

		// mCurrentList = mSymbols.get(mJAJPUserSymbolArray[type].get(id));
		// return (mCurrentList != null);

		// １ページも読めないなら壊れてるかも
		if (!mUserSymbolLoaded) {
			loadUserSymbolList();
			String s = mWnn.getApplicationContext().getString(R.string.toast_setusersymboldictionary_broken);
			Toast.makeText(mWnn.getApplicationContext(), s, Toast.LENGTH_LONG).show();
			return false;
		}
			
		int l1 = mJAJPUserSymbolArray.length;
		if (type < l1) {
			ArrayList<String> a = mJAJPUserSymbolArray[type];
			int l2 = a.size();
			if (id < l2) {
				String s = a.get(id);
				mCurrentList = mSymbols.get(s);
				return (mCurrentList != null);
			}

			// このキーボードには定義がない
			if (l2 == 0) {
				String s = mWnn.getApplicationContext().getString(R.string.toast_setusersymboldictionary_nodata);
				Toast.makeText(mWnn.getApplicationContext(), s, Toast.LENGTH_SHORT).show();
				return false;
			}

			// ページ異常
			String s = String.format(": id=%d, l2=%d", id, l2);
			Log.e("NicoWnnG", "setUserSymbolDictionary(): failed: overpage"+s);
			String s2 = mWnn.getApplicationContext().getString(R.string.toast_setusersymboldictionary_error_overpage);
			Toast.makeText(mWnn.getApplicationContext(), s2+s, Toast.LENGTH_LONG).show();
			return false;
		}

		// モード異常
		String s = String.format(": type=%d, l1=%d", type, l1);
		Log.e("NicoWnnG", "setUserSymbolDictionary(): failed: overmode"+s);
		String s2 = mWnn.getApplicationContext().getString(R.string.toast_setusersymboldictionary_error_overmode);
		Toast.makeText(mWnn.getApplicationContext(), s2+s, Toast.LENGTH_LONG).show();
		return false;
	}

	/***********************************************************************
	 * WnnEngine's interface
	 **********************************************************************/
	/** @see net.gorry.android.input.nicownng.WnnEngine#init */
	public void init() {}

	/** @see net.gorry.android.input.nicownng.WnnEngine#close */
	public void close() {}

	/** @see net.gorry.android.input.nicownng.WnnEngine#predict */
	public int predict(final ComposingText text, final int minLen, final int maxLen) {
		/* ignore if there is no list for the type */
		if (mCurrentList == null) {
			mCurrentListIterator = null;
			return 0;
		}

		/* return the iterator of the list */
		mCurrentListIterator = mCurrentList.iterator();
		return 1;
	}

	/** @see net.gorry.android.input.nicownng.WnnEngine#convert */
	public int convert(final ComposingText text) {
		return 0;
	}

	/** @see net.gorry.android.input.nicownng.WnnEngine#searchWords */
	public int searchWords(final String key) {return 0;}

	/** @see net.gorry.android.input.nicownng.WnnEngine#searchWords */
	public int searchWords(final WnnWord word) {return 0;}

	/** reset word count */
	public void resetCandidate() {
		if (mCurrentList == null) {
			return;
		}
		mCurrentListIterator = mCurrentList.iterator();
	}

	/** @see net.gorry.android.input.nicownng.WnnEngine#getNextCandidate */
	public WnnWord getNextCandidate() {
		if ((mCurrentListIterator == null) || !mCurrentListIterator.hasNext()) {
			return null;
		}
		final String str = mCurrentListIterator.next();
		final WnnWord word = new WnnWord(str, str);
		return word;
	}

	/** @see net.gorry.android.input.nicownng.WnnEngine#learn */
	public boolean learn(final WnnWord word) {return false;}

	/** @see net.gorry.android.input.nicownng.WnnEngine#forget */
	public boolean forget(final WnnWord word) {return false;}

	/** @see net.gorry.android.input.nicownng.WnnEngine#addWord */
	public int addWord(final WnnWord word) {return 0;}

	/** @see net.gorry.android.input.nicownng.WnnEngine#addWords */
	public int addWords(final WnnWord[] words, Runnable progress) {return 0;}

	/** @see net.gorry.android.input.nicownng.WnnEngine#deleteWord */
	public boolean deleteWord(final WnnWord word) {return false;}

	/** @see net.gorry.android.input.nicownng.WnnEngine#setPreferences */
	public void setPreferences(final SharedPreferences pref) {}

	/** @see net.gorry.android.input.nicownng.WnnEngine#breakSequence */
	public void breakSequence() {}

	/** @see net.gorry.android.input.nicownng.WnnEngine#makeCandidateListOf */
	public int makeCandidateListOf(final int clausePosition) {return 0;}

	/** @see net.gorry.android.input.nicownng.WnnEngine#initializeDictionary */
	public boolean initializeDictionary(final int dictionary) {return true;}

	/** @see net.gorry.android.input.nicownng.WnnEngine#initializeDictionary */
	public boolean initializeDictionary(final int dictionary, final int type) {return true;}

	/** @see net.gorry.android.input.nicownng.WnnEngine#getUserDictionaryWords */
	public WnnWord[] getUserDictionaryWords() {return null;}
}
