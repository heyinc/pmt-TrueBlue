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

package com.coiney.android.trueblue.internal;

import android.bluetooth.BluetoothSocket;

import com.coiney.android.trueblue.*;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.quality.Strictness;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class ConnectionImplTests {

    // Ensure that Mockito is used despite using Robolectric's test runner.
    @Rule
    public MockitoRule mMockitoRule = MockitoJUnit.rule().strictness(Strictness.WARN);

    @Mock
    private BluetoothSocket mBluetoothSocket;
    private ByteArrayOutputStream mOutputStream;

    @Before
    public void setUp() {
        mOutputStream = new ByteArrayOutputStream();
    }

    @Test
    public void isOpenWhenConnectionCreatedShouldReturnTrue() {
        final ConnectionImpl connection = prepareConnection(null);
        assertTrue(connection.isOpen());
    }

    @Test
    public void isOpenWhenConnectionClosedShouldReturnFalse() {
        final ConnectionImpl connection = prepareConnection(null);
        connection.close();
        assertFalse(connection.isOpen());
    }

    @Test
    public void onCloseListenerWhenRegisteredAndConnectionClosedShouldReceiveNotification() {
        final ConnectionImpl connection = prepareConnection(null);
        final Connection.OnCloseListener listener =
                mock(Connection.OnCloseListener.class);
        connection.registerOnCloseListener(listener);
        connection.close();
        verify(listener, times(1)).onConnectionClosed(connection, false);
    }

    @Test
    public void closeIsIdempotent() throws Exception {
        final ConnectionImpl connection = prepareConnection(null);
        final Connection.OnCloseListener listener =
                mock(Connection.OnCloseListener.class);
        connection.registerOnCloseListener(listener);
        connection.close();
        connection.close();
        verify(listener, times(1)).onConnectionClosed(connection, false);
    }

    @Test
    public void readWhenInputStreamNotClosedAndDataAvailableShouldBytesRead() throws Exception {
        final byte[] data = new byte[]{ 0x00, 0x01, 0x02, 0x03, 0x04, 0x05 };
        final ConnectionImpl connection = prepareConnection(new ByteArrayInputStream(data));
        final byte[] buffer = new byte[data.length + 1];
        final int numBytes = connection.read(buffer);
        // We use a ByteArrayInputStream here, which is not ideal. Unlike the
        // InputStream returned from BluetoothSocket#getInputStream(), it does
        // not block, and is guaranteed to return all of its data in a single
        // call if the buffer provided is larger than the amount of data
        // available. It is better than nothing for basic testing, though, so
        // long as these limitations are acknowledged.
        assertEquals(data.length, numBytes);
        assertTrue(Arrays.equals(data, Arrays.copyOfRange(buffer, 0, numBytes)));
    }

    @Test
    public void readWhenInputStreamIsClosedShouldThrowException() throws Exception {
        final InputStream inputStream = new ByteArrayInputStream(new byte[0]);
        final ConnectionImpl connection = prepareConnection(inputStream);
        inputStream.close();
        try {
            connection.read(new byte[1]);
            fail("Exception should have been thrown.");
        } catch (IOException ignored) {}
    }

    @Test
    public void readWhenInputStreamIsClosedShouldCloseConnection() throws Exception {
        final InputStream inputStream = new ByteArrayInputStream(new byte[0]);
        final ConnectionImpl connection = prepareConnection(inputStream);
        final Connection.OnCloseListener listener =
                mock(Connection.OnCloseListener.class);
        connection.registerOnCloseListener(listener);
        inputStream.close();
        try {
            connection.read(new byte[1]);
        } catch (IOException ignored) {}
        verify(listener, times(1)).onConnectionClosed(connection, true);
    }

    @Test
    public void writeWhenOutputStreamIsNotClosedShouldWriteData() throws Exception {
        final ConnectionImpl connection = prepareConnection(null);
        final byte[] data = new byte[]{ 0x00, 0x01, 0x02, 0x03, 0x04, 0x05 };
        connection.write(data);
        mOutputStream.flush();
        assertTrue(Arrays.equals(data, mOutputStream.toByteArray()));
    }

    @Test
    public void writeWhenOutputStreamIsClosedShouldThrowException() throws Exception {
        // ByteArrayOutputStream#close() does nothing, so unfortunately we have
        // to get creative ...
        final ByteArrayOutputStream outputStream = spy(new ByteArrayOutputStream());
        Mockito.doThrow(new IOException()).when(outputStream).write(any(byte[].class));
        final ConnectionImpl connection = new ConnectionImpl(mBluetoothSocket,
                new ByteArrayInputStream(new byte[0]), outputStream);
        try {
            connection.write(new byte[]{ 0x00, 0x01, 0x02, 0x03, 0x04, 0x05 });
            fail("Exception should have been thrown.");
        } catch (IOException ignored) {}
    }

    @Test
    public void writeWhenOutputStreamIsClosedShouldCloseConnection() throws Exception {
        // ByteArrayOutputStream#close() does nothing, so unfortunately we have
        // to get creative ...
        final ByteArrayOutputStream outputStream = spy(new ByteArrayOutputStream());
        Mockito.doThrow(new IOException()).when(outputStream).write(any(byte[].class));
        final ConnectionImpl connection = new ConnectionImpl(mBluetoothSocket,
                new ByteArrayInputStream(new byte[0]), outputStream);
        final Connection.OnCloseListener listener =
                mock(Connection.OnCloseListener.class);
        connection.registerOnCloseListener(listener);
        mOutputStream.close();
        try {
            connection.write(new byte[]{ 0x00, 0x01, 0x02, 0x03, 0x04, 0x05 });
        } catch (IOException ignored) {}
        verify(listener, times(1)).onConnectionClosed(connection, true);
    }

    private ConnectionImpl prepareConnection(InputStream inputStream) {
        return new ConnectionImpl(mBluetoothSocket, inputStream, mOutputStream);
    }
}
