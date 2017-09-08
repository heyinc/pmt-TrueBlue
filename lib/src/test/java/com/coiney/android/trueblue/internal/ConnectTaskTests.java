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

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.quality.Strictness;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/**
 * ConnectTask is very difficult to test as it has to deal with multiple
 * threads and makes extensive use of the Android Bluetooth subsystem (which
 * requires all kinds of mocking and stubbing of objects we don't own). The
 * recent addition of pairing management made it even more difficult to test,
 * and would suggest that there is a better way. The current tests block, so
 * they have been disabled until we decide how to go forward from here.
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
@Ignore
public class ConnectTaskTests {

    // Ensure that Mockito is used despite using Robolectric's test runner.
    @Rule
    public MockitoRule mMockitoRule = MockitoJUnit.rule().strictness(Strictness.WARN);

    @Mock private AdapterManager mAdapterManager;
    @Mock private BluetoothDevice mBluetoothDevice;
    @Mock private PairingMonitor mPairingMonitor;

//    @Test
//    public void runWhenConnectionSucceedsShouldReturnSuccessViaCallback() throws Exception {
//        when(mAdapterManager.isDiscoveryRunning()).thenReturn(false);
//        final BluetoothSocket bluetoothSocket = mock(BluetoothSocket.class);
//        when(mBluetoothDevice.createRfcommSocketToServiceRecord(any(UUID.class)))
//                .thenReturn(bluetoothSocket);
//        final ConnectTask.Callback callback = mock(ConnectTask.Callback.class);
//        final ConnectTask connectTask = createConnectTask(callback);
//        connectTask.run();
//        verify(callback, times(1)).onConnectionSucceeded(eq(connectTask), any(Connection.class));
//    }
//
//    @Test
//    public void runShouldAttemptConnectionUsingProvidedUuid() throws Exception {
//        when(mAdapterManager.isDiscoveryRunning()).thenReturn(false);
//        final BluetoothSocket bluetoothSocket = mock(BluetoothSocket.class);
//        when(mBluetoothDevice.createRfcommSocketToServiceRecord(any(UUID.class)))
//                .thenReturn(bluetoothSocket);
//        final UUID uuid = UUID.randomUUID();
//        createConnectTask(uuid).run();
//        verify(mBluetoothDevice, times(1)).createRfcommSocketToServiceRecord(uuid);
//    }
//
//    @Test
//    public void runWhenDiscoveryIsRunningShouldDelayUntilDiscoveryHasFinished() throws Exception {
//        // Simulate discovery finishing. Not ideal ...
//        final ConnectTask.Callback callback = mock(ConnectTask.Callback.class);
//        final ConnectTask connectTask = createConnectTask(callback);
//        when(mAdapterManager.isDiscoveryRunning()).thenAnswer(
//                new Answer<Object>() {
//                    private int callCount = 0;
//
//                    @Override
//                    public Object answer(InvocationOnMock invocation) throws Throwable {
//                        callCount++;
//                        if (callCount < 2) {
//                            verify(callback, times(0)).onConnectionSucceeded(eq(connectTask),
//                                    any(Connection.class));
//                            return true;
//                        }
//                        return false;
//                    }
//                });
//        when(mBluetoothDevice.createRfcommSocketToServiceRecord(any(UUID.class)))
//                .thenReturn(mock(BluetoothSocket.class));
//        connectTask.run();
//        verify(callback, times(1)).onConnectionSucceeded(eq(connectTask), any(Connection.class));
//    }
//
//    @Test
//    public void runWhenSocketCreationFailsAndNoRetriesRemainShouldReturnFailureViaCallback()
//            throws Exception {
//        when(mAdapterManager.isDiscoveryRunning()).thenReturn(false);
//        when(mBluetoothDevice.createRfcommSocketToServiceRecord(any(UUID.class)))
//                .thenThrow(new IOException());
//        final ConnectionRetryPolicy retryPolicy = new ConnectionRetryPolicy.Builder()
//                .setRetryCount(0).build();
//        final ConnectTask.Callback callback = mock(ConnectTask.Callback.class);
//        final ConnectTask connectTask = createConnectTask(retryPolicy, callback);
//        connectTask.run();
//        verify(callback, times(1)).onConnectionFailed(connectTask);
//    }
//
//    @Test
//    public void runWhenSocketCreationFailsAndRetryRemainsShouldRetry() throws Exception {
//        when(mAdapterManager.isDiscoveryRunning()).thenReturn(false);
//        when(mBluetoothDevice.createRfcommSocketToServiceRecord(any(UUID.class)))
//                .thenThrow(new IOException());
//        final ConnectionRetryPolicy retryPolicy = spy(new ConnectionRetryPolicy.Builder()
//                .setRetryCount(1).build());
//        final ConnectTask.Callback callback = mock(ConnectTask.Callback.class);
//        createConnectTask(retryPolicy, callback).run();
//        verify(retryPolicy, times(1)).retry();
//    }
//
//    @Test
//    public void runWhenSocketConnectionFailsAndNoRetriesRemainShouldReturnFailureViaCallback()
//            throws Exception {
//        when(mAdapterManager.isDiscoveryRunning()).thenReturn(false);
//        final BluetoothSocket bluetoothSocket = mock(BluetoothSocket.class);
//        when(mBluetoothDevice.createRfcommSocketToServiceRecord(any(UUID.class)))
//                .thenReturn(bluetoothSocket);
//        Mockito.doThrow(new IOException()).when(bluetoothSocket).connect();
//        final ConnectionRetryPolicy retryPolicy = new ConnectionRetryPolicy.Builder()
//                .setRetryCount(0).build();
//        final ConnectTask.Callback callback = mock(ConnectTask.Callback.class);
//        final ConnectTask connectTask = createConnectTask(retryPolicy, callback);
//        connectTask.run();
//        verify(callback, times(1)).onConnectionFailed(connectTask);
//    }
//
//    @Test
//    public void runWhenSocketConnectionFailsAndRetryRemainShouldRetry()
//            throws Exception {
//        when(mAdapterManager.isDiscoveryRunning()).thenReturn(false);
//        final BluetoothSocket bluetoothSocket = mock(BluetoothSocket.class);
//        when(mBluetoothDevice.createRfcommSocketToServiceRecord(any(UUID.class)))
//                .thenReturn(bluetoothSocket);
//        Mockito.doThrow(new IOException()).when(bluetoothSocket).connect();
//        final ConnectionRetryPolicy retryPolicy = spy(new ConnectionRetryPolicy.Builder()
//                .setRetryCount(1).build());
//        final ConnectTask.Callback callback = mock(ConnectTask.Callback.class);
//        createConnectTask(retryPolicy, callback).run();
//        verify(retryPolicy, times(1)).retry();
//    }
//
//    private ConnectTask createConnectTask(UUID uuid) {
//        return createConnectTask(uuid, mock(ConnectionRetryPolicy.class),
//                mock(ConnectTask.Callback.class));
//    }
//
//    private ConnectTask createConnectTask(ConnectTask.Callback callback) {
//        return createConnectTask(mock(ConnectionRetryPolicy.class), callback);
//    }
//
//    private ConnectTask createConnectTask(ConnectionRetryPolicy retryPolicy,
//            ConnectTask.Callback callback) {
//        return createConnectTask(UUID.randomUUID(), retryPolicy, callback);
//    }
//
//    private ConnectTask createConnectTask(UUID uuid, ConnectionRetryPolicy retryPolicy,
//            ConnectTask.Callback callback) {
//        return new ConnectTask(mBluetoothDevice, uuid, retryPolicy, mAdapterManager,
//                mPairingMonitor, callback, mock(Logger.class));
//    }
}
