package co.elastic.apm.compile.tools.tasks.rootproject;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import co.elastic.apm.compile.tools.utils.TextUtils;

public abstract class SubprojectPomLicensesMergerTask extends DefaultTask {

    @InputFiles
    public abstract ConfigurableFileCollection getSubprojectLicensedDependenciesFiles();

    @OutputFile
    public abstract RegularFileProperty getMergedSubprojectLicensedDependencies();

    @TaskAction
    public void action() {
        Set<File> licensedDependenciesFiles = getSubprojectLicensedDependenciesFiles().getFiles();
        try {
            mergeUniqueLines(getMergedSubprojectLicensedDependencies().get().getAsFile(), licensedDependenciesFiles);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void mergeUniqueLines(File intoFile, Set<File> licensedDependenciesFiles) throws IOException {
        OutputStream out = new FileOutputStream(intoFile);
        List<String> addedLines = new ArrayList<>();
        for (File file : licensedDependenciesFiles) {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                if (!addedLines.contains(line)) {
                    if (!addedLines.isEmpty()) {
                        TextUtils.writeText(out, "\n");
                    }
                    TextUtils.writeText(out, line);
                    addedLines.add(line);
                }
            }
            reader.close();
        }
        out.close();
    }
}
