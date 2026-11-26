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
import androidx.preference.PreferenceScreen;

import com.android.settings.R;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settings.core.TogglePreferenceController;
import com.android.settingslib.PrimarySwitchPreference;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnStart;
import com.android.settingslib.core.lifecycle.events.OnStop;

import lineageos.providers.LineageSettings;

public class QsBrightnessSliderParentPreferenceController extends TogglePreferenceController
        implements PreferenceControllerMixin, LifecycleObserver, OnStart, OnStop {

    private PrimarySwitchPreference mPreference;
    private SettingObserver mSettingObserver;

    public QsBrightnessSliderParentPreferenceController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        mPreference = screen.findPreference(getPreferenceKey());
        mSettingObserver = new SettingObserver(mPreference);
    }

    @Override
    public boolean isChecked() {
        return QsBrightnessSliderSettings.getShowMode(mContext)
                != QsBrightnessSliderSettings.SLIDER_OFF;
    }

    @Override
    public boolean setChecked(boolean isChecked) {
        return QsBrightnessSliderSettings.setSliderEnabled(mContext, isChecked);
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);
        final int mode = QsBrightnessSliderSettings.getShowMode(mContext);
        if (mPreference != null) {
            mPreference.setChecked(mode != QsBrightnessSliderSettings.SLIDER_OFF);
        }

        final CharSequence summary;
        if (mode == QsBrightnessSliderSettings.SLIDER_OFF) {
            summary = mContext.getText(R.string.switch_off_text);
        } else if (mode == QsBrightnessSliderSettings.SLIDER_ALWAYS) {
            summary = mContext.getText(R.string.qs_brightness_slider_show_always);
        } else {
            summary = mContext.getText(R.string.qs_brightness_slider_show_expanded);
        }
        preference.setSummary(summary);
    }

    @Override
    public void onStart() {
        if (mSettingObserver != null) {
            mSettingObserver.register(mContext.getContentResolver());
            mSettingObserver.onChange(false, null);
        }
    }

    @Override
    public void onStop() {
        if (mSettingObserver != null) {
            mSettingObserver.unregister(mContext.getContentResolver());
        }
    }

    @Override
    public int getSliceHighlightMenuRes() {
        return R.string.menu_key_display;
    }

    private class SettingObserver extends ContentObserver {
        private final Uri SHOW_SLIDER_URI = LineageSettings.Secure.getUriFor(
                LineageSettings.Secure.QS_SHOW_BRIGHTNESS_SLIDER);
        private final Preference mPref;

        SettingObserver(Preference preference) {
            super(new Handler(Looper.getMainLooper()));
            mPref = preference;
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
                if (mPref != null) {
                    updateState(mPref);
                }
            }
        }
    }
}
