/*
 * Licensed to Elasticsearch B.V. under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch B.V. licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package co.elastic.apm.android.sdk.internal.utilities.time.ntp;

import android.content.Context;

import androidx.annotation.VisibleForTesting;

import java.io.IOException;

import co.elastic.apm.android.common.internal.logging.Elog;
import co.elastic.apm.android.sdk.internal.utilities.concurrency.BackgroundExecutor;
import co.elastic.apm.android.sdk.internal.utilities.concurrency.Result;
import co.elastic.apm.android.sdk.internal.utilities.concurrency.impl.SimpleBackgroundExecutor;
import co.elastic.apm.android.sdk.internal.utilities.otel.ElasticClock;
import io.opentelemetry.sdk.common.Clock;

public final class NtpManager implements BackgroundExecutor.Callback<Void> {
    private final TrueTimeWrapper trueTimeWrapper;
    private final BackgroundExecutor executor;
    private ElasticClock clock;

    @VisibleForTesting()
    public NtpManager(TrueTimeWrapper trueTimeWrapper, BackgroundExecutor executor) {
        this.trueTimeWrapper = trueTimeWrapper;
        this.executor = executor;
    }

    public NtpManager(Context context) {
        this(new TrueTimeWrapper(context), new SimpleBackgroundExecutor());
    }

    public void initialize() {
        trueTimeWrapper.withSharedPreferencesCache();
        trueTimeWrapper.withRootDispersionMax(200);
        trueTimeWrapper.withRootDelayMax(200);
        if (trueTimeWrapper.isInitialized()) {
            Elog.getLogger().info("NTP already initialized");
            return;
        }
        Elog.getLogger().info("About to initialize the NTP");
        executor.execute(() -> {
            try {
                trueTimeWrapper.initialize();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return null;
        }, this);
    }

    public Clock getClock() {
        if (clock == null) {
            clock = new ElasticClock(trueTimeWrapper);
        }
        return clock;
    }

    @Override
    public void onFinish(Result<Void> result) {
        if (result.isSuccess) {
            Elog.getLogger().info("NTP successfully initialized");
        } else {
            Elog.getLogger().info("NTP failed to initialize", result.error);
        }
    }
}
