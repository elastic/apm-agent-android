package co.elastic.apm.android.agp;

import com.android.build.api.artifact.MultipleArtifact;
import com.android.build.api.variant.Component;

import org.gradle.api.Task;
import org.gradle.api.file.Directory;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileSystemLocationProperty;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskProvider;

import co.elastic.apm.android.agp.api.AgpCompatibleUseCase;
import kotlin.jvm.functions.Function1;

public class Agp72CompatibleUseCase implements AgpCompatibleUseCase {

    @Override
    public void registerAssetGeneratorTask(Component component,
                                           TaskProvider<? extends Task> taskProvider,
                                           Provider<DirectoryProperty> outputDir) {
        component.getArtifacts().use(taskProvider)
                .wiredWith((Function1<Task, FileSystemLocationProperty<Directory>>) task -> outputDir.get())
                .toAppendTo(MultipleArtifact.ASSETS.INSTANCE);
    }
}
