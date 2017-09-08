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
import com.coiney.android.trueblue.Connection;

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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class ConnectionProxyTests {

    // Ensure that Mockito is used despite using Robolectric's test runner.
    @Rule
    public MockitoRule mMockitoRule = MockitoJUnit.rule().strictness(Strictness.WARN);

    @Mock private BluetoothDevice mBluetoothDevice;
    @Mock private ConnectionAttemptCallback mConnectionAttemptCallback;
    @Mock private ConnectionProxy.Listener mListener;
    @Mock private ConnectTask mConnectTask;

    private ConnectionProxy mConnectionProxy;
    private ExecutorService mExecutorService = Executors.newSingleThreadExecutor();

    @Before
    public void setUp() {
        mConnectionProxy = new ConnectionProxy(mBluetoothDevice, mConnectionAttemptCallback,
                mListener);
    }

    @Test
    public void getBluetoothDeviceShouldReturnBluetoothDevice() {
        assertEquals(mBluetoothDevice, mConnectionProxy.getDevice());
    }

    @Test
    public void isConnectedWhenConnectedShouldReturnTrue() {
        mConnectionProxy.connect(mConnectTask, mExecutorService);
        final Connection connection = mock(Connection.class);
        when(connection.isOpen()).thenReturn(true);
        mConnectionProxy.onConnectionSucceeded(mConnectTask, connection);
        assertTrue(mConnectionProxy.isConnected());
    }

    @Test
    public void isConnectedWhenBeforeConnectionAttemptShouldReturnFalse() {
        assertFalse(mConnectionProxy.isConnected());
    }

    @Test
    public void isConnectedWhenConnectingShouldReturnFalse() {
        mConnectionProxy.connect(mConnectTask, mExecutorService);
        assertFalse(mConnectionProxy.isConnected());
    }

    @Test
    public void isConnectedWhenDisconnectedShouldReturnFalse() {
        mConnectionProxy.connect(mConnectTask, mExecutorService);
        final Connection connection = mock(Connection.class);
        mConnectionProxy.onConnectionSucceeded(mConnectTask, connection);
        when(connection.isOpen()).thenReturn(false);
        assertFalse(mConnectionProxy.isConnected());
    }

    @Test
    public void successfulConnectionAttemptShouldInformCallback() {
        mConnectionProxy.connect(mConnectTask, mExecutorService);
        final Connection connection = mock(Connection.class);
        when(connection.isOpen()).thenReturn(true);
        mConnectionProxy.onConnectionSucceeded(mConnectTask, connection);
        verify(mConnectionAttemptCallback, times(1)).onConnectionAttemptSucceeded(mBluetoothDevice,
                connection);
    }

    @Test
    public void successfulConnectionAttemptShouldInformListener() {
        mConnectionProxy.connect(mConnectTask, mExecutorService);
        final Connection connection = mock(Connection.class);
        when(connection.isOpen()).thenReturn(true);
        mConnectionProxy.onConnectionSucceeded(mConnectTask, connection);
        verify(mListener, times(1)).onConnectionAttemptSucceeded(mConnectionProxy);
    }

    @Test
    public void failedConnectionAttemptShouldInformCallback() {
        mConnectionProxy.connect(mConnectTask, mExecutorService);
        mConnectionProxy.onConnectionFailed(mConnectTask);
        verify(mConnectionAttemptCallback, times(1)).onConnectionAttemptFailed(mBluetoothDevice);
    }

    @Test
    public void failedConnectionAttemptShouldInformListener() {
        mConnectionProxy.connect(mConnectTask, mExecutorService);
        final Connection connection = mock(Connection.class);
        when(connection.isOpen()).thenReturn(true);
        mConnectionProxy.onConnectionFailed(mConnectTask);
        verify(mListener, times(1)).onConnectionAttemptFailed(mConnectionProxy);
    }

    @Test
    public void cancelledConnectionAttemptShouldInformCallback() {
        mConnectionProxy.connect(mConnectTask, mExecutorService);
        mConnectionProxy.onConnectionCancelled(mConnectTask);
        verify(mConnectionAttemptCallback, times(1)).onConnectionAttemptCancelled(mBluetoothDevice);
    }

    @Test
    public void cancelledConnectionAttemptShouldInformListener() {
        mConnectionProxy.connect(mConnectTask, mExecutorService);
        final Connection connection = mock(Connection.class);
        when(connection.isOpen()).thenReturn(true);
        mConnectionProxy.onConnectionCancelled(mConnectTask);
        verify(mListener, times(1)).onConnectionAttemptCancelled(mConnectionProxy);
    }

    @Test
    public void connectionClosureShouldInformListener() {
        mConnectionProxy.connect(mConnectTask, mExecutorService);
        final Connection connection = mock(Connection.class);
        when(connection.isOpen()).thenReturn(true);
        mConnectionProxy.onConnectionSucceeded(mConnectTask, connection);
        mConnectionProxy.onConnectionClosed(connection, false);
        verify(mListener, times(1)).onConnectionClosed(mConnectionProxy, false);
    }

    @Test
    public void connectionTerminationShouldInformListener() {
        mConnectionProxy.connect(mConnectTask, mExecutorService);
        final Connection connection = mock(Connection.class);
        when(connection.isOpen()).thenReturn(true);
        mConnectionProxy.onConnectionSucceeded(mConnectTask, connection);
        mConnectionProxy.onConnectionClosed(connection, true);
        verify(mListener, times(1)).onConnectionClosed(mConnectionProxy, true);
    }
}
