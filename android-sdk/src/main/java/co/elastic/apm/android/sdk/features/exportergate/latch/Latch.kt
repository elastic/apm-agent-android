package co.elastic.apm.android.sdk.features.exportergate.latch

internal interface Latch {

    companion object {
        internal fun composite(vararg latches: Latch): Latch {
            return MultiLatch(latches.toList())
        }
    }

    fun open()
}
