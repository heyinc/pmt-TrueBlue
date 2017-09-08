/*
 * Copyright 2017 Coiney, Inc.
 * Copyright 2016 - 2017 Daniel Carter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.coiney.android.trueblue.internal;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Build;
import android.os.ParcelUuid;
import android.support.test.filters.SdkSuppress;

import com.coiney.android.trueblue.BuildConfig;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.quality.Strictness;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.IOException;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * A collection of useful Closeable related utility methods.
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public final class BluetoothUtilsTests {

    // Ensure that Mockito is used despite using Robolectric's test runner.
    @Rule
    public MockitoRule mMockitoRule = MockitoJUnit.rule().strictness(Strictness.WARN);

    @Test
    public void closeSocketSilentlyCloseSucceeds() throws Exception {
        final BluetoothSocket bluetoothSocket = mock(BluetoothSocket.class);
        BluetoothUtils.closeSocketSilently(bluetoothSocket);
    }

    @Test
    public void closeSocketSilentlyCloseFails() throws Exception {
        final BluetoothSocket bluetoothSocket = mock(BluetoothSocket.class);
        doThrow(new IOException()).when(bluetoothSocket).close();
        BluetoothUtils.closeSocketSilently(bluetoothSocket);
    }

    @Test
    public void closeSocketSilentlyNullParameter() throws Exception {
        BluetoothUtils.closeSocketSilently(null);
    }

    // This method behaves differently on APIs 14 and below and APIs 15 and
    // above. Unfortunately, we can't specifically test the former because
    // Robolectric support starts at API 16.
    @Test
    public void getLikelyServiceRecordUuidBasic() {
        final BluetoothDevice device = mock(BluetoothDevice.class);
        assertEquals(BluetoothUtils.getLikelyServiceRecordUuid(device),
                UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
    }

    @Test
    @SdkSuppress(minSdkVersion = Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
    public void getLikelyServiceRecordUuidNoDeviceUuidsAvailable() {
        final BluetoothDevice device = mock(BluetoothDevice.class);
        when(device.getUuids()).thenReturn(null);
        assertEquals(BluetoothUtils.getLikelyServiceRecordUuid(device),
                UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
    }

    @Test
    @SdkSuppress(minSdkVersion = Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
    public void getLikelyServiceRecordUuidDeviceUuisEmpty() {
        final BluetoothDevice device = mock(BluetoothDevice.class);
        when(device.getUuids()).thenReturn(new ParcelUuid[]{});
        assertEquals(BluetoothUtils.getLikelyServiceRecordUuid(device),
                UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
    }

    @Test
    @SdkSuppress(minSdkVersion = Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
    public void getLikelyServiceRecordUuidOneDeviceUuidAvailableWithNoActualUuid() {
        final BluetoothDevice device = mock(BluetoothDevice.class);
        final ParcelUuid parcelUuid = mock(ParcelUuid.class);
        when(parcelUuid.getUuid()).thenReturn(null);
        when(device.getUuids()).thenReturn(new ParcelUuid[]{parcelUuid});
        assertEquals(BluetoothUtils.getLikelyServiceRecordUuid(device),
                UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
    }

    @Test
    @SdkSuppress(minSdkVersion = Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
    public void getLikelyServiceRecordUuidOneDeviceUuidAvailableWithActualUuid() {
        final BluetoothDevice device = mock(BluetoothDevice.class);
        final ParcelUuid parcelUuid = mock(ParcelUuid.class);
        final UUID uuid = UUID.randomUUID();
        when(parcelUuid.getUuid()).thenReturn(uuid);
        when(device.getUuids()).thenReturn(new ParcelUuid[]{parcelUuid});
        assertEquals(BluetoothUtils.getLikelyServiceRecordUuid(device), uuid);
    }

    @Test
    @SdkSuppress(minSdkVersion = Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
    public void getLikelyServiceRecordUuidWhenMultipleDeviceUuidsAllWithActualUuidsAvailableShouldReturnFirstOne() {
        final BluetoothDevice device = mock(BluetoothDevice.class);
        final ParcelUuid parcelUuid1 = mock(ParcelUuid.class);
        final UUID uuid1 = UUID.randomUUID();
        when(parcelUuid1.getUuid()).thenReturn(uuid1);
        // Mockito thinks this is unnecessary stubbing, but I disagree. The
        // code under test will never execute the stubbed method, but that is
        // an implementation detail. As a result we use "warn" rather than
        // "strict" level Mockito strictness in this test file.
        final ParcelUuid parcelUuid2 = mock(ParcelUuid.class);
        final UUID uuid2 = UUID.randomUUID();
        when(parcelUuid2.getUuid()).thenReturn(uuid2);
        when(device.getUuids()).thenReturn(new ParcelUuid[]{parcelUuid1, parcelUuid2});
        assertEquals(BluetoothUtils.getLikelyServiceRecordUuid(device), uuid1);
    }

    @Test
    @SdkSuppress(minSdkVersion = Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
    public void getLikelyServiceRecordUuidWithMultipleDeviceUuidsAvailableShouldReturnFirstWithActualUuid() {
        final BluetoothDevice device = mock(BluetoothDevice.class);
        final ParcelUuid parcelUuid1 = mock(ParcelUuid.class);
        when(parcelUuid1.getUuid()).thenReturn(null);
        final ParcelUuid parcelUuid2 = mock(ParcelUuid.class);
        final UUID uuid2 = UUID.randomUUID();
        when(parcelUuid2.getUuid()).thenReturn(uuid2);
        when(device.getUuids()).thenReturn(new ParcelUuid[]{parcelUuid1, parcelUuid2});
        assertEquals(BluetoothUtils.getLikelyServiceRecordUuid(device), uuid2);
    }
}
