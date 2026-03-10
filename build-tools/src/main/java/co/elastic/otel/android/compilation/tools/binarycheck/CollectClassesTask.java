package co.elastic.otel.android.compilation.tools.binarycheck;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.Directory;
import org.gradle.api.file.RegularFile;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;

/**
 * Adapter task that receives compiled class artifacts from AGP's variant API via {@code
 * ScopedArtifacts.toGet(CLASSES, ...)}. Its input properties are then referenced by downstream BCV
 * tasks.
 *
 * <p>This task has no action, it exists solely to bridge AGP's {@code ListProperty}-based artifact
 * API with BCV's {@code ConfigurableFileCollection}-based inputs.
 */
public abstract class CollectClassesTask extends DefaultTask {

  @InputFiles
  @PathSensitive(PathSensitivity.RELATIVE)
  public abstract ListProperty<Directory> getClassesDirectories();

  @InputFiles
  @PathSensitive(PathSensitivity.NONE)
  public abstract ListProperty<RegularFile> getClassesJars();
}
