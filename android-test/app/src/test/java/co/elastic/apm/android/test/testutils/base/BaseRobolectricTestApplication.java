package co.elastic.apm.android.test.testutils.base;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import android.app.Application;

import org.robolectric.TestLifecycleApplication;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import co.elastic.apm.android.sdk.ElasticApmAgent;
import co.elastic.apm.android.sdk.ElasticApmConfiguration;
import co.elastic.apm.android.sdk.connectivity.Connectivity;
import co.elastic.apm.android.sdk.connectivity.opentelemetry.SignalConfiguration;
import co.elastic.apm.android.sdk.internal.configuration.Configuration;
import co.elastic.apm.android.sdk.internal.configuration.provider.ConfigurationsProvider;
import co.elastic.apm.android.sdk.internal.features.centralconfig.CentralConfigurationManager;
import co.elastic.apm.android.sdk.internal.features.centralconfig.initializer.CentralConfigurationInitializer;
import co.elastic.apm.android.sdk.internal.injection.AgentDependenciesInjector;
import co.elastic.apm.android.sdk.internal.time.ntp.NtpManager;
import co.elastic.apm.android.test.common.agent.AgentInitializer;
import co.elastic.apm.android.test.common.logs.LogRecordExporterCaptor;
import co.elastic.apm.android.test.common.metrics.MetricExporterCaptor;
import co.elastic.apm.android.test.common.metrics.MetricsFlusher;
import co.elastic.apm.android.test.common.spans.SpanExporterCaptor;
import co.elastic.apm.android.test.providers.ExportersProvider;
import co.elastic.apm.android.test.testutils.TestElasticClock;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.logs.GlobalLoggerProvider;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.logs.export.SimpleLogRecordProcessor;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;

public class BaseRobolectricTestApplication extends Application implements ExportersProvider,
        TestLifecycleApplication, AgentDependenciesInjector, AgentDependenciesInjector.Interceptor,
        ConfigurationsProvider {
    private final SpanExporterCaptor spanExporter;
    private final LogRecordExporterCaptor logRecordExporter;
    private final MetricExporterCaptor metricExporter;
    private final List<Configuration> configurations = new ArrayList<>();
    private NtpManager ntpManager;
    private CentralConfigurationInitializer centralConfigurationInitializer;

    protected void initializeAgent() {
        initializeAgent(null, null);
    }

    protected void initializeAgentWithCustomConfig(ElasticApmConfiguration configuration) {
        initializeAgent(configuration, null);
    }

    protected void initializeAgentWithCustomConnectivity(Connectivity connectivity) {
        initializeAgent(null, connectivity);
    }

    protected void initializeAgentWithExtraConfigurations(Configuration... configurations) {
        initializeAgent(null, null, configurations);
    }

    protected void initializeAgent(ElasticApmConfiguration configuration, Connectivity connectivity, Configuration... extraConfigs) {
        if (configuration == null) {
            configuration = ElasticApmConfiguration.getDefault();
        }
        if (extraConfigs != null) {
            configurations.addAll(Arrays.asList(extraConfigs));
        }
        AgentInitializer.injectSignalConfiguration(configuration, getSignalConfiguration());
        AgentInitializer.initialize(this, configuration, connectivity, this);
    }

    public BaseRobolectricTestApplication() {
        spanExporter = new SpanExporterCaptor();
        metricExporter = new MetricExporterCaptor();
        logRecordExporter = new LogRecordExporterCaptor();
        setUpAgentDependencies();
    }

    private void setUpAgentDependencies() {
        setUpNtpManager();
        setUpCentralConfigurationInitializer();
    }

    private void setUpCentralConfigurationInitializer() {
        centralConfigurationInitializer = mock(CentralConfigurationInitializer.class);
        CentralConfigurationManager centralConfigurationManager = mock(CentralConfigurationManager.class);
        doReturn(centralConfigurationManager).when(centralConfigurationInitializer).getManager();
    }

    private void setUpNtpManager() {
        Clock clock = new TestElasticClock();
        ntpManager = mock(NtpManager.class);
        doReturn(clock).when(ntpManager).getClock();
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

    protected SignalConfiguration getSignalConfiguration() {
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
        configurations.clear();
        ElasticApmAgent.resetForTest();
        Thread.setDefaultUncaughtExceptionHandler(null);
        GlobalOpenTelemetry.resetForTest();
        GlobalLoggerProvider.resetForTest();
    }

    @Override
    public NtpManager getNtpManager() {
        return ntpManager;
    }

    @Override
    public CentralConfigurationInitializer getCentralConfigurationInitializer() {
        return centralConfigurationInitializer;
    }

    @Override
    public ConfigurationsProvider getConfigurationsProvider() {
        return this;
    }

    @Override
    public AgentDependenciesInjector intercept(AgentDependenciesInjector agentDependenciesInjector) {
        configurations.addAll(agentDependenciesInjector.getConfigurationsProvider().provideConfigurations());
        return this;
    }

    @Override
    public List<Configuration> provideConfigurations() {
        return configurations;
    }
}
