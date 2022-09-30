package co.elastic.apm.android.test.testutils.base;

import org.robolectric.RuntimeEnvironment;

import co.elastic.apm.android.test.common.BaseTest;
import co.elastic.apm.android.test.common.spans.SpanExporterCaptor;
import co.elastic.apm.android.test.utilities.SpanExporterProvider;

public class BaseRobolectricTest extends BaseTest {

    @Override
    protected SpanExporterCaptor getSpanExporter() {
        SpanExporterProvider spanExporterProvider = (SpanExporterProvider) RuntimeEnvironment.getApplication();
        return spanExporterProvider.getSpanExporter();
    }
}
