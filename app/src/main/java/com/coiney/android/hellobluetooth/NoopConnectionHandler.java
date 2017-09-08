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

import android.support.annotation.NonNull;

import com.coiney.android.trueblue.ConnectionClient;

final class NoopConnectionHandler implements ConnectionClient.Callback {

    @Override
    public void onConnectionClosed(@NonNull ConnectionClient client,
            boolean wasClosedByError) {
        // NO-OP
    }

    @Override
    public void onDataRead(@NonNull ConnectionClient client, @NonNull byte[] data) {
        // NO-OP
    }

    @Override
    public void onDataWritten(@NonNull ConnectionClient client, @NonNull byte[] data) {
        // NO-OP
    }

    @Override
    public void onReadErrorEncountered(@NonNull ConnectionClient client) {
        // NO-OP
    }

    @Override
    public void onWriteErrorEncountered(@NonNull ConnectionClient client, @NonNull byte[] data) {
        // NO-OP
    }
}
