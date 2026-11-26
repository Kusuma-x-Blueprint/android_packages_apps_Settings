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

package com.android.settings.gestures;

import android.app.settings.SettingsEnums;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

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
public class QuickPulldownGestureSettings extends DashboardFragment
        implements PreferenceControllerMixin {

    private static final String TAG = "QuickPulldownGestureSettings";
    private static final String PREF_LAST_PULLDOWN_SIDE = "qs_quick_pulldown_last_side";

    static final int PULLDOWN_DIR_NONE = 0;
    static final int PULLDOWN_DIR_RIGHT = 1;
    static final int PULLDOWN_DIR_LEFT = 2;

    static int getPulldownMode(Context context) {
        return LineageSettings.System.getInt(context.getContentResolver(),
                LineageSettings.System.STATUS_BAR_QUICK_QS_PULLDOWN, PULLDOWN_DIR_NONE);
    }

    static int getLastSide(Context context) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        final int saved = prefs.getInt(PREF_LAST_PULLDOWN_SIDE, PULLDOWN_DIR_RIGHT);
        return saved != PULLDOWN_DIR_NONE ? saved : PULLDOWN_DIR_RIGHT;
    }

    static void saveLastSide(Context context, int side) {
        if (side != PULLDOWN_DIR_NONE) {
            PreferenceManager.getDefaultSharedPreferences(context)
                    .edit()
                    .putInt(PREF_LAST_PULLDOWN_SIDE, side)
                    .apply();
        }
    }

    static boolean setPulldownMode(Context context, int mode) {
        saveLastSide(context, mode);
        return LineageSettings.System.putInt(context.getContentResolver(),
                LineageSettings.System.STATUS_BAR_QUICK_QS_PULLDOWN, mode);
    }

    static boolean setPulldownEnabled(Context context, boolean enabled) {
        final int current = getPulldownMode(context);
        if (enabled) {
            final int target = current != PULLDOWN_DIR_NONE ? current : getLastSide(context);
            return LineageSettings.System.putInt(context.getContentResolver(),
                    LineageSettings.System.STATUS_BAR_QUICK_QS_PULLDOWN, target);
        }
        saveLastSide(context, current);
        return LineageSettings.System.putInt(context.getContentResolver(),
                LineageSettings.System.STATUS_BAR_QUICK_QS_PULLDOWN, PULLDOWN_DIR_NONE);
    }

    static boolean isRtlMode(Resources res) {
        return res.getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
    }

    static ContentObserver observeSetting(Context context, Runnable onChangeCallback) {
        final Uri uri = LineageSettings.System.getUriFor(
                LineageSettings.System.STATUS_BAR_QUICK_QS_PULLDOWN);
        final ContentObserver observer = new ContentObserver(new Handler(Looper.getMainLooper())) {
            @Override
            public void onChange(boolean selfChange, Uri changedUri) {
                if (changedUri == null || uri.equals(changedUri)) {
                    onChangeCallback.run();
                }
            }
        };
        context.getContentResolver().registerContentObserver(uri, false, observer);
        return observer;
    }

    @Override
    protected List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        return buildPreferenceControllers(context, getSettingsLifecycle());
    }

    private static List<AbstractPreferenceController> buildPreferenceControllers(Context context,
            Lifecycle lifecycle) {
        List<AbstractPreferenceController> controllers = new ArrayList<>();
        controllers.add(new QuickPulldownSwitchPreferenceController(context, lifecycle));
        controllers.add(new QuickPulldownSidePreferenceController(context, lifecycle));
        return controllers;
    }

    @Override
    public int getMetricsCategory() {
        return SettingsEnums.PAGE_UNKNOWN;
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.quick_pulldown_gesture_settings;
    }

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.quick_pulldown_gesture_settings) {

                @Override
                public List<AbstractPreferenceController> createPreferenceControllers(
                        Context context) {
                    return buildPreferenceControllers(context, null);
                }
            };
}
