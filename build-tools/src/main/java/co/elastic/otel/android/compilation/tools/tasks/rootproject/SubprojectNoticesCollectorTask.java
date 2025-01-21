package co.elastic.otel.android.compilation.tools.tasks.rootproject;

import org.apache.commons.io.FileUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class SubprojectNoticesCollectorTask extends DefaultTask {

    @InputFiles
    public abstract ConfigurableFileCollection getSubprojectNoticeFiles();

    @OutputDirectory
    public abstract DirectoryProperty getMergedSubprojectNoticeFiles();

    @TaskAction
    public void action() {
        File outputDir = getMergedSubprojectNoticeFiles().get().getAsFile();
        try {
            FileUtils.cleanDirectory(outputDir);
            copyFiles(getNoticeFiles(), outputDir);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Set<File> getNoticeFiles() {
        Set<File> noticeFiles = new HashSet<>();
        Set<File> noticeFilesDirs = getSubprojectNoticeFiles().getFiles();
        for (File dir : noticeFilesDirs) {
            File[] files = dir.listFiles();
            if (files == null) {
                continue;
            }
            noticeFiles.addAll(Arrays.asList(files));
        }
        return noticeFiles;
    }

    private void copyFiles(Set<File> noticeFiles, File intoDir) throws IOException {
        List<String> copiedFileNames = new ArrayList<>();
        for (File noticeFile : noticeFiles) {
            String name = noticeFile.getName();
            if (!copiedFileNames.contains(name)) {
                File destinationFile = new File(intoDir, name);
                FileUtils.copyFile(noticeFile, destinationFile);
                copiedFileNames.add(name);
            }
        }
    }
}
