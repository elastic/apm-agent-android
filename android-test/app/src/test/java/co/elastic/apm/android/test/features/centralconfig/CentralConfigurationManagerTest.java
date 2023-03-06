package co.elastic.apm.android.test.features.centralconfig;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import android.content.Context;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

import co.elastic.apm.android.sdk.connectivity.Connectivity;
import co.elastic.apm.android.sdk.internal.configuration.Configuration;
import co.elastic.apm.android.sdk.internal.configuration.ConfigurationOption;
import co.elastic.apm.android.sdk.internal.configuration.Configurations;
import co.elastic.apm.android.sdk.internal.configuration.OptionsRegistry;
import co.elastic.apm.android.sdk.internal.configuration.impl.ConnectivityConfiguration;
import co.elastic.apm.android.sdk.internal.features.centralconfig.CentralConfigurationManager;
import co.elastic.apm.android.sdk.internal.services.preferences.PreferencesService;
import co.elastic.apm.android.sdk.internal.time.SystemTimeProvider;
import co.elastic.apm.android.test.testutils.base.BaseRobolectricTest;
import co.elastic.apm.android.test.testutils.base.BaseRobolectricTestApplication;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

@Config(application = CentralConfigurationManagerTest.CentralConfigurationTestApp.class)
public class CentralConfigurationManagerTest extends BaseRobolectricTest {
    private File configFile;
    private MockWebServer webServer;
    private CentralConfigurationManager manager;
    private SystemTimeProvider systemTimeProvider;
    private PreferencesService preferences;
    private Configurations configurationsSpy;
    private static final String PREFERENCE_REFRESH_TIMEOUT_NAME = "central_configuration_refresh_timeout";

    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Before
    public void setUp() throws IOException {
        CentralConfigurationTestApp app = (CentralConfigurationTestApp) RuntimeEnvironment.getApplication();
        manager = app.manager;
        systemTimeProvider = app.systemTimeProvider;
        preferences = app.preferences;
        webServer = new MockWebServer();
        File filesDir = temporaryFolder.newFolder("filesDir");
        configFile = new File(filesDir, "elastic_agent_configuration.json");
        doReturn(filesDir).when(app.context).getFilesDir();
        setUpConfigurationRegistrySpy();
    }

