package co.elastic.apm.compile.tools.tasks;

import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.result.ResolvedArtifactResult;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import co.elastic.apm.compile.tools.data.ArtifactLicense;
import co.elastic.apm.compile.tools.utils.PomReader;

public abstract class PomLicensesCollectorTask extends BasePomTask {

    @InputFiles
    public abstract Property<Configuration> getRuntimeDependencies();

    @Optional
    @InputFile
    public abstract Property<File> getManualLicenseMapping();

    @OutputFile
    public abstract RegularFileProperty getLicensesFound();

    @TaskAction
    public void action() {
        try {
            List<ArtifactLicense> artifactLicenses = extractLicenses(getPomArtifacts(getComponentIdentifiers(getRuntimeDependencies().get())));
            File licensesFoundFile = getLicensesFound().get().getAsFile();
            writeToFile(licensesFoundFile, artifactLicenses);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Map<String, String> getManualMappedLicenses() {
        Map<String, String> mappedLicenses = new HashMap<>();
        File manualMappingFile = getManualLicenseMapping().getOrNull();

        if (manualMappingFile == null) {
            return mappedLicenses;
        }

        try {
            BufferedReader reader = new BufferedReader(new FileReader(manualMappingFile));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                String dependencyUri = parts[0];
                String licenseId = parts[1];
                if (mappedLicenses.containsKey(dependencyUri)) {
                    throw new RuntimeException("Duplicated dependency license mapping for: " + licenseId);
                }
                mappedLicenses.put(dependencyUri, licenseId);
            }
            reader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return mappedLicenses;
    }

    private void writeToFile(File licensesFoundFile, List<ArtifactLicense> artifactLicenses) throws IOException {
        FileWriter fileWriter = new FileWriter(licensesFoundFile);
        PrintWriter printWriter = new PrintWriter(fileWriter);
        boolean firstIteration = true;

        for (ArtifactLicense artifactLicense : artifactLicenses) {
            if (!firstIteration) {
                printWriter.println();
            } else {
                firstIteration = false;
            }
            printWriter.print(artifactLicense.serialize());
        }

        printWriter.close();
    }

    private List<ArtifactLicense> extractLicenses(List<ResolvedArtifactResult> pomArtifacts) throws ParserConfigurationException, SAXException, IOException {
        Map<String, String> manualMappedLicenses = getManualMappedLicenses();
        List<ArtifactLicense> artifactLicenses = new ArrayList<>();
        List<String> notFoundLicenses = new ArrayList<>();

        for (ResolvedArtifactResult pomArtifact : pomArtifacts) {
            String displayName = pomArtifact.getId().getComponentIdentifier().getDisplayName();
            String licenseName;
            if (manualMappedLicenses.containsKey(displayName)) {
                licenseName = manualMappedLicenses.get(displayName);
            } else {
                File pomFile = pomArtifact.getFile();
                PomReader reader = new PomReader(pomFile);
                licenseName = reader.getLicenseId();
            }
            if (licenseName != null) {
                artifactLicenses.add(new ArtifactLicense(displayName, licenseName));
            } else {
                notFoundLicenses.add(displayName);
            }
        }

        if (!notFoundLicenses.isEmpty()) {
            throw new RuntimeException("Could not find a license in the POM file for: " + notFoundLicenses);
        }

        return artifactLicenses;
    }
}
