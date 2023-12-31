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

package com.android.car.settings.system.hardwareinfo;

import android.car.drivingstate.CarUxRestrictions;
import android.content.Context;
import android.os.SystemProperties;

import androidx.preference.Preference;

import com.android.car.settings.common.FragmentController;
import com.android.car.settings.common.PreferenceController;

/** Updates hardware revision entry summary with the hardware revision. */
public class HardwareRevisionPreferenceController extends PreferenceController<Preference> {

    public HardwareRevisionPreferenceController(Context context,
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
    protected void updateState(Preference preference) {
        preference.setSummary(SystemProperties.get("ro.boot.hardware.revision"));
    }

    @Override
    protected int getDefaultAvailabilityStatus() {
        return AVAILABLE_FOR_VIEWING;
    }
}
