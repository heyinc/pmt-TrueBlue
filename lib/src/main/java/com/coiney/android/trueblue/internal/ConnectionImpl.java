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

import android.bluetooth.BluetoothSocket;
import android.support.annotation.NonNull;

import com.coiney.android.trueblue.Connection;

import net.jcip.annotations.ThreadSafe;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Implementation of the {@link Connection} interface for a connection with a
 * Bluetooth device.
 */
@ThreadSafe
class ConnectionImpl implements Connection {

    private final BluetoothSocket mBluetoothSocket;
    private final InputStream mInputStream;
    private final AtomicBoolean mIsOpen = new AtomicBoolean(true);
    private final Set<OnCloseListener> mOnCloseListeners = new CopyOnWriteArraySet<>();
    private final OutputStream mOutputStream;

    /**
     * Create a new synchronous connection with the provided parameters.
     *
     * @param bluetoothSocket Bluetooth socket backing the connection.
     * @param inputStream of the Bluetooth socket.
     * @param outputStream of the Bluetooth socket.
     */
    ConnectionImpl(@NonNull BluetoothSocket bluetoothSocket, @NonNull InputStream inputStream,
            @NonNull OutputStream outputStream) {
        mBluetoothSocket = bluetoothSocket;
        mInputStream = inputStream;
        mOutputStream = outputStream;
    }

    /**
     * Ask whether the connection is open or not.
     *
     * @return flag indicating whether the connection is open or not.
     */
    @Override
    public boolean isOpen() {
        return mIsOpen.get();
    }

    /**
     * Close the connection.
     *
     * Note that calling this method on a connection which is already closed
     * has no effect.
     */
    @Override
    public void close() {
        closeConnection(false);
    }

    /**
     * Read from the connection into the provided buffer, blocking until the
     * read completes.
     *
     * No guarantee is made that the buffer will be full when this method
     * returns. Rather, the number of bytes actually read is provided as the
     * return value of the method.
     *
     * @param buffer to read data into.
     *
     * @return the number of bytes actually read.
     *
     * @throws IOException upon error.
     */
    @Override
    public int read(@NonNull byte[] buffer) throws IOException {
        try {
            final int numBytesRead;
            synchronized (mInputStream) {
                numBytesRead = mInputStream.read(buffer);
            }
            if (numBytesRead < 0) {
                throw new IOException("Read error - closing connection.");
            }
            return numBytesRead;
        } catch (IOException e) {
            closeConnection(true);
            throw e;
        }
    }

    /**
     * Write the provided data to the connection, blocking until the write
     * completes.
     *
     * @param data to write.
     *
     * @throws IOException upon error.
     */
    @Override
    public void write(@NonNull byte[] data) throws IOException {
        synchronized (mOutputStream) {
            try {
                mOutputStream.write(data);
            } catch (IOException e) {
                closeConnection(true);
                throw e;
            }
        }
    }

    /**
     * Register an on close listener. Be sure to unregister it using {@link
     * #unregisterOnCloseListener(OnCloseListener)} when it is no longer
     * required.
     *
     * @param listener to register.
     */
    @Override
    public void registerOnCloseListener(@NonNull OnCloseListener listener) {
        mOnCloseListeners.add(listener);
    }

    /**
     * Unregister a previously registered on close listener.
     *
     * @param listener to unregister.
     */
    @Override
    public void unregisterOnCloseListener(@NonNull OnCloseListener listener) {
        mOnCloseListeners.remove(listener);
    }

    private void closeConnection(boolean wasClosedByError) {
        if (!mIsOpen.getAndSet(false)) {
            return;
        }
        CloseableUtils.closeSilently(mInputStream);
        CloseableUtils.closeSilently(mOutputStream);
        BluetoothUtils.closeSocketSilently(mBluetoothSocket);
        for (OnCloseListener listener : mOnCloseListeners) {
            listener.onConnectionClosed(this, wasClosedByError);
        }
        mOnCloseListeners.clear();
    }
}
