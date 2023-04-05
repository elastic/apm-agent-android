package co.elastic.apm.android.test.common.agent;

import android.content.Context;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import co.elastic.apm.android.sdk.ElasticApmAgent;
import co.elastic.apm.android.sdk.ElasticApmConfiguration;
import co.elastic.apm.android.sdk.connectivity.Connectivity;
import co.elastic.apm.android.sdk.connectivity.opentelemetry.SignalConfiguration;
import co.elastic.apm.android.sdk.internal.injection.AgentDependenciesInjector;

public class AgentInitializer {

    public static void initialize(Context context, ElasticApmConfiguration configuration) {
        initialize(context, configuration, null, null);
    }

    public static void initialize(Context context, ElasticApmConfiguration
            configuration, Connectivity connectivity, AgentDependenciesInjector.Interceptor injectInterceptor) {
        if (configuration == null) {
            configuration = ElasticApmConfiguration.getDefault();
        }
        internalInitialize(context, configuration, connectivity, injectInterceptor);
    }

    public static void injectSignalConfiguration(ElasticApmConfiguration configuration,
                                                 SignalConfiguration signalConfiguration) {
        try {
            Field field = ElasticApmConfiguration.class.getDeclaredField("signalConfiguration");
            field.setAccessible(true);
            field.set(configuration, signalConfiguration);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static void internalInitialize(Context context, ElasticApmConfiguration configuration,
                                           Connectivity connectivity, AgentDependenciesInjector.Interceptor interceptor) {
        try {
            Method method = ElasticApmAgent.class.getDeclaredMethod("initialize", Context.class, ElasticApmConfiguration.class, Connectivity.class, AgentDependenciesInjector.Interceptor.class);
            method.setAccessible(true);
            method.invoke(null, context, configuration, connectivity, interceptor);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
