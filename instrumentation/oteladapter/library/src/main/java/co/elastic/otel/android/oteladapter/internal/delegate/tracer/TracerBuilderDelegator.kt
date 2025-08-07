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

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
class TracerBuilderDelegator(
    initialValue: TracerBuilder,
    private val references: MultipleReference<Tracer>
) : Delegator<TracerBuilder>(initialValue), TracerBuilder {

    override fun setSchemaUrl(schemaUrl: String): TracerBuilder? {
        return getDelegate().setSchemaUrl(schemaUrl)
    }

    override fun setInstrumentationVersion(instrumentationScopeVersion: String): TracerBuilder? {
        return getDelegate().setInstrumentationVersion(instrumentationScopeVersion)
    }

    override fun build(): Tracer? {
        return references.maybeAdd(getDelegate().build())
    }

    override fun getNoopValue(): TracerBuilder {
        return NOOP_INSTANCE
    }

    companion object {
        val NOOP_INSTANCE: TracerBuilder = NoopTracerBuilder()
    }

    private class NoopTracerBuilder : TracerBuilder {
        override fun setSchemaUrl(schemaUrl: String): TracerBuilder? {
            return this
        }

        override fun setInstrumentationVersion(instrumentationScopeVersion: String): TracerBuilder? {
            return this
        }

        override fun build(): Tracer? {
            return TracerDelegator.NoopTracer.INSTANCE
        }
    }
}