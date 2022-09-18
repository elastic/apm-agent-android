package co.elastic.apm.compile.tools.tasks;

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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import co.elastic.apm.compile.tools.data.ArtifactIdentification;
import co.elastic.apm.compile.tools.data.Gav;
import co.elastic.apm.compile.tools.tasks.subprojects.BasePomTask;
import co.elastic.apm.compile.tools.utils.PomReader;
import co.elastic.apm.compile.tools.utils.TextUtils;

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
                addSeparator(out);
            } else {
                firstIteration = false;
            }
            TextUtils.writeText(out, artifact.id.getDisplayName() + " NOTICE\n\n");
            InputStream in = new FileInputStream(artifact.noticeFile);
            int b;
            while ((b = in.read(buf)) >= 0)
                out.write(buf, 0, b);
            in.close();
        }
        out.close();
    }

    private void addSeparator(OutputStream out) {
        TextUtils.addSeparator(out, '-');
    }

    private List<ArtifactNoticeInfo> getNoticesInfo(File[] files) {
        List<Gav> gavs = new ArrayList<>();
        for (File file : files) {
            String[] parts = file.getName().split("\\.\\.");
            gavs.add(new Gav(parts[0], parts[1], parts[2]));
        }

        List<ResolvedArtifactResult> pomArtifacts = getPomArtifactsForGavs(gavs);

        List<ArtifactNoticeInfo> noticeInfos = new ArrayList<>();
        for (ResolvedArtifactResult pomArtifact : pomArtifacts) {
            noticeInfos.add(createNoticeInfo(pomArtifact, files));
        }

        noticeInfos.sort(Comparator.comparing(artifactNoticeInfo -> artifactNoticeInfo.id.getDisplayName().toLowerCase(Locale.US)));

        return noticeInfos;
    }

    private ArtifactNoticeInfo createNoticeInfo(ResolvedArtifactResult pomArtifact, File[] files) {
        String gradleUri = pomArtifact.getId().getComponentIdentifier().getDisplayName();
        String fileName = gradleUri.replaceAll(":", "..");
        PomReader reader = new PomReader(pomArtifact.getFile());
        ArtifactIdentification identification = new ArtifactIdentification(reader.getName(), reader.getUrl(), gradleUri);
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

    private static class ArtifactNoticeInfo {
        public final ArtifactIdentification id;
        public final File noticeFile;

        private ArtifactNoticeInfo(ArtifactIdentification id, File noticeFile) {
            this.id = id;
            this.noticeFile = noticeFile;
        }
    }
}
