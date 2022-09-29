package co.elastic.apm.android.common.internal.logging;

import androidx.annotation.NonNull;

import org.slf4j.Logger;

public class Elog {

    private static ELoggerFactory loggerFactory;

    public static void init(ELoggerFactory factory) {
        if (loggerFactory != null) {
            System.out.println("Already initialized");
            return;
        }
        loggerFactory = factory;
    }

    public static Logger getLogger(@NonNull String name) {
        return loggerFactory.getLogger(name);
    }

    public static Logger getLogger() {
        return loggerFactory.getDefaultLogger();
    }
}
