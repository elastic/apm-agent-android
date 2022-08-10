package co.elastic.apm.android.sdk.utility;

import android.content.Context;
import android.os.Build;

import java.lang.reflect.Field;

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

    private static String getServiceVersion(Context appContext) {
        try {
            Class<?> serviceBuildConfig = Class.forName(appContext.getPackageName() + ".BuildConfig");
            Field versionNameField = serviceBuildConfig.getDeclaredField("VERSION_NAME");
            return (String) versionNameField.get(serviceBuildConfig);
        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}