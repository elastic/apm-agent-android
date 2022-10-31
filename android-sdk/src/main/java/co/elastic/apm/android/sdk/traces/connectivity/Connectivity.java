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
package co.elastic.apm.android.sdk.traces.connectivity;

import co.elastic.apm.android.sdk.ElasticApmAgent;
import co.elastic.apm.android.sdk.internal.services.Service;
import co.elastic.apm.android.sdk.internal.services.metadata.ApmMetadataService;
import co.elastic.apm.android.sdk.providers.LazyProvider;
import co.elastic.apm.android.sdk.providers.Provider;
import co.elastic.apm.android.sdk.traces.connectivity.custom.CustomExporterConnectivity;
import co.elastic.apm.android.sdk.traces.connectivity.custom.CustomProcessorConnectivity;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;

public interface Connectivity {

    static CommonConnectivity create(String endpoint) {
        return new CommonConnectivity(endpoint);
    }

    static Connectivity custom(SpanExporter exporter) {
        return new CustomExporterConnectivity(exporter);
    }

    static Connectivity custom(SpanProcessor processor) {
        return new CustomProcessorConnectivity(processor);
    }

    static Provider<Connectivity> getDefault() {
        return LazyProvider.of(() -> {
            ApmMetadataService service = ElasticApmAgent.get().getService(Service.Names.METADATA);
            CommonConnectivity connectivity = Connectivity.create(service.getServerUrl());
            String secretToken = service.getSecretToken();
            if (secretToken != null) {
                connectivity.withSecretToken(secretToken);
            }
            return connectivity;
        });
    }

    SpanProcessor getSpanProcessor();
}
