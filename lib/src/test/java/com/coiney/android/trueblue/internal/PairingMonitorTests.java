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
import android.content.Context;
import android.content.Intent;

import com.coiney.android.trueblue.BuildConfig;

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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class PairingMonitorTests {

    // Ensure that Mockito is used despite using Robolectric's test runner.
    @Rule
    public MockitoRule mMockitoRule = MockitoJUnit.rule().strictness(Strictness.WARN);

    @Mock
    private Context mContext;

    private PairingMonitor mPairingMonitor;

    @Before
    public void setUp() {
        mContext = Shadows.shadowOf(RuntimeEnvironment.application).getApplicationContext();
        mPairingMonitor = new PairingMonitor(mContext);
    }

    @Test
    public void listenerWhenMonitorStartedAndListenerRegisteredAndPairingStartedShouldReceiveNotification() {
        final PairingMonitor.Listener listener = mock(PairingMonitor.Listener.class);
        final BluetoothDevice bluetoothDevice = mock(BluetoothDevice.class);
        mPairingMonitor.start();
        mPairingMonitor.registerListener(listener);
        mContext.sendBroadcast(new Intent(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
                .putExtra(BluetoothDevice.EXTRA_DEVICE, bluetoothDevice)
                .putExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.BOND_NONE)
                .putExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.BOND_BONDING));
        ShadowLooper.getShadowMainLooper().runToEndOfTasks();
        verify(listener, times(1)).onPairingAttemptStarted(bluetoothDevice);
    }

    @Test
    public void listenerWhenMonitorNotStartedAndListenerRegisteredAndPairingStartedShouldNotReceiveNotification() {
        final PairingMonitor.Listener listener = mock(PairingMonitor.Listener.class);
        final BluetoothDevice bluetoothDevice = mock(BluetoothDevice.class);
        mPairingMonitor.registerListener(listener);
        mContext.sendBroadcast(new Intent(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
                .putExtra(BluetoothDevice.EXTRA_DEVICE, bluetoothDevice)
                .putExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.BOND_NONE)
                .putExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.BOND_BONDING));
        ShadowLooper.getShadowMainLooper().runToEndOfTasks();
        verify(listener, times(0)).onPairingAttemptStarted(bluetoothDevice);
    }

    @Test
    public void listenerWhenMonitorStartedAndListenerUnregisteredAndPairingStartedShouldNotReceiveNotification() {
        final PairingMonitor.Listener listener = mock(PairingMonitor.Listener.class);
        final BluetoothDevice bluetoothDevice = mock(BluetoothDevice.class);
        mPairingMonitor.start();
        mPairingMonitor.registerListener(listener);
        mPairingMonitor.unregisterListener(listener);
        mContext.sendBroadcast(new Intent(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
                .putExtra(BluetoothDevice.EXTRA_DEVICE, bluetoothDevice)
                .putExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.BOND_NONE)
                .putExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.BOND_BONDING));
        ShadowLooper.getShadowMainLooper().runToEndOfTasks();
        verify(listener, times(0)).onPairingAttemptStarted(bluetoothDevice);
    }

    @Test
    public void listenerWhenMonitorStartedAndListenerRegisteredAndPairingSucceededShouldReceiveNotification() {
        final PairingMonitor.Listener listener = mock(PairingMonitor.Listener.class);
        final BluetoothDevice bluetoothDevice = mock(BluetoothDevice.class);
        mPairingMonitor.start();
        mPairingMonitor.registerListener(listener);
        mContext.sendBroadcast(new Intent(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
                .putExtra(BluetoothDevice.EXTRA_DEVICE, bluetoothDevice)
                .putExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.BOND_BONDING)
                .putExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.BOND_BONDED));
        ShadowLooper.getShadowMainLooper().runToEndOfTasks();
        verify(listener, times(1)).onPairingAttemptSucceeded(bluetoothDevice);
    }

    @Test
    public void listenerWhenMonitorNotStartedAndListenerRegisteredAndPairingSucceededShouldNotReceiveNotification() {
        final PairingMonitor.Listener listener = mock(PairingMonitor.Listener.class);
        final BluetoothDevice bluetoothDevice = mock(BluetoothDevice.class);
        mPairingMonitor.registerListener(listener);
        mContext.sendBroadcast(new Intent(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
                .putExtra(BluetoothDevice.EXTRA_DEVICE, bluetoothDevice)
                .putExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.BOND_BONDING)
                .putExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.BOND_BONDED));
        ShadowLooper.getShadowMainLooper().runToEndOfTasks();
        verify(listener, times(0)).onPairingAttemptSucceeded(bluetoothDevice);
    }

    @Test
    public void listenerWhenMonitorStartedAndListenerUnregisteredAndPairingSucceededShouldNotReceiveNotification() {
        final PairingMonitor.Listener listener = mock(PairingMonitor.Listener.class);
        final BluetoothDevice bluetoothDevice = mock(BluetoothDevice.class);
        mPairingMonitor.start();
        mPairingMonitor.registerListener(listener);
        mPairingMonitor.unregisterListener(listener);
        mContext.sendBroadcast(new Intent(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
                .putExtra(BluetoothDevice.EXTRA_DEVICE, bluetoothDevice)
                .putExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.BOND_BONDING)
                .putExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.BOND_BONDED));
        ShadowLooper.getShadowMainLooper().runToEndOfTasks();
        verify(listener, times(0)).onPairingAttemptSucceeded(bluetoothDevice);
    }

    @Test
    public void listenerWhenMonitorStartedAndListenerRegisteredAndPairingFailedShouldReceiveNotification() {
        final PairingMonitor.Listener listener = mock(PairingMonitor.Listener.class);
        final BluetoothDevice bluetoothDevice = mock(BluetoothDevice.class);
        mPairingMonitor.start();
        mPairingMonitor.registerListener(listener);
        mContext.sendBroadcast(new Intent(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
                .putExtra(BluetoothDevice.EXTRA_DEVICE, bluetoothDevice)
                .putExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.BOND_BONDING)
                .putExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.BOND_NONE));
        ShadowLooper.getShadowMainLooper().runToEndOfTasks();
        verify(listener, times(1)).onPairingAttemptFailed(bluetoothDevice);
    }

    @Test
    public void listenerWhenMonitorNotStartedAndListenerRegisteredAndPairingFailedShouldNotReceiveNotification() {
        final PairingMonitor.Listener listener = mock(PairingMonitor.Listener.class);
        final BluetoothDevice bluetoothDevice = mock(BluetoothDevice.class);
        mPairingMonitor.registerListener(listener);
        mContext.sendBroadcast(new Intent(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
                .putExtra(BluetoothDevice.EXTRA_DEVICE, bluetoothDevice)
                .putExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.BOND_BONDING)
                .putExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.BOND_NONE));
        ShadowLooper.getShadowMainLooper().runToEndOfTasks();
        verify(listener, times(0)).onPairingAttemptFailed(bluetoothDevice);
    }

    @Test
    public void listenerWhenMonitorStartedAndListenerUnregisteredAndPairingFailedShouldNotReceiveNotification() {
        final PairingMonitor.Listener listener = mock(PairingMonitor.Listener.class);
        final BluetoothDevice bluetoothDevice = mock(BluetoothDevice.class);
        mPairingMonitor.start();
        mPairingMonitor.registerListener(listener);
        mPairingMonitor.unregisterListener(listener);
        mContext.sendBroadcast(new Intent(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
                .putExtra(BluetoothDevice.EXTRA_DEVICE, bluetoothDevice)
                .putExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.BOND_BONDING)
                .putExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.BOND_NONE));
        ShadowLooper.getShadowMainLooper().runToEndOfTasks();
        verify(listener, times(0)).onPairingAttemptFailed(bluetoothDevice);
    }
}
