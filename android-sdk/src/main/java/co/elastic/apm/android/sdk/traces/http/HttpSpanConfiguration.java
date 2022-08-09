package co.elastic.apm.android.sdk.traces.http;

import java.util.Collections;
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
        private final Set<HttpSpanRule> filterRules = new HashSet<>(Collections.singleton(HttpSpanRule.getDefault()));

        public Builder addFilterRule(HttpSpanRule rule) {
            filterRules.add(rule);
            return this;
        }

        public HttpSpanConfiguration build() {
            return new HttpSpanConfiguration(HttpSpanRule.composite(filterRules));
        }
    }
}
