package co.elastic.apm.android.test.attributes.traces

import co.elastic.apm.android.sdk.traces.common.tools.ElasticTracer

class SpanAttrHost {

    fun methodWithSpan() {
        val span = ElasticTracer.androidActivity().spanBuilder("My Span").startSpan()
        span.end()
    }
}