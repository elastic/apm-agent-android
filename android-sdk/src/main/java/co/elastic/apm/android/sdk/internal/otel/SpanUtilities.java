package co.elastic.apm.android.sdk.internal.otel;

import io.opentelemetry.context.Context;

public final class SpanUtilities {

    public static boolean runningSpanFound() {
        return !runningSpanNotFound();
    }

    public static boolean runningSpanNotFound() {
        return Context.current().equals(Context.root());
    }
}
