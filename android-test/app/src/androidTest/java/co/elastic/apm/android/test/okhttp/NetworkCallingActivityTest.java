package co.elastic.apm.android.test.okhttp;

import static org.junit.Assert.assertNotNull;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import co.elastic.apm.android.test.base.BaseEspressoTest;
import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

@RunWith(AndroidJUnit4.class)
public class NetworkCallingActivityTest extends BaseEspressoTest<NetworkCallingActivity> {

    private MockWebServer mockWebServer;
    private HttpUrl mockUrl;

    @Test
    public void whenOtherOkhttpInterceptorsAreSet_propagateParentContext() throws InterruptedException {
        RecordedRequest recordedRequest = mockWebServer.takeRequest(1, TimeUnit.SECONDS);

        assertNotNull(recordedRequest.getHeader("MY-HEADER"));
        assertNotNull(recordedRequest.getHeader("traceparent"));
    }

    @Override
    protected void onActivity(NetworkCallingActivity activity) {
        activity.makeNetworkCall(mockUrl);
    }

    @Override
    protected void onBefore() {
        mockWebServer = new MockWebServer();
        mockWebServer.enqueue(new MockResponse().setResponseCode(200));
        try {
            mockWebServer.start();
            mockUrl = mockWebServer.url("/");
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