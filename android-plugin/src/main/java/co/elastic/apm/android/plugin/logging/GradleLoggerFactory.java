package co.elastic.apm.android.plugin.logging;

import org.gradle.api.logging.Logging;
import org.slf4j.Logger;

import co.elastic.apm.android.common.internal.logging.ELoggerFactory;

public class GradleLoggerFactory extends ELoggerFactory {

    @Override
    public Logger getLogger(String name) {
        return Logging.getLogger(name);
    }

    @Override
    protected String getDefaultName() {
        return "[ELASTIC_AGENT]";
    }
}
