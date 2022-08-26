package co.elastic.apm.android.sdk.traces.connectivity;

import co.elastic.apm.android.sdk.traces.otel.exporter.provider.ExporterProvider;

public class CustomConnectivity implements Connectivity {
    private final ExporterProvider exporterProvider;

    public CustomConnectivity(ExporterProvider exporterProvider) {
        this.exporterProvider = exporterProvider;
    }

    @Override
    public ExporterProvider getExporterProvider() {
        return exporterProvider;
    }
}
