package co.elastic.apm.android.test.common.logs.verifiers.events;

import co.elastic.apm.android.test.common.logs.verifiers.LogVerifier;

public interface EventVerifier<T extends EventVerifier<?>> extends LogVerifier<T> {

    T isNamed(String eventName);
}
