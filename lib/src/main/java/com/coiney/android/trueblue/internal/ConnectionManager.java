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

import android.bluetooth.BluetoothDevice;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.coiney.android.trueblue.ConnectionAttemptCallback;
import com.coiney.android.trueblue.DeviceConnectionListener;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;

/**
 * Manages connections to Bluetooth devices.
 */
@ThreadSafe
public final class ConnectionManager implements ConnectionProxy.Listener {

    private final AdapterManager mAdapterManager;
    private final ExecutorService mConnectTaskExecutor;
    private final Set<DeviceConnectionListener> mDeviceConnectionListeners =
            new CopyOnWriteArraySet<>();
    private final Logger mLogger;
    @GuardedBy("mManagedConnectionsLock")
    private final Map<BluetoothDevice, ConnectionProxy> mManagedConnections = new HashMap<>();
    private final Object mManagedConnectionsLock = new Object();
    private final PairingMonitor mPairingMonitor;

    /**
     * Create a connection manager with the provided parameters.
     *
     * @param adapterManager to use when attempting connections.
     * @param pairingMonitor to use when attempting connections.
     * @param connectTaskExecutor to execute connection tasks on.
     * @param logger to log to.
     */
    public ConnectionManager(@NonNull AdapterManager adapterManager,
            @NonNull PairingMonitor pairingMonitor, @NonNull ExecutorService connectTaskExecutor,
            @NonNull Logger logger) {
        mAdapterManager = adapterManager;
        mConnectTaskExecutor = connectTaskExecutor;
        mLogger = logger;
        mPairingMonitor = pairingMonitor;
    }

    /**
     * Ask whether the provided device is connected or not.
     *
     * @param device to check the connectivity of.
     *
     * @return flag indicating whether the device is connected or not.
     */
    public boolean isConnected(@NonNull BluetoothDevice device) {
        final ConnectionProxy connectionProxy = getConnectionProxy(device);
        return connectionProxy != null && connectionProxy.isConnected();
    }

    /**
     * Ask whether the provided device is connected or being connected to, or
     * not.
     *
     * @param device to check the connectivity of.
     *
     * @return flag indicating whether the device is connected or being
     *         connected to, or not.
     */
    public boolean isConnectedOrConnecting(@NonNull BluetoothDevice device) {
        return getConnectionProxy(device) != null;
    }

    /**
     * Attempt to connect to the provided Bluetooth device with the provided
     * connection configuration. The connection attempt takes place
     * asynchronously, with progress and results reported via the provided
     * callback.
     *
     * This method returns a flag indicating whether the connection attempt
     * was able to be successfully started or not. A return value of false
     * generally indicates that the device is already connected or being
     * connected to.
     *
     * @param device to attempt connection to.
     * @param connectionConfiguration to use when attempting to connect.
     * @param callback to report progress and results to.
     *
     * @return flag indicating whether the connection attempt was successfully
     *         started or not.
     */
    public boolean connect(@NonNull BluetoothDevice device,
            @NonNull ConnectionConfiguration connectionConfiguration,
            @Nullable ConnectionAttemptCallback callback) {
        synchronized (mManagedConnectionsLock) {
            if (mManagedConnections.containsKey(device)) {
                mLogger.d(device, "Cannot attempt connection - already connected or connection" +
                        "in progress.");
                return false;
            }
            final ConnectionProxy connectionProxy = new ConnectionProxy(device, callback, this);
            final ConnectTask connectTask = new ConnectTask(device, connectionConfiguration,
                    mAdapterManager, mPairingMonitor, connectionProxy, mLogger);
            mManagedConnections.put(device, connectionProxy);
            mLogger.d(device, "Starting asynchronous connection attempt.");
            connectionProxy.connect(connectTask, mConnectTaskExecutor);
            return true;
        }
    }

