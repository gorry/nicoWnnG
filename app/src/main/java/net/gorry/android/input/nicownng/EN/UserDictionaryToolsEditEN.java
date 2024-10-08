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

package net.gorry.android.input.nicownng.EN;

import net.gorry.android.input.nicownng.*;

import android.view.View;

/**
 * The user dictionary's word editor class for English IME.
 *
 * @author Copyright (C) 2009 OMRON SOFTWARE CO., LTD.  All Rights Reserved.
 */
public class UserDictionaryToolsEditEN extends UserDictionaryToolsEdit {
    /**
     * Constructor
     */
    public UserDictionaryToolsEditEN() {
        super();
        initialize();
    }

    /**
     * Constructor 
     *
     * @param focusView         The view
     * @param focusPairView     The pair view
     */
    public UserDictionaryToolsEditEN(View focusView, View focusPairView) {
        super(focusView, focusPairView);
        initialize();
    }

    /**
     * Initialize the parameters
     */
    public void initialize() {
		mPackageName  = NicoWnnG.PACKAGE_NAME;
        mListViewName = mPackageName+".EN.UserDictionaryToolsListEN";
    }

    /** @see net.gorry.android.input.nicownng.UserDictionaryToolsEdit#sendEventToIME */
    @Override protected boolean sendEventToIME(NicoWnnGEvent ev) {
        try {
            return NicoWnnGEN.getInstance().onEvent(ev);
        } catch (Exception ex) {
            /* do nothing if an error occurs */
        }
        return false;
    }

    /** @see net.gorry.android.input.nicownng.UserDictionaryToolsEdit#createUserDictionaryToolsList */
    @Override protected UserDictionaryToolsList createUserDictionaryToolsList() {
        return new UserDictionaryToolsListEN();
    }
}
