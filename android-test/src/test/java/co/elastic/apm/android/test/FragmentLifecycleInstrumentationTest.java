package co.elastic.apm.android.test;


import static org.junit.Assert.assertNull;

import androidx.fragment.app.testing.FragmentScenario;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.List;

import co.elastic.apm.android.test.fragments.FullCreationFragment;
import co.elastic.apm.android.test.fragments.ViewlessCreationFragment;
import co.elastic.apm.android.test.testutils.MainApp;
import co.elastic.apm.android.test.testutils.base.BaseTest;
import co.elastic.apm.android.test.testutils.spans.Spans;
import io.opentelemetry.sdk.trace.data.SpanData;

@Config(application = MainApp.class)
@RunWith(RobolectricTestRunner.class)
public class FragmentLifecycleInstrumentationTest extends BaseTest {

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
                        .isNamed(getClassSpanName(FullCreationFragment.class, " - Creating"));

                Spans.verify(onCreateSpan)
                        .isDirectChildOf(rootSpan)
                        .isNamed(getSpanMethodName(FullCreationFragment.class, FragmentMethod.ON_CREATE));
                Spans.verify(fragment.getOnCreateSpanContext()).belongsTo(onCreateSpan);

                Spans.verify(onCreateViewSpan)
                        .isDirectChildOf(rootSpan)
                        .isNamed(getSpanMethodName(FullCreationFragment.class, FragmentMethod.ON_CREATE_VIEW));
                Spans.verify(fragment.getOnCreateViewSpanContext()).belongsTo(onCreateViewSpan);

                Spans.verify(onViewCreatedSpan)
                        .isDirectChildOf(rootSpan)
                        .isNamed(getSpanMethodName(FullCreationFragment.class, FragmentMethod.ON_VIEW_CREATED));
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
                        .isNamed(getClassSpanName(ViewlessCreationFragment.class, " - Creating"));

                Spans.verify(onCreateSpan)
                        .isDirectChildOf(rootSpan)
                        .isNamed(getSpanMethodName(ViewlessCreationFragment.class, FragmentMethod.ON_CREATE));
                Spans.verify(fragment.getOnCreateSpanContext()).belongsTo(onCreateSpan);

                Spans.verify(onCreateViewSpan)
                        .isDirectChildOf(rootSpan)
                        .isNamed(getSpanMethodName(ViewlessCreationFragment.class, FragmentMethod.ON_CREATE_VIEW));
                Spans.verify(fragment.getOnCreateViewSpanContext()).belongsTo(onCreateViewSpan);

                assertNull(fragment.getOnViewCreatedSpanContext());
            });
        }
    }
}
