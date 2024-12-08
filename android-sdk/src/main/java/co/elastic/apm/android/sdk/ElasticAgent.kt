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
package co.elastic.apm.android.sdk

import android.app.Application
import co.elastic.apm.android.sdk.internal.api.ElasticOtelAgent
import co.elastic.apm.android.sdk.internal.opentelemetry.ElasticOpenTelemetryBuilder
import io.opentelemetry.api.OpenTelemetry

class ElasticAgent private constructor(
    configuration: Configuration
) : ElasticOtelAgent(configuration) {
    private val openTelemetry = configuration.openTelemetrySdk

    override fun getOpenTelemetry(): OpenTelemetry {
        return openTelemetry
    }

    override fun onClose() {
        openTelemetry.close()
    }

    companion object {
        @JvmStatic
        fun builder(application: Application): Builder {
            return Builder(application)
        }
    }

    class Builder internal constructor(application: Application) :
        ElasticOpenTelemetryBuilder<Builder>(application) {

        fun build(): ElasticAgent {
            return ElasticAgent(buildConfiguration())
        }
    }
}