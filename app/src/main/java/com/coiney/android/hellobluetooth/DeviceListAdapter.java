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
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

final class DeviceListAdapter extends ArrayAdapter<BluetoothDevice> {

    DeviceListAdapter(Context context, int resource) {
        super(context, resource);
    }

    DeviceListAdapter(Context context, int resource, List<BluetoothDevice> objects) {
        super(context, resource, objects);
    }

    @Override
    @NonNull
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return getItemView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView,
            @NonNull ViewGroup parent) {
        return getItemView(position, convertView, parent);
    }

    private View getItemView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.device_list_item, null);
        }
        final BluetoothDevice device = getItem(position);
        final String deviceName = (device != null) ? device.getName() : null;
        final String deviceAddress = (device != null) ? device.getAddress() : "?";
        final String displayName = ((deviceName != null) ? deviceName : "<Unknown>") +
                " (" + deviceAddress + ")";
        ((TextView) convertView.findViewById(android.R.id.text1)).setText(displayName);
        return convertView;
    }
}
