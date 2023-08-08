package co.elastic.apm.android.sdk.connectivity.opentelemetry.exporters;

public interface VisitableExporters {
    void setExporterVisitor(ExporterVisitor visitor);
}
