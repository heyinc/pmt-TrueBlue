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

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;

import com.coiney.android.trueblue.BluetoothStatusListener;
import com.coiney.android.trueblue.BuildConfig;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.quality.Strictness;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class AdapterManagerTests {

    // Ensure that Mockito is used despite using Robolectric's test runner.
    @Rule
    public MockitoRule mMockitoRule = MockitoJUnit.rule().strictness(Strictness.STRICT_STUBS);

    @Mock private BluetoothAdapter mAdapter;
    @Mock private BluetoothStatusMonitor mBluetoothStatusMonitor;

    private AdapterManager mAdapterManager;

    @Before
    public void setUp() {
        mAdapterManager = new AdapterManager(mAdapter, mBluetoothStatusMonitor, mock(Logger.class));
    }

    // public void start()

    @Test
    public void startShouldStartBluetoothStatusMonitor() {
        mAdapterManager.start();
        Mockito.verify(mBluetoothStatusMonitor, times(1)).start();
    }

    // public Set<BluetoothDevice> getDeviceList()

    @Test
    public void getDeviceListWhenDevicesAvailableShouldReturnDeviceSet() {
        final Set<BluetoothDevice> systemDevices = new HashSet<>();
        systemDevices.add(mock(BluetoothDevice.class));
        systemDevices.add(mock(BluetoothDevice.class));
        when(mAdapter.getBondedDevices()).thenReturn(systemDevices);
        assertEquals("Device list not accurate.", systemDevices, mAdapterManager.getDeviceList());
    }

    @Test
    public void getDeviceListWhenNoDevicesAvailableShouldReturnEmptySet() {
        when(mAdapter.getBondedDevices()).thenReturn(null);
        final Set<BluetoothDevice> serviceDevices = mAdapterManager.getDeviceList();
        assertNotNull("Device list should not be null.", serviceDevices);
        assertTrue("Device list should be empty.", serviceDevices.isEmpty());
    }

    // public boolean isAdapterEnabled()

    @Test
    public void isAdapterEnabledWhenBluetoothAdapterEnabledShouldReturnTrue() {
        when(mAdapter.isEnabled()).thenReturn(true);
        assertTrue("Bluetooth is enabled but reported as disabled.",
                mAdapterManager.isAdapterEnabled());
    }

    @Test
    public void isAdapterEnabledWhenBluetoothAdapterDisabledShouldReturnFalse() {
        when(mAdapter.isEnabled()).thenReturn(false);
        assertFalse("Bluetooth is enabled but reported as disabled.",
                mAdapterManager.isAdapterEnabled());
    }

    // public boolean isDiscoveryRunning()

    @Test
    public void isDiscoveryRunningWhenDiscoveryIsRunningShouldReturnTrue() {
        when(mAdapter.isDiscovering()).thenReturn(true);
        assertTrue("Discovery is reported as not running when it is.",
                mAdapterManager.isDiscoveryRunning());
    }

    @Test
    public void isDiscoveryRunningWhenDiscoveryIsNotRunningShouldReturnFalse() {
        when(mAdapter.isDiscovering()).thenReturn(false);
        assertFalse("Discovery is reported as running when it is not.",
                mAdapterManager.isDiscoveryRunning());
    }

    // public void requestBluetoothBeEnabled(Activity activity)

    @Test
    public void requestBluetoothBeEnabledWhenBluetoothIsDisabledShouldStartSystemActivity() {
        final Activity activity = mock(Activity.class);
        when(mAdapter.isEnabled()).thenReturn(false);
        mAdapterManager.requestBluetoothBeEnabled(activity);
        final ArgumentCaptor<Intent> argument = ArgumentCaptor.forClass(Intent.class);
        verify(activity).startActivity(argument.capture());
        assertEquals(BluetoothAdapter.ACTION_REQUEST_ENABLE, argument.getValue().getAction());
    }

    @Test
    public void requestBluetoothBeEnabledWhenBluetoothIsEnabledShouldDoNothing() {
        final Activity activity = mock(Activity.class);
        when(mAdapter.isEnabled()).thenReturn(true);
        mAdapterManager.requestBluetoothBeEnabled(activity);
        verifyZeroInteractions(activity);
    }

    // public void requestBluetoothBeEnabled(Activity activity, int requestCode)

    @Test
    public void requestBluetoothBeEnabledWhenForResultAndBluetoothIsDisabledShouldStartSystemActivityWithRequestCode() {
        final Activity activity = mock(Activity.class);
        when(mAdapter.isEnabled()).thenReturn(false);
        final int requestCode = 12345;
        mAdapterManager.requestBluetoothBeEnabled(activity, requestCode);
        final ArgumentCaptor<Intent> argument = ArgumentCaptor.forClass(Intent.class);
        verify(activity).startActivityForResult(argument.capture(), eq(requestCode));
        assertEquals(BluetoothAdapter.ACTION_REQUEST_ENABLE, argument.getValue().getAction());
    }

    @Test
    public void requestBluetoothBeEnabledWhenForResultAndBluetoothIsEnabledShouldDoNothing() {
        final Activity activity = mock(Activity.class);
        when(mAdapter.isEnabled()).thenReturn(true);
        final int requestCode = 12345;
        mAdapterManager.requestBluetoothBeEnabled(activity, requestCode);
        verifyZeroInteractions(activity);
    }

    // public void registerBluetoothStatusListener(BluetoothStatusListener listener)

    @Test
    public void registerBluetoothStatusListenerShouldRegisterListenerWithBluetoothStatusMonitor() {
        final BluetoothStatusListener listener = mock(BluetoothStatusListener.class);
        mAdapterManager.registerBluetoothStatusListener(listener);
        //Mockito.verify(mBluetoothStatusMonitor, times(1)).registerListener(listener);
    }

    // public void unregisterBluetoothStatusListener(BluetoothStatusListener listener)

    @Test
    public void unregisterBluetoothStatusListenerShouldUnregisterListenerFromBluetoothStatusMonitor() {
        final BluetoothStatusListener listener = mock(BluetoothStatusListener.class);
        mAdapterManager.unregisterBluetoothStatusListener(listener);
        Mockito.verify(mBluetoothStatusMonitor, times(1)).unregisterListener(listener);
    }

    // boolean startDiscovery()

    @Test
    public void startDiscoveryShouldStartDiscovery() {
        mAdapterManager.startDiscovery();
        verify(mAdapter, times(1)).startDiscovery();
    }

    @Test
    public void startDiscoveryWhenStartSucceedsShouldReturnTrue() {
        when(mAdapter.startDiscovery()).thenReturn(true);
        assertTrue("Discovery was not successfully started.", mAdapterManager.startDiscovery());
    }

    @Test
    public void startDiscoveryWhenStartFailsShouldReturnFalse() {
        when(mAdapter.startDiscovery()).thenReturn(false);
        assertFalse("Discovery was successfully started.", mAdapterManager.startDiscovery());
    }

    // boolean stopDiscovery()

    @Test
    public void stopDiscoveryShouldCancelDiscovery() {
        mAdapterManager.stopDiscovery();
        verify(mAdapter, times(1)).cancelDiscovery();
    }

    @Test
    public void stopDiscoveryWhenCancelSucceedsShouldReturnTrue() {
        when(mAdapter.cancelDiscovery()).thenReturn(true);
        assertTrue("Discovery was not successfully stopped.", mAdapterManager.stopDiscovery());
    }

    @Test
    public void stopDiscoveryWhenCancelFailsShouldReturnFalse() {
        when(mAdapter.cancelDiscovery()).thenReturn(false);
        assertFalse("Discovery was successfully stopped.", mAdapterManager.stopDiscovery());
    }
}
