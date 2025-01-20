package co.elastic.otel.android.compilation.tools.embedding.tasks;

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar;

import org.gradle.api.file.Directory;
import org.gradle.api.file.RegularFile;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Internal;

public abstract class EmbeddedClassesMergerTask extends ShadowJar {

    @InputFiles
    public abstract ListProperty<RegularFile> getInputJars();

    @InputFiles
    public abstract ListProperty<Directory> getLocalClassesDirs();

    @Internal
    public RegularFileProperty getOutputFile() {
        return (RegularFileProperty) getArchiveFile();
    }
}