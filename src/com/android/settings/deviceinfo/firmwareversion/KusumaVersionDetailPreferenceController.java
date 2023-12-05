/*
 * Copyright (C) 2023 Project Lineage Remix Open Source
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

package com.android.settings.deviceinfo.firmwareversion;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;
import androidx.preference.Preference;

import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.core.BasePreferenceController;

public class KusumaVersionDetailPreferenceController extends BasePreferenceController {

    private static final String TAG = "KusumaVersionDetailPreferenceController";
    private static final String KUSUMA_EXTRA_VERSION_CODENAME = "ro.kusuma.extra.version.codename";
    private static final String KUSUMA_EXTRA_VERSION_NUMBER = "ro.kusuma.extra.version.number";
    private static final String KUSUMA_EXTRA_VERSION_URL = "ro.kusuma.extra.version.url";
    private static final Uri INTENT_KUSUMA_EXTRA_VERSION = Uri.parse(SystemProperties.get(KUSUMA_EXTRA_VERSION_URL));

    public KusumaVersionDetailPreferenceController(Context context, String key) {
        super(context, key);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public CharSequence getSummary() {
        String kusumaExtraVersionCodename = SystemProperties.get(KUSUMA_EXTRA_VERSION_CODENAME);
        String kusumaExtraVersionNumber = SystemProperties.get(KUSUMA_EXTRA_VERSION_NUMBER);
        return kusumaExtraVersionCodename + " (" +  kusumaExtraVersionNumber + ")";
    }

    @Override
    public boolean handlePreferenceTreeClick(Preference preference) {
        if (!TextUtils.equals(preference.getKey(), getPreferenceKey())) {
            return false;
        }
        if (Utils.isMonkeyRunning()) {
            return false;
        }
        PackageManager mPackageManager = mContext.getPackageManager();
        final Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(INTENT_KUSUMA_EXTRA_VERSION);
        if (mPackageManager.queryIntentActivities(intent, 0).isEmpty()) {
            // Don't send out the intent to stop crash
            Log.w(TAG, "queryIntentActivities() returns empty");
            return true;
        }
        mContext.startActivity(intent);
        return true;
    }
}
