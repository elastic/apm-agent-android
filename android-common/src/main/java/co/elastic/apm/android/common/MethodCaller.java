package co.elastic.apm.android.common;

import java.lang.reflect.Method;

public interface MethodCaller {

    void doCall(Method method, Object[] params);
}
