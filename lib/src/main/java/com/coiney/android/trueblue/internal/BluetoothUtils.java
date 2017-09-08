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
import android.bluetooth.BluetoothSocket;
import android.os.Build;
import android.os.ParcelUuid;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.IOException;
import java.util.UUID;

/**
 * A collection of useful Bluetooth related utility methods.
 */
final class BluetoothUtils {

    private static final UUID COMMON_BLUETOOTH_SERVICE_UUID =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private BluetoothUtils() {
        throw new AssertionError("Instantiation is not supported.");
    }

    /**
     * Close a Bluetooth socket silently, ignoring any exceptions generated in
     * the process.
     *
     * @param socket to close silently.
     */
    static void closeSocketSilently(@Nullable BluetoothSocket socket) {
        if (socket == null) {
            return;
        }
        try {
            socket.close();
        } catch (IOException ignored) {}
    }

    /**
     * Attempt to guess the service record UUID to use to connect to a
     * Bluetooth device. According to the Android documentation it is worth
     * trying the well known SPP UUID when connecting to a Bluetooth serial
     * board, so we default to this. On APIs 15 and above we can take this a
     * step further and ask a discovered device for a list of its UUIDs.
     * Currently we simply select the first one in the list, assuming the list
     * isn't empty.
     *
     * @param device to determine the likely service record UUID for.
     *
     * @return the likely service record UUID for the Bluetooth device.
     */
    @NonNull
    static UUID getLikelyServiceRecordUuid(@NonNull BluetoothDevice device) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
            final ParcelUuid[] parcelUuids = device.getUuids();
            if (parcelUuids != null) {
                for (ParcelUuid parcelUuid : parcelUuids) {
                    final UUID uuid = parcelUuid.getUuid();
                    if (uuid != null) {
                        return uuid;
                    }
                }
            }
        }
        return COMMON_BLUETOOTH_SERVICE_UUID;
    }
}
