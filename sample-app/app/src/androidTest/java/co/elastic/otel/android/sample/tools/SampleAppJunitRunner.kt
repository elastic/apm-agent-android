package co.elastic.otel.android.sample.tools

import androidx.test.runner.AndroidJUnitRunner

class SampleAppJunitRunner : AndroidJUnitRunner() {
    init {
        System.setProperty("io.opentelemetry.context.contextStorageProvider", "default")
    }
}