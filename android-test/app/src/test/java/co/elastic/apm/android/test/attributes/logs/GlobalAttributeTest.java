package co.elastic.apm.android.test.attributes.logs;

import org.junit.Test;
import org.robolectric.annotation.Config;

import co.elastic.apm.android.test.attributes.traces.common.AppsWithConnectivity;
import co.elastic.apm.android.test.common.logs.Logs;
import co.elastic.apm.android.test.testutils.base.BaseRobolectricTest;
import io.opentelemetry.sdk.logs.data.LogRecordData;

public class GlobalAttributeTest extends BaseRobolectricTest {

    @Test
    public void whenALogIsCreated_verifyItHasSessionIdAsParam() {
        LogRecordData log = captureLog();

        Logs.verifyRecord(log)
                .hasAttribute("session.id");
    }

    @Config(application = AppsWithConnectivity.WithWifi.class)
    @Test
    public void whenALogIsCreated_andThereIsWifiConnectivity_verifyItHasWifiConnectivityParam() {
        LogRecordData log = captureLog();

        Logs.verifyRecord(log)
                .hasAttribute("net.host.connection.type", "wifi");
    }

    @Config(application = AppsWithConnectivity.WithCellular.class)
    @Test
    public void whenALogIsCreated_andThereIsCellularConnectivity_verifyItHasCellularConnectivityParam() {
        LogRecordData log = captureLog();

        Logs.verifyRecord(log)
                .hasAttribute("net.host.connection.type", "cell");
    }

    @Config(application = AppsWithConnectivity.WithCellularAndSubtype.class)
    @Test
    public void whenALogIsCreated_andThereIsCellularConnectivityWithSubtype_verifyItHasCellularConnectivityParam() {
        LogRecordData log = captureLog();

        Logs.verifyRecord(log)
                .hasAttribute("net.host.connection.type", "cell")
                .hasAttribute("net.host.connection.subtype", "EDGE");
    }

    private LogRecordData captureLog() {
        LogAttrHost host = new LogAttrHost();

        host.methodWithLog();

        return getRecordedLog();
    }
}
