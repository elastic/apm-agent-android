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
package co.elastic.otel.android.internal.features.diskbuffering

import co.elastic.otel.android.common.internal.logging.Elog
import co.elastic.otel.android.features.diskbuffering.DiskBufferingConfiguration
import co.elastic.otel.android.internal.exporters.configurable.MutableLogRecordExporter
import co.elastic.otel.android.internal.exporters.configurable.MutableMetricExporter
import co.elastic.otel.android.internal.exporters.configurable.MutableSpanExporter
import co.elastic.otel.android.internal.features.diskbuffering.tools.DiskBufferingExporterCallback
import co.elastic.otel.android.internal.features.diskbuffering.tools.DiskManager
import co.elastic.otel.android.internal.features.diskbuffering.tools.FromDiskExporter
import co.elastic.otel.android.internal.features.exportergate.ExporterGateManager
import co.elastic.otel.android.internal.services.ServiceManager
import co.elastic.otel.android.internal.time.SystemTimeProvider
import io.opentelemetry.contrib.disk.buffering.exporters.LogRecordToDiskExporter
import io.opentelemetry.contrib.disk.buffering.exporters.MetricToDiskExporter
import io.opentelemetry.contrib.disk.buffering.exporters.SpanToDiskExporter
import io.opentelemetry.contrib.disk.buffering.storage.SignalStorage
import io.opentelemetry.contrib.disk.buffering.storage.impl.FileLogRecordStorage
import io.opentelemetry.contrib.disk.buffering.storage.impl.FileMetricStorage
import io.opentelemetry.contrib.disk.buffering.storage.impl.FileSpanStorage
import io.opentelemetry.contrib.disk.buffering.storage.impl.FileStorageConfiguration
import io.opentelemetry.sdk.logs.export.LogRecordExporter
import io.opentelemetry.sdk.metrics.export.MetricExporter
import io.opentelemetry.sdk.trace.export.SpanExporter
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.seconds

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
internal class DiskBufferingManager private constructor(
    private val systemTimeProvider: SystemTimeProvider,
    private val serviceManager: ServiceManager,
    private val gateManager: ExporterGateManager,
    private val configuration: DiskBufferingConfiguration,
    private val exportFromDiskIntervalMillis: Long = TimeUnit.SECONDS.toMillis(5)
) {
    private var spanExporter: MutableSpanExporter? = null
    private var logRecordExporter: MutableLogRecordExporter? = null
    private var metricExporter: MutableMetricExporter? = null
    private var interceptedSpanExporter: SpanExporter? = null
    private var interceptedLogRecordExporter: LogRecordExporter? = null
    private var interceptedMetricExporter: MetricExporter? = null
    private var toDiskSpanExporter: SpanToDiskExporter? = null
    private var toDiskLogRecordExporter: LogRecordToDiskExporter? = null
    private var toDiskMetricExporter: MetricToDiskExporter? = null
    private var signalFromDiskExporter: SignalFromDiskExporter? = null
    private val logger = Elog.getLogger()

    private fun exportFromDisk() {
        logger.debug("About to start exporting from disk")
        signalFromDiskExporter?.let {
            var exportedTimes = 0
            while (it.exportBatchOfEach()) {
                exportedTimes++
            }
            logger.debug("Times signals exported from disk: {}", exportedTimes)
        } ?: logger.debug("Signal from disk exporter is null")
    }

    internal fun close() {
        signalFromDiskExporter?.close()
        signalFromDiskExporter = null
        interceptedSpanExporter?.shutdown()
        interceptedSpanExporter = null
        interceptedLogRecordExporter?.shutdown()
        interceptedLogRecordExporter = null
        interceptedMetricExporter?.shutdown()
        interceptedMetricExporter = null
        toDiskSpanExporter?.shutdown()
        toDiskSpanExporter = null
        toDiskLogRecordExporter?.shutdown()
        toDiskLogRecordExporter = null
        toDiskMetricExporter?.shutdown()
        toDiskMetricExporter = null
    }

    internal fun interceptSpanExporter(interceptedSpanExporter: SpanExporter): SpanExporter {
        this.interceptedSpanExporter = interceptedSpanExporter
        spanExporter = MutableSpanExporter().apply { setDelegate(interceptedSpanExporter) }
        return spanExporter!!
    }

    internal fun interceptLogRecordExporter(interceptedLogRecordExporter: LogRecordExporter): LogRecordExporter {
        this.interceptedLogRecordExporter = interceptedLogRecordExporter
        logRecordExporter =
            MutableLogRecordExporter().apply { setDelegate(interceptedLogRecordExporter) }
        return logRecordExporter!!
    }

    internal fun interceptMetricExporter(interceptedMetricExporter: MetricExporter): MetricExporter {
        this.interceptedMetricExporter = interceptedMetricExporter
        metricExporter = MutableMetricExporter().apply { setDelegate(interceptedMetricExporter) }
        return metricExporter!!
    }

    internal fun initialize() {
        logger.debug("Initializing disk buffering with configuration: {}", configuration)
        if (configuration is DiskBufferingConfiguration.Enabled) {
            doInitialize(configuration)
        } else {
            openLatch()
        }
    }

    private fun doInitialize(configuration: DiskBufferingConfiguration.Enabled) {
        serviceManager.getBackgroundWorkService().submit {
            try {
                val diskManager = DiskManager.create(serviceManager, configuration)
                val storageConfiguration = createStorageConfiguration(diskManager, configuration)
                val spanStorage =
                    FileSpanStorage.create(
                        File(diskManager.getSignalsCacheDir(), "spans"),
                        storageConfiguration
                    )
                val logStorage = FileLogRecordStorage.create(
                    File(diskManager.getSignalsCacheDir(), "logs"),
                    storageConfiguration
                )
                val metricStorage = FileMetricStorage.create(
                    File(diskManager.getSignalsCacheDir(), "metrics"),
                    storageConfiguration
                )
                signalFromDiskExporter =
                    createFromDiskExporter(spanStorage, logStorage, metricStorage)
                toDiskSpanExporter = interceptedSpanExporter?.let {
                    SpanToDiskExporter.builder(spanStorage)
                        .setExporterCallback(
                            DiskBufferingExporterCallback(
                                "spans",
                                it::export
                            )
                        ).build()
                }
                toDiskLogRecordExporter = interceptedLogRecordExporter?.let {
                    LogRecordToDiskExporter.builder(logStorage)
                        .setExporterCallback(
                            DiskBufferingExporterCallback(
                                "logs",
                                it::export
                            )
                        ).build()
                }
                toDiskMetricExporter = interceptedMetricExporter?.let {
                    MetricToDiskExporter.builder(metricStorage)
                        .setAggregationTemporalitySelector(it::getAggregationTemporality)
                        .setExporterCallback(
                            DiskBufferingExporterCallback(
                                "metrics",
                                it::export
                            )
                        ).build()
                }
                enableDiskBuffering()
                startExportSchedule()
            } catch (e: IOException) {
                logger.error("Could not initialize disk buffering", e)
            } finally {
                openLatch()
            }
        }
    }

    private fun startExportSchedule() {
        serviceManager.getBackgroundWorkService()
            .schedulePeriodicTask(exportFromDiskIntervalMillis, TimeUnit.MILLISECONDS, 1000) {
                exportFromDisk()
            }
    }

    private fun openLatch() {
        gateManager.openLatches(DiskBufferingManager::class.java)
    }

    private fun enableDiskBuffering() {
        logger.debug("Disk buffering enabled: {}", configuration)
        toDiskSpanExporter?.let {
            spanExporter!!.setDelegate(it)
        }
        toDiskLogRecordExporter?.let {
            logRecordExporter!!.setDelegate(it)
        }
        toDiskMetricExporter?.let {
            metricExporter!!.setDelegate(it)
        }
    }

    private fun createFromDiskExporter(
        spanStorage: SignalStorage.Span,
        logStorage: SignalStorage.LogRecord,
        metricStorage: SignalStorage.Metric
    ): SignalFromDiskExporter {
        val networkExportTimeout = 10.seconds
        val builder = SignalFromDiskExporter.builder()
        interceptedSpanExporter?.let {
            builder.setSpanFromDiskExporter(
                FromDiskExporter(
                    spanStorage,
                    it::export,
                    networkExportTimeout
                )
            )
        }
        interceptedLogRecordExporter?.let {
            builder.setLogRecordFromDiskExporter(
                FromDiskExporter(
                    logStorage,
                    it::export,
                    networkExportTimeout
                )
            )
        }
        interceptedMetricExporter?.let {
            builder.setMetricFromDiskExporter(
                FromDiskExporter(
                    metricStorage,
                    it::export,
                    networkExportTimeout
                )
            )
        }

        return builder.build()
    }

    private fun createStorageConfiguration(
        diskManager: DiskManager,
        configuration: DiskBufferingConfiguration.Enabled
    ): FileStorageConfiguration {
        val builder = FileStorageConfiguration.builder()
            .setMaxFileSize(diskManager.getMaxCacheFileSize())
            .setMaxFolderSize(diskManager.getMaxFolderSize())
            .setMaxFileAgeForWriteMillis(TimeUnit.SECONDS.toMillis(2))
            .setMinFileAgeForReadMillis(TimeUnit.SECONDS.toMillis(4))

        configuration.maxFileAgeForWrite?.let {
            builder.setMaxFileAgeForWriteMillis(it)
        }
        configuration.minFileAgeForRead?.let {
            builder.setMinFileAgeForReadMillis(it)
        }

        return builder.build()
    }

    companion object {
        internal fun create(
            systemTimeProvider: SystemTimeProvider,
            serviceManager: ServiceManager,
            gateManager: ExporterGateManager,
            diskBufferingConfiguration: DiskBufferingConfiguration
        ): DiskBufferingManager {
            val latchName = "Disk buffering"
            gateManager.createSpanGateLatch(DiskBufferingManager::class.java, latchName)
            gateManager.createLogRecordLatch(DiskBufferingManager::class.java, latchName)
            gateManager.createMetricGateLatch(DiskBufferingManager::class.java, latchName)
            return DiskBufferingManager(
                systemTimeProvider,
                serviceManager,
                gateManager,
                diskBufferingConfiguration
            )
        }
    }
}