package co.elastic.apm.compile.tools.publishing.tasks;

import org.gradle.api.DefaultTask;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PostDeployTask extends DefaultTask {

    private static final Pattern MINOR_VERSION_PATTERN = Pattern.compile("(?<=^\\d{1,2}\\.)\\d+");

    @TaskAction
    public void execute() {
        File gradlePropertiesFile = getGradlePropertiesFile();
        Properties properties = getProperties(gradlePropertiesFile);

        String currentVersion = properties.getProperty("version");
        setGitTag(currentVersion);
        String newVersion = bumpMinorVersion(currentVersion);
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

    private String bumpMinorVersion(String version) {
        log("Bumping minor version for: " + version);
        Matcher minorVersionMatcher = MINOR_VERSION_PATTERN.matcher(version);
        if (minorVersionMatcher.find()) {
            int currentMinorVersion = Integer.parseInt(version.substring(minorVersionMatcher.start(), minorVersionMatcher.end()));
            log("Current minor version is: " + currentMinorVersion);
            String newVersion = version.replaceFirst(MINOR_VERSION_PATTERN.pattern(), String.valueOf(currentMinorVersion + 1));
            log("The new version is: " + newVersion);
            return newVersion;
        } else {
            throw new IllegalArgumentException("Could not find minor version in: " + version);
        }
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
        getProject().getLogger().lifecycle(message);
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