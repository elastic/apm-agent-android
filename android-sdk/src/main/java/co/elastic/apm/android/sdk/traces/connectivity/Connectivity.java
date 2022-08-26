package co.elastic.apm.android.sdk.traces.connectivity;

import co.elastic.apm.android.sdk.traces.otel.exporter.provider.ExporterProvider;

public interface Connectivity {

    static Connectivity simple(String endpoint) {
        return new SimpleConnectivity(endpoint);
    }

    static Connectivity token(String endpoint, String token) {
        return new BearerConnectivity(endpoint, token);
    }

    static Connectivity custom(ExporterProvider exporterProvider) {
        return new CustomConnectivity(exporterProvider);
    }

    ExporterProvider getExporterProvider();
}
