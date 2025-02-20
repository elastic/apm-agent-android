package co.elastic.otel.android.internal.api

import co.elastic.otel.android.api.ElasticOtelAgent
import co.elastic.otel.android.api.flusher.LogRecordFlusher
import co.elastic.otel.android.api.flusher.MetricFlusher
import co.elastic.otel.android.api.flusher.SpanFlusher

interface ManagedElasticOtelAgentContract : ElasticOtelAgent, SpanFlusher, LogRecordFlusher,
    MetricFlusher