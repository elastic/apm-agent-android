package co.elastic.apm.android.test.testutils.base;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import android.app.Application;

import org.robolectric.TestLifecycleApplication;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import co.elastic.apm.android.sdk.ElasticApmAgent;
import co.elastic.apm.android.sdk.internal.injection.AgentDependenciesInjector;
import co.elastic.apm.android.sdk.internal.time.ntp.NtpManager;
import co.elastic.apm.android.sdk.traces.connectivity.Connectivity;
import co.elastic.apm.android.test.common.spans.SpanExporterCaptor;
import co.elastic.apm.android.test.testutils.AgentDependenciesProvider;
import co.elastic.apm.android.test.testutils.TestElasticClock;
import co.elastic.apm.android.test.utilities.SpanExporterProvider;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;

public class BaseRobolectricTestApplication extends Application implements SpanExporterProvider, TestLifecycleApplication, AgentDependenciesProvider {
    private final SpanExporterCaptor exporter;
    private AgentDependenciesInjector injector;
    private NtpManager ntpManager;

    public BaseRobolectricTestApplication() {
        exporter = new SpanExporterCaptor();
        setUpAgentDependencies();
    }

    private void setUpAgentDependencies() {
        injector = mock(AgentDependenciesInjector.class);
        Clock clock = new TestElasticClock();
        ntpManager = mock(NtpManager.class);
        doReturn(clock).when(ntpManager).getClock();
        doReturn(ntpManager).when(injector).getNtpManager();

        try {
            Field instanceField = AgentDependenciesInjector.class.getDeclaredField("INSTANCE");
            instanceField.setAccessible(true);
            instanceField.set(null, injector);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public SpanExporterCaptor getSpanExporter() {
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

    @Override
    public NtpManager getNtpManager() {
        return ntpManager;
    }
}
