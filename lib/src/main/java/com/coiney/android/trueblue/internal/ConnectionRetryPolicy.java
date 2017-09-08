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

import net.jcip.annotations.NotThreadSafe;

/**
 * Controls all aspects of retrying connection attempts to a device upon
 * failure. This class cannot be instantiated directly - instead, use the
 * contained Builder class.
 *
 * Heavily inspired by the RetryPolicy interface in the Volley library.
 */
@NotThreadSafe
public class ConnectionRetryPolicy {

    private final float mRetryDelayBackoffMultiplier;
    private int mCurrentRetryDelay;
    private int mNumberOfRetriesAttempted;
    private final int mMaximumNumberOfRetries;

    private ConnectionRetryPolicy(int initialRetryDelay, int maximumNumberOfRetries,
            float retryDelayBackoffMultiplier) {
        mCurrentRetryDelay = initialRetryDelay;
        mMaximumNumberOfRetries = maximumNumberOfRetries;
        mRetryDelayBackoffMultiplier = retryDelayBackoffMultiplier;
        mNumberOfRetriesAttempted = 0;
    }

    int getCurrentRetryDelay() {
        return mCurrentRetryDelay;
    }

    int getRemainingRetryCount() {
        return mMaximumNumberOfRetries - mNumberOfRetriesAttempted;
    }

    boolean hasAttemptRemaining() {
        return mNumberOfRetriesAttempted < mMaximumNumberOfRetries;
    }

    int getNumberOfRetriesAttempted() {
        return mNumberOfRetriesAttempted;
    }

    void retry() {
        if (!hasAttemptRemaining()) {
            throw new IllegalStateException("Maximum number of retries has been exceeded.");
        }
        if (mNumberOfRetriesAttempted > 0) {
            mCurrentRetryDelay = (int) (mCurrentRetryDelay * mRetryDelayBackoffMultiplier);
        }
        mNumberOfRetriesAttempted++;
    }

    /**
     * Facilitates creation of custom retry policies.
     */
    public static final class Builder {

        private static final int DEFAULT_RETRY_COUNT = 0;
        private static final int DEFAULT_RETRY_DELAY = 2000;
        private static final float DEFAULT_BACKOFF_MULTIPLIER = 1.5f;

        private int mRetryCount = DEFAULT_RETRY_COUNT;
        private int mInitialRetryDelay = DEFAULT_RETRY_DELAY;
        private float mBackoffMultiplier = DEFAULT_BACKOFF_MULTIPLIER;

        /**
         * Create a retry policy builder.
         */
        public Builder() { /* Empty */ }

        /**
         * Set the maximum number of times to retry the connection attempt
         * to the device after initial failure.
         *
         * The default if this is not set is 0 (i.e. no retries).
         *
         * @param retryCount The maximum number of times to retry the
         *                   connection attempt after the initial failure.
         */
        public Builder setRetryCount(int retryCount) {
            if (retryCount < 0) {
                throw new IllegalArgumentException("Retry count must be at least 0");
            }
            mRetryCount = retryCount;
            return this;
        }

        /**
         * Set the initial delay in milliseconds between retry attempts (this
         * is how long the first retry will be delayed - any subsequent retries
         * may be delayed by a different amount depending on the value of the
         * backoff multiplier).
         *
         * The default if this is not set is 2000 milliseconds.
         *
         * @param initialRetryDelay The delay in the maximum number of times to
         *                          retry the connection attempt after the
         *                          initial failure.
         */
        public Builder setInitialRetryDelay(int initialRetryDelay) {
            if (initialRetryDelay < 0) {
                throw new IllegalArgumentException("Initial retry delay must be positive.");
            }
            mInitialRetryDelay = initialRetryDelay;
            return this;
        }

        /**
         * Set the maximum number of times to retry the connection attempt
         * to the device after initial failure.
         *
         * The default if this is not set is 1.5.
         *
         * @param backoffMultiplier The multiplier to use to modify the
         *                          previous delay for the subsequent
         *                          retry attempt.
         */
        public Builder setBackoffMultiplier(float backoffMultiplier) {
            if (backoffMultiplier < 0) {
                throw new IllegalArgumentException("Backoff multiplier must be at least 0.");
            }
            mBackoffMultiplier = backoffMultiplier;
            return this;
        }

        /**
         * Create the connection retry policy being built.
         */
        public ConnectionRetryPolicy build() {
            return new ConnectionRetryPolicy(mInitialRetryDelay, mRetryCount, mBackoffMultiplier);
        }
    }
}
