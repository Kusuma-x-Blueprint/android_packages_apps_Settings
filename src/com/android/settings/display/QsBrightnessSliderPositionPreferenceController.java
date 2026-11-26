/*
 * Copyright (C) 2026 Pelita
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

package com.android.settings.display;

import android.content.Context;
import android.net.Uri;

import com.android.settings.R;
import com.android.settingslib.core.lifecycle.Lifecycle;

import lineageos.providers.LineageSettings;

public class QsBrightnessSliderPositionPreferenceController
        extends QsBrightnessRadioCategoryController {

    private static final String KEY = "qs_brightness_slider_position_category";
    private static final String PREF_KEY_PREFIX = "qs_brightness_slider_position_";

    public QsBrightnessSliderPositionPreferenceController(Context context, Lifecycle lifecycle) {
        super(context, lifecycle, KEY, PREF_KEY_PREFIX,
                R.array.qs_brightness_slider_position_entries,
                R.array.qs_brightness_slider_position_values);
    }

    @Override
    protected Uri getObservedUri() {
        return LineageSettings.Secure.getUriFor(
                LineageSettings.Secure.QS_BRIGHTNESS_SLIDER_POSITION);
    }

    @Override
    protected int getSelectedValue() {
        return LineageSettings.Secure.getInt(mContext.getContentResolver(),
                LineageSettings.Secure.QS_BRIGHTNESS_SLIDER_POSITION,
                QsBrightnessSliderSettings.POSITION_TOP);
    }

    @Override
    protected void writeSelectedValue(int value) {
        LineageSettings.Secure.putInt(mContext.getContentResolver(),
                LineageSettings.Secure.QS_BRIGHTNESS_SLIDER_POSITION, value);
    }
}
