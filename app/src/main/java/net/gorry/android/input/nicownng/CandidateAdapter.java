
package net.gorry.android.input.nicownng;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.BaseAdapter;
import android.widget.TextView;


/** */
public class CandidateAdapter extends BaseAdapter {
    private TextCandidatesViewManager context;

    private ArrayList<WnnWord> mWnnWordArray;
	private ArrayList<Integer> mWnnWordTextLength;
	private ArrayList<Integer> mWnnWordOccupyCount;
    
	/* constractor */
    public CandidateAdapter(TextCandidatesViewManager c) {
        context = c;
        mWnnWordArray = new ArrayList<WnnWord>();
		mWnnWordTextLength = new ArrayList<Integer>();
		mWnnWordOccupyCount = new ArrayList<Integer>();
    }

	/* entry data */
    public void addData(int index, WnnWord word, Integer length, Integer count) {
		mWnnWordArray.add(index, word);
		mWnnWordTextLength.add(index, length);
		mWnnWordOccupyCount.add(index, count);
    }
	/* replace data */
    public void replaceData(int index, WnnWord word, Integer length, Integer count) {
		mWnnWordArray.add(index, word);
		mWnnWordTextLength.add(index, length);
		mWnnWordOccupyCount.add(index, count);
    }
	/* clear data */
	public void clearData() {
		mWnnWordArray.clear();
		mWnnWordTextLength.clear();
		mWnnWordOccupyCount.clear();
	}

    public int getCount() {
		return mWnnWordArray.size();
    }
    
    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

	/*
	 * 
	 */
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView textView;
        if (convertView == null) {
            textView = this.context.createCandidateView();
		}
		else{
            textView=(TextView)convertView;
        }
        textView.setText(mWnnWordArray.get(position).candidate);
        textView.setId(position+1);
        textView.setVisibility(View.VISIBLE);
        return textView;

    }
}
