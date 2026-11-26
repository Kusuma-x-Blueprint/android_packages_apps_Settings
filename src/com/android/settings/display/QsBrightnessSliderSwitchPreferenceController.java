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
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnPause;
import com.android.settingslib.core.lifecycle.events.OnResume;
import com.android.settingslib.widget.MainSwitchPreference;

import lineageos.providers.LineageSettings;

public class QsBrightnessSliderSwitchPreferenceController extends AbstractPreferenceController
        implements PreferenceControllerMixin, OnCheckedChangeListener,
        LifecycleObserver, OnResume, OnPause {

    private static final String KEY = "qs_brightness_slider_switch";

    private MainSwitchPreference mSwitch;
    private SettingObserver mSettingObserver;

    public QsBrightnessSliderSwitchPreferenceController(Context context, Lifecycle lifecycle) {
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
        Preference pref = screen.findPreference(getPreferenceKey());
        if (pref instanceof MainSwitchPreference) {
            mSwitch = (MainSwitchPreference) pref;
            mSwitch.addOnSwitchChangeListener(this);
            mSettingObserver = new SettingObserver(mSwitch);
            updateState(mSwitch);
        }
    }

    @Override
    public void updateState(Preference preference) {
        if (mSwitch != null) {
            final boolean isChecked = QsBrightnessSliderSettings.getShowMode(mContext)
                    != QsBrightnessSliderSettings.SLIDER_OFF;
            if (mSwitch.isChecked() != isChecked) {
                mSwitch.setChecked(isChecked);
            }
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        final boolean currentlyChecked = QsBrightnessSliderSettings.getShowMode(mContext)
                != QsBrightnessSliderSettings.SLIDER_OFF;
        if (isChecked != currentlyChecked) {
            QsBrightnessSliderSettings.setSliderEnabled(mContext, isChecked);
        }
        if (mSwitch != null && mSwitch.isChecked() != isChecked) {
            mSwitch.setChecked(isChecked);
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
        private final Preference mPreference;

        SettingObserver(Preference preference) {
            super(new Handler(Looper.getMainLooper()));
            mPreference = preference;
        }

        void register(ContentResolver cr) {
            cr.registerContentObserver(SHOW_SLIDER_URI, false, this);
        }

        void unregister(ContentResolver cr) {
            cr.unregisterContentObserver(this);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            if (uri == null || SHOW_SLIDER_URI.equals(uri)) {
                updateState(mPreference);
            }
        }
    }
}
