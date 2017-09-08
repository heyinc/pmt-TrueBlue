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

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.coiney.android.trueblue.ConnectionClient;
import com.coiney.android.trueblue.ConnectionAttemptCallback;
import com.coiney.android.trueblue.ConnectionClients;
import com.coiney.android.trueblue.ConnectionAttemptConfiguration;
import com.coiney.android.trueblue.DiscoveryError;
import com.coiney.android.trueblue.DiscoveryListener;
import com.coiney.android.trueblue.TrueBlue;
import com.coiney.android.trueblue.Connection;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemClick;

public class DiscoveryActivity extends BaseBluetoothActivity implements
        DiscoveryListener, ConnectionAttemptCallback {

    private static final int REQUEST_DISCOVERY_PERMISSIONS = 127;

    private TrueBlue mBluetoothService;
    @BindView(R.id.bluetooth_warning) TextView mBluetoothWarning;
    private DeviceListAdapter mDiscoveryResultsAdapter;
    @BindView(R.id.discovery_results_list) ListView mDiscoveryResultsList;
    private boolean mEnablingBluetoothShouldStartDiscovery;
    @BindView(R.id.start_discovery_button) Button mStartButton;
    @BindView(R.id.stop_discovery_button) Button mStopButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discovery);
        ButterKnife.bind(this);
        mBluetoothService = TrueBlue.getInstance();
        if (!mBluetoothService.isBluetoothAvailable()) {
            mBluetoothWarning.setText(R.string.bluetooth_unavailable);
            mBluetoothWarning.setVisibility(View.VISIBLE);
            return;
        }
        mDiscoveryResultsAdapter = new DeviceListAdapter(this, R.layout.device_list_item);
        mDiscoveryResultsList.setAdapter(mDiscoveryResultsAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mBluetoothService.registerDiscoveryListener(this);
        updateBluetoothStatus();
        updateButtons(mBluetoothService.isServiceDiscoveryScanRunning());
    }

    @Override
    protected void onPause() {
        super.onPause();
        mBluetoothService.unregisterDiscoveryListener(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
            @NonNull int[] grantResults) {
        if (requestCode != REQUEST_DISCOVERY_PERMISSIONS) {
            return;
        }
        updateButtons(false);
        if (permissions.length <= 0 || grantResults.length <= 0 || permissions.length !=
                grantResults.length) {
            return;
        }
        for (int i = 0; i < permissions.length; i++) {
            if (permissions[i].equals(Manifest.permission.ACCESS_COARSE_LOCATION) &&
                    PackageManager.PERMISSION_GRANTED == grantResults[i]) {
                startDiscovery();
            }
        }
    }

    @OnClick(R.id.start_discovery_button)
    void startDiscovery() {
        updateButtons(true);
        mDiscoveryResultsAdapter.clear();
        final DiscoveryError error = mBluetoothService.startDiscovery(this);
        if (error != null) {
            updateButtons(false);
            switch (error) {
                case BLUETOOTH_DISABLED:
                    mEnablingBluetoothShouldStartDiscovery = true;
                    mBluetoothService.requestBluetoothBeEnabled(this);
                    break;
                case COARSE_LOCATION_PERMISSION_REQUIRED:
                    requestCoarseLocationPermissions();
                    break;
                default:
                    showSnackbar("Discovery error.");
                    break;
            }
        }
    }

    @OnClick(R.id.stop_discovery_button)
    void stopDiscovery() {
        mStopButton.setEnabled(false);
        mBluetoothService.stopDiscovery();
    }

    @OnItemClick(R.id.discovery_results_list)
    void connectToDevice(AdapterView<?> parent, int position) {
        final BluetoothDevice device = (BluetoothDevice) parent.getItemAtPosition(position);
        if (null == device) {
            return;
        }
        // Tailor connection configuration for pairing.
        final ConnectionAttemptConfiguration connectionConfiguration =
                new ConnectionAttemptConfiguration.Builder().setRetryCount(0)
                        .setCanInterruptDiscoveryScan(false)
                        .build();
        mBluetoothService.connect(device, connectionConfiguration, this);
    }

    // Bluetooth status listener

    @Override
    protected void handleBluetoothDisabled() {
        updateBluetoothStatus();
    }

    @Override
    protected void handleBluetoothEnabled() {
        updateBluetoothStatus();
        if (mEnablingBluetoothShouldStartDiscovery) {
            mEnablingBluetoothShouldStartDiscovery = false;
            startDiscovery();
        }
    }

    // Discovery scan callback

    @Override
    public void onDeviceDiscovered(@NonNull BluetoothDevice device) {
        // Avoid adding the same device to the adapter multiple times.
        if (mDiscoveryResultsAdapter.getPosition(device) < 0) {
            mDiscoveryResultsAdapter.add(device);
        }
    }

    @Override
    public void onDiscoveryFinished() {
        updateButtons(false);
        showSnackbar("Discovery finished.");
    }

    @Override
    public void onDiscoveryStarted() {
        showSnackbar("Discovery started.");
    }

    // Device connection attempt callback

    @Override
    public void onConnectionAttemptCancelled(@NonNull BluetoothDevice device) {
        showSnackbar("Pairing with " + getDeviceName(device) + " cancelled.");
    }

    @Override
    public void onConnectionAttemptFailed(@NonNull BluetoothDevice device) {
    }

    @Override
    public void onConnectionAttemptSucceeded(@NonNull BluetoothDevice device,
            @NonNull Connection connection) {
        // Fake something useful using the connection - required to get close
        // notifications (these only trigger upon read/write error).
        final NoopConnectionHandler noopHandler = new NoopConnectionHandler();
        final ConnectionClient connectionClient = ConnectionClients.wrap(connection, 256,
                noopHandler);
        connectionClient.startReading();
    }

    @Override
    public void onPairingAttemptFailed(@NonNull BluetoothDevice device) {
        showSnackbar("Pairing with " + getDeviceName(device) + " failed.");
    }

    @Override
    public void onPairingAttemptStarted(@NonNull BluetoothDevice device) {
        showSnackbar("Pairing with " + getDeviceName(device) + " ...");
    }

    @Override
    public void onPairingAttemptSucceeded(@NonNull BluetoothDevice device) {
        showSnackbar("Paired with " + getDeviceName(device) + ".");
    }

    private void updateBluetoothStatus() {
        if (mBluetoothService.isBluetoothEnabled()) {
            mBluetoothWarning.setVisibility(View.GONE);
        } else {
            mBluetoothWarning.setText(R.string.bluetooth_disabled);
            mBluetoothWarning.setVisibility(View.VISIBLE);
        }
    }

    private void updateButtons(boolean isDiscoveryRunning) {
        mStartButton.setEnabled(!isDiscoveryRunning);
        mStopButton.setEnabled(isDiscoveryRunning);
    }

    private void requestCoarseLocationPermissions() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)) {
            // TODO Show rationale.
            // return;
        }
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                REQUEST_DISCOVERY_PERMISSIONS);
    }
}
