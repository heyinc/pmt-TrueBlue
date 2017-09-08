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
import com.coiney.android.trueblue.Connection;

import net.jcip.annotations.ThreadSafe;

import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * Proxy facade class designed to abstract away the implementation details of a
 * connection. Responsible for carrying out the connection attempt and managing
 * the resulting connection (if the attempt is successful). Reports results
 * internally via {@link Listener} and externally (i.e. to the client which
 * requested the connection) via {@link ConnectionAttemptCallback}.
 */
@ThreadSafe
final class ConnectionProxy implements ConnectTask.Callback, Connection.OnCloseListener {

    private final BluetoothDevice mDevice;
    private final WeakReference<ConnectionAttemptCallback> mCallbackWeakReference;
    private ConnectTask mConnectTask;
    private Future mConnectTaskFuture;
    private Connection mConnection;
    private final Listener mListener;
    private boolean mStarted;

    /**
     * Create a connection proxy with the provided parameters.
     *
     * @param device to attempt to connect to.
     * @param callback to report connection and pairing attempt progress and
     *                 results to (externally).
     * @param listener to report connection related events to (internally).
     */
    ConnectionProxy(@NonNull BluetoothDevice device, @Nullable ConnectionAttemptCallback callback,
            @NonNull Listener listener) {
        mDevice = device;
        mCallbackWeakReference = new WeakReference<>(callback);
        mListener = listener;
    }

    /**
     * Get the Bluetooth device the connection is associated with.
     *
     * @return the Bluetooth device the connection is associated with.
     */
    @NonNull
    BluetoothDevice getDevice() {
        return mDevice;
    }

    /**
     * Ask whether the connection being managed by the proxy is complete and
     * open.
     *
     * @return flag indicating whether the connection being managed by the
     *         proxy is complete and open.
     */
    synchronized boolean isConnected() {
        return mConnection != null && mConnection.isOpen();
    }

    /**
     * Connect to the Bluetooth device being managed using the provided connect
     * task and executor service.
     *
     * The primary reason this is required is so that the task can be started
     * in such a way that it returns a future, allowing it to be cancelled if
     * required (i.e. as part of a disconnect attempt).
     *
     * @param connectTask to use to connect to the Bluetooth device.
     * @param executorService to run the connect task on.
     */
    synchronized void connect(@NonNull ConnectTask connectTask,
            @NonNull ExecutorService executorService) {
        if (mStarted) {
            return;
        }
        mStarted = true;
        mConnectTask = connectTask;
        mConnectTaskFuture = executorService.submit(mConnectTask);
    }

    /**
     * Disconnect from the Bluetooth device being managed, either closing the
     * connection if it has completed and is open or cancelling the ongoing
     * connection attempt otherwise.
     */
    synchronized void disconnect() {
        if (mConnection != null) {
            mConnection.close();
            mConnection = null;
        } else if (mConnectTaskFuture != null && mConnectTask != null) {
            // This is a bit complicated because of the various states the task
            // can enter - sleeping, blocking on a BluetoothSocket, or
            // executing code - and the fact the method required to stop it in
            // each case is different. TLDR we need to both interrupt the
            // thread running the task and directly ask it to cancel or
            // disconnect.
            mConnectTaskFuture.cancel(true);
            mConnectTask.cancelOrDisconnect();
        }
    }

    // Connect task callback

    @Override
    public synchronized void onConnectionCancelled(@NonNull ConnectTask connectTask) {
        mConnectTask = null;
        mConnectTaskFuture = null;
        mListener.onConnectionAttemptCancelled(this);
        final ConnectionAttemptCallback callback = mCallbackWeakReference.get();
        if (callback != null) {
            ThreadUtils.postOnMainThread(new Runnable() {
                @Override
                public void run() {
                    callback.onConnectionAttemptCancelled(mDevice);
                }
            });
        }
    }

