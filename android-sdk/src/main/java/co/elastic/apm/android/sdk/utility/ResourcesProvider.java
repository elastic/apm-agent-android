package co.elastic.apm.android.sdk.utility;

import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.reflect.Field;
import java.util.Locale;

import co.elastic.apm.android.sdk.BuildConfig;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;

public class ResourcesProvider {

    public static Attributes getCommonResourceAttributes(Context appContext) {
        return Attributes.builder()
                .put(ResourceAttributes.SERVICE_NAME, appContext.getPackageName())
                .put(ResourceAttributes.SERVICE_VERSION, getServiceVersion(appContext))
                .put("telemetry.sdk.name", "android")
                .put("telemetry.sdk.version", BuildConfig.APM_AGENT_VERSION)
                .put("telemetry.sdk.language", "java")
                .put(ResourceAttributes.OS_DESCRIPTION, getOsDescription())
                .put(ResourceAttributes.OS_TYPE, "linux")
                .put(ResourceAttributes.OS_VERSION, Build.VERSION.RELEASE)
                .put(ResourceAttributes.OS_NAME, Build.VERSION.CODENAME)
                .put(ResourceAttributes.DEPLOYMENT_ENVIRONMENT, getDeploymentEnvironment(appContext))
                .put(ResourceAttributes.DEVICE_ID, DeviceIdProvider.getDeviceId(appContext))
                .put(ResourceAttributes.DEVICE_MODEL_IDENTIFIER, Build.MODEL)
                .put(ResourceAttributes.DEVICE_MANUFACTURER, Build.MANUFACTURER)
                .build();
    }

    private static String getOsDescription() {
        StringBuilder descriptionBuilder = new StringBuilder();
        descriptionBuilder.append("Android ");
        descriptionBuilder.append(Build.VERSION.RELEASE);
        descriptionBuilder.append(", API level ");
        descriptionBuilder.append(Build.VERSION.SDK_INT);
        descriptionBuilder.append(", NAME ");
        descriptionBuilder.append(Build.VERSION.CODENAME);
        descriptionBuilder.append(", BUILD ");
        descriptionBuilder.append(Build.VERSION.INCREMENTAL);
        return descriptionBuilder.toString();
    }

    private static String getDeploymentEnvironment(Context appContext) {
        String flavor = getBuildFlavor(appContext);
        String buildType = getBuildType(appContext);

        if (flavor == null) {
            return buildType;
        }

        return flavor + capitalize(buildType);
    }

    private static String capitalize(String value) {
        return value.substring(0, 1).toUpperCase(Locale.US) + value.substring(1);
    }

    @NonNull
    private static String getBuildType(Context appContext) {
        Class<?> serviceBuildConfig = getBuildConfigClass(appContext);
        return getField(serviceBuildConfig, "BUILD_TYPE");
    }

    @Nullable
    private static String getBuildFlavor(Context appContext) {
        Class<?> serviceBuildConfig = getBuildConfigClass(appContext);
        try {
            return getField(serviceBuildConfig, "FLAVOR");
        } catch (RuntimeException e) {
            return null;
        }
    }

    @NonNull
    private static String getServiceVersion(Context appContext) {
        Class<?> serviceBuildConfig = getBuildConfigClass(appContext);
        return getField(serviceBuildConfig, "VERSION_NAME");
    }

    @SuppressWarnings("unchecked")
    private static <T> T getField(Class<?> fromClass, String name) {
        try {
            Field field = fromClass.getDeclaredField(name);
            return (T) field.get(fromClass);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static Class<?> getBuildConfigClass(Context appContext) {
        try {
            return Class.forName(appContext.getPackageName() + ".BuildConfig");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}