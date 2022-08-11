package co.elastic.apm.android.sdk.traces.otel.exporter;

import java.util.concurrent.TimeUnit;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.data.DelegatingSpanData;
import io.opentelemetry.sdk.trace.data.SpanData;

class TimeSkewAwareSpanData extends DelegatingSpanData {

    protected TimeSkewAwareSpanData(SpanData delegate) {
        super(delegate);
    }

    @Override
    public Resource getResource() {
        return super.getResource()
                .merge(Resource.create(Attributes.of(AttributeKey.longKey("telemetry.sdk.elastic_export_timestamp"), TimeUnit.MILLISECONDS.toNanos(System.currentTimeMillis()))));
    }
}