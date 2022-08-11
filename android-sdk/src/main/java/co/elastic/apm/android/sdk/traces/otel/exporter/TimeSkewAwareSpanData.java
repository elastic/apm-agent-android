package co.elastic.apm.android.sdk.traces.otel.exporter;

import java.util.concurrent.TimeUnit;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.trace.data.DelegatingSpanData;
import io.opentelemetry.sdk.trace.data.SpanData;

class TimeSkewAwareSpanData extends DelegatingSpanData {

    protected TimeSkewAwareSpanData(SpanData delegate) {
        super(delegate);
    }

    @Override
    public Attributes getAttributes() {
        return Attributes.builder().putAll(super.getAttributes())
                .put("telemetry.sdk.elastic_export_timestamp", TimeUnit.MILLISECONDS.toNanos(System.currentTimeMillis()))
                .build();
    }
}