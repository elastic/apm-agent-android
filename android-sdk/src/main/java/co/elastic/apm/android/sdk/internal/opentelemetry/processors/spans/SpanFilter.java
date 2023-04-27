package co.elastic.apm.android.sdk.internal.opentelemetry.processors.spans;

import co.elastic.apm.android.sdk.internal.opentelemetry.processors.common.Filter;
import io.opentelemetry.sdk.trace.ReadableSpan;

public interface SpanFilter extends Filter<ReadableSpan> {
}
