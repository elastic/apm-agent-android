package co.elastic.otel.android.plugin.internal

import co.elastic.otel.android.plugin.ElasticAgentPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project

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