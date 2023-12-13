package co.elastic.apm.android.sample

import android.app.Application
import co.elastic.apm.android.sdk.ElasticApmAgent

class MyApp : Application() {

    override fun onCreate() {
        super.onCreate()
        ElasticApmAgent.initialize(this)
    }
}