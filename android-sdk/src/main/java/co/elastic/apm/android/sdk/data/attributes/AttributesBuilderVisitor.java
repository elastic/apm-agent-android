package co.elastic.apm.android.sdk.data.attributes;

import io.opentelemetry.api.common.AttributesBuilder;

public interface AttributesBuilderVisitor {

    void visit(AttributesBuilder builder);
}
