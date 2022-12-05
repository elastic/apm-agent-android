package co.elastic.apm.android.test.testutils.base;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.lang.reflect.Field;

import co.elastic.apm.android.sdk.internal.injection.AgentDependenciesInjector;
import co.elastic.apm.android.sdk.internal.time.ntp.NtpManager;
import co.elastic.apm.android.test.common.BaseTest;
import co.elastic.apm.android.test.common.spans.SpanExporterCaptor;
import co.elastic.apm.android.test.testutils.MainApp;
import co.elastic.apm.android.test.utilities.SpanExporterProvider;

@Config(application = MainApp.class)
@RunWith(RobolectricTestRunner.class)
public abstract class BaseRobolectricTest extends BaseTest {
    protected AgentDependenciesInjector injector;
    protected NtpManager ntpManager;

    @Before
    public void baseSetUp() throws NoSuchFieldException, IllegalAccessException {
        injector = mock(AgentDependenciesInjector.class);
        ntpManager = mock(NtpManager.class);
        doReturn(ntpManager).when(injector).getNtpManager();

        Field instanceField = AgentDependenciesInjector.class.getDeclaredField("INSTANCE");
        instanceField.setAccessible(true);
        instanceField.set(null, injector);
    }

    @Override
    protected SpanExporterCaptor getSpanExporter() {
        SpanExporterProvider spanExporterProvider = (SpanExporterProvider) RuntimeEnvironment.getApplication();
        return spanExporterProvider.getSpanExporter();
    }
}
