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
package co.elastic.otel.android.oteladapter.internal.delegate.meter

import co.elastic.otel.android.oteladapter.internal.delegate.tools.Delegator
import io.opentelemetry.api.metrics.MeterBuilder
import io.opentelemetry.api.metrics.MeterProvider

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
class MeterProviderDelegator(initialValue: MeterProvider) : Delegator<MeterProvider>(initialValue),
    MeterProvider {

    override fun meterBuilder(instrumentationScopeName: String): MeterBuilder? {
        return getDelegate().meterBuilder(instrumentationScopeName)
    }

    override fun getNoopValue(): MeterProvider {
        return NOOP_INSTANCE
    }

    companion object {
        val NOOP_INSTANCE: MeterProvider = MeterProvider.noop()
    }
}