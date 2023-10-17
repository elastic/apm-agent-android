package co.elastic.apm.android.test.lifecycle;

import co.elastic.apm.android.test.base.BaseEspressoTest;

public abstract class BaseLifecycleInstrumentationTest extends BaseEspressoTest {

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

    protected String getSpanMethodName(ActivityMethod method) {
        return method.name;
    }

    protected String getSpanMethodName(FragmentMethod method) {
        return method.name;
    }

    protected String getRootLifecycleSpanName(Class<?> theClass) {
        return theClass.getSimpleName() + " - View appearing";
    }
}
