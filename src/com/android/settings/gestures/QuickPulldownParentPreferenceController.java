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

import androidx.annotation.NonNull;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnStart;
import com.android.settingslib.core.lifecycle.events.OnStop;

public class QuickPulldownParentPreferenceController extends BasePreferenceController
        implements PreferenceControllerMixin, LifecycleObserver, OnStart, OnStop {

    private Preference mPreference;
    private ContentObserver mSettingObserver;

    public QuickPulldownParentPreferenceController(@NonNull Context context,
            @NonNull String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    @NonNull
    public CharSequence getSummary() {
        final int mode = QuickPulldownGestureSettings.getPulldownMode(mContext);
        final Resources res = mContext.getResources();
        switch (mode) {
            case QuickPulldownGestureSettings.PULLDOWN_DIR_RIGHT:
            case QuickPulldownGestureSettings.PULLDOWN_DIR_LEFT:
                final boolean isRtl = QuickPulldownGestureSettings.isRtlMode(res);
                final int sideRes = (mode == QuickPulldownGestureSettings.PULLDOWN_DIR_LEFT) ^ isRtl
                        ? R.string.qs_quick_pulldown_summary_left
                        : R.string.qs_quick_pulldown_summary_right;
                return res.getString(R.string.qs_quick_pulldown_summary, res.getString(sideRes));
            case QuickPulldownGestureSettings.PULLDOWN_DIR_NONE:
            default:
                return res.getString(R.string.qs_quick_pulldown_off);
        }
    }

    @Override
    public void displayPreference(@NonNull PreferenceScreen screen) {
        super.displayPreference(screen);
        mPreference = screen.findPreference(getPreferenceKey());
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);
        refreshSummary(preference);
    }

    @Override
    public void onStart() {
        if (mPreference != null && mSettingObserver == null) {
            mSettingObserver = QuickPulldownGestureSettings.observeSetting(
                    mContext, () -> updateState(mPreference));
            updateState(mPreference);
        }
    }

    @Override
    public void onStop() {
        if (mSettingObserver != null) {
            mContext.getContentResolver().unregisterContentObserver(mSettingObserver);
            mSettingObserver = null;
        }
    }
}
