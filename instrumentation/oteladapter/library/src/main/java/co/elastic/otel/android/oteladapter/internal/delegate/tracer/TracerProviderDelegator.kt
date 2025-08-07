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
package co.elastic.otel.android.oteladapter.internal.delegate.tracer

import co.elastic.otel.android.oteladapter.internal.delegate.tools.Delegator
import co.elastic.otel.android.oteladapter.internal.delegate.tools.MultipleReference
import io.opentelemetry.api.trace.Tracer
import io.opentelemetry.api.trace.TracerBuilder
import io.opentelemetry.api.trace.TracerProvider

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
class TracerProviderDelegator(initialValue: TracerProvider) :
    Delegator<TracerProvider>(initialValue), TracerProvider {
    private val tracerReferences = MultipleReference<Tracer>(TracerDelegator.NoopTracer.INSTANCE) {
        TracerDelegator(it)
    }
    private val tracerBuilderReferences = MultipleReference(TracerBuilderDelegator.NOOP_INSTANCE) {
        TracerBuilderDelegator(it, tracerReferences)
    }

    override fun get(instrumentationScopeName: String): Tracer? {
        return tracerReferences.maybeAdd(getDelegate().get(instrumentationScopeName))
    }

    override fun get(
        instrumentationScopeName: String,
        instrumentationScopeVersion: String
    ): Tracer? {
        return tracerReferences.maybeAdd(
            getDelegate().get(
                instrumentationScopeName,
                instrumentationScopeVersion
            )
        )
    }

    override fun tracerBuilder(instrumentationScopeName: String): TracerBuilder? {
        return tracerBuilderReferences.maybeAdd(getDelegate().tracerBuilder(instrumentationScopeName))
    }

    override fun getNoopValue(): TracerProvider {
        return NOOP_INSTANCE
    }

    override fun reset() {
        super.reset()
        tracerReferences.reset()
        tracerBuilderReferences.reset()
    }

    companion object {
        private val NOOP_INSTANCE = NoopTracerProvider()
    }

    private class NoopTracerProvider : TracerProvider {
        override fun get(instrumentationScopeName: String): Tracer? {
            return TracerDelegator.NoopTracer.INSTANCE
        }

        override fun get(
            instrumentationScopeName: String,
            instrumentationScopeVersion: String
        ): Tracer? {
            return TracerDelegator.NoopTracer.INSTANCE
        }
    }
}