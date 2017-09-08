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

import java.io.IOException;

/**
 * <p>
 * A generic connection representation which provides byte stream based
 * bidirectional communication with a connected Bluetooth device.
 * </p>
 *
 * <p>
 * Connections start in the "open" state and can be closed, which results
 * in them entering a permanent "closed" state. Connections may be closed
 * manually, assuming they are open at the time, and will also be
 * automatically closed if a read or write error is encountered. Once closed, a
 * connection cannot be re-opened.
 * </p>
 *
 * <p>
 * If necessary it is possible to listen for connection closure events by
 * registering an {@link OnCloseListener} using {@link
 * #registerOnCloseListener(OnCloseListener)}.
 * </p>
 *
 * <p>
 * All read and write operations provided will be performed synchronously, and
 * will therefore block until complete. As a result, it is the responsibility
 * of the caller to manage threading concerns (i.e. ensuring that the UI thread
 * is never blocked). An asynchronous connection client is also provided which
 * manages threading internally, at least in relation to blocking concerns -
 * see {@link ConnectionClient} for details.
 * </p>
 */
public interface Connection {

    /**
     * Ask whether the connection is open or not.
     *
     * @return flag indicating whether the connection is open or not.
     */
    boolean isOpen();

    /**
     * <p>
     * Close the connection.
     * </p>
     *
     * <p>
     * Note that calling this method on a connection which is already closed
     * has no effect.
     * </p>
     */
    void close();

    /**
     * <p>
     * Read from the connection into the provided buffer, blocking until the
     * read completes.
     * </p>
     *
     * <p>
     * No guarantee is made that the buffer will be full when this method
     * returns. Rather, the number of bytes actually read is provided as the
     * return value of the method.
     * </p>
     *
     * @param buffer to read data into.
     *
     * @return the number of bytes actually read.
     *
     * @throws IOException upon error.
     */
    int read(@NonNull byte[] buffer) throws IOException;

    /**
     * Write the provided data to the connection, blocking until the write
     * completes.
     *
     * @param data to write.
     *
     * @throws IOException upon error.
     */
    void write(@NonNull byte[] data) throws IOException;

    /**
     * Register an on close listener. Be sure to unregister it using {@link
     * #unregisterOnCloseListener(OnCloseListener)} when it is no longer
     * required.
     *
     * @param listener to register.
     */
    void registerOnCloseListener(@NonNull OnCloseListener listener);

    /**
     * Unregister a previously registered on close listener.
     *
     * @param listener to unregister.
     */
    void unregisterOnCloseListener(@NonNull OnCloseListener listener);

    /**
     * Listener interface for connection closure events.
     */
    interface OnCloseListener {

        /**
         * Called when the connection is closed. A flag is provided indicating
         * whether the connection was closed due to an error or not.
         *
         * @param connection which was closed.
         * @param wasClosedByError or not.
         */
        void onConnectionClosed(@NonNull Connection connection,
                boolean wasClosedByError);
    }
}
