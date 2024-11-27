package co.elastic.apm.android.test.initialization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.Test;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.io.IOException;

import co.elastic.apm.android.sdk.ElasticApmAgent;
import co.elastic.apm.android.sdk.ElasticApmConfiguration;
import co.elastic.apm.android.sdk.connectivity.Connectivity;
import co.elastic.apm.android.sdk.connectivity.ExportProtocol;
import co.elastic.apm.android.sdk.connectivity.auth.impl.ApiKeyConfiguration;
import co.elastic.apm.android.sdk.connectivity.auth.impl.SecretTokenConfiguration;
import co.elastic.apm.android.sdk.connectivity.opentelemetry.DefaultSignalConfiguration;
import co.elastic.apm.android.sdk.connectivity.opentelemetry.SignalConfiguration;
import co.elastic.apm.android.sdk.connectivity.opentelemetry.exporters.ExporterVisitor;
import co.elastic.apm.android.sdk.connectivity.opentelemetry.exporters.VisitableExporters;
import co.elastic.apm.android.sdk.features.persistence.PersistenceConfiguration;
import co.elastic.apm.android.sdk.features.persistence.scheduler.ExportScheduler;
import co.elastic.apm.android.sdk.internal.features.centralconfig.poll.ConfigurationPollManager;
import co.elastic.apm.android.sdk.internal.features.persistence.PersistenceInitializer;
import co.elastic.apm.android.sdk.internal.opentelemetry.clock.ElasticClock;
import co.elastic.apm.android.sdk.internal.services.Service;
import co.elastic.apm.android.sdk.internal.services.ServiceManager;
import co.elastic.apm.android.sdk.internal.services.metadata.ApmMetadataService;
import co.elastic.apm.android.sdk.internal.services.periodicwork.PeriodicWorkService;
import co.elastic.apm.android.test.testutils.MainApp;
import co.elastic.apm.android.test.testutils.base.BaseRobolectricTest;
import co.elastic.apm.android.test.testutils.base.BaseRobolectricTestApplication;
import io.opentelemetry.exporter.otlp.http.logs.OtlpHttpLogRecordExporter;
import io.opentelemetry.exporter.otlp.http.metrics.OtlpHttpMetricExporter;
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter;
import io.opentelemetry.exporter.otlp.logs.OtlpGrpcLogRecordExporter;
import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporter;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.logs.LogRecordProcessor;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import io.opentelemetry.sdk.metrics.export.MetricReader;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;

public class InitializationTest extends BaseRobolectricTest {

    @Test
    public void verifyDefaults() throws IOException {
        MainApp app = getApp();

        PersistenceInitializer persistenceInitializer = app.getPersistenceInitializer();
        verify(persistenceInitializer, never()).prepare();
        verify(persistenceInitializer, never()).createSignalDiskExporter();
        verify(getAgentDependenciesInjector().getSessionManager()).initialize();

        PeriodicWorkService periodicWorkService = getPeriodicWorkService();
        assertTrue(periodicWorkService.isInitialized());
    }

    @Config(application = AppWithMockPollManager.class)
    @Test
    public void verifyCentralConfiguration_isInitialized() {
        AppWithMockPollManager app = getApp();

        assertTrue(getPeriodicWorkService().getTasks().contains(getApp().getCentralConfigurationInitializer()));
        assertEquals(app.pollManager, ConfigurationPollManager.get());
    }

    @Config(application = AppWithoutServerUrlSetInGradle.class)
    @Test
    public void whenServerUrlIsNotFound_failInitialization() {
        AppWithoutServerUrlSetInGradle app = getApp();

        assertTrue(app.errorWhenInitializing);
        assertEquals("serverUrl not found. You need to provide it in the Gradle config or set it up manually in the ElasticAgent runtime configuration. More info on: https://www.elastic.co/guide/en/apm/agent/android/current/configuration.html",
                app.errorMessage);
    }

    @Test
    public void verifyElapsedTimeClock_isInitialized() {
        ElasticClock clock = getAgentDependenciesInjector().getElasticClock();

        assertTrue(getPeriodicWorkService().getTasks().contains(clock));
    }

