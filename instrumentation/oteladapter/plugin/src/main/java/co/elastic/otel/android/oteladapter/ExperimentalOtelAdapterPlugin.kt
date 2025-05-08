package co.elastic.otel.android.oteladapter

import co.elastic.otel.android.plugin.ElasticAgentPlugin
import co.elastic.otel.android.plugin.internal.InstrumentationPlugin
import org.gradle.api.Project

class ExperimentalOtelAdapterPlugin : InstrumentationPlugin() {

    override fun onApply(target: Project, agentPlugin: ElasticAgentPlugin) {
        TODO("Not yet implemented")
    }
}