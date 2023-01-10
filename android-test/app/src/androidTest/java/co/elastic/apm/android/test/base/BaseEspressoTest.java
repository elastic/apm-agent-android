package co.elastic.apm.android.test.base;

import android.app.Activity;

import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.IdlingResource;
import androidx.test.ext.junit.rules.ActivityScenarioRule;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;

import co.elastic.apm.android.test.DefaultApp;
import co.elastic.apm.android.test.activities.espresso.IdlingResourceProvider;
import co.elastic.apm.android.test.common.BaseTest;
import co.elastic.apm.android.test.common.metrics.MetricExporterCaptor;
import co.elastic.apm.android.test.common.spans.SpanExporterCaptor;

public abstract class BaseEspressoTest<T extends Activity> extends BaseTest {
    private SpanExporterCaptor spanExporterCaptor;
    private MetricExporterCaptor metricExporterCaptor;
    private IdlingResource idlingResource;
    protected T activity;

    @Rule
    public ActivityScenarioRule<T> activityScenarioRule = new ActivityScenarioRule<>(getActivityClass());

    @Before
    public void baseSetUp() {
        onBefore();
        activityScenarioRule.getScenario().onActivity(activity -> {
            DefaultApp application = (DefaultApp) activity.getApplication();
            spanExporterCaptor = application.getSpanExporter();
            metricExporterCaptor = application.getMetricExporter();
            if (activity instanceof IdlingResourceProvider) {
                idlingResource = ((IdlingResourceProvider) activity).getIdlingResource();
                IdlingRegistry.getInstance().register(idlingResource);
            }
            this.activity = activity;
            onActivity(activity);
        });
    }

    protected void onActivity(T activity) {

    }

    protected void onBefore() {

    }

    protected void onAfter() {

    }

    @After
    public void cleanUp() {
        onAfter();
        if (idlingResource != null) {
            IdlingRegistry.getInstance().unregister(idlingResource);
            idlingResource = null;
        }
    }

    @Override
    protected SpanExporterCaptor getSpanExporter() {
        return spanExporterCaptor;
    }

    @Override
    protected MetricExporterCaptor getMetricExporter() {
        return metricExporterCaptor;
    }

    protected abstract Class<T> getActivityClass();
}
