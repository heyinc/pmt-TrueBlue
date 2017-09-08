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

import com.coiney.android.trueblue.BuildConfig;
import com.coiney.android.trueblue.ConnectionAttemptCallback;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.quality.Strictness;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.concurrent.Executors;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * The lack of a functional Android Bluetooth subsystem makes it extremely
 * difficult to test ConnectionManager without resorting to tactics which make
 * tests fragile. As a result, current test coverage is minimal.
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class ConnectionManagerTests {

    // Ensure that Mockito is used despite using Robolectric's test runner.
    @Rule
    public MockitoRule mMockitoRule = MockitoJUnit.rule().strictness(Strictness.WARN);

    @Mock private AdapterManager mAdapterManager;
    @Mock private PairingMonitor mPairingMonitor;
    @Mock private ConnectionAttemptCallback mConnectionAttemptCallback;

    private ConnectionManager mConnectionManager;

    @Before
    public void setUp() {
        mConnectionManager = new ConnectionManager(mAdapterManager, mPairingMonitor,
                Executors.newSingleThreadExecutor(), mock(Logger.class));
    }

    @Test
    public void connectWhenTheDeviceIsNotManagedShouldReturnTrue() {
        final BluetoothDevice bluetoothDevice = mock(BluetoothDevice.class);
        assertTrue(mConnectionManager.connect(bluetoothDevice,
                mock(ConnectionConfiguration.class), mConnectionAttemptCallback));
    }

    @Test
    public void connectWhenTheDeviceIsAlreadyManagedShouldReturnFalse() {
        final BluetoothDevice bluetoothDevice = mock(BluetoothDevice.class);
        mConnectionManager.connect(bluetoothDevice, mock(ConnectionConfiguration.class),
                mConnectionAttemptCallback);
        assertFalse(mConnectionManager.connect(bluetoothDevice, mock(ConnectionConfiguration.class),
                mConnectionAttemptCallback));
    }

    @Test
    public void disconnectWhenTheDeviceIsBeingManagedShouldReturnTrue() {
        final BluetoothDevice bluetoothDevice = mock(BluetoothDevice.class);
        mConnectionManager.connect(bluetoothDevice, mock(ConnectionConfiguration.class),
                mConnectionAttemptCallback);
        assertTrue(mConnectionManager.disconnect(bluetoothDevice));
    }

    @Test
    public void disconnectWhenTheDeviceIsNotManagedShouldReturnFalse() {
        final BluetoothDevice bluetoothDevice = mock(BluetoothDevice.class);
        assertFalse(mConnectionManager.disconnect(bluetoothDevice));
    }
}
