package co.elastic.apm.compile.tools.tasks;

import org.gradle.api.artifacts.result.ResolvedArtifactResult;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.elastic.apm.compile.tools.data.ArtifactIdentification;
import co.elastic.apm.compile.tools.data.ArtifactLicense;
import co.elastic.apm.compile.tools.data.Gav;
import co.elastic.apm.compile.tools.data.License;
import co.elastic.apm.compile.tools.utils.LicensesProvider;
import co.elastic.apm.compile.tools.utils.PomReader;
import co.elastic.apm.compile.tools.utils.TextUtils;

public abstract class CreateDependenciesListTask extends BasePomTask {

    private static final String LICENSE_TITLE_FORMAT = "\nThis product includes software licensed under the '%s' license from the following sources:\n";
    private static final String DEPENDENCY_LINE_FORMAT = "\n - %s";

    @InputFile
    public abstract RegularFileProperty getLicensesFound();

    @OutputFile
    public abstract RegularFileProperty getOutputFile();

    @TaskAction
    public void action() {
        Map<License, List<ArtifactIdentification>> licensedArtifacts = getLicensedArtifacts();
        List<License> sortedLicenses = new ArrayList<>(licensedArtifacts.keySet());
        sortedLicenses.sort(Comparator.comparing(it -> it.name));

        try {
            OutputStream stream = new FileOutputStream(getOutputFile().get().getAsFile());
            boolean firstIteration = true;
            for (License license : sortedLicenses) {
                if (!firstIteration) {
                    TextUtils.addSeparator(stream, '-');
                } else {
                    firstIteration = false;
                }
                TextUtils.writeText(stream, String.format(LICENSE_TITLE_FORMAT, license.name));
                List<ArtifactIdentification> identifications = licensedArtifacts.get(license);
                identifications.sort(Comparator.comparing(it -> it.name));
                for (ArtifactIdentification identification : identifications) {
                    addDependencyLine(stream, identification);
                }
            }
            stream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void addDependencyLine(OutputStream stream, ArtifactIdentification identification) {
        TextUtils.writeText(stream, String.format(DEPENDENCY_LINE_FORMAT, identification.getDisplayName()));
    }

    private Map<License, List<ArtifactIdentification>> getLicensedArtifacts() {
        Map<String, List<Gav>> licensesFound = getLicensesFoundGavs();
        Map<String, String> licenses = LicensesProvider.findLicensesMap();
        Map<License, List<ArtifactIdentification>> licencedArtifacts = new HashMap<>();

        for (String id : licensesFound.keySet()) {
            License license = new License(id, licenses.get(id));
            List<ArtifactIdentification> identifications = new ArrayList<>();
            List<ResolvedArtifactResult> pomArtifacts = getPomArtifactsForGavs(licensesFound.get(id));
            for (ResolvedArtifactResult pomArtifact : pomArtifacts) {
                PomReader reader = new PomReader(pomArtifact.getFile());
                identifications.add(new ArtifactIdentification(reader.getName(), reader.getUrl()));
            }
            licencedArtifacts.put(license, identifications);
        }

        return licencedArtifacts;
    }

    private Map<String, List<Gav>> getLicensesFoundGavs() {
        Map<String, List<Gav>> licensesFoundMap = new HashMap<>();
        try {
            File licensesFound = getLicensesFound().get().getAsFile();
            BufferedReader reader = new BufferedReader(new FileReader(licensesFound));
            String line;
            while ((line = reader.readLine()) != null) {
                ArtifactLicense artifactLicense = ArtifactLicense.parse(line);
                List<Gav> gavItems = licensesFoundMap.getOrDefault(artifactLicense.licenseId, new ArrayList<>());
                gavItems.add(Gav.parseUri(artifactLicense.uri));
                if (!licensesFoundMap.containsKey(artifactLicense.licenseId)) {
                    licensesFoundMap.put(artifactLicense.licenseId, gavItems);
                }
            }
            reader.close();
            return licensesFoundMap;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static class LicensedArtifact {
        final ArtifactIdentification identification;
        final ArtifactLicense license;

        private LicensedArtifact(ArtifactIdentification identification, ArtifactLicense license) {
            this.identification = identification;
            this.license = license;
        }
    }
}
