package co.elastic.otel.android.compilation.tools.binarycheck;

import com.android.build.api.artifact.ScopedArtifact;
import com.android.build.api.variant.LibraryAndroidComponentsExtension;
import com.android.build.api.variant.ScopedArtifacts;
import kotlin.Unit;
import kotlinx.validation.ApiValidationExtension;
import kotlinx.validation.KotlinApiBuildTask;
import kotlinx.validation.KotlinApiCompareTask;
import kotlinx.validation.SyncFile;
import org.gradle.api.Project;
import org.gradle.api.artifacts.VersionCatalogsExtension;
import org.gradle.api.file.RegularFile;
import org.gradle.api.tasks.TaskProvider;

/**
 * Workaround for <a
 * href="https://github.com/Kotlin/binary-compatibility-validator/issues/312">BCV #312</a>.
 *
 * <p>With AGP 9.x it's no longer needed to apply the {@code kotlin-android} plugin, so BCV never registers its
 * {@code apiDump}/{@code apiCheck} tasks for Android library modules. This class manually wires
 * them using AGP's variant artifacts API.
 *
 * <p>Remove this once BCV ships a fix.
 */
public final class BcvAndroidTaskWire {

  public static void register(
      Project project,
      ApiValidationExtension extension,
      LibraryAndroidComponentsExtension androidComponents) {
    addKotlinMetadataJvmDependency(project);
    androidComponents.onVariants(
        androidComponents.selector().withBuildType("release"),
        variant -> {
          String dumpFileName = project.getName() + ".api";
          RegularFile apiDumpFile =
              project
                  .getLayout()
                  .getProjectDirectory()
                  .dir(extension.getApiDumpDirectory())
                  .file(dumpFileName);

          TaskProvider<CollectClassesTask> collectClasses =
              project.getTasks().register("collectReleaseClasses", CollectClassesTask.class);

          variant
              .getArtifacts()
              .forScope(ScopedArtifacts.Scope.PROJECT)
              .use(collectClasses)
              .toGet(
                  ScopedArtifact.CLASSES.INSTANCE,
                  CollectClassesTask::getClassesJars,
                  CollectClassesTask::getClassesDirectories);

          TaskProvider<KotlinApiBuildTask> apiBuild =
              project
                  .getTasks()
                  .register(
                      "apiBuild",
                      KotlinApiBuildTask.class,
                      task -> {
                        task.getInputClassesDirs()
                            .from(
                                collectClasses.flatMap(CollectClassesTask::getClassesDirectories));
                        task.getOutputApiFile()
                            .set(
                                project
                                    .getLayout()
                                    .getBuildDirectory()
                                    .file(task.getName() + "/" + dumpFileName));
                        task.getRuntimeClasspath()
                            .from(project.getConfigurations().named("bcv-rt-jvm-cp-resolver"));
                      });

          TaskProvider<KotlinApiCompareTask> apiCheck =
              project
                  .getTasks()
                  .register(
                      "apiCheck",
                      KotlinApiCompareTask.class,
                      task -> {
                        task.setGroup("verification");
                        task.getProjectApiFile().set(apiDumpFile);
                        task.getGeneratedApiFile()
                            .set(apiBuild.flatMap(KotlinApiBuildTask::getOutputApiFile));
                      });

          project
              .getTasks()
              .register(
                  "apiDump",
                  SyncFile.class,
                  task -> {
                    task.getFrom().set(apiBuild.flatMap(KotlinApiBuildTask::getOutputApiFile));
                    task.getTo().set(apiDumpFile);
                  });

          project.getTasks().named("check").configure(task -> task.dependsOn(apiCheck));
          return Unit.INSTANCE;
        });
  }

  /**
   * BCV's {@code withKotlinPluginVersion} only fires when a KGP plugin ID is applied, which doesn't
   * happen with AGP 9.x's built-in Kotlin support. This adds the missing dependency.
   */
  private static void addKotlinMetadataJvmDependency(Project project) {
    String kotlinVersion =
        project
            .getExtensions()
            .getByType(VersionCatalogsExtension.class)
            .named("libs")
            .findVersion("kotlin")
            .get()
            .toString();
    project
        .getDependencies()
        .add("bcv-rt-jvm-cp", "org.jetbrains.kotlin:kotlin-metadata-jvm:" + kotlinVersion);
  }

  private BcvAndroidTaskWire() {}
}
