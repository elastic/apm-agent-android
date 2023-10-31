package co.elastic.apm.compile.tools.embedding;

import static co.elastic.apm.compile.tools.utils.Constants.ARTIFACT_TYPE_ATTR;

import com.android.build.api.artifact.ScopedArtifact;
import com.android.build.api.variant.AndroidComponentsExtension;
import com.android.build.api.variant.ScopedArtifacts;
import com.android.build.api.variant.Variant;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.tasks.TaskProvider;

import java.util.Collections;

import co.elastic.apm.compile.tools.embedding.extensions.ShadowExtension;
import co.elastic.apm.compile.tools.embedding.tasks.EmbeddedClassesMergerTask;
import kotlin.Unit;

@SuppressWarnings("unchecked")
public class EmbeddingDependenciesPlugin implements Plugin<Project> {

    public static final String EMBEDDED_CLASSPATH_NAME = "embeddedClasspath";
    private static final String SHADOW_EXTENSION_NAME = "shadowJar";
    private ShadowExtension shadowExtension;

    @Override
    public void apply(Project project) {
        AndroidComponentsExtension<?, ?, Variant> componentsExtension = project.getExtensions().getByType(AndroidComponentsExtension.class);
        Configuration embeddedClasspath = getEmbeddedClasspath(project);
        shadowExtension = project.getExtensions().create(SHADOW_EXTENSION_NAME, ShadowExtension.class);

        componentsExtension.onVariants(componentsExtension.selector().all(), variant -> {

            variant.getArtifacts().forScope(ScopedArtifacts.Scope.PROJECT).use(getEmbeddedClassesMergerTaskProvider(project, embeddedClasspath, variant))
                    .toTransform(ScopedArtifact.CLASSES.INSTANCE, EmbeddedClassesMergerTask::getInputJars,
                            EmbeddedClassesMergerTask::getLocalClassesDirs, EmbeddedClassesMergerTask::getOutputFile);
            return Unit.INSTANCE;
        });
    }

    private TaskProvider<EmbeddedClassesMergerTask> getEmbeddedClassesMergerTaskProvider(Project project, Configuration embedded, Variant variant) {
        TaskProvider<EmbeddedClassesMergerTask> taskProvider = project.getTasks().register(variant.getName() + "EmbeddedClassesMerger", EmbeddedClassesMergerTask.class);
        taskProvider.configure(task -> {
            task.from(task.getLocalClassesDirs());
            task.setConfigurations(Collections.singletonList(embedded));
            for (ShadowExtension.Relocation relocation : shadowExtension.getRelocations()) {
                task.relocate(relocation.getPattern().get(), relocation.getDestination().get());
            }
        });
        return taskProvider;
    }

    private Configuration getEmbeddedClasspath(Project project) {
        Configuration embedded = project.getConfigurations().create("embedded", configuration -> {
            configuration.setCanBeConsumed(false);
            configuration.setCanBeResolved(false);
        });
        Configuration classpath = project.getConfigurations().create(EMBEDDED_CLASSPATH_NAME, configuration -> {
            configuration.setCanBeResolved(true);
            configuration.setCanBeConsumed(false);
            configuration.extendsFrom(embedded);
            configuration.getAttributes().attribute(ARTIFACT_TYPE_ATTR, "android-classes");
        });
        Configuration compileOnly = project.getConfigurations().getByName("compileOnly");
        compileOnly.extendsFrom(embedded);

        return classpath;
    }
}
