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
import android.graphics.Rect;
import android.view.WindowManager;

import com.android.settings.R;
import com.android.settings.core.TogglePreferenceController;

import lineageos.providers.LineageSettings;

public class TaskbarPreferenceController extends TogglePreferenceController {

    private static final int MIN_LARGESCREEN_WIDTH_DP = 600;

    public TaskbarPreferenceController(Context context, String key) {
        super(context, key);
    }

    public static boolean isLargeScreen(Context context) {
        final WindowManager wm = context.getSystemService(WindowManager.class);
        if (wm != null) {
            final Rect bounds = wm.getMaximumWindowMetrics().getBounds();
            final float density = context.getResources().getDisplayMetrics().density;
            final float smallestWidth = Math.min(bounds.width(), bounds.height()) / density;
            return smallestWidth >= MIN_LARGESCREEN_WIDTH_DP;
        }
        return context.getResources().getConfiguration().smallestScreenWidthDp
                >= MIN_LARGESCREEN_WIDTH_DP;
    }

    @Override
    public boolean isChecked() {
        return LineageSettings.System.getInt(mContext.getContentResolver(),
                LineageSettings.System.ENABLE_TASKBAR,
                isLargeScreen(mContext) ? 1 : 0) == 1;
    }

    @Override
    public boolean setChecked(boolean isChecked) {
        return LineageSettings.System.putInt(mContext.getContentResolver(),
                LineageSettings.System.ENABLE_TASKBAR, isChecked ? 1 : 0);
    }

    @Override
    public int getAvailabilityStatus() {
        return isLargeScreen(mContext) ? AVAILABLE : UNSUPPORTED_ON_DEVICE;
    }

    @Override
    public int getSliceHighlightMenuRes() {
        return R.string.menu_key_system;
    }
}
