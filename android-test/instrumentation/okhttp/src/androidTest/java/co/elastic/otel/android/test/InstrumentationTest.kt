package co.elastic.otel.android.test

import co.elastic.otel.android.test.rule.AndroidTestAgentRule
import io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat
import java.io.IOException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import java.util.function.Consumer
import junit.framework.TestCase.fail
import okhttp3.Call
import okhttp3.Callback
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
        executeSyncHttpCall("GET", 200, "{}")

        agentRule.flushSpans().join(5, TimeUnit.SECONDS)

        assertThat(agentRule.getFinishedSpans()).hasSize(1)
        assertThat(agentRule.getFinishedSpans().first()).hasName("GET")
    }

    private fun executeSuccessfulHttpCall(responseCode: Int) {
        executeSuccessfulHttpCall(responseCode, "{}", emptyMap())
    }

    private fun executeSuccessfulHttpCall(
        responseCode: Int,
        responseBody: String,
        responseHeaders: Map<String, String>
    ) {
        val mockResponse = MockResponse().setResponseCode(responseCode).setBody(responseBody)
        responseHeaders.forEach { (name: String, value: String) ->
            mockResponse.addHeader(name, value)
        }
        webServer.enqueue(mockResponse)

        val responseStr = AtomicReference("")
        try {
            executeAsyncHttpCall(
                Request.Builder().url(webServer.url("/")).build()
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

    private fun executeFailedHttpCall() {
        val url = webServer.url("/")
        webServer.shutdown()

        try {
            executeAsyncHttpCall(Request.Builder().url(url).build()) { }
        } catch (e: InterruptedException) {
            throw RuntimeException(e)
        }
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
        responseBody: String
    ): Response {
        val mockResponse = MockResponse().setResponseCode(responseCode).setBody(responseBody)
        webServer.enqueue(mockResponse)

        val request = Request.Builder()
            .method(method, null)
            .url(webServer.url("/")).build()

        return clientBuilder.build().newCall(request).execute()
    }
}