package co.elastic.apm.android.test.testutils;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;

import android.app.Activity;

import org.mockito.ArgumentCaptor;
import org.robolectric.RuntimeEnvironment;

import java.util.List;

import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SpanExporter;

public class BaseTest {

    protected enum ActivityMethod {
        ON_CREATE("$$robo$$android_app_Activity$performCreate");

        private final String robolectricName;

        ActivityMethod(String robolectricName) {
            this.robolectricName = robolectricName;
        }
    }

    @SuppressWarnings("unchecked")
    protected List<SpanData> getRecordedSpans(int amountExpected) {
        SpanExporter spanExporter = getSpanExporter();
        ArgumentCaptor<List<SpanData>> captor = ArgumentCaptor.forClass(List.class);
        verify(spanExporter).export(captor.capture());
        List<SpanData> spans = captor.getValue();
        assertEquals(amountExpected, spans.size());

        return spans;
    }

    protected SpanExporter getSpanExporter() {
        SpanExporterProvider spanExporterProvider = (SpanExporterProvider) RuntimeEnvironment.getApplication();
        return spanExporterProvider.getSpanExporter();
    }

    protected String getSpanMethodName(ActivityMethod method) {
        return Activity.class.getName() + "->" + method.robolectricName;
    }

    protected SpanData getRecordedSpan() {
        return getRecordedSpans(1).get(0);
    }
}
