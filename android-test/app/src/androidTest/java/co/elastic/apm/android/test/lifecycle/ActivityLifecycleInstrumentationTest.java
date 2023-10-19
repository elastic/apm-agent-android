package co.elastic.apm.android.test.lifecycle;

import static org.junit.Assert.assertNull;

import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ActivityScenario;

import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

import co.elastic.apm.android.sdk.ElasticApmAgent;
import co.elastic.apm.android.sdk.ElasticApmConfiguration;
import co.elastic.apm.android.sdk.instrumentation.InstrumentationConfiguration;
import co.elastic.apm.android.test.activities.ErrorActivity;
import co.elastic.apm.android.test.activities.ErrorHalfWayActivity;
import co.elastic.apm.android.test.activities.FullCreationActivity;
import co.elastic.apm.android.test.activities.Hilt_InstrumentedActivity;
import co.elastic.apm.android.test.activities.MissingOnResumeActivity;
import co.elastic.apm.android.test.activities.MissingOnStartAndOnResumeActivity;
import co.elastic.apm.android.test.activities.NoLifecycleMethodsActivity;
import co.elastic.apm.android.test.activities.SimpleCoroutineActivity;
import co.elastic.apm.android.test.activities.TitleActivity;
import co.elastic.apm.android.test.common.spans.Spans;
import io.opentelemetry.sdk.trace.data.SpanData;

public class ActivityLifecycleInstrumentationTest extends BaseLifecycleInstrumentationTest {

    @Test
    public void onCreation_wrapWithSpan() {
        try (ActivityScenario<FullCreationActivity> controller = ActivityScenario.launch(FullCreationActivity.class)) {
            controller.onActivity(activity -> {
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
            });
        }
    }

    @Test
    public void onBringingBackToForeground_wrapWithSpan_onlyMethodsCalled() {
        try (ActivityScenario<FullCreationActivity> controller = ActivityScenario.launch(FullCreationActivity.class)) {
            // Creation spans
            getRecordedSpans(4);

            // Backgrounding screen
            controller.moveToState(Lifecycle.State.CREATED); // The enum name CREATED is misleading, this will STOP the activity.

            // Bringing back to the foreground
            controller.moveToState(Lifecycle.State.RESUMED);

            controller.onActivity(activity -> {
                List<SpanData> spans = getRecordedSpans(3);

                SpanData rootSpan = spans.get(0);
                SpanData onStartSpan = spans.get(1);
                SpanData onResumeSpan = spans.get(2);

                Spans.verify(rootSpan)
                        .hasNoParent()
                        .isNamed(getRootLifecycleSpanName(FullCreationActivity.class));

                Spans.verify(onStartSpan)
                        .isNamed(getSpanMethodName(ActivityMethod.ON_START))
                        .isDirectChildOf(rootSpan);
                Spans.verify(activity.getOnStartSpanContext()).belongsTo(onStartSpan);

                Spans.verify(onResumeSpan)
                        .isNamed(getSpanMethodName(ActivityMethod.ON_RESUME))
                        .isDirectChildOf(rootSpan);
                Spans.verify(activity.getOnResumeSpanContext()).belongsTo(onResumeSpan);
            });
        }
    }

    @Test
    public void onCreation_whenMissingOnResume_wrapWithSpan() {
        try (ActivityScenario<MissingOnResumeActivity> controller = ActivityScenario.launch(MissingOnResumeActivity.class)) {
            controller.onActivity(activity -> {
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
            });
        }
    }

    @Test
    public void onCreation_whenMissingOnStartAndOnResume_wrapWithSpan() {
        try (ActivityScenario<MissingOnStartAndOnResumeActivity> controller = ActivityScenario.launch(MissingOnStartAndOnResumeActivity.class)) {
            controller.onActivity(activity -> {
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
            });
        }
    }

