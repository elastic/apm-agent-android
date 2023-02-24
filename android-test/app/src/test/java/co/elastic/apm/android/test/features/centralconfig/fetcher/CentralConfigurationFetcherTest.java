package co.elastic.apm.android.test.features.centralconfig.fetcher;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;

import co.elastic.apm.android.sdk.internal.configuration.impl.ConnectivityConfiguration;
import co.elastic.apm.android.sdk.internal.features.centralconfig.fetcher.CentralConfigurationFetcher;
import co.elastic.apm.android.sdk.internal.features.centralconfig.fetcher.ConfigurationFileProvider;
import co.elastic.apm.android.sdk.internal.features.centralconfig.fetcher.FetchResult;
import co.elastic.apm.android.sdk.internal.services.preferences.PreferencesService;
import co.elastic.apm.android.test.testutils.base.BaseRobolectricTest;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

public class CentralConfigurationFetcherTest extends BaseRobolectricTest implements ConfigurationFileProvider {
    private PreferencesService preferences;
    private ConnectivityConfiguration connectivity;
    private File configurationFile;
    private CentralConfigurationFetcher fetcher;
    private MockWebServer webServer;

    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Before
    public void setUp() throws IOException {
        setUpConnectivity();
        preferences = Mockito.mock(PreferencesService.class);
        configurationFile = temporaryFolder.newFile("configFile.json");

        fetcher = new CentralConfigurationFetcher(connectivity, this, preferences);
    }

    @After
    public void tearDown() throws IOException {
        webServer.shutdown();
    }

    private void setUpConnectivity() {
        webServer = new MockWebServer();
        connectivity = Mockito.mock(ConnectivityConfiguration.class);
        doReturn("http://" + webServer.getHostName() + ":" + webServer.getPort()).when(connectivity).getEndpoint();
    }

    @Test
    public void whenConfigurationIsReceived_respondWithConfigurationHasChanged() throws IOException {
        enqueueResponse(200, "{}");

        FetchResult fetch = fetcher.fetch();

        assertTrue(fetch.configurationHasChanged);
    }

    private void enqueueResponse(int code, String body) {
        webServer.enqueue(new MockResponse().setResponseCode(code).setBody(body));
    }

    @Override
    public File getConfigurationFile() {
        return configurationFile;
    }
}
