package co.elastic.apm.android.test.okhttp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import androidx.annotation.NonNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import co.elastic.apm.android.sdk.traces.ElasticTracers;
import co.elastic.apm.android.test.base.BaseEspressoTest;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Scope;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import okhttp3.mockwebserver.SocketPolicy;

public class NetworkCallingActivityTest extends BaseEspressoTest {
    private OkHttpClient.Builder clientBuilder;
    private MockWebServer webServer;

    @Before
    public void setUp() {
        webServer = new MockWebServer();
        try {
            webServer.start();
            clientBuilder = new OkHttpClient.Builder();
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
        clientBuilder.addInterceptor(chain -> {
            Request request = chain.request();
            return chain.proceed(request.newBuilder().addHeader("MY-HEADER", "My header value").build());
        });

        Span span = ElasticTracers.androidActivity().spanBuilder("Http parent span").startSpan();
        try (Scope ignored = span.makeCurrent()) {
            executeSuccessfulHttpCall(200, "{}", Collections.emptyMap());
        }
        span.end();

        RecordedRequest recordedRequest = webServer.takeRequest(1, TimeUnit.SECONDS);

        assertNotNull(recordedRequest.getHeader("MY-HEADER"));
        assertNotNull(recordedRequest.getHeader("traceparent"));
    }

    private void executeSuccessfulHttpCall(int responseCode, String responseBody, Map<String, String> responseHeaders) {
        MockResponse mockResponse = new MockResponse().setResponseCode(responseCode).setBody(responseBody);
        responseHeaders.forEach(mockResponse::addHeader);
        webServer.enqueue(mockResponse);
        AtomicReference<String> responseStr = new AtomicReference<>("");
        try {
            executeHttpCall(new Request.Builder().url(webServer.url("/")).build(), response -> {
                try {
                    responseStr.set(response.body().string());
                } catch (IOException e) {
                    fail(e.getMessage());
                }
            });
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        assertEquals("{}", responseStr.get());
    }

    private void executeFailedHttpCall() {
        webServer.enqueue(new MockResponse()
                .setBody("{}")
                .setSocketPolicy(SocketPolicy.DISCONNECT_DURING_RESPONSE_BODY));

        Consumer<Response> responseConsumer = response -> {
            try {
                response.body().string();
                fail();
            } catch (IOException e) {
                assertEquals("unexpected end of stream", e.getMessage());
            }
        };

        try {
            executeHttpCall(new Request.Builder().url(webServer.url("/")).build(), responseConsumer);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void executeHttpCall(Request request, Consumer<Response> responseConsumer) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        clientBuilder.build().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                latch.countDown();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                responseConsumer.accept(response);
                latch.countDown();
            }
        });
        latch.await(1, TimeUnit.SECONDS);
    }
}