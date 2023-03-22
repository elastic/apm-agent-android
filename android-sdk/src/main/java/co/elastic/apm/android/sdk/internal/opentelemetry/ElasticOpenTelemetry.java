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
package co.elastic.apm.android.sdk.internal.opentelemetry;

import co.elastic.apm.android.sdk.internal.configuration.Configurations;
import co.elastic.apm.android.sdk.internal.configuration.impl.AllInstrumentationConfiguration;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.api.trace.TracerProvider;
import io.opentelemetry.context.propagation.ContextPropagators;

public class ElasticOpenTelemetry implements OpenTelemetry {
    private final OpenTelemetry wrapped;

    public ElasticOpenTelemetry(OpenTelemetry wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public TracerProvider getTracerProvider() {
        if (instrumentationIsNotEnabled()) {
            return TracerProvider.noop();
        }
        return wrapped.getTracerProvider();
    }

    @Override
    public MeterProvider getMeterProvider() {
        if (instrumentationIsNotEnabled()) {
            return MeterProvider.noop();
        }
        return wrapped.getMeterProvider();
    }

    @Override
    public ContextPropagators getPropagators() {
        return wrapped.getPropagators();
    }

    private boolean instrumentationIsNotEnabled() {
        return !Configurations.get(AllInstrumentationConfiguration.class).isEnabled();
    }
}
