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

import static android.view.WindowManagerPolicyConstants.NAV_BAR_MODE_2BUTTON_OVERLAY;
import static android.view.WindowManagerPolicyConstants.NAV_BAR_MODE_3BUTTON_OVERLAY;

import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.accessibility.Flags;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;

import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnStart;
import com.android.settingslib.core.lifecycle.events.OnStop;

import java.util.Arrays;
import java.util.List;

public class ButtonNavigationSettingsOrderPreferenceController extends BasePreferenceController
        implements ButtonNavigationSettingsOrderRadioButton.OnClickListener,
        LifecycleObserver, OnStart, OnStop {

    @VisibleForTesting
    static final String KEY_DEFAULT = "navbar_order_preference_default";
    @VisibleForTesting
    static final String KEY_REVERSE = "navbar_order_preference_reverse";

    private PreferenceCategory mPreferenceCategory;
    private ButtonNavigationSettingsOrderRadioButton mDefaultOrderPref;
    private ButtonNavigationSettingsOrderRadioButton mReverseOrderPref;

    private final ContentObserver mContentObserver = new ContentObserver(
            new Handler(Looper.getMainLooper())) {
        @Override
        public void onChange(boolean selfChange, @Nullable Uri uri) {
            updateOrderState();
        }
    };

    public ButtonNavigationSettingsOrderPreferenceController(Context context, String key) {
        super(context, key);
    }

    @Override
    public int getAvailabilityStatus() {
        if (!SystemNavigationPreferenceController.isOverlayPackageAvailable(mContext,
                NAV_BAR_MODE_2BUTTON_OVERLAY)
                && !SystemNavigationPreferenceController.isOverlayPackageAvailable(mContext,
                NAV_BAR_MODE_3BUTTON_OVERLAY)) {
            return UNSUPPORTED_ON_DEVICE;
        }
        return Flags.navbarFlipOrderOption() ? AVAILABLE : CONDITIONALLY_UNAVAILABLE;
    }

    @Override
    public void displayPreference(@NonNull PreferenceScreen screen) {
        super.displayPreference(screen);
        mPreferenceCategory = screen.findPreference(getPreferenceKey());
        if (mPreferenceCategory == null) {
            return;
        }

        if (mDefaultOrderPref == null) {
            List<Integer> defaultIcons = Arrays.asList(
                    R.drawable.ic_sysbar_back,
                    R.drawable.ic_sysbar_home,
                    R.drawable.ic_sysbar_recents);
            List<Integer> defaultLabels = Arrays.asList(
                    R.string.navbar_back_button,
                    R.string.navbar_home_button,
                    R.string.navbar_recent_button);
            mDefaultOrderPref = new ButtonNavigationSettingsOrderRadioButton(
                    mContext, defaultIcons, defaultLabels);
            mDefaultOrderPref.setKey(KEY_DEFAULT);
            mDefaultOrderPref.setOrder(1);
            mDefaultOrderPref.setPersistent(false);
            mDefaultOrderPref.setListener(this);
        }

        if (mReverseOrderPref == null) {
            List<Integer> reverseIcons = Arrays.asList(
                    R.drawable.ic_sysbar_recents,
                    R.drawable.ic_sysbar_home,
                    R.drawable.ic_sysbar_back);
            List<Integer> reverseLabels = Arrays.asList(
                    R.string.navbar_recent_button,
                    R.string.navbar_home_button,
                    R.string.navbar_back_button);
            mReverseOrderPref = new ButtonNavigationSettingsOrderRadioButton(
                    mContext, reverseIcons, reverseLabels);
            mReverseOrderPref.setKey(KEY_REVERSE);
            mReverseOrderPref.setOrder(2);
            mReverseOrderPref.setPersistent(false);
            mReverseOrderPref.setListener(this);
        }

        if (mPreferenceCategory.findPreference(KEY_DEFAULT) == null) {
            mPreferenceCategory.addPreference(mDefaultOrderPref);
        }
        if (mPreferenceCategory.findPreference(KEY_REVERSE) == null) {
            mPreferenceCategory.addPreference(mReverseOrderPref);
        }

        updateOrderState();
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);
        updateOrderState();
    }

    @Override
    public void onStart() {
        mContext.getContentResolver().registerContentObserver(
                Settings.Secure.getUriFor(Settings.Secure.NAVIGATIONBAR_KEY_ORDER),
                false, mContentObserver);
    }

    @Override
    public void onStop() {
        mContext.getContentResolver().unregisterContentObserver(mContentObserver);
    }

    @Override
    public void onRadioButtonClicked(@NonNull ButtonNavigationSettingsOrderRadioButton source) {
        boolean isReverse = (source == mReverseOrderPref);
        Settings.Secure.putInt(mContext.getContentResolver(),
                Settings.Secure.NAVIGATIONBAR_KEY_ORDER, isReverse ? 1 : 0);
        updateOrderState();
    }

    private void updateOrderState() {
        boolean isReverse = Settings.Secure.getInt(mContext.getContentResolver(),
                Settings.Secure.NAVIGATIONBAR_KEY_ORDER, 0) == 1;
        if (mDefaultOrderPref != null) {
            mDefaultOrderPref.setChecked(!isReverse);
        }
        if (mReverseOrderPref != null) {
            mReverseOrderPref.setChecked(isReverse);
        }
    }
}
