package co.elastic.apm.android.test.attributes.logs;

import static co.elastic.apm.android.test.attributes.common.ResourcesApp.DEVICE_MANUFACTURER;
import static co.elastic.apm.android.test.attributes.common.ResourcesApp.DEVICE_MODEL_NAME;
import static co.elastic.apm.android.test.attributes.common.ResourcesApp.RUNTIME_VERSION;

import org.junit.Test;
import org.robolectric.annotation.Config;

import co.elastic.apm.android.test.BuildConfig;
import co.elastic.apm.android.test.attributes.common.ResourcesApp;
import co.elastic.apm.android.test.common.logs.Logs;
import co.elastic.apm.android.test.testutils.base.BaseRobolectricTest;
import io.opentelemetry.sdk.logs.data.LogRecordData;

@Config(application = ResourcesApp.class)
public class ResourcesTest extends BaseRobolectricTest {

    @Test
    public void whenALogIsCreated_serviceNameIsSet() {
        LogRecordData log = captureLog();

        Logs.verify(log)
                .hasResource("service.name", "my-app");
    }

    @Test
    public void whenALogIsCreated_serviceVersionIsSet() {
        LogRecordData log = captureLog();

        Logs.verify(log)
                .hasResource("service.version", "1.0");
    }

    @Test
    public void whenALogIsCreated_agentNameIsSet() {
        LogRecordData log = captureLog();

        Logs.verify(log)
                .hasResource("telemetry.sdk.name", "android");
    }

    @Test
    public void whenALogIsCreated_agentVersionIsSet() {
        LogRecordData log = captureLog();

        Logs.verify(log)
                .hasResource("telemetry.sdk.version", System.getProperty("agentVersion"));
    }

    @Test
    public void whenALogIsCreated_osDescriptionIsSet() {
        LogRecordData log = captureLog();

        Logs.verify(log)
                .hasResource("os.description", "Android 12, API level 32, BUILD unknown");
    }

    @Test
    public void whenALogIsCreated_osNameIsSet() {
        LogRecordData log = captureLog();

        Logs.verify(log)
                .hasResource("os.name", "Android");
    }

    @Test
    public void whenALogIsCreated_osVersionSet() {
        LogRecordData log = captureLog();

        Logs.verify(log)
                .hasResource("os.version", "12");
    }

    @Test
    public void whenALogIsCreated_deploymentEnvironmentIsSet() {
        LogRecordData log = captureLog();

        Logs.verify(log)
                .hasResource("deployment.environment", (BuildConfig.DEBUG) ? "debug" : "release");
    }

    @Test
    public void whenALogIsCreated_deviceIdIsSet() {
        LogRecordData log = captureLog();

        Logs.verify(log)
                .hasResource("device.id");
    }

    @Test
    public void whenALogIsCreated_deviceModelIdIsSet() {
        LogRecordData log = captureLog();

        Logs.verify(log)
                .hasResource("device.model.identifier", DEVICE_MODEL_NAME);
    }

    @Test
    public void whenALogIsCreated_deviceModelManufacturerIsSet() {
        LogRecordData log = captureLog();

        Logs.verify(log)
                .hasResource("device.manufacturer", DEVICE_MANUFACTURER);
    }

    @Test
    public void whenALogIsCreated_processRuntimeNameIsSet() {
        LogRecordData log = captureLog();

        Logs.verify(log)
                .hasResource("process.runtime.name", "Android Runtime");
    }

    @Test
    public void whenALogIsCreated_processRuntimeVersionIsSet() {
        LogRecordData log = captureLog();

        Logs.verify(log)
                .hasResource("process.runtime.version", RUNTIME_VERSION);
    }

    private LogRecordData captureLog() {
        LogAttrHost host = new LogAttrHost();

        host.methodWithLog();

        return getRecordedLog();
    }
}
