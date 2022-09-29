package co.elastic.apm.android.sdk.internal.otel;

import static co.elastic.apm.android.sdk.traces.common.rules.DiscardExclusionRule.DISCARDED_NAME;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Context;

public final class SpanUtilities {

    /**
     * Checks if there's a span context history available, if there is, it sets the previous context
     * as the current one and discards the current {@link Span}. Otherwise it does nothing.
     */
    public static void tryRevertCurrentContext() {
        if (runningSpanNotFound()) {
            return;
        }
        HistoryScope scope = HistoryScope.from(Context.current());
        if (scope != null) {
            scope.close();
            discard(Span.current());
        }
    }

    /**
     * Discards a span from being exported.
     *
     * @param span - The span to be discarded.
     */
    public static void discard(Span span) {
        span.updateName(DISCARDED_NAME);
        span.end();
    }

    public static boolean runningSpanFound() {
        return !runningSpanNotFound();
    }

    public static boolean runningSpanNotFound() {
        return Context.current().equals(Context.root());
    }
}
