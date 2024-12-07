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

import android.app.Application
import co.elastic.apm.android.sdk.features.diskbuffering.DiskBufferingConfiguration
import co.elastic.apm.android.sdk.internal.services.ServiceManager
import io.opentelemetry.api.OpenTelemetry
import java.io.Closeable

abstract class ElasticOtelAgent(
    application: Application,
    private val diskBufferingConfiguration: DiskBufferingConfiguration
) : Closeable {

    init {
        ServiceManager.initialize(application)
        ServiceManager.get().start()
    }

    abstract fun getOpenTelemetry(): OpenTelemetry

    protected abstract fun onClose()

    final override fun close() {
        ServiceManager.get().stop()
        onClose()
    }
}