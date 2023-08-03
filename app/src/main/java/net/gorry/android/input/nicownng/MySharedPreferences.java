/**
 * 
 */
package net.gorry.android.input.nicownng;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import android.preference.PreferenceManager;
import android.util.Log;
import android.util.Xml;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

/**
 * @author gorry
 *
 */
public class MySharedPreferences {
	private static final String TAG = "MySharedPreferences";
	private static final boolean V = false;
	private static final boolean D = false;
	private static final boolean I = false;

	private Context me;
	private boolean mShared = false;
	private String mFilename;
	private SharedPreferences mPref;
	private SharedPreferences.Editor mEditor;
	private TreeMap<String, Object> mMap;
	private TreeMap<String, Object> mEditMap;

	/**
	 * @param context context
	 * @param name name
	 */
	@SuppressWarnings("unchecked")
	public MySharedPreferences(Context context, String name) {
		if (I) Log.i(TAG, "MySharedPreferences(): name=[" + name + "]");
		me = context;
		if (name == null) {
			if (I) Log.i(TAG, "MySharedPreferences(): default shared");
			mFilename = "(default)";
			mShared = true;
			mPref = PreferenceManager.getDefaultSharedPreferences(me);
			mEditor = mPref.edit();
			return;
		}
		mFilename = name;
		if (!mFilename.startsWith("/")) {
			if (I) Log.i(TAG, "MySharedPreferences(): shared");
			mShared = true;
			mPref = me.getSharedPreferences(mFilename, 0);
			mEditor = mPref.edit();
			return;
		}
		mShared = false;
		mMap = new TreeMap<String, Object>();
		try {
			FileInputStream istream = new FileInputStream(mFilename);
			XmlPullParser parser = Xml.newPullParser();
			parser.setInput(istream, "UTF-8");
			int event = parser.getEventType();
			while (event != XmlPullParser.END_DOCUMENT) {
				switch (event) {
				case XmlPullParser.START_TAG:
					TreeMap<String, Object> map = new TreeMap<String, Object>();
					readValueXml(map, parser);
					if (map.containsKey("")) {
						Object map2 = map.get("");
						if (map2 != null) {
							mMap.putAll((TreeMap<String, Object>)map2);
						}
					}
					break;
				}
				event = parser.next();
			}
			istream.close();
		} catch (org.xmlpull.v1.XmlPullParserException e) {
			e.printStackTrace();
        } catch (FileNotFoundException e) {
			// e.printStackTrace();
        } catch (IOException e) {
			e.printStackTrace();
        }
	}

