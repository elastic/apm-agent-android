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
package co.elastic.apm.android.sdk.testutils

import android.app.Application
import co.elastic.apm.android.sdk.exporters.ExporterProvider
import co.elastic.apm.android.sdk.internal.api.ElasticOtelAgent
import co.elastic.apm.android.sdk.internal.opentelemetry.ElasticOpenTelemetryBuilder
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.sdk.OpenTelemetrySdk

class TestElasticOtelAgent(application: Application, private val openTelemetry: OpenTelemetrySdk) :
    ElasticOtelAgent(application) {

    override fun getOpenTelemetry(): OpenTelemetry {
        return openTelemetry
    }

    override fun onClose() {
        openTelemetry.close()
    }

    companion object {
        fun builder(application: Application): Builder {
            return Builder(application)
        }
    }

    class Builder(private val application: Application) :
        ElasticOpenTelemetryBuilder<Builder>(application) {

        public override fun setExporterProvider(value: ExporterProvider): Builder {
            return super.setExporterProvider(value)
        }

        fun build(): TestElasticOtelAgent {
            return TestElasticOtelAgent(application, buildConfiguration())
        }
    }
}