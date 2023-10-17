package co.elastic.apm.android.test;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.CountDownLatch;

import co.elastic.apm.android.sdk.ElasticApmAgent;
import co.elastic.apm.android.sdk.ElasticApmConfiguration;
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
    private ElasticApmConfiguration originalAgentConfig;
    private ElasticApmConfiguration currentAgentConfig;

    @Override
    public void onCreate() {
        super.onCreate();
        originalAgentConfig = ElasticApmConfiguration.builder().setSignalConfiguration(getSignalConfiguration()).build();
        initializeAgent(originalAgentConfig);
    }

    public void reInitializeAgent(ElasticApmConfiguration configuration) throws InterruptedException {
        ElasticApmAgent.resetForTest();
        if (Looper.myLooper() == Looper.getMainLooper()) {
            initializeAgent(configuration);
        } else {
            CountDownLatch countDownLatch = new CountDownLatch(1);
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(() -> {
                initializeAgent(configuration);
                countDownLatch.countDown();
            });
            countDownLatch.await();
        }
    }

    private void initializeAgent(ElasticApmConfiguration configuration) {
        currentAgentConfig = configuration;
        AgentInitializer.initialize(this, configuration);
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

    public void reset() throws InterruptedException {
        spanExporter.clearCapturedSpans();
        metricExporter.clearCapturedMetrics();
        logRecordExporter.clearCapturedLogs();
        if (currentAgentConfig != originalAgentConfig || !ElasticApmAgent.isInitialized()) {
            reInitializeAgent(originalAgentConfig);
        }
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
