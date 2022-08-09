package co.elastic.apm.android.sdk.traces.http;

import java.util.HashSet;
import java.util.Set;

import co.elastic.apm.android.sdk.traces.http.filtering.HttpSpanRule;

public class HttpSpanConfiguration {
    public final HttpSpanRule filterRule;

    public HttpSpanConfiguration(HttpSpanRule filterRule) {
        this.filterRule = filterRule;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final Set<HttpSpanRule> filterRules = new HashSet<>();

        public Builder addFilterRule(HttpSpanRule rule) {
            filterRules.add(rule);
            return this;
        }

        public HttpSpanConfiguration build() {
            HttpSpanRule rule = (filterRules.isEmpty()) ? HttpSpanRule.allowAll() : HttpSpanRule.composite(filterRules);
            return new HttpSpanConfiguration(rule);
        }
    }
}
