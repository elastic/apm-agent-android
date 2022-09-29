package co.elastic.apm.android.common.internal.logging;

import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;

public abstract class ELoggerFactory implements ILoggerFactory {

    public Logger getDefaultLogger() {
        return getLogger(getDefaultName());
    }

    protected abstract String getDefaultName();
}
