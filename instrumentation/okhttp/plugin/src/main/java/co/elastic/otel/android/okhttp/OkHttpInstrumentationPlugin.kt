package co.elastic.otel.android.okhttp

import co.elastic.otel.android.instrumentation.generated.BuildConfig
import co.elastic.otel.android.plugin.internal.InstrumentationPlugin
import org.gradle.api.Project

class OkHttpInstrumentationPlugin : InstrumentationPlugin() {

    override fun onApply(target: Project) {
        target.dependencies.add(
            "implementation",
            target.dependencies.create(BuildConfig.LIBRARY_URI)
        )
        target.dependencies.add(
            "byteBuddy",
            target.dependencies.create(BuildConfig.BYTEBUDDY_PLUGIN)
        )
    }
}