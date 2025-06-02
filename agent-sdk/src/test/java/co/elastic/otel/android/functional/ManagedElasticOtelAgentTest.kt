/*
 * Licensed to Elasticsearch B.V. under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch B.V. licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package co.elastic.otel.android.functional

import android.app.Application
import android.content.Intent
import co.elastic.otel.android.features.session.SessionIdGenerator
import co.elastic.otel.android.internal.api.ManagedElasticOtelAgent
import co.elastic.otel.android.internal.features.clock.ElasticClockBroadcastReceiver
import co.elastic.otel.android.internal.features.diskbuffering.DiskBufferingConfiguration
import co.elastic.otel.android.internal.services.ServiceManager
import co.elastic.otel.android.internal.services.appinfo.AppInfoService
import co.elastic.otel.android.internal.time.SystemTimeProvider
import co.elastic.otel.android.internal.time.ntp.SntpClient
import co.elastic.otel.android.processors.ProcessorFactory
import co.elastic.otel.android.test.common.ElasticAttributes.getLogRecordDefaultAttributes
import co.elastic.otel.android.test.common.ElasticAttributes.getSpanDefaultAttributes
import co.elastic.otel.android.test.exporter.InMemoryExporterProvider
import co.elastic.otel.android.test.processor.SimpleProcessorFactory
import co.elastic.otel.android.testutils.DummySntpClient
import io.mockk.Runs
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.sdk.logs.LogRecordProcessor
import io.opentelemetry.sdk.logs.export.BatchLogRecordProcessor
import io.opentelemetry.sdk.logs.export.LogRecordExporter
import io.opentelemetry.sdk.metrics.export.MetricExporter
import io.opentelemetry.sdk.metrics.export.MetricReader
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader
import io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat
import io.opentelemetry.sdk.trace.SpanProcessor
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor
import io.opentelemetry.sdk.trace.export.SpanExporter
import java.io.File
import java.io.IOException
import java.time.Duration
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong
import org.awaitility.core.ConditionTimeoutException
import org.awaitility.kotlin.await
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class ManagedElasticOtelAgentTest {
    private lateinit var agent: ManagedElasticOtelAgent
    private lateinit var simpleProcessorFactory: SimpleProcessorFactory
    private val inMemoryExporters = InMemoryExporterProvider()

    companion object {
        private val SPAN_DEFAULT_ATTRIBUTES = getSpanDefaultAttributes()
        private val LOG_DEFAULT_ATTRIBUTES = getLogRecordDefaultAttributes()
    }

    @Before
    fun setUp() {
        simpleProcessorFactory = SimpleProcessorFactory()
    }

    @After
    fun tearDown() {
        closeAgent()
    }

    private fun closeAgent() {
        try {
            agent.close()
            inMemoryExporters.reset()
        } catch (ignored: UninitializedPropertyAccessException) {
        }
    }

    @Test
    fun `Disk buffering enabled, happy path`() {
        val configuration = DiskBufferingConfiguration.enabled()
        configuration.maxFileAgeForWrite = 500
        configuration.minFileAgeForRead = 501
        agent = initialize(diskBufferingConfiguration = configuration)

        sendSpan()
        sendLog()
        sendMetric()

        awaitForCacheFileCreation(listOf("spans", "logs", "metrics"))

        // Nothing should have gotten exported because it was stored in disk.
        assertThat(inMemoryExporters.getFinishedSpans()).isEmpty()
        assertThat(inMemoryExporters.getFinishedLogRecords()).isEmpty()
        assertThat(inMemoryExporters.getFinishedMetrics()).isEmpty()

        // Re-init
        closeAgent()
        agent = initialize(diskBufferingConfiguration = configuration)

        val waitTimeMillis2 = awaitAndTrackTimeMillis {
            inMemoryExporters.getFinishedSpans().isNotEmpty()
                    && inMemoryExporters.getFinishedLogRecords().isNotEmpty()
                    && inMemoryExporters.getFinishedMetrics().isNotEmpty()
        }
        assertThat(waitTimeMillis2).isLessThan(2000)

        // Now we should see the previously-stored signals exported.
        assertThat(inMemoryExporters.getFinishedSpans()).hasSize(1)
        assertThat(inMemoryExporters.getFinishedLogRecords()).hasSize(1)
        assertThat(inMemoryExporters.getFinishedMetrics()).hasSize(1)
    }

    @Test
    fun `Disk buffering enabled, when signals come in before init is finished`() {
        val configuration = DiskBufferingConfiguration.enabled()
        configuration.maxFileAgeForWrite = 500
        configuration.minFileAgeForRead = 501
        agent = initialize(diskBufferingConfiguration = configuration)

        sendSpan()
        sendLog()
        sendMetric()

        val waitTimeMillis = awaitAndTrackTimeMillis {
            inMemoryExporters.getFinishedSpans().isNotEmpty()
                    && inMemoryExporters.getFinishedLogRecords().isNotEmpty()
                    && inMemoryExporters.getFinishedMetrics().isNotEmpty()
        }
        assertThat(waitTimeMillis).isLessThan(1500)

        // We should see the previously-stored signals exported.
        assertThat(inMemoryExporters.getFinishedSpans()).hasSize(1)
        assertThat(inMemoryExporters.getFinishedLogRecords()).hasSize(1)
        assertThat(inMemoryExporters.getFinishedMetrics()).hasSize(1)
    }

    @Test
    fun `Disk buffering enabled with io exception`() {
        val appInfoService = mockk<AppInfoService>(relaxed = true)
        every {
            appInfoService.getCacheDir()
            appInfoService.getAvailableCacheSpace(any())
        }.throws(IOException())
        val serviceManager = spyk(ServiceManager.create(RuntimeEnvironment.getApplication()))
        every { serviceManager.getAppInfoService() }.returns(appInfoService)
        agent = initialize(
            serviceManager = serviceManager,
            diskBufferingConfiguration = DiskBufferingConfiguration.enabled()
        )

        awaitForOpenGates()

        sendSpan()
        sendLog()
        sendMetric()

        // The signals should have gotten exported right away.
        assertThat(inMemoryExporters.getFinishedSpans()).hasSize(1)
        assertThat(inMemoryExporters.getFinishedLogRecords()).hasSize(1)
        assertThat(inMemoryExporters.getFinishedMetrics()).hasSize(1)
    }

    @Test
    fun `Disk buffering disabled`() {
        agent = initialize(diskBufferingConfiguration = DiskBufferingConfiguration.disabled())

        sendSpan()
        sendLog()
        sendMetric()

        awaitForOpenGates()

        val waitTimeMillis = awaitAndTrackTimeMillis {
            inMemoryExporters.getFinishedSpans().size == 1 &&
                    inMemoryExporters.getFinishedLogRecords().size == 1 &&
                    inMemoryExporters.getFinishedMetrics().size == 1
        }

        // The signals should have gotten exported right away.
        assertThat(waitTimeMillis).isLessThan(1000)
    }

    @Test
    fun `Verify clock behavior`() {
        val localTimeReference = 1577836800000L
        val timeOffset = 500L
        val expectedCurrentTime = localTimeReference + timeOffset
        val sntpClient = mockk<SntpClient>()
        val currentTime = AtomicLong(0)
        val systemTimeProvider = spyk(SystemTimeProvider())
        every { systemTimeProvider.getCurrentTimeMillis() }.answers {
            currentTime.get()
        }
        every { systemTimeProvider.getElapsedRealTime() }.returns(0)
        every { sntpClient.fetchTimeOffset(localTimeReference) }.returns(
            SntpClient.Response.Success(timeOffset)
        )
        every { sntpClient.close() } just Runs
        agent = initialize(sntpClient = sntpClient, systemTimeProvider = systemTimeProvider)

        await.until {
            agent.getElasticClockManager().getTimeOffsetManager()
                .getTimeOffset() == expectedCurrentTime
        }

        sendSpan()

        assertThat(inMemoryExporters.getFinishedSpans().first()).startsAt(
            expectedCurrentTime * 1_000_000
        )

        // Reset after almost 24h with unavailable ntp server.
        closeAgent()
        every { sntpClient.fetchTimeOffset(any()) }.returns(
            SntpClient.Response.Error(SntpClient.ErrorType.TRY_LATER)
        )
        currentTime.set(currentTime.get() + TimeUnit.HOURS.toMillis(24) - 1)
        agent = initialize(sntpClient = sntpClient, systemTimeProvider = systemTimeProvider)

        sendSpan()

        awaitForOpenGates()

        assertThat(inMemoryExporters.getFinishedSpans().first()).startsAt(
            expectedCurrentTime * 1_000_000
        )

        // Forward time past 24h and trigger time sync
        currentTime.set(currentTime.get() + 1)
        inMemoryExporters.resetExporters()
        val elapsedTime = 1000L
        every { systemTimeProvider.getElapsedRealTime() }.returns(elapsedTime)
        agent.getElasticClockManager().getTimeOffsetManager().sync()

        sendSpan()

        assertThat(inMemoryExporters.getFinishedSpans().first()).startsAt(
            currentTime.get() * 1_000_000
        )

        // Ensuring cache was cleared.
        closeAgent()
        every { sntpClient.fetchTimeOffset(any()) }.returns(
            SntpClient.Response.Error(SntpClient.ErrorType.TRY_LATER)
        )
        currentTime.set(currentTime.get() + 1000)
        agent = initialize(sntpClient = sntpClient, systemTimeProvider = systemTimeProvider)

        sendSpan()
        sendLog()

        awaitForOpenGates(5)

        await.atMost(Duration.ofSeconds(2))
            .until { inMemoryExporters.getFinishedSpans().isNotEmpty() }

        assertThat(inMemoryExporters.getFinishedSpans().first()).startsAt(
            currentTime.get() * 1_000_000
        )

        // Picking up new time when available
        inMemoryExporters.resetExporters()
        every { sntpClient.fetchTimeOffset(localTimeReference + elapsedTime) }.returns(
            SntpClient.Response.Success(timeOffset)
        )
        agent.getElasticClockManager().getTimeOffsetManager().sync()

        sendSpan()

        assertThat(inMemoryExporters.getFinishedSpans().first()).startsAt(
            (timeOffset + localTimeReference + elapsedTime) * 1_000_000
        )

        // Reset after 24h with unavailable ntp server.
        closeAgent()
        every { sntpClient.fetchTimeOffset(any()) }.returns(
            SntpClient.Response.Error(SntpClient.ErrorType.TRY_LATER)
        )
        currentTime.set(currentTime.get() + TimeUnit.HOURS.toMillis(24))
        agent = initialize(sntpClient = sntpClient, systemTimeProvider = systemTimeProvider)

        sendSpan()
        sendLog()

        awaitForOpenGates(5)

        assertThat(inMemoryExporters.getFinishedSpans().first()).startsAt(
            currentTime.get() * 1_000_000
        )

        // Restarting with remote time available.
        closeAgent()
        every { sntpClient.fetchTimeOffset(localTimeReference + elapsedTime) }.returns(
            SntpClient.Response.Success(timeOffset)
        )
        agent = initialize(sntpClient = sntpClient, systemTimeProvider = systemTimeProvider)
        agent.getElasticClockManager().getTimeOffsetManager().sync()

        sendSpan()

        awaitForOpenGates()

        assertThat(inMemoryExporters.getFinishedSpans().first()).startsAt(
            (timeOffset + localTimeReference + elapsedTime) * 1_000_000
        )

        // Ensuring cache is cleared on reboot.
        closeAgent()
        triggerRebootBroadcast()
        every { sntpClient.fetchTimeOffset(any()) }.returns(
            SntpClient.Response.Error(SntpClient.ErrorType.TRY_LATER)
        )
        currentTime.set(currentTime.get() + 1000)
        agent = initialize(sntpClient = sntpClient, systemTimeProvider = systemTimeProvider)

        sendSpan()
        sendLog()

        awaitForOpenGates(5)

        assertThat(inMemoryExporters.getFinishedSpans().first()).startsAt(
            currentTime.get() * 1_000_000
        )
    }

    private fun triggerRebootBroadcast() {
        val intent = Intent(Intent.ACTION_BOOT_COMPLETED)
        val broadcastReceivers =
            RuntimeEnvironment.getApplication().packageManager.queryBroadcastReceivers(intent, 0)
        assertThat(broadcastReceivers).hasSize(1)
        ElasticClockBroadcastReceiver().onReceive(RuntimeEnvironment.getApplication(), intent)
    }

    @Test
    fun `Verify clock initialization behavior`() {
        val localTimeReference = 1577836800000L
        val timeOffset = 500L
        val expectedCurrentTime = localTimeReference + timeOffset
        val sntpClient = mockk<SntpClient>()
        val currentTime = AtomicLong(12345)
        val systemTimeProvider = spyk(SystemTimeProvider())
        every { systemTimeProvider.getCurrentTimeMillis() }.answers {
            currentTime.get()
        }
        every { systemTimeProvider.getElapsedRealTime() }.returns(0)
        every { sntpClient.fetchTimeOffset(localTimeReference) }.returns(
            SntpClient.Response.Error(SntpClient.ErrorType.TRY_LATER)
        ).andThen(
            SntpClient.Response.Success(timeOffset)
        )
        every { sntpClient.close() } just Runs
        agent = initialize(
            sntpClient = sntpClient,
            systemTimeProvider = systemTimeProvider,
            sessionIdGenerator = { "session-id" }
        )

        sendSpan()
        sendLog()
        sendMetric()

        await.atMost(Duration.ofSeconds(1)).until {
            agent.getExporterGateManager().metricGateIsOpen()
        }

        // Spans and logs aren't exported yet.
        assertThat(inMemoryExporters.getFinishedSpans()).isEmpty()
        assertThat(inMemoryExporters.getFinishedLogRecords()).isEmpty()
        assertThat(inMemoryExporters.getFinishedMetrics()).hasSize(1)

        // Time gets eventually set.
        agent.getElasticClockManager().getTimeOffsetManager().sync()

        await.until {
            inMemoryExporters.getFinishedSpans().isNotEmpty()
        }
        await.until {
            inMemoryExporters.getFinishedLogRecords().isNotEmpty()
        }

        val spanData = inMemoryExporters.getFinishedSpans().first()
        assertThat(spanData).startsAt(
            expectedCurrentTime * 1_000_000
        ).hasAttributes(SPAN_DEFAULT_ATTRIBUTES)
        val logRecordData = inMemoryExporters.getFinishedLogRecords().first()
        assertThat(logRecordData).hasTimestamp(expectedCurrentTime * 1_000_000)
            .hasObservedTimestamp(currentTime.get() * 1_000_000)
            .hasAttributes(LOG_DEFAULT_ATTRIBUTES)

        // Send new data just for fun
        inMemoryExporters.resetExporters()

        sendSpan()
        sendLog()
        sendMetric()

        assertThat(inMemoryExporters.getFinishedSpans()).hasSize(1)
        assertThat(inMemoryExporters.getFinishedLogRecords()).hasSize(1)
        assertThat(inMemoryExporters.getFinishedMetrics()).hasSize(1)

        assertThat(inMemoryExporters.getFinishedSpans().first()).startsAt(
            expectedCurrentTime * 1_000_000
        ).hasAttributes(SPAN_DEFAULT_ATTRIBUTES)
        assertThat(inMemoryExporters.getFinishedLogRecords().first())
            .hasTimestamp(0)
            .hasObservedTimestamp(expectedCurrentTime * 1_000_000)
            .hasAttributes(LOG_DEFAULT_ATTRIBUTES)
    }

    @Test
    fun `Verify clock initialization behavior when processor delays signals`() {
        val localTimeReference = 1577836800000L
        val timeOffset = 500L
        val expectedCurrentTime = localTimeReference + timeOffset
        val sntpClient = mockk<SntpClient>()
        val currentTime = AtomicLong(12345)
        val systemTimeProvider = spyk(SystemTimeProvider())
        every { systemTimeProvider.getCurrentTimeMillis() }.answers {
            currentTime.get()
        }
        every { systemTimeProvider.getElapsedRealTime() }.returns(0)
        every { sntpClient.fetchTimeOffset(localTimeReference) }.returns(
            SntpClient.Response.Error(SntpClient.ErrorType.TRY_LATER)
        ).andThen(
            SntpClient.Response.Success(timeOffset)
        )
        every { sntpClient.close() } just Runs
        agent = initialize(
            sessionIdGenerator = { "session-id" },
            sntpClient = sntpClient,
            systemTimeProvider = systemTimeProvider,
            processorFactory = BatchProcessorFactory()
        )

        sendSpan()
        sendLog()

        await.atMost(Duration.ofSeconds(1)).until {
            agent.getExporterGateManager().metricGateIsOpen()
        }

        // Spans and logs aren't exported yet.
        assertThat(inMemoryExporters.getFinishedSpans()).isEmpty()
        assertThat(inMemoryExporters.getFinishedLogRecords()).isEmpty()

        // Time gets eventually set.
        agent.getElasticClockManager().getTimeOffsetManager().sync()

        await.until {
            inMemoryExporters.getFinishedSpans().isNotEmpty()
        }
        await.until {
            inMemoryExporters.getFinishedLogRecords().isNotEmpty()
        }

        val spanData = inMemoryExporters.getFinishedSpans().first()
        assertThat(spanData).startsAt(
            expectedCurrentTime * 1_000_000
        ).hasAttributes(SPAN_DEFAULT_ATTRIBUTES)
        val logRecordData = inMemoryExporters.getFinishedLogRecords().first()
        assertThat(logRecordData).hasTimestamp(expectedCurrentTime * 1_000_000)
            .hasObservedTimestamp(currentTime.get() * 1_000_000)
            .hasAttributes(LOG_DEFAULT_ATTRIBUTES)

        // Send new data just for fun
        inMemoryExporters.resetExporters()

        sendSpan()
        sendLog()

        await.until {
            inMemoryExporters.getFinishedSpans().isNotEmpty()
        }
        await.until {
            inMemoryExporters.getFinishedLogRecords().isNotEmpty()
        }
        assertThat(inMemoryExporters.getFinishedSpans()).hasSize(1)
        assertThat(inMemoryExporters.getFinishedLogRecords()).hasSize(1)

        assertThat(inMemoryExporters.getFinishedSpans().first()).startsAt(
            expectedCurrentTime * 1_000_000
        ).hasAttributes(SPAN_DEFAULT_ATTRIBUTES)
        assertThat(inMemoryExporters.getFinishedLogRecords().first())
            .hasTimestamp(0)
            .hasObservedTimestamp(expectedCurrentTime * 1_000_000)
            .hasAttributes(LOG_DEFAULT_ATTRIBUTES)
    }

    @Test
    fun `Verify clock initialization when the first signal items come after init is done`() {
        val localTimeReference = 1577836800000L
        val timeOffset = 500L
        val expectedCurrentTime = localTimeReference + timeOffset
        val sntpClient = mockk<SntpClient>()
        val currentTime = AtomicLong(12345)
        val systemTimeProvider = spyk(SystemTimeProvider())
        every { systemTimeProvider.getCurrentTimeMillis() }.answers {
            currentTime.get()
        }
        every { systemTimeProvider.getElapsedRealTime() }.returns(0)
        every { sntpClient.fetchTimeOffset(localTimeReference) }.returns(
            SntpClient.Response.Success(timeOffset)
        )
        every { sntpClient.close() } just Runs
        agent = initialize(
            sessionIdGenerator = { "session-id" },
            sntpClient = sntpClient,
            systemTimeProvider = systemTimeProvider,
        )

        await.until {
            agent.getElasticClockManager().getClock().now() == expectedCurrentTime * 1_000_000
        }

        sendSpan()
        sendLog()
        sendMetric()

        // Everything's exported right away
        assertThat(inMemoryExporters.getFinishedSpans()).hasSize(1)
        assertThat(inMemoryExporters.getFinishedLogRecords()).hasSize(1)
        assertThat(inMemoryExporters.getFinishedMetrics()).hasSize(1)

        val spanData = inMemoryExporters.getFinishedSpans().first()
        assertThat(spanData).startsAt(
            expectedCurrentTime * 1_000_000
        ).hasAttributes(SPAN_DEFAULT_ATTRIBUTES)
        val logRecordData = inMemoryExporters.getFinishedLogRecords().first()
        assertThat(logRecordData).hasTimestamp(0)
            .hasObservedTimestamp(expectedCurrentTime * 1_000_000)
            .hasAttributes(LOG_DEFAULT_ATTRIBUTES)
    }

    @Test
    fun `Verify clock initialization behavior, when latch waiting times out`() {
        val sntpClient = mockk<SntpClient>()
        val currentTime = AtomicLong(12345)
        val systemTimeProvider = spyk(SystemTimeProvider())
        every { systemTimeProvider.getCurrentTimeMillis() }.answers {
            currentTime.get()
        }
        every { systemTimeProvider.getElapsedRealTime() }.returns(0)
        every { sntpClient.fetchTimeOffset(any()) }.returns(
            SntpClient.Response.Error(SntpClient.ErrorType.TRY_LATER)
        )
        every { sntpClient.close() } just Runs
        agent = initialize(
            sessionIdGenerator = { "session-id" },
            sntpClient = sntpClient,
            systemTimeProvider = systemTimeProvider,
        )

        await.atMost(Duration.ofSeconds(1)).until {
            agent.getExporterGateManager().metricGateIsOpen()
        }

        sendSpan()
        sendLog()
        sendMetric()

        // Spans and logs aren't exported yet.
        assertThat(inMemoryExporters.getFinishedSpans()).isEmpty()
        assertThat(inMemoryExporters.getFinishedLogRecords()).isEmpty()
        assertThat(inMemoryExporters.getFinishedMetrics()).hasSize(1)

        val waitTimeMillis = awaitAndTrackTimeMillis {
            inMemoryExporters.getFinishedSpans().isNotEmpty()
                    && inMemoryExporters.getFinishedLogRecords().isNotEmpty()
        }

        assertThat(waitTimeMillis).isBetween(2500, 3500)
        val spanData = inMemoryExporters.getFinishedSpans().first()
        assertThat(spanData).startsAt(
            currentTime.get() * 1_000_000
        ).hasAttributes(SPAN_DEFAULT_ATTRIBUTES)
        val logRecordData = inMemoryExporters.getFinishedLogRecords().first()
        assertThat(logRecordData)
            .hasTimestamp(0)
            .hasObservedTimestamp(currentTime.get() * 1_000_000)
            .hasAttributes(LOG_DEFAULT_ATTRIBUTES)
    }

    @Test
    fun `Verify clock initialization behavior, when buffer gets full`() {
        val sntpClient = mockk<SntpClient>()
        val currentTime = AtomicLong(12345)
        val systemTimeProvider = spyk(SystemTimeProvider())
        val bufferSize = 10
        every { systemTimeProvider.getCurrentTimeMillis() }.answers {
            currentTime.get()
        }
        every { systemTimeProvider.getElapsedRealTime() }.returns(0)
        every { sntpClient.fetchTimeOffset(any()) }.returns(
            SntpClient.Response.Error(SntpClient.ErrorType.TRY_LATER)
        )
        every { sntpClient.close() } just Runs
        agent = initialize(
            sessionIdGenerator = { "session-id" },
            sntpClient = sntpClient,
            systemTimeProvider = systemTimeProvider,
            gateSignalBufferSize = bufferSize
        )

        repeat(bufferSize) {
            sendSpan()
            sendLog()
        }

        // Spans and logs aren't exported yet.
        assertThat(inMemoryExporters.getFinishedSpans()).isEmpty()
        assertThat(inMemoryExporters.getFinishedLogRecords()).isEmpty()

        // Sending one more to go over the buffer size.
        sendSpan()
        sendLog()

        val waitTimeMillis = awaitAndTrackTimeMillis {
            inMemoryExporters.getFinishedSpans().isNotEmpty()
                    && inMemoryExporters.getFinishedLogRecords().isNotEmpty()
        }

        assertThat(waitTimeMillis).isLessThan(1000)
        assertThat(inMemoryExporters.getFinishedSpans()).hasSize(bufferSize + 1)
        assertThat(inMemoryExporters.getFinishedLogRecords()).hasSize(bufferSize + 1)
        val spanData = inMemoryExporters.getFinishedSpans().first()
        assertThat(spanData).startsAt(
            currentTime.get() * 1_000_000
        ).hasAttributes(SPAN_DEFAULT_ATTRIBUTES)
        val logRecordData = inMemoryExporters.getFinishedLogRecords().first()
        assertThat(logRecordData)
            .hasTimestamp(0)
            .hasObservedTimestamp(currentTime.get() * 1_000_000)
            .hasAttributes(LOG_DEFAULT_ATTRIBUTES)
    }

    @Test
    fun `Verify session manager behavior`() {
        val timeLimitMillis = TimeUnit.MINUTES.toMillis(30)
        val currentTimeMillis = AtomicLong(timeLimitMillis)
        val systemTimeProvider = spyk(SystemTimeProvider())
        every { systemTimeProvider.getCurrentTimeMillis() }.answers { currentTimeMillis.get() }
        val sessionIdGenerator = mockk<SessionIdGenerator>()
        every { sessionIdGenerator.generate() }.returns("first-id")
        agent = initialize(
            sessionIdGenerator = { sessionIdGenerator.generate() },
            systemTimeProvider = systemTimeProvider,
        )

        awaitForOpenGates()

        sendSpan()
        sendLog()

        verifySessionId(inMemoryExporters.getFinishedSpans().first().attributes, "first-id")
        verifySessionId(
            inMemoryExporters.getFinishedLogRecords().first().attributes,
            "first-id"
        )
        verify(exactly = 1) { sessionIdGenerator.generate() }

        // Reset and verify that the id has been cached for just under the time limit.
        clearMocks(sessionIdGenerator)
        closeAgent()
        currentTimeMillis.set(currentTimeMillis.get() + timeLimitMillis - 1)
        agent = initialize(
            sessionIdGenerator = { sessionIdGenerator.generate() },
            systemTimeProvider = systemTimeProvider,
        )

        awaitForOpenGates()

        sendSpan()
        sendLog()

        verifySessionId(inMemoryExporters.getFinishedSpans().first().attributes, "first-id")
        verifySessionId(
            inMemoryExporters.getFinishedLogRecords().first().attributes,
            "first-id"
        )
        verify(exactly = 0) { sessionIdGenerator.generate() } // Was retrieved from cache.

        // Reset and verify that the id expires after an idle period of time
        clearMocks(sessionIdGenerator)
        every { sessionIdGenerator.generate() }.returns("second-id")
        inMemoryExporters.resetExporters()
        currentTimeMillis.set(currentTimeMillis.get() + timeLimitMillis - 1)

        sendSpan()
        sendLog()

        // Idle time passes
        currentTimeMillis.set(currentTimeMillis.get() + timeLimitMillis)

        sendSpan()
        sendLog()

        val finishedSpanItems = inMemoryExporters.getFinishedSpans()
        val finishedLogRecordItems = inMemoryExporters.getFinishedLogRecords()
        verifySessionId(finishedSpanItems.first().attributes, "first-id")
        verifySessionId(finishedSpanItems[1].attributes, "second-id")
        verifySessionId(finishedLogRecordItems.first().attributes, "first-id")
        verifySessionId(finishedLogRecordItems[1].attributes, "second-id")
        verify(exactly = 1) { sessionIdGenerator.generate() } // Was regenerated after idle time.

        // Reset and verify that the id expires 4 hours regardless of not enough idle time.
        clearMocks(sessionIdGenerator)
        every { sessionIdGenerator.generate() }.returns("third-id")
            .andThen("fourth-id")
        inMemoryExporters.resetExporters()

        repeat(18) { // each idle time 30 min
            currentTimeMillis.set(currentTimeMillis.get() + timeLimitMillis - 1)
            sendSpan()
        }

        val spanItems = inMemoryExporters.getFinishedSpans()
        var position = 0
        repeat(8) {
            verifySessionId(spanItems[position].attributes, "second-id")
            position++
        }
        repeat(8) {
            verifySessionId(spanItems[position].attributes, "third-id")
            position++
        }
        verifySessionId(spanItems[16].attributes, "third-id")
        verifySessionId(spanItems[17].attributes, "fourth-id")
    }

    private fun initialize(
        application: Application = RuntimeEnvironment.getApplication(),
        serviceManager: ServiceManager = ServiceManager.create(application),
        diskBufferingConfiguration: DiskBufferingConfiguration = DiskBufferingConfiguration.disabled(),
        systemTimeProvider: SystemTimeProvider = SystemTimeProvider(),
        sntpClient: SntpClient = DummySntpClient(),
        gateSignalBufferSize: Int? = null,
        sessionIdGenerator: SessionIdGenerator? = null,
        processorFactory: ProcessorFactory = simpleProcessorFactory
    ): ManagedElasticOtelAgent {
        val featuresBuilder =
            ManagedElasticOtelAgent.ManagedFeatures.Builder(application)
                .setSntpClient(sntpClient)
                .setDiskBufferingConfiguration(diskBufferingConfiguration)
        sessionIdGenerator?.let { featuresBuilder.setSessionIdGenerator(it) }
        gateSignalBufferSize?.let { featuresBuilder.setGateSignalBufferSize(it) }
        return ManagedElasticOtelAgent.Builder()
            .setProcessorFactory(processorFactory)
            .setExporterProvider(inMemoryExporters)
            .build(serviceManager, featuresBuilder.build(serviceManager, systemTimeProvider))
    }

    private fun verifySessionId(attributes: Attributes, value: String) {
        assertThat(attributes.get(AttributeKey.stringKey("session.id"))).isEqualTo(value)
    }

    private fun sendSpan(name: String = "span-name") {
        agent.getOpenTelemetry().getTracer("TestTracer")
            .spanBuilder(name)
            .startSpan()
            .end()
    }

    private fun sendLog() {
        agent.getOpenTelemetry().logsBridge.get("LoggerScope").logRecordBuilder()
            .setBody("Log body").emit()
    }

    private fun sendMetric() {
        agent.getOpenTelemetry().getMeter("MeterScope")
            .counterBuilder("counter")
            .build()
            .add(1)
        simpleProcessorFactory.flushMetrics().join(1, TimeUnit.SECONDS)
    }

    private fun awaitAndTrackTimeMillis(condition: () -> Boolean): Long {
        val waitStart = System.nanoTime()
        await.until(condition)
        val waitTimeMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - waitStart)
        return waitTimeMillis
    }

    private fun awaitForCacheFileCreation(dirNames: List<String>) {
        val signalsDir = File(RuntimeEnvironment.getApplication().cacheDir, "opentelemetry/signals")
        val dirs = dirNames.map { File(signalsDir, it) }
        await.until {
            var dirsNotEmpty = 0
            dirs.forEach {
                if (it.list().isNotEmpty()) {
                    dirsNotEmpty++
                }
            }
            dirsNotEmpty == dirs.size
        }
    }

    private fun awaitForOpenGates(maxSecondsToWait: Int = 1) {
        try {
            await.atMost(Duration.ofSeconds(maxSecondsToWait.toLong())).until {
                agent.getExporterGateManager().allGatesAreOpen()
            }
        } catch (e: ConditionTimeoutException) {
            println(
                "Pending latches: \n${
                    agent.getExporterGateManager().getAllOpenLatches().joinToString("\n")
                }"
            )
            throw e
        }
    }

    class BatchProcessorFactory : ProcessorFactory {
        override fun createSpanProcessor(exporter: SpanExporter?): SpanProcessor? {
            return BatchSpanProcessor.builder(exporter)
                .setScheduleDelay(Duration.ofSeconds(2))
                .build()
        }

        override fun createLogRecordProcessor(exporter: LogRecordExporter?): LogRecordProcessor? {
            return BatchLogRecordProcessor.builder(exporter)
                .setScheduleDelay(Duration.ofSeconds(2))
                .build()
        }

        override fun createMetricReader(exporter: MetricExporter?): MetricReader? {
            return PeriodicMetricReader.builder(exporter)
                .setInterval(Duration.ofSeconds(4))
                .build()
        }
    }
}