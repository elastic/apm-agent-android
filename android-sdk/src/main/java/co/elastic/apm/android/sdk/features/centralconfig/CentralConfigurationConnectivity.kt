package co.elastic.apm.android.sdk.features.centralconfig

import co.elastic.apm.android.sdk.connectivity.ConnectivityConfiguration
import co.elastic.apm.android.sdk.exporters.apmserver.ApmServerConnectivity

data class CentralConfigurationConnectivity(
    val apmServerUrl: String,
    val serviceName: String,
    val serviceDeployment: String?,
    val headers: Map<String, String>
) : ConnectivityConfiguration {
    private val baseUrl by lazy { apmServerUrl.trimEnd('/') + "/config/v1/agents?service.name=$serviceName" }

    companion object {
        fun fromApmServerConfig(
            serviceName: String,
            serviceDeployment: String?,
            configuration: ApmServerConnectivity
        ): CentralConfigurationConnectivity {
            return CentralConfigurationConnectivity(
                configuration.getUrl(), serviceName, serviceDeployment,
                configuration.getHeaders()
            )
        }
    }

    override fun getUrl(): String {
        return when (serviceDeployment) {
            null -> baseUrl
            else -> "$baseUrl&service.deployment=$serviceDeployment"
        }
    }

    override fun getHeaders(): Map<String, String> {
        return headers
    }
}