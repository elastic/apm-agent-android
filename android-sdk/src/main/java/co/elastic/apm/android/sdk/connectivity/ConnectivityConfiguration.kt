package co.elastic.apm.android.sdk.connectivity

interface ConnectivityConfiguration {
    fun getUrl(): String

    fun getHeaders(): Map<String, String>
}