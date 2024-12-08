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
package co.elastic.apm.android.sdk.internal.api

import co.elastic.apm.android.sdk.features.diskbuffering.DiskBufferingManager
import co.elastic.apm.android.sdk.internal.services.re.ServiceManager
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.sdk.OpenTelemetrySdk
import java.io.Closeable

abstract class ElasticOtelAgent(private val configuration: Configuration) : Closeable {

    init {
        configuration.diskBufferingManager.initialize(configuration.serviceManager)
    }

    abstract fun getOpenTelemetry(): OpenTelemetry

    final override fun close() {
        onClose()
        configuration.serviceManager.close()
        configuration.openTelemetrySdk.close()
    }

    protected abstract fun onClose()

    data class Configuration(
        val openTelemetrySdk: OpenTelemetrySdk,
        val serviceManager: ServiceManager,
        val diskBufferingManager: DiskBufferingManager
    )
}