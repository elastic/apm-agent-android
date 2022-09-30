package co.elastic.apm.android.test.testutils.base;

import android.app.Application;

import org.robolectric.TestLifecycleApplication;

import java.lang.reflect.Method;

import co.elastic.apm.android.sdk.ElasticApmAgent;
import co.elastic.apm.android.sdk.traces.connectivity.Connectivity;
import co.elastic.apm.android.test.utilities.DummySpanExporter;
import co.elastic.apm.android.test.utilities.SpanExporterProvider;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;

public class BaseRobolectricTestApplication extends Application implements SpanExporterProvider, TestLifecycleApplication {
    private final DummySpanExporter exporter;

    public BaseRobolectricTestApplication() {
        exporter = new DummySpanExporter();
    }

    @Override
    public DummySpanExporter getSpanExporter() {
        return exporter;
    }

    protected Connectivity getConnectivity() {
        return Connectivity.custom(SimpleSpanProcessor.create(exporter));
    }

    @Override
    public void beforeTest(Method method) {

    }

    @Override
    public void prepareTest(Object test) {

    }

    @Override
    public void afterTest(Method method) {
        ElasticApmAgent.get().destroy();
        GlobalOpenTelemetry.resetForTest();
    }
}
