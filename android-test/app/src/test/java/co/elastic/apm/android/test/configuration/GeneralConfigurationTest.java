package co.elastic.apm.android.test.configuration;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.robolectric.annotation.Config;

import co.elastic.apm.android.sdk.ElasticApmConfiguration;
import co.elastic.apm.android.sdk.internal.configuration.Configurations;
import co.elastic.apm.android.sdk.internal.configuration.impl.GeneralConfiguration;
import co.elastic.apm.android.test.BuildConfig;
import co.elastic.apm.android.test.testutils.base.BaseRobolectricTest;
import co.elastic.apm.android.test.testutils.base.BaseRobolectricTestApplication;

public class GeneralConfigurationTest extends BaseRobolectricTest {

    @Test
    public void whenNoServiceNameIsProvided_useServiceNameFromRuntimeApis() {
        assertEquals("android-test", getConfiguration().getServiceName());
    }

    @Config(application = AppWithCustomServiceName.class)
    @Test
    public void whenServiceNameIsProvided_returnProvidedValue() {
        assertEquals("custom-name", getConfiguration().getServiceName());
    }

    @Test
    public void whenNoServiceVersionIsProvided_useServiceVersionFromRuntimeApis() {
        assertEquals("1.0", getConfiguration().getServiceVersion());
    }

    @Config(application = AppWithCustomServiceVersion.class)
    @Test
    public void whenServiceVersionIsProvided_returnProvidedValue() {
        assertEquals("2.0.0", getConfiguration().getServiceVersion());
    }

    @Test
    public void whenServiceEnvironmentIsRequested_provideAppBuildType() {
        assertEquals(BuildConfig.BUILD_TYPE, getConfiguration().getServiceEnvironment());
    }

    private GeneralConfiguration getConfiguration() {
        return Configurations.get(GeneralConfiguration.class);
    }

    private static class AppWithCustomServiceName extends BaseRobolectricTestApplication {
        @Override
        public void onCreate() {
            initializeAgentWithCustomConfig(ElasticApmConfiguration.builder()
                    .setServiceName("custom-name").build());
        }
    }

    private static class AppWithCustomServiceVersion extends BaseRobolectricTestApplication {
        @Override
        public void onCreate() {
            initializeAgentWithCustomConfig(ElasticApmConfiguration.builder()
                    .setServiceVersion("2.0.0").build());
        }
    }
}
