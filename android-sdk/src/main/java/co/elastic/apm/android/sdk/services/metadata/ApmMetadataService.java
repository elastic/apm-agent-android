package co.elastic.apm.android.sdk.services.metadata;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import co.elastic.apm.android.common.ApmInfo;
import co.elastic.apm.android.sdk.services.Service;

public class ApmMetadataService implements Service {
    private final Properties apmInfoProperties;

    public ApmMetadataService(Context appContext) {
        apmInfoProperties = getApmInfoProperties(appContext);
    }

    @NonNull
    public String getServiceVersion() {
        return apmInfoProperties.getProperty(ApmInfo.KEY_SERVICE_VERSION);
    }

    @NonNull
    public String getDeploymentEnvironment() {
        return apmInfoProperties.getProperty(ApmInfo.KEY_SERVICE_VARIANT_NAME);
    }

    @Nullable
    public String getOkHttpVersion() {
        return apmInfoProperties.getProperty(ApmInfo.KEY_SCOPE_OKHTTP_VERSION);
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public String name() {
        return Service.Names.METADATA;
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
