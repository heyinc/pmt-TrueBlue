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

package com.coiney.android.trueblue;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.AnyThread;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.coiney.android.trueblue.internal.AdapterManager;
import com.coiney.android.trueblue.internal.BluetoothCompat;
import com.coiney.android.trueblue.internal.BluetoothStatusMonitor;
import com.coiney.android.trueblue.internal.ConnectionManager;
import com.coiney.android.trueblue.internal.DiscoveryManager;
import com.coiney.android.trueblue.internal.Logger;
import com.coiney.android.trueblue.internal.PairingMonitor;

import net.jcip.annotations.ThreadSafe;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.Executors;

/**
 * <p>
 * High level "all-in-one" Bluetooth service designed to simplify Bluetooth
 * operations on Android.
 * </p>
 *
 * <p>
 * Features include Bluetooth subsystem management, device connection
 * management and discovery scanning.
 * </p>
 *
 * <p>
 * This service manages its own instances. To get started, initialize the
 * service with an Android {@link Context}. It is safe to use an {@link
 * android.app.Activity} context here, but an {@link android.app.Application}
 * is recommended. Once initialized, it is possible to obtain an instance of
 * the service wherever required. See below for example code:
 * </p>
 *
 * <pre>
 * {@code
 * // During application/library initialization:
 * TrueBlue.initialize(context);
 *
 * // When an instance is required:
 * TrueBlue.getInstance();
 * }
 * </pre>
 *
 * <p>
 * Note that it is always possible to initialize and obtain an instance of the
 * service, even when Bluetooth is not available. This is a deliberate design
 * choice. The service will log a warning if Bluetooth is not available when it
 * is initialized. Basic query features such as determining whether Bluetooth
 * is available will, naturally, function as expected. All other operations,
 * however, will simply be no-ops.
 * </p>
 */
@ThreadSafe
public final class TrueBlue {

    private static final String DEFAULT_LOG_TAG = "TrueBlue";

    private static TrueBlue sInstance;

    private final AdapterManager mAdapterManager;
    private final ConnectionManager mConnectionManager;
    private final DiscoveryManager mDiscoveryManager;

    private TrueBlue(@Nullable AdapterManager adapterManager,
            @Nullable ConnectionManager connectionManager,
            @Nullable DiscoveryManager discoveryManager) {
        mAdapterManager = adapterManager;
        mConnectionManager = connectionManager;
        mDiscoveryManager = discoveryManager;
    }

    /**
     * <p>
     * Initialize the service with a context prior to use.
     * </p>
     *
     * <p>
     * This method returns an instance for the sake of convenience. To obtain
     * an instance after this, call {@link #getInstance()}.
     * </p>
     *
     * <p>
     * This method will always succeed, even if Bluetooth is not available on
     * the device. In this case the resulting service instance accessible via
     * {@link #getInstance()} will essentially be a null service.
     * </p>
     *
     * <p>
     * This method can be called multiple times, but under most circumstances
     * should only be called once.
     * </p>
     *
     * <p>
     * This method should only be called on the main (UI) thread.
     * </p>
     *
     * @param context to use to initialize the service.
     *
     * @return service instance.
     */
    @MainThread
    public static synchronized TrueBlue init(@NonNull Context context) {
        return init(context, DEFAULT_LOG_TAG);
    }

