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
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;

import androidx.annotation.NonNull;

import com.android.settings.core.BasePreferenceController;

/**
 * Availability-only controller for the remote TorchLongPressPower LineageParts preference.
 */
public class TorchLongPressPowerPreferenceController extends BasePreferenceController {

    public TorchLongPressPowerPreferenceController(@NonNull Context context,
            @NonNull String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        return isTorchLongPressPowerGestureAvailable(mContext)
                ? AVAILABLE : UNSUPPORTED_ON_DEVICE;
    }

    private static boolean isTorchLongPressPowerGestureAvailable(@NonNull Context context) {
        if (!KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_POWER)) {
            return false;
        }
        if (!context.getResources().getBoolean(
                com.android.internal.R.bool.config_supportLongPressPowerWhenNonInteractive)) {
            return false;
        }
        return deviceSupportsFlashLight(context);
    }

    private static boolean deviceSupportsFlashLight(@NonNull Context context) {
        CameraManager cameraManager = context.getSystemService(CameraManager.class);
        if (cameraManager == null) {
            return false;
        }
        try {
            for (String id : cameraManager.getCameraIdList()) {
                CameraCharacteristics c = cameraManager.getCameraCharacteristics(id);
                Boolean flashAvailable = c.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                Integer lensFacing = c.get(CameraCharacteristics.LENS_FACING);
                if (Boolean.TRUE.equals(flashAvailable)
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
}
