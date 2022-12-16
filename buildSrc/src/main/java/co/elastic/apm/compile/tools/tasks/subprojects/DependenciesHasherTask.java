package co.elastic.apm.compile.tools.tasks.subprojects;

import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.component.ComponentIdentifier;
import org.gradle.api.artifacts.result.ResolvedArtifactResult;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import co.elastic.apm.compile.tools.utils.TextUtils;

/**
 * Creates a hash out of the set of direct dependencies IDs for the host project, as an identifier
 * for telling when there has been a change in the dependencies.
 */
public abstract class DependenciesHasherTask extends BasePomTask {

    @InputFiles
    public abstract ListProperty<Configuration> getRuntimeDependencies();

    @OutputFile
    public abstract RegularFileProperty getOutputFile();

    @TaskAction
    public void execute() {
        List<ComponentIdentifier> componentIdentifiers = getComponentIdentifiers(getRuntimeDependencies().get());
        List<ResolvedArtifactResult> artifacts = getPomArtifacts(componentIdentifiers);
        List<String> dependenciesUris = extractUrisAlphabeticallyOrdered(artifacts);

        TextUtils.writeText(getOutputFile().get().getAsFile(), createMd5Hash(dependenciesUris));
    }

    private String createMd5Hash(List<String> dependenciesUris) {
        String commaSeparatedDependenciesUris = String.join(",", dependenciesUris);
        return TextUtils.hashMd5(commaSeparatedDependenciesUris);
    }

    private List<String> extractUrisAlphabeticallyOrdered(List<ResolvedArtifactResult> artifacts) {
        List<String> uris = new ArrayList<>();
        for (ResolvedArtifactResult artifact : artifacts) {
            uris.add(artifact.getId().getDisplayName().toLowerCase(Locale.US));
        }
        Collections.sort(uris);
        return uris;
    }
}
