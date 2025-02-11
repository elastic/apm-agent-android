package co.elastic.otel.android.internal.features.httpinterceptor

import co.elastic.otel.android.interceptor.Interceptor
import io.opentelemetry.sdk.common.CompletableResultCode
import io.opentelemetry.sdk.trace.data.SpanData
import io.opentelemetry.sdk.trace.export.SpanExporter
import io.opentelemetry.semconv.UrlAttributes

internal class HttpSpanExporter(
    private val delegate: SpanExporter,
    private val httpSpanInterceptor: Interceptor<SpanData>
) : SpanExporter {

    override fun export(spans: Collection<SpanData>): CompletableResultCode {
        val processedSpans = mutableListOf<SpanData>()

        spans.forEach {
            processedSpans.add(process(it))
        }

        return delegate.export(processedSpans)
    }

    private fun process(spanData: SpanData): SpanData {
        if (spanData.attributes.get(UrlAttributes.URL_FULL) != null) {
            return httpSpanInterceptor.intercept(spanData)
        }
        return spanData
    }

    override fun flush(): CompletableResultCode {
        return delegate.flush()
    }

    override fun shutdown(): CompletableResultCode {
        return delegate.shutdown()
    }
}