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

package net.gorry.android.input.nicownng.JAJP;

import java.util.Comparator;

import net.gorry.android.input.nicownng.NicoWnnG;
import net.gorry.android.input.nicownng.NicoWnnGEvent;
import net.gorry.android.input.nicownng.NicoWnnGJAJP;
import net.gorry.android.input.nicownng.R;
import net.gorry.android.input.nicownng.UserDictionaryToolsEdit;
import net.gorry.android.input.nicownng.UserDictionaryToolsList;
import net.gorry.android.input.nicownng.WnnWord;
import android.view.View;
import android.view.Window;

/**
 * The user dictionary tool class for Japanese IME.
 *
 * @author Copyright (C) 2009 OMRON SOFTWARE CO., LTD.  All Rights Reserved.
 */
public class UserDictionaryToolsListJAJP extends UserDictionaryToolsList {
	/**
	 * Constructor
	 */
	public UserDictionaryToolsListJAJP() {
		mListViewName = "net.gorry.android.input.nicownng.JAJP.UserDictionaryToolsListJAJP";
		mEditViewName = "net.gorry.android.input.nicownng.JAJP.UserDictionaryToolsEditJAJP";
		mImportExportName = "UserDicJAJP.xml";
		mLearnFlashName   = NicoWnnGJAJP.getInstance().writableJAJPDic;
		mLearnSDName      = NicoWnnG.writableJAJPBaseName;
		mPackageName  = "net.gorry.android.input.nicownng";
	}

	/** @see net.gorry.android.input.nicownng.UserDictionaryToolsList#headerCreate */
	@Override protected void headerCreate() {
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.user_dictionary_tools_list_header_ja);
	}

	/** @see net.gorry.android.input.nicownng.UserDictionaryToolsList#createUserDictionaryToolsEdit */
	@Override protected UserDictionaryToolsEdit createUserDictionaryToolsEdit(final View view1, final View view2) {
		return new UserDictionaryToolsEditJAJP(view1, view2);
	}

	/** @see net.gorry.android.input.nicownng.UserDictionaryToolsList#sendEventToIME */
	@Override protected boolean sendEventToIME(final NicoWnnGEvent ev) {
		try {
			return NicoWnnGJAJP.getInstance().onEvent(ev);
		} catch (final Exception ex) {
			/* do nothing if an error occurs */
		}
		return false;
	}

	/** @see net.gorry.android.input.nicownng.UserDictionaryToolsList#getComparator */
	@Override protected Comparator<WnnWord> getComparator() {
		return new ListComparatorJAJP();
	}

	/** Comparator class for sorting the list of Japanese user dictionary */
	protected class ListComparatorJAJP implements Comparator<WnnWord>{
		public int compare(final WnnWord word1, final WnnWord word2) {
			return word1.stroke.compareTo(word2.stroke);
		};
	}
}
