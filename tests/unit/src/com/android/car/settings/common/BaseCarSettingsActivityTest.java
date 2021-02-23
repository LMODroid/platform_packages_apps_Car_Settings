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

package com.android.car.settings.common;

import static com.google.common.truth.Truth.assertThat;

import static org.testng.Assert.assertThrows;

import android.car.drivingstate.CarUxRestrictions;
import android.content.Context;
import android.view.View;

import androidx.fragment.app.DialogFragment;
import androidx.preference.Preference;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import com.android.car.settings.R;
import com.android.car.settings.testutils.BaseCarSettingsTestActivity;
import com.android.car.settings.testutils.BaseTestSettingsFragment;
import com.android.car.ui.baselayout.Insets;
import com.android.car.ui.toolbar.Toolbar;
import com.android.car.ui.toolbar.ToolbarController;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicReference;

@RunWith(AndroidJUnit4.class)
public class BaseCarSettingsActivityTest {

    private Context mContext = ApplicationProvider.getApplicationContext();
    private BaseCarSettingsTestActivity mActivity;

    @Rule
    public ActivityTestRule<BaseCarSettingsTestActivity> mActivityTestRule =
            new ActivityTestRule<>(BaseCarSettingsTestActivity.class);

    @Before
    public void setUp() throws Throwable {
        mActivity = mActivityTestRule.getActivity();
    }

    @Test
    public void onPreferenceStartFragment_launchesFragment() throws Throwable {
        Preference pref = new Preference(mContext);
        pref.setFragment(BaseTestSettingsFragment.class.getName());

        mActivityTestRule.runOnUiThread(() ->
                mActivity.onPreferenceStartFragment(/* caller= */ null, pref));
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        assertThat(mActivity.getSupportFragmentManager().findFragmentById(
                R.id.fragment_container)).isInstanceOf(BaseTestSettingsFragment.class);
    }

    @Test
    public void launchFragment_dialogFragment_throwsError() {
        DialogFragment dialogFragment = new DialogFragment();

        assertThrows(IllegalArgumentException.class,
                () -> mActivity.launchFragment(dialogFragment));
    }

    @Test
    public void onUxRestrictionsChanged_topFragmentInBackStackHasUpdatedUxRestrictions()
            throws Throwable {
        CarUxRestrictions oldUxRestrictions = new CarUxRestrictions.Builder(
                /* reqOpt= */ true,
                CarUxRestrictions.UX_RESTRICTIONS_BASELINE,
                /* timestamp= */ 0
        ).build();

        CarUxRestrictions newUxRestrictions = new CarUxRestrictions.Builder(
                /* reqOpt= */ true,
                CarUxRestrictions.UX_RESTRICTIONS_NO_SETUP,
                /* timestamp= */ 0
        ).build();

        AtomicReference<BaseTestSettingsFragment> fragmentA = new AtomicReference<>();
        AtomicReference<BaseTestSettingsFragment> fragmentB = new AtomicReference<>();

        mActivityTestRule.runOnUiThread(() -> {
            fragmentA.set(new BaseTestSettingsFragment());
            fragmentB.set(new BaseTestSettingsFragment());
            mActivity.launchFragment(fragmentA.get());
            mActivity.onUxRestrictionsChanged(oldUxRestrictions);
            mActivity.launchFragment(fragmentB.get());
            mActivity.onUxRestrictionsChanged(newUxRestrictions);
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        assertThat(
                fragmentB.get().getUxRestrictions().isSameRestrictions(newUxRestrictions)).isTrue();
    }

    @Test
    @Ignore("b/178512946")
    public void onBackStackChanged_uxRestrictionsChanged_currentFragmentHasUpdatedUxRestrictions()
            throws Throwable {
        CarUxRestrictions oldUxRestrictions = new CarUxRestrictions.Builder(
                /* reqOpt= */ true,
                CarUxRestrictions.UX_RESTRICTIONS_BASELINE,
                /* timestamp= */ 0
        ).build();

        CarUxRestrictions newUxRestrictions = new CarUxRestrictions.Builder(
                /* reqOpt= */ true,
                CarUxRestrictions.UX_RESTRICTIONS_NO_SETUP,
                /* timestamp= */ 0
        ).build();

        AtomicReference<BaseTestSettingsFragment> fragmentA = new AtomicReference<>();
        AtomicReference<BaseTestSettingsFragment> fragmentB = new AtomicReference<>();

        mActivityTestRule.runOnUiThread(() -> {
            fragmentA.set(new BaseTestSettingsFragment());
            fragmentB.set(new BaseTestSettingsFragment());
            mActivity.launchFragment(fragmentA.get());
            mActivity.onUxRestrictionsChanged(oldUxRestrictions);
            mActivity.launchFragment(fragmentB.get());
            mActivity.onUxRestrictionsChanged(newUxRestrictions);
            mActivity.goBack();
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        assertThat(
                fragmentA.get().getUxRestrictions().isSameRestrictions(newUxRestrictions)).isTrue();
    }

    @Test
    @Ignore("b/178512946")
    public void onBackStackChanged_toolbarUpdated() throws Throwable {
        ToolbarController toolbar = mActivity.getToolbar();

        mActivityTestRule.runOnUiThread(() -> {
            BaseTestSettingsFragment fragment1 = new BaseTestSettingsFragment();
            mActivity.launchFragment(fragment1);
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        assertThat(toolbar.getState()).isEqualTo(Toolbar.State.HOME);

        mActivityTestRule.runOnUiThread(() -> {
            BaseTestSettingsFragment fragment2 = new BaseTestSettingsFragment();
            mActivity.launchFragment(fragment2);
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        assertThat(toolbar.getState()).isEqualTo(Toolbar.State.SUBPAGE);
        assertThat(toolbar.getNavButtonMode()).isEqualTo(Toolbar.NavButtonMode.BACK);

        mActivityTestRule.runOnUiThread(() -> mActivity.goBack());
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        assertThat(toolbar.getState()).isEqualTo(Toolbar.State.HOME);
    }

    @Test
    public void onInsetsChanged_paddingUpdated() throws Throwable {
        int testInsetValue = 15;
        Insets testInsets =
                new Insets(testInsetValue, testInsetValue, testInsetValue, testInsetValue);
        mActivityTestRule.runOnUiThread(() -> mActivity.onCarUiInsetsChanged(testInsets));
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        View activityWrapper = mActivity.findViewById(R.id.car_settings_activity_wrapper);
        assertThat(activityWrapper.getPaddingTop()).isEqualTo(testInsets.getTop());
        assertThat(activityWrapper.getPaddingRight()).isEqualTo(testInsets.getRight());
        assertThat(activityWrapper.getPaddingBottom()).isEqualTo(testInsets.getBottom());
        assertThat(activityWrapper.getPaddingLeft()).isEqualTo(testInsets.getLeft());
    }
}