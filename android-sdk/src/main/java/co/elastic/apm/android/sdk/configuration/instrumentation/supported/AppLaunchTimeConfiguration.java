package co.elastic.apm.android.sdk.configuration.instrumentation.supported;

import co.elastic.apm.android.sdk.configuration.FeatureConfiguration;

public class AppLaunchTimeConfiguration extends FeatureConfiguration {

    @Override
    protected boolean enabled() {
        return false;
    }
}
