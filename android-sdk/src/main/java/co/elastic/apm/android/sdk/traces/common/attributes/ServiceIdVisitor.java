package co.elastic.apm.android.sdk.traces.common.attributes;

import android.content.Context;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import co.elastic.apm.android.common.ApmInfo;
import co.elastic.apm.android.sdk.attributes.AttributesBuilderVisitor;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;

public class ServiceIdVisitor implements AttributesBuilderVisitor {
    private final Context appContext;
    private final String serviceName;
    private final String serviceVersion;

    public ServiceIdVisitor(Context appContext, String serviceName, String serviceVersion) {
        this.appContext = appContext;
        this.serviceName = serviceName;
        this.serviceVersion = serviceVersion;
    }

    @Override
    public void visit(AttributesBuilder builder) {
        Properties apmInfoProperties = getApmInfoProperties(appContext);
        String serviceName = (this.serviceName != null) ? this.serviceName : appContext.getPackageName();
        String serviceVersion = (this.serviceVersion != null) ? this.serviceVersion : apmInfoProperties.getProperty(ApmInfo.KEY_VERSION);
        builder.put(ResourceAttributes.SERVICE_NAME, serviceName)
                .put(ResourceAttributes.SERVICE_VERSION, serviceVersion)
                .put(ResourceAttributes.DEPLOYMENT_ENVIRONMENT, apmInfoProperties.getProperty(ApmInfo.KEY_VARIANT_NAME));
    }

    private Properties getApmInfoProperties(Context appContext) {
        try (InputStream propertiesFileInputStream = appContext.getAssets().open(ApmInfo.ASSET_FILE_NAME)) {
            Properties properties = new Properties();
            properties.load(propertiesFileInputStream);
            return properties;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
