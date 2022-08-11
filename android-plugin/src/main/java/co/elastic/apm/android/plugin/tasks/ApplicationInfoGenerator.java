package co.elastic.apm.android.plugin.tasks;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.FixedValue;
import net.bytebuddy.matcher.ElementMatchers;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import co.elastic.apm.android.info.ApplicationInfo;

public abstract class ApplicationInfoGenerator extends DefaultTask {

    @Input
    public abstract Property<String> getVersion();

    @Input
    public abstract Property<String> getVariantName();

    @OutputDirectory
    public abstract DirectoryProperty getOutputDir();

    @TaskAction
    public void execute() {
        byte[] classBytes = getApplicationInfoClassBytes();
        File file = getOutputClassFile();
        writeBytesIntoFile(classBytes, file);
    }

    private File getOutputClassFile() {
        File directory = getOutputDir().get().getAsFile();
        String className = ApplicationInfo.class.getName();
        String pathName = className.replace('.', '/') + ".class";
        File relativeFile = new File(pathName);
        File packageDir = new File(directory, relativeFile.getParent());
        if (!packageDir.exists()) {
            packageDir.mkdirs();
        }
        return new File(packageDir, relativeFile.getName());
    }

    private void writeBytesIntoFile(byte[] classBytes, File file) {
        try {
            Files.write(file.toPath(), classBytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private byte[] getApplicationInfoClassBytes() {
        return new ByteBuddy()
                .redefine(ApplicationInfo.class)
                .method(ElementMatchers.named("getVersion"))
                .intercept(FixedValue.value(getVersion().get()))
                .method(ElementMatchers.named("getVariantName"))
                .intercept(FixedValue.value(getVariantName().get()))
                .make()
                .getBytes();
    }
}
