package co.elastic.apm.android.common.internal.logging;

import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;

public class Elog {

    private static ILoggerFactory loggerFactory;

    public static void init(ILoggerFactory factory) {
        if (loggerFactory != null) {
            throw new IllegalStateException(Elog.class.getSimpleName() + " already initialized");
        }
        loggerFactory = factory;
    }

    public static Logger getLogger(String name) {
        return loggerFactory.getLogger(name);
    }
}
