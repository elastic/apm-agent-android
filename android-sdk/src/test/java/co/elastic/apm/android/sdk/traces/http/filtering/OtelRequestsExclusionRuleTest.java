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

import static org.junit.Assert.assertTrue;

import androidx.annotation.NonNull;

import org.junit.Before;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;

import co.elastic.apm.android.sdk.traces.http.data.HttpRequest;

public class OtelRequestsExclusionRuleTest {

    private OtelRequestsExclusionRule rule;

    @Before
    public void setUp() {
        rule = new OtelRequestsExclusionRule();
    }

    @Test
    public void excludeOtelSpansExports() {
        URL url = getUrl("http://10.0.2.2:8200/opentelemetry.proto.collector.trace.v1.TraceService/Export");

        assertTrue(rule.exclude(new HttpRequest(null, url)));
    }

    @Test
    public void excludeOtelMetricExports() {
        URL url = getUrl("http://10.0.2.2:8200/opentelemetry.proto.collector.metrics.v1.MetricsService/Export");

        assertTrue(rule.exclude(new HttpRequest(null, url)));
    }

    @NonNull
    private URL getUrl(String url) {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}