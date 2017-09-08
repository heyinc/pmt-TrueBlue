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

import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;

import com.coiney.android.trueblue.internal.ConnectionConfiguration;
import com.coiney.android.trueblue.internal.ConnectionRetryPolicy;

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
public final class ConnectionAttemptConfiguration {

    private final ConnectionConfiguration mConnectionConfiguration;

    private ConnectionAttemptConfiguration(
            @NonNull ConnectionConfiguration connectionConfiguration) {
        mConnectionConfiguration = connectionConfiguration;
    }

    ConnectionConfiguration getInternalConnectionConfiguration() {
        return mConnectionConfiguration;
    }

    /**
     * Builds {@link ConnectionAttemptConfiguration} instances.
     */
    public static final class Builder {

        private static final int DEFAULT_RETRY_COUNT = 0;
        private static final int DEFAULT_INITIAL_RETRY_DELAY_MS = 2000;
        private static final float DEFAULT_BACKOFF_MULTIPLIER = 1.5f;

        private boolean mCanInterruptDiscoveryScan = true;
        private final ConnectionRetryPolicy.Builder mConnectionRetryPolicyBuilder =
                new ConnectionRetryPolicy.Builder();
        private boolean mIsSecure = true;
        private UUID mUuid;

        /**
         * Create a builder instance.
         */
        public Builder() {
            mConnectionRetryPolicyBuilder.setRetryCount(DEFAULT_RETRY_COUNT);
            mConnectionRetryPolicyBuilder.setInitialRetryDelay(DEFAULT_INITIAL_RETRY_DELAY_MS);
            mConnectionRetryPolicyBuilder.setBackoffMultiplier(DEFAULT_BACKOFF_MULTIPLIER);
        }

        /**
         * <p>
         * Set whether the connection can interrupt a running discovery scan
         * when it begins, or should wait until the discovery scan has
         * completed.
         * <p>
         *
         * <p>
         * The default value if this is not set is true (i.e. to interrupt).
         * </p>
         *
         * @param canInterruptDiscoveryScan or not.
         *
         * @return builder instance.
         */
        public Builder setCanInterruptDiscoveryScan(boolean canInterruptDiscoveryScan) {
            mCanInterruptDiscoveryScan = canInterruptDiscoveryScan;
            return this;
        }

        /**
         * <p>
         * Set the initial delay in milliseconds between retry attempts.
         * </p>
         *
         * <p>
         * This is how long the first retry will be delayed - any subsequent
         * retries may be delayed by a different amount depending on the value
         * of the backoff multiplier.
         * </p>
         *
         * <p>
         * The default value if this is not set is 2000 milliseconds.
         * </p>
         *
         * @param initialRetryDelay The delay in the maximum number of times to
         *                          retry the connection attempt after the
         *                          initial failure.
         *
         * @return builder instance.
         *
         * @throws IllegalArgumentException if initialRetryDelay is less than
         *                                  zero.
         */
        public Builder setInitialRetryDelay(int initialRetryDelay) {
            mConnectionRetryPolicyBuilder.setInitialRetryDelay(initialRetryDelay);
            return this;
        }

        /**
         * <p>
         * Set the maximum number of times to retry the connection attempt
         * to the device after initial failure.
         * </p>
         *
         * <p>
         * The default value if this is not set is 0 (i.e. no retries).
         * </p>
         *
         * @param retryCount The maximum number of times to retry the
         *                   connection attempt after the initial failure.
         *
         * @return builder instance.
         *
         * @throws IllegalArgumentException if retryCount is less than zero.
         */
        public Builder setRetryCount(int retryCount) {
            mConnectionRetryPolicyBuilder.setRetryCount(retryCount);
            return this;
        }

        /**
         * <p>
         * Set the multiplier to apply to the retry delay after a connection
         * attempt has failed.
         * </p>
         *
         * <p>
         * Note that this multiplier applies from the second failed connection
         * attempt - the first is made using the initial connection retry delay
         * (see {@link #setInitialRetryDelay(int)}.
         * </p>
         *
         * <p>
         * The default value if this is not set is 1.5.
         * </p>
         *
         * @param backoffMultiplier The multiplier to use to modify the
         *                          previous delay for the subsequent retry
         *                          attempt.
         *
         * @return builder instance.
         *
         * @throws IllegalArgumentException if backoffMultiplier is less than
         *                                  zero.
         */
        public Builder setRetryDelayBackoffMultiplier(float backoffMultiplier) {
            if (backoffMultiplier < 0) {
                throw new IllegalArgumentException("Backoff multiplier must be at least 0.");
            }
            mConnectionRetryPolicyBuilder.setBackoffMultiplier(backoffMultiplier);
            return this;
        }

        /**
         * <p>
         * Set whether the connection should be secure or not.
         * </p>
         *
         * <p>
         * The default value for this is true (i.e. secure).
         * </p>
         *
         * <p>
         * NOTE: This method should only be called on API versions 10+. It has
         * no effect when called on lower versions - all connections on such
         * APIs will be secure.
         * </p>
         *
         * @param isSecure or not.
         *
         * @return builder instance.
         */
        @RequiresApi(Build.VERSION_CODES.GINGERBREAD_MR1)
        public Builder setSecure(boolean isSecure) {
            mIsSecure = isSecure;
            return this;
        }

        /**
         * <p>
         * Set the UUID for the service record to connect to on the Bluetooth
         * device.
         * </p>
         *
         * <p>
         * If this is not provided an attempt will be made to determine a
         * suitable value at connection time.
         * </p>
         *
         * @param uuid for the service record to connect to on the Bluetooth
         *             device.
         *
         * @return builder instance.
         */
        public Builder setServiceRecordUuid(@Nullable UUID uuid) {
            mUuid = uuid;
            return this;
        }

        /**
         * Create the connection configuration instance with the set
         * parameters.
         *
         * @return connection configuration instance.
         */
        public ConnectionAttemptConfiguration build() {
            return new ConnectionAttemptConfiguration(new ConnectionConfiguration(mUuid, mIsSecure,
                    mCanInterruptDiscoveryScan, mConnectionRetryPolicyBuilder.build()));
        }
    }
}
