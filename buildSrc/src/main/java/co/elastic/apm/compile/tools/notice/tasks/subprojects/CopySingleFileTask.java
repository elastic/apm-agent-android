package co.elastic.apm.compile.tools.notice.tasks.subprojects;

import org.apache.commons.io.FileUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;

public abstract class CopySingleFileTask extends DefaultTask {

    @InputFile
    public abstract RegularFileProperty getInputFile();

    @OutputFile
    public abstract RegularFileProperty getOutputFile();

    @TaskAction
    public void action() throws IOException {
        FileUtils.copyFile(getInputFile().get().getAsFile(), getOutputFile().get().getAsFile());
    }
}
