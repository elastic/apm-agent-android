package co.elastic.apm.android.test.opentelemetry.common;

import co.elastic.apm.android.sdk.ElasticApmConfiguration;
import co.elastic.apm.android.test.testutils.base.BaseRobolectricTestApplication;

public final class AppUseCases {

    public static class AppWithSampleRateZero extends BaseRobolectricTestApplication {
        @Override
        public void onCreate() {
            super.onCreate();
            ElasticApmConfiguration configuration = ElasticApmConfiguration.builder()
                    .setSampleRate(0)
                    .build();
            initializeAgentWithCustomConfig(configuration);
        }
    }
}
