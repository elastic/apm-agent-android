package co.elastic.otel.android.internal.features.diskbuffering.tools

import co.elastic.otel.android.common.internal.logging.Elog
import io.opentelemetry.contrib.disk.buffering.exporters.callback.ExporterCallback
import io.opentelemetry.sdk.common.CompletableResultCode

class DiskBufferingExporterCallback<T>(
    private val signalId: String,
    private val networkExport: (MutableCollection<T>) -> CompletableResultCode
) : ExporterCallback<T> {
    private val logger = Elog.getLogger("disk_buffering_callback")

    override fun onExportSuccess(items: Collection<T>) {
        logger.debug("'$signalId' signals successfully stored in disk")
    }

    override fun onExportError(
        items: Collection<T>,
        error: Throwable?
    ) {
        logger.error(
            "'$signalId' signals failed to store in disk. Attempting to export right away.",
            error
        )
        networkExport.invoke(items as MutableCollection<T>)
    }

    override fun onShutdown() {
        logger.debug("'$signalId' signals disk buffer exporter closed")
    }
}