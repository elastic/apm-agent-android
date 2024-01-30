package co.elastic.apm.android.test.lifecycle;

import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ActivityScenario;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import co.elastic.apm.android.sdk.ElasticApmConfiguration;
import co.elastic.apm.android.sdk.instrumentation.InstrumentationConfiguration;
import co.elastic.apm.android.test.activities.FullCreationActivity;
import co.elastic.apm.android.test.activities.SimpleCoroutineActivity;
import co.elastic.apm.android.test.common.spans.Spans;
import io.opentelemetry.sdk.trace.data.SpanData;

public class ActivityLifecycleInstrumentationTest extends BaseLifecycleInstrumentationTest {

    @Before
    public void setUp() {
        getSpanExporter().clearCapturedSpans();
    }

    @Test
    public void onCreation_wrapWithSpan() {
        try (ActivityScenario<FullCreationActivity> controller = ActivityScenario.launch(FullCreationActivity.class)) {
            controller.onActivity(activity -> {
                List<SpanData> spans = getRecordedSpans(1);

                SpanData onCreateSpan = spans.get(0);

                Spans.verify(onCreateSpan)
                        .hasAttribute("screen.name", "FullCreationActivity")
                        .isNamed(getSpanMethodName(ActivityMethod.ON_CREATE));
                Spans.verify(activity.getOnCreateSpanContext()).belongsTo(onCreateSpan);
            });
        }
    }

    @Test
    public void onBringingBackToForeground_wrapWithSpan_onlyMethodsCalled() {
        try (ActivityScenario<FullCreationActivity> controller = ActivityScenario.launch(FullCreationActivity.class)) {
            // Creation spans
            getRecordedSpans(1);

            // Backgrounding screen
            controller.moveToState(Lifecycle.State.CREATED); // The enum name CREATED is misleading, this will STOP the activity.

            // Bringing back to the foreground
            controller.moveToState(Lifecycle.State.RESUMED);

            controller.onActivity(activity -> {
                List<SpanData> spans = getRecordedSpans(5);

                SpanData onPauseSpan = spans.get(0);
                SpanData onCreateSpan = spans.get(1);
                SpanData onStopSpan = spans.get(2);
                SpanData onPauseSpan2 = spans.get(3);
                SpanData onRestartedSpan = spans.get(4);

                Spans.verify(onPauseSpan)
                        .hasNoParent()
                        .isNamed(getSpanMethodName(ActivityMethod.ON_PAUSE));
                Spans.verify(onCreateSpan)
                        .hasNoParent()
                        .isNamed(getSpanMethodName(ActivityMethod.ON_CREATE));
                Spans.verify(onStopSpan)
                        .hasNoParent()
                        .isNamed(getSpanMethodName(ActivityMethod.ON_STOP));
                Spans.verify(onPauseSpan2)
                        .hasNoParent()
                        .isNamed(getSpanMethodName(ActivityMethod.ON_PAUSE));
                Spans.verify(onRestartedSpan)
                        .isNamed(getSpanMethodName(ActivityMethod.ON_RESUME))
                        .hasNoParent();
                Spans.verify(activity.getOnResumeSpanContext()).belongsTo(onRestartedSpan);
                Spans.verify(activity.getOnStartSpanContext()).belongsTo(onRestartedSpan);
            });
        }
    }

    @Test
    public void onCreation_whenCoroutineIsLaunched_preserveContext() {
        try (ActivityScenario<SimpleCoroutineActivity> ignored = ActivityScenario.launch(SimpleCoroutineActivity.class)) {
            List<SpanData> spans = getRecordedSpans(2);

            SpanData onCreateSpan = spans.get(0);
            SpanData mySpan = spans.get(1);

            Spans.verify(mySpan)
                    .isDirectChildOf(onCreateSpan)
                    .isNamed("My Span Inside Coroutine");
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
}
