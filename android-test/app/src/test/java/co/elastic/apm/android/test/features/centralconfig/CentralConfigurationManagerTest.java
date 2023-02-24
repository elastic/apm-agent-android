package co.elastic.apm.android.test.features.centralconfig;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import android.content.Context;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;

import co.elastic.apm.android.sdk.internal.configuration.Configuration;
import co.elastic.apm.android.sdk.internal.configuration.Configurations;
import co.elastic.apm.android.sdk.internal.features.centralconfig.CentralConfigurationListener;
import co.elastic.apm.android.sdk.internal.features.centralconfig.CentralConfigurationManager;
import co.elastic.apm.android.test.testutils.base.BaseRobolectricTest;

public class CentralConfigurationManagerTest extends BaseRobolectricTest {
    private Context context;
    private File configFile;
    private CentralConfigurationManager manager;

    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Before
    public void setUp() throws IOException {
        context = mock(Context.class);
        manager = new CentralConfigurationManager(context);
        File filesDir = temporaryFolder.newFolder("filesDir");
        configFile = new File(filesDir, "elastic_agent_configuration.json");
        doReturn(filesDir).when(context).getFilesDir();
    }

    @Test
    public void whenNotifyingConfigChanges_targetCentralConfigListeners() {
        NormalConfiguration normalConfiguration = mock(NormalConfiguration.class);
        CentralAwareConfiguration centralAwareConfiguration = mock(CentralAwareConfiguration.class);
        injectConfigurations(normalConfiguration, centralAwareConfiguration);
        setConfigFileContents("{\"someKey\":\"someValue\"}");
        Map<String, String> map = new HashMap<>();
        map.put("someKey", "someValue");

        manager.publishCachedConfig();

        verify(centralAwareConfiguration).onUpdate(map);
        verifyNoInteractions(normalConfiguration);
    }

    private void setConfigFileContents(String contents) {
        try {
            if (!configFile.exists()) {
                configFile.createNewFile();
            }
            Files.write(configFile.toPath(), contents.getBytes(StandardCharsets.UTF_8), StandardOpenOption.WRITE);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings({"unchecked", "ConstantConditions"})
    private void injectConfigurations(Configuration... configurations) {
        try {
            Field mapField = Configurations.class.getDeclaredField("configurations");
            mapField.setAccessible(true);
            Field instanceField = Configurations.class.getDeclaredField("INSTANCE");
            instanceField.setAccessible(true);
            Configurations instance = (Configurations) instanceField.get(null);
            Map<Class<? extends Configuration>, Configuration> map = (Map<Class<? extends Configuration>, Configuration>) mapField.get(instance);
            for (Configuration configuration : configurations) {
                map.put(configuration.getClass(), configuration);
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static class NormalConfiguration implements Configuration {

    }

    private static class CentralAwareConfiguration implements Configuration, CentralConfigurationListener {

        @Override
        public void onUpdate(Map<String, String> map) {

        }
    }
}
