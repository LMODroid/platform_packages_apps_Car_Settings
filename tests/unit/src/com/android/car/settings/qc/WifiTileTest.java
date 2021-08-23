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

package com.android.car.settings.qc;

import static com.android.car.qc.QCItem.QC_ACTION_TOGGLE_STATE;

import static com.google.common.truth.Truth.assertThat;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.android.car.qc.QCItem;
import com.android.car.qc.QCTile;
import com.android.car.settings.R;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@RunWith(AndroidJUnit4.class)
public class WifiTileTest {
    private Context mContext = spy(ApplicationProvider.getApplicationContext());
    private WifiTile mWifiTile;

    @Mock
    private WifiManager mWifiManager;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(mContext.getSystemService(WifiManager.class)).thenReturn(mWifiManager);

        mWifiTile = new WifiTile(mContext);
    }

    @Test
    public void getQCItem_wifiDisabled_returnsTile() {
        when(mWifiManager.isWifiEnabled()).thenReturn(false);
        when(mWifiManager.getWifiState()).thenReturn(WifiManager.WIFI_STATE_DISABLED);
        QCItem item = mWifiTile.getQCItem();
        assertThat(item).isNotNull();
        assertThat(item instanceof QCTile).isTrue();
        QCTile tile = (QCTile) item;
        assertThat(tile.getIcon()).isNotNull();
        assertThat(tile.isChecked()).isFalse();
        assertThat(tile.getSubtitle()).isEqualTo(mContext.getString(R.string.wifi_disabled));
    }

    @Test
    public void getQCItem_wifiNotConnected_returnsTile() {
        when(mWifiManager.isWifiEnabled()).thenReturn(true);
        when(mWifiManager.getWifiState()).thenReturn(WifiManager.WIFI_STATE_ENABLED);
        WifiInfo wifiInfo = mock(WifiInfo.class);
        when(wifiInfo.getSSID()).thenReturn(WifiManager.UNKNOWN_SSID);
        when(mWifiManager.getConnectionInfo()).thenReturn(wifiInfo);
        QCItem item = mWifiTile.getQCItem();
        assertThat(item).isNotNull();
        assertThat(item instanceof QCTile).isTrue();
        QCTile tile = (QCTile) item;
        assertThat(tile.getIcon()).isNotNull();
        assertThat(tile.isChecked()).isTrue();
        assertThat(tile.getSubtitle()).isEqualTo(mContext.getString(R.string.wifi_disconnected));
    }

    @Test
    public void getQCItem_wifiConnected_returnsTile() {
        String testSSID = "TEST_SSID";
        when(mWifiManager.isWifiEnabled()).thenReturn(true);
        when(mWifiManager.getWifiState()).thenReturn(WifiManager.WIFI_STATE_ENABLED);
        WifiInfo wifiInfo = mock(WifiInfo.class);
        when(wifiInfo.getSSID()).thenReturn(testSSID);
        when(mWifiManager.getConnectionInfo()).thenReturn(wifiInfo);
        QCItem item = mWifiTile.getQCItem();
        assertThat(item).isNotNull();
        assertThat(item instanceof QCTile).isTrue();
        QCTile tile = (QCTile) item;
        assertThat(tile.getIcon()).isNotNull();
        assertThat(tile.isChecked()).isTrue();
        assertThat(tile.getSubtitle()).isEqualTo(testSSID);
    }

    @Test
    public void onNotifyChange_togglesWifi() {
        when(mWifiManager.isWifiEnabled()).thenReturn(false);
        when(mWifiManager.getWifiState()).thenReturn(WifiManager.WIFI_STATE_DISABLED);
        Intent intent = new Intent();
        intent.putExtra(QC_ACTION_TOGGLE_STATE, true);
        mWifiTile.onNotifyChange(intent);
        verify(mWifiManager).setWifiEnabled(true);
    }
}