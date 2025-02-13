package co.elastic.otel.android.plugin.extensions

import org.gradle.api.provider.ListProperty

interface BytecodeInstrumentation {
    val disableForBuildTypes: ListProperty<String>
}
