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
package co.elastic.otel.android.internal.features.instrumentation

import android.app.Application
import co.elastic.otel.android.api.ElasticOtelAgent
import co.elastic.otel.android.common.internal.logging.Elog
import co.elastic.otel.android.instrumentation.internal.Instrumentation
import java.util.ServiceLoader

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
internal class InstrumentationManager(
    private val application: Application,
    private val instrumentations: List<Instrumentation>
) {
    private val logger = Elog.getLogger()

    companion object {
        internal fun create(application: Application): InstrumentationManager {
            return InstrumentationManager(
                application,
                ServiceLoader.load(Instrumentation::class.java).toList()
            )
        }
    }

    fun initialize(agent: ElasticOtelAgent) {
        logger.debug("InstrumentationManager - Before installing instrumentations")
        instrumentations.forEach {
            logger.debug("Installing '${it.getId()}' with version '${it.getVersion()}'")
            it.install(application, agent)
        }
        logger.debug("InstrumentationManager - After installing instrumentations")
    }
}