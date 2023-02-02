package co.elastic.apm.android.test.initialization;

import org.junit.After;
import org.junit.Test;
import org.robolectric.Robolectric;
import org.robolectric.android.controller.ActivityController;

import co.elastic.apm.android.sdk.internal.features.launchtime.LaunchTimeTracker;
import co.elastic.apm.android.test.activities.FullCreationActivity;
import co.elastic.apm.android.test.activities.OnStartOnlyActivity;
import co.elastic.apm.android.test.common.metrics.Metrics;
import co.elastic.apm.android.test.testutils.base.BaseRobolectricTest;
import io.opentelemetry.sdk.metrics.data.MetricData;

public class AppLaunchTimeTest extends BaseRobolectricTest {

    @After
    public void tearDown() {
        LaunchTimeTracker.resetForTest();
    }

    @Test
    public void whenFirstActivityIsOpen_trackStartupTime_in_onResume() {
        try (ActivityController<FullCreationActivity> controller = Robolectric.buildActivity(FullCreationActivity.class)) {
            controller.create().start().postCreate(null);

            // Checking that there's no metrics up to this point
            getRecordedMetrics(0);

            controller.resume();

            MetricData startupMetric = getRecordedMetric();

            Metrics.verify(startupMetric)
                    .isNamed("application.launch.time");
        }
    }

    @Test
    public void whenMetricsAreFlushedMoreThanOnce_trackStartupTime_onlyOnce() {
        try (ActivityController<FullCreationActivity> controller = Robolectric.buildActivity(FullCreationActivity.class)) {
            controller.setup();

            flushMetrics();
            flushMetrics();

            MetricData startupMetric = getRecordedMetric();

            Metrics.verify(startupMetric)
                    .isNamed("application.launch.time");
        }
    }

    @Test
    public void whenMoreThanOneActivityGetsOpened_trackStartupTime_fromTheFirstOneOnly() {
        try (ActivityController<FullCreationActivity> controller = Robolectric.buildActivity(FullCreationActivity.class)) {
            controller.setup();

            Metrics.verify(getRecordedMetric())
                    .isNamed("application.launch.time");
        }
        try (ActivityController<OnStartOnlyActivity> controller = Robolectric.buildActivity(OnStartOnlyActivity.class)) {
            controller.setup();

            getRecordedMetrics(0);
        }
    }

}
