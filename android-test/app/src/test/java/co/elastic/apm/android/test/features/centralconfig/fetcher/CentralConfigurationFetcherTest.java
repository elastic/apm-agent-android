package co.elastic.apm.android.test.features.centralconfig.fetcher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import androidx.annotation.NonNull;

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
import co.elastic.apm.android.test.BuildConfig;
import co.elastic.apm.android.test.testutils.base.BaseRobolectricTest;
import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

@SuppressWarnings("ConstantConditions")
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
        enqueueSimpleResponse();

        FetchResult fetch = fetcher.fetch();

        assertTrue(fetch.configurationHasChanged);
    }

    @Test
    public void whenPreparingRequest_sendQueryParams() throws IOException, InterruptedException {
        enqueueSimpleResponse();

        fetcher.fetch();

        HttpUrl recordedRequestUrl = webServer.takeRequest().getRequestUrl();
        assertEquals("my-app", recordedRequestUrl.queryParameter("service.name"));
        assertEquals(BuildConfig.BUILD_TYPE, recordedRequestUrl.queryParameter("service.environment"));
    }

    @Test
    public void whenEtagReceived_storeIt() throws IOException {
        String theEtag = "someEtag";
        enqueueResponse(getResponse(200, "{}").setHeader("ETag", theEtag));

        fetcher.fetch();

        verify(preferences).store("central_configuration_etag", theEtag);
    }

    @Test
    public void whenPreparingRequest_andEtagIsAvailable_sendIt() throws IOException, InterruptedException {
        String theEtag = "someEtag";
        doReturn(theEtag).when(preferences).retrieveString("central_configuration_etag");
        enqueueSimpleResponse();

        fetcher.fetch();

        String sentEtag = webServer.takeRequest().getHeader("ETag");
        assertEquals(theEtag, sentEtag);
    }

    private void enqueueSimpleResponse() {
        enqueueResponse(getResponse(200, "{}"));
    }

    private void enqueueResponse(MockResponse response) {
        webServer.enqueue(response);
    }

    @NonNull
    private MockResponse getResponse(int code, String body) {
        return new MockResponse().setResponseCode(code).setBody(body);
    }

    @Override
    public File getConfigurationFile() {
        return configurationFile;
    }
}
