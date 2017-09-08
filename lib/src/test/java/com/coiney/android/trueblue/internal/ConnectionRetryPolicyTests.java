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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.hamcrest.core.Is.*;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class ConnectionRetryPolicyTests {

    ConnectionRetryPolicy.Builder mBuilder;

    @Before
    public void setUp() {
        mBuilder = new ConnectionRetryPolicy.Builder();
    }

    @Test
    public void defaultRetryCount() throws Exception {
        final ConnectionRetryPolicy policy = mBuilder.build();
        assertThat("Retry count is not 0.", policy.getRemainingRetryCount(), is(0));
    }

    @Test
    public void defaultRetryDelay() throws Exception {
        final ConnectionRetryPolicy policy = mBuilder.build();
        assertThat("Retry count is not 2000.", policy.getCurrentRetryDelay(), is(2000));
    }

    @Test
    public void defaultRetryAttempts() throws Exception {
        final ConnectionRetryPolicy policy = mBuilder.build();
        assertFalse("Retry attempts available but shouldn't be.", policy.hasAttemptRemaining());
    }

    @Test(expected=IllegalStateException.class)
    public void defaultRetry() throws Exception {
        mBuilder.build().retry();
    }

    @Test
    public void singleRetryRetryCount() throws Exception {
        final ConnectionRetryPolicy policy = mBuilder.setRetryCount(1).build();
        assertThat("Retry count is not 1.", policy.getRemainingRetryCount(), is(1));
    }

    @Test
    public void singleRetryRetryDelay() throws Exception {
        final ConnectionRetryPolicy policy = mBuilder.setRetryCount(1).build();
        assertThat("Retry delay is not 2000.", policy.getCurrentRetryDelay(), is(2000));
    }

    @Test
    public void singleRetryRetryAttemptsAvailable() throws Exception {
        final ConnectionRetryPolicy policy = mBuilder.setRetryCount(1).build();
        assertTrue("Retry attempts not available but should be.", policy.hasAttemptRemaining());
    }

    @Test
    public void singleRetryRetrySucceeds() throws Exception {
        mBuilder.setRetryCount(1).build().retry();
    }

    @Test
    public void singleRetryRetryCountAfterRetry() throws Exception {
        final ConnectionRetryPolicy policy = mBuilder.setRetryCount(1).build();
        policy.retry();
        assertThat("Retry count is not 0.", policy.getRemainingRetryCount(), is(0));
    }

    @Test
    public void singleRetryRetryDelayAfterRetry() throws Exception {
        final ConnectionRetryPolicy policy = mBuilder.setRetryCount(1).build();
        policy.retry();
        assertThat("Retry delay is not 2000.", policy.getCurrentRetryDelay(), is(2000));
    }

    @Test
    public void singleRetryRetryAttemptsAvailableAfterRetry() throws Exception {
        final ConnectionRetryPolicy policy = mBuilder.setRetryCount(1).build();
        policy.retry();
        assertFalse("Retry attempts available but shouldn't be.", policy.hasAttemptRemaining());
    }

    @Test
    public void singleRetryWithRetryDelayRetryDelay() throws Exception {
        final ConnectionRetryPolicy policy = mBuilder.setRetryCount(1).setInitialRetryDelay(5000)
                .build();
        assertThat("Retry delay is not 5000.", policy.getCurrentRetryDelay(), is(5000));
    }

    @Test
    public void singleRetryWithRetryDelayRetryDelayAfterRetry() throws Exception {
        final ConnectionRetryPolicy policy = mBuilder.setRetryCount(1).setInitialRetryDelay(5000)
                .build();
        policy.retry();
        assertThat("Retry delay is not 5000.", policy.getCurrentRetryDelay(), is(5000));
    }

    @Test
    public void multipleRetryWithRetryDelayAndBackoffMultiplierRetryCount() {
        final ConnectionRetryPolicy policy = mBuilder.setRetryCount(2).setInitialRetryDelay(5000)
                .setBackoffMultiplier(2).build();
        assertThat("Retry count is not 2.", policy.getRemainingRetryCount(), is(2));
        policy.retry();
        assertThat("Retry count is not 1.", policy.getRemainingRetryCount(), is(1));
        policy.retry();
        assertThat("Retry count is not 0.", policy.getRemainingRetryCount(), is(0));
    }

    @Test
    public void multipleRetryWithRetryDelayAndBackoffMultiplierHasRetryAttempts() {
        final ConnectionRetryPolicy policy = mBuilder.setRetryCount(2).setInitialRetryDelay(5000)
                .setBackoffMultiplier(2).build();
        assertTrue("Retry attempts not available but should be.", policy.hasAttemptRemaining());
        policy.retry();
        assertTrue("Retry attempts not available but should be.", policy.hasAttemptRemaining());
        policy.retry();
        assertFalse("Retry attempts available but shouldn't be.", policy.hasAttemptRemaining());
    }

    @Test
    public void multipleRetryWithRetryDelayAndBackoffMultiplierRetryDelayAfterRetries() {
        final ConnectionRetryPolicy policy = mBuilder.setRetryCount(2).setInitialRetryDelay(5000)
                .setBackoffMultiplier(2).build();
        assertThat("Retry delay is not 5000.", policy.getCurrentRetryDelay(), is(5000));
        policy.retry();
        assertThat("Retry delay is not 10000.", policy.getCurrentRetryDelay(), is(5000));
        policy.retry();
        assertThat("Retry delay is not 20000.", policy.getCurrentRetryDelay(), is(10000));
    }
}
