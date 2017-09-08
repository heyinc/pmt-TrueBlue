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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Monitors and reports on pairing events in the Bluetooth subsystem.
 */
public class PairingMonitor extends BroadcastReceiver {

    private final Context mContext;
    private final CopyOnWriteArraySet<Listener> mListeners = new CopyOnWriteArraySet<>();

    /**
     * Create a pairing monitor with the provided context.
     *
     * @param context with which to access pairing related system broadcasts.
     */
    public PairingMonitor(@NonNull Context context) {
        mContext = context.getApplicationContext();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(intent.getAction())) {
            return;
        }
        final int previousBondState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE,
                BluetoothDevice.BOND_NONE);
        final BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        final int currentBondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE,
                BluetoothDevice.BOND_NONE);
        Log.d("TrueBlue", "Device " + device.getAddress() + " bond state change " +
                "from " + previousBondState + " to " + currentBondState + ".");
        switch (previousBondState) {
            case BluetoothDevice.BOND_NONE:
                // Entering pairing mode.
                if (BluetoothDevice.BOND_BONDING == currentBondState) {
                    for (Listener listener : mListeners) {
                        listener.onPairingAttemptStarted(device);
                    }
                }
                break;
            case BluetoothDevice.BOND_BONDING:
                // Pairing result.
                if (BluetoothDevice.BOND_BONDED == currentBondState) {
                    for (Listener listener : mListeners) {
                        listener.onPairingAttemptSucceeded(device);
                    }
                } else if (BluetoothDevice.BOND_NONE == currentBondState) {
                    for (Listener listener : mListeners) {
                        listener.onPairingAttemptFailed(device);
                    }
                }
                break;
            case BluetoothDevice.BOND_BONDED:
                // Pairing removal - not currently supported.
                break;
            default:
                break;
        }
    }

    /**
     * Start monitoring for pairing related system broadcasts.
     */
    public void start() {
        ContextUtils.unregisterReceiverSilently(mContext, this);
        mContext.registerReceiver(this,
                new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED));
    }

    /**
     * Stop monitoring for pairing related system broadcasts. This will also
     * clear all registered listeners.
     */
    public void stop() {
        ContextUtils.unregisterReceiverSilently(mContext, this);
        mListeners.clear();
    }

    /**
     * Register a listener for Bluetooth pairing related events. Remember to
     * unregister using {@link #unregisterListener(Listener)}) when these
     * events are no longer required.
     *
     * @param listener to register.
     */
    void registerListener(Listener listener) {
        mListeners.add(listener);
    }

    /**
     * Unregister a previously registered listener for Bluetooth pairing
     * related events.
     *
     * @param listener to unregister.
     */
    void unregisterListener(Listener listener) {
        mListeners.remove(listener);
    }

    /**
     * Listener interface for Bluetooth pairing related events.
     */
    interface Listener {

        /**
         * Called when a pairing attempt with a Bluetooth device has failed.
         *
         * @param device for which a pairing attempt has failed.
         */
        void onPairingAttemptFailed(@NonNull BluetoothDevice device);

        /**
         * Called when a pairing attempt with a Bluetooth device has started.
         *
         * @param device for which a pairing attempt has started.
         */
        void onPairingAttemptStarted(@NonNull BluetoothDevice device);

        /**
         * Called when a pairing attempt with a Bluetooth device has succeeded.
         *
         * @param device for which a pairing attempt has succeeded.
         */
        void onPairingAttemptSucceeded(@NonNull BluetoothDevice device);
    }
}
