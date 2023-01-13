package co.elastic.apm.android.test.attributes.metrics

import io.opentelemetry.api.GlobalOpenTelemetry
import io.opentelemetry.api.metrics.Meter

class MetricAttrHost {

    fun methodWithCounter() {
        getMeter().counterBuilder("my-test-counter").build().add(2)
    }

    private fun getMeter(): Meter {
        return GlobalOpenTelemetry.getMeterProvider().meterBuilder("Test meter").build()
    }
}