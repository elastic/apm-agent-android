package co.elastic.otel.android.internal.features.httpinterceptor

import co.elastic.otel.android.interceptor.Interceptor
import io.opentelemetry.sdk.trace.data.SpanData

class HttpSpanNameInterceptor : Interceptor<SpanData> {

    override fun intercept(item: SpanData): SpanData {
        TODO("Not yet implemented")
    }
}