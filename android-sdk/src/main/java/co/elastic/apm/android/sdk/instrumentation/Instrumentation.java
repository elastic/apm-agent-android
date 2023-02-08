package co.elastic.apm.android.sdk.instrumentation;

import co.elastic.apm.android.sdk.instrumentation.supported.AppLaunchTimeConfiguration;
import co.elastic.apm.android.sdk.instrumentation.supported.CrashReportingConfiguration;
import co.elastic.apm.android.sdk.instrumentation.supported.HttpRequestsConfiguration;
import co.elastic.apm.android.sdk.instrumentation.supported.ScreenRenderingConfiguration;
import co.elastic.apm.android.sdk.internal.configuration.FeatureConfiguration;

public abstract class Instrumentation extends FeatureConfiguration {

    protected abstract Type getInstrumentationType();

    @Override
    protected Class<? extends FeatureConfiguration> getParentConfiguration() {
        switch (getInstrumentationType()) {
            case HTTP_REQUESTS:
                return HttpRequestsConfiguration.class;
            case SCREEN_RENDERING:
                return ScreenRenderingConfiguration.class;
            case CRASH_REPORTING:
                return CrashReportingConfiguration.class;
            case APP_LAUNCH_TIME:
                return AppLaunchTimeConfiguration.class;
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
