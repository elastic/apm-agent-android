package co.elastic.apm.android.test.lifecycle;

import co.elastic.apm.android.test.testutils.base.BaseTest;

public class BaseLifecycleInstrumentationTest extends BaseTest {

    protected String getRootLifecycleSpanName(Class<?> theClass) {
        return getClassSpanName(theClass, " - Creating");
    }
}
