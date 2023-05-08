/*
 * Licensed to Elasticsearch B.V. under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch B.V. licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package co.elastic.apm.android.sdk.traces.http;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import co.elastic.apm.android.sdk.attributes.AttributesVisitor;
import co.elastic.apm.android.sdk.traces.http.attributes.HttpAttributesVisitor;
import co.elastic.apm.android.sdk.traces.http.attributes.HttpAttributesVisitorWrapper;
import co.elastic.apm.android.sdk.traces.http.attributes.visitors.BasicHttpAttributesVisitor;
import co.elastic.apm.android.sdk.traces.http.data.HttpRequest;
import co.elastic.apm.android.sdk.traces.http.filtering.HttpFilter;

public class HttpTraceConfiguration {
    public final Collection<HttpFilter> httpFilters;
    private final Collection<HttpAttributesVisitor> httpAttributesVisitors;

    private HttpTraceConfiguration(Builder builder) {
        httpFilters = Collections.unmodifiableCollection(builder.filters);
        httpAttributesVisitors = Collections.unmodifiableCollection(builder.httpAttributesVisitors);
    }

    public AttributesVisitor createHttpAttributesVisitor(HttpRequest request) {
        List<AttributesVisitor> visitors = new ArrayList<>();

        for (HttpAttributesVisitor httpVisitor : httpAttributesVisitors) {
            visitors.add(new HttpAttributesVisitorWrapper(request, httpVisitor));
        }

        return AttributesVisitor.compose(visitors);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final Set<HttpFilter> filters = new HashSet<>();
        private final Set<HttpAttributesVisitor> httpAttributesVisitors = new HashSet<>();

        private Builder() {
            httpAttributesVisitors.add(new BasicHttpAttributesVisitor());
        }

        public Builder addFilter(HttpFilter rule) {
            filters.add(rule);
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
