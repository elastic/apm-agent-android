package co.elastic.apm.android.test.common.spans.verifiers;

import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.sdk.trace.data.SpanData;

public interface SpanVerifier<T extends SpanVerifier<?>> {
    T isNamed(String spanName);

    T isDirectChildOf(SpanData span);

    T hasNoParent();

    T hasAttribute(String attributeName);

    T hasAttribute(String attributeName, String attributeValue);

    T hasAttribute(String attributeName, Long attributeValue);

    T hasAttribute(String attributeName, Integer attributeValue);

    T hasResource(String resourceName);

    T hasResource(String resourceName, String resourceValue);

    T hasResource(String resourceName, Integer resourceValue);

    T startedAt(long timeInNanoseconds);

    T isOfKind(SpanKind kind);
}
