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

/* The internal package of this library contains code which is purely for use
 * within the library. The entire package is subject to change at any time with
 * no notice, and should therefore never be used directly from outside the
 * library.
 */

package com.coiney.android.trueblue.internal;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.coiney.android.trueblue.BluetoothStatusListener;

import net.jcip.annotations.ThreadSafe;

import java.util.Collections;
import java.util.Set;

/**
 * Simple wrapper class around a {@link BluetoothAdapter} instance which
 * simplifies interactions with the Bluetooth subsystem.
 */
@ThreadSafe
public class AdapterManager {

    private final BluetoothAdapter mAdapter;
    private final Logger mLogger;
    private final BluetoothStatusMonitor mStatusMonitor;

    /**
     * Create a Bluetooth adapter manager with the provided parameters.
     *
     * @param adapter to manage.
     * @param statusMonitor with which to monitor adapter status changes.
     * @param logger to log to.
     */
    public AdapterManager(@NonNull BluetoothAdapter adapter,
            @NonNull BluetoothStatusMonitor statusMonitor, @NonNull Logger logger) {
        mAdapter = adapter;
        mLogger = logger;
        mStatusMonitor = statusMonitor;
    }

    /**
     * Start the adapter manager. This is required primarily to commence
     * monitoring and reporting on the status of Bluetooth at the system level
     * (enabled or disabled).
     */
    public void start() {
        mStatusMonitor.start();
    }

    /**
     * Obtain the list of devices paired with the system. Returns an empty set
     * rather than null if there are no paired devices.
     *
     * @return the list of devices paired with the system.
     */
    @NonNull
    public Set<BluetoothDevice> getDeviceList() {
        final Set<BluetoothDevice> devices = mAdapter.getBondedDevices();
        if (devices == null) {
            return Collections.emptySet();
        }
        return devices;
    }

    /**
     * Ask whether the system's Bluetooth adapter is enabled or not.
     *
     * @return flag indicating whether the system's Bluetooth adapter is
     *         enabled or not.
     */
    public boolean isAdapterEnabled() {
        return mAdapter.isEnabled();
    }

    /**
     * Ask whether a discovery scan is running at the system level or not.
     *
     * @return flag indicating whether a discovery scan is running at the
     *         system level or not.
     */
    public boolean isDiscoveryRunning() {
        return mAdapter.isDiscovering();
    }

    /**
     * Ask the user to enable Bluetooth.
     *
     * @param activity to use to launch the request.
     */
    public void requestBluetoothBeEnabled(@NonNull Activity activity) {
        if (mAdapter.isEnabled()) {
            mLogger.i("Cannot request that Bluetooth be enabled - it is already enabled.");
            return;
        }
        activity.startActivity(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE));
    }

    /**
     * Ask the user to enable Bluetooth.
     *
     * @param activity to use to launch the request.
     * @param requestCode to use when returning the resulting activity
     *                    result.
     */
    public void requestBluetoothBeEnabled(@NonNull Activity activity, int requestCode) {
        if (mAdapter.isEnabled()) {
            mLogger.i("Cannot request that Bluetooth be enabled - it is already enabled.");
            return;
        }
        activity.startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE),
                requestCode);
    }

    /**
     * Register a Bluetooth status listener to obtain status change events. Be
     * sure to call {@link
     * #unregisterBluetoothStatusListener(BluetoothStatusListener)} when the
     * listener is no longer required.
     *
     * @param listener to register.
     */
    public void registerBluetoothStatusListener(@NonNull BluetoothStatusListener listener) {
        mStatusMonitor.registerListener(listener);
    }

    /**
     * Unregister a previously registered Bluetooth status listener.
     *
     * @param listener to unregister.
     */
    public void unregisterBluetoothStatusListener(@NonNull BluetoothStatusListener listener) {
        mStatusMonitor.unregisterListener(listener);
    }

    /**
     * Attempt to start a system level discovery scan, returning the result.
     *
     * @return flag indicating whether discovery was successfully started or
     *         not.
     */
    boolean startDiscovery() {
        return mAdapter.startDiscovery();
    }

    /**
     * Attempt to stop a system level discovery scan, returning the result.
     *
     * @return flag indicating whether discovery was successfully stopped or
     *         not.
     */
    boolean stopDiscovery() {
        return mAdapter.cancelDiscovery();
    }
}
