/*
 * Copyright (C) 2018 The Android Open Source Project
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

package com.android.car.settings.common;

import android.car.drivingstate.CarUxRestrictions;
import android.content.Context;
import android.content.Intent;

import androidx.preference.Preference;
import androidx.preference.PreferenceGroup;

import java.util.List;

/**
 * Injects preferences from other system applications at a placeholder location. The placeholder
 * should be a {@link PreferenceGroup} which sets the controller attribute to the fully qualified
 * name of this class. The preference should contain an intent which will be passed to
 * {@link ExtraSettingsLoader#loadPreferences(Intent)}.
 *
 * <p>For example:
 * <pre>{@code
 * <PreferenceCategory
 *     android:key="@string/pk_system_extra_settings"
 *     android:title="@string/system_extra_settings_title"
 *     settings:controller="com.android.settings.common.ExtraSettingsPreferenceController">
 *     <intent android:action="com.android.settings.action.EXTRA_SETTINGS">
 *         <extra android:name="com.android.settings.category"
 *                android:value="com.android.settings.category.system"/>
 *     </intent>
 * </PreferenceCategory>
 * }</pre>
 *
 * @see ExtraSettingsLoader
 */
// TODO: investigate using SettingsLib Tiles.
public class ExtraSettingsPreferenceController extends PreferenceController<PreferenceGroup> {

    private final ExtraSettingsLoader mExtraSettingsLoader;
    private boolean mSettingsLoaded;

    public ExtraSettingsPreferenceController(Context context, String preferenceKey,
            FragmentController fragmentController, CarUxRestrictions restrictionInfo) {
        super(context, preferenceKey, fragmentController, restrictionInfo);
        mExtraSettingsLoader = new ExtraSettingsLoader(context);
    }

    @Override
    protected Class<PreferenceGroup> getPreferenceType() {
        return PreferenceGroup.class;
    }

    @Override
    protected void updateState(PreferenceGroup preference) {
        if (!mSettingsLoaded) {
            List<Preference> extraSettings = mExtraSettingsLoader.loadPreferences(
                    preference.getIntent());
            for (Preference setting : extraSettings) {
                preference.addPreference(setting);
            }
            mSettingsLoaded = true;
        }
        preference.setVisible(preference.getPreferenceCount() > 0);
    }
}
