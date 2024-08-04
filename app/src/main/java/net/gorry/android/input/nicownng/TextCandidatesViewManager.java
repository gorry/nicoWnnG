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

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Environment;
import android.os.Vibrator;
import androidx.preference.PreferenceManager;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.DynamicDrawableSpan;
import android.text.style.ImageSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.graphics.LightingColorFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuff.Mode;

/**
 * The default candidates view manager class using {@link EditText}.
 *
 * @author Copyright (C) 2009 OMRON SOFTWARE CO., LTD.  All Rights Reserved.
 */
public class TextCandidatesViewManager implements CandidatesViewManager, GestureDetector.OnGestureListener {
	private static final String TAG = "TextCandidatesViewMan";

	private static String M() {
		StackTraceElement[] es = new Exception().getStackTrace();
		int count = 1; while (es[count].getMethodName().contains("$")) count++;
		return es[count].getFileName()+"("+es[count].getLineNumber()+"): "+es[count].getMethodName()+"()";
	}

	private static final boolean T = false; // true;

	private Context me;

	/** Height of a line */
	// public static final int LINE_HEIGHT = 34;

	public static final int LINE_NUM_MAX      = 5;

	public static final int FULL_VIEW_DIV     = 10;

	/** Number of lines to display (Portrait) */
	//public static final int LINE_NUM_PORTRAIT       = 1;
	/** Number of lines to display (Landscape) */
	//public static final int LINE_NUM_LANDSCAPE      = 1;

	/** Maximum lines */
	private static final int DISPLAY_LINE_MAX_COUNT = 1000;
	/** Width of the view */
	private static final int CANDIDATE_MINIMUM_WIDTH = 48;
	/** Height of the view */
	//private static final int CANDIDATE_MINIMUM_HEIGHT = 42;
	/**
	 * candidate-line height
	 */
	private static int mCandidateViewHeightIndex = 0;
	public static int candidateViewDataTable[] = { 24, 33, 42, 51, 60, 69, 78, 87, 105, 123, };
	private static int mCandidateTextSizeIndex = 2;
	public static int candidateTextSizeTable[] = { 12, 16, 20, 24, 28, 32, 36, 40, 48, 56, };

	/** Maximum number of displaying candidates par one line (full view mode) */
	private static int mFullViewDiv = FULL_VIEW_DIV;

	/** Body view of the candidates list */
	private ViewGroup  mViewBody;
	/** Scroller of {@code mViewBodyText} */
	private ScrollView           mViewBodyVScroll2nd;
	private HorizontalScrollView mViewBodyHScroll;

	private boolean mIsScroll;
	/** Base of {@code mViewCandidateList1st}, {@code mViewCandidateList2nd} */
	private ViewGroup mViewCandidateBase;
	private ViewGroup mViewCandidateBase2;
	/** The view of the scaling up candidate */
	private View mViewScaleUp;
	/** Layout for the candidates list on normal view */
	private LinearLayout  mViewCandidateList1st;
	/** Layout for the candidates list on full view */
	private RelativeLayout mViewCandidateList2nd;

	private Button mViewCandidateButtonFullList;

	/** {@link NicoWnnG} instance using this manager */
	private NicoWnnG mWnn;
	/** View type (VIEW_TYPE_NORMAL or VIEW_TYPE_FULL or VIEW_TYPE_CLOSE) */
	private int mViewType;
	/** Portrait display({@code true}) or landscape({@code false}) */
	private boolean mPortrait;

	/** Width of the view */
	public int mViewWidth;
	/** Height of the view */
	private int mViewHeight;
	/** Whether hide the view if there is no candidates */
	private boolean mAutoHideMode;
	/** The converter to be get candidates from and notice the selected candidate to. */
	public WnnEngine mConverter;

	/** Number of candidates displaying */
	int mWordCount;
	int[] mWnnLineCount    = new int[LINE_NUM_MAX];
	int[] mWnnLineCountMax = new int[LINE_NUM_MAX];
	private final int[] mWnnLineOffset   = new int[LINE_NUM_MAX];

	private int mMaxLine;

	private boolean mSpaceBelowCandidate;

	private boolean mCandidateVertical;

	/** get last id of topline */
	private int mToplineLastId;

	private int mTotalLastId;
	/** List of candidates */
	private final ArrayList<WnnWord> mWnnWordArray = new ArrayList<WnnWord>();
	private final ArrayList<Integer> mWnnWordTextLength = new ArrayList<Integer>();
	private final ArrayList<Integer> mWnnWordOccupyCount = new ArrayList<Integer>();
	private final ArrayList<Integer> mWnnWordTextLength2 = new ArrayList<Integer>();
	private final ArrayList<Integer> mWnnWordOccupyCount2 = new ArrayList<Integer>();

	/** Gesture detector */
	private GestureDetector mGestureDetector;
	/** The word pressed */
	private WnnWord mWord;
	/** Number of lines displayed */
	private int mLineCount = 1;

	/** {@code true} if the candidate delete state is selected */
	private boolean mIsScaleUp = false;

	/** {@code true} if the full screen mode is selected */
	private boolean mIsFullView = false;

	private boolean mIsCreateFullView = false;

	private boolean mIsLockHScroll = false;

	private int mTargetScrollWidth;

	/** The event object for "touch" */
	private MotionEvent mMotionEvent = null;

	/** {@code true} if there are more candidates to display. */
	private boolean mCanReadMore = false;
	/** Width of {@code mReadMoreButton} */
	private int mTextColor = 0;
	private int mTextBackgroundColor = 0;
	private int mTextBackgroundSelectColor = 0;
	/** Template object for each candidate and normal/full view change button */
	private TextView mViewCandidateTemplate;
	/** Number of candidates in full view */
	private int mFullViewWordCount;
	/** Number of candidates in the current line (in full view) */
	private int mFullViewOccupyCount;
	/** View of the previous candidate (in full view) */
	private TextView mFullViewPrevView;
	/** Id of the top line view (in full view) */
	private int mFullViewPrevLineTopId;
	/** Layout of the previous candidate (in full view) */
	private RelativeLayout.LayoutParams mFullViewPrevParams;
	/** Whether all candidates is displayed */
	private boolean mCreateCandidateDone;
	/** general infomation about a display */
	private static final DisplayMetrics mMetrics = new DisplayMetrics();

	private final boolean mIsCandidateActive = false;

	/* asynctask */
	private TextCandidateTask mCandidateTask = null;
	private boolean   mIsActiveTask = false;
	private boolean   mIsCancelTask = false;

	public int mCandidateViewWidth = 0;
	private int mShowCandidateLines = 0;
	private int mLastShowCandidateLines = 0;
	private boolean mShrinkCandidateLines = false;

	private LinearLayout mCandidateSpaceView = null;

	private boolean mShowCandidateButtonFullList = false;
	private final int mCandidateButtonFullListWidth = 0;

	private int mWnnWordConvertPos = 0;

