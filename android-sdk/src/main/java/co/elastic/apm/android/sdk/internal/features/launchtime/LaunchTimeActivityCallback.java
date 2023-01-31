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

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.concurrent.TimeUnit;

import co.elastic.apm.android.common.internal.logging.Elog;
import co.elastic.apm.android.sdk.ElasticApmAgent;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.Meter;

public final class LaunchTimeActivityCallback implements Application.ActivityLifecycleCallbacks {

    @Override
    public void onActivityPreResumed(@NonNull Activity activity) {
        unregisterCallback(activity);

        if (LaunchTimeTracker.stopTimer()) {
            long launchTimeInNanos = LaunchTimeTracker.getElapsedTimeInNanos();
            sendAppLaunchTimeMetric(TimeUnit.NANOSECONDS.toMillis(launchTimeInNanos));
        }
    }

    private void sendAppLaunchTimeMetric(long launchTimeMillis) {
        Elog.getLogger().debug("Sending launch time metric of {} milliseconds", launchTimeMillis);
        Meter meter = GlobalOpenTelemetry.getMeter("LaunchTimeTracker");
        DoubleHistogram histogram = meter.histogramBuilder("application.launch.time").build();
        histogram.record(launchTimeMillis);
        ElasticApmAgent.get().getFlusher().flushMetrics();
    }

    private void unregisterCallback(@NonNull Activity activity) {
        Elog.getLogger().debug("Unregistering launch time activity callback");
        activity.getApplication().unregisterActivityLifecycleCallbacks(this);
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {

    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {

    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {

    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {

    }
}
