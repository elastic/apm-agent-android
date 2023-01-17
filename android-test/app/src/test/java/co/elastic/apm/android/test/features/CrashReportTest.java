package co.elastic.apm.android.test.features;

import org.junit.Test;

import java.io.PrintWriter;
import java.io.StringWriter;

import co.elastic.apm.android.test.common.logs.Logs;
import co.elastic.apm.android.test.testutils.base.BaseRobolectricTest;
import io.opentelemetry.sdk.logs.data.LogRecordData;

public class CrashReportTest extends BaseRobolectricTest {

    @Test
    public void whenCrashHappens_captureLogEvent() {
        IllegalStateException exception = new IllegalStateException("Custom exception");
        Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), exception);

        LogRecordData log = getRecordedLog();

        Logs.verifyException(log)
                .isNamed("crash")
                .hasExceptionMessage("Custom exception")
                .hasStacktrace(stackTraceToString(exception))
                .hasExceptionType("java.lang.IllegalStateException");
    }

    private String stackTraceToString(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        throwable.printStackTrace(pw);
        pw.flush();

        return sw.toString();
    }
}
