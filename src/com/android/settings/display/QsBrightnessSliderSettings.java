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

import android.app.settings.SettingsEnums;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.preference.PreferenceManager;

import com.android.settings.R;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.search.SearchIndexable;

import lineageos.providers.LineageSettings;

import java.util.ArrayList;
import java.util.List;

@SearchIndexable
public class QsBrightnessSliderSettings extends DashboardFragment
        implements PreferenceControllerMixin {

    private static final String TAG = "QsBrightnessSliderSettings";
    private static final String PREF_LAST_SHOW_MODE = "qs_show_brightness_slider_last_mode";
    private static final String KEY_AUTO_BRIGHTNESS_CATEGORY = "qs_show_auto_brightness_category";
    private static final String KEY_AUTO_BRIGHTNESS = "qs_show_auto_brightness";

    static final int SLIDER_OFF = 0;
    static final int SLIDER_EXPANDED = 1;
    static final int SLIDER_ALWAYS = 2;

    static final int POSITION_TOP = 0;

    static int getShowMode(Context context) {
        return LineageSettings.Secure.getInt(context.getContentResolver(),
                LineageSettings.Secure.QS_SHOW_BRIGHTNESS_SLIDER, SLIDER_EXPANDED);
    }

    static int getLastShowMode(Context context) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        final int saved = prefs.getInt(PREF_LAST_SHOW_MODE, SLIDER_EXPANDED);
        return saved != SLIDER_OFF ? saved : SLIDER_EXPANDED;
    }

    static void saveLastShowMode(Context context, int mode) {
        if (mode != SLIDER_OFF) {
            PreferenceManager.getDefaultSharedPreferences(context)
                    .edit()
                    .putInt(PREF_LAST_SHOW_MODE, mode)
                    .apply();
        }
    }

    static boolean setShowMode(Context context, int mode) {
        saveLastShowMode(context, mode);
        return LineageSettings.Secure.putInt(context.getContentResolver(),
                LineageSettings.Secure.QS_SHOW_BRIGHTNESS_SLIDER, mode);
    }

    static boolean setSliderEnabled(Context context, boolean enabled) {
        final int current = getShowMode(context);
        if (enabled) {
            final int target = current != SLIDER_OFF ? current : getLastShowMode(context);
            return LineageSettings.Secure.putInt(context.getContentResolver(),
                    LineageSettings.Secure.QS_SHOW_BRIGHTNESS_SLIDER, target);
        }
        saveLastShowMode(context, current);
        return LineageSettings.Secure.putInt(context.getContentResolver(),
                LineageSettings.Secure.QS_SHOW_BRIGHTNESS_SLIDER, SLIDER_OFF);
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        if (!getResources().getBoolean(
                com.android.internal.R.bool.config_automatic_brightness_available)) {
            removePreference(KEY_AUTO_BRIGHTNESS_CATEGORY);
        }
    }

    @Override
    protected List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        return buildPreferenceControllers(context, getSettingsLifecycle());
    }

    private static List<AbstractPreferenceController> buildPreferenceControllers(Context context,
            Lifecycle lifecycle) {
        List<AbstractPreferenceController> controllers = new ArrayList<>();
        controllers.add(new QsBrightnessSliderSwitchPreferenceController(context, lifecycle));
        controllers.add(new QsBrightnessSliderShowPreferenceController(context, lifecycle));
        controllers.add(new QsBrightnessSliderPositionPreferenceController(context, lifecycle));
        return controllers;
    }

    @Override
    public int getMetricsCategory() {
        return SettingsEnums.DISPLAY;
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.qs_brightness_slider_settings;
    }

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.qs_brightness_slider_settings) {

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    List<String> keys = super.getNonIndexableKeys(context);
                    if (!context.getResources().getBoolean(
                            com.android.internal.R.bool.config_automatic_brightness_available)) {
                        keys.add(KEY_AUTO_BRIGHTNESS);
                    }
                    return keys;
                }

                @Override
                public List<AbstractPreferenceController> createPreferenceControllers(
                        Context context) {
                    return buildPreferenceControllers(context, null);
                }
            };
}
