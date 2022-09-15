package co.elastic.apm.compile.tools.tasks;

import org.gradle.api.artifacts.query.ArtifactResolutionQuery;
import org.gradle.api.artifacts.result.ResolvedArtifactResult;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.SkipWhenEmpty;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import co.elastic.apm.compile.tools.data.ArtifactIdentification;
import co.elastic.apm.compile.tools.utils.PomReader;

public abstract class NoticeMergerTask extends BasePomTask {

    @SkipWhenEmpty
    @InputDirectory
    public abstract DirectoryProperty getNoticeFilesDir();

    @OutputFile
    public abstract RegularFileProperty getOutputFile();

    @TaskAction
    public void action() {
        File[] noticeFiles = getNoticeFilesDir().get().getAsFile().listFiles();
        File mergedNotice = getOutputFile().get().getAsFile();
        try {
            mergeFiles(getNoticesInfo(noticeFiles), mergedNotice);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void mergeFiles(List<ArtifactNoticeInfo> artifacts, File into) throws IOException {
        OutputStream out = new FileOutputStream(into);
        byte[] buf = new byte[1024 * 8];
        boolean firstIteration = true;
        for (ArtifactNoticeInfo artifact : artifacts) {
            if (!firstIteration) {
                writeText(out, "\n--------------------------------------------------\n");
            } else {
                firstIteration = false;
            }
            writeText(out, artifact.id.getDisplayName() + " NOTICE\n\n");
            InputStream in = new FileInputStream(artifact.noticeFile);
            int b;
            while ((b = in.read(buf)) >= 0)
                out.write(buf, 0, b);
            in.close();
        }
        out.close();
    }

    private void writeText(OutputStream stream, String text) throws IOException {
        stream.write(text.getBytes(StandardCharsets.UTF_8));
    }

    private List<ArtifactNoticeInfo> getNoticesInfo(File[] files) {
        List<Gav> gavs = new ArrayList<>();
        for (File file : files) {
            String[] parts = file.getName().split("\\.\\.");
            gavs.add(new Gav(parts[0], parts[1], parts[2]));
        }

        ArtifactResolutionQuery pomQuery = getPomBaseQuery();

        for (Gav gav : gavs) {
            pomQuery.forModule(gav.group, gav.artifactName, gav.version);
        }

        List<ResolvedArtifactResult> pomArtifacts = getPomArtifacts(pomQuery);

        List<ArtifactNoticeInfo> noticeInfos = new ArrayList<>();
        for (ResolvedArtifactResult pomArtifact : pomArtifacts) {
            noticeInfos.add(createNoticeInfo(pomArtifact, files));
        }

        noticeInfos.sort(Comparator.comparing(artifactNoticeInfo -> artifactNoticeInfo.id.getDisplayName()));

        return noticeInfos;
    }

    private ArtifactNoticeInfo createNoticeInfo(ResolvedArtifactResult pomArtifact, File[] files) {
        String fileName = pomArtifact.getId().getComponentIdentifier().getDisplayName().replaceAll(":", "..");
        PomReader reader = new PomReader(pomArtifact.getFile());
        ArtifactIdentification identification = new ArtifactIdentification(reader.getName(), reader.getUrl());
        File noticeFile = findFileWithName(files, fileName);
        return new ArtifactNoticeInfo(identification, noticeFile);
    }

    private File findFileWithName(File[] files, String fileName) {
        for (File file : files) {
            if (file.getName().equals(fileName)) {
                return file;
            }
        }

        throw new RuntimeException("Could not find file named: " + fileName);
    }

    private static class Gav {
        private final String group;
        private final String artifactName;
        private final String version;

        private Gav(String group, String artifactName, String version) {
            this.group = group;
            this.artifactName = artifactName;
            this.version = version;
        }
    }

    private static class ArtifactNoticeInfo {
        public final ArtifactIdentification id;
        public final File noticeFile;

        private ArtifactNoticeInfo(ArtifactIdentification id, File noticeFile) {
            this.id = id;
            this.noticeFile = noticeFile;
        }
    }
}
