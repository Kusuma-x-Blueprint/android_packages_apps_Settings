/*
 * Copyright (C) 2021 The Android Open Source Project
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

import android.app.settings.SettingsEnums;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.ListPreference;
import androidx.preference.Preference;

import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;

import lineageos.providers.LineageSettings;
import org.lineageos.internal.util.DeviceKeysConstants.Action;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A fragment that includes settings for 2- and 3-button navigation modes.
 */
@SearchIndexable(forTarget = SearchIndexable.MOBILE)
public class ButtonNavigationSettingsFragment extends DashboardFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String TAG = "ButtonNavigationSettingsFragment";

    public static final String BUTTON_NAVIGATION_SETTINGS =
            "com.android.settings.BUTTON_NAVIGATION_SETTINGS";

    private static final String KEY_NAVIGATION_BACK_LONG_PRESS = "navigation_back_long_press";
    private static final String KEY_NAVIGATION_HOME_LONG_PRESS = "navigation_home_long_press";
    private static final String KEY_NAVIGATION_HOME_DOUBLE_TAP = "navigation_home_double_tap";
    private static final String KEY_NAVIGATION_APP_SWITCH_LONG_PRESS =
            "navigation_app_switch_long_press";

    private ListPreference mNavigationBackLongPressAction;
    private ListPreference mNavigationHomeLongPressAction;
    private ListPreference mNavigationHomeDoubleTapAction;
    private ListPreference mNavigationAppSwitchLongPressAction;

    @Override
    public int getMetricsCategory() {
        return SettingsEnums.SETTINGS_BUTTON_NAV_DLG;
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.button_navigation_settings;
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Resources res = getResources();
        final ContentResolver resolver = requireActivity().getContentResolver();

        List<Integer> unsupportedValues = new ArrayList<>();
        List<String> entries = new ArrayList<>(
                Arrays.asList(res.getStringArray(R.array.hardware_keys_action_entries)));
        List<String> values = new ArrayList<>(
                Arrays.asList(res.getStringArray(R.array.hardware_keys_action_values)));

        unsupportedValues.add(Action.SPLIT_SCREEN.ordinal());

        for (int unsupportedValue : unsupportedValues) {
            entries.remove(unsupportedValue);
            values.remove(unsupportedValue);
        }

        String[] actionEntries = entries.toArray(new String[0]);
        String[] actionValues = values.toArray(new String[0]);

        Action defaultBackLongPressAction = Action.fromIntSafe(res.getInteger(
                org.lineageos.platform.internal.R.integer.config_longPressOnBackBehavior));
        Action defaultHomeLongPressAction = Action.fromIntSafe(res.getInteger(
                org.lineageos.platform.internal.R.integer.config_longPressOnHomeBehavior));
        Action defaultHomeDoubleTapAction = Action.fromIntSafe(res.getInteger(
                org.lineageos.platform.internal.R.integer.config_doubleTapOnHomeBehavior));
        Action defaultAppSwitchLongPressAction = Action.fromIntSafe(res.getInteger(
                org.lineageos.platform.internal.R.integer.config_longPressOnAppSwitchBehavior));

        Action backLongPressAction = Action.fromSettings(resolver,
                LineageSettings.System.KEY_BACK_LONG_PRESS_ACTION,
                defaultBackLongPressAction);
        Action homeLongPressAction = Action.fromSettings(resolver,
                LineageSettings.System.KEY_HOME_LONG_PRESS_ACTION,
                defaultHomeLongPressAction);
        Action homeDoubleTapAction = Action.fromSettings(resolver,
                LineageSettings.System.KEY_HOME_DOUBLE_TAP_ACTION,
                defaultHomeDoubleTapAction);
        Action appSwitchLongPressAction = Action.fromSettings(resolver,
                LineageSettings.System.KEY_APP_SWITCH_LONG_PRESS_ACTION,
                defaultAppSwitchLongPressAction);

        mNavigationBackLongPressAction = initList(KEY_NAVIGATION_BACK_LONG_PRESS,
                backLongPressAction, actionEntries, actionValues);
        mNavigationHomeLongPressAction = initList(KEY_NAVIGATION_HOME_LONG_PRESS,
                homeLongPressAction, actionEntries, actionValues);
        mNavigationHomeDoubleTapAction = initList(KEY_NAVIGATION_HOME_DOUBLE_TAP,
                homeDoubleTapAction, actionEntries, actionValues);
        mNavigationAppSwitchLongPressAction = initList(KEY_NAVIGATION_APP_SWITCH_LONG_PRESS,
                appSwitchLongPressAction, actionEntries, actionValues);
    }

    private ListPreference initList(String key, Action value, String[] entries, String[] values) {
        return initList(key, value.ordinal(), entries, values);
    }

    private ListPreference initList(String key, int value, String[] entries, String[] values) {
        ListPreference list = findPreference(key);
        if (list == null) return null;
        list.setEntries(entries);
        list.setEntryValues(values);
        list.setValue(Integer.toString(value));
        list.setSummary(list.getEntry());
        list.setOnPreferenceChangeListener(this);
        return list;
    }

    private void handleListChange(ListPreference pref, Object newValue, String setting) {
        String value = (String) newValue;
        int index = pref.findIndexOfValue(value);
        pref.setSummary(pref.getEntries()[index]);
        LineageSettings.System.putInt(requireActivity().getContentResolver(), setting,
                Integer.parseInt(value));
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mNavigationBackLongPressAction) {
            handleListChange(mNavigationBackLongPressAction, newValue,
                    LineageSettings.System.KEY_BACK_LONG_PRESS_ACTION);
            return true;
        } else if (preference == mNavigationHomeLongPressAction) {
            handleListChange(mNavigationHomeLongPressAction, newValue,
                    LineageSettings.System.KEY_HOME_LONG_PRESS_ACTION);
            return true;
        } else if (preference == mNavigationHomeDoubleTapAction) {
            handleListChange(mNavigationHomeDoubleTapAction, newValue,
                    LineageSettings.System.KEY_HOME_DOUBLE_TAP_ACTION);
            return true;
        } else if (preference == mNavigationAppSwitchLongPressAction) {
            handleListChange(mNavigationAppSwitchLongPressAction, newValue,
                    LineageSettings.System.KEY_APP_SWITCH_LONG_PRESS_ACTION);
            return true;
        }
        return false;
    }

    @Override
    @Nullable
    public String getPreferenceScreenBindingKey(@NonNull Context context) {
        return ButtonNavigationSettingsScreen.KEY;
    }

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.button_navigation_settings) {

                @Override
                protected boolean isPageSearchEnabled(Context context) {
                    return SystemNavigationPreferenceController.isOverlayPackageAvailable(context,
                            NAV_BAR_MODE_2BUTTON_OVERLAY)
                            || SystemNavigationPreferenceController.isOverlayPackageAvailable(
                            context,
                            NAV_BAR_MODE_3BUTTON_OVERLAY);
                }
            };
}
