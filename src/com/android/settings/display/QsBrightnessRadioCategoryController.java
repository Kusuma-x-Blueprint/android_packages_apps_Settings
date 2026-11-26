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

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;

import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnPause;
import com.android.settingslib.core.lifecycle.events.OnResume;
import com.android.settingslib.widget.SelectorWithWidgetPreference;

import lineageos.providers.LineageSettings;

import java.util.LinkedHashMap;
import java.util.Map;

abstract class QsBrightnessRadioCategoryController extends AbstractPreferenceController
        implements SelectorWithWidgetPreference.OnClickListener, LifecycleObserver,
        OnResume, OnPause, PreferenceControllerMixin {

    private final Map<Integer, SelectorWithWidgetPreference> mPreferences = new LinkedHashMap<>();
    private final String mCategoryKey;
    private final String mPrefKeyPrefix;
    private final int mEntriesResId;
    private final int mValuesResId;

    private PreferenceCategory mPreferenceCategory;
    private SettingObserver mSettingObserver;

    QsBrightnessRadioCategoryController(Context context, Lifecycle lifecycle,
            String categoryKey, String prefKeyPrefix, int entriesResId, int valuesResId) {
        super(context);
        mCategoryKey = categoryKey;
        mPrefKeyPrefix = prefKeyPrefix;
        mEntriesResId = entriesResId;
        mValuesResId = valuesResId;
        if (lifecycle != null) {
            lifecycle.addObserver(this);
        }
    }

    protected abstract Uri getObservedUri();

    protected abstract int getSelectedValue();

    protected abstract void writeSelectedValue(int value);

    @Override
    public String getPreferenceKey() {
        return mCategoryKey;
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

        final String[] entries = mContext.getResources().getStringArray(mEntriesResId);
        final String[] values = mContext.getResources().getStringArray(mValuesResId);

        for (int i = 0; i < values.length; i++) {
            final int value = Integer.parseInt(values[i]);
            SelectorWithWidgetPreference pref = new SelectorWithWidgetPreference(
                    mPreferenceCategory.getContext());
            pref.setKey(mPrefKeyPrefix + value);
            pref.setTitle(entries[i]);
            pref.setOnClickListener(this);
            mPreferenceCategory.addPreference(pref);
            mPreferences.put(value, pref);
        }

        mSettingObserver = new SettingObserver(mPreferenceCategory);
    }

    @Override
    public void onRadioButtonClicked(SelectorWithWidgetPreference preference) {
        for (Map.Entry<Integer, SelectorWithWidgetPreference> entry : mPreferences.entrySet()) {
            if (entry.getValue() == preference) {
                writeSelectedValue(entry.getKey());
                break;
            }
        }
    }

    @Override
    public void updateState(Preference preference) {
        final boolean isSliderEnabled = QsBrightnessSliderSettings.getShowMode(mContext)
                != QsBrightnessSliderSettings.SLIDER_OFF;
        final int selectedValue = getSelectedValue();

        for (Map.Entry<Integer, SelectorWithWidgetPreference> entry : mPreferences.entrySet()) {
            final boolean isChecked = entry.getKey() == selectedValue;
            if (entry.getValue().isChecked() != isChecked) {
                entry.getValue().setChecked(isChecked);
            }
            entry.getValue().setEnabled(isSliderEnabled);
        }
    }

    @Override
    public void onResume() {
        if (mSettingObserver != null) {
            mSettingObserver.register(mContext.getContentResolver());
            mSettingObserver.onChange(false, null);
        }
    }

    @Override
    public void onPause() {
        if (mSettingObserver != null) {
            mSettingObserver.unregister(mContext.getContentResolver());
        }
    }

    private class SettingObserver extends ContentObserver {
        private final Uri SHOW_SLIDER_URI = LineageSettings.Secure.getUriFor(
                LineageSettings.Secure.QS_SHOW_BRIGHTNESS_SLIDER);
        private final Uri TARGET_URI = getObservedUri();
        private final Preference mPreference;

        SettingObserver(Preference preference) {
            super(new Handler(Looper.getMainLooper()));
            mPreference = preference;
        }

        void register(ContentResolver cr) {
            cr.registerContentObserver(SHOW_SLIDER_URI, false, this);
            if (!SHOW_SLIDER_URI.equals(TARGET_URI)) {
                cr.registerContentObserver(TARGET_URI, false, this);
            }
        }

        void unregister(ContentResolver cr) {
            cr.unregisterContentObserver(this);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            if (uri == null || SHOW_SLIDER_URI.equals(uri) || TARGET_URI.equals(uri)) {
                updateState(mPreference);
            }
        }
    }
}