    @SuppressWarnings("ConstantConditions")
    private void setUpConfigurationRegistrySpy() {
        try {
            Field instanceField = Configurations.class.getDeclaredField("INSTANCE");
            instanceField.setAccessible(true);
            Object instance = instanceField.get(null);
            configurationsSpy = (Configurations) spy(instance);
            instanceField.set(null, configurationsSpy);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @After
    public void tearDown() throws IOException {
        webServer.shutdown();
    }

    @Test
    public void whenPublishingCachedConfig_notifyConfigs() {
        setConfigFileContents("{\"someKey\":\"true\"}");

        manager.publishCachedConfig();

        assertTrue(getDummyConfiguration().getOptionValue());
        verify(configurationsSpy).doReload();
    }

    @Test
    public void whenPublishingCachedConfig_andTheresNoAvailableConfig_doNothing() {
        manager.publishCachedConfig();

        assertFalse(getDummyConfiguration().getOptionValue());

        verify(configurationsSpy, never()).doReload();
    }

    @Test
    public void whenPublishingCachedConfig_andConfigIsUnreadable_doNothing() {
        setConfigFileContents("not a json");

        manager.publishCachedConfig();

        verify(configurationsSpy, never()).doReload();
    }

    @Test
    public void whenFetchingRemoteConfigSucceeds_notifyConfigs() throws IOException {
        stubNetworkResponse(200, "{\"someKey\":\"true\"}");

        manager.sync();

        assertTrue(getDummyConfiguration().getOptionValue());
    }

    @Test
    public void whenMaxAgeIsProvided_returnItAndStoreRefreshTime() throws IOException {
        long currentTimeMillis = 1_000_000;
        doReturn(currentTimeMillis).when(systemTimeProvider).getCurrentTimeMillis();
        stubNetworkResponse(200, "{\"aKey\":\"aValue\"}", "max-age=500");

        Integer maxAge = manager.sync();

        assertEquals(500, maxAge.intValue());
        verifyRefreshTimeoutTimeSet(1_500_000L);
    }

    @Test
    public void whenMaxAgeIsNotProvided_returnNull_and_doNotChange_preferences() throws IOException {
        stubNetworkResponse(200, "{\"aKey\":\"aValue\"}");

        Integer maxAge = manager.sync();

        assertNull(maxAge);
        verify(preferences, never()).store(eq(PREFERENCE_REFRESH_TIMEOUT_NAME), anyLong());
    }

    @Test
    public void whenFetchingRemoteConfigDoesNotSucceed_doNotNotifyListeners() throws IOException {
        stubNetworkResponse(304, "{\"aKey\":\"aValue\"}");

        manager.sync();

        verify(configurationsSpy, never()).doReload();
    }

    @Test
    public void whenSyncIsRequested_maxAgeIsAvailableAndExpired_executeFetching() throws IOException {
        stubNetworkResponse(200, "{}");
        long currentTimeMillis = 1_000_000;
        doReturn(currentTimeMillis).when(systemTimeProvider).getCurrentTimeMillis();
        setRefreshTimeoutTime(currentTimeMillis - 1);

        manager.sync();

        verify(configurationsSpy).doReload();
    }

    @Test
    public void whenSyncIsRequested_maxAgeIsAvailableAndNotExpired_doNotExecuteFetching() throws IOException {
        stubNetworkResponse(200, "{}");
        long currentTimeMillis = 1_000_000;
        doReturn(currentTimeMillis).when(systemTimeProvider).getCurrentTimeMillis();
        setRefreshTimeoutTime(currentTimeMillis + 1);

        manager.sync();

        verify(configurationsSpy).doReload();
    }

    private DummyConfiguration getDummyConfiguration() {
        return Configurations.get(DummyConfiguration.class);
    }

    private void verifyRefreshTimeoutTimeSet(long time) {
        verify(preferences).store(PREFERENCE_REFRESH_TIMEOUT_NAME, time);
    }

    private void setRefreshTimeoutTime(long timeInMillis) {
        preferences.store(PREFERENCE_REFRESH_TIMEOUT_NAME, timeInMillis);
    }

    private void stubNetworkResponse(int code, String body) {
        stubNetworkResponse(code, body, null);
    }

    private void stubNetworkResponse(int code, String body, String cacheControlHeader) {
        try {
            Connectivity connectivity = mock(Connectivity.class);
            Field field = ConnectivityConfiguration.class.getDeclaredField("connectivity");
            field.setAccessible(true);
            field.set(Configurations.get(ConnectivityConfiguration.class), connectivity);
            doReturn("http://" + webServer.getHostName() + ":" + webServer.getPort()).when(connectivity).endpoint();
            MockResponse response = new MockResponse().setResponseCode(code).setBody(body);
            if (cacheControlHeader != null) {
                response = response.setHeader("Cache-Control", cacheControlHeader);
            }
            webServer.enqueue(response);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private void setConfigFileContents(String contents) {
        try {
            if (!configFile.exists()) {
                configFile.createNewFile();
            }
            Files.write(configFile.toPath(), contents.getBytes(StandardCharsets.UTF_8), StandardOpenOption.WRITE);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static class DummyConfiguration extends Configuration {
        final ConfigurationOption<Boolean> option;

        private DummyConfiguration(String optionKey, boolean optionValue) {
            option = createBooleanOption(optionKey, optionValue);
        }

        public boolean getOptionValue() {
            return option.get();
        }

        @Override
        protected void visitOptions(OptionsRegistry options) {
            super.visitOptions(options);
            options.register(option);
        }
    }

    protected static class CentralConfigurationTestApp extends BaseRobolectricTestApplication {
        private Context context;
        private SystemTimeProvider systemTimeProvider;
        private CentralConfigurationManager manager;
        private PreferencesService preferences;

        @Override
        public void onCreate() {
            super.onCreate();
            context = mock(Context.class);
            doReturn(context).when(context).getApplicationContext();
            preferences = mock(PreferencesService.class);
            systemTimeProvider = mock(SystemTimeProvider.class);
            manager = new CentralConfigurationManager(context, systemTimeProvider, preferences);
            doReturn(manager).when(getCentralConfigurationInitializer()).getManager();

            initializeAgentWithExtraConfigurations(new DummyConfiguration("someKey", false),
                    mock(Configuration.class));
        }
    }
}
