package co.elastic.apm.android.test.lifecycle;

import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;

import java.util.List;

import co.elastic.apm.android.test.activities.ErrorActivity;
import co.elastic.apm.android.test.activities.ErrorCoroutineActivity;
import co.elastic.apm.android.test.activities.ErrorHalfWayActivity;
import co.elastic.apm.android.test.activities.FullCreationActivity;
import co.elastic.apm.android.test.activities.Hilt_InstrumentedActivity;
import co.elastic.apm.android.test.activities.MissingOnResumeActivity;
import co.elastic.apm.android.test.activities.MissingOnStartAndOnResumeActivity;
import co.elastic.apm.android.test.activities.SimpleCoroutineActivity;
import co.elastic.apm.android.test.testutils.MainApp;
import co.elastic.apm.android.test.testutils.spans.Spans;
import io.opentelemetry.sdk.trace.data.SpanData;

@Config(application = MainApp.class)
@RunWith(RobolectricTestRunner.class)
public class ActivityLifecycleInstrumentationTest extends BaseLifecycleInstrumentationTest {

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
                    .isNamed(getRootLifecycleSpanName(FullCreationActivity.class));

            Spans.verify(onCreateSpan)
                    .isNamed(getSpanMethodName(ActivityMethod.ON_CREATE))
                    .isDirectChildOf(rootSpan);
            Spans.verify(activity.getOnCreateSpanContext()).belongsTo(onCreateSpan);

            Spans.verify(onStartSpan)
                    .isNamed(getSpanMethodName(ActivityMethod.ON_START))
                    .isDirectChildOf(rootSpan);
            Spans.verify(activity.getOnStartSpanContext()).belongsTo(onStartSpan);

            Spans.verify(onResumeSpan)
                    .isNamed(getSpanMethodName(ActivityMethod.ON_RESUME))
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
                    .isNamed(getRootLifecycleSpanName(MissingOnResumeActivity.class));

            Spans.verify(onCreateSpan)
                    .isNamed(getSpanMethodName(ActivityMethod.ON_CREATE))
                    .isDirectChildOf(rootSpan);
            Spans.verify(activity.getOnCreateSpanContext()).belongsTo(onCreateSpan);

            Spans.verify(onStartSpan)
                    .isNamed(getSpanMethodName(ActivityMethod.ON_START))
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
                    .isNamed(getRootLifecycleSpanName(MissingOnStartAndOnResumeActivity.class));

            Spans.verify(onCreateSpan)
                    .isNamed(getSpanMethodName(ActivityMethod.ON_CREATE))
                    .isDirectChildOf(rootSpan);
            Spans.verify(activity.getOnCreateSpanContext()).belongsTo(onCreateSpan);
        }
    }

    @Test
    public void onCreation_whenOnStartOnly_wrapWithSpan() {
        try (ActivityController<MissingOnStartAndOnResumeActivity> controller = Robolectric.buildActivity(MissingOnStartAndOnResumeActivity.class)) {
            controller.setup();
            MissingOnStartAndOnResumeActivity activity = controller.get();

            List<SpanData> spans = getRecordedSpans(2);

            SpanData rootSpan = spans.get(0);
            SpanData onCreateSpan = spans.get(1);

            Spans.verify(rootSpan)
                    .hasNoParent()
                    .isNamed(getRootLifecycleSpanName(MissingOnStartAndOnResumeActivity.class));

            Spans.verify(onCreateSpan)
                    .isNamed(getSpanMethodName(ActivityMethod.ON_CREATE))
                    .isDirectChildOf(rootSpan);
            Spans.verify(activity.getOnCreateSpanContext()).belongsTo(onCreateSpan);
        }
    }

    @Test
    public void onCreation_whenInterruptedHalfwayByException_endRootSpan() {
        try (ActivityController<ErrorHalfWayActivity> controller = Robolectric.buildActivity(ErrorHalfWayActivity.class)) {
            try {
                controller.setup();
            } catch (IllegalStateException e) {
                ErrorHalfWayActivity activity = controller.get();

                List<SpanData> spans = getRecordedSpans(3);

                SpanData rootSpan = spans.get(0);
                SpanData onCreateSpan = spans.get(1);
                SpanData onStartSpan = spans.get(2);

                Spans.verify(rootSpan)
                        .hasNoParent()
                        .isNamed(getRootLifecycleSpanName(ErrorHalfWayActivity.class));

                Spans.verify(onCreateSpan)
                        .isNamed(getSpanMethodName(ActivityMethod.ON_CREATE))
                        .isDirectChildOf(rootSpan);
                Spans.verify(activity.getOnCreateSpanContext()).belongsTo(onCreateSpan);

                Spans.verifyFailed(onStartSpan)
                        .isNamed(getSpanMethodName(ActivityMethod.ON_START))
                        .hasAmountOfRecordedExceptions(1)
                        .hasRecordedException(e)
                        .isDirectChildOf(rootSpan);
                Spans.verify(activity.getOnStartSpanContext()).belongsTo(onStartSpan);

                assertNull(activity.getOnResumeSpanContext());
            }
        }
    }

    @Test
    public void onCreation_whenActivityIsGeneratedByHilt_ignore() {
        try (ActivityController<Hilt_InstrumentedActivity> controller = Robolectric.buildActivity(Hilt_InstrumentedActivity.class)) {
            controller.setup();

            getRecordedSpans(0);
        }
    }

    @Test
    public void onCreate_recordException() {
        try (ActivityController<ErrorActivity> controller = Robolectric.buildActivity(ErrorActivity.class)) {
            try {
                controller.setup();
            } catch (IllegalStateException e) {
                List<SpanData> spans = getRecordedSpans(2);
                SpanData rootSpan = spans.get(0);
                SpanData onCreateSpan = spans.get(1);

                Spans.verifyFailed(onCreateSpan)
                        .isDirectChildOf(rootSpan)
                        .hasAmountOfRecordedExceptions(1)
                        .hasRecordedException(e);
            }
        }
    }

    @Test
    public void onCreation_whenCoroutineIsLaunched_createSpanForIt_and_preserveContext() {
        try (ActivityController<SimpleCoroutineActivity> controller = Robolectric.buildActivity(SimpleCoroutineActivity.class)) {
            controller.setup();

            List<SpanData> spans = getRecordedSpans(4);

            SpanData onCreateSpan = spans.get(1);
            SpanData coroutineSpan = spans.get(2);
            SpanData mySpan = spans.get(3);

            Spans.verify(coroutineSpan)
                    .isDirectChildOf(onCreateSpan)
                    .isNamed("Coroutine");

            Spans.verify(mySpan)
                    .isDirectChildOf(coroutineSpan)
                    .isNamed("My Span Inside Coroutine");
        }
    }

    @Test
    public void onCreation_whenCoroutineCrashes_recordException() {
        try (ActivityController<ErrorCoroutineActivity> controller = Robolectric.buildActivity(ErrorCoroutineActivity.class)) {
            controller.setup();
            List<SpanData> spans = getRecordedSpans(3);

            SpanData onCreateSpan = spans.get(1);
            SpanData coroutineSpan = spans.get(2);

            Spans.verifyFailed(coroutineSpan)
                    .isDirectChildOf(onCreateSpan)
                    .hasAmountOfRecordedExceptions(1)
                    .isNamed("Coroutine");
        }
    }
}
