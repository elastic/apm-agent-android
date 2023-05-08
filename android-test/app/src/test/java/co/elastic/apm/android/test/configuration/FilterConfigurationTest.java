package co.elastic.apm.android.test.configuration;

import org.junit.Test;
import org.robolectric.annotation.Config;

import java.util.List;
import java.util.Objects;

import co.elastic.apm.android.sdk.ElasticApmConfiguration;
import co.elastic.apm.android.sdk.logs.ElasticLoggers;
import co.elastic.apm.android.sdk.metrics.ElasticMeters;
import co.elastic.apm.android.sdk.traces.ElasticTracers;
import co.elastic.apm.android.test.common.logs.Logs;
import co.elastic.apm.android.test.common.metrics.Metrics;
import co.elastic.apm.android.test.common.spans.Spans;
import co.elastic.apm.android.test.testutils.base.BaseRobolectricTest;
import co.elastic.apm.android.test.testutils.base.BaseRobolectricTestApplication;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.logs.Logger;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.trace.data.SpanData;

public class FilterConfigurationTest extends BaseRobolectricTest {

    @Config(application = SignalsFilteredApp.class)
    @Test
    public void verifySpanFilter() {
        Tracer tracer = ElasticTracers.create("someTracer");

        Span spanOne = tracer.spanBuilder("spanOne").startSpan();
        spanOne.setAttribute("includeSpan", false);

        Span spanTwo = tracer.spanBuilder("spanTwo").startSpan();
        spanTwo.setAttribute("includeSpan", true);

        spanOne.end();
        spanTwo.end();

        List<SpanData> recordedSpans = getRecordedSpans(1);

        Spans.verify(recordedSpans.get(0))
                .isNamed("spanTwo");
    }

    @Config(application = SignalsFilteredApp.class)
    @Test
    public void verifyLogRecordFilter() {
        Logger logger = ElasticLoggers.builder("LoggerScope").build();

        logger.logRecordBuilder()
                .setBody("first log")
                .setAttribute(AttributeKey.booleanKey("includeLog"), false).emit();

        logger.logRecordBuilder()
                .setBody("second log")
                .setAttribute(AttributeKey.booleanKey("includeLog"), true).emit();

        List<LogRecordData> recordedLogs = getRecordedLogs(1);

        Logs.verifyRecord(recordedLogs.get(0))
                .hasBody("second log");
    }

    @Config(application = SignalsFilteredApp.class)
    @Test
    public void verifyMetricFilter() {
        Meter meter = ElasticMeters.create("someMeter");

        meter.counterBuilder("includeMetric").build().add(1);
        meter.counterBuilder("secondCounter").build().add(1);

        flushMetrics();
        List<MetricData> metrics = getRecordedMetrics(1);

        Metrics.verify(metrics.get(0))
                .isNamed("includeMetric");
    }

    private static class SignalsFilteredApp extends BaseRobolectricTestApplication {
        @Override
        public void onCreate() {
            initializeAgentWithCustomConfig(ElasticApmConfiguration.builder()
                    .addSpanFilter(readableSpan -> Boolean.TRUE.equals(readableSpan.getAttribute(AttributeKey.booleanKey("includeSpan"))))
                    .addLogFilter(logRecordData -> Boolean.TRUE.equals(logRecordData.getAttributes().get(AttributeKey.booleanKey("includeLog"))))
                    .addMetricFilter(metricData -> Objects.equals(metricData.getName(), "includeMetric"))
                    .build());
        }
    }
}