    @Test
    public void whenSecretTokenIsProvided_createConnectivityWithIt() {
        spyOnServices();
        ApmMetadataService service = ServiceManager.get().getService(Service.Names.METADATA);
        doReturn("someSecretToken").when(service).getSecretToken();

        assertTrue(Connectivity.getDefault().authConfiguration() instanceof SecretTokenConfiguration);
    }

    @Test
    public void whenApiKeyIsProvided_createConnectivityWithIt() {
        spyOnServices();
        ApmMetadataService service = ServiceManager.get().getService(Service.Names.METADATA);
        doReturn("someApiKey").when(service).getApiKey();

        assertTrue(Connectivity.getDefault().authConfiguration() instanceof ApiKeyConfiguration);
    }

    @Test
    public void whenSecretTokenAndApiKeyAreProvided_createConnectivityWithApiKey() {
        spyOnServices();
        ApmMetadataService service = ServiceManager.get().getService(Service.Names.METADATA);
        doReturn("someSecretToken").when(service).getSecretToken();
        doReturn("someApiKey").when(service).getApiKey();

        assertTrue(Connectivity.getDefault().authConfiguration() instanceof ApiKeyConfiguration);
    }

    @Test
    public void whenNoAuthKeysAreProvided_createSimpleConnectivity() {
        spyOnServices();
        ApmMetadataService service = ServiceManager.get().getService(Service.Names.METADATA);
        doReturn(null).when(service).getSecretToken();
        doReturn(null).when(service).getApiKey();

        assertNull(Connectivity.getDefault().authConfiguration());
    }

    @Config(application = AppWithPersistenceEnabled.class)
    @Test
    public void verifyPersistenceInitialization() throws IOException {
        AppWithPersistenceEnabled app = getApp();

        PersistenceInitializer persistenceInitializer = app.getPersistenceInitializer();

        verify(persistenceInitializer).prepare();
        verify(persistenceInitializer).createSignalDiskExporter();
        verify(app.exportScheduler).onPersistenceEnabled();
        verifyNoMoreInteractions(app.exportScheduler);
        assertEquals(persistenceInitializer, app.capturedExporterVisitor);
    }

    @Config(application = AppWithPersistenceEnabledWithoutVisitableExporters.class)
    @Test
    public void whenSignalConfigurationDoesNotAllowVisitingExporters_verifyPersistenceInitialization() throws IOException {
        AppWithPersistenceEnabledWithoutVisitableExporters app = getApp();

        PersistenceInitializer persistenceInitializer = app.getPersistenceInitializer();

        verify(persistenceInitializer, never()).prepare();
        verify(persistenceInitializer, never()).createSignalDiskExporter();
        verify(app.exportScheduler).onPersistenceDisabled();
    }

    @Config(application = AppWithDefaultExporterProtocol.class)
    @Test
    public void verifyDefaultExporters() {
        AppWithDefaultExporterProtocol app = getApp();

        assertTrue(app.spanExporter instanceof OtlpGrpcSpanExporter);
        assertTrue(app.metricExporter instanceof OtlpGrpcMetricExporter);
        assertTrue(app.logRecordExporter instanceof OtlpGrpcLogRecordExporter);
    }

    @Config(application = AppWithHTTPExporterProtocol.class)
    @Test
    public void verifyHttpExporters() {
        AppWithHTTPExporterProtocol app = getApp();

        assertTrue(app.spanExporter instanceof OtlpHttpSpanExporter);
        assertTrue(app.metricExporter instanceof OtlpHttpMetricExporter);
        assertTrue(app.logRecordExporter instanceof OtlpHttpLogRecordExporter);
    }

    private static PeriodicWorkService getPeriodicWorkService() {
        return ServiceManager.get().getService(Service.Names.PERIODIC_WORK);
    }

    @SuppressWarnings("unchecked")
    private static <T extends BaseRobolectricTestApplication> T getApp() {
        return (T) RuntimeEnvironment.getApplication();
    }

    private static class AppWithMockPollManager extends BaseRobolectricTestApplication {
        private ConfigurationPollManager pollManager;

        @Override
        public void onCreate() {
            super.onCreate();
            pollManager = mock(ConfigurationPollManager.class);
            doReturn(pollManager).when(getCentralConfigurationInitializer()).getPollManager();
            initializeAgent();
        }
    }

