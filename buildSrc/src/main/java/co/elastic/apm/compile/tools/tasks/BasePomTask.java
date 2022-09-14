package co.elastic.apm.compile.tools.tasks;

import org.gradle.api.artifacts.component.ComponentIdentifier;
import org.gradle.api.artifacts.query.ArtifactResolutionQuery;
import org.gradle.api.artifacts.result.ArtifactResult;
import org.gradle.api.artifacts.result.ComponentArtifactsResult;
import org.gradle.api.artifacts.result.ResolvedArtifactResult;
import org.gradle.api.tasks.Internal;
import org.gradle.maven.MavenModule;
import org.gradle.maven.MavenPomArtifact;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class BasePomTask extends BaseTask {

    protected List<ResolvedArtifactResult> getPomArtifacts(List<ComponentIdentifier> fromIds) {
        return getPomArtifacts(getPomBaseQuery().forComponents(fromIds));
    }

    @Internal
    @SuppressWarnings("unchecked")
    protected ArtifactResolutionQuery getPomBaseQuery() {
        return getProject().getDependencies().createArtifactResolutionQuery()
                .withArtifacts(MavenModule.class, MavenPomArtifact.class);
    }

    protected List<ResolvedArtifactResult> getPomArtifacts(ArtifactResolutionQuery query) {
        List<ResolvedArtifactResult> results = new ArrayList<>();

        for (ComponentArtifactsResult component : query.execute().getResolvedComponents()) {
            Set<ArtifactResult> artifacts = component.getArtifacts(MavenPomArtifact.class);
            if (!artifacts.iterator().hasNext()) {
                throw new RuntimeException("No POM file found for: " + component.getId().getDisplayName());
            }
            ArtifactResult artifact = artifacts.iterator().next();
            results.add((ResolvedArtifactResult) artifact);
        }

        return results;
    }
}
