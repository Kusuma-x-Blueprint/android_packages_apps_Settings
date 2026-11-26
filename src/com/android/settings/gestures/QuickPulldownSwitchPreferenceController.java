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
import android.database.ContentObserver;
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

public class QuickPulldownSwitchPreferenceController extends AbstractPreferenceController
        implements PreferenceControllerMixin, OnCheckedChangeListener,
        LifecycleObserver, OnResume, OnPause {

    private static final String KEY = "qs_quick_pulldown_switch";

    private MainSwitchPreference mSwitch;
    private ContentObserver mSettingObserver;

    public QuickPulldownSwitchPreferenceController(Context context, Lifecycle lifecycle) {
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
        final Preference pref = screen.findPreference(getPreferenceKey());
        if (pref instanceof MainSwitchPreference) {
            mSwitch = (MainSwitchPreference) pref;
            mSwitch.addOnSwitchChangeListener(this);
            updateState(mSwitch);
        }
    }

    @Override
    public void updateState(Preference preference) {
        if (mSwitch != null) {
            final boolean isChecked = QuickPulldownGestureSettings.getPulldownMode(mContext)
                    != QuickPulldownGestureSettings.PULLDOWN_DIR_NONE;
            if (mSwitch.isChecked() != isChecked) {
                mSwitch.setChecked(isChecked);
            }
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        final boolean currentlyChecked = QuickPulldownGestureSettings.getPulldownMode(mContext)
                != QuickPulldownGestureSettings.PULLDOWN_DIR_NONE;
        if (isChecked != currentlyChecked) {
            QuickPulldownGestureSettings.setPulldownEnabled(mContext, isChecked);
        }
        if (mSwitch != null && mSwitch.isChecked() != isChecked) {
            mSwitch.setChecked(isChecked);
        }
    }

    @Override
    public void onResume() {
        if (mSwitch != null && mSettingObserver == null) {
            mSettingObserver = QuickPulldownGestureSettings.observeSetting(
                    mContext, () -> updateState(mSwitch));
            updateState(mSwitch);
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
