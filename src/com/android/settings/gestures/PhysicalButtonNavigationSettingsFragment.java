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

import android.app.settings.SettingsEnums;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreferenceCompat;

import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;

import lineageos.hardware.LineageHardwareManager;
import lineageos.providers.LineageSettings;
import org.lineageos.internal.util.DeviceKeysConstants;
import org.lineageos.internal.util.DeviceKeysConstants.Action;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SearchIndexable(forTarget = SearchIndexable.MOBILE)
public class PhysicalButtonNavigationSettingsFragment extends DashboardFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String TAG = "PhysicalButtonNavigationSettings";

    private static final String KEY_SWAP_CAPACITIVE_KEYS = "swap_capacitive_keys";
    private static final String KEY_BUTTON_BACKLIGHT = "button_backlight";
    private static final String KEY_HOME_LONG_PRESS = "hardware_keys_home_long_press";
    private static final String KEY_HOME_DOUBLE_TAP = "hardware_keys_home_double_tap";
    private static final String KEY_BACK_LONG_PRESS = "hardware_keys_back_long_press";
    private static final String KEY_MENU_PRESS = "hardware_keys_menu_press";
    private static final String KEY_MENU_LONG_PRESS = "hardware_keys_menu_long_press";
    private static final String KEY_ASSIST_PRESS = "hardware_keys_assist_press";
    private static final String KEY_ASSIST_LONG_PRESS = "hardware_keys_assist_long_press";
    private static final String KEY_APP_SWITCH_PRESS = "hardware_keys_app_switch_press";
    private static final String KEY_APP_SWITCH_LONG_PRESS = "hardware_keys_app_switch_long_press";

    private SwitchPreferenceCompat mSwapCapacitiveKeys;
    private ButtonBacklightBrightness mButtonBacklight;
    private ListPreference mHomeLongPressAction;
    private ListPreference mHomeDoubleTapAction;
    private ListPreference mBackLongPressAction;
    private ListPreference mMenuPressAction;
    private ListPreference mMenuLongPressAction;
    private ListPreference mAssistPressAction;
    private ListPreference mAssistLongPressAction;
    private ListPreference mAppSwitchPressAction;
    private ListPreference mAppSwitchLongPressAction;

    private LineageHardwareManager mHardware;

    @Override
    public int getMetricsCategory() {
        return SettingsEnums.SETTINGS_BUTTON_NAV_DLG;
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.physical_button_navigation_settings;
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mHardware = LineageHardwareManager.getInstance(getActivity());
        final Resources res = getResources();
        final ContentResolver resolver = requireActivity().getContentResolver();
        final PreferenceScreen prefScreen = getPreferenceScreen();

        final boolean hasHomeKey = hasHomeKey(getActivity());
        final boolean hasBackKey = hasBackKey(getActivity());
        final boolean hasMenuKey = hasMenuKey(getActivity());
        final boolean hasAssistKey = hasAssistKey(getActivity());
        final boolean hasAppSwitchKey = hasAppSwitchKey(getActivity());

        mSwapCapacitiveKeys = findPreference(KEY_SWAP_CAPACITIVE_KEYS);
        if (mSwapCapacitiveKeys != null) {
            if (!mHardware.isSupported(LineageHardwareManager.FEATURE_KEY_SWAP)) {
                prefScreen.removePreference(mSwapCapacitiveKeys);
                mSwapCapacitiveKeys = null;
            } else {
                mSwapCapacitiveKeys.setOnPreferenceChangeListener(this);
            }
        }

        mButtonBacklight = findPreference(KEY_BUTTON_BACKLIGHT);
        if (mButtonBacklight != null) {
            if (!ButtonBacklightBrightness.hasButtonBacklightSupport(getActivity())
                    && !ButtonBacklightBrightness.hasKeyboardBacklightSupport(getActivity())) {
                prefScreen.removePreference(mButtonBacklight);
                mButtonBacklight = null;
            }
        }

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

        if (hasHomeKey) {
            Action defaultHomeLongPressAction = Action.fromIntSafe(res.getInteger(
                    org.lineageos.platform.internal.R.integer.config_longPressOnHomeBehavior));
            Action defaultHomeDoubleTapAction = Action.fromIntSafe(res.getInteger(
                    org.lineageos.platform.internal.R.integer.config_doubleTapOnHomeBehavior));
            Action homeLongPressAction = Action.fromSettings(resolver,
                    LineageSettings.System.KEY_HOME_LONG_PRESS_ACTION,
                    defaultHomeLongPressAction);
            Action homeDoubleTapAction = Action.fromSettings(resolver,
                    LineageSettings.System.KEY_HOME_DOUBLE_TAP_ACTION,
                    defaultHomeDoubleTapAction);
            mHomeLongPressAction = initList(KEY_HOME_LONG_PRESS, homeLongPressAction, actionEntries, actionValues);
            mHomeDoubleTapAction = initList(KEY_HOME_DOUBLE_TAP, homeDoubleTapAction, actionEntries, actionValues);
        } else {
            removePreference(KEY_HOME_LONG_PRESS);
            removePreference(KEY_HOME_DOUBLE_TAP);
        }

        if (hasBackKey) {
            Action defaultBackLongPressAction = Action.fromIntSafe(res.getInteger(
                    org.lineageos.platform.internal.R.integer.config_longPressOnBackBehavior));
            Action backLongPressAction = Action.fromSettings(resolver,
                    LineageSettings.System.KEY_BACK_LONG_PRESS_ACTION,
                    defaultBackLongPressAction);
            mBackLongPressAction = initList(KEY_BACK_LONG_PRESS, backLongPressAction, actionEntries, actionValues);
        } else {
            removePreference(KEY_BACK_LONG_PRESS);
        }

        if (hasMenuKey) {
            Action pressAction = Action.fromSettings(resolver,
                    LineageSettings.System.KEY_MENU_ACTION, Action.MENU);
            mMenuPressAction = initList(KEY_MENU_PRESS, pressAction, actionEntries, actionValues);

            Action longPressAction = Action.fromSettings(resolver,
                    LineageSettings.System.KEY_MENU_LONG_PRESS_ACTION,
                    hasAssistKey ? Action.NOTHING : Action.APP_SWITCH);
            mMenuLongPressAction = initList(KEY_MENU_LONG_PRESS, longPressAction, actionEntries, actionValues);
        } else {
            removePreference(KEY_MENU_PRESS);
            removePreference(KEY_MENU_LONG_PRESS);
        }

        if (hasAssistKey) {
            Action pressAction = Action.fromSettings(resolver,
                    LineageSettings.System.KEY_ASSIST_ACTION, Action.SEARCH);
            mAssistPressAction = initList(KEY_ASSIST_PRESS, pressAction, actionEntries, actionValues);

            Action longPressAction = Action.fromSettings(resolver,
                    LineageSettings.System.KEY_ASSIST_LONG_PRESS_ACTION, Action.VOICE_SEARCH);
            mAssistLongPressAction = initList(KEY_ASSIST_LONG_PRESS, longPressAction, actionEntries, actionValues);
        } else {
            removePreference(KEY_ASSIST_PRESS);
            removePreference(KEY_ASSIST_LONG_PRESS);
        }

        if (hasAppSwitchKey) {
            Action defaultAppSwitchLongPressAction = Action.fromIntSafe(res.getInteger(
                    org.lineageos.platform.internal.R.integer.config_longPressOnAppSwitchBehavior));
            Action pressAction = Action.fromSettings(resolver,
                    LineageSettings.System.KEY_APP_SWITCH_ACTION, Action.APP_SWITCH);
            Action appSwitchLongPressAction = Action.fromSettings(resolver,
                    LineageSettings.System.KEY_APP_SWITCH_LONG_PRESS_ACTION,
                    defaultAppSwitchLongPressAction);
            mAppSwitchPressAction = initList(KEY_APP_SWITCH_PRESS, pressAction, actionEntries, actionValues);
            mAppSwitchLongPressAction = initList(KEY_APP_SWITCH_LONG_PRESS, appSwitchLongPressAction, actionEntries, actionValues);
        } else {
            removePreference(KEY_APP_SWITCH_PRESS);
            removePreference(KEY_APP_SWITCH_LONG_PRESS);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mSwapCapacitiveKeys != null) {
            mSwapCapacitiveKeys.setChecked(
                    mHardware.get(LineageHardwareManager.FEATURE_KEY_SWAP));
        }
        if (mButtonBacklight != null) {
            mButtonBacklight.updateSummary();
        }
    }

    private ListPreference initList(String key, Action value, String[] entries, String[] values) {
        ListPreference list = findPreference(key);
        if (list == null) return null;
        list.setEntries(entries);
        list.setEntryValues(values);
        list.setValue(Integer.toString(value.ordinal()));
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
        if (preference == mSwapCapacitiveKeys) {
            mHardware.set(LineageHardwareManager.FEATURE_KEY_SWAP, (Boolean) newValue);
            return true;
        } else if (preference == mHomeLongPressAction) {
            handleListChange(mHomeLongPressAction, newValue,
                    LineageSettings.System.KEY_HOME_LONG_PRESS_ACTION);
            return true;
        } else if (preference == mHomeDoubleTapAction) {
            handleListChange(mHomeDoubleTapAction, newValue,
                    LineageSettings.System.KEY_HOME_DOUBLE_TAP_ACTION);
            return true;
        } else if (preference == mBackLongPressAction) {
            handleListChange(mBackLongPressAction, newValue,
                    LineageSettings.System.KEY_BACK_LONG_PRESS_ACTION);
            return true;
        } else if (preference == mMenuPressAction) {
            handleListChange(mMenuPressAction, newValue,
                    LineageSettings.System.KEY_MENU_ACTION);
            return true;
        } else if (preference == mMenuLongPressAction) {
            handleListChange(mMenuLongPressAction, newValue,
                    LineageSettings.System.KEY_MENU_LONG_PRESS_ACTION);
            return true;
        } else if (preference == mAssistPressAction) {
            handleListChange(mAssistPressAction, newValue,
                    LineageSettings.System.KEY_ASSIST_ACTION);
            return true;
        } else if (preference == mAssistLongPressAction) {
            handleListChange(mAssistLongPressAction, newValue,
                    LineageSettings.System.KEY_ASSIST_LONG_PRESS_ACTION);
            return true;
        } else if (preference == mAppSwitchPressAction) {
            handleListChange(mAppSwitchPressAction, newValue,
                    LineageSettings.System.KEY_APP_SWITCH_ACTION);
            return true;
        } else if (preference == mAppSwitchLongPressAction) {
            handleListChange(mAppSwitchLongPressAction, newValue,
                    LineageSettings.System.KEY_APP_SWITCH_LONG_PRESS_ACTION);
            return true;
        }
        return false;
    }

    private static int getDeviceKeys(Context context) {
        return context.getResources().getInteger(
                org.lineageos.platform.internal.R.integer.config_deviceHardwareKeys);
    }

    private static boolean hasHomeKey(Context context) {
        return (getDeviceKeys(context) & DeviceKeysConstants.KEY_MASK_HOME) != 0;
    }

    private static boolean hasBackKey(Context context) {
        return (getDeviceKeys(context) & DeviceKeysConstants.KEY_MASK_BACK) != 0;
    }

    private static boolean hasMenuKey(Context context) {
        return (getDeviceKeys(context) & DeviceKeysConstants.KEY_MASK_MENU) != 0;
    }

    private static boolean hasAssistKey(Context context) {
        return (getDeviceKeys(context) & DeviceKeysConstants.KEY_MASK_ASSIST) != 0;
    }

    private static boolean hasAppSwitchKey(Context context) {
        return (getDeviceKeys(context) & DeviceKeysConstants.KEY_MASK_APP_SWITCH) != 0;
    }

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.physical_button_navigation_settings) {
                @Override
                protected boolean isPageSearchEnabled(Context context) {
                    return SystemNavigationPreferenceController.isKeyDisablerSupported(context);
                }
            };
}