    /**
     * <p>
     * As per {@link #init(Context)}, but allows the setting of the tag
     * to use when logging.
     * </p>
     *
     * <p>
     * This method should only be called on the main (UI) thread.
     * </p>
     *
     * @param context to use to initialize the service.
     * @param logTag to use when logging.
     *
     * @return service instance.
     */
    @MainThread
    public static synchronized TrueBlue init(@NonNull Context context,
            @NonNull String logTag) {
        final Logger logger = new Logger(logTag);
        logger.d("Starting TrueBlue v" + BuildConfig.VERSION_NAME + ".");
        final Context applicationContext = context.getApplicationContext();
        final BluetoothAdapter adapter = BluetoothCompat.getBluetoothAdapter(applicationContext);
        final AdapterManager adapterManager;
        final ConnectionManager connectionManager;
        final DiscoveryManager discoveryManager;
        if (null == adapter) {
            logger.w("Bluetooth is not supported on this device - all service operations are " +
                    "no-ops.");
            adapterManager = null;
            connectionManager = null;
            discoveryManager = null;
        } else {
            final BluetoothStatusMonitor bluetoothStatusMonitor =
                    new BluetoothStatusMonitor(applicationContext);
            adapterManager = new AdapterManager(adapter, bluetoothStatusMonitor, logger);
            final PairingMonitor pairingMonitor = new PairingMonitor(applicationContext);
            connectionManager = new ConnectionManager(adapterManager, pairingMonitor,
                    Executors.newCachedThreadPool(), logger);
            discoveryManager = new DiscoveryManager(adapterManager, applicationContext, logger);
            adapterManager.start();
            discoveryManager.start();
            pairingMonitor.start();
        }
        sInstance = new TrueBlue(adapterManager, connectionManager, discoveryManager);
        return sInstance;
    }

    /**
     * <p>
     * Obtain a previously initialized instance of the service.
     * </p>
     *
     * <p>
     * Note that {@link #init(Context)} must have been called prior to
     * this method or an exception will be thrown.
     * </p>
     *
     * @return service instance.
     */
    @AnyThread
    @NonNull
    public static synchronized TrueBlue getInstance() {
        if (null == sInstance) {
            throw new IllegalStateException("Must be initialized first.");
        }
        return sInstance;
    }

    // Bluetooth adapter management

    /**
     * <p>
     * Ask whether Bluetooth is available on the device.
     * </p>
     *
     * <p>
     * This method can be called before initializing and obtaining an instance
     * of the service. See {@link #isBluetoothAvailable()} for the instance
     * version of this method.
     * </p>
     *
     * @return flag indicating whether Bluetooth is available or not.
     */
    @AnyThread
    public static boolean isBluetoothAvailable(@NonNull Context context) {
        return BluetoothCompat.getBluetoothAdapter(context) != null;
    }

    /**
     * <p>
     * Ask whether Bluetooth is currently enabled at the system level.
     * </p>
     *
     * <p>
     * If you wish to be notified upon changes in the status of the system's
     * Bluetooth subsystem, register a {@link BluetoothStatusListener} using
     * {@link #registerBluetoothStatusListener(BluetoothStatusListener)}.
     * </p>
     *
     * @return flag indicating whether Bluetooth is enabled or not.
     */
    @AnyThread
    public boolean isBluetoothEnabled() {
        return mAdapterManager != null && mAdapterManager.isAdapterEnabled();
    }

    /**
     * Ask whether Bluetooth is available on the device.
     *
     * @return flag indicating whether Bluetooth is available or not.
     */
    @AnyThread
    public boolean isBluetoothAvailable() {
        return mAdapterManager != null;
    }

    /**
     * <p>
     * Ask whether a discovery scan started by this service is running.
     * </p>
     *
     * <p>
     * Be aware that this only reflects the status of discovery scans started
     * by this service, as opposed to at the system level (i.e. by other
     * applications or services).
     * </p>
     *
     * <p>
     * If you wish to determine whether a discovery scan is running at the
     * system level regardless of whether it was started by this service or
     * not, call {@link #isSystemDiscoveryScanRunning()}.
     * </p>
     *
     * @return flag indicating whether a discovery scan started by this service
     *         is running or not.
     */
    @AnyThread
    public boolean isServiceDiscoveryScanRunning() {
        return mDiscoveryManager != null && mDiscoveryManager.isDiscoveryRunning();
    }

    /**
     * <p>
     * Ask whether the system is running a discovery scan.
     * </p>
     *
     * <p>
     * Be aware that this reflects the status of the system discovery scan
     * subsystem regardless of whether a running scan was started by this
     * service or another component.
     * </p>
     *
     * <p>
     * If you wish only to determine whether this service is running a
     * discovery scan, call {@link #isServiceDiscoveryScanRunning()}.
     * </p>
     *
     * @return flag indicating whether the system is running a discovery scan
     *         or not.
     */
    @AnyThread
    public boolean isSystemDiscoveryScanRunning() {
        return mAdapterManager != null && mAdapterManager.isDiscoveryRunning();
    }

