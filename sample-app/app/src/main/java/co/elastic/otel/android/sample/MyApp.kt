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
            .setExportUrl("http://10.0.2.2:8200")
            .setManagementUrl("http://10.0.2.2:8200/config/v1/agents")
            .setServiceName("weather-sample-app")
            .build()

        agent.span("Creating app") {
            agent.log("During app creation")
        }
    }
}