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

import android.annotation.TargetApi;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Build;
import android.support.annotation.NonNull;

import com.coiney.android.trueblue.Connection;

import net.jcip.annotations.ThreadSafe;

import java.io.IOException;
import java.util.UUID;

/**
 * Thread used to attempt to open a connection to a given BluetoothDevice.
 * Communicates the progress and result of the attempt via the provided
 * callback.
 */
@ThreadSafe
class ConnectTask implements Runnable, PairingMonitor.Listener {

    private static final int CONNECTION_DELAY_DURING_DISCOVERY = 1000;

    private final AdapterManager mAdapterManager;
    private final BluetoothDevice mDevice;
    private Boolean mDidPairingSucceed;
    private final ConnectionConfiguration mConnectionConfiguration;
    private final Callback mCallback;
    private boolean mCancelled;
    private Connection mConnection;
    private final Object mConnectionLock = new Object();
    private final ConnectionRetryPolicy mConnectionRetryPolicy;
    private volatile boolean mHasPairingStarted;
    private final Logger mLogger;
    private final PairingMonitor mPairingMonitor;
    private BluetoothSocket mSocket;

    /**
     * Create a connect task with the provided parameters.
     *
     * @param device to connect to.
     * @param connectionConfiguration to use when connecting.
     * @param adapterManager with which to check and stop discovery scanning.
     * @param pairingMonitor with which to obtain pairing related information.
     * @param callback to which to report connection progress and results.
     * @param logger to log to.
     */
    ConnectTask(@NonNull BluetoothDevice device,
            @NonNull ConnectionConfiguration connectionConfiguration,
            @NonNull AdapterManager adapterManager, @NonNull PairingMonitor pairingMonitor,
            @NonNull Callback callback, @NonNull Logger logger) {
        mAdapterManager = adapterManager;
        mDevice = device;
        mCallback = callback;
        mConnectionConfiguration = connectionConfiguration;
        mConnectionRetryPolicy = connectionConfiguration.getConnectionRetryPolicy();
        mLogger = logger;
        mPairingMonitor = pairingMonitor;
    }

    /**
     * Run the task, attempting to connect to the provided Bluetooth device.
     */
    @Override
    public void run() {
        mLogger.d(mDevice, "Connect task starting up.");
        UUID serviceRecordUuid = mConnectionConfiguration.getServiceRecordUuid();
        if (null == serviceRecordUuid) {
            serviceRecordUuid = BluetoothUtils.getLikelyServiceRecordUuid(mDevice);
            mLogger.d(mDevice, "No service record UUID provided - trying " +
                    serviceRecordUuid + ".");
        }
        final boolean shouldConnectSecurely = mConnectionConfiguration.isSecure() ||
                Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD_MR1;
        mLogger.d(mDevice, "Using " + (shouldConnectSecurely ? "secure" : "insecure") +
                " connection.");
        boolean canRetry = true;
        while (null == mConnection && canRetry) {
            mLogger.d(mDevice, "Connection attempt #%d.",
                    mConnectionRetryPolicy.getNumberOfRetriesAttempted() + 1);
            try {
                if (!mConnectionConfiguration.canInterruptDiscoveryScan()) {
                    mLogger.d(mDevice, "Not permitted to interrupt discovery scan.");
                    ensureDiscoveryNotRunning();
                }
                mAdapterManager.stopDiscovery();
                final boolean isPairingRequired = (mDevice.getBondState() !=
                        BluetoothDevice.BOND_BONDED);
                if (isPairingRequired) {
                    mLogger.d(mDevice, "Pairing will be required as part of connection " +
                            "attempt.");
                    mPairingMonitor.registerListener(this);
                }
                try {
                    openConnection(serviceRecordUuid, shouldConnectSecurely, isPairingRequired);
                } catch (IOException e) {
                    canRetry = handleConnectionAttemptFailure(isPairingRequired);
                }
            } catch (InterruptedException e) {
                handleConnectionAttemptCancellation();
                break;
            }
        }
        // Make absolutely sure we have unregistered.
        mPairingMonitor.unregisterListener(this);
    }

    /**
     * Interrupt the connection attempt, either cancelling the attempt if it is
     * ongoing or closing any connection which has been established.
     */
    void cancelOrDisconnect() {
        mLogger.d(mDevice, "Cancel or disconnect requested.");
        synchronized (mConnectionLock) {
            // If a connection has already been successfully opened then close
            // it. Otherwise, if a connection attempt is being made then close
            // the underlying Bluetooth socket. Otherwise, simply record the
            // fact that cancellation has been requested - this flag will be
            // checked at various points during task processing.
            mCancelled = true;
            if (mConnection != null) {
                mLogger.d(mDevice, "Connection present - closing.");
                mConnection.close();
            } else {
                mLogger.d(mDevice, "No connection present - attempting to close socket.");
                BluetoothUtils.closeSocketSilently(mSocket);
            }
        }
    }

    @Override
    public void onPairingAttemptFailed(@NonNull BluetoothDevice device) {
        if (!mDevice.equals(device)) {
            return;
        }
        synchronized (this) {
            mDidPairingSucceed = false;
            notifyAll();
        }
    }

    @Override
    public void onPairingAttemptStarted(@NonNull BluetoothDevice device) {
        if (!mDevice.equals(device)) {
            return;
        }
        // We need to record this because in the event a connection fails for
        // some reason before pairing starts we must not wait for the system to
        // give us a pairing result.
        mHasPairingStarted = true;
        mCallback.onPairingStarted(this);
    }

