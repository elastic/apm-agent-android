package co.elastic.apm.android.sample

import android.app.Application
import co.elastic.apm.android.sdk.ElasticApmAgent
import co.elastic.apm.android.sdk.ElasticApmConfiguration
import co.elastic.apm.android.sdk.configuration.logging.LogLevel
import co.elastic.apm.android.sdk.configuration.logging.LoggingPolicy
import co.elastic.apm.android.sdk.features.persistence.PersistenceConfiguration

class MyApp : Application() {

    override fun onCreate() {
        super.onCreate()
        ElasticApmAgent.initialize(
            this,
            ElasticApmConfiguration.builder()
                .setPersistenceConfiguration(
                    PersistenceConfiguration.builder().setEnabled(true).build()
                )
                .setLibraryLoggingPolicy(LoggingPolicy.enabled(LogLevel.TRACE))
                .build()
        )
    }
}