    @Test
    public void onCreation_whenOnStartOnly_wrapWithSpan() {
        try (ActivityScenario<MissingOnStartAndOnResumeActivity> controller = ActivityScenario.launch(MissingOnStartAndOnResumeActivity.class)) {
            controller.onActivity(activity -> {

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
            });
        }
    }

    @Ignore("No way to run this test using Espresso, the exception doesn't get caught.")
    @Test
    public void onCreation_whenInterruptedHalfwayByException_endRootSpan() {
        try {
            try (ActivityScenario<ErrorHalfWayActivity> ignored = ActivityScenario.launch(ErrorHalfWayActivity.class)) {
            }
        } catch (IllegalStateException e) {
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

            Spans.verifyFailed(onStartSpan)
                    .isNamed(getSpanMethodName(ActivityMethod.ON_START))
                    .hasAmountOfRecordedExceptions(1)
                    .hasRecordedException(e)
                    .isDirectChildOf(rootSpan);
        }
    }

    @Test
    public void onCreation_whenActivityIsGeneratedByHilt_ignore() {
        try (ActivityScenario<Hilt_InstrumentedActivity> ignored = ActivityScenario.launch(Hilt_InstrumentedActivity.class)) {
            getRecordedSpans(0);
        }
    }

    @Ignore("No way to run this test using Espresso, the exception doesn't get caught.")
    @Test
    public void onCreate_recordException() {
        try {
            try (ActivityScenario<ErrorActivity> ignored = ActivityScenario.launch(ErrorActivity.class)) {
            }
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

    @Test
    public void onCreation_whenCoroutineIsLaunched_preserveContext() {
        try (ActivityScenario<SimpleCoroutineActivity> ignored = ActivityScenario.launch(SimpleCoroutineActivity.class)) {
            List<SpanData> spans = getRecordedSpans(3);

            SpanData onCreateSpan = spans.get(1);
            SpanData mySpan = spans.get(2);

            Spans.verify(mySpan)
                    .isDirectChildOf(onCreateSpan)
                    .isNamed("My Span Inside Coroutine");
        }
    }

    @Test
    public void onCreation_whenTitleIsAvailable_keepPreviouslySetSpanName() {
        try (ActivityScenario<TitleActivity> ignored = ActivityScenario.launch(TitleActivity.class)) {
            List<SpanData> spans = getRecordedSpans(2);

            SpanData rootSpan = spans.get(0);
            SpanData onCreateSpan = spans.get(1);

            Spans.verify(rootSpan)
                    .hasNoParent()
                    .isNamed(getRootLifecycleSpanName(TitleActivity.class));

            Spans.verify(onCreateSpan)
                    .isDirectChildOf(rootSpan)
                    .isNamed(getSpanMethodName(ActivityMethod.ON_CREATE));
        }
    }

    @Test
    public void onCreation_whenNoLifecycleMethodsAvailable_doNothing() {
        try (ActivityScenario<NoLifecycleMethodsActivity> ignored = ActivityScenario.launch(NoLifecycleMethodsActivity.class)) {
            getRecordedSpans(0);
        }
    }

    @Test
    public void whenInstrumentationIsDisabled_doNotSendScreenRenderingSpans() {
        ElasticApmConfiguration configuration = ElasticApmConfiguration.builder()
                .setInstrumentationConfiguration(InstrumentationConfiguration.builder().enableScreenRendering(false).build())
                .build();
        overrideAgentConfiguration(configuration);
        try (ActivityScenario<FullCreationActivity> ignored = ActivityScenario.launch(FullCreationActivity.class)) {
            getRecordedSpans(0);
        }
    }

    @Test
    public void whenAgentIsNotInitialized_doNotSendScreenRenderingSpans() {
        ElasticApmAgent.resetForTest();
        try (ActivityScenario<FullCreationActivity> ignored = ActivityScenario.launch(FullCreationActivity.class)) {
            getRecordedSpans(0);
        }
    }
}
