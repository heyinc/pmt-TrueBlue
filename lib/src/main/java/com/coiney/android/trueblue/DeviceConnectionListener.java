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
 * Listener interface which provides information regarding changes in the
 * status of a connected Bluetooth device.
 */
public interface DeviceConnectionListener {

    /**
     * Called when a device is connected to.
     *
     * @param device connected to.
     */
    void onDeviceConnected(@NonNull BluetoothDevice device);

    /**
     * Called when the connection to a Bluetooth device is closed by request.
     *
     * @param device the connection was closed for.
     * @param wasCausedByError or not (i.e. requested closure vs termination
     *                         due to an error).
     */
    void onDeviceDisconnected(@NonNull BluetoothDevice device, boolean wasCausedByError);
}
