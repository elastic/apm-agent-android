package co.elastic.apm.android.test.lifecycle;

import co.elastic.apm.android.test.base.BaseEspressoTest;

public abstract class BaseLifecycleInstrumentationTest extends BaseEspressoTest {

    protected enum ActivityMethod {
        ON_CREATE("Created"),
        ON_RESUME("Restarted"),
        ON_PAUSE("Paused"),
        ON_STOP("Stopped");

        private final String name;

        ActivityMethod(String name) {
            this.name = name;
        }
    }

    protected enum FragmentMethod {
        ON_CREATE("Created"),
        ON_VIEW_DESTROYED("ViewDestroyed"),
        ON_PAUSE("Paused"),
        ON_DESTROY("Destroyed");

        private final String name;

        FragmentMethod(String name) {
            this.name = name;
        }
    }

    protected String getSpanMethodName(ActivityMethod method) {
        return method.name;
    }

    protected String getSpanMethodName(FragmentMethod method) {
        return method.name;
    }
}
