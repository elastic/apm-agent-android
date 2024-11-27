package co.elastic.apm.android.test.testutils.base;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import android.app.Application;

import org.robolectric.TestLifecycleApplication;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.elastic.apm.android.sdk.ElasticApmAgent;
import co.elastic.apm.android.sdk.ElasticApmConfiguration;
import co.elastic.apm.android.sdk.connectivity.Connectivity;
import co.elastic.apm.android.sdk.connectivity.opentelemetry.SignalConfiguration;
import co.elastic.apm.android.sdk.internal.configuration.Configuration;
import co.elastic.apm.android.sdk.internal.configuration.provider.ConfigurationsProvider;
import co.elastic.apm.android.sdk.internal.features.centralconfig.CentralConfigurationManager;
import co.elastic.apm.android.sdk.internal.features.centralconfig.initializer.CentralConfigurationInitializer;
import co.elastic.apm.android.sdk.internal.features.persistence.PersistenceInitializer;
import co.elastic.apm.android.sdk.internal.injection.AgentDependenciesInjector;
import co.elastic.apm.android.sdk.internal.opentelemetry.clock.ElasticClock;
import co.elastic.apm.android.sdk.internal.services.Service;
import co.elastic.apm.android.sdk.internal.services.ServiceManager;
import co.elastic.apm.android.sdk.session.SessionManager;
import co.elastic.apm.android.test.common.agent.AgentInitializer;
import co.elastic.apm.android.test.common.logs.LogRecordExporterCaptor;
import co.elastic.apm.android.test.common.metrics.MetricExporterCaptor;
import co.elastic.apm.android.test.common.metrics.MetricsFlusher;
import co.elastic.apm.android.test.common.spans.SpanExporterCaptor;
import co.elastic.apm.android.test.providers.ExportersProvider;
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
    private ElasticClock clock;
    private SessionManager sessionManager;
    private CentralConfigurationInitializer centralConfigurationInitializer;
    private PersistenceInitializer persistenceInitializer;

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

    @SuppressWarnings("unchecked")
    protected void spyOnServices() {
        try {
            ServiceManager instance = ServiceManager.get();
            Field field = ServiceManager.class.getDeclaredField("services");
            field.setAccessible(true);
            Map<String, Service> services = (Map<String, Service>) field.get(instance);
            Map<String, Service> spies = new HashMap<>();
            services.forEach((key, service) -> spies.put(key, spy(service)));
            field.set(instance, spies);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public BaseRobolectricTestApplication() {
        spanExporter = new SpanExporterCaptor();
        metricExporter = new MetricExporterCaptor();
        logRecordExporter = new LogRecordExporterCaptor();
        setUpAgentDependencies();
    }

    private void setUpAgentDependencies() {
        setUpClock();
        setUpCentralConfigurationInitializer();
        persistenceInitializer = mock(PersistenceInitializer.class);
        setUpSessionManager();
    }

    private void setUpCentralConfigurationInitializer() {
        centralConfigurationInitializer = mock(CentralConfigurationInitializer.class);
        CentralConfigurationManager centralConfigurationManager = mock(CentralConfigurationManager.class);
        doReturn(centralConfigurationManager).when(centralConfigurationInitializer).getManager();
    }

    private void setUpClock() {
        clock = mock();
    }

    private void setUpSessionManager() {
        sessionManager = mock(SessionManager.class);
        doReturn("SESSION-ID").when(sessionManager).getSessionId();
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
    }

    @Override
    public ElasticClock getElasticClock() {
        return clock;
    }

    @Override
    public SessionManager getSessionManager() {
        return sessionManager;
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
    public PersistenceInitializer getPersistenceInitializer() {
        return persistenceInitializer;
    }

    @Override
    public AgentDependenciesInjector intercept(AgentDependenciesInjector agentDependenciesInjector) {
        configurations.addAll(agentDependenciesInjector.getConfigurationsProvider().provideConfigurations());
        return this;
    }

    @Override
    public List<Configuration> provideConfigurations() {
        List<Configuration> spies = new ArrayList<>();
        for (Configuration configuration : configurations) {
            try {
                spies.add(spy(configuration));
            } catch (IllegalArgumentException ignored) {
                spies.add(configuration);
            }
        }
        return spies;
    }
}
