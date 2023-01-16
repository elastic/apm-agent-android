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
package co.elastic.apm.android.sdk.attributes.resources;

import co.elastic.apm.android.sdk.ElasticApmAgent;
import co.elastic.apm.android.sdk.ElasticApmConfiguration;
import co.elastic.apm.android.sdk.attributes.AttributesVisitor;
import co.elastic.apm.android.sdk.internal.services.Service;
import co.elastic.apm.android.sdk.internal.services.metadata.ApmMetadataService;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;

public class ServiceIdVisitor implements AttributesVisitor {
    private final String serviceName;
    private final String serviceVersion;

    public ServiceIdVisitor() {
        ElasticApmConfiguration configuration = ElasticApmAgent.get().configuration;
        this.serviceName = configuration.serviceName;
        this.serviceVersion = configuration.serviceVersion;
    }

    @Override
    public void visit(AttributesBuilder builder) {
        ApmMetadataService metadata = getApmMetadataService();
        String serviceName = (this.serviceName != null) ? this.serviceName : metadata.getServiceName();
        String serviceVersion = (this.serviceVersion != null) ? this.serviceVersion : metadata.getServiceVersion();
        builder.put(ResourceAttributes.SERVICE_NAME, serviceName)
                .put(ResourceAttributes.SERVICE_VERSION, serviceVersion)
                .put(ResourceAttributes.DEPLOYMENT_ENVIRONMENT, metadata.getDeploymentEnvironment());
    }

    private ApmMetadataService getApmMetadataService() {
        return ElasticApmAgent.get().getService(Service.Names.METADATA);
    }
}
