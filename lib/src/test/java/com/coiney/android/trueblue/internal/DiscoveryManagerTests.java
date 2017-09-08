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

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.test.filters.SdkSuppress;

import com.coiney.android.trueblue.BuildConfig;
import com.coiney.android.trueblue.DiscoveryError;
import com.coiney.android.trueblue.DiscoveryListener;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.quality.Strictness;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLooper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class DiscoveryManagerTests {

    // Ensure that Mockito is used despite using Robolectric's test runner.
    @Rule
    public MockitoRule mMockitoRule = MockitoJUnit.rule().strictness(Strictness.WARN);

    @Mock
    private AdapterManager mAdapterManager;
    private Context mContext;
    private DiscoveryManager mDiscoveryManager;

    @Before
    public void setUp() {
        final Logger logger = mock(Logger.class);
        mContext = Shadows.shadowOf(RuntimeEnvironment.application).getApplicationContext();
        mDiscoveryManager = new DiscoveryManager(mAdapterManager, mContext, logger);
    }

    @Test
    public void isDiscoveryRunningWhenDiscoveryIsRunningShouldReturnTrue() {
        // Set up the system such that discovery can be successfully "started".
        configureForSuccessfulDiscoveryStart();
        mDiscoveryManager.startDiscovery();
        assertTrue(mDiscoveryManager.isDiscoveryRunning());
    }

    @Test
    public void isDiscoveryRunningWhenDiscoveryIsNotRunningShouldReturnFalse() {
        assertFalse(mDiscoveryManager.isDiscoveryRunning());
    }

    @Test
    public void startDiscoveryWhenDiscoveryCanBeStartedShouldReturnNull() {
        configureForSuccessfulDiscoveryStart();
        assertNull(mDiscoveryManager.startDiscovery());
    }

    @Test
    public void startDiscoveryWhenAlreadyStartedInServiceShouldReturnError() {
        configureForSuccessfulDiscoveryStart();
        mDiscoveryManager.startDiscovery();
        assertEquals(DiscoveryError.ALREADY_RUNNING, mDiscoveryManager.startDiscovery());
    }

    @Test
    public void startDiscoveryAfterManualStopBeforeFinishedBroadcastReceivedShouldReturnError() {
        configureForSuccessfulDiscoveryStart();
        mDiscoveryManager.startDiscovery();
        mDiscoveryManager.stopDiscovery();
        assertEquals(DiscoveryError.ALREADY_RUNNING, mDiscoveryManager.startDiscovery());
    }

    @Test
    public void startDiscoveryAfterManualStopAndAfterFinishedBroadcastReceivedShouldReturnNull() {
        mDiscoveryManager.start();
        configureForSuccessfulDiscoveryStart();
        mDiscoveryManager.startDiscovery();
        mDiscoveryManager.stopDiscovery();
        mContext.sendBroadcast(new Intent(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
        ShadowLooper.getShadowMainLooper().runToEndOfTasks();
        assertNull(mDiscoveryManager.startDiscovery());
    }

    @Test
    @SdkSuppress(minSdkVersion = Build.VERSION_CODES.M)
    public void startDiscoveryWhenCoarseAccessPermissionsRequiredShouldReturnError() {
        assertEquals(DiscoveryError.COARSE_LOCATION_PERMISSION_REQUIRED,
                mDiscoveryManager.startDiscovery());
    }

    @Test
    public void startDiscoveryWhenBluetoothDisabledShouldReturnError() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Shadows.shadowOf(RuntimeEnvironment.application).grantPermissions(
                    Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        when(mAdapterManager.isAdapterEnabled()).thenReturn(false);
        assertEquals(DiscoveryError.BLUETOOTH_DISABLED, mDiscoveryManager.startDiscovery());
    }

    @Test
    public void startDiscoveryWhenAlreadyStartedOutsideServiceIsRunningShouldReturnError() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Shadows.shadowOf(RuntimeEnvironment.application).grantPermissions(
                    Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        when(mAdapterManager.isAdapterEnabled()).thenReturn(true);
        when(mAdapterManager.startDiscovery()).thenReturn(false);
        assertEquals(DiscoveryError.SYSTEM_ERROR, mDiscoveryManager.startDiscovery());
    }

    @Test
    public void stopDiscoveryWhenDiscoveryStartedAndStopRequestSucceedsShouldReturnTrue() {
        configureForSuccessfulDiscoveryStart();
        when(mAdapterManager.stopDiscovery()).thenReturn(true);
        mDiscoveryManager.startDiscovery();
        assertTrue(mDiscoveryManager.stopDiscovery());
    }

    @Test
    public void stopDiscoveryWhenDiscoveryStartedAndStopRequestFailsShouldReturnTrue() {
        configureForSuccessfulDiscoveryStart();
        when(mAdapterManager.stopDiscovery()).thenReturn(false);
        mDiscoveryManager.startDiscovery();
        assertFalse(mDiscoveryManager.stopDiscovery());
    }

    @Test
    public void stopDiscoveryWhenDiscoveryNotStartedShouldReturnFalse() {
        assertFalse(mDiscoveryManager.stopDiscovery());
    }

    @Test
    public void discoveryStartBroadcastWhenManagerStartedAndDiscoveryStartedViaManagerAndListenerRegisteredShouldNotifyListener() {
        mDiscoveryManager.start();
        configureForSuccessfulDiscoveryStart();
        final DiscoveryListener listener = mock(DiscoveryListener.class);
        mDiscoveryManager.registerListener(listener);
        mDiscoveryManager.startDiscovery();
        mContext.sendBroadcast(new Intent(BluetoothAdapter.ACTION_DISCOVERY_STARTED));
        ShadowLooper.getShadowMainLooper().runToEndOfTasks();
        verify(listener, times(1)).onDiscoveryStarted();
    }

    @Test
    public void discoveryStartBroadcastWhenManagerStartedAndDiscoveryStartedViaSystemAndListenerRegisteredShouldNotNotifyListener() {
        mDiscoveryManager.start();
        configureForSuccessfulDiscoveryStart();
        final DiscoveryListener listener = mock(DiscoveryListener.class);
        mDiscoveryManager.registerListener(listener);
        mContext.sendBroadcast(new Intent(BluetoothAdapter.ACTION_DISCOVERY_STARTED));
        ShadowLooper.getShadowMainLooper().runToEndOfTasks();
        verify(listener, times(0)).onDiscoveryStarted();
    }

    @Test
    public void discoveryStartBroadcastWhenManagerNotStartedAndDiscoveryStartedViaManagerAndListenerRegisteredShouldNotNotifyListener() {
        configureForSuccessfulDiscoveryStart();
        final DiscoveryListener listener = mock(DiscoveryListener.class);
        mDiscoveryManager.registerListener(listener);
        mDiscoveryManager.startDiscovery();
        mContext.sendBroadcast(new Intent(BluetoothAdapter.ACTION_DISCOVERY_STARTED));
        ShadowLooper.getShadowMainLooper().runToEndOfTasks();
        verify(listener, times(0)).onDiscoveryStarted();
    }

    @Test
    public void discoveryStartBroadcastWhenManagerNotStartedAndDiscoveryStartedViaManagerAndListenerUnregisteredShouldNotNotifyListener() {
        mDiscoveryManager.start();
        configureForSuccessfulDiscoveryStart();
        final DiscoveryListener listener = mock(DiscoveryListener.class);
        mDiscoveryManager.registerListener(listener);
        mDiscoveryManager.unregisterListener(listener);
        mDiscoveryManager.startDiscovery();
        mContext.sendBroadcast(new Intent(BluetoothAdapter.ACTION_DISCOVERY_STARTED));
        ShadowLooper.getShadowMainLooper().runToEndOfTasks();
        verify(listener, times(0)).onDiscoveryStarted();
    }

    @Test
    public void deviceFoundBroadcastWhenManagerStartedAndDiscoveryStartedViaManagerAndListenerRegisteredShouldNotifyListener() {
        mDiscoveryManager.start();
        configureForSuccessfulDiscoveryStart();
        final DiscoveryListener listener = mock(DiscoveryListener.class);
        mDiscoveryManager.registerListener(listener);
        mDiscoveryManager.startDiscovery();
        final BluetoothDevice bluetoothDevice = mock(BluetoothDevice.class);
        mContext.sendBroadcast(new Intent(BluetoothDevice.ACTION_FOUND)
                .putExtra(BluetoothDevice.EXTRA_DEVICE, bluetoothDevice));
        ShadowLooper.getShadowMainLooper().runToEndOfTasks();
        verify(listener, times(1)).onDeviceDiscovered(bluetoothDevice);
    }

    @Test
    public void deviceFoundBroadcastWhenManagerStartedAndDiscoveryStartedViaSystemAndListenerRegisteredShouldNotNotifyListener() {
        mDiscoveryManager.start();
        configureForSuccessfulDiscoveryStart();
        final DiscoveryListener listener = mock(DiscoveryListener.class);
        mDiscoveryManager.registerListener(listener);
        final BluetoothDevice bluetoothDevice = mock(BluetoothDevice.class);
        mContext.sendBroadcast(new Intent(BluetoothDevice.ACTION_FOUND)
                .putExtra(BluetoothDevice.EXTRA_DEVICE, bluetoothDevice));
        ShadowLooper.getShadowMainLooper().runToEndOfTasks();
        verify(listener, times(0)).onDeviceDiscovered(bluetoothDevice);
    }

    @Test
    public void deviceFoundBroadcastWhenManagerNotStartedAndDiscoveryStartedViaManagerAndListenerRegisteredShouldNotNotifyListener() {
        configureForSuccessfulDiscoveryStart();
        final DiscoveryListener listener = mock(DiscoveryListener.class);
        mDiscoveryManager.registerListener(listener);
        mDiscoveryManager.startDiscovery();
        final BluetoothDevice bluetoothDevice = mock(BluetoothDevice.class);
        mContext.sendBroadcast(new Intent(BluetoothDevice.ACTION_FOUND)
                .putExtra(BluetoothDevice.EXTRA_DEVICE, bluetoothDevice));
        ShadowLooper.getShadowMainLooper().runToEndOfTasks();
        verify(listener, times(0)).onDeviceDiscovered(bluetoothDevice);
    }

    @Test
    public void deviceFoundBroadcastWhenManagerStartedAndDiscoveryStartedViaManagerAndListenerUnregisteredShouldNotNotifyListener() {
        mDiscoveryManager.start();
        configureForSuccessfulDiscoveryStart();
        final DiscoveryListener listener = mock(DiscoveryListener.class);
        mDiscoveryManager.registerListener(listener);
        mDiscoveryManager.unregisterListener(listener);
        mDiscoveryManager.startDiscovery();
        final BluetoothDevice bluetoothDevice = mock(BluetoothDevice.class);
        mContext.sendBroadcast(new Intent(BluetoothDevice.ACTION_FOUND)
                .putExtra(BluetoothDevice.EXTRA_DEVICE, bluetoothDevice));
        ShadowLooper.getShadowMainLooper().runToEndOfTasks();
        verify(listener, times(0)).onDeviceDiscovered(bluetoothDevice);
    }

    @Test
    public void discoveryFinishedBroadcastWhenManagerStartedAndDiscoveryStartedViaManagerAndListenerRegisteredShouldNotifyListener() {
        mDiscoveryManager.start();
        configureForSuccessfulDiscoveryStart();
        final DiscoveryListener listener = mock(DiscoveryListener.class);
        mDiscoveryManager.registerListener(listener);
        mDiscoveryManager.startDiscovery();
        mContext.sendBroadcast(new Intent(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
        ShadowLooper.getShadowMainLooper().runToEndOfTasks();
        verify(listener, times(1)).onDiscoveryFinished();
    }

    @Test
    public void discoveryFinishedBroadcastWhenManagerStartedAndDiscoveryStartedViaSystemAndListenerRegisteredShouldNotNotifyListener() {
        mDiscoveryManager.start();
        configureForSuccessfulDiscoveryStart();
        final DiscoveryListener listener = mock(DiscoveryListener.class);
        mDiscoveryManager.registerListener(listener);
        mContext.sendBroadcast(new Intent(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
        ShadowLooper.getShadowMainLooper().runToEndOfTasks();
        verify(listener, times(0)).onDiscoveryFinished();
    }

    @Test
    public void discoveryFinishedBroadcastWhenManagerNotStartedAndDiscoveryStartedViaManagerAndListenerRegisteredShouldNotNotifyListener() {
        configureForSuccessfulDiscoveryStart();
        final DiscoveryListener listener = mock(DiscoveryListener.class);
        mDiscoveryManager.registerListener(listener);
        mDiscoveryManager.startDiscovery();
        mContext.sendBroadcast(new Intent(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
        ShadowLooper.getShadowMainLooper().runToEndOfTasks();
        verify(listener, times(0)).onDiscoveryFinished();
    }

    @Test
    public void discoveryFinishedBroadcastWhenManagerStartedAndDiscoveryStartedViaManagerAndListenerUnregisteredShouldNotNotifyListener() {
        mDiscoveryManager.start();
        configureForSuccessfulDiscoveryStart();
        final DiscoveryListener listener = mock(DiscoveryListener.class);
        mDiscoveryManager.registerListener(listener);
        mDiscoveryManager.unregisterListener(listener);
        mDiscoveryManager.startDiscovery();
        mContext.sendBroadcast(new Intent(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
        ShadowLooper.getShadowMainLooper().runToEndOfTasks();
        verify(listener, times(0)).onDiscoveryFinished();
    }

    private void configureForSuccessfulDiscoveryStart() {
        Shadows.shadowOf(RuntimeEnvironment.application).grantPermissions(
                Manifest.permission.ACCESS_COARSE_LOCATION);
        when(mAdapterManager.isAdapterEnabled()).thenReturn(true);
        when(mAdapterManager.startDiscovery()).thenReturn(true);
    }
}
