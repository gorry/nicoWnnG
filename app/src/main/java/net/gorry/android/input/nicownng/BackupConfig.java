/**
 * 
 */
package net.gorry.android.input.nicownng;

import android.content.Context;
import android.os.Environment;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.widget.Toast;

/**
 * @author gorry
 *
 */
public class BackupConfig extends DialogPreference {

	private static final String sFileName = "nicoWnnG/system.setting";
	private Context me;
	
	/**
	 * @param context context
	 * @param attrs attrs
	 */
	public BackupConfig(final Context context, final AttributeSet attrs) {
		super(context, attrs);
		me = context;
	}

	/**
	 * @param context context
	 */
	public BackupConfig(final Context context) {
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
				p.Backup(wnn, filename);
				Toast.makeText(
					me.getApplicationContext(),
					R.string.toast_config_backup_success,
					Toast.LENGTH_LONG
				).show();
			} catch (final Exception e) {
				Toast.makeText(
					me.getApplicationContext(),
					R.string.toast_config_backup_failed,
					Toast.LENGTH_LONG
				).show();
			}
		}
	}
}
