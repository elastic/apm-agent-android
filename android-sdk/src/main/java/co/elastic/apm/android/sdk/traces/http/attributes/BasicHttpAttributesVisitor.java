package co.elastic.apm.android.sdk.traces.http.attributes;

import co.elastic.apm.android.sdk.traces.http.data.HttpRequest;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.semconv.trace.attributes.SemanticAttributes;

public class BasicHttpAttributesVisitor implements HttpAttributesVisitor {

    @Override
    public void visit(AttributesBuilder builder, HttpRequest request) {
        builder.put(SemanticAttributes.HTTP_URL, request.url.toString())
                .put(SemanticAttributes.HTTP_METHOD, request.method);
    }
}
