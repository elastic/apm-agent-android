package co.elastic.apm.android.test;

import android.app.Application;

import co.elastic.apm.android.sdk.connectivity.opentelemetry.SignalConfiguration;
import co.elastic.apm.android.test.common.agent.AgentInitializer;
import co.elastic.apm.android.test.common.logs.LogRecordExporterCaptor;
import co.elastic.apm.android.test.common.metrics.MetricExporterCaptor;
import co.elastic.apm.android.test.common.metrics.MetricsFlusher;
import co.elastic.apm.android.test.common.spans.SpanExporterCaptor;
import co.elastic.apm.android.test.providers.ExportersProvider;
import io.opentelemetry.sdk.logs.export.SimpleLogRecordProcessor;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;

public class DefaultApp extends Application implements ExportersProvider {
    private final SpanExporterCaptor spanExporter;
    private final LogRecordExporterCaptor logRecordExporter;
    private final MetricExporterCaptor metricExporter;

    @Override
    public void onCreate() {
        super.onCreate();
        AgentInitializer.initialize(this, getSignalConfiguration());
    }

    protected SignalConfiguration getSignalConfiguration() {
        PeriodicMetricReader metricReader = PeriodicMetricReader.create(metricExporter);
        MetricsFlusher flusher = new MetricsFlusher(metricReader);
        metricExporter.setFlusher(flusher);
        return SignalConfiguration.custom(SimpleSpanProcessor.create(spanExporter),
                SimpleLogRecordProcessor.create(logRecordExporter),
                metricReader);
    }

    public DefaultApp() {
        spanExporter = new SpanExporterCaptor();
        metricExporter = new MetricExporterCaptor();
        logRecordExporter = new LogRecordExporterCaptor();
    }

    @Override
    public SpanExporterCaptor getSpanExporter() {
        return spanExporter;
    }

    @Override
    public MetricExporterCaptor getMetricExporter() {
        return metricExporter;
    }

    @Override
    public LogRecordExporterCaptor getLogRecordExporter() {
        return logRecordExporter;
    }
}
