package co.elastic.apm.android.test.initialization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.lang.reflect.Field;

import co.elastic.apm.android.sdk.ElasticApmConfiguration;
import co.elastic.apm.android.sdk.connectivity.Connectivity;
import co.elastic.apm.android.sdk.connectivity.auth.impl.ApiKeyConfiguration;
import co.elastic.apm.android.sdk.connectivity.auth.impl.SecretTokenConfiguration;
import co.elastic.apm.android.sdk.internal.features.centralconfig.poll.ConfigurationPollManager;
import co.elastic.apm.android.sdk.internal.services.Service;
import co.elastic.apm.android.sdk.internal.services.ServiceManager;
import co.elastic.apm.android.sdk.internal.services.metadata.ApmMetadataService;
import co.elastic.apm.android.sdk.session.impl.DefaultSessionIdProvider;
import co.elastic.apm.android.test.testutils.base.BaseRobolectricTest;
import co.elastic.apm.android.test.testutils.base.BaseRobolectricTestApplication;

public class InitializationTest extends BaseRobolectricTest {

    @Config(application = AppWithMockSessionId.class)
    @Test
    public void whenSessionIdProviderIsInitializable_initializeIt() {
        AppWithMockSessionId app = (AppWithMockSessionId) RuntimeEnvironment.getApplication();

        verify(app.sessionIdProvider).initialize();
    }

    @Config(application = AppWithMockPollManager.class)
    @Test
    public void verifyCentralConfiguration_isInitialized() {
        AppWithMockPollManager app = (AppWithMockPollManager) RuntimeEnvironment.getApplication();

        verify(getAgentDependenciesInjector().getCentralConfigurationInitializer()).initialize();

        assertEquals(app.pollManager, ConfigurationPollManager.get());
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
}
