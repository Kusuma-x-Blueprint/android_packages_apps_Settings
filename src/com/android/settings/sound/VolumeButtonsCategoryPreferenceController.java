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

import com.android.settings.core.BasePreferenceController;

import org.lineageos.internal.util.DeviceKeysConstants;

public class VolumeButtonsCategoryPreferenceController extends BasePreferenceController {

    public VolumeButtonsCategoryPreferenceController(Context context, String key) {
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
}
