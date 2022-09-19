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
package co.elastic.apm.android.sdk.traces.otel.exporter;

import java.util.concurrent.TimeUnit;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.data.DelegatingSpanData;
import io.opentelemetry.sdk.trace.data.SpanData;

class TimeSkewAwareSpanData extends DelegatingSpanData {

    protected TimeSkewAwareSpanData(SpanData delegate) {
        super(delegate);
    }

    @Override
    public Resource getResource() {
        return super.getResource()
                .merge(Resource.create(Attributes.of(AttributeKey.longKey("telemetry.sdk.elastic_export_timestamp"), TimeUnit.MILLISECONDS.toNanos(System.currentTimeMillis()))));
    }
}