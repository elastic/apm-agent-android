package co.elastic.apm.android.test.network.okhttp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import co.elastic.apm.android.sdk.traces.common.tools.ElasticTracer;
import co.elastic.apm.android.sdk.traces.http.impl.okhttp.OkHttpContextStore;
import co.elastic.apm.android.sdk.traces.http.impl.okhttp.OtelOkHttpEventListener;
import co.elastic.apm.android.sdk.traces.http.impl.okhttp.OtelOkHttpInterceptor;
import co.elastic.apm.android.test.common.spans.Spans;
import co.elastic.apm.android.test.testutils.base.BaseRobolectricTest;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.context.Scope;
import io.opentelemetry.sdk.trace.data.SpanData;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.SocketPolicy;

public class OkHttpSpansTest extends BaseRobolectricTest {
    private MockWebServer webServer;
    private OkHttpClient client;
    private Request request;

    @Before
    public void setUp() {
        webServer = new MockWebServer();
        OkHttpContextStore contextStore = new OkHttpContextStore();
        client = new OkHttpClient.Builder()
                .eventListenerFactory(new OtelOkHttpEventListener.Factory(contextStore))
                .addInterceptor(new OtelOkHttpInterceptor(contextStore))
                .build();

        request = new Request.Builder()
                .url(webServer.url("/"))
                .build();
    }

    @After
    public void tearDown() throws IOException {
        webServer.shutdown();
    }

    @Test
    public void verifyHttpSpanStructure_whenSucceeded() {
        executeSuccessfulHttpCall();

        List<SpanData> spans = getRecordedSpans(2);
        SpanData httpSpan = spans.get(1);

        Spans.verify(httpSpan)
                .isNamed("GET localhost")
                .isOfKind(SpanKind.CLIENT)
                .hasAttribute("http.url", "http://localhost:" + webServer.getPort() + "/")
                .hasAttribute("http.method", "GET");
    }

    @Test
    public void verifyHttpSpanStructure_whenFailed() {
        executeFailedHttpCall();

        List<SpanData> spans = getRecordedSpans(2);
        SpanData httpSpan = spans.get(1);

        Spans.verifyFailed(httpSpan)
                .isNamed("GET localhost")
                .isOfKind(SpanKind.CLIENT)
                .hasAmountOfRecordedExceptions(1)
                .hasAttribute("http.url", "http://localhost:" + webServer.getPort() + "/")
                .hasAttribute("http.method", "GET");
    }

    @Test
    public void whenThereIsAnExistingSpanContext_createHttpSpanOnly() {
        String existingSpanName = "SomeSpan";
        Span parentSpan = ElasticTracer.create("SomeScope").spanBuilder(existingSpanName).startSpan();
        try (Scope ignored = parentSpan.makeCurrent()) {
            executeSuccessfulHttpCall();
        } finally {
            parentSpan.end();
        }

        List<SpanData> spans = getRecordedSpans(2);

        SpanData parentSpanData = spans.get(0);
        SpanData httpSpan = spans.get(1);

        Spans.verify(parentSpanData)
                .isNamed(existingSpanName)
                .hasNoParent();

        Spans.verify(httpSpan)
                .isDirectChildOf(parentSpanData);
    }

    @Test
    public void whenThereIsNoParentSpanContext_wrapHttpSpanWithTransactionSpan_forSuccessfulCall() {
        executeSuccessfulHttpCall();

        verifyWrappedSpanCase();
    }

    @Test
    public void whenThereIsNoParentSpanContext_wrapHttpSpanWithTransactionSpan_forFailedCall() {
        executeFailedHttpCall();

        verifyWrappedSpanCase();
    }

    private void verifyWrappedSpanCase() {
        List<SpanData> spans = getRecordedSpans(2);

        SpanData transactionSpan = spans.get(0);
        SpanData httpSpan = spans.get(1);

        Spans.verify(transactionSpan)
                .isNamed("Transaction - GET localhost")
                .isOfKind(SpanKind.INTERNAL)
                .hasNoParent();

        Spans.verify(httpSpan)
                .isOfKind(SpanKind.CLIENT)
                .isDirectChildOf(transactionSpan);
    }

    private void executeSuccessfulHttpCall() {
        webServer.enqueue(new MockResponse().setResponseCode(200).setBody("{}"));
        try {
            Response response = executeHttpCall();
            assertEquals("{}", response.body().string());
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    private void executeFailedHttpCall() {
        webServer.enqueue(new MockResponse()
                .setBody("{}")
                .setSocketPolicy(SocketPolicy.DISCONNECT_DURING_RESPONSE_BODY));
        Response response = executeHttpCall();
        try {
            response.body().string();
            fail();
        } catch (IOException e) {
            assertEquals("unexpected end of stream", e.getMessage());
        }
    }

    private Response executeHttpCall() {
        try {
            return client.newCall(request).execute();
        } catch (IOException e) {
            fail(e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
