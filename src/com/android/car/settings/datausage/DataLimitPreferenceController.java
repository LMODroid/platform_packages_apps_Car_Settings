/*
 * Copyright (C) 2019 The Android Open Source Project
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

package com.android.car.settings.datausage;

import static android.net.NetworkPolicy.LIMIT_DISABLED;

import android.car.drivingstate.CarUxRestrictions;
import android.content.Context;
import android.net.NetworkPolicy;
import android.net.NetworkPolicyManager;
import android.net.NetworkTemplate;
import android.os.Bundle;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;

import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.preference.Preference;
import androidx.preference.PreferenceGroup;
import androidx.preference.TwoStatePreference;

import com.android.car.settings.R;
import com.android.car.settings.common.ConfirmationDialogFragment;
import com.android.car.settings.common.FragmentController;
import com.android.car.settings.common.PreferenceController;
import com.android.settingslib.NetworkPolicyEditor;

/** Controls setting the data limit threshold. */
public class DataLimitPreferenceController extends PreferenceController<PreferenceGroup> implements
        Preference.OnPreferenceChangeListener, ConfirmationDialogFragment.ConfirmListener {

    @VisibleForTesting
    static final float LIMIT_BYTES_MULTIPLIER = 1.2f;
    private static final long GIB_IN_BYTES = 1024 * 1024 * 1024;

    private final NetworkPolicyEditor mPolicyEditor;
    private final TelephonyManager mTelephonyManager;
    private final SubscriptionManager mSubscriptionManager;

    private TwoStatePreference mEnableDataLimitPreference;
    private Preference mSetDataLimitPreference;
    private NetworkTemplate mNetworkTemplate;

    public DataLimitPreferenceController(Context context, String preferenceKey,
            FragmentController fragmentController, CarUxRestrictions uxRestrictions) {
        super(context, preferenceKey, fragmentController, uxRestrictions);
        mPolicyEditor = new NetworkPolicyEditor(NetworkPolicyManager.from(context));
        mTelephonyManager = context.getSystemService(TelephonyManager.class);
        mSubscriptionManager = context.getSystemService(SubscriptionManager.class);
    }

    @Override
    protected Class<PreferenceGroup> getPreferenceType() {
        return PreferenceGroup.class;
    }

    @Override
    protected void onCreateInternal() {
        mEnableDataLimitPreference = (TwoStatePreference) getPreference().findPreference(
                getContext().getString(R.string.pk_data_set_limit));
        mEnableDataLimitPreference.setOnPreferenceChangeListener(this);
        mSetDataLimitPreference = getPreference().findPreference(
                getContext().getString(R.string.pk_data_limit));
        mNetworkTemplate = DataUsageUtils.getMobileNetworkTemplate(mTelephonyManager,
                DataUsageUtils.getDefaultSubscriptionId(mSubscriptionManager));

        ConfirmationDialogFragment.resetListeners(
                (ConfirmationDialogFragment) getFragmentController().findDialogByTag(
                        ConfirmationDialogFragment.TAG),
                /* confirmListener= */ this,
                /* rejectListener= */ null);
    }

    @Override
    protected void updateState(PreferenceGroup preference) {
        long limitBytes = mPolicyEditor.getPolicyLimitBytes(mNetworkTemplate);

        if (limitBytes == LIMIT_DISABLED) {
            mEnableDataLimitPreference.setChecked(false);
            mSetDataLimitPreference.setSummary(null);
        } else {
            mEnableDataLimitPreference.setChecked(true);
            mSetDataLimitPreference.setSummary(
                    DataUsageUtils.bytesToIecUnits(getContext(), limitBytes));
        }
        mSetDataLimitPreference.setEnabled(mEnableDataLimitPreference.isChecked());
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean enabled = (Boolean) newValue;
        if (!enabled) {
            mPolicyEditor.setPolicyLimitBytes(mNetworkTemplate, LIMIT_DISABLED);
            refreshUi();
            return true;
        }

        ConfirmationDialogFragment dialogFragment =
                new ConfirmationDialogFragment.Builder(getContext())
                        .setTitle(R.string.data_usage_limit_dialog_title)
                        .setMessage(R.string.data_usage_limit_dialog_mobile)
                        .setPositiveButton(android.R.string.ok, this)
                        .setNegativeButton(android.R.string.cancel, null)
                        .build();
        getFragmentController().showDialog(dialogFragment, ConfirmationDialogFragment.TAG);

        // This preference is enabled / disabled by ConfirmationDialogFragment.
        return false;
    }

    @Override
    public void onConfirm(@Nullable Bundle arguments) {
        NetworkPolicy policy = mPolicyEditor.getPolicy(mNetworkTemplate);
        long minLimitBytes = 0;
        if (policy != null) {
            minLimitBytes = (long) (policy.warningBytes * LIMIT_BYTES_MULTIPLIER);
        }

        long limitBytes = Math.max(5 * GIB_IN_BYTES, minLimitBytes);

        mPolicyEditor.setPolicyLimitBytes(mNetworkTemplate, limitBytes);
        refreshUi();
    }
}
