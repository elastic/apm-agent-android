package co.elastic.apm.android.sdk.traces.http.filtering;

public class OtelExportExclusionRule implements HttpSpanRule {

    @Override
    public boolean isSpannable(HttpRequest request) {
        return !request.url.getPath().startsWith("/opentelemetry.proto.collector.trace.v1.TraceService");
    }
}
