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

import net.gorry.android.input.nicownng.*;

import android.content.Context;
import androidx.preference.ListPreference;
import android.util.AttributeSet;

/**
 * The preference class of keyboard image list for Japanese IME.
 * This class notices to {@code OpenWnnJAJP} that the keyboard image is changed.
 * 
 * @author Copyright (C) 2009, OMRON SOFTWARE CO., LTD.
 */
public class KeyboardListPreferenceJAJP extends ListPreference {

	public KeyboardListPreferenceJAJP(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    public KeyboardListPreferenceJAJP(Context context) {
        this(context, null);
    }

    // @Override
	public void onDialogClosed(boolean positiveResult) {
    	// super.onDialogClosed(positiveResult);
    	
    	if (positiveResult) {
    		NicoWnnGJAJP wnn = NicoWnnGJAJP.getInstance();
        	int code = NicoWnnGEvent.CHANGE_INPUT_VIEW;
        	NicoWnnGEvent ev = new NicoWnnGEvent(code);
        	try {
        		wnn.onEvent(ev);
        	} catch (Exception ex) {
        	}   		
    	}
    }
}
