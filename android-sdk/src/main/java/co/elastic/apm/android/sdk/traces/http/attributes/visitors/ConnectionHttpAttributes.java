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
package co.elastic.apm.android.sdk.traces.http.attributes.visitors;

import co.elastic.apm.android.sdk.ElasticApmAgent;
import co.elastic.apm.android.sdk.providers.LazyProvider;
import co.elastic.apm.android.sdk.internal.services.Service;
import co.elastic.apm.android.sdk.internal.services.network.NetworkService;
import co.elastic.apm.android.sdk.internal.services.network.data.type.NetworkType;
import co.elastic.apm.android.sdk.traces.http.attributes.HttpAttributesVisitor;
import co.elastic.apm.android.sdk.traces.http.data.HttpRequest;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.semconv.trace.attributes.SemanticAttributes;

public class ConnectionHttpAttributes implements HttpAttributesVisitor {
    private final LazyProvider<NetworkService> networkServiceProvider;

    public ConnectionHttpAttributes() {
        networkServiceProvider = LazyProvider.of(() -> ElasticApmAgent.get().getService(Service.Names.NETWORK));
    }

    @Override
    public void visit(AttributesBuilder builder, HttpRequest request) {
        NetworkType networkType = networkServiceProvider.get().getType();
        builder.put(SemanticAttributes.NET_HOST_CONNECTION_TYPE, networkType.getName());
        if (networkType.getSubTypeName() != null) {
            builder.put(SemanticAttributes.NET_HOST_CONNECTION_SUBTYPE, networkType.getSubTypeName());
        }
    }
}
