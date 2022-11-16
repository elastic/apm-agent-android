package co.elastic.apm.android.test.testutils.base;

import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import co.elastic.apm.android.test.common.BaseTest;
import co.elastic.apm.android.test.common.spans.SpanExporterCaptor;
import co.elastic.apm.android.test.testutils.MainApp;
import co.elastic.apm.android.test.utilities.SpanExporterProvider;

@Config(application = MainApp.class)
@RunWith(RobolectricTestRunner.class)
public abstract class BaseRobolectricTest extends BaseTest {

    @Override
    protected SpanExporterCaptor getSpanExporter() {
        SpanExporterProvider spanExporterProvider = (SpanExporterProvider) RuntimeEnvironment.getApplication();
        return spanExporterProvider.getSpanExporter();
    }
}
