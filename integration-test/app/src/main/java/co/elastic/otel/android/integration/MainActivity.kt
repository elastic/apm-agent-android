package co.elastic.otel.android.integration

import android.app.Activity
import android.os.Bundle
import co.elastic.otel.android.extensions.log
import co.elastic.otel.android.extensions.span
import co.elastic.otel.android.integration.MyApp.Companion.agent
import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.api.logs.Severity
import io.opentelemetry.api.trace.SpanKind
import io.opentelemetry.api.trace.StatusCode

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val otel = agent.getOpenTelemetry()

        exerciseTracingApi(otel)
        exerciseLogsApi(otel)
        exerciseMetricsApi(otel)

        agent.span("span name") {
            agent.log("log body")
        }
    }

    private fun exerciseTracingApi(otel: io.opentelemetry.api.OpenTelemetry) {
        val tracer = otel.getTracer("integration-tracer")
        val attrs = Attributes.of(AttributeKey.stringKey("key"), "value")

        val spanBuilder = tracer.spanBuilder("api-coverage-span")
        spanBuilder.setAllAttributes(attrs)
        spanBuilder.setAttribute("str-attr", "v")
        spanBuilder.setAttribute("long-attr", 1L)
        spanBuilder.setAttribute("double-attr", 1.0)
        spanBuilder.setAttribute("bool-attr", true)
        spanBuilder.setAttribute(AttributeKey.longKey("typed-long"), 42)
        spanBuilder.setSpanKind(SpanKind.INTERNAL)

        val span = spanBuilder.startSpan()
        span.setAllAttributes(attrs)
        span.setAttribute("span-str", "v")
        span.setAttribute("span-long", 1L)
        span.setAttribute("span-double", 1.0)
        span.setAttribute("span-bool", true)
        span.setAttribute(AttributeKey.longKey("span-typed-long"), 42)
        span.addEvent("event-name")
        span.addEvent("event-with-attrs", attrs)
        span.setStatus(StatusCode.OK)
        span.recordException(RuntimeException("test"))
        span.end()
    }

    private fun exerciseLogsApi(otel: io.opentelemetry.api.OpenTelemetry) {
        val logger = otel.logsBridge.get("integration-logger")
        val attrs = Attributes.of(AttributeKey.stringKey("log-key"), "log-value")

        val builder = logger.logRecordBuilder()
        builder.setAllAttributes(attrs)
        builder.setAttribute("log-str", "v")
        builder.setAttribute("log-long", 1L)
        builder.setAttribute("log-double", 1.0)
        builder.setAttribute("log-bool", true)
        builder.setBody("test body")
        builder.setSeverity(Severity.INFO)
        builder.setSeverityText("INFO")
        builder.emit()
    }

    private fun exerciseMetricsApi(otel: io.opentelemetry.api.OpenTelemetry) {
        val meter = otel.getMeter("integration-meter")
        val attrs = Attributes.of(AttributeKey.stringKey("metric-key"), "metric-value")

        meter.counterBuilder("test-counter").build().add(1, attrs)
        meter.counterBuilder("test-double-counter").ofDoubles().build().add(1.0, attrs)
        meter.upDownCounterBuilder("test-updown").build().add(-1, attrs)
        meter.upDownCounterBuilder("test-double-updown").ofDoubles().build().add(-1.0, attrs)
        meter.histogramBuilder("test-histogram").build().record(1.0, attrs)
        meter.histogramBuilder("test-long-histogram").ofLongs().build().record(1, attrs)
        meter.gaugeBuilder("test-gauge").build().set(1.0, attrs)
        meter.gaugeBuilder("test-long-gauge").ofLongs().build().set(1, attrs)
    }
}