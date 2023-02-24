package co.elastic.apm.android.test.features.centralconfig;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import android.content.Context;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;

import co.elastic.apm.android.sdk.connectivity.Connectivity;
import co.elastic.apm.android.sdk.internal.configuration.Configuration;
import co.elastic.apm.android.sdk.internal.configuration.Configurations;
import co.elastic.apm.android.sdk.internal.configuration.impl.ConnectivityConfiguration;
import co.elastic.apm.android.sdk.internal.features.centralconfig.CentralConfigurationListener;
import co.elastic.apm.android.sdk.internal.features.centralconfig.CentralConfigurationManager;
import co.elastic.apm.android.sdk.internal.services.Service;
import co.elastic.apm.android.sdk.internal.services.ServiceManager;
import co.elastic.apm.android.sdk.internal.services.preferences.PreferencesService;
import co.elastic.apm.android.sdk.internal.time.SystemTimeProvider;
import co.elastic.apm.android.test.testutils.base.BaseRobolectricTest;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

public class CentralConfigurationManagerTest extends BaseRobolectricTest {
    private Context context;
    private File configFile;
    private MockWebServer webServer;
    private SystemTimeProvider systemTimeProvider;
    private CentralConfigurationManager manager;

    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Before
    public void setUp() throws IOException {
        webServer = new MockWebServer();
        context = mock(Context.class);
        systemTimeProvider = mock(SystemTimeProvider.class);
        manager = new CentralConfigurationManager(context, systemTimeProvider);
        File filesDir = temporaryFolder.newFolder("filesDir");
        configFile = new File(filesDir, "elastic_agent_configuration.json");
        doReturn(filesDir).when(context).getFilesDir();
    }

    @After
    public void tearDown() throws IOException {
        webServer.shutdown();
    }

    @Test
    public void whenPublishingCachedConfig_targetCentralConfigListeners() {
        NormalConfiguration normalConfiguration = mock(NormalConfiguration.class);
        CentralAwareConfiguration centralAwareConfiguration = mock(CentralAwareConfiguration.class);
        injectConfigurations(normalConfiguration, centralAwareConfiguration);
        setConfigFileContents("{\"someKey\":\"someValue\"}");
        Map<String, String> map = new HashMap<>();
        map.put("someKey", "someValue");

        manager.publishCachedConfig();

        verify(centralAwareConfiguration).onUpdate(map);
        verifyNoInteractions(normalConfiguration);
    }

    @Test
    public void whenPublishingCachedConfig_andTheresNoAvailableConfig_doNothing() {
        CentralAwareConfiguration centralAwareConfiguration = mock(CentralAwareConfiguration.class);
        injectConfigurations(centralAwareConfiguration);

        manager.publishCachedConfig();

        verifyNoInteractions(centralAwareConfiguration);
    }

    @Test
    public void whenPublishingCachedConfig_andConfigIsUnreadable_doNothing() {
        CentralAwareConfiguration centralAwareConfiguration = mock(CentralAwareConfiguration.class);
        injectConfigurations(centralAwareConfiguration);
        setConfigFileContents("not a json");

        manager.publishCachedConfig();

        verifyNoInteractions(centralAwareConfiguration);
    }

    @Test
    public void whenFetchingRemoteConfigSucceeds_notifyListeners() throws IOException {
        NormalConfiguration normalConfiguration = mock(NormalConfiguration.class);
        CentralAwareConfiguration centralAwareConfiguration = mock(CentralAwareConfiguration.class);
        injectConfigurations(normalConfiguration, centralAwareConfiguration);
        stubNetworkResponse(200, "{\"aKey\":\"aValue\"}");
        Map<String, String> map = new HashMap<>();
        map.put("aKey", "aValue");

        manager.sync();

        verify(centralAwareConfiguration).onUpdate(map);
        verifyNoInteractions(normalConfiguration);
    }

    @Test
    public void whenMaxAgeIsProvided_returnItAndStoreRefreshTime() throws IOException {
        stubNetworkResponse(200, "{\"aKey\":\"aValue\"}", "max-age=12345");

        Integer maxAge = manager.sync();

        assertEquals(12345, maxAge.intValue());
    }

    @Test
    public void whenMaxAgeIsNotProvided_returnNull() throws IOException {
        stubNetworkResponse(200, "{\"aKey\":\"aValue\"}");

        Integer maxAge = manager.sync();

        assertNull(maxAge);
    }

    @Test
    public void whenFetchingRemoteConfigDoesNotSucceed_doNotNotifyListeners() throws IOException {
        NormalConfiguration normalConfiguration = mock(NormalConfiguration.class);
        CentralAwareConfiguration centralAwareConfiguration = mock(CentralAwareConfiguration.class);
        injectConfigurations(normalConfiguration, centralAwareConfiguration);
        stubNetworkResponse(304, "{\"aKey\":\"aValue\"}");

        manager.sync();

        verifyNoInteractions(normalConfiguration, centralAwareConfiguration);
    }

    @Test
    public void whenSyncIsRequested_maxAgeIsAvailableAndExpired_executeFetching() throws IOException {
        CentralAwareConfiguration centralAwareConfiguration = mock(CentralAwareConfiguration.class);
        injectConfigurations(centralAwareConfiguration);
        stubNetworkResponse(200, "{}");
        long currentTimeMillis = 1_000_000;
        doReturn(currentTimeMillis).when(systemTimeProvider).getCurrentTimeMillis();
        setMaxAgeTimeout(currentTimeMillis - 1);

        manager.sync();

        verify(centralAwareConfiguration).onUpdate(anyMap());
    }

    @Test
    public void whenSyncIsRequested_maxAgeIsAvailableAndNotExpired_doNotExecuteFetching() throws IOException {
        CentralAwareConfiguration centralAwareConfiguration = mock(CentralAwareConfiguration.class);
        injectConfigurations(centralAwareConfiguration);
        stubNetworkResponse(200, "{}");
        long currentTimeMillis = 1_000_000;
        doReturn(currentTimeMillis).when(systemTimeProvider).getCurrentTimeMillis();
        setMaxAgeTimeout(currentTimeMillis + 1);

        manager.sync();

        verifyNoInteractions(centralAwareConfiguration);
    }

    private void setMaxAgeTimeout(long timeInMillis) {
        PreferencesService preferences = ServiceManager.get().getService(Service.Names.PREFERENCES);
        preferences.store("central_configuration_refresh_timeout", timeInMillis);
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

    @SuppressWarnings({"unchecked", "ConstantConditions"})
    private void injectConfigurations(Configuration... configurations) {
        try {
            Field mapField = Configurations.class.getDeclaredField("configurations");
            mapField.setAccessible(true);
            Field instanceField = Configurations.class.getDeclaredField("INSTANCE");
            instanceField.setAccessible(true);
            Configurations instance = (Configurations) instanceField.get(null);
            Map<Class<? extends Configuration>, Configuration> map = (Map<Class<? extends Configuration>, Configuration>) mapField.get(instance);
            for (Configuration configuration : configurations) {
                map.put(configuration.getClass(), configuration);
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static class NormalConfiguration implements Configuration {

    }

    private static class CentralAwareConfiguration implements Configuration, CentralConfigurationListener {

        @Override
        public void onUpdate(Map<String, String> map) {

        }
    }
}
