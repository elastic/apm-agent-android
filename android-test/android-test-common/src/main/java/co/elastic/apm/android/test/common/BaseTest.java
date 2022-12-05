package co.elastic.apm.android.test.common;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import org.junit.Before;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import co.elastic.apm.android.sdk.internal.injection.AgentDependenciesInjector;
import co.elastic.apm.android.sdk.internal.time.ntp.NtpManager;
import co.elastic.apm.android.test.common.spans.SpanExporterCaptor;
import io.opentelemetry.sdk.trace.data.SpanData;

public abstract class BaseTest {
    protected AgentDependenciesInjector injector;
    protected NtpManager ntpManager;

    @Before
    public void commonBaseSetUp() throws NoSuchFieldException, IllegalAccessException {
        injector = mock(AgentDependenciesInjector.class);
        ntpManager = mock(NtpManager.class);
        doReturn(ntpManager).when(injector).getNtpManager();

        Field instanceField = AgentDependenciesInjector.class.getDeclaredField("INSTANCE");
        instanceField.setAccessible(true);
        instanceField.set(null, injector);
    }

    protected List<SpanData> getRecordedSpans(int amountExpected) {
        List<SpanData> spans = getCapturedSpansOrderedByCreation(getSpanExporter());
        assertEquals(amountExpected, spans.size());

        return spans;
    }

    private List<SpanData> getCapturedSpansOrderedByCreation(SpanExporterCaptor spanExporter) {
        List<SpanData> spans = new ArrayList<>();
        for (List<SpanData> list : spanExporter.getCapturedSpans()) {
            if (list.size() > 1) {
                // Since we're using SimpleSpanProcessor, each call to SpanExporter.export must contain
                // only one span.
                throw new IllegalStateException();
            }
            spans.add(list.get(0));
        }

        spans.sort(Comparator.comparing(SpanData::getStartEpochNanos));
        spanExporter.clearCapturedSpans();
        return spans;
    }

    protected abstract SpanExporterCaptor getSpanExporter();

    protected String getClassSpanName(Class<?> theClass, String suffix) {
        return theClass.getName() + suffix;
    }

    protected SpanData getRecordedSpan() {
        return getRecordedSpans(1).get(0);
    }
}
