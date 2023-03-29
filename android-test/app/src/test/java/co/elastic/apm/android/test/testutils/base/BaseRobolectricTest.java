package co.elastic.apm.android.test.testutils.base;

import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import co.elastic.apm.android.sdk.internal.injection.AgentDependenciesInjector;
import co.elastic.apm.android.test.common.BaseTest;
import co.elastic.apm.android.test.common.logs.LogRecordExporterCaptor;
import co.elastic.apm.android.test.common.metrics.MetricExporterCaptor;
import co.elastic.apm.android.test.common.spans.SpanExporterCaptor;
import co.elastic.apm.android.test.providers.ExportersProvider;
import co.elastic.apm.android.test.testutils.MainApp;

@Config(application = MainApp.class)
@RunWith(RobolectricTestRunner.class)
public abstract class BaseRobolectricTest extends BaseTest {

    @Override
    protected SpanExporterCaptor getSpanExporter() {
        return getExporterProvider().getSpanExporter();
    }

    @Override
    protected LogRecordExporterCaptor getLogRecordExporter() {
        return getExporterProvider().getLogRecordExporter();
    }

    @Override
    protected MetricExporterCaptor getMetricExporter() {
        return getExporterProvider().getMetricExporter();
    }

    protected AgentDependenciesInjector getAgentDependenciesInjector() {
        return (AgentDependenciesInjector) RuntimeEnvironment.getApplication();
    }

    protected void spyOnServices() {
        ((BaseRobolectricTestApplication) RuntimeEnvironment.getApplication()).spyOnServices();
    }

    private ExportersProvider getExporterProvider() {
        return (ExportersProvider) RuntimeEnvironment.getApplication();
    }
}