	/**
	 * 値読み込み用XMLパーザ
	 * @param map 値格納用map
	 * @param parser XmlPullParser
	 * @return 処理成功ならtrue
	 */
	private final boolean readValueXml(Map<String, Object> map, XmlPullParser parser) throws XmlPullParserException, IOException {
		if (I) Log.i(TAG, "readValueXml()");
		Object obj = null;
		final String valueName = parser.getAttributeValue(null, "name");
		final String tagName = parser.getName();
		int event;
		boolean result = false;
		
		if (I) Log.i(TAG, "tagName=[" + tagName + "], valueName=[" + valueName + "]");

		if (tagName.equals("null")) {
			result = true;
		} else if (tagName.equals("map")) {
			parser.next();
			result = readMapXml(map, parser, valueName);
			return (result);
		} else if (tagName.equals("int")) {
			String value = parser.getAttributeValue(null, "value");
			if (I) Log.i(TAG, "value=[" + value + "]");
			obj = Integer.parseInt(value);
			map.put(valueName, obj);
			result = true;
		} else if (tagName.equals("boolean")) {
			String value = parser.getAttributeValue(null, "value");
			if (I) Log.i(TAG, "value=[" + value + "]");
			obj = Boolean.valueOf(value);
			map.put(valueName, obj);
			result = true;
		} else if (tagName.equals("string")) {
			String value = "";
			if (I) Log.i(TAG, "<string> start");
			while ((event = parser.next()) != XmlPullParser.END_DOCUMENT) {
				if (event == XmlPullParser.START_TAG) {
					String tag = parser.getName();
					throw new XmlPullParserException("<string> cannot contains any other tag: <" + tag + ">");
				} else if (event == XmlPullParser.END_TAG) {
					String tag = parser.getName();
					if (tag.equals("string")) {
						if (I) Log.i(TAG, "<string> close: value=[" + value + "]");
						obj = value;
						map.put(valueName, obj);
						result = true;
						return (result);
					}
					throw new XmlPullParserException("<string> must be closed by </string>: </" + tag + ">");
				} else if (event == XmlPullParser.TEXT) {
					String content = parser.getText();
					if (I) Log.i(TAG, "content=[" + content + "]");
					value += content;
				} else {
					throw new XmlPullParserException("cannot parse in <string>: event " + event);
				}
			}
		} else {
			throw new XmlPullParserException("unknown tag <" + tagName + "> start");
		}

		while ((event = parser.next()) != XmlPullParser.END_DOCUMENT) {
			if (event == XmlPullParser.START_TAG) {
				String tag = parser.getName();
				throw new XmlPullParserException("<" + tagName + "> cannot contains any other tag: <" + tag + ">");
			} else if (event == XmlPullParser.END_TAG) {
				String tag = parser.getName();
				if (tag.equals(tagName)) {
					if (I) Log.i(TAG, "<" + tagName + "> close.");
					return result;
				}
				throw new XmlPullParserException("<" + tagName + "> is closed by another tag:  </" + tag + ">");
			} else if (event == XmlPullParser.TEXT) {
				String content = parser.getText();
				if (I) Log.i(TAG, "contains text [" + content + "]");
				// throw new XmlPullParserException("cannot contains text [" + content + "] in <" + tagName + ">");
			} else {
				throw new XmlPullParserException("cannot parse in <" + tagName + ">: event " + event);
			}
		}

		throw new XmlPullParserException("end of document in <" + tagName + ">");
	}

	/**
	 * readMapXml
	 * @param map 値格納用map
	 * @param parser XmlPullParser
	 * @param tagName mapキー名
	 * @return 処理成功ならtrue
	 */
	private final boolean readMapXml(Map<String, Object> map, XmlPullParser parser, String tagName) throws XmlPullParserException, IOException {
		if (I) Log.i(TAG, "readMapXml()");
		TreeMap<String, Object> map2 = new TreeMap<String, Object>();
		boolean result = false;
		String tagName2 = (tagName == null) ? "" : tagName;

		int event = parser.getEventType();
		while (event != XmlPullParser.END_DOCUMENT) {
			if (event == XmlPullParser.START_TAG) {
				readValueXml(map2, parser);
			} else if (event == XmlPullParser.END_TAG) {
				String tag = parser.getName();
				if (tag.equals("map")) {
					if (I) Log.i(TAG, "<map> close");
					Object obj = map2;
					map.put(tagName2, obj);
					result = true;
					break;
				}
				throw new XmlPullParserException("<map> must be closed by </map>: </" + tag + ">");
			} else if (event == XmlPullParser.TEXT) {
				String content = parser.getText();
				if (I) Log.i(TAG, "contains text [" + content + "]");
				// throw new XmlPullParserException("cannot contains text [" + content + "] in <" + tagName + ">");
			} else {
				throw new XmlPullParserException("cannot parse in <map>: event " + event);
			}
			event = parser.next();
		}

		return result;
	}

	/**
	 * getInt
	 * @param name name
	 * @param def def
	 * @return getInt
	 */
	public int getInt(String name, int def) {
		if (I) Log.i(TAG, "getInt(): " + mFilename + ": name=[" + name + "], def=[" + def + "]");
		int value;
		if (mShared) {
			value = mPref.getInt(name, def);
			if (I) Log.i(TAG, "getInt(): shared: return value=[" + value + "]");
			return value;
		}
		if (mMap.containsKey(name)) {
			value = (Integer)mMap.get(name);
		} else {
			if (I) Log.i(TAG, "getString(): not have key");
			value = def;
		}
		if (I) Log.i(TAG, "getString(): return value=[" + value + "]");
		return value;
	}

