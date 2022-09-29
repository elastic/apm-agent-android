package co.elastic.apm.android.sdk.internal.logging;

import android.util.Log;

import org.slf4j.event.Level;

import co.elastic.apm.android.common.internal.logging.BaseELogger;

class AndroidLogger extends BaseELogger {

    AndroidLogger(String tag) {
        super(tag);
    }

    @Override
    protected void handleLoggingCall(Level level, String formattedMessage, Throwable throwable) {
        switch (level) {
            case ERROR:
                Log.e(name, formattedMessage, throwable);
                break;
            case WARN:
                Log.w(name, formattedMessage, throwable);
                break;
            case INFO:
                Log.i(name, formattedMessage, throwable);
                break;
            case DEBUG:
                Log.d(name, formattedMessage, throwable);
                break;
            case TRACE:
                Log.v(name, formattedMessage, throwable);
                break;
        }
    }

    @Override
    public boolean isTraceEnabled() {
        return true;
    }

    @Override
    public boolean isDebugEnabled() {
        return true;
    }

    @Override
    public boolean isInfoEnabled() {
        return true;
    }

    @Override
    public boolean isWarnEnabled() {
        return true;
    }

    @Override
    public boolean isErrorEnabled() {
        return true;
    }
}
