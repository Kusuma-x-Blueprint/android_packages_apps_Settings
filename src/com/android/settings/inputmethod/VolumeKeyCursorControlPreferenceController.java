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

package com.android.settings.inputmethod;

import android.content.Context;
import android.provider.Settings;

import androidx.preference.ListPreference;
import androidx.preference.Preference;

import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;

import org.lineageos.internal.util.DeviceKeysConstants;

public class VolumeKeyCursorControlPreferenceController extends BasePreferenceController
        implements Preference.OnPreferenceChangeListener {

    public VolumeKeyCursorControlPreferenceController(Context context, String key) {
        super(context, key);
    }

    public static boolean isVolumeKeyCursorControlAvailable(Context context) {
        final int deviceKeys = context.getResources().getInteger(
                org.lineageos.platform.internal.R.integer.config_deviceHardwareKeys);
        return (deviceKeys & DeviceKeysConstants.KEY_MASK_VOLUME) != 0;
    }

    @Override
    public int getAvailabilityStatus() {
        return isVolumeKeyCursorControlAvailable(mContext)
                ? AVAILABLE : UNSUPPORTED_ON_DEVICE;
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);
        if (preference instanceof ListPreference) {
            ListPreference listPref = (ListPreference) preference;
            int value = Settings.System.getInt(mContext.getContentResolver(),
                    Settings.System.VOLUME_KEY_CURSOR_CONTROL, 0);
            listPref.setValue(String.valueOf(value));
            refreshSummary(preference);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        int value = Integer.parseInt((String) newValue);
        Settings.System.putInt(mContext.getContentResolver(),
                Settings.System.VOLUME_KEY_CURSOR_CONTROL, value);
        refreshSummary(preference);
        return true;
    }

    @Override
    public CharSequence getSummary() {
        int value = Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.VOLUME_KEY_CURSOR_CONTROL, 0);
        String[] values = mContext.getResources().getStringArray(
                R.array.volbtn_cursor_control_values);
        String[] entries = mContext.getResources().getStringArray(
                R.array.volbtn_cursor_control_entries);
        if (values == null || entries == null) {
            return null;
        }
        for (int i = 0; i < values.length; i++) {
            if (Integer.parseInt(values[i]) == value) {
                return entries[i];
            }
        }
        return null;
    }
}
