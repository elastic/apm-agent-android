package co.elastic.apm.compile.tools.tasks;

import org.apache.commons.io.FileUtils;
import org.gradle.api.Project;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import co.elastic.apm.compile.tools.data.ArtifactLicense;
import co.elastic.apm.compile.tools.metadata.NoticeMetadataHandler;
import co.elastic.apm.compile.tools.utils.TextUtils;

public abstract class CreateNoticeTask extends BaseTask {

    @InputFile
    public abstract RegularFileProperty getLicensedDependencies();

    @InputFiles
    public abstract ConfigurableFileCollection getMergedNoticeFiles();

    @InputFile
    public abstract RegularFileProperty getFoundLicensesIds();

    @InputFile
    @Optional
    public abstract RegularFileProperty getDependenciesHashFile();

    @OutputDirectory
    public abstract DirectoryProperty getOutputDir();

    @OutputFile
    public abstract RegularFileProperty getMetadataOutputFile();

    public CreateNoticeTask() {
        getMetadataOutputFile().set(NoticeMetadataHandler.getMetadataFile(getProject()));
    }

    @TaskAction
    public void action() {
        File licensesFile = getLicensedDependencies().get().getAsFile();
        Set<File> mergedNoticesFile = getMergedNoticeFiles().getFiles();
        List<String> licenseIds = getLicenseIds();
        List<File> filesToMerge = new ArrayList<>();
        if (licensesFile.length() > 0) {
            filesToMerge.add(licensesFile);
        }
        if (!mergedNoticesFile.isEmpty()) {
            File first = mergedNoticesFile.iterator().next();
            if (first != null && first.exists()) {
                filesToMerge.add(first);
            }
        }

        try {
            File metaInfDir = new File(getOutputDir().getAsFile().get(), "META-INF");
            metaInfDir.mkdirs();
            OutputStream outputStream = new FileOutputStream(new File(metaInfDir, "NOTICE"));
            addToOutputStreamAndCloseInput(outputStream, getNoticeHeaderInputStream());
            if (!filesToMerge.isEmpty()) {
                addPanelSeparator(outputStream);
                addFilesContent(outputStream, filesToMerge);
            }
            if (!licenseIds.isEmpty()) {
                addPanelSeparator(outputStream);
                addLicenses(outputStream, licenseIds);
            }
            outputStream.close();
            saveMetadata();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void saveMetadata() {
        if (!getDependenciesHashFile().isPresent()) {
            Project project = getProject();
            project.getLogger().lifecycle("Skipping metadata saving for project: " + project.getName());
            return;
        }
        File dependenciesHashFile = getDependenciesHashFile().get().getAsFile();
        File metadataFile = getMetadataOutputFile().get().getAsFile();
        try {
            String dependenciesHash = FileUtils.readFileToString(dependenciesHashFile, StandardCharsets.UTF_8);
            NoticeMetadataHandler noticeMetadataHandler = NoticeMetadataHandler.create();
            noticeMetadataHandler.setDependenciesHash(dependenciesHash);
            noticeMetadataHandler.save(metadataFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<String> getLicenseIds() {
        try {
            List<String> licenseIds = new ArrayList<>();
            File licensesFound = getFoundLicensesIds().get().getAsFile();
            BufferedReader reader = new BufferedReader(new FileReader(licensesFound));
            String line;
            while ((line = reader.readLine()) != null) {
                ArtifactLicense artifactLicense = ArtifactLicense.parse(line);
                String licenseId = artifactLicense.licenseId;
                if (!licenseIds.contains(licenseId)) {
                    licenseIds.add(licenseId);
                }
            }
            reader.close();
            licenseIds.sort(Comparator.comparing(it -> it));
            return licenseIds;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void addFilesContent(OutputStream out, List<File> files) throws IOException {
        boolean firstIteration = true;
        for (File file : files) {
            if (!firstIteration) {
                addPanelSeparator(out);
            } else {
                firstIteration = false;
            }
            InputStream in = new FileInputStream(file);
            addToOutputStreamAndCloseInput(out, in);
        }
    }

    private void addLicenses(OutputStream outputStream, List<String> licenseIds) throws IOException {
        boolean firstIteration = true;
        for (String licenseId : licenseIds) {
            if (!firstIteration) {
                addLicenseSeparator(outputStream);
            } else {
                firstIteration = false;
            }
            addToOutputStreamAndCloseInput(outputStream, getResourceInputStream("/licenses/" + licenseId));
        }
    }

    private void addToOutputStreamAndCloseInput(OutputStream out, InputStream in) throws IOException {
        byte[] buf = new byte[1024 * 8];
        int b;
        while ((b = in.read(buf)) >= 0) {
            out.write(buf, 0, b);
        }
        in.close();
    }

    private void addPanelSeparator(OutputStream out) {
        TextUtils.addSeparator(out, '#');
    }

    private void addLicenseSeparator(OutputStream out) {
        TextUtils.addSeparator(out, '-');
    }

    private InputStream getNoticeHeaderInputStream() {
        return getResourceInputStream("/notice_header.txt");
    }

    private InputStream getResourceInputStream(String path) {
        return CreateNoticeTask.class.getResourceAsStream(path);
    }
}
