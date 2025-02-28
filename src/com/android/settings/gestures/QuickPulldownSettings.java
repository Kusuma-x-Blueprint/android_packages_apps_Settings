/*
 * Copyright (C) 2019 The Android Open Source Project
 * Copyright (C) 2024 Kusuma
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
import android.content.SharedPreferences;
import android.provider.SearchIndexableResource;

import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;

import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.core.AbstractPreferenceController;

import java.util.ArrayList;
import java.util.List;

@SearchIndexable
public class QuickPulldownSettings extends DashboardFragment {

    private static final String TAG = "QuickPulldownSettings";

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    protected List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        return buildPreferenceControllers(context, getSettingsLifecycle());
    }

    private static List<AbstractPreferenceController> buildPreferenceControllers(Context context,
            Lifecycle lifecycle) {
        List<AbstractPreferenceController> controllers = new ArrayList<>();
        controllers.add(new QuickPulldownMainSwitchPreferenceController(context));
        controllers.add(new QuickPulldownSelectorPreferenceController(context, lifecycle));
        return controllers;
    }

    @Override
    public int getMetricsCategory() {
        return -1;
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.quick_pulldown_settings;
    }

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.quick_pulldown_settings) {
        @Override
        public List<AbstractPreferenceController> createPreferenceControllers(
            Context context) {
                return buildPreferenceControllers(context, null);
            }
        };
}
