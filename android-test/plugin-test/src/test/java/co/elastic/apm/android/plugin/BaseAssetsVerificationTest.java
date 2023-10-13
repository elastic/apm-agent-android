package co.elastic.apm.android.plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import co.elastic.apm.android.common.ApmInfo;
import co.elastic.apm.android.plugin.testutils.BaseFunctionalTest;

public abstract class BaseAssetsVerificationTest extends BaseFunctionalTest {

    protected Properties getGeneratedProperties(String variantName) {
        File output = getGeneratedPropertiesFile(variantName + "GenerateApmInfo");
        return loadProperties(output);
    }

    protected File getGeneratedPropertiesFile(String taskName) {
        return getBuildDirFile(getRelativePathToGeneratedAssetFile(taskName));
    }

    protected String getRelativePathToGeneratedAssetFile(String taskName) {
        return "generated/assets/" + taskName + "/" + ApmInfo.ASSET_FILE_NAME;
    }

    protected Properties loadProperties(File propertiesFile) {
        Properties properties = new Properties();
        try (InputStream is = new FileInputStream(propertiesFile)) {
            properties.load(is);
            return properties;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
