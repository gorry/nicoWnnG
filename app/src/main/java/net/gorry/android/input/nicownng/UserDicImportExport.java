package net.gorry.android.input.nicownng;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import androidx.documentfile.provider.DocumentFile;


public class UserDicImportExport extends AsyncTask<String, String, String[]>{
	private final UserDictionaryToolsList mActivity;
	private String[]  mResultString;
	private int mDialogMode;
	private int mDialogCount;
	private int mDialogUpdateCount;
	private ProgressDialog mProgressDialog = null;


	public UserDicImportExport(final UserDictionaryToolsList activity, int dialogMode) {
		mActivity  = activity;
		mDialogMode = dialogMode;
	}
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		mProgressDialog = mActivity.createProgressDialog(mDialogMode);
	}

	@Override
	protected String[] doInBackground(final String... params)  {
		boolean result = false;
		mResultString = new String[2];
		if (params[0].equals("import")) {
			result = importUserDic(Uri.parse(params[1]), Uri.parse(params[2]), params[3]);
			if (true == result) {
				mResultString[0] = "true";
				mResultString[1] = mActivity.getString(R.string.dialog_import_dic_message_done);
			}
			else{
				mResultString[0] = "false";
				mResultString[1] = mActivity.getString(R.string.dialog_import_dic_message_failed);
			}
		}
		else if (params[0].equals("import_msime")) {
			result = importMSIMEDic(Uri.parse(params[1]), "MS932");
			if (true == result) {
				mResultString[0] = "true";
				mResultString[1] = mActivity.getString(R.string.dialog_import_textdic_message_done);
			}
			else{
				mResultString[0] = "false";
				mResultString[1] = mActivity.getString(R.string.dialog_import_textdic_message_failed);
			}
		}
		else if (params[0].equals("export")) {
			exportUserDic(params[1], Uri.parse(params[2]), Uri.parse(params[3]));
		}
		return mResultString;
	}
	@Override
	protected void onPostExecute(final String[] result) {
		mActivity.removeProgressDialog(result);
	}
	@Override
	protected void onCancelled() {
		super.onCancelled();
	}
	@Override
	protected void onProgressUpdate(final String... values) {
		String s;
		if (mDialogMode == 3) {
			s = mActivity.getDialogMessage()+"(" + Integer.toString(mDialogUpdateCount) + "/" + Integer.toString(mDialogCount) + ")";
		} else {
			s = mActivity.getDialogMessage()+"(" + Integer.toString(mDialogCount) + ")";
		}
		mProgressDialog.setMessage(s);
	}

	private final Runnable onUpdateDictionary = new Runnable() {
		public void run() {
			publishProgress();
			mDialogUpdateCount += 100;
		}
	};
		
	/*
	 *
	 */
	private boolean importMSIMEDic(final Uri wordsFileName, final String encode) {
		// import words dic
		try {
			ContentResolver cr = mActivity.getContentResolver();
			final InputStream istream = cr.openInputStream(wordsFileName);
			Log.d("load", "open fileWords\n");
			final BufferedReader finWords = new BufferedReader(new InputStreamReader(istream, encode));
			String line;
			mDialogCount = 0;
			mDialogUpdateCount = 0;
			final ArrayList<WnnWord> words = new ArrayList<WnnWord>();
			
			while ((line = finWords.readLine()) != null) {
				// 行頭の空白を除去
				line.replaceFirst("^[ \t]+", "");

				// "!"で始まる行はスキップ
				if (line.matches("^!")) continue;
				
				// タブで分割
				String[] w = line.split("\t");

				// ２つ以上の項目がない行はスキップ
				if (w.length < 2) continue;
				
				// 登録
				final WnnWord newword = new WnnWord();
				newword.stroke = w[0];
				newword.candidate = w[1];
				words.add(newword);

				mDialogCount++;
				if ((mDialogCount%100) == 0) {
					publishProgress();
					/*
					final WnnWord[] wordsarray = words.toArray(new WnnWord[words.size()]);
					final boolean result = mActivity.addImportWords(wordsarray);
					words.clear();
					if (false == result) {
						Log.d("load", "cannot import word\n");
						break;
					}
					*/
				}
				// Log.d("read", count+": " + w[0] + " : "+ w[1]);
			}
			mActivity.runOnUiThread(new Runnable(){ public void run() {
				mProgressDialog.dismiss();
				mDialogMode = 3;
				mProgressDialog = mActivity.createProgressDialog(mDialogMode);
			}});
			if (words.size() > 0) {
				final WnnWord[] wordsarray = words.toArray(new WnnWord[words.size()]);
				final boolean result = mActivity.addImportWords(wordsarray, onUpdateDictionary);
				if (false == result) {
					Log.d("load", "cannot import word\n");
				}
			}
		} catch (final Exception e) {
			return false;
		} finally {
			//
		}
		Log.d("load", "finish import!!\n");
		return true;
	}

	/*
	 *
	 */
	private boolean importUserDic(final Uri inWordsFileName, final Uri inLearnFileName, final String outLearnFileName) {
		NicoWnnGJAJP wnn = NicoWnnGJAJP.getInstance();
		ContentResolver cr = mActivity.getContentResolver();

		// import learn dic
		try {
			final InputStream istream = cr.openInputStream(inLearnFileName);
			final FileOutputStream ostream = new FileOutputStream(outLearnFileName);
			byte[] buf = new byte[1024*16];
			while (true) {
				int size = istream.read(buf);
				if (size < 0) break;
				ostream.write(buf, 0, size);
			}
			istream.close();
			ostream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

        // import words dic
		try {
			final InputStream finWords = cr.openInputStream(inWordsFileName);
			// Log.d("load", "open fileWords\n");
			// Log.d("load", "open finWordsm\n");
			final XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			final XmlPullParser parser = factory.newPullParser();
			parser.setInput(finWords, "UTF8");
			// Log.d("load", "create XML parser\n");
			int eventType;
			String tagName;
			final String tagText;
			mDialogCount = 0;
			for(eventType = parser.getEventType(); eventType != XmlPullParser.END_DOCUMENT; eventType = parser.next()){
				tagName = parser.getName();
				if (eventType != XmlPullParser.START_TAG)    continue;
				if (tagName == null)    continue;
				if (!tagName.equals("dicword"))    continue;
				// entry word
				final WnnWord newword = new WnnWord();
				newword.stroke    = parser.getAttributeValue(null,"stroke").replaceAll("\"", "");
				// Log.d("load", "get stroke = "+newword.stroke+"\n");
				do {
					eventType = parser.next();
					if (eventType == XmlPullParser.TEXT) {
						newword.candidate = parser.getText().replaceAll("\"", "");;
						// Log.d("load", "get candidate = "+newword.candidate+"\n");
						break;
					}
				} while (eventType == XmlPullParser.END_DOCUMENT);
				if (eventType == XmlPullParser.END_DOCUMENT) {
					break;
				}
				final boolean result = mActivity.addImportWord(newword);
				if (false == result) {
					Log.e("load", "cannot import word ["+newword+"]");
					break;
				}

				mDialogCount++;
				// Log.d("load", count+": " + newword.stroke + " : "+ newword.candidate);
			}
		} catch (final Exception e) {
			return false;
		} finally {
			//
		}
		Log.d("load", "finish import!!\n");
		return true;
	}
	/*
	 *
	 */
	private boolean exportUserDic(final String inLearnFileName, final Uri outWordsFileName, final Uri outLearnFileName) {
		NicoWnnGJAJP wnn = NicoWnnGJAJP.getInstance();
		ContentResolver cr = mActivity.getContentResolver();

		mResultString[0] = "false";
		mResultString[1] = mActivity.getString(R.string.dialog_export_dic_message_failed);

		try {
			final OutputStream fout = cr.openOutputStream(outWordsFileName);
			// output XML header
			final String header = new String("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
			final String top    = new String("<wordlist>\n");
			final String end    = new String("</wordlist>\n");

			fout.write(header.getBytes());
			fout.write(top.getBytes());
			// create data
			final int size = mActivity.getWordListSize();
			WnnWord getword;
			for (int iI = 0; iI < size; ++iI) {
				getword = mActivity.getWnnWord(iI);
				final String outstring = new String("  <dicword stroke=\"" + getword.stroke + "\">\"" + getword.candidate + "\"</dicword>\n");
				fout.write(outstring.getBytes());
			}
			fout.close();
			fout.write(end.getBytes());
		} catch (final Exception e) {
			mResultString[0] = "false";
			mResultString[1] = mActivity.getString(R.string.dialog_export_dic_message_failed);
			return false;
		} finally {
		}

		// export learn dic
		try {
			final FileInputStream istream = new FileInputStream(inLearnFileName);
			final OutputStream ostream = cr.openOutputStream(outLearnFileName);
			byte[] buf = new byte[1024*16];
			while (true) {
				int size = istream.read(buf);
				if (size < 0) break;
				ostream.write(buf, 0, size);
			}
			istream.close();
			ostream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		mResultString[0] = "true";
		mResultString[1] = mActivity.getString(R.string.dialog_export_dic_message_done);

		return true;
	}
}
/****************************** end of file ******************************/
