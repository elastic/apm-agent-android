package co.elastic.apm.android.sdk.internal.exceptions;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

import java.io.PrintWriter;
import java.io.StringWriter;

import io.opentelemetry.api.logs.EventBuilder;
import io.opentelemetry.api.logs.GlobalLoggerProvider;
import io.opentelemetry.api.logs.Logger;
import io.opentelemetry.api.logs.LoggerProvider;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
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
        SdkLoggerProvider loggerProvider = getLoggerProvider();

        emitCrashEvent(getCrashReporter(loggerProvider), e);
        loggerProvider.forceFlush();

        wrapped.uncaughtException(t, e);
    }

    private void emitCrashEvent(Logger crashReporter, @NonNull Throwable e) {
        EventBuilder crashEvent = crashReporter.eventBuilder("crash");
        crashEvent.setAttribute(SemanticAttributes.EXCEPTION_MESSAGE, e.getMessage());
        crashEvent.setAttribute(SemanticAttributes.EXCEPTION_STACKTRACE, stackTraceToString(e));
        crashEvent.setAttribute(SemanticAttributes.EXCEPTION_TYPE, e.getClass().getName());
        crashEvent.emit();
    }

    private Logger getCrashReporter(LoggerProvider loggerProvider) {
        return loggerProvider.loggerBuilder("CrashReport")
                .setEventDomain("device").build();
    }

    private SdkLoggerProvider getLoggerProvider() {
        return (SdkLoggerProvider) GlobalLoggerProvider.get();
    }

    private String stackTraceToString(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        throwable.printStackTrace(pw);
        pw.flush();

        return sw.toString();
    }
}
