package co.elastic.apm.android.test.testutils;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;

import android.app.Activity;

import org.mockito.ArgumentCaptor;
import org.robolectric.RuntimeEnvironment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SpanExporter;

public class BaseTest {

    private Map<String, String> activityMethodNames = new HashMap<>();

    public BaseTest() {
        activityMethodNames.put("onCreate", "$$robo$$android_app_Activity$performCreate");
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

    protected void verifyActivityMethodSpanName(SpanData span, String methodName) {
        String mappedName = activityMethodNames.get(methodName);
        assertEquals(Activity.class.getName() + "->" + mappedName, span.getName());
    }
}
