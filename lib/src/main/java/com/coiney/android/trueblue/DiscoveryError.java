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
 * Represents the types of error that can be encountered during Bluetooth
 * discovery scans.
 */
public enum DiscoveryError {

    /**
     * The request to start a discovery scan failed because the service is
     * already managing a running scan.
     */
    ALREADY_RUNNING,

    /**
     * The request to start a discovery scan failed because Bluetooth was
     * disabled at the time the start was attempted.
     */
    BLUETOOTH_DISABLED,

    /**
     * <p>
     * The request to start a discovery scan failed because Bluetooth is not
     * available to the system.
     * </p>
     *
     * <p>
     * This generally indicates that there is no
     * Bluetooth hardware available.
     * </p>
     */
    BLUETOOTH_NOT_AVAILABLE,

    /**
     * <p>
     * Coarse location permissions are required in order to start the discovery
     * scan.
     * </p>
     *
     * <p>
     * The user should be asked to provide these permissions and a new request
     * made to start a discovery scan if they do.
     * </p>
     */
    COARSE_LOCATION_PERMISSION_REQUIRED,

    /**
     * <p>
     * The system indicated that it could not start a discovery scan.
     * </p>
     *
     * <p>
     * This generally indicates that a discovery scan initiated outside of this
     * service is already running.
     * </p>
     */
    SYSTEM_ERROR
}
