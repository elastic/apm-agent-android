package co.elastic.apm.android.sdk.exporters.apmserver

import co.elastic.apm.android.sdk.connectivity.ConnectivityConfiguration
import co.elastic.apm.android.sdk.exporters.configuration.ExportProtocol

data class ApmServerConnectivityConfiguration(
    val url: String,
    val auth: Auth = Auth.None,
    val extraHeaders: Map<String, String> = emptyMap(),
    val exportProtocol: ExportProtocol = ExportProtocol.HTTP
) : ConnectivityConfiguration {
    private val baseUrl by lazy { url.trimEnd('/') }

    override fun getUrl(): String = url

    override fun getHeaders(): Map<String, String> {
        val headers = mutableMapOf<String, String>()
        headers.putAll(extraHeaders)
        if (auth is Auth.SecretToken) {
            headers[AUTHORIZATION_HEADER_KEY] = "Bearer ${auth.token}"
        } else if (auth is Auth.ApiKey) {
            headers[AUTHORIZATION_HEADER_KEY] = "ApiKey ${auth.key}"
        }
        return headers
    }

    fun getTracesUrl(): String {
        return getSignalUrl(baseUrl, "traces", exportProtocol)
    }

    fun getLogsUrl(): String {
        return getSignalUrl(baseUrl, "logs", exportProtocol)
    }

    fun getMetricsUrl(): String {
        return getSignalUrl(baseUrl, "metrics", exportProtocol)
    }

    private fun getSignalUrl(
        baseUrl: String,
        signalId: String,
        exportProtocol: ExportProtocol
    ): String {
        return when (exportProtocol) {
            ExportProtocol.GRPC -> baseUrl
            ExportProtocol.HTTP -> getHttpUrl(baseUrl, signalId)
        }
    }

    private fun getHttpUrl(url: String, signalId: String): String {
        return String.format("%s/v1/%s", url, signalId)
    }

    sealed class Auth {
        data class ApiKey(val key: String) : Auth()
        data class SecretToken(val token: String) : Auth()
        data object None : Auth()
    }

    companion object {
        private const val AUTHORIZATION_HEADER_KEY = "Authorization"
    }
}