package co.elastic.apm.android.sdk.traces.http.filtering;

public class OtelRequestsExclusionRule extends HttpExclusionRule {

    @Override
    boolean exclude(Request request) {
        return request.url.getPath().startsWith("/opentelemetry.proto.collector.trace.v1.TraceService");
    }
}
