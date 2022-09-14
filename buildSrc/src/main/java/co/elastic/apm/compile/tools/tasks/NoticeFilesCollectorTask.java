package co.elastic.apm.compile.tools.tasks;

import org.apache.commons.io.FileUtils;
import org.gradle.api.Action;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.component.ComponentIdentifier;
import org.gradle.api.artifacts.result.ResolvedArtifactResult;
import org.gradle.api.attributes.Attribute;
import org.gradle.api.attributes.AttributeContainer;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.specs.Spec;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public abstract class NoticeFilesCollectorTask extends BaseTask {

    private static final Attribute<String> ARTIFACT_TYPE_ATTR = Attribute.of("artifactType", String.class);
    private static final Pattern NOTICE_FILE_NAME_PATTERN = Pattern.compile("^META-INF[/\\\\]([Nn][Oo][Tt][Ii][Cc][Ee])[^/\\\\]*");

    @InputFiles
    public abstract Property<Configuration> getRuntimeDependencies();

    @OutputDirectory
    public abstract DirectoryProperty getOutputDir();

    @TaskAction
    public void action() {
        try {
            FileUtils.cleanDirectory(getOutputDir().get().getAsFile());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Configuration dependencies = getRuntimeDependencies().get();
        List<ComponentIdentifier> allDependenciesIds = getComponentIdentifiers(dependencies);

        Set<ResolvedArtifactResult> jarArtifacts = dependencies.getIncoming().artifactView(configuration -> {
            configuration.setLenient(false);
            configuration.attributes(new JarAttributeAction());
            configuration.componentFilter(new ExternalComponentsSpec(allDependenciesIds));
        }).getArtifacts().getArtifacts();

        List<ComponentIdentifier> foundFor = extractNoticeFiles(jarArtifacts);
        allDependenciesIds.removeAll(foundFor);
        List<String> remainingDependenciesIdNames = new ArrayList<>();
        for (ComponentIdentifier id : allDependenciesIds) {
            remainingDependenciesIdNames.add(id.getDisplayName());
        }

        if (!remainingDependenciesIdNames.isEmpty()) {
            getProject().getLogger().warn("Not found NOTICE files for: " + remainingDependenciesIdNames);
        }
    }

    private List<ComponentIdentifier> extractNoticeFiles(Set<ResolvedArtifactResult> jarArtifacts) {
        List<ComponentIdentifier> foundNoticeFilesFor = new ArrayList<>();

        for (ResolvedArtifactResult artifact : jarArtifacts) {
            if (extractNoticeFromJar(artifact)) {
                foundNoticeFilesFor.add(artifact.getId().getComponentIdentifier());
            }
        }

        return foundNoticeFilesFor;
    }

    private boolean extractNoticeFromJar(ResolvedArtifactResult jarArtifact) {
        try {
            ZipFile zipFile = new ZipFile(jarArtifact.getFile());
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry zipEntry = entries.nextElement();
                if (NOTICE_FILE_NAME_PATTERN.matcher(zipEntry.getName()).matches()) {
                    String fileName = jarArtifact.getId().getComponentIdentifier().getDisplayName().replaceAll(":", "..");
                    File outputFile = getOutputDir().file(fileName).get().getAsFile();
                    InputStream inputStream = zipFile.getInputStream(zipEntry);
                    Files.copy(inputStream, outputFile.toPath());
                    inputStream.close();
                    zipFile.close();
                    return true;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return false;
    }

    private static class ExternalComponentsSpec implements Spec<ComponentIdentifier> {
        private final List<ComponentIdentifier> allowedComponents;

        private ExternalComponentsSpec(List<ComponentIdentifier> allowedComponents) {
            this.allowedComponents = allowedComponents;
        }

        @Override
        public boolean isSatisfiedBy(ComponentIdentifier componentIdentifier) {
            return allowedComponents.contains(componentIdentifier);
        }
    }

    private class JarAttributeAction implements Action<AttributeContainer> {

        @Override
        public void execute(AttributeContainer attributeContainer) {
            String type = isAndroidProject() ? "android-classes" : "jar";
            attributeContainer.attribute(ARTIFACT_TYPE_ATTR, type);
        }
    }
}
