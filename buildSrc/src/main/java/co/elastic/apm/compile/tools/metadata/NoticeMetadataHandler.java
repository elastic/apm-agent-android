package co.elastic.apm.compile.tools.metadata;

import org.gradle.api.Project;
import org.gradle.api.file.RegularFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

public class NoticeMetadataHandler {
    private static final String METADATA_FILE_NAME = "notice.properties";
    private static final String PROPERTY_DEPENDENCIES_HASH = "dependencies.hash";
    private final Properties properties;

    public static RegularFile getMetadataFile(Project project) {
        return project.getLayout().getProjectDirectory().file("metadata/" + METADATA_FILE_NAME);
    }

    public static NoticeMetadataHandler create() {
        return new NoticeMetadataHandler(new Properties());
    }

    public static NoticeMetadataHandler read(File noticePropertiesFile) {
        Properties properties = new Properties();
        try (InputStream in = new FileInputStream(noticePropertiesFile)) {
            properties.load(in);
            return new NoticeMetadataHandler(properties);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private NoticeMetadataHandler(Properties properties) {
        this.properties = properties;
    }

    public void setDependenciesHash(String dependenciesHash) {
        properties.setProperty(PROPERTY_DEPENDENCIES_HASH, dependenciesHash);
    }

    public void save(File output) {
        try (OutputStream out = new FileOutputStream(output)) {
            properties.store(out, null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
