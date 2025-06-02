package co.elastic.otel.android.sample

import android.app.Application
import androidx.test.platform.app.InstrumentationRegistry
import co.elastic.otel.android.ElasticApmAgent
import co.elastic.otel.android.extensions.log
import co.elastic.otel.android.extensions.span
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class OtlpHttpTest {
    private lateinit var webServer: MockWebServer
    private lateinit var agent: ElasticApmAgent

    @Before
    fun setUp() {
        webServer = MockWebServer()
        webServer.start()
        agent =
            ElasticApmAgent.builder(InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as Application)
                .setExportUrl(webServer.url("/").toString())
                .setServiceName("test-app")
                .build()
    }

    @After
    fun tearDown() {
        webServer.shutdown()
        agent.close()
    }

    @Test
    fun verifyHttpRequests() {
        val expectedPaths = listOf("/v1/traces", "/v1/metrics", "/v1/logs")
        val receivedPaths = mutableListOf<String>()
        val latch = CountDownLatch(3)
        webServer.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                val path = request.path!!
                if (path !in receivedPaths) {
                    receivedPaths.add(path)
                    latch.countDown()
                }
                return MockResponse()
            }
        }

        agent.span("Some span") {
            agent.log("Some log")
        }
        agent.getOpenTelemetry().getMeter("MeterScope").counterBuilder("Counter").build().add(1)

        latch.await(30, TimeUnit.SECONDS)

        assertEquals(expectedPaths.sorted(), receivedPaths.sorted())
    }
}