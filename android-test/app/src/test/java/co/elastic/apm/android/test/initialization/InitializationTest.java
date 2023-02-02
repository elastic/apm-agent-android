package co.elastic.apm.android.test.initialization;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import co.elastic.apm.android.sdk.ElasticApmAgent;
import co.elastic.apm.android.sdk.ElasticApmConfiguration;
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

    private static class AppWithMockSessionId extends BaseRobolectricTestApplication {
        private DefaultSessionIdProvider sessionIdProvider;

        @Override
        public void onCreate() {
            super.onCreate();
            sessionIdProvider = mock(DefaultSessionIdProvider.class);
            ElasticApmAgent.initialize(this,
                    ElasticApmConfiguration.builder().setSessionIdProvider(sessionIdProvider).build(),
                    getConnectivity());
        }
    }
}
