package co.elastic.apm.compile.tools.tasks;

import org.gradle.api.DefaultTask;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ExternalModuleDependency;
import org.gradle.api.artifacts.ModuleVersionIdentifier;
import org.gradle.api.artifacts.ResolvedArtifact;
import org.gradle.api.artifacts.component.ComponentIdentifier;
import org.gradle.api.artifacts.result.ArtifactResolutionResult;
import org.gradle.api.artifacts.result.ArtifactResult;
import org.gradle.api.artifacts.result.ComponentArtifactsResult;
import org.gradle.api.artifacts.result.ResolvedArtifactResult;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.gradle.maven.MavenModule;
import org.gradle.maven.MavenPomArtifact;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import co.elastic.apm.compile.tools.data.ArtifactLicense;
import co.elastic.apm.compile.tools.utils.PomReader;

public abstract class PomLicensesCollector extends DefaultTask {

    @InputFiles
    public abstract Property<Configuration> getRuntimeDependencies();

    @OutputFile
    public abstract RegularFileProperty getLicensesFound();

    @SuppressWarnings("unchecked")
    @TaskAction
    public void action() {
        ArtifactResolutionResult result = getProject().getDependencies().createArtifactResolutionQuery()
                .forComponents(getComponentIdentifiers())
                .withArtifacts(MavenModule.class, MavenPomArtifact.class)
                .execute();

        try {
            List<ArtifactLicense> artifactLicenses = extractLicenses(getPomArtifacts(result));
            File licensesFoundFile = getLicensesFound().get().getAsFile();
            writeToFile(licensesFoundFile, artifactLicenses);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void writeToFile(File licensesFoundFile, List<ArtifactLicense> artifactLicenses) throws IOException {
        FileWriter fileWriter = new FileWriter(licensesFoundFile);
        PrintWriter printWriter = new PrintWriter(fileWriter);

        for (ArtifactLicense artifactLicense : artifactLicenses) {
            printWriter.println(artifactLicense.serialize());
        }

        printWriter.close();
    }

    private List<ArtifactLicense> extractLicenses(Map<ComponentIdentifier, ResolvedArtifactResult> pomArtifacts) throws ParserConfigurationException, SAXException, IOException {
        List<ArtifactLicense> artifactLicenses = new ArrayList<>();

        for (ComponentIdentifier pomArtifactKey : pomArtifacts.keySet()) {
            ResolvedArtifactResult pomArtifact = pomArtifacts.get(pomArtifactKey);
            File pomFile = pomArtifact.getFile();
            PomReader reader = new PomReader(pomFile);
            String licenseName = reader.getLicenseName();
            String displayName = pomArtifactKey.getDisplayName();
            if (licenseName != null) {
                artifactLicenses.add(new ArtifactLicense(displayName, licenseName));
            } else {
                getProject().getLogger().warn("Could not find a license in the POM file for: " + displayName);
            }
        }

        return artifactLicenses;
    }

    private Map<ComponentIdentifier, ResolvedArtifactResult> getPomArtifacts(ArtifactResolutionResult result) {
        Map<ComponentIdentifier, ResolvedArtifactResult> results = new HashMap<>();

        for (ComponentArtifactsResult component : result.getResolvedComponents()) {
            Set<ArtifactResult> artifacts = component.getArtifacts(MavenPomArtifact.class);
            ComponentIdentifier id = component.getId();
            String displayName = id.getDisplayName();
            if (!artifacts.iterator().hasNext()) {
                throw new RuntimeException("No POM file found for: " + displayName);
            }
            ArtifactResult artifact = artifacts.iterator().next();
            results.put(id, (ResolvedArtifactResult) artifact);
        }

        return results;
    }

    private List<ComponentIdentifier> getComponentIdentifiers() {
        List<String> externalDependenciesIds = new ArrayList<>();

        for (Dependency dependency : getRuntimeDependencies().get().getAllDependencies()) {
            if (dependency instanceof ExternalModuleDependency) {
                ExternalModuleDependency moduleDependency = (ExternalModuleDependency) dependency;
                externalDependenciesIds.add(moduleDependency.getGroup() + ":" + moduleDependency.getName());
            }
        }

        Set<ResolvedArtifact> resolvedArtifacts = getRuntimeDependencies().get().getResolvedConfiguration().getResolvedArtifacts();
        List<ComponentIdentifier> identifiers = new ArrayList<>();

        for (ResolvedArtifact resolvedArtifact : resolvedArtifacts) {
            ModuleVersionIdentifier moduleId = resolvedArtifact.getModuleVersion().getId();
            if (isDirectDependency(moduleId, externalDependenciesIds)) {
                identifiers.add(resolvedArtifact.getId().getComponentIdentifier());
            }
        }

        return identifiers;
    }

    private boolean isDirectDependency(ModuleVersionIdentifier moduleId, List<String> externalDependenciesIds) {
        String moduleIdName = moduleId.getGroup() + ":" + moduleId.getName();
        return externalDependenciesIds.contains(moduleIdName);
    }

    private Map<String, String> getLicensesIds() {
        Map<String, String> ids = new HashMap<>();
        InputStream resourceStream = getClass().getResourceAsStream("/licenses_ids.txt");
        BufferedReader reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(resourceStream)));

        try {
            while (reader.ready()) {
                String[] parts = reader.readLine().split("\\|");
                String id = parts[0];
                String description = parts[1];
                if (ids.containsKey(id)) {
                    throw new RuntimeException("Duplicated licence id: " + id);
                }
                ids.put(id, description);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return ids;
    }
}