    /**
     * <p>
     * Make a request to the user to enable Bluetooth using the provided
     * activity, ignoring the result.
     * </p>
     *
     * <p>
     * Register a {@link BluetoothStatusListener} (see {@link
     * #registerBluetoothStatusListener(BluetoothStatusListener)} to be
     * informed of changes in the status of the Bluetooth subsystem. Be aware
     * that this listener will not provide feedback in the event that the user
     * refuses to enable Bluetooth. For this, use {@link
     * #requestBluetoothBeEnabled(Activity, int)} instead and handle the
     * result.
     * </p>
     *
     * <p>
     * This method should only be called on the main (UI) thread.
     * </p>
     *
     * @param activity from which to make the request.
     */
    @MainThread
    public void requestBluetoothBeEnabled(@NonNull Activity activity) {
        if (mAdapterManager != null) {
            mAdapterManager.requestBluetoothBeEnabled(activity);
        }
    }

    /**
     * <p>
     * Make a request to the user to enable Bluetooth using the provided
     * activity, returning the result to the provided activity.
     * </p>
     *
     * <p>
     * The result of the request will be communicated via
     * {@link Activity#onActivityResult(int, int, Intent)} with the provided
     * status code.
     * </p>
     *
     * <p>
     * Be aware that a success result via {@link
     * Activity#onActivityResult(int, int, Intent)} does not indicate that the
     * system has actually enabled Bluetooth. If this distinction is important
     * for your use case, register a {@link BluetoothStatusListener} (see
     * {@link #registerBluetoothStatusListener(BluetoothStatusListener)} to be
     * informed of changes in the status of the Bluetooth subsystem.
     * </p>
     *
     * <p>
     * This method should only be called on the main (UI) thread.
     * </p>
     *
     * @param activity from which to make the request.
     * @param requestCode with which to make the request.
     */
    @MainThread
    public void requestBluetoothBeEnabled(@NonNull Activity activity, int requestCode) {
        if (mAdapterManager != null) {
            mAdapterManager.requestBluetoothBeEnabled(activity, requestCode);
        }
    }
    /**
     * Obtain the set of Bluetooth devices currently paired with the system.
     *
     * @return set of paired Bluetooth devices.
     */
    @AnyThread
    @NonNull
    public Set<BluetoothDevice> getDeviceList() {
        if (mAdapterManager != null) {
            return mAdapterManager.getDeviceList();
        } else {
            return Collections.emptySet();
        }
    }

    // Device connection management

    /**
     * Ask whether the provided Bluetooth device is connected or not.
     *
     * @param device to check.
     *
     * @return flag indicating whether the device is connected or not.
     */
    @AnyThread
    public boolean isConnected(BluetoothDevice device) {
        return mConnectionManager != null && mConnectionManager.isConnected(device);
    }

    /**
     * Ask whether the provided Bluetooth device is connected or being
     * connected to, or not.
     *
     * @param device to check.
     *
     * @return flag indicating whether the device is connected or being
     *         connected to, or not.
     */
    @AnyThread
    public boolean isConnectedOrConnecting(BluetoothDevice device) {
        return mConnectionManager != null && mConnectionManager.isConnectedOrConnecting(device);
    }

    /**
     * <p>
     * Attempt a Bluetooth connection using the provided configuration.
     * </p>
     *
     * <p>
     * Attempting to connect to a device which hasn't been paired with will
     * result in a pairing request dialog being displayed to the user. If the
     * pairing succeeds then then connection attempt will continue, otherwise
     * it will fail.
     * </p>
     *
     * <p>
     * The result of the connection attempt will be returned via the callback,
     * if it is provided. The callback will be called on the main thread.
     * </p>
     *
     * @param device to connect to.
     * @param connectionAttemptConfiguration to use when connecting.
     * @param callback to return results via.
     *
     * @return flag indicating whether the connection request was accepted.
     */
    @AnyThread
    public boolean connect(@NonNull BluetoothDevice device,
            @NonNull ConnectionAttemptConfiguration connectionAttemptConfiguration,
            @Nullable ConnectionAttemptCallback callback) {
        return mConnectionManager != null && mConnectionManager.connect(device,
                connectionAttemptConfiguration.getInternalConnectionConfiguration(), callback);
    }

