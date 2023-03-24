package co.elastic.apm.android.test.attributes.traces

import co.elastic.apm.android.sdk.traces.ElasticTracers

class SpanAttrHost {

    fun methodWithSpan() {
        val span = ElasticTracers.androidActivity().spanBuilder("My Span").startSpan()
        span.end()
    }
}