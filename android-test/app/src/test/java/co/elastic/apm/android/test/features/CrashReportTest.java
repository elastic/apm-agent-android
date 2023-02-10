package co.elastic.apm.android.test.features;

import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.mockito.Mockito;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.io.PrintWriter;
import java.io.StringWriter;

import co.elastic.apm.android.test.common.logs.Logs;
import co.elastic.apm.android.test.testutils.base.BaseRobolectricTest;
import co.elastic.apm.android.test.testutils.base.BaseRobolectricTestApplication;
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

    @Config(application = ApplicationWithExistingExceptionHandler.class)
    @Test
    public void whenCrashHappens_and_thereIsAnExistingExceptionHandler_delegateToIt() {
        IllegalStateException exception = new IllegalStateException("Custom exception");
        Thread.UncaughtExceptionHandler elasticHandler = Thread.getDefaultUncaughtExceptionHandler();
        elasticHandler.uncaughtException(Thread.currentThread(), exception);

        ApplicationWithExistingExceptionHandler app = (ApplicationWithExistingExceptionHandler) RuntimeEnvironment.getApplication();

        assertNotEquals(elasticHandler, app.originalExceptionHandler);
        verify(app.originalExceptionHandler).uncaughtException(Thread.currentThread(), exception);
    }

    public static class ApplicationWithExistingExceptionHandler extends BaseRobolectricTestApplication {
        public Thread.UncaughtExceptionHandler originalExceptionHandler;

        @Override
        public void onCreate() {
            super.onCreate();
            originalExceptionHandler = Mockito.mock(Thread.UncaughtExceptionHandler.class);
            Thread.setDefaultUncaughtExceptionHandler(originalExceptionHandler);
            initializeAgent();
        }
    }

    private String stackTraceToString(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        throwable.printStackTrace(pw);
        pw.flush();

        return sw.toString();
    }
}
