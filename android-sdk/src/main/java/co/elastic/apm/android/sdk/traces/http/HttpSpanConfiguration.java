package co.elastic.apm.android.sdk.traces.http;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import co.elastic.apm.android.sdk.traces.http.filtering.HttpExclusionRule;
import co.elastic.apm.android.sdk.traces.http.filtering.OtelRequestsExclusionRule;

public class HttpSpanConfiguration {
    public final List<HttpExclusionRule> exclusionRules;

    private HttpSpanConfiguration(List<HttpExclusionRule> exclusionRule) {
        this.exclusionRules = exclusionRule;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final Set<HttpExclusionRule> exclusionRules = new HashSet<>(Collections.singleton(new OtelRequestsExclusionRule()));

        private Builder() {
        }

        public Builder addExclusionRule(HttpExclusionRule rule) {
            exclusionRules.add(rule);
            return this;
        }

        public HttpSpanConfiguration build() {
            return new HttpSpanConfiguration(new ArrayList<>(exclusionRules));
        }
    }
}
