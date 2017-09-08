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
import android.content.Intent;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.coiney.android.trueblue.ConnectionAttemptCallback;
import com.coiney.android.trueblue.ConnectionClient;
import com.coiney.android.trueblue.ConnectionClients;
import com.coiney.android.trueblue.ConnectionAttemptConfiguration;
import com.coiney.android.trueblue.DeviceConnectionListener;
import com.coiney.android.trueblue.TrueBlue;
import com.coiney.android.trueblue.Connection;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemSelected;

public class MainActivity extends BaseBluetoothActivity implements DeviceConnectionListener,
        ConnectionAttemptCallback {

    private TrueBlue mBluetoothService;
    @BindView(R.id.bluetooth_warning) TextView mBluetoothWarning;
    @BindView(R.id.connect_button) Button mConnectButton;
    @BindView(R.id.disconnect_all_button) Button mDisconnectAllButton;
    @BindView(R.id.disconnect_button) Button mDisconnectButton;
    @BindView(R.id.paired_device_list) Spinner mPairedDeviceList;
    DeviceListAdapter mPairedDeviceListAdapter;
    @BindView(R.id.update_paired_device_list_button) Button mUpdatePairedDeviceListButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mBluetoothService = TrueBlue.getInstance();
        if (!getBluetoothService().isBluetoothAvailable()) {
            mBluetoothWarning.setText(R.string.bluetooth_unavailable);
            mBluetoothWarning.setVisibility(View.VISIBLE);
            return;
        }
        mPairedDeviceListAdapter = new DeviceListAdapter(getApplicationContext(),
                R.layout.device_list_item);
        mDisconnectAllButton.setEnabled(true);
        mUpdatePairedDeviceListButton.setEnabled(true);
        mPairedDeviceList.setAdapter(mPairedDeviceListAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getBluetoothService().registerDeviceConnectionListener(this);
        updateBluetoothInformation();
    }

    @Override
    protected void onPause() {
        super.onPause();
        getBluetoothService().unregisterDeviceConnectionListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.discovery) {
            startActivity(new Intent(MainActivity.this, DiscoveryActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.connect_button)
    void connectToDevice(View v) {
        v.setEnabled(false);
        final BluetoothDevice device = (BluetoothDevice) mPairedDeviceList.getSelectedItem();
        if (device != null) {
            final ConnectionAttemptConfiguration connectionConfiguration =
                    new ConnectionAttemptConfiguration.Builder().setRetryCount(2)
                            .setInitialRetryDelay(1500)
                            .setRetryDelayBackoffMultiplier(1.25f)
                            .build();
            mBluetoothService.connect(device, connectionConfiguration, this);
        }
    }

    @OnClick(R.id.disconnect_button)
    void disconnectFromDevice(View v) {
        v.setEnabled(false);
        final BluetoothDevice device = (BluetoothDevice) mPairedDeviceList.getSelectedItem();
        if (device != null) {
            mBluetoothService.disconnect(device);
        }
    }

    @OnClick(R.id.disconnect_all_button)
    void disconnectFromAllDevices() {
        mBluetoothService.disconnectAll();
    }

    @OnClick(R.id.update_paired_device_list_button)
    void updatePairedDeviceList() {
        if (!mBluetoothService.isBluetoothEnabled()) {
            mBluetoothService.requestBluetoothBeEnabled(this);
        } else {
            updatePairedDevicesList();
        }
    }

    @OnItemSelected(R.id.paired_device_list)
    void updateConnectivityButtons(AdapterView<?> parent, int position) {
        final BluetoothDevice device = (BluetoothDevice) parent.getItemAtPosition(position);
        setConnectionButtonStatuses(device);
    }

    @Override
    public void onConnectionAttemptCancelled(@NonNull BluetoothDevice device) {
        setConnectionButtonStatusesIfSelected(device);
        showSnackbar("Connection to " + getDeviceName(device) + " cancelled.");
    }

    @Override
    public void onConnectionAttemptFailed(@NonNull BluetoothDevice device) {
        setConnectionButtonStatusesIfSelected(device);
        showSnackbar("Connection to " + getDeviceName(device) + " failed.");
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
        setConnectionButtonStatusesIfSelected(device);
        showSnackbar("Connected to " + getDeviceName(device) + ".");
    }

    @Override
    public void onPairingAttemptFailed(@NonNull BluetoothDevice device) {
        // Should never happen - this activity only deals with connections to
        // devices which have already been paired with.
    }

    @Override
    public void onPairingAttemptStarted(@NonNull BluetoothDevice device) {
        // Should never happen - this activity only deals with connections to
        // devices which have already been paired with.
    }

    @Override
    public void onPairingAttemptSucceeded(@NonNull BluetoothDevice device) {
        // Should never happen - this activity only deals with connections to
        // devices which have already been paired with.
    }

    @Override
    public void onDeviceConnected(@NonNull final BluetoothDevice device) {
        // Empty - we are connecting so we will handle this in the
        // onConnectionAttemptSucceeded callback.
    }

    @Override
    public void onDeviceDisconnected(@NonNull final BluetoothDevice device,
            final boolean wasCausedByError) {
        setConnectionButtonStatusesIfSelected(device);
        showSnackbar("Disconnected from " + getDeviceName(device));
    }

    @Override
    protected void handleBluetoothDisabled() {
        updateBluetoothInformation();
    }

    @Override
    protected void handleBluetoothEnabled() {
        updateBluetoothInformation();
    }

    private void updateBluetoothInformation() {
        if (mBluetoothService.isBluetoothEnabled()) {
            mBluetoothWarning.setVisibility(View.GONE);
        } else {
            mBluetoothWarning.setText(R.string.bluetooth_unavailable);
            mBluetoothWarning.setVisibility(View.VISIBLE);
        }
        updatePairedDevicesList();
    }

    private void updatePairedDevicesList() {
        mPairedDeviceListAdapter.clear();
        if (!mBluetoothService.isBluetoothEnabled()) {
            return;
        }
        for (BluetoothDevice device : getBluetoothService().getDeviceList()) {
            mPairedDeviceListAdapter.add(device);
        }
    }

    private void setConnectionButtonStatusesIfSelected(final BluetoothDevice device) {
        final BluetoothDevice selectedDevice =
                (BluetoothDevice) mPairedDeviceList.getSelectedItem();
        if (selectedDevice != null && selectedDevice.equals(device)) {
            setConnectionButtonStatuses(device);
        }
    }

    private void setConnectionButtonStatuses(BluetoothDevice device) {
        final boolean isConnectedOrConnecting = getBluetoothService()
                .isConnectedOrConnecting(device);
        mConnectButton.setEnabled(!isConnectedOrConnecting);
        mDisconnectButton.setEnabled(isConnectedOrConnecting);
    }
}
