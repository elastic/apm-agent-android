package co.elastic.apm.android.test.attributes.logs;

import org.junit.Test;

import co.elastic.apm.android.sdk.internal.time.ntp.NtpManager;
import co.elastic.apm.android.test.common.logs.Logs;
import co.elastic.apm.android.test.testutils.TestElasticClock;
import co.elastic.apm.android.test.testutils.base.BaseRobolectricTest;
import io.opentelemetry.sdk.logs.data.LogRecordData;

public class ClockTest extends BaseRobolectricTest {

    @Test
    public void whenALogIsCreated_itHasTimestampSetFromElasticClock() {
        long startTimeFromElasticClock = 123456789;
        NtpManager ntpManager = getAgentDependenciesProvider().getNtpManager();
        TestElasticClock clock = (TestElasticClock) ntpManager.getClock();
        clock.setForcedNow(startTimeFromElasticClock);
        LogRecordData log = captureLog();

        Logs.verify(log)
                .startedAt(startTimeFromElasticClock);
    }

    private LogRecordData captureLog() {
        LogAttrHost host = new LogAttrHost();

        host.methodWithLog();

        return getRecordedLog();
    }
}
