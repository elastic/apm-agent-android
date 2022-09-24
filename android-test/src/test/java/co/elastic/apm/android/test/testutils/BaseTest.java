package co.elastic.apm.android.test.testutils;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;

import android.app.Activity;

import org.mockito.ArgumentCaptor;
import org.robolectric.RuntimeEnvironment;

import java.util.List;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.context.Context;
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

    protected void verifySuccessfulSpan(SpanData span) {
        assertEquals(StatusCode.UNSET, span.getStatus().getStatusCode());
    }

    protected void verifyContextSource(Context context, SpanData source) {
        SpanContext spanContext = Span.fromContext(context).getSpanContext();
        assertEquals(spanContext, source.getSpanContext());
    }

    protected void verifyActivityMethodSpanName(SpanData span, ActivityMethod method) {
        assertEquals(Activity.class.getName() + "->" + method.robolectricName, span.getName());
    }
}
