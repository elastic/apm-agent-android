package co.elastic.otel.android.launchtime

import co.elastic.otel.android.instrumentation.generated.BuildConfig
import co.elastic.otel.android.plugin.internal.InstrumentationPlugin
import org.gradle.api.Project

class LaunchTimePlugin : InstrumentationPlugin() {

    override fun onApply(target: Project) {
        target.dependencies.add(
            "implementation",
            target.dependencies.create(BuildConfig.LIBRARY_URI)
        )
    }
}