package net.gorry.android.input.nicownng;

import java.io.File;
import java.util.HashMap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * @author gorry
 *
 */
public class SelectTxtFileAdapter extends BaseAdapter {
	private final LayoutInflater mInflater;
	private final File[] mFiles;
	private final String[] mTypes;
	private final String[] mDescs;
	private final HashMap<String, String> typeTable = new HashMap<String, String>();

	private final Bitmap mFolderIcon;
	private final Bitmap mTxtIcon;
	private final int mHighlightPos;

	/**
	 * @param context context
	 * @param files files
	 * @param pos pos
	 */
	public SelectTxtFileAdapter(final Context context, final File[] files, final int pos) {
		mHighlightPos = ((pos < 0) ? 0 : pos);
		mInflater = LayoutInflater.from(context);

		mFiles = files;
		mTypes = new String[files.length];
		mDescs = new String[files.length];

		typeTable.put("txt", "text/plain");

		for (int i=0; i<files.length; i++) {
			final File file = files[i];

			if (file.isDirectory()) {
				mTypes[i] = "text/directory";
				continue;
			}

			final String filename = file.getName();
			final int extpos = filename.lastIndexOf('.');
			if (extpos >= 0) {
				final String ext = filename.substring(extpos + 1);
				mTypes[i] = typeTable.get(ext);
			} else {
				mTypes[i] = null;
			}
		}

		mFolderIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_folder);
		mTxtIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_txt);
	}

	public int getCount() {
		return mFiles.length;
	}

	public Object getItem(final int position) {
		return position;
	}

	public long getItemId(final int position) {
		return position;
	}

	static class ViewHolder {
		TextView name;
		TextView desc;
		ImageView icon;
	}

	/**
	 * @param position position
	 * @return type
	 */
	public String getType(final int position) {
		if ((position < 0) || (position >= mTypes.length)) return null;
		return mTypes[position];
	}

	public View getView(final int position, final View convertView, final ViewGroup parent) {
		ViewHolder holder;
		View v;
		final File file = mFiles[position];
		Bitmap b;

		if (convertView == null) {
			v = mInflater.inflate(R.layout.selecttxtfileadapter, null);

			holder = new ViewHolder();
			holder.name = (TextView)v.findViewById(R.id.firstLine);
			holder.desc = (TextView)v.findViewById(R.id.secondLine);
			holder.icon = (ImageView)v.findViewById(R.id.icon);

			v.setTag(holder);
		} else {
			v = convertView;
			holder = (ViewHolder) convertView.getTag();
		}

		String viewName = file.getName();
		if (file.isDirectory() || (file.getName().equals(".."))) {
			b = mFolderIcon;
			viewName += "/";
			holder.name.setText(viewName);
			holder.name.setTextColor(Color.CYAN);
			holder.desc.setText("");
			holder.desc.setVisibility(View.GONE);
		} else {
			b = mTxtIcon;
			holder.name.setText(viewName);
			holder.name.setTextColor(Color.WHITE);
			holder.desc.setText(mDescs[position]);
			holder.desc.setVisibility(View.VISIBLE);
		}
		holder.icon.setImageBitmap(b);
		if (position == mHighlightPos) {
			v.setBackgroundColor(Color.argb(128,128,128,128));
		} else {
			v.setBackgroundColor(Color.TRANSPARENT);
		}

		return v;
	}

}