package co.elastic.apm.android.test.common.logs.verifiers;

public interface LogVerifier<T extends LogVerifier<?>> {
    T hasResource(String resourceName);

    T hasResource(String resourceName, String resourceValue);

    T hasResource(String resourceName, Integer resourceValue);

    T startedAt(long timeInNanoseconds);

    T hasAttribute(String attrName);

    T hasAttribute(String attrName, String attrValue);
}
