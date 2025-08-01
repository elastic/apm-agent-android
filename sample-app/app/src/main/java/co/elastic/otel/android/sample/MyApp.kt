package co.elastic.otel.android.sample

import android.app.Application
import co.elastic.otel.android.ElasticApmAgent
import co.elastic.otel.android.api.ElasticOtelAgent
import co.elastic.otel.android.extensions.log
import co.elastic.otel.android.extensions.span

class MyApp : Application() {
    companion object {
        internal lateinit var agent: ElasticOtelAgent
    }

    override fun onCreate() {
        super.onCreate()
        agent = ElasticApmAgent.builder(this)
            // For Android Emulators, the "10.0.2.2" address is the one of its host machine.
            // Using it here allows accessing services that are running on the host machine from an
            // Android application that runs in the emulator.
            .setExportUrl("http://10.0.2.2:4318")
            .setServiceName("weather-sample-app")
            .build()

        agent.span("Creating app") {
            agent.log("During app creation")
        }
    }
}