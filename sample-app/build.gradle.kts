import java.util.Properties

plugins {
    alias(rootLibs.plugins.androidApp) apply false
    alias(rootLibs.plugins.androidLib) apply false
    alias(rootLibs.plugins.kotlin.android) apply false
}

val agentProperties = Properties()
val propertiesFile = File(rootDir, "../gradle.properties")
propertiesFile.inputStream().use {
    agentProperties.load(it)
}

val agentVersion = agentProperties["version"]
subprojects {
    if (name == "app") {
        configurations.all {
            File(rootDir, "../instrumentation").listFiles().forEach {
                val dirName = it.name
                if (dirName != "api") {
                    resolutionStrategy.dependencySubstitution {
                        substitute(module("co.elastic.otel.android.instrumentation:${dirName}-library"))
                            .using(module("co.elastic.otel.android.${dirName}:library:$agentVersion"))
                    }
                    resolutionStrategy.dependencySubstitution {
                        substitute(module("co.elastic.otel.android.instrumentation:${dirName}-bytebuddy"))
                            .using(module("co.elastic.otel.android.${dirName}:bytebuddy:$agentVersion"))
                    }
                }
            }
        }
    }
}
