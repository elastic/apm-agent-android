package co.elastic.apm.android.test.network.okhttp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.robolectric.annotation.Config;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import co.elastic.apm.android.sdk.ElasticApmConfiguration;
import co.elastic.apm.android.sdk.instrumentation.InstrumentationConfiguration;
import co.elastic.apm.android.sdk.traces.ElasticTracers;
import co.elastic.apm.android.sdk.traces.http.impl.okhttp.OkHttpContextStore;
import co.elastic.apm.android.sdk.traces.http.impl.okhttp.OtelOkHttpEventListener;
import co.elastic.apm.android.sdk.traces.http.impl.okhttp.OtelOkHttpInterceptor;
import co.elastic.apm.android.test.common.spans.Spans;
import co.elastic.apm.android.test.testutils.AppWithoutInitializedAgent;
import co.elastic.apm.android.test.testutils.base.BaseRobolectricTest;
import co.elastic.apm.android.test.testutils.base.BaseRobolectricTestApplication;
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
    private OkHttpContextStore contextStore;

    @Before
    public void setUp() {
        webServer = new MockWebServer();
        contextStore = spy(new OkHttpContextStore());
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
        Map<String, String> responseHeaders = new HashMap<>();
        responseHeaders.put("Content-Length", "2");
        executeSuccessfulHttpCall(request, 200, "{}", responseHeaders);

        List<SpanData> spans = getRecordedSpans(2);
        SpanData httpSpan = spans.get(1);

        Spans.verify(httpSpan)
                .isNamed("GET localhost")
                .isOfKind(SpanKind.CLIENT)
                .hasAttribute("http.url", "http://localhost:" + webServer.getPort() + "/")
                .hasAttribute("http.method", "GET")
                .hasAttribute("http.status_code", 200)
                .hasAttribute("http.response_content_length", 2);
        verify(contextStore).remove(any());
    }

    @Test
    public void verifyHttpSpanStructure_whenReceivingHttpError() {
        executeSuccessfulHttpCall(request, 500);

        List<SpanData> spans = getRecordedSpans(2);
        SpanData httpSpan = spans.get(1);

        Spans.verifyFailed(httpSpan)
                .isNamed("GET localhost")
                .isOfKind(SpanKind.CLIENT)
                .hasAttribute("http.url", "http://localhost:" + webServer.getPort() + "/")
                .hasAttribute("http.method", "GET")
                .hasAttribute("http.status_code", 500);
    }

    @Test
    public void verifyHttpSpanStructure_whenFailed() {
        executeFailedHttpCall(request);

        List<SpanData> spans = getRecordedSpans(2);
        SpanData httpSpan = spans.get(1);

        Spans.verifyFailed(httpSpan)
                .isNamed("GET localhost")
                .isOfKind(SpanKind.CLIENT)
                .hasAmountOfRecordedExceptions(1)
                .hasAttribute("http.url", "http://localhost:" + webServer.getPort() + "/")
                .hasAttribute("http.method", "GET");

        verify(contextStore).remove(any());
    }

    @Test
    public void excludeHttpCallsFromOTelTracesExporter() {
        Request otelExporterRequest = new Request.Builder()
                .url(webServer.url("/opentelemetry.proto.collector.trace.v1.TraceService/Export"))
                .build();

        executeSuccessfulHttpCall(otelExporterRequest);

        getRecordedSpans(0);
        verify(contextStore, never()).put(any(), any());
    }

    @Test
    public void excludeHttpCallsFromOTelMetricsExporter() {
        Request otelExporterRequest = new Request.Builder()
                .url(webServer.url("/opentelemetry.proto.collector.metrics.v1.MetricsService/Export"))
                .build();

        executeSuccessfulHttpCall(otelExporterRequest);

        getRecordedSpans(0);
        verify(contextStore, never()).put(any(), any());
    }

    @Test
    public void excludeHttpCallsFromOTelLogsExporter() {
        Request otelExporterRequest = new Request.Builder()
                .url(webServer.url("/opentelemetry.proto.collector.logs.v1.LogsService/Export"))
                .build();

        executeSuccessfulHttpCall(otelExporterRequest);

        getRecordedSpans(0);
        verify(contextStore, never()).put(any(), any());
    }

    @Test
    public void whenThereIsAnExistingSpanContext_createHttpSpanOnly() {
        String existingSpanName = "SomeSpan";
        Span parentSpan = ElasticTracers.create("SomeScope").spanBuilder(existingSpanName).startSpan();
        try (Scope ignored = parentSpan.makeCurrent()) {
            executeSuccessfulHttpCall(request);
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
        executeSuccessfulHttpCall(request);

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

    @Test
    public void whenThereIsNoParentSpanContext_wrapHttpSpanWithTransactionSpan_forFailedCall() {
        executeFailedHttpCall(request);

        List<SpanData> spans = getRecordedSpans(2);

        SpanData transactionSpan = spans.get(0);
        SpanData httpSpan = spans.get(1);

        Spans.verify(transactionSpan)
                .isNamed("Transaction - GET localhost")
                .isOfKind(SpanKind.INTERNAL)
                .hasNoParent();

        Spans.verifyFailed(httpSpan)
                .isOfKind(SpanKind.CLIENT)
                .isDirectChildOf(transactionSpan);
    }

    @Config(application = DisabledHttpRequestsApp.class)
    @Test
    public void whenHttpInstrumentationIsDisabled_doNotSendAnyOkHttpSpans() {
        executeSuccessfulHttpCall(request);

        getRecordedSpans(0);
    }

    private void executeSuccessfulHttpCall(Request request) {
        executeSuccessfulHttpCall(request, 200);
    }

    @Config(application = AppWithoutInitializedAgent.class)
    @Test
    public void whenAgentIsNotInitialized_doNotSendAnyOkHttpSpans() {
        executeSuccessfulHttpCall(request);

        getRecordedSpans(0);
    }

    private void executeSuccessfulHttpCall(Request request, int responseCode) {
        executeSuccessfulHttpCall(request, responseCode, "{}", Collections.emptyMap());
    }

    private void executeSuccessfulHttpCall(Request request, int responseCode, String body, Map<String, String> headers) {
        MockResponse mockResponse = new MockResponse().setResponseCode(responseCode).setBody(body);
        headers.forEach(mockResponse::addHeader);
        webServer.enqueue(mockResponse);
        try {
            Response response = executeHttpCall(request);
            assertEquals("{}", response.body().string());
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    private void executeFailedHttpCall(Request request) {
        webServer.enqueue(new MockResponse()
                .setBody("{}")
                .setSocketPolicy(SocketPolicy.DISCONNECT_DURING_RESPONSE_BODY));
        Response response = executeHttpCall(request);
        try {
            response.body().string();
            fail();
        } catch (IOException e) {
            assertEquals("unexpected end of stream", e.getMessage());
        }
    }

    private Response executeHttpCall(Request request) {
        try {
            return client.newCall(request).execute();
        } catch (IOException e) {
            fail(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private static class DisabledHttpRequestsApp extends BaseRobolectricTestApplication {
        @Override
        public void onCreate() {
            super.onCreate();
            ElasticApmConfiguration configuration = ElasticApmConfiguration.builder().setInstrumentationConfiguration(InstrumentationConfiguration.builder()
                    .enableHttpTracing(false)
                    .build()).build();

            initializeAgentWithCustomConfig(configuration);
        }
    }
}
