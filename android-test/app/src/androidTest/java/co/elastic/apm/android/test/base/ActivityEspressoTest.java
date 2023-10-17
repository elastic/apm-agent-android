package co.elastic.apm.android.test.base;

import android.app.Activity;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.IdlingResource;
import androidx.test.ext.junit.rules.ActivityScenarioRule;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;

import co.elastic.apm.android.test.activities.espresso.IdlingResourceProvider;

public abstract class ActivityEspressoTest<T extends Activity> extends BaseEspressoTest {
    private IdlingResource idlingResource;
    protected T activity;

    @Before
    public void activitySetUp() {
        onBefore();
        try (ActivityScenario<T> scenario = ActivityScenario.launch(getActivityClass())) {
            scenario.onActivity(activity -> {
                if (activity instanceof IdlingResourceProvider) {
                    idlingResource = ((IdlingResourceProvider) activity).getIdlingResource();
                    IdlingRegistry.getInstance().register(idlingResource);
                }
                this.activity = activity;
                onActivity(activity);
            });
        }
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

    protected abstract Class<T> getActivityClass();
}
