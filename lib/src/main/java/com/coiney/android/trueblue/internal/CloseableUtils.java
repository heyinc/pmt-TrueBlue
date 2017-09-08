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

import android.support.annotation.Nullable;

import java.io.Closeable;
import java.io.IOException;

/**
 * A collection of useful Closeable related utility methods.
 */
final class CloseableUtils {

    /**
     * Close a {@link Closeable} silently, ignoring any exceptions generated in
     * the process.
     *
     * @param closeable to close silently.
     */
    static void closeSilently(@Nullable Closeable closeable) {
        if (null == closeable) {
            return;
        }
        try {
            closeable.close();
        } catch (IOException ignored) {}
    }
}
