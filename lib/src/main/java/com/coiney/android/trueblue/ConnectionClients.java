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

import android.support.annotation.NonNull;

import com.coiney.android.trueblue.internal.ConnectionClientImpl;

/**
 * Contains various methods related to {@link ConnectionClient}s.
 */
public final class ConnectionClients {

    private ConnectionClients() {
        throw new AssertionError("Instantiation is not supported.");
    }

    /**
     * <p>
     * Wrap the provided {@link Connection} instance in a {@link
     * ConnectionClient} for easier management and access to asynchronous
     * operations.
     * </p>
     *
     * <p>
     * NOTE: Do not create more than one connection client for any given
     * connection instance.
     * </p>
     *
     * @param connection to manage.
     * @param readBufferSize to use when reading data.
     * @param callback to report the results of asynchronous operations to.
     *
     * @return connection client.
     *
     * @throws IllegalArgumentException if readBufferSize is less than 1.
     */
    @NonNull
    public static ConnectionClient wrap(@NonNull Connection connection,
            int readBufferSize, @NonNull ConnectionClient.Callback callback) {
        if (readBufferSize <= 0) {
            throw new IllegalArgumentException("readBufferSize must be at least 1.");
        }
        return new ConnectionClientImpl(connection, readBufferSize, callback);
    }
}