	// docomo emoji hashmap
	private static final HashMap<String, Integer> DOCOMO_EMOJI_TABLE = new HashMap<String, Integer>() {/**
	 *
	 */
		private static final long serialVersionUID = 1L;

		{
			put("\uE63E", R.drawable.docomo_1); put("\uE63F", R.drawable.docomo_2); put("\uE640", R.drawable.docomo_3); put("\uE641", R.drawable.docomo_4);
			put("\uE642", R.drawable.docomo_5); put("\uE643", R.drawable.docomo_6); put("\uE644", R.drawable.docomo_7); put("\uE645", R.drawable.docomo_8);
			put("\uE646", R.drawable.docomo_9); put("\uE647", R.drawable.docomo_10); put("\uE648", R.drawable.docomo_11); put("\uE649", R.drawable.docomo_12);
			put("\uE64A", R.drawable.docomo_13); put("\uE64B", R.drawable.docomo_14); put("\uE64C", R.drawable.docomo_15); put("\uE64D", R.drawable.docomo_16);
			put("\uE64E", R.drawable.docomo_17); put("\uE64F", R.drawable.docomo_18); put("\uE650", R.drawable.docomo_19); put("\uE651", R.drawable.docomo_20);
			put("\uE652", R.drawable.docomo_21); put("\uE653", R.drawable.docomo_22); put("\uE654", R.drawable.docomo_23); put("\uE655", R.drawable.docomo_24);
			put("\uE656", R.drawable.docomo_25); put("\uE657", R.drawable.docomo_26); put("\uE658", R.drawable.docomo_27); put("\uE659", R.drawable.docomo_28);
			put("\uE65A", R.drawable.docomo_29); put("\uE65B", R.drawable.docomo_30); put("\uE65C", R.drawable.docomo_31); put("\uE65D", R.drawable.docomo_32);
			put("\uE65E", R.drawable.docomo_33); put("\uE65F", R.drawable.docomo_34); put("\uE660", R.drawable.docomo_35); put("\uE661", R.drawable.docomo_36);
			put("\uE662", R.drawable.docomo_37); put("\uE663", R.drawable.docomo_38); put("\uE664", R.drawable.docomo_39); put("\uE665", R.drawable.docomo_40);
			put("\uE666", R.drawable.docomo_41); put("\uE667", R.drawable.docomo_42); put("\uE668", R.drawable.docomo_43); put("\uE669", R.drawable.docomo_44);
			put("\uE66A", R.drawable.docomo_45); put("\uE66B", R.drawable.docomo_46); put("\uE66C", R.drawable.docomo_47); put("\uE66D", R.drawable.docomo_48);
			put("\uE66E", R.drawable.docomo_49); put("\uE66F", R.drawable.docomo_50); put("\uE670", R.drawable.docomo_51); put("\uE671", R.drawable.docomo_52);
			put("\uE672", R.drawable.docomo_53); put("\uE673", R.drawable.docomo_54); put("\uE674", R.drawable.docomo_55); put("\uE675", R.drawable.docomo_56);
			put("\uE676", R.drawable.docomo_57); put("\uE677", R.drawable.docomo_58); put("\uE678", R.drawable.docomo_59); put("\uE679", R.drawable.docomo_60);
			put("\uE67A", R.drawable.docomo_61); put("\uE67B", R.drawable.docomo_62); put("\uE67C", R.drawable.docomo_63); put("\uE67D", R.drawable.docomo_64);
			put("\uE67E", R.drawable.docomo_65); put("\uE67F", R.drawable.docomo_66); put("\uE680", R.drawable.docomo_67); put("\uE681", R.drawable.docomo_68);
			put("\uE682", R.drawable.docomo_69); put("\uE683", R.drawable.docomo_70); put("\uE684", R.drawable.docomo_71); put("\uE685", R.drawable.docomo_72);
			put("\uE686", R.drawable.docomo_73); put("\uE687", R.drawable.docomo_74); put("\uE688", R.drawable.docomo_75); put("\uE689", R.drawable.docomo_76);
			put("\uE68A", R.drawable.docomo_77); put("\uE68B", R.drawable.docomo_78); put("\uE68C", R.drawable.docomo_79); put("\uE68D", R.drawable.docomo_80);
			put("\uE68E", R.drawable.docomo_81); put("\uE68F", R.drawable.docomo_82); put("\uE690", R.drawable.docomo_83); put("\uE691", R.drawable.docomo_84);
			put("\uE692", R.drawable.docomo_85); put("\uE693", R.drawable.docomo_86); put("\uE694", R.drawable.docomo_87); put("\uE695", R.drawable.docomo_88);
			put("\uE696", R.drawable.docomo_89); put("\uE697", R.drawable.docomo_90); put("\uE698", R.drawable.docomo_91); put("\uE699", R.drawable.docomo_92);
			put("\uE69A", R.drawable.docomo_93); put("\uE69B", R.drawable.docomo_94); put("\uE69C", R.drawable.docomo_95); put("\uE69D", R.drawable.docomo_96);
			put("\uE69E", R.drawable.docomo_97); put("\uE69F", R.drawable.docomo_98); put("\uE6A0", R.drawable.docomo_99); put("\uE6A1", R.drawable.docomo_100);
			put("\uE6A2", R.drawable.docomo_101); put("\uE6A3", R.drawable.docomo_102); put("\uE6A4", R.drawable.docomo_103); put("\uE6A5", R.drawable.docomo_104);
			put("\uE6CE", R.drawable.docomo_105); put("\uE6CF", R.drawable.docomo_106); put("\uE6D0", R.drawable.docomo_107); put("\uE6D1", R.drawable.docomo_108);
			put("\uE6D2", R.drawable.docomo_109); put("\uE6D3", R.drawable.docomo_110); put("\uE6D4", R.drawable.docomo_111); put("\uE6D5", R.drawable.docomo_112);
			put("\uE6D6", R.drawable.docomo_113); put("\uE6D7", R.drawable.docomo_114); put("\uE6D8", R.drawable.docomo_115); put("\uE6D9", R.drawable.docomo_116);
			put("\uE6DA", R.drawable.docomo_117); put("\uE6DB", R.drawable.docomo_118); put("\uE6DC", R.drawable.docomo_119); put("\uE6DD", R.drawable.docomo_120);
			put("\uE6DE", R.drawable.docomo_121); put("\uE6DF", R.drawable.docomo_122); put("\uE6E0", R.drawable.docomo_123); put("\uE6E1", R.drawable.docomo_124);
			put("\uE6E2", R.drawable.docomo_125); put("\uE6E3", R.drawable.docomo_126); put("\uE6E4", R.drawable.docomo_127); put("\uE6E5", R.drawable.docomo_128);
			put("\uE6E6", R.drawable.docomo_129); put("\uE6E7", R.drawable.docomo_130); put("\uE6E8", R.drawable.docomo_131); put("\uE6E9", R.drawable.docomo_132);
			put("\uE6EA", R.drawable.docomo_133); put("\uE6EB", R.drawable.docomo_134); put("\uE70B", R.drawable.docomo_135); put("\uE6EC", R.drawable.docomo_136);
			put("\uE6ED", R.drawable.docomo_137); put("\uE6EE", R.drawable.docomo_138); put("\uE6EF", R.drawable.docomo_139); put("\uE6F0", R.drawable.docomo_140);
			put("\uE6F1", R.drawable.docomo_141); put("\uE6F2", R.drawable.docomo_142); put("\uE6F3", R.drawable.docomo_143); put("\uE6F4", R.drawable.docomo_144);
			put("\uE6F5", R.drawable.docomo_145); put("\uE6F6", R.drawable.docomo_146); put("\uE6F7", R.drawable.docomo_147); put("\uE6F8", R.drawable.docomo_148);
			put("\uE6F9", R.drawable.docomo_149); put("\uE6FA", R.drawable.docomo_150); put("\uE6FB", R.drawable.docomo_151); put("\uE6FC", R.drawable.docomo_152);
			put("\uE6FD", R.drawable.docomo_153); put("\uE6FE", R.drawable.docomo_154); put("\uE6FF", R.drawable.docomo_155); put("\uE700", R.drawable.docomo_156);
			put("\uE701", R.drawable.docomo_157); put("\uE702", R.drawable.docomo_158); put("\uE703", R.drawable.docomo_159); put("\uE704", R.drawable.docomo_160);
			put("\uE705", R.drawable.docomo_161); put("\uE706", R.drawable.docomo_162); put("\uE707", R.drawable.docomo_163); put("\uE708", R.drawable.docomo_164);
			put("\uE709", R.drawable.docomo_165); put("\uE70A", R.drawable.docomo_166); put("\uE6AC", R.drawable.docomo_167); put("\uE6AD", R.drawable.docomo_168);
			put("\uE6AE", R.drawable.docomo_169); put("\uE6B1", R.drawable.docomo_170); put("\uE6B2", R.drawable.docomo_171); put("\uE6B3", R.drawable.docomo_172);
			put("\uE6B7", R.drawable.docomo_173); put("\uE6B8", R.drawable.docomo_174); put("\uE6B9", R.drawable.docomo_175); put("\uE6BA", R.drawable.docomo_176);
			put("\uE70C", R.drawable.docomo_ex1); put("\uE70D", R.drawable.docomo_ex2); put("\uE70E", R.drawable.docomo_ex3); put("\uE70F", R.drawable.docomo_ex4);
			put("\uE710", R.drawable.docomo_ex5); put("\uE711", R.drawable.docomo_ex6); put("\uE712", R.drawable.docomo_ex7); put("\uE713", R.drawable.docomo_ex8);
			put("\uE714", R.drawable.docomo_ex9); put("\uE715", R.drawable.docomo_ex10); put("\uE716", R.drawable.docomo_ex11); put("\uE717", R.drawable.docomo_ex12);
			put("\uE718", R.drawable.docomo_ex13); put("\uE719", R.drawable.docomo_ex14); put("\uE71A", R.drawable.docomo_ex15); put("\uE71B", R.drawable.docomo_ex16);
			put("\uE71C", R.drawable.docomo_ex17); put("\uE71D", R.drawable.docomo_ex18); put("\uE71E", R.drawable.docomo_ex19); put("\uE71F", R.drawable.docomo_ex20);
			put("\uE720", R.drawable.docomo_ex21); put("\uE721", R.drawable.docomo_ex22); put("\uE722", R.drawable.docomo_ex23); put("\uE723", R.drawable.docomo_ex24);
			put("\uE724", R.drawable.docomo_ex25); put("\uE725", R.drawable.docomo_ex26); put("\uE726", R.drawable.docomo_ex27); put("\uE727", R.drawable.docomo_ex28);
			put("\uE728", R.drawable.docomo_ex29); put("\uE729", R.drawable.docomo_ex30); put("\uE72A", R.drawable.docomo_ex31); put("\uE72B", R.drawable.docomo_ex32);
			put("\uE72C", R.drawable.docomo_ex33); put("\uE72D", R.drawable.docomo_ex34); put("\uE72E", R.drawable.docomo_ex35); put("\uE72F", R.drawable.docomo_ex36);
			put("\uE730", R.drawable.docomo_ex37); put("\uE731", R.drawable.docomo_ex38); put("\uE732", R.drawable.docomo_ex39); put("\uE733", R.drawable.docomo_ex40);
			put("\uE734", R.drawable.docomo_ex41); put("\uE735", R.drawable.docomo_ex42); put("\uE736", R.drawable.docomo_ex43); put("\uE737", R.drawable.docomo_ex44);
			put("\uE738", R.drawable.docomo_ex45); put("\uE739", R.drawable.docomo_ex46); put("\uE73A", R.drawable.docomo_ex47); put("\uE73B", R.drawable.docomo_ex48);
			put("\uE73C", R.drawable.docomo_ex49); put("\uE73D", R.drawable.docomo_ex50); put("\uE73E", R.drawable.docomo_ex51); put("\uE73F", R.drawable.docomo_ex52);
			put("\uE740", R.drawable.docomo_ex53); put("\uE741", R.drawable.docomo_ex54); put("\uE742", R.drawable.docomo_ex55); put("\uE743", R.drawable.docomo_ex56);
			put("\uE744", R.drawable.docomo_ex57); put("\uE745", R.drawable.docomo_ex58); put("\uE746", R.drawable.docomo_ex59); put("\uE747", R.drawable.docomo_ex60);
			put("\uE748", R.drawable.docomo_ex61); put("\uE749", R.drawable.docomo_ex62); put("\uE74A", R.drawable.docomo_ex63); put("\uE74B", R.drawable.docomo_ex64);
			put("\uE74C", R.drawable.docomo_ex65); put("\uE74D", R.drawable.docomo_ex66); put("\uE74E", R.drawable.docomo_ex67); put("\uE74F", R.drawable.docomo_ex68);
			put("\uE750", R.drawable.docomo_ex69); put("\uE751", R.drawable.docomo_ex70); put("\uE752", R.drawable.docomo_ex71); put("\uE753", R.drawable.docomo_ex72);
			put("\uE754", R.drawable.docomo_ex73); put("\uE755", R.drawable.docomo_ex74); put("\uE756", R.drawable.docomo_ex75); put("\uE757", R.drawable.docomo_ex76);
		}
	};

	/** Event listener for touching a candidate */
	private final OnTouchListener mCandidateOnTouch = new OnTouchListener() {
		public boolean onTouch(final View v, final MotionEvent event) {
			if (mMotionEvent != null) {
				return true;
			}

			if ((event.getAction() == MotionEvent.ACTION_UP)
					&& (v instanceof TextView)) {
				final Drawable d = v.getBackground();
				if (d != null) {
					d.setState(new int[] {});
				}
			}

			mMotionEvent = event;
			final boolean ret = mWnn.onEvent(new NicoWnnGEvent(NicoWnnGEvent.CANDIDATE_VIEW_TOUCH));
			mMotionEvent = null;
			return ret;
		}
	};


	/** Event listener for clicking a candidate */
	private final OnClickListener mCandidateOnClick = new OnClickListener() {
		public void onClick(final View v) {
			if (!v.isShown()) {
				return;
			}

			if (v instanceof TextView) {
				checkCandidateTask();
				final TextView text = (TextView)v;
				final int wordcount = (int)(text.getId()) - 1;
				WnnWord word = null;
				word = mWnnWordArray.get(wordcount);
				selectWnnWordCandidate(wordcount);
				selectCandidate(word);
			}
		}
	};

