package co.elastic.apm.android.test.testutils.base;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import android.app.Application;

import org.robolectric.TestLifecycleApplication;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import co.elastic.apm.android.sdk.ElasticApmAgent;
import co.elastic.apm.android.sdk.ElasticApmConfiguration;
import co.elastic.apm.android.sdk.connectivity.opentelemetry.SignalConfiguration;
import co.elastic.apm.android.sdk.internal.injection.AgentDependenciesInjector;
import co.elastic.apm.android.sdk.internal.time.ntp.NtpManager;
import co.elastic.apm.android.test.common.agent.AgentInitializer;
import co.elastic.apm.android.test.common.logs.LogRecordExporterCaptor;
import co.elastic.apm.android.test.common.metrics.MetricExporterCaptor;
import co.elastic.apm.android.test.common.metrics.MetricsFlusher;
import co.elastic.apm.android.test.common.spans.SpanExporterCaptor;
import co.elastic.apm.android.test.providers.ExportersProvider;
import co.elastic.apm.android.test.testutils.AgentDependenciesProvider;
import co.elastic.apm.android.test.testutils.TestElasticClock;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.logs.GlobalLoggerProvider;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.logs.export.SimpleLogRecordProcessor;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;

public class BaseRobolectricTestApplication extends Application implements ExportersProvider,
        TestLifecycleApplication, AgentDependenciesProvider {
    private final SpanExporterCaptor spanExporter;
    private final LogRecordExporterCaptor logRecordExporter;
    private final MetricExporterCaptor metricExporter;
    private AgentDependenciesInjector injector;
    private NtpManager ntpManager;

    protected void initializeAgentWithCustomConfig(ElasticApmConfiguration configuration) {
        AgentInitializer.initialize(this, configuration, getSignalConfiguration());
    }

    protected void initializeAgent() {
        AgentInitializer.initialize(this, getSignalConfiguration());
    }

    public BaseRobolectricTestApplication() {
        spanExporter = new SpanExporterCaptor();
        metricExporter = new MetricExporterCaptor();
        logRecordExporter = new LogRecordExporterCaptor();
        setUpAgentDependencies();
    }

    private void setUpAgentDependencies() {
        injector = mock(AgentDependenciesInjector.class);
        Clock clock = new TestElasticClock();
        ntpManager = mock(NtpManager.class);
        doReturn(clock).when(ntpManager).getClock();
        doReturn(ntpManager).when(injector).getNtpManager();

        try {
            Field instanceField = AgentDependenciesInjector.class.getDeclaredField("INSTANCE");
            instanceField.setAccessible(true);
            instanceField.set(null, injector);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
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

    private SignalConfiguration getSignalConfiguration() {
        PeriodicMetricReader metricReader = PeriodicMetricReader.create(metricExporter);
        MetricsFlusher flusher = new MetricsFlusher(metricReader);
        metricExporter.setFlusher(flusher);
        return SignalConfiguration.custom(SimpleSpanProcessor.create(spanExporter),
                SimpleLogRecordProcessor.create(logRecordExporter),
                metricReader);
    }

    @Override
    public void beforeTest(Method method) {

    }

    @Override
    public void prepareTest(Object test) {

    }

    @Override
    public void afterTest(Method method) {
        ElasticApmAgent.resetForTest();
        Thread.setDefaultUncaughtExceptionHandler(null);
        GlobalOpenTelemetry.resetForTest();
        GlobalLoggerProvider.resetForTest();
    }

    @Override
    public NtpManager getNtpManager() {
        return ntpManager;
    }
}
