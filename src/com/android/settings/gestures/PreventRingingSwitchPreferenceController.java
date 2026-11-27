/*
 * Copyright (C) 2019 The Android Open Source Project
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
import android.provider.Settings;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import androidx.annotation.VisibleForTesting;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.android.settings.R;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnPause;
import com.android.settingslib.core.lifecycle.events.OnResume;
import com.android.settingslib.widget.MainSwitchPreference;

public class PreventRingingSwitchPreferenceController extends AbstractPreferenceController
        implements PreferenceControllerMixin, OnCheckedChangeListener,
        LifecycleObserver, OnResume, OnPause {

    private static final String KEY = "gesture_prevent_ringing_switch";
    private final Context mContext;

    @VisibleForTesting
    MainSwitchPreference mSwitch;
    private SettingObserver mSettingObserver;

    public PreventRingingSwitchPreferenceController(Context context) {
        this(context, null);
    }

    public PreventRingingSwitchPreferenceController(Context context, Lifecycle lifecycle) {
        super(context);
        mContext = context;
        if (lifecycle != null) {
            lifecycle.addObserver(this);
        }
    }

    @Override
    public String getPreferenceKey() {
        return KEY;
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        if (isAvailable()) {
            Preference pref = screen.findPreference(getPreferenceKey());
            if (pref != null) {
                pref.setOnPreferenceClickListener(preference -> {
                    boolean isChecked = PreventRingingGestureSettings.getPreventRingingMode(mContext)
                            != Settings.Secure.VOLUME_HUSH_OFF;
                    PreventRingingGestureSettings.setPreventRingingEnabled(mContext, !isChecked);
                    return true;
                });
                mSwitch = (MainSwitchPreference) pref;
                mSwitch.setTitle(mContext.getString(R.string.prevent_ringing_main_switch_title));
                mSwitch.addOnSwitchChangeListener(this);
                mSettingObserver = new SettingObserver(mSwitch);
                updateState(mSwitch);
            }
        }
    }

    public void setChecked(boolean isChecked) {
        if (mSwitch != null) {
            mSwitch.setChecked(isChecked);
        }
    }

    @Override
    public void updateState(Preference preference) {
        int preventRingingSetting = PreventRingingGestureSettings.getPreventRingingMode(mContext);
        setChecked(preventRingingSetting != Settings.Secure.VOLUME_HUSH_OFF);
    }

    @Override
    public boolean isAvailable() {
        return mContext.getResources().getBoolean(
                com.android.internal.R.bool.config_volumeHushGestureEnabled);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        PreventRingingGestureSettings.setPreventRingingEnabled(mContext, isChecked);
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
        private final Uri VOLUME_HUSH_URI = Settings.Secure.getUriFor(
                Settings.Secure.VOLUME_HUSH_GESTURE);
        private final Preference mPreference;

        SettingObserver(Preference preference) {
            super(new Handler(Looper.getMainLooper()));
            mPreference = preference;
        }

        void register(ContentResolver cr) {
            cr.registerContentObserver(VOLUME_HUSH_URI, false, this);
        }

        void unregister(ContentResolver cr) {
            cr.unregisterContentObserver(this);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            if (uri == null || VOLUME_HUSH_URI.equals(uri)) {
                updateState(mPreference);
            }
        }
    }
}
