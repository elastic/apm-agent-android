package co.elastic.apm.android.sdk.connectivity.opentelemetry.exporters;

public interface ExporterVisitor {
    <T> T visitExporter(T exporter);
}
