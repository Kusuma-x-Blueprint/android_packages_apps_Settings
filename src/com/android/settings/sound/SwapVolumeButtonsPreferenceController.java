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

import com.android.settings.R;
import com.android.settings.core.TogglePreferenceController;

import lineageos.providers.LineageSettings;
import org.lineageos.internal.util.DeviceKeysConstants;

public class SwapVolumeButtonsPreferenceController extends TogglePreferenceController {

    public SwapVolumeButtonsPreferenceController(Context context, String key) {
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
    public boolean isChecked() {
        return LineageSettings.System.getInt(mContext.getContentResolver(),
                LineageSettings.System.SWAP_VOLUME_KEYS_ON_ROTATION, 0) != 0;
    }

    @Override
    public boolean setChecked(boolean isChecked) {
        int value = isChecked ? mContext.getResources().getInteger(
                R.integer.config_volumeRockerVsDisplayOrientation) : 0;
        return LineageSettings.System.putInt(mContext.getContentResolver(),
                LineageSettings.System.SWAP_VOLUME_KEYS_ON_ROTATION, value);
    }

    @Override
    public int getSliceHighlightMenuRes() {
        return R.string.menu_key_sound;
    }
}
