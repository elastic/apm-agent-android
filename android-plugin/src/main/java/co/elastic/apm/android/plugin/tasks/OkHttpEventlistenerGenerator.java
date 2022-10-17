/*
 * Licensed to Elasticsearch B.V. under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch B.V. licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package co.elastic.apm.android.plugin.tasks;

import static net.bytebuddy.matcher.ElementMatchers.not;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.ClassFileVersion;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.pool.TypePool;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import co.elastic.apm.android.common.MethodCaller;
import co.elastic.apm.android.common.okhttp.eventlistener.CompositeEventListener;
import okhttp3.EventListener;

public abstract class OkHttpEventlistenerGenerator extends DefaultTask {

    @Input
    public abstract Property<String> getJvmTargetVersion();

    @InputFiles
    public abstract ConfigurableFileCollection getAppRuntimeClasspath();

    @OutputDirectory
    public abstract DirectoryProperty getOutputDir();

    @TaskAction
    public void action() {
        String name = CompositeEventListener.getGeneratedName();
        byte[] bytes = getByteBuddy().subclass(CompositeEventListener.class)
                .name(name)
                .method(ElementMatchers.isDeclaredBy(getEventListenerFromProject())
                        .and(not(ElementMatchers.isStatic()))
                        .and(not(ElementMatchers.isConstructor()))
                ).intercept(Advice.to(CompositeEventListenerAdvice.class))
                .make()
                .getBytes();

        String filePath = name.replaceAll("\\.", "/") + ".class";
        storeClassFile(bytes, filePath);
    }

    private TypeDescription getEventListenerFromProject() {
        TypePool typePool = getTypePool(getAppRuntimeClasspath().getFiles());
        return typePool.describe(EventListener.class.getName()).resolve();
    }

    private TypePool getTypePool(Set<File> dependencies) {
        List<ClassFileLocator> classFileLocators = new ArrayList<>();
        try {
            for (File dependency : dependencies) {
                if (dependency.isFile()) {
                    classFileLocators.add(ClassFileLocator.ForJarFile.of(dependency));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return TypePool.Default.of(new ClassFileLocator.Compound(classFileLocators));
    }

    private ByteBuddy getByteBuddy() {
        ClassFileVersion classFileVersion = ClassFileVersion.ofJavaVersionString(getJvmTargetVersion().get());
        return new ByteBuddy(classFileVersion);
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
