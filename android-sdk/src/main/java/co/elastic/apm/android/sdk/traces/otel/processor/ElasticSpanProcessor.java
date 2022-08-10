package co.elastic.apm.android.sdk.traces.otel.processor;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.trace.ReadWriteSpan;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SpanProcessor;

public class ElasticSpanProcessor implements SpanProcessor {
    private final SpanProcessor original;
    private final Set<ExclusionRule> rules = new HashSet<>();

    public void addAllExclusionRules(Collection<? extends ExclusionRule> rules) {
        this.rules.addAll(rules);
    }

    public ElasticSpanProcessor(SpanProcessor original) {
        this.original = original;
    }

    @Override
    public void onStart(Context parentContext, ReadWriteSpan span) {
        original.onStart(parentContext, span);
    }

    @Override
    public boolean isStartRequired() {
        return original.isStartRequired();
    }

    @Override
    public void onEnd(ReadableSpan span) {
        if (shouldExclude(span)) {
            return;
        }
        original.onEnd(span);
    }

    @Override
    public boolean isEndRequired() {
        return true;
    }

    private boolean shouldExclude(ReadableSpan span) {
        for (ExclusionRule rule : rules) {
            if (rule.exclude(span)) {
                return true;
            }
        }
        return false;
    }

    public interface ExclusionRule {
        boolean exclude(ReadableSpan span);
    }
}
