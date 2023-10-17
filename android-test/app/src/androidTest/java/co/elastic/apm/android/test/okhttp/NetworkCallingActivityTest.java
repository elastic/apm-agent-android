package co.elastic.apm.android.test.okhttp;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import co.elastic.apm.android.test.base.ActivityEspressoTest;
import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

public class NetworkCallingActivityTest extends ActivityEspressoTest<NetworkCallingActivity> {

    private MockWebServer mockWebServer;
    private HttpUrl mockUrl;

    @Test
    public void whenOtherOkhttpInterceptorsAreSet_propagateParentContext() throws InterruptedException {
        activity.makeNetworkCall(mockUrl);

        RecordedRequest recordedRequest = mockWebServer.takeRequest(1, TimeUnit.SECONDS);

        assertNotNull(recordedRequest.getHeader("MY-HEADER"));
        assertNotNull(recordedRequest.getHeader("traceparent"));
    }

    @Override
    protected void onBefore() {
        mockWebServer = new MockWebServer();
        try {
            mockWebServer.start();
            mockUrl = mockWebServer.url("/");
            mockWebServer.enqueue(new MockResponse().setBody("{}").setResponseCode(200));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void onAfter() {
        try {
            mockWebServer.shutdown();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected Class<NetworkCallingActivity> getActivityClass() {
        return NetworkCallingActivity.class;
    }
}