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

package com.coiney.android.hellobluetooth;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;

import com.coiney.android.trueblue.BluetoothStatusListener;
import com.coiney.android.trueblue.TrueBlue;

public class BaseBluetoothActivity extends AppCompatActivity implements BluetoothStatusListener {

    private static final String DEFAULT_UNKNOWN_DEVICE_NAME = "<Unknown>";

    private TrueBlue mBluetoothService;
    private Snackbar mSnackbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBluetoothService = TrueBlue.getInstance();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mBluetoothService.registerBluetoothStatusListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mBluetoothService.unregisterBluetoothStatusListener(this);
    }

    @Override
    public void onBluetoothDisabled() {
        handleBluetoothDisabled();
    }

    @Override
    public void onBluetoothEnabled() {
        handleBluetoothEnabled();
    }

    protected void handleBluetoothDisabled() { /* Empty */ }

    protected void handleBluetoothEnabled() { /* Empty */ }

    protected TrueBlue getBluetoothService() {
        return mBluetoothService;
    }

    protected String getDeviceName(BluetoothDevice device) {
        final String deviceName = device.getName();
        return (deviceName != null) ? deviceName : DEFAULT_UNKNOWN_DEVICE_NAME;
    }

    protected void showSnackbar(String message) {
        if (mSnackbar != null && mSnackbar.isShown()) {
            mSnackbar.dismiss();
        }
        mSnackbar = Snackbar.make(findViewById(android.R.id.content), message,
                Snackbar.LENGTH_SHORT);
        mSnackbar.show();
    }
}
