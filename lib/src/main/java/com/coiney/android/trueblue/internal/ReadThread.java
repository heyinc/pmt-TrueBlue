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

import com.coiney.android.trueblue.Connection;

import net.jcip.annotations.ThreadSafe;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * Thread which reads continuously from an {@link InputStream} into a buffer,
 * blocking when no data is available. Results (data and errors) are reported
 * via a callback.
 */
@ThreadSafe
final class ReadThread extends Thread {

    private final Callback mCallback;
    private final Connection mConnection;
    private final int mReadBufferSize;

    /**
     * Create a read thread with the provided parameters.
     *
     * @param connection from which to read.
     * @param readBufferSize for the buffer to read into.
     * @param callback to report results to.
     */
    ReadThread(@NonNull Connection connection, int readBufferSize,
            @NonNull Callback callback) {
        super("TrueBlue-ReadThread");
        mCallback = callback;
        mConnection = connection;
        mReadBufferSize = readBufferSize;
    }

    /**
     * Do not call this method - use {@link #start()} instead.
     */
    @Override
    public void run() {
        final byte[] buffer = new byte[mReadBufferSize];
        int numberOfBytesRead;
        try {
            while ((numberOfBytesRead = mConnection.read(buffer)) > -1) {
                mCallback.onDataRead(Arrays.copyOfRange(buffer, 0, numberOfBytesRead));
            }
        } catch (IOException e) {
            mCallback.onReadErrorEncountered();
        }
    }

    /**
     * Callback interface for data read events on a connection.
     */
    public interface Callback {

        /**
         * Called when data has been successfully read from the connection.
         *
         * @param bytes read.
         */
        void onDataRead(@NonNull byte[] bytes);

        /**
         * Called when an error has been encountered while reading from the
         * connection.
         */
        void onReadErrorEncountered();
    }
}
