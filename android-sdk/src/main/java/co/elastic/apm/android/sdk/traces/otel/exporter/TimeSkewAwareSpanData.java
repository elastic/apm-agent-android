package co.elastic.apm.android.sdk.traces.otel.exporter;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.data.EventData;
import io.opentelemetry.sdk.trace.data.LinkData;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.data.StatusData;

class TimeSkewAwareSpanData implements SpanData {

    private final SpanData original;

    TimeSkewAwareSpanData(SpanData original) {
        this.original = original;
    }

    @Override
    public String getName() {
        return original.getName();
    }

    @Override
    public SpanKind getKind() {
        return original.getKind();
    }

    @Override
    public SpanContext getSpanContext() {
        return original.getSpanContext();
    }

    @Override
    public SpanContext getParentSpanContext() {
        return original.getParentSpanContext();
    }

    @Override
    public StatusData getStatus() {
        return original.getStatus();
    }

    @Override
    public long getStartEpochNanos() {
        return original.getStartEpochNanos();
    }

    @Override
    public Attributes getAttributes() {
        return original.getAttributes();
    }

    @Override
    public List<EventData> getEvents() {
        return original.getEvents();
    }

    @Override
    public List<LinkData> getLinks() {
        return original.getLinks();
    }

    @Override
    public long getEndEpochNanos() {
        return original.getEndEpochNanos();
    }

    @Override
    public boolean hasEnded() {
        return original.hasEnded();
    }

    @Override
    public int getTotalRecordedEvents() {
        return original.getTotalRecordedEvents();
    }

    @Override
    public int getTotalRecordedLinks() {
        return original.getTotalRecordedLinks();
    }

    @Override
    public int getTotalAttributeCount() {
        return original.getTotalAttributeCount();
    }

    @Override
    public InstrumentationLibraryInfo getInstrumentationLibraryInfo() {
        return original.getInstrumentationLibraryInfo();
    }

    @Override
    public InstrumentationScopeInfo getInstrumentationScopeInfo() {
        return original.getInstrumentationScopeInfo();
    }

    @Override
    public Resource getResource() {
        return original.getResource()
                .merge(Resource.create(Attributes.of(AttributeKey.longKey("telemetry.sdk.elastic_export_timestamp"), TimeUnit.MILLISECONDS.toNanos(System.currentTimeMillis()))));
    }
}