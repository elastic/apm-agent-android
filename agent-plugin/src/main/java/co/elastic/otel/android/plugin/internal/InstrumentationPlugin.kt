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
package co.elastic.otel.android.plugin.internal

import co.elastic.otel.android.plugin.ElasticAgentPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
abstract class InstrumentationPlugin : Plugin<Project> {

    final override fun apply(target: Project) {
        with(target.plugins) {
            if (!hasPlugin(ElasticAgentPlugin::class.java)) {
                apply(ElasticAgentPlugin::class.java)
            }
        }
        onApply(target, target.plugins.getPlugin(ElasticAgentPlugin::class.java))
    }

    abstract fun onApply(target: Project, agentPlugin: ElasticAgentPlugin)
}