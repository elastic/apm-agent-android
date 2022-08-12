package co.elastic.apm.android.plugin.tasks;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

import co.elastic.apm.android.common.ApmInfo;

public abstract class ApmInfoGenerator extends DefaultTask {

    @Input
    public abstract Property<String> getVariantName();

    @Input
    public abstract Property<String> getVersion();

    @OutputDirectory
    public abstract DirectoryProperty getOutputDir();

    @TaskAction
    public void execute() {
        File propertiesFile = new File(getOutputDir().get().getAsFile(), ApmInfo.ASSET_FILE_NAME);
        Properties properties = new Properties();
        properties.put(ApmInfo.KEY_VARIANT_NAME, getVariantName().get());
        properties.put(ApmInfo.KEY_VERSION, getVersion().get());

        try (OutputStream outputStream = new FileOutputStream(propertiesFile)) {
            properties.store(outputStream, null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
