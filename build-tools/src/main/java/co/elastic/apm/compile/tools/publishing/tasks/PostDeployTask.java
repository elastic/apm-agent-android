package co.elastic.apm.compile.tools.publishing.tasks;

import org.gradle.api.DefaultTask;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.TaskAction;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Properties;

public class PostDeployTask extends DefaultTask {

    private static final Logger logger = Logging.getLogger(VersionUtility.class);

    @TaskAction
    public void execute() {
        File gradlePropertiesFile = getGradlePropertiesFile();
        Properties properties = getProperties(gradlePropertiesFile);

        String currentVersion = properties.getProperty("version");
        setGitTag(currentVersion);
        String newVersion = VersionUtility.bumpMinorVersion(currentVersion);
        updateVersion(gradlePropertiesFile, properties, newVersion);
    }

    private void setGitTag(String version) {
        log("Setting git tag to: " + version);
        runCommand("git tag " + version);
        runCommand("git push --tags");
    }

    private void updateVersion(File gradlePropertiesFile, Properties properties, String newVersion) {
        log("Updating version to: " + newVersion);
        properties.setProperty("version", newVersion);
        saveProperties(properties, gradlePropertiesFile);
        runCommand("git commit -a -m \"Preparing for the next release\"");
        runCommand("git push");
    }

    private Properties getProperties(File from) {
        Properties properties = new Properties();
        try (InputStream in = new FileInputStream(from)) {
            properties.load(in);
            return properties;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void saveProperties(Properties properties, File into) {
        try (OutputStream out = new FileOutputStream(into)) {
            properties.store(out, null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private File getGradlePropertiesFile() {
        return getProject().file("gradle.properties");
    }

    private void log(String message) {
        logger.lifecycle(message);
    }

    private void runCommand(String command) {
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("bash", "-c", command);

        try {
            Process process = processBuilder.start();
            StringBuilder output = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println(output);
            } else {
                throw new RuntimeException(output.toString());
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}