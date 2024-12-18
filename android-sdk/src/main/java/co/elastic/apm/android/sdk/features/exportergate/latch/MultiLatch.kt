package co.elastic.apm.android.sdk.features.exportergate.latch

internal class MultiLatch(private val latches: List<Latch>) : Latch {
    override fun open() {
        latches.forEach { it.open() }
    }
}
