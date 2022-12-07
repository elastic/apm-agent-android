package co.elastic.apm.compile.tools.embedding;

import com.android.build.api.artifact.MultipleArtifact;
import com.android.build.api.variant.AndroidComponentsExtension;
import com.android.build.api.variant.Variant;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.attributes.Attribute;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.Directory;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileTree;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Sync;
import org.gradle.api.tasks.TaskProvider;

import java.io.File;
import java.util.concurrent.Callable;

import co.elastic.apm.compile.tools.embedding.tasks.EmbeddedClassesGathererTask;
import kotlin.Unit;

@SuppressWarnings("unchecked")
public class EmbeddingDependenciesPlugin implements Plugin<Project> {

    private static final Attribute<String> ARTIFACT_TYPE_ATTRIBUTE = Attribute.of("artifactType", String.class);

    @Override
    public void apply(Project project) {
        AndroidComponentsExtension<?, ?, Variant> componentsExtension = project.getExtensions().getByType(AndroidComponentsExtension.class);
        Configuration embeddedClasspath = getEmbeddedClasspath(project);
        Provider<FileCollection> classesProvider = getClassesProvider(project, embeddedClasspath);
        String embeddedClassesTaskName = "embeddedClasses";
        Provider<Directory> classesDir = project.getLayout().getBuildDirectory().dir(embeddedClassesTaskName);
        TaskProvider<Sync> syncEmbeddedClassesTask = project.getTasks().register(embeddedClassesTaskName, Sync.class, sync -> {
            sync.from(classesProvider);
            sync.into(classesDir);
        });

        componentsExtension.onVariants(componentsExtension.selector().all(), variant -> {
            TaskProvider<EmbeddedClassesGathererTask> taskProvider = getEmbeddedClassesGathererTaskProvider(project, classesDir, variant);
            taskProvider.configure(task -> task.dependsOn(syncEmbeddedClassesTask));

            variant.getArtifacts().use(taskProvider)
                    .wiredWith(EmbeddedClassesGathererTask::getOutputDir)
                    .toAppendTo(MultipleArtifact.ALL_CLASSES_DIRS.INSTANCE);
            return Unit.INSTANCE;
        });
    }

    private TaskProvider<EmbeddedClassesGathererTask> getEmbeddedClassesGathererTaskProvider(Project project, Provider<Directory> classesDir, Variant variant) {
        TaskProvider<EmbeddedClassesGathererTask> taskProvider = project.getTasks().register(variant.getName() + "EmbeddedClassesGatherer", EmbeddedClassesGathererTask.class);
        taskProvider.configure(task -> {
            task.getClassesDir().set(classesDir);
            task.getOutputDir().set(project.getLayout().getBuildDirectory().dir(task.getName()));
        });
        return taskProvider;
    }

    private Configuration getEmbeddedClasspath(Project project) {
        Configuration embedded = project.getConfigurations().create("embedded", configuration -> {
            configuration.setCanBeConsumed(false);
            configuration.setCanBeResolved(false);
        });
        Configuration classpath = project.getConfigurations().create("embeddedClasspath", configuration -> {
            configuration.setCanBeResolved(true);
            configuration.setCanBeConsumed(false);
            configuration.extendsFrom(embedded);
            configuration.getAttributes().attribute(ARTIFACT_TYPE_ATTRIBUTE, "android-classes");
        });
        Configuration compileOnly = project.getConfigurations().getByName("compileOnly");
        compileOnly.extendsFrom(embedded);

        return classpath;
    }

    private Provider<FileCollection> getClassesProvider(Project project, Configuration classpath) {
        return project.provider(new LazyFileCollectionProvider(project, classpath));
    }

    private static class LazyFileCollectionProvider implements Callable<FileCollection> {
        private final Project project;
        private final Configuration classpath;
        private FileCollection cachedFileCollection;

        private LazyFileCollectionProvider(Project project, Configuration classpath) {
            this.project = project;
            this.classpath = classpath;
        }

        @Override
        public FileCollection call() {
            if (cachedFileCollection != null) {
                return cachedFileCollection;
            }
            ConfigurableFileCollection fileCollection = project.files();
            for (File file : classpath.getFiles()) {
                if (file.getName().endsWith(".jar")) {
                    FileTree files = project.zipTree(file);
                    fileCollection.from(files);
                }
            }

            cachedFileCollection = fileCollection;
            return fileCollection;
        }
    }
}
