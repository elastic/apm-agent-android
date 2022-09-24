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

    protected enum ActivityMethodName {
        ON_CREATE("$$robo$$android_app_Activity$performCreate");

        private final String robolectricName;

        ActivityMethodName(String robolectricName) {
            this.robolectricName = robolectricName;
        }
    }

    @SuppressWarnings("unchecked")
    protected List<SpanData> getSentSpans() {
        SpanExporter spanExporter = getSpanExporter();
        ArgumentCaptor<List<SpanData>> captor = ArgumentCaptor.forClass(List.class);
        verify(spanExporter).export(captor.capture());

        return captor.getValue();
    }

    protected SpanExporter getSpanExporter() {
        SpanExporterProvider spanExporterProvider = (SpanExporterProvider) RuntimeEnvironment.getApplication();
        return spanExporterProvider.getSpanExporter();
    }

    protected void verifyActivityMethodSpanName(SpanData span, ActivityMethodName name) {
        assertEquals(Activity.class.getName() + "->" + name.robolectricName, span.getName());
    }
}
