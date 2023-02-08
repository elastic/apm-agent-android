package co.elastic.apm.android.sdk.internal.instrumentation;

import co.elastic.apm.android.sdk.instrumentation.Instrumentation;

public abstract class SupportedInstrumentation extends Instrumentation {

    @Override
    protected Type getInstrumentationType() {
        return null;
    }
}