	/** Event listener for long-clicking a candidate */
	private final OnLongClickListener mCandidateOnLongClick = new OnLongClickListener() {
		public boolean onLongClick(final View v) {
			if (mViewScaleUp == null) {
				return false;
			}

			if (!v.isShown()) {
				return true;
			}

			final Drawable d = v.getBackground();
			if (d != null) {
				if(d.getState().length == 0){
					return true;
				}
			}

			final int wordcount = (int)(((TextView)v).getId()) - 1;
			mWord = mWnnWordArray.get(wordcount);
			setViewScaleUp(true, mWord);

			return true;
		}
	};

	/**
	 * Constructor
	 */
	/**
	public TextCandidatesViewManager() {
		this(-1);
	}
	 */

	/**
	 * Constructor
	 *
	 * @param displayLimit      The limit of display
	 */
	public TextCandidatesViewManager(final int displayLimit, final Context context) {
		if (T) Log.v(TAG, M()+"@in: displayLimit="+displayLimit+", context="+context);

		me = context;
		mAutoHideMode = true;
		mIsActiveTask = false;
		mMetrics.setToDefaults();

		if (T) Log.v(TAG, M()+"@out");
	}

	/**
	 * Set auto-hide mode.
	 * @param hide      {@code true} if the view will hidden when no candidate exists;
	 *                  {@code false} if the view is always shown.
	 */
	public void setAutoHide(final boolean hide) {
		if (T) Log.v(TAG, M()+"@in: hide="+hide);

		mAutoHideMode = hide;

		if (T) Log.v(TAG, M()+"@out");
	}

	public void reloadPreference(final SharedPreferences pref) {
		if (T) Log.v(TAG, M()+"@in: pref="+pref);

		mMaxLine = mWnn.getOrientPrefKeyMode() ? 3 : 1;
		mCandidateVertical = false;
		mSpaceBelowCandidate = false;
		mShrinkCandidateLines = false;
		mShowCandidateButtonFullList = true;

		if (null != pref) {
			final String sMaxLine = mWnn.getOrientPrefKeyMode() ? "3" : "1";
			mMaxLine = Integer.valueOf(mWnn.getOrientPrefString(pref, "nico_candidate_lines", sMaxLine));
			mCandidateVertical = mWnn.getOrientPrefBoolean(pref, "nico_candidate_vertical", true);
			mSpaceBelowCandidate = mWnn.getOrientPrefBoolean(pref, "nospace_candidate2", false);
			mShrinkCandidateLines = mWnn.getOrientPrefBoolean(pref, "shrink_candidate_lines", false);
			mShowCandidateButtonFullList = mWnn.getOrientPrefBoolean(pref, "show_candidate_fulllist_button", true);
			mCandidateViewHeightIndex = Integer.valueOf(mWnn.getOrientPrefString(pref, "candidateview_height_mode2", "2"));
			mCandidateTextSizeIndex = Integer.valueOf(mWnn.getOrientPrefString(pref, "candidate_font_size", "2"));
		}

		if (T) Log.v(TAG, M()+"@out");
	}

	public void showViewTree() {
		// showViewTreeSub(mViewBody, 0);
		View rootView = mViewBody.getRootView();
		showViewTreeSub(rootView, 0);
	}

	public void showViewTreeSub(View v, int l) {
		String lv = "";
		for (int i=0; i<l; i++) {
			lv += "  ";
		}
		Log.v(TAG, lv+v.toString()+" ("+v.getLeft()+","+v.getTop()+","+v.getWidth()+","+v.getHeight()+")");

		if (v instanceof ViewGroup) {
			ViewGroup g = (ViewGroup)v;
			int count = g.getChildCount();
			for (int i=0; i<count; i++) {
				View child = g.getChildAt(i);
				showViewTreeSub(child, l+1);
			}
		}

	}

	/** @see CandidatesViewManager */
	public View initView(final NicoWnnG parent, final int width, final int height) {
		if (T) Log.v(TAG, M()+"@in: parent="+parent+", width="+width+", height="+height);

		Button b;
		mWnn = parent;
		mViewWidth = width;
		mViewHeight = height;
		// mViewWidth = Math.round(width / mMetrics.density);
		// mViewHeight = Math.round(height / mMetrics.density);
		mPortrait =
			(parent.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE);

		final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(parent);
		reloadPreference(pref);

		final Resources r = mWnn.getResources();

		final LayoutInflater inflater = parent.getLayoutInflater();
		mViewBody = (ViewGroup)inflater.inflate(R.layout.candidates, null);

		mViewBodyVScroll2nd = (ScrollView)mViewBody.findViewById(R.id.candview_scroll);
		mViewBodyVScroll2nd.setOnTouchListener(mCandidateOnTouch);

		mViewBodyHScroll = (HorizontalScrollView)mViewBody.findViewById(R.id.candview_hscroll);
		mViewBodyHScroll.setOnTouchListener(mCandidateOnTouch);
		mViewBodyHScroll.setHorizontalFadingEdgeEnabled(true);
		mIsLockHScroll = false;

		// mViewCandidateBase2 = (ViewGroup)mViewBody.findViewById(R.id.candview_base2);
		mViewCandidateBase = (ViewGroup)mViewBody.findViewById(R.id.candview_base);
		if (T) Log.v(TAG, M()+"mViewCandidateBase="+mViewCandidateBase);

		mViewCandidateList1st = (LinearLayout)mViewBody.findViewById(R.id.candidates_1st_view);
		if (T) Log.v(TAG, M()+"mViewCandidateList1st="+mViewCandidateList1st);
		mViewCandidateList1st.setOnTouchListener(mCandidateOnTouch);
		mViewCandidateList1st.setOnClickListener(mCandidateOnClick);

		mViewCandidateList2nd = (RelativeLayout)mViewBody.findViewById(R.id.candidates_2nd_view);
		if (T) Log.v(TAG, M()+"mViewCandidateList2nd="+mViewCandidateList2nd);
		mViewCandidateList2nd.setOnTouchListener(mCandidateOnTouch);
		mViewCandidateList2nd.setOnClickListener(mCandidateOnClick);

		mViewCandidateButtonFullList = (Button)mViewBody.findViewById(R.id.button_fulllist);

		// create first textView
		for (int iI = 0; iI < LINE_NUM_MAX; ++iI) {
			if (T) Log.v(TAG, M()+"iI="+iI);
			final TextView textView = createCandidateView();
			final LinearLayout lineView = new LinearLayout(me);
			if (T) Log.v(TAG, M()+"textView="+textView+", lineView="+lineView);
			final int h = getCandidateMinimumHeight();
			final LinearLayout.LayoutParams layoutParams =
				new LinearLayout.LayoutParams(
						ViewGroup.LayoutParams.WRAP_CONTENT,
						h
				);
			lineView.setLayoutParams(layoutParams);
			lineView.setGravity(Gravity.LEFT|Gravity.CENTER_VERTICAL|Gravity.CLIP_VERTICAL);
			lineView.setMinimumHeight(h);
			lineView.setVisibility(View.VISIBLE);
			lineView.addView(textView);
			mViewCandidateList1st.addView(lineView);
		}
		{
			mCandidateSpaceView = new LinearLayout(me);
			final LinearLayout.LayoutParams layoutParams =
				new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
						(getCandidateMinimumHeight()*2/3)
				);
			mCandidateSpaceView.setLayoutParams(layoutParams);
			mViewCandidateList1st.addView(mCandidateSpaceView);
			if (mSpaceBelowCandidate) {
				mCandidateSpaceView.setVisibility(View.VISIBLE);
			} else {
				mCandidateSpaceView.setVisibility(View.GONE);
			}
		}

		final TextView textView2nd = createCandidateView();
		mViewCandidateList2nd.addView(textView2nd);
		mViewCandidateTemplate = textView2nd;

		mTextColor = r.getColor(R.color.candidate_text);
		mTextBackgroundColor = r.getColor(R.color.candidate_textback);
		mTextBackgroundSelectColor = r.getColor(R.color.candidate_textback_select);

		setViewType(CandidatesViewManager.VIEW_TYPE_CLOSE);

		mGestureDetector = new GestureDetector(this);

		final View scaleUp = inflater.inflate(R.layout.candidate_scale_up, null);
		mViewScaleUp = scaleUp;

		/* select button */
		b = (Button)scaleUp.findViewById(R.id.candidate_select);
		b.setOnClickListener(new View.OnClickListener() {
			public void onClick(final View v) {
				selectCandidate(mWord);
			}
		});

		/* delete button */
		b = (Button)scaleUp.findViewById(R.id.candidate_delete);
		b.setOnClickListener(new View.OnClickListener() {
			public void onClick(final View v) {
				deleteCandidate(mWord);
				setViewLayout(CandidatesViewManager.VIEW_TYPE_NORMAL);
				mWnn.onEvent(new NicoWnnGEvent(NicoWnnGEvent.UPDATE_CANDIDATE));
			}
		});

		/* cancel button */
		b = (Button)scaleUp.findViewById(R.id.candidate_cancel);
		b.setOnClickListener(new View.OnClickListener() {
			public void onClick(final View v) {
				setViewLayout(CandidatesViewManager.VIEW_TYPE_NORMAL);
				mWnn.onEvent(new NicoWnnGEvent(NicoWnnGEvent.UPDATE_CANDIDATE));
			}
		});

		b = (Button)mViewBody.findViewById(R.id.button_fulllist);
		b.setOnClickListener(mFullListOnClick);
		final LinearLayout v = (LinearLayout)mViewBody.findViewById(R.id.button_fulllist_inner_layout);
		v.setOnClickListener(mFullListOnClick);

		/*
		setViewLayout(CandidatesViewManager.VIEW_TYPE_NORMAL);
		mViewCandidateList1st.setVisibility(View.VISIBLE);
		mViewCandidateList2nd.setVisibility(View.GONE);
		mViewCandidateBase.setMinimumHeight(-1);
		mViewCandidateList1st.setMinimumHeight(1);
		*/

		if (T) showViewTree();

