package co.elastic.apm.android.test.spanattributes;

import android.os.Build;

import org.junit.Test;
import org.robolectric.annotation.Config;
import org.robolectric.util.ReflectionHelpers;

import java.util.List;

import co.elastic.apm.android.test.BuildConfig;
import co.elastic.apm.android.test.common.spans.Spans;
import co.elastic.apm.android.test.testutils.MainApp;
import co.elastic.apm.android.test.testutils.base.BaseRobolectricTest;
import io.opentelemetry.sdk.trace.data.SpanData;

@Config(application = ResourcesTest.ResourcesApp.class)
public class ResourcesTest extends BaseRobolectricTest {

    public static final String RUNTIME_VERSION = "0.0.0";
    public static final String DEVICE_MODEL_NAME = "Universe E10";
    public static final String DEVICE_MANUFACTURER = "Droidlastic";

    @Test
    public void whenASpanIsCreated_serviceNameIsSet() {
        SpanData customSpan = captureSpan();

        Spans.verify(customSpan)
                .hasResource("service.name", "my-app");
    }

    @Test
    public void whenASpanIsCreated_serviceVersionIsSet() {
        SpanData customSpan = captureSpan();

        Spans.verify(customSpan)
                .hasResource("service.version", "1.0");
    }

    @Test
    public void whenASpanIsCreated_agentNameIsSet() {
        SpanData customSpan = captureSpan();

        Spans.verify(customSpan)
                .hasResource("telemetry.sdk.name", "android");
    }

    @Test
    public void whenASpanIsCreated_agentVersionIsSet() {
        SpanData customSpan = captureSpan();

        Spans.verify(customSpan)
                .hasResource("telemetry.sdk.version", System.getProperty("agentVersion"));
    }

    @Test
    public void whenASpanIsCreated_osDescriptionIsSet() {
        SpanData customSpan = captureSpan();

        Spans.verify(customSpan)
                .hasResource("os.description", "Android 12, API level 32, BUILD unknown");
    }

    @Test
    public void whenASpanIsCreated_osNameIsSet() {
        SpanData customSpan = captureSpan();

        Spans.verify(customSpan)
                .hasResource("os.name", "Android");
    }

    @Test
    public void whenASpanIsCreated_osVersionSet() {
        SpanData customSpan = captureSpan();

        Spans.verify(customSpan)
                .hasResource("os.version", "12");
    }

    @Test
    public void whenASpanIsCreated_deploymentEnvironmentIsSet() {
        SpanData customSpan = captureSpan();

        Spans.verify(customSpan)
                .hasResource("deployment.environment", (BuildConfig.DEBUG) ? "debug" : "release");
    }

    @Test
    public void whenASpanIsCreated_deviceIdIsSet() {
        SpanData customSpan = captureSpan();

        Spans.verify(customSpan)
                .hasResource("device.id");
    }

    @Test
    public void whenASpanIsCreated_deviceModelIdIsSet() {
        SpanData customSpan = captureSpan();

        Spans.verify(customSpan)
                .hasResource("device.model.identifier", DEVICE_MODEL_NAME);
    }

    @Test
    public void whenASpanIsCreated_deviceModelManufacturerIsSet() {
        SpanData customSpan = captureSpan();

        Spans.verify(customSpan)
                .hasResource("device.manufacturer", DEVICE_MANUFACTURER);
    }

    @Test
    public void whenASpanIsCreated_processRuntimeNameIsSet() {
        SpanData customSpan = captureSpan();

        Spans.verify(customSpan)
                .hasResource("process.runtime.name", "Android Runtime");
    }

    @Test
    public void whenASpanIsCreated_processRuntimeVersionIsSet() {
        SpanData customSpan = captureSpan();

        Spans.verify(customSpan)
                .hasResource("process.runtime.version", RUNTIME_VERSION);
    }

    @Test
    public void whenASpanIsCreated_typeIsSet() {
        SpanData customSpan = captureSpan();

        Spans.verify(customSpan)
                .hasResource("type", "mobile");
    }

    private SpanData captureSpan() {
        SpanAttrHost host = new SpanAttrHost();

        host.methodWithSpan();

        List<SpanData> spans = getRecordedSpans(1);
        return spans.get(0);
    }

    public static class ResourcesApp extends MainApp {
        @Override
        public void onCreate() {
            ReflectionHelpers.setStaticField(Build.class, "MODEL", DEVICE_MODEL_NAME);
            ReflectionHelpers.setStaticField(Build.class, "MANUFACTURER", DEVICE_MANUFACTURER);
            System.setProperty("java.vm.version", RUNTIME_VERSION);
            super.onCreate();
        }
    }
}