package co.elastic.apm.android.sdk.traces.http.filtering;

import java.util.Set;

public class MultiHttpSpanRule implements HttpSpanRule {
    private final Set<HttpSpanRule> rules;

    public static MultiHttpSpanRule create(Set<HttpSpanRule> rules) {
        return new MultiHttpSpanRule(rules);
    }

    private MultiHttpSpanRule(Set<HttpSpanRule> rules) {
        this.rules = rules;
    }

    @Override
    public boolean isSpannable(HttpRequest request) {
        for (HttpSpanRule rule : rules) {
            if (!rule.isSpannable(request)) {
                return false;
            }
        }

        return true;
    }
}