    /**
     * Attempt to disconnect from the provided Bluetooth device. The
     * disconnection attempt may be processed asynchronously.
     *
     * This method returns a flag indicating whether the disconnection
     * attempt was able to be successfully started or not. A return value of
     * false generally indicates that the device is not being managed
     * (be it as a connected device or a device being connected to).
     *
     * To determine exactly when the device is disconnected, register a
     * {@link DeviceConnectionListener} using {@link
     * #registerDeviceConnectionListener(DeviceConnectionListener)}.
     *
     * @param device to attempt to disconnect.
     *
     * @return flag indicating whether the disconnection attempt was
     *         successfully started or not.
     */
    public boolean disconnect(@NonNull BluetoothDevice device) {
        final ConnectionProxy connectionProxy = getConnectionProxy(device);
        if (null == connectionProxy) {
            mLogger.d(device, "Cannot disconnect - connection is not being managed.");
            return false;
        }
        mLogger.d(device, "Disconnecting.");
        connectionProxy.disconnect();
        return true;
    }

    /**
     * Attempt to disconnect from all Bluetooth devices which are either
     * connected or being connected to. Note that this is a point in time
     * process, and will not prevent any connection attempts started after the
     * method returns.
     */
    public void disconnectAll() {
        if (!mAdapterManager.isAdapterEnabled()) {
            mLogger.d("Cannot disconnect all devices - Bluetooth is disabled.");
            return;
        }
        final Set<BluetoothDevice> managedDevicesCopy;
        synchronized (mManagedConnectionsLock) {
            managedDevicesCopy = new HashSet<>(mManagedConnections.keySet());
        }
        for (BluetoothDevice device : managedDevicesCopy) {
            disconnect(device);
        }
    }

    /**
     * Register a listener for device connection events. Be sure to unregister
     * using {@link #unregisterDeviceConnectionListener(DeviceConnectionListener)}
     * when the events are no longer required.
     *
     * @param listener to register.
     */
    public void registerDeviceConnectionListener(@NonNull DeviceConnectionListener listener) {
        mDeviceConnectionListeners.add(listener);
    }

    /**
     * Unregister a previously registered device connection listener.
     *
     * @param listener to unregister.
     */
    public void unregisterDeviceConnectionListener(@NonNull DeviceConnectionListener listener) {
        mDeviceConnectionListeners.remove(listener);
    }

    @Override
    public void onConnectionAttemptCancelled(@NonNull ConnectionProxy connectionProxy) {
        mLogger.d(connectionProxy.getDevice(), "Connection attempt cancelled - purging " +
                "management data.");
        purgeConnectionProxy(connectionProxy);
    }

    @Override
    public void onConnectionClosed(@NonNull ConnectionProxy connectionProxy,
            final boolean wasClosedByError) {
        mLogger.d(connectionProxy.getDevice(), "Connection " + (wasClosedByError ?
                "terminated" : "closed") + ".");
        purgeConnectionProxy(connectionProxy);
        final BluetoothDevice device = connectionProxy.getDevice();
        ThreadUtils.postOnMainThread(new Runnable() {
            @Override
            public void run() {
                for (DeviceConnectionListener listener : mDeviceConnectionListeners) {
                    listener.onDeviceDisconnected(device, wasClosedByError);
                }
            }
        });
    }

    @Override
    public void onConnectionAttemptFailed(@NonNull ConnectionProxy connectionProxy) {
        mLogger.d(connectionProxy.getDevice(), "Connection attempt failed - purging " +
                "management data.");
        purgeConnectionProxy(connectionProxy);
    }

    @Override
    public void onConnectionAttemptSucceeded(@NonNull ConnectionProxy connectionProxy) {
        mLogger.d(connectionProxy.getDevice(), "Connection attempt succeeded.");
        final BluetoothDevice device = connectionProxy.getDevice();
        ThreadUtils.postOnMainThread(new Runnable() {
            @Override
            public void run() {
                for (DeviceConnectionListener listener : mDeviceConnectionListeners) {
                    listener.onDeviceConnected(device);
                }
            }
        });
    }

    private void purgeConnectionProxy(ConnectionProxy connectionProxy) {
        synchronized (mManagedConnectionsLock) {
            mManagedConnections.remove(connectionProxy.getDevice());
        }
    }

    @Nullable
    private ConnectionProxy getConnectionProxy(BluetoothDevice device) {
        synchronized (mManagedConnectionsLock) {
            return mManagedConnections.get(device);
        }
    }
}
