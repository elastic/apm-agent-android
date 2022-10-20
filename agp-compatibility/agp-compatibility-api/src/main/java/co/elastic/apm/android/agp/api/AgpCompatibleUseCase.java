package co.elastic.apm.android.agp.api;

import com.android.build.api.variant.Component;

import org.gradle.api.Task;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskProvider;

public interface AgpCompatibleUseCase {

    void registerAssetGeneratorTask(Component component,
                                    TaskProvider<? extends Task> taskProvider,
                                    Provider<DirectoryProperty> outputDir);
}
