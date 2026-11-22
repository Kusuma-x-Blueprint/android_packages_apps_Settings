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

package com.android.settings.sound;

import android.content.Context;

import androidx.preference.ListPreference;
import androidx.preference.Preference;

import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;

import lineageos.providers.LineageSettings;
import org.lineageos.internal.util.DeviceKeysConstants;

public class VolumePanelPositionPreferenceController extends BasePreferenceController
        implements Preference.OnPreferenceChangeListener {

    public VolumePanelPositionPreferenceController(Context context, String key) {
        super(context, key);
    }

    @Override
    public int getAvailabilityStatus() {
        final int deviceKeys = mContext.getResources().getInteger(
                org.lineageos.platform.internal.R.integer.config_deviceHardwareKeys);
        if ((deviceKeys & DeviceKeysConstants.KEY_MASK_VOLUME) == 0) {
            return UNSUPPORTED_ON_DEVICE;
        }
        return AVAILABLE;
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);
        if (preference instanceof ListPreference) {
            ListPreference listPref = (ListPreference) preference;
            int value = LineageSettings.Secure.getInt(mContext.getContentResolver(),
                    LineageSettings.Secure.VOLUME_PANEL_ON_LEFT, 0);
            listPref.setValue(String.valueOf(value));
            refreshSummary(preference);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        int value = Integer.parseInt((String) newValue);
        LineageSettings.Secure.putInt(mContext.getContentResolver(),
                LineageSettings.Secure.VOLUME_PANEL_ON_LEFT, value);
        refreshSummary(preference);
        return true;
    }

    @Override
    public CharSequence getSummary() {
        int value = LineageSettings.Secure.getInt(mContext.getContentResolver(),
                LineageSettings.Secure.VOLUME_PANEL_ON_LEFT, 0);
        String[] values = mContext.getResources().getStringArray(
                R.array.volume_panel_position_values);
        String[] entries = mContext.getResources().getStringArray(
                R.array.volume_panel_position_entries);
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
