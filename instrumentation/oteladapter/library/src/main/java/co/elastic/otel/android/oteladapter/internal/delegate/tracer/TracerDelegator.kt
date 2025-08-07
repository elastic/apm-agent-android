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
import co.elastic.otel.android.oteladapter.internal.delegate.tracer.span.NoopSpanBuilder
import co.elastic.otel.android.oteladapter.internal.delegate.tracer.span.SpanBuilderDelegator
import io.opentelemetry.api.trace.SpanBuilder
import io.opentelemetry.api.trace.Tracer

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
class TracerDelegator(initialValue: Tracer) : Delegator<Tracer>(initialValue), Tracer {
    private val spanBuilders = MultipleReference<SpanBuilder>(NoopSpanBuilder.INSTANCE) {
        SpanBuilderDelegator(it)
    }

    override fun spanBuilder(spanName: String): SpanBuilder? {
        return spanBuilders.maybeAdd(getDelegate().spanBuilder(spanName))
    }

    override fun reset() {
        super.reset()
        spanBuilders.reset()
    }

    override fun getNoopValue(): Tracer {
        return NoopTracer.INSTANCE
    }

    class NoopTracer private constructor() : Tracer {

        override fun spanBuilder(spanName: String): SpanBuilder? {
            return NoopSpanBuilder.INSTANCE
        }

        companion object {
            val INSTANCE = NoopTracer()
        }
    }
}