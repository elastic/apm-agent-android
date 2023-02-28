package co.elastic.apm.android.test.configuration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.robolectric.annotation.Config;

import co.elastic.apm.android.sdk.connectivity.Connectivity;
import co.elastic.apm.android.sdk.internal.configuration.Configurations;
import co.elastic.apm.android.sdk.internal.configuration.impl.ConnectivityConfiguration;
import co.elastic.apm.android.test.testutils.base.BaseRobolectricTest;
import co.elastic.apm.android.test.testutils.base.BaseRobolectricTestApplication;

public class ConnectivityConfigurationTest extends BaseRobolectricTest {

    @Test
    public void whenConnectivityIsNotProvided_provideCompileTimeValues() {
        assertEquals("http://localhost", getConfiguration().getEndpoint());
        assertNull(getConfiguration().getAuthConfiguration());
    }

    @Config(application = AppWithCustomConnectivity.class)
    @Test
    public void whenConnectivityIsProvided_returnProvidedValues() {
        assertEquals("https://sample", getConfiguration().getEndpoint());
        assertEquals("Bearer someSecretToken", getConfiguration().getAuthConfiguration().asAuthorizationHeaderValue());
    }

    private ConnectivityConfiguration getConfiguration() {
        return Configurations.get(ConnectivityConfiguration.class);
    }

    private static class AppWithCustomConnectivity extends BaseRobolectricTestApplication {
        @Override
        public void onCreate() {
            initializeAgentWithCustomConnectivity(Connectivity.withSecretToken("https://sample", "someSecretToken"));
        }
    }
}
