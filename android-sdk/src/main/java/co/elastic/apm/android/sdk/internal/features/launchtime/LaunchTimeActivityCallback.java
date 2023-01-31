package co.elastic.apm.android.sdk.internal.features.launchtime;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import co.elastic.apm.android.common.internal.logging.Elog;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.Meter;

public final class LaunchTimeActivityCallback implements Application.ActivityLifecycleCallbacks {

    @Override
    public void onActivityPreResumed(@NonNull Activity activity) {
        unregisterCallback(activity);

        if (LaunchTimeTracker.stopTimer()) {
            long launchTimeInNanos = LaunchTimeTracker.getElapsedTimeInNanos();
            sendAppLaunchTimeMetric(launchTimeInNanos);
        }
    }

    private void sendAppLaunchTimeMetric(long launchTimeInNanos) {
        Elog.getLogger().info("Sending launch time metric");
        Meter meter = GlobalOpenTelemetry.getMeter("LaunchTimeTracker");
        DoubleHistogram histogram = meter.histogramBuilder("application.launch.time").build();
        histogram.record(launchTimeInNanos);
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
