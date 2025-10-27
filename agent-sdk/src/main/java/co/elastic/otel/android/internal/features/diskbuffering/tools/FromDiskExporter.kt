package co.elastic.otel.android.internal.features.diskbuffering.tools

import androidx.annotation.GuardedBy
import io.opentelemetry.contrib.disk.buffering.storage.SignalStorage
import io.opentelemetry.sdk.common.CompletableResultCode
import java.util.concurrent.TimeUnit
import kotlin.time.Duration
import okio.Closeable

class FromDiskExporter<T>(
    private val signalStorage: SignalStorage<T>,
    private val networkExport: (MutableCollection<T>) -> CompletableResultCode,
    timeout: Duration
) : Closeable {
    @GuardedBy("nextItemLock")
    private var nextItem: MutableCollection<T>? = null
    private val nextItemLock = Any()
    private val timeoutMillis = timeout.inWholeMilliseconds

    fun exportNextBatch(): Boolean {
        getNextItem()?.let { items ->
            val result = networkExport(items).join(timeoutMillis, TimeUnit.MILLISECONDS)

            if (result.isSuccess) {
                synchronized(nextItemLock) {
                    nextItem = null
                }
                return true
            }
        }

        return false
    }

    private fun getNextItem(): MutableCollection<T>? {
        synchronized(nextItemLock) {
            if (nextItem == null) {
                val iterator = signalStorage.iterator()
                if (iterator.hasNext()) {
                    nextItem = iterator.next()
                }
            }

            return nextItem
        }
    }

    override fun close() {
        signalStorage.close()
    }
}