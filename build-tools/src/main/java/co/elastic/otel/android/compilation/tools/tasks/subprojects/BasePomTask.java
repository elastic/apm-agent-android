package co.elastic.otel.android.compilation.tools.tasks.subprojects;

import org.gradle.api.artifacts.component.ComponentIdentifier;
import org.gradle.api.artifacts.query.ArtifactResolutionQuery;
import org.gradle.api.artifacts.result.ArtifactResult;
import org.gradle.api.artifacts.result.ComponentArtifactsResult;
import org.gradle.api.artifacts.result.ResolvedArtifactResult;
import org.gradle.maven.MavenModule;
import org.gradle.maven.MavenPomArtifact;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import co.elastic.otel.android.compilation.tools.data.Gav;
import co.elastic.otel.android.compilation.tools.tasks.BaseTask;

public class BasePomTask extends BaseTask {

    protected List<ResolvedArtifactResult> getPomArtifacts(Collection<ComponentIdentifier> fromIds) {
        return getPomArtifacts(getPomBaseQuery().forComponents(fromIds));
    }

    @SuppressWarnings("unchecked")
    private ArtifactResolutionQuery getPomBaseQuery() {
        return getProject().getDependencies().createArtifactResolutionQuery()
                .withArtifacts(MavenModule.class, MavenPomArtifact.class);
    }

    protected List<ResolvedArtifactResult> getPomArtifactsForGavs(List<Gav> forGavs) {
        ArtifactResolutionQuery pomQuery = getPomBaseQuery();

        for (Gav gav : forGavs) {
            pomQuery.forModule(gav.group, gav.artifactName, gav.version);
        }

        return getPomArtifacts(pomQuery);
    }

    protected ResolvedArtifactResult getPomArtifactsForGav(Gav forGav) {
        if (forGav == null) {
            return null;
        }

        ArtifactResolutionQuery pomQuery = getPomBaseQuery();

        pomQuery.forModule(forGav.group, forGav.artifactName, forGav.version);

        List<ResolvedArtifactResult> pomArtifacts = getPomArtifacts(pomQuery);
        if (pomArtifacts.isEmpty()) {
            return null;
        }
        return pomArtifacts.get(0);
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
