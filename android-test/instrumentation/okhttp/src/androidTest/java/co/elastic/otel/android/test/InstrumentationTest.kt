package co.elastic.otel.android.test

import co.elastic.otel.android.test.rule.AndroidTestAgentRule
import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.trace.SpanKind
import io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat
import io.opentelemetry.sdk.trace.data.StatusData
import java.io.IOException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import java.util.function.Consumer
import junit.framework.TestCase.fail
import okhttp3.Call
import okhttp3.Callback
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test


class InstrumentationTest {

    @get:Rule
    val agentRule = AndroidTestAgentRule()

    private lateinit var webServer: MockWebServer
    private lateinit var clientBuilder: OkHttpClient.Builder

    @Before
    fun setUp() {
        webServer = MockWebServer()
        webServer.start()
        clientBuilder = OkHttpClient.Builder()
    }

    @After
    fun tearDown() {
        webServer.shutdown()
    }

    @Test
    fun verifyOkHttpSyncCallSpan() {
        val url = webServer.url("/")
        executeSyncHttpCall("GET", 200, "{}", url)

        agentRule.flushSpans().join(5, TimeUnit.SECONDS)

        assertThat(agentRule.getFinishedSpans()).hasSize(1)
        assertThat(agentRule.getFinishedSpans().first()).hasName("GET")
            .hasKind(SpanKind.CLIENT)
            .hasStatus(StatusData.ok())
            .hasAttribute(AttributeKey.stringKey("url.full"), url.toString())
            .hasAttribute(AttributeKey.stringKey("http.request.method"), "GET")
            .hasAttribute(AttributeKey.longKey("http.response.status_code"), 200)
            .hasAttribute(AttributeKey.stringKey("server.address"), "localhost")
            .hasAttribute(AttributeKey.longKey("server.port"), webServer.port.toLong())
    }

    @Test
    fun verifyOkHttpAsyncCallSpan() {
        val url = webServer.url("/")
        executeAsyncSuccessfulHttpCall("GET", 200, "{}", url)

        agentRule.flushSpans().join(5, TimeUnit.SECONDS)

        assertThat(agentRule.getFinishedSpans()).hasSize(1)
        assertThat(agentRule.getFinishedSpans().first()).hasName("GET")
            .hasKind(SpanKind.CLIENT)
            .hasStatus(StatusData.ok())
            .hasAttribute(AttributeKey.stringKey("url.full"), url.toString())
            .hasAttribute(AttributeKey.stringKey("http.request.method"), "GET")
            .hasAttribute(AttributeKey.longKey("http.response.status_code"), 200)
            .hasAttribute(AttributeKey.stringKey("server.address"), "localhost")
            .hasAttribute(AttributeKey.longKey("server.port"), webServer.port.toLong())
    }

    @Test
    fun verifyOkHttpAsyncCallSpan_withErrorCode() {
        val url = webServer.url("/")
        executeAsyncSuccessfulHttpCall("GET", 500, "{}", url)

        agentRule.flushSpans().join(5, TimeUnit.SECONDS)

        assertThat(agentRule.getFinishedSpans()).hasSize(1)
        assertThat(agentRule.getFinishedSpans().first()).hasName("GET")
            .hasKind(SpanKind.CLIENT)
            .hasStatus(StatusData.ok())
            .hasAttribute(AttributeKey.stringKey("url.full"), url.toString())
            .hasAttribute(AttributeKey.stringKey("http.request.method"), "GET")
            .hasAttribute(AttributeKey.longKey("http.response.status_code"), 500)
            .hasAttribute(AttributeKey.stringKey("server.address"), "localhost")
            .hasAttribute(AttributeKey.longKey("server.port"), webServer.port.toLong())
    }

    private fun executeAsyncSuccessfulHttpCall(
        method: String,
        responseCode: Int,
        responseBody: String,
        url: HttpUrl = webServer.url("/")
    ) {
        val mockResponse = MockResponse().setResponseCode(responseCode).setBody(responseBody)
        webServer.enqueue(mockResponse)

        val responseStr = AtomicReference("")
        try {
            executeAsyncHttpCall(
                Request.Builder()
                    .method(method, null)
                    .url(url).build()
            ) { response ->
                try {
                    responseStr.set(response.body!!.string())
                } catch (e: IOException) {
                    fail(e.message)
                }
            }
        } catch (e: InterruptedException) {
            throw RuntimeException(e)
        }
        assertThat(responseStr.get()).isEqualTo(responseBody)
    }

    private fun executeAsyncHttpCall(request: Request, responseConsumer: Consumer<Response>) {
        val latch = CountDownLatch(1)
        clientBuilder.build().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                latch.countDown()
            }

            override fun onResponse(call: Call, response: Response) {
                responseConsumer.accept(response)
                latch.countDown()
            }
        })
        latch.await(1, TimeUnit.SECONDS)
    }

    private fun executeSyncHttpCall(
        method: String,
        responseCode: Int,
        responseBody: String,
        url: HttpUrl = webServer.url("/")
    ): Response {
        val mockResponse = MockResponse().setResponseCode(responseCode).setBody(responseBody)
        webServer.enqueue(mockResponse)

        val request = Request.Builder()
            .method(method, null)
            .url(url).build()

        return clientBuilder.build().newCall(request).execute()
    }
}