package co.elastic.otel.android.instrumentation

import co.elastic.otel.android.api.ElasticOtelAgent

interface AndroidInstrumentation {

    fun install(agent: ElasticOtelAgent)
}