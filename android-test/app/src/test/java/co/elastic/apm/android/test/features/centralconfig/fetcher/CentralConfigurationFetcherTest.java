package co.elastic.apm.android.test.features.centralconfig.fetcher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import androidx.annotation.NonNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import co.elastic.apm.android.sdk.connectivity.auth.AuthConfiguration;
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
        preferences = mock(PreferencesService.class);
        configurationFile = temporaryFolder.newFile("configFile.json");

        fetcher = new CentralConfigurationFetcher(connectivity, this, preferences);
    }

    @After
    public void tearDown() throws IOException {
        webServer.shutdown();
    }

    private void setUpConnectivity() {
        webServer = new MockWebServer();
        connectivity = mock(ConnectivityConfiguration.class);
        setConnectivityEndpoint("");
    }

    private void setConnectivityEndpoint(String path) {
        doReturn("http://" + webServer.getHostName() + ":" + webServer.getPort() + path).when(connectivity).getEndpoint();
    }

    @Test
    public void whenResponseIsOk_respondWithConfigurationHasChanged() throws IOException {
        enqueueSimpleResponse();

        FetchResult fetch = fetcher.fetch();

        assertTrue(fetch.configurationHasChanged);
    }

    @Test
    public void whenResponseIsNotOk_respondWithConfigurationHasNotChanged() throws IOException {
        enqueueResponse(getResponse(304, ""));

        FetchResult fetch = fetcher.fetch();

        assertFalse(fetch.configurationHasChanged);
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
    public void whenPreparingRequest_keep_original_url_path() throws IOException, InterruptedException {
        enqueueSimpleResponse();

        setConnectivityEndpoint("/some/path");
        fetcher = new CentralConfigurationFetcher(connectivity, this, preferences);
        fetcher.fetch();

        HttpUrl recordedRequestUrl = webServer.takeRequest().getRequestUrl();
        assertEquals("/some/path/config%2Fv1%2Fagents", recordedRequestUrl.encodedPath());
    }

    @Test
    public void whenPreparingRequest_sendContentType() throws IOException, InterruptedException {
        enqueueSimpleResponse();

        fetcher.fetch();

        assertEquals("application/json", webServer.takeRequest().getHeader("Content-Type"));
    }

    @Test
    public void whenPreparingRequest_andThereIsAuthenticationAvailable_sendAuthHeader() throws
            IOException, InterruptedException {
        String authHeaderValue = "Bearer something";
        AuthConfiguration authConfiguration = mock(AuthConfiguration.class);
        doReturn(authHeaderValue).when(authConfiguration).asAuthorizationHeaderValue();
        doReturn(authConfiguration).when(connectivity).getAuthConfiguration();
        enqueueSimpleResponse();

        fetcher.fetch();

        assertEquals(authHeaderValue, webServer.takeRequest().getHeader("Authorization"));
    }

    @Test
    public void whenEtagReceived_storeIt() throws IOException {
        String theEtag = "someEtag";
        enqueueResponse(getResponse(200, "{}").setHeader("ETag", theEtag));

        fetcher.fetch();

        verify(preferences).store("central_configuration_etag", theEtag);
    }

    @Test
    public void whenPreparingRequest_andEtagIsAvailable_sendIt() throws
            IOException, InterruptedException {
        String theEtag = "someEtag";
        doReturn(theEtag).when(preferences).retrieveString("central_configuration_etag");
        enqueueSimpleResponse();

        fetcher.fetch();

        String sentEtag = webServer.takeRequest().getHeader("If-None-Match");
        assertEquals(theEtag, sentEtag);
    }

    @Test
    public void whenCacheControlReceived_returnMaxAgeTime() throws IOException {
        int headerMaxAge = 12345;
        String headerValue = "max-age=" + headerMaxAge;
        enqueueResponse(getResponse(200, "{}").setHeader("Cache-Control", headerValue));

        FetchResult result = fetcher.fetch();

        assertEquals(headerMaxAge, result.maxAgeInSeconds.intValue());
    }

    @Test
    public void whenCacheControlReceived_WithoutMaxAge_returnMaxAgeTimeAsNull() throws
            IOException {
        String headerValue = "no-cache";
        enqueueResponse(getResponse(200, "{}").setHeader("Cache-Control", headerValue));

        FetchResult result = fetcher.fetch();

        assertNull(result.maxAgeInSeconds);
    }

    @Test
    public void whenCacheControlNotReceived_returnMaxAgeTimeAsNull() throws IOException {
        enqueueSimpleResponse();

        FetchResult result = fetcher.fetch();

        assertNull(result.maxAgeInSeconds);
    }

    @Test
    public void whenConfigurationReceived_storeInProvidedFile() throws IOException {
        String body = "{\"some\":\"configValue\"}";
        enqueueResponse(getResponse(200, body));

        fetcher.fetch();

        assertEquals(body, new String(Files.readAllBytes(configurationFile.toPath())));
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