		if (T) Log.v(TAG, M()+"@out: view="+mViewBody);
		return mViewBody;
	}

	private final OnClickListener mFullListOnClick = new OnClickListener() {
		public void onClick(final View v) {
			if (mIsFullView) {
				if (true == mIsScroll) {
					mIsScroll = false;
					return;
				}
				DefaultSoftKeyboard.doClickFeedback(1);
				mIsFullView = false;
				mIsScroll   = false; // reset scroll flag
				mWnn.onEvent(new NicoWnnGEvent(NicoWnnGEvent.LIST_CANDIDATES_NORMAL));
			} else {
				DefaultSoftKeyboard.doClickFeedback(1);
				mIsFullView = true;
				mIsScroll   = false; // reset scroll flag
				mWnn.onEvent(new NicoWnnGEvent(NicoWnnGEvent.LIST_CANDIDATES_FULL));
			}
		}
	};
	
	/** @see CandidatesViewManager#getCurrentView */
	public View getCurrentView() {
		return mViewBody;
	}

	/** @see CandidatesViewManager#setViewType */
	public void setViewType(final int type) {
		if (T) Log.v(TAG, M()+"@in: type="+type);

		final boolean readMore = setViewLayout(type);

		if (readMore) {
			if (false == mIsCreateFullView) {
				mCanReadMore = false;
				mFullViewWordCount = 0;
				mFullViewOccupyCount = 0;
				mFullViewPrevLineTopId = 0;
				mCreateCandidateDone = false;
				mIsScroll = false;

				mLineCount     = 1;
				mWordCount     = 0;
				mToplineLastId = 0;
				mTotalLastId   = 0;
				mIsLockHScroll = false;

				displayCandidates(mConverter, false, -1);
				mIsCreateFullView = true;
			}
			else{
				visibleFullCandidate();
			}
		} else {
			if (type == CandidatesViewManager.VIEW_TYPE_NORMAL) {
				mIsFullView = false;
				/*
				  if (mDisplayEndOffset > 0) {
				  int maxLine = getMaxLine();
				  displayCandidates(this.mConverter, false, maxLine);
				  }
				 */
			} else {
				mIsFullView = true;
				if (mViewBody.isShown()) {
					mWnn.setCandidatesViewShown(false);
				}
			}
		}
		if (T) Log.v(TAG, M()+"@out");
	}

	/**
	 * Set the view layout
	 *
	 * @param type      View type
	 * @return          {@code true} if display is updated; {@code false} if otherwise
	 */
	private boolean setViewLayout(final int type) {
		if (T) Log.v(TAG, M()+"@in: type="+type);

		mViewType = type;
		setViewScaleUp(false, null);
		final int height = getCandidateMinimumHeight();

		switch (type) {
			case CandidatesViewManager.VIEW_TYPE_CLOSE:
				checkCandidateTask();
				mViewCandidateBase.setMinimumHeight(-1);
				if (T) Log.v(TAG, M()+"@out: false");
				return false;

			case CandidatesViewManager.VIEW_TYPE_NORMAL:
				clearNormalCandidates();
				mViewBodyVScroll2nd.scrollTo(0, 0);
				mViewCandidateList1st.setVisibility(View.VISIBLE);
				mViewCandidateList2nd.setVisibility(View.GONE);
				mViewCandidateBase.setMinimumHeight(-1);
				mViewCandidateList1st.setMinimumHeight(1);
				for (int iI = 0; iI < LINE_NUM_MAX; ++iI) {
					final LinearLayout lineView = (LinearLayout)mViewCandidateList1st.getChildAt(iI);
					int maxLine = getMaxLine();
					if (iI < maxLine) {
						lineView.setVisibility(View.VISIBLE);
						final LinearLayout.LayoutParams layoutParams =
							new LinearLayout.LayoutParams(
									ViewGroup.LayoutParams.WRAP_CONTENT,
									height
							);
						lineView.setLayoutParams(layoutParams);
						lineView.setMinimumHeight(height);
						for (int i=0; i<lineView.getChildCount(); i++) {
							final TextView v = (TextView)lineView.getChildAt(i);
							v.setTextSize(candidateTextSizeTable[mCandidateTextSizeIndex]);
							v.setMinHeight(height);
							v.setMaxHeight(height);
							v.setHeight(height);
						}
					} else{
						lineView.setVisibility(View.GONE);
					}
				}
				mViewCandidateTemplate.setTextSize(candidateTextSizeTable[mCandidateTextSizeIndex]);
				mViewCandidateTemplate.setHeight(getCandidateMinimumHeight());
				// mViewCandidateList1st.requestLayout();
				mViewCandidateList2nd.requestLayout();
				mViewCandidateButtonFullList.setBackgroundResource(R.drawable.candidate_listup);
				// mViewCandidateButtonFullList.setText(R.string.key_dic_listup);
				if (T) Log.v(TAG, M()+"@out: false");
				return false;

			case CandidatesViewManager.VIEW_TYPE_FULL:
			default:
				mViewCandidateList1st.setVisibility(View.GONE);
				mViewCandidateList2nd.setVisibility(View.VISIBLE);
				mViewCandidateList2nd.setMinimumHeight(mViewHeight);
				mViewCandidateBase.setMinimumHeight(mViewHeight);
				mViewCandidateButtonFullList.setBackgroundResource(R.drawable.candidate_listdown);
				// mViewCandidateButtonFullList.setText(R.string.key_dic_listdown);
				for (int i=0; i<mViewCandidateList2nd.getChildCount(); i++) {
					final TextView v = (TextView)mViewCandidateList2nd.getChildAt(i);
					v.setTextSize(candidateTextSizeTable[mCandidateTextSizeIndex]);
					v.setMinHeight(height);
					v.setMaxHeight(height);
					v.setHeight(height);
				}
				mViewCandidateList2nd.requestLayout();
				if (T) Log.v(TAG, M()+"@out: true");
				return true;
		}
	}

	/** @see CandidatesViewManager#getViewType */
	public int getViewType() {
		return mViewType;
	}

	/** set full view */
	public void setFullView() {
		if (T) Log.v(TAG, M()+"@in");

		mIsFullView = true;
		mIsScroll   = false; // reset scroll flag
		mWnn.onEvent(new NicoWnnGEvent(NicoWnnGEvent.LIST_CANDIDATES_FULL));

		if (T) Log.v(TAG, M()+"@out");
	}

	/** @see CandidatesViewManager#displayCandidates */
	public void displayCandidates(final WnnEngine converter) {
		if (T) Log.v(TAG, M()+"@in: converter="+converter);

		final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mWnn);

		reloadPreference(pref);

		mCanReadMore = false;
		mIsFullView = false;
		mFullViewWordCount = 0;
		mFullViewOccupyCount = 0;
		mFullViewPrevLineTopId = 0;
		mCreateCandidateDone = false;
		mIsCreateFullView = false;
		mIsScroll = false;

		clearCandidates();
		mConverter = converter;

		mCandidateViewWidth = mViewWidth;
		if (mShowCandidateButtonFullList) {
			final LinearLayout v = (LinearLayout)mViewBody.findViewById(R.id.button_fulllist_inner_layout);
			mCandidateViewWidth -= v.getWidth();
		}
		int indentWidth = mCandidateViewWidth / FULL_VIEW_DIV;
		indentWidth = Math.max( indentWidth, getCandidateMinimumWidth());
		mFullViewDiv = mCandidateViewWidth / indentWidth;

		createWnnWordArray();

		setViewLayout(CandidatesViewManager.VIEW_TYPE_NORMAL);
		/* create normalview */
		display1stCandidates(mConverter, mCandidateViewWidth);
		mTargetScrollWidth = mCandidateViewWidth / 2;
		// mViewBodyHScroll.scrollTo(0, 0);
		mViewBodyHScroll.smoothScrollTo(0, 0);
		mIsLockHScroll = false;
		invalidate1stView();
		/* create background normalview */
		startCandidateTask();

		if (T) Log.v(TAG, M()+"@out");
	}
	/*
	 *
	 */
	private void startCandidateTask() {
		if (T) Log.v(TAG, M()+"@in");

		mCandidateTask = new TextCandidateTask(this);
		if (null != mCandidateTask) {
			mIsActiveTask = true;
			mIsCancelTask = false;
			mCandidateTask.execute(mCandidateViewWidth*1000);
		}

		if (T) Log.v(TAG, M()+"@out");
	}
	/*
	 *
	 */
	public void checkCandidateTask() {
		if (T) Log.v(TAG, M()+"@in");

		if (null != mCandidateTask) {
			mCandidateTask.cancel(true);
			mCandidateTask = null;
		}

		if (T) Log.v(TAG, M()+"@out");
	}
	/*
	 *
	 */
	public void hideCandidateTask() {
		if (T) Log.v(TAG, M()+"@in");

		int count = 100;
		if (null != mCandidateTask) {
			mCandidateTask.cancel(true);
			while(count > 0) {
				try {
					Thread.sleep(20);
				} catch (final Exception e) {
					if (T) Log.v(TAG, M()+"@out: timeout");
					return;
				}
				//SystemClock.sleep(20);
				if (true == mIsCancelTask) {
					break;
				}
				count--;
			}
			mCandidateTask = null;
		}

		if (T) Log.v(TAG, M()+"@out");
	}
	/*
	 * cancel callback
	 */
	public void cancelTask() {
		if (T) Log.v(TAG, M()+"@in");

		mIsCancelTask = true;

		if (T) Log.v(TAG, M()+"@out");
	}

	private int mIndicateCandidateWordNum = 0;
	/*
	 *
	 */
	synchronized public void clearIndicateCandidateView() {
		if (T) Log.v(TAG, M()+"@in");

		mIndicateCandidateWordNum = 0;

		if (T) Log.v(TAG, M()+"@out");
	}
	/*
	 *
	 */
	synchronized public int getIndicateCandidateView() {
		return mIndicateCandidateWordNum;
	}
	/*
	 *
	 */
	synchronized public void setIndicateCandidateView() {
		if (T) Log.v(TAG, M()+"@in");

		if (mIndicateCandidateWordNum != 0) {
			selectIndicateCandidateView(mIndicateCandidateWordNum-1, false);
		}
		mIndicateCandidateWordNum = mWnnWordConvertPos+1;
		selectIndicateCandidateView(mIndicateCandidateWordNum-1, true);

		if (T) Log.v(TAG, M()+"@out");
	}
	/*
	 *
	 */
	public void scrollIntoView(LinearLayout lineView, TextView v) {
		if (T) Log.v(TAG, M()+"@in");

		final int viewWidth = mCandidateViewWidth;
		int indentWidth = viewWidth / mFullViewDiv;
		indentWidth = Math.max( indentWidth, getCandidateMinimumWidth());

		final int margin = indentWidth*(mFullViewDiv/2);
		int tvx = v.getLeft();
		int tvy = v.getTop();
		int tvw = v.getWidth();
		int tvh = v.getHeight();
		int lvx = lineView.getLeft();
		int lvy = lineView.getTop();
		int lvw = lineView.getWidth();
		int lvh = lineView.getHeight();
		int clw = mViewCandidateBase.getWidth();
		int clh = mViewCandidateBase.getHeight();
		int hsx = mViewBodyHScroll.getScrollX();
		int hsy = mViewBodyHScroll.getScrollY();
		int xscroll = hsx;
		int yscroll = hsy;

		// if (xscroll < tvx+tvw-clw+margin) xscroll = tvx+tvw-clw+margin;
		// if (xscroll > tvx-margin) xscroll = tvx-margin;
		xscroll = tvx-((clw-tvw)/2);
		if (xscroll < 0) xscroll = 0;
		if (xscroll > lvw-clw) xscroll = lvw-clw;
		
		if (xscroll != hsx) {
			if (!mIsLockHScroll) {
				// mViewBodyHScroll.smoothScrollTo(xscroll, hsy);
				mViewBodyHScroll.scrollTo(xscroll, hsy);
			}
		}

		if (T) Log.v(TAG, M()+"@out");
	}
	/*
	 *
	 */
	synchronized public void selectIndicateCandidateView(final int num, final boolean sel) {
		if (T) Log.v(TAG, M()+"@in: num="+num+", sel="+sel+", mWnnWordArray="+mWnnWordArray);

		if (mWnnWordArray == null) {
			if (T) Log.v(TAG, M()+"return");
			return;
		}
		if (num < 0) {
			if (T) Log.v(TAG, M()+"return");
			return;
		}
		if (num >= mWnnWordArray.size()) {
			if (T) Log.v(TAG, M()+"return");
			return;
		}

		int line;
		int count = 0;
		final LinearLayout lineView;
		final TextView candidateView;

		if (mCandidateVertical) {
			line = num % mShowCandidateLines;
			lineView = (LinearLayout)mViewCandidateList1st.getChildAt(line);
			if (lineView == null) {
				if (T) Log.v(TAG, M()+"return");
				return;
			}
			int ct = lineView.getChildCount();
			if (ct > 0) ct--;
			final int idx = num/mShowCandidateLines;
			final int idx2 = (idx <= ct) ? idx : ct;
			candidateView = (TextView)lineView.getChildAt(idx2);
			if (candidateView == null) {
				if (T) Log.v(TAG, M()+"return");
				return;
			}
		} else {
			for (line=0; line<mShowCandidateLines; line++) {
				int count2 = count + mWnnLineCountMax[line];
				if (num < count2) break;
				count = count2;
			}
			if (line >= mShowCandidateLines) {
				if (T) Log.v(TAG, M()+"return");
				return;
			}

			if (mViewCandidateList1st == null) {
				if (T) Log.v(TAG, M()+"return");
				return;
			}
			lineView = (LinearLayout)mViewCandidateList1st.getChildAt(line);
			if (lineView == null) {
				if (T) Log.v(TAG, M()+"return");
				return;
			}
			int ct = lineView.getChildCount();
			if (ct > 0) ct--;
			final int idx = num-count;
			final int idx2 = (idx <= ct) ? idx : ct;
			candidateView = (TextView)lineView.getChildAt(idx2);
			if (candidateView == null) {
				if (T) Log.v(TAG, M()+"return");
				return;
			}
		}

		final Drawable bg = candidateView.getBackground();
		if (sel) {
			LightingColorFilter f = new LightingColorFilter(mTextBackgroundSelectColor, 0);
			bg.setColorFilter(f);
			scrollIntoView((LinearLayout)mViewCandidateList1st.getChildAt(0), candidateView);
		} else {
			bg.clearColorFilter();
		}
		bg.invalidateSelf();

		if (T) Log.v(TAG, M()+"@out");
	}

	public WnnWord getRightWnnWordCandidate() {
		if (mCandidateVertical) {
			return getNextLineWnnWordCandidate();
		}
		return getNextWnnWordCandidate();
	}

	public WnnWord getLeftWnnWordCandidate() {
		if (mCandidateVertical) {
			return getPrevLineWnnWordCandidate();
		}
		return getPrevWnnWordCandidate();
	}

	public WnnWord getUpWnnWordCandidate() {
		if (mCandidateVertical) {
			return getPrevWnnWordCandidate();
		}
		return getPrevLineWnnWordCandidate();
	}

	public WnnWord getDownWnnWordCandidate() {
		if (mCandidateVertical) {
			return getNextWnnWordCandidate();
		}
		return getNextLineWnnWordCandidate();
	}

	private void waitActiveTask() {
		//
	}

	/*
	 *
	 */
	synchronized public WnnWord getNextWnnWordCandidate() {
		if (mWnnWordArray == null) {
			return null;
		}
		waitActiveTask();
		if (mWnnWordArray.size() == 0) {
			return null;
		}
		if (mIndicateCandidateWordNum != 0) {
			mWnnWordConvertPos++;
		}
		if (mWnnWordConvertPos >= mWnnWordArray.size()) {
			mWnnWordConvertPos = 0;
		}
		if (mWnnWordConvertPos < 0) {
			mWnnWordConvertPos = mWnnWordArray.size()-1;
		}
		WnnWord wnnword = mWnnWordArray.get(mWnnWordConvertPos);
		return wnnword;
	}
	/*
	 *
	 */
	synchronized public WnnWord getPrevWnnWordCandidate() {
		if (mWnnWordArray == null) {
			return null;
		}
		waitActiveTask();
		if (mWnnWordArray.size() == 0) {
			return null;
		}
		if (mIndicateCandidateWordNum != 0) {
			mWnnWordConvertPos--;
		}
		if (mWnnWordConvertPos >= mWnnWordArray.size()) {
			mWnnWordConvertPos = 0;
		}
		if (mWnnWordConvertPos < 0) {
			mWnnWordConvertPos = mWnnWordArray.size()-1;
		}
		WnnWord wnnword = mWnnWordArray.get(mWnnWordConvertPos);
		return wnnword;
	}
	/*
	 *
	 */
	synchronized public WnnWord getNextLineWnnWordCandidate() {
		if (mWnnWordArray == null) {
			return null;
		}
		waitActiveTask();
		if (mWnnWordArray.size() == 0) {
			return null;
		}
		if (mShowCandidateLines == 0 ) {
			return null;
		}
		if (mIndicateCandidateWordNum != 0) {
			mWnnWordConvertPos += mShowCandidateLines;
		}
		if (mWnnWordConvertPos >= mWnnWordArray.size()) {
			mWnnWordConvertPos = mWnnWordConvertPos % mShowCandidateLines;
		}
		if (mWnnWordConvertPos < 0) {
			final int l = (mWnnWordArray.size() + mShowCandidateLines-1) / mShowCandidateLines;
			mWnnWordConvertPos += l*mShowCandidateLines;
			if (mWnnWordConvertPos >= mWnnWordArray.size()) {
				mWnnWordConvertPos -= mShowCandidateLines;
			}
		}
		WnnWord wnnword = mWnnWordArray.get(mWnnWordConvertPos);
		return wnnword;
	}
	/*
	 *
	 */
	synchronized public WnnWord getPrevLineWnnWordCandidate() {
		if (mWnnWordArray == null) {
			return null;
		}
		waitActiveTask();
		if (mWnnWordArray.size() == 0) {
			return null;
		}
		if (mShowCandidateLines == 0 ) {
			return null;
		}
		if (mIndicateCandidateWordNum != 0) {
			mWnnWordConvertPos -= mShowCandidateLines;
		}
		if (mWnnWordConvertPos >= mWnnWordArray.size()) {
			mWnnWordConvertPos = mWnnWordConvertPos % mShowCandidateLines;
		}
		if (mWnnWordConvertPos < 0) {
			final int l = (mWnnWordArray.size() + mShowCandidateLines-1) / mShowCandidateLines;
			mWnnWordConvertPos += l*mShowCandidateLines;
			if (mWnnWordConvertPos >= mWnnWordArray.size()) {
				mWnnWordConvertPos -= mShowCandidateLines;
			}
		}
		WnnWord wnnword = mWnnWordArray.get(mWnnWordConvertPos);
		return wnnword;
	}
	/*
	 *
	 */
	synchronized public WnnWord getNowWnnWordCandidate() {
		if (mWnnWordArray == null) {
			return null;
		}
		waitActiveTask();
		if (mWnnWordArray.size() == 0) {
			return null;
		}
		if (mWnnWordConvertPos >= mWnnWordArray.size()) {
			mWnnWordConvertPos = 0;
		}
		if (mWnnWordConvertPos < 0) {
			mWnnWordConvertPos = mWnnWordArray.size()-1;
		}
		WnnWord wnnword = mWnnWordArray.get(mWnnWordConvertPos);
		return wnnword;
	}
	/*
	 *
	 */
	synchronized public WnnWord getFirstWnnWordCandidate() {
		if (mWnnWordArray == null) {
			return null;
		}
		if (mWnnWordArray.size() == 0) {
			return null;
		}
		mWnnWordConvertPos = 0;
		WnnWord wnnword = mWnnWordArray.get(mWnnWordConvertPos);
		return wnnword;
	}
	/*
	 *
	 */
	synchronized public void selectWnnWordCandidate(int n) {
		if (mWnnWordArray == null) {
			return;
		}
		if (mWnnWordArray.size() <= n) {
			return;
		}
		mWnnWordConvertPos = n;
	}
	/*
	 *
	 */
	//synchronized private void createWnnWordArray() {
	private void createWnnWordArray() {
		if (T) Log.v(TAG, M()+"@in");

		final WnnEngine converter = mConverter;
		if (null == converter) {
			if (T) Log.v(TAG, M()+"return");
			return;
		}
		WnnWord result = null;
		int index = 0;
		final int viewWidth = mCandidateViewWidth;
		int indentWidth = viewWidth / mFullViewDiv;
		indentWidth = Math.max( indentWidth, getCandidateMinimumWidth());
		final int maxindex = DISPLAY_LINE_MAX_COUNT * mFullViewDiv;
		int maxWidth = 0;
		int maxOccupyCount = 0;
		do {
			result = converter.getNextCandidate();
			if (result == null) {
				break;
			}
			final int textLength  = measureText(result.candidate, 0, result.candidate.length());
			final int occupyCount = Math.min(((textLength+indentWidth-1) / indentWidth), mFullViewDiv);
			// final int occupyCount = Math.min((textLength + 4) / indentWidth, mFullViewDiv);
			final int textWidth   = indentWidth * occupyCount;

			mWnnWordArray.add(index, result);
			mWnnWordTextLength.add(index, textWidth);
			mWnnWordOccupyCount.add(index, occupyCount);
			mWnnWordTextLength2.add(index, textWidth);
			mWnnWordOccupyCount2.add(index, occupyCount);
			maxWidth += textWidth;
			maxOccupyCount += occupyCount;
			index++;
		} while (index < maxindex);

		/* split wnnword tables */
		final int wordCount = mWnnWordArray.size();
		final int maxline = getMaxLine();
		if (mCandidateVertical) {
			mShowCandidateLines = mWnnWordArray.size();
		} else {
			mShowCandidateLines = (maxWidth+mCandidateViewWidth-1) / mCandidateViewWidth;
		}
		if (mShowCandidateLines > maxline) {
			mShowCandidateLines = maxline;
		}
		if (mShowCandidateLines == 0) {
			mShowCandidateLines = 1;
		}
		if (mCandidateVertical) {
			int splitCounts = (mWnnWordArray.size()+mShowCandidateLines-1)/mShowCandidateLines;
			int n = mWnnWordArray.size() % mShowCandidateLines;
			if (n == 0) n = mShowCandidateLines;
			for (int j=0; j<mShowCandidateLines; j++) {
				mWnnLineCount[j] = 0;
				mWnnLineCountMax[j] = (j < n) ? splitCounts : splitCounts-1;
				mWnnLineOffset[j] = j;
			}
			index = 0;
			for (int i=0; i<splitCounts; i++) {
				maxOccupyCount = 0;
				for (int j=0; j<mShowCandidateLines; j++) {
					if (index+j >= mWnnWordArray.size()) break;
					int oc = mWnnWordOccupyCount2.get(index+j);
					if (maxOccupyCount < oc) {
						maxOccupyCount = oc;
					}
				}
				for (int j=0; j<mShowCandidateLines; j++) {
					if (index+j >= mWnnWordArray.size()) break;
					mWnnWordTextLength2.set(index+j, indentWidth * maxOccupyCount);
					mWnnWordOccupyCount2.set(index+j, maxOccupyCount);
				}
				index += mShowCandidateLines;
			}
		} else {
			int lastOccupyCount = maxOccupyCount;
			int offset = 0;
			for (int i=0; i<mShowCandidateLines; i++) {
				int splitOccupyCount = ((lastOccupyCount+mShowCandidateLines-i-1)/(mShowCandidateLines-i));
				mWnnLineCount[i] = 0;
				mWnnLineOffset[i] = offset;
				int linecount = 0;
				int occupyCount = 0;
				while (offset < wordCount) {
					linecount++;
					occupyCount += mWnnWordOccupyCount2.get(offset++);
					if (occupyCount >= splitOccupyCount) break;
				}
				mWnnLineCountMax[i] = linecount;
				lastOccupyCount -= occupyCount;
			}
		}
		mWnnWordConvertPos = 0;

		if (T) Log.v(TAG, M()+"@out");
	}

	public int getMaxLine() {
		return mMaxLine;
	}

	/*
	 *
	 */
	//synchronized private void display1stCandidates(WnnEngine converter, int width) {
	private void display1stCandidates(final WnnEngine converter, final int width) {
		if (T) Log.v(TAG, M()+"@in: converter="+converter+", width="+width);

		if (converter == null) {
			if (T) Log.v(TAG, M()+"return");
			return;
		}
		/* Get candidates */
		WnnWord result = null;
		final int maxline = getMaxLine();
		int wordcount = 0;
		int size      = 0;
		int offset    = 0;
		int calcwidth = 0;
		int calcid    = 0;
		int iI;
		final int maxid = mWnnWordArray.size()-1;
		int totalwords = 0;
		for (iI = 0; iI < mShowCandidateLines; ++iI) {
			if (T) Log.v(TAG, M()+"iI="+iI+", mShowCandidateLines="+mShowCandidateLines);
			wordcount = mWnnLineCount[iI];
			size      = mWnnLineCountMax[iI];
			offset    = mWnnLineOffset[iI];
			calcwidth = 0;

			final LinearLayout candidateList = mViewCandidateList1st;
			final LinearLayout lineView = (LinearLayout)candidateList.getChildAt(iI);
			while (wordcount < size) {
				if (T) Log.v(TAG, M()+"wordcount="+wordcount+", size="+size);
				if (mCandidateVertical) {
					calcid = wordcount*mShowCandidateLines + offset;
				} else {
					calcid = wordcount + offset;
				}
				if (calcid > maxid) {
					Log.e("NicoWnnG", "display1stCandidates : calcid(" + calcid + ") > maxid(" + maxid + ")");
					calcid = maxid;
				}
				result = mWnnWordArray.get(calcid);
				calcwidth += mWnnWordTextLength2.get(calcid);
				set1stCandidate(wordcount, calcid, result, mWnnWordTextLength2.get(calcid), mWnnWordOccupyCount2.get(calcid), lineView);
				wordcount++;
				if (T) Log.v(TAG, M()+"calcwidth="+calcwidth+", width="+width);
				if (calcwidth >= width) {
					break;
				}
			}
			totalwords += wordcount;
			mWnnLineCount[iI] = wordcount;
		}
		
		if (totalwords == 0) { /* no candidates */
			if (mAutoHideMode) {
				mWnn.setCandidatesViewShown(false);
				return;
			} else {
				mCanReadMore = false;
				mIsFullView = false;
				setViewLayout(CandidatesViewManager.VIEW_TYPE_NORMAL);
			}
		}
		else{
			mCanReadMore = true;
			//if (mLastShowCandidateLines != mShowCandidateLines) {
			{
				mLastShowCandidateLines = mShowCandidateLines;
				final LinearLayout candidateList = mViewCandidateList1st;
				for (int i=0; i<getMaxLine(); i++) {
					final LinearLayout lineView = (LinearLayout)candidateList.getChildAt(i);
					if (!mShrinkCandidateLines || (i < mShowCandidateLines)) {
						lineView.setVisibility(View.VISIBLE);
					} else {
						lineView.setVisibility(View.GONE);
					}
				}
			}
		}
		if (mShowCandidateButtonFullList) {
			final LinearLayout.LayoutParams layoutParams =
				new LinearLayout.LayoutParams(
						ViewGroup.LayoutParams.WRAP_CONTENT,
						ViewGroup.LayoutParams.FILL_PARENT
						// getCandidateMinimumHeight(),
				);
			final LinearLayout v = (LinearLayout)mViewBody.findViewById(R.id.button_fulllist_inner_layout);
			v.setLayoutParams(layoutParams);
			mViewCandidateButtonFullList.setVisibility(View.VISIBLE);
			v.requestLayout();
		} else {
			mViewCandidateButtonFullList.setVisibility(View.GONE);
		}
		if (mSpaceBelowCandidate) {
			mCandidateSpaceView.setVisibility(View.VISIBLE);
		} else {
			mCandidateSpaceView.setVisibility(View.GONE);
		}
		mViewCandidateList1st.requestLayout();

		if (!(mViewBody.isShown())) {
			mWnn.setCandidatesViewShown(true);
		}

		if (T) Log.v(TAG, M()+"@out");
	}
	/*
	 *
	 */
	public int calc1stCandidates(final int line, final int width) {
		if (T) Log.v(TAG, M()+"@in: line="+line+", width="+width);

		if (false == mIsActiveTask) {
			if (T) Log.v(TAG, M()+"return");
			return 0;
		}

		int wordcount = mWnnLineCount[line];
		int size      = mWnnLineCountMax[line];
		int offset    = mWnnLineOffset[line];
		int calcid    = 0;
		int calcwidth = 0;
		while (calcwidth < width) {
			if (wordcount >= size) {
				break;
			}
			if (mCandidateVertical) {
				calcid = wordcount*mShowCandidateLines + line;
			} else {
				calcid = wordcount + offset;
			}
			if (calcid >= mWnnWordArray.size()) {
				break;
			}
			calcwidth += mWnnWordTextLength2.get(calcid);
			wordcount++;
		}

		if (T) Log.v(TAG, M()+"@out: width="+calcwidth);
		return calcwidth;
	}
	/*
	 *
	 */
	public boolean display1stCandidates(final int line, final int width) {
		if (T) Log.v(TAG, M()+"@in: line="+line+", width="+width);

		if (false == mIsActiveTask) {
			if (T) Log.v(TAG, M()+"@out: true");
			return true;
		}
		boolean result = false;
		int wordcount = mWnnLineCount[line];
		int size      = mWnnLineCountMax[line];
		int offset    = mWnnLineOffset[line];
		int calcid    = 0;
		int calcwidth = 0;
		final LinearLayout candidateList = mViewCandidateList1st;
		final LinearLayout lineView = (LinearLayout)candidateList.getChildAt(line);
		while (calcwidth < width) {
			if (wordcount >= size) {
				result = true;
				break;
			}
			if (mCandidateVertical) {
				calcid = wordcount*mShowCandidateLines + line;
			} else {
				calcid = wordcount + offset;
			}
			if (calcid >= mWnnWordArray.size()) {
				result = true;
				break;
			}
			WnnWord wnnword = mWnnWordArray.get(calcid);
			calcwidth += mWnnWordTextLength2.get(calcid);
			set1stCandidate(wordcount, calcid, wnnword, mWnnWordTextLength2.get(calcid), mWnnWordOccupyCount2.get(calcid), lineView);
			wordcount++;
		}
		mWnnLineCount[line] = wordcount;

		if (T) Log.v(TAG, M()+"@out: result="+result);
		return result;
	}
	/*
	 *
	 */
	public void invalidate1stView() {
		if (T) Log.v(TAG, M()+"@in");

		/*
		final int maxLine  = getMaxLine();
		int width = 0;
		for (int iI = 0; iI < maxLine; ++iI) {
			final LinearLayout lineView = (LinearLayout)mViewCandidateList1st.getChildAt(iI);
			final int getwidth = lineView.getWidth();
			if (width < getwidth) {
				width = getwidth;
			}
		}
		*/
		final int getx = mViewBodyHScroll.getScrollX();
		final int gety = mViewBodyHScroll.getScrollY();
		mViewCandidateList1st.invalidate();
		mViewBodyHScroll.scrollTo(getx, gety);

		if (T) Log.v(TAG, M()+"@out");
	}
	/*
	 *
	 */
	public void display1stLastSetup() {
		if (T) Log.v(TAG, M()+"@in");

		mIsActiveTask = false;

		if (T) Log.v(TAG, M()+"@out");
	}
	/**
	 * Display the candidates.
	 *
	 * @param converter  {@link WnnEngine} which holds candidates.
	 * @param dispFirst  Whether it is the first time displaying the candidates
	 * @param maxLine    The maximum number of displaying lines
	 */
	//synchronized private void displayCandidates(WnnEngine converter, boolean dispFirst, int maxLine) {
	private void displayCandidates(final WnnEngine converter, final boolean dispFirst, final int maxLine) {
		if (T) Log.v(TAG, M()+"@in: converter="+converter+", dispFirst="+dispFirst+", maxLine="+maxLine);

		if (converter == null) {
			if (T) Log.v(TAG, M()+"return");
			return;
		}
		boolean isBreak = false;

		/* Get candidates */
		WnnWord result = null;
		final int size = mWnnWordArray.size();
		while (mWordCount < size) {
			result = mWnnWordArray.get(mWordCount);
			setCandidate(result, mWnnWordTextLength.get(mWordCount), mWnnWordOccupyCount.get(mWordCount));
			if (dispFirst && (maxLine < mLineCount)) {
				mCanReadMore = true;
				isBreak = true;
				break;
			}
		}

		if (!isBreak && !mCreateCandidateDone) {
			/* align left if necessary */
			createNextLine();
			mCreateCandidateDone = true;
		}

		if (mWordCount < 1) { /* no candidates */
			if (mAutoHideMode) {
				mWnn.setCandidatesViewShown(false);
				return;
			} else {
				mCanReadMore = false;
				mIsFullView = false;
				setViewLayout(CandidatesViewManager.VIEW_TYPE_NORMAL);
			}
		}
		if (!(mViewBody.isShown())) {
			mWnn.setCandidatesViewShown(true);
		}

		if (T) Log.v(TAG, M()+"@out");
		return;
	}

	/**
	 *
	 */
	private void set1stCandidate(final int viewindex, final int inid, final WnnWord word, final int textLength, final int occupyCount, final LinearLayout lineView) {
		if (T) Log.v(TAG, M()+"@in: viewindex="+viewindex+", inid="+inid+", word="+word+", textLength="+textLength+", occupyCount="+occupyCount+", lineView="+lineView);

		TextView textView;
		final int viewWidth = mCandidateViewWidth;
		int indentWidth = viewWidth / mFullViewDiv;
		indentWidth = Math.max( indentWidth, getCandidateMinimumWidth());

		final int width = indentWidth * occupyCount;
		// final int width = textLength;
		final int height = getCandidateMinimumHeight();
		boolean iscreate = false;
		int ct = lineView.getChildCount();
		if (ct > 0) ct--;
		if (viewindex > ct) {
			textView = createCandidateView();
			iscreate = true;
		} else {
			textView = (TextView) lineView.getChildAt(viewindex);
			if (textView == null) {
				textView = createCandidateView();
				iscreate = true;
			}
		}

		final int textId = inid + 1;
		
		textView.setTextSize(candidateTextSizeTable[mCandidateTextSizeIndex]);
		textView.setText(word.candidate);
		textView.setTextColor(mTextColor);
		textView.getBackground().clearColorFilter();
		textView.setId(textId);
		textView.setVisibility(View.VISIBLE);
		textView.setPressed(false);
		textView.setEllipsize(TextUtils.TruncateAt.END);
		textView.setPadding(1, 1, 1, 1);
		textView.setWidth(width);
		textView.setMinHeight(height);
		textView.setMaxHeight(height);
		textView.setHeight(height);
		textView.setGravity(Gravity.CENTER|Gravity.CLIP_VERTICAL);

		textView.setOnClickListener(mCandidateOnClick);
		textView.setOnLongClickListener(mCandidateOnLongClick);
		textView.setBackgroundResource(R.drawable.cand_back);

		textView.setOnTouchListener(mCandidateOnTouch);

		checkImageSpan(textView, word);
		if (true == iscreate) {
			lineView.addView(textView);
		}

		if (T) Log.v(TAG, M()+"@out");
	}

	/**
	 * Add a candidate into the list.
	 * @param word        A candidate word
	 */
	private void setCandidate(final WnnWord word, final int textLength, final int occupyCount) {
		if (T) Log.v(TAG, M()+"@in: word="+word+", textLength="+textLength+", occupyCount="+occupyCount);

		TextView textView;
		final int viewWidth = mCandidateViewWidth;
		int indentWidth = viewWidth / mFullViewDiv;
		indentWidth = Math.max( indentWidth, getCandidateMinimumWidth());

		final RelativeLayout layout = mViewCandidateList2nd;

		final int width = indentWidth * occupyCount;
		final int height = getCandidateMinimumHeight();
		final RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width, height);

		final int textId = mWordCount+1;
		if (mFullViewDiv < (mFullViewOccupyCount + occupyCount)) {
			if (mFullViewDiv != mFullViewOccupyCount) {
				mFullViewPrevParams.width += (mFullViewDiv - mFullViewOccupyCount) * indentWidth;
				mViewCandidateList2nd.updateViewLayout(mFullViewPrevView, mFullViewPrevParams);
			}
			if (getMaxLine() >= mLineCount) {
				mToplineLastId = textId;
			}
			createNextLine();
		}

		if (mFullViewPrevLineTopId == 0) {
			params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		} else {
			params.addRule(RelativeLayout.BELOW, mFullViewPrevLineTopId);
		}

		if (mFullViewOccupyCount == 0) {
			params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		} else {
			params.addRule(RelativeLayout.RIGHT_OF, (mWordCount));
		}

		textView = (TextView) layout.getChildAt(mFullViewWordCount);
		if (textView == null) {
			textView = createCandidateView();
			textView.setLayoutParams(params);

			mViewCandidateList2nd.addView(textView);
		} else {
			mViewCandidateList2nd.updateViewLayout(textView, params);
		}

		mFullViewOccupyCount += occupyCount;
		mFullViewWordCount++;
		mFullViewPrevView = textView;
		mFullViewPrevParams = params;

		textView.setTextSize(candidateTextSizeTable[mCandidateTextSizeIndex]);
		textView.setText(word.candidate);
		textView.setTextColor(mTextColor);
		textView.getBackground().clearColorFilter();
		textView.setId(textId);
		textView.setVisibility(View.VISIBLE);
		textView.setPressed(false);
		textView.setEllipsize(TextUtils.TruncateAt.END);
		textView.setPadding(1, 1, 1, 1);
		textView.setMinHeight(height);
		textView.setMaxHeight(height);
		textView.setHeight(height);
		textView.setGravity(Gravity.CENTER|Gravity.CLIP_VERTICAL);

		textView.setOnClickListener(mCandidateOnClick);
		textView.setOnLongClickListener(mCandidateOnLongClick);
		textView.setBackgroundResource(R.drawable.cand_back);

		textView.setOnTouchListener(mCandidateOnTouch);

		checkImageSpan(textView, word);
		mWordCount++;
		mTotalLastId = textId;

		/*
		if (mFullViewDiv < (mFullViewOccupyCount + occupyCount)) {
			if (mFullViewDiv != mFullViewOccupyCount) {
				mFullViewPrevParams.width += (mFullViewDiv - mFullViewOccupyCount) * indentWidth;
				mViewCandidateList2nd.updateViewLayout(mFullViewPrevView, mFullViewPrevParams);
			}
			if (getMaxLine() >= mLineCount) {
				mToplineLastId = textId;
			}
			createNextLine();
		}
		 */

		if (T) Log.v(TAG, M()+"@out");
	}
	/*
	 * check Image Span
	 */
	private void checkImageSpan(final TextView textView, final WnnWord word) {
		if (T) Log.v(TAG, M()+"@in: textView="+textView+", word="+word);

		// if (false) {
		if ((Build.VERSION.SDK_INT >= 11) && (Build.VERSION.SDK_INT <= 13)) {
		// if ((Build.VERSION.SDK_INT >= 11)) {
		// if ((Build.VERSION.SDK_INT >= 11)||(mMetrics.densityDpi > 200)) {
//			checkImageSpan_V3(textView, word);
//			return;
		}
		ImageSpan span = null;
		final Integer getres = getDocomoEmojiRes(word);
		if (word.candidate.equals(" ")) {
			span = new ImageSpan(mWnn, R.drawable.word_half_space, DynamicDrawableSpan.ALIGN_BASELINE);
		} else if (word.candidate.equals("\u3000" /* full-width space */)) {
			span = new ImageSpan(mWnn, R.drawable.word_full_space, DynamicDrawableSpan.ALIGN_BASELINE);
		} else if (null != getres) {
			// check docomo emoji
			span = new ImageSpan(mWnn, getres.intValue(), DynamicDrawableSpan.ALIGN_BASELINE);
		}
		if (span != null) {
			final SpannableString spannable = new SpannableString("   ");
			spannable.setSpan(span, 1, 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			textView.setText(spannable);
		}

		if (T) Log.v(TAG, M()+"@out");
	}

	// OS 3.xだとImageSpan設定後にtextViewの縦位置が狂うバグがあるのでこちらで代用
	private void checkImageSpan_V3(final TextView textView, final WnnWord word) {
		if (T) Log.v(TAG, M()+"@in: textView="+textView+", word="+word);

		int res = 0;
		ImageSpan span = null;
		final Integer getres = getDocomoEmojiRes(word);
		if (word.candidate.equals(" ")) {
			res = R.drawable.word_half_space;
		} else if (word.candidate.equals("\u3000" /* full-width space */)) {
			res = R.drawable.word_full_space;
		} else if (null != getres) {
			// check docomo emoji
			res = getres.intValue();
		}
		if (res != 0) {
			textView.setText("");
			Drawable d = mWnn.getResources().getDrawable(res);
			d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
			textView.setCompoundDrawables(null, d, null, null);
		} else {
			textView.setCompoundDrawables(null, null, null, null);
		}

		if (T) Log.v(TAG, M()+"@out");
	}

	/**
	 * Create a view for a candidate.
	 * @return the view
	 */
	public TextView createCandidateView() {
		if (T) Log.v(TAG, M()+"@in");

		final TextView text = new TextView(me);
		final Resources r = me.getResources();
		final int height = getCandidateMinimumHeight();
		text.setTextSize(candidateTextSizeTable[mCandidateTextSizeIndex]);
		text.setBackgroundResource(R.drawable.cand_back);
		text.setGravity(Gravity.CENTER|Gravity.CLIP_VERTICAL);
		text.setSingleLine();
		text.setPadding(1, 1, 1, 1);
		text.setLayoutParams(
		  new LinearLayout.LayoutParams(
		    ViewGroup.LayoutParams.WRAP_CONTENT,
		    ViewGroup.LayoutParams.WRAP_CONTENT,
		    1.0f
		  )
		);
		text.setMinHeight(height);
		text.setMaxHeight(height);
		text.setHeight(height);
		text.setMinimumWidth(getCandidateMinimumWidth());

		if (T) Log.v(TAG, M()+"@out: text="+text);
		return text;
	}

	/**
	 * Clear the list of the normal candidate view.
	 */
	private void clearNormalViewCandidate() {
		if (T) Log.v(TAG, M()+"@in");

		final LinearLayout candidateList = mViewCandidateList1st;
		final int lineNum = getMaxLine();
		for (int i = 0; i < lineNum; i++) {
			final LinearLayout lineView = (LinearLayout)candidateList.getChildAt(i);
			final int size = lineView.getChildCount();
			for (int j = 0; j < size; j++) {
				final View v = lineView.getChildAt(j);
				v.setVisibility(View.GONE);
			}
		}

		if (T) Log.v(TAG, M()+"@out");
	}

	/** @see CandidatesViewManager#clearCandidates */
	public void clearCandidates() {
		if (T) Log.v(TAG, M()+"@in");

		int size = 0;
		checkCandidateTask();
		clearNormalViewCandidate();
		clearIndicateCandidateView();

		final RelativeLayout layout2nd = mViewCandidateList2nd;
		size = layout2nd.getChildCount();
		for (int i = 0; i < size; i++) {
			final View v = layout2nd.getChildAt(i);
			v.setVisibility(View.GONE);
		}

		mLineCount = 1;
		mWordCount = 0;
		mToplineLastId = 0;
		mTotalLastId   = 0;
		mWnnWordArray.clear();
		mWnnWordTextLength.clear();
		mWnnWordOccupyCount.clear();
		mWnnWordTextLength2.clear();
		mWnnWordOccupyCount2.clear();

		mIsFullView = false;
		setViewLayout(CandidatesViewManager.VIEW_TYPE_NORMAL);
		if (mAutoHideMode) {
			setViewLayout(CandidatesViewManager.VIEW_TYPE_CLOSE);
		}

		if (mAutoHideMode && mViewBody.isShown()) {
			mWnn.setCandidatesViewShown(false);
		}
		mCanReadMore = false;

		if (T) Log.v(TAG, M()+"@out");
	}

	/** clear normalCandidate */
	private void clearNormalCandidates() {
		if (T) Log.v(TAG, M()+"@in");

		if (false == mIsCreateFullView) {
			if (T) Log.v(TAG, M()+"return");
			return;
		}
		if (mTotalLastId > mToplineLastId) {
			mCanReadMore = true;
			mIsScroll    = false;
		}

		if (T) Log.v(TAG, M()+"@out");
	}
	/** view fullCandidate */
	private void visibleFullCandidate() {
		if (T) Log.v(TAG, M()+"@in");

		if (false == mIsCreateFullView) {
			if (T) Log.v(TAG, M()+"return");
			return;
		}
		final RelativeLayout layout = mViewCandidateList2nd;
		int size = layout.getChildCount();
		if (size > mTotalLastId) {
			size = mTotalLastId;
		}
		for (int i = 0; i < size; i++) {
			final View v = layout.getChildAt(i);
			v.setVisibility(View.VISIBLE);
		}
		if (mTotalLastId != 0) {
			mCanReadMore = false;
		}

		if (T) Log.v(TAG, M()+"@out");
	}
	/** @see CandidatesViewManager#setPreferences */
	public void setPreferences(final SharedPreferences pref) {
		//
	}

	/**
	 * Process {@code OpenWnnEvent.CANDIDATE_VIEW_TOUCH} event.
	 *
	 * @return      {@code true} if event is processed; {@code false} if otherwise
	 */
	public boolean onTouchSync() {
		return mGestureDetector.onTouchEvent(mMotionEvent);
	}

	/**
	 * Select a candidate.
	 * <br>
	 * This method notices the selected word to {@link NicoWnnG}.
	 *
	 * @param word  The selected word
	 */
	private void selectCandidate(final WnnWord word) {
		if (T) Log.v(TAG, M()+"@in: word="+word);

		setViewLayout(CandidatesViewManager.VIEW_TYPE_NORMAL);
		DefaultSoftKeyboard.doClickFeedback(1);
		mIsLockHScroll = true;
		mWnn.onEvent(new NicoWnnGEvent(NicoWnnGEvent.SELECT_CANDIDATE, word));
		mIsLockHScroll = false;

		if (T) Log.v(TAG, M()+"@out");
	}

	private void deleteCandidate(final WnnWord word) {
		if (T) Log.v(TAG, M()+"@in: word="+word);

		setViewLayout(CandidatesViewManager.VIEW_TYPE_NORMAL);
		DefaultSoftKeyboard.doClickFeedback(1);
		mIsLockHScroll = true;
		mWnn.onEvent(new NicoWnnGEvent(NicoWnnGEvent.FORGET_CANDIDATE, word));
		mIsLockHScroll = false;

		if (T) Log.v(TAG, M()+"@out");
	}

	/** @see android.view.GestureDetector.OnGestureListener#onDown */
	public boolean onDown(final MotionEvent arg0) {
		return false;
	}

	/** @see android.view.GestureDetector.OnGestureListener#onFling */
	public boolean onFling(final MotionEvent e1, final MotionEvent e2, final float veloX, final float veloY) {
		if (mIsScaleUp) {
			return false;
		}
		boolean consumed = false;

		int gety = 0;
		if ((e1 != null) && (e2 != null)) {
			gety = (int)(e2.getRawY() - e1.getRawY());
		}
		int getx = 0;
		if ((e1 != null) && (e2 != null)) {
			getx = (int)(e2.getRawX() - e1.getRawX());
		}

		int w = (int)(getCandidateMinimumHeight()*1);
		//if (arg3 < (float)-(getCandidateMinimumHeight() * 10)) {
		if (!mIsFullView) {
			if ((-w < getx) && (getx < w)) {
				if (gety < -w) {
					if ((mViewType == CandidatesViewManager.VIEW_TYPE_NORMAL) && mCanReadMore) {
						DefaultSoftKeyboard.doClickFeedback(2);
						mIsFullView = true;
						mIsScroll   = false; // reset scroll flag
						mWnn.onEvent(new NicoWnnGEvent(NicoWnnGEvent.LIST_CANDIDATES_FULL));
						consumed = true;
					}
				}
			}
		}
		//else if (arg3 > (float)getCandidateMinimumHeight()) {
		else {
			if ((-w < gety) && (gety < w)) {
				if ((getx < -w) || (w < getx)) {
					// if ((mViewBodyVScroll2nd.getScrollY() == 0) && (true == mIsFullView)) {
					/*
				if (true == mIsScroll) {
					mIsScroll = false;
					return false;
				}
					 */
					DefaultSoftKeyboard.doClickFeedback(2);
					mIsFullView = false;
					mWnn.onEvent(new NicoWnnGEvent(NicoWnnGEvent.LIST_CANDIDATES_NORMAL));
					consumed = true;
				}
			}
		}
		return consumed;
	}

	/** @see android.view.GestureDetector.OnGestureListener#onLongPress */
	public void onLongPress(final MotionEvent arg0) {
		return;
	}

	/** @see android.view.GestureDetector.OnGestureListener#onScroll */
	public boolean onScroll(final MotionEvent arg0, final MotionEvent arg1, final float arg2, final float arg3) {
		if (mViewBodyVScroll2nd.getScrollY() != 0) {
			mIsScroll = true;
		}
		return false;
	}
	/** @see android.view.GestureDetector.OnGestureListener#onShowPress */
	public void onShowPress(final MotionEvent arg0) {
	}

	/** @see android.view.GestureDetector.OnGestureListener#onSingleTapUp */
	public boolean onSingleTapUp(final MotionEvent arg0) {
		return false;
	}

	/**
	 * Retrieve the width of string to draw.
	 *
	 * @param text          The string
	 * @param start         The start position (specified by the number of character)
	 * @param end           The end position (specified by the number of character)
	 * @return          The width of string to draw
	 */
	public int measureText(final CharSequence text, final int start, final int end) {
		if (T) Log.v(TAG, M()+"@in: text="+text+", start="+start+", end="+end);

		final TextPaint paint = mViewCandidateTemplate.getPaint();
		final int getwidth = Math.max((int)paint.measureText(text, start, end)+8, getCandidateMinimumWidth());
		//final int getwidth = Math.max((int)paint.measureText(text, start, end), CANDIDATE_MINIMUM_WIDTH);
		//int getwidth = Math.max((int)(end - start) * 40, CANDIDATE_MINIMUM_WIDTH);

		if (T) Log.v(TAG, M()+"@out: getwidth="+getwidth);
		return getwidth;
	}

	/**
	 * Switch list/enlarge view mode.
	 * @param up  {@code true}:enlarge, {@code false}:list
	 * @param word  The candidate word to be enlarged.
	 */
	private void setViewScaleUp(final boolean up, final WnnWord word) {
		if (T) Log.v(TAG, M()+"@in: up="+up+", word="+word);

		if ((up == mIsScaleUp) || (mViewScaleUp == null)) {
			if (T) Log.v(TAG, M()+"return");
			return;
		}
		if (up) {
			setViewLayout(CandidatesViewManager.VIEW_TYPE_NORMAL);
			mViewCandidateList1st.setVisibility(View.GONE);
			mViewCandidateList2nd.setVisibility(View.GONE);
			mViewCandidateBase.setMinimumHeight(-1);
			mViewCandidateBase.addView(mViewScaleUp);
			final TextView text = (TextView)mViewScaleUp.findViewById(R.id.candidate_scale_up_text);
			text.setText(word.candidate);
			checkImageSpan(text, word);
			if (!mPortrait) {
				final Resources r = me.getResources();
				text.setTextSize(r.getDimensionPixelSize(R.dimen.candidate_delete_word_size_landscape));
			}

			mIsScaleUp = true;
		} else {
			mIsScaleUp = false;
			mViewCandidateBase.removeView(mViewScaleUp);
		}
		if (T) Log.v(TAG, M()+"@out");
	}

	/**
	 * Create a layout for the next line.
	 */
	private void createNextLine() {
		if (T) Log.v(TAG, M()+"@in");

		mFullViewOccupyCount = 0;
		mFullViewPrevLineTopId = mFullViewPrevView.getId();
		mLineCount++;

		if (T) Log.v(TAG, M()+"@out");
	}

	/**
	 * @return the minimum width of a candidate view.
	 */
	public static int getCandidateMinimumWidth() {
		return (int)(CANDIDATE_MINIMUM_WIDTH * mMetrics.density);
		//return (CANDIDATE_MINIMUM_WIDTH);
	}

	/**
	 * @return the minimum height of a candidate view.
	 */
	public static int getCandidateMinimumHeight() {
		return (int)(candidateViewDataTable[mCandidateViewHeightIndex] * mMetrics.density);
		//return (candidateViewDataTable[mCandidateViewHeightIndex]);
	}

	/**
	 * get docomo-emoji resource
	 */
	private Integer getDocomoEmojiRes(final WnnWord word) {
		return DOCOMO_EMOJI_TABLE.get(word.candidate);
	}

	public int getCandidateTextSize() {
		return candidateTextSizeTable[mCandidateTextSizeIndex];
	}
}
