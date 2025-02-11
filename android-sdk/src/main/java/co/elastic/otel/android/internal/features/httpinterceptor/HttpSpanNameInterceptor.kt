package co.elastic.otel.android.internal.features.httpinterceptor

import co.elastic.otel.android.interceptor.Interceptor
import io.opentelemetry.sdk.trace.data.DelegatingSpanData
import io.opentelemetry.sdk.trace.data.SpanData
import io.opentelemetry.semconv.UrlAttributes

class HttpSpanNameInterceptor : Interceptor<SpanData> {
    private companion object {
        private val URL_PATTERN = Regex("https?://([^/]+).*")
    }

    override fun intercept(item: SpanData): SpanData {
        val url = item.attributes.get(UrlAttributes.URL_FULL) ?: return item

        return getNewName(item.name, url)?.let { newName ->
            NameDelegatingSpanData(item, newName)
        } ?: item
    }

    private fun getNewName(name: String, url: String): String? {
        return URL_PATTERN.matchEntire(url)?.let { match ->
            "$name ${match.groupValues[1]}"
        }
    }

    private class NameDelegatingSpanData(
        delegate: SpanData,
        private val name: String
    ) : DelegatingSpanData(delegate) {

        override fun getName(): String {
            return name
        }
    }
}