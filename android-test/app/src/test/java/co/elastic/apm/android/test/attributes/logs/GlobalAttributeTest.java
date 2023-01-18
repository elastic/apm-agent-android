package co.elastic.apm.android.test.attributes.logs;

import org.junit.Test;

import co.elastic.apm.android.test.common.logs.Logs;
import co.elastic.apm.android.test.testutils.base.BaseRobolectricTest;
import io.opentelemetry.sdk.logs.data.LogRecordData;

public class GlobalAttributeTest extends BaseRobolectricTest {

    @Test
    public void whenALogIsCreated_verifyItHasSessionIdAsParam() {
        LogRecordData log = captureLog();

        Logs.verify(log)
                .hasAttribute("session.id");
    }

    private LogRecordData captureLog() {
        LogAttrHost host = new LogAttrHost();

        host.methodWithLog();

        return getRecordedLog();
    }
}
