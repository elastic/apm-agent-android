package co.elastic.apm.android.sdk.traces.http.filtering;

import androidx.annotation.NonNull;

import co.elastic.apm.android.sdk.traces.http.data.HttpRequest;

public class OtelRequestsExclusionRule extends HttpExclusionRule {

    @Override
    public boolean exclude(@NonNull HttpRequest request) {
        return request.url.getPath().startsWith("/opentelemetry.proto.collector.trace.v1.TraceService");
    }
}
