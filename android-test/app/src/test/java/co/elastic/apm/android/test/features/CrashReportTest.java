package co.elastic.apm.android.test.features;

import org.junit.Test;
import org.robolectric.Robolectric;
import org.robolectric.android.controller.ActivityController;

import java.io.PrintWriter;
import java.io.StringWriter;

import co.elastic.apm.android.test.activities.ErrorActivity;
import co.elastic.apm.android.test.common.logs.Logs;
import co.elastic.apm.android.test.testutils.base.BaseRobolectricTest;
import io.opentelemetry.sdk.logs.data.LogRecordData;

public class CrashReportTest extends BaseRobolectricTest {

    @Test
    public void whenCrashHappens_captureLogEvent() {
        try (ActivityController<ErrorActivity> controller = Robolectric.buildActivity(ErrorActivity.class)) {
            try {
                controller.setup();
            } catch (IllegalStateException e) {
                LogRecordData log = getRecordedLog();

                Logs.verifyException(log)
                        .isNamed("crash")
                        .hasExceptionMessage("Creating exception")
                        .hasExceptionType(IllegalStateException.class.getName())
                        .hasStacktrace(stackTraceToString(e));
            }
        }
    }

    public String stackTraceToString(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        throwable.printStackTrace(pw);
        pw.flush();

        return sw.toString();
    }
}
