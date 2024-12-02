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
package co.elastic.apm.android.sdk.internal.configuration

import co.elastic.apm.android.sdk.testutils.ElasticAgentRule
import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.sdk.logs.data.LogRecordData
import io.opentelemetry.sdk.metrics.data.MetricData
import io.opentelemetry.sdk.trace.ReadableSpan
import java.lang.Boolean.TRUE
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class FilterConfigurationTest {
    @get:Rule
    val agentRule = ElasticAgentRule()

    @Test
    fun `Verify signal filters`() {
        agentRule.initialize(configurationInterceptor = {
            it.addSpanFilter { readableSpan: ReadableSpan ->
                TRUE == readableSpan.getAttribute(
                    AttributeKey.booleanKey("includeSpan")
                )
            }.addLogFilter { logRecordData: LogRecordData ->
                TRUE == logRecordData.attributes
                    .get(AttributeKey.booleanKey("includeLog"))
            }.addMetricFilter { metricData: MetricData -> metricData.name == "includeMetric" }
        })

        agentRule.sendSpan("spanOne") {
            setAttribute("includeSpan", false)
        }
        agentRule.sendSpan("spanTwo") {
            setAttribute("includeSpan", true)
        }
        agentRule.sendLog("first log") {
            setAttribute(AttributeKey.booleanKey("includeLog"), false)
        }
        agentRule.sendLog("second log") {
            setAttribute(AttributeKey.booleanKey("includeLog"), true)
        }
        agentRule.sendMetricCounter("includeMetric")
        agentRule.sendMetricCounter("secondCounter")

        val finishedSpans = agentRule.getFinishedSpans()
        val finishedLogs = agentRule.getFinishedLogRecords()
        val finishedMetrics = agentRule.getFinishedMetrics()
        assertThat(finishedSpans).hasSize(1)
        assertThat(finishedLogs).hasSize(1)
        assertThat(finishedMetrics).hasSize(1)
        assertThat(finishedSpans.first().name).isEqualTo("spanTwo")
        assertThat(finishedLogs.first().body.asString()).isEqualTo("second log")
        assertThat(finishedMetrics.first().name).isEqualTo("includeMetric")
    }
}
