package co.elastic.otel.android.instrumentation.internal

import co.elastic.otel.android.api.ElasticOtelAgent

interface Instrumentation {

    fun install(agent: ElasticOtelAgent)
}