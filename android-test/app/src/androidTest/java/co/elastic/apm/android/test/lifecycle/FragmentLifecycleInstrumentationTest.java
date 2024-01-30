package co.elastic.apm.android.test.lifecycle;


import androidx.fragment.app.testing.FragmentScenario;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import co.elastic.apm.android.sdk.ElasticApmConfiguration;
import co.elastic.apm.android.sdk.instrumentation.InstrumentationConfiguration;
import co.elastic.apm.android.test.common.spans.Spans;
import co.elastic.apm.android.test.fragments.FullCreationFragment;
import io.opentelemetry.sdk.trace.data.SpanData;

public class FragmentLifecycleInstrumentationTest extends BaseLifecycleInstrumentationTest {

    @Before
    public void setUp() {
        getSpanExporter().clearCapturedSpans();
    }

    @Test
    public void onCreation_wrapWithSpans() {
        try (FragmentScenario<FullCreationFragment> scenario = FragmentScenario.launchInContainer(FullCreationFragment.class)) {
            scenario.onFragment(fragment -> {
                List<SpanData> spans = getRecordedSpans(2);
                SpanData onCreateActivitySpan = spans.get(0);
                SpanData onCreateFragmentSpan = spans.get(1);

                Spans.verify(onCreateActivitySpan)
                        .hasNoParent()
                        .hasAttribute("screen.name", "EmptyFragmentActivity")
                        .isNamed(getSpanMethodName(FragmentMethod.ON_CREATE));

                Spans.verify(onCreateFragmentSpan)
                        .hasNoParent()
                        .hasAttribute("screen.name", "FullCreationFragment")
                        .isNamed(getSpanMethodName(FragmentMethod.ON_CREATE));
                Spans.verify(fragment.getOnCreateSpanContext()).belongsTo(onCreateFragmentSpan);
                Spans.verify(fragment.getOnCreateViewSpanContext()).belongsTo(onCreateFragmentSpan);
                Spans.verify(fragment.getOnViewCreatedSpanContext()).belongsTo(onCreateFragmentSpan);
            });
        }
    }

    @Test
    public void whenInstrumentationIsDisabled_doNotSendScreenRenderingSpans() {
        ElasticApmConfiguration configuration = ElasticApmConfiguration.builder()
                .setInstrumentationConfiguration(InstrumentationConfiguration.builder().enableScreenRendering(false).build())
                .build();
        overrideAgentConfiguration(configuration);
        try (FragmentScenario<FullCreationFragment> scenario = FragmentScenario.launchInContainer(FullCreationFragment.class)) {
            scenario.onFragment(fullCreationFragment -> getRecordedSpans(0));
        }
    }
}
