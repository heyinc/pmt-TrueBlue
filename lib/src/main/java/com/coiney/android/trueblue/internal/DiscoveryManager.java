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

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;

import com.coiney.android.trueblue.DiscoveryError;
import com.coiney.android.trueblue.DiscoveryListener;

import net.jcip.annotations.ThreadSafe;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Convenience wrapper around the discovery subsystem provided by a Bluetooth
 * adapter managed by an {@link AdapterManager}.
 */
@ThreadSafe
public final class DiscoveryManager extends BroadcastReceiver {

    private final AdapterManager mAdapterManager;
    private final Context mContext;
    private boolean mIsScanning;
    private final Set<DiscoveryListener> mListeners = new CopyOnWriteArraySet<>();
    private final Logger mLogger;

    /**
     * Create a discovery manager with the provided parameters.
     *
     * @param adapterManager with which to manage system level discovery scans.
     * @param context with which to access discovery related system broadcasts.
     * @param logger to log to.
     */
    public DiscoveryManager(@NonNull AdapterManager adapterManager, @NonNull Context context,
            @NonNull Logger logger) {
        mAdapterManager = adapterManager;
        mContext = context.getApplicationContext();
        mLogger = logger;
    }

    /**
     * Start the discovery manager. The primary purpose of this method is to
     * start monitoring for discovery related system broadcasts.
     */
    public void start() {
        startMonitoring();
    }

    /**
     * Ask whether a discovery scan started by this discovery manager is
     * currently running or not.
     *
     * @return flag indicating whether a discovery scan started by this
     *         discovery manager is currently running or not.
     */
    public synchronized boolean isDiscoveryRunning() {
        return mIsScanning;
    }

    /**
     * Attempt to start a discovery scan. The scan is performed asynchronously
     * with progress and results reported via {@link DiscoveryListener}, which
     * can be registered using {@link #registerListener(DiscoveryListener)}.
     *
     * This method returns either null (in the event the discovery scan starts
     * successfully) or a {@link DiscoveryError} value explaining the reason
     * the scan failed to start.
     *
     * @return discovery error which prevented the scan starting, or null.
     */
    @Nullable
    public synchronized DiscoveryError startDiscovery() {
        if (mIsScanning) {
            mLogger.d("Cannot startDiscovery discovery scan - a scan is already running.");
            return DiscoveryError.ALREADY_RUNNING;
        }
        if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(mContext,
                Manifest.permission.ACCESS_COARSE_LOCATION)) {
            mLogger.d("Requesting coarse location access permission from user in order to " +
                    "perform Bluetooth discovery scan.");
            return DiscoveryError.COARSE_LOCATION_PERMISSION_REQUIRED;
        }
        if (!mAdapterManager.isAdapterEnabled()) {
            mLogger.d("Cannot startDiscovery discovery scan - Bluetooth is not enabled.");
            return DiscoveryError.BLUETOOTH_DISABLED;
        }
        if (mAdapterManager.isDiscoveryRunning()) {
            mLogger.d("Cannot startDiscovery discovery scan - one has already been started " +
                    "outside of this service.");
            return DiscoveryError.SYSTEM_ERROR;
        }
        mLogger.d("Starting discovery scan.");
        mIsScanning = mAdapterManager.startDiscovery();
        if (!mIsScanning) {
            return DiscoveryError.SYSTEM_ERROR;
        }
        return null;
    }

    /**
     * Attempt to stop a discovery scan started by this service. The attempt is
     * performed asynchronously - register a {@link DiscoveryListener} to
     * determine exactly when the scan has stopped.
     *
     * This method returns true if the attempt to stop a running discovery scan
     * was successfully started, or false otherwise. A false return value
     * generally indicates that a discovery scan is not running.
     *
     * @return flag indicating whether the attempt to stop a running discovery
     *         scan started successfully or not.
     */
    public synchronized boolean stopDiscovery() {
        // Avoid cancelling discovery scans started outside this service.
        if (!mIsScanning) {
            mLogger.d("Cannot stop discovery scan - not running.");
            return false;
        }
        mLogger.d("Stopping discovery scan.");
        return mAdapterManager.stopDiscovery();
    }

    /**
     * Register a listener for discovery related events. Remember to unregister
     * using {@link #unregisterListener(DiscoveryListener)} when these events
     * are no longer required.
     *
     * @param listener to register.
     */
    public void registerListener(@NonNull DiscoveryListener listener) {
        mListeners.add(listener);
    }

    /**
     * Unregister a previously registered listener.
     *
     * @param listener to unregister.
     */
    public void unregisterListener(@NonNull DiscoveryListener listener) {
        mListeners.remove(listener);
    }

    @Override
    public synchronized void onReceive(Context context, Intent intent) {
        // Avoid reporting events for discovery scans started outside
        // this service.
        if (!mIsScanning) {
            return;
        }
        switch (intent.getAction()) {
            case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                handleDiscoveryStarted();
                break;
            case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                handleDiscoveryFinished();
                break;
            case BluetoothDevice.ACTION_FOUND:
                handleDeviceDiscovered((BluetoothDevice) intent.getParcelableExtra(
                        BluetoothDevice.EXTRA_DEVICE));
                break;
            default:
                break;
        }
    }

    private void startMonitoring() {
        stopMonitoring();
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        mContext.registerReceiver(this, intentFilter);
    }

    private void stopMonitoring() {
        ContextUtils.unregisterReceiverSilently(mContext, this);
    }

    private void handleDiscoveryStarted() {
        mLogger.d("Discovery scan started.");
        for (DiscoveryListener listener : mListeners) {
            listener.onDiscoveryStarted();
        }
    }

    private void handleDeviceDiscovered(BluetoothDevice device) {
        final String deviceName = (device.getName() != null) ? device.getName() : "<Unknown>";
        mLogger.d("Discovered device with name: '" + deviceName + "'.");
        for (DiscoveryListener listener : mListeners) {
            listener.onDeviceDiscovered(device);
        }
    }

    private void handleDiscoveryFinished() {
        mLogger.d("Discovery scan finished.");
        for (DiscoveryListener listener : mListeners) {
            listener.onDiscoveryFinished();
        }
        mIsScanning = false;
    }
}
