package co.elastic.apm.android.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;

import java.util.List;

import co.elastic.apm.android.sdk.ElasticApmAgent;
import co.elastic.apm.android.test.testutils.BaseTest;
import co.elastic.apm.android.test.testutils.BaseTestApplication;
import co.elastic.apm.android.test.testutils.Spans;
import io.opentelemetry.sdk.trace.data.SpanData;

@RunWith(RobolectricTestRunner.class)
public class ActivityLifecycleInstrumentationTest extends BaseTest {

    @Config(application = MainApp.class)
    @Test
    public void onCreate_wrapWithSpan() {
        try (ActivityController<MainActivity> controller = Robolectric.buildActivity(MainActivity.class)) {
            controller.setup();
            MainActivity activity = controller.get();

            List<SpanData> sentSpans = getSentSpans();
            assertEquals(1, sentSpans.size());
            SpanData span = sentSpans.get(0);

            Spans.verify(span)
                    .hasNoError()
                    .isNamed(getSpanMethodName(ActivityMethod.ON_CREATE));
            Spans.verify(activity.getOnCreateSpanContext()).belongsTo(span);
        }
    }

    public static class MainApp extends BaseTestApplication {

        @Override
        public void onCreate() {
            super.onCreate();
            ElasticApmAgent.initialize(this, getConnectivity());
        }
    }
}
