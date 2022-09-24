package co.elastic.apm.android.test;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;

import co.elastic.apm.android.sdk.ElasticApmAgent;
import co.elastic.apm.android.test.testutils.BaseTest;
import co.elastic.apm.android.test.testutils.BaseTestApplication;
import co.elastic.apm.android.test.testutils.Spans;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.sdk.trace.data.SpanData;

@Config(application = ActivityLifecycleInstrumentationTest.MainApp.class)
@RunWith(RobolectricTestRunner.class)
public class ActivityLifecycleInstrumentationTest extends BaseTest {

    @Test
    public void onCreate_wrapWithSpan() {
        try (ActivityController<MainActivity> controller = Robolectric.buildActivity(MainActivity.class)) {
            controller.setup();
            MainActivity activity = controller.get();

            SpanData span = getRecordedSpan();

            Spans.verify(span)
                    .hasNoError()
                    .isNamed(getSpanMethodName(ActivityMethod.ON_CREATE));
            Spans.verify(activity.getOnCreateSpanContext()).belongsTo(span);
        }
    }

    @Test
    public void onCreate_recordException() {
        try (ActivityController<ErrorActivity> controller = Robolectric.buildActivity(ErrorActivity.class)) {
            try {
                controller.setup();
            } catch (IllegalStateException e) {
                SpanData span = getRecordedSpan();

                Spans.verify(span).hasError()
                        .hasAmountOfRecordedExceptions(1)
                        .hasRecordedException(e);
            }
        }
    }

    @After
    public void cleanUp() {
        ElasticApmAgent.get().destroy();
        GlobalOpenTelemetry.resetForTest();
    }

    public static class MainApp extends BaseTestApplication {

        @Override
        public void onCreate() {
            super.onCreate();
            ElasticApmAgent.initialize(this, getConnectivity());
        }
    }
}
