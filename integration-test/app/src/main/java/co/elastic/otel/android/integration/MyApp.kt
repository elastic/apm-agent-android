package co.elastic.otel.android.integration

import android.app.Application
import co.elastic.otel.android.ElasticApmAgent
import co.elastic.otel.android.api.ElasticOtelAgent

class MyApp : Application() {
    companion object {
        internal lateinit var agent: ElasticOtelAgent
    }

    override fun onCreate() {
        super.onCreate()
        agent = ElasticApmAgent.builder(this)
            .setExportUrl("http://10.0.2.2:4318")
            .setServiceName("integration-test-app")
            .build()
    }
}