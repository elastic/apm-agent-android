package co.elastic.apm.android.test.common.spans.verifiers;

import io.opentelemetry.sdk.trace.data.SpanData;

public interface SpanVerifier<T extends SpanVerifier<?>> {
    T isNamed(String spanName);

    T isDirectChildOf(SpanData span);

    T hasNoParent();

    T hasAttributeNamed(String attributeName);
}
