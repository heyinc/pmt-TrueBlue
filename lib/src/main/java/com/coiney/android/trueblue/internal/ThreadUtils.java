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

import android.os.Handler;
import android.os.Looper;

/**
 * A collection of useful thread / handler related utility methods.
 */
final class ThreadUtils {

    private static final Handler sMainThreadHandler = new Handler(Looper.getMainLooper());

    private ThreadUtils() {
        throw new AssertionError("Instantiation is not supported.");
    }

    /**
     * Post a runnable on the application's main thread.
     *
     * @param runnable to post on the main thread.
     */
    static void postOnMainThread(Runnable runnable) {
        sMainThreadHandler.post(runnable);
    }
}
