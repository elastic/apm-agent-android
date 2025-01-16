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
import co.elastic.apm.android.sdk.internal.services.ServiceManager
import co.elastic.apm.android.sdk.session.SessionProvider
import co.elastic.apm.android.sdk.tools.interceptor.Interceptor
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.common.Clock

class TestElasticOtelAgent(serviceManager: ServiceManager, configuration: Configuration) :
    ElasticOtelAgent(serviceManager, configuration) {
    private val openTelemetry: OpenTelemetrySdk = configuration.openTelemetrySdk

    override fun getOpenTelemetry(): OpenTelemetry {
        return openTelemetry
    }

    override fun onClose() {
    }

    companion object {
        fun builder(application: Application): Builder {
            return Builder(application)
        }
    }

    class Builder(private val application: Application) : ElasticOpenTelemetryBuilder<Builder>() {
        val configurationInterceptors = mutableListOf<Interceptor<Configuration>>()
        val serviceManagerInterceptors = mutableListOf<Interceptor<ServiceManager>>()

        public override fun setExporterProvider(value: ExporterProvider): Builder {
            return super.setExporterProvider(value)
        }

        public override fun setClock(value: Clock): Builder {
            return super.setClock(value)
        }

        public override fun setSessionProvider(value: SessionProvider): Builder {
            return super.setSessionProvider(value)
        }

        fun build(): TestElasticOtelAgent {
            val serviceManager = Interceptor.composite(serviceManagerInterceptors)
                .intercept(ServiceManager.create(application))
            return TestElasticOtelAgent(
                serviceManager,
                Interceptor.composite(configurationInterceptors)
                    .intercept(buildConfiguration(serviceManager))
            )
        }
    }
}