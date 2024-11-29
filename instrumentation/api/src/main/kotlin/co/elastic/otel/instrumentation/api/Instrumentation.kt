package co.elastic.otel.instrumentation.api

import co.elastic.otel.api.ElasticAgent

interface Instrumentation {
    fun install(agent: ElasticAgent)
}