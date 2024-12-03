package co.elastic.apm.android.sdk.internal.opentelemetry.processors.spans

import co.elastic.apm.android.sdk.tools.Interceptor
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.context.Context
import io.opentelemetry.sdk.trace.ReadWriteSpan
import io.opentelemetry.sdk.trace.ReadableSpan
import io.opentelemetry.sdk.trace.SpanProcessor

internal class SpanAttributesProcessor(private val interceptor: Interceptor<Attributes>) :
    SpanProcessor {

    override fun onStart(parentContext: Context, span: ReadWriteSpan) {
        span.setAllAttributes(interceptor.intercept(span.attributes))
    }

    override fun isStartRequired(): Boolean = true

    override fun onEnd(span: ReadableSpan) {

    }

    override fun isEndRequired(): Boolean = false
}