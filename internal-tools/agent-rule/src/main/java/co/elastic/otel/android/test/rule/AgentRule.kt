package co.elastic.otel.android.test.rule

import android.app.Application
import co.elastic.otel.android.api.ElasticOtelAgent
import co.elastic.otel.android.exporters.ExporterProvider
import co.elastic.otel.android.internal.api.ManagedElasticOtelAgent
import co.elastic.otel.android.internal.features.diskbuffering.DiskBufferingConfiguration
import co.elastic.otel.android.internal.services.ServiceManager
import co.elastic.otel.android.internal.time.SystemTimeProvider
import co.elastic.otel.android.internal.time.ntp.SntpClient
import co.elastic.otel.android.test.common.ElasticAttributes.DEFAULT_SESSION_ID
import io.opentelemetry.sdk.common.CompletableResultCode
import io.opentelemetry.sdk.logs.data.LogRecordData
import io.opentelemetry.sdk.logs.export.LogRecordExporter
import io.opentelemetry.sdk.metrics.data.MetricData
import io.opentelemetry.sdk.metrics.export.MetricExporter
import io.opentelemetry.sdk.testing.exporter.InMemoryLogRecordExporter
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricExporter
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter
import io.opentelemetry.sdk.trace.data.SpanData
import io.opentelemetry.sdk.trace.export.SpanExporter
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

abstract class AgentRule : TestRule {
    @Volatile
    private var agent: ManagedElasticOtelAgent? = null

    @Volatile
    private var inMemoryExporters: InMemoryExporterProvider? = null

    override fun apply(base: Statement, description: Description): Statement {
        return try {
            object : Statement() {
                override fun evaluate() {
                    inMemoryExporters = InMemoryExporterProvider()
                    runInitialization {
                        agent = createAgent(getApplication())
                    }
                    base.evaluate()
                }
            }
        } finally {
            agent = null
            inMemoryExporters = null
        }
    }

    fun getAgent(): ElasticOtelAgent {
        return agent!!
    }

    fun getFinishedMetrics(): List<MetricData> {
        return inMemoryExporters!!.getFinishedMetrics()
    }

    fun getFinishedLogRecords(): List<LogRecordData> {
        return inMemoryExporters!!.getFinishedLogRecords()
    }

    fun getFinishedSpans(): List<SpanData> {
        return inMemoryExporters!!.getFinishedSpans()
    }

    fun flushSpans(): CompletableResultCode {
        return agent!!.flushSpans()
    }

    fun flushLogs(): CompletableResultCode {
        return agent!!.flushLogRecords()
    }

    private fun createAgent(application: Application): ManagedElasticOtelAgent {
        val serviceManager = ServiceManager.create(application)
        val features = ManagedElasticOtelAgent.ManagedFeatures.Builder(application)
            .setSntpClient(LocalSntpClient())
            .setSessionIdGenerator { DEFAULT_SESSION_ID }
            .setDiskBufferingConfiguration(DiskBufferingConfiguration.disabled())
            .build(serviceManager, SystemTimeProvider())

        return ManagedElasticOtelAgent.Builder()
            .setExporterProvider(inMemoryExporters!!)
            .build(serviceManager, features)
    }

    class LocalSntpClient : SntpClient {

        override fun fetchTimeOffset(currentTimeMillis: Long): SntpClient.Response {
            return SntpClient.Response.Success(0)
        }

        override fun close() {
        }
    }

    private class InMemoryExporterProvider : ExporterProvider {
        private val inMemoryMetricExporter = InMemoryMetricExporter.create()
        private val inMemoryLogRecordExporter = InMemoryLogRecordExporter.create()
        private val inMemorySpanExporter = InMemorySpanExporter.create()

        fun getFinishedMetrics(): List<MetricData> {
            return inMemoryMetricExporter.finishedMetricItems
        }

        fun getFinishedLogRecords(): List<LogRecordData> {
            return inMemoryLogRecordExporter.finishedLogRecordItems
        }

        fun getFinishedSpans(): List<SpanData> {
            return inMemorySpanExporter.finishedSpanItems
        }

        override fun getSpanExporter(): SpanExporter? {
            return inMemorySpanExporter
        }

        override fun getLogRecordExporter(): LogRecordExporter? {
            return inMemoryLogRecordExporter
        }

        override fun getMetricExporter(): MetricExporter? {
            return inMemoryMetricExporter
        }
    }

    abstract fun runInitialization(initialization: () -> Unit)

    abstract fun getApplication(): Application
}