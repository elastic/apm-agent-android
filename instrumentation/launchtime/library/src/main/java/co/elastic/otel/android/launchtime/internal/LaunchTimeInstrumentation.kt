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
package co.elastic.otel.android.launchtime.internal

import android.app.Application
import androidx.lifecycle.ProcessLifecycleOwner
import co.elastic.otel.android.api.ElasticOtelAgent
import co.elastic.otel.android.api.flusher.MetricFlusher
import co.elastic.otel.android.instrumentation.generated.launchtime.BuildConfig
import co.elastic.otel.android.instrumentation.internal.Instrumentation
import com.google.auto.service.AutoService

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
@AutoService(Instrumentation::class)
class LaunchTimeInstrumentation : Instrumentation, LaunchTimeApplicationListener.Callback {
    @Volatile
    private var agent: ElasticOtelAgent? = null

    @Volatile
    private var observer: LaunchTimeApplicationListener? = null

    override fun install(application: Application, agent: ElasticOtelAgent) {
        this.agent = agent
        observer = LaunchTimeApplicationListener(this).apply {
            ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        }
    }

    override fun onLaunchTimeAvailable(launchTimeMillis: Long) {
        agent?.let { sendAppLaunchTimeMetric(it, launchTimeMillis) }
        observer?.let { ProcessLifecycleOwner.get().lifecycle.removeObserver(it) }
        observer = null
        agent = null
    }

    private fun sendAppLaunchTimeMetric(agent: ElasticOtelAgent, launchTimeMillis: Long) {
        val meter = agent.getOpenTelemetry().getMeter("LaunchTimeTracker")
        val launchTime = meter.gaugeBuilder("application.launch.time").buildObserver()
        val batchCallback = meter.batchCallback({
            launchTime.record(launchTimeMillis.toDouble())
        }, launchTime)
        if (agent is MetricFlusher) {
            agent.flushMetrics()
        }
        batchCallback.close()
    }

    override fun getId(): String {
        return BuildConfig.INSTRUMENTATION_ID
    }

    override fun getVersion(): String {
        return BuildConfig.INSTRUMENTATION_VERSION
    }
}