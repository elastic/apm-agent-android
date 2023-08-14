package co.elastic.apm.android.test.initialization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.io.IOException;
import java.lang.reflect.Field;

import co.elastic.apm.android.sdk.ElasticApmAgent;
import co.elastic.apm.android.sdk.ElasticApmConfiguration;
import co.elastic.apm.android.sdk.connectivity.Connectivity;
import co.elastic.apm.android.sdk.connectivity.auth.impl.ApiKeyConfiguration;
import co.elastic.apm.android.sdk.connectivity.auth.impl.SecretTokenConfiguration;
import co.elastic.apm.android.sdk.connectivity.opentelemetry.SignalConfiguration;
import co.elastic.apm.android.sdk.connectivity.opentelemetry.exporters.ExporterVisitor;
import co.elastic.apm.android.sdk.connectivity.opentelemetry.exporters.VisitableExporters;
import co.elastic.apm.android.sdk.features.persistence.PersistenceConfiguration;
import co.elastic.apm.android.sdk.internal.features.centralconfig.poll.ConfigurationPollManager;
import co.elastic.apm.android.sdk.internal.features.persistence.PersistenceInitializer;
import co.elastic.apm.android.sdk.internal.services.Service;
import co.elastic.apm.android.sdk.internal.services.ServiceManager;
import co.elastic.apm.android.sdk.internal.services.metadata.ApmMetadataService;
import co.elastic.apm.android.sdk.internal.services.periodicwork.PeriodicWorkService;
import co.elastic.apm.android.sdk.internal.time.ntp.NtpManager;
import co.elastic.apm.android.sdk.session.impl.DefaultSessionIdProvider;
import co.elastic.apm.android.test.testutils.MainApp;
import co.elastic.apm.android.test.testutils.base.BaseRobolectricTest;
import co.elastic.apm.android.test.testutils.base.BaseRobolectricTestApplication;
import io.opentelemetry.sdk.logs.LogRecordProcessor;
import io.opentelemetry.sdk.metrics.export.MetricReader;
import io.opentelemetry.sdk.trace.SpanProcessor;

public class InitializationTest extends BaseRobolectricTest {

    @Config(application = MainApp.class)
    @Test
    public void verifyDefaults() throws IOException {
        MainApp app = getApp();

        PersistenceInitializer persistenceInitializer = app.getPersistenceInitializer();
        verify(persistenceInitializer, never()).prepare();
        verify(persistenceInitializer, never()).createSignalDiskExporter();

        PeriodicWorkService periodicWorkService = getPeriodicWorkService();
        assertTrue(periodicWorkService.isInitialized());
    }

    @Config(application = AppWithMockSessionId.class)
    @Test
    public void whenSessionIdProviderIsInitializable_initializeIt() {
        AppWithMockSessionId app = getApp();

        verify(app.sessionIdProvider).initialize();
    }

    @Config(application = AppWithMockPollManager.class)
    @Test
    public void verifyCentralConfiguration_isInitialized() {
        AppWithMockPollManager app = getApp();

        assertTrue(getPeriodicWorkService().getTasks().contains(getApp().getCentralConfigurationInitializer()));
        assertEquals(app.pollManager, ConfigurationPollManager.get());
    }

    @Test
    public void verifyNtpManager_isInitialized() {
        NtpManager ntpManager = getAgentDependenciesInjector().getNtpManager();

        verify(ntpManager).initialize();
        assertTrue(getPeriodicWorkService().getTasks().contains(ntpManager));
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
        assertEquals(persistenceInitializer, app.capturedExporterVisitor);
    }

    @Config(application = AppWithPersistenceEnabledWithoutVisitableExporters.class)
    @Test
    public void whenSignalConfigurationDoesNotAllowVisitingExporters_verifyPersistenceInitialization() throws IOException {
        AppWithPersistenceEnabledWithoutVisitableExporters app = getApp();

        PersistenceInitializer persistenceInitializer = app.getPersistenceInitializer();

        verify(persistenceInitializer, never()).prepare();
        verify(persistenceInitializer, never()).createSignalDiskExporter();
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

    private static class AppWithMockSessionId extends BaseRobolectricTestApplication {
        private DefaultSessionIdProvider sessionIdProvider;

        @Override
        public void onCreate() {
            super.onCreate();
            sessionIdProvider = mock(DefaultSessionIdProvider.class);
            ElasticApmConfiguration configuration = ElasticApmConfiguration.builder().build();
            setSessionIdProvider(configuration, sessionIdProvider);

            initializeAgentWithCustomConfig(configuration);
        }

        private void setSessionIdProvider(ElasticApmConfiguration configuration, DefaultSessionIdProvider sessionIdProvider) {
            try {
                Field sessionIdProviderField = configuration.getClass().getDeclaredField("sessionIdProvider");
                sessionIdProviderField.setAccessible(true);
                sessionIdProviderField.set(configuration, sessionIdProvider);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static class AppWithPersistenceEnabled extends BaseRobolectricTestApplication implements SignalConfiguration, VisitableExporters {
        private ExporterVisitor capturedExporterVisitor;

        @Override
        public void onCreate() {
            super.onCreate();
            PersistenceConfiguration persistenceConfiguration = PersistenceConfiguration.builder().setEnabled(true).build();

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
        @Override
        public void onCreate() {
            super.onCreate();
            PersistenceConfiguration persistenceConfiguration = PersistenceConfiguration.builder().setEnabled(true).build();

            initializeAgentWithCustomConfig(ElasticApmConfiguration.builder()
                    .setPersistenceConfiguration(persistenceConfiguration)
                    .build());
        }
    }
}
