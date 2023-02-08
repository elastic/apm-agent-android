package co.elastic.apm.android.sdk.instrumentation;

import co.elastic.apm.android.sdk.instrumentation.supported.AppLaunchTimeInstrumentation;
import co.elastic.apm.android.sdk.instrumentation.supported.CrashReportingInstrumentation;
import co.elastic.apm.android.sdk.instrumentation.supported.HttpRequestsInstrumentation;
import co.elastic.apm.android.sdk.instrumentation.supported.ScreenRenderingInstrumentation;
import co.elastic.apm.android.sdk.internal.configuration.FeatureConfiguration;

public abstract class Instrumentation extends FeatureConfiguration {

    protected abstract Type getInstrumentationType();

    @Override
    protected Class<? extends FeatureConfiguration> getParentConfigurationType() {
        switch (getInstrumentationType()) {
            case HTTP_REQUESTS:
                return HttpRequestsInstrumentation.class;
            case SCREEN_RENDERING:
                return ScreenRenderingInstrumentation.class;
            case CRASH_REPORTING:
                return CrashReportingInstrumentation.class;
            case APP_LAUNCH_TIME:
                return AppLaunchTimeInstrumentation.class;
        }

        return InstrumentationConfiguration.class;
    }

    public enum Type {
        HTTP_REQUESTS,
        SCREEN_RENDERING,
        CRASH_REPORTING,
        APP_LAUNCH_TIME
    }
}
