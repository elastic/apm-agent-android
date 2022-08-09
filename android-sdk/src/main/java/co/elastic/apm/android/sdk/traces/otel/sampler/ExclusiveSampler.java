package co.elastic.apm.android.sdk.traces.otel.sampler;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.trace.data.LinkData;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import io.opentelemetry.sdk.trace.samplers.SamplingResult;

public class ExclusiveSampler implements Sampler {
    private final Set<Rule> exclusionRules = new HashSet<>();

    public void addRule(Rule rule) {
        exclusionRules.add(rule);
    }

    public void addAllRules(Collection<? extends Rule> rules) {
        exclusionRules.addAll(rules);
    }

    @Override
    public SamplingResult shouldSample(Context parentContext, String traceId, String name, SpanKind spanKind, Attributes attributes, List<LinkData> parentLinks) {
        if (shouldExclude(name, spanKind, attributes)) {
            return SamplingResult.drop();
        }
        return SamplingResult.recordAndSample();
    }

    private boolean shouldExclude(String name, SpanKind spanKind, Attributes attributes) {
        for (Rule rule : exclusionRules) {
            if (rule.exclude(name, spanKind, attributes)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getDescription() {
        return "Excludes spans based on registered exclusion rules";
    }

    public interface Rule {
        boolean exclude(String spanName, SpanKind kind, Attributes attributes);
    }
}
