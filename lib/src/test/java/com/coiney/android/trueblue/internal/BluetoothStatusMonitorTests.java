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

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;

import com.coiney.android.trueblue.BluetoothStatusListener;
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
import org.robolectric.annotation.Config;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class BluetoothStatusMonitorTests {

    // Ensure that Mockito is used despite using Robolectric's test runner.
    @Rule
    public MockitoRule mMockitoRule = MockitoJUnit.rule().strictness(Strictness.STRICT_STUBS);

    private Context mContext;
    private BluetoothStatusMonitor mBluetoothStatusMonitor;
    @Mock private BluetoothStatusListener mListener;

    @Before
    public void setUp() {
        mContext = RuntimeEnvironment.application.getApplicationContext();
        mBluetoothStatusMonitor = new BluetoothStatusMonitor(mContext);
    }

    @Test
    public void notificationWhileMonitorStartedButNoListenersRegisteredDoesNotCrash() throws Exception {
        mBluetoothStatusMonitor.start();
        fakeSystemBluetoothStatusChange(mContext, true);
    }

    @Test
    public void notificationWhileMonitorStoppedButNoListenersRegisteredDoesNotCrash() throws Exception {
        mBluetoothStatusMonitor.start();
        mBluetoothStatusMonitor.stop();
        fakeSystemBluetoothStatusChange(mContext, true);
    }

    @Test
    public void singleListenerRegisteredWhileMonitorStartedReceivesBluetoothEnabledNotification() {
        mBluetoothStatusMonitor.start();
        mBluetoothStatusMonitor.registerListener(mListener);
        fakeSystemBluetoothStatusChange(mContext, true);
        verifyEventsReceived(mListener, 1, 0);
    }

    @Test
    public void singleListenerRegisteredWhileMonitorStartedReceivesBluetoothDisabledNotification() {
        mBluetoothStatusMonitor.start();
        mBluetoothStatusMonitor.registerListener(mListener);
        fakeSystemBluetoothStatusChange(mContext, false);
        verifyEventsReceived(mListener, 0, 1);
    }

    @Test
    public void singleListenerRegisteredBeforeMonitorStartedReceivesBluetoothEnabledNotification() {
        mBluetoothStatusMonitor.registerListener(mListener);
        mBluetoothStatusMonitor.start();
        fakeSystemBluetoothStatusChange(mContext, true);
        verifyEventsReceived(mListener, 1, 0);
    }

    @Test
    public void singleListenerRegisteredBeforeMonitorStartedReceivesBluetoothDisabledNotification() {
        mBluetoothStatusMonitor.registerListener(mListener);
        mBluetoothStatusMonitor.start();
        fakeSystemBluetoothStatusChange(mContext, false);
        verifyEventsReceived(mListener, 0, 1);
    }

    @Test
    public void singleListenerRegisteredWhileMonitorStartedDoesNotReceiveBluetoothEnabledNotificationAfterMonitorStopped() {
        mBluetoothStatusMonitor.start();
        mBluetoothStatusMonitor.registerListener(mListener);
        mBluetoothStatusMonitor.stop();
        fakeSystemBluetoothStatusChange(mContext, true);
        verifyEventsReceived(mListener, 0, 0);
    }

    @Test
    public void singleListenerRegisteredWhileMonitorStartedDoesNotReceiveBluetoothDisabledNotificationAfterMonitorStopped() {
        mBluetoothStatusMonitor.start();
        mBluetoothStatusMonitor.registerListener(mListener);
        mBluetoothStatusMonitor.stop();
        fakeSystemBluetoothStatusChange(mContext, false);
        verifyEventsReceived(mListener, 0, 0);
    }

    @Test
    public void singleListenerRegisteredWhileMonitorStoppedDoesNotReceiveBluetoothEnabledNotification() {
        mBluetoothStatusMonitor.stop();
        mBluetoothStatusMonitor.registerListener(mListener);
        fakeSystemBluetoothStatusChange(mContext, true);
        verifyEventsReceived(mListener, 0, 0);
    }

    @Test
    public void singleListenerRegisteredWhileMonitorStoppedDoesNotReceiveBluetoothDisabledNotification() {
        mBluetoothStatusMonitor.registerListener(mListener);
        mBluetoothStatusMonitor.stop();
        fakeSystemBluetoothStatusChange(mContext, false);
        verifyEventsReceived(mListener, 0, 0);
    }

    @Test
    public void singleListenerRegisteredWhileMonitorStartedThenUnregisteredDoesNotReceiveBluetoothEnabledNotification() {
        mBluetoothStatusMonitor.start();
        mBluetoothStatusMonitor.registerListener(mListener);
        mBluetoothStatusMonitor.unregisterListener(mListener);
        fakeSystemBluetoothStatusChange(mContext, true);
        verifyEventsReceived(mListener, 0, 0);
    }

    @Test
    public void singleListenerRegisteredWhileMonitorStartedThenUnregisteredDoesNotReceiveBluetoothDisabledNotification() {
        mBluetoothStatusMonitor.start();
        mBluetoothStatusMonitor.registerListener(mListener);
        mBluetoothStatusMonitor.unregisterListener(mListener);
        fakeSystemBluetoothStatusChange(mContext, false);
        verifyEventsReceived(mListener, 0, 0);
    }

    @Test
    public void stoppingMonitorClearsListeners() {
        final BluetoothStatusListener listener = mock(BluetoothStatusListener.class);
        mBluetoothStatusMonitor.start();
        mBluetoothStatusMonitor.registerListener(listener);
        mBluetoothStatusMonitor.stop();
        mBluetoothStatusMonitor.start();
        fakeSystemBluetoothStatusChange(mContext, true);
        verifyEventsReceived(mListener, 0, 0);
    }

    private void fakeSystemBluetoothStatusChange(Context context, boolean isEnabled) {
        context.sendBroadcast(new Intent(BluetoothAdapter.ACTION_STATE_CHANGED)
                .putExtra(BluetoothAdapter.EXTRA_STATE, isEnabled ? BluetoothAdapter.STATE_ON :
                         BluetoothAdapter.STATE_OFF));
    }

    private void verifyEventsReceived(BluetoothStatusListener listener, int enabledCount,
            int disabledCount) {
        verify(listener, times(enabledCount)).onBluetoothEnabled();
        verify(listener, times(disabledCount)).onBluetoothDisabled();
    }
}
