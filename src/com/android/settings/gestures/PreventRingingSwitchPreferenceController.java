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

import android.content.Context;
import android.provider.Settings;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import androidx.annotation.VisibleForTesting;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.android.settings.R;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.widget.MainSwitchPreference;

public class PreventRingingSwitchPreferenceController extends AbstractPreferenceController
        implements PreferenceControllerMixin, OnCheckedChangeListener {

    private static final String KEY = "gesture_prevent_ringing_switch";
    private final Context mContext;

    @VisibleForTesting
    MainSwitchPreference mSwitch;

    public PreventRingingSwitchPreferenceController(Context context) {
        super(context);
        mContext = context;
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
        int preventRingingSetting = Settings.Secure.getInt(mContext.getContentResolver(),
                Settings.Secure.VOLUME_HUSH_GESTURE, Settings.Secure.VOLUME_HUSH_VIBRATE);
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
        if (mSwitch != null && mSwitch.isChecked() != isChecked) {
            mSwitch.setChecked(isChecked);
        }
    }
}
