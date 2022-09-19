/* 
Licensed to Elasticsearch B.V. under one or more contributor
license agreements. See the NOTICE file distributed with
this work for additional information regarding copyright
ownership. Elasticsearch B.V. licenses this file to you under
the Apache License, Version 2.0 (the "License"); you may
not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License. 
*/
package co.elastic.apm.android.sdk.traces.http;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import co.elastic.apm.android.sdk.attributes.AttributesBuilderVisitor;
import co.elastic.apm.android.sdk.attributes.AttributesCompose;
import co.elastic.apm.android.sdk.traces.http.attributes.HttpAttributesVisitor;
import co.elastic.apm.android.sdk.traces.http.attributes.HttpAttributesVisitorWrapper;
import co.elastic.apm.android.sdk.traces.http.attributes.visitors.BasicHttpAttributesVisitor;
import co.elastic.apm.android.sdk.traces.http.attributes.visitors.CarrierHttpAttributes;
import co.elastic.apm.android.sdk.traces.http.attributes.visitors.ConnectionHttpAttributes;
import co.elastic.apm.android.sdk.traces.http.data.HttpRequest;
import co.elastic.apm.android.sdk.traces.http.filtering.HttpExclusionRule;
import co.elastic.apm.android.sdk.traces.http.filtering.OtelRequestsExclusionRule;

public class HttpTraceConfiguration {
    public final Collection<HttpExclusionRule> exclusionRules;
    private final Collection<HttpAttributesVisitor> httpAttributesVisitors;

    private HttpTraceConfiguration(Builder builder) {
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

        public HttpTraceConfiguration build() {
            return new HttpTraceConfiguration(this);
        }
    }
}
