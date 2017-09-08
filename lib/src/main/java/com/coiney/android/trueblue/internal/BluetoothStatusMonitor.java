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

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;

import com.coiney.android.trueblue.BluetoothStatusListener;
import net.jcip.annotations.ThreadSafe;

import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Monitors the status of Bluetooth on the device and provides appropriate
 * notifications to all registered listeners when Bluetooth is enabled or
 * disabled.
 */
@ThreadSafe
public class BluetoothStatusMonitor extends BroadcastReceiver {

    private final Context mContext;
    private final CopyOnWriteArraySet<BluetoothStatusListener> mListeners =
            new CopyOnWriteArraySet<>();

    /**
     * Create a Bluetooth status monitor with the provided parameters.
     *
     * @param context to use to register listeners.
     */
    public BluetoothStatusMonitor(@NonNull Context context) {
        mContext = context.getApplicationContext();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!BluetoothAdapter.ACTION_STATE_CHANGED.equals(intent.getAction())) {
            return;
        }
        switch (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF)) {
            case BluetoothAdapter.STATE_ON:
                for (BluetoothStatusListener listener : mListeners) {
                    listener.onBluetoothEnabled();
                }
                break;
            case BluetoothAdapter.STATE_OFF:
                for (BluetoothStatusListener listener : mListeners) {
                    listener.onBluetoothDisabled();
                }
                break;
            default:
                break;
        }
    }

    /**
     * Start monitoring the Bluetooth subsystem for adapter status changes.
     */
    public void start() {
        ContextUtils.unregisterReceiverSilently(mContext, this);
        mContext.registerReceiver(this, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
    }

    /**
     * Stop monitoring the Bluetooth subsystem for adapter status changes.
     */
    public void stop() {
        ContextUtils.unregisterReceiverSilently(mContext, this);
        mListeners.clear();
    }

    /**
     * Register a Bluetooth status listener. Be sure to call
     * {@link #unregisterListener(BluetoothStatusListener)} when the listener
     * is no longer required.
     *
     * @param listener to register.
     */
    void registerListener(@NonNull BluetoothStatusListener listener) {
        mListeners.add(listener);
    }

    /**
     * Unregister a previously registered Bluetooth status listener.
     *
     * @param listener to unregister.
     */
    void unregisterListener(@NonNull BluetoothStatusListener listener) {
        mListeners.remove(listener);
    }
}
