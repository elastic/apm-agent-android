package co.elastic.apm.android.sdk.features.centralconfig

import co.elastic.apm.android.sdk.internal.configuration.kotlin.Configuration

internal class CentralConfiguration : Configuration() {
    private val recording = createBooleanOption("recording", true)

    fun isRecording(): Boolean {
        return recording.get()
    }
}