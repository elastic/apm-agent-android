package co.elastic.apm.android.common.internal.logging;

import org.slf4j.Marker;
import org.slf4j.event.Level;
import org.slf4j.helpers.LegacyAbstractLogger;
import org.slf4j.helpers.MessageFormatter;

public abstract class BaseELogger extends LegacyAbstractLogger {

    protected BaseELogger(String tag) {
        name = tag;
    }

    @Override
    protected String getFullyQualifiedCallerName() {
        return null;
    }

    @Override
    protected void handleNormalizedLoggingCall(Level level, Marker marker, String msg, Object[] arguments, Throwable throwable) {
        handleLoggingCall(
                level,
                MessageFormatter.arrayFormat(msg, arguments, throwable).getMessage(),
                throwable
        );
    }

    protected abstract void handleLoggingCall(Level level, String formattedMessage, Throwable throwable);
}