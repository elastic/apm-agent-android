package co.elastic.apm.android.sdk.exporters.apmserver

import co.elastic.apm.android.sdk.connectivity.ConnectivityConfigurationManager

internal class ApmServerConnectivityConfigurationManager(initialValue: ApmServerConnectivityConfiguration) :
    ConnectivityConfigurationManager(initialValue) {

    fun setConnectivityConfiguration(value: ApmServerConnectivityConfiguration) {
        set(value)
    }

    fun getConnectivityConfiguration(): ApmServerConnectivityConfiguration {
        return get() as ApmServerConnectivityConfiguration
    }
}