    @Override
    public void onPairingAttemptSucceeded(@NonNull BluetoothDevice device) {
        if (!mDevice.equals(device)) {
            return;
        }
        synchronized (this) {
            mDidPairingSucceed = true;
            notifyAll();
        }
    }

    private void ensureDiscoveryNotRunning() throws InterruptedException {
        while (mAdapterManager.isDiscoveryRunning()) {
            mLogger.d(mDevice, "Discovery scan in progress - delaying connection " +
                    "attempt by %d ms.", CONNECTION_DELAY_DURING_DISCOVERY);
            // Ideally this should be a wait-notify setup rather than polling.
            Thread.sleep(CONNECTION_DELAY_DURING_DISCOVERY);
        }
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD_MR1)
    private void openConnection(UUID serviceRecordUuid, boolean shouldConnectSecurely,
            boolean isPairingRequired)
            throws IOException, InterruptedException {
        try {
            final BluetoothSocket socket;
            if (shouldConnectSecurely) {
                socket = mDevice.createRfcommSocketToServiceRecord(serviceRecordUuid);
            } else {
                // API version check is incorporated into flag.
                socket = mDevice.createInsecureRfcommSocketToServiceRecord(
                        serviceRecordUuid);
            }
            synchronized (mConnectionLock) {
                if (mCancelled) {
                    throw new InterruptedException();
                }
                mSocket = socket;
            }
            mSocket.connect();
            if (isPairingRequired && mHasPairingStarted && didPairingSucceed()) {
                mCallback.onPairingSucceeded(this);
            }
            synchronized (mConnectionLock) {
                mSocket = null;
                handleConnectionAttemptSucceeded(socket);
            }
        } catch (IOException e) {
            synchronized (mConnectionLock) {
                BluetoothUtils.closeSocketSilently(mSocket);
                mSocket = null;
            }
            if (Thread.interrupted()) {
                throw new InterruptedException();
            } else {
                throw e;
            }
        }
    }

    private boolean didPairingSucceed() throws InterruptedException {
        synchronized (this) {
            if (null == mDidPairingSucceed) {
                mLogger.d(mDevice, "Waiting for pairing result from system.");
                wait();
            }
            return mDidPairingSucceed;
        }
    }

    private boolean handleConnectionAttemptFailure(boolean wasPairingRequired)
            throws InterruptedException {
        if (wasPairingRequired) {
            if (mHasPairingStarted) {
                if (didPairingSucceed()) {
                    mLogger.d(mDevice, "Pairing succeeded but connection failed. Can " +
                            "continue if retries are available.");
                    mCallback.onPairingSucceeded(this);
                } else {
                    mLogger.d(mDevice, "Pairing failed - ignoring any remaining retries.");
                    mCallback.onPairingFailed(this);
                    mCallback.onConnectionFailed(this);
                    return false;
                }
            } else {
                mLogger.d(mDevice, "Pairing required but connection failed before it " +
                        "started - will try again if retries remain.");
            }
        }
        if (!mConnectionRetryPolicy.hasAttemptRemaining()) {
            mLogger.d(mDevice, "Final connection attempt failed.");
            mCallback.onConnectionFailed(this);
            return false;
        }
        mLogger.d(mDevice, "Connection attempt failed - %d attempt(s) remaining.",
                mConnectionRetryPolicy.getRemainingRetryCount());
        mConnectionRetryPolicy.retry();
        final int retryDelay = mConnectionRetryPolicy.getCurrentRetryDelay();
        mLogger.d(mDevice, "Waiting %d ms before next connection attempt.", retryDelay);
        Thread.sleep(retryDelay);
        return true;
    }

    private void handleConnectionAttemptCancellation() {
        mLogger.d(mDevice, "Connection attempt cancelled.");
        mCallback.onConnectionCancelled(this);
    }

    private void handleConnectionAttemptSucceeded(BluetoothSocket socket) throws IOException {
        mLogger.d(mDevice, "Connection succeeded.");
        mConnection = new ConnectionImpl(socket, socket.getInputStream(),
                socket.getOutputStream());
        mCallback.onConnectionSucceeded(this, mConnection);
    }

    /**
     * Callback interface for reporting the progress and results of attempting
     * to connect to a Bluetooth device.
     */
    interface Callback {

        /**
         * Called when the connection attempt has been volitionally cancelled.
         *
         * @param connectTask which was cancelled.
         */
        void onConnectionCancelled(@NonNull ConnectTask connectTask);

        /**
         * Called when the connection attempt has failed and all retries have
         * been exhausted.
         *
         * @param connectTask which failed.
         */
        void onConnectionFailed(@NonNull ConnectTask connectTask);

        /**
         * Called when the connection attempt has succeeded.
         *
         * @param connectTask which succeeded.
         * @param connection which was established to the Bluetooth device.
         */
        void onConnectionSucceeded(@NonNull ConnectTask connectTask,
                @NonNull Connection connection);

        /**
         * Called when pairing was required as part of the connection process
         * but it failed.
         *
         * @param connectTask for which pairing failed.
         */
        void onPairingFailed(@NonNull ConnectTask connectTask);

        /**
         * Called when pairing is required as part of the connection process.
         *
         * @param connectTask for which pairing is required.
         */
        void onPairingStarted(@NonNull ConnectTask connectTask);

        /**
         * Called when pairing was required as part of the connection process
         * and it succeeded.
         *
         * @param connectTask for which pairing succeeded.
         */
        void onPairingSucceeded(@NonNull ConnectTask connectTask);
    }
}
