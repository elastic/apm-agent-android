package co.elastic.apm.compile.tools.embedding.transforms;

import org.gradle.api.Action;
import org.gradle.api.artifacts.transform.InputArtifact;
import org.gradle.api.artifacts.transform.TransformAction;
import org.gradle.api.artifacts.transform.TransformOutputs;
import org.gradle.api.artifacts.transform.TransformParameters;
import org.gradle.api.artifacts.transform.TransformSpec;
import org.gradle.api.attributes.Attribute;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.provider.Provider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public abstract class AarGradleTransformAction implements TransformAction<TransformParameters.None> {

    private static final String AAR_FILE_EXTENSION = "aar";
    public static final Attribute<String> ARTIFACT_TYPE_ATTRIBUTE = Attribute.of("artifactType", String.class);
    public static final String ELASTIC_JAR = "elastic-jar";

    @InputArtifact
    public abstract Provider<FileSystemLocation> getInputArtifact();

    public void transform(TransformOutputs transformOutputs) {
        File input = getInputArtifact().get().getAsFile();
        String outputName = input.getName().replaceAll("\\." + AAR_FILE_EXTENSION + "$", ".jar");
        try {
            try (ZipFile zipFile = new ZipFile(input)) {
                ZipEntry entry = zipFile.getEntry("classes.jar");
                try (InputStream inputStream = zipFile.getInputStream(entry)) {
                    try (OutputStream outputStream = new FileOutputStream(transformOutputs.file(outputName))) {
                        byte[] buffer = new byte[1024 * 8];
                        int length;
                        while ((length = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, length);
                        }
                    }
                }
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to transform " + getInputArtifact(), exception);
        }
    }

    public static class ConfigurationAction implements Action<TransformSpec<TransformParameters.None>> {

        public void execute(TransformSpec<TransformParameters.None> spec) {
            spec.getFrom().attribute(ARTIFACT_TYPE_ATTRIBUTE, AAR_FILE_EXTENSION);
            spec.getTo().attribute(ARTIFACT_TYPE_ATTRIBUTE, ELASTIC_JAR);
        }
    }
}