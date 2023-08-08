package co.elastic.apm.android.sdk.internal.configuration.impl;

import co.elastic.apm.android.sdk.ElasticApmConfiguration;
import co.elastic.apm.android.sdk.internal.configuration.Configuration;

public final class SignalPersistenceConfiguration extends Configuration {
    private final int maxCacheSize;

    public SignalPersistenceConfiguration(ElasticApmConfiguration apmConfiguration) {
        this.maxCacheSize = apmConfiguration.persistenceConfiguration.maxCacheSize;
    }

    public int getMaxCacheSize() {
        return maxCacheSize;
    }
}
