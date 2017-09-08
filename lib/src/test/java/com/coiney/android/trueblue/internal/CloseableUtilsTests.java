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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.Closeable;
import java.io.IOException;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

/**
 * A collection of useful Closeable related utility methods.
 */
@RunWith(MockitoJUnitRunner.class)
public final class CloseableUtilsTests {

    @Test
    public void closeSucceeds() throws Exception {
        final Closeable closeable = mock(Closeable.class);
        CloseableUtils.closeSilently(closeable);
    }

    @Test
    public void closeFails() throws Exception {
        final Closeable closeable = mock(Closeable.class);
        doThrow(new IOException()).when(closeable).close();
        CloseableUtils.closeSilently(closeable);
    }

    @Test
    public void nullParameter() throws Exception {
        CloseableUtils.closeSilently(null);
    }
}
