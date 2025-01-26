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
package co.elastic.otel.android.plugin

import co.elastic.apm.generated.BuildConfig
import co.elastic.otel.android.common.internal.logging.Elog
import co.elastic.otel.android.plugin.logging.GradleLoggerFactory
import net.bytebuddy.build.gradle.android.ByteBuddyAndroidPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project

internal class ApmAndroidAgentPlugin : Plugin<Project> {
    private lateinit var project: Project

    override fun apply(target: Project) {
        this.project = target
        Elog.init(GradleLoggerFactory())
        addByteBuddyPlugin()
        addSdkDependency()
    }

    private fun addByteBuddyPlugin() {
        project.pluginManager.apply(ByteBuddyAndroidPlugin::class.java)
    }

    private fun addSdkDependency() {
        project.dependencies.add("implementation", BuildConfig.SDK_DEPENDENCY_URI)
    }
}