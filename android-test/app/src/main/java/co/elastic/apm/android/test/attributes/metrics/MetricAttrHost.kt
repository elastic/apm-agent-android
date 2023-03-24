package co.elastic.apm.android.test.attributes.metrics

import co.elastic.apm.android.sdk.metrics.ElasticMeters
import io.opentelemetry.api.metrics.Meter

class MetricAttrHost {

    fun methodWithCounter() {
        getMeter().counterBuilder("my-test-counter").build().add(2)
    }

    private fun getMeter(): Meter {
        return ElasticMeters.builder("Test meter").build()
    }
}