	/**
	 * getString
	 * @param name name
	 * @param def def
	 * @return getInt
	 */
	public String getString(String name, String def) {
		if (I) Log.i(TAG, "getString(): " + mFilename + ": name=[" + name + "], def=[" + def + "]");
		String value;
		if (mShared) {
			value = mPref.getString(name, def);
			if (I) Log.i(TAG, "getString(): shared: return value=[" + value + "]");
			return value;
		}
		if (mMap.containsKey(name)) {
			value = (String)mMap.get(name);
		} else {
			if (I) Log.i(TAG, "getString(): not have key");
			value = def;
		}
		if (I) Log.i(TAG, "getString(): return value=[" + value + "]");
		return value;
	}

	/**
	 * getBoolean
	 * @param name name
	 * @param def def
	 * @return getInt
	 */
	public boolean getBoolean(String name, boolean def) {
		if (I) Log.i(TAG, "getBoolean(): " + mFilename + ": name=[" + name + "], def=[" + def + "]");
		boolean value;
		if (mShared) {
			value = mPref.getBoolean(name, def);
			if (I) Log.i(TAG, "getBoolean(): shared: return value=[" + value + "]");
			return value;
		}
		if (mMap.containsKey(name)) {
			value = (Boolean)mMap.get(name);
		} else {
			if (I) Log.i(TAG, "getBoolean(): not have key");
			value = def;
		}
		if (I) Log.i(TAG, "getBoolean(): return value=[" + value + "]");
		return value;
	}

	/**
	 * @author gorry
	 *
	 */
	public final class Editor {

		/**
		 * clear
		 * @return this
		 */
		public MySharedPreferences.Editor clear() {
			if (I) Log.i(TAG, "Editor.clear(): " + mFilename);
			if (mShared) {
				if (I) Log.i(TAG, "Editor.clear(): shared");
				mEditor.clear();
				return this;
			}

			mEditMap.clear();
			return this;
		}

		/**
		 * commit
		 * @return 成功ならtrue
		 */
		public boolean commit() {
			if (I) Log.i(TAG, "Editor.commit(): " + mFilename);
			if (mShared) {
				if (I) Log.i(TAG, "Editor.commit(): shared");
				return mEditor.commit();
			}

			boolean result = false;
			try {
				// 親フォルダを作る
				File file = new File(mFilename);
				String parentDirPath = file.getParent();
				file = new File(parentDirPath);
				if (file.isDirectory()) {
					if (I) Log.i(TAG, "parent Directory ["+parentDirPath+"] exists");
				} else {
					file.mkdirs();
					if (!file.isDirectory()) {
						Log.e(TAG, "cannot create Directory ["+parentDirPath+"]");
						return false;
					}
					if (I) Log.i(TAG, "parent Directory ["+parentDirPath+"] created");
				}

				FileOutputStream ostream = new FileOutputStream(mFilename);
				XmlSerializer writer = Xml.newSerializer();
				writer.setOutput(ostream, "UTF-8");
				writer.startDocument("UTF-8", true);
				writer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
				writeMapXml(null, mEditMap, writer);
				writer.flush();
				ostream.close();
				result = true;
			} catch (org.xmlpull.v1.XmlPullParserException e) {
				e.printStackTrace();
	        } catch (FileNotFoundException e) {
				e.printStackTrace();
	        } catch (IOException e) {
				e.printStackTrace();
	        }
			
	        return result;
		}

		/**
		 * remove
		 * @param name name
		 * @return this
		 */
		public MySharedPreferences.Editor remove(String name) {
			if (I) Log.i(TAG, "Editor.remove(): " + mFilename);
			if (mShared) {
				if (I) Log.i(TAG, "Editor.remove(): shared");
				mEditor.remove(name);
				return this;
			}
			mEditMap.remove(name);
			return this;
		}

		/**
		 * putInt
		 * @param name name
		 * @param data data
		 * @return this
		 */
		public MySharedPreferences.Editor putInt(String name, int data) {
			if (I) Log.i(TAG, "Editor.putInt(): " + mFilename + ": name=[" + name + "], data=[" + data + "]");
			if (mShared) {
				if (I) Log.i(TAG, "Editor.putInt(): shared");
				mEditor.putInt(name, data);
				return this;
			}
			mEditMap.put(name, data);
			return this;
		}

