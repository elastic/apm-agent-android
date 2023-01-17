package co.elastic.apm.android.sdk.internal.exceptions;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

import java.io.PrintWriter;
import java.io.StringWriter;

import io.opentelemetry.api.logs.EventBuilder;
import io.opentelemetry.api.logs.GlobalLoggerProvider;
import io.opentelemetry.api.logs.Logger;
import io.opentelemetry.semconv.trace.attributes.SemanticAttributes;

public final class ElasticExceptionHandler implements Thread.UncaughtExceptionHandler {
    private final Thread.UncaughtExceptionHandler wrapped;

    @VisibleForTesting
    public ElasticExceptionHandler(Thread.UncaughtExceptionHandler wrapped) {
        this.wrapped = wrapped;
    }

    public ElasticExceptionHandler() {
        this(Thread.getDefaultUncaughtExceptionHandler());
    }

    @Override
    public void uncaughtException(@NonNull Thread t, @NonNull Throwable e) {
        Logger crashReporter = GlobalLoggerProvider.get().loggerBuilder("CrashReport")
                .setEventDomain("device").build();
        EventBuilder crashEvent = crashReporter.eventBuilder("crash");
        crashEvent.setAttribute(SemanticAttributes.EXCEPTION_MESSAGE, e.getMessage());
        crashEvent.setAttribute(SemanticAttributes.EXCEPTION_STACKTRACE, stackTraceToString(e));
        crashEvent.setAttribute(SemanticAttributes.EXCEPTION_TYPE, e.getClass().getName());
        crashEvent.emit();

        wrapped.uncaughtException(t, e);
    }

    private String stackTraceToString(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        throwable.printStackTrace(pw);
        pw.flush();

        return sw.toString();
    }
}
