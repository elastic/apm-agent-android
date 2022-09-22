package co.elastic.apm.android.plugin.tasks;

import static net.bytebuddy.matcher.ElementMatchers.not;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.matcher.ElementMatchers;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;

import co.elastic.apm.android.common.MethodCaller;
import co.elastic.apm.android.common.okhttp.eventlistener.CompositeEventListener;
import okhttp3.EventListener;

public abstract class OkHttpEventlistenerGenerator extends DefaultTask {

    @OutputDirectory
    public abstract DirectoryProperty getOutputDir();

    @TaskAction
    public void action() {
        String packageName = CompositeEventListener.class.getPackage().getName();
        String simpleName = "Generated_" + CompositeEventListener.class.getSimpleName();
        String name = packageName + "." + simpleName;
        byte[] bytes = new ByteBuddy().subclass(CompositeEventListener.class)
                .name(name)
                .method(ElementMatchers.isDeclaredBy(EventListener.class)
                        .and(not(ElementMatchers.isStatic()))
                        .and(not(ElementMatchers.isConstructor()))
                ).intercept(Advice.to(CompositeEventListenerAdvice.class))
                .make()
                .getBytes();

        String filePath = name.replaceAll("\\.", "/") + ".class";
        storeClassFile(bytes, filePath);
    }

    private void storeClassFile(byte[] classBytes, String relativePath) {
        File file = new File(getOutputDir().get().getAsFile(), relativePath);
        if (!file.getParentFile().exists()) {
            boolean dirsCreated = file.getParentFile().mkdirs();
            if (!dirsCreated) {
                throw new RuntimeException("Could not create dirs for " + relativePath);
            }
        }
        try {
            Files.write(file.toPath(), classBytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static class CompositeEventListenerAdvice {

        @Advice.OnMethodEnter
        public static void onEnter(@Advice.This MethodCaller owner, @Advice.Origin Method self, @Advice.AllArguments Object[] args) {
            owner.doCall(self, args);
        }
    }
}
