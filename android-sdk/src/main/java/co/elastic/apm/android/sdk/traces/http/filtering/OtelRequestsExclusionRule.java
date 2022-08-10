package co.elastic.apm.android.sdk.traces.http.filtering;

import androidx.annotation.NonNull;

public class OtelRequestsExclusionRule extends HttpExclusionRule {

    @Override
    public boolean exclude(@NonNull Request request) {
        return request.url.getPath().startsWith("/opentelemetry.proto.collector.trace.v1.TraceService");
    }
}
