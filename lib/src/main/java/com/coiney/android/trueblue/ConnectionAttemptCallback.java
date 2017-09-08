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

import android.bluetooth.BluetoothDevice;
import android.support.annotation.NonNull;

/**
 * Callback interface which reports the result of an attempt to connect to a
 * Bluetooth device.
 */
public interface ConnectionAttemptCallback {

    /**
     * Called when the attempt to connect to the Bluetooth device succeeds.
     *
     * @param device connected to.
     * @param connection to device.
     */
    void onConnectionAttemptSucceeded(@NonNull BluetoothDevice device,
            @NonNull Connection connection);

    /**
     * Called when the attempt to connect to the Bluetooth device has been
     * cancelled.
     *
     * @param device for which the connection attempt was cancelled.
     */
    void onConnectionAttemptCancelled(@NonNull BluetoothDevice device);

    /**
     * Called when the attempt to connect to the Bluetooth device fails.
     *
     * @param device for which the connection attempt failed.
     */
    void onConnectionAttemptFailed(@NonNull BluetoothDevice device);

    /**
     * <p>
     * Called when pairing is required as part of the connection attempt, but
     * the pairing attempt fails.
     * </p>
     *
     * <p>
     * Note that this is purely informational - it does not mean that the
     * connection itself has failed. This information will be provided
     * separately via the onConnectionAttempt* methods.
     * </p>
     *
     * @param device for which the pairing attempt failed.
     */
    void onPairingAttemptFailed(@NonNull BluetoothDevice device);

    /**
     * Called when pairing is required as part of the connection attempt, and
     * the pairing process has been started.
     *
     * @param device for which pairing has started.
     */
    void onPairingAttemptStarted(@NonNull BluetoothDevice device);

    /**
     * <p>
     * Called when pairing is required as part of the connection attempt, and
     * the pairing attempt succeeds.
     * </p>
     *
     * <p>
     * Note that this is purely informational - it does not mean that the
     * connection itself has succeeded. This information will be provided
     * separately via the onConnectionAttempt* methods.
     * </p>
     *
     * @param device for which the pairing attempt failed.
     */
    void onPairingAttemptSucceeded(@NonNull BluetoothDevice device);
}
