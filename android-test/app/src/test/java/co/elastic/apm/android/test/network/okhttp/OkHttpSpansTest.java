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
        webServer.enqueue(new MockResponse().setResponseCode(200).setBody("{}"));
    }

    @After
    public void tearDown() throws IOException {
        webServer.shutdown();
    }

    @Test
    public void verifyHttpSpanStructure() {
        executeHttpCall();

        List<SpanData> spans = getRecordedSpans(2);
        SpanData httpSpan = spans.get(1);

        Spans.verify(httpSpan)
                .isNamed("GET localhost")
                .isOfKind(SpanKind.CLIENT)
                .hasAttribute("http.url", "http://localhost:" + webServer.getPort() + "/")
                .hasAttribute("http.method", "GET");
    }

    @Test
    public void whenThereIsAnExistingSpanContext_createHttpSpanOnly() {
        Span parentSpan = ElasticTracer.create("SomeScope").spanBuilder("SomeSpan").startSpan();
        try (Scope ignored = parentSpan.makeCurrent()) {
            executeHttpCall();
        } finally {
            parentSpan.end();
        }

        List<SpanData> spans = getRecordedSpans(2);

        SpanData parentSpanData = spans.get(0);
        SpanData httpSpan = spans.get(1);

        Spans.verify(parentSpanData)
                .hasNoParent();

        Spans.verify(httpSpan)
                .isDirectChildOf(parentSpanData);
    }

    @Test
    public void whenThereIsNoParentSpanContext_wrapHttpSpanWithTransactionSpan() {
        executeHttpCall();

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

    private void executeHttpCall() {
        try {
            Response response = client.newCall(request).execute();
            assertEquals("{}", response.body().string());
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
}
