package co.elastic.otel.android.compilation.tools.sourceheader.subplugins;

import com.diffplug.gradle.spotless.SpotlessExtension;
import com.diffplug.gradle.spotless.SpotlessPlugin;
import com.diffplug.spotless.FormatterStep;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.tasks.TaskProvider;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BaseSourceHeaderPlugin implements Plugin<Project> {
    protected SpotlessExtension spotlessExtension;

    @Override
    public void apply(Project project) {
        project.getPluginManager().apply(SpotlessPlugin.class);
        spotlessExtension = project.getExtensions().getByType(SpotlessExtension.class);

        spotlessExtension.java(javaExtension -> {
            javaExtension.licenseHeader(getLicenseHeader());
            javaExtension.target("src/*/java/**/*.java");
        });
        spotlessExtension.kotlin(kotlinExtension -> {
            kotlinExtension.licenseHeader(getLicenseHeader());
            kotlinExtension.target("src/*/java/**/*.kt");
        });
        spotlessExtension.format("internalNoticeExtension", formatExtension -> {
            formatExtension.target("src/main/java/**/*.kt", "src/main/java/**/*.java");
            formatExtension.addStep(InternalNoticeStep.INSTANCE);
        });
    }

    protected String getLicenseHeader() {
        return "/*\n" +
                " * Licensed to Elasticsearch B.V. under one or more contributor\n" +
                " * license agreements. See the NOTICE file distributed with\n" +
                " * this work for additional information regarding copyright\n" +
                " * ownership. Elasticsearch B.V. licenses this file to you under\n" +
                " * the Apache License, Version 2.0 (the \"License\"); you may\n" +
                " * not use this file except in compliance with the License.\n" +
                " * You may obtain a copy of the License at\n" +
                " *\n" +
                " *\thttp://www.apache.org/licenses/LICENSE-2.0\n" +
                " *\n" +
                " * Unless required by applicable law or agreed to in writing,\n" +
                " * software distributed under the License is distributed on an\n" +
                " * \"AS IS\" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY\n" +
                " * KIND, either express or implied.  See the License for the\n" +
                " * specific language governing permissions and limitations\n" +
                " * under the License.\n" +
                " */\n";
    }

    protected TaskProvider<Task> getSpotlessApply(Project project) {
        return project.getTasks().named("spotlessApply");
    }

    public static class InternalNoticeStep implements FormatterStep {
        public static final InternalNoticeStep INSTANCE = new InternalNoticeStep();

        private InternalNoticeStep() {
        }

        private static final Pattern LAST_IMPORT_PATTERN = Pattern.compile("import .+\\n*(?![\\S\\s]*import .+)");
        private static final Pattern PACKAGE_PATTERN = Pattern.compile("package .+\\n*");
        private static final String INTERNAL_NOTICE = """
                /**
                 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
                 * any time.
                 */
                """;

        @Nonnull
        @Override
        public String getName() {
            return "Internal notice";
        }

        @Nullable
        @Override
        public String format(@Nonnull String rawUnix, File file) {
            if (file.getName().equals("package-info.java")) {
                return null;
            }
            if (!file.getPath().contains("/internal/")) {
                return null;
            }
            if (rawUnix.contains("* This class is internal and is hence not for public use.")) {
                return null;
            }
            Matcher lastImport = LAST_IMPORT_PATTERN.matcher(rawUnix);
            if (lastImport.find()) {
                return insertNotice(rawUnix, lastImport.end());
            }
            Matcher packageDeclaration = PACKAGE_PATTERN.matcher(rawUnix);
            if (packageDeclaration.find()) {
                return insertNotice(rawUnix, packageDeclaration.end());
            }
            return rawUnix;
        }

        private String insertNotice(String rawUnix, int atIndex) {
            StringBuilder builder = new StringBuilder(rawUnix);
            builder.insert(atIndex, INTERNAL_NOTICE);
            return builder.toString();
        }

        @Override
        public void close() throws Exception {
        }
    }
}
