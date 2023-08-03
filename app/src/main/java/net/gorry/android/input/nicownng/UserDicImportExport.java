package net.gorry.android.input.nicownng;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;


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
			result = importUserDic(params[1], params[2], params[3]);
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
			result = importMSIMEDic(params[1], "MS932");
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
			exportUserDic(params[1], params[2], params[3]);
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
	private boolean importMSIMEDic(final String wordsFileName, final String encode) {
		// import words dic
		try {
			final File fileWords = new File(wordsFileName);
			Log.d("load", "open fileWords\n");
			final BufferedReader finWords = new BufferedReader(new InputStreamReader(new FileInputStream(fileWords), encode));
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
	private boolean importUserDic(final String wordsFileName, final String learnFileName_sd, final String learnFileName_flash) {
		final File fileSdCard = getExternalStorageDirectory();
		if (null == fileSdCard) {
			return false;
		}
		final File fileBase = createNicoWnnGDirectory(fileSdCard);
		if (null == fileBase) {
			return false;
		}
		// import learn dic
		final File sFile = new File(fileBase, learnFileName_sd);
		final File dFile = new File(learnFileName_flash);
		if (false == copyFile(dFile, sFile)) {
			return false;
		}
		Log.d("load", "finish import learn dic!!\n");

		// import words dic
		try {
			final File fileWords = new File(fileBase, wordsFileName);
			// Log.d("load", "open fileWords\n");
			final FileInputStream finWords = new FileInputStream(fileWords);
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
	private boolean exportUserDic(final String wordsFileName, final String learnFileName_flash, final String learnFileName_sd) {
		mResultString[0] = "true";
		mResultString[1] = mActivity.getString(R.string.dialog_export_dic_message_done);

		final File fileSdCard = getExternalStorageDirectory();
		if (null == fileSdCard) {
			mResultString[0] = "false";
			mResultString[1] = mActivity.getString(R.string.dialog_export_dic_message_failed);
			return false;
		}
		final File fileBase = createNicoWnnGDirectory(fileSdCard);
		if (null == fileBase) {
			return false;
		}
		try {
			final File fileSave = new File(fileBase, wordsFileName);
			final FileOutputStream fout = new FileOutputStream(fileSave);
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
			fout.write(end.getBytes());
		} catch (final Exception e) {
			mResultString[0] = "false";
			mResultString[1] = mActivity.getString(R.string.dialog_export_dic_message_failed);
			return false;
		} finally {
			//
		}
		// export learn dic
		final File sFile = new File(learnFileName_flash);
		final File dFile = new File(fileBase, learnFileName_sd);
		copyFile(dFile, sFile);
		return true;
	}
	/*************************************************************************************/
	/* file load/save                                                                    */
	/*************************************************************************************/
	/*
	 *
	 */
	private File getExternalStorageDirectory() {
		final boolean state = Environment.getExternalStorageState().contains(Environment.MEDIA_MOUNTED);
		if (false == state) {
			Log.w("sdcard", "not mount sdcard!!\n");
			return null;
		}
		// Log.d("sdcard", "mount sdcard!!\n");
		return Environment.getExternalStorageDirectory();
	}
	/*
	 *
	 */
	private File createNicoWnnGDirectory(final File fileSdCard) {
		boolean result;
		File fileOld  = null;
		File fileNico = null;
		fileOld  = new File(fileSdCard, "OpenWnn");
		fileNico = new File(fileSdCard, "nicoWnnG");
		if (!fileOld.exists()) {
			fileOld  = new File(fileSdCard, "NicoWnnG");
		}
		if (fileOld.exists()) {
			result = fileOld.renameTo(fileNico);
			if (false == result) {
				mResultString[0] = "false";
				mResultString[1] = mActivity.getString(R.string.dialog_importexport_rendir_failed);
				return null;
			}
			return fileNico;
		}
		if (!fileNico.exists()) {
			if (!fileNico.mkdir()) {
				mResultString[0] = "false";
				mResultString[1] = mActivity.getString(R.string.dialog_export_dic_message_failed);
				return null;
			} // mkdir
		} // exists
		return fileNico;
	}
	/*
	 *
	 */
	private boolean copyFile(final File dFile, final File sFile) {
		if (!sFile.exists()) {
			return false;
		}
		InputStream input = null;
		OutputStream output = null;
		try {
			input  = new FileInputStream(sFile);
			output = new FileOutputStream(dFile);
			final int DEFAULT_BUFFER_SIZE = 1024 * 4;
			final byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
			int n = 0;
			while (-1 != (n = input.read(buffer))) {
				output.write(buffer, 0, n);
			}
			input.close();
			output.close();
		} catch (final Exception e) {
			return false;
		}
		return true;
	}
}
/****************************** end of file ******************************/
