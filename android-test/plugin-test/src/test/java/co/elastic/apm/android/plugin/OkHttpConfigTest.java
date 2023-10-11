package co.elastic.apm.android.plugin;

import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.util.Properties;

import co.elastic.apm.android.common.ApmInfo;

public class OkHttpConfigTest extends BaseAssetsVerificationTest {

    @Rule
    public TemporaryFolder projectTemporaryFolder = new TemporaryFolder();

    @Before
    public void setUp() {
        addPlugin("com.android.application");
        addPlugin("co.elastic.apm.android");
        getDefaultElasticBlockBuilder().setServerUrl("http://some.server");
    }

    @Test
    public void whenGeneratingAssetsFile_getProjectsOkHttpVersion() {
        setUpProject();

        runGradle("assembleDebug");

        verifyTaskIsSuccessful(":debugGenerateApmInfo");
        Properties properties = getGeneratedProperties("debug");
        String okhttpVersion = properties.getProperty(ApmInfo.KEY_SCOPE_OKHTTP_VERSION);
        assertNotNull(okhttpVersion);
    }

    @Override
    protected File getProjectDir() {
        return projectTemporaryFolder.getRoot();
    }

    @Override
    protected String getAndroidGradlePluginVersion() {
        return "7.4.0";
    }
}
