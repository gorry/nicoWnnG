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

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import androidx.fragment.app.FragmentTransaction;

public class ActivityNicoWnnGSetting extends AppCompatActivity {


	public static ActivityNicoWnnGSetting me;
	public static ActivityNicoWnnGSetting getInstance() {
		return me;
	}

	private FragmentNicoWnnGSetting mFragment;
	private boolean mIsLandscape;
	private MyDocumentTreeSelector mMyDocumentTreeSelector;


	@Override public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		me = this;

		mMyDocumentTreeSelector = new MyDocumentTreeSelector(this);
		mIsLandscape = (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE);

		setContentView(R.layout.activity_setting);
		if (savedInstanceState == null) {
			mFragment = new FragmentNicoWnnGSetting(me);
			FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
			transaction.replace(R.id.fragment_setting, mFragment);
			transaction.commit();
		}
	}

	public void copyInPreferences(final boolean sw, final SharedPreferences pref) {
		mFragment.copyInPreferences(sw, pref);
	}

	public void copyOutPreferences(final boolean sw, final SharedPreferences pref) {
		mFragment.copyOutPreferences(sw, pref);
	}

	public boolean isLandscape() {
		return mIsLandscape;
	}

	public MyDocumentTreeSelector getMyDocumentTreeSelector() {
		return mMyDocumentTreeSelector;
	}

}
