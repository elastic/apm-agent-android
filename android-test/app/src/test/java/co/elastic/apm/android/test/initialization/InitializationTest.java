package co.elastic.apm.android.test.initialization;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.lang.reflect.Field;

import co.elastic.apm.android.sdk.ElasticApmConfiguration;
import co.elastic.apm.android.sdk.internal.features.centralconfig.poll.ConfigurationPollManager;
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