    @Override
    public synchronized void onConnectionFailed(@NonNull ConnectTask connectTask) {
        mConnectTask = null;
        mConnectTaskFuture = null;
        mListener.onConnectionAttemptFailed(this);
        final ConnectionAttemptCallback callback = mCallbackWeakReference.get();
        if (callback != null) {
            ThreadUtils.postOnMainThread(new Runnable() {
                @Override
                public void run() {
                    callback.onConnectionAttemptFailed(mDevice);
                }
            });
        }
    }

    @Override
    public synchronized void onConnectionSucceeded(@NonNull ConnectTask connectTask,
            @NonNull final Connection connection) {
        mConnection = connection;
        mConnection.registerOnCloseListener(this);
        mConnectTask = null;
        mConnectTaskFuture = null;
        mListener.onConnectionAttemptSucceeded(this);
        final ConnectionAttemptCallback callback = mCallbackWeakReference.get();
        if (callback != null) {
            ThreadUtils.postOnMainThread(new Runnable() {
                @Override
                public void run() {
                    callback.onConnectionAttemptSucceeded(mDevice, connection);
                }
            });
        }
    }

    @Override
    public void onPairingFailed(@NonNull ConnectTask connectTask) {
        final ConnectionAttemptCallback callback = mCallbackWeakReference.get();
        if (callback != null) {
            ThreadUtils.postOnMainThread(new Runnable() {
                @Override
                public void run() {
                    callback.onPairingAttemptFailed(mDevice);
                }
            });
        }
    }

    @Override
    public void onPairingStarted(@NonNull ConnectTask connectTask) {
        final ConnectionAttemptCallback callback = mCallbackWeakReference.get();
        if (callback != null) {
            ThreadUtils.postOnMainThread(new Runnable() {
                @Override
                public void run() {
                    callback.onPairingAttemptStarted(mDevice);
                }
            });
        }
    }

    @Override
    public void onPairingSucceeded(@NonNull ConnectTask connectTask) {
        final ConnectionAttemptCallback callback = mCallbackWeakReference.get();
        if (callback != null) {
            ThreadUtils.postOnMainThread(new Runnable() {
                @Override
                public void run() {
                    callback.onPairingAttemptSucceeded(mDevice);
                }
            });
        }
    }

    // Connection on close listener

    @Override
    public synchronized void onConnectionClosed(@NonNull Connection connection,
            boolean wasClosedByError) {
        mConnection.unregisterOnCloseListener(this);
        mConnection = null;
        mListener.onConnectionClosed(this, wasClosedByError);
    }

    /**
     * Internal listener interface for connection attempt related events.
     */
    interface Listener {

        /**
         * Called when the attempt to connect to the Bluetooth device has been
         * cancelled.
         *
         * @param connectionProxy managing the Bluetooth device for which the
         *                        connection attempt was cancelled.
         */
        void onConnectionAttemptCancelled(@NonNull ConnectionProxy connectionProxy);

        /**
         * Called when the established, open connection being managed has been
         * closed, be it volitionally or due to an error being encountered.
         *
         * @param connectionProxy managing the connection which was closed.
         * @param wasClosedByError or not (i.e. was disconnection deliberate or
         *                         accidental).
         */
        void onConnectionClosed(@NonNull ConnectionProxy connectionProxy, boolean wasClosedByError);

        /**
         * Called when the attempt to connect to the Bluetooth device has
         * failed (and all retries have been exhausted).
         *
         * @param connectionProxy managing the Bluetooth device for which the
         *                        connection attempt failed.
         */
        void onConnectionAttemptFailed(@NonNull ConnectionProxy connectionProxy);

        /**
         * Called when the attempt to connect to the Bluetooth device has
         * succeeded and an established, open connection is now available.
         *
         * @param connectionProxy managing the Bluetooth device for which the
         *                        connection attempt succeeded.
         */
        void onConnectionAttemptSucceeded(@NonNull ConnectionProxy connectionProxy);
    }
}
