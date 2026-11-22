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

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

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

import lineageos.providers.LineageSettings;

import java.util.LinkedHashMap;
import java.util.Map;

public class TorchLongPressPowerTimeoutPreferenceController extends AbstractPreferenceController
        implements SelectorWithWidgetPreference.OnClickListener, LifecycleObserver,
        OnResume, OnPause, PreferenceControllerMixin {

    private static final String KEY = "torch_long_press_power_timeout_category";
    private static final String PREF_KEY_PREFIX = "torch_timeout_";

    private final Context mContext;
    private final Map<Integer, SelectorWithWidgetPreference> mPreferences = new LinkedHashMap<>();

    private PreferenceCategory mPreferenceCategory;
    private SettingObserver mSettingObserver;

    public TorchLongPressPowerTimeoutPreferenceController(Context context, Lifecycle lifecycle) {
        super(context);
        mContext = context;

        if (lifecycle != null) {
            lifecycle.addObserver(this);
        }
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        if (!isAvailable()) {
            return;
        }
        mPreferenceCategory = screen.findPreference(getPreferenceKey());
        if (mPreferenceCategory == null) {
            return;
        }

        mPreferenceCategory.removeAll();
        mPreferences.clear();

        final String[] entries = mContext.getResources().getStringArray(
                R.array.torch_long_press_power_timeout_entries);
        final String[] values = mContext.getResources().getStringArray(
                R.array.torch_long_press_power_timeout_values);

        for (int i = 0; i < values.length; i++) {
            final int value = Integer.parseInt(values[i]);
            final String entry = entries[i];
            SelectorWithWidgetPreference pref = new SelectorWithWidgetPreference(
                    mPreferenceCategory.getContext());
            pref.setKey(PREF_KEY_PREFIX + value);
            pref.setTitle(entry);
            pref.setOnClickListener(this);
            mPreferenceCategory.addPreference(pref);
            mPreferences.put(value, pref);
        }

        mSettingObserver = new SettingObserver(mPreferenceCategory);
    }

    @Override
    public boolean isAvailable() {
        return TorchLongPressPowerParentPreferenceController
                .isTorchLongPressPowerGestureAvailable(mContext);
    }

    @Override
    public String getPreferenceKey() {
        return KEY;
    }

    @Override
    public void onRadioButtonClicked(SelectorWithWidgetPreference preference) {
        for (Map.Entry<Integer, SelectorWithWidgetPreference> entry : mPreferences.entrySet()) {
            if (entry.getValue() == preference) {
                LineageSettings.System.putInt(mContext.getContentResolver(),
                        LineageSettings.System.TORCH_LONG_PRESS_POWER_TIMEOUT, entry.getKey());
                break;
            }
        }
    }

    @Override
    public void updateState(Preference preference) {
        final boolean isTorchGestureEnabled = LineageSettings.System.getInt(
                mContext.getContentResolver(),
                LineageSettings.System.TORCH_LONG_PRESS_POWER_GESTURE, 0) != 0;
        final int currentTimeout = LineageSettings.System.getInt(
                mContext.getContentResolver(),
                LineageSettings.System.TORCH_LONG_PRESS_POWER_TIMEOUT, 0);

        for (Map.Entry<Integer, SelectorWithWidgetPreference> entry : mPreferences.entrySet()) {
            final boolean isChecked = entry.getKey() == currentTimeout;
            if (entry.getValue().isChecked() != isChecked) {
                entry.getValue().setChecked(isChecked);
            }
            entry.getValue().setEnabled(isTorchGestureEnabled);
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
        private final Uri TORCH_GESTURE_URI = LineageSettings.System.getUriFor(
                LineageSettings.System.TORCH_LONG_PRESS_POWER_GESTURE);
        private final Uri TORCH_TIMEOUT_URI = LineageSettings.System.getUriFor(
                LineageSettings.System.TORCH_LONG_PRESS_POWER_TIMEOUT);

        private final Preference mPreference;

        public SettingObserver(Preference preference) {
            super(new Handler(Looper.getMainLooper()));
            mPreference = preference;
        }

        public void register(ContentResolver cr) {
            cr.registerContentObserver(TORCH_GESTURE_URI, false, this);
            cr.registerContentObserver(TORCH_TIMEOUT_URI, false, this);
        }

        public void unregister(ContentResolver cr) {
            cr.unregisterContentObserver(this);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            if (uri == null || TORCH_GESTURE_URI.equals(uri) || TORCH_TIMEOUT_URI.equals(uri)) {
                updateState(mPreference);
            }
        }
    }
}
