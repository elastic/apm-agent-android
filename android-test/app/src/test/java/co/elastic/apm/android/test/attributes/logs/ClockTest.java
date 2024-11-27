package co.elastic.apm.android.test.attributes.logs;

import static org.mockito.Mockito.doReturn;

import org.junit.Test;

import co.elastic.apm.android.test.common.logs.Logs;
import co.elastic.apm.android.test.testutils.base.BaseRobolectricTest;
import io.opentelemetry.sdk.logs.data.LogRecordData;

public class ClockTest extends BaseRobolectricTest {

    @Test
    public void whenALogIsCreated_itHasTimestampSetFromElasticClock() {
        long startTimeFromElasticClock = 123456789;
        setNow(startTimeFromElasticClock);
        LogRecordData log = captureLog();

        Logs.verifyRecord(log)
                .startedAt(startTimeFromElasticClock);
    }

    @Test
    public void whenAnEventIsCreated_itHasTimestampSetFromElasticClock() {
        long startTimeFromElasticClock = 123456789;
        setNow(startTimeFromElasticClock);
        LogRecordData event = captureEvent();

        Logs.verifyRecord(event)
                .startedAt(startTimeFromElasticClock);
    }

    private void setNow(long now) {
        doReturn(now).when(getAgentDependenciesInjector().getElasticClock()).now();
    }

    private LogRecordData captureLog() {
        LogAttrHost host = new LogAttrHost();

        host.methodWithLog();

        return getRecordedLog();
    }

    private LogRecordData captureEvent() {
        LogAttrHost host = new LogAttrHost();

        host.methodWithEvent();

        return getRecordedLog();
    }
}
