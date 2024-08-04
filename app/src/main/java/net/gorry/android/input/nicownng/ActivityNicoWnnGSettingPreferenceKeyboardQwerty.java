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
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import net.gorry.mydocument.MyDocumentFileSelector;
import net.gorry.mydocument.MyDocumentTreeSelector;

public class ActivityNicoWnnGSettingPreferenceKeyboardQwerty extends PreferenceFragmentCompat {
	public ActivityNicoWnnGSettingPreferenceKeyboardQwerty() {
	}

	@Override
	public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
		setPreferencesFromResource(R.xml.nicownng_pref_ja_keyboard_qwerty, rootKey);
	}
}
