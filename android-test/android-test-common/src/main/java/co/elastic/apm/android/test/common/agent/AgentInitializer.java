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
        ElasticApmAgent agent = ElasticApmAgent.initialize(context, configuration, connectivity);
        if (signalConfiguration != null) {
            injectSignalConfiguration(agent, signalConfiguration);
        }
    }

    private static void injectSignalConfiguration(ElasticApmAgent agent, SignalConfiguration
            signalConfiguration) {
        try {
            Field field = ElasticApmAgent.class.getDeclaredField("signalConfiguration");
            field.setAccessible(true);
            field.set(agent, signalConfiguration);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
