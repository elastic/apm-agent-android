package co.elastic.otel.android.launchtime

import co.elastic.otel.android.instrumentation.generated.BuildConfig
import org.gradle.api.Plugin
import org.gradle.api.Project

class LaunchTimePlugin : Plugin<Project> {

    override fun apply(target: Project) {
        target.dependencies.add(
            "implementation",
            target.dependencies.create(BuildConfig.LIBRARY_URI)
        )
    }
}