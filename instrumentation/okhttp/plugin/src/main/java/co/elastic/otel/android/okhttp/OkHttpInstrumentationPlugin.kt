package co.elastic.otel.android.okhttp

import co.elastic.otel.android.instrumentation.generated.BuildConfig
import co.elastic.otel.android.plugin.ElasticAgentPlugin
import co.elastic.otel.android.plugin.internal.ByteBuddyDependencyAttacher
import co.elastic.otel.android.plugin.internal.InstrumentationPlugin
import org.gradle.api.Project

class OkHttpInstrumentationPlugin : InstrumentationPlugin() {

    override fun onApply(target: Project, agentPlugin: ElasticAgentPlugin) {
        target.dependencies.add(
            "implementation",
            target.dependencies.create(BuildConfig.LIBRARY_URI)
        )
        agentPlugin.addBuildVariantListener(
            ByteBuddyDependencyAttacher(
                target,
                BuildConfig.BYTEBUDDY_PLUGIN
            )
        )
    }
}