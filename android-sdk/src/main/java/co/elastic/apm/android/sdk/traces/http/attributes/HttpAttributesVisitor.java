package co.elastic.apm.android.sdk.traces.http.attributes;

import co.elastic.apm.android.sdk.traces.http.data.HttpRequest;
import io.opentelemetry.api.common.AttributesBuilder;

public interface HttpAttributesVisitor {

    void visit(AttributesBuilder builder, HttpRequest request);
}
