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

/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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

import android.bluetooth.BluetoothDevice;
import android.support.annotation.NonNull;
import android.util.Log;

import net.jcip.annotations.ThreadSafe;

import java.util.Locale;

/**
 * Logging helper class. To see debug log entries, remember to configure your device
 * as follows:
 *
 * {@code <android-sdk>/platform-tools/adb shell setprop log.tag.TrueBlue DEBUG}
 *
 * This class is heavily based on VolleyLog in the Volley project. See:
 * https://android.googlesource.com/platform/frameworks/volley
 */
@ThreadSafe
public class Logger {

    private final String mTag;

    /**
     * Create a logger with the provided log tag.
     *
     * @param tag to use when logging.
     */
    public Logger(@NonNull String tag) {
        mTag = tag;
    }

    /**
     * Log a verbose level message with the provided format and arguments.
     *
     * Note that logging will only occur if enabled for the given tag and
     * log level, as determined by {@link Log#isLoggable(String, int)}.
     *
     * @param format for the message.
     * @param args to apply to the message format.
     */
    public void v(String format, Object... args) {
        if (Log.isLoggable(mTag, Log.VERBOSE)) {
            Log.v(mTag, buildMessage(format, args));
        }
    }

    /**
     * Log a debug level message with the provided format and arguments.
     *
     * Note that logging will only occur if enabled for the given tag and
     * log level, as determined by {@link Log#isLoggable(String, int)}.
     *
     * @param format for the message.
     * @param args to apply to the message format.
     */
    public void d(String format, Object... args) {
        if (Log.isLoggable(mTag, Log.DEBUG)) {
            Log.d(mTag, buildMessage(format, args));
        }
    }

    /**
     * Log a debug level message related to the provided Bluetooth device and
     * with the provided format and arguments.
     *
     * Note that logging will only occur if enabled for the given tag and
     * log level, as determined by {@link Log#isLoggable(String, int)}.
     *
     * @param device to associate with the message.
     * @param format for the message.
     * @param args to apply to the message format.
     */
    public void d(BluetoothDevice device, String format, Object... args) {
        if (Log.isLoggable(mTag, Log.DEBUG)) {
            Log.d(mTag, buildMessage(device, format, args));
        }
    }

    /**
     * Log an informational level message with the provided format and
     * arguments.
     *
     * Note that logging will only occur if enabled for the given tag and
     * log level, as determined by {@link Log#isLoggable(String, int)}.
     *
     * @param format for the message.
     * @param args to apply to the message format.
     */
    public void i(String format, Object... args) {
        if (Log.isLoggable(mTag, Log.INFO)) {
            Log.i(mTag, buildMessage(format, args));
        }
    }

    /**
     * Log a warning level message with the provided format and arguments.
     *
     * Note that logging will only occur if enabled for the given tag and
     * log level, as determined by {@link Log#isLoggable(String, int)}.
     *
     * @param format for the message.
     * @param args to apply to the message format.
     */
    public void w(String format, Object... args) {
        if (Log.isLoggable(mTag, Log.WARN)) {
            Log.w(mTag, buildMessage(format, args));
        }
    }

    /**
     * Log an error level message with the provided format and arguments.
     *
     * Note that logging will only occur if enabled for the given tag and
     * log level, as determined by {@link Log#isLoggable(String, int)}.
     *
     * @param format for the message.
     * @param args to apply to the message format.
     */
    public void e(String format, Object... args) {
        if (Log.isLoggable(mTag, Log.ERROR)) {
            Log.e(mTag, buildMessage(format, args));
        }
    }

    /**
     * Log an error level message with the provided throwable, format and
     * arguments.
     *
     * Note that logging will only occur if enabled for the given tag and
     * log level, as determined by {@link Log#isLoggable(String, int)}.
     *
     * @param throwable encountered.
     * @param format for the message.
     * @param args to apply to the message format.
     */
    public void e(Throwable throwable, String format, Object... args) {
        if (Log.isLoggable(mTag, Log.ERROR)) {
            Log.e(mTag, buildMessage(format, args), throwable);
        }
    }

    /**
     * Log a WTF level message with the provided format and arguments.
     *
     * Note that logging will only occur if enabled for the given tag and
     * log level, as determined by {@link Log#isLoggable(String, int)}.
     *
     * @param format for the message.
     * @param args to apply to the message format.
     */
    public void wtf(String format, Object... args) {
        Log.wtf(mTag, buildMessage(format, args));
    }

    /**
     * Log a WTF level message with the provided throwable, format and
     * arguments.
     *
     * Note that logging will only occur if enabled for the given tag and
     * log level, as determined by {@link Log#isLoggable(String, int)}.
     *
     * @param throwable encountered.
     * @param format for the message.
     * @param args to apply to the message format.
     */
    public void wtf(Throwable throwable, String format, Object... args) {
        Log.wtf(mTag, buildMessage(format, args), throwable);
    }

    private static String buildMessage(String format, Object... args) {
        final String message = (args == null) ? format : String.format(Locale.US, format, args);
        return String.format(Locale.US, "[%d] %s", Thread.currentThread().getId(), message);
    }

    private static String buildMessage(BluetoothDevice device, String format, Object... args) {
        final String deviceName = device.getName();
        final String deviceInfo = "[" + (deviceName != null ? deviceName : "<Unknown>") + "][" +
                device.getAddress() + "]";
        final String message = (args == null) ? format : String.format(Locale.US, format, args);
        return String.format(Locale.US, "[%d]%s %s", Thread.currentThread().getId(), deviceInfo,
                message);
    }

//    /**
//     * Formats the caller's provided message and prepends useful info like
//     * calling thread ID and method name.
//     */
//    private static String buildMessage(String format, Object... args) {
//        String msg = (args == null) ? format : String.format(Locale.US, format, args);
//        StackTraceElement[] trace = new Throwable().fillInStackTrace().getStackTrace();
//        String caller = "<unknown>";
//        // Walk up the stack looking for the first caller outside of Logger.
//        // It will be at least two frames up, so startDiscovery there.
//        for (int i = 2; i < trace.length; i++) {
//            Class<?> klass = trace[i].getClass();
//            if (!klass.equals(Logger.class)) {
//                String callingClass = trace[i].getClassName();
//                callingClass = callingClass.substring(callingClass.lastIndexOf('.') + 1);
//                callingClass = callingClass.substring(callingClass.lastIndexOf('$') + 1);
//
//                caller = callingClass + "." + trace[i].getMethodName();
//                break;
//            }
//        }
//        return String.format(Locale.US, "[%d] %s: %s",
//                Thread.currentThread().getId(), caller, msg);
//    }
}
