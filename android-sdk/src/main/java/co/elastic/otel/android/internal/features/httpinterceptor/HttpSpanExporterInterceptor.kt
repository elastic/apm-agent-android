package co.elastic.otel.android.internal.features.httpinterceptor

import co.elastic.otel.android.interceptor.Interceptor
import io.opentelemetry.sdk.trace.data.SpanData
import io.opentelemetry.sdk.trace.export.SpanExporter

internal class HttpSpanExporterInterceptor(private val httpSpanInterceptor: Interceptor<SpanData>) :
    Interceptor<SpanExporter> {

    override fun intercept(item: SpanExporter): SpanExporter {
        return HttpSpanExporter(item, httpSpanInterceptor)
    }
}