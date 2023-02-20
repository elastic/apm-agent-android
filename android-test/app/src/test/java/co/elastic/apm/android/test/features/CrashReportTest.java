package co.elastic.apm.android.test.features;

import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.verify;

import androidx.annotation.NonNull;

import org.junit.Test;
import org.mockito.Mockito;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.io.PrintWriter;
import java.io.StringWriter;

import co.elastic.apm.android.sdk.ElasticApmConfiguration;
import co.elastic.apm.android.sdk.instrumentation.InstrumentationConfiguration;
import co.elastic.apm.android.test.common.logs.Logs;
import co.elastic.apm.android.test.testutils.AppWithoutInitializedAgent;
import co.elastic.apm.android.test.testutils.base.BaseRobolectricTest;
import co.elastic.apm.android.test.testutils.base.BaseRobolectricTestApplication;
import io.opentelemetry.sdk.logs.data.LogRecordData;

public class CrashReportTest extends BaseRobolectricTest {

    @Test
    public void whenCrashHappens_captureLogEvent() {
        Exception exception = new IllegalStateException("Custom exception");
        throwException(exception);

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
        Thread.UncaughtExceptionHandler elasticHandler = throwException(exception);

        ApplicationWithExistingExceptionHandler app = (ApplicationWithExistingExceptionHandler) RuntimeEnvironment.getApplication();

        assertNotEquals(elasticHandler, app.originalExceptionHandler);
        verify(app.originalExceptionHandler).uncaughtException(Thread.currentThread(), exception);
    }

    @Config(application = AppWithInstrumentationDisabled.class)
    @Test
    public void whenInstrumentationIsDisabled_doNotSendCrashReport() {
        throwException();

        getRecordedLogs(0);
    }

    @Config(application = AppWithoutInitializedAgent.class)
    @Test
    public void whenTheAgentIsNotInitialized_doNotSendCrashReport() {
        throwException();

        getRecordedLogs(0);
    }

    private Thread.UncaughtExceptionHandler throwException() {
        return throwException(new IllegalStateException("Custom exception"));
    }

    @NonNull
    private Thread.UncaughtExceptionHandler throwException(Exception exception) {
        Thread.UncaughtExceptionHandler exceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        if (exceptionHandler != null) {
            exceptionHandler.uncaughtException(Thread.currentThread(), exception);
        }
        return exceptionHandler;
    }

    private static class AppWithInstrumentationDisabled extends BaseRobolectricTestApplication {
        @Override
        public void onCreate() {
            super.onCreate();
            ElasticApmConfiguration configuration = ElasticApmConfiguration.builder()
                    .setInstrumentationConfiguration(InstrumentationConfiguration.builder().enableCrashReporting(false).build())
                    .build();

            initializeAgentWithCustomConfig(configuration);
        }
    }

    private static class ApplicationWithExistingExceptionHandler extends BaseRobolectricTestApplication {
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
