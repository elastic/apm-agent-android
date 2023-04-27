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
package co.elastic.apm.android.sdk.traces.http.filtering;

import androidx.annotation.NonNull;

import java.net.MalformedURLException;
import java.net.URL;

import co.elastic.apm.android.sdk.internal.opentelemetry.processors.spans.SpanFilter;
import co.elastic.apm.android.sdk.traces.http.data.HttpRequest;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.semconv.trace.attributes.SemanticAttributes;

abstract public class HttpFilter implements SpanFilter {

    @Override
    public boolean shouldInclude(ReadableSpan item) {
        String httpMethod = item.getAttribute(SemanticAttributes.HTTP_METHOD);
        if (httpMethod == null) {
            // Not an http-related Span.
            return false;
        }

        return shouldInclude(new HttpRequest(httpMethod, getUrl(item)));
    }

    private URL getUrl(ReadableSpan span) {
        String urlString = span.getAttribute(SemanticAttributes.HTTP_URL);
        try {
            return new URL(urlString);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public abstract boolean shouldInclude(@NonNull HttpRequest request);
}