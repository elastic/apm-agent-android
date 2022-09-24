package co.elastic.apm.android.test.testutils;

import static org.mockito.Mockito.mock;

import android.app.Application;

import co.elastic.apm.android.sdk.traces.connectivity.Connectivity;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;

public class BaseTestApplication extends Application implements SpanExporterProvider {
    protected final SpanExporter exporter;

    public BaseTestApplication() {
        exporter = mock(SpanExporter.class);
    }

    @Override
    public SpanExporter getSpanExporter() {
        return exporter;
    }

    protected Connectivity getConnectivity() {
        return Connectivity.custom(SimpleSpanProcessor.create(exporter));
    }
}
