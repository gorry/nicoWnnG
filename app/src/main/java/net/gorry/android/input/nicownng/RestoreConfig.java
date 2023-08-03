/**
 * 
 */
package net.gorry.android.input.nicownng;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.widget.Toast;

/**
 * @author gorry
 *
 */
public class RestoreConfig extends DialogPreference {

	private static final String sFileName = "nicoWnnG/system.setting";
	private Context me;

	public RestoreConfig(final Context context, final AttributeSet attrs) {
		super(context, attrs);
		me = context;
	}

	public RestoreConfig(final Context context) {
		this(context, null);
	}

	@Override
	protected void onDialogClosed(final boolean positiveResult) {
		super.onDialogClosed(positiveResult);
		
		if (positiveResult) {
			if (NicoWnnGJAJP.getInstance() == null) {
				new NicoWnnGJAJP(me);
			}
			NicoWnnGJAJP wnn = NicoWnnGJAJP.getInstance();
			ConfigBackup p = new ConfigBackup();
			String path = Environment.getExternalStorageDirectory().toString();
			String filename = path + "/" + sFileName;

			try {
				p.Restore(wnn, filename);
				SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(me);
				NicoWnnGControlPanelJAJP n = NicoWnnGControlPanelJAJP.getInstance();
				n.copyInPreferences(n.isLandscape(), pref);
				Toast.makeText(
						me.getApplicationContext(),
						R.string.toast_config_restore_success,
						Toast.LENGTH_LONG
					).show();
				n.finish();
			} catch (final Exception e) {
				Toast.makeText(
					me.getApplicationContext(),
					R.string.toast_config_restore_failed,
					Toast.LENGTH_LONG
				).show();
			}
		}
	}
}
