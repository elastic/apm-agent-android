package co.elastic.apm.android.test;

import android.app.Application;

import co.elastic.apm.android.sdk.ElasticApmAgent;
import co.elastic.apm.android.sdk.traces.connectivity.Connectivity;
import co.elastic.apm.android.test.common.spans.SpanExporterCaptor;
import co.elastic.apm.android.test.utilities.SpanExporterProvider;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;

public class DefaultApp extends Application implements SpanExporterProvider {
    private final SpanExporterCaptor exporter;

    @Override
    public void onCreate() {
        super.onCreate();
        ElasticApmAgent.initialize(this, getConnectivity());
    }

    protected Connectivity getConnectivity() {
        return Connectivity.custom(SimpleSpanProcessor.create(exporter));
    }

    public DefaultApp() {
        exporter = new SpanExporterCaptor();
    }

    @Override
    public SpanExporterCaptor getSpanExporter() {
        return exporter;
    }
}
