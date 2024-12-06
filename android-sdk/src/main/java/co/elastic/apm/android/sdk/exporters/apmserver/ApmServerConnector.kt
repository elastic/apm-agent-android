package co.elastic.apm.android.sdk.exporters.apmserver

class ApmServerConnector internal constructor(
    val connectivityConfigurationManager: ApmServerConnectivityConfigurationManager,
    val exporterProvider: ApmServerExporterProvider
) {
}