    /**
     * Convenience version of {@link
     * #connect(BluetoothDevice, ConnectionAttemptConfiguration, ConnectionAttemptCallback)}
     * which uses default values.
     *
     * @param device to connect to.
     * @param callback to return results via.
     *
     * @return flag indicating whether the connection request was accepted.
     */
    @AnyThread
    public boolean connect(@NonNull BluetoothDevice device,
            @Nullable ConnectionAttemptCallback callback) {
        return mConnectionManager != null && mConnectionManager.connect(device,
                new ConnectionAttemptConfiguration.Builder().build()
                        .getInternalConnectionConfiguration(), callback);
    }

    /**
     * <p>
     * Attempt to disconnect the provided Bluetooth device.
     * </p>
     *
     * <p>
     * If the device is currently connected to then the connection to it will
     * be closed. If it is being connected to then the connection attempt will
     * be cancelled. If it is neither then nothing will happen.
     * </p>
     *
     * <p>
     * Disconnection occurs asynchronously so there is no guarantee that the
     * device is actually disconnected when this method returns. In addition,
     * this method does not take a callback parameter. If you wish to be
     * informed exactly when the device is disconnected, please register a
     * {@link DeviceConnectionListener} using {@link
     * #registerDeviceConnectionListener(DeviceConnectionListener)}.
     * </p>
     *
     * @param device to attempt disconnection from.
     *
     * @return flag indicating whether the disconnection request was accepted.
     */
    @AnyThread
    public boolean disconnect(BluetoothDevice device) {
        return mConnectionManager != null && mConnectionManager.disconnect(device);
    }

    /**
     * <p>
     * Request that all connected Bluetooth devices be disconnected.
     * </p>
     *
     * <p>
     * Be aware that this operation will only disconnect devices for which
     * connections are being managed at the time it is called. Any connect
     * operations subsequently started will not be affected.
     * </p>
     *
     * <p>
     * NOTE: You must register a device connection listener before calling this
     * method if you wish to be notified of the actual disconnections.
     * </p>
     */
    @AnyThread
    public void disconnectAll() {
        if (mConnectionManager != null) {
            mConnectionManager.disconnectAll();
        }
    }

    // Discovery scan management

    /**
     * <p>
     * Attempt to start a discovery scan to detect any available Bluetooth
     * devices in the area.
     * </p>
     *
     * <p>
     * If the scan starts successfully, this method returns null. If it does
     * not, a {@link DiscoveryError} explaining the reason for the failure will
     * be returned.
     * </p>
     *
     * <p>
     * Scan events are reported via the {@link DiscoveryListener} interface.
     * Either register a listener using {@link
     * #registerDiscoveryListener(DiscoveryListener)}, or call the convenience
     * method {@link #startDiscovery(DiscoveryListener)}.
     * </p>
     *
     * <p>
     * Note that all methods on any registered listener interfaces will be
     * called on the main thread.
     * </p>
     *
     * @return discovery error, or null.
     */
    @AnyThread
    public DiscoveryError startDiscovery() {
        if (mDiscoveryManager != null) {
            return mDiscoveryManager.startDiscovery();
        } else {
            return DiscoveryError.BLUETOOTH_NOT_AVAILABLE;
        }
    }

    /**
     * <p>
     * Start a discovery scan to detect any available Bluetooth devices in the
     * area, first registering the provided listener to receive discovery
     * events.
     * </p>
     *
     * <p>
     * Remember to call {@link #unregisterDiscoveryListener(DiscoveryListener)}
     * when discovery events are no longer required.
     * </p>
     *
     * <p>
     * Note that all methods on any registered listener interfaces will be
     * called on the main thread.
     * </p>
     *
     * @param listener to provide discovery events to.
     */
    @AnyThread
    public DiscoveryError startDiscovery(@NonNull DiscoveryListener listener) {
        if (mDiscoveryManager != null) {
            mDiscoveryManager.registerListener(listener);
            return mDiscoveryManager.startDiscovery();
        } else {
            return DiscoveryError.BLUETOOTH_NOT_AVAILABLE;
        }
    }

