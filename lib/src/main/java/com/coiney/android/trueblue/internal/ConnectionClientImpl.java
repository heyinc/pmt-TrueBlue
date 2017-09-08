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
import com.coiney.android.trueblue.ConnectionClient;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

/**
 * Basic implementation of the {@link ConnectionClient} interface.
 */
@ThreadSafe
public class ConnectionClientImpl implements ConnectionClient, ReadThread.Callback,
        WriteThread.Callback, Connection.OnCloseListener {

    private final Callback mCallback;
    private final int mReadBufferSize;
    private final Object mReadLock = new Object();
    @GuardedBy("mReadLock")
    private ReadThread mReadThread;
    private final Connection mConnection;
    private final Object mWriteLock = new Object();
    @GuardedBy("mWriteLock")
    private WriteThread mWriteThread;

    /**
     * Create a new asynchronous connection client with the provided parameters.
     *
     * @param connection to manage.
     * @param readBufferSize to use when reading.
     * @param callback to report operation results to.
     */
    public ConnectionClientImpl(@NonNull Connection connection, int readBufferSize,
            @NonNull Callback callback) {
        mCallback = callback;
        mReadBufferSize = readBufferSize;
        mConnection = connection;
        mConnection.registerOnCloseListener(this);
    }

    /**
     * Ask whether the connection being managed is open or not.
     *
     * @return flag indicating whether the connection being managed is open or
     *         not.
     */
    @Override
    public boolean isOpen() {
        return mConnection.isOpen();
    }

    /**
     * <p>
     * Close the connection being managed.
     * </p>
     *
     * <p>
     * Upon completion the {@link
     * ConnectionClient.Callback#onConnectionClosed(ConnectionClient, boolean)}
     * method will be called.
     * </p>
     *
     * <p>
     * Note that calling this method when the connection being managed has
     * already been closed (be it via this method or otherwise) has no effect.
     * </p>
     */
    @Override
    public void close() {
        mConnection.close();
    }

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
     * ConnectionClient.Callback#onDataRead(ConnectionClient, byte[])} and
     * {@link Callback#onReadErrorEncountered(ConnectionClient)} respectively.
     * </p>
     *
     * <p>
     * Note that calling this when reading has already been started will have
     * no effect.
     * </p>
     *
     * @throws IllegalStateException if the connection has been closed.
     */
    @Override
    public void startReading() {
        synchronized (mReadLock) {
            if (!isOpen()) {
                throw new IllegalStateException("Connection has been closed.");
            }
            if (mReadThread != null) {
                return;
            }
            mReadThread = new ReadThread(mConnection, mReadBufferSize, this);
            mReadThread.start();
        }
    }

    /**
     * <p>
     * Write the provided data asynchronously to the connection being managed.
     * </p>
     *
     * <p>
     * The result of the operation will be provided via the callback - either
     * {@link ConnectionClient.Callback#onDataWritten(ConnectionClient, byte[])}
     * upon success or {@link
     * ConnectionClient.Callback#onWriteErrorEncountered(ConnectionClient, byte[])}
     * upon failure.
     * </p>
     *
     * @param data to write.
     *
     * @throws IllegalStateException if the connection has been closed.
     */
    @Override
    public void write(@NonNull byte[] data) {
        synchronized (mWriteLock) {
            if (!isOpen()) {
                throw new IllegalStateException("Connection has been closed.");
            }
            if (mWriteThread == null) {
                mWriteThread = new WriteThread(mConnection, this);
                mWriteThread.start();
            }
            mWriteThread.write(data);
        }
    }

    @Override
    public void onConnectionClosed(@NonNull Connection connection,
            boolean wasClosedByError) {
        if (!isOpen()) {
            return;
        }
        mConnection.unregisterOnCloseListener(this);
        synchronized (mReadLock) {
            if (mReadThread != null) {
                mReadThread.interrupt();
                mReadThread = null;
            }
        }
        synchronized (mWriteLock) {
            if (mWriteThread != null) {
                mWriteThread.interrupt();
                mWriteThread = null;
            }
        }
        mCallback.onConnectionClosed(this, wasClosedByError);
    }

    @Override
    public void onDataRead(@NonNull byte[] bytes) {
        mCallback.onDataRead(this, bytes);
    }

    @Override
    public void onReadErrorEncountered() {
        mCallback.onReadErrorEncountered(this);
    }

    @Override
    public void onDataWritten(@NonNull byte[] data) {
        mCallback.onDataWritten(this, data);
    }

    @Override
    public void onWriteErrorEncountered(@NonNull byte[] data) {
        mCallback.onWriteErrorEncountered(this, data);
    }
}
