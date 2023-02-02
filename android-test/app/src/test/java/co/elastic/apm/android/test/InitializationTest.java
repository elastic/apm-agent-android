package co.elastic.apm.android.test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;

import co.elastic.apm.android.sdk.ElasticApmAgent;
import co.elastic.apm.android.sdk.ElasticApmConfiguration;
import co.elastic.apm.android.sdk.session.impl.DefaultSessionIdProvider;
import co.elastic.apm.android.test.activities.FullCreationActivity;
import co.elastic.apm.android.test.activities.OnStartOnlyActivity;
import co.elastic.apm.android.test.common.metrics.Metrics;
import co.elastic.apm.android.test.testutils.base.BaseRobolectricTest;
import co.elastic.apm.android.test.testutils.base.BaseRobolectricTestApplication;
import io.opentelemetry.sdk.metrics.data.MetricData;

public class InitializationTest extends BaseRobolectricTest {

    @Config(application = AppWithMockSessionId.class)
    @Test
    public void whenSessionIdProviderIsInitializable_initializeIt() {
        AppWithMockSessionId app = (AppWithMockSessionId) RuntimeEnvironment.getApplication();

        verify(app.sessionIdProvider).initialize();
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

    private static class AppWithMockSessionId extends BaseRobolectricTestApplication {
        private DefaultSessionIdProvider sessionIdProvider;

        @Override
        public void onCreate() {
            super.onCreate();
            sessionIdProvider = mock(DefaultSessionIdProvider.class);
            ElasticApmAgent.initialize(this,
                    ElasticApmConfiguration.builder().setSessionIdProvider(sessionIdProvider).build(),
                    getConnectivity());
        }
    }
}
