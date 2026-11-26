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

import android.content.Context;
import android.content.res.Resources;
import android.database.ContentObserver;

import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;

import com.android.settings.R;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnPause;
import com.android.settingslib.core.lifecycle.events.OnResume;
import com.android.settingslib.widget.SelectorWithWidgetPreference;

import java.util.LinkedHashMap;
import java.util.Map;

public class QuickPulldownSidePreferenceController extends AbstractPreferenceController
        implements PreferenceControllerMixin, SelectorWithWidgetPreference.OnClickListener,
        LifecycleObserver, OnResume, OnPause {

    private static final String KEY = "qs_quick_pulldown_side_category";
    private static final String PREF_KEY_PREFIX = "qs_quick_pulldown_side_";

    private final Map<Integer, SelectorWithWidgetPreference> mPreferences = new LinkedHashMap<>();

    private PreferenceCategory mPreferenceCategory;
    private ContentObserver mSettingObserver;

    public QuickPulldownSidePreferenceController(Context context, Lifecycle lifecycle) {
        super(context);
        if (lifecycle != null) {
            lifecycle.addObserver(this);
        }
    }

    @Override
    public String getPreferenceKey() {
        return KEY;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        mPreferenceCategory = screen.findPreference(getPreferenceKey());
        if (mPreferenceCategory == null) {
            return;
        }

        mPreferenceCategory.removeAll();
        mPreferences.clear();

        final Resources res = mContext.getResources();
        final int entriesRes = QuickPulldownGestureSettings.isRtlMode(res)
                ? R.array.qs_quick_pulldown_entries_rtl
                : R.array.qs_quick_pulldown_entries;
        final String[] entries = res.getStringArray(entriesRes);
        final String[] values = res.getStringArray(R.array.qs_quick_pulldown_values);

        for (int i = 0; i < values.length; i++) {
            final int value = Integer.parseInt(values[i]);
            final SelectorWithWidgetPreference pref =
                    new SelectorWithWidgetPreference(mPreferenceCategory.getContext());
            pref.setKey(PREF_KEY_PREFIX + value);
            pref.setTitle(entries[i]);
            pref.setOnClickListener(this);
            mPreferenceCategory.addPreference(pref);
            mPreferences.put(value, pref);
        }

        updateState(mPreferenceCategory);
    }

    @Override
    public void onRadioButtonClicked(SelectorWithWidgetPreference preference) {
        for (Map.Entry<Integer, SelectorWithWidgetPreference> entry : mPreferences.entrySet()) {
            if (entry.getValue() == preference) {
                QuickPulldownGestureSettings.setPulldownMode(mContext, entry.getKey());
                break;
            }
        }
    }

    @Override
    public void updateState(Preference preference) {
        final int mode = QuickPulldownGestureSettings.getPulldownMode(mContext);
        final boolean isEnabled = mode != QuickPulldownGestureSettings.PULLDOWN_DIR_NONE;
        final int selectedValue;
        if (isEnabled) {
            QuickPulldownGestureSettings.saveLastSide(mContext, mode);
            selectedValue = mode;
        } else {
            selectedValue = QuickPulldownGestureSettings.getLastSide(mContext);
        }

        for (Map.Entry<Integer, SelectorWithWidgetPreference> entry : mPreferences.entrySet()) {
            final SelectorWithWidgetPreference pref = entry.getValue();
            final boolean isChecked = entry.getKey() == selectedValue;
            if (pref.isChecked() != isChecked) {
                pref.setChecked(isChecked);
            }
            pref.setEnabled(isEnabled);
        }
    }

    @Override
    public void onResume() {
        if (mPreferenceCategory != null && mSettingObserver == null) {
            mSettingObserver = QuickPulldownGestureSettings.observeSetting(
                    mContext, () -> updateState(mPreferenceCategory));
            updateState(mPreferenceCategory);
        }
    }

    @Override
    public void onPause() {
        if (mSettingObserver != null) {
            mContext.getContentResolver().unregisterContentObserver(mSettingObserver);
            mSettingObserver = null;
        }
    }
}
