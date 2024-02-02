package co.elastic.apm.android.plugin;

import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import java.io.File;

public class OkHttpConfigTest extends BaseAssetsVerificationTest {

    @Rule
    public TemporaryFolder projectTemporaryFolder = new TemporaryFolder();

    @Before
    public void setUp() {
        addPlugin("com.android.application");
        addPlugin("co.elastic.apm.android");
        getDefaultElasticBlockBuilder().setServerUrl("http://some.server");
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
