package co.elastic.apm.android.test.common.agent;

import android.content.Context;

import java.lang.reflect.Field;

import co.elastic.apm.android.sdk.ElasticApmAgent;
import co.elastic.apm.android.sdk.ElasticApmConfiguration;
import co.elastic.apm.android.sdk.connectivity.Connectivity;
import co.elastic.apm.android.sdk.connectivity.opentelemetry.SignalConfiguration;

public class AgentInitializer {

    public static void initialize(Context context, SignalConfiguration signalConfiguration) {
        initialize(context, null, signalConfiguration);
    }

    public static void initialize(Context context, ElasticApmConfiguration configuration, SignalConfiguration signalConfiguration) {
        initialize(context, configuration, null, signalConfiguration);
    }

    public static void initialize(Context context, ElasticApmConfiguration
            configuration, Connectivity connectivity, SignalConfiguration signalConfiguration) {
        if (configuration == null) {
            configuration = ElasticApmConfiguration.getDefault();
        }
        if (signalConfiguration != null) {
            injectSignalConfiguration(configuration, signalConfiguration);
        }
        ElasticApmAgent.initialize(context, configuration, connectivity);
    }

    private static void injectSignalConfiguration(ElasticApmConfiguration configuration,
                                                  SignalConfiguration signalConfiguration) {
        try {
            Field field = ElasticApmConfiguration.class.getDeclaredField("signalConfiguration");
            field.setAccessible(true);
            field.set(configuration, signalConfiguration);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
