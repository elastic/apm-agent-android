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
import co.elastic.apm.android.test.common.spans.SpanExporterCaptor;

public abstract class BaseEspressoTest<T extends Activity> extends BaseTest {
    private SpanExporterCaptor spanExporterCaptor;
    private IdlingResource idlingResource;

    @Rule
    public ActivityScenarioRule<T> activityScenarioRule = new ActivityScenarioRule<T>(getActivityClass());

    @Before
    public void setUp() {
        activityScenarioRule.getScenario().onActivity(activity -> {
            spanExporterCaptor = ((DefaultApp) activity.getApplication()).getSpanExporter();
            if (activity instanceof IdlingResourceProvider) {
                idlingResource = ((IdlingResourceProvider) activity).getIdlingResource();
                IdlingRegistry.getInstance().register(idlingResource);
            }
        });
    }

    @After
    public void cleanUp() {
        if (idlingResource != null) {
            IdlingRegistry.getInstance().unregister(idlingResource);
            idlingResource = null;
        }
    }

    @Override
    protected SpanExporterCaptor getSpanExporter() {
        return spanExporterCaptor;
    }

    protected abstract Class<T> getActivityClass();
}
