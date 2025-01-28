package co.elastic.otel.android.instrumentation.internal

import android.app.Application
import co.elastic.otel.android.api.ElasticOtelAgent

interface Instrumentation {

    fun install(application: Application, agent: ElasticOtelAgent)
}