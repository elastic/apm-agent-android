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
package co.elastic.otel.android.exporters.configurable

import co.elastic.otel.android.internal.exporters.configurable.MutableMetricExporter
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.opentelemetry.sdk.common.CompletableResultCode
import io.opentelemetry.sdk.metrics.InstrumentType
import io.opentelemetry.sdk.metrics.data.AggregationTemporality
import io.opentelemetry.sdk.metrics.data.MetricData
import io.opentelemetry.sdk.metrics.export.MetricExporter
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class MutableMetricExporterTest {

    @Test
    fun `Verify states`() {
        // Initial state
        val exporter = MutableMetricExporter()
        assertThat(exporter.getDelegate()).isNull()
        // Delegation
        assertThat(exporter.export(mutableSetOf())).isEqualTo(CompletableResultCode.ofSuccess())
        assertThat(exporter.flush()).isEqualTo(CompletableResultCode.ofSuccess())
        assertThat(exporter.shutdown()).isEqualTo(CompletableResultCode.ofSuccess())
        assertThat(exporter.getAggregationTemporality(InstrumentType.COUNTER)).isEqualTo(
            AggregationTemporality.DELTA
        )

        // Changed state
        val dummyExporter = mockk<MetricExporter>()
        val dummyReturnValue = CompletableResultCode.ofFailure()
        val dummyAggregationTemporality = mockk<AggregationTemporality>()
        every { dummyExporter.export(any()) }.returns(dummyReturnValue)
        every { dummyExporter.flush() }.returns(dummyReturnValue)
        every { dummyExporter.shutdown() }.returns(dummyReturnValue)
        every { dummyExporter.getAggregationTemporality(any()) }.returns(dummyAggregationTemporality)
        exporter.setDelegate(dummyExporter)

        assertThat(exporter.getDelegate()).isEqualTo(dummyExporter)

        // Delegation
        val metrics = mutableSetOf<MetricData>()
        val instrumentType = mockk<InstrumentType>()
        assertThat(exporter.export(metrics)).isEqualTo(dummyReturnValue)
        assertThat(exporter.flush()).isEqualTo(dummyReturnValue)
        assertThat(exporter.shutdown()).isEqualTo(dummyReturnValue)
        assertThat(exporter.getAggregationTemporality(instrumentType)).isEqualTo(
            dummyAggregationTemporality
        )
        verify {
            dummyExporter.export(metrics)
            dummyExporter.flush()
            dummyExporter.shutdown()
            dummyExporter.getAggregationTemporality(instrumentType)
        }
    }
}