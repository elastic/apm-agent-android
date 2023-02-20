package co.elastic.apm.android.test.lifecycle;

import co.elastic.apm.android.sdk.ElasticApmConfiguration;
import co.elastic.apm.android.sdk.instrumentation.InstrumentationConfiguration;
import co.elastic.apm.android.test.testutils.base.BaseRobolectricTestApplication;

class AppWithScreenRenderingInstrumentationDisabled extends BaseRobolectricTestApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        ElasticApmConfiguration configuration = ElasticApmConfiguration.builder()
                .setInstrumentationConfiguration(InstrumentationConfiguration.builder().enableScreenRendering(false).build())
                .build();

        initializeAgentWithCustomConfig(configuration);
    }
}
