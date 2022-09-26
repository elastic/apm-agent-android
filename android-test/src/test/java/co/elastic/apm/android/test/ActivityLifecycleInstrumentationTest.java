package co.elastic.apm.android.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;

import java.util.List;

import co.elastic.apm.android.sdk.ElasticApmAgent;
import co.elastic.apm.android.test.testutils.base.BaseTest;
import co.elastic.apm.android.test.testutils.base.BaseTestApplication;
import co.elastic.apm.android.test.testutils.spans.Spans;
import io.opentelemetry.sdk.trace.data.SpanData;

@Config(application = ActivityLifecycleInstrumentationTest.MainApp.class)
@RunWith(RobolectricTestRunner.class)
public class ActivityLifecycleInstrumentationTest extends BaseTest {

    @Test
    public void onCreation_wrapWithSpan() {
        try (ActivityController<FullCreationActivity> controller = Robolectric.buildActivity(FullCreationActivity.class)) {
            controller.setup();
            FullCreationActivity activity = controller.get();

            List<SpanData> spans = getRecordedSpans(4);

            SpanData rootSpan = spans.get(0);
            SpanData onCreateSpan = spans.get(1);
            SpanData onStartSpan = spans.get(2);
            SpanData onResumeSpan = spans.get(3);

            Spans.verify(rootSpan)
                    .hasNoParent()
                    .isNamed(getActivitySpanName(FullCreationActivity.class, " - Creating"));

            Spans.verify(onCreateSpan)
                    .isNamed(getSpanMethodName(FullCreationActivity.class, ActivityMethod.ON_CREATE))
                    .isDirectChildOf(rootSpan);
            Spans.verify(activity.getOnCreateSpanContext()).belongsTo(onCreateSpan);

            Spans.verify(onStartSpan)
                    .isNamed(getSpanMethodName(FullCreationActivity.class, ActivityMethod.ON_START))
                    .isDirectChildOf(rootSpan);
            Spans.verify(activity.getOnStartSpanContext()).belongsTo(onStartSpan);

            Spans.verify(onResumeSpan)
                    .isNamed(getSpanMethodName(FullCreationActivity.class, ActivityMethod.ON_RESUME))
                    .isDirectChildOf(rootSpan);
            Spans.verify(activity.getOnResumeSpanContext()).belongsTo(onResumeSpan);
        }
    }

    @Test
    public void onCreation_whenMissingOnResume_wrapWithSpan() {
        try (ActivityController<MissingOnResumeActivity> controller = Robolectric.buildActivity(MissingOnResumeActivity.class)) {
            controller.setup();
            MissingOnResumeActivity activity = controller.get();

            List<SpanData> spans = getRecordedSpans(3);

            SpanData rootSpan = spans.get(0);
            SpanData onCreateSpan = spans.get(1);
            SpanData onStartSpan = spans.get(2);

            Spans.verify(rootSpan)
                    .hasNoParent()
                    .isNamed(getActivitySpanName(MissingOnResumeActivity.class, " - Creating"));

            Spans.verify(onCreateSpan)
                    .isNamed(getSpanMethodName(MissingOnResumeActivity.class, ActivityMethod.ON_CREATE))
                    .isDirectChildOf(rootSpan);
            Spans.verify(activity.getOnCreateSpanContext()).belongsTo(onCreateSpan);

            Spans.verify(onStartSpan)
                    .isNamed(getSpanMethodName(MissingOnResumeActivity.class, ActivityMethod.ON_START))
                    .isDirectChildOf(rootSpan);
            Spans.verify(activity.getOnStartSpanContext()).belongsTo(onStartSpan);
        }
    }

    @Test
    public void onCreation_whenMissingOnStartAndOnResume_wrapWithSpan() {
        try (ActivityController<MissingOnStartAndOnResumeActivity> controller = Robolectric.buildActivity(MissingOnStartAndOnResumeActivity.class)) {
            controller.setup();
            MissingOnStartAndOnResumeActivity activity = controller.get();

            List<SpanData> spans = getRecordedSpans(2);

            SpanData rootSpan = spans.get(0);
            SpanData onCreateSpan = spans.get(1);

            Spans.verify(rootSpan)
                    .hasNoParent()
                    .isNamed(getActivitySpanName(MissingOnStartAndOnResumeActivity.class, " - Creating"));

            Spans.verify(onCreateSpan)
                    .isNamed(getSpanMethodName(MissingOnStartAndOnResumeActivity.class, ActivityMethod.ON_CREATE))
                    .isDirectChildOf(rootSpan);
            Spans.verify(activity.getOnCreateSpanContext()).belongsTo(onCreateSpan);
        }
    }

    @Test
    public void onCreate_recordException() {
        try (ActivityController<ErrorActivity> controller = Robolectric.buildActivity(ErrorActivity.class)) {
            try {
                controller.setup();
            } catch (IllegalStateException e) {
                SpanData span = getRecordedSpan();

                Spans.verifyFailed(span)
                        .hasAmountOfRecordedExceptions(1)
                        .hasRecordedException(e);
            }
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
