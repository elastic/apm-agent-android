package co.elastic.apm.compile.tools.embedding.tasks;

import org.apache.commons.io.FileUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public abstract class EmbeddedClassesGathererTask extends DefaultTask {

    @InputDirectory
    public abstract DirectoryProperty getClassesDir();

    @OutputDirectory
    public abstract DirectoryProperty getOutputDir();

    @TaskAction
    public void execute() throws IOException {
        File outputFile = getOutputDir().get().getAsFile();
        FileUtils.cleanDirectory(outputFile);
        File origin = getClassesDir().getAsFile().get();
        for (File file : Objects.requireNonNull(origin.listFiles())) {
            FileUtils.copyDirectoryToDirectory(file, outputFile);
        }
    }
}
