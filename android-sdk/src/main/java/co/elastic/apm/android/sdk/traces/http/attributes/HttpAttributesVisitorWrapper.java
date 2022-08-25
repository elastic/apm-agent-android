package co.elastic.apm.android.sdk.traces.http.attributes;

import co.elastic.apm.android.sdk.attributes.AttributesBuilderVisitor;
import co.elastic.apm.android.sdk.traces.http.data.HttpRequest;
import io.opentelemetry.api.common.AttributesBuilder;

public class HttpAttributesVisitorWrapper implements AttributesBuilderVisitor {
    private final HttpRequest request;
    private final HttpAttributesVisitor visitor;

    public HttpAttributesVisitorWrapper(HttpRequest request, HttpAttributesVisitor visitor) {
        this.request = request;
        this.visitor = visitor;
    }

    @Override
    public void visit(AttributesBuilder builder) {
        visitor.visit(builder, request);
    }
}
