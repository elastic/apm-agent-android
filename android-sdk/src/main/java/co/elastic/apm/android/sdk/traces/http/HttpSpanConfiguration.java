package co.elastic.apm.android.sdk.traces.http;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import co.elastic.apm.android.sdk.attributes.AttributesCompose;
import co.elastic.apm.android.sdk.traces.http.filtering.HttpExclusionRule;
import co.elastic.apm.android.sdk.traces.http.filtering.OtelRequestsExclusionRule;

public class HttpSpanConfiguration {
    public final List<HttpExclusionRule> exclusionRules;
    public final AttributesCompose httpAttributes;

    private HttpSpanConfiguration(Builder builder) {
        exclusionRules = new ArrayList<>(builder.exclusionRules);
        httpAttributes = builder.httpAttributes;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final Set<HttpExclusionRule> exclusionRules = new HashSet<>(Collections.singleton(new OtelRequestsExclusionRule()));
        private final AttributesCompose httpAttributes;

        private Builder() {
            httpAttributes = null;
        }

        public Builder addExclusionRule(HttpExclusionRule rule) {
            exclusionRules.add(rule);
            return this;
        }

        public HttpSpanConfiguration build() {
            return new HttpSpanConfiguration(this);
        }
    }
}
