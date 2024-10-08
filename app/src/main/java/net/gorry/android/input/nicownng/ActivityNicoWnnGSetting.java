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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import net.gorry.mydocument.MyDocumentFileSelector;
import net.gorry.mydocument.MyDocumentTreeSelector;

public class ActivityNicoWnnGSetting extends AppCompatActivity implements PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {


	public static ActivityNicoWnnGSetting me;
	public static ActivityNicoWnnGSetting getInstance() {
		return me;
	}

	private FragmentNicoWnnGSetting mFragment;
	private MyDocumentTreeSelector mMyDocumentTreeSelector;
	private MyDocumentFileSelector mMyDocumentFileSelector;


	@Override public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		me = this;

		mMyDocumentTreeSelector = new MyDocumentTreeSelector(this);
		mMyDocumentFileSelector = new MyDocumentFileSelector(this);

		setContentView(R.layout.activity_setting);
		if (savedInstanceState == null) {
			mFragment = new FragmentNicoWnnGSetting();
			// mFragment.setParentActivity(me);
			FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
			transaction
				.replace(R.id.fragment_setting, mFragment)
				.commit();
		}
	}

	public void copyInPreferences(final boolean sw, final SharedPreferences pref) {
		mFragment.copyInPreferences(sw, pref);
	}

	public void copyOutPreferences(final boolean sw, final SharedPreferences pref) {
		mFragment.copyOutPreferences(sw, pref);
	}

	public MyDocumentTreeSelector getMyDocumentTreeSelector() {
		return mMyDocumentTreeSelector;
	}

	public MyDocumentFileSelector getMyDocumentFileSelector() {
		return mMyDocumentFileSelector;
	}

	@Override
	public boolean onSupportNavigateUp() {
		if (getSupportFragmentManager().popBackStackImmediate()) {
			return true;
		}
		return super.onSupportNavigateUp();
	}


	@Override
	public boolean onPreferenceStartFragment(@NonNull PreferenceFragmentCompat caller, @NonNull Preference pref) {
		Bundle args = pref.getExtras();
		Fragment fragment = getSupportFragmentManager().getFragmentFactory().instantiate(
				getClassLoader(),
				pref.getFragment()
		);
		fragment.setArguments(args);
		fragment.setTargetFragment(caller, 0);
		// Replace the existing Fragment with the new Fragment
		getSupportFragmentManager()
				.beginTransaction()
				.replace(R.id.fragment_setting, fragment)
				.addToBackStack(null)
				.commit();
		return true;
	}

	public FragmentNicoWnnGSetting getFragment() {
		return mFragment;
	}

}
