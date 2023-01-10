package co.elastic.apm.android.test;

import android.app.Application;

import co.elastic.apm.android.sdk.ElasticApmAgent;
import co.elastic.apm.android.sdk.connectivity.Connectivity;
import co.elastic.apm.android.test.common.metrics.MetricExporterCaptor;
import co.elastic.apm.android.test.common.spans.SpanExporterCaptor;
import co.elastic.apm.android.test.providers.ExportersProvider;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;

public class DefaultApp extends Application implements ExportersProvider {
    private final SpanExporterCaptor spanExporter;
    private final MetricExporterCaptor metricExporter;

    @Override
    public void onCreate() {
        super.onCreate();
        ElasticApmAgent.initialize(this, getConnectivity());
    }

    protected Connectivity getConnectivity() {
        return Connectivity.custom(SimpleSpanProcessor.create(spanExporter), PeriodicMetricReader.create(metricExporter));
    }

    public DefaultApp() {
        spanExporter = new SpanExporterCaptor();
        metricExporter = new MetricExporterCaptor();
    }

    @Override
    public SpanExporterCaptor getSpanExporter() {
        return spanExporter;
    }

    @Override
    public MetricExporterCaptor getMetricExporter() {
        return metricExporter;
    }
}
