package co.elastic.apm.android.plugin;

import co.elastic.apm.android.common.ApmInfo;

public class OkHttpConfig73Test extends OkHttpConfigTest {

    @Override
    protected String getAndroidGradlePluginVersion() {
        return "7.3.0";
    }

    @Override
    protected String getRelativePathToGeneratedAssetFile(String taskName) {
        return "ASSETS/" + taskName + "/" + ApmInfo.ASSET_FILE_NAME;
    }
}
