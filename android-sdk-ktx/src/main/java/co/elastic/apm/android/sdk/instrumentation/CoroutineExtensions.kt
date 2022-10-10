package co.elastic.apm.android.sdk.instrumentation

import co.elastic.apm.android.sdk.internal.otel.SpanUtilities
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
        val spanContext = Context.current().asContextElement()
        scope.launch(context + spanContext, start, block)
    } else {
        scope.launch(context, start, block)
    }
}
