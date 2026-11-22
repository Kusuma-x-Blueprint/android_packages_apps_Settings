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
import androidx.preference.PreferenceScreen;

import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnStart;
import com.android.settingslib.core.lifecycle.events.OnStop;

import lineageos.providers.LineageSettings;
import org.lineageos.internal.util.DeviceKeysConstants;

public class VolumeButtonMusicControlsPreferenceController extends BasePreferenceController
        implements LifecycleObserver, OnStart, OnStop {

    private Preference mPreference;
    private SettingObserver mSettingObserver;

    public VolumeButtonMusicControlsPreferenceController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    public static boolean isVolumeButtonMusicControlsAvailable(Context context) {
        final int deviceKeys = context.getResources().getInteger(
                org.lineageos.platform.internal.R.integer.config_deviceHardwareKeys);
        return (deviceKeys & DeviceKeysConstants.KEY_MASK_VOLUME) != 0;
    }

    @Override
    public int getAvailabilityStatus() {
        return isVolumeButtonMusicControlsAvailable(mContext)
                ? AVAILABLE : UNSUPPORTED_ON_DEVICE;
    }

    @Override
    public CharSequence getSummary() {
        final boolean isEnabled = LineageSettings.System.getInt(mContext.getContentResolver(),
                LineageSettings.System.VOLBTN_MUSIC_CONTROLS, 1) != 0;
        return mContext.getText(isEnabled ? R.string.gesture_setting_on : R.string.gesture_setting_off);
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        mPreference = screen.findPreference(getPreferenceKey());
        mSettingObserver = new SettingObserver(mPreference);
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);
        refreshSummary(preference);
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

    private class SettingObserver extends ContentObserver {
        private final Uri MUSIC_CONTROLS_URI = LineageSettings.System.getUriFor(
                LineageSettings.System.VOLBTN_MUSIC_CONTROLS);
        private final Preference mPref;

        public SettingObserver(Preference preference) {
            super(new Handler(Looper.getMainLooper()));
            mPref = preference;
        }

        public void register(ContentResolver cr) {
            cr.registerContentObserver(MUSIC_CONTROLS_URI, false, this);
        }

        public void unregister(ContentResolver cr) {
            cr.unregisterContentObserver(this);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            if (uri == null || MUSIC_CONTROLS_URI.equals(uri)) {
                if (mPref != null) {
                    refreshSummary(mPref);
                }
            }
        }
    }
}
