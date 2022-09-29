package co.elastic.apm.android.sdk.internal.logging;

import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;

public class AndroidLoggerFactory implements ILoggerFactory {
    @Override
    public Logger getLogger(String name) {
        return new AndroidLogger(name);
    }
}
