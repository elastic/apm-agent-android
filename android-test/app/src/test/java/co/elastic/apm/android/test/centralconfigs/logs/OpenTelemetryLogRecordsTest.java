package co.elastic.apm.android.test.centralconfigs.logs;

import static org.mockito.Mockito.doReturn;

import org.junit.Test;
import org.robolectric.annotation.Config;

import co.elastic.apm.android.sdk.internal.configuration.Configurations;
import co.elastic.apm.android.sdk.internal.configuration.impl.AllInstrumentationConfiguration;
import co.elastic.apm.android.sdk.logs.ElasticLoggers;
import co.elastic.apm.android.test.centralconfigs.common.AppUseCases;
import co.elastic.apm.android.test.testutils.base.BaseRobolectricTest;
import io.opentelemetry.api.logs.Logger;

public class OpenTelemetryLogRecordsTest extends BaseRobolectricTest {

    @Test
    public void whenRecordingIsEnabled_exportLogRecords() {
        Logger logger = ElasticLoggers.builder("someInstrumentation").build();

        logger.logRecordBuilder().setBody("something").emit();

        getRecordedLogs(1);
    }

    @Test
    public void whenRecordingIsNotEnabled_doNotExportLogRecords() {
        Logger logger = ElasticLoggers.builder("someInstrumentation").build();

        doReturn(false).when(Configurations.get(AllInstrumentationConfiguration.class)).isEnabled();

        logger.logRecordBuilder().setBody("something").emit();

        getRecordedLogs(0);
    }

    @Config(application = AppUseCases.AppWithSampleRateZero.class)
    @Test
    public void whenSampleRateIsZero_doNotExportLogRecords() {
        Logger logger = ElasticLoggers.builder("someInstrumentation").build();

        logger.logRecordBuilder().setBody("something").emit();

        getRecordedLogs(0);
    }
}
