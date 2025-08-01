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
import co.elastic.otel.android.internal.features.diskbuffering.tools.DiskManager
import co.elastic.otel.android.internal.features.diskbuffering.tools.ExtendedDelegateLogRecordExporter
import co.elastic.otel.android.internal.features.exportergate.ExporterGateManager
import co.elastic.otel.android.internal.services.ServiceManager
import co.elastic.otel.android.internal.time.SystemTimeProvider
import io.opentelemetry.contrib.disk.buffering.LogRecordFromDiskExporter
import io.opentelemetry.contrib.disk.buffering.LogRecordToDiskExporter
import io.opentelemetry.contrib.disk.buffering.MetricFromDiskExporter
import io.opentelemetry.contrib.disk.buffering.MetricToDiskExporter
import io.opentelemetry.contrib.disk.buffering.SpanFromDiskExporter
import io.opentelemetry.contrib.disk.buffering.SpanToDiskExporter
import io.opentelemetry.contrib.disk.buffering.config.StorageConfiguration
import io.opentelemetry.contrib.disk.buffering.internal.storage.Storage
import io.opentelemetry.contrib.disk.buffering.internal.utils.SignalTypes
import io.opentelemetry.sdk.logs.export.LogRecordExporter
import io.opentelemetry.sdk.metrics.export.MetricExporter
import io.opentelemetry.sdk.trace.export.SpanExporter
import java.io.IOException
import java.util.concurrent.TimeUnit

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
                val storageConfiguration = createStorageConfiguration(configuration)
                val spanStorage = Storage.builder(SignalTypes.spans)
                    .setStorageConfiguration(storageConfiguration)
                    .build()
                val logStorage = Storage.builder(SignalTypes.logs)
                    .setStorageConfiguration(storageConfiguration)
                    .build()
                val metricStorage = Storage.builder(SignalTypes.metrics)
                    .setStorageConfiguration(storageConfiguration)
                    .build()
                signalFromDiskExporter =
                    createFromDiskExporter(spanStorage, logStorage, metricStorage)
                toDiskSpanExporter = interceptedSpanExporter?.let {
                    SpanToDiskExporter.create(it, spanStorage)
                }
                toDiskLogRecordExporter = interceptedLogRecordExporter?.let {
                    LogRecordToDiskExporter.create(it, logStorage)
                }
                toDiskMetricExporter = interceptedMetricExporter?.let {
                    MetricToDiskExporter.create(it, metricStorage)
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
        spanStorage: Storage,
        logStorage: Storage,
        metricStorage: Storage
    ): SignalFromDiskExporter {
        val builder =
            SignalFromDiskExporter.builder()
        interceptedSpanExporter?.let {
            builder.setSpanFromDiskExporter(SpanFromDiskExporter.create(it, spanStorage))
        }
        interceptedLogRecordExporter?.let {
            builder.setLogRecordFromDiskExporter(
                LogRecordFromDiskExporter.create(
                    ExtendedDelegateLogRecordExporter(it),
                    logStorage
                )
            )
        }
        interceptedMetricExporter?.let {
            builder.setMetricFromDiskExporter(
                MetricFromDiskExporter.create(it, metricStorage)
            )
        }

        return builder.build()
    }

    private fun createStorageConfiguration(configuration: DiskBufferingConfiguration.Enabled): StorageConfiguration {
        val diskManager = DiskManager.create(serviceManager, configuration)
        val builder = StorageConfiguration.builder()
            .setMaxFileSize(diskManager.getMaxCacheFileSize())
            .setMaxFolderSize(diskManager.getMaxFolderSize())
            .setMaxFileAgeForWriteMillis(TimeUnit.SECONDS.toMillis(2))
            .setMinFileAgeForReadMillis(TimeUnit.SECONDS.toMillis(4))
            .setRootDir(diskManager.getSignalsCacheDir())

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