package co.elastic.apm.android.sdk.internal.otel;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.ContextKey;
import io.opentelemetry.context.Scope;

public final class HistoryScope implements Scope {
    private final Context previousContext;
    private static final ContextKey<HistoryScope> KEY = ContextKey.named("previous-span-context");

    public static HistoryScope of(Context previousContext) {
        return new HistoryScope(previousContext);
    }

    public static HistoryScope from(Context current) {
        return current.get(KEY);
    }

    public Context storeIn(Span span) {
        return storeIn(Context.current().with(span));
    }

    public Context storeIn(Context currentContext) {
        return currentContext.with(KEY, this);
    }

    private HistoryScope(Context previousContext) {
        this.previousContext = previousContext;
    }

    @Override
    public void close() {
        previousContext.makeCurrent();
    }
}
