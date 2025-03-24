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
package co.elastic.otel.android.crash.internal

import android.app.Application
import co.elastic.otel.android.api.ElasticOtelAgent
import co.elastic.otel.android.crash.internal.handler.ElasticExceptionHandler
import co.elastic.otel.android.instrumentation.generated.BuildConfig
import co.elastic.otel.android.instrumentation.internal.Instrumentation
import com.google.auto.service.AutoService

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
@AutoService(Instrumentation::class)
class CrashInstrumentation : Instrumentation {

    override fun install(application: Application, agent: ElasticOtelAgent) {
        Thread.setDefaultUncaughtExceptionHandler(ElasticExceptionHandler.create(agent))
    }

    override fun getId(): String {
        return BuildConfig.INSTRUMENTATION_ID
    }

    override fun getVersion(): String {
        return BuildConfig.INSTRUMENTATION_VERSION
    }
}