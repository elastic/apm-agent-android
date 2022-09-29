package co.elastic.apm.android.sdk.internal.logging;

import org.slf4j.Logger;

import co.elastic.apm.android.common.internal.logging.ELoggerFactory;

public class AndroidLoggerFactory extends ELoggerFactory {

    @Override
    public Logger getLogger(String name) {
        return new AndroidLogger(name);
    }

    @Override
    protected String getDefaultName() {
        return "[ELASTIC_AGENT]";
    }
}
