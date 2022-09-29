package co.elastic.apm.android.sdk.traces.common.rules;

import co.elastic.apm.android.sdk.traces.otel.processor.ElasticSpanProcessor;
import io.opentelemetry.sdk.trace.ReadableSpan;

public class DiscardExclusionRule implements ElasticSpanProcessor.ExclusionRule {
    public static final String DISCARDED_NAME = "<discard>";

    @Override
    public boolean exclude(ReadableSpan span) {
        return span.getName().equals(DISCARDED_NAME);
    }
}
