package co.elastic.apm.android.sdk.instrumentation

import co.elastic.apm.android.sdk.internal.instrumentation.CoroutineHelper
import co.elastic.apm.android.sdk.internal.otel.HistoryScope
import co.elastic.apm.android.sdk.internal.otel.SpanUtilities
import io.opentelemetry.api.trace.StatusCode
import io.opentelemetry.context.Context
import io.opentelemetry.extension.kotlin.asContextElement
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext


fun launch(
    scope: CoroutineScope,
    providedContext: CoroutineContext? = EmptyCoroutineContext,
    providedStart: CoroutineStart? = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> Unit
): Job {
    val context = providedContext ?: EmptyCoroutineContext
    val start = providedStart ?: CoroutineStart.DEFAULT

    return if (SpanUtilities.runningSpanFound()) {
        val oldScope = HistoryScope.of(Context.current())
        val span = CoroutineHelper.startCoroutineSpan("Coroutine")
        val current = oldScope.storeIn(span)
        val spanContext = current.asContextElement()
        scope.launch(context + spanContext, start, block).also {
            it.invokeOnCompletion { e ->
                e?.let { exception ->
                    span.setStatus(StatusCode.ERROR)
                    span.recordException(exception)
                }
                span.end()
            }
        }
    } else {
        scope.launch(context, start, block)
    }
}
