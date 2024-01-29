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
package co.elastic.apm.android.sdk.internal.features.launchtime;

import java.util.concurrent.TimeUnit;

import co.elastic.apm.android.common.internal.logging.Elog;
import co.elastic.apm.android.sdk.ElasticApmAgent;
import co.elastic.apm.android.sdk.instrumentation.Instrumentations;
import co.elastic.apm.android.sdk.metrics.ElasticMeters;
import io.opentelemetry.android.instrumentation.ApplicationStateListener;
import io.opentelemetry.api.metrics.BatchCallback;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.ObservableDoubleMeasurement;

public final class LaunchTimeApplicationListener implements ApplicationStateListener {

    @Override
    public void onApplicationForegrounded() {
        if (LaunchTimeTracker.stopTimer()) {
            if (Instrumentations.isAppLaunchTimeEnabled()) {
                long launchTimeInNanos = LaunchTimeTracker.getElapsedTimeInNanos();
                sendAppLaunchTimeMetric(TimeUnit.NANOSECONDS.toMillis(launchTimeInNanos));
            }
        }
    }

    private void sendAppLaunchTimeMetric(long launchTimeMillis) {
        Elog.getLogger().info("Setting up launch time metric");
        Meter meter = ElasticMeters.create("LaunchTimeTracker");
        ObservableDoubleMeasurement launchTime = meter.gaugeBuilder("application.launch.time").buildObserver();
        BatchCallback batchCallback = meter.batchCallback(() -> {
            Elog.getLogger().debug("Sending launch time metric of {} milliseconds", launchTimeMillis);
            launchTime.record(launchTimeMillis);
        }, launchTime);
        ElasticApmAgent.get().getFlusher().flushMetrics();
        batchCallback.close();
    }

    @Override
    public void onApplicationBackgrounded() {

    }
}
