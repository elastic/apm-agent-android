package co.elastic.apm.android.sdk.internal.logging;

import org.slf4j.Marker;
import org.slf4j.event.Level;
import org.slf4j.helpers.LegacyAbstractLogger;
import org.slf4j.helpers.MessageFormatter;

abstract class BaseLogger extends LegacyAbstractLogger {

    BaseLogger(String tag) {
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