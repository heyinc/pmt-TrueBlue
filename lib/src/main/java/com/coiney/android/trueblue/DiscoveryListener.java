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
 * Listener interface which provides events related to Bluetooth discovery
 * scanning.
 */
public interface DiscoveryListener {

    /**
     * <p>
     * Called when a pairable Bluetooth device is discovered during a
     * discovery scan.
     * </p>
     *
     * <p>
     * Be aware that this method may be called with the same device multiple
     * times in any given discovery scan.
     * </p>
     *
     * @param device discovered.
     */
    void onDeviceDiscovered(@NonNull BluetoothDevice device);

    /**
     * Called when a discovery scan has finished.
     */
    void onDiscoveryFinished();

    /**
     * Called when a discovery scan has been started.
     */
    void onDiscoveryStarted();
}