    private static class AppWithPersistenceEnabled extends BaseRobolectricTestApplication implements SignalConfiguration, VisitableExporters {
        private ExportScheduler exportScheduler;
        private ExporterVisitor capturedExporterVisitor;

        @Override
        public void onCreate() {
            super.onCreate();
            exportScheduler = mock(ExportScheduler.class);
            PersistenceConfiguration persistenceConfiguration = PersistenceConfiguration.builder()
                    .setExportScheduler(exportScheduler)
                    .setEnabled(true).build();

            initializeAgentWithCustomConfig(ElasticApmConfiguration.builder()
                    .setPersistenceConfiguration(persistenceConfiguration)
                    .build());
        }

        @Override
        protected SignalConfiguration getSignalConfiguration() {
            return this;
        }

        @Override
        public SpanProcessor getSpanProcessor() {
            return mock(SpanProcessor.class);
        }

        @Override
        public LogRecordProcessor getLogProcessor() {
            return mock(LogRecordProcessor.class);
        }

        @Override
        public MetricReader getMetricReader() {
            return mock(MetricReader.class);
        }

        @Override
        public void setExporterVisitor(ExporterVisitor exporterVisitor) {
            capturedExporterVisitor = exporterVisitor;
        }
    }

    private static class AppWithPersistenceEnabledWithoutVisitableExporters extends BaseRobolectricTestApplication {
        private ExportScheduler exportScheduler;

        @Override
        public void onCreate() {
            super.onCreate();
            exportScheduler = mock(ExportScheduler.class);
            PersistenceConfiguration persistenceConfiguration = PersistenceConfiguration.builder()
                    .setExportScheduler(exportScheduler)
                    .setEnabled(true).build();

            initializeAgentWithCustomConfig(ElasticApmConfiguration.builder()
                    .setPersistenceConfiguration(persistenceConfiguration)
                    .build());
        }
    }

    private static class AppWithoutServerUrlSetInGradle extends BaseRobolectricTestApplication {
        public boolean errorWhenInitializing = false;
        public String errorMessage;

        @Override
        public void onCreate() {
            super.onCreate();
            ServiceManager.setInitializationCallback(() -> {
                spyOnServices();
                ApmMetadataService service = ServiceManager.get().getService(Service.Names.METADATA);
                doReturn(null).when(service).getServerUrl();
            });
            try {
                ElasticApmAgent.initialize(this);
            } catch (IllegalArgumentException e) {
                errorWhenInitializing = true;
                errorMessage = e.getMessage();
            }
        }
    }

    private static class BaseAppWithCustomSignalConfiguration extends BaseRobolectricTestApplication implements ExporterVisitor {
        public SpanExporter spanExporter;
        public LogRecordExporter logRecordExporter;
        public MetricExporter metricExporter;
        private DefaultSignalConfiguration signalConfiguration;

        @Override
        public void onCreate() {
            super.onCreate();
            signalConfiguration = SignalConfiguration.create();
            signalConfiguration.setExporterVisitor(this);
        }

        @Override
        protected SignalConfiguration getSignalConfiguration() {
            return signalConfiguration;
        }

        @Override
        public <T> T visitExporter(T exporter) {
            if (exporter instanceof SpanExporter) {
                spanExporter = (SpanExporter) exporter;
            } else if (exporter instanceof LogRecordExporter) {
                logRecordExporter = (LogRecordExporter) exporter;
            } else if (exporter instanceof MetricExporter) {
                metricExporter = (MetricExporter) exporter;
            }
            return exporter;
        }
    }

    private static class AppWithHTTPExporterProtocol extends BaseAppWithCustomSignalConfiguration {
        @Override
        public void onCreate() {
            super.onCreate();

            initializeAgentWithCustomConfig(ElasticApmConfiguration.builder()
                    .setExportProtocol(ExportProtocol.HTTP)
                    .build());
        }
    }

    private static class AppWithDefaultExporterProtocol extends BaseAppWithCustomSignalConfiguration {
        @Override
        public void onCreate() {
            super.onCreate();
            initializeAgent();
        }
    }
}
