package co.elastic.apm.android.test.common.logs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;

public class LogRecordExporterCaptor implements LogRecordExporter {
    private final List<List<LogRecordData>> capturedLogs = new ArrayList<>();

    @Override
    public CompletableResultCode export(Collection<LogRecordData> logs) {
        capturedLogs.add(new ArrayList<>(logs));
        return CompletableResultCode.ofSuccess();
    }

    @Override
    public CompletableResultCode flush() {
        return CompletableResultCode.ofSuccess();
    }

    @Override
    public CompletableResultCode shutdown() {
        return CompletableResultCode.ofSuccess();
    }

    public List<List<LogRecordData>> getCapturedLogs() {
        return Collections.unmodifiableList(capturedLogs);
    }

    public void clearCapturedLogs() {
        capturedLogs.clear();
    }
}
