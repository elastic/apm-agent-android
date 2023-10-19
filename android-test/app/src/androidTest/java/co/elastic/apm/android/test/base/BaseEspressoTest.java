package co.elastic.apm.android.test.base;

import org.junit.After;
import org.junit.Before;

import co.elastic.apm.android.sdk.ElasticApmConfiguration;
import co.elastic.apm.android.test.DefaultApp;
import co.elastic.apm.android.test.common.BaseTest;
import co.elastic.apm.android.test.common.logs.LogRecordExporterCaptor;
import co.elastic.apm.android.test.common.metrics.MetricExporterCaptor;
import co.elastic.apm.android.test.common.spans.SpanExporterCaptor;

public class BaseEspressoTest extends BaseTest {
    private SpanExporterCaptor spanExporterCaptor;
    private LogRecordExporterCaptor logRecordExporterCaptor;
    private MetricExporterCaptor metricExporterCaptor;

    @Before
    public void baseSetUp() {
        DefaultApp application = getDefaultApp();
        spanExporterCaptor = application.getSpanExporter();
        logRecordExporterCaptor = application.getLogRecordExporter();
        metricExporterCaptor = application.getMetricExporter();
    }

    @After
    public void baseTearDown() {
        try {
            getDefaultApp().reset();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static DefaultApp getDefaultApp() {
        return (DefaultApp) TestRunner.application;
    }

    protected void overrideAgentConfiguration(ElasticApmConfiguration configuration) {
        try {
            getDefaultApp().reInitializeAgent(configuration);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected SpanExporterCaptor getSpanExporter() {
        return spanExporterCaptor;
    }

    @Override
    protected MetricExporterCaptor getMetricExporter() {
        return metricExporterCaptor;
    }

    @Override
    protected LogRecordExporterCaptor getLogRecordExporter() {
        return logRecordExporterCaptor;
    }
}