		/**
		 * putString
		 * @param name name
		 * @param data data
		 * @return this
		 */
		public MySharedPreferences.Editor putString(String name, String data) {
			if (I) Log.i(TAG, "Editor.putString(): " + mFilename + ": name=[" + name + "], data=[" + data + "]");
			if (mShared) {
				if (I) Log.i(TAG, "Editor.putString(): shared");
				mEditor.putString(name, data);
				return this;
			}
			mEditMap.put(name, data);
			return this;
		}

		/**
		 * putBoolean
		 * @param name name
		 * @param data data
		 * @return this
		 */
		public MySharedPreferences.Editor putBoolean(String name, boolean data) {
			if (I) Log.i(TAG, "Editor.putBoolean(): " + mFilename + ": name=[" + name + "], data=[" + data + "]");
			if (mShared) {
				if (I) Log.i(TAG, "Editor.putBoolean(): shared");
				mEditor.putBoolean(name, data);
				return this;
			}
			mEditMap.put(name, data);
			return this;
		}
	}

	/**
	 * edit
	 * @return edit
	 */
	public Editor edit() {
		if (I) Log.i(TAG, "Editor.edit(): " + mFilename);
		if (mShared) {
			if (I) Log.i(TAG, "Editor.edit(): shared");
			return new Editor();
		}
		mEditMap = new TreeMap<String, Object>();
		mEditMap.putAll(mMap);
		return new Editor();
	}

	/**
	 * 値書き込み用XMLライタ
	 * @param name Name
	 * @param data Data
	 * @return 書き込み成功時true
	 */
	private final boolean writeValueXml(String name, Object data, XmlSerializer writer) throws XmlPullParserException, IOException {
		if (I) Log.i(TAG, "writeValueXml(): " + mFilename + ": name=[" + name + "], data=[" + data + "]");
		String typeStr = null;
		if (data == null) {
			if (I) Log.i(TAG, "writeValueXml(): write <null>");
			writer.startTag(null, "null");
			if (name != null) {
				writer.attribute(null, "name", name);
			}
			writer.endTag(null, "null");
			return true;
		} else if (data instanceof String) {
			if (I) Log.i(TAG, "writeValueXml(): write <string>");
			writer.startTag(null, "string");
			if (name != null) {
				writer.attribute(null, "name", name);
			}
			writer.text(data.toString());
			writer.endTag(null, "string");
			return true;
		} else if (data instanceof Map) {
			@SuppressWarnings("unchecked")
			Map<String, Object> map = (Map<String, Object>)data;
			writeMapXml(name, map, writer);
			return true;
		} else if (data instanceof Integer) {
			typeStr = "int";
		} else if (data instanceof Boolean) {
			typeStr = "boolean";
		} else {
			if (I) Log.i(TAG, "writeValueXml(): unknown type");
			throw new RuntimeException("writeValueXml(): unknown type: name=[" + name + "], data=[" + data + "]");
		}

		if (I) Log.i(TAG, "writeValueXml(): write <" + typeStr + ">");
		writer.startTag(null, typeStr);
		if (name != null) {
			writer.attribute(null, "name", name);
		}
		writer.attribute(null, "value", data.toString());
		writer.endTag(null, typeStr);
		return true;
	}


	/**
	 * Map書き込み用XMLライタ
	 * @param name Name
	 * @param map Map
	 * @return 書き込み成功時true
	 */
	private final boolean writeMapXml(String name, Map<String, Object> map, XmlSerializer writer) throws XmlPullParserException, IOException {
		if (I) Log.i(TAG, "writeMapXml(): " + mFilename + ": name=[" + name + "]");

		if (map == null) {
			writer.startTag(null, "null");
			writer.endTag(null, "null");
			return true;
		}

		writer.startTag(null, "map");
		if (name != null) {
			writer.attribute(null, "name", name);
		}

		Set<?> s = map.entrySet();
		Iterator<?> i = s.iterator();
		while (i.hasNext()) {
			@SuppressWarnings("rawtypes")
			Map.Entry entry = (Map.Entry)i.next();
			final String entryname = (String)entry.getKey();
			final Object entrydata = entry.getValue();
			if (I) Log.i(TAG, "writeMapXml(): map-key=[" + entryname + "]");
			writeValueXml(entryname, entrydata, writer);
		}
		
		writer.endTag(null, "map");
		return true;
	}

}
