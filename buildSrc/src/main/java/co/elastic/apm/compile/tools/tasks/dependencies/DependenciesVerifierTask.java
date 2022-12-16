package co.elastic.apm.compile.tools.tasks.dependencies;

import org.apache.commons.io.FileUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import co.elastic.apm.compile.tools.metadata.NoticeMetadataHandler;

/**
 * Compares the last notice metadata's dependencies hash with the current hash and fails if they
 * are different.
 */
public abstract class DependenciesVerifierTask extends DefaultTask {

    @InputFile
    public abstract RegularFileProperty getDependenciesHashFile();

    @TaskAction
    public void execute() {
        File dependencyHashFile = getDependenciesHashFile().get().getAsFile();
        File noticePropertiesFile = NoticeMetadataHandler.getMetadataFile(getProject()).getAsFile();
        NoticeMetadataHandler noticeMetadataHandler = NoticeMetadataHandler.read(noticePropertiesFile);
        String currentDependenciesHash = readDependenciesHash(dependencyHashFile);
        String lastStoredDependenciesHash = noticeMetadataHandler.getDependenciesHash();

        if (!Objects.equals(currentDependenciesHash, lastStoredDependenciesHash)) {
            throw new GradleException("The NOTICE file is outdated, run the `createNoticeFile` task to re-generate it.");
        }
    }

    private String readDependenciesHash(File dependencyHashFile) {
        try {
            return FileUtils.readFileToString(dependencyHashFile, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
