package co.elastic.apm.android.sdk.features.diskbuffering

data class DiskBufferingConfiguration(val enabled: Boolean) {
    internal val maxCacheFileSize = 1024 * 1024
    internal val maxCacheSize = 30 * 1024 * 1024 // 30 MB
}