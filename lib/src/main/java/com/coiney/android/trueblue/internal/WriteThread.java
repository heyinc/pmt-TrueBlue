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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Thread which writes data to a an output stream as quickly as it can,
 * blocking when no data is available. Results (data and errors) are reported
 * via a callback.
 */
@ThreadSafe
final class WriteThread extends Thread {

    private final Callback mCallback;
    private final Connection mConnection;
    private final BlockingQueue<byte[]> mDataQueue = new LinkedBlockingQueue<>();

    /**
     * Create a write thread with the provided parameters.
     *
     * @param connection to write to.
     * @param callback to report results to.
     */
    WriteThread(@NonNull Connection connection, @NonNull Callback callback) {
        super("TrueBlue-WriteThread");
        mCallback = callback;
        mConnection = connection;
    }

    /**
     * Do not call this method - use {@link #start()} instead.
     */
    @Override
    public void run() {
        byte[] data = null;
        // noinspection InfiniteLoopStatement
        while (!isInterrupted()) {
            try {
                data = mDataQueue.take();
                mConnection.write(data);
                mCallback.onDataWritten(data);
            } catch (IOException e) {
                mCallback.onWriteErrorEncountered(data);
            } catch (InterruptedException e) {
                break;
            }
            data = null;
        }
        mDataQueue.clear();
    }

    /**
     * Write the provided data to the connection. This operation will be
     * performed asynchronously, with the result provided via the callback.
     *
     * @param data to write to the connection.
     */
    void write(@NonNull byte[] data) {
        mDataQueue.add(data);
    }

    /**
     * Callback interface for data write events on a connection.
     */
    public interface Callback {

        /**
         * Called when data has been successfully written to the connection.
         *
         * @param data which was successfully written.
         */
        void onDataWritten(@NonNull byte[] data);

        /**
         * Called when an error has been encountered while writing to the
         * connection.
         */
        void onWriteErrorEncountered(@NonNull byte[] data);
    }
}
