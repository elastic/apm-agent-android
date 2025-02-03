package co.elastic.otel.android.crash.internal

import android.app.Application
import co.elastic.otel.android.api.ElasticOtelAgent
import co.elastic.otel.android.instrumentation.internal.Instrumentation
import com.google.auto.service.AutoService

@AutoService(Instrumentation::class)
class CrashInstrumentation : Instrumentation {

    override fun install(application: Application, agent: ElasticOtelAgent) {
        TODO("Not yet implemented")
    }
}