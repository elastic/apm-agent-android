package co.elastic.apm.android.test.okhttp;

import static org.junit.Assert.assertNotNull;

import androidx.annotation.NonNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import co.elastic.apm.android.sdk.traces.ElasticTracers;
import co.elastic.apm.android.test.base.BaseEspressoTest;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Scope;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

public class NetworkCallingActivityTest extends BaseEspressoTest {

    private MockWebServer webServer;

    @Before
    public void setUp() {
        webServer = new MockWebServer();
        try {
            webServer.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @After
    public void tearDown() {
        try {
            webServer.shutdown();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void whenOtherOkhttpInterceptorsAreSet_propagateParentContext() throws InterruptedException {
        webServer.enqueue(new MockResponse().setResponseCode(200).setBody("{}"));
        makeNetworkCall(webServer.url("/"));

        RecordedRequest recordedRequest = webServer.takeRequest(1, TimeUnit.SECONDS);

        assertNotNull(recordedRequest.getHeader("MY-HEADER"));
        assertNotNull(recordedRequest.getHeader("traceparent"));
    }

    private void makeNetworkCall(HttpUrl mockServerUrl) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    Request request = chain.request();

                    return chain.proceed(request.newBuilder().addHeader("MY-HEADER", "My header value").build());
                }).build();

        Request request = new Request.Builder()
                .url(mockServerUrl)
                .build();

        Span span = ElasticTracers.androidActivity().spanBuilder("Http parent span").startSpan();
        try (Scope ignored = span.makeCurrent()) {
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    latch.countDown();
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    latch.countDown();
                }
            });
        }
        span.end();

        latch.await(1, TimeUnit.SECONDS);
    }
}