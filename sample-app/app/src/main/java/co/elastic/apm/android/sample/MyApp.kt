package co.elastic.apm.android.sample

import android.app.Application
import co.elastic.otel.android.ElasticApmAgent
import co.elastic.otel.android.api.ElasticOtelAgent
import co.elastic.otel.android.extensions.log

class MyApp : Application() {
    companion object {
        internal lateinit var agent: ElasticOtelAgent
    }

    override fun onCreate() {
        super.onCreate()
        agent = ElasticApmAgent.builder(this)
            .setUrl("http://10.0.2.2:8200")
            .setServiceName("weather-sample-app")
            .build()

        agent.log("App created")
    }
}