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
package co.elastic.otel.android.oteladapter.internal.delegate

import co.elastic.otel.android.oteladapter.internal.delegate.context.ContextPropagatorsDelegate
import co.elastic.otel.android.oteladapter.internal.delegate.logger.LoggerProviderDelegator
import co.elastic.otel.android.oteladapter.internal.delegate.meter.MeterProviderDelegator
import co.elastic.otel.android.oteladapter.internal.delegate.tools.Delegator
import co.elastic.otel.android.oteladapter.internal.delegate.tracer.TracerProviderDelegator
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.logs.LoggerProvider
import io.opentelemetry.api.metrics.MeterProvider
import io.opentelemetry.api.trace.TracerProvider
import io.opentelemetry.context.propagation.ContextPropagators

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
internal class OpenTelemetryDelegator(initialValue: OpenTelemetry) :
    Delegator<OpenTelemetry>(initialValue),
    OpenTelemetry {
    private val tracerProvider = TracerProviderDelegator(initialValue.tracerProvider)
    private val meterProvider = MeterProviderDelegator(initialValue.meterProvider)
    private val loggerProvider = LoggerProviderDelegator(initialValue.logsBridge)
    private val contextPropagators = ContextPropagatorsDelegate(initialValue.propagators)

    override fun setDelegate(value: OpenTelemetry) {
        super.setDelegate(value)
        tracerProvider.setDelegate(value.tracerProvider)
        meterProvider.setDelegate(value.meterProvider)
        loggerProvider.setDelegate(value.logsBridge)
        contextPropagators.setDelegate(value.propagators)
    }

    override fun reset() {
        super.reset()
        tracerProvider.reset()
        meterProvider.reset()
        loggerProvider.reset()
        contextPropagators.reset()
    }

    override fun getTracerProvider(): TracerProvider {
        return tracerProvider
    }

    override fun getMeterProvider(): MeterProvider {
        return meterProvider
    }

    override fun getLogsBridge(): LoggerProvider {
        return loggerProvider
    }

    override fun getPropagators(): ContextPropagators {
        return contextPropagators
    }

    override fun getNoopValue(): OpenTelemetry {
        return NOOP_INSTANCE
    }

    companion object {
        private val NOOP_INSTANCE = OpenTelemetry.noop()
    }
}