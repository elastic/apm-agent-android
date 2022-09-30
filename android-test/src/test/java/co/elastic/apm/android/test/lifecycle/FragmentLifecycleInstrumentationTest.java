package co.elastic.apm.android.test.lifecycle;


import static org.junit.Assert.assertNull;

import androidx.fragment.app.testing.FragmentScenario;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.List;

import co.elastic.apm.android.test.fragments.ErrorFragment;
import co.elastic.apm.android.test.fragments.FullCreationFragment;
import co.elastic.apm.android.test.fragments.Hilt_InstrumentedFragment;
import co.elastic.apm.android.test.fragments.OnCreateMissingFragment;
import co.elastic.apm.android.test.fragments.OnCreateViewOnlyFragment;
import co.elastic.apm.android.test.fragments.ViewlessCreationFragment;
import co.elastic.apm.android.test.testutils.MainApp;
import co.elastic.apm.android.test.spans.Spans;
import io.opentelemetry.sdk.trace.data.SpanData;

@Config(application = MainApp.class)
@RunWith(RobolectricTestRunner.class)
public class FragmentLifecycleInstrumentationTest extends BaseLifecycleInstrumentationTest {

    @Test
    public void onCreation_wrapWithSpans() {
        try (FragmentScenario<FullCreationFragment> scenario = FragmentScenario.launchInContainer(FullCreationFragment.class)) {
            scenario.onFragment(fragment -> {
                List<SpanData> spans = getRecordedSpans(4);
                SpanData rootSpan = spans.get(0);
                SpanData onCreateSpan = spans.get(1);
                SpanData onCreateViewSpan = spans.get(2);
                SpanData onViewCreatedSpan = spans.get(3);

                Spans.verify(rootSpan)
                        .hasNoParent()
                        .isNamed(getRootLifecycleSpanName(FullCreationFragment.class));

                Spans.verify(onCreateSpan)
                        .isDirectChildOf(rootSpan)
                        .isNamed(getSpanMethodName(FragmentMethod.ON_CREATE));
                Spans.verify(fragment.getOnCreateSpanContext()).belongsTo(onCreateSpan);

                Spans.verify(onCreateViewSpan)
                        .isDirectChildOf(rootSpan)
                        .isNamed(getSpanMethodName(FragmentMethod.ON_CREATE_VIEW));
                Spans.verify(fragment.getOnCreateViewSpanContext()).belongsTo(onCreateViewSpan);

                Spans.verify(onViewCreatedSpan)
                        .isDirectChildOf(rootSpan)
                        .isNamed(getSpanMethodName(FragmentMethod.ON_VIEW_CREATED));
                Spans.verify(fragment.getOnViewCreatedSpanContext()).belongsTo(onViewCreatedSpan);
            });
        }
    }

    @Test
    public void onCreation_viewlessFragment_ignoreOnViewCreated() {
        try (FragmentScenario<ViewlessCreationFragment> scenario = FragmentScenario.launchInContainer(ViewlessCreationFragment.class)) {
            scenario.onFragment(fragment -> {
                List<SpanData> spans = getRecordedSpans(3);
                SpanData rootSpan = spans.get(0);
                SpanData onCreateSpan = spans.get(1);
                SpanData onCreateViewSpan = spans.get(2);

                Spans.verify(rootSpan)
                        .hasNoParent()
                        .isNamed(getRootLifecycleSpanName(ViewlessCreationFragment.class));

                Spans.verify(onCreateSpan)
                        .isDirectChildOf(rootSpan)
                        .isNamed(getSpanMethodName(FragmentMethod.ON_CREATE));
                Spans.verify(fragment.getOnCreateSpanContext()).belongsTo(onCreateSpan);

                Spans.verify(onCreateViewSpan)
                        .isDirectChildOf(rootSpan)
                        .isNamed(getSpanMethodName(FragmentMethod.ON_CREATE_VIEW));
                Spans.verify(fragment.getOnCreateViewSpanContext()).belongsTo(onCreateViewSpan);

                assertNull(fragment.getOnViewCreatedSpanContext());
            });
        }
    }

    @Test
    public void onCreation_onCreateMissing_wrapOthersWithSpan() {
        try (FragmentScenario<OnCreateMissingFragment> scenario = FragmentScenario.launchInContainer(OnCreateMissingFragment.class)) {
            scenario.onFragment(fragment -> {
                List<SpanData> spans = getRecordedSpans(3);
                SpanData rootSpan = spans.get(0);
                SpanData onCreateViewSpan = spans.get(1);
                SpanData onViewCreatedSpan = spans.get(2);

                Spans.verify(rootSpan)
                        .hasNoParent()
                        .isNamed(getRootLifecycleSpanName(OnCreateMissingFragment.class));

                Spans.verify(onCreateViewSpan)
                        .isDirectChildOf(rootSpan)
                        .isNamed(getSpanMethodName(FragmentMethod.ON_CREATE_VIEW));
                Spans.verify(fragment.getOnCreateViewSpanContext()).belongsTo(onCreateViewSpan);

                Spans.verify(onViewCreatedSpan)
                        .isDirectChildOf(rootSpan)
                        .isNamed(getSpanMethodName(FragmentMethod.ON_VIEW_CREATED));
                Spans.verify(fragment.getOnViewCreatedSpanContext()).belongsTo(onViewCreatedSpan);
            });
        }
    }

    @Test
    public void onCreation_whenOnlyOnCreateViewIsAvailable_ignoreOthers() {
        try (FragmentScenario<OnCreateViewOnlyFragment> scenario = FragmentScenario.launchInContainer(OnCreateViewOnlyFragment.class)) {
            scenario.onFragment(fragment -> {
                List<SpanData> spans = getRecordedSpans(2);
                SpanData rootSpan = spans.get(0);
                SpanData onCreateViewSpan = spans.get(1);

                Spans.verify(rootSpan)
                        .hasNoParent()
                        .isNamed(getRootLifecycleSpanName(OnCreateViewOnlyFragment.class));

                Spans.verify(onCreateViewSpan)
                        .isDirectChildOf(rootSpan)
                        .isNamed(getSpanMethodName(FragmentMethod.ON_CREATE_VIEW));
                Spans.verify(fragment.getOnCreateViewSpanContext()).belongsTo(onCreateViewSpan);
            });
        }
    }

    @Test
    public void onCreation_whenFragmentIsGeneratedBuHilt_ignore() {
        try (FragmentScenario<Hilt_InstrumentedFragment> scenario = FragmentScenario.launchInContainer(Hilt_InstrumentedFragment.class)) {
            getRecordedSpans(0);
        }
    }

    @Test
    public void onCreation_whenInterruptedHalfwayByException_endRootSpan() {
        try {
            FragmentScenario.launchInContainer(ErrorFragment.class);
        } catch (NullPointerException e) {
            List<SpanData> spans = getRecordedSpans(3);
            SpanData rootSpan = spans.get(0);
            SpanData onCreateSpan = spans.get(1);
            SpanData onCreateViewSpan = spans.get(2);

            Spans.verify(rootSpan)
                    .hasNoParent()
                    .isNamed(getRootLifecycleSpanName(ErrorFragment.class));

            Spans.verify(onCreateSpan)
                    .isDirectChildOf(rootSpan)
                    .isNamed(getSpanMethodName(FragmentMethod.ON_CREATE));

            Spans.verifyFailed(onCreateViewSpan)
                    .isDirectChildOf(rootSpan)
                    .hasAmountOfRecordedExceptions(1)
                    .hasRecordedException(e)
                    .isNamed(getSpanMethodName(FragmentMethod.ON_CREATE_VIEW));
        }
    }
}

