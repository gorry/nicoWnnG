/*****
 *
 */
package net.gorry.android.input.nicownng;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class Mushroom extends Activity {
	public static final String ACTION = "Mushroom";
	private Activity me;


	private static final String ACTION_INTERCEPT = "com.adamrocker.android.simeji.ACTION_INTERCEPT";
	private static final String CATEGORY_KEY = "com.adamrocker.android.simeji.REPLACE";
	private static final String REPLACE_KEY = "replace_key";

	private static final String GETACTION_INTERCEPT = "net.gorry.android.input.nicownng.ACTION_INTERCEPT";

	private String mGetString;

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onStart() {
		super.onStart();
		me = this;
		MyAppInfo.setContext(me);
		MyIcon.setContext(me);

		// setContentView(R.layout.mushroom);

		final Intent getintent = getIntent();
		final String action = getintent.getAction();
		if ((action == null) || !action.equals(GETACTION_INTERCEPT)) {
			returnActivityResult("");
			finish();
			return;
		}
		mGetString = getintent.getStringExtra(REPLACE_KEY);
		if (mGetString == null) {
			mGetString = "";
		}

		final Intent intent = new Intent(ACTION_INTERCEPT);
		intent.addCategory(CATEGORY_KEY);
		intent.putExtra(REPLACE_KEY, mGetString);
		final List<ResolveInfo> info = getPackageManager().queryIntentActivities(
				intent, PackageManager.MATCH_DEFAULT_ONLY
		);
		if (info.size() == 0) {
			Toast.makeText(me.getApplicationContext(), R.string.mushroom_error_notfound,
					Toast.LENGTH_LONG).show();
			returnActivityResult("");
			finish();
			return;
		}

		// パッケージ名一覧を作成
		final ArrayList<String> pkgName1 = new ArrayList<String>();
		final ArrayList<String> pkgName2 = new ArrayList<String>();
		final ArrayList<Integer> pkgId = new ArrayList<Integer>();
		{
			for (int i=0; i<info.size(); i++) {
				final ResolveInfo ri = info.get(i);
				final String packageName = MyAppInfo.getPackageName(ri);
				final String activityName = MyAppInfo.getActivityName(ri);
				final String pkgIdent = packageName + "-" + activityName;
				pkgName1.add(pkgIdent);
				pkgName2.add(pkgIdent);
			}
		}

		// 過去実行したパッケージを先に積む
		final AwaitAlertDialogIconList dlg = new AwaitAlertDialogIconList(me);
		{
			final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(me);
			final int numPackages = pref.getInt("mushroom_packagenames", 0);
			for (int i=0; i<numPackages; i++) {
				final String key = "mushroom_packagename_" + i;
				final String data = pref.getString(key, "");
				final int n = pkgName1.indexOf(data);
				if (n >= 0) {
					pkgName1.remove(n);
					final int id = pkgName2.indexOf(data);
					final ResolveInfo ri = info.get(id);
					final String appName = MyAppInfo.getAppName(ri);
					final String packageName = MyAppInfo.getPackageName(ri);
					final String activityName = MyAppInfo.getActivityName(ri);
					if ((appName != null) && (appName.length() > 0) && (packageName != null) && (packageName.length() > 0) && (activityName != null) && (activityName.length() > 0)) {
						final Drawable icon = MyAppInfo.getIcon(ri);
						dlg.addItem(appName, icon, 1);
						pkgId.add(id);
					}
				}
			}
		}

		// 未実行のパッケージを後に積む
		{
			for (int i=0; i<pkgName1.size(); i++) {
				final int id = pkgName2.indexOf(pkgName1.get(i));
				final ResolveInfo ri = info.get(id);
				final String appName = MyAppInfo.getAppName(ri);
				final String packageName = MyAppInfo.getPackageName(ri);
				final String activityName = MyAppInfo.getActivityName(ri);
				if ((appName != null) && (appName.length() > 0) && (packageName != null) && (packageName.length() > 0) && (activityName != null) && (activityName.length() > 0)) {
					final Drawable icon = MyAppInfo.getIcon(ri);
					dlg.addItem(appName, icon, 1);
					pkgId.add(id);
				}
			}
		}

		// リストから選択
		final String title = me.getString(R.string.mushroom_dialog_select_title);
		dlg.setTitle(title);
		dlg.setLongClickEnable(false);
		(me).runOnUiThread(new Runnable(){ public void run() {
			dlg.create();
			new Thread(new Runnable() {	public void run() {
				final int result = dlg.show();
				if (result != AwaitAlertDialogBase.CANCEL) {
					final int id = result - AwaitAlertDialogIconList.CLICKED;
					if ((0 <= id) && (id < pkgId.size())) {
						final ResolveInfo ri = info.get(pkgId.get(id));
						final String appName = MyAppInfo.getAppName(ri);
						final String packageName = MyAppInfo.getPackageName(ri);
						final String activityName = MyAppInfo.getActivityName(ri);
						if ((appName != null) && (appName.length() > 0) && (packageName != null) && (packageName.length() > 0) && (activityName != null) && (activityName.length() > 0)) {
							// パッケージ名一覧を保存
							{
								final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(me);
								final SharedPreferences.Editor editor = pref.edit();
								String pkgIdent = packageName + "-" + activityName;
								int n = 0;
								String key = "mushroom_packagename_" + n;
								n++;
								editor.putString(key, pkgIdent);
								pkgId.set(id, -1);
								for (int i=0; i<pkgId.size(); i++) {
									final int id2 = pkgId.get(i);
									if (id2 >= 0) {
										final ResolveInfo ri2 = info.get(id2);
										final String packageName2 = MyAppInfo.getPackageName(ri2);
										final String activityName2 = MyAppInfo.getActivityName(ri2);
										pkgIdent = packageName2 + "-" + activityName2;
										key = "mushroom_packagename_" + n;
										n++;
										editor.putString(key, pkgIdent);
									}
								}
								editor.putInt("mushroom_packagenames", pkgId.size());
								editor.commit();
							}

							// 実行
							final Intent intent2 = new Intent(ACTION_INTERCEPT);
							intent2.addCategory(CATEGORY_KEY);
							intent2.setClassName(packageName, activityName);
							intent2.putExtra(REPLACE_KEY, mGetString);
							// intent.setFlags(Intent2.FLAG_ACTIVITY_NEW_TASK);
							startActivityForResult(intent2, 0);
							return;
						}
					}
				}
				returnActivityResult("");
				finish();
				return;
			} }).start();
		}});
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override public void onActivityResult(final int req, final int result, final Intent intent) {
		String str = "";

		if ((req == 0) && (result == RESULT_OK)) {
			str = intent.getStringExtra("replace_key");
		}
		returnActivityResult(str);
		finish();
	}

	public void returnActivityResult(final String str) {
		// mWindow.setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
		final Intent ret = new Intent(ACTION);
		ret.putExtra("replace_key", str);
		sendBroadcast(ret);
	}

}
