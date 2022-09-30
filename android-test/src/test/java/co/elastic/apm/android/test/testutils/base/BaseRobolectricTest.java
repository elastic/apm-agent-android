package co.elastic.apm.android.test.testutils.base;

import org.robolectric.RuntimeEnvironment;

import co.elastic.apm.android.test.BaseTest;
import co.elastic.apm.android.test.utilities.SpanExporterProvider;
import co.elastic.apm.android.test.utilities.DummySpanExporter;

public class BaseRobolectricTest extends BaseTest {

    @Override
    protected DummySpanExporter getSpanExporter() {
        SpanExporterProvider spanExporterProvider = (SpanExporterProvider) RuntimeEnvironment.getApplication();
        return spanExporterProvider.getSpanExporter();
    }
}
