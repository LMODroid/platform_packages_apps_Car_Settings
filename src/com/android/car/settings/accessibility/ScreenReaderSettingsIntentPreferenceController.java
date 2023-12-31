/*
 * Copyright (C) 2022 The Android Open Source Project
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

package com.android.car.settings.accessibility;

import android.car.drivingstate.CarUxRestrictions;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import androidx.preference.Preference;

import com.android.car.settings.common.FragmentController;
import com.android.car.settings.common.PreferenceController;

/**
 * Preference controller that intents out to the settings activity for the system default screen
 * reader if it exists.
 */
public class ScreenReaderSettingsIntentPreferenceController extends
        PreferenceController<Preference> {

    public ScreenReaderSettingsIntentPreferenceController(Context context,
            String preferenceKey,
            FragmentController fragmentController,
            CarUxRestrictions uxRestrictions) {
        super(context, preferenceKey, fragmentController, uxRestrictions);
    }

    @Override
    protected Class<Preference> getPreferenceType() {
        return Preference.class;
    }

    @Override
    protected boolean handlePreferenceClicked(Preference preference) {
        ComponentName screenReaderSettingsActivity =
                ScreenReaderUtils.getScreenReaderSettingsActivity(getContext());
        if (screenReaderSettingsActivity == null) {
            return true;
        }

        Intent intent = new Intent(Intent.ACTION_MAIN).setComponent(
                screenReaderSettingsActivity);
        getContext().startActivity(intent);
        return true;
    }

    @Override
    protected int getDefaultAvailabilityStatus() {
        ComponentName screenReaderSettingsActivity =
                ScreenReaderUtils.getScreenReaderSettingsActivity(getContext());
        return screenReaderSettingsActivity != null ? AVAILABLE : CONDITIONALLY_UNAVAILABLE;
    }
}
