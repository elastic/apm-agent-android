package co.elastic.apm.android.test.testutils.base;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import android.app.Activity;

import androidx.fragment.app.Fragment;

import org.mockito.ArgumentCaptor;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import co.elastic.apm.android.test.testutils.spans.SpanExporterProvider;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SpanExporter;

public class BaseTest {

    protected enum ActivityMethod {
        ON_CREATE("onCreate"),
        ON_RESUME("onResume"),
        ON_START("onStart");

        private final String name;

        ActivityMethod(String name) {
            this.name = name;
        }
    }

    protected enum FragmentMethod {
        ON_CREATE("onCreate"),
        ON_CREATE_VIEW("onCreateView"),
        ON_VIEW_CREATED("onViewCreated");

        private final String name;

        FragmentMethod(String name) {
            this.name = name;
        }
    }

    protected List<SpanData> getRecordedSpans(int amountExpected) {
        SpanExporter spanExporter = getSpanExporter();
        List<SpanData> spans = getCapturedSpansOrderedByCreation(spanExporter, amountExpected);
        assertEquals(amountExpected, spans.size());

        return spans;
    }

    @SuppressWarnings("unchecked")
    private List<SpanData> getCapturedSpansOrderedByCreation(SpanExporter spanExporter, int amountExpected) {
        List<SpanData> spans = new ArrayList<>();
        ArgumentCaptor<List<SpanData>> captor = ArgumentCaptor.forClass(List.class);
        verify(spanExporter, times(amountExpected)).export(captor.capture());
        for (List<SpanData> list : captor.getAllValues()) {
            if (list.size() > 1) {
                // Since we're using SimpleSpanProcessor, each call to SpanExporter.export must contain
                // only one span.
                throw new IllegalStateException();
            }
            spans.add(list.get(0));
        }

        spans.sort(Comparator.comparing(SpanData::getStartEpochNanos));
        return spans;
    }

    protected SpanExporter getSpanExporter() {
        SpanExporterProvider spanExporterProvider = (SpanExporterProvider) RuntimeEnvironment.getApplication();
        return spanExporterProvider.getSpanExporter();
    }

    protected String getClassSpanName(Class<?> theClass, String suffix) {
        return theClass.getName() + suffix;
    }

    protected String getSpanMethodName(Class<? extends Activity> activityClass, ActivityMethod method) {
        return getClassSpanName(activityClass, "->" + method.name);
    }

    protected String getSpanMethodName(Class<? extends Fragment> fragmentClass, FragmentMethod method) {
        return getClassSpanName(fragmentClass, "->" + method.name);
    }

    protected SpanData getRecordedSpan() {
        return getRecordedSpans(1).get(0);
    }
}