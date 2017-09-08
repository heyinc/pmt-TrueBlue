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

/**
 * Listener interface which provides information regarding changes in the
 * status of Bluetooth at the system level.
 */
public interface BluetoothStatusListener {

    /**
     * Called when Bluetooth is disabled at the system level.
     */
    void onBluetoothDisabled();

    /**
     * Called when Bluetooth is enabled at the system level.
     */
    void onBluetoothEnabled();
}