    /**
     * <p>
     * Attempt to stop a running discovery scan which was started by this
     * service.
     * <p>
     *
     * <p>
     * There are several important things to
     * note about this method:
     * </p>
     *
     * <ul>
     *     <li>it can only stop discovery scans which were started using {@link
     *     #startDiscovery()}.</li>
     *     <li>it is an asynchronous operation and can take some time to
     *     complete.</li>
     *     <li>if the caller needs to be notified when the scan is actually
     *     stopped but did not start the scan via {@link #startDiscovery()}
     *     then it must register a {@link DiscoveryListener} prior to calling
     *     this method.</li>
     *     <li>if no discovery scan is running then it does nothing.</li>
     * </ul>
     *
     * @return flag indicating whether the discovery stop request was
     *         successful.
     */
    @AnyThread
    public boolean stopDiscovery() {
        return mDiscoveryManager != null && mDiscoveryManager.stopDiscovery();
    }

    // Listener management

    /**
     * <p>
     * Register a listener for Bluetooth status related service events.
     * </p>
     *
     * <p>
     * Do not forget to call {@link
     * #unregisterBluetoothStatusListener(BluetoothStatusListener)} to
     * unregister the listener when it is no longer required.
     * </p>
     *
     * <p>
     * Note that the listener will be called on the main thread regardless of
     * which thread it is registered on.
     * </p>
     *
     * @param listener to register.
     */
    @AnyThread
    public void registerBluetoothStatusListener(BluetoothStatusListener listener) {
        if (mAdapterManager != null) {
            mAdapterManager.registerBluetoothStatusListener(listener);
        }
    }

    /**
     * Unregister a listener previously registered for Bluetooth status related
     * service events.
     *
     * @param listener to unregister.
     */
    @AnyThread
    public void unregisterBluetoothStatusListener(BluetoothStatusListener listener) {
        if (mAdapterManager != null) {
            mAdapterManager.unregisterBluetoothStatusListener(listener);
        }
    }

    /**
     * <p>
     * Register a listener for device connection status related service events.
     * </p>
     *
     * <p>
     * Do not forget to call {@link
     * #unregisterDeviceConnectionListener(DeviceConnectionListener)} to
     * unregister the listener when it is no longer required.
     * </p>
     *
     * <p>
     * Note that the listener will be called on the main thread regardless of
     * which thread it is registered on.
     * </p>
     *
     * @param listener to register.
     */
    @AnyThread
    public void registerDeviceConnectionListener(DeviceConnectionListener listener) {
        if (mConnectionManager != null) {
            mConnectionManager.registerDeviceConnectionListener(listener);
        }
    }

    /**
     * Unregister a listener previously registered for device connection status
     * related service events.
     *
     * @param listener to unregister.
     */
    @AnyThread
    public void unregisterDeviceConnectionListener(DeviceConnectionListener listener) {
        if (mConnectionManager != null) {
            mConnectionManager.unregisterDeviceConnectionListener(listener);
        }
    }

    /**
     * <p>
     * Register a listener for discovery scan related service events.
     * </p>
     *
     * <p>
     * Do not forget to call {@link
     * #unregisterDiscoveryListener(DiscoveryListener)} to unregister the
     * listener when it is no longer required.
     * </p>
     *
     * <p>
     * Note that the listener will be called on the main thread regardless of
     * which thread it is registered on.
     * </p>
     *
     * @param listener to register.
     */
    @AnyThread
    public void registerDiscoveryListener(DiscoveryListener listener) {
        if (mDiscoveryManager != null) {
            mDiscoveryManager.registerListener(listener);
        }
    }

    /**
     * Unregister a listener previously registered for discovery scan related
     * service events.
     *
     * @param listener to unregister.
     */
    @AnyThread
    public void unregisterDiscoveryListener(DiscoveryListener listener) {
        if (mDiscoveryManager != null) {
            mDiscoveryManager.unregisterListener(listener);
        }
    }
}
