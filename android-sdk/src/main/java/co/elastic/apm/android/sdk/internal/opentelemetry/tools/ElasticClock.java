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
package co.elastic.apm.android.sdk.internal.opentelemetry.tools;

import androidx.annotation.VisibleForTesting;

import java.util.concurrent.TimeUnit;

import co.elastic.apm.android.common.internal.logging.Elog;
import co.elastic.apm.android.sdk.internal.time.SystemTimeProvider;
import co.elastic.apm.android.sdk.internal.time.ntp.TrueTimeWrapper;
import io.opentelemetry.sdk.common.Clock;

public final class ElasticClock implements Clock {
    private final TrueTimeWrapper trueTimeWrapper;
    private final SystemTimeProvider systemTimeProvider;

    @VisibleForTesting
    public ElasticClock(TrueTimeWrapper trueTimeWrapper, SystemTimeProvider systemTimeProvider) {
        this.trueTimeWrapper = trueTimeWrapper;
        this.systemTimeProvider = systemTimeProvider;
    }

    public ElasticClock(TrueTimeWrapper trueTimeWrapper) {
        this(trueTimeWrapper, SystemTimeProvider.get());
    }

    @Override
    public long now() {
        if (trueTimeWrapper.isInitialized()) {
            Elog.getLogger().debug("Returning true time");
            try {
                return TimeUnit.MILLISECONDS.toNanos(trueTimeWrapper.now().getTime());
            } catch (Throwable t) {
                trueTimeWrapper.clearCachedInfo();
                Elog.getLogger().error("Could not get true time", t);
            }
        }
        Elog.getLogger().debug("Returning system time");
        return TimeUnit.MILLISECONDS.toNanos(systemTimeProvider.getCurrentTimeMillis());
    }

    @Override
    public long nanoTime() {
        return systemTimeProvider.getNanoTime();
    }
}
