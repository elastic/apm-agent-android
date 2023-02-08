package co.elastic.apm.android.sdk.configuration.instrumentation;

import co.elastic.apm.android.sdk.internal.configuration.FeatureConfiguration;

public interface InstrumentationConfiguration {

    FeatureConfiguration getHttpRequestsConfiguration();

    FeatureConfiguration getScreenRenderingConfiguration();

    FeatureConfiguration getCrashReportingConfiguration();

    FeatureConfiguration getAppLaunchTimeConfiguration();
}
