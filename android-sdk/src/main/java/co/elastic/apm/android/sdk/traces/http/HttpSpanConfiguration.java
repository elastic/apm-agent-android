package co.elastic.apm.android.sdk.traces.http;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import co.elastic.apm.android.sdk.attributes.AttributesBuilderVisitor;
import co.elastic.apm.android.sdk.attributes.AttributesCompose;
import co.elastic.apm.android.sdk.traces.http.attributes.BasicHttpAttributesVisitor;
import co.elastic.apm.android.sdk.traces.http.attributes.CarrierHttpAttributes;
import co.elastic.apm.android.sdk.traces.http.attributes.ConnectionHttpAttributes;
import co.elastic.apm.android.sdk.traces.http.attributes.HttpAttributesVisitor;
import co.elastic.apm.android.sdk.traces.http.attributes.HttpAttributesVisitorWrapper;
import co.elastic.apm.android.sdk.traces.http.data.HttpRequest;
import co.elastic.apm.android.sdk.traces.http.filtering.HttpExclusionRule;
import co.elastic.apm.android.sdk.traces.http.filtering.OtelRequestsExclusionRule;

public class HttpSpanConfiguration {
    public final Collection<HttpExclusionRule> exclusionRules;
    private final Collection<HttpAttributesVisitor> httpAttributesVisitors;

    private HttpSpanConfiguration(Builder builder) {
        exclusionRules = Collections.unmodifiableCollection(builder.exclusionRules);
        httpAttributesVisitors = Collections.unmodifiableCollection(builder.httpAttributesVisitors);
    }

    public AttributesCompose createHttpAttributesCompose(HttpRequest request) {
        List<AttributesBuilderVisitor> visitors = new ArrayList<>();

        for (HttpAttributesVisitor httpVisitor : httpAttributesVisitors) {
            visitors.add(new HttpAttributesVisitorWrapper(request, httpVisitor));
        }

        return new AttributesCompose(visitors);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final Set<HttpExclusionRule> exclusionRules = new HashSet<>();
        private final Set<HttpAttributesVisitor> httpAttributesVisitors = new HashSet<>();

        private Builder() {
            exclusionRules.add(new OtelRequestsExclusionRule());
            httpAttributesVisitors.add(new BasicHttpAttributesVisitor());
            httpAttributesVisitors.add(new CarrierHttpAttributes());
            httpAttributesVisitors.add(new ConnectionHttpAttributes());
        }

        public Builder addExclusionRule(HttpExclusionRule rule) {
            exclusionRules.add(rule);
            return this;
        }

        public Builder addHttpAttributesVisitor(HttpAttributesVisitor visitor) {
            httpAttributesVisitors.add(visitor);
            return this;
        }

        public HttpSpanConfiguration build() {
            return new HttpSpanConfiguration(this);
        }
    }
}
