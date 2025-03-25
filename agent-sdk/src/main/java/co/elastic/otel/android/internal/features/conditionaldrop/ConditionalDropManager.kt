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
package co.elastic.otel.android.internal.features.conditionaldrop

import io.opentelemetry.sdk.logs.export.LogRecordExporter
import io.opentelemetry.sdk.metrics.export.MetricExporter
import io.opentelemetry.sdk.trace.export.SpanExporter

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
internal class ConditionalDropManager(private var dropCondition: ((SignalType) -> Boolean)? = null) {

    fun dropWhen(condition: (SignalType) -> Boolean) {
        dropCondition = dropCondition?.let {
            { type ->
                it(type) || condition(type)
            }
        } ?: condition
    }

    fun createConditionalDropSpanExporter(delegate: SpanExporter): SpanExporter {
        return dropCondition?.let { ConditionalDropSpanExporter(delegate, it) } ?: delegate
    }

    fun createConditionalDropLogRecordExporter(delegate: LogRecordExporter): LogRecordExporter {
        return dropCondition?.let { ConditionalDropLogRecordExporter(delegate, it) } ?: delegate
    }

    fun createConditionalDropMetricExporter(delegate: MetricExporter): MetricExporter {
        return dropCondition?.let { ConditionalDropMetricExporter(delegate, it) } ?: delegate
    }
}