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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.UUID;

/**
 * <p>
 * Provides the connection related configuration parameters to use when
 * attempting to connect to a Bluetooth device.
 * </p>
 *
 * <p>
 * This class cannot be instantiated directly - instead, use an instance of
 * the contained Builder class.
 * </p>
 */
public class ConnectionConfiguration {

    private final boolean mCanInterruptDiscoveryScan;
    private final ConnectionRetryPolicy mConnectionRetryPolicy;
    private final boolean mIsSecure;
    private final UUID mServiceRecordUuid;

    public ConnectionConfiguration(UUID serviceRecordUuid, boolean isSecure,
            boolean canInterruptDiscoveryScan,
            ConnectionRetryPolicy connectionRetryPolicy) {
        mCanInterruptDiscoveryScan = canInterruptDiscoveryScan;
        mConnectionRetryPolicy = connectionRetryPolicy;
        mIsSecure = isSecure;
        mServiceRecordUuid = serviceRecordUuid;
    }

    /**
     * Ask whether any discovery scan which may be running at the time the
     * connection attempt starts may be interrupted in order to begin the
     * connection attempt immediately. If not, the connection attempt will
     * wait until the discovery scan has finished.
     *
     * @return flag indicating whether or not any discovery scan which may be
     *         running at the time the connection attempt starts may be
     *         interrupted.
     */
    boolean canInterruptDiscoveryScan() {
        return mCanInterruptDiscoveryScan;
    }

    /**
     * Get the connection retry policy for the configuration.
     *
     * @return the connection retry policy for the configuration.
     */
    @NonNull
    ConnectionRetryPolicy getConnectionRetryPolicy() {
        return mConnectionRetryPolicy;
    }

    /**
     * Ask whether the connection should be performed in a secure way or not.
     *
     * @return flag indicating whether the connection should be performed in a
     *         secure way or not.
     */
    boolean isSecure() {
        return mIsSecure;
    }

    /**
     * Get the service record UUID to use when connecting, if any.
     *
     * @return the service record UUID to use when connecting, or null.
     */
    @Nullable
    UUID getServiceRecordUuid() {
        return mServiceRecordUuid;
    }
}
