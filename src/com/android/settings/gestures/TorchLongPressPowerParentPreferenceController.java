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
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;

import androidx.annotation.NonNull;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.android.settings.R;
import com.android.settings.core.TogglePreferenceController;
import com.android.settingslib.PrimarySwitchPreference;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnStart;
import com.android.settingslib.core.lifecycle.events.OnStop;

import lineageos.providers.LineageSettings;

public class TorchLongPressPowerParentPreferenceController extends TogglePreferenceController
        implements LifecycleObserver, OnStart, OnStop {

    private PrimarySwitchPreference mPreference;
    private SettingObserver mSettingObserver;

    public TorchLongPressPowerParentPreferenceController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    public static boolean isTorchLongPressPowerGestureAvailable(Context context) {
        if (!KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_POWER)) {
            return false;
        }
        if (!context.getResources().getBoolean(
                com.android.internal.R.bool.config_supportLongPressPowerWhenNonInteractive)) {
            return false;
        }
        return deviceSupportsFlashLight(context);
    }

    public static boolean deviceSupportsFlashLight(@NonNull Context context) {
        CameraManager cameraManager = context.getSystemService(CameraManager.class);
        if (cameraManager == null) {
            return false;
        }
        try {
            String[] ids = cameraManager.getCameraIdList();
            for (String id : ids) {
                CameraCharacteristics c = cameraManager.getCameraCharacteristics(id);
                Boolean flashAvailable = c.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                Integer lensFacing = c.get(CameraCharacteristics.LENS_FACING);
                if (flashAvailable != null && flashAvailable
                        && lensFacing != null
                        && lensFacing == CameraCharacteristics.LENS_FACING_BACK) {
                    return true;
                }
            }
        } catch (CameraAccessException | AssertionError e) {
            // Ignore
        }
        return false;
    }

    @Override
    public int getAvailabilityStatus() {
        return isTorchLongPressPowerGestureAvailable(mContext)
                ? AVAILABLE : UNSUPPORTED_ON_DEVICE;
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        mPreference = screen.findPreference(getPreferenceKey());
        mSettingObserver = new SettingObserver(mPreference);
    }

    @Override
    public boolean isChecked() {
        return LineageSettings.System.getInt(mContext.getContentResolver(),
                LineageSettings.System.TORCH_LONG_PRESS_POWER_GESTURE, 0) != 0;
    }

    @Override
    public boolean setChecked(boolean isChecked) {
        return LineageSettings.System.putInt(mContext.getContentResolver(),
                LineageSettings.System.TORCH_LONG_PRESS_POWER_GESTURE, isChecked ? 1 : 0);
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);
        final boolean isChecked = isChecked();
        if (mPreference != null) {
            mPreference.setChecked(isChecked);
        }

        CharSequence summary;
        if (isChecked) {
            final int timeout = LineageSettings.System.getInt(mContext.getContentResolver(),
                    LineageSettings.System.TORCH_LONG_PRESS_POWER_TIMEOUT, 0);
            summary = getTimeoutSummary(timeout);
        } else {
            summary = mContext.getText(R.string.switch_off_text);
        }
        preference.setSummary(summary);
    }

    private CharSequence getTimeoutSummary(int timeout) {
        final String[] entries = mContext.getResources().getStringArray(
                R.array.torch_long_press_power_timeout_entries);
        final String[] values = mContext.getResources().getStringArray(
                R.array.torch_long_press_power_timeout_values);
        for (int i = 0; i < values.length; i++) {
            if (Integer.parseInt(values[i]) == timeout) {
                return entries[i];
            }
        }
        return mContext.getText(R.string.torch_long_press_power_timeout_never);
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
        return R.string.menu_key_system;
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
