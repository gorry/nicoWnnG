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

import net.gorry.android.input.nicownng.NicoWnnGEvent;
import net.gorry.android.input.nicownng.NicoWnnGJAJP;
import net.gorry.android.input.nicownng.UserDictionaryToolsEdit;
import net.gorry.android.input.nicownng.UserDictionaryToolsList;
import android.view.View;

/**
 * The user dictionary's word editor class for Japanese IME.
 *
 * @author Copyright (C) 2009 OMRON SOFTWARE CO., LTD.  All Rights Reserved.
 */
public class UserDictionaryToolsEditJAJP extends UserDictionaryToolsEdit {
	/**
	 * Constructor
	 */
	public UserDictionaryToolsEditJAJP() {
		super();
		initialize();
	}

	/**
	 * Constructor
	 *
	 * @param focusView         The view
	 * @param focusPairView     The pair view
	 */
	public UserDictionaryToolsEditJAJP(final View focusView, final View focusPairView) {
		super(focusView, focusPairView);
		initialize();
	}

	/**
	 * Initialize the parameters
	 */
	public void initialize() {
		mListViewName = "net.gorry.android.input.nicownng.JAJP.UserDictionaryToolsListJAJP";
		mPackageName  = "net.gorry.android.input.nicownng";
	}

	/** @see net.gorry.android.input.nicownng.UserDictionaryToolsEdit#sendEventToIME */
	@Override protected boolean sendEventToIME(final NicoWnnGEvent ev) {
		try {
			return NicoWnnGJAJP.getInstance().onEvent(ev);
		} catch (final Exception ex) {
			/* do nothing if an error occurs */
		}
		return false;
	}

	/** @see net.gorry.android.input.nicownng.UserDictionaryToolsEdit#createUserDictionaryToolsList */
	@Override protected UserDictionaryToolsList createUserDictionaryToolsList() {
		return new UserDictionaryToolsListJAJP();
	}
}
