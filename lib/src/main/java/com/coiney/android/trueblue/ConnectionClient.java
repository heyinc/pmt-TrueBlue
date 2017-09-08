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

/**
 * <p>
 * A simple client which manages a {@link Connection} instance and carries out
 * basic operations on it asynchronously.
 * </p>
 *
 * <p>
 * The results of these operations are reported via {@link Callback}. Note that
 * all of the methods in the callback interface will be called from worker
 * threads.
 * </p>
 */
public interface ConnectionClient {

    /**
     * Ask whether the connection being managed is open or not.
     *
     * @return flag indicating whether the connection being managed is open or
     *         not.
     */
    boolean isOpen();

    /**
     * <p>
     * Close the connection being managed.
     * </p>
     *
     * <p>
     * Upon completion the {@link
     * Callback#onConnectionClosed(ConnectionClient, boolean)} method will
     * be called.
     * </p>
     *
     * <p>
     * Note that calling this method when the connection being managed has
     * already been closed (be it via this method or otherwise) has no effect.
     * </p>
     */
    void close();

    /**
     * <p>
     * Start reading continuously and asynchronously from the connection being
     * managed.
     * </p>
     *
     * <p>
     * The reading will continue until the connection is closed, be it
     * volitionally or as the result of an error being encountered (be it while
     * reading or writing). Note that no data will be read from the connection
     * until this method is called.
     * </p>
     *
     * <p>
     * Read data and any errors encountered will be provided via {@link
     * Callback#onDataRead(ConnectionClient, byte[])} and {@link
     * Callback#onReadErrorEncountered(ConnectionClient)} respectively.
     * </p>
     *
     * <p>
     * Note that calling this when reading has already been started will have
     * no effect.
     * </p>
     *
     * @throws IllegalStateException if the connection has been closed.
     */
    void startReading();

    /**
     * <p>
     * Write the provided data asynchronously to the connection being managed.
     * </p>
     *
     * <p>
     * The result of the operation will be provided via the callback - either
     * {@link Callback#onDataWritten(ConnectionClient, byte[])} upon
     * success or
     * {@link Callback#onWriteErrorEncountered(ConnectionClient, byte[])}
     * upon failure.
     * </p>
     *
     * <p>
     * Note that for efficiency's sake the data provided is not copied. Do not
     * modify the data after calling this method.
     * </p>
     *
     * @param data to write.
     *
     * @throws IllegalStateException if the connection has been closed.
     */
    void write(@NonNull byte[] data);

    /**
     * Callback interface which reports the results of asynchronous operations
     * carried out on the connection.
     */
    interface Callback {

        /**
         * Called when the connection the client is managing is closed.
         *
         * @param client managing the connection which was closed.
         * @param wasClosedByError or not.
         */
        void onConnectionClosed(@NonNull ConnectionClient client, boolean wasClosedByError);

        /**
         * Called when data has been successfully read from the connection the
         * client is managing.
         *
         * @param client which read the data.
         * @param data read.
         */
        void onDataRead(@NonNull ConnectionClient client, @NonNull byte[] data);

        /**
         * Called when data has been successfully written to the connection the
         * client is managing.
         *
         * @param client which wrote the data.
         * @param data written.
         */
        void onDataWritten(@NonNull ConnectionClient client, @NonNull byte[] data);

        /**
         * Called when a read error has been encountered on the connection the
         * client is managing.
         *
         * @param client which encountered the read error.
         */
        void onReadErrorEncountered(@NonNull ConnectionClient client);

        /**
         * Called when a write error has been encountered on the connection the
         * client is managing.
         *
         * @param client which encountered the write error.
         * @param data for which the write failed.
         */
        void onWriteErrorEncountered(@NonNull ConnectionClient client, @NonNull byte[] data);
    }
}
