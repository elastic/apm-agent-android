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
package co.elastic.otel.android.oteladapter.internal.delegate.context

import co.elastic.otel.android.oteladapter.internal.delegate.tools.Delegator
import io.opentelemetry.context.Context
import io.opentelemetry.context.propagation.TextMapGetter
import io.opentelemetry.context.propagation.TextMapPropagator
import io.opentelemetry.context.propagation.TextMapSetter

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
internal class TextMapPropagatorDelegate(initialValue: TextMapPropagator) :
    Delegator<TextMapPropagator>(initialValue), TextMapPropagator {

    override fun fields(): Collection<String?>? {
        return getDelegate().fields()
    }

    override fun <C : Any?> inject(
        context: Context,
        carrier: C?,
        setter: TextMapSetter<C?>
    ) {
        getDelegate().inject(context, carrier, setter)
    }

    override fun <C : Any?> extract(
        context: Context,
        carrier: C?,
        getter: TextMapGetter<C?>
    ): Context? {
        return getDelegate().extract(context, carrier, getter)
    }

    override fun getNoopValue(): TextMapPropagator {
        return NOOP_INSTANCE
    }

    companion object {
        private val NOOP_INSTANCE = TextMapPropagator.noop()
    }
}