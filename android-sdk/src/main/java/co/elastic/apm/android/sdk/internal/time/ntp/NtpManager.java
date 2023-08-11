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
package co.elastic.apm.android.sdk.internal.time.ntp;

import android.content.Context;

import androidx.annotation.VisibleForTesting;

import java.util.concurrent.atomic.AtomicBoolean;

import co.elastic.apm.android.common.internal.logging.Elog;
import co.elastic.apm.android.sdk.internal.opentelemetry.tools.ElasticClock;
import co.elastic.apm.android.sdk.internal.services.periodicwork.PeriodicTask;
import io.opentelemetry.sdk.common.Clock;

public final class NtpManager extends PeriodicTask {
    private final TrueTimeWrapper trueTimeWrapper;
    private final AtomicBoolean isInitialized = new AtomicBoolean(false);
    private ElasticClock clock;

    @VisibleForTesting()
    public NtpManager(TrueTimeWrapper trueTimeWrapper) {
        super();
        this.trueTimeWrapper = trueTimeWrapper;
    }

    public NtpManager(Context context) {
        this(new TrueTimeWrapper(context));
    }

    public boolean isInitialized() {
        return isInitialized.get();
    }

    public void initialize() {
        trueTimeWrapper.withSharedPreferencesCache();
        trueTimeWrapper.withRootDispersionMax(200);
        trueTimeWrapper.withRootDelayMax(200);
    }

    public Clock getClock() {
        if (clock == null) {
            clock = new ElasticClock(trueTimeWrapper);
        }
        return clock;
    }

    @Override
    protected void onPeriodicTaskRun() {
        Elog.getLogger().info("About to initialize the NTP");
        if (trueTimeWrapper.isInitialized()) {
            isInitialized.set(true);
            Elog.getLogger().info("NTP already initialized");
            return;
        }
        try {
            trueTimeWrapper.initialize();
            isInitialized.set(true);
            Elog.getLogger().info("NTP successfully initialized");
        } catch (Throwable t) {
            Elog.getLogger().info("NTP failed to initialize", t);
        }
    }

    @Override
    protected long getMillisToWaitBeforeNextRun() {
        return 0;
    }

    @Override
    public boolean isFinished() {
        return isInitialized();
    }
}
