/*
 * Copyright (C) 2024 Project Lineage Remix Open Source
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

package com.android.settings.gestures;

import android.content.Context;
import android.provider.Settings;

import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;

public class ButtonCombinationPreferenceController extends BasePreferenceController {

    public ButtonCombinationPreferenceController(Context context, String key) {
        super(context, key);
    }

    @Override
    public CharSequence getSummary() {
        boolean onlyScreenOn = Settings.Secure.getInt(mContext.getContentResolver(),
                Settings.Secure.KEY_COMBINATION_WAKEUP, 1) == 0;
        if (onlyScreenOn) {
            return mContext.getText(R.string.button_combination_summary_only_screen_on);
        } else {
            return mContext.getText(R.string.button_combination_summary_always_on);
        }
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